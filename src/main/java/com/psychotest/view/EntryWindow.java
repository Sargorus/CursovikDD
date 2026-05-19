package main.java.com.psychotest.view;
import java.awt.*;
import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class EntryWindow extends JFrame {
    private JTextField newLogin;
    private JPasswordField newPassword;
    private JButton entryButton;
    private String loginHint = "Введите логин";
    private String passwordHint = "Введите пароль";

    public EntryWindow() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        setTitle("PsychoTest - Вход в систему");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        newLogin = new JTextField(15);
        newPassword = new JPasswordField(15);
        entryButton = new JButton("Войти");

        // Добавляем подсказки (placeholders)
        newLogin.setText(loginHint);
        newLogin.setForeground(Color.GRAY);
        newLogin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (newLogin.getText().equals(loginHint)) {
                    newLogin.setText("");
                    newLogin.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (newLogin.getText().isEmpty()) {
                    newLogin.setText(loginHint);
                    newLogin.setForeground(Color.GRAY);
                }
            }
        });

        newPassword.setText(passwordHint);
        newPassword.setForeground(Color.GRAY);
        newPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (new String(newPassword.getPassword()).equals(passwordHint)) {
                    newPassword.setText("");
                    newPassword.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (newPassword.getPassword().length == 0) {
                    newPassword.setText(passwordHint);
                    newPassword.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Психологическое тестирование");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Логин:"), gbc);
        gbc.gridx = 1;
        add(newLogin, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Пароль:"), gbc);
        gbc.gridx = 1;
        add(newPassword, gbc);

        entryButton.setBackground(new Color(70, 130, 200));
        entryButton.setForeground(Color.WHITE);
        entryButton.setFocusPainted(false);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(entryButton, gbc);

        getRootPane().setDefaultButton(entryButton);
    }

    // Геттеры
    public JTextField getNewLogin() { return newLogin; }
    public JPasswordField getNewPassword() { return newPassword; }
    public JButton getEntryButton() { return entryButton; }
    public String getLoginHint() { return loginHint; }
    public String getPasswordHint() { return passwordHint; }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearFields() {
        newLogin.setText(loginHint);
        newLogin.setForeground(Color.GRAY);
        newPassword.setText(passwordHint);
        newPassword.setForeground(Color.GRAY);
    }

    public void close() {
        this.dispose();
    }
}
