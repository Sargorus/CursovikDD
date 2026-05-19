package main.java.com.psychotest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Parameter implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int testId;
    private String name;
    private String scaleType; // "BINARY" или "RANGE"
    private int minValue;
    private int maxValue;
    private List<ParameterInterpretation> interpretations;

    public Parameter() {
        this.interpretations = new ArrayList<>();
    }

    public Parameter(String name, String scaleType) {
        this();
        this.name = name;
        this.scaleType = scaleType;
    }

    // Геттеры
    public int getId() { return id; }
    public int getTestId() { return testId; }
    public String getName() { return name; }
    public String getScaleType() { return scaleType; }
    public int getMinValue() { return minValue; }
    public int getMaxValue() { return maxValue; }
    public List<ParameterInterpretation> getInterpretations() { return interpretations; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setTestId(int testId) { this.testId = testId; }
    public void setName(String name) { this.name = name; }
    public void setScaleType(String scaleType) { this.scaleType = scaleType; }
    public void setMinValue(int minValue) { this.minValue = minValue; }
    public void setMaxValue(int maxValue) { this.maxValue = maxValue; }
    public void setInterpretations(List<ParameterInterpretation> interpretations) {
        this.interpretations = interpretations;
    }
}
