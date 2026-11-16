package servlet;

import dao.ChiTietHoaDonDAO;
import dao.HoaDonDAO;
import dao.SizeDAO;
import dao.SanPhamDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.OrderStatusUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/chitiethoadon")
public class ChiTietHoaDonServlet extends HttpServlet {

    private final ChiTietHoaDonDAO dao = new ChiTietHoaDonDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final SizeDAO sizeDAO = new SizeDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listChiTietHoaDon(request, response);
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
                deleteChiTietHoaDon(request, response);
                break;
            default:
                listChiTietHoaDon(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertChiTietHoaDon(request, response);
        } else if ("update".equals(action)) {
            updateChiTietHoaDon(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/chitiethoadon");
        }
    }

    private void listChiTietHoaDon(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ChiTietHoaDon> dsChiTietHoaDon = dao.findAll();
        request.setAttribute("dsChiTietHoaDon", dsChiTietHoaDon);
        request.setAttribute("completedHoaDonMap", buildCompletedMap(hoaDonDAO.findCompletedMaHDs()));
        request.getRequestDispatcher("/views/chitiethoadon/list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        loadSizeOptions(request);
        request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");

        if (isBlank(id)) {
            response.sendRedirect(request.getContextPath() + "/chitiethoadon");
            return;
        }

        try {
            Integer maCTHD = Integer.parseInt(id.trim());
            ChiTietHoaDon chiTietHoaDon = dao.findById(maCTHD);

            if (chiTietHoaDon == null) {
                response.sendRedirect(request.getContextPath() + "/chitiethoadon");
                return;
            }

            if (isHoaDonCompleted(chiTietHoaDon.getMaHD())) {
                forwardListWithError(request, response, "Hóa đơn đã thanh toán nên không thể chỉnh sửa chi tiết.");
                return;
            }

            loadSizeOptions(request);
            request.setAttribute("chiTietHoaDon", chiTietHoaDon);
            request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/chitiethoadon");
        }
    }

    private void insertChiTietHoaDon(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maCTHDStr = request.getParameter("maCTHD");
        String maHD = request.getParameter("maHD");
        String maSP = request.getParameter("maSP");
        String maSize = request.getParameter("maSize");
        String soLuongStr = request.getParameter("soLuong");
        String donGiaStr = request.getParameter("donGia");

        if (isBlank(maCTHDStr) || isBlank(maHD) || isBlank(maSP)
                || isBlank(maSize) || isBlank(soLuongStr) || isBlank(donGiaStr)) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
            return;
        }

        try {
            Integer maCTHD = Integer.parseInt(maCTHDStr.trim());
            Integer soLuong = Integer.parseInt(soLuongStr.trim());
            BigDecimal donGia = new BigDecimal(donGiaStr.trim());

            if (isHoaDonCompleted(maHD.trim())) {
                request.setAttribute("error", "Hóa đơn đã thanh toán nên không thể thêm chi tiết.");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (hoaDonDAO.findById(maHD.trim()) == null) {
                request.setAttribute("error", "Hóa đơn không tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (sanPhamDAO.findById(maSP.trim()) == null) {
                request.setAttribute("error", "Sản phẩm không tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (sizeDAO.findById(maSize.trim()) == null) {
                request.setAttribute("error", "Size không tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (dao.existsById(maCTHD)) {
                request.setAttribute("error", "Mã chi tiết hóa đơn đã tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (soLuong <= 0) {
                request.setAttribute("error", "Số lượng phải lớn hơn 0!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                request.setAttribute("error", "Đơn giá không được âm!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));

            ChiTietHoaDon cthd = new ChiTietHoaDon();
            cthd.setMaCTHD(maCTHD);
            cthd.setMaHD(maHD.trim());
            cthd.setMaSP(maSP.trim());
            cthd.setMaSize(maSize.trim());
            cthd.setSoLuong(soLuong);
            cthd.setDonGia(donGia);
            cthd.setThanhTien(thanhTien);

            dao.create(cthd);
            response.sendRedirect(request.getContextPath() + "/chitiethoadon");

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Mã chi tiết, số lượng hoặc đơn giá không hợp lệ!");
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm chi tiết hóa đơn: " + e.getMessage());
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
        }
    }

    private void updateChiTietHoaDon(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maCTHDStr = request.getParameter("maCTHD");
        String maHD = request.getParameter("maHD");
        String maSP = request.getParameter("maSP");
        String maSize = request.getParameter("maSize");
        String soLuongStr = request.getParameter("soLuong");
        String donGiaStr = request.getParameter("donGia");

        if (isBlank(maCTHDStr)) {
            response.sendRedirect(request.getContextPath() + "/chitiethoadon");
            return;
        }

        try {
            Integer maCTHD = Integer.parseInt(maCTHDStr.trim());
            ChiTietHoaDon cthd = dao.findById(maCTHD);

            if (cthd == null) {
                response.sendRedirect(request.getContextPath() + "/chitiethoadon");
                return;
            }

            if (isHoaDonCompleted(cthd.getMaHD())) {
                forwardListWithError(request, response, "Hóa đơn đã thanh toán nên không thể chỉnh sửa chi tiết.");
                return;
            }

            if (isBlank(maHD) || isBlank(maSP) || isBlank(maSize)
                    || isBlank(soLuongStr) || isBlank(donGiaStr)) {
                request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
                request.setAttribute("chiTietHoaDon", cthd);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (isHoaDonCompleted(maHD.trim())) {
                request.setAttribute("error", "Hóa đơn đã thanh toán nên không thể chuyển chi tiết sang hóa đơn này.");
                request.setAttribute("chiTietHoaDon", cthd);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (hoaDonDAO.findById(maHD.trim()) == null) {
                request.setAttribute("error", "Hóa đơn không tồn tại!");
                request.setAttribute("chiTietHoaDon", cthd);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (sanPhamDAO.findById(maSP.trim()) == null) {
                request.setAttribute("error", "Sản phẩm không tồn tại!");
                request.setAttribute("chiTietHoaDon", cthd);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (sizeDAO.findById(maSize.trim()) == null) {
                request.setAttribute("error", "Size không tồn tại!");
                request.setAttribute("chiTietHoaDon", cthd);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            Integer soLuong = Integer.parseInt(soLuongStr.trim());
            BigDecimal donGia = new BigDecimal(donGiaStr.trim());

            if (soLuong <= 0) {
                request.setAttribute("error", "Số lượng phải lớn hơn 0!");
                request.setAttribute("chiTietHoaDon", cthd);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                request.setAttribute("error", "Đơn giá không được âm!");
                request.setAttribute("chiTietHoaDon", cthd);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
                return;
            }

            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));

            cthd.setMaHD(maHD.trim());
            cthd.setMaSP(maSP.trim());
            cthd.setMaSize(maSize.trim());
            cthd.setSoLuong(soLuong);
            cthd.setDonGia(donGia);
            cthd.setThanhTien(thanhTien);

            dao.update(cthd);
            response.sendRedirect(request.getContextPath() + "/chitiethoadon");

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Mã chi tiết, số lượng hoặc đơn giá không hợp lệ!");
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật chi tiết hóa đơn: " + e.getMessage());
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitiethoadon/form.jsp").forward(request, response);
        }
    }

    private void deleteChiTietHoaDon(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id != null && !id.trim().isEmpty()) {
            try {
                Integer maCTHD = Integer.parseInt(id.trim());
                ChiTietHoaDon chiTietHoaDon = dao.findById(maCTHD);
                if (chiTietHoaDon != null && isHoaDonCompleted(chiTietHoaDon.getMaHD())) {
                    forwardListWithError(request, response, "Hóa đơn đã thanh toán nên không thể xóa chi tiết.");
                    return;
                }
                dao.delete(maCTHD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/chitiethoadon");
    }

    private void loadSizeOptions(HttpServletRequest request) {
        request.setAttribute("dsSize", sizeDAO.findAll());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isHoaDonCompleted(String maHD) {
        if (isBlank(maHD)) {
            return false;
        }

        return hoaDonDAO.isLocked(maHD.trim());
    }

    private Map<String, Boolean> buildCompletedMap(List<String> ids) {
        Map<String, Boolean> completedMap = new HashMap<>();
        if (ids != null) {
            for (String id : ids) {
                if (id != null) {
                    completedMap.put(id, Boolean.TRUE);
                }
            }
        }
        return completedMap;
    }

    private void forwardListWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        listChiTietHoaDon(request, response);
    }
}
