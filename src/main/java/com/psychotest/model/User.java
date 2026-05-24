package main.java.com.psychotest.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role; // ADMIN, TEACHER, TAKER
    private LocalDateTime createdAt;

    // Конструкторы
    public User() {}

    public User(String username, String passwordHash, String fullName, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    // Геттеры
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return fullName != null ? fullName : username;
    }

}