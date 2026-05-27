package gui;

import dao.Ban_DAO;
import dao.DonDatBan_DAO;
import dao.KhuVuc_DAO;
import entity.Ban;
import entity.DonDatBan;
import entity.KhuVuc;
import entity.TrangThaiBan;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class QuanLyBan extends JPanel {

    private static final Color MAIN_BLUE  = Color.decode("#0B3D59");
    private static final Color GREEN      = Color.decode("#27AE60");
    private static final Color RED_DANG   = Color.decode("#E74C3C");
    private static final Color ORANGE     = Color.decode("#E67E22");
    private static final Color PURPLE     = Color.decode("#8E44AD");
    private static final Color GRAY_MT    = Color.decode("#7F8C8D");
    private static final Color BG_LIGHT   = Color.decode("#F0F2F5");
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color BORDER_CLR = Color.decode("#DDE1E7");
    private static final Color GOLD_VIP   = Color.decode("#C5A059");

    private final Ban_DAO       banDAO = new Ban_DAO();
    private final DonDatBan_DAO ddbDAO = new DonDatBan_DAO();
    private final KhuVuc_DAO    kvDAO  = new KhuVuc_DAO();
    private JPanel contentPanel;
    private List<KhuVuc> cachedKV = new ArrayList<>();

    public QuanLyBan() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);
        add(buildHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_LIGHT);
        contentPanel.setBorder(new EmptyBorder(4, 28, 24, 28));

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_LIGHT);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadCards();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(true);
        p.setBackground(MAIN_BLUE);
        p.setBorder(new EmptyBorder(10, 28, 10, 28));

        JLabel title = new JLabel("QUẢN LÝ TÌNH TRẠNG BÀN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(GOLD_VIP);

        JLabel sub = new JLabel("Theo dõi và quản lý hoạt động / bảo trì các bàn trong nhà hàng");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(180, 200, 220));

        JPanel pLeft = new JPanel();
        pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
        pLeft.setOpaque(false);
        pLeft.add(title);
        pLeft.add(Box.createVerticalStrut(4));
        pLeft.add(sub);

        JButton btnAdd = new JButton("+ Thêm bàn") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? GREEN.darker() : GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 20);
                g2.setColor(GREEN.darker().darker());
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 40, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setPreferredSize(new Dimension(160, 30));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> showAddBanDialog());

        JButton btnRefresh = new JButton("⟳  Làm mới") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? GOLD_VIP.darker() : GOLD_VIP);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(GOLD_VIP.darker().darker());
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btnRefresh.setForeground(MAIN_BLUE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setPreferredSize(new Dimension(160, 30));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadCards());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        row.setOpaque(false);
        row.add(makeLegend("Trống", GREEN));
        row.add(makeLegend("Đã đặt", ORANGE));
        row.add(makeLegend("Đang dùng", RED_DANG));
        row.add(makeLegend("Quá giờ", PURPLE));
        row.add(makeLegend("Bảo trì", GRAY_MT));
        row.add(Box.createHorizontalStrut(8));
        row.add(btnAdd);
        row.add(btnRefresh);

        JPanel pRight = new JPanel(new GridBagLayout());
        pRight.setOpaque(false);
        pRight.add(row);

        p.add(pLeft, BorderLayout.WEST);
        p.add(pRight, BorderLayout.EAST);
        return p;
    }

    private JLabel makeLegend(String text, Color c) {
        JLabel l = new JLabel("● " + text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(c);
        return l;
    }

    public void refreshData() {
        loadCards();
    }

    private void loadCards() {
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                List<Ban>    dsBan       = banDAO.getAllBan();
                List<KhuVuc> dsKV        = kvDAO.getAllKhuVuc();
                Set<String>  activeMaBan  = new HashSet<>();
                Set<String>  overdueMaBan = new HashSet<>();
                Date now = new Date();
                for (DonDatBan d : ddbDAO.getAllDonDatBanWithBan()) {
                    if (d.isTrangThai()) continue;
                    Date tgRoi = d.getThoiGianDuKienRoi();
                    if (tgRoi != null && tgRoi.before(now)) {
                        for (Ban b : d.getDsBan()) overdueMaBan.add(b.getMaBan());
                        continue;
                    }
                    for (Ban b : d.getDsBan()) activeMaBan.add(b.getMaBan());
                }
                return new Object[]{dsBan, activeMaBan, overdueMaBan, dsKV};
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void done() {
                try {
                    Object[]     r          = get();
                    List<Ban>    dsBan      = (List<Ban>)    r[0];
                    Set<String>  activeMa   = (Set<String>)  r[1];
                    Set<String>  overdueMa  = (Set<String>)  r[2];
                    List<KhuVuc> dsKV       = (List<KhuVuc>) r[3];

                    cachedKV = dsKV;

                    // Nhóm theo loại bàn: Thường trước, VIP sau
                    Map<String, List<Ban>> banByLoai = new LinkedHashMap<>();
                    banByLoai.put("Thường", new ArrayList<>());
                    banByLoai.put("VIP",    new ArrayList<>());
                    for (Ban ban : dsBan) {
                        String loai = ban.getLoaiBan() != null ? ban.getLoaiBan().trim() : "";
                        if (loai.equalsIgnoreCase("VIP")) banByLoai.get("VIP").add(ban);
                        else                              banByLoai.get("Thường").add(ban);
                    }

                    contentPanel.removeAll();
                    for (Map.Entry<String, List<Ban>> entry : banByLoai.entrySet()) {
                        if (entry.getValue().isEmpty()) continue;
                        contentPanel.add(buildKvSection(entry.getKey(), entry.getValue(), activeMa, overdueMa));
                        contentPanel.add(Box.createVerticalStrut(10));
                    }
                    contentPanel.add(Box.createVerticalGlue());
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private JPanel buildKvSection(String tenKV, List<Ban> bans, Set<String> activeMa, Set<String> overdueMa) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        boolean isVipSection = bans.stream().anyMatch(b ->
                "VIP".equalsIgnoreCase(b.getLoaiBan() != null ? b.getLoaiBan().trim() : ""));
        final Color hdrColor = isVipSection ? GOLD_VIP : MAIN_BLUE;
        String label      = tenKV != null && !tenKV.isEmpty() ? tenKV.toUpperCase() : "KHU VỰC";
        String countLabel = "  (" + bans.size() + " bàn)";
        String hdrPrefix  = isVipSection ? "★   " : "▼   ";

        JButton hdr = new JButton(hdrPrefix + label + countLabel) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hdrColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hdr.setForeground(Color.WHITE);
        hdr.setFocusPainted(false);
        hdr.setBorderPainted(false);
        hdr.setContentAreaFilled(false);
        hdr.setBorder(new EmptyBorder(7, 12, 7, 12));
        hdr.setHorizontalAlignment(SwingConstants.LEFT);
        hdr.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel grid = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 0, 4, 0));
        for (Ban ban : bans) {
            boolean active  = activeMa.contains(ban.getMaBan());
            boolean overdue = overdueMa.contains(ban.getMaBan());
            grid.add(makeCard(ban, active, overdue));
        }

        hdr.addActionListener(e -> {
            boolean vis = !grid.isVisible();
            grid.setVisible(vis);
            hdr.setText((vis ? hdrPrefix : "☰   ") + label + countLabel);
            wrap.revalidate();
        });

        wrap.add(hdr,  BorderLayout.NORTH);
        wrap.add(grid, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel makeCard(Ban ban, boolean hasActiveDon, boolean isOverdue) {
        TrangThaiBan tt          = ban.getTinhTrangBan();
        boolean      isOverdueUI = isOverdue && !hasActiveDon && tt == TrangThaiBan.DangDuocSuDung;
        boolean      isBaoTri    = (tt == TrangThaiBan.BaoTri);
        boolean      canToggle   = isBaoTri || !hasActiveDon;
        Color        sc          = isOverdueUI ? PURPLE : statusColor(tt);
        String       badgeText   = isOverdueUI ? "Quá giờ" : statusLabel(tt);
        boolean      isVip       = "VIP".equalsIgnoreCase(ban.getLoaiBan() != null ? ban.getLoaiBan().trim() : "");
        boolean[]    hovered     = {false};

        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isBaoTri ? new Color(242, 242, 244) : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(isBaoTri ? GRAY_MT : (isVip ? GOLD_VIP : (hovered[0] ? sc : BORDER_CLR)));
                g2.setStroke(new BasicStroke(isBaoTri ? 1.5f : (isVip ? 2f : (hovered[0] ? 1.5f : 1f))));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(165, 220));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Icon bàn nhìn từ trên xuống ───────────────────────────────────
        TableIconPanel iconPanel = new TableIconPanel(MAIN_BLUE, sc, ban.getSucChua(), isVip, isBaoTri, GOLD_VIP);
        iconPanel.setPreferredSize(new Dimension(165, 98));

        // ── Thông tin ─────────────────────────────────────────────────────
        JPanel pInfo = new JPanel();
        pInfo.setLayout(new BoxLayout(pInfo, BoxLayout.Y_AXIS));
        pInfo.setOpaque(false);
        pInfo.setBorder(new EmptyBorder(6, 13, 4, 13));

        JLabel lblNum = new JLabel("Bàn " + ban.getSoBan());
        lblNum.setFont(new Font("Inter Bold", Font.BOLD, 19));
        lblNum.setForeground(isBaoTri ? new Color(110, 110, 115) : (isVip ? GOLD_VIP : MAIN_BLUE));
        lblNum.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel((isVip ? "VIP" : "Thường") + "  •  " + ban.getSucChua() + " người");
        lblSub.setFont(new Font("Segoe UI", isVip ? Font.BOLD : Font.PLAIN, 12));
        lblSub.setForeground(isVip ? GOLD_VIP.darker() : new Color(130, 140, 150));
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBadge = new JLabel(badgeText, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(sc.getRed(), sc.getGreen(), sc.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setFont(new Font("Inter Bold", Font.BOLD, 12));
        lblBadge.setForeground(sc);
        lblBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        lblBadge.setOpaque(false);
        lblBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        pInfo.add(lblNum);
        pInfo.add(Box.createVerticalStrut(3));
        pInfo.add(lblSub);
        pInfo.add(Box.createVerticalStrut(6));
        pInfo.add(lblBadge);

        // ── Nút bảo trì ───────────────────────────────────────────────────
        String btnText  = isBaoTri ? "Kết thúc bảo trì" : "Đặt bảo trì";
        Color  btnColor = isBaoTri ? GREEN : GRAY_MT;

        JButton btnToggle = new JButton(btnText) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = isEnabled()
                        ? (getModel().isRollover() ? btnColor.darker() : btnColor)
                        : new Color(195, 198, 202);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setEnabled(canToggle);
        btnToggle.setFocusPainted(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setPreferredSize(new Dimension(0, 30));
        btnToggle.setCursor(canToggle
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        if (!canToggle) btnToggle.setToolTipText("Bàn đang có lịch đặt — không thể đặt bảo trì");

        btnToggle.addActionListener(e -> {
            TrangThaiBan next  = isBaoTri ? TrangThaiBan.Trong : TrangThaiBan.BaoTri;
            String       title = isBaoTri ? "Kết thúc bảo trì" : "Đặt bảo trì";
            String       msg   = isBaoTri
                    ? "Xác nhận kết thúc bảo trì\ncho Bàn " + ban.getSoBan() + "?"
                    : "Xác nhận đặt Bàn " + ban.getSoBan() + "\nvào trạng thái bảo trì?";
            Color accent = isBaoTri ? GREEN : GRAY_MT;
            if (showConfirmDialog(title, msg, accent)) {
                boolean ok = banDAO.updateTinhTrangBan(ban.getMaBan(), next);
                if (ok) loadCards();
                else showErrorDialog("Cập nhật thất bại!\nVui lòng thử lại.");
            }
        });

        JPanel pBtn = new JPanel(new BorderLayout());
        pBtn.setOpaque(false);
        pBtn.setBorder(new EmptyBorder(0, 10, 10, 10));
        pBtn.add(btnToggle, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered[0] = true;  card.repaint(); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { hovered[0] = false; card.repaint(); }
        });

        card.add(iconPanel, BorderLayout.NORTH);
        card.add(pInfo,     BorderLayout.CENTER);
        card.add(pBtn,      BorderLayout.SOUTH);
        return card;
    }

    // ── Icon bàn vẽ bằng Java2D (top-down view) ───────────────────────────────
    static class TableIconPanel extends JPanel {
        private final Color tableColor;   // màu mặt bàn
        private final Color tintColor;    // màu nền mờ (theo trạng thái)
        private final int   seats;
        private final boolean isVip;
        private final boolean isMaintenance;
        private final Color goldColor;

        TableIconPanel(Color tableColor, Color tintColor, int seats, boolean isVip, boolean isMaintenance, Color goldColor) {
            this.tableColor    = tableColor;
            this.tintColor     = tintColor;
            this.seats         = seats;
            this.isVip         = isVip;
            this.isMaintenance = isMaintenance;
            this.goldColor     = goldColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();

            // Nền nhạt màu trạng thái
            g2.setColor(new Color(tintColor.getRed(), tintColor.getGreen(), tintColor.getBlue(), 20));
            g2.fillRoundRect(0, 0, W, H, 14, 14);

            // Mặt bàn top-down
            int tableW = (int)(W * 0.62);
            int tableH = (int)(H * 0.55);
            int tableX = (W - tableW) / 2;
            int tableY = (H - tableH) / 2;

            Color cLight = new Color(
                Math.min(255, tableColor.getRed()   + 60),
                Math.min(255, tableColor.getGreen() + 60),
                Math.min(255, tableColor.getBlue()  + 60));

            // Bóng đổ
            g2.setColor(new Color(0, 0, 0, 28));
            g2.fillRoundRect(tableX + 4, tableY + 5, tableW, tableH, 14, 14);

            // Mặt bàn gradient
            g2.setPaint(new GradientPaint(tableX, tableY, cLight, tableX, tableY + tableH, tableColor));
            g2.fillRoundRect(tableX, tableY, tableW, tableH, 14, 14);

            // Viền
            g2.setPaint(tableColor.darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(tableX, tableY, tableW, tableH, 14, 14);

            // Nhãn
            String label = isVip ? "★ VIP" : (isMaintenance ? "B.TRÌ" : "");
            if (!label.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(255, 255, 255, 220));
                g2.drawString(label,
                    tableX + (tableW - fm.stringWidth(label)) / 2,
                    tableY + (tableH + fm.getAscent() - fm.getDescent()) / 2);
            }

            g2.dispose();
        }
    }

    // ── Thêm bàn dialog ────────────────────────────────────────────────────
    private void showAddBanDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Thêm bàn mới", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(460, 320);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 226), 1));

        // Header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 16, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel lblTitle = new JLabel("Thêm bàn mới");
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 28, 8, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        Font fLabel = new Font("Segoe UI", Font.PLAIN, 16);
        Font fField = new Font("Segoe UI", Font.PLAIN, 16);

        JSpinner spinSoBan = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        ((JSpinner.DefaultEditor) spinSoBan.getEditor()).getTextField().setFont(fField);

        JSpinner spinSucChua = new JSpinner(new SpinnerNumberModel(4, 1, 50, 1));
        ((JSpinner.DefaultEditor) spinSucChua.getEditor()).getTextField().setFont(fField);

        JComboBox<String> cmbLoai = new JComboBox<>(new String[]{"Thường", "VIP"});
        cmbLoai.setFont(fField);

        List<KhuVuc> kvList = cachedKV.isEmpty() ? kvDAO.getAllKhuVuc() : cachedKV;

        String[][] rows = {
            {"Số bàn:", null},
            {"Sức chứa (người):", null},
            {"Loại bàn:", null}
        };
        JComponent[] fields = {spinSoBan, spinSucChua, cmbLoai};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(fLabel);
            lbl.setForeground(new Color(70, 80, 95));
            form.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add(fields[i], gbc);
        }

        // Buttons
        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBtns.setOpaque(false);
        pBtns.setBorder(new EmptyBorder(4, 16, 18, 16));

        JButton btnCancel  = makeDialogBtn("Huỷ",        new Color(230, 232, 236), new Color(80, 90, 100));
        JButton btnConfirm = makeDialogBtn("Thêm bàn",   GREEN,                    Color.WHITE);

        btnCancel.addActionListener(e -> dlg.dispose());

        btnConfirm.addActionListener(e -> {
            int    soBan   = (int) spinSoBan.getValue();
            int    sucChua = (int) spinSucChua.getValue();
            String loai    = (String) cmbLoai.getSelectedItem();

            // Tự động xác định khu vực từ loại bàn
            KhuVuc kv = null;
            boolean wantVip = "VIP".equals(loai);
            for (KhuVuc k : kvList) {
                String tenKV = k.getTenKV() != null ? k.getTenKV().toLowerCase() : "";
                if (wantVip && tenKV.contains("vip")) { kv = k; break; }
                if (!wantVip && !tenKV.contains("vip")) { kv = k; break; }
            }
            if (kv == null && !kvList.isEmpty()) kv = kvList.get(0);

            if (kv == null) { showErrorDialog("Không tìm thấy khu vực phù hợp trong hệ thống!"); return; }
            if (banDAO.isSoBanExists(soBan)) {
                showErrorDialog("Số bàn " + soBan + " đã tồn tại!\nVui lòng chọn số bàn khác.");
                return;
            }

            String maBan = banDAO.getNextMaBan();
            Ban newBan = new Ban();
            newBan.setMaBan(maBan);
            newBan.setSoBan(soBan);
            newBan.setSucChua(sucChua);
            newBan.setLoaiBan(loai);
            newBan.setKhuVuc(kv);
            newBan.setTinhTrangBan(TrangThaiBan.Trong);

            boolean ok = banDAO.addBan(newBan);
            if (ok) {
                dlg.dispose();
                loadCards();
            } else {
                showErrorDialog("Thêm bàn thất bại!\nVui lòng thử lại.");
            }
        });

        pBtns.add(btnCancel);
        pBtns.add(btnConfirm);

        root.add(header, BorderLayout.NORTH);
        root.add(form,   BorderLayout.CENTER);
        root.add(pBtns,  BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ── Custom confirm dialog ───────────────────────────────────────────────
    private boolean showConfirmDialog(String title, String message, Color accent) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), title,
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(400, 210);
        dlg.setLocationRelativeTo(this);

        final boolean[] result = {false};

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 226), 1));

        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 16, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        JPanel pMsg = new JPanel(new BorderLayout());
        pMsg.setOpaque(false);
        pMsg.setBorder(new EmptyBorder(22, 24, 10, 24));
        JLabel lblMsg = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        lblMsg.setForeground(new Color(50, 60, 70));
        pMsg.add(lblMsg, BorderLayout.CENTER);

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBtns.setOpaque(false);
        pBtns.setBorder(new EmptyBorder(0, 16, 16, 16));

        JButton btnCancel  = makeDialogBtn("Huỷ",      new Color(230, 232, 236), new Color(80, 90, 100));
        JButton btnConfirm = makeDialogBtn("Xác nhận", accent,                   Color.WHITE);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnConfirm.addActionListener(e -> { result[0] = true; dlg.dispose(); });

        pBtns.add(btnCancel);
        pBtns.add(btnConfirm);

        root.add(header, BorderLayout.NORTH);
        root.add(pMsg,   BorderLayout.CENTER);
        root.add(pBtns,  BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
        return result[0];
    }

    private void showErrorDialog(String message) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Lỗi",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setSize(400, 190);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 226), 1));

        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(RED_DANG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 16, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 48));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel lblTitle = new JLabel("Lỗi");
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        JPanel pMsg = new JPanel(new BorderLayout());
        pMsg.setOpaque(false);
        pMsg.setBorder(new EmptyBorder(18, 24, 8, 24));
        JLabel lblMsg = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMsg.setForeground(new Color(50, 60, 70));
        pMsg.add(lblMsg, BorderLayout.CENTER);

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBtns.setOpaque(false);
        pBtns.setBorder(new EmptyBorder(0, 16, 14, 16));
        JButton btnOk = makeDialogBtn("Đóng", RED_DANG, Color.WHITE);
        btnOk.addActionListener(e -> dlg.dispose());
        pBtns.add(btnOk);

        root.add(header, BorderLayout.NORTH);
        root.add(pMsg,   BorderLayout.CENTER);
        root.add(pBtns,  BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JButton makeDialogBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(120, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Color statusColor(TrangThaiBan tt) {
        if (tt == null) return GREEN;
        switch (tt) {
            case DaDuocDat:      return ORANGE;
            case DangDuocSuDung: return RED_DANG;
            case DangDonDep:     return PURPLE;
            case BaoTri:         return GRAY_MT;
            default:             return GREEN;
        }
    }

    private String statusLabel(TrangThaiBan tt) {
        if (tt == null) return "Trống";
        switch (tt) {
            case DaDuocDat:      return "Đã đặt trước";
            case DangDuocSuDung: return "Đang sử dụng";
            case DangDonDep:     return "Đang dọn dẹp";
            case BaoTri:         return "Bảo trì";
            default:             return "Trống";
        }
    }

    // ── WrapLayout: wraps children to next row when they exceed container width ──
    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension d = layoutSize(target, false);
            d.width -= getHgap() + 1;
            return d;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth <= 0) {
                    // Try to get width from the enclosing JViewport / JScrollPane
                    Container vp = SwingUtilities.getAncestorOfClass(JViewport.class, target);
                    if (vp != null) targetWidth = vp.getWidth();
                    if (targetWidth <= 0) {
                        Container sp = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
                        if (sp != null) targetWidth = sp.getWidth();
                    }
                    if (targetWidth <= 0) targetWidth = 900;
                }
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0; rowHeight = 0;
                    }
                    if (rowWidth != 0) rowWidth += hgap;
                    rowWidth  += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                addRow(dim, rowWidth, rowHeight);
                dim.width  += insets.left + insets.right + hgap * 2;
                dim.height += insets.top + insets.bottom + vgap * 2;
                if (SwingUtilities.getAncestorOfClass(JScrollPane.class, target) != null
                        && target.isValid()) {
                    dim.width -= hgap + 1;
                }
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowHeight;
        }
    }
}
