package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Group;
import main.java.com.psychotest.model.Test;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class AssignTestDialog extends JDialog {
    private TeacherController controller;
    private int testId;
    private String testName;

    private JComboBox<Group> groupCombo;
    private JSpinner dateSpinner;
    private JCheckBox noDueDateCheckBox;
    private boolean confirmed = false;

    public AssignTestDialog(Window parent, TeacherController controller, int testId, String testName) {
        super(parent, "Назначение теста: " + testName, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.testId = testId;
        this.testName = testName;
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(450, 250);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Информационная панель
        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Назначение теста: " + testName));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));
        add(infoPanel, BorderLayout.NORTH);

        // Основная панель выбора группы
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Выбор группы
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Выберите группу:"), gbc);

        gbc.gridx = 1;
        List<Group> groups = controller.getAllGroups();
        groupCombo = new JComboBox<>(groups.toArray(new Group[0]));
        groupCombo.setPreferredSize(new Dimension(200, 25));

        if (groups.isEmpty()) {
            groupCombo.addItem(new Group("Нет доступных групп", ""));
            groupCombo.setEnabled(false);
        }
        mainPanel.add(groupCombo, gbc);

        // Дата выполнения
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Дата выполнения:"), gbc);

        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy");
        dateSpinner.setEditor(dateEditor);
        mainPanel.add(dateSpinner, gbc);

        // Чекбокс "Без срока"
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        noDueDateCheckBox = new JCheckBox("Без срока выполнения (тест доступен без ограничений)");
        noDueDateCheckBox.setSelected(true); // по умолчанию — без срока
        noDueDateCheckBox.addActionListener(e -> dateSpinner.setEnabled(!noDueDateCheckBox.isSelected()));
        dateSpinner.setEnabled(false); // изначально спиннер отключён
        mainPanel.add(noDueDateCheckBox, gbc);
        gbc.gridwidth = 1;

        add(mainPanel, BorderLayout.CENTER);

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
        Group selectedGroup = (Group) groupCombo.getSelectedItem();
        if (selectedGroup == null || selectedGroup.getId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Выберите группу для назначения!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // null если выбрано "Без срока", иначе — выбранная дата
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