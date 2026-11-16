package servlet;

import dao.LichSuGiaSanPhamDAO;
import dao.SanPhamDAO;
import entity.LichSuGiaSanPham;
import entity.SanPham;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/lichsugiasanpham")
public class LichSuGiaSanPhamServlet extends HttpServlet {

    private final LichSuGiaSanPhamDAO dao = new LichSuGiaSanPhamDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listLichSuGia(request, response);
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
                deleteLichSuGia(request, response);
                break;
            default:
                listLichSuGia(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertLichSuGia(request, response);
        } else if ("update".equals(action)) {
            updateLichSuGia(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/lichsugiasanpham");
        }
    }

    private void listLichSuGia(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        List<LichSuGiaSanPham> dsLichSuGia = (keyword == null || keyword.trim().isEmpty())
                ? dao.findAll()
                : dao.findByKeyword(keyword);

        request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
        request.setAttribute("dsLichSuGia", dsLichSuGia);
        request.getRequestDispatcher("/views/lichsugiasanpham/list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LichSuGiaSanPham lichSuGiaSanPham = new LichSuGiaSanPham();
        lichSuGiaSanPham.setNgayApDung(LocalDateTime.now());
        request.setAttribute("lichSuGiaSanPham", lichSuGiaSanPham);
        request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer maLSGia = parseInteger(request.getParameter("id"));
        if (maLSGia == null) {
            response.sendRedirect(request.getContextPath() + "/lichsugiasanpham");
            return;
        }

        LichSuGiaSanPham lichSuGiaSanPham = dao.findById(maLSGia);
        if (lichSuGiaSanPham == null) {
            response.sendRedirect(request.getContextPath() + "/lichsugiasanpham");
            return;
        }

        request.setAttribute("lichSuGiaSanPham", lichSuGiaSanPham);
        request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
    }

    private void insertLichSuGia(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            LichSuGiaSanPham lichSuGiaSanPham = buildFromRequest(request, null);
            validateHistory(lichSuGiaSanPham, null);

            dao.create(lichSuGiaSanPham);
            response.sendRedirect(request.getContextPath() + "/lichsugiasanpham");
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Giá cũ và giá mới phải là số hợp lệ.");
            request.setAttribute("lichSuGiaSanPham", buildFormObject(request, null));
            request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("lichSuGiaSanPham", buildFormObject(request, null));
            request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Ngày áp dụng không đúng định dạng.");
            request.setAttribute("lichSuGiaSanPham", buildFormObject(request, null));
            request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm lịch sử giá: " + e.getMessage());
            request.setAttribute("lichSuGiaSanPham", buildFormObject(request, null));
            request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
        }
    }

    private void updateLichSuGia(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer maLSGia = parseInteger(request.getParameter("maLSGia"));
        if (maLSGia == null) {
            response.sendRedirect(request.getContextPath() + "/lichsugiasanpham");
            return;
        }

        LichSuGiaSanPham existing = dao.findById(maLSGia);
        if (existing == null) {
            response.sendRedirect(request.getContextPath() + "/lichsugiasanpham");
            return;
        }

        try {
            LichSuGiaSanPham lichSuGiaSanPham = buildFromRequest(request, existing);
            validateHistory(lichSuGiaSanPham, existing);

            dao.update(lichSuGiaSanPham);
            response.sendRedirect(request.getContextPath() + "/lichsugiasanpham");
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Giá cũ và giá mới phải là số hợp lệ.");
            request.setAttribute("lichSuGiaSanPham", buildFormObject(request, existing));
            request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("lichSuGiaSanPham", buildFormObject(request, existing));
            request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Ngày áp dụng không đúng định dạng.");
            request.setAttribute("lichSuGiaSanPham", buildFormObject(request, existing));
            request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật lịch sử giá: " + e.getMessage());
            request.setAttribute("lichSuGiaSanPham", buildFormObject(request, existing));
            request.getRequestDispatcher("/views/lichsugiasanpham/form.jsp").forward(request, response);
        }
    }

    private void deleteLichSuGia(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer maLSGia = parseInteger(request.getParameter("id"));
        if (maLSGia != null) {
            try {
                dao.delete(maLSGia);
            } catch (Exception e) {
                request.setAttribute("error", "Lỗi khi xóa lịch sử giá: " + e.getMessage());
                listLichSuGia(request, response);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/lichsugiasanpham");
    }

    private LichSuGiaSanPham buildFromRequest(HttpServletRequest request, LichSuGiaSanPham base)
            throws DateTimeParseException {

        LichSuGiaSanPham lichSuGiaSanPham = base != null ? base : new LichSuGiaSanPham();
        Integer maLSGia = parseInteger(request.getParameter("maLSGia"));
        if (maLSGia != null) {
            lichSuGiaSanPham.setMaLSGia(maLSGia);
        }

        String maSP = normalize(request.getParameter("maSP"));
        BigDecimal giaCu = parseMoneyOrZero(request.getParameter("giaCu"));
        BigDecimal giaMoi = parseMoneyOrZero(request.getParameter("giaMoi"));
        String ngayApDungStr = request.getParameter("ngayApDung");
        String lyDo = normalize(request.getParameter("lyDo"));

        lichSuGiaSanPham.setMaSP(maSP);
        lichSuGiaSanPham.setGiaCu(giaCu);
        lichSuGiaSanPham.setGiaMoi(giaMoi);
        lichSuGiaSanPham.setNgayApDung(parseDateTimeOrNow(ngayApDungStr));
        lichSuGiaSanPham.setLyDo(lyDo);
        return lichSuGiaSanPham;
    }

    private LichSuGiaSanPham buildFormObject(HttpServletRequest request, LichSuGiaSanPham fallback) {
        LichSuGiaSanPham lichSuGiaSanPham = fallback != null ? fallback : new LichSuGiaSanPham();
        Integer maLSGia = parseInteger(request.getParameter("maLSGia"));
        if (maLSGia != null) {
            lichSuGiaSanPham.setMaLSGia(maLSGia);
        }
        lichSuGiaSanPham.setMaSP(normalize(request.getParameter("maSP")));
        lichSuGiaSanPham.setGiaCu(parseMoneySafely(request.getParameter("giaCu")));
        lichSuGiaSanPham.setGiaMoi(parseMoneySafely(request.getParameter("giaMoi")));
        lichSuGiaSanPham.setLyDo(normalize(request.getParameter("lyDo")));

        String ngayApDungStr = request.getParameter("ngayApDung");
        if (ngayApDungStr == null || ngayApDungStr.trim().isEmpty()) {
            if (lichSuGiaSanPham.getNgayApDung() == null) {
                lichSuGiaSanPham.setNgayApDung(LocalDateTime.now());
            }
        } else {
            try {
                lichSuGiaSanPham.setNgayApDung(LocalDateTime.parse(ngayApDungStr.trim()));
            } catch (DateTimeParseException e) {
                if (lichSuGiaSanPham.getNgayApDung() == null) {
                    lichSuGiaSanPham.setNgayApDung(LocalDateTime.now());
                }
            }
        }

        return lichSuGiaSanPham;
    }

    private void validateHistory(LichSuGiaSanPham lichSuGiaSanPham, LichSuGiaSanPham existing) {
        if (lichSuGiaSanPham == null) {
            throw new IllegalArgumentException("Dữ liệu lịch sử giá không hợp lệ.");
        }

        if (lichSuGiaSanPham.getMaSP() == null || lichSuGiaSanPham.getMaSP().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }

        if (!sanPhamDAO.existsById(lichSuGiaSanPham.getMaSP())) {
            throw new IllegalArgumentException("Mã sản phẩm không tồn tại trong danh sách sản phẩm.");
        }

        if (lichSuGiaSanPham.getGiaCu() == null || lichSuGiaSanPham.getGiaCu().signum() < 0) {
            throw new IllegalArgumentException("Giá cũ không được nhỏ hơn 0.");
        }

        if (lichSuGiaSanPham.getGiaMoi() == null || lichSuGiaSanPham.getGiaMoi().signum() < 0) {
            throw new IllegalArgumentException("Giá mới không được nhỏ hơn 0.");
        }

        if (lichSuGiaSanPham.getNgayApDung() == null) {
            if (existing != null && existing.getNgayApDung() != null) {
                lichSuGiaSanPham.setNgayApDung(existing.getNgayApDung());
            } else {
                lichSuGiaSanPham.setNgayApDung(LocalDateTime.now());
            }
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseMoneyOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private BigDecimal parseMoneySafely(String value) {
        try {
            return parseMoneyOrZero(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDateTime parseDateTimeOrNow(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(value.trim());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
