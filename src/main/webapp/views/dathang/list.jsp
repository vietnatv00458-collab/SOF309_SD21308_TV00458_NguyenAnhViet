<%@ page import="entity.TaiKhoan" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%
    TaiKhoan user = (TaiKhoan) session.getAttribute("user");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý đặt hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>Quản Lý Đặt Hàng</h1>
            <div class="user-box">
                Xin chào:
                <b><%= user != null ? user.getTenDangNhap() : "" %></b>
                | Vai trò:
                <b><%= user != null ? user.getVaiTro() : "" %></b>
            </div>
        </div>

        <div class="content-box">
            <div class="page-header">
                <h2>Danh sách đặt hàng</h2>
                <a class="btn btn-add"
                   href="${pageContext.request.contextPath}/dathang?action=add">
                    + Thêm đặt hàng
                </a>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Mã đặt hàng</th>
                        <th>Mã khách hàng</th>
                        <th>Mã NV xử lý</th>
                        <th>SĐT</th>
                        <th>Địa chỉ giao hàng</th>
                        <th>Ngày đặt</th>
                        <th>Trạng thái</th>
                        <th>Tổng tiền</th>
                        <th class="text-center">Hành động</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${empty dsDatHang}">
                            <tr>
                                <td colspan="9" class="text-center">Chưa có đơn đặt hàng nào</td>
                            </tr>
                        </c:when>

                    <c:otherwise>
                            <c:forEach var="dh" items="${dsDatHang}">
                                <c:set var="lockedDatHang"
                                       value="${not empty lockedDatHangMap and lockedDatHangMap[dh.maDatHang]}"/>
                                <tr>
                                    <td>${dh.maDatHang}</td>
                                    <td>${dh.maKH}</td>
                                    <td>${dh.maNV}</td>
                                    <td>${dh.soDienThoai}</td>
                                    <td>${dh.diaChiGiaoHang}</td>
                                    <td>${dh.ngayDat}</td>
                                    <td>${dh.trangThai}</td>
                                    <td>${dh.tongTien}</td>
                                    <td class="action-cell text-center">
                                        <c:choose>
                                            <c:when test="${dh.trangThai eq 'Hoàn thành'}">
                                                -
                                            </c:when>
                                            <c:when test="${lockedDatHang}">
                                                -
                                            </c:when>
                                            <c:otherwise>
                                                <a class="btn-action btn-edit"
                                                   href="${pageContext.request.contextPath}/dathang?action=edit&id=${dh.maDatHang}">
                                                    Sửa
                                                </a>

                                                <a class="btn-action btn-delete"
                                                   href="${pageContext.request.contextPath}/dathang?action=delete&id=${dh.maDatHang}"
                                                   onclick="return confirm('Bạn có chắc muốn xóa đơn đặt hàng ${dh.maDatHang} không?');">
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
        </div>
    </div>
</div>

</body>
</html>
