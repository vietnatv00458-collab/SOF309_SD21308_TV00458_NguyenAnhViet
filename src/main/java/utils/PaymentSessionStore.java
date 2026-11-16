package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class PaymentSessionStore {

    public static final long TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10);

    private static final long RETENTION_MILLIS = TimeUnit.MINUTES.toMillis(15);
    private static final String DELIVERY_METHOD_PICKUP = "TAI_QUAN";
    private static final String DELIVERY_METHOD_DELIVERY = "GIAO_HANG";
    private static final ConcurrentHashMap<String, PaymentSession> SESSIONS = new ConcurrentHashMap<>();
    private static final OrderPersistenceService ORDER_PERSISTENCE_SERVICE = new OrderPersistenceService();

    private PaymentSessionStore() {
    }

    public static PaymentSession create(BigDecimal amount, String ownerKey, String note, String maKH, String cartJson) {
        return create(amount, BigDecimal.ZERO, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, ownerKey, note, null, maKH, cartJson,
                DELIVERY_METHOD_PICKUP, null);
    }

    public static PaymentSession create(BigDecimal originalAmount, BigDecimal discountAmount, String promotionCode,
                                        String promotionName, BigDecimal promotionPercent, String ownerKey,
                                        String note, String maNV, String maKH, String cartJson) {
        return create(originalAmount, discountAmount, promotionCode, promotionName, null, promotionPercent, BigDecimal.ZERO, ownerKey,
                note, maNV, maKH, cartJson, DELIVERY_METHOD_PICKUP, null);
    }

    public static PaymentSession create(BigDecimal originalAmount, BigDecimal discountAmount, String promotionCode,
                                        String promotionName, String promotionType, BigDecimal promotionPercent,
                                        BigDecimal promotionMaxAmount, String ownerKey,
                                        String note, String maNV, String maKH, String cartJson,
                                        String deliveryMethod, String deliveryAddress) {
        purgeStaleSessions();

        PaymentSession session;
        String sessionId;
        BigDecimal normalizedOriginal = normalizeAmount(originalAmount);
        BigDecimal normalizedDiscount = normalizeDiscount(discountAmount, normalizedOriginal);
        BigDecimal normalizedFinal = normalizedOriginal.subtract(normalizedDiscount);
        BigDecimal normalizedMaxAmount = normalizeAmount(promotionMaxAmount);
        BigDecimal normalizedPercent = normalizePercent(promotionPercent);
        String normalizedDeliveryMethod = normalizeDeliveryMethod(deliveryMethod);

        do {
            sessionId = generateSessionId();
            session = new PaymentSession(
                    sessionId,
                    normalizedFinal,
                    normalizedOriginal,
                    normalizedDiscount,
                    trimToNull(promotionCode),
                    trimToNull(promotionName),
                    normalizePromotionType(promotionType),
                    normalizedPercent,
                    normalizedMaxAmount,
                    trimToNull(ownerKey),
                    normalizeNote(note),
                    trimToNull(maNV),
                    trimToNull(maKH),
                    trimToNull(cartJson),
                    normalizedDeliveryMethod,
                    normalizedDeliveryMethod.equals(DELIVERY_METHOD_DELIVERY) ? trimToNull(deliveryAddress) : null
            );
        } while (SESSIONS.putIfAbsent(sessionId, session) != null);

        return session;
    }

    public static PaymentSession get(String sessionId) {
        String key = trimToNull(sessionId);
        if (key == null) {
            return null;
        }

        PaymentSession session = SESSIONS.get(key);
        if (session == null) {
            return null;
        }

        synchronized (session) {
            session.refreshIfExpired();
        }

        purgeStaleSessions();
        return session;
    }

    public static PaymentSession findLatestPendingByAmount(BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        purgeStaleSessions();

        PaymentSession latest = null;
        for (PaymentSession session : SESSIONS.values()) {
            if (session == null) {
                continue;
            }

            synchronized (session) {
                session.refreshIfExpired();
                if (!session.isPending() || session.getAmount().compareTo(normalizedAmount) != 0) {
                    continue;
                }

                if (latest == null || session.getCreatedAt() > latest.getCreatedAt()) {
                    latest = session;
                }
            }
        }

        return latest;
    }

    public static PaymentSession complete(String sessionId, String source) {
        String key = trimToNull(sessionId);
        if (key == null) {
            return null;
        }

        PaymentSession session = SESSIONS.get(key);
        if (session == null) {
            return null;
        }

        synchronized (session) {
            session.refreshIfExpired();

            if (session.isCompleted() || session.isFailed()) {
                return session;
            }

            if (session.isExpired()) {
                session.markFailed("Thanh toán thất bại");
                return session;
            }

            if (!persistPaidOrder(session, source)) {
                session.markFailed(session.getOrderPersistError() != null
                        ? session.getOrderPersistError()
                        : "Không lưu được đơn hàng vào CSDL.");
                return session;
            }

            session.markCompleted(source);
        }

        purgeStaleSessions();
        return session;
    }

    private static boolean persistPaidOrder(PaymentSession session, String source) {
        if (session.isOrderPersisted()) {
            return true;
        }

        try {
            OrderPersistenceService.PersistedOrder order =
                    ORDER_PERSISTENCE_SERVICE.persistPaidOrder(session, source);
            session.markOrderPersisted(order);
            return true;
        } catch (Exception ex) {
            session.markOrderPersistError(ex.getMessage());
            return false;
        }
    }

    private static String generateSessionId() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(0, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizeDiscount(BigDecimal amount, BigDecimal maxAmount) {
        BigDecimal normalized = normalizeAmount(amount);
        BigDecimal ceiling = normalizeAmount(maxAmount);
        if (normalized.compareTo(ceiling) > 0) {
            return ceiling;
        }
        return normalized;
    }

    private static BigDecimal normalizePercent(BigDecimal percent) {
        if (percent == null || percent.signum() < 0) {
            return BigDecimal.ZERO;
        }
        if (percent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return BigDecimal.valueOf(99.99);
        }
        return percent;
    }

    private static String normalizeNote(String note) {
        String trimmed = trimToNull(note);
        return trimmed != null ? trimmed : "Thanh toán";
    }

    private static String normalizeDeliveryMethod(String deliveryMethod) {
        String trimmed = trimToNull(deliveryMethod);
        if (trimmed == null) {
            return DELIVERY_METHOD_PICKUP;
        }

        String normalized = trimmed.replace(" ", "").replace("-", "_").toUpperCase(Locale.ROOT);
        if (DELIVERY_METHOD_DELIVERY.equals(normalized) || "DELIVERY".equals(normalized)) {
            return DELIVERY_METHOD_DELIVERY;
        }

        return DELIVERY_METHOD_PICKUP;
    }

    private static String buildPaymentReference(String note, String sessionId) {
        String token = trimToNull(sessionId);
        if (token != null) {
            return token;
        }

        String base = trimToNull(note);
        return base != null ? base : "Thanh toán";
    }

    private static void purgeStaleSessions() {
        long now = System.currentTimeMillis();
        SESSIONS.entrySet().removeIf(entry -> {
            PaymentSession session = entry.getValue();
            return session != null && now - session.getCreatedAt() > RETENTION_MILLIS;
        });
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static final class PaymentSession {

        private final String sessionId;
        private final BigDecimal amount;
        private final BigDecimal originalAmount;
        private final BigDecimal discountAmount;
        private final String promotionCode;
        private final String promotionName;
        private final String promotionType;
        private final BigDecimal promotionPercent;
        private final BigDecimal promotionMaxAmount;
        private final String ownerKey;
        private final String note;
        private final String maNV;
        private final String maKH;
        private final String cartJson;
        private final String deliveryMethod;
        private final String deliveryAddress;
        private final long createdAt;
        private final long expiresAt;

        private volatile long updatedAt;
        private volatile long completedAt;
        private volatile String status;
        private volatile String statusText;
        private volatile String completedSource;
        private volatile boolean orderPersisted;
        private volatile String orderPersistError;
        private volatile String maGioHang;
        private volatile String maDatHang;
        private volatile String maHD;

        private PaymentSession(String sessionId, BigDecimal amount, BigDecimal originalAmount,
                                BigDecimal discountAmount, String promotionCode, String promotionName,
                                String promotionType, BigDecimal promotionPercent, BigDecimal promotionMaxAmount,
                                String ownerKey, String note,
                                String maNV, String maKH, String cartJson,
                                String deliveryMethod, String deliveryAddress) {
            this.sessionId = sessionId;
            this.amount = amount;
            this.originalAmount = originalAmount;
            this.discountAmount = discountAmount;
            this.promotionCode = promotionCode;
            this.promotionName = promotionName;
            this.promotionType = promotionType;
            this.promotionPercent = promotionPercent;
            this.promotionMaxAmount = promotionMaxAmount;
            this.ownerKey = ownerKey;
            this.note = note;
            this.maNV = maNV;
            this.maKH = maKH;
            this.cartJson = cartJson;
            this.deliveryMethod = deliveryMethod;
            this.deliveryAddress = deliveryAddress;
            this.createdAt = System.currentTimeMillis();
            this.expiresAt = this.createdAt + TIMEOUT_MILLIS;
            this.updatedAt = this.createdAt;
            this.status = "PENDING";
            this.statusText = "Chờ thanh toán";
        }

        private void refreshIfExpired() {
            if (isPending() && isExpired()) {
                markFailed("Thanh toán thất bại");
            }
        }

        private void markCompleted(String source) {
            if (!isPending()) {
                return;
            }

            this.status = "COMPLETED";
            this.statusText = "Hoàn thành";
            this.completedAt = System.currentTimeMillis();
            this.updatedAt = this.completedAt;
            this.completedSource = trimToNull(source);
        }

        private void markFailed(String message) {
            if (isCompleted()) {
                return;
            }

            this.status = "FAILED";
            this.statusText = trimToNull(message) != null ? message.trim() : "Thanh toán thất bại";
            this.completedAt = System.currentTimeMillis();
            this.updatedAt = this.completedAt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public BigDecimal getOriginalAmount() {
            return originalAmount;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public String getPromotionCode() {
            return promotionCode;
        }

        public String getPromotionName() {
            return promotionName;
        }

        public String getPromotionType() {
            return promotionType;
        }

        public BigDecimal getPromotionPercent() {
            return promotionPercent;
        }

        public BigDecimal getPromotionMaxAmount() {
            return promotionMaxAmount;
        }

        public String getOwnerKey() {
            return ownerKey;
        }

        public String getNote() {
            return note;
        }

        public String getMaNV() {
            return maNV;
        }

        public String getMaKH() {
            return maKH;
        }

        public String getCartJson() {
            return cartJson;
        }

        public String getDeliveryMethod() {
            return deliveryMethod;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public long getUpdatedAt() {
            return updatedAt;
        }

        public long getCompletedAt() {
            return completedAt;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public long getRemainingMillis() {
            return Math.max(0L, expiresAt - System.currentTimeMillis());
        }

        public long getRemainingSeconds() {
            return Math.max(0L, (getRemainingMillis() + 999L) / 1000L);
        }

        public String getStatus() {
            return status;
        }

        public String getStatusText() {
            return statusText;
        }

        public String getCompletedSource() {
            return completedSource;
        }

        public boolean isOrderPersisted() {
            return orderPersisted;
        }

        public String getOrderPersistError() {
            return orderPersistError;
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

        private void markOrderPersisted(OrderPersistenceService.PersistedOrder order) {
            this.orderPersisted = order != null && order.isSaved();
            this.orderPersistError = null;
            this.maGioHang = order != null ? order.getMaGioHang() : null;
            this.maDatHang = order != null ? order.getMaDatHang() : null;
            this.maHD = order != null ? order.getMaHD() : null;
        }

        private void markOrderPersistError(String message) {
            this.orderPersisted = false;
            this.orderPersistError = trimToNull(message) != null
                    ? message.trim()
                    : "Không lưu được đơn hàng vào CSDL.";
        }

        public String getPaymentNote() {
            return buildPaymentReference(note, sessionId);
        }

        public boolean isPending() {
            return "PENDING".equals(status);
        }

        public boolean isCompleted() {
            return "COMPLETED".equals(status);
        }

        public boolean isFailed() {
            return "FAILED".equals(status);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expiresAt;
        }

        public boolean isTerminal() {
            return isCompleted() || isFailed();
        }
    }

    private static String normalizePromotionType(String promotionType) {
        String trimmed = trimToNull(promotionType);
        if (trimmed == null) {
            return null;
        }

        String normalized = trimmed.replace(" ", "").replace("-", "").replace("_", "").toUpperCase(Locale.ROOT);
        if ("AMOUNT".equals(normalized) || "SOTIEN".equals(normalized) || "MONEY".equals(normalized)) {
            return "AMOUNT";
        }

        if ("PERCENT".equals(normalized) || "PHANTRAM".equals(normalized)) {
            return "PERCENT";
        }

        return normalized;
    }
}
