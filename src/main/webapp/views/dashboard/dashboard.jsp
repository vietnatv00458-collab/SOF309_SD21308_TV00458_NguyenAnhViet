<%@ page import="entity.TaiKhoan,utils.RoleUtils" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%
  TaiKhoan user = (TaiKhoan) request.getAttribute("user");
  String roleLabel = RoleUtils.displayRole(user != null ? user.getVaiTro() : null);
%>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Dashboard quản lý</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=144">
</head>
<body class="dashboard-page">

<div class="layout">
  <jsp:include page="/views/common/sidebar.jsp"/>

  <div class="main-content">
    <div class="topbar">
      <h1>Dashboard quản lý</h1>
      <div class="user-box">
        Xin chào:
        <b><%= user != null ? user.getTenDangNhap() : "" %></b>
        | Vai trò:
        <b><%= roleLabel %></b>
      </div>
    </div>

    <div class="content-box">
      <div class="page-header">
        <h2>Chức năng quản lý</h2>
        <div class="page-actions">
          <a class="btn btn-add" href="${pageContext.request.contextPath}/sanpham?action=add">
            + Thêm sản phẩm
          </a>
        </div>
      </div>

      <c:if test="${not empty bannerUploadMessage}">
        <div class="alert alert-success">${bannerUploadMessage}</div>
      </c:if>
      <c:if test="${not empty bannerUploadError}">
        <div class="alert alert-error">${bannerUploadError}</div>
      </c:if>

      <div class="dashboard-banner-section">
        <div class="dashboard-banner-head">
          <div>
            <h2>Ảnh banner khuyến mãi</h2>
          </div>
          <form class="dashboard-banner-upload"
                action="${pageContext.request.contextPath}/dashboard"
                method="post"
                enctype="multipart/form-data">
            <input id="dashboardBannerFile"
                   name="bannerImage"
                   type="file"
                   accept="image/png,image/jpeg,image/webp,image/gif"
                   required>
            <select id="dashboardBannerTarget" name="replaceBannerTarget" class="dashboard-banner-select">
              <option value="">Thêm ảnh mới</option>
              <c:forEach var="imageName" items="${bannerEditableImages}">
                <option value="${imageName}">${imageName}</option>
              </c:forEach>
            </select>
            <button class="btn btn-add" type="submit">Lưu banner</button>
          </form>
        </div>

        <div class="dashboard-banner-carousel">
          <div id="dashboardBannerTrack" class="dashboard-banner-track">
            <c:forEach var="image" items="${promoBannerCarouselImages}">
              <figure class="dashboard-banner-slide">
                <img src="${pageContext.request.contextPath}${image}" alt="Ảnh banner khuyến mãi">
              </figure>
            </c:forEach>
          </div>
          <div id="dashboardBannerDots" class="dashboard-banner-dots" aria-label="Điều hướng banner khuyến mãi"></div>
        </div>
      </div>

      <div class="dashboard-cards">
        <div class="card">
          <h3>Khách hàng</h3>
          <p class="card-number">${tongKhachHang}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/khachhang">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Nhân viên</h3>
          <p class="card-number">${tongNhanVien}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/nhanvien">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Tài khoản</h3>
          <p class="card-number">${tongTaiKhoan}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/taikhoan">Danh sách tài khoản</a>
        </div>
        <div class="card">
          <h3>Sản phẩm</h3>
          <p class="card-number">${tongSanPham}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/sanpham">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Lịch sử giá</h3>
          <p class="card-number">${tongLichSuGia}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/lichsugiasanpham">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Khuyến mãi</h3>
          <p class="card-number">--</p>
          <a class="card-link" href="${pageContext.request.contextPath}/khuyenmai">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Hóa đơn</h3>
          <p class="card-number">${tongHoaDon}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/hoadon">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Đặt hàng</h3>
          <p class="card-number">${tongDatHang}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/dathang">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Giỏ hàng</h3>
          <p class="card-number">${tongGioHang}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/giohang">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Tổng doanh thu</h3>
          <p class="card-number">${tongDoanhThu}</p>
          <a class="card-link" href="${pageContext.request.contextPath}/dashboard#doanhthu">Xem chi tiết</a>
        </div>
        <div class="card">
          <h3>Chi tiết hóa đơn</h3>
          <p class="card-number">--</p>
          <a class="card-link" href="${pageContext.request.contextPath}/chitiethoadon">Mở quản lý</a>
        </div>
        <div class="card">
          <h3>Chi tiết giỏ hàng</h3>
          <p class="card-number">--</p>
          <a class="card-link" href="${pageContext.request.contextPath}/chitietgiohang">Mở quản lý</a>
        </div>
      </div>

      <div class="dashboard-section-header" id="thongtin-sanpham">
        <h2>Thông tin sản phẩm</h2>
        <a class="btn btn-add" href="${pageContext.request.contextPath}/sanpham?action=add">
          + Thêm thông tin sản phẩm
        </a>
      </div>

      <div class="table-wrapper">
        <table class="data-table dashboard-product-table">
          <thead>
          <tr>
            <th>Mã SP</th>
            <th>Tên sản phẩm</th>
            <th>Đơn giá</th>
            <th>Thành phần</th>
            <th>Mô tả</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${empty dsSanPhamDashboard}">
              <tr>
                <td colspan="6" class="text-center">Chưa có dữ liệu sản phẩm</td>
              </tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="sp" items="${dsSanPhamDashboard}">
                <tr>
                  <td>${sp.maSP}</td>
                  <td>${sp.tenSP}</td>
                  <td>${empty sp.donGia ? 'Chưa cập nhật' : sp.donGia}</td>
                  <td class="dashboard-product-text">${empty sp.thanhPhan ? 'Chưa cập nhật' : sp.thanhPhan}</td>
                  <td class="dashboard-product-text">${empty sp.moTa ? 'Chưa cập nhật' : sp.moTa}</td>
                  <td class="text-center">
                    <a class="btn-action btn-edit"
                       href="${pageContext.request.contextPath}/sanpham?action=edit&id=${sp.maSP}">
                      Sửa
                    </a>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>

      <div class="dashboard-section-header" id="lichsugia">
        <h2>Lịch sử giá sản phẩm</h2>
        <a class="btn btn-add" href="${pageContext.request.contextPath}/lichsugiasanpham">
          Xem toàn bộ
        </a>
      </div>

      <div class="table-wrapper">
        <table class="data-table dashboard-product-table">
          <thead>
          <tr>
            <th>Mã SP</th>
            <th>Giá cũ</th>
            <th>Giá mới</th>
            <th>Ngày áp dụng</th>
            <th>Lý do</th>
            <th class="text-center">Hành động</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${empty dsLichSuGiaDashboard}">
              <tr>
                <td colspan="6" class="text-center">Chưa có dữ liệu lịch sử giá</td>
              </tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="item" items="${dsLichSuGiaDashboard}">
                <tr>
                  <td>${item.maSP}</td>
                  <td>${item.giaCuText}</td>
                  <td>${item.giaMoiText}</td>
                  <td>${item.ngayApDungText}</td>
                  <td class="dashboard-product-text">${empty item.lyDo ? 'Không có lý do' : item.lyDo}</td>
                  <td class="text-center">
                    <a class="btn-action btn-edit"
                       href="${pageContext.request.contextPath}/lichsugiasanpham?action=edit&id=${item.maLSGia}">
                      Sửa
                    </a>
                    <a class="btn-action btn-delete"
                       href="${pageContext.request.contextPath}/lichsugiasanpham?action=delete&id=${item.maLSGia}"
                       onclick="return confirm('Bạn có chắc muốn xóa lịch sử giá ${item.maLSGia} không?');">
                      Xóa
                    </a>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>

      <div class="dashboard-section-header" id="doanhthu">
        <h2>Doanh thu theo tháng</h2>
      </div>

      <div class="table-wrapper">
        <table class="data-table revenue-table">
          <thead>
          <tr>
            <th>Tháng</th>
            <th class="text-right">Doanh thu</th>
          </tr>
          </thead>
          <tbody>
          <c:choose>
            <c:when test="${empty doanhThuTheoThang}">
              <tr>
                <td colspan="2" class="text-center">Chưa có dữ liệu doanh thu theo tháng</td>
              </tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="item" items="${doanhThuTheoThang}">
                <tr>
                  <td>${item.monthLabel}</td>
                  <td class="text-right">${item.revenueText}</td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<script>
  (function () {
    const track = document.getElementById('dashboardBannerTrack');
    const dots = document.getElementById('dashboardBannerDots');

    if (!track || !dots) {
      return;
    }

    const slides = Array.from(track.querySelectorAll('.dashboard-banner-slide'));
    if (slides.length === 0) {
      return;
    }

    function getPageSize() {
      return window.matchMedia('(max-width: 768px)').matches ? 1 : 2;
    }

    function getPageCount() {
      return Math.max(1, Math.ceil(slides.length / getPageSize()));
    }

    function getStep() {
      const first = slides[0];
      const slideWidth = first ? first.getBoundingClientRect().width : track.clientWidth;
      const gap = parseFloat(window.getComputedStyle(track).columnGap || window.getComputedStyle(track).gap || '0') || 0;
      const pageSize = getPageSize();
      return slideWidth * pageSize + gap * Math.max(0, pageSize - 1);
    }

    function getPageIndex() {
      const step = getStep();
      if (!step) {
        return 0;
      }

      return Math.round(track.scrollLeft / step);
    }

    function renderDots() {
      const pageCount = getPageCount();
      dots.innerHTML = '';

      for (let pageIndex = 0; pageIndex < pageCount; pageIndex++) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'dashboard-banner-dot';
        button.dataset.carouselPage = String(pageIndex);
        button.setAttribute('aria-label', 'Chuyển tới cụm ảnh ' + (pageIndex + 1));
        dots.appendChild(button);
      }
    }

    function updateDots() {
      const buttons = Array.from(dots.querySelectorAll('.dashboard-banner-dot'));
      if (buttons.length === 0) {
        return;
      }

      const activePage = Math.min(buttons.length - 1, getPageIndex());
      buttons.forEach(function (button, pageIndex) {
        button.classList.toggle('is-active', pageIndex === activePage);
        button.setAttribute('aria-current', pageIndex === activePage ? 'true' : 'false');
      });
    }

    function goToPage(pageIndex) {
      const pageCount = getPageCount();
      if (!pageCount) {
        return;
      }

      const targetPage = Math.max(0, Math.min(pageIndex, pageCount - 1));
      track.scrollTo({ left: targetPage * getStep(), behavior: 'smooth' });
      window.setTimeout(updateDots, 220);
    }

    dots.addEventListener('click', function (event) {
      const button = event.target.closest('button[data-carousel-page]');
      if (!button) {
        return;
      }

      const pageIndex = parseInt(button.dataset.carouselPage, 10);
      if (Number.isNaN(pageIndex)) {
        return;
      }

      goToPage(pageIndex);
    });

    track.addEventListener('scroll', function () {
      window.requestAnimationFrame(updateDots);
    }, { passive: true });

    window.addEventListener('resize', function () {
      renderDots();
      updateDots();
    });

    renderDots();
    updateDots();
  }());
</script>

</body>
</html>
