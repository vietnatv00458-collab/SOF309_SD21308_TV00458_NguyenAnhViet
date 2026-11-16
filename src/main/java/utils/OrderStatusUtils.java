package utils;

import java.text.Normalizer;
import java.util.Locale;

public final class OrderStatusUtils {

    private static final String STATUS_WAITING = "CHONHAN";
    private static final String STATUS_PROCESSING_CODE = "DANGLAM";
    private static final String STATUS_DELIVERING_CODE = "DANGGIAO";
    private static final String STATUS_COMPLETED = "HOANTHANH";
    private static final String STATUS_CANCELLED = "DAHUY";
    private static final String STATUS_PROCESSING = "DANGXULY";
    private static final String STATUS_PREPARING = "DANGLAMDON";
    private static final String STATUS_PREPARING_LEGACY = "DANGCHUANBI";
    private static final String STATUS_DELIVERING = "DANGGIAOHANG";
    private static final String STATUS_DELIVERING_LEGACY = "DANGGIAO";

    private OrderStatusUtils() {
    }

    public static boolean isCompleted(String status) {
        String normalized = normalize(status);
        return STATUS_COMPLETED.equals(normalized)
                || "HOAN_THANH".equals(normalized)
                || "DATHANHTOAN".equals(normalized)
                || "COMPLETED".equals(normalized)
                || "COMPLETE".equals(normalized)
                || "DONE".equals(normalized);
    }

    public static boolean isCancelled(String status) {
        String normalized = normalize(status);
        return STATUS_CANCELLED.equals(normalized) || "HUY".equals(normalized);
    }

    public static boolean isPreparing(String status) {
        String normalized = normalize(status);
        return STATUS_WAITING.equals(normalized)
                || "PENDING".equals(normalized)
                || STATUS_PROCESSING_CODE.equals(normalized)
                || STATUS_PROCESSING.equals(normalized)
                || "PROCESSING".equals(normalized)
                || STATUS_PREPARING.equals(normalized)
                || STATUS_PREPARING_LEGACY.equals(normalized)
                || "PREPARING".equals(normalized)
                || "DANGPHACHE".equals(normalized)
                || "DANGPHUCVU".equals(normalized)
                || "WAITING".equals(normalized);
    }

    public static boolean isDelivering(String status) {
        String normalized = normalize(status);
        return STATUS_DELIVERING_CODE.equals(normalized)
                || STATUS_DELIVERING.equals(normalized)
                || STATUS_DELIVERING_LEGACY.equals(normalized)
                || "DELIVERING".equals(normalized)
                || "SHIPPING".equals(normalized);
    }

    public static boolean isActive(String status) {
        return !isCompleted(status) && !isCancelled(status);
    }

    public static boolean isRecognized(String status) {
        String normalized = normalize(status);
        return isPreparing(normalized)
                || isDelivering(normalized)
                || isCompleted(normalized)
                || isCancelled(normalized)
                || "DAGUI".equals(normalized)
                || "DANGCHO".equals(normalized);
    }

    public static boolean canTransition(String currentStatus, String nextStatus) {
        String current = normalize(currentStatus);
        String next = normalize(nextStatus);

        if (next.isEmpty() || isCancelled(current) || isCompleted(current)) {
            return false;
        }

        if (isCancelled(next) || isCompleted(next)) {
            return true;
        }

        if (current.isEmpty()) {
            return isPreparing(next) || isDelivering(next);
        }

        if (isPreparing(current)) {
            return isPreparing(next) || isDelivering(next);
        }

        if (isDelivering(current)) {
            return isDelivering(next) || isCompleted(next);
        }

        return isPreparing(next) || isDelivering(next);
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String stripped = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return stripped.replaceAll("[\\s_-]+", "").toUpperCase(Locale.ROOT);
    }
}
