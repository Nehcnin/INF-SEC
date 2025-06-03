package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to manage user registration, login, and password hashing via PBKDF2.
 * Stores user credentials in data/users.csv as:
 *    username,base64Salt,base64HashedPassword
 */
public class UserAuthUtil {
    private static final String USER_DB_PATH = "data/users.csv";
    private static final int SALT_LENGTH = 16;       // bytes
    private static final int ITERATIONS  = 65536;
    private static final int KEY_LENGTH  = 256;      // bits

    /**
     * Registers a new username with the given plaintext password.
     * Returns true if registration succeeded, false if username already exists.
     */
    public static boolean registerUser(String username, char[] password) throws Exception {
        Map<String, String[]> users = loadAllUsers();
        if (users.containsKey(username)) {
            return false;  // already exists
        }

        // Generate random salt
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(salt);

        // Hash password
        byte[] hashed = pbkdf2Hash(password, salt);

        // Store: username, base64(salt), base64(hashed)
        String record = username + "," +
                Base64.getEncoder().encodeToString(salt) + "," +
                Base64.getEncoder().encodeToString(hashed);

        // Ensure data directory exists
        File parent = new File(USER_DB_PATH).getParentFile();
        if (parent != null) parent.mkdirs();

        try (FileWriter fw = new FileWriter(USER_DB_PATH, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(record);
        }
        return true;
    }

    /**
     * Attempts login: returns true if username exists and password matches.
     */
    public static boolean authenticate(String username, char[] password) throws Exception {
        Map<String, String[]> users = loadAllUsers();
        if (!users.containsKey(username)) return false;

        String[] saltAndHash = users.get(username);
        byte[] salt   = Base64.getDecoder().decode(saltAndHash[0]);
        byte[] target = Base64.getDecoder().decode(saltAndHash[1]);

        byte[] hashed = pbkdf2Hash(password, salt);
        return constantTimeArrayEquals(hashed, target);
    }

    /**
     * Loads all users from data/users.csv into a map:
     *    username → [ base64Salt, base64Hash ]
     */
    private static Map<String, String[]> loadAllUsers() throws IOException {
        Map<String, String[]> map = new HashMap<>();
        File f = new File(USER_DB_PATH);
        if (!f.exists()) return map;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    map.put(parts[0], new String[]{parts[1], parts[2]});
                }
            }
        }
        return map;
    }

    /**
     * PBKDF2 with HmacSHA256.
     */
    private static byte[] pbkdf2Hash(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Compares two byte[] in constant time to prevent timing attacks.
     */
    private static boolean constantTimeArrayEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
