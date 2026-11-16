package utils;

public final class AppConfig {

    private static final String LOCAL_JDBC_URL =
            "jdbc:sqlserver://DESKTOP-KE0S1RI;instanceName=SA;databaseName=QuanLyCafe;encrypt=true;trustServerCertificate=true;loginTimeout=30";
    private static final String LOCAL_JDBC_USER = "sa";
    private static final String LOCAL_JDBC_PASSWORD = "chu cuong";

    private AppConfig() {
    }

    public static String getApplicationBaseUrl() {
        return firstNonBlank(
                System.getProperty("app.base.url"),
                System.getenv("APP_BASE_URL")
        );
    }

    public static String getJdbcUrl() {
        String explicitUrl = firstNonBlank(
                System.getProperty("db.jdbc.url"),
                System.getenv("DB_JDBC_URL")
        );
        if (explicitUrl != null) {
            return explicitUrl;
        }

        String host = firstNonBlank(
                System.getProperty("db.host"),
                System.getenv("DB_HOST")
        );
        if (host == null) {
            return LOCAL_JDBC_URL;
        }

        String port = firstNonBlank(
                System.getProperty("db.port"),
                System.getenv("DB_PORT"),
                "1433"
        );
        String databaseName = firstNonBlank(
                System.getProperty("db.name"),
                System.getenv("DB_NAME"),
                "QuanLyCafe"
        );
        String instanceName = firstNonBlank(
                System.getProperty("db.instanceName"),
                System.getenv("DB_INSTANCE_NAME")
        );
        boolean encrypt = Boolean.parseBoolean(firstNonBlank(
                System.getProperty("db.encrypt"),
                System.getenv("DB_ENCRYPT"),
                "true"
        ));
        boolean trustServerCertificate = Boolean.parseBoolean(firstNonBlank(
                System.getProperty("db.trustServerCertificate"),
                System.getenv("DB_TRUST_SERVER_CERTIFICATE"),
                "true"
        ));
        String loginTimeout = firstNonBlank(
                System.getProperty("db.loginTimeout"),
                System.getenv("DB_LOGIN_TIMEOUT"),
                "30"
        );

        StringBuilder url = new StringBuilder("jdbc:sqlserver://")
                .append(host);

        if (port != null && !port.trim().isEmpty()) {
            url.append(':').append(port.trim());
        }

        if (instanceName != null && !instanceName.trim().isEmpty()) {
            url.append(";instanceName=").append(instanceName.trim());
        }

        url.append(";databaseName=").append(databaseName.trim());
        url.append(";encrypt=").append(encrypt);
        url.append(";trustServerCertificate=").append(trustServerCertificate);
        url.append(";loginTimeout=").append(loginTimeout.trim());
        return url.toString();
    }

    public static String getJdbcUser() {
        return firstNonBlank(
                System.getProperty("db.username"),
                System.getenv("DB_USERNAME"),
                LOCAL_JDBC_USER
        );
    }

    public static String getJdbcPassword() {
        return firstNonBlank(
                System.getProperty("db.password"),
                System.getenv("DB_PASSWORD"),
                LOCAL_JDBC_PASSWORD
        );
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
}
