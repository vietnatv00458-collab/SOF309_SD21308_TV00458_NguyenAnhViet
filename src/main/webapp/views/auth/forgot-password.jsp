<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu - Quản Lý Quán Cafe</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=151">
</head>
<body class="login-page" style="background: #f5e6d3 url('${pageContext.request.contextPath}/images/auth-background.png?v=151') center top / cover no-repeat !important;">

<div class="login-container">
    <div class="login-box forgot-box">
        <div class="auth-logo">
            <img src="${pageContext.request.contextPath}/images/logo-premium-final.png" alt="Premium Coffee">
        </div>
        <h2>QUÊN MẬT KHẨU</h2>
        <p class="sub-title">Nhập email, nhận mã xác thực và đặt lại mật khẩu ngay</p>

        <div class="reset-steps" aria-label="Các bước khôi phục mật khẩu">
            <div class="reset-step is-active">
                <span class="reset-step-index">1</span>
                <span class="reset-step-text">Nhập email</span>
            </div>
            <div class="reset-step ${otpSent != null && otpSent ? 'is-active' : ''}">
                <span class="reset-step-index">2</span>
                <span class="reset-step-text">Xác thực OTP</span>
            </div>
            <div class="reset-step">
                <span class="reset-step-index">3</span>
                <span class="reset-step-text">Đặt mật khẩu mới</span>
            </div>
        </div>

        <% String error = (String) request.getAttribute("error"); %>
        <% String message = (String) request.getAttribute("message"); %>
        <% if (error != null) { %>
        <div class="error-message"><%= error %></div>
        <% } %>
        <% if (message != null) { %>
        <div class="success-message"><%= message %></div>
        <% } %>

        <form id="forgotPasswordSendForm" class="login-form" method="post" action="${pageContext.request.contextPath}/forgot-password">
            <input type="hidden" name="action" value="send">
            <div class="form-group-login">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" value="${email}" required autocomplete="email">
            </div>
            <p class="helper-text">Mã OTP sẽ được gửi đến email đã đăng ký trong hệ thống.</p>

            <div class="button-group">
                <button type="submit" class="btn-login">Gửi mã xác thực</button>
                <a href="${pageContext.request.contextPath}/login" class="btn-register">Quay lại đăng nhập</a>
            </div>
        </form>

        <%
            Boolean otpSent = (Boolean) request.getAttribute("otpSent");
            if (otpSent != null && otpSent) {
        %>
        <div class="otp-panel">
            <div class="otp-panel-head">
                <h3>Nhập mã xác thực</h3>
                <p class="helper-text compact">Mã có hiệu lực trong 5 phút.</p>
            </div>
            <form class="login-form" method="post" action="${pageContext.request.contextPath}/forgot-password">
                <input type="hidden" name="action" value="reset">
                <div class="form-group-login">
                    <label for="otp">Mã OTP</label>
                    <input type="text" id="otp" name="otp" inputmode="numeric" maxlength="6" required>
                </div>
                <div class="form-group-login">
                    <label for="newPassword">Mật khẩu mới</label>
                    <input type="password" id="newPassword" name="newPassword" required autocomplete="new-password">
                </div>
                <div class="form-group-login">
                    <label for="confirmPassword">Nhập lại mật khẩu mới</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required autocomplete="new-password">
                </div>
                <div class="button-group">
                    <button type="submit" class="btn-login">Đặt lại mật khẩu</button>
                    <button type="submit" form="forgotPasswordSendForm" class="btn-register">Gửi lại mã</button>
                </div>
            </form>
        </div>
        <% } %>
    </div>
</div>

</body>
</html>
