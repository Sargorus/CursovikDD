package main.java.com.psychotest.service;

import main.java.com.psychotest.model.*;
import main.java.com.psychotest.util.DatabaseConnection;

import java.sql.*;
import java.util.*;

public class ResultCalculationService {

    /**
     * Рассчитывает результаты теста на основе ответов пользователя
     */
    public TestResult calculateResults(int sessionId, Test test) throws SQLException {
        TestResult result = new TestResult();
        result.setSessionId(sessionId);
        result.setTestId(test.getId());
        result.setTestName(test.getName());

        // 1. Получаем все ответы пользователя
        Map<Integer, Integer> userAnswers = getUserAnswers(sessionId);
        if (userAnswers.isEmpty()) {
            result.setErrorMessage("Нет сохранённых ответов");
            return result;
        }

        // 2. Загружаем полную структуру теста (параметры, вопросы, ответы)
        List<Parameter> parameters = loadParameters(test.getId());
        List<Question> questions = loadQuestionsWithAnswers(test.getId());

        // 3. Создаём маппинг вопрос -> выбранный ответ
        Map<Integer, AnswerOption> selectedAnswers = new HashMap<>();
        for (Question q : questions) {
            Integer selectedOptionId = userAnswers.get(q.getId());
            if (selectedOptionId != null) {
                for (AnswerOption opt : q.getAnswerOptions()) {
                    if (opt.getId() == selectedOptionId) {
                        selectedAnswers.put(q.getId(), opt);
                        break;
                    }
                }
            }
        }

        // 4. Рассчитываем сырые баллы по каждому параметру
        Map<String, Integer> rawScores = new HashMap<>();
        for (Parameter param : parameters) {
            rawScores.put(param.getName(), 0);
        }

        for (Question q : questions) {
            AnswerOption selected = selectedAnswers.get(q.getId());
            if (selected != null) {
                Map<Integer, Integer> impacts = selected.getParameterImpacts();
                for (int i = 0; i < parameters.size(); i++) {
                    String paramName = parameters.get(i).getName();
                    Integer delta = impacts.get(i);
                    if (delta != null) {
                        rawScores.put(paramName, rawScores.get(paramName) + delta);
                    }
                }
            }
        }
        result.setRawScores(rawScores);

        // 5. Масштабируем результаты (если есть калибровка)
        Map<String, Integer> scaledScores = new HashMap<>();
        Map<String, String> interpretations = new HashMap<>();

        for (Parameter param : parameters) {
            int rawScore = rawScores.get(param.getName());
            int scaledScore = rawScore;
            String interpretation = "";

            if (param.getScaleType().equals("BINARY")) {
                // Бинарная шкала: определяем по знаку
                String code = rawScore > 0 ? "Плюс" : "Минус";
                interpretation = findBinaryInterpretation(param, code);
            } else {
                // Диапазонная шкала: ищем интерпретацию по диапазону
                interpretation = findRangeInterpretation(param, rawScore);
            }

            scaledScores.put(param.getName(), scaledScore);
            interpretations.put(param.getName(), interpretation);
        }

        result.setScaledScores(scaledScores);
        result.setInterpretations(interpretations);
        result.setCompleted(true);

        // 6. Сохраняем результаты в БД
        saveResults(sessionId, parameters, rawScores, scaledScores, interpretations);

        return result;
    }

    /**
     * Получить ответы пользователя из сессии
     */
    private Map<Integer, Integer> getUserAnswers(int sessionId) throws SQLException {
        Map<Integer, Integer> answers = new HashMap<>();
        String sql = "SELECT question_id, answer_option_id FROM user_answers WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                answers.put(rs.getInt("question_id"), rs.getInt("answer_option_id"));
            }
        }
        return answers;
    }

    /**
     * Загрузить параметры теста
     */
    private List<Parameter> loadParameters(int testId) throws SQLException {
        List<Parameter> parameters = new ArrayList<>();
        String sql = "SELECT * FROM parameters WHERE test_id = ? ORDER BY id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Parameter param = new Parameter();
                param.setId(rs.getInt("id"));
                param.setTestId(rs.getInt("test_id"));
                param.setName(rs.getString("name"));
                param.setScaleType(rs.getString("scale_type"));
                param.setMinValue(rs.getInt("min_value"));
                param.setMaxValue(rs.getInt("max_value"));

                // Загружаем интерпретации
                param.setInterpretations(loadInterpretations(param.getId(), param.getScaleType()));
                parameters.add(param);
            }
        }
        return parameters;
    }

    /**
     * Загрузить интерпретации параметра
     */
    private List<ParameterInterpretation> loadInterpretations(int paramId, String scaleType) throws SQLException {
        List<ParameterInterpretation> interpretations = new ArrayList<>();
        String sql = "SELECT * FROM parameter_interpretations WHERE parameter_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, paramId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ParameterInterpretation interp = new ParameterInterpretation();
                interp.setId(rs.getInt("id"));
                interp.setParameterId(rs.getInt("parameter_id"));
                if (scaleType.equals("RANGE")) {
                    interp.setRangeStart(rs.getInt("range_start"));
                    interp.setRangeEnd(rs.getInt("range_end"));
                } else {
                    interp.setBinaryValue(rs.getString("binary_value"));
                }
                interp.setInterpretationText(rs.getString("interpretation_text"));
                interpretations.add(interp);
            }
        }
        return interpretations;
    }

    /**
     * Загрузить вопросы с ответами
     */
    private List<Question> loadQuestionsWithAnswers(int testId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE test_id = ? ORDER BY order_num";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt("id"));
                q.setTestId(rs.getInt("test_id"));
                q.setText(rs.getString("text"));
                q.setOrderNum(rs.getInt("order_num"));
                q.setAnswerOptions(loadAnswerOptions(q.getId()));
                questions.add(q);
            }
        }
        return questions;
    }

    /**
     * Загрузить варианты ответов с влиянием на параметры
     */
    private List<AnswerOption> loadAnswerOptions(int questionId) throws SQLException {
        List<AnswerOption> options = new ArrayList<>();
        String sql = "SELECT * FROM answer_options WHERE question_id = ? ORDER BY order_num";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                AnswerOption opt = new AnswerOption();
                opt.setId(rs.getInt("id"));
                opt.setQuestionId(rs.getInt("question_id"));
                opt.setText(rs.getString("text"));
                opt.setOrderNum(rs.getInt("order_num"));
                opt.setParameterImpacts(loadImpacts(opt.getId()));
                options.add(opt);
            }
        }
        return options;
    }

    /**
     * Загрузить влияния ответа на параметры
     */
    private Map<Integer, Integer> loadImpacts(int answerOptionId) throws SQLException {
        Map<Integer, Integer> impacts = new HashMap<>();
        String sql = "SELECT parameter_id, delta FROM answer_parameter_impact WHERE answer_option_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, answerOptionId);
            ResultSet rs = pstmt.executeQuery();
            // Находим индекс параметра (нужно будет сопоставить позже)
            while (rs.next()) {
                impacts.put(rs.getInt("parameter_id"), rs.getInt("delta"));
            }
        }
        return impacts;
    }

    /**
     * Найти бинарную интерпретацию
     */
    private String findBinaryInterpretation(Parameter param, String code) {
        for (ParameterInterpretation interp : param.getInterpretations()) {
            if (code.equals(interp.getBinaryValue())) {
                return interp.getInterpretationText();
            }
        }
        return "Нет интерпретации";
    }

    /**
     * Найти интерпретацию по диапазону
     */
    private String findRangeInterpretation(Parameter param, int score) {
        for (ParameterInterpretation interp : param.getInterpretations()) {
            if (interp.getRangeStart() != null && interp.getRangeEnd() != null) {
                if (score >= interp.getRangeStart() && score <= interp.getRangeEnd()) {
                    return interp.getInterpretationText();
                }
            }
        }
        return "Нет интерпретации для значения " + score;
    }

    /**
     * Сохранить результаты в БД
     */
    private void saveResults(int sessionId, List<Parameter> parameters,
                             Map<String, Integer> rawScores,
                             Map<String, Integer> scaledScores,
                             Map<String, String> interpretations) throws SQLException {
        String sql = "INSERT INTO test_results (session_id, parameter_id, raw_score, scaled_score, " +
                "interpreted_code, interpretation_text) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Parameter param : parameters) {
                String paramName = param.getName();
                pstmt.setInt(1, sessionId);
                pstmt.setInt(2, param.getId());
                pstmt.setInt(3, rawScores.get(paramName));
                pstmt.setInt(4, scaledScores.get(paramName));
                pstmt.setString(5, null); // interpreted_code для диапазонных шкал
                pstmt.setString(6, interpretations.get(paramName));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}
