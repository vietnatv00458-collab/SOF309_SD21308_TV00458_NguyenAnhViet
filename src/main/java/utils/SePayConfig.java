package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class SePayConfig {

    private static final String DEFAULT_BANK_NAME = "Ngân Hàng TMCP Quân Đội (MB)";
    private static final String DEFAULT_BANK_CODE = "MB";
    private static final String DEFAULT_ACCOUNT_NUMBER = "686110708";
    private static final String DEFAULT_ACCOUNT_NAME = "HAI TRAN DANG KHOA";
    private static final String DEFAULT_PAYMENT_NOTE = "Thanh toán";
    private static final Properties PROPERTIES = loadProperties();

    private SePayConfig() {
    }

    public static String getBankName() {
        return valueOrDefault(firstNonBlank(
                property("sepay.bankName"),
                systemProperty("sepay.bankName"),
                environment("SEPAY_BANK_NAME")
        ), DEFAULT_BANK_NAME);
    }

    public static String getBankCode() {
        return valueOrDefault(firstNonBlank(
                property("sepay.bankCode"),
                systemProperty("sepay.bankCode"),
                environment("SEPAY_BANK_CODE")
        ), DEFAULT_BANK_CODE);
    }

    public static String getAccountNumber() {
        return valueOrDefault(firstNonBlank(
                property("sepay.accountNumber"),
                systemProperty("sepay.accountNumber"),
                environment("SEPAY_ACCOUNT_NUMBER")
        ), DEFAULT_ACCOUNT_NUMBER);
    }

    public static String getAccountName() {
        return valueOrDefault(firstNonBlank(
                property("sepay.accountName"),
                systemProperty("sepay.accountName"),
                environment("SEPAY_ACCOUNT_NAME")
        ), DEFAULT_ACCOUNT_NAME);
    }

    public static String getPaymentNote() {
        return valueOrDefault(firstNonBlank(
                property("sepay.paymentNote"),
                systemProperty("sepay.paymentNote"),
                environment("SEPAY_PAYMENT_NOTE")
        ), DEFAULT_PAYMENT_NOTE);
    }

    public static String getWebhookApiKey() {
        return trimToNull(firstNonBlank(
                property("sepay.apiKey"),
                systemProperty("sepay.apiKey"),
                environment("SEPAY_API_KEY"),
                environment("SEPAY_WEBHOOK_API_KEY")
        ));
    }

    public static String getWebhookSecretKey() {
        return trimToNull(firstNonBlank(
                property("sepay.webhookSecret"),
                systemProperty("sepay.webhookSecret"),
                environment("SEPAY_WEBHOOK_SECRET"),
                environment("SEPAY_IPN_SECRET")
        ));
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = SePayConfig.class.getClassLoader().getResourceAsStream("sepay.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // Fallback to system properties / environment variables.
        }
        return properties;
    }

    private static String property(String name) {
        return PROPERTIES.getProperty(name);
    }

    private static String systemProperty(String name) {
        return System.getProperty(name);
    }

    private static String environment(String name) {
        return System.getenv(name);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (trimToNull(value) != null) {
                return value;
            }
        }

        return null;
    }

    private static String valueOrDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed != null ? trimmed : defaultValue;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
