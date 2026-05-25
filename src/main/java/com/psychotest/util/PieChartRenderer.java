package main.java.com.psychotest.util;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Утилита для отрисовки круговых диаграмм через Java2D.
 * Используется как во View (ChartDialog), так и в сервисном слое (ExcelReportService).
 */
public class PieChartRenderer {

    public static final Color[] CHART_COLORS = {
            new Color(70,  130, 200),   // синий
            new Color(220, 80,  80),    // красный
            new Color(80,  180, 80),    // зелёный
            new Color(240, 170, 50),    // жёлтый
            new Color(150, 90,  200),   // фиолетовый
            new Color(80,  200, 200),   // бирюзовый
            new Color(220, 120, 60),    // оранжевый
            new Color(180, 180, 60),    // оливковый
    };

    /**
     * Создаёт изображение круговой диаграммы заданного размера.
     */
    public static BufferedImage render(String title, Map<String, Integer> data,
                                       int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        paint(g2, title, data, width, height);
        g2.dispose();
        return img;
    }

    /**
     * Рисует диаграмму в переданный Graphics2D в области (0,0)–(w,h).
     */
    public static void paint(Graphics2D g2, String title,
                             Map<String, Integer> data, int w, int h) {
        // ── заголовок ──────────────────────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(40, 40, 80));
        FontMetrics fmTitle = g2.getFontMetrics();
        g2.drawString(title, (w - fmTitle.stringWidth(title)) / 2, 22);

        if (data == null || data.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            String nd = "Нет данных";
            g2.drawString(nd, (w - g2.getFontMetrics().stringWidth(nd)) / 2, h / 2);
            return;
        }

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) return;

        String[] keys   = data.keySet().toArray(new String[0]);
        int[]    values = new int[keys.length];
        for (int i = 0; i < keys.length; i++) values[i] = data.get(keys[i]);

        // ── размеры области pie + легенды ──────────────────────────────────────
        int legendLineH = 20;
        int legendH     = keys.length * legendLineH + 8;
        int pieAreaH    = h - 35 - legendH - 12;
        int pieSize     = Math.max(50, Math.min(w - 60, pieAreaH));
        int pieX        = (w - pieSize) / 2;
        int pieY        = 30;

        // ── секторы ────────────────────────────────────────────────────────────
        double startAngle = 90.0;
        for (int i = 0; i < keys.length; i++) {
            double arcAngle = -360.0 * values[i] / total;
            g2.setColor(CHART_COLORS[i % CHART_COLORS.length]);
            Arc2D.Double arc = new Arc2D.Double(
                    pieX, pieY, pieSize, pieSize, startAngle, arcAngle, Arc2D.PIE);
            g2.fill(arc);

            // белая рамка между секторами
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(arc);

            // процент внутри сектора (при достаточном угле)
            if (Math.abs(arcAngle) > 10) {
                double mid = Math.toRadians(startAngle + arcAngle / 2.0);
                int lx = (int)(pieX + pieSize / 2.0 + pieSize * 0.3 * Math.cos(mid));
                int ly = (int)(pieY + pieSize / 2.0 - pieSize * 0.3 * Math.sin(mid));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                String pct = Math.round(100.0 * values[i] / total) + "%";
                FontMetrics pfm = g2.getFontMetrics();
                g2.drawString(pct, lx - pfm.stringWidth(pct) / 2, ly + 4);
            }
            startAngle += arcAngle;
        }

        // ── легенда ────────────────────────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        int legendY = pieY + pieSize + 14;
        for (int i = 0; i < keys.length; i++) {
            int lx = 15;
            int ly = legendY + i * legendLineH;
            g2.setColor(CHART_COLORS[i % CHART_COLORS.length]);
            g2.fillRect(lx, ly - 11, 14, 14);
            g2.setColor(Color.DARK_GRAY);
            String label = keys[i] + "  —  " + values[i] +
                    " (" + Math.round(100.0 * values[i] / total) + "%)";
            g2.drawString(label, lx + 18, ly);
        }
    }
}
