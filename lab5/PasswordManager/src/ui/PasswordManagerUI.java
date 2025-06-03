package ui;

import model.PasswordEntry;
import util.EncryptionUtil;
import util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Swing-based UI for the Password Manager (per‐user).
 *
 * - On construction, decrypts data/{username}_passwords.csv.enc → data/{username}_passwords.csv (if exists).
 * - On logout or window close, encrypts plaintext CSV back to .enc and removes plaintext.
 *
 * Supports:
 *  - Add New Entry (Title, Password, URL, Notes)
 *  - Delete Entry
 *  - Search by Title (filters the list)
 *  - Random Password Generator (with copy button)
 *  - Show Password (always visible button)
 *  - Copy to Clipboard (always visible button)
 *  - Logout (and re‐encrypt file)
 */
public class PasswordManagerUI extends JFrame {
    private final String username;
    private final File encryptedFile;
    private final File plainFile;
    private ArrayList<PasswordEntry> entries = new ArrayList<>();

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> passwordList = new JList<>(listModel);

    // Always-visible buttons
    private JButton showBtn   = new JButton("Show Password");
    private JButton copyBtn   = new JButton("Copy to Clipboard");

    public PasswordManagerUI(String username) {
        this.username = username;
        this.encryptedFile = new File("data/" + username + "_passwords.csv.enc");
        this.plainFile     = new File("data/" + username + "_passwords.csv");

        setTitle("Password Manager – User: " + username);
        setSize(800, 550);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top buttons
        JButton addBtn    = new JButton("Add New Entry");
        JButton deleteBtn = new JButton("Delete Entry");
        JButton searchBtn = new JButton("Search by Title");
        JButton randBtn   = new JButton("Generate Random Password");
        JButton logoutBtn = new JButton("Logout");
        JButton updateBtn = new JButton("Update Entry");



        JPanel topPanel = new JPanel();
        topPanel.add(addBtn);
        topPanel.add(deleteBtn);
        topPanel.add(searchBtn);
        topPanel.add(randBtn);
        topPanel.add(updateBtn);
        topPanel.add(logoutBtn);

        updateBtn.addActionListener(e -> updateEntry());

        add(topPanel, BorderLayout.NORTH);

        // Center: scrollable list
        passwordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(passwordList), BorderLayout.CENTER);

        // Bottom panel: always-show "Show" and "Copy" buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(showBtn);
        bottomPanel.add(copyBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Initially disable until an entry is selected
        showBtn.setEnabled(false);
        copyBtn.setEnabled(false);

        // Decrypt user file if it exists
        decryptUserFileIfNeeded();
        // Load CSV into entries
        loadPasswordsFromCsv();
        refreshList();

        // List selection listener: enable/disable show & copy
        passwordList.addListSelectionListener(e -> {
            boolean selected = passwordList.getSelectedIndex() >= 0;
            showBtn.setEnabled(selected);
            copyBtn.setEnabled(selected);
        });

        // Button actions
        addBtn.addActionListener(e -> addEntry());
        deleteBtn.addActionListener(e -> deleteEntry());
        searchBtn.addActionListener(e -> searchEntry());
        randBtn.addActionListener(e -> generateRandom());
        logoutBtn.addActionListener(e -> logout());

        showBtn.addActionListener(e -> showSelectedPassword());
        copyBtn.addActionListener(e -> copySelectedPassword());

        // On window close: trigger logout (which encrypts), then exit
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });
    }

    /** When creating a new user, create an empty encrypted file. */
    public static void initializeEmptyUserFile(String username) {
        try {
            File enc = new File("data/" + username + "_passwords.csv.enc");
            enc.getParentFile().mkdirs();
            // Create empty plaintext CSV and immediately encrypt it:
            File tempCsv = new File("data/" + username + "_passwords.csv");
            try (PrintWriter pw = new PrintWriter(new FileWriter(tempCsv))) {
                // no lines
            }
            // Encrypt
            String plainText = new String(Files.readAllBytes(Paths.get(tempCsv.getPath())), "UTF-8");
            String cipherText = EncryptionUtil.encrypt(plainText);
            try (PrintWriter fos = new PrintWriter(new FileWriter(enc))) {
                fos.print(cipherText);
            }
            tempCsv.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** If encrypted file exists, decrypt it to plaintext; otherwise do nothing. */
    private void decryptUserFileIfNeeded() {
        if (!encryptedFile.exists()) return;
        try {
            String base64Cipher = new String(Files.readAllBytes(Paths.get(encryptedFile.getPath())), "UTF-8");
            String decryptedCsv = EncryptionUtil.decrypt(base64Cipher);
            try (PrintWriter pw = new PrintWriter(new FileWriter(plainFile))) {
                pw.print(decryptedCsv);
            }
            encryptedFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to decrypt your password file.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /** Encrypts plaintext CSV back to .enc and deletes plaintext. */
    private void encryptUserFile() {
        try {
            if (!plainFile.exists()) {
                return;
            }
            String csvContent = new String(Files.readAllBytes(Paths.get(plainFile.getPath())), "UTF-8");
            String cipherText = EncryptionUtil.encrypt(csvContent);
            try (PrintWriter pw = new PrintWriter(new FileWriter(encryptedFile))) {
                pw.print(cipherText);
            }
            plainFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to encrypt your password file.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Updates selected entry with new values, encrypts and saves. */
    private void updateEntry() {
        int idx = passwordList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select an entry to update.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PasswordEntry selected = entries.get(idx);

        JTextField titleField = new JTextField(selected.getTitle());
        JPasswordField pwField = new JPasswordField();  // blank by default
        JTextField urlField = new JTextField(selected.getUrl());
        JTextField notesField = new JTextField(selected.getNotes());

        Object[] inputs = {
                "New Title (leave empty to keep):", titleField,
                "New Password (leave blank to keep):", pwField,
                "New URL:", urlField,
                "New Notes:", notesField
        };

        int result = JOptionPane.showConfirmDialog(
                this, inputs, "Update Password Entry", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newTitle = titleField.getText().trim();
            String newUrl = urlField.getText().trim();
            String newNotes = notesField.getText().trim();
            String newPlainPw = new String(pwField.getPassword());

            if (!newTitle.isEmpty()) {
                selected.setTitle(newTitle);
            }
            selected.setUrl(newUrl);
            selected.setNotes(newNotes);

            if (!newPlainPw.isEmpty()) {
                try {
                    String encrypted = EncryptionUtil.encrypt(newPlainPw);
                    selected.setEncryptedPassword(encrypted);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Failed to encrypt updated password.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            savePasswordsToCsv();
            refreshList();
        }
    }


    /** Adds a new entry: prompts Title/Password/URL/Notes, encrypts password, saves. */
    private void addEntry() {
        JTextField titleField = new JTextField();
        JPasswordField pwField = new JPasswordField();
        JTextField urlField = new JTextField();
        JTextField notesField = new JTextField();

        Object[] inputs = {
                "Title:", titleField,
                "Password:", pwField,
                "URL:", urlField,
                "Notes:", notesField
        };

        int result = JOptionPane.showConfirmDialog(
                this, inputs, "Add New Password Entry", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String url   = urlField.getText().trim();
            String notes = notesField.getText().trim();
            String plainPwd = new String(pwField.getPassword());

            if (title.isEmpty() || plainPwd.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Title and Password cannot be empty.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String encrypted = EncryptionUtil.encrypt(plainPwd);
                PasswordEntry entry = new PasswordEntry(title, encrypted, url, notes);
                entries.add(entry);
                savePasswordsToCsv();
                refreshList();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error encrypting password.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Deletes selected entry, then saves CSV. */
    private void deleteEntry() {
        int idx = passwordList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an entry to delete.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PasswordEntry toDelete = entries.get(idx);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete entry \"" + toDelete.getTitle() + "\"?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            entries.remove(idx);
            savePasswordsToCsv();
            refreshList();
        }
    }

    /**
     * Filters the list to show only entries whose title contains the query.
     */
    private void searchEntry() {
        String query = JOptionPane.showInputDialog(this, "Enter title to search:");
        if (query == null) return;

        listModel.clear();
        for (PasswordEntry e : entries) {
            if (e.getTitle().toLowerCase().contains(query.toLowerCase())) {
                listModel.addElement(e.getTitle() + " | " + e.getUrl() + " | " + e.getNotes());
            }
        }

        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No matching entries found.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshList();
        }
    }

    /** Generates a random password, displays it with a copy button. */
    private void generateRandom() {
        JTextField lengthField = new JTextField();
        Object[] prompt = {"Enter desired length:", lengthField};
        int res = JOptionPane.showConfirmDialog(
                this, prompt, "Generate Random Password", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        int length;
        try {
            length = Integer.parseInt(lengthField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String pw = PasswordUtil.generateRandomPassword(length);
        String strength = PasswordUtil.checkStrength(pw);

        // Show in a dialog with a non-editable textfield and a copy button
        JTextField genField = new JTextField(pw, 20);
        genField.setEditable(false);
        JButton copyGenBtn = new JButton("Copy");

        copyGenBtn.addActionListener(e -> {
            StringSelection selection = new StringSelection(genField.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            JOptionPane.showMessageDialog(this, "Password copied to clipboard.", "Copied",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Generated:"));
        panel.add(genField);
        panel.add(copyGenBtn);
        panel.add(new JLabel("Strength: " + strength));

        JOptionPane.showMessageDialog(this, panel, "Random Password", JOptionPane.PLAIN_MESSAGE);
    }

    /** Always-enabled: decrypts and shows the selected entry’s password. */
    private void showSelectedPassword() {
        int idx = passwordList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select an entry first.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PasswordEntry pe = entries.get(idx);
        try {
            String decrypted = EncryptionUtil.decrypt(pe.getEncryptedPassword());
            JOptionPane.showMessageDialog(
                    this,
                    "Title: " + pe.getTitle() + "\n" +
                            "URL: " + pe.getUrl() + "\n" +
                            "Notes: " + pe.getNotes() + "\n\n" +
                            "Password: " + decrypted,
                    "Password Details",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error decrypting password.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Always-enabled: decrypts selected entry’s password and copies it to clipboard. */
    private void copySelectedPassword() {
        int idx = passwordList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select an entry first.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PasswordEntry pe = entries.get(idx);
        try {
            String decrypted = EncryptionUtil.decrypt(pe.getEncryptedPassword());
            StringSelection selection = new StringSelection(decrypted);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            JOptionPane.showMessageDialog(this,
                    "Password copied to clipboard.", "Copied",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error decrypting password.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Refreshes the JList to show all entries as “Title | URL | Notes”. */
    private void refreshList() {
        listModel.clear();
        for (PasswordEntry e : entries) {
            listModel.addElement(e.getTitle() + " | " + e.getUrl() + " | " + e.getNotes());
        }
    }

    /**
     * Reads data/{username}_passwords.csv (if exists), parses:
     *     Title,EncryptedPassword,URL,Notes
     * and rebuilds entries list.
     */
    private void loadPasswordsFromCsv() {
        if (!plainFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(plainFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                if (parts.length < 4) continue;
                String title = unescapeCsv(parts[0]);
                String encPw  = unescapeCsv(parts[1]);
                String url    = unescapeCsv(parts[2]);
                String notes  = unescapeCsv(parts[3]);
                entries.add(new PasswordEntry(title, encPw, url, notes));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load your passwords.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Writes all entries to data/{username}_passwords.csv, one line per entry:
     *    Title,EncryptedPassword,URL,Notes
     */
    private void savePasswordsToCsv() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(plainFile))) {
            for (PasswordEntry e : entries) {
                pw.println(
                        escapeCsv(e.getTitle())   + "," +
                                escapeCsv(e.getEncryptedPassword()) + "," +
                                escapeCsv(e.getUrl())     + "," +
                                escapeCsv(e.getNotes())
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to save your passwords.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Splits a CSV line into fields, handling quoted commas.
     */
    private String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    /**
     * If a field contains comma, quote, or newline, wrap in double quotes and escape internal quotes.
     */
    private String escapeCsv(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            String escaped = field.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return field;
    }

    /**
     * Remove wrapping quotes and un-escape double quotes.
     */
    private String unescapeCsv(String field) {
        if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
            String inner = field.substring(1, field.length() - 1);
            return inner.replace("\"\"", "\"");
        }
        return field;
    }

    /** Logs out the user: encrypts plaintext CSV back to .enc, deletes plaintext, then returns to login. */
    private void logout() {
        encryptUserFile();
        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginUI loginUI = new LoginUI();
            loginUI.setVisible(true);
        });
    }
}
