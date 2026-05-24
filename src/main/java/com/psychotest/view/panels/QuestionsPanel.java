package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TestConstructorController;
import main.java.com.psychotest.model.AnswerOption;
import main.java.com.psychotest.model.Parameter;
import main.java.com.psychotest.model.Question;
import main.java.com.psychotest.view.dialogs.QuestionEditorDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editQuestion());

        JButton removeButton = new JButton("➖ Удалить вопрос");
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> removeQuestion());

        questionsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = questionsTable.getSelectedRow() != -1;
            editButton.setEnabled(selected);
            removeButton.setEnabled(selected);
        });

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
        QuestionEditorDialog dialog = new QuestionEditorDialog(
                SwingUtilities.getWindowAncestor(this),
                controller.getParameters());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            controller.addQuestion(dialog.getQuestionText(), dialog.getAnswers());
        }
    }

    private void editQuestion() {
        int row = questionsTable.getSelectedRow();
        if (row == -1) return;

        QuestionEditorDialog dialog = new QuestionEditorDialog(
                SwingUtilities.getWindowAncestor(this),
                controller.getParameters(),
                controller.getQuestion(row));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            controller.updateQuestion(row, dialog.getQuestionText(), dialog.getAnswers());
        }
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