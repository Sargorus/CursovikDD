package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.model.User;
import javax.swing.*;
import java.awt.*;

public class UserDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JComboBox<String> roleCombo;
    private boolean confirmed = false;
    private User user;

    public UserDialog(Window parent, User editUser) {
        super(parent, editUser == null ? "Добавление пользователя" : "Редактирование пользователя",
                ModalityType.APPLICATION_MODAL);
        this.user = editUser;
        initComponents();

        if (editUser != null) {
            loadUserData();
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Логин
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Логин:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        add(usernameField, gbc);

        // Пароль
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Пароль:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        if (user != null) {
            passwordField.setToolTipText("Оставьте пустым, чтобы не менять пароль");
        }

        // ФИО
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("ФИО:"), gbc);
        gbc.gridx = 1;
        fullNameField = new JTextField(15);
        add(fullNameField, gbc);

        // Роль
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Роль:"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"TAKER", "TEACHER", "ADMIN"});
        roleCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    switch (value.toString()) {
                        case "ADMIN": setText("Администратор"); break;
                        case "TEACHER": setText("Преподаватель"); break;
                        case "TAKER": setText("Тестируемый"); break;
                    }
                }
                return this;
            }
        });
        add(roleCombo, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> ok());
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> cancel());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }

    private void loadUserData() {
        usernameField.setText(user.getUsername());
        usernameField.setEnabled(false);
        fullNameField.setText(user.getFullName());
        roleCombo.setSelectedItem(user.getRole());
    }

    private void ok() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите логин!");
            return;
        }

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ФИО!");
            return;
        }

        if (user == null && password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите пароль!");
            return;
        }

        if (user == null) {
            user = new User();
        }

        user.setUsername(username);
        if (!password.isEmpty()) {
            user.setPasswordHash(password); // Будет захеширован в UsersPanel
        }
        user.setFullName(fullName);
        user.setRole(role);

        confirmed = true;
        dispose();
    }

    private void cancel() {
        confirmed = false;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public User getUser() {
        return user;
    }
}
