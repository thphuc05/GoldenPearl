package gui;

import dao.*;
import entity.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class QuanLyDatBan extends JPanel {

    // ── constants ────────────────────────────────────────────────────────
    private static final double TIEN_COC    = 500_000.0;
    private static final Color  MAIN_BLUE   = Color.decode("#0B3D59");
    private static final Color  GOLD_COLOR  = Color.decode("#C5A059");
    private static final Color  GREEN_TRONG = Color.decode("#27AE60");
    private static final Color  AMBER_DAT   = Color.decode("#E67E22");
    private static final Color  RED_DANG    = Color.decode("#E74C3C");
    private static final Color  BG_LIGHT    = Color.decode("#F0F2F5");
    private static final Color  TEXT_DARK   = Color.decode("#2C3E50");
    private static final Color  BORDER_CLR  = Color.decode("#DDE1E7");
    private static final DecimalFormat FMT  = new DecimalFormat("#,###");

    // --- ĐỊNH NGHĨA KHUNG GIỜ (SLOTS) ---
    private static final String[] SLOT_KEYS    = {"SANG",        "CHIEU",       "TOI"};
    private static final String[] SLOT_LABELS  = {"10:00–14:00", "15:00–19:00", "19:30–23:00"};
    private static final int[]    SLOT_START_H = {10, 15, 19};
    private static final int[]    SLOT_START_M = { 0,  0, 30};
    private static final int[]    SLOT_END_H   = {14, 19, 23};
    private static final int[]    SLOT_END_M   = { 0,  0,  0};

    // --- CÁC ĐỐI TƯỢNG TRUY XUẤT DỮ LIỆU (DAO) ---
    private final Ban_DAO            banDAO  = new Ban_DAO();
    private final KhachHang_DAO      khDAO   = new KhachHang_DAO();
    private final DonDatBan_DAO      ddbDAO  = new DonDatBan_DAO();
    private final HoaDon_DAO         hdDAO   = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO  cthdDAO = new ChiTietHoaDon_DAO();
    private final SanPham_DAO        spDAO   = new SanPham_DAO();

    // --- BIẾN TRẠNG THÁI HỆ THỐNG ---
    private final NhanVien currentNV;
    private Ban    currentBan;
    private String currentFilter       = "SANG";
    private Date   selectedBookingDate;
    private final List<Date> bookingDates = new ArrayList<>();

    private JTextField txtSearchBan;
    private JComboBox<String> cbNgayDat;

    // --- THÀNH PHẦN GIAO DIỆN (COMPONENTS) ---
    private JPanel      pThuongGrid, pVIPGrid;
    private JPanel      pThuongContent, pVIPContent;
    private JButton[]   filterBtns;
    private JPanel      rightPanel;
    private CardLayout  rightCard;

    // Widgets cho trạng thái "Đã đặt" (Reserved)
    private JLabel            lblResTitle, lblResKhach, lblResSdt, lblResGhiChu, lblResKhung, lblResTotal;
    private DefaultTableModel tmResDish;

    // Widgets cho trạng thái "Đang dùng" (Using)
    private JLabel            lblUseTitle, lblUseKhach, lblUseTong, lblUseCoc, lblUseConLai;
    private DefaultTableModel tmUseDish;

    /**
     * Khởi tạo giao diện Quản lý đặt bàn.
     * @param nhanVien Nhân viên đang đăng nhập hệ thống.
     */
    public QuanLyDatBan(NhanVien nhanVien) {
        this.currentNV = nhanVien;
        selectedBookingDate = truncateToDay(new Date());
        autoSelectSlot();
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);
        add(buildNorthBar(), BorderLayout.NORTH);
        add(buildCenter(),   BorderLayout.CENTER);
    }

    public void refreshData() { loadTableCards(); showEmpty(); }

    private void autoSelectSlot() {
        if (isToday(selectedBookingDate)) {
            for (int i = 0; i < SLOT_KEYS.length; i++) {
                if (!isSlotPastNow(i)) { currentFilter = SLOT_KEYS[i]; return; }
            }
        }
        currentFilter = SLOT_KEYS[0];
    }

    // ── NORTH bar ────────────────────────────────────────────────────────
    private JPanel buildNorthBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAIN_BLUE);
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(new EmptyBorder(0, 18, 0, 12));

        // --- BÊN TRÁI (WEST): Tiêu đề & Ô tìm kiếm ---
        // Dùng GridBagLayout để tự động căn giữa dọc
        JPanel pnlWest = new JPanel(new GridBagLayout());
        pnlWest.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ ĐẶT BÀN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(GOLD_COLOR);
        title.setBorder(new EmptyBorder(0, 0, 0, 20)); // Tạo khoảng cách 20px bên phải chữ để cách ô search ra
        pnlWest.add(title);

        // Tạo ô tìm kiếm
        txtSearchBan = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(160, 160, 160)); // Màu xám mờ
                    g2.setFont(getFont().deriveFont(Font.ITALIC)); // Chữ in nghiêng

                    // Canh tọa độ để vẽ chữ lọt thỏm vào giữa ô
                    int baseline = getBaseline(getWidth(), getHeight());
                    g2.drawString("Vui lòng nhập số điện thoại...", getInsets().left, baseline);
                    g2.dispose();
                }
            }
        };

        // Giữ nguyên các thông số cũ để không bị vỡ giao diện
        txtSearchBan.setPreferredSize(new Dimension(300, 30));
        txtSearchBan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchBan.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1), new EmptyBorder(3, 8, 3, 8)));
        txtSearchBan.setToolTipText("Nhập SĐT khách hàng và nhấn Enter...");

        // SỰ KIỆN MỚI: Bấm Enter để tìm đơn đặt bàn theo SĐT
        txtSearchBan.addActionListener(e -> {
            String phone = txtSearchBan.getText().trim();
            if (!phone.isEmpty()) {
                searchBookingByPhone(phone); // Gọi hàm tìm kiếm mới
            }
        });

        // Add JLabel (icon search nhỏ) và TextField vào panel
        JPanel pnlSearch = new JPanel(new BorderLayout(6, 0));
        pnlSearch.setOpaque(false);
        JLabel lblIconSearch = new JLabel("🔍"); // Icon kính lúp
        lblIconSearch.setForeground(Color.WHITE);
        pnlSearch.add(lblIconSearch, BorderLayout.WEST);
        pnlSearch.add(txtSearchBan, BorderLayout.CENTER);

        pnlWest.add(pnlSearch);
        bar.add(pnlWest, BorderLayout.WEST);

        // --- BÊN PHẢI (EAST): Cụm thông tin Ngày tháng & Chú thích ---
        // 1. Dùng GridBagLayout Wrapper để kéo mọi thứ xuống chính giữa theo chiều dọc
        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);

        // 2. Các phần tử bên trong vẫn xếp hàng ngang (FlowLayout)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        String todayStr = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        JLabel lblToday = new JLabel("Hôm nay: " + todayStr);
        lblToday.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblToday.setForeground(new Color(200, 220, 240));
        right.add(lblToday);
        right.add(vSep());

        JLabel lblPicker = new JLabel("Ngày đặt:");
        lblPicker.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPicker.setForeground(GOLD_COLOR);
        right.add(lblPicker);

        JComboBox<String> dateCombo = buildDateCombo();
        dateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateCombo.setPreferredSize(new Dimension(185, 26));
        right.add(dateCombo);

        // 3. Đưa vào bar
        rightWrapper.add(right);
        bar.add(rightWrapper, BorderLayout.EAST);

        return bar;
    }

    private JLabel vSep() {
        JLabel l = new JLabel("  |  ");
        l.setForeground(new Color(150, 180, 210));
        return l;
    }

    private JComboBox<String> buildDateCombo() {
        bookingDates.clear();
        cbNgayDat = new JComboBox<>(); // Dùng biến toàn cục
        JComboBox<String> combo = cbNgayDat;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy (EEE)", new Locale("vi", "VN"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i <= 14; i++) {
            bookingDates.add(cal.getTime());
            combo.addItem(i == 0 ? "Hôm nay - " + sdf.format(cal.getTime()) : sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        selectedBookingDate = bookingDates.get(0);
        combo.setSelectedIndex(0);
        combo.addActionListener(e -> {
            int idx = combo.getSelectedIndex();
            if (idx >= 0 && idx < bookingDates.size()) {
                selectedBookingDate = bookingDates.get(idx);
                autoSelectSlot();
                refreshFilterBtns();
                loadTableCards();
                showEmpty();
            }
        });
        return combo;
    }

    private JLabel chip(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(c);
        return l;
    }

    // ── CENTER split ─────────────────────────────────────────────────────
    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightPanel());
        split.setBorder(null);
        split.setDividerSize(4);
        split.setBackground(BG_LIGHT);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.56));
        return split;
    }

    // ── LEFT: filter + accordions ────────────────────────────────────────
    private JScrollPane buildLeftPanel() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(BG_LIGHT);
        wrap.setBorder(new EmptyBorder(10, 10, 10, 6));

        // 1. Tạo container chính cho hàng filter bằng BorderLayout
        JPanel filterContainer = new JPanel(new BorderLayout());
        filterContainer.setOpaque(false);
        filterContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // 2. Panel bên trái: Chứa Label và các nút (Dùng FlowLayout)
        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        filterLeft.setOpaque(false);

        JLabel lblF = new JLabel("Khung giờ:");
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblF.setForeground(TEXT_DARK);
        filterLeft.add(lblF);

        filterBtns = new JButton[SLOT_KEYS.length];
        for (int i = 0; i < SLOT_KEYS.length; i++) {
            JButton b = new JButton(SLOT_LABELS[i]);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            filterBtns[i] = b;
            final String key = SLOT_KEYS[i];
            b.addActionListener(e -> {
                currentFilter = key;
                refreshFilterBtns();
                loadTableCards();
                showEmpty();
            });
            filterLeft.add(b);
        }

        // 3. Panel bên phải: Các chip trạng thái
        JPanel filterCol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        filterCol.setOpaque(false);
        filterCol.add(chip("● Trống",     GREEN_TRONG));
        filterCol.add(chip("● Đã đặt",    AMBER_DAT));
        filterCol.add(chip("● Đang dùng", RED_DANG));

        // 4. Ráp hai phần vào container chính
        filterContainer.add(filterLeft, BorderLayout.WEST); // Đẩy sang trái
        filterContainer.add(filterCol, BorderLayout.EAST);   // Đẩy sang phải

        refreshFilterBtns();

        wrap.add(filterContainer);
        wrap.add(Box.createVerticalStrut(8));

        pThuongContent = new JPanel();
        pThuongContent.setLayout(new BoxLayout(pThuongContent, BoxLayout.Y_AXIS));
        pThuongContent.setOpaque(false);
        pThuongGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 8));
        pThuongGrid.setOpaque(false);
        pThuongContent.add(pThuongGrid);
        wrap.add(accordion("BÀN THƯỜNG", pThuongContent));
        wrap.add(Box.createVerticalStrut(6));

        pVIPContent = new JPanel();
        pVIPContent.setLayout(new BoxLayout(pVIPContent, BoxLayout.Y_AXIS));
        pVIPContent.setOpaque(false);
        pVIPGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 8));
        pVIPGrid.setOpaque(false);
        pVIPContent.add(pVIPGrid);
        wrap.add(accordion("BÀN VIP", pVIPContent));
        wrap.add(Box.createVerticalGlue());

        loadTableCards();

        JScrollPane sc = new JScrollPane(wrap);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.setBackground(BG_LIGHT);
        return sc;
    }

    private void refreshFilterBtns() {
        if (filterBtns == null) return;
        boolean todaySelected = isToday(selectedBookingDate);
        for (int i = 0; i < filterBtns.length; i++) {
            boolean active   = SLOT_KEYS[i].equals(currentFilter);
            boolean pastSlot = todaySelected && isSlotPastNow(i);
            filterBtns[i].setEnabled(!pastSlot);
            filterBtns[i].setBackground(pastSlot ? new Color(210, 210, 210)
                                                  : (active ? MAIN_BLUE : Color.WHITE));
            filterBtns[i].setForeground(pastSlot ? new Color(140, 140, 140)
                                                  : (active ? Color.WHITE : TEXT_DARK));
            filterBtns[i].setBorder(active
                    ? new EmptyBorder(4, 12, 4, 12)
                    : new LineBorder(pastSlot ? new Color(210, 210, 210) : BORDER_CLR, 1));
        }
    }

    private JPanel accordion(String title, JPanel content) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JButton hdr = new JButton("▼   " + title);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hdr.setForeground(Color.WHITE);
        hdr.setBackground(MAIN_BLUE);
        hdr.setFocusPainted(false);
        hdr.setBorder(new EmptyBorder(7, 12, 7, 12));
        hdr.setHorizontalAlignment(SwingConstants.LEFT);
        hdr.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hdr.addActionListener(e -> {
            boolean vis = !content.isVisible();
            content.setVisible(vis);
            hdr.setText((vis ? "▼   " : "☰   ") + title);
            wrap.revalidate();
        });
        wrap.add(hdr, BorderLayout.NORTH);
        wrap.add(content, BorderLayout.CENTER);
        return wrap;
    }

    // ── table cards ───────────────────────────────────────────────────────
    void loadTableCards() {
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return new Object[]{ banDAO.getAllBan(), ddbDAO.getAllDonDatBan() };
            }
            @SuppressWarnings("unchecked")
            @Override protected void done() {
                try {
                    Object[] r = get();
                    List<Ban>       dsBan   = (List<Ban>) r[0];
                    List<DonDatBan> allDons = (List<DonDatBan>) r[1];
                    pThuongGrid.removeAll(); pVIPGrid.removeAll();
                    for (Ban ban : dsBan) {
                        TrangThaiBan status = computeEffectiveStatus(ban, allDons);
                        JPanel card = makeTableCard(ban, status);
                        String loai = ban.getLoaiBan() != null ? ban.getLoaiBan().trim() : "";
                        if (loai.equalsIgnoreCase("VIP")) pVIPGrid.add(card);
                        else                              pThuongGrid.add(card);
                    }
                    pThuongGrid.revalidate(); pThuongGrid.repaint();
                    pVIPGrid.revalidate();    pVIPGrid.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private TrangThaiBan computeEffectiveStatus(Ban ban, List<DonDatBan> allDons) {
        for (DonDatBan d : allDons) {
            if (!d.isTrangThai()
                    && d.getBan() != null
                    && d.getBan().getMaBan().equals(ban.getMaBan())
                    && isSameDay(d.getThoiGianDen(), selectedBookingDate)
                    && currentFilter.equals(d.getKhungGio())) {
                int idx = getSlotIndex(currentFilter);
                if (isToday(selectedBookingDate) && isSlotActive(idx)
                        && ban.getTinhTrangBan() == TrangThaiBan.DangDuocSuDung) {
                    return TrangThaiBan.DangDuocSuDung;
                }
                return TrangThaiBan.DaDuocDat;
            }
        }
        return TrangThaiBan.Trong;
    }

    private DonDatBan findActiveDon(String maBan) {
        return findActiveDonFromList(maBan, ddbDAO.getAllDonDatBan());
    }

    private DonDatBan findActiveDonFromList(String maBan, List<DonDatBan> allDons) {
        for (DonDatBan d : allDons) {
            if (!d.isTrangThai()
                    && d.getBan() != null
                    && d.getBan().getMaBan().equals(maBan)
                    && isSameDay(d.getThoiGianDen(), selectedBookingDate)
                    && currentFilter.equals(d.getKhungGio())) {
                return d;
            }
        }
        return null;
    }

    private JPanel makeTableCard(Ban ban, TrangThaiBan trang) {
        Color bg = trang == TrangThaiBan.DaDuocDat      ? AMBER_DAT
                 : trang == TrangThaiBan.DangDuocSuDung ? RED_DANG
                 : GREEN_TRONG;
        RoundedPanel card = new RoundedPanel(14, bg);
        card.setPreferredSize(new Dimension(110, 88));
        card.setLayout(new BorderLayout(0, 0));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // main label: "Bàn X" large
        JLabel numLbl = new JLabel("Bàn " + ban.getSoBan(), SwingConstants.CENTER);
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        numLbl.setForeground(Color.WHITE);
        numLbl.setBorder(new EmptyBorder(10, 4, 0, 4));
        card.add(numLbl, BorderLayout.CENTER);

        JPanel bot = new JPanel(new GridLayout(2, 1, 0, 2));
        bot.setOpaque(false);
        // capacity: prominent, white
        JLabel sucLbl = new JLabel(ban.getSucChua() + " người", SwingConstants.CENTER);
        sucLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sucLbl.setForeground(Color.WHITE);
        // status chip
        String statusText = trang == TrangThaiBan.DaDuocDat ? "Đã đặt"
                : trang == TrangThaiBan.DangDuocSuDung ? "Đang dùng" : "Trống";
        JLabel statLbl = new JLabel(statusText, SwingConstants.CENTER);
        statLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        statLbl.setForeground(new Color(255, 255, 255, 200));
        bot.add(sucLbl); bot.add(statLbl);
        bot.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(bot, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onCardClick(ban); }
            @Override public void mouseEntered(MouseEvent e) { card.setBorderHighlight(true);  card.repaint(); }
            @Override public void mouseExited (MouseEvent e) { card.setBorderHighlight(false); card.repaint(); }
        });
        return card;
    }

    private void onCardClick(Ban ban) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                Ban fresh = banDAO.getBanByMa(ban.getMaBan());
                Ban b = (fresh != null) ? fresh : ban;
                List<DonDatBan> allDons = ddbDAO.getAllDonDatBan();
                TrangThaiBan t = computeEffectiveStatus(b, allDons);
                if (t == TrangThaiBan.DaDuocDat || t == TrangThaiBan.DangDuocSuDung) {
                    DonDatBan don = findActiveDonFromList(b.getMaBan(), allDons);
                    HoaDon hd = (don != null) ? hdDAO.getHoaDonByMaDon(don.getMaDon()) : null;
                    KhachHang fullKH = null;
                    List<ChiTietHoaDon> cths = new ArrayList<>();
                    if (hd != null) {
                        if (hd.getKhachHang() != null)
                            fullKH = khDAO.getKhachHangByMa(hd.getKhachHang().getMaKH());
                        cths = cthdDAO.getChiTietByMaHD(hd.getMaHD());
                    }
                    return new Object[]{b, t, don, hd, fullKH, cths};
                }
                return new Object[]{b, t, null, null, null, new ArrayList<>()};
            }
            @SuppressWarnings("unchecked")
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Object[] r = get();
                    currentBan = (Ban) r[0];
                    TrangThaiBan t = (TrangThaiBan) r[1];
                    if (t == TrangThaiBan.DaDuocDat)
                        renderReserved(currentBan, (DonDatBan)r[2], (HoaDon)r[3], (KhachHang)r[4], (List<ChiTietHoaDon>)r[5]);
                    else if (t == TrangThaiBan.DangDuocSuDung)
                        renderUsing(currentBan, (DonDatBan)r[2], (HoaDon)r[3], (KhachHang)r[4], (List<ChiTietHoaDon>)r[5]);
                    else showBooking(currentBan);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ── RIGHT panel ──────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        rightCard  = new CardLayout();
        rightPanel = new JPanel(rightCard);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(buildEmpty(),    "empty");
        rightPanel.add(buildReserved(), "reserved");
        rightPanel.add(buildUsing(),    "using");
        rightCard.show(rightPanel, "empty");
        return rightPanel;
    }

    // ── empty card ───────────────────────────────────────────────────────
    private JPanel buildEmpty() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel("← Chọn bàn để xem chi tiết");
        l.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        l.setForeground(new Color(180, 180, 180));
        p.add(l);
        return p;
    }

    // ── reserved card ────────────────────────────────────────────────────
    private JPanel buildReserved() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(255, 248, 240));
        hdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(255, 200, 150)),
                new EmptyBorder(10, 14, 10, 14)));
        lblResTitle = new JLabel("ĐÃ ĐẶT");
        lblResTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblResTitle.setForeground(AMBER_DAT);
        hdr.add(lblResTitle, BorderLayout.WEST);
        lblResKhung = new JLabel();
        lblResKhung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResKhung.setForeground(new Color(150, 100, 0));
        hdr.add(lblResKhung, BorderLayout.EAST);
        root.add(hdr, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(10, 14, 6, 14));

        JPanel info = new JPanel(new GridLayout(3, 2, 8, 6));
        info.setOpaque(false);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        info.add(infoKey("Khách hàng:"));    info.add(lblResKhach  = infoVal(""));
        info.add(infoKey("Số điện thoại:")); info.add(lblResSdt    = infoVal(""));
        info.add(infoKey("Ghi chú:"));       info.add(lblResGhiChu = infoVal(""));
        body.add(info);
        body.add(Box.createVerticalStrut(8));
        body.add(hsep());

        body.add(sectionLabel("DANH SÁCH MÓN ĐÃ GỌI"));
        // Cập nhật lại 5 cột cho chuẩn
        tmResDish = new DefaultTableModel(new String[]{"STT", "Tên món", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Ghi đè để bảng tự dãn chiều cao
        JTable tRes = new JTable(tmResDish) {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }
        };

        styleTable(tRes);
        centerCol(tRes, 0); centerCol(tRes, 2);
        tRes.getColumnModel().getColumn(0).setMaxWidth(42);
        tRes.getColumnModel().getColumn(2).setMaxWidth(44);

        // Ghi đè để JScrollPane không bị ép thu nhỏ, cho phép bung lụa
        JScrollPane sc = new JScrollPane(tRes) {
            @Override
            public Dimension getMaximumSize() {
                // Rộng tối đa, cao vừa đủ theo preferred size của nó
                return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        sc.setBorder(new LineBorder(BORDER_CLR));
        // ĐÃ XÓA: sc.setMaximumSize(...) cũ đi
        body.add(sc);

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        totalRow.setBorder(new EmptyBorder(4, 0, 0, 0));
        lblResTotal = new JLabel("Tổng: 0đ");
        lblResTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResTotal.setForeground(TEXT_DARK);
        totalRow.add(new JLabel(), BorderLayout.WEST);
        totalRow.add(lblResTotal, BorderLayout.EAST);
        body.add(totalRow);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        root.add(bodyScroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 3, 8, 0));
        btnRow.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(10, 14, 10, 14)));
        btnRow.setBackground(Color.WHITE);
        JButton bHuy    = actionBtn("HỦY ĐẶT",    new Color(255, 236, 236), RED_DANG,  true);
        JButton bThem   = actionBtn("+ THÊM MÓN",  new Color(232, 242, 255), MAIN_BLUE, true);
        JButton bCheckin = actionBtn("CHECK-IN",   MAIN_BLUE,               Color.WHITE, false);
        bHuy.addActionListener(e    -> doCancelBooking());
        bThem.addActionListener(e   -> doAddDish(false));
        bCheckin.addActionListener(e -> doCheckIn());
        btnRow.add(bHuy); btnRow.add(bThem); btnRow.add(bCheckin);
        root.add(btnRow, BorderLayout.SOUTH);
        return root;
    }

    // ── using card ───────────────────────────────────────────────────────
    private JPanel buildUsing() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(255, 243, 243));
        hdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(255, 180, 180)),
                new EmptyBorder(10, 14, 10, 14)));
        lblUseTitle = new JLabel("ĐANG PHỤC VỤ");
        lblUseTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblUseTitle.setForeground(RED_DANG);
        hdr.add(lblUseTitle, BorderLayout.WEST);
        lblUseKhach = new JLabel();
        lblUseKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUseKhach.setForeground(new Color(140, 50, 50));
        hdr.add(lblUseKhach, BorderLayout.EAST);
        root.add(hdr, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(10, 14, 6, 14));

        body.add(sectionLabel("ĐƠN GỌI MÓN"));
        tmUseDish = new DefaultTableModel(new String[]{"STT", "Tên món", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Ghi đè để bảng tự dãn chiều cao
        JTable tUse = new JTable(tmUseDish) {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }
        };

        styleTable(tUse);
        centerCol(tUse, 0); centerCol(tUse, 2);
        tUse.getColumnModel().getColumn(0).setMaxWidth(42);
        tUse.getColumnModel().getColumn(2).setMaxWidth(44);

        // Ghi đè để JScrollPane không bị ép thu nhỏ
        JScrollPane sc = new JScrollPane(tUse) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        sc.setBorder(new LineBorder(BORDER_CLR));
        // ĐÃ XÓA: sc.setMaximumSize(...) cũ đi
        body.add(sc);

        JPanel sumOuter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        sumOuter.setOpaque(false);
        sumOuter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        JPanel sum = new JPanel(new GridLayout(3, 2, 14, 5));
        sum.setOpaque(false);
        sum.add(infoKey("Tổng tiền món:")); sum.add(lblUseTong   = infoVal("0đ"));
        sum.add(infoKey("Đã cọc:"));       sum.add(lblUseCoc    = infoVal("−" + FMT.format(TIEN_COC) + "đ"));
        sum.add(infoKey("Còn lại:"));      sum.add(lblUseConLai = infoVal("0đ"));
        lblUseConLai.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUseConLai.setForeground(RED_DANG);
        sumOuter.add(sum);
        body.add(sumOuter);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        root.add(bodyScroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(10, 14, 10, 14)));
        btnRow.setBackground(Color.WHITE);
        JButton bThem     = actionBtn("+ THÊM MÓN", new Color(232, 242, 255), MAIN_BLUE,  true);
        JButton bCheckout = actionBtn("THANH TOÁN",  GREEN_TRONG,             Color.WHITE, false);
        bThem.addActionListener(e     -> doAddDish(true));
        bCheckout.addActionListener(e -> doCheckout());
        btnRow.add(bThem); btnRow.add(bCheckout);
        root.add(btnRow, BorderLayout.SOUTH);
        return root;
    }

    // ── show cards ───────────────────────────────────────────────────────
    private void showEmpty() {
        currentBan = null;
        rightCard.show(rightPanel, "empty");
    }

    private void showBooking(Ban ban) {
        QuanLyDatBan_DB bookingPanel = new QuanLyDatBan_DB(
                currentNV,
                ban,
                selectedBookingDate,
                currentFilter,
                new QuanLyDatBan_DB.BookingListener() {
                    @Override
                    public void onBookingSuccess() {
                        loadTableCards(); // Cập nhật lại màu của bàn
                        showEmpty();      // Ẩn panel đi (hiện panel trống)
                    }

                    @Override
                    public void onCancel() {
                        showEmpty();
                    }
                }
        );
        Component[] comps = rightPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof QuanLyDatBan_DB) {
                rightPanel.remove(c);
            }
        }

        // Add panel mới vào CardLayout
        rightPanel.add(bookingPanel, "booking");
        rightCard.show(rightPanel, "booking");
    }

    private void renderReserved(Ban ban, DonDatBan don, HoaDon hd, KhachHang fullKH, List<ChiTietHoaDon> cths) {
        lblResTitle.setText("ĐÃ ĐẶT  —  BÀN " + ban.getSoBan());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        lblResKhung.setText(sdf.format(selectedBookingDate) + "  ·  " + getSlotLabel(currentFilter));

        lblResKhach.setText(fullKH != null ? fullKH.getTenKH() : "—");
        lblResSdt.setText(fullKH != null ? fullKH.getSoDT() : "—");
        lblResGhiChu.setText(don != null && don.getGhiChu() != null && !don.getGhiChu().isEmpty()
                ? don.getGhiChu() : "");

        tmResDish.setRowCount(0);
        double total = 0;
        int stt = 1;

        for (ChiTietHoaDon ct : cths) {
            // ĐÂY LÀ CHỖ QUYẾT ĐỊNH: ÉP NHÂN SỐ LƯỢNG NGAY TẠI LÚC VẼ GIAO DIỆN
            double donGia = ct.getDonGia();
            double thanhTienDung = donGia * ct.getSoLuong();

            tmResDish.addRow(new Object[]{
                    stt++,
                    ct.getMonAn().getTenMon(),
                    ct.getSoLuong(),
                    FMT.format(donGia) + "đ",          // In ra cột Đơn giá
                    FMT.format(thanhTienDung) + "đ"    // In ra cột Thành tiền (Đã nhân)
            });
            total += thanhTienDung; // Cộng tổng bằng số tiền đã nhân
        }
        lblResTotal.setText("Tổng: " + FMT.format(total) + "đ");
        rightCard.show(rightPanel, "reserved");
    }

    private void renderUsing(Ban ban, DonDatBan don, HoaDon hd, KhachHang fullKH, List<ChiTietHoaDon> cths) {
        lblUseTitle.setText("ĐANG PHỤC VỤ  —  BÀN " + ban.getSoBan());
        lblUseKhach.setText(fullKH != null ? "Khách: " + fullKH.getTenKH() : "");

        tmUseDish.setRowCount(0);
        double total = 0;
        int stt = 1;
        for (ChiTietHoaDon ct : cths) {
            // ÉP TÍNH LẠI THÀNH TIỀN: Đơn giá * Số lượng
            double thanhTienDung = ct.getDonGia() * ct.getSoLuong();

            tmUseDish.addRow(new Object[]{
                    stt++,
                    ct.getMonAn().getTenMon(),
                    ct.getSoLuong(),
                    FMT.format(ct.getDonGia()) + "đ",
                    FMT.format(thanhTienDung) + "đ"
            });
            total += thanhTienDung; // Cộng tổng bằng số đã nhân
        }

        double coc    = (hd != null) ? hd.getTienCoc() : TIEN_COC;
        double conLai = total - coc;

        lblUseTong.setText(FMT.format(total) + "đ");
        lblUseCoc.setText("−" + FMT.format(coc) + "đ");
        lblUseConLai.setText(FMT.format(Math.max(0, conLai)) + "đ");
        rightCard.show(rightPanel, "using");
    }

    // ── business logic ───────────────────────────────────────────────────
    private void doCancelBooking() {
        if (currentBan == null) return;
        int r = JOptionPane.showConfirmDialog(this,
            "Xác nhận hủy đặt bàn số " + currentBan.getSoBan() + "?",
            "Hủy đặt bàn", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        DonDatBan don = findActiveDon(currentBan.getMaBan());
        if (don != null) {
            HoaDon hd = hdDAO.getHoaDonByMaDon(don.getMaDon());
            if (hd != null) { cthdDAO.deleteByMaHD(hd.getMaHD()); hdDAO.deleteHoaDon(hd.getMaHD()); }
            ddbDAO.deleteDonDatBan(don.getMaDon());
        }
        if (isToday(selectedBookingDate)) {
            banDAO.updateTinhTrangBan(currentBan.getMaBan(), TrangThaiBan.Trong);
        }
        loadTableCards(); showEmpty();
    }

    private void doCheckIn() {
        if (currentBan == null) return;

        // date must have arrived
        if (truncateToDay(selectedBookingDate).after(truncateToDay(new Date()))) {
            msg("Chưa tới ngày đặt bàn (" + new SimpleDateFormat("dd/MM/yyyy").format(selectedBookingDate)
                    + "), không thể check-in!");
            return;
        }
        // slot must have started (applies only when booking date is today)
        if (isToday(selectedBookingDate)) {
            int idx = getSlotIndex(currentFilter);
            Calendar now = Calendar.getInstance();
            int h = now.get(Calendar.HOUR_OF_DAY);
            int m = now.get(Calendar.MINUTE);
            boolean started = h > SLOT_START_H[idx]
                    || (h == SLOT_START_H[idx] && m >= SLOT_START_M[idx]);
            if (!started) {
                msg("Khung giờ " + getSlotLabel(currentFilter) + " chưa bắt đầu.\n"
                    + "Check-in từ " + String.format("%02d:%02d", SLOT_START_H[idx], SLOT_START_M[idx]) + " trở đi.");
                return;
            }
        }

        // must have at least 1 dish ordered
        DonDatBan don = findActiveDon(currentBan.getMaBan());
        if (don == null) { msg("Không tìm thấy đơn đặt bàn!"); return; }
        HoaDon hd = hdDAO.getHoaDonByMaDon(don.getMaDon());
        if (hd == null) { msg("Không tìm thấy hóa đơn!"); return; }
        List<ChiTietHoaDon> cths = cthdDAO.getChiTietByMaHD(hd.getMaHD());
        if (cths == null || cths.isEmpty()) {
            msg("Vui lòng gọi ít nhất 1 món trước khi check-in!");
            return;
        }

        banDAO.updateTinhTrangBan(currentBan.getMaBan(), TrangThaiBan.DangDuocSuDung);
        loadTableCards();
        onCardClick(currentBan);
    }

    private void doAddDish(boolean fromUsing) {
        if (currentBan == null) return;
        DonDatBan don = findActiveDon(currentBan.getMaBan());
        HoaDon hd = (don != null) ? hdDAO.getHoaDonByMaDon(don.getMaDon()) : null;
        if (hd == null) { msg("Không tìm thấy hóa đơn!"); return; }
        new QuanLyDatBan_GM(
            (Frame) SwingUtilities.getWindowAncestor(this),
            hd, spDAO, cthdDAO, hdDAO,
            () -> onCardClick(currentBan)
        ).setVisible(true);
    }

    private void doCheckout() {
        if (currentBan == null) return;
        DonDatBan don = findActiveDon(currentBan.getMaBan());
        HoaDon hd = (don != null) ? hdDAO.getHoaDonByMaDon(don.getMaDon()) : null;
        if (hd == null) { msg("Không tìm thấy hóa đơn!"); return; }

        List<ChiTietHoaDon> cths = cthdDAO.getChiTietByMaHD(hd.getMaHD());
        double tongMon = 0;
        for (ChiTietHoaDon ct : cths) tongMon += ct.getThanhTien();
        double coc    = hd.getTienCoc();
        double conLai = Math.max(0, tongMon - coc);

        int r = JOptionPane.showConfirmDialog(this,
            String.format("Tổng tiền món: %sđ%nĐã cọc trước: %sđ%nKhách trả thêm: %sđ%n%nXác nhận thanh toán?",
                FMT.format(tongMon), FMT.format(coc), FMT.format(conLai)),
            "Thanh toán", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        hdDAO.updateTongTien(hd.getMaHD(), tongMon);
        hdDAO.updateStatus(hd.getMaHD(), true);
        if (don != null) { don.setTrangThai(true); ddbDAO.updateDonDatBan(don); }
        banDAO.updateTinhTrangBan(currentBan.getMaBan(), TrangThaiBan.Trong);
        JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        loadTableCards(); showEmpty();
    }

    // ── time-slot helpers ─────────────────────────────────────────────────
    private static boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = Calendar.getInstance(); c1.setTime(d1);
        Calendar c2 = Calendar.getInstance(); c2.setTime(d2);
        return c1.get(Calendar.YEAR)        == c2.get(Calendar.YEAR)
            && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isToday(Date d) { return isSameDay(d, new Date()); }

    private static Date truncateToDay(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);      c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private boolean isSlotPastNow(int idx) {
        if (!isToday(selectedBookingDate)) return false;
        Calendar now = Calendar.getInstance();
        int h = now.get(Calendar.HOUR_OF_DAY);
        int m = now.get(Calendar.MINUTE);
        return h > SLOT_END_H[idx] || (h == SLOT_END_H[idx] && m >= SLOT_END_M[idx]);
    }

    private boolean isSlotActive(int idx) {
        if (!isToday(selectedBookingDate)) return false;
        Calendar now = Calendar.getInstance();
        int h = now.get(Calendar.HOUR_OF_DAY);
        int m = now.get(Calendar.MINUTE);
        boolean started  = h > SLOT_START_H[idx] || (h == SLOT_START_H[idx] && m >= SLOT_START_M[idx]);
        boolean notEnded = h < SLOT_END_H[idx]   || (h == SLOT_END_H[idx]   && m <  SLOT_END_M[idx]);
        return started && notEnded;
    }

    private int getSlotIndex(String key) {
        for (int i = 0; i < SLOT_KEYS.length; i++) if (SLOT_KEYS[i].equals(key)) return i;
        return 0;
    }

    private String getSlotLabel(String key) { return SLOT_LABELS[getSlotIndex(key)]; }

    // ── PreorderDishDialog ────────────────────────────────────────────────
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

            tmCart = new DefaultTableModel(new String[]{"Tên món", "SL", "Thành tiền"}, 0) {
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
                double tt = sp.getGiaBan() * e.getValue();
                tmCart.addRow(new Object[]{sp.getTenMon(), e.getValue(), FMT.format(tt) + "đ"});
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

    // ── AddDishDialog ────────────────────────────────────────────────────
    private static class AddDishDialog extends JDialog {
        private final HoaDon hd;
        private final SanPham_DAO spDAO;
        private final ChiTietHoaDon_DAO cthdDAO;
        private final HoaDon_DAO hdDAO;
        private final Runnable onDone;
        private final Map<String,Integer> cart = new LinkedHashMap<>();
        private DefaultTableModel tmCart;
        private JLabel lblTotal;

        AddDishDialog(Frame owner, HoaDon hd, SanPham_DAO spDAO,
                      ChiTietHoaDon_DAO cthdDAO, HoaDon_DAO hdDAO, Runnable onDone) {
            super(owner, "Thêm món vào hóa đơn", true);
            this.hd = hd; this.spDAO = spDAO; this.cthdDAO = cthdDAO;
            this.hdDAO = hdDAO; this.onDone = onDone;
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

    // ── UI helpers ───────────────────────────────────────────────────────
    private JButton qtyBtn(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(26, 26));
        b.setMargin(new Insets(0, 0, 0, 0)); b.setFocusPainted(false);
        b.setBackground(BG_LIGHT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

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
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1), new EmptyBorder(3, 8, 3, 8)));
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

    private JLabel infoKey(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(100, 110, 130));
        return l;
    }

    private JLabel infoVal(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(245, 247, 250));
        t.setRowHeight(26); t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setSelectionBackground(new Color(220, 235, 255));
    }

    private void centerCol(JTable t, int col) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        t.getColumnModel().getColumn(col).setCellRenderer(r);
    }

    private void msg(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    // ── WrapLayout ───────────────────────────────────────────────────────
    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container t) { return layout(t, true); }
        @Override public Dimension minimumLayoutSize(Container t) {
            Dimension d = layout(t, false); d.width -= getHgap() + 1; return d;
        }

        private Dimension layout(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int tw = target.getWidth();
                if (tw == 0) tw = Integer.MAX_VALUE;
                Insets ins = target.getInsets();
                int max = tw - (ins.left + ins.right + getHgap() * 2);
                Dimension dim = new Dimension(0, 0);
                int rw = 0, rh = 0;
                for (Component m : target.getComponents()) {
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rw + d.width > max) {
                        dim.width = Math.max(dim.width, rw);
                        dim.height += rh + getVgap();
                        rw = 0; rh = 0;
                    }
                    rw += d.width + getHgap(); rh = Math.max(rh, d.height);
                }
                dim.width = Math.max(dim.width, rw);
                dim.height += rh + ins.top + ins.bottom + getVgap() * 2;
                return dim;
            }
        }
    }

    private void searchBookingByPhone(String phone) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<DonDatBan, Void>() {
            @Override
            protected DonDatBan doInBackground() throws Exception {
                KhachHang kh = khDAO.getKhachHangBySdt(phone);
                if (kh == null) return null;

                List<DonDatBan> allDons = ddbDAO.getAllDonDatBan();
                Date today = truncateToDay(new Date());

                // Quét toàn bộ đơn đặt bàn của khách này
                for (DonDatBan d : allDons) {
                    if (!d.isTrangThai()
                            && d.getKhachHang() != null
                            && d.getKhachHang().getMaKH().equals(kh.getMaKH())) {

                        // Nếu ngày đến >= ngày hôm nay (nghĩa là đơn sắp tới) thì lụm luôn!
                        Date ngayDen = truncateToDay(d.getThoiGianDen());
                        if (!ngayDen.before(today)) {
                            return d;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    DonDatBan foundDon = get();

                    if (foundDon != null) {
                        // 1. Lấy thông tin ngày và khung giờ của cái đơn vừa tìm được
                        Date ngayKhachDat = foundDon.getThoiGianDen();
                        String khungGioKhachDat = foundDon.getKhungGio();

                        // 2. DỊCH CHUYỂN GIAO DIỆN: Cập nhật biến ngày và đổi ComboBox
                        selectedBookingDate = ngayKhachDat;
                        if (cbNgayDat != null) {
                            for (int i = 0; i < bookingDates.size(); i++) {
                                if (isSameDay(bookingDates.get(i), ngayKhachDat)) {
                                    // Tạm thời tắt sự kiện để combo box không load lại trang gây loạn
                                    ActionListener[] listeners = cbNgayDat.getActionListeners();
                                    for (ActionListener al : listeners) cbNgayDat.removeActionListener(al);

                                    cbNgayDat.setSelectedIndex(i); // Nhảy ngày

                                    for (ActionListener al : listeners) cbNgayDat.addActionListener(al);
                                    break;
                                }
                            }
                        }

                        // 3. DỊCH CHUYỂN GIAO DIỆN: Đổi màu nút Khung giờ SÁNG/CHIỀU/TỐI
                        currentFilter = khungGioKhachDat;
                        refreshFilterBtns();

                        // 4. Load lại sơ đồ bàn của đúng cái ca đó, và tự động bật thông tin bàn
                        loadTableCards();
                        onCardClick(foundDon.getBan());
                        txtSearchBan.setText(""); // Xóa ô search

                        // Báo cho Lễ tân biết đã nhảy đến ca nào
                        JOptionPane.showMessageDialog(QuanLyDatBan.this,
                                "Khách có đơn đặt bàn vào lúc: " + getSlotLabel(khungGioKhachDat)
                                        + "\nNgày: " + new SimpleDateFormat("dd/MM/yyyy").format(ngayKhachDat),
                                "Đã tự động chuyển đến ca đặt", JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        JOptionPane.showMessageDialog(QuanLyDatBan.this,
                                "Số điện thoại " + phone + " không có đơn đặt bàn nào sắp tới!",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
    // ── RoundedPanel ─────────────────────────────────────────────────────
    private static class RoundedPanel extends JPanel {
        private final int   radius;
        private final Color fillColor;
        private boolean     highlight = false;

        RoundedPanel(int radius, Color fill) {
            this.radius = radius; this.fillColor = fill;
            setOpaque(false);
        }

        void setBorderHighlight(boolean h) { this.highlight = h; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (highlight) {
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
