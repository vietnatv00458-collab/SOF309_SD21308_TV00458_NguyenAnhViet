package com.poly.sof309.controllers;

import dao.ChiTietGioHangDAO;
import dao.DatHangDAO;
import dao.GioHangDAO;
import dao.SanPhamDAO;
import dao.SizeDAO;
import entity.ChiTietGioHang;
import entity.DatHang;
import entity.GioHang;
import entity.SanPham;
import entity.Size;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import servlet.ChiTietGioHangServlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * =========================================================================
 * [Y3] UNIT TEST CONTROLLER: ChiTietGioHangServlet (/chitietgiohang)
 *
 * Kỹ thuật:
 *  - Mockito.mockConstruction: thay 5 DAO bằng mock => KHÔNG cần SQL Server.
 *  - Mock HttpServletRequest / HttpServletResponse / RequestDispatcher.
 *  - @ParameterizedTest + @CsvSource: kiểm tra giá trị biên của Form.
 *  - assertThrows: kiểm tra ngoại lệ lan ra ngoài Controller.
 *
 * Tài liệu tham chiếu: SPECIFICATIONS.md (FR-HD-05), TEST_CASES.md (TC-HD-12)
 * =========================================================================
 */
@DisplayName("ChiTietGioHangServlet - Unit Test (Y3)")
class ChiTietGioHangControllerTest {

    private static final String CTX = "/QuanLyCafe";
    private static final String FORM_JSP = "/views/chitietgiohang/form.jsp";
    private static final String LIST_JSP = "/views/chitietgiohang/list.jsp";
    /** Dùng trong @CsvSource để đánh dấu "không có lỗi, lưu thành công". */
    private static final String OK = "OK";

    private ChiTietGioHangServlet servlet;
    private ChiTietGioHangDAO chiTietDao;
    private DatHangDAO datHangDao;
    private GioHangDAO gioHangDao;
    private SizeDAO sizeDao;
    private SanPhamDAO sanPhamDao;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    private DatHang datHang;
    private GioHang gioHang;
    private SanPham sanPham;
    private Size size;

    @BeforeEach
    void setUp() {
        // Servlet tự "new" 5 DAO ở field => phải bật mockConstruction TRƯỚC khi new servlet.
        try (MockedConstruction<ChiTietGioHangDAO> ctMock = mockConstruction(ChiTietGioHangDAO.class);
             MockedConstruction<DatHangDAO> dhMock = mockConstruction(DatHangDAO.class);
             MockedConstruction<GioHangDAO> ghMock = mockConstruction(GioHangDAO.class);
             MockedConstruction<SizeDAO> sizeMock = mockConstruction(SizeDAO.class);
             MockedConstruction<SanPhamDAO> spMock = mockConstruction(SanPhamDAO.class)) {

            servlet = new ChiTietGioHangServlet();

            chiTietDao = ctMock.constructed().get(0);
            datHangDao = dhMock.constructed().get(0);
            gioHangDao = ghMock.constructed().get(0);
            sizeDao = sizeMock.constructed().get(0);
            sanPhamDao = spMock.constructed().get(0);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        when(request.getContextPath()).thenReturn(CTX);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        datHang = mock(DatHang.class);
        gioHang = mock(GioHang.class);
        sanPham = mock(SanPham.class);
        size = mock(Size.class);
    }

    // ------------------------------------------------------------------
    // Hàm hỗ trợ
    // ------------------------------------------------------------------

    private void get(String action) throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(action);
        servlet.service(request, response); // service() là public => gọi được doGet/doPost (protected)
    }

    private void post(String action) throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn(action);
        servlet.service(request, response);
    }

    private void stubForm(String maCTGH, String maGioHang, String maDatHang,
                          String maSP, String maSize, String soLuong, String donGia) {
        when(request.getParameter("maCTGH")).thenReturn(maCTGH);
        when(request.getParameter("maGioHang")).thenReturn(maGioHang);
        when(request.getParameter("maDatHang")).thenReturn(maDatHang);
        when(request.getParameter("maSP")).thenReturn(maSP);
        when(request.getParameter("maSize")).thenReturn(maSize);
        when(request.getParameter("soLuong")).thenReturn(soLuong);
        when(request.getParameter("donGia")).thenReturn(donGia);
    }

    /** Giả lập: giỏ hàng, sản phẩm, size đều tồn tại; mã CTGH chưa trùng. */
    private void stubAllReferencesExist() {
        when(gioHangDao.findById(anyString())).thenReturn(gioHang);
        when(sanPhamDao.findById(anyString())).thenReturn(sanPham);
        when(sizeDao.findById(anyString())).thenReturn(size);
        when(chiTietDao.existsById(anyInt())).thenReturn(false);
    }

    /** Kiểm tra Controller báo lỗi: set attribute "error" rồi forward về trang jsp. */
    private void assertShowsError(String message, String jsp) throws Exception {
        verify(request).setAttribute("error", message);
        verify(request).getRequestDispatcher(jsp);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // A. THÊM CHI TIẾT GIỎ HÀNG (POST action=create) - KIỂM TRA BIÊN FORM
    // ==================================================================

    @Test
    @DisplayName("UT-CTGH-01: Thêm hợp lệ -> lưu DB và redirect về danh sách")
    void insert_validForm_createsAndRedirects() throws Exception {
        stubAllReferencesExist();
        stubForm("1", "GH01", null, "SP01", "S", "2", "45000");

        post("create");

        verify(chiTietDao).create(any(ChiTietGioHang.class));
        verify(response).sendRedirect(CTX + "/chitietgiohang");
    }

    @Test
    @DisplayName("UT-CTGH-02: Dữ liệu được trim, maDatHang trống được lưu là null")
    void insert_trimsInputAndNullsBlankMaDatHang() throws Exception {
        stubAllReferencesExist();
        stubForm(" 7 ", " GH01 ", "   ", " SP01 ", " M ", " 2 ", " 45000.50 ");

        post("create");

        ArgumentCaptor<ChiTietGioHang> captor = ArgumentCaptor.forClass(ChiTietGioHang.class);
        verify(chiTietDao).create(captor.capture());
        ChiTietGioHang saved = captor.getValue();

        assertEquals(Integer.valueOf(7), saved.getMaCTGH());
        assertEquals("GH01", saved.getMaGioHang());
        assertNull(saved.getMaDatHang(), "maDatHang trống phải lưu null");
        assertEquals("SP01", saved.getMaSP());
        assertEquals("M", saved.getMaSize());
        assertEquals(Integer.valueOf(2), saved.getSoLuong());
        assertEquals(new BigDecimal("45000.50"), saved.getDonGia());
    }

    @ParameterizedTest(name = "[{index}] {6}")
    @CsvSource({
            // maCTGH, maGioHang, maSP, maSize, soLuong, donGia, moTa
            ",GH01,SP01,S,1,45000,maCTGH để trống (null)",
            "1,,SP01,S,1,45000,maGioHang để trống (null)",
            "1,GH01,,S,1,45000,maSP để trống (null)",
            "1,GH01,SP01,,1,45000,maSize để trống (null)",
            "1,GH01,SP01,S,,45000,soLuong để trống (null)",
            "1,GH01,SP01,S,1,,donGia để trống (null)",
            "'   ',GH01,SP01,S,1,45000,maCTGH chỉ gồm khoảng trắng",
            "1,GH01,SP01,S,1,'   ',donGia chỉ gồm khoảng trắng"
    })
    @DisplayName("UT-CTGH-03: Thiếu trường bắt buộc -> báo lỗi, không lưu")
    void insert_missingRequiredField_showsError(String maCTGH, String maGioHang, String maSP,
                                                String maSize, String soLuong, String donGia,
                                                String moTa) throws Exception {
        stubForm(maCTGH, maGioHang, null, maSP, maSize, soLuong, donGia);

        post("create");

        assertShowsError("Vui lòng nhập đầy đủ thông tin bắt buộc!", FORM_JSP);
        verify(chiTietDao, never()).create(any());
    }

    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource({
            // maCTGH, soLuong, donGia, moTa
            "abc,1,45000,maCTGH không phải số",
            "1.5,1,45000,maCTGH là số thập phân",
            "2147483648,1,45000,maCTGH vượt Integer.MAX_VALUE",
            "1,abc,45000,soLuong không phải số",
            "1,1.5,45000,soLuong là số thập phân",
            "1,1,abc,donGia không phải số",
            "1,1,'12,5',donGia dùng dấu phẩy"
    })
    @DisplayName("UT-CTGH-04: Sai kiểu số -> báo 'phải là số hợp lệ'")
    void insert_notANumber_showsError(String maCTGH, String soLuong, String donGia,
                                      String moTa) throws Exception {
        stubForm(maCTGH, "GH01", null, "SP01", "S", soLuong, donGia);

        post("create");

        assertShowsError("Mã CTGH và số lượng phải là số hợp lệ!", FORM_JSP);
        verify(chiTietDao, never()).create(any());
    }

    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource({
            // soLuong, donGia, ketQuaMongDoi (OK = lưu thành công), moTa
            "-1,45000,Số lượng phải lớn hơn 0!,soLuong = -1 (dưới biên)",
            "0,45000,Số lượng phải lớn hơn 0!,soLuong = 0 (biên không hợp lệ)",
            "1,45000,OK,soLuong = 1 (biên hợp lệ nhỏ nhất)",
            "2147483647,45000,OK,soLuong = Integer.MAX_VALUE",
            "1,-0.01,Đơn giá không được âm!,donGia = -0.01 (dưới biên)",
            "1,0,OK,donGia = 0 (biên hợp lệ)",
            "1,0.01,OK,donGia = 0.01 (trên biên)",
            "-1,-5,Số lượng phải lớn hơn 0!,cả hai sai => báo lỗi số lượng trước"
    })
    @DisplayName("UT-CTGH-05: Giá trị biên của soLuong và donGia khi thêm")
    void insert_boundaryValues(String soLuong, String donGia, String expected,
                               String moTa) throws Exception {
        stubAllReferencesExist();
        stubForm("1", "GH01", null, "SP01", "S", soLuong, donGia);

        post("create");

        if (OK.equals(expected)) {
            verify(chiTietDao).create(any(ChiTietGioHang.class));
            verify(response).sendRedirect(CTX + "/chitietgiohang");
        } else {
            assertShowsError(expected, FORM_JSP);
            verify(chiTietDao, never()).create(any());
        }
    }

    @ParameterizedTest(name = "[{index}] {7}")
    @CsvSource({
            // maDatHang, locked, datHangTonTai, gioHangTonTai, spTonTai, sizeTonTai, ctghDaTonTai, loiMongDoi
            "DH01, true,  true,  true,  true,  true,  false, Đơn đặt hàng đã thanh toán nên không thể thêm chi tiết vào đơn này.",
            "DH99, false, false, true,  true,  true,  false, Đơn đặt hàng không tồn tại!",
            ",     false, false, false, true,  true,  false, Giỏ hàng không tồn tại!",
            ",     false, false, true,  false, true,  false, Sản phẩm không tồn tại!",
            ",     false, false, true,  true,  false, false, Size không tồn tại!",
            ",     false, false, true,  true,  true,  true,  Mã chi tiết giỏ hàng đã tồn tại!"
    })
    @DisplayName("UT-CTGH-06: Quy tắc nghiệp vụ (tham chiếu không tồn tại, đơn đã khóa, trùng mã)")
    void insert_businessRules(String maDatHang, boolean locked, boolean datHangExists,
                              boolean gioHangExists, boolean sanPhamExists, boolean sizeExists,
                              boolean ctghExists, String expectedError) throws Exception {
        stubForm("1", "GH01", maDatHang, "SP01", "S", "1", "45000");
        when(datHangDao.isLocked(anyString())).thenReturn(locked);
        when(datHangDao.findById(anyString())).thenReturn(datHangExists ? datHang : null);
        when(gioHangDao.findById(anyString())).thenReturn(gioHangExists ? gioHang : null);
        when(sanPhamDao.findById(anyString())).thenReturn(sanPhamExists ? sanPham : null);
        when(sizeDao.findById(anyString())).thenReturn(sizeExists ? size : null);
        when(chiTietDao.existsById(anyInt())).thenReturn(ctghExists);

        post("create");

        assertShowsError(expectedError, FORM_JSP);
        verify(chiTietDao, never()).create(any());
    }

    // ==================================================================
    // B. CẬP NHẬT (POST action=update) - GIÁ TRỊ BIÊN
    // ==================================================================

    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource({
            "0,45000,Số lượng phải lớn hơn 0!,soLuong = 0",
            "1,-1,Đơn giá không được âm!,donGia = -1",
            "1,0,OK,biên hợp lệ soLuong=1 donGia=0",
            "3,45000,OK,giá trị bình thường"
    })
    @DisplayName("UT-CTGH-07: Giá trị biên khi cập nhật")
    void update_boundaryValues(String soLuong, String donGia, String expected,
                               String moTa) throws Exception {
        ChiTietGioHang existing = new ChiTietGioHang();
        existing.setMaCTGH(5);
        when(chiTietDao.findById(5)).thenReturn(existing);
        stubAllReferencesExist();
        stubForm("5", "GH01", null, "SP01", "S", soLuong, donGia);

        post("update");

        if (OK.equals(expected)) {
            verify(chiTietDao).update(existing);
            verify(response).sendRedirect(CTX + "/chitietgiohang");
        } else {
            assertShowsError(expected, FORM_JSP);
            verify(chiTietDao, never()).update(any());
        }
    }

    // ==================================================================
    // C. ĐƠN ĐÃ THANH TOÁN BỊ KHÓA (edit / delete)
    // ==================================================================

    @ParameterizedTest(name = "[{index}] action={0}")
    @CsvSource({
            "edit,Đơn đặt hàng đã thanh toán nên không thể chỉnh sửa chi tiết.",
            "delete,Đơn đặt hàng đã thanh toán nên không thể xóa chi tiết."
    })
    @DisplayName("UT-CTGH-08: Chi tiết thuộc đơn đã thanh toán -> không cho sửa/xóa")
    void editOrDelete_lockedOrder_showsErrorOnList(String action, String expected) throws Exception {
        ChiTietGioHang ct = new ChiTietGioHang();
        ct.setMaCTGH(5);
        ct.setMaDatHang("DH01");
        when(chiTietDao.findById(5)).thenReturn(ct);
        when(datHangDao.isLocked("DH01")).thenReturn(true);
        when(request.getParameter("id")).thenReturn("5");

        get(action);

        assertShowsError(expected, LIST_JSP);
        verify(chiTietDao, never()).delete(any());
    }

    @ParameterizedTest(name = "[{index}] id=\"{0}\"")
    @ValueSource(strings = {"abc", "1.5", "99999999999", " "})
    @DisplayName("UT-CTGH-09: Xóa với id không hợp lệ -> bỏ qua, không xóa, về danh sách")
    void delete_invalidId_isIgnored(String id) throws Exception {
        when(request.getParameter("id")).thenReturn(id);

        assertDoesNotThrow(() -> get("delete"));

        verify(chiTietDao, never()).delete(any());
        verify(response).sendRedirect(CTX + "/chitietgiohang");
    }

    // ==================================================================
    // D. assertThrows - NGOẠI LỆ LAN RA NGOÀI CONTROLLER
    // ==================================================================

    @Test
    @DisplayName("UT-CTGH-10: Lỗi DB khi xem danh sách -> ngoại lệ lan ra (không bị bắt)")
    void list_daoFails_propagatesRuntimeException() {
        when(chiTietDao.findAll()).thenThrow(new IllegalStateException("DB down"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> get(null));

        assertEquals("DB down", ex.getMessage());
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("UT-CTGH-11: Lỗi DB khi xóa -> ngoại lệ lan ra (Controller chỉ bắt NumberFormatException)")
    void delete_daoFails_propagatesRuntimeException() throws Exception {
        when(request.getParameter("id")).thenReturn("5");
        when(chiTietDao.findById(5)).thenReturn(null);
        doThrow(new RuntimeException("FK violation")).when(chiTietDao).delete(5);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> get("delete"));

        assertEquals("FK violation", ex.getMessage());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("UT-CTGH-12: forward JSP lỗi -> ServletException lan ra")
    void list_forwardFails_propagatesServletException() throws Exception {
        doThrow(new ServletException("Không render được JSP"))
                .when(dispatcher).forward(any(), any());

        ServletException ex = assertThrows(ServletException.class, () -> get(null));

        assertEquals("Không render được JSP", ex.getMessage());
    }

    @Test
    @DisplayName("UT-CTGH-13: Encoding không hỗ trợ -> UnsupportedEncodingException lan ra từ doPost")
    void post_badEncoding_propagatesIOException() throws Exception {
        doThrow(new UnsupportedEncodingException("UTF-8 bị từ chối"))
                .when(request).setCharacterEncoding("UTF-8");

        assertThrows(UnsupportedEncodingException.class, () -> post("create"));

        verify(chiTietDao, never()).create(any());
    }

    @Test
    @DisplayName("UT-CTGH-14: redirect lỗi mạng -> IOException lan ra")
    void post_unknownAction_redirectFails_propagatesIOException() throws Exception {
        doThrow(new IOException("Mất kết nối"))
                .when(response).sendRedirect(anyString());

        assertThrows(IOException.class, () -> post("khong-ton-tai"));
    }

    @Test
    @DisplayName("UT-CTGH-15 (đối chiếu): Lỗi DB khi THÊM bị Controller bắt và đổi thành thông báo")
    void insert_daoFails_isCaughtAndShownAsMessage() throws Exception {
        stubAllReferencesExist();
        stubForm("1", "GH01", null, "SP01", "S", "1", "45000");
        doThrow(new RuntimeException("Duplicate key")).when(chiTietDao).create(any(ChiTietGioHang.class));

        assertDoesNotThrow(() -> post("create"));

        assertShowsError("Lỗi khi thêm chi tiết giỏ hàng: Duplicate key", FORM_JSP);
    }
}
