package main.java.com.psychotest.service;

import main.java.com.psychotest.model.AnswerOption;
import main.java.com.psychotest.model.Parameter;
import main.java.com.psychotest.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Сервис умного отбора вопросов из банка для сессии тестирования.
 *
 * <p>Гарантирует, что отобранные вопросы в совокупности позволяют
 * достичь целевых значений по каждому параметру:
 * <ul>
 *   <li>если для параметра задан {@code targetMaxScore} — сумма лучших
 *       (максимальных) баллов по отобранным вопросам ≥ targetMaxScore;</li>
 *   <li>если задан {@code targetMinScore} — сумма худших (минимальных)
 *       баллов ≤ targetMinScore.</li>
 * </ul>
 *
 * <p>Алгоритм жадный: на каждом шаге выбирается вопрос, вносящий наибольший
 * вклад в устранение оставшихся «дефицитов». Когда все ограничения выполнены,
 * оставшиеся слоты заполняются случайными вопросами.
 */
public class QuestionSelectionService {

    /**
     * Отбирает {@code count} вопросов из {@code bank}.
     *
     * <p>Обязательные вопросы ({@code isMandatory() == true}) всегда включаются.
     * Из оставшихся слотов выбираются вопросы жадным алгоритмом по целевым значениям параметров.
     *
     * @param bank       полный банк вопросов теста
     * @param parameters параметры теста (с targetMinScore / targetMaxScore и реальными ID)
     * @param count      сколько вопросов нужно выдать в сессии
     * @return список отобранных вопросов (размер = min(count, bank.size()))
     */
    public List<Question> select(List<Question> bank, List<Parameter> parameters, int count) {
        if (bank.isEmpty() || count <= 0) return new ArrayList<>();
        if (count >= bank.size()) return new ArrayList<>(bank);

        // Разделяем на обязательные и необязательные
        List<Question> mandatory = new ArrayList<>();
        List<Question> optional  = new ArrayList<>();
        for (Question q : bank) {
            if (q.isMandatory()) mandatory.add(q);
            else optional.add(q);
        }

        List<Question> selected = new ArrayList<>(mandatory);

        // Если обязательных уже больше нужного — возвращаем только первые count обязательных
        if (selected.size() >= count) {
            return new ArrayList<>(selected.subList(0, count));
        }

        int remaining = count - selected.size();

        // Проверяем, есть ли параметры с целевыми значениями
        boolean hasTargets = parameters.stream().anyMatch(p ->
                p.getTargetMinScore() != null || p.getTargetMaxScore() != null);

        if (!hasTargets) {
            // Нет ограничений → добираем случайно из необязательных
            selected.addAll(randomSubset(optional, remaining));
            return selected;
        }

        // ---------- Жадный алгоритм для необязательных слотов ----------

        // Инициализируем накопленные суммы из уже выбранных обязательных вопросов
        int[] curMax = new int[parameters.size()];
        int[] curMin = new int[parameters.size()];
        for (Question mq : mandatory) {
            updateAccumulators(mq, curMax, curMin, parameters);
        }

        List<Question> remainingOptional = new ArrayList<>(optional);
        Collections.shuffle(remainingOptional); // случайный порядок для разрешения ничьих

        while (selected.size() < count && !remainingOptional.isEmpty()) {
            boolean constraintsMet = areAllConstraintsMet(curMax, curMin, parameters);

            Question best;
            if (constraintsMet) {
                best = remainingOptional.get(0);
            } else {
                best = chooseBest(remainingOptional, curMax, curMin, parameters);
            }

            selected.add(best);
            remainingOptional.remove(best);
            updateAccumulators(best, curMax, curMin, parameters);
        }

        return selected;
    }

    /** Обновляет накопленные maxDelta/minDelta после добавления вопроса. */
    private void updateAccumulators(Question q, int[] curMax, int[] curMin,
                                    List<Parameter> parameters) {
        for (int i = 0; i < parameters.size(); i++) {
            int paramId = parameters.get(i).getId();
            int maxD = 0, minD = 0;
            boolean found = false;
            for (AnswerOption opt : q.getAnswerOptions()) {
                Integer delta = opt.getParameterImpacts().get(paramId);
                if (delta != null) {
                    if (!found) { maxD = delta; minD = delta; found = true; }
                    else { maxD = Math.max(maxD, delta); minD = Math.min(minD, delta); }
                }
            }
            curMax[i] += maxD;
            curMin[i] += minD;
        }
    }

    // ─────────────────── вспомогательные методы ───────────────────

    /**
     * Проверяет, все ли ограничения выполнены при текущих суммах.
     */
    private boolean areAllConstraintsMet(int[] curMax, int[] curMin, List<Parameter> parameters) {
        for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            if (p.getTargetMaxScore() != null && curMax[i] < p.getTargetMaxScore()) return false;
            if (p.getTargetMinScore() != null && curMin[i] > p.getTargetMinScore()) return false;
        }
        return true;
    }

    /**
     * Выбирает из списка кандидатов вопрос, максимально сокращающий суммарный дефицит.
     */
    private Question chooseBest(List<Question> candidates, int[] curMax, int[] curMin,
                                 List<Parameter> parameters) {
        Question best = candidates.get(0);
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Question q : candidates) {
            double score = computeGainScore(q, curMax, curMin, parameters);
            if (score > bestScore) {
                bestScore = score;
                best = q;
            }
        }
        return best;
    }

    /**
     * Оценивает, насколько добавление вопроса {@code q} сокращает суммарный дефицит.
     *
     * <ul>
     *   <li>Дефицит по максимуму: max(0, targetMax − curMax[i]) → уменьшается на min(дефицит, maxDelta)</li>
     *   <li>Дефицит по минимуму: max(0, curMin[i] − targetMin) → уменьшается на min(дефицит, −minDelta)</li>
     * </ul>
     */
    private double computeGainScore(Question q, int[] curMax, int[] curMin,
                                     List<Parameter> parameters) {
        double gain = 0;
        for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);

            // Получаем дельты этого вопроса для параметра p
            int maxD = 0, minD = 0;
            for (AnswerOption opt : q.getAnswerOptions()) {
                Integer delta = opt.getParameterImpacts().get(p.getId());
                if (delta != null) {
                    maxD = Math.max(maxD, delta);
                    minD = Math.min(minD, delta);
                }
            }

            // Сколько дефицита по targetMax мы закроем?
            if (p.getTargetMaxScore() != null) {
                int defMax = Math.max(0, p.getTargetMaxScore() - curMax[i]);
                gain += Math.min(defMax, Math.max(0, maxD));
            }

            // Сколько дефицита по targetMin мы закроем?
            if (p.getTargetMinScore() != null) {
                int defMin = Math.max(0, curMin[i] - p.getTargetMinScore());
                gain += Math.min(defMin, Math.max(0, -minD));
            }
        }
        return gain;
    }

    /** Случайная подвыборка без повторений. */
    private List<Question> randomSubset(List<Question> bank, int count) {
        List<Question> copy = new ArrayList<>(bank);
        Collections.shuffle(copy);
        return new ArrayList<>(copy.subList(0, Math.min(count, copy.size())));
    }
}
