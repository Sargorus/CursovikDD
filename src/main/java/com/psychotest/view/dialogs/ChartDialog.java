package main.java.com.psychotest.view.dialogs;

import main.java.com.psychotest.util.PieChartRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Диалог с круговыми диаграммами распределения результатов по параметрам.
 * Рядом с каждой диаграммой отображается таблица участников с указанием
 * области диаграммы, в которую они попали.
 */
public class ChartDialog extends JDialog {

    /**
     * Основной конструктор.
     *
     * @param statistics  paramName → (метка → кол-во)
     * @param participants paramName → (метка → [ФИО участников])
     */
    public ChartDialog(Window parent, String testName,
                       Map<String, Map<String, Integer>> statistics,
                       Map<String, Map<String, List<String>>> participants) {
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
        JPanel chartsPanel = new JPanel();
        chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
        chartsPanel.setBackground(Color.WHITE);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (statistics == null || statistics.isEmpty()) {
            chartsPanel.add(new JLabel("Нет данных для отображения"));
        } else {
            for (Map.Entry<String, Map<String, Integer>> entry : statistics.entrySet()) {
                String paramName = entry.getKey();
                Map<String, Integer> paramStats = entry.getValue();
                Map<String, List<String>> paramParticipants =
                        (participants != null) ? participants.get(paramName) : null;

                JPanel row = buildParameterRow(paramName, paramStats, paramParticipants);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                chartsPanel.add(row);
                chartsPanel.add(Box.createVerticalStrut(14));
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
        setSize(920, Math.min(840, count * 320 + 130));
        setLocationRelativeTo(parent);
    }

    /** Обратная совместимость — без данных участников */
    public ChartDialog(Window parent, String testName,
                       Map<String, Map<String, Integer>> statistics) {
        this(parent, testName, statistics, null);
    }

    // ── Строка для одного параметра: [диаграмма | список участников] ──────────
    private JPanel buildParameterRow(String paramName,
                                     Map<String, Integer> stats,
                                     Map<String, List<String>> participants) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 220)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // Левая часть — диаграмма
        PieChartPanel chart = new PieChartPanel(paramName, stats, 280, 260);
        row.add(chart, BorderLayout.WEST);

        // Правая часть — список участников
        row.add(buildParticipantListPanel(paramName, stats, participants), BorderLayout.CENTER);

        return row;
    }

    /** Строит панель таблицы участников, сгруппированных по области диаграммы */
    private JPanel buildParticipantListPanel(String paramName,
                                             Map<String, Integer> stats,
                                             Map<String, List<String>> participants) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Участники по областям: " + paramName);
        title.setFont(new Font("Arial", Font.BOLD, 12));
        title.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));
        panel.add(title, BorderLayout.NORTH);

        // Таблица: Участник | Область диаграммы
        String[] cols = {"Участник", "Область диаграммы"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Порядок меток берём из stats, чтобы цвета в таблице совпадали с диаграммой
        String[] orderedLabels = (stats != null)
                ? stats.keySet().toArray(new String[0])
                : new String[0];

        if (participants != null && !participants.isEmpty()) {
            for (String label : orderedLabels) {
                List<String> names = participants.get(label);
                if (names == null) continue;
                for (String name : names) {
                    tableModel.addRow(new Object[]{name, label});
                }
            }
        }

        if (tableModel.getRowCount() == 0) {
            tableModel.addRow(new Object[]{"—", "Нет данных"});
        }

        JTable table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 230));
        table.getColumnModel().getColumn(0).setPreferredWidth(210);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);

        // Раскрашиваем строки в пастельный цвет соответствующего сектора
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    String label = (String) t.getModel().getValueAt(row, 1);
                    int idx = Arrays.asList(orderedLabels).indexOf(label);
                    if (idx >= 0) {
                        c.setBackground(mixWithWhite(
                                PieChartRenderer.CHART_COLORS[idx % PieChartRenderer.CHART_COLORS.length],
                                0.78f));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        // Заголовок таблицы — тёмный фон как у остального интерфейса
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(220, 220, 230));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(400, 230));
        panel.add(sp, BorderLayout.CENTER);

        // Итоговая строка: сколько всего участников
        int total = tableModel.getRowCount();
        if (total > 0 && !"—".equals(tableModel.getValueAt(0, 0))) {
            JLabel countLabel = new JLabel("Всего участников: " + total);
            countLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            countLabel.setForeground(Color.GRAY);
            countLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));
            panel.add(countLabel, BorderLayout.SOUTH);
        }

        return panel;
    }

    /** Смешивает цвет с белым, получая пастельный оттенок (factor 0.0–1.0 → чем выше, тем светлее) */
    private static Color mixWithWhite(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed()   + (255 - c.getRed())   * factor));
        int g = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, (int)(c.getBlue()  + (255 - c.getBlue())  * factor));
        return new Color(r, g, b);
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
