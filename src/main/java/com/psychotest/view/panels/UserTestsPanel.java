package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.TestSession;
import main.java.com.psychotest.service.ResultService;
import main.java.com.psychotest.view.dialogs.AssignTestDialog;
import main.java.com.psychotest.view.dialogs.ResultDetailDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserTestsPanel extends JPanel {
    private TeacherController controller;
    private int currentUserId;
    private String currentUserName;
    private JTable testsTable;
    private DefaultTableModel tableModel;
    private JButton assignButton;
    private JButton viewResultButton;
    private JButton unassignButton;
    private JLabel userInfoLabel;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public UserTestsPanel(TeacherController controller) {
        this.controller = controller;
        initComponents();
        showEmptyState();
    }

    public void setUser(int userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        userInfoLabel.setText("👤 Участник: " + userName);
        loadUserTests();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель с информацией о пользователе
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        userInfoLabel = new JLabel("👤 Участник: не выбран");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userInfoLabel.setForeground(new Color(70, 130, 200));
        topPanel.add(userInfoLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> {
            if (currentUserId > 0) {
                loadUserTests();
            }
        });
        topPanel.add(refreshButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Таблица тестов
        String[] columns = {"ID", "Название теста", "Назначен", "Статус", "Дата прохождения", "Результат"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        testsTable = new JTable(tableModel);
        testsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        testsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = testsTable.getSelectedRow() != -1;
            boolean isCompleted = false;
            if (hasSelection) {
                String status = (String) tableModel.getValueAt(testsTable.getSelectedRow(), 3);
                isCompleted = "Завершён".equals(status);
            }
            viewResultButton.setEnabled(hasSelection && isCompleted);
            unassignButton.setEnabled(hasSelection && !isCompleted);
        });

        testsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        testsTable.getColumnModel().getColumn(2).setMaxWidth(100);
        testsTable.getColumnModel().getColumn(3).setMaxWidth(100);
        testsTable.getColumnModel().getColumn(4).setMaxWidth(120);
        testsTable.getColumnModel().getColumn(5).setMaxWidth(200);

        JScrollPane scrollPane = new JScrollPane(testsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        assignButton = new JButton("➕ Назначить тест");
        assignButton.addActionListener(e -> assignTest());

        viewResultButton = new JButton("🔍 Просмотреть результат");
        viewResultButton.setEnabled(false);
        viewResultButton.addActionListener(e -> viewResult());

        unassignButton = new JButton("➖ Отвязать тест");
        unassignButton.setEnabled(false);
        unassignButton.addActionListener(e -> unassignTest());

        buttonPanel.add(assignButton);
        buttonPanel.add(viewResultButton);
        buttonPanel.add(unassignButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUserTests() {
        tableModel.setRowCount(0);

        if (currentUserId <= 0) {
            showEmptyState();
            return;
        }

        // Получаем назначенные тесты
        List<Integer> assignedTestIds = controller.getAssignedTestIdsForUser(currentUserId);
        List<Test> assignedTests = assignedTestIds.stream()
                .map(id -> controller.getTestById(id))
                .filter(t -> t != null)
                .collect(Collectors.toList());

        // Получаем пройденные тесты
        List<TestSession> completedSessions = controller.getCompletedSessionsForUser(currentUserId);

        // Создаём мапу для быстрого поиска пройденных тестов
        java.util.Map<Integer, TestSession> completedMap = new java.util.HashMap<>();
        for (TestSession session : completedSessions) {
            completedMap.put(session.getTestId(), session);
        }

        // Отображаем все назначенные тесты
        for (Test test : assignedTests) {
            TestSession completed = completedMap.get(test.getId());
            boolean isCompleted = completed != null;

            Object[] row = {
                    test.getId(),
                    test.getName(),
                    "Да",
                    isCompleted ? "Завершён" : "Не завершён",
                    isCompleted && completed.getEndTime() != null ?
                            completed.getEndTime().format(formatter) : "—",
                    isCompleted ? "Просмотреть" : "—"
            };
            tableModel.addRow(row);
        }

        // Добавляем пройденные тесты, которые уже не назначены (если такие есть)
        for (TestSession session : completedSessions) {
            boolean isAssigned = assignedTests.stream().anyMatch(t -> t.getId() == session.getTestId());
            if (!isAssigned) {
                Test test = controller.getTestById(session.getTestId());
                if (test != null) {
                    Object[] row = {
                            test.getId(),
                            test.getName(),
                            "Нет",
                            "Завершён",
                            session.getEndTime() != null ? session.getEndTime().format(formatter) : "—",
                            "Просмотреть"
                    };
                    tableModel.addRow(row);
                }
            }
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"", "Нет назначенных тестов", "", "", "", ""});
        }
    }

    private void showEmptyState() {
        tableModel.setRowCount(0);
        userInfoLabel.setText("👤 Участник: не выбран");
        tableModel.addRow(new Object[]{"", "Выберите участника из списка слева", "", "", "", ""});
    }

    private void assignTest() {
        // Получаем список тестов, которые ещё не назначены пользователю
        List<Integer> assignedIds = controller.getAssignedTestIdsForUser(currentUserId);
        List<Test> allTests = controller.getMyTests();
        List<Test> availableTests = allTests.stream()
                .filter(t -> !assignedIds.contains(t.getId()))
                .collect(Collectors.toList());

        if (availableTests.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Нет доступных тестов для назначения.\nВсе тесты уже назначены этому пользователю.",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Создаём диалог выбора теста
        JComboBox<Test> testCombo = new JComboBox<>(availableTests.toArray(new Test[0]));

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Выберите тест для назначения:"));
        panel.add(testCombo);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Назначение теста", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Test selectedTest = (Test) testCombo.getSelectedItem();
            if (selectedTest != null) {
                // ПЕРЕДАЁМ NULL ВМЕСТО ДАТЫ
                boolean success = controller.assignTestToUser(selectedTest.getId(), currentUserId, null);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Тест \"" + selectedTest.getName() + "\" назначен пользователю " + currentUserName);
                    loadUserTests();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при назначении теста!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void viewResult() {
        int selectedRow = testsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int testId = (int) tableModel.getValueAt(selectedRow, 0);
        String testName = (String) tableModel.getValueAt(selectedRow, 1);

        // Находим сессию с результатами
        List<TestSession> sessions = controller.getCompletedSessionsForUser(currentUserId);
        TestSession targetSession = sessions.stream()
                .filter(s -> s.getTestId() == testId)
                .findFirst()
                .orElse(null);

        if (targetSession != null) {
            ResultService.SessionDetail detail = controller.getSessionDetail(targetSession.getId());
            if (detail != null) {
                ResultDetailDialog dialog = new ResultDetailDialog(
                        SwingUtilities.getWindowAncestor(this),
                        detail, currentUserName, testName
                );
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Не удалось загрузить результат!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void unassignTest() {
        int selectedRow = testsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int testId = (int) tableModel.getValueAt(selectedRow, 0);
        String testName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Отвязать тест \"" + testName + "\" от пользователя " + currentUserName + "?\n" +
                        "Это действие нельзя отменить.",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = controller.unassignTestFromUser(testId, currentUserId);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Тест \"" + testName + "\" отвязан от пользователя " + currentUserName);
                loadUserTests();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при отвязке теста!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
