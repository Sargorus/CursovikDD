package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TestConstructorController;
import main.java.com.psychotest.model.AnswerOption;
import main.java.com.psychotest.model.Parameter;
import main.java.com.psychotest.model.Question;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Единый диалог добавления/редактирования вопроса.
 * Вопрос, все его ответы и влияния на параметры редактируются в одном окне.
 */
public class QuestionEditorDialog extends JDialog {

    private static final int MIN_ANSWERS = 2;
    private static final int MAX_ANSWERS = 10;

    private final List<Parameter> params;

    private JTextArea questionField;
    private JPanel answersContainer;   // содержит строки ответов (без заголовка)
    private JPanel answersWrapper;     // заголовок + answersContainer

    private final List<JTextField> answerTextFields = new ArrayList<>();
    private final List<List<JSpinner>> impactSpinners = new ArrayList<>();

    // Результат: null — пользователь отменил
    private String savedQuestionText;
    private List<TestConstructorController.AnswerOptionData> savedAnswers;

    /** Конструктор для добавления нового вопроса */
    public QuestionEditorDialog(Window parent, List<Parameter> params) {
        this(parent, params, null);
    }

    /** Конструктор для редактирования существующего вопроса */
    public QuestionEditorDialog(Window parent, List<Parameter> params, Question existing) {
        super(parent,
              existing == null ? "Добавление вопроса" : "Редактирование вопроса",
              ModalityType.APPLICATION_MODAL);
        this.params = params;
        buildUI(existing);
        setSize(Math.max(650, 300 + params.size() * 80), 520);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Построение интерфейса
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUI(Question existing) {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildQuestionTextPanel(existing), BorderLayout.NORTH);
        root.add(buildAnswersSectionPanel(existing), BorderLayout.CENTER);
        root.add(buildButtonBar(), BorderLayout.SOUTH);
    }

    /** Поле ввода текста вопроса */
    private JPanel buildQuestionTextPanel(Question existing) {
        JPanel panel = new JPanel(new BorderLayout(5, 4));
        panel.add(new JLabel("Текст вопроса:"), BorderLayout.NORTH);

        questionField = new JTextArea(3, 40);
        questionField.setLineWrap(true);
        questionField.setWrapStyleWord(true);
        if (existing != null) questionField.setText(existing.getText());

        JScrollPane sp = new JScrollPane(questionField);
        sp.setMinimumSize(new Dimension(0, 70));
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    /** Секция "Варианты ответов" с заголовком и динамическими строками */
    private JPanel buildAnswersSectionPanel(Question existing) {
        // Заголовочная строка
        JPanel header = buildHeaderRow();

        // Контейнер строк ответов
        answersContainer = new JPanel();
        answersContainer.setLayout(new BoxLayout(answersContainer, BoxLayout.Y_AXIS));

        // Наполняем начальными ответами
        int initCount = (existing != null) ? existing.getAnswerOptions().size() : 2;
        for (int i = 0; i < initCount; i++) {
            AnswerOption opt = (existing != null && i < existing.getAnswerOptions().size())
                    ? existing.getAnswerOptions().get(i) : null;
            appendAnswerRow(opt);
        }

        // Обёртка: заголовок + строки в скролле
        answersWrapper = new JPanel(new BorderLayout());
        answersWrapper.add(header, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(answersContainer);
        scroll.setBorder(null);
        answersWrapper.add(scroll, BorderLayout.CENTER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createTitledBorder("Варианты ответов"));
        outer.add(answersWrapper, BorderLayout.CENTER);

        // Кнопки добавить/удалить строку ответа
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton addRowBtn = new JButton("➕ Добавить ответ");
        JButton delRowBtn = new JButton("➖ Удалить последний");
        addRowBtn.addActionListener(e -> {
            if (answerTextFields.size() < MAX_ANSWERS) {
                appendAnswerRow(null);
                refreshAnswersPanel();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Максимальное количество ответов: " + MAX_ANSWERS,
                        "Ограничение", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        delRowBtn.addActionListener(e -> {
            if (answerTextFields.size() > MIN_ANSWERS) {
                removeLastAnswerRow();
                refreshAnswersPanel();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Минимальное количество ответов: " + MIN_ANSWERS,
                        "Ограничение", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnRow.add(addRowBtn);
        btnRow.add(delRowBtn);
        outer.add(btnRow, BorderLayout.SOUTH);

        return outer;
    }

    /** Заголовочная строка таблицы ответов */
    private JPanel buildHeaderRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(new Color(220, 230, 245));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 6, 3, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.weightx = 0;
        row.add(bold("#"), g);

        g.gridx = 1; g.weightx = 1.0;
        row.add(bold("Текст ответа"), g);

        for (int i = 0; i < params.size(); i++) {
            g.gridx = 2 + i; g.weightx = 0;
            JLabel lbl = bold(params.get(i).getName());
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setPreferredSize(new Dimension(70, 20));
            row.add(lbl, g);
        }
        return row;
    }

    /** Добавляет одну строку ответа (с предзаполнением из existing, если не null) */
    private void appendAnswerRow(AnswerOption existing) {
        int rowNum = answerTextFields.size() + 1;

        JPanel row = new JPanel(new GridBagLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setMinimumSize(new Dimension(0, 36));
        // Чередующийся фон для читаемости
        row.setBackground(rowNum % 2 == 0 ? new Color(248, 248, 248) : Color.WHITE);
        row.setOpaque(true);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 6, 2, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Номер строки
        g.gridx = 0; g.weightx = 0;
        JLabel numLbl = new JLabel(rowNum + ".");
        numLbl.setPreferredSize(new Dimension(22, 22));
        row.add(numLbl, g);

        // Поле текста ответа
        JTextField tf = new JTextField(existing != null ? existing.getText() : "", 20);
        g.gridx = 1; g.weightx = 1.0;
        row.add(tf, g);
        answerTextFields.add(tf);

        // Спиннеры влияния на параметры
        List<JSpinner> rowSpinners = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            int prefill = 0;
            if (existing != null) {
                Integer v = existing.getParameterImpacts().get(i);
                if (v != null) prefill = v;
            }
            JSpinner sp = new JSpinner(new SpinnerNumberModel(Math.max(0, prefill), 0, 9999, 1));
            sp.setPreferredSize(new Dimension(65, 26));
            g.gridx = 2 + i; g.weightx = 0;
            row.add(sp, g);
            rowSpinners.add(sp);
        }
        impactSpinners.add(rowSpinners);

        answersContainer.add(row);
    }

    /** Удаляет последнюю строку ответа */
    private void removeLastAnswerRow() {
        int last = answerTextFields.size() - 1;
        answerTextFields.remove(last);
        impactSpinners.remove(last);
        answersContainer.remove(answersContainer.getComponentCount() - 1);
    }

    /** Перерисовывает панель ответов после добавления/удаления строки */
    private void refreshAnswersPanel() {
        answersContainer.revalidate();
        answersContainer.repaint();
    }

    /** Нижняя панель с кнопками «Сохранить» и «Отмена» */
    private JPanel buildButtonBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton saveBtn = new JButton("✅ Сохранить");
        saveBtn.setBackground(new Color(70, 130, 200));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> trySave());

        JButton cancelBtn = new JButton("Отмена");
        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn);
        panel.add(cancelBtn);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Логика сохранения
    // ─────────────────────────────────────────────────────────────────────────

    private void trySave() {
        String qText = questionField.getText().trim();
        if (qText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите текст вопроса!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            questionField.requestFocus();
            return;
        }

        List<TestConstructorController.AnswerOptionData> answers = new ArrayList<>();
        for (int i = 0; i < answerTextFields.size(); i++) {
            String text = answerTextFields.get(i).getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Введите текст ответа №" + (i + 1) + "!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                answerTextFields.get(i).requestFocus();
                return;
            }
            TestConstructorController.AnswerOptionData data =
                    new TestConstructorController.AnswerOptionData(text, i);
            List<JSpinner> spinners = impactSpinners.get(i);
            for (int j = 0; j < spinners.size(); j++) {
                int delta = (int) spinners.get(j).getValue();
                if (delta != 0) data.addImpact(j, delta);
            }
            answers.add(data);
        }

        savedQuestionText = qText;
        savedAnswers = answers;
        dispose();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Результат для вызывающего кода
    // ─────────────────────────────────────────────────────────────────────────

    /** true — пользователь нажал «Сохранить», false — отменил/закрыл окно */
    public boolean isSaved() {
        return savedAnswers != null;
    }

    public String getQuestionText() {
        return savedQuestionText;
    }

    public List<TestConstructorController.AnswerOptionData> getAnswers() {
        return savedAnswers;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Вспомогательный метод
    // ─────────────────────────────────────────────────────────────────────────

    private static JLabel bold(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        return lbl;
    }
}
