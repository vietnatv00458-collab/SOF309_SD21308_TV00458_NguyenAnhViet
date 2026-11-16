package utils;

import java.io.File;

public final class UploadPathUtil {

    private static final String UPLOAD_DIR_PROPERTY = "quanlycafe.upload.dir";
    private static final String BANNER_UPLOAD_DIR_PROPERTY = "quanlycafe.banner.upload.dir";

    private UploadPathUtil() {
    }

    public static File productUploadDir() {
        return resolveUploadDir(UPLOAD_DIR_PROPERTY, "QuanLyCafeUploads/products");
    }

    public static File bannerUploadDir() {
        return resolveUploadDir(BANNER_UPLOAD_DIR_PROPERTY, "QuanLyCafeUploads/banners");
    }

    private static File resolveUploadDir(String propertyName, String fallbackRelativePath) {
        String configuredDir = System.getProperty(propertyName);
        if (configuredDir != null && !configuredDir.trim().isEmpty()) {
            return new File(configuredDir.trim());
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.trim().isEmpty()) {
            return new File(userHome, fallbackRelativePath);
        }

        String userDir = System.getProperty("user.dir");
        if (userDir != null && !userDir.trim().isEmpty()) {
            return new File(userDir, fallbackRelativePath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.trim().isEmpty()) {
            return new File(catalinaBase, fallbackRelativePath);
        }

        return new File(fallbackRelativePath);
    }
}
