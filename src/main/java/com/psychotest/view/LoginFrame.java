package main.java.com.psychotest.view;

import main.java.com.psychotest.model.User;
import main.java.com.psychotest.service.AuthService;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private AuthService authService;

    public LoginFrame() {
        authService = new AuthService();
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        setTitle("PsychoTest - Вход в систему");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Заголовок
        JLabel titleLabel = new JLabel("Психологическое тестирование");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // Логин
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Логин:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Пароль
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Пароль:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Кнопка входа
        JButton loginButton = new JButton("Войти");
        loginButton.setBackground(new Color(70, 130, 200));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> performLogin());

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(loginButton, gbc);

        // Кнопка выхода
        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> System.exit(0));
        gbc.gridy = 4;
        add(exitButton, gbc);

        // Добавляем обработку Enter
        getRootPane().setDefaultButton(loginButton);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Пожалуйста, введите логин и пароль",
                    "Ошибка входа",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = authService.authenticate(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this,
                    "Добро пожаловать, " + user.getFullName() + "!",
                    "Успешный вход",
                    JOptionPane.INFORMATION_MESSAGE);
            this.dispose();

            // Пока просто показываем сообщение о роли
            JOptionPane.showMessageDialog(null,
                    "Ваша роль: " + user.getRole() + "\nДальнейшая функциональность будет добавлена",
                    "Информация",
                    JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Неверный логин или пароль",
                    "Ошибка входа",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}