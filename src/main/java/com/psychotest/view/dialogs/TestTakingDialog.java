package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TakerController;
import main.java.com.psychotest.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class TestTakingDialog extends JDialog {
    private TakerController controller;
    private int sessionId;
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
    private JButton finishButton;

    // Хранилище сохранённых ответов (questionId -> answerOptionId)
    private Map<Integer, Integer> savedAnswers;

    public TestTakingDialog(Window parent, TakerController controller,
                            int sessionId, int testId, String testName) {
        super(parent, "Прохождение теста: " + testName, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.sessionId = sessionId;
        this.testId = testId;
        this.testName = testName;
        this.savedAnswers = new HashMap<>();

        // Загружаем вопросы теста
        Test test = controller.getFullTest(testId);

        if (test != null && test.getQuestionBank() != null && !test.getQuestionBank().isEmpty()) {
            this.questions = test.getQuestionBank();
        } else {
            this.questions = List.of();
            JOptionPane.showMessageDialog(parent,
                    "Тест не содержит вопросов!\nПожалуйста, сообщите преподавателю.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Загружаем ранее сохранённые ответы из БД
        loadSavedAnswers();

        initComponents();
        loadQuestion();
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmAbandon();
            }
        });
    }

    /**
     * Загружает ранее сохранённые ответы из базы данных через контроллер
     */
    private void loadSavedAnswers() {
        savedAnswers = controller.getSavedAnswers(sessionId);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель с прогрессом
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        progressLabel = new JLabel("Вопрос 1 из " + questions.size());
        topPanel.add(progressLabel, BorderLayout.WEST);

        progressBar = new JProgressBar(0, questions.size());
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        topPanel.add(progressBar, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Центральная панель - вопрос и ответы
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

        // Нижняя панель с кнопками
        JPanel bottomPanel = new JPanel(new FlowLayout());

        prevButton = new JButton("◀ Назад");
        prevButton.addActionListener(e -> previousQuestion());

        nextButton = new JButton("Далее ▶");
        nextButton.addActionListener(e -> nextQuestion());

        finishButton = new JButton("✅ Завершить");
        finishButton.addActionListener(e -> finishTest());

        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishTest();
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        questionLabel.setText("<html><b>Вопрос " + (currentQuestionIndex + 1) + ":</b><br>" +
                escapeHtml(question.getText()) + "</html>");

        // Очищаем и заполняем ответы
        List<AnswerOption> options = question.getAnswerOptions();
        for (int i = 0; i < answerButtons.length; i++) {
            if (i < options.size()) {
                answerButtons[i].setText(options.get(i).getText());
                answerButtons[i].setVisible(true);
            } else {
                answerButtons[i].setVisible(false);
            }
        }

        // Восстанавливаем сохранённый ответ, если есть
        Integer savedAnswerId = savedAnswers.get(question.getId());
        if (savedAnswerId != null) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).getId() == savedAnswerId) {
                    answerButtons[i].setSelected(true);
                    break;
                }
            }
        } else {
            answerGroup.clearSelection();
        }

        // Обновляем прогресс
        progressBar.setValue(currentQuestionIndex);
        progressLabel.setText("Вопрос " + (currentQuestionIndex + 1) + " из " + questions.size());

        // Обновляем кнопки
        prevButton.setEnabled(currentQuestionIndex > 0);

        // На последнем вопросе меняем текст кнопки "Далее" на "Завершить"
        // и скрываем отдельную кнопку finishButton, чтобы не было двух одинаковых
        boolean isLastQuestion = currentQuestionIndex == questions.size() - 1;
        if (isLastQuestion) {
            nextButton.setText("✅ Завершить");
        } else {
            nextButton.setText("Далее ▶");
        }
        finishButton.setVisible(!isLastQuestion);
    }

    private void saveCurrentAnswer() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) {
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        List<AnswerOption> options = question.getAnswerOptions();

        for (int i = 0; i < options.size(); i++) {
            if (answerButtons[i].isSelected()) {
                int answerOptionId = options.get(i).getId();
                savedAnswers.put(question.getId(), answerOptionId);
                controller.saveAnswer(sessionId, question.getId(), answerOptionId);
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
        // Сохраняем ответ на текущий вопрос, если он не был сохранён
        saveCurrentAnswer();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите завершить тест?\n" +
                        "После завершения вы не сможете изменить ответы.",
                "Завершение теста",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Завершаем тест в отдельном потоке
            SwingWorker<TestResult, Void> worker = new SwingWorker<TestResult, Void>() {
                @Override
                protected TestResult doInBackground() {
                    return controller.calculateAndCompleteTest(sessionId);
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        TestResult result = get();
                        if (result != null && result.isCompleted()) {
                            JOptionPane.showMessageDialog(TestTakingDialog.this,
                                    "Тест завершён!\nСпасибо за прохождение.",
                                    "Тест завершён", JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        } else {
                            String errorMsg = result != null ? result.getErrorMessage() : "Неизвестная ошибка";
                            JOptionPane.showMessageDialog(TestTakingDialog.this,
                                    "Ошибка при расчёте результатов: " + errorMsg,
                                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                            dispose();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(TestTakingDialog.this,
                                "Ошибка: " + e.getMessage(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                        dispose();
                    }
                }
            };
            worker.execute();
        }
    }

    private void confirmAbandon() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите прервать тест?\n" +
                        "Тест не будет засчитан, результат не сохранится.\n" +
                        "При следующем запуске тест начнётся заново.",
                "Прерывание теста",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Сохраняем текущий ответ перед выходом
            saveCurrentAnswer();
            controller.abandonTest(sessionId);
            dispose();
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}