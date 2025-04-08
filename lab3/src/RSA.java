import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class RSA {

    public static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++)
            if (n % i == 0) return false;
        return true;
    }

    //greatest common divisor
    public static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    // Extended Euclidean Algorithm to find modular inverse
    public static int modInverse(int e, int phi) {
        int t = 0, newT = 1;
        int r = phi, newR = e;
        while (newR != 0) {
            int quotient = r / newR;
            int temp = t;
            t = newT;
            newT = temp - quotient * newT;
            temp = r;
            r = newR;
            newR = temp - quotient * newR;
        }
        if (r > 1) throw new ArithmeticException("No modular inverse found.");
        if (t < 0) t += phi;
        return t;
    }

    // Factorize n to retrieve p and q
    public static int[] factorizeN(int n) {
        for (int i = 2; i <= 1000; i++) {
            if (n % i == 0 && isPrime(i)) {
                int q = n / i;
                if (isPrime(q)) return new int[]{i, q};
            }
        }
        throw new IllegalArgumentException("No prime factors found for n = " + n);
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter 1 for decryption, and 2 for Encryption");
        int t = sc.nextInt();
        if (t == 2) {

            // Input primes p and q
            System.out.print("Enter prime p (≤ 1000): ");
            int p = sc.nextInt();
            System.out.print("Enter prime q (≤ 1000): ");
            int q = sc.nextInt();
            sc.nextLine();

            if (!isPrime(p) || !isPrime(q)) {
                System.out.println("Invalid input: both p and q must be primes ≤ 1000.");
                return;
            }

            int n = p * q;
            int phi = (p - 1) * (q - 1);

            // Choose public exponent
            int e = 3;
            while (gcd(e, phi) != 1) e++;

            // Compute private key
            int d = modInverse(e, phi);

            // Input plaintext message
            System.out.print("Enter text to encrypt: ");
            String message = sc.nextLine();

            // Encrypt each character
            List<BigInteger> encryptedList = new ArrayList<>();
            for (char c : message.toCharArray()) {
                BigInteger m = BigInteger.valueOf((int) c);
                BigInteger cipher = m.modPow(BigInteger.valueOf(e), BigInteger.valueOf(n));
                encryptedList.add(cipher);
            }

            // Save encrypted data and public key to file
            FileWriter writer = new FileWriter("rsa.txt");
            for (BigInteger enc : encryptedList) {
                writer.write(enc.toString() + " ");
            }
            writer.write("\n" + n + " " + e);
            writer.close();

            System.out.println("\nEncrypted text (numbers): " + encryptedList);
            System.out.println("Public key: (n = " + n + ", e = " + e + ")");
            System.out.println("Saved to file: rsa.txt");
        } else {
            //Decryption

            // Read encrypted message and public key from file
            BufferedReader reader = new BufferedReader(new FileReader("rsa.txt"));
            String[] encryptedValues = reader.readLine().trim().split(" ");
            String[] keyParts = reader.readLine().trim().split(" ");
            int n2 = Integer.parseInt(keyParts[0]);
            int e2 = Integer.parseInt(keyParts[1]);
            reader.close();

            // Factor n to retrieve p and q
            int[] primes = factorizeN(n2);
            int p2 = primes[0];
            int q2 = primes[1];

            // Recalculate phi and private key d
            int phi2 = (p2 - 1) * (q2 - 1);
            int d2 = modInverse(e2, phi2);

            // Decrypt each number to get the original characters
            StringBuilder decryptedMessage = new StringBuilder();
            for (String part : encryptedValues) {
                BigInteger cipher = new BigInteger(part);
                BigInteger decryptedChar = cipher.modPow(BigInteger.valueOf(d2), BigInteger.valueOf(n2));
                decryptedMessage.append((char) decryptedChar.intValue());
            }

            System.out.println("\nDecrypted message: " + decryptedMessage.toString());
        }
    }
}
