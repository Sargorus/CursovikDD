package main.java.com.psychotest.service;

import main.java.com.psychotest.exception.InvalidRangeException;
import main.java.com.psychotest.model.*;

import java.util.ArrayList;
import java.util.List;

public class TestValidationService {

    /**
     * Проверяет корректность добавляемого диапазона
     * @throws InvalidRangeException если диапазон некорректен или пересекается
     */
    public void validateNewRange(Parameter param, int min, int max) throws InvalidRangeException {
        // Проверка 1: min не может быть больше max
        if (min > max) {
            throw InvalidRangeException.invalidOrder(min, max);
        }

        // Проверка 2: проверка границ (если заданы)
        if (param.getMinValue() > 0 || param.getMaxValue() > 0) {
            if (min < param.getMinValue() || max > param.getMaxValue()) {
                throw InvalidRangeException.outOfBounds(min, param.getMinValue(), param.getMaxValue());
            }
        }

        // Проверка 3: нет пересечений с существующими диапазонами
        for (ParameterInterpretation existing : param.getInterpretations()) {
            if (existing.getRangeStart() != null && existing.getRangeEnd() != null) {
                if (isOverlapping(min, max, existing.getRangeStart(), existing.getRangeEnd())) {
                    throw InvalidRangeException.overlappingRange(
                            param.getName(), min, max,
                            existing.getRangeStart(), existing.getRangeEnd(),
                            existing.getInterpretationText()
                    );
                }
            }
        }
    }

    /**
     * Проверяет, что интерпретации покрывают весь возможный диапазон
     * @throws InvalidRangeException если есть "дырки" в покрытии
     */
    public void validateRangeCoverage(Parameter param, int minPossible, int maxPossible) throws InvalidRangeException {
        if (!param.getScaleType().equals("RANGE")) return;

        List<int[]> coveredRanges = new ArrayList<>();
        for (ParameterInterpretation interp : param.getInterpretations()) {
            if (interp.getRangeStart() != null && interp.getRangeEnd() != null) {
                coveredRanges.add(new int[]{interp.getRangeStart(), interp.getRangeEnd()});
            }
        }

        // Сортируем по началу
        coveredRanges.sort((a, b) -> Integer.compare(a[0], b[0]));

        // Проверяем покрытие
        int expectedStart = minPossible;
        for (int[] range : coveredRanges) {
            if (range[0] > expectedStart) {
                throw new InvalidRangeException(
                        "Обнаружен непокрытый диапазон [" + expectedStart + "-" + (range[0] - 1) + "] " +
                                "для параметра '" + param.getName() + "'"
                );
            }
            expectedStart = Math.max(expectedStart, range[1] + 1);
        }

        if (expectedStart <= maxPossible) {
            throw new InvalidRangeException(
                    "Диапазон [" + expectedStart + "-" + maxPossible + "] не покрыт интерпретациями " +
                            "для параметра '" + param.getName() + "'"
            );
        }
    }

    // Проверка пересечения двух отрезков
    private boolean isOverlapping(int a1, int a2, int b1, int b2) {
        return !(a2 < b1 || a1 > b2);
    }

    // ... остальные методы без изменений ...

    public PossibleRanges calculatePossibleRanges(TestState state) {
        PossibleRanges ranges = new PossibleRanges();

        for (Parameter param : state.getParameters()) {
            int minPossible = 0;
            int maxPossible = 0;
            int paramIndex = state.getParameters().indexOf(param);

            for (Question q : state.getQuestions()) {
                int minDelta = 0;
                int maxDelta = 0;

                for (AnswerOption opt : q.getAnswerOptions()) {
                    Integer delta = opt.getParameterImpacts().get(paramIndex);
                    if (delta != null) {
                        minDelta = Math.min(minDelta, delta);
                        maxDelta = Math.max(maxDelta, delta);
                    }
                }
                minPossible += minDelta;
                maxPossible += maxDelta;
            }

            ranges.add(param.getName(), minPossible, maxPossible);
        }

        return ranges;
    }

    public ValidationResult validate(TestState state) {
        ValidationResult result = new ValidationResult();

        // 1. Название
        if (state.getTestName().trim().isEmpty()) {
            result.addError("Введите название теста");
        }

        // 2. Параметры
        if (state.getParameters().isEmpty()) {
            result.addError("Добавьте хотя бы один параметр (шкалу)");
        } else {
            for (Parameter param : state.getParameters()) {
                if (param.getInterpretations().isEmpty()) {
                    result.addError("У параметра '" + param.getName() + "' нет интерпретаций");
                }

                // Проверка покрытия диапазонов
                if (param.getScaleType().equals("RANGE") && !state.getQuestions().isEmpty()) {
                    try {
                        PossibleRanges ranges = calculatePossibleRanges(state);
                        RangeInfo range = ranges.get(param.getName());
                        if (range != null) {
                            validateRangeCoverage(param, range.getMin(), range.getMax());
                        }
                    } catch (InvalidRangeException e) {
                        result.addError(e.getMessage());
                    }
                }
            }
        }

        // 3. Вопросы
        if (state.getQuestions().isEmpty()) {
            result.addError("Добавьте хотя бы один вопрос");
        } else {
            for (int i = 0; i < state.getQuestions().size(); i++) {
                Question q = state.getQuestions().get(i);
                if (q.getText().trim().isEmpty()) {
                    result.addError("Вопрос #" + (i+1) + " не имеет текста");
                }
                if (q.getAnswerOptions().size() < 2) {
                    result.addError("В вопросе #" + (i+1) + " должно быть минимум 2 варианта ответа");
                }
                if (q.getAnswerOptions().size() > 10) {
                    result.addError("В вопросе #" + (i+1) + " не может быть больше 10 вариантов ответа");
                }
            }
        }

        // 4. Возможные диапазоны
        PossibleRanges ranges = calculatePossibleRanges(state);
        result.setRangeInfo(ranges);

        return result;
    }

    // Добавьте в конец файла TestValidationService.java, перед последней скобкой }

    // Внутренние классы
    public static class PossibleRanges {
        private List<RangeInfo> ranges = new ArrayList<>();

        public void add(String paramName, int min, int max) {
            ranges.add(new RangeInfo(paramName, min, max));
        }

        public RangeInfo get(String paramName) {
            for (RangeInfo r : ranges) {
                if (r.getName().equals(paramName)) return r;
            }
            return null;
        }

        public List<RangeInfo> getAll() { return ranges; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n--- ВОЗМОЖНЫЕ ДИАПАЗОНЫ РЕЗУЛЬТАТОВ ---\n");
            for (RangeInfo r : ranges) {
                sb.append("• ").append(r.getName()).append(": от ").append(r.getMin());
                sb.append(" до ").append(r.getMax()).append("\n");
            }
            return sb.toString();
        }
    }

    public static class RangeInfo {
        private String name;
        private int min;
        private int max;

        public RangeInfo(String name, int min, int max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        public String getName() { return name; }
        public int getMin() { return min; }
        public int getMax() { return max; }
    }

    public static class ValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private PossibleRanges rangeInfo;

        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        public void setRangeInfo(PossibleRanges info) { this.rangeInfo = info; }

        public boolean isValid() { return errors.isEmpty(); }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }

        public String getErrorMessage() {
            return String.join("\n• ", errors);
        }

        public String getWarningMessage() {
            return String.join("\n• ", warnings);
        }

        public PossibleRanges getRangeInfo() { return rangeInfo; }
    }
}
