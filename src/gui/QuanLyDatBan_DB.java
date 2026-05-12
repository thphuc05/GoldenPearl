package gui;

import dao.*;
import entity.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class QuanLyDatBan_DB extends JPanel {

    // ── constants (Bê từ bên kia sang) ───────────────────────────────────
    private static final double TIEN_COC    = 500_000.0;
    private static final Color  MAIN_BLUE   = Color.decode("#0B3D59");
    private static final Color  RED_DANG    = Color.decode("#E74C3C");
    private static final Color  BG_LIGHT    = Color.decode("#F0F2F5");
    private static final Color  TEXT_DARK   = Color.decode("#2C3E50");
    private static final Color  BORDER_CLR  = Color.decode("#DDE1E7");
    private static final DecimalFormat FMT  = new DecimalFormat("#,###");

    private static final String[] SLOT_KEYS    = {"SANG",        "CHIEU",       "TOI"};
    private static final String[] SLOT_LABELS  = {"10:00–14:00", "15:00–19:00", "19:30–23:00"};
    private static final int[]    SLOT_START_H = {10, 15, 19};
    private static final int[]    SLOT_START_M = { 0,  0, 30};
    private static final int[]    SLOT_END_H   = {14, 19, 23};
    private static final int[]    SLOT_END_M   = { 0,  0,  0};

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final Ban_DAO            banDAO  = new Ban_DAO();
    private final KhachHang_DAO      khDAO   = new KhachHang_DAO();
    private final DonDatBan_DAO      ddbDAO  = new DonDatBan_DAO();
    private final HoaDon_DAO         hdDAO   = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO  cthdDAO = new ChiTietHoaDon_DAO();
    private final SanPham_DAO        spDAO   = new SanPham_DAO();

    private final ChiTietDatBan_DAO  ctdbDAO = new ChiTietDatBan_DAO();

    // ── state ────────────────────────────────────────────────────────────
    private final NhanVien currentNV;
    /** Danh sách bàn được chọn cho đơn đặt này (1 hoặc nhiều bàn). */
    private final List<Ban> selectedBans;
    private final String currentFilter;
    private final Date selectedBookingDate;
    private final BookingListener listener;

    // booking widgets
    private JLabel            lblBookingTitle, lblSlotDisplay;
    private JTextField        txtTenKH, txtSdtKH, txtGhiChu;
    private JLabel            lblCartSummary;
    private Map<String,Integer> bookingCart = new LinkedHashMap<>();

    // ── Interface Callback ───────────────────────────────────────────────
    public interface BookingListener {
        void onBookingSuccess();
        void onCancel();
    }

    public QuanLyDatBan_DB(NhanVien nhanVien, List<Ban> bans, Date bookingDate, String filter, BookingListener listener) {
        this.currentNV = nhanVien;
        this.selectedBans = bans != null ? bans : new ArrayList<>();
        this.selectedBookingDate = bookingDate;
        this.currentFilter = filter;
        this.listener = listener;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        buildUI();
    }

    private void buildUI() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(245, 247, 250));
        hdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_CLR),
                new EmptyBorder(10, 14, 10, 14)));

        // Tên bàn: "Bàn 1" hoặc "Bàn 1, Bàn 3, Bàn 5"
        String tenBan = selectedBans.stream()
                .map(b -> "Bàn " + b.getSoBan())
                .collect(java.util.stream.Collectors.joining(", "));
        int tongSucChua = selectedBans.stream().mapToInt(Ban::getSucChua).sum();
        String loai = selectedBans.size() == 1 && selectedBans.get(0).getLoaiBan() != null
                ? selectedBans.get(0).getLoaiBan() : (selectedBans.size() > 1 ? "Nhiều bàn" : "Thường");

        lblBookingTitle = new JLabel(tenBan + "  ·  " + loai + "  ·  " + tongSucChua + " người");
        lblBookingTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBookingTitle.setForeground(MAIN_BLUE);
        hdr.add(lblBookingTitle, BorderLayout.WEST);

        JLabel sub = new JLabel("Tiền cọc bắt buộc: " + FMT.format(TIEN_COC) + "đ");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sub.setForeground(RED_DANG);
        hdr.add(sub, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(10, 14, 10, 14));

        // ── read-only slot/date display
        body.add(sectionLabel("THÔNG TIN ĐẶT BÀN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        lblSlotDisplay = new JLabel("  Ngày:  " + sdf.format(selectedBookingDate) + "     Khung giờ:  " + getSlotLabel(currentFilter));
        lblSlotDisplay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSlotDisplay.setForeground(MAIN_BLUE);
        lblSlotDisplay.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 210, 240), 1),
                new EmptyBorder(7, 12, 7, 12)));
        lblSlotDisplay.setOpaque(true);
        lblSlotDisplay.setBackground(new Color(236, 244, 255));
        lblSlotDisplay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblSlotDisplay.setAlignmentX(0f);
        body.add(lblSlotDisplay);
        body.add(Box.createVerticalStrut(8));
        body.add(hsep());

        // ── customer info
        body.add(sectionLabel("THÔNG TIN KHÁCH HÀNG"));
        body.add(fieldRow("Tên khách  *",     txtTenKH  = inputField()));
        body.add(Box.createVerticalStrut(6));
        body.add(fieldRow("Số điện thoại  *", txtSdtKH  = inputField()));
        body.add(Box.createVerticalStrut(6));
        body.add(fieldRow("Ghi chú",          txtGhiChu = inputField()));
        body.add(Box.createVerticalStrut(4));
        body.add(hsep());

        txtSdtKH.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                String sdt = txtSdtKH.getText().trim();
                if (!sdt.isEmpty() && txtTenKH.getText().trim().isEmpty()) {
                    KhachHang kh = khDAO.getKhachHangBySdt(sdt);
                    if (kh != null) txtTenKH.setText(kh.getTenKH());
                }
            }
        });

        // ── optional pre-order
        JButton btnPreorder = new JButton("GỌI MÓN TRƯỚC  (Tùy chọn)");
        btnPreorder.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPreorder.setBackground(new Color(236, 244, 255));
        btnPreorder.setForeground(MAIN_BLUE);
        btnPreorder.setFocusPainted(false);
        btnPreorder.setBorder(new EmptyBorder(7, 12, 7, 12));
        btnPreorder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnPreorder.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPreorder.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPreorder.addActionListener(e -> {
            new PreorderDishDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    spDAO, bookingCart, updatedCart -> {
                bookingCart.clear();
                bookingCart.putAll(updatedCart);
                refreshCartSummary();
            }).setVisible(true);
        });
        body.add(btnPreorder);

        lblCartSummary = new JLabel("Chưa gọi món trước");
        lblCartSummary.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblCartSummary.setForeground(new Color(130, 130, 130));
        lblCartSummary.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCartSummary.setBorder(new EmptyBorder(3, 4, 3, 4));
        body.add(lblCartSummary);
        body.add(hsep());

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(14);
        add(bodyScroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(10, 14, 10, 14)));
        btnRow.setBackground(Color.WHITE);
        JButton bHuy = actionBtn("HỦY",               Color.WHITE, TEXT_DARK,  true);
        JButton bOk  = actionBtn("XÁC NHẬN ĐẶT BÀN",  MAIN_BLUE,  Color.WHITE, false);

        bHuy.addActionListener(e -> listener.onCancel());
        bOk.addActionListener(e  -> doConfirmBooking());

        btnRow.add(bHuy); btnRow.add(bOk);
        add(btnRow, BorderLayout.SOUTH);
    }

    private void refreshCartSummary() {
        if (lblCartSummary == null) return;
        if (bookingCart.isEmpty()) {
            lblCartSummary.setText("Chưa gọi món trước");
            lblCartSummary.setForeground(new Color(130, 130, 130));
            return;
        }
        Map<String, SanPham> map = new HashMap<>();
        for (SanPham sp : spDAO.getAllSanPham()) map.put(sp.getMaMon(), sp);
        int totalQty = 0; double total = 0;
        for (Map.Entry<String,Integer> e : bookingCart.entrySet()) {
            SanPham sp = map.get(e.getKey()); if (sp == null) continue;
            totalQty += e.getValue();
            total += sp.getGiaBan() * e.getValue();
        }
        lblCartSummary.setText("Đã gọi trước: " + totalQty + " món  |  Tổng: " + FMT.format(total) + "đ");
        lblCartSummary.setForeground(RED_DANG);
    }

    // ── Xử lý Logic ───────────────────────────────────────────────────────
    private void doConfirmBooking() {
        String ten = txtTenKH.getText().trim();
        String sdt = txtSdtKH.getText().trim();
        if (ten.isEmpty()) { msg("Vui lòng nhập tên khách hàng!"); return; }
        if (!sdt.matches("0\\d{9}")) { msg("Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)!"); return; }
        if (selectedBans.isEmpty()) { msg("Không có bàn nào được chọn!"); return; }
        if (isToday(selectedBookingDate) && isSlotPastNow(getSlotIndex(currentFilter))) {
            msg("Khung giờ " + getSlotLabel(currentFilter) + " hôm nay đã qua!\nVui lòng chọn khung giờ khác.");
            return;
        }

        // Kiểm tra tất cả bàn xem có bị trùng lịch không
        for (Ban ban : selectedBans) {
            if (findActiveDon(ban.getMaBan()) != null) {
                msg("Bàn " + ban.getSoBan() + " đã được đặt trong khung giờ "
                        + getSlotLabel(currentFilter)
                        + "\nngày " + new SimpleDateFormat("dd/MM/yyyy").format(selectedBookingDate) + "!");
                return;
            }
        }

        KhachHang kh = khDAO.getKhachHangBySdt(sdt);
        if (kh == null) {
            kh = new KhachHang();
            kh.setMaKH(khDAO.getNextMaKH());
            kh.setTenKH(ten);
            kh.setSoDT(sdt);
            khDAO.addKhachHang(kh);
        }

        // Tổng sức chứa của tất cả bàn được chọn
        int tongSucChua = selectedBans.stream().mapToInt(Ban::getSucChua).sum();

        Timestamp now = new Timestamp(System.currentTimeMillis());
        DonDatBan don = new DonDatBan();
        don.setMaDon(ddbDAO.getNextMaDon());
        don.setThoiGianDat(now);
        don.setThoiGianDen(selectedBookingDate);
        don.setSoLuongKhach(tongSucChua);
        don.setKhachHang(kh);
        don.setNhanVien(currentNV);
        don.setDsBan(new ArrayList<>(selectedBans));   // ← gán danh sách bàn
        don.setTrangThai(false);
        don.setKhungGio(currentFilter);
        don.setGhiChu(txtGhiChu.getText().trim());

        // INSERT DonDatBan + INSERT ChiTietDatBan cho mỗi bàn (trong DAO)
        ddbDAO.addDonDatBan(don);

        // Pre-order
        List<SanPham> allSP = spDAO.getAllSanPham();
        Map<String,SanPham> spMap = new HashMap<>();
        for (SanPham sp : allSP) spMap.put(sp.getMaMon(), sp);
        double foodTotal = 0;
        for (Map.Entry<String,Integer> e : bookingCart.entrySet()) {
            SanPham sp = spMap.get(e.getKey());
            if (sp != null) foodTotal += sp.getGiaBan() * e.getValue();
        }

        HoaDon hd = new HoaDon();
        hd.setMaHD(hdDAO.getNextMaHD());
        hd.setNgayLap(now);
        hd.setThoiGian(new java.sql.Time(now.getTime()));
        hd.setTongTien(TIEN_COC + foodTotal);
        hd.setTrangThai(false);
        hd.setDonDatBan(don);
        hd.setNhanVien(currentNV);
        hd.setKhachHang(kh);
        hd.setTienCoc(TIEN_COC);
        hdDAO.create(hd);

        for (Map.Entry<String,Integer> e : bookingCart.entrySet()) {
            SanPham sp = spMap.get(e.getKey());
            if (sp == null || e.getValue() <= 0) continue;
            double tt = sp.getGiaBan() * e.getValue();
            HoaDon ref = new HoaDon(); ref.setMaHD(hd.getMaHD());
            cthdDAO.create(new ChiTietHoaDon(sp, ref, e.getValue(), sp.getGiaBan(), "", tt));
        }

        // Cập nhật trạng thái tất cả bàn được chọn
        if (isToday(selectedBookingDate)) {
            for (Ban ban : selectedBans) {
                banDAO.updateTinhTrangBan(ban.getMaBan(), TrangThaiBan.DaDuocDat);
            }
        }

        // Xây chuỗi tên bàn để hiển thị trong thông báo
        String tenBan = selectedBans.stream()
                .map(b -> "Bàn " + b.getSoBan())
                .collect(java.util.stream.Collectors.joining(", "));

        JOptionPane.showMessageDialog(this,
                "Đặt bàn thành công!\nBàn: " + tenBan
                        + "\nNgày: " + new SimpleDateFormat("dd/MM/yyyy").format(selectedBookingDate)
                        + "\nKhung giờ: " + getSlotLabel(currentFilter)
                        + "\nTiền cọc: " + FMT.format(TIEN_COC) + "đ",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);

        listener.onBookingSuccess();
    }

    private DonDatBan findActiveDon(String maBan) {
        List<DonDatBan> allDons = ddbDAO.getAllDonDatBanWithBan();
        for (DonDatBan d : allDons) {
            boolean hasBan = d.getDsBan().stream()
                    .anyMatch(b -> b.getMaBan().equals(maBan));
            if (!d.isTrangThai() && hasBan
                    && isSameDay(d.getThoiGianDen(), selectedBookingDate)
                    && currentFilter.equals(d.getKhungGio())) {
                return d;
            }
        }
        return null;
    }

    // ── Time Helpers ─────────────────────────────────────────────────────
    private static boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = Calendar.getInstance(); c1.setTime(d1);
        Calendar c2 = Calendar.getInstance(); c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
    private static boolean isToday(Date d) { return isSameDay(d, new Date()); }
    private boolean isSlotPastNow(int idx) {
        if (!isToday(selectedBookingDate)) return false;
        Calendar now = Calendar.getInstance();
        int h = now.get(Calendar.HOUR_OF_DAY);
        int m = now.get(Calendar.MINUTE);
        return h > SLOT_END_H[idx] || (h == SLOT_END_H[idx] && m >= SLOT_END_M[idx]);
    }
    private int getSlotIndex(String key) {
        for (int i = 0; i < SLOT_KEYS.length; i++) if (SLOT_KEYS[i].equals(key)) return i;
        return 0;
    }
    private String getSlotLabel(String key) { return SLOT_LABELS[getSlotIndex(key)]; }

    // ── UI Helpers ───────────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(100, 120, 150));
        l.setAlignmentX(0f);
        l.setBorder(new EmptyBorder(6, 0, 3, 0));
        return l;
    }
    private JSeparator hsep() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setForeground(BORDER_CLR);
        return s;
    }
    private JPanel fieldRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setPreferredSize(new Dimension(130, 20));
        row.add(lbl, BorderLayout.WEST);
        if (field != null) row.add(field, BorderLayout.CENTER);
        return row;
    }
    private JTextField inputField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        f.setPreferredSize(new Dimension(0, 32));
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_CLR, 1), new EmptyBorder(3, 8, 3, 8)));
        return f;
    }
    private JButton actionBtn(String text, Color bg, Color fg, boolean outlined) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
        b.setBorder(outlined ? new LineBorder(BORDER_CLR) : new EmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void msg(String m) { JOptionPane.showMessageDialog(this, m, "Thông báo", JOptionPane.WARNING_MESSAGE); }


    // ── PreorderDishDialog (Y nguyên code cũ) ────────────────────────────
    interface CartCallback { void onConfirm(Map<String,Integer> cart); }

    private static class PreorderDishDialog extends JDialog {
        private final SanPham_DAO spDAO;
        private final Map<String,Integer> initialCart;
        private final CartCallback callback;
        private final Map<String,Integer> cart = new LinkedHashMap<>();
        private DefaultTableModel tmCart;
        private JLabel lblTotal;

        PreorderDishDialog(Frame owner, SanPham_DAO spDAO, Map<String,Integer> initialCart, CartCallback callback) {
            super(owner, "Gọi món trước", true);
            this.spDAO = spDAO; this.initialCart = initialCart; this.callback = callback;
            cart.putAll(initialCart);
            setSize(880, 580);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            build();
        }

        private void build() {
            List<SanPham> allSP = spDAO.getAllSanPham();
            Map<String, List<SanPham>> byLoai = new LinkedHashMap<>();
            for (SanPham sp : allSP) {
                if (!sp.isTrangThai()) continue;
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
                tb.setFocusPainted(false); tb.setContentAreaFilled(false);
                tb.setOpaque(false); tb.setBorderPainted(false);
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

            tmCart = new DefaultTableModel(new String[]{"Tên món", "SL", "Đơn giá", "Thành tiền"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable tCart = new JTable(tmCart);
            styleTableStatic(tCart);
            tCart.setPreferredScrollableViewportSize(new Dimension(0, 100));

            lblTotal = new JLabel("Tổng gọi món: 0đ");
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTotal.setForeground(RED_DANG);

            JButton bCancel = btnStatic("Hủy",     Color.WHITE, TEXT_DARK, true);
            JButton bOk     = btnStatic("XÁC NHẬN", MAIN_BLUE,  Color.WHITE, false);
            bCancel.addActionListener(e -> dispose());
            bOk.addActionListener(e -> { callback.onConfirm(new LinkedHashMap<>(cart)); dispose(); });

            JPanel southBottom = new JPanel(new BorderLayout(8, 0));
            southBottom.setOpaque(false);
            southBottom.add(lblTotal, BorderLayout.WEST);
            JPanel bRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            bRow.setOpaque(false); bRow.add(bCancel); bRow.add(bOk);
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

            refreshCart(allSP);
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
            if (tmCart == null) return;
            tmCart.setRowCount(0);
            double total = 0;
            Map<String,SanPham> map = new HashMap<>();
            for (SanPham sp : allSP) map.put(sp.getMaMon(), sp);
            for (Map.Entry<String,Integer> e : cart.entrySet()) {
                SanPham sp = map.get(e.getKey()); if (sp == null) continue;

                double donGia = sp.getGiaBan();
                double tt = donGia * e.getValue(); // Đã nhân số lượng

                // Add đủ 4 cột vào bảng
                tmCart.addRow(new Object[]{
                        sp.getTenMon(),
                        e.getValue(),
                        FMT.format(donGia) + "đ",  // Cột Đơn giá
                        FMT.format(tt) + "đ"       // Cột Thành tiền
                });
                total += tt;
            }
            if (lblTotal != null) lblTotal.setText("Tổng gọi món: " + FMT.format(total) + "đ");
        }

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
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fillColor;
        RoundedPanel(int radius, Color fill) {
            this.radius = radius; this.fillColor = fill;
            setOpaque(false);
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