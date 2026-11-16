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
  <title>Lịch sử giá sản phẩm</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=142">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>Lịch Sử Giá Sản Phẩm</h1>
      <div class="user-box">
        Xin chào:
        <b><%= user != null ? user.getTenDangNhap() : "" %></b>
        | Vai trò:
        <b><%= user != null ? user.getVaiTro() : "" %></b>
      </div>
    </div>

    <div class="content-box">
      <div class="page-header">
        <h2>Danh sách lịch sử giá</h2>
        <a class="btn btn-add"
           href="${pageContext.request.contextPath}/lichsugiasanpham?action=add">
          + Thêm lịch sử giá
        </a>
      </div>

      <form class="list-toolbar" method="get" action="${pageContext.request.contextPath}/lichsugiasanpham">
        <div class="list-search">
          <input
              type="search"
              name="keyword"
              value="${keyword}"
              placeholder="Tìm theo mã sản phẩm hoặc lý do..."
              aria-label="Tìm lịch sử giá">
          <button class="btn-icon-search" type="submit" aria-label="Tìm kiếm">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" stroke-width="2"></circle>
              <path d="M16 16l5 5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
            </svg>
          </button>
        </div>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/lichsugiasanpham">Xóa lọc</a>
      </form>

      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
          <tr>
            <th>Mã LS</th>
            <th>Mã SP</th>
            <th>Giá cũ</th>
            <th>Giá mới</th>
            <th>Ngày áp dụng</th>
            <th>Lý do</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${empty dsLichSuGia}">
              <tr>
                <td colspan="7" class="text-center">
                  <c:choose>
                    <c:when test="${empty keyword}">Chưa có lịch sử giá nào</c:when>
                    <c:otherwise>Không tìm thấy lịch sử giá phù hợp</c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:when>

            <c:otherwise>
              <c:forEach var="item" items="${dsLichSuGia}">
                <tr>
                  <td>${item.maLSGia}</td>
                  <td>${item.maSP}</td>
                  <td>${item.giaCuText}</td>
                  <td>${item.giaMoiText}</td>
                  <td>${item.ngayApDungText}</td>
                  <td>${item.lyDo}</td>
                  <td class="action-cell text-center">
                    <a class="btn-action btn-edit"
                       href="${pageContext.request.contextPath}/lichsugiasanpham?action=edit&id=${item.maLSGia}">
                      Sửa
                    </a>

                    <a class="btn-action btn-delete"
                       href="${pageContext.request.contextPath}/lichsugiasanpham?action=delete&id=${item.maLSGia}"
                       onclick="return confirm('Bạn có chắc muốn xóa lịch sử giá ${item.maLSGia} không?');">
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
