package com.poly.sof309.controllers;

import dao.KhachHangDAO;
import dao.TaiKhoanDAO;
import entity.KhachHang;
import entity.TaiKhoan;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import servlet.RegisterServlet;
import utils.AuthStore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * =========================================================================
 * [Y3] UNIT TEST CONTROLLER: RegisterServlet (/register)
 * Tham chiếu: SPECIFICATIONS.md FR-REG-01..05  |  TEST_CASES.md TC-REG-01..14
 * =========================================================================
 */
@DisplayName("RegisterServlet - Unit Test (Y3)")
class RegisterControllerTest {

    private static final String CTX = "/QuanLyCafe";
    private static final String REGISTER_JSP = "/views/auth/register.jsp";
    private static final String OK = "OK";

    private RegisterServlet servlet;
    private KhachHangDAO khachHangDao;
    private TaiKhoanDAO taiKhoanDao;
    private AuthStore authStore;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        try (MockedConstruction<KhachHangDAO> khMock = mockConstruction(KhachHangDAO.class);
             MockedConstruction<TaiKhoanDAO> tkMock = mockConstruction(TaiKhoanDAO.class);
             MockedConstruction<AuthStore> asMock = mockConstruction(AuthStore.class)) {

            servlet = new RegisterServlet();
            khachHangDao = khMock.constructed().get(0);
            taiKhoanDao = tkMock.constructed().get(0);
            authStore = asMock.constructed().get(0);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn(CTX);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getSession(true)).thenReturn(session);
    }

    // ------------------------------------------------------------------
    // Hàm hỗ trợ
    // ------------------------------------------------------------------

    private void get() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        servlet.service(request, response);
    }

    private void post() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        servlet.service(request, response);
    }

    private void stubForm(String hoTen, String sdt, String email, String diaChi,
                          String matKhau, String xacNhan) {
        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("sdt")).thenReturn(sdt);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("diaChi")).thenReturn(diaChi);
        when(request.getParameter("matKhau")).thenReturn(matKhau);
        when(request.getParameter("xacNhanMatKhau")).thenReturn(xacNhan);
    }

    /** Giả lập khách MỚI: SĐT chưa có, mã tiếp theo là KH03, chưa có tài khoản. */
    private void stubNewCustomer() {
        when(khachHangDao.findByPhone(anyString())).thenReturn(null);
        when(khachHangDao.generateNextMaKH()).thenReturn("KH03");
        when(taiKhoanDao.existsById(anyString())).thenReturn(false);
    }

    private KhachHang khachHang(String ma, String hoTen, String sdt, String email, String diaChi) {
        KhachHang kh = new KhachHang();
        kh.setMaKH(ma);
        kh.setHoTen(hoTen);
        kh.setSdt(sdt);
        kh.setEmail(email);
        kh.setDiaChi(diaChi);
        return kh;
    }

    private void assertShowsError(String message) throws Exception {
        verify(request).setAttribute("error", message);
        verify(request).getRequestDispatcher(REGISTER_JSP);
        verify(dispatcher).forward(request, response);
        verify(taiKhoanDao, never()).saveCustomerRegistration(any(), any());
        verify(session, never()).setAttribute(anyString(), any());
    }

    // ==================================================================
    // A. KIỂM TRA FORM (TC-REG-02..09)
    // ==================================================================

    @ParameterizedTest(name = "[{index}] {4}")
    @CsvSource({
            // hoTen, sdt, matKhau, xacNhan, moTa
            ",0922222222,abc123,abc123,hoTen để trống (null)",
            "'   ',0922222222,abc123,abc123,hoTen chỉ gồm khoảng trắng",
            "Nguyễn Văn An,,abc123,abc123,sdt để trống (null)",
            "Nguyễn Văn An,'   ',abc123,abc123,sdt chỉ gồm khoảng trắng",
            "Nguyễn Văn An,0922222222,,abc123,matKhau để trống (null)",
            "Nguyễn Văn An,0922222222,abc123,,xacNhanMatKhau để trống (null)",
            "Nguyễn Văn An,0922222222,'      ',abc123,matKhau chỉ gồm khoảng trắng"
    })
    @DisplayName("UT-REG-01: Thiếu trường bắt buộc -> báo lỗi, không tạo tài khoản")
    void missingRequiredField_showsError(String hoTen, String sdt, String matKhau,
                                         String xacNhan, String moTa) throws Exception {
        stubForm(hoTen, sdt, null, null, matKhau, xacNhan);

        post();

        assertShowsError("Họ tên, số điện thoại và mật khẩu không được để trống!");
    }

    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource({
            // matKhau, xacNhan, ketQuaMongDoi (OK = đăng ký thành công), moTa
            "abc123,abc124,Xác nhận mật khẩu không khớp!,không khớp",
            "abc,abd,Xác nhận mật khẩu không khớp!,vừa ngắn vừa không khớp => báo KHÔNG KHỚP trước (TC-REG-09)",
            "abc12,abc12,Mật khẩu phải có ít nhất 6 ký tự!,5 ký tự (dưới biên) (TC-REG-07)",
            "'  abc12  ',abc12,Mật khẩu phải có ít nhất 6 ký tự!,5 ký tự sau khi trim",
            "abc123,abc123,OK,6 ký tự (biên hợp lệ) (TC-REG-08)",
            "'  abc123  ',abc123,OK,6 ký tự sau khi trim",
            "abcdefghijklmnopqrstuvwxyz0123456789,abcdefghijklmnopqrstuvwxyz0123456789,OK,mật khẩu dài 36 ký tự"
    })
    @DisplayName("UT-REG-02: Quy tắc mật khẩu - khớp và độ dài tối thiểu 6")
    void passwordRules_boundaries(String matKhau, String xacNhan, String expected,
                                  String moTa) throws Exception {
        stubNewCustomer();
        stubForm("Nguyễn Văn An", "0922222222", null, null, matKhau, xacNhan);

        post();

        if (OK.equals(expected)) {
            verify(taiKhoanDao).saveCustomerRegistration(any(KhachHang.class), any(TaiKhoan.class));
            verify(response).sendRedirect(CTX + "/customer-shop");
        } else {
            assertShowsError(expected);
        }
    }

    // ==================================================================
    // B. QUY TẮC NGHIỆP VỤ (TC-REG-01, 10, 11, 12)
    // ==================================================================

    @Test
    @DisplayName("UT-REG-03: Khách mới -> tạo KhachHang + TaiKhoan (mật khẩu băm), tự đăng nhập")
    void newCustomer_createsAccountAndLogsIn() throws Exception {
        stubNewCustomer();
        stubForm("  Nguyễn Văn An  ", " 0922222222 ", null, null, "abc123", "abc123");

        post();

        ArgumentCaptor<KhachHang> khCaptor = ArgumentCaptor.forClass(KhachHang.class);
        ArgumentCaptor<TaiKhoan> tkCaptor = ArgumentCaptor.forClass(TaiKhoan.class);
        verify(taiKhoanDao).saveCustomerRegistration(khCaptor.capture(), tkCaptor.capture());

        KhachHang kh = khCaptor.getValue();
        assertEquals("KH03", kh.getMaKH());
        assertEquals("Nguyễn Văn An", kh.getHoTen(), "họ tên phải được trim");
        assertEquals("0922222222", kh.getSdt(), "SĐT phải được trim");
        assertEquals("", kh.getEmail(), "khách mới không nhập email -> chuỗi rỗng");
        assertEquals("", kh.getDiaChi(), "khách mới không nhập địa chỉ -> chuỗi rỗng");

        TaiKhoan tk = tkCaptor.getValue();
        assertEquals("KH03", tk.getMaKH());
        assertEquals("KH03", tk.getTenDangNhap(), "tên đăng nhập = mã khách hàng");
        assertEquals("KHACHHANG", tk.getVaiTro());
        assertNull(tk.getMaNV());
        assertNotEquals("abc123", tk.getMatKhau(), "không được lưu mật khẩu dạng rõ");
        assertTrue(tk.getMatKhau().startsWith("PBKDF2$"), "mật khẩu phải được băm PBKDF2 (FR-AUTH-05)");

        verify(session).setAttribute(eq("user"), any(TaiKhoan.class));
        verify(session).setAttribute("role", "KHACHHANG");
        verify(response).sendRedirect(CTX + "/customer-shop");
    }

    @Test
    @DisplayName("UT-REG-04: SĐT đã có tài khoản -> báo lỗi, không tạo mới (TC-REG-10)")
    void existingPhoneWithAccount_showsError() throws Exception {
        KhachHang existing = khachHang("KH01", "Khách cũ", "0911111111", "a@a.com", "HCM");
        when(khachHangDao.findByPhone("0911111111")).thenReturn(existing);
        when(taiKhoanDao.existsById("KH01")).thenReturn(true);
        stubForm("Nguyễn Văn An", "0911111111", null, null, "abc123", "abc123");

        post();

        assertShowsError("Số điện thoại này đã có tài khoản. Hãy đăng nhập.");
        verify(khachHangDao, never()).generateNextMaKH();
    }

    @Test
    @DisplayName("UT-REG-05: SĐT là khách chưa có tài khoản -> dùng lại MaKH, giữ email/địa chỉ cũ (TC-REG-11)")
    void existingCustomerWithoutAccount_reusesMaKHAndKeepsOldData() throws Exception {
        KhachHang existing = khachHang("KH02", "Khách cũ", "0933333333", "old@mail.com", "12 Lê Lợi");
        when(khachHangDao.findByPhone("0933333333")).thenReturn(existing);
        when(taiKhoanDao.existsById("KH02")).thenReturn(false);
        stubForm("Trần Thị B", "0933333333", null, null, "abc123", "abc123");

        post();

        ArgumentCaptor<KhachHang> khCaptor = ArgumentCaptor.forClass(KhachHang.class);
        ArgumentCaptor<TaiKhoan> tkCaptor = ArgumentCaptor.forClass(TaiKhoan.class);
        verify(taiKhoanDao).saveCustomerRegistration(khCaptor.capture(), tkCaptor.capture());

        assertEquals("KH02", khCaptor.getValue().getMaKH());
        assertEquals("old@mail.com", khCaptor.getValue().getEmail());
        assertEquals("12 Lê Lợi", khCaptor.getValue().getDiaChi());
        assertEquals("KH02", tkCaptor.getValue().getTenDangNhap());
        verify(khachHangDao, never()).generateNextMaKH();
    }

    @Test
    @DisplayName("UT-REG-06: Khách cũ nhập email/địa chỉ mới -> ưu tiên dữ liệu mới")
    void existingCustomer_newEmailAndAddressOverrideOldOnes() throws Exception {
        KhachHang existing = khachHang("KH02", "Khách cũ", "0933333333", "old@mail.com", "12 Lê Lợi");
        when(khachHangDao.findByPhone("0933333333")).thenReturn(existing);
        stubForm("Trần Thị B", "0933333333", " new@mail.com ", " 99 Nguyễn Huệ ", "abc123", "abc123");

        post();

        ArgumentCaptor<KhachHang> khCaptor = ArgumentCaptor.forClass(KhachHang.class);
        verify(taiKhoanDao).saveCustomerRegistration(khCaptor.capture(), any(TaiKhoan.class));
        assertEquals("new@mail.com", khCaptor.getValue().getEmail());
        assertEquals("99 Nguyễn Huệ", khCaptor.getValue().getDiaChi());
    }

    @Test
    @DisplayName("UT-REG-07: Lưu CSDL lỗi -> báo lỗi, không đăng nhập")
    void saveFails_showsErrorAndDoesNotLogin() throws Exception {
        stubNewCustomer();
        stubForm("Nguyễn Văn An", "0922222222", null, null, "abc123", "abc123");
        doThrow(new RuntimeException("Duplicate key"))
                .when(taiKhoanDao).saveCustomerRegistration(any(), any());

        assertDoesNotThrow(() -> post());

        verify(request).setAttribute("error", "Lỗi khi đăng ký khách hàng: Duplicate key");
        verify(request).getRequestDispatcher(REGISTER_JSP);
        verify(session, never()).setAttribute(anyString(), any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("UT-REG-08: AuthStore ném IOException -> bị bỏ qua, vẫn đăng ký thành công")
    void authStoreFails_isIgnored() throws Exception {
        stubNewCustomer();
        stubForm("Nguyễn Văn An", "0922222222", null, null, "abc123", "abc123");
        doThrow(new IOException("Không ghi được file"))
                .when(authStore).save(any(), any(), any(), any(), any());

        assertDoesNotThrow(() -> post());

        verify(response).sendRedirect(CTX + "/customer-shop");
    }

    // ==================================================================
    // C. GET + assertThrows
    // ==================================================================

    @Test
    @DisplayName("UT-REG-09: GET /register -> hiển thị form đăng ký")
    void get_showsRegisterForm() throws Exception {
        get();

        verify(request).getRequestDispatcher(REGISTER_JSP);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("UT-REG-10: Encoding không hỗ trợ -> UnsupportedEncodingException lan ra")
    void post_badEncoding_throws() throws Exception {
        doThrow(new UnsupportedEncodingException("UTF-8 bị từ chối"))
                .when(request).setCharacterEncoding("UTF-8");

        assertThrows(UnsupportedEncodingException.class, () -> post());

        verify(taiKhoanDao, never()).saveCustomerRegistration(any(), any());
    }

    @Test
    @DisplayName("UT-REG-11: Forward JSP lỗi -> ServletException lan ra")
    void forwardFails_throwsServletException() throws Exception {
        doThrow(new ServletException("Không render được JSP"))
                .when(dispatcher).forward(any(), any());

        ServletException ex = assertThrows(ServletException.class, () -> get());

        assertEquals("Không render được JSP", ex.getMessage());
    }

    @Test
    @DisplayName("UT-REG-12: Lỗi DB khi tra SĐT -> ngoại lệ lan ra (Controller không bắt)")
    void findByPhoneFails_propagates() throws Exception {
        stubForm("Nguyễn Văn An", "0922222222", null, null, "abc123", "abc123");
        when(khachHangDao.findByPhone(anyString())).thenThrow(new IllegalStateException("DB down"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> post());

        assertEquals("DB down", ex.getMessage());
        verify(taiKhoanDao, never()).saveCustomerRegistration(any(), any());
    }
}
