package com.poly.sof309.controllers;

import dao.LoginDAO;
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
import org.mockito.MockedConstruction;
import servlet.LoginServlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * =========================================================================
 * [Y3] UNIT TEST CONTROLLER: LoginServlet (/login)
 * Tham chiếu: SPECIFICATIONS.md FR-AUTH-01..08, NFR-03
 *             TEST_CASES.md TC-AUTH-01..20
 *
 * Lưu ý kỹ thuật: LoginServlet tạo "new LoginDAO()" ngay TRONG doPost,
 * nên mockConstruction phải đang mở khi gọi servlet.service(...).
 * =========================================================================
 */
@DisplayName("LoginServlet - Unit Test (Y3)")
class LoginControllerTest {

    private static final String CTX = "/QuanLyCafe";
    private static final String LOGIN_JSP = "/views/auth/login.jsp";

    private LoginServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new LoginServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn(CTX);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getSession()).thenReturn(session);
    }

    // ------------------------------------------------------------------
    // Hàm hỗ trợ
    // ------------------------------------------------------------------

    private void post() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        servlet.service(request, response);
    }

    private void stubLogin(String loginId, String sdt, String username, String password, String returnUrl) {
        when(request.getParameter("loginId")).thenReturn(loginId);
        when(request.getParameter("sdt")).thenReturn(sdt);
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("returnUrl")).thenReturn(returnUrl);
    }

    private TaiKhoan taiKhoan(String vaiTro) {
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("user01");
        tk.setVaiTro(vaiTro);
        return tk;
    }

    /** LoginDAO giả: đăng nhập luôn trả về "result" (null = đăng nhập thất bại). */
    private MockedConstruction<LoginDAO> daoReturning(TaiKhoan result) {
        return mockConstruction(LoginDAO.class,
                (mock, ctx) -> when(mock.login(any(), any())).thenReturn(result));
    }

    /** LoginDAO giả: đăng nhập luôn ném ngoại lệ. */
    private MockedConstruction<LoginDAO> daoThrowing(RuntimeException ex) {
        return mockConstruction(LoginDAO.class,
                (mock, ctx) -> when(mock.login(any(), any())).thenThrow(ex));
    }

    // ==================================================================
    // A. LẤY loginId / password TỪ FORM
    // ==================================================================

    @ParameterizedTest(name = "[{index}] {4}")
    @CsvSource({
            // loginId, sdt, username, loginIdMongDoi (NULL = null), moTa
            "0900000001,,,0900000001,dùng loginId",
            ",0911111111,,0911111111,loginId trống -> dùng sdt",
            ",,admin01,admin01,loginId và sdt trống -> dùng username",
            "'  nv01  ',,,nv01,loginId có khoảng trắng hai đầu được trim (TC-AUTH-12)",
            "0900000001,0911111111,admin01,0900000001,ưu tiên loginId > sdt > username",
            "'   ',0911111111,admin01,0911111111,loginId toàn khoảng trắng bị bỏ qua",
            ",,,NULL,không có thông tin đăng nhập nào -> null"
    })
    @DisplayName("UT-AUTH-01: Thứ tự ưu tiên và chuẩn hóa loginId")
    void loginId_resolutionOrder(String loginId, String sdt, String username,
                                 String expected, String moTa) throws Exception {
        String expectedLoginId = "NULL".equals(expected) ? null : expected;
        stubLogin(loginId, sdt, username, "Secret1", null);

        try (MockedConstruction<LoginDAO> daoMock = daoReturning(taiKhoan("Admin"))) {
            post();
            verify(daoMock.constructed().get(0)).login(expectedLoginId, "Secret1");
        }
    }

    @ParameterizedTest(name = "[{index}] password=\"{0}\" -> {1}")
    @CsvSource({
            "'  Secret1  ',Secret1",
            "'   ',NULL",
            ",NULL"
    })
    @DisplayName("UT-AUTH-02: Mật khẩu được trim; trống/khoảng trắng -> null (TC-AUTH-08)")
    void password_isTrimmedAndBlankBecomesNull(String password, String expected) throws Exception {
        String expectedPassword = "NULL".equals(expected) ? null : expected;
        stubLogin("admin01", null, null, password, null);

        try (MockedConstruction<LoginDAO> daoMock = daoReturning(null)) {
            post();
            verify(daoMock.constructed().get(0)).login("admin01", expectedPassword);
        }
    }

    // ==================================================================
    // B. ĐĂNG NHẬP THÀNH CÔNG - ĐIỀU HƯỚNG THEO VAI TRÒ (TC-AUTH-01..05)
    // ==================================================================

    @ParameterizedTest(name = "[{index}] vaiTro={0} -> {1}")
    @CsvSource({
            "Admin,/dashboard",
            "Quản lý,/dashboard",
            "manager,/dashboard",
            "NhanVien,/nhanvien-home",
            "staff,/nhanvien-home",
            "GiaoHang,/giaohang-home",
            "shipper,/giaohang-home",
            "KHACHHANG,/customer-shop",
            "customer,/customer-shop"
    })
    @DisplayName("UT-AUTH-03: Thành công -> lưu session và chuyển tới trang chủ của vai trò")
    void success_redirectsByRole(String vaiTro, String expectedPath) throws Exception {
        TaiKhoan tk = taiKhoan(vaiTro);
        stubLogin("user01", null, null, "Secret1", null);

        try (MockedConstruction<LoginDAO> ignored = daoReturning(tk)) {
            post();
        }

        verify(session).setAttribute("user", tk);
        verify(session).setAttribute("role", vaiTro);
        verify(response).sendRedirect(CTX + expectedPath);
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource({
            // loginId, luuLoginPhone, moTa
            "1234567,false,7 chữ số -> KHÔNG phải SĐT (TC-AUTH-13)",
            "12345678,true,8 chữ số (biên dưới)",
            "0900000001,true,SĐT 10 chữ số",
            "123456789012345,true,15 chữ số (biên trên)",
            "1234567890123456,false,16 chữ số (vượt biên)",
            "09000000a1,false,lẫn chữ cái",
            "nv01,false,tên đăng nhập"
    })
    @DisplayName("UT-AUTH-04: Lưu loginPhone khi loginId là 8-15 chữ số (TC-AUTH-14)")
    void success_loginPhoneBoundary(String loginId, boolean saved, String moTa) throws Exception {
        stubLogin(loginId, null, null, "Secret1", null);

        try (MockedConstruction<LoginDAO> ignored = daoReturning(taiKhoan("Admin"))) {
            post();
        }

        if (saved) {
            verify(session).setAttribute("loginPhone", loginId);
            verify(session, never()).removeAttribute("loginPhone");
        } else {
            verify(session).removeAttribute("loginPhone");
            verify(session, never()).setAttribute(eq("loginPhone"), any());
        }
    }

    // ==================================================================
    // C. returnUrl - CHỐNG OPEN REDIRECT (TC-AUTH-15..18, NFR-03)
    // ==================================================================

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource({
            // returnUrl, duongDanSauContext, moTa
            "/customer-shop,/customer-shop,returnUrl nội bộ hợp lệ (TC-AUTH-15)",
            "/customer-shop/history,/customer-shop/history,returnUrl nội bộ có đường dẫn con",
            "'  /orders  ',/orders,returnUrl có khoảng trắng hai đầu được trim",
            "//evil.com,/customer-shop,open redirect dạng // bị chặn (TC-AUTH-16)",
            "http://evil.com,/customer-shop,URL tuyệt đối bị chặn (TC-AUTH-17)",
            "evil.com,/customer-shop,không bắt đầu bằng / bị chặn",
            "'   ',/customer-shop,chỉ khoảng trắng -> về trang chủ khách",
            ",/customer-shop,không có returnUrl -> về trang chủ khách"
    })
    @DisplayName("UT-AUTH-05: Khách hàng + returnUrl -> chỉ chấp nhận đường dẫn nội bộ")
    void customer_returnUrlIsSanitized(String returnUrl, String expectedPath, String moTa) throws Exception {
        stubLogin("KH01", null, null, "Secret1", returnUrl);

        try (MockedConstruction<LoginDAO> ignored = daoReturning(taiKhoan("KHACHHANG"))) {
            post();
        }

        verify(response).sendRedirect(CTX + expectedPath);
    }

    @ParameterizedTest(name = "[{index}] vaiTro={0}")
    @CsvSource({
            "Admin,/dashboard",
            "NhanVien,/nhanvien-home",
            "GiaoHang,/giaohang-home"
    })
    @DisplayName("UT-AUTH-06: Vai trò nội bộ bỏ qua returnUrl (TC-AUTH-18)")
    void staff_ignoresReturnUrl(String vaiTro, String expectedPath) throws Exception {
        stubLogin("user01", null, null, "Secret1", "/customer-shop");

        try (MockedConstruction<LoginDAO> ignored = daoReturning(taiKhoan(vaiTro))) {
            post();
        }

        verify(response).sendRedirect(CTX + expectedPath);
    }

    // ==================================================================
    // D. ĐĂNG NHẬP THẤT BẠI (TC-AUTH-06..09, 20)
    // ==================================================================

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource({
            "admin01,saiMatKhau,sai mật khẩu (TC-AUTH-06)",
            "khongco123,abc123,tài khoản không tồn tại (TC-AUTH-07)",
            "' OR 1=1 --,x,chuỗi SQL injection chỉ được coi là dữ liệu (TC-AUTH-20)"
    })
    @DisplayName("UT-AUTH-07: DAO trả null -> báo sai thông tin, KHÔNG tạo session")
    void failure_showsErrorAndNoSession(String loginId, String password, String moTa) throws Exception {
        stubLogin(loginId, null, null, password, null);

        try (MockedConstruction<LoginDAO> ignored = daoReturning(null)) {
            post();
        }

        verify(request).setAttribute("error", "Sai số điện thoại hoặc mật khẩu!");
        verify(request).getRequestDispatcher(LOGIN_JSP);
        verify(dispatcher).forward(request, response);
        verify(request, never()).getSession();
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("UT-AUTH-08: Lỗi CSDL -> báo lỗi kết nối, không văng ngoại lệ")
    void dbError_showsConnectionMessage() throws Exception {
        stubLogin("admin01", null, null, "Secret1", null);

        try (MockedConstruction<LoginDAO> ignored = daoThrowing(new IllegalStateException("DB down"))) {
            assertDoesNotThrow(() -> post());
        }

        verify(request).setAttribute("error",
                "Không thể đăng nhập lúc này. Kiểm tra kết nối cơ sở dữ liệu.");
        verify(request).getRequestDispatcher(LOGIN_JSP);
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("UT-AUTH-09: Quirk - lỗi redirect (IOException) bị bắt và báo nhầm là lỗi CSDL")
    void redirectIOException_isReportedAsDatabaseError() throws Exception {
        stubLogin("admin01", null, null, "Secret1", null);
        doThrow(new IOException("Mất kết nối")).when(response).sendRedirect(anyString());

        try (MockedConstruction<LoginDAO> ignored = daoReturning(taiKhoan("Admin"))) {
            assertDoesNotThrow(() -> post());
        }

        verify(request).setAttribute("error",
                "Không thể đăng nhập lúc này. Kiểm tra kết nối cơ sở dữ liệu.");
    }

    // ==================================================================
    // E. GET + assertThrows
    // ==================================================================

    @Test
    @DisplayName("UT-AUTH-10: GET /login -> hiển thị form đăng nhập")
    void get_showsLoginForm() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        servlet.service(request, response);

        verify(request).getRequestDispatcher(LOGIN_JSP);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("UT-AUTH-11: Encoding không hỗ trợ -> UnsupportedEncodingException lan ra")
    void post_badEncoding_throws() throws Exception {
        doThrow(new UnsupportedEncodingException("UTF-8 bị từ chối"))
                .when(request).setCharacterEncoding("UTF-8");

        assertThrows(UnsupportedEncodingException.class, () -> post());
    }

    @Test
    @DisplayName("UT-AUTH-12: Forward trang lỗi cũng hỏng -> ServletException lan ra (forward được gọi 2 lần)")
    void forwardFails_throwsServletException() throws Exception {
        stubLogin("admin01", null, null, "sai", null);
        doThrow(new ServletException("Không render được JSP"))
                .when(dispatcher).forward(any(), any());

        try (MockedConstruction<LoginDAO> ignored = daoReturning(null)) {
            ServletException ex = assertThrows(ServletException.class, () -> post());
            assertEquals("Không render được JSP", ex.getMessage());
        }

        // Lần 1 trong try (đăng nhập sai), bị catch(Exception) bắt, lần 2 trong catch -> lan ra.
        verify(dispatcher, times(2)).forward(any(), any());
    }
}
