package utils;

import entity.ChiTietGioHang;
import entity.ChiTietHoaDon;
import entity.DatHang;
import entity.DoanhThu;
import entity.GioHang;
import entity.HoaDon;
import entity.KhachHang;
import entity.SanPham;
import entity.Size;
import dao.HoaDonDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderPersistenceService {

    private static final int MAX_ID_ATTEMPTS = 1000;
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{([^{}]*)}");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"(?:\\\\.|[^\"])*\"|-?\\d+(?:\\.\\d+)?|true|false|null)");
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    public PersistedOrder persistPaidOrder(PaymentSessionStore.PaymentSession session, String source) {
        if (session == null || trimToNull(session.getMaKH()) == null) {
            return PersistedOrder.empty();
        }

        List<CartItem> items = parseCartItems(session.getCartJson());
        if (items.isEmpty()) {
            return PersistedOrder.empty();
        }

        EntityManager em = JPAUtil.EMF.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            String maKH = trimToNull(session.getMaKH());
            KhachHang khachHang = em.find(KhachHang.class, maKH);
            if (khachHang == null) {
                throw new IllegalStateException("Khách hàng không tồn tại.");
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            BigDecimal originalTotal = calculateSubtotal(items);
            BigDecimal discountPercent = normalizePercent(session.getPromotionPercent());
            BigDecimal discountAmount = calculateDiscountAmount(originalTotal, discountPercent);
            BigDecimal finalTotal = originalTotal.subtract(discountAmount);
            boolean deliveryOrder = isDeliveryOrder(session.getDeliveryMethod());

            if (finalTotal.signum() <= 0) {
                throw new IllegalStateException("Tổng thanh toán không hợp lệ.");
            }

            String maGioHang = nextId(em, GioHang.class, "GH");
            GioHang gioHang = new GioHang();
            gioHang.setMaGioHang(maGioHang);
            gioHang.setMaKH(maKH);
            gioHang.setNgayTao(now);
            em.persist(gioHang);

            String maDatHang = nextId(em, DatHang.class, "DH");
            DatHang datHang = new DatHang();
            datHang.setMaDatHang(maDatHang);
            datHang.setMaKH(maKH);
            datHang.setMaNV(null);
            datHang.setMaNVGiaoHang(null);
            datHang.setDaNhanGiaoHang(Boolean.FALSE);
            datHang.setNgayDat(now);
            datHang.setTrangThai("CHO_NHAN");
            datHang.setSoDienThoai(khachHang != null ? trimToNull(khachHang.getSdt()) : null);
            datHang.setDiaChiGiaoHang(deliveryOrder ? resolveDeliveryAddress(session, khachHang) : null);
            datHang.setTongTien(finalTotal);
            em.persist(datHang);

            String maHD = hoaDonDAO.generateNextMaHD();
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHD(maHD);
            hoaDon.setMaKH(maKH);
            hoaDon.setMaNV(null);
            hoaDon.setMaNVGiaoHang(null);
            hoaDon.setDaNhanGiaoHang(Boolean.FALSE);
            hoaDon.setMaDatHang(maDatHang);
            hoaDon.setNgayLap(now);
            hoaDon.setMaKM(trimToNull(session.getPromotionCode()));
            hoaDon.setTongTien(originalTotal);
            hoaDon.setGiamGia(discountAmount);
            hoaDon.setThanhTien(finalTotal);
            hoaDon.setSoDienThoai(khachHang != null ? trimToNull(khachHang.getSdt()) : null);
            hoaDon.setDiaChiGiaoHang(deliveryOrder ? resolveDeliveryAddress(session, khachHang) : null);
            hoaDon.setTrangThai("CHO_NHAN");
            em.persist(hoaDon);

            for (CartItem item : items) {
                SanPham product = em.find(SanPham.class, item.maSP);
                if (product == null) {
                    throw new IllegalStateException("Sản phẩm " + item.maSP + " không tồn tại.");
                }

                Size size = em.find(Size.class, item.size);
                if (size == null) {
                    throw new IllegalStateException("Size " + item.size + " không tồn tại.");
                }

                if (item.qty <= 0) {
                    throw new IllegalStateException("Số lượng sản phẩm không hợp lệ.");
                }

                if (item.price == null || item.price.signum() <= 0) {
                    throw new IllegalStateException("Đơn giá sản phẩm không hợp lệ.");
                }

                BigDecimal lineTotal = item.price.multiply(BigDecimal.valueOf(item.qty));

                ChiTietGioHang chiTietGioHang = new ChiTietGioHang();
                chiTietGioHang.setMaGioHang(maGioHang);
                chiTietGioHang.setMaDatHang(maDatHang);
                chiTietGioHang.setMaSP(item.maSP);
                chiTietGioHang.setMaSize(item.size);
                chiTietGioHang.setSoLuong(item.qty);
                chiTietGioHang.setDonGia(item.price);
                em.persist(chiTietGioHang);

                ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();
                chiTietHoaDon.setMaHD(maHD);
                chiTietHoaDon.setMaSP(item.maSP);
                chiTietHoaDon.setMaSize(item.size);
                chiTietHoaDon.setSoLuong(item.qty);
                chiTietHoaDon.setDonGia(item.price);
                chiTietHoaDon.setThanhTien(lineTotal);
                em.persist(chiTietHoaDon);
            }

            DoanhThu doanhThu = new DoanhThu();
            doanhThu.setMaDT(nextId(em, DoanhThu.class, "DT"));
            doanhThu.setMaHD(maHD);
            doanhThu.setNgay(LocalDate.now());
            doanhThu.setDoanhThu(finalTotal);
            doanhThu.setChiPhi(BigDecimal.ZERO);
            em.persist(doanhThu);

            tx.commit();
            return new PersistedOrder(maGioHang, maDatHang, maHD, true);
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public static BigDecimal calculateCartSubtotal(String cartJson) {
        return new OrderPersistenceService().calculateSubtotalFromJson(cartJson);
    }

    private BigDecimal calculateSubtotalFromJson(String cartJson) {
        return calculateSubtotal(parseCartItems(cartJson));
    }

    private BigDecimal calculateSubtotal(List<CartItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.price.multiply(BigDecimal.valueOf(item.qty)));
        }
        return normalizeMoney(total);
    }

    private BigDecimal calculateDiscountAmount(BigDecimal subtotal, BigDecimal discountPercent) {
        BigDecimal normalizedSubtotal = normalizeMoney(subtotal);
        BigDecimal normalizedPercent = normalizePercent(discountPercent);
        if (normalizedSubtotal.signum() <= 0 || normalizedPercent.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountFactor = BigDecimal.ONE.subtract(normalizedPercent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal discountedTotal = normalizedSubtotal.multiply(discountFactor);
        BigDecimal roundedDiscountedTotal = normalizeMoney(discountedTotal);
        BigDecimal discountAmount = normalizedSubtotal.subtract(roundedDiscountedTotal);
        return discountAmount.signum() > 0 ? discountAmount : BigDecimal.ZERO;
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizePercent(BigDecimal percent) {
        if (percent == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal normalized = percent;
        if (normalized.signum() < 0) {
            normalized = BigDecimal.ZERO;
        }
        if (normalized.compareTo(BigDecimal.valueOf(100)) >= 0) {
            normalized = BigDecimal.valueOf(99.99);
        }
        return normalized;
    }

    private String nextId(EntityManager em, Class<?> entityClass, String prefix) {
        long seed = Math.abs(System.currentTimeMillis() % 100_000_000L);
        for (int attempt = 0; attempt < MAX_ID_ATTEMPTS; attempt++) {
            String id = prefix + String.format(Locale.ROOT, "%08d", (seed + attempt) % 100_000_000L);
            if (em.find(entityClass, id) == null) {
                return id;
            }
        }
        throw new IllegalStateException("Không tạo được mã mới cho " + prefix);
    }

    private List<CartItem> parseCartItems(String cartJson) {
        List<CartItem> items = new ArrayList<>();
        String json = trimToNull(cartJson);
        if (json == null) {
            return items;
        }

        Matcher objectMatcher = OBJECT_PATTERN.matcher(json);
        while (objectMatcher.find()) {
            String objectBody = objectMatcher.group(1);
            CartItem item = new CartItem();

            Matcher fieldMatcher = FIELD_PATTERN.matcher(objectBody);
            while (fieldMatcher.find()) {
                String key = fieldMatcher.group(1);
                String value = parseJsonValue(fieldMatcher.group(2));

                if ("maSP".equals(key)) {
                    item.maSP = normalizeProductId(value);
                } else if ("name".equals(key)) {
                    item.name = trimToNull(value);
                } else if ("size".equals(key)) {
                    item.size = normalizeSize(value);
                } else if ("qty".equals(key)) {
                    item.qty = parseInt(value, 0);
                } else if ("price".equals(key)) {
                    item.price = parseMoney(value);
                }
            }

            if (item.maSP == null) {
                item.maSP = mapProductNameToId(item.name);
            }
            if (item.size == null) {
                item.size = "M";
            }
            if (item.qty <= 0) {
                item.qty = 0;
            }
            if (item.price == null || item.price.signum() < 0) {
                item.price = BigDecimal.ZERO;
            }

            if (item.maSP != null) {
                items.add(item);
            }
        }

        return items;
    }

    private String parseJsonValue(String rawValue) {
        if (rawValue == null || "null".equals(rawValue)) {
            return null;
        }
        if (!rawValue.startsWith("\"")) {
            return rawValue;
        }

        String value = rawValue.substring(1, rawValue.length() - 1);
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\/", "/")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private String normalizeProductId(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String upper = trimmed.toUpperCase(Locale.ROOT);
        return upper.matches("SP\\d{2}") ? upper : null;
    }

    private String normalizeSize(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String upper = trimmed.toUpperCase(Locale.ROOT);
        return ("S".equals(upper) || "M".equals(upper) || "L".equals(upper)) ? upper : "M";
    }

    private String mapProductNameToId(String name) {
        String normalized = normalizeText(name);
        if (normalized.contains("CAFE SUA") || normalized.contains("CA PHE SUA")) return "SP02";
        if (normalized.contains("CAFE DEN") || normalized.contains("CA PHE DEN")) return "SP01";
        if (normalized.contains("BAC XIU")) return "SP03";
        if (normalized.contains("LATTE")) return "SP04";
        if (normalized.contains("CAPPUCCINO")) return "SP05";
        if (normalized.contains("ESPRESSO")) return "SP06";
        if (normalized.contains("MOCHA") || normalized.contains("CACAO") || normalized.contains("CHOCOLATE")) return "SP07";
        if (normalized.contains("TRA VAI")) return "SP09";
        if (normalized.contains("TRA CHANH")) return "SP10";
        if (normalized.contains("MATCHA")) return "SP12";
        if (normalized.contains("TRA SUA")) return "SP11";
        if (normalized.contains("SINH TO BO")) return "SP13";
        if (normalized.contains("SINH TO DAU")) return "SP14";
        if (normalized.contains("CAM")) return "SP15";
        if (normalized.contains("TRA DAO")) return "SP08";
        return null;
    }

    private boolean isDeliveryOrder(String deliveryMethod) {
        String normalized = normalizeDeliveryMethod(deliveryMethod);
        return "GIAO_HANG".equals(normalized) || "DELIVERY".equals(normalized);
    }

    private String normalizeDeliveryMethod(String deliveryMethod) {
        String trimmed = trimToNull(deliveryMethod);
        if (trimmed == null) {
            return "TAI_QUAN";
        }

        String normalized = trimmed.replace(" ", "").replace("-", "_").toUpperCase(Locale.ROOT);
        if ("GIAO_HANG".equals(normalized) || "DELIVERY".equals(normalized)) {
            return "GIAO_HANG";
        }

        return "TAI_QUAN";
    }

    private String resolveDeliveryAddress(PaymentSessionStore.PaymentSession session, KhachHang khachHang) {
        String fromSession = trimToNull(session != null ? session.getDeliveryAddress() : null);
        if (fromSession != null) {
            return fromSession;
        }

        return khachHang != null ? trimToNull(khachHang.getDiaChi()) : null;
    }

    private String normalizeText(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return "";
        }
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('Đ', 'D')
                .replace('đ', 'd')
                .toUpperCase(Locale.ROOT);
    }

    private BigDecimal parseMoney(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return BigDecimal.ZERO;
        }
        try {
            String digitsOnly = trimmed.replaceAll("[^\\d]", "");
            if (digitsOnly.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(digitsOnly);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static final class PersistedOrder {
        private final String maGioHang;
        private final String maDatHang;
        private final String maHD;
        private final boolean saved;

        private PersistedOrder(String maGioHang, String maDatHang, String maHD, boolean saved) {
            this.maGioHang = maGioHang;
            this.maDatHang = maDatHang;
            this.maHD = maHD;
            this.saved = saved;
        }

        private static PersistedOrder empty() {
            return new PersistedOrder(null, null, null, false);
        }

        public String getMaGioHang() {
            return maGioHang;
        }

        public String getMaDatHang() {
            return maDatHang;
        }

        public String getMaHD() {
            return maHD;
        }

        public boolean isSaved() {
            return saved;
        }
    }

    private static final class CartItem {
        private String maSP;
        private String name;
        private String size;
        private int qty;
        private BigDecimal price;
    }
}
