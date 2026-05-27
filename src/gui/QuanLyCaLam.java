package gui;

import dao.CaLam_DAO;
import dao.NhanVien_DAO;
import dao.PhanCongCa_DAO;
import entity.CaLam;
import entity.NhanVien;
import entity.PhanCongCa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class QuanLyCaLam extends JPanel {

    private static final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private static final Color GOLD_COLOR   = Color.decode("#C5A059");
    private static final Color CONTENT_BG   = Color.decode("#F0F2F5");
    private static final Color TEXT_DARK    = Color.decode("#333333");
    private static final Color BORDER_LIGHT = Color.decode("#E0E0E0");
    private static final Color YELLOW_MULTI = new Color(255, 243, 176);
    private static final Color SHIFT_BG     = Color.decode("#E8EEF3");
    private static final Color GREEN_CELL   = new Color(230, 247, 237);

    private final CaLam_DAO     caDAO = new CaLam_DAO();
    private final NhanVien_DAO  nvDAO = new NhanVien_DAO();
    private final PhanCongCa_DAO pcDAO = new PhanCongCa_DAO();

    private NhanVien currentNV;

    private JSpinner spNgayMoc;
    private JTable   scheduleTable;
    private DefaultTableModel tableModel;
    private JComboBox<NhanVien> cboNhanVien;
    private JSpinner spNgayTruc;
    private JComboBox<CaLam> cboCaLam;

    private Calendar currentWeekStart;
    private List<CaLam> dsCa = new ArrayList<>();

    private static final String[] DAY_LABELS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
    private static final int MIN_ROW_H = 72;

    public QuanLyCaLam(NhanVien nv) {
        this.currentNV = nv;
        setLayout(new BorderLayout(0, 0));
        setBackground(CONTENT_BG);
        initCurrentWeek();
        initUI();
    }

    public QuanLyCaLam() { this(null); }

    // ─── Init ────────────────────────────────────────────────────────────────

    private void initCurrentWeek() {
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        zeroTime(currentWeekStart);
    }

    private void zeroTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }


    private void initUI() {
        add(buildHeader(), BorderLayout.NORTH);

        JPanel pMain = new JPanel(new BorderLayout(0, 10));
        pMain.setOpaque(false);
        pMain.setBorder(new EmptyBorder(14, 28, 20, 28));
        pMain.add(buildFilterPanel(), BorderLayout.NORTH);
        pMain.add(buildScheduleTable(), BorderLayout.CENTER);
        pMain.add(buildAssignForm(), BorderLayout.SOUTH);
        add(pMain, BorderLayout.CENTER);

        loadSchedule();
    }

    // ─── Header ─────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(MAIN_BLUE);
        p.setBorder(new EmptyBorder(10, 28, 10, 28));

        JLabel lblTitle = new JLabel("QUẢN LÝ CA LÀM VIỆC");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(GOLD_COLOR);
        JLabel lblSub = new JLabel("Phân công và theo dõi ca làm việc của nhân viên");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(180, 200, 220));
        JPanel pTBox = new JPanel(); pTBox.setLayout(new BoxLayout(pTBox, BoxLayout.Y_AXIS)); pTBox.setOpaque(false);
        pTBox.add(lblTitle); pTBox.add(Box.createVerticalStrut(2)); pTBox.add(lblSub);
        p.add(pTBox, BorderLayout.WEST);
        return p;
    }

    // ─── Filter ─────────────────────────────────────────────────────────────

    private JPanel buildFilterPanel() {
        JPanel card = makeCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        card.setBorder(new EmptyBorder(0, 8, 0, 8));

        JLabel lbl = new JLabel("Bộ lọc lịch làm việc:");
        lbl.setFont(new Font("Inter Bold", Font.BOLD, 15));
        lbl.setForeground(TEXT_DARK);
        card.add(lbl);

        card.add(label("Kiểu xem:"));
        JComboBox<String> cboKieu = new JComboBox<>(new String[]{"Theo Tuần"});
        cboKieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboKieu.setPreferredSize(new Dimension(150, 32));
        card.add(cboKieu);

        card.add(label("Chọn ngày mốc:"));
        SpinnerDateModel mdl = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        spNgayMoc = new JSpinner(mdl);
        spNgayMoc.setEditor(new JSpinner.DateEditor(spNgayMoc, "dd/MM/yyyy"));
        ((JSpinner.DefaultEditor) spNgayMoc.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 15));
        spNgayMoc.setPreferredSize(new Dimension(150, 32));
        card.add(spNgayMoc);

        JButton btnTai = makeBlueButton("Tải Lịch");
        btnTai.addActionListener(e -> {
            Date d = (Date) spNgayMoc.getValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            zeroTime(cal);
            currentWeekStart = cal;
            loadSchedule();
        });
        card.add(btnTai);
        return card;
    }

    // ─── Schedule Table ──────────────────────────────────────────────────────

    private JPanel buildScheduleTable() {
        dsCa = caDAO.getAll();

        tableModel = new DefaultTableModel(buildColumnHeaders(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (CaLam ca : dsCa) {
            Object[] row = new Object[8];
            row[0] = ca;
            for (int i = 1; i < 8; i++) row[i] = new ArrayList<String>();
            tableModel.addRow(row);
        }

        scheduleTable = new JTable(tableModel);
        scheduleTable.setFillsViewportHeight(true);   // fill empty space below rows
        scheduleTable.setShowGrid(true);
        scheduleTable.setGridColor(BORDER_LIGHT);
        scheduleTable.setIntercellSpacing(new Dimension(1, 1));
        scheduleTable.setSelectionBackground(new Color(11, 61, 89, 30));
        scheduleTable.setRowHeight(MIN_ROW_H);

        // Header
        JTableHeader header = scheduleTable.getTableHeader();
        header.setFont(new Font("Inter Bold", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 44));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                l.setBackground(MAIN_BLUE);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Inter Bold", Font.BOLD, 14));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setOpaque(true);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                        new Color(255, 255, 255, 40)));
                return l;
            }
        });

        // Column renderers
        scheduleTable.getColumnModel().getColumn(0).setCellRenderer(new ShiftNameRenderer());
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        for (int i = 1; i <= 7; i++) {
            scheduleTable.getColumnModel().getColumn(i).setCellRenderer(new EmployeeCellRenderer());
            scheduleTable.getColumnModel().getColumn(i).setPreferredWidth(110);
        }

        // Click vào ô → tự điền ngày + ca vào form bên dưới
        scheduleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = scheduleTable.rowAtPoint(e.getPoint());
                int col = scheduleTable.columnAtPoint(e.getPoint());
                if (row < 0 || col < 1) return;   // bỏ qua cột tên ca
                // Tính ngày từ cột
                Calendar cal = (Calendar) currentWeekStart.clone();
                cal.add(Calendar.DAY_OF_MONTH, col - 1);
                spNgayTruc.setValue(cal.getTime());
                // Chọn ca tương ứng với row
                if (row < dsCa.size()) cboCaLam.setSelectedItem(dsCa.get(row));
            }
        });

        JScrollPane scroll = new JScrollPane(scheduleTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);

        // Auto-adjust row height to fill the viewport
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int viewH = e.getComponent().getHeight();
                if (viewH > 0 && !dsCa.isEmpty()) {
                    int rowH = Math.max(MIN_ROW_H, viewH / dsCa.size());
                    if (scheduleTable.getRowHeight() != rowH)
                        scheduleTable.setRowHeight(rowH);
                }
            }
        });

        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ─── Assign Form ─────────────────────────────────────────────────────────

    private JPanel buildAssignForm() {
        JPanel card = makeCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        card.setBorder(new EmptyBorder(0, 8, 0, 8));

        JLabel lbl = new JLabel("Thao Tác Phân công ca trực:");
        lbl.setFont(new Font("Inter Bold", Font.BOLD, 15));
        lbl.setForeground(TEXT_DARK);
        card.add(lbl);

        card.add(label("Nhân viên:"));
        cboNhanVien = new JComboBox<>();cboNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof NhanVien ? ((NhanVien) value).getTenNV() : "");
                return this;
            }
        });
        cboNhanVien.setPreferredSize(new Dimension(180, 32));
        loadNhanVien();
        card.add(cboNhanVien);

        card.add(label("Ngày trực:"));
        SpinnerDateModel ngayMdl = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        spNgayTruc = new JSpinner(ngayMdl);
        spNgayTruc.setEditor(new JSpinner.DateEditor(spNgayTruc, "dd/MM/yyyy"));
        ((JSpinner.DefaultEditor) spNgayTruc.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 15));
        spNgayTruc.setPreferredSize(new Dimension(150, 32));
        card.add(spNgayTruc);

        card.add(label("Ca làm:"));
        cboCaLam = new JComboBox<>(); cboCaLam.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        for (CaLam ca : dsCa) cboCaLam.addItem(ca);
        cboCaLam.setPreferredSize(new Dimension(140, 32));
        card.add(cboCaLam);

        JButton btnReset = new JButton("Làm mới");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setPreferredSize(new Dimension(100, 32));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            if (cboNhanVien.getItemCount() > 0) cboNhanVien.setSelectedIndex(0);
            spNgayTruc.setValue(new Date());
            if (cboCaLam.getItemCount() > 0) cboCaLam.setSelectedIndex(0);
        });
        card.add(btnReset);

        JButton btnLuu = makeBlueButton("Lưu phân công");
        btnLuu.addActionListener(e -> savePhanCong());
        card.add(btnLuu);

        JButton btnXoa = makeRedButton("Xóa phân công");
        btnXoa.addActionListener(e -> deletePhanCong());
        card.add(btnXoa);

        return card;
    }

    // ─── Data ────────────────────────────────────────────────────────────────

    private void loadNhanVien() {
        cboNhanVien.removeAllItems();
        List<NhanVien> ds = nvDAO.getAllNhanVien();
        if (ds != null)
            for (NhanVien nv : ds)
                if (nv.isTrangThai()) cboNhanVien.addItem(nv);
    }

    private void loadSchedule() {
        String[] cols = buildColumnHeaders();
        TableColumnModel tcm = scheduleTable.getColumnModel();
        for (int i = 0; i < 8; i++) tcm.getColumn(i).setHeaderValue(cols[i]);
        scheduleTable.getTableHeader().repaint();

        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_MONTH, 6);
        weekEnd.set(Calendar.HOUR_OF_DAY, 23);
        weekEnd.set(Calendar.MINUTE, 59);
        weekEnd.set(Calendar.SECOND, 59);

        List<PhanCongCa> dsPhanCong = pcDAO.getByWeek(currentWeekStart.getTime(), weekEnd.getTime());

        Map<String, Map<Integer, List<String>>> map = new LinkedHashMap<>();
        for (CaLam ca : dsCa) map.put(ca.getMaCa(), new HashMap<>());

        for (PhanCongCa pc : dsPhanCong) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(pc.getNgayLam());
            int dayIdx = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7;
            Map<Integer, List<String>> caMap = map.get(pc.getCaLam().getMaCa());
            if (caMap != null)
                caMap.computeIfAbsent(dayIdx, k -> new ArrayList<>())
                     .add(pc.getNhanVien().getTenNV());
        }

        for (int row = 0; row < dsCa.size(); row++) {
            Map<Integer, List<String>> dayMap =
                    map.getOrDefault(dsCa.get(row).getMaCa(), Collections.emptyMap());
            for (int col = 0; col < 7; col++)
                tableModel.setValueAt(dayMap.getOrDefault(col, new ArrayList<>()), row, col + 1);
        }
        tableModel.fireTableDataChanged();
    }

    private void savePhanCong() {
        NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();
        CaLam   ca = (CaLam)    cboCaLam.getSelectedItem();
        if (nv == null || ca == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.sql.Date sqlDate = new java.sql.Date(((Date) spNgayTruc.getValue()).getTime());
        if (pcDAO.addPhanCong(ca.getMaCa(), nv.getMaNV(), sqlDate)) {
            JOptionPane.showMessageDialog(this, "Phân công ca thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadSchedule();
        } else {
            JOptionPane.showMessageDialog(this,
                    nv.getTenNV() + " đã được phân công ca này vào ngày đó rồi!",
                    "Trùng lặp", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deletePhanCong() {
        NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();
        CaLam   ca = (CaLam)    cboCaLam.getSelectedItem();
        if (nv == null || ca == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        java.sql.Date sqlDate = new java.sql.Date(((Date) spNgayTruc.getValue()).getTime());
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa phân công của " + nv.getTenNV() + " khỏi " + ca.getTenCa()
                + " ngày " + new SimpleDateFormat("dd/MM/yyyy").format(sqlDate) + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (pcDAO.deletePhanCongByKey(ca.getMaCa(), nv.getMaNV(), sqlDate)) {
            JOptionPane.showMessageDialog(this, "Đã xóa phân công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadSchedule();
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phân công này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void refreshData() {
        loadNhanVien();
        cboCaLam.removeAllItems();
        dsCa = caDAO.getAll();
        for (CaLam ca : dsCa) cboCaLam.addItem(ca);
        loadSchedule();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String[] buildColumnHeaders() {
        String[] cols = new String[8];
        cols[0] = "Ca Làm Việc";
        Calendar cal = (Calendar) currentWeekStart.clone();
        SimpleDateFormat fmt = new SimpleDateFormat("(dd/MM)");
        for (int i = 0; i < 7; i++) {
            cols[i + 1] = DAY_LABELS[i] + " " + fmt.format(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return cols;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return l;
    }

    private JPanel makeCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(218, 222, 228));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
    }

    private JButton makeBlueButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0x0D4F72) : MAIN_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter Medium", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeRedButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xC0392B) : new Color(0xE74C3C));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter Medium", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Cell Renderers ──────────────────────────────────────────────────────

    private class ShiftNameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel inner = new JPanel();
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            inner.setOpaque(false);

            if (value instanceof CaLam) {
                CaLam ca = (CaLam) value;

                JLabel lblName = new JLabel(ca.getTenCa());
                lblName.setFont(new Font("Inter Bold", Font.BOLD, 16));
                lblName.setForeground(MAIN_BLUE);
                lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblTime = new JLabel(ca.getKhungGio());
                lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblTime.setForeground(new Color(100, 100, 100));
                lblTime.setAlignmentX(Component.CENTER_ALIGNMENT);

                inner.add(lblName);
                inner.add(Box.createVerticalStrut(5));
                inner.add(lblTime);
            }

            JPanel wrap = new JPanel(new GridBagLayout());
            wrap.setBackground(SHIFT_BG);
            wrap.add(inner);
            return wrap;
        }
    }

    @SuppressWarnings("unchecked")
    private class EmployeeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            List<String> names = (value instanceof List) ? (List<String>) value : Collections.emptyList();

            if (names.isEmpty()) {
                JPanel p = new JPanel(new GridBagLayout());
                p.setBackground(Color.WHITE);
                JLabel lbl = new JLabel("Trống");
                lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                lbl.setForeground(new Color(195, 195, 195));
                p.add(lbl);
                return p;
            }

            JPanel inner = new JPanel();
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            inner.setOpaque(false);
            for (int i = 0; i < names.size(); i++) {
                JLabel lbl = new JLabel("• " + names.get(i));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                lbl.setForeground(TEXT_DARK);
                lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                inner.add(lbl);
                if (i < names.size() - 1) inner.add(Box.createVerticalStrut(4));
            }

            JPanel wrap = new JPanel(new GridBagLayout());
            wrap.setBackground(names.size() > 1 ? YELLOW_MULTI : GREEN_CELL);
            wrap.add(inner);
            return wrap;
        }
    }
}
