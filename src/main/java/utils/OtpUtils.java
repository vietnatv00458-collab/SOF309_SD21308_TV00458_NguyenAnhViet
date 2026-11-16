package utils;

import java.security.SecureRandom;

public final class OtpUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpUtils() {
    }

    public static String generateSixDigitCode() {
        int code = RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
