package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TakerController;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.view.dialogs.TestTakingDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AvailableTestsPanel extends JPanel {
    private TakerController controller;
    private JTable testsTable;
    private DefaultTableModel tableModel;
    private JButton startButton;

    public AvailableTestsPanel(TakerController controller) {
        this.controller = controller;
        initComponents();
        loadTests();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Верхняя панель с информацией
        JLabel infoLabel = new JLabel("Доступные тесты для прохождения");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(infoLabel, BorderLayout.NORTH);

        // Таблица тестов
        String[] columns = {"ID", "Название", "Описание", "Вопросов"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        testsTable = new JTable(tableModel);
        testsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        testsTable.getSelectionModel().addListSelectionListener(e -> {
            startButton.setEnabled(testsTable.getSelectedRow() != -1);
        });

        testsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        testsTable.getColumnModel().getColumn(3).setMaxWidth(80);

        JScrollPane scrollPane = new JScrollPane(testsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> loadTests());

        startButton = new JButton("▶ Начать тест");
        startButton.setEnabled(false);
        startButton.addActionListener(e -> startTest());

        buttonPanel.add(refreshButton);
        buttonPanel.add(startButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadTests() {
        tableModel.setRowCount(0);
        List<Test> tests = controller.getAvailableTests();

        for (Test test : tests) {
            Object[] row = {
                    test.getId(),
                    test.getName(),
                    truncate(test.getDescription(), 50),
                    test.getQuestionsPerSession() > 0 ? test.getQuestionsPerSession() : "Все"
            };
            tableModel.addRow(row);
        }

        if (tests.isEmpty()) {
            tableModel.addRow(new Object[]{"", "Нет доступных тестов", "", ""});
        }
    }

    private void startTest() {
        int selectedRow = testsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int testId = (int) tableModel.getValueAt(selectedRow, 0);
        String testName = (String) tableModel.getValueAt(selectedRow, 1);

        // Подтверждение начала теста
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите начать тест \"" + testName + "\"?\n" +
                        "После начала вы не сможете прерваться (ответы сохраняются автоматически).",
                "Начало теста",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int sessionId = controller.startTest(testId);
            if (sessionId != -1) {
                TestTakingDialog dialog = new TestTakingDialog(
                        SwingUtilities.getWindowAncestor(this),
                        controller,
                        sessionId,
                        testId,
                        testName
                );
                dialog.setVisible(true);
                // После закрытия диалога обновляем список
                loadTests();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при начале теста!",
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
