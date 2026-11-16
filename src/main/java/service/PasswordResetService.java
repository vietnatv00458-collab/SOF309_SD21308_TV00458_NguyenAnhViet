package service;

import dao.KhachHangDAO;
import dao.TaiKhoanDAO;
import entity.KhachHang;
import entity.TaiKhoan;
import utils.AppConfig;
import jakarta.servlet.http.HttpSession;
import utils.AuthStore;
import utils.EmailGateway;
import utils.EmailGatewayFactory;
import utils.OtpUtils;
import utils.PasswordUtils;

import java.io.IOException;

public class PasswordResetService {

    private static final String SESSION_EMAIL = "forgotPasswordEmail";
    private static final String SESSION_CUSTOMER_ID = "forgotPasswordCustomerId";
    private static final String SESSION_USERNAME = "forgotPasswordUsername";
    private static final String SESSION_OTP = "forgotPasswordOtp";
    private static final String SESSION_OTP_EXPIRES = "forgotPasswordOtpExpires";
    private static final String SESSION_OTP_ATTEMPTS = "forgotPasswordOtpAttempts";
    private static final long OTP_TTL_MS = 5 * 60 * 1000L;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final AuthStore authStore = new AuthStore();
    private final EmailGateway emailGateway = EmailGatewayFactory.createDefault();

    public Result requestOtp(String email, HttpSession session) {
        String normalizedEmail = trimToNull(email);
        if (!isValidEmail(normalizedEmail)) {
            return Result.failure("Vui lòng nhập email hợp lệ.", false, normalizedEmail);
        }

        KhachHang khachHang = khachHangDAO.findByEmail(normalizedEmail);
        TaiKhoan taiKhoan = null;

        if (khachHang != null && khachHang.getMaKH() != null) {
            taiKhoan = taiKhoanDAO.findById(khachHang.getMaKH());
        }

        if (taiKhoan == null) {
            try {
                taiKhoan = authStore.findByEmail(normalizedEmail);
            } catch (IOException ignored) {
                // DB is the primary source; AuthStore is compatibility fallback.
            }
        }

        if (khachHang == null && taiKhoan != null && taiKhoan.getMaKH() != null) {
            khachHang = khachHangDAO.findById(taiKhoan.getMaKH());
        }

        if (taiKhoan == null) {
            return Result.failure("Không tìm thấy tài khoản gắn với email này trong hệ thống.", false, normalizedEmail);
        }

        String otp = OtpUtils.generateSixDigitCode();
        String subject = "Mã xác thực đặt lại mật khẩu";
        String body = buildOtpEmailBody(otp);

        try {
            emailGateway.send(normalizedEmail, subject, body);
        } catch (RuntimeException ex) {
            return Result.failure("Không gửi được email xác thực. Kiểm tra cấu hình SMTP.", false, normalizedEmail);
        }

        long expiresAt = System.currentTimeMillis() + OTP_TTL_MS;
        session.setAttribute(SESSION_EMAIL, normalizedEmail);
        session.setAttribute(SESSION_CUSTOMER_ID, khachHang != null ? khachHang.getMaKH() : trimToNull(taiKhoan.getMaKH()));
        session.setAttribute(SESSION_USERNAME, trimToNull(taiKhoan.getTenDangNhap()));
        session.setAttribute(SESSION_OTP, otp);
        session.setAttribute(SESSION_OTP_EXPIRES, expiresAt);
        session.setAttribute(SESSION_OTP_ATTEMPTS, 0);

        return Result.success("Đã gửi mã xác thực đến email đã đăng ký.", true, false, normalizedEmail);
    }

    public Result resetPassword(HttpSession session, String email, String otp, String newPassword, String confirmPassword) {
        String sessionEmail = trimToNull((String) session.getAttribute(SESSION_EMAIL));
        String sessionOtp = trimToNull((String) session.getAttribute(SESSION_OTP));
        Long expiresAt = (Long) session.getAttribute(SESSION_OTP_EXPIRES);
        Integer attempts = (Integer) session.getAttribute(SESSION_OTP_ATTEMPTS);
        String customerId = trimToNull((String) session.getAttribute(SESSION_CUSTOMER_ID));
        String username = trimToNull((String) session.getAttribute(SESSION_USERNAME));
        String normalizedEmail = trimToNull(email);
        String normalizedOtp = trimToNull(otp);
        String normalizedNewPassword = trimToNull(newPassword);
        String normalizedConfirmPassword = trimToNull(confirmPassword);

        if (sessionEmail == null || sessionOtp == null || expiresAt == null || (customerId == null && username == null)) {
            return Result.failure("Vui lòng yêu cầu mã xác thực trước.", false, null);
        }

        if (normalizedEmail == null) {
            normalizedEmail = sessionEmail;
        }

        if (!sessionEmail.equalsIgnoreCase(normalizedEmail)) {
            return Result.failure("Email không khớp với yêu cầu đã gửi mã.", true, sessionEmail);
        }

        if (System.currentTimeMillis() > expiresAt) {
            clearOtpSession(session);
            return Result.failure("Mã xác thực đã hết hạn. Vui lòng gửi lại mã mới.", false, null);
        }

        int currentAttempts = attempts != null ? attempts : 0;
        if (currentAttempts >= MAX_OTP_ATTEMPTS) {
            clearOtpSession(session);
            return Result.failure("Bạn đã nhập sai quá nhiều lần. Vui lòng gửi lại mã mới.", false, null);
        }

        if (!sessionOtp.equals(normalizedOtp)) {
            session.setAttribute(SESSION_OTP_ATTEMPTS, currentAttempts + 1);
            return Result.failure("Mã xác thực không đúng.", true, sessionEmail);
        }

        if (!isValidPassword(normalizedNewPassword)) {
            return Result.failure("Mật khẩu mới phải có ít nhất 6 ký tự.", true, sessionEmail);
        }

        if (!normalizedNewPassword.equals(normalizedConfirmPassword)) {
            return Result.failure("Xác nhận mật khẩu không khớp.", true, sessionEmail);
        }

        KhachHang khachHang = customerId != null ? khachHangDAO.findById(customerId) : null;
        TaiKhoan dbTaiKhoan = null;
        if (customerId != null) {
            dbTaiKhoan = taiKhoanDAO.findById(customerId);
        }
        if (dbTaiKhoan == null && username != null) {
            dbTaiKhoan = taiKhoanDAO.findByUsername(username);
        }

        TaiKhoan fileTaiKhoan = null;
        try {
            fileTaiKhoan = authStore.findByEmail(sessionEmail);
        } catch (IOException ignored) {
            // Fallback tiếp theo sẽ thử theo username nếu có.
        }
        if (fileTaiKhoan == null && username != null) {
            try {
                fileTaiKhoan = authStore.findByUsername(username);
            } catch (IOException ignored) {
                // Continue with DB-only update if available.
            }
        }

        if (dbTaiKhoan == null && fileTaiKhoan == null) {
            return Result.failure("Không thể tìm thấy tài khoản để cập nhật mật khẩu.", true, sessionEmail);
        }

        String hashedPassword = PasswordUtils.hash(normalizedNewPassword);

        if (dbTaiKhoan != null) {
            dbTaiKhoan.setMatKhau(hashedPassword);
            try {
                taiKhoanDAO.update(dbTaiKhoan);
            } catch (RuntimeException ex) {
                return Result.failure("Không thể cập nhật mật khẩu lúc này.", true, sessionEmail);
            }
        }

        try {
            boolean updated = false;
            if (sessionEmail != null) {
                updated = authStore.updatePasswordByEmail(sessionEmail, normalizedNewPassword);
            }
            if (!updated && username != null) {
                updated = authStore.updatePasswordByUsername(username, normalizedNewPassword);
            }
            if (!updated && fileTaiKhoan != null) {
                fileTaiKhoan.setMatKhau(hashedPassword);
                if (khachHang != null) {
                    authStore.save(fileTaiKhoan, khachHang.getHoTen(), khachHang.getSdt(), khachHang.getEmail(), khachHang.getDiaChi());
                } else {
                    authStore.save(fileTaiKhoan, null, null, sessionEmail, null);
                }
            }
        } catch (IOException ignored) {
            // DB is the primary source of truth; local auth store is compatibility only.
        }

        clearOtpSession(session);
        session.removeAttribute("user");
        session.removeAttribute("role");
        session.removeAttribute("loginPhone");
        session.removeAttribute(SESSION_USERNAME);
        return Result.success("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.", false, true, null);
    }

    public boolean hasActiveOtp(HttpSession session) {
        if (session == null) {
            return false;
        }

        Object email = session.getAttribute(SESSION_EMAIL);
        Object expiresAt = session.getAttribute(SESSION_OTP_EXPIRES);
        if (email == null || !(expiresAt instanceof Long)) {
            return false;
        }

        if (System.currentTimeMillis() > (Long) expiresAt) {
            clearOtpSession(session);
            return false;
        }

        return true;
    }

    public String getActiveEmail(HttpSession session) {
        if (!hasActiveOtp(session)) {
            return null;
        }

        return trimToNull((String) session.getAttribute(SESSION_EMAIL));
    }

    private void clearOtpSession(HttpSession session) {
        if (session == null) {
            return;
        }

        session.removeAttribute(SESSION_EMAIL);
        session.removeAttribute(SESSION_CUSTOMER_ID);
        session.removeAttribute(SESSION_USERNAME);
        session.removeAttribute(SESSION_OTP);
        session.removeAttribute(SESSION_OTP_EXPIRES);
        session.removeAttribute(SESSION_OTP_ATTEMPTS);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    private String buildOtpEmailBody(String otp) {
        String baseUrl = AppConfig.getApplicationBaseUrl();
        String resetLink = baseUrl != null
                ? baseUrl.replaceAll("/+$", "") + "/forgot-password"
                : null;

        return "Xin chao,\n\n"
                + "Ma xac nhan quen mat khau cua ban la: " + otp + "\n"
                + "Ma nay co hieu luc trong 5 phut.\n\n"
                + (resetLink != null ? "Truy cap: " + resetLink + "\n\n" : "")
                + "Neu ban khong yeu cau dat lai mat khau, hay bo qua email nay.\n";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static final class Result {
        private final boolean success;
        private final String message;
        private final boolean otpStep;
        private final boolean redirectToLogin;
        private final String email;

        private Result(boolean success, String message, boolean otpStep, boolean redirectToLogin, String email) {
            this.success = success;
            this.message = message;
            this.otpStep = otpStep;
            this.redirectToLogin = redirectToLogin;
            this.email = email;
        }

        public static Result success(String message, boolean otpStep, boolean redirectToLogin, String email) {
            return new Result(true, message, otpStep, redirectToLogin, email);
        }

        public static Result failure(String message, boolean otpStep, String email) {
            return new Result(false, message, otpStep, false, email);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isOtpStep() {
            return otpStep;
        }

        public boolean isRedirectToLogin() {
            return redirectToLogin;
        }

        public String getEmail() {
            return email;
        }
    }
}
