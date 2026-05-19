package main.java.com.psychotest.controller;

import main.java.com.psychotest.dao.TestDAO;
import main.java.com.psychotest.dao.TestSessionDAO;
import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.model.*;
import main.java.com.psychotest.service.ResultCalculationService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TakerController {
    private int userId;
    private TestDAO testDAO;
    private TestSessionDAO sessionDAO;
    private UserDAO userDAO;
    private ResultCalculationService resultService;

    public TakerController(int userId) {
        this.userId = userId;
        this.testDAO = new TestDAO();
        this.sessionDAO = new TestSessionDAO();
        this.userDAO = new UserDAO();
        this.resultService = new ResultCalculationService();
    }

    // ========== Доступные тесты ==========

    /**
     * Получить список доступных тестов (личные + групповые назначения)
     */
    public List<Test> getAvailableTests() {
        try {
            List<Integer> testIds = testDAO.getAvailableTestsForUser(userId);
            List<Test> tests = new ArrayList<>();
            for (int testId : testIds) {
                Test test = testDAO.findById(testId);
                if (test != null) {
                    tests.add(test);
                }
            }
            return tests;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Получить полное состояние теста для прохождения
     */
    public Test getFullTest(int testId) {
        try {
            return testDAO.findById(testId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ========== Прохождение теста ==========

    /**
     * Начать новую сессию тестирования
     */
    public int startTest(int testId) {
        try {
            return sessionDAO.createSession(userId, testId);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Сохранить ответ
     */
    public boolean saveAnswer(int sessionId, int questionId, int answerOptionId) {
        try {
            sessionDAO.saveAnswer(sessionId, questionId, answerOptionId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Завершить тест и получить результат
     */
    public TestResult calculateAndCompleteTest(int sessionId) {
        try {
            // Получаем сессию и тест
            TestSession session = sessionDAO.getSession(sessionId);
            if (session == null) {
                return null;
            }

            // Получаем полный тест
            Test test = testDAO.findById(session.getTestId());
            if (test == null) {
                return null;
            }

            // Рассчитываем результаты
            TestResult result = resultService.calculateResults(sessionId, test);

            // Завершаем сессию
            sessionDAO.completeSession(sessionId);

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Прервать тест
     */
    public boolean abandonTest(int sessionId) {
        try {
            sessionDAO.abandonSession(sessionId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Получить текущую сессию
     */
    public TestSession getCurrentSession(int sessionId) {
        try {
            return sessionDAO.getSession(sessionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ========== Результаты ==========

    /**
     * Получить историю пройденных тестов
     */
    public List<TestSession> getTestHistory() {
        try {
            return sessionDAO.getUserSessions(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Получить детальные результаты сессии
     */
    public TestResult getTestResult(int sessionId) {
        try {
            TestSession session = sessionDAO.getSession(sessionId);
            if (session == null || !"COMPLETED".equals(session.getStatus())) {
                return null;
            }
            Test test = testDAO.findById(session.getTestId());
            return resultService.calculateResults(sessionId, test);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}