package main.java.com.psychotest.util;

import java.io.*;
import java.util.Properties;

/**
 * Хранит параметры подключения к БД и умеет читать/писать db.properties
 * рядом с рабочей директорией приложения.
 */
public class DbConfig {

    private static final String CONFIG_FILE = "db.properties";

    private String host;
    private int    port;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    public DbConfig(String host, int port, String dbName, String dbUser, String dbPassword) {
        this.host       = host;
        this.port       = port;
        this.dbName     = dbName;
        this.dbUser     = dbUser;
        this.dbPassword = dbPassword;
    }

    /** Строка JDBC URL */
    public String buildUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    // -------- геттеры --------
    public String getHost()       { return host; }
    public int    getPort()       { return port; }
    public String getDbName()     { return dbName; }
    public String getDbUser()     { return dbUser; }
    public String getDbPassword() { return dbPassword; }

    // -------- ввод-вывод --------

    /**
     * Загружает конфигурацию из db.properties.
     * @return DbConfig или null если файл не найден / повреждён
     */
    public static DbConfig load() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return null;

        Properties props = new Properties();
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
            String host   = props.getProperty("db.host",     "localhost");
            int    port   = Integer.parseInt(props.getProperty("db.port", "5432"));
            String dbName = props.getProperty("db.name",     "psychotest_db");
            String dbUser = props.getProperty("db.user",     "postgres");
            String dbPass = props.getProperty("db.password", "");
            return new DbConfig(host, port, dbName, dbUser, dbPass);
        } catch (Exception e) {
            System.err.println("Не удалось загрузить db.properties: " + e.getMessage());
            return null;
        }
    }

    /**
     * Сохраняет конфигурацию в db.properties.
     */
    public void save() throws IOException {
        Properties props = new Properties();
        props.setProperty("db.host",     host);
        props.setProperty("db.port",     String.valueOf(port));
        props.setProperty("db.name",     dbName);
        props.setProperty("db.user",     dbUser);
        props.setProperty("db.password", dbPassword);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            props.store(writer, "PsychoTest — Database Configuration");
        }
    }

    /** Возвращает конфигурацию по умолчанию для первого запуска */
    public static DbConfig defaults() {
        return new DbConfig("localhost", 5432, "psychotest_db", "postgres", "");
    }

    public static boolean configFileExists() {
        return new File(CONFIG_FILE).exists();
    }

    @Override
    public String toString() {
        return dbUser + "@" + host + ":" + port + "/" + dbName;
    }
}
