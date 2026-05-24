package main.java.com.psychotest.controller;

import main.java.com.psychotest.dao.GroupDAO;
import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.model.Group;
import main.java.com.psychotest.model.User;

import java.util.List;

public class AdminController {
    private UserDAO userDAO;
    private GroupDAO groupDAO;

    public AdminController() {
        this.userDAO = new UserDAO();
        this.groupDAO = new GroupDAO();
    }

    // ========== Управление пользователями ==========

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    public User getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public boolean addUser(String username, String passwordHash, String fullName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setFullName(fullName);
        user.setRole(role);
        return userDAO.save(user);
    }

    public boolean updateUser(int id, String fullName, String role) {
        User user = userDAO.findById(id);
        if (user != null) {
            user.setFullName(fullName);
            user.setRole(role);
            return userDAO.update(user);
        }
        return false;
    }

    public boolean updateUserPassword(int id, String newPasswordHash) {
        return userDAO.updatePassword(id, newPasswordHash);
    }

    public boolean deleteUser(int id) {
        return userDAO.delete(id);
    }

    public List<User> getUsersByRole(String role) {
        return userDAO.findByRole(role);
    }

    // ========== Управление группами ==========

    public List<Group> getAllGroups() {
        return groupDAO.findAll();
    }

    public Group getGroupById(int id) {
        return groupDAO.findById(id);
    }

    public Group getGroupByName(String name) {
        return groupDAO.findByName(name);
    }

    public boolean addGroup(String name, String description) {
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        return groupDAO.save(group);
    }

    public boolean updateGroup(int id, String name, String description) {
        Group group = groupDAO.findById(id);
        if (group != null) {
            group.setName(name);
            group.setDescription(description);
            return groupDAO.update(group);
        }
        return false;
    }

    public boolean deleteGroup(int id) {
        return groupDAO.delete(id);
    }

    // ========== Управление участниками групп ==========

    public boolean addUserToGroup(int userId, int groupId) {
        return groupDAO.addUserToGroup(userId, groupId);
    }

    public boolean removeUserFromGroup(int userId, int groupId) {
        return groupDAO.removeUserFromGroup(userId, groupId);
    }

    public List<User> getGroupMembers(int groupId) {
        Group group = groupDAO.findById(groupId);
        return group != null ? group.getMembers() : List.of();
    }

    public List<Group> getUserGroups(int userId) {
        return groupDAO.findGroupsByUser(userId);
    }

    // ========== Поиск и фильтрация ==========

    public List<User> searchUsers(String searchText) {
        List<User> allUsers = userDAO.findAll();
        if (searchText == null || searchText.trim().isEmpty()) {
            return allUsers;
        }
        String search = searchText.toLowerCase().trim();
        return allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(search) ||
                        u.getFullName().toLowerCase().contains(search))
                .toList();
    }

    public List<Group> searchGroups(String searchText) {
        List<Group> allGroups = groupDAO.findAll();
        if (searchText == null || searchText.trim().isEmpty()) {
            return allGroups;
        }
        String search = searchText.toLowerCase().trim();
        return allGroups.stream()
                .filter(g -> g.getName().toLowerCase().contains(search))
                .toList();
    }
}