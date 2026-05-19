package main.java.com.psychotest.view;

import main.java.com.psychotest.model.User;
import main.java.com.psychotest.view.panels.GroupsPanel;
import main.java.com.psychotest.view.panels.ResultsViewPanel;
import main.java.com.psychotest.view.panels.UsersPanel;
import javax.swing.*;
import java.awt.*;
import main.java.com.psychotest.controller.AdminController;
import main.java.com.psychotest.controller.TeacherController;
import main.java.com.psychotest.view.panels.MyTestsPanel;

public class MainWindow extends JFrame {
    private User currentUser;
    private JMenuBar menuBar;
    private JMenu fileMenu, testMenu, userMenu, reportMenu, accountMenu;
    private JMenuItem exitItem, logoutItem, createTestItem, myTestsItem, assignTestItem,
            viewResultsItem, manageUsersItem, manageGroupsItem;
    private JLabel welcomeLabel;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane;
    private TeacherController teacherController;

    public MainWindow(User user) {
        this.currentUser = user;
        initComponents();
        setupLayout();
        setupMenu();

        tabbedPane = new JTabbedPane();

        if (currentUser.getRole().equals("ADMIN")) {
            setupAdminPanels();
        } else if (currentUser.getRole().equals("TEACHER")) {
            this.teacherController = new TeacherController(currentUser.getId());
            setupTeacherPanels();
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

    private void setupMenu() {
        menuBar = new JMenuBar();

        // Файл меню
        fileMenu = new JMenu("Файл");
        exitItem = new JMenuItem("Выход из приложения");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Аккаунт меню (для выхода из профиля)
        accountMenu = new JMenu("Аккаунт");
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

        // Создаём вкладки с панелями, передавая контроллер
        JTabbedPane tabbedPane = new JTabbedPane();

        UsersPanel usersPanel = new UsersPanel(adminController);
        tabbedPane.addTab("👥 Пользователи", usersPanel);

        GroupsPanel groupsPanel = new GroupsPanel(adminController);
        tabbedPane.addTab("📁 Группы", groupsPanel);

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

        JTabbedPane tabbedPane = new JTabbedPane();

        // Панель "Мои тесты"
        MyTestsPanel myTestsPanel = new MyTestsPanel(teacherController);
        tabbedPane.addTab("📋 Мои тесты", myTestsPanel);

        // Панель "Результаты"
        ResultsViewPanel resultsPanel = new ResultsViewPanel(teacherController);
        tabbedPane.addTab("📊 Результаты", resultsPanel);

        // Панель "Назначения" (опционально, позже)
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

    public void setContentPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }


}