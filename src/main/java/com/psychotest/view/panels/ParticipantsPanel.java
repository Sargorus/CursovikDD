package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Group;
import main.java.com.psychotest.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class ParticipantsPanel extends JPanel {
    private TeacherController controller;
    private JTable participantsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> groupFilterCombo;
    private OnParticipantSelectedListener listener;

    public interface OnParticipantSelectedListener {
        void onParticipantSelected(int userId, String userName);
    }

    public ParticipantsPanel(TeacherController controller) {
        this.controller = controller;
        initComponents();
        loadParticipants();
    }

    public void setOnParticipantSelectedListener(OnParticipantSelectedListener listener) {
        this.listener = listener;
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Верхняя панель с поиском и фильтром
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Поиск:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadParticipants();
            }
        });
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.WEST);

        // Панель фильтра по группам
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("Фильтр по группе:"));
        groupFilterCombo = new JComboBox<>();
        groupFilterCombo.addItem("Все группы");
        loadGroupsToFilter();
        groupFilterCombo.addActionListener(e -> loadParticipants());
        filterPanel.add(groupFilterCombo);
        topPanel.add(filterPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Таблица участников
        String[] columns = {"ID", "ФИО", "Логин", "Группы", "Доступные тесты", "Пройдено"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        participantsTable = new JTable(tableModel);
        participantsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ← ИЗМЕНЕНИЕ: переход по двойному клику
        participantsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // двойной клик
                    int row = participantsTable.getSelectedRow();
                    if (row != -1 && listener != null) {
                        int userId = (int) tableModel.getValueAt(row, 0);
                        String userName = (String) tableModel.getValueAt(row, 1);
                        listener.onParticipantSelected(userId, userName);
                    }
                }
            }
        });

        participantsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        participantsTable.getColumnModel().getColumn(2).setMaxWidth(120);
        participantsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        participantsTable.getColumnModel().getColumn(4).setMaxWidth(80);
        participantsTable.getColumnModel().getColumn(5).setMaxWidth(80);

        JScrollPane scrollPane = new JScrollPane(participantsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель с кнопкой обновления
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> {
            loadGroupsToFilter();
            loadParticipants();
        });
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadGroupsToFilter() {
        List<Group> groups = controller.getAllGroups();
        String currentSelection = (String) groupFilterCombo.getSelectedItem();
        groupFilterCombo.removeAllItems();
        groupFilterCombo.addItem("Все группы");
        for (Group group : groups) {
            groupFilterCombo.addItem(group.getName());
        }
        if (currentSelection != null && groupFilterCombo.getItemCount() > 0) {
            groupFilterCombo.setSelectedItem(currentSelection);
        }
    }

    private void loadParticipants() {
        tableModel.setRowCount(0);

        String searchText = searchField.getText().toLowerCase().trim();
        String selectedGroup = (String) groupFilterCombo.getSelectedItem();

        List<User> allTakers = controller.getAllTakers();

        List<User> filteredUsers = allTakers.stream()
                .filter(u -> searchText.isEmpty() ||
                        u.getFullName().toLowerCase().contains(searchText) ||
                        u.getUsername().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        for (User user : filteredUsers) {
            // Получаем группы пользователя
            List<Group> userGroups = controller.getUserGroups(user.getId());
            String groupsStr = userGroups.stream()
                    .map(Group::getName)
                    .collect(Collectors.joining(", "));

            // Фильтр по группе
            if (!"Все группы".equals(selectedGroup) && selectedGroup != null) {
                boolean inGroup = userGroups.stream()
                        .anyMatch(g -> g.getName().equals(selectedGroup));
                if (!inGroup) {
                    continue;
                }
            }

            // Получаем статистику тестов
            int totalTests = controller.getUserTestCount(user.getId());
            int completedTests = controller.getUserCompletedTestCount(user.getId());

            Object[] row = {
                    user.getId(),
                    user.getFullName(),
                    user.getUsername(),
                    groupsStr.isEmpty() ? "—" : groupsStr,
                    totalTests,
                    completedTests
            };
            tableModel.addRow(row);
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"", "Нет участников", "", "", "", ""});
        }
    }
}