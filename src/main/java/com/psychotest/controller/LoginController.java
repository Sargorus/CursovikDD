package main.java.com.psychotest.controller;

import main.java.com.psychotest.model.User;
import main.java.com.psychotest.service.AuthService;
import main.java.com.psychotest.view.EntryWindow;
import main.java.com.psychotest.view.MainWindow;
import javax.swing.JOptionPane;

public class LoginController {
    private EntryWindow window;
    private AuthService authService;

    public LoginController(EntryWindow window){
        this.window = window;
        this.authService = new AuthService();
        attachListeners();
    }

    private void attachListeners() {
        window.getEntryButton().addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String login = window.getNewLogin().getText();
        String password = new String(window.getNewPassword().getPassword());

        // Проверка на подсказки
        if (login.equals(window.getLoginHint()) || login.trim().isEmpty()) {
            window.showError("Пожалуйста, введите логин!");
            return;
        }

        if (password.equals(window.getPasswordHint()) || password.length() == 0) {
            window.showError("Пожалуйста, введите пароль!");
            return;
        }

        // Проверка в базе данных через AuthService
        User user = authService.authenticate(login, password);

        if (user != null) {
            window.showSuccess("Добро пожаловать, " + user.getFullName() + "!");
            window.clearFields();
            openMainWindow(user);
        } else {
            window.showError("Неверный логин или пароль!");
            window.clearFields();
        }
    }

    private void openMainWindow(User user) {
        window.setVisible(false);
        window.dispose();

        MainWindow mainWindow = new MainWindow(user);
        MainWindowController controller = new MainWindowController(mainWindow, user);
        mainWindow.setVisible(true);
    }
}
