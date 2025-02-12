public class Main {
    public static void main(String[] args) {
        String alphabet= new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz,./;'[]*()");
        VigenereCipher vigenereCipher;
        if(args.length==2){
//first parameter is the key, second parameter is the option, 1 for ascii alphabet, and 2 for custom alphabet
            if(args[1].equals("1"))
             vigenereCipher=new VigenereCipher(args[0]);
            else
                vigenereCipher = new VigenereCipher(args[0],alphabet);

        }
        else{
            throw new RuntimeException();
        }
        MainFrame mainFrame = new MainFrame(vigenereCipher);
        MainController mainController = new MainController(mainFrame);
        Thread thread = new Thread(mainController);
        thread.start();
    }
}