package main.java.com.psychotest.dao;

import main.java.com.psychotest.model.Group;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    // Найти группу по ID
    public Group findById(int id) {
        String sql = "SELECT * FROM groups WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Group group = mapResultSetToGroup(rs);
                group.setMembers(getGroupMembers(id));
                return group;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Найти группу по имени
    public Group findByName(String name) {
        String sql = "SELECT * FROM groups WHERE name = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Group group = mapResultSetToGroup(rs);
                group.setMembers(getGroupMembers(group.getId()));
                return group;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получить все группы
    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT * FROM groups ORDER BY name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Group group = mapResultSetToGroup(rs);
                group.setMembers(getGroupMembers(group.getId()));
                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    // Получить группы пользователя
    public List<Group> findGroupsByUser(int userId) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.* FROM groups g " +
                "JOIN user_groups ug ON g.id = ug.group_id " +
                "WHERE ug.user_id = ? ORDER BY g.name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Group group = mapResultSetToGroup(rs);
                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    // Сохранить группу
    public boolean save(Group group) {
        String sql = "INSERT INTO groups (name, description) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, group.getName());
            pstmt.setString(2, group.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    group.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Обновить группу
    public boolean update(Group group) {
        String sql = "UPDATE groups SET name = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, group.getName());
            pstmt.setString(2, group.getDescription());
            pstmt.setInt(3, group.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Удалить группу
    public boolean delete(int id) {
        // Сначала удаляем связи с пользователями
        String deleteRelations = "DELETE FROM user_groups WHERE group_id = ?";
        String deleteGroup = "DELETE FROM groups WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteRelations);
                 PreparedStatement pstmt2 = conn.prepareStatement(deleteGroup)) {

                pstmt1.setInt(1, id);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, id);
                int affected = pstmt2.executeUpdate();

                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Добавить пользователя в группу
    public boolean addUserToGroup(int userId, int groupId) {
        String sql = "INSERT INTO user_groups (user_id, group_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Удалить пользователя из группы
    public boolean removeUserFromGroup(int userId, int groupId) {
        String sql = "DELETE FROM user_groups WHERE user_id = ? AND group_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Получить участников группы (один JOIN-запрос вместо N+1)
    private List<User> getGroupMembers(int groupId) {
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.password_hash, u.full_name, u.role, u.created_at " +
                "FROM users u " +
                "JOIN user_groups ug ON u.id = ug.user_id " +
                "WHERE ug.group_id = ? ORDER BY u.full_name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                if (rs.getTimestamp("created_at") != null) {
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                members.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // Маппинг ResultSet в Group
    private Group mapResultSetToGroup(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getInt("id"));
        group.setName(rs.getString("name"));
        group.setDescription(rs.getString("description"));
        return group;
    }
}