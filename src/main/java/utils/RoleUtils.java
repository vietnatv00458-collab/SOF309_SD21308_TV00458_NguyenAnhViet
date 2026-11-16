package utils;

import java.text.Normalizer;
import java.util.Locale;

public final class RoleUtils {

    private RoleUtils() {
    }

    public static String normalize(String role) {
        if (role == null) {
            return "";
        }

        String stripped = Normalizer.normalize(role.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return stripped.replace(" ", "").toUpperCase(Locale.ROOT);
    }

    public static boolean isManagerRole(String role) {
        String normalized = normalize(role);
        return "ADMIN".equals(normalized)
                || "QUANLY".equals(normalized)
                || "MANAGER".equals(normalized);
    }

    public static boolean isEmployeeRole(String role) {
        String normalized = normalize(role);
        return "NHANVIEN".equals(normalized)
                || "EMPLOYEE".equals(normalized)
                || "STAFF".equals(normalized);
    }

    public static boolean isDeliveryRole(String role) {
        String normalized = normalize(role);
        return "GIAOHANG".equals(normalized)
                || "DELIVERY".equals(normalized)
                || "SHIPPER".equals(normalized);
    }

    public static boolean isCustomerRole(String role) {
        String normalized = normalize(role);
        return "KHACHHANG".equals(normalized)
                || "CUSTOMER".equals(normalized);
    }

    public static String displayRole(String role) {
        if (isManagerRole(role)) {
            return "Quản lý";
        }

        if (isEmployeeRole(role)) {
            return "Nhân viên";
        }

        if (isDeliveryRole(role)) {
            return "Giao hàng";
        }

        if (isCustomerRole(role)) {
            return "Khách hàng";
        }

        return role == null ? "" : role;
    }

    public static String homePath(String role) {
        if (isManagerRole(role)) {
            return "/dashboard";
        }

        if (isEmployeeRole(role)) {
            return "/nhanvien-home";
        }

        if (isDeliveryRole(role)) {
            return "/giaohang-home";
        }

        if (isCustomerRole(role)) {
            return "/customer-shop";
        }

        return "/login";
    }
}
