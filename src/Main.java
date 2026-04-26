//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import view.EntryWindow;
import controller.LoginController;
import javax.swing.*;

public class Main
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            //View
            EntryWindow window = new EntryWindow("Авторизация");

            //Controller
            new LoginController(window);
        });
    }
}