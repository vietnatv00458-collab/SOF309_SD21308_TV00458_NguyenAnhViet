package servlet;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.SecureRandom;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@WebServlet("/khuyenmai")
public class KhuyenMaiServlet extends HttpServlet {

    private static final int VOUCHER_CODE_LENGTH = 6;
    private static final String VOUCHER_CODE_PREFIX = "VC";
    private static final char[] VOUCHER_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final String DISCOUNT_TYPE_PERCENT = "PERCENT";
    private static final String DISCOUNT_TYPE_AMOUNT = "AMOUNT";

    private final KhuyenMaiDAO dao = new KhuyenMaiDAO();
    private final SecureRandom secureRandom = new SecureRandom();

    private boolean canViewPromotions(String role) {
        return isManager(role) || isEmployee(role);
    }

    private boolean canManagePromotions(String role) {
        return isManager(role);
    }

    private boolean isManager(String role) {
        return utils.RoleUtils.isManagerRole(role);
    }

    private boolean isEmployee(String role) {
        return utils.RoleUtils.isEmployeeRole(role);
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String generateVoucherCode() {
        for (int attempt = 0; attempt < 100; attempt++) {
            String code = buildVoucherCode();
            if (!dao.existsById(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Không thể tạo mã voucher duy nhất. Vui lòng thử lại.");
    }

    private String buildVoucherCode() {
        StringBuilder builder = new StringBuilder(VOUCHER_CODE_LENGTH);
        builder.append(VOUCHER_CODE_PREFIX);

        int remaining = VOUCHER_CODE_LENGTH - VOUCHER_CODE_PREFIX.length();
        for (int i = 0; i < remaining; i++) {
            builder.append(VOUCHER_CODE_CHARS[secureRandom.nextInt(VOUCHER_CODE_CHARS.length)]);
        }

        return builder.toString();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        String role = getCurrentRole(request);

        if (action == null || action.trim().isEmpty()) {
            if (!canViewPromotions(role)) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            listKhuyenMai(request, response);
            return;
        }

        switch (action) {
            case "add":
                if (!canManagePromotions(role)) {
                    response.sendRedirect(request.getContextPath() + "/khuyenmai");
                    return;
                }
                showAddForm(request, response);
                break;
            case "edit":
                if (!canManagePromotions(role)) {
                    response.sendRedirect(request.getContextPath() + "/khuyenmai");
                    return;
                }
                showEditForm(request, response);
                break;
            case "delete":
                if (!canManagePromotions(role)) {
                    response.sendRedirect(request.getContextPath() + "/khuyenmai");
                    return;
                }
                deleteKhuyenMai(request, response);
                break;
            default:
                listKhuyenMai(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String role = getCurrentRole(request);

        if ("create".equals(action)) {
            if (!canManagePromotions(role)) {
                response.sendRedirect(request.getContextPath() + "/khuyenmai");
                return;
            }
            insertKhuyenMai(request, response);
        } else if ("update".equals(action)) {
            if (!canManagePromotions(role)) {
                response.sendRedirect(request.getContextPath() + "/khuyenmai");
                return;
            }
            updateKhuyenMai(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/khuyenmai");
        }
    }

    // =========================
    // DANH SÁCH
    // =========================
    private void listKhuyenMai(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<KhuyenMai> dsKhuyenMai = dao.findAll();
        request.setAttribute("dsKhuyenMai", dsKhuyenMai);
        request.getRequestDispatcher("/views/khuyenmai/list.jsp").forward(request, response);
    }

    // =========================
    // FORM THÊM
    // =========================
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("formLoaiGiamGia", DISCOUNT_TYPE_PERCENT);
        request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
    }

    // =========================
    // FORM SỬA
    // =========================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maKM = request.getParameter("id");

        if (maKM == null || maKM.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/khuyenmai");
            return;
        }

        KhuyenMai km = dao.findById(normalizeCode(maKM));

        if (km == null) {
            response.sendRedirect(request.getContextPath() + "/khuyenmai");
            return;
        }

        request.setAttribute("khuyenMai", km);
        request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
    }

    // =========================
    // THÊM
    // =========================
    private void insertKhuyenMai(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String tenKM = request.getParameter("tenKM");
        String loaiGiamGia = normalizeDiscountType(request.getParameter("loaiGiamGia"));
        String phanTramGiamStr = request.getParameter("phanTramGiam");
        String soTienGiamStr = request.getParameter("soTienGiam");
        String mucGiamToiDaStr = request.getParameter("mucGiamToiDa");
        String ngayBatDauStr = request.getParameter("ngayBatDau");
        String ngayKetThucStr = request.getParameter("ngayKetThuc");
        String moTa = request.getParameter("moTa");

        if (tenKM == null || tenKM.trim().isEmpty()) {
            request.setAttribute("error", "Tên khuyến mãi không được để trống!");
            keepFormValues(request, tenKM, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr,
                    ngayBatDauStr, ngayKetThucStr, moTa);
            request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
            return;
        }

        try {
            KhuyenMai km = new KhuyenMai();
            km.setMaKM(generateVoucherCode());
            km.setTenKM(tenKM.trim());
            applyDiscountFields(km, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr);
            applyDateFields(km, ngayBatDauStr, ngayKetThucStr, moTa);

            dao.create(km);

            response.sendRedirect(request.getContextPath() + "/khuyenmai");

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Giá trị giảm không hợp lệ!");
            keepFormValues(request, tenKM, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr,
                    ngayBatDauStr, ngayKetThucStr, moTa);
            request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            keepFormValues(request, tenKM, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr,
                    ngayBatDauStr, ngayKetThucStr, moTa);
            request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm khuyến mãi: " + e.getMessage());
            keepFormValues(request, tenKM, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr,
                    ngayBatDauStr, ngayKetThucStr, moTa);
            request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
        }
    }

    // =========================
    // CẬP NHẬT
    // =========================
    private void updateKhuyenMai(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maKM = request.getParameter("maKM");
        String tenKM = request.getParameter("tenKM");
        String loaiGiamGia = normalizeDiscountType(request.getParameter("loaiGiamGia"));
        String phanTramGiamStr = request.getParameter("phanTramGiam");
        String soTienGiamStr = request.getParameter("soTienGiam");
        String mucGiamToiDaStr = request.getParameter("mucGiamToiDa");
        String ngayBatDauStr = request.getParameter("ngayBatDau");
        String ngayKetThucStr = request.getParameter("ngayKetThuc");
        String moTa = request.getParameter("moTa");

        if (maKM == null || maKM.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/khuyenmai");
            return;
        }

        String normalizedMaKM = normalizeCode(maKM);
        KhuyenMai km = dao.findById(normalizedMaKM);

        if (km == null) {
            response.sendRedirect(request.getContextPath() + "/khuyenmai");
            return;
        }

        if (tenKM == null || tenKM.trim().isEmpty()) {
            request.setAttribute("error", "Tên khuyến mãi không được để trống!");
            request.setAttribute("khuyenMai", km);
            keepFormValues(request, tenKM, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr,
                    ngayBatDauStr, ngayKetThucStr, moTa);
            request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
            return;
        }

        try {
            km.setTenKM(tenKM.trim());
            applyDiscountFields(km, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr);
            applyDateFields(km, ngayBatDauStr, ngayKetThucStr, moTa);

            dao.update(km);

            response.sendRedirect(request.getContextPath() + "/khuyenmai");

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Giá trị giảm không hợp lệ!");
            request.setAttribute("khuyenMai", km);
            keepFormValues(request, tenKM, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr,
                    ngayBatDauStr, ngayKetThucStr, moTa);
            request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("khuyenMai", km);
            keepFormValues(request, tenKM, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr,
                    ngayBatDauStr, ngayKetThucStr, moTa);
            request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật khuyến mãi: " + e.getMessage());
            request.setAttribute("khuyenMai", km);
            keepFormValues(request, tenKM, loaiGiamGia, phanTramGiamStr, soTienGiamStr, mucGiamToiDaStr,
                    ngayBatDauStr, ngayKetThucStr, moTa);
            request.getRequestDispatcher("/views/khuyenmai/form.jsp").forward(request, response);
        }
    }

    // =========================
    // XÓA
    // =========================
    private void deleteKhuyenMai(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String maKM = request.getParameter("id");

        if (maKM != null && !maKM.trim().isEmpty()) {
            try {
                dao.delete(maKM.trim());
            } catch (RuntimeException e) {
                request.setAttribute("error", "Không thể xóa khuyến mãi: " + e.getMessage());
                listKhuyenMai(request, response);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/khuyenmai");
    }

    private void applyDiscountFields(KhuyenMai km, String loaiGiamGia, String phanTramGiamStr,
                                     String soTienGiamStr, String mucGiamToiDaStr) {
        String type = normalizeDiscountType(loaiGiamGia);
        if (type == null) {
            throw new IllegalArgumentException("Vui lòng chọn kiểu giảm giá.");
        }

        km.setLoaiGiamGia(type);

        if (DISCOUNT_TYPE_AMOUNT.equals(type)) {
            BigDecimal soTienGiam = parseMoney(soTienGiamStr);
            if (soTienGiam.signum() <= 0) {
                throw new IllegalArgumentException("Số tiền giảm phải lớn hơn 0.");
            }
            km.setSoTienGiam(soTienGiam);
            km.setPhanTramGiam(null);
            km.setMucGiamToiDa(null);
            return;
        }

        Double phanTramGiam = parseDoubleOrNull(phanTramGiamStr);
        if (phanTramGiam == null || phanTramGiam <= 0 || phanTramGiam >= 100) {
            throw new IllegalArgumentException("Phần trăm giảm phải lớn hơn 0 và nhỏ hơn 100.");
        }

        km.setPhanTramGiam(phanTramGiam);
        km.setSoTienGiam(null);
        km.setMucGiamToiDa(parseMoneyOrNull(mucGiamToiDaStr));
    }

    private void applyDateFields(KhuyenMai km, String ngayBatDauStr, String ngayKetThucStr, String moTa) {
        LocalDate ngayBatDau = parseDateOrNull(ngayBatDauStr);
        LocalDate ngayKetThuc = parseDateOrNull(ngayKetThucStr);

        if (ngayBatDau != null && ngayKetThuc != null && ngayKetThuc.isBefore(ngayBatDau)) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
        }

        km.setNgayBatDau(ngayBatDau);
        km.setNgayKetThuc(ngayKetThuc);
        km.setMoTa(trimToNull(moTa));
    }

    private void keepFormValues(HttpServletRequest request, String tenKM, String loaiGiamGia,
                                String phanTramGiamStr, String soTienGiamStr, String mucGiamToiDaStr,
                                String ngayBatDauStr, String ngayKetThucStr, String moTa) {
        request.setAttribute("formTenKM", tenKM);
        request.setAttribute("formLoaiGiamGia", normalizeDiscountType(loaiGiamGia));
        request.setAttribute("formPhanTramGiam", phanTramGiamStr);
        request.setAttribute("formSoTienGiam", soTienGiamStr);
        request.setAttribute("formMucGiamToiDa", mucGiamToiDaStr);
        request.setAttribute("formNgayBatDau", ngayBatDauStr);
        request.setAttribute("formNgayKetThuc", ngayKetThucStr);
        request.setAttribute("formMoTa", moTa);
    }

    private String normalizeDiscountType(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replace(" ", "").replace("-", "").replace("_", "").toUpperCase(Locale.ROOT);
        if ("PERCENT".equals(normalized) || "PHANTRAM".equals(normalized)) {
            return DISCOUNT_TYPE_PERCENT;
        }

        if ("AMOUNT".equals(normalized) || "SOTIEN".equals(normalized) || "MONEY".equals(normalized)) {
            return DISCOUNT_TYPE_AMOUNT;
        }

        return null;
    }

    private Double parseDoubleOrNull(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : Double.parseDouble(trimmed);
    }

    private BigDecimal parseMoney(String value) {
        BigDecimal money = parseMoneyOrNull(value);
        return money != null ? money : BigDecimal.ZERO;
    }

    private BigDecimal parseMoneyOrNull(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return new BigDecimal(trimmed);
    }

    private LocalDate parseDateOrNull(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : LocalDate.parse(trimmed);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getCurrentRole(HttpServletRequest request) {
        if (request == null || request.getSession(false) == null) {
            return null;
        }

        Object user = request.getSession(false).getAttribute("user");
        if (user instanceof entity.TaiKhoan) {
            return ((entity.TaiKhoan) user).getVaiTro();
        }

        return null;
    }
}
