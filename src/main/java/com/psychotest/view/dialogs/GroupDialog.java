package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.model.Group;
import javax.swing.*;
import java.awt.*;

public class GroupDialog extends JDialog {
    private JTextField nameField;
    private JTextArea descriptionArea;
    private boolean confirmed = false;
    private Group group;

    public GroupDialog(Window parent, Group editGroup) {
        super(parent, editGroup == null ? "Добавление группы" : "Редактирование группы",
                ModalityType.APPLICATION_MODAL);
        this.group = editGroup;
        initComponents();

        if (editGroup != null) {
            loadGroupData();
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Название
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Название группы:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        add(nameField, gbc);

        // Описание
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Описание:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        add(scrollPane, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> ok());
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> cancel());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }

    private void loadGroupData() {
        nameField.setText(group.getName());
        descriptionArea.setText(group.getDescription());
    }

    private void ok() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите название группы!");
            return;
        }

        if (group == null) {
            group = new Group();
        }

        group.setName(name);
        group.setDescription(description);

        confirmed = true;
        dispose();
    }

    private void cancel() {
        confirmed = false;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Group getGroup() {
        return group;
    }
}
