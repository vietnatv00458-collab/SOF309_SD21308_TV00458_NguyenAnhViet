<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>${khachHang != null ? "Sửa khách hàng" : "Thêm khách hàng"}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>${khachHang != null ? "SỬA KHÁCH HÀNG" : "THÊM KHÁCH HÀNG"}</h1>
    </div>

    <div class="form-box">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <form method="post" action="${pageContext.request.contextPath}/khachhang">
        <input type="hidden" name="action"
               value="${khachHang != null ? 'update' : 'create'}"/>

        <div class="form-grid">
          <div class="form-group">
            <label for="maKH">Mã khách hàng</label>
            <input type="text"
                   id="maKH"
                   name="maKH"
                   value="${khachHang.maKH}"
            ${khachHang != null ? "readonly" : ""}
                   required>
          </div>

          <div class="form-group">
            <label for="hoTen">Họ tên</label>
            <input type="text"
                   id="hoTen"
                   name="hoTen"
                   value="${khachHang.hoTen}"
                   required>
          </div>

          <div class="form-group">
            <label for="sdt">SĐT</label>
            <input type="text"
                   id="sdt"
                   name="sdt"
                   value="${khachHang.sdt}">
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input type="email"
                   id="email"
                   name="email"
                   value="${khachHang.email}">
          </div>

          <div class="form-group full">
            <label for="diaChi">Địa chỉ</label>
            <input type="text"
                   id="diaChi"
                   name="diaChi"
                   value="${khachHang.diaChi}">
          </div>

        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            ${khachHang != null ? "Cập nhật" : "Thêm mới"}
          </button>
          <a href="${pageContext.request.contextPath}/khachhang" class="btn btn-back">
            Quay lại
          </a>
        </div>
      </form>
    </div>
  </div>
</div>

</body>
</html>
