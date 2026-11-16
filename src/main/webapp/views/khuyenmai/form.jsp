﻿﻿<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>${khuyenMai != null ? "Sửa khuyến mãi" : "Thêm khuyến mãi"}</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=141">
  <style>
    body {
      background: #f5efe6;
    }

    .layout {
      min-height: 100vh;
      background: #f5efe6;
    }

    .main-content {
      background: #f5efe6;
      padding-top: 16px;
    }

    .topbar {
      background: #f5efe6;
      border: 0;
      box-shadow: none;
      padding: 0 0 12px;
      margin-bottom: 0;
    }

    .form-box {
      width: 100%;
      max-width: none;
      margin-top: 0;
      background: #fffaf2;
      padding: 24px 28px 28px;
    }

    .form-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 18px 22px;
      align-items: start;
    }

    .form-group.full {
      grid-column: 1 / -1;
    }

    .form-group input,
    .form-group textarea,
    .form-group select {
      width: 100%;
      min-width: 0;
    }

    .discount-switch {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 10px;
      align-items: stretch;
      margin: 6px 0 0;
    }

    .discount-switch .discount-option {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      min-width: 0;
      height: 52px;
      box-sizing: border-box;
      padding: 0 14px;
      border: 1px solid #d9c4aa;
      border-radius: 8px;
      background: #fffaf3;
      cursor: pointer;
      font-weight: 600;
      font-size: 14px;
      text-align: center;
      white-space: nowrap;
      overflow: hidden;
    }

    .discount-switch .discount-option-content {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      min-width: 0;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
    }

    .discount-switch .discount-option-content input[type="radio"] {
      flex: 0 0 auto;
      margin: 0;
      position: static;
    }

    .discount-switch .discount-option-text {
      flex: 0 1 auto;
      min-width: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: clip;
      line-height: 1;
    }

    .discount-panel {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 10px 12px;
      padding: 12px;
      border: 1px dashed #d9c4aa;
      border-radius: 8px;
      background: #fffdf9;
    }

    .discount-panel small {
      grid-column: 1 / -1;
    }

    .discount-panel[hidden] {
      display: none;
    }

    .voucher-code {
      display: inline-flex;
      align-items: center;
      width: 100%;
      min-height: 36px;
      padding: 0 10px;
      border: 1px solid #d9c4aa;
      border-radius: 8px;
      background: #f8f2ea;
      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
      font-size: 13px;
      font-weight: 700;
      color: #6f4324;
    }

    @media (max-width: 900px) {
      .form-box {
        padding: 22px 18px 24px;
      }

      .form-grid {
        grid-template-columns: 1fr;
      }

      .discount-panel {
        grid-template-columns: 1fr;
      }

      .discount-switch .discount-option {
        width: 100%;
      }
    }
  </style>
</head>
<body>

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>${khuyenMai != null ? "SỬA KHUYẾN MÃI" : "THÊM KHUYẾN MÃI"}</h1>
    </div>

    <div class="form-box">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <c:set var="currentLoaiGiamGia" value="${formLoaiGiamGia}" />
      <c:if test="${empty currentLoaiGiamGia}">
        <c:choose>
          <c:when test="${khuyenMai != null and not empty khuyenMai.loaiGiamGia}">
            <c:set var="currentLoaiGiamGia" value="${khuyenMai.loaiGiamGia}" />
          </c:when>
          <c:when test="${khuyenMai != null and khuyenMai.soTienGiam != null}">
            <c:set var="currentLoaiGiamGia" value="AMOUNT" />
          </c:when>
          <c:otherwise>
            <c:set var="currentLoaiGiamGia" value="PERCENT" />
          </c:otherwise>
        </c:choose>
      </c:if>

      <c:set var="currentTenKM" value="${formTenKM ne null ? formTenKM : (khuyenMai != null ? khuyenMai.tenKM : '')}" />
      <c:set var="currentPhanTramGiam" value="${formPhanTramGiam ne null ? formPhanTramGiam : (khuyenMai != null ? khuyenMai.phanTramGiam : '')}" />
      <c:set var="currentSoTienGiam" value="${formSoTienGiam ne null ? formSoTienGiam : (khuyenMai != null ? khuyenMai.soTienGiam : '')}" />
      <c:set var="currentMucGiamToiDa" value="${formMucGiamToiDa ne null ? formMucGiamToiDa : (khuyenMai != null ? khuyenMai.mucGiamToiDa : '')}" />
      <c:set var="currentNgayBatDau" value="${formNgayBatDau ne null ? formNgayBatDau : (khuyenMai != null ? khuyenMai.ngayBatDau : '')}" />
      <c:set var="currentNgayKetThuc" value="${formNgayKetThuc ne null ? formNgayKetThuc : (khuyenMai != null ? khuyenMai.ngayKetThuc : '')}" />
      <c:set var="currentMoTa" value="${formMoTa ne null ? formMoTa : (khuyenMai != null ? khuyenMai.moTa : '')}" />

      <form method="post" action="${pageContext.request.contextPath}/khuyenmai">
        <input type="hidden" name="action"
               value="${khuyenMai != null ? 'update' : 'create'}"/>
        <input type="hidden" name="loaiGiamGia" id="loaiGiamGiaHidden" value="${currentLoaiGiamGia}">

        <div class="form-grid">
          <c:choose>
            <c:when test="${khuyenMai != null}">
              <div class="form-group">
                <label for="maKM">Mã khuyến mãi</label>
                <input type="text"
                       id="maKM"
                       name="maKM"
                       style="text-transform: uppercase;"
                       value="${khuyenMai.maKM}"
                       readonly>
                <small>Mã này được tạo tự động và dùng cho khách hàng nhập khi thanh toán.</small>
              </div>
            </c:when>
            <c:otherwise>
              <div class="form-group full">
                <label>Mã khuyến mãi</label>
                <div class="voucher-code"><c:out value="${empty previewMaKM ? 'KM000001' : previewMaKM}"/></div>
                <small>Không cần nhập tay. Hệ thống sẽ sinh mã tuần tự theo dạng KM000001, KM000002...</small>
              </div>
            </c:otherwise>
          </c:choose>

           <div class="form-group">
             <label for="tenKM">Tên khuyến mãi</label>
             <input type="text"
                   id="tenKM"
                   name="tenKM"
                   value="${currentTenKM}"
                   required>
          </div>

          <div class="form-group full">
            <label>Kiểu giảm giá</label>
            <div class="discount-switch">
              <label class="discount-option">
                <span class="discount-option-content">
                  <input type="radio" name="loaiGiamGiaChoice" value="PERCENT" <c:if test="${currentLoaiGiamGia eq 'PERCENT'}">checked</c:if>>
                  <span class="discount-option-text">Phần trăm</span>
                </span>
              </label>
              <label class="discount-option">
                <span class="discount-option-content">
                  <input type="radio" name="loaiGiamGiaChoice" value="AMOUNT" <c:if test="${currentLoaiGiamGia eq 'AMOUNT'}">checked</c:if>>
                  <span class="discount-option-text">Số tiền</span>
                </span>
              </label>
            </div>
          </div>

          <div class="form-group full">
            <div id="percentPanel" class="discount-panel" <c:if test="${currentLoaiGiamGia eq 'AMOUNT'}">hidden</c:if>>
              <label for="phanTramGiam">Phần trăm giảm</label>
              <input type="number"
                     id="phanTramGiam"
                     name="phanTramGiam"
                     step="0.01"
                     min="0"
                     max="100"
                     value="${currentPhanTramGiam}">

              <label for="mucGiamToiDa">Mức giảm tối đa</label>
              <input type="number"
                     id="mucGiamToiDa"
                     name="mucGiamToiDa"
                     step="1"
                     min="0"
                     value="${currentMucGiamToiDa}">
              <small>Chỉ dùng cho khuyến mãi theo phần trăm.</small>
            </div>

            <div id="amountPanel" class="discount-panel" <c:if test="${currentLoaiGiamGia ne 'AMOUNT'}">hidden</c:if>>
              <label for="soTienGiam">Số tiền giảm</label>
              <input type="number"
                     id="soTienGiam"
                     name="soTienGiam"
                     step="1"
                     min="0"
                     value="${currentSoTienGiam}">
              <small>Nhập số tiền giảm cố định cho mỗi đơn.</small>
            </div>
          </div>

          <div class="form-group">
            <label for="ngayBatDau">Ngày bắt đầu</label>
            <input type="date"
                   id="ngayBatDau"
                   name="ngayBatDau"
                   value="${currentNgayBatDau}">
          </div>

          <div class="form-group">
            <label for="ngayKetThuc">Ngày kết thúc</label>
            <input type="date"
                   id="ngayKetThuc"
                   name="ngayKetThuc"
                   value="${currentNgayKetThuc}">
          </div>

          <div class="form-group full">
            <label for="moTa">Mô tả</label>
            <textarea id="moTa" name="moTa">${currentMoTa}</textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            ${khuyenMai != null ? "Cập nhật" : "Thêm mới"}
          </button>
          <a href="${pageContext.request.contextPath}/khuyenmai" class="btn btn-back">
            Quay lại
          </a>
        </div>
      </form>
    </div>
  </div>
</div>

<script>
  (function () {
    const hiddenType = document.getElementById('loaiGiamGiaHidden');
    const percentPanel = document.getElementById('percentPanel');
    const amountPanel = document.getElementById('amountPanel');
    const radios = document.querySelectorAll('input[name="loaiGiamGiaChoice"]');
    const percentInput = document.getElementById('phanTramGiam');
    const maxInput = document.getElementById('mucGiamToiDa');
    const amountInput = document.getElementById('soTienGiam');

    function applyMode(mode) {
      const isAmount = mode === 'AMOUNT';
      if (hiddenType) {
        hiddenType.value = isAmount ? 'AMOUNT' : 'PERCENT';
      }
      if (percentPanel) {
        percentPanel.hidden = isAmount;
      }
      if (amountPanel) {
        amountPanel.hidden = !isAmount;
      }
      if (percentInput) {
        percentInput.disabled = isAmount;
      }
      if (maxInput) {
        maxInput.disabled = isAmount;
      }
      if (amountInput) {
        amountInput.disabled = !isAmount;
      }
    }

    radios.forEach(function (radio) {
      radio.addEventListener('change', function () {
        applyMode(this.value);
      });
    });

    applyMode(hiddenType && hiddenType.value ? hiddenType.value : 'PERCENT');
  }());
</script>

</body>
</html>
