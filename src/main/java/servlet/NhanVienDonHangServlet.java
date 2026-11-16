package servlet;

import dao.DatHangDAO;
import dao.DoanhThuDAO;
import dao.HoaDonDAO;
import dao.TaiKhoanDAO;
import entity.DatHang;
import entity.HoaDon;
import entity.TaiKhoan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.JPAUtil;
import utils.OrderStatusUtils;
import utils.RoleUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = {"/nhanvien-donhang", "/giaohang-donhang"})
public class NhanVienDonHangServlet extends HttpServlet {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));
    private final DatHangDAO dao = new DatHangDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final DoanhThuDAO doanhThuDAO = new DoanhThuDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TaiKhoan currentUser = resolveCurrentUser(request);
        HttpSession session = request.getSession(false);
        String action = trimToNull(request.getParameter("action"));

        if ("summary".equalsIgnoreCase(action)) {
            if (!isAuthorized(currentUser)) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập.");
                return;
            }

            writeSummary(response, resolveVisibleOrders(currentUser, session));
            return;
        }

        if (!isAuthorized(currentUser)) {
            response.sendRedirect(request.getContextPath() + loginPathForRequest(request));
            return;
        }

        response.sendRedirect(request.getContextPath() + RoleUtils.homePath(currentUser.getVaiTro()));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        TaiKhoan currentUser = resolveCurrentUser(request);
        HttpSession session = request.getSession(false);
        if (!isAuthorized(currentUser)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập.");
            return;
        }

        String action = normalizeAction(trimToNull(request.getParameter("action")));
        if (action == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ.");
            return;
        }

        String maDatHang = trimToNull(request.getParameter("id"));
        if (maDatHang == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã đơn hàng.");
            return;
        }

        String deliveryMaNV = trimToNull(request.getParameter("deliveryMaNV"));
        OrderWorkflowResult result = processOrderAction(currentUser, session, maDatHang, action, deliveryMaNV);
        if (!result.success) {
            writeError(response, result.statusCode, result.message);
            return;
        }

        writeOk(response, result.message);
    }

    private OrderWorkflowResult processOrderAction(TaiKhoan currentUser, HttpSession session, String maDatHang, String action, String deliveryMaNV) {
        String currentMaNV = resolveCurrentEmployeeCode(currentUser, session);
        if (currentMaNV == null) {
            return OrderWorkflowResult.error(HttpServletResponse.SC_BAD_REQUEST, "Tài khoản chưa có mã nhân viên.");
        }

        boolean deliveryRole = RoleUtils.isDeliveryRole(currentUser.getVaiTro());
        boolean managerRole = RoleUtils.isManagerRole(currentUser.getVaiTro());

        EntityManager em = JPAUtil.EMF.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            DatHang order = em.find(DatHang.class, maDatHang);
            if (order == null) {
                tx.rollback();
                return OrderWorkflowResult.error(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy đơn hàng.");
            }

            if (OrderStatusUtils.isCompleted(order.getTrangThai()) || OrderStatusUtils.isCancelled(order.getTrangThai())) {
                tx.rollback();
                return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Đơn hàng này đã được xử lý.");
            }

            if (deliveryRole) {
                if (!currentMaNV.equalsIgnoreCase(trimToNull(order.getMaNVGiaoHang()))) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Đơn này không được giao cho bạn.");
                }

                if ("claim".equals(action)) {
                    if (!OrderStatusUtils.isDelivering(order.getTrangThai())) {
                        tx.rollback();
                        return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Đơn này chưa sẵn sàng để nhận.");
                    }

                    order.setDaNhanGiaoHang(Boolean.TRUE);
                    em.merge(order);
                    tx.commit();
                    return OrderWorkflowResult.success("Đã nhận đơn " + order.getMaDatHang() + ".");
                }

                if (!"complete".equals(action)) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_BAD_REQUEST, "Tài xế chỉ được nhận đơn hoặc xác nhận đã giao.");
                }

                if (!Boolean.TRUE.equals(order.getDaNhanGiaoHang())) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Bạn cần nhận đơn trước khi xác nhận hoàn tất.");
                }

                order.setTrangThai("HOAN_THANH");
                order.setDaNhanGiaoHang(Boolean.TRUE);
                em.merge(order);
                syncInvoiceWithOrder(em, order, order.getMaNV(), true);
                tx.commit();
                persistRevenueIfAny(order.getMaDatHang());
                return OrderWorkflowResult.success("Đã xác nhận giao xong đơn " + order.getMaDatHang() + ".");
            }

            if ("claim".equals(action)) {
                String assignedMaNV = trimToNull(order.getMaNV());
                if (assignedMaNV != null && !assignedMaNV.equalsIgnoreCase(currentMaNV) && !managerRole) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Đơn này đã được nhân viên khác nhận.");
                }

                order.setMaNV(currentMaNV);
                if (!OrderStatusUtils.isDelivering(order.getTrangThai())) {
                    order.setTrangThai(resolveActiveOrderStatus(order));
                }
                em.merge(order);
                syncInvoiceWithOrder(em, order, currentMaNV, false);
                tx.commit();
                return OrderWorkflowResult.success("Đã nhận đơn " + order.getMaDatHang() + ".");
            }

            if ("assign-delivery".equals(action)) {
                if (!hasText(order.getDiaChiGiaoHang())) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Chỉ các đơn giao hàng mới được phân công cho tài xế.");
                }

                if (!OrderStatusUtils.isPreparing(order.getTrangThai())) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Chỉ được chuyển đơn sau khi đã làm xong.");
                }

                if (!hasText(deliveryMaNV)) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_BAD_REQUEST, "Vui lòng chọn người giao hàng.");
                }

                if (OrderStatusUtils.isCompleted(order.getTrangThai()) || OrderStatusUtils.isCancelled(order.getTrangThai())) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Đơn này đã được xử lý.");
                }

                String assignedMaNV = trimToNull(order.getMaNV());
                if (assignedMaNV != null && !assignedMaNV.equalsIgnoreCase(currentMaNV) && !managerRole) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Đơn này đã được nhân viên khác xử lý.");
                }

                if (assignedMaNV == null) {
                    order.setMaNV(currentMaNV);
                }

                order.setMaNVGiaoHang(deliveryMaNV);
                order.setTrangThai("DANG_GIAO");
                order.setDaNhanGiaoHang(Boolean.FALSE);
                em.merge(order);
                syncInvoiceWithOrder(em, order, order.getMaNV(), false);
                tx.commit();
                return OrderWorkflowResult.success("Đã xác nhận chuyển đơn " + order.getMaDatHang() + " cho người giao hàng.");
            }

            if ("complete".equals(action)) {
                if (hasText(order.getDiaChiGiaoHang())) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Đơn giao hàng phải do tài xế xác nhận hoàn tất.");
                }

                String assignedMaNV = trimToNull(order.getMaNV());
                if (assignedMaNV != null && !assignedMaNV.equalsIgnoreCase(currentMaNV) && !managerRole) {
                    tx.rollback();
                    return OrderWorkflowResult.error(HttpServletResponse.SC_CONFLICT, "Đơn này đang do nhân viên khác xử lý.");
                }

                if (assignedMaNV == null) {
                    order.setMaNV(currentMaNV);
                }

                order.setTrangThai("HOAN_THANH");
                em.merge(order);
                syncInvoiceWithOrder(em, order, currentMaNV, true);
                tx.commit();
                persistRevenueIfAny(order.getMaDatHang());
                return OrderWorkflowResult.success("Đã hoàn thành đơn " + order.getMaDatHang() + ".");
            }

            tx.rollback();
            return OrderWorkflowResult.error(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ.");
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return OrderWorkflowResult.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ex.getMessage() != null ? ex.getMessage() : "Không thể xử lý đơn hàng.");
        } finally {
            em.close();
        }
    }

    private void syncInvoiceWithOrder(EntityManager em, DatHang order, String maNV, boolean markCompleted) {
        HoaDon invoice = findInvoiceByOrderId(em, order != null ? order.getMaDatHang() : null);
        if (invoice == null) {
            return;
        }

        if (trimToNull(invoice.getMaDatHang()) == null && order != null) {
            invoice.setMaDatHang(order.getMaDatHang());
        }

        invoice.setMaNV(trimToNull(maNV));
        if (order != null) {
            invoice.setMaNVGiaoHang(trimToNull(order.getMaNVGiaoHang()));
            invoice.setDaNhanGiaoHang(order.getDaNhanGiaoHang());
        }
        if (markCompleted) {
            invoice.setTrangThai("HOAN_THANH");
        }
        em.merge(invoice);
    }

    private void persistRevenueIfAny(String maDatHang) {
        HoaDon invoice = hoaDonDAO.findByMaDatHang(maDatHang);
        if (invoice != null) {
            doanhThuDAO.upsertForHoaDon(invoice);
        }
    }

    private HoaDon findInvoiceByOrderId(EntityManager em, String maDatHang) {
        String normalizedMaDatHang = trimToNull(maDatHang);
        if (normalizedMaDatHang == null) {
            return null;
        }

        List<HoaDon> invoices = em.createQuery(
                        "SELECT h FROM HoaDon h WHERE h.maDatHang = :maDatHang ORDER BY h.maHD DESC",
                        HoaDon.class
                )
                .setParameter("maDatHang", normalizedMaDatHang)
                .setMaxResults(1)
                .getResultList();
        return invoices.isEmpty() ? null : invoices.get(0);
    }

    private void writeSummary(HttpServletResponse response, List<DatHang> orders) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);

        StringBuilder json = new StringBuilder(512);
        json.append('{');
        long openCount = 0;
        if (orders != null) {
            for (DatHang order : orders) {
                if (order != null && OrderStatusUtils.isActive(order.getTrangThai())) {
                    openCount++;
                }
            }
        }
        appendNumberField(json, "openCount", openCount);
        json.append(',');
        json.append("\"orders\":[");
        if (orders != null) {
            boolean first = true;
            for (DatHang order : orders) {
                if (order == null) {
                    continue;
                }

                if (!first) {
                    json.append(',');
                }
                first = false;
                appendOrder(json, order);
            }
        }
        json.append(']');
        json.append('}');

        response.getWriter().write(json.toString());
    }

    private void appendOrder(StringBuilder json, DatHang order) {
        json.append('{');
        boolean first = true;
        first = appendOrderStringField(json, "maDatHang", order.getMaDatHang(), first);
        first = appendOrderStringField(json, "maKH", order.getMaKH(), first);
        first = appendOrderStringField(json, "soDienThoai", order.getSoDienThoai(), first);
        first = appendOrderStringField(json, "diaChiGiaoHang", order.getDiaChiGiaoHang(), first);
        first = appendOrderStringField(json, "maNV", order.getMaNV(), first);
        first = appendOrderStringField(json, "maNVGiaoHang", order.getMaNVGiaoHang(), first);
        first = appendOrderBooleanField(json, "daNhanGiaoHang", Boolean.TRUE.equals(order.getDaNhanGiaoHang()), first);
        first = appendOrderStringField(json, "trangThai", order.getTrangThai(), first);
        first = appendOrderStringField(json, "ngayDat", order.getNgayDat() != null ? order.getNgayDat().toLocalDateTime().format(DATE_FORMAT) : null, first);
        first = appendOrderNumberField(json, "tongTien", order.getTongTien(), first);
        first = appendOrderBooleanField(json, "deliveryOrder", hasText(order.getDiaChiGiaoHang()), first);
        appendOrderBooleanField(json, "readyToConfirm", OrderStatusUtils.isDelivering(order.getTrangThai()), first);
        json.append('}');
    }

    private void writeOk(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);

        StringBuilder json = new StringBuilder(128);
        json.append('{');
        appendBooleanField(json, "success", true);
        appendStringField(json, "message", message);
        json.append('}');
        response.getWriter().write(json.toString());
    }

    private void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);

        StringBuilder json = new StringBuilder(128);
        json.append('{');
        appendBooleanField(json, "success", false);
        appendStringField(json, "message", message);
        json.append('}');
        response.getWriter().write(json.toString());
    }

    private void appendStringField(StringBuilder json, String key, String value) {
        appendCommaIfNeeded(json);
        json.append('"').append(escapeJson(key)).append("\":");
        if (value == null) {
            json.append("null");
        } else {
            json.append('"').append(escapeJson(value)).append('"');
        }
    }

    private void appendNumberField(StringBuilder json, String key, java.math.BigDecimal value) {
        appendCommaIfNeeded(json);
        json.append('"').append(escapeJson(key)).append("\":");
        if (value == null) {
            json.append("0");
        } else {
            json.append(value.toPlainString());
        }
    }

    private void appendNumberField(StringBuilder json, String key, long value) {
        appendCommaIfNeeded(json);
        json.append('"').append(escapeJson(key)).append("\":").append(value);
    }

    private void appendBooleanField(StringBuilder json, String key, boolean value) {
        appendCommaIfNeeded(json);
        json.append('"').append(escapeJson(key)).append("\":").append(value);
    }

    private boolean appendOrderStringField(StringBuilder json, String key, String value, boolean first) {
        if (!first) {
            json.append(',');
        }

        json.append('"').append(escapeJson(key)).append("\":");
        if (value == null) {
            json.append("null");
        } else {
            json.append('"').append(escapeJson(value)).append('"');
        }
        return false;
    }

    private boolean appendOrderNumberField(StringBuilder json, String key, java.math.BigDecimal value, boolean first) {
        if (!first) {
            json.append(',');
        }

        json.append('"').append(escapeJson(key)).append("\":");
        if (value == null) {
            json.append("0");
        } else {
            json.append(value.toPlainString());
        }
        return false;
    }

    private boolean appendOrderBooleanField(StringBuilder json, String key, boolean value, boolean first) {
        if (!first) {
            json.append(',');
        }

        json.append('"').append(escapeJson(key)).append("\":").append(value);
        return false;
    }

    private void appendCommaIfNeeded(StringBuilder json) {
        if (json.length() > 1) {
            json.append(',');
        }
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private TaiKhoan resolveCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session != null ? session.getAttribute("user") : null;
        return user instanceof TaiKhoan ? (TaiKhoan) user : null;
    }

    private boolean isAuthorized(TaiKhoan user) {
        return user != null
                && (RoleUtils.isEmployeeRole(user.getVaiTro())
                || RoleUtils.isDeliveryRole(user.getVaiTro())
                || RoleUtils.isManagerRole(user.getVaiTro()));
    }

    private List<DatHang> resolveVisibleOrders(TaiKhoan currentUser, HttpSession session) {
        if (currentUser == null) {
            return java.util.Collections.emptyList();
        }

        if (RoleUtils.isDeliveryRole(currentUser.getVaiTro())) {
            return dao.findAssignedDeliveryOrders(resolveCurrentEmployeeCode(currentUser, session));
        }

        if (RoleUtils.isEmployeeRole(currentUser.getVaiTro())) {
            return dao.findVisibleOrdersForEmployee(resolveCurrentEmployeeCode(currentUser, session));
        }

        return dao.findActiveOrders();
    }

    private String resolveCurrentEmployeeCode(TaiKhoan currentUser, HttpSession session) {
        if (currentUser == null) {
            return null;
        }

        if (!RoleUtils.isEmployeeRole(currentUser.getVaiTro())
                && !RoleUtils.isManagerRole(currentUser.getVaiTro())
                && !RoleUtils.isDeliveryRole(currentUser.getVaiTro())) {
            return null;
        }

        String maNV = trimToNull(currentUser.getMaNV());
        if (maNV != null && new dao.NhanVienDAO().findActiveById(maNV) != null) {
            return maNV;
        }

        if (session != null) {
            String loginPhone = trimToNull((String) session.getAttribute("loginPhone"));
            if (loginPhone != null) {
                entity.NhanVien nhanVien = new dao.NhanVienDAO().findByPhone(loginPhone);
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

    private String resolveActiveOrderStatus(DatHang order) {
        return "DANG_LAM";
    }

    private String normalizeAction(String action) {
        String normalized = trimToNull(action);
        if (normalized == null) {
            return null;
        }

        if ("update-status".equalsIgnoreCase(normalized)) {
            return "complete";
        }

        if ("assign".equalsIgnoreCase(normalized) || "claim".equalsIgnoreCase(normalized)) {
            return "claim";
        }

        if ("assign-delivery".equalsIgnoreCase(normalized)) {
            return "assign-delivery";
        }

        if ("complete".equalsIgnoreCase(normalized)) {
            return "complete";
        }

        return null;
    }

    private String loginPathForRequest(HttpServletRequest request) {
        return "/login";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static final class OrderWorkflowResult {
        private final boolean success;
        private final int statusCode;
        private final String message;

        private OrderWorkflowResult(boolean success, int statusCode, String message) {
            this.success = success;
            this.statusCode = statusCode;
            this.message = message;
        }

        private static OrderWorkflowResult success(String message) {
            return new OrderWorkflowResult(true, HttpServletResponse.SC_OK, message);
        }

        private static OrderWorkflowResult error(int statusCode, String message) {
            return new OrderWorkflowResult(false, statusCode, message);
        }
    }
}
