package main.java.com.psychotest.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AnswerOption implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int questionId;
    private String text;
    private int orderNum;
    private Map<Integer, Integer> parameterImpacts; // parameterId -> delta

    public AnswerOption() {
        this.parameterImpacts = new HashMap<>();
    }

    public AnswerOption(String text, int orderNum) {
        this();
        this.text = text;
        this.orderNum = orderNum;
    }

    // Геттеры
    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getText() { return text; }
    public int getOrderNum() { return orderNum; }
    public Map<Integer, Integer> getParameterImpacts() { return parameterImpacts; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public void setText(String text) { this.text = text; }
    public void setOrderNum(int orderNum) { this.orderNum = orderNum; }
    public void setParameterImpacts(Map<Integer, Integer> parameterImpacts) {
        this.parameterImpacts = parameterImpacts;
    }

    public void addParameterImpact(int parameterId, int delta) {
        this.parameterImpacts.put(parameterId, delta);
    }
}