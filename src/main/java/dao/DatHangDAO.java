package dao;

import entity.DatHang;
import dao.HoaDonDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;
import utils.OrderStatusUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DatHangDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;
    private static volatile boolean schemaChecked = false;

    public DatHangDAO() {
        ensureDatHangColumnsExist();
    }

    private void ensureDatHangColumnsExist() {
        if (schemaChecked) {
            return;
        }

        synchronized (DatHangDAO.class) {
            if (schemaChecked) {
                return;
            }

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                ensureColumnExists(em, "MaNV", "ALTER TABLE DatHang ADD MaNV NVARCHAR(10) NULL");
                ensureColumnExists(em, "MaNVGiaoHang", "ALTER TABLE DatHang ADD MaNVGiaoHang NVARCHAR(10) NULL");
                ensureColumnExists(em, "DaNhanGiaoHang", "ALTER TABLE DatHang ADD DaNhanGiaoHang BIT NULL");
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
                "IF COL_LENGTH('DatHang', '" + columnName + "') IS NULL " + ddl
        ).executeUpdate();
    }

    public List<DatHang> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT d FROM DatHang d ORDER BY d.maDatHang",
                    DatHang.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<String> findCompletedMaDatHangs() {
        List<String> completedIds = new ArrayList<>();
        for (DatHang datHang : findAll()) {
            if (datHang != null
                    && datHang.getMaDatHang() != null
                    && OrderStatusUtils.isCompleted(datHang.getTrangThai())) {
                completedIds.add(datHang.getMaDatHang());
            }
        }
        return completedIds;
    }

    public List<String> findLockedMaDatHangs() {
        List<String> lockedIds = new ArrayList<>();
        HoaDonDAO hoaDonDAO = new HoaDonDAO();

        for (DatHang datHang : findAll()) {
            if (datHang == null || datHang.getMaDatHang() == null) {
                continue;
            }

            if (isLocked(datHang.getMaDatHang(), hoaDonDAO)) {
                lockedIds.add(datHang.getMaDatHang());
            }
        }

        return lockedIds;
    }

    public List<DatHang> findActiveOrders() {
        List<DatHang> activeOrders = new ArrayList<>();
        for (DatHang datHang : findAll()) {
            if (datHang == null) {
                continue;
            }

            if (OrderStatusUtils.isCompleted(datHang.getTrangThai())
                    || OrderStatusUtils.isCancelled(datHang.getTrangThai())) {
                continue;
            }

            activeOrders.add(datHang);
        }

        activeOrders.sort(Comparator
                .comparing(DatHang::getNgayDat, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
                .thenComparing(DatHang::getMaDatHang, Comparator.nullsLast(String::compareToIgnoreCase)));
        return activeOrders;
    }

    public List<DatHang> findAssignedDeliveryOrders(String maNVGiaoHang) {
        String normalizedMaNV = trimToNull(maNVGiaoHang);
        if (normalizedMaNV == null) {
            return new ArrayList<>();
        }

        List<DatHang> assignedOrders = new ArrayList<>();
        for (DatHang datHang : findAll()) {
            if (datHang == null) {
                continue;
            }

            if (!normalizedMaNV.equalsIgnoreCase(trimToNull(datHang.getMaNVGiaoHang()))) {
                continue;
            }

            if (OrderStatusUtils.isCompleted(datHang.getTrangThai())
                    || OrderStatusUtils.isCancelled(datHang.getTrangThai())) {
                continue;
            }

            assignedOrders.add(datHang);
        }

        assignedOrders.sort(Comparator
                .comparing(DatHang::getNgayDat, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
                .thenComparing(DatHang::getMaDatHang, Comparator.nullsLast(String::compareToIgnoreCase)));
        return assignedOrders;
    }

    public List<DatHang> findVisibleOrdersForEmployee(String maNV) {
        String normalizedMaNV = trimToNull(maNV);
        List<DatHang> visibleOrders = new ArrayList<>();

        for (DatHang datHang : findAll()) {
            if (datHang == null) {
                continue;
            }

            boolean activeOrder = OrderStatusUtils.isActive(datHang.getTrangThai());
            boolean ownedHistory = normalizedMaNV != null
                    && normalizedMaNV.equalsIgnoreCase(trimToNull(datHang.getMaNV()));

            if (activeOrder || ownedHistory) {
                visibleOrders.add(datHang);
            }
        }

        visibleOrders.sort(Comparator
                .comparing((DatHang d) -> !OrderStatusUtils.isActive(d.getTrangThai()))
                .thenComparing(DatHang::getNgayDat, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(DatHang::getMaDatHang, Comparator.nullsLast(String::compareToIgnoreCase)));
        return visibleOrders;
    }

    public long countActiveOrders() {
        return findActiveOrders().size();
    }

    public boolean isLocked(String maDatHang) {
        return isLocked(maDatHang, new HoaDonDAO());
    }

    private boolean isLocked(String maDatHang, HoaDonDAO hoaDonDAO) {
        String normalizedId = trimToNull(maDatHang);
        if (normalizedId == null) {
            return false;
        }

        DatHang datHang = findById(normalizedId);
        if (datHang == null) {
            return false;
        }

        if (OrderStatusUtils.isCompleted(datHang.getTrangThai())) {
            return true;
        }

        return hoaDonDAO != null && hoaDonDAO.findByMaDatHang(normalizedId) != null;
    }

    public boolean updateStatus(String maDatHang, String trangThai) {
        String normalizedId = trimToNull(maDatHang);
        String normalizedStatus = trimToNull(trangThai);
        if (normalizedId == null || normalizedStatus == null || !OrderStatusUtils.isRecognized(normalizedStatus)) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            DatHang datHang = em.find(DatHang.class, normalizedId);
            if (datHang == null) {
                em.getTransaction().rollback();
                return false;
            }

            datHang.setTrangThai(normalizedStatus);
            em.merge(datHang);
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

    public boolean updateEmployee(String maDatHang, String maNV) {
        String normalizedId = trimToNull(maDatHang);
        String normalizedMaNV = trimToNull(maNV);
        if (normalizedId == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            DatHang datHang = em.find(DatHang.class, normalizedId);
            if (datHang == null) {
                em.getTransaction().rollback();
                return false;
            }

            datHang.setMaNV(normalizedMaNV);
            em.merge(datHang);
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

    public boolean updateDeliveryEmployee(String maDatHang, String maNVGiaoHang) {
        String normalizedId = trimToNull(maDatHang);
        if (normalizedId == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            DatHang datHang = em.find(DatHang.class, normalizedId);
            if (datHang == null) {
                em.getTransaction().rollback();
                return false;
            }

            datHang.setMaNVGiaoHang(trimToNull(maNVGiaoHang));
            em.merge(datHang);
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

    public DatHang findById(String maDatHang) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(DatHang.class, maDatHang);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String maDatHang) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(DatHang.class, maDatHang) != null;
        } finally {
            em.close();
        }
    }

    public void create(DatHang datHang) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(datHang);
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

    public void update(DatHang datHang) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(datHang);
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

    public void delete(String maDatHang) {
        String normalizedMaDatHang = trimToNull(maDatHang);
        if (normalizedMaDatHang == null) {
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            DatHang dh = em.find(DatHang.class, normalizedMaDatHang);
            if (dh != null) {
                if (OrderStatusUtils.isCompleted(dh.getTrangThai())) {
                    throw new IllegalStateException("Đơn đã hoàn thành nên không thể xóa.");
                }

                new ChiTietGioHangDAO().deleteByMaDatHang(em, normalizedMaDatHang);
                em.remove(dh);
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
