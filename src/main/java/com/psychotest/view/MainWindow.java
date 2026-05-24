package main.java.com.psychotest.view;

import main.java.com.psychotest.controller.AdminController;
import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.controller.TakerController;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.view.panels.*;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private User currentUser;
    private JMenuBar menuBar;
    private JMenu fileMenu, testMenu, userMenu, reportMenu, accountMenu;
    private JMenuItem exitItem, logoutItem, createTestItem, myTestsItem, assignTestItem,
            viewResultsItem, manageUsersItem, manageGroupsItem;
    private JLabel welcomeLabel;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane;
    private JPanel statusBar;
    private JLabel statusLabel;

    private TeacherController teacherController;
    private AllTestsPanel allTestsPanel;
    private ResultsViewPanel resultsPanel;
    private TakerController takerController;
    private ParticipantsPanel participantsPanel;
    private UserTestsPanel userTestsPanel;

    public MainWindow(User user) {
        this.currentUser = user;
        initComponents();
        setupLayout();
        setupMenu();
        setupStatusBar();

        if (currentUser.getRole().equals("ADMIN")) {
            setupAdminPanels();
        } else if (currentUser.getRole().equals("TEACHER")) {
            this.teacherController = new TeacherController(currentUser.getId());
            setupTeacherPanels();
        } else if (currentUser.getRole().equals("TAKER")) {
            this.takerController = new TakerController(currentUser.getId());
            setupTakerPanels();
        }
    }

    private String getRoleName(String role) {
        switch (role) {
            case "ADMIN": return "Администратор";
            case "TEACHER": return "Преподаватель";
            case "TAKER": return "Тестируемый";
            default: return role;
        }
    }

    private void initComponents() {
        setTitle("PsychoTest - Главное окно");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        welcomeLabel = new JLabel("Добро пожаловать, " + currentUser.getFullName() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(welcomeLabel, BorderLayout.CENTER);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    // СОЗДАНИЕ СТАТУСНОЙ СТРОКИ
    private void setupStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setBackground(new Color(240, 240, 240));
        statusBar.setPreferredSize(new Dimension(getWidth(), 30));

        // Левая часть - информация о пользователе
        // Используем существующий метод getRoleName() из класса MainWindow
        String roleName = getRoleName(currentUser.getRole());  // ← этот метод уже есть в классе
        statusLabel = new JLabel("  👤 " + currentUser.getFullName() + " | Роль: " + roleName + " | Логин: " + currentUser.getUsername());
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusBar.add(statusLabel, BorderLayout.WEST);

        // Правая часть - текущая дата и время
        JLabel dateLabel = new JLabel(getCurrentDateTime() + "  ");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        dateLabel.setForeground(Color.GRAY);
        statusBar.add(dateLabel, BorderLayout.EAST);

        // Таймер для обновления времени
        new Timer(1000, e -> {
            dateLabel.setText(getCurrentDateTime() + "  ");
        }).start();

        add(statusBar, BorderLayout.SOUTH);
    }

    private String getCurrentDateTime() {
        return java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        );
    }

    private void setupMenu() {
        menuBar = new JMenuBar();

        // Файл меню
        fileMenu = new JMenu("Файл");
        exitItem = new JMenuItem("Выход из приложения");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Аккаунт меню
        accountMenu = new JMenu("Аккаунт");

        JMenuItem userInfoItem = new JMenuItem("Информация о пользователе");
        userInfoItem.addActionListener(e -> showUserInfo());
        accountMenu.add(userInfoItem);

        accountMenu.addSeparator();

        logoutItem = new JMenuItem("Выйти из профиля");
        logoutItem.addActionListener(e -> logout());
        accountMenu.add(logoutItem);

        menuBar.add(accountMenu);

        // В зависимости от роли добавляем разные меню
        switch (currentUser.getRole()) {
            case "ADMIN":
                setupAdminMenu();
                break;
            case "TEACHER":
                setupTeacherMenu();
                break;
            case "TAKER":
                setupTakerMenu();
                break;
        }

        setJMenuBar(menuBar);
    }

    // МЕТОД ДЛЯ ПОКАЗА ИНФОРМАЦИИ О ПОЛЬЗОВАТЕЛЕ
    private void showUserInfo() {
        String message = String.format(
                "═══════════════════════════════════════\n" +
                        "        ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ        \n" +
                        "═══════════════════════════════════════\n\n" +
                        "👤 ФИО: %s\n" +
                        "🔑 Логин: %s\n" +
                        "⭐ Роль: %s\n" +
                        "🆔 ID: %d\n" +
                        "📅 Дата регистрации: %s\n" +
                        "═══════════════════════════════════════",
                currentUser.getFullName(),
                currentUser.getUsername(),
                getRoleName(currentUser.getRole()),
                currentUser.getId(),
                currentUser.getCreatedAt() != null ?
                        currentUser.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) :
                        "Неизвестно"
        );
        JOptionPane.showMessageDialog(this, message, "Информация о пользователе", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupAdminMenu() {
        userMenu = new JMenu("Управление");
        manageUsersItem = new JMenuItem("Управление пользователями и группами");
        manageUsersItem.addActionListener(e -> setupAdminPanels());
        userMenu.add(manageUsersItem);
        menuBar.add(userMenu);
    }

    private void setupTeacherMenu() {
        testMenu = new JMenu("Тесты");
        createTestItem = new JMenuItem("Создать тест");
        myTestsItem = new JMenuItem("Мои тесты");
        assignTestItem = new JMenuItem("Назначить тест");
        testMenu.add(createTestItem);
        testMenu.add(myTestsItem);
        testMenu.add(assignTestItem);

        reportMenu = new JMenu("Отчеты");
        viewResultsItem = new JMenuItem("Просмотр результатов");
        reportMenu.add(viewResultsItem);

        menuBar.add(testMenu);
        menuBar.add(reportMenu);
    }

    private void setupTakerMenu() {
        testMenu = new JMenu("Тестирование");
        myTestsItem = new JMenuItem("Доступные тесты");
        viewResultsItem = new JMenuItem("Мои результаты");
        testMenu.add(myTestsItem);
        testMenu.add(viewResultsItem);
        menuBar.add(testMenu);
    }

    private void setupAdminPanels() {
        contentPanel.removeAll();

        // Создаём контроллер для администратора
        AdminController adminController = new AdminController();

        // Создаём вкладки с панелями
        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка "Пользователи"
        UsersPanel usersPanel = new UsersPanel(adminController);
        tabbedPane.addTab("👥 Пользователи", usersPanel);

        // Вкладка "Группы"
        GroupsPanel groupsPanel = new GroupsPanel(adminController);
        tabbedPane.addTab("📁 Группы", groupsPanel);

        // "ТЕСТЫ" (для администратора)
        AllTestsPanel testsPanel = new AllTestsPanel(adminController);
        tabbedPane.addTab("📋 Тесты", testsPanel);

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Выход из профиля (возврат на окно входа)
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите выйти из профиля?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose(); // Закрываем главное окно

            // Открываем окно входа заново
            javax.swing.SwingUtilities.invokeLater(() -> {
                main.java.com.psychotest.view.EntryWindow entryWindow =
                        new main.java.com.psychotest.view.EntryWindow();
                new main.java.com.psychotest.controller.LoginController(entryWindow);
                entryWindow.setVisible(true);
            });
        }
    }

    // Полный выход из приложения
    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите выйти из приложения?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // Метод для отображения панелей преподавателя
    private void setupTeacherPanels() {
        contentPanel.removeAll();

        JTabbedPane mainTabbedPane = new JTabbedPane();

        // Вкладка "Мои тесты"
        allTestsPanel = new AllTestsPanel(teacherController);
        allTestsPanel.setOnTestSelectedListener((testId, testName) -> {
            if (resultsPanel != null) {
                resultsPanel.selectTest(testId, testName);
                // Переключаемся на вкладку результатов
                for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
                    if (mainTabbedPane.getComponentAt(i) == resultsPanel) {
                        mainTabbedPane.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
        mainTabbedPane.addTab("📋 Тесты", allTestsPanel);

        // Вкладка "Участники" (новая)
        ParticipantsPanel participantsPanel = new ParticipantsPanel(teacherController);
        participantsPanel.setOnParticipantSelectedListener((userId, userName) -> {
            userTestsPanel.setUser(userId, userName);
            // Переключаемся на вкладку тестов участника
            for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
                if (mainTabbedPane.getComponentAt(i) == userTestsPanel) {
                    mainTabbedPane.setSelectedIndex(i);
                    break;
                }
            }
        });
        mainTabbedPane.addTab("👥 Участники", participantsPanel);

        // Вкладка "Тесты участника" (новая)
        userTestsPanel = new UserTestsPanel(teacherController);
        mainTabbedPane.addTab("📝 Тесты участника", userTestsPanel);

        // Вкладка "Результаты"
        resultsPanel = new ResultsViewPanel(teacherController);
        mainTabbedPane.addTab("📊 Результаты", resultsPanel);

        contentPanel.add(mainTabbedPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void setupTakerPanels() {
        contentPanel.removeAll();

        JTabbedPane tabbedPane = new JTabbedPane();

        // Панель "Доступные тесты"
        AvailableTestsPanel availableTestsPanel = new AvailableTestsPanel(takerController);
        tabbedPane.addTab("📋 Доступные тесты", availableTestsPanel);

        // Панель "Мои результаты"
        MyResultsPanel myResultsPanel = new MyResultsPanel(takerController);
        tabbedPane.addTab("📊 Мои результаты", myResultsPanel);

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setContentComponent(Component component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Геттеры
    public User getCurrentUser() { return currentUser; }
    public JPanel getContentPanel() { return contentPanel; }

    public JMenuItem getExitItem() { return exitItem; }
    public JMenuItem getLogoutItem() { return logoutItem; }
    public JMenuItem getCreateTestItem() { return createTestItem; }
    public JMenuItem getMyTestsItem() { return myTestsItem; }
    public JMenuItem getAssignTestItem() { return assignTestItem; }
    public JMenuItem getViewResultsItem() { return viewResultsItem; }
    public JMenuItem getManageUsersItem() { return manageUsersItem; }
    public JMenuItem getManageGroupsItem() { return manageGroupsItem; }
    public TeacherController getTeacherController() {return teacherController;}
    public void setContentPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }


}