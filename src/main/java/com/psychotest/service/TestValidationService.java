package main.java.com.psychotest.service;

import main.java.com.psychotest.exception.InvalidRangeException;
import main.java.com.psychotest.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            PossibleRanges calcRanges = state.getQuestions().isEmpty()
                    ? null : calculatePossibleRanges(state);

            for (Parameter param : state.getParameters()) {
                if (param.getInterpretations().isEmpty()) {
                    result.addError("У параметра '" + param.getName() + "' нет интерпретаций");
                    continue;
                }

                if (param.getScaleType().equals("RANGE")) {
                    // Определяем ожидаемый диапазон для покрытия интерпретациями
                    Integer tMin = param.getTargetMinScore();
                    Integer tMax = param.getTargetMaxScore();

                    if (tMin != null && tMax != null) {
                        // Используем целевые значения как обязательный диапазон
                        try { validateRangeCoverage(param, tMin, tMax); }
                        catch (InvalidRangeException e) { result.addError(e.getMessage()); }

                        // Проверяем, что ни одна интерпретация не выходит за пределы
                        for (ParameterInterpretation interp : param.getInterpretations()) {
                            if (interp.getRangeStart() != null && interp.getRangeStart() < tMin) {
                                result.addError("Интерпретация параметра '" + param.getName()
                                        + "' начинается ниже целевого минимума " + tMin);
                            }
                            if (interp.getRangeEnd() != null && interp.getRangeEnd() > tMax) {
                                result.addError("Интерпретация параметра '" + param.getName()
                                        + "' выходит за целевой максимум " + tMax);
                            }
                        }
                    } else if (calcRanges != null) {
                        // Fallback: проверяем по расчётному диапазону из вопросов
                        RangeInfo range = calcRanges.get(param.getName());
                        if (range != null) {
                            try { validateRangeCoverage(param, range.getMin(), range.getMax()); }
                            catch (InvalidRangeException e) { result.addError(e.getMessage()); }
                        }
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

        // 5. Проверка банка вопросов (только если включён режим банка)
        validateQuestionBank(state, result);

        return result;
    }

    /**
     * Проверяет корректность банка вопросов:
     * <ul>
     *   <li>Слишком много обязательных вопросов (больше, чем questionsPerSession)</li>
     *   <li>Недостаточно вопросов для сессии</li>
     *   <li>Необязательные вопросы, которые никогда не попадут в сессию</li>
     *   <li>Недостижимость целевых значений параметров при любом составе сессии</li>
     * </ul>
     */
    public void validateQuestionBank(TestState state, ValidationResult result) {
        int K = state.getQuestionsPerSession();
        if (K <= 0 || state.getQuestions().isEmpty()) return; // режим банка не включён

        List<Question> allQ = state.getQuestions();
        List<Question> mandatory = allQ.stream()
                .filter(Question::isMandatory).collect(Collectors.toList());
        List<Question> optional = allQ.stream()
                .filter(q -> !q.isMandatory()).collect(Collectors.toList());

        int M = mandatory.size();
        int N = optional.size();
        int total = allQ.size();

        // Ошибка: больше обязательных, чем мест в сессии
        if (M > K) {
            result.addError("Обязательных вопросов (" + M + ") больше, чем вопросов в сессии ("
                    + K + "). Уменьшите количество обязательных или увеличьте размер сессии.");
            return;
        }

        // Ошибка: недостаточно вопросов для сессии вообще
        if (total < K) {
            result.addError("В банке " + total + " вопросов, но сессия требует " + K
                    + ". Добавьте ещё " + (K - total) + " вопрос(ов).");
            return;
        }

        // Предупреждение: необязательные вопросы, которые никогда не попадут
        if (M == K && N > 0) {
            result.addWarning("⚠ " + N + " необязательных вопрос(ов) никогда не попадут в сессию: "
                    + "обязательных вопросов ровно столько, сколько мест в сессии (" + K + ").");
        } else if (M < K) {
            // Необязательные вопросы, которые никогда не смогут попасть из-за математики
            // Это возможно только если N > 0 и M + N >= K (уже проверили выше),
            // но в будущем можно добавить проверку "вопросы заблокированные связями"
        }

        // Проверка достижимости целевых значений из доступного набора вопросов в сессии
        checkTargetAchievability(state, mandatory, optional, K, result);
    }

    /**
     * Проверяет, можно ли в принципе достичь целевых значений параметров
     * при самом выгодном составе сессии (обязательные + лучшие необязательные).
     */
    private void checkTargetAchievability(TestState state,
                                           List<Question> mandatory,
                                           List<Question> optional,
                                           int K, ValidationResult result) {
        List<Parameter> params = state.getParameters();
        int slots = K - mandatory.size(); // свободных слотов для необязательных

        for (Parameter param : params) {
            if (!"RANGE".equals(param.getScaleType())) continue;
            Integer tMax = param.getTargetMaxScore();
            Integer tMin = param.getTargetMinScore();
            if (tMax == null && tMin == null) continue;

            int paramIdx = params.indexOf(param);

            // Вклад обязательных вопросов
            int mandMaxSum = 0, mandMinSum = 0;
            for (Question q : mandatory) {
                int maxD = 0, minD = 0;
                boolean found = false;
                for (AnswerOption opt : q.getAnswerOptions()) {
                    Integer delta = opt.getParameterImpacts().get(paramIdx);
                    if (delta != null) {
                        if (!found) { maxD = delta; minD = delta; found = true; }
                        else { maxD = Math.max(maxD, delta); minD = Math.min(minD, delta); }
                    }
                }
                mandMaxSum += maxD;
                mandMinSum += minD;
            }

            // Выбираем лучшие необязательные для максимизации / минимизации
            List<Integer> optMaxDeltas = new ArrayList<>();
            List<Integer> optMinDeltas = new ArrayList<>();
            for (Question q : optional) {
                int maxD = 0, minD = 0;
                boolean found = false;
                for (AnswerOption opt : q.getAnswerOptions()) {
                    Integer delta = opt.getParameterImpacts().get(paramIdx);
                    if (delta != null) {
                        if (!found) { maxD = delta; minD = delta; found = true; }
                        else { maxD = Math.max(maxD, delta); minD = Math.min(minD, delta); }
                    }
                }
                optMaxDeltas.add(maxD);
                optMinDeltas.add(minD);
            }

            // Лучшие slots необязательных для максимума
            if (tMax != null && slots > 0) {
                List<Integer> sorted = optMaxDeltas.stream()
                        .sorted((a, b) -> b - a).collect(Collectors.toList());
                int bestOptMax = sorted.subList(0, Math.min(slots, sorted.size()))
                        .stream().mapToInt(Integer::intValue).sum();
                if (mandMaxSum + bestOptMax < tMax) {
                    result.addWarning("⚠ Параметр '" + param.getName()
                            + "': даже при самом выгодном составе сессии максимальный балл ("
                            + (mandMaxSum + bestOptMax) + ") не достигает целевого максимума ("
                            + tMax + "). Добавьте вопросы с большим влиянием или уменьшите цель.");
                }
            }

            // Лучшие slots необязательных для минимума
            if (tMin != null && slots > 0) {
                List<Integer> sorted = optMinDeltas.stream()
                        .sorted(Integer::compareTo).collect(Collectors.toList());
                int bestOptMin = sorted.subList(0, Math.min(slots, sorted.size()))
                        .stream().mapToInt(Integer::intValue).sum();
                if (mandMinSum + bestOptMin > tMin) {
                    result.addWarning("⚠ Параметр '" + param.getName()
                            + "': даже при самом невыгодном составе сессии минимальный балл ("
                            + (mandMinSum + bestOptMin) + ") не достигает целевого минимума ("
                            + tMin + "). Добавьте вопросы с меньшим (отрицательным) влиянием.");
                }
            }
        }
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
