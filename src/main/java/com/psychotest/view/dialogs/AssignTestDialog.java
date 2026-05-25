package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Group;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class AssignTestDialog extends JDialog {
    private TeacherController controller;
    private int testId;
    private String testName;

    private JTable groupTable;
    private DefaultTableModel groupTableModel;
    private List<Group> groups;
    private JSpinner dateSpinner;
    private JCheckBox noDueDateCheckBox;
    private boolean confirmed = false;

    public AssignTestDialog(Window parent, TeacherController controller, int testId, String testName) {
        super(parent, "Назначение теста: " + testName, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.testId = testId;
        this.testName = testName;
        initComponents();
        setLocationRelativeTo(parent);
        setSize(550, 420);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Информационная панель
        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Назначение теста: " + testName));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));
        add(infoPanel, BorderLayout.NORTH);

        // Центральная панель
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Метка над таблицей
        JLabel groupLabel = new JLabel("Выберите группу для назначения теста:");
        groupLabel.setFont(groupLabel.getFont().deriveFont(Font.BOLD));
        centerPanel.add(groupLabel, BorderLayout.NORTH);

        // Таблица групп
        groups = controller.getAllGroups();
        String[] columns = {"Название группы", "Описание"};
        groupTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Group g : groups) {
            String desc = g.getDescription() != null ? g.getDescription() : "";
            groupTableModel.addRow(new Object[]{g.getName(), desc});
        }

        if (groups.isEmpty()) {
            groupTableModel.addRow(new Object[]{"Нет доступных групп", ""});
        }

        groupTable = new JTable(groupTableModel);
        groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupTable.setRowHeight(26);
        groupTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        groupTable.getColumnModel().getColumn(1).setPreferredWidth(280);

        if (groups.isEmpty()) {
            groupTable.setEnabled(false);
        } else {
            groupTable.setRowSelectionInterval(0, 0); // выбираем первую группу по умолчанию
        }

        JScrollPane groupScroll = new JScrollPane(groupTable);
        groupScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        centerPanel.add(groupScroll, BorderLayout.CENTER);

        // Панель с датой
        JPanel datePanel = new JPanel(new GridBagLayout());
        datePanel.setBorder(BorderFactory.createTitledBorder("Срок выполнения"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        noDueDateCheckBox = new JCheckBox("Без срока выполнения (тест доступен без ограничений)");
        noDueDateCheckBox.setSelected(true);
        datePanel.add(noDueDateCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        datePanel.add(new JLabel("Дата выполнения:"), gbc);

        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setEnabled(false); // по умолчанию отключён
        datePanel.add(dateSpinner, gbc);

        noDueDateCheckBox.addActionListener(e ->
                dateSpinner.setEnabled(!noDueDateCheckBox.isSelected()));

        centerPanel.add(datePanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton assignButton = new JButton("✅ Назначить группе");
        assignButton.addActionListener(e -> assign());
        JButton cancelButton = new JButton("❌ Отмена");
        cancelButton.addActionListener(e -> cancel());
        buttonPanel.add(assignButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void assign() {
        int selectedRow = groupTable.getSelectedRow();
        if (selectedRow < 0 || groups.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Выберите группу для назначения!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Group selectedGroup = groups.get(selectedRow);

        // null если «Без срока», иначе выбранная дата
        Date dueDate = noDueDateCheckBox.isSelected() ? null : (Date) dateSpinner.getValue();

        boolean success = controller.assignTestToGroup(testId, selectedGroup.getId(), dueDate);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Тест \"" + testName + "\" назначен группе \"" + selectedGroup.getName() + "\"");
            confirmed = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при назначении теста!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancel() {
        confirmed = false;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
