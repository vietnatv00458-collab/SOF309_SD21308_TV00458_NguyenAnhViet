# ĐẶC TẢ HỆ THỐNG — QUẢN LÝ QUÁN COFFEE

| Thông tin | Nội dung |
|---|---|
| Tên hệ thống | Quản lý quán Coffee (`QuanLyCafe`) |
| Artifact Maven | `com.cafe:CafeManagement:1.0-SNAPSHOT` (đóng gói WAR, `finalName = QuanLyCafe`) |
| Phiên bản tài liệu | 1.0 |
| Ngày lập | 16/11/2026 |
| Nguồn | Phân tích mã nguồn trong `QuanLyCafe (2).zip` |

> **Lưu ý:** Tài liệu được suy ra trực tiếp từ mã nguồn (servlet, DAO, entity, filter, JSP). Những chỗ hành vi thực tế khác với kỳ vọng nghiệp vụ được liệt kê riêng ở **Mục 11 – Hạn chế và rủi ro đã quan sát**.

---

## 1. Giới thiệu

### 1.1 Mục đích
Hệ thống hỗ trợ vận hành một quán cà phê gồm hai mặt:

- **Quản trị nội bộ:** quản lý sản phẩm, giá, khuyến mãi, nhân viên, tài khoản, khách hàng, đơn hàng, hóa đơn, doanh thu.
- **Cửa hàng trực tuyến cho khách:** xem thực đơn, chọn size, giỏ hàng, mã giảm giá, chọn nhận tại quán hoặc giao hàng, thanh toán chuyển khoản QR (SePay/VietQR).

### 1.2 Phạm vi
Ứng dụng web Java chạy trên servlet container hỗ trợ Jakarta EE 10 (Servlet 6.0). Phạm vi gồm các module ở Mục 4. Không bao gồm: ứng dụng di động riêng, quản lý kho nguyên liệu, chấm công/tính lương, API công khai cho bên thứ ba (trừ webhook SePay).

### 1.3 Thuật ngữ

| Thuật ngữ | Ý nghĩa |
|---|---|
| ĐH / DatHang | Đơn đặt hàng của khách |
| HĐ / HoaDon | Hóa đơn, sinh cùng đơn đặt hàng |
| CTHĐ | Chi tiết hóa đơn |
| KM / KhuyenMai | Khuyến mãi / voucher |
| LSG | Lịch sử giá sản phẩm |
| IPN / Webhook | Thông báo giao dịch ngân hàng do SePay gọi về |
| Phiên thanh toán | Bản ghi tạm trong bộ nhớ, mã dạng `PAY-XXXXXXXXXXXX` |

---

## 2. Kiến trúc và công nghệ

| Hạng mục | Công nghệ |
|---|---|
| Ngôn ngữ / JDK | Java 17 |
| Web | Jakarta Servlet 6.0.0, JSP 3.1.1, JSTL 3.0.1 (Glassfish) |
| ORM | Jakarta Persistence 3.1.0 + Hibernate Core 6.4.4.Final, persistence-unit `cafePU` (RESOURCE_LOCAL) |
| CSDL | Microsoft SQL Server (driver `mssql-jdbc 12.6.1.jre11`), database `QuanLyCafe` |
| Email | Jakarta Mail 2.0.1 (SMTP) |
| Build | Maven, packaging `war` |
| Thanh toán | SePay webhook + ảnh QR từ `img.vietqr.io` |

### 2.1 Phân lớp mã nguồn

| Gói | Vai trò |
|---|---|
| `servlet` | Controller, 26 servlet (Mục 7) |
| `dao` | Truy cập dữ liệu JPA/JPQL (14 DAO) |
| `entity` | 13 entity ánh xạ bảng |
| `service` | `NhanVienService` (xóa mềm NV), `PasswordResetService` (OTP quên mật khẩu) |
| `filter` | `AuthFilter` áp dụng cho `/*` — xác thực + phân quyền theo URL |
| `utils` | Băm mật khẩu, vai trò, trạng thái đơn, phiên thanh toán, lưu đơn, email, cấu hình, ảnh banner/upload |

### 2.2 Luồng xử lý chung
`Trình duyệt → AuthFilter → Servlet → DAO/Service → SQL Server → forward JSP (/views/...)`

---

## 3. Vai trò và phân quyền

### 3.1 Vai trò
Giá trị `TaiKhoan.VaiTro` được chuẩn hóa (bỏ dấu, bỏ khoảng trắng, in hoa) bởi `RoleUtils`.

| Nhóm | Giá trị chấp nhận | Trang chủ sau đăng nhập |
|---|---|---|
| Quản lý | `ADMIN`, `QUANLY`, `MANAGER` | `/dashboard` |
| Nhân viên | `NHANVIEN`, `EMPLOYEE`, `STAFF` | `/nhanvien-home` |
| Giao hàng | `GIAOHANG`, `DELIVERY`, `SHIPPER` | `/giaohang-home` |
| Khách hàng | `KHACHHANG`, `CUSTOMER` | `/customer-shop` |

### 3.2 Ma trận truy cập (theo `AuthFilter`)

Ký hiệu: ✔ được phép · ✘ bị chặn (chuyển hướng về trang chủ của vai trò, hoặc `/login` nếu chưa đăng nhập) · 👁 chỉ xem.

| Tài nguyên | Khách vãng lai | Khách hàng | Nhân viên | Giao hàng | Quản lý |
|---|:-:|:-:|:-:|:-:|:-:|
| `/`, `/home`, `/login`, `/register`, `/forgot-password`, `/logout` | ✔ | ✔ | ✔ | ✔ | ✔ |
| `/payment-session`, `/sepay-ipn`, `/sepay-webhook` | ✔ | ✔ | ✔ | ✔ | ✔ |
| `/css/*`, `/js/*`, `/images/*`, `/uploads/*`, ảnh `.png/.jpg/.jpeg/.gif/.ico` | ✔ | ✔ | ✔ | ✔ | ✔ |
| `/customer-shop`, `/views/customer/*` | ✔ | ✔ | ✘ | ✘ | ✘ |
| `/dashboard`, `/taikhoan`, `/khachhang`, `/nhanvien`, `/dathang`, `/giohang`, `/chitiethoadon`, `/chitietgiohang` (và `/views/...` tương ứng) | ✘ | ✘ | ✘ | ✘ | ✔ |
| `/sanpham`, `/lichsugiasanpham` (danh sách) | ✘ | ✘ | ✔ | ✘ | ✔ |
| `/views/sanpham/form` (thêm/sửa SP) | ✘ | ✘ | ✘ | ✘ | ✔ |
| `/khuyenmai` (danh sách) | ✘ | ✘ | 👁 | ✘ | ✔ |
| `/views/khuyenmai/form` | ✘ | ✘ | ✘ | ✘ | ✔ |
| `/hoadon`, `/views/hoadon/*` | ✘ | ✘ | ✔ | ✔ | ✔ |
| `/nhanvien-home`, `/nhanvien-donhang` | ✘ | ✘ | ✔ | ✘ | ✔ |
| `/giaohang-home`, `/giaohang-donhang` | ✘ | ✘ | ✘ | ✔ | ✔ |
| Đường dẫn khác (mặc định) | ✘ | ✔ | ✔ | ✔ | ✔ |

Quy tắc bổ sung:
- Đường dẫn có tiền tố `/QuanLyCafe/` được chuẩn hóa (cắt tiền tố) trước khi kiểm tra quyền.
- Quản lý được phép mọi thao tác trên sản phẩm, khuyến mãi; nhân viên chỉ xem — thao tác ghi bị servlet từ chối bằng thông báo hoặc chuyển hướng.

---

## 4. Yêu cầu chức năng

Mã yêu cầu dạng `FR-<module>-<số>`. Mức ưu tiên: **Cao / Trung bình / Thấp**.

### 4.1 Xác thực và tài khoản (AUTH)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-AUTH-01 | Đăng nhập bằng **số điện thoại** (8–15 chữ số) hoặc **tên đăng nhập** cùng mật khẩu. Tham số nhận: `loginId`, `sdt` hoặc `username`. | Cao |
| FR-AUTH-02 | Nếu `loginId` là số điện thoại: tra nhân viên theo SĐT trước, rồi khách hàng theo SĐT, rồi tên đăng nhập. Nếu không phải SĐT: tra tên đăng nhập, rồi khách hàng theo SĐT. | Cao |
| FR-AUTH-03 | Sai thông tin hiển thị: `Sai số điện thoại hoặc mật khẩu!`. Lỗi CSDL hiển thị: `Không thể đăng nhập lúc này. Kiểm tra kết nối cơ sở dữ liệu.` | Cao |
| FR-AUTH-04 | Tài khoản gắn nhân viên đã xóa mềm (`IsDeleted = 1`) **không** đăng nhập được. | Cao |
| FR-AUTH-05 | Mật khẩu lưu dạng `PBKDF2$210000$<salt>$<hash>` (PBKDF2WithHmacSHA256, salt 16 byte, khóa 256 bit). Mật khẩu cũ dạng văn bản thường vẫn đăng nhập được và tự nâng cấp sang băm sau lần đăng nhập thành công. | Cao |
| FR-AUTH-06 | Đăng nhập thành công: lưu `user`, `role` vào session; nếu `loginId` toàn số 8–15 chữ số thì lưu thêm `loginPhone`. Chuyển hướng tới trang chủ của vai trò. | Cao |
| FR-AUTH-07 | Khách hàng đăng nhập kèm `returnUrl` hợp lệ (bắt đầu bằng `/`, không bắt đầu bằng `//`) thì quay lại `returnUrl`; ngược lại về `/customer-shop`. | Trung bình |
| FR-AUTH-08 | Đăng xuất qua `/logout`, hủy phiên. | Cao |
| FR-AUTH-09 | Dự phòng: nếu CSDL không tìm thấy hoặc lỗi, thử đăng nhập qua `AuthStore` (kho tệp tương thích cũ). | Thấp |

### 4.2 Đăng ký khách hàng (REG)

| Mã | Yêu cầu |
|---|---|
| FR-REG-01 | Trường: `hoTen`, `sdt`, `email`, `diaChi`, `matKhau`, `xacNhanMatKhau`. Bắt buộc: họ tên, SĐT, mật khẩu, xác nhận mật khẩu. |
| FR-REG-02 | Thứ tự kiểm tra và thông báo: (1) thiếu trường → `Họ tên, số điện thoại và mật khẩu không được để trống!`; (2) không khớp → `Xác nhận mật khẩu không khớp!`; (3) dưới 6 ký tự → `Mật khẩu phải có ít nhất 6 ký tự!`; (4) SĐT đã có tài khoản → `Số điện thoại này đã có tài khoản. Hãy đăng nhập.` |
| FR-REG-03 | Nếu SĐT đã tồn tại trong `KhachHang` (chưa có tài khoản) thì dùng lại `MaKH` đó; ngược lại sinh mã mới dạng `KH%02d` (KH01, KH02, …). |
| FR-REG-04 | Tạo đồng thời `KhachHang` và `TaiKhoan` (`TenDangNhap = MaKH`, `VaiTro = KHACHHANG`, mật khẩu băm) trong cùng một thao tác lưu. |
| FR-REG-05 | Đăng ký thành công: tự đăng nhập và chuyển tới `/customer-shop`. |

### 4.3 Quên mật khẩu bằng OTP (PWD)

| Mã | Yêu cầu |
|---|---|
| FR-PWD-01 | Bước 1: nhập email, định dạng hợp lệ `^[^@\s]+@[^@\s]+\.[^@\s]+$`; sai → `Vui lòng nhập email hợp lệ.` |
| FR-PWD-02 | Tìm khách hàng theo email rồi tài khoản theo `MaKH`; không thấy → `Không tìm thấy tài khoản gắn với email này trong hệ thống.` |
| FR-PWD-03 | Sinh OTP 6 chữ số (`SecureRandom`), gửi email tiêu đề `Mã xác thực đặt lại mật khẩu`; gửi lỗi → `Không gửi được email xác thực. Kiểm tra cấu hình SMTP.` |
| FR-PWD-04 | OTP lưu trong session, hiệu lực **5 phút**, tối đa **5 lần** nhập sai; quá hạn → `Mã xác thực đã hết hạn. Vui lòng gửi lại mã mới.`; quá số lần → `Bạn đã nhập sai quá nhiều lần. Vui lòng gửi lại mã mới.` |
| FR-PWD-05 | Bước 2 (`action=reset`): kiểm tra email khớp yêu cầu gửi mã, OTP đúng (`Mã xác thực không đúng.`), mật khẩu mới ≥ 6 ký tự, xác nhận khớp (`Xác nhận mật khẩu không khớp.`). |
| FR-PWD-06 | Thành công: cập nhật mật khẩu băm, xóa OTP và thông tin đăng nhập khỏi session, chuyển `/login` với thông báo `Đổi mật khẩu thành công. Vui lòng đăng nhập lại.` |

### 4.4 Quản lý khách hàng (KH) — vai trò Quản lý

| Mã | Yêu cầu |
|---|---|
| FR-KH-01 | Danh sách, thêm, sửa, xóa khách hàng (`action = add / edit / delete`, gửi biểu mẫu `create / update`). |
| FR-KH-02 | Thêm: mã KH và họ tên bắt buộc (`Mã khách hàng và họ tên không được để trống!`); mã trùng bị từ chối (`Mã khách hàng đã tồn tại!`). |
| FR-KH-03 | Sửa: họ tên bắt buộc (`Họ tên không được để trống!`). |
| FR-KH-04 | Xóa: không cho xóa khách còn lịch sử đơn hàng hoặc hóa đơn (`Không thể xóa khách hàng vì còn lịch sử đơn hàng hoặc hóa đơn.`). |

### 4.5 Quản lý nhân viên (NV) — vai trò Quản lý

| Mã | Yêu cầu |
|---|---|
| FR-NV-01 | Thêm/sửa/xem nhân viên: mã (tự sinh nếu trống hoặc trùng, tiền tố `NV`), họ tên, ngày sinh, giới tính, SĐT, email, địa chỉ, chức vụ, lương, ngày vào làm. |
| FR-NV-02 | Họ tên bắt buộc (`Họ tên không được để trống!`); ngày sinh bắt buộc (`Ngày sinh không được để trống!`). |
| FR-NV-03 | Nhân viên phải **đủ 18 tuổi trở lên** (`Nhân viên phải đủ 18 tuổi trở lên!`). |
| FR-NV-04 | Lương phải là số (`Lương phải là số!`); ngày sinh/ngày vào làm sai định dạng → `Ngày sinh hoặc ngày vào làm không hợp lệ!` |
| FR-NV-05 | **Xóa mềm:** đặt `IsDeleted = 1`, không xóa bản ghi. Khi xóa, các đơn đặt hàng/hóa đơn **đang xử lý** của nhân viên bị xóa cùng chi tiết và doanh thu liên quan; các đơn **đã hoàn tất** được giữ lại. Có bước xem trước số lượng bị ảnh hưởng (`previewDeletion`). |
| FR-NV-06 | Nhân viên đã xóa mềm không đăng nhập được và không được chọn để nhận/giao đơn. |

### 4.6 Quản lý tài khoản (TK) — vai trò Quản lý

| Mã | Yêu cầu |
|---|---|
| FR-TK-01 | Tạo tài khoản cho nhân viên: bắt buộc chọn nhân viên, tên đăng nhập, mật khẩu (`Vui lòng chọn nhân viên, nhập tên đăng nhập và mật khẩu!`). |
| FR-TK-02 | Mã nhân viên phải tồn tại (`Mã nhân viên chưa tồn tại. Hãy tạo nhân viên trước khi tạo tài khoản.`); mỗi nhân viên một tài khoản (`Nhân viên này đã có tài khoản!`). |
| FR-TK-03 | Tên đăng nhập duy nhất (`Tên đăng nhập đã tồn tại!`). |
| FR-TK-04 | Chế độ tạo tài khoản giao hàng chỉ cho chọn nhân viên giao hàng (`Chế độ này chỉ cho phép chọn nhân viên giao hàng.`). |
| FR-TK-05 | Sửa: tên đăng nhập và mật khẩu không được để trống; tên đăng nhập không được trùng tài khoản khác. |
| FR-TK-06 | Xóa tài khoản; lỗi hiển thị `Không thể xóa tài khoản: …`. |
| FR-TK-07 | **Vai trò của tài khoản được suy ra từ `NhanVien.ChucVu`** khi tạo/sửa: chức vụ thuộc nhóm quản lý (`Admin`, `Quản lý`, `Manager`) → `Admin`; thuộc nhóm giao hàng (`Giao hàng`, `Delivery`, `Shipper`) → `GiaoHang`; còn lại hoặc để trống → `NhanVien`. |

### 4.7 Quản lý sản phẩm (SP)

| Mã | Yêu cầu |
|---|---|
| FR-SP-01 | Quản lý và Nhân viên xem danh sách; **chỉ Quản lý** thêm/sửa/xóa. Nhân viên thực hiện thao tác ghi nhận `Bạn chỉ có quyền xem danh sách sản phẩm.` |
| FR-SP-02 | Thuộc tính: mã SP (tự sinh tiền tố `SP` nếu trống/trùng), tên (bắt buộc), loại, size, đơn giá, số lượng tồn, mô tả (≤ 200), thành phần (≤ 500), hình ảnh (≤ 255). |
| FR-SP-03 | Tên trống → `Tên sản phẩm không được để trống!`; đơn giá < 0 → `Đơn giá không được nhỏ hơn 0!`; tồn < 0 → `Số lượng tồn không được nhỏ hơn 0!`; tồn không nguyên → `Số lượng tồn phải là số nguyên!`. Đơn giá và tồn để trống được coi là 0. |
| FR-SP-04 | Tải ảnh qua `hinhAnhFile`; chỉ nhận `.png .jpg .jpeg .webp .gif` (`Ảnh sản phẩm chỉ hỗ trợ PNG, JPG, JPEG, WEBP hoặc GIF.`). Ảnh phục vụ qua `/uploads/*`. |
| FR-SP-05 | Không cho xóa sản phẩm đã có lịch sử bán, chi tiết đơn hàng hoặc lịch sử giá (`Không thể xóa sản phẩm đã có lịch sử bán, chi tiết đơn hàng hoặc lịch sử giá.`). |
| FR-SP-06 | Cửa hàng khách hiển thị sản phẩm theo nhóm: Bánh, Cafe, Trà sữa, Sinh tố; phân trang 10 sản phẩm/trang; có tìm kiếm. |

### 4.8 Lịch sử giá sản phẩm (LSG)

| Mã | Yêu cầu |
|---|---|
| FR-LSG-01 | Xem, thêm, sửa, xóa bản ghi lịch sử giá; tìm theo từ khóa. Quyền truy cập: Quản lý và Nhân viên (theo bộ lọc). |
| FR-LSG-02 | Mã SP bắt buộc (`Mã sản phẩm không được để trống.`) và phải tồn tại (`Mã sản phẩm không tồn tại trong danh sách sản phẩm.`). |
| FR-LSG-03 | Giá cũ và giá mới đều ≥ 0 (`Giá cũ không được nhỏ hơn 0.`, `Giá mới không được nhỏ hơn 0.`); không phải số → `Giá cũ và giá mới phải là số hợp lệ.` |
| FR-LSG-04 | Ngày áp dụng dạng `yyyy-MM-ddTHH:mm`; để trống lấy thời điểm hiện tại; sai định dạng → `Ngày áp dụng không đúng định dạng.` |

### 4.9 Quản lý khuyến mãi (KM)

| Mã | Yêu cầu |
|---|---|
| FR-KM-01 | Quản lý: thêm/sửa/xóa. Nhân viên: chỉ xem danh sách; thao tác ghi bị chuyển về `/khuyenmai`. |
| FR-KM-02 | Mã voucher do hệ thống sinh, **6 ký tự**, bắt đầu `VC` + 4 ký tự ngẫu nhiên từ `A–Z0–9`, đảm bảo không trùng (thử tối đa 100 lần). |
| FR-KM-03 | Tên khuyến mãi bắt buộc (`Tên khuyến mãi không được để trống!`). |
| FR-KM-04 | Kiểu giảm: `PERCENT` (chấp nhận `PHANTRAM`) hoặc `AMOUNT` (chấp nhận `SOTIEN`, `MONEY`); thiếu kiểu → `Vui lòng chọn kiểu giảm giá.` |
| FR-KM-05 | `AMOUNT`: số tiền giảm > 0 (`Số tiền giảm phải lớn hơn 0.`); xóa phần trăm và mức giảm tối đa. |
| FR-KM-06 | `PERCENT`: 0 < phần trăm < 100 (`Phần trăm giảm phải lớn hơn 0 và nhỏ hơn 100.`); mức giảm tối đa (tùy chọn). |
| FR-KM-07 | Ngày kết thúc ≥ ngày bắt đầu (`Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!`). Ngày bắt đầu/kết thúc có thể để trống (không giới hạn phía đó). |
| FR-KM-08 | Voucher **còn hiệu lực** khi `ngày hiện tại ≥ NgàyBắtĐầu` và `ngày hiện tại ≤ NgàyKếtThúc` (bao gồm cả hai đầu mút). |
| FR-KM-09 | Giá trị số không parse được → `Giá trị giảm không hợp lệ!`. |

### 4.10 Cửa hàng khách hàng và giỏ hàng (SHOP)

| Mã | Yêu cầu |
|---|---|
| FR-SHOP-01 | `/customer-shop` hiển thị sản phẩm, banner khuyến mãi, danh sách voucher còn hiệu lực, thông tin hồ sơ khách. Khách vãng lai xem được; nhân viên/quản lý/giao hàng bị chuyển về trang chủ vai trò. |
| FR-SHOP-02 | Thêm vào giỏ qua hộp chọn **size S / M / L**. Cùng sản phẩm + cùng size tăng số lượng; khác size là dòng riêng (khóa `MaSP-Size`). |
| FR-SHOP-03 | Giỏ hàng lưu ở `localStorage` của trình duyệt theo khóa chủ giỏ; giỏ khách vãng lai được gộp vào giỏ tài khoản sau khi đăng nhập. |
| FR-SHOP-04 | Chọn/nhập mã giảm giá; mã sai hoặc hết hạn → `Mã giảm giá không hợp lệ hoặc đã hết hạn.`; mã chọn được ghi nhớ giữa các lần tải trang. |
| FR-SHOP-05 | Hình thức nhận: **Tại quán** (`TAI_QUAN`, mặc định) hoặc **Giao hàng** (`GIAO_HANG`). Giao hàng bắt buộc nhập địa chỉ (`Vui lòng nhập địa chỉ giao hàng.`). |
| FR-SHOP-06 | Khách tự xem/cập nhật/xóa hồ sơ trên cửa hàng (`customerProfileAction = create/update`). |

### 4.11 Thanh toán (PAY)

| Mã | Yêu cầu |
|---|---|
| FR-PAY-01 | `POST /payment-session?action=create` tạo phiên thanh toán với: `cartJson`, `couponCode`, `deliveryMethod`, `deliveryAddress`, `note`, `ownerKey`, `maNV`. Mã khách lấy từ tài khoản đăng nhập. |
| FR-PAY-02 | Lỗi tạo phiên: chưa xác định khách → 401 `Vui lòng đăng nhập tài khoản khách hàng trước khi thanh toán.`; giỏ trống hoặc tổng ≤ 0 → 400 `Giỏ hàng trống, không thể tạo đơn.`; giao hàng thiếu địa chỉ → 400 `Vui lòng nhập địa chỉ giao hàng.`; mã giảm giá sai → 400 `Mã giảm giá không hợp lệ hoặc đã hết hạn.`; tổng sau giảm ≤ 0 → 400 `Mã giảm giá không hợp lệ cho đơn hàng này.` |
| FR-PAY-03 | Giảm giá: `AMOUNT` trừ thẳng số tiền; `PERCENT` giảm theo phần trăm, **làm tròn về đồng**, bị giới hạn bởi `MucGiamToiDa` nếu có (> 0). Số tiền giảm không vượt tổng tiền hàng. |
| FR-PAY-04 | Phiên có thời gian chờ **10 phút**, giữ lại trong bộ nhớ **15 phút** rồi bị dọn. Quá hạn → trạng thái thất bại `Thanh toán thất bại`. |
| FR-PAY-05 | Giao diện hiển thị mã QR VietQR (`img.vietqr.io`), nội dung chuyển khoản chứa mã phiên, đồng hồ đếm ngược, nút sao chép nội dung. |
| FR-PAY-06 | `GET /payment-session?id=…` trả trạng thái phiên (JSON): `status`, `statusText`, số tiền, giảm giá, `expiresAt`, phương thức giao hàng… Thiếu mã → 400 `Thiếu mã phiên thanh toán.`; không có → 404 `Không tìm thấy phiên thanh toán.` |
| FR-PAY-07 | Khi phiên hoàn tất, hệ thống lưu đơn (xem FR-ORD-01). Nếu lưu thất bại, phiên chuyển thất bại kèm lý do. |
| FR-PAY-08 | Webhook `/sepay-ipn` (cũng `/sepay-webhook`): xem Mục 6.3. |

### 4.12 Lưu đơn sau thanh toán (ORD)

| Mã | Yêu cầu |
|---|---|
| FR-ORD-01 | Trong **một giao dịch** hệ thống tạo: `GioHang` (mã `GH..`), `DatHang` (`DH..`), `HoaDon`, các `ChiTietGioHang`, `ChiTietHoaDon`, và một `DoanhThu` (`DT..`) với `DoanhThu = ThanhTien`, `ChiPhi = 0`. Lỗi bất kỳ → rollback toàn bộ. |
| FR-ORD-02 | Trạng thái ban đầu của `DatHang` và `HoaDon`: `CHO_NHAN`; `MaNV`, `MaNVGiaoHang` rỗng; `DaNhanGiaoHang = false`. |
| FR-ORD-03 | Số điện thoại lấy từ hồ sơ khách. Địa chỉ giao hàng chỉ lưu khi đơn là giao hàng (ưu tiên địa chỉ nhập, dự phòng địa chỉ hồ sơ); đơn tại quán để trống địa chỉ. |
| FR-ORD-04 | Kiểm tra khi lưu: khách tồn tại (`Khách hàng không tồn tại.`), sản phẩm tồn tại, size tồn tại, số lượng > 0 (`Số lượng sản phẩm không hợp lệ.`), đơn giá > 0 (`Đơn giá sản phẩm không hợp lệ.`), tổng thanh toán > 0 (`Tổng thanh toán không hợp lệ.`). |
| FR-ORD-05 | Mỗi dòng: `ThanhTien = DonGia × SoLuong`. `HoaDon.TongTien` = tổng tiền hàng, `GiamGia` = số tiền giảm, `ThanhTien = TongTien − GiamGia`. |

### 4.13 Xử lý đơn hàng (WF) — Nhân viên, Giao hàng, Quản lý

Endpoint `/nhanvien-donhang` và `/giaohang-donhang`, tham số `action` và `id` (mã đặt hàng), `deliveryMaNV`. `GET ?action=summary` trả tổng quan.

| Mã | Yêu cầu |
|---|---|
| FR-WF-01 | **claim** (`assign`/`claim`): nhân viên nhận đơn → `MaNV` = nhân viên hiện tại; nếu đơn chưa ở trạng thái giao thì chuyển sang trạng thái đang làm. Đơn đã có nhân viên khác nhận → 409 `Đơn này đã được nhân viên khác nhận.` (Quản lý được ghi đè). |
| FR-WF-02 | **assign-delivery:** chỉ cho đơn có địa chỉ giao hàng (`Chỉ các đơn giao hàng mới được phân công cho tài xế.`); đơn phải đang ở trạng thái chuẩn bị (`Chỉ được chuyển đơn sau khi đã làm xong.`); phải chọn người giao (`Vui lòng chọn người giao hàng.`). Kết quả: `MaNVGiaoHang` = tài xế, trạng thái `DANG_GIAO`, `DaNhanGiaoHang = false`. |
| FR-WF-03 | **complete** bởi nhân viên: chỉ với đơn **tại quán** (đơn giao hàng → 409 `Đơn giao hàng phải do tài xế xác nhận hoàn tất.`). Kết quả `HOAN_THANH`, đồng bộ hóa đơn, ghi doanh thu. |
| FR-WF-04 | Tài xế **claim:** chỉ đơn được giao cho mình (`Đơn này không được giao cho bạn.`) và đang ở trạng thái giao (`Đơn này chưa sẵn sàng để nhận.`) → `DaNhanGiaoHang = true`. |
| FR-WF-05 | Tài xế **complete:** phải nhận đơn trước (`Bạn cần nhận đơn trước khi xác nhận hoàn tất.`) → `HOAN_THANH`. Tài xế chỉ được claim hoặc complete (`Tài xế chỉ được nhận đơn hoặc xác nhận đã giao.`). |
| FR-WF-06 | Đơn đã `HOAN_THANH` hoặc `DA_HUY` không xử lý tiếp (409 `Đơn hàng này đã được xử lý.`); không tìm thấy → 404 `Không tìm thấy đơn hàng.`; tài khoản thiếu mã nhân viên → 400 `Tài khoản chưa có mã nhân viên.`; `action` lạ → 400 `Hành động không hợp lệ.` |
| FR-WF-07 | Mọi thay đổi trạng thái đơn được đồng bộ sang `HoaDon` tương ứng (nhân viên, trạng thái, nhận giao hàng). |

**Sơ đồ trạng thái đơn hàng (luồng chuẩn của hệ thống):**

```
                   claim                    assign-delivery (đơn giao hàng)
  CHO_NHAN ───────────────► (đang làm) ───────────────────────────────► DANG_GIAO
      │                          │                                          │
      │                          │ complete (đơn tại quán)                  │ tài xế claim
      │                          ▼                                          │ rồi complete
      └────────────────────► HOAN_THANH ◄───────────────────────────────────┘
```

### 4.14 Quản lý hóa đơn (HD)

| Mã | Yêu cầu |
|---|---|
| FR-HD-01 | Danh sách/xem hóa đơn cho Quản lý, Nhân viên, Giao hàng. Thêm/sửa/xóa hóa đơn thủ công do Quản lý thực hiện (theo các lỗi kiểm tra bên dưới). |
| FR-HD-02 | Kiểm tra khi thêm/sửa: mã KH tồn tại (`Mã khách hàng không tồn tại hoặc không được để trống!`); mã đặt hàng tồn tại (`Mã đặt hàng không tồn tại!`); mã khuyến mãi tồn tại (`Mã khuyến mãi không tồn tại!`); mã nhân viên bắt buộc (`Mã nhân viên không được để trống!`). |
| FR-HD-03 | Hóa đơn đã hoàn tất không xử lý tiếp (`Hóa đơn này đã hoàn tất.`). Thao tác nhận/hoàn thành hóa đơn theo cùng quy tắc vai trò như FR-WF. |
| FR-HD-04 | Mã hóa đơn tự sinh dạng số 4 chữ số tăng dần (`%04d`, ví dụ `0001`). |
| FR-HD-05 | Quản lý còn có màn chi tiết hóa đơn (`/chitiethoadon`), giỏ hàng (`/giohang`), chi tiết giỏ (`/chitietgiohang`), đặt hàng (`/dathang`) để tra cứu/hiệu chỉnh dữ liệu. |

### 4.15 Dashboard và doanh thu (DASH)

| Mã | Yêu cầu |
|---|---|
| FR-DASH-01 | `/dashboard` (chỉ Quản lý) hiển thị: tổng khách hàng, nhân viên, tài khoản, sản phẩm, bản ghi lịch sử giá, hóa đơn, đặt hàng, giỏ hàng, **tổng doanh thu**, **doanh thu theo tháng**, 6 sản phẩm đầu, 5 bản ghi lịch sử giá mới nhất. |
| FR-DASH-02 | Tổng doanh thu = tổng cột `DoanhThu` của bảng `DoanhThu`. Doanh thu theo tháng gom theo tháng của cột `Ngay`. |
| FR-DASH-03 | Quản lý tải/thay banner khuyến mãi (`bannerUpload`, `bannerCount`, `bannerMode`, `replaceBannerTarget`). Không có ảnh hợp lệ → `Chưa chọn ảnh hợp lệ.`; lỗi lưu → `Không thể lưu ảnh banner.` Ảnh banner phục vụ qua `/promo-banners/*`. |
| FR-DASH-04 | Bản ghi `DoanhThu` được tạo **ngay khi đơn được thanh toán và lưu** (FR-ORD-01); khi đơn hoàn tất, hệ thống ghi/cập nhật lại (upsert theo `MaHD`) nên mỗi hóa đơn chỉ có một bản ghi doanh thu. Hệ quả: tổng doanh thu trên dashboard gồm cả đơn đã thanh toán nhưng chưa hoàn tất. |

### 4.16 Trang chủ nhân viên / giao hàng

| Mã | Yêu cầu |
|---|---|
| FR-HOME-01 | `/nhanvien-home` là trang làm việc của nhân viên, hiển thị danh sách đơn cần xử lý và thao tác nhận/hoàn thành đơn (qua `/nhanvien-donhang`). |
| FR-HOME-02 | `/giaohang-home` là trang làm việc của tài xế, hiển thị đơn giao hàng và thao tác nhận/xác nhận đã giao (qua `/giaohang-donhang`). |

---

## 5. Mô hình dữ liệu

### 5.1 Danh sách bảng

| Bảng | Khóa chính | Cột chính |
|---|---|---|
| `KhachHang` | `MaKH` (≤10) | `HoTen` (NN, ≤100), `SDT` (≤15), `Email` (≤100), `DiaChi` (≤200) |
| `NhanVien` | `MaNV` (≤10) | `HoTen` (NN), `NgaySinh`, `GioiTinh` (≤10), `SDT`, `Email`, `DiaChi`, `ChucVu` (≤50), `Luong` (18,2), `NgayVaoLam`, `IsDeleted` (bit, NN, mặc định 0) |
| `TaiKhoan` | `MaTK` (identity) | `MaNV`, `MaKH`, `TenDangNhap` (NN, **unique**, ≤50), `MatKhau` (NN, ≤255), `VaiTro` (NN, ≤20) |
| `SanPham` | `MaSP` (≤10) | `TenSP` (NN, ≤100), `LoaiSP` (≤50), `SizeSP` (≤20), `DonGia`, `SoLuongTon`, `MoTa` (≤200), `ThanhPhan` (≤500), `HinhAnh` (≤255) |
| `Size` | `MaSize` (≤10) | `TenSize` (≤20), `PhuThu` (18,2) |
| `LichSuGiaSanPham` | `MaLSGia` (identity) | `MaSP` (NN), `GiaCu` (NN), `GiaMoi` (NN), `NgayApDung`, `LyDo` (≤255) |
| `KhuyenMai` | `MaKM` (≤10) | `TenKM` (NN, ≤100), `LoaiGiamGia` (≤20), `PhanTramGiam`, `SoTienGiam`, `MucGiamToiDa`, `NgayBatDau`, `NgayKetThuc`, `MoTa` (≤200) |
| `GioHang` | `MaGioHang` (≤10) | `MaKH`, `NgayTao` |
| `ChiTietGioHang` | `MaCTGH` (identity) | `MaGioHang` (NN), `MaDatHang`, `MaSP` (NN), `MaSize`, `SoLuong` (NN), `DonGia` (NN) |
| `DatHang` | `MaDatHang` (≤10) | `MaKH` (NN), `MaNV`, `MaNVGiaoHang`, `DaNhanGiaoHang`, `NgayDat`, `TrangThai` (≤50), `SoDienThoai` (≤15), `DiaChiGiaoHang` (≤255), `TongTien` |
| `HoaDon` | `MaHD` (≤10) | `MaKH`, `MaNV`, `MaNVGiaoHang`, `MaDatHang`, `MaKM`, `SoDienThoai`, `DiaChiGiaoHang`, `DaNhanGiaoHang`, `NgayLap`, `TongTien`, `GiamGia`, `ThanhTien`, `TrangThai` (≤30) |
| `ChiTietHoaDon` | `MaCTHD` (identity) | `MaHD` (NN), `MaSP` (NN), `MaSize`, `SoLuong` (NN), `DonGia` (NN), `ThanhTien` (NN) |
| `DoanhThu` | `MaDT` (≤10) | `MaHD`, `Ngay`, `DoanhThu`, `ChiPhi`, `LoiNhuan` (cột tính, chỉ đọc) |

(NN = NOT NULL)

### 5.2 Quan hệ logic
- `TaiKhoan` → `NhanVien` (qua `MaNV`) hoặc `KhachHang` (qua `MaKH`); tài khoản khách có `TenDangNhap = MaKH`.
- `DatHang` 1–1 `HoaDon` (qua `HoaDon.MaDatHang`).
- `HoaDon` 1–n `ChiTietHoaDon`; `DatHang`/`GioHang` 1–n `ChiTietGioHang`.
- `HoaDon` 1–1 `DoanhThu` (qua `MaHD`); `HoaDon` n–1 `KhuyenMai`.
- `SanPham` 1–n `LichSuGiaSanPham`.

Các quan hệ được áp dụng ở mức ứng dụng (entity dùng cột khóa dạng chuỗi, không khai báo `@ManyToOne`).

### 5.3 Tập lệnh SQL kèm theo (`/sql`)

| Tệp | Tác dụng |
|---|---|
| `add_sanpham_hinhanh.sql` | Thêm cột `HinhAnh` (NVARCHAR 255) và `ThanhPhan` (NVARCHAR 500) vào `SanPham` nếu chưa có |
| `soft_delete_nhanvien.sql` | Thêm cột `IsDeleted BIT NOT NULL DEFAULT 0` vào `NhanVien` |
| `reset_invoice_workflow.sql` | **Xóa toàn bộ** `DoanhThu`, `ChiTietHoaDon`, `HoaDon` và đặt lại identity `ChiTietHoaDon` — chỉ dùng cho môi trường thử nghiệm |

---

## 6. Quy tắc nghiệp vụ

### 6.1 Quy tắc sinh mã

| Đối tượng | Quy tắc | Ví dụ |
|---|---|---|
| Khách hàng | `KH` + số, tối thiểu 2 chữ số | `KH01` |
| Nhân viên | `NV` + số, đệm số 0 theo độ rộng hiện có | `NV01` |
| Sản phẩm | `SP` + số, đệm số 0 theo độ rộng hiện có | `SP01` |
| Khuyến mãi | `VC` + 4 ký tự `A–Z0–9` ngẫu nhiên | `VC7K2Q` |
| Hóa đơn | số 4 chữ số tăng dần | `0001` |
| Giỏ hàng / Đặt hàng / Doanh thu | `GH`, `DH`, `DT` + số tăng dần | `DH01`, `DT01` |
| Phiên thanh toán | `PAY-` + 12 ký tự hex in hoa | `PAY-3F9A1C07BD52` |

### 6.2 Trạng thái đơn hàng

`OrderStatusUtils` chuẩn hóa chuỗi (bỏ dấu, khoảng trắng, `_`, `-`, in hoa) rồi phân vào các nhóm:

| Nhóm | Một số giá trị chấp nhận |
|---|---|
| Đang chuẩn bị | `CHO_NHAN`, `PENDING`, `DANG_LAM`, `DANG_XU_LY`, `PROCESSING`, `DANG_LAM_DON`, `DANG_CHUAN_BI`, `PREPARING`, `DANG_PHA_CHE`, `DANG_PHUC_VU`, `WAITING` |
| Đang giao | `DANG_GIAO`, `DANG_GIAO_HANG`, `DELIVERING`, `SHIPPING` |
| Hoàn thành | `HOAN_THANH`, `DA_THANH_TOAN`, `COMPLETED`, `COMPLETE`, `DONE` |
| Đã hủy | `DA_HUY`, `HUY` |

Quy tắc chuyển (`canTransition`): từ **hoàn thành/hủy** không chuyển tiếp; từ bất kỳ trạng thái đang hoạt động được phép sang **hủy** hoặc **hoàn thành**; từ **chuẩn bị** sang chuẩn bị hoặc giao; từ **giao** sang giao hoặc hoàn thành; trạng thái đích rỗng bị từ chối.

### 6.3 Webhook SePay (`/sepay-ipn`, `/sepay-webhook`)

1. Đọc thân yêu cầu; xác thực:
   - Có header `X-SePay-Signature`/`X-SePay-Timestamp` → kiểm tra chữ ký HMAC-SHA256 (`sha256=` + HMAC của `timestamp.rawBody`).
   - Ngược lại có `Authorization: Apikey <key>` → so sánh hằng thời gian với khóa cấu hình.
   - Sai → 401 `{"success":false,"message":"Unauthorized"}`; thân rỗng/không phân tích được → 400 `Invalid payload`.
2. Hỗ trợ `application/json`, `application/x-www-form-urlencoded`, `multipart/form-data`.
3. Chống trùng giao dịch bằng khóa khử trùng; giao dịch lặp trả thành công mà không xử lý lại.
4. Chỉ xử lý giao dịch **tiền vào**, **đúng số tài khoản** cấu hình, **số tiền > 0**.
5. Tìm phiên theo mã `PAY-XXXXXXXXXXXX` trong nội dung chuyển khoản; nếu không có hoặc lệch số tiền, lấy phiên **đang chờ mới nhất có cùng số tiền**.
6. Tìm được phiên thì hoàn tất phiên (lưu đơn). Mọi trường hợp bỏ qua đều trả HTTP 200 `success`.
7. `GET` trả văn bản `SePay webhook is ready` kèm payload gần nhất (phục vụ kiểm tra).

### 6.4 Cấu hình

| Cấu hình | Vị trí | Ghi chú |
|---|---|---|
| Kết nối CSDL | `src/main/resources/META-INF/persistence.xml` | Driver SQL Server, `hibernate.hbm2ddl` **không bật** → lược đồ phải tạo sẵn |
| SePay | `sepay.properties`, system property `sepay.*` | Ngân hàng, mã ngân hàng, số tài khoản, tên tài khoản, nội dung mặc định, API key / secret webhook |
| Email SMTP | `email.properties` (gốc dự án và `resources`) | Đọc bởi `EmailConfig`; tệp gốc nằm trong `.gitignore` |
| Địa chỉ ứng dụng | `AppConfig` | Dùng dựng liên kết trong email |

---

## 7. Danh mục URL / Servlet

| URL | Servlet | Vai trò chính |
|---|---|---|
| `/`, `/home` | `HomeServlet` | Trang chủ |
| `/login`, `/register`, `/logout`, `/forgot-password` | `LoginServlet`, `RegisterServlet`, `LogoutServlet`, `ForgotPasswordServlet` | Xác thực |
| `/customer-shop`, `/customer-portal` | `CustomerShopServlet`, `CustomerPortalServlet` | Cửa hàng khách |
| `/payment-session` | `PaymentSessionServlet` | Tạo/tra cứu/hoàn tất phiên thanh toán |
| `/sepay-ipn`, `/sepay-webhook` | `SePayIpnServlet` | Nhận thông báo giao dịch |
| `/dashboard` | `DashboardServlet` | Thống kê, banner |
| `/khachhang`, `/nhanvien`, `/taikhoan` | `KhachHangServlet`, `NhanVienServlet`, `TaiKhoanServlet` | Quản trị người dùng |
| `/sanpham`, `/lichsugiasanpham`, `/khuyenmai` | `SanPhamServlet`, `LichSuGiaSanPhamServlet`, `KhuyenMaiServlet` | Danh mục & giá |
| `/giohang`, `/chitietgiohang`, `/dathang`, `/hoadon`, `/chitiethoadon` | các servlet tương ứng | Đơn hàng, hóa đơn |
| `/nhanvien-home`, `/nhanvien-donhang` | `NhanVienHomeServlet`, `NhanVienDonHangServlet` | Việc của nhân viên |
| `/giaohang-home`, `/giaohang-donhang` | `GiaoHangHomeServlet`, `NhanVienDonHangServlet` | Việc của tài xế |
| `/uploads/*`, `/promo-banners/*` | `UploadedImageServlet`, `PromoBannerImageServlet` | Phục vụ ảnh tải lên |

Quy ước CRUD chung của các servlet quản trị: `GET ?action=add|edit|delete` và `POST` với `action=create|update`.

---

## 8. Yêu cầu phi chức năng

| Mã | Nhóm | Yêu cầu |
|---|---|---|
| NFR-01 | Bảo mật | Mật khẩu băm PBKDF2 (210.000 vòng); so sánh băm bằng so sánh hằng thời gian. |
| NFR-02 | Bảo mật | Mọi đường dẫn phi công khai đi qua `AuthFilter`; kiểm tra quyền theo vai trò đã chuẩn hóa. |
| NFR-03 | Bảo mật | `returnUrl` chỉ chấp nhận đường dẫn nội bộ (chống open redirect). |
| NFR-04 | Bảo mật | Webhook phải xác thực bằng chữ ký HMAC hoặc API key (xem rủi ro R-03). |
| NFR-05 | Toàn vẹn dữ liệu | Việc tạo `GioHang`, `DatHang`, `HoaDon`, chi tiết và `DoanhThu` là một giao dịch nguyên tử. |
| NFR-06 | Toàn vẹn dữ liệu | Xóa mềm nhân viên; không xóa khách/sản phẩm đã phát sinh giao dịch. |
| NFR-07 | Mã hóa ký tự | Toàn bộ biểu mẫu và phản hồi dùng UTF-8 (tiếng Việt có dấu). |
| NFR-08 | Hiệu năng | Phiên thanh toán xử lý trong bộ nhớ (`ConcurrentHashMap`), đồng bộ theo từng phiên; tự dọn phiên cũ sau 15 phút. |
| NFR-09 | Khả dụng | Giao diện duyệt được trên trình duyệt hiện đại, có bố cục co giãn (`style.css`). |
| NFR-10 | Tương thích | Chạy trên servlet container hỗ trợ Jakarta EE 10 (Tomcat 10.1+); không dùng `javax.*`. |
| NFR-11 | Bảo trì | Tách lớp servlet/DAO/entity/service; các tập lệnh nâng cấp CSDL nằm trong `/sql`. |
| NFR-12 | Quan sát | `hibernate.show_sql` và `format_sql` đang bật (phục vụ phát triển). |

---

## 9. Ràng buộc và giả định

- Phiên thanh toán **không bền vững**: khởi động lại ứng dụng làm mất phiên đang chờ.
- Giỏ hàng nằm ở trình duyệt (`localStorage`), không đồng bộ giữa thiết bị.
- Đơn giá trong giỏ lấy từ dữ liệu thẻ sản phẩm phía giao diện và được gửi lên qua `cartJson`.
- Môi trường dự kiến: SQL Server cục bộ, Tomcat 10.x, Java 17, tài khoản SePay đã cấu hình webhook trỏ về `/sepay-ipn`.
- Hệ thống không tự tạo lược đồ CSDL; cần tạo bảng trước và chạy các tập lệnh trong `/sql` khi nâng cấp.

---

## 10. Truy vết yêu cầu → thành phần mã

| Nhóm yêu cầu | Thành phần chính |
|---|---|
| AUTH / REG / PWD | `LoginServlet`, `LoginDAO`, `RegisterServlet`, `ForgotPasswordServlet`, `PasswordResetService`, `PasswordUtils`, `OtpUtils`, `AuthStore`, `AuthFilter`, `RoleUtils` |
| KH / NV / TK | `KhachHangServlet/DAO`, `NhanVienServlet/DAO`, `NhanVienService`, `TaiKhoanServlet/DAO` |
| SP / LSG / KM | `SanPhamServlet/DAO`, `LichSuGiaSanPhamServlet/DAO`, `KhuyenMaiServlet/DAO`, `UploadPathUtil` |
| SHOP / PAY / ORD | `CustomerShopServlet`, `views/customer/shop.jsp`, `PaymentSessionServlet`, `PaymentSessionStore`, `SePayIpnServlet`, `SePayConfig`, `OrderPersistenceService` |
| WF / HD | `NhanVienDonHangServlet`, `HoaDonServlet`, `HoaDonDAO`, `DatHangDAO`, `OrderStatusUtils` |
| DASH | `DashboardServlet`, `DoanhThuDAO`, `BannerImageUtil`, `PromoBannerImageServlet` |

---

## 11. Hạn chế và rủi ro đã quan sát

Các điểm dưới đây rút ra từ việc đọc mã; nên xác minh bằng kiểm thử (có test case tương ứng trong `TEST_CASES.md`).

| Mã | Mức | Mô tả | Gợi ý |
|---|---|---|---|
| R-01 | **Cao** | `POST /payment-session?action=complete&id=<PAY-…>` nằm trong nhóm đường dẫn công khai và **không kiểm tra giao dịch ngân hàng thực tế**. Biết mã phiên là đủ để hoàn tất phiên và tạo đơn đã "thanh toán". | Chỉ cho phép hoàn tất qua webhook đã xác thực hoặc yêu cầu quyền nhân viên. |
| R-02 | **Cao** | Đơn giá lấy từ `cartJson` do trình duyệt gửi; máy chủ chỉ kiểm tra giá > 0, **không đối chiếu** với `SanPham.DonGia` hay `Size.PhuThu`. Khách có thể sửa giá. | Tính lại giá phía máy chủ từ CSDL. |
| R-03 | **Cao** | Webhook chấp nhận yêu cầu **không có** header xác thực (`Authorization` trống → được phép). | Bắt buộc có chữ ký hoặc API key. |
| R-04 | Trung bình | Khi lưu đơn, `OrderPersistenceService` tính giảm giá chỉ theo **phần trăm** của phiên. Voucher kiểu `AMOUNT` có thể bị lưu `GiamGia = 0` và `MucGiamToiDa` có thể không được áp lại, làm hóa đơn/doanh thu lệch số tiền khách thực trả. | Dùng đúng số tiền giảm đã tính trong phiên thanh toán. |
| R-05 | Trung bình | Không trừ `SanPham.SoLuongTon` khi bán; không kiểm tra tồn kho khi đặt hàng. | Bổ sung trừ/kiểm tồn trong giao dịch lưu đơn. |
| R-06 | Trung bình | Webhook ghép giao dịch với **phiên đang chờ mới nhất cùng số tiền** khi không đọc được mã phiên → có thể xác nhận nhầm cho khách khác có cùng số tiền. | Bắt buộc mã phiên trong nội dung chuyển khoản. |
| R-07 | Trung bình | Thông tin nhạy cảm (mật khẩu CSDL `sa`, API key) nằm trực tiếp trong `persistence.xml`/`sepay.properties` trong kho mã. | Chuyển sang biến môi trường/secret. |
| R-08 | Trung bình | Thao tác xóa thực hiện qua `GET ?action=delete&id=…`, không có token chống CSRF. | Dùng `POST` + token CSRF. |
| R-09 | Trung bình | `/payment-session` công khai; nếu phiên chưa có khách đăng nhập, mã khách có thể lấy từ tham số `maKH` do client gửi. | Chỉ lấy mã khách từ session. |
| R-10 | Thấp | Đăng ký không kiểm tra định dạng SĐT/email; đăng nhập coi chuỗi 8–15 chữ số là SĐT. | Bổ sung kiểm tra định dạng. |
| R-11 | Thấp | Chuẩn hóa vai trò rộng (cả tiếng Việt/Anh); `/customer-portal` rơi vào nhánh mặc định "chỉ cần đăng nhập". | Rà soát danh sách đường dẫn khi thêm chức năng. |
| R-12 | Thấp | `reset_invoice_workflow.sql` xóa toàn bộ hóa đơn/doanh thu. | Chỉ chạy trên môi trường thử. |
| R-13 | Thấp | Thư mục `target/`, `.idea/`, `tmp/` và `email.properties` gốc có trong bản nén. | Loại khỏi kho mã nguồn. |