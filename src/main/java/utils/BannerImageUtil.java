package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class BannerImageUtil {

    private static final List<String> DEFAULT_BANNER_IMAGES = Arrays.asList(
            "/images/promo-voucher-1.png",
            "/images/promo-voucher-3.png"
    );

    private BannerImageUtil() {
    }

    public static List<String> loadPromoBannerImages() {
        List<String> bannerImages = new ArrayList<>();
        File uploadDir = UploadPathUtil.bannerUploadDir();
        File[] files = uploadDir != null && uploadDir.isDirectory()
                ? uploadDir.listFiles(file -> file != null && file.isFile() && isSupportedImageName(file.getName()))
                : null;

        if (files != null && files.length > 0) {
            Arrays.sort(files, Comparator
                    .comparingLong(File::lastModified)
                    .reversed()
                    .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));

            for (File file : files) {
                bannerImages.add("/promo-banners/" + file.getName() + "?v=" + file.lastModified());
            }
        }

        if (bannerImages.isEmpty()) {
            bannerImages.addAll(DEFAULT_BANNER_IMAGES);
        }

        return bannerImages;
    }

    public static List<String> buildPromoBannerCarouselImages(List<String> bannerImages) {
        List<String> carouselImages = new ArrayList<>();
        if (bannerImages != null) {
            carouselImages.addAll(bannerImages);
        }

        if (carouselImages.size() == 2) {
            carouselImages.add(carouselImages.get(1));
            carouselImages.add(carouselImages.get(0));
        }

        return carouselImages;
    }

    public static boolean isSupportedImageName(String fileName) {
        if (fileName == null) {
            return false;
        }

        String normalized = fileName.trim().toLowerCase(Locale.ROOT);
        return normalized.endsWith(".png")
                || normalized.endsWith(".jpg")
                || normalized.endsWith(".jpeg")
                || normalized.endsWith(".webp")
                || normalized.endsWith(".gif");
    }
}
