package gui;

import dao.ChiTietHoaDon_DAO;
import dao.ChiTietHoaDon_DAO.MonBep;
import entity.TrangThaiMon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class QuanLyBep extends JPanel {

    private static final Color BG         = Color.decode("#F0F2F5");
    private static final Color MAIN_BLUE  = Color.decode("#0B3D59");
    private static final Color GOLD_COLOR = Color.decode("#C5A059");
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color BORDER     = Color.decode("#E0E0E0");
    private static final Color C_CHO      = Color.decode("#FFA726"); // cam - chờ
    private static final Color C_LAM      = Color.decode("#1565C0"); // xanh dương - đang làm
    private static final Color C_XONG     = Color.decode("#2E7D32"); // xanh lá - xong
    private static final Color TEXT_DARK  = Color.decode("#333333");

    private final ChiTietHoaDon_DAO cthdDAO = new ChiTietHoaDon_DAO();
    private final SimpleDateFormat timeFmt  = new SimpleDateFormat("HH:mm");

    private JPanel pContent;
    private JLabel lblCount;
    private JLabel lblRefresh;
    private Timer autoRefreshTimer;
    private int countDown = 30;
    private List<MonBep> currentDs = new java.util.ArrayList<>();

    public QuanLyBep() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        add(buildHeader(), BorderLayout.NORTH);

        pContent = new JPanel();
        pContent.setLayout(new BoxLayout(pContent, BoxLayout.Y_AXIS));
        pContent.setOpaque(false);
        pContent.setBorder(new EmptyBorder(4, 28, 20, 28));

        JScrollPane scroll = new JScrollPane(pContent);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        startAutoRefresh();
        refreshData();
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(true);
        p.setBackground(MAIN_BLUE);
        p.setBorder(new EmptyBorder(10, 28, 10, 28));

        JLabel lblTitle = new JLabel("QUẢN LÝ BẾP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(GOLD_COLOR);
        JLabel lblSub = new JLabel("Xem và xác nhận trạng thái các món đang chờ chế biến");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(180, 200, 220));
        JPanel pLeft = new JPanel(); pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS)); pLeft.setOpaque(false);
        pLeft.add(lblTitle); pLeft.add(Box.createVerticalStrut(2)); pLeft.add(lblSub);

        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pRight.setOpaque(false);

        lblCount = new JLabel("0 món đang chờ");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCount.setForeground(new Color(180, 200, 220));

        lblRefresh = new JLabel("Tự làm mới: 30s");
        lblRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRefresh.setForeground(new Color(180, 200, 220));

        JButton btnRefresh = new JButton("⟳ Làm mới ngay");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(GOLD_COLOR);
        btnRefresh.setForeground(MAIN_BLUE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnRefresh.addActionListener(e -> { countDown = 30; refreshData(); });

        pRight.add(lblRefresh);
        pRight.add(lblCount);
        pRight.add(btnRefresh);

        p.add(pLeft, BorderLayout.WEST);
        p.add(pRight, BorderLayout.EAST);
        return p;
    }

    // ── Auto refresh ─────────────────────────────────────────────────────────
    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                countDown--;
                SwingUtilities.invokeLater(() ->
                    lblRefresh.setText("Tự làm mới: " + countDown + "s"));
                if (countDown <= 0) {
                    countDown = 30;
                    SwingUtilities.invokeLater(() -> refreshData());
                }
            }
        }, 1000, 1000);
    }

    // ── Load data ────────────────────────────────────────────────────────────
    public void refreshData() {
        new SwingWorker<List<MonBep>, Void>() {
            @Override protected List<MonBep> doInBackground() {
                return cthdDAO.getDsMonChoXuLy();
            }
            @Override protected void done() {
                try {
                    currentDs = get();
                    renderOrders(currentDs);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void renderOrders(List<MonBep> ds) {
        pContent.removeAll();

        if (ds.isEmpty()) {
            JLabel lbl = new JLabel("Không có món nào đang chờ xử lý", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lbl.setForeground(new Color(150, 150, 150));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setBorder(new EmptyBorder(60, 0, 0, 0));
            pContent.add(lbl);
            lblCount.setText("Không có món đang chờ");
            pContent.revalidate();
            pContent.repaint();
            return;
        }

        // Nhóm theo đơn đặt bàn (maDon); walk-in nhóm theo maHD
        Map<String, List<MonBep>> byDon = new LinkedHashMap<>();
        for (MonBep m : ds) {
            String key = (m.maDon != null && !m.maDon.isEmpty()) ? m.maDon : "WALKIN_" + m.maHD;
            byDon.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(m);
        }

        long choCount = ds.stream().filter(m -> m.trangThaiMon == TrangThaiMon.CHO_XU_LY).count();
        long lamCount = ds.stream().filter(m -> m.trangThaiMon == TrangThaiMon.DANG_LAM).count();
        lblCount.setText(choCount + " chờ  |  " + lamCount + " đang làm");

        for (List<MonBep> group : byDon.values()) {
            MonBep first = group.get(0);
            String tenCum = buildTenCum(first);
            pContent.add(buildBanSection(tenCum, group));
            pContent.add(Box.createVerticalStrut(14));
        }

        pContent.revalidate();
        pContent.repaint();
    }

    // ── Tên cụm bàn ─────────────────────────────────────────────────────────
    private String buildTenCum(MonBep first) {
        if (first.tenCum != null && !first.tenCum.isEmpty()) {
            // tenCum = "1+2" → hiển thị "BÀN 01+02"
            String[] parts = first.tenCum.split("\\+");
            StringBuilder sb = new StringBuilder("BÀN ");
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) sb.append("+");
                try { sb.append(String.format("%02d", Integer.parseInt(parts[i].trim()))); }
                catch (NumberFormatException e) { sb.append(parts[i].trim()); }
            }
            return sb.toString();
        }
        return first.soBan == 0 ? "Không xác định bàn" : "BÀN " + String.format("%02d", first.soBan);
    }

    // ── Card theo nhóm đơn ───────────────────────────────────────────────────
    private JPanel buildBanSection(String tenBan, List<MonBep> items) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Header bàn
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBan = new JLabel(tenBan);
        lblBan.setFont(new Font("Inter Bold", Font.BOLD, 15));
        lblBan.setForeground(MAIN_BLUE);
        header.add(lblBan, BorderLayout.WEST);

        JLabel lblSoMon = new JLabel(items.size() + " món");
        lblSoMon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoMon.setForeground(new Color(100, 100, 100));
        header.add(lblSoMon, BorderLayout.EAST);

        section.add(header);
        section.add(Box.createVerticalStrut(6));

        // Grid các thẻ món
        JPanel grid = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (MonBep m : items) {
            grid.add(buildMonCard(m));
        }

        section.add(grid);
        section.add(Box.createVerticalStrut(4));

        // Đường kẻ phân cách
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        section.add(sep);

        return section;
    }

    // ── Thẻ từng món ─────────────────────────────────────────────────────────
    private JPanel buildMonCard(MonBep m) {
        Color statusColor = m.trangThaiMon == TrangThaiMon.CHO_XU_LY ? C_CHO : C_LAM;
        String statusLabel = m.trangThaiMon == TrangThaiMon.CHO_XU_LY ? "CHỜ" : "ĐANG LÀM";
        String btnLabel    = m.trangThaiMon == TrangThaiMon.CHO_XU_LY ? "Bắt đầu làm" : "✓ Xong";
        TrangThaiMon nextStatus = m.trangThaiMon == TrangThaiMon.CHO_XU_LY ? TrangThaiMon.DANG_LAM : TrangThaiMon.DA_XONG;
        Color btnColor = m.trangThaiMon == TrangThaiMon.CHO_XU_LY ? C_CHO : C_XONG;

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(statusColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.fillRoundRect(0, 0, 6, getHeight(), 0, 0);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(0, 6));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(220, 140));
        card.setBorder(new EmptyBorder(10, 14, 10, 12));

        // Status badge
        JLabel lblStatus = new JLabel(statusLabel);
        lblStatus.setFont(new Font("Inter Bold", Font.BOLD, 10));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setOpaque(true);
        lblStatus.setBackground(statusColor);
        lblStatus.setBorder(new EmptyBorder(2, 8, 2, 8));

        JPanel pTop = new JPanel(new BorderLayout());
        pTop.setOpaque(false);
        pTop.add(lblStatus, BorderLayout.EAST);

        // Tên món
        JLabel lblTen = new JLabel("<html><b>" + m.tenMon + "</b></html>");
        lblTen.setFont(new Font("Inter Bold", Font.BOLD, 14));
        lblTen.setForeground(TEXT_DARK);

        // Số lượng + giờ
        String tgStr = m.thoiGianGoi != null ? timeFmt.format(m.thoiGianGoi) : "--:--";
        JLabel lblInfo = new JLabel("x" + m.soLuong + "   |   " + tgStr);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(100, 100, 100));

        // Ghi chú
        JLabel lblGhiChu = new JLabel(
            (m.ghiChu != null && !m.ghiChu.isEmpty()) ? "📝 " + m.ghiChu : " ");
        lblGhiChu.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblGhiChu.setForeground(new Color(130, 100, 30));

        // Nút hành động
        JButton btn = new JButton(btnLabel) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(btnColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter Bold", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btn.addActionListener(e -> onUpdateStatus(m, nextStatus, btn));

        JPanel pCenter = new JPanel();
        pCenter.setLayout(new BoxLayout(pCenter, BoxLayout.Y_AXIS));
        pCenter.setOpaque(false);
        pCenter.add(lblTen);
        pCenter.add(Box.createVerticalStrut(3));
        pCenter.add(lblInfo);
        pCenter.add(Box.createVerticalStrut(2));
        pCenter.add(lblGhiChu);

        card.add(pTop, BorderLayout.NORTH);
        card.add(pCenter, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    // ── Xử lý cập nhật trạng thái ────────────────────────────────────────────
    private void onUpdateStatus(MonBep m, TrangThaiMon nextStatus, JButton btn) {
        TrangThaiMon prevStatus = m.trangThaiMon;

        // Optimistic update: cập nhật UI ngay lập tức, không chờ DB
        m.trangThaiMon = nextStatus;
        if (nextStatus == TrangThaiMon.DA_XONG) {
            currentDs.remove(m);
        }
        countDown = 30;
        renderOrders(currentDs);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return cthdDAO.updateTrangThaiMon(m.maHD, m.maMon,
                        (java.sql.Timestamp) m.thoiGianGoi, nextStatus);
            }
            @Override protected void done() {
                try {
                    if (!get()) {
                        // DB thất bại: hoàn tác optimistic update
                        m.trangThaiMon = prevStatus;
                        if (nextStatus == TrangThaiMon.DA_XONG) {
                            currentDs.add(m);
                        }
                        renderOrders(currentDs);
                        JOptionPane.showMessageDialog(QuanLyBep.this,
                            "Không thể cập nhật trạng thái. Vui lòng thử lại.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    m.trangThaiMon = prevStatus;
                    if (nextStatus == TrangThaiMon.DA_XONG) {
                        currentDs.add(m);
                    }
                    renderOrders(currentDs);
                }
            }
        }.execute();
    }

    // ── WrapLayout: layout tự xuống dòng ──────────────────────────────────────
    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int maxW = target.getWidth();
                if (maxW == 0) maxW = Integer.MAX_VALUE;
                Insets ins = target.getInsets();
                int x = ins.left + getHgap(), y = ins.top + getVgap(), rowH = 0;
                int maxX = maxW - ins.right - getHgap();

                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width > maxX && x > ins.left + getHgap()) {
                        y += rowH + getVgap(); rowH = 0; x = ins.left + getHgap();
                    }
                    x += d.width + getHgap();
                    rowH = Math.max(rowH, d.height);
                }
                y += rowH + ins.bottom + getVgap();
                return new Dimension(maxW, y);
            }
        }
    }
}
