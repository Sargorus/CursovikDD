package main.java.com.psychotest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Шаг 1: Информация о тесте
    private String testName = "";
    private String testDescription = "";

    // Шаг 2: Параметры
    private List<Parameter> parameters = new ArrayList<>();

    // Шаг 3: Вопросы и ответы (банк вопросов)
    private List<Question> questions = new ArrayList<>();

    // Настройки банка вопросов
    private int questionsPerSession = 0;  // сколько вопросов выдавать в сессии (0 = все вопросы)

    // Калибровка параметров (для масштабирования результатов)
    private Map<String, ParameterCalibration> calibrations = new HashMap<>();

    // Текущий шаг (для возврата в конструкторе)
    private int currentStep = 0;

    // Флаг завершённости
    private boolean completed = false;

    // Конструкторы
    public TestState() {}

    // ========== Геттеры и сеттеры ==========

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getTestDescription() { return testDescription; }
    public void setTestDescription(String testDescription) { this.testDescription = testDescription; }

    public List<Parameter> getParameters() { return parameters; }
    public void setParameters(List<Parameter> parameters) { this.parameters = parameters; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public int getQuestionsPerSession() { return questionsPerSession; }
    public void setQuestionsPerSession(int questionsPerSession) { this.questionsPerSession = questionsPerSession; }

    public Map<String, ParameterCalibration> getCalibrations() { return calibrations; }
    public void setCalibrations(Map<String, ParameterCalibration> calibrations) { this.calibrations = calibrations; }

    public int getCurrentStep() { return currentStep; }
    public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    // ========== Вспомогательные методы ==========

    public void addParameter(Parameter param) {
        this.parameters.add(param);
    }

    public void removeParameter(int index) {
        this.parameters.remove(index);
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public void removeQuestion(int index) {
        this.questions.remove(index);
    }

    public void addCalibration(ParameterCalibration cal) {
        this.calibrations.put(cal.getParamName(), cal);
    }

    public ParameterCalibration getCalibration(String paramName) {
        return calibrations.get(paramName);
    }

    /**
     * Базовая проверка минимальных требований к тесту
     */
    public boolean isValid() {
        return !testName.trim().isEmpty() &&
                !parameters.isEmpty() &&
                !questions.isEmpty();
    }

    /**
     * Проверка, можно ли использовать банк вопросов
     */
    public boolean isQuestionBankEnabled() {
        return questionsPerSession > 0 && questionsPerSession < questions.size();
    }

    /**
     * Получить количество вопросов для сессии
     * Если банк не используется, возвращает общее количество вопросов
     */
    public int getEffectiveQuestionsPerSession() {
        if (isQuestionBankEnabled()) {
            return questionsPerSession;
        }
        return questions.size();
    }

    /**
     * Сброс всех данных (для нового теста)
     */
    public void reset() {
        testName = "";
        testDescription = "";
        parameters.clear();
        questions.clear();
        calibrations.clear();
        questionsPerSession = 0;
        currentStep = 0;
        completed = false;
    }

    // ========== Внутренний класс для калибровки параметра ==========

    public static class ParameterCalibration implements Serializable {
        private static final long serialVersionUID = 1L;

        private String paramName;      // название параметра
        private int targetMin;         // целевой минимум (например, 0)
        private int targetMax;         // целевой максимум (например, 70)
        private int currentMin;        // реальный минимум из банка вопросов
        private int currentMax;        // реальный максимум из банка вопросов
        private double scaleFactor;    // коэффициент масштабирования

        public ParameterCalibration() {}

        public ParameterCalibration(String paramName, int targetMin, int targetMax,
                                    int currentMin, int currentMax, double scaleFactor) {
            this.paramName = paramName;
            this.targetMin = targetMin;
            this.targetMax = targetMax;
            this.currentMin = currentMin;
            this.currentMax = currentMax;
            this.scaleFactor = scaleFactor;
        }

        // Геттеры и сеттеры
        public String getParamName() { return paramName; }
        public void setParamName(String paramName) { this.paramName = paramName; }

        public int getTargetMin() { return targetMin; }
        public void setTargetMin(int targetMin) { this.targetMin = targetMin; }

        public int getTargetMax() { return targetMax; }
        public void setTargetMax(int targetMax) { this.targetMax = targetMax; }

        public int getCurrentMin() { return currentMin; }
        public void setCurrentMin(int currentMin) { this.currentMin = currentMin; }

        public int getCurrentMax() { return currentMax; }
        public void setCurrentMax(int currentMax) { this.currentMax = currentMax; }

        public double getScaleFactor() { return scaleFactor; }
        public void setScaleFactor(double scaleFactor) { this.scaleFactor = scaleFactor; }

        /**
         * Масштабирует сырое значение в целевой диапазон
         */
        public int scale(int rawScore) {
            if (currentMax == currentMin) {
                return (targetMin + targetMax) / 2; // если нет вариативности - среднее
            }
            double scaled = targetMin + (rawScore - currentMin) * scaleFactor;
            return (int) Math.round(Math.max(targetMin, Math.min(targetMax, scaled)));
        }

        @Override
        public String toString() {
            return String.format("%s: target=[%d,%d] current=[%d,%d] scale=%.2f",
                    paramName, targetMin, targetMax, currentMin, currentMax, scaleFactor);
        }
    }
}