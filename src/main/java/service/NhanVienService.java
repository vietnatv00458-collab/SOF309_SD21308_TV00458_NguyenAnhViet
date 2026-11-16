package service;

import entity.DatHang;
import entity.HoaDon;
import entity.NhanVien;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import utils.JPAUtil;
import utils.OrderStatusUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NhanVienService {

    private final EntityManagerFactory emf = JPAUtil.EMF;

    public DeletionPreview previewDeletion(String maNV) {
        String normalizedMaNV = trimToNull(maNV);
        if (normalizedMaNV == null) {
            return DeletionPreview.empty();
        }

        EntityManager em = emf.createEntityManager();
        try {
            List<DatHang> datHangs = findDatHangsByEmployee(em, normalizedMaNV);
            List<HoaDon> hoaDons = findHoaDonsByEmployee(em, normalizedMaNV);

            long activeDatHangCount = 0L;
            long activeHoaDonCount = 0L;
            long completedDatHangCount = 0L;
            long completedHoaDonCount = 0L;

            for (DatHang datHang : datHangs) {
                if (datHang == null) {
                    continue;
                }

                if (shouldKeepDatHang(em, datHang)) {
                    completedDatHangCount++;
                    continue;
                }

                if (OrderStatusUtils.isActive(datHang.getTrangThai())) {
                    activeDatHangCount++;
                }
            }

            for (HoaDon hoaDon : hoaDons) {
                if (hoaDon == null) {
                    continue;
                }

                if (shouldKeepHoaDon(em, hoaDon)) {
                    completedHoaDonCount++;
                    continue;
                }

                if (OrderStatusUtils.isActive(hoaDon.getTrangThai())) {
                    activeHoaDonCount++;
                }
            }

            return new DeletionPreview(normalizedMaNV, activeDatHangCount, activeHoaDonCount, completedDatHangCount, completedHoaDonCount);
        } finally {
            em.close();
        }
    }

    public DeletionResult deleteEmployee(String maNV) {
        String normalizedMaNV = trimToNull(maNV);
        if (normalizedMaNV == null) {
            return DeletionResult.notFound();
        }

        DeletionPreview preview = previewDeletion(normalizedMaNV);
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            NhanVien employee = em.find(NhanVien.class, normalizedMaNV);
            if (employee == null) {
                em.getTransaction().rollback();
                return DeletionResult.notFound();
            }

            if (Boolean.TRUE.equals(employee.getDaXoa())) {
                em.getTransaction().rollback();
                return DeletionResult.alreadyDeleted(normalizedMaNV);
            }

            List<DatHang> datHangs = findDatHangsByEmployee(em, normalizedMaNV);
            List<HoaDon> hoaDons = findHoaDonsByEmployee(em, normalizedMaNV);

            List<String> datHangIdsToDelete = new ArrayList<>();
            List<String> hoaDonIdsToDelete = new ArrayList<>();

            for (DatHang datHang : datHangs) {
                if (datHang == null || !OrderStatusUtils.isActive(datHang.getTrangThai()) || shouldKeepDatHang(em, datHang)) {
                    continue;
                }
                String maDatHang = trimToNull(datHang.getMaDatHang());
                if (maDatHang != null) {
                    datHangIdsToDelete.add(maDatHang);
                }
            }

            for (HoaDon hoaDon : hoaDons) {
                if (hoaDon == null || !OrderStatusUtils.isActive(hoaDon.getTrangThai()) || shouldKeepHoaDon(em, hoaDon)) {
                    continue;
                }
                String maHD = trimToNull(hoaDon.getMaHD());
                if (maHD != null) {
                    hoaDonIdsToDelete.add(maHD);
                }
            }

            int chiTietDeleted = 0;
            int doanhThuDeleted = 0;
            if (!hoaDonIdsToDelete.isEmpty()) {
                chiTietDeleted = em.createQuery(
                                "DELETE FROM ChiTietHoaDon c WHERE c.maHD IN :maHDs"
                        )
                        .setParameter("maHDs", hoaDonIdsToDelete)
                        .executeUpdate();

                doanhThuDeleted = em.createQuery(
                                "DELETE FROM DoanhThu d WHERE d.maHD IN :maHDs"
                        )
                        .setParameter("maHDs", hoaDonIdsToDelete)
                        .executeUpdate();

                em.createQuery("DELETE FROM HoaDon h WHERE h.maHD IN :maHDs")
                        .setParameter("maHDs", hoaDonIdsToDelete)
                        .executeUpdate();
            }

            int datHangDeleted = 0;
            if (!datHangIdsToDelete.isEmpty()) {
                em.createQuery("DELETE FROM ChiTietGioHang c WHERE c.maDatHang IN :maDatHangs")
                        .setParameter("maDatHangs", datHangIdsToDelete)
                        .executeUpdate();
                datHangDeleted = em.createQuery("DELETE FROM DatHang d WHERE d.maDatHang IN :maDatHangs")
                        .setParameter("maDatHangs", datHangIdsToDelete)
                        .executeUpdate();
            }

            employee.setDaXoa(Boolean.TRUE);
            em.merge(employee);

            em.getTransaction().commit();

            return new DeletionResult(
                    true,
                    normalizedMaNV,
                    datHangDeleted,
                    hoaDonIdsToDelete.size(),
                    chiTietDeleted,
                    doanhThuDeleted,
                    preview.getCompletedDatHangCount(),
                    preview.getCompletedHoaDonCount()
            );
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    private List<DatHang> findDatHangsByEmployee(EntityManager em, String maNV) {
        TypedQuery<DatHang> query = em.createQuery(
                "SELECT d FROM DatHang d WHERE d.maNV = :maNV ORDER BY d.maDatHang",
                DatHang.class
        );
        return query.setParameter("maNV", maNV).getResultList();
    }

    private List<HoaDon> findHoaDonsByEmployee(EntityManager em, String maNV) {
        TypedQuery<HoaDon> query = em.createQuery(
                "SELECT h FROM HoaDon h WHERE h.maNV = :maNV ORDER BY h.maHD",
                HoaDon.class
        );
        return query.setParameter("maNV", maNV).getResultList();
    }

    private boolean shouldKeepDatHang(EntityManager em, DatHang datHang) {
        if (datHang == null) {
            return false;
        }

        if (OrderStatusUtils.isCompleted(datHang.getTrangThai())) {
            return true;
        }

        String maDatHang = trimToNull(datHang.getMaDatHang());
        if (maDatHang == null) {
            return false;
        }

        List<HoaDon> invoices = em.createQuery(
                        "SELECT h FROM HoaDon h WHERE h.maDatHang = :maDatHang ORDER BY h.maHD",
                        HoaDon.class
                )
                .setParameter("maDatHang", maDatHang)
                .getResultList();
        for (HoaDon invoice : invoices) {
            if (shouldKeepHoaDon(em, invoice)) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldKeepHoaDon(EntityManager em, HoaDon hoaDon) {
        if (hoaDon == null) {
            return false;
        }

        if (OrderStatusUtils.isCompleted(hoaDon.getTrangThai())) {
            return true;
        }

        String maHD = trimToNull(hoaDon.getMaHD());
        if (maHD == null) {
            return false;
        }

        Long revenueCount = em.createQuery(
                        "SELECT COUNT(d) FROM DoanhThu d WHERE d.maHD = :maHD",
                        Long.class
                )
                .setParameter("maHD", maHD)
                .getSingleResult();
        return revenueCount != null && revenueCount > 0L;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static final class DeletionPreview {
        private final String maNV;
        private final long activeDatHangCount;
        private final long activeHoaDonCount;
        private final long completedDatHangCount;
        private final long completedHoaDonCount;

        private DeletionPreview(String maNV, long activeDatHangCount, long activeHoaDonCount,
                                long completedDatHangCount, long completedHoaDonCount) {
            this.maNV = maNV;
            this.activeDatHangCount = activeDatHangCount;
            this.activeHoaDonCount = activeHoaDonCount;
            this.completedDatHangCount = completedDatHangCount;
            this.completedHoaDonCount = completedHoaDonCount;
        }

        public static DeletionPreview empty() {
            return new DeletionPreview(null, 0L, 0L, 0L, 0L);
        }

        public String getMaNV() {
            return maNV;
        }

        public long getActiveDatHangCount() {
            return activeDatHangCount;
        }

        public long getActiveHoaDonCount() {
            return activeHoaDonCount;
        }

        public long getCompletedDatHangCount() {
            return completedDatHangCount;
        }

        public long getCompletedHoaDonCount() {
            return completedHoaDonCount;
        }

        public long getPendingOrderCount() {
            return activeDatHangCount + activeHoaDonCount;
        }

        public long getCompletedOrderCount() {
            return completedDatHangCount + completedHoaDonCount;
        }
    }

    public static final class DeletionResult {
        private final boolean success;
        private final String maNV;
        private final int deletedDatHangCount;
        private final int deletedHoaDonCount;
        private final int deletedChiTietHoaDonCount;
        private final int deletedDoanhThuCount;
        private final long keptCompletedDatHangCount;
        private final long keptCompletedHoaDonCount;

        private DeletionResult(boolean success, String maNV, int deletedDatHangCount, int deletedHoaDonCount,
                               int deletedChiTietHoaDonCount, int deletedDoanhThuCount,
                               long keptCompletedDatHangCount, long keptCompletedHoaDonCount) {
            this.success = success;
            this.maNV = maNV;
            this.deletedDatHangCount = deletedDatHangCount;
            this.deletedHoaDonCount = deletedHoaDonCount;
            this.deletedChiTietHoaDonCount = deletedChiTietHoaDonCount;
            this.deletedDoanhThuCount = deletedDoanhThuCount;
            this.keptCompletedDatHangCount = keptCompletedDatHangCount;
            this.keptCompletedHoaDonCount = keptCompletedHoaDonCount;
        }

        public static DeletionResult notFound() {
            return new DeletionResult(false, null, 0, 0, 0, 0, 0L, 0L);
        }

        public static DeletionResult alreadyDeleted(String maNV) {
            return new DeletionResult(true, maNV, 0, 0, 0, 0, 0L, 0L);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMaNV() {
            return maNV;
        }

        public int getDeletedDatHangCount() {
            return deletedDatHangCount;
        }

        public int getDeletedHoaDonCount() {
            return deletedHoaDonCount;
        }

        public int getDeletedChiTietHoaDonCount() {
            return deletedChiTietHoaDonCount;
        }

        public int getDeletedDoanhThuCount() {
            return deletedDoanhThuCount;
        }

        public long getKeptCompletedDatHangCount() {
            return keptCompletedDatHangCount;
        }

        public long getKeptCompletedHoaDonCount() {
            return keptCompletedHoaDonCount;
        }

        public long getDeletedOrderCount() {
            return deletedDatHangCount + deletedHoaDonCount;
        }
    }
}
