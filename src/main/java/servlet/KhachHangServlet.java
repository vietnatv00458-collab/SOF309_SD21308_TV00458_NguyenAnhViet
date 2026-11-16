package servlet;

import dao.KhachHangDAO;
import dao.TaiKhoanDAO;
import entity.KhachHang;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/khachhang")
public class KhachHangServlet extends HttpServlet {

    private final KhachHangDAO dao = new KhachHangDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            listKhachHang(request, response);
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
                deleteKhachHang(request, response);
                break;
            default:
                listKhachHang(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            insertKhachHang(request, response);
        } else if ("update".equals(action)) {
            updateKhachHang(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/khachhang");
        }
    }

    private void listKhachHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<KhachHang> dsKhachHang = dao.findAll();
        request.setAttribute("dsKhachHang", dsKhachHang);
        request.getRequestDispatcher("/views/khachhang/list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/khachhang/form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maKH = request.getParameter("id");

        if (maKH == null || maKH.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/khachhang");
            return;
        }

        KhachHang khachHang = dao.findById(maKH.trim());

        if (khachHang == null) {
            response.sendRedirect(request.getContextPath() + "/khachhang");
            return;
        }

        request.setAttribute("khachHang", khachHang);
        request.getRequestDispatcher("/views/khachhang/form.jsp").forward(request, response);
    }

    private void insertKhachHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maKH = request.getParameter("maKH");
        String hoTen = request.getParameter("hoTen");
        String sdt = request.getParameter("sdt");
        String email = request.getParameter("email");
        String diaChi = request.getParameter("diaChi");
        boolean fromCustomerShop = isCustomerShopSource(request);

        if (maKH == null || maKH.trim().isEmpty()
                || hoTen == null || hoTen.trim().isEmpty()) {

            request.setAttribute("error", "Mã khách hàng và họ tên không được để trống!");
            request.getRequestDispatcher("/views/khachhang/form.jsp").forward(request, response);
            return;
        }

        if (dao.existsById(maKH.trim())) {
            request.setAttribute("error", "Mã khách hàng đã tồn tại!");
            request.getRequestDispatcher("/views/khachhang/form.jsp").forward(request, response);
            return;
        }

        try {
            KhachHang kh = new KhachHang();
            kh.setMaKH(maKH.trim());
            kh.setHoTen(hoTen.trim());
            kh.setSdt(sdt != null ? sdt.trim() : "");
            kh.setEmail(email != null ? email.trim() : "");
            kh.setDiaChi(diaChi != null ? diaChi.trim() : "");

            dao.create(kh);
            response.sendRedirect(fromCustomerShop
                    ? request.getContextPath() + "/customer-shop"
                    : request.getContextPath() + "/khachhang");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi thêm khách hàng: " + e.getMessage());
            request.getRequestDispatcher("/views/khachhang/form.jsp").forward(request, response);
        }
    }

    private void updateKhachHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maKH = request.getParameter("maKH");
        String hoTen = request.getParameter("hoTen");
        String sdt = request.getParameter("sdt");
        String email = request.getParameter("email");
        String diaChi = request.getParameter("diaChi");
        boolean fromCustomerShop = isCustomerShopSource(request);

        if (maKH == null || maKH.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/khachhang");
            return;
        }

        KhachHang kh = dao.findById(maKH.trim());

        if (kh == null) {
            response.sendRedirect(request.getContextPath() + "/khachhang");
            return;
        }

        if (hoTen == null || hoTen.trim().isEmpty()) {
            request.setAttribute("error", "Họ tên không được để trống!");
            request.setAttribute("khachHang", kh);
            request.getRequestDispatcher("/views/khachhang/form.jsp").forward(request, response);
            return;
        }

        try {
            kh.setHoTen(hoTen.trim());
            kh.setSdt(sdt != null ? sdt.trim() : "");
            kh.setEmail(email != null ? email.trim() : "");
            kh.setDiaChi(diaChi != null ? diaChi.trim() : "");

            dao.update(kh);
            response.sendRedirect(fromCustomerShop
                    ? request.getContextPath() + "/customer-shop"
                    : request.getContextPath() + "/khachhang");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi khi cập nhật khách hàng: " + e.getMessage());
            request.setAttribute("khachHang", kh);
            request.getRequestDispatcher("/views/khachhang/form.jsp").forward(request, response);
        }
    }

    private void deleteKhachHang(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String maKH = request.getParameter("id");
        boolean fromCustomerShop = isCustomerShopSource(request);

        if (maKH != null && !maKH.trim().isEmpty()) {
            maKH = maKH.trim();

            try {
                if (dao.hasTransactionalHistory(maKH)) {
                    String message = "Không thể xóa khách hàng vì còn lịch sử đơn hàng hoặc hóa đơn.";
                    if (fromCustomerShop) {
                        HttpSession session = request.getSession();
                        session.setAttribute("error", message);
                        response.sendRedirect(request.getContextPath() + "/customer-shop");
                    } else {
                        request.setAttribute("error", message);
                        listKhachHang(request, response);
                    }
                    return;
                }

                dao.delete(maKH);
                taiKhoanDAO.deleteByMaKH(maKH);

                response.sendRedirect(fromCustomerShop
                        ? request.getContextPath() + "/logout"
                        : request.getContextPath() + "/khachhang?msg=deleted");
                return;

            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/khachhang?error=delete_fail");
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/khachhang");
    }

    private boolean isCustomerShopSource(HttpServletRequest request) {
        String source = request.getParameter("source");
        return source != null && "customer-shop".equalsIgnoreCase(source.trim());
    }
}
