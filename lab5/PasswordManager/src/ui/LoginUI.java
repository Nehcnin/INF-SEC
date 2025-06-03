package ui;

import util.UserAuthUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A simple Swing login/registration window.
 *  - On “Register,” prompts for new username/password; creates a new user in data/users.csv.
 *  - On “Login,” verifies credentials; if successful, opens PasswordManagerUI(username).
 */
public class LoginUI extends JFrame {
    private JTextField usernameField = new JTextField(15);
    private JPasswordField passwordField = new JPasswordField(15);

    public LoginUI() {
        setTitle("Password Manager - Login/Register");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        panel.add(loginBtn);
        panel.add(registerBtn);

        add(panel);

        // Actions
        loginBtn.addActionListener(e -> attemptLogin());
        registerBtn.addActionListener(e -> attemptRegister());

        // If window closed, exit
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void attemptLogin() {
        String user = usernameField.getText().trim();
        char[] pw = passwordField.getPassword();
        if (user.isEmpty() || pw.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Username and Password are required.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (UserAuthUtil.authenticate(user, pw)) {
                // Open main UI
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    PasswordManagerUI pmui = new PasswordManagerUI(user);
                    pmui.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials.", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "An error occurred during login.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptRegister() {
        String user = usernameField.getText().trim();
        char[] pw = passwordField.getPassword();
        if (user.isEmpty() || pw.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Username and Password are required to register.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean success = UserAuthUtil.registerUser(user, pw);
            if (success) {
                // Also create an empty encrypted file for this new user
                PasswordManagerUI.initializeEmptyUserFile(user);
                JOptionPane.showMessageDialog(this,
                        "Registration successful. You may now login.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Username already exists. Choose another.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "An error occurred during registration.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
