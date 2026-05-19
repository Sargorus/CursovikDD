package main.java.com.psychotest.service;

import main.java.com.psychotest.dao.TestDAO;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.TestState;
import java.sql.SQLException;
import java.util.List;

public class TestPersistenceService {
    private TestDAO testDAO;

    public TestPersistenceService() {
        this.testDAO = new TestDAO();
    }

    /**
     * Сохраняет тест из черновика в базу данных
     * @param state состояние теста
     * @param teacherId ID преподавателя
     * @return ID сохранённого теста
     */
    public int saveTest(TestState state, int teacherId) throws SQLException {
        return testDAO.save(state, teacherId);
    }

    /**
     * Загружает полный тест для редактирования (как копию)
     */
    public TestState loadTestForEditing(int testId) throws SQLException {
        return testDAO.loadFullTestState(testId);
    }

    /**
     * Получает тест без деталей (только основная информация)
     */
    public Test getTestInfo(int testId) throws SQLException {
        return testDAO.findById(testId);
    }

    /**
     * Получает все тесты, созданные преподавателем
     */
    public List<Test> getTestsByTeacher(int teacherId) throws SQLException {
        return testDAO.findByTeacher(teacherId);
    }

    /**
     * Получает все тесты (для администратора)
     */
    public List<Test> getAllTests() throws SQLException {
        return testDAO.findAll();
    }

    /**
     * Обновляет информацию о тесте
     */
    public boolean updateTest(Test test) throws SQLException {
        return testDAO.update(test);
    }

    /**
     * Удаляет тест
     */
    public boolean deleteTest(int testId) throws SQLException {
        return testDAO.delete(testId);
    }

    /**
     * Назначает тест пользователю
     */
    public boolean assignTestToUser(int testId, int assignedBy, int userId, java.util.Date dueDate) throws SQLException {
        return testDAO.assignToUser(testId, assignedBy, userId, new java.sql.Date(dueDate.getTime()));
    }

    /**
     * Назначает тест группе
     */
    public boolean assignTestToGroup(int testId, int assignedBy, int groupId, java.util.Date dueDate) throws SQLException {
        return testDAO.assignToGroup(testId, assignedBy, groupId, new java.sql.Date(dueDate.getTime()));
    }

    /**
     * Получает список ID тестов, доступных пользователю
     */
    public List<Integer> getAvailableTestIdsForUser(int userId) throws SQLException {
        return testDAO.getAvailableTestsForUser(userId);
    }
}
