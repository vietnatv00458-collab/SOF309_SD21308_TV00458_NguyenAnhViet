<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="entity.ChiTietHoaDon" %>
<%@ page import="entity.Size" %>
<%@ page import="entity.TaiKhoan" %>
<%@ page import="java.util.List" %>

<%
  ChiTietHoaDon ct = (ChiTietHoaDon) request.getAttribute("chiTietHoaDon");
  List<Size> dsSize = (List<Size>) request.getAttribute("dsSize");
  TaiKhoan user = (TaiKhoan) session.getAttribute("user");
  boolean isEdit = (ct != null);
  String error = (String) request.getAttribute("error");
  String contextPath = request.getContextPath();

  String maCTHD = isEdit ? String.valueOf(ct.getMaCTHD()) : "";
  String maHD = isEdit && ct.getMaHD() != null ? ct.getMaHD() : "";
  String maSP = isEdit && ct.getMaSP() != null ? ct.getMaSP() : "";
  String maSize = isEdit && ct.getMaSize() != null ? ct.getMaSize() : "";
  String soLuong = isEdit ? String.valueOf(ct.getSoLuong()) : "";
  String donGia = isEdit && ct.getDonGia() != null ? ct.getDonGia().toString() : "";
%>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title><%= isEdit ? "Sửa Chi Tiết Hóa Đơn" : "Thêm Chi Tiết Hóa Đơn" %></title>
  <link rel="stylesheet" href="<%= contextPath %>/css/style.css?v=140">
</head>
<body>
<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1><%= isEdit ? "Sửa Chi Tiết Hóa Đơn" : "Thêm Chi Tiết Hóa Đơn" %></h1>
      <div class="user-box">
        Xin chào: <b><%= user != null ? user.getTenDangNhap() : "" %></b>
        | Vai trò: <b><%= user != null ? user.getVaiTro() : "" %></b>
      </div>
    </div>

    <div class="content-box form-container">
    <div class="page-header">
      <h2><%= isEdit ? "SỬA CHI TIẾT HÓA ĐƠN" : "THÊM CHI TIẾT HÓA ĐƠN" %></h2>
      <p>Nhập thông tin chi tiết hóa đơn</p>
    </div>

    <% if (error != null && !error.trim().isEmpty()) { %>
    <div class="error-message"><%= error %></div>
    <% } %>

    <form class="crud-form" method="post" action="<%= contextPath %>/chitiethoadon">
      <input type="hidden" name="action" value="<%= isEdit ? "update" : "create" %>">

        <div class="form-grid">
        <div class="form-group">
          <label for="maCTHD">Mã CTHĐ</label>
          <input type="number" id="maCTHD" name="maCTHD"
                 value="<%= maCTHD %>"
            <%= isEdit ? "readonly" : "" %> required>
        </div>

        <div class="form-group">
          <label for="maHD">Mã Hóa Đơn</label>
          <input type="text" id="maHD" name="maHD"
                 value="<%= maHD %>" required>
        </div>

        <div class="form-group">
          <label for="maSP">Mã Sản Phẩm</label>
          <input type="text" id="maSP" name="maSP"
                 value="<%= maSP %>" required>
        </div>

        <div class="form-group">
          <label for="maSize">Mã Size</label>
          <select id="maSize" name="maSize" required>
            <option value="">-- Chọn size --</option>
            <%
              if (dsSize != null) {
                for (Size size : dsSize) {
                  String selected = size != null && size.getMaSize() != null
                          && size.getMaSize().equals(maSize) ? "selected" : "";
            %>
            <option value="<%= size.getMaSize() %>" <%= selected %>>
              <%= size.getMaSize() %> - <%= size.getTenSize() != null ? size.getTenSize() : "" %>
            </option>
            <%
                }
              }
            %>
          </select>
        </div>

        <div class="form-group">
          <label for="soLuong">Số lượng</label>
          <input type="number" id="soLuong" name="soLuong" min="1"
                 value="<%= soLuong %>" required>
        </div>

        <div class="form-group">
          <label for="donGia">Đơn giá</label>
          <input type="number" step="0.01" id="donGia" name="donGia"
                 value="<%= donGia %>" required>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-save">
          <%= isEdit ? "Cập nhật" : "Thêm mới" %>
        </button>
        <a href="<%= contextPath %>/chitiethoadon" class="btn btn-cancel">Hủy</a>
      </div>
    </form>
  </div>
  </div>
</div>
</body>
</html>
