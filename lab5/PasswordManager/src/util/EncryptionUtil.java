package util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for AES encryption/decryption.
 *  - KEY and IV are both 16 bytes (AES-128).
 *  - In production, the key/IV should be derived from a master password, not hard-coded.
 */
public class EncryptionUtil {
    private static final String AES_KEY = "0123456789abcdef";   // 16 chars => 128-bit
    private static final String AES_IV  = "abcdefghijklmnop";   // 16 chars => 128-bit

    public static String encrypt(String plaintext) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(AES_IV.getBytes("UTF-8"));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String base64Cipher) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(AES_IV.getBytes("UTF-8"));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decoded = Base64.getDecoder().decode(base64Cipher);
        byte[] plain = cipher.doFinal(decoded);
        return new String(plain, "UTF-8");
    }
}
