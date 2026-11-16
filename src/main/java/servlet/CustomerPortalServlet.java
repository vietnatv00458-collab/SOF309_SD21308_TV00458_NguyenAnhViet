package servlet;

import entity.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(urlPatterns = {"/customer-portal", "/QuanLyCafe/customer-portal"})
public class CustomerPortalServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        TaiKhoan user = (TaiKhoan) session.getAttribute("user");
        if (!isCustomerRole(user != null ? user.getVaiTro() : null)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String shopUrl = getServletContext().getInitParameter("customerShopUrl");
        if (shopUrl == null || shopUrl.trim().isEmpty()) {
            shopUrl = request.getContextPath() + "/customer-shop";
        } else {
            shopUrl = resolveRedirectUrl(request, shopUrl.trim());
        }

        request.setAttribute("shopUrl", shopUrl);
        request.getRequestDispatcher("/views/customer/portal.jsp").forward(request, response);
    }

    private String resolveRedirectUrl(HttpServletRequest request, String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        if (url.startsWith("/")) {
            return request.getContextPath() + url;
        }

        return request.getContextPath() + "/" + url;
    }

    private boolean isCustomerRole(String role) {
        if (role == null) {
            return false;
        }

        return "KHACHHANG".equalsIgnoreCase(role)
                || "KhachHang".equalsIgnoreCase(role)
                || "KháchHàng".equalsIgnoreCase(role)
                || "Khach Hang".equalsIgnoreCase(role);
    }
}
