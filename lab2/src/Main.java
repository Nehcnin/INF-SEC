import java.util.Scanner;

public class Main {


    public static void Encrypt(String plaintext, String secretKey, String mode) throws Exception {
        String filename="encrypted.txt";
        String encrypted = AES.encrypt(plaintext,secretKey,mode);
        System.out.println("Encrypted text: " + encrypted);

        AES.saveToFile(filename, encrypted);
        System.out.println("Saved to file "+filename);
    }

    public static void Decrypt(String secretKey, String mode) throws Exception {
        String filename="encrypted.txt";
        String encrypted = AES.readFromFile(filename);
        String decrypted = AES.decrypt(encrypted, secretKey, mode);
        System.out.println("Decrypted text: " + decrypted);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        boolean run = true;
        while(run) {
            try {
                System.out.println("Enter E for ENCRYPTING, D for DECRYPTING");
                String choice = in.nextLine().toUpperCase();
                System.out.println();

                System.out.println("Enter the 16 bits long secret key:");
                String secretKey = in.nextLine();
                System.out.println();

                System.out.println("Choose method:");
                System.out.println("Enter 1 for ECB");
                System.out.println("Enter 2 for CBC");
                System.out.println("Enter 3 for CFB");
                String mode = in.nextLine();
                System.out.println();



                if(choice.equals("E")){

                    System.out.println("Enter the plain text:");
                    String plaintext = in.nextLine();
                    System.out.println();

                    Encrypt(plaintext,secretKey,mode);
                }else if(choice.equals("D")){
                    Decrypt(secretKey,mode);
                }
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println("Enter E to EXIT the application, R to RESTART it");
                String str = in.nextLine().toUpperCase();
                if(str.equals("E")){
                    run=false;
                }

            } catch (Exception e) {
                System.out.println("Error "+e.getMessage());
            }
        }
        in.close();
    }
}