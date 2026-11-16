package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.RoleUtils;

import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());
        String accessPath = normalizeLegacyContextPath(path);

        if (isStaticOrPublicPath(accessPath)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);
        String role = null;
        if (loggedIn) {
            Object user = session.getAttribute("user");
            if (user instanceof entity.TaiKhoan) {
                role = ((entity.TaiKhoan) user).getVaiTro();
            }
        }

        if (canAccess(accessPath, loggedIn, role)) {
            chain.doFilter(req, res);
        } else {
            response.sendRedirect(contextPath + redirectTarget(accessPath, role));
        }
    }

    private String normalizeLegacyContextPath(String path) {
        if (path == null) {
            return "";
        }

        if (path.equals("/QuanLyCafe")) {
            return "/";
        }

        if (path.startsWith("/QuanLyCafe/")) {
            return path.substring("/QuanLyCafe".length());
        }

        return path;
    }

    private boolean isStaticOrPublicPath(String path) {
        return path.isEmpty()
                || "/".equals(path)
                || "/index.jsp".equals(path)
                || "/home".equals(path)
                || "/login".equals(path)
                || "/forgot-password".equals(path)
                || "/register".equals(path)
                || "/logout".equals(path)
                || "/payment-session".equals(path)
                || "/sepay-ipn".equals(path)
                || "/sepay-webhook".equals(path)
                || path.startsWith("/views/auth/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/uploads/")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".ico");
    }

    private boolean canAccess(String path, boolean loggedIn, String role) {
        if ("/customer-shop".equals(path)) {
            return !loggedIn || RoleUtils.isCustomerRole(role);
        }

        if (path.startsWith("/views/khuyenmai/form")) {
            return loggedIn && RoleUtils.isManagerRole(role);
        }

        if (path.startsWith("/views/khuyenmai/list")) {
            return loggedIn && (RoleUtils.isEmployeeRole(role) || RoleUtils.isManagerRole(role));
        }

        if ("/dashboard".equals(path)
                || path.startsWith("/views/dashboard/")
                || path.startsWith("/views/taikhoan/")
                || path.startsWith("/views/khachhang/")
                || path.startsWith("/views/nhanvien/list")
                || path.startsWith("/views/nhanvien/form")
                || path.startsWith("/views/dathang/")
                || path.startsWith("/views/giohang/")
                || path.startsWith("/views/chitiethoadon/")
                || path.startsWith("/views/chitietgiohang/")
                || "/taikhoan".equals(path)
                || "/khachhang".equals(path)
                || "/nhanvien".equals(path)
                || "/dathang".equals(path)
                || "/giohang".equals(path)
                || "/chitiethoadon".equals(path)
                || "/chitietgiohang".equals(path)) {
            return loggedIn && RoleUtils.isManagerRole(role);
        }

        if (path.startsWith("/views/sanpham/form")) {
            return loggedIn && RoleUtils.isManagerRole(role);
        }

        if ("/hoadon".equals(path)) {
            return loggedIn && (RoleUtils.isEmployeeRole(role) || RoleUtils.isDeliveryRole(role) || RoleUtils.isManagerRole(role));
        }

        if (path.startsWith("/views/hoadon/")) {
            return loggedIn && (RoleUtils.isEmployeeRole(role) || RoleUtils.isDeliveryRole(role) || RoleUtils.isManagerRole(role));
        }

        if ("/nhanvien-home".equals(path)
                || "/nhanvien-donhang".equals(path)
                || "/giaohang-home".equals(path)
                || "/giaohang-donhang".equals(path)
                || path.startsWith("/views/nhanvien/home")
                || path.startsWith("/views/giaohang/")
                || path.startsWith("/views/sanpham/")
                || path.startsWith("/views/lichsugiasanpham/")
                || "/sanpham".equals(path)
                || "/lichsugiasanpham".equals(path)
                || "/khuyenmai".equals(path)) {
            if ("/giaohang-home".equals(path) || "/giaohang-donhang".equals(path) || path.startsWith("/views/giaohang/")) {
                return loggedIn && (RoleUtils.isDeliveryRole(role) || RoleUtils.isManagerRole(role));
            }

            if ("/khuyenmai".equals(path)) {
                return loggedIn && (RoleUtils.isEmployeeRole(role) || RoleUtils.isManagerRole(role));
            }

            return loggedIn && (RoleUtils.isEmployeeRole(role) || RoleUtils.isManagerRole(role));
        }

        if (path.startsWith("/views/customer/")) {
            return !loggedIn || RoleUtils.isCustomerRole(role);
        }

        if (path.startsWith("/views/common/")) {
            return loggedIn;
        }

        return loggedIn;
    }

    private String redirectTarget(String path, String role) {
        if ("/customer-shop".equals(path) || path.startsWith("/views/customer/")) {
            return RoleUtils.homePath(role);
        }

        if (RoleUtils.isManagerRole(role)) {
            return RoleUtils.homePath(role);
        }

        if (RoleUtils.isEmployeeRole(role)) {
            return RoleUtils.homePath(role);
        }

        if (RoleUtils.isCustomerRole(role)) {
            return RoleUtils.homePath(role);
        }

        if ("/giaohang-home".equals(path)
                || "/giaohang-donhang".equals(path)
                || path.startsWith("/views/giaohang/")) {
            return "/login";
        }

        return "/login";
    }
}
