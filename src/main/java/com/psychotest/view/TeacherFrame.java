package main.java.com.psychotest.view;

import main.java.com.psychotest.model.User;
import javax.swing.*;
import java.awt.*;

public class TeacherFrame extends JFrame {
    private User currentUser;

    public TeacherFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("PsychoTest - Панель преподавателя");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();

        JMenu testMenu = new JMenu("Тесты");
        JMenuItem createTestItem = new JMenuItem("Создать тест");
        JMenuItem myTestsItem = new JMenuItem("Мои тесты");
        testMenu.add(createTestItem);
        testMenu.add(myTestsItem);

        JMenu assignMenu = new JMenu("Назначения");
        JMenuItem assignTestItem = new JMenuItem("Назначить тест");
        JMenuItem viewAssignmentsItem = new JMenuItem("Просмотр назначений");
        assignMenu.add(assignTestItem);
        assignMenu.add(viewAssignmentsItem);

        JMenu reportMenu = new JMenu("Отчеты");
        JMenuItem viewResultsItem = new JMenuItem("Просмотр результатов");
        reportMenu.add(viewResultsItem);

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        menuBar.add(testMenu);
        menuBar.add(assignMenu);
        menuBar.add(reportMenu);

        setJMenuBar(menuBar);

        JLabel welcomeLabel = new JLabel("Добро пожаловать, преподаватель " + currentUser.getFullName() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(welcomeLabel, BorderLayout.CENTER);

        // Временные обработчики
        createTestItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Конструктор тестов будет добавлен"));
        myTestsItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Список тестов будет добавлен"));
    }
}