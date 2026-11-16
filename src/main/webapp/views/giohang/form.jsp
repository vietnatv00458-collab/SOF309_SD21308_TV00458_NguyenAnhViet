<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%
    String ngayTaoValue = "";
    Object obj = request.getAttribute("gioHang");
    if (obj != null) {
        entity.GioHang gh = (entity.GioHang) obj;
        if (gh.getNgayTao() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            ngayTaoValue = sdf.format(gh.getNgayTao());
        }
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${gioHang != null ? "Sửa giỏ hàng" : "Thêm giỏ hàng"}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>${gioHang != null ? "SỬA GIỎ HÀNG" : "THÊM GIỎ HÀNG"}</h1>
        </div>

        <div class="form-box">
            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/giohang">
                <input type="hidden" name="action"
                       value="${gioHang != null ? 'update' : 'create'}"/>

                <div class="form-grid">
                    <div class="form-group">
                        <label for="maGioHang">Mã giỏ hàng</label>
                        <input type="text"
                               id="maGioHang"
                               name="maGioHang"
                               value="${gioHang.maGioHang}"
                        ${gioHang != null ? "readonly" : ""}
                               required>
                    </div>

                    <div class="form-group">
                        <label for="maKH">Mã khách hàng</label>
                        <input type="text"
                               id="maKH"
                               name="maKH"
                               value="${gioHang.maKH}"
                               required>
                    </div>

                    <div class="form-group">
                        <label for="ngayTao">Ngày tạo</label>
                        <input type="datetime-local"
                               id="ngayTao"
                               name="ngayTao"
                               value="<%= ngayTaoValue %>">
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-save">
                        ${gioHang != null ? "Cập nhật" : "Thêm mới"}
                    </button>
                    <a href="${pageContext.request.contextPath}/giohang" class="btn btn-back">
                        Quay lại
                    </a>
                </div>
            </form>
        </div>
    </div>
</div>

</body>
</html>