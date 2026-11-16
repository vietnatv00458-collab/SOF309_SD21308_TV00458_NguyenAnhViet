package dao;

import entity.NhanVien;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.util.List;
import java.util.Locale;

public class NhanVienDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;
    private static volatile boolean schemaChecked = false;

    public NhanVienDAO() {
        ensureNhanVienColumnsExist();
    }

    private void ensureNhanVienColumnsExist() {
        if (schemaChecked) {
            return;
        }

        synchronized (NhanVienDAO.class) {
            if (schemaChecked) {
                return;
            }

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                ensureColumnExists(em, "IsDeleted",
                        "ALTER TABLE NhanVien ADD IsDeleted BIT NOT NULL CONSTRAINT DF_NhanVien_IsDeleted DEFAULT(0)");
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
                "IF COL_LENGTH('NhanVien', '" + columnName + "') IS NULL " + ddl
        ).executeUpdate();
    }

    public List<NhanVien> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT n FROM NhanVien n WHERE (n.daXoa = false OR n.daXoa IS NULL) ORDER BY n.maNV",
                    NhanVien.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<NhanVien> findAllIncludingDeleted() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT n FROM NhanVien n ORDER BY n.maNV",
                    NhanVien.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public NhanVien findById(String maNV) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(NhanVien.class, maNV);
        } finally {
            em.close();
        }
    }

    public NhanVien findActiveById(String maNV) {
        String normalizedMaNV = trimToNull(maNV);
        if (normalizedMaNV == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NhanVien n WHERE n.maNV = :maNV AND (n.daXoa = false OR n.daXoa IS NULL)",
                            NhanVien.class
                    )
                    .setParameter("maNV", normalizedMaNV)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    public NhanVien findByPhone(String sdt) {
        String normalizedSdt = trimToNull(sdt);
        if (normalizedSdt == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NhanVien n WHERE n.sdt = :sdt AND (n.daXoa = false OR n.daXoa IS NULL)",
                            NhanVien.class
                    )
                    .setParameter("sdt", normalizedSdt)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String maNV) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(NhanVien.class, maNV) != null;
        } finally {
            em.close();
        }
    }

    public List<NhanVien> findEmployeesWithoutAccount() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT n FROM NhanVien n " +
                            "WHERE (n.daXoa = false OR n.daXoa IS NULL) " +
                            "AND NOT EXISTS (" +
                            "SELECT t FROM TaiKhoan t WHERE t.maNV = n.maNV" +
                            ") " +
                            "ORDER BY n.maNV",
                    NhanVien.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<NhanVien> findDeliveryEmployeesWithoutAccount() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NhanVien n " +
                                    "WHERE (n.daXoa = false OR n.daXoa IS NULL) " +
                                    "AND (LOWER(TRIM(n.chucVu)) = LOWER(:giaoHang1) " +
                                    "OR LOWER(TRIM(n.chucVu)) = LOWER(:giaoHang2)) " +
                                    "AND NOT EXISTS (" +
                                    "SELECT t FROM TaiKhoan t WHERE t.maNV = n.maNV" +
                                    ") " +
                                    "ORDER BY n.maNV",
                            NhanVien.class
                    )
                    .setParameter("giaoHang1", "GiaoHang")
                    .setParameter("giaoHang2", "Giao Hàng")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public String generateNextMaNV() {
        EntityManager em = emf.createEntityManager();
        try {
            List<String> ids = em.createQuery(
                    "SELECT n.maNV FROM NhanVien n",
                    String.class
            ).getResultList();
            return generateNextCode("NV", ids);
        } finally {
            em.close();
        }
    }

    public List<NhanVien> findByChucVu(String chucVu) {
        String normalized = trimToNull(chucVu);
        if (normalized == null) {
            return findAll();
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NhanVien n WHERE (n.daXoa = false OR n.daXoa IS NULL) " +
                                    "AND LOWER(TRIM(n.chucVu)) = LOWER(:chucVu) ORDER BY n.maNV",
                            NhanVien.class
                    )
                    .setParameter("chucVu", normalized)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<NhanVien> findDeliveryEmployees() {
        List<NhanVien> employees = findByChucVu("GiaoHang");
        if (!employees.isEmpty()) {
            return employees;
        }

        return findByChucVu("Giao Hàng");
    }

    public void create(NhanVien nv) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (nv.getDaXoa() == null) {
                nv.setDaXoa(Boolean.FALSE);
            }
            em.persist(nv);
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

    public void update(NhanVien nv) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(nv);
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

    public boolean softDelete(String maNV) {
        String normalizedMaNV = trimToNull(maNV);
        if (normalizedMaNV == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            NhanVien nv = em.find(NhanVien.class, normalizedMaNV);
            if (nv == null) {
                em.getTransaction().rollback();
                return false;
            }

            nv.setDaXoa(Boolean.TRUE);
            em.merge(nv);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(String maNV) {
        softDelete(maNV);
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
