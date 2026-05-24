package main.java.com.psychotest.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// DbConfig находится в том же пакете — import не нужен

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        this.url = System.getProperty("DB_URL", "jdbc:postgresql://localhost:5432/psychotest_db");
        this.username = System.getProperty("DB_USER", "postgres");
        this.password = System.getProperty("DB_PASSWORD", "123");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Возвращает новое подключение к БД.
     * Каждый вызывающий код обязан закрыть соединение сам
     * (через try-with-resources или явный conn.close()).
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(true);
        return conn;
    }

    /**
     * Применяет настройки из DbConfig.
     * Вызывается из Main перед первым обращением к БД.
     */
    public void configure(DbConfig config) {
        this.url      = config.buildUrl();
        this.username = config.getDbUser();
        this.password = config.getDbPassword();
    }

    public void setTestMode(boolean testMode) {
        if (testMode) {
            this.url = "jdbc:postgresql://localhost:5432/psychotest_test_db";
        } else {
            this.url = System.getProperty("DB_URL", "jdbc:postgresql://localhost:5432/psychotest_db");
        }
    }

    /** Устаревший вспомогательный метод — соединение уже закрывается через try-with-resources. */
    public void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
