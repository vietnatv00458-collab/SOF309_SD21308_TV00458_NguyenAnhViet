﻿<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>${sanPham != null ? "Sửa sản phẩm" : "Thêm sản phẩm"}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=141">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>${sanPham != null ? "SỬA SẢN PHẨM" : "THÊM SẢN PHẨM"}</h1>
    </div>

    <div class="form-box">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <c:set var="currentMaSP" value="${formMaSP ne null ? formMaSP : (sanPham != null ? sanPham.maSP : '')}" />
      <c:set var="currentTenSP" value="${formTenSP ne null ? formTenSP : (sanPham != null ? sanPham.tenSP : '')}" />
      <c:set var="currentLoaiSP" value="${formLoaiSP ne null ? formLoaiSP : (sanPham != null ? sanPham.loaiSP : '')}" />
      <c:set var="currentSizeSP" value="${formSizeSP ne null ? formSizeSP : (sanPham != null ? sanPham.sizeSP : '')}" />
      <c:set var="currentDonGia" value="${formDonGia ne null ? formDonGia : (sanPham != null && sanPham.donGia != null ? sanPham.donGia : 0)}" />
      <c:set var="currentSoLuongTon" value="${formSoLuongTon ne null ? formSoLuongTon : (sanPham != null && sanPham.soLuongTon != null ? sanPham.soLuongTon : 0)}" />
      <c:set var="currentMoTa" value="${formMoTa ne null ? formMoTa : (sanPham != null ? sanPham.moTa : '')}" />
      <c:set var="currentThanhPhan" value="${formThanhPhan ne null ? formThanhPhan : (sanPham != null ? sanPham.thanhPhan : '')}" />

      <form method="post" action="${pageContext.request.contextPath}/sanpham" enctype="multipart/form-data">
        <input type="hidden" name="action"
               value="${sanPham != null ? 'update' : 'create'}"/>

        <div class="form-grid">
          <div class="form-group">
            <label for="maSP">Mã sản phẩm (tự động)</label>
            <input type="text"
                   id="maSP"
                   name="maSP"
                   value="${currentMaSP}"
                   readonly>
            <small class="form-note">Hệ thống tự sinh mã khi lưu sản phẩm mới.</small>
          </div>

          <div class="form-group">
            <label for="tenSP">Tên sản phẩm</label>
            <input type="text"
                   id="tenSP"
                   name="tenSP"
                   value="${currentTenSP}"
                   required>
          </div>

          <div class="form-group">
            <label for="loaiSP">Loại sản phẩm</label>
            <input type="text"
                   id="loaiSP"
                   name="loaiSP"
                   value="${currentLoaiSP}">
          </div>

          <div class="form-group">
            <label for="sizeSP">Size SP</label>
            <input type="text"
                   id="sizeSP"
                   name="sizeSP"
                   value="${currentSizeSP}"
                   placeholder="S, M, L">
          </div>

          <div class="form-group">
            <label for="donGia">Đơn giá</label>
            <input type="number"
                   step="0.01"
                   min="0"
                   id="donGia"
                   name="donGia"
                   value="${currentDonGia}">
          </div>

          <div class="form-group">
            <label for="soLuongTon">Số lượng tồn</label>
            <input type="number"
                   min="0"
                   id="soLuongTon"
                   name="soLuongTon"
                   value="${currentSoLuongTon}">
          </div>

          <div class="form-group full">
            <label for="hinhAnhFile">Ảnh sản phẩm</label>
            <input type="file"
                   id="hinhAnhFile"
                   name="hinhAnhFile"
                   accept="image/png,image/jpeg,image/webp,image/gif">
            <c:if test="${sanPham != null && not empty sanPham.hinhAnh}">
              <div class="product-image-current">
                <c:choose>
                  <c:when test="${fn:startsWith(sanPham.hinhAnh, '/')}">
                    <img src="${pageContext.request.contextPath}${sanPham.hinhAnh}" alt="${sanPham.tenSP}">
                  </c:when>
                  <c:when test="${fn:startsWith(sanPham.hinhAnh, 'uploads/')}">
                    <img src="${pageContext.request.contextPath}/${sanPham.hinhAnh}" alt="${sanPham.tenSP}">
                  </c:when>
                  <c:otherwise>
                    <img src="${pageContext.request.contextPath}/images/${sanPham.hinhAnh}" alt="${sanPham.tenSP}">
                  </c:otherwise>
                </c:choose>
                <span>Ảnh hiện tại: ${sanPham.hinhAnh}</span>
              </div>
            </c:if>
          </div>

          <div class="form-group full">
            <label for="thanhPhan">Thành phần</label>
            <textarea id="thanhPhan"
                      name="thanhPhan"
                      rows="4"
                      placeholder="Ví dụ: cà phê nguyên chất, sữa tươi, trân châu">${currentThanhPhan}</textarea>
          </div>

          <div class="form-group full">
            <label for="moTa">Mô tả</label>
            <textarea id="moTa"
                      name="moTa"
                      rows="4">${currentMoTa}</textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            ${sanPham != null ? "Cập nhật" : "Thêm mới"}
          </button>
          <a href="${pageContext.request.contextPath}/sanpham" class="btn btn-back">
            Quay lại
          </a>
        </div>
      </form>
    </div>
  </div>
</div>

</body>
</html>
