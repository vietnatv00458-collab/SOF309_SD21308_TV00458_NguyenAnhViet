package servlet;

import dao.LoginDAO;
import entity.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.RoleUtils;

import java.io.IOException;

@WebServlet(urlPatterns = {"/login", "/QuanLyCafe/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String loginId = trimToNull(request.getParameter("loginId"));
        if (loginId == null) {
            loginId = trimToNull(request.getParameter("sdt"));
        }
        if (loginId == null) {
            loginId = trimToNull(request.getParameter("username"));
        }
        String password = trimToNull(request.getParameter("password"));
        String returnUrl = request.getParameter("returnUrl");

        try {
            LoginDAO loginDAO = new LoginDAO();
            TaiKhoan tk = loginDAO.login(loginId, password);

            if (tk != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", tk);
                session.setAttribute("role", tk.getVaiTro());
                if (loginId != null && loginId.matches("\\d{8,15}")) {
                    session.setAttribute("loginPhone", loginId);
                } else {
                    session.removeAttribute("loginPhone");
                }

                response.sendRedirect(resolveRedirectUrl(request, tk.getVaiTro(), returnUrl));
            } else {
            request.setAttribute("error", "Sai số điện thoại hoặc mật khẩu!");
                request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Không thể đăng nhập lúc này. Kiểm tra kết nối cơ sở dữ liệu.");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
        }
    }

    private String resolveRedirectUrl(HttpServletRequest request, String role, String returnUrl) {
        if (RoleUtils.isCustomerRole(role)) {
            String safeReturnUrl = sanitizeReturnUrl(request, returnUrl);
            if (safeReturnUrl != null) {
                return safeReturnUrl;
            }
            return request.getContextPath() + RoleUtils.homePath(role);
        }

        return request.getContextPath() + RoleUtils.homePath(role);
    }

    private String sanitizeReturnUrl(HttpServletRequest request, String returnUrl) {
        if (returnUrl == null || returnUrl.trim().isEmpty()) {
            return null;
        }

        String trimmed = returnUrl.trim();
        if (!trimmed.startsWith("/") || trimmed.startsWith("//")) {
            return null;
        }

        return request.getContextPath() + trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
