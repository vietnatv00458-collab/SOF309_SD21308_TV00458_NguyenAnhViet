# BẢNG CA KIỂM THỬ — QUẢN LÝ QUÁN COFFEE

| Thông tin | Nội dung |
|---|---|
| Hệ thống | Quản lý quán Coffee (`QuanLyCafe`) |
| Tài liệu tham chiếu | `SPECIFICATIONS.md`, `TEST_PLAN.md` |
| Phiên bản | 1.0 |
| Ngày lập | 16/11/2026 |
| Tổng số ca | **309** |

## 1. Hướng dẫn sử dụng

**Cách đọc bảng**

- **Mã ca:** `TC-<MODULE>-<số>`. Mã yêu cầu ở cột cuối truy vết về `SPECIFICATIONS.md` (ví dụ `FR-PAY-03`, `3.2` là mục 3.2 ma trận phân quyền, `R-xx` là rủi ro ở mục 11).
- **Ưu tiên:** `P1` Cao (tiền, bảo mật, bán hàng), `P2` Trung bình, `P3` Thấp.
- **Kết quả thực tế:** ghi vào cột cuối bằng `✔ Đạt`, `✘ Trượt`, `⏸ Bị chặn`, `— Chưa chạy` (mặc định `☐`). Khi trượt, ghi mã lỗi trong nhật ký lỗi.
- Các ca có dấu **⚠** kiểm chứng các rủi ro đã quan sát trong mã nguồn (R-01…R-09). Cột “Kết quả mong đợi” nêu hành vi **đúng theo đặc tả/an toàn**; phần *“Mã hiện tại …”* ghi hành vi dự đoán từ mã nguồn. Nếu thực tế khớp phần dự đoán thì ca **Trượt** và là lỗi cần sửa.
- Mọi thông báo lỗi trong cột mong đợi được trích nguyên văn từ mã nguồn.

**Dữ liệu và tài khoản mẫu** (xem chi tiết ở `TEST_PLAN.md` mục 4.2)

| Đối tượng | Giá trị |
|---|---|
| Quản lý | `admin01` / `Admin@123` |
| Nhân viên | `nv01` (SĐT 0900000001), `nv02` — mật khẩu `Nv@12345` |
| Giao hàng | `gh01`, `gh02` — mật khẩu `Gh@12345` |
| Khách hàng | `KH01` (SĐT 0911111111, có tài khoản), `KH02` (chưa có tài khoản) |
| Khuyến mãi | `VCPCT1` 10%, `VCPCT2` 50% tối đa 20.000, `VCAMT1` 15.000đ, `VCEXP1` hết hạn, `VCFUT1` chưa bắt đầu, `VCEDGE` kết thúc hôm nay |
| Đơn mẫu | `DH01` tại quán, `DH02` giao hàng, `DH03` đã hoàn thành |

**Địa chỉ cơ sở:** `http://localhost:8080/QuanLyCafe` (điều chỉnh theo môi trường).

## 2. Tổng hợp

| Module | Tên | Số ca | P1 | P2 | P3 |
|---|---|---:|---:|---:|---:|
| AUTH | Đăng nhập / Đăng xuất | 20 | 11 | 6 | 3 |
| REG | Đăng ký khách hàng | 14 | 9 | 4 | 1 |
| PWD | Quên mật khẩu bằng OTP | 18 | 9 | 7 | 2 |
| PERM | Phân quyền theo vai trò | 18 | 13 | 4 | 1 |
| KH | Quản lý khách hàng | 9 | 2 | 6 | 1 |
| NV | Quản lý nhân viên | 15 | 8 | 5 | 2 |
| TK | Quản lý tài khoản | 13 | 7 | 5 | 1 |
| SP | Quản lý sản phẩm | 22 | 9 | 8 | 5 |
| LSG | Lịch sử giá sản phẩm | 10 | 3 | 4 | 3 |
| KM | Quản lý khuyến mãi | 24 | 11 | 11 | 2 |
| SHOP | Cửa hàng khách và giỏ hàng | 21 | 10 | 6 | 5 |
| PAY | Phiên thanh toán | 22 | 15 | 4 | 3 |
| IPN | Webhook SePay | 21 | 13 | 5 | 3 |
| ORD | Lưu đơn sau thanh toán | 16 | 10 | 6 | 0 |
| WF | Quy trình xử lý đơn | 23 | 13 | 8 | 2 |
| HD | Hóa đơn | 12 | 1 | 11 | 0 |
| DASH | Dashboard, doanh thu, banner | 10 | 5 | 1 | 4 |
| NFR | Bảo mật và phi chức năng | 16 | 8 | 6 | 2 |
| E2E | Kịch bản xuyên suốt | 5 | 5 | 0 | 0 |
| **Tổng** | | **309** | **162** | **107** | **40** |

## 3. Đăng nhập / Đăng xuất (AUTH)

Yêu cầu liên quan: FR-AUTH-01…09

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-AUTH-01 | Đăng nhập nhân viên bằng số điện thoại | NV01 có SĐT 0900000001, tài khoản nv01 | 1. Mở /login<br>2. Nhập loginId `0900000001`, mật khẩu `Nv@12345`<br>3. Nhấn Đăng nhập | Chuyển tới `/nhanvien-home`; session có `user` và `role` | P1 | FR-AUTH-01, 02, 06 | ☐ |
| TC-AUTH-02 | Đăng nhập quản lý bằng tên đăng nhập | Tài khoản admin01 (vai trò Admin) | 1. Nhập `admin01` / `Admin@123` | Chuyển tới `/dashboard` | P1 | FR-AUTH-01, 06 | ☐ |
| TC-AUTH-03 | Đăng nhập khách hàng bằng số điện thoại | KH01 SĐT 0911111111 có tài khoản | 1. Nhập `0911111111` và mật khẩu đúng | Chuyển tới `/customer-shop` | P1 | FR-AUTH-02, 06 | ☐ |
| TC-AUTH-04 | Đăng nhập khách hàng bằng mã khách (tên đăng nhập) | Tên đăng nhập của khách = MaKH (`KH01`) | 1. Nhập `KH01` và mật khẩu đúng | Đăng nhập thành công, về `/customer-shop` | P2 | FR-AUTH-01, REG-04 | ☐ |
| TC-AUTH-05 | Đăng nhập tài xế | Tài khoản gh01 | 1. Nhập `gh01` / `Gh@12345` | Chuyển tới `/giaohang-home` | P1 | FR-AUTH-06 | ☐ |
| TC-AUTH-06 | Sai mật khẩu | Tài khoản admin01 | 1. Nhập `admin01` và mật khẩu sai | Ở lại trang đăng nhập, hiển thị `Sai số điện thoại hoặc mật khẩu!`; không tạo session `user` | P1 | FR-AUTH-03 | ☐ |
| TC-AUTH-07 | Tài khoản không tồn tại | — | 1. Nhập `khongco123` / `abc123` | Hiển thị `Sai số điện thoại hoặc mật khẩu!` | P1 | FR-AUTH-03 | ☐ |
| TC-AUTH-08 | Để trống tên đăng nhập và mật khẩu | — | 1. Nhấn Đăng nhập khi để trống cả hai ô | Không đăng nhập được; hiển thị thông báo lỗi, hệ thống không báo lỗi 500 | P2 | FR-AUTH-03 | ☐ |
| TC-AUTH-09 | Nhân viên đã xóa mềm không đăng nhập được | NV05 `IsDeleted = 1` có tài khoản, mật khẩu đúng | 1. Đăng nhập bằng tài khoản NV05 | Bị từ chối với `Sai số điện thoại hoặc mật khẩu!` | P1 | FR-AUTH-04, NV-06 | ☐ |
| TC-AUTH-10 | Tự nâng cấp mật khẩu cũ (văn bản thường) sang băm | Sửa trực tiếp CSDL: `TaiKhoan.MatKhau = '123456'` cho một tài khoản | 1. Đăng nhập bằng mật khẩu `123456`<br>2. Kiểm tra cột `MatKhau` trong CSDL | Đăng nhập thành công; `MatKhau` đổi thành dạng `PBKDF2$210000$…`; đăng nhập lại vẫn đúng | P2 | FR-AUTH-05 | ☐ |
| TC-AUTH-11 | Mật khẩu lưu dạng băm có muối | Hai tài khoản đặt cùng mật khẩu | 1. Đăng ký/đặt mật khẩu giống nhau cho 2 tài khoản<br>2. So sánh cột `MatKhau` | Hai chuỗi băm khác nhau (salt ngẫu nhiên), đều bắt đầu `PBKDF2$210000$` | P2 | FR-AUTH-05, NFR-01 | ☐ |
| TC-AUTH-12 | Cắt khoảng trắng đầu/cuối loginId | NV01 SĐT 0900000001 | 1. Nhập ` 0900000001 ` (có khoảng trắng hai đầu) và mật khẩu đúng | Đăng nhập thành công | P3 | FR-AUTH-01 | ☐ |
| TC-AUTH-13 | Chuỗi 7 chữ số không được coi là số điện thoại | — | 1. Nhập `0900000` và mật khẩu bất kỳ | Coi là tên đăng nhập, không tìm theo SĐT → đăng nhập thất bại, không lỗi 500 | P3 | FR-AUTH-02 | ☐ |
| TC-AUTH-14 | Lưu `loginPhone` khi đăng nhập bằng SĐT | Nhân viên nv01 | 1. Đăng nhập bằng `0900000001`<br>2. Tạo phiên thanh toán (cần mã NV) | Mã nhân viên được xác định đúng nhờ `loginPhone`; đăng nhập bằng tên đăng nhập thì thuộc tính này bị xóa | P3 | FR-AUTH-06 | ☐ |
| TC-AUTH-15 | returnUrl nội bộ hợp lệ (khách hàng) | Khách KH01 | 1. Mở `/login?returnUrl=/customer-shop` (form gửi kèm `returnUrl`)<br>2. Đăng nhập đúng | Chuyển tới `/QuanLyCafe/customer-shop` (context + returnUrl) | P2 | FR-AUTH-07 | ☐ |
| TC-AUTH-16 | returnUrl dạng `//evil.com` bị bỏ qua | Khách KH01 | 1. Gửi POST /login với `returnUrl=//evil.com` | Không chuyển ra ngoài; về `/customer-shop` | P1 | FR-AUTH-07, NFR-03 | ☐ |
| TC-AUTH-17 | returnUrl tuyệt đối `http://evil.com` bị bỏ qua | Khách KH01 | 1. Gửi POST /login với `returnUrl=http://evil.com` | Về `/customer-shop` | P1 | FR-AUTH-07, NFR-03 | ☐ |
| TC-AUTH-18 | returnUrl không ảnh hưởng vai trò nội bộ | Nhân viên nv01 | 1. POST /login kèm `returnUrl=/dashboard` | Vẫn về `/nhanvien-home` (chỉ khách dùng returnUrl) | P2 | FR-AUTH-07 | ☐ |
| TC-AUTH-19 | Đăng xuất | Đã đăng nhập bất kỳ vai trò | 1. Nhấn Đăng xuất (`/logout`)<br>2. Mở lại `/dashboard` hoặc nhấn nút Back | Session bị hủy; truy cập trang quản trị bị chuyển về `/login` | P1 | FR-AUTH-08 | ☐ |
| TC-AUTH-20 | Tấn công SQL injection ở ô đăng nhập | — | 1. Nhập loginId `' OR 1=1 --` và mật khẩu `x` | Không đăng nhập được; không lỗi 500 (truy vấn dùng tham số) | P1 | FR-AUTH-03, NFR-02 | ☐ |

## 4. Đăng ký khách hàng (REG)

Yêu cầu liên quan: FR-REG-01…05

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-REG-01 | Đăng ký khách mới thành công | SĐT `0922222222` chưa tồn tại | 1. Mở /register<br>2. Nhập họ tên `Nguyễn Văn An`, SĐT, email, địa chỉ, mật khẩu `abc123`, xác nhận `abc123`<br>3. Gửi | Tạo `KhachHang` mã mới dạng `KHnn`; tạo `TaiKhoan` (`TenDangNhap = MaKH`, `VaiTro = KHACHHANG`, mật khẩu băm); tự đăng nhập; chuyển `/customer-shop` | P1 | FR-REG-01, 03, 04, 05 | ☐ |
| TC-REG-02 | Thiếu họ tên | — | 1. Bỏ trống họ tên, điền các ô còn lại, gửi | `Họ tên, số điện thoại và mật khẩu không được để trống!`; giữ ở trang đăng ký | P1 | FR-REG-02 | ☐ |
| TC-REG-03 | Thiếu số điện thoại | — | 1. Bỏ trống SĐT, gửi | Cùng thông báo như trên | P1 | FR-REG-02 | ☐ |
| TC-REG-04 | Thiếu mật khẩu hoặc xác nhận mật khẩu | — | 1. Bỏ trống ô xác nhận mật khẩu, gửi | Cùng thông báo `… không được để trống!` | P1 | FR-REG-02 | ☐ |
| TC-REG-05 | Họ tên chỉ gồm khoảng trắng | — | 1. Nhập họ tên `   `, các ô khác hợp lệ | Coi như trống → thông báo lỗi bắt buộc | P2 | FR-REG-02 | ☐ |
| TC-REG-06 | Xác nhận mật khẩu không khớp | — | 1. Mật khẩu `abc123`, xác nhận `abc124` | `Xác nhận mật khẩu không khớp!` | P1 | FR-REG-02 | ☐ |
| TC-REG-07 | Mật khẩu 5 ký tự (biên dưới) | — | 1. Mật khẩu và xác nhận `abc12` | `Mật khẩu phải có ít nhất 6 ký tự!` | P1 | FR-REG-02 | ☐ |
| TC-REG-08 | Mật khẩu 6 ký tự (biên hợp lệ) | SĐT chưa dùng | 1. Mật khẩu và xác nhận `abc123` | Đăng ký thành công | P1 | FR-REG-02 | ☐ |
| TC-REG-09 | Thứ tự kiểm tra lỗi: không khớp ưu tiên trước độ dài | — | 1. Mật khẩu `abc`, xác nhận `abd` | Hiển thị `Xác nhận mật khẩu không khớp!` (không phải lỗi độ dài) | P3 | FR-REG-02 | ☐ |
| TC-REG-10 | SĐT đã có tài khoản | 0911111111 thuộc KH01 đã có tài khoản | 1. Đăng ký với SĐT `0911111111` | `Số điện thoại này đã có tài khoản. Hãy đăng nhập.`; không tạo bản ghi mới | P1 | FR-REG-02 | ☐ |
| TC-REG-11 | SĐT đã là khách hàng nhưng chưa có tài khoản | KH02 có SĐT, chưa có `TaiKhoan` | 1. Đăng ký bằng SĐT của KH02, bỏ trống email/địa chỉ | Dùng lại `MaKH = KH02` (không tạo khách trùng); giữ email/địa chỉ cũ của KH02; tạo tài khoản mới | P1 | FR-REG-03 | ☐ |
| TC-REG-12 | Bỏ trống email và địa chỉ (khách mới) | SĐT mới | 1. Chỉ điền họ tên, SĐT, mật khẩu | Đăng ký thành công; email và địa chỉ lưu rỗng | P2 | FR-REG-01 | ☐ |
| TC-REG-13 | Sinh mã khách liên tiếp | Đã có KH01…KH02 | 1. Đăng ký 2 khách mới liên tiếp | Mã lần lượt `KH03`, `KH04` (không trùng) | P2 | FR-REG-03 | ☐ |
| TC-REG-14 | Họ tên tiếng Việt có dấu và ký tự đặc biệt | — | 1. Họ tên `Trần Thị Bích Ngọc – Đà Nẵng` | Lưu và hiển thị đúng UTF-8, không lỗi font | P2 | NFR-07 | ☐ |

## 5. Quên mật khẩu bằng OTP (PWD)

Yêu cầu liên quan: FR-PWD-01…06

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-PWD-01 | Gửi OTP thành công | KH01 có email thật của hộp thư thử và có tài khoản | 1. Mở /forgot-password<br>2. Nhập email KH01, gửi | Thông báo `Đã gửi mã xác thực đến email đã đăng ký.`; chuyển sang bước nhập OTP; hộp thư nhận email tiêu đề `Mã xác thực đặt lại mật khẩu` | P1 | FR-PWD-01, 03 | ☐ |
| TC-PWD-02 | Nội dung email chứa OTP 6 chữ số và thời hạn 5 phút | Đã gửi OTP | 1. Mở email nhận được | Có mã gồm đúng 6 chữ số và câu nêu hiệu lực 5 phút | P2 | FR-PWD-03, 04 | ☐ |
| TC-PWD-03 | Email sai định dạng | — | 1. Nhập `abc@` rồi gửi | `Vui lòng nhập email hợp lệ.` | P1 | FR-PWD-01 | ☐ |
| TC-PWD-04 | Email để trống | — | 1. Gửi form với email trống | `Vui lòng nhập email hợp lệ.` | P2 | FR-PWD-01 | ☐ |
| TC-PWD-05 | Email không gắn tài khoản nào | — | 1. Nhập `khongco@example.com` | `Không tìm thấy tài khoản gắn với email này trong hệ thống.` | P1 | FR-PWD-02 | ☐ |
| TC-PWD-06 | Lỗi cấu hình SMTP | Cố ý đặt sai mật khẩu SMTP | 1. Nhập email hợp lệ có tài khoản, gửi | `Không gửi được email xác thực. Kiểm tra cấu hình SMTP.`; không lưu OTP | P2 | FR-PWD-03 | ☐ |
| TC-PWD-07 | Đặt lại mật khẩu thành công | Đã nhận OTP đúng | 1. Nhập OTP, mật khẩu mới `newpass1`, xác nhận `newpass1`, gửi<br>2. Đăng nhập bằng mật khẩu mới<br>3. Thử mật khẩu cũ | Chuyển `/login` với `Đổi mật khẩu thành công. Vui lòng đăng nhập lại.`; mật khẩu mới đăng nhập được, mật khẩu cũ bị từ chối; `MatKhau` dạng băm | P1 | FR-PWD-05, 06 | ☐ |
| TC-PWD-08 | OTP sai | Đang ở bước OTP | 1. Nhập OTP sai `000000` | `Mã xác thực không đúng.`; vẫn ở bước OTP; bộ đếm sai tăng 1 | P1 | FR-PWD-05 | ☐ |
| TC-PWD-09 | Nhập sai 4 lần rồi nhập đúng ở lần thứ 5 (biên) | OTP còn hạn | 1. Nhập sai 4 lần<br>2. Lần 5 nhập đúng OTP và mật khẩu hợp lệ | Đặt lại mật khẩu thành công | P2 | FR-PWD-04 | ☐ |
| TC-PWD-10 | Nhập sai 5 lần thì khóa OTP | OTP còn hạn | 1. Nhập sai 5 lần<br>2. Lần 6 nhập OTP **đúng** | `Bạn đã nhập sai quá nhiều lần. Vui lòng gửi lại mã mới.`; OTP bị hủy; phải yêu cầu mã mới | P1 | FR-PWD-04 | ☐ |
| TC-PWD-11 | OTP hết hạn sau 5 phút | Đã gửi OTP | 1. Chờ hơn 5 phút<br>2. Nhập OTP đúng | `Mã xác thực đã hết hạn. Vui lòng gửi lại mã mới.` | P1 | FR-PWD-04 | ☐ |
| TC-PWD-12 | Mật khẩu mới 5 ký tự | OTP đúng | 1. Mật khẩu mới `abc12` | `Mật khẩu mới phải có ít nhất 6 ký tự.`; OTP vẫn còn hiệu lực | P1 | FR-PWD-05 | ☐ |
| TC-PWD-13 | Xác nhận mật khẩu mới không khớp | OTP đúng | 1. Mật khẩu `abc123`, xác nhận `abc124` | `Xác nhận mật khẩu không khớp.` | P2 | FR-PWD-05 | ☐ |
| TC-PWD-14 | Đặt lại khi chưa yêu cầu OTP | Session mới | 1. Gửi trực tiếp POST `action=reset` kèm OTP bất kỳ | `Vui lòng yêu cầu mã xác thực trước.` | P1 | FR-PWD-05 | ☐ |
| TC-PWD-15 | Email ở bước đặt lại khác email đã gửi mã | Đã gửi OTP cho email A | 1. Gửi reset với email B và OTP đúng | `Email không khớp với yêu cầu đã gửi mã.` | P2 | FR-PWD-05 | ☐ |
| TC-PWD-16 | Gửi lại OTP làm OTP cũ vô hiệu | Đã nhận OTP1 | 1. Yêu cầu gửi lại → OTP2<br>2. Dùng OTP1 để đặt lại | OTP1 bị từ chối `Mã xác thực không đúng.`; OTP2 dùng được; bộ đếm sai được đặt lại 0 | P2 | FR-PWD-04 | ☐ |
| TC-PWD-17 | Tải lại trang khi OTP còn hiệu lực | Đã gửi OTP, chưa quá 5 phút | 1. F5 / mở lại `/forgot-password` | Vẫn hiển thị bước nhập OTP kèm email | P3 | FR-PWD-04 | ☐ |
| TC-PWD-18 | Đặt lại xong thì hủy phiên đăng nhập hiện tại | Đã đăng nhập ở cùng trình duyệt | 1. Hoàn tất đặt lại mật khẩu | Session mất `user`, `role`; phải đăng nhập lại | P3 | FR-PWD-06 | ☐ |

## 6. Phân quyền theo vai trò (PERM)

Yêu cầu liên quan: Mục 3.2 đặc tả — AuthFilter

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-PERM-01 | Khách vãng lai truy cập `/dashboard` | Chưa đăng nhập | 1. GET `/dashboard` | Chuyển hướng `/login` | P1 | 3.2 | ☐ |
| TC-PERM-02 | Khách vãng lai truy cập `/sanpham`, `/hoadon`, `/khuyenmai` | Chưa đăng nhập | 1. GET lần lượt 3 URL | Cả 3 chuyển hướng `/login` | P1 | 3.2 | ☐ |
| TC-PERM-03 | Khách vãng lai truy cập JSP trực tiếp | Chưa đăng nhập | 1. GET `/views/dashboard/dashboard.jsp`, `/views/khachhang/list.jsp` | Chuyển hướng `/login` | P1 | 3.2 | ☐ |
| TC-PERM-04 | Khách vãng lai xem cửa hàng và trang công khai | Chưa đăng nhập | 1. GET `/`, `/home`, `/login`, `/register`, `/forgot-password`, `/customer-shop` | Tất cả trả 200, hiển thị bình thường | P1 | 3.2 | ☐ |
| TC-PERM-05 | Tài nguyên tĩnh không cần đăng nhập | Chưa đăng nhập | 1. GET `/css/style.css`, `/images/logo-premium.png`, một ảnh trong `/uploads/` | Trả 200 | P2 | 3.2 | ☐ |
| TC-PERM-06 | Khách hàng truy cập trang quản trị | Đăng nhập KH01 | 1. GET `/dashboard`, `/nhanvien`, `/sanpham`, `/hoadon` | Mỗi lần chuyển hướng về `/customer-shop` | P1 | 3.2 | ☐ |
| TC-PERM-07 | Khách hàng truy cập trang việc của nhân viên/tài xế | Đăng nhập KH01 | 1. GET `/nhanvien-home`, `/giaohang-home`, `/nhanvien-donhang` | Chuyển hướng về `/customer-shop` | P1 | 3.2 | ☐ |
| TC-PERM-08 | Nhân viên truy cập trang chỉ dành cho quản lý | Đăng nhập nv01 | 1. GET `/dashboard`, `/taikhoan`, `/khachhang`, `/nhanvien`, `/dathang`, `/giohang`, `/chitiethoadon`, `/chitietgiohang` | Mỗi lần chuyển hướng về `/nhanvien-home` | P1 | 3.2 | ☐ |
| TC-PERM-09 | Nhân viên truy cập cửa hàng khách | Đăng nhập nv01 | 1. GET `/customer-shop` | Chuyển hướng `/nhanvien-home` | P2 | 3.2 | ☐ |
| TC-PERM-10 | Nhân viên được xem sản phẩm, lịch sử giá, khuyến mãi, hóa đơn | Đăng nhập nv01 | 1. GET `/sanpham`, `/lichsugiasanpham`, `/khuyenmai`, `/hoadon` | Trả 200 | P1 | 3.2 | ☐ |
| TC-PERM-11 | Nhân viên bị chặn form thêm/sửa sản phẩm và khuyến mãi | Đăng nhập nv01 | 1. GET `/views/sanpham/form.jsp`, `/views/khuyenmai/form.jsp` | Chuyển hướng `/nhanvien-home` | P1 | 3.2 | ☐ |
| TC-PERM-12 | Tài xế truy cập trang của mình và hóa đơn | Đăng nhập gh01 | 1. GET `/giaohang-home`, `/giaohang-donhang?action=summary`, `/hoadon` | Trả 200 | P1 | 3.2 | ☐ |
| TC-PERM-13 | Tài xế truy cập trang nhân viên / quản trị | Đăng nhập gh01 | 1. GET `/nhanvien-home`, `/sanpham`, `/dashboard`, `/khuyenmai` | Bị chặn, chuyển hướng `/login` (vai trò giao hàng không có trang chủ chuyển hướng riêng) | P1 | 3.2 | ☐ |
| TC-PERM-14 | Quản lý truy cập toàn bộ khu quản trị | Đăng nhập admin01 | 1. GET `/dashboard`, `/taikhoan`, `/khachhang`, `/nhanvien`, `/dathang`, `/giohang`, `/chitiethoadon`, `/chitietgiohang`, `/sanpham`, `/lichsugiasanpham`, `/khuyenmai`, `/hoadon`, `/nhanvien-home`, `/giaohang-home` | Tất cả trả 200 | P1 | 3.2 | ☐ |
| TC-PERM-15 | Quản lý truy cập cửa hàng khách | Đăng nhập admin01 | 1. GET `/customer-shop` | Chuyển hướng `/dashboard` | P2 | 3.2 | ☐ |
| TC-PERM-16 | Tiền tố cũ `/QuanLyCafe/` vẫn bị kiểm tra quyền | Chưa đăng nhập | 1. GET `/QuanLyCafe/QuanLyCafe/dashboard` (tiền tố lặp) | Được chuẩn hóa như `/dashboard` → chuyển `/login`, không bỏ qua kiểm tra | P3 | 3.2 | ☐ |
| TC-PERM-17 | Phiên hết hạn | Đăng nhập admin01, để session quá thời gian hoặc xóa cookie | 1. Thao tác tiếp trên `/sanpham` | Chuyển hướng `/login` | P2 | 3.2 | ☐ |
| TC-PERM-18 | Thao tác ghi bị chặn đúng vai trò (xác nhận ở máy chủ, không chỉ ẩn nút) | Đăng nhập nv01 | 1. Dùng Postman gửi POST `/nhanvien` action=create<br>2. POST `/taikhoan` action=create | Bị chuyển hướng, không có bản ghi mới | P1 | 3.2, NFR-02 | ☐ |

## 7. Quản lý khách hàng (KH)

Yêu cầu liên quan: FR-KH-01…04

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-KH-01 | Xem danh sách khách hàng | Đăng nhập admin01 | 1. Mở `/khachhang` | Hiển thị danh sách đầy đủ cột Mã, Họ tên, SĐT, Email, Địa chỉ | P2 | FR-KH-01 | ☐ |
| TC-KH-02 | Thêm khách hàng hợp lệ | Quản lý | 1. `?action=add`<br>2. Nhập `KH90`, họ tên, SĐT, email, địa chỉ → Lưu | Quay về danh sách và có bản ghi `KH90` | P2 | FR-KH-01, 02 | ☐ |
| TC-KH-03 | Thêm khách thiếu mã hoặc họ tên | Quản lý | 1. Bỏ trống mã (hoặc họ tên), Lưu | `Mã khách hàng và họ tên không được để trống!` | P2 | FR-KH-02 | ☐ |
| TC-KH-04 | Thêm khách trùng mã | Đã có KH01 | 1. Thêm khách mã `KH01` | `Mã khách hàng đã tồn tại!` | P1 | FR-KH-02 | ☐ |
| TC-KH-05 | Sửa khách hàng hợp lệ | KH02 | 1. `?action=edit&id=KH02`, đổi địa chỉ → Lưu | Dữ liệu cập nhật | P2 | FR-KH-01 | ☐ |
| TC-KH-06 | Sửa khách với họ tên trống | KH02 | 1. Xóa trắng họ tên → Lưu | `Họ tên không được để trống!` | P2 | FR-KH-03 | ☐ |
| TC-KH-07 | Xóa khách chưa phát sinh giao dịch | Khách `KH90` không có đơn/hóa đơn | 1. `?action=delete&id=KH90` | Khách bị xóa; tài khoản gắn với khách (nếu có) cũng bị xóa | P2 | FR-KH-04 | ☐ |
| TC-KH-08 | Xóa khách đã có đơn hoặc hóa đơn | KH01 có đơn | 1. Xóa KH01 | `Không thể xóa khách hàng vì còn lịch sử đơn hàng hoặc hóa đơn.`; dữ liệu giữ nguyên | P1 | FR-KH-04 | ☐ |
| TC-KH-09 | Họ tên vượt 100 ký tự | Quản lý | 1. Thêm khách với họ tên dài 101 ký tự | Không lưu; hiển thị thông báo lỗi (`Lỗi khi thêm khách hàng: …`), hệ thống không sập | P3 | FR-KH-01 | ☐ |

## 8. Quản lý nhân viên (NV)

Yêu cầu liên quan: FR-NV-01…06

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-NV-01 | Thêm nhân viên hợp lệ, để trống mã | Quản lý | 1. `?action=add`<br>2. Họ tên, ngày sinh 01/01/1995, lương 8000000, chức vụ `Pha chế` → Lưu | Mã tự sinh `NVnn` (tiếp theo); `NgayVaoLam` = hôm nay nếu để trống; `IsDeleted = 0` | P1 | FR-NV-01 | ☐ |
| TC-NV-02 | Thêm nhân viên thiếu họ tên | Quản lý | 1. Bỏ trống họ tên → Lưu | `Họ tên không được để trống!` | P1 | FR-NV-02 | ☐ |
| TC-NV-03 | Thêm nhân viên thiếu ngày sinh | Quản lý | 1. Bỏ trống ngày sinh → Lưu | `Ngày sinh không được để trống!` | P1 | FR-NV-02 | ☐ |
| TC-NV-04 | Ngày sinh khiến tuổi 17 (biên dưới) | Hôm nay là D | 1. Ngày sinh = D − 18 năm + 1 ngày | `Nhân viên phải đủ 18 tuổi trở lên!` | P1 | FR-NV-03 | ☐ |
| TC-NV-05 | Ngày sinh đúng 18 tuổi hôm nay (biên) | Hôm nay là D | 1. Ngày sinh = D − 18 năm | Lưu thành công | P1 | FR-NV-03 | ☐ |
| TC-NV-06 | Ngày sinh ở tương lai | — | 1. Ngày sinh năm sau | `Nhân viên phải đủ 18 tuổi trở lên!` | P2 | FR-NV-03 | ☐ |
| TC-NV-07 | Lương không phải số | — | 1. Lương `abc` → Lưu | `Lương phải là số!` | P2 | FR-NV-04 | ☐ |
| TC-NV-08 | Ngày sinh sai định dạng (gửi trực tiếp) | — | 1. POST ngày sinh `31/02/2000` | `Ngày sinh hoặc ngày vào làm không hợp lệ!` | P3 | FR-NV-04 | ☐ |
| TC-NV-09 | Sửa nhân viên hợp lệ | NV01 | 1. Đổi chức vụ và lương → Lưu | Cập nhật thành công | P2 | FR-NV-01 | ☐ |
| TC-NV-10 | Sửa nhân viên thành dưới 18 tuổi | NV01 | 1. Đổi ngày sinh sao cho tuổi 17 → Lưu | `Nhân viên phải đủ 18 tuổi trở lên!`, dữ liệu cũ giữ nguyên | P2 | FR-NV-03 | ☐ |
| TC-NV-11 | Xóa mềm nhân viên không có đơn | NV06 không có đơn | 1. `?action=delete&id=NV06` | `IsDeleted = 1` trong CSDL (bản ghi không bị xóa vật lý); có thông báo thành công; NV06 không còn trong danh sách đang làm việc | P1 | FR-NV-05 | ☐ |
| TC-NV-12 | Xóa nhân viên có đơn đang xử lý | NV01 có 1 đơn `CHO_NHAN` đã nhận (kèm hóa đơn, chi tiết, doanh thu) | 1. Xóa NV01<br>2. Kiểm tra bảng `DatHang`, `HoaDon`, `ChiTietHoaDon`, `DoanhThu` | Đơn/hóa đơn đang xử lý của NV01 cùng chi tiết và doanh thu bị xóa; thông báo nêu số lượng đã xóa | P1 | FR-NV-05 | ☐ |
| TC-NV-13 | Xóa nhân viên có đơn đã hoàn tất | NV01 có đơn `HOAN_THANH` | 1. Xóa NV01 | Đơn/hóa đơn hoàn tất được giữ nguyên; thông báo nêu số bản ghi giữ lại | P1 | FR-NV-05 | ☐ |
| TC-NV-14 | Xóa nhân viên đã xóa mềm / không tồn tại | NV05 đã `IsDeleted = 1`; mã `NV999` | 1. Xóa NV05<br>2. Xóa `NV999` | Hiển thị `Không tìm thấy nhân viên để xóa.`; không thay đổi dữ liệu | P3 | FR-NV-05 | ☐ |
| TC-NV-15 | Nhân viên đã xóa mềm không được chọn nhận/giao đơn | NV05 đã xóa mềm | 1. Mở màn phân công tài xế | NV05 không xuất hiện trong danh sách chọn | P2 | FR-NV-06 | ☐ |

## 9. Quản lý tài khoản (TK)

Yêu cầu liên quan: FR-TK-01…07

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-TK-01 | Tạo tài khoản cho nhân viên hợp lệ | NV07 (`ChucVu = Pha chế`) chưa có tài khoản | 1. `/taikhoan?action=add`<br>2. Chọn NV07, tên đăng nhập `nv07`, mật khẩu `Nv@12345` → Lưu | Tạo thành công; `VaiTro = NhanVien`; mật khẩu lưu dạng băm; nv07 đăng nhập được | P1 | FR-TK-01, 07 | ☐ |
| TC-TK-02 | Vai trò suy ra từ chức vụ: giao hàng | NV08 `ChucVu = Giao hàng` | 1. Tạo tài khoản cho NV08 | `VaiTro = GiaoHang`; đăng nhập về `/giaohang-home` | P1 | FR-TK-07 | ☐ |
| TC-TK-03 | Vai trò suy ra từ chức vụ: quản lý | NV09 `ChucVu = Quản lý` | 1. Tạo tài khoản cho NV09 | `VaiTro = Admin`; đăng nhập về `/dashboard` | P1 | FR-TK-07 | ☐ |
| TC-TK-04 | Thiếu nhân viên / tên đăng nhập / mật khẩu | — | 1. Gửi form thiếu một trong ba trường | `Vui lòng chọn nhân viên, nhập tên đăng nhập và mật khẩu!` | P1 | FR-TK-01 | ☐ |
| TC-TK-05 | Mã nhân viên không tồn tại | — | 1. POST create với `maNV=NV999` | `Mã nhân viên chưa tồn tại. Hãy tạo nhân viên trước khi tạo tài khoản.` | P2 | FR-TK-02 | ☐ |
| TC-TK-06 | Nhân viên đã có tài khoản | NV01 có tài khoản | 1. Tạo tài khoản mới cho NV01 | `Nhân viên này đã có tài khoản!` | P1 | FR-TK-02 | ☐ |
| TC-TK-07 | Tên đăng nhập trùng | Đã có `nv01` | 1. Tạo tài khoản NV07 với tên `nv01` | `Tên đăng nhập đã tồn tại!` | P1 | FR-TK-03 | ☐ |
| TC-TK-08 | Chế độ tạo tài khoản giao hàng chỉ nhận nhân viên giao hàng | NV07 (không phải giao hàng) | 1. Mở `/taikhoan?action=add&kind=delivery`<br>2. Cố gửi NV07 | `Chế độ này chỉ cho phép chọn nhân viên giao hàng.` | P2 | FR-TK-04 | ☐ |
| TC-TK-09 | Sửa tài khoản: đổi mật khẩu | Tài khoản nv07 | 1. `?action=edit&id=NV07`, mật khẩu mới `Nv@99999` → Lưu<br>2. Đăng nhập bằng mật khẩu mới | Đăng nhập thành công bằng mật khẩu mới; mật khẩu cũ bị từ chối | P1 | FR-TK-05 | ☐ |
| TC-TK-10 | Sửa tài khoản bỏ trống tên hoặc mật khẩu | nv07 | 1. Xóa trắng tên đăng nhập → Lưu | `Tên đăng nhập và mật khẩu không được để trống!` | P2 | FR-TK-05 | ☐ |
| TC-TK-11 | Sửa tên đăng nhập trùng tài khoản khác | nv07, nv01 | 1. Đổi tên của nv07 thành `nv01` | `Tên đăng nhập đã tồn tại!` | P2 | FR-TK-05 | ☐ |
| TC-TK-12 | Đổi chức vụ rồi sửa tài khoản thì vai trò cập nhật theo | NV07 đổi `ChucVu` thành `Giao hàng` | 1. Mở sửa tài khoản NV07 → Lưu<br>2. Đăng nhập nv07 | `VaiTro = GiaoHang`; về `/giaohang-home` | P3 | FR-TK-07 | ☐ |
| TC-TK-13 | Xóa tài khoản | Tài khoản nv07 | 1. `?action=delete&id=NV07`<br>2. Thử đăng nhập nv07 | Tài khoản bị xóa; đăng nhập thất bại; nhân viên NV07 vẫn còn | P2 | FR-TK-06 | ☐ |

## 10. Quản lý sản phẩm (SP)

Yêu cầu liên quan: FR-SP-01…06

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-SP-01 | Quản lý xem danh sách sản phẩm | admin01 | 1. Mở `/sanpham` | Hiển thị danh sách kèm ảnh, đơn giá, tồn | P2 | FR-SP-01 | ☐ |
| TC-SP-02 | Thêm sản phẩm hợp lệ có ảnh PNG | admin01; tệp `latte.png` < 5MB | 1. `?action=add`<br>2. Tên `Latte đá`, loại `Cafe`, size, đơn giá 45000, tồn 100, mô tả, thành phần, chọn ảnh → Lưu | Quay về danh sách; sản phẩm có mã `SPnn` tự sinh; ảnh hiển thị qua `/uploads/…` | P1 | FR-SP-02, 04 | ☐ |
| TC-SP-03 | Để trống mã sản phẩm → tự sinh | Quản lý | 1. Xóa ô mã → Lưu (dữ liệu khác hợp lệ) | Mã sinh tiếp theo, không lỗi | P2 | FR-SP-02 | ☐ |
| TC-SP-04 | Mã sản phẩm trùng → hệ thống cấp mã mới | Đã có SP01 | 1. Nhập mã `SP01` → Lưu | Không ghi đè SP01; sản phẩm mới nhận mã kế tiếp | P2 | FR-SP-02 | ☐ |
| TC-SP-05 | Tên sản phẩm trống | Quản lý | 1. Bỏ trống tên → Lưu | `Tên sản phẩm không được để trống!`; giữ lại các giá trị đã nhập | P1 | FR-SP-03 | ☐ |
| TC-SP-06 | Đơn giá âm | Quản lý | 1. Đơn giá `-1` | `Đơn giá không được nhỏ hơn 0!` | P1 | FR-SP-03 | ☐ |
| TC-SP-07 | Số lượng tồn âm | Quản lý | 1. Tồn `-5` | `Số lượng tồn không được nhỏ hơn 0!` | P1 | FR-SP-03 | ☐ |
| TC-SP-08 | Số lượng tồn không phải số nguyên | Quản lý | 1. Tồn `1.5` hoặc `abc` | `Số lượng tồn phải là số nguyên!` | P2 | FR-SP-03 | ☐ |
| TC-SP-09 | Đơn giá và tồn để trống | Quản lý | 1. Bỏ trống cả hai → Lưu | Lưu với đơn giá 0 và tồn 0 | P3 | FR-SP-03 | ☐ |
| TC-SP-10 | Đơn giá bằng 0 (biên) | Quản lý | 1. Đơn giá `0` | Lưu được | P3 | FR-SP-03 | ☐ |
| TC-SP-11 | Định dạng ảnh không hỗ trợ | Quản lý; tệp `.bmp`, `.pdf`, `.exe` | 1. Chọn từng tệp → Lưu | `Ảnh sản phẩm chỉ hỗ trợ PNG, JPG, JPEG, WEBP hoặc GIF.`; không lưu sản phẩm | P1 | FR-SP-04 | ☐ |
| TC-SP-12 | Đuôi ảnh viết hoa `.JPG` | Quản lý | 1. Chọn `anh.JPG` → Lưu | Chấp nhận (đuôi được chuẩn hóa chữ thường) | P3 | FR-SP-04 | ☐ |
| TC-SP-13 | Ảnh vượt giới hạn 5MB | Tệp 6MB | 1. Chọn ảnh 6MB → Lưu | Bị từ chối bởi giới hạn tải lên; sản phẩm không được tạo; hệ thống không sập | P2 | FR-SP-04 | ☐ |
| TC-SP-14 | Sửa sản phẩm không chọn ảnh mới | SP có ảnh | 1. Sửa giá, không chọn ảnh → Lưu | Giá cập nhật; ảnh cũ giữ nguyên | P2 | FR-SP-02 | ☐ |
| TC-SP-15 | Sửa sản phẩm với dữ liệu sai | SP bất kỳ | 1. Sửa đơn giá `-5` / tên trống / tồn `x` | Hiện đúng thông báo như khi thêm | P2 | FR-SP-03 | ☐ |
| TC-SP-16 | Xóa sản phẩm chưa có lịch sử | SP mới tạo, chưa có giao dịch | 1. `?action=delete&id=…` | Sản phẩm bị xóa | P2 | FR-SP-05 | ☐ |
| TC-SP-17 | Xóa sản phẩm đã có lịch sử bán / chi tiết đơn / lịch sử giá | SP đã bán | 1. Xóa sản phẩm đó | `Không thể xóa sản phẩm đã có lịch sử bán, chi tiết đơn hàng hoặc lịch sử giá.`; dữ liệu giữ nguyên | P1 | FR-SP-05 | ☐ |
| TC-SP-18 | Nhân viên chỉ được xem — mở form thêm | Đăng nhập nv01 | 1. GET `/sanpham?action=add` | Không hiển thị form; hiển thị danh sách kèm `Bạn chỉ có quyền xem danh sách sản phẩm.` | P1 | FR-SP-01 | ☐ |
| TC-SP-19 | Nhân viên không thể sửa/xóa/tạo qua yêu cầu trực tiếp | Đăng nhập nv01 | 1. GET `?action=edit&id=SP01`, `?action=delete&id=SP01`<br>2. POST create | Cả ba bị từ chối với thông báo trên; dữ liệu không đổi | P1 | FR-SP-01 | ☐ |
| TC-SP-20 | Cửa hàng: lọc theo loại | Có sản phẩm ở 4 loại | 1. Chọn lần lượt Bánh, Cafe, Trà sữa, Sinh tố | Chỉ hiện sản phẩm thuộc loại, tiêu đề loại cập nhật | P3 | FR-SP-06 | ☐ |
| TC-SP-21 | Cửa hàng: tìm kiếm và phân trang 10 sản phẩm/trang | ≥ 11 sản phẩm | 1. Mở cửa hàng, chuyển trang 2<br>2. Gõ từ khóa tên | Mỗi trang tối đa 10 sản phẩm; tìm kiếm lọc đúng | P3 | FR-SP-06 | ☐ |
| TC-SP-22 | Chống chèn script trong tên sản phẩm | Quản lý | 1. Tạo sản phẩm tên `<script>alert(1)</script>`<br>2. Xem danh sách và cửa hàng | Hiển thị nguyên văn như chữ, không chạy script | P1 | NFR-02 | ☐ |

## 11. Lịch sử giá sản phẩm (LSG)

Yêu cầu liên quan: FR-LSG-01…04

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-LSG-01 | Xem danh sách và tìm theo từ khóa | Có nhiều bản ghi | 1. Mở `/lichsugiasanpham`<br>2. Nhập từ khóa mã/tên SP | Danh sách lọc đúng | P3 | FR-LSG-01 | ☐ |
| TC-LSG-02 | Thêm lịch sử giá hợp lệ | SP01 tồn tại | 1. Mã SP `SP01`, giá cũ 40000, giá mới 45000, ngày áp dụng, lý do → Lưu | Bản ghi mới xuất hiện | P1 | FR-LSG-01 | ☐ |
| TC-LSG-03 | Mã sản phẩm trống | — | 1. Bỏ trống mã SP | `Mã sản phẩm không được để trống.` | P1 | FR-LSG-02 | ☐ |
| TC-LSG-04 | Mã sản phẩm không tồn tại | — | 1. Mã `SP999` | `Mã sản phẩm không tồn tại trong danh sách sản phẩm.` | P1 | FR-LSG-02 | ☐ |
| TC-LSG-05 | Giá cũ âm | — | 1. Giá cũ `-1` | `Giá cũ không được nhỏ hơn 0.` | P2 | FR-LSG-03 | ☐ |
| TC-LSG-06 | Giá mới âm | — | 1. Giá mới `-1` | `Giá mới không được nhỏ hơn 0.` | P2 | FR-LSG-03 | ☐ |
| TC-LSG-07 | Giá không phải số | — | 1. Giá cũ `abc` | `Giá cũ và giá mới phải là số hợp lệ.` | P2 | FR-LSG-03 | ☐ |
| TC-LSG-08 | Ngày áp dụng để trống | — | 1. Bỏ trống ngày | Lưu với thời điểm hiện tại | P3 | FR-LSG-04 | ☐ |
| TC-LSG-09 | Ngày áp dụng sai định dạng | — | 1. POST ngày `không-phải-ngày` | `Ngày áp dụng không đúng định dạng.` | P3 | FR-LSG-04 | ☐ |
| TC-LSG-10 | Sửa và xóa bản ghi | Có bản ghi | 1. Sửa lý do → Lưu<br>2. Xóa bản ghi | Cập nhật và xóa thành công; danh sách phản ánh đúng | P2 | FR-LSG-01 | ☐ |

## 12. Quản lý khuyến mãi (KM)

Yêu cầu liên quan: FR-KM-01…09

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-KM-01 | Quản lý xem danh sách khuyến mãi | admin01 | 1. Mở `/khuyenmai` | Hiển thị mọi khuyến mãi và nút thêm/sửa/xóa | P2 | FR-KM-01 | ☐ |
| TC-KM-02 | Nhân viên chỉ xem, không thao tác | nv01 | 1. Mở `/khuyenmai`<br>2. GET `?action=add`, `?action=edit&id=…`, `?action=delete&id=…`; POST create | Xem được danh sách; mọi thao tác ghi bị chuyển về `/khuyenmai`, không thay đổi dữ liệu | P1 | FR-KM-01 | ☐ |
| TC-KM-03 | Thêm khuyến mãi phần trăm hợp lệ | admin01 | 1. Tên `Giảm 10%`, kiểu PERCENT, 10, ngày hôm nay → +30 ngày → Lưu | Mã tự sinh dài 6 ký tự, bắt đầu `VC`, 4 ký tự còn lại thuộc `A–Z0–9`; hiển thị trong danh sách | P1 | FR-KM-02, 06 | ☐ |
| TC-KM-04 | Thêm khuyến mãi theo số tiền hợp lệ | admin01 | 1. Kiểu AMOUNT, số tiền 15000 → Lưu | Lưu; `PhanTramGiam` và `MucGiamToiDa` null | P1 | FR-KM-05 | ☐ |
| TC-KM-05 | Tên khuyến mãi trống | — | 1. Bỏ trống tên | `Tên khuyến mãi không được để trống!`; giữ lại dữ liệu đã nhập | P1 | FR-KM-03 | ☐ |
| TC-KM-06 | Không chọn kiểu giảm giá | — | 1. Gửi form không có kiểu | `Vui lòng chọn kiểu giảm giá.` | P2 | FR-KM-04 | ☐ |
| TC-KM-07 | Số tiền giảm bằng 0 hoặc âm | — | 1. AMOUNT với `0`, rồi `-1000` | `Số tiền giảm phải lớn hơn 0.` | P1 | FR-KM-05 | ☐ |
| TC-KM-08 | Phần trăm bằng 0 | — | 1. PERCENT `0` | `Phần trăm giảm phải lớn hơn 0 và nhỏ hơn 100.` | P1 | FR-KM-06 | ☐ |
| TC-KM-09 | Phần trăm bằng 100 (biên trên) | — | 1. PERCENT `100` | Cùng thông báo lỗi | P1 | FR-KM-06 | ☐ |
| TC-KM-10 | Phần trăm âm | — | 1. PERCENT `-5` | Cùng thông báo lỗi | P2 | FR-KM-06 | ☐ |
| TC-KM-11 | Phần trăm 0.01 và 99.99 (biên hợp lệ) | — | 1. Tạo hai khuyến mãi với 0.01 và 99.99 | Cả hai lưu thành công | P2 | FR-KM-06 | ☐ |
| TC-KM-12 | Phần trăm không phải số | — | 1. PERCENT `abc` | `Giá trị giảm không hợp lệ!` | P2 | FR-KM-09 | ☐ |
| TC-KM-13 | Mức giảm tối đa tùy chọn | — | 1. PERCENT 50, mức tối đa 20000<br>2. PERCENT 50, bỏ trống mức tối đa | Cả hai lưu được; trường hợp bỏ trống `MucGiamToiDa = null` | P2 | FR-KM-06 | ☐ |
| TC-KM-14 | Ngày kết thúc trước ngày bắt đầu | — | 1. Bắt đầu 10/12, kết thúc 09/12 | `Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!` | P1 | FR-KM-07 | ☐ |
| TC-KM-15 | Ngày kết thúc bằng ngày bắt đầu (biên) | — | 1. Bắt đầu = kết thúc | Lưu thành công | P2 | FR-KM-07 | ☐ |
| TC-KM-16 | Không nhập ngày bắt đầu/kết thúc | — | 1. Bỏ trống cả hai ngày | Lưu thành công; voucher không giới hạn thời gian | P3 | FR-KM-07 | ☐ |
| TC-KM-17 | Sửa khuyến mãi từ PERCENT sang AMOUNT | Voucher PERCENT 10 có mức tối đa | 1. Sửa sang AMOUNT 20000 → Lưu | `SoTienGiam = 20000`; `PhanTramGiam` và `MucGiamToiDa` bị xóa | P2 | FR-KM-05 | ☐ |
| TC-KM-18 | Xóa khuyến mãi chưa dùng | Voucher mới | 1. Xóa | Voucher bị xóa khỏi danh sách | P2 | FR-KM-01 | ☐ |
| TC-KM-19 | Xóa khuyến mãi đã gắn vào hóa đơn | Voucher đã được dùng ở 1 hóa đơn | 1. Xóa voucher đó | Ghi nhận hành vi thực tế: hoặc thông báo `Không thể xóa khuyến mãi: …` giữ nguyên dữ liệu, hoặc xóa an toàn; hóa đơn cũ không hỏng | P2 | FR-KM-01 | ☐ |
| TC-KM-20 | Hiệu lực — hôm nay là ngày kết thúc (biên) | Voucher `VCEDGE` kết thúc hôm nay | 1. Mở cửa hàng | Voucher vẫn hiển thị và áp dụng được | P1 | FR-KM-08 | ☐ |
| TC-KM-21 | Hiệu lực — đã hết hạn hôm qua | Voucher `VCEXP1` | 1. Mở cửa hàng<br>2. Nhập mã bằng tay | Không có trong danh sách; nhập mã bị từ chối `Mã giảm giá không hợp lệ hoặc đã hết hạn.` | P1 | FR-KM-08 | ☐ |
| TC-KM-22 | Hiệu lực — chưa đến ngày bắt đầu | Voucher `VCFUT1` bắt đầu ngày mai | 1. Mở cửa hàng | Không hiển thị và không áp dụng được | P1 | FR-KM-08 | ☐ |
| TC-KM-23 | Hiệu lực — hôm nay là ngày bắt đầu (biên) | Voucher bắt đầu hôm nay | 1. Mở cửa hàng | Hiển thị và áp dụng được | P2 | FR-KM-08 | ☐ |
| TC-KM-24 | Mã voucher không trùng khi tạo hàng loạt | — | 1. Tạo liên tiếp 30 khuyến mãi | 30 mã đều khác nhau, đều 6 ký tự, tiền tố `VC` | P3 | FR-KM-02 | ☐ |

## 13. Cửa hàng khách và giỏ hàng (SHOP)

Yêu cầu liên quan: FR-SHOP-01…06

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-SHOP-01 | Khách vãng lai xem cửa hàng | Chưa đăng nhập | 1. Mở `/customer-shop` | Thấy sản phẩm, banner, danh sách voucher còn hiệu lực | P1 | FR-SHOP-01 | ☐ |
| TC-SHOP-02 | Thêm sản phẩm vào giỏ với size M | Đang ở cửa hàng | 1. Nhấn thêm sản phẩm<br>2. Chọn `Size M`<br>3. Mở giỏ | Giỏ có 1 dòng `<Tên> - Size M`, số lượng 1, đơn giá của sản phẩm | P1 | FR-SHOP-02 | ☐ |
| TC-SHOP-03 | Cùng sản phẩm, cùng size → tăng số lượng | Giỏ có `SPxx-M` | 1. Thêm lại cùng sản phẩm, chọn `M` | Vẫn 1 dòng, số lượng 2 | P1 | FR-SHOP-02 | ☐ |
| TC-SHOP-04 | Cùng sản phẩm, khác size → dòng riêng | Giỏ có `SPxx-M` | 1. Thêm cùng sản phẩm, chọn `L` | Có 2 dòng riêng (`M`, `L`) | P1 | FR-SHOP-02 | ☐ |
| TC-SHOP-05 | Hủy chọn size | Hộp chọn size đang mở | 1. Nhấn Hủy | Hộp đóng, giỏ không đổi | P3 | FR-SHOP-02 | ☐ |
| TC-SHOP-06 | Tăng/giảm/xóa dòng trong giỏ | Giỏ có 2 dòng | 1. Tăng, giảm số lượng, xóa một dòng | Tổng tiền cập nhật đúng sau mỗi thao tác; giảm về 0 loại dòng đó | P1 | FR-SHOP-03 | ☐ |
| TC-SHOP-07 | Giỏ còn sau khi tải lại trang | Giỏ có hàng | 1. F5 | Giỏ giữ nguyên (localStorage) | P2 | FR-SHOP-03 | ☐ |
| TC-SHOP-08 | Gộp giỏ vãng lai vào giỏ tài khoản khi đăng nhập | Giỏ vãng lai có 1 dòng; tài khoản KH01 đã có giỏ riêng | 1. Đăng nhập KH01 | Hai giỏ được gộp; dòng trùng (cùng mã + size) cộng dồn số lượng | P2 | FR-SHOP-03 | ☐ |
| TC-SHOP-09 | Áp mã giảm giá hợp lệ từ danh sách | Có voucher 10%; giỏ 100000 | 1. Chọn voucher | Hiển thị giảm 10000 và tổng 90000 | P1 | FR-SHOP-04 | ☐ |
| TC-SHOP-10 | Nhập mã giảm giá sai | — | 1. Gõ mã `ZZZZZZ` | `Mã giảm giá không hợp lệ hoặc đã hết hạn.`; tổng không đổi | P1 | FR-SHOP-04 | ☐ |
| TC-SHOP-11 | Bỏ mã giảm giá | Đã áp mã | 1. Xóa mã/chọn rỗng | Tổng trở lại giá gốc | P2 | FR-SHOP-04 | ☐ |
| TC-SHOP-12 | Mã giảm giá được ghi nhớ khi tải lại | Đã áp mã | 1. F5 | Mã vẫn được chọn | P3 | FR-SHOP-04 | ☐ |
| TC-SHOP-13 | Không có voucher khả dụng | Mọi voucher hết hạn | 1. Mở giỏ | Ô chọn bị vô hiệu; thông báo `Hiện chưa có mã giảm giá khả dụng.` | P3 | FR-SHOP-04 | ☐ |
| TC-SHOP-14 | Chọn Giao hàng nhưng không nhập địa chỉ | Giỏ có hàng | 1. Chọn Giao hàng, bỏ trống địa chỉ, nhấn Thanh toán | Hộp thoại `Vui lòng nhập địa chỉ giao hàng.`; không tạo phiên | P1 | FR-SHOP-05 | ☐ |
| TC-SHOP-15 | Chọn Giao hàng với địa chỉ hợp lệ | Giỏ có hàng | 1. Chọn Giao hàng, nhập địa chỉ, thanh toán | Tạo phiên thanh toán kèm phương thức và địa chỉ; lựa chọn được ghi nhớ | P1 | FR-SHOP-05 | ☐ |
| TC-SHOP-16 | Chọn Tại quán không cần địa chỉ | Giỏ có hàng | 1. Chọn Tại quán, thanh toán | Tạo phiên bình thường; địa chỉ không bắt buộc | P1 | FR-SHOP-05 | ☐ |
| TC-SHOP-17 | Thanh toán khi giỏ trống | Giỏ rỗng | 1. Nhấn Thanh toán | Không tạo phiên (nút bị chặn hoặc thông báo `Giỏ hàng trống…`) | P2 | FR-PAY-02 | ☐ |
| TC-SHOP-18 | Nhân viên mở cửa hàng khách | Đăng nhập nv01 | 1. GET `/customer-shop` | Chuyển về `/nhanvien-home` | P2 | FR-SHOP-01 | ☐ |
| TC-SHOP-19 | Khách cập nhật hồ sơ | Đăng nhập KH01 | 1. Sửa địa chỉ trong hồ sơ trên cửa hàng → Lưu | Hồ sơ cập nhật; đơn giao hàng sau đó dùng địa chỉ đã nhập ở bước thanh toán | P3 | FR-SHOP-06 | ☐ |
| TC-SHOP-20 | Khách tự xóa hồ sơ | Khách `KH90` chưa có đơn | 1. Dùng chức năng xóa hồ sơ trên cửa hàng | Khách và tài khoản bị xóa, đăng xuất. Nếu đã có đơn/hóa đơn: hiển thị `Không thể xóa khách hàng vì còn lịch sử đơn hàng hoặc hóa đơn.` Ghi nhận nếu bộ lọc quyền chặn thao tác này | P2 | FR-SHOP-06, KH-04 | ☐ |
| TC-SHOP-21 | Xem chi tiết sản phẩm | — | 1. Nhấn xem chi tiết một sản phẩm | Hiện mã, loại, size, thành phần, mô tả, giá, ảnh | P3 | FR-SP-06 | ☐ |

## 14. Phiên thanh toán (PAY)

Yêu cầu liên quan: FR-PAY-01…07

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-PAY-01 | Tạo phiên: tại quán, không voucher | Đăng nhập KH01; giỏ 2 dòng tổng 100000 | 1. POST `/payment-session` `action=create`, `cartJson` hợp lệ, `deliveryMethod=TAI_QUAN` | JSON: `sessionId` dạng `PAY-` + 12 ký tự hex, `status` đang chờ, số tiền 100000, `expiresAt` ≈ +10 phút | P1 | FR-PAY-01, 04 | ☐ |
| TC-PAY-02 | Hiển thị QR và nội dung chuyển khoản | Đã tạo phiên | 1. Mở cửa sổ thanh toán | Có ảnh QR VietQR đúng ngân hàng/số tài khoản/số tiền, nội dung chứa mã phiên, đồng hồ đếm ngược, nút sao chép hoạt động | P1 | FR-PAY-05 | ☐ |
| TC-PAY-03 | Giỏ trống (thiếu cartJson) | Đăng nhập KH01 | 1. POST create không có `cartJson` | HTTP 400 `Giỏ hàng trống, không thể tạo đơn.` | P1 | FR-PAY-02 | ☐ |
| TC-PAY-04 | Giỏ có tổng bằng 0 | Đăng nhập KH01 | 1. POST create với `cartJson` toàn dòng giá 0 / số lượng 0 | HTTP 400 `Giỏ hàng trống, không thể tạo đơn.` | P2 | FR-PAY-02 | ☐ |
| TC-PAY-05 | Chưa đăng nhập và không biết mã khách | Chưa đăng nhập | 1. POST create không có `maKH` | HTTP 401 `Vui lòng đăng nhập tài khoản khách hàng trước khi thanh toán.` | P1 | FR-PAY-02 | ☐ |
| TC-PAY-06 | Giao hàng thiếu địa chỉ | Đăng nhập KH01 | 1. POST create `deliveryMethod=GIAO_HANG`, không có địa chỉ | HTTP 400 `Vui lòng nhập địa chỉ giao hàng.` | P1 | FR-PAY-02 | ☐ |
| TC-PAY-07 | Giao hàng có địa chỉ | Đăng nhập KH01 | 1. POST create `GIAO_HANG` + địa chỉ | Phiên tạo thành công, JSON có `deliveryMethod = GIAO_HANG` và địa chỉ | P1 | FR-PAY-01 | ☐ |
| TC-PAY-08 | Voucher 10% — đơn 100.000 | Voucher `VCPCT1` | 1. Tạo phiên với `couponCode=VCPCT1` | Giảm 10.000; cần thanh toán 90.000 | P1 | FR-PAY-03 | ☐ |
| TC-PAY-09 | Voucher 50%, tối đa 20.000 — đơn 100.000 | Voucher `VCPCT2` | 1. Tạo phiên với `couponCode=VCPCT2` | Giảm bị chặn ở 20.000; thanh toán 80.000 | P1 | FR-PAY-03 | ☐ |
| TC-PAY-10 | Voucher 50%, tối đa 20.000 — đơn 30.000 | Voucher `VCPCT2` | 1. Tạo phiên | Giảm 15.000 (dưới mức tối đa); thanh toán 15.000 | P1 | FR-PAY-03 | ☐ |
| TC-PAY-11 | Voucher số tiền 15.000 — đơn 100.000 | Voucher `VCAMT1` | 1. Tạo phiên | Giảm 15.000; thanh toán 85.000 | P1 | FR-PAY-03 | ☐ |
| TC-PAY-12 | Voucher số tiền bằng đúng tổng đơn | `VCAMT1` 15.000, đơn 15.000 | 1. Tạo phiên | HTTP 400 `Mã giảm giá không hợp lệ cho đơn hàng này.` (tổng sau giảm ≤ 0) | P1 | FR-PAY-02 | ☐ |
| TC-PAY-13 | Voucher số tiền lớn hơn tổng đơn | `VCAMT1` 15.000, đơn 10.000 | 1. Tạo phiên | Giảm không vượt tổng đơn → tổng sau giảm 0 → HTTP 400 như trên | P1 | FR-PAY-03 | ☐ |
| TC-PAY-14 | Voucher hết hạn hoặc không tồn tại | `VCEXP1`; mã `XXXXXX` | 1. Tạo phiên với từng mã | HTTP 400 `Mã giảm giá không hợp lệ hoặc đã hết hạn.` | P1 | FR-PAY-02 | ☐ |
| TC-PAY-15 | Làm tròn giảm giá theo đồng | Voucher 15%, đơn 99.990 | 1. Tạo phiên | Số tiền giảm và số cần thanh toán đều là số nguyên đồng; `đơn − giảm = cần thanh toán` | P2 | FR-PAY-03 | ☐ |
| TC-PAY-16 | Tra cứu trạng thái phiên | Đã có phiên | 1. GET `/payment-session?id=<id>` | JSON có `sessionId`, `status`, `statusText`, số tiền, giảm giá, `expiresAt` | P1 | FR-PAY-06 | ☐ |
| TC-PAY-17 | Tra cứu thiếu mã phiên | — | 1. GET `/payment-session` | HTTP 400 `Thiếu mã phiên thanh toán.` | P2 | FR-PAY-06 | ☐ |
| TC-PAY-18 | Tra cứu phiên không tồn tại | — | 1. GET `?id=PAY-000000000000` | HTTP 404 `Không tìm thấy phiên thanh toán.` | P2 | FR-PAY-06 | ☐ |
| TC-PAY-19 | Hành động không hợp lệ | — | 1. POST `action=abc` | HTTP 400 `Hành động không hợp lệ.` | P3 | FR-PAY | ☐ |
| TC-PAY-20 | Phiên quá 10 phút không thanh toán | Đã tạo phiên, không chuyển tiền | 1. Chờ > 10 phút<br>2. GET trạng thái | Trạng thái thất bại `Thanh toán thất bại`; không có đơn hàng nào được tạo; nút hiển thị `Phiên hết hạn` | P1 | FR-PAY-04 | ☐ |
| TC-PAY-21 | Phiên bị dọn sau 15 phút | Phiên đã hết hạn | 1. Chờ > 15 phút<br>2. GET trạng thái | HTTP 404 (phiên đã bị xóa) | P3 | FR-PAY-04 | ☐ |
| TC-PAY-22 | Mất phiên khi khởi động lại ứng dụng | Phiên đang chờ | 1. Khởi động lại Tomcat<br>2. GET trạng thái | 404; ghi nhận hạn chế phiên lưu trong bộ nhớ | P3 | NFR-08 | ☐ |

## 15. Webhook SePay (IPN)

Yêu cầu liên quan: Mục 6.3 đặc tả

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-IPN-01 | Webhook JSON hợp lệ hoàn tất phiên | Phiên `PAY-A…` đang chờ 90.000; cấu hình số tài khoản khớp; xác thực hợp lệ | 1. POST `/sepay-ipn` JSON: tiền vào, đúng số tài khoản, `transferAmount=90000`, `content` chứa `PAY-A…` | HTTP 200 `success`; phiên chuyển hoàn tất; đơn được lưu (xem ORD) | P1 | FR-PAY-07, 6.3 | ☐ |
| TC-IPN-02 | Webhook dạng form-urlencoded | Như trên | 1. Gửi cùng dữ liệu dạng `application/x-www-form-urlencoded` | Xử lý giống JSON | P2 | 6.3 | ☐ |
| TC-IPN-03 | Webhook dạng multipart/form-data | Như trên | 1. Gửi dạng multipart | Xử lý giống JSON | P3 | 6.3 | ☐ |
| TC-IPN-04 | Chữ ký HMAC đúng | Đã cấu hình secret | 1. Gửi kèm `X-SePay-Timestamp` và `X-SePay-Signature = sha256=` + HMAC-SHA256(`timestamp.rawBody`) | Được chấp nhận và xử lý | P1 | 6.3 | ☐ |
| TC-IPN-05 | Chữ ký HMAC sai | Đã cấu hình secret | 1. Gửi chữ ký sai | HTTP 401 `Unauthorized`; phiên không đổi | P1 | 6.3 | ☐ |
| TC-IPN-06 | Có timestamp nhưng thiếu chữ ký | — | 1. Chỉ gửi `X-SePay-Timestamp` | HTTP 401 | P2 | 6.3 | ☐ |
| TC-IPN-07 | API key đúng | `sepay.apiKey` đã cấu hình | 1. Gửi `Authorization: Apikey <key>` | Được chấp nhận | P1 | 6.3 | ☐ |
| TC-IPN-08 | API key sai | — | 1. Gửi `Authorization: Apikey sai` | HTTP 401 | P1 | 6.3 | ☐ |
| TC-IPN-09 | ⚠ R-03 — Không có header xác thực | Phiên đang chờ | 1. Gửi webhook hợp lệ **không** kèm bất kỳ header xác thực | Mong đợi (NFR-04): HTTP 401, phiên không đổi. Mã hiện tại chấp nhận → ghi nhận lỗi bảo mật Cao | P1 | NFR-04, R-03 | ☐ |
| TC-IPN-10 | Thân yêu cầu rỗng | — | 1. POST thân rỗng | HTTP 400 `Invalid payload` | P2 | 6.3 | ☐ |
| TC-IPN-11 | Cùng giao dịch gửi hai lần (chống trùng) | Phiên đang chờ | 1. Gửi webhook giống hệt 2 lần liên tiếp | Cả hai HTTP 200; chỉ **một** đơn được tạo | P1 | 6.3 | ☐ |
| TC-IPN-12 | Giao dịch tiền ra bị bỏ qua | Phiên đang chờ | 1. Gửi `transferType = out` | HTTP 200; phiên vẫn đang chờ | P2 | 6.3 | ☐ |
| TC-IPN-13 | Sai số tài khoản nhận tiền | — | 1. Gửi với `accountNumber` khác cấu hình | HTTP 200; phiên không đổi | P1 | 6.3 | ☐ |
| TC-IPN-14 | Số tiền bằng 0 hoặc âm | — | 1. Gửi `transferAmount = 0` rồi `-1000` | HTTP 200; bỏ qua | P2 | 6.3 | ☐ |
| TC-IPN-15 | Ghép phiên theo mã trong nội dung chuyển khoản | 2 phiên đang chờ cùng 90.000 (`PAY-A`, `PAY-B`) | 1. Webhook với nội dung chứa `PAY-A` | Chỉ phiên A hoàn tất, B vẫn chờ | P1 | 6.3 | ☐ |
| TC-IPN-16 | ⚠ R-06 — Không có mã phiên, ghép theo số tiền | 2 phiên đang chờ cùng 90.000 của hai khách khác nhau | 1. Webhook nội dung không có mã phiên | Hiện tại hoàn tất phiên **mới nhất** cùng số tiền (có thể là của khách khác). Ghi nhận rủi ro; mong đợi theo đặc tả an toàn: không ghép bừa | P1 | R-06 | ☐ |
| TC-IPN-17 | Mã phiên đúng nhưng số tiền lệch | Phiên A 90.000 | 1. Webhook chứa `PAY-A` với số tiền 80.000, không có phiên nào 80.000 | Không hoàn tất phiên A; HTTP 200 | P1 | 6.3 | ☐ |
| TC-IPN-18 | Chuyển tiền sau khi phiên đã hết hạn | Phiên quá 10 phút | 1. Gửi webhook đúng mã/số tiền | Phiên không chuyển thành công; **không** tạo đơn; ghi nhận tình huống khách đã chuyển tiền nhưng không có đơn (cần đối soát/hoàn tiền thủ công) | P1 | FR-PAY-04 | ☐ |
| TC-IPN-19 | GET kiểm tra webhook | — | 1. GET `/sepay-ipn` | HTTP 200 văn bản `SePay webhook is ready` kèm payload gần nhất | P3 | 6.3 | ☐ |
| TC-IPN-20 | Đường dẫn bí danh `/sepay-webhook` | Phiên đang chờ | 1. Gửi webhook tới `/sepay-webhook` | Xử lý giống `/sepay-ipn` | P3 | 6.3 | ☐ |
| TC-IPN-21 | Gọi webhook không cần đăng nhập | Chưa có session | 1. POST từ client không cookie | Không bị chuyển về `/login` (đường dẫn công khai) | P1 | 3.2 | ☐ |

## 16. Lưu đơn sau thanh toán (ORD)

Yêu cầu liên quan: FR-ORD-01…05

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-ORD-01 | Lưu đơn tại quán thành công | Phiên đang chờ 2 dòng hàng, KH01, không voucher | 1. Hoàn tất phiên qua webhook<br>2. Kiểm tra CSDL | Tạo 1 `GioHang`, 1 `DatHang` (`CHO_NHAN`, `MaNV` rỗng, địa chỉ rỗng, `DaNhanGiaoHang = 0`), 1 `HoaDon` (`CHO_NHAN`), 2 `ChiTietGioHang`, 2 `ChiTietHoaDon`, 1 `DoanhThu` (`ChiPhi = 0`) | P1 | FR-ORD-01, 02 | ☐ |
| TC-ORD-02 | Số tiền khớp giữa các bảng | Đơn 2 dòng: 45000×2 và 30000×1, không voucher | 1. Kiểm tra số tiền các bảng | `CTHĐ.ThanhTien = giá × SL`; `HĐ.TongTien = 120000`; `GiamGia = 0`; `HĐ.ThanhTien = 120000`; `ĐH.TongTien = 120000`; `DoanhThu = 120000` | P1 | FR-ORD-05 | ☐ |
| TC-ORD-03 | Đơn giao hàng lưu địa chỉ | Phiên `GIAO_HANG` địa chỉ `12 Lê Lợi, Q1` | 1. Hoàn tất phiên | `DatHang` và `HoaDon` đều có `DiaChiGiaoHang` đúng; khi không nhập địa chỉ ở bước thanh toán thì dùng địa chỉ hồ sơ khách | P1 | FR-ORD-03 | ☐ |
| TC-ORD-04 | Đơn tại quán không lưu địa chỉ giao hàng | Phiên `TAI_QUAN`, hồ sơ khách có địa chỉ | 1. Hoàn tất phiên | `DiaChiGiaoHang` của đơn và hóa đơn là rỗng/null | P1 | FR-ORD-03 | ☐ |
| TC-ORD-05 | Số điện thoại lấy từ hồ sơ khách | KH01 SĐT 0911111111 | 1. Hoàn tất phiên | `SoDienThoai` của `DatHang` và `HoaDon` = `0911111111` | P2 | FR-ORD-03 | ☐ |
| TC-ORD-06 | Quy tắc sinh mã | CSDL đã có GH/DH/HĐ/DT | 1. Tạo 2 đơn liên tiếp | Mã `GHnn`, `DHnn`, `DTnn` tăng dần; mã hóa đơn 4 chữ số tăng dần; không trùng | P2 | FR-ORD-01, 6.1 | ☐ |
| TC-ORD-07 | Voucher % được lưu đúng | Voucher 10%, đơn 100.000 | 1. Hoàn tất phiên | `HoaDon.MaKM` = mã voucher; `GiamGia = 10000`; `ThanhTien = 90000`; `DoanhThu = 90000` | P1 | FR-ORD-05 | ☐ |
| TC-ORD-08 | ⚠ R-04 — Voucher số tiền được lưu đúng | Voucher `VCAMT1` 15.000, đơn 100.000 (khách thanh toán 85.000) | 1. Hoàn tất phiên<br>2. Xem `HoaDon.GiamGia`, `ThanhTien`, `DoanhThu` | Mong đợi: `GiamGia = 15000`, `ThanhTien = 85000`, `DoanhThu = 85000`. Mã hiện tại chỉ tính theo phần trăm nên có thể lưu `GiamGia = 0` và `ThanhTien = 100000` → lệch với số tiền khách trả (ghi nhận lỗi Cao) | P1 | FR-ORD-05, R-04 | ☐ |
| TC-ORD-09 | ⚠ R-04 — Voucher % có mức tối đa | Voucher `VCPCT2` 50% tối đa 20.000, đơn 100.000 (khách trả 80.000) | 1. Hoàn tất phiên<br>2. Kiểm tra hóa đơn | Mong đợi `GiamGia = 20000`, `ThanhTien = 80000`. Mã hiện tại có thể lưu giảm 50.000 (bỏ qua mức tối đa) → ghi nhận lỗi Cao nếu lệch | P1 | FR-ORD-05, R-04 | ☐ |
| TC-ORD-10 | Sản phẩm trong giỏ không tồn tại | `cartJson` chứa `maSP = SP999` | 1. Hoàn tất phiên | Giao dịch rollback; không có `GioHang/DatHang/HoaDon` mồ côi; phiên chuyển thất bại với lý do `Sản phẩm SP999 không tồn tại.` | P1 | FR-ORD-04 | ☐ |
| TC-ORD-11 | Size không tồn tại | `cartJson` có size `XL` | 1. Hoàn tất phiên | Rollback; lý do `Size XL không tồn tại.` | P1 | FR-ORD-04 | ☐ |
| TC-ORD-12 | Số lượng không hợp lệ | Dòng có số lượng 0 hoặc âm | 1. Hoàn tất phiên | Rollback hoặc dòng bị loại; không có đơn sai; thông báo `Số lượng sản phẩm không hợp lệ.` nếu bị từ chối | P2 | FR-ORD-04 | ☐ |
| TC-ORD-13 | Mã khách không tồn tại | Phiên có `maKH = KH999` | 1. Hoàn tất phiên | Rollback; lý do `Khách hàng không tồn tại.` | P2 | FR-ORD-04 | ☐ |
| TC-ORD-14 | Hoàn tất phiên hai lần chỉ tạo một đơn | Phiên đã hoàn tất | 1. Gọi lại hoàn tất (webhook trùng khác mã giao dịch hoặc `complete`) | Không tạo đơn thứ hai; số bản ghi `DatHang/HoaDon/DoanhThu` không đổi | P1 | FR-ORD-01 | ☐ |
| TC-ORD-15 | Hai webhook đồng thời cho một phiên | Phiên đang chờ | 1. Gửi 2 webhook khác mã giao dịch cùng lúc | Chỉ một đơn được tạo | P2 | FR-ORD-01, NFR-08 | ☐ |
| TC-ORD-16 | ⚠ R-05 — Tồn kho sau khi bán | SP01 tồn 100; đơn mua 3 | 1. Hoàn tất phiên<br>2. Xem `SoLuongTon` | Đặc tả không yêu cầu hiện hành; kiểm chứng và ghi nhận: tồn vẫn 100 (không bị trừ) → đề xuất bổ sung | P2 | R-05 | ☐ |

## 17. Quy trình xử lý đơn (WF)

Yêu cầu liên quan: FR-WF-01…07

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-WF-01 | Nhân viên nhận đơn | Đơn `DH01` tại quán `CHO_NHAN`; đăng nhập nv01 | 1. POST `/nhanvien-donhang` `action=claim&id=DH01` | `DatHang.MaNV = NV01`; trạng thái chuyển sang đang làm; hóa đơn tương ứng đồng bộ; thông báo `Đã nhận đơn DH01.` | P1 | FR-WF-01, 07 | ☐ |
| TC-WF-02 | Nhân viên thứ hai nhận đơn đã có người nhận | DH01 do NV01 nhận; đăng nhập nv02 | 1. claim DH01 | HTTP 409 `Đơn này đã được nhân viên khác nhận.` | P1 | FR-WF-01 | ☐ |
| TC-WF-03 | Quản lý được nhận/ghi đè đơn của nhân viên | DH01 do NV01 nhận; đăng nhập admin01 | 1. claim DH01 | Thành công, `MaNV` đổi sang mã của quản lý | P2 | FR-WF-01 | ☐ |
| TC-WF-04 | Hoàn thành đơn tại quán | DH01 tại quán do nv01 nhận | 1. `action=complete&id=DH01` | `DatHang` và `HoaDon` = `HOAN_THANH`; `DoanhThu` của hóa đơn tồn tại đúng 1 bản ghi; thông báo `Đã hoàn thành đơn DH01.` | P1 | FR-WF-03, DASH-04 | ☐ |
| TC-WF-05 | Nhân viên không được tự hoàn thành đơn giao hàng | Đơn giao hàng `DH02` | 1. nv01 `action=complete&id=DH02` | HTTP 409 `Đơn giao hàng phải do tài xế xác nhận hoàn tất.` | P1 | FR-WF-03 | ☐ |
| TC-WF-06 | Phân công tài xế cho đơn tại quán bị từ chối | Đơn tại quán | 1. `action=assign-delivery&id=DH01&deliveryMaNV=NV03` | HTTP 409 `Chỉ các đơn giao hàng mới được phân công cho tài xế.` | P1 | FR-WF-02 | ☐ |
| TC-WF-07 | Phân công khi chưa chọn tài xế | Đơn giao hàng đang chuẩn bị | 1. assign-delivery không có `deliveryMaNV` | HTTP 400 `Vui lòng chọn người giao hàng.` | P1 | FR-WF-02 | ☐ |
| TC-WF-08 | Phân công đơn không còn ở trạng thái chuẩn bị | Đơn đã `DANG_GIAO` | 1. assign-delivery lần nữa | HTTP 409 `Chỉ được chuyển đơn sau khi đã làm xong.` | P2 | FR-WF-02 | ☐ |
| TC-WF-09 | Phân công tài xế hợp lệ | Đơn giao hàng `CHO_NHAN`; nhân viên đã nhận | 1. `assign-delivery&id=DH02&deliveryMaNV=NV03` | `MaNVGiaoHang = NV03`; trạng thái `DANG_GIAO`; `DaNhanGiaoHang = 0`; hóa đơn đồng bộ; thông báo `Đã xác nhận chuyển đơn DH02 cho người giao hàng.` | P1 | FR-WF-02 | ☐ |
| TC-WF-10 | Tài xế khác thao tác đơn không phải của mình | DH02 giao cho gh01; đăng nhập gh02 | 1. claim DH02 | HTTP 409 `Đơn này không được giao cho bạn.` | P1 | FR-WF-04 | ☐ |
| TC-WF-11 | Tài xế nhận đơn khi đơn chưa sẵn sàng | Đơn giao cho gh01 nhưng trạng thái chưa `DANG_GIAO` | 1. gh01 claim | HTTP 409 `Đơn này chưa sẵn sàng để nhận.` | P2 | FR-WF-04 | ☐ |
| TC-WF-12 | Tài xế nhận đơn hợp lệ | DH02 `DANG_GIAO` giao cho gh01 | 1. gh01 `action=claim&id=DH02` | `DaNhanGiaoHang = 1`; thông báo `Đã nhận đơn DH02.` | P1 | FR-WF-04 | ☐ |
| TC-WF-13 | Tài xế hoàn thành khi chưa nhận đơn | DH02 `DaNhanGiaoHang = 0` | 1. gh01 `action=complete` | HTTP 409 `Bạn cần nhận đơn trước khi xác nhận hoàn tất.` | P1 | FR-WF-05 | ☐ |
| TC-WF-14 | Tài xế hoàn thành đơn đã nhận | DH02 đã claim | 1. gh01 `action=complete` | `DatHang` và `HoaDon` = `HOAN_THANH`; doanh thu đúng 1 bản ghi; thông báo `Đã xác nhận giao xong đơn DH02.` | P1 | FR-WF-05, 07 | ☐ |
| TC-WF-15 | Tài xế dùng hành động ngoài quyền | gh01 có đơn DH02 | 1. gh01 `action=assign-delivery` | HTTP 400 `Tài xế chỉ được nhận đơn hoặc xác nhận đã giao.` | P2 | FR-WF-05 | ☐ |
| TC-WF-16 | Thao tác trên đơn đã hoàn thành | DH01 `HOAN_THANH` | 1. claim hoặc complete DH01 | HTTP 409 `Đơn hàng này đã được xử lý.` | P1 | FR-WF-06 | ☐ |
| TC-WF-17 | Mã đơn không tồn tại | — | 1. claim `DH999` | HTTP 404 `Không tìm thấy đơn hàng.` | P2 | FR-WF-06 | ☐ |
| TC-WF-18 | Hành động không hợp lệ | — | 1. `action=abc` | HTTP 400 `Hành động không hợp lệ.` | P2 | FR-WF-06 | ☐ |
| TC-WF-19 | Tài khoản không có mã nhân viên | Tài khoản quản lý/nhân viên không liên kết `MaNV` | 1. claim bất kỳ đơn | HTTP 400 `Tài khoản chưa có mã nhân viên.` | P2 | FR-WF-06 | ☐ |
| TC-WF-20 | Bí danh `update-status`, `assign` | Đơn hợp lệ | 1. Gọi `action=update-status`; gọi `action=assign` | Hành xử tương ứng `complete` và `claim` | P3 | FR-WF-01, 03 | ☐ |
| TC-WF-21 | Tổng quan đơn `?action=summary` | Có đơn các trạng thái | 1. GET `/nhanvien-donhang?action=summary` | JSON tổng quan đơn đúng với dữ liệu | P3 | FR-WF | ☐ |
| TC-WF-22 | Hai nhân viên nhận cùng một đơn đồng thời | Đơn `CHO_NHAN` | 1. nv01 và nv02 gửi claim cùng lúc | Chỉ một người được gán; người còn lại nhận 409 hoặc dữ liệu cuối nhất quán | P2 | FR-WF-01 | ☐ |
| TC-WF-23 | Khách gọi API xử lý đơn | Đăng nhập KH01 | 1. POST `/nhanvien-donhang` claim | Bị chuyển hướng/từ chối, đơn không đổi | P1 | 3.2 | ☐ |

## 18. Hóa đơn (HD)

Yêu cầu liên quan: FR-HD-01…05

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-HD-01 | Xem danh sách hóa đơn theo vai trò | Có hóa đơn | 1. Mở `/hoadon` với admin01, nv01, gh01 | Cả ba xem được danh sách; khách hàng bị chặn | P2 | FR-HD-01 | ☐ |
| TC-HD-02 | Thêm hóa đơn thủ công hợp lệ | admin01; KH01, DH hợp lệ, voucher hợp lệ, NV01 | 1. `?action=add` điền đủ → Lưu | Hóa đơn mới xuất hiện với mã 4 chữ số tăng dần | P2 | FR-HD-01, 04 | ☐ |
| TC-HD-03 | Mã khách không tồn tại hoặc trống | — | 1. Nhập `KH999` / để trống | `Mã khách hàng không tồn tại hoặc không được để trống!` | P2 | FR-HD-02 | ☐ |
| TC-HD-04 | Mã đặt hàng không tồn tại | — | 1. Nhập `DH999` | `Mã đặt hàng không tồn tại!` | P2 | FR-HD-02 | ☐ |
| TC-HD-05 | Mã khuyến mãi không tồn tại | — | 1. Nhập `VC0000` | `Mã khuyến mãi không tồn tại!` | P2 | FR-HD-02 | ☐ |
| TC-HD-06 | Mã nhân viên trống | — | 1. Bỏ trống mã nhân viên | `Mã nhân viên không được để trống!` | P2 | FR-HD-02 | ☐ |
| TC-HD-07 | Sửa hóa đơn | Hóa đơn chưa hoàn tất | 1. Sửa trạng thái/nhân viên → Lưu | Cập nhật thành công; sai dữ liệu hiển thị cùng thông báo như khi thêm | P2 | FR-HD-01 | ☐ |
| TC-HD-08 | Xóa hóa đơn | Hóa đơn thử | 1. Xóa | Hóa đơn bị xóa hoặc hiển thị `Không thể xóa hóa đơn: …` khi có ràng buộc; dữ liệu liên quan nhất quán | P2 | FR-HD-01 | ☐ |
| TC-HD-09 | Xử lý hóa đơn đã hoàn tất | Hóa đơn `HOAN_THANH` | 1. Nhận/hoàn thành hóa đơn đó | `Hóa đơn này đã hoàn tất.` | P1 | FR-HD-03 | ☐ |
| TC-HD-10 | Tài khoản không có mã nhân viên khi xử lý hóa đơn | Tài khoản không liên kết `MaNV` | 1. Nhận hóa đơn | `Tài khoản chưa có mã nhân viên.` | P2 | FR-HD-03 | ☐ |
| TC-HD-11 | Quy trình hóa đơn giao hàng theo vai trò | Hóa đơn giao hàng | 1. Nhân viên phân công tài xế từ màn hóa đơn<br>2. Tài xế nhận và hoàn thành | Quy tắc giống FR-WF; thiếu tài xế → `Vui lòng chọn nhân viên giao hàng.` | P2 | FR-HD-03 | ☐ |
| TC-HD-12 | Chi tiết hóa đơn và các trang tra cứu chỉ dành cho quản lý | nv01 / admin01 | 1. nv01 GET `/chitiethoadon`, `/dathang`, `/giohang`<br>2. admin01 GET cùng URL | nv01 bị chuyển; admin01 xem được | P2 | FR-HD-05 | ☐ |

## 19. Dashboard, doanh thu, banner (DASH)

Yêu cầu liên quan: FR-DASH-01…04

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-DASH-01 | Số liệu thống kê khớp CSDL | Dữ liệu mồi | 1. Mở `/dashboard`<br>2. So sánh với `COUNT(*)` các bảng KH, NV, TK, SP, LSG, HĐ, ĐH, GH | Tất cả số liệu khớp | P1 | FR-DASH-01 | ☐ |
| TC-DASH-02 | Tổng doanh thu | Có nhiều bản ghi `DoanhThu` | 1. So sánh với `SUM(DoanhThu)` | Khớp chính xác, định dạng tiền VND | P1 | FR-DASH-02 | ☐ |
| TC-DASH-03 | Doanh thu theo tháng | Doanh thu rải nhiều tháng | 1. Xem biểu đồ/bảng theo tháng<br>2. Đối chiếu `GROUP BY` tháng | Mỗi tháng khớp tổng trong CSDL | P2 | FR-DASH-02 | ☐ |
| TC-DASH-04 | Doanh thu ghi ngay khi đơn được thanh toán | Đơn mới thanh toán, trạng thái `CHO_NHAN` | 1. Xem bảng `DoanhThu`<br>2. Mở dashboard | Đã có bản ghi doanh thu cho hóa đơn; tổng doanh thu đã bao gồm đơn này | P1 | FR-DASH-04 | ☐ |
| TC-DASH-05 | Hoàn tất đơn không nhân đôi doanh thu | Đơn trên | 1. Hoàn thành đơn<br>2. Đếm bản ghi `DoanhThu` theo `MaHD`; xem tổng | Vẫn đúng 1 bản ghi cho `MaHD`; tổng doanh thu không đổi | P1 | FR-DASH-04 | ☐ |
| TC-DASH-06 | Dashboard khi CSDL chưa có dữ liệu | CSDL trống | 1. Mở dashboard | Các số = 0, không lỗi 500 | P3 | FR-DASH-01 | ☐ |
| TC-DASH-07 | Tải banner hợp lệ | admin01; tệp ảnh PNG/JPG | 1. Tải banner qua form trên dashboard | Thông báo thành công; banner hiển thị trên dashboard và `/customer-shop` | P3 | FR-DASH-03 | ☐ |
| TC-DASH-08 | Tải banner khi chưa chọn ảnh | — | 1. Gửi form không chọn ảnh | `Chưa chọn ảnh hợp lệ.` | P3 | FR-DASH-03 | ☐ |
| TC-DASH-09 | Thay thế một banner có sẵn | Đã có banner | 1. Dùng `replaceBannerTarget` thay ảnh | Ảnh được thay đúng vị trí | P3 | FR-DASH-03 | ☐ |
| TC-DASH-10 | Nhân viên không xem được dashboard | nv01 | 1. GET `/dashboard` | Chuyển hướng `/nhanvien-home` | P1 | 3.2 | ☐ |

## 20. Bảo mật và phi chức năng (NFR)

Yêu cầu liên quan: Mục 8, 11 đặc tả

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-NFR-01 | ⚠ R-01 — Hoàn tất phiên không cần trả tiền | Khách A tạo phiên `PAY-X…` nhưng **không** chuyển khoản | 1. Chưa đăng nhập (hoặc tài khoản bất kỳ), POST `/payment-session` `action=complete&id=PAY-X…` | Mong đợi: bị từ chối, không tạo đơn. Mã hiện tại cho phép hoàn tất → nếu đơn được tạo, ghi nhận lỗi **Nghiêm trọng** | P1 | R-01 | ☐ |
| TC-NFR-02 | ⚠ R-02 — Sửa đơn giá trong `cartJson` | Sản phẩm giá 45.000 | 1. Dùng Postman gửi `cartJson` với giá 1.000<br>2. Hoàn tất thanh toán đúng 1.000 | Mong đợi: máy chủ dùng giá trong CSDL (kèm phụ thu size) và từ chối. Mã hiện tại chỉ kiểm tra giá > 0 → ghi nhận lỗi **Nghiêm trọng** | P1 | R-02 | ☐ |
| TC-NFR-03 | ⚠ R-02b — Phụ thu size | Size L có `PhuThu > 0` | 1. Mua cùng sản phẩm size S và L<br>2. So sánh đơn giá trong đơn | Đối chiếu với bảng `Size`: nếu hai size cùng đơn giá thì ghi nhận chưa áp phụ thu | P2 | R-02 | ☐ |
| TC-NFR-04 | ⚠ R-09 — Giả mạo `maKH` khi tạo phiên | Chưa đăng nhập | 1. POST create với `maKH=KH01`, giỏ hợp lệ | Mong đợi: HTTP 401. Mã hiện tại có thể tạo phiên cho KH01 → ghi nhận lỗi | P1 | R-09 | ☐ |
| TC-NFR-05 | ⚠ R-08 — Xóa qua GET (CSRF) | Quản lý đang đăng nhập | 1. Trong tab khác mở trang HTML có `<img src="…/sanpham?action=delete&id=SPxx">` (SP chưa có lịch sử) | Mong đợi: không xóa nếu thiếu token. Mã hiện tại xóa được → ghi nhận lỗi | P2 | R-08 | ☐ |
| TC-NFR-06 | XSS lưu trữ qua họ tên/địa chỉ/mô tả | — | 1. Nhập `<img src=x onerror=alert(1)>` vào họ tên khách, địa chỉ giao hàng, mô tả khuyến mãi<br>2. Xem ở danh sách, hóa đơn, cửa hàng | Hiển thị dạng văn bản, không chạy script | P1 | NFR-02 | ☐ |
| TC-NFR-07 | Chống SQL injection ở ô tìm kiếm | — | 1. Tìm lịch sử giá/sản phẩm với từ khóa `'; DROP TABLE SanPham; --` | Không lỗi 500, bảng còn nguyên, kết quả rỗng | P1 | NFR-02 | ☐ |
| TC-NFR-08 | Truy cập tệp ngoài thư mục qua `/uploads/` | — | 1. GET `/uploads/../WEB-INF/web.xml`, `/uploads/..%2f..%2fpom.xml` | 404/400; không đọc được tệp ngoài thư mục ảnh | P1 | NFR-02 | ☐ |
| TC-NFR-09 | Tệp cấu hình không bị lộ qua web | — | 1. GET `/persistence.xml`, `/META-INF/persistence.xml`, `/sepay.properties`, `/email.properties` | 404 | P1 | NFR-02, R-07 | ☐ |
| TC-NFR-10 | Thông báo lỗi không lộ chi tiết kỹ thuật | — | 1. Gây lỗi CSDL khi thêm dữ liệu quá độ dài | Thông báo có thể kèm lý do ngắn nhưng không lộ chuỗi kết nối/tên máy chủ/mật khẩu | P2 | NFR-02 | ☐ |
| TC-NFR-11 | Mật khẩu không xuất hiện ở dạng rõ trong CSDL | Sau đăng ký / đổi mật khẩu / tạo tài khoản | 1. Đọc cột `MatKhau` của bản ghi mới | Luôn dạng `PBKDF2$…` | P1 | NFR-01 | ☐ |
| TC-NFR-12 | UTF-8 xuyên suốt | — | 1. Nhập tiếng Việt có dấu ở mọi form chính (khách, sản phẩm, khuyến mãi, địa chỉ) | Lưu và hiển thị đúng ở danh sách, hóa đơn, email | P2 | NFR-07 | ☐ |
| TC-NFR-13 | Tương thích trình duyệt | Chrome, Firefox, Edge | 1. Chạy E2E-01 trên từng trình duyệt | Cùng kết quả, giao diện không vỡ | P2 | NFR-09 | ☐ |
| TC-NFR-14 | Giao diện trên màn hình 390px | Thiết bị/giả lập di động | 1. Mở cửa hàng, thêm hàng, mở giỏ, thanh toán | Không tràn ngang, nút bấm được, QR hiển thị đủ | P3 | NFR-09 | ☐ |
| TC-NFR-15 | Hiệu năng danh sách lớn | 500 sản phẩm / 2000 hóa đơn | 1. Mở `/sanpham`, `/hoadon`, `/dashboard` | Mỗi trang tải trong ≤ 3 giây trên môi trường thử | P3 | NFR-08 | ☐ |
| TC-NFR-16 | Đồng thời nhiều phiên thanh toán | 20 khách tạo phiên cùng lúc | 1. Tạo 20 phiên song song<br>2. Hoàn tất từng phiên | Mã phiên không trùng; mỗi phiên tạo đúng 1 đơn; không lỗi tranh chấp | P2 | NFR-05, 08 | ☐ |

## 21. Kịch bản xuyên suốt (E2E)

Yêu cầu liên quan: TEST_PLAN mục 7.2

| Mã ca | Tiêu đề | Tiền điều kiện / Dữ liệu | Các bước thực hiện | Kết quả mong đợi | Ưu tiên | Yêu cầu | Kết quả thực tế |
|---|---|---|---|---|:-:|---|:-:|
| TC-E2E-01 | E2E-01 Khách mua tại quán với voucher % | Voucher 10%, SĐT mới | 1. Đăng ký<br>2. Thêm 2 sản phẩm size khác nhau<br>3. Áp voucher 10%<br>4. Chọn tại quán, tạo phiên<br>5. Gửi webhook tiền vào<br>6. nv01 nhận đơn → hoàn thành<br>7. Xem dashboard | Đơn đi đúng `CHO_NHAN → HOAN_THANH`; hóa đơn đúng số tiền sau giảm; doanh thu đúng và không nhân đôi; khách thấy đơn đã xử lý | P1 | SHOP, PAY, ORD, WF, DASH | ☐ |
| TC-E2E-02 | E2E-02 Khách mua giao hàng với voucher số tiền | Voucher `VCAMT1`, gh01 hoạt động | 1. Đăng nhập KH01<br>2. Chọn giao hàng + địa chỉ, áp voucher 15.000<br>3. Thanh toán qua webhook<br>4. nv01 nhận → phân công gh01<br>5. gh01 nhận → xác nhận giao | Đơn và hóa đơn `HOAN_THANH`; `GiamGia = 15000`; doanh thu bằng số tiền khách trả (liên hệ R-04) | P1 | PAY, ORD, WF | ☐ |
| TC-E2E-03 | E2E-03 Quản lý tạo sản phẩm mới và bán | admin01 | 1. Tạo sản phẩm + lịch sử giá + voucher<br>2. Khách mở cửa hàng, mua sản phẩm với voucher<br>3. Hoàn tất đơn<br>4. Xem dashboard | Sản phẩm/voucher hiển thị đúng với khách; số liệu dashboard tăng tương ứng | P1 | SP, LSG, KM, SHOP, DASH | ☐ |
| TC-E2E-04 | E2E-04 Vòng đời nhân viên | admin01 | 1. Tạo nhân viên + tài khoản<br>2. Nhân viên đăng nhập, nhận 1 đơn đang chờ và hoàn thành 1 đơn khác<br>3. Quản lý xóa mềm nhân viên<br>4. Thử đăng nhập lại | Không đăng nhập được; đơn đang xử lý của nhân viên bị xóa, đơn hoàn tất vẫn còn, doanh thu hoàn tất vẫn đúng | P1 | NV, TK, AUTH, WF | ☐ |
| TC-E2E-05 | E2E-05 Quên mật khẩu rồi mua hàng | KH01 có email thật | 1. Quên mật khẩu → OTP → đặt mật khẩu mới<br>2. Đăng nhập mật khẩu mới<br>3. Mua hàng | Đăng nhập được bằng mật khẩu mới, mật khẩu cũ bị từ chối, mua hàng bình thường | P1 | PWD, AUTH, SHOP | ☐ |

## 22. Ma trận truy vết yêu cầu → module kiểm thử

| Nhóm yêu cầu | Module ca kiểm thử |
|---|---|
| FR-AUTH | AUTH, PERM |
| FR-REG | REG |
| FR-PWD | PWD |
| FR-KH | KH |
| FR-NV | NV, AUTH |
| FR-TK | TK |
| FR-SP | SP |
| FR-LSG | LSG |
| FR-KM | KM, PAY |
| FR-SHOP | SHOP |
| FR-PAY | PAY, IPN |
| FR-ORD | ORD |
| FR-WF | WF |
| FR-HD | HD |
| FR-DASH | DASH |
| NFR / Rủi ro R-01…R-13 | NFR, IPN, ORD (các ca ⚠) |
| Kịch bản xuyên suốt | E2E |

## 23. Danh mục ca ⚠ (kiểm chứng rủi ro mã nguồn)

| Rủi ro | Mô tả ngắn | Ca kiểm thử |
|---|---|---|
| R-01 | Hoàn tất phiên thanh toán không cần trả tiền | TC-NFR-01 |
| R-02 | Giá và phụ thu size lấy từ client | TC-NFR-02, TC-NFR-03 |
| R-03 | Webhook chấp nhận khi không có header xác thực | TC-IPN-09 |
| R-04 | Giảm giá theo số tiền / mức tối đa bị lưu sai trong hóa đơn | TC-ORD-08, TC-ORD-09 |
| R-05 | Không trừ tồn kho khi bán | TC-ORD-16 |
| R-06 | Ghép phiên theo số tiền khi không có mã phiên | TC-IPN-16 |
| R-08 | Xóa qua GET, thiếu CSRF | TC-NFR-05 |
| R-09 | Giả mạo `maKH` khi tạo phiên | TC-NFR-04 |