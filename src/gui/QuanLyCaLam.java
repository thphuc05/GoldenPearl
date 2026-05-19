package gui;

import dao.CaLam_DAO;
import dao.LichLamViec_DAO;
import dao.NhanVien_DAO;
import entity.CaLam;
import entity.LichLamViec;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Panel Quản Lý Ca Làm – phiên bản 5.0 (Realtime Auto Status).
 *
 * <p>Thay đổi so với v4:
 * <ul>
 *   <li>XÓA ComboBox chọn trạng thái thủ công – trạng thái tự tính từ giờ hệ thống</li>
 *   <li>Label trạng thái màu tự cập nhật mỗi 30 giây bằng javax.swing.Timer</li>
 *   <li>Không lưu trangThai vào DB – tính realtime từ gioBatDau/gioKetThuc</li>
 *   <li>Sửa giờ ca chỉ qua popup admin (không chỉnh trực tiếp trên form)</li>
 *   <li>Form chính chỉ dùng để xem + thêm lịch phân công</li>
 * </ul>
 */
public class QuanLyCaLam extends JPanel {

    // ── Colors đồng bộ hệ thống ──────────────────────────────────────────
    private static final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private static final Color GOLD_COLOR   = Color.decode("#C5A059");
    private static final Color TEXT_DARK    = Color.decode("#333333");
    private static final Color BORDER_CLR   = Color.decode("#E0E0E0");
    private static final Color SELECT_BG    = Color.decode("#EBF5FB");
    private static final Color GREEN_OPEN   = Color.decode("#27AE60");
    private static final Color ORANGE_SOON  = Color.decode("#E67E22");
    private static final Color GRAY_CLOSED  = Color.decode("#95A5A6");
    private static final Color RED_BTN      = Color.decode("#E74C3C");
    private static final Color GREEN_BTN    = Color.decode("#27AE60");
    private static final Color CYAN_BTN     = Color.decode("#1ABC9C");
    private static final Color CAL_HEADER   = Color.decode("#F0F4F8");
    private static final Color CAL_TODAY    = Color.decode("#FFF9C4");
    private static final Color CAL_SHIFT_BG = Color.decode("#FFFDE7");
    private static final Color CAL_SHIFT_BRD = Color.decode("#F9A825");
    private static final Color INPUT_BG     = Color.WHITE;
    private static final Color GRAY_TEXT    = new Color(120, 120, 120);

    // ── Form fields ───────────────────────────────────────────────────────
    private JTextField txtMaCa;
    private JTextField txtMaNV;
    private JTextField txtTenNhanVien;
    private JTextField txtNgayLam;
    private JComboBox<CaLam> cmbCaLam;

    // ── Bộ lọc ───────────────────────────────────────────────────────────
    private JTextField        txtSearchNV;
    private JTextField        txtFilterDate;
    private JComboBox<String> cmbFilterRange;
    private JButton           btnXem;

    // ── Bảng danh sách ca ────────────────────────────────────────────────
    private JTable            tableCalLam;
    private DefaultTableModel modelCaLam;

    // ── Lịch tuần ────────────────────────────────────────────────────────
    private JPanel weekCalendarPanel;
    private Date   currentWeekStart;

    // ── Buttons ───────────────────────────────────────────────────────────
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;

    // ── DAOs ──────────────────────────────────────────────────────────────
    private final CaLam_DAO       caDao   = new CaLam_DAO();
    private final LichLamViec_DAO lichDao = new LichLamViec_DAO();
    private final NhanVien_DAO    nvDao   = new NhanVien_DAO();

    // ── Cache ─────────────────────────────────────────────────────────────
    private List<LichLamViec> cachedLich    = new ArrayList<>();
    private List<NhanVien>    dsNhanVien    = new ArrayList<>();
    private List<CaLam>       dsCaLam      = new ArrayList<>();

    // ── Auto-refresh timer ────────────────────────────────────────────────

    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy");

    // ════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════════

    public QuanLyCaLam() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 14, 10, 14));

        currentWeekStart = getMonday(new Date());

        add(buildTopFilterBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftForm(), buildRightContent());
        split.setDividerLocation(240);
        split.setDividerSize(4);
        split.setResizeWeight(0);
        split.setBorder(null);
        split.setBackground(Color.WHITE);
        add(split, BorderLayout.CENTER);


        loadDataAsync();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TOP FILTER BAR
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildTopFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));

        txtSearchNV = mkFieldPlaceholder(140, "Tìm kiếm nhân viên...");

        cmbFilterRange = new JComboBox<>(new String[]{"Hôm nay", "Tuần này", "Tháng này"});
        styleCombo(cmbFilterRange, 120);

        txtFilterDate = mkField(100);
        txtFilterDate.setText(dateFmt.format(new Date()));
        txtFilterDate.setEditable(false);
        txtFilterDate.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnPickDate = mkIconBtn("📅");
        btnPickDate.addActionListener(e -> showDatePicker(txtFilterDate, d -> {
            currentWeekStart = getMonday(d);
            renderWeekCalendar();
        }));

        btnXem = mkBtn("Xem", MAIN_BLUE, Color.WHITE, 80);
        btnXem.addActionListener(e -> handleXem());

        bar.add(txtSearchNV);
        bar.add(Box.createHorizontalStrut(4));
        bar.add(cmbFilterRange);
        bar.add(txtFilterDate);
        bar.add(btnPickDate);
        bar.add(Box.createHorizontalStrut(4));
        bar.add(btnXem);
        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LEFT FORM – chỉ nhập mã ca, mã NV, ngày làm
    //  (KHÔNG có spinner giờ, KHÔNG có combobox trạng thái)
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildLeftForm() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setPreferredSize(new Dimension(240, 0));

        JLabel lbl = new JLabel("THÔNG TIN CA LÀM");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(MAIN_BLUE);
        lbl.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_CLR),
                new EmptyBorder(8, 10, 8, 10)));
        root.add(lbl, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets  = new Insets(4, 0, 4, 0);

        // Khởi tạo ComboBox thay cho JTextField txtMaCa
        cmbCaLam = new JComboBox<>();
        // Đổ dữ liệu từ danh sách ca hiện có
        for (CaLam c : dsCaLam) {
            cmbCaLam.addItem(c);
        }
        cmbCaLam.setPreferredSize(new Dimension(0, 32));
        cmbCaLam.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtMaNV        = mkField(0);
        txtTenNhanVien = mkField(0);
        txtTenNhanVien.setEditable(false);
        txtTenNhanVien.setBackground(new Color(248, 248, 248));
        txtNgayLam     = mkField(0);
        txtNgayLam.setText(dateFmt.format(new Date()));

        txtMaNV.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { lookupNhanVien(); }
        });
        txtMaNV.addActionListener(e -> lookupNhanVien());

        JButton btnPickNgay = mkIconBtn("📅");
        btnPickNgay.addActionListener(e -> showDatePicker(txtNgayLam, null));
        JPanel ngayPanel = new JPanel(new BorderLayout(2, 0));
        ngayPanel.setBackground(Color.WHITE);
        ngayPanel.add(txtNgayLam, BorderLayout.CENTER);
        ngayPanel.add(btnPickNgay, BorderLayout.EAST);

        int row = 0;
        addFormRow(form, g, row++, "Chọn ca:",        cmbCaLam);
        addFormRow(form, g, row++, "Mã nhân viên:",  txtMaNV);
        addFormRow(form, g, row++, "Tên nhân viên:", txtTenNhanVien);
        addFormRow(form, g, row++, "Ngày làm:",      ngayPanel);

        // Spacer
        g.gridy = row * 2; g.weighty = 1;
        form.add(Box.createVerticalGlue(), g);
        g.weighty = 0;

        root.add(new JScrollPane(form,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        root.add(buildFormButtons(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildFormButtons() {
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(10, 10, 10, 10)));

        btnThem   = mkBtn("Thêm",    GREEN_BTN,  Color.WHITE, 0);
        btnSua    = mkBtn("Sửa",     GOLD_COLOR, MAIN_BLUE,   0);
        btnXoa    = mkBtn("Xóa",     RED_BTN,    Color.WHITE, 0);
        btnLamMoi = mkBtn("Làm mới", CYAN_BTN,   Color.WHITE, 0);

        p.add(btnThem); p.add(btnSua);
        p.add(btnXoa);  p.add(btnLamMoi);

        btnThem.addActionListener(e -> handleThem());
        btnSua.addActionListener(e -> handleSua());
        btnXoa.addActionListener(e -> handleXoa());
        btnLamMoi.addActionListener(e -> clearForm());
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RIGHT CONTENT
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildRightContent() {
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(0, 10, 0, 0));
        right.add(buildTablePanel(), BorderLayout.CENTER);
        right.add(buildWeekCalendarSection(), BorderLayout.SOUTH);
        return right;
    }

    // ── Bảng danh sách – KHÔNG có cột trạng thái thủ công ────────────────

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.add(mkSectionTitle("DANH SÁCH CA LÀM"), BorderLayout.NORTH);

        // Cột Trạng thái hiển thị realtime (tính theo giờ HT)
        String[] cols = {"Mã ca", "Mã NV", "Tên nhân viên", "Ngày làm", "Giờ bắt đầu", "Giờ kết thúc"};
        modelCaLam = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCalLam = new JTable(modelCaLam);
        styleTable(tableCalLam);
        tableCalLam.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableCalLam.getColumnModel().getColumn(1).setPreferredWidth(60);
        tableCalLam.getColumnModel().getColumn(2).setPreferredWidth(130);
        tableCalLam.getColumnModel().getColumn(3).setPreferredWidth(90);
        tableCalLam.getColumnModel().getColumn(4).setPreferredWidth(85);
        tableCalLam.getColumnModel().getColumn(5).setPreferredWidth(85);

        tableCalLam.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });

        // Double-click → mở popup sửa giờ ca (admin only)
        tableCalLam.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openEditCaPopup();
            }
        });

        JScrollPane scroll = new JScrollPane(tableCalLam);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.setPreferredSize(new Dimension(0, 200));
        p.add(scroll, BorderLayout.CENTER);

        // Ghi chú hướng dẫn
        JLabel hint = new JLabel("  💡 Double-click vào hàng để sửa giờ ca (admin)");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(GRAY_TEXT);
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    // ── Lịch tuần ────────────────────────────────────────────────────────

    private JPanel buildWeekCalendarSection() {
        JPanel section = new JPanel(new BorderLayout(0, 4));
        section.setBackground(Color.WHITE);
        section.setPreferredSize(new Dimension(0, 420));

        section.add(mkSectionTitle("LỊCH LÀM TRONG TUẦN"), BorderLayout.NORTH);

        weekCalendarPanel = new JPanel(new GridLayout(1, 7, 4, 0));
        weekCalendarPanel.setBackground(Color.WHITE);
        renderWeekCalendar();

        JScrollPane scroll = new JScrollPane(weekCalendarPanel);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    private void renderWeekCalendar() {
        weekCalendarPanel.removeAll();

        String[] dayLabels = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        Date today = new Date();

        for (int i = 0; i < 7; i++) {
            Calendar dayCal = Calendar.getInstance();
            dayCal.setTime(currentWeekStart);
            dayCal.add(Calendar.DAY_OF_MONTH, i);
            Date dayDate = dayCal.getTime();
            boolean isToday = isSameDay(dayDate, today);

            JPanel col = new JPanel();
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
            col.setBackground(isToday ? CAL_TODAY : Color.WHITE);
            col.setBorder(BorderFactory.createLineBorder(BORDER_CLR));

            JLabel lblDay = new JLabel(
                    dayLabels[i] + " (" + new SimpleDateFormat("dd/MM").format(dayDate) + ")",
                    SwingConstants.CENTER);
            lblDay.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblDay.setForeground(isToday ? MAIN_BLUE : TEXT_DARK);
            lblDay.setOpaque(true);
            lblDay.setBackground(isToday ? new Color(227, 242, 253) : CAL_HEADER);
            lblDay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            lblDay.setPreferredSize(new Dimension(0, 28));
            lblDay.setAlignmentX(Component.CENTER_ALIGNMENT);
            col.add(lblDay);

            for (LichLamViec l : getLichByDate(dayDate)) {
                col.add(Box.createVerticalStrut(4));
                col.add(buildShiftCard(l));
            }
            col.add(Box.createVerticalGlue());
            weekCalendarPanel.add(col);
        }
        weekCalendarPanel.revalidate();
        weekCalendarPanel.repaint();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  POPUP SỬA GIỜ CA (admin only)
    // ════════════════════════════════════════════════════════════════════════

    private void openEditCaPopup() {
        int row = tableCalLam.getSelectedRow();
        if (row < 0 || row >= cachedLich.size()) return;
        LichLamViec l = cachedLich.get(row);
        if (l.getCaLam() == null) return;
        CaLam ca = caDao.getCaLamByMa(l.getCaLam().getMaCa());
        if (ca == null) return;

        // ── Popup dialog sửa giờ ──────────────────────────────────────────
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Sửa giờ ca: " + ca.getTenCa(), true);
        dlg.setSize(320, 240);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        g.insets = new Insets(6, 4, 6, 4);

        SpinnerDateModel mBD = new SpinnerDateModel();
        JSpinner spnBD = new JSpinner(mBD);
        spnBD.setEditor(new JSpinner.DateEditor(spnBD, "HH:mm"));
        if (ca.getGioBatDau()  != null) spnBD.setValue(ca.getGioBatDau());
        styleSpinner(spnBD);

        SpinnerDateModel mKT = new SpinnerDateModel();
        JSpinner spnKT = new JSpinner(mKT);
        spnKT.setEditor(new JSpinner.DateEditor(spnKT, "HH:mm"));
        if (ca.getGioKetThuc() != null) spnKT.setValue(ca.getGioKetThuc());
        styleSpinner(spnKT);

        JTextField txtTen = mkField(0);
        txtTen.setText(ca.getTenCa());

        g.gridy = 0; g.gridx = 0; form.add(new JLabel("Tên ca:"), g);
        g.gridy = 1;               form.add(txtTen, g);
        g.gridy = 2; g.gridx = 0; form.add(new JLabel("Giờ bắt đầu:"), g);
        g.gridy = 3;               form.add(spnBD, g);
        g.gridy = 4; g.gridx = 0; form.add(new JLabel("Giờ kết thúc:"), g);
        g.gridy = 5;               form.add(spnKT, g);
        dlg.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setBackground(Color.WHITE);
        JButton btnCancel = mkBtn("Hủy",   Color.WHITE, TEXT_DARK, 80);
        btnCancel.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        JButton btnSave   = mkBtn("Lưu",   MAIN_BLUE,   Color.WHITE, 80);
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            ca.setTenCa(txtTen.getText().trim());
            ca.setGioBatDau(new Time(((Date) spnBD.getValue()).getTime()));
            ca.setGioKetThuc(new Time(((Date) spnKT.getValue()).getTime()));
            if (caDao.update(ca)) {
                // Cập nhật cache
                for (int i = 0; i < dsCaLam.size(); i++)
                    if (dsCaLam.get(i).getMaCa().equals(ca.getMaCa())) { dsCaLam.set(i, ca); break; }
                for (LichLamViec ll : cachedLich)
                    if (ll.getCaLam() != null && ll.getCaLam().getMaCa().equals(ca.getMaCa()))
                        ll.setCaLam(ca);
                populateTable(cachedLich);
                renderWeekCalendar();
                JOptionPane.showMessageDialog(dlg, "✅ Đã cập nhật giờ ca!");
            }
            dlg.dispose();
        });
        btns.add(btnCancel); btns.add(btnSave);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATA LOADING
    // ════════════════════════════════════════════════════════════════════════

    private void loadDataAsync() {
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return new Object[]{ nvDao.getAllNhanVien(), lichDao.getAll(), caDao.getAllCaLam() };
            }
            @Override @SuppressWarnings("unchecked") protected void done() {
                try {
                    Object[] r = get();
                    dsNhanVien = (List<NhanVien>) r[0];
                    cachedLich = (List<LichLamViec>) r[1];
                    dsCaLam    = (List<CaLam>) r[2];

                    // --- THÊM ĐOẠN NÀY ĐỂ ĐỔ DỮ LIỆU VÀO COMBOBOX ---
                    cmbCaLam.removeAllItems();
                    for (CaLam c : dsCaLam) {
                        cmbCaLam.addItem(c);
                    }
                    // ---------------------------------------------------

                    populateTable(cachedLich);
                    renderWeekCalendar();
                    clearForm();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void populateTable(List<LichLamViec> ds) {
        modelCaLam.setRowCount(0);
        for (LichLamViec l : ds) {
            String maNV = l.getNhanVien() != null ? l.getNhanVien().getMaNV() : "";
            String ten  = l.getNhanVien() != null ? nullSafe(l.getNhanVien().getTenNV()) : "";
            String maCa = l.getCaLam()    != null ? l.getCaLam().getMaCa() : "";
            String ngay = l.getNgayLam()  != null ? dateFmt.format(l.getNgayLam()) : "";
            String gbd  = (l.getCaLam() != null && l.getCaLam().getGioBatDau()  != null)
                    ? l.getCaLam().getGioBatDau().toString().substring(0, 5)  : "";
            String gkt  = (l.getCaLam() != null && l.getCaLam().getGioKetThuc() != null)
                    ? l.getCaLam().getGioKetThuc().toString().substring(0, 5) : "";

            // CHỈ CẦN 6 CỘT NÀY, BỎ CỘT THỨ 7 (Trạng thái)
            modelCaLam.addRow(new Object[]{ maCa, maNV, ten, ngay, gbd, gkt });
        }
    }

    private JLabel buildShiftCard(LichLamViec l) {
        String maNV = l.getNhanVien() != null ? l.getNhanVien().getMaNV() : "?";
        String gbd  = (l.getCaLam() != null && l.getCaLam().getGioBatDau() != null)
                ? l.getCaLam().getGioBatDau().toString().substring(0, 5) : "--:--";
        String gkt  = (l.getCaLam() != null && l.getCaLam().getGioKetThuc() != null)
                ? l.getCaLam().getGioKetThuc().toString().substring(0, 5) : "--:--";

        JLabel card = new JLabel("<html><b>" + maNV + "</b><br>" + gbd + "-" + gkt + "</html>");
        card.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        card.setBackground(CAL_SHIFT_BG);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createLineBorder(CAL_SHIFT_BRD));
        return card;
    }



    // ════════════════════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ════════════════════════════════════════════════════════════════════════

    private void handleXem() {
        String kw = txtSearchNV.getText().trim().toLowerCase();
        String range = (String) cmbFilterRange.getSelectedItem();
        List<LichLamViec> filtered = new ArrayList<>();
        for (LichLamViec l : cachedLich) {
            if (!kw.isEmpty()) {
                String maNV = l.getNhanVien() != null ? l.getNhanVien().getMaNV().toLowerCase() : "";
                String ten  = l.getNhanVien() != null ? nullSafe(l.getNhanVien().getTenNV()).toLowerCase() : "";
                if (!maNV.contains(kw) && !ten.contains(kw)) continue;
            }
            if (l.getNgayLam() != null && !matchesDateRange(l.getNgayLam(), range)) continue;
            filtered.add(l);
        }
        populateTable(filtered);
        renderWeekCalendar();
    }

    private void handleThem() {
        // 1. Lấy dữ liệu từ giao diện
        CaLam selectedCa = (CaLam) cmbCaLam.getSelectedItem(); // (Cái ComboBox bạn vừa tạo)
        String maNVStr = txtMaNV.getText().trim();
        String ngayStr = txtNgayLam.getText().trim();

        if (selectedCa == null || maNVStr.isEmpty() || ngayStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Ca, nhập NV và Ngày!");
            return;
        }

        try {
            Date ngay = dateFmt.parse(ngayStr); // Dùng formatter có sẵn của bạn

            // 2. Sinh mã tự động thông qua DAO (Logic nằm ở DAO để chuẩn hóa)
            String maCaMoi = lichDao.generateMaCa(selectedCa, ngay);

            // 3. Tạo đối tượng CaLam "tạm thời" để lưu mã mới sinh vào DB
            CaLam caPhucVuLuu = new CaLam(maCaMoi, selectedCa.getTenCa(), selectedCa.getGioBatDau(), selectedCa.getGioKetThuc());

            // 4. Tạo đối tượng Nhân Viên
            NhanVien nv = new NhanVien();
            nv.setMaNV(maNVStr);
            nv.setTenNV(txtTenNhanVien.getText().trim());

            // 5. Tạo đối tượng Lịch và lưu
            LichLamViec l = new LichLamViec(0, nv, caPhucVuLuu, ngay);

            if (lichDao.create(l)) {
                JOptionPane.showMessageDialog(this, "✅ Thêm lịch thành công với mã: " + maCaMoi);
                // Reload lại bảng và lịch tuần
                cachedLich = lichDao.getAll();
                populateTable(cachedLich);
                renderWeekCalendar();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm lịch!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày!");
        }
    }

    private void handleSua() {
        int row = tableCalLam.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn lịch cần sửa!"); return; }
        // Mở popup sửa giờ ca
        openEditCaPopup();
    }

    private void handleXoa() {
        int row = tableCalLam.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn lịch cần xóa!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa lịch làm việc này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int maLich = cachedLich.get(row).getMaLich();
        if (lichDao.delete(maLich)) {
            cachedLich = lichDao.getAll();
            populateTable(cachedLich);
            renderWeekCalendar();
            clearForm();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FORM HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private void fillFormFromTable() {
        int row = tableCalLam.getSelectedRow();
        if (row < 0 || row >= cachedLich.size()) return;
        LichLamViec l = cachedLich.get(row);
        txtMaCa.setText(l.getCaLam()    != null ? l.getCaLam().getMaCa()   : "");
        txtMaNV.setText(l.getNhanVien() != null ? l.getNhanVien().getMaNV() : "");
        txtTenNhanVien.setText(l.getNhanVien() != null ? nullSafe(l.getNhanVien().getTenNV()) : "");
        txtNgayLam.setText(l.getNgayLam() != null ? dateFmt.format(l.getNgayLam()) : "");
    }

    private void clearForm() {
        txtMaCa.setText("");
        txtMaNV.setText("");
        txtTenNhanVien.setText("");
        txtNgayLam.setText(dateFmt.format(new Date()));
        tableCalLam.clearSelection();
    }

    private void lookupNhanVien() {
        String maNV = txtMaNV.getText().trim();
        if (maNV.isEmpty()) { txtTenNhanVien.setText(""); return; }
        for (NhanVien nv : dsNhanVien) {
            if (nv.getMaNV().equalsIgnoreCase(maNV)) {
                txtTenNhanVien.setText(nullSafe(nv.getTenNV()));
                return;
            }
        }
        txtTenNhanVien.setText("(không tìm thấy)");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATE / TIME UTILS
    // ════════════════════════════════════════════════════════════════════════

    private Date getMonday(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);      c.set(Calendar.MILLISECOND, 0);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        c.add(Calendar.DAY_OF_MONTH, dow == Calendar.SUNDAY ? -6 : Calendar.MONDAY - dow);
        return c.getTime();
    }

    private boolean isSameDay(Date a, Date b) {
        if (a == null || b == null) return false;
        Calendar ca = Calendar.getInstance(); ca.setTime(a);
        Calendar cb = Calendar.getInstance(); cb.setTime(b);
        return ca.get(Calendar.YEAR)        == cb.get(Calendar.YEAR)
                && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR);
    }

    private List<LichLamViec> getLichByDate(Date d) {
        List<LichLamViec> r = new ArrayList<>();
        for (LichLamViec l : cachedLich)
            if (l.getNgayLam() != null && isSameDay(l.getNgayLam(), d)) r.add(l);
        return r;
    }

    private boolean matchesDateRange(Date d, String range) {
        Calendar now = Calendar.getInstance();
        Calendar dc  = Calendar.getInstance(); dc.setTime(d);
        switch (range != null ? range : "Hôm nay") {
            case "Hôm nay":   return isSameDay(d, now.getTime());
            case "Tuần này": {
                Calendar ws = Calendar.getInstance(); ws.setTime(getMonday(now.getTime()));
                Calendar we = (Calendar) ws.clone(); we.add(Calendar.DAY_OF_MONTH, 6);
                return !dc.before(ws) && !dc.after(we);
            }
            case "Tháng này":
                return dc.get(Calendar.YEAR)  == now.get(Calendar.YEAR)
                        && dc.get(Calendar.MONTH) == now.get(Calendar.MONTH);
            default: return true;
        }
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    // ════════════════════════════════════════════════════════════════════════
    //  DATE PICKER
    // ════════════════════════════════════════════════════════════════════════

    @FunctionalInterface interface DateCB { void onSelected(Date d); }

    private void showDatePicker(JTextField target, DateCB cb) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn ngày", true);
        dlg.setSize(280, 310); dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        Calendar cal = Calendar.getInstance();
        try { if (!target.getText().isEmpty()) cal.setTime(dateFmt.parse(target.getText())); }
        catch (Exception ignored) {}

        // Mốc thời gian hôm nay (để so sánh)
        Calendar calNow = Calendar.getInstance();
        calNow.set(Calendar.HOUR_OF_DAY, 0); calNow.set(Calendar.MINUTE, 0);
        calNow.set(Calendar.SECOND, 0); calNow.set(Calendar.MILLISECOND, 0);

        JLabel lblM = new JLabel("", SwingConstants.CENTER);
        lblM.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JPanel hdr = new JPanel(new BorderLayout(4, 4));
        hdr.setBackground(Color.WHITE); hdr.setBorder(new EmptyBorder(6, 8, 6, 8));
        JButton prev = mkIconBtn("◀"); JButton next = mkIconBtn("▶");
        hdr.add(prev, BorderLayout.WEST); hdr.add(lblM, BorderLayout.CENTER); hdr.add(next, BorderLayout.EAST);
        dlg.add(hdr, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        grid.setBackground(Color.WHITE); grid.setBorder(new EmptyBorder(4, 8, 8, 8));
        dlg.add(grid, BorderLayout.CENTER);

        Runnable render = () -> {
            grid.removeAll();
            lblM.setText(new SimpleDateFormat("MM/yyyy").format(cal.getTime()));
            for (String s : new String[]{"CN","T2","T3","T4","T5","T6","T7"}) {
                JLabel l = new JLabel(s, SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11)); l.setForeground(MAIN_BLUE);
                grid.add(l);
            }
            Calendar tmp = (Calendar) cal.clone(); tmp.set(Calendar.DAY_OF_MONTH, 1);
            int off = tmp.get(Calendar.DAY_OF_WEEK) - 1;
            int max = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < off; i++) grid.add(new JLabel(""));
            for (int d = 1; d <= max; d++) {
                final int day = d;
                Calendar dCal = (Calendar) cal.clone();
                dCal.set(Calendar.DAY_OF_MONTH, day);

                JButton btn = new JButton(String.valueOf(d));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                btn.setFocusPainted(false);

                // Logic chặn ngày quá khứ
                if (dCal.before(calNow)) {
                    btn.setEnabled(false);
                    btn.setBackground(new Color(240, 240, 240));
                } else {
                    btn.setBackground(Color.WHITE);
                }

                btn.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
                btn.setMargin(new Insets(2,2,2,2));
                btn.addActionListener(e -> {
                    target.setText(dateFmt.format(dCal.getTime()));
                    if (cb != null) cb.onSelected(dCal.getTime());
                    dlg.dispose();
                });
                grid.add(btn);
            }
            grid.revalidate(); grid.repaint();
        };
        render.run();
        prev.addActionListener(e -> { cal.add(Calendar.MONTH,-1); render.run(); });
        next.addActionListener(e -> { cal.add(Calendar.MONTH, 1); render.run(); });
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI FACTORY
    // ════════════════════════════════════════════════════════════════════════

    private JTextField mkField(int w) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(INPUT_BG); f.setForeground(TEXT_DARK);
        f.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                new EmptyBorder(2, 6, 2, 6)));
        if (w > 0) f.setPreferredSize(new Dimension(w, 32));
        else        f.setPreferredSize(new Dimension(100, 32));
        return f;
    }

    private JTextField mkFieldPlaceholder(int w, String ph) {
        JTextField f = mkField(w);
        f.setForeground(GRAY_TEXT); f.setText(ph);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) { f.setText(""); f.setForeground(TEXT_DARK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setForeground(GRAY_TEXT); f.setText(ph); }
            }
        });
        return f;
    }

    private JButton mkBtn(String t, Color bg, Color fg, int w) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(w > 0 ? w : 80, 34));
        return b;
    }

    private JButton mkIconBtn(String icon) {
        JButton b = new JButton(icon);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setPreferredSize(new Dimension(32, 32));
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleCombo(JComboBox<String> c, int w) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(INPUT_BG);
        if (w > 0) c.setPreferredSize(new Dimension(w, 32));
    }

    private void styleSpinner(JSpinner s) {
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setPreferredSize(new Dimension(0, 32));
    }

    private JLabel mkSectionTitle(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(MAIN_BLUE); l.setOpaque(true);
        l.setBackground(CAL_HEADER);
        l.setPreferredSize(new Dimension(0, 28));
        l.setBorder(new MatteBorder(1, 0, 1, 0, BORDER_CLR));
        return l;
    }

    private void addFormRow(JPanel p, GridBagConstraints g, int row, String lbl, Component comp) {
        g.gridy = row * 2; g.gridx = 0; g.weighty = 0;
        JLabel label = new JLabel(lbl);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12)); label.setForeground(GRAY_TEXT);
        p.add(label, g);
        g.gridy = row * 2 + 1; p.add(comp, g);
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12)); t.setRowHeight(30);
        t.setSelectionBackground(SELECT_BG); t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(CAL_HEADER); t.getTableHeader().setForeground(MAIN_BLUE);
        t.getTableHeader().setPreferredSize(new Dimension(0, 32));
        t.setGridColor(BORDER_CLR); t.setBackground(Color.WHITE); t.setShowVerticalLines(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(center);
    }
}
