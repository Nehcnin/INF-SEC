import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AES {

    public static String encrypt(String plaintext, String secretKey, String mode) throws Exception {
        mode = getMode(mode);

        Cipher cipher;
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher = getCipher(secretKey, mode, Cipher.ENCRYPT_MODE, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        if (!mode.equals("ECB")) {
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            return Base64.getEncoder().encodeToString(combined);
        }
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText, String secretKey, String mode) throws Exception {
        mode = getMode(mode);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] iv = new byte[16];
        byte[] encryptedBytes;

        if (!mode.equals("ECB")) {
            System.arraycopy(decodedBytes, 0, iv, 0, iv.length);
            encryptedBytes = new byte[decodedBytes.length - iv.length];
            System.arraycopy(decodedBytes, iv.length, encryptedBytes, 0, encryptedBytes.length);
        } else {
            encryptedBytes = decodedBytes;
        }

        Cipher cipher = getCipher(secretKey, mode, Cipher.DECRYPT_MODE, new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static Cipher getCipher(String secretKey, String mode, int cipherMode, IvParameterSpec ivSpec) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/" + mode + "/PKCS5Padding");

        if (mode.equals("ECB")) {
            cipher.init(cipherMode, keySpec);
        } else {
            cipher.init(cipherMode, keySpec, ivSpec);
        }
        return cipher;
    }

    private static String getMode(String mode) {
        switch (mode) {
            case "1": return "ECB";
            case "2": return "CBC";
            case "3": return "CFB";
            default: throw new IllegalArgumentException("Wrong mode!");
        }
    }

    public static void saveToFile(String filename, String data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(data);
        }
    }

    public static String readFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return reader.readLine();
        }
    }
}
