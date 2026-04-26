package view;
import java.awt.*;
import javax.swing.*;

public class MainWindow extends JFrame {
    private JLabel welcomeLabel;
    private JButton logoutButton;

    public MainWindow(String username) {
        super("Главное окно");
        setSize(400, 300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Панель с приветствием
        JPanel topPanel = new JPanel();
        welcomeLabel = new JLabel("Добро пожаловать, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(75, 0, 130));
        topPanel.add(welcomeLabel);
        add(topPanel, BorderLayout.CENTER);

        // Панель с кнопкой выхода
        JPanel bottomPanel = new JPanel();
        logoutButton = new JButton("Выйти");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBackground(new Color(75, 0, 130));
        logoutButton.setForeground(Color.WHITE);
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    public void close() {
        this.dispose();
    }
}
