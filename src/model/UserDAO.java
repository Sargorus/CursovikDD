package model;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static List<User> users = new ArrayList<>();

    static {
        users.add(new User("a", "a"));
        users.add(new User("user", "user123"));
    }

    // Проверка существования пользователя
    public static boolean checkUser(String login, String password){
        for(User user : users) {
            if(user.getLogin().equals(login) && user.getPassword().equals(password)){
                return true;
            }
        }
        return false;
    }

    // Добавление нового пользователя
    public static boolean addUser(String login, String password) {
        // Проверка, не существует ли уже такой логин
        for (User user : users) {
            if (user.getLogin().equals(login)) {
                return false;
            }
        }
        users.add(new User(login, password));
        return true;
    }

    // Получить всех пользователей (для отладки)
    public static List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    // Удалить пользователя
    public static boolean removeUser(String login) {
        return users.removeIf(user -> user.getLogin().equals(login));
    }
}
