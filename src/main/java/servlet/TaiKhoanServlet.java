package servlet;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import entity.NhanVien;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.PasswordUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/taikhoan")
public class TaiKhoanServlet extends HttpServlet {

    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listTaiKhoan(request, response);
            return;
        }

        switch (action) {
            case "add":
                showAddForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteTaiKhoan(request, response);
                break;
            default:
                listTaiKhoan(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertTaiKhoan(request, response);
        } else if ("update".equals(action)) {
            updateTaiKhoan(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/taikhoan");
        }
    }

    private void listTaiKhoan(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<TaiKhoan> dsTaiKhoan = dao.findAll();
        request.setAttribute("dsTaiKhoan", dsTaiKhoan);
        request.getRequestDispatcher("/views/taikhoan/list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareAddForm(request, isDeliveryCreateMode(request));
        request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maNV = request.getParameter("id");

        if (isBlank(maNV)) {
            response.sendRedirect(request.getContextPath() + "/taikhoan");
            return;
        }

        TaiKhoan taiKhoan = dao.findById(maNV.trim());

        if (taiKhoan == null) {
            response.sendRedirect(request.getContextPath() + "/taikhoan");
            return;
        }

        taiKhoan.setMatKhau(null);
        request.setAttribute("taiKhoan", taiKhoan);
        request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
    }

    private void insertTaiKhoan(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maNV = trimToNull(request.getParameter("maNV"));
        String tenDangNhap = trimToNull(request.getParameter("tenDangNhap"));
        String matKhau = trimToNull(request.getParameter("matKhau"));
        boolean deliveryMode = isDeliveryCreateMode(request);

        if (isBlank(maNV) || isBlank(tenDangNhap) || isBlank(matKhau)) {
            request.setAttribute("error", "Vui lòng chọn nhân viên, nhập tên đăng nhập và mật khẩu!");
            keepCreateFormValues(request, maNV, tenDangNhap, matKhau);
            prepareAddForm(request, deliveryMode);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
            return;
        }

        if (!nhanVienDAO.existsById(maNV)) {
            request.setAttribute("error", "Mã nhân viên chưa tồn tại. Hãy tạo nhân viên trước khi tạo tài khoản.");
            keepCreateFormValues(request, maNV, tenDangNhap, matKhau);
            prepareAddForm(request, deliveryMode);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
            return;
        }

        if (dao.existsById(maNV)) {
            request.setAttribute("error", "Nhân viên này đã có tài khoản!");
            keepCreateFormValues(request, maNV, tenDangNhap, matKhau);
            prepareAddForm(request, deliveryMode);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
            return;
        }

        if (dao.existsByUsername(tenDangNhap)) {
            request.setAttribute("error", "Tên đăng nhập đã tồn tại!");
            keepCreateFormValues(request, maNV, tenDangNhap, matKhau);
            prepareAddForm(request, deliveryMode);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
            return;
        }

        if (deliveryMode && !isDeliveryEmployee(maNV)) {
            request.setAttribute("error", "Chế độ này chỉ cho phép chọn nhân viên giao hàng.");
            keepCreateFormValues(request, maNV, tenDangNhap, matKhau);
            prepareAddForm(request, true);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
            return;
        }

        try {
            TaiKhoan tk = new TaiKhoan();
            tk.setMaNV(maNV);
            tk.setMaKH(null);
            tk.setTenDangNhap(tenDangNhap);
            tk.setMatKhau(PasswordUtils.hash(matKhau));
            tk.setVaiTro(resolveRoleFromNhanVien(maNV));
            tk.setTrangThai(true);

            dao.create(tk);
            response.sendRedirect(request.getContextPath() + "/taikhoan");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm tài khoản: " + e.getMessage());
            keepCreateFormValues(request, maNV, tenDangNhap, matKhau);
            prepareAddForm(request, deliveryMode);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
        }
    }

    private void updateTaiKhoan(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maNV = trimToNull(request.getParameter("maNV"));
        String tenDangNhap = trimToNull(request.getParameter("tenDangNhap"));
        String matKhau = trimToNull(request.getParameter("matKhau"));

        if (isBlank(maNV)) {
            response.sendRedirect(request.getContextPath() + "/taikhoan");
            return;
        }

        TaiKhoan tk = dao.findById(maNV);
        if (tk == null) {
            response.sendRedirect(request.getContextPath() + "/taikhoan");
            return;
        }

        if (isBlank(tenDangNhap) || isBlank(matKhau)) {
            request.setAttribute("error", "Tên đăng nhập và mật khẩu không được để trống!");
            tk.setMatKhau(null);
            request.setAttribute("taiKhoan", tk);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
            return;
        }

        TaiKhoan other = dao.findByUsername(tenDangNhap);
        if (other != null && !maNV.equalsIgnoreCase(other.getMaNV())) {
            request.setAttribute("error", "Tên đăng nhập đã tồn tại!");
            tk.setMatKhau(null);
            request.setAttribute("taiKhoan", tk);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
            return;
        }

        try {
            tk.setTenDangNhap(tenDangNhap);
            tk.setMatKhau(PasswordUtils.hash(matKhau));
            tk.setVaiTro(resolveRoleFromNhanVien(maNV));
            if (tk.getTrangThai() == null) {
                tk.setTrangThai(true);
            }

            dao.update(tk);
            response.sendRedirect(request.getContextPath() + "/taikhoan");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật tài khoản: " + e.getMessage());
            tk.setMatKhau(null);
            request.setAttribute("taiKhoan", tk);
            request.getRequestDispatcher("/views/taikhoan/form.jsp").forward(request, response);
        }
    }

    private void deleteTaiKhoan(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String maNV = request.getParameter("id");

        if (!isBlank(maNV)) {
            try {
                dao.delete(maNV.trim());
            } catch (RuntimeException e) {
                request.setAttribute("error", "Không thể xóa tài khoản: " + e.getMessage());
                listTaiKhoan(request, response);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/taikhoan");
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

    private void keepCreateFormValues(HttpServletRequest request, String maNV,
                                      String tenDangNhap, String matKhau) {
        request.setAttribute("formMaNV", maNV);
        request.setAttribute("formTenDangNhap", tenDangNhap);
        request.setAttribute("formMatKhau", matKhau);
    }

    private void prepareAddForm(HttpServletRequest request, boolean deliveryOnly) {
        request.setAttribute("createMode", deliveryOnly ? "delivery" : "general");
        if (deliveryOnly) {
            request.setAttribute("dsNhanVienChuaCoTaiKhoan", nhanVienDAO.findDeliveryEmployeesWithoutAccount());
            return;
        }

        request.setAttribute("dsNhanVienChuaCoTaiKhoan", nhanVienDAO.findEmployeesWithoutAccount());
    }

    private String resolveRoleFromNhanVien(String maNV) {
        NhanVien nhanVien = nhanVienDAO.findById(maNV);
        if (nhanVien == null || nhanVien.getChucVu() == null) {
            return "NhanVien";
        }

        String chucVu = nhanVien.getChucVu().trim();
        if (utils.RoleUtils.isManagerRole(chucVu)) {
            return "Admin";
        }

        if (utils.RoleUtils.isDeliveryRole(chucVu)) {
            return "GiaoHang";
        }

        return "NhanVien";
    }

    private boolean isDeliveryCreateMode(HttpServletRequest request) {
        String kind = trimToNull(request.getParameter("kind"));
        return kind != null && ("delivery".equalsIgnoreCase(kind) || "giaohang".equalsIgnoreCase(kind));
    }

    private boolean isDeliveryEmployee(String maNV) {
        NhanVien nhanVien = nhanVienDAO.findById(maNV);
        if (nhanVien == null || nhanVien.getChucVu() == null) {
            return false;
        }

        return utils.RoleUtils.isDeliveryRole(nhanVien.getChucVu());
    }
}
