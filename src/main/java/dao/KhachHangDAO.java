package dao;

import entity.DatHang;
import entity.GioHang;
import entity.HoaDon;
import entity.KhachHang;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.util.Comparator;
import java.util.List;

public class KhachHangDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;

    public List<KhachHang> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT k FROM KhachHang k ORDER BY k.maKH",
                    KhachHang.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public String generateNextMaKH() {
        List<KhachHang> list = findAll();

        int max = list.stream()
                .map(KhachHang::getMaKH)
                .filter(ma -> ma != null && ma.matches("KH\\d+"))
                .map(ma -> Integer.parseInt(ma.substring(2)))
                .max(Comparator.naturalOrder())
                .orElse(0);

        return String.format("KH%02d", max + 1);
    }

    public boolean existsByPhone(String sdt) {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(k) FROM KhachHang k WHERE k.sdt = :sdt",
                    Long.class
            ).setParameter("sdt", sdt).getSingleResult();

            return count > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByEmail(String email) {
        String normalizedEmail = trimToNull(email);
        if (normalizedEmail == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
                Long count = em.createQuery(
                    "SELECT COUNT(k) FROM KhachHang k WHERE k.email IS NOT NULL AND LOWER(TRIM(k.email)) = LOWER(:email)",
                    Long.class
            ).setParameter("email", normalizedEmail).getSingleResult();

            return count > 0;
        } finally {
            em.close();
        }
    }

    public KhachHang findByPhone(String sdt) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT k FROM KhachHang k WHERE k.sdt = :sdt",
                            KhachHang.class
                    )
                    .setParameter("sdt", sdt)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    public KhachHang findByEmail(String email) {
        String normalizedEmail = trimToNull(email);
        if (normalizedEmail == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT k FROM KhachHang k WHERE k.email IS NOT NULL AND LOWER(TRIM(k.email)) = LOWER(:email)",
                            KhachHang.class
                    )
                    .setParameter("email", normalizedEmail)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    public KhachHang findById(String maKH) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(KhachHang.class, maKH);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String maKH) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(KhachHang.class, maKH) != null;
        } finally {
            em.close();
        }
    }

    public void create(KhachHang kh) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(kh);
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

    public void update(KhachHang kh) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(kh);
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

    public void delete(String maKH) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            KhachHang kh = em.find(KhachHang.class, maKH);
            if (kh != null) {
                if (hasTransactionalHistory(em, kh.getMaKH())) {
                    throw new IllegalStateException("Không thể xóa khách hàng vì còn lịch sử đơn hàng hoặc hóa đơn.");
                }
                em.remove(kh);
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

    public boolean hasTransactionalHistory(String maKH) {
        EntityManager em = emf.createEntityManager();
        try {
            return hasTransactionalHistory(em, maKH);
        } finally {
            em.close();
        }
    }

    private boolean hasTransactionalHistory(EntityManager em, String maKH) {
        String normalizedMaKH = trimToNull(maKH);
        if (em == null || normalizedMaKH == null) {
            return false;
        }

        Long countDatHang = em.createQuery(
                        "SELECT COUNT(d) FROM DatHang d WHERE d.maKH = :maKH",
                        Long.class
                )
                .setParameter("maKH", normalizedMaKH)
                .getSingleResult();
        if (countDatHang != null && countDatHang > 0L) {
            return true;
        }

        Long countHoaDon = em.createQuery(
                        "SELECT COUNT(h) FROM HoaDon h WHERE h.maKH = :maKH",
                        Long.class
                )
                .setParameter("maKH", normalizedMaKH)
                .getSingleResult();
        if (countHoaDon != null && countHoaDon > 0L) {
            return true;
        }

        Long countGioHang = em.createQuery(
                        "SELECT COUNT(g) FROM GioHang g WHERE g.maKH = :maKH",
                        Long.class
                )
                .setParameter("maKH", normalizedMaKH)
                .getSingleResult();
        return countGioHang != null && countGioHang > 0L;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
