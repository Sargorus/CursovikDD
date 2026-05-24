package main.java.com.psychotest.dao;

import main.java.com.psychotest.model.AnswerOption;
import main.java.com.psychotest.model.Question;
import main.java.com.psychotest.model.TestSession;
import main.java.com.psychotest.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
     * Использует UPSERT: если ответ на этот вопрос уже есть — обновляет его.
     * Это позволяет корректно сохранять изменённый ответ при возврате назад.
     */
    public void saveAnswer(int sessionId, int questionId, int answerOptionId) throws SQLException {
        String sql = "INSERT INTO user_answers (session_id, question_id, answer_option_id, answered_at) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (session_id, question_id) " +
                "DO UPDATE SET answer_option_id = EXCLUDED.answer_option_id, " +
                "answered_at = CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            pstmt.setInt(2, questionId);
            pstmt.setInt(3, answerOptionId);
            pstmt.executeUpdate();
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
