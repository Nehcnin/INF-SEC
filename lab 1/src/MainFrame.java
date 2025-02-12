import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private JTextArea jTextArea1;
    private JTextArea jTextArea2;
    private JButton jButton1;
    private JButton jButton2;
    private VigenereCipher vigenereCipher;
    private int lastPressed;
    private String lastText;
    private int lastAction;
    public MainFrame(VigenereCipher vigenereCipher){
        setLayout(new GridLayout(2,2));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(200,200,1000,600);

        this.vigenereCipher=vigenereCipher;
        lastPressed =0;
        lastAction=0;

        jTextArea1 = new JTextArea();
        jTextArea2 = new JTextArea();
        jTextArea2.setEditable(false);
        jTextArea1.setBorder(new LineBorder(Color.BLACK));
        jTextArea2.setBorder(new LineBorder(Color.BLACK));

        jButton1 = new JButton("ENCRYPT");
        jButton2 = new JButton("DECRYPT");

        add(jTextArea1);
        add(jTextArea2);
        add(jButton1);
        add(jButton2);

        setVisible(true);

        jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //String encrypted = vigenereCipher.Encrypt(jTextArea1.getText());
                //jTextArea2.setText(encrypted);
                lastPressed=1;
            }
        });

        jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //String decrypted = vigenereCipher.Decrypt(jTextArea1.getText());
                //jTextArea2.setText(decrypted);
                lastPressed=2;
            }
        });
    }

    public int getLastPressed() {
        return lastPressed;
    }

    public void Encrypt(){
        if(!jTextArea1.getText().equals(lastText) || lastAction!=1) {
            String encrypted = vigenereCipher.Encrypt(jTextArea1.getText());
            jTextArea2.setText(encrypted);
            lastText = jTextArea1.getText();
            lastAction=1;
        }
    }

    public void Decrypt(){
        if(!jTextArea1.getText().equals(lastText) || lastAction!=2) {
            String decrypted = vigenereCipher.Decrypt(jTextArea1.getText());
            jTextArea2.setText(decrypted);
            lastText = jTextArea1.getText();
            lastAction=2;
        }
    }
}
