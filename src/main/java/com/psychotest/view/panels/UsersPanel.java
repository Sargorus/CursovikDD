package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.AdminController;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.util.PasswordUtil;
import main.java.com.psychotest.view.dialogs.UserDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {
    private AdminController controller;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public UsersPanel(AdminController controller) {
        this.controller = controller;
        initComponents();
        loadUsers();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Поиск:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchUsers();
            }
        });
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Таблица пользователей
        String[] columns = {"ID", "Логин", "ФИО", "Роль", "Дата создания"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.getTableHeader().setReorderingAllowed(false);

        usersTable.getColumnModel().getColumn(0).setMaxWidth(50);
        usersTable.getColumnModel().getColumn(3).setMaxWidth(120);
        usersTable.getColumnModel().getColumn(4).setMaxWidth(150);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton("➕ Добавить");
        addButton.addActionListener(e -> addUser());

        JButton editButton = new JButton("✏️ Редактировать");
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editUser());

        JButton deleteButton = new JButton("🗑️ Удалить");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteUser());

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> loadUsers());

        usersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = usersTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = controller.getAllUsers();
        for (User user : users) {
            addUserToTable(user);
        }
    }

    private void searchUsers() {
        String searchText = searchField.getText();
        tableModel.setRowCount(0);
        List<User> users = controller.searchUsers(searchText);
        for (User user : users) {
            addUserToTable(user);
        }
    }

    private void addUserToTable(User user) {
        Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                getRoleName(user.getRole()),
                user.getCreatedAt() != null ? user.getCreatedAt().toString().substring(0, 10) : ""
        };
        tableModel.addRow(row);
    }

    private String getRoleName(String role) {
        switch (role) {
            case "ADMIN": return "Администратор";
            case "TEACHER": return "Преподаватель";
            case "TAKER": return "Тестируемый";
            default: return role;
        }
    }

    private void addUser() {
        UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            User user = dialog.getUser();
            String hashedPassword = PasswordUtil.hashPassword(user.getPasswordHash());
            if (controller.addUser(user.getUsername(), hashedPassword, user.getFullName(), user.getRole())) {
                JOptionPane.showMessageDialog(this, "Пользователь успешно добавлен!");
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении пользователя!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя для редактирования!");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        User user = controller.getUserById(userId);

        if (user != null) {
            UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), user);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                User updatedUser = dialog.getUser();

                if (controller.updateUser(userId, updatedUser.getFullName(), updatedUser.getRole())) {
                    // Если пароль был изменён
                    if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isEmpty()) {
                        String newHashedPassword = PasswordUtil.hashPassword(updatedUser.getPasswordHash());
                        controller.updateUserPassword(userId, newHashedPassword);
                    }
                    JOptionPane.showMessageDialog(this, "Пользователь успешно обновлён!");
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении пользователя!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя для удаления!");
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить пользователя '" + username + "'?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            if (controller.deleteUser(userId)) {
                JOptionPane.showMessageDialog(this, "Пользователь успешно удалён!");
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении пользователя!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}