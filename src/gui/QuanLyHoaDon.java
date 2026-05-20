package gui;

import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO;
import dao.KhuVuc_DAO;
import entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel quản lý hóa đơn – phiên bản 3.0 (POS Logic).
 *
 * <p>Thay đổi so với v2:
 * <ul>
 *   <li>ĐÃ XÓA nút "Cập nhật trạng thái" – trạng thái tự động theo nghiệp vụ</li>
 *   <li>Nút THANH TOÁN chỉ hiện khi hóa đơn chưa/đang cọc – bị vô hiệu sau khi đã TT</li>
 *   <li>Sau thanh toán: disable Thêm Món, không cho sửa</li>
 *   <li>Trạng thái chỉ để theo dõi, KHÔNG sửa thủ công</li>
 * </ul>
 */
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
    private JButton btnViewDetail;

    // ── Tables ───────────────────────────────────────────────────────────────
    private JTable            tableHoaDon;
    private JTable            tableChiTiet;
    private DefaultTableModel modelHoaDon;
    private DefaultTableModel modelChiTiet;

    // ── Info labels ──────────────────────────────────────────────────────────
    private JLabel lblMaHD, lblNgayLap, lblNhanVien, lblKhachHang;
    private JLabel lblBan, lblTongTien, lblTienCoc, lblTongCong;
    private JLabel lblTrangThai;
    private JLabel lblHinhThuc;
    private JLabel lblCaLam;
    private JLabel lblKhungGio;  // [MỚI] khung giờ đặt bàn

    // ── DAOs ─────────────────────────────────────────────────────────────────
    private final HoaDon_DAO       hdDao  = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO ctDao  = new ChiTietHoaDon_DAO();
    private final KhuVuc_DAO       kvDao  = new KhuVuc_DAO();

    // ── Cache ────────────────────────────────────────────────────────────────
    private Map<String, String> maHDToKhuVuc = new HashMap<>();
    private Map<String, String> maHDToBan    = new HashMap<>();
    private List<HoaDon>        cachedHoaDon = new ArrayList<>();

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

    public QuanLyHoaDon() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel lblTitle = new JLabel("QUẢN LÝ HÓA ĐƠN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 30));
        lblTitle.setForeground(TEXT_DARK);
        add(lblTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Color.WHITE);
        add(content, BorderLayout.CENTER);

        content.add(createFilterPanel(), BorderLayout.NORTH);
        content.add(createCenterPanel(), BorderLayout.CENTER);
        content.add(createBottomPanel(), BorderLayout.SOUTH);

        txtFromDate.setText(dateSdf.format(new Date()));
        txtToDate.setText(dateSdf.format(new Date()));

        bindEvents();
    }

    private String getSlotLabel(String key) {
        if (key == null || key.trim().isEmpty()) return "—";
        switch (key.toUpperCase()) {
            case "SANG":  return "10:00–14:00";
            case "CHIEU": return "15:00–19:00";
            case "TOI":   return "19:30–23:00";
            default:      return key;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI BUILDING
    // ════════════════════════════════════════════════════════════════════════

    private JPanel createFilterPanel() {
        JPanel pFilter = new JPanel(new GridBagLayout());
        pFilter.setBackground(Color.WHITE);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "BỘ LỌC TÌM KIẾM");
        border.setTitleFont(new Font("Inter Bold", Font.BOLD, 13));
        border.setTitleColor(TEXT_DARK);
        pFilter.setBorder(border);

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

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new GridLayout(1, 2, 14, 0));
        center.setBackground(Color.WHITE);
        center.add(createInvoiceListPanel());
        center.add(createInvoiceDetailPanel());
        return center;
    }

    private JPanel createInvoiceListPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Color.WHITE);
        TitledBorder lb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "DANH SÁCH HÓA ĐƠN");
        lb.setTitleFont(new Font("Inter Bold", Font.BOLD, 13));
        lb.setTitleColor(TEXT_DARK);
        left.setBorder(lb);

        String[] cols = {"Mã HĐ", "Ngày Lập", "Khách Hàng", "Bàn", "Khung Giờ", "Trạng Thái", "Hình Thức", "Ca Làm"};
        modelHoaDon = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHoaDon = new JTable(modelHoaDon);
        styleTable(tableHoaDon);
        tableHoaDon.getColumnModel().getColumn(0).setPreferredWidth(68);
        tableHoaDon.getColumnModel().getColumn(1).setPreferredWidth(115);
        tableHoaDon.getColumnModel().getColumn(2).setPreferredWidth(115);
        tableHoaDon.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableHoaDon.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableHoaDon.getColumnModel().getColumn(5).setPreferredWidth(120);
        tableHoaDon.getColumnModel().getColumn(6).setPreferredWidth(100);
        tableHoaDon.getColumnModel().getColumn(7).setPreferredWidth(85);
        tableHoaDon.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        left.add(new JScrollPane(tableHoaDon), BorderLayout.CENTER);
        return left;
    }

    private JPanel createInvoiceDetailPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(Color.WHITE);
        TitledBorder rb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "CHI TIẾT HÓA ĐƠN");
        rb.setTitleFont(new Font("Inter Bold", Font.BOLD, 13));
        rb.setTitleColor(TEXT_DARK);
        right.setBorder(rb);
        right.add(createInfoPanel(), BorderLayout.NORTH);

        String[] colsCT = {"STT", "Tên Món Ăn", "SL", "Đơn Giá", "Thành Tiền"};
        modelChiTiet = new DefaultTableModel(colsCT, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableChiTiet = new JTable(modelChiTiet);
        styleDetailTable(tableChiTiet);

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        tableChiTiet.getColumnModel().getColumn(0).setCellRenderer(centerR);
        tableChiTiet.getColumnModel().getColumn(0).setPreferredWidth(38);
        tableChiTiet.getColumnModel().getColumn(2).setCellRenderer(centerR);
        tableChiTiet.getColumnModel().getColumn(2).setPreferredWidth(38);

        right.add(new JScrollPane(tableChiTiet), BorderLayout.CENTER);
        return right;
    }

    private JPanel createInfoPanel() {
        JPanel info = new JPanel(new GridLayout(12, 1, 0, 3));
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(8, 10, 4, 10));

        lblMaHD      = mkInfoLabel();
        lblNgayLap   = mkInfoLabel();
        lblNhanVien  = mkInfoLabel();
        lblKhachHang = mkInfoLabel();
        lblBan       = mkInfoLabel();
        lblKhungGio  = mkInfoLabel();   // [MỚI]
        lblCaLam     = mkInfoLabel();
        lblTrangThai = mkInfoLabel();
        lblHinhThuc  = mkInfoLabel();
        lblTongTien  = mkInfoLabel();
        lblTienCoc   = mkInfoLabel();
        lblTongCong  = mkInfoLabel();

        info.add(lblMaHD);   info.add(lblNgayLap);  info.add(lblNhanVien);
        info.add(lblKhachHang); info.add(lblBan);   info.add(lblKhungGio);
        info.add(lblCaLam);  info.add(lblTrangThai); info.add(lblHinhThuc);
        info.add(lblTongTien);  info.add(lblTienCoc);  info.add(lblTongCong);

        setInvoiceDetail(null, null);
        return info;
    }

    private JPanel createBottomPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        p.setBackground(Color.WHITE);

        btnRefresh    = mkBtn("LÀM MỚI",    Color.WHITE, TEXT_DARK,   120);
        btnRefresh.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        btnViewDetail = mkBtn("XEM CHI TIẾT", GOLD_COLOR, MAIN_BLUE,  160);
        btnPrint      = mkBtn("IN HÓA ĐƠN",  MAIN_BLUE,  Color.WHITE, 140);

        p.add(btnRefresh);
        p.add(btnViewDetail);
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

        tableHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedDetail();
            }
        });

        btnViewDetail.addActionListener(e -> handleViewDetail());
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
        modelHoaDon.setRowCount(0);
        modelChiTiet.setRowCount(0);
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return new Object[]{
                        hdDao.getKhuVucMapForAllHoaDon(),
                        hdDao.getDsBanDisplayForAllHoaDon(),
                        hdDao.getAllHoaDon()
                };
            }
            @Override @SuppressWarnings("unchecked") protected void done() {
                try {
                    Object[] r = get();
                    maHDToKhuVuc = (Map<String, String>) r[0];
                    maHDToBan    = (Map<String, String>) r[1];
                    List<HoaDon> ds = (List<HoaDon>) r[2];
                    cachedHoaDon = ds != null ? ds : new ArrayList<>();
                    applyCurrentFilters();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FILTER / SEARCH
    // ════════════════════════════════════════════════════════════════════════

    private void applyCurrentFilters() {
        String selKV = (String) cmbKhuVuc.getSelectedItem();
        String selTT = (String) cmbTrangThai.getSelectedItem();

        boolean filterKV = selKV != null && !selKV.equals("Tất cả");
        TrangThaiThanhToan filterTT = resolveTrangThaiFilter(selTT);

        modelHoaDon.setRowCount(0);
        for (HoaDon hd : cachedHoaDon) {
            if (filterKV) {
                String kv = maHDToKhuVuc.get(hd.getMaHD());
                if (!selKV.equals(kv)) continue;
            }
            if (filterTT != null && hd.getTrangThaiThanhToan() != filterTT) continue;
            addRowToInvoiceTable(hd);
        }

        if (modelHoaDon.getRowCount() > 0) {
            tableHoaDon.setRowSelectionInterval(0, 0);
            loadSelectedDetail();
        } else {
            setInvoiceDetail(null, null);
        }
    }

    private TrangThaiThanhToan resolveTrangThaiFilter(String sel) {
        if (sel == null || sel.equals("Tất cả")) return null;
        return TrangThaiThanhToan.fromDisplay(sel);
    }


    private void searchHoaDon() {
        String keyword = txtSearch.getText().trim();
        try {
            Date from = dateSdf.parse(txtFromDate.getText());
            Date to   = dateSdf.parse(txtToDate.getText());
            Calendar cFrom = toStartOfDay(from);
            Calendar cTo   = toEndOfDay(to);

            List<HoaDon> result = new ArrayList<>();
            if (!keyword.isEmpty()) {
                HoaDon hd = hdDao.getHoaDonByMa(keyword);
                if (hd != null && isInDateRange(hd.getNgayLap(), cFrom.getTime(), cTo.getTime()))
                    result.add(hd);
                else
                    JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn phù hợp!");
            } else {
                List<HoaDon> ds = hdDao.getHoaDonByDateRange(cFrom.getTime(), cTo.getTime());
                if (ds != null) result.addAll(ds);
            }
            cachedHoaDon = result;
            applyCurrentFilters();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày!");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DETAIL
    // ════════════════════════════════════════════════════════════════════════

    private void loadSelectedDetail() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) { setInvoiceDetail(null, null); return; }
        final String maHD = modelHoaDon.getValueAt(row, 0).toString();

        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return new Object[]{ hdDao.getHoaDonByMa(maHD), ctDao.getChiTietByMaHD(maHD) };
            }
            @Override @SuppressWarnings("unchecked") protected void done() {
                try {
                    Object[] r = get();
                    setInvoiceDetail((HoaDon) r[0], (List<ChiTietHoaDon>) r[1]);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void setInvoiceDetail(HoaDon hd, List<ChiTietHoaDon> dsCT) {
        if (modelChiTiet != null) modelChiTiet.setRowCount(0);

        if (hd == null) {
            lblMaHD.setText("Mã HĐ: ");         lblNgayLap.setText("Ngày Lập: ");
            lblNhanVien.setText("Nhân Viên: ");  lblKhachHang.setText("Khách Hàng: ");
            lblBan.setText("Bàn: ");             lblKhungGio.setText("Khung Giờ: ");
            lblCaLam.setText("Ca Làm: ");
            lblTrangThai.setText("Trạng Thái: "); lblHinhThuc.setText("Hình Thức TT: ");
            lblTongTien.setText("Tổng Tiền: ");  lblTienCoc.setText("Tiền Cọc: ");
            lblTongCong.setText("Tổng Cộng: ");
            return;
        }

        lblMaHD.setText("Mã HĐ: " + hd.getMaHD());
        String ngay = hd.getNgayLap() != null ? dateSdf.format(hd.getNgayLap()) : "";
        String gio  = hd.getThoiGian() != null
                ? hd.getThoiGian().toString().substring(0, 5)
                : (hd.getNgayLap() != null ? timeSdf.format(hd.getNgayLap()) : "");
        lblNgayLap.setText("Ngày Lập: " + ngay + "  " + gio);

        lblNhanVien.setText("Nhân Viên: " + (hd.getNhanVien() != null
                ? hd.getNhanVien().getMaNV() + " - " + nvTen(hd.getNhanVien()) : ""));
        lblKhachHang.setText("Khách Hàng: " + (hd.getKhachHang() != null
                ? hd.getKhachHang().getMaKH() + khTen(hd.getKhachHang()) : "Khách vãng lai"));

        lblBan.setText("Bàn: " + (hd.getDonDatBan() != null
                ? maHDToBan.getOrDefault(hd.getMaHD(), "") : ""));

        String rawKhungGio = (hd.getDonDatBan() != null) ? hd.getDonDatBan().getKhungGio() : null;
        lblKhungGio.setText("Khung Giờ ĐB: " + getSlotLabel(rawKhungGio));
        lblKhungGio.setForeground(MAIN_BLUE);


        // Trạng thái – chỉ hiển thị, không cho sửa
        TrangThaiThanhToan tt = hd.getTrangThaiThanhToan();
        lblTrangThai.setText("Trạng Thái: " + tt.getDisplay());
        switch (tt) {
            case DA_THANH_TOAN:   lblTrangThai.setForeground(GREEN_STATUS);  break;
            case CHUA_THANH_TOAN: lblTrangThai.setForeground(ORANGE_COLOR);  break;
            case DA_COC:          lblTrangThai.setForeground(PURPLE_COLOR);  break;
            case DA_HUY:          lblTrangThai.setForeground(RED_STATUS);    break;
            default:              lblTrangThai.setForeground(TEXT_DARK);
        }

        HinhThucThanhToan ht = hd.getHinhThucThanhToan();
        lblHinhThuc.setText("Hình Thức TT: " + (ht != null ? ht.getDisplay() : "(chưa có)"));
        lblHinhThuc.setForeground(ht != null ? MAIN_BLUE : Color.GRAY);

        double tongTienMon = 0;
        if (dsCT != null) {
            for (ChiTietHoaDon ct : dsCT) {
                tongTienMon += ct.getThanhTien();
                modelChiTiet.addRow(new Object[]{
                        modelChiTiet.getRowCount() + 1,
                        ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "",
                        ct.getSoLuong(),
                        String.format("%,.0fđ", ct.getDonGia()),
                        String.format("%,.0fđ", ct.getThanhTien())
                });
            }
        }

        double tienCoc = hd.getTienCoc();
        double hieuSo  = tongTienMon - tienCoc;
        lblTongTien.setText("Tổng tiền món: " + String.format("%,.0fđ", tongTienMon));
        lblTienCoc.setText("Tiền đã cọc: "    + String.format("%,.0fđ", tienCoc));

        if (hieuSo < 0) {
            lblTongCong.setText("TIỀN HOÀN LẠI: " + String.format("%,.0fđ", Math.abs(hieuSo)));
            lblTongCong.setForeground(GREEN_STATUS);
        } else {
            lblTongCong.setText("CẦN THANH TOÁN: " + String.format("%,.0fđ", hieuSo));
            lblTongCong.setForeground(RED_STATUS);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ════════════════════════════════════════════════════════════════════════

    private void handleViewDetail() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần xem!"); return; }
        String maHD = modelHoaDon.getValueAt(row, 0).toString();
        HoaDon hd = hdDao.getHoaDonByMa(maHD);
        if (hd != null) {
            List<ChiTietHoaDon> dsCT = ctDao.getChiTietByMaHD(maHD);
            String tenBan = maHDToBan.getOrDefault(maHD, "");
            new QuanLyHoaDon_CTHD(SwingUtilities.getWindowAncestor(this), hd, dsCT, true, tenBan)
                    .setVisible(true);
        }
    }

    private void handlePrint() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần in!"); return; }
        String maHD = modelHoaDon.getValueAt(row, 0).toString();
        Object[] opts = {"Xác nhận", "Hủy"};
        int choice = JOptionPane.showOptionDialog(this,
                "Bạn chắc chắn muốn in hóa đơn " + maHD + "?",
                "Xác nhận in hóa đơn",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opts, opts[0]);
        if (choice == 0)
            JOptionPane.showMessageDialog(this, "In hóa đơn thành công!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetFiltersAndReload() {
        txtSearch.setText("");
        cmbKhuVuc.setSelectedIndex(0);
        cmbTrangThai.setSelectedIndex(0);
        cmbCaLam.setSelectedIndex(0);
        txtFromDate.setText(dateSdf.format(new Date()));
        txtToDate.setText(dateSdf.format(new Date()));
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
        String tenBan  = maHDToBan.getOrDefault(hd.getMaHD(), "");

        String rawKhungGio = (hd.getDonDatBan() != null) ? hd.getDonDatBan().getKhungGio() : null;
        String khungGio = getSlotLabel(rawKhungGio);

        String ht  = hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan().getDisplay() : "";

        modelHoaDon.addRow(new Object[]{
                hd.getMaHD(), ngayGio,
                hd.getKhachHang() != null
                        ? hd.getKhachHang().getMaKH() + khTen(hd.getKhachHang())
                        : "Khách vãng lai",
                tenBan,
                khungGio,
                hd.getTrangThaiThanhToan().getDisplay(),
                ht,
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATE HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private Calendar toStartOfDay(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);      c.set(Calendar.MILLISECOND,0);
        return c;
    }
    private Calendar toEndOfDay(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY,23); c.set(Calendar.MINUTE,59);
        c.set(Calendar.SECOND,59);      c.set(Calendar.MILLISECOND,999);
        return c;
    }
    private boolean isInDateRange(Date date, Date from, Date to) {
        return date != null && !date.before(from) && !date.after(to);
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
        l.setFont(new Font("Inter Bold", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }
    private JTextField mkField(int w) {
        JTextField f = new JTextField();
        f.setFont(new Font("Inter", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        f.setPreferredSize(new Dimension(w, 40));
        return f;
    }
    private JComboBox<String> mkCombo(int w) {
        JComboBox<String> c = new JComboBox<>();
        c.setFont(new Font("Inter", Font.PLAIN, 13));
        c.setPreferredSize(new Dimension(w, 40));
        c.setBackground(Color.WHITE);
        return c;
    }
    private JTextField mkReadonlyDate() {
        JTextField f = mkField(100);
        f.setEditable(false);
        f.setHorizontalAlignment(SwingConstants.CENTER);
        return f;
    }
    private JPanel wrapDate(JTextField field) {
        JButton btn = new JButton("📅");
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setBackground(Color.WHITE);
        btn.addActionListener(e -> new DatePickerDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), field).setVisible(true));
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }
    private JButton mkBtn(String t, Color bg, Color fg, int w) {
        JButton b = new JButton(t);
        b.setFont(new Font("Inter Bold", Font.BOLD, 13));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(w, 44));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JLabel mkInfoLabel() {
        JLabel l = new JLabel();
        l.setFont(new Font("Inter Bold", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }
    private void styleTable(JTable t) {
        t.setFont(new Font("Inter", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setSelectionBackground(SELECT_BG);
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 13));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        t.setGridColor(new Color(235,235,235));
        t.setBackground(Color.WHITE);
        t.setShowVerticalLines(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
    private void styleDetailTable(JTable t) {
        t.setFont(new Font("Inter", Font.PLAIN, 13));
        t.setRowHeight(32);
        t.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 13));
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
        t.setGridColor(new Color(235,235,235));
        t.setBackground(Color.WHITE);
        t.setShowVerticalLines(false);
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
            setFont(new Font("Inter Bold", Font.BOLD, 13));
            return this;
        }
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
            h.setBorder(new EmptyBorder(4,4,4,4));
            JButton prev = navBtn("<"); JButton next = navBtn(">");
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(new Font("Inter Bold", Font.BOLD, 14));
            prev.addActionListener(e -> { cal.add(Calendar.MONTH,-1); refreshCalendar(); });
            next.addActionListener(e -> { cal.add(Calendar.MONTH, 1); refreshCalendar(); });
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
            for (String d : new String[]{"CN","T2","T3","T4","T5","T6","T7"}) {
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
                b.setBackground(Color.WHITE); b.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
                b.addActionListener(e -> { cal.set(Calendar.DAY_OF_MONTH, d); target.setText(dateSdf.format(cal.getTime())); dispose(); });
                daysPanel.add(b);
            }
            daysPanel.revalidate(); daysPanel.repaint();
        }
    }
}