import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Waiting for message...");

        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message = in.readLine();
        String signature = in.readLine();
        String publicKeyStr = in.readLine();
        socket.close();

        System.out.println("Message received: " + message);
        System.out.println("Signature: " + signature);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Do you want to modify the signature? (yes/no): ");
        String response = reader.readLine();
        if (response.equalsIgnoreCase("yes")) {
            System.out.print("Enter the new signaature: ");
            signature = reader.readLine();
        }

        // Sending the message
        Socket socket2 = new Socket("localhost", 1235);
        PrintWriter out = new PrintWriter(socket2.getOutputStream(), true);
        out.println(message);
        out.println(signature);
        out.println(publicKeyStr);
        socket2.close();
        serverSocket.close();
    }
}
