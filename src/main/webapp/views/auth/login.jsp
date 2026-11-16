﻿﻿<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Quản Lý Quán Cafe</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=148">
</head>
<body class="login-page" style="background: #f5e6d3 url('${pageContext.request.contextPath}/images/auth-background.png?v=148') center top / cover no-repeat !important;">

<div class="login-container">
    <div class="login-box">
        <div class="auth-logo">
            <img src="${pageContext.request.contextPath}/images/logo-premium-final.png" alt="Premium Coffee">
        </div>
        <h2>ĐĂNG NHẬP HỆ THỐNG</h2>
        <p class="sub-title">Quản Lý Quán Cafe</p>

        <% String error = (String) request.getAttribute("error"); %>
        <% String message = (String) session.getAttribute("message"); %>
        <% if (error != null) { %>
        <div class="error-message"><%= error %></div>
        <% } %>
        <% if (message != null) { %>
        <div class="success-message"><%= message %></div>
        <% session.removeAttribute("message"); %>
        <% } %>

        <form class="login-form" method="post" action="${pageContext.request.contextPath}/login">
            <div class="form-group-login">
                <label for="loginId">Số điện thoại</label>
                <input type="text" id="loginId" name="loginId" required autocomplete="tel">
            </div>

            <div class="form-group-login">
                <label for="password">Mật khẩu</label>
                <input type="password" id="password" name="password" required>
            </div>

            <div class="button-group">
                <button type="submit" class="btn-login">Đăng nhập</button>
                <a href="${pageContext.request.contextPath}/register" class="btn-register">Đăng ký khách hàng</a>
            </div>
        </form>

        <div class="auth-links">
            <a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
        </div>
    </div>
</div>

</body>
</html>
