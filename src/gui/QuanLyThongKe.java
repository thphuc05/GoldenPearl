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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Màn hình Thống kê Doanh thu Nhà hàng.
 *
 * <p>Panel này cho phép người dùng xem tổng quan doanh thu theo khoảng thời gian
 * tùy chọn, bao gồm:</p>
 * <ul>
 *   <li>Tổng doanh thu và tổng số hóa đơn trong khoảng ngày đã chọn</li>
 *   <li>Biểu đồ Donut thể hiện cơ cấu doanh thu theo danh mục món ăn</li>
 *   <li>Top 4 món ăn bán chạy nhất (dạng mini card)</li>
 *   <li>Danh sách top 5 món ăn bán chạy nhất (dạng bảng xếp hạng)</li>
 * </ul>
 *
 * <p>Dữ liệu được tải bất đồng bộ bằng {@link SwingWorker} để tránh đơ giao diện.</p>
 */
public class QuanLyThongKe extends JPanel {

    // ── Colors ──────────────────────────────────────────────────────────────

    /** Màu xanh đậm chủ đạo của nhà hàng, dùng cho tiêu đề và text quan trọng. */
    private final Color MAIN_BLUE    = Color.decode("#0B3D59");

    /** Màu vàng gold thương hiệu, dùng cho các điểm nhấn và nút bấm. */
    private final Color GOLD_COLOR   = Color.decode("#C5A059");

    /** Màu nền của khu vực nội dung chính (xám nhạt). */
    private final Color CONTENT_BG   = Color.decode("#F0F2F5");

    /** Màu nền của các card (trắng). */
    private final Color CARD_BG      = Color.WHITE;

    /** Màu chữ mặc định (xám đậm). */
    private final Color TEXT_DARK    = Color.decode("#333333");

    /** Màu viền nhạt dùng để phân cách các phần tử. */
    private final Color BORDER_LIGHT = Color.decode("#E0E0E0");

    // ── State ────────────────────────────────────────────────────────────────

    /** DAO truy vấn bảng HoaDon — dùng để lấy danh sách hóa đơn theo ngày. */
    private final HoaDon_DAO hd_dao = new HoaDon_DAO();

    /** DAO truy vấn bảng ChiTietHoaDon — dùng để tính doanh thu theo danh mục và top món. */
    private final ChiTietHoaDon_DAO ct_dao = new ChiTietHoaDon_DAO();

    /** Định dạng số tiền với dấu phân cách hàng nghìn (ví dụ: 1,500,000). */
    private final DecimalFormat df = new DecimalFormat("#,###");

    // ── Widgets ──────────────────────────────────────────────────────────────

    private JLabel lblTotalRevenue;
    private JLabel lblTotalProfit;
    private JLabel lblTotalInvoices;

    /** Kỳ đang chọn: "TUAN" | "THANG" | "QUY" */
    private String currentPeriod = "THANG";
    private JButton btnTuan, btnThang, btnQuy;

    /** Biểu đồ Donut hiển thị cơ cấu doanh thu theo danh mục. */
    private DonutChart donutChart;

    /** Panel chứa chú thích màu sắc tương ứng với từng phần trên biểu đồ Donut. */
    private JPanel legendPanel;

    /** Panel chứa 4 mini card hiển thị top 4 món ăn bán chạy nhất. */
    private JPanel miniCardsPanel;

    /** Panel chứa danh sách xếp hạng top 5 món ăn bán chạy nhất. */
    private JPanel bestSellersPanel;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Khởi tạo màn hình thống kê.
     * Tự động thiết lập layout, thêm các thành phần giao diện,
     * và đặt ngày mặc định là 30 ngày gần nhất.
     */
    public QuanLyThongKe() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);

        add(createTitlePanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);

        // Mặc định: tháng này
        activatePeriod(btnThang);
        loadData();
    }

    /**
     * Làm mới dữ liệu thống kê.
     * Đặt lại ngày về mặc định (30 ngày gần nhất) rồi tải lại dữ liệu.
     * Được gọi từ bên ngoài khi chuyển tab sang màn hình này.
     */
    public void refreshData() {
        currentPeriod = "THANG";
        activatePeriod(btnThang);
        loadData();
    }

    // ── Title ─────────────────────────────────────────────────────────────────

    /**
     * Tạo panel tiêu đề phía trên cùng của màn hình.
     *
     * @return JPanel chứa label "THỐNG KÊ DOANH THU NHÀ HÀNG"
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BLUE);
        panel.setBorder(new EmptyBorder(10, 28, 10, 28));

        JLabel lbl = new JLabel("THỐNG KÊ DOANH THU NHÀ HÀNG");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(GOLD_COLOR);
        JLabel lblSub = new JLabel("Xem báo cáo doanh thu và xu hướng kinh doanh theo thời gian");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(180, 200, 220));
        JPanel pTBox = new JPanel(); pTBox.setLayout(new BoxLayout(pTBox, BoxLayout.Y_AXIS)); pTBox.setOpaque(false);
        pTBox.add(lbl); pTBox.add(Box.createVerticalStrut(2)); pTBox.add(lblSub);
        panel.add(pTBox, BorderLayout.WEST);
        return panel;
    }

    // ── Content panel ────────────────────────────────────────────────────────

    /**
     * Tạo panel nội dung chính, bao gồm 2 hàng:
     * <ul>
     *   <li>Hàng trên: 2 stat card + bộ lọc ngày</li>
     *   <li>Hàng dưới: biểu đồ Donut bên trái + top món + bảng xếp hạng bên phải</li>
     * </ul>
     *
     * @return JPanel tổng chứa toàn bộ nội dung
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        panel.add(createTopRow(), BorderLayout.NORTH);
        panel.add(createBottomRow(), BorderLayout.CENTER);
        return panel;
    }

    // ── Top row: stat cards + filter ─────────────────────────────────────────

    /**
     * Tạo hàng phía trên gồm 3 cột:
     * card tổng doanh thu, card tổng hóa đơn, và bộ lọc ngày.
     *
     * @return JPanel hàng trên với chiều cao cố định 130px
     */
    private JPanel createTopRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 15, 0));
        row.setBackground(CONTENT_BG);
        row.setPreferredSize(new Dimension(0, 130));

        lblTotalRevenue  = new JLabel("0 VNĐ");
        lblTotalProfit   = new JLabel("0 VNĐ");
        lblTotalInvoices = new JLabel("0");

        row.add(createStatCard("TỔNG DOANH THU", lblTotalRevenue,  "💰", new Color(39, 174, 96)));
        row.add(createStatCard("LỢI NHUẬN",      lblTotalProfit,   "📈", new Color(197, 160, 89)));
        row.add(createStatCard("TỔNG HÓA ĐƠN",   lblTotalInvoices, "🧾", new Color(52, 152, 219)));
        row.add(createPeriodPanel());
        return row;
    }

    /**
     * Tạo một card thống kê hình chữ nhật bo góc với thanh màu accent ở đáy.
     *
     * @param title      Tiêu đề hiển thị phía trên (ví dụ: "TỔNG DOANH THU")
     * @param valueLabel Label sẽ được cập nhật động khi tải dữ liệu
     * @param icon       Emoji icon hiển thị bên phải (ví dụ: "💰")
     * @param accent     Màu thanh nhấn ở đáy card
     * @return JPanel dạng stat card đã được thiết kế sẵn
     */
    private JPanel createStatCard(String title, JLabel valueLabel, String icon, Color accent) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Vẽ thanh màu accent ở đáy card
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

    private JPanel createPeriodPanel() {
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
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("KỲ BÁO CÁO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(GOLD_COLOR);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new GridLayout(1, 3, 8, 0));
        btnRow.setOpaque(false);
        btnTuan  = makePeriodBtn("Tuần này");
        btnThang = makePeriodBtn("Tháng này");
        btnQuy   = makePeriodBtn("Quý này");

        btnTuan.addActionListener(e  -> { currentPeriod = "TUAN";  activatePeriod(btnTuan);  loadData(); });
        btnThang.addActionListener(e -> { currentPeriod = "THANG"; activatePeriod(btnThang); loadData(); });
        btnQuy.addActionListener(e   -> { currentPeriod = "QUY";   activatePeriod(btnQuy);   loadData(); });

        btnRow.add(btnTuan);
        btnRow.add(btnThang);
        btnRow.add(btnQuy);
        card.add(btnRow, BorderLayout.CENTER);
        return card;
    }

    private JButton makePeriodBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void activatePeriod(JButton active) {
        for (JButton b : new JButton[]{btnTuan, btnThang, btnQuy}) {
            b.setBackground(Color.WHITE);
            b.setForeground(TEXT_DARK);
            b.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
            b.repaint();
        }
        active.setBackground(MAIN_BLUE);
        active.setForeground(Color.WHITE);
        active.setBorder(BorderFactory.createLineBorder(MAIN_BLUE));
        active.repaint();
    }

    // ── Bottom row: donut + right panel ──────────────────────────────────────

    /**
     * Tạo hàng phía dưới gồm 2 cột:
     * biểu đồ Donut bên trái và panel tổng hợp top món bên phải.
     *
     * @return JPanel hàng dưới chia đôi theo chiều ngang
     */
    private JPanel createBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 15, 0));
        row.setBackground(CONTENT_BG);
        row.add(createDonutPanel());
        row.add(createRightPanel());
        return row;
    }

    /**
     * Tạo panel bên trái chứa biểu đồ Donut và chú thích màu sắc.
     * Biểu đồ thể hiện tỷ lệ doanh thu của từng danh mục món ăn.
     *
     * @return JPanel card bo góc chứa DonutChart và legendPanel
     */
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

    /**
     * Tạo panel bên phải gồm 2 phần xếp dọc:
     * <ul>
     *   <li>Phía trên: 4 mini card top món ăn bán chạy (dạng lưới 2x2)</li>
     *   <li>Phía dưới: bảng xếp hạng chi tiết top 5 món ăn bán chạy</li>
     * </ul>
     *
     * @return JPanel tổng chứa miniCardsPanel và bestSellersPanel
     */
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

    /**
     * Khởi tạo ngày mặc định cho bộ lọc:
     * ngày kết thúc là hôm nay, ngày bắt đầu là 30 ngày trước.
     * Được gọi trong constructor và khi {@link #refreshData()} được gọi.
     */
    private void loadData() {
        Calendar cal = Calendar.getInstance();
        final Date start, end;

        switch (currentPeriod) {
            case "TUAN": {
                int dow  = cal.get(Calendar.DAY_OF_WEEK);
                int diff = (dow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dow);
                cal.add(Calendar.DAY_OF_YEAR, diff);
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                start = cal.getTime();
                cal.add(Calendar.DAY_OF_YEAR, 6);
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999);
                end = cal.getTime();
                break;
            }
            case "QUY": {
                int m      = cal.get(Calendar.MONTH);
                int qStart = (m / 3) * 3;
                cal.set(Calendar.MONTH, qStart);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                start = cal.getTime();
                cal.set(Calendar.MONTH, qStart + 2);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999);
                end = cal.getTime();
                break;
            }
            default: { // THANG
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
                start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999);
                end = cal.getTime();
            }
        }

        final Timestamp tsFrom = new Timestamp(start.getTime());
        final Timestamp tsTo   = new Timestamp(end.getTime());

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                // Dùng cùng tsFrom/tsTo cho tất cả queries — tránh lệch kết quả
                List<HoaDon> invoices = hd_dao.getHoaDonByDateRange(tsFrom, tsTo);
                long   paidCount = invoices.stream().filter(HoaDon::isTrangThai).count();
                double revenue   = invoices.stream().filter(HoaDon::isTrangThai).mapToDouble(HoaDon::getTongTien).sum();
                double profit    = ct_dao.getProfitByDateRange(tsFrom, tsTo);
                Map<String, Double>  catRevenue = ct_dao.getRevenueByCategoryInDateRange(tsFrom, tsTo);
                Map<String, Integer> top5       = ct_dao.getTop5SellingDishesByDateRange(tsFrom, tsTo);
                return new Object[]{paidCount, revenue, profit, catRevenue, top5};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] r        = get();
                    long   paidCount  = (long)   r[0];
                    double revenue    = (double)  r[1];
                    double profit     = (double)  r[2];
                    Map<String, Double>  catRevenue = (Map<String, Double>)  r[3];
                    Map<String, Integer> top5       = (Map<String, Integer>) r[4];

                    lblTotalRevenue.setText(df.format(revenue) + " VNĐ");
                    lblTotalProfit.setText(df.format(profit)   + " VNĐ");
                    lblTotalInvoices.setText(String.valueOf(paidCount));
                    donutChart.setData(catRevenue);
                    updateLegend(catRevenue);
                    updateMiniCards(top5);
                    updateBestSellers(top5);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(QuanLyThongKe.this, "Lỗi tải dữ liệu.");
                }
            }
        }.execute();
    }

    /**
     * Cập nhật phần chú thích (legend) bên dưới biểu đồ Donut.
     * Mỗi dòng gồm: chấm màu — tên danh mục — phần trăm — số tiền.
     *
     * @param catRevenue Map tên danh mục → doanh thu (VNĐ), đã lọc theo ngày
     */
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

    /**
     * Cập nhật 4 mini card hiển thị top 4 món ăn bán chạy nhất.
     * Nếu có ít hơn 4 món, các ô còn lại sẽ hiển thị "—".
     *
     * @param top5 Map tên món → số suất đã bán, sắp xếp giảm dần, đã lọc theo ngày
     */
    private void updateMiniCards(Map<String, Integer> top5) {
        miniCardsPanel.removeAll();
        Color[] accents = {
                new Color(39, 174, 96), new Color(52, 152, 219),
                new Color(231, 76, 60), new Color(243, 156, 18)
        };
        int idx = 0;
        for (Map.Entry<String, Integer> e : top5.entrySet()) {
            if (idx >= 4) break; // Chỉ hiển thị tối đa 4 mini card
            miniCardsPanel.add(createMiniCard("Bán chạy: " + e.getKey(), e.getValue() + " suất", accents[idx]));
            idx++;
        }
        // Điền card trống nếu có ít hơn 4 món
        while (idx < 4) {
            miniCardsPanel.add(createMiniCard("—", "—", BORDER_LIGHT));
            idx++;
        }
        miniCardsPanel.revalidate();
        miniCardsPanel.repaint();
    }

    /**
     * Tạo một mini card nhỏ hiển thị tên món và số suất đã bán.
     * Card có góc bo tròn và thanh màu accent ở đáy.
     *
     * @param title  Dòng tiêu đề (thường là tên món, ví dụ: "Bán chạy: Phở bò")
     * @param value  Giá trị hiển thị (ví dụ: "42 suất")
     * @param accent Màu thanh nhấn ở đáy card, phân biệt thứ hạng
     * @return JPanel dạng mini card đã được thiết kế sẵn
     */
    private JPanel createMiniCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Thanh màu nhỏ ở đáy để phân biệt thứ hạng
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

    /**
     * Cập nhật bảng xếp hạng top 5 món ăn bán chạy nhất.
     * Mỗi hàng hiển thị: số thứ hạng (màu vàng) — tên món — số suất.
     * Từ hàng thứ 2 trở đi có đường kẻ phân cách phía trên.
     *
     * @param top5 Map tên món → số suất đã bán, sắp xếp giảm dần, đã lọc theo ngày
     */
    private void updateBestSellers(Map<String, Integer> top5) {
        bestSellersPanel.removeAll();
        int rank = 1;
        for (Map.Entry<String, Integer> e : top5.entrySet()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5, 0, 5, 0));

            // Số thứ hạng màu vàng gold
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

            // Thêm đường kẻ phân cách phía trên cho các hàng từ thứ 2 trở đi
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


    // ── DonutChart ────────────────────────────────────────────────────────────

    /**
     * Component vẽ biểu đồ Donut (hình tròn có lỗ ở giữa) tùy chỉnh bằng Graphics2D.
     *
     * <p>Mỗi phần của biểu đồ tương ứng với một danh mục món ăn,
     * diện tích cung tỷ lệ thuận với doanh thu của danh mục đó.
     * Nếu chưa có dữ liệu, hiển thị vòng tròn rỗng kèm chữ "Chưa có dữ liệu".</p>
     */
    class DonutChart extends JPanel {

        /** Dữ liệu hiển thị: Map tên danh mục → doanh thu. */
        private Map<String, Double> data = new LinkedHashMap<>();

        /**
         * Bảng màu cho các phần của biểu đồ.
         * Màu được lấy theo chỉ số {@code ci % COLORS.length} để tránh tràn mảng.
         */
        final Color[] COLORS = {
                Color.decode("#C5A059"), Color.decode("#27AE60"),
                Color.decode("#2980B9"), Color.decode("#E67E22"),
                Color.decode("#95A5A6")
        };

        /**
         * Cập nhật dữ liệu và vẽ lại biểu đồ.
         *
         * @param d Map tên danh mục → doanh thu mới nhất từ database
         */
        public void setData(Map<String, Double> d) {
            this.data = d;
            repaint();
        }

        /**
         * Vẽ biểu đồ Donut lên component.
         *
         * <p>Thuật toán:</p>
         * <ol>
         *   <li>Tính tổng doanh thu để quy đổi sang góc độ (360°)</li>
         *   <li>Vẽ từng cung tròn (fillArc) với góc tỷ lệ theo doanh thu</li>
         *   <li>Vẽ hình tròn trắng ở giữa để tạo hiệu ứng lỗ Donut</li>
         * </ol>
         *
         * @param g Graphics context được cung cấp bởi Swing
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data.isEmpty()) {
                // Hiển thị vòng tròn rỗng và thông báo khi chưa có dữ liệu
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

            // Vẽ từng cung tương ứng với một danh mục, bắt đầu từ 12 giờ (90°)
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

            // Vẽ hình tròn trắng ở giữa để tạo hiệu ứng "lỗ" của Donut
            int holeSize = size / 2;
            g2.setColor(getBackground());
            g2.fillOval(x + size / 4, y + size / 4, holeSize, holeSize);

            g2.dispose();
        }
    }

}