package main.java.com.psychotest.service;

import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.TestResult;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.util.PieChartRenderer;
import java.util.stream.Collectors;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ExcelReportService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Экспортирует результаты теста в Excel файл (с диаграммами, списком участников
     * и листом групповой аналитики по параметрам).
     *
     * @param results      список результатов
     * @param test         тест
     * @param statistics   статистика интерпретаций: paramName → (метка → кол-во)
     * @param participants список участников по областям: paramName → (метка → [ФИО, ...])
     * @param paramScores  баллы участников по параметрам: paramName → [UserScore, ...]
     * @param filePath     путь для сохранения
     * @return true если успешно
     */
    public boolean exportResultsToExcel(List<ResultService.TestResult> results,
                                        Test test,
                                        Map<String, Map<String, Integer>> statistics,
                                        Map<String, Map<String, List<String>>> participants,
                                        Map<String, List<GroupReportAnalytics.UserScore>> paramScores,
                                        String filePath) {
        try (Workbook workbook = new HSSFWorkbook()) {

            Map<String, CellStyle> styles = createStyles(workbook);

            Sheet summarySheet = workbook.createSheet("Общая статистика");
            createSummarySheet(summarySheet, results, test, styles);

            Sheet detailsSheet = workbook.createSheet("Детальные результаты");
            createDetailsSheet(detailsSheet, results, test, styles);

            // Лист аналитики группы (если есть баллы)
            if (paramScores != null && !paramScores.isEmpty()) {
                Sheet analyticsSheet = workbook.createSheet("Аналитика группы");
                createGroupAnalyticsSheet(analyticsSheet, paramScores, styles);
            }

            // Лист с диаграммами (если есть данные)
            if (statistics != null && !statistics.isEmpty()) {
                Sheet chartsSheet = workbook.createSheet("Диаграммы");
                createChartsSheet(chartsSheet, statistics, participants, workbook, styles);
            }

            for (int i = 0; i <= 6; i++) {
                try { detailsSheet.autoSizeColumn(i); } catch (Exception ignored) {}
                try { summarySheet.autoSizeColumn(i); } catch (Exception ignored) {}
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Экспортирует детальные результаты конкретной сессии
     */
    public boolean exportSessionDetailsToExcel(ResultService.SessionDetail detail,
                                               String userName,
                                               String testName,
                                               String filePath) {
        try (Workbook workbook = new HSSFWorkbook()) {
            Map<String, CellStyle> styles = createStyles(workbook);

            // Лист с результатами по шкалам
            Sheet paramsSheet = workbook.createSheet("Результаты по шкалам");
            createParametersSheet(paramsSheet, detail, userName, testName, styles);

            // Лист с ответами на вопросы
            Sheet answersSheet = workbook.createSheet("Ответы на вопросы");
            createAnswersSheet(answersSheet, detail, userName, testName, styles);

            // Лист с интерпретацией
            Sheet interpretationSheet = workbook.createSheet("Интерпретация");
            createInterpretationSheet(interpretationSheet, detail, userName, testName, styles);

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Экспортирует результаты пробного запуска теста преподавателем в Excel.
     * Результаты не хранятся в БД — только интерпретация по параметрам.
     */
    public boolean exportPreviewResultToExcel(TestResult result, String testName, String filePath) {
        try (Workbook workbook = new HSSFWorkbook()) {
            Map<String, CellStyle> styles = createStyles(workbook);

            Sheet sheet = workbook.createSheet("Пробный запуск");
            int rowNum = 0;

            // Заголовок
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Результаты пробного запуска: " + testName);
            titleCell.setCellStyle(styles.get("header"));
            rowNum++;

            // Метаданные
            Row infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Тест:");
            infoRow.createCell(1).setCellValue(testName);

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Дата:");
            infoRow.createCell(1).setCellValue(java.time.LocalDateTime.now().format(DATE_FORMATTER));

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("⚠ Пробный запуск — результаты не сохранены в БД");

            rowNum++;

            // Шапка таблицы
            String[] columns = {"Параметр", "Интерпретация"};
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(styles.get("header"));
            }

            // Данные
            if (result.getInterpretations() != null) {
                for (Map.Entry<String, String> entry : result.getInterpretations().entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    CellStyle normal = styles.get("normal");
                    Cell c0 = row.createCell(0); c0.setCellValue(entry.getKey()); c0.setCellStyle(normal);
                    Cell c1 = row.createCell(1); c1.setCellValue(entry.getValue() != null ? entry.getValue() : ""); c1.setCellStyle(normal);
                }
            }

            try { sheet.autoSizeColumn(0); } catch (Exception ignored) {}
            try { sheet.autoSizeColumn(1); } catch (Exception ignored) {}

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createGroupAnalyticsSheet(Sheet sheet,
                                            Map<String, List<GroupReportAnalytics.UserScore>> paramScores,
                                            Map<String, CellStyle> styles) {
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Аналитика группы по параметрам");
        titleCell.setCellStyle(styles.get("header"));
        rowNum++;

        for (Map.Entry<String, List<GroupReportAnalytics.UserScore>> entry : paramScores.entrySet()) {
            String paramName = entry.getKey();
            List<GroupReportAnalytics.UserScore> scores = entry.getValue();

            // Заголовок параметра
            Row paramHeader = sheet.createRow(rowNum++);
            Cell paramCell = paramHeader.createCell(0);
            paramCell.setCellValue("Параметр: " + paramName);
            paramCell.setCellStyle(styles.get("header"));

            if (scores == null || scores.isEmpty()) {
                Row noData = sheet.createRow(rowNum++);
                noData.createCell(0).setCellValue("Нет данных");
                rowNum++;
                continue;
            }

            // Вычисляем статистику
            List<GroupReportAnalytics.UserScore> maxHolders = GroupReportAnalytics.getMaxScoreHolders(scores);
            List<GroupReportAnalytics.UserScore> minHolders = GroupReportAnalytics.getMinScoreHolders(scores);
            double median = GroupReportAnalytics.getMedian(scores);
            int    range  = GroupReportAnalytics.getRange(scores);
            int    total  = scores.size();

            String maxNames = maxHolders.stream()
                    .map(GroupReportAnalytics.UserScore::getFullName)
                    .collect(Collectors.joining(", "));
            String minNames = minHolders.stream()
                    .map(GroupReportAnalytics.UserScore::getFullName)
                    .collect(Collectors.joining(", "));

            // Краткая сводка
            rowNum = addAnalyticsRow(sheet, rowNum, styles, "Участников", String.valueOf(total), null);
            rowNum = addAnalyticsRow(sheet, rowNum, styles,
                    "Максимальный балл",
                    String.valueOf(maxHolders.get(0).getScore()),
                    maxNames);
            rowNum = addAnalyticsRow(sheet, rowNum, styles,
                    "Минимальный балл",
                    String.valueOf(minHolders.get(0).getScore()),
                    minNames);
            rowNum = addAnalyticsRow(sheet, rowNum, styles,
                    "Медиана",
                    String.format("%.2f", median), null);
            rowNum = addAnalyticsRow(sheet, rowNum, styles,
                    "Размах (макс − мин)",
                    String.valueOf(range), null);
            rowNum++;

            // Список по убыванию (от большего к меньшему)
            Row descHeader = sheet.createRow(rowNum++);
            Cell descCell = descHeader.createCell(0);
            descCell.setCellValue("По убыванию (от большего к меньшему)");
            descCell.setCellStyle(styles.get("header"));

            rowNum = addRankedListHeader(sheet, rowNum, styles);
            List<GroupReportAnalytics.UserScore> descending = scores.stream()
                    .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                    .collect(Collectors.toList());
            for (int i = 0; i < descending.size(); i++) {
                GroupReportAnalytics.UserScore us = descending.get(i);
                rowNum = addRankedListRow(sheet, rowNum, styles, i + 1, us.getFullName(), us.getScore());
            }
            rowNum++;

            // Список по возрастанию (от меньшего к большему)
            Row ascHeader = sheet.createRow(rowNum++);
            Cell ascCell = ascHeader.createCell(0);
            ascCell.setCellValue("По возрастанию (от меньшего к большему)");
            ascCell.setCellStyle(styles.get("header"));

            rowNum = addRankedListHeader(sheet, rowNum, styles);
            List<GroupReportAnalytics.UserScore> ascending = scores.stream()
                    .sorted((a, b) -> Integer.compare(a.getScore(), b.getScore()))
                    .collect(Collectors.toList());
            for (int i = 0; i < ascending.size(); i++) {
                GroupReportAnalytics.UserScore us = ascending.get(i);
                rowNum = addRankedListRow(sheet, rowNum, styles, i + 1, us.getFullName(), us.getScore());
            }

            rowNum += 2; // отступ между параметрами
        }

        try { sheet.autoSizeColumn(0); } catch (Exception ignored) {}
        try { sheet.autoSizeColumn(1); } catch (Exception ignored) {}
        try { sheet.autoSizeColumn(2); } catch (Exception ignored) {}
    }

    /**
     * Добавляет строку аналитики: метка | значение | [доп. инфо].
     * Возвращает следующий номер строки.
     */
    private int addAnalyticsRow(Sheet sheet, int rowNum, Map<String, CellStyle> styles,
                                 String label, String value, String extra) {
        Row row = sheet.createRow(rowNum);
        CellStyle normal = styles.get("normal");

        Cell c0 = row.createCell(0); c0.setCellValue(label); c0.setCellStyle(normal);
        Cell c1 = row.createCell(1); c1.setCellValue(value); c1.setCellStyle(normal);
        if (extra != null && !extra.isEmpty()) {
            Cell c2 = row.createCell(2); c2.setCellValue(extra); c2.setCellStyle(normal);
        }
        return rowNum + 1;
    }

    private int addRankedListHeader(Sheet sheet, int rowNum, Map<String, CellStyle> styles) {
        Row row = sheet.createRow(rowNum);
        CellStyle h = styles.get("header");
        Cell c0 = row.createCell(0); c0.setCellValue("№");      c0.setCellStyle(h);
        Cell c1 = row.createCell(1); c1.setCellValue("ФИО");    c1.setCellStyle(h);
        Cell c2 = row.createCell(2); c2.setCellValue("Балл");   c2.setCellStyle(h);
        return rowNum + 1;
    }

    private int addRankedListRow(Sheet sheet, int rowNum, Map<String, CellStyle> styles,
                                  int rank, String fullName, int score) {
        Row row = sheet.createRow(rowNum);
        CellStyle normal = styles.get("normal");
        Cell c0 = row.createCell(0); c0.setCellValue(rank);     c0.setCellStyle(normal);
        Cell c1 = row.createCell(1); c1.setCellValue(fullName); c1.setCellStyle(normal);
        Cell c2 = row.createCell(2); c2.setCellValue(score);    c2.setCellStyle(normal);
        return rowNum + 1;
    }

    private Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new java.util.HashMap<>();

        // Заголовок (жирный, с фоном)
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        styles.put("header", headerStyle);

        // Обычная ячейка
        CellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderTop(BorderStyle.THIN);
        normalStyle.setBorderLeft(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);
        styles.put("normal", normalStyle);

        // Зелёный для успешных
        CellStyle successStyle = workbook.createCellStyle();
        successStyle.cloneStyleFrom(normalStyle);
        successStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        successStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("success", successStyle);

        // Жёлтый для в процессе
        CellStyle warningStyle = workbook.createCellStyle();
        warningStyle.cloneStyleFrom(normalStyle);
        warningStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("warning", warningStyle);

        // Красный для прерванных
        CellStyle errorStyle = workbook.createCellStyle();
        errorStyle.cloneStyleFrom(normalStyle);
        errorStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("error", errorStyle);

        return styles;
    }

    private void createChartsSheet(Sheet sheet,
                                   Map<String, Map<String, Integer>> statistics,
                                   Map<String, Map<String, List<String>>> participants,
                                   Workbook workbook,
                                   Map<String, CellStyle> styles) {
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Диаграммы распределения результатов");
        titleCell.setCellStyle(styles.get("header"));
        rowNum++;

        Drawing<?> drawing  = sheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();

        for (Map.Entry<String, Map<String, Integer>> entry : statistics.entrySet()) {
            String paramName = entry.getKey();
            Map<String, Integer> paramStats = entry.getValue();
            Map<String, List<String>> paramParticipants =
                    (participants != null) ? participants.get(paramName) : null;

            // Подпись параметра (колонки 0 и 9)
            Row labelRow = sheet.createRow(rowNum);
            Cell labelCell = labelRow.createCell(0);
            labelCell.setCellValue(paramName);
            labelCell.setCellStyle(styles.get("header"));
            Cell listTitleCell = labelRow.createCell(9);
            listTitleCell.setCellValue("Список участников по областям");
            listTitleCell.setCellStyle(styles.get("header"));
            rowNum++;

            int imageStartRow = rowNum; // строка, с которой стартует изображение

            // ── Рендерим диаграмму в PNG ───────────────────────────────────────
            try {
                BufferedImage img = PieChartRenderer.render(paramName, paramStats, 600, 400);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "PNG", baos);
                int picIdx = workbook.addPicture(baos.toByteArray(), Workbook.PICTURE_TYPE_PNG);

                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0);
                anchor.setRow1(imageStartRow);
                anchor.setCol2(8);
                anchor.setRow2(imageStartRow + 22);
                drawing.createPicture(anchor, picIdx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // ── Таблица участников правее диаграммы (столбцы J=9, K=10) ───────
            int tableRow = imageStartRow;

            // Шапка таблицы
            Row colHeaderRow = getOrCreateRow(sheet, tableRow++);
            Cell nameHdr = colHeaderRow.createCell(9);
            nameHdr.setCellValue("Участник");
            nameHdr.setCellStyle(styles.get("header"));
            Cell areaHdr = colHeaderRow.createCell(10);
            areaHdr.setCellValue("Область диаграммы");
            areaHdr.setCellStyle(styles.get("header"));

            // Данные — идём по меткам в том же порядке, что и на диаграмме
            if (paramParticipants != null && !paramParticipants.isEmpty()) {
                for (String label : paramStats.keySet()) {
                    List<String> names = paramParticipants.get(label);
                    if (names == null) continue;
                    for (String name : names) {
                        Row dataRow = getOrCreateRow(sheet, tableRow++);
                        Cell nameCell = dataRow.createCell(9);
                        nameCell.setCellValue(name);
                        nameCell.setCellStyle(styles.get("normal"));
                        Cell areaCell = dataRow.createCell(10);
                        areaCell.setCellValue(label);
                        areaCell.setCellStyle(styles.get("normal"));
                    }
                }
            } else {
                Row noDataRow = getOrCreateRow(sheet, tableRow);
                noDataRow.createCell(9).setCellValue("Нет данных");
            }

            rowNum += 24; // высота изображения + отступ
        }

        // Авторазмер столбцов списка участников
        try { sheet.autoSizeColumn(9);  } catch (Exception ignored) {}
        try { sheet.autoSizeColumn(10); } catch (Exception ignored) {}
    }

    /** Возвращает существующую строку или создаёт новую, чтобы не затереть уже записанные ячейки */
    private Row getOrCreateRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return (row != null) ? row : sheet.createRow(rowIndex);
    }

    private void createSummarySheet(Sheet sheet, List<ResultService.TestResult> results,
                                    Test test, Map<String, CellStyle> styles) {
        int rowNum = 0;

        // Заголовок
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Отчёт по тесту: " + test.getName());
        titleCell.setCellStyle(styles.get("header"));

        rowNum++;

        // Информация о тесте
        Row infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Дата формирования:");
        infoRow.createCell(1).setCellValue(java.time.LocalDateTime.now().format(DATE_FORMATTER));

        infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Всего участников:");
        infoRow.createCell(1).setCellValue(results.size());

        long completedCount = results.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .count();
        infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Завершили тест:");
        infoRow.createCell(1).setCellValue(completedCount);

        rowNum += 2;

        // Таблица результатов
        String[] columns = {"№", "ФИО", "Логин", "Дата прохождения", "Статус"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(styles.get("header"));
        }

        int num = 1;
        for (ResultService.TestResult result : results) {
            Row row = sheet.createRow(rowNum++);
            CellStyle normal = styles.get("normal");

            Cell c0 = row.createCell(0); c0.setCellValue(num++); c0.setCellStyle(normal);
            Cell c1 = row.createCell(1); c1.setCellValue(result.getUserFullName()); c1.setCellStyle(normal);
            Cell c2 = row.createCell(2); c2.setCellValue(result.getUserLogin()); c2.setCellStyle(normal);
            Cell c3 = row.createCell(3); c3.setCellValue(result.getFormattedDate()); c3.setCellStyle(normal);

            Cell statusCell = row.createCell(4);
            statusCell.setCellValue(getStatusText(result.getStatus()));
            statusCell.setCellStyle(getStatusStyle(result.getStatus(), styles));
        }
    }

    private void createDetailsSheet(Sheet sheet, List<ResultService.TestResult> results,
                                    Test test, Map<String, CellStyle> styles) {
        int rowNum = 0;

        // Заголовок
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Детальные результаты теста: " + test.getName());
        titleCell.setCellStyle(styles.get("header"));

        rowNum++;

        // Таблица с деталями
        String[] columns = {"ID сессии", "ФИО", "Логин", "Дата начала", "Дата завершения",
                "Статус", "Время прохождения (мин)"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(styles.get("header"));
        }

        for (ResultService.TestResult result : results) {
            Row row = sheet.createRow(rowNum++);
            CellStyle normal = styles.get("normal");

            Cell c0 = row.createCell(0); c0.setCellValue(result.getSessionId()); c0.setCellStyle(normal);
            Cell c1 = row.createCell(1); c1.setCellValue(result.getUserFullName()); c1.setCellStyle(normal);
            Cell c2 = row.createCell(2); c2.setCellValue(result.getUserLogin()); c2.setCellStyle(normal);
            Cell c3 = row.createCell(3); c3.setCellValue(formatTimestamp(result.getStartTime())); c3.setCellStyle(normal);
            Cell c4 = row.createCell(4); c4.setCellValue(formatTimestamp(result.getEndTime())); c4.setCellStyle(normal);

            Cell statusCell = row.createCell(5);
            statusCell.setCellValue(getStatusText(result.getStatus()));
            statusCell.setCellStyle(getStatusStyle(result.getStatus(), styles));

            // Время прохождения в минутах
            long minutes = 0;
            if (result.getStartTime() != null && result.getEndTime() != null) {
                minutes = (result.getEndTime().getTime() - result.getStartTime().getTime()) / (60 * 1000);
            }
            Cell c6 = row.createCell(6); c6.setCellValue(minutes); c6.setCellStyle(normal);
        }
    }

    private void createParametersSheet(Sheet sheet, ResultService.SessionDetail detail,
                                       String userName, String testName,
                                       Map<String, CellStyle> styles) {
        int rowNum = 0;

        // Заголовок
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Результаты по шкалам");
        titleCell.setCellStyle(styles.get("header"));

        rowNum++;

        // Информация
        Row infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Тестируемый:");
        infoRow.createCell(1).setCellValue(userName);

        infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Тест:");
        infoRow.createCell(1).setCellValue(testName);

        infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Дата:");
        infoRow.createCell(1).setCellValue(java.time.LocalDateTime.now().format(DATE_FORMATTER));

        rowNum++;

        // Таблица параметров (без сырых баллов, только интерпретация и шкала)
        String[] columns = {"Параметр", "Тип шкалы", "Область", "Интерпретация"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(styles.get("header"));
        }

        if (detail.getParameterResults() != null) {
            for (var entry : detail.getParameterResults().entrySet()) {
                ResultService.ParameterResult pr = entry.getValue();
                Row row = sheet.createRow(rowNum++);
                CellStyle normal = styles.get("normal");

                Cell c0 = row.createCell(0); c0.setCellValue(pr.getParamName()); c0.setCellStyle(normal);
                Cell c1 = row.createCell(1); c1.setCellValue(pr.getScaleType().equals("BINARY") ? "Бинарная" : "Диапазонная"); c1.setCellStyle(normal);
                Cell c2 = row.createCell(2); c2.setCellValue(pr.getInterpretedCode() != null ? pr.getInterpretedCode() : "—"); c2.setCellStyle(normal);
                Cell c3 = row.createCell(3); c3.setCellValue(pr.getInterpretationText() != null ? pr.getInterpretationText() : ""); c3.setCellStyle(normal);
            }
        }
    }

    private void createAnswersSheet(Sheet sheet, ResultService.SessionDetail detail,
                                    String userName, String testName,
                                    Map<String, CellStyle> styles) {
        int rowNum = 0;

        // Заголовок
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Ответы на вопросы");
        titleCell.setCellStyle(styles.get("header"));

        rowNum++;

        // Информация
        Row infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Тестируемый:");
        infoRow.createCell(1).setCellValue(userName);

        infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Тест:");
        infoRow.createCell(1).setCellValue(testName);

        rowNum++;

        // Таблица вопросов и ответов
        String[] columns = {"№", "Вопрос", "Ответ"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(styles.get("header"));
        }

        if (detail.getAnswers() != null) {
            int num = 1;
            for (ResultService.AnswerDetail answer : detail.getAnswers()) {
                Row row = sheet.createRow(rowNum++);
                CellStyle normal = styles.get("normal");

                Cell c0 = row.createCell(0); c0.setCellValue(num++); c0.setCellStyle(normal);
                Cell c1 = row.createCell(1); c1.setCellValue(answer.getQuestionText()); c1.setCellStyle(normal);
                Cell c2 = row.createCell(2); c2.setCellValue(answer.getAnswerText()); c2.setCellStyle(normal);
            }
        }
    }

    private void createInterpretationSheet(Sheet sheet, ResultService.SessionDetail detail,
                                           String userName, String testName,
                                           Map<String, CellStyle> styles) {
        int rowNum = 0;

        // Заголовок
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Интерпретация результатов");
        titleCell.setCellStyle(styles.get("header"));

        rowNum++;

        // Информация
        Row infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Тестируемый:");
        infoRow.createCell(1).setCellValue(userName);

        infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Тест:");
        infoRow.createCell(1).setCellValue(testName);

        rowNum++;

        // Интерпретация по каждому параметру
        if (detail.getParameterResults() != null) {
            for (var entry : detail.getParameterResults().entrySet()) {
                ResultService.ParameterResult pr = entry.getValue();

                Row paramRow = sheet.createRow(rowNum++);
                Cell paramCell = paramRow.createCell(0);
                paramCell.setCellValue(pr.getParamName());
                paramCell.setCellStyle(styles.get("header"));

                if (pr.getInterpretedCode() != null && !pr.getInterpretedCode().isEmpty()) {
                    Row codeRow = sheet.createRow(rowNum++);
                    codeRow.createCell(0).setCellValue("Область: " + pr.getInterpretedCode());
                }

                Row interpRow = sheet.createRow(rowNum++);
                interpRow.createCell(0).setCellValue(pr.getInterpretationText() != null ? pr.getInterpretationText() : "");

                rowNum++; // Пустая строка между параметрами
            }
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        return timestamp.toLocalDateTime().format(DATE_FORMATTER);
    }

    private String getStatusText(String status) {
        switch (status) {
            case "COMPLETED": return "Завершён";
            case "IN_PROGRESS": return "В процессе";
            case "ABANDONED": return "Прерван";
            default: return status;
        }
    }

    private CellStyle getStatusStyle(String status, Map<String, CellStyle> styles) {
        switch (status) {
            case "COMPLETED": return styles.get("success");
            case "IN_PROGRESS": return styles.get("warning");
            case "ABANDONED": return styles.get("error");
            default: return styles.get("normal");
        }
    }
}