package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class EmailConfig {

    private static final Properties PROPERTIES = loadProperties();

    private EmailConfig() {
    }

    public static String getSmtpHost() {
        return firstNonBlank(
                property("mail.smtp.host"),
                property("mail.smtpHost"),
                System.getProperty("mail.smtp.host"),
                System.getenv("MAIL_SMTP_HOST"),
                "smtp.gmail.com"
        );
    }

    public static int getSmtpPort() {
        String raw = firstNonBlank(
                property("mail.smtp.port"),
                System.getProperty("mail.smtp.port"),
                System.getenv("MAIL_SMTP_PORT"),
                "587"
        );

        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 587;
        }
    }

    public static boolean isStartTlsEnabled() {
        return Boolean.parseBoolean(firstNonBlank(
                property("mail.smtp.starttls.enable"),
                System.getProperty("mail.smtp.starttls.enable"),
                System.getenv("MAIL_SMTP_STARTTLS"),
                "true"
        ));
    }

    public static boolean isSslEnabled() {
        return Boolean.parseBoolean(firstNonBlank(
                property("mail.smtp.ssl.enable"),
                System.getProperty("mail.smtp.ssl.enable"),
                System.getenv("MAIL_SMTP_SSL"),
                "false"
        ));
    }

    public static String getUsername() {
        return firstNonBlank(
                property("mail.smtp.username"),
                property("mail.username"),
                System.getProperty("mail.smtp.username"),
                System.getenv("MAIL_SMTP_USERNAME")
        );
    }

    public static String getPassword() {
        return stripInternalWhitespace(firstNonBlank(
                property("mail.smtp.password"),
                property("mail.password"),
                System.getProperty("mail.smtp.password"),
                System.getenv("MAIL_SMTP_PASSWORD")
        ));
    }

    public static String getFromAddress() {
        return firstNonBlank(
                property("mail.from.address"),
                System.getProperty("mail.from.address"),
                System.getenv("MAIL_FROM_ADDRESS"),
                getUsername()
        );
    }

    public static String getFromName() {
        return firstNonBlank(
                property("mail.from.name"),
                System.getProperty("mail.from.name"),
                System.getenv("MAIL_FROM_NAME"),
                "Quan Ly Cafe"
        );
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        loadFromExplicitPath(properties, firstNonBlank(
                System.getProperty("mail.config.file"),
                System.getenv("EMAIL_CONFIG_FILE")
        ));
        loadFromFile(properties, new File(System.getProperty("user.dir"), "email.properties"));
        loadFromFile(properties, new File(System.getProperty("user.dir"), "config/email.properties"));

        try (InputStream inputStream = EmailConfig.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // Fallback to system properties / environment variables.
        }

        return properties;
    }

    private static void loadFromExplicitPath(Properties properties, String path) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        loadFromFile(properties, new File(path.trim()));
    }

    private static void loadFromFile(Properties properties, File file) {
        if (file == null || !file.isFile()) {
            return;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException ignored) {
            // Fallback to system properties / environment variables.
        }
    }

    private static String property(String name) {
        return PROPERTIES.getProperty(name);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return null;
    }

    private static String stripInternalWhitespace(String value) {
        if (value == null) {
            return null;
        }

        return value.replaceAll("\\s+", "");
    }
}
