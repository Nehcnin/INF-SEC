public class MainController implements Runnable {
    private MainFrame mainFrame;
    public MainController(MainFrame mainFrame){
        this.mainFrame=mainFrame;
    }

    @Override
    public void run() {
        while(true){
            if(mainFrame.getLastPressed()==1){
                mainFrame.Encrypt();
            }

            if(mainFrame.getLastPressed()==2){
                mainFrame.Decrypt();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
