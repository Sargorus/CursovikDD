package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.service.ResultService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResultsViewPanel extends JPanel {
    private TeacherController controller;
    private JComboBox<Test> testCombo;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton exportButton;
    private JButton viewDetailButton;

    public ResultsViewPanel(TeacherController controller) {
        this.controller = controller;
        initComponents();
        loadTests();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель с выбором теста
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Выберите тест:"));
        testCombo = new JComboBox<>();
        testCombo.setPreferredSize(new Dimension(300, 25));
        testCombo.addActionListener(e -> loadResults());
        topPanel.add(testCombo);

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> {
            loadTests();
            loadResults();
        });
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Центральная панель - таблица результатов
        String[] columns = {"ID", "ФИО", "Логин", "Дата прохождения", "Статус", "Результат"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            viewDetailButton.setEnabled(resultsTable.getSelectedRow() != -1);
            exportButton.setEnabled(resultsTable.getSelectedRow() != -1);
        });

        // Настройка ширины колонок
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        resultsTable.getColumnModel().getColumn(2).setMaxWidth(120);
        resultsTable.getColumnModel().getColumn(3).setMaxWidth(120);
        resultsTable.getColumnModel().getColumn(4).setMaxWidth(100);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        viewDetailButton = new JButton("🔍 Детальный просмотр");
        viewDetailButton.setEnabled(false);
        viewDetailButton.addActionListener(e -> viewDetail());

        exportButton = new JButton("📊 Экспорт в Excel");
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> exportToExcel());

        buttonPanel.add(viewDetailButton);
        buttonPanel.add(exportButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadTests() {
        testCombo.removeAllItems();
        List<Test> tests = controller.getMyTests();
        for (Test test : tests) {
            testCombo.addItem(test);
        }
        if (tests.size() > 0) {
            loadResults();
        }
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        Test selectedTest = (Test) testCombo.getSelectedItem();
        if (selectedTest == null) {
            return;
        }

        List<ResultService.TestResult> results = controller.getResultsForTest(selectedTest.getId());

        for (ResultService.TestResult result : results) {
            Object[] row = {
                    result.getSessionId(),
                    result.getUserFullName(),
                    result.getUserLogin(),
                    result.getFormattedDate(),
                    getStatusText(result.getStatus()),
                    "-" // TODO: добавить краткий результат
            };
            tableModel.addRow(row);
        }

        if (results.isEmpty()) {
            tableModel.setRowCount(0);
            tableModel.addRow(new Object[]{"", "Нет данных", "", "", "", ""});
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "COMPLETED": return "Завершён";
            case "IN_PROGRESS": return "В процессе";
            case "ABANDONED": return "Прерван";
            default: return status;
        }
    }

    private void viewDetail() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите результат для просмотра!");
            return;
        }

        int sessionId = (int) tableModel.getValueAt(selectedRow, 0);
        String userName = (String) tableModel.getValueAt(selectedRow, 1);

        ResultService.SessionDetail detail = controller.getSessionDetail(sessionId);
        if (detail != null) {
            // TODO: Показать детали в отдельном диалоге
            JOptionPane.showMessageDialog(this,
                    "Детальный просмотр для " + userName + "\n" +
                            "Параметров: " + (detail.getParameterResults() != null ? detail.getParameterResults().size() : 0) + "\n" +
                            "Ответов: " + (detail.getAnswers() != null ? detail.getAnswers().size() : 0),
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Не удалось загрузить детали результата!");
        }
    }

    private void exportToExcel() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите результат для экспорта!");
            return;
        }

        // TODO: Экспорт в Excel
        JOptionPane.showMessageDialog(this,
                "Экспорт в Excel будет реализован в следующей версии.",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }


}