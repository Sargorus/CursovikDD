package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TestConstructorController;
import main.java.com.psychotest.exception.InvalidRangeException;
import main.java.com.psychotest.model.Parameter;
import main.java.com.psychotest.model.ParameterInterpretation;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ParametersPanel extends JPanel {
    private TestConstructorController controller;
    private JTable paramsTable;
    private DefaultTableModel tableModel;
    private JTextArea interpretationArea;

    public ParametersPanel(TestConstructorController controller) {
        this.controller = controller;
        initComponents();
        loadData();

        // Подписываемся на изменения модели
        controller.addListener(() -> loadData());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JLabel infoLabel = new JLabel("Параметры (шкалы) теста. Например: EI (экстраверсия/интроверсия)");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoLabel.setForeground(Color.GRAY);
        add(infoLabel, BorderLayout.NORTH);

        String[] columns = {"Название", "Тип шкалы", "Интерпретации"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paramsTable = new JTable(tableModel);
        paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paramsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showInterpretation();
            }
        });

        JScrollPane tableScroll = new JScrollPane(paramsTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Список параметров"));

        interpretationArea = new JTextArea();
        interpretationArea.setEditable(false);
        interpretationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane interpretScroll = new JScrollPane(interpretationArea);
        interpretScroll.setBorder(BorderFactory.createTitledBorder("Интерпретация выбранного параметра"));
        interpretScroll.setPreferredSize(new Dimension(300, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, interpretScroll);
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("➕ Добавить параметр");
        addButton.addActionListener(e -> addParameter());

        JButton editButton = new JButton("✏️ Редактировать");
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editParameter());

        JButton removeButton = new JButton("➖ Удалить параметр");
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> removeParameter());

        paramsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = paramsTable.getSelectedRow() != -1;
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
        for (Parameter param : controller.getParameters()) {
            String typeStr = param.getScaleType().equals("BINARY") ? "Бинарная" : "Диапазонная";
            tableModel.addRow(new Object[]{
                    param.getName(),
                    typeStr,
                    param.getInterpretations().size() + " вариантов"
            });
        }
    }

    private void showInterpretation() {
        int row = paramsTable.getSelectedRow();
        List<Parameter> params = controller.getParameters();
        if (row >= 0 && row < params.size()) {
            Parameter param = params.get(row);
            StringBuilder sb = new StringBuilder();
            sb.append("Параметр: ").append(param.getName()).append("\n");
            sb.append("Тип: ").append(param.getScaleType().equals("BINARY") ? "Бинарная" : "Диапазонная").append("\n\n");
            sb.append("Интерпретации:\n");
            sb.append("─".repeat(40)).append("\n");

            for (ParameterInterpretation interp : param.getInterpretations()) {
                if (param.getScaleType().equals("BINARY")) {
                    sb.append("  ").append(interp.getBinaryValue()).append(": ");
                    sb.append(interp.getInterpretationText()).append("\n");
                } else {
                    sb.append("  ").append(interp.getRangeStart()).append("-").append(interp.getRangeEnd());
                    sb.append(": ").append(interp.getInterpretationText()).append("\n");
                }
            }
            interpretationArea.setText(sb.toString());
        } else {
            interpretationArea.setText("Выберите параметр для просмотра интерпретации");
        }
    }

    private void addParameter() {
        JTextField nameField = new JTextField(15);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Бинарная (E/I)", "Диапазонная (0-100)"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Название параметра:"));
        panel.add(nameField);
        panel.add(new JLabel("Тип шкалы:"));
        panel.add(typeCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Новый параметр", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim().toUpperCase();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите название параметра!");
                return;
            }

            // Проверка на дублирующееся имя
            boolean duplicate = controller.getParameters().stream()
                    .anyMatch(p -> p.getName().equals(name));
            if (duplicate) {
                JOptionPane.showMessageDialog(this,
                        "Параметр с именем «" + name + "» уже существует!",
                        "Дубликат", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String type = (String) typeCombo.getSelectedItem();
            boolean isBinary = type.startsWith("Бинарная");
            String scaleType = isBinary ? "BINARY" : "RANGE";

            List<ParameterInterpretation> interpretations = new ArrayList<>();

            if (isBinary) {
                addBinaryInterpretations(interpretations);
            } else {
                addRangeInterpretations(interpretations);
            }

            if (!interpretations.isEmpty()) {
                controller.addParameter(name, scaleType, interpretations);
            }
        }
    }

    private void addBinaryInterpretations(List<ParameterInterpretation> interpretations) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        JTextField leftValue = new JTextField(5);
        JTextArea leftDesc = new JTextArea(3, 20);
        leftDesc.setLineWrap(true);

        JTextField rightValue = new JTextField(5);
        JTextArea rightDesc = new JTextArea(3, 20);
        rightDesc.setLineWrap(true);

        panel.add(new JLabel("Левый полюс (код):"));
        panel.add(leftValue);
        panel.add(new JLabel("Описание левого полюса:"));
        panel.add(new JScrollPane(leftDesc));
        panel.add(new JLabel("Правый полюс (код):"));
        panel.add(rightValue);
        panel.add(new JLabel("Описание правого полюса:"));
        panel.add(new JScrollPane(rightDesc));

        int result = JOptionPane.showConfirmDialog(this, panel, "Интерпретации", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String leftVal = leftValue.getText().trim().toUpperCase();
            String rightVal = rightValue.getText().trim().toUpperCase();

            if (!leftVal.isEmpty() && !leftDesc.getText().trim().isEmpty()) {
                interpretations.add(new ParameterInterpretation(leftVal, leftDesc.getText().trim()));
            }
            if (!rightVal.isEmpty() && !rightDesc.getText().trim().isEmpty()) {
                interpretations.add(new ParameterInterpretation(rightVal, rightDesc.getText().trim()));
            }
        }
    }

    private void addRangeInterpretations(List<ParameterInterpretation> interpretations) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> interpList = new JList<>(listModel);

        JButton addInterpButton = new JButton("Добавить интерпретацию");
        JButton doneButton = new JButton("Готово");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(interpList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addInterpButton);
        buttonPanel.add(doneButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Интерпретации для параметра",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(panel);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        addInterpButton.addActionListener(e -> {
            JTextField minField = new JTextField(5);
            JTextField maxField = new JTextField(5);
            JTextArea descArea = new JTextArea(3, 20);
            descArea.setLineWrap(true);

            JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            inputPanel.add(new JLabel("Диапазон от:"));
            inputPanel.add(minField);
            inputPanel.add(new JLabel("до:"));
            inputPanel.add(maxField);
            inputPanel.add(new JLabel("Интерпретация:"));
            inputPanel.add(new JScrollPane(descArea));

            int res = JOptionPane.showConfirmDialog(dialog, inputPanel, "Добавить интерпретацию", JOptionPane.OK_CANCEL_OPTION);

            if (res == JOptionPane.OK_OPTION) {
                try {
                    int min = Integer.parseInt(minField.getText().trim());
                    int max = Integer.parseInt(maxField.getText().trim());
                    String desc = descArea.getText().trim();

                    if (min > max) {
                        JOptionPane.showMessageDialog(dialog, "Минимальное значение не может быть больше максимального!");
                        return;
                    }

                    if (desc.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Введите текст интерпретации!");
                        return;
                    }

                    listModel.addElement(min + "-" + max + ": " + desc);
                    interpretations.add(new ParameterInterpretation(min, max, desc));

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Введите корректные числа!");
                }
            }
        });

        doneButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void editParameter() {
        int row = paramsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите параметр для редактирования!");
            return;
        }

        Parameter param = controller.getParameter(row);

        // Шаг 1: редактирование названия (тип шкалы менять нельзя — сломает ответы)
        JTextField nameField = new JTextField(param.getName(), 15);
        String typeDisplay = param.getScaleType().equals("BINARY") ? "Бинарная" : "Диапазонная";
        JLabel typeLabel = new JLabel(typeDisplay + "  (изменить нельзя)");
        typeLabel.setForeground(Color.GRAY);

        JPanel namePanel = new JPanel(new GridLayout(0, 2, 5, 5));
        namePanel.add(new JLabel("Название параметра:"));
        namePanel.add(nameField);
        namePanel.add(new JLabel("Тип шкалы:"));
        namePanel.add(typeLabel);

        int r = JOptionPane.showConfirmDialog(this, namePanel,
                "Редактирование параметра", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim().toUpperCase();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите название параметра!");
            return;
        }

        // Проверка на дублирующееся имя (не считая сам редактируемый параметр)
        final int currentRow = row;
        boolean duplicate = false;
        java.util.List<main.java.com.psychotest.model.Parameter> params = controller.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (i != currentRow && params.get(i).getName().equals(name)) {
                duplicate = true;
                break;
            }
        }
        if (duplicate) {
            JOptionPane.showMessageDialog(this,
                    "Параметр с именем «" + name + "» уже существует!",
                    "Дубликат", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Шаг 2: редактирование интерпретаций
        List<ParameterInterpretation> newInterpretations;
        if (param.getScaleType().equals("BINARY")) {
            newInterpretations = editBinaryInterpretations(param.getInterpretations());
            if (newInterpretations == null) return; // пользователь отменил
        } else {
            newInterpretations = new ArrayList<>(param.getInterpretations());
            editRangeInterpretationsInPlace(newInterpretations);
        }

        controller.updateParameter(row, name, newInterpretations);
    }

    /** Возвращает новый список бинарных интерпретаций с предзаполнением, или null если отменено */
    private List<ParameterInterpretation> editBinaryInterpretations(List<ParameterInterpretation> current) {
        String leftVal = "", leftDesc = "", rightVal = "", rightDesc = "";
        if (current.size() >= 1) {
            leftVal  = current.get(0).getBinaryValue() != null       ? current.get(0).getBinaryValue()       : "";
            leftDesc = current.get(0).getInterpretationText() != null ? current.get(0).getInterpretationText() : "";
        }
        if (current.size() >= 2) {
            rightVal  = current.get(1).getBinaryValue() != null       ? current.get(1).getBinaryValue()       : "";
            rightDesc = current.get(1).getInterpretationText() != null ? current.get(1).getInterpretationText() : "";
        }

        JTextField leftValueField  = new JTextField(leftVal, 5);
        JTextArea  leftDescArea    = new JTextArea(leftDesc, 3, 20);
        leftDescArea.setLineWrap(true);
        JTextField rightValueField = new JTextField(rightVal, 5);
        JTextArea  rightDescArea   = new JTextArea(rightDesc, 3, 20);
        rightDescArea.setLineWrap(true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Левый полюс (код):"));
        panel.add(leftValueField);
        panel.add(new JLabel("Описание левого полюса:"));
        panel.add(new JScrollPane(leftDescArea));
        panel.add(new JLabel("Правый полюс (код):"));
        panel.add(rightValueField);
        panel.add(new JLabel("Описание правого полюса:"));
        panel.add(new JScrollPane(rightDescArea));

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Редактирование интерпретаций", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return null;

        List<ParameterInterpretation> updated = new ArrayList<>();
        String lv = leftValueField.getText().trim().toUpperCase();
        String ld = leftDescArea.getText().trim();
        String rv = rightValueField.getText().trim().toUpperCase();
        String rd = rightDescArea.getText().trim();

        if (!lv.isEmpty() && !ld.isEmpty()) {
            updated.add(new ParameterInterpretation(lv, ld));
        }
        if (!rv.isEmpty() && !rd.isEmpty()) {
            updated.add(new ParameterInterpretation(rv, rd));
        }
        return updated;
    }

    /** Открывает диалог редактирования диапазонных интерпретаций, изменяя список на месте */
    private void editRangeInterpretationsInPlace(List<ParameterInterpretation> interpretations) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        // Заполняем текущими интерпретациями
        for (ParameterInterpretation interp : interpretations) {
            listModel.addElement(interp.getRangeStart() + "-" + interp.getRangeEnd()
                    + ": " + interp.getInterpretationText());
        }

        JList<String> interpList = new JList<>(listModel);
        JButton addBtn    = new JButton("➕ Добавить");
        JButton removeBtn = new JButton("➖ Удалить выбранное");
        JButton doneBtn   = new JButton("✅ Готово");

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JScrollPane(interpList), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(doneBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Диапазонные интерпретации", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(panel);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        addBtn.addActionListener(e -> {
            JTextField minF = new JTextField(5), maxF = new JTextField(5);
            JTextArea  descA = new JTextArea(3, 20);
            descA.setLineWrap(true);
            JPanel ip = new JPanel(new GridLayout(0, 2, 5, 5));
            ip.add(new JLabel("Диапазон от:")); ip.add(minF);
            ip.add(new JLabel("до:"));          ip.add(maxF);
            ip.add(new JLabel("Интерпретация:")); ip.add(new JScrollPane(descA));
            if (JOptionPane.showConfirmDialog(dialog, ip, "Добавить интерпретацию",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int min = Integer.parseInt(minF.getText().trim());
                    int max = Integer.parseInt(maxF.getText().trim());
                    String desc = descA.getText().trim();
                    if (min > max) { JOptionPane.showMessageDialog(dialog, "min > max!"); return; }
                    if (desc.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Введите текст!"); return; }
                    listModel.addElement(min + "-" + max + ": " + desc);
                    interpretations.add(new ParameterInterpretation(min, max, desc));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Введите корректные числа!");
                }
            }
        });

        removeBtn.addActionListener(e -> {
            int sel = interpList.getSelectedIndex();
            if (sel >= 0 && sel < interpretations.size()) {
                listModel.remove(sel);
                interpretations.remove(sel);
            }
        });

        doneBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void removeParameter() {
        int row = paramsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите параметр для удаления!");
            return;
        }

        String paramName = (String) tableModel.getValueAt(row, 0);
        int usageCount = controller.getParameterUsageCount(row);

        String warning = "Удалить параметр '" + paramName + "'?\n\n";
        if (usageCount > 0) {
            warning += "⚠️ ВНИМАНИЕ! Этот параметр используется в " + usageCount + " вопросах.\n";
            warning += "После удаления:\n• Все связанные ответы потеряют влияние\n";
            warning += "• Индексы параметров в вопросах будут автоматически скорректированы\n\n";
        }
        warning += "Вы уверены, что хотите продолжить?";

        int confirm = JOptionPane.showConfirmDialog(this, warning,
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                usageCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.removeParameter(row);
        }
    }
}