﻿﻿<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký khách hàng - Quản Lý Quán Cafe</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=148">
</head>
<body class="login-page" style="background: #f5e6d3 url('${pageContext.request.contextPath}/images/auth-background.png?v=148') center top / cover no-repeat !important;">

<div class="login-container">
    <div class="login-box register-box">
        <div class="auth-logo">
            <img src="${pageContext.request.contextPath}/images/logo-premium-final.png" alt="Premium Coffee">
        </div>
        <h2>ĐĂNG KÝ KHÁCH HÀNG</h2>
        <p class="sub-title">Thông tin sẽ được lưu vào cơ sở dữ liệu.</p>

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
        <div class="error-message"><%= error %></div>
        <% } %>

        <form class="login-form" method="post" action="${pageContext.request.contextPath}/register">
            <div class="form-group-login">
                <label for="hoTen">Họ tên</label>
                <input type="text" id="hoTen" name="hoTen" value="${param.hoTen}" required>
            </div>

            <div class="form-group-login">
                <label for="sdt">Số điện thoại</label>
                <input type="text" id="sdt" name="sdt" value="${param.sdt}" required>
            </div>

            <div class="form-group-login">
                <label for="matKhau">Mật khẩu</label>
                <input type="password" id="matKhau" name="matKhau" required minlength="6" autocomplete="new-password">
            </div>

            <div class="form-group-login">
                <label for="xacNhanMatKhau">Xác nhận mật khẩu</label>
                <input type="password" id="xacNhanMatKhau" name="xacNhanMatKhau" required minlength="6" autocomplete="new-password">
            </div>

            <div class="form-group-login">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" value="${param.email}">
            </div>

            <div class="form-group-login">
                <label for="diaChi">Địa chỉ</label>
                <input type="text" id="diaChi" name="diaChi" value="${param.diaChi}">
            </div>

            <div class="button-group">
                <button type="submit" class="btn-register">Đăng ký</button>
                <a href="${pageContext.request.contextPath}/login" class="btn-login">Quay lại</a>
            </div>
        </form>
    </div>
</div>

</body>
</html>
