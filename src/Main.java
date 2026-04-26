//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import model.UserDAO;
import view.EntryWindow;

import javax.swing.*;
import java.awt.*;

public class Main
{
    public static void main(String[] args)
    {
        EntryWindow window = new EntryWindow("Авторизация");

        //обработчик для кнопки "Войти"
        window.getEntryButton().addActionListener(e -> {
            String login = window.getNewLogin().getText();
            String password = new String(window.getNewPassword().getPassword());

            // Проверка, реальные ли данные вводит пользователь
            if (login.equals("Введите логин") || login.trim().isEmpty()){
                JOptionPane.showMessageDialog(window,
                        "Пожалуйста, введите логин!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            } else if (password.equals("Введите пароль") || password.length() == 0){
                JOptionPane.showMessageDialog(window,
                        "Пожалуйста, введите пароль!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            } else{
                // Проверка есть ли пользователь в базе данных
                if (UserDAO.checkUser(login, password)){
                    JOptionPane.showMessageDialog(window,
                            "Добро пожаловать, " + login + "!",
                            "Успешный вход",
                            JOptionPane.INFORMATION_MESSAGE);
                    window.clearFields();
                }
            }
        });
    }
}