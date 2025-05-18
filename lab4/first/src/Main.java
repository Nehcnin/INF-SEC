import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws Exception {
        // generating rsa key pairRSA
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Requesting message
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter the message: ");
        String message = reader.readLine();

        // Creating digital signature
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(message.getBytes());
        byte[] signatureBytes = sign.sign();
        String signature = Base64.getEncoder().encodeToString(signatureBytes);

        // Public key base64
        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        System.out.println("Signed and ready for sending.");

        // Sending
        Socket socket = new Socket("localhost", 1234);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(message);
        out.println(signature);
        out.println(publicKeyStr);
        socket.close();
    }
}
