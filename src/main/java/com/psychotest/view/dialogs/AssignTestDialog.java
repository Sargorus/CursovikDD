package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.model.Group;
import main.java.com.psychotest.model.User;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class AssignTestDialog extends JDialog {
    private TeacherController controller;
    private int testId;
    private String testName;

    private JTabbedPane tabbedPane;
    private JComboBox<User> userCombo;
    private JComboBox<Group> groupCombo;
    private JSpinner dateSpinner;
    private boolean confirmed = false;

    public AssignTestDialog(Window parent, TeacherController controller, int testId, String testName) {
        super(parent, "Назначение теста: " + testName, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.testId = testId;
        this.testName = testName;
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(500, 300);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Информационная панель
        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Назначение теста: " + testName));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));
        add(infoPanel, BorderLayout.NORTH);

        // Вкладки для выбора пользователя или группы
        tabbedPane = new JTabbedPane();

        // Вкладка "Пользователь"
        JPanel userPanel = createUserPanel();
        tabbedPane.addTab("👤 Конкретному пользователю", userPanel);

        // Вкладка "Группа"
        JPanel groupPanel = createGroupPanel();
        tabbedPane.addTab("👥 Целой группе", groupPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton assignButton = new JButton("✅ Назначить");
        assignButton.addActionListener(e -> assign());
        JButton cancelButton = new JButton("❌ Отмена");
        cancelButton.addActionListener(e -> cancel());

        buttonPanel.add(assignButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Выбор пользователя
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Выберите пользователя:"), gbc);

        gbc.gridx = 1;
        List<User> takers = controller.getAllTakers();
        userCombo = new JComboBox<>(takers.toArray(new User[0]));
        userCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(userCombo, gbc);

        // Дата выполнения
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Дата выполнения (опционально):"), gbc);

        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy");
        dateSpinner.setEditor(dateEditor);
        panel.add(dateSpinner, gbc);

        // Подсказка
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel hintLabel = new JLabel("Оставьте дату пустой, если тест без ограничения по времени");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, gbc);

        return panel;
    }

    private JPanel createGroupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Выбор группы
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Выберите группу:"), gbc);

        gbc.gridx = 1;
        List<Group> groups = controller.getAllGroups();
        groupCombo = new JComboBox<>(groups.toArray(new Group[0]));
        groupCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(groupCombo, gbc);

        // Дата выполнения
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Дата выполнения (опционально):"), gbc);

        gbc.gridx = 1;
        JSpinner groupDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(groupDateSpinner, "dd.MM.yyyy");
        groupDateSpinner.setEditor(dateEditor);
        panel.add(groupDateSpinner, gbc);

        // Подсказка
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel hintLabel = new JLabel("Оставьте дату пустой, если тест без ограничения по времени");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, gbc);

        return panel;
    }

    private void assign() {
        int selectedTab = tabbedPane.getSelectedIndex();
        Date dueDate = (Date) dateSpinner.getValue();

        boolean success;

        if (selectedTab == 0) {
            // Назначение пользователю
            User selectedUser = (User) userCombo.getSelectedItem();
            if (selectedUser == null) {
                JOptionPane.showMessageDialog(this, "Выберите пользователя!");
                return;
            }
            success = controller.assignTestToUser(testId, selectedUser.getId(), dueDate);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Тест \"" + testName + "\" назначен пользователю " + selectedUser.getFullName());
            }
        } else {
            // Назначение группе
            Group selectedGroup = (Group) groupCombo.getSelectedItem();
            if (selectedGroup == null) {
                JOptionPane.showMessageDialog(this, "Выберите группу!");
                return;
            }
            success = controller.assignTestToGroup(testId, selectedGroup.getId(), dueDate);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Тест \"" + testName + "\" назначен группе " + selectedGroup.getName());
            }
        }

        if (success) {
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
