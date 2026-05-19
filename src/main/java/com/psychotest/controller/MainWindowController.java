package main.java.com.psychotest.controller;

import main.java.com.psychotest.model.User;
import main.java.com.psychotest.view.MainWindow;
import javax.swing.JOptionPane;

public class MainWindowController {
    private MainWindow window;
    private User currentUser;

    public MainWindowController(MainWindow window, User user) {
        this.window = window;
        this.currentUser = user;
        attachListeners();
        showWelcomeMessage();
    }

    private void attachListeners() {
        // Обработчики для разных ролей
        if (currentUser.getRole().equals("ADMIN")) {
            if (window.getManageUsersItem() != null) {
                window.getManageUsersItem().addActionListener(e -> manageUsers());
            }
            if (window.getManageGroupsItem() != null) {
                window.getManageGroupsItem().addActionListener(e -> manageGroups());
            }
        }

        if (currentUser.getRole().equals("TEACHER")) {
            if (window.getCreateTestItem() != null) {
                window.getCreateTestItem().addActionListener(e -> createTest());
            }
            if (window.getMyTestsItem() != null) {
                window.getMyTestsItem().addActionListener(e -> viewMyTests());
            }
            if (window.getAssignTestItem() != null) {
                window.getAssignTestItem().addActionListener(e -> assignTest());
            }
            if (window.getViewResultsItem() != null) {
                window.getViewResultsItem().addActionListener(e -> viewResults());
            }
        }

        if (currentUser.getRole().equals("TAKER")) {
            if (window.getMyTestsItem() != null) {
                window.getMyTestsItem().addActionListener(e -> viewAvailableTests());
            }
            if (window.getViewResultsItem() != null) {
                window.getViewResultsItem().addActionListener(e -> viewMyResults());
            }
        }

        if (window.getExitItem() != null) {
            window.getExitItem().addActionListener(e -> exit());
        }
    }

    private void showWelcomeMessage() {
        JOptionPane.showMessageDialog(window,
                "Добро пожаловать в систему, " + currentUser.getFullName() + "!\n" +
                        "Ваша роль: " + getRoleName(currentUser.getRole()),
                "Добро пожаловать",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String getRoleName(String role) {
        switch (role) {
            case "ADMIN": return "Администратор";
            case "TEACHER": return "Преподаватель";
            case "TAKER": return "Тестируемый";
            default: return role;
        }
    }

    // Методы для реализации
    private void manageUsers() {
        JOptionPane.showMessageDialog(window,
                "Управление пользователями будет реализовано в следующей версии",
                "В разработке",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void manageGroups() {
        JOptionPane.showMessageDialog(window,
                "Управление группами будет реализовано в следующей версии",
                "В разработке",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void createTest() {
        main.java.com.psychotest.view.dialogs.TestConstructorDialog dialog =
                new main.java.com.psychotest.view.dialogs.TestConstructorDialog(window, currentUser.getId());
        dialog.setVisible(true);
    }

    private void viewMyTests() {
        JOptionPane.showMessageDialog(window,
                "Список тестов будет отображаться здесь",
                "Информация",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void assignTest() {
        JOptionPane.showMessageDialog(window,
                "Назначение тестов будет реализовано в следующей версии",
                "В разработке",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewResults() {
        JOptionPane.showMessageDialog(window,
                "Просмотр результатов будет реализован в следующей версии",
                "В разработке",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewAvailableTests() {
        JOptionPane.showMessageDialog(window,
                "Доступные тесты будут отображаться здесь",
                "Информация",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewMyResults() {
        JOptionPane.showMessageDialog(window,
                "Ваши результаты будут отображаться здесь",
                "Информация",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exit() {
        int confirm = JOptionPane.showConfirmDialog(window,
                "Вы уверены, что хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            window.dispose();
            System.exit(0);
        }
    }
}
