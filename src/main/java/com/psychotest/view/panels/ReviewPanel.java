package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TestConstructorController;
import main.java.com.psychotest.model.AnswerOption;
import main.java.com.psychotest.model.Parameter;
import main.java.com.psychotest.model.ParameterInterpretation;
import main.java.com.psychotest.model.Question;
import main.java.com.psychotest.service.TestValidationService;
import javax.swing.*;
import java.awt.*;

public class ReviewPanel extends JPanel {
    private TestConstructorController controller;
    private JTextArea reviewArea;
    private JButton refreshButton;

    public ReviewPanel(TestConstructorController controller) {
        this.controller = controller;
        initComponents();
        updateReview();

        // Подписываемся на изменения модели
        controller.addListener(() -> updateReview());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        reviewArea = new JTextArea();
        reviewArea.setEditable(false);
        reviewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reviewArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        refreshButton = new JButton("🔄 Обновить предпросмотр");
        refreshButton.addActionListener(e -> updateReview());
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void updateReview() {
        StringBuilder sb = new StringBuilder();

        // Заголовок
        sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                     ПРЕДПРОСМОТР ТЕСТА                           ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");

        // Шаг 1: Информация о тесте
        sb.append("📋 ИНФОРМАЦИЯ О ТЕСТЕ\n");
        sb.append("─".repeat(50)).append("\n");
        sb.append("Название: ").append(controller.getTestName().isEmpty() ? "(не указано)" : controller.getTestName()).append("\n");
        sb.append("Описание: ").append(controller.getTestDescription().isEmpty() ? "(не указано)" : controller.getTestDescription()).append("\n");
        int qps = controller.getQuestionsPerSession();
        sb.append("Вопросов в сессии: ").append(qps > 0 ? qps + " (случайная выборка)" : "Все вопросы").append("\n\n");

        // Шаг 2: Параметры
        sb.append("📊 ПАРАМЕТРЫ (шкалы)\n");
        sb.append("─".repeat(50)).append("\n");
        if (controller.getParameters().isEmpty()) {
            sb.append("⚠️ Нет добавленных параметров!\n");
        } else {
            for (Parameter param : controller.getParameters()) {
                sb.append("• ").append(param.getName());
                sb.append(" (").append(param.getScaleType().equals("BINARY") ? "Бинарная" : "Диапазонная").append(")\n");
                for (ParameterInterpretation interp : param.getInterpretations()) {
                    if (param.getScaleType().equals("BINARY")) {
                        sb.append("    - ").append(interp.getBinaryValue()).append(": ");
                        sb.append(truncate(interp.getInterpretationText(), 60)).append("\n");
                    } else {
                        sb.append("    - ").append(interp.getRangeStart()).append("-").append(interp.getRangeEnd());
                        sb.append(": ").append(truncate(interp.getInterpretationText(), 50)).append("\n");
                    }
                }
                sb.append("\n");
            }
        }

        // Шаг 3: Вопросы
        sb.append("❓ ВОПРОСЫ\n");
        sb.append("─".repeat(50)).append("\n");
        if (controller.getQuestions().isEmpty()) {
            sb.append("⚠️ Нет добавленных вопросов!\n");
        } else {
            for (int i = 0; i < controller.getQuestions().size(); i++) {
                Question q = controller.getQuestions().get(i);
                sb.append(i + 1).append(". ").append(q.getText()).append("\n");

                for (int j = 0; j < q.getAnswerOptions().size(); j++) {
                    AnswerOption opt = q.getAnswerOptions().get(j);
                    sb.append("   ").append((char)('А' + j)).append(") ").append(opt.getText());

                    if (!opt.getParameterImpacts().isEmpty()) {
                        sb.append(" → ");
                        boolean first = true;
                        for (var entry : opt.getParameterImpacts().entrySet()) {
                            if (!first) sb.append(", ");
                            int paramIndex = entry.getKey();
                            int delta = entry.getValue();
                            String paramName = paramIndex < controller.getParameters().size() ?
                                    controller.getParameters().get(paramIndex).getName() : "?";
                            sb.append(paramName).append(": ");
                            sb.append(delta > 0 ? "+" : "").append(delta);
                            first = false;
                        }
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }
        }

        // Валидация через контроллер
        TestValidationService.ValidationResult validation = controller.validateTest();

        sb.append("\n📈 СТАТИСТИКА И ВАЛИДАЦИЯ\n");
        sb.append("─".repeat(50)).append("\n");

        if (validation.isValid()) {
            sb.append("✅ ТЕСТ ГОТОВ К СОХРАНЕНИЮ!\n");
        } else {
            sb.append("❌ ОШИБКИ:\n• ").append(validation.getErrorMessage()).append("\n");
        }

        if (!validation.getWarningMessage().isEmpty()) {
            sb.append("\n⚠️ ПРЕДУПРЕЖДЕНИЯ:\n• ").append(validation.getWarningMessage()).append("\n");
        }

        if (validation.getRangeInfo() != null) {
            sb.append(validation.getRangeInfo().toString());
        }

        reviewArea.setText(sb.toString());
        reviewArea.setCaretPosition(0);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
