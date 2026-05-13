package gui;

import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO;
import dao.KhuVuc_DAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.KhuVuc;

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
 * Panel quản lý hóa đơn trong hệ thống quản lý nhà hàng.
 *
 * <p>Panel này cung cấp giao diện để:
 * <ul>
 *   <li>Xem danh sách toàn bộ hóa đơn</li>
 *   <li>Tìm kiếm và lọc hóa đơn theo mã, khu vực, trạng thái, ngày</li>
 *   <li>Xem chi tiết từng hóa đơn và các món ăn đã gọi</li>
 *   <li>In hóa đơn</li>
 * </ul>
 *
 * @author Restaurant Management Team
 * @version 1.0
 */
public class QuanLyHoaDon extends JPanel {

    // =========================================================================
    // UI Components – Filter Bar
    // =========================================================================

    /** Ô nhập mã hóa đơn để tìm kiếm nhanh. */
    private JTextField txtSearch;

    /** Ô hiển thị ngày bắt đầu của khoảng lọc (read-only, chọn qua DatePicker). */
    private JTextField txtFromDate;

    /** Ô hiển thị ngày kết thúc của khoảng lọc (read-only, chọn qua DatePicker). */
    private JTextField txtToDate;

    /** ComboBox chọn khu vực để lọc hóa đơn. */
    private JComboBox<String> cmbKhuVuc;

    /** ComboBox chọn trạng thái thanh toán để lọc hóa đơn. */
    private JComboBox<String> cmbTrangThai;

    // =========================================================================
    // UI Components – Buttons
    // =========================================================================

    /** Nút thực hiện tìm kiếm / lọc theo điều kiện đã nhập. */
    private JButton btnSearch;

    /** Nút đặt lại toàn bộ bộ lọc về mặc định và tải lại dữ liệu. */
    private JButton btnRefresh;

    /** Nút in hóa đơn đang được chọn. */
    private JButton btnPrint;

    /** Nút mở dialog xem chi tiết hóa đơn đang được chọn. */
    private JButton btnViewDetail;

    // =========================================================================
    // UI Components – Tables
    // =========================================================================

    /** Bảng hiển thị danh sách hóa đơn (bên trái). */
    private JTable tableHoaDon;

    /** Bảng hiển thị danh sách món ăn trong hóa đơn đang chọn (bên phải). */
    private JTable tableChiTiet;

    /** Model dữ liệu cho {@link #tableHoaDon}. */
    private DefaultTableModel modelHoaDon;

    /** Model dữ liệu cho {@link #tableChiTiet}. */
    private DefaultTableModel modelChiTiet;

    // =========================================================================
    // UI Components – Info Labels (panel chi tiết phía trên bảng món)
    // =========================================================================

    private JLabel lblMaHD;
    private JLabel lblNgayLap;
    private JLabel lblNhanVien;
    private JLabel lblKhachHang;
    private JLabel lblBan;        // [MỚI] hiển thị số bàn trong chi tiết
    private JLabel lblTongTien;
    private JLabel lblTienCoc;
    private JLabel lblTongCong;
    private JLabel lblTrangThai;

    // =========================================================================
    // DAOs
    // =========================================================================

    /** DAO thao tác với bảng HoaDon. */
    private final HoaDon_DAO hdDao = new HoaDon_DAO();

    /** DAO thao tác với bảng ChiTietHoaDon. */
    private final ChiTietHoaDon_DAO ctDao = new ChiTietHoaDon_DAO();

    /** DAO thao tác với bảng KhuVuc. */
    private final KhuVuc_DAO kvDao = new KhuVuc_DAO();

    // =========================================================================
    // State / Cache
    // =========================================================================

    /**
     * Map ánh xạ mã hóa đơn sang tên khu vực.
     * Dùng để lọc theo khu vực mà không cần query lại DB.
     */
    private Map<String, String> maHDToKhuVuc = new HashMap<>();

    /**
     * [MỚI] Map ánh xạ mã hóa đơn sang chuỗi tên bàn (vd: "Bàn 1, Bàn 3").
     * Hỗ trợ cả hóa đơn 1 bàn cũ và nhiều bàn mới.
     */
    private Map<String, String> maHDToBan = new HashMap<>();

    /**
     * Danh sách hóa đơn đã được tải về từ DB (hoặc kết quả tìm kiếm gần nhất).
     * Được dùng làm nguồn cho các bộ lọc phía client.
     */
    private List<HoaDon> cachedHoaDon = new ArrayList<>();

    // =========================================================================
    // Color Constants
    // =========================================================================

    private static final Color TEXT_DARK    = Color.decode("#333333");
    private static final Color BORDER_COLOR = Color.decode("#E0E0E0");
    private static final Color SELECT_BG    = Color.decode("#EBF5FB");
    private static final Color GREEN_STATUS = Color.decode("#27AE60");
    private static final Color RED_STATUS   = Color.decode("#E74C3C");
    private static final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private static final Color GOLD_COLOR   = Color.decode("#C5A059");

    // =========================================================================
    // Date Formatters
    // =========================================================================

    /** Định dạng ngày giờ đầy đủ: {@code dd/MM/yyyy HH:mm}. */
    private final SimpleDateFormat dateTimeSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /** Định dạng chỉ ngày: {@code dd/MM/yyyy}. */
    private final SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy");

    /** Định dạng chỉ giờ: {@code HH:mm}. */
    private final SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Khởi tạo panel quản lý hóa đơn.
     *
     * <p>Thực hiện:
     * <ol>
     *   <li>Dựng toàn bộ giao diện (filter bar, bảng danh sách, panel chi tiết, toolbar)</li>
     *   <li>Đặt ngày mặc định cho bộ lọc là ngày hôm nay</li>
     *   <li>Đăng ký các event listener</li>
     *   <li>Tải danh sách khu vực bất đồng bộ vào ComboBox</li>
     * </ol>
     */
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
        loadKhuVucAsync();
    }

    // =========================================================================
    // UI Building Methods
    // =========================================================================

    /**
     * Tạo panel bộ lọc tìm kiếm phía trên cùng.
     *
     * <p>Bao gồm: ô mã HĐ, combo khu vực, combo trạng thái,
     * ô từ ngày / đến ngày (có DatePicker), và nút TÌM KIẾM.
     *
     * @return panel bộ lọc đã được cấu hình đầy đủ
     */
    private JPanel createFilterPanel() {
        JPanel pFilter = new JPanel(new GridBagLayout());
        pFilter.setBackground(Color.WHITE);

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "BỘ LỌC TÌM KIẾM");
        border.setTitleFont(new Font("Inter Bold", Font.BOLD, 13));
        border.setTitleColor(TEXT_DARK);
        pFilter.setBorder(border);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 6, 10, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mã HĐ
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pFilter.add(mkLabel("Mã HĐ:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.10;
        txtSearch = mkField(110);
        pFilter.add(txtSearch, gbc);

        // Khu vực
        gbc.gridx = 2; gbc.weightx = 0;
        pFilter.add(mkLabel("Khu vực:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.14;
        cmbKhuVuc = mkCombo(130);
        pFilter.add(cmbKhuVuc, gbc);

        // Trạng thái
        gbc.gridx = 4; gbc.weightx = 0;
        pFilter.add(mkLabel("Trạng thái:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.13;
        cmbTrangThai = mkCombo(130);
        cmbTrangThai.addItem("Tất cả");
        cmbTrangThai.addItem("Đã thanh toán");
        cmbTrangThai.addItem("Chưa thanh toán");
        pFilter.add(cmbTrangThai, gbc);

        // Từ ngày
        gbc.gridx = 6; gbc.weightx = 0;
        pFilter.add(mkLabel("Từ ngày:"), gbc);
        gbc.gridx = 7; gbc.weightx = 0.16;
        txtFromDate = mkReadonlyDate();
        pFilter.add(wrapDate(txtFromDate), gbc);

        // Đến ngày
        gbc.gridx = 8; gbc.weightx = 0;
        pFilter.add(mkLabel("Đến ngày:"), gbc);
        gbc.gridx = 9; gbc.weightx = 0.16;
        txtToDate = mkReadonlyDate();
        pFilter.add(wrapDate(txtToDate), gbc);

        // Nút tìm kiếm
        gbc.gridx = 10; gbc.weightx = 0;
        btnSearch = mkBtn("TÌM KIẾM", MAIN_BLUE, Color.WHITE, 120);
        pFilter.add(btnSearch, gbc);

        return pFilter;
    }

    /**
     * Tạo panel trung tâm gồm hai cột: danh sách hóa đơn (trái) và chi tiết hóa đơn (phải).
     *
     * @return panel trung tâm chia đôi (GridLayout 1x2)
     */
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new GridLayout(1, 2, 14, 0));
        center.setBackground(Color.WHITE);
        center.add(createInvoiceListPanel());
        center.add(createInvoiceDetailPanel());
        return center;
    }

    /**
     * Tạo panel bên trái chứa bảng danh sách hóa đơn.
     *
     * @return panel danh sách hóa đơn
     */
    private JPanel createInvoiceListPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Color.WHITE);

        TitledBorder lb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "DANH SÁCH HÓA ĐƠN");
        lb.setTitleFont(new Font("Inter Bold", Font.BOLD, 13));
        lb.setTitleColor(TEXT_DARK);
        left.setBorder(lb);

        String[] cols = {"Mã HĐ", "Ngày Lập", "Nhân Viên", "Khách Hàng", "Bàn", "Trạng Thái"};
        modelHoaDon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tableHoaDon = new JTable(modelHoaDon);
        styleTable(tableHoaDon);
        tableHoaDon.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableHoaDon.getColumnModel().getColumn(1).setPreferredWidth(130);
        tableHoaDon.getColumnModel().getColumn(2).setPreferredWidth(90);
        tableHoaDon.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableHoaDon.getColumnModel().getColumn(4).setPreferredWidth(120); // [MỚI] cột Bàn
        tableHoaDon.getColumnModel().getColumn(5).setPreferredWidth(110);
        tableHoaDon.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer()); // [FIX] Trạng Thái đã đổi sang cột 5

        left.add(new JScrollPane(tableHoaDon), BorderLayout.CENTER);
        return left;
    }

    /**
     * Tạo panel bên phải chứa thông tin tóm tắt và bảng chi tiết món ăn của hóa đơn đang chọn.
     *
     * @return panel chi tiết hóa đơn
     */
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
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tableChiTiet = new JTable(modelChiTiet);
        styleDetailTable(tableChiTiet);
        tableChiTiet.getColumnModel().getColumn(0).setPreferredWidth(38);
        tableChiTiet.getColumnModel().getColumn(0).setMaxWidth(42);
        tableChiTiet.getColumnModel().getColumn(2).setPreferredWidth(38);
        tableChiTiet.getColumnModel().getColumn(2).setMaxWidth(48);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableChiTiet.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableChiTiet.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        right.add(new JScrollPane(tableChiTiet), BorderLayout.CENTER);
        return right;
    }

    /**
     * Tạo panel thông tin tóm tắt (mã HĐ, ngày, nhân viên, khách hàng, tổng tiền, tiền cọc...).
     *
     * @return panel info với 8 label xếp theo GridLayout dọc
     */
    private JPanel createInfoPanel() {
        JPanel info = new JPanel(new GridLayout(8, 1, 0, 4));
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(8, 10, 4, 10));

        lblMaHD      = mkInfoLabel();
        lblNgayLap   = mkInfoLabel();
        lblNhanVien  = mkInfoLabel();
        lblKhachHang = mkInfoLabel();
        lblBan       = mkInfoLabel(); // [MỚI]
        lblTongTien  = mkInfoLabel();
        lblTienCoc   = mkInfoLabel();
        lblTongCong  = mkInfoLabel();
        lblTrangThai = mkInfoLabel();

        info.add(lblMaHD);
        info.add(lblNgayLap);
        info.add(lblNhanVien);
        info.add(lblKhachHang);
        info.add(lblBan);       // [MỚI]
        info.add(lblTongTien);
        info.add(lblTienCoc);
        info.add(lblTongCong);
        info.add(lblTrangThai);

        setInvoiceDetail(null, null);
        return info;
    }

    /**
     * Tạo thanh công cụ phía dưới (QUAY LẠI, XEM CHI TIẾT, IN HÓA ĐƠN).
     *
     * @return panel toolbar căn phải
     */
    private JPanel createBottomPanel() {
        JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        pControl.setBackground(Color.WHITE);

        btnRefresh = mkBtn("QUAY LẠI", Color.WHITE, TEXT_DARK, 130);
        btnRefresh.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        btnViewDetail = mkBtn("XEM CHI TIẾT", GOLD_COLOR, MAIN_BLUE, 175);
        btnPrint      = mkBtn("IN HÓA ĐƠN",  MAIN_BLUE,  Color.WHITE, 155);

        pControl.add(btnRefresh);
        pControl.add(btnViewDetail);
        pControl.add(btnPrint);
        return pControl;
    }

    // =========================================================================
    // Event Binding
    // =========================================================================

    /**
     * Đăng ký tất cả event listener cho các thành phần UI.
     *
     * <ul>
     *   <li>{@link #btnRefresh} – đặt lại bộ lọc và tải lại DB</li>
     *   <li>{@link #btnSearch} – tìm kiếm theo mã và khoảng ngày</li>
     *   <li>{@link #cmbKhuVuc}, {@link #cmbTrangThai} – tự lọc khi thay đổi</li>
     *   <li>{@link #tableHoaDon} selection – tải chi tiết hóa đơn được chọn</li>
     *   <li>{@link #btnViewDetail} – mở dialog {@link QuanLyHoaDon_CTHD}</li>
     *   <li>{@link #btnPrint} – xác nhận rồi in hóa đơn</li>
     * </ul>
     */
    private void bindEvents() {
        btnRefresh.addActionListener(e -> resetFiltersAndReload());
        btnSearch.addActionListener(e -> searchHoaDon());

        // Tự lọc khi người dùng đổi combo
        cmbKhuVuc.addActionListener(e -> applyCurrentFilters());
        cmbTrangThai.addActionListener(e -> applyCurrentFilters());

        tableHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedDetail();
        });

        btnViewDetail.addActionListener(e -> handleViewDetail());
        btnPrint.addActionListener(e -> handlePrint());
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Làm mới dữ liệu – tải lại toàn bộ hóa đơn từ DB.
     *
     * <p>Được gọi từ bên ngoài khi có thay đổi dữ liệu (ví dụ: sau khi thanh toán xong).
     */
    public void refreshData() {
        loadDataFromDB();
    }

    // =========================================================================
    // Data Loading
    // =========================================================================

    /**
     * Tải danh sách khu vực từ DB bất đồng bộ và nạp vào {@link #cmbKhuVuc}.
     */
    private void loadKhuVucAsync() {
        cmbKhuVuc.addItem("Tất cả");
        new SwingWorker<List<KhuVuc>, Void>() {
            @Override
            protected List<KhuVuc> doInBackground() {
                return kvDao.getAllKhuVuc();
            }

            @Override
            protected void done() {
                try {
                    for (KhuVuc kv : get()) {
                        cmbKhuVuc.addItem(kv.getTenKV());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Tải toàn bộ hóa đơn và map khu vực từ DB bất đồng bộ, sau đó áp dụng bộ lọc hiện tại.
     *
     * <p>Kết quả được lưu vào {@link #cachedHoaDon} và {@link #maHDToKhuVuc}.
     */
    private void loadDataFromDB() {
        modelHoaDon.setRowCount(0);
        modelChiTiet.setRowCount(0);

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                Map<String, String> kvMap = hdDao.getKhuVucMapForAllHoaDon();
                Map<String, String> banMap = hdDao.getDsBanDisplayForAllHoaDon(); // [MỚI]
                List<HoaDon> ds = hdDao.getAllHoaDon();
                return new Object[]{kvMap, banMap, ds};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] result = get();
                    maHDToKhuVuc = (Map<String, String>) result[0];
                    maHDToBan    = (Map<String, String>) result[1]; // [MỚI]
                    List<HoaDon> ds = (List<HoaDon>) result[2];
                    cachedHoaDon = ds != null ? ds : new ArrayList<>();
                    applyCurrentFilters();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // =========================================================================
    // Filter / Search Logic
    // =========================================================================

    /**
     * Áp dụng bộ lọc khu vực và trạng thái lên {@link #cachedHoaDon} và hiển thị kết quả.
     *
     * <p>Không gọi DB – chỉ lọc trên cache đã có. Tự chọn hàng đầu tiên nếu có kết quả.
     */
    private void applyCurrentFilters() {
        String selectedKV = (String) cmbKhuVuc.getSelectedItem();
        String selectedTT = (String) cmbTrangThai.getSelectedItem();

        boolean filterByArea = selectedKV != null && !selectedKV.equals("Tất cả");
        Boolean filterByStatus = resolveStatusFilter(selectedTT);

        modelHoaDon.setRowCount(0);

        for (HoaDon hd : cachedHoaDon) {
            if (filterByArea) {
                String kv = maHDToKhuVuc.get(hd.getMaHD());
                if (!selectedKV.equals(kv)) continue;
            }
            if (filterByStatus != null && hd.isTrangThai() != filterByStatus) continue;
            addRowToInvoiceTable(hd);
        }

        if (modelHoaDon.getRowCount() > 0) {
            tableHoaDon.setRowSelectionInterval(0, 0);
            loadSelectedDetail();
        } else {
            setInvoiceDetail(null, null);
        }
    }

    /**
     * Chuyển đổi chuỗi trạng thái từ ComboBox sang giá trị {@code Boolean} để lọc.
     *
     * @param selectedTT chuỗi trạng thái được chọn
     * @return {@code Boolean.TRUE} nếu "Đã thanh toán", {@code Boolean.FALSE} nếu "Chưa thanh toán",
     *         {@code null} nếu "Tất cả"
     */
    private Boolean resolveStatusFilter(String selectedTT) {
        if ("Đã thanh toán".equals(selectedTT))    return Boolean.TRUE;
        if ("Chưa thanh toán".equals(selectedTT))  return Boolean.FALSE;
        return null;
    }

    /**
     * Tìm kiếm hóa đơn theo mã HĐ (nếu có) kết hợp khoảng ngày từ {@link #txtFromDate} đến {@link #txtToDate}.
     *
     * <p>Kết quả được lưu vào {@link #cachedHoaDon} và bộ lọc hiện tại được áp dụng lại.
     * Hiển thị thông báo nếu không tìm thấy hoặc định dạng ngày không hợp lệ.
     */
    private void searchHoaDon() {
        String keyword = txtSearch.getText().trim();
        try {
            Date fromDate = dateSdf.parse(txtFromDate.getText());
            Date toDate   = dateSdf.parse(txtToDate.getText());

            Calendar cFrom = toStartOfDay(fromDate);
            Calendar cTo   = toEndOfDay(toDate);

            List<HoaDon> result = new ArrayList<>();

            if (!keyword.isEmpty()) {
                HoaDon hd = hdDao.getHoaDonByMa(keyword);
                if (hd != null && isInDateRange(hd.getNgayLap(), cFrom.getTime(), cTo.getTime())) {
                    result.add(hd);
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn phù hợp!");
                }
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

    // =========================================================================
    // Detail Loading & Display
    // =========================================================================

    /**
     * Tải chi tiết hóa đơn đang được chọn trong {@link #tableHoaDon} bất đồng bộ.
     *
     * <p>Lấy mã HĐ từ hàng đang chọn, truy vấn DB để lấy đầy đủ thông tin
     * {@link HoaDon} và danh sách {@link ChiTietHoaDon}, rồi cập nhật UI.
     */
    private void loadSelectedDetail() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) {
            setInvoiceDetail(null, null);
            modelChiTiet.setRowCount(0);
            return;
        }

        final String maHD = modelHoaDon.getValueAt(row, 0).toString();

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                HoaDon hd = hdDao.getHoaDonByMa(maHD);
                List<ChiTietHoaDon> dsCT = ctDao.getChiTietByMaHD(maHD);
                return new Object[]{hd, dsCT};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] result = get();
                    setInvoiceDetail((HoaDon) result[0], (List<ChiTietHoaDon>) result[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Cập nhật panel thông tin và bảng chi tiết món ăn theo hóa đơn được truyền vào.
     *
     * <p>Nếu {@code hd} là {@code null}, tất cả label được xóa trắng và bảng được làm sạch.
     * Tính toán hiệu số giữa tổng tiền món và tiền cọc để hiển thị
     * "TIỀN HOÀN LẠI" (xanh lá) hoặc "CẦN THANH TOÁN" (đỏ).
     *
     * @param hd   hóa đơn cần hiển thị, hoặc {@code null} để xóa trắng
     * @param dsCT danh sách chi tiết món ăn tương ứng, hoặc {@code null}
     */
    private void setInvoiceDetail(HoaDon hd, List<ChiTietHoaDon> dsCT) {
        if (modelChiTiet != null) modelChiTiet.setRowCount(0);

        if (hd == null) {
            lblMaHD.setText("Mã HĐ: ");
            lblNgayLap.setText("Ngày Lập: ");
            lblNhanVien.setText("Nhân Viên: ");
            lblKhachHang.setText("Khách Hàng: ");
            lblBan.setText("Bàn: ");            // [MỚI]
            lblTongTien.setText("Tổng Tiền: ");
            lblTienCoc.setText("Tiền Cọc: ");
            lblTongCong.setText("Tổng Cộng: ");
            lblTrangThai.setText("Trạng Thái: ");
            return;
        }

        lblMaHD.setText("Mã HĐ: " + hd.getMaHD());

        String ngay = hd.getNgayLap() != null ? dateSdf.format(hd.getNgayLap()) : "";
        String gio  = hd.getThoiGian() != null
                ? hd.getThoiGian().toString().substring(0, 5)
                : (hd.getNgayLap() != null ? timeSdf.format(hd.getNgayLap()) : "");
        lblNgayLap.setText("Ngày Lập: " + ngay + "  " + gio);

        lblNhanVien.setText("Nhân Viên: " + (hd.getNhanVien() != null
                ? hd.getNhanVien().getMaNV() + " - " + hd.getNhanVien().getTenNV() : ""));

        lblKhachHang.setText("Khách Hàng: " + (hd.getKhachHang() != null
                ? hd.getKhachHang().getMaKH()
                  + (hd.getKhachHang().getTenKH() != null && !hd.getKhachHang().getTenKH().isEmpty()
                     ? " – " + hd.getKhachHang().getTenKH() : "")
                : "Khách vãng lai"));

        // [MỚI] Hiển thị số bàn từ map đã load sẵn
        String tenBanDetail = hd.getDonDatBan() != null
                ? maHDToBan.getOrDefault(hd.getMaHD(), "")
                : "";
        lblBan.setText("Bàn: " + tenBanDetail);

        // Tính tổng tiền món
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
        lblTienCoc.setText("Tiền đã cọc: "   + String.format("%,.0fđ", tienCoc));

        if (hieuSo < 0) {
            lblTongCong.setText("TIỀN HOÀN LẠI: " + String.format("%,.0fđ", Math.abs(hieuSo)));
            lblTongCong.setForeground(new Color(46, 204, 113));
        } else {
            lblTongCong.setText("CẦN THANH TOÁN: " + String.format("%,.0fđ", hieuSo));
            lblTongCong.setForeground(new Color(231, 76, 60));
        }
    }

    // =========================================================================
    // Action Handlers
    // =========================================================================

    /**
     * Đặt lại toàn bộ bộ lọc về mặc định (hôm nay, tất cả khu vực, tất cả trạng thái)
     * và tải lại dữ liệu từ DB.
     */
    private void resetFiltersAndReload() {
        txtSearch.setText("");
        cmbKhuVuc.setSelectedIndex(0);
        cmbTrangThai.setSelectedIndex(0);
        txtFromDate.setText(dateSdf.format(new Date()));
        txtToDate.setText(dateSdf.format(new Date()));
        loadDataFromDB();
    }

    /**
     * Xử lý sự kiện nút XEM CHI TIẾT: mở dialog {@link QuanLyHoaDon_CTHD} cho hóa đơn đang chọn.
     *
     * <p>Hiển thị cảnh báo nếu chưa chọn hàng nào.
     */
    private void handleViewDetail() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần xem!");
            return;
        }
        String maHD = modelHoaDon.getValueAt(row, 0).toString();
        HoaDon hd = hdDao.getHoaDonByMa(maHD);
        if (hd != null) {
            List<ChiTietHoaDon> dsCT = ctDao.getChiTietByMaHD(maHD);
            String tenBanCTHD = maHDToBan.getOrDefault(maHD, ""); // [MỚI] truyền tên bàn
            new QuanLyHoaDon_CTHD(SwingUtilities.getWindowAncestor(this), hd, dsCT, true, tenBanCTHD)
                    .setVisible(true);
        }
    }

    /**
     * Xử lý sự kiện nút IN HÓA ĐƠN: hiển thị hộp thoại xác nhận trước khi in.
     *
     * <p>Hiển thị cảnh báo nếu chưa chọn hàng nào.
     */
    private void handlePrint() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần in!");
            return;
        }
        String maHD = modelHoaDon.getValueAt(row, 0).toString();
        Object[] options = {"Xác nhận", "Hủy"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Bạn chắc chắn muốn in hóa đơn " + maHD + "?",
                "Xác nhận in hóa đơn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            JOptionPane.showMessageDialog(this, "In hóa đơn thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    // Table Row Helpers
    // =========================================================================

    /**
     * Thêm một hàng đại diện cho {@link HoaDon} vào {@link #modelHoaDon}.
     *
     * @param hd hóa đơn cần thêm vào bảng
     */
    private void addRowToInvoiceTable(HoaDon hd) {
        String ngayGio = "";
        if (hd.getNgayLap() != null) {
            String ngay = dateSdf.format(hd.getNgayLap());
            String gio  = hd.getThoiGian() != null
                    ? hd.getThoiGian().toString().substring(0, 5)
                    : timeSdf.format(hd.getNgayLap());
            ngayGio = ngay + " " + gio;
        }

        // [MỚI] Lấy chuỗi tên bàn từ map; fallback nếu không có trong map
        String tenBan = maHDToBan.getOrDefault(hd.getMaHD(), "");
        modelHoaDon.addRow(new Object[]{
                hd.getMaHD(),
                ngayGio,
                hd.getNhanVien()  != null ? hd.getNhanVien().getMaNV()  : "",
                hd.getKhachHang() != null
                        ? hd.getKhachHang().getMaKH()
                          + (hd.getKhachHang().getTenKH() != null && !hd.getKhachHang().getTenKH().isEmpty()
                             ? " – " + hd.getKhachHang().getTenKH() : "")
                        : "Khách vãng lai",
                tenBan,          // [MỚI] cột Bàn
                hd.isTrangThai() ? "Đã thanh toán" : "Chưa thanh toán"
        });
    }

    // =========================================================================
    // Date Utility Helpers
    // =========================================================================

    /**
     * Tạo {@link Calendar} đại diện cho đầu ngày (00:00:00) của {@code date}.
     *
     * @param date ngày cần xử lý
     * @return Calendar được đặt về 00:00:00
     */
    private Calendar toStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Tạo {@link Calendar} đại diện cho cuối ngày (23:59:59) của {@code date}.
     *
     * @param date ngày cần xử lý
     * @return Calendar được đặt về 23:59:59
     */
    private Calendar toEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }

    /**
     * Kiểm tra xem {@code date} có nằm trong khoảng [{@code from}, {@code to}] không.
     *
     * @param date  ngày cần kiểm tra
     * @param from  mốc bắt đầu (inclusive)
     * @param to    mốc kết thúc (inclusive)
     * @return {@code true} nếu date không null và nằm trong khoảng
     */
    private boolean isInDateRange(Date date, Date from, Date to) {
        return date != null && !date.before(from) && !date.after(to);
    }

    // =========================================================================
    // UI Factory Helpers
    // =========================================================================

    /**
     * Tạo một {@link JLabel} tiêu đề với font và màu chuẩn.
     *
     * @param text nội dung hiển thị
     * @return label đã được style
     */
    private JLabel mkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter Bold", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    /**
     * Tạo một {@link JTextField} với chiều rộng ưu tiên cho trước.
     *
     * @param width chiều rộng ưu tiên (px)
     * @return text field đã được style
     */
    private JTextField mkField(int width) {
        JTextField f = new JTextField();
        f.setFont(new Font("Inter", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        f.setPreferredSize(new Dimension(width, 40));
        return f;
    }

    /**
     * Tạo một {@link JComboBox} rỗng với chiều rộng ưu tiên cho trước.
     *
     * @param width chiều rộng ưu tiên (px)
     * @return combo box đã được style
     */
    private JComboBox<String> mkCombo(int width) {
        JComboBox<String> c = new JComboBox<>();
        c.setFont(new Font("Inter", Font.PLAIN, 13));
        c.setPreferredSize(new Dimension(width, 40));
        c.setBackground(Color.WHITE);
        return c;
    }

    /**
     * Tạo một text field chỉ đọc dùng để hiển thị ngày (người dùng chọn qua DatePicker).
     *
     * @return text field read-only đã được style
     */
    private JTextField mkReadonlyDate() {
        JTextField f = mkField(100);
        f.setEditable(false);
        f.setHorizontalAlignment(SwingConstants.CENTER);
        return f;
    }

    /**
     * Bọc một text field ngày cùng với nút lịch (📅) mở {@link DatePickerDialog}.
     *
     * @param field text field chứa ngày đã chọn
     * @return panel tổ hợp gồm field và nút mở DatePicker
     */
    private JPanel wrapDate(JTextField field) {
        JButton btn = new JButton("📅");
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setBackground(Color.WHITE);
        btn.addActionListener(e -> {
            DatePickerDialog dlg = new DatePickerDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), field);
            dlg.setVisible(true);
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    /**
     * Tạo một {@link JButton} với màu nền, màu chữ và chiều rộng cho trước.
     *
     * @param text  nhãn nút
     * @param bg    màu nền
     * @param fg    màu chữ
     * @param width chiều rộng ưu tiên (px)
     * @return button đã được style
     */
    private JButton mkBtn(String text, Color bg, Color fg, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(width, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Tạo một {@link JLabel} dùng trong panel thông tin chi tiết hóa đơn.
     *
     * @return label đã được style (font bold, màu TEXT_DARK)
     */
    private JLabel mkInfoLabel() {
        JLabel l = new JLabel();
        l.setFont(new Font("Inter Bold", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    /**
     * Áp dụng style chung cho bảng danh sách hóa đơn.
     *
     * @param t bảng cần style
     */
    private void styleTable(JTable t) {
        t.setFont(new Font("Inter", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setSelectionBackground(SELECT_BG);
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 13));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        t.setGridColor(new Color(235, 235, 235));
        t.setBackground(Color.WHITE);
        t.setShowVerticalLines(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    /**
     * Áp dụng style chung cho bảng chi tiết món ăn.
     *
     * @param t bảng cần style
     */
    private void styleDetailTable(JTable t) {
        t.setFont(new Font("Inter", Font.PLAIN, 13));
        t.setRowHeight(32);
        t.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 13));
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
        t.setGridColor(new Color(235, 235, 235));
        t.setBackground(Color.WHITE);
        t.setShowVerticalLines(false);
    }

    // =========================================================================
    // Inner Classes
    // =========================================================================

    /**
     * Cell renderer tô màu cột "Trạng Thái" trong bảng danh sách hóa đơn.
     *
     * <ul>
     *   <li>"Đã thanh toán" → xanh lá ({@link QuanLyHoaDon#GREEN_STATUS})</li>
     *   <li>"Chưa thanh toán" → cam (#E67E22)</li>
     *   <li>Giá trị khác → đỏ ({@link QuanLyHoaDon#RED_STATUS})</li>
     * </ul>
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        private static final Color COLOR_ORANGE = Color.decode("#E67E22");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value == null ? "" : value.toString();
            if (!isSelected) {
                if ("Đã thanh toán".equals(status))    setForeground(GREEN_STATUS);
                else if ("Chưa thanh toán".equals(status)) setForeground(COLOR_ORANGE);
                else setForeground(RED_STATUS);
            } else {
                setForeground(TEXT_DARK);
            }
            setFont(new Font("Inter Bold", Font.BOLD, 13));
            return this;
        }
    }

    /**
     * Dialog chọn ngày dạng lịch tháng (calendar picker) dùng cho các ô bộ lọc ngày.
     *
     * <p>Hiển thị lưới các ngày trong tháng; người dùng click vào ngày
     * thì giá trị được ghi vào {@code target} theo định dạng {@code dd/MM/yyyy}.
     */
    class DatePickerDialog extends JDialog {

        /** Text field nhận kết quả ngày được chọn. */
        private final JTextField target;

        /** Calendar dùng để điều hướng tháng/năm và lưu ngày đang chọn. */
        private final Calendar cal;

        /** Panel chứa các nút ngày (được dựng lại mỗi khi đổi tháng). */
        private JPanel daysPanel;

        /** Label hiển thị tháng/năm hiện tại ở giữa header. */
        private JLabel monthLabel;

        /**
         * Tạo DatePickerDialog và khởi tạo giao diện.
         *
         * @param parent cửa sổ cha
         * @param target text field nhận kết quả
         */
        public DatePickerDialog(JFrame parent, JTextField target) {
            super(parent, "Chọn ngày", true);
            this.target = target;
            cal = Calendar.getInstance();

            // Khởi tạo calendar từ giá trị hiện tại của field (nếu có)
            try {
                if (!target.getText().isEmpty()) {
                    cal.setTime(dateSdf.parse(target.getText()));
                }
            } catch (Exception ignored) { }

            setSize(300, 330);
            setLocationRelativeTo(target);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            add(createPickerHeader(), BorderLayout.NORTH);

            daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
            daysPanel.setBackground(Color.WHITE);
            daysPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
            add(daysPanel, BorderLayout.CENTER);

            refreshCalendar();
        }

        /**
         * Tạo header của dialog gồm nút tháng trước (&lt;), label tháng/năm, nút tháng sau (&gt;).
         *
         * @return panel header đã cấu hình
         */
        private JPanel createPickerHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Color.WHITE);
            header.setBorder(new EmptyBorder(4, 4, 4, 4));

            JButton btnPrev = mkNavBtn("<");
            JButton btnNext = mkNavBtn(">");
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(new Font("Inter Bold", Font.BOLD, 14));

            btnPrev.addActionListener(e -> { cal.add(Calendar.MONTH, -1); refreshCalendar(); });
            btnNext.addActionListener(e -> { cal.add(Calendar.MONTH,  1); refreshCalendar(); });

            header.add(btnPrev,    BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(btnNext,    BorderLayout.EAST);
            return header;
        }

        /**
         * Tạo nút điều hướng tháng (prev/next).
         *
         * @param text nhãn nút ("<" hoặc ">")
         * @return button đã được style
         */
        private JButton mkNavBtn(String text) {
            JButton b = new JButton(text);
            b.setFocusPainted(false);
            b.setBackground(Color.WHITE);
            b.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            return b;
        }

        /**
         * Làm mới lưới ngày theo tháng/năm hiện tại trong {@link #cal}.
         *
         * <p>Xóa toàn bộ nội dung cũ, hiển thị tiêu đề ngày trong tuần,
         * rồi dựng lại các nút ngày. Click vào nút ngày sẽ cập nhật {@link #target}
         * và đóng dialog.
         */
        private void refreshCalendar() {
            daysPanel.removeAll();
            monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()));

            // Header ngày trong tuần
            for (String dayName : new String[]{"CN", "T2", "T3", "T4", "T5", "T6", "T7"}) {
                JLabel l = new JLabel(dayName, SwingConstants.CENTER);
                l.setFont(new Font("Inter Bold", Font.BOLD, 11));
                daysPanel.add(l);
            }

            // Padding ngày đầu tháng
            Calendar tmp = (Calendar) cal.clone();
            tmp.set(Calendar.DAY_OF_MONTH, 1);
            int startOffset = tmp.get(Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = 0; i < startOffset; i++) {
                daysPanel.add(new JLabel(""));
            }

            // Nút từng ngày
            for (int day = 1; day <= daysInMonth; day++) {
                final int selectedDay = day;
                JButton b = new JButton(String.valueOf(day));
                b.setFont(new Font("Inter", Font.PLAIN, 12));
                b.setFocusPainted(false);
                b.setBackground(Color.WHITE);
                b.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                b.addActionListener(e -> {
                    cal.set(Calendar.DAY_OF_MONTH, selectedDay);
                    target.setText(dateSdf.format(cal.getTime()));
                    dispose();
                });
                daysPanel.add(b);
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }
}