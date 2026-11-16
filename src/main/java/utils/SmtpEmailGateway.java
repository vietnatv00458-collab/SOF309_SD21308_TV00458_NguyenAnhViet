package utils;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SmtpEmailGateway implements EmailGateway {

    private final String host;
    private final int port;
    private final boolean startTls;
    private final boolean ssl;
    private final String username;
    private final String password;
    private final String fromAddress;
    private final String fromName;

    public SmtpEmailGateway(String host, int port, boolean startTls, boolean ssl,
                            String username, String password, String fromAddress, String fromName) {
        this.host = trimToNull(host);
        this.port = port;
        this.startTls = startTls;
        this.ssl = ssl;
        this.username = trimToNull(username);
        this.password = trimToNull(password);
        this.fromAddress = trimToNull(fromAddress);
        this.fromName = trimToNull(fromName);
    }

    @Override
    public void send(String toAddress, String subject, String body) {
        String recipient = trimToNull(toAddress);
        if (host == null || fromAddress == null || recipient == null) {
            throw new IllegalStateException("Thiếu cấu hình SMTP hoặc địa chỉ email không hợp lệ.");
        }

        try (Socket socket = createSocket();
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            expectCode(readResponse(reader), 220);
            sendCommand(writer, "EHLO localhost");
            SmtpResponse ehloResponse = readResponse(reader);
            expectCode(ehloResponse, 250);

            if (startTls && !ssl && ehloResponse.message.contains("STARTTLS")) {
                sendCommand(writer, "STARTTLS");
                expectCode(readResponse(reader), 220);
                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                Socket tlsSocket = sslSocketFactory.createSocket(socket, host, port, true);
                sendMail(tlsSocket, recipient, subject, body);
                return;
            }

            if (username != null && password != null) {
                authenticate(writer, reader);
            }

            sendEnvelope(writer, reader, recipient, subject, body);
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể gửi email xác thực.", ex);
        }
    }

    private void sendMail(Socket socket, String recipient, String subject, String body) throws IOException {
        try (Socket closeable = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(closeable.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(closeable.getOutputStream(), StandardCharsets.UTF_8))) {

            sendCommand(writer, "EHLO localhost");
            expectCode(readResponse(reader), 250);
            if (username != null && password != null) {
                authenticate(writer, reader);
            }
            sendEnvelope(writer, reader, recipient, subject, body);
        }
    }

    private void sendEnvelope(BufferedWriter writer, BufferedReader reader, String recipient, String subject, String body)
            throws IOException {
        sendCommand(writer, "MAIL FROM:<" + fromAddress + ">");
        expectCode(readResponse(reader), 250);

        sendCommand(writer, "RCPT TO:<" + recipient + ">");
        expectCode(readResponse(reader), 250, 251);

        sendCommand(writer, "DATA");
        expectCode(readResponse(reader), 354);

        String encodedBody = body == null ? "" : body;
        writeData(writer, "From: " + formatFromHeader());
        writeData(writer, "To: " + recipient);
        writeData(writer, "Subject: " + subject);
        writeData(writer, "MIME-Version: 1.0");
        writeData(writer, "Content-Type: text/plain; charset=UTF-8");
        writeData(writer, "Content-Transfer-Encoding: 8bit");
        writeData(writer, "");
        for (String line : encodedBody.split("\\R", -1)) {
            writeData(writer, line);
        }
        writeData(writer, ".");
        expectCode(readResponse(reader), 250);
        sendCommand(writer, "QUIT");
    }

    private void authenticate(BufferedWriter writer, BufferedReader reader) throws IOException {
        sendCommand(writer, "AUTH LOGIN");
        expectCode(readResponse(reader), 334);
        sendCommand(writer, Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)));
        expectCode(readResponse(reader), 334);
        sendCommand(writer, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
        expectCode(readResponse(reader), 235);
    }

    private Socket createSocket() throws IOException {
        if (ssl) {
            return SSLSocketFactory.getDefault().createSocket(host, port);
        }
        return SocketFactory.getDefault().createSocket(host, port);
    }

    private void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command);
        writer.write("\r\n");
        writer.flush();
    }

    private void writeData(BufferedWriter writer, String line) throws IOException {
        if (line != null && line.startsWith(".")) {
            writer.write(".");
        }
        writer.write(line == null ? "" : line);
        writer.write("\r\n");
        writer.flush();
    }

    private SmtpResponse readResponse(BufferedReader reader) throws IOException {
        StringBuilder message = new StringBuilder();
        String line;
        int code = -1;
        do {
            line = reader.readLine();
            if (line == null) {
                throw new IOException("SMTP server closed the connection.");
            }
            if (line.length() >= 3) {
                code = parseCode(line.substring(0, 3));
            }
            if (message.length() > 0) {
                message.append('\n');
            }
            message.append(line);
        } while (line.length() >= 4 && line.charAt(3) == '-');
        return new SmtpResponse(code, message.toString());
    }

    private void expectCode(SmtpResponse response, int... expected) {
        for (int code : expected) {
            if (response.code == code) {
                return;
            }
        }
        throw new IllegalStateException("SMTP trả về mã không mong đợi: " + response.code + " | " + response.message);
    }

    private int parseCode(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String formatFromHeader() {
        if (fromName == null) {
            return fromAddress;
        }
        return fromName + " <" + fromAddress + ">";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class SmtpResponse {
        final int code;
        final String message;

        SmtpResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
