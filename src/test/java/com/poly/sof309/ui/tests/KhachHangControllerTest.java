package com.poly.sof309.controllers;

import dao.KhachHangDAO;
import dao.TaiKhoanDAO;
import entity.KhachHang;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import servlet.KhachHangServlet;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * =========================================================================
 * [Y3] UNIT TEST CONTROLLER: KhachHangServlet (/khachhang)
 * Tham chiếu: SPECIFICATIONS.md FR-KH-01..04  |  TEST_CASES.md TC-KH-01..09
 * =========================================================================
 */
@DisplayName("KhachHangServlet - Unit Test (Y3)")
class KhachHangControllerTest {

    private static final String CTX = "/QuanLyCafe";
    private static final String FORM_JSP = "/views/khachhang/form.jsp";
    private static final String LIST_JSP = "/views/khachhang/list.jsp";
    private static final String OK = "OK";
    private static final String HAS_HISTORY_MSG =
            "Không thể xóa khách hàng vì còn lịch sử đơn hàng hoặc hóa đơn.";

    private KhachHangServlet servlet;
    private KhachHangDAO dao;
    private TaiKhoanDAO taiKhoanDao;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        try (MockedConstruction<KhachHangDAO> khMock = mockConstruction(KhachHangDAO.class);
             MockedConstruction<TaiKhoanDAO> tkMock = mockConstruction(TaiKhoanDAO.class)) {

            servlet = new KhachHangServlet();
            dao = khMock.constructed().get(0);
            taiKhoanDao = tkMock.constructed().get(0);
        }

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

    private void get(String action) throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(action);
        servlet.service(request, response);
    }

    private void post(String action) throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn(action);
        servlet.service(request, response);
    }

    private void stubForm(String maKH, String hoTen, String sdt, String email, String diaChi) {
        when(request.getParameter("maKH")).thenReturn(maKH);
        when(request.getParameter("hoTen")).thenReturn(hoTen);
        when(request.getParameter("sdt")).thenReturn(sdt);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("diaChi")).thenReturn(diaChi);
    }

    private KhachHang khachHang(String ma, String hoTen) {
        KhachHang kh = new KhachHang();
        kh.setMaKH(ma);
        kh.setHoTen(hoTen);
        kh.setSdt("0911111111");
        kh.setEmail("a@a.com");
        kh.setDiaChi("HCM");
        return kh;
    }

    private void assertShowsError(String message, String jsp) throws Exception {
        verify(request).setAttribute("error", message);
        verify(request).getRequestDispatcher(jsp);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // A. XEM DANH SÁCH / FORM (TC-KH-01)
    // ==================================================================

    @Test
    @DisplayName("UT-KH-01: Không có action -> hiển thị danh sách khách hàng")
    void list_showsAllCustomers() throws Exception {
        List<KhachHang> ds = List.of(khachHang("KH01", "An"), khachHang("KH02", "Bình"));
        when(dao.findAll()).thenReturn(ds);

        get(null);

        verify(request).setAttribute("dsKhachHang", ds);
        verify(request).getRequestDispatcher(LIST_JSP);
        verify(dispatcher).forward(request, response);
    }

    @ParameterizedTest(name = "[{index}] action=\"{0}\"")
    @ValueSource(strings = {"abc", "DELETE", "Add", " "})
    @DisplayName("UT-KH-02: action lạ/viết hoa/khoảng trắng -> rơi về danh sách, không xóa")
    void unknownGetAction_fallsBackToList(String action) throws Exception {
        get(action);

        verify(request).getRequestDispatcher(LIST_JSP);
        verify(dao, never()).delete(anyString());
    }

    @Test
    @DisplayName("UT-KH-03: action=add -> hiển thị form trống")
    void add_showsEmptyForm() throws Exception {
        get("add");

        verify(request).getRequestDispatcher(FORM_JSP);
        verify(dispatcher).forward(request, response);
        verify(request, never()).setAttribute(eq("khachHang"), any());
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @CsvSource({
            ",id thiếu (null)",
            "'   ',id chỉ khoảng trắng",
            "KH999,không tìm thấy khách hàng"
    })
    @DisplayName("UT-KH-04: Sửa với id sai/không tồn tại -> redirect về danh sách")
    void edit_invalidOrMissing_redirectsToList(String id, String moTa) throws Exception {
        when(request.getParameter("id")).thenReturn(id);
        when(dao.findById(anyString())).thenReturn(null);

        get("edit");

        verify(response).sendRedirect(CTX + "/khachhang");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("UT-KH-05: Sửa với id hợp lệ -> hiển thị form kèm dữ liệu (TC-KH-05)")
    void edit_found_showsFormWithData() throws Exception {
        KhachHang kh = khachHang("KH02", "Bình");
        when(request.getParameter("id")).thenReturn(" KH02 ");
        when(dao.findById("KH02")).thenReturn(kh);

        get("edit");

        verify(request).setAttribute("khachHang", kh);
        verify(request).getRequestDispatcher(FORM_JSP);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // B. THÊM KHÁCH HÀNG (TC-KH-02..04, 09)
    // ==================================================================

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource({
            // maKH, hoTen, moTa
            ",Nguyễn Văn An,maKH để trống (null)",
            "'   ',Nguyễn Văn An,maKH chỉ gồm khoảng trắng",
            "KH90,,hoTen để trống (null)",
            "KH90,'   ',hoTen chỉ gồm khoảng trắng",
            ",,cả hai để trống"
    })
    @DisplayName("UT-KH-06: Thiếu mã hoặc họ tên -> báo lỗi, không lưu (TC-KH-03)")
    void create_missingRequired_showsError(String maKH, String hoTen, String moTa) throws Exception {
        stubForm(maKH, hoTen, "0900000000", "a@a.com", "HCM");

        post("create");

        assertShowsError("Mã khách hàng và họ tên không được để trống!", FORM_JSP);
        verify(dao, never()).create(any());
    }

    @Test
    @DisplayName("UT-KH-07: Mã khách hàng trùng -> báo lỗi (TC-KH-04)")
    void create_duplicateMaKH_showsError() throws Exception {
        stubForm("KH01", "Nguyễn Văn An", "0900000000", null, null);
        when(dao.existsById("KH01")).thenReturn(true);

        post("create");

        assertShowsError("Mã khách hàng đã tồn tại!", FORM_JSP);
        verify(dao, never()).create(any());
    }

    @Test
    @DisplayName("UT-KH-08: Thêm hợp lệ -> trim dữ liệu, trường tùy chọn null => chuỗi rỗng (TC-KH-02)")
    void create_valid_trimsAndDefaultsToEmpty() throws Exception {
        stubForm(" KH90 ", "  Nguyễn Văn An  ", null, null, null);
        when(dao.existsById("KH90")).thenReturn(false);

        post("create");

        ArgumentCaptor<KhachHang> captor = ArgumentCaptor.forClass(KhachHang.class);
        verify(dao).create(captor.capture());
        KhachHang saved = captor.getValue();
        assertEquals("KH90", saved.getMaKH());
        assertEquals("Nguyễn Văn An", saved.getHoTen());
        assertEquals("", saved.getSdt());
        assertEquals("", saved.getEmail());
        assertEquals("", saved.getDiaChi());
        verify(response).sendRedirect(CTX + "/khachhang");
    }

    @ParameterizedTest(name = "[{index}] source=\"{0}\"")
    @CsvSource({
            ",/khachhang",
            "customer-shop,/customer-shop",
            "'  CUSTOMER-SHOP  ',/customer-shop",
            "admin,/khachhang"
    })
    @DisplayName("UT-KH-09: Thêm xong -> redirect theo nguồn (source=customer-shop về cửa hàng)")
    void create_redirectDependsOnSource(String source, String expectedPath) throws Exception {
        stubForm("KH90", "Nguyễn Văn An", "0900000000", null, null);
        when(request.getParameter("source")).thenReturn(source);

        post("create");

        verify(dao).create(any(KhachHang.class));
        verify(response).sendRedirect(CTX + expectedPath);
    }

    @Test
    @DisplayName("UT-KH-10: Lỗi DB khi thêm -> Controller bắt và hiện thông báo")
    void create_daoFails_showsMessage() throws Exception {
        stubForm("KH90", "Nguyễn Văn An", "0900000000", null, null);
        doThrow(new RuntimeException("Họ tên quá dài")).when(dao).create(any(KhachHang.class));

        assertDoesNotThrow(() -> post("create"));

        assertShowsError("Lỗi khi thêm khách hàng: Họ tên quá dài", FORM_JSP);
    }

    // ==================================================================
    // C. CẬP NHẬT (TC-KH-05, 06)
    // ==================================================================

    @ParameterizedTest(name = "[{index}] {1}")
    @CsvSource({
            ",maKH null",
            "'   ',maKH chỉ khoảng trắng",
            "KH999,không tìm thấy khách hàng"
    })
    @DisplayName("UT-KH-11: Cập nhật với mã sai/không tồn tại -> redirect về danh sách")
    void update_invalidOrMissing_redirectsToList(String maKH, String moTa) throws Exception {
        stubForm(maKH, "Tên mới", null, null, null);
        when(dao.findById(anyString())).thenReturn(null);

        post("update");

        verify(response).sendRedirect(CTX + "/khachhang");
        verify(dao, never()).update(any());
    }

    @ParameterizedTest(name = "[{index}] hoTen=\"{0}\"")
    @CsvSource({
            ",null",
            "'   ',chỉ khoảng trắng"
    })
    @DisplayName("UT-KH-12: Cập nhật với họ tên trống -> báo lỗi, giữ dữ liệu cũ (TC-KH-06)")
    void update_blankName_showsError(String hoTen, String moTa) throws Exception {
        KhachHang kh = khachHang("KH02", "Bình");
        when(dao.findById("KH02")).thenReturn(kh);
        stubForm("KH02", hoTen, null, null, null);

        post("update");

        verify(request).setAttribute("khachHang", kh);
        assertShowsError("Họ tên không được để trống!", FORM_JSP);
        verify(dao, never()).update(any());
        assertEquals("Bình", kh.getHoTen(), "dữ liệu cũ không được bị thay đổi");
    }

    @Test
    @DisplayName("UT-KH-13: Cập nhật hợp lệ -> cập nhật các trường và redirect (TC-KH-05)")
    void update_valid_updatesAndRedirects() throws Exception {
        KhachHang kh = khachHang("KH02", "Bình");
        when(dao.findById("KH02")).thenReturn(kh);
        stubForm("KH02", "  Bình Mới ", " 0988888888 ", " b@b.com ", " 99 Nguyễn Huệ ");

        post("update");

        verify(dao).update(kh);
        assertEquals("Bình Mới", kh.getHoTen());
        assertEquals("0988888888", kh.getSdt());
        assertEquals("b@b.com", kh.getEmail());
        assertEquals("99 Nguyễn Huệ", kh.getDiaChi());
        verify(response).sendRedirect(CTX + "/khachhang");
    }

    @Test
    @DisplayName("UT-KH-14: Lỗi DB khi cập nhật -> Controller bắt và hiện thông báo")
    void update_daoFails_showsMessage() throws Exception {
        KhachHang kh = khachHang("KH02", "Bình");
        when(dao.findById("KH02")).thenReturn(kh);
        stubForm("KH02", "Bình Mới", null, null, null);
        doThrow(new RuntimeException("Timeout")).when(dao).update(any(KhachHang.class));

        assertDoesNotThrow(() -> post("update"));

        assertShowsError("Lỗi khi cập nhật khách hàng: Timeout", FORM_JSP);
    }

    @ParameterizedTest(name = "[{index}] action=\"{0}\"")
    @CsvSource({
            ",null",
            "abc,hành động lạ"
    })
    @DisplayName("UT-KH-15: POST với action không hợp lệ -> redirect về danh sách")
    void post_unknownAction_redirectsToList(String action, String moTa) throws Exception {
        post(action);

        verify(response).sendRedirect(CTX + "/khachhang");
        verify(dao, never()).create(any());
        verify(dao, never()).update(any());
    }

    // ==================================================================
    // D. XÓA KHÁCH HÀNG (TC-KH-07, 08)
    // ==================================================================

    @Test
    @DisplayName("UT-KH-16: Xóa khách chưa có giao dịch -> xóa khách rồi xóa tài khoản (TC-KH-07)")
    void delete_noHistory_deletesCustomerThenAccount() throws Exception {
        when(request.getParameter("id")).thenReturn(" KH90 ");
        when(dao.hasTransactionalHistory("KH90")).thenReturn(false);

        get("delete");

        InOrder order = inOrder(dao, taiKhoanDao);
        order.verify(dao).delete("KH90");
        order.verify(taiKhoanDao).deleteByMaKH("KH90");
        verify(response).sendRedirect(CTX + "/khachhang?msg=deleted");
    }

    @Test
    @DisplayName("UT-KH-17: Quản lý tự xóa từ cửa hàng (source=customer-shop) -> đăng xuất")
    void delete_fromCustomerShop_redirectsToLogout() throws Exception {
        when(request.getParameter("id")).thenReturn("KH90");
        when(request.getParameter("source")).thenReturn("customer-shop");

        get("delete");

        verify(dao).delete("KH90");
        verify(response).sendRedirect(CTX + "/logout");
    }

    @Test
    @DisplayName("UT-KH-18: Khách còn lịch sử giao dịch (quản trị) -> báo lỗi, KHÔNG xóa (TC-KH-08)")
    void delete_withHistory_admin_showsErrorOnList() throws Exception {
        when(request.getParameter("id")).thenReturn("KH01");
        when(dao.hasTransactionalHistory("KH01")).thenReturn(true);

        get("delete");

        assertShowsError(HAS_HISTORY_MSG, LIST_JSP);
        verify(dao, never()).delete(anyString());
        verify(taiKhoanDao, never()).deleteByMaKH(anyString());
    }

    @Test
    @DisplayName("UT-KH-19: Khách còn lịch sử giao dịch (từ cửa hàng) -> lỗi lưu vào session")
    void delete_withHistory_fromShop_putsErrorInSession() throws Exception {
        when(request.getParameter("id")).thenReturn("KH01");
        when(request.getParameter("source")).thenReturn("customer-shop");
        when(dao.hasTransactionalHistory("KH01")).thenReturn(true);

        get("delete");

        verify(session).setAttribute("error", HAS_HISTORY_MSG);
        verify(response).sendRedirect(CTX + "/customer-shop");
        verify(dao, never()).delete(anyString());
    }

    @ParameterizedTest(name = "[{index}] id=\"{0}\"")
    @CsvSource({
            ",null",
            "'   ',chỉ khoảng trắng"
    })
    @DisplayName("UT-KH-20: Xóa với id trống -> redirect về danh sách, không xóa")
    void delete_blankId_redirectsToList(String id, String moTa) throws Exception {
        when(request.getParameter("id")).thenReturn(id);

        get("delete");

        verify(response).sendRedirect(CTX + "/khachhang");
        verify(dao, never()).delete(anyString());
    }

    @Test
    @DisplayName("UT-KH-21: Lỗi DB khi xóa -> bị bắt, redirect error=delete_fail, không xóa tài khoản")
    void delete_daoFails_redirectsWithError() throws Exception {
        when(request.getParameter("id")).thenReturn("KH90");
        doThrow(new RuntimeException("FK violation")).when(dao).delete("KH90");

        assertDoesNotThrow(() -> get("delete"));

        verify(response).sendRedirect(CTX + "/khachhang?error=delete_fail");
        verify(taiKhoanDao, never()).deleteByMaKH(anyString());
    }

    // ==================================================================
    // E. assertThrows - NGOẠI LỆ LAN RA NGOÀI CONTROLLER
    // ==================================================================

    @Test
    @DisplayName("UT-KH-22: Lỗi DB khi xem danh sách -> ngoại lệ lan ra (Controller không bắt)")
    void list_daoFails_propagates() {
        when(dao.findAll()).thenThrow(new IllegalStateException("DB down"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> get(null));

        assertEquals("DB down", ex.getMessage());
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("UT-KH-23: Lỗi DB khi mở form sửa -> ngoại lệ lan ra")
    void edit_daoFails_propagates() {
        when(request.getParameter("id")).thenReturn("KH01");
        when(dao.findById("KH01")).thenThrow(new IllegalStateException("DB down"));

        assertThrows(IllegalStateException.class, () -> get("edit"));
    }

    @Test
    @DisplayName("UT-KH-24: Forward JSP lỗi -> ServletException lan ra")
    void forwardFails_throwsServletException() throws Exception {
        doThrow(new ServletException("Không render được JSP"))
                .when(dispatcher).forward(any(), any());

        ServletException ex = assertThrows(ServletException.class, () -> get(null));

        assertEquals("Không render được JSP", ex.getMessage());
    }

    @Test
    @DisplayName("UT-KH-25: Encoding không hỗ trợ -> UnsupportedEncodingException lan ra từ doPost")
    void post_badEncoding_throws() throws Exception {
        doThrow(new UnsupportedEncodingException("UTF-8 bị từ chối"))
                .when(request).setCharacterEncoding("UTF-8");

        assertThrows(UnsupportedEncodingException.class, () -> post("create"));

        verify(dao, never()).create(any());
    }
}
