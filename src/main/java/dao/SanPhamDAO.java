package dao;

import entity.ChiTietGioHang;
import entity.ChiTietHoaDon;
import entity.LichSuGiaSanPham;
import entity.SanPham;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.util.List;
import java.util.Locale;

public class SanPhamDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;
    private static volatile boolean schemaChecked = false;

    public SanPhamDAO() {
        ensureProductColumnsExist();
    }

    private void ensureProductColumnsExist() {
        if (schemaChecked) {
            return;
        }

        synchronized (SanPhamDAO.class) {
            if (schemaChecked) {
                return;
            }

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                ensureColumnExists(em, "HinhAnh", "ALTER TABLE SanPham ADD HinhAnh NVARCHAR(255) NULL");
                ensureColumnExists(em, "ThanhPhan", "ALTER TABLE SanPham ADD ThanhPhan NVARCHAR(500) NULL");
                em.getTransaction().commit();
                schemaChecked = true;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        }
    }

    private void ensureColumnExists(EntityManager em, String columnName, String ddl) {
        em.createNativeQuery(
                "IF COL_LENGTH('SanPham', '" + columnName + "') IS NULL " + ddl
        ).executeUpdate();
    }

    public List<SanPham> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM SanPham s ORDER BY s.maSP",
                    SanPham.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<SanPham> findByKeyword(String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return findAll();
        }

        String likeKeyword = "%" + trimmed.toLowerCase() + "%";

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM SanPham s " +
                            "WHERE LOWER(s.maSP) LIKE :keyword " +
                                    "OR LOWER(s.tenSP) LIKE :keyword " +
                                    "OR (s.loaiSP IS NOT NULL AND LOWER(s.loaiSP) LIKE :keyword) " +
                                    "OR (s.sizeSP IS NOT NULL AND LOWER(s.sizeSP) LIKE :keyword) " +
                                    "OR (s.moTa IS NOT NULL AND LOWER(s.moTa) LIKE :keyword) " +
                                    "OR (s.thanhPhan IS NOT NULL AND LOWER(s.thanhPhan) LIKE :keyword) " +
                                    "ORDER BY s.maSP",
                            SanPham.class
                    )
                    .setParameter("keyword", likeKeyword)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public SanPham findById(String maSP) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(SanPham.class, maSP);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String maSP) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(SanPham.class, maSP) != null;
        } finally {
            em.close();
        }
    }

    public String generateNextMaSP() {
        EntityManager em = emf.createEntityManager();
        try {
            List<String> ids = em.createQuery(
                    "SELECT s.maSP FROM SanPham s",
                    String.class
            ).getResultList();
            return generateNextCode("SP", ids);
        } finally {
            em.close();
        }
    }

    public void create(SanPham sanPham) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(sanPham);
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

    public void update(SanPham sanPham) {
        updateInternal(sanPham, null);
    }

    public void updateWithHistory(SanPham sanPham, LichSuGiaSanPham historyEntry) {
        updateInternal(sanPham, historyEntry);
    }

    private void updateInternal(SanPham sanPham, LichSuGiaSanPham historyEntry) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(sanPham);
            if (historyEntry != null) {
                em.persist(historyEntry);
            }
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

    public void delete(String maSP) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            SanPham sp = em.find(SanPham.class, maSP);
            if (sp != null) {
                if (hasUsageHistory(em, sp.getMaSP())) {
                    throw new IllegalStateException("Không thể xóa sản phẩm đã có lịch sử bán hoặc chi tiết đơn hàng.");
                }
                em.remove(sp);
            }
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

    public boolean hasUsageHistory(String maSP) {
        EntityManager em = emf.createEntityManager();
        try {
            return hasUsageHistory(em, maSP);
        } finally {
            em.close();
        }
    }

    private boolean hasUsageHistory(EntityManager em, String maSP) {
        String normalizedMaSP = trimToNull(maSP);
        if (em == null || normalizedMaSP == null) {
            return false;
        }

        Long countHoaDon = em.createQuery(
                        "SELECT COUNT(c) FROM ChiTietHoaDon c WHERE c.maSP = :maSP",
                        Long.class
                )
                .setParameter("maSP", normalizedMaSP)
                .getSingleResult();
        if (countHoaDon != null && countHoaDon > 0L) {
            return true;
        }

        Long countGioHang = em.createQuery(
                        "SELECT COUNT(c) FROM ChiTietGioHang c WHERE c.maSP = :maSP",
                        Long.class
                )
                .setParameter("maSP", normalizedMaSP)
                .getSingleResult();
        if (countGioHang != null && countGioHang > 0L) {
            return true;
        }

        Long countHistory = em.createQuery(
                        "SELECT COUNT(l) FROM LichSuGiaSanPham l WHERE l.maSP = :maSP",
                        Long.class
                )
                .setParameter("maSP", normalizedMaSP)
                .getSingleResult();
        return countHistory != null && countHistory > 0L;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateNextCode(String prefix, List<String> ids) {
        int max = 0;
        int width = 2;

        if (ids != null) {
            for (String rawId : ids) {
                if (rawId == null) {
                    continue;
                }

                String id = rawId.trim().toUpperCase(Locale.ROOT);
                if (!id.startsWith(prefix)) {
                    continue;
                }

                String suffix = id.substring(prefix.length());
                if (!suffix.matches("\\d+")) {
                    continue;
                }

                max = Math.max(max, Integer.parseInt(suffix));
                width = Math.max(width, suffix.length());
            }
        }

        return prefix + String.format("%0" + width + "d", max + 1);
    }
}
