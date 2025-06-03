import ui.LoginUI;

import javax.swing.SwingUtilities;

/**
 * Entry point: launches the Login UI.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginUI loginUI = new LoginUI();
            loginUI.setVisible(true);
        });
    }
}
