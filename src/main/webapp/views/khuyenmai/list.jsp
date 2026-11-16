<%@ page import="entity.TaiKhoan,utils.RoleUtils" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%
    TaiKhoan user = (TaiKhoan) session.getAttribute("user");
    boolean canManagePromotion = user != null && RoleUtils.isManagerRole(user.getVaiTro());
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý khuyến mãi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>Quản Lý Khuyến Mãi</h1>
            <div class="user-box">
                Xin chào:
                <b><%= user != null ? user.getTenDangNhap() : "" %></b>
                | Vai trò:
                <b><%= user != null ? user.getVaiTro() : "" %></b>
            </div>
        </div>

        <div class="content-box">
            <div class="page-header">
                <h2>Danh sách khuyến mãi</h2>
                <% if (canManagePromotion) { %>
                <a class="btn btn-add"
                   href="${pageContext.request.contextPath}/khuyenmai?action=add">
                    + Thêm khuyến mãi
                </a>
                <% } %>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Mã KM</th>
                        <th>Tên KM</th>
                        <th>Kiểu giảm</th>
                        <th>Giá trị giảm</th>
                        <th>Giảm tối đa</th>
                        <th>Ngày bắt đầu</th>
                        <th>Ngày kết thúc</th>
                        <th>Mô tả</th>
                        <% if (canManagePromotion) { %>
                        <th class="text-center">Hành động</th>
                        <% } %>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${empty dsKhuyenMai}">
                            <tr>
                                <td colspan="<%= canManagePromotion ? 9 : 8 %>" class="text-center">Chưa có khuyến mãi nào</td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach var="km" items="${dsKhuyenMai}">
                                <tr>
                                    <td>${km.maKM}</td>
                                    <td>${km.tenKM}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${km.loaiGiamGia == 'AMOUNT' || (empty km.loaiGiamGia && km.soTienGiam != null)}">
                                                Giảm bằng số tiền
                                            </c:when>
                                            <c:otherwise>Giảm theo phần trăm</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${km.soTienGiam != null}">
                                                ${km.soTienGiam} đ
                                            </c:when>
                                            <c:otherwise>
                                                ${km.phanTramGiam}%
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${km.mucGiamToiDa != null}">
                                                ${km.mucGiamToiDa} đ
                                            </c:when>
                                            <c:otherwise>Không giới hạn</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${km.ngayBatDau}</td>
                                    <td>${km.ngayKetThuc}</td>
                                    <td>${km.moTa}</td>
                                    <% if (canManagePromotion) { %>
                                    <td class="action-cell text-center">
                                        <a class="btn-action btn-edit"
                                           href="${pageContext.request.contextPath}/khuyenmai?action=edit&id=${km.maKM}">
                                            Sửa
                                        </a>

                                        <a class="btn-action btn-delete"
                                           href="${pageContext.request.contextPath}/khuyenmai?action=delete&id=${km.maKM}"
                                           onclick="return confirm('Bạn có chắc muốn xóa khuyến mãi ${km.maKM} không?');">
                                            Xóa
                                        </a>
                                    </td>
                                    <% } %>
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
