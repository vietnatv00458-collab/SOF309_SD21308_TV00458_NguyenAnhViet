<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String shopUrl = (String) request.getAttribute("shopUrl");
    if (shopUrl == null || shopUrl.trim().isEmpty()) {
        shopUrl = request.getContextPath() + "/customer-shop";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang khách hàng - Quán Cafe</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
    <meta http-equiv="refresh" content="2;url=<%= shopUrl %>">
</head>
<body class="customer-portal-page">
<div class="customer-portal">
    <div class="customer-portal-box">
        <p class="portal-label">Đăng nhập thành công</p>
        <h1>Chuyển đến trang mua hàng</h1>
        <p class="portal-text">
            Hệ thống đang đưa bạn sang website bán cafe để chọn và đặt hàng.
        </p>
        <p class="portal-url"><%= shopUrl %></p>
        <div class="portal-actions">
            <a class="btn btn-primary" href="<%= shopUrl %>">Vào ngay</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
        </div>
    </div>
</div>
</body>
</html>
