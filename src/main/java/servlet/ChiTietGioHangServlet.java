package servlet;

import dao.ChiTietGioHangDAO;
import dao.DatHangDAO;
import dao.GioHangDAO;
import dao.SizeDAO;
import dao.SanPhamDAO;
import entity.ChiTietGioHang;
import entity.DatHang;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/chitietgiohang")
public class ChiTietGioHangServlet extends HttpServlet {

    private final ChiTietGioHangDAO dao = new ChiTietGioHangDAO();
    private final DatHangDAO datHangDAO = new DatHangDAO();
    private final GioHangDAO gioHangDAO = new GioHangDAO();
    private final SizeDAO sizeDAO = new SizeDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listChiTietGioHang(request, response);
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
                deleteChiTietGioHang(request, response);
                break;
            default:
                listChiTietGioHang(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertChiTietGioHang(request, response);
        } else if ("update".equals(action)) {
            updateChiTietGioHang(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/chitietgiohang");
        }
    }

    private void listChiTietGioHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ChiTietGioHang> ds = dao.findAll();
        request.setAttribute("dsChiTietGioHang", ds);
        request.setAttribute("completedDatHangMap", buildCompletedMap(datHangDAO.findCompletedMaDatHangs()));
        request.setAttribute("lockedDatHangMap", buildCompletedMap(datHangDAO.findLockedMaDatHangs()));
        request.getRequestDispatcher("/views/chitietgiohang/list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        loadSizeOptions(request);
        request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");

        if (isBlank(id)) {
            response.sendRedirect(request.getContextPath() + "/chitietgiohang");
            return;
        }

        try {
            Integer maCTGH = Integer.parseInt(id.trim());
            ChiTietGioHang ct = dao.findById(maCTGH);

            if (ct == null) {
                response.sendRedirect(request.getContextPath() + "/chitietgiohang");
                return;
            }

            if (isDatHangLocked(ct.getMaDatHang())) {
                forwardListWithError(request, response, "Đơn đặt hàng đã thanh toán nên không thể chỉnh sửa chi tiết.");
                return;
            }

            loadSizeOptions(request);
            request.setAttribute("chiTietGioHang", ct);
            request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/chitietgiohang");
        }
    }

    private void insertChiTietGioHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maCTGHStr = request.getParameter("maCTGH");
        String maGioHang = request.getParameter("maGioHang");
        String maDatHang = request.getParameter("maDatHang");
        String maSP = request.getParameter("maSP");
        String maSize = request.getParameter("maSize");
        String soLuongStr = request.getParameter("soLuong");
        String donGiaStr = request.getParameter("donGia");

        if (isBlank(maCTGHStr) || isBlank(maGioHang) || isBlank(maSP)
                || isBlank(maSize) || isBlank(soLuongStr) || isBlank(donGiaStr)) {

            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
            return;
        }

        try {
            Integer maCTGH = Integer.parseInt(maCTGHStr.trim());
            Integer soLuong = Integer.parseInt(soLuongStr.trim());
            BigDecimal donGia = new BigDecimal(donGiaStr.trim());

            if (!isBlank(maDatHang) && isDatHangLocked(maDatHang.trim())) {
                request.setAttribute("error", "Đơn đặt hàng đã thanh toán nên không thể thêm chi tiết vào đơn này.");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (!isBlank(maDatHang) && datHangDAO.findById(maDatHang.trim()) == null) {
                request.setAttribute("error", "Đơn đặt hàng không tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (gioHangDAO.findById(maGioHang.trim()) == null) {
                request.setAttribute("error", "Giỏ hàng không tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (sanPhamDAO.findById(maSP.trim()) == null) {
                request.setAttribute("error", "Sản phẩm không tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (sizeDAO.findById(maSize.trim()) == null) {
                request.setAttribute("error", "Size không tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (dao.existsById(maCTGH)) {
                request.setAttribute("error", "Mã chi tiết giỏ hàng đã tồn tại!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (soLuong <= 0) {
                request.setAttribute("error", "Số lượng phải lớn hơn 0!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                request.setAttribute("error", "Đơn giá không được âm!");
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            ChiTietGioHang ct = new ChiTietGioHang();
            ct.setMaCTGH(maCTGH);
            ct.setMaGioHang(maGioHang.trim());
            ct.setMaDatHang(!isBlank(maDatHang) ? maDatHang.trim() : null);
            ct.setMaSP(maSP.trim());
            ct.setMaSize(maSize.trim());
            ct.setSoLuong(soLuong);
            ct.setDonGia(donGia);

            dao.create(ct);
            response.sendRedirect(request.getContextPath() + "/chitietgiohang");

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Mã CTGH và số lượng phải là số hợp lệ!");
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm chi tiết giỏ hàng: " + e.getMessage());
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
        }
    }

    private void updateChiTietGioHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maCTGHStr = request.getParameter("maCTGH");
        String maGioHang = request.getParameter("maGioHang");
        String maDatHang = request.getParameter("maDatHang");
        String maSP = request.getParameter("maSP");
        String maSize = request.getParameter("maSize");
        String soLuongStr = request.getParameter("soLuong");
        String donGiaStr = request.getParameter("donGia");

        if (isBlank(maCTGHStr)) {
            response.sendRedirect(request.getContextPath() + "/chitietgiohang");
            return;
        }

        try {
            Integer maCTGH = Integer.parseInt(maCTGHStr.trim());
            ChiTietGioHang ct = dao.findById(maCTGH);

            if (ct == null) {
                response.sendRedirect(request.getContextPath() + "/chitietgiohang");
                return;
            }

            if (isDatHangLocked(ct.getMaDatHang())) {
                forwardListWithError(request, response, "Đơn đặt hàng đã thanh toán nên không thể chỉnh sửa chi tiết.");
                return;
            }

            if (isBlank(maGioHang) || isBlank(maSP) || isBlank(maSize)
                    || isBlank(soLuongStr) || isBlank(donGiaStr)) {
                request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
                request.setAttribute("chiTietGioHang", ct);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (!isBlank(maDatHang) && isDatHangLocked(maDatHang.trim())) {
                request.setAttribute("error", "Đơn đặt hàng đã thanh toán nên không thể chuyển chi tiết sang đơn này.");
                request.setAttribute("chiTietGioHang", ct);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (!isBlank(maDatHang) && datHangDAO.findById(maDatHang.trim()) == null) {
                request.setAttribute("error", "Đơn đặt hàng không tồn tại!");
                request.setAttribute("chiTietGioHang", ct);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (gioHangDAO.findById(maGioHang.trim()) == null) {
                request.setAttribute("error", "Giỏ hàng không tồn tại!");
                request.setAttribute("chiTietGioHang", ct);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (sanPhamDAO.findById(maSP.trim()) == null) {
                request.setAttribute("error", "Sản phẩm không tồn tại!");
                request.setAttribute("chiTietGioHang", ct);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (sizeDAO.findById(maSize.trim()) == null) {
                request.setAttribute("error", "Size không tồn tại!");
                request.setAttribute("chiTietGioHang", ct);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            Integer soLuong = Integer.parseInt(soLuongStr.trim());
            BigDecimal donGia = new BigDecimal(donGiaStr.trim());

            if (soLuong <= 0) {
                request.setAttribute("error", "Số lượng phải lớn hơn 0!");
                request.setAttribute("chiTietGioHang", ct);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                request.setAttribute("error", "Đơn giá không được âm!");
                request.setAttribute("chiTietGioHang", ct);
                loadSizeOptions(request);
                request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
                return;
            }

            ct.setMaGioHang(maGioHang.trim());
            ct.setMaDatHang(!isBlank(maDatHang) ? maDatHang.trim() : null);
            ct.setMaSP(maSP.trim());
            ct.setMaSize(maSize.trim());
            ct.setSoLuong(soLuong);
            ct.setDonGia(donGia);

            dao.update(ct);
            response.sendRedirect(request.getContextPath() + "/chitietgiohang");

        } catch (NumberFormatException e) {
            request.setAttribute("error", "Mã CTGH và số lượng phải là số hợp lệ!");
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật chi tiết giỏ hàng: " + e.getMessage());
            loadSizeOptions(request);
            request.getRequestDispatcher("/views/chitietgiohang/form.jsp").forward(request, response);
        }
    }

    private void deleteChiTietGioHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");

        if (!isBlank(id)) {
            try {
                Integer maCTGH = Integer.parseInt(id.trim());
                ChiTietGioHang ct = dao.findById(maCTGH);
                if (ct != null && isDatHangLocked(ct.getMaDatHang())) {
                    forwardListWithError(request, response, "Đơn đặt hàng đã thanh toán nên không thể xóa chi tiết.");
                    return;
                }
                dao.delete(maCTGH);
            } catch (NumberFormatException ignored) {
            }
        }

        response.sendRedirect(request.getContextPath() + "/chitietgiohang");
    }

    private void loadSizeOptions(HttpServletRequest request) {
        request.setAttribute("dsSize", sizeDAO.findAll());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isDatHangLocked(String maDatHang) {
        if (isBlank(maDatHang)) {
            return false;
        }

        return datHangDAO.isLocked(maDatHang.trim());
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
        listChiTietGioHang(request, response);
    }
}
