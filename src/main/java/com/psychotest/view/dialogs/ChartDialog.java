package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.util.PieChartRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Диалог с круговыми диаграммами распределения результатов по параметрам.
 * Для каждого параметра строится отдельный «пирог».
 */
public class ChartDialog extends JDialog {

    public ChartDialog(Window parent, String testName,
                       Map<String, Map<String, Integer>> statistics) {
        super(parent, "Диаграмма результатов: " + testName,
                ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout());

        // ── заголовок ──────────────────────────────────────────────────────────
        JLabel header = new JLabel("Распределение участников по интерпретациям",
                SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        add(header, BorderLayout.NORTH);

        // ── панель с диаграммами ───────────────────────────────────────────────
        JPanel chartsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        chartsPanel.setBackground(Color.WHITE);

        if (statistics == null || statistics.isEmpty()) {
            chartsPanel.add(new JLabel("Нет данных для отображения"));
        } else {
            for (Map.Entry<String, Map<String, Integer>> entry : statistics.entrySet()) {
                chartsPanel.add(new PieChartPanel(entry.getKey(), entry.getValue(), 360, 310));
            }
        }

        JScrollPane scroll = new JScrollPane(chartsPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ── кнопка «Закрыть» ──────────────────────────────────────────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Закрыть");
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        // ── подбор размера окна ───────────────────────────────────────────────
        int count = (statistics == null) ? 0 : statistics.size();
        int cols  = Math.min(count, 2);
        int rows  = (count == 0) ? 1 : (int) Math.ceil((double) count / cols);
        setSize(Math.max(500, cols * 400 + 60),
                Math.min(750, rows * 340 + 100));
        setLocationRelativeTo(parent);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Вспомогательный компонент — один «пирог»
    // ──────────────────────────────────────────────────────────────────────────
    public static class PieChartPanel extends JPanel {
        private final String title;
        private final Map<String, Integer> data;

        public PieChartPanel(String title, Map<String, Integer> data, int w, int h) {
            this.title = title;
            this.data  = data;
            setPreferredSize(new Dimension(w, h));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 220), 1),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            PieChartRenderer.paint(g2, title, data, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
