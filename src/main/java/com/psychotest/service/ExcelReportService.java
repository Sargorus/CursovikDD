package main.java.com.psychotest.service;

import main.java.com.psychotest.model.Test;
import main.java.com.psychotest.model.User;
import main.java.com.psychotest.util.PieChartRenderer;
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
     * Экспортирует результаты теста в Excel файл (с диаграммами распределения).
     *
     * @param results    список результатов
     * @param test       тест
     * @param statistics статистика интерпретаций: paramName → (метка → кол-во)
     * @param filePath   путь для сохранения
     * @return true если успешно
     */
    public boolean exportResultsToExcel(List<ResultService.TestResult> results,
                                        Test test,
                                        Map<String, Map<String, Integer>> statistics,
                                        String filePath) {
        try (Workbook workbook = new HSSFWorkbook()) {

            Map<String, CellStyle> styles = createStyles(workbook);

            Sheet summarySheet = workbook.createSheet("Общая статистика");
            createSummarySheet(summarySheet, results, test, styles);

            Sheet detailsSheet = workbook.createSheet("Детальные результаты");
            createDetailsSheet(detailsSheet, results, test, styles);

            // Лист с диаграммами (если есть данные)
            if (statistics != null && !statistics.isEmpty()) {
                Sheet chartsSheet = workbook.createSheet("Диаграммы");
                createChartsSheet(chartsSheet, statistics, workbook, styles);
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
            // Подпись параметра
            Row labelRow = sheet.createRow(rowNum);
            Cell labelCell = labelRow.createCell(0);
            labelCell.setCellValue(entry.getKey());
            labelCell.setCellStyle(styles.get("header"));
            rowNum++;

            // Рендерим диаграмму в PNG
            try {
                BufferedImage img = PieChartRenderer.render(
                        entry.getKey(), entry.getValue(), 600, 400);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "PNG", baos);
                int picIdx = workbook.addPicture(
                        baos.toByteArray(), Workbook.PICTURE_TYPE_PNG);

                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0);
                anchor.setRow1(rowNum);
                anchor.setCol2(8);
                anchor.setRow2(rowNum + 22);
                drawing.createPicture(anchor, picIdx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            rowNum += 24; // высота изображения + отступ
        }
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

        // Таблица параметров
        String[] columns = {"Параметр", "Тип шкалы", "Сырой балл", "Итоговый балл", "Код", "Интерпретация"};
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
                Cell c2 = row.createCell(2); c2.setCellValue(pr.getRawScore()); c2.setCellStyle(normal);
                Cell c3 = row.createCell(3); c3.setCellValue(pr.getScaledScore()); c3.setCellStyle(normal);
                Cell c4 = row.createCell(4); c4.setCellValue(pr.getInterpretedCode() != null ? pr.getInterpretedCode() : ""); c4.setCellStyle(normal);
                Cell c5 = row.createCell(5); c5.setCellValue(pr.getInterpretationText()); c5.setCellStyle(normal);
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

                rowNum++;

                Row scoreRow = sheet.createRow(rowNum++);
                scoreRow.createCell(0).setCellValue("Сырой балл: " + pr.getRawScore());
                scoreRow.createCell(1).setCellValue("Итоговый балл: " + pr.getScaledScore());

                if (pr.getInterpretedCode() != null && !pr.getInterpretedCode().isEmpty()) {
                    Row codeRow = sheet.createRow(rowNum++);
                    codeRow.createCell(0).setCellValue("Код: " + pr.getInterpretedCode());
                }

                Row interpRow = sheet.createRow(rowNum++);
                interpRow.createCell(0).setCellValue(pr.getInterpretationText());

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