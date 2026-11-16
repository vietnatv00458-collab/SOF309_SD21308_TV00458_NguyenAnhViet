package servlet;

import dao.DatHangDAO;
import dao.DoanhThuDAO;
import dao.GioHangDAO;
import dao.HoaDonDAO;
import dao.LichSuGiaSanPhamDAO;
import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.SanPhamDAO;
import dao.TaiKhoanDAO;
import entity.DoanhThu;
import entity.LichSuGiaSanPham;
import entity.SanPham;
import entity.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import utils.BannerImageUtil;
import utils.RoleUtils;
import utils.UploadPathUtil;

@MultipartConfig(maxFileSize = 5L * 1024 * 1024, maxRequestSize = 25L * 1024 * 1024)
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        TaiKhoan user = getDashboardUser(request, response, session);
        if (user == null) {
            return;
        }

        populateBannerUploadStatus(request);

        KhachHangDAO khachHangDAO = new KhachHangDAO();
        NhanVienDAO nhanVienDAO = new NhanVienDAO();
        TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
        SanPhamDAO sanPhamDAO = new SanPhamDAO();
        LichSuGiaSanPhamDAO lichSuGiaSanPhamDAO = new LichSuGiaSanPhamDAO();
        HoaDonDAO hoaDonDAO = new HoaDonDAO();
        DatHangDAO datHangDAO = new DatHangDAO();
        GioHangDAO gioHangDAO = new GioHangDAO();
        DoanhThuDAO doanhThuDAO = new DoanhThuDAO();

        List<SanPham> dsSanPham = sanPhamDAO.findAll();
        List<DoanhThu> dsDoanhThu = doanhThuDAO.findAll();
        List<LichSuGiaSanPham> dsLichSuGia = lichSuGiaSanPhamDAO.findLatest(5);

        request.setAttribute("user", user);
        List<String> promoBannerImages = BannerImageUtil.loadPromoBannerImages();
        request.setAttribute("promoBannerImages", promoBannerImages);
        request.setAttribute("promoBannerCarouselImages", BannerImageUtil.buildPromoBannerCarouselImages(promoBannerImages));
        request.setAttribute("bannerEditableImages", buildEditableBannerNames(promoBannerImages));
        request.setAttribute("tongKhachHang", khachHangDAO.findAll().size());
        request.setAttribute("tongNhanVien", nhanVienDAO.findAll().size());
        request.setAttribute("tongTaiKhoan", taiKhoanDAO.findAll().size());
        request.setAttribute("tongSanPham", dsSanPham.size());
        request.setAttribute("tongLichSuGia", lichSuGiaSanPhamDAO.count());
        request.setAttribute("dsSanPhamDashboard", limitProducts(dsSanPham, 6));
        request.setAttribute("dsLichSuGiaDashboard", dsLichSuGia);
        request.setAttribute("tongHoaDon", hoaDonDAO.findAll().size());
        request.setAttribute("tongDatHang", datHangDAO.findAll().size());
        request.setAttribute("tongGioHang", gioHangDAO.findAll().size());
        request.setAttribute("tongDoanhThu", formatMoney(sumRevenue(dsDoanhThu)));
        request.setAttribute("doanhThuTheoThang", buildMonthlyRevenue(dsDoanhThu));

        request.getRequestDispatcher("/views/dashboard/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        TaiKhoan user = getDashboardUser(request, response, session);
        if (user == null) {
            return;
        }

        String replaceBannerTarget = trimToNull(request.getParameter("replaceBannerTarget"));
        int savedCount = saveBannerImage(request, replaceBannerTarget);
        if (savedCount > 0) {
            response.sendRedirect(request.getContextPath()
                    + "/dashboard?bannerUpload=success&bannerCount=" + savedCount
                    + "&bannerMode=" + (replaceBannerTarget != null ? "replace" : "add"));
            return;
        }

        response.sendRedirect(request.getContextPath() + "/dashboard?bannerUpload=empty");
    }

    private TaiKhoan getDashboardUser(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        TaiKhoan user = (TaiKhoan) session.getAttribute("user");
        if (!RoleUtils.isManagerRole(user != null ? user.getVaiTro() : null)) {
            response.sendRedirect(request.getContextPath() + RoleUtils.homePath(user != null ? user.getVaiTro() : null));
            return null;
        }

        return user;
    }

    private void populateBannerUploadStatus(HttpServletRequest request) {
        String status = trimToNull(request.getParameter("bannerUpload"));
        if (status == null) {
            return;
        }

        if ("success".equalsIgnoreCase(status)) {
            int count = 0;
            String countValue = trimToNull(request.getParameter("bannerCount"));
            if (countValue != null) {
                try {
                    count = Integer.parseInt(countValue);
                } catch (NumberFormatException ignored) {
                    count = 0;
                }
            }

            String mode = trimToNull(request.getParameter("bannerMode"));
            boolean replaceMode = "replace".equalsIgnoreCase(mode);
            request.setAttribute("bannerUploadMessage", count > 0
                    ? (replaceMode ? "Đã thay " : "Đã thêm ") + count + " ảnh banner."
                    : (replaceMode ? "Đã thay ảnh banner." : "Đã thêm ảnh banner."));
            return;
        }

        if ("empty".equalsIgnoreCase(status)) {
            request.setAttribute("bannerUploadError", "Chưa chọn ảnh hợp lệ.");
            return;
        }

        request.setAttribute("bannerUploadError", "Không thể lưu ảnh banner.");
    }

    private List<String> buildEditableBannerNames(List<String> bannerImages) {
        List<String> editable = new ArrayList<>();
        if (bannerImages == null) {
            return editable;
        }

        for (String image : bannerImages) {
            if (image == null) {
                continue;
            }

            String trimmed = image.trim();
            int queryIndex = trimmed.indexOf('?');
            if (queryIndex >= 0) {
                trimmed = trimmed.substring(0, queryIndex);
            }
            if (trimmed.startsWith("/promo-banners/")) {
                editable.add(new File(trimmed).getName());
            }
        }
        return editable;
    }

    private int saveBannerImage(HttpServletRequest request, String replaceBannerTarget) throws IOException, ServletException {
        File uploadDir = UploadPathUtil.bannerUploadDir();
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new ServletException("Không tạo được thư mục lưu ảnh banner.");
        }

        Part imagePart = null;
        for (Part part : request.getParts()) {
            if (part == null || part.getSize() <= 0 || !"bannerImage".equals(part.getName())) {
                continue;
            }

            String submittedName = part.getSubmittedFileName();
            if (submittedName == null || submittedName.trim().isEmpty()) {
                continue;
            }

            String originalName = Paths.get(submittedName).getFileName().toString();
            if (!BannerImageUtil.isSupportedImageName(originalName)) {
                continue;
            }

            imagePart = part;
            break;
        }

        if (imagePart == null) {
            return 0;
        }

        String newSubmittedName = imagePart.getSubmittedFileName();
        String newOriginalName = Paths.get(newSubmittedName).getFileName().toString();
        String extension = newOriginalName.substring(newOriginalName.lastIndexOf('.')).toLowerCase(Locale.ROOT);

        if (replaceBannerTarget != null) {
            String targetFileName = Paths.get(replaceBannerTarget).getFileName().toString();
            if (!BannerImageUtil.isSupportedImageName(targetFileName)) {
                throw new ServletException("Ảnh banner được chọn không hợp lệ.");
            }

            File targetFile = new File(uploadDir, targetFileName);
            if (!targetFile.isFile()) {
                throw new ServletException("Ảnh banner được chọn không tồn tại.");
            }

            try (InputStream inputStream = imagePart.getInputStream()) {
                Files.copy(
                        inputStream,
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
            return 1;
        }

        String fileName = "banner-" + UUID.randomUUID() + extension;
        try (InputStream inputStream = imagePart.getInputStream()) {
            Files.copy(
                    inputStream,
                    new File(uploadDir, fileName).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return 1;
    }

    private List<SanPham> limitProducts(List<SanPham> products, int limit) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }

        if (products.size() <= limit) {
            return products;
        }

        return new ArrayList<>(products.subList(0, limit));
    }

    private List<MonthlyRevenueRow> buildMonthlyRevenue(List<DoanhThu> dsDoanhThu) {
        Map<YearMonth, BigDecimal> tongTheoThang = new TreeMap<>(Comparator.reverseOrder());
        if (dsDoanhThu != null) {
            for (DoanhThu doanhThu : dsDoanhThu) {
                if (doanhThu == null || doanhThu.getNgay() == null || doanhThu.getDoanhThu() == null) {
                    continue;
                }
                YearMonth month = YearMonth.from(doanhThu.getNgay());
                tongTheoThang.merge(month, doanhThu.getDoanhThu(), BigDecimal::add);
            }
        }

        List<MonthlyRevenueRow> result = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal> entry : tongTheoThang.entrySet()) {
            result.add(new MonthlyRevenueRow(formatMonth(entry.getKey()), formatMoney(entry.getValue())));
        }
        return result;
    }

    private BigDecimal sumRevenue(List<DoanhThu> dsDoanhThu) {
        BigDecimal total = BigDecimal.ZERO;
        if (dsDoanhThu == null) {
            return total;
        }

        for (DoanhThu doanhThu : dsDoanhThu) {
            if (doanhThu != null && doanhThu.getDoanhThu() != null) {
                total = total.add(doanhThu.getDoanhThu());
            }
        }
        return total;
    }

    private String formatMonth(YearMonth month) {
        return month != null ? month.format(MONTH_FORMATTER) : "";
    }

    private String formatMoney(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount != null ? amount : BigDecimal.ZERO) + " đ";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static final class MonthlyRevenueRow {
        private final String monthLabel;
        private final String revenueText;

        public MonthlyRevenueRow(String monthLabel, String revenueText) {
            this.monthLabel = monthLabel;
            this.revenueText = revenueText;
        }

        public String getMonthLabel() {
            return monthLabel;
        }

        public String getRevenueText() {
            return revenueText;
        }
    }
}
