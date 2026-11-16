package dao;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.DoanhThu;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;
import utils.OrderStatusUtils;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HoaDonDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;
    private static volatile boolean schemaChecked = false;

    public HoaDonDAO() {
        ensureHoaDonColumnsExist();
    }

    private void ensureHoaDonColumnsExist() {
        if (schemaChecked) {
            return;
        }

        synchronized (HoaDonDAO.class) {
            if (schemaChecked) {
                return;
            }

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                ensureColumnExists(em, "TrangThai", "ALTER TABLE HoaDon ADD TrangThai NVARCHAR(30) NULL");
                ensureColumnExists(em, "MaDatHang", "ALTER TABLE HoaDon ADD MaDatHang NVARCHAR(10) NULL");
                ensureColumnExists(em, "MaNVGiaoHang", "ALTER TABLE HoaDon ADD MaNVGiaoHang NVARCHAR(10) NULL");
                ensureColumnExists(em, "SoDienThoai", "ALTER TABLE HoaDon ADD SoDienThoai NVARCHAR(15) NULL");
                ensureColumnExists(em, "DiaChiGiaoHang", "ALTER TABLE HoaDon ADD DiaChiGiaoHang NVARCHAR(255) NULL");
                ensureColumnExists(em, "DaNhanGiaoHang", "ALTER TABLE HoaDon ADD DaNhanGiaoHang BIT NULL");
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
                "IF COL_LENGTH('HoaDon', '" + columnName + "') IS NULL " + ddl
        ).executeUpdate();
    }

    public List<HoaDon> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT h FROM HoaDon h ORDER BY h.maHD",
                    HoaDon.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<HoaDon> findByMaNV(String maNV) {
        String normalizedMaNV = trimToNull(maNV);
        if (normalizedMaNV == null) {
            return Collections.emptyList();
        }

        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT h FROM HoaDon h WHERE h.maNV = :maNV ORDER BY h.maHD",
                            HoaDon.class
                    )
                    .setParameter("maNV", normalizedMaNV)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<HoaDon> findVisibleForEmployee(String maNV) {
        String normalizedMaNV = trimToNull(maNV);
        if (normalizedMaNV == null) {
            return Collections.emptyList();
        }

        List<HoaDon> visibleHoaDon = new ArrayList<>();
        for (HoaDon hoaDon : findAll()) {
            if (hoaDon == null) {
                continue;
            }

            String assignedMaNV = trimToNull(hoaDon.getMaNV());
            if (assignedMaNV == null || assignedMaNV.equalsIgnoreCase(normalizedMaNV)) {
                visibleHoaDon.add(hoaDon);
            }
        }

        visibleHoaDon.sort((left, right) -> {
            String leftStatus = left != null ? trimToNull(left.getTrangThai()) : null;
            String rightStatus = right != null ? trimToNull(right.getTrangThai()) : null;
            boolean leftActive = !OrderStatusUtils.isCompleted(leftStatus) && !OrderStatusUtils.isCancelled(leftStatus);
            boolean rightActive = !OrderStatusUtils.isCompleted(rightStatus) && !OrderStatusUtils.isCancelled(rightStatus);
            if (leftActive != rightActive) {
                return leftActive ? -1 : 1;
            }

            String leftDate = left != null && left.getNgayLap() != null ? left.getNgayLap().toString() : "";
            String rightDate = right != null && right.getNgayLap() != null ? right.getNgayLap().toString() : "";
            int dateCompare = rightDate.compareTo(leftDate);
            if (dateCompare != 0) {
                return dateCompare;
            }

            String leftId = left != null ? trimToNull(left.getMaHD()) : null;
            String rightId = right != null ? trimToNull(right.getMaHD()) : null;
            if (leftId == null && rightId == null) {
                return 0;
            }
            if (leftId == null) {
                return 1;
            }
            if (rightId == null) {
                return -1;
            }
            return rightId.compareToIgnoreCase(leftId);
        });
        return visibleHoaDon;
    }

    public List<HoaDon> findVisibleForDelivery(String maNVGiaoHang) {
        String normalizedMaNV = trimToNull(maNVGiaoHang);
        if (normalizedMaNV == null) {
            return Collections.emptyList();
        }

        List<HoaDon> visibleHoaDon = new ArrayList<>();
        for (HoaDon hoaDon : findAll()) {
            if (hoaDon == null) {
                continue;
            }

            String assignedDeliveryMaNV = trimToNull(hoaDon.getMaNVGiaoHang());
            if (!normalizedMaNV.equalsIgnoreCase(assignedDeliveryMaNV)) {
                continue;
            }

            visibleHoaDon.add(hoaDon);
        }

        visibleHoaDon.sort((left, right) -> {
            String leftDate = left != null && left.getNgayLap() != null ? left.getNgayLap().toString() : "";
            String rightDate = right != null && right.getNgayLap() != null ? right.getNgayLap().toString() : "";
            int dateCompare = rightDate.compareTo(leftDate);
            if (dateCompare != 0) {
                return dateCompare;
            }

            String leftId = left != null ? trimToNull(left.getMaHD()) : null;
            String rightId = right != null ? trimToNull(right.getMaHD()) : null;
            if (leftId == null && rightId == null) {
                return 0;
            }
            if (leftId == null) {
                return 1;
            }
            if (rightId == null) {
                return -1;
            }
            return rightId.compareToIgnoreCase(leftId);
        });
        return visibleHoaDon;
    }

    public HoaDon findByMaDatHang(String maDatHang) {
        String normalizedMaDatHang = trimToNull(maDatHang);
        if (normalizedMaDatHang == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        try {
            List<HoaDon> result = em.createQuery(
                            "SELECT h FROM HoaDon h WHERE h.maDatHang = :maDatHang ORDER BY h.maHD DESC",
                            HoaDon.class
                    )
                    .setParameter("maDatHang", normalizedMaDatHang)
                    .setMaxResults(1)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    public List<String> findCompletedMaHDs() {
        List<String> completedIds = new ArrayList<>();
        Set<String> revenueLockedIds = new HashSet<>();
        for (DoanhThu doanhThu : new DoanhThuDAO().findAll()) {
            if (doanhThu != null && doanhThu.getMaHD() != null) {
                revenueLockedIds.add(doanhThu.getMaHD());
            }
        }

        for (HoaDon hoaDon : findAll()) {
            if (hoaDon != null
                    && hoaDon.getMaHD() != null
                    && (OrderStatusUtils.isCompleted(hoaDon.getTrangThai())
                    || revenueLockedIds.contains(hoaDon.getMaHD()))) {
                completedIds.add(hoaDon.getMaHD());
            }
        }
        return completedIds;
    }

    public boolean isLocked(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return false;
        }

        HoaDon hoaDon = findById(maHD.trim());
        return hoaDon != null && (
                OrderStatusUtils.isCompleted(hoaDon.getTrangThai())
                        || new DoanhThuDAO().existsByMaHD(hoaDon.getMaHD())
        );
    }

    public HoaDon findById(String maHD) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(HoaDon.class, maHD);
        } finally {
            em.close();
        }
    }

    public String generateNextMaHD() {
        EntityManager em = emf.createEntityManager();
        try {
            List<String> ids = em.createQuery(
                    "SELECT h.maHD FROM HoaDon h",
                    String.class
            ).getResultList();

            int max = 0;
            if (ids != null) {
                for (String rawId : ids) {
                    String id = trimToNull(rawId);
                    if (id == null || !id.matches("\\d{4}")) {
                        continue;
                    }

                    max = Math.max(max, Integer.parseInt(id));
                }
            }

            if (max >= 9999) {
                throw new IllegalStateException("Đã hết mã hóa đơn 4 số.");
            }

            return String.format("%04d", max + 1);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String maHD) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(HoaDon.class, maHD) != null;
        } finally {
            em.close();
        }
    }

    public void create(HoaDon hoaDon) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(hoaDon);
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

    public void update(HoaDon hoaDon) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(hoaDon);
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

    public void delete(String maHD) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            HoaDon hd = em.find(HoaDon.class, maHD);
            if (hd != null) {
                em.createQuery("DELETE FROM ChiTietHoaDon c WHERE c.maHD = :maHD")
                        .setParameter("maHD", hd.getMaHD())
                        .executeUpdate();
                em.createQuery("DELETE FROM DoanhThu d WHERE d.maHD = :maHD")
                        .setParameter("maHD", hd.getMaHD())
                        .executeUpdate();
                em.remove(hd);
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

    public boolean updateEmployeeByOrder(String maDatHang, String maNV) {
        String normalizedMaDatHang = trimToNull(maDatHang);
        if (normalizedMaDatHang == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<HoaDon> result = em.createQuery(
                            "SELECT h FROM HoaDon h WHERE h.maDatHang = :maDatHang ORDER BY h.maHD DESC",
                            HoaDon.class
                    )
                    .setParameter("maDatHang", normalizedMaDatHang)
                    .setMaxResults(1)
                    .getResultList();

            if (result.isEmpty()) {
                em.getTransaction().rollback();
                return false;
            }

            HoaDon hoaDon = result.get(0);
            hoaDon.setMaNV(trimToNull(maNV));
            if (hoaDon.getMaDatHang() == null) {
                hoaDon.setMaDatHang(normalizedMaDatHang);
            }
            em.merge(hoaDon);
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

    public boolean updateEmployee(String maHD, String maNV) {
        String normalizedMaHD = trimToNull(maHD);
        if (normalizedMaHD == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            HoaDon hoaDon = em.find(HoaDon.class, normalizedMaHD);
            if (hoaDon == null) {
                em.getTransaction().rollback();
                return false;
            }

            hoaDon.setMaNV(trimToNull(maNV));
            em.merge(hoaDon);
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

    public boolean updateDeliveryEmployee(String maHD, String maNVGiaoHang) {
        String normalizedMaHD = trimToNull(maHD);
        if (normalizedMaHD == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            HoaDon hoaDon = em.find(HoaDon.class, normalizedMaHD);
            if (hoaDon == null) {
                em.getTransaction().rollback();
                return false;
            }

            hoaDon.setMaNVGiaoHang(trimToNull(maNVGiaoHang));
            em.merge(hoaDon);
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

    public boolean updateDeliveryAccepted(String maHD, boolean accepted) {
        String normalizedMaHD = trimToNull(maHD);
        if (normalizedMaHD == null) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            HoaDon hoaDon = em.find(HoaDon.class, normalizedMaHD);
            if (hoaDon == null) {
                em.getTransaction().rollback();
                return false;
            }

            hoaDon.setDaNhanGiaoHang(accepted);
            em.merge(hoaDon);
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

    public boolean updateStatus(String maHD, String trangThai) {
        String normalizedMaHD = trimToNull(maHD);
        String normalizedStatus = trimToNull(trangThai);
        if (normalizedMaHD == null || normalizedStatus == null || !OrderStatusUtils.isRecognized(normalizedStatus)) {
            return false;
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            HoaDon hoaDon = em.find(HoaDon.class, normalizedMaHD);
            if (hoaDon == null) {
                em.getTransaction().rollback();
                return false;
            }

            hoaDon.setTrangThai(normalizedStatus);
            em.merge(hoaDon);
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
