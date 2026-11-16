package dao;

import entity.LichSuGiaSanPham;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.math.BigDecimal;
import java.util.List;

public class LichSuGiaSanPhamDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;
    private static volatile boolean schemaChecked = false;

    public LichSuGiaSanPhamDAO() {
        ensureHistoryTableExists();
    }

    private void ensureHistoryTableExists() {
        if (schemaChecked) {
            return;
        }

        synchronized (LichSuGiaSanPhamDAO.class) {
            if (schemaChecked) {
                return;
            }

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                em.createNativeQuery(
                        "IF OBJECT_ID(N'LichSuGiaSanPham', N'U') IS NULL " +
                                "BEGIN " +
                                "CREATE TABLE LichSuGiaSanPham (" +
                                "MaLSGia INT IDENTITY(1,1) PRIMARY KEY, " +
                                "MaSP VARCHAR(10) NOT NULL, " +
                                "GiaCu DECIMAL(18,2) NOT NULL, " +
                                "GiaMoi DECIMAL(18,2) NOT NULL, " +
                                "NgayApDung DATETIME DEFAULT GETDATE(), " +
                                "LyDo NVARCHAR(255), " +
                                "CONSTRAINT FK_LSGia_SanPham FOREIGN KEY (MaSP) REFERENCES SanPham(MaSP)" +
                                "); " +
                                "END"
                ).executeUpdate();
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

    public List<LichSuGiaSanPham> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT l FROM LichSuGiaSanPham l ORDER BY l.ngayApDung DESC, l.maLSGia DESC",
                    LichSuGiaSanPham.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<LichSuGiaSanPham> findLatest(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            int max = limit > 0 ? limit : 5;
            return em.createQuery(
                            "SELECT l FROM LichSuGiaSanPham l ORDER BY l.ngayApDung DESC, l.maLSGia DESC",
                            LichSuGiaSanPham.class
                    )
                    .setMaxResults(max)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<LichSuGiaSanPham> findByKeyword(String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return findAll();
        }

        String likeKeyword = "%" + trimmed.toLowerCase() + "%";

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT l FROM LichSuGiaSanPham l " +
                                    "WHERE LOWER(l.maSP) LIKE :keyword " +
                                    "OR (l.lyDo IS NOT NULL AND LOWER(l.lyDo) LIKE :keyword) " +
                                    "ORDER BY l.ngayApDung DESC, l.maLSGia DESC",
                            LichSuGiaSanPham.class
                    )
                    .setParameter("keyword", likeKeyword)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public LichSuGiaSanPham findById(Integer maLSGia) {
        if (maLSGia == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.find(LichSuGiaSanPham.class, maLSGia);
        } finally {
            em.close();
        }
    }

    public List<LichSuGiaSanPham> findByProductCode(String maSP) {
        String trimmed = maSP == null ? "" : maSP.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT l FROM LichSuGiaSanPham l " +
                                    "WHERE l.maSP = :maSP " +
                                    "ORDER BY l.ngayApDung DESC, l.maLSGia DESC",
                            LichSuGiaSanPham.class
                    )
                    .setParameter("maSP", trimmed)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existsByProductCode(String maSP) {
        String trimmed = maSP == null ? "" : maSP.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            Number result = (Number) em.createQuery(
                    "SELECT COUNT(l) FROM LichSuGiaSanPham l WHERE l.maSP = :maSP"
            ).setParameter("maSP", trimmed).getSingleResult();
            return result != null && result.longValue() > 0;
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            Number result = (Number) em.createQuery(
                    "SELECT COUNT(l) FROM LichSuGiaSanPham l"
            ).getSingleResult();
            return result != null ? result.longValue() : 0L;
        } finally {
            em.close();
        }
    }

    public void create(LichSuGiaSanPham lichSuGiaSanPham) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(lichSuGiaSanPham);
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

    public void update(LichSuGiaSanPham lichSuGiaSanPham) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(lichSuGiaSanPham);
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

    public void delete(Integer maLSGia) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            LichSuGiaSanPham history = em.find(LichSuGiaSanPham.class, maLSGia);
            if (history != null) {
                em.remove(history);
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
}
