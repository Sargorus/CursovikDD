package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TestConstructorController;
import main.java.com.psychotest.service.TestDraftService;
import main.java.com.psychotest.view.panels.TestInfoPanel;
import main.java.com.psychotest.view.panels.ParametersPanel;
import main.java.com.psychotest.view.panels.QuestionsPanel;
import main.java.com.psychotest.view.panels.ReviewPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

public class TestConstructorDialog extends JDialog {
    private JTabbedPane tabbedPane;
    private TestInfoPanel infoPanel;
    private ParametersPanel paramsPanel;
    private QuestionsPanel questionsPanel;
    private ReviewPanel reviewPanel;

    private TestConstructorController controller;
    private TestDraftService draftService;
    private int teacherId;
    private Timer autoSaveTimer;

    private JButton prevButton;
    private JButton nextButton;
    private JButton saveDraftButton;
    private JButton cancelButton;

    public TestConstructorDialog(Window parent, int teacherId) {
        super(parent, "Конструктор тестов", ModalityType.APPLICATION_MODAL);
        this.teacherId = teacherId;
        this.controller = new TestConstructorController(teacherId);
        this.draftService = new TestDraftService();

        initComponents();
        setupAutoSave();
        setupWindowListener();

        setSize(900, 700);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Создаём панели для каждого шага - ПЕРЕДАЁМ ТОЛЬКО КОНТРОЛЛЕР
        infoPanel = new TestInfoPanel(controller);
        paramsPanel = new ParametersPanel(controller);
        questionsPanel = new QuestionsPanel(controller);
        reviewPanel = new ReviewPanel(controller);

        // Создаём вкладки
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("1. Информация о тесте", infoPanel);
        tabbedPane.addTab("2. Параметры (шкалы)", paramsPanel);
        tabbedPane.addTab("3. Вопросы и ответы", questionsPanel);
        tabbedPane.addTab("4. Проверка и сохранение", reviewPanel);

        // Запрещаем переключение вкладок мышью (только кнопками)
        tabbedPane.setEnabled(false);

        add(tabbedPane, BorderLayout.CENTER);

        // Панель навигации
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.SOUTH);

        // Обновляем состояние кнопок при загрузке
        updateNavigationButtons();
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        prevButton = new JButton("◀ Назад");
        nextButton = new JButton("Далее ▶");
        saveDraftButton = new JButton("💾 Сохранить черновик");
        cancelButton = new JButton("Отмена");

        prevButton.addActionListener(e -> previousStep());
        nextButton.addActionListener(e -> nextStep());
        saveDraftButton.addActionListener(e -> saveDraft());
        cancelButton.addActionListener(e -> cancel());

        // Стилизация кнопок
        styleButton(prevButton, new Color(100, 100, 100));
        styleButton(nextButton, new Color(70, 130, 200));
        styleButton(saveDraftButton, new Color(34, 139, 34));
        styleButton(cancelButton, new Color(178, 34, 34));

        panel.add(prevButton);
        panel.add(nextButton);
        panel.add(saveDraftButton);
        panel.add(cancelButton);

        return panel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 35));
    }

    private void updateNavigationButtons() {
        int currentStep = controller.getCurrentStep();
        prevButton.setEnabled(currentStep > 0);

        if (currentStep == 3) {
            nextButton.setText("✅ Сохранить тест");
        } else {
            nextButton.setText("Далее ▶");
        }
    }

    private void previousStep() {
        int currentStep = controller.getCurrentStep();
        if (currentStep > 0) {
            controller.setCurrentStep(currentStep - 1);
            tabbedPane.setSelectedIndex(currentStep - 1);
            updateNavigationButtons();

            // Обновляем предпросмотр если возвращаемся с последнего шага
            if (currentStep == 3) {
                reviewPanel.updateReview();
            }
        }
    }

    private void nextStep() {
        // Валидация текущего шага
        if (!validateCurrentStep()) {
            return;
        }

        int currentStep = controller.getCurrentStep();

        if (currentStep == 3) {
            // Сохраняем тест
            saveTest();
        } else {
            controller.setCurrentStep(currentStep + 1);
            tabbedPane.setSelectedIndex(currentStep + 1);
            updateNavigationButtons();

            // Обновляем предпросмотр если переходим на последний шаг
            if (currentStep + 1 == 3) {
                reviewPanel.updateReview();
            }
        }
    }

    private boolean validateCurrentStep() {
        int step = controller.getCurrentStep();

        switch (step) {
            case 0: // Информация о тесте
                if (controller.getTestName().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Введите название теста!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case 1: // Параметры
                if (controller.getParameters().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Добавьте хотя бы один параметр (шкалу)!\n\n" +
                                    "Пример: EI (экстраверсия/интроверсия)",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case 2: // Вопросы
                if (controller.getQuestions().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Добавьте хотя бы один вопрос!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
        }
        return true;
    }

    private void saveDraft() {
        if (controller.saveDraft()) {
            JOptionPane.showMessageDialog(this,
                    "Черновик сохранён!\n\n" +
                            "Вы можете закрыть конструктор и продолжить позже.",
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при сохранении черновика!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTest() {
        if (!controller.validateTest().isValid()) {
            JOptionPane.showMessageDialog(this,
                    "Тест не готов к сохранению!\n\nПроверьте:\n• Название теста\n• Наличие параметров\n• Наличие вопросов",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Сохранить тест \"" + controller.getTestName() + "\"?",
                "Подтверждение сохранения",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int testId = controller.saveTestToDatabase();
                JOptionPane.showMessageDialog(this,
                        "Тест успешно сохранён!\nID теста: " + testId,
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при сохранении теста:\n" + e.getMessage(),
                        "Ошибка БД", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void cancel() {
        boolean hasDraft = !controller.getTestName().isEmpty() ||
                !controller.getParameters().isEmpty() ||
                !controller.getQuestions().isEmpty();

        if (hasDraft) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "У вас есть несохранённые изменения.\n\n" +
                            "Хотите сохранить черновик перед выходом?",
                    "Подтверждение выхода",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                saveDraft();
                dispose();
            } else if (confirm == JOptionPane.NO_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
    }

    private void setupAutoSave() {
        // Автосохранение каждые 30 секунд
        autoSaveTimer = new Timer(30000, e -> {
            if (!controller.getTestName().isEmpty() ||
                    !controller.getParameters().isEmpty() ||
                    !controller.getQuestions().isEmpty()) {
                controller.saveDraft();
            }
        });
        autoSaveTimer.start();
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                autoSaveTimer.stop();
                // Последнее автосохранение перед закрытием
                if (!controller.getTestName().isEmpty() ||
                        !controller.getParameters().isEmpty() ||
                        !controller.getQuestions().isEmpty()) {
                    controller.saveDraft();
                }
            }
        });
    }
}
