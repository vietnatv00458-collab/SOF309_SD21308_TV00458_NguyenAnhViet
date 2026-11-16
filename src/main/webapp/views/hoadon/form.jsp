<%@ page import="entity.HoaDon" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%
  HoaDon formHoaDon = (HoaDon) request.getAttribute("hoaDon");
  String nextMaHD = (String) request.getAttribute("nextMaHD");
  String defaultMaNV = (String) request.getAttribute("defaultMaNV");
  String defaultMaDatHang = (String) request.getAttribute("defaultMaDatHang");
  String maHDValue = formHoaDon != null && formHoaDon.getMaHD() != null ? formHoaDon.getMaHD() : nextMaHD;
  String maDatHangValue = formHoaDon != null && formHoaDon.getMaDatHang() != null
          ? formHoaDon.getMaDatHang()
          : defaultMaDatHang;
  String maNVValue = formHoaDon != null && formHoaDon.getMaNV() != null ? formHoaDon.getMaNV() : defaultMaNV;
  String ngayLapValue = formHoaDon != null && formHoaDon.getNgayLap() != null
          ? formHoaDon.getNgayLap().toLocalDateTime().toString().substring(0, 16)
          : "";
%>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>${hoaDon != null ? "Sửa hóa đơn" : "Thêm hóa đơn"}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>${hoaDon != null ? "SỬA HÓA ĐƠN" : "THÊM HÓA ĐƠN"}</h1>
    </div>

    <div class="form-box">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <form method="post" action="${pageContext.request.contextPath}/hoadon">
        <input type="hidden" name="action"
               value="${hoaDon != null ? 'update' : 'create'}"/>

        <div class="form-grid">
          <div class="form-group">
            <label for="maHD">Mã hóa đơn</label>
            <input type="text"
                   id="maHD"
                   name="maHD"
                   value="<%= maHDValue != null ? maHDValue : "" %>"
                   <%= maHDValue != null ? "readonly" : "" %>
                   required>
          </div>

          <div class="form-group">
            <label for="maKH">Mã khách hàng</label>
            <input type="text"
                   id="maKH"
                   name="maKH"
                   value="${hoaDon.maKH}">
          </div>

          <div class="form-group">
            <label for="maDatHang">Mã đặt hàng</label>
            <input type="text"
                   id="maDatHang"
                   name="maDatHang"
                   value="<%= maDatHangValue != null ? maDatHangValue : "" %>"
                   <%= maDatHangValue != null ? "readonly" : "" %>>
          </div>

          <div class="form-group">
            <label for="maNV">Mã nhân viên</label>
            <input type="text"
                   id="maNV"
                   name="maNV"
                   value="<%= maNVValue != null ? maNVValue : "" %>"
                   <%= maNVValue != null ? "readonly" : "" %>>
          </div>

          <div class="form-group">
            <label for="maKM">Mã khuyến mãi</label>
            <input type="text"
                   id="maKM"
                   name="maKM"
                   value="${hoaDon.maKM}">
          </div>

          <div class="form-group">
            <label for="ngayLap">Ngày lập</label>
            <input type="datetime-local"
                   id="ngayLap"
                   name="ngayLap"
                   value="<%= ngayLapValue %>">
          </div>

          <div class="form-group">
            <label for="tongTien">Tổng tiền</label>
            <input type="number"
                   step="0.01"
                   id="tongTien"
                   name="tongTien"
                   value="${hoaDon.tongTien}">
          </div>

          <div class="form-group">
            <label for="giamGia">Giảm giá</label>
            <input type="number"
                   step="0.01"
                   id="giamGia"
                   name="giamGia"
                   value="${hoaDon.giamGia}">
          </div>

          <div class="form-group">
            <label for="thanhTien">Thành tiền</label>
            <input type="number"
                   step="0.01"
                   id="thanhTien"
                   name="thanhTien"
                   value="${hoaDon.thanhTien}">
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            ${hoaDon != null ? "Cập nhật" : "Thêm mới"}
          </button>
          <a href="${pageContext.request.contextPath}/hoadon" class="btn btn-back">
            Quay lại
          </a>
        </div>
      </form>
    </div>
  </div>
</div>

</body>
</html>
