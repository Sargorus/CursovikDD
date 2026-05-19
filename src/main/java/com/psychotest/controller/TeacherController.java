package main.java.com.psychotest.controller;

import main.java.com.psychotest.dao.TestDAO;
import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.dao.GroupDAO;
import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.model.Group;
import main.java.com.psychotest.model.TestState;
import main.java.com.psychotest.service.TestPersistenceService;

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

    public TeacherController(int teacherId) {
        this.teacherId = teacherId;
        this.testDAO = new TestDAO();
        this.userDAO = new UserDAO();
        this.groupDAO = new GroupDAO();
        this.persistenceService = new TestPersistenceService();
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
     * Назначить тест пользователю
     */
    public boolean assignTestToUser(int testId, int userId, Date dueDate) {
        try {
            return testDAO.assignToUser(testId, teacherId, userId, new java.sql.Date(dueDate.getTime()));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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

    public int getTeacherId() {
        return teacherId;
    }
}
