package dao;

import entity.HoaDon;
import entity.KhuyenMai;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class KhuyenMaiDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;
    private static volatile boolean schemaChecked = false;

    public KhuyenMaiDAO() {
        ensureKhuyenMaiColumnsExist();
    }

    private void ensureKhuyenMaiColumnsExist() {
        if (schemaChecked) {
            return;
        }

        synchronized (KhuyenMaiDAO.class) {
            if (schemaChecked) {
                return;
            }

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                ensureColumnExists(em, "LoaiGiamGia", "ALTER TABLE KhuyenMai ADD LoaiGiamGia NVARCHAR(20) NULL");
                ensureColumnExists(em, "SoTienGiam", "ALTER TABLE KhuyenMai ADD SoTienGiam DECIMAL(18,2) NULL");
                ensureColumnExists(em, "MucGiamToiDa", "ALTER TABLE KhuyenMai ADD MucGiamToiDa DECIMAL(18,2) NULL");
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
                "IF COL_LENGTH('KhuyenMai', '" + columnName + "') IS NULL " + ddl
        ).executeUpdate();
    }

    public List<KhuyenMai> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT k FROM KhuyenMai k ORDER BY k.maKM",
                    KhuyenMai.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<KhuyenMai> findActive(LocalDate referenceDate) {
        EntityManager em = emf.createEntityManager();
        try {
            LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();
            List<KhuyenMai> all = em.createQuery(
                    "SELECT k FROM KhuyenMai k ORDER BY k.maKM",
                    KhuyenMai.class
            ).getResultList();

            List<KhuyenMai> active = new java.util.ArrayList<>();
            for (KhuyenMai khuyenMai : all) {
                if (isActive(khuyenMai, today)) {
                    active.add(khuyenMai);
                }
            }

            active.sort((a, b) -> {
                double ap = discountPriority(a);
                double bp = discountPriority(b);
                int compare = Double.compare(bp, ap);
                if (compare != 0) {
                    return compare;
                }

                String aCode = a != null && a.getMaKM() != null ? a.getMaKM() : "";
                String bCode = b != null && b.getMaKM() != null ? b.getMaKM() : "";
                return aCode.compareToIgnoreCase(bCode);
            });
            return active;
        } finally {
            em.close();
        }
    }

    public KhuyenMai findActiveById(String maKM, LocalDate referenceDate) {
        if (maKM == null) {
            return null;
        }

        KhuyenMai khuyenMai = findById(maKM);
        if (khuyenMai == null || !isActive(khuyenMai, referenceDate)) {
            return null;
        }
        return khuyenMai;
    }

    public KhuyenMai findById(String maKM) {
        EntityManager em = emf.createEntityManager();
        try {
            return findByCode(em, maKM);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String maKM) {
        EntityManager em = emf.createEntityManager();
        try {
            return findByCode(em, maKM) != null;
        } finally {
            em.close();
        }
    }

    private String normalizeCode(String maKM) {
        if (maKM == null) {
            return null;
        }

        String trimmed = maKM.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toUpperCase(Locale.ROOT);
    }

    private KhuyenMai findByCode(EntityManager em, String maKM) {
        String normalized = normalizeCode(maKM);
        if (normalized == null) {
            return null;
        }

        List<KhuyenMai> results = em.createQuery(
                "SELECT k FROM KhuyenMai k WHERE UPPER(k.maKM) = :maKM",
                KhuyenMai.class
        ).setParameter("maKM", normalized).getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    private boolean isActive(KhuyenMai khuyenMai, LocalDate referenceDate) {
        if (khuyenMai == null) {
            return false;
        }

        LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();
        LocalDate ngayBatDau = khuyenMai.getNgayBatDau();
        LocalDate ngayKetThuc = khuyenMai.getNgayKetThuc();
        if (!hasValidDiscountValue(khuyenMai)) {
            return false;
        }

        if (ngayBatDau != null && today.isBefore(ngayBatDau)) {
            return false;
        }

        if (ngayKetThuc != null && today.isAfter(ngayKetThuc)) {
            return false;
        }

        return true;
    }

    private boolean hasValidDiscountValue(KhuyenMai khuyenMai) {
        if (khuyenMai == null) {
            return false;
        }

        String type = normalizeType(khuyenMai.getLoaiGiamGia());
        if ("AMOUNT".equals(type)) {
            return khuyenMai.getSoTienGiam() != null && khuyenMai.getSoTienGiam().signum() > 0;
        }

        if ("PERCENT".equals(type)) {
            Double percent = khuyenMai.getPhanTramGiam();
            return percent != null && percent > 0 && percent < 100;
        }

        if (khuyenMai.getSoTienGiam() != null && khuyenMai.getSoTienGiam().signum() > 0) {
            return true;
        }

        Double percent = khuyenMai.getPhanTramGiam();
        return percent != null && percent > 0 && percent < 100;
    }

    private String normalizeType(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replace(" ", "").replace("-", "").replace("_", "").toUpperCase(Locale.ROOT);
    }

    private double discountPriority(KhuyenMai khuyenMai) {
        if (khuyenMai == null) {
            return 0D;
        }

        String type = normalizeType(khuyenMai.getLoaiGiamGia());
        if ("AMOUNT".equals(type) && khuyenMai.getSoTienGiam() != null) {
            return khuyenMai.getSoTienGiam().doubleValue();
        }

        if (khuyenMai.getPhanTramGiam() != null) {
            return khuyenMai.getPhanTramGiam();
        }

        if (khuyenMai.getSoTienGiam() != null) {
            return khuyenMai.getSoTienGiam().doubleValue();
        }

        return 0D;
    }

    public void create(KhuyenMai khuyenMai) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(khuyenMai);
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

    public void update(KhuyenMai khuyenMai) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(khuyenMai);
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

    public void delete(String maKM) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            KhuyenMai km = findByCode(em, maKM);
            if (km != null) {
                Long usedCount = em.createQuery(
                                "SELECT COUNT(h) FROM HoaDon h WHERE h.maKM = :maKM",
                                Long.class
                        )
                        .setParameter("maKM", km.getMaKM())
                        .getSingleResult();
                if (usedCount != null && usedCount > 0L) {
                    throw new IllegalStateException("Không thể xóa khuyến mãi đã được dùng trong hóa đơn.");
                }
                em.remove(km);
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
