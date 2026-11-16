package servlet;

import dao.GioHangDAO;
import dao.KhachHangDAO;
import entity.GioHang;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/giohang")
public class GioHangServlet extends HttpServlet {

    private final GioHangDAO dao = new GioHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listGioHang(request, response);
            return;
        }

        switch (action) {
            case "add":
                showAddForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteGioHang(request, response);
                break;
            default:
                listGioHang(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertGioHang(request, response);
        } else if ("update".equals(action)) {
            updateGioHang(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/giohang");
        }
    }

    // =========================
    // DANH SÁCH
    // =========================
    private void listGioHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<GioHang> dsGioHang = dao.findAll();
        request.setAttribute("dsGioHang", dsGioHang);
        request.getRequestDispatcher("/views/giohang/list.jsp").forward(request, response);
    }

    // =========================
    // FORM THÊM
    // =========================
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
    }

    // =========================
    // FORM SỬA
    // =========================
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maGioHang = request.getParameter("id");

        if (maGioHang == null || maGioHang.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/giohang");
            return;
        }

        GioHang gioHang = dao.findById(maGioHang.trim());

        if (gioHang == null) {
            response.sendRedirect(request.getContextPath() + "/giohang");
            return;
        }

        request.setAttribute("gioHang", gioHang);
        request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
    }

    // =========================
    // THÊM GIỎ HÀNG
    // =========================
    private void insertGioHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maGioHang = request.getParameter("maGioHang");
        String maKH = request.getParameter("maKH");
        String ngayTaoStr = request.getParameter("ngayTao");

        if (maGioHang == null || maGioHang.trim().isEmpty()
                || maKH == null || maKH.trim().isEmpty()) {

            request.setAttribute("error", "Mã giỏ hàng và mã khách hàng không được để trống!");
            request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
            return;
        }

        if (dao.existsById(maGioHang.trim())) {
            request.setAttribute("error", "Mã giỏ hàng đã tồn tại!");
            request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
            return;
        }

        if (!khachHangDAO.existsById(maKH.trim())) {
            request.setAttribute("error", "Mã khách hàng không tồn tại!");
            request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
            return;
        }

        try {
            GioHang gh = new GioHang();
            gh.setMaGioHang(maGioHang.trim());
            gh.setMaKH(maKH.trim());

            if (ngayTaoStr != null && !ngayTaoStr.trim().isEmpty()) {
                gh.setNgayTao(Timestamp.valueOf(ngayTaoStr.trim().replace("T", " ") + ":00"));
            } else {
                gh.setNgayTao(new Timestamp(System.currentTimeMillis()));
            }

            dao.create(gh);

            response.sendRedirect(request.getContextPath() + "/giohang");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm giỏ hàng: " + e.getMessage());
            request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
        }
    }

    // =========================
    // CẬP NHẬT GIỎ HÀNG
    // =========================
    private void updateGioHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maGioHang = request.getParameter("maGioHang");
        String maKH = request.getParameter("maKH");
        String ngayTaoStr = request.getParameter("ngayTao");

        if (maGioHang == null || maGioHang.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/giohang");
            return;
        }

        GioHang gh = dao.findById(maGioHang.trim());

        if (gh == null) {
            response.sendRedirect(request.getContextPath() + "/giohang");
            return;
        }

        if (maKH == null || maKH.trim().isEmpty()) {
            request.setAttribute("error", "Mã khách hàng không được để trống!");
            request.setAttribute("gioHang", gh);
            request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
            return;
        }

        if (!khachHangDAO.existsById(maKH.trim())) {
            request.setAttribute("error", "Mã khách hàng không tồn tại!");
            request.setAttribute("gioHang", gh);
            request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
            return;
        }

        try {
            gh.setMaKH(maKH.trim());

            if (ngayTaoStr != null && !ngayTaoStr.trim().isEmpty()) {
                gh.setNgayTao(Timestamp.valueOf(ngayTaoStr.trim().replace("T", " ") + ":00"));
            }

            dao.update(gh);

            response.sendRedirect(request.getContextPath() + "/giohang");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật giỏ hàng: " + e.getMessage());
            request.setAttribute("gioHang", gh);
            request.getRequestDispatcher("/views/giohang/form.jsp").forward(request, response);
        }
    }

    // =========================
    // XÓA GIỎ HÀNG
    // =========================
    private void deleteGioHang(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String maGioHang = request.getParameter("id");

        if (maGioHang != null && !maGioHang.trim().isEmpty()) {
            try {
                dao.delete(maGioHang.trim());
            } catch (RuntimeException e) {
                request.setAttribute("error", "Không thể xóa giỏ hàng: " + e.getMessage());
                listGioHang(request, response);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/giohang");
    }
}
