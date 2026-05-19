package main.java.com.psychotest.model;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private int id;
    private int testId;
    private String text;
    private int orderNum;
    private List<AnswerOption> answerOptions;

    public Question() {
        this.answerOptions = new ArrayList<>();
    }

    public Question(String text, int orderNum) {
        this();
        this.text = text;
        this.orderNum = orderNum;
    }

    // Геттеры
    public int getId() { return id; }
    public int getTestId() { return testId; }
    public String getText() { return text; }
    public int getOrderNum() { return orderNum; }
    public List<AnswerOption> getAnswerOptions() { return answerOptions; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setTestId(int testId) { this.testId = testId; }
    public void setText(String text) { this.text = text; }
    public void setOrderNum(int orderNum) { this.orderNum = orderNum; }
    public void setAnswerOptions(List<AnswerOption> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public void addAnswerOption(AnswerOption option) {
        this.answerOptions.add(option);
    }
}
