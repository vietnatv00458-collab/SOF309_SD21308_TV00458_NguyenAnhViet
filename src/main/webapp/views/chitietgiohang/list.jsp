<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="entity.ChiTietGioHang" %>
<%@ page import="entity.TaiKhoan" %>

<%
    List<ChiTietGioHang> ds = (List<ChiTietGioHang>) request.getAttribute("dsChiTietGioHang");
    Map<String, Boolean> completedDatHangMap = (Map<String, Boolean>) request.getAttribute("completedDatHangMap");
    Map<String, Boolean> lockedDatHangMap = (Map<String, Boolean>) request.getAttribute("lockedDatHangMap");
    String contextPath = request.getContextPath();
    TaiKhoan user = (TaiKhoan) session.getAttribute("user");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi Tiết Giỏ Hàng</title>
    <link rel="stylesheet" href="<%= contextPath %>/css/style.css?v=140">
</head>
<body>
<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>Chi Tiết Giỏ Hàng</h1>
            <div class="user-box">
                Xin chào: <b><%= user != null ? user.getTenDangNhap() : "" %></b>
                | Vai trò: <b><%= user != null ? user.getVaiTro() : "" %></b>
            </div>
        </div>

        <div class="content-box">
        <div class="page-header">
            <h2>QUẢN LÝ CHI TIẾT GIỎ HÀNG</h2>
            <p>Danh sách sản phẩm đang có trong các giỏ hàng</p>
        </div>

        <div class="toolbar">
            <a href="<%= contextPath %>/chitietgiohang?action=add" class="btn btn-add">
                + Thêm chi tiết giỏ hàng
            </a>

            <a href="<%= contextPath %>/dashboard" class="btn btn-secondary">
                ← Quay lại Dashboard
            </a>
        </div>

        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                <tr>
                    <th>Mã CTGH</th>
                    <th>Mã Giỏ Hàng</th>
                    <th>Mã Đặt Hàng</th>
                    <th>Mã Sản Phẩm</th>
                    <th>Mã Size</th>
                    <th>Số Lượng</th>
                    <th>Đơn Giá</th>
                    <th>Hành động</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (ds != null && !ds.isEmpty()) {
                        for (ChiTietGioHang ct : ds) {
                %>
                <tr>
                    <td><%= ct.getMaCTGH() %></td>
                    <td><%= ct.getMaGioHang() %></td>
                    <td><%= ct.getMaDatHang() != null ? ct.getMaDatHang() : "" %></td>
                    <td><%= ct.getMaSP() %></td>
                    <td><%= ct.getMaSize() != null ? ct.getMaSize() : "" %></td>
                    <td><%= ct.getSoLuong() %></td>
                    <td><%= ct.getDonGia() %></td>
                    <td class="action-col">
                        <%
                            boolean locked = (lockedDatHangMap != null
                                    && ct.getMaDatHang() != null
                                    && Boolean.TRUE.equals(lockedDatHangMap.get(ct.getMaDatHang())))
                                    || (completedDatHangMap != null
                                    && ct.getMaDatHang() != null
                                    && Boolean.TRUE.equals(completedDatHangMap.get(ct.getMaDatHang())));
                            if (!locked) {
                        %>
                        <a href="<%= contextPath %>/chitietgiohang?action=edit&id=<%= ct.getMaCTGH() %>"
                           class="btn-action btn-edit">
                            Sửa
                        </a>
                        <a href="<%= contextPath %>/chitietgiohang?action=delete&id=<%= ct.getMaCTGH() %>"
                           class="btn-action btn-delete"
                           onclick="return confirm('Bạn có chắc muốn xóa chi tiết giỏ hàng này không?');">
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
                    <td colspan="8" class="empty-cell">Không có dữ liệu chi tiết giỏ hàng.</td>
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
