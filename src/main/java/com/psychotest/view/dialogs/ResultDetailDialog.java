package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.service.ResultService;
import javax.swing.*;
import java.awt.*;

public class ResultDetailDialog extends JDialog {
    private ResultService.SessionDetail detail;
    private String userName;
    private String testName;

    public ResultDetailDialog(Window parent, ResultService.SessionDetail detail,
                              String userName, String testName) {
        super(parent, "Результаты теста: " + testName + " - " + userName,
                ModalityType.APPLICATION_MODAL);
        this.detail = detail;
        this.userName = userName;
        this.testName = testName;
        initComponents();
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Создаём вкладки
        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка с параметрами (результатами)
        JPanel paramsPanel = createParametersPanel();
        tabbedPane.addTab("📊 Результаты по шкалам", paramsPanel);

        // Вкладка с ответами на вопросы
        JPanel answersPanel = createAnswersPanel();
        tabbedPane.addTab("❓ Ответы на вопросы", answersPanel);

        // Вкладка с интерпретацией
        JPanel interpretationPanel = createInterpretationPanel();
        tabbedPane.addTab("📝 Интерпретация", interpretationPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout());

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
        String defaultFileName = "result_" + userName + "_" +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));
        fileChooser.setDialogTitle("Сохранить отчёт как...");

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (!filePath.endsWith(".xlsx")) {
            filePath += ".xlsx";
        }

        // TODO: Вызвать экспорт через контроллер
        JOptionPane.showMessageDialog(this,
                "Экспорт детального отчёта будет реализован в следующей версии.\n" +
                        "Пока что используйте экспорт из главного окна.",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createParametersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (detail.getParameterResults() == null || detail.getParameterResults().isEmpty()) {
            JLabel emptyLabel = new JLabel("Нет данных о параметрах", SwingConstants.CENTER);
            panel.add(emptyLabel, BorderLayout.CENTER);
            return panel;
        }

        // Создаём таблицу для параметров
        String[] columns = {"Параметр", "Тип шкалы", "Сырой балл", "Итоговый балл", "Код", "Интерпретация"};
        Object[][] data = new Object[detail.getParameterResults().size()][6];

        int i = 0;
        for (var entry : detail.getParameterResults().entrySet()) {
            ResultService.ParameterResult pr = entry.getValue();
            data[i][0] = pr.getParamName();
            data[i][1] = pr.getScaleType().equals("BINARY") ? "Бинарная" : "Диапазонная";
            data[i][2] = pr.getRawScore();
            data[i][3] = pr.getScaledScore();
            data[i][4] = pr.getInterpretedCode() != null ? pr.getInterpretedCode() : "";
            data[i][5] = truncate(pr.getInterpretationText(), 80);
            i++;
        }

        JTable table = new JTable(data, columns);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Добавляем информационную панель
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Сырой балл - сумма влияний ответов. Итоговый балл - масштабированный результат."));
        infoPanel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(infoPanel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createAnswersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (detail.getAnswers() == null || detail.getAnswers().isEmpty()) {
            JLabel emptyLabel = new JLabel("Нет ответов на вопросы", SwingConstants.CENTER);
            panel.add(emptyLabel, BorderLayout.CENTER);
            return panel;
        }

        // Создаём панель со списком вопросов и ответов
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        int questionNum = 1;
        for (ResultService.AnswerDetail answer : detail.getAnswers()) {
            JPanel questionPanel = createQuestionPanel(questionNum++, answer.getQuestionText(), answer.getAnswerText());
            listPanel.add(questionPanel);
            listPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createQuestionPanel(int number, String questionText, String answerText) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(new Color(250, 250, 250));

        // Номер и текст вопроса
        JLabel questionLabel = new JLabel("<html><b>Вопрос " + number + ":</b> " + escapeHtml(questionText) + "</html>");
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Ответ
        JLabel answerLabel = new JLabel("<html><b>Ответ:</b> " + escapeHtml(answerText) + "</html>");
        answerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        answerLabel.setForeground(new Color(0, 100, 0));

        panel.add(questionLabel, BorderLayout.NORTH);
        panel.add(answerLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInterpretationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (detail.getParameterResults() == null || detail.getParameterResults().isEmpty()) {
            JLabel emptyLabel = new JLabel("Нет интерпретаций", SwingConstants.CENTER);
            panel.add(emptyLabel, BorderLayout.CENTER);
            return panel;
        }

        // Создаём текстовую область для полной интерпретации
        JTextArea interpretationArea = new JTextArea();
        interpretationArea.setEditable(false);
        interpretationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        interpretationArea.setLineWrap(true);
        interpretationArea.setWrapStyleWord(true);

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                    РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ                        \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n\n");
        sb.append("Тест: ").append(testName).append("\n");
        sb.append("Тестируемый: ").append(userName).append("\n");
        sb.append("───────────────────────────────────────────────────────────────────\n\n");

        for (var entry : detail.getParameterResults().entrySet()) {
            ResultService.ParameterResult pr = entry.getValue();
            sb.append("📊 ").append(pr.getParamName()).append("\n");
            sb.append("   ").append("─".repeat(pr.getParamName().length() + 2)).append("\n");
            sb.append("   Сырой балл: ").append(pr.getRawScore()).append("\n");
            sb.append("   Итоговый балл: ").append(pr.getScaledScore()).append("\n");
            if (pr.getInterpretedCode() != null && !pr.getInterpretedCode().isEmpty()) {
                sb.append("   Код: ").append(pr.getInterpretedCode()).append("\n");
            }
            sb.append("   ").append(pr.getInterpretationText()).append("\n");
            sb.append("\n");
        }

        interpretationArea.setText(sb.toString());
        interpretationArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(interpretationArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
