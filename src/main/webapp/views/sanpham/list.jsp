﻿<%@ page import="entity.TaiKhoan" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%
  TaiKhoan user = (TaiKhoan) session.getAttribute("user");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Quản lý sản phẩm</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=141">
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>Quản Lý Sản Phẩm</h1>
      <div class="user-box">
        Xin chào:
        <b><%= user != null ? user.getTenDangNhap() : "" %></b>
        | Vai trò:
        <b><%= user != null ? user.getVaiTro() : "" %></b>
      </div>
    </div>

    <div class="content-box">
      <div class="page-header">
        <h2>Danh sách sản phẩm</h2>
        <c:if test="${canManageProducts}">
          <a class="btn btn-add"
             href="${pageContext.request.contextPath}/sanpham?action=add">
            + Thêm sản phẩm
          </a>
        </c:if>
      </div>

      <form class="list-toolbar" method="get" action="${pageContext.request.contextPath}/sanpham">
        <div class="list-search">
          <input
              type="search"
              name="keyword"
              value="${keyword}"
              placeholder="Tìm theo mã, tên, loại, mô tả hoặc thành phần..."
              aria-label="Tìm sản phẩm">
          <button class="btn-icon-search" type="submit" aria-label="Tìm kiếm">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" stroke-width="2"></circle>
              <path d="M16 16l5 5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
            </svg>
          </button>
        </div>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/sanpham">Xóa lọc</a>
      </form>

      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
          <tr>
            <th>Mã SP</th>
            <th>Ảnh</th>
            <th>Tên sản phẩm</th>
            <th>Loại SP</th>
            <th>Size SP</th>
            <th>Đơn giá</th>
            <th>Số lượng tồn</th>
            <th>Mô tả</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>

          <tbody>
          <c:choose>
            <c:when test="${empty dsSanPham}">
              <tr>
                <td colspan="9" class="text-center">
                  <c:choose>
                    <c:when test="${empty keyword}">Chưa có sản phẩm nào</c:when>
                    <c:otherwise>Không tìm thấy sản phẩm phù hợp</c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:when>

            <c:otherwise>
              <c:forEach var="sp" items="${dsSanPham}">
                <tr>
                  <td>${sp.maSP}</td>
                  <td>
                    <c:if test="${not empty sp.hinhAnh}">
                      <button type="button"
                              class="product-image-trigger"
                              title="Xem thông tin sản phẩm"
                              data-ma-sp="<c:out value='${sp.maSP}'/>"
                              data-ten-sp="<c:out value='${sp.tenSP}'/>"
                              data-loai-sp="<c:out value='${sp.loaiSP}'/>"
                              data-size-sp="<c:out value='${sp.sizeSP}'/>"
                              data-don-gia="<c:out value='${sp.donGia}'/>"
                              data-so-luong-ton="<c:out value='${sp.soLuongTon}'/>"
                              data-mo-ta="<c:out value='${sp.moTa}'/>"
                              data-thanh-phan="<c:out value='${sp.thanhPhan}'/>">
                        <c:choose>
                          <c:when test="${fn:startsWith(sp.hinhAnh, '/')}">
                            <img class="product-table-image"
                                 src="${pageContext.request.contextPath}${sp.hinhAnh}"
                                 alt="${sp.tenSP}">
                          </c:when>
                          <c:when test="${fn:startsWith(sp.hinhAnh, 'uploads/')}">
                            <img class="product-table-image"
                                 src="${pageContext.request.contextPath}/${sp.hinhAnh}"
                                 alt="${sp.tenSP}">
                          </c:when>
                          <c:otherwise>
                            <img class="product-table-image"
                                 src="${pageContext.request.contextPath}/images/${sp.hinhAnh}"
                                 alt="${sp.tenSP}">
                          </c:otherwise>
                        </c:choose>
                      </button>
                    </c:if>
                  </td>
                  <td>${sp.tenSP}</td>
                  <td>${sp.loaiSP}</td>
                  <td>${sp.sizeSP}</td>
                  <td>${sp.donGia}</td>
                  <td>${sp.soLuongTon}</td>
                  <td>${sp.moTa}</td>
                  <td class="action-cell text-center">
                    <c:choose>
                      <c:when test="${canManageProducts}">
                        <a class="btn-action btn-edit"
                           href="${pageContext.request.contextPath}/sanpham?action=edit&id=${sp.maSP}">
                          Sửa
                        </a>

                        <a class="btn-action btn-delete"
                           href="${pageContext.request.contextPath}/sanpham?action=delete&id=${sp.maSP}"
                           onclick="return confirm('Bạn có chắc muốn xóa sản phẩm ${sp.maSP} không?');">
                          Xóa
                        </a>
                      </c:when>
                      <c:otherwise>
                        -
                      </c:otherwise>
                    </c:choose>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>

      <div id="productDetailModal" class="product-detail-modal" aria-hidden="true">
        <div id="productDetailBackdrop" class="product-detail-backdrop" hidden></div>
        <div class="product-detail-panel" role="dialog" aria-modal="true" aria-labelledby="productDetailTitle">
          <div class="product-detail-panel-header">
            <div>
              <h3 id="productDetailTitle" class="product-detail-panel-title">Chi tiết sản phẩm</h3>
              <p id="productDetailSubtitle" class="product-detail-panel-subtitle"></p>
            </div>
            <button id="productDetailClose" type="button" class="product-detail-panel-close" aria-label="Đóng">×</button>
          </div>

          <div class="product-detail-panel-body">
            <div class="product-detail-visual">
              <img id="productDetailImage" src="" alt="">
            </div>

            <div class="product-detail-content">
              <div class="product-detail-meta-grid">
                <div class="product-detail-meta-item">
                  <span class="product-detail-meta-label">Mã SP</span>
                  <strong id="productDetailCode" class="product-detail-meta-value"></strong>
                </div>
                <div class="product-detail-meta-item">
                  <span class="product-detail-meta-label">Đơn giá</span>
                  <strong id="productDetailPrice" class="product-detail-meta-value"></strong>
                </div>
                <div class="product-detail-meta-item">
                  <span class="product-detail-meta-label">Size</span>
                  <strong id="productDetailSize" class="product-detail-meta-value"></strong>
                </div>
                <div class="product-detail-meta-item">
                  <span class="product-detail-meta-label">Số lượng tồn</span>
                  <strong id="productDetailStock" class="product-detail-meta-value"></strong>
                </div>
              </div>

              <section class="product-detail-section">
                <h4>Mô tả</h4>
                <p id="productDetailDescription" class="product-detail-text"></p>
              </section>

              <section class="product-detail-section">
                <h4>Thành phần</h4>
                <p id="productDetailIngredients" class="product-detail-text"></p>
              </section>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
  (function () {
    const modal = document.getElementById('productDetailModal');
    if (!modal) {
      return;
    }

    const backdrop = document.getElementById('productDetailBackdrop');
    const closeButton = document.getElementById('productDetailClose');
    const title = document.getElementById('productDetailTitle');
    const subtitle = document.getElementById('productDetailSubtitle');
    const image = document.getElementById('productDetailImage');
    const code = document.getElementById('productDetailCode');
    const price = document.getElementById('productDetailPrice');
    const size = document.getElementById('productDetailSize');
    const stock = document.getElementById('productDetailStock');
    const description = document.getElementById('productDetailDescription');
    const ingredients = document.getElementById('productDetailIngredients');
    const imageButtons = document.querySelectorAll('.product-image-trigger');
    const defaultImage = '${pageContext.request.contextPath}/images/product-boba-milk-tea.png';

    function textOrFallback(value, fallback) {
      const text = String(value || '').trim();
      return text ? text : fallback;
    }

    function formatMoney(value) {
      const normalized = String(value || '').replace(/[^0-9.-]/g, '');
      const amount = Number(normalized);
      if (!Number.isFinite(amount) || amount < 0) {
        return textOrFallback(value, 'Chưa cập nhật');
      }
      return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
    }

    function openProductDetail(button) {
      if (!button) {
        return;
      }

      const img = button.querySelector('img');
      const productName = textOrFallback(button.dataset.tenSp, 'Sản phẩm');
      const productCode = textOrFallback(button.dataset.maSp, 'Chưa cập nhật');
      const productType = textOrFallback(button.dataset.loaiSp, 'Chưa cập nhật');

      title.textContent = productName;
      subtitle.textContent = 'Mã SP: ' + productCode + ' • Loại: ' + productType;
      image.src = img ? (img.getAttribute('src') || defaultImage) : defaultImage;
      image.alt = 'Ảnh sản phẩm ' + productName;
      code.textContent = productCode;
      price.textContent = formatMoney(button.dataset.donGia);
      size.textContent = textOrFallback(button.dataset.sizeSp, 'Chưa cập nhật');
      stock.textContent = textOrFallback(button.dataset.soLuongTon, 'Chưa cập nhật');
      description.textContent = textOrFallback(button.dataset.moTa, 'Chưa cập nhật mô tả.');
      ingredients.textContent = textOrFallback(button.dataset.thanhPhan, 'Chưa cập nhật thành phần.');

      modal.classList.add('is-open');
      modal.setAttribute('aria-hidden', 'false');
      if (backdrop) {
        backdrop.hidden = false;
      }

      if (closeButton) {
        window.setTimeout(function () {
          closeButton.focus();
        }, 0);
      }
    }

    function closeProductDetail() {
      modal.classList.remove('is-open');
      modal.setAttribute('aria-hidden', 'true');
      if (backdrop) {
        backdrop.hidden = true;
      }
    }

    imageButtons.forEach(function (button) {
      button.addEventListener('click', function () {
        openProductDetail(button);
      });
    });

    if (closeButton) {
      closeButton.addEventListener('click', closeProductDetail);
    }

    if (backdrop) {
      backdrop.addEventListener('click', closeProductDetail);
    }

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape' && modal.classList.contains('is-open')) {
        closeProductDetail();
      }
    });
  })();
</script>

</body>
</html>
