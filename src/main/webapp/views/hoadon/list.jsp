<%@ page import="entity.TaiKhoan,utils.RoleUtils" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%
    TaiKhoan user = (TaiKhoan) session.getAttribute("user");
    boolean employeeWorkView = Boolean.TRUE.equals(request.getAttribute("employeeWorkView"));
    boolean deliveryView = Boolean.TRUE.equals(request.getAttribute("deliveryView"));
    String currentEmployeeCode = request.getAttribute("currentEmployeeCode") != null
            ? String.valueOf(request.getAttribute("currentEmployeeCode"))
            : "";
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý hóa đơn</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=147">
    <style>
        .work-order-section {
            margin-top: 20px;
        }

        .work-order-table {
            min-width: 0;
            table-layout: fixed;
            width: 100%;
        }

        .work-order-status {
            display: inline-flex;
            align-items: center;
            padding: 6px 12px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
            white-space: nowrap;
        }

        .work-order-preparing {
            background: #e8f3ff;
            color: #1d4ed8;
        }

        .work-order-delivering {
            background: #e7f8ee;
            color: #047857;
        }

        .work-order-completed {
            background: #dcfce7;
            color: #166534;
        }

        .work-order-cancelled {
            background: #fee2e2;
            color: #b91c1c;
        }

        .work-order-default {
            background: #f3ece3;
            color: #7a4d2e;
        }

        .work-order-action-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 36px;
            padding: 0 14px;
            border: 0;
            border-radius: 8px;
            background: #8b5a38;
            color: #fff;
            font-family: inherit;
            font-size: 13px;
            font-weight: 700;
            cursor: pointer;
            box-shadow: 0 6px 14px rgba(139, 90, 56, 0.15);
        }

        .work-order-action-btn:hover {
            background: #70442a;
        }

        .work-order-action-btn[disabled] {
            opacity: 0.55;
            cursor: not-allowed;
        }

        .work-order-empty-row td {
            text-align: center;
            color: #7a6b5c;
            padding: 24px 14px;
        }

        .work-order-delivery-select {
            min-width: 180px;
            min-height: 36px;
            border: 1px solid #d6c4b4;
            border-radius: 8px;
            background: #fff;
            padding: 0 10px;
        }

        .work-order-table th,
        .work-order-table td {
            padding: 10px 8px;
            font-size: 12px;
            vertical-align: middle;
            word-break: break-word;
        }

        .work-order-table th:nth-child(1),
        .work-order-table td:nth-child(1) {
            width: 82px;
        }

        .work-order-table th:nth-child(2),
        .work-order-table td:nth-child(2) {
            width: 88px;
        }

        .work-order-table th:nth-child(3),
        .work-order-table td:nth-child(3) {
            width: 96px;
        }

        .work-order-table th:nth-child(4),
        .work-order-table td:nth-child(4) {
            width: 96px;
        }

        .work-order-table th:nth-child(5),
        .work-order-table td:nth-child(5) {
            width: 180px;
        }

        .work-order-table th:nth-child(6),
        .work-order-table td:nth-child(6) {
            width: 110px;
        }

        .work-order-table th:nth-child(7),
        .work-order-table td:nth-child(7) {
            width: 92px;
        }

        .work-order-table th:nth-child(8),
        .work-order-table td:nth-child(8) {
            width: 92px;
        }

        .work-order-table th:nth-child(9),
        .work-order-table td:nth-child(9) {
            width: 92px;
        }

        .work-order-table th:nth-child(10),
        .work-order-table td:nth-child(10) {
            width: 150px;
        }

        @media (max-width: 1280px) {
            .work-order-table {
                min-width: 0;
            }

            .work-order-table th,
            .work-order-table td {
                padding: 8px 6px;
                font-size: 11px;
            }
        }
    </style>
</head>
<body>

<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>Quản Lý Hóa Đơn</h1>
            <div class="user-box">
                Xin chào:
                <b><%= user != null ? user.getTenDangNhap() : "" %></b>
                | Vai trò:
                <b><%= RoleUtils.displayRole(user != null ? user.getVaiTro() : null) %></b>
            </div>
        </div>

        <div class="content-box">
            <c:choose>
                <c:when test="${employeeWorkView}">
                    <div class="page-header">
                        <h2>Thanh toán / làm đơn</h2>
                        <button id="workOrderRefresh" class="btn btn-add" type="button">Làm mới</button>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <template id="deliveryStaffOptions">
                        <option value="">-- Chọn nhân viên giao hàng --</option>
                        <c:forEach var="shipper" items="${deliveryEmployees}">
                            <option value="${shipper.maNV}">
                                <c:out value="${shipper.maNV}"/> - <c:out value="${shipper.hoTen}"/>
                            </option>
                        </c:forEach>
                    </template>

                    <div class="table-wrapper work-order-section">
                        <table class="data-table work-order-table">
                            <thead>
                            <tr>
                                <th>Mã HD</th>
                                <th>Mã đơn</th>
                                <th>Mã KH</th>
                                <th>SĐT</th>
                                <th>Địa chỉ / nhận hàng</th>
                                <th>Ngày lập</th>
                                <th>Trạng thái</th>
                                <th>NV xử lý</th>
                                <th>NV giao</th>
                                <th class="text-center">Hành động</th>
                            </tr>
                            </thead>
                            <tbody id="workOrderBody">
                            <c:choose>
                                <c:when test="${empty dsHoaDon}">
                                    <tr class="work-order-empty-row">
                                        <td colspan="10">Chưa có hóa đơn nào</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="hd" items="${dsHoaDon}">
                                        <c:set var="statusClass" value="work-order-default"/>
                                        <c:set var="actionLabel" value=""/>
                                        <c:set var="actionType" value=""/>
                                        <c:set var="assignedMaNV" value="${empty hd.maNV ? '' : hd.maNV}"/>
                                        <c:set var="assignedDeliveryMaNV" value="${empty hd.maNVGiaoHang ? '' : hd.maNVGiaoHang}"/>
                                        <c:set var="deliveryAccepted" value="${not empty hd.daNhanGiaoHang and hd.daNhanGiaoHang}"/>
                                        <c:set var="deliveryOrder" value="${not empty hd.diaChiGiaoHang}"/>
                                        <c:set var="assignedToCurrent" value="${not empty assignedMaNV and not empty currentEmployeeCode and assignedMaNV eq currentEmployeeCode}"/>
                                        <c:set var="deliveryToCurrent" value="${not empty assignedDeliveryMaNV and not empty currentEmployeeCode and assignedDeliveryMaNV eq currentEmployeeCode}"/>

                                        <c:choose>
                                            <c:when test="${hd.trangThai eq 'CHO_NHAN' or hd.trangThai eq 'Đang làm đơn' or hd.trangThai eq 'Đang xử lý' or hd.trangThai eq 'Đang chuẩn bị'}">
                                                <c:set var="statusClass" value="work-order-preparing"/>
                                                <c:choose>
                                                    <c:when test="${empty assignedMaNV and not deliveryView}">
                                                        <c:set var="actionLabel" value="Nhận đơn"/>
                                                        <c:set var="actionType" value="claim"/>
                                                    </c:when>
                                                    <c:when test="${assignedToCurrent and not deliveryOrder and not deliveryView}">
                                                        <c:set var="actionLabel" value="Hoàn thành"/>
                                                        <c:set var="actionType" value="complete"/>
                                                    </c:when>
                                                    <c:when test="${assignedToCurrent and deliveryOrder and not deliveryView and empty assignedDeliveryMaNV}">
                                                        <c:set var="actionLabel" value="Hoàn thành"/>
                                                        <c:set var="actionType" value="complete"/>
                                                    </c:when>
                                                    <c:when test="${assignedToCurrent and deliveryOrder and not deliveryView and not empty assignedDeliveryMaNV}">
                                                        <c:set var="actionLabel" value="Đã chuyển giao"/>
                                                    </c:when>
                                                </c:choose>
                                            </c:when>
                                            <c:when test="${hd.trangThai eq 'DANG_LAM' or hd.trangThai eq 'Đang làm'}">
                                                <c:set var="statusClass" value="work-order-preparing"/>
                                                <c:choose>
                                                    <c:when test="${deliveryView and deliveryToCurrent and not deliveryAccepted}">
                                                        <c:set var="actionLabel" value="Xác nhận nhận đơn"/>
                                                        <c:set var="actionType" value="claim"/>
                                                    </c:when>
                                                    <c:when test="${deliveryView and deliveryToCurrent and deliveryAccepted}">
                                                        <c:set var="actionLabel" value="Hoàn thành giao hàng"/>
                                                        <c:set var="actionType" value="complete"/>
                                                    </c:when>
                                                    <c:when test="${not deliveryView and assignedToCurrent and not deliveryOrder}">
                                                        <c:set var="actionLabel" value="Hoàn thành"/>
                                                        <c:set var="actionType" value="complete"/>
                                                    </c:when>
                                                    <c:when test="${not deliveryView and assignedToCurrent and deliveryOrder and empty assignedDeliveryMaNV}">
                                                        <c:set var="actionLabel" value="Hoàn thành"/>
                                                        <c:set var="actionType" value="complete"/>
                                                    </c:when>
                                                    <c:when test="${not deliveryView and assignedToCurrent and deliveryOrder and not empty assignedDeliveryMaNV}">
                                                        <c:set var="actionLabel" value="Đã chuyển giao"/>
                                                    </c:when>
                                                </c:choose>
                                            </c:when>
                                            <c:when test="${hd.trangThai eq 'DANG_GIAO' or hd.trangThai eq 'Đang giao hàng' or hd.trangThai eq 'Đang giao'}">
                                                <c:set var="statusClass" value="work-order-delivering"/>
                                                <c:choose>
                                                    <c:when test="${deliveryView and deliveryToCurrent and not deliveryAccepted}">
                                                        <c:set var="actionLabel" value="Xác nhận nhận đơn"/>
                                                        <c:set var="actionType" value="claim"/>
                                                    </c:when>
                                                    <c:when test="${deliveryView and deliveryToCurrent and deliveryAccepted}">
                                                        <c:set var="actionLabel" value="Hoàn thành giao hàng"/>
                                                        <c:set var="actionType" value="complete"/>
                                                    </c:when>
                                                    <c:when test="${not deliveryView and assignedToCurrent}">
                                                        <c:set var="actionLabel" value="Đang giao"/>
                                                    </c:when>
                                                </c:choose>
                                            </c:when>
                                            <c:when test="${hd.trangThai eq 'HOAN_THANH' or hd.trangThai eq 'Hoàn thành'}">
                                                <c:set var="statusClass" value="work-order-completed"/>
                                            </c:when>
                                            <c:when test="${hd.trangThai eq 'Đã hủy'}">
                                                <c:set var="statusClass" value="work-order-cancelled"/>
                                            </c:when>
                                        </c:choose>

                                        <tr data-hd-id="${hd.maHD}" data-ma-dat-hang="${hd.maDatHang}" data-delivery-order="${deliveryOrder}">
                                            <td><c:out value="${hd.maHD}"/></td>
                                            <td><c:out value="${hd.maDatHang}"/></td>
                                            <td><c:out value="${hd.maKH}"/></td>
                                            <td><c:out value="${hd.soDienThoai}"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty hd.diaChiGiaoHang}">
                                                        Mua tại quán
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:out value="${hd.diaChiGiaoHang}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:if test="${not empty hd.ngayLap}">
                                                    <fmt:formatDate value="${hd.ngayLap}" pattern="dd/MM/yyyy HH:mm"/>
                                                </c:if>
                                            </td>
                                            <td>
                                                <span class="work-order-status ${statusClass}">
                                                    <c:choose>
                                                        <c:when test="${hd.trangThai eq 'CHO_NHAN' or hd.trangThai eq 'Đang làm đơn' or hd.trangThai eq 'Đang xử lý' or hd.trangThai eq 'Đang chuẩn bị'}">
                                                            Chờ nhận
                                                        </c:when>
                                                        <c:when test="${hd.trangThai eq 'DANG_LAM' or hd.trangThai eq 'Đang làm'}">
                                                            Đang làm
                                                        </c:when>
                                                        <c:when test="${hd.trangThai eq 'DANG_GIAO' or hd.trangThai eq 'Đang giao hàng' or hd.trangThai eq 'Đang giao'}">
                                                            Đang giao
                                                        </c:when>
                                                        <c:when test="${hd.trangThai eq 'HOAN_THANH' or hd.trangThai eq 'Hoàn thành'}">
                                                            Hoàn thành
                                                        </c:when>
                                                        <c:when test="${hd.trangThai eq 'Đã hủy'}">
                                                            Đã hủy
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:out value="${hd.trangThai}"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty assignedMaNV}">
                                                        Chưa nhận
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:out value="${assignedMaNV}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty assignedDeliveryMaNV}">
                                                        -
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:out value="${assignedDeliveryMaNV}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${actionType eq 'complete' and deliveryOrder and not deliveryView and assignedToCurrent and empty assignedDeliveryMaNV}">
                                                        <div style="display:flex;gap:8px;align-items:center;justify-content:center;flex-wrap:wrap;">
                                                            <select class="work-order-delivery-select" data-hd-id="${hd.maHD}">
                                                                <option value="">-- Chọn nhân viên giao hàng --</option>
                                                                <c:forEach var="shipper" items="${deliveryEmployees}">
                                                                    <option value="${shipper.maNV}">
                                                                        <c:out value="${shipper.maNV}"/> - <c:out value="${shipper.hoTen}"/>
                                                                    </option>
                                                                </c:forEach>
                                                            </select>
                                                            <button type="button"
                                                                    class="work-order-action-btn"
                                                                    data-hd-id="${hd.maHD}"
                                                                    data-action="${actionType}">
                                                                <c:out value="${actionLabel}"/>
                                                            </button>
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${actionType eq 'claim' or actionType eq 'complete'}">
                                                        <button type="button"
                                                                class="work-order-action-btn"
                                                                data-hd-id="${hd.maHD}"
                                                                data-action="${actionType}">
                                                            <c:out value="${actionLabel}"/>
                                                        </button>
                                                    </c:when>
                                                    <c:when test="${empty actionLabel and assignedToCurrent and deliveryOrder and not empty assignedDeliveryMaNV and not deliveryView}">
                                                        <button type="button" class="work-order-action-btn" disabled>
                                                            Đã chuyển giao
                                                        </button>
                                                    </c:when>
                                                    <c:when test="${empty actionLabel and assignedToCurrent and not deliveryOrder and not deliveryView}">
                                                        <button type="button" class="work-order-action-btn" disabled>
                                                            Đã nhận bởi <c:out value="${assignedMaNV}"/>
                                                        </button>
                                                    </c:when>
                                                    <c:when test="${deliveryView and deliveryToCurrent and not deliveryAccepted}">
                                                        <button type="button"
                                                                class="work-order-action-btn"
                                                                data-hd-id="${hd.maHD}"
                                                                data-action="claim">
                                                            Xác nhận nhận đơn
                                                        </button>
                                                    </c:when>
                                                    <c:when test="${deliveryView and deliveryToCurrent and deliveryAccepted}">
                                                        <button type="button"
                                                                class="work-order-action-btn"
                                                                data-hd-id="${hd.maHD}"
                                                                data-action="complete">
                                                            Hoàn thành giao hàng
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button type="button" class="work-order-action-btn" disabled>-</button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="page-header">
                        <h2>Danh sách hóa đơn</h2>
                        <c:if test="${canManageHoaDon}">
                            <a class="btn btn-add" href="${pageContext.request.contextPath}/hoadon?action=add">+ Thêm hóa đơn</a>
                        </c:if>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <div class="table-wrapper">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Mã HD</th>
                                <th>Mã KH</th>
                                <th>Mã đơn</th>
                                <th>Mã NV</th>
                                <th>Mã giao</th>
                                <th>Mã KM</th>
                                <th>Ngày lập</th>
                                <th>Tổng tiền</th>
                                <th>Giảm giá</th>
                                <th>Thành tiền</th>
                                <th>Trạng thái</th>
                                <th class="text-center">Hành động</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${empty dsHoaDon}">
                                    <tr>
                                        <td colspan="12" class="text-center">Chưa có hóa đơn nào</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="hd" items="${dsHoaDon}">
                                        <c:set var="lockedHoaDon" value="${not empty lockedHoaDonMap and lockedHoaDonMap[hd.maHD]}"/>
                                        <tr>
                                            <td><c:out value="${hd.maHD}"/></td>
                                            <td><c:out value="${hd.maKH}"/></td>
                                            <td><c:out value="${hd.maDatHang}"/></td>
                                            <td><c:out value="${hd.maNV}"/></td>
                                            <td><c:out value="${hd.maNVGiaoHang}"/></td>
                                            <td><c:out value="${hd.maKM}"/></td>
                                            <td><c:out value="${hd.ngayLap}"/></td>
                                            <td><c:out value="${hd.tongTien}"/></td>
                                            <td><c:out value="${hd.giamGia}"/></td>
                                            <td><c:out value="${hd.thanhTien}"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${lockedHoaDon}">
                                                        Hoàn thành
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:choose>
                                                            <c:when test="${hd.trangThai eq 'CHO_NHAN'}">Chờ nhận</c:when>
                                                            <c:when test="${hd.trangThai eq 'DANG_LAM'}">Đang làm</c:when>
                                                            <c:when test="${hd.trangThai eq 'DANG_GIAO'}">Đang giao</c:when>
                                                            <c:when test="${hd.trangThai eq 'HOAN_THANH'}">Hoàn thành</c:when>
                                                            <c:otherwise><c:out value="${hd.trangThai}"/></c:otherwise>
                                                        </c:choose>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="action-cell text-center">
                                                <c:choose>
                                                    <c:when test="${lockedHoaDon or not canManageHoaDon}">
                                                        -
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a class="btn-action btn-edit" href="${pageContext.request.contextPath}/hoadon?action=edit&id=${hd.maHD}">Sửa</a>
                                                        <a class="btn-action btn-delete"
                                                           href="${pageContext.request.contextPath}/hoadon?action=delete&id=${hd.maHD}"
                                                           onclick="return confirm('Bạn có chắc muốn xóa hóa đơn ${hd.maHD} không?');">
                                                            Xóa
                                                        </a>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<c:if test="${employeeWorkView}">
    <script>
        (function () {
            const API_URL = '<%= request.getContextPath() %>/hoadon';
            const workOrderBody = document.getElementById('workOrderBody');
            const workOrderRefresh = document.getElementById('workOrderRefresh');

            function trimValue(value) {
                return String(value == null ? '' : value).trim();
            }

            async function submitAction(hdId, action, deliveryMaNV) {
                const payload = new URLSearchParams();
                payload.set('action', action);
                payload.set('id', hdId);
                if (deliveryMaNV) {
                    payload.set('deliveryMaNV', deliveryMaNV);
                }

                const response = await fetch(API_URL, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                    },
                    body: payload.toString()
                });

                if (response.redirected && response.url) {
                    window.location.href = response.url;
                    return;
                }

                const text = await response.text();
                if (!response.ok) {
                    alert(text || 'Không thể xử lý hóa đơn.');
                    return;
                }

                window.location.reload();
            }

            if (workOrderBody) {
                workOrderBody.querySelectorAll('button[data-hd-id]').forEach(function (button) {
                    if (button.disabled) {
                        return;
                    }

                    button.addEventListener('click', function () {
                        const hdId = trimValue(button.dataset.hdId);
                        const action = trimValue(button.dataset.action);
                        if (!hdId || !action) {
                            return;
                        }

                        if (action === 'complete') {
                            const select = workOrderBody.querySelector('.work-order-delivery-select[data-hd-id="' + CSS.escape(hdId) + '"]');
                            if (select && select.value) {
                                submitAction(hdId, action, trimValue(select.value));
                                return;
                            }

                            const row = button.closest('tr');
                            const deliveryOrder = row && row.dataset ? String(row.dataset.deliveryOrder || '').toLowerCase() === 'true' : false;
                            if (deliveryOrder && row && row.querySelector('.work-order-delivery-select')) {
                                alert('Vui lòng chọn nhân viên giao hàng.');
                                return;
                            }
                        }

                        submitAction(hdId, action, '');
                    });
                });
            }

            if (workOrderRefresh) {
                workOrderRefresh.addEventListener('click', function () {
                    window.location.reload();
                });
            }

            setInterval(function () {
                window.location.reload();
            }, 5000);
        })();
    </script>
</c:if>

</body>
</html>
