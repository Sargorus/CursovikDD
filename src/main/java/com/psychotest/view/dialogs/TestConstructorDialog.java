package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.controller.TestConstructorController;
import main.java.com.psychotest.model.TestState;
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

    /** Конструктор для создания нового теста */
    public TestConstructorDialog(Window parent, int teacherId) {
        super(parent, "Конструктор тестов", ModalityType.APPLICATION_MODAL);
        this.teacherId = teacherId;
        this.draftService = new TestDraftService();

        // Если на диске есть старый черновик — спрашиваем пользователя
        if (draftService.hasDraft(teacherId)) {
            int choice = JOptionPane.showConfirmDialog(
                    parent,
                    "Найден несохранённый черновик теста.\nПродолжить с черновиком?",
                    "Черновик найден",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                draftService.clearDraft(teacherId);  // удаляем черновик, откроем пустой конструктор
            }
        }

        this.controller = new TestConstructorController(teacherId);

        initComponents();
        setupAutoSave();
        setupWindowListener();

        setSize(900, 700);
        setLocationRelativeTo(parent);
    }

    /** Конструктор для редактирования существующего теста */
    public TestConstructorDialog(Window parent, int teacherId, int editingTestId, TestState existingState) {
        super(parent, "Редактирование теста", ModalityType.APPLICATION_MODAL);
        this.teacherId = teacherId;
        this.controller = new TestConstructorController(teacherId, editingTestId, existingState);
        this.draftService = new TestDraftService();

        initComponents();
        // В режиме редактирования автосохранение черновика не нужно
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

        // В режиме редактирования черновик не нужен — скрываем кнопку
        saveDraftButton.setVisible(!controller.isEditMode());

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

        // ── Режим создания нового теста: простой вопрос ───────────────────────
        if (!controller.isEditMode()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Сохранить тест «" + controller.getTestName() + "»?",
                    "Подтверждение сохранения",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                doSave(false);
            }
            return;
        }

        // ── Режим редактирования: три варианта ───────────────────────────────
        int existingResults = controller.countExistingResults();

        String details = (existingResults > 0)
                ? "У теста есть " + existingResults + " завершённых результатов.\n\n"
                  + "• «Заменить» удалит все результаты (структура изменилась).\n"
                  + "• «Новый тест» создаст копию — старые результаты сохранятся.\n\n"
                : "Выберите, что сделать с изменениями:\n\n";

        String[] options = {
                "✏  Заменить существующий",
                "📋  Сохранить как новый тест",
                "Отмена"
        };

        int choice = JOptionPane.showOptionDialog(this,
                "Тест: «" + controller.getTestName() + "»\n\n" + details,
                "Сохранение изменений",
                JOptionPane.YES_NO_CANCEL_OPTION,
                existingResults > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);   // кнопка по умолчанию — «Сохранить как новый»

        if (choice == 0) {
            // Заменить существующий — дополнительное подтверждение при наличии результатов
            if (existingResults > 0) {
                int warn = JOptionPane.showConfirmDialog(this,
                        "⚠  Все " + existingResults + " результатов будут БЕЗВОЗВРАТНО УДАЛЕНЫ.\n\n"
                        + "Старые ответы участников несовместимы с новой структурой теста.\n"
                        + "Продолжить замену?",
                        "Подтверждение удаления результатов",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (warn != JOptionPane.YES_OPTION) return;
            }
            doSave(false);

        } else if (choice == 1) {
            // Сохранить как новый тест
            doSave(true);
        }
        // choice == 2 (Отмена) или окно закрыто — ничего не делаем
    }

    /**
     * Выполняет сохранение и показывает сообщение об успехе.
     *
     * @param asNew {@code true} — создать новый тест (старый остаётся нетронутым);
     *              {@code false} — заменить существующий (или создать при первом сохранении)
     */
    private void doSave(boolean asNew) {
        try {
            int testId;
            String successMsg;

            if (asNew) {
                testId = controller.saveAsNewTest();
                successMsg = "Тест успешно сохранён как новый!\n"
                           + "ID нового теста: " + testId + "\n\n"
                           + "Старый тест и все его результаты остались в базе данных.";
            } else {
                testId = controller.saveTestToDatabase();
                successMsg = controller.isEditMode()
                        ? "Тест успешно обновлён!\nID теста: " + testId
                        : "Тест успешно сохранён!\nID теста: " + testId;
            }

            JOptionPane.showMessageDialog(this,
                    successMsg, "Успех", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при сохранении теста:\n" + e.getMessage(),
                    "Ошибка БД", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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
                // autoSaveTimer == null в режиме редактирования (setupAutoSave не вызывается)
                if (autoSaveTimer != null) {
                    autoSaveTimer.stop();
                    // Последнее автосохранение перед закрытием (только в режиме создания)
                    if (!controller.getTestName().isEmpty() ||
                            !controller.getParameters().isEmpty() ||
                            !controller.getQuestions().isEmpty()) {
                        controller.saveDraft();
                    }
                }
            }
        });
    }
}
