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
  <title>Quản lý nhân viên</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>Quản Lý Nhân Viên</h1>
      <div class="user-box">
        Xin chào:
        <b><%= user != null ? user.getTenDangNhap() : "" %></b>
        | Vai trò:
        <b><%= user != null ? user.getVaiTro() : "" %></b>
      </div>
    </div>

    <div class="content-box">
      <div class="page-header">
        <h2>Danh sách nhân viên</h2>
        <a class="btn btn-add"
           href="${pageContext.request.contextPath}/nhanvien?action=add">
          + Thêm nhân viên
        </a>
      </div>

      <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success">${sessionScope.success}</div>
        <% session.removeAttribute("success"); %>
      </c:if>

      <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-error">${sessionScope.error}</div>
        <% session.removeAttribute("error"); %>
      </c:if>

      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
          <tr>
            <th>Mã NV</th>
            <th>Họ tên</th>
            <th>Ngày sinh</th>
            <th>Ngày vào làm</th>
            <th>Giới tính</th>
            <th>SĐT</th>
            <th>Email</th>
            <th>Chức vụ</th>
            <th>Lương</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <c:choose>
            <c:when test="${empty dsNhanVien}">
              <tr>
                <td colspan="10" class="text-center">Chưa có nhân viên nào</td>
              </tr>
            </c:when>

            <c:otherwise>
              <c:forEach var="nv" items="${dsNhanVien}">
                <tr>
                  <td>${nv.maNV}</td>
                  <td>${nv.hoTen}</td>
                  <td>${nv.ngaySinh}</td>
                  <td>${nv.ngayVaoLam}</td>
                  <td>${nv.gioiTinh}</td>
                  <td>${nv.sdt}</td>
                  <td>${nv.email}</td>
                  <td>${nv.chucVu}</td>
                  <td>${nv.luong}</td>
                  <td class="action-cell text-center">
                    <a class="btn-action btn-edit"
                       href="${pageContext.request.contextPath}/nhanvien?action=edit&id=${nv.maNV}">
                      Sửa
                    </a>

                    <a class="btn-action btn-delete"
                       href="${pageContext.request.contextPath}/nhanvien?action=delete&id=${nv.maNV}"
                       data-pending-count="${deleteImpactMap[nv.maNV]}"
                       data-staff-code="${nv.maNV}"
                       onclick="return confirmDelete(this);">
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

<script>
  function confirmDelete(button) {
    const pendingCount = Number(button.getAttribute('data-pending-count') || '0');
    const staffCode = button.getAttribute('data-staff-code') || '';
    return confirm(
      `Nhân viên ${staffCode} đang có ${pendingCount} đơn chưa hoàn thành. ` +
      `Khi xóa nhân viên, các đơn chưa hoàn thành sẽ bị xóa, các đơn đã hoàn thành vẫn được giữ lại. ` +
      `Bạn có chắc chắn muốn tiếp tục?`
    );
  }
</script>

</body>
</html>
