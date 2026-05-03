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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class QuanLyHoaDon extends JPanel {
    private JTextField txtSearch, txtFromDate, txtToDate;
    private JComboBox<String> cmbKhuVuc, cmbTrangThai;
    private JButton btnSearch, btnRefresh, btnPrint, btnViewDetail;
    private JTable tableHoaDon, tableChiTiet;
    private DefaultTableModel modelHoaDon, modelChiTiet;

    private JLabel lblMaHD, lblNgayLap, lblNhanVien, lblKhachHang, lblTongTien, lblTrangThai;

    private final HoaDon_DAO hd_dao = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO ct_dao = new ChiTietHoaDon_DAO();
    private final KhuVuc_DAO kv_dao = new KhuVuc_DAO();

    private Map<String, String> maHDToKhuVuc = new HashMap<>();
    private List<HoaDon> cachedHoaDon = new ArrayList<>();

    private final Color TEXT_DARK    = Color.decode("#333333");
    private final Color BORDER_COLOR = Color.decode("#E0E0E0");
    private final Color SELECT_BG    = Color.decode("#EBF5FB");
    private final Color GREEN_STATUS = Color.decode("#27AE60");
    private final Color RED_STATUS   = Color.decode("#E74C3C");
    private final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private final Color GOLD_COLOR   = Color.decode("#C5A059");

    private final SimpleDateFormat dateTimeSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final SimpleDateFormat dateSdf     = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat timeSdf     = new SimpleDateFormat("HH:mm");

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

        // Load khu vuc
        cmbKhuVuc.addItem("Tất cả");
        for (KhuVuc kv : kv_dao.getAllKhuVuc()) cmbKhuVuc.addItem(kv.getTenKV());

        txtFromDate.setText(dateSdf.format(new Date()));
        txtToDate.setText(dateSdf.format(new Date()));

        bindEvents();
        loadDataFromDB();
    }

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

        // Mã HĐ (thu gọn)
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

        // Tìm kiếm
        gbc.gridx = 10; gbc.weightx = 0;
        btnSearch = mkBtn("TÌM KIẾM", MAIN_BLUE, Color.WHITE, 120);
        pFilter.add(btnSearch, gbc);

        return pFilter;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new GridLayout(1, 2, 14, 0));
        center.setBackground(Color.WHITE);

        // LEFT: list
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Color.WHITE);
        TitledBorder lb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "DANH SÁCH HÓA ĐƠN");
        lb.setTitleFont(new Font("Inter Bold", Font.BOLD, 13));
        lb.setTitleColor(TEXT_DARK);
        left.setBorder(lb);

        String[] colsHD = {"Mã HĐ", "Ngày Lập", "Nhân Viên", "Khách Hàng", "Trạng Thái"};
        modelHoaDon = new DefaultTableModel(colsHD, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableHoaDon = new JTable(modelHoaDon);
        styleTable(tableHoaDon);
        tableHoaDon.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableHoaDon.getColumnModel().getColumn(1).setPreferredWidth(130);
        tableHoaDon.getColumnModel().getColumn(2).setPreferredWidth(90);
        tableHoaDon.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableHoaDon.getColumnModel().getColumn(4).setPreferredWidth(110);

        tableHoaDon.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel,
                    boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val == null ? "" : val.toString();
                if (!sel) {
                    if ("Đã thanh toán".equals(s)) setForeground(GREEN_STATUS);
                    else if ("Chưa thanh toán".equals(s)) setForeground(Color.decode("#E67E22"));
                    else setForeground(RED_STATUS);
                } else {
                    setForeground(TEXT_DARK);
                }
                setFont(new Font("Inter Bold", Font.BOLD, 13));
                return this;
            }
        });

        left.add(new JScrollPane(tableHoaDon), BorderLayout.CENTER);

        // RIGHT: detail
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
        tableChiTiet.getColumnModel().getColumn(0).setPreferredWidth(38);
        tableChiTiet.getColumnModel().getColumn(0).setMaxWidth(42);
        tableChiTiet.getColumnModel().getColumn(2).setPreferredWidth(38);
        tableChiTiet.getColumnModel().getColumn(2).setMaxWidth(48);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableChiTiet.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableChiTiet.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        right.add(new JScrollPane(tableChiTiet), BorderLayout.CENTER);

        center.add(left);
        center.add(right);
        return center;
    }

    private JPanel createInfoPanel() {
        JPanel info = new JPanel(new GridLayout(6, 1, 0, 4));
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(8, 10, 4, 10));

        lblMaHD       = mkInfoLabel();
        lblNgayLap    = mkInfoLabel();
        lblNhanVien   = mkInfoLabel();
        lblKhachHang  = mkInfoLabel();
        lblTongTien   = mkInfoLabel();
        lblTrangThai  = mkInfoLabel();

        info.add(lblMaHD);
        info.add(lblNgayLap);
        info.add(lblNhanVien);
        info.add(lblKhachHang);
        info.add(lblTongTien);
        info.add(lblTrangThai);

        setInvoiceDetail(null, null);
        return info;
    }

    private JPanel createBottomPanel() {
        JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        pControl.setBackground(Color.WHITE);
        btnRefresh    = mkBtn("QUAY LẠI",    Color.WHITE, TEXT_DARK, 130);
        btnRefresh.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btnViewDetail = mkBtn("XEM CHI TIẾT", GOLD_COLOR, MAIN_BLUE, 175);
        btnPrint      = mkBtn("IN HÓA ĐƠN",  MAIN_BLUE,  Color.WHITE, 155);
        pControl.add(btnRefresh);
        pControl.add(btnViewDetail);
        pControl.add(btnPrint);
        return pControl;
    }

    private void bindEvents() {
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbKhuVuc.setSelectedIndex(0);
            cmbTrangThai.setSelectedIndex(0);
            txtFromDate.setText(dateSdf.format(new Date()));
            txtToDate.setText(dateSdf.format(new Date()));
            loadDataFromDB();
        });

        btnSearch.addActionListener(e -> searchHoaDon());

        // Auto-filter when combo changes
        cmbKhuVuc.addActionListener(e -> applyCurrentFilters());
        cmbTrangThai.addActionListener(e -> applyCurrentFilters());

        tableHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedDetail();
        });

        btnViewDetail.addActionListener(e -> {
            int row = tableHoaDon.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần xem!"); return; }
            String maHD = modelHoaDon.getValueAt(row, 0).toString();
            HoaDon hd = hd_dao.getHoaDonByMa(maHD);
            if (hd != null) {
                List<ChiTietHoaDon> dsCT = ct_dao.getChiTietByMaHD(maHD);
                new InvoiceDialog((JFrame) SwingUtilities.getWindowAncestor(this), hd, dsCT, true).setVisible(true);
            }
        });

        btnPrint.addActionListener(e -> {
            int row = tableHoaDon.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn cần in!"); return; }
            String maHD = modelHoaDon.getValueAt(row, 0).toString();
            HoaDon hd = hd_dao.getHoaDonByMa(maHD);
            if (hd != null) {
                List<ChiTietHoaDon> dsCT = ct_dao.getChiTietByMaHD(maHD);
                new InvoiceDialog((JFrame) SwingUtilities.getWindowAncestor(this), hd, dsCT, false).setVisible(true);
            }
        });
    }

    public void refreshData() { loadDataFromDB(); }

    private void loadDataFromDB() {
        modelHoaDon.setRowCount(0);
        modelChiTiet.setRowCount(0);
        maHDToKhuVuc = hd_dao.getKhuVucMapForAllHoaDon();
        List<HoaDon> ds = hd_dao.getAllHoaDon();
        cachedHoaDon = ds != null ? ds : new ArrayList<>();
        applyCurrentFilters();
    }

    private void applyCurrentFilters() {
        String selectedKV = (String) cmbKhuVuc.getSelectedItem();
        String selectedTT = (String) cmbTrangThai.getSelectedItem();
        boolean filterKV = selectedKV != null && !selectedKV.equals("Tất cả");
        Boolean filterTT = null;
        if ("Đã thanh toán".equals(selectedTT))   filterTT = Boolean.TRUE;
        else if ("Chưa thanh toán".equals(selectedTT)) filterTT = Boolean.FALSE;

        modelHoaDon.setRowCount(0);
        for (HoaDon hd : cachedHoaDon) {
            if (filterKV) {
                String kv = maHDToKhuVuc.get(hd.getMaHD());
                if (!selectedKV.equals(kv)) continue;
            }
            if (filterTT != null && hd.isTrangThai() != filterTT) continue;
            addToTable(hd);
        }
        if (modelHoaDon.getRowCount() > 0) {
            tableHoaDon.setRowSelectionInterval(0, 0);
            loadSelectedDetail();
        } else setInvoiceDetail(null, null);
    }

    private void addToTable(HoaDon hd) {
        modelHoaDon.addRow(new Object[]{
                hd.getMaHD(),
                hd.getNgayLap() != null ? dateSdf.format(hd.getNgayLap()) + " " +
                        (hd.getThoiGian() != null ? hd.getThoiGian().toString().substring(0, 5)
                                                  : timeSdf.format(hd.getNgayLap())) : "",
                hd.getNhanVien() != null ? hd.getNhanVien().getMaNV() : "",
                hd.getKhachHang() != null ? hd.getKhachHang().getMaKH() : "Khách vãng lai",
                hd.isTrangThai() ? "Đã thanh toán" : "Chưa thanh toán"
        });
    }

    private void loadSelectedDetail() {
        int row = tableHoaDon.getSelectedRow();
        if (row == -1) { setInvoiceDetail(null, null); modelChiTiet.setRowCount(0); return; }
        String maHD = modelHoaDon.getValueAt(row, 0).toString();
        HoaDon hd = hd_dao.getHoaDonByMa(maHD);
        List<ChiTietHoaDon> dsCT = ct_dao.getChiTietByMaHD(maHD);
        setInvoiceDetail(hd, dsCT);
    }

    private void setInvoiceDetail(HoaDon hd, List<ChiTietHoaDon> dsCT) {
        if (modelChiTiet != null) modelChiTiet.setRowCount(0);
        if (hd == null) {
            lblMaHD.setText("Mã HĐ: ");
            lblNgayLap.setText("Ngày Lập: ");
            lblNhanVien.setText("Nhân Viên: ");
            lblKhachHang.setText("Khách Hàng: ");
            lblTongTien.setText("Tổng Tiền: ");
            lblTrangThai.setText("Trạng Thái: ");
            return;
        }
        lblMaHD.setText("Mã HĐ: " + hd.getMaHD());
        String ngay = hd.getNgayLap() != null ? dateSdf.format(hd.getNgayLap()) : "";
        String gio  = hd.getThoiGian() != null ? hd.getThoiGian().toString().substring(0, 5)
                : (hd.getNgayLap() != null ? timeSdf.format(hd.getNgayLap()) : "");
        lblNgayLap.setText("Ngày Lập: " + ngay + "  " + gio);
        lblNhanVien.setText("Nhân Viên: " + (hd.getNhanVien() != null
                ? hd.getNhanVien().getMaNV() + " - " + hd.getNhanVien().getTenNV() : ""));
        lblKhachHang.setText("Khách Hàng: " + (hd.getKhachHang() != null
                ? hd.getKhachHang().getMaKH() : "Khách vãng lai"));
        lblTongTien.setText("Tổng Tiền: " + String.format("%,.0fđ", hd.getTongTien()));
        boolean paid = hd.isTrangThai();
        lblTrangThai.setText("Trạng Thái: " + (paid ? "Đã thanh toán" : "Chưa thanh toán"));
        lblTrangThai.setForeground(paid ? GREEN_STATUS : Color.decode("#E67E22"));
        if (dsCT == null || modelChiTiet == null) return;
        int stt = 1;
        for (ChiTietHoaDon ct : dsCT) {
            modelChiTiet.addRow(new Object[]{
                    stt++,
                    ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "",
                    ct.getSoLuong(),
                    String.format("%,.0fđ", ct.getDonGia()),
                    String.format("%,.0fđ", ct.getThanhTien())
            });
        }
    }

    private void searchHoaDon() {
        String s = txtSearch.getText().trim();
        try {
            Date from = dateSdf.parse(txtFromDate.getText());
            Date to   = dateSdf.parse(txtToDate.getText());
            Calendar cFrom = Calendar.getInstance(); cFrom.setTime(from);
            cFrom.set(Calendar.HOUR_OF_DAY, 0); cFrom.set(Calendar.MINUTE, 0); cFrom.set(Calendar.SECOND, 0);
            Calendar cTo = Calendar.getInstance(); cTo.setTime(to);
            cTo.set(Calendar.HOUR_OF_DAY, 23); cTo.set(Calendar.MINUTE, 59); cTo.set(Calendar.SECOND, 59);

            List<HoaDon> result = new ArrayList<>();
            if (!s.isEmpty()) {
                HoaDon hd = hd_dao.getHoaDonByMa(s);
                if (hd != null && hd.getNgayLap() != null
                        && !hd.getNgayLap().before(cFrom.getTime()) && !hd.getNgayLap().after(cTo.getTime())) {
                    result.add(hd);
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn phù hợp!");
                }
            } else {
                List<HoaDon> ds = hd_dao.getHoaDonByDateRange(cFrom.getTime(), cTo.getTime());
                if (ds != null) result.addAll(ds);
            }
            cachedHoaDon = result;
            applyCurrentFilters();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày!"); }
    }

    // ---- helpers ----
    private JLabel mkLabel(String text) {
        JLabel l = new JLabel(text);
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
        btn.addActionListener(e -> {
            DatePickerDialog dlg = new DatePickerDialog((JFrame) SwingUtilities.getWindowAncestor(this), field);
            dlg.setVisible(true);
        });
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    private JButton mkBtn(String text, Color bg, Color fg, int w) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(w, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
        t.setGridColor(new Color(235, 235, 235));
        t.setBackground(Color.WHITE);
        t.setShowVerticalLines(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void styleDetailTable(JTable t) {
        t.setFont(new Font("Inter", Font.PLAIN, 13));
        t.setRowHeight(32);
        t.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 13));
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
        t.setGridColor(new Color(235, 235, 235));
        t.setBackground(Color.WHITE);
        t.setShowVerticalLines(false);
    }

    // ---- InvoiceDialog ----
    class InvoiceDialog extends JDialog {
        private final Color R_NAVY  = Color.decode("#0B3D59");
        private final Color R_GOLD  = Color.decode("#C5A059");
        private final Color R_LINE  = new Color(220, 220, 220);
        private final Color R_GREEN = Color.decode("#27AE60");
        private final Color R_BG    = Color.WHITE;

        private JPanel receiptPanel;

        public InvoiceDialog(JFrame parent, HoaDon hd, List<ChiTietHoaDon> dsCT, boolean isDetail) {
            super(parent, isDetail ? "Chi Tiết Hóa Đơn" : "Phiếu Thanh Toán", true);
            setSize(460, 648);
            setLocationRelativeTo(parent);
            setResizable(false);
            setLayout(new BorderLayout());

            receiptPanel = buildReceipt(hd, dsCT, isDetail);

            JScrollPane scroll = new JScrollPane(receiptPanel);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(new Color(240, 242, 245));
            add(scroll, BorderLayout.CENTER);

            JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            bar.setBackground(R_BG);
            bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, R_LINE));

            if (isDetail) {
                JButton btnClose = mkDialogBtn("ĐÓNG", Color.WHITE, TEXT_DARK);
                btnClose.setBorder(BorderFactory.createLineBorder(R_LINE));
                btnClose.addActionListener(e -> dispose());
                bar.add(btnClose);
            } else {
                JButton btnCancel = mkDialogBtn("HỦY", Color.WHITE, TEXT_DARK);
                btnCancel.setBorder(BorderFactory.createLineBorder(R_LINE));
                btnCancel.addActionListener(e -> dispose());
                JButton btnPay = mkDialogBtn("THANH TOÁN", R_NAVY, Color.WHITE);
                btnPay.addActionListener(e -> {
                    JOptionPane.showMessageDialog(InvoiceDialog.this,
                            "In hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                });
                bar.add(btnCancel);
                bar.add(btnPay);
            }
            add(bar, BorderLayout.SOUTH);
        }

        private JPanel buildReceipt(HoaDon hd, List<ChiTietHoaDon> dsCT, boolean isDetail) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(R_BG);
            p.setBorder(new EmptyBorder(10, 24, 10, 24));

            DecimalFormat numFmt = new DecimalFormat("#,###");

            // ── Restaurant header ─────────────────────────────────────────
            p.add(mkCenterLbl("GOLDEN PEARL", 23, R_NAVY, Font.BOLD));
            p.add(Box.createVerticalStrut(2));
            p.add(mkCenterLbl("36 Thích Bửu Đăng, P.Hạnh Thông", 10, new Color(120, 120, 120), Font.PLAIN));
            p.add(mkCenterLbl("Thành phố Hồ Chí Minh", 10, new Color(120, 120, 120), Font.PLAIN));
            p.add(Box.createVerticalStrut(7));
            p.add(mkDashLine());
            p.add(Box.createVerticalStrut(5));

            // ── Receipt type ──────────────────────────────────────────────
            p.add(mkCenterLbl(isDetail ? "CHI TIẾT HÓA ĐƠN" : "PHIẾU THANH TOÁN", 13, R_NAVY, Font.BOLD));
            p.add(Box.createVerticalStrut(5));
            p.add(mkDashLine());
            p.add(Box.createVerticalStrut(7));

            // ── Invoice info ──────────────────────────────────────────────
            p.add(mkInfoRow("Mã hóa đơn", hd.getMaHD()));
            p.add(Box.createVerticalStrut(3));
            p.add(mkInfoRow("Ngày lập", hd.getNgayLap() != null ? dateTimeSdf.format(hd.getNgayLap()) : "—"));
            p.add(Box.createVerticalStrut(3));
            String nvText = hd.getNhanVien() != null
                    ? hd.getNhanVien().getMaNV() + "  –  " + hd.getNhanVien().getTenNV() : "—";
            p.add(mkInfoRow("Nhân viên", nvText));
            p.add(Box.createVerticalStrut(3));
            String khText = hd.getKhachHang() != null ? hd.getKhachHang().getMaKH() : "Khách vãng lai";
            p.add(mkInfoRow("Khách hàng", khText));
            p.add(Box.createVerticalStrut(8));
            p.add(mkDashLine());
            p.add(Box.createVerticalStrut(5));

            // ── Items header ──────────────────────────────────────────────
            p.add(mkItemsHeader());
            p.add(mkSolidLine(R_NAVY));

            // ── Item rows ─────────────────────────────────────────────────
            if (dsCT != null && !dsCT.isEmpty()) {
                int idx = 1;
                for (ChiTietHoaDon ct : dsCT) {
                    String name = ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "";
                    p.add(mkItemRow(idx, name, ct.getSoLuong(),
                            numFmt.format(ct.getDonGia()) + "đ",
                            numFmt.format(ct.getThanhTien()) + "đ",
                            idx % 2 == 0));
                    idx++;
                }
            } else {
                p.add(mkCenterLbl("(Không có chi tiết món)", 11, new Color(160, 160, 160), Font.ITALIC));
            }
            p.add(Box.createVerticalStrut(4));
            p.add(mkSolidLine(R_LINE));
            p.add(Box.createVerticalStrut(8));

            // ── Total ─────────────────────────────────────────────────────
            JPanel totalRow = new JPanel(new BorderLayout());
            totalRow.setOpaque(false);
            totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel kLbl = new JLabel("TỔNG TIỀN");
            kLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            kLbl.setForeground(R_NAVY);
            JLabel vLbl = new JLabel(numFmt.format(hd.getTongTien()) + "đ", SwingConstants.RIGHT);
            vLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            vLbl.setForeground(R_GOLD);
            totalRow.add(kLbl, BorderLayout.WEST);
            totalRow.add(vLbl, BorderLayout.EAST);
            p.add(totalRow);
            p.add(Box.createVerticalStrut(7));

            // ── Status badge (chỉ hiện khi xem chi tiết) ─────────────────
            boolean paid = hd.isTrangThai();
            if (isDetail) {
                JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                statusWrap.setOpaque(false);
                statusWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                JLabel sLbl = new JLabel(paid ? "  ĐÃ THANH TOÁN  " : "  CHƯA THANH TOÁN  ");
                sLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                sLbl.setForeground(Color.WHITE);
                sLbl.setBackground(paid ? R_GREEN : Color.decode("#E67E22"));
                sLbl.setOpaque(true);
                sLbl.setBorder(new EmptyBorder(4, 12, 4, 12));
                statusWrap.add(sLbl);
                p.add(statusWrap);
            }
            p.add(Box.createVerticalStrut(10));
            p.add(mkDashLine());
            p.add(Box.createVerticalStrut(8));

            // ── WiFi (hai dòng, không emoji) ──────────────────────────────
            p.add(mkCenterLbl("WiFi: Golden Pearl", 11, new Color(80, 80, 80), Font.PLAIN));
            p.add(Box.createVerticalStrut(4));
            p.add(mkCenterLbl("Mật khẩu: 123456789", 11, new Color(80, 80, 80), Font.PLAIN));
            p.add(Box.createVerticalStrut(8));
            p.add(mkDashLine());
            p.add(Box.createVerticalStrut(6));

            // ── Footer ────────────────────────────────────────────────────
            p.add(mkCenterLbl("Cảm ơn quý khách!  Hẹn gặp lại tại Golden Pearl", 11, R_GOLD, Font.ITALIC));

            return p;
        }

        private JLabel mkCenterLbl(String text, int size, Color color, int style) {
            JLabel l = new JLabel(text, SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", style, size));
            l.setForeground(color);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            l.setMaximumSize(new Dimension(Integer.MAX_VALUE, size + 12));
            return l;
        }

        private JPanel mkInfoRow(String key, String value) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            JLabel kl = new JLabel(key + ":");
            kl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            kl.setForeground(new Color(140, 140, 140));
            kl.setPreferredSize(new Dimension(105, 20));
            JLabel vl = new JLabel(value);
            vl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            vl.setForeground(new Color(33, 33, 33));
            row.add(kl, BorderLayout.WEST);
            row.add(vl, BorderLayout.CENTER);
            return row;
        }

        private JLabel mkRLabel(String text, int fixedW, int align, Color fg, int style) {
            JLabel l = new JLabel(text, align);
            l.setFont(new Font("Segoe UI", style, 11));
            l.setForeground(fg);
            if (fixedW > 0) {
                l.setMinimumSize(new Dimension(fixedW, 0));
                l.setPreferredSize(new Dimension(fixedW, 20));
                l.setMaximumSize(new Dimension(fixedW, Integer.MAX_VALUE));
            } else {
                l.setMinimumSize(new Dimension(20, 0));
                l.setPreferredSize(new Dimension(60, 20));
                l.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            }
            return l;
        }

        private JPanel mkItemsHeader() {
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setBackground(R_NAVY);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            row.setBorder(new EmptyBorder(5, 4, 5, 4));
            row.add(mkRLabel("#",          22, SwingConstants.CENTER, Color.WHITE, Font.BOLD));
            row.add(Box.createHorizontalStrut(4));
            row.add(mkRLabel("Tên món ăn", -1, SwingConstants.LEFT,   Color.WHITE, Font.BOLD));
            row.add(Box.createHorizontalStrut(4));
            row.add(mkRLabel("SL",         26, SwingConstants.CENTER, Color.WHITE, Font.BOLD));
            row.add(Box.createHorizontalStrut(4));
            row.add(mkRLabel("Đơn giá",    74, SwingConstants.RIGHT,  Color.WHITE, Font.BOLD));
            row.add(Box.createHorizontalStrut(4));
            row.add(mkRLabel("Thành tiền", 84, SwingConstants.RIGHT,  Color.WHITE, Font.BOLD));
            return row;
        }

        private JPanel mkItemRow(int idx, String name, int qty, String price, String total, boolean shaded) {
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setBackground(shaded ? new Color(248, 248, 248) : R_BG);
            row.setOpaque(true);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            row.setBorder(new EmptyBorder(4, 4, 4, 4));
            Color dark = Color.decode("#333333");
            row.add(mkRLabel(String.valueOf(idx), 22, SwingConstants.CENTER, dark,   Font.PLAIN));
            row.add(Box.createHorizontalStrut(4));
            row.add(mkRLabel(name,                -1, SwingConstants.LEFT,   dark,   Font.PLAIN));
            row.add(Box.createHorizontalStrut(4));
            row.add(mkRLabel(String.valueOf(qty), 26, SwingConstants.CENTER, dark,   Font.PLAIN));
            row.add(Box.createHorizontalStrut(4));
            row.add(mkRLabel(price,               74, SwingConstants.RIGHT,  dark,   Font.PLAIN));
            row.add(Box.createHorizontalStrut(4));
            row.add(mkRLabel(total,               84, SwingConstants.RIGHT,  R_NAVY, Font.BOLD));
            return row;
        }

        private JPanel mkDashLine() {
            JPanel line = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(R_LINE);
                    float[] dash = {5f, 4f};
                    g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
                    g2.drawLine(0, 1, getWidth(), 1);
                }
            };
            line.setOpaque(false);
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
            line.setPreferredSize(new Dimension(400, 3));
            return line;
        }

        private JPanel mkSolidLine(Color color) {
            JPanel line = new JPanel();
            line.setBackground(color);
            line.setOpaque(true);
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            line.setPreferredSize(new Dimension(400, 1));
            return line;
        }

        private JButton mkDialogBtn(String text, Color bg, Color fg) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setFocusPainted(false);
            btn.setBorder(new EmptyBorder(8, 22, 8, 22));
            btn.setPreferredSize(new Dimension(0, 36));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }
    }

    class DatePickerDialog extends JDialog {
        private final JTextField target;
        private final Calendar cal;
        private JPanel daysPanel;
        private JLabel monthLabel;

        public DatePickerDialog(JFrame parent, JTextField t) {
            super(parent, "Chọn ngày", true);
            target = t;
            cal = Calendar.getInstance();
            try { if (!t.getText().isEmpty()) cal.setTime(dateSdf.parse(t.getText())); } catch (Exception e2) {}
            setSize(300, 330);
            setLocationRelativeTo(t);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Color.WHITE);
            header.setBorder(new EmptyBorder(4, 4, 4, 4));
            JButton btnP = mkNavBtn("<");
            JButton btnN = mkNavBtn(">");
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(new Font("Inter Bold", Font.BOLD, 14));
            btnP.addActionListener(e -> { cal.add(Calendar.MONTH, -1); refresh(); });
            btnN.addActionListener(e -> { cal.add(Calendar.MONTH, 1); refresh(); });
            header.add(btnP, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(btnN, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
            daysPanel.setBackground(Color.WHITE);
            daysPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
            add(daysPanel, BorderLayout.CENTER);
            refresh();
        }

        private JButton mkNavBtn(String text) {
            JButton b = new JButton(text);
            b.setFocusPainted(false);
            b.setBackground(Color.WHITE);
            b.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            return b;
        }

        private void refresh() {
            daysPanel.removeAll();
            monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()));
            for (String d : new String[]{"CN", "T2", "T3", "T4", "T5", "T6", "T7"}) {
                JLabel l = new JLabel(d, SwingConstants.CENTER);
                l.setFont(new Font("Inter Bold", Font.BOLD, 11));
                daysPanel.add(l);
            }
            Calendar tmp = (Calendar) cal.clone();
            tmp.set(Calendar.DAY_OF_MONTH, 1);
            int start = tmp.get(Calendar.DAY_OF_WEEK) - 1;
            int days  = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < start; i++) daysPanel.add(new JLabel(""));
            for (int i = 1; i <= days; i++) {
                final int day = i;
                JButton b = new JButton(String.valueOf(i));
                b.setFont(new Font("Inter", Font.PLAIN, 12));
                b.setFocusPainted(false);
                b.setBackground(Color.WHITE);
                b.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                b.addActionListener(e -> { cal.set(Calendar.DAY_OF_MONTH, day); target.setText(dateSdf.format(cal.getTime())); dispose(); });
                daysPanel.add(b);
            }
            daysPanel.revalidate(); daysPanel.repaint();
        }
    }
}
