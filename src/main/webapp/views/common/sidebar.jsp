<%@ page import="entity.TaiKhoan,utils.RoleUtils" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
  TaiKhoan user = null;
  if (session != null) {
    user = (TaiKhoan) session.getAttribute("user");
  }

  String role = user != null ? user.getVaiTro() : null;
  boolean manager = RoleUtils.isManagerRole(role);
  boolean employee = RoleUtils.isEmployeeRole(role);
  boolean customer = RoleUtils.isCustomerRole(role);
%>

<style>
  .layout .sidebar {
    width: 250px !important;
    min-width: 250px !important;
    max-width: 250px !important;
    background: linear-gradient(180deg, #3a2114 0%, #2a160d 100%) !important;
    color: #fff8ea !important;
    padding: 24px 18px !important;
    flex: 0 0 250px !important;
    height: 100vh !important;
    overflow-y: auto !important;
    overflow-x: hidden !important;
    position: sticky !important;
    top: 0 !important;
    align-self: flex-start !important;
    z-index: 2 !important;
  }

  .layout .sidebar .sidebar-brand {
    display: flex !important;
    align-items: center !important;
    gap: 12px !important;
    margin-bottom: 28px !important;
    padding: 10px 8px 18px !important;
    border-bottom: 1px solid rgba(232, 195, 132, 0.22) !important;
  }

  .layout .sidebar .sidebar-brand img {
    width: 54px !important;
    min-width: 54px !important;
    max-width: 54px !important;
    height: 54px !important;
    min-height: 54px !important;
    max-height: 54px !important;
    border-radius: 50% !important;
    object-fit: cover !important;
    display: block !important;
    position: static !important;
    transform: none !important;
  }

  .layout .sidebar .sidebar-brand span,
  .layout .sidebar .sidebar-brand small {
    display: block !important;
  }
</style>

<div class="sidebar">
  <div class="sidebar-brand">
    <img src="${pageContext.request.contextPath}/images/logo-premium-final.png" alt="Premium Coffee">
    <div>
      <span>Premium</span>
      <small>Coffee</small>
    </div>
  </div>

  <ul>
    <li><a href="${pageContext.request.contextPath}/home">Trang chủ</a></li>

<% if (manager) { %>
    <li><a href="${pageContext.request.contextPath}/dashboard">Dashboard quản lý</a></li>
    <li><a href="${pageContext.request.contextPath}/taikhoan">Tài khoản</a></li>
    <li><a href="${pageContext.request.contextPath}/nhanvien">Nhân viên</a></li>
    <li><a href="${pageContext.request.contextPath}/khachhang">Khách hàng</a></li>
    <li><a href="${pageContext.request.contextPath}/sanpham">Sản phẩm</a></li>
    <li><a href="${pageContext.request.contextPath}/lichsugiasanpham">Lịch sử giá sản phẩm</a></li>
    <li><a href="${pageContext.request.contextPath}/khuyenmai">Khuyến mãi</a></li>
    <li><a href="${pageContext.request.contextPath}/hoadon">Hóa đơn</a></li>
    <li><a href="${pageContext.request.contextPath}/dashboard#doanhthu">Doanh thu</a></li>
    <li><a href="${pageContext.request.contextPath}/dathang">Đặt hàng</a></li>
    <li><a href="${pageContext.request.contextPath}/giohang">Giỏ hàng</a></li>
    <li><a href="${pageContext.request.contextPath}/chitiethoadon">Chi tiết hóa đơn</a></li>
    <li><a href="${pageContext.request.contextPath}/chitietgiohang">Chi tiết giỏ hàng</a></li>
    <% } else if (employee) { %>
    <li><a href="${pageContext.request.contextPath}/nhanvien-home">Trang nhân viên</a></li>
    <li><a href="${pageContext.request.contextPath}/hoadon">Thanh toán / Hóa đơn</a></li>
    <li><a href="${pageContext.request.contextPath}/sanpham">Sản phẩm</a></li>
    <li><a href="${pageContext.request.contextPath}/lichsugiasanpham">Lịch sử giá sản phẩm</a></li>
    <li><a href="${pageContext.request.contextPath}/khuyenmai">Khuyến mãi</a></li>
    <% } else if (RoleUtils.isDeliveryRole(role)) { %>
    <li><a href="${pageContext.request.contextPath}/giaohang-home">Trang giao hàng</a></li>
    <li><a href="${pageContext.request.contextPath}/hoadon">Hóa đơn</a></li>
    <% } else if (customer) { %>
    <li><a href="${pageContext.request.contextPath}/customer-shop">Cửa hàng</a></li>
    <% } %>

    <li><a href="${pageContext.request.contextPath}/logout">Đăng xuất</a></li>
  </ul>
</div>
