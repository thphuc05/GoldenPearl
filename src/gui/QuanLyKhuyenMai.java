package gui;

import dao.KhuyenMai_DAO;
import entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class QuanLyKhuyenMai extends JPanel {

    private final KhuyenMai_DAO dao = new KhuyenMai_DAO();
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private JTable table;
    private DefaultTableModel tableModel;
    private List<KhuyenMai> currentList = new ArrayList<>();

    private JTextField txtMaKM, txtTenKM, txtSearch;
    private JSpinner spPhanTram, spNgayBD, spNgayKT;
    private JCheckBox chkNgayBD, chkNgayKT;
    private JButton btnThemMoi, btnLuu, btnXoa, btnLamMoi;

    private boolean dangChinhSua = false;

    private final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private final Color GOLD_COLOR   = Color.decode("#C5A059");
    private final Color TEXT_DARK    = Color.decode("#333333");
    private final Color BORDER_COLOR = Color.decode("#E0E0E0");
    private final Color SELECT_BG    = Color.decode("#EBF5FB");
    private final Color GREEN_ACTIVE = Color.decode("#27AE60");
    private final Color RED_EXPIRED  = Color.decode("#E74C3C");
    private final Color ORANGE_SOON  = Color.decode("#E67E22");

    public QuanLyKhuyenMai() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        bindEvents();
        refreshData();
    }

    // ─── TOP ─────────────────────────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setOpaque(false);

        // ── Thanh tiêu đề xanh (chỉ title) ──────────────────────────────
        JPanel pTitle = new JPanel(new BorderLayout());
        pTitle.setBackground(MAIN_BLUE);
        pTitle.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel lbl = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(GOLD_COLOR);
        JLabel lblSub = new JLabel("Tạo và quản lý các chương trình giảm giá, ưu đãi");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(180, 200, 220));
        JPanel pTBox = new JPanel(); pTBox.setLayout(new BoxLayout(pTBox, BoxLayout.Y_AXIS)); pTBox.setOpaque(false);
        pTBox.add(lbl); pTBox.add(Box.createVerticalStrut(2)); pTBox.add(lblSub);
        pTitle.add(pTBox, BorderLayout.WEST);
        wrapper.add(pTitle, BorderLayout.NORTH);

        // ── Toolbar: tìm kiếm ────────────────────────────────────────────
        JPanel pToolbar = new JPanel(new BorderLayout(0, 0));
        pToolbar.setBackground(Color.decode("#F0F2F5"));
        pToolbar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(GOLD_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        pSearch.setOpaque(false);
        pSearch.setBorder(new EmptyBorder(6, 10, 6, 10));

        pSearch.add(mkLbl("Tên khuyến mãi:"));
        txtSearch = mkField(200);
        pSearch.add(txtSearch);
        JButton btnSearch = mkBtn("Tìm", GOLD_COLOR, MAIN_BLUE);
        btnSearch.addActionListener(e -> filterTable());
        pSearch.add(btnSearch);
        JButton btnReset = mkBtn("Xóa lọc", Color.decode("#E0E0E0"), TEXT_DARK);
        btnReset.addActionListener(e -> { txtSearch.setText(""); refreshData(); });
        pSearch.add(btnReset);

        pToolbar.add(pSearch, BorderLayout.EAST);
        wrapper.add(pToolbar, BorderLayout.SOUTH);

        return wrapper;
    }

    // ─── CENTER ───────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(0, 16, 0, 16));

        String[] cols = {"Mã KM", "Tên khuyến mãi", "% Giảm", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setSelectionBackground(SELECT_BG);
        table.setSelectionForeground(TEXT_DARK);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 13));
        table.getTableHeader().setBackground(MAIN_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Renderer cột trạng thái (badge màu)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setOpaque(true);
                String v = val != null ? val.toString() : "";
                if (!sel) {
                    switch (v) {
                        case "Hoạt động":
                            l.setForeground(Color.WHITE); l.setBackground(GREEN_ACTIVE); break;
                        case "Hết hạn":
                            l.setForeground(Color.WHITE); l.setBackground(RED_EXPIRED); break;
                        case "Chưa bắt đầu":
                            l.setForeground(Color.WHITE); l.setBackground(ORANGE_SOON); break;
                        default:
                            l.setForeground(TEXT_DARK); l.setBackground(Color.WHITE);
                    }
                }
                return l;
            }
        });

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerR);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        center.add(scroll, BorderLayout.CENTER);
        return center;
    }

    // ─── BOTTOM: form ─────────────────────────────────────────────────────────
    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(new EmptyBorder(8, 16, 16, 16));

        JPanel kmWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(GOLD_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        kmWrapper.setOpaque(false);
        JLabel kmHdr = new JLabel("  THÔNG TIN KHUYẾN MÃI");
        kmHdr.setFont(new Font("Inter Bold", Font.BOLD, 12));
        kmHdr.setForeground(TEXT_DARK);
        kmHdr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GOLD_COLOR),
                new EmptyBorder(8, 6, 8, 6)));
        kmWrapper.add(kmHdr, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 1: Mã KM + Tên KM
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        form.add(mkLbl("Mã KM:"), g);
        g.gridx = 1; g.weightx = 0.5;
        txtMaKM = mkField(120);
        txtMaKM.setEditable(false);
        txtMaKM.setBackground(new Color(245, 245, 245));
        form.add(txtMaKM, g);

        g.gridx = 2; g.weightx = 0;
        form.add(mkLbl("Tên khuyến mãi:"), g);
        g.gridx = 3; g.weightx = 2; g.gridwidth = 3;
        txtTenKM = mkField(300);
        form.add(txtTenKM, g);
        g.gridwidth = 1;

        // Dòng 2: % giảm + ngày bắt đầu + ngày kết thúc
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        form.add(mkLbl("% Giảm giá:"), g);
        g.gridx = 1; g.weightx = 0.5;
        spPhanTram = buildPercentSpinner();
        form.add(spPhanTram, g);

        g.gridx = 2; g.weightx = 0;
        chkNgayBD = new JCheckBox("Ngày bắt đầu:");
        chkNgayBD.setFont(new Font("Inter", Font.PLAIN, 13));
        chkNgayBD.setForeground(TEXT_DARK);
        chkNgayBD.setBackground(Color.WHITE);
        form.add(chkNgayBD, g);
        g.gridx = 3; g.weightx = 1;
        spNgayBD = buildDateSpinner();
        spNgayBD.setEnabled(false);
        form.add(spNgayBD, g);

        g.gridx = 4; g.weightx = 0;
        chkNgayKT = new JCheckBox("Ngày kết thúc:");
        chkNgayKT.setFont(new Font("Inter", Font.PLAIN, 13));
        chkNgayKT.setForeground(TEXT_DARK);
        chkNgayKT.setBackground(Color.WHITE);
        form.add(chkNgayKT, g);
        g.gridx = 5; g.weightx = 1;
        spNgayKT = buildDateSpinner();
        spNgayKT.setEnabled(false);
        form.add(spNgayKT, g);

        kmWrapper.add(form, BorderLayout.CENTER);
        bottom.add(kmWrapper, BorderLayout.CENTER);

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pBtn.setBackground(Color.WHITE);
        btnThemMoi = mkBtn("+ Thêm mới", MAIN_BLUE, Color.WHITE);
        btnLuu     = mkBtn("Lưu",        GREEN_ACTIVE, Color.WHITE);
        btnXoa     = mkBtn("Xóa",        RED_EXPIRED,  Color.WHITE);
        btnLamMoi  = mkBtn("Làm mới",    Color.decode("#E0E0E0"), TEXT_DARK);
        btnLuu.setEnabled(false);
        btnXoa.setEnabled(false);
        pBtn.add(btnThemMoi);
        pBtn.add(btnLuu);
        pBtn.add(btnXoa);
        pBtn.add(btnLamMoi);
        bottom.add(pBtn, BorderLayout.SOUTH);

        return bottom;
    }

    // ─── Sự kiện ──────────────────────────────────────────────────────────────
    private void bindEvents() {
        chkNgayBD.addActionListener(e -> spNgayBD.setEnabled(chkNgayBD.isSelected()));
        chkNgayKT.addActionListener(e -> spNgayKT.setEnabled(chkNgayKT.isSelected()));

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0 || row >= currentList.size()) return;
                fillForm(currentList.get(row), true);
            }
        });

        btnThemMoi.addActionListener(e -> {
            clearForm();
            dangChinhSua = false;
            txtMaKM.setText(dao.generateMaKM());
            btnLuu.setEnabled(true);
            btnXoa.setEnabled(false);
            txtTenKM.requestFocus();
        });

        btnLuu.addActionListener(e -> save());
        btnXoa.addActionListener(e -> delete());
        btnLamMoi.addActionListener(e -> { clearForm(); refreshData(); });
    }

    private void fillForm(KhuyenMai km, boolean isEdit) {
        dangChinhSua = isEdit;
        txtMaKM.setText(km.getMaKM());
        txtTenKM.setText(km.getTenKM());
        spPhanTram.setValue(km.getPhanTramGiam());

        if (km.getNgayBatDau() != null) {
            chkNgayBD.setSelected(true);
            spNgayBD.setEnabled(true);
            spNgayBD.setValue(km.getNgayBatDau());
        } else {
            chkNgayBD.setSelected(false);
            spNgayBD.setEnabled(false);
        }

        if (km.getNgayKetThuc() != null) {
            chkNgayKT.setSelected(true);
            spNgayKT.setEnabled(true);
            spNgayKT.setValue(km.getNgayKetThuc());
        } else {
            chkNgayKT.setSelected(false);
            spNgayKT.setEnabled(false);
        }

        btnLuu.setEnabled(true);
        btnXoa.setEnabled(isEdit);
    }

    // ─── Lưu ──────────────────────────────────────────────────────────────────
    private void save() {
        String ten = txtTenKM.getText().trim();
        if (ten.isEmpty()) { showErr("Vui lòng nhập tên khuyến mãi."); txtTenKM.requestFocus(); return; }

        // Commit giá trị đang nhập trong spinner trước khi đọc
        try { spPhanTram.commitEdit(); } catch (Exception ignored) {}
        double pt = ((Number) spPhanTram.getValue()).doubleValue();
        if (pt <= 0 || pt >= 100) {
            showErr("% Giảm giá phải lớn hơn 0 và nhỏ hơn 100.");
            spPhanTram.requestFocus(); return;
        }

        Date ngayBD = chkNgayBD.isSelected() ? (Date) spNgayBD.getValue() : null;
        Date ngayKT = chkNgayKT.isSelected() ? (Date) spNgayKT.getValue() : null;

        if (ngayBD != null && ngayKT != null && !ngayKT.after(ngayBD)) {
            showErr("Ngày kết thúc phải sau ngày bắt đầu."); return;
        }

        KhuyenMai km = new KhuyenMai();
        km.setMaKM(txtMaKM.getText().trim());
        km.setTenKM(ten);
        km.setPhanTramGiam(pt);
        km.setNgayBatDau(ngayBD);
        km.setNgayKetThuc(ngayKT);

        boolean isNew = !dangChinhSua;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return isNew ? dao.add(km) : dao.update(km);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(QuanLyKhuyenMai.this,
                                isNew ? "Thêm khuyến mãi thành công!" : "Cập nhật thành công!",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        clearForm(); refreshData();
                    } else {
                        showErr(isNew ? "Thêm thất bại. Mã đã tồn tại?" : "Cập nhật thất bại.");
                    }
                } catch (Exception ex) { ex.printStackTrace(); showErr("Lỗi: " + ex.getMessage()); }
            }
        }.execute();
    }

    // ─── Xóa ──────────────────────────────────────────────────────────────────
    private void delete() {
        String ma = txtMaKM.getText().trim();
        if (ma.isEmpty()) return;
        int res = JOptionPane.showConfirmDialog(this,
                "Xóa khuyến mãi \"" + txtTenKM.getText().trim() + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.YES_OPTION) return;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return dao.delete(ma); }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(QuanLyKhuyenMai.this,
                                "Đã xóa khuyến mãi.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        clearForm(); refreshData();
                    } else {
                        showErr("Xóa thất bại. Khuyến mãi đang được sử dụng?");
                    }
                } catch (Exception ex) { ex.printStackTrace(); showErr("Lỗi: " + ex.getMessage()); }
            }
        }.execute();
    }

    // ─── Dữ liệu ──────────────────────────────────────────────────────────────
    public void refreshData() {
        new SwingWorker<List<KhuyenMai>, Void>() {
            @Override protected List<KhuyenMai> doInBackground() { return dao.getAll(); }
            @Override protected void done() {
                try { populateTable(get()); } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void filterTable() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) { refreshData(); return; }
        new SwingWorker<List<KhuyenMai>, Void>() {
            @Override protected List<KhuyenMai> doInBackground() { return dao.getAll(); }
            @Override protected void done() {
                try {
                    List<KhuyenMai> all = get();
                    all.removeIf(km -> !km.getTenKM().toLowerCase().contains(kw)
                            && !km.getMaKM().toLowerCase().contains(kw));
                    populateTable(all);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void populateTable(List<KhuyenMai> ds) {
        currentList = new ArrayList<>(ds);
        tableModel.setRowCount(0);
        Date now = new Date();
        for (KhuyenMai km : ds) {
            tableModel.addRow(new Object[]{
                km.getMaKM(),
                km.getTenKM(),
                String.format("%.1f%%", km.getPhanTramGiam()),
                km.getNgayBatDau()  != null ? SDF.format(km.getNgayBatDau())  : "--",
                km.getNgayKetThuc() != null ? SDF.format(km.getNgayKetThuc()) : "--",
                tinhTrangThai(km, now)
            });
        }
    }

    private String tinhTrangThai(KhuyenMai km, Date now) {
        Date bd = km.getNgayBatDau(), kt = km.getNgayKetThuc();
        if (bd == null && kt == null) return "Hoạt động";
        if (kt != null && now.after(kt))  return "Hết hạn";
        if (bd != null && now.before(bd)) return "Chưa bắt đầu";
        return "Hoạt động";
    }

    private void clearForm() {
        dangChinhSua = false;
        txtMaKM.setText(""); txtTenKM.setText("");
        spPhanTram.setValue(10.0);
        chkNgayBD.setSelected(false); spNgayBD.setEnabled(false);
        chkNgayKT.setSelected(false); spNgayKT.setEnabled(false);
        // Reset spinner về hôm nay
        spNgayBD.setValue(new Date());
        spNgayKT.setValue(new Date());
        btnLuu.setEnabled(false);
        btnXoa.setEnabled(false);
        table.clearSelection();
    }

    // ─── Tạo spinner ──────────────────────────────────────────────────────────
    private JSpinner buildPercentSpinner() {
        // min=0.1, max=99.9, step=0.5; không cho đúng 0 hay 100
        SpinnerNumberModel model = new SpinnerNumberModel(10.0, 0.1, 99.9, 0.5);
        JSpinner sp = new JSpinner(model);
        // Hiển thị dạng "10.0%" (% là ký tự literal, không nhân 100)
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(sp, "#0.0'%'");
        sp.setEditor(editor);
        sp.setFont(new Font("Inter", Font.PLAIN, 13));
        sp.setPreferredSize(new Dimension(120, 32));
        styleSpinner(sp);
        return sp;
    }

    private JSpinner buildDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "dd/MM/yyyy");
        sp.setEditor(editor);
        editor.getTextField().setFont(new Font("Inter", Font.PLAIN, 13));
        sp.setPreferredSize(new Dimension(140, 32));
        styleSpinner(sp);
        return sp;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        if (sp.getEditor() instanceof JComponent) {
            JComponent editor = (JComponent) sp.getEditor();
            editor.setBorder(new EmptyBorder(2, 6, 2, 2));
        }
    }

    // ─── Helper UI ────────────────────────────────────────────────────────────
    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel mkLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter", Font.PLAIN, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JTextField mkField(int width) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Inter", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(width, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        return tf;
    }

    private JButton mkBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : Color.decode("#CCCCCC"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter Bold", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(120, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
