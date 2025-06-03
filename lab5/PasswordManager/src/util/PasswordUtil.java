package util;

import java.security.SecureRandom;

/**
 * Utility for random password generation and strength checking.
 */
public class PasswordUtil {
    private static final String CHAR_POOL =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "0123456789" +
                    "@#$%!&*";

    /**
     * Generates a random password of given length using CHAR_POOL.
     */
    public static String generateRandomPassword(int length) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = rnd.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * Basic strength checker:
     *  - +1 point for length ≥ 8
     *  - +1 point for lowercase
     *  - +1 point for uppercase
     *  - +1 point for digit
     *  - +1 point for symbol (from @#$%!&*)
     */
    public static String checkStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[@#$%!&*].*")) score++;

        return switch (score) {
            case 5 -> "Very Strong";
            case 4 -> "Strong";
            case 3 -> "Medium";
            case 2 -> "Weak";
            default -> "Very Weak";
        };
    }
}
