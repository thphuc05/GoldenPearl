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
    private static final Color  GREEN_TRONG  = Color.decode("#27AE60");
    private static final Color  AMBER_DAT    = Color.decode("#E67E22");
    private static final Color  RED_DANG     = Color.decode("#E74C3C");
    private static final Color  PURPLE_DON   = Color.decode("#8E44AD");
    private static final Color  BG_LIGHT    = Color.decode("#F0F2F5");
    private static final Color  TEXT_DARK   = Color.decode("#2C3E50");
    private static final Color  BORDER_CLR  = Color.decode("#DDE1E7");
    private static final DecimalFormat FMT  = new DecimalFormat("#,###");

    // --- DANH SÁCH GIỜ LỌC (mỗi 30 phút, 10:00 → 22:00) ---
    private static final java.util.List<String> TIME_OPTIONS = buildTimeOptions();
    private static java.util.List<String> buildTimeOptions() {
        java.util.List<String> list = new ArrayList<>();
        for (int h = 10; h <= 22; h++) {
            list.add(String.format("%02d:00", h));
            if (h < 22) list.add(String.format("%02d:30", h));
        }
        return list;
    }

    // --- SERVICE ---
    private final service.DatBanService datBanService = new service.DatBanService();

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
    private String currentSelectedTime = "11:00";
    private Date   selectedBookingDate;
    private final List<Date> bookingDates = new ArrayList<>();

    private JTextField        txtSearchBan;
    private JComboBox<String> cbNgayDat;
    private JComboBox<String> cbTimeFilter;

    // ── Chọn nhiều bàn ────────────────────────────────────────────────────
    /** Toggle button: OFF = chọn 1 bàn như cũ / ON = chọn nhiều bàn */
    private JButton btnMultiTableMode;
    /** true khi đang ở chế độ chọn nhiều bàn */
    private boolean multiTableMode = false;
    /** Danh sách bàn đang được chọn trong chế độ multi-table */
    private final List<Ban> selectedTables = new ArrayList<>();
    /** Màu highlight bàn đang được chọn trong multi-table mode */
    private static final Color SELECTED_MULTI = Color.decode("#3498DB");

    // ── Worker refs để cancel trước khi chạy mới (tránh chồng worker) ──────────
    private SwingWorker<?, ?> currentLoadWorker;

    // ── Sidebar lock callbacks (Feature 4) ──────────────────────────────────
    private final Runnable lockSidebar;
    private final Runnable unlockSidebar;
    private JLabel lblMultiModeInfo;

    // ── Badge đơn đặt bàn ────────────────────────────────────────────────────
    private JButton btnBadge;
    private javax.swing.Timer badgeRefreshTimer;

    // ── Overstay detection ───────────────────────────────────────────────────
    private javax.swing.Timer overstayTimer;
    private final Map<String, Long> overstayNotified = new HashMap<>();
    private static final long OVERSTAY_RENOTIFY_MS = 15 * 60 * 1_000L;

    // --- THÀNH PHẦN GIAO DIỆN (COMPONENTS) ---
    private JPanel      pThuongGrid, pVIPGrid;
    private JPanel      pThuongContent, pVIPContent;
    private JPanel      rightPanel;
    private CardLayout  rightCard;
    private JPanel      centerWrapper;
    private CardLayout  centerCardLayout;

    // Widgets cho trạng thái "Đã đặt" (Reserved)
    private JLabel            lblResTitle, lblResKhach, lblResSdt, lblResGhiChu, lblResKhung, lblResTotal;
    private DefaultTableModel tmResDish;

    // Widgets cho trạng thái "Đang dùng" (Using)
    private JLabel            lblUseTitle, lblUseKhach, lblUseTong, lblUseCoc, lblUseConLai;
    private JLabel            lblUseInfoKhach, lblUseInfoSdt, lblUseInfoGio, lblUseInfoGhiChu;
    private DefaultTableModel tmUseDish;

    /**
     * Khởi tạo giao diện Quản lý đặt bàn.
     * @param nhanVien Nhân viên đang đăng nhập hệ thống.
     */
    public QuanLyDatBan(NhanVien nhanVien) {
        this(nhanVien, null, null);
    }

    public QuanLyDatBan(NhanVien nhanVien, Runnable lockSidebar, Runnable unlockSidebar) {
        this.currentNV     = nhanVien;
        this.lockSidebar   = lockSidebar;
        this.unlockSidebar = unlockSidebar;
        selectedBookingDate = truncateToDay(new Date());
        autoSelectTime();
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);
        add(buildNorthBar(), BorderLayout.NORTH);
        centerCardLayout = new CardLayout();
        centerWrapper    = new JPanel(centerCardLayout);
        centerWrapper.add(buildCenter(), "MAIN");
        add(centerWrapper, BorderLayout.CENTER);
        startOverstayTimer();
        refreshBadge();
        startBadgeTimer();
    }

    public void refreshData() { loadTableCards(); showEmpty(); }

    private void autoSelectTime() {
        if (isToday(selectedBookingDate)) {
            currentSelectedTime = "Ăn ngay";
            return;
        }
        currentSelectedTime = "11:00";
    }

    private void updateTimeFilterSelection() {
        if (cbTimeFilter == null) return;
        for (int i = 0; i < cbTimeFilter.getItemCount(); i++) {
            if (currentSelectedTime.equals(cbTimeFilter.getItemAt(i))) {
                ActionListener[] listeners = cbTimeFilter.getActionListeners();
                for (ActionListener al : listeners) cbTimeFilter.removeActionListener(al);
                cbTimeFilter.setSelectedIndex(i);
                for (ActionListener al : listeners) cbTimeFilter.addActionListener(al);
                return;
            }
        }
    }

    /**
     * Rebuild danh sách giờ trong cbTimeFilter theo ngày đang chọn.
     * Nếu là hôm nay → chỉ hiện giờ chưa qua; ngày tương lai → hiện tất cả.
     */
    private void refreshTimeFilterItems() {
        if (cbTimeFilter == null) return;
        ActionListener[] listeners = cbTimeFilter.getActionListeners();
        for (ActionListener al : listeners) cbTimeFilter.removeActionListener(al);
        cbTimeFilter.removeAllItems();

        boolean today = isToday(selectedBookingDate);
        Calendar now  = Calendar.getInstance();
        int nowH = now.get(Calendar.HOUR_OF_DAY);
        int nowM = now.get(Calendar.MINUTE);

        if (today) cbTimeFilter.addItem("Ăn ngay");

        for (String t : TIME_OPTIONS) {
            if (today) {
                String[] parts = t.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h < nowH || (h == nowH && m < nowM)) continue; // đã qua → bỏ
            }
            cbTimeFilter.addItem(t);
        }

        // Giữ giờ đang chọn nếu còn trong list, không thì chọn giờ đầu tiên
        boolean kept = false;
        for (int i = 0; i < cbTimeFilter.getItemCount(); i++) {
            if (currentSelectedTime.equals(cbTimeFilter.getItemAt(i))) {
                cbTimeFilter.setSelectedIndex(i);
                kept = true;
                break;
            }
        }
        if (!kept && cbTimeFilter.getItemCount() > 0) {
            cbTimeFilter.setSelectedIndex(0);
            currentSelectedTime = cbTimeFilter.getItemAt(0);
        }

        for (ActionListener al : listeners) cbTimeFilter.addActionListener(al);
    }

    // ── NORTH bar ────────────────────────────────────────────────────────
    private JPanel buildNorthBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAIN_BLUE);
        bar.setPreferredSize(new Dimension(0, 68));
        bar.setBorder(new EmptyBorder(10, 28, 10, 28));

        // --- BÊN TRÁI (WEST): Tiêu đề & Ô tìm kiếm ---
        // Dùng GridBagLayout để tự động căn giữa dọc
        JPanel pnlWest = new JPanel(new GridBagLayout());
        pnlWest.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ ĐẶT BÀN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(GOLD_COLOR);
        JLabel titleSub = new JLabel("Đặt bàn và quản lý lịch đặt chỗ của khách hàng");
        titleSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleSub.setForeground(new Color(180, 200, 220));
        JPanel pTitleBox = new JPanel(); pTitleBox.setLayout(new BoxLayout(pTitleBox, BoxLayout.Y_AXIS)); pTitleBox.setOpaque(false);
        pTitleBox.add(title); pTitleBox.add(Box.createVerticalStrut(2)); pTitleBox.add(titleSub);
        pTitleBox.setBorder(new EmptyBorder(0, 0, 0, 20));
        pnlWest.add(pTitleBox);

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
        lblToday.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblToday.setForeground(new Color(200, 220, 240));
        right.add(lblToday);
        right.add(vSep());

        JLabel lblPicker = new JLabel("Ngày đặt:");
        lblPicker.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPicker.setForeground(GOLD_COLOR);
        right.add(lblPicker);

        JComboBox<String> dateCombo = buildDateCombo();
        dateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateCombo.setPreferredSize(new Dimension(200, 26));
        right.add(dateCombo);

        right.add(vSep());
        btnBadge = new JButton("Kiểm tra 0");
        btnBadge.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btnBadge.setForeground(Color.WHITE);
        btnBadge.setBackground(new Color(120, 120, 130));
        btnBadge.setBorderPainted(false);
        btnBadge.setFocusPainted(false);
        btnBadge.setOpaque(true);
        btnBadge.setPreferredSize(new Dimension(100, 26));
        btnBadge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBadge.setToolTipText("Xem danh sách đơn đặt bàn theo ngày");
        btnBadge.addActionListener(e -> showBookingListDialog());
        right.add(btnBadge);

        // 3. Đưa vào bar
        rightWrapper.add(right);
        bar.add(rightWrapper, BorderLayout.EAST);

        // Info label: hiện khi đang chọn nhiều bàn
        lblMultiModeInfo = new JLabel("⚠  Đang chọn bàn — Hoàn thành hoặc hủy để dùng sidebar");
        lblMultiModeInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMultiModeInfo.setForeground(new Color(255, 220, 60));
        lblMultiModeInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblMultiModeInfo.setVisible(false);
        bar.add(lblMultiModeInfo, BorderLayout.CENTER);

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
                autoSelectTime();
                refreshTimeFilterItems();
                loadTableCards();
                showEmpty();
                refreshBadge();
            }
        });
        return combo;
    }

    private JLabel chip(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
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
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.60));
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

        JLabel lblF = new JLabel("Giờ Đặt:");
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblF.setForeground(TEXT_DARK);
        filterLeft.add(lblF);

        cbTimeFilter = new JComboBox<>();
        cbTimeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTimeFilter.setPreferredSize(new Dimension(150, 26));
        refreshTimeFilterItems(); // điền items đã lọc + chọn đúng giờ
        cbTimeFilter.addActionListener(e -> {
            String sel = (String) cbTimeFilter.getSelectedItem();
            if (sel != null) {
                currentSelectedTime = sel;
                loadTableCards();
                showEmpty();
            }
        });
        filterLeft.add(cbTimeFilter);
        JPanel filtercen = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        filtercen.setOpaque(false);
        btnMultiTableMode = new JButton("Chọn nhiều bàn: TẮT");
        btnMultiTableMode.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btnMultiTableMode.setPreferredSize(new Dimension(200, 25));
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
        filterContainer.add(filterLeft, BorderLayout.WEST);
        filterContainer.add(filterCol, BorderLayout.EAST);

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
        if (currentLoadWorker != null && !currentLoadWorker.isDone())
            currentLoadWorker.cancel(true);
        currentLoadWorker = new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return new Object[]{ banDAO.getAllBan(), ddbDAO.getAllDonDatBanWithBan() };
            }
            @SuppressWarnings("unchecked")
            @Override protected void done() {
                if (isCancelled()) return;
                try {
                    Object[] r = get();
                    List<Ban>       dsBan   = (List<Ban>) r[0];
                    List<DonDatBan> allDons = (List<DonDatBan>) r[1];
                    pThuongGrid.removeAll(); pVIPGrid.removeAll();
                    for (Ban ban : dsBan) {
                        if (ban.getTinhTrangBan() == TrangThaiBan.BaoTri) continue;
                        TrangThaiBan status = computeEffectiveStatus(ban, allDons);
                        DonDatBan activeDon = (status != TrangThaiBan.Trong)
                                ? findActiveDonFromList(ban.getMaBan(), allDons) : null;
                        JPanel card = makeTableCard(ban, status, activeDon);
                        String loai = ban.getLoaiBan() != null ? ban.getLoaiBan().trim() : "";
                        if (loai.equalsIgnoreCase("VIP")) pVIPGrid.add(card);
                        else                              pThuongGrid.add(card);
                    }
                    pThuongGrid.revalidate(); pThuongGrid.repaint();
                    pVIPGrid.revalidate();    pVIPGrid.repaint();
                } catch (Exception ignored) {}
            }
        };
        currentLoadWorker.execute();
    }

    private TrangThaiBan computeEffectiveStatus(Ban ban, List<DonDatBan> allDons) {
        // Bàn đang dọn dẹp: giữ nguyên trạng thái DB, không xét lịch
        if (ban.getTinhTrangBan() == TrangThaiBan.DangDonDep) return TrangThaiBan.DangDonDep;

        Date timePoint = buildBookingDateTime(selectedBookingDate, currentSelectedTime);
        for (DonDatBan d : allDons) {
            if (d.isTrangThai()) continue;
            boolean hasBan = d.getDsBan().stream()
                    .anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
            if (!hasBan) continue;
            Date tgDen = d.getThoiGianDen();
            Date tgRoi = d.computeThoiGianDuKienRoi();
            if (tgDen == null || tgRoi == null) continue;
            // Bàn bận khi: tgDen <= timePoint < tgRoi
            if (!timePoint.before(tgDen) && timePoint.before(tgRoi)) {
                if (isToday(selectedBookingDate)
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
        Date timePoint = buildBookingDateTime(selectedBookingDate, currentSelectedTime);
        for (DonDatBan d : allDons) {
            if (d.isTrangThai()) continue;
            boolean hasBan = d.getDsBan().stream()
                    .anyMatch(b -> b.getMaBan().equals(maBan));
            if (!hasBan) continue;
            Date tgDen = d.getThoiGianDen();
            Date tgRoi = d.computeThoiGianDuKienRoi();
            if (tgDen == null || tgRoi == null) continue;
            if (!timePoint.before(tgDen) && timePoint.before(tgRoi)) {
                return d;
            }
        }
        return null;
    }

    // ── Overstay detection ───────────────────────────────────────────────────

    private void startOverstayTimer() {
        overstayTimer = new javax.swing.Timer(60_000, e -> runTimerChecks());
        overstayTimer.setInitialDelay(60_000);
        overstayTimer.start();
    }

    // Gộp 2 tác vụ timer vào 1 SwingWorker, dùng chung 1 lần getAllDonDatBanWithBan()
    private void runTimerChecks() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() {
                List<Ban>       dsBan   = banDAO.getAllBan();
                List<DonDatBan> allDons = ddbDAO.getAllDonDatBanWithBan(); // 1 lần duy nhất
                Date now = new Date();

                // Đánh dấu DaDuocDat khi còn <= 60 phút trước giờ đến
                long sixtyMin = 60 * 60 * 1_000L;
                for (DonDatBan don : allDons) {
                    if (don.isTrangThai()) continue;
                    Date tgDen = don.getThoiGianDen();
                    if (tgDen == null) continue;
                    long diff = tgDen.getTime() - now.getTime();
                    if (diff >= 0 && diff <= sixtyMin) {
                        for (Ban b : don.getDsBan()) {
                            if (b.getTinhTrangBan() == TrangThaiBan.Trong)
                                banDAO.updateTinhTrangBan(b.getMaBan(), TrangThaiBan.DaDuocDat);
                        }
                    }
                }

                // Kiểm tra quá giờ
                List<Object[]> result = new ArrayList<>();
                for (Ban ban : dsBan) {
                    TrangThaiBan status = computeEffectiveStatus(ban, allDons);
                    if (status != TrangThaiBan.DangDuocSuDung) continue;
                    DonDatBan don = findActiveDonFromList(ban.getMaBan(), allDons);
                    if (don == null) continue;
                    Date tgRoi = don.computeThoiGianDuKienRoi();
                    if (tgRoi == null || !now.after(tgRoi)) continue;
                    long overMs = now.getTime() - tgRoi.getTime();
                    Long lastNotify = overstayNotified.get(ban.getMaBan());
                    if (lastNotify != null && (now.getTime() - lastNotify) < OVERSTAY_RENOTIFY_MS) continue;
                    result.add(new Object[]{ban, don, overMs / 60_000L});
                }
                return result;
            }
            @Override protected void done() {
                if (isCancelled()) return;
                try {
                    for (Object[] row : get()) {
                        Ban       ban         = (Ban)       row[0];
                        DonDatBan don         = (DonDatBan) row[1];
                        long      minutesOver = (long)      row[2];
                        overstayNotified.put(ban.getMaBan(), System.currentTimeMillis());
                        showOverstayDialog(ban, don, minutesOver);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void showOverstayDialog(Ban ban, DonDatBan don, long minutesOver) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent instanceof Frame ? (Frame) parent : null,
                "Cảnh báo quá giờ", false);
        dlg.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(10, 12));
        content.setBorder(new EmptyBorder(20, 28, 16, 28));
        content.setBackground(Color.WHITE);

        JLabel iconLbl = new JLabel("⚠", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLbl.setForeground(new Color(0xE67E22));

        String tenBan = "Bàn " + ban.getSoBan();
        String tenKH  = (don.getKhachHang() != null && don.getKhachHang().getTenKH() != null)
                        ? don.getKhachHang().getTenKH() : "Khách";
        JLabel msgLbl = new JLabel(
                "<html><center>"
                + "<b style='font-size:14px'>" + tenBan + " đã quá giờ dự kiến!</b><br><br>"
                + "Khách: <b>" + tenKH + "</b><br>"
                + "Đã quá <b>" + minutesOver + " phút</b>"
                + "</center></html>", SwingConstants.CENTER);
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        content.add(iconLbl, BorderLayout.NORTH);
        content.add(msgLbl,  BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 4, 0));

        JButton btnIgnore  = new JButton("Bỏ qua");
        btnIgnore.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnIgnore.addActionListener(e -> dlg.dispose());

        JButton btnAddDish = new JButton("Gọi thêm món");
        btnAddDish.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddDish.setBackground(new Color(0x27AE60));
        btnAddDish.setForeground(Color.WHITE);
        btnAddDish.setOpaque(true);
        btnAddDish.setBorderPainted(false);
        btnAddDish.addActionListener(e -> {
            dlg.dispose();
            currentBan = ban;
            doAddDish(true);
        });

        btnPanel.add(btnIgnore);
        btnPanel.add(btnAddDish);
        content.add(btnPanel, BorderLayout.SOUTH);

        dlg.add(content);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(340, 220));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel makeTableCard(Ban ban, TrangThaiBan trang, DonDatBan activeDon) {
        boolean isSelected = multiTableMode
                && selectedTables.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
        boolean isVip = "VIP".equalsIgnoreCase(ban.getLoaiBan() != null ? ban.getLoaiBan().trim() : "");
        Color sc = isSelected ? SELECTED_MULTI
                : trang == TrangThaiBan.DaDuocDat      ? AMBER_DAT
                : trang == TrangThaiBan.DangDuocSuDung ? RED_DANG
                : trang == TrangThaiBan.DangDonDep     ? PURPLE_DON
                : GREEN_TRONG;

        boolean[] hovered = {false};

        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? new Color(220, 235, 255) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(isVip ? GOLD_COLOR : (hovered[0] ? sc : BORDER_CLR));
                g2.setStroke(new BasicStroke(isVip ? 2f : (hovered[0] ? 1.5f : 1f)));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(150, 200));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Icon bàn (màu xanh nhà hàng, nền mờ theo trạng thái) ─────────
        QuanLyBan.TableIconPanel iconPanel = new QuanLyBan.TableIconPanel(
                MAIN_BLUE, sc, ban.getSucChua(), isVip, false, GOLD_COLOR);
        iconPanel.setPreferredSize(new Dimension(160, 90));

        // ── Thông tin ─────────────────────────────────────────────────────
        JPanel pInfo = new JPanel();
        pInfo.setLayout(new BoxLayout(pInfo, BoxLayout.Y_AXIS));
        pInfo.setOpaque(false);
        pInfo.setBorder(new EmptyBorder(5, 12, 4, 12));

        JLabel lblNum = new JLabel(isSelected
                ? "<html>Bàn " + ban.getSoBan() + "&nbsp;<font size='3'>✓</font></html>"
                : "Bàn " + ban.getSoBan());
        lblNum.setFont(new Font("Inter Bold", Font.BOLD, 18));
        lblNum.setForeground(isSelected ? SELECTED_MULTI : (isVip ? GOLD_COLOR : MAIN_BLUE));
        lblNum.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblType = new JLabel((isVip ? "VIP" : "Thường") + "  •  " + ban.getSucChua() + " người");
        lblType.setFont(new Font("Segoe UI", isVip ? Font.BOLD : Font.PLAIN, 12));
        lblType.setForeground(isVip ? GOLD_COLOR.darker() : new Color(130, 140, 150));
        lblType.setAlignmentX(Component.LEFT_ALIGNMENT);

        String statusText = isSelected                             ? "Đã chọn"
                : trang == TrangThaiBan.DaDuocDat      ? "Đã đặt trước"
                : trang == TrangThaiBan.DangDuocSuDung ? "Đang sử dụng"
                : trang == TrangThaiBan.DangDonDep     ? "Đang dọn dẹp"
                : "Trống";
        JLabel lblBadge = new JLabel(statusText) {
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
        pInfo.add(Box.createVerticalStrut(2));
        pInfo.add(lblType);
        pInfo.add(Box.createVerticalStrut(5));
        pInfo.add(lblBadge);

        // ── Giờ rời dự kiến ───────────────────────────────────────────────
        String roiText = "";
        if (activeDon != null) {
            java.util.Date tgRoi = activeDon.computeThoiGianDuKienRoi();
            if (tgRoi != null) roiText = "→ " + new SimpleDateFormat("HH:mm").format(tgRoi);
        }
        JLabel lblRoi = new JLabel(roiText);
        lblRoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRoi.setForeground(new Color(130, 140, 150));
        lblRoi.setBorder(new EmptyBorder(0, 12, 8, 12));

        card.add(iconPanel, BorderLayout.NORTH);
        card.add(pInfo,     BorderLayout.CENTER);
        card.add(lblRoi,    BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onCardClick(ban); }
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  card.repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered[0] = false; card.repaint(); }
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
                    else if (t == TrangThaiBan.DangDonDep)
                        renderCleaning(currentBan);
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
            // Kiểm tra cùng loại bàn (Thường / VIP)
            if (!selectedTables.isEmpty()) {
                String loaiCu  = selectedTables.get(0).getLoaiBan();
                String loaiMoi = b.getLoaiBan();
                boolean same = (loaiCu == null && loaiMoi == null)
                        || (loaiCu != null && loaiCu.equalsIgnoreCase(loaiMoi));
                if (!same) {
                    msg("Chỉ được chọn cùng loại bàn.\n"
                            + "Đang chọn: " + (loaiCu != null ? loaiCu : "Thường")
                            + " — Bàn " + b.getSoBan() + " là loại: " + (loaiMoi != null ? loaiMoi : "Thường"));
                    return;
                }
            }
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
            if (lblMultiModeInfo != null) lblMultiModeInfo.setVisible(true);
            if (lockSidebar != null) lockSidebar.run();
            showMultiTableBookingPrompt();
        } else {
            btnMultiTableMode.setText("Chọn nhiều bàn: TẮT");
            btnMultiTableMode.setBackground(new Color(100, 100, 110));
            if (lblMultiModeInfo != null) lblMultiModeInfo.setVisible(false);
            if (unlockSidebar != null) unlockSidebar.run();
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
        boolean walkIn = "Ăn ngay".equals(currentSelectedTime);
        // Tạo QuanLyDatBan_DB với danh sách bàn đã chọn
        QuanLyDatBan_DB bookingPanel = new QuanLyDatBan_DB(
                currentNV,
                new ArrayList<>(selectedTables),
                selectedBookingDate,
                currentSelectedTime,
                walkIn,
                new QuanLyDatBan_DB.BookingListener() {
                    @Override public void onBookingSuccess() {
                        // Tắt multi-mode, reset, refresh
                        multiTableMode = false;
                        selectedTables.clear();
                        btnMultiTableMode.setText("Chọn nhiều bàn: TẮT");
                        btnMultiTableMode.setBackground(new Color(100, 100, 110));
                        if (lblMultiModeInfo != null) lblMultiModeInfo.setVisible(false);
                        if (unlockSidebar != null) unlockSidebar.run();
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
        lblUseKhach.setVisible(false); // ẩn — info nay nằm ở infoBlock bên dưới
        hdr.add(lblUseKhach, BorderLayout.EAST);

        // ── Info block thông tin đơn đặt bàn ────────────────────────────────
        JPanel infoBlock = new JPanel(new GridLayout(4, 2, 8, 4));
        infoBlock.setBackground(Color.WHITE);
        infoBlock.setBorder(new EmptyBorder(8, 14, 6, 14));
        infoBlock.add(infoKey("Khách hàng:"));    infoBlock.add(lblUseInfoKhach = infoVal(""));
        infoBlock.add(infoKey("Số điện thoại:")); infoBlock.add(lblUseInfoSdt   = infoVal(""));
        infoBlock.add(infoKey("Giờ đến:"));       infoBlock.add(lblUseInfoGio   = infoVal(""));
        infoBlock.add(infoKey("Ghi chú:"));       infoBlock.add(lblUseInfoGhiChu = infoVal(""));

        JPanel northArea = new JPanel(new BorderLayout());
        northArea.setBackground(Color.WHITE);
        northArea.add(hdr,       BorderLayout.NORTH);
        northArea.add(infoBlock, BorderLayout.CENTER);
        northArea.add(hsep(),    BorderLayout.SOUTH);
        root.add(northArea, BorderLayout.NORTH);

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
        boolean walkIn = "Ăn ngay".equals(currentSelectedTime);
        QuanLyDatBan_DB bookingPanel = new QuanLyDatBan_DB(
                currentNV,
                java.util.Collections.singletonList(ban),
                selectedBookingDate,
                currentSelectedTime,
                walkIn,
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
        if (don != null && don.getThoiGianDen() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date tgRoi = don.computeThoiGianDuKienRoi();
            String roiStr = tgRoi != null ? new SimpleDateFormat("HH:mm").format(tgRoi) : "?";
            lblResKhung.setText(sdf.format(don.getThoiGianDen()) + " → " + roiStr);
        } else {
            lblResKhung.setText("");
        }

        lblResKhach.setText(fullKH != null ? fullKH.getTenKH() : "—");
        lblResSdt.setText(fullKH != null ? fullKH.getSoDT() : "—");
        lblResGhiChu.setText(don != null && don.getGhiChu() != null && !don.getGhiChu().isEmpty()
                ? don.getGhiChu() : "");

        // Trạng thái "Đã đặt" → cho phép chỉnh sửa toàn bộ món (+/-/xóa)
        boolean paid = (hd != null && hd.getTrangThaiThanhToan() == TrangThaiThanhToan.DA_THANH_TOAN);
        rebuildDishRows(pResDishRows, cths, hd,
                /* canEditAll= */ !paid,
                /* onlyBeverages= */ false,
                /* isPaid= */ paid,
                /* canCancelMon= */ false);

        // Tính tổng
        double total = 0;
        for (ChiTietHoaDon ct : cths) total += ct.getDonGia() * ct.getSoLuong();
        lblResTotal.setText("Tổng: " + FMT.format(total) + "đ");

        rightCard.show(rightPanel, "reserved");
    }

    private void renderUsing(Ban ban, DonDatBan don, HoaDon hd, KhachHang fullKH, List<ChiTietHoaDon> cths) {
        lblUseTitle.setText("ĐANG PHỤC VỤ  —  BÀN " + ban.getSoBan());

        // Thông tin đơn đặt bàn
        lblUseInfoKhach.setText(fullKH != null ? fullKH.getTenKH() : "Khách vãng lai");
        lblUseInfoSdt.setText(fullKH != null && !"0000000000".equals(fullKH.getSoDT())
                ? fullKH.getSoDT() : "—");
        if (don != null && don.getThoiGianDen() != null) {
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
            Date tgRoi = don.computeThoiGianDuKienRoi();
            String roiStr = tgRoi != null ? " → " + new SimpleDateFormat("HH:mm").format(tgRoi) : "";
            lblUseInfoGio.setText(tf.format(don.getThoiGianDen()) + roiStr);
        } else {
            lblUseInfoGio.setText("—");
        }
        lblUseInfoGhiChu.setText(don != null && don.getGhiChu() != null && !don.getGhiChu().isEmpty()
                ? don.getGhiChu() : "—");

        boolean paid = (hd != null && hd.getTrangThaiThanhToan() == TrangThaiThanhToan.DA_THANH_TOAN);

        // Tất cả món CHO_XU_LY đều có nút Hủy; không cho chỉnh sửa số lượng
        rebuildDishRows(pUseDishRows, cths, hd,
                /* canEditAll= */ false,
                /* onlyBeverages= */ false,
                /* isPaid= */ paid,
                /* canCancelMon= */ !paid);

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

    private void renderCleaning(Ban ban) {
        // Tạo panel tạm hiển thị trạng thái dọn dẹp + nút xác nhận xong
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(40, 30, 40, 30));

        JLabel icon = new JLabel("🧹", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JLabel title = new JLabel("BÀN " + ban.getSoBan() + " — ĐANG DỌN DẸP", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(PURPLE_DON);

        JLabel sub = new JLabel("Bàn đã thanh toán, đang được dọn dẹp để phục vụ khách tiếp theo.", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_DARK);

        JButton btnDonXong = new JButton("✓  Dọn xong — Bàn sẵn sàng");
        btnDonXong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDonXong.setBackground(PURPLE_DON);
        btnDonXong.setForeground(Color.WHITE);
        btnDonXong.setFocusPainted(false);
        btnDonXong.setBorderPainted(false);
        btnDonXong.setPreferredSize(new Dimension(0, 46));
        btnDonXong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDonXong.addActionListener(e -> {
            banDAO.updateTinhTrangBan(ban.getMaBan(), TrangThaiBan.Trong);
            loadTableCards();
            showEmpty();
        });

        JPanel pCenter = new JPanel();
        pCenter.setLayout(new BoxLayout(pCenter, BoxLayout.Y_AXIS));
        pCenter.setOpaque(false);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDonXong.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDonXong.setMaximumSize(new Dimension(320, 46));
        pCenter.add(icon);
        pCenter.add(Box.createVerticalStrut(12));
        pCenter.add(title);
        pCenter.add(Box.createVerticalStrut(8));
        pCenter.add(sub);
        pCenter.add(Box.createVerticalStrut(24));
        pCenter.add(btnDonXong);

        p.add(pCenter, BorderLayout.CENTER);

        // Xóa panel dọn dẹp cũ nếu có, tránh tích lũy
        for (Component c : rightPanel.getComponents()) {
            if ("cleaning".equals(c.getName())) { rightPanel.remove(c); break; }
        }
        p.setName("cleaning");
        rightPanel.add(p, "cleaning");
        rightCard.show(rightPanel, "cleaning");
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
                                 boolean onlyBeverages, boolean isPaid, boolean canCancelMon) {
        container.removeAll();
        int stt = 1;
        for (ChiTietHoaDon ct : cths) {
            boolean isBev  = isBeverageCategory(ct);
            boolean canEdit;
            if (isPaid) {
                canEdit = false;
            } else if (canEditAll) {
                canEdit = true;
            } else if (canCancelMon) {
                // "Đang phục vụ": CHO_XU_LY luôn được nhập số; DA_XONG chỉ đồ uống được nhập số
                boolean isCHO  = ct.getTrangThaiMon() == TrangThaiMon.CHO_XU_LY;
                boolean isXONG = ct.getTrangThaiMon() == TrangThaiMon.DA_XONG;
                canEdit = isCHO || (isXONG && isBev);
            } else {
                canEdit = onlyBeverages && isBev;
            }

            JPanel row = buildDishDataRow(stt++, ct, hd, canEdit, canEditAll, container, cths, isPaid, canCancelMon);
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
    private JPanel buildDishDataRow(int stt, ChiTietHoaDon ct, HoaDon hd, boolean canEdit, boolean canDelete, JPanel container, List<ChiTietHoaDon> cths, boolean isPaid, boolean canCancelMon) {
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
        JLabel lName = new JLabel(buildMonNameHtml(ct));
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
                    rebuildDishRows(container, cths, hd, canDelete, !canDelete, isPaid, canCancelMon);
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
                    if (newQty < 1) { txtQty.setText(String.valueOf(ct.getSoLuong())); return; }
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
            if (canDelete) qPanel.add(btnPlus); // chỉ hiện + khi được phép xóa (reserved mode)
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
                    rebuildDishRows(container, cths, hd, true, false, isPaid, canCancelMon);
                    refreshTotals(container, cths, hd);
                });
                dPan.add(btnDel);
                row.add(dPan, g);
            } else if (canCancelMon && ct.getTrangThaiMon() == TrangThaiMon.CHO_XU_LY) {
                // CHO_XU_LY trong "đang phục vụ": vừa nhập số vừa có nút Hủy
                JPanel huyPan2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                huyPan2.setOpaque(false);
                huyPan2.setPreferredSize(new Dimension(55, 24));
                huyPan2.setMinimumSize(new Dimension(55, 24));
                JButton btnHuy2 = new JButton("Hủy");
                btnHuy2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btnHuy2.setPreferredSize(new Dimension(48, 22));
                btnHuy2.setMargin(new Insets(0, 2, 0, 2));
                btnHuy2.setFocusPainted(false);
                btnHuy2.setBackground(new Color(255, 230, 230));
                btnHuy2.setForeground(RED_DANG);
                btnHuy2.setToolTipText("Hủy món đang chờ bếp");
                btnHuy2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                final String maMon2 = ct.getMonAn().getMaMon();
                btnHuy2.addActionListener(ev -> {
                    int confirm = JOptionPane.showConfirmDialog(QuanLyDatBan.this,
                            "Hủy \"" + ct.getMonAn().getTenMon() + "\" (đang chờ bếp xử lý)?",
                            "Hủy món", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    cthdDAO.deleteChiTietCHO_XU_LY(hd.getMaHD(), maMon2);
                    cths.removeIf(c -> maMon2.equals(c.getMonAn().getMaMon())
                            && c.getTrangThaiMon() == TrangThaiMon.CHO_XU_LY);
                    hdDAO.updateTongTien(hd.getMaHD(), recalcTotal(cths));
                    rebuildDishRows(container, cths, hd, canDelete, !canDelete, isPaid, canCancelMon);
                    refreshTotals(container, cths, hd);
                });
                huyPan2.add(btnHuy2);
                row.add(huyPan2, g);
            } else {
                JLabel bLk = new JLabel();
                bLk.setPreferredSize(new Dimension(55, 24));
                bLk.setMinimumSize(new Dimension(55, 24));
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
            if (canCancelMon && ct.getTrangThaiMon() == TrangThaiMon.CHO_XU_LY) {
                JPanel huyPan = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                huyPan.setOpaque(false);
                huyPan.setPreferredSize(new Dimension(55, 24));
                huyPan.setMinimumSize(new Dimension(55, 24));
                JButton btnHuy = new JButton("Hủy");
                btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btnHuy.setPreferredSize(new Dimension(48, 22));
                btnHuy.setMargin(new Insets(0, 2, 0, 2));
                btnHuy.setFocusPainted(false);
                btnHuy.setBackground(new Color(255, 230, 230));
                btnHuy.setForeground(RED_DANG);
                btnHuy.setToolTipText("Hủy món đang chờ bếp");
                btnHuy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                final String maMon = ct.getMonAn().getMaMon();
                btnHuy.addActionListener(ev -> {
                    int confirm = JOptionPane.showConfirmDialog(QuanLyDatBan.this,
                            "Hủy \"" + ct.getMonAn().getTenMon() + "\" (đang chờ bếp xử lý)?",
                            "Hủy món", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                    cthdDAO.deleteChiTietCHO_XU_LY(hd.getMaHD(), maMon);
                    cths.removeIf(c -> maMon.equals(c.getMonAn().getMaMon())
                            && c.getTrangThaiMon() == TrangThaiMon.CHO_XU_LY);
                    hdDAO.updateTongTien(hd.getMaHD(), recalcTotal(cths));
                    rebuildDishRows(container, cths, hd, canDelete, !canDelete, isPaid, canCancelMon);
                    refreshTotals(container, cths, hd);
                });
                huyPan.add(btnHuy);
                row.add(huyPan, g);
            } else {
                JLabel blk2 = new JLabel();
                blk2.setPreferredSize(new Dimension(35, 24));
                blk2.setMinimumSize(new Dimension(35, 24));
                row.add(blk2, g);
            }
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

    /** Tên món kèm badge trạng thái bếp (HTML). Không hiện nếu DA_PHUC_VU. */
    private String buildMonNameHtml(ChiTietHoaDon ct) {
        String name = ct.getMonAn().getTenMon();
        TrangThaiMon ttm = ct.getTrangThaiMon();
        if (ttm == null || ttm == TrangThaiMon.DA_PHUC_VU) return name;
        String color, label;
        switch (ttm) {
            case CHO_XU_LY: color = "#E65100"; label = "chờ bếp";    break;
            case DANG_LAM:  color = "#1565C0"; label = "đang làm";   break;
            case DA_XONG:   color = "#2E7D32"; label = "bếp xong ✓"; break;
            default:        return name;
        }
        return "<html>" + name + " &nbsp;<font color='" + color +
               "' style='font-size:10px'>[" + label + "]</font></html>";
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

        // Xác định có được hoàn cọc không (trước giờ đặt ít nhất GIO_HAN_HOAN_COC tiếng)
        boolean hoanCoc = true;
        if (don != null && don.getThoiGianDen() != null) {
            long millisToStart = don.getThoiGianDen().getTime() - System.currentTimeMillis();
            long giuiHanMs = (long) util.Constants.GIO_HAN_HOAN_COC * 60 * 60 * 1000;
            hoanCoc = millisToStart >= giuiHanMs;
        }

        String gioHen = (don != null && don.getThoiGianDen() != null)
                ? new SimpleDateFormat("HH:mm dd/MM/yyyy").format(don.getThoiGianDen())
                : "";
        String confirmMsg = hoanCoc
                ? "Xác nhận hủy đặt bàn số " + currentBan.getSoBan() + "?\n"
                  + "Tiền cọc sẽ được HOÀN TRẢ cho khách."
                : "Xác nhận hủy đặt bàn số " + currentBan.getSoBan() + "?\n"
                  + "Giờ đặt: " + gioHen + " – đã trong vòng "
                  + util.Constants.GIO_HAN_HOAN_COC + " tiếng.\n"
                  + "Tiền cọc sẽ KHÔNG được hoàn trả.";

        int r = JOptionPane.showConfirmDialog(this, confirmMsg,
                "Hủy đặt bàn", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        if (don != null) {
            try {
                datBanService.huyDatBan(don, hoanCoc);
            } catch (Exception ex) {
                msg("Lỗi khi hủy đặt bàn: " + ex.getMessage());
                return;
            }
        } else if (isToday(selectedBookingDate)) {
            banDAO.updateTinhTrangBan(currentBan.getMaBan(), TrangThaiBan.Trong);
        }

        String resultMsg = hoanCoc
                ? "Đã hủy đặt bàn. Tiền cọc được hoàn trả cho khách."
                : "Đã hủy đặt bàn. Tiền cọc không hoàn (quá hạn " + util.Constants.GIO_HAN_HOAN_COC + " tiếng).";
        msg(resultMsg);
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
        // Kiểm tra giờ đặt đã đến chưa
        DonDatBan preCheck = findActiveDon(currentBan.getMaBan());
        if (preCheck != null && preCheck.getThoiGianDen() != null
                && new Date().before(preCheck.getThoiGianDen())) {
            msg("Chưa tới giờ đặt bàn ("
                    + new SimpleDateFormat("HH:mm dd/MM/yyyy").format(preCheck.getThoiGianDen())
                    + "), không thể check-in!");
            return;
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
                hd, currentBan.getSoBan(), spDAO, cthdDAO, hdDAO,
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

        // Kiểm tra: còn món chưa nấu xong thì chưa cho thanh toán
        List<String> monChuaXong = new ArrayList<>();
        for (ChiTietHoaDon ct : cths) {
            TrangThaiMon tt = ct.getTrangThaiMon();
            if (tt == TrangThaiMon.CHO_XU_LY || tt == TrangThaiMon.DANG_LAM) {
                String ten = ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "Món không rõ";
                monChuaXong.add("  •  " + ten + "  (" + tt.getTenHienThi() + ")");
            }
        }
        if (!monChuaXong.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa thể thanh toán!\n\nCác món sau vẫn chưa được bếp hoàn thành:\n\n"
                    + String.join("\n", monChuaXong)
                    + "\n\nVui lòng chờ bếp hoàn thành trước khi thanh toán.",
                    "Món chưa hoàn thành", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final Ban banRef = currentBan;
        String tenBanStr = don != null && !don.getDsBan().isEmpty()
                ? don.getDsBan().stream()
                        .map(b -> "Bàn " + b.getSoBan())
                        .collect(java.util.stream.Collectors.joining(", "))
                : "Bàn " + banRef.getSoBan();

        ThanhToanDialog payPanel = new ThanhToanDialog(
                hd, cths, tongMon, hdDAO, tenBanStr,
                currentNV,
                () -> {
                    // onSuccess: đặt bàn về "Đang dọn dẹp" → nhân viên xác nhận "Dọn xong" mới thành Trống
                    if (don != null) { don.setTrangThai(true); ddbDAO.updateDonDatBan(don); }
                    if (don != null && !don.getDsBan().isEmpty()) {
                        for (Ban b : don.getDsBan())
                            banDAO.updateTinhTrangBan(b.getMaBan(), TrangThaiBan.DangDonDep);
                    } else {
                        banDAO.updateTinhTrangBan(banRef.getMaBan(), TrangThaiBan.DangDonDep);
                    }
                    showMainCenter();
                },
                this::showMainCenter  // onBack
        );

        // Xóa payment panel cũ nếu còn tồn tại
        for (Component c : centerWrapper.getComponents()) {
            if ("PAYMENT".equals(c.getName())) centerWrapper.remove(c);
        }
        payPanel.setName("PAYMENT");
        centerWrapper.add(payPanel, "PAYMENT");
        centerCardLayout.show(centerWrapper, "PAYMENT");
    }

    private void showMainCenter() {
        centerCardLayout.show(centerWrapper, "MAIN");
        loadTableCards();
        showEmpty();
    }

    // ── time helpers ──────────────────────────────────────────────────────
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

    /** Ghép ngày từ date và giờ từ timeStr ("HH:mm") thành full datetime. */
    private static Date buildBookingDateTime(Date date, String timeStr) {
        if ("Ăn ngay".equals(timeStr)) return new Date();
        String[] parts = timeStr.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

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

    // ── Badge đơn đặt bàn ────────────────────────────────────────────────────

    private void refreshBadge() {
        if (btnBadge == null) return;
        final Date snap = selectedBookingDate;
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() {
                List<DonDatBan> all = ddbDAO.getAllDonDatBanWithBan();
                return countDonByDate(all, snap);
            }
            @Override protected void done() {
                try {
                    int count = get();
                    btnBadge.setText("🔔 " + count);
                    btnBadge.setBackground(count > 0
                            ? new Color(210, 45, 45)
                            : new Color(120, 120, 130));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private int countDonByDate(List<DonDatBan> all, Date selectedDate) {
        if (all == null || selectedDate == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endOfDay = cal.getTime();
        Date startFilter = isToday(selectedDate) ? new Date() : truncateToDay(selectedDate);
        int count = 0;
        for (DonDatBan don : all) {
            if (don.isTrangThai()) continue;
            Date tgDen = don.getThoiGianDen();
            if (tgDen != null && !tgDen.before(startFilter) && !tgDen.after(endOfDay)) count++;
        }
        return count;
    }

    private void startBadgeTimer() {
        badgeRefreshTimer = new javax.swing.Timer(60_000, e -> {
            if (isToday(selectedBookingDate)) refreshBadge();
        });
        badgeRefreshTimer.start();
    }

    private void showBookingListDialog() {
        final Date snap = selectedBookingDate;
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                // Load cả 2 trong 1 background thread
                List<DonDatBan> all = ddbDAO.getAllDonDatBanWithBan();
                List<entity.KhachHang> allKH = khDAO.getAllKhachHang();

                // Build map maKH → KhachHang để tra cứu O(1)
                Map<String, entity.KhachHang> khMap = new HashMap<>();
                if (allKH != null)
                    for (entity.KhachHang kh : allKH)
                        if (kh.getMaKH() != null) khMap.put(kh.getMaKH(), kh);

                if (all == null) return new Object[]{new ArrayList<>(), khMap};
                Calendar cal = Calendar.getInstance();
                cal.setTime(snap);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Date endOfDay = cal.getTime();
                Date startFilter = isToday(snap) ? new Date() : truncateToDay(snap);
                List<DonDatBan> filtered = new ArrayList<>();
                for (DonDatBan don : all) {
                    if (don.isTrangThai()) continue;
                    Date tgDen = don.getThoiGianDen();
                    if (tgDen != null && !tgDen.before(startFilter) && !tgDen.after(endOfDay))
                        filtered.add(don);
                }
                filtered.sort((a, b) -> a.getThoiGianDen().compareTo(b.getThoiGianDen()));
                return new Object[]{filtered, khMap};
            }
            @SuppressWarnings("unchecked")
            @Override protected void done() {
                try {
                    Object[] result = get();
                    buildAndShowBookingDialog(
                            (List<DonDatBan>) result[0],
                            (Map<String, entity.KhachHang>) result[1],
                            snap);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void buildAndShowBookingDialog(List<DonDatBan> filtered,
                                           Map<String, entity.KhachHang> khMap,
                                           Date forDate) {
        String title = "Đơn đặt bàn ngày "
                + new SimpleDateFormat("dd/MM/yyyy").format(forDate);

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this), title,
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] cols = {"Mã đơn", "Giờ đến", "Bàn", "Tên khách", "SĐT", "Số khách"};
        javax.swing.table.DefaultTableModel model =
                new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
        for (DonDatBan don : filtered) {
            // maKH từ mapRow chỉ có mã — tra cứu full object từ khMap
            String maKH = don.getKhachHang() != null ? don.getKhachHang().getMaKH() : null;
            entity.KhachHang kh = (maKH != null && khMap != null) ? khMap.get(maKH) : null;
            model.addRow(new Object[]{
                don.getMaDon(),
                don.getThoiGianDen() != null ? timeFmt.format(don.getThoiGianDen()) : "",
                don.getDanhSachTenBan(),
                kh != null ? kh.getTenKH() : "",
                kh != null ? kh.getSoDT() : "",
                don.getSoLuongKhach()
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);
        table.setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 248, 248));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(65);

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("Không có đơn đặt bàn nào.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(new Color(150, 150, 150));
            dialog.add(empty, BorderLayout.CENTER);
        } else {
            dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        }

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(ev -> dialog.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        south.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR));
        south.add(btnClose);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.setVisible(true);
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
                        // 1. Lấy ngày & giờ từ thoiGianDen
                        Date ngayKhachDat = foundDon.getThoiGianDen();
                        Calendar bookCal = Calendar.getInstance();
                        bookCal.setTime(ngayKhachDat);
                        int bh = bookCal.get(Calendar.HOUR_OF_DAY);
                        int bm = bookCal.get(Calendar.MINUTE) < 30 ? 0 : 30;
                        String foundTime = String.format("%02d:%02d", bh, bm);

                        // 2. Cập nhật ngày trong ComboBox
                        selectedBookingDate = truncateToDay(ngayKhachDat);
                        if (cbNgayDat != null) {
                            for (int i = 0; i < bookingDates.size(); i++) {
                                if (isSameDay(bookingDates.get(i), ngayKhachDat)) {
                                    ActionListener[] listeners = cbNgayDat.getActionListeners();
                                    for (ActionListener al : listeners) cbNgayDat.removeActionListener(al);
                                    cbNgayDat.setSelectedIndex(i);
                                    for (ActionListener al : listeners) cbNgayDat.addActionListener(al);
                                    break;
                                }
                            }
                        }

                        // 3. Cập nhật giờ lọc
                        currentSelectedTime = foundTime;
                        updateTimeFilterSelection();

                        // 4. Load lại sơ đồ và tự động mở thông tin bàn
                        loadTableCards();
                        Ban firstBan = foundDon.getDsBan().isEmpty()
                                ? foundDon.getBan()
                                : foundDon.getDsBan().get(0);
                        if (firstBan != null) onCardClick(firstBan);
                        txtSearchBan.setText("");

                        JOptionPane.showMessageDialog(QuanLyDatBan.this,
                                "Khách có đơn đặt bàn lúc: "
                                        + new SimpleDateFormat("HH:mm dd/MM/yyyy").format(ngayKhachDat),
                                "Đã tự động chuyển đến giờ đặt", JOptionPane.INFORMATION_MESSAGE);

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
        private boolean     highlight   = false;
        private boolean     goldBorder  = false;

        RoundedPanel(int radius, Color fill) {
            this.radius = radius; this.fillColor = fill;
            setOpaque(false);
        }

        void setBorderHighlight(boolean h) { this.highlight = h; }
        void setGoldBorder(boolean g)      { this.goldBorder = g; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (goldBorder) {
                g2.setColor(highlight ? Color.WHITE : GOLD_COLOR);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, radius, radius);
            } else if (highlight) {
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