package gui;

import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO;
import dao.NhanVien_DAO;
import entity.HoaDon;
import entity.NhanVien;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class TrangChu extends JFrame {
    private JPanel sidebar;
    private JPanel menuButtonsPanel;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JButton lastSelectedButton;
    private TaiKhoan taiKhoan;
    private NhanVien nhanVien;

    private DashboardPanel pDashboard;
    private QuanLyDatBan pDatBan;
    private QuanLyHoaDon pHoaDon;
    private QuanLyNhanVien pNhanVien;
    private QuanLyKhachHang pKhachHang;
    private QuanLyMonAn pMonAn;
    private QuanLyThongKe pThongKe;

    private final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private final Color GOLD_COLOR   = Color.decode("#C5A059");
    private final Color MAIN_RED     = Color.decode("#FF4B2B");
    private final Color ACCENT_ORANGE = Color.decode("#FFAD06");
    private final Color SIDEBAR_BG   = Color.WHITE;
    private final Color CONTENT_BG   = Color.decode("#F0F2F5");
    private final Color TEXT_DARK    = Color.decode("#333333");
    private final Color CARD_BG      = Color.WHITE;
    private final Color BORDER_LIGHT = Color.decode("#E0E0E0");

    public TrangChu(TaiKhoan tk) {
        super("Hệ thống quản lý nhà hàng Golden Pearl");
        this.taiKhoan = tk;
        loadFonts();
        initUI();
        if (tk != null) {
            new SwingWorker<NhanVien, Void>() {
                @Override protected NhanVien doInBackground() {
                    return new NhanVien_DAO().getNhanVienByMaTK(tk.getMaTK());
                }
                @Override protected void done() {
                    try { nhanVien = get(); } catch (Exception e) { e.printStackTrace(); }
                }
            }.execute();
        }
    }

    public TrangChu() { this(null); }

    private void loadFonts() {
        lib.FontLoader.registerFont("data/fonts/InstrumentSerif-Regular.ttf");
        lib.FontLoader.registerFont("data/fonts/Inter-Bold.otf");
        lib.FontLoader.registerFont("data/fonts/Inter-Medium.otf");
    }

    private void initUI() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(CONTENT_BG);

        pDashboard = new DashboardPanel();
        contentArea.add(pDashboard, "TrangChủ");
        add(contentArea, BorderLayout.CENTER);

        if (menuButtonsPanel != null && menuButtonsPanel.getComponentCount() > 0
                && menuButtonsPanel.getComponent(0) instanceof JButton) {
            JButton btnHome = (JButton) menuButtonsPanel.getComponent(0);
            lastSelectedButton = btnHome;
            btnHome.setForeground(Color.WHITE);
            ImageIcon li = (ImageIcon) btnHome.getClientProperty("lightIcon");
            if (li != null) btnHome.setIcon(li);
            cardLayout.show(contentArea, "TrangChủ");
            pDashboard.refreshData();
        }
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(SIDEBAR_BG);
        panel.setPreferredSize(new Dimension(230, getHeight()));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_LIGHT));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // User info at TOP
        panel.add(createUserBox());

        // Separator under user box
        JSeparator sep1 = new JSeparator();
        sep1.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep1.setForeground(BORDER_LIGHT);
        panel.add(sep1);
        panel.add(Box.createVerticalStrut(6));

        // Menu buttons
        menuButtonsPanel = new JPanel();
        menuButtonsPanel.setOpaque(false);
        menuButtonsPanel.setLayout(new BoxLayout(menuButtonsPanel, BoxLayout.Y_AXIS));
        menuButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        menuButtonsPanel.add(createSidebarButton("Trang chủ", "home_filled_300dp_FFFFFF.png",
                e -> showCard("TrangChủ", (JButton) e.getSource())));
        menuButtonsPanel.add(Box.createVerticalStrut(2));
        menuButtonsPanel.add(createSidebarButton("Đặt bàn", "menu_open_300dp_FFFFFF.png",
                e -> showCard("ĐặtBàn", (JButton) e.getSource())));
        menuButtonsPanel.add(Box.createVerticalStrut(2));
        menuButtonsPanel.add(createSidebarButton("Quản lý hoá đơn", "receipt_300dp_FFFFFF.png",
                e -> showCard("HóaĐơn", (JButton) e.getSource())));
        menuButtonsPanel.add(Box.createVerticalStrut(2));
        menuButtonsPanel.add(createSidebarButton("Quản lý nhân viên", "badge_300dp_FFFFFF.png",
                e -> showCard("NhânViên", (JButton) e.getSource())));
        menuButtonsPanel.add(Box.createVerticalStrut(2));
        menuButtonsPanel.add(createSidebarButton("Quản lý khách hàng", "people_300dp_FFFFFF.png",
                e -> showCard("KháchHàng", (JButton) e.getSource())));
        menuButtonsPanel.add(Box.createVerticalStrut(2));
        menuButtonsPanel.add(createSidebarButton("Quản lý món ăn", "dinner_dining_300dp_FFFFFF.png",
                e -> showCard("MónĂn", (JButton) e.getSource())));
        menuButtonsPanel.add(Box.createVerticalStrut(2));
        menuButtonsPanel.add(createSidebarButton("Thống kê doanh thu",
                "attach_money_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png",
                e -> showCard("ThốngKê", (JButton) e.getSource())));

        panel.add(menuButtonsPanel);
        panel.add(Box.createVerticalGlue());

        // Separator above logout
        JSeparator sep2 = new JSeparator();
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep2.setForeground(BORDER_LIGHT);
        panel.add(sep2);
        panel.add(Box.createVerticalStrut(4));

        // Logout button — added directly (same layout as menu buttons)
        JButton btnLogout = createSidebarButton("Đăng xuất",
                "person_apron_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png", e -> {
                    int res = JOptionPane.showConfirmDialog(this,
                            "Bạn có muốn đăng xuất không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) { dispose(); new Login(); }
                });
        Color logoutRed = new Color(210, 55, 55);
        btnLogout.setForeground(logoutRed);
        btnLogout.putClientProperty("defaultFg", logoutRed);
        ImageIcon logoutIcon = getColoredIcon(
                "data/icons/person_apron_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png", 17, 17, logoutRed);
        if (logoutIcon != null) {
            btnLogout.setIcon(logoutIcon);
            btnLogout.putClientProperty("darkIcon", logoutIcon);
        }
        panel.add(btnLogout);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private JPanel createUserBox() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        outer.setBorder(new EmptyBorder(10, 10, 6, 10));

        JPanel box = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAIN_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lblIcon = new JLabel("👤", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        lblIcon.setPreferredSize(new Dimension(36, 36));
        lblIcon.setForeground(Color.WHITE);

        JPanel pInfo = new JPanel();
        pInfo.setLayout(new BoxLayout(pInfo, BoxLayout.Y_AXIS));
        pInfo.setOpaque(false);

        String role = taiKhoan != null ? "Vai trò: " + taiKhoan.getVaiTro() : "Vai trò: Admin";
        String name = taiKhoan != null ? taiKhoan.getTenTK() : "Admin";

        JLabel lblRole = new JLabel(role);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblRole.setForeground(new Color(180, 210, 235));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);


        pInfo.add(lblRole);
        pInfo.add(Box.createVerticalStrut(2));
        pInfo.add(lblName);
        pInfo.add(Box.createVerticalStrut(4));

        box.add(lblIcon, BorderLayout.WEST);
        box.add(pInfo, BorderLayout.CENTER);
        outer.add(box, BorderLayout.CENTER);
        return outer;
    }

    private JButton createSidebarButton(String text, String iconName, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (this == lastSelectedButton) {
                    g2.setColor(MAIN_BLUE);
                    g2.fillRoundRect(2, 1, getWidth() - 4, getHeight() - 2, 10, 10);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(11, 61, 89, 18));
                    g2.fillRoundRect(2, 1, getWidth() - 4, getHeight() - 2, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        String iconPath = "data/icons/" + iconName;
        ImageIcon darkIcon  = getColoredIcon(iconPath, 17, 17, new Color(0x44, 0x44, 0x44));
        ImageIcon lightIcon = getColoredIcon(iconPath, 17, 17, Color.WHITE);
        if (darkIcon != null) btn.setIcon(darkIcon);
        btn.putClientProperty("darkIcon",  darkIcon);
        btn.putClientProperty("lightIcon", lightIcon);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(230, 42));
        btn.setMinimumSize(new Dimension(160, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_DARK);
        btn.putClientProperty("defaultFg", TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);
        btn.setMargin(new Insets(0, 10, 0, 4));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private void showCard(String cardName, JButton source) {
        switch (cardName) {
            case "TrangChủ":
                if (pDashboard == null) { pDashboard = new DashboardPanel(); contentArea.add(pDashboard, "TrangChủ"); }
                pDashboard.refreshData(); break;  // dashboard luôn refresh (live stats)
            case "NhânViên":
                if (pNhanVien == null) { pNhanVien = new QuanLyNhanVien(); contentArea.add(pNhanVien, "NhânViên"); pNhanVien.refreshData(); }
                break;
            case "HóaĐơn":
                if (pHoaDon == null) { pHoaDon = new QuanLyHoaDon(); contentArea.add(pHoaDon, "HóaĐơn"); pHoaDon.refreshData(); }
                break;
            case "KháchHàng":
                if (pKhachHang == null) { pKhachHang = new QuanLyKhachHang(); contentArea.add(pKhachHang, "KháchHàng"); pKhachHang.refreshData(); }
                break;
            case "MónĂn":
                if (pMonAn == null) { pMonAn = new QuanLyMonAn(); contentArea.add(pMonAn, "MónĂn"); pMonAn.refreshData(); }
                break;
            case "ThốngKê":
                if (pThongKe == null) { pThongKe = new QuanLyThongKe(); contentArea.add(pThongKe, "ThốngKê"); }
                pThongKe.refreshData(); break;  // thống kê luôn refresh
            case "ĐặtBàn":
                if (pDatBan == null) { pDatBan = new QuanLyDatBan(nhanVien); contentArea.add(pDatBan, "ĐặtBàn"); }
                pDatBan.refreshData(); break;  // đặt bàn luôn refresh (trạng thái bàn thay đổi liên tục)
        }
        cardLayout.show(contentArea, cardName);
        if (lastSelectedButton != null) {
            Color dfg = (Color) lastSelectedButton.getClientProperty("defaultFg");
            lastSelectedButton.setForeground(dfg != null ? dfg : TEXT_DARK);
            ImageIcon di = (ImageIcon) lastSelectedButton.getClientProperty("darkIcon");
            if (di != null) lastSelectedButton.setIcon(di);
            lastSelectedButton.repaint();
        }
        lastSelectedButton = source;
        lastSelectedButton.setForeground(Color.WHITE);
        ImageIcon li = (ImageIcon) lastSelectedButton.getClientProperty("lightIcon");
        if (li != null) lastSelectedButton.setIcon(li);
        lastSelectedButton.repaint();
    }

    private ImageIcon getScaledIcon(String path, int w, int h) {
        return getColoredIcon(path, w, h, null);
    }

    private ImageIcon getColoredIcon(String path, int w, int h, Color recolor) {
        try {
            BufferedImage src = ImageIO.read(new File(path));
            if (src == null) return null;
            BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, w, h, null);
            g.dispose();
            if (recolor != null) {
                int rgb = recolor.getRGB() & 0x00FFFFFF;
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int px = dst.getRGB(x, y);
                        int alpha = (px >> 24) & 0xFF;
                        if (alpha > 10) dst.setRGB(x, y, (alpha << 24) | rgb);
                    }
                }
            }
            return new ImageIcon(dst);
        } catch (Exception e) { return null; }
    }

    // ======================== DASHBOARD ========================
    private class DashboardPanel extends JPanel {
        private final HoaDon_DAO hd_dao = new HoaDon_DAO();
        private final ChiTietHoaDon_DAO ct_dao = new ChiTietHoaDon_DAO();
        private JLabel lblRevenue, lblProfit, lblInvoices, lblCustomers;
        private JPanel pChartContainer, pBestSellers;
        private DefaultTableModel tableModel;
        private int currentDays = 7;
        private JButton btnToday, btn7Days, btn30Days;
        private final DecimalFormat df = new DecimalFormat("#,### VNĐ");
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

        public DashboardPanel() {
            setLayout(new BorderLayout(0, 16));
            setBackground(CONTENT_BG);
            setBorder(new EmptyBorder(24, 32, 24, 32));
            initUI();
        }

        private void initUI() {
            // HEADER
            JPanel pHeader = new JPanel(new BorderLayout());
            pHeader.setOpaque(false);
            JLabel lblTitle = new JLabel("TỔNG QUAN DOANH THU");
            lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 26));
            lblTitle.setForeground(TEXT_DARK);
            pHeader.add(lblTitle, BorderLayout.WEST);

            JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            pFilter.setOpaque(false);
            btnToday  = makeFilterBtn("Hôm nay");
            btn7Days  = makeFilterBtn("7 ngày qua");
            btn30Days = makeFilterBtn("30 ngày qua");
            btnToday.addActionListener(e -> { currentDays = 1; activateFilter(btnToday); loadData(1); });
            btn7Days.addActionListener(e -> { currentDays = 7; activateFilter(btn7Days); loadData(7); });
            btn30Days.addActionListener(e -> { currentDays = 30; activateFilter(btn30Days); loadData(30); });
            pFilter.add(btnToday); pFilter.add(btn7Days); pFilter.add(btn30Days);
            pHeader.add(pFilter, BorderLayout.EAST);
            add(pHeader, BorderLayout.NORTH);

            JPanel pContent = new JPanel(new BorderLayout(0, 16));
            pContent.setOpaque(false);

            // Stat cards
            JPanel pStats = new JPanel(new GridLayout(1, 4, 18, 0));
            pStats.setOpaque(false);
            pStats.setPreferredSize(new Dimension(0, 115));
            lblRevenue   = new JLabel("0 VNĐ");
            lblProfit    = new JLabel("0 VNĐ");
            lblInvoices  = new JLabel("0");
            lblCustomers = new JLabel("0");
            pStats.add(makeStatCard("TỔNG DOANH THU", lblRevenue,   new Color(46, 204, 113)));
            pStats.add(makeStatCard("LỢI NHUẬN",       lblProfit,    new Color(230, 126, 34)));
            pStats.add(makeStatCard("TỔNG HÓA ĐƠN",   lblInvoices,  new Color(52, 152, 219)));
            pStats.add(makeStatCard("TỔNG KHÁCH HÀNG", lblCustomers, MAIN_RED));
            pContent.add(pStats, BorderLayout.NORTH);

            // Chart — full width
            JPanel pChartCard = makeCard();
            pChartCard.setLayout(new BorderLayout(0, 8));
            pChartCard.setBorder(new EmptyBorder(18, 18, 18, 18));
            JLabel lblChart = new JLabel("Biểu đồ tăng trưởng doanh thu");
            lblChart.setFont(new Font("Inter Bold", Font.BOLD, 15));
            lblChart.setForeground(TEXT_DARK);
            pChartCard.add(lblChart, BorderLayout.NORTH);
            pChartContainer = new JPanel(new BorderLayout());
            pChartContainer.setOpaque(false);
            pChartCard.add(pChartContainer, BorderLayout.CENTER);
            pContent.add(pChartCard, BorderLayout.CENTER);

            // Bottom row: invoice table (left 60%) + best sellers (right 40%)
            JPanel pInvoiceCard = makeCard();
            pInvoiceCard.setLayout(new BorderLayout(0, 8));
            pInvoiceCard.setBorder(new EmptyBorder(14, 18, 14, 18));
            JLabel lblTblTitle = new JLabel("Chi tiết hóa đơn");
            lblTblTitle.setFont(new Font("Inter Bold", Font.BOLD, 15));
            lblTblTitle.setForeground(TEXT_DARK);
            pInvoiceCard.add(lblTblTitle, BorderLayout.NORTH);

            String[] cols = {"Hóa đơn", "Ngày", "Tổng tiền", "Lợi nhuận"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable tbl = new JTable(tableModel);
            tbl.setFont(new Font("Inter", Font.PLAIN, 13));
            tbl.setRowHeight(28);
            tbl.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 13));
            tbl.setBackground(Color.WHITE);
            tbl.setGridColor(new Color(230, 230, 230));
            tbl.setShowVerticalLines(false);
            pInvoiceCard.add(new JScrollPane(tbl), BorderLayout.CENTER);

            JPanel pBestCard = makeCard();
            pBestCard.setLayout(new BorderLayout(0, 8));
            pBestCard.setBorder(new EmptyBorder(14, 18, 14, 18));
            JLabel lblBest = new JLabel("Món ăn bán chạy nhất");
            lblBest.setFont(new Font("Inter Bold", Font.BOLD, 15));
            lblBest.setForeground(TEXT_DARK);
            pBestCard.add(lblBest, BorderLayout.NORTH);
            pBestSellers = new JPanel();
            pBestSellers.setLayout(new BoxLayout(pBestSellers, BoxLayout.Y_AXIS));
            pBestSellers.setOpaque(false);
            JScrollPane bsScroll = new JScrollPane(pBestSellers);
            bsScroll.setOpaque(false); bsScroll.getViewport().setOpaque(false); bsScroll.setBorder(null);
            pBestCard.add(bsScroll, BorderLayout.CENTER);

            JPanel pBottom = new JPanel(null) {
                @Override public void doLayout() {
                    int w = getWidth(), h = getHeight(), gap = 14;
                    int lw = (int)((w - gap) * 0.60);
                    pInvoiceCard.setBounds(0, 0, lw, h);
                    pBestCard.setBounds(lw + gap, 0, w - gap - lw, h);
                }
            };
            pBottom.setOpaque(false);
            pBottom.setPreferredSize(new Dimension(0, 260));
            pBottom.add(pInvoiceCard);
            pBottom.add(pBestCard);

            pContent.add(pBottom, BorderLayout.SOUTH);

            add(pContent, BorderLayout.CENTER);
        }

        private JButton makeFilterBtn(String text) {
            JButton btn = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    if (!getBackground().equals(MAIN_BLUE)) {
                        g2.setColor(BORDER_LIGHT);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Inter Medium", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_DARK);
            btn.setBorderPainted(false);
            btn.setBorder(new EmptyBorder(5, 16, 5, 16));
            btn.setPreferredSize(new Dimension(115, 32));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private void activateFilter(JButton active) {
            for (JButton b : new JButton[]{btnToday, btn7Days, btn30Days}) {
                b.setBackground(Color.WHITE); b.setForeground(TEXT_DARK); b.repaint();
            }
            active.setBackground(MAIN_BLUE); active.setForeground(Color.WHITE); active.repaint();
        }

        private JPanel makeCard() {
            return new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(CARD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.setColor(new Color(218, 222, 228));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    g2.dispose();
                }
            };
        }

        private JPanel makeStatCard(String title, JLabel valueLbl, Color accent) {
            JPanel card = makeCard();
            card.setOpaque(false);
            card.setLayout(new BorderLayout(0, 4));
            card.setBorder(new EmptyBorder(14, 18, 14, 18));

            JPanel pTop = new JPanel(new BorderLayout());
            pTop.setOpaque(false);
            JLabel lblT = new JLabel(title);
            lblT.setFont(new Font("Inter", Font.BOLD, 11));
            lblT.setForeground(new Color(110, 110, 110));
            pTop.add(lblT, BorderLayout.WEST);
            JLabel dot = new JLabel("●");
            dot.setForeground(accent); dot.setFont(new Font("Dialog", Font.BOLD, 16));
            pTop.add(dot, BorderLayout.EAST);
            card.add(pTop, BorderLayout.NORTH);

            valueLbl.setFont(new Font("Inter Bold", Font.BOLD, 19));
            valueLbl.setForeground(TEXT_DARK);
            card.add(valueLbl, BorderLayout.CENTER);

            JPanel line = new JPanel();
            line.setBackground(accent);
            line.setPreferredSize(new Dimension(0, 3));
            card.add(line, BorderLayout.SOUTH);
            return card;
        }

        public void refreshData() { activateFilter(btn7Days); loadData(currentDays); }

        @SuppressWarnings("unchecked")
        public void loadData(int days) {
            new SwingWorker<Map<String, Object>, Void>() {
                @Override
                protected Map<String, Object> doInBackground() {
                    Calendar cal = Calendar.getInstance();
                    Date end = cal.getTime();
                    cal.add(Calendar.DAY_OF_YEAR, -days + 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
                    Date start = cal.getTime();

                    List<HoaDon> dsHD = hd_dao.getHoaDonByDateRange(start, end);
                    double totalRev = 0; int totalInv = 0;
                    Set<String> customers = new HashSet<>();
                    Map<String, Double> chartData = new LinkedHashMap<>();

                    Calendar tmp = Calendar.getInstance(); tmp.setTime(start);
                    for (int i = 0; i < days; i++) {
                        chartData.put(sdf.format(tmp.getTime()), 0.0);
                        tmp.add(Calendar.DAY_OF_YEAR, 1);
                    }

                    java.sql.Timestamp ts1 = new java.sql.Timestamp(start.getTime());
                    java.sql.Timestamp ts2 = new java.sql.Timestamp(end.getTime());
                    double profit = ct_dao.getProfitByDateRange(ts1, ts2);
                    Map<String, Double> profitMap = ct_dao.getProfitGroupedByMaHD(ts1, ts2);
                    Map<String, Integer> top = ct_dao.getTop5SellingDishes();

                    List<Object[]> rows = new ArrayList<>();
                    SimpleDateFormat dfmt = new SimpleDateFormat("dd/MM/yyyy");
                    for (HoaDon hd : dsHD) {
                        if (hd.isTrangThai()) {
                            totalRev += hd.getTongTien();
                            totalInv++;
                            if (hd.getKhachHang() != null) customers.add(hd.getKhachHang().getMaKH());
                            String ds = sdf.format(hd.getNgayLap());
                            if (chartData.containsKey(ds)) chartData.put(ds, chartData.get(ds) + hd.getTongTien());
                            if (rows.size() < 10) {
                                double rowProfit = profitMap.getOrDefault(hd.getMaHD(), 0.0);
                                rows.add(new Object[]{hd.getMaHD(), dfmt.format(hd.getNgayLap()),
                                        new DecimalFormat("#,###").format(hd.getTongTien()) + " VNĐ",
                                        rowProfit > 0 ? new DecimalFormat("#,###").format(rowProfit) + " VNĐ" : "—"});
                            }
                        }
                    }

                    Map<String, Double> profitByDate = new LinkedHashMap<>();
                    Calendar tmp2 = Calendar.getInstance(); tmp2.setTime(start);
                    for (int j = 0; j < days; j++) {
                        profitByDate.put(sdf.format(tmp2.getTime()), 0.0);
                        tmp2.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    for (HoaDon hd2 : dsHD) {
                        if (hd2.isTrangThai()) {
                            String ds2 = sdf.format(hd2.getNgayLap());
                            double p2 = profitMap.getOrDefault(hd2.getMaHD(), 0.0);
                            if (profitByDate.containsKey(ds2))
                                profitByDate.put(ds2, profitByDate.get(ds2) + p2);
                        }
                    }

                    Map<String, Object> res = new HashMap<>();
                    res.put("rev", totalRev); res.put("inv", totalInv);
                    res.put("cust", customers.size()); res.put("profit", profit);
                    res.put("chart", chartData); res.put("top", top); res.put("rows", rows);
                    res.put("profitByDate", profitByDate);
                    return res;
                }

                @Override
                protected void done() {
                    try {
                        Map<String, Object> res = get();
                        lblRevenue.setText(df.format(res.get("rev")));
                        lblProfit.setText(df.format(res.get("profit")));
                        lblInvoices.setText(String.valueOf(res.get("inv")));
                        lblCustomers.setText(String.valueOf(res.get("cust")));

                        pChartContainer.removeAll();
                        pChartContainer.add(new SimpleBarChart(
                            (Map<String, Double>) res.get("chart"),
                            (Map<String, Double>) res.get("profitByDate")), BorderLayout.CENTER);
                        pChartContainer.revalidate(); pChartContainer.repaint();

                        pBestSellers.removeAll();
                        int rank = 1;
                        for (Map.Entry<String, Integer> e : ((Map<String, Integer>) res.get("top")).entrySet()) {
                            pBestSellers.add(makeBestRow(rank++, e.getKey(), e.getValue()));
                            pBestSellers.add(Box.createVerticalStrut(10));
                        }
                        pBestSellers.revalidate(); pBestSellers.repaint();

                        tableModel.setRowCount(0);
                        for (Object[] row : (List<Object[]>) res.get("rows")) tableModel.addRow(row);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        }

        private JPanel makeBestRow(int rank, String name, int qty) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            JLabel lblRank = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
            lblRank.setFont(new Font("Inter Bold", Font.BOLD, 13));
            lblRank.setForeground(rank <= 3 ? MAIN_RED : TEXT_DARK);
            lblRank.setPreferredSize(new Dimension(26, 26));
            lblRank.setBorder(BorderFactory.createLineBorder(rank <= 3 ? MAIN_RED : BORDER_LIGHT));
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Inter Bold", Font.BOLD, 13));
            lblName.setForeground(TEXT_DARK);
            JLabel lblQty = new JLabel(qty + " suất");
            lblQty.setFont(new Font("Inter", Font.PLAIN, 12));
            lblQty.setForeground(new Color(130, 130, 130));
            row.add(lblRank, BorderLayout.WEST);
            row.add(lblName, BorderLayout.CENTER);
            row.add(lblQty, BorderLayout.EAST);
            return row;
        }
    }

    private class SimpleBarChart extends JPanel {
        private final Map<String, Double> data;
        private final Map<String, Double> profitData;
        private int hoverIndex = -1;
        private final DecimalFormat ttFmt = new DecimalFormat("#,###");

        SimpleBarChart(Map<String, Double> data, Map<String, Double> profitData) {
            this.data = data;
            this.profitData = profitData;
            setOpaque(false);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int idx = hitTest(e.getX(), e.getY());
                    if (idx != hoverIndex) { hoverIndex = idx; repaint(); }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) {
                    if (hoverIndex != -1) { hoverIndex = -1; repaint(); }
                }
            });
        }

        private int hitTest(int mx, int my) {
            if (data == null || data.isEmpty()) return -1;
            int w = getWidth(), h = getHeight(), pad = 35, lpad = 30;
            int count = data.size();
            int gap = count > 10 ? 4 : 14;
            int barW = Math.max(2, (w - 2 * pad - lpad) / count - gap);
            double max = data.values().stream().max(Double::compare).orElse(1.0);
            if (max == 0) max = 1.0;
            int x = pad + lpad + 8, i = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int barH = (int) ((entry.getValue() / max) * (h - 2 * pad - 16));
                if (mx >= x && mx <= x + barW && my >= h - pad - barH && my <= h - pad) return i;
                x += barW + gap; i++;
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = 35, lpad = 30;
            g2.setColor(new Color(210, 210, 210));
            g2.drawLine(pad + lpad, h - pad, w - pad, h - pad);
            g2.drawLine(pad + lpad, pad, pad + lpad, h - pad);
            if (data == null || data.isEmpty()) { g2.dispose(); return; }
            double max = data.values().stream().max(Double::compare).orElse(1.0);
            if (max == 0) max = 1.0;
            int count = data.size();
            int gap = count > 10 ? 4 : 14;
            int barW = Math.max(2, (w - 2 * pad - lpad) / count - gap);
            int x = pad + lpad + 8, i = 0;
            int tipX = -1, tipBarH = -1;
            String tipDate = null; double tipRev = 0, tipProfit = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int barH = (int) ((entry.getValue() / max) * (h - 2 * pad - 16));
                if (i == hoverIndex) {
                    g2.setPaint(new GradientPaint(x, h - pad - barH, MAIN_BLUE, x, h - pad, new Color(0x1A, 0x6A, 0x9A)));
                    tipX = x; tipBarH = barH; tipDate = entry.getKey();
                    tipRev = entry.getValue();
                    tipProfit = profitData != null ? profitData.getOrDefault(entry.getKey(), 0.0) : 0.0;
                } else {
                    g2.setPaint(new GradientPaint(x, h - pad - barH, MAIN_RED, x, h - pad, ACCENT_ORANGE));
                }
                g2.fillRoundRect(x, h - pad - barH, barW, barH, 5, 5);
                if (count <= 10 || i % (count / 7 + 1) == 0) {
                    g2.setColor(TEXT_DARK);
                    g2.setFont(new Font("Inter", Font.PLAIN, 9));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(entry.getKey(), x + (barW - fm.stringWidth(entry.getKey())) / 2, h - pad + 13);
                }
                x += barW + gap; i++;
            }
            if (hoverIndex >= 0 && tipDate != null) {
                String l1 = tipDate;
                String l2 = "Doanh thu: " + ttFmt.format(tipRev) + " VNĐ";
                String l3 = "Lợi nhuận: " + ttFmt.format(tipProfit) + " VNĐ";
                g2.setFont(new Font("Inter Bold", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tw = Math.max(fm.stringWidth(l2), Math.max(fm.stringWidth(l3), fm.stringWidth(l1))) + 20;
                int th = 60;
                int tx = tipX + barW / 2 - tw / 2;
                int ty = h - pad - tipBarH - th - 6;
                if (tx < pad + lpad) tx = pad + lpad;
                if (tx + tw > w - pad) tx = w - pad - tw;
                if (ty < 2) ty = h - pad - tipBarH + 4;
                g2.setColor(new Color(30, 30, 30, 215));
                g2.fillRoundRect(tx, ty, tw, th, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Inter Bold", Font.BOLD, 11));
                g2.drawString(l1, tx + 10, ty + 16);
                g2.setFont(new Font("Inter", Font.PLAIN, 10));
                g2.drawString(l2, tx + 10, ty + 33);
                g2.drawString(l3, tx + 10, ty + 49);
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new TrangChu().setVisible(true));
    }
}
