<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%
  String ngayDatValue = "";
  Object obj = request.getAttribute("datHang");
  if (obj != null) {
    entity.DatHang dh = (entity.DatHang) obj;
    if (dh.getNgayDat() != null) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
      ngayDatValue = sdf.format(dh.getNgayDat());
    }
  }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${datHang != null ? "Sửa đặt hàng" : "Thêm đặt hàng"}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>${datHang != null ? "SỬA ĐẶT HÀNG" : "THÊM ĐẶT HÀNG"}</h1>
    </div>

    <div class="form-box">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <form method="post" action="${pageContext.request.contextPath}/dathang">
        <input type="hidden" name="action"
               value="${datHang != null ? 'update' : 'create'}"/>

        <div class="form-grid">
          <div class="form-group">
            <label for="maDatHang">Mã đặt hàng</label>
            <input type="text"
                   id="maDatHang"
                   name="maDatHang"
                   value="${datHang.maDatHang}"
            ${datHang != null ? "readonly" : ""}
                   required>
          </div>

          <div class="form-group">
            <label for="maKH">Mã khách hàng</label>
            <input type="text"
                   id="maKH"
                   name="maKH"
                   value="${datHang.maKH}"
                   required>
          </div>

          <div class="form-group">
            <label for="maNV">Mã NV xử lý</label>
            <input type="text"
                   id="maNV"
                   name="maNV"
                   value="${datHang.maNV}"
                   readonly>
          </div>

          <div class="form-group">
            <label for="soDienThoai">Số điện thoại</label>
            <input type="text"
                   id="soDienThoai"
                   name="soDienThoai"
                   value="${datHang.soDienThoai}">
          </div>

          <div class="form-group full">
            <label for="diaChiGiaoHang">Địa chỉ giao hàng</label>
            <input type="text"
                   id="diaChiGiaoHang"
                   name="diaChiGiaoHang"
                   value="${datHang.diaChiGiaoHang}">
          </div>

          <div class="form-group">
            <label for="ngayDat">Ngày đặt</label>
            <input type="datetime-local"
                   id="ngayDat"
                   name="ngayDat"
                   value="<%= ngayDatValue %>">
          </div>

          <div class="form-group">
            <label for="trangThai">Trạng thái</label>
            <select name="trangThai" id="trangThai">
              <option value="Đang làm đơn"
              ${datHang != null && datHang.trangThai == 'Đang làm đơn' ? 'selected' : ''}>
                Đang làm đơn
              </option>
              <option value="Đang giao hàng"
              ${datHang != null && datHang.trangThai == 'Đang giao hàng' ? 'selected' : ''}>
                Đang giao hàng
              </option>
              <option value="Hoàn thành"
              ${datHang != null && datHang.trangThai == 'Hoàn thành' ? 'selected' : ''}>
                Hoàn thành
              </option>
              <option value="Đã hủy"
              ${datHang != null && datHang.trangThai == 'Đã hủy' ? 'selected' : ''}>
                Đã hủy
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="tongTien">Tổng tiền</label>
            <input type="number"
                   step="0.01"
                   id="tongTien"
                   name="tongTien"
                   value="${datHang.tongTien}">
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            ${datHang != null ? "Cập nhật" : "Thêm mới"}
          </button>
          <a href="${pageContext.request.contextPath}/dathang" class="btn btn-back">
            Quay lại
          </a>
        </div>
      </form>
    </div>
  </div>
</div>

</body>
</html>
