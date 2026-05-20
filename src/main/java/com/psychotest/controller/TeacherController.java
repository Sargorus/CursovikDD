package main.java.com.psychotest.controller;

import main.java.com.psychotest.dao.TestDAO;
import main.java.com.psychotest.dao.TestSessionDAO;
import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.dao.GroupDAO;
import main.java.com.psychotest.model.*;
import main.java.com.psychotest.service.TestPersistenceService;
import main.java.com.psychotest.service.ResultService;
import main.java.com.psychotest.service.ExcelReportService;
import main.java.com.psychotest.util.DatabaseConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TeacherController {
    private TestDAO testDAO;
    private UserDAO userDAO;
    private GroupDAO groupDAO;
    private TestPersistenceService persistenceService;
    private int teacherId;
    private ResultService resultService;
    private ExcelReportService excelService;
    private TestSessionDAO sessionDAO;

    public TeacherController(int teacherId) {
        this.teacherId = teacherId;
        this.testDAO = new TestDAO();
        this.userDAO = new UserDAO();
        this.groupDAO = new GroupDAO();
        this.persistenceService = new TestPersistenceService();
        this.resultService = new ResultService();
        this.excelService = new ExcelReportService();
        this.sessionDAO = new TestSessionDAO();
    }

    // ========== Управление тестами ==========

    /**
     * Получить все тесты, созданные преподавателем
     */
    public List<Test> getMyTests() {
        try {
            return testDAO.findByTeacher(teacherId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Получить тест по ID (основная информация)
     */
    public Test getTestById(int testId) {
        try {
            return testDAO.findById(testId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получить полное состояние теста для редактирования (копия)
     */
    public TestState getTestForEditing(int testId) {
        try {
            return persistenceService.loadTestForEditing(testId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Удалить тест
     */
    public boolean deleteTest(int testId) {
        try {
            return testDAO.delete(testId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== Назначение тестов ==========

    /**
     * Получить всех пользователей (для выбора при назначении)
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Получить всех тестируемых (TAKER)
     */
    public List<User> getAllTakers() {
        return userDAO.findByRole("TAKER");
    }

    /**
     * Получить все группы
     */
    public List<Group> getAllGroups() {
        return groupDAO.findAll();
    }

    /**
     * Назначить тест группе
     */
    public boolean assignTestToGroup(int testId, int groupId, Date dueDate) {
        try {
            return testDAO.assignToGroup(testId, teacherId, groupId, new java.sql.Date(dueDate.getTime()));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Получить список ID тестов, доступных пользователю
     */
    public List<Integer> getAvailableTestIdsForUser(int userId) {
        try {
            return testDAO.getAvailableTestsForUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ========== Поиск и фильтрация ==========

    /**
     * Поиск тестов по названию
     */
    public List<Test> searchMyTests(String searchText) {
        List<Test> myTests = getMyTests();
        if (searchText == null || searchText.trim().isEmpty()) {
            return myTests;
        }
        String search = searchText.toLowerCase().trim();
        return myTests.stream()
                .filter(t -> t.getName().toLowerCase().contains(search))
                .toList();
    }

    /**
     * Поиск пользователей по ФИО или логину
     */
    public List<User> searchUsers(String searchText) {
        List<User> users = getAllUsers();
        if (searchText == null || searchText.trim().isEmpty()) {
            return users;
        }
        String search = searchText.toLowerCase().trim();
        return users.stream()
                .filter(u -> u.getFullName().toLowerCase().contains(search) ||
                        u.getUsername().toLowerCase().contains(search))
                .toList();
    }

    /**
     * Поиск групп по названию
     */
    public List<Group> searchGroups(String searchText) {
        List<Group> groups = getAllGroups();
        if (searchText == null || searchText.trim().isEmpty()) {
            return groups;
        }
        String search = searchText.toLowerCase().trim();
        return groups.stream()
                .filter(g -> g.getName().toLowerCase().contains(search))
                .toList();
    }

    // ========== Работа с результатами ==========

    /**
     * Получить все результаты для теста
     */
    public List<ResultService.TestResult> getResultsForTest(int testId) {
        try {
            return resultService.getResultsForTest(testId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Получить детальные результаты сессии
     */
    public ResultService.SessionDetail getSessionDetail(int sessionId) {
        try {
            return resultService.getSessionDetail(sessionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получить название теста по ID сессии
     */
    public String getTestNameBySessionId(int sessionId) {
        try {
            return resultService.getTestNameBySessionId(sessionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return "Неизвестный тест";
        }
    }

    public int getTeacherId() {
        return teacherId;
    }

    // ========== Экспорт в Excel ==========

    /**
     * Экспортирует результаты теста в Excel
     */
    public boolean exportTestResultsToExcel(int testId, String filePath) {
        Test test = getTestById(testId);
        if (test == null) {
            return false;
        }

        List<ResultService.TestResult> results = getResultsForTest(testId);
        return excelService.exportResultsToExcel(results, test, filePath);
    }

    /**
     * Экспортирует детальные результаты сессии в Excel
     */
    public boolean exportSessionDetailsToExcel(int sessionId, String filePath) {
        ResultService.SessionDetail detail = getSessionDetail(sessionId);
        if (detail == null) {
            return false;
        }

        String userName = "";
        String testName = getTestNameBySessionId(sessionId);

        // Получаем имя пользователя
        for (ResultService.TestResult result : getResultsForTest(getTestIdBySessionId(sessionId))) {
            if (result.getSessionId() == sessionId) {
                userName = result.getUserFullName();
                break;
            }
        }

        return excelService.exportSessionDetailsToExcel(detail, userName, testName, filePath);
    }

    // Вспомогательный метод
    private int getTestIdBySessionId(int sessionId) {
        try {
            String sql = "SELECT test_id FROM test_sessions WHERE id = ?";
            try (java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionId);
                java.sql.ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Получить группы пользователя
     */
    public List<Group> getUserGroups(int userId) {
        return groupDAO.findGroupsByUser(userId);
    }

    /**
     * Получить количество назначенных тестов для пользователя
     */
    public int getUserTestCount(int userId) {
        try {
            return testDAO.getAssignedTestCountForUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Получить количество пройденных тестов для пользователя
     */
    public int getUserCompletedTestCount(int userId) {
        try {
            return testDAO.getCompletedTestCountForUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Получить ID назначенных тестов для пользователя
     */
    public List<Integer> getAssignedTestIdsForUser(int userId) {
        try {
            return testDAO.getAssignedTestIdsForUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Получить завершённые сессии пользователя
     */
    public List<TestSession> getCompletedSessionsForUser(int userId) {
        try {
            return sessionDAO.getCompletedSessionsForUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Назначить тест пользователю
     */
    public boolean assignTestToUser(int testId, int userId, Date dueDate) {
        try {
            return testDAO.assignToUser(testId, teacherId, userId,
                    dueDate != null ? new java.sql.Date(dueDate.getTime()) : null);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Отвязать тест от пользователя (удалить назначение)
     */
    public boolean unassignTestFromUser(int testId, int userId) {
        try {
            return testDAO.unassignTestFromUser(testId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
