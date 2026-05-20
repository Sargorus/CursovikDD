package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.AdminController;
import main.java.com.psychotest.model.Group;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.view.dialogs.GroupDialog;
import main.java.com.psychotest.view.dialogs.GroupMembersDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GroupsPanel extends JPanel {
    private AdminController controller;
    private JTable groupsTable;
    private DefaultTableModel tableModel;

    public GroupsPanel(AdminController controller) {
        this.controller = controller;
        initComponents();
        loadGroups();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Таблица групп
        String[] columns = {"ID", "Название", "Описание", "Участников"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        groupsTable = new JTable(tableModel);
        groupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(groupsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton("➕ Добавить группу");
        addButton.addActionListener(e -> addGroup());

        JButton editButton = new JButton("✏️ Редактировать");
        editButton.addActionListener(e -> editGroup());

        JButton membersButton = new JButton("👥 Участники");
        membersButton.addActionListener(e -> manageMembers());

        JButton deleteButton = new JButton("🗑️ Удалить");
        deleteButton.addActionListener(e -> deleteGroup());

        JButton refreshButton = new JButton("🔄 Обновить");
        refreshButton.addActionListener(e -> loadGroups());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(membersButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadGroups() {
        tableModel.setRowCount(0);
        List<Group> groups = controller.getAllGroups();
        for (Group group : groups) {
            Object[] row = {
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    group.getMembers().size()
            };
            tableModel.addRow(row);
        }
    }

    private void addGroup() {
        GroupDialog dialog = new GroupDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Group group = dialog.getGroup();
            if (controller.addGroup(group.getName(), group.getDescription())) {
                JOptionPane.showMessageDialog(this, "Группа успешно добавлена!");
                loadGroups();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении группы!");
            }
        }
    }

    private void editGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите группу для редактирования!");
            return;
        }

        int groupId = (int) tableModel.getValueAt(selectedRow, 0);
        Group group = controller.getGroupById(groupId);

        if (group != null) {
            GroupDialog dialog = new GroupDialog(SwingUtilities.getWindowAncestor(this), group);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Group updatedGroup = dialog.getGroup();
                if (controller.updateGroup(groupId, updatedGroup.getName(), updatedGroup.getDescription())) {
                    JOptionPane.showMessageDialog(this, "Группа успешно обновлена!");
                    loadGroups();
                } else {
                    JOptionPane.showMessageDialog(this, "Ошибка при обновлении группы!");
                }
            }
        }
    }

    private void manageMembers() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите группу для управления участниками!");
            return;
        }

        int groupId = (int) tableModel.getValueAt(selectedRow, 0);
        Group group = controller.getGroupById(groupId);

        if (group != null) {
            List<User> allUsers = controller.getAllUsers();
            GroupMembersDialog dialog = new GroupMembersDialog(
                    SwingUtilities.getWindowAncestor(this), group, allUsers, controller);
            dialog.setVisible(true);
            loadGroups(); // Обновляем отображение
        }
    }

    private void deleteGroup() {
        int selectedRow = groupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите группу для удаления!");
            return;
        }

        String groupName = (String) tableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить группу '" + groupName + "'?\n" +
                        "Все связи с пользователями будут удалены.",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int groupId = (int) tableModel.getValueAt(selectedRow, 0);
            if (controller.deleteGroup(groupId)) {
                JOptionPane.showMessageDialog(this, "Группа успешно удалена!");
                loadGroups();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при удалении группы!");
            }
        }
    }
}