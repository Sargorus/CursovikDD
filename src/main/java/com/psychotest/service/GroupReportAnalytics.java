package main.java.com.psychotest.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Статистические характеристики группы по одному параметру теста.
 * Все методы принимают готовый список UserScore, поэтому класс не зависит от БД.
 */
public class GroupReportAnalytics {

    public static class UserScore {
        private final String fullName;
        private final int score;

        public UserScore(String fullName, int score) {
            this.fullName = fullName;
            this.score = score;
        }

        public String getFullName() { return fullName; }
        public int getScore() { return score; }
    }

    /** Участники с максимальным баллом (могут быть равные). */
    public static List<UserScore> getMaxScoreHolders(List<UserScore> scores) {
        if (scores == null || scores.isEmpty()) return Collections.emptyList();
        int max = scores.stream().mapToInt(UserScore::getScore).max().orElse(0);
        return scores.stream().filter(s -> s.getScore() == max).collect(Collectors.toList());
    }

    /** Участники с минимальным баллом (могут быть равные). */
    public static List<UserScore> getMinScoreHolders(List<UserScore> scores) {
        if (scores == null || scores.isEmpty()) return Collections.emptyList();
        int min = scores.stream().mapToInt(UserScore::getScore).min().orElse(0);
        return scores.stream().filter(s -> s.getScore() == min).collect(Collectors.toList());
    }

    /** Среднее арифметическое. */
    public static double getMean(List<UserScore> scores) {
        if (scores == null || scores.isEmpty()) return 0;
        return scores.stream().mapToInt(UserScore::getScore).average().orElse(0);
    }

    /** Медиана (середина распределения, устойчива к выбросам). */
    public static double getMedian(List<UserScore> scores) {
        if (scores == null || scores.isEmpty()) return 0;
        List<Integer> sorted = scores.stream()
                .map(UserScore::getScore)
                .sorted()
                .collect(Collectors.toList());
        int n = sorted.size();
        return (n % 2 == 0)
                ? (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0
                : sorted.get(n / 2);
    }

    /**
     * Стандартное отклонение (показывает однородность группы:
     * малое СО — группа похожа, большое — нужен дифференцированный подход).
     */
    public static double getStdDev(List<UserScore> scores) {
        if (scores == null || scores.size() < 2) return 0;
        double mean = getMean(scores);
        double variance = scores.stream()
                .mapToDouble(s -> Math.pow(s.getScore() - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    /** Размах: максимум − минимум. */
    public static int getRange(List<UserScore> scores) {
        if (scores == null || scores.isEmpty()) return 0;
        int max = scores.stream().mapToInt(UserScore::getScore).max().orElse(0);
        int min = scores.stream().mapToInt(UserScore::getScore).min().orElse(0);
        return max - min;
    }

    /** Количество участников со значением строго выше среднего. */
    public static long countAboveMean(List<UserScore> scores) {
        if (scores == null || scores.isEmpty()) return 0;
        double mean = getMean(scores);
        return scores.stream().filter(s -> s.getScore() > mean).count();
    }

    /** Количество участников со значением строго ниже среднего. */
    public static long countBelowMean(List<UserScore> scores) {
        if (scores == null || scores.isEmpty()) return 0;
        double mean = getMean(scores);
        return scores.stream().filter(s -> s.getScore() < mean).count();
    }
}
