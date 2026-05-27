package gui;

import dao.*;
import entity.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Dialog Gọi Món – phiên bản 4.0 (POS Lock Logic).
 *
 * <p>Thay đổi so với v3:
 * <ul>
 *   <li>Kiểm tra trạng thái hóa đơn TRƯỚC KHI mở – từ chối nếu DA_THANH_TOAN / DA_HUY</li>
 *   <li>Toàn bộ UI bị disable khi HĐ đã thanh toán (double-check khi commit)</li>
 *   <li>Validation tại tầng commit(): không thể bypass qua UI</li>
 *   <li>Hiển thị banner cảnh báo rõ ràng khi bị khóa</li>
 * </ul>
 */
public class QuanLyDatBan_GM extends JDialog {

    private static final Color MAIN_BLUE  = Color.decode("#0B3D59");
    private static final Color RED_DANG   = Color.decode("#E74C3C");
    private static final Color BG_LIGHT   = Color.decode("#F0F2F5");
    private static final Color TEXT_DARK  = Color.decode("#2C3E50");
    private static final Color BORDER_CLR = Color.decode("#DDE1E7");
    private static final Color LOCK_BG    = Color.decode("#FFF3F3");
    private static final Color LOCK_TXT   = Color.decode("#C0392B");
    private static final DecimalFormat FMT = new DecimalFormat("#,###");

    private final HoaDon           hd;
    private final int              soBan;
    private final SanPham_DAO      spDAO;
    private final ChiTietHoaDon_DAO cthdDAO;
    private final HoaDon_DAO       hdDAO;
    private final Runnable         onDone;

    /** true khi hóa đơn đã khóa – không cho phép thêm/sửa/xóa */
    private final boolean isLocked;

    private final Map<String, Integer> cart = new LinkedHashMap<>();
    private DefaultTableModel tmCart;
    private JLabel lblTotal;

    // ── Constructor ───────────────────────────────────────────────────────

    public QuanLyDatBan_GM(Frame owner, HoaDon hd, int soBan, SanPham_DAO spDAO,
                           ChiTietHoaDon_DAO cthdDAO, HoaDon_DAO hdDAO, Runnable onDone) {
        super(owner, buildTitle(hd), true);
        this.hd      = hd;
        this.soBan   = soBan;
        this.spDAO   = spDAO;
        this.cthdDAO = cthdDAO;
        this.hdDAO   = hdDAO;
        this.onDone  = onDone;

        // ── Kiểm tra khóa ngay khi khởi tạo ─────────────────────────────
        TrangThaiThanhToan tt = hd.getTrangThaiThanhToan();
        this.isLocked = (tt == TrangThaiThanhToan.DA_THANH_TOAN
                || tt == TrangThaiThanhToan.DA_HUY);

        setSize(880, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Banner cảnh báo nếu bị khóa
        if (isLocked) add(buildLockBanner(tt), BorderLayout.NORTH);

        build();
    }

    /** Tiêu đề dialog thay đổi theo trạng thái. */
    private static String buildTitle(HoaDon hd) {
        TrangThaiThanhToan tt = hd.getTrangThaiThanhToan();
        if (tt == TrangThaiThanhToan.DA_THANH_TOAN)
            return "Chi tiết hóa đơn " + hd.getMaHD() + "  [ĐÃ KHÓA – Đã thanh toán]";
        if (tt == TrangThaiThanhToan.DA_HUY)
            return "Chi tiết hóa đơn " + hd.getMaHD() + "  [ĐÃ KHÓA – Đã hủy]";
        return "Thêm món vào hóa đơn " + hd.getMaHD();
    }

    /** Banner đỏ hiển thị khi bị khóa. */
    private JPanel buildLockBanner(TrangThaiThanhToan tt) {
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        banner.setBackground(LOCK_BG);
        banner.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 100, 100)));

        JLabel icon = new JLabel("🔒");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        String msg = (tt == TrangThaiThanhToan.DA_THANH_TOAN)
                ? "Hóa đơn đã thanh toán – Không thể gọi thêm món hoặc chỉnh sửa."
                : "Hóa đơn đã bị hủy – Không thể thực hiện bất kỳ thao tác nào.";

        JLabel lbl = new JLabel(msg);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(LOCK_TXT);

        banner.add(icon);
        banner.add(lbl);
        return banner;
    }

    // ── UI Builder ────────────────────────────────────────────────────────

    private void build() {
        List<SanPham> allSP = spDAO.getAllSanPham();
        Map<String, List<SanPham>> byLoai = new LinkedHashMap<>();
        for (SanPham sp : allSP) {
            if (!sp.isTrangThai()) continue;
            String loai = (sp.getLoaiSanPham() != null) ? sp.getLoaiSanPham().getTenLoai() : "Khác";
            byLoai.computeIfAbsent(loai, k -> new ArrayList<>()).add(sp);
        }

        // ── Danh mục bên trái (Có thanh cuộn) ─────────────────────────
        JPanel catPanel = new JPanel(new BorderLayout(0, 6));
        catPanel.setPreferredSize(new Dimension(190, 0));
        catPanel.setBackground(BG_LIGHT);
        catPanel.setBorder(new EmptyBorder(8, 8, 8, 4));

        JLabel catTitle = new JLabel("Danh mục");
        catTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        catPanel.add(catTitle, BorderLayout.NORTH);

        JPanel catList = new JPanel();
        catList.setLayout(new BoxLayout(catList, BoxLayout.Y_AXIS));
        catList.setOpaque(false);

        JPanel dishGrid = new JPanel(new GridLayout(0, 3, 8, 8));
        dishGrid.setBackground(Color.WHITE);
        dishGrid.setBorder(new EmptyBorder(6, 6, 6, 6));

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
            tb.setAlignmentX(Component.LEFT_ALIGNMENT);
            tb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

            if (cat.equals(first[0])) tb.setSelected(true);
            bg.add(tb);
            catList.add(tb);
            catList.add(Box.createVerticalStrut(4));

            tb.addActionListener(e -> {
                dishGrid.removeAll();
                for (SanPham sp : byLoai.getOrDefault(cat, new ArrayList<>()))
                    dishGrid.add(buildDishCard(sp, allSP));
                dishGrid.revalidate();
                dishGrid.repaint();
            });
        }

        JScrollPane catScroll = new JScrollPane(catList);
        catScroll.setBorder(null);
        catScroll.setOpaque(false);
        catScroll.getViewport().setOpaque(false);
        catScroll.getVerticalScrollBar().setUnitIncrement(16);
        catPanel.add(catScroll, BorderLayout.CENTER);

        if (first[0] != null)
            for (SanPham sp : byLoai.getOrDefault(first[0], new ArrayList<>()))
                dishGrid.add(buildDishCard(sp, allSP));

        JScrollPane dishScroll = new JScrollPane(dishGrid);

        // ── Giỏ hàng ─────────────────────────────────────────────────
        tmCart = new DefaultTableModel(new String[]{"Tên món", "SL", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tCart = new JTable(tmCart);
        styleTableStatic(tCart);
        tCart.setPreferredScrollableViewportSize(new Dimension(0, 100));

        lblTotal = new JLabel(isLocked ? "Hóa đơn đã khóa" : "Tổng thêm: 0đ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(isLocked ? LOCK_TXT : RED_DANG);

        JButton bCancel = btnStatic("Đóng", Color.WHITE, TEXT_DARK, true);
        JButton bOk     = btnStatic("Xác Nhận Món", MAIN_BLUE, Color.WHITE, false);
        bCancel.addActionListener(e -> dispose());

        if (isLocked) {
            bOk.setEnabled(false);
            bOk.setBackground(new Color(200, 200, 200));
            bOk.setText("🔒 Đã khóa");
            bOk.setToolTipText(hd.getTrangThaiThanhToan() == TrangThaiThanhToan.DA_THANH_TOAN
                    ? "Hóa đơn đã thanh toán, không thể gọi thêm món." : "Hóa đơn đã hủy.");
            setEnabled(catList, false);
            setEnabled(dishGrid, false);
        } else {
            bOk.addActionListener(e -> commit(allSP));
        }

        JPanel southBottom = new JPanel(new BorderLayout(8, 0));
        southBottom.setOpaque(false);
        southBottom.add(lblTotal, BorderLayout.WEST);
        JPanel bRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bRow.setOpaque(false);
        bRow.add(bCancel);
        bRow.add(bOk);
        southBottom.add(bRow, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setBorder(new EmptyBorder(6, 8, 8, 8));
        south.setBackground(Color.WHITE);
        south.add(new JScrollPane(tCart), BorderLayout.CENTER);
        south.add(southBottom, BorderLayout.SOUTH);

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catPanel, dishScroll);
        topSplit.setDividerLocation(190);
        topSplit.setDividerSize(4);
        topSplit.setBorder(null);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, south);
        mainSplit.setDividerLocation(isLocked ? 280 : 330);
        mainSplit.setDividerSize(4);
        mainSplit.setBorder(null);

        add(mainSplit, BorderLayout.CENTER);
    }

    /** Đệ quy disable tất cả component con. */
    private void setEnabled(Container container, boolean enabled) {
        for (Component c : container.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container) setEnabled((Container) c, enabled);
        }
    }

    private JPanel buildDishCard(SanPham sp, List<SanPham> allSP) {
        RoundedPanel card = new RoundedPanel(10, isLocked ? new Color(250, 250, 250) : Color.WHITE);
        card.setLayout(new BorderLayout(4, 4));
        card.setBorder(new CompoundBorder(new LineBorder(isLocked ? new Color(220, 220, 220) : BORDER_CLR, 1), new EmptyBorder(7, 8, 7, 8)));
        card.setFocusable(false);

        JLabel name = new JLabel("<html><b>" + sp.getTenMon() + "</b><br>"
                + "<font color='" + (isLocked ? "#AAAAAA" : "#E74C3C") + "'>"
                + FMT.format(sp.getGiaBan()) + "đ</font></html>");
        name.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        card.add(name, BorderLayout.CENTER);

        JPanel qr = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        qr.setOpaque(false);
        qr.setFocusable(false);

        JButton minus = qtyBtnS("−");
        minus.setFocusable(false);

        JTextField txtQty = new JTextField(String.valueOf(cart.getOrDefault(sp.getMaMon(), 0)), 3);
        txtQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtQty.setHorizontalAlignment(SwingConstants.CENTER);
        txtQty.setBorder(new LineBorder(BORDER_CLR));
        txtQty.setFocusable(true);

        // Sự kiện: Bôi đen khi chọn
        txtQty.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { txtQty.selectAll(); }
        });

        // Sự kiện: Nhấn Enter để xác nhận số lượng
        txtQty.addActionListener(e -> {
            try {
                int q = Integer.parseInt(txtQty.getText());
                if (q <= 0) cart.remove(sp.getMaMon());
                else cart.put(sp.getMaMon(), q);
                refreshCart(allSP);
            } catch (NumberFormatException ex) {
                txtQty.setText(String.valueOf(cart.getOrDefault(sp.getMaMon(), 0)));
            }
        });

        JButton plus = qtyBtnS("+");
        plus.setFocusable(false);

        if (!isLocked) {
            plus.addActionListener(e -> {
                int q = cart.getOrDefault(sp.getMaMon(), 0) + 1;
                cart.put(sp.getMaMon(), q);
                txtQty.setText(String.valueOf(q));
                refreshCart(allSP);
            });
            minus.addActionListener(e -> {
                int q = cart.getOrDefault(sp.getMaMon(), 0);
                if (q > 0) {
                    if (--q == 0) cart.remove(sp.getMaMon());
                    else cart.put(sp.getMaMon(), q);
                    txtQty.setText(String.valueOf(q));
                    refreshCart(allSP);
                }
            });
        } else {
            txtQty.setEditable(false);
        }

        qr.add(minus);
        qr.add(txtQty);
        qr.add(plus);
        card.add(qr, BorderLayout.SOUTH);
        return card;
    }

    private void refreshCart(List<SanPham> allSP) {
        tmCart.setRowCount(0);
        double total = 0;
        Map<String, SanPham> map = new HashMap<>();
        for (SanPham sp : allSP) map.put(sp.getMaMon(), sp);
        for (Map.Entry<String, Integer> e : cart.entrySet()) {
            SanPham sp = map.get(e.getKey());
            if (sp == null) continue;
            double tt = sp.getGiaBan() * e.getValue();
            tmCart.addRow(new Object[]{sp.getTenMon(), e.getValue(), FMT.format(tt) + "đ"});
            total += tt;
        }
        lblTotal.setText("Tổng thêm: " + FMT.format(total) + "đ");
    }

    /**
     * Commit – double-check trạng thái tại đây trước khi ghi DB.
     * Đảm bảo không thể bypass dù thao tác qua màn hình khác.
     */
    private void commit(List<SanPham> allSP) {
        if (cart.isEmpty()) { dispose(); return; }

        // ── Double-check tại tầng business logic ─────────────────────
        HoaDon fresh = hdDAO.getHoaDonByMa(hd.getMaHD());
        if (fresh != null) {
            TrangThaiThanhToan ttFresh = fresh.getTrangThaiThanhToan();
            if (ttFresh == TrangThaiThanhToan.DA_THANH_TOAN) {
                JOptionPane.showMessageDialog(this,
                        "Hóa đơn " + hd.getMaHD() + " đã được thanh toán!\n"
                                + "Không thể thêm món vào hóa đơn đã thanh toán.",
                        "Hóa đơn đã khóa", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
            if (ttFresh == TrangThaiThanhToan.DA_HUY) {
                JOptionPane.showMessageDialog(this,
                        "Hóa đơn " + hd.getMaHD() + " đã bị hủy!",
                        "Hóa đơn đã khóa", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
        }

        // ── Thêm món ─────────────────────────────────────────────────
        Map<String, SanPham> map = new HashMap<>();
        for (SanPham sp : allSP) map.put(sp.getMaMon(), sp);
        double added = 0;
        List<String> errors = new ArrayList<>();

        for (Map.Entry<String, Integer> e : cart.entrySet()) {
            SanPham sp = map.get(e.getKey());
            if (sp == null || e.getValue() <= 0) continue;
            double tt = sp.getGiaBan() * e.getValue();
            int status = cthdDAO.getChiTietStatus(hd.getMaHD(), sp.getMaMon());

            if (status == 2 || status == 3) {
                // Bếp đang nấu (DANG_LAM) hoặc tất cả đã xong (DA_XONG)
                // → INSERT dòng mới để bếp thấy phần gọi thêm riêng biệt
                HoaDon ref = new HoaDon(); ref.setMaHD(hd.getMaHD());
                boolean ok = cthdDAO.create(new ChiTietHoaDon(sp, ref, e.getValue(), sp.getGiaBan(),
                        "Bàn " + soBan + " gọi thêm", tt));
                if (ok) {
                    added += tt;
                } else {
                    errors.add(sp.getTenMon());
                }

            } else if (status == 1) {
                // Có row CHO_XU_LY đang chờ → cộng dồn số lượng trên row đó
                int oldQty = 0; double oldTt = 0;
                for (ChiTietHoaDon ct : cthdDAO.getChiTietByMaHD(hd.getMaHD())) {
                    if (ct.getMonAn().getMaMon().equals(sp.getMaMon())
                            && ct.getTrangThaiMon() == entity.TrangThaiMon.CHO_XU_LY) {
                        oldQty = ct.getSoLuong();
                        oldTt  = ct.getThanhTien();
                        break;
                    }
                }
                int nq = oldQty + e.getValue();
                // FIX: kiểm tra return value của updateSoLuong
                boolean ok = cthdDAO.updateSoLuong(hd.getMaHD(), sp.getMaMon(), nq, sp.getGiaBan() * nq);
                if (ok) {
                    added += (sp.getGiaBan() * nq - oldTt);
                } else {
                    errors.add(sp.getTenMon());
                }

            } else {
                // Lần đầu gọi món này → INSERT mới, ghiChu trống
                HoaDon ref = new HoaDon(); ref.setMaHD(hd.getMaHD());
                boolean ok = cthdDAO.create(new ChiTietHoaDon(sp, ref, e.getValue(), sp.getGiaBan(), "", tt));
                if (ok) {
                    added += tt;
                } else {
                    errors.add(sp.getTenMon());
                }
            }
        }

        if (added > 0) {
            hdDAO.updateTongTien(hd.getMaHD(), hd.getTongTien() + added);
        }

        // Báo lỗi nếu có món thêm thất bại
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không thể thêm các món sau (lỗi DB):\n• " + String.join("\n• ", errors),
                    "Lỗi thêm món", JOptionPane.ERROR_MESSAGE);
        }

        dispose();
        onDone.run();
    }

    // ── UI helpers ────────────────────────────────────────────────────────

    private static void styleTableStatic(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setRowHeight(24);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private static JButton btnStatic(String text, Color bg, Color fg, boolean outlined) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
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
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setFocusPainted(false);
        b.setBackground(BG_LIGHT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static class RoundedPanel extends JPanel {
        private final int   radius;
        private final Color fillColor;
        RoundedPanel(int radius, Color fill) {
            this.radius = radius; this.fillColor = fill; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}