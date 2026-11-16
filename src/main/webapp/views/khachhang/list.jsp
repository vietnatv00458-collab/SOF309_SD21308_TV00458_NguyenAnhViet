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
    <title>Quản lý khách hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>Quản Lý Khách Hàng</h1>
            <div class="user-box">
                Xin chào:
                <b><%= user != null ? user.getTenDangNhap() : "" %></b>
                | Vai trò:
                <b><%= user != null ? user.getVaiTro() : "" %></b>
            </div>
        </div>

        <div class="content-box">
            <div class="page-header">
                <h2>Danh sách khách hàng</h2>
                <a class="btn btn-add"
                   href="${pageContext.request.contextPath}/khachhang?action=add">
                    + Thêm khách hàng
                </a>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Mã KH</th>
                        <th>Họ tên</th>
                        <th>SĐT</th>
                        <th>Email</th>
                        <th>Địa chỉ</th>
                        <th class="text-center">Hành động</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${empty dsKhachHang}">
                            <tr>
                                <td colspan="6" class="text-center">Chưa có khách hàng nào</td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach var="kh" items="${dsKhachHang}">
                                <tr>
                                    <td>${kh.maKH}</td>
                                    <td>${kh.hoTen}</td>
                                    <td>${kh.sdt}</td>
                                    <td>${kh.email}</td>
                                    <td>${kh.diaChi}</td>
                                    <td class="action-cell text-center">
                                        <a class="btn-action btn-edit"
                                           href="${pageContext.request.contextPath}/khachhang?action=edit&id=${kh.maKH}">
                                            Sửa
                                        </a>

                                        <a class="btn-action btn-delete"
                                           href="${pageContext.request.contextPath}/khachhang?action=delete&id=${kh.maKH}"
                                           onclick="return confirm('Bạn có chắc muốn xóa khách hàng ${kh.maKH} không?');">
                                            Xóa
                                        </a>
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
