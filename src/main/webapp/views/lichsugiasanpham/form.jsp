<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>${lichSuGiaSanPham != null && lichSuGiaSanPham.maLSGia != null ? "Sửa lịch sử giá" : "Thêm lịch sử giá"}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=142">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>${lichSuGiaSanPham != null && lichSuGiaSanPham.maLSGia != null ? "SỬA LỊCH SỬ GIÁ" : "THÊM LỊCH SỬ GIÁ"}</h1>
    </div>

    <div class="form-box">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <form method="post" action="${pageContext.request.contextPath}/lichsugiasanpham">
        <input type="hidden" name="action"
               value="${lichSuGiaSanPham != null && lichSuGiaSanPham.maLSGia != null ? 'update' : 'create'}"/>

        <c:if test="${lichSuGiaSanPham != null && lichSuGiaSanPham.maLSGia != null}">
          <input type="hidden" name="maLSGia" value="${lichSuGiaSanPham.maLSGia}">
        </c:if>

        <div class="form-grid">
          <div class="form-group">
            <label for="maSP">Mã sản phẩm</label>
            <input type="text"
                   id="maSP"
                   name="maSP"
                   value="${lichSuGiaSanPham.maSP}"
                   required>
          </div>

          <div class="form-group">
            <label for="giaCu">Giá cũ</label>
            <input type="number"
                   step="0.01"
                   min="0"
                   id="giaCu"
                   name="giaCu"
                   value="${lichSuGiaSanPham != null && lichSuGiaSanPham.giaCu != null ? lichSuGiaSanPham.giaCu : 0}">
          </div>

          <div class="form-group">
            <label for="giaMoi">Giá mới</label>
            <input type="number"
                   step="0.01"
                   min="0"
                   id="giaMoi"
                   name="giaMoi"
                   value="${lichSuGiaSanPham != null && lichSuGiaSanPham.giaMoi != null ? lichSuGiaSanPham.giaMoi : 0}">
          </div>

          <div class="form-group">
            <label for="ngayApDung">Ngày áp dụng</label>
            <input type="datetime-local"
                   id="ngayApDung"
                   name="ngayApDung"
                   value="${lichSuGiaSanPham != null ? lichSuGiaSanPham.ngayApDungInputValue : ''}">
          </div>

          <div class="form-group full">
            <label for="lyDo">Lý do</label>
            <textarea id="lyDo"
                      name="lyDo"
                      rows="4"
                      placeholder="Ví dụ: cập nhật giá bán theo menu mới">${lichSuGiaSanPham.lyDo}</textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            ${lichSuGiaSanPham != null && lichSuGiaSanPham.maLSGia != null ? "Cập nhật" : "Thêm mới"}
          </button>
          <a href="${pageContext.request.contextPath}/lichsugiasanpham" class="btn btn-back">
            Quay lại
          </a>
        </div>
      </form>
    </div>
  </div>
</div>

</body>
</html>
