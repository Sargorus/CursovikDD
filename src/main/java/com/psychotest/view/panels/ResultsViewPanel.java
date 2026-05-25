package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.service.ResultService;
import main.java.com.psychotest.view.dialogs.ChartDialog;
import main.java.com.psychotest.view.dialogs.ResultDetailDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class ResultsViewPanel extends JPanel {
    private TeacherController controller;
    private JComboBox<Test> testCombo;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton exportButton;
    private JButton viewDetailButton;
    private JButton deleteResultButton;
    private JButton chartButton;
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
            updateExportButton();
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
            int row = resultsTable.getSelectedRow();
            // Включаем только когда выбрана реальная строка (не строка-заглушка)
            boolean rowIsReal = row != -1 && tableModel.getValueAt(row, 0) instanceof Integer;
            viewDetailButton.setEnabled(rowIsReal);
            deleteResultButton.setEnabled(rowIsReal);
            updateExportButton();
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

        deleteResultButton = new JButton("🗑️ Удалить результат");
        deleteResultButton.setEnabled(false);
        deleteResultButton.addActionListener(e -> deleteResult());

        chartButton = new JButton("🥧 Диаграмма участников");
        chartButton.setEnabled(false);
        chartButton.addActionListener(e -> showChart());

        exportButton = new JButton("📊 Экспорт в Excel");
        exportButton.setEnabled(false); // включается после выбора теста
        exportButton.addActionListener(e -> exportToExcel());

        buttonPanel.add(viewDetailButton);
        buttonPanel.add(deleteResultButton);
        buttonPanel.add(chartButton);
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

    /** Включает кнопки экспорта и диаграммы если выбран тест с результатами */
    private void updateExportButton() {
        boolean testSelected = testCombo.getSelectedItem() != null;
        exportButton.setEnabled(testSelected);
        // Диаграмма доступна только если есть хотя бы одна настоящая строка
        boolean hasResults = testSelected && tableModel.getRowCount() > 0
                && tableModel.getValueAt(0, 0) instanceof Integer;
        chartButton.setEnabled(hasResults);
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        Test selectedTest = (Test) testCombo.getSelectedItem();
        updateExportButton();
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

        // Обновляем кнопки после наполнения таблицы (теперь известно, есть ли реальные строки)
        updateExportButton();
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

        Object idValDetail = tableModel.getValueAt(selectedRow, 0);
        if (!(idValDetail instanceof Integer)) return;
        int sessionId = (int) idValDetail;
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

    private void deleteResult() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) return;

        Object idVal = tableModel.getValueAt(selectedRow, 0);
        if (!(idVal instanceof Integer)) return;
        int sessionId = (int) idVal;
        String userName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить результат тестирования пользователя «" + userName + "»?\n\n" +
                "Будут удалены: ответы на вопросы, результаты по параметрам\n" +
                "и запись о прохождении теста. Это действие нельзя отменить.",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = controller.deleteResult(sessionId);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Результат успешно удалён.",
                        "Готово", JOptionPane.INFORMATION_MESSAGE);
                loadResults();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при удалении результата!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showChart() {
        Test selectedTest = (Test) testCombo.getSelectedItem();
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Выберите тест для построения диаграммы!");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Загружаем статистику И список участников по областям в фоне
        SwingWorker<Object[], Void> worker = new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                Map<String, Map<String, Integer>> stats =
                        controller.getTestStatistics(selectedTest.getId());
                Map<String, Map<String, List<String>>> participants =
                        controller.getParticipantsByLabel(selectedTest.getId());
                return new Object[]{stats, participants};
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Integer>> stats =
                            (Map<String, Map<String, Integer>>) result[0];
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, List<String>>> participants =
                            (Map<String, Map<String, List<String>>>) result[1];

                    if (stats == null || stats.isEmpty()) {
                        JOptionPane.showMessageDialog(ResultsViewPanel.this,
                                "Нет данных для построения диаграммы.\n" +
                                "Убедитесь, что тест имеет параметры с интерпретациями.",
                                "Нет данных", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    ChartDialog dialog = new ChartDialog(
                            SwingUtilities.getWindowAncestor(ResultsViewPanel.this),
                            selectedTest.getName(), stats, participants);
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ResultsViewPanel.this,
                            "Ошибка при построении диаграммы: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void exportToExcel() {
        Test selectedTest = (Test) testCombo.getSelectedItem();
        if (selectedTest == null) {
            JOptionPane.showMessageDialog(this, "Выберите тест для экспорта!");
            return;
        }

        int selectedRow = resultsTable.getSelectedRow();
        int option;

        if (selectedRow == -1) {
            // Строка не выбрана — только экспорт всего теста
            option = 0;
        } else {
            option = JOptionPane.showOptionDialog(this,
                    "Что вы хотите экспортировать?",
                    "Экспорт в Excel",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Все результаты теста", "Только выбранный результат", "Отмена"},
                    "Все результаты теста");
            if (option == 2 || option == JOptionPane.CLOSED_OPTION) return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("report.xls"));
        fileChooser.setDialogTitle("Сохранить отчёт как...");

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String basePath = fileChooser.getSelectedFile().getAbsolutePath();
        String finalPath = basePath.endsWith(".xls") ? basePath : basePath + ".xls";
        // Если выбрана строка-заглушка (ID не Integer) — сбрасываем выбор
        boolean rowIsReal = selectedRow >= 0 && tableModel.getValueAt(selectedRow, 0) instanceof Integer;
        if (!rowIsReal && option != 0) {
            option = 0; // принудительно экспортируем весь тест
        }
        int sessionId = rowIsReal ? (int) tableModel.getValueAt(selectedRow, 0) : -1;
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