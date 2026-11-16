package servlet;

import dao.DatHangDAO;
import dao.DoanhThuDAO;
import dao.KhachHangDAO;
import dao.KhuyenMaiDAO;
import dao.HoaDonDAO;
import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.DatHang;
import entity.HoaDon;
import entity.NhanVien;
import entity.TaiKhoan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.OrderStatusUtils;
import utils.RoleUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/hoadon")
public class HoaDonServlet extends HttpServlet {

    private final HoaDonDAO dao = new HoaDonDAO();
    private final DatHangDAO datHangDAO = new DatHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final KhuyenMaiDAO khuyenMaiDAO = new KhuyenMaiDAO();
    private final DoanhThuDAO doanhThuDAO = new DoanhThuDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listHoaDon(request, response);
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
                deleteHoaDon(request, response);
                break;
            default:
                listHoaDon(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertHoaDon(request, response);
        } else if ("update".equals(action)) {
            updateHoaDon(request, response);
        } else if ("claim".equals(action) || "complete".equals(action)) {
            processWorkflowAction(request, response, action);
        } else {
            response.sendRedirect(request.getContextPath() + "/hoadon");
        }
    }

    // =========================
    // DANH SÁCH
    // =========================
    private void listHoaDon(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TaiKhoan currentUser = resolveCurrentUser(request);
        HttpSession session = request.getSession(false);
        String currentMaNV = resolveCurrentMaNV(currentUser, session);
        boolean managerView = currentUser != null && RoleUtils.isManagerRole(currentUser.getVaiTro());
        boolean deliveryView = currentUser != null && RoleUtils.isDeliveryRole(currentUser.getVaiTro());
        boolean employeeWorkView = !managerView && (deliveryView || (currentUser != null && RoleUtils.isEmployeeRole(currentUser.getVaiTro())));
        List<HoaDon> dsHoaDon = resolveVisibleHoaDonList(currentUser, currentMaNV);
        request.setAttribute("dsHoaDon", dsHoaDon);
        request.setAttribute("lockedHoaDonMap", buildLockedMap(dao.findCompletedMaHDs()));
        request.setAttribute("canManageHoaDon", canManageHoaDon(currentUser, session));
        request.setAttribute("employeeWorkView", employeeWorkView);
        request.setAttribute("deliveryView", deliveryView);
        request.setAttribute("currentEmployeeCode", currentMaNV);
        request.setAttribute("deliveryEmployees", nhanVienDAO.findDeliveryEmployees());
        request.getRequestDispatcher("/views/hoadon/list.jsp").forward(request, response);
    }

    // =========================
    // FORM THÊM
    // =========================
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TaiKhoan currentUser = resolveCurrentUser(request);
        HttpSession session = request.getSession(false);
        if (!canManageHoaDon(currentUser, session)) {
            forwardHoaDonListWithError(request, response, "Bạn không có quyền thêm hóa đơn.");
            return;
        }
        String defaultMaDatHang = trimToNull(request.getParameter("maDatHang"));
        request.setAttribute("defaultMaDatHang", defaultMaDatHang);
        request.setAttribute("nextMaHD", dao.generateNextMaHD());
        request.setAttribute("defaultMaNV", resolveInvoiceEmployeeCode(currentUser, session, defaultMaDatHang, request.getParameter("maNV")));
        request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
    }

    // =========================
    // FORM SỬA
    // =========================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maHD = request.getParameter("id");

        if (maHD == null || maHD.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        HoaDon hoaDon = dao.findById(maHD.trim());

        if (hoaDon == null) {
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        TaiKhoan currentUser = resolveCurrentUser(request);
        HttpSession session = request.getSession(false);
        if (!canManageHoaDon(currentUser, session)) {
            forwardHoaDonListWithError(request, response, "Bạn không có quyền sửa hóa đơn.");
            return;
        }
        if (!canManageHoaDon(hoaDon, currentUser, session)) {
            forwardHoaDonListWithError(request, response, "Bạn chỉ được thao tác trên hóa đơn của mình.");
            return;
        }

        if (isLocked(hoaDon)) {
            forwardHoaDonListWithError(request, response, "Hóa đơn đã thanh toán nên không thể chỉnh sửa.");
            return;
        }

        request.setAttribute("hoaDon", hoaDon);
        request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
    }

    // =========================
    // THÊM HÓA ĐƠN
    // =========================
    private void insertHoaDon(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TaiKhoan currentUser = resolveCurrentUser(request);
        HttpSession session = request.getSession(false);
        String maHD = dao.generateNextMaHD();
        String maKH = request.getParameter("maKH");
        String maDatHang = trimToNull(request.getParameter("maDatHang"));
        String maNV = resolveInvoiceEmployeeCode(currentUser, session, maDatHang, request.getParameter("maNV"));
        String maKM = request.getParameter("maKM");
        String ngayLapStr = request.getParameter("ngayLap");
        String tongTienStr = request.getParameter("tongTien");
        String giamGiaStr = request.getParameter("giamGia");
        String thanhTienStr = request.getParameter("thanhTien");

        try {
            if (trimToNull(maKH) == null || !khachHangDAO.existsById(maKH.trim())) {
                request.setAttribute("error", "Mã khách hàng không tồn tại hoặc không được để trống!");
                prepareCreateFormDefaults(request, currentUser, session);
                request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
                return;
            }

            if (maDatHang != null && datHangDAO.findById(maDatHang) == null) {
                request.setAttribute("error", "Mã đặt hàng không tồn tại!");
                prepareCreateFormDefaults(request, currentUser, session);
                request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
                return;
            }

            if (maKM != null && !maKM.trim().isEmpty() && !khuyenMaiDAO.existsById(maKM.trim())) {
                request.setAttribute("error", "Mã khuyến mãi không tồn tại!");
                prepareCreateFormDefaults(request, currentUser, session);
                request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
                return;
            }

            if (trimToNull(maNV) == null) {
                request.setAttribute("error", "Mã nhân viên không được để trống!");
                prepareCreateFormDefaults(request, currentUser, session);
                request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
                return;
            }

            HoaDon hd = new HoaDon();
            hd.setMaHD(maHD);
            hd.setMaKH(maKH.trim());
            hd.setMaNV(trimToNull(maNV));
            hd.setMaNVGiaoHang(null);
            hd.setDaNhanGiaoHang(Boolean.FALSE);
            hd.setMaDatHang(maDatHang);
            hd.setMaKM(maKM != null && !maKM.trim().isEmpty() ? maKM.trim() : null);

            if (ngayLapStr != null && !ngayLapStr.trim().isEmpty()) {
                hd.setNgayLap(Timestamp.valueOf(ngayLapStr.trim().replace("T", " ") + ":00"));
            } else {
                hd.setNgayLap(new Timestamp(System.currentTimeMillis()));
            }

            BigDecimal tongTien = parseBigDecimalOrZero(tongTienStr);
            BigDecimal giamGia = parseBigDecimalOrZero(giamGiaStr);
            BigDecimal thanhTien = computeThanhTien(tongTien, giamGia, thanhTienStr);

            hd.setTongTien(tongTien);
            hd.setGiamGia(giamGia);
            hd.setThanhTien(thanhTien);
            hd.setSoDienThoai(null);
            hd.setDiaChiGiaoHang(null);
            hd.setTrangThai("CHO_NHAN");

            dao.create(hd);

            response.sendRedirect(request.getContextPath() + "/hoadon");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm hóa đơn: " + e.getMessage());
            prepareCreateFormDefaults(request, currentUser, session);
            request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
        }
    }

    // =========================
    // CẬP NHẬT HÓA ĐƠN
    // =========================
    private void updateHoaDon(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maHD = request.getParameter("maHD");
        String maKH = request.getParameter("maKH");
        String maDatHang = trimToNull(request.getParameter("maDatHang"));
        String maKM = request.getParameter("maKM");
        String ngayLapStr = request.getParameter("ngayLap");
        String tongTienStr = request.getParameter("tongTien");
        String giamGiaStr = request.getParameter("giamGia");
        String thanhTienStr = request.getParameter("thanhTien");

        if (maHD == null || maHD.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        HoaDon hd = dao.findById(maHD.trim());

        if (hd == null) {
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        TaiKhoan currentUser = resolveCurrentUser(request);
        HttpSession session = request.getSession(false);
        if (!canManageHoaDon(hd, currentUser, session)) {
            forwardHoaDonListWithError(request, response, "Bạn chỉ được thao tác trên hóa đơn của mình.");
            return;
        }

        if (isLocked(hd)) {
            forwardHoaDonListWithError(request, response, "Hóa đơn đã thanh toán nên không thể chỉnh sửa.");
            return;
        }

        try {
            if (trimToNull(maKH) == null || !khachHangDAO.existsById(maKH.trim())) {
                request.setAttribute("error", "Mã khách hàng không tồn tại hoặc không được để trống!");
                request.setAttribute("hoaDon", hd);
                request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
                return;
            }

            if (maDatHang != null && datHangDAO.findById(maDatHang) == null) {
                request.setAttribute("error", "Mã đặt hàng không tồn tại!");
                request.setAttribute("hoaDon", hd);
                request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
                return;
            }

            if (maKM != null && !maKM.trim().isEmpty() && !khuyenMaiDAO.existsById(maKM.trim())) {
                request.setAttribute("error", "Mã khuyến mãi không tồn tại!");
                request.setAttribute("hoaDon", hd);
                request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
                return;
            }

            String maNV = trimToNull(hd.getMaNV());
            if (maNV == null) {
                maNV = resolveInvoiceEmployeeCode(currentUser, session, maDatHang != null ? maDatHang : hd.getMaDatHang(), request.getParameter("maNV"));
            }
            if (maNV == null) {
                request.setAttribute("error", "Mã nhân viên không được để trống!");
                request.setAttribute("hoaDon", hd);
                request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
                return;
            }

            hd.setMaKH(maKH.trim());
            hd.setMaNV(maNV);
            hd.setMaDatHang(maDatHang != null ? maDatHang : hd.getMaDatHang());
            hd.setMaKM(maKM != null && !maKM.trim().isEmpty() ? maKM.trim() : null);

            if (ngayLapStr != null && !ngayLapStr.trim().isEmpty()) {
                hd.setNgayLap(Timestamp.valueOf(ngayLapStr.trim().replace("T", " ") + ":00"));
            }

            BigDecimal tongTien = parseBigDecimalOrZero(tongTienStr);
            BigDecimal giamGia = parseBigDecimalOrZero(giamGiaStr);
            hd.setTongTien(tongTien);
            hd.setGiamGia(giamGia);
            hd.setThanhTien(computeThanhTien(tongTien, giamGia, thanhTienStr));

            dao.update(hd);

            response.sendRedirect(request.getContextPath() + "/hoadon");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật hóa đơn: " + e.getMessage());
            request.setAttribute("hoaDon", hd);
            request.getRequestDispatcher("/views/hoadon/form.jsp").forward(request, response);
        }
    }

    // =========================
    // XÓA HÓA ĐƠN
    // =========================
    private void deleteHoaDon(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maHD = request.getParameter("id");

        if (maHD != null && !maHD.trim().isEmpty()) {
            HoaDon hoaDon = dao.findById(maHD.trim());
            TaiKhoan currentUser = resolveCurrentUser(request);
            HttpSession session = request.getSession(false);
            if (!canManageHoaDon(currentUser, session)) {
                forwardHoaDonListWithError(request, response, "Bạn không có quyền xóa hóa đơn.");
                return;
            }
            if (hoaDon != null && !canManageHoaDon(hoaDon, currentUser, session)) {
                forwardHoaDonListWithError(request, response, "Bạn chỉ được thao tác trên hóa đơn của mình.");
                return;
            }
            if (hoaDon != null && isLocked(hoaDon)) {
                forwardHoaDonListWithError(request, response, "Hóa đơn đã thanh toán nên không thể xóa.");
                return;
            }
            try {
                dao.delete(maHD.trim());
            } catch (RuntimeException e) {
                forwardHoaDonListWithError(request, response, "Không thể xóa hóa đơn: " + e.getMessage());
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/hoadon");
    }

    private void processWorkflowAction(HttpServletRequest request, HttpServletResponse response, String action)
            throws ServletException, IOException {

        TaiKhoan currentUser = resolveCurrentUser(request);
        HttpSession session = request.getSession(false);
        String maHD = trimToNull(request.getParameter("id"));
        if (maHD == null) {
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        HoaDon hoaDon = dao.findById(maHD);
        if (hoaDon == null) {
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        boolean managerRole = currentUser != null && RoleUtils.isManagerRole(currentUser.getVaiTro());
        boolean employeeRole = currentUser != null && RoleUtils.isEmployeeRole(currentUser.getVaiTro());
        boolean deliveryRole = currentUser != null && RoleUtils.isDeliveryRole(currentUser.getVaiTro());
        String currentMaNV = resolveCurrentMaNV(currentUser, session);

        if (currentMaNV == null || (!employeeRole && !deliveryRole && !managerRole)) {
            forwardHoaDonListWithError(request, response, "Tài khoản chưa có mã nhân viên.");
            return;
        }

        if (OrderStatusUtils.isCompleted(hoaDon.getTrangThai()) || OrderStatusUtils.isCancelled(hoaDon.getTrangThai())) {
            forwardHoaDonListWithError(request, response, "Hóa đơn này đã hoàn tất.");
            return;
        }

        boolean deliveryOrder = hasText(hoaDon.getDiaChiGiaoHang());

        try {
            if (deliveryRole && !employeeRole && !managerRole) {
                handleDeliveryWorkflow(hoaDon, currentMaNV, action, request, response);
                return;
            }

            handleEmployeeWorkflow(hoaDon, currentMaNV, action, deliveryOrder, managerRole, request, response);
        } catch (RuntimeException ex) {
            forwardHoaDonListWithError(request, response, ex.getMessage() != null ? ex.getMessage() : "Không thể xử lý hóa đơn.");
        }
    }

    private void handleEmployeeWorkflow(HoaDon hoaDon, String currentMaNV, String action, boolean deliveryOrder,
                                        boolean managerRole, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String normalizedAction = trimToNull(action);
        if (normalizedAction == null) {
            forwardHoaDonListWithError(request, response, "Hành động không hợp lệ.");
            return;
        }

        String assignedMaNV = trimToNull(hoaDon.getMaNV());
        if ("claim".equalsIgnoreCase(normalizedAction)) {
            if (assignedMaNV != null && !assignedMaNV.equalsIgnoreCase(currentMaNV) && !managerRole) {
                forwardHoaDonListWithError(request, response, "Hóa đơn này đã được nhân viên khác nhận.");
                return;
            }

            hoaDon.setMaNV(currentMaNV);
            hoaDon.setTrangThai("DANG_LAM");
            dao.updateEmployee(hoaDon.getMaHD(), currentMaNV);
            dao.updateStatus(hoaDon.getMaHD(), "DANG_LAM");
            syncLinkedOrder(hoaDon.getMaDatHang(), currentMaNV, null, null, "DANG_LAM", null);
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        if (!"complete".equalsIgnoreCase(normalizedAction)) {
            forwardHoaDonListWithError(request, response, "Hành động không hợp lệ.");
            return;
        }

        if (assignedMaNV != null && !assignedMaNV.equalsIgnoreCase(currentMaNV) && !managerRole) {
            forwardHoaDonListWithError(request, response, "Hóa đơn này đang do nhân viên khác xử lý.");
            return;
        }

        if (assignedMaNV == null) {
            hoaDon.setMaNV(currentMaNV);
            dao.updateEmployee(hoaDon.getMaHD(), currentMaNV);
        }

        if (deliveryOrder) {
            String deliveryMaNV = trimToNull(request.getParameter("deliveryMaNV"));
            if (deliveryMaNV == null) {
                forwardHoaDonListWithError(request, response, "Vui lòng chọn nhân viên giao hàng.");
                return;
            }

            hoaDon.setMaNVGiaoHang(deliveryMaNV);
            hoaDon.setDaNhanGiaoHang(Boolean.FALSE);
            hoaDon.setTrangThai("DANG_GIAO");
            dao.updateDeliveryEmployee(hoaDon.getMaHD(), deliveryMaNV);
            dao.updateDeliveryAccepted(hoaDon.getMaHD(), false);
            dao.updateStatus(hoaDon.getMaHD(), "DANG_GIAO");
            syncLinkedOrder(hoaDon.getMaDatHang(), currentMaNV, deliveryMaNV, Boolean.FALSE, "DANG_GIAO", null);
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        hoaDon.setTrangThai("HOAN_THANH");
        dao.updateStatus(hoaDon.getMaHD(), "HOAN_THANH");
        syncLinkedOrder(hoaDon.getMaDatHang(), currentMaNV, null, null, "HOAN_THANH", "HOAN_THANH");
        doanhThuDAO.upsertForHoaDon(hoaDon);
        response.sendRedirect(request.getContextPath() + "/hoadon");
    }

    private void handleDeliveryWorkflow(HoaDon hoaDon, String currentMaNV, String action,
                                        HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String assignedDeliveryMaNV = trimToNull(hoaDon.getMaNVGiaoHang());
        if (assignedDeliveryMaNV == null || !assignedDeliveryMaNV.equalsIgnoreCase(currentMaNV)) {
            forwardHoaDonListWithError(request, response, "Hóa đơn này không được giao cho bạn.");
            return;
        }

        String normalizedAction = trimToNull(action);
        if ("claim".equalsIgnoreCase(normalizedAction)) {
            hoaDon.setDaNhanGiaoHang(Boolean.TRUE);
            dao.updateDeliveryAccepted(hoaDon.getMaHD(), true);
            syncLinkedOrder(hoaDon.getMaDatHang(), null, currentMaNV, Boolean.TRUE, null, null);
            response.sendRedirect(request.getContextPath() + "/hoadon");
            return;
        }

        if (!"complete".equalsIgnoreCase(normalizedAction)) {
            forwardHoaDonListWithError(request, response, "Hành động không hợp lệ.");
            return;
        }

        if (!Boolean.TRUE.equals(hoaDon.getDaNhanGiaoHang())) {
            forwardHoaDonListWithError(request, response, "Bạn cần xác nhận nhận đơn trước.");
            return;
        }

        hoaDon.setTrangThai("HOAN_THANH");
        dao.updateStatus(hoaDon.getMaHD(), "HOAN_THANH");
        syncLinkedOrder(hoaDon.getMaDatHang(), null, currentMaNV, Boolean.TRUE, "HOAN_THANH", "HOAN_THANH");
        doanhThuDAO.upsertForHoaDon(hoaDon);
        response.sendRedirect(request.getContextPath() + "/hoadon");
    }

    private void syncLinkedOrder(String maDatHang, String maNV, String maNVGiaoHang, Boolean daNhanGiaoHang,
                                 String trangThai, String invoiceTrangThai) {
        String normalizedMaDatHang = trimToNull(maDatHang);
        if (normalizedMaDatHang == null) {
            return;
        }

        DatHang order = datHangDAO.findById(normalizedMaDatHang);
        if (order == null) {
            return;
        }

        if (maNV != null) {
            order.setMaNV(maNV);
        }
        if (maNVGiaoHang != null) {
            order.setMaNVGiaoHang(maNVGiaoHang);
        }
        if (daNhanGiaoHang != null) {
            order.setDaNhanGiaoHang(daNhanGiaoHang);
        }
        if (trangThai != null) {
            order.setTrangThai(trangThai);
        }
        datHangDAO.update(order);
    }

    // =========================
    // HÀM HỖ TRỢ
    // =========================
    private BigDecimal parseBigDecimalOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private BigDecimal computeThanhTien(BigDecimal tongTien, BigDecimal giamGia, String providedThanhTien) {
        BigDecimal normalizedTongTien = tongTien != null ? tongTien : BigDecimal.ZERO;
        BigDecimal normalizedGiamGia = giamGia != null ? giamGia : BigDecimal.ZERO;
        BigDecimal computed = normalizedTongTien.subtract(normalizedGiamGia);
        if (computed.signum() < 0) {
            throw new IllegalArgumentException("Giảm giá không được lớn hơn tổng tiền.");
        }

        BigDecimal provided = parseNullableBigDecimal(providedThanhTien);
        if (provided != null && provided.compareTo(computed) != 0) {
            return computed;
        }

        return computed;
    }

    private BigDecimal parseNullableBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    private void prepareCreateFormDefaults(HttpServletRequest request, TaiKhoan currentUser, HttpSession session) {
        String defaultMaDatHang = trimToNull(request.getParameter("maDatHang"));
        request.setAttribute("defaultMaDatHang", defaultMaDatHang);
        request.setAttribute("nextMaHD", dao.generateNextMaHD());
        request.setAttribute("defaultMaNV", resolveInvoiceEmployeeCode(currentUser, session, defaultMaDatHang, request.getParameter("maNV")));
    }

    private boolean isLocked(HoaDon hoaDon) {
        return hoaDon != null && dao.isLocked(hoaDon.getMaHD());
    }

    private boolean canManageHoaDon(TaiKhoan currentUser, HttpSession session) {
        return currentUser != null
                && (RoleUtils.isManagerRole(currentUser.getVaiTro())
                || RoleUtils.isEmployeeRole(currentUser.getVaiTro()));
    }

    private boolean canManageHoaDon(HoaDon hoaDon, TaiKhoan currentUser, HttpSession session) {
        if (hoaDon == null || currentUser == null) {
            return false;
        }

        if (RoleUtils.isManagerRole(currentUser.getVaiTro())) {
            return true;
        }

        if (!RoleUtils.isEmployeeRole(currentUser.getVaiTro())) {
            return false;
        }

        String currentMaNV = resolveCurrentMaNV(currentUser, session);
        return currentMaNV != null && currentMaNV.equalsIgnoreCase(resolveHoaDonEmployeeCode(hoaDon));
    }

    private List<HoaDon> resolveVisibleHoaDonList(TaiKhoan currentUser, String currentMaNV) {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        if (RoleUtils.isManagerRole(currentUser.getVaiTro())) {
            return dao.findAll();
        }

        if (RoleUtils.isDeliveryRole(currentUser.getVaiTro())) {
            return dao.findVisibleForDelivery(currentMaNV);
        }

        if (RoleUtils.isEmployeeRole(currentUser.getVaiTro())) {
            return dao.findVisibleForEmployee(currentMaNV);
        }

        return Collections.emptyList();
    }

    private List<HoaDon> resolveVisibleHoaDonList(TaiKhoan currentUser, HttpSession session, Map<String, String> orderEmployeeMap, Map<String, String> orderDeliveryMap) {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        if (RoleUtils.isManagerRole(currentUser.getVaiTro())) {
            return dao.findAll();
        }

        if (RoleUtils.isEmployeeRole(currentUser.getVaiTro())) {
            String currentMaNV = resolveCurrentMaNV(currentUser, session);
            if (currentMaNV == null) {
                return Collections.emptyList();
            }

            List<HoaDon> visibleHoaDon = new ArrayList<>();
            for (HoaDon hoaDon : dao.findAll()) {
                if (hoaDon == null) {
                    continue;
                }

                String hoaDonEmployeeCode = resolveHoaDonEmployeeCode(hoaDon, orderEmployeeMap);
                if (currentMaNV.equalsIgnoreCase(hoaDonEmployeeCode)) {
                    visibleHoaDon.add(hoaDon);
                }
            }
            return visibleHoaDon;
        }

        if (RoleUtils.isDeliveryRole(currentUser.getVaiTro())) {
            String currentMaNV = resolveCurrentMaNV(currentUser, session);
            if (currentMaNV == null) {
                return Collections.emptyList();
            }

            List<HoaDon> visibleHoaDon = new ArrayList<>();
            for (HoaDon hoaDon : dao.findAll()) {
                if (hoaDon == null) {
                    continue;
                }

                String hoaDonMaNV = trimToNull(hoaDon.getMaNV());
                String deliveryMaNV = null;
                String maDatHang = trimToNull(hoaDon.getMaDatHang());
                if (maDatHang != null && orderDeliveryMap != null) {
                    deliveryMaNV = trimToNull(orderDeliveryMap.get(maDatHang));
                }

                if (currentMaNV.equalsIgnoreCase(hoaDonMaNV)
                        || currentMaNV.equalsIgnoreCase(deliveryMaNV)) {
                    visibleHoaDon.add(hoaDon);
                }
            }
            return visibleHoaDon;
        }

        return Collections.emptyList();
    }

    private TaiKhoan resolveCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session != null ? session.getAttribute("user") : null;
        return user instanceof TaiKhoan ? (TaiKhoan) user : null;
    }

    private String resolveCurrentMaNV(TaiKhoan currentUser, HttpSession session) {
        if (currentUser == null) {
            return null;
        }

        String maNV = trimToNull(currentUser.getMaNV());
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

        String tenDangNhap = trimToNull(currentUser.getTenDangNhap());
        if (tenDangNhap == null) {
            return null;
        }

        return taiKhoanDAO.findMaNVByUsername(tenDangNhap);
    }

    private String resolveInvoiceEmployeeCode(TaiKhoan currentUser, HttpSession session, String maDatHang, String requestedMaNV) {
        String orderMaNV = resolveOrderEmployeeCode(maDatHang);
        if (orderMaNV != null) {
            return orderMaNV;
        }

        String currentMaNV = resolveCurrentMaNV(currentUser, session);
        if (currentMaNV != null) {
            return currentMaNV;
        }

        String trimmedRequestedMaNV = trimToNull(requestedMaNV);
        if (trimmedRequestedMaNV != null) {
            return trimmedRequestedMaNV;
        }

        return null;
    }

    private String resolveOrderEmployeeCode(String maDatHang) {
        String normalizedMaDatHang = trimToNull(maDatHang);
        if (normalizedMaDatHang == null) {
            return null;
        }

        DatHang order = datHangDAO.findById(normalizedMaDatHang);
        if (order == null) {
            return null;
        }

        return trimToNull(order.getMaNV());
    }

    private Map<String, String> buildOrderEmployeeMap() {
        Map<String, String> orderEmployeeMap = new HashMap<>();
        for (DatHang order : datHangDAO.findAll()) {
            if (order == null) {
                continue;
            }

            String maDatHang = trimToNull(order.getMaDatHang());
            String maNV = trimToNull(order.getMaNV());
            if (maDatHang != null && maNV != null) {
                orderEmployeeMap.put(maDatHang, maNV);
            }
        }
        return orderEmployeeMap;
    }

    private Map<String, String> buildOrderDeliveryMap() {
        Map<String, String> orderDeliveryMap = new HashMap<>();
        for (DatHang order : datHangDAO.findAll()) {
            if (order == null) {
                continue;
            }

            String maDatHang = trimToNull(order.getMaDatHang());
            String maNVGiaoHang = trimToNull(order.getMaNVGiaoHang());
            if (maDatHang != null && maNVGiaoHang != null) {
                orderDeliveryMap.put(maDatHang, maNVGiaoHang);
            }
        }
        return orderDeliveryMap;
    }

    private Map<String, String> buildHoaDonMaNVMap(List<HoaDon> hoaDons, Map<String, String> orderEmployeeMap) {
        Map<String, String> hoaDonMaNVMap = new HashMap<>();
        if (hoaDons == null || hoaDons.isEmpty()) {
            return hoaDonMaNVMap;
        }

        for (HoaDon hoaDon : hoaDons) {
            if (hoaDon == null) {
                continue;
            }

            String maHD = trimToNull(hoaDon.getMaHD());
            String maNV = resolveHoaDonEmployeeCode(hoaDon, orderEmployeeMap);
            if (maHD != null && maNV != null) {
                hoaDonMaNVMap.put(maHD, maNV);
            }
        }

        return hoaDonMaNVMap;
    }

    private String resolveHoaDonEmployeeCode(HoaDon hoaDon) {
        return resolveHoaDonEmployeeCode(hoaDon, buildOrderEmployeeMap());
    }

    private String resolveHoaDonEmployeeCode(HoaDon hoaDon, Map<String, String> orderEmployeeMap) {
        if (hoaDon == null) {
            return null;
        }

        String hoaDonMaNV = trimToNull(hoaDon.getMaNV());
        if (hoaDonMaNV != null) {
            return hoaDonMaNV;
        }

        String maDatHang = trimToNull(hoaDon.getMaDatHang());
        if (maDatHang == null || orderEmployeeMap == null) {
            return null;
        }

        return trimToNull(orderEmployeeMap.get(maDatHang));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return trimToNull(value) != null;
    }

    private void forwardHoaDonListWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        listHoaDon(request, response);
    }

    private java.util.Map<String, Boolean> buildLockedMap(List<String> ids) {
        java.util.Map<String, Boolean> lockedMap = new java.util.HashMap<>();
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
