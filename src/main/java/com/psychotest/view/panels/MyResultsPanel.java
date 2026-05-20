package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TakerController;
import main.java.com.psychotest.model.TestSession;
import main.java.com.psychotest.model.TestResult;
import main.java.com.psychotest.view.dialogs.ResultDetailDialog;
import main.java.com.psychotest.service.ResultService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.awt.Dialog.ModalityType;

public class MyResultsPanel extends JPanel {
    private TakerController controller;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton viewDetailButton;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public MyResultsPanel(TakerController controller) {
        this.controller = controller;
        initComponents();
        loadResults();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Верхняя панель с информацией
        JLabel infoLabel = new JLabel("История пройденных тестов");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(infoLabel, BorderLayout.NORTH);

        // Таблица результатов
        String[] columns = {"ID", "Тест", "Дата прохождения", "Статус"};
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
        });

        resultsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        resultsTable.getColumnModel().getColumn(3).setMaxWidth(100);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> loadResults());

        viewDetailButton = new JButton("🔍 Детальный просмотр");
        viewDetailButton.setEnabled(false);
        viewDetailButton.addActionListener(e -> viewDetail());

        buttonPanel.add(refreshButton);
        buttonPanel.add(viewDetailButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        List<TestSession> sessions = controller.getTestHistory();

        for (TestSession session : sessions) {
            String statusText = getStatusText(session.getStatus());
            Object[] row = {
                    session.getId(),
                    getTestName(session.getTestId()),
                    session.getStartTime() != null ? session.getStartTime().format(formatter) : "",
                    statusText
            };
            tableModel.addRow(row);
        }

        if (sessions.isEmpty()) {
            tableModel.addRow(new Object[]{"", "Нет пройденных тестов", "", ""});
        }
    }

    private String getTestName(int testId) {
        // TODO: загружать название теста из БД
        return "Тест #" + testId;
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
            return;
        }

        int sessionId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 3);

        if (!"Завершён".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Детальный просмотр доступен только для завершённых тестов.",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Показываем индикатор загрузки
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<TestResult, Void> worker = new SwingWorker<TestResult, Void>() {
            @Override
            protected TestResult doInBackground() {
                return controller.getTestResult(sessionId);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    TestResult result = get();
                    if (result != null) {
                        showResultDialog(result);
                    } else {
                        JOptionPane.showMessageDialog(MyResultsPanel.this,
                                "Не удалось загрузить результаты!",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MyResultsPanel.this,
                            "Ошибка: " + e.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showResultDialog(TestResult result) {
        // Создаём диалог с результатами
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Результаты теста: " + result.getTestName(),
                ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка с результатами по параметрам
        JPanel paramsPanel = createParametersPanel(result);
        tabbedPane.addTab("📊 Результаты", paramsPanel);

        // Вкладка с полной интерпретацией
        JPanel interpretationPanel = createInterpretationPanel(result);
        tabbedPane.addTab("📝 Интерпретация", interpretationPanel);

        dialog.add(tabbedPane, BorderLayout.CENTER);

        // Кнопка закрытия
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createParametersPanel(TestResult result) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (result.getScaledScores() == null || result.getScaledScores().isEmpty()) {
            JLabel emptyLabel = new JLabel("Нет данных о результатах", SwingConstants.CENTER);
            panel.add(emptyLabel, BorderLayout.CENTER);
            return panel;
        }

        // Создаём таблицу
        String[] columns = {"Параметр", "Результат", "Интерпретация"};
        Object[][] data = new Object[result.getScaledScores().size()][3];

        int i = 0;
        for (Map.Entry<String, Integer> entry : result.getScaledScores().entrySet()) {
            String paramName = entry.getKey();
            data[i][0] = paramName;
            data[i][1] = entry.getValue();
            data[i][2] = result.getInterpretations() != null ?
                    result.getInterpretations().get(paramName) : "";
            i++;
        }

        JTable table = new JTable(data, columns);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(350);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInterpretationPanel(TestResult result) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea interpretationArea = new JTextArea();
        interpretationArea.setEditable(false);
        interpretationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        interpretationArea.setLineWrap(true);
        interpretationArea.setWrapStyleWord(true);

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                    РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ                        \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n\n");
        sb.append("Тест: ").append(result.getTestName()).append("\n");
        sb.append("───────────────────────────────────────────────────────────────────\n\n");

        if (result.getScaledScores() != null) {
            for (Map.Entry<String, Integer> entry : result.getScaledScores().entrySet()) {
                String paramName = entry.getKey();
                sb.append("📊 ").append(paramName).append(": ").append(entry.getValue()).append("\n");
                if (result.getInterpretations() != null) {
                    sb.append("   ").append(result.getInterpretations().get(paramName)).append("\n");
                }
                sb.append("\n");
            }
        }

        interpretationArea.setText(sb.toString());
        interpretationArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(interpretationArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}