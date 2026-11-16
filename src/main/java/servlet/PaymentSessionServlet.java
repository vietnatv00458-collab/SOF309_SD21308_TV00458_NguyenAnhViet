package servlet;

import dao.KhuyenMaiDAO;
import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import entity.KhuyenMai;
import entity.NhanVien;
import utils.OrderPersistenceService;
import utils.PaymentSessionStore;
import utils.PaymentSessionStore.PaymentSession;
import utils.RoleUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;

@WebServlet("/payment-session")
public class PaymentSessionServlet extends HttpServlet {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        handleStatus(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String action = trimToNull(request.getParameter("action"));
        if ("create".equalsIgnoreCase(action)) {
            handleCreate(request, response);
        } else if ("complete".equalsIgnoreCase(action)) {
            handleComplete(request, response);
        } else {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ.");
        }
    }

    private void handleStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionId = trimToNull(request.getParameter("id"));
        if (sessionId == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã phiên thanh toán.");
            return;
        }

        PaymentSession session = PaymentSessionStore.get(sessionId);
        if (session == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy phiên thanh toán.");
            return;
        }

        writeSession(response, session);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ownerKey = trimToNull(request.getParameter("ownerKey"));
        String note = trimToNull(request.getParameter("note"));
        String maNV = trimToNull(request.getParameter("maNV"));
        String cartJson = trimToNull(request.getParameter("cartJson"));
        String couponCode = trimToNull(request.getParameter("couponCode"));
        String deliveryMethod = normalizeDeliveryMethod(request.getParameter("deliveryMethod"));
        String deliveryAddress = trimToNull(request.getParameter("deliveryAddress"));

        TaiKhoan user = resolveCurrentUser(request);
        String maKH = user != null ? trimToNull(user.getMaKH()) : null;
        if (maKH == null) {
            maKH = trimToNull(request.getParameter("maKH"));
        }
        if (maNV == null) {
            maNV = resolveCurrentEmployeeCode(request, user);
        }
        if (maKH == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập tài khoản khách hàng trước khi thanh toán.");
            return;
        }

        if (cartJson == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Giỏ hàng trống, không thể tạo đơn.");
            return;
        }

        if (isDeliveryMethod(deliveryMethod) && deliveryAddress == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Vui lòng nhập địa chỉ giao hàng.");
            return;
        }

        BigDecimal originalAmount = OrderPersistenceService.calculateCartSubtotal(cartJson);
        if (originalAmount.signum() <= 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Giỏ hàng trống, không thể tạo đơn.");
            return;
        }

        KhuyenMai khuyenMai = null;
        if (couponCode != null) {
            khuyenMai = resolvePromotion(couponCode);
            if (khuyenMai == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Mã giảm giá không hợp lệ hoặc đã hết hạn.");
                return;
            }
        }

        DiscountResult discount = calculateDiscountAmount(originalAmount, khuyenMai);
        BigDecimal discountAmount = discount.discountAmount;
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);

        if (finalAmount.signum() <= 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Mã giảm giá không hợp lệ cho đơn hàng này.");
            return;
        }

        PaymentSession session = PaymentSessionStore.create(
                originalAmount,
                discountAmount,
                khuyenMai != null ? khuyenMai.getMaKM() : null,
                khuyenMai != null ? khuyenMai.getTenKM() : null,
                discount.promotionType,
                discount.promotionPercent,
                discount.promotionMaxAmount,
                ownerKey,
                note,
                maNV,
                maKH,
                cartJson,
                deliveryMethod,
                deliveryAddress
        );
        writeSession(response, session);
    }

    private void handleComplete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionId = trimToNull(request.getParameter("id"));
        if (sessionId == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã phiên thanh toán.");
            return;
        }

        String source = trimToNull(request.getParameter("source"));
        PaymentSession session = PaymentSessionStore.complete(sessionId, source);
        if (session == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy phiên thanh toán.");
            return;
        }

        writeSession(response, session);
    }

    private void writeSession(HttpServletResponse response, PaymentSession session) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);

        StringBuilder json = new StringBuilder(256);
        json.append('{');
        appendStringField(json, "sessionId", session.getSessionId());
        appendStringField(json, "status", session.getStatus());
        appendStringField(json, "statusText", session.getStatusText());
        appendStringField(json, "paymentNote", session.getPaymentNote());
        appendStringField(json, "ownerKey", session.getOwnerKey());
        appendStringField(json, "note", session.getNote());
        appendStringField(json, "completedSource", session.getCompletedSource());
        appendStringField(json, "maNV", session.getMaNV());
        appendStringField(json, "maKH", session.getMaKH());
        appendStringField(json, "deliveryMethod", session.getDeliveryMethod());
        appendStringField(json, "deliveryAddress", session.getDeliveryAddress());
        appendStringField(json, "promotionCode", session.getPromotionCode());
        appendStringField(json, "promotionName", session.getPromotionName());
        appendStringField(json, "promotionType", session.getPromotionType());
        appendStringField(json, "maGioHang", session.getMaGioHang());
        appendStringField(json, "maDatHang", session.getMaDatHang());
        appendStringField(json, "maHD", session.getMaHD());
        appendStringField(json, "orderPersistError", session.getOrderPersistError());
        appendNumberField(json, "amount", session.getAmount());
        appendNumberField(json, "originalAmount", session.getOriginalAmount());
        appendNumberField(json, "discountAmount", session.getDiscountAmount());
        appendNumberField(json, "promotionPercent", session.getPromotionPercent());
        appendNumberField(json, "promotionMaxAmount", session.getPromotionMaxAmount());
        appendNumberField(json, "createdAt", session.getCreatedAt());
        appendNumberField(json, "updatedAt", session.getUpdatedAt());
        appendNumberField(json, "completedAt", session.getCompletedAt());
        appendNumberField(json, "expiresAt", session.getExpiresAt());
        appendNumberField(json, "remainingSeconds", session.getRemainingSeconds());
        appendBooleanField(json, "terminal", session.isTerminal());
        appendBooleanField(json, "completed", session.isCompleted());
        appendBooleanField(json, "failed", session.isFailed());
        appendBooleanField(json, "orderPersisted", session.isOrderPersisted());
        json.append('}');

        response.getWriter().write(json.toString());
    }

    private void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);

        StringBuilder json = new StringBuilder(128);
        json.append('{');
        appendStringField(json, "message", message);
        json.append('}');

        response.getWriter().write(json.toString());
    }

    private void appendStringField(StringBuilder json, String key, String value) {
        appendCommaIfNeeded(json);
        json.append('"').append(escapeJson(key)).append("\":");
        if (value == null) {
            json.append("null");
        } else {
            json.append('"').append(escapeJson(value)).append('"');
        }
    }

    private void appendNumberField(StringBuilder json, String key, BigDecimal value) {
        appendCommaIfNeeded(json);
        json.append('"').append(escapeJson(key)).append("\":");
        if (value == null) {
            json.append("0");
        } else {
            json.append(value.toPlainString());
        }
    }

    private void appendNumberField(StringBuilder json, String key, long value) {
        appendCommaIfNeeded(json);
        json.append('"').append(escapeJson(key)).append("\":").append(value);
    }

    private void appendBooleanField(StringBuilder json, String key, boolean value) {
        appendCommaIfNeeded(json);
        json.append('"').append(escapeJson(key)).append("\":").append(value);
    }

    private void appendCommaIfNeeded(StringBuilder json) {
        if (json.length() > 1) {
            json.append(',');
        }
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private BigDecimal parseAmount(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal parsed = new BigDecimal(trimmed);
            return parsed.signum() < 0 ? BigDecimal.ZERO : parsed;
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private KhuyenMai resolvePromotion(String couponCode) {
        String maKM = trimToNull(couponCode);
        if (maKM == null) {
            return null;
        }

        return new KhuyenMaiDAO().findActiveById(maKM, LocalDate.now());
    }

    private boolean isDeliveryMethod(String deliveryMethod) {
        String normalized = normalizeDeliveryMethod(deliveryMethod);
        return "GIAO_HANG".equals(normalized);
    }

    private String normalizeDeliveryMethod(String deliveryMethod) {
        String trimmed = trimToNull(deliveryMethod);
        if (trimmed == null) {
            return "TAI_QUAN";
        }

        String normalized = trimmed.replace(" ", "").replace("-", "_").toUpperCase(Locale.ROOT);
        if ("GIAO_HANG".equals(normalized) || "DELIVERY".equals(normalized)) {
            return "GIAO_HANG";
        }

        return "TAI_QUAN";
    }

    private DiscountResult calculateDiscountAmount(BigDecimal originalAmount, KhuyenMai khuyenMai) {
        BigDecimal subtotal = originalAmount != null && originalAmount.signum() > 0
                ? originalAmount.setScale(0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        if (subtotal.signum() <= 0 || khuyenMai == null) {
            return DiscountResult.empty();
        }

        String type = normalizePromotionType(khuyenMai.getLoaiGiamGia());
        if ("AMOUNT".equals(type)) {
            BigDecimal fixedAmount = normalizeMoney(khuyenMai.getSoTienGiam());
            BigDecimal discountAmount = fixedAmount.min(subtotal);
            return DiscountResult.of(discountAmount, "AMOUNT", BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal percent = khuyenMai.getPhanTramGiam() != null && khuyenMai.getPhanTramGiam() > 0
                ? BigDecimal.valueOf(khuyenMai.getPhanTramGiam())
                : BigDecimal.ZERO;
        if (percent.signum() <= 0) {
            return DiscountResult.empty();
        }

        BigDecimal discountFactor = BigDecimal.ONE.subtract(percent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal discountedTotal = subtotal.multiply(discountFactor).setScale(0, RoundingMode.HALF_UP);
        BigDecimal discountAmount = subtotal.subtract(discountedTotal);
        BigDecimal maxAmount = normalizeMoney(khuyenMai.getMucGiamToiDa());
        if (maxAmount.signum() > 0 && discountAmount.compareTo(maxAmount) > 0) {
            discountAmount = maxAmount;
        }
        if (discountAmount.signum() <= 0) {
            return DiscountResult.empty();
        }

        return DiscountResult.of(discountAmount, "PERCENT", percent, maxAmount);
    }

    private TaiKhoan resolveCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session != null ? session.getAttribute("user") : null;
        return user instanceof TaiKhoan ? (TaiKhoan) user : null;
    }

    private String resolveCurrentEmployeeCode(HttpServletRequest request, TaiKhoan user) {
        if (user == null) {
            return null;
        }

        if (!RoleUtils.isEmployeeRole(user.getVaiTro()) && !RoleUtils.isManagerRole(user.getVaiTro())) {
            return null;
        }

        String maNV = trimToNull(user.getMaNV());
        if (maNV != null && nhanVienDAO.findActiveById(maNV) != null) {
            return maNV;
        }

        HttpSession session = request != null ? request.getSession(false) : null;
        if (session != null) {
            String loginPhone = trimToNull((String) session.getAttribute("loginPhone"));
            if (loginPhone != null) {
                NhanVien nhanVien = nhanVienDAO.findByPhone(loginPhone);
                if (nhanVien != null && trimToNull(nhanVien.getMaNV()) != null) {
                    return trimToNull(nhanVien.getMaNV());
                }
            }
        }

        String tenDangNhap = trimToNull(user.getTenDangNhap());
        if (tenDangNhap == null) {
            return null;
        }

        return taiKhoanDAO.findMaNVByUsername(tenDangNhap);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizePromotionType(String promotionType) {
        String trimmed = trimToNull(promotionType);
        if (trimmed == null) {
            return "";
        }

        String normalized = trimmed.replace(" ", "").replace("-", "").replace("_", "").toUpperCase(Locale.ROOT);
        if ("AMOUNT".equals(normalized) || "SOTIEN".equals(normalized) || "MONEY".equals(normalized)) {
            return "AMOUNT";
        }

        if ("PERCENT".equals(normalized) || "PHANTRAM".equals(normalized)) {
            return "PERCENT";
        }

        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private static final class DiscountResult {
        private final BigDecimal discountAmount;
        private final String promotionType;
        private final BigDecimal promotionPercent;
        private final BigDecimal promotionMaxAmount;

        private DiscountResult(BigDecimal discountAmount, String promotionType, BigDecimal promotionPercent,
                               BigDecimal promotionMaxAmount) {
            this.discountAmount = discountAmount;
            this.promotionType = promotionType;
            this.promotionPercent = promotionPercent;
            this.promotionMaxAmount = promotionMaxAmount;
        }

        private static DiscountResult empty() {
            return new DiscountResult(BigDecimal.ZERO, null, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        private static DiscountResult of(BigDecimal discountAmount, String promotionType,
                                         BigDecimal promotionPercent, BigDecimal promotionMaxAmount) {
            return new DiscountResult(
                    discountAmount != null && discountAmount.signum() > 0 ? discountAmount : BigDecimal.ZERO,
                    promotionType,
                    promotionPercent != null ? promotionPercent : BigDecimal.ZERO,
                    promotionMaxAmount != null ? promotionMaxAmount : BigDecimal.ZERO
            );
        }
    }
}
