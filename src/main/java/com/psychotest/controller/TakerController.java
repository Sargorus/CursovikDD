package main.java.com.psychotest.controller;

import main.java.com.psychotest.dao.TestDAO;
import main.java.com.psychotest.dao.TestSessionDAO;
import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.exception.DatabaseException;
import main.java.com.psychotest.exception.EntityNotFoundException;
import main.java.com.psychotest.exception.SessionException;
import main.java.com.psychotest.model.*;
import main.java.com.psychotest.service.QuestionSelectionService;
import main.java.com.psychotest.service.ResultCalculationService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TakerController {
    private int userId;
    private TestDAO testDAO;
    private TestSessionDAO sessionDAO;
    private UserDAO userDAO;
    private ResultCalculationService resultService;
    private QuestionSelectionService selectionService;

    public TakerController(int userId) {
        this.userId = userId;
        this.testDAO = new TestDAO();
        this.sessionDAO = new TestSessionDAO();
        this.userDAO = new UserDAO();
        this.resultService = new ResultCalculationService();
        this.selectionService = new QuestionSelectionService();
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
     * Загружает полный тест без отбора вопросов.
     * Используется при расчёте результатов (сессия уже завершена).
     *
     * @throws EntityNotFoundException если тест с указанным ID не найден
     * @throws DatabaseException       при ошибке обращения к БД
     */
    public Test getFullTest(int testId) {
        try {
            Test test = testDAO.findById(testId);
            if (test == null) {
                throw new EntityNotFoundException("Тест", testId);
            }
            test.setQuestionBank(testDAO.loadQuestionsForTest(testId));
            return test;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка загрузки теста #" + testId, e);
        }
    }

    /**
     * Отбирает вопросы из банка для новой сессии и сохраняет выборку в БД.
     *
     * <p>Алгоритм:
     * <ol>
     *   <li>Если у теста задан {@code questionsPerSession} меньше размера банка,
     *       используется {@link QuestionSelectionService} — жадный отбор,
     *       гарантирующий достижимость целевых значений по каждому параметру.</li>
     *   <li>Иначе выдаются все вопросы банка.</li>
     * </ol>
     *
     * @param testId    ID теста
     * @param sessionId ID только что созданной сессии (для сохранения выборки)
     * @return Test с заполненным questionBank
     * @throws EntityNotFoundException если тест не найден
     * @throws DatabaseException       при ошибке обращения к БД
     */
    public Test getFullTest(int testId, int sessionId) {
        try {
            Test test = testDAO.findById(testId);
            if (test == null) {
                throw new EntityNotFoundException("Тест", testId);
            }

            List<Question> allQuestions = testDAO.loadQuestionsForTest(testId);
            List<Question> selected;

            int limit = test.getQuestionsPerSession();
            if (limit > 0 && allQuestions.size() > limit) {
                // Умный отбор с учётом целевых диапазонов параметров
                List<Parameter> parameters = testDAO.loadParameters(testId);
                selected = selectionService.select(allQuestions, parameters, limit);
            } else {
                selected = new ArrayList<>(allQuestions);
            }

            // Сохраняем выданные вопросы для этой сессии
            if (sessionId > 0 && !selected.isEmpty()) {
                sessionDAO.saveSessionQuestions(sessionId, selected);
            }

            test.setQuestionBank(selected);
            return test;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка загрузки теста #" + testId, e);
        }
    }

    // ========== Прохождение теста ==========

    /**
     * Начать новую сессию тестирования.
     *
     * @return ID созданной сессии
     * @throws SessionException  если сессию не удалось создать по неизвестной причине
     * @throws DatabaseException при ошибке обращения к БД
     */
    public int startTest(int testId) {
        try {
            int sessionId = sessionDAO.createSession(userId, testId);
            if (sessionId < 0) {
                throw new SessionException("Не удалось создать сессию для теста #" + testId
                        + ". Возможно, тест недоступен.");
            }
            return sessionId;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при создании сессии тестирования", e);
        }
    }

    /**
     * Сохранить ответ на вопрос.
     *
     * @throws DatabaseException при ошибке обращения к БД
     */
    public void saveAnswer(int sessionId, int questionId, int answerOptionId) {
        try {
            sessionDAO.saveAnswer(sessionId, questionId, answerOptionId);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка сохранения ответа (сессия=" + sessionId
                    + ", вопрос=" + questionId + ")", e);
        }
    }

    /**
     * Завершить тест и получить результат.
     *
     * @return рассчитанный результат
     * @throws EntityNotFoundException если сессия или тест не найдены
     * @throws DatabaseException       при ошибке обращения к БД
     */
    public TestResult calculateAndCompleteTest(int sessionId) {
        try {
            // Получаем сессию
            TestSession session = sessionDAO.getSession(sessionId);
            if (session == null) {
                throw new EntityNotFoundException("Сессия тестирования", sessionId);
            }

            // Получаем полный тест (getFullTest бросает EntityNotFoundException/DatabaseException)
            Test test = getFullTest(session.getTestId());

            // Рассчитываем результаты
            TestResult result = resultService.calculateResults(sessionId, test);

            // Завершаем сессию
            sessionDAO.completeSession(sessionId);

            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при расчёте результатов сессии #" + sessionId, e);
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
            if (test == null) {
                return null; // тест был удалён — результат показать нельзя
            }
            return resultService.calculateResults(sessionId, test);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получить название теста по ID
     */
    public String getTestName(int testId) {
        try {
            Test test = testDAO.findById(testId);
            return test != null ? test.getName() : "Тест #" + testId;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Тест #" + testId;
        }
    }

    /**
     * Получить сохранённые ответы для сессии.
     * Возвращает пустую карту при ошибке (не критично — пользователь просто
     * не увидит ранее выбранные ответы при возобновлении).
     */
    public Map<Integer, Integer> getSavedAnswers(int sessionId) {
        try {
            return sessionDAO.getUserAnswers(sessionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }


}