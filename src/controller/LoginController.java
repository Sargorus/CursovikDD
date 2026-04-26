package controller;

import view.EntryWindow;
import view.MainWindow;
import model.UserDAO;

public class LoginController {
    private EntryWindow window;

    public LoginController(EntryWindow window){
        this.window = window;
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
        } else if (password.equals(window.getPasswordHint()) || password.length() == 0) {
            window.showError("Пожалуйста, введите пароль!");
        } else {
            // Проверка в базе данных
            if (UserDAO.checkUser(login, password)) {
                window.showSuccess("Добро пожаловать, " + login + "!");
                window.clearFields();

                openMainWindow(login);
            } else {
                window.showError("Неверный логин или пароль!");
                window.clearFields();
            }
        }
    }

    private void openMainWindow(String username) {
        window.close();
        MainWindow mainWindow = new MainWindow(username);
        new MainWindowController(mainWindow, username);
    }
}
