# AI Log 01 — Thiết kế Unit Test cho Controller (Tuần 2 – Y3)

| Thông tin | Nội dung |
|---|---|
| Sinh viên | Nguyễn Anh Việt – TV00458 |
| Lớp / Môn | SD21308 – SOF309 |
| Công cụ AI | Claude (Anthropic) |
| Controller được test | `ChiTietGioHangServlet`, `RegisterServlet`, `LoginServlet`, `KhachHangServlet` |
| File test | `ChiTietGioHangControllerTest`, `RegisterControllerTest`, `LoginControllerTest`, `KhachHangControllerTest` (trong `src/test/java/com/poly/sof309/controllers/`) |
| Tài liệu tham chiếu | `SPECIFICATIONS.md` (FR-AUTH, FR-REG, FR-KH, FR-HD-05), `TEST_CASES.md` (TC-AUTH, TC-REG, TC-KH, TC-HD-12) |

> **Lưu ý khi nộp:** các mục có dấu `[ĐIỀN]` là phần sinh viên tự ghi theo những gì mình thực sự đã làm và đã thấy khi chạy.

---

## 1. Prompt đã dùng

**Prompt 1 – Hỏi hướng làm tuần 2**
> Gửi ảnh yêu cầu Tuần 2 (Unit Test Controller, `@ParameterizedTest` + `@CsvSource`, `assertThrows()`) và link repo, nhờ AI hướng dẫn từng bước.

**Prompt 2 – Cung cấp ngữ cảnh dự án**
> Gửi `TEST_CASES.md`, `SPECIFICATIONS.md`, `pom.xml`, `SampleControllerTest.java` và mã nguồn `ChiTietGioHangServlet`, `ChiTietGioHangDAO`, `ChiTietGioHang` của hệ thống QuanLyCafe.

`[ĐIỀN]` Dán nguyên văn prompt thật của bạn nếu khác với tóm tắt trên.

## 2. Kết quả AI đề xuất

1. **Cách cô lập Controller khỏi CSDL:** Servlet tự `new` các DAO ở field và DAO gọi `JPAUtil.EMF` (cần SQL Server). AI đề xuất dùng `Mockito.mockConstruction` để thay 5 DAO bằng mock, và mock `HttpServletRequest/Response/RequestDispatcher`.
2. **Cách gọi doGet/doPost:** hai hàm này `protected` nên test (khác package) gọi qua hàm `service()` công khai của `HttpServlet`.
3. **Kỹ thuật thiết kế test case:** phân vùng tương đương và phân tích giá trị biên cho các trường của Form.
4. **Bổ sung phụ thuộc vào `pom.xml`:** `mockito-core`, `jakarta.servlet-api` (provided), `jakarta.persistence-api`.

## 3. Thiết kế test case

### 3.1 Kỹ thuật

| Kỹ thuật | Áp dụng cho |
|---|---|
| Phân vùng tương đương | Trường để trống / sai kiểu số / hợp lệ |
| Giá trị biên | `soLuong`: -1, 0, 1, Integer.MAX_VALUE; `donGia`: -0.01, 0, 0.01; `maCTGH` = Integer.MAX_VALUE + 1 |
| Kiểm tra thứ tự ưu tiên lỗi | Cả `soLuong` và `donGia` đều sai -> chỉ báo lỗi `soLuong` |
| Kiểm tra ngoại lệ | `assertThrows` / `assertDoesNotThrow` |

### 3.2 Bảng test case

| Mã | Mô tả | Kỹ thuật JUnit | Số ca | Kết quả mong đợi |
|---|---|---|---:|---|
| UT-CTGH-01 | Thêm hợp lệ | `@Test` | 1 | Gọi `create()`, redirect `/chitietgiohang` |
| UT-CTGH-02 | Trim dữ liệu, `maDatHang` trống -> null | `@Test` + `ArgumentCaptor` | 1 | Entity lưu đúng giá trị đã trim |
| UT-CTGH-03 | Thiếu trường bắt buộc (null / khoảng trắng) | `@ParameterizedTest` + `@CsvSource` | 8 | `Vui lòng nhập đầy đủ thông tin bắt buộc!` |
| UT-CTGH-04 | Sai kiểu số (chữ, thập phân, tràn Integer, dấu phẩy) | `@ParameterizedTest` + `@CsvSource` | 7 | `Mã CTGH và số lượng phải là số hợp lệ!` |
| UT-CTGH-05 | Biên `soLuong` / `donGia` khi thêm | `@ParameterizedTest` + `@CsvSource` | 8 | Lỗi tương ứng hoặc lưu thành công |
| UT-CTGH-06 | Quy tắc nghiệp vụ: đơn khóa, tham chiếu không tồn tại, trùng mã | `@ParameterizedTest` + `@CsvSource` | 6 | Thông báo lỗi tương ứng, không lưu |
| UT-CTGH-07 | Biên khi cập nhật | `@ParameterizedTest` + `@CsvSource` | 4 | Lỗi hoặc `update()` + redirect |
| UT-CTGH-08 | Đơn đã thanh toán không cho sửa/xóa | `@ParameterizedTest` + `@CsvSource` | 2 | Hiện lỗi trên trang danh sách |
| UT-CTGH-09 | Xóa với `id` không hợp lệ | `@ParameterizedTest` + `@ValueSource` | 4 | Bỏ qua, không xóa, redirect |
| UT-CTGH-10 | Lỗi DB khi xem danh sách | `assertThrows` | 1 | `IllegalStateException` lan ra |
| UT-CTGH-11 | Lỗi DB khi xóa | `assertThrows` | 1 | `RuntimeException` lan ra |
| UT-CTGH-12 | Forward JSP lỗi | `assertThrows` | 1 | `ServletException` lan ra |
| UT-CTGH-13 | Encoding không hỗ trợ | `assertThrows` | 1 | `UnsupportedEncodingException` lan ra |
| UT-CTGH-14 | Redirect lỗi mạng | `assertThrows` | 1 | `IOException` lan ra |
| UT-CTGH-15 | Lỗi DB khi thêm (đối chiếu) | `assertDoesNotThrow` | 1 | Bị bắt, hiện `Lỗi khi thêm chi tiết giỏ hàng: …` |

### 3.3 Test case bổ sung cho 3 controller còn lại

**RegisterServlet** (truy vết: FR-REG-01..05, TC-REG-01..13)

| Mã | Mô tả | Kỹ thuật JUnit | Số ca |
|---|---|---|---:|
| UT-REG-01 | Thiếu trường bắt buộc (null / khoảng trắng) | `@ParameterizedTest` + `@CsvSource` | 7 |
| UT-REG-02 | Mật khẩu: không khớp, độ dài 5/6, trim, thứ tự ưu tiên lỗi | `@ParameterizedTest` + `@CsvSource` | 7 |
| UT-REG-03..06 | Khách mới / SĐT đã có tài khoản / dùng lại MaKH / ưu tiên dữ liệu mới | `@Test` + `ArgumentCaptor` | 4 |
| UT-REG-07..08 | Lỗi lưu CSDL; AuthStore lỗi bị bỏ qua | `assertDoesNotThrow` | 2 |
| UT-REG-09..12 | GET form; encoding; forward; lỗi DB tra SĐT | `assertThrows` | 4 |

**LoginServlet** (truy vết: FR-AUTH-01..08, NFR-03, TC-AUTH-01..20)

| Mã | Mô tả | Kỹ thuật JUnit | Số ca |
|---|---|---|---:|
| UT-AUTH-01 | Thứ tự `loginId` > `sdt` > `username`, trim | `@ParameterizedTest` + `@CsvSource` | 7 |
| UT-AUTH-02 | Mật khẩu trim, trống -> null | `@ParameterizedTest` + `@CsvSource` | 3 |
| UT-AUTH-03 | Điều hướng theo vai trò | `@ParameterizedTest` + `@CsvSource` | 9 |
| UT-AUTH-04 | Biên 7/8/15/16 chữ số cho `loginPhone` | `@ParameterizedTest` + `@CsvSource` | 7 |
| UT-AUTH-05 | `returnUrl`: hợp lệ, `//evil.com`, `http://...`, rỗng | `@ParameterizedTest` + `@CsvSource` | 8 |
| UT-AUTH-06 | Vai trò nội bộ bỏ qua `returnUrl` | `@ParameterizedTest` + `@CsvSource` | 3 |
| UT-AUTH-07..09 | Đăng nhập sai; lỗi CSDL; lỗi redirect | `@ParameterizedTest`, `assertDoesNotThrow` | 5 |
| UT-AUTH-10..12 | GET form; encoding; forward lỗi | `assertThrows` | 3 |

**KhachHangServlet** (truy vết: FR-KH-01..04, TC-KH-01..09)

| Mã | Mô tả | Kỹ thuật JUnit | Số ca |
|---|---|---|---:|
| UT-KH-01..05 | Danh sách, action lạ, form thêm, sửa | `@Test`, `@ValueSource`, `@CsvSource` | 10 |
| UT-KH-06..10 | Thêm: thiếu mã/họ tên, trùng mã, trim, nguồn `customer-shop`, lỗi DB | `@ParameterizedTest` + `@CsvSource` | 14 |
| UT-KH-11..15 | Cập nhật: mã sai, họ tên trống, hợp lệ, lỗi DB, action lạ | `@ParameterizedTest` + `@CsvSource` | 10 |
| UT-KH-16..21 | Xóa: có/không lịch sử giao dịch, từ cửa hàng, lỗi DB | `@Test`, `@CsvSource`, `InOrder` | 7 |
| UT-KH-22..25 | Lỗi DB danh sách/sửa, forward, encoding | `assertThrows` | 4 |

## 4. Nhận xét về mã nguồn khi thiết kế test

Các điểm này AI nêu ra khi đọc `ChiTietGioHangServlet`; sinh viên cần **tự kiểm chứng** trước khi ghi nhận là lỗi:

1. **Ngoại lệ không được bắt ở `list` và `delete`:** lỗi DB làm trang trả HTTP 500 thay vì hiện thông báo thân thiện (xem UT-CTGH-10, UT-CTGH-11). Trong khi `insert`/`update` thì có bắt (UT-CTGH-15).
2. **Nghi vấn ở chức năng thêm:** entity `ChiTietGioHang` khai báo `@GeneratedValue(IDENTITY)` nhưng Controller bắt người dùng tự nhập `maCTGH` rồi gọi `persist`. Với Hibernate, việc này có thể gây lỗi "detached entity passed to persist" khi chạy thật. Unit test dùng DAO giả nên **không phát hiện** được; cần kiểm thử tích hợp hoặc UI (Tuần sau) để xác nhận.
3. **Chưa có thông báo cho `id` không hợp lệ khi xóa:** Controller im lặng bỏ qua (UT-CTGH-09), không báo gì cho người dùng.
4. **`LoginServlet` báo sai nguyên nhân:** `sendRedirect` nằm trong `try`, nên `IOException` khi redirect bị bắt và hiện "Kiểm tra kết nối cơ sở dữ liệu" (UT-AUTH-09). Khi trang đăng nhập không render được, `forward` bị gọi hai lần (UT-AUTH-12).
5. **`RegisterServlet` không kiểm tra định dạng SĐT/email** (khớp rủi ro R-10 trong `SPECIFICATIONS.md`). Ngoài ra khách cũ không có email thì lưu `null`, còn khách mới lưu `""` (không nhất quán).
6. **`KhachHangServlet` xóa khách rồi mới xóa tài khoản ở hai bước riêng** (UT-KH-16); nếu bước hai lỗi thì còn tài khoản mồ côi. Việc xóa qua `GET` cũng thuộc rủi ro R-08 (CSRF).
7. **Giới hạn của unit test:** các ràng buộc của CSDL như họ tên tối đa 100 ký tự (TC-KH-09) hay unique `TenDangNhap` nằm ở tầng DB, nên unit test với DAO giả không kiểm tra được; cần test tích hợp.

`[ĐIỀN]` Bạn đã kiểm chứng điểm nào, bằng cách nào, kết quả ra sao.

## 5. Phần tôi đã kiểm chứng / chỉnh sửa

`[ĐIỀN]` Ví dụ (ghi đúng việc bạn làm):

- [ ] Đã đọc từng test case và đối chiếu thông báo lỗi với chuỗi trong `ChiTietGioHangServlet`.
- [ ] Đã sửa tên class/package cho khớp thực tế (`GioHang`, `SanPham`, `Size`, `DatHang`…).
- [ ] Đã thử sửa mã nguồn để test **fail** (ví dụ đổi `soLuong <= 0` thành `soLuong < 0`) và xác nhận test bắt được lỗi.
- [ ] Những chỗ AI viết sai/thiếu và cách tôi xử lý: …

## 6. Kết quả chạy

```
mvn test
```

`[ĐIỀN]` Dán phần cuối của log, ví dụ:

```
[INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Đường dẫn commit: `[ĐIỀN]`
