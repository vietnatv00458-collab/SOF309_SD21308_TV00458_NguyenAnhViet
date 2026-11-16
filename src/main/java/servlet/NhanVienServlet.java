package servlet;

import dao.NhanVienDAO;
import entity.NhanVien;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.NhanVienService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/nhanvien")
public class NhanVienServlet extends HttpServlet {

    private final NhanVienDAO dao = new NhanVienDAO();
    private final NhanVienService service = new NhanVienService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listNhanVien(request, response);
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
                deleteNhanVien(request, response);
                break;
            default:
                listNhanVien(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertNhanVien(request, response);
        } else if ("update".equals(action)) {
            updateNhanVien(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/nhanvien");
        }
    }

    // =========================
    // DANH SÁCH
    // =========================
    private void listNhanVien(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<NhanVien> dsNhanVien = dao.findAll();
        request.setAttribute("dsNhanVien", dsNhanVien);
        request.setAttribute("deleteImpactMap", buildDeleteImpactMap(dsNhanVien));
        request.getRequestDispatcher("/views/nhanvien/list.jsp").forward(request, response);
    }

    // =========================
    // FORM THÊM
    // =========================
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("formMaNV", dao.generateNextMaNV());
        prepareAgeConstraint(request);
        request.getRequestDispatcher("/views/nhanvien/form.jsp").forward(request, response);
    }

    // =========================
    // FORM SỬA
    // =========================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maNV = request.getParameter("id");

        if (maNV == null || maNV.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/nhanvien");
            return;
        }

        NhanVien nhanVien = dao.findActiveById(maNV.trim());

        if (nhanVien == null) {
            response.sendRedirect(request.getContextPath() + "/nhanvien");
            return;
        }

        request.setAttribute("nhanVien", nhanVien);
        prepareAgeConstraint(request);
        request.getRequestDispatcher("/views/nhanvien/form.jsp").forward(request, response);
    }

    // =========================
    // THÊM NHÂN VIÊN
    // =========================
    private void insertNhanVien(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maNV = trimToNull(request.getParameter("maNV"));
        String hoTen = trimToNull(request.getParameter("hoTen"));
        String ngaySinh = trimToNull(request.getParameter("ngaySinh"));
        String ngayVaoLam = trimToNull(request.getParameter("ngayVaoLam"));
        String gioiTinh = trimToNull(request.getParameter("gioiTinh"));
        String sdt = trimToNull(request.getParameter("sdt"));
        String email = trimToNull(request.getParameter("email"));
        String diaChi = trimToNull(request.getParameter("diaChi"));
        String chucVu = trimToNull(request.getParameter("chucVu"));
        String luongStr = trimToNull(request.getParameter("luong"));

        if (isBlank(maNV) || dao.existsById(maNV)) {
            maNV = dao.generateNextMaNV();
        }

        keepFormValues(request, maNV, hoTen, ngaySinh, ngayVaoLam, gioiTinh, sdt, email, diaChi, chucVu, luongStr);

        if (isBlank(hoTen)) {

            request.setAttribute("error", "Họ tên không được để trống!");
            forwardForm(request, response);
            return;
        }

        try {
            BigDecimal luong = parseBigDecimalOrZero(luongStr);
            LocalDate ngaySinhValue = parseLocalDateOrNull(ngaySinh);
            LocalDate ngayVaoLamValue = parseLocalDateOrNull(ngayVaoLam);

            if (ngaySinhValue == null) {
                request.setAttribute("error", "Ngày sinh không được để trống!");
                forwardForm(request, response);
                return;
            }

            if (!isAtLeast18(ngaySinhValue)) {
                request.setAttribute("error", "Nhân viên phải đủ 18 tuổi trở lên!");
                forwardForm(request, response);
                return;
            }

            if (ngayVaoLamValue == null) {
                ngayVaoLamValue = LocalDate.now();
            }

            NhanVien nv = new NhanVien();
            nv.setMaNV(maNV);
            nv.setHoTen(hoTen);
            nv.setNgaySinh(ngaySinhValue);
            nv.setNgayVaoLam(ngayVaoLamValue);
            nv.setGioiTinh(gioiTinh);
            nv.setSdt(sdt);
            nv.setEmail(email);
            nv.setDiaChi(diaChi);
            nv.setChucVu(chucVu);
            nv.setLuong(luong);

            dao.create(nv);
            response.sendRedirect(request.getContextPath() + "/nhanvien");

        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Ngày sinh hoặc ngày vào làm không hợp lệ!");
            forwardForm(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Lương phải là số!");
            forwardForm(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm nhân viên: " + e.getMessage());
            forwardForm(request, response);
        }
    }

    // =========================
    // CẬP NHẬT NHÂN VIÊN
    // =========================
    private void updateNhanVien(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maNV = trimToNull(request.getParameter("maNV"));
        String hoTen = trimToNull(request.getParameter("hoTen"));
        String ngaySinh = trimToNull(request.getParameter("ngaySinh"));
        String ngayVaoLam = trimToNull(request.getParameter("ngayVaoLam"));
        String gioiTinh = trimToNull(request.getParameter("gioiTinh"));
        String sdt = trimToNull(request.getParameter("sdt"));
        String email = trimToNull(request.getParameter("email"));
        String diaChi = trimToNull(request.getParameter("diaChi"));
        String chucVu = trimToNull(request.getParameter("chucVu"));
        String luongStr = trimToNull(request.getParameter("luong"));

        keepFormValues(request, maNV, hoTen, ngaySinh, ngayVaoLam, gioiTinh, sdt, email, diaChi, chucVu, luongStr);

        if (isBlank(maNV)) {
            response.sendRedirect(request.getContextPath() + "/nhanvien");
            return;
        }

        NhanVien nv = dao.findActiveById(maNV);

        if (nv == null) {
            response.sendRedirect(request.getContextPath() + "/nhanvien");
            return;
        }

        if (isBlank(hoTen)) {
            request.setAttribute("error", "Họ tên không được để trống!");
            request.setAttribute("nhanVien", nv);
            forwardForm(request, response);
            return;
        }

        try {
            BigDecimal luong = parseBigDecimalOrZero(luongStr);
            LocalDate ngaySinhValue = parseLocalDateOrNull(ngaySinh);
            LocalDate ngayVaoLamValue = parseLocalDateOrNull(ngayVaoLam);

            if (ngaySinhValue == null) {
                request.setAttribute("error", "Ngày sinh không được để trống!");
                request.setAttribute("nhanVien", nv);
                forwardForm(request, response);
                return;
            }

            if (!isAtLeast18(ngaySinhValue)) {
                request.setAttribute("error", "Nhân viên phải đủ 18 tuổi trở lên!");
                request.setAttribute("nhanVien", nv);
                forwardForm(request, response);
                return;
            }

            if (ngayVaoLamValue == null) {
                ngayVaoLamValue = nv.getNgayVaoLam();
            }

            nv.setHoTen(hoTen.trim());
            nv.setNgaySinh(ngaySinhValue);
            nv.setNgayVaoLam(ngayVaoLamValue);
            nv.setGioiTinh(gioiTinh);
            nv.setSdt(sdt);
            nv.setEmail(email);
            nv.setDiaChi(diaChi);
            nv.setChucVu(chucVu);
            nv.setLuong(luong);

            dao.update(nv);
            response.sendRedirect(request.getContextPath() + "/nhanvien");

        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Ngày sinh hoặc ngày vào làm không hợp lệ!");
            request.setAttribute("nhanVien", nv);
            forwardForm(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Lương phải là số!");
            request.setAttribute("nhanVien", nv);
            forwardForm(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật nhân viên: " + e.getMessage());
            request.setAttribute("nhanVien", nv);
            forwardForm(request, response);
        }
    }

    private BigDecimal parseBigDecimalOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private LocalDate parseLocalDateOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(value.trim());
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

    private boolean isAtLeast18(LocalDate ngaySinh) {
        return !ngaySinh.isAfter(LocalDate.now().minusYears(18));
    }

    private void prepareAgeConstraint(HttpServletRequest request) {
        request.setAttribute("ngaySinhMax", LocalDate.now().minusYears(18));
    }

    private void forwardForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepareAgeConstraint(request);
        request.getRequestDispatcher("/views/nhanvien/form.jsp").forward(request, response);
    }

    private void keepFormValues(HttpServletRequest request, String maNV, String hoTen,
                                String ngaySinh, String ngayVaoLam, String gioiTinh,
                                String sdt, String email, String diaChi,
                                String chucVu, String luong) {
        request.setAttribute("formMaNV", emptyIfNull(maNV));
        request.setAttribute("formHoTen", emptyIfNull(hoTen));
        request.setAttribute("formNgaySinh", emptyIfNull(ngaySinh));
        request.setAttribute("formNgayVaoLam", emptyIfNull(ngayVaoLam));
        request.setAttribute("formGioiTinh", emptyIfNull(gioiTinh));
        request.setAttribute("formSdt", emptyIfNull(sdt));
        request.setAttribute("formEmail", emptyIfNull(email));
        request.setAttribute("formDiaChi", emptyIfNull(diaChi));
        request.setAttribute("formChucVu", emptyIfNull(chucVu));
        request.setAttribute("formLuong", emptyIfNull(luong));
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    // =========================
    // XÓA NHÂN VIÊN
    // =========================
    private void deleteNhanVien(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String maNV = request.getParameter("id");

        if (maNV != null && !maNV.trim().isEmpty()) {
            HttpSession session = request.getSession();
            try {
                NhanVienService.DeletionPreview preview = service.previewDeletion(maNV.trim());
                NhanVienService.DeletionResult result = service.deleteEmployee(maNV.trim());
                if (result != null && result.isSuccess()) {
                    session.setAttribute("success", buildDeleteSuccessMessage(result, preview));
                } else {
                    session.setAttribute("error", "Không tìm thấy nhân viên để xóa.");
                }
            } catch (RuntimeException e) {
                session.setAttribute("error", "Không thể xóa nhân viên: " + e.getMessage());
            }
        }

        response.sendRedirect(request.getContextPath() + "/nhanvien");
    }

    private Map<String, Long> buildDeleteImpactMap(List<NhanVien> dsNhanVien) {
        Map<String, Long> impactMap = new HashMap<>();
        if (dsNhanVien == null) {
            return impactMap;
        }

        for (NhanVien nv : dsNhanVien) {
            if (nv == null || nv.getMaNV() == null) {
                continue;
            }
            NhanVienService.DeletionPreview preview = service.previewDeletion(nv.getMaNV());
            impactMap.put(nv.getMaNV(), preview.getPendingOrderCount());
        }
        return impactMap;
    }

    private String buildDeleteSuccessMessage(NhanVienService.DeletionResult result, NhanVienService.DeletionPreview preview) {
        long completed = preview != null ? preview.getCompletedOrderCount() : 0L;
        return "Đã xóa nhân viên " + result.getMaNV()
                + ". Đã xóa " + result.getDeletedOrderCount() + " đơn chưa hoàn thành"
                + ", giữ lại " + completed + " đơn đã hoàn thành.";
    }
}
