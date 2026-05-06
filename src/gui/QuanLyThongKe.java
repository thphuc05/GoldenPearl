package gui;

import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class


QuanLyThongKe extends JPanel {

    // ── Colors ──────────────────────────────────────────────────────────────
    private final Color MAIN_BLUE   = Color.decode("#0B3D59");
    private final Color GOLD_COLOR  = Color.decode("#C5A059");
    private final Color CONTENT_BG  = Color.decode("#F0F2F5");
    private final Color CARD_BG     = Color.WHITE;
    private final Color TEXT_DARK   = Color.decode("#333333");
    private final Color BORDER_LIGHT = Color.decode("#E0E0E0");

    // ── State ────────────────────────────────────────────────────────────────
    private final HoaDon_DAO hd_dao = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO ct_dao = new ChiTietHoaDon_DAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private final DecimalFormat df = new DecimalFormat("#,###");

    // ── Widgets ──────────────────────────────────────────────────────────────
    private JLabel lblTotalRevenue, lblTotalInvoices;
    private JTextField txtFromDate, txtToDate;
    private DonutChart donutChart;
    private JPanel legendPanel;
    private JPanel miniCardsPanel;
    private JPanel bestSellersPanel;

    // ── Constructor ──────────────────────────────────────────────────────────
    public QuanLyThongKe() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);

        add(createTitlePanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);

        initDates();
    }

    public void refreshData() {
        initDates();
        loadData();
    }

    // ── Title ─────────────────────────────────────────────────────────────────
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        JLabel lbl = new JLabel("THỐNG KÊ DOANH THU NHÀ HÀNG", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl.setForeground(MAIN_BLUE);
        lbl.setBorder(new EmptyBorder(18, 0, 18, 0));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    // ── Content panel ────────────────────────────────────────────────────────
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        panel.add(createTopRow(), BorderLayout.NORTH);
        panel.add(createBottomRow(), BorderLayout.CENTER);
        return panel;
    }

    // ── Top row: stat cards + filter ─────────────────────────────────────────
    private JPanel createTopRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 15, 0));
        row.setBackground(CONTENT_BG);
        row.setPreferredSize(new Dimension(0, 130));

        lblTotalRevenue  = new JLabel("0 VNĐ");
        lblTotalInvoices = new JLabel("0");

        row.add(createStatCard("TỔNG DOANH THU", lblTotalRevenue, "💰", new Color(39, 174, 96)));
        row.add(createStatCard("TỔNG HÓA ĐƠN",   lblTotalInvoices, "🧾", new Color(52, 152, 219)));
        row.add(createFilterPanel());
        return row;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String icon, Color accent) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, getHeight() - 6, getWidth(), 6, 0, 0);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel textPanel = new JPanel(new BorderLayout(0, 6));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 100, 100));
        textPanel.add(lblTitle, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_DARK);
        textPanel.add(valueLabel, BorderLayout.CENTER);

        card.add(textPanel, BorderLayout.CENTER);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLbl, BorderLayout.EAST);

        return card;
    }

    private JPanel createFilterPanel() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(GOLD_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel filterTitle = new JLabel("BỘ LỌC TÌM KIẾM");
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterTitle.setForeground(GOLD_COLOR);
        filterTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(filterTitle, BorderLayout.NORTH);

        JPanel dateRow = new JPanel(new GridLayout(1, 4, 8, 0));
        dateRow.setOpaque(false);

        txtFromDate = createDateField();
        txtToDate   = createDateField();

        dateRow.add(makeLabel("Từ ngày:"));
        JPanel pFrom = makeDatePicker(txtFromDate);
        dateRow.add(pFrom);
        dateRow.add(makeLabel("Đến ngày:"));
        JPanel pTo = makeDatePicker(txtToDate);
        dateRow.add(pTo);
        card.add(dateRow, BorderLayout.CENTER);

        JButton btnView = new JButton("XEM DOANH THU");
        btnView.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnView.setBackground(GOLD_COLOR);
        btnView.setForeground(MAIN_BLUE);
        btnView.setFocusPainted(false);
        btnView.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnView.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnView.setBackground(GOLD_COLOR.brighter()); }
            public void mouseExited(MouseEvent e)  { btnView.setBackground(GOLD_COLOR); }
        });
        btnView.addActionListener(e -> loadData());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        btnPanel.setOpaque(false);
        btnPanel.add(btnView);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    // ── Bottom row: donut + right panel ──────────────────────────────────────
    private JPanel createBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 15, 0));
        row.setBackground(CONTENT_BG);
        row.add(createDonutPanel());
        row.add(createRightPanel());
        return row;
    }

    private JPanel createDonutPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel chartTitle = new JLabel("CƠ CẤU DOANH THU");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        chartTitle.setForeground(MAIN_BLUE);
        card.add(chartTitle, BorderLayout.NORTH);

        donutChart = new DonutChart();
        donutChart.setBackground(CARD_BG);
        card.add(donutChart, BorderLayout.CENTER);

        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        card.add(legendPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(CONTENT_BG);

        miniCardsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        miniCardsPanel.setBackground(CONTENT_BG);
        panel.add(miniCardsPanel, BorderLayout.NORTH);

        JPanel bestCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        bestCard.setOpaque(false);
        bestCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel bestTitle = new JLabel("Món ăn bán chạy nhất");
        bestTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bestTitle.setForeground(MAIN_BLUE);
        bestTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        bestCard.add(bestTitle, BorderLayout.NORTH);

        bestSellersPanel = new JPanel();
        bestSellersPanel.setLayout(new BoxLayout(bestSellersPanel, BoxLayout.Y_AXIS));
        bestSellersPanel.setOpaque(false);
        bestCard.add(bestSellersPanel, BorderLayout.CENTER);

        panel.add(bestCard, BorderLayout.CENTER);
        return panel;
    }

    // ── Data loading ──────────────────────────────────────────────────────────
    private void initDates() {
        Calendar cal = Calendar.getInstance();
        txtToDate.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_MONTH, -30);
        txtFromDate.setText(sdf.format(cal.getTime()));
    }

    private void loadData() {
        final String fromText = txtFromDate.getText();
        final String toText   = txtToDate.getText();
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                Date fromDate = sdf.parse(fromText);
                Date toDate   = sdf.parse(toText);
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromDate);
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
                Timestamp tsFrom = new Timestamp(cal.getTimeInMillis());
                cal.setTime(toDate);
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
                Timestamp tsTo = new Timestamp(cal.getTimeInMillis());
                List<HoaDon> invoices = hd_dao.getHoaDonByDateRange(fromDate, toDate);
                long paidCount = invoices.stream().filter(HoaDon::isTrangThai).count();
                double revenue = invoices.stream().filter(HoaDon::isTrangThai).mapToDouble(HoaDon::getTongTien).sum();
                Map<String, Double> catRevenue = ct_dao.getRevenueByCategoryInDateRange(tsFrom, tsTo);
                Map<String, Integer> top5 = ct_dao.getTop5SellingDishes();
                return new Object[]{paidCount, revenue, catRevenue, top5};
            }
            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] r = get();
                    long paidCount = (long) r[0];
                    double revenue = (double) r[1];
                    Map<String, Double>  catRevenue = (Map<String, Double>)  r[2];
                    Map<String, Integer> top5       = (Map<String, Integer>) r[3];
                    lblTotalRevenue.setText(df.format(revenue) + " VNĐ");
                    lblTotalInvoices.setText(String.valueOf(paidCount));
                    donutChart.setData(catRevenue);
                    updateLegend(catRevenue);
                    updateMiniCards(top5);
                    updateBestSellers(top5);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(QuanLyThongKe.this, "Vui lòng chọn ngày hợp lệ.");
                }
            }
        }.execute();
    }

    private void updateLegend(Map<String, Double> catRevenue) {
        legendPanel.removeAll();
        Color[] colors = donutChart.COLORS;
        double total = catRevenue.values().stream().mapToDouble(v -> v).sum();
        int ci = 0;
        for (Map.Entry<String, Double> e : catRevenue.entrySet()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setOpaque(false);

            JLabel dot = new JLabel("●");
            dot.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            dot.setForeground(colors[ci % colors.length]);
            row.add(dot);

            int pct = total > 0 ? (int) Math.round(e.getValue() / total * 100) : 0;
            JLabel txt = new JLabel(e.getKey() + "  " + pct + "%  (" + df.format(e.getValue()) + " VNĐ)");
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txt.setForeground(TEXT_DARK);
            row.add(txt);

            legendPanel.add(row);
            ci++;
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private void updateMiniCards(Map<String, Integer> top5) {
        miniCardsPanel.removeAll();
        Color[] accents = {
            new Color(39, 174, 96), new Color(52, 152, 219),
            new Color(231, 76, 60), new Color(243, 156, 18)
        };
        int idx = 0;
        for (Map.Entry<String, Integer> e : top5.entrySet()) {
            if (idx >= 4) break;
            miniCardsPanel.add(createMiniCard("Bán chạy: " + e.getKey(), e.getValue() + " suất", accents[idx]));
            idx++;
        }
        while (idx < 4) {
            miniCardsPanel.add(createMiniCard("—", "—", BORDER_LIGHT));
            idx++;
        }
        miniCardsPanel.revalidate();
        miniCardsPanel.repaint();
    }

    private JPanel createMiniCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.fillRoundRect(0, getHeight() - 4, getWidth(), 4, 0, 0);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(120, 120, 120));
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(TEXT_DARK);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    private void updateBestSellers(Map<String, Integer> top5) {
        bestSellersPanel.removeAll();
        int rank = 1;
        for (Map.Entry<String, Integer> e : top5.entrySet()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5, 0, 5, 0));

            JLabel lblRank = new JLabel(rank + "");
            lblRank.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblRank.setForeground(GOLD_COLOR);
            lblRank.setPreferredSize(new Dimension(24, 0));
            row.add(lblRank, BorderLayout.WEST);

            JLabel lblName = new JLabel(e.getKey());
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblName.setForeground(TEXT_DARK);
            row.add(lblName, BorderLayout.CENTER);

            JLabel lblQty = new JLabel(e.getValue() + " suất");
            lblQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblQty.setForeground(new Color(100, 100, 100));
            row.add(lblQty, BorderLayout.EAST);

            if (rank > 1) {
                row.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 0, 0, BORDER_LIGHT),
                    new EmptyBorder(5, 0, 5, 0)
                ));
            }
            bestSellersPanel.add(row);
            rank++;
        }
        bestSellersPanel.revalidate();
        bestSellersPanel.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JTextField createDateField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        f.setBackground(Color.WHITE);
        f.setForeground(MAIN_BLUE);
        f.setFont(new Font("Segoe UI", Font.BOLD, 13));
        f.setHorizontalAlignment(SwingConstants.CENTER);
        f.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        return f;
    }

    private JPanel makeDatePicker(JTextField field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JButton calBtn = new JButton("📅");
        calBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        calBtn.setBackground(GOLD_COLOR);
        calBtn.setForeground(MAIN_BLUE);
        calBtn.setFocusPainted(false);
        calBtn.setBorder(new EmptyBorder(4, 6, 4, 6));
        calBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calBtn.addActionListener(e -> {
            CustomDatePicker dlg = new CustomDatePicker(
                (JFrame) SwingUtilities.getWindowAncestor(this), field);
            dlg.setVisible(true);
        });

        p.add(field, BorderLayout.CENTER);
        p.add(calBtn, BorderLayout.EAST);
        return p;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }

    // ── DonutChart ────────────────────────────────────────────────────────────
    class DonutChart extends JPanel {
        private Map<String, Double> data = new LinkedHashMap<>();
        final Color[] COLORS = {
            Color.decode("#C5A059"), Color.decode("#27AE60"),
            Color.decode("#2980B9"), Color.decode("#E67E22"),
            Color.decode("#95A5A6")
        };

        public void setData(Map<String, Double> d) {
            this.data = d;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data.isEmpty()) {
                g2.setColor(BORDER_LIGHT);
                int s = Math.min(getWidth(), getHeight()) - 20;
                int x = (getWidth() - s) / 2, y = (getHeight() - s) / 2;
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(x, y, s, s);
                g2.setColor(new Color(180, 180, 180));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String msg = "Chưa có dữ liệu";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }

            double total = data.values().stream().mapToDouble(v -> v).sum();
            if (total == 0) { g2.dispose(); return; }

            int margin = 20;
            int size = Math.min(getWidth() - margin * 2, getHeight() - margin * 2);
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            int startAngle = 90;
            int ci = 0;
            for (Map.Entry<String, Double> e : data.entrySet()) {
                int arc = (int) Math.round(e.getValue() / total * 360);
                if (arc == 0) { ci++; continue; }
                g2.setColor(COLORS[ci % COLORS.length]);
                g2.fillArc(x, y, size, size, startAngle, arc);
                startAngle += arc;
                ci++;
            }

            // Donut hole
            int holeSize = size / 2;
            g2.setColor(getBackground());
            g2.fillOval(x + size / 4, y + size / 4, holeSize, holeSize);

            g2.dispose();
        }
    }

    // ── CustomDatePicker ──────────────────────────────────────────────────────
    class CustomDatePicker extends JDialog {
        private final JTextField target;
        private final Calendar cal;
        private JPanel daysPanel;
        private JLabel monthLabel;

        private final Color CAL_BG  = Color.decode("#EBF5FB");
        private final Color DAY_TEXT = MAIN_BLUE;

        public CustomDatePicker(JFrame parent, JTextField target) {
            super(parent, "Chọn ngày", true);
            this.target = target;
            this.cal    = Calendar.getInstance();
            try {
                if (!target.getText().isEmpty()) cal.setTime(sdf.parse(target.getText()));
            } catch (Exception ex) { /* keep today */ }

            setSize(320, 380);
            setLocationRelativeTo(target);
            setLayout(new BorderLayout());
            getContentPane().setBackground(CAL_BG);

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(MAIN_BLUE);
            header.setBorder(new EmptyBorder(5, 5, 5, 5));

            JButton btnPrev = makeNavBtn("<");
            JButton btnNext = makeNavBtn(">");
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setForeground(GOLD_COLOR);
            monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            updateHeader();

            btnPrev.addActionListener(e -> { cal.add(Calendar.MONTH, -1); updateCalendar(); });
            btnNext.addActionListener(e -> { cal.add(Calendar.MONTH, 1);  updateCalendar(); });

            header.add(btnPrev, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(btnNext, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
            daysPanel.setBackground(CAL_BG);
            daysPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            updateCalendar();
            add(daysPanel, BorderLayout.CENTER);
        }

        private JButton makeNavBtn(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btn.setForeground(GOLD_COLOR);
            btn.setContentAreaFilled(false);
            btn.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 1));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(45, 30));
            return btn;
        }

        private void updateHeader() {
            monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()).toUpperCase());
        }

        private void updateCalendar() {
            daysPanel.removeAll();
            updateHeader();

            String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
            for (String d : days) {
                JLabel l = new JLabel(d, SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setForeground(DAY_TEXT);
                daysPanel.add(l);
            }

            Calendar temp = (Calendar) cal.clone();
            temp.set(Calendar.DAY_OF_MONTH, 1);
            int startDay    = temp.get(Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
            Calendar today  = Calendar.getInstance();

            for (int i = 0; i < startDay; i++) daysPanel.add(new JLabel(""));

            for (int i = 1; i <= daysInMonth; i++) {
                final int day = i;
                JButton btn = new JButton(String.valueOf(i));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.setBackground(Color.WHITE);
                btn.setForeground(DAY_TEXT);
                btn.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 20)));

                temp.set(Calendar.DAY_OF_MONTH, day);
                boolean isSelected = sdf.format(temp.getTime()).equals(target.getText());
                boolean isToday    = sdf.format(temp.getTime()).equals(sdf.format(today.getTime()));

                if (isSelected) {
                    btn.setBackground(GOLD_COLOR);
                    btn.setForeground(MAIN_BLUE);
                } else if (isToday) {
                    btn.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 2));
                }

                btn.addActionListener(e -> {
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    target.setText(sdf.format(cal.getTime()));
                    dispose();
                });

                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (!btn.getBackground().equals(GOLD_COLOR))
                            btn.setBackground(new Color(235, 245, 251));
                    }
                    public void mouseExited(MouseEvent e) {
                        if (!btn.getBackground().equals(GOLD_COLOR))
                            btn.setBackground(Color.WHITE);
                    }
                });

                daysPanel.add(btn);
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }
}
