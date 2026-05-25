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

    /**
     * Меняет пароль пользователя.
     * @param userId       ID пользователя
     * @param currentPassword текущий пароль (в открытом виде)
     * @param newPassword  новый пароль (в открытом виде)
     * @return true — успешно; false — текущий пароль неверен или пользователь не найден
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        User user = userDAO.findById(userId);
        if (user == null) return false;
        if (!PasswordUtil.checkPassword(currentPassword, user.getPasswordHash())) return false;
        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }
}