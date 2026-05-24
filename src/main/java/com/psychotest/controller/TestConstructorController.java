package main.java.com.psychotest.controller;

import main.java.com.psychotest.exception.InvalidRangeException;
import main.java.com.psychotest.model.*;
import main.java.com.psychotest.dao.TestDAO;
import main.java.com.psychotest.service.TestPersistenceService;
import main.java.com.psychotest.service.TestValidationService;
import main.java.com.psychotest.service.TestDraftService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TestConstructorController {
    private TestState testState;
    private TestValidationService validator;
    private TestDraftService draftService;
    private int teacherId;
    /** ID редактируемого теста, или -1 если создаём новый */
    private int editingTestId = -1;
    private List<ModelChangeListener> listeners = new ArrayList<>();

    /** Конструктор для создания нового теста (загружает черновик при наличии) */
    public TestConstructorController(int teacherId) {
        this.teacherId = teacherId;
        this.validator = new TestValidationService();
        this.draftService = new TestDraftService();
        this.testState = draftService.loadLastDraft(teacherId);
    }

    /** Конструктор для редактирования существующего теста */
    public TestConstructorController(int teacherId, int editingTestId, TestState existingState) {
        this.teacherId = teacherId;
        this.editingTestId = editingTestId;
        this.validator = new TestValidationService();
        this.draftService = new TestDraftService();
        this.testState = existingState;
    }

    public boolean isEditMode() {
        return editingTestId > 0;
    }

    public int getEditingTestId() {
        return editingTestId;
    }

    // ========== Наблюдатель для обновления View ==========
    public interface ModelChangeListener {
        void onModelChanged();
    }

    public void addListener(ModelChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyModelChanged() {
        for (ModelChangeListener listener : listeners) {
            listener.onModelChanged();
        }
    }

    // ========== Геттеры для View (только чтение) ==========
    public String getTestName() { return testState.getTestName(); }
    public String getTestDescription() { return testState.getTestDescription(); }
    public List<Parameter> getParameters() { return new ArrayList<>(testState.getParameters()); }
    public List<Question> getQuestions() { return new ArrayList<>(testState.getQuestions()); }
    public int getQuestionsPerSession() { return testState.getQuestionsPerSession(); }
    public int getCurrentStep() { return testState.getCurrentStep(); }

    public TestValidationService.ValidationResult validateTest() {
        return validator.validate(testState);
    }

    // ========== Команды для изменения модели ==========
    public void setTestName(String name) {
        testState.setTestName(name);
        notifyModelChanged();
    }

    public void setTestDescription(String description) {
        testState.setTestDescription(description);
        notifyModelChanged();
    }

    public void setCurrentStep(int step) {
        testState.setCurrentStep(step);
        notifyModelChanged();
    }

    public void addParameter(String name, String scaleType, List<ParameterInterpretation> interpretations) {
        Parameter param = new Parameter();
        param.setName(name);
        param.setScaleType(scaleType);
        param.setInterpretations(interpretations);
        testState.addParameter(param);
        notifyModelChanged();
    }

    public void removeParameter(int index) {
        if (index < 0 || index >= testState.getParameters().size()) {
            return;
        }
        testState.removeParameter(index);

        // Корректируем индексы параметров во всех ответах
        for (Question q : testState.getQuestions()) {
            for (AnswerOption opt : q.getAnswerOptions()) {
                Map<Integer, Integer> newImpacts = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : opt.getParameterImpacts().entrySet()) {
                    int oldIndex = entry.getKey();
                    int delta = entry.getValue();
                    if (oldIndex > index) {
                        newImpacts.put(oldIndex - 1, delta);
                    } else if (oldIndex < index) {
                        newImpacts.put(oldIndex, delta);
                    }
                }
                opt.setParameterImpacts(newImpacts);
            }
        }
        notifyModelChanged();
    }

    public int getParameterUsageCount(int paramIndex) {
        int count = 0;
        for (Question q : testState.getQuestions()) {
            for (AnswerOption opt : q.getAnswerOptions()) {
                if (opt.getParameterImpacts().containsKey(paramIndex)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    public void validateNewRange(Parameter param, int min, int max) throws InvalidRangeException {
        validator.validateNewRange(param, min, max);
    }

    public void addQuestion(String text, List<AnswerOptionData> answers) {
        Question question = new Question();
        question.setText(text);
        question.setOrderNum(testState.getQuestions().size());

        for (AnswerOptionData data : answers) {
            AnswerOption option = new AnswerOption();
            option.setText(data.text);
            option.setOrderNum(data.orderNum);
            for (var entry : data.impacts.entrySet()) {
                option.addParameterImpact(entry.getKey(), entry.getValue());
            }
            question.addAnswerOption(option);
        }

        testState.addQuestion(question);
        notifyModelChanged();
    }

    public void removeQuestion(int index) {
        testState.removeQuestion(index);
        notifyModelChanged();
    }

    public void setQuestionsPerSession(int count) {
        testState.setQuestionsPerSession(count);
        notifyModelChanged();
    }

    // ========== DTO для передачи данных ответа ==========
    public static class AnswerOptionData {
        public String text;
        public int orderNum;
        public Map<Integer, Integer> impacts = new HashMap<>();

        public AnswerOptionData(String text, int orderNum) {
            this.text = text;
            this.orderNum = orderNum;
        }

        public void addImpact(int paramIndex, int delta) {
            impacts.put(paramIndex, delta);
        }
    }

    // ========== Работа с черновиками ==========
    public boolean saveDraft() {
        return draftService.saveDraft(teacherId, testState);
    }

    public void clearDraft() {
        draftService.clearDraft(teacherId);
    }

    // ========== Сохранение в БД ==========

    /**
     * Сохраняет тест в БД.
     * В режиме редактирования заменяет содержимое существующего теста и возвращает его ID.
     * При создании нового — создаёт запись и возвращает новый ID.
     */
    public int saveTestToDatabase() throws SQLException {
        if (isEditMode()) {
            TestDAO testDAO = new TestDAO();
            testDAO.replaceTestContent(editingTestId, testState);
            // В режиме редактирования черновик не трогаем
            return editingTestId;
        } else {
            TestPersistenceService persistenceService = new TestPersistenceService();
            int testId = persistenceService.saveTest(testState, teacherId);
            draftService.clearDraft(teacherId);
            return testId;
        }
    }
}