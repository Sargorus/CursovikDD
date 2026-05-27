package main.java.com.psychotest.service;

import main.java.com.psychotest.dao.TestDAO;
import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultService {
    private TestDAO testDAO;
    private UserDAO userDAO;

    public ResultService() {
        this.testDAO = new TestDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Получить все результаты для теста
     */
    public List<TestResult> getResultsForTest(int testId) throws SQLException {
        List<TestResult> results = new ArrayList<>();
        String sql = "SELECT ts.id, ts.user_id, ts.start_time, ts.end_time, ts.status, " +
                "u.full_name, u.username " +
                "FROM test_sessions ts " +
                "JOIN users u ON ts.user_id = u.id " +
                "WHERE ts.test_id = ? AND ts.status = 'COMPLETED' " +
                "ORDER BY ts.end_time DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TestResult result = new TestResult();
                result.setSessionId(rs.getInt("id"));
                result.setUserId(rs.getInt("user_id"));
                result.setUserFullName(rs.getString("full_name"));
                result.setUserLogin(rs.getString("username"));
                result.setStartTime(rs.getTimestamp("start_time"));
                result.setEndTime(rs.getTimestamp("end_time"));
                result.setStatus(rs.getString("status"));
                results.add(result);
            }
        }
        return results;
    }

    /**
     * Получить детальные результаты сессии (ответы на вопросы и интерпретации)
     */
    public SessionDetail getSessionDetail(int sessionId) throws SQLException {
        SessionDetail detail = new SessionDetail();
        detail.setSessionId(sessionId);

        // Получаем параметры и их интерпретации
        String paramSql = "SELECT p.name, p.scale_type, tr.raw_score, tr.scaled_score, " +
                "tr.interpreted_code, tr.interpretation_text " +
                "FROM test_results tr " +
                "JOIN parameters p ON tr.parameter_id = p.id " +
                "WHERE tr.session_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(paramSql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            Map<String, ParameterResult> paramResults = new HashMap<>();
            while (rs.next()) {
                ParameterResult pr = new ParameterResult();
                pr.setParamName(rs.getString("name"));
                pr.setScaleType(rs.getString("scale_type"));
                pr.setRawScore(rs.getInt("raw_score"));
                pr.setScaledScore(rs.getInt("scaled_score"));
                pr.setInterpretedCode(rs.getString("interpreted_code"));
                pr.setInterpretationText(rs.getString("interpretation_text"));
                paramResults.put(pr.getParamName(), pr);
            }
            detail.setParameterResults(paramResults);
        }

        // Получаем ответы на вопросы вместе с влиянием каждого ответа на параметры.
        // LEFT JOIN — если у ответа нет влияний, строка всё равно попадёт в результат
        // (param_name и delta будут NULL).
        String answerSql =
                "SELECT q.id AS question_id, q.order_num, q.text AS question_text, " +
                "       a_o.text AS answer_text, " +
                "       p.name  AS param_name, api.delta " +
                "FROM user_answers ua " +
                "JOIN  questions             q   ON ua.question_id     = q.id " +
                "JOIN  answer_options        a_o ON ua.answer_option_id = a_o.id " +
                "LEFT JOIN answer_parameter_impact api ON api.answer_option_id = a_o.id " +
                "LEFT JOIN parameters        p   ON api.parameter_id   = p.id " +
                "WHERE ua.session_id = ? " +
                "ORDER BY q.order_num, p.id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(answerSql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            // LinkedHashMap сохраняет порядок вопросов; ключ — question_id
            Map<Integer, AnswerDetail> answersMap = new java.util.LinkedHashMap<>();
            while (rs.next()) {
                int qId = rs.getInt("question_id");
                AnswerDetail ad = answersMap.computeIfAbsent(qId, k -> {
                    AnswerDetail a = new AnswerDetail();
                    try {
                        a.setQuestionText(rs.getString("question_text"));
                        a.setAnswerText(rs.getString("answer_text"));
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    return a;
                });
                String paramName = rs.getString("param_name");
                if (paramName != null) {
                    ad.getParameterImpacts().put(paramName, rs.getInt("delta"));
                }
            }
            detail.setAnswers(new ArrayList<>(answersMap.values()));
        }

        return detail;
    }

    // ========== Внутренние классы для данных ==========

    public static class TestResult {
        private int sessionId;
        private int userId;
        private String userFullName;
        private String userLogin;
        private Timestamp startTime;
        private Timestamp endTime;
        private String status;

        // Геттеры и сеттеры
        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getUserFullName() { return userFullName; }
        public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

        public String getUserLogin() { return userLogin; }
        public void setUserLogin(String userLogin) { this.userLogin = userLogin; }

        public Timestamp getStartTime() { return startTime; }
        public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

        public Timestamp getEndTime() { return endTime; }
        public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getFormattedDate() {
            if (endTime != null) {
                return endTime.toLocalDateTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                );
            }
            return "";
        }
    }

    public static class ParameterResult {
        private String paramName;
        private String scaleType;
        private int rawScore;
        private int scaledScore;
        private String interpretedCode;
        private String interpretationText;

        // Геттеры и сеттеры
        public String getParamName() { return paramName; }
        public void setParamName(String paramName) { this.paramName = paramName; }

        public String getScaleType() { return scaleType; }
        public void setScaleType(String scaleType) { this.scaleType = scaleType; }

        public int getRawScore() { return rawScore; }
        public void setRawScore(int rawScore) { this.rawScore = rawScore; }

        public int getScaledScore() { return scaledScore; }
        public void setScaledScore(int scaledScore) { this.scaledScore = scaledScore; }

        public String getInterpretedCode() { return interpretedCode; }
        public void setInterpretedCode(String interpretedCode) { this.interpretedCode = interpretedCode; }

        public String getInterpretationText() { return interpretationText; }
        public void setInterpretationText(String interpretationText) { this.interpretationText = interpretationText; }
    }

    public static class AnswerDetail {
        private String questionText;
        private String answerText;
        /** Название параметра → балл (delta), полученный за этот ответ */
        private Map<String, Integer> parameterImpacts = new java.util.LinkedHashMap<>();

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getAnswerText() { return answerText; }
        public void setAnswerText(String answerText) { this.answerText = answerText; }

        public Map<String, Integer> getParameterImpacts() { return parameterImpacts; }
        public void setParameterImpacts(Map<String, Integer> parameterImpacts) {
            this.parameterImpacts = parameterImpacts;
        }
    }

    public static class SessionDetail {
        private int sessionId;
        private Map<String, ParameterResult> parameterResults;
        private List<AnswerDetail> answers;

        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }

        public Map<String, ParameterResult> getParameterResults() { return parameterResults; }
        public void setParameterResults(Map<String, ParameterResult> parameterResults) {
            this.parameterResults = parameterResults;
        }

        public List<AnswerDetail> getAnswers() { return answers; }
        public void setAnswers(List<AnswerDetail> answers) { this.answers = answers; }
    }

    /**
     * Статистика интерпретаций по параметрам для всего теста.
     * @return paramName → (метка интерпретации → количество участников)
     */
    public Map<String, Map<String, Integer>> getTestStatistics(int testId) throws SQLException {
        Map<String, Map<String, Integer>> stats = new java.util.LinkedHashMap<>();

        String sql =
                "SELECT p.name AS param_name, " +
                "       CASE " +
                "           WHEN tr.interpreted_code IS NOT NULL AND tr.interpreted_code != '' " +
                "               THEN tr.interpreted_code " +
                "           WHEN tr.interpretation_text IS NOT NULL AND tr.interpretation_text != '' " +
                "               THEN tr.interpretation_text " +
                "           ELSE 'Нет данных' " +
                "       END AS label, " +
                "       COUNT(*) AS cnt " +
                "FROM test_results tr " +
                "JOIN parameters p   ON tr.parameter_id  = p.id " +
                "JOIN test_sessions ts ON tr.session_id  = ts.id " +
                "WHERE ts.test_id = ? AND ts.status = 'COMPLETED' " +
                "GROUP BY p.name, " +
                "         CASE " +
                "             WHEN tr.interpreted_code IS NOT NULL AND tr.interpreted_code != '' " +
                "                 THEN tr.interpreted_code " +
                "             WHEN tr.interpretation_text IS NOT NULL AND tr.interpretation_text != '' " +
                "                 THEN tr.interpretation_text " +
                "             ELSE 'Нет данных' " +
                "         END " +
                "ORDER BY p.name, cnt DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String paramName = rs.getString("param_name");
                String label     = rs.getString("label");
                int    cnt       = rs.getInt("cnt");
                stats.computeIfAbsent(paramName, k -> new java.util.LinkedHashMap<>())
                     .put(label, cnt);
            }
        }
        return stats;
    }

    /**
     * Для каждого параметра возвращает: метка интерпретации → список ФИО участников.
     * Порядок меток совпадает с порядком из getTestStatistics (по параметру, затем по метке).
     *
     * @return paramName → (label → [fullName, ...])
     */
    public Map<String, Map<String, List<String>>> getParticipantsByLabel(int testId) throws SQLException {
        Map<String, Map<String, List<String>>> result = new java.util.LinkedHashMap<>();

        String sql =
                "SELECT p.name AS param_name, " +
                "       CASE " +
                "           WHEN tr.interpreted_code IS NOT NULL AND tr.interpreted_code != '' " +
                "               THEN tr.interpreted_code " +
                "           WHEN tr.interpretation_text IS NOT NULL AND tr.interpretation_text != '' " +
                "               THEN tr.interpretation_text " +
                "           ELSE 'Нет данных' " +
                "       END AS label, " +
                "       u.full_name " +
                "FROM test_results tr " +
                "JOIN parameters    p  ON tr.parameter_id = p.id " +
                "JOIN test_sessions ts ON tr.session_id   = ts.id " +
                "JOIN users         u  ON ts.user_id      = u.id " +
                "WHERE ts.test_id = ? AND ts.status = 'COMPLETED' " +
                "ORDER BY p.name, label, u.full_name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String paramName = rs.getString("param_name");
                String label     = rs.getString("label");
                String fullName  = rs.getString("full_name");
                result
                    .computeIfAbsent(paramName, k -> new java.util.LinkedHashMap<>())
                    .computeIfAbsent(label,     k -> new java.util.ArrayList<>())
                    .add(fullName);
            }
        }
        return result;
    }

    /**
     * Для каждого параметра возвращает список (ФИО, масштабированный балл) по всем
     * завершённым сессиям теста. Используется для расчёта групповой статистики.
     *
     * @return paramName → [UserScore, ...]  (отсортировано по имени параметра, затем по баллу)
     */
    public Map<String, List<GroupReportAnalytics.UserScore>> getParameterScoresByUser(int testId)
            throws SQLException {
        Map<String, List<GroupReportAnalytics.UserScore>> result = new java.util.LinkedHashMap<>();

        String sql =
                "SELECT p.name AS param_name, u.full_name, tr.scaled_score " +
                "FROM test_results tr " +
                "JOIN test_sessions ts ON tr.session_id  = ts.id " +
                "JOIN users          u  ON ts.user_id    = u.id " +
                "JOIN parameters     p  ON tr.parameter_id = p.id " +
                "WHERE ts.test_id = ? AND ts.status = 'COMPLETED' " +
                "ORDER BY p.name, tr.scaled_score";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, testId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String paramName = rs.getString("param_name");
                String fullName  = rs.getString("full_name");
                int    score     = rs.getInt("scaled_score");
                result.computeIfAbsent(paramName, k -> new java.util.ArrayList<>())
                      .add(new GroupReportAnalytics.UserScore(fullName, score));
            }
        }
        return result;
    }

    /**
     * Получить название теста по ID сессии
     */
    public String getTestNameBySessionId(int sessionId) throws SQLException {
        String sql = "SELECT t.name FROM tests t " +
                "JOIN test_sessions ts ON t.id = ts.test_id " +
                "WHERE ts.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        }
        return "Неизвестный тест";
    }
}
