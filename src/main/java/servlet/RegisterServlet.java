package servlet;

import dao.KhachHangDAO;
import dao.TaiKhoanDAO;
import entity.KhachHang;
import entity.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.AuthStore;
import utils.PasswordUtils;

import java.io.IOException;

@WebServlet(urlPatterns = {"/register", "/QuanLyCafe/register"})
public class RegisterServlet extends HttpServlet {

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final AuthStore authStore = new AuthStore();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String hoTen = trimToNull(request.getParameter("hoTen"));
        String sdt = trimToNull(request.getParameter("sdt"));
        String email = trimToNull(request.getParameter("email"));
        String diaChi = trimToNull(request.getParameter("diaChi"));
        String matKhau = trimToNull(request.getParameter("matKhau"));
        String xacNhanMatKhau = trimToNull(request.getParameter("xacNhanMatKhau"));

        if (isBlank(hoTen) || isBlank(sdt) || isBlank(matKhau) || isBlank(xacNhanMatKhau)) {
            request.setAttribute("error", "Họ tên, số điện thoại và mật khẩu không được để trống!");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!matKhau.equals(xacNhanMatKhau)) {
            request.setAttribute("error", "Xác nhận mật khẩu không khớp!");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (matKhau.length() < 6) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        KhachHang existingCustomer = khachHangDAO.findByPhone(sdt);
        String maKH = existingCustomer != null ? existingCustomer.getMaKH() : khachHangDAO.generateNextMaKH();
        if (taiKhoanDAO.existsById(maKH)) {
            request.setAttribute("error", "Số điện thoại này đã có tài khoản. Hãy đăng nhập.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        String emailValue = email != null
                ? email
                : (existingCustomer != null ? trimToNull(existingCustomer.getEmail()) : "");
        String diaChiValue = diaChi != null
                ? diaChi
                : (existingCustomer != null ? trimToNull(existingCustomer.getDiaChi()) : "");

        KhachHang khachHang = new KhachHang();
        khachHang.setMaKH(maKH);
        khachHang.setHoTen(hoTen);
        khachHang.setSdt(sdt);
        khachHang.setEmail(emailValue);
        khachHang.setDiaChi(diaChiValue);

        TaiKhoan customerAccount = new TaiKhoan();
        customerAccount.setMaNV(null);
        customerAccount.setMaKH(maKH);
        customerAccount.setTenDangNhap(maKH);
        customerAccount.setMatKhau(PasswordUtils.hash(matKhau));
        customerAccount.setVaiTro("KHACHHANG");
        customerAccount.setTrangThai(true);

        try {
            taiKhoanDAO.saveCustomerRegistration(khachHang, customerAccount);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi đăng ký khách hàng: " + e.getMessage());
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        try {
            authStore.save(customerAccount, hoTen, sdt, emailValue, diaChiValue);
        } catch (IOException ignored) {
            // Database là nguồn lưu chính; AuthStore chỉ giữ tương thích đăng nhập cũ.
        }

        TaiKhoan customerSession = new TaiKhoan();
        customerSession.setMaNV(null);
        customerSession.setMaKH(maKH);
        customerSession.setTenDangNhap(maKH);
        customerSession.setMatKhau(customerAccount.getMatKhau());
        customerSession.setVaiTro("KHACHHANG");
        customerSession.setTrangThai(true);

        HttpSession session = request.getSession(true);
        session.setAttribute("user", customerSession);
        session.setAttribute("role", "KHACHHANG");

        response.sendRedirect(request.getContextPath() + "/customer-shop");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
