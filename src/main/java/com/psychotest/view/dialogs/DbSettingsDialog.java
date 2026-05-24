package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.util.DbConfig;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Диалог настройки подключения к БД.
 * Показывается при первом запуске или при ошибке подключения.
 * Содержит форму с полями и кнопкой "Проверить подключение".
 */
public class DbSettingsDialog extends JDialog {

    private JTextField    hostField;
    private JSpinner      portSpinner;
    private JTextField    dbNameField;
    private JTextField    userField;
    private JPasswordField passwordField;
    private JLabel        statusLabel;

    private DbConfig result = null;

    /**
     * @param parent        родительское окно (может быть null при запуске до главного окна)
     * @param currentConfig текущие настройки для предзаполнения (null → используются defaults)
     * @param errorMessage  сообщение об ошибке (null → без ошибки)
     */
    public DbSettingsDialog(Window parent, DbConfig currentConfig, String errorMessage) {
        super(parent, "Настройки подключения к базе данных",
                parent != null ? ModalityType.APPLICATION_MODAL : ModalityType.TOOLKIT_MODAL);

        DbConfig cfg = (currentConfig != null) ? currentConfig : DbConfig.defaults();
        initComponents(cfg, errorMessage);

        setSize(480, errorMessage != null ? 420 : 390);
        setLocationRelativeTo(parent);
        setResizable(false);
        // Запрещаем закрытие по крестику — пользователь должен явно нажать "Выход"
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    private void initComponents(DbConfig cfg, String errorMessage) {
        setLayout(new BorderLayout(10, 10));

        // ---- Верхняя часть: заголовок + ошибка ----
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JLabel titleLabel = new JLabel("🗄️  Подключение к PostgreSQL");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        if (errorMessage != null) {
            JLabel errLabel = new JLabel(
                    "<html><font color='red'>⚠&nbsp;" + escapeHtml(errorMessage) + "</font></html>");
            errLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            topPanel.add(errLabel, BorderLayout.CENTER);
        }

        add(topPanel, BorderLayout.NORTH);

        // ---- Центральная часть: форма ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        hostField   = new JTextField(cfg.getHost(), 22);
        portSpinner = new JSpinner(new SpinnerNumberModel(cfg.getPort(), 1, 65535, 1));
        ((JSpinner.DefaultEditor) portSpinner.getEditor()).getTextField().setColumns(7);
        dbNameField   = new JTextField(cfg.getDbName(), 22);
        userField     = new JTextField(cfg.getDbUser(), 22);
        passwordField = new JPasswordField(cfg.getDbPassword(), 22);

        int row = 0;
        addRow(form, gbc, row++, "Хост (IP или имя):",    hostField);
        addRow(form, gbc, row++, "Порт:",                  portSpinner);
        addRow(form, gbc, row++, "Имя базы данных:",       dbNameField);
        addRow(form, gbc, row++, "Пользователь БД:",       userField);
        addRow(form, gbc, row++, "Пароль:",                passwordField);

        JLabel hint = new JLabel(
                "<html><font color='gray' size='2'>Файл настроек сохраняется в db.properties рядом с приложением.</font></html>");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        form.add(hint, gbc);
        gbc.gridwidth = 1;

        add(form, BorderLayout.CENTER);

        // ---- Нижняя часть: статус + кнопки ----
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton testBtn = new JButton("🔌 Проверить подключение");
        testBtn.addActionListener(e -> testConnection());

        JButton saveBtn = new JButton("✅ Сохранить и продолжить");
        saveBtn.setBackground(new Color(34, 139, 34));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveAndClose());

        JButton exitBtn = new JButton("Выход из приложения");
        exitBtn.addActionListener(e -> System.exit(0));

        btnPanel.add(testBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(exitBtn);
        bottomPanel.add(btnPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private DbConfig buildConfig() {
        return new DbConfig(
                hostField.getText().trim(),
                (int) portSpinner.getValue(),
                dbNameField.getText().trim(),
                userField.getText().trim(),
                new String(passwordField.getPassword())
        );
    }

    /** Пробует соединение в фоновом потоке и обновляет statusLabel */
    private void testConnection() {
        DbConfig cfg = buildConfig();
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setText("⏳ Проверка подключения...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    Class.forName("org.postgresql.Driver");
                    try (Connection conn = DriverManager.getConnection(
                            cfg.buildUrl(), cfg.getDbUser(), cfg.getDbPassword())) {
                        return null; // успех
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        statusLabel.setForeground(new Color(0, 128, 0));
                        statusLabel.setText("✅ Подключение успешно!");
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("❌ " + truncate(error, 72));
                    }
                } catch (Exception e) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("❌ " + e.getMessage());
                }
            }
        }.execute();
    }

    private void saveAndClose() {
        DbConfig cfg = buildConfig();

        if (cfg.getHost().isEmpty() || cfg.getDbName().isEmpty() || cfg.getDbUser().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Заполните все поля: хост, имя БД и пользователь обязательны.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            cfg.save();
            result = cfg;
            dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Не удалось сохранить файл настроек:\n" + e.getMessage(),
                    "Ошибка записи", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------- утилиты --------

    private String truncate(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max) + "...";
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Результат после закрытия диалога: DbConfig если пользователь нажал "Сохранить", иначе null */
    public DbConfig getResult() { return result; }
}
