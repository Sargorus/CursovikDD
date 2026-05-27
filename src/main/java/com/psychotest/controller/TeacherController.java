package main.java.com.psychotest.controller;

import main.java.com.psychotest.dao.TestDAO;
import main.java.com.psychotest.dao.TestSessionDAO;
import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.dao.GroupDAO;
import main.java.com.psychotest.model.*;
import main.java.com.psychotest.service.TestPersistenceService;
import main.java.com.psychotest.service.ResultService;
import main.java.com.psychotest.service.ExcelReportService;
import main.java.com.psychotest.service.GroupReportAnalytics;
import main.java.com.psychotest.service.ResultCalculationService;
import main.java.com.psychotest.service.QuestionSelectionService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * Получить тесты, созданные этим преподавателем
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
            // dueDate может быть null (без срока) — передаём как есть в DAO
            return testDAO.assignToGroup(testId, teacherId, groupId, dueDate);
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
     * Получить все тесты из БД (для фильтра "Все тесты")
     */
    public List<Test> getAllTests() {
        try {
            return testDAO.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Получить имя автора по ID пользователя
     */
    public String getAuthorName(int userId) {
        User user = userDAO.findById(userId);
        return user != null ? user.getFullName() : "Неизвестный";
    }

    /**
     * Поиск тестов по названию
     */
    public List<Test> searchMyTests(String searchText) {
        List<Test> myTests = getMyTests();  // теперь это все тесты
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
     * Удалить результат сессии (ответы, расчёты и саму сессию)
     */
    public boolean deleteResult(int sessionId) {
        try {
            sessionDAO.deleteSession(sessionId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

    /**
     * Загружает полное состояние теста из БД для редактирования
     */
    public TestState loadTestState(int testId) {
        try {
            return testDAO.loadFullTestState(testId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Заменяет содержимое существующего теста новым состоянием
     */
    public void updateTestContent(int testId, TestState state) throws SQLException {
        testDAO.replaceTestContent(testId, state);
    }

    // ========== Экспорт в Excel ==========

    /**
     * Получить статистику интерпретаций по параметрам для всего теста.
     * @return paramName → (метка → количество)
     */
    public Map<String, Map<String, Integer>> getTestStatistics(int testId) {
        try {
            return resultService.getTestStatistics(testId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new java.util.LinkedHashMap<>();
        }
    }

    /**
     * Для каждого параметра возвращает список участников, сгруппированных по
     * метке интерпретации (области диаграммы).
     *
     * @return paramName → (label → [ФИО участников])
     */
    public Map<String, Map<String, List<String>>> getParticipantsByLabel(int testId) {
        try {
            return resultService.getParticipantsByLabel(testId);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return new java.util.LinkedHashMap<>();
        }
    }

    /**
     * Для каждого параметра возвращает список (ФИО, балл) по всем завершённым сессиям.
     * Используется для расчёта групповой статистики в Excel-отчёте.
     *
     * @return paramName → [UserScore, ...]
     */
    public Map<String, List<GroupReportAnalytics.UserScore>> getParameterScoresByUser(int testId) {
        try {
            return resultService.getParameterScoresByUser(testId);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return new java.util.LinkedHashMap<>();
        }
    }

    /**
     * Экспортирует результаты теста в Excel (включая диаграммы, список участников
     * и лист групповой аналитики по параметрам)
     */
    public boolean exportTestResultsToExcel(int testId, String filePath) {
        Test test = getTestById(testId);
        if (test == null) {
            return false;
        }

        List<ResultService.TestResult> results           = getResultsForTest(testId);
        Map<String, Map<String, Integer>> statistics     = getTestStatistics(testId);
        Map<String, Map<String, List<String>>> participants = getParticipantsByLabel(testId);
        Map<String, List<GroupReportAnalytics.UserScore>> paramScores = getParameterScoresByUser(testId);
        return excelService.exportResultsToExcel(results, test, statistics, participants, paramScores, filePath);
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
            return sessionDAO.getTestIdBySessionId(sessionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
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

    // ========== Пробный запуск теста (без сохранения в БД) ==========

    /**
     * Загружает тест для пробного запуска преподавателем.
     * Применяет умный отбор вопросов (если задан questionsPerSession),
     * чтобы превью отражало реальную сессию.
     */
    public Test getFullTestForPreview(int testId) {
        try {
            Test test = testDAO.findById(testId);
            if (test != null) {
                List<Question> allQuestions = testDAO.loadQuestionsForTest(testId);
                int limit = test.getQuestionsPerSession();
                List<Question> selected;
                if (limit > 0 && allQuestions.size() > limit) {
                    List<Parameter> parameters = testDAO.loadParameters(testId);
                    selected = new QuestionSelectionService().select(allQuestions, parameters, limit);
                } else {
                    selected = allQuestions;
                }
                test.setQuestionBank(selected);
            }
            return test;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Рассчитывает результаты пробного прохождения теста — без сохранения в БД.
     *
     * @param testId      ID теста
     * @param testName    название теста
     * @param userAnswers questionId → answerOptionId (собранные в UI)
     * @return рассчитанный результат или null при ошибке
     */
    public TestResult calculatePreviewResult(int testId, String testName,
                                             Map<Integer, Integer> userAnswers) {
        try {
            return new ResultCalculationService().calculateResultsLocally(testId, testName, userAnswers);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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
