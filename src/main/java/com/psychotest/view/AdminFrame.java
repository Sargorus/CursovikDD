package main.java.com.psychotest.view;

import main.java.com.psychotest.model.User;
import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {
    private User currentUser;

    public AdminFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("PsychoTest - Панель администратора");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Создаем меню
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu userMenu = new JMenu("Пользователи");
        JMenuItem createUserItem = new JMenuItem("Создать пользователя");
        JMenuItem viewUsersItem = new JMenuItem("Список пользователей");
        userMenu.add(createUserItem);
        userMenu.add(viewUsersItem);

        JMenu groupMenu = new JMenu("Группы");
        JMenuItem createGroupItem = new JMenuItem("Создать группу");
        JMenuItem viewGroupsItem = new JMenuItem("Список групп");
        groupMenu.add(createGroupItem);
        groupMenu.add(viewGroupsItem);

        menuBar.add(fileMenu);
        menuBar.add(userMenu);
        menuBar.add(groupMenu);

        setJMenuBar(menuBar);

        // Приветствие
        JLabel welcomeLabel = new JLabel("Добро пожаловать, " + currentUser.getFullName() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(welcomeLabel, BorderLayout.CENTER);

        // Временные обработчики
        createUserItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Функция создания пользователя будет добавлена"));
        viewUsersItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Функция просмотра пользователей будет добавлена"));
        createGroupItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Функция создания групп будет добавлена"));
        viewGroupsItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Функция просмотра групп будет добавлена"));
    }
}