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

/**
 * Lớp QuanLyDatBan: Quản lý sơ đồ bàn ăn, đặt bàn, check-in và thanh toán.
 * <p>
 * Chức năng chính:
 * - Hiển thị sơ đồ bàn theo thời gian (Sáng/Chiều/Tối).
 * - Theo dõi trạng thái bàn (Trống/Đã đặt/Đang dùng).
 * - Tìm kiếm đơn đặt bàn nhanh qua số điện thoại.
 * - Xử lý quy trình: Đặt món -> Check-in -> Thanh toán.
 * </p>
 *
 * @author Le Van Hoa
 * @version 1.0
 */
public class QuanLyDatBan extends JPanel {

    // --- CÁC HẰNG SỐ CẤU HÌNH GIAO DIỆN ---
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

    // ── Chọn nhiều bàn ────────────────────────────────────────────────────
    /** Toggle button: OFF = chọn 1 bàn như cũ / ON = chọn nhiều bàn */
    private JButton btnMultiTableMode;
    /** true khi đang ở chế độ chọn nhiều bàn */
    private boolean multiTableMode = false;
    /** Danh sách bàn đang được chọn trong chế độ multi-table */
    private final List<Ban> selectedTables = new ArrayList<>();
    /** Màu highlight bàn đang được chọn trong multi-table mode */
    private static final Color SELECTED_MULTI = Color.decode("#3498DB");

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
        JPanel filtercen = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        filtercen.setOpaque(false);
        btnMultiTableMode = new JButton("Chọn nhiều bàn: TẮT");
        btnMultiTableMode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMultiTableMode.setPreferredSize(new Dimension(180, 25));
        btnMultiTableMode.setBackground(new Color(100, 100, 110));
        btnMultiTableMode.setForeground(Color.WHITE);
        btnMultiTableMode.setFocusPainted(false);
        btnMultiTableMode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMultiTableMode.addActionListener(e -> toggleMultiTableMode());
        filtercen.add(btnMultiTableMode);

        JPanel filtercen = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        filtercen.setOpaque(false);
        btnMultiTableMode = new JButton("Chọn nhiều bàn: TẮT");
        btnMultiTableMode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMultiTableMode.setPreferredSize(new Dimension(180, 25));
        btnMultiTableMode.setBackground(new Color(100, 100, 110));
        btnMultiTableMode.setForeground(Color.WHITE);
        btnMultiTableMode.setFocusPainted(false);
        btnMultiTableMode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMultiTableMode.addActionListener(e -> toggleMultiTableMode());
        filtercen.add(btnMultiTableMode);

        // 3. Panel bên phải: Các chip trạng thái
        JPanel filterCol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        filterCol.setOpaque(false);
        filterCol.add(chip("● Trống",     GREEN_TRONG));
        filterCol.add(chip("● Đã đặt",    AMBER_DAT));
        filterCol.add(chip("● Đang dùng", RED_DANG));

        // 4. Ráp hai phần vào container chính
        filterContainer.add(filtercen,BorderLayout.CENTER);
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
                // Cần dsBan đầy đủ để computeEffectiveStatus hoạt động đúng
                return new Object[]{ banDAO.getAllBan(), ddbDAO.getAllDonDatBanWithBan() };
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
            // Kiểm tra xem bàn này có trong dsBan của đơn không
            boolean hasBan = d.getDsBan().stream()
                    .anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
            if (!d.isTrangThai()
                    && hasBan
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
        return findActiveDonFromList(maBan, ddbDAO.getAllDonDatBanWithBan());
    }

    private DonDatBan findActiveDonFromList(String maBan, List<DonDatBan> allDons) {
        for (DonDatBan d : allDons) {
            boolean hasBan = d.getDsBan().stream()
                    .anyMatch(b -> b.getMaBan().equals(maBan));
            if (!d.isTrangThai()
                    && hasBan
                    && isSameDay(d.getThoiGianDen(), selectedBookingDate)
                    && currentFilter.equals(d.getKhungGio())) {
                return d;
            }
        }
        return null;
    }

    private JPanel makeTableCard(Ban ban, TrangThaiBan trang) {
        // Màu: nếu bàn đang được chọn trong multi-table mode → màu xanh dương đặc
        boolean isSelected = multiTableMode
                && selectedTables.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
        Color bg = isSelected ? SELECTED_MULTI
                : trang == TrangThaiBan.DaDuocDat      ? AMBER_DAT
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
        // Hiển thị dấu ✓ nhỏ khi bàn đang được chọn
        if (isSelected) {
            numLbl.setText("<html><center>Bàn " + ban.getSoBan() + "<br><font size='3'>✓</font></center></html>");
        }
        card.add(numLbl, BorderLayout.CENTER);

        JPanel bot = new JPanel(new GridLayout(2, 1, 0, 2));
        bot.setOpaque(false);
        JLabel sucLbl = new JLabel(ban.getSucChua() + " người", SwingConstants.CENTER);
        sucLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sucLbl.setForeground(Color.WHITE);
        String statusText = isSelected ? "Đã chọn"
                : trang == TrangThaiBan.DaDuocDat ? "Đã đặt"
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
        // ── Chế độ chọn nhiều bàn ────────────────────────────────────────
        if (multiTableMode) {
            onCardClickMultiMode(ban);
            return;
        }
        // ── Chế độ chọn 1 bàn (logic cũ) ────────────────────────────────
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                Ban fresh = banDAO.getBanByMa(ban.getMaBan());
                Ban b = (fresh != null) ? fresh : ban;
                List<DonDatBan> allDons = ddbDAO.getAllDonDatBanWithBan();
                TrangThaiBan t = computeEffectiveStatus(b, allDons);
                if (t == TrangThaiBan.DaDuocDat || t == TrangThaiBan.DangDuocSuDung) {
                    DonDatBan don = findActiveDonFromList(b.getMaBan(), allDons);
                    HoaDon hd = (don != null) ? hdDAO.getHoaDonByMaDon(don.getMaDon()) : null;
                    KhachHang fullKH = null;
                    List<ChiTietHoaDon> cths = new ArrayList<>();
                    if (hd != null) {
                        if (hd.getKhachHang() != null)
                            fullKH = khDAO.getKhachHangByMa(hd.getKhachHang().getMaKH());
                        cths = cthdDAO.getChiTietByMaHDWithLoai(hd.getMaHD());
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

    /**
     * Xử lý click bàn khi đang ở chế độ chọn nhiều bàn.
     * Chỉ cho phép chọn bàn Trống; bàn đã đặt/đang dùng bị bỏ qua.
     */
    private void onCardClickMultiMode(Ban ban) {
        // Kiểm tra bàn có trống không (phải load trạng thái thực)
        Ban fresh = banDAO.getBanByMa(ban.getMaBan());
        Ban b = (fresh != null) ? fresh : ban;
        List<DonDatBan> allDons = ddbDAO.getAllDonDatBanWithBan();
        TrangThaiBan t = computeEffectiveStatus(b, allDons);

        if (t != TrangThaiBan.Trong) {
            msg("Chỉ có thể chọn bàn đang trống trong chế độ đặt nhiều bàn.");
            return;
        }

        boolean alreadySelected = selectedTables.stream()
                .anyMatch(sel -> sel.getMaBan().equals(b.getMaBan()));
        if (alreadySelected) {
            // Bỏ chọn
            selectedTables.removeIf(sel -> sel.getMaBan().equals(b.getMaBan()));
        } else {
            // Thêm vào danh sách
            selectedTables.add(b);
        }

        // Cập nhật UI: nút đặt bàn + refresh màu card
        refreshMultiTableStatus();
        loadTableCards();
    }

    /** Bật / tắt chế độ chọn nhiều bàn */
    private void toggleMultiTableMode() {
        multiTableMode = !multiTableMode;
        selectedTables.clear();
        if (multiTableMode) {
            btnMultiTableMode.setText("Chọn nhiều bàn: BẬT  ✓");
            btnMultiTableMode.setBackground(SELECTED_MULTI);
            showMultiTableBookingPrompt();
        } else {
            btnMultiTableMode.setText("Chọn nhiều bàn: TẮT");
            btnMultiTableMode.setBackground(new Color(100, 100, 110));
            showEmpty();
        }
        loadTableCards();
    }

    /** Hiển thị panel hướng dẫn + danh sách bàn đang chọn ở right panel */
    private void showMultiTableBookingPrompt() {
        // Tạo một panel đơn giản hướng dẫn người dùng chọn bàn
        JPanel p = buildMultiTableInfoPanel();
        Component[] comps = rightPanel.getComponents();
        for (Component c : comps) {
            if ("multiInfo".equals(c.getName())) rightPanel.remove(c);
        }
        p.setName("multiInfo");
        rightPanel.add(p, "multiInfo");
        rightCard.show(rightPanel, "multiInfo");
    }

    private JPanel buildMultiTableInfoPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(235, 245, 255));
        hdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_CLR),
                new EmptyBorder(10, 14, 10, 14)));
        JLabel title = new JLabel("ĐẶT NHIỀU BÀN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(SELECTED_MULTI);
        hdr.add(title, BorderLayout.WEST);
        root.add(hdr, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(14, 16, 14, 16));
        body.setBackground(Color.WHITE);

        JLabel hint = new JLabel("<html><b>Hướng dẫn:</b> Click vào các bàn trống để chọn.<br>"
                + "Click lần nữa để bỏ chọn.</html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setAlignmentX(0f);
        body.add(hint);
        body.add(Box.createVerticalStrut(12));

        // Nhãn cập nhật động hiển thị bàn đang chọn
        JLabel lblSelected = new JLabel("Chưa chọn bàn nào");
        lblSelected.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelected.setForeground(SELECTED_MULTI);
        lblSelected.setName("lblMultiSelected");
        lblSelected.setAlignmentX(0f);
        body.add(lblSelected);
        body.add(Box.createVerticalStrut(8));

        JLabel lblCapacity = new JLabel("");
        lblCapacity.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCapacity.setForeground(new Color(80, 80, 80));
        lblCapacity.setName("lblMultiCapacity");
        lblCapacity.setAlignmentX(0f);
        body.add(lblCapacity);
        body.add(Box.createVerticalStrut(14));

        JButton btnProceed = new JButton("XÁC NHẬN & ĐẶT BÀN");
        btnProceed.setBackground(SELECTED_MULTI);
        btnProceed.setForeground(Color.WHITE);
        btnProceed.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProceed.setFocusPainted(false);
        btnProceed.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnProceed.setAlignmentX(0f);
        btnProceed.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnProceed.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnProceed.addActionListener(e -> doShowMultiTableBookingForm());
        body.add(btnProceed);

        root.add(body, BorderLayout.CENTER);

        // Timer để cập nhật label mỗi 200ms (phản ánh selectedTables live)
        javax.swing.Timer refreshTimer = new javax.swing.Timer(200, e -> {
            if (!multiTableMode) { ((javax.swing.Timer) e.getSource()).stop(); return; }
            if (selectedTables.isEmpty()) {
                lblSelected.setText("Chưa chọn bàn nào");
                lblCapacity.setText("");
            } else {
                String names = selectedTables.stream()
                        .map(b -> "Bàn " + b.getSoBan())
                        .collect(java.util.stream.Collectors.joining(", "));
                int total = selectedTables.stream().mapToInt(Ban::getSucChua).sum();
                lblSelected.setText("Đã chọn: " + names);
                lblCapacity.setText("Tổng sức chứa: " + total + " người");
            }
        });
        refreshTimer.start();

        return root;
    }

    /** Cập nhật nút / trạng thái sau mỗi lần click bàn multi-mode */
    private void refreshMultiTableStatus() {
        // Panel multiInfo sẽ tự cập nhật qua Timer; không cần làm gì thêm
    }

    /** Mở form đặt bàn khi đã chọn đủ bàn */
    private void doShowMultiTableBookingForm() {
        if (selectedTables.isEmpty()) {
            msg("Vui lòng chọn ít nhất 1 bàn!");
            return;
        }
        // Tạo QuanLyDatBan_DB với danh sách bàn đã chọn
        QuanLyDatBan_DB bookingPanel = new QuanLyDatBan_DB(
                currentNV,
                new ArrayList<>(selectedTables),
                selectedBookingDate,
                currentFilter,
                new QuanLyDatBan_DB.BookingListener() {
                    @Override public void onBookingSuccess() {
                        // Tắt multi-mode, reset, refresh
                        multiTableMode = false;
                        selectedTables.clear();
                        btnMultiTableMode.setText("Chọn nhiều bàn: TẮT");
                        btnMultiTableMode.setBackground(new Color(100, 100, 110));
                        loadTableCards();
                        showEmpty();
                    }
                    @Override public void onCancel() {
                        showMultiTableBookingPrompt();
                    }
                }
        );
        Component[] comps = rightPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof QuanLyDatBan_DB) rightPanel.remove(c);
        }
        rightPanel.add(bookingPanel, "booking");
        rightCard.show(rightPanel, "booking");
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
    // ── Bảng Reserved – dùng panel động thay cho JTable cố định ─────────
    private JPanel  pResDishRows;  // container chứa các dòng món (thay cho JTable)

    private JPanel buildReserved() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // ── Header tiêu đề bàn ───────────────────────────────────────────
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

        // ── Phần cố định phía trên: thông tin KH ─────────────────────────
        JPanel topFixed = new JPanel();
        topFixed.setLayout(new BoxLayout(topFixed, BoxLayout.Y_AXIS));
        topFixed.setBackground(Color.WHITE);
        topFixed.setBorder(new EmptyBorder(10, 14, 4, 14));

        JPanel info = new JPanel(new GridLayout(3, 2, 8, 6));
        info.setOpaque(false);
        info.add(infoKey("Khách hàng:"));    info.add(lblResKhach  = infoVal(""));
        info.add(infoKey("Số điện thoại:")); info.add(lblResSdt    = infoVal(""));
        info.add(infoKey("Ghi chú:"));       info.add(lblResGhiChu = infoVal(""));
        topFixed.add(info);
        topFixed.add(Box.createVerticalStrut(8));
        topFixed.add(hsep());
        topFixed.add(sectionLabel("DANH SÁCH MÓN ĐÃ GỌI"));

        // header cột bảng
        JPanel dishHeader = buildDishHeaderRow(true);
        topFixed.add(dishHeader);

        // ── Phần CENTER: bảng món fill toàn bộ không gian còn lại ────────
        pResDishRows = new ScrollablePanel();
        pResDishRows.setLayout(new BoxLayout(pResDishRows, BoxLayout.Y_AXIS));
        pResDishRows.setBackground(Color.WHITE);

        JScrollPane tableScroll = new JScrollPane(pResDishRows);
        tableScroll.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));
        tableScroll.getVerticalScrollBar().setUnitIncrement(14);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // ── Dòng tổng tiền ────────────────────────────────────────────────
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(Color.WHITE);
        totalRow.setBorder(new EmptyBorder(6, 14, 6, 14));
        lblResTotal = new JLabel("Tổng: 0đ");
        lblResTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResTotal.setForeground(TEXT_DARK);
        totalRow.add(new JLabel(), BorderLayout.WEST);
        totalRow.add(lblResTotal, BorderLayout.EAST);

        // ── Ghép layout: topFixed NORTH, tableScroll CENTER, totalRow trước SOUTH
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setBackground(Color.WHITE);
        centerArea.add(topFixed,    BorderLayout.NORTH);
        centerArea.add(tableScroll, BorderLayout.CENTER);
        centerArea.add(totalRow,    BorderLayout.SOUTH);
        root.add(centerArea, BorderLayout.CENTER);

        // ── Nút hành động ─────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new GridLayout(1, 3, 8, 0));
        btnRow.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(10, 14, 10, 14)));
        btnRow.setBackground(Color.WHITE);
        JButton bHuy     = actionBtn("HỦY ĐẶT",   new Color(255, 236, 236), RED_DANG,   true);
        JButton bThem    = actionBtn("+ THÊM MÓN", new Color(232, 242, 255), MAIN_BLUE,  true);
        JButton bCheckin = actionBtn("CHECK-IN",   MAIN_BLUE,               Color.WHITE, false);
        bHuy.addActionListener(e     -> doCancelBooking());
        bThem.addActionListener(e    -> doAddDish(false));
        bCheckin.addActionListener(e -> doCheckIn());
        btnRow.add(bHuy); btnRow.add(bThem); btnRow.add(bCheckin);
        root.add(btnRow, BorderLayout.SOUTH);
        return root;
    }

    // ── Bảng Using – panel động với nút +/- cho Beer/Ngọt/Suối ──────────
    private JPanel pUseDishRows;   // container các dòng món trong "using"

    private JPanel buildUsing() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // ── Header tiêu đề bàn ───────────────────────────────────────────
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

        // ── Phần cố định phía trên: label + header cột ───────────────────
        JPanel topFixed = new JPanel();
        topFixed.setLayout(new BoxLayout(topFixed, BoxLayout.Y_AXIS));
        topFixed.setBackground(Color.WHITE);
        topFixed.setBorder(new EmptyBorder(8, 14, 0, 14));
        topFixed.add(sectionLabel("ĐƠN GỌI MÓN"));
        JPanel dishHeader = buildDishHeaderRow(true);
        topFixed.add(dishHeader);

        // ── Phần CENTER: bảng món fill toàn bộ không gian còn lại ────────
        pUseDishRows = new ScrollablePanel();
        pUseDishRows.setLayout(new BoxLayout(pUseDishRows, BoxLayout.Y_AXIS));
        pUseDishRows.setBackground(Color.WHITE);

        JScrollPane tableScroll = new JScrollPane(pUseDishRows);
        tableScroll.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));
        tableScroll.getVerticalScrollBar().setUnitIncrement(14);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // ── Phần tổng kết tiền ────────────────────────────────────────────
        JPanel sumBlock = new JPanel(new BorderLayout());
        sumBlock.setBackground(Color.WHITE);
        sumBlock.setBorder(new EmptyBorder(8, 14, 6, 14));
        JPanel sum = new JPanel(new GridLayout(3, 2, 14, 5));
        sum.setOpaque(false);
        sum.add(infoKey("Tổng tiền món:")); sum.add(lblUseTong   = infoVal("0đ"));
        sum.add(infoKey("Đã cọc:"));       sum.add(lblUseCoc    = infoVal("−" + FMT.format(TIEN_COC) + "đ"));
        sum.add(infoKey("Còn lại:"));      sum.add(lblUseConLai = infoVal("0đ"));
        lblUseConLai.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUseConLai.setForeground(RED_DANG);
        sumBlock.add(sum, BorderLayout.EAST);

        // ── Ghép layout ───────────────────────────────────────────────────
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setBackground(Color.WHITE);
        centerArea.add(topFixed,    BorderLayout.NORTH);
        centerArea.add(tableScroll, BorderLayout.CENTER);
        centerArea.add(sumBlock,    BorderLayout.SOUTH);
        root.add(centerArea, BorderLayout.CENTER);

        // ── Nút hành động ─────────────────────────────────────────────────
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
                java.util.Collections.singletonList(ban),   // 1 bàn → List<Ban>
                selectedBookingDate,
                currentFilter,
                new QuanLyDatBan_DB.BookingListener() {
                    @Override
                    public void onBookingSuccess() {
                        loadTableCards();
                        showEmpty();
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

        // Trạng thái "Đã đặt" → cho phép chỉnh sửa toàn bộ món (+/-/xóa)
        boolean paid = (hd != null && hd.getTrangThaiThanhToan() == TrangThaiThanhToan.DA_THANH_TOAN);
        rebuildDishRows(pResDishRows, cths, hd,
                /* canEditAll= */ !paid,
                /* onlyBeverages= */ false,
                /* isPaid= */ paid);

        // Tính tổng
        double total = 0;
        for (ChiTietHoaDon ct : cths) total += ct.getDonGia() * ct.getSoLuong();
        lblResTotal.setText("Tổng: " + FMT.format(total) + "đ");

        rightCard.show(rightPanel, "reserved");
    }

    private void renderUsing(Ban ban, DonDatBan don, HoaDon hd, KhachHang fullKH, List<ChiTietHoaDon> cths) {
        lblUseTitle.setText("ĐANG PHỤC VỤ  —  BÀN " + ban.getSoBan());
        lblUseKhach.setText(fullKH != null ? "Khách: " + fullKH.getTenKH() : "");

        boolean paid = (hd != null && hd.getTrangThaiThanhToan() == TrangThaiThanhToan.DA_THANH_TOAN);

        // Trạng thái "Đang phục vụ": chỉ cho chỉnh sửa số lượng danh mục đồ uống
        rebuildDishRows(pUseDishRows, cths, hd,
                /* canEditAll= */ false,
                /* onlyBeverages= */ !paid,
                /* isPaid= */ paid);

        double total = 0;
        for (ChiTietHoaDon ct : cths) total += ct.getDonGia() * ct.getSoLuong();

        double coc    = (hd != null) ? hd.getTienCoc() : TIEN_COC;
        double conLai = total - coc;

        lblUseTong.setText(FMT.format(total) + "đ");
        lblUseCoc.setText("−" + FMT.format(coc) + "đ");
        lblUseConLai.setText(FMT.format(Math.max(0, conLai)) + "đ");

        // Ẩn/hiện nút THÊM MÓN theo trạng thái hóa đơn
        updateUsingButtons(paid, hd);

        rightCard.show(rightPanel, "using");
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPER: Xây dựng lại danh sách dòng món ăn động theo trạng thái bàn
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Các tên danh mục được phép chỉnh sửa số lượng khi bàn đang phục vụ.
     * So sánh không phân biệt hoa/thường, substring match.
     */
    private static boolean isBeverageCategory(ChiTietHoaDon ct) {
        SanPham sp = ct.getMonAn();
        if (sp == null || sp.getLoaiSanPham() == null) return false;
        String cat = sp.getLoaiSanPham().getTenLoai();
        if (cat == null) return false;
        cat = cat.toLowerCase().trim();
        return cat.contains("beer")
                || cat.contains("bia")
                || cat.contains("ngọt")
                || cat.contains("ngot")
                || cat.contains("có gas")
                || cat.contains("co gas")
                || cat.contains("suối")
                || cat.contains("suoi")
                || cat.contains("nước suối")
                || cat.contains("nuoc suoi");
    }

    /**
     * Header cố định cho bảng món (thay thế JTable header).
     * @param hasAction true = có cột hành động (+/-/xóa)
     */
    private JPanel buildDishHeaderRow(boolean hasAction) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(new Color(240, 244, 250));
        row.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1),
                new EmptyBorder(4, 6, 4, 6)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;

        Font hFont = new Font("Segoe UI", Font.BOLD, 11);
        Color hClr = new Color(80, 100, 130);

        g.gridx = 0;
        g.weightx = 0;
        JLabel c0 = headerCell("STT", hFont, hClr, SwingConstants.CENTER);
        c0.setPreferredSize(new Dimension(35, 20));
        c0.setMinimumSize(new Dimension(35, 20));
        row.add(c0, g);

        g.gridx = 1;
        g.weightx = 1.0;
        JLabel c1 = headerCell("Tên món", hFont, hClr, SwingConstants.LEFT);
        row.add(c1, g);

        g.gridx = 2;
        g.weightx = 0;
        JLabel c2 = headerCell("SL", hFont, hClr, SwingConstants.CENTER);
        c2.setPreferredSize(new Dimension(90, 20));
        c2.setMinimumSize(new Dimension(90, 20));
        row.add(c2, g);

        g.gridx = 3;
        g.weightx = 0;
        JLabel c3 = headerCell("Đơn giá", hFont, hClr, SwingConstants.RIGHT);
        c3.setPreferredSize(new Dimension(85, 20));
        c3.setMinimumSize(new Dimension(85, 20));
        row.add(c3, g);

        g.gridx = 4;
        g.weightx = 0;
        JLabel c4 = headerCell("Thành tiền", hFont, hClr, SwingConstants.RIGHT);
        c4.setPreferredSize(new Dimension(95, 20));
        c4.setMinimumSize(new Dimension(95, 20));
        row.add(c4, g);

        if (hasAction) {
            g.gridx = 5;
            g.weightx = 0;
            JLabel c5 = headerCell("", hFont, hClr, SwingConstants.CENTER);
            c5.setPreferredSize(new Dimension(35, 20));
            c5.setMinimumSize(new Dimension(35, 20));
            row.add(c5, g);
        }

        return row;
    }

    private JLabel headerCell(String text, Font font, Color clr, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(font); l.setForeground(clr);
        return l;
    }

    /**
     * Xây lại toàn bộ danh sách dòng món trong container chỉ định.
     *
     * @param container    Panel chứa các dòng (pResDishRows / pUseDishRows)
     * @param cths         Danh sách chi tiết hóa đơn hiện tại
     * @param hd           Hóa đơn (dùng để lưu thay đổi)
     * @param canEditAll   true → cho phép +/-/xóa tất cả (trạng thái Đã đặt)
     * @param onlyBeverages true → chỉ cho +/- đồ uống (trạng thái Đang phục vụ)
     * @param isPaid       true → khóa toàn bộ (đã thanh toán)
     */
    private void rebuildDishRows(JPanel container, List<ChiTietHoaDon> cths,
                                 HoaDon hd, boolean canEditAll,
                                 boolean onlyBeverages, boolean isPaid) {
        container.removeAll();
        int stt = 1;
        for (ChiTietHoaDon ct : cths) {
            boolean isBev  = isBeverageCategory(ct);
            boolean canEdit = !isPaid && (canEditAll || (onlyBeverages && isBev));

            JPanel row = buildDishDataRow(stt++, ct, hd, canEdit, canEditAll, container, cths, isPaid);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            container.add(row);

            // Zebra striping
            row.setBackground((stt % 2 == 0) ? new Color(250, 250, 252) : Color.WHITE);
        }

        if (isPaid) {
            JLabel lockNote = new JLabel("  🔒 Hóa đơn đã thanh toán, không thể cập nhật món.");
            lockNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lockNote.setForeground(new Color(160, 80, 80));
            lockNote.setAlignmentX(Component.LEFT_ALIGNMENT);
            lockNote.setBorder(new EmptyBorder(4, 6, 2, 6));
            container.add(lockNote);
        }

        container.revalidate();
        container.repaint();
    }

    /**
     * Tạo một dòng dữ liệu cho 1 chi tiết hóa đơn.
     * Nếu canEdit=true: hiển thị nút [−] SL [+] và nút xóa [×].
     * Nếu canEdit=false nhưng isServiceBev=true: hiển thị nút [−] SL [+] (không xóa).
     */
    private JPanel buildDishDataRow(int stt, ChiTietHoaDon ct, HoaDon hd, boolean canEdit, boolean canDelete, JPanel container, List<ChiTietHoaDon> cths, boolean isPaid) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_CLR),
                new EmptyBorder(3, 6, 3, 6)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        Font dFont = new Font("Segoe UI", Font.PLAIN, 12);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;

        g.gridx = 0;
        g.weightx = 0;
        JLabel lStt = new JLabel(String.valueOf(stt), SwingConstants.CENTER);
        lStt.setFont(dFont);
        lStt.setPreferredSize(new Dimension(35, 24));
        lStt.setMinimumSize(new Dimension(35, 24));
        row.add(lStt, g);

        g.gridx = 1;
        g.weightx = 1.0;
        JLabel lName = new JLabel(ct.getMonAn().getTenMon());
        lName.setFont(dFont);
        row.add(lName, g);

        g.gridx = 2;
        g.weightx = 0;
        if (canEdit) {
            JPanel qPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
            qPanel.setOpaque(false);
            qPanel.setPreferredSize(new Dimension(90, 24));
            qPanel.setMinimumSize(new Dimension(90, 24));

            JButton btnMinus = qtyBtn("−");

            // --- THAY THẾ JLabel BẰNG JTextField ---
            JTextField txtQty = new JTextField(String.valueOf(ct.getSoLuong()), 3);
            txtQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
            txtQty.setPreferredSize(new Dimension(28, 22));
            txtQty.setHorizontalAlignment(JTextField.CENTER);
            txtQty.setBorder(new LineBorder(BORDER_CLR));

            // Bôi đen khi focus để xóa số cũ nhanh
            txtQty.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) { txtQty.selectAll(); }
            });

            JButton btnPlus = qtyBtn("+");

            JLabel[] totalRef = {null};

            btnPlus.addActionListener(e -> {
                if (hd == null) return;
                int newQty = ct.getSoLuong() + 1;
                double newTT = ct.getDonGia() * newQty;
                if (cthdDAO.updateSoLuong(hd.getMaHD(), ct.getMonAn().getMaMon(), newQty, newTT)) {
                    ct.setSoLuong(newQty);
                    ct.setThanhTien(newTT);
                    txtQty.setText(String.valueOf(newQty)); // Cập nhật text field
                    if (totalRef[0] != null) totalRef[0].setText(FMT.format(newTT) + "đ");
                    hdDAO.updateTongTien(hd.getMaHD(), recalcTotal(cths));
                    refreshTotals(container, cths, hd);
                }
            });

            btnMinus.addActionListener(e -> {
                if (hd == null) return;
                int cur = ct.getSoLuong();
                if (cur <= 1) {
                    if (!canDelete) {
                        msg("Số lượng tối thiểu là 1.");
                        return;
                    }
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Số lượng về 0. Xóa \"" + ct.getMonAn().getTenMon() + "\" khỏi hóa đơn?",
                            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    cthdDAO.deleteByMaHDAndMaMon(hd.getMaHD(), ct.getMonAn().getMaMon());
                    cths.remove(ct);
                    hdDAO.updateTongTien(hd.getMaHD(), recalcTotal(cths));
                    rebuildDishRows(container, cths, hd, canDelete, !canDelete, isPaid);
                    refreshTotals(container, cths, hd);
                    return;
                }
                int newQty = cur - 1;
                double newTT = ct.getDonGia() * newQty;
                if (cthdDAO.updateSoLuong(hd.getMaHD(), ct.getMonAn().getMaMon(), newQty, newTT)) {
                    ct.setSoLuong(newQty);
                    ct.setThanhTien(newTT);
                    txtQty.setText(String.valueOf(newQty)); // Cập nhật text field
                    if (totalRef[0] != null) totalRef[0].setText(FMT.format(newTT) + "đ");
                    hdDAO.updateTongTien(hd.getMaHD(), recalcTotal(cths));
                    refreshTotals(container, cths, hd);
                }
            });

            // Xử lý sự kiện nhấn Enter tại JTextField
            txtQty.addActionListener(e -> {
                try {
                    int newQty = Integer.parseInt(txtQty.getText());
                    if (newQty < 0) throw new NumberFormatException();
                    double newTT = ct.getDonGia() * newQty;
                    if (cthdDAO.updateSoLuong(hd.getMaHD(), ct.getMonAn().getMaMon(), newQty, newTT)) {
                        ct.setSoLuong(newQty);
                        ct.setThanhTien(newTT);
                        if (totalRef[0] != null) totalRef[0].setText(FMT.format(newTT) + "đ");
                        hdDAO.updateTongTien(hd.getMaHD(), recalcTotal(cths));
                        refreshTotals(container, cths, hd);
                    }
                } catch (NumberFormatException ex) { txtQty.setText(String.valueOf(ct.getSoLuong())); }
            });

            qPanel.add(btnMinus);
            qPanel.add(txtQty);
            qPanel.add(btnPlus);
            row.add(qPanel, g);

            g.gridx = 3;
            g.weightx = 0;
            JLabel lDG = new JLabel(FMT.format(ct.getDonGia()) + "đ", SwingConstants.RIGHT);
            lDG.setFont(dFont);
            lDG.setPreferredSize(new Dimension(85, 24));
            lDG.setMinimumSize(new Dimension(85, 24));
            row.add(lDG, g);

            g.gridx = 4;
            g.weightx = 0;
            JLabel lTT = new JLabel(FMT.format(ct.getDonGia() * ct.getSoLuong()) + "đ", SwingConstants.RIGHT);
            lTT.setFont(dFont);
            lTT.setPreferredSize(new Dimension(95, 24));
            lTT.setMinimumSize(new Dimension(95, 24));
            row.add(lTT, g);
            totalRef[0] = lTT;

            g.gridx = 5;
            g.weightx = 0;
            if (canDelete) {
                JPanel dPan = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                dPan.setOpaque(false);
                dPan.setPreferredSize(new Dimension(35, 24));
                dPan.setMinimumSize(new Dimension(35, 24));

                JButton btnDel = new JButton("×");
                btnDel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnDel.setPreferredSize(new Dimension(24, 22));
                btnDel.setMargin(new Insets(0, 0, 0, 0));
                btnDel.setFocusPainted(false);
                btnDel.setBackground(new Color(255, 230, 230));
                btnDel.setForeground(RED_DANG);
                btnDel.setToolTipText("Xóa món này");
                btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnDel.addActionListener(e -> {
                    if (hd == null) return;
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Xóa \"" + ct.getMonAn().getTenMon() + "\" khỏi danh sách?",
                            "Xóa món", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    cthdDAO.deleteByMaHDAndMaMon(hd.getMaHD(), ct.getMonAn().getMaMon());
                    cths.remove(ct);
                    hdDAO.updateTongTien(hd.getMaHD(), recalcTotal(cths));
                    rebuildDishRows(container, cths, hd, true, false, isPaid);
                    refreshTotals(container, cths, hd);
                });
                dPan.add(btnDel);
                row.add(dPan, g);
            } else {
                JLabel bLk = new JLabel();
                bLk.setPreferredSize(new Dimension(35, 24));
                bLk.setMinimumSize(new Dimension(35, 24));
                row.add(bLk, g);
            }

        } else {
            JLabel lQty = new JLabel(String.valueOf(ct.getSoLuong()), SwingConstants.CENTER);
            lQty.setFont(dFont);
            lQty.setForeground(isPaid ? new Color(140, 140, 140) : TEXT_DARK);
            lQty.setPreferredSize(new Dimension(90, 24));
            lQty.setMinimumSize(new Dimension(90, 24));
            row.add(lQty, g);

            g.gridx = 3;
            g.weightx = 0;
            JLabel lDG = new JLabel(FMT.format(ct.getDonGia()) + "đ", SwingConstants.RIGHT);
            lDG.setFont(dFont);
            lDG.setForeground(isPaid ? new Color(140, 140, 140) : TEXT_DARK);
            lDG.setPreferredSize(new Dimension(85, 24));
            lDG.setMinimumSize(new Dimension(85, 24));
            row.add(lDG, g);

            g.gridx = 4;
            g.weightx = 0;
            JLabel lTT = new JLabel(FMT.format(ct.getDonGia() * ct.getSoLuong()) + "đ", SwingConstants.RIGHT);
            lTT.setFont(dFont);
            lTT.setForeground(isPaid ? new Color(140, 140, 140) : TEXT_DARK);
            lTT.setPreferredSize(new Dimension(95, 24));
            lTT.setMinimumSize(new Dimension(95, 24));
            row.add(lTT, g);

            g.gridx = 5;
            g.weightx = 0;
            JLabel blk2 = new JLabel();
            blk2.setPreferredSize(new Dimension(35, 24));
            blk2.setMinimumSize(new Dimension(35, 24));
            row.add(blk2, g);
        }

        return row;
    }

    /** Nút tăng/giảm số lượng nhỏ gọn */
    private JButton qtyBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(24, 22));
        b.setMargin(new Insets(0,0,0,0));
        b.setFocusPainted(false);
        b.setBackground(BG_LIGHT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Tính lại tổng tiền từ list chi tiết */
    private double recalcTotal(List<ChiTietHoaDon> cths) {
        double t = 0;
        for (ChiTietHoaDon ct : cths) t += ct.getDonGia() * ct.getSoLuong();
        return t;
    }

    /**
     * Refresh nhãn tổng tiền sau khi thay đổi số lượng.
     * Tự phát hiện đang ở "reserved" hay "using" qua container.
     */
    private void refreshTotals(JPanel container, List<ChiTietHoaDon> cths, HoaDon hd) {
        double total = recalcTotal(cths);
        if (container == pResDishRows && lblResTotal != null) {
            lblResTotal.setText("Tổng: " + FMT.format(total) + "đ");
        } else if (container == pUseDishRows) {
            if (lblUseTong != null) lblUseTong.setText(FMT.format(total) + "đ");
            double coc    = (hd != null) ? hd.getTienCoc() : TIEN_COC;
            double conLai = total - coc;
            if (lblUseConLai != null)
                lblUseConLai.setText(FMT.format(Math.max(0, conLai)) + "đ");
        }
    }


    /**
     * Tìm lại nút trong panel bottom và disable/enable tương ứng.
     */
    private void updateUsingButtons(boolean paid, HoaDon hd) {
        // Tìm panel "using" từ rightPanel
        for (Component c : rightPanel.getComponents()) {
            if (!(c instanceof JPanel)) continue;
            disableThemMonInPanel((JPanel) c, paid, hd);
        }
    }

    private void disableThemMonInPanel(JPanel panel, boolean paid, HoaDon hd) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                if (btn.getText().contains("THÊM MÓN") || btn.getText().contains("+ THÊM")) {
                    btn.setEnabled(!paid);
                    btn.setBackground(paid ? new Color(200, 200, 200)
                            : new Color(232, 242, 255));
                    btn.setForeground(paid ? new Color(130, 130, 130) : MAIN_BLUE);
                    if (paid) btn.setToolTipText("Hóa đơn đã thanh toán – không thể gọi thêm món");
                }
            }
            if (c instanceof JPanel) disableThemMonInPanel((JPanel) c, paid, hd);
        }
    }

    // ── business logic ───────────────────────────────────────────────────
    private void doCancelBooking() {
        if (currentBan == null) return;
        DonDatBan don = findActiveDon(currentBan.getMaBan());

        // ── Kiểm tra nghiệp vụ: chỉ hủy trước giờ đặt ít nhất 4 tiếng ──
        if (don != null && don.getThoiGianDen() != null) {
            long now      = System.currentTimeMillis();
            long gioVao   = don.getThoiGianDen().getTime();
            // Nếu đã tính theo khungGio thì dùng SLOT_START_H của slot tương ứng
            // Ưu tiên: lấy giờ bắt đầu ca/slot thực tế từ selectedBookingDate + SLOT_START
            int slotIdx  = getSlotIndex(currentFilter);
            Calendar bookingStart = Calendar.getInstance();
            bookingStart.setTime(selectedBookingDate);
            bookingStart.set(Calendar.HOUR_OF_DAY, SLOT_START_H[slotIdx]);
            bookingStart.set(Calendar.MINUTE,      SLOT_START_M[slotIdx]);
            bookingStart.set(Calendar.SECOND, 0);
            bookingStart.set(Calendar.MILLISECOND, 0);

            long millisToStart = bookingStart.getTimeInMillis() - now;
            long fourHoursMs   = 4L * 60 * 60 * 1000; // 4 tiếng tính bằng ms

            if (millisToStart < fourHoursMs) {
                String gioSlot = String.format("%02d:%02d",
                        SLOT_START_H[slotIdx], SLOT_START_M[slotIdx]);
                msg("Chỉ được hủy bàn trước giờ đặt tối thiểu 4 tiếng.\n"
                        + "Ca đặt bắt đầu lúc " + gioSlot + " – hiện đã quá hạn hủy.");
                return;
            }
        }

        int r = JOptionPane.showConfirmDialog(this,
                "Xác nhận hủy đặt bàn số " + currentBan.getSoBan() + "?",
                "Hủy đặt bàn", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        if (don != null) {
            HoaDon hd = hdDAO.getHoaDonByMaDon(don.getMaDon());
            if (hd != null) {
                // ── Tự động cập nhật trạng thái DA_HUY thay vì xóa HĐ ──
                hdDAO.updateTrangThai(hd.getMaHD(), entity.TrangThaiThanhToan.DA_HUY);
            }
            ddbDAO.deleteDonDatBan(don.getMaDon());
            // Reset trạng thái tất cả bàn trong đơn
            if (isToday(selectedBookingDate)) {
                for (Ban b : don.getDsBan()) {
                    banDAO.updateTinhTrangBan(b.getMaBan(), TrangThaiBan.Trong);
                }
            }
        } else if (isToday(selectedBookingDate)) {
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
        List<ChiTietHoaDon> cths = cthdDAO.getChiTietByMaHDWithLoai(hd.getMaHD());
        if (cths == null || cths.isEmpty()) {
            msg("Vui lòng gọi ít nhất 1 món trước khi check-in!");
            return;
        }

        // Update tất cả bàn trong đơn
        for (Ban b : don.getDsBan()) {
            banDAO.updateTinhTrangBan(b.getMaBan(), TrangThaiBan.DangDuocSuDung);
        }
        // fallback: nếu dsBan chưa load, cập nhật ít nhất bàn hiện tại
        if (don.getDsBan().isEmpty()) {
            banDAO.updateTinhTrangBan(currentBan.getMaBan(), TrangThaiBan.DangDuocSuDung);
        }
        loadTableCards();
        onCardClick(currentBan);
    }

    private void doAddDish(boolean fromUsing) {
        if (currentBan == null) return;
        DonDatBan don = findActiveDon(currentBan.getMaBan());
        HoaDon hd = (don != null) ? hdDAO.getHoaDonByMaDon(don.getMaDon()) : null;
        if (hd == null) { msg("Không tìm thấy hóa đơn!"); return; }

        // ── Kiểm tra khóa tại tầng GUI (validation lớp 1) ────────────────
        TrangThaiThanhToan tt = hd.getTrangThaiThanhToan();
        if (tt == TrangThaiThanhToan.DA_THANH_TOAN) {
            msg("Bàn này đã hoàn tất thanh toán.\nKhông thể gọi thêm món cho hóa đơn đã thanh toán.");
            return;
        }
        if (tt == TrangThaiThanhToan.DA_HUY) {
            msg("Hóa đơn đã bị hủy. Không thể thực hiện thao tác này.");
            return;
        }

        // ── Mở dialog – validation lớp 2 bên trong GM ────────────────────
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

        List<ChiTietHoaDon> cths = cthdDAO.getChiTietByMaHDWithLoai(hd.getMaHD());
        double tongMon = 0;
        for (ChiTietHoaDon ct : cths) tongMon += ct.getThanhTien();
        double coc    = hd.getTienCoc();
        double conLai = Math.max(0, tongMon - coc);

        int r = JOptionPane.showConfirmDialog(this,
                String.format("Tổng tiền món: %sđ%nĐã cọc trước: %sđ%nKhách trả thêm: %sđ%n%nXác nhận thanh toán?",
                        FMT.format(tongMon), FMT.format(coc), FMT.format(conLai)),
                "Thanh toán", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        // [MỚI] Chọn hình thức thanh toán
        entity.HinhThucThanhToan[] htOptions = entity.HinhThucThanhToan.values();
        String[] htLabels = new String[htOptions.length];
        for (int i = 0; i < htOptions.length; i++) htLabels[i] = htOptions[i].getDisplay();
        int htChoice = JOptionPane.showOptionDialog(this,
                "Chọn hình thức thanh toán:", "Hình Thức Thanh Toán",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, htLabels, htLabels[0]);
        entity.HinhThucThanhToan hinhThuc = (htChoice >= 0) ? htOptions[htChoice] : htOptions[0];

        hdDAO.updateTongTien(hd.getMaHD(), tongMon);
        hdDAO.updateThanhToan(hd.getMaHD(), entity.TrangThaiThanhToan.DA_THANH_TOAN, hinhThuc);

        if (don != null) { don.setTrangThai(true); ddbDAO.updateDonDatBan(don); }
        // Reset tất cả bàn trong đơn
        if (don != null && !don.getDsBan().isEmpty()) {
            for (Ban b : don.getDsBan()) {
                banDAO.updateTinhTrangBan(b.getMaBan(), TrangThaiBan.Trong);
            }
        } else {
            banDAO.updateTinhTrangBan(currentBan.getMaBan(), TrangThaiBan.Trong);
        }
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

    // ── UI helpers ───────────────────────────────────────────────────────

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
                        // Dùng bàn đầu tiên của đơn để navigate
                        Ban firstBan = foundDon.getDsBan().isEmpty()
                                ? foundDon.getBan()
                                : foundDon.getDsBan().get(0);
                        if (firstBan != null) onCardClick(firstBan);
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
    // ── ScrollablePanel: JPanel tự stretch full width trong JScrollPane ──
    private static class ScrollablePanel extends JPanel implements javax.swing.Scrollable {
        ScrollablePanel() { super(); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 14; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return visibleRect.height; }
        @Override public boolean getScrollableTracksViewportWidth()  { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
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