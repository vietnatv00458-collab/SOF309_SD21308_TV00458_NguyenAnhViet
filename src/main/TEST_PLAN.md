# KẾ HOẠCH KIỂM THỬ — QUẢN LÝ QUÁN COFFEE

| Thông tin | Nội dung |
|---|---|
| Hệ thống | Quản lý quán Coffee (`QuanLyCafe`) |
| Tài liệu tham chiếu | `SPECIFICATIONS.md` (đặc tả), `TEST_CASES.md` (bảng ca kiểm thử) |
| Phiên bản | 1.0 |
| Ngày lập | 16/11/2026 |

---

## 1. Mục đích

Xác nhận hệ thống đáp ứng các yêu cầu trong `SPECIFICATIONS.md`, đặc biệt:

1. Đăng nhập, đăng ký, quên mật khẩu và **phân quyền theo vai trò** hoạt động đúng.
2. Các module quản trị (khách hàng, nhân viên, tài khoản, sản phẩm, giá, khuyến mãi) kiểm tra dữ liệu đúng quy tắc.
3. Luồng **đặt hàng → thanh toán → lưu đơn → xử lý đơn → hoàn thành → doanh thu** nhất quán về dữ liệu và số tiền.
4. Các **rủi ro đã quan sát** (R-01 … R-13 trong đặc tả) được kiểm chứng và ghi nhận kết quả.

---

## 2. Phạm vi

### 2.1 Trong phạm vi

| Nhóm | Nội dung | Mã yêu cầu |
|---|---|---|
| Xác thực | Đăng nhập, đăng xuất, đăng ký, quên mật khẩu OTP | FR-AUTH, FR-REG, FR-PWD |
| Phân quyền | Ma trận truy cập theo vai trò, chuyển hướng | Mục 3.2 đặc tả |
| Quản trị | Khách hàng, nhân viên (xóa mềm), tài khoản | FR-KH, FR-NV, FR-TK |
| Danh mục | Sản phẩm, lịch sử giá, khuyến mãi | FR-SP, FR-LSG, FR-KM |
| Bán hàng | Cửa hàng khách, giỏ hàng, mã giảm giá, giao hàng/tại quán | FR-SHOP |
| Thanh toán | Phiên thanh toán, QR, webhook SePay | FR-PAY |
| Đơn hàng | Lưu đơn, quy trình nhận/giao/hoàn thành, hóa đơn | FR-ORD, FR-WF, FR-HD |
| Báo cáo | Dashboard, doanh thu, banner | FR-DASH |
| Phi chức năng | Bảo mật, toàn vẹn dữ liệu, tương thích, hiệu năng cơ bản | Mục 8 đặc tả |

### 2.2 Ngoài phạm vi

- Kiểm thử hạ tầng ngân hàng/SePay thật (dùng giả lập webhook thay thế).
- Kiểm thử tải lớn (hàng nghìn người dùng đồng thời); chỉ kiểm tra đồng thời mức cơ bản.
- Kiểm thử trên thiết bị di động gốc (chỉ kiểm tra giao diện co giãn trên trình duyệt).
- Hiệu năng của máy chủ SQL Server, độ tin cậy của dịch vụ `img.vietqr.io`.

---

## 3. Chiến lược kiểm thử

### 3.1 Các mức kiểm thử

| Mức | Mục tiêu | Cách thực hiện | Công cụ đề xuất |
|---|---|---|---|
| Kiểm thử đơn vị | Hàm thuần: `PasswordUtils`, `RoleUtils`, `OrderStatusUtils`, `OtpUtils`, tính giảm giá | Viết test JUnit 5, không cần CSDL | JUnit 5, AssertJ |
| Kiểm thử tích hợp | DAO + CSDL, `OrderPersistenceService`, `NhanVienService`, `PasswordResetService` | CSDL SQL Server thử nghiệm riêng, dữ liệu mồi | JUnit 5, Testcontainers (SQL Server) hoặc DB riêng |
| Kiểm thử hệ thống / chức năng | Toàn bộ luồng qua giao diện và HTTP | Chạy theo `TEST_CASES.md` | Trình duyệt, Postman/curl |
| Kiểm thử phân quyền | Mọi URL × mọi vai trò | Bảng ma trận, gọi HTTP với từng phiên | Postman collection / script curl |
| Kiểm thử API/Webhook | `/payment-session`, `/sepay-ipn` | Gửi yêu cầu giả lập, kiểm tra JSON và dữ liệu | Postman, curl |
| Kiểm thử bảo mật cơ bản | R-01…R-09 | Kiểm thử thủ công theo kịch bản tấn công đơn giản | Postman, DevTools, OWASP ZAP (tùy chọn) |
| Kiểm thử chấp nhận (UAT) | Quy trình thực tế tại quán | Chủ quán/nhân viên thao tác theo kịch bản | Thủ công |
| Kiểm thử hồi quy | Sau mỗi bản sửa lỗi | Chạy lại bộ ca mức Cao | Thủ công + tự động hóa dần |

### 3.2 Kỹ thuật thiết kế ca kiểm thử

- **Phân vùng tương đương và giá trị biên:** mật khẩu (5/6 ký tự), tuổi nhân viên (17/18), phần trăm giảm (0, 0.01, 99.99, 100), ngày bắt đầu/kết thúc, SĐT (7/8/15/16 chữ số), OTP (hết hạn đúng 5 phút), số lần nhập sai OTP (4/5/6).
- **Bảng quyết định:** phân quyền theo vai trò; quy trình đơn tại quán so với đơn giao hàng.
- **Chuyển trạng thái:** vòng đời đơn `CHO_NHAN → … → DANG_GIAO → HOAN_THANH`, và các chuyển trạng thái không hợp lệ.
- **Đoán lỗi:** dữ liệu rỗng, khoảng trắng, ký tự đặc biệt/tiếng Việt, số âm, chuỗi quá dài, thao tác lặp, yêu cầu giả mạo.
- **Kiểm thử âm tính (negative)** cho mọi thông báo lỗi liệt kê trong đặc tả.

### 3.3 Thứ tự ưu tiên

| Mức | Tiêu chí | Ví dụ |
|---|---|---|
| **P1 – Cao** | Ảnh hưởng tiền, bảo mật, khả năng bán hàng | Thanh toán, lưu đơn, giảm giá, đăng nhập, phân quyền, webhook |
| **P2 – Trung bình** | Chức năng quản trị thường dùng | CRUD sản phẩm/khuyến mãi/nhân viên, xử lý đơn |
| **P3 – Thấp** | Giao diện, tiện ích | Banner, phân trang, ghi nhớ mã giảm giá |

---

## 4. Môi trường kiểm thử

### 4.1 Môi trường

| Thành phần | Cấu hình |
|---|---|
| Hệ điều hành | Windows 10/11 hoặc Linux |
| JDK | 17 |
| Servlet container | Tomcat 10.1.x (Jakarta EE 10) |
| Cơ sở dữ liệu | SQL Server 2019/2022 (database `QuanLyCafe` **riêng cho kiểm thử**) |
| Build | `mvn clean package` → `QuanLyCafe.war` |
| SMTP | Hộp thư thử (Mailtrap/MailHog) cho email OTP |
| SePay | Không dùng tài khoản thật; giả lập webhook bằng Postman, cấu hình `sepay.properties` thử nghiệm |
| Trình duyệt | Chrome, Firefox, Edge bản mới nhất; thêm Safari nếu có |
| Màn hình | 1920×1080, 1366×768, 390×844 (di động) |

### 4.2 Dữ liệu kiểm thử mồi (seed)

| Loại | Dữ liệu |
|---|---|
| Tài khoản Quản lý | `admin01` / `Admin@123` (nhân viên `NV00` có `ChucVu = Quản lý` → vai trò `Admin`) |
| Tài khoản Nhân viên | `nv01` / `Nv@12345` (liên kết `NV01`, SĐT `0900000001`, `ChucVu = Pha chế`) |
| Tài khoản Nhân viên thứ 2 | `nv02` / `Nv@12345` (liên kết `NV02`) để kiểm thử tranh chấp nhận đơn |
| Tài khoản Giao hàng | `gh01` / `Gh@12345` (liên kết `NV03`), `gh02` (liên kết `NV04`); hai nhân viên này có `ChucVu = Giao hàng` |
| Nhân viên đã xóa mềm | `NV05` có tài khoản, `IsDeleted = 1` |
| Khách hàng | `KH01` (SĐT `0911111111`, email thật của hộp thư thử, có tài khoản); `KH02` (có SĐT, **chưa có** tài khoản) |
| Sản phẩm | ≥ 12 sản phẩm gồm 4 loại (Bánh, Cafe, Trà sữa, Sinh tố), có ảnh; 1 sản phẩm chưa phát sinh giao dịch (xóa được) và 1 sản phẩm đã có lịch sử |
| Size | `S`, `M`, `L` với phụ thu khác nhau |
| Khuyến mãi | `VCPCT1` giảm 10% (không giới hạn); `VCPCT2` giảm 50%, tối đa 20.000đ; `VCAMT1` giảm 15.000đ; `VCEXP1` đã hết hạn; `VCFUT1` chưa đến ngày bắt đầu; `VCEDGE` có `NgàyKếtThúc = hôm nay` |
| Đơn hàng | Một đơn `CHO_NHAN` tại quán, một đơn `CHO_NHAN` giao hàng, một đơn `HOAN_THANH` |

Thao tác trước mỗi vòng: khôi phục CSDL từ bản sao lưu chuẩn (không dùng `reset_invoice_workflow.sql` trên môi trường chung).

---

## 5. Tiêu chí vào / ra

### 5.1 Tiêu chí bắt đầu
- Bản build `QuanLyCafe.war` triển khai thành công, trang `/login` truy cập được.
- CSDL kiểm thử đã tạo lược đồ, chạy đủ 3 tập lệnh trong `/sql` (trừ `reset_invoice_workflow.sql`) và nạp dữ liệu mồi.
- `TEST_CASES.md` đã được rà soát.
- SMTP thử và công cụ giả lập webhook hoạt động.

### 5.2 Tiêu chí kết thúc
- 100% ca **P1** đã chạy; ≥ 95% ca P1 đạt.
- ≥ 90% ca P2 đạt; ≥ 80% ca P3 đạt.
- Không còn lỗi **Nghiêm trọng (Critical)** hoặc **Cao (High)** mở, hoặc có phê duyệt ngoại lệ bằng văn bản.
- Các rủi ro R-01…R-09 đều có kết luận (đã sửa / chấp nhận rủi ro / lên kế hoạch sửa).
- Báo cáo kiểm thử được lập và ký duyệt.

### 5.3 Tiêu chí tạm dừng / tiếp tục
- **Tạm dừng** khi: build không chạy được; không kết nối CSDL; > 30% ca P1 trong một module thất bại do cùng lỗi chặn.
- **Tiếp tục** khi lỗi chặn được sửa và build mới được triển khai, chạy lại smoke test.

---

## 6. Phạm vi chi tiết theo module

| Module | Số ca | Trọng tâm |
|---|---:|---|
| AUTH – Đăng nhập/đăng xuất | 20 | Đăng nhập bằng SĐT/tên đăng nhập, nâng cấp mật khẩu cũ, nhân viên đã xóa, `returnUrl`, SQL injection |
| REG – Đăng ký | 14 | Thứ tự kiểm tra lỗi, biên mật khẩu 5/6 ký tự, dùng lại `MaKH` có sẵn, tự đăng nhập |
| PWD – Quên mật khẩu | 18 | Email, OTP hết hạn 5 phút, giới hạn 5 lần sai, đặt lại mật khẩu |
| PERM – Phân quyền | 18 | Ma trận vai trò × URL, chuyển hướng, chặn thao tác ghi ở máy chủ |
| KH – Khách hàng | 9 | Trùng mã, xóa khi có lịch sử |
| NV – Nhân viên | 15 | Biên 18 tuổi, lương, xóa mềm và dọn đơn đang xử lý |
| TK – Tài khoản | 13 | Trùng tên đăng nhập, mỗi NV một tài khoản, vai trò suy ra từ chức vụ |
| SP – Sản phẩm | 22 | Giá/tồn, ảnh, giới hạn 5MB, xóa khi có lịch sử, quyền nhân viên, XSS |
| LSG – Lịch sử giá | 10 | Mã SP, giá âm, ngày áp dụng |
| KM – Khuyến mãi | 24 | Kiểu giảm, biên phần trăm 0/100, ngày, hiệu lực hai đầu mút, mã voucher |
| SHOP – Cửa hàng/giỏ | 21 | Size, gộp giỏ, mã giảm giá, địa chỉ giao hàng, hồ sơ khách |
| PAY – Thanh toán | 22 | Tạo phiên, tính giảm giá, hết hạn 10/15 phút, trạng thái |
| IPN – Webhook | 21 | Xác thực HMAC/API key, chống trùng, ghép phiên, hết hạn |
| ORD – Lưu đơn | 16 | Số tiền các bảng, rollback, giảm giá theo số tiền/mức tối đa, tồn kho |
| WF – Quy trình đơn | 23 | Nhận/giao/hoàn thành, quyền theo vai trò, tranh chấp |
| HD – Hóa đơn | 12 | Kiểm tra khóa ngoại logic, hóa đơn đã hoàn tất |
| DASH – Dashboard | 10 | Số liệu, doanh thu theo tháng, không nhân đôi doanh thu, banner |
| NFR – Bảo mật/phi chức năng | 16 | R-01…R-09, XSS, UTF-8, tương thích, di động |
| E2E – Kịch bản xuyên suốt | 5 | Năm kịch bản ở mục 7.2 |
| **Tổng** | **309** | P1: 162 · P2: 107 · P3: 40 — chi tiết trong `TEST_CASES.md` |

---

## 7. Quy trình thực hiện

### 7.1 Các giai đoạn

| Giai đoạn | Công việc | Đầu ra |
|---|---|---|
| 1. Chuẩn bị | Dựng môi trường, nạp dữ liệu mồi, rà soát test case | Môi trường sẵn sàng |
| 2. Smoke test | ~15 ca P1 cốt lõi (đăng nhập, xem cửa hàng, tạo phiên thanh toán, hoàn thành đơn) | Quyết định chấp nhận build |
| 3. Kiểm thử chức năng | Chạy toàn bộ theo module, ưu tiên P1 → P2 → P3 | Kết quả từng ca, danh sách lỗi |
| 4. Kiểm thử tích hợp luồng | Kịch bản xuyên suốt (Mục 7.2) | Kết quả luồng E2E |
| 5. Bảo mật & phi chức năng | Kịch bản R-01…R-09, tương thích trình duyệt | Báo cáo rủi ro |
| 6. Hồi quy | Chạy lại sau khi sửa lỗi | Xác nhận đã sửa |
| 7. Báo cáo | Tổng hợp số liệu, kết luận | Báo cáo kiểm thử |

### 7.2 Kịch bản xuyên suốt (E2E)

| Mã | Kịch bản |
|---|---|
| E2E-01 | Khách đăng ký → chọn 2 sản phẩm khác size → áp voucher % → chọn **tại quán** → tạo phiên → webhook thanh toán → đơn xuất hiện `CHO_NHAN` → nhân viên nhận đơn → hoàn thành → hóa đơn `HOAN_THANH` → doanh thu tăng đúng `ThanhTien` |
| E2E-02 | Khách đăng nhập → chọn **giao hàng** + địa chỉ → voucher số tiền → thanh toán → nhân viên nhận đơn → phân công tài xế → tài xế nhận → tài xế xác nhận giao → đơn/hóa đơn `HOAN_THANH`, doanh thu ghi đúng |
| E2E-03 | Quản lý tạo sản phẩm mới + lịch sử giá + voucher → khách thấy sản phẩm/voucher trên cửa hàng → mua → số liệu dashboard tăng tương ứng |
| E2E-04 | Quản lý tạo nhân viên + tài khoản → nhân viên đăng nhập → xóa mềm nhân viên → không đăng nhập được, đơn đã hoàn tất vẫn còn |
| E2E-05 | Khách quên mật khẩu → nhận OTP → đổi mật khẩu → đăng nhập mật khẩu mới thành công, mật khẩu cũ bị từ chối |

### 7.3 Quản lý lỗi

**Mức nghiêm trọng**

| Mức | Định nghĩa | Ví dụ |
|---|---|---|
| Critical | Mất tiền/dữ liệu, bypass bảo mật, hệ thống sập | Hoàn tất thanh toán không cần trả tiền (R-01); khách sửa giá (R-02) |
| High | Chức năng chính sai, không có cách tránh | Hóa đơn lệch số tiền thực trả (R-04); không lưu được đơn |
| Medium | Chức năng phụ sai, có cách tránh | Thông báo lỗi sai; phân trang sai |
| Low | Giao diện, chính tả | Lệch căn lề, sai chính tả |

**Vòng đời lỗi:** Mới → Đã xác nhận → Đang sửa → Đã sửa → Kiểm tra lại → Đóng (hoặc Mở lại / Từ chối).

**Thông tin tối thiểu khi báo lỗi:** mã ca kiểm thử, các bước tái hiện, kết quả mong đợi/thực tế, vai trò & dữ liệu dùng, ảnh chụp/nhật ký (`catalina.out`, console), trình duyệt & phiên bản build.

---

## 8. Rủi ro của quá trình kiểm thử và biện pháp

| Rủi ro | Khả năng | Ảnh hưởng | Biện pháp |
|---|---|---|---|
| Không có tài khoản SePay thử → khó kiểm thử webhook | Cao | Cao | Giả lập webhook bằng Postman với cùng định dạng JSON/form/multipart; kiểm thử riêng chữ ký HMAC |
| Phiên thanh toán nằm trong bộ nhớ, khởi động lại làm mất | Trung bình | Trung bình | Không khởi động lại Tomcat giữa các bước của một ca; ghi rõ trong ca kiểm thử mất phiên |
| Phụ thuộc đồng hồ (hết hạn 5 phút OTP, 10 phút phiên, ngày voucher) | Cao | Trung bình | Dùng dữ liệu voucher đặt ngày tương đối; với hết hạn thật phải chờ hoặc rút ngắn bằng hằng số ở bản test |
| Dữ liệu dùng chung giữa các ca (mã tự tăng) | Trung bình | Trung bình | Khôi phục CSDL trước mỗi vòng; ghi lại mã sinh ra |
| Chạy nhầm `reset_invoice_workflow.sql` trên dữ liệu chung | Thấp | Cao | Chỉ dùng trên bản sao CSDL cá nhân |
| Lộ khóa/mật khẩu trong `persistence.xml` | Trung bình | Cao | Dùng cấu hình riêng cho môi trường test, không đưa thông tin thật vào kho mã |
| Hành vi không khớp đặc tả (R-01…R-09) | Cao | Cao | Ghi nhận là lỗi/ngoại lệ, đối chiếu với chủ sản phẩm |

---

## 9. Vai trò và trách nhiệm

| Vai trò | Trách nhiệm |
|---|---|
| Quản lý kiểm thử (Test Lead) | Lập kế hoạch, phân công, theo dõi tiến độ, duyệt báo cáo |
| Kiểm thử viên | Thiết kế/chạy ca, báo lỗi, kiểm tra lại |
| Lập trình viên | Sửa lỗi, hỗ trợ dựng môi trường, viết test đơn vị/tích hợp |
| Chủ quán / đại diện nghiệp vụ | Xác nhận quy tắc nghiệp vụ, thực hiện UAT |
| Quản trị CSDL | Sao lưu/khôi phục, cấp quyền |

---

## 10. Sản phẩm bàn giao

| Sản phẩm | Mô tả |
|---|---|
| `TEST_PLAN.md` | Kế hoạch kiểm thử (tài liệu này) |
| `TEST_CASES.md` | Bảng ca kiểm thử có kết quả thực tế |
| Nhật ký lỗi | Danh sách lỗi kèm mức độ, trạng thái |
| Bộ Postman collection / script | Kiểm thử API, phân quyền, webhook |
| Báo cáo kiểm thử | Tổng hợp số ca đạt/trượt, độ phủ yêu cầu, rủi ro còn lại |

### Chỉ số báo cáo
- Tỷ lệ ca đạt/trượt/bị chặn theo module và theo mức ưu tiên.
- Mật độ lỗi theo module; số lỗi mở theo mức nghiêm trọng.
- Độ phủ yêu cầu: mỗi `FR-*` có ít nhất một ca kiểm thử.

---

## 11. Lịch trình đề xuất

| Tuần | Công việc |
|---|---|
| 1 | Chuẩn bị môi trường, dữ liệu mồi; smoke test; kiểm thử AUTH/REG/PWD/PERM |
| 2 | KH, NV, TK, SP, LSG, KM |
| 3 | SHOP, PAY, IPN, ORD, WF, HD, DASH + kịch bản E2E |
| 4 | Bảo mật & phi chức năng, hồi quy, UAT, báo cáo |

(Thời lượng điều chỉnh theo số người thực hiện; ước tính cho 1–2 kiểm thử viên.)

---

## 12. Phê duyệt

| Vai trò | Họ tên | Ngày | Chữ ký |
|---|---|---|---|
| Test Lead | | | |
| Trưởng nhóm phát triển | | | |
| Đại diện nghiệp vụ (chủ quán) | | | |