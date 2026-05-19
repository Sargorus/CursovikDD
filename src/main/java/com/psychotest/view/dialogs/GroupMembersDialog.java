package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.dao.GroupDAO;
import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.model.Group;
import main.java.com.psychotest.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GroupMembersDialog extends JDialog {
    private Group group;
    private GroupDAO groupDAO;
    private UserDAO userDAO;
    private JTable membersTable;
    private JTable availableUsersTable;
    private DefaultTableModel membersModel;
    private DefaultTableModel availableModel;
    private List<User> allUsers;
    private boolean confirmed = false;

    public GroupMembersDialog(Window parent, Group group, List<User> allAvailableUsers) {
        super(parent, "Управление участниками группы: " + group.getName(), ModalityType.APPLICATION_MODAL);
        this.group = group;
        this.groupDAO = new GroupDAO();
        this.userDAO = new UserDAO();
        this.allUsers = new ArrayList<>(allAvailableUsers);
        initComponents();
        loadData();
        setSize(800, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Основная панель с двумя таблицами
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Левая панель - участники группы
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Участники группы"));

        String[] memberColumns = {"ID", "ФИО", "Логин", "Роль"};
        membersModel = new DefaultTableModel(memberColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        membersTable = new JTable(membersModel);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane membersScroll = new JScrollPane(membersTable);
        leftPanel.add(membersScroll, BorderLayout.CENTER);

        JButton removeButton = new JButton("➖ Удалить из группы");
        removeButton.addActionListener(e -> removeMember());
        leftPanel.add(removeButton, BorderLayout.SOUTH);

        // Правая панель - доступные пользователи
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Доступные пользователи"));

        String[] userColumns = {"ID", "ФИО", "Логин", "Роль"};
        availableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        availableUsersTable = new JTable(availableModel);
        availableUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane availableScroll = new JScrollPane(availableUsersTable);
        rightPanel.add(availableScroll, BorderLayout.CENTER);

        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Поиск по ФИО или логину");
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchUsers(searchField.getText());
            }
        });
        searchPanel.add(new JLabel("Поиск:"));
        searchPanel.add(searchField);
        rightPanel.add(searchPanel, BorderLayout.NORTH);

        JButton addButton = new JButton("➕ Добавить в группу");
        addButton.addActionListener(e -> addMember());
        rightPanel.add(addButton, BorderLayout.SOUTH);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton saveButton = new JButton("💾 Сохранить и закрыть");
        saveButton.addActionListener(e -> saveAndClose());

        JButton cancelButton = new JButton("❌ Отмена");
        cancelButton.addActionListener(e -> cancel());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Загружаем участников группы
        loadMembers();

        // Загружаем всех пользователей (кроме администраторов?)
        loadAllUsers();
    }

    private void loadMembers() {
        membersModel.setRowCount(0);
        for (User member : group.getMembers()) {
            Object[] row = {
                    member.getId(),
                    member.getFullName(),
                    member.getUsername(),
                    getRoleName(member.getRole())
            };
            membersModel.addRow(row);
        }
    }

    private void loadAllUsers() {
        // Загружаем всех пользователей, кроме текущего администратора (опционально)
        // Исключаем уже состоящих в группе
        List<User> usersToShow = new ArrayList<>();

        for (User user : allUsers) {
            boolean alreadyInGroup = false;
            for (User member : group.getMembers()) {
                if (member.getId() == user.getId()) {
                    alreadyInGroup = true;
                    break;
                }
            }
            if (!alreadyInGroup) {
                usersToShow.add(user);
            }
        }

        availableModel.setRowCount(0);
        for (User user : usersToShow) {
            Object[] row = {
                    user.getId(),
                    user.getFullName(),
                    user.getUsername(),
                    getRoleName(user.getRole())
            };
            availableModel.addRow(row);
        }
    }

    private void searchUsers(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadAllUsers();
            return;
        }

        String search = searchText.toLowerCase().trim();
        availableModel.setRowCount(0);

        for (User user : allUsers) {
            boolean alreadyInGroup = false;
            for (User member : group.getMembers()) {
                if (member.getId() == user.getId()) {
                    alreadyInGroup = true;
                    break;
                }
            }

            if (!alreadyInGroup &&
                    (user.getFullName().toLowerCase().contains(search) ||
                            user.getUsername().toLowerCase().contains(search))) {
                Object[] row = {
                        user.getId(),
                        user.getFullName(),
                        user.getUsername(),
                        getRoleName(user.getRole())
                };
                availableModel.addRow(row);
            }
        }
    }

    private void addMember() {
        int selectedRow = availableUsersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите пользователя для добавления в группу!",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) availableModel.getValueAt(selectedRow, 0);
        String userName = (String) availableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Добавить пользователя '" + userName + "' в группу?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (groupDAO.addUserToGroup(userId, group.getId())) {
                // Обновляем данные
                User addedUser = userDAO.findById(userId);
                if (addedUser != null) {
                    group.addMember(addedUser);
                }
                loadMembers();
                loadAllUsers();
                JOptionPane.showMessageDialog(this, "Пользователь добавлен в группу!");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при добавлении пользователя!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите участника для удаления из группы!",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) membersModel.getValueAt(selectedRow, 0);
        String userName = (String) membersModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить пользователя '" + userName + "' из группы?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (groupDAO.removeUserFromGroup(userId, group.getId())) {
                // Обновляем данные
                User removedUser = null;
                for (User u : group.getMembers()) {
                    if (u.getId() == userId) {
                        removedUser = u;
                        break;
                    }
                }
                if (removedUser != null) {
                    group.removeMember(removedUser);
                }
                loadMembers();
                loadAllUsers();
                JOptionPane.showMessageDialog(this, "Пользователь удалён из группы!");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при удалении пользователя!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAndClose() {
        confirmed = true;
        dispose();
    }

    private void cancel() {
        confirmed = false;
        dispose();
    }

    private String getRoleName(String role) {
        switch (role) {
            case "ADMIN": return "Администратор";
            case "TEACHER": return "Преподаватель";
            case "TAKER": return "Тестируемый";
            default: return role;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}