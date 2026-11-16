<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="entity.ChiTietGioHang" %>
<%@ page import="entity.Size" %>
<%@ page import="entity.TaiKhoan" %>
<%@ page import="java.util.List" %>

<%
    ChiTietGioHang ct = (ChiTietGioHang) request.getAttribute("chiTietGioHang");
    List<Size> dsSize = (List<Size>) request.getAttribute("dsSize");
    TaiKhoan user = (TaiKhoan) session.getAttribute("user");
    boolean isEdit = (ct != null);
    String error = (String) request.getAttribute("error");
    String contextPath = request.getContextPath();

    String maCTGH = isEdit ? String.valueOf(ct.getMaCTGH()) : "";
    String maGioHang = isEdit && ct.getMaGioHang() != null ? ct.getMaGioHang() : "";
    String maDatHang = isEdit && ct.getMaDatHang() != null ? ct.getMaDatHang() : "";
    String maSP = isEdit && ct.getMaSP() != null ? ct.getMaSP() : "";
    String maSize = isEdit && ct.getMaSize() != null ? ct.getMaSize() : "";
    String soLuong = isEdit ? String.valueOf(ct.getSoLuong()) : "";
    String donGia = isEdit && ct.getDonGia() != null ? String.valueOf(ct.getDonGia()) : "";
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title><%= isEdit ? "Sửa Chi Tiết Giỏ Hàng" : "Thêm Chi Tiết Giỏ Hàng" %></title>
    <link rel="stylesheet" href="<%= contextPath %>/css/style.css?v=140">
</head>
<body>
<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1><%= isEdit ? "Sửa Chi Tiết Giỏ Hàng" : "Thêm Chi Tiết Giỏ Hàng" %></h1>
            <div class="user-box">
                Xin chào: <b><%= user != null ? user.getTenDangNhap() : "" %></b>
                | Vai trò: <b><%= user != null ? user.getVaiTro() : "" %></b>
            </div>
        </div>

        <div class="content-box form-container">
        <div class="page-header">
            <h2><%= isEdit ? "SỬA CHI TIẾT GIỎ HÀNG" : "THÊM CHI TIẾT GIỎ HÀNG" %></h2>
            <p>Nhập thông tin sản phẩm trong giỏ hàng</p>
        </div>

        <% if (error != null && !error.trim().isEmpty()) { %>
        <div class="error-message"><%= error %></div>
        <% } %>

        <form class="crud-form" method="post" action="<%= contextPath %>/chitietgiohang">
            <input type="hidden" name="action" value="<%= isEdit ? "update" : "create" %>">

            <div class="form-grid">
                <div class="form-group">
                    <label for="maCTGH">Mã CTGH</label>
                    <input type="number" id="maCTGH" name="maCTGH"
                           value="<%= maCTGH %>"
                        <%= isEdit ? "readonly" : "" %> required>
                </div>

                <div class="form-group">
                    <label for="maGioHang">Mã Giỏ Hàng</label>
                    <input type="text" id="maGioHang" name="maGioHang"
                           value="<%= maGioHang %>" required>
                </div>

                <div class="form-group">
                    <label for="maDatHang">Mã Đặt Hàng</label>
                    <input type="text" id="maDatHang" name="maDatHang"
                           value="<%= maDatHang %>">
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
                <a href="<%= contextPath %>/chitietgiohang" class="btn btn-cancel">Hủy</a>
            </div>
        </form>
    </div>
    </div>
</div>
</body>
</html>
