package gui;

import dao.*;
import entity.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class QLDB_DatBan extends JDialog {
    // ── Constants được mang qua ──────────────────────────────────────────
    private static final Color MAIN_BLUE  = Color.decode("#0B3D59");
    private static final Color RED_DANG   = Color.decode("#E74C3C");
    private static final Color BG_LIGHT   = Color.decode("#F0F2F5");
    private static final Color TEXT_DARK  = Color.decode("#2C3E50");
    private static final Color BORDER_CLR = Color.decode("#DDE1E7");
    private static final DecimalFormat FMT = new DecimalFormat("#,###");

    private final HoaDon hd;
    private final SanPham_DAO spDAO;
    private final ChiTietHoaDon_DAO cthdDAO;
    private final HoaDon_DAO hdDAO;
    private final Runnable onDone;
    private final Map<String, Integer> cart = new LinkedHashMap<>();
    private DefaultTableModel tmCart;
    private JLabel lblTotal;

    public QLDB_DatBan(Frame owner, HoaDon hd, SanPham_DAO spDAO,
                       ChiTietHoaDon_DAO cthdDAO, HoaDon_DAO hdDAO, Runnable onDone) {
        super(owner, "Thêm món vào hóa đơn", true);
        this.hd = hd;
        this.spDAO = spDAO;
        this.cthdDAO = cthdDAO;
        this.hdDAO = hdDAO;
        this.onDone = onDone;

        setSize(880, 580);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        List<SanPham> allSP = spDAO.getAllSanPham();
        Map<String, List<SanPham>> byLoai = new LinkedHashMap<>();
        for (SanPham sp : allSP) {
            if (!sp.isTrangThai()) continue; // skip hết món
            String loai = (sp.getLoaiSanPham() != null) ? sp.getLoaiSanPham().getTenLoai() : "Khác";
            byLoai.computeIfAbsent(loai, k -> new ArrayList<>()).add(sp);
        }

        JPanel catPanel = new JPanel(new BorderLayout(0, 6));
        catPanel.setPreferredSize(new Dimension(190, 0));
        catPanel.setBackground(BG_LIGHT);
        catPanel.setBorder(new EmptyBorder(8, 8, 8, 4));
        JLabel catTitle = new JLabel("Danh mục");
        catTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        catPanel.add(catTitle, BorderLayout.NORTH);

        JPanel dishGrid = new JPanel(new GridLayout(0, 3, 8, 8));
        dishGrid.setBackground(Color.WHITE);
        dishGrid.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel catList = new JPanel(new GridLayout(0, 1, 0, 4));
        catList.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        String[] first = {byLoai.keySet().stream().findFirst().orElse(null)};
        for (String cat : byLoai.keySet()) {
            JToggleButton tb = new JToggleButton(cat) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected() ? MAIN_BLUE : new Color(210, 234, 255));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    setForeground(isSelected() ? Color.WHITE : TEXT_DARK);
                    super.paintComponent(g);
                }
            };
            tb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tb.setFocusPainted(false);
            tb.setContentAreaFilled(false);
            tb.setOpaque(false);
            tb.setBorderPainted(false);
            tb.setBorder(new EmptyBorder(6, 10, 6, 10));
            if (cat.equals(first[0])) tb.setSelected(true);
            bg.add(tb); catList.add(tb);
            tb.addActionListener(e -> {
                dishGrid.removeAll();
                for (SanPham sp : byLoai.getOrDefault(cat, new ArrayList<>()))
                    dishGrid.add(buildDishCard(sp, allSP));
                dishGrid.revalidate(); dishGrid.repaint();
            });
        }
        catPanel.add(new JScrollPane(catList), BorderLayout.CENTER);
        if (first[0] != null)
            for (SanPham sp : byLoai.getOrDefault(first[0], new ArrayList<>()))
                dishGrid.add(buildDishCard(sp, allSP));

        JScrollPane dishScroll = new JScrollPane(dishGrid);

        tmCart = new DefaultTableModel(new String[]{"Tên món", "SL", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tCart = new JTable(tmCart);
        styleTableStatic(tCart);
        tCart.setPreferredScrollableViewportSize(new Dimension(0, 100));

        lblTotal = new JLabel("Tổng thêm: 0đ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(RED_DANG);

        JButton bCancel = btnStatic("Hủy",               Color.WHITE, TEXT_DARK,  true);
        JButton bOk     = btnStatic("Thêm món", MAIN_BLUE,   Color.WHITE, false);
        bCancel.addActionListener(e -> dispose());
        bOk.addActionListener(e     -> commit(allSP));

        JPanel southBottom = new JPanel(new BorderLayout(8, 0));
        southBottom.setOpaque(false);
        southBottom.add(lblTotal, BorderLayout.WEST);
        JPanel bRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bRow.setOpaque(false);
        bRow.add(bCancel); bRow.add(bOk);
        southBottom.add(bRow, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setBorder(new EmptyBorder(6, 8, 8, 8));
        south.setBackground(Color.WHITE);
        south.add(new JScrollPane(tCart), BorderLayout.CENTER);
        south.add(southBottom, BorderLayout.SOUTH);

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catPanel, dishScroll);
        topSplit.setDividerLocation(190); topSplit.setDividerSize(4); topSplit.setBorder(null);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, south);
        mainSplit.setDividerLocation(330); mainSplit.setDividerSize(4); mainSplit.setBorder(null);
        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel buildDishCard(SanPham sp, List<SanPham> allSP) {
        RoundedPanel card = new RoundedPanel(10, Color.WHITE);
        card.setLayout(new BorderLayout(4, 4));
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_CLR, 1), new EmptyBorder(7, 8, 7, 8)));
        JLabel name = new JLabel("<html><b>" + sp.getTenMon() + "</b><br>"
                + "<font color='#E74C3C'>" + FMT.format(sp.getGiaBan()) + "đ</font></html>");
        name.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        card.add(name, BorderLayout.CENTER);

        JPanel qr = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        qr.setOpaque(false);
        JButton minus = qtyBtnS("−");
        JLabel cnt = new JLabel(String.valueOf(cart.getOrDefault(sp.getMaMon(), 0)));
        cnt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cnt.setPreferredSize(new Dimension(24, 22));
        cnt.setHorizontalAlignment(SwingConstants.CENTER);
        JButton plus = qtyBtnS("+");
        plus.addActionListener(e -> {
            int q = cart.getOrDefault(sp.getMaMon(), 0) + 1;
            cart.put(sp.getMaMon(), q); cnt.setText(String.valueOf(q)); refreshCart(allSP);
        });
        minus.addActionListener(e -> {
            int q = cart.getOrDefault(sp.getMaMon(), 0);
            if (q > 0) {
                if (--q == 0) cart.remove(sp.getMaMon()); else cart.put(sp.getMaMon(), q);
                cnt.setText(String.valueOf(q)); refreshCart(allSP);
            }
        });
        qr.add(minus); qr.add(cnt); qr.add(plus);
        card.add(qr, BorderLayout.SOUTH);
        return card;
    }

    private void refreshCart(List<SanPham> allSP) {
        tmCart.setRowCount(0);
        double total = 0;
        Map<String,SanPham> map = new HashMap<>();
        for (SanPham sp : allSP) map.put(sp.getMaMon(), sp);
        for (Map.Entry<String,Integer> e : cart.entrySet()) {
            SanPham sp = map.get(e.getKey()); if (sp == null) continue;
            double tt = sp.getGiaBan() * e.getValue();
            tmCart.addRow(new Object[]{sp.getTenMon(), e.getValue(), FMT.format(tt) + "đ"});
            total += tt;
        }
        lblTotal.setText("Tổng thêm: " + FMT.format(total) + "đ");
    }

    private void commit(List<SanPham> allSP) {
        if (cart.isEmpty()) { dispose(); return; }
        Map<String,SanPham> map = new HashMap<>();
        for (SanPham sp : allSP) map.put(sp.getMaMon(), sp);
        double added = 0;
        for (Map.Entry<String,Integer> e : cart.entrySet()) {
            SanPham sp = map.get(e.getKey());
            if (sp == null || e.getValue() <= 0) continue;
            double tt = sp.getGiaBan() * e.getValue();
            if (cthdDAO.existsChiTiet(hd.getMaHD(), sp.getMaMon())) {
                int oldQty = 0; double oldTt = 0;
                for (ChiTietHoaDon ct : cthdDAO.getChiTietByMaHD(hd.getMaHD()))
                    if (ct.getMonAn().getMaMon().equals(sp.getMaMon())) {
                        oldQty = ct.getSoLuong(); oldTt = ct.getThanhTien(); break;
                    }
                int nq = oldQty + e.getValue();
                double ntt = sp.getGiaBan() * nq;
                cthdDAO.updateSoLuong(hd.getMaHD(), sp.getMaMon(), nq, ntt);
                added += (ntt - oldTt);
            } else {
                HoaDon ref = new HoaDon(); ref.setMaHD(hd.getMaHD());
                cthdDAO.create(new ChiTietHoaDon(sp, ref, e.getValue(), sp.getGiaBan(), "", tt));
                added += tt;
            }
        }
        hdDAO.updateTongTien(hd.getMaHD(), hd.getTongTien() + added);
        dispose(); onDone.run();
    }

    // ── Các hàm UI helper tĩnh ───────────────────────────────────────────
    private static void styleTableStatic(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setRowHeight(24); t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private static JButton btnStatic(String text, Color bg, Color fg, boolean outlined) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(160, 36));
        b.setBorder(outlined
                ? new CompoundBorder(new LineBorder(BORDER_CLR), new EmptyBorder(7, 24, 7, 24))
                : new EmptyBorder(7, 24, 7, 24));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton qtyBtnS(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(26, 26));
        b.setMargin(new Insets(0, 0, 0, 0)); b.setFocusPainted(false);
        b.setBackground(BG_LIGHT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── RoundedPanel dùng nội bộ ─────────────────────────────────────────
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fillColor;

        RoundedPanel(int radius, Color fill) {
            this.radius = radius; this.fillColor = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}