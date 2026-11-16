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
  <title>Quản lý giỏ hàng</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>Quản Lý Giỏ Hàng</h1>
      <div class="user-box">
        Xin chào:
        <b><%= user != null ? user.getTenDangNhap() : "" %></b>
        | Vai trò:
        <b><%= user != null ? user.getVaiTro() : "" %></b>
      </div>
    </div>

    <div class="content-box">
      <div class="page-header">
        <h2>Danh sách giỏ hàng</h2>
        <a class="btn btn-add"
           href="${pageContext.request.contextPath}/giohang?action=add">
          + Thêm giỏ hàng
        </a>
      </div>

      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
          <tr>
            <th>Mã giỏ hàng</th>
            <th>Mã khách hàng</th>
            <th>Ngày tạo</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <c:choose>
            <c:when test="${empty dsGioHang}">
              <tr>
                <td colspan="4" class="text-center">Chưa có giỏ hàng nào</td>
              </tr>
            </c:when>

            <c:otherwise>
              <c:forEach var="gh" items="${dsGioHang}">
                <tr>
                  <td>${gh.maGioHang}</td>
                  <td>${gh.maKH}</td>
                  <td>${gh.ngayTao}</td>
                  <td class="action-cell text-center">
                    <a class="btn-action btn-edit"
                       href="${pageContext.request.contextPath}/giohang?action=edit&id=${gh.maGioHang}">
                      Sửa
                    </a>

                    <a class="btn-action btn-delete"
                       href="${pageContext.request.contextPath}/giohang?action=delete&id=${gh.maGioHang}"
                       onclick="return confirm('Bạn có chắc muốn xóa giỏ hàng ${gh.maGioHang} không?');">
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