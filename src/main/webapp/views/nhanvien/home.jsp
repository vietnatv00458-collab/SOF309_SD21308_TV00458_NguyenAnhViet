<%@ page import="entity.TaiKhoan,utils.RoleUtils" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    TaiKhoan user = (TaiKhoan) request.getAttribute("user");
    String roleLabel = RoleUtils.displayRole(user != null ? user.getVaiTro() : null);
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Trang nhân viên</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=147">
</head>
<body>

<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>Trang nhân viên</h1>
            <div class="user-box">
                Xin chào:
                <b><%= user != null ? user.getTenDangNhap() : "" %></b>
                | Vai trò:
                <b><%= roleLabel %></b>
            </div>
        </div>

        <div class="content-box">
            <div class="page-header">
                <h2>Chức năng được phép</h2>
            </div>

            <div class="dashboard-cards">
                <div class="card">
                    <h3>Thanh toán / Hóa đơn</h3>
                    <p><a href="${pageContext.request.contextPath}/hoadon">Mở chức năng</a></p>
                </div>
                <div class="card">
                    <h3>Sản phẩm</h3>
                    <p><a href="${pageContext.request.contextPath}/sanpham">Mở chức năng</a></p>
                </div>
                <div class="card">
                    <h3>Khuyến mãi</h3>
                    <p><a href="${pageContext.request.contextPath}/khuyenmai">Mở chức năng</a></p>
                </div>
                <div class="card">
                    <h3>Đăng xuất</h3>
                    <p><a href="${pageContext.request.contextPath}/logout">Thoát tài khoản</a></p>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
