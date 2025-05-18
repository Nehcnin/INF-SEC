import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(1235);
        System.out.println("Waiting for the message...");

        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message = in.readLine();
        String signature = in.readLine();
        String publicKeyStr = in.readLine();
        socket.close();

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        // Checking the signature
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(message.getBytes());
        boolean isValid = sig.verify(Base64.getDecoder().decode(signature));

        System.out.println("Message: " + message);
        System.out.println("Signature: " + signature);
        System.out.println("Is valid: " + (isValid ? "YES" : "NO"));
        serverSocket.close();
    }
}
