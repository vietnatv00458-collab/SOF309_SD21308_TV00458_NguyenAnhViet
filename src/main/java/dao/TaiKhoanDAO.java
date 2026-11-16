package dao;

import entity.KhachHang;
import entity.NhanVien;
import entity.TaiKhoan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;
import utils.RoleUtils;

import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;

    public List<TaiKhoan> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<TaiKhoan> list = em.createQuery(
                    "SELECT t FROM TaiKhoan t ORDER BY t.tenDangNhap",
                    TaiKhoan.class
            ).getResultList();
            hydrateDisplayFields(em, list);
            return list;
        } finally {
            em.close();
        }
    }

    public TaiKhoan findById(String identifier) {
        EntityManager em = emf.createEntityManager();
        try {
            TaiKhoan tk = em.createQuery(
                            "SELECT t FROM TaiKhoan t WHERE t.tenDangNhap = :identifier OR t.maNV = :identifier OR t.maKH = :identifier",
                            TaiKhoan.class)
                    .setParameter("identifier", identifier)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            hydrateDisplayFields(em, tk);
            return tk;
        } finally {
            em.close();
        }
    }

    public boolean existsById(String identifier) {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(t) FROM TaiKhoan t WHERE t.tenDangNhap = :identifier OR t.maNV = :identifier OR t.maKH = :identifier",
                            Long.class)
                    .setParameter("identifier", identifier)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByUsername(String tenDangNhap) {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(t) FROM TaiKhoan t WHERE t.tenDangNhap = :tenDangNhap",
                            Long.class
                    )
                    .setParameter("tenDangNhap", tenDangNhap)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public void create(TaiKhoan taiKhoan) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(taiKhoan);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            throw e;
        } finally {
            em.close();
        }
    }

    public void saveCustomerRegistration(KhachHang khachHang, TaiKhoan taiKhoan) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            KhachHang existingCustomer = em.find(KhachHang.class, khachHang.getMaKH());
            if (existingCustomer != null) {
                existingCustomer.setHoTen(khachHang.getHoTen());
                existingCustomer.setSdt(khachHang.getSdt());
                existingCustomer.setEmail(khachHang.getEmail());
                existingCustomer.setDiaChi(khachHang.getDiaChi());
            } else {
                em.persist(khachHang);
            }

            taiKhoan.setMaTK(null);
            taiKhoan.setMaNV(null);
            taiKhoan.setMaKH(khachHang.getMaKH());
            taiKhoan.setVaiTro("KHACHHANG");
            taiKhoan.setTrangThai(true);
            em.persist(taiKhoan);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(TaiKhoan taiKhoan) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(taiKhoan);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(String tenDangNhap) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            TaiKhoan tk = em.createQuery(
                            "SELECT t FROM TaiKhoan t WHERE t.tenDangNhap = :identifier OR t.maNV = :identifier OR t.maKH = :identifier",
                            TaiKhoan.class)
                    .setParameter("identifier", tenDangNhap)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (tk != null) {
                em.remove(tk);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteByMaKH(String maKH) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM TaiKhoan t WHERE t.maKH = :maKH")
                    .setParameter("maKH", maKH)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public TaiKhoan findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            TaiKhoan tk = em.createQuery(
                            "SELECT t FROM TaiKhoan t WHERE t.tenDangNhap = :username",
                            TaiKhoan.class)
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            hydrateDisplayFields(em, tk);
            return tk;
        } finally {
            em.close();
        }
    }

    public String findMaNVByUsername(String username) {
        TaiKhoan taiKhoan = findByUsername(username);
        String maNV = taiKhoan != null ? trimToNull(taiKhoan.getMaNV()) : null;
        if (maNV == null) {
            return null;
        }

        NhanVien nhanVien = new NhanVienDAO().findActiveById(maNV);
        return nhanVien != null ? maNV : null;
    }

    public void insert(TaiKhoan tk) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tk);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private void hydrateDisplayFields(EntityManager em, List<TaiKhoan> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (TaiKhoan tk : new ArrayList<>(list)) {
            hydrateDisplayFields(em, tk);
        }
    }

    private void hydrateDisplayFields(EntityManager em, TaiKhoan tk) {
        if (tk == null) {
            return;
        }

        String identifier = tk.getMaNV();
        NhanVien nv = null;
        KhachHang khachHang = null;

        if (identifier != null && !identifier.trim().isEmpty()) {
            nv = em.find(NhanVien.class, identifier);
        }
        if (nv == null) {
            String customerIdentifier = tk.getMaKH();
            if (customerIdentifier == null || customerIdentifier.trim().isEmpty()) {
                customerIdentifier = identifier;
            }
            if (customerIdentifier != null && !customerIdentifier.trim().isEmpty()) {
                khachHang = em.find(KhachHang.class, customerIdentifier);
            }
        }

        if (nv != null) {
            String chucVu = nv.getChucVu();
            if (RoleUtils.isManagerRole(chucVu)) {
                tk.setVaiTro("Admin");
            } else if (RoleUtils.isDeliveryRole(chucVu)) {
                tk.setVaiTro("GiaoHang");
            } else {
                tk.setVaiTro("NhanVien");
            }
            tk.setMaKH(null);
        } else if (khachHang != null) {
            tk.setVaiTro("KHACHHANG");
            tk.setMaKH(khachHang.getMaKH());
        }

        if (tk.getVaiTro() == null || tk.getVaiTro().trim().isEmpty()) {
            tk.setVaiTro("NhanVien");
        }
        if (tk.getTrangThai() == null) {
            tk.setTrangThai(true);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
