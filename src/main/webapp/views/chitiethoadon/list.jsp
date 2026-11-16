<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="entity.ChiTietHoaDon" %>
<%@ page import="entity.TaiKhoan" %>

<%
  List<ChiTietHoaDon> ds = (List<ChiTietHoaDon>) request.getAttribute("dsChiTietHoaDon");
  Map<String, Boolean> completedHoaDonMap = (Map<String, Boolean>) request.getAttribute("completedHoaDonMap");
  String contextPath = request.getContextPath();
  TaiKhoan user = (TaiKhoan) session.getAttribute("user");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Chi Tiết Hóa Đơn</title>
  <link rel="stylesheet" href="<%= contextPath %>/css/style.css?v=140">
</head>
<body>
<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>Chi Tiết Hóa Đơn</h1>
      <div class="user-box">
        Xin chào: <b><%= user != null ? user.getTenDangNhap() : "" %></b>
        | Vai trò: <b><%= user != null ? user.getVaiTro() : "" %></b>
      </div>
    </div>

    <div class="content-box">
    <div class="page-header">
      <h2>QUẢN LÝ CHI TIẾT HÓA ĐƠN</h2>
      <p>Danh sách các sản phẩm thuộc từng hóa đơn</p>
    </div>

    <div class="toolbar">
      <a href="<%= contextPath %>/chitiethoadon?action=add" class="btn btn-add">
        + Thêm chi tiết hóa đơn
      </a>

      <a href="<%= contextPath %>/dashboard" class="btn btn-secondary">
        ← Quay lại Dashboard
      </a>
    </div>

    <div class="table-wrapper">
      <table class="data-table">
        <thead>
        <tr>
          <th>Mã CTHĐ</th>
          <th>Mã Hóa Đơn</th>
          <th>Mã Sản Phẩm</th>
          <th>Mã Size</th>
          <th>Số Lượng</th>
          <th>Đơn Giá</th>
          <th>Thành Tiền</th>
          <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <%
          if (ds != null && !ds.isEmpty()) {
            for (ChiTietHoaDon ct : ds) {
        %>
        <tr>
          <td><%= ct.getMaCTHD() %></td>
          <td><%= ct.getMaHD() %></td>
          <td><%= ct.getMaSP() %></td>
          <td><%= ct.getMaSize() != null ? ct.getMaSize() : "" %></td>
          <td><%= ct.getSoLuong() %></td>
          <td><%= ct.getDonGia() %></td>
          <td><%= ct.getThanhTien() %></td>
          <td class="action-col">
            <%
              boolean locked = completedHoaDonMap != null
                      && ct.getMaHD() != null
                      && Boolean.TRUE.equals(completedHoaDonMap.get(ct.getMaHD()));
              if (!locked) {
            %>
            <a href="<%= contextPath %>/chitiethoadon?action=edit&id=<%= ct.getMaCTHD() %>"
               class="btn-action btn-edit">
              Sửa
            </a>
            <a href="<%= contextPath %>/chitiethoadon?action=delete&id=<%= ct.getMaCTHD() %>"
               class="btn-action btn-delete"
               onclick="return confirm('Bạn có chắc muốn xóa chi tiết hóa đơn này không?');">
              Xóa
            </a>
            <%
              } else {
            %>
            -
            <%
              }
            %>
          </td>
        </tr>
        <%
            }
          } else {
        %>
        <tr>
          <td colspan="8" class="empty-cell">Không có dữ liệu chi tiết hóa đơn.</td>
        </tr>
        <%
          }
        %>
        </tbody>
      </table>
    </div>
  </div>
  </div>
</div>
</body>
</html>
