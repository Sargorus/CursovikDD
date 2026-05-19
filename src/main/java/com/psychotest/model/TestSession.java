package main.java.com.psychotest.model;

import java.time.LocalDateTime;

public class TestSession {
    private int id;
    private int userId;
    private int testId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // IN_PROGRESS, COMPLETED, ABANDONED

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}