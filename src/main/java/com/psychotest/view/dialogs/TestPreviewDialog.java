package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Диалог пробного прохождения теста преподавателем.
 * Работает так же, как TestTakingDialog, но НЕ создаёт сессию в БД
 * и НЕ сохраняет ответы — всё хранится в памяти.
 * По завершении показывает результаты и предлагает экспорт в Excel.
 */
public class TestPreviewDialog extends JDialog {

    private TeacherController controller;
    private int testId;
    private String testName;
    private List<Question> questions;
    private int currentQuestionIndex = 0;

    private ButtonGroup answerGroup;
    private JRadioButton[] answerButtons;
    private JLabel questionLabel;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JButton nextButton;
    private JButton prevButton;

    /** In-memory: questionId → answerOptionId */
    private final Map<Integer, Integer> userAnswers = new HashMap<>();

    public TestPreviewDialog(Window parent, TeacherController controller, int testId, String testName) {
        super(parent, "🧪 Пробный запуск: " + testName, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.testId = testId;
        this.testName = testName;

        Test test = controller.getFullTestForPreview(testId);
        if (test != null && test.getQuestionBank() != null && !test.getQuestionBank().isEmpty()) {
            this.questions = test.getQuestionBank();
        } else {
            this.questions = List.of();
            JOptionPane.showMessageDialog(parent,
                    "Тест не содержит вопросов!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initComponents();
        loadQuestion();
        setSize(620, 520);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // ── Предупреждение о пробном режиме ──────────────────────────────────
        JLabel warningLabel = new JLabel(
                "⚠ Пробный режим — результаты не сохраняются в базу данных",
                SwingConstants.CENTER);
        warningLabel.setForeground(new Color(150, 75, 0));
        warningLabel.setFont(warningLabel.getFont().deriveFont(Font.BOLD, 11f));
        warningLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 2, 10));

        // ── Прогресс ─────────────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));

        progressLabel = new JLabel("Вопрос 1 из " + questions.size());
        topPanel.add(progressLabel, BorderLayout.WEST);

        progressBar = new JProgressBar(0, questions.size());
        progressBar.setStringPainted(true);
        topPanel.add(progressBar, BorderLayout.CENTER);

        // Оборачиваем warningLabel + topPanel в NORTH
        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(warningLabel, BorderLayout.NORTH);
        northWrapper.add(topPanel, BorderLayout.SOUTH);
        add(northWrapper, BorderLayout.NORTH);

        // ── Вопрос + варианты ответов ─────────────────────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        centerPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel answersPanel = new JPanel();
        answersPanel.setLayout(new BoxLayout(answersPanel, BoxLayout.Y_AXIS));
        answerGroup = new ButtonGroup();
        answerButtons = new JRadioButton[10]; // максимум 10 ответов
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i] = new JRadioButton();
            answerButtons[i].setFont(new Font("Arial", Font.PLAIN, 12));
            answerGroup.add(answerButtons[i]);
            answersPanel.add(answerButtons[i]);
            answersPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(answersPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Выберите вариант ответа"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ── Кнопки навигации ─────────────────────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout());

        prevButton = new JButton("◀ Назад");
        prevButton.addActionListener(e -> previousQuestion());

        nextButton = new JButton("Далее ▶");
        nextButton.addActionListener(e -> nextQuestion());

        JButton cancelBtn = new JButton("Отмена");
        cancelBtn.addActionListener(e -> dispose());

        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(cancelBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishTest();
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        questionLabel.setText("<html><b>Вопрос " + (currentQuestionIndex + 1) + ":</b><br>"
                + escapeHtml(question.getText()) + "</html>");

        List<AnswerOption> options = question.getAnswerOptions();
        for (int i = 0; i < answerButtons.length; i++) {
            if (i < options.size()) {
                answerButtons[i].setText(options.get(i).getText());
                answerButtons[i].setVisible(true);
            } else {
                answerButtons[i].setVisible(false);
            }
        }

        // Восстанавливаем ранее выбранный ответ (если есть)
        Integer savedId = userAnswers.get(question.getId());
        if (savedId != null) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).getId() == savedId) {
                    answerButtons[i].setSelected(true);
                    break;
                }
            }
        } else {
            answerGroup.clearSelection();
        }

        progressBar.setValue(currentQuestionIndex);
        progressLabel.setText("Вопрос " + (currentQuestionIndex + 1) + " из " + questions.size());
        prevButton.setEnabled(currentQuestionIndex > 0);

        boolean isLast = currentQuestionIndex == questions.size() - 1;
        nextButton.setText(isLast ? "✅ Завершить" : "Далее ▶");
    }

    private void saveCurrentAnswer() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) return;
        Question question = questions.get(currentQuestionIndex);
        List<AnswerOption> options = question.getAnswerOptions();
        for (int i = 0; i < options.size(); i++) {
            if (answerButtons[i].isSelected()) {
                userAnswers.put(question.getId(), options.get(i).getId());
                break;
            }
        }
    }

    private void nextQuestion() {
        saveCurrentAnswer();
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            loadQuestion();
        } else {
            finishTest();
        }
    }

    private void previousQuestion() {
        if (currentQuestionIndex > 0) {
            saveCurrentAnswer();
            currentQuestionIndex--;
            loadQuestion();
        }
    }

    private void finishTest() {
        saveCurrentAnswer();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Завершить пробный запуск теста?\n\nРезультаты будут показаны, но не сохранены в базу данных.",
                "Завершение пробного запуска",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        final Map<Integer, Integer> answersCopy = new HashMap<>(userAnswers);

        new SwingWorker<TestResult, Void>() {
            @Override
            protected TestResult doInBackground() {
                return controller.calculatePreviewResult(testId, testName, answersCopy);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    TestResult result = get();
                    Window ancestor = SwingUtilities.getWindowAncestor(TestPreviewDialog.this);
                    dispose();

                    if (result != null && result.isCompleted()) {
                        new PreviewResultDialog(ancestor, result, testName).setVisible(true);
                    } else {
                        String err = (result != null) ? result.getErrorMessage() : "Неизвестная ошибка";
                        JOptionPane.showMessageDialog(ancestor,
                                "Ошибка при расчёте результатов: " + err,
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(null,
                            "Ошибка: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
