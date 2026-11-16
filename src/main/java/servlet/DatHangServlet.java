package servlet;

import dao.DatHangDAO;
import dao.KhachHangDAO;
import entity.DatHang;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.OrderStatusUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/dathang")
public class DatHangServlet extends HttpServlet {

    private final DatHangDAO dao = new DatHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listDatHang(request, response);
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
                deleteDatHang(request, response);
                break;
            default:
                listDatHang(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertDatHang(request, response);
        } else if ("update".equals(action)) {
            updateDatHang(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/dathang");
        }
    }

    // =========================
    // DANH SÁCH
    // =========================
    private void listDatHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<DatHang> dsDatHang = dao.findAll();
        request.setAttribute("dsDatHang", dsDatHang);
        request.setAttribute("lockedDatHangMap", buildLockedMap(dao.findLockedMaDatHangs()));
        request.getRequestDispatcher("/views/dathang/list.jsp").forward(request, response);
    }

    // =========================
    // FORM THÊM
    // =========================
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
    }

    // =========================
    // FORM SỬA
    // =========================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maDatHang = request.getParameter("id");

        if (maDatHang == null || maDatHang.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dathang");
            return;
        }

        DatHang datHang = dao.findById(maDatHang.trim());

        if (datHang == null) {
            response.sendRedirect(request.getContextPath() + "/dathang");
            return;
        }

        if (isInProgress(datHang.getTrangThai())) {
            forwardDatHangListWithError(request, response, "Đơn đang làm không thể chỉnh sửa.");
            return;
        }

        if (isCompleted(datHang.getTrangThai())) {
            forwardDatHangListWithError(request, response, "Đơn đặt hàng đã hoàn thành nên không thể chỉnh sửa.");
            return;
        }

        request.setAttribute("datHang", datHang);
        request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
    }

    // =========================
    // THÊM ĐẶT HÀNG
    // =========================
    private void insertDatHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maDatHang = request.getParameter("maDatHang");
        String maKH = request.getParameter("maKH");
        String ngayDatStr = request.getParameter("ngayDat");
        String trangThai = request.getParameter("trangThai");
        String soDienThoai = request.getParameter("soDienThoai");
        String diaChiGiaoHang = request.getParameter("diaChiGiaoHang");
        String tongTienStr = request.getParameter("tongTien");

        if (maDatHang == null || maDatHang.trim().isEmpty()
                || maKH == null || maKH.trim().isEmpty()) {

            request.setAttribute("error", "Mã đặt hàng và mã khách hàng không được để trống!");
            request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
            return;
        }

        if (dao.existsById(maDatHang.trim())) {
            request.setAttribute("error", "Mã đặt hàng đã tồn tại!");
            request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
            return;
        }

        if (!khachHangDAO.existsById(maKH.trim())) {
            request.setAttribute("error", "Mã khách hàng không tồn tại!");
            request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
            return;
        }

        try {
            DatHang dh = new DatHang();
            dh.setMaDatHang(maDatHang.trim());
            dh.setMaKH(maKH.trim());
            dh.setSoDienThoai(soDienThoai != null ? soDienThoai.trim() : null);
            dh.setDiaChiGiaoHang(diaChiGiaoHang != null ? diaChiGiaoHang.trim() : null);

            if (ngayDatStr != null && !ngayDatStr.trim().isEmpty()) {
                dh.setNgayDat(Timestamp.valueOf(ngayDatStr.trim().replace("T", " ") + ":00"));
            } else {
                dh.setNgayDat(new Timestamp(System.currentTimeMillis()));
            }

            String normalizedStatus = normalizeStatusOrDefault(trangThai, "Đang làm đơn");
            if (!OrderStatusUtils.isRecognized(normalizedStatus)) {
                request.setAttribute("error", "Trạng thái đơn hàng không hợp lệ!");
                request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
                return;
            }
            dh.setTrangThai(normalizedStatus);
            BigDecimal tongTien = parseBigDecimalOrZero(tongTienStr);
            if (tongTien.signum() < 0) {
                request.setAttribute("error", "Tổng tiền không được nhỏ hơn 0!");
                request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
                return;
            }
            dh.setTongTien(tongTien);

            dao.create(dh);

            response.sendRedirect(request.getContextPath() + "/dathang");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm đặt hàng: " + e.getMessage());
            request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
        }
    }

    // =========================
    // CẬP NHẬT ĐẶT HÀNG
    // =========================
    private void updateDatHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maDatHang = request.getParameter("maDatHang");
        String maKH = request.getParameter("maKH");
        String ngayDatStr = request.getParameter("ngayDat");
        String trangThai = request.getParameter("trangThai");
        String soDienThoai = request.getParameter("soDienThoai");
        String diaChiGiaoHang = request.getParameter("diaChiGiaoHang");
        String tongTienStr = request.getParameter("tongTien");

        if (maDatHang == null || maDatHang.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dathang");
            return;
        }

        DatHang dh = dao.findById(maDatHang.trim());

        if (dh == null) {
            response.sendRedirect(request.getContextPath() + "/dathang");
            return;
        }

        if (isInProgress(dh.getTrangThai())) {
            forwardDatHangListWithError(request, response, "Đơn đang làm không thể chỉnh sửa.");
            return;
        }

        if (isCompleted(dh.getTrangThai())) {
            forwardDatHangListWithError(request, response, "Đơn đặt hàng đã hoàn thành nên không thể chỉnh sửa.");
            return;
        }

        if (maKH == null || maKH.trim().isEmpty()) {
            request.setAttribute("error", "Mã khách hàng không được để trống!");
            request.setAttribute("datHang", dh);
            request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
            return;
        }

        if (!khachHangDAO.existsById(maKH.trim())) {
            request.setAttribute("error", "Mã khách hàng không tồn tại!");
            request.setAttribute("datHang", dh);
            request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
            return;
        }

        try {
            dh.setMaKH(maKH.trim());
            dh.setSoDienThoai(soDienThoai != null ? soDienThoai.trim() : null);
            dh.setDiaChiGiaoHang(diaChiGiaoHang != null ? diaChiGiaoHang.trim() : null);

            if (ngayDatStr != null && !ngayDatStr.trim().isEmpty()) {
                dh.setNgayDat(Timestamp.valueOf(ngayDatStr.trim().replace("T", " ") + ":00"));
            }

            String nextStatus = isBlank(trangThai) ? dh.getTrangThai() : trangThai.trim();
            if (!OrderStatusUtils.isRecognized(nextStatus)) {
                request.setAttribute("error", "Trạng thái đơn hàng không hợp lệ!");
                request.setAttribute("datHang", dh);
                request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
                return;
            }
            dh.setTrangThai(nextStatus);
            BigDecimal tongTien = parseBigDecimalOrZero(tongTienStr);
            if (tongTien.signum() < 0) {
                request.setAttribute("error", "Tổng tiền không được nhỏ hơn 0!");
                request.setAttribute("datHang", dh);
                request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
                return;
            }
            dh.setTongTien(tongTien);

            dao.update(dh);

            response.sendRedirect(request.getContextPath() + "/dathang");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật đặt hàng: " + e.getMessage());
            request.setAttribute("datHang", dh);
            request.getRequestDispatcher("/views/dathang/form.jsp").forward(request, response);
        }
    }

    private BigDecimal parseBigDecimalOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(value.trim());
    }

    // =========================
    // XÓA ĐẶT HÀNG
    // =========================
    private void deleteDatHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maDatHang = request.getParameter("id");

        if (maDatHang == null || maDatHang.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dathang");
            return;
        }

        String normalizedMaDatHang = maDatHang.trim();
        DatHang datHang = dao.findById(normalizedMaDatHang);
        if (datHang != null && dao.isLocked(datHang.getMaDatHang())) {
            forwardDatHangListWithError(request, response, "Đơn đặt hàng đã hoàn thành hoặc đã thanh toán nên không thể xóa.");
            return;
        }

        try {
            dao.delete(normalizedMaDatHang);
            response.sendRedirect(request.getContextPath() + "/dathang");
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = "Lỗi khi xóa đặt hàng.";
            }
            forwardDatHangListWithError(request, response, message);
        }
    }

    private boolean isCompleted(String status) {
        return OrderStatusUtils.isCompleted(status);
    }

    private boolean isInProgress(String status) {
        return OrderStatusUtils.isPreparing(status) || OrderStatusUtils.isDelivering(status);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeStatusOrDefault(String status, String defaultValue) {
        if (isBlank(status)) {
            return defaultValue;
        }

        return status.trim();
    }

    private void forwardDatHangListWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        listDatHang(request, response);
    }

    private Map<String, Boolean> buildLockedMap(List<String> ids) {
        Map<String, Boolean> lockedMap = new HashMap<>();
        if (ids != null) {
            for (String id : ids) {
                if (id != null) {
                    lockedMap.put(id, Boolean.TRUE);
                }
            }
        }
        return lockedMap;
    }
}
