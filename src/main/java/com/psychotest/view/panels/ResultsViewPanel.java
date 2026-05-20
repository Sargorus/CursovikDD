package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.service.ResultService;
import main.java.com.psychotest.view.dialogs.ResultDetailDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ResultsViewPanel extends JPanel {
    private TeacherController controller;
    private JComboBox<Test> testCombo;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton exportButton;
    private JButton viewDetailButton;
    private JLabel currentTestLabel;
    private int preSelectedTestId = -1;
    private String preSelectedTestName = "";

    public ResultsViewPanel(TeacherController controller) {
        this.controller = controller;
        initComponents();
        loadTests();
    }

    // ПРЕДВАРИТЕЛЬНЫЙ ВЫБОРА ТЕСТА
    public void selectTest(int testId, String testName) {
        this.preSelectedTestId = testId;
        this.preSelectedTestName = testName;
        loadTests();
        // Выбираем тест в комбобоксе
        for (int i = 0; i < testCombo.getItemCount(); i++) {
            Test test = testCombo.getItemAt(i);
            if (test.getId() == testId) {
                testCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель с выбором теста и информацией
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Панель выбора теста
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("Выберите тест:"));
        testCombo = new JComboBox<>();
        testCombo.setPreferredSize(new Dimension(300, 25));
        testCombo.addActionListener(e -> {
            if (testCombo.getSelectedItem() != null) {
                loadResults();
            }
        });
        selectPanel.add(testCombo);

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> {
            loadTests();
            loadResults();
        });
        selectPanel.add(refreshButton);

        topPanel.add(selectPanel, BorderLayout.WEST);

        // НАДПИСЬ С ТЕКУЩИМ ТЕСТОМ
        currentTestLabel = new JLabel("Текущий тест: не выбран");
        currentTestLabel.setFont(new Font("Arial", Font.BOLD, 12));
        currentTestLabel.setForeground(new Color(70, 130, 200));
        topPanel.add(currentTestLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Центральная панель - таблица результатов
        String[] columns = {"ID сессии", "ФИО", "Логин", "Дата прохождения", "Статус"};
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

        resultsTable.getColumnModel().getColumn(0).setMaxWidth(80);
        resultsTable.getColumnModel().getColumn(2).setMaxWidth(120);
        resultsTable.getColumnModel().getColumn(3).setMaxWidth(150);
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

        if (tests.isEmpty()) {
            currentTestLabel.setText("Текущий тест: нет доступных тестов");
        }
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        Test selectedTest = (Test) testCombo.getSelectedItem();
        if (selectedTest == null) {
            currentTestLabel.setText("Текущий тест: не выбран");
            return;
        }

        // ОБНОВЛЯЕМ НАДПИСЬ
        currentTestLabel.setText("Текущий тест: " + selectedTest.getName());

        List<ResultService.TestResult> results = controller.getResultsForTest(selectedTest.getId());

        for (ResultService.TestResult result : results) {
            Object[] row = {
                    result.getSessionId(),
                    result.getUserFullName(),
                    result.getUserLogin(),
                    result.getFormattedDate(),
                    getStatusText(result.getStatus())
            };
            tableModel.addRow(row);
        }

        if (results.isEmpty()) {
            tableModel.addRow(new Object[]{"", "Нет результатов", "", "", ""});
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

        Test selectedTest = (Test) testCombo.getSelectedItem();
        String testName = selectedTest != null ? selectedTest.getName() : "Тест";

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<ResultService.SessionDetail, Void> worker = new SwingWorker<>() {
            @Override
            protected ResultService.SessionDetail doInBackground() {
                return controller.getSessionDetail(sessionId);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    ResultService.SessionDetail detail = get();
                    if (detail != null) {
                        ResultDetailDialog dialog = new ResultDetailDialog(
                                SwingUtilities.getWindowAncestor(ResultsViewPanel.this),
                                detail, userName, testName
                        );
                        dialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(ResultsViewPanel.this,
                                "Не удалось загрузить детали результата!",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ResultsViewPanel.this,
                            "Ошибка: " + e.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void exportToExcel() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите результат для экспорта!");
            return;
        }

        int option = JOptionPane.showOptionDialog(this,
                "Что вы хотите экспортировать?",
                "Экспорт в Excel",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Все результаты теста", "Только выбранный результат", "Отмена"},
                "Все результаты теста");

        if (option == 2 || option == JOptionPane.CLOSED_OPTION) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("report.xlsx"));
        fileChooser.setDialogTitle("Сохранить отчёт как...");

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String basePath = fileChooser.getSelectedFile().getAbsolutePath();
        String finalPath = basePath.endsWith(".xlsx") ? basePath : basePath + ".xlsx";
        int sessionId = (int) tableModel.getValueAt(selectedRow, 0);
        Test selectedTest = (Test) testCombo.getSelectedItem();
        int exportOption = option;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (exportOption == 0) {
                    if (selectedTest == null) {
                        return false;
                    }
                    return controller.exportTestResultsToExcel(selectedTest.getId(), finalPath);
                } else {
                    return controller.exportSessionDetailsToExcel(sessionId, finalPath);
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(ResultsViewPanel.this,
                                "Отчёт успешно сохранён!\n" + finalPath,
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ResultsViewPanel.this,
                                "Ошибка при создании отчёта!",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ResultsViewPanel.this,
                            "Ошибка: " + e.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}