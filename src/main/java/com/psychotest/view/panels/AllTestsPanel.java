package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.view.dialogs.AssignTestDialog;
import main.java.com.psychotest.view.dialogs.TestConstructorDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AllTestsPanel extends JPanel {
    private TeacherController controller;
    private JTable testsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton editButton;
    private JButton deleteButton;
    private JButton assignButton;
    private JButton resultsButton;
    private OnTestSelectedListener listener;
    private boolean isAdmin = false;  // ← флаг для администратора

    public interface OnTestSelectedListener {
        void onTestSelected(int testId, String testName);
    }

    // Конструктор для преподавателя
    public AllTestsPanel(TeacherController controller) {
        this.controller = controller;
        this.isAdmin = false;
        initComponents();
        loadTests();
    }

    // Конструктор для администратора (передаём null, так как у админа нет TeacherController)
    public AllTestsPanel() {
        this.controller = null;
        this.isAdmin = true;
        initComponents();
        loadTestsForAdmin();
    }

    public void setOnTestSelectedListener(OnTestSelectedListener listener) {
        this.listener = listener;
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Верхняя панель с поиском
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Поиск:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchTests();
            }
        });
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.WEST);

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> {
            if (isAdmin) {
                loadTestsForAdmin();
            } else {
                loadTests();
            }
        });
        topPanel.add(refreshButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Таблица тестов
        String[] columns = {"ID", "Название", "Описание", "Автор", "Вопросов в сессии", "Дата создания"};
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
            if (editButton != null) editButton.setEnabled(hasSelection);
            if (deleteButton != null) deleteButton.setEnabled(hasSelection);
            if (assignButton != null) assignButton.setEnabled(hasSelection);
            if (resultsButton != null) resultsButton.setEnabled(hasSelection);
        });

        testsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        testsTable.getColumnModel().getColumn(3).setMaxWidth(150);
        testsTable.getColumnModel().getColumn(4).setMaxWidth(120);
        testsTable.getColumnModel().getColumn(5).setMaxWidth(120);

        JScrollPane scrollPane = new JScrollPane(testsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton createButton = new JButton("➕ Создать тест");
        createButton.addActionListener(e -> createTest());

        editButton = new JButton("✏️ Редактировать");
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editTest());

        assignButton = new JButton("📋 Назначить");
        assignButton.setEnabled(false);
        assignButton.addActionListener(e -> assignTest());

        resultsButton = new JButton("📊 Результаты");
        resultsButton.setEnabled(false);
        resultsButton.addActionListener(e -> showResults());

        deleteButton = new JButton("🗑️ Удалить");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteTest());

        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        buttonPanel.add(assignButton);
        buttonPanel.add(resultsButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Загрузка всех тестов для администратора
    private void loadTestsForAdmin() {
        tableModel.setRowCount(0);
        List<Test> tests = getAllTestsFromDB();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Test test : tests) {
            String authorName = getAuthorName(test.getCreatedBy());
            Object[] row = {
                    test.getId(),
                    test.getName(),
                    truncate(test.getDescription(), 50),
                    authorName,
                    test.getQuestionsPerSession() > 0 ? test.getQuestionsPerSession() : "Все",
                    test.getCreatedAt() != null ? test.getCreatedAt().format(formatter) : ""
            };
            tableModel.addRow(row);
        }

        if (tests.isEmpty()) {
            tableModel.addRow(new Object[]{"", "Нет тестов", "", "", "", ""});
        }
    }

    // Загрузка тестов для преподавателя (только свои)
    private void loadTests() {
        tableModel.setRowCount(0);
        List<Test> tests = controller.getMyTests();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Test test : tests) {
            Object[] row = {
                    test.getId(),
                    test.getName(),
                    truncate(test.getDescription(), 50),
                    "Я",
                    test.getQuestionsPerSession() > 0 ? test.getQuestionsPerSession() : "Все",
                    test.getCreatedAt() != null ? test.getCreatedAt().format(formatter) : ""
            };
            tableModel.addRow(row);
        }

        if (tests.isEmpty()) {
            tableModel.addRow(new Object[]{"", "Нет тестов", "", "", "", ""});
        }
    }

    private void searchTests() {
        String searchText = searchField.getText();
        tableModel.setRowCount(0);

        List<Test> tests;
        if (isAdmin) {
            tests = searchTestsInAll(searchText);
        } else {
            tests = controller.searchMyTests(searchText);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Test test : tests) {
            String authorName = isAdmin ? getAuthorName(test.getCreatedBy()) : "Я";
            Object[] row = {
                    test.getId(),
                    test.getName(),
                    truncate(test.getDescription(), 50),
                    authorName,
                    test.getQuestionsPerSession() > 0 ? test.getQuestionsPerSession() : "Все",
                    test.getCreatedAt() != null ? test.getCreatedAt().format(formatter) : ""
            };
            tableModel.addRow(row);
        }

        if (tests.isEmpty()) {
            tableModel.addRow(new Object[]{"", "Нет тестов", "", "", "", ""});
        }
    }

    // Методы для администратора (работа с БД напрямую)
    private List<Test> getAllTestsFromDB() {
        try {
            main.java.com.psychotest.dao.TestDAO testDAO = new main.java.com.psychotest.dao.TestDAO();
            return testDAO.findAll();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private List<Test> searchTestsInAll(String searchText) {
        try {
            main.java.com.psychotest.dao.TestDAO testDAO = new main.java.com.psychotest.dao.TestDAO();
            List<Test> allTests = testDAO.findAll();
            if (searchText == null || searchText.trim().isEmpty()) {
                return allTests;
            }
            String search = searchText.toLowerCase().trim();
            return allTests.stream()
                    .filter(t -> t.getName().toLowerCase().contains(search))
                    .toList();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private String getAuthorName(int createdBy) {
        try {
            main.java.com.psychotest.dao.UserDAO userDAO = new main.java.com.psychotest.dao.UserDAO();
            main.java.com.psychotest.model.User user = userDAO.findById(createdBy);
            return user != null ? user.getFullName() : "Неизвестный";
        } catch (Exception e) {
            return "Неизвестный";
        }
    }

    private void createTest() {
        if (isAdmin) {
            JOptionPane.showMessageDialog(this,
                    "Администратор не может создавать тесты.\nВойдите как преподаватель.",
                    "Доступ запрещён", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TestConstructorDialog dialog = new TestConstructorDialog(
                SwingUtilities.getWindowAncestor(this),
                controller.getTeacherId()
        );
        dialog.setVisible(true);
        loadTests();
    }

    private void editTest() {
        int selectedRow = testsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите тест для редактирования!");
            return;
        }

        int testId = (int) tableModel.getValueAt(selectedRow, 0);
        String testName = (String) tableModel.getValueAt(selectedRow, 1);

        JOptionPane.showMessageDialog(this,
                "Редактирование теста \"" + testName + "\"\n\n" +
                        "В текущей версии редактирование доступно только через создание копии.\n" +
                        "Скопируйте тест и отредактируйте копию.",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private void assignTest() {
        int selectedRow = testsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите тест для назначения!");
            return;
        }

        int testId = (int) tableModel.getValueAt(selectedRow, 0);
        String testName = (String) tableModel.getValueAt(selectedRow, 1);

        if (isAdmin) {
            JOptionPane.showMessageDialog(this,
                    "Администратор не может назначать тесты.\nВойдите как преподаватель.",
                    "Доступ запрещён", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AssignTestDialog dialog = new AssignTestDialog(
                SwingUtilities.getWindowAncestor(this),
                controller,
                testId,
                testName
        );
        dialog.setVisible(true);
    }

    private void showResults() {
        int selectedRow = testsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int testId = (int) tableModel.getValueAt(selectedRow, 0);
        String testName = (String) tableModel.getValueAt(selectedRow, 1);

        if (listener != null) {
            listener.onTestSelected(testId, testName);
        }
    }

    private void deleteTest() {
        int selectedRow = testsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите тест для удаления!");
            return;
        }

        int testId = (int) tableModel.getValueAt(selectedRow, 0);
        String testName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить тест \"" + testName + "\"?\n" +
                        "Все назначения и результаты будут также удалены.",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (isAdmin) {
                deleteTestAsAdmin(testId);
            } else {
                if (controller.deleteTest(testId)) {
                    JOptionPane.showMessageDialog(this, "Тест успешно удалён!");
                    loadTests();
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при удалении теста!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteTestAsAdmin(int testId) {
        try {
            main.java.com.psychotest.dao.TestDAO testDAO = new main.java.com.psychotest.dao.TestDAO();
            if (testDAO.delete(testId)) {
                JOptionPane.showMessageDialog(this, "Тест успешно удалён!");
                loadTestsForAdmin();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении теста!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при удалении теста: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}