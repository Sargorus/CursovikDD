//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
/*
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
*/

package main.java.com.psychotest;

import main.java.com.psychotest.util.DatabaseInitializer;
import main.java.com.psychotest.view.EntryWindow;
import main.java.com.psychotest.controller.LoginController;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("PsychoTest v1.0 запуск...");

        // Инициализация БД с тестовыми данными
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // Запускаем Swing-приложение в EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // Установка системного Look and Feel
                javax.swing.UIManager.setLookAndFeel(
                        javax.swing.UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Создание окна входа и контроллера
            EntryWindow window = new EntryWindow();
            new LoginController(window);
            window.setVisible(true);
        });
    }
}