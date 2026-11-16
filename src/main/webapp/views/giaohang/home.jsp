<%@ page import="entity.TaiKhoan,utils.RoleUtils" %>
<%@ page import="entity.DatHang" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%
    TaiKhoan user = (TaiKhoan) request.getAttribute("user");
    String roleLabel = RoleUtils.displayRole(user != null ? user.getVaiTro() : null);
    String currentEmployeeCodeValue = (String) request.getAttribute("currentEmployeeCode");
    if (currentEmployeeCodeValue == null || currentEmployeeCodeValue.trim().isEmpty()) {
        currentEmployeeCodeValue = user != null && user.getMaNV() != null ? user.getMaNV().trim() : "";
    }
    String currentEmployeeCode = currentEmployeeCodeValue != null ? currentEmployeeCodeValue.trim() : "";
    java.util.List<DatHang> dsDonHang = (java.util.List<DatHang>) request.getAttribute("dsDonHangGiao");
    Integer soDonDangGiao = (Integer) request.getAttribute("soDonDangGiao");
    if (soDonDangGiao == null) {
        soDonDangGiao = dsDonHang != null ? dsDonHang.size() : 0;
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Trang giao hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=149">
    <style>
        .delivery-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 16px;
        }

        .delivery-summary {
            padding: 16px 18px;
            border: 1px solid #d9c2a4;
            border-radius: 10px;
            background: #fff8ef;
        }

        .delivery-summary h3 {
            margin: 0 0 6px;
            font-size: 18px;
        }

        .delivery-table {
            min-width: 980px;
        }

        .delivery-action-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 36px;
            padding: 0 14px;
            border: 0;
            border-radius: 8px;
            background: #1f7a4f;
            color: #fff;
            font: inherit;
            font-size: 13px;
            font-weight: 700;
            cursor: pointer;
        }

        .delivery-action-btn:hover {
            background: #16623f;
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            padding: 6px 12px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
            white-space: nowrap;
            background: #e7f8ee;
            color: #047857;
        }

        .delivery-empty-row td {
            text-align: center;
            padding: 24px 14px;
            color: #7a6b5c;
        }
    </style>
</head>
<body>

<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>Trang giao hàng</h1>
            <div class="user-box">
                Xin chào:
                <b><%= user != null ? user.getTenDangNhap() : "" %></b>
                | Vai trò:
                <b><%= roleLabel %></b>
            </div>
        </div>

        <div class="content-box">
            <div class="delivery-grid">
                <div class="delivery-summary">
                    <h3>Đơn đang chờ giao</h3>
                    <p>Nhận danh sách đơn đã được phân công và xác nhận khi giao xong.</p>
                    <strong id="deliveryCount"><%= soDonDangGiao %></strong>
                </div>

                <div class="table-wrapper">
                    <table class="data-table delivery-table">
                        <thead>
                        <tr>
                            <th>Mã đơn</th>
                            <th>Mã khách hàng</th>
                            <th>SĐT</th>
                            <th>Địa chỉ giao hàng</th>
                            <th>Ngày đặt</th>
                            <th>Trạng thái</th>
                            <th>Tổng tiền</th>
                            <th class="text-center">Hành động</th>
                        </tr>
                        </thead>
                        <tbody id="deliveryOrderBody">
                        <c:choose>
                            <c:when test="${empty dsDonHangGiao}">
                                <tr class="delivery-empty-row">
                                    <td colspan="8">Chưa có đơn giao nào được phân công</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="dh" items="${dsDonHangGiao}">
                                    <c:set var="deliveryAccepted" value="${not empty dh.daNhanGiaoHang and dh.daNhanGiaoHang}"/>
                                    <c:set var="actionLabel" value="${deliveryAccepted ? 'Xác nhận đã giao' : 'Nhận đơn'}"/>
                                    <c:set var="actionType" value="${deliveryAccepted ? 'complete' : 'claim'}"/>
                                    <tr data-order-id="${dh.maDatHang}">
                                        <td><c:out value="${dh.maDatHang}"/></td>
                                        <td><c:out value="${dh.maKH}"/></td>
                                        <td><c:out value="${dh.soDienThoai}"/></td>
                                        <td><c:out value="${dh.diaChiGiaoHang}"/></td>
                                        <td><c:if test="${not empty dh.ngayDat}"><fmt:formatDate value="${dh.ngayDat}" pattern="dd/MM/yyyy HH:mm"/></c:if></td>
                                        <td><span class="status-badge status-delivering">Đang giao</span></td>
                                        <td><fmt:formatNumber value="${empty dh.tongTien ? 0 : dh.tongTien}" type="number" groupingUsed="true" maxFractionDigits="0"/> đ</td>
                                        <td class="text-center">
                                            <button type="button"
                                                    class="delivery-action-btn"
                                                    data-order-id="${dh.maDatHang}"
                                                    data-action="${actionType}">
                                                <c:out value="${actionLabel}"/>
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

    <script>
    (function () {
        const ORDER_API_URL = '<%= request.getContextPath() %>/giaohang-donhang';
        const LOGIN_URL = '<%= request.getContextPath() %>/login';
        const deliveryOrderBody = document.getElementById('deliveryOrderBody');
        const deliveryCount = document.getElementById('deliveryCount');

        function trimValue(value) {
            return String(value == null ? '' : value).trim();
        }

        function escapeHtml(value) {
            return String(value == null ? '' : value)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        function formatMoney(value) {
            const amount = Number(value);
            if (!Number.isFinite(amount)) {
                return '0 đ';
            }
            return new Intl.NumberFormat('vi-VN').format(Math.round(amount)) + ' đ';
        }

        function renderOrders(orders) {
            if (!deliveryOrderBody) {
                return;
            }

            if (!Array.isArray(orders) || orders.length === 0) {
                deliveryOrderBody.innerHTML = '<tr class="delivery-empty-row"><td colspan="8">Chưa có đơn giao nào được phân công</td></tr>';
                if (deliveryCount) {
                    deliveryCount.textContent = '0';
                }
                return;
            }

            deliveryOrderBody.innerHTML = orders.map(function (order) {
                const orderId = trimValue(order && order.maDatHang);
                const customerId = trimValue(order && order.maKH);
                const phone = trimValue(order && order.soDienThoai);
                const address = trimValue(order && order.diaChiGiaoHang);
                const timeText = trimValue(order && order.ngayDat);
                const amountText = formatMoney(order && order.tongTien);
                const deliveryAccepted = !!(order && order.daNhanGiaoHang);
                const actionLabel = deliveryAccepted ? 'Xác nhận đã giao' : 'Nhận đơn';
                const actionType = deliveryAccepted ? 'complete' : 'claim';

                return [
                    '<tr data-order-id="', escapeHtml(orderId), '">',
                    '<td>', escapeHtml(orderId), '</td>',
                    '<td>', escapeHtml(customerId), '</td>',
                    '<td>', escapeHtml(phone), '</td>',
                    '<td>', escapeHtml(address), '</td>',
                    '<td>', escapeHtml(timeText), '</td>',
                    '<td><span class="status-badge status-delivering">Đang giao</span></td>',
                    '<td>', escapeHtml(amountText), '</td>',
                    '<td class="text-center">',
                    '<button type="button" class="delivery-action-btn" data-order-id="', escapeHtml(orderId), '" data-action="', escapeHtml(actionType), '">', escapeHtml(actionLabel), '</button>',
                    '</td>',
                    '</tr>'
                ].join('');
            }).join('');
            if (deliveryCount) {
                deliveryCount.textContent = String(orders.length);
            }
        }

        async function loadOrders() {
            const response = await fetch(ORDER_API_URL + '?action=summary', {
                cache: 'no-store',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });

            if (response.status === 401) {
                window.location.href = LOGIN_URL;
                return;
            }

            const data = await response.json().catch(function () { return null; });
            if (!response.ok || !data) {
                return;
            }

            renderOrders(Array.isArray(data.orders) ? data.orders : []);
        }

        async function submitComplete(orderId, button) {
            const action = button && button.dataset ? trimValue(button.dataset.action) : 'complete';
            const payload = new URLSearchParams();
            payload.set('action', action || 'complete');
            payload.set('id', trimValue(orderId));

            const originalText = button ? button.textContent : '';
            if (button) {
                button.disabled = true;
                button.textContent = action === 'claim' ? 'Đang nhận...' : 'Đang xác nhận...';
            }

            try {
                const response = await fetch(ORDER_API_URL, {
                    method: 'POST',
                    body: payload,
                    credentials: 'same-origin',
                    headers: { 'Accept': 'application/json' }
                });

                const data = await response.json().catch(function () { return null; });
                if (!response.ok || !data || data.success === false) {
                    throw new Error((data && data.message) || 'Không thể xử lý đơn.');
                }

                await loadOrders();
            } catch (error) {
                window.alert((error && error.message) || 'Không thể xử lý đơn.');
            } finally {
                if (button) {
                    button.disabled = false;
                    button.textContent = originalText || (action === 'claim' ? 'Nhận đơn' : 'Xác nhận đã giao');
                }
            }
        }

        if (deliveryOrderBody) {
            deliveryOrderBody.addEventListener('click', function (event) {
                const button = event.target.closest('button[data-order-id][data-action]');
                if (!button) {
                    return;
                }

                const action = trimValue(button.dataset.action);
                const promptText = action === 'claim'
                    ? 'Nhận đơn ' + trimValue(button.dataset.orderId) + ' về xử lý?'
                    : 'Xác nhận đơn ' + trimValue(button.dataset.orderId) + ' đã giao xong?';
                if (!window.confirm(promptText)) {
                    return;
                }

                submitComplete(button.dataset.orderId, button);
            });
        }

        loadOrders().catch(function () {
            // Giữ giao diện hiện tại nếu không tải được.
        });

        window.setInterval(function () {
            loadOrders().catch(function () {});
        }, 5000);
    }());
</script>

</body>
</html>
