package main.java.com.psychotest.service;

import main.java.com.psychotest.dao.UserDAO;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.util.PasswordUtil;

public class AuthService {
    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User authenticate(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }
}