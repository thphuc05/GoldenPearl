package gui;

import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO;
import dao.KhuVuc_DAO;
import entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class QuanLyHoaDon extends JPanel {

    // ── Filter bar ───────────────────────────────────────────────────────────
    private JTextField        txtSearch;
    private JTextField        txtFromDate;
    private JTextField        txtToDate;
    private JComboBox<String> cmbKhuVuc;
    private JComboBox<String> cmbTrangThai;
    private JComboBox<String> cmbCaLam;
    private JButton           btnSearch;
    private JButton           btnRefresh;

    // ── Action buttons ───────────────────────────────────────────────────────
    private JButton btnPrint;

    // ── Table ────────────────────────────────────────────────────────────────
    private JTable            tableHoaDon;
    private DefaultTableModel modelHoaDon;

    // ── DAOs ─────────────────────────────────────────────────────────────────
    private final HoaDon_DAO        hdDao = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO ctDao = new ChiTietHoaDon_DAO();
    private final KhuVuc_DAO        kvDao = new KhuVuc_DAO();

    // ── Service ───────────────────────────────────────────────────────────────
    private final service.HoaDonService hoaDonService = new service.HoaDonService();

    // ── SwingWorker tracking (race condition fix) ─────────────────────────────
    private SwingWorker<?, ?> currentWorker = null;

    // ── Cache ────────────────────────────────────────────────────────────────
    private Map<String, String> maHDToKhuVuc   = new HashMap<>();
    private Map<String, String> maHDToBan      = new HashMap<>();
    private Map<String, String> maHDToKhungGio = new HashMap<>();
    private List<HoaDon>        cachedHoaDon   = new ArrayList<>();

    // ── Colors ───────────────────────────────────────────────────────────────
    private static final Color TEXT_DARK    = Color.decode("#333333");
    private static final Color BORDER_COLOR = Color.decode("#E0E0E0");
    private static final Color SELECT_BG    = Color.decode("#EBF5FB");
    private static final Color GREEN_STATUS = Color.decode("#27AE60");
    private static final Color RED_STATUS   = Color.decode("#E74C3C");
    private static final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private static final Color GOLD_COLOR   = Color.decode("#C5A059");
    private static final Color ORANGE_COLOR = Color.decode("#E67E22");
    private static final Color PURPLE_COLOR = Color.decode("#8E44AD");

    // ── Formatters ───────────────────────────────────────────────────────────
    private final SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    // ════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════════

    private static final Color BG_LIGHT = Color.decode("#F0F2F5");

    public QuanLyHoaDon() {
        setLayout(new BorderLayout(0, 14));
        setBackground(BG_LIGHT);

        // Header
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(true);
        pHeader.setBackground(MAIN_BLUE);
        pHeader.setBorder(new EmptyBorder(10, 28, 10, 28));
        JLabel lblTitle = new JLabel("QUẢN LÝ HÓA ĐƠN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(GOLD_COLOR);
        JLabel lblSub = new JLabel("Tra cứu và quản lý tất cả hóa đơn");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(180, 200, 220));
        JPanel pTitleBox = new JPanel();
        pTitleBox.setLayout(new BoxLayout(pTitleBox, BoxLayout.Y_AXIS));
        pTitleBox.setOpaque(false);
        pTitleBox.add(lblTitle);
        pTitleBox.add(Box.createVerticalStrut(2));
        pTitleBox.add(lblSub);
        pHeader.add(pTitleBox, BorderLayout.WEST);
        add(pHeader, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 24, 20, 24));
        add(content, BorderLayout.CENTER);

        content.add(createFilterPanel(), BorderLayout.NORTH);
        content.add(createInvoiceListPanel(), BorderLayout.CENTER);
        content.add(createBottomPanel(), BorderLayout.SOUTH);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        txtFromDate.setText(dateSdf.format(cal.getTime()));
        txtToDate.setText(dateSdf.format(new Date(System.currentTimeMillis() + 30L * 86_400_000)));

        bindEvents();
        loadDataFromDB();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI BUILDING
    // ════════════════════════════════════════════════════════════════════════

    private JPanel createFilterPanel() {
        JPanel pFilter = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        pFilter.setOpaque(false);
        pFilter.setBorder(new EmptyBorder(14, 16, 14, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        g.gridy = 0;
        addFilter(pFilter, g, 0,  "Mã HĐ:",       txtSearch     = mkField(110));
        addFilter(pFilter, g, 2,  "Khu vực:",      cmbKhuVuc     = mkCombo(130));
        addFilter(pFilter, g, 4,  "Trạng thái:",   cmbTrangThai  = mkCombo(155));
        for (TrangThaiThanhToan t : TrangThaiThanhToan.values())
            cmbTrangThai.addItem(t.getDisplay());
        cmbTrangThai.insertItemAt("Tất cả", 0);
        cmbTrangThai.setSelectedIndex(0);

        addFilter(pFilter, g, 6,  "Ca làm:",       cmbCaLam      = mkCombo(140));
        cmbCaLam.addItem("Tất cả ca");
        addFilter(pFilter, g, 8,  "Từ ngày:",      wrapDate(txtFromDate = mkReadonlyDate()));
        addFilter(pFilter, g, 10, "Đến ngày:",     wrapDate(txtToDate   = mkReadonlyDate()));

        g.gridx = 12; g.weightx = 0;
        btnSearch = mkBtn("TÌM KIẾM", MAIN_BLUE, Color.WHITE, 120);
        pFilter.add(btnSearch, g);
        return pFilter;
    }

    private void addFilter(JPanel p, GridBagConstraints g, int col, String lbl, Object comp) {
        g.gridx = col; g.weightx = 0;
        p.add(mkLabel(lbl), g);
        g.gridx = col + 1; g.weightx = 0.12;
        p.add(comp instanceof JPanel ? (JPanel) comp : (Component) comp, g);
    }

    private JPanel createInvoiceListPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        // Section header inside card
        JLabel secLbl = new JLabel("  DANH SÁCH HÓA ĐƠN");
        secLbl.setFont(new Font("Inter Bold", Font.BOLD, 15));
        secLbl.setForeground(MAIN_BLUE);
        secLbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 6, 12, 6)));
        panel.add(secLbl, BorderLayout.NORTH);

        String[] cols = {"Mã HĐ", "Ngày Lập", "Mã NV", "Khách Hàng", "Bàn",
                         "Khung Giờ", "Tổng Tiền", "Trạng Thái", "Hình Thức", "Chi Tiết"};
        modelHoaDon = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 9; }
        };
        tableHoaDon = new JTable(modelHoaDon);
        styleTable(tableHoaDon);

        tableHoaDon.getColumnModel().getColumn(0).setPreferredWidth(68);
        tableHoaDon.getColumnModel().getColumn(1).setPreferredWidth(115);
        tableHoaDon.getColumnModel().getColumn(2).setPreferredWidth(75);
        tableHoaDon.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableHoaDon.getColumnModel().getColumn(4).setPreferredWidth(75);
        tableHoaDon.getColumnModel().getColumn(5).setPreferredWidth(115);
        tableHoaDon.getColumnModel().getColumn(6).setPreferredWidth(95);
        tableHoaDon.getColumnModel().getColumn(7).setPreferredWidth(120);
        tableHoaDon.getColumnModel().getColumn(8).setPreferredWidth(100);
        tableHoaDon.getColumnModel().getColumn(9).setPreferredWidth(90);
        tableHoaDon.getColumnModel().getColumn(9).setMaxWidth(100);

        tableHoaDon.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
        tableHoaDon.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer());
        tableHoaDon.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor());

        panel.add(new JScrollPane(tableHoaDon), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        p.setOpaque(false);

        btnRefresh = mkBtn("LÀM MỚI", Color.WHITE, TEXT_DARK, 120);
        btnRefresh.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btnPrint   = mkBtn("IN HÓA ĐƠN", MAIN_BLUE, Color.WHITE, 140);

        p.add(btnRefresh);
        p.add(btnPrint);
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EVENTS
    // ════════════════════════════════════════════════════════════════════════

    private void bindEvents() {
        btnRefresh.addActionListener(e -> resetFiltersAndReload());
        btnSearch.addActionListener(e -> searchHoaDon());
        cmbKhuVuc.addActionListener(e -> applyCurrentFilters());
        cmbTrangThai.addActionListener(e -> applyCurrentFilters());
        cmbCaLam.addActionListener(e -> applyCurrentFilters());
        btnPrint.addActionListener(e -> handlePrint());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PUBLIC
    // ════════════════════════════════════════════════════════════════════════

    public void refreshData() { loadDataFromDB(); }

    // ════════════════════════════════════════════════════════════════════════
    //  DATA LOADING
    // ════════════════════════════════════════════════════════════════════════

    private void loadDataFromDB() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        modelHoaDon.setRowCount(0);
        currentWorker = new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return new Object[]{
                        hdDao.getKhuVucMapForAllHoaDon(),
                        hdDao.getDsBanDisplayForAllHoaDon(),
                        hdDao.getKhungGioMapForAllHoaDon(),
                        hdDao.getAllHoaDon(),
                        kvDao.getAllKhuVuc()
                };
            }
            @Override @SuppressWarnings("unchecked") protected void done() {
                if (isCancelled()) return;
                try {
                    Object[] r = get();
                    maHDToKhuVuc   = (Map<String, String>) r[0];
                    maHDToBan      = (Map<String, String>) r[1];
                    maHDToKhungGio = (Map<String, String>) r[2];
                    List<HoaDon> ds = (List<HoaDon>) r[3];
                    cachedHoaDon = ds != null ? ds : new ArrayList<>();

                    String prevKV = (String) cmbKhuVuc.getSelectedItem();
                    cmbKhuVuc.removeAllItems();
                    cmbKhuVuc.addItem("Tất cả");
                    List<entity.KhuVuc> dsKV = (List<entity.KhuVuc>) r[4];
                    if (dsKV != null) {
                        for (entity.KhuVuc kv : dsKV) cmbKhuVuc.addItem(kv.getTenKV());
                    }
                    if (prevKV != null) cmbKhuVuc.setSelectedItem(prevKV);

                    applyCurrentFilters();
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        currentWorker.execute();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FILTER / SEARCH
    // ════════════════════════════════════════════════════════════════════════

    private void applyCurrentFilters() {
        String selKV = (String) cmbKhuVuc.getSelectedItem();
        String selTT = (String) cmbTrangThai.getSelectedItem();
        List<HoaDon> filtered = hoaDonService.filter(cachedHoaDon, selKV, selTT, maHDToKhuVuc);
        modelHoaDon.setRowCount(0);
        for (HoaDon hd : filtered) addRowToInvoiceTable(hd);
    }

    private void searchHoaDon() {
        String keyword = txtSearch.getText().trim();
        try {
            Date from = dateSdf.parse(txtFromDate.getText());
            Date to   = dateSdf.parse(txtToDate.getText());
            List<HoaDon> result = hoaDonService.searchByKeywordAndDateRange(
                    keyword, toStartOfDay(from).getTime(), toEndOfDay(to).getTime());
            if (!keyword.isEmpty() && result.isEmpty())
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn phù hợp!");
            cachedHoaDon = result;
            applyCurrentFilters();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày!");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ════════════════════════════════════════════════════════════════════════

    private void openDetailForRow(int row) {
        if (row < 0 || row >= modelHoaDon.getRowCount()) return;
        final String maHD = modelHoaDon.getValueAt(row, 0).toString();
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        currentWorker = new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return new Object[]{ hdDao.getHoaDonByMa(maHD), ctDao.getChiTietByMaHD(maHD) };
            }
            @Override @SuppressWarnings("unchecked") protected void done() {
                if (isCancelled()) return;
                try {
                    Object[] r = get();
                    HoaDon hd = (HoaDon) r[0];
                    List<ChiTietHoaDon> dsCT = (List<ChiTietHoaDon>) r[1];
                    if (hd != null) {
                        String tenBan = maHDToBan.getOrDefault(maHD, "");
                        new QuanLyHoaDon_CTHD(SwingUtilities.getWindowAncestor(QuanLyHoaDon.this),
                                hd, dsCT, true, tenBan).setVisible(true);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        currentWorker.execute();
    }

    private void handlePrint() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần in!"); return; }
        String maHD = modelHoaDon.getValueAt(row, 0).toString();

        // Tìm HoaDon trong cache
        HoaDon hd = null;
        for (HoaDon h : cachedHoaDon) {
            if (h.getMaHD().equals(maHD)) { hd = h; break; }
        }
        if (hd == null) { JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu hóa đơn!"); return; }

        final HoaDon hdFinal = hd;
        final String tenBan  = maHDToBan.getOrDefault(maHD, "Chua xac dinh");

        btnPrint.setEnabled(false);
        btnPrint.setText("Dang tao PDF...");

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                java.util.List<entity.ChiTietHoaDon> cths = ctDao.getChiTietByMaHD(maHD);
                util.PdfHoaDon.xuatVaMo(hdFinal, cths, tenBan);
                return null;
            }
            @Override protected void done() {
                btnPrint.setEnabled(true);
                btnPrint.setText("IN HÓA ĐƠN");
                try {
                    get();
                    JOptionPane.showMessageDialog(QuanLyHoaDon.this,
                            "Da xuat PDF thanh cong!\nFile luu tai: output/hoadon/HoaDon_" + maHD + ".pdf",
                            "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(QuanLyHoaDon.this,
                            "Loi tao PDF: " + ex.getCause().getMessage(),
                            "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void resetFiltersAndReload() {
        txtSearch.setText("");
        cmbKhuVuc.setSelectedIndex(0);
        cmbTrangThai.setSelectedIndex(0);
        cmbCaLam.setSelectedIndex(0);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        txtFromDate.setText(dateSdf.format(cal.getTime()));
        txtToDate.setText(dateSdf.format(new Date(System.currentTimeMillis() + 30L * 86_400_000)));
        loadDataFromDB();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TABLE ROW
    // ════════════════════════════════════════════════════════════════════════

    private void addRowToInvoiceTable(HoaDon hd) {
        String ngayGio = "";
        if (hd.getNgayLap() != null) {
            String ngay = dateSdf.format(hd.getNgayLap());
            String gio  = hd.getThoiGian() != null
                    ? hd.getThoiGian().toString().substring(0, 5)
                    : timeSdf.format(hd.getNgayLap());
            ngayGio = ngay + " " + gio;
        }
        String maNV     = hd.getNhanVien()           != null ? hd.getNhanVien().getMaNV()  : "";
        String tenBan   = maHDToBan.getOrDefault(hd.getMaHD(), "");
        String khungGio = maHDToKhungGio.getOrDefault(hd.getMaHD(), "—");
        String ht       = hd.getHinhThucThanhToan()  != null ? hd.getHinhThucThanhToan().getDisplay() : "";

        modelHoaDon.addRow(new Object[]{
                hd.getMaHD(),
                ngayGio,
                maNV,
                hd.getKhachHang() != null
                        ? hd.getKhachHang().getMaKH() + khTen(hd.getKhachHang())
                        : "Khách vãng lai",
                tenBan,
                khungGio,
                String.format("%,.0fđ", hd.getTongTien()),
                hd.getTrangThaiThanhToan().getDisplay(),
                ht,
                "Xem"
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATE HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private Calendar toStartOfDay(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);      c.set(Calendar.MILLISECOND, 0);
        return c;
    }
    private Calendar toEndOfDay(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);      c.set(Calendar.MILLISECOND, 999);
        return c;
    }
    // ════════════════════════════════════════════════════════════════════════
    //  STRING HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private String nvTen(NhanVien nv) {
        return (nv.getTenNV() != null && !nv.getTenNV().isEmpty()) ? " - " + nv.getTenNV() : "";
    }
    private String khTen(KhachHang kh) {
        return (kh.getTenKH() != null && !kh.getTenKH().isEmpty()) ? " – " + kh.getTenKH() : "";
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI FACTORY
    // ════════════════════════════════════════════════════════════════════════

    private JLabel mkLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Inter Bold", Font.BOLD, 15));
        l.setForeground(TEXT_DARK);
        return l;
    }
    private JTextField mkField(int w) {
        JTextField f = new JTextField();
        f.setFont(new Font("Inter", Font.PLAIN, 15));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        f.setPreferredSize(new Dimension(w, 44));
        return f;
    }
    private JComboBox<String> mkCombo(int w) {
        JComboBox<String> c = new JComboBox<>();
        c.setFont(new Font("Inter", Font.PLAIN, 15));
        c.setPreferredSize(new Dimension(w, 44));
        c.setBackground(Color.WHITE);
        return c;
    }
    private JTextField mkReadonlyDate() {
        JTextField f = mkField(110);
        f.setEditable(false);
        f.setHorizontalAlignment(SwingConstants.CENTER);
        return f;
    }
    private JPanel wrapDate(JTextField field) {
        JButton btn = new JButton("📅");
        btn.setPreferredSize(new Dimension(44, 44));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setBackground(Color.WHITE);
        btn.addActionListener(e -> new DatePickerDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), field).setVisible(true));
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }
    private JButton mkBtn(String t, Color bg, Color fg, int w) {
        JButton b = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg.equals(Color.WHITE) ? Color.WHITE
                        : (getModel().isRollover() ? bg.darker() : bg));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (bg.equals(Color.WHITE)) { g2.setColor(BORDER_COLOR); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font("Inter Bold", Font.BOLD, 15));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(w, 46));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        t.setRowHeight(42);
        t.setSelectionBackground(SELECT_BG);
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 15));
        t.getTableHeader().setPreferredSize(new Dimension(0, 44));
        t.getTableHeader().setBackground(new Color(248, 249, 251));
        t.setGridColor(new Color(235, 235, 235));
        t.setBackground(Color.WHITE);
        t.setShowVerticalLines(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INNER CLASSES
    // ════════════════════════════════════════════════════════════════════════

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String s = value == null ? "" : value.toString();
                TrangThaiThanhToan tt = TrangThaiThanhToan.fromDisplay(s);
                switch (tt) {
                    case DA_THANH_TOAN:   setForeground(GREEN_STATUS);  break;
                    case CHUA_THANH_TOAN: setForeground(ORANGE_COLOR);  break;
                    case DA_COC:          setForeground(PURPLE_COLOR);  break;
                    case DA_HUY:          setForeground(RED_STATUS);    break;
                    default:              setForeground(TEXT_DARK);
                }
            } else { setForeground(TEXT_DARK); }
            setFont(new Font("Inter Bold", Font.BOLD, 15));
            return this;
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("Xem");
            setFont(new Font("Inter Bold", Font.BOLD, 14));
            setBackground(GOLD_COLOR);
            setForeground(MAIN_BLUE);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow;

        public ButtonEditor() {
            super(new JCheckBox());
            button = new JButton("Xem");
            button.setFont(new Font("Inter Bold", Font.BOLD, 14));
            button.setBackground(GOLD_COLOR);
            button.setForeground(MAIN_BLUE);
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            setClickCountToStart(1);
            button.addActionListener(e -> {
                fireEditingStopped();
                openDetailForRow(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() { return "Xem"; }
    }

    class DatePickerDialog extends JDialog {
        private final JTextField target;
        private final Calendar   cal;
        private JPanel daysPanel;
        private JLabel monthLabel;

        public DatePickerDialog(JFrame parent, JTextField target) {
            super(parent, "Chọn ngày", true);
            this.target = target;
            cal = Calendar.getInstance();
            try { if (!target.getText().isEmpty()) cal.setTime(dateSdf.parse(target.getText())); }
            catch (Exception ignored) {}
            setSize(300, 330); setLocationRelativeTo(target);
            setLayout(new BorderLayout()); getContentPane().setBackground(Color.WHITE);
            add(createPickerHeader(), BorderLayout.NORTH);
            daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
            daysPanel.setBackground(Color.WHITE);
            daysPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
            add(daysPanel, BorderLayout.CENTER);
            refreshCalendar();
        }
        private JPanel createPickerHeader() {
            JPanel h = new JPanel(new BorderLayout()); h.setBackground(Color.WHITE);
            h.setBorder(new EmptyBorder(4, 4, 4, 4));
            JButton prev = navBtn("<"); JButton next = navBtn(">");
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(new Font("Inter Bold", Font.BOLD, 14));
            prev.addActionListener(e -> { cal.add(Calendar.MONTH, -1); refreshCalendar(); });
            next.addActionListener(e -> { cal.add(Calendar.MONTH,  1); refreshCalendar(); });
            h.add(prev, BorderLayout.WEST); h.add(monthLabel, BorderLayout.CENTER); h.add(next, BorderLayout.EAST);
            return h;
        }
        private JButton navBtn(String t) {
            JButton b = new JButton(t); b.setFocusPainted(false);
            b.setBackground(Color.WHITE); b.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            return b;
        }
        private void refreshCalendar() {
            daysPanel.removeAll();
            monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()));
            for (String d : new String[]{"CN", "T2", "T3", "T4", "T5", "T6", "T7"}) {
                JLabel l = new JLabel(d, SwingConstants.CENTER);
                l.setFont(new Font("Inter Bold", Font.BOLD, 11)); daysPanel.add(l);
            }
            Calendar tmp = (Calendar) cal.clone(); tmp.set(Calendar.DAY_OF_MONTH, 1);
            int off = tmp.get(Calendar.DAY_OF_WEEK) - 1;
            int max = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < off; i++) daysPanel.add(new JLabel(""));
            for (int day = 1; day <= max; day++) {
                final int d = day;
                JButton b = new JButton(String.valueOf(d));
                b.setFont(new Font("Inter", Font.PLAIN, 12)); b.setFocusPainted(false);
                b.setBackground(Color.WHITE); b.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                b.addActionListener(e -> { cal.set(Calendar.DAY_OF_MONTH, d); target.setText(dateSdf.format(cal.getTime())); dispose(); });
                daysPanel.add(b);
            }
            daysPanel.revalidate(); daysPanel.repaint();
        }
    }
}
