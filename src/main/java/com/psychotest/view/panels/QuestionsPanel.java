package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TestConstructorController;
import main.java.com.psychotest.model.AnswerOption;
import main.java.com.psychotest.model.Parameter;
import main.java.com.psychotest.model.Question;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionsPanel extends JPanel {
    private TestConstructorController controller;
    private JTable questionsTable;
    private DefaultTableModel tableModel;
    private JTextArea answersArea;

    public QuestionsPanel(TestConstructorController controller) {
        this.controller = controller;
        initComponents();
        loadData();

        // Подписываемся на изменения модели
        controller.addListener(() -> loadData());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель с информацией
        JLabel infoLabel = new JLabel("Вопросы теста. У каждого вопроса должно быть 2-10 вариантов ответа.");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoLabel.setForeground(Color.GRAY);
        add(infoLabel, BorderLayout.NORTH);

        // Центральная панель - таблица вопросов
        String[] columns = {"№", "Вопрос", "Ответов"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        questionsTable = new JTable(tableModel);
        questionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showAnswers();
            }
        });

        JScrollPane tableScroll = new JScrollPane(questionsTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Список вопросов"));

        // Правая панель для отображения ответов
        answersArea = new JTextArea();
        answersArea.setEditable(false);
        answersArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane answersScroll = new JScrollPane(answersArea);
        answersScroll.setBorder(BorderFactory.createTitledBorder("Варианты ответов и их влияние"));
        answersScroll.setPreferredSize(new Dimension(350, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, answersScroll);
        splitPane.setDividerLocation(450);
        add(splitPane, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("➕ Добавить вопрос");
        addButton.addActionListener(e -> addQuestion());

        JButton editButton = new JButton("✏️ Редактировать");
        editButton.addActionListener(e -> editQuestion());

        JButton removeButton = new JButton("➖ Удалить вопрос");
        removeButton.addActionListener(e -> removeQuestion());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Question> questions = controller.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            tableModel.addRow(new Object[]{
                    i + 1,
                    q.getText(),
                    q.getAnswerOptions().size()
            });
        }
    }

    private void showAnswers() {
        int row = questionsTable.getSelectedRow();
        List<Question> questions = controller.getQuestions();
        if (row >= 0 && row < questions.size()) {
            Question question = questions.get(row);
            List<Parameter> params = controller.getParameters();

            StringBuilder sb = new StringBuilder();
            sb.append("Вопрос: ").append(question.getText()).append("\n\n");
            sb.append("Варианты ответов:\n");
            sb.append("─".repeat(50)).append("\n");

            List<AnswerOption> options = question.getAnswerOptions();
            for (int i = 0; i < options.size(); i++) {
                AnswerOption opt = options.get(i);
                sb.append(i + 1).append(". ").append(opt.getText()).append("\n");

                if (!opt.getParameterImpacts().isEmpty()) {
                    sb.append("   Влияние на параметры:\n");
                    for (var entry : opt.getParameterImpacts().entrySet()) {
                        int paramIndex = entry.getKey();
                        int delta = entry.getValue();
                        String paramName = (paramIndex >= 0 && paramIndex < params.size())
                                ? params.get(paramIndex).getName() : "?";
                        sb.append("     • ").append(paramName).append(": ");
                        sb.append(delta > 0 ? "+" : "").append(delta).append("\n");
                    }
                }
                sb.append("\n");
            }
            answersArea.setText(sb.toString());
        } else {
            answersArea.setText("Выберите вопрос для просмотра ответов");
        }
    }

    private void addQuestion() {
        JTextField questionField = new JTextField(30);
        JSpinner answerCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 10, 1));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Текст вопроса:"), gbc);
        gbc.gridx = 1;
        panel.add(questionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Количество ответов (2-10):"), gbc);
        gbc.gridx = 1;
        panel.add(answerCountSpinner, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Новый вопрос", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String questionText = questionField.getText().trim();
            if (questionText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите текст вопроса!");
                return;
            }

            int numAnswers = (int) answerCountSpinner.getValue();
            List<TestConstructorController.AnswerOptionData> answers = new ArrayList<>();

            // Добавляем ответы
            for (int i = 0; i < numAnswers; i++) {
                TestConstructorController.AnswerOptionData answerData = addAnswerToQuestion(i + 1);
                if (answerData != null) {
                    answers.add(answerData);
                } else {
                    return; // пользователь отменил добавление ответа
                }
            }

            if (!answers.isEmpty()) {
                controller.addQuestion(questionText, answers);
            }
        }
    }

    private TestConstructorController.AnswerOptionData addAnswerToQuestion(int answerNumber) {
        JTextField answerField = new JTextField(30);

        // Панель для влияния на параметры
        List<Parameter> params = controller.getParameters();
        JPanel impactsPanel = new JPanel(new GridLayout(params.size(), 2, 5, 5));
        Map<Integer, JSpinner> impactSpinners = new HashMap<>();

        for (int i = 0; i < params.size(); i++) {
            Parameter param = params.get(i);
            impactsPanel.add(new JLabel(param.getName() + ":"));
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, -10, 10, 1));
            impactsPanel.add(spinner);
            impactSpinners.put(i, spinner);
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Вариант ответа #" + answerNumber + ":"), BorderLayout.NORTH);
        panel.add(answerField, BorderLayout.CENTER);

        if (!params.isEmpty()) {
            JPanel impactWrapper = new JPanel(new BorderLayout());
            impactWrapper.setBorder(BorderFactory.createTitledBorder("Влияние на параметры"));
            impactWrapper.add(impactsPanel, BorderLayout.CENTER);
            panel.add(impactWrapper, BorderLayout.SOUTH);
        }

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Добавление ответа", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String answerText = answerField.getText().trim();
            if (answerText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите текст ответа!");
                return null;
            }

            TestConstructorController.AnswerOptionData data =
                    new TestConstructorController.AnswerOptionData(answerText, answerNumber - 1);

            // Добавляем влияния
            for (var entry : impactSpinners.entrySet()) {
                int paramIndex = entry.getKey();
                int delta = (int) entry.getValue().getValue();
                if (delta != 0) {
                    data.addImpact(paramIndex, delta);
                }
            }
            return data;
        }
        return null;
    }

    private void editQuestion() {
        int row = questionsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите вопрос для редактирования!");
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Редактирование вопроса будет в следующей версии.\n" +
                        "Пока что удалите и создайте заново.",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private void removeQuestion() {
        int row = questionsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите вопрос для удаления!");
            return;
        }

        String questionText = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить вопрос '" + questionText + "'?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.removeQuestion(row);
        }
    }
}