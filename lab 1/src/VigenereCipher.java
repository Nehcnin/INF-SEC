public class VigenereCipher {

    private String key;
    private int mode;
    private String alphabet;

    public VigenereCipher(String key){
        this.key=key;
        mode=1;
    }

    public VigenereCipher(String key,String alphabet){
        this.key=key;
        mode=2;
        this.alphabet = alphabet;
    }

    public String Encrypt(String text) {
        char[] characters = new char[text.length()];
        int ind = 0;
        for (int i = 0; i < text.length(); i++) {
            if(mode==1)
                characters[i] = EncryptChar(text.charAt(i), key.charAt(ind));
            else
                characters[i] = EncryptChar2(text.charAt(i), key.charAt(ind));
            ind++;
            if (ind >= key.length()) {
                ind = 0;
            }
        }
        return new String(characters);
    }

    public char EncryptChar(char a, char b){
        if(a<32||a>126){
            throw new RuntimeException();
        }
        //System.out.println(a + " "+b);
        //System.out.println((int)a + " "+(int)b);
        return (char)(((int)a+(int)b-64)%96+32);
    }

    public char EncryptChar2(char a, char b){
        return alphabet.charAt((alphabet.indexOf(a)+alphabet.indexOf(b))%alphabet.length());
    }

    public String Decrypt(String text) {
        char[] characters = new char[text.length()];
        int ind = 0;
        for (int i = 0; i < text.length(); i++) {
            if(mode==1)
                characters[i] = DecryptChar(text.charAt(i), key.charAt(ind));
            else
                characters[i] = DecryptChar2(text.charAt(i), key.charAt(ind));
            ind++;
            if (ind >= key.length()) {
                ind = 0;
            }
        }
        return new String(characters);
    }

    public char DecryptChar(char a, char b){
        if(a<32||a>126){
            throw new RuntimeException();
        }
        return (char)((a-b+96)%96+32);
    }

    public char DecryptChar2(char a, char b){
        return alphabet.charAt((alphabet.indexOf(a)-alphabet.indexOf(b)+alphabet.length())%alphabet.length());
    }
}
