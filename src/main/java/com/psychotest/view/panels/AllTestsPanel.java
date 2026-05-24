package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.AdminController;
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
    private AdminController adminController;
    private JTable testsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JButton editButton;
    private JButton deleteButton;
    private JButton assignButton;
    private JButton resultsButton;
    private OnTestSelectedListener listener;
    private boolean isAdmin = false;

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

    // Конструктор для администратора
    public AllTestsPanel(AdminController adminController) {
        this.adminController = adminController;
        this.isAdmin = true;
        initComponents();
        loadTestsForAdmin();
    }

    public void setOnTestSelectedListener(OnTestSelectedListener listener) {
        this.listener = listener;
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Верхняя панель с поиском и фильтром
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Панель поиска (слева)
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

        // Панель фильтра и кнопок (справа)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Фильтр для преподавателя (для администратора не нужен)
        if (!isAdmin) {
            rightPanel.add(new JLabel("Показать:"));
            filterCombo = new JComboBox<>(new String[]{"Мои тесты", "Все тесты"});
            filterCombo.setPreferredSize(new Dimension(120, 25));
            filterCombo.addActionListener(e -> loadTests());
            rightPanel.add(filterCombo);
        }

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> {
            if (isAdmin) {
                loadTestsForAdmin();
            } else {
                loadTests();
            }
        });
        rightPanel.add(refreshButton);

        topPanel.add(rightPanel, BorderLayout.EAST);

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
        testsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
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

    // Загрузка тестов для преподавателя (с учётом фильтра)
    private void loadTests() {
        tableModel.setRowCount(0);

        boolean showAll = filterCombo != null && "Все тесты".equals(filterCombo.getSelectedItem());
        List<Test> tests = showAll ? controller.getAllTests() : controller.getMyTests();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Test test : tests) {
            String authorName = showAll ? controller.getAuthorName(test.getCreatedBy()) : "Я";
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

    // Загрузка всех тестов для администратора
    private void loadTestsForAdmin() {
        tableModel.setRowCount(0);

        List<Test> tests = adminController.getAllTests();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Test test : tests) {
            String authorName = adminController.getAuthorName(test.getCreatedBy());
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

    // Поиск тестов
    private void searchTests() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (isAdmin) {
            loadTestsForAdmin();
            if (!searchText.isEmpty()) {
                for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
                    String name = (String) tableModel.getValueAt(i, 1);
                    if (name != null && !name.toLowerCase().contains(searchText)) {
                        tableModel.removeRow(i);
                    }
                }
            }
        } else {
            boolean showAll = filterCombo != null && "Все тесты".equals(filterCombo.getSelectedItem());
            List<Test> tests;

            if (showAll) {
                tests = controller.getAllTests();
                if (!searchText.isEmpty()) {
                    tests = tests.stream()
                            .filter(t -> t.getName().toLowerCase().contains(searchText))
                            .toList();
                }
            } else {
                tests = controller.searchMyTests(searchText);
            }

            tableModel.setRowCount(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            for (Test test : tests) {
                String authorName = showAll ? controller.getAuthorName(test.getCreatedBy()) : "Я";
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
    }

    // Создание теста (только для преподавателя)
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

    // Редактирование теста
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

    // Назначение теста
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

    // Показать результаты
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

    // Удаление теста
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
            boolean success;
            if (isAdmin) {
                success = deleteTestAsAdmin(testId);
            } else {
                success = controller.deleteTest(testId);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Тест успешно удалён!");
                if (isAdmin) {
                    loadTestsForAdmin();
                } else {
                    loadTests();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении теста!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Удаление теста для администратора
    private boolean deleteTestAsAdmin(int testId) {
        return adminController.deleteTest(testId);
    }

    // Обрезка длинных строк
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}