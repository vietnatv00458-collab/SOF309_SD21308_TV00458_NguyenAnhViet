package servlet;

import dao.LichSuGiaSanPhamDAO;
import dao.SanPhamDAO;
import entity.LichSuGiaSanPham;
import entity.TaiKhoan;
import entity.SanPham;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import utils.RoleUtils;
import utils.UploadPathUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@WebServlet("/sanpham")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class SanPhamServlet extends HttpServlet {

    private final SanPhamDAO dao = new SanPhamDAO();
    private final LichSuGiaSanPhamDAO lichSuGiaSanPhamDAO = new LichSuGiaSanPhamDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listSanPham(request, response);
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
                deleteSanPham(request, response);
                break;
            default:
                listSanPham(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertSanPham(request, response);
        } else if ("update".equals(action)) {
            updateSanPham(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/sanpham");
        }
    }

    // =========================
    // DANH SÁCH
    // =========================
    private void listSanPham(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        List<SanPham> dsSanPham = dao.findByKeyword(keyword);

        request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
        request.setAttribute("dsSanPham", dsSanPham);
        request.setAttribute("canManageProducts", isProductManager(request));
        request.getRequestDispatcher("/views/sanpham/list.jsp").forward(request, response);
    }

    // =========================
    // FORM THÊM
    // =========================
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isProductManager(request)) {
            denyProductManagement(request, response);
            return;
        }

        request.setAttribute("formMaSP", dao.generateNextMaSP());
        request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
    }

    // =========================
    // FORM SỬA
    // =========================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isProductManager(request)) {
            denyProductManagement(request, response);
            return;
        }

        String maSP = request.getParameter("id");

        if (maSP == null || maSP.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/sanpham");
            return;
        }

        SanPham sanPham = dao.findById(maSP.trim());

        if (sanPham == null) {
            response.sendRedirect(request.getContextPath() + "/sanpham");
            return;
        }

        request.setAttribute("sanPham", sanPham);
        request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
    }

    // =========================
    // THÊM SẢN PHẨM
    // =========================
    private void insertSanPham(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isProductManager(request)) {
            denyProductManagement(request, response);
            return;
        }

        String maSP = trimToNull(request.getParameter("maSP"));
        String tenSP = trimToNull(request.getParameter("tenSP"));
        String loaiSP = trimToNull(request.getParameter("loaiSP"));
        String sizeSP = trimToNull(request.getParameter("sizeSP"));
        String donGiaStr = trimToNull(request.getParameter("donGia"));
        String soLuongTonStr = trimToNull(request.getParameter("soLuongTon"));
        String moTa = trimToNull(request.getParameter("moTa"));
        String thanhPhan = trimToNull(request.getParameter("thanhPhan"));

        if (isBlank(maSP) || dao.existsById(maSP)) {
            maSP = dao.generateNextMaSP();
        }

        keepFormValues(request, maSP, tenSP, loaiSP, sizeSP, donGiaStr, soLuongTonStr, moTa, thanhPhan);

        if (isBlank(tenSP)) {
            request.setAttribute("error", "Tên sản phẩm không được để trống!");
            request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
            return;
        }

        try {
            BigDecimal donGia = BigDecimal.ZERO;
            Integer soLuongTon = 0;

            if (donGiaStr != null && !donGiaStr.trim().isEmpty()) {
                donGia = new BigDecimal(donGiaStr.trim());
            }

            if (soLuongTonStr != null && !soLuongTonStr.trim().isEmpty()) {
                soLuongTon = Integer.parseInt(soLuongTonStr.trim());
            }

            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                request.setAttribute("error", "Đơn giá không được nhỏ hơn 0!");
                request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
                return;
            }

            if (soLuongTon < 0) {
                request.setAttribute("error", "Số lượng tồn không được nhỏ hơn 0!");
                request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
                return;
            }

            SanPham sp = new SanPham();
            sp.setMaSP(maSP);
            sp.setTenSP(tenSP);
            sp.setLoaiSP(loaiSP);
            sp.setSizeSP(sizeSP);
            sp.setDonGia(donGia);
            sp.setSoLuongTon(soLuongTon);
            sp.setMoTa(moTa);
            sp.setThanhPhan(thanhPhan);
            sp.setHinhAnh(saveUploadedImage(request, null));

            dao.create(sp);

            response.sendRedirect(request.getContextPath() + "/sanpham");

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Số lượng tồn phải là số nguyên!");
            request.setAttribute("sanPham", null);
            request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            request.setAttribute("sanPham", null);
            request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
        }
    }

    // =========================
    // CẬP NHẬT SẢN PHẨM
    // =========================
    private void updateSanPham(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isProductManager(request)) {
            denyProductManagement(request, response);
            return;
        }

        String maSP = trimToNull(request.getParameter("maSP"));
        String tenSP = trimToNull(request.getParameter("tenSP"));
        String loaiSP = trimToNull(request.getParameter("loaiSP"));
        String sizeSP = trimToNull(request.getParameter("sizeSP"));
        String donGiaStr = trimToNull(request.getParameter("donGia"));
        String soLuongTonStr = trimToNull(request.getParameter("soLuongTon"));
        String moTa = trimToNull(request.getParameter("moTa"));
        String thanhPhan = trimToNull(request.getParameter("thanhPhan"));

        keepFormValues(request, maSP, tenSP, loaiSP, sizeSP, donGiaStr, soLuongTonStr, moTa, thanhPhan);

        if (maSP == null || maSP.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/sanpham");
            return;
        }

        SanPham sp = dao.findById(maSP.trim());

        if (sp == null) {
            response.sendRedirect(request.getContextPath() + "/sanpham");
            return;
        }

        if (tenSP == null || tenSP.trim().isEmpty()) {
            request.setAttribute("error", "Tên sản phẩm không được để trống!");
            request.setAttribute("sanPham", sp);
            request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
            return;
        }

        try {
            BigDecimal giaCu = sp.getDonGia() != null ? sp.getDonGia() : BigDecimal.ZERO;
            BigDecimal donGia = BigDecimal.ZERO;
            Integer soLuongTon = 0;

            if (donGiaStr != null && !donGiaStr.trim().isEmpty()) {
                donGia = new BigDecimal(donGiaStr.trim());
            }

            if (soLuongTonStr != null && !soLuongTonStr.trim().isEmpty()) {
                soLuongTon = Integer.parseInt(soLuongTonStr.trim());
            }

            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                request.setAttribute("error", "Đơn giá không được nhỏ hơn 0!");
                request.setAttribute("sanPham", sp);
                request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
                return;
            }

            if (soLuongTon < 0) {
                request.setAttribute("error", "Số lượng tồn không được nhỏ hơn 0!");
                request.setAttribute("sanPham", sp);
                request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
                return;
            }

            sp.setTenSP(tenSP.trim());
            sp.setLoaiSP(loaiSP);
            sp.setSizeSP(sizeSP);
            sp.setDonGia(donGia);
            sp.setSoLuongTon(soLuongTon);
            sp.setMoTa(moTa);
            sp.setThanhPhan(thanhPhan);
            sp.setHinhAnh(saveUploadedImage(request, sp.getHinhAnh()));

            if (giaCu.compareTo(donGia) != 0) {
                LichSuGiaSanPham historyEntry = new LichSuGiaSanPham();
                historyEntry.setMaSP(sp.getMaSP());
                historyEntry.setGiaCu(giaCu);
                historyEntry.setGiaMoi(donGia);
                historyEntry.setNgayApDung(LocalDateTime.now());
                historyEntry.setLyDo("Cập nhật giá sản phẩm");

                dao.updateWithHistory(sp, historyEntry);
            } else {
                dao.update(sp);
            }

            response.sendRedirect(request.getContextPath() + "/sanpham");

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Số lượng tồn phải là số nguyên!");
            request.setAttribute("sanPham", sp);
            request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            request.setAttribute("sanPham", sp);
            request.getRequestDispatcher("/views/sanpham/form.jsp").forward(request, response);
        }
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

    private void keepFormValues(HttpServletRequest request, String maSP, String tenSP,
                                String loaiSP, String sizeSP, String donGiaStr,
                                String soLuongTonStr, String moTa, String thanhPhan) {
        request.setAttribute("formMaSP", maSP);
        request.setAttribute("formTenSP", tenSP);
        request.setAttribute("formLoaiSP", loaiSP);
        request.setAttribute("formSizeSP", sizeSP);
        request.setAttribute("formDonGia", donGiaStr);
        request.setAttribute("formSoLuongTon", soLuongTonStr);
        request.setAttribute("formMoTa", moTa);
        request.setAttribute("formThanhPhan", thanhPhan);
    }

    private boolean isProductManager(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object currentUser = session.getAttribute("user");
        if (!(currentUser instanceof TaiKhoan)) {
            return false;
        }

        return RoleUtils.isManagerRole(((TaiKhoan) currentUser).getVaiTro());
    }

    private void denyProductManagement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("error", "Bạn chỉ có quyền xem danh sách sản phẩm.");
        listSanPham(request, response);
    }

    // =========================
    // XÓA SẢN PHẨM
    // =========================
    private void deleteSanPham(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isProductManager(request)) {
            denyProductManagement(request, response);
            return;
        }

        String maSP = request.getParameter("id");

        if (maSP != null && !maSP.trim().isEmpty()) {
            String normalizedMaSP = maSP.trim();
            if (dao.hasUsageHistory(normalizedMaSP)) {
                request.setAttribute("error", "Không thể xóa sản phẩm đã có lịch sử bán, chi tiết đơn hàng hoặc lịch sử giá.");
                listSanPham(request, response);
                return;
            }

            try {
                dao.delete(normalizedMaSP);
            } catch (Exception e) {
                request.setAttribute("error", "Lỗi khi xóa sản phẩm: " + e.getMessage());
                listSanPham(request, response);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/sanpham");
    }

    private String saveUploadedImage(HttpServletRequest request, String currentImage)
            throws IOException, ServletException {

        Part imagePart = request.getPart("hinhAnhFile");
        if (imagePart == null || imagePart.getSize() <= 0) {
            return currentImage;
        }

        String submittedName = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString();
        String extension = "";
        int dotIndex = submittedName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = submittedName.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        if (!extension.matches("\\.(png|jpg|jpeg|webp|gif)")) {
            throw new ServletException("Ảnh sản phẩm chỉ hỗ trợ PNG, JPG, JPEG, WEBP hoặc GIF.");
        }

        File uploadDir = UploadPathUtil.productUploadDir();
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new ServletException("Không tạo được thư mục lưu ảnh.");
        }

        String fileName = "product-" + UUID.randomUUID() + extension;
        Files.copy(
                imagePart.getInputStream(),
                new File(uploadDir, fileName).toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );
        return "/uploads/" + fileName;
    }
}
