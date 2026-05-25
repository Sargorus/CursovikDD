package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.model.TestResult;
import main.java.com.psychotest.service.ExcelReportService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Диалог отображения результатов пробного запуска теста преподавателем.
 * Показывает интерпретации по каждому параметру и предлагает экспорт в Excel.
 * Результаты НЕ сохраняются в БД.
 */
public class PreviewResultDialog extends JDialog {

    private final TestResult result;
    private final String testName;
    private final ExcelReportService excelService = new ExcelReportService();

    public PreviewResultDialog(Window parent, TestResult result, String testName) {
        super(parent, "Результаты пробного запуска: " + testName, ModalityType.APPLICATION_MODAL);
        this.result = result;
        this.testName = testName;
        initComponents();
        setSize(700, 520);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // ── Уведомление о пробном режиме ─────────────────────────────────────
        JLabel noteLabel = new JLabel(
                "⚠ Пробный запуск — результаты не сохранены в базу данных",
                SwingConstants.CENTER);
        noteLabel.setForeground(new Color(150, 75, 0));
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.BOLD, 12f));
        noteLabel.setOpaque(true);
        noteLabel.setBackground(new Color(255, 248, 220));
        noteLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 160, 50)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        add(noteLabel, BorderLayout.NORTH);

        // ── Текст с результатами ──────────────────────────────────────────────
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 12, 10, 12));

        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════════════════════════════════════\n");
        sb.append("              РЕЗУЛЬТАТЫ ПРОБНОГО ЗАПУСКА                   \n");
        sb.append("════════════════════════════════════════════════════════════\n\n");
        sb.append("Тест: ").append(testName).append("\n");
        sb.append("────────────────────────────────────────────────────────────\n\n");

        if (result.getInterpretations() != null && !result.getInterpretations().isEmpty()) {
            for (Map.Entry<String, String> entry : result.getInterpretations().entrySet()) {
                sb.append("📊  ").append(entry.getKey()).append("\n");
                sb.append("    ").append("─".repeat(entry.getKey().length() + 2)).append("\n");
                sb.append("    ").append(entry.getValue() != null ? entry.getValue() : "Нет интерпретации").append("\n\n");
            }
        } else {
            sb.append("Нет данных для отображения.\n");
            sb.append("Возможно, не на все вопросы даны ответы.\n");
        }

        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);

        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // ── Кнопки ────────────────────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));

        JButton exportButton = new JButton("📊 Экспорт в Excel");
        exportButton.addActionListener(e -> exportToExcel());

        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        String defaultName = "preview_" + testName.replaceAll("[^a-zA-Zа-яА-Я0-9_]", "_")
                + "_" + java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".xls";
        fileChooser.setSelectedFile(new java.io.File(defaultName));
        fileChooser.setDialogTitle("Сохранить результаты пробного запуска...");

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String rawPath = fileChooser.getSelectedFile().getAbsolutePath();
        final String filePath = rawPath.endsWith(".xls") ? rawPath : rawPath + ".xls";

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return excelService.exportPreviewResultToExcel(result, testName, filePath);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(PreviewResultDialog.this,
                                "Отчёт сохранён:\n" + filePath,
                                "Экспорт завершён", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(PreviewResultDialog.this,
                                "Ошибка при сохранении файла!\nПроверьте права доступа и путь.",
                                "Ошибка экспорта", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(PreviewResultDialog.this,
                            "Ошибка: " + e.getMessage(),
                            "Ошибка экспорта", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
