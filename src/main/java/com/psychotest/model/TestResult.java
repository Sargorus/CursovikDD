package main.java.com.psychotest.model;

import java.util.Map;

public class TestResult {
    private int sessionId;
    private int testId;
    private String testName;
    private Map<String, Integer> rawScores;
    private Map<String, Integer> scaledScores;
    private Map<String, String> interpretations;
    private boolean completed;
    private String errorMessage;

    // Геттеры и сеттеры
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public Map<String, Integer> getRawScores() { return rawScores; }
    public void setRawScores(Map<String, Integer> rawScores) { this.rawScores = rawScores; }

    public Map<String, Integer> getScaledScores() { return scaledScores; }
    public void setScaledScores(Map<String, Integer> scaledScores) { this.scaledScores = scaledScores; }

    public Map<String, String> getInterpretations() { return interpretations; }
    public void setInterpretations(Map<String, String> interpretations) { this.interpretations = interpretations; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
