package main.java.com.psychotest.util;

import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.util.PasswordUtil;

public class DatabaseInitializer {

    /**
     * Инициализирует БД: проверяет подключение и создаёт первичные данные.
     * Бросает исключение при ошибке подключения — Main.java поймает его и
     * предложит пользователю исправить настройки подключения.
     */
    public static void initialize() throws Exception {
        // Проверяем подключение к БД (бросает SQLException если недоступна)
        try (java.sql.Connection testConn = DatabaseConnection.getInstance().getConnection()) {
            // соединение живо — продолжаем
        }

        // Проверяем, есть ли пользователи
        UserDAO userDAO = new UserDAO();
        try {
            if (userDAO.findAll().isEmpty()) {
                // Создаём администратора по умолчанию
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
            // Ошибка работы с пользователями некритична для запуска
            System.err.println("Предупреждение при инициализации данных: " + e.getMessage());
        }
    }
}
