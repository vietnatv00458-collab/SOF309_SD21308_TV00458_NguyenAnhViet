package dao;

import entity.HoaDon;
import entity.DoanhThu;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DoanhThuDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;

    public List<DoanhThu> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT d FROM DoanhThu d ORDER BY d.maDT",
                    DoanhThu.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existsByMaHD(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(d) FROM DoanhThu d WHERE d.maHD = :maHD",
                    Long.class
            ).setParameter("maHD", maHD.trim()).getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public DoanhThu findByMaHD(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            List<DoanhThu> results = em.createQuery(
                            "SELECT d FROM DoanhThu d WHERE d.maHD = :maHD ORDER BY d.maDT DESC",
                            DoanhThu.class
                    )
                    .setParameter("maHD", maHD.trim())
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public void upsertForHoaDon(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getMaHD() == null || hoaDon.getMaHD().trim().isEmpty()) {
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            DoanhThu existing = em.createQuery(
                            "SELECT d FROM DoanhThu d WHERE d.maHD = :maHD",
                            DoanhThu.class
                    )
                    .setParameter("maHD", hoaDon.getMaHD().trim())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            BigDecimal revenue = hoaDon.getThanhTien() != null ? hoaDon.getThanhTien() : BigDecimal.ZERO;
            if (existing == null) {
                DoanhThu doanhThu = new DoanhThu();
                doanhThu.setMaDT(generateNextMaDT(em));
                doanhThu.setMaHD(hoaDon.getMaHD().trim());
                doanhThu.setNgay(hoaDon.getNgayLap() != null
                        ? hoaDon.getNgayLap().toLocalDateTime().toLocalDate()
                        : LocalDate.now());
                doanhThu.setDoanhThu(revenue);
                doanhThu.setChiPhi(BigDecimal.ZERO);
                em.persist(doanhThu);
            } else {
                existing.setNgay(hoaDon.getNgayLap() != null
                        ? hoaDon.getNgayLap().toLocalDateTime().toLocalDate()
                        : existing.getNgay());
                existing.setDoanhThu(revenue);
                if (existing.getChiPhi() == null) {
                    existing.setChiPhi(BigDecimal.ZERO);
                }
                em.merge(existing);
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

    private String generateNextMaDT(EntityManager em) {
        List<String> ids = em.createQuery(
                "SELECT d.maDT FROM DoanhThu d",
                String.class
        ).getResultList();

        int max = 0;
        for (String rawId : ids) {
            if (rawId == null) {
                continue;
            }

            String id = rawId.trim();
            if (!id.matches("DT\\d+")) {
                continue;
            }

            max = Math.max(max, Integer.parseInt(id.substring(2)));
        }

        return String.format("DT%02d", max + 1);
    }

    public void create(DoanhThu doanhThu) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(doanhThu);
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
