package main.java.com.psychotest.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;
    private boolean isTestMode = false;

    private DatabaseConnection() {
        // Для тестирования можно использовать переменные окружения или properties
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

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("✓ Connected to PostgreSQL database!");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("✗ Connection failed: " + e.getMessage());
            throw e;
        }
    }

    public void setTestMode(boolean testMode) {
        this.isTestMode = testMode;
        if (testMode) {
            this.url = "jdbc:postgresql://localhost:5432/psychotest_test_db";
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
