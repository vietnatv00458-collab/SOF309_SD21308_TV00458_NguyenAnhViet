<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${nhanVien != null ? "Sửa nhân viên" : "Thêm nhân viên"}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=140">
</head>
<body>

<div class="layout">
    <jsp:include page="/views/common/sidebar.jsp"/>

    <div class="main-content">
        <div class="topbar">
            <h1>${nhanVien != null ? "SỬA NHÂN VIÊN" : "THÊM NHÂN VIÊN"}</h1>
        </div>

        <div class="form-box">
            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <c:set var="currentMaNV" value="${formMaNV ne null ? formMaNV : (nhanVien != null ? nhanVien.maNV : '')}" />
            <c:set var="currentHoTen" value="${formHoTen ne null ? formHoTen : (nhanVien != null ? nhanVien.hoTen : '')}" />
            <c:set var="currentNgaySinh" value="${formNgaySinh ne null ? formNgaySinh : (nhanVien != null ? nhanVien.ngaySinh : '')}" />
            <c:set var="currentNgayVaoLam" value="${formNgayVaoLam ne null ? formNgayVaoLam : (nhanVien != null ? nhanVien.ngayVaoLam : '')}" />
            <c:set var="currentGioiTinh" value="${formGioiTinh ne null ? formGioiTinh : (nhanVien != null ? nhanVien.gioiTinh : '')}" />
            <c:set var="currentSdt" value="${formSdt ne null ? formSdt : (nhanVien != null ? nhanVien.sdt : '')}" />
            <c:set var="currentEmail" value="${formEmail ne null ? formEmail : (nhanVien != null ? nhanVien.email : '')}" />
            <c:set var="currentDiaChi" value="${formDiaChi ne null ? formDiaChi : (nhanVien != null ? nhanVien.diaChi : '')}" />
            <c:set var="currentChucVu" value="${formChucVu ne null ? formChucVu : (nhanVien != null ? nhanVien.chucVu : '')}" />
            <c:set var="currentLuong" value="${formLuong ne null ? formLuong : (nhanVien != null && nhanVien.luong != null ? nhanVien.luong : '')}" />

            <form method="post" action="${pageContext.request.contextPath}/nhanvien">
                <input type="hidden" name="action"
                       value="${nhanVien != null ? 'update' : 'create'}"/>

                <div class="form-grid">
                    <div class="form-group">
                        <label for="maNV">Mã nhân viên (tự động)</label>
                        <input type="text"
                               id="maNV"
                               name="maNV"
                               value="${currentMaNV}"
                               readonly>
                        <small class="form-note">Hệ thống tự sinh mã khi lưu nhân viên mới.</small>
                    </div>

                    <div class="form-group">
                        <label for="hoTen">Họ tên</label>
                        <input type="text"
                               id="hoTen"
                               name="hoTen"
                               value="${currentHoTen}"
                               required>
                    </div>

                    <div class="form-group">
                        <label for="ngaySinh">Ngày sinh</label>
                        <input type="date"
                               id="ngaySinh"
                               name="ngaySinh"
                               value="${currentNgaySinh}"
                               max="${ngaySinhMax}"
                               required>
                    </div>

                    <div class="form-group">
                        <label for="ngayVaoLam">Ngày vào làm</label>
                        <input type="date"
                               id="ngayVaoLam"
                               name="ngayVaoLam"
                               value="${currentNgayVaoLam}">
                    </div>

                    <div class="form-group">
                        <label for="gioiTinh">Giới tính</label>
                        <select id="gioiTinh" name="gioiTinh">
                            <option value="">-- Chọn giới tính --</option>
                            <option value="Nam"
                            ${currentGioiTinh == 'Nam' ? 'selected' : ''}>
                                Nam
                            </option>
                            <option value="Nữ"
                            ${currentGioiTinh == 'Nữ' ? 'selected' : ''}>
                                Nữ
                            </option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="sdt">SĐT</label>
                        <input type="text"
                               id="sdt"
                               name="sdt"
                               value="${currentSdt}">
                    </div>

                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email"
                               id="email"
                               name="email"
                               value="${currentEmail}">
                    </div>

                    <div class="form-group full">
                        <label for="diaChi">Địa chỉ</label>
                        <input type="text"
                               id="diaChi"
                               name="diaChi"
                               value="${currentDiaChi}">
                    </div>

                    <div class="form-group">
                        <label for="chucVu">Chức vụ</label>
                        <input type="text"
                               id="chucVu"
                               name="chucVu"
                               value="${currentChucVu}">
                    </div>

                    <div class="form-group">
                        <label for="luong">Lương</label>
                        <input type="number"
                               step="0.01"
                               id="luong"
                               name="luong"
                               value="${currentLuong}">
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-save">
                        ${nhanVien != null ? "Cập nhật" : "Thêm mới"}
                    </button>
                    <a href="${pageContext.request.contextPath}/nhanvien" class="btn btn-back">
                        Quay lại
                    </a>
                </div>
            </form>
        </div>
    </div>
</div>

</body>
</html>
