package main.java.com.psychotest.view;

import main.java.com.psychotest.model.User;
import javax.swing.*;
import java.awt.*;

public class TakerFrame extends JFrame {
    private User currentUser;

    public TakerFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("PsychoTest - Тестирование");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Добро пожаловать, " + currentUser.getFullName() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton availableTestsButton = new JButton("Доступные тесты");
        availableTestsButton.setFont(new Font("Arial", Font.PLAIN, 16));
        availableTestsButton.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Список доступных тестов будет отображаться здесь"));

        JButton myResultsButton = new JButton("Мои результаты");
        myResultsButton.setFont(new Font("Arial", Font.PLAIN, 16));
        myResultsButton.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "История результатов будет отображаться здесь"));

        JButton exitButton = new JButton("Выход");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 16));
        exitButton.addActionListener(e -> System.exit(0));

        centerPanel.add(availableTestsButton);
        centerPanel.add(myResultsButton);
        centerPanel.add(exitButton);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
}