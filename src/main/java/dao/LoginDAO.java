package dao;

import entity.KhachHang;
import entity.NhanVien;
import entity.TaiKhoan;
import utils.AuthStore;
import utils.PasswordUtils;

public class LoginDAO {

    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final AuthStore authStore = new AuthStore();

    public TaiKhoan login(String loginId, String password) {
        boolean looksLikePhone = loginId != null && loginId.matches("\\d{8,15}");

        try {
            TaiKhoan tk = null;
            if (looksLikePhone) {
                NhanVien nhanVien = nhanVienDAO.findByPhone(loginId);
                if (nhanVien != null && nhanVien.getMaNV() != null) {
                    tk = taiKhoanDAO.findById(nhanVien.getMaNV());
                    if (tk != null && tk.getMaNV() == null) {
                        tk.setMaNV(nhanVien.getMaNV());
                    }
                }
                KhachHang khachHang = khachHangDAO.findByPhone(loginId);
                if (tk == null && khachHang != null) {
                    tk = taiKhoanDAO.findById(khachHang.getMaKH());
                }
                if (tk == null) {
                    tk = taiKhoanDAO.findByUsername(loginId);
                }
            } else {
                tk = taiKhoanDAO.findByUsername(loginId);
                if (tk == null) {
                    KhachHang khachHang = khachHangDAO.findByPhone(loginId);
                    if (khachHang != null) {
                        tk = taiKhoanDAO.findById(khachHang.getMaKH());
                    }
                }
            }

            if (tk != null) {
                if (password == null || !PasswordUtils.matches(password, tk.getMatKhau())) {
                    return null;
                }

                if (!isActiveEmployeeAccount(tk)) {
                    return null;
                }

                if (tk.getMatKhau() != null && !PasswordUtils.isHashed(tk.getMatKhau())) {
                    try {
                        tk.setMatKhau(PasswordUtils.hash(password));
                        taiKhoanDAO.update(tk);
                    } catch (Exception ignored) {
                        // Không chặn đăng nhập nếu chưa nâng cấp được mật khẩu cũ.
                    }
                }
                return tk;
            }
        } catch (Exception ignored) {
            // Fallback sang kho auth file nếu CSDL gặp lỗi hoặc chưa có dữ liệu.
        }

        try {
            TaiKhoan tk = looksLikePhone ? authStore.loginByPhone(loginId, password) : authStore.login(loginId, password);
            if (tk != null) {
                return tk;
            }
            return looksLikePhone ? authStore.login(loginId, password) : authStore.loginByPhone(loginId, password);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isActiveEmployeeAccount(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            return false;
        }

        String maNV = taiKhoan.getMaNV();
        if (maNV == null || maNV.trim().isEmpty()) {
            return true;
        }

        return nhanVienDAO.findActiveById(maNV.trim()) != null;
    }
}
