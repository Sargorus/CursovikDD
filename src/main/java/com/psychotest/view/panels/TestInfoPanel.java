package main.java.com.psychotest.view.panels;

import main.java.com.psychotest.controller.TestConstructorController;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class TestInfoPanel extends JPanel {
    private TestConstructorController controller;
    private JTextField nameField;
    private JTextArea descArea;

    public TestInfoPanel(TestConstructorController controller) {
        this.controller = controller;
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Главная панель с формой
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Заголовок
        JLabel headerLabel = new JLabel("Основная информация о тесте");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(headerLabel, gbc);

        // Название теста
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Название теста:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(30);
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(nameField, gbc);

        // Описание теста
        gbc.gridy = 2;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Описание теста:"), gbc);

        gbc.gridx = 1;
        descArea = new JTextArea(8, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(descArea);
        formPanel.add(scrollPane, gbc);

        // Подсказка
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel hintLabel = new JLabel("Совет: Дайте тесту понятное название, чтобы ученики могли легко его найти");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);
        formPanel.add(hintLabel, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void loadData() {
        nameField.setText(controller.getTestName());
        descArea.setText(controller.getTestDescription());

        // Добавляем слушатели для автоматического обновления состояния через контроллер
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            private void update() {
                controller.setTestName(nameField.getText());
            }
        });

        descArea.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            private void update() {
                controller.setTestDescription(descArea.getText());
            }
        });
    }
}
