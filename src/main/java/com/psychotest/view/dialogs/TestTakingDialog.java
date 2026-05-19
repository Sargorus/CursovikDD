package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TakerController;
import main.java.com.psychotest.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

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

    public TestTakingDialog(Window parent, TakerController controller,
                            int sessionId, int testId, String testName) {
        super(parent, "Прохождение теста: " + testName, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.sessionId = sessionId;
        this.testId = testId;
        this.testName = testName;

        // Загружаем вопросы теста
        Test test = controller.getFullTest(testId);
        if (test != null) {
            this.questions = test.getQuestionBank();
        } else {
            this.questions = List.of();
        }

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
                // Восстанавливаем сохранённый ответ, если есть
                // TODO: загрузить сохранённый ответ из БД
            } else {
                answerButtons[i].setVisible(false);
            }
        }

        answerGroup.clearSelection();

        // Обновляем прогресс
        progressBar.setValue(currentQuestionIndex);
        progressLabel.setText("Вопрос " + (currentQuestionIndex + 1) + " из " + questions.size());

        // Обновляем кнопки
        prevButton.setEnabled(currentQuestionIndex > 0);
        nextButton.setEnabled(true);
        finishButton.setEnabled(true);
    }

    private void saveCurrentAnswer() {
        Question question = questions.get(currentQuestionIndex);
        for (int i = 0; i < question.getAnswerOptions().size(); i++) {
            if (answerButtons[i].isSelected()) {
                int answerOptionId = question.getAnswerOptions().get(i).getId();
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
                        if (result != null) {
                            showResultDialog(result);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(TestTakingDialog.this,
                                    "Ошибка при расчёте результатов!",
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
                        "Прогресс будет сохранён, но тест не будет засчитан.",
                "Прерывание теста",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.abandonTest(sessionId);
            dispose();
        }
    }

    private void showResultDialog(TestResult result) {
        // TODO: показать диалог с результатами
        JOptionPane.showMessageDialog(this,
                "Тест завершён!\n\n" +
                        "Ваши результаты будут отображены в истории.",
                "Результаты",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}