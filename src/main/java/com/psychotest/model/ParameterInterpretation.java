package main.java.com.psychotest.model;

import java.io.Serializable;

public class ParameterInterpretation implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int parameterId;
    private Integer rangeStart;
    private Integer rangeEnd;
    private String binaryValue;
    private String interpretationText;

    // Конструкторы
    public ParameterInterpretation() {}

    public ParameterInterpretation(String binaryValue, String interpretationText) {
        this.binaryValue = binaryValue;
        this.interpretationText = interpretationText;
    }

    public ParameterInterpretation(int rangeStart, int rangeEnd, String interpretationText) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.interpretationText = interpretationText;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getParameterId() { return parameterId; }
    public void setParameterId(int parameterId) { this.parameterId = parameterId; }

    public Integer getRangeStart() { return rangeStart; }
    public void setRangeStart(Integer rangeStart) { this.rangeStart = rangeStart; }

    public Integer getRangeEnd() { return rangeEnd; }
    public void setRangeEnd(Integer rangeEnd) { this.rangeEnd = rangeEnd; }

    public String getBinaryValue() { return binaryValue; }
    public void setBinaryValue(String binaryValue) { this.binaryValue = binaryValue; }

    public String getInterpretationText() { return interpretationText; }
    public void setInterpretationText(String interpretationText) { this.interpretationText = interpretationText; }
}
