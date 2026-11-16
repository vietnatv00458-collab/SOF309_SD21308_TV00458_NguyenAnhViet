﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<%@ page import="entity.TaiKhoan" %>
<%@ page import="entity.KhuyenMai" %>
<%@ page import="entity.SanPham" %>
<%@ page import="utils.PaymentSessionStore" %>
<%@ page import="utils.SePayConfig" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private String htmlEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String jsEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    private String formatVnd(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(value != null ? value : BigDecimal.ZERO) + " đ";
    }

    private String formatPercent(Double value) {
        if (value == null) {
            return "0";
        }

        BigDecimal percent = BigDecimal.valueOf(value).stripTrailingZeros();
        return percent.toPlainString();
    }

    private String categoryKey(String loaiSP) {
        if (loaiSP == null) {
            return "tra-sua";
        }

        String lower = loaiSP.toLowerCase(new Locale("vi", "VN"));
        if (lower.contains("bánh") || lower.contains("banh")) {
            return "banh";
        }
        if (lower.contains("sinh")) {
            return "sinh-to";
        }
        if (lower.contains("cafe") || lower.contains("coffee") || lower.contains("cà phê") || lower.contains("ca phe")) {
            return "cafe";
        }
        return "tra-sua";
    }

    private String categoryLabel(String loaiSP) {
        String category = categoryKey(loaiSP);
        if ("banh".equals(category)) {
            return "Bánh";
        }
        if ("cafe".equals(category)) {
            return "Cà phê";
        }
        if ("sinh-to".equals(category)) {
            return "Sinh tố";
        }
        return "Trà sữa";
    }

    private String fallbackImage(String loaiSP) {
        String category = categoryKey(loaiSP);
        if ("banh".equals(category)) {
            return "promo-tiramisu.png";
        }
        if ("cafe".equals(category)) {
            return "ca-phe-sua.png";
        }
        if ("sinh-to".equals(category)) {
            return "product-avocado.png";
        }
        return "product-boba-milk-tea.png";
    }

    private String productImage(SanPham sp) {
        if (sp == null) {
            return "product-boba-milk-tea.png";
        }

        String uploaded = sp.getHinhAnh();
        if (uploaded != null && !uploaded.trim().isEmpty()) {
            String trimmed = uploaded.trim();
            return trimmed.startsWith("/") ? trimmed : (trimmed.startsWith("uploads/") ? "/" + trimmed : trimmed);
        }

        String maSP = sp.getMaSP() != null ? sp.getMaSP().trim().toUpperCase(Locale.ROOT) : "";
        String tenSP = sp.getTenSP() != null ? sp.getTenSP().trim().toLowerCase(new Locale("vi", "VN")) : "";

        if (tenSP.contains("tiramisu") || maSP.startsWith("BK01")) {
            return "promo-tiramisu.png";
        }
        if (tenSP.contains("bánh") || tenSP.contains("banh") || maSP.startsWith("BK")) {
            return "promo-coffee.png";
        }
        if (tenSP.contains("bơ") || tenSP.contains("bo")) {
            return "product-avocado.png";
        }
        if (tenSP.contains("dâu") || tenSP.contains("dau")) {
            return "product-strawberry.png";
        }
        if (tenSP.contains("matcha")) {
            return "product-matcha-boba.png";
        }
        if (tenSP.contains("trà sữa") || tenSP.contains("tra sua")) {
            return "product-boba-milk-tea.png";
        }
        if (tenSP.contains("vải") || tenSP.contains("vai")) {
            return "product-lychee-tea.png";
        }
        if (tenSP.contains("cam")) {
            return tenSP.contains("nước") || tenSP.contains("nuoc") ? "product-orange.png" : "product-orange-tea.png";
        }
        if (tenSP.contains("chanh") || tenSP.contains("đào") || tenSP.contains("dao")) {
            return "product-citrus.png";
        }
        if (tenSP.contains("cacao") || tenSP.contains("chocolate") || tenSP.contains("mocha")) {
            return "product-cacao.png";
        }
        if (tenSP.contains("đen") || tenSP.contains("den") || tenSP.contains("espresso")) {
            return "ca-phe-den.png";
        }
        if (tenSP.contains("sữa") || tenSP.contains("sua") || tenSP.contains("latte") || tenSP.contains("cappuccino")) {
            return "ca-phe-sua.png";
        }

        return fallbackImage(sp.getLoaiSP());
    }
%>
<%
    TaiKhoan user = (TaiKhoan) request.getAttribute("user");
    entity.KhachHang customerProfile = (entity.KhachHang) request.getAttribute("customerProfile");
    String customerDisplayName = (String) request.getAttribute("customerDisplayName");
    String cartOwnerKey = (String) request.getAttribute("cartOwnerKey");
    String customerProfileAction = (String) request.getAttribute("customerProfileAction");
    String customerDeleteUrl = (String) request.getAttribute("customerDeleteUrl");
    boolean loggedIn = user != null;
    String loginUrl = request.getContextPath() + "/login?returnUrl=/customer-shop";
    if (customerDisplayName == null || customerDisplayName.trim().isEmpty()) {
        customerDisplayName = "Khách hàng";
    }
    if (cartOwnerKey == null || cartOwnerKey.trim().isEmpty()) {
        cartOwnerKey = "guest";
    }
    if (customerProfileAction == null || customerProfileAction.trim().isEmpty()) {
        customerProfileAction = customerProfile != null ? "update" : "create";
    }
    if (customerDeleteUrl == null || customerDeleteUrl.trim().isEmpty()) {
        customerDeleteUrl = request.getContextPath() + "/logout";
    }
    String customerMaKH = user != null && user.getMaKH() != null ? user.getMaKH() : "";
    String customerHoTen = customerProfile != null && customerProfile.getHoTen() != null ? customerProfile.getHoTen() : "";
    String customerSdt = customerProfile != null && customerProfile.getSdt() != null ? customerProfile.getSdt() : "";
    String customerEmail = customerProfile != null && customerProfile.getEmail() != null ? customerProfile.getEmail() : "";
    String customerDiaChi = customerProfile != null && customerProfile.getDiaChi() != null ? customerProfile.getDiaChi() : "";
    String customerPhone = customerSdt != null ? customerSdt : "";
    String customerRole = user != null && user.getVaiTro() != null ? user.getVaiTro() : "";
    String customerStatus = user != null && user.getTrangThai() != null ? (user.getTrangThai() ? "Hoạt động" : "Tạm khóa") : "Không xác định";
    String sepayBankName = SePayConfig.getBankName();
    String sepayBankCode = SePayConfig.getBankCode();
    String sepayAccountNumber = SePayConfig.getAccountNumber();
    String sepayAccountName = SePayConfig.getAccountName();
    String sepayPaymentNote = SePayConfig.getPaymentNote();
    long paymentTimeoutMillis = PaymentSessionStore.TIMEOUT_MILLIS;
    List<SanPham> dsSanPham = (List<SanPham>) request.getAttribute("dsSanPham");
    List<KhuyenMai> dsKhuyenMai = (List<KhuyenMai>) request.getAttribute("dsKhuyenMai");
    List<String> promoBannerImages = (List<String>) request.getAttribute("promoBannerImages");
    if (promoBannerImages == null || promoBannerImages.isEmpty()) {
        promoBannerImages = java.util.Arrays.asList(
                "/images/promo-voucher-1.png",
                "/images/promo-voucher-3.png"
        );
    }
    List<String> promoBannerCarouselImages = (List<String>) request.getAttribute("promoBannerCarouselImages");
    if (promoBannerCarouselImages == null || promoBannerCarouselImages.isEmpty()) {
        promoBannerCarouselImages = promoBannerImages;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giao Diện Cửa Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=144">
    <style>
        .shop-container {
            max-width: 1600px;
            margin: 0 auto;
            padding: 0 20px 56px;
        }

        .customer-shop-page {
            background: #f5efe6;
            color: #2c2018;
            overflow-x: hidden;
        }

        .shop-navbar {
            background: rgba(255, 251, 245, 0.96);
            border-bottom: 1px solid #e4d1b8;
            box-shadow: 0 1px 0 rgba(139, 90, 56, 0.04);
        }

        .shop-navbar-top {
            max-width: 1600px;
            margin: 0 auto;
            padding: 10px 18px 12px;
            display: grid;
            grid-template-columns: auto minmax(320px, 1fr) auto;
            align-items: center;
            gap: 22px;
        }

        .shop-logo-placeholder {
            display: flex;
            align-items: center;
        }

        .shop-logo-image {
            width: 68px;
            max-width: 68px;
            height: auto;
            display: block;
            object-fit: contain;
        }

        .shop-search-row {
            width: 100%;
        }

        .shop-search-box {
            width: 100%;
            max-width: 394px;
            height: 40px;
            border-radius: 999px;
            background: #efe4d5;
            display: flex;
            align-items: center;
            padding: 0 18px;
        }

        .shop-search-box input {
            width: 100%;
            border: 0;
            background: transparent;
            outline: none;
            color: #5f4a3a;
            font-size: 15px;
        }

        .shop-search-box input::placeholder {
            color: #8d7a68;
        }

        .shop-nav-right {
            display: flex;
            align-items: center;
            gap: 18px;
            justify-self: end;
            position: relative;
        }

        .shop-user-menu {
            position: relative;
        }

        .shop-user-info {
            border: 1px solid #d9c2a4;
            background: #fff8ef;
            color: #7a4d2e;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            padding: 10px 14px;
            border-radius: 14px;
            font-family: inherit;
            font-size: 14px;
            font-weight: 700;
            line-height: 1.2;
            cursor: pointer;
            white-space: nowrap;
            box-shadow: 0 4px 10px rgba(139, 90, 56, 0.08);
        }

        .shop-user-info:hover {
            border-color: #b7885f;
            box-shadow: 0 6px 14px rgba(139, 90, 56, 0.12);
        }

        .shop-user-avatar {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: #8b5a38;
            color: #fff;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }

        .shop-user-avatar svg {
            width: 16px;
            height: 16px;
            fill: none;
            stroke: currentColor;
            stroke-width: 2;
        }

        .shop-user-label {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            min-width: 0;
        }

        .shop-user-name {
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 180px;
        }

        .shop-user-chevron {
            width: 14px;
            height: 14px;
            stroke: currentColor;
            fill: none;
            stroke-width: 2;
            flex-shrink: 0;
        }

        .shop-user-panel {
            position: absolute;
            top: calc(100% + 12px);
            right: 0;
            width: min(460px, calc(100vw - 24px));
            background: #fffaf2;
            border: 1px solid #e5d1b5;
            border-radius: 18px;
            box-shadow: 0 18px 38px rgba(139, 90, 56, 0.16);
            overflow: hidden;
            z-index: 45;
            display: none;
        }

        .shop-user-panel.is-open {
            display: block;
        }

        .shop-user-panel-header {
            padding: 16px 16px 14px;
            background: #f7ebda;
            border-bottom: 1px solid #e6d3bb;
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 12px;
        }

        .shop-user-panel-kicker {
            margin: 0;
            color: #8b5a38;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0;
        }

        .shop-user-panel-title {
            margin: 4px 0 0;
            color: #3b2519;
            font-size: 18px;
            line-height: 1.25;
            font-weight: 700;
        }

        .shop-user-panel-close {
            width: 34px;
            height: 34px;
            border: 0;
            border-radius: 10px;
            background: #f0ddc8;
            color: #7a4d2e;
            font-family: inherit;
            font-size: 20px;
            line-height: 1;
            cursor: pointer;
            flex-shrink: 0;
        }

        .shop-user-panel-body {
            padding: 16px;
        }

        .shop-user-meta {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px;
            margin-bottom: 14px;
        }

        .shop-user-meta-item {
            padding: 10px 12px;
            border-radius: 12px;
            background: #fffdf8;
            border: 1px solid #ead8c3;
        }

        .shop-user-meta-label {
            display: block;
            margin-bottom: 4px;
            color: #6b7280;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
        }

        .shop-user-meta-value {
            color: #1f2937;
            font-size: 14px;
            font-weight: 700;
            word-break: break-word;
        }

        .shop-user-form {
            display: grid;
            gap: 12px;
        }

        .shop-user-fields {
            display: grid;
            gap: 12px;
        }

        .shop-user-field {
            display: grid;
            gap: 6px;
        }

        .shop-user-field label {
            color: #5c4737;
            font-size: 13px;
            font-weight: 700;
        }

        .shop-user-field input {
            width: 100%;
            height: 42px;
            border: 1px solid #dcc7ae;
            border-radius: 12px;
            padding: 0 12px;
            background: #fffefb;
            color: #3b2519;
            font-family: inherit;
            font-size: 14px;
            outline: none;
        }

        .shop-user-field input:focus {
            border-color: #b7885f;
            box-shadow: 0 0 0 3px rgba(139, 90, 56, 0.12);
        }

        .shop-user-field input[readonly] {
            background: #fbf5ec;
            color: #7a6756;
        }

        .shop-user-actions {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 10px;
            margin-top: 4px;
        }

        .shop-user-action {
            height: 42px;
            border: 0;
            border-radius: 12px;
            font-family: inherit;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
        }

        .shop-user-action-save {
            background: #8b5a38;
            color: #fff;
        }

        .shop-user-action-delete {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: #f3ece3;
            color: #a54b2a;
            text-decoration: none;
        }

        .shop-user-action-logout {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: #efe4d5;
            color: #7a4d2e;
            text-decoration: none;
        }

        .shop-user-action-delete:hover,
        .shop-user-action-logout:hover,
        .shop-user-action-save:hover {
            filter: brightness(0.98);
        }

        .shop-user-empty {
            padding: 16px;
            color: #4b5563;
            font-size: 14px;
            line-height: 1.5;
        }

        .shop-user-panel.is-open {
            display: block;
        }

        .shop-user-panel-footer-note {
            margin: 2px 0 0;
            color: #7f6a58;
            font-size: 12px;
            line-height: 1.4;
        }

        .shop-user-action[disabled] {
            opacity: 0.45;
            cursor: not-allowed;
        }

        .shop-user-panel .shop-user-action-delete[aria-disabled="true"] {
            pointer-events: none;
            opacity: 0.45;
        }

        .shop-user-info strong {
            font-weight: 700;
        }

        .shop-delivery-section {
            display: grid;
            gap: 12px;
            padding: 14px 16px;
            border: 1px solid #e5d1b5;
            border-radius: 16px;
            background: #fffdf8;
        }

        .shop-delivery-section-head {
            display: grid;
            gap: 4px;
        }

        .shop-delivery-section-title {
            color: #3b2519;
            font-size: 15px;
            font-weight: 700;
            line-height: 1.35;
        }

        .shop-delivery-toggle {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px;
        }

        .shop-delivery-option {
            height: 42px;
            border: 1px solid #dcc7ae;
            border-radius: 12px;
            background: #fffefb;
            color: #6a4a35;
            font-family: inherit;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
        }

        .shop-delivery-option.is-active {
            border-color: #8b5a38;
            background: #8b5a38;
            color: #fff;
        }

        .shop-delivery-address {
            display: grid;
            gap: 8px;
        }

        .shop-delivery-address label {
            color: #5c4737;
            font-size: 13px;
            font-weight: 700;
        }

        .shop-delivery-address input {
            width: 100%;
            height: 42px;
            border: 1px solid #dcc7ae;
            border-radius: 12px;
            padding: 0 12px;
            background: #fffefb;
            color: #3b2519;
            font-family: inherit;
            font-size: 14px;
            outline: none;
        }

        .shop-delivery-address input:focus {
            border-color: #b7885f;
            box-shadow: 0 0 0 3px rgba(139, 90, 56, 0.12);
        }

        .shop-delivery-hint {
            margin: 0;
            color: #7f6a58;
            font-size: 12px;
            line-height: 1.45;
        }

        .shop-nav-icons {
            display: flex;
            align-items: center;
            gap: 18px;
            color: #8b5a38;
        }

        .shop-nav-icons a {
            color: #8b5a38;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }

        .shop-nav-icon {
            width: 26px;
            height: 26px;
            stroke: currentColor;
            fill: none;
            stroke-width: 1.8;
        }

        .shop-navbar-menu {
            max-width: 1600px;
            margin: 0 auto;
            padding: 12px 18px 13px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 66px;
            border-top: 1px solid #e6d3bb;
        }

        .shop-navbar-menu a,
        .shop-category-toggle,
        .shop-category-menu button {
            color: #6f5b4a;
            font-size: 15px;
            font-weight: 400;
            letter-spacing: 0;
            text-transform: uppercase;
        }

        .shop-category-dropdown {
            position: relative;
        }

        .shop-category-toggle {
            display: inline-flex;
            align-items: center;
            gap: 7px;
            border: 0;
            background: transparent;
            font-family: inherit;
            cursor: pointer;
            padding: 0;
        }

        .shop-category-toggle svg {
            width: 14px;
            height: 14px;
            stroke: currentColor;
            stroke-width: 2;
            fill: none;
            transition: transform 0.2s ease;
        }

        .shop-category-dropdown.is-open .shop-category-toggle svg {
            transform: rotate(180deg);
        }

        .shop-category-menu {
            position: absolute;
            top: calc(100% + 14px);
            left: 50%;
            z-index: 20;
            min-width: 190px;
            padding: 8px;
            background: #fffbf5;
            border: 1px solid #e2c9a9;
            border-radius: 14px;
            box-shadow: 0 16px 34px rgba(74, 43, 24, 0.16);
            opacity: 0;
            visibility: hidden;
            transform: translate(-50%, -6px);
            transition: opacity 0.2s ease, transform 0.2s ease, visibility 0.2s ease;
        }

        .shop-category-dropdown.is-open .shop-category-menu {
            opacity: 1;
            visibility: visible;
            transform: translate(-50%, 0);
        }

        .shop-category-menu button {
            width: 100%;
            border: 0;
            background: transparent;
            border-radius: 10px;
            padding: 11px 12px;
            text-align: left;
            font-family: inherit;
            cursor: pointer;
            color: #6a4a35;
        }

        .shop-category-menu button:hover,
        .shop-category-menu button.is-active {
            background: #f3e7d7;
            color: #8b5a38;
            font-weight: 700;
        }

        .banner-container {
            width: 100%;
            margin: 30px auto 0;
            padding: 0 0 12px;
        }

        .banner-content {
            position: relative;
            width: 88%;
            max-width: 1250px;
            overflow: hidden;
            height: 280px;
            margin: 25px auto 0;
            border-radius: 20px;
            background: #000;
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
            cursor: pointer;
        }

        .banner-content::before {
            content: "";
            position: absolute;
            inset: 0;
            background: rgba(0, 0, 0, 0.25);
            z-index: 1;
            pointer-events: none;
        }

        .banner-slides {
            display: flex;
            width: 100%;
            height: 100%;
            transition: transform 0.45s ease;
            will-change: transform;
        }

        .banner-slide {
            flex: 0 0 100%;
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            position: relative;
            z-index: 0;
        }

        .banner-slide-one,
        .banner-slide-two {
            object-position: center center;
        }

        .banner-nav {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            width: 44px;
            height: 44px;
            border: 0;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.72);
            color: #3f2a17;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            z-index: 3;
            box-shadow: 0 8px 18px rgba(0, 0, 0, 0.12);
            display: none;
        }

        .banner-nav svg {
            width: 22px;
            height: 22px;
        }

        .banner-nav-prev {
            left: 18px;
        }

        .banner-nav-next {
            right: 18px;
        }

        .banner-indicators {
            position: absolute;
            left: 50%;
            bottom: 18px;
            transform: translateX(-50%);
            display: flex;
            align-items: center;
            gap: 10px;
            z-index: 3;
        }

        .banner-dot {
            width: 12px;
            height: 12px;
            padding: 0;
            border: 1px solid rgba(0, 0, 0, 0.55);
            border-radius: 999px;
            background: rgba(255, 255, 255, 0.9);
            cursor: pointer;
        }

        .banner-dot.is-active {
            background: #111827;
        }

        .banner-dot:focus-visible,
        .banner-nav:focus-visible,
        .btn-order:focus-visible,
        .btn-view-more:focus-visible,
        .shop-category-toggle:focus-visible,
        .shop-category-menu button:focus-visible {
            outline: 2px solid #b7885f;
            outline-offset: 2px;
        }

        .best-seller-section {
            padding: 18px 0 10px;
        }

        .best-seller-title {
            margin: 0 0 14px;
            padding-left: 165px;
            color: #3b2519;
            text-align: left;
            font-size: 22px;
            line-height: 1.15;
            font-weight: 700;
            letter-spacing: 0;
        }

        .mb-1 {
            margin-bottom: 0.25rem;
        }

        .best-seller-grid {
            display: grid;
            grid-template-columns: repeat(5, minmax(0, 240px));
            justify-content: center;
            gap: 16px;
            align-items: stretch;
        }

        .best-seller-carousel {
            position: relative;
        }

        .best-seller-viewport {
            overflow: hidden;
        }

        .best-seller-track {
            display: flex;
            transition: transform 0.35s ease;
            will-change: transform;
        }

        .best-seller-slide {
            flex: 0 0 100%;
        }

        .best-seller-slide .best-seller-grid {
            width: 100%;
        }

        .product-carousel-nav {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            width: 44px;
            height: 44px;
            border: 0;
            border-radius: 50%;
            background: rgba(255, 251, 245, 0.96);
            color: #8b5a38;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 6px 16px rgba(139, 90, 56, 0.14);
            cursor: pointer;
            z-index: 4;
        }

        .product-carousel-nav:hover {
            background: #fff;
        }

        .product-carousel-nav:disabled {
            opacity: 0.35;
            cursor: not-allowed;
        }

        .product-carousel-nav-prev {
            left: -12px;
        }

        .product-carousel-nav-next {
            right: -12px;
        }

        .product-carousel-nav svg {
            width: 22px;
            height: 22px;
        }

        .product-card {
            background: #fffaf4;
            border: 1px solid #ead9c4;
            border-radius: 16px;
            padding: 14px;
            text-align: left;
            overflow: hidden;
            box-shadow: 0 4px 12px rgba(139, 90, 56, 0.08);
            display: flex;
            flex-direction: column;
            min-width: 0;
            transition: 0.3s;
        }

        .product-card:hover {
            transform: translateY(-8px);
            box-shadow: 0 10px 25px rgba(139, 90, 56, 0.18);
        }

        .product-visual {
            position: relative;
            width: 100%;
            height: 190px;
            border-radius: 14px;
            overflow: hidden;
            background: #f4eadf;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .product-visual-frame {
            width: 100%;
            height: 100%;
            min-height: 190px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .product-visual-frame img {
            width: 78%;
            height: 78%;
            object-fit: contain;
            border-radius: 8px;
            display: block;
        }

        .product-visual-trigger {
            width: 100%;
            height: 100%;
            border: 0;
            padding: 0;
            background: transparent;
            cursor: pointer;
            display: flex;
            align-items: stretch;
            justify-content: center;
        }

        .product-visual-trigger:focus-visible {
            outline: 2px solid #c89a54;
            outline-offset: 2px;
            border-radius: 14px;
        }

        .product-visual-trigger .product-visual-frame {
            width: 100%;
        }

        .best-seller-badge {
            position: absolute;
            left: 0;
            right: 0;
            bottom: 0;
            background: #c11c1c;
            color: #fff;
            text-align: center;
            font-size: 14px;
            font-weight: 700;
            padding: 10px 0;
        }

        .product-info {
            padding: 0;
            display: flex;
            flex-direction: column;
        }

        .product-name {
            margin: 14px 0 0;
            color: #2f2118;
            font-size: 20px;
            line-height: 1.25;
            font-weight: 700;
            text-transform: uppercase;
            min-height: auto;
        }

        .product-price {
            margin: 10px 0 0;
            color: #8b5a38;
            font-size: 24px;
            font-weight: 700;
        }

        .btn-order {
            width: 100%;
            height: 44px;
            border: none;
            border-radius: 10px;
            background: #8b5a38;
            color: #fff;
            text-decoration: none;
            font-family: inherit;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .btn-order-icon {
            width: 16px;
            height: 16px;
            flex-shrink: 0;
        }

        .product-detail-backdrop {
            position: fixed;
            inset: 0;
            background: rgba(38, 24, 15, 0.56);
            z-index: 74;
        }

        .product-detail-modal {
            position: fixed;
            inset: 0;
            z-index: 75;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 18px;
        }

        .product-detail-modal.is-open {
            display: flex;
        }

        .product-detail-panel {
            position: relative;
            width: min(960px, 100%);
            max-height: min(90vh, 900px);
            overflow: auto;
            border: 1px solid #e6d3bb;
            border-radius: 18px;
            background: #fffaf4;
            box-shadow: 0 22px 56px rgba(57, 35, 20, 0.28);
            z-index: 76;
        }

        .product-detail-panel-header {
            padding: 18px 20px 16px;
            border-bottom: 1px solid #e6d3bb;
            background: #f7ebda;
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 14px;
        }

        .product-detail-panel-title {
            margin: 0;
            color: #3b2519;
            font-size: 20px;
            line-height: 1.25;
            font-weight: 700;
        }

        .product-detail-panel-subtitle {
            margin: 6px 0 0;
            color: #7f6a58;
            font-size: 13px;
            line-height: 1.45;
        }

        .product-detail-panel-close {
            width: 36px;
            height: 36px;
            border: 0;
            border-radius: 10px;
            background: #f0ddc8;
            color: #7a4d2e;
            font-family: inherit;
            font-size: 20px;
            line-height: 1;
            cursor: pointer;
            flex-shrink: 0;
        }

        .product-detail-panel-body {
            padding: 20px;
            display: grid;
            grid-template-columns: minmax(260px, 340px) minmax(0, 1fr);
            gap: 18px;
        }

        .product-detail-visual {
            min-height: 320px;
            border: 1px solid #ead9c4;
            border-radius: 16px;
            background: #fffdf9;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 16px;
        }

        .product-detail-visual img {
            width: 100%;
            height: 100%;
            max-height: 320px;
            object-fit: contain;
            border-radius: 12px;
            background: #fff;
        }

        .product-detail-content {
            display: grid;
            gap: 16px;
        }

        .product-detail-meta-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 12px;
        }

        .product-detail-meta-item {
            border: 1px solid #ead9c4;
            border-radius: 12px;
            background: #fffdf9;
            padding: 12px 14px;
        }

        .product-detail-meta-label {
            display: block;
            color: #7f6a58;
            font-size: 12px;
            line-height: 1.25;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0;
        }

        .product-detail-meta-value {
            display: block;
            margin-top: 6px;
            color: #3b2519;
            font-size: 15px;
            line-height: 1.4;
            font-weight: 700;
            white-space: pre-wrap;
        }

        .product-detail-section {
            padding: 14px 16px;
            border: 1px solid #ead9c4;
            border-radius: 12px;
            background: #fffdf9;
        }

        .product-detail-section h4 {
            margin: 0 0 10px;
            color: #3b2519;
            font-size: 15px;
            line-height: 1.25;
            font-weight: 700;
        }

        .product-detail-text {
            margin: 0;
            color: #5c4737;
            font-size: 14px;
            line-height: 1.6;
            white-space: pre-wrap;
        }

        .product-detail-actions {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 12px;
        }

        .product-detail-action {
            height: 46px;
            border-radius: 8px;
            border: 1px solid transparent;
            font-family: inherit;
            font-size: 15px;
            line-height: 1;
            font-weight: 700;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .product-detail-action svg {
            width: 16px;
            height: 16px;
            flex-shrink: 0;
        }

        .product-detail-action-secondary {
            background: #fffaf4;
            border-color: #dcc7ae;
            color: #7a4d2e;
        }

        .product-detail-action-primary {
            background: #8b5a38;
            color: #fff;
        }

        @media (max-width: 768px) {
            .product-detail-panel-body {
                grid-template-columns: 1fr;
            }

            .product-detail-meta-grid {
                grid-template-columns: 1fr;
            }

            .product-detail-visual {
                min-height: 240px;
            }

            .product-detail-actions {
                grid-template-columns: 1fr;
            }
        }

        .view-more-container {
            display: flex;
            justify-content: center;
            margin-top: 14px;
        }

        .btn-view-more {
            border: 1px solid #b7885f;
            background: #fffaf4;
            color: #7a4d2e;
            border-radius: 4px;
            padding: 9px 18px;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
        }

        .shop-cart-toggle {
            position: relative;
            width: 42px;
            height: 42px;
            border: 1px solid #dcc7ae;
            border-radius: 12px;
            background: #fffaf4;
            color: #8b5a38;
            font-family: inherit;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            flex-shrink: 0;
        }

        .shop-mobile-actions {
            display: none;
        }

        .shop-cart-toggle svg {
            width: 20px;
            height: 20px;
            fill: none;
            stroke: currentColor;
            stroke-width: 1.9;
        }

        .shop-cart-badge {
            position: absolute;
            top: -6px;
            right: -6px;
            min-width: 20px;
            height: 20px;
            padding: 0 5px;
            border-radius: 999px;
            background: #c11c1c;
            color: #fff;
            font-size: 12px;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.18);
        }

        .shop-cart-backdrop {
            position: fixed;
            inset: 0;
            background: rgba(17, 24, 39, 0.45);
            z-index: 40;
        }

        .shop-cart-drawer {
            position: fixed;
            top: 0;
            right: 0;
            width: min(420px, 100%);
            height: 100vh;
            background: #fff;
            box-shadow: -18px 0 40px rgba(0, 0, 0, 0.18);
            transform: translateX(100%);
            transition: transform 0.28s ease;
            z-index: 41;
            display: flex;
            flex-direction: column;
        }

        .shop-cart-drawer.is-open {
            transform: translateX(0);
        }

        .shop-cart-header {
            padding: 18px 18px 14px;
            border-bottom: 1px solid #e6d3bb;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
        }

        .shop-cart-title {
            margin: 0;
            color: #3b2519;
            font-size: 18px;
            line-height: 1.2;
            font-weight: 700;
        }

        .shop-cart-close {
            width: 36px;
            height: 36px;
            border: 0;
            border-radius: 10px;
            background: #f0ddc8;
            color: #7a4d2e;
            font-family: inherit;
            font-size: 20px;
            line-height: 1;
            cursor: pointer;
        }

        .shop-cart-body {
            flex: 1;
            overflow: auto;
            padding: 16px 18px;
        }

        .shop-cart-empty {
            color: #7f6a58;
            font-size: 14px;
            padding: 8px 0;
        }

        .shop-cart-item {
            display: grid;
            grid-template-columns: 74px minmax(0, 1fr);
            gap: 12px;
            padding: 12px 0;
            border-bottom: 1px solid #f0f0f0;
        }

        .shop-cart-thumb {
            width: 74px;
            height: 74px;
            border-radius: 12px;
            overflow: hidden;
            background: #f4eadf;
            flex-shrink: 0;
        }

        .shop-cart-thumb img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }

        .shop-cart-body h3 {
            margin: 0;
            color: #2f2118;
            font-size: 15px;
            line-height: 1.3;
            font-weight: 700;
        }

        .shop-cart-price {
            margin: 4px 0 10px;
            color: #8b5a38;
            font-size: 14px;
            font-weight: 700;
        }

        .shop-cart-controls {
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
        }

        .shop-cart-qty-btn {
            width: 30px;
            height: 30px;
            border: 1px solid #dcc7ae;
            border-radius: 8px;
            background: #fffaf4;
            color: #3b2519;
            font-family: inherit;
            cursor: pointer;
            font-size: 18px;
            line-height: 1;
        }

        .shop-cart-qty {
            min-width: 22px;
            text-align: center;
            font-weight: 700;
        }

        .shop-cart-remove {
            border: 0;
            background: transparent;
            color: #a54b2a;
            font-family: inherit;
            font-size: 13px;
            font-weight: 700;
            cursor: pointer;
            padding: 0;
        }

        .shop-cart-footer {
            border-top: 1px solid #e6d3bb;
            padding: 14px 18px 18px;
            background: #fffaf4;
            display: grid;
            gap: 12px;
        }

        .shop-cart-coupon {
            display: grid;
            gap: 8px;
        }

        .shop-cart-coupon label {
            color: #5c4737;
            font-size: 13px;
            font-weight: 700;
        }

        .shop-coupon-row {
            display: grid;
            grid-template-columns: minmax(0, 1fr) auto auto;
            gap: 8px;
            align-items: stretch;
        }

        .shop-coupon-input {
            width: 100%;
            height: 42px;
            border: 1px solid #dcc7ae;
            border-radius: 10px;
            background: #fffefb;
            color: #3b2519;
            font-family: inherit;
            font-size: 14px;
            padding: 0 12px;
            outline: none;
        }

        .shop-coupon-input:focus {
            border-color: #b7885f;
            box-shadow: 0 0 0 3px rgba(139, 90, 56, 0.12);
        }

        .shop-coupon-button {
            min-width: 84px;
            height: 42px;
            border-radius: 10px;
            border: 1px solid transparent;
            font-family: inherit;
            font-size: 13px;
            font-weight: 700;
            padding: 0 12px;
            cursor: pointer;
            white-space: nowrap;
        }

        .shop-coupon-button-primary {
            background: #8b5a38;
            color: #fff;
        }

        .shop-coupon-button-primary:hover {
            background: #754b2f;
        }

        .shop-coupon-button-secondary {
            border-color: #dcc7ae;
            background: #fffaf4;
            color: #a54b2a;
        }

        .shop-coupon-button-secondary:hover {
            background: #fff5e9;
        }

        .shop-coupon-button:disabled {
            opacity: 0.6;
            cursor: default;
        }

        .shop-coupon-select {
            display: none;
        }

        .shop-coupon-hint {
            margin: 0;
            color: #7f6a58;
            font-size: 12px;
            line-height: 1.4;
        }

        .shop-cart-summary {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            color: #3b2519;
            font-size: 15px;
            font-weight: 700;
        }

        .shop-cart-summary-discount {
            color: #a54b2a;
        }

        .shop-cart-actions {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px;
        }

        .shop-cart-action {
            height: 42px;
            border: 0;
            border-radius: 10px;
            font-family: inherit;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
        }

        .shop-cart-action-primary {
            background: #8b5a38;
            color: #fff;
        }

        .shop-cart-action-secondary {
            background: #efe4d5;
            color: #7a4d2e;
        }

        .payment-backdrop {
            position: fixed;
            inset: 0;
            background: rgba(38, 24, 15, 0.56);
            z-index: 70;
        }

        .payment-modal {
            position: fixed;
            inset: 0;
            z-index: 80;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 18px;
        }

        .payment-modal.is-open {
            display: flex;
        }

        .payment-panel {
            width: min(920px, 100%);
            max-height: min(90vh, 920px);
            overflow: auto;
            border: 1px solid #e6d3bb;
            border-radius: 18px;
            background: #fffaf4;
            box-shadow: 0 22px 56px rgba(57, 35, 20, 0.28);
        }

        .payment-panel-header {
            padding: 18px 20px 16px;
            border-bottom: 1px solid #e6d3bb;
            background: #f7ebda;
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 14px;
        }

        .payment-panel-title {
            margin: 0;
            color: #3b2519;
            font-size: 20px;
            line-height: 1.25;
            font-weight: 700;
        }

        .payment-panel-subtitle {
            margin: 6px 0 0;
            color: #7f6a58;
            font-size: 13px;
            line-height: 1.45;
        }

        .payment-panel-close {
            width: 36px;
            height: 36px;
            border: 0;
            border-radius: 10px;
            background: #f0ddc8;
            color: #7a4d2e;
            font-family: inherit;
            font-size: 20px;
            line-height: 1;
            cursor: pointer;
            flex-shrink: 0;
        }

        .payment-panel-body {
            padding: 20px;
            display: grid;
            grid-template-columns: minmax(280px, 1fr) minmax(320px, 1fr);
            gap: 18px;
        }

        .payment-qr-box,
        .payment-config-box {
            border: 1px solid #e6d3bb;
            border-radius: 16px;
            background: #fffdf9;
            padding: 16px;
        }

        .payment-qr-image {
            width: 100%;
            max-width: 360px;
            aspect-ratio: 1 / 1;
            margin: 0 auto;
            display: block;
            object-fit: contain;
            border-radius: 16px;
            background: #fff;
            border: 1px solid #ead9c4;
        }

        .payment-qr-fallback {
            min-height: 360px;
            display: none;
            align-items: center;
            justify-content: center;
            text-align: center;
            color: #7f6a58;
            border: 1px dashed #d9c2a4;
            border-radius: 16px;
            padding: 18px;
            line-height: 1.55;
        }

        .payment-qr-fallback.is-visible {
            display: flex;
        }

        .payment-order-total {
            margin: 14px 0 0;
            color: #3b2519;
            font-size: 22px;
            line-height: 1.2;
            font-weight: 700;
            text-align: center;
        }

        .payment-discount-summary {
            margin: 12px 0 0;
            display: grid;
            gap: 8px;
            padding: 12px 14px;
            background: #f8f1e7;
            border: 1px solid #ead9c4;
            border-radius: 12px;
        }

        .payment-discount-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            color: #3b2519;
            font-size: 14px;
            font-weight: 700;
        }

        .payment-discount-row span {
            color: #5c4737;
            font-size: 13px;
        }

        .payment-status-row {
            margin: 12px 0 0;
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            align-items: center;
            gap: 10px;
        }

        .payment-status-badge {
            padding: 7px 12px;
            border-radius: 999px;
            border: 1px solid transparent;
            font-size: 12px;
            line-height: 1;
            font-weight: 700;
        }

        .payment-status-pending {
            background: #fff4cc;
            color: #8c5a00;
            border-color: #f2d27c;
        }

        .payment-status-completed {
            background: #d7f5e3;
            color: #0f6a36;
            border-color: #9dd9b8;
        }

        .payment-status-failed {
            background: #fde2e2;
            color: #a11b1b;
            border-color: #f1aaaa;
        }

        .payment-status-countdown {
            color: #7f6a58;
            font-size: 13px;
            line-height: 1.4;
        }

        .payment-qr-note {
            margin: 8px 0 0;
            color: #7f6a58;
            font-size: 13px;
            line-height: 1.45;
            text-align: center;
        }

        .payment-config-title {
            margin: 0 0 12px;
            color: #3b2519;
            font-size: 18px;
            line-height: 1.25;
            font-weight: 700;
        }

        .payment-config-grid {
            display: grid;
            gap: 12px;
        }

        .payment-field {
            display: grid;
            gap: 6px;
        }

        .payment-field label {
            color: #5c4737;
            font-size: 13px;
            font-weight: 700;
        }

        .payment-field input {
            width: 100%;
            height: 42px;
            border: 1px solid #dcc7ae;
            border-radius: 12px;
            padding: 0 12px;
            background: #fffefb;
            color: #3b2519;
            font-family: inherit;
            font-size: 14px;
            outline: none;
        }

        .payment-field input:focus {
            border-color: #b7885f;
            box-shadow: 0 0 0 3px rgba(139, 90, 56, 0.12);
        }

        .payment-field input[readonly] {
            background: #fbf5ec;
            color: #7a6756;
            cursor: default;
        }

        .payment-actions {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px;
            margin-top: 4px;
        }

        .payment-action {
            min-height: 42px;
            padding: 8px 12px;
            border: 0;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: inherit;
            font-size: 14px;
            font-weight: 700;
            line-height: 1.25;
            text-align: center;
            white-space: normal;
            cursor: pointer;
        }

        .payment-action:disabled {
            cursor: default;
            opacity: 0.64;
        }

        .payment-action-primary {
            background: #8b5a38;
            color: #fff;
        }

        .payment-action-secondary {
            background: #efe4d5;
            color: #7a4d2e;
        }

        .size-backdrop {
            position: fixed;
            inset: 0;
            background: rgba(38, 24, 15, 0.56);
            z-index: 60;
        }

        .size-modal {
            position: fixed;
            inset: 0;
            z-index: 65;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 18px;
        }

        .size-modal.is-open {
            display: flex;
        }

        .size-panel {
            width: min(420px, 100%);
            border: 1px solid #e6d3bb;
            border-radius: 16px;
            background: #fffaf4;
            box-shadow: 0 22px 56px rgba(57, 35, 20, 0.28);
            overflow: hidden;
        }

        .size-panel-header {
            padding: 18px 18px 14px;
            border-bottom: 1px solid #e6d3bb;
            background: #f7ebda;
        }

        .size-panel-title {
            margin: 0;
            color: #3b2519;
            font-size: 18px;
            line-height: 1.25;
            font-weight: 700;
        }

        .size-panel-subtitle {
            margin: 6px 0 0;
            color: #7f6a58;
            font-size: 13px;
            line-height: 1.45;
        }

        .size-panel-body {
            padding: 18px;
            display: grid;
            gap: 12px;
        }

        .size-buttons {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px;
        }

        .size-button {
            height: 48px;
            border: 1px solid #dcc7ae;
            border-radius: 12px;
            background: #fffefb;
            color: #3b2519;
            font-family: inherit;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
        }

        .size-button:hover {
            border-color: #b7885f;
        }

        .size-panel-close-row {
            display: flex;
            justify-content: flex-end;
        }

        .size-panel-close {
            border: 0;
            background: transparent;
            color: #a54b2a;
            font-family: inherit;
            font-size: 13px;
            font-weight: 700;
            cursor: pointer;
            padding: 0;
        }

        .payment-transfer-box {
            margin-top: 14px;
            padding: 12px 14px;
            border-radius: 12px;
            background: #f8f1e7;
            border: 1px solid #ead9c4;
        }

        .payment-transfer-label {
            margin: 0 0 6px;
            color: #5c4737;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0;
        }

        .payment-transfer-value {
            margin: 0;
            color: #3b2519;
            font-size: 13px;
            line-height: 1.5;
            word-break: break-word;
            white-space: pre-line;
        }

        .news-section {
            width: 100%;
            max-width: 1120px;
            margin: 0 auto;
            padding: 24px 0 10px;
        }

        .news-title {
            margin: 0;
            color: #2f1d16;
            font-size: 22px;
            line-height: 1.15;
            font-weight: 700;
            letter-spacing: 0;
        }

        .news-subtitle {
            margin: 8px 0 18px;
            color: #7d654f;
            font-size: 14px;
        }

        .shop-contact {
            padding: 30px 0 20px;
        }

        .shop-contact h2 {
            margin: 0;
            color: #2f1d16;
            font-size: 22px;
            line-height: 1.15;
            font-weight: 700;
        }

        .info-container {
            display: flex;
            gap: 40px;
            align-items: flex-start;
            margin-top: 18px;
            flex-wrap: wrap;
        }

        .left-content,
        .right-content {
            flex: 1;
            min-width: 280px;
        }

        .left-content h2,
        .right-content h2 {
            margin: 0 0 15px;
            color: #2f1d16;
            font-size: 30px;
            line-height: 1.2;
            font-weight: 700;
        }

        .left-content p,
        .right-content p {
            margin: 0 0 14px;
            color: #6f5b4a;
            font-size: 18px;
            line-height: 1.7;
            max-width: none;
        }

        .map-card {
            margin-top: 20px;
            padding: 18px 20px;
            border: 1px solid #e1c9ac;
            border-radius: 16px;
            display: flex;
            align-items: center;
            gap: 16px;
            background: linear-gradient(180deg, #fffdf8 0%, #f7ebdd 100%);
            color: #2f1d16;
            text-decoration: none;
            box-shadow: 0 10px 22px rgba(124, 78, 47, 0.08);
            max-width: 620px;
            transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
        }

        .map-card:hover {
            border-color: #c89a54;
            box-shadow: 0 14px 26px rgba(124, 78, 47, 0.12);
            transform: translateY(-2px);
        }

        .icon {
            width: 60px;
            height: 60px;
            border-radius: 14px;
            background: #f1dfc9;
            color: #8b5a38;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            font-size: 28px;
        }

        .map-info h3 {
            margin: 0 0 10px;
            color: #2f1d16;
            font-size: 20px;
            line-height: 1.2;
            font-weight: 700;
        }

        .map-info p {
            margin: 0;
            color: #6f5b4a;
            font-size: 15px;
            line-height: 1.5;
            max-width: none;
        }

        .news-carousel {
            position: relative;
        }

        .news-carousel-track {
            display: flex;
            gap: 14px;
            width: 100%;
            overflow-x: auto;
            scroll-snap-type: x mandatory;
            scroll-behavior: smooth;
            touch-action: pan-y;
            cursor: grab;
            scrollbar-width: none;
            -ms-overflow-style: none;
            padding-bottom: 0;
        }

        .news-carousel-track::-webkit-scrollbar {
            display: none;
        }

        .news-carousel-nav {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            width: 42px;
            height: 42px;
            border: 0;
            border-radius: 50%;
            background: rgba(255, 250, 243, 0.92);
            color: #8b5a38;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            z-index: 3;
            box-shadow: 0 8px 18px rgba(124, 78, 47, 0.16);
            backdrop-filter: blur(4px);
        }

        .news-carousel-nav svg {
            width: 20px;
            height: 20px;
        }

        .news-carousel-nav-prev {
            left: -12px;
        }

        .news-carousel-nav-next {
            right: -12px;
        }

        .news-carousel-nav:hover {
            background: #fff7ec;
            color: #5f3a23;
        }

        .news-carousel-slide {
            flex: 0 0 clamp(280px, calc((100% - 14px) / 2), 560px);
            margin: 0;
            overflow: hidden;
            border-radius: 16px;
            background: linear-gradient(180deg, #fffdf8 0%, #f5eadc 100%);
            border: 1px solid #e2c9ac;
            box-shadow: 0 10px 24px rgba(124, 78, 47, 0.12);
            scroll-snap-align: start;
        }

        .news-carousel-slide img {
            display: block;
            width: 100%;
            aspect-ratio: 16 / 9;
            object-fit: cover;
            object-position: center center;
        }

        .news-carousel-dots {
            position: absolute;
            left: 50%;
            bottom: 14px;
            transform: translateX(-50%);
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 6px 10px;
            border-radius: 999px;
            background: rgba(255, 250, 243, 0.84);
            backdrop-filter: blur(4px);
            box-shadow: 0 8px 18px rgba(124, 78, 47, 0.12);
            z-index: 2;
        }

        .news-carousel-dot {
            width: 10px;
            height: 10px;
            border: 0;
            border-radius: 999px;
            background: #d7bca0;
            cursor: pointer;
            padding: 0;
            transition: transform 0.18s ease, background-color 0.18s ease, box-shadow 0.18s ease;
        }

        .news-carousel-dot.is-active {
            background: #8b5a38;
            box-shadow: 0 0 0 3px rgba(139, 90, 56, 0.16);
            transform: scale(1.1);
        }

        .news-carousel-dot:focus-visible {
            outline: 2px solid #8b5a38;
            outline-offset: 3px;
        }

        @media (max-width: 1400px) {
            .best-seller-grid {
                grid-template-columns: repeat(5, minmax(0, 240px));
            }
        }

        @media (max-width: 1100px) {
            .shop-navbar-menu {
                gap: 32px;
            }

            .news-carousel-slide {
                flex-basis: clamp(250px, 84vw, 520px);
            }

            .best-seller-grid {
                grid-template-columns: repeat(3, minmax(0, 240px));
            }
        }

        @media (max-width: 900px) {
            .info-container {
                flex-direction: column;
                gap: 24px;
            }

            .best-seller-grid {
                grid-template-columns: repeat(2, minmax(0, 240px));
            }

            .best-seller-title {
                font-size: 22px;
            }

            .left-content h2,
            .right-content h2 {
                font-size: 26px;
            }
        }

        @media (max-width: 640px) {
            .shop-container {
                padding: 0 12px 136px;
            }

            .banner-container {
                padding: 10px 0 0;
            }

            .shop-navbar-top {
                grid-template-columns: 1fr;
                gap: 10px;
                padding: 10px 12px 12px;
            }

            .shop-search-row,
            .shop-nav-right {
                grid-column: 1 / -1;
            }

            .shop-nav-right {
                justify-self: stretch;
                justify-content: space-between;
                gap: 12px;
                flex-wrap: wrap;
            }

            .shop-user-menu {
                width: 100%;
            }

            .shop-user-info {
                width: 100%;
                justify-content: space-between;
            }

            .shop-navbar-menu {
                gap: 22px;
                justify-content: flex-start;
                overflow-x: auto;
                white-space: nowrap;
            }

            .shop-category-menu {
                left: 0;
                transform: translate(0, -6px);
            }

            .shop-category-dropdown.is-open .shop-category-menu {
                transform: translate(0, 0);
            }

            .shop-user-info {
                white-space: normal;
            }

            .shop-user-panel {
                width: calc(100vw - 24px);
                right: 0;
            }

            .shop-user-meta {
                grid-template-columns: 1fr;
            }

            .shop-user-actions {
                grid-template-columns: 1fr;
            }

            .shop-search-box {
                max-width: none;
                height: 44px;
                border-radius: 16px;
                background: rgba(239, 228, 213, 0.92);
                box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
            }

            .shop-logo-image {
                width: 56px;
                max-width: 56px;
            }

            .banner-content {
                width: 100%;
                height: auto;
                aspect-ratio: 16 / 9;
                margin-top: 14px;
                border-radius: 22px;
                box-shadow: 0 18px 36px rgba(78, 49, 31, 0.16);
                border: 1px solid rgba(228, 209, 186, 0.9);
            }

            .banner-content::before {
                background: linear-gradient(180deg, rgba(0, 0, 0, 0.18) 0%, rgba(0, 0, 0, 0.08) 100%);
            }

            .banner-nav {
                width: 36px;
                height: 36px;
                backdrop-filter: blur(8px);
                box-shadow: 0 10px 20px rgba(47, 29, 22, 0.18);
            }

            .banner-nav-prev {
                left: 10px;
            }

            .banner-nav-next {
                right: 10px;
            }

            .banner-indicators {
                bottom: 12px;
            }

            .banner-dot {
                width: 8px;
                height: 8px;
            }

            .news-section,
            .best-seller-section,
            .shop-contact {
                width: 100%;
                max-width: none;
                margin-top: 18px;
                padding: 18px 14px 16px;
                background: rgba(255, 250, 244, 0.78);
                border: 1px solid rgba(226, 201, 172, 0.72);
                border-radius: 22px;
                box-shadow: 0 14px 30px rgba(78, 49, 31, 0.08);
                backdrop-filter: blur(6px);
            }

            .news-title,
            .best-seller-title {
                padding-left: 0;
                margin-bottom: 10px;
            }

            .news-subtitle {
                margin-bottom: 12px;
            }

            .best-seller-slide .best-seller-grid {
                grid-template-columns: repeat(1, minmax(0, 1fr));
            }

            .best-seller-carousel {
                margin-top: 2px;
            }

            .product-name {
                font-size: 16px;
                line-height: 1.3;
            }

            .product-price,
            .btn-order,
            .btn-view-more {
                font-size: 13px;
            }

            .product-card {
                border-radius: 18px;
                padding: 12px;
                box-shadow: 0 10px 24px rgba(88, 57, 33, 0.08);
            }

            .product-visual {
                height: 168px;
                border-radius: 14px;
            }

            .product-visual-frame {
                min-height: 168px;
                border-radius: 14px;
            }

            .product-visual-frame img {
                width: 82%;
                height: 82%;
            }

            .product-detail-action,
            .btn-order,
            .btn-view-more {
                border-radius: 12px;
            }

            .product-carousel-nav {
                width: 36px;
                height: 36px;
                box-shadow: 0 10px 20px rgba(47, 29, 22, 0.18);
            }

            .product-carousel-nav-prev {
                left: -8px;
            }

            .product-carousel-nav-next {
                right: -8px;
            }

            .shop-cart-drawer {
                width: 100%;
            }

            .shop-cart-actions {
                grid-template-columns: 1fr;
            }

            .news-carousel-track {
                gap: 12px;
            }

            .news-carousel-nav {
                width: 36px;
                height: 36px;
                box-shadow: 0 10px 20px rgba(47, 29, 22, 0.18);
            }

            .news-carousel-nav-prev {
                left: -8px;
            }

            .news-carousel-nav-next {
                right: -8px;
            }

            .news-carousel-dots {
                gap: 6px;
                padding: 5px 8px;
                bottom: 10px;
            }

            .news-carousel-dot {
                width: 8px;
                height: 8px;
            }

            .shop-mobile-actions {
                position: fixed;
                left: 10px;
                right: 10px;
                bottom: calc(10px + env(safe-area-inset-bottom));
                z-index: 39;
                display: grid;
                grid-template-columns: 1fr 1.3fr;
                gap: 10px;
                padding: 10px;
                border-radius: 20px;
                background: rgba(255, 251, 245, 0.88);
                border: 1px solid rgba(228, 209, 186, 0.72);
                box-shadow: 0 18px 36px rgba(47, 29, 22, 0.18);
                backdrop-filter: blur(14px);
            }

            .shop-mobile-action {
                min-height: 50px;
                padding: 12px 14px;
                border: 0;
                border-radius: 14px;
                font-family: inherit;
                font-size: 15px;
                font-weight: 700;
                cursor: pointer;
                box-shadow: 0 8px 18px rgba(57, 35, 20, 0.12);
            }

            .shop-mobile-action-primary {
                background: #8b5a38;
                color: #fff;
            }

            .shop-mobile-action-secondary {
                background: #fffaf4;
                color: #7a4d2e;
                border: 1px solid #dcc7ae;
            }

            .shop-cart-drawer {
                border-top-left-radius: 20px;
                border-top-right-radius: 20px;
            }

            .shop-cart-header {
                padding: 16px 16px 12px;
            }

            .shop-cart-body {
                padding: 14px 16px;
            }

            .shop-cart-footer {
                padding: 12px 16px 16px;
            }

            .shop-cart-action {
                height: 46px;
                border-radius: 12px;
            }
        }

        @media (max-width: 900px) {
            .payment-panel-body {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 640px) {
            .map-card {
                align-items: flex-start;
                padding: 16px;
                gap: 14px;
                max-width: 100%;
            }

            .icon {
                width: 44px;
                height: 44px;
                font-size: 22px;
            }

            .map-info h3 {
                font-size: 17px;
            }

            .left-content p,
            .right-content p,
            .map-info p {
                font-size: 15px;
            }

            .payment-actions {
                grid-template-columns: 1fr;
            }

            .product-detail-panel,
            .payment-panel,
            .size-panel,
            .shop-user-panel {
                width: calc(100vw - 16px);
                max-height: calc(100vh - 24px);
            }

            .product-detail-panel-header,
            .payment-panel-header,
            .shop-user-panel-header,
            .size-panel-header {
                padding: 14px 14px 12px;
            }

            .product-detail-panel-body,
            .payment-panel-body,
            .shop-user-panel-body {
                padding: 14px;
            }

            .shop-coupon-row {
                grid-template-columns: 1fr;
            }

            .shop-delivery-toggle {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body class="customer-shop-page">

<header class="shop-navbar">
    <div class="shop-navbar-top">
        <div class="shop-logo-placeholder">
            <img class="shop-logo-image" src="${pageContext.request.contextPath}/images/logo-premium-source.png?v=1" alt="Premium Coffee">
        </div>

        <div class="shop-search-row">
            <div class="shop-search-box">
                <input id="shopSearchInput" type="search" placeholder="Bạn muốn mua gì..." autocomplete="off">
            </div>
        </div>

        <div class="shop-nav-right">
            <% if (loggedIn && customerProfile != null) { %>
                <div class="shop-user-menu">
                <button id="shopUserToggle" class="shop-user-info" type="button" aria-haspopup="dialog" aria-expanded="false" aria-controls="shopUserPanel">
                    <span class="shop-user-avatar" aria-hidden="true">
                        <svg viewBox="0 0 24 24">
                            <circle cx="12" cy="8" r="3.4"></circle>
                            <path d="M5 19a7 7 0 0 1 14 0" fill="none" stroke-linecap="round"></path>
                        </svg>
                    </span>
                    <span class="shop-user-label">
                        <span class="shop-user-name"><%= htmlEscape(customerDisplayName) %></span>
                        <svg class="shop-user-chevron" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M7 10l5 5 5-5" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                    </span>
                </button>
                <div id="shopUserPanel" class="shop-user-panel" aria-hidden="true">
                    <div class="shop-user-panel-header">
                        <div>
                            <p class="shop-user-panel-kicker">Thông tin khách hàng</p>
                            <h2 class="shop-user-panel-title"><%= htmlEscape(customerDisplayName) %></h2>
                        </div>
                        <button id="shopUserClose" class="shop-user-panel-close" type="button" aria-label="Đóng thông tin khách hàng">×</button>
                    </div>
                    <div class="shop-user-panel-body">
                        <div class="shop-user-meta">
                            <div class="shop-user-meta-item">
                                <span class="shop-user-meta-label">Mã khách hàng</span>
                                <span class="shop-user-meta-value"><%= htmlEscape(customerMaKH) %></span>
                            </div>
                            <div class="shop-user-meta-item">
                                <span class="shop-user-meta-label">Số điện thoại</span>
                                <span class="shop-user-meta-value"><%= htmlEscape(customerPhone) %></span>
                            </div>
                            <div class="shop-user-meta-item">
                                <span class="shop-user-meta-label">Vai trò</span>
                                <span class="shop-user-meta-value"><%= htmlEscape(customerRole) %></span>
                            </div>
                            <div class="shop-user-meta-item">
                                <span class="shop-user-meta-label">Trạng thái</span>
                                <span class="shop-user-meta-value"><%= htmlEscape(customerStatus) %></span>
                            </div>
                        </div>

                        <form id="shopUserForm" class="shop-user-form" method="post" action="${pageContext.request.contextPath}/khachhang">
                            <input type="hidden" name="action" value="<%= htmlEscape(customerProfileAction) %>">
                            <input type="hidden" name="source" value="customer-shop">
                            <input type="hidden" name="maKH" value="<%= htmlEscape(customerMaKH) %>">

                            <div class="shop-user-fields">
                                <div class="shop-user-field">
                                    <label for="shopUserHoTen">Họ tên</label>
                                    <input id="shopUserHoTen" name="hoTen" type="text" value="<%= htmlEscape(customerHoTen) %>" required>
                                </div>
                                <div class="shop-user-field">
                                    <label for="shopUserSdt">Số điện thoại</label>
                                    <input id="shopUserSdt" name="sdt" type="text" value="<%= htmlEscape(customerSdt) %>">
                                </div>
                                <div class="shop-user-field">
                                    <label for="shopUserEmail">Email</label>
                                    <input id="shopUserEmail" name="email" type="email" value="<%= htmlEscape(customerEmail) %>">
                                </div>
                                <div class="shop-user-field">
                                    <label for="shopUserDiaChi">Địa chỉ</label>
                                    <input id="shopUserDiaChi" name="diaChi" type="text" value="<%= htmlEscape(customerDiaChi) %>">
                                </div>
                            </div>

                            <div class="shop-user-actions">
                                <button type="submit" class="shop-user-action shop-user-action-save">Lưu</button>
                                <a id="shopUserDelete" href="<%= htmlEscape(customerDeleteUrl) %>" class="shop-user-action shop-user-action-delete">Xóa</a>
                                <a href="${pageContext.request.contextPath}/logout" class="shop-user-action shop-user-action-logout">Đăng xuất</a>
                            </div>
                            <p class="shop-user-panel-footer-note">Dữ liệu sẽ được lưu vào hồ sơ khách hàng đang đăng nhập.</p>
                        </form>
                    </div>
                </div>
            </div>
            <% } %>
            <div class="shop-nav-icons">
                <a href="#lienhe" title="Liên hệ" aria-label="Liên hệ">
                    <svg class="shop-nav-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <rect x="4" y="6.5" width="16" height="11" rx="2"/>
                        <path d="M5 8l7 5 7-5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </a>
                <% if (!loggedIn) { %>
                <a href="<%= loginUrl %>" title="Đăng nhập" aria-label="Đăng nhập">
                    <svg class="shop-nav-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <circle cx="12" cy="8" r="3.3"/>
                        <path d="M5.5 19a6.5 6.5 0 0 1 13 0" fill="none" stroke-linecap="round"/>
                    </svg>
                </a>
                <% } %>
            </div>
            <button id="shopCartToggle" class="shop-cart-toggle" type="button" aria-label="Mở giỏ hàng" aria-expanded="false" aria-controls="shopCartDrawer">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <circle cx="9" cy="19" r="1.5"></circle>
                    <circle cx="17" cy="19" r="1.5"></circle>
                    <path d="M3 4h2l2.2 9.2a2 2 0 0 0 2 1.6h6.7a2 2 0 0 0 1.9-1.4L20 8H7.2" fill="none" stroke-linecap="round" stroke-linejoin="round"></path>
                </svg>
                <span class="shop-cart-badge" id="shopCartCount">0</span>
            </button>
        </div>
    </div>

        <nav class="shop-navbar-menu">
            <a href="#sanpham">TRANG CHỦ</a>
            <div class="shop-category-dropdown">
                <button id="shopCategoryToggle" class="shop-category-toggle" type="button" aria-haspopup="true" aria-expanded="false">
                    SẢN PHẨM
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M7 10l5 5 5-5" stroke-linecap="round" stroke-linejoin="round"></path>
                    </svg>
                </button>
                <div class="shop-category-menu" role="menu" aria-label="Sản phẩm">
                    <button type="button" data-category="banh" role="menuitem">Bánh</button>
                    <button type="button" data-category="cafe" role="menuitem">Cafe</button>
                    <button type="button" data-category="tra-sua" role="menuitem">Trà sữa</button>
                    <button type="button" data-category="sinh-to" role="menuitem">Sinh tố</button>
                </div>
            </div>
            <a href="#lienhe">VỀ CHÚNG TÔI</a>
            <a href="#news">KHUYẾN MÃI</a>
        </nav>
</header>

<%
    String customerShopError = (String) session.getAttribute("error");
    if (customerShopError != null && !customerShopError.trim().isEmpty()) {
%>
<div class="shop-container" style="padding-top: 16px; padding-bottom: 0;">
    <div class="alert alert-error"><%= htmlEscape(customerShopError) %></div>
</div>
<%
        session.removeAttribute("error");
    }
%>

<div class="shop-mobile-actions" aria-label="Thao tác nhanh trên điện thoại">
    <button id="shopMobileCartButton" class="shop-mobile-action shop-mobile-action-secondary" type="button">Giỏ hàng</button>
    <button id="shopMobileCheckoutButton" class="shop-mobile-action shop-mobile-action-primary" type="button">Đặt hàng</button>
</div>

<section class="banner-container">
    <div class="banner-content">
        <div class="banner-slides">
            <img class="banner-slide banner-slide-one" src="${pageContext.request.contextPath}/images/banner.png" alt="Banner 1">
            <img class="banner-slide banner-slide-two" src="${pageContext.request.contextPath}/images/banner-slider.png" alt="Banner 2">
        </div>
        <button class="banner-nav banner-nav-prev" type="button" aria-label="Ảnh trước">
            <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
        </button>
        <button class="banner-nav banner-nav-next" type="button" aria-label="Ảnh sau">
            <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M9 6l6 6-6 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
        </button>
        <div class="banner-indicators" aria-hidden="true">
            <button class="banner-dot is-active" type="button" data-slide="0" aria-label="Banner 1"></button>
            <button class="banner-dot" type="button" data-slide="1" aria-label="Banner 2"></button>
        </div>
    </div>
</section>

<main class="shop-container">
    <section id="news" class="news-section">
        <h2 class="news-title">Tin tức & Khuyến mãi</h2>
        <p class="news-subtitle">Tin tức & Khuyến mãi của cửa hàng</p>
        <div class="news-carousel">
            <button id="newsCarouselPrev" class="news-carousel-nav news-carousel-nav-prev" type="button" aria-label="Xem banner trước">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </button>
            <div id="newsCarouselTrack" class="news-carousel-track">
                <%
                    for (String imagePath : promoBannerCarouselImages) {
                %>
                <figure class="news-carousel-slide">
                    <img src="<%= request.getContextPath() + imagePath %>" alt="Banner khuyến mãi">
                </figure>
                <%
                    }
                %>
            </div>
            <button id="newsCarouselNext" class="news-carousel-nav news-carousel-nav-next" type="button" aria-label="Xem banner sau">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M9 6l6 6-6 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </button>
            <div id="newsCarouselDots" class="news-carousel-dots" aria-label="Điều hướng banner khuyến mãi"></div>
        </div>
    </section>

    <section id="sanpham" class="best-seller-section">
        <h2 class="best-seller-title mb-2">SẢN PHẨM NỔI BẬT</h2>

        <div class="best-seller-carousel">
            <button id="productCarouselPrev" class="product-carousel-nav product-carousel-nav-prev" type="button" aria-label="Xem sản phẩm trước">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M15 6l-6 6 6 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </button>
            <div class="best-seller-viewport">
                <div id="productCarouselTrack" class="best-seller-track"></div>
            </div>
            <button id="productCarouselNext" class="product-carousel-nav product-carousel-nav-next" type="button" aria-label="Xem sản phẩm sau">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M9 6l6 6-6 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </button>
        </div>

        <p class="shop-search-empty" hidden>Không tìm thấy sản phẩm phù hợp.</p>
    </section>

    <section id="lienhe" class="shop-contact">

        <div class="info-container">
            <div class="left-content">
                <h2>Liên hệ thêm</h2>
                <p><strong>Địa chỉ:</strong> FPT PolySchool Thủ Dầu Một</p>
                <p><strong>Điện thoại:</strong> 0123 456 789</p>
                <p><strong>Email:</strong> premiumcoffee@gmail.com</p>
                <p><strong>Giờ mở cửa:</strong> 07:00 - 22:00</p>
            </div>

            <div class="right-content">
                <h2>Về chúng tôi</h2>
                <p>
                    Premium Coffee là cửa hàng cà phê hiện đại, ấm cúng, mang đến những ly cà phê chất lượng cao cùng
                    không gian thoải mái để thư giãn, gặp gỡ và làm việc.
                </p>
                <p>
                    Với nguồn nguyên liệu tuyển chọn kỹ lưỡng và phong cách phục vụ tận tâm, chúng tôi luôn đặt chất
                    lượng và trải nghiệm khách hàng lên hàng đầu.
                </p>
            </div>
        </div>

        <a class="map-card"
           href="https://www.google.com/maps/search/?api=1&query=FPT%20PolySchool%20Th%C3%BB%20D%E1%BA%A7u%20Một%2C%20B%C3%ACnh%20D%C6%B0%C6%A1ng"
           target="_blank"
           rel="noopener noreferrer"
           aria-label="Mở Google Maps đến FPT PolySchool Thủ Dầu Một">
            <div class="icon" aria-hidden="true">📍</div>
            <div class="map-info">
                <h3>FPT PolySchool Thủ Dầu Một</h3>
                <p>Bấm vào đây để mở Google Maps và xem đường đi đến cơ sở này.</p>
            </div>
        </a>
    </section>
</main>

<div id="productDetailBackdrop" class="product-detail-backdrop" hidden></div>
<section id="productDetailModal" class="product-detail-modal" aria-hidden="true">
    <div class="product-detail-panel" role="dialog" aria-modal="true" aria-labelledby="productDetailTitle">
        <div class="product-detail-panel-header">
            <div>
                <h2 id="productDetailTitle" class="product-detail-panel-title">Chi tiết sản phẩm</h2>
                <p id="productDetailSubtitle" class="product-detail-panel-subtitle"></p>
            </div>
            <button id="productDetailClose" class="product-detail-panel-close" type="button" aria-label="Đóng">×</button>
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
                        <span class="product-detail-meta-label">Loại</span>
                        <strong id="productDetailCategory" class="product-detail-meta-value"></strong>
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
                <div class="product-detail-actions">
                    <button id="productDetailAddToCart" type="button" class="product-detail-action product-detail-action-secondary">
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M3 4h2l2.2 9.2a2 2 0 0 0 2 1.6h6.9a2 2 0 0 0 1.9-1.4L20 8H7.2" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/>
                            <circle cx="10" cy="20" r="1.5" fill="currentColor"></circle>
                            <circle cx="17" cy="20" r="1.5" fill="currentColor"></circle>
                        </svg>
                        <span>Thêm vào giỏ</span>
                    </button>
                    <button id="productDetailBuyNow" type="button" class="product-detail-action product-detail-action-primary">
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M5 12h10.2M11 6l6 6-6 6" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <span>Đặt mua</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</section>

<div id="sizeBackdrop" class="size-backdrop" hidden></div>
<section id="sizeModal" class="size-modal" aria-hidden="true">
    <div class="size-panel" role="dialog" aria-modal="true" aria-labelledby="sizeTitle">
        <div class="size-panel-header">
            <h2 id="sizeTitle" class="size-panel-title">Chọn size</h2>
            <p id="sizeSubtitle" class="size-panel-subtitle">Chọn một size để thêm vào giỏ hàng.</p>
        </div>
        <div class="size-panel-body">
            <div class="size-buttons">
                <button type="button" class="size-button" data-size-value="S">Size S</button>
                <button type="button" class="size-button" data-size-value="M">Size M</button>
                <button type="button" class="size-button" data-size-value="L">Size L</button>
            </div>
            <div class="size-panel-close-row">
                <button id="sizeClose" class="size-panel-close" type="button">Hủy</button>
            </div>
        </div>
    </div>
</section>

<div id="shopCartBackdrop" class="shop-cart-backdrop" hidden></div>
<aside id="shopCartDrawer" class="shop-cart-drawer" aria-hidden="true">
    <div class="shop-cart-header">
        <h2 class="shop-cart-title">Giỏ hàng</h2>
        <button id="shopCartClose" class="shop-cart-close" type="button" aria-label="Đóng giỏ hàng">×</button>
    </div>
    <div id="shopCartItems" class="shop-cart-body"></div>
    <div class="shop-cart-footer">
        <div class="shop-cart-coupon">
            <label for="shopCouponInput">Mã giảm giá</label>
            <div class="shop-coupon-row">
                <input id="shopCouponInput"
                       class="shop-coupon-input"
                       type="text"
                       placeholder="Nhập mã giảm giá"
                       autocomplete="off"
                       autocapitalize="characters"
                       spellcheck="false">
                <button id="shopCouponApply" class="shop-coupon-button shop-coupon-button-primary" type="button">Áp dụng</button>
                <button id="shopCouponClear" class="shop-coupon-button shop-coupon-button-secondary" type="button">Xóa</button>
            </div>
            <select id="shopCouponSelect" class="shop-coupon-select" hidden <%= (dsKhuyenMai == null || dsKhuyenMai.isEmpty()) ? "disabled" : "" %>>
                <option value="">Không áp dụng</option>
                <%
                    if (dsKhuyenMai != null && !dsKhuyenMai.isEmpty()) {
                        for (KhuyenMai km : dsKhuyenMai) {
                              if (km == null || km.getMaKM() == null) {
                                  continue;
                              }

                              String discountType = km.getLoaiGiamGia();
                              String displayLabel;
                              String percentText = "";
                              String amountText = "";
                              String rawPercent = "";
                              String rawAmount = "";
                              String rawMax = "";

                              if ("AMOUNT".equalsIgnoreCase(discountType) || (discountType == null && km.getSoTienGiam() != null)) {
                                  amountText = formatVnd(km.getSoTienGiam());
                                  rawAmount = km.getSoTienGiam() != null ? km.getSoTienGiam().toPlainString() : "";
                                  displayLabel = km.getMaKM() + " - Giảm " + amountText;
                              } else {
                                  percentText = formatPercent(km.getPhanTramGiam());
                                  rawPercent = km.getPhanTramGiam() != null ? km.getPhanTramGiam().toString() : "";
                                  displayLabel = km.getMaKM() + " - " + percentText + "%";
                                  if (km.getMucGiamToiDa() != null) {
                                      rawMax = km.getMucGiamToiDa().toPlainString();
                                      displayLabel += " - Tối đa " + formatVnd(km.getMucGiamToiDa());
                                  }
                              }

                              if (km.getTenKM() != null && !km.getTenKM().trim().isEmpty()) {
                                  displayLabel += " - " + km.getTenKM().trim();
                              }
                              String dataType = "AMOUNT".equalsIgnoreCase(discountType) || (discountType == null && km.getSoTienGiam() != null) ? "AMOUNT" : "PERCENT";
                  %>
                  <option value="<%= htmlEscape(km.getMaKM()) %>"
                          data-type="<%= htmlEscape(dataType) %>"
                          data-percent="<%= htmlEscape(rawPercent) %>"
                          data-amount="<%= htmlEscape(rawAmount) %>"
                          data-max="<%= htmlEscape(rawMax) %>"
                          data-label="<%= htmlEscape(displayLabel) %>">
                      <%= htmlEscape(displayLabel) %>
                  </option>
                <%
                        }
                    } else {
                %>
                <option value="" disabled>Chưa có mã giảm giá khả dụng</option>
                <%
                    }
                %>
            </select>
            <p id="shopCouponHint" class="shop-coupon-hint">Nhập mã rồi bấm Áp dụng để giảm trực tiếp vào đơn hàng.</p>
        </div>
        <div class="shop-delivery-section">
            <div class="shop-delivery-section-head">
                <div class="shop-delivery-section-title">Phương thức nhận hàng</div>
                <p class="shop-delivery-hint">Chọn mua tại quán hoặc giao đến địa chỉ của bạn.</p>
            </div>
            <div class="shop-delivery-toggle" role="radiogroup" aria-label="Phương thức nhận hàng">
                <button type="button" class="shop-delivery-option is-active" data-delivery-method="TAI_QUAN">Mua tại quán</button>
                <button type="button" class="shop-delivery-option" data-delivery-method="GIAO_HANG">Giao hàng</button>
            </div>
            <div id="shopDeliveryAddressWrap" class="shop-delivery-address" hidden>
                <label for="shopDeliveryAddress">Địa chỉ giao hàng</label>
                <input id="shopDeliveryAddress" type="text" value="" placeholder="Nhập địa chỉ giao hàng" disabled>
                <p class="shop-delivery-hint">Nhân viên sẽ thấy địa chỉ này khi đơn được tạo.</p>
            </div>
        </div>
        <div class="shop-cart-summary">
            <span>Tạm tính</span>
            <span id="shopCartSubtotal">0 đ</span>
        </div>
        <div class="shop-cart-summary shop-cart-summary-discount">
            <span>Giảm giá</span>
            <span id="shopCartDiscount">0 đ</span>
        </div>
        <div class="shop-cart-summary">
            <span>Thanh toán</span>
            <span id="shopCartTotal">0 đ</span>
        </div>
        <div class="shop-cart-actions">
            <button id="shopCartClear" class="shop-cart-action shop-cart-action-secondary" type="button">Xóa giỏ</button>
            <button id="shopCartCheckout" class="shop-cart-action shop-cart-action-primary" type="button">Đặt hàng</button>
        </div>
    </div>
</aside>

<div id="paymentBackdrop" class="payment-backdrop" hidden></div>
<section id="paymentModal" class="payment-modal" aria-hidden="true">
    <div class="payment-panel" role="dialog" aria-modal="true" aria-labelledby="paymentTitle">
        <div class="payment-panel-header">
            <div>
                <h2 id="paymentTitle" class="payment-panel-title">Thanh toán QR</h2>
                <p class="payment-panel-subtitle">QR sẽ đổi theo tổng tiền trong giỏ hàng.</p>
            </div>
            <button id="paymentClose" class="payment-panel-close" type="button" aria-label="Đóng thanh toán">×</button>
        </div>
        <div class="payment-panel-body">
            <div class="payment-qr-box">
                <img id="paymentQrImage" class="payment-qr-image" alt="Mã QR thanh toán">
                <div id="paymentQrFallback" class="payment-qr-fallback">
                    Chưa đủ thông tin ngân hàng để tạo QR.
                </div>
                <div id="paymentAmountText" class="payment-order-total">0 đ</div>
                <div class="payment-discount-summary">
                    <div class="payment-discount-row">
                        <span>Tạm tính</span>
                        <strong id="paymentOriginalAmountText">0 đ</strong>
                    </div>
                    <div class="payment-discount-row">
                        <span>Giảm giá</span>
                        <strong id="paymentDiscountAmountText">0 đ</strong>
                    </div>
                </div>
                <div class="payment-status-row">
                    <span id="paymentStatusBadge" class="payment-status-badge payment-status-pending">Chờ thanh toán</span>
                    <span id="paymentStatusCountdown" class="payment-status-countdown">Còn 60 giây</span>
                </div>
                <div class="payment-qr-note">Khách quét QR và chuyển đúng số tiền hiển thị bên dưới.</div>
                <div class="payment-transfer-box">
                    <p class="payment-transfer-label">Nội dung chuyển khoản</p>
                    <p id="paymentTransferText" class="payment-transfer-value"></p>
                </div>
            </div>
            <div class="payment-config-box">
                <h3 class="payment-config-title">Thông tin ngân hàng</h3>
                <div class="payment-config-grid">
                    <div class="payment-field">
                        <label>Ngân hàng</label>
                        <input type="text" value="<%= htmlEscape(sepayBankName) %>" readonly>
                    </div>
                    <div class="payment-field">
                        <label>Số tài khoản</label>
                        <input type="text" value="<%= htmlEscape(sepayAccountNumber) %>" readonly>
                    </div>
                    <div class="payment-field">
                        <label>Tên chủ tài khoản</label>
                        <input type="text" value="<%= htmlEscape(sepayAccountName) %>" readonly>
                    </div>
                    <div class="payment-field">
                        <label>Nội dung chuyển khoản</label>
                        <input id="paymentTransferInput" type="text" value="<%= htmlEscape(sepayPaymentNote) %>" readonly>
                    </div>
                    <div class="payment-actions">
                        <button id="paymentConfirm" class="payment-action payment-action-primary" type="button">Xác nhận đã thanh toán</button>
                        <button id="paymentCopy" class="payment-action payment-action-secondary" type="button">Copy nội dung</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<script>
    (function () {
        const CART_OWNER_KEY = '<%= jsEscape(cartOwnerKey) %>';
        const CART_STORAGE_KEY = 'quanlycafe.customerShopCart.v1:' + CART_OWNER_KEY;
        const LEGACY_GUEST_CART_STORAGE_KEY = 'quanlycafe.customerShopCart.v1:guest';
        const LOGIN_URL = '<%= loginUrl %>';
        const IS_LOGGED_IN = <%= loggedIn %>;
        const PRODUCT_IMAGE_BASE = '<%= request.getContextPath() %>/images/';
        const track = document.querySelector('.banner-slides');
        const slides = track ? Array.from(track.querySelectorAll('.banner-slide')) : [];
        const prevBtn = document.querySelector('.banner-nav-prev');
        const nextBtn = document.querySelector('.banner-nav-next');
        const bannerContent = document.querySelector('.banner-content');
        const bannerDots = Array.from(document.querySelectorAll('.banner-dot'));
        const newsCarouselTrack = document.getElementById('newsCarouselTrack');
        const newsCarouselDots = document.getElementById('newsCarouselDots');
        const newsCarouselPrev = document.getElementById('newsCarouselPrev');
        const newsCarouselNext = document.getElementById('newsCarouselNext');
        const searchInput = document.getElementById('shopSearchInput');
        const mobileCartButton = document.getElementById('shopMobileCartButton');
        const mobileCheckoutButton = document.getElementById('shopMobileCheckoutButton');
        const categoryDropdown = document.querySelector('.shop-category-dropdown');
        const categoryToggle = document.getElementById('shopCategoryToggle');
        const categoryButtons = Array.from(document.querySelectorAll('.shop-category-menu button'));
        const productSection = document.getElementById('sanpham');
        const productTitle = productSection ? productSection.querySelector('.best-seller-title') : null;
        const allProductsLink = document.getElementById('shopAllProductsLink');
        const productCarouselTrack = document.getElementById('productCarouselTrack');
        const productCarouselPrev = document.getElementById('productCarouselPrev');
        const productCarouselNext = document.getElementById('productCarouselNext');
        const emptyState = document.querySelector('.shop-search-empty');
        const viewMore = document.querySelector('.view-more-container');
        const cartToggle = document.getElementById('shopCartToggle');
        const sizeModal = document.getElementById('sizeModal');
        const sizeBackdrop = document.getElementById('sizeBackdrop');
        const sizeClose = document.getElementById('sizeClose');
        const sizeSubtitle = document.getElementById('sizeSubtitle');
        const sizeButtons = Array.from(document.querySelectorAll('.size-button'));
        const cartDrawer = document.getElementById('shopCartDrawer');
        const cartBackdrop = document.getElementById('shopCartBackdrop');
        const cartClose = document.getElementById('shopCartClose');
        const cartItems = document.getElementById('shopCartItems');
        const cartCount = document.getElementById('shopCartCount');
        const cartSubtotal = document.getElementById('shopCartSubtotal');
        const cartDiscount = document.getElementById('shopCartDiscount');
        const cartTotal = document.getElementById('shopCartTotal');
        const couponInput = document.getElementById('shopCouponInput');
        const couponApply = document.getElementById('shopCouponApply');
        const couponClear = document.getElementById('shopCouponClear');
        const couponSelect = document.getElementById('shopCouponSelect');
        const couponHint = document.getElementById('shopCouponHint');
        const cartClear = document.getElementById('shopCartClear');
        const cartCheckout = document.getElementById('shopCartCheckout');
        const paymentModal = document.getElementById('paymentModal');
        const paymentBackdrop = document.getElementById('paymentBackdrop');
        const paymentClose = document.getElementById('paymentClose');
        const productDetailModal = document.getElementById('productDetailModal');
        const productDetailBackdrop = document.getElementById('productDetailBackdrop');
        const productDetailClose = document.getElementById('productDetailClose');
        const productDetailTitle = document.getElementById('productDetailTitle');
        const productDetailSubtitle = document.getElementById('productDetailSubtitle');
        const productDetailImage = document.getElementById('productDetailImage');
        const productDetailCode = document.getElementById('productDetailCode');
        const productDetailPrice = document.getElementById('productDetailPrice');
        const productDetailSize = document.getElementById('productDetailSize');
        const productDetailCategory = document.getElementById('productDetailCategory');
        const productDetailDescription = document.getElementById('productDetailDescription');
        const productDetailIngredients = document.getElementById('productDetailIngredients');
        const productDetailAddToCart = document.getElementById('productDetailAddToCart');
        const productDetailBuyNow = document.getElementById('productDetailBuyNow');
        const paymentQrImage = document.getElementById('paymentQrImage');
        const paymentQrFallback = document.getElementById('paymentQrFallback');
        const paymentAmountText = document.getElementById('paymentAmountText');
        const paymentOriginalAmountText = document.getElementById('paymentOriginalAmountText');
        const paymentDiscountAmountText = document.getElementById('paymentDiscountAmountText');
        const paymentStatusBadge = document.getElementById('paymentStatusBadge');
        const paymentStatusCountdown = document.getElementById('paymentStatusCountdown');
        const paymentTransferText = document.getElementById('paymentTransferText');
        const paymentTransferInput = document.getElementById('paymentTransferInput');
        const paymentConfirm = document.getElementById('paymentConfirm');
        const paymentCopy = document.getElementById('paymentCopy');
        const paymentPanelSubtitle = document.querySelector('.payment-panel-subtitle');
        const deliveryMethodButtons = Array.from(document.querySelectorAll('.shop-delivery-option'));
        const deliveryAddressWrap = document.getElementById('shopDeliveryAddressWrap');
        const deliveryAddressInput = document.getElementById('shopDeliveryAddress');
        const userToggle = document.getElementById('shopUserToggle');
        const userPanel = document.getElementById('shopUserPanel');
        const userClose = document.getElementById('shopUserClose');
        const userDelete = document.getElementById('shopUserDelete');
        const moneyFormat = new Intl.NumberFormat('vi-VN');
        const defaultPaymentConfig = {
            bank: '<%= jsEscape(sepayBankCode) %>',
            bankName: '<%= jsEscape(sepayBankName) %>',
            accountNo: '<%= jsEscape(sepayAccountNumber) %>',
            accountName: '<%= jsEscape(sepayAccountName) %>',
            note: '<%= jsEscape(sepayPaymentNote) %>'
        };
        const paymentConfig = Object.freeze(defaultPaymentConfig);
        const PAYMENT_SESSION_URL = '<%= request.getContextPath() %>/payment-session';
        const PAYMENT_TIMEOUT_MS = <%= paymentTimeoutMillis %>;
        const PAYMENT_QR_FALLBACK_TEXT = paymentQrFallback ? paymentQrFallback.textContent.trim() : '';
        const COUPON_STORAGE_KEY = 'quanlycafe.customerShopCoupon.v1:' + CART_OWNER_KEY;
        const DELIVERY_METHOD_STORAGE_KEY = 'quanlycafe.customerShopDelivery.v1:' + CART_OWNER_KEY;
        const DELIVERY_METHOD_PICKUP = 'TAI_QUAN';
        const DELIVERY_METHOD_DELIVERY = 'GIAO_HANG';
        const bankCodeMap = {
            vietcombank: 'VCB',
            vcb: 'VCB',
            bidv: 'BIDV',
            mb: 'MB',
            mbbank: 'MB',
            militarybank: 'MB',
            techcombank: 'TCB',
            tcb: 'TCB',
            vietinbank: 'CTG',
            ctg: 'CTG',
            acb: 'ACB',
            sacombank: 'STB',
            stb: 'STB',
            vpbank: 'VPB',
            vpb: 'VPB',
            tpbank: 'TPB',
            tpb: 'TPB',
            hdbank: 'HDB',
            hdb: 'HDB',
            msb: 'MSB',
            agribank: 'VBA',
            agribankvn: 'VBA'
        };

        let index = 0;

        migrateGuestCartIfNeeded();

        let cart = loadCart();
        let pendingProductCard = null;
        let pendingProductAction = 'cart';
        let currentDetailCard = null;
        let productSearchQuery = '';
        let selectedProductCategory = '';
        let currentProductPage = 0;
        let newsCarouselAutoDirection = 1;
        let newsCarouselAutoTimer = null;
        let activePaymentSession = null;
        let activePaymentSessionExpiresAt = 0;
        let activePaymentAmount = 0;
        let activePaymentToken = 0;
        let paymentPollTimer = null;
        let paymentCountdownTimer = null;
        let paymentConfirmInFlight = false;
        let couponFeedback = '';
        let selectedDeliveryMethod = DELIVERY_METHOD_PICKUP;
        const productCatalog = [
            <%
                boolean firstDbProduct = true;
                if (dsSanPham != null) {
                    for (SanPham sp : dsSanPham) {
                        if (sp == null || sp.getTenSP() == null || sp.getTenSP().trim().isEmpty()) {
                            continue;
                        }

                        String image = productImage(sp);
                        if (!firstDbProduct) {
                            out.print(",");
                        }
                        firstDbProduct = false;
            %>
            {
                maSP: '<%= jsEscape(sp.getMaSP()) %>',
                name: '<%= jsEscape(sp.getTenSP()) %>',
                priceLabel: '<%= jsEscape(formatVnd(sp.getDonGia() != null ? sp.getDonGia().setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO)) %>',
                priceValue: '<%= jsEscape(sp.getDonGia() != null ? sp.getDonGia().setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() : "0") %>',
                image: '<%= jsEscape(image) %>',
                category: '<%= jsEscape(categoryKey(sp.getLoaiSP())) %>',
                categoryLabel: '<%= jsEscape(categoryLabel(sp.getLoaiSP())) %>',
                sizeLabel: '<%= jsEscape(sp.getSizeSP()) %>',
                description: '<%= jsEscape(sp.getMoTa()) %>',
                ingredients: '<%= jsEscape(sp.getThanhPhan()) %>'
            }
            <%
                    }
                }
            %>
        ];
        if (productCatalog.length === 0) {
            productCatalog.push(
                { maSP: 'SP02', name: 'CAFE SỮA (L)', priceLabel: '35.000 đ', image: 'ca-phe-sua.png', category: 'cafe' },
                { maSP: 'SP01', name: 'CAFE ĐEN (L)', priceLabel: '30.000 đ', image: 'ca-phe-den.png', category: 'cafe' },
                { maSP: 'SP13', name: 'SINH TỐ BƠ', priceLabel: '32.000 đ', image: 'product-avocado.png', category: 'sinh-to' },
                { maSP: 'SP15', name: 'TRÀ CAM ĐÁ', priceLabel: '28.000 đ', image: 'product-citrus.png', category: 'tra-sua' },
                { maSP: 'SP14', name: 'SINH TỐ DÂU', priceLabel: '34.000 đ', image: 'product-strawberry.png', category: 'sinh-to' },
                { maSP: 'SP07', name: 'CÀ PHÊ CACAO', priceLabel: '38.000 đ', image: 'product-cacao.png', category: 'cafe' },
                { maSP: 'SP09', name: 'TRÀ VẢI', priceLabel: '32.000 đ', image: 'product-lychee-tea.png', category: 'tra-sua' },
                { maSP: 'SP15', name: 'TRÀ CAM', priceLabel: '30.000 đ', image: 'product-orange-tea.png', category: 'tra-sua' },
                { maSP: 'SP12', name: 'MATCHA TRÂN CHÂU', priceLabel: '35.000 đ', image: 'product-matcha-boba.png', category: 'tra-sua' },
                { maSP: 'SP11', name: 'TRÀ SỮA TRÂN CHÂU', priceLabel: '35.000 đ', image: 'product-boba-milk-tea.png', category: 'tra-sua' },
                { maSP: 'SP06', name: 'ESPRESSO', priceLabel: '29.000 đ', image: 'ca-phe-den.png', category: 'cafe' },
                { maSP: 'SP05', name: 'CAPPUCCINO', priceLabel: '42.000 đ', image: 'ca-phe-sua.png', category: 'cafe' },
                { maSP: 'SP04', name: 'LATTE', priceLabel: '45.000 đ', image: 'ca-phe-sua.png', category: 'cafe' },
                { maSP: 'SP03', name: 'BẠC XỈU', priceLabel: '33.000 đ', image: 'product-boba-milk-tea.png', category: 'cafe' },
                { maSP: 'SP07', name: 'CHOCOLATE ĐÁ XAY', priceLabel: '40.000 đ', image: 'product-cacao.png', category: 'cafe' },
                { maSP: 'SP15', name: 'NƯỚC CAM TƯƠI', priceLabel: '27.000 đ', image: 'product-orange.png', category: 'tra-sua' },
                { maSP: 'BK01', name: 'BÁNH TIRAMISU', priceLabel: '45.000 đ', image: 'promo-tiramisu.png', category: 'banh' },
                { maSP: 'BK02', name: 'BÁNH CÀ PHÊ KEM', priceLabel: '42.000 đ', image: 'promo-coffee.png', category: 'banh' }
            );
        }

        const categoryTitles = {
            '': 'SẢN PHẨM NỔI BẬT',
            'cafe': 'SẢN PHẨM CAFE',
            'tra-sua': 'SẢN PHẨM TRÀ SỮA',
            'banh': 'SẢN PHẨM BÁNH',
            'sinh-to': 'SẢN PHẨM SINH TỐ'
        };
        const categoryDisplayLabels = {
            '': 'Sản phẩm nổi bật',
            'cafe': 'Cà phê',
            'tra-sua': 'Trà sữa',
            'banh': 'Bánh',
            'sinh-to': 'Sinh tố'
        };

        function renderBanner() {
            if (!track || slides.length < 2 || !prevBtn || !nextBtn) {
                return;
            }
            track.style.transform = 'translateX(' + (-index * 100) + '%)';

            bannerDots.forEach(function (dot, dotIndex) {
                dot.classList.toggle('is-active', dotIndex === index);
            });
        }

        function getNewsCarouselPageSize() {
            return window.matchMedia('(max-width: 768px)').matches ? 1 : 2;
        }

        function getNewsCarouselPageCount() {
            if (!newsCarouselTrack) {
                return 0;
            }

            return Math.max(1, Math.ceil(newsCarouselTrack.children.length / getNewsCarouselPageSize()));
        }

        function getNewsCarouselStep() {
            if (!newsCarouselTrack) {
                return 0;
            }

            const firstSlide = newsCarouselTrack.querySelector('.news-carousel-slide');
            if (!firstSlide) {
                return newsCarouselTrack.clientWidth;
            }

            const slideWidth = firstSlide.getBoundingClientRect().width;
            const trackStyles = window.getComputedStyle(newsCarouselTrack);
            const gap = parseFloat(trackStyles.columnGap || trackStyles.gap || '0') || 0;
            const pageSize = getNewsCarouselPageSize();
            return slideWidth * pageSize + gap * Math.max(0, pageSize - 1);
        }

        function getNewsCarouselPageIndex() {
            const step = getNewsCarouselStep();
            if (!step || !newsCarouselTrack) {
                return 0;
            }

            return Math.round(newsCarouselTrack.scrollLeft / step);
        }

        function stopNewsCarouselAutoplay() {
            if (newsCarouselAutoTimer) {
                window.clearInterval(newsCarouselAutoTimer);
                newsCarouselAutoTimer = null;
            }
        }

        function startNewsCarouselAutoplay() {
            if (!newsCarouselTrack || !newsCarouselDots) {
                return;
            }

            const pageCount = getNewsCarouselPageCount();
            if (pageCount <= 1) {
                stopNewsCarouselAutoplay();
                return;
            }

            stopNewsCarouselAutoplay();
            newsCarouselAutoTimer = window.setInterval(function () {
                if (document.hidden) {
                    return;
                }

                const latestPageCount = getNewsCarouselPageCount();
                if (latestPageCount <= 1) {
                    return;
                }

                let currentPage = getNewsCarouselPageIndex();
                if (newsCarouselAutoDirection > 0 && currentPage >= latestPageCount - 1) {
                    newsCarouselAutoDirection = -1;
                } else if (newsCarouselAutoDirection < 0 && currentPage <= 0) {
                    newsCarouselAutoDirection = 1;
                }

                currentPage = getNewsCarouselPageIndex();
                scrollNewsCarouselToPage(currentPage + newsCarouselAutoDirection);
            }, 3500);
        }

        function resetNewsCarouselAutoplay() {
            startNewsCarouselAutoplay();
        }

        function renderNewsCarouselDots() {
            if (!newsCarouselDots || !newsCarouselTrack) {
                return;
            }

            const pageCount = getNewsCarouselPageCount();
            newsCarouselDots.innerHTML = '';

            for (let pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'news-carousel-dot';
                button.dataset.carouselPage = String(pageIndex);
                button.setAttribute('aria-label', 'Chuyển tới cụm ảnh ' + (pageIndex + 1));
                newsCarouselDots.appendChild(button);
            }
        }

        function updateNewsCarouselDots() {
            if (!newsCarouselDots || !newsCarouselTrack) {
                return;
            }

            const buttons = Array.from(newsCarouselDots.querySelectorAll('.news-carousel-dot'));
            if (buttons.length === 0) {
                return;
            }

            const activePage = Math.min(buttons.length - 1, getNewsCarouselPageIndex());
            buttons.forEach(function (button, pageIndex) {
                button.classList.toggle('is-active', pageIndex === activePage);
                button.setAttribute('aria-current', pageIndex === activePage ? 'true' : 'false');
            });
        }

        function scrollNewsCarouselToPage(pageIndex) {
            if (!newsCarouselTrack) {
                return;
            }

            const step = getNewsCarouselStep();
            const pageCount = getNewsCarouselPageCount();
            if (!step || pageCount <= 0) {
                return;
            }

            const targetPage = Math.max(0, Math.min(pageIndex, pageCount - 1));
            newsCarouselTrack.scrollTo({ left: targetPage * step, behavior: 'smooth' });
            window.setTimeout(updateNewsCarouselDots, 220);
        }

        function escapeHtml(value) {
            return String(value || '')
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        function productImageSrc(image) {
            const value = String(image || '').trim();
            if (!value) {
                return PRODUCT_IMAGE_BASE + 'product-boba-milk-tea.png';
            }
            if (value.indexOf('/uploads/') === 0) {
                return '<%= request.getContextPath() %>' + value;
            }
            if (value.indexOf('http://') === 0 || value.indexOf('https://') === 0) {
                return value;
            }
            return PRODUCT_IMAGE_BASE + value;
        }

        function textOrFallback(value, fallback) {
            const text = String(value || '').trim();
            return text ? text : fallback;
        }

        function formatCurrencyText(value) {
            const normalized = String(value || '').replace(/[^0-9]/g, '');
            const amount = Number(normalized);
            if (!Number.isFinite(amount) || amount < 0) {
                return textOrFallback(value, 'Chưa cập nhật');
            }
            return moneyFormat.format(amount) + ' đ';
        }

        function openProductDetailModal(button) {
            if (!productDetailModal || !button) {
                return;
            }

            currentDetailCard = button.closest('.product-card');
            const name = textOrFallback(button.dataset.name, 'Sản phẩm');
            const codeValue = textOrFallback(button.dataset.maSp, 'Chưa cập nhật');
            const imageValue = textOrFallback(button.dataset.image, PRODUCT_IMAGE_BASE + 'product-boba-milk-tea.png');
            const categoryValue = textOrFallback(button.dataset.categoryLabel, 'Chưa cập nhật');

            if (productDetailTitle) {
                productDetailTitle.textContent = name;
            }
            if (productDetailSubtitle) {
                productDetailSubtitle.textContent = 'Mã SP: ' + codeValue + ' • Loại: ' + categoryValue;
            }
            if (productDetailImage) {
                productDetailImage.src = imageValue;
                productDetailImage.alt = 'Ảnh sản phẩm ' + name;
            }
            if (productDetailCode) {
                productDetailCode.textContent = codeValue;
            }
            if (productDetailPrice) {
                productDetailPrice.textContent = formatCurrencyText(button.dataset.priceLabel);
            }
            if (productDetailSize) {
                productDetailSize.textContent = textOrFallback(button.dataset.sizeLabel, 'Chưa cập nhật');
            }
            if (productDetailCategory) {
                productDetailCategory.textContent = categoryValue;
            }
            if (productDetailDescription) {
                productDetailDescription.textContent = textOrFallback(button.dataset.description, 'Chưa cập nhật mô tả.');
            }
            if (productDetailIngredients) {
                productDetailIngredients.textContent = textOrFallback(button.dataset.ingredients, 'Chưa cập nhật thành phần.');
            }

            productDetailModal.classList.add('is-open');
            productDetailModal.setAttribute('aria-hidden', 'false');
            if (productDetailBackdrop) {
                productDetailBackdrop.hidden = false;
            }

            if (productDetailClose) {
                window.setTimeout(function () {
                    productDetailClose.focus();
                }, 0);
            }
        }

        function closeProductDetailModal() {
            if (!productDetailModal) {
                return;
            }

            productDetailModal.classList.remove('is-open');
            productDetailModal.setAttribute('aria-hidden', 'true');
            currentDetailCard = null;
            if (productDetailBackdrop) {
                productDetailBackdrop.hidden = true;
            }
        }

        function getFilteredProducts() {
            const query = productSearchQuery.trim();
            return productCatalog.filter(function (product) {
                const matchesCategory = !selectedProductCategory || product.category === selectedProductCategory;
                const searchable = [
                    product.name,
                    product.maSP,
                    product.categoryLabel,
                    product.description,
                    product.ingredients
                ].join(' ').toLowerCase();
                const matchesSearch = !query || searchable.includes(query);
                return matchesCategory && matchesSearch;
            });
        }

        function updateCategoryTitle() {
            if (productTitle) {
                productTitle.textContent = categoryTitles[selectedProductCategory] || categoryTitles[''];
            }

            categoryButtons.forEach(function (button) {
                button.classList.toggle('is-active', button.dataset.category === selectedProductCategory);
            });
        }

        function closeCategoryMenu() {
            if (!categoryDropdown || !categoryToggle) {
                return;
            }

            categoryDropdown.classList.remove('is-open');
            categoryToggle.setAttribute('aria-expanded', 'false');
        }

        function selectProductCategory(category) {
            const nextCategory = category || '';
            selectedProductCategory = selectedProductCategory === nextCategory ? '' : nextCategory;
            productSearchQuery = '';
            if (searchInput) {
                searchInput.value = '';
            }
            currentProductPage = 0;
            updateCategoryTitle();
            renderProductCatalog();
            closeCategoryMenu();

            if (productSection) {
                productSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        }

        function renderProductCard(product) {
            return '' +
                '<article class="product-card" data-ma-sp="' + escapeHtml(product.maSP || '') + '">' +
                    '<div class="product-visual">' +
                        '<button type="button" class="product-visual-trigger" ' +
                            'title="Xem thông tin sản phẩm" ' +
                            'data-ma-sp="' + escapeHtml(product.maSP || '') + '" ' +
                            'data-name="' + escapeHtml(product.name || '') + '" ' +
                            'data-price-label="' + escapeHtml(product.priceLabel || '') + '" ' +
                            'data-price-value="' + escapeHtml(product.priceValue || '') + '" ' +
                            'data-image="' + escapeHtml(productImageSrc(product.image)) + '" ' +
                            'data-category-label="' + escapeHtml(product.categoryLabel || categoryDisplayLabels[product.category] || '') + '" ' +
                            'data-size-label="' + escapeHtml(product.sizeLabel || '') + '" ' +
                            'data-description="' + escapeHtml(product.description || '') + '" ' +
                            'data-ingredients="' + escapeHtml(product.ingredients || '') + '">' +
                            '<div class="product-visual-frame">' +
                                '<img src="' + escapeHtml(productImageSrc(product.image)) + '" alt="' + escapeHtml(product.name || '') + '">' +
                            '</div>' +
                        '</button>' +
                    '</div>' +
                    '<div class="product-info">' +
                        '<h3 class="product-name">' + escapeHtml(product.name) + '</h3>' +
                        '<p class="product-price">' + escapeHtml(product.priceLabel) + '</p>' +
                        '<button type="button" class="btn-order">' +
                            '<svg class="btn-order-icon" viewBox="0 0 24 24" aria-hidden="true">' +
                                '<path d="M3 4h2l2.2 9.2a2 2 0 0 0 2 1.6h6.9a2 2 0 0 0 1.9-1.4L20 8H7.2" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/>' +
                                '<circle cx="10" cy="20" r="1.5" fill="currentColor"></circle>' +
                                '<circle cx="17" cy="20" r="1.5" fill="currentColor"></circle>' +
                            '</svg>' +
                            '<span>Thêm vào giỏ</span>' +
                        '</button>' +
                    '</div>' +
                '</article>';
        }

        function renderProductSlide(products) {
            return '<div class="best-seller-slide"><div class="best-seller-grid">' +
                products.map(renderProductCard).join('') +
            '</div></div>';
        }

        function renderProductCatalog() {
            if (!productCarouselTrack) {
                return;
            }

            const filtered = getFilteredProducts();
            const pageSize = 10;

            if (filtered.length === 0) {
                productCarouselTrack.innerHTML = '';
                productCarouselTrack.style.transform = 'translateX(0)';

                if (emptyState) {
                    emptyState.hidden = false;
                }

                if (viewMore) {
                    viewMore.hidden = true;
                }

                if (productCarouselPrev) {
                    productCarouselPrev.disabled = true;
                }

                if (productCarouselNext) {
                    productCarouselNext.disabled = true;
                }

                return;
            }

            const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));

            if (currentProductPage >= totalPages) {
                currentProductPage = totalPages - 1;
            }
            if (currentProductPage < 0) {
                currentProductPage = 0;
            }

            const pages = [];
            for (let page = 0; page < totalPages; page += 1) {
                const start = page * pageSize;
                const products = filtered.slice(start, start + pageSize);
                pages.push(renderProductSlide(products));
            }

            productCarouselTrack.innerHTML = pages.join('');
            productCarouselTrack.style.transform = 'translateX(' + (-currentProductPage * 100) + '%)';

            if (emptyState) {
                emptyState.hidden = filtered.length !== 0;
            }

            if (viewMore) {
                viewMore.hidden = true;
            }

            if (productCarouselPrev) {
                productCarouselPrev.disabled = currentProductPage <= 0;
            }

            if (productCarouselNext) {
                productCarouselNext.disabled = currentProductPage >= totalPages - 1;
            }
        }

        function renderSearch() {
            productSearchQuery = searchInput ? searchInput.value.trim().toLowerCase() : '';
            currentProductPage = 0;
            renderProductCatalog();
        }

        function loadCart() {
            try {
                const raw = window.localStorage.getItem(CART_STORAGE_KEY);
                if (!raw) {
                    return [];
                }

                const parsed = JSON.parse(raw);
                return Array.isArray(parsed)
                    ? parsed.map(normalizeCartItem).filter(Boolean)
                    : [];
            } catch (error) {
                return [];
            }
        }

        function readCartByStorageKey(storageKey) {
            try {
                const raw = window.localStorage.getItem(storageKey);
                if (!raw) {
                    return [];
                }

                const parsed = JSON.parse(raw);
                return Array.isArray(parsed)
                    ? parsed.map(normalizeCartItem).filter(Boolean)
                    : [];
            } catch (error) {
                return [];
            }
        }

        function saveCart() {
            try {
                window.localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
            } catch (error) {
                // Bỏ qua nếu trình duyệt chặn localStorage.
            }
        }

        function normalizeCartItem(item) {
            if (!item || typeof item !== 'object') {
                return null;
            }

            const id = String(item.id || '').trim();
            if (!id) {
                return null;
            }

            return {
                id: id,
                maSP: String(item.maSP || '').trim(),
                name: String(item.name || 'Sản phẩm'),
                size: String(item.size || '').trim(),
                price: parseMoneyValue(item.price),
                image: String(item.image || ''),
                qty: Math.max(1, Number(item.qty) || 1)
            };
        }

        function mergeCartItems(primaryCart, secondaryCart) {
            const merged = [];

            function addItem(item) {
                const normalized = normalizeCartItem(item);
                if (!normalized) {
                    return;
                }

                const existing = merged.find(function (entry) {
                    return entry.id === normalized.id;
                });

                if (existing) {
                    existing.qty += normalized.qty;
                    return;
                }

                merged.push(normalized);
            }

            primaryCart.forEach(addItem);
            secondaryCart.forEach(addItem);
            return merged;
        }

        function migrateGuestCartIfNeeded() {
            if (!IS_LOGGED_IN || CART_OWNER_KEY === 'guest') {
                return;
            }

            const guestCart = readCartByStorageKey(LEGACY_GUEST_CART_STORAGE_KEY);
            if (guestCart.length === 0) {
                return;
            }

            const currentCart = readCartByStorageKey(CART_STORAGE_KEY);
            const mergedCart = mergeCartItems(currentCart, guestCart);

            try {
                window.localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(mergedCart));
                window.localStorage.removeItem(LEGACY_GUEST_CART_STORAGE_KEY);
            } catch (error) {
                // Bỏ qua nếu trình duyệt chặn localStorage.
            }
        }

        function slugify(text) {
            return String(text || '')
                .toLowerCase()
                .normalize('NFD')
                .replace(/[\u0300-\u036f]/g, '')
                .replace(/[^a-z0-9]+/g, '-')
                .replace(/^-+|-+$/g, '');
        }

        function parsePrice(text) {
            return parseMoneyValue(text);
        }

        function parseMoneyValue(value) {
            const digits = String(value || '').replace(/[^\d]/g, '');
            return digits ? Number(digits) : 0;
        }

        function formatMoney(value) {
            return moneyFormat.format(value) + ' đ';
        }

        function trimValue(value) {
            return String(value || '').trim();
        }

        function normalizeCouponCode(value) {
            return trimValue(value).toUpperCase();
        }

        function findCouponOptionByCode(code) {
            if (!couponSelect) {
                return null;
            }

            const normalized = normalizeCouponCode(code);
            if (!normalized) {
                return null;
            }

            for (let i = 0; i < couponSelect.options.length; i += 1) {
                const option = couponSelect.options[i];
                if (normalizeCouponCode(option.value) === normalized) {
                    return option;
                }
            }

            return null;
        }

        function loadPersistedCouponCode() {
            try {
                return normalizeCouponCode(window.localStorage.getItem(COUPON_STORAGE_KEY));
            } catch (error) {
                return '';
            }
        }

        function savePersistedCouponCode(code) {
            try {
                if (code) {
                    window.localStorage.setItem(COUPON_STORAGE_KEY, normalizeCouponCode(code));
                } else {
                    window.localStorage.removeItem(COUPON_STORAGE_KEY);
                }
            } catch (error) {
                // Bỏ qua nếu trình duyệt chặn localStorage.
            }
        }

        function syncCouponInputFromSelection() {
            if (!couponInput) {
                return;
            }

            couponInput.value = couponSelect ? String(couponSelect.value || '') : '';
        }

        function restoreCouponSelection() {
            const storedCode = loadPersistedCouponCode();
            if (!storedCode) {
                return;
            }

            const option = findCouponOptionByCode(storedCode);
            if (!option) {
                savePersistedCouponCode('');
                return;
            }

            if (couponSelect) {
                couponSelect.value = option.value;
            }
            syncCouponInputFromSelection();
        }

        function applyCouponCode(rawCode) {
            if (!couponSelect || couponSelect.disabled) {
                couponFeedback = 'Hiện chưa có mã giảm giá khả dụng.';
                updateCouponHint();
                return false;
            }

            const code = normalizeCouponCode(rawCode);
            if (!code) {
                couponFeedback = '';
                if (couponSelect) {
                    couponSelect.value = '';
                }
                if (couponInput) {
                    couponInput.value = '';
                }
                savePersistedCouponCode('');
                renderCart();
                return true;
            }

            const option = findCouponOptionByCode(code);
            if (!option || !option.value) {
                couponFeedback = 'Mã giảm giá không hợp lệ hoặc đã hết hạn.';
                updateCouponHint();
                return false;
            }

            couponFeedback = '';
            couponSelect.value = option.value;
            syncCouponInputFromSelection();
            savePersistedCouponCode(option.value);
            renderCart();
            return true;
        }

        function clearCouponCode() {
            couponFeedback = '';
            if (couponSelect) {
                couponSelect.value = '';
            }
            if (couponInput) {
                couponInput.value = '';
            }
            savePersistedCouponCode('');
            renderCart();
        }

        function normalizeDeliveryMethod(value) {
            const raw = trimValue(value);
            if (!raw) {
                return DELIVERY_METHOD_PICKUP;
            }

            const normalized = raw.replace(/\s+/g, '_').replace(/-/g, '_').toUpperCase();
            if (normalized === DELIVERY_METHOD_DELIVERY || normalized === 'DELIVERY') {
                return DELIVERY_METHOD_DELIVERY;
            }

            return DELIVERY_METHOD_PICKUP;
        }

        function getDeliveryAddress() {
            if (selectedDeliveryMethod !== DELIVERY_METHOD_DELIVERY) {
                return '';
            }
            return deliveryAddressInput ? trimValue(deliveryAddressInput.value) : '';
        }

        function getDeliveryMethodLabel(method) {
            return normalizeDeliveryMethod(method) === DELIVERY_METHOD_DELIVERY ? 'Giao hàng' : 'Mua tại quán';
        }

        function getDeliverySummary(method, address) {
            if (normalizeDeliveryMethod(method) === DELIVERY_METHOD_DELIVERY) {
                return 'Giao hàng tới: ' + (trimValue(address) || 'chưa có địa chỉ');
            }

            return 'Nhận hàng tại quán';
        }

        function saveDeliveryPreference() {
            try {
                window.localStorage.setItem(DELIVERY_METHOD_STORAGE_KEY, JSON.stringify({
                    method: selectedDeliveryMethod,
                    address: getDeliveryAddress()
                }));
            } catch (error) {
                // Bỏ qua nếu trình duyệt chặn localStorage.
            }
        }

        function syncDeliveryMethodControls(shouldPersist) {
            deliveryMethodButtons.forEach(function (button) {
                const method = normalizeDeliveryMethod(button.dataset.deliveryMethod);
                const isActive = method === selectedDeliveryMethod;
                button.classList.toggle('is-active', isActive);
                button.setAttribute('aria-pressed', isActive ? 'true' : 'false');
            });

            if (deliveryAddressWrap) {
                deliveryAddressWrap.hidden = selectedDeliveryMethod !== DELIVERY_METHOD_DELIVERY;
            }

            if (deliveryAddressInput) {
                deliveryAddressInput.required = selectedDeliveryMethod === DELIVERY_METHOD_DELIVERY;
                deliveryAddressInput.disabled = selectedDeliveryMethod !== DELIVERY_METHOD_DELIVERY;
                if (selectedDeliveryMethod !== DELIVERY_METHOD_DELIVERY) {
                    deliveryAddressInput.value = '';
                }
            }

            if (shouldPersist !== false) {
                saveDeliveryPreference();
            }
        }

        function loadDeliveryPreference() {
            let stored = null;
            try {
                stored = window.localStorage.getItem(DELIVERY_METHOD_STORAGE_KEY);
            } catch (error) {
                stored = null;
            }

            if (stored) {
                try {
                    const parsed = JSON.parse(stored);
                    selectedDeliveryMethod = normalizeDeliveryMethod(parsed && parsed.method);
                    if (deliveryAddressInput && trimValue(parsed && parsed.address)) {
                        deliveryAddressInput.value = trimValue(parsed.address);
                    }
                } catch (error) {
                    selectedDeliveryMethod = DELIVERY_METHOD_PICKUP;
                }
            }

            if (selectedDeliveryMethod !== DELIVERY_METHOD_DELIVERY) {
                if (deliveryAddressInput) {
                    deliveryAddressInput.value = '';
                }
            }

            syncDeliveryMethodControls(false);
        }

        function setDeliveryMethod(method) {
            selectedDeliveryMethod = normalizeDeliveryMethod(method);
            if (selectedDeliveryMethod !== DELIVERY_METHOD_DELIVERY && deliveryAddressInput) {
                deliveryAddressInput.value = '';
            }
            syncDeliveryMethodControls(true);
        }

        function validateDeliveryChoice() {
            if (selectedDeliveryMethod !== DELIVERY_METHOD_DELIVERY) {
                return true;
            }

            const address = getDeliveryAddress();
            if (!address) {
                window.alert('Vui lòng nhập địa chỉ giao hàng.');
                if (deliveryAddressInput) {
                    deliveryAddressInput.focus();
                }
                return false;
            }

            return true;
        }

        function normalizeBankCode(input) {
            const raw = trimValue(input);
            if (!raw) {
                return '';
            }

            const compact = raw.toLowerCase().replace(/[^a-z0-9]/g, '');
            return bankCodeMap[compact] || raw.toUpperCase();
        }

        function buildPaymentReference(note, sessionId) {
            const token = trimValue(sessionId);
            if (token) {
                return token;
            }

            return trimValue(note) || defaultPaymentConfig.note;
        }

        function buildPaymentTransferText(amount, config, sessionId) {
            return buildPaymentReference(config.note, sessionId);
        }

        function buildPaymentQrUrl(amount, config, paymentNote) {
            const bankCode = normalizeBankCode(config.bank);
            const accountNo = trimValue(config.accountNo);
            if (!bankCode || !accountNo) {
                return '';
            }

            const params = new URLSearchParams();
            params.set('amount', String(Math.max(0, Math.round(amount))));
            params.set('addInfo', trimValue(paymentNote) || buildPaymentReference(config.note, ''));

            const accountName = trimValue(config.accountName);
            if (accountName) {
                params.set('accountName', accountName);
            }

            return 'https://img.vietqr.io/image/' + encodeURIComponent(bankCode) + '-' + encodeURIComponent(accountNo) + '-compact2.png?' + params.toString();
        }

        function clearPaymentTimers() {
            if (paymentPollTimer) {
                window.clearInterval(paymentPollTimer);
                paymentPollTimer = null;
            }

            if (paymentCountdownTimer) {
                window.clearInterval(paymentCountdownTimer);
                paymentCountdownTimer = null;
            }
        }

        function getPaymentStatusLabel(status) {
            if (status === 'completed') {
                return 'Hoàn thành';
            }

            if (status === 'failed') {
                return 'Thanh toán thất bại';
            }

            return 'Chờ thanh toán';
        }

        function formatRemainingTime(remainingSeconds) {
            const totalSeconds = Math.max(0, Math.ceil(Number(remainingSeconds) || 0));
            const minutes = Math.floor(totalSeconds / 60);
            const seconds = totalSeconds % 60;

            if (minutes <= 0) {
                return 'Còn ' + seconds + ' giây';
            }

            if (seconds <= 0) {
                return 'Còn ' + minutes + ' phút';
            }

            return 'Còn ' + minutes + ' phút ' + seconds + ' giây';
        }

        function updatePaymentConfirmButton(status) {
            if (!paymentConfirm) {
                return;
            }

            const normalizedStatus = status === 'completed' || status === 'failed' || status === 'loading' || status === 'unavailable' || status === 'busy'
                ? status
                : 'pending';

            if (normalizedStatus === 'completed') {
                paymentConfirm.textContent = 'Hoàn thành';
                paymentConfirm.disabled = true;
                return;
            }

            if (normalizedStatus === 'failed') {
                paymentConfirm.textContent = 'Phiên hết hạn';
                paymentConfirm.disabled = true;
                return;
            }

            if (normalizedStatus === 'busy') {
                paymentConfirm.textContent = 'Đang xác nhận...';
                paymentConfirm.disabled = true;
                return;
            }

            if (normalizedStatus === 'loading') {
                paymentConfirm.textContent = 'Đang tạo phiên...';
                paymentConfirm.disabled = true;
                return;
            }

            if (normalizedStatus === 'unavailable') {
                paymentConfirm.textContent = 'Không khả dụng';
                paymentConfirm.disabled = true;
                return;
            }

            paymentConfirm.textContent = 'Xác nhận đã thanh toán';
            paymentConfirm.disabled = false;
        }

        function getAppliedCouponSummary(session) {
            if (!session) {
                return '';
            }

            const code = String(session.promotionCode || '').trim();
            const name = String(session.promotionName || '').trim();
            const type = String(session.promotionType || '').trim().toUpperCase();
            const percent = Number(session.promotionPercent || 0);
            const discountAmount = Number(session.discountAmount || 0);
            const maxAmount = Number(session.promotionMaxAmount || 0);
            if (!code) {
                return '';
            }

            const display = name ? (code + ' - ' + name) : code;
            if (type === 'AMOUNT') {
                return display + ' (Giảm ' + formatMoney(Math.max(0, discountAmount)) + ')';
            }

            if (!Number.isFinite(percent) || percent <= 0) {
                return display;
            }

            const limitText = Number.isFinite(maxAmount) && maxAmount > 0 ? (' - Tối đa ' + formatMoney(maxAmount)) : '';
            return display + ' (' + percent + '%' + limitText + ')';
        }

        function buildPaymentSubtitle(session) {
            const deliveryMethod = session && session.deliveryMethod ? session.deliveryMethod : selectedDeliveryMethod;
            const deliveryAddress = session && session.deliveryAddress ? session.deliveryAddress : getDeliveryAddress();
            const methodSummary = getDeliverySummary(deliveryMethod, deliveryAddress);
            const couponSummary = getAppliedCouponSummary(session);

            if (couponSummary) {
                return methodSummary + ' | Đã áp dụng ' + couponSummary + '.';
            }

            return methodSummary + '.';
        }

        function updatePaymentDiscountSummary(session, fallbackAmount) {
            const originalAmount = session && Number.isFinite(Number(session.originalAmount))
                ? Number(session.originalAmount)
                : Number(fallbackAmount) || 0;
            const discountAmount = session && Number.isFinite(Number(session.discountAmount))
                ? Math.max(0, Number(session.discountAmount))
                : 0;

            if (paymentOriginalAmountText) {
                paymentOriginalAmountText.textContent = formatMoney(originalAmount);
            }

            if (paymentDiscountAmountText) {
                paymentDiscountAmountText.textContent = discountAmount > 0 ? ('-' + formatMoney(discountAmount)) : formatMoney(0);
            }
        }

        function renderPaymentStatus(status, remainingSeconds) {
            const normalizedStatus = status === 'completed' || status === 'failed' ? status : 'pending';

            if (paymentStatusBadge) {
                paymentStatusBadge.className = 'payment-status-badge payment-status-' + normalizedStatus;
                paymentStatusBadge.textContent = getPaymentStatusLabel(normalizedStatus);
            }

            if (paymentStatusCountdown) {
                if (normalizedStatus === 'completed') {
                    paymentStatusCountdown.textContent = 'Hoàn thành';
                } else if (normalizedStatus === 'failed') {
                    paymentStatusCountdown.textContent = 'Phiên thanh toán đã hết hạn';
                } else {
                    paymentStatusCountdown.textContent = formatRemainingTime(remainingSeconds);
                }
            }

            if (paymentConfirmInFlight) {
                updatePaymentConfirmButton('busy');
            } else if (normalizedStatus === 'completed') {
                updatePaymentConfirmButton('completed');
            } else if (normalizedStatus === 'failed') {
                updatePaymentConfirmButton('failed');
            } else if (activePaymentSession && String(activePaymentSession.sessionId || '').trim()) {
                updatePaymentConfirmButton('pending');
            } else {
                updatePaymentConfirmButton('loading');
            }

            if (paymentPanelSubtitle) {
                if (normalizedStatus === 'completed') {
                    paymentPanelSubtitle.textContent = buildPaymentSubtitle(activePaymentSession) + ' Đơn đã được tạo.';
                } else if (normalizedStatus === 'failed') {
                    paymentPanelSubtitle.textContent = 'Phiên thanh toán đã hết hạn.';
                } else {
                    paymentPanelSubtitle.textContent = buildPaymentSubtitle(activePaymentSession);
                }
            }
        }

        function updatePaymentView(amount, session) {
            const paymentNote = session && session.paymentNote ? session.paymentNote : buildPaymentReference(paymentConfig.note, '');
            const displayAmount = session && Number.isFinite(Number(session.amount))
                ? Number(session.amount)
                : amount;
            const qrUrl = buildPaymentQrUrl(displayAmount, paymentConfig, paymentNote);

            updatePaymentDiscountSummary(session, amount);

            if (paymentAmountText) {
                paymentAmountText.textContent = formatMoney(displayAmount);
            }

            if (paymentStatusBadge) {
                paymentStatusBadge.className = 'payment-status-badge payment-status-pending';
                paymentStatusBadge.textContent = 'Chờ thanh toán';
            }

            if (paymentStatusCountdown) {
                const initialRemainingSeconds = session && Number.isFinite(Number(session.remainingSeconds))
                    ? Number(session.remainingSeconds)
                    : PAYMENT_TIMEOUT_MS / 1000;
                paymentStatusCountdown.textContent = formatRemainingTime(initialRemainingSeconds);
            }

            if (paymentPanelSubtitle) {
                paymentPanelSubtitle.textContent = buildPaymentSubtitle(session);
            }

            if (paymentTransferText) {
                paymentTransferText.textContent = paymentNote;
            }
            if (paymentTransferInput) {
                paymentTransferInput.value = paymentNote;
            }

            if (paymentCopy) {
                paymentCopy.textContent = 'Copy nội dung';
                paymentCopy.disabled = false;
            }

            if (paymentQrImage && paymentQrFallback) {
                if (qrUrl) {
                    paymentQrImage.src = qrUrl;
                    paymentQrImage.alt = 'Mã QR thanh toán ' + formatMoney(displayAmount);
                    paymentQrImage.hidden = false;
                    paymentQrFallback.classList.remove('is-visible');
                    paymentQrFallback.textContent = PAYMENT_QR_FALLBACK_TEXT || paymentQrFallback.textContent;
                } else {
                    paymentQrImage.removeAttribute('src');
                    paymentQrImage.hidden = true;
                    paymentQrFallback.textContent = PAYMENT_QR_FALLBACK_TEXT || 'Chưa đủ thông tin ngân hàng để tạo QR.';
                    paymentQrFallback.classList.add('is-visible');
                }
            }
        }

        function resetPaymentSessionState() {
            activePaymentSession = null;
            activePaymentSessionExpiresAt = 0;
            activePaymentAmount = 0;
            activePaymentToken += 1;
            paymentConfirmInFlight = false;
            clearPaymentTimers();
        }

        async function createPaymentSession(amount) {
            const payload = new URLSearchParams();
            payload.set('action', 'create');
            payload.set('amount', String(Math.max(0, Math.round(amount))));
            payload.set('ownerKey', CART_OWNER_KEY);
            payload.set('note', defaultPaymentConfig.note);
            payload.set('cartJson', JSON.stringify(cart));
            payload.set('deliveryMethod', selectedDeliveryMethod);
            if (selectedDeliveryMethod === DELIVERY_METHOD_DELIVERY) {
                payload.set('deliveryAddress', getDeliveryAddress());
            }
            const couponCode = getSelectedCouponCode();
            if (couponCode) {
                payload.set('couponCode', couponCode);
            }

            const response = await fetch(PAYMENT_SESSION_URL, {
                method: 'POST',
                body: payload,
                credentials: 'same-origin',
                headers: {
                    'Accept': 'application/json'
                }
            });

            const data = await response.json().catch(function () {
                return null;
            });

            if (!response.ok || !data || !data.sessionId) {
                throw new Error((data && data.message) || 'Không thể tạo phiên thanh toán.');
            }

            return data;
        }

        async function completePaymentSession(sessionId, flowToken) {
            const payload = new URLSearchParams();
            payload.set('action', 'complete');
            payload.set('id', sessionId);
            payload.set('source', 'manual-confirmation');

            const response = await fetch(PAYMENT_SESSION_URL, {
                method: 'POST',
                body: payload,
                credentials: 'same-origin',
                headers: {
                    'Accept': 'application/json'
                }
            });

            const data = await response.json().catch(function () {
                return null;
            });

            if (!response.ok || !data || !data.sessionId) {
                throw new Error((data && data.message) || 'Không thể xác nhận thanh toán.');
            }

            paymentConfirmInFlight = false;
            applyPaymentSessionState(data, flowToken);
            return data;
        }

        async function refreshPaymentSessionStatus(sessionId, flowToken) {
            if (!sessionId || flowToken !== activePaymentToken) {
                return;
            }

            try {
                const response = await fetch(PAYMENT_SESSION_URL + '?action=status&id=' + encodeURIComponent(sessionId), {
                    cache: 'no-store',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json'
                    }
                });

                const data = await response.json().catch(function () {
                    return null;
                });

                if (!response.ok || !data || flowToken !== activePaymentToken) {
                    return;
                }

                applyPaymentSessionState(data, flowToken);
            } catch (error) {
                // Bỏ qua lỗi polling tạm thời.
            }
        }

        function applyPaymentSessionState(data, flowToken) {
            if (!data || flowToken !== activePaymentToken) {
                return;
            }

            const statusRaw = String(data.status || '').toLowerCase();
            const status = statusRaw === 'completed' || statusRaw === 'failed' ? statusRaw : 'pending';
            const remainingSeconds = Number(data.remainingSeconds);
            const expiresAt = Number(data.expiresAt);

            if (status === 'completed' || status === 'failed') {
                paymentConfirmInFlight = false;
            }

            activePaymentSession = data;
            activePaymentSessionExpiresAt = Number.isFinite(expiresAt) ? expiresAt : activePaymentSessionExpiresAt;
            updatePaymentDiscountSummary(data, activePaymentAmount || Number(data.amount) || 0);

            if (paymentTransferText && data.paymentNote) {
                paymentTransferText.textContent = data.paymentNote;
            }
            if (paymentTransferInput && data.paymentNote) {
                paymentTransferInput.value = data.paymentNote;
            }

            if (status === 'pending' && Number.isFinite(remainingSeconds) && remainingSeconds <= 0) {
                renderPaymentStatus('failed', 0);
                clearPaymentTimers();
                return;
            }

            renderPaymentStatus(status, Number.isFinite(remainingSeconds) ? remainingSeconds : PAYMENT_TIMEOUT_MS / 1000);

            if (status === 'completed') {
                clearPaymentTimers();
                cart = [];
                renderCart();
                return;
            }

            if (status === 'failed') {
                clearPaymentTimers();
            }
        }

        function startPaymentTracking(session) {
            const sessionId = session ? String(session.sessionId || '').trim() : '';
            if (!sessionId) {
                return;
            }

            const flowToken = activePaymentToken;
            const expiresAt = Number(session.expiresAt) || (Date.now() + PAYMENT_TIMEOUT_MS);
            activePaymentSession = session;
            activePaymentSessionExpiresAt = expiresAt;
            activePaymentAmount = Number(session.amount) || activePaymentAmount;

            clearPaymentTimers();

            paymentCountdownTimer = window.setInterval(function () {
                if (flowToken !== activePaymentToken) {
                    clearPaymentTimers();
                    return;
                }

                const remainingSeconds = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000));

                if (remainingSeconds <= 0) {
                    renderPaymentStatus('failed', 0);
                    clearPaymentTimers();
                    return;
                }

                if (!activePaymentSession || String(activePaymentSession.status || '').toLowerCase() !== 'completed') {
                    renderPaymentStatus('pending', remainingSeconds);
                }
            }, 1000);

            paymentPollTimer = window.setInterval(function () {
                refreshPaymentSessionStatus(sessionId, flowToken);
            }, 3000);

            renderPaymentStatus('pending', Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000)));
        }

        async function openPaymentModal(amount) {
            if (!paymentModal || !paymentBackdrop) {
                return;
            }

            if (!validateDeliveryChoice()) {
                return;
            }

            closeCart();
            resetPaymentSessionState();
            activePaymentAmount = amount;

            paymentBackdrop.hidden = false;
            paymentModal.classList.add('is-open');
            paymentModal.setAttribute('aria-hidden', 'false');

            if (paymentQrFallback) {
                paymentQrFallback.textContent = 'Đang khởi tạo phiên thanh toán...';
                paymentQrFallback.classList.add('is-visible');
            }

            if (paymentQrImage) {
                paymentQrImage.hidden = true;
                paymentQrImage.removeAttribute('src');
            }

            if (paymentAmountText) {
                paymentAmountText.textContent = formatMoney(amount);
            }

            updatePaymentDiscountSummary({
                originalAmount: getCartSubtotal(),
                discountAmount: getCartDiscountAmount(),
                promotionCode: getSelectedCouponCode(),
                promotionName: getSelectedCouponLabel(),
                promotionPercent: getSelectedCouponPercent()
            }, amount);

            if (paymentTransferText) {
                paymentTransferText.textContent = 'Đang tạo mã giao dịch...';
            }
            if (paymentTransferInput) {
                paymentTransferInput.value = 'Đang tạo mã giao dịch...';
            }

            renderPaymentStatus('pending', PAYMENT_TIMEOUT_MS / 1000);
            const flowToken = activePaymentToken;

            try {
                const session = await createPaymentSession(amount);

                if (!paymentModal || !paymentBackdrop) {
                    return;
                }

                if (flowToken !== activePaymentToken) {
                    return;
                }

                updatePaymentView(amount, session);
                startPaymentTracking(session);
            } catch (error) {
                if (flowToken !== activePaymentToken) {
                    return;
                }

                const message = error && error.message
                    ? String(error.message)
                    : 'Không thể khởi tạo phiên thanh toán.';

                if (paymentStatusBadge) {
                    paymentStatusBadge.className = 'payment-status-badge payment-status-failed';
                    paymentStatusBadge.textContent = 'Thanh toán thất bại';
                }

                if (paymentStatusCountdown) {
                    paymentStatusCountdown.textContent = 'Lỗi tạo phiên';
                }

                if (paymentPanelSubtitle) {
                    paymentPanelSubtitle.textContent = message;
                }

                if (paymentQrFallback) {
                    paymentQrFallback.textContent = message;
                    paymentQrFallback.classList.add('is-visible');
                }

                if (paymentQrImage) {
                    paymentQrImage.hidden = true;
                    paymentQrImage.removeAttribute('src');
                }

                updatePaymentConfirmButton('unavailable');
            }
        }

        function closePaymentModal() {
            if (!paymentModal || !paymentBackdrop) {
                return;
            }

            resetPaymentSessionState();
            paymentModal.classList.remove('is-open');
            paymentModal.setAttribute('aria-hidden', 'true');
            paymentBackdrop.hidden = true;

            if (paymentPanelSubtitle) {
                const couponSummary = getAppliedCouponSummary(activePaymentSession);
                paymentPanelSubtitle.textContent = couponSummary
                    ? ('Đã áp dụng ' + couponSummary + '.')
                    : 'QR sẽ đổi theo tổng tiền trong giỏ hàng.';
            }

            if (paymentQrFallback) {
                paymentQrFallback.textContent = PAYMENT_QR_FALLBACK_TEXT || 'Chưa đủ thông tin ngân hàng để tạo QR.';
            }

            if (paymentOriginalAmountText) {
                paymentOriginalAmountText.textContent = formatMoney(0);
            }

            if (paymentDiscountAmountText) {
                paymentDiscountAmountText.textContent = formatMoney(0);
            }

            updatePaymentConfirmButton('loading');
        }

        function syncCheckoutLabel() {
            if (!cartCheckout) {
                return;
            }

            cartCheckout.textContent = IS_LOGGED_IN ? 'Thanh toán QR' : 'Đăng nhập để thanh toán';
        }

        function getCartQuantity() {
            return cart.reduce(function (total, item) {
                return total + item.qty;
            }, 0);
        }

        function getSelectedCouponOption() {
            if (!couponSelect) {
                return null;
            }

            if (couponSelect.selectedIndex < 0) {
                return null;
            }

            return couponSelect.options[couponSelect.selectedIndex] || null;
        }

        function getSelectedCouponCode() {
            const option = getSelectedCouponOption();
            if (!option || !option.value) {
                return '';
            }

            return String(option.value || '').trim();
        }

        function getSelectedCouponLabel() {
            const option = getSelectedCouponOption();
            if (!option || !option.value) {
                return '';
            }

            return String(option.dataset.label || option.textContent || '').trim();
        }

        function getSelectedCouponType() {
            const option = getSelectedCouponOption();
            if (!option || !option.value) {
                return '';
            }

            return String(option.dataset.type || '').trim().toUpperCase();
        }

        function getSelectedCouponPercent() {
            const option = getSelectedCouponOption();
            if (!option || !option.value) {
                return 0;
            }

            const percent = Number(option.dataset.percent || 0);
            if (!Number.isFinite(percent) || percent <= 0) {
                return 0;
            }

            return Math.min(percent, 99.99);
        }

        function getSelectedCouponAmount() {
            const option = getSelectedCouponOption();
            if (!option || !option.value) {
                return 0;
            }

            const amount = Number(option.dataset.amount || 0);
            return Number.isFinite(amount) && amount > 0 ? Math.max(0, Math.round(amount)) : 0;
        }

        function getSelectedCouponMaxAmount() {
            const option = getSelectedCouponOption();
            if (!option || !option.value) {
                return 0;
            }

            const amount = Number(option.dataset.max || 0);
            return Number.isFinite(amount) && amount > 0 ? Math.max(0, Math.round(amount)) : 0;
        }

        function getCartSubtotal() {
            return cart.reduce(function (total, item) {
                return total + (Math.max(0, parseMoneyValue(item.price)) * item.qty);
            }, 0);
        }

        function getCartDiscountAmount() {
            const subtotal = getCartSubtotal();
            const type = getSelectedCouponType();
            const percent = getSelectedCouponPercent();
            const amount = getSelectedCouponAmount();
            const maxAmount = getSelectedCouponMaxAmount();

            if (subtotal <= 0) {
                return 0;
            }

            if (type === 'AMOUNT') {
                return Math.max(0, Math.min(amount, subtotal));
            }

            if (percent <= 0) {
                return 0;
            }

            const discountedTotal = Math.max(0, Math.round(subtotal * (100 - percent) / 100));
            const discount = Math.max(0, subtotal - discountedTotal);
            if (maxAmount > 0) {
                return Math.min(discount, maxAmount);
            }

            return discount;
        }

        function getCartTotal() {
            return Math.max(0, getCartSubtotal() - getCartDiscountAmount());
        }

        function updateCouponHint() {
            if (!couponHint) {
                return;
            }

            if (!couponSelect || couponSelect.disabled) {
                couponHint.textContent = 'Chưa có mã giảm giá khả dụng.';
                return;
            }

            if (couponFeedback) {
                couponHint.textContent = couponFeedback;
                return;
            }

            const label = getSelectedCouponLabel();
            if (label) {
                couponHint.textContent = 'Đang áp dụng ' + label + '.';
                return;
            }

            const draftCode = couponInput ? normalizeCouponCode(couponInput.value) : '';
            if (!draftCode) {
                couponHint.textContent = 'Nhập mã rồi bấm Áp dụng để giảm trực tiếp vào đơn hàng.';
                return;
            }

            const draftOption = findCouponOptionByCode(draftCode);
            if (!draftOption) {
                couponHint.textContent = 'Mã chưa hợp lệ. Bấm Áp dụng sau khi nhập đúng code.';
                return;
            }

            const draftPercent = Number(draftOption.dataset.percent || 0);
            if (draftPercent > 0) {
                couponHint.textContent = 'Mã hợp lệ. Bấm Áp dụng để giảm ' + String(draftPercent).replace(/\.0+$/, '') + '%.';
            } else {
                couponHint.textContent = 'Mã hợp lệ. Bấm Áp dụng để giảm trực tiếp vào đơn hàng.';
            }
        }

        function openCart() {
            if (!cartDrawer || !cartBackdrop || !cartToggle) {
                return;
            }

            cartDrawer.classList.add('is-open');
            cartBackdrop.hidden = false;
            cartDrawer.setAttribute('aria-hidden', 'false');
            cartToggle.setAttribute('aria-expanded', 'true');
        }

        function closeCart() {
            if (!cartDrawer || !cartBackdrop || !cartToggle) {
                return;
            }

            cartDrawer.classList.remove('is-open');
            cartBackdrop.hidden = true;
            cartDrawer.setAttribute('aria-hidden', 'true');
            cartToggle.setAttribute('aria-expanded', 'false');
        }

        function openUserPanel() {
            if (!userPanel || !userToggle) {
                return;
            }

            userPanel.classList.add('is-open');
            userPanel.setAttribute('aria-hidden', 'false');
            userToggle.setAttribute('aria-expanded', 'true');
        }

        function closeUserPanel() {
            if (!userPanel || !userToggle) {
                return;
            }

            userPanel.classList.remove('is-open');
            userPanel.setAttribute('aria-hidden', 'true');
            userToggle.setAttribute('aria-expanded', 'false');
        }

        function syncCartSummary() {
            if (cartCount) {
                cartCount.textContent = String(getCartQuantity());
            }

            const subtotal = getCartSubtotal();
            const discount = getCartDiscountAmount();
            const total = Math.max(0, subtotal - discount);

            if (cartSubtotal) {
                cartSubtotal.textContent = formatMoney(subtotal);
            }

            if (cartDiscount) {
                cartDiscount.textContent = discount > 0 ? ('-' + formatMoney(discount)) : formatMoney(0);
            }

            if (cartTotal) {
                cartTotal.textContent = formatMoney(total);
            }

            updateCouponHint();
            syncCheckoutLabel();
        }

        function renderCart() {
            if (!cartItems) {
                return;
            }

            cartItems.innerHTML = '';

            if (cart.length === 0) {
                const empty = document.createElement('div');
                empty.className = 'shop-cart-empty';
                empty.textContent = 'Chưa có sản phẩm nào.';
                cartItems.appendChild(empty);
                syncCartSummary();
                saveCart();
                return;
            }

            cart.forEach(function (item) {
                const row = document.createElement('article');
                row.className = 'shop-cart-item';

                const thumb = document.createElement('div');
                thumb.className = 'shop-cart-thumb';

                const image = document.createElement('img');
                image.src = item.image;
                image.alt = item.name;
                thumb.appendChild(image);

                const body = document.createElement('div');
                body.className = 'shop-cart-body';

                const title = document.createElement('h3');
                title.textContent = item.size ? item.name + ' - Size ' + item.size : item.name;

                const price = document.createElement('p');
                price.className = 'shop-cart-price';
                price.textContent = formatMoney(item.price);

                const controls = document.createElement('div');
                controls.className = 'shop-cart-controls';

                const minus = document.createElement('button');
                minus.type = 'button';
                minus.className = 'shop-cart-qty-btn';
                minus.dataset.cartAction = 'decrease';
                minus.dataset.cartId = item.id;
                minus.setAttribute('aria-label', 'Giảm số lượng');
                minus.textContent = '−';

                const qty = document.createElement('span');
                qty.className = 'shop-cart-qty';
                qty.textContent = String(item.qty);

                const plus = document.createElement('button');
                plus.type = 'button';
                plus.className = 'shop-cart-qty-btn';
                plus.dataset.cartAction = 'increase';
                plus.dataset.cartId = item.id;
                plus.setAttribute('aria-label', 'Tăng số lượng');
                plus.textContent = '+';

                const remove = document.createElement('button');
                remove.type = 'button';
                remove.className = 'shop-cart-remove';
                remove.dataset.cartAction = 'remove';
                remove.dataset.cartId = item.id;
                remove.setAttribute('aria-label', 'Xóa sản phẩm');
                remove.textContent = 'Xóa';

                controls.appendChild(minus);
                controls.appendChild(qty);
                controls.appendChild(plus);
                controls.appendChild(remove);

                body.appendChild(title);
                body.appendChild(price);
                body.appendChild(controls);

                row.appendChild(thumb);
                row.appendChild(body);
                cartItems.appendChild(row);
            });

            syncCartSummary();
            saveCart();
        }

        function addProductFromCard(card) {
            addProductFromCardWithSize(card, 'M');
        }

        function addProductFromCardWithSize(card, size, action) {
            if (!card) {
                return;
            }

            const nameNode = card.querySelector('.product-name');
            const priceNode = card.querySelector('.product-price');
            const imageNode = card.querySelector('.product-visual-frame img');
            const name = nameNode ? nameNode.textContent.trim() : 'Sản phẩm';
            const sizeValue = String(size || 'M').toUpperCase();
            const maSP = String(card.dataset.maSp || '').trim();
            const id = maSP ? (maSP + '-' + sizeValue) : slugify(name + '-' + sizeValue);
            const price = parseMoneyValue(card.dataset.priceValue || (priceNode ? priceNode.textContent : '0'));
            const image = imageNode ? imageNode.getAttribute('src') : '';

            if (!id) {
                return;
            }

            const existing = cart.find(function (item) {
                return item.id === id;
            });

            if (existing) {
                existing.qty += 1;
            } else {
                cart.push({
                    id: id,
                    maSP: maSP,
                    name: name,
                    size: sizeValue,
                    price: price,
                    image: image,
                    qty: 1
                });
            }

            renderCart();

            if (action === 'buy-now') {
                if (cartCheckout) {
                    cartCheckout.click();
                } else if (IS_LOGGED_IN) {
                    openPaymentModal(getCartTotal());
                } else {
                    window.location.href = LOGIN_URL;
                }
                return;
            }

            openCart();
        }

        function openSizeModal(card, action) {
            if (!sizeModal || !sizeBackdrop) {
                return;
            }

            pendingProductCard = card;
            pendingProductAction = action === 'buy-now' ? 'buy-now' : 'cart';
            if (sizeSubtitle) {
                sizeSubtitle.textContent = pendingProductAction === 'buy-now'
                    ? 'Chọn một size để đặt mua và thanh toán.'
                    : 'Chọn một size để thêm vào giỏ hàng.';
            }
            sizeBackdrop.hidden = false;
            sizeModal.classList.add('is-open');
            sizeModal.setAttribute('aria-hidden', 'false');
        }

        function closeSizeModal() {
            if (!sizeModal || !sizeBackdrop) {
                return;
            }

            pendingProductCard = null;
            pendingProductAction = 'cart';
            if (sizeSubtitle) {
                sizeSubtitle.textContent = 'Chọn một size để thêm vào giỏ hàng.';
            }
            sizeModal.classList.remove('is-open');
            sizeModal.setAttribute('aria-hidden', 'true');
            sizeBackdrop.hidden = true;
        }

        function changeCartQty(id, delta) {
            cart = cart
                .map(function (item) {
                    if (item.id !== id) {
                        return item;
                    }

                    return {
                        id: item.id,
                        name: item.name,
                        price: item.price,
                        image: item.image,
                        qty: item.qty + delta
                    };
                })
                .filter(function (item) {
                    return item.qty > 0;
                });

            renderCart();
        }

        if (cartToggle) {
            cartToggle.addEventListener('click', function () {
                if (cartDrawer && cartDrawer.classList.contains('is-open')) {
                    closeCart();
                } else {
                    openCart();
                }
            });
        }

        if (cartClose) {
            cartClose.addEventListener('click', closeCart);
        }

        if (cartBackdrop) {
            cartBackdrop.addEventListener('click', closeCart);
        }

        if (cartClear) {
            cartClear.addEventListener('click', function () {
                cart = [];
                renderCart();
                closeCart();
            });
        }

        if (couponInput) {
            couponInput.addEventListener('input', function () {
                this.value = normalizeCouponCode(this.value);
                couponFeedback = '';
                updateCouponHint();
            });

            couponInput.addEventListener('keydown', function (event) {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    applyCouponCode(couponInput.value);
                }
            });
        }

        if (couponApply) {
            couponApply.addEventListener('click', function () {
                applyCouponCode(couponInput ? couponInput.value : '');
            });
        }

        if (couponClear) {
            couponClear.addEventListener('click', function () {
                clearCouponCode();
            });
        }

        if (couponSelect) {
            couponSelect.addEventListener('change', function () {
                syncCouponInputFromSelection();
                savePersistedCouponCode(getSelectedCouponCode());
                couponFeedback = '';
                renderCart();
            });
        }

        deliveryMethodButtons.forEach(function (button) {
            button.addEventListener('click', function () {
                setDeliveryMethod(button.dataset.deliveryMethod || DELIVERY_METHOD_PICKUP);
            });
        });

        if (deliveryAddressInput) {
            deliveryAddressInput.addEventListener('input', saveDeliveryPreference);
            deliveryAddressInput.addEventListener('change', saveDeliveryPreference);
        }

        if (cartCheckout) {
            cartCheckout.addEventListener('click', async function () {
                if (cart.length === 0) {
                    return;
                }

                if (!IS_LOGGED_IN) {
                    window.location.href = LOGIN_URL;
                    return;
                }

                cartCheckout.disabled = true;

                try {
                    await openPaymentModal(getCartTotal());
                } finally {
                    cartCheckout.disabled = false;
                }
            });
        }

        if (paymentClose) {
            paymentClose.addEventListener('click', closePaymentModal);
        }

        if (paymentBackdrop) {
            paymentBackdrop.addEventListener('click', closePaymentModal);
        }

        if (paymentCopy) {
            paymentCopy.addEventListener('click', async function () {
                const payload = activePaymentSession && activePaymentSession.paymentNote
                    ? activePaymentSession.paymentNote
                    : buildPaymentTransferText(activePaymentAmount || getCartTotal(), paymentConfig, activePaymentSession ? activePaymentSession.sessionId : '');

                try {
                    await navigator.clipboard.writeText(payload);
                    paymentCopy.textContent = 'Đã copy';
                    window.setTimeout(function () {
                        paymentCopy.textContent = 'Copy nội dung';
                    }, 1200);
                } catch (error) {
                    window.alert('Không thể copy nội dung thanh toán.');
                }
            });
        }

        if (paymentConfirm) {
            paymentConfirm.addEventListener('click', async function () {
                const sessionId = activePaymentSession ? String(activePaymentSession.sessionId || '').trim() : '';
                const sessionStatus = activePaymentSession ? String(activePaymentSession.status || '').toLowerCase() : '';

                if (!sessionId || sessionStatus !== 'pending') {
                    return;
                }

                if (!window.confirm('Xác nhận giao dịch này đã được thanh toán?')) {
                    return;
                }

                const flowToken = activePaymentToken;
                paymentConfirmInFlight = true;
                paymentConfirm.disabled = true;
                paymentConfirm.textContent = 'Đang xác nhận...';

                try {
                    await completePaymentSession(sessionId, flowToken);
                } catch (error) {
                    if (flowToken !== activePaymentToken) {
                        return;
                    }

                    paymentConfirmInFlight = false;
                    window.alert((error && error.message) || 'Không thể xác nhận thanh toán.');
                    renderPaymentStatus('pending', Math.max(0, Math.ceil((activePaymentSessionExpiresAt - Date.now()) / 1000)));
                }
            });
        }

        if (userToggle) {
            userToggle.addEventListener('click', function (event) {
                event.stopPropagation();
                if (userPanel && userPanel.classList.contains('is-open')) {
                    closeUserPanel();
                } else {
                    openUserPanel();
                }
            });
        }

        if (userClose) {
            userClose.addEventListener('click', closeUserPanel);
        }

        if (userDelete) {
            userDelete.addEventListener('click', function (event) {
                if (!window.confirm('Xóa hồ sơ khách hàng này?')) {
                    event.preventDefault();
                }
            });
        }

        if (mobileCartButton && cartToggle) {
            mobileCartButton.addEventListener('click', function () {
                cartToggle.click();
            });
        }

        if (mobileCheckoutButton && cartCheckout) {
            mobileCheckoutButton.addEventListener('click', function () {
                cartCheckout.click();
            });
        }

        if (categoryToggle && categoryDropdown) {
            categoryToggle.addEventListener('click', function () {
                const isOpen = categoryDropdown.classList.toggle('is-open');
                categoryToggle.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
            });
        }

        categoryButtons.forEach(function (button) {
            button.addEventListener('click', function () {
                selectProductCategory(button.dataset.category || '');
            });
        });

        if (allProductsLink) {
            allProductsLink.addEventListener('click', function () {
                selectProductCategory('');
            });
        }

        document.addEventListener('click', function (event) {
            if (!categoryDropdown || !categoryDropdown.classList.contains('is-open')) {
                return;
            }

            if (!categoryDropdown.contains(event.target)) {
                closeCategoryMenu();
            }
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeCategoryMenu();
            }
        });

        if (productCarouselPrev) {
            productCarouselPrev.addEventListener('click', function () {
                if (currentProductPage > 0) {
                    currentProductPage -= 1;
                    renderProductCatalog();
                }
            });
        }

        if (productCarouselNext) {
            productCarouselNext.addEventListener('click', function () {
                const filtered = getFilteredProducts();
                const totalPages = Math.max(1, Math.ceil(filtered.length / 10));
                if (currentProductPage < totalPages - 1) {
                    currentProductPage += 1;
                    renderProductCatalog();
                }
            });
        }

        if (productCarouselTrack) {
            productCarouselTrack.addEventListener('click', function (event) {
                const detailButton = event.target.closest('button.product-visual-trigger');
                if (detailButton) {
                    openProductDetailModal(detailButton);
                    return;
                }

                const button = event.target.closest('button.btn-order');
                if (!button) {
                    return;
                }

                const card = button.closest('.product-card');
                if (!card) {
                    return;
                }

                openSizeModal(card);
            });
        }

        if (newsCarouselTrack && newsCarouselDots) {
            if (newsCarouselPrev) {
                newsCarouselPrev.addEventListener('click', function () {
                    newsCarouselAutoDirection = -1;
                    scrollNewsCarouselToPage(getNewsCarouselPageIndex() - 1);
                    resetNewsCarouselAutoplay();
                });
            }

            if (newsCarouselNext) {
                newsCarouselNext.addEventListener('click', function () {
                    newsCarouselAutoDirection = 1;
                    scrollNewsCarouselToPage(getNewsCarouselPageIndex() + 1);
                    resetNewsCarouselAutoplay();
                });
            }

            newsCarouselDots.addEventListener('click', function (event) {
                const button = event.target.closest('button[data-carousel-page]');
                if (!button) {
                    return;
                }

                const pageIndex = parseInt(button.dataset.carouselPage, 10);
                if (Number.isNaN(pageIndex)) {
                    return;
                }

                scrollNewsCarouselToPage(pageIndex);
                resetNewsCarouselAutoplay();
            });

            newsCarouselTrack.addEventListener('scroll', function () {
                window.requestAnimationFrame(updateNewsCarouselDots);
            }, { passive: true });

            newsCarouselTrack.addEventListener('mouseenter', stopNewsCarouselAutoplay);
            newsCarouselTrack.addEventListener('mouseleave', resetNewsCarouselAutoplay);
            newsCarouselTrack.addEventListener('pointerdown', stopNewsCarouselAutoplay);
            newsCarouselTrack.addEventListener('pointerup', resetNewsCarouselAutoplay);
            newsCarouselDots.addEventListener('mouseenter', stopNewsCarouselAutoplay);
            newsCarouselDots.addEventListener('mouseleave', resetNewsCarouselAutoplay);

            window.addEventListener('resize', function () {
                renderNewsCarouselDots();
                updateNewsCarouselDots();
            });
            document.addEventListener('visibilitychange', function () {
                if (document.hidden) {
                    stopNewsCarouselAutoplay();
                } else {
                    resetNewsCarouselAutoplay();
                }
            });
            renderNewsCarouselDots();
            updateNewsCarouselDots();
            startNewsCarouselAutoplay();
        }

        if (sizeClose) {
            sizeClose.addEventListener('click', closeSizeModal);
        }

        if (sizeBackdrop) {
            sizeBackdrop.addEventListener('click', closeSizeModal);
        }

        if (productDetailClose) {
            productDetailClose.addEventListener('click', closeProductDetailModal);
        }

        if (productDetailBackdrop) {
            productDetailBackdrop.addEventListener('click', closeProductDetailModal);
        }

        if (productDetailAddToCart) {
            productDetailAddToCart.addEventListener('click', function () {
                const card = currentDetailCard;
                if (!card) {
                    return;
                }

                closeProductDetailModal();
                openSizeModal(card, 'cart');
            });
        }

        if (productDetailBuyNow) {
            productDetailBuyNow.addEventListener('click', function () {
                const card = currentDetailCard;
                if (!card) {
                    return;
                }

                closeProductDetailModal();
                openSizeModal(card, 'buy-now');
            });
        }

        sizeButtons.forEach(function (button) {
            button.addEventListener('click', function () {
                const sizeValue = button.dataset.sizeValue || 'M';
                const card = pendingProductCard;
                const action = pendingProductAction;
                closeSizeModal();
                if (card) {
                    addProductFromCardWithSize(card, sizeValue, action);
                }
            });
        });

        if (cartItems) {
            cartItems.addEventListener('click', function (event) {
                const button = event.target.closest('button[data-cart-action]');
                if (!button) {
                    return;
                }

                const action = button.dataset.cartAction;
                const id = button.dataset.cartId;

                if (action === 'increase') {
                    changeCartQty(id, 1);
                } else if (action === 'decrease') {
                    changeCartQty(id, -1);
                } else if (action === 'remove') {
                    cart = cart.filter(function (item) {
                        return item.id !== id;
                    });
                    renderCart();
                }
            });
        }

        if (track && slides.length >= 2 && prevBtn && nextBtn) {
            prevBtn.addEventListener('click', function () {
                index = (index - 1 + slides.length) % slides.length;
                renderBanner();
            });

            nextBtn.addEventListener('click', function () {
                index = (index + 1) % slides.length;
                renderBanner();
            });

            bannerDots.forEach(function (dot) {
                dot.addEventListener('click', function () {
                    const slideIndex = Number(dot.dataset.slide);
                    if (!Number.isNaN(slideIndex)) {
                        index = slideIndex % slides.length;
                        renderBanner();
                    }
                });
            });

            if (bannerContent) {
                bannerContent.addEventListener('click', function (event) {
                    if (event.target.closest('button')) {
                        return;
                    }

                    index = (index + 1) % slides.length;
                    renderBanner();
                });
            }

            renderBanner();
        }

        if (searchInput) {
            searchInput.addEventListener('input', renderSearch);
        }

        window.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeCart();
                closeSizeModal();
                closePaymentModal();
                closeProductDetailModal();
                closeUserPanel();
            }
        });

        document.addEventListener('click', function (event) {
            if (userPanel && userPanel.classList.contains('is-open')) {
                const clickedInsideUserMenu = event.target.closest('.shop-user-menu');
                if (!clickedInsideUserMenu) {
                    closeUserPanel();
                }
            }
        });

        syncCheckoutLabel();
        updateCategoryTitle();
        loadDeliveryPreference();
        renderProductCatalog();
        restoreCouponSelection();
        renderCart();
    })();
</script>

</body>
</html>
