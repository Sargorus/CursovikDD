package main.java.com.psychotest.model;

import java.time.LocalDateTime;
import java.util.List;

public class Test {
    private int id;
    private String name;
    private String description;
    private int createdBy;
    private int questionsPerSession;
    private LocalDateTime createdAt;
    private List<Question> questionBank;  // банк вопросов
    private List<Parameter> parameters;    // параметры

    public Test() {
        this.questionsPerSession = 0;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public int getQuestionsPerSession() { return questionsPerSession; }
    public void setQuestionsPerSession(int questionsPerSession) { this.questionsPerSession = questionsPerSession; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Question> getQuestionBank() { return questionBank; }
    public void setQuestionBank(List<Question> questionBank) { this.questionBank = questionBank; }

    public List<Parameter> getParameters() { return parameters; }
    public void setParameters(List<Parameter> parameters) { this.parameters = parameters; }

    @Override
    public String toString() {
        return name != null && !name.isEmpty() ? name : "Тест #" + id;
    }
}
