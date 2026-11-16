package utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    public static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("cafePU", buildPersistenceProperties());

    public static void close() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }

    private static Map<String, Object> buildPersistenceProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        properties.put("jakarta.persistence.jdbc.url", AppConfig.getJdbcUrl());
        properties.put("jakarta.persistence.jdbc.user", AppConfig.getJdbcUser());
        properties.put("jakarta.persistence.jdbc.password", AppConfig.getJdbcPassword());
        return properties;
    }
}
