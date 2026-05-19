package main.java.com.psychotest.view;
import main.java.com.psychotest.model.User;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private User currentUser;
    private JMenuBar menuBar;
    private JMenu fileMenu, testMenu, userMenu, reportMenu;
    private JMenuItem exitItem, createTestItem, myTestsItem, assignTestItem,
            viewResultsItem, manageUsersItem, manageGroupsItem;
    private JLabel welcomeLabel;
    private JPanel contentPanel;

    public MainWindow(User user) {
        this.currentUser = user;
        initComponents();
        setupLayout();
        setupMenu();
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
        exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

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
        manageUsersItem = new JMenuItem("Управление пользователями");
        manageGroupsItem = new JMenuItem("Управление группами");
        userMenu.add(manageUsersItem);
        userMenu.add(manageGroupsItem);
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

    // Геттеры для контроллера
    public User getCurrentUser() { return currentUser; }
    public JPanel getContentPanel() { return contentPanel; }

    public JMenuItem getExitItem() { return exitItem; }
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
