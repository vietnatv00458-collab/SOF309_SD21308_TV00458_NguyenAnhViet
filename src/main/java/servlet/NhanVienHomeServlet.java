package servlet;

import dao.DatHangDAO;
import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.DatHang;
import entity.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.RoleUtils;
import utils.OrderStatusUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/nhanvien-home")
public class NhanVienHomeServlet extends HttpServlet {

    private final DatHangDAO datHangDAO = new DatHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        TaiKhoan user = (TaiKhoan) session.getAttribute("user");
        if (!RoleUtils.isEmployeeRole(user != null ? user.getVaiTro() : null)) {
            response.sendRedirect(request.getContextPath() + RoleUtils.homePath(user != null ? user.getVaiTro() : null));
            return;
        }

        request.setAttribute("user", user);
        String currentEmployeeCode = resolveCurrentEmployeeCode(user, session);
        request.setAttribute("currentEmployeeCode", currentEmployeeCode);
        request.setAttribute("deliveryEmployees", nhanVienDAO.findDeliveryEmployees());
        List<DatHang> dsDonHangDangXuLy = datHangDAO.findVisibleOrdersForEmployee(currentEmployeeCode);
        request.setAttribute("dsDonHangDangXuLy", dsDonHangDangXuLy);
        long soDonChoXuLy = 0;
        if (dsDonHangDangXuLy != null) {
            for (DatHang datHang : dsDonHangDangXuLy) {
                if (OrderStatusUtils.isActive(datHang != null ? datHang.getTrangThai() : null)) {
                    soDonChoXuLy++;
                }
            }
        }
        request.setAttribute("soDonChoXuLy", (int) soDonChoXuLy);
        request.getRequestDispatcher("/views/nhanvien/home.jsp").forward(request, response);
    }

    private String resolveCurrentEmployeeCode(TaiKhoan user, HttpSession session) {
        if (user == null) {
            return null;
        }

        String maNV = trimToNull(user.getMaNV());
        if (maNV != null && nhanVienDAO.findActiveById(maNV) != null) {
            return maNV;
        }

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
}
