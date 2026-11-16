<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<% 
  entity.TaiKhoan taiKhoan = (entity.TaiKhoan) request.getAttribute("taiKhoan");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>${taiKhoan != null ? "Sửa tài khoản" : "Thêm tài khoản"}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>
        <c:choose>
          <c:when test="${taiKhoan != null}">SỬA TÀI KHOẢN</c:when>
          <c:when test="${createMode eq 'delivery'}">THÊM TÀI KHOẢN GIAO HÀNG</c:when>
          <c:otherwise>THÊM TÀI KHOẢN</c:otherwise>
        </c:choose>
      </h1>
    </div>

    <div class="form-box">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <c:set var="selectedMaNV" value="${formMaNV ne null ? formMaNV : (taiKhoan != null ? taiKhoan.maNV : '')}" />
      <c:set var="isDeliveryMode" value="${createMode eq 'delivery'}" />

      <form method="post" action="${pageContext.request.contextPath}/taikhoan">
        <input type="hidden" name="action"
               value="${taiKhoan != null ? 'update' : 'create'}"/>
        <c:if test="${isDeliveryMode}">
          <input type="hidden" name="kind" value="delivery"/>
        </c:if>

        <c:if test="${isDeliveryMode && taiKhoan == null}">
          <div class="alert alert-info" style="margin-bottom:16px;">
            Chỉ hiển thị nhân viên giao hàng chưa có tài khoản.
          </div>
        </c:if>

        <div class="form-grid">
          <div class="form-group">
            <label for="maNV">
              ${taiKhoan != null ? "Mã nhân viên" : (isDeliveryMode ? "Chọn nhân viên giao hàng" : "Chọn nhân viên")}
            </label>
            <c:choose>
              <c:when test="${taiKhoan != null}">
                <input type="text"
                       id="maNV"
                       name="maNV"
                       value="${taiKhoan.maNV}"
                       readonly>
              </c:when>
              <c:otherwise>
                <select id="maNV" name="maNV" required>
                  <option value="">-- ${isDeliveryMode ? "Chọn nhân viên giao hàng" : "Chọn nhân viên"} --</option>
                  <c:forEach var="nv" items="${dsNhanVienChuaCoTaiKhoan}">
                    <option value="${nv.maNV}" ${selectedMaNV == nv.maNV ? 'selected' : ''}>
                      ${nv.maNV} - ${nv.hoTen}
                    </option>
                  </c:forEach>
                </select>
                <small class="form-note">
                  ${isDeliveryMode ? "Chỉ hiện nhân viên giao hàng chưa có tài khoản." : "Chỉ hiện nhân viên chưa có tài khoản."}
                </small>
                <c:if test="${empty dsNhanVienChuaCoTaiKhoan}">
                  <div class="alert alert-error">
                    ${isDeliveryMode ? "Chưa có nhân viên giao hàng nào chưa có tài khoản." : "Chưa có nhân viên nào chưa có tài khoản."}
                  </div>
                </c:if>
              </c:otherwise>
            </c:choose>
          </div>

          <div class="form-group">
            <label for="tenDangNhap">Tên đăng nhập</label>
            <input type="text"
                   id="tenDangNhap"
                   name="tenDangNhap"
                   value="${taiKhoan != null ? taiKhoan.tenDangNhap : formTenDangNhap}"
                   required>
          </div>

          <div class="form-group">
            <label for="matKhau">Mật khẩu</label>
            <input type="password"
                   id="matKhau"
                   name="matKhau"
                   value="${taiKhoan != null ? '' : formMatKhau}"
                   autocomplete="new-password"
                   required>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            ${taiKhoan != null ? "Cập nhật" : "Thêm mới"}
          </button>
          <a href="${pageContext.request.contextPath}/taikhoan" class="btn btn-back">
            Quay lại
          </a>
        </div>
      </form>
    </div>
  </div>
</div>

</body>
</html>
