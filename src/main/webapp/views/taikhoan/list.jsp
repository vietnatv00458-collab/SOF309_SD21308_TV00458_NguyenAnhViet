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
  <title>Quản lý tài khoản</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>Quản Lý Tài Khoản</h1>
      <div class="user-box">
        Xin chào:
        <b><%= user != null ? user.getTenDangNhap() : "" %></b>
        | Vai trò:
        <b><%= user != null ? user.getVaiTro() : "" %></b>
      </div>
    </div>

    <div class="content-box">
      <div class="page-header">
        <h2>Danh sách tài khoản</h2>
        <div style="display:flex; gap:10px; flex-wrap:wrap;">
          <a class="btn btn-add"
             href="${pageContext.request.contextPath}/taikhoan?action=add">
            + Thêm tài khoản
          </a>
          <a class="btn btn-add"
             href="${pageContext.request.contextPath}/taikhoan?action=add&kind=delivery">
            + Tạo TK giao hàng
          </a>
        </div>
      </div>

      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
          <tr>
            <th>Mã NV</th>
            <th>Tên đăng nhập</th>
            <th>Mật khẩu</th>
            <th>Vai trò</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <c:choose>
            <c:when test="${empty dsTaiKhoan}">
              <tr>
                <td colspan="5" class="text-center">Chưa có tài khoản nào</td>
              </tr>
            </c:when>

            <c:otherwise>
              <c:forEach var="tk" items="${dsTaiKhoan}">
                <tr>
                  <td>${tk.maNV}</td>
                  <td>${tk.tenDangNhap}</td>
                  <td>Đã mã hóa</td>
                  <td>
                    <c:choose>
                      <c:when test="${tk.vaiTro == 'Admin'}">Quản lý</c:when>
                      <c:when test="${tk.vaiTro == 'GiaoHang'}">Giao hàng</c:when>
                      <c:when test="${tk.vaiTro == 'KHACHHANG'}">Khách hàng</c:when>
                      <c:otherwise>Nhân viên</c:otherwise>
                    </c:choose>
                  </td>
                  <td class="action-cell text-center">
                    <a class="btn-action btn-edit"
                       href="${pageContext.request.contextPath}/taikhoan?action=edit&id=${tk.maNV}">
                      Sửa
                    </a>

                    <a class="btn-action btn-delete"
                       href="${pageContext.request.contextPath}/taikhoan?action=delete&id=${tk.maNV}"
                       onclick="return confirm('Bạn có chắc muốn xóa tài khoản ${tk.maNV} không?');">
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
