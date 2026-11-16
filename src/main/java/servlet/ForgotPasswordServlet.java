package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.PasswordResetService;

import java.io.IOException;

@WebServlet(urlPatterns = {"/forgot-password", "/QuanLyCafe/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private final PasswordResetService passwordResetService = new PasswordResetService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        hydrateViewFromSession(request);
        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();

        String action = trimToNull(request.getParameter("action"));
        if ("reset".equalsIgnoreCase(action)) {
            handleReset(request, response, session);
            return;
        }

        handleSendOtp(request, response, session);
    }

    private void handleSendOtp(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        String email = trimToNull(request.getParameter("email"));
        PasswordResetService.Result result = passwordResetService.requestOtp(email, session);

        if (result.isSuccess()) {
            request.setAttribute("message", result.getMessage());
            request.setAttribute("otpSent", result.isOtpStep());
            request.setAttribute("email", result.getEmail());
        } else {
            request.setAttribute("error", result.getMessage());
            request.setAttribute("email", email);
        }

        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    private void handleReset(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        String email = trimToNull(request.getParameter("email"));
        String otp = trimToNull(request.getParameter("otp"));
        String newPassword = trimToNull(request.getParameter("newPassword"));
        String confirmPassword = trimToNull(request.getParameter("confirmPassword"));

        PasswordResetService.Result result = passwordResetService.resetPassword(session, email, otp, newPassword, confirmPassword);

        if (result.isSuccess() && result.isRedirectToLogin()) {
            session.setAttribute("message", result.getMessage());
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (result.isSuccess()) {
            request.setAttribute("message", result.getMessage());
        } else {
            request.setAttribute("error", result.getMessage());
        }

        request.setAttribute("otpSent", result.isOtpStep());
        request.setAttribute("email", result.getEmail() != null ? result.getEmail() : email);
        request.getRequestDispatcher("/views/auth/forgot-password.jsp").forward(request, response);
    }

    private void hydrateViewFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        if (passwordResetService.hasActiveOtp(session)) {
            request.setAttribute("otpSent", true);
            request.setAttribute("email", passwordResetService.getActiveEmail(session));
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
