package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.PaymentSessionStore;
import utils.PaymentSessionStore.PaymentSession;
import utils.SePayConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/sepay-ipn", "/sepay-webhook"})
public class SePayIpnServlet extends HttpServlet {

    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile(
            "\"((?:\\\\.|[^\"\\\\])*)\"\\s*:\\s*(\"((?:\\\\.|[^\"\\\\])*)\"|null|-?\\d+(?:\\.\\d+)?|true|false)"
    );
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("\\b(PAY-[A-F0-9]{12})\\b", Pattern.CASE_INSENSITIVE);
    private static final ConcurrentHashMap<String, Long> PROCESSED_TRANSACTION_IDS = new ConcurrentHashMap<>();
    private static final long DEDUP_RETENTION_MILLIS = TimeUnit.HOURS.toMillis(24);
    private static final long MAX_HMAC_CLOCK_SKEW_SECONDS = 300L;
    private static volatile String lastWebhookSummary = "Chua nhan webhook nao.";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);

        String rawBody = readRequestBody(request);
        if (!isAuthorized(request, rawBody)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "{\"success\":false,\"message\":\"Unauthorized\"}");
            return;
        }

        Map<String, String> payload = parsePayload(rawBody, request.getContentType());
        if (payload.isEmpty()) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "{\"success\":false,\"message\":\"Invalid payload\"}");
            return;
        }
        lastWebhookSummary = payload.toString();

        String transactionId = buildDedupKey(payload);
        if (!markTransactionSeen(transactionId)) {
            writeSuccess(response);
            return;
        }

        if (!isIncomingTransfer(payload)) {
            writeSuccess(response);
            return;
        }

        String accountNumber = firstNonBlank(payload, "account_number", "accountNumber");
        if (!matchesConfiguredAccount(accountNumber)) {
            writeSuccess(response);
            return;
        }

        BigDecimal amount = parseAmount(firstNonBlank(payload, "amount", "transferAmount", "transfer_amount", "money"));
        if (amount == null || amount.signum() <= 0) {
            writeSuccess(response);
            return;
        }

        String sessionId = extractSessionId(payload);
        PaymentSession session = sessionId != null ? PaymentSessionStore.get(sessionId) : null;
        if (session == null || session.getAmount().compareTo(amount) != 0) {
            session = PaymentSessionStore.findLatestPendingByAmount(amount);
        }

        if (session == null) {
            writeSuccess(response);
            return;
        }

        PaymentSessionStore.complete(session.getSessionId(), "sepay-ipn");
        writeSuccess(response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("SePay webhook is ready\nLast payload: " + lastWebhookSummary);
    }

    private boolean isAuthorized(HttpServletRequest request, String rawBody) {
        String signature = trimToNull(request.getHeader("X-SePay-Signature"));
        String timestampText = trimToNull(request.getHeader("X-SePay-Timestamp"));
        String secretKey = SePayConfig.getWebhookSecretKey();

        if (signature != null || timestampText != null) {
            return verifyHmacSignature(rawBody, signature, timestampText, secretKey);
        }

        String expectedApiKey = SePayConfig.getWebhookApiKey();
        String authorization = trimToNull(request.getHeader("Authorization"));
        if (authorization == null) {
            return true;
        }

        if (expectedApiKey == null || !authorization.regionMatches(true, 0, "Apikey ", 0, 7)) {
            return false;
        }

        String providedApiKey = authorization.substring(7).trim();
        return MessageDigest.isEqual(
                expectedApiKey.getBytes(StandardCharsets.UTF_8),
                providedApiKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    private boolean verifyHmacSignature(String rawBody, String signature, String timestampText, String secretKey) {
        if (secretKey == null || signature == null || timestampText == null) {
            return false;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampText.trim());
        } catch (NumberFormatException ex) {
            return false;
        }

        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > MAX_HMAC_CLOCK_SKEW_SECONDS) {
            return false;
        }

        try {
            String expectedSignature = "sha256=" + hmacSha256Hex(timestamp + "." + rawBody, secretKey);
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.trim().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            return false;
        }
    }

    private String hmacSha256Hex(String value, String secretKey) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format(Locale.ROOT, "%02x", b));
        }
        return hex.toString();
    }

    private Map<String, String> parsePayload(String rawBody, String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        if (normalizedContentType.startsWith("application/x-www-form-urlencoded")) {
            return parseUrlEncodedPayload(rawBody);
        }

        if (normalizedContentType.startsWith("multipart/form-data")) {
            return parseMultipartPayload(rawBody, contentType);
        }

        return parseJsonPayload(rawBody);
    }

    private Map<String, String> parseUrlEncodedPayload(String rawBody) {
        Map<String, String> values = new HashMap<>();
        if (rawBody == null || rawBody.trim().isEmpty()) {
            return values;
        }

        String[] pairs = rawBody.split("&");
        for (String pair : pairs) {
            if (pair == null || pair.isEmpty()) {
                continue;
            }

            int separator = pair.indexOf('=');
            String keyPart = separator >= 0 ? pair.substring(0, separator) : pair;
            String valuePart = separator >= 0 ? pair.substring(separator + 1) : "";

            try {
                String key = URLDecoder.decode(keyPart, StandardCharsets.UTF_8.name());
                String value = URLDecoder.decode(valuePart, StandardCharsets.UTF_8.name());
                values.put(key, trimToNull(value));
            } catch (Exception ignored) {
                // Bỏ qua field không giải mã được.
            }
        }

        return values;
    }

    private Map<String, String> parseMultipartPayload(String rawBody, String contentType) {
        Map<String, String> values = new HashMap<>();
        String boundary = extractBoundary(contentType);
        if (boundary == null || rawBody == null || rawBody.isEmpty()) {
            return values;
        }

        String delimiter = "--" + boundary;
        String[] parts = rawBody.split(Pattern.quote(delimiter));
        for (String part : parts) {
            if (part == null) {
                continue;
            }

            String trimmedPart = part.trim();
            if (trimmedPart.isEmpty() || "--".equals(trimmedPart)) {
                continue;
            }

            int headerEnd = trimmedPart.indexOf("\r\n\r\n");
            int separatorLength = 4;
            if (headerEnd < 0) {
                headerEnd = trimmedPart.indexOf("\n\n");
                separatorLength = 2;
            }

            if (headerEnd < 0) {
                continue;
            }

            String headers = trimmedPart.substring(0, headerEnd);
            String body = trimmedPart.substring(headerEnd + separatorLength);
            String fieldName = extractMultipartFieldName(headers);
            if (fieldName == null) {
                continue;
            }

            values.put(fieldName, trimToNull(stripTrailingLineBreaks(body)));
        }

        return values;
    }

    private Map<String, String> parseJsonPayload(String rawBody) {
        Map<String, String> values = new HashMap<>();
        if (rawBody == null || rawBody.trim().isEmpty()) {
            return values;
        }

        Matcher matcher = JSON_FIELD_PATTERN.matcher(rawBody);
        while (matcher.find()) {
            String key = unescapeJsonString(matcher.group(1));
            String rawValue = matcher.group(2);
            String value = rawValue;
            if (rawValue != null && rawValue.startsWith("\"")) {
                value = unescapeJsonString(matcher.group(3));
            } else if ("null".equals(rawValue)) {
                value = null;
            }
            values.put(key, value);
        }

        return values;
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder(256);
        try (BufferedReader reader = request.getReader()) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
        }
        return builder.toString();
    }

    private String buildDedupKey(Map<String, String> payload) {
        String transactionId = firstNonBlank(payload, "transaction_id", "transactionId", "id");
        if (transactionId != null) {
            return transactionId.trim();
        }

        String fallbackSeed = String.join("|",
                valueOrEmpty(firstNonBlank(payload, "gateway")),
                valueOrEmpty(firstNonBlank(payload, "transactionDate", "transaction_date")),
                valueOrEmpty(firstNonBlank(payload, "accountNumber", "account_number")),
                valueOrEmpty(firstNonBlank(payload, "content")),
                valueOrEmpty(firstNonBlank(payload, "description")),
                valueOrEmpty(firstNonBlank(payload, "amount", "transferAmount", "transfer_amount")),
                valueOrEmpty(firstNonBlank(payload, "referenceCode", "reference_code"))
        );

        return "FALLBACK-" + UUID.nameUUIDFromBytes(fallbackSeed.getBytes(StandardCharsets.UTF_8));
    }

    private boolean markTransactionSeen(String transactionId) {
        String key = trimToNull(transactionId);
        if (key == null) {
            return true;
        }

        purgeOldTransactions();
        return PROCESSED_TRANSACTION_IDS.putIfAbsent(key, System.currentTimeMillis()) == null;
    }

    private void purgeOldTransactions() {
        long now = System.currentTimeMillis();
        PROCESSED_TRANSACTION_IDS.entrySet().removeIf(entry -> now - entry.getValue() > DEDUP_RETENTION_MILLIS);
    }

    private boolean isIncomingTransfer(Map<String, String> payload) {
        String transferType = firstNonBlank(payload, "transfer_type", "transferType");
        if (transferType == null) {
            return firstNonBlank(payload, "content", "description") != null
                    && firstNonBlank(payload, "amount", "transferAmount", "transfer_amount", "money") != null;
        }

        String normalized = transferType.trim().toLowerCase(Locale.ROOT);
        return "credit".equals(normalized) || "in".equals(normalized);
    }

    private boolean matchesConfiguredAccount(String accountNumber) {
        if (trimToNull(accountNumber) == null) {
            return true;
        }

        String expectedAccountNumber = SePayConfig.getAccountNumber();
        if (expectedAccountNumber == null) {
            return true;
        }

        return expectedAccountNumber.equals(trimToNull(accountNumber));
    }

    private String extractSessionId(Map<String, String> payload) {
        String[] candidates = {
                firstNonBlank(payload, "code"),
                firstNonBlank(payload, "payment_code"),
                firstNonBlank(payload, "content"),
                firstNonBlank(payload, "description")
        };

        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }

            Matcher matcher = SESSION_ID_PATTERN.matcher(candidate);
            if (matcher.find()) {
                return matcher.group(1).toUpperCase(Locale.ROOT);
            }
        }

        return null;
    }

    private BigDecimal parseAmount(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        String normalized = trimmed.replaceAll("[^0-9.\\-]", "");
        if (normalized.isEmpty() || "-".equals(normalized)) {
            return null;
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeContentType(String contentType) {
        String trimmed = trimToNull(contentType);
        if (trimmed == null) {
            return "";
        }

        int separator = trimmed.indexOf(';');
        String normalized = separator >= 0 ? trimmed.substring(0, separator) : trimmed;
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    private String extractBoundary(String contentType) {
        String trimmed = trimToNull(contentType);
        if (trimmed == null) {
            return null;
        }

        String[] segments = trimmed.split(";");
        for (String segment : segments) {
            if (segment == null) {
                continue;
            }

            String candidate = segment.trim();
            if (candidate.toLowerCase(Locale.ROOT).startsWith("boundary=")) {
                String boundary = candidate.substring("boundary=".length()).trim();
                if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() >= 2) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
                return trimToNull(boundary);
            }
        }

        return null;
    }

    private String extractMultipartFieldName(String headers) {
        if (headers == null) {
            return null;
        }

        String[] headerLines = headers.split("\\r?\\n");
        for (String line : headerLines) {
            if (line == null) {
                continue;
            }

            String trimmedLine = line.trim();
            if (!trimmedLine.toLowerCase(Locale.ROOT).startsWith("content-disposition:")) {
                continue;
            }

            Matcher matcher = Pattern.compile("name=\"([^\"]+)\"").matcher(trimmedLine);
            if (matcher.find()) {
                return trimToNull(matcher.group(1));
            }
        }

        return null;
    }

    private String stripTrailingLineBreaks(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value;
        while (trimmed.endsWith("\r") || trimmed.endsWith("\n")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed;
    }

    private String firstNonBlank(Map<String, String> payload, String... keys) {
        if (payload == null || keys == null) {
            return null;
        }

        for (String key : keys) {
            if (key == null) {
                continue;
            }

            String value = trimToNull(payload.get(key));
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String unescapeJsonString(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        StringBuilder unescaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch != '\\' || i + 1 >= value.length()) {
                unescaped.append(ch);
                continue;
            }

            char next = value.charAt(++i);
            switch (next) {
                case '"':
                    unescaped.append('"');
                    break;
                case '\\':
                    unescaped.append('\\');
                    break;
                case '/':
                    unescaped.append('/');
                    break;
                case 'b':
                    unescaped.append('\b');
                    break;
                case 'f':
                    unescaped.append('\f');
                    break;
                case 'n':
                    unescaped.append('\n');
                    break;
                case 'r':
                    unescaped.append('\r');
                    break;
                case 't':
                    unescaped.append('\t');
                    break;
                case 'u':
                    if (i + 4 < value.length()) {
                        String hex = value.substring(i + 1, i + 5);
                        try {
                            unescaped.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException ex) {
                            unescaped.append('\\').append('u').append(hex);
                            i += 4;
                        }
                    } else {
                        unescaped.append('\\').append('u');
                    }
                    break;
                default:
                    unescaped.append(next);
                    break;
            }
        }
        return unescaped.toString();
    }

    private void writeSuccess(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("Xu ly webhook thanh cong");
    }

    private void writeJson(HttpServletResponse response, int statusCode, String body) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(body);
    }
}
