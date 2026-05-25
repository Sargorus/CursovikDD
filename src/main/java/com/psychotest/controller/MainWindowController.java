package main.java.com.psychotest.controller;

import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.service.AuthService;
import main.java.com.psychotest.view.MainWindow;
import main.java.com.psychotest.view.dialogs.AssignTestDialog;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindowController {
    private final MainWindow window;
    private final User currentUser;
    private final TeacherController teacherController;
    private final AuthService authService = new AuthService();

    public MainWindowController(MainWindow window, User user) {
        this.window = window;
        this.currentUser = user;
        this.teacherController = new TeacherController(currentUser.getId());
        attachListeners();
    }

    private void attachListeners() {
        // Обработчики для разных ролей
        // Для администратора слушатели меню уже установлены в MainWindow.setupAdminMenu()
        // и setupAdminPanels() вызывается оттуда напрямую — дублировать здесь не нужно.

        if (currentUser.getRole().equals("TEACHER")) {
            if (window.getCreateTestItem() != null) {
                window.getCreateTestItem().addActionListener(e -> createTest());
            }
            if (window.getMyTestsItem() != null) {
                window.getMyTestsItem().addActionListener(e -> showMyTests());
            }
            if (window.getAssignTestItem() != null) {
                window.getAssignTestItem().addActionListener(e -> showAssignTest());
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

        // Смена пароля — общий для всех ролей
        if (window.getChangePasswordItem() != null) {
            window.getChangePasswordItem().addActionListener(e -> changePassword());
        }
    }

    // ========== Методы для преподавателя ==========

    private void createTest() {
        main.java.com.psychotest.view.dialogs.TestConstructorDialog dialog =
                new main.java.com.psychotest.view.dialogs.TestConstructorDialog(window, currentUser.getId());
        dialog.setVisible(true);
    }

    private void showMyTests() {
        window.switchToTab(0); // Вкладка "Тесты"
    }

    private void showAssignTest() {
        // Сначала нужно выбрать тест
        List<Test> tests = teacherController.getMyTests();
        if (tests.isEmpty()) {
            JOptionPane.showMessageDialog(window,
                    "У вас нет созданных тестов для назначения!",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Создаём диалог выбора теста
        Test test = (Test) JOptionPane.showInputDialog(window,
                "Выберите тест для назначения:",
                "Назначение теста",
                JOptionPane.QUESTION_MESSAGE,
                null,
                tests.toArray(),
                tests.get(0));

        if (test != null) {
            AssignTestDialog dialog = new AssignTestDialog(window, teacherController,
                    test.getId(), test.getName());
            dialog.setVisible(true);
        }
    }

    private void viewResults() {
        window.switchToTab(3); // Вкладка "Результаты"
    }

    // ========== Методы для тестируемого ==========

    private void viewAvailableTests() {
        window.switchToTab(0); // Вкладка "Доступные тесты"
    }

    private void viewMyResults() {
        window.switchToTab(1); // Вкладка "Мои результаты"
    }

    // ========== Общие методы ==========

    private void changePassword() {
        JPasswordField currentField = new JPasswordField(20);
        JPasswordField newField     = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Текущий пароль:"));
        panel.add(currentField);
        panel.add(new JLabel("Новый пароль:"));
        panel.add(newField);
        panel.add(new JLabel("Подтвердите новый пароль:"));
        panel.add(confirmField);

        int result = JOptionPane.showConfirmDialog(window, panel,
                "Смена пароля", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String current = new String(currentField.getPassword());
        String newPass  = new String(newField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (newPass.length() < 4) {
            JOptionPane.showMessageDialog(window,
                    "Новый пароль должен содержать не менее 4 символов!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(window,
                    "Пароли не совпадают!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = authService.changePassword(currentUser.getId(), current, newPass);
        if (success) {
            JOptionPane.showMessageDialog(window,
                    "Пароль успешно изменён!", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(window,
                    "Текущий пароль введён неверно!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
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