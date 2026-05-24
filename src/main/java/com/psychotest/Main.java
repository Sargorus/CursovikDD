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

import main.java.com.psychotest.util.DatabaseConnection;
import main.java.com.psychotest.util.DatabaseInitializer;
import main.java.com.psychotest.util.DbConfig;
import main.java.com.psychotest.view.EntryWindow;
import main.java.com.psychotest.view.dialogs.DbSettingsDialog;
import main.java.com.psychotest.controller.LoginController;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("PsychoTest v1.0 запуск...");

        // ── Шаг 1: Установка L&F до показа любых диалогов ──
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // ── Шаг 2: Загружаем конфигурацию БД ──
        DbConfig config = DbConfig.load();

        if (config == null) {
            // Первый запуск — файл настроек ещё не создан
            config = showDbSettings(null,
                    "Файл настроек не найден. Укажите параметры подключения к PostgreSQL.");
            if (config == null) System.exit(0);
        }

        // ── Шаг 3: Применяем настройки и проверяем соединение ──
        DatabaseConnection.getInstance().configure(config);

        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            // Соединение не удалось — даём пользователю исправить настройки
            String errMsg = "Не удалось подключиться к базе данных:\n" + e.getMessage();
            System.err.println(errMsg);

            config = showDbSettings(config, errMsg);
            if (config == null) System.exit(0);

            DatabaseConnection.getInstance().configure(config);
            try {
                DatabaseInitializer.initialize();
            } catch (Exception e2) {
                JOptionPane.showMessageDialog(null,
                        "Ошибка инициализации БД:\n" + e2.getMessage(),
                        "Критическая ошибка", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        // ── Шаг 4: Запускаем главное окно ──
        SwingUtilities.invokeLater(() -> {
            EntryWindow window = new EntryWindow();
            new LoginController(window);
            window.setVisible(true);
        });
    }

    /**
     * Показывает диалог настройки БД синхронно (блокирует главный поток до закрытия).
     * @return DbConfig если пользователь нажал "Сохранить и продолжить", null если "Выход"
     */
    private static DbConfig showDbSettings(DbConfig current, String errorMessage) {
        DbConfig[] result = {null};
        try {
            SwingUtilities.invokeAndWait(() -> {
                DbSettingsDialog dlg = new DbSettingsDialog(null, current, errorMessage);
                dlg.setVisible(true);
                result[0] = dlg.getResult();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result[0];
    }
}