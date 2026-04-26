package controller;

import view.MainWindow;
import view.EntryWindow;
import javax.swing.JOptionPane;

public class MainWindowController {
    private MainWindow window;
    private String username;

    public MainWindowController(MainWindow window, String username) {
        this.window = window;
        this.username = username;
        attachListeners();
    }

    private void attachListeners() {
        window.getLogoutButton().addActionListener(e -> handleLogout());
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(window,
                "Вы уверены, что хотите выйти?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            window.close();

            EntryWindow entryWindow = new EntryWindow("Авторизация");
            new LoginController(entryWindow);
        }
    }
}
