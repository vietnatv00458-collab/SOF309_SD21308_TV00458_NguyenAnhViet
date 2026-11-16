package servlet;

import dao.DatHangDAO;
import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.DatHang;
import entity.NhanVien;
import entity.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.RoleUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/giaohang-home")
public class GiaoHangHomeServlet extends HttpServlet {

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
        if (!RoleUtils.isDeliveryRole(user != null ? user.getVaiTro() : null) && !RoleUtils.isManagerRole(user != null ? user.getVaiTro() : null)) {
            response.sendRedirect(request.getContextPath() + RoleUtils.homePath(user != null ? user.getVaiTro() : null));
            return;
        }

        String currentEmployeeCode = resolveCurrentEmployeeCode(user, session);
        List<DatHang> dsDonHang = datHangDAO.findAssignedDeliveryOrders(currentEmployeeCode);
        request.setAttribute("user", user);
        request.setAttribute("currentEmployeeCode", currentEmployeeCode);
        request.setAttribute("dsDonHangGiao", dsDonHang);
        request.setAttribute("soDonDangGiao", dsDonHang != null ? dsDonHang.size() : 0);
        request.getRequestDispatcher("/views/giaohang/home.jsp").forward(request, response);
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
