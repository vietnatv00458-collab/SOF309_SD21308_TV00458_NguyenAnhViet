package servlet;

import dao.KhachHangDAO;
import dao.KhuyenMaiDAO;
import dao.SanPhamDAO;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.SanPham;
import entity.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.BannerImageUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(urlPatterns = {"/customer-shop", "/QuanLyCafe/customer-shop"})
public class CustomerShopServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        TaiKhoan user = session != null ? (TaiKhoan) session.getAttribute("user") : null;
        KhachHang customerProfile = resolveCustomerProfile(user);
        boolean hasCustomerProfile = customerProfile != null;

        request.setAttribute("user", user);
        request.setAttribute("customerDisplayName", resolveCustomerDisplayName(user));
        request.setAttribute("cartOwnerKey", resolveCartOwnerKey(user));
        request.setAttribute("customerProfile", customerProfile);
        request.setAttribute("customerHasProfile", hasCustomerProfile);
        request.setAttribute("customerProfileAction", hasCustomerProfile ? "update" : "create");
        request.setAttribute("customerDeleteUrl", buildDeleteUrl(request, user));
        request.setAttribute("dsSanPham", loadShopProducts());
        request.setAttribute("dsKhuyenMai", loadActivePromotions());
        List<String> promoBannerImages = BannerImageUtil.loadPromoBannerImages();
        request.setAttribute("promoBannerImages", promoBannerImages);
        request.setAttribute("promoBannerCarouselImages", BannerImageUtil.buildPromoBannerCarouselImages(promoBannerImages));
        request.getRequestDispatcher("/views/customer/shop.jsp").forward(request, response);
    }

    private List<SanPham> loadShopProducts() {
        return new SanPhamDAO().findAll();
    }

    private List<KhuyenMai> loadActivePromotions() {
        return new KhuyenMaiDAO().findActive(LocalDate.now());
    }

    private KhachHang resolveCustomerProfile(TaiKhoan user) {
        if (user == null) {
            return null;
        }

        String maKH = trimToNull(user.getMaKH());
        if (maKH == null) {
            return null;
        }

        return new KhachHangDAO().findById(maKH);
    }

    private String resolveCustomerDisplayName(TaiKhoan user) {
        if (user == null) {
            return "Khách hàng";
        }

        String maKH = trimToNull(user.getMaKH());
        if (maKH != null) {
            KhachHang khachHang = new KhachHangDAO().findById(maKH);
            if (khachHang != null) {
                String hoTen = trimToNull(khachHang.getHoTen());
                if (hoTen != null) {
                    return hoTen;
                }
            }
        }

        return "Khách hàng";
    }

    private String resolveCartOwnerKey(TaiKhoan user) {
        String username = user != null ? trimToNull(user.getTenDangNhap()) : null;
        if (username == null) {
            return "guest";
        }

        return username.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String buildDeleteUrl(HttpServletRequest request, TaiKhoan user) {
        String maKH = user != null ? trimToNull(user.getMaKH()) : null;
        if (maKH == null) {
            return request.getContextPath() + "/logout";
        }

        return request.getContextPath() + "/khachhang?action=delete&id=" + maKH + "&source=customer-shop";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
