package main.java.com.psychotest.dao;

import main.java.com.psychotest.model.AnswerOption;
import main.java.com.psychotest.model.Question;
import main.java.com.psychotest.model.TestSession;
import main.java.com.psychotest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

public class TestSessionDAO {

    /**
     * Создать новую сессию тестирования
     */
    public int createSession(int userId, int testId) throws SQLException {
        String sql = "INSERT INTO test_sessions (user_id, test_id, start_time, status) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP, 'IN_PROGRESS') RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, testId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Сохранить (или обновить) ответ пользователя.
     * Использует DELETE + INSERT вместо ON CONFLICT, чтобы не зависеть
     * от наличия UNIQUE-ограничения на (session_id, question_id).
     * DELETE ничего не делает, если ответ ещё не был дан — всё безопасно.
     */
    public void saveAnswer(int sessionId, int questionId, int answerOptionId) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Удаляем старый ответ на этот вопрос (если есть)
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM user_answers WHERE session_id = ? AND question_id = ?")) {
                    del.setInt(1, sessionId);
                    del.setInt(2, questionId);
                    del.executeUpdate();
                }
                // Вставляем новый ответ
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO user_answers (session_id, question_id, answer_option_id, answered_at) " +
                        "VALUES (?, ?, ?, CURRENT_TIMESTAMP)")) {
                    ins.setInt(1, sessionId);
                    ins.setInt(2, questionId);
                    ins.setInt(3, answerOptionId);
                    ins.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Завершить сессию
     */
    public void completeSession(int sessionId) throws SQLException {
        String sql = "UPDATE test_sessions SET end_time = CURRENT_TIMESTAMP, status = 'COMPLETED' " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Полностью удаляет сессию и все связанные данные:
     * результаты по параметрам, ответы пользователя, саму запись сессии.
     */
    public void deleteSession(int sessionId) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM test_results WHERE session_id = ?")) {
                    pstmt.setInt(1, sessionId);
                    pstmt.executeUpdate();
                }
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM user_answers WHERE session_id = ?")) {
                    pstmt.setInt(1, sessionId);
                    pstmt.executeUpdate();
                }
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM test_sessions WHERE id = ?")) {
                    pstmt.setInt(1, sessionId);
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Сохраняет список вопросов, выданных в данной сессии.
     * Вызывается один раз при старте сессии после отбора вопросов.
     */
    public void saveSessionQuestions(int sessionId, List<Question> questions) throws SQLException {
        String sql = "INSERT INTO session_questions (session_id, question_id, question_order) " +
                     "VALUES (?, ?, ?) ON CONFLICT (session_id, question_id) DO NOTHING";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int order = 0;
            for (Question q : questions) {
                pstmt.setInt(1, sessionId);
                pstmt.setInt(2, q.getId());
                pstmt.setInt(3, order++);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Загружает ID вопросов, выданных в данной сессии, в порядке их показа.
     * Возвращает пустой список, если записей нет (старые сессии до добавления функции).
     */
    public List<Integer> loadSessionQuestionIds(int sessionId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT question_id FROM session_questions WHERE session_id = ? " +
                     "ORDER BY question_order";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("question_id"));
            }
        }
        return ids;
    }

    /**
     * Возвращает все ответы пользователя для данной сессии.
     * Ключ — questionId, значение — answerOptionId.
     */
    public Map<Integer, Integer> getUserAnswers(int sessionId) throws SQLException {
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
     * Возвращает test_id для указанной сессии, или -1 если сессия не найдена.
     */
    public int getTestIdBySessionId(int sessionId) throws SQLException {
        String sql = "SELECT test_id FROM test_sessions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("test_id");
            }
        }
        return -1;
    }

    /**
     * Прервать сессию
     */
    public void abandonSession(int sessionId) throws SQLException {
        String sql = "UPDATE test_sessions SET end_time = CURRENT_TIMESTAMP, status = 'ABANDONED' " +
                "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Получить сессию по ID
     */
    public TestSession getSession(int sessionId) throws SQLException {
        String sql = "SELECT * FROM test_sessions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                TestSession session = new TestSession();
                session.setId(rs.getInt("id"));
                session.setUserId(rs.getInt("user_id"));
                session.setTestId(rs.getInt("test_id"));
                session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                if (rs.getTimestamp("end_time") != null) {
                    session.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                }
                session.setStatus(rs.getString("status"));
                return session;
            }
        }
        return null;
    }

    /**
     * Получить все сессии пользователя
     */
    public List<TestSession> getUserSessions(int userId) throws SQLException {
        List<TestSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM test_sessions WHERE user_id = ? ORDER BY start_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TestSession session = new TestSession();
                session.setId(rs.getInt("id"));
                session.setUserId(rs.getInt("user_id"));
                session.setTestId(rs.getInt("test_id"));
                session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                if (rs.getTimestamp("end_time") != null) {
                    session.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                }
                session.setStatus(rs.getString("status"));
                sessions.add(session);
            }
        }
        return sessions;
    }

    /**
     * Получить завершённые сессии пользователя
     */
    public List<TestSession> getCompletedSessionsForUser(int userId) throws SQLException {
        List<TestSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM test_sessions WHERE user_id = ? AND status = 'COMPLETED' ORDER BY end_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TestSession session = new TestSession();
                session.setId(rs.getInt("id"));
                session.setUserId(rs.getInt("user_id"));
                session.setTestId(rs.getInt("test_id"));
                session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                if (rs.getTimestamp("end_time") != null) {
                    session.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                }
                session.setStatus(rs.getString("status"));
                sessions.add(session);
            }
        }
        return sessions;
    }
}
