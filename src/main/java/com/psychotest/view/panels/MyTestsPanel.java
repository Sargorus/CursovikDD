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

public class MyTestsPanel extends JPanel {
    private TeacherController controller;
    private JTable testsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton editButton;
    private JButton deleteButton;
    private JButton assignButton;

    public MyTestsPanel(TeacherController controller) {
        this.controller = controller;
        initComponents();
        loadTests();
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
        refreshButton.addActionListener(e -> loadTests());
        topPanel.add(refreshButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Таблица тестов
        String[] columns = {"ID", "Название", "Описание", "Вопросов в сессии", "Дата создания"};
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
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
            assignButton.setEnabled(hasSelection);
        });

        // Настройка ширины колонок
        testsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        testsTable.getColumnModel().getColumn(3).setMaxWidth(120);
        testsTable.getColumnModel().getColumn(4).setMaxWidth(120);

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

        deleteButton = new JButton("🗑️ Удалить");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteTest());

        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        buttonPanel.add(assignButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadTests() {
        tableModel.setRowCount(0);
        List<Test> tests = controller.getMyTests();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Test test : tests) {
            Object[] row = {
                    test.getId(),
                    test.getName(),
                    truncate(test.getDescription(), 50),
                    test.getQuestionsPerSession() > 0 ? test.getQuestionsPerSession() : "Все",
                    test.getCreatedAt() != null ? test.getCreatedAt().format(formatter) : ""
            };
            tableModel.addRow(row);
        }
    }

    private void searchTests() {
        String searchText = searchField.getText();
        tableModel.setRowCount(0);
        List<Test> tests = controller.searchMyTests(searchText);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Test test : tests) {
            Object[] row = {
                    test.getId(),
                    test.getName(),
                    truncate(test.getDescription(), 50),
                    test.getQuestionsPerSession() > 0 ? test.getQuestionsPerSession() : "Все",
                    test.getCreatedAt() != null ? test.getCreatedAt().format(formatter) : ""
            };
            tableModel.addRow(row);
        }
    }

    private void createTest() {
        TestConstructorDialog dialog = new TestConstructorDialog(
                SwingUtilities.getWindowAncestor(this),
                controller.getTeacherId()
        );
        dialog.setVisible(true);
        loadTests(); // Обновляем список после создания
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

        // Открываем диалог назначения теста
        AssignTestDialog dialog = new AssignTestDialog(
                SwingUtilities.getWindowAncestor(this),
                controller,
                testId,
                testName
        );
        dialog.setVisible(true);
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
            if (controller.deleteTest(testId)) {
                JOptionPane.showMessageDialog(this, "Тест успешно удалён!");
                loadTests();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении теста!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}