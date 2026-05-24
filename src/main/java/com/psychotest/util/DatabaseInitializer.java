package main.java.com.psychotest.util;

import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.util.PasswordUtil;

public class DatabaseInitializer {

    public static void initialize() {
        try {
            // Проверяем подключение к БД
            DatabaseConnection.getInstance().getConnection();

            // Проверяем, есть ли пользователи
            UserDAO userDAO = new UserDAO();
            if (userDAO.findAll().isEmpty()) {
                // Создаем администратора по умолчанию
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(PasswordUtil.hashPassword("admin123"));
                admin.setFullName("System Administrator");
                admin.setRole("ADMIN");

                if (userDAO.save(admin)) {
                    System.out.println("✓ Создан администратор: login=admin, password=admin123");
                } else {
                    System.out.println("✗ Не удалось создать администратора");
                }
            } else {
                System.out.println("✓ В базе уже есть пользователи");
            }
        } catch (Exception e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
