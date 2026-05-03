package gui;

import dao.HoaDon_DAO;
import dao.KhachHang_DAO;
import entity.HoaDon;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class QuanLyKhachHang extends JPanel {
    private JTextField txtMaKH, txtTenKH, txtSoDT, txtSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    private JButton btnViewInvoice, btnPrevPage, btnNextPage;
    private JComboBox<String> cbPageSize;
    private JLabel lblTotal;
    private JTable table;
    private DefaultTableModel tableModel;
    private DefaultTableModel invoiceModel;
    private KhachHang_DAO kh_dao;
    private HoaDon_DAO hd_dao;

    private List<KhachHang> allData = new ArrayList<>();
    private int currentPage = 1;
    private int pageSize = 10;

    private final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private final Color GOLD_COLOR   = Color.decode("#C5A059");
    private final Color TEXT_DARK    = Color.decode("#333333");
    private final Color BORDER_COLOR = Color.decode("#E0E0E0");
    private final Color SELECT_BG    = Color.decode("#EBF5FB");
    private final Color GREEN_STATUS = Color.decode("#27AE60");
    private final Color RED_STATUS   = Color.decode("#E74C3C");

    private final SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy");

    public QuanLyKhachHang() {
        kh_dao = new KhachHang_DAO();
        hd_dao = new HoaDon_DAO();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setBorder(new EmptyBorder(14, 0, 6, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pMain = new JPanel(new BorderLayout(0, 0));
        pMain.setBackground(Color.WHITE);
        pMain.setBorder(new EmptyBorder(0, 14, 14, 14));
        pMain.add(createFilterBar(), BorderLayout.NORTH);

        JPanel pContent = new JPanel(new GridLayout(1, 2, 14, 0));
        pContent.setBackground(Color.WHITE);
        pContent.add(createLeftPanel());
        pContent.add(createRightPanel());
        pMain.add(pContent, BorderLayout.CENTER);

        add(pMain, BorderLayout.CENTER);

        initEvents();
        loadDataToTable();
        resetForm();
    }

    // ── Filter bar (full width) ───────────────────────────────────────────────
    private JPanel createFilterBar() {
        JPanel pOuter = new JPanel(new BorderLayout());
        pOuter.setBackground(Color.WHITE);
        pOuter.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel pBar = new JPanel(new BorderLayout(0, 6));
        pBar.setBackground(Color.WHITE);
        pBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 12, 10, 12)));

        JLabel lblSection = new JLabel("BỘ LỌC TÌM KIẾM");
        lblSection.setFont(new Font("Inter Bold", Font.BOLD, 12));
        lblSection.setForeground(MAIN_BLUE);
        lblSection.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(0, 0, 6, 0)));
        pBar.add(lblSection, BorderLayout.NORTH);

        JPanel pRow = new JPanel(new BorderLayout(8, 0));
        pRow.setBackground(Color.WHITE);

        JLabel lblLbl = new JLabel("Tìm kiếm (Tên/SĐT):");
        lblLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLbl.setForeground(TEXT_DARK);
        pRow.add(lblLbl, BorderLayout.WEST);

        txtSearch = mkField();
        pRow.add(txtSearch, BorderLayout.CENTER);

        JPanel pBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pBtnRow.setBackground(Color.WHITE);
        btnSearch = mkColorBtn("Tìm kiếm", MAIN_BLUE, Color.WHITE);
        JButton btnRefresh = mkColorBtn("Làm mới", Color.WHITE, TEXT_DARK);
        lblTotal = new JLabel("Tổng: 0 khách hàng");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotal.setForeground(TEXT_DARK);
        btnClear = btnRefresh;
        pBtnRow.add(btnSearch);
        pBtnRow.add(btnRefresh);
        pBtnRow.add(lblTotal);
        pRow.add(pBtnRow, BorderLayout.EAST);

        pBar.add(pRow, BorderLayout.CENTER);
        pOuter.add(pBar, BorderLayout.CENTER);
        return pOuter;
    }

    // ── LEFT: table + pagination ──────────────────────────────────────────────
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JLabel hdr = new JLabel("  DANH SÁCH KHÁCH HÀNG");
        hdr.setFont(new Font("Inter Bold", Font.BOLD, 13));
        hdr.setForeground(TEXT_DARK);
        hdr.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(10, 6, 10, 6)));
        panel.add(hdr, BorderLayout.NORTH);

        String[] cols = {"Mã KH", "Tên khách hàng", "Số điện thoại"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.setGridColor(new Color(235, 235, 235));
        table.setShowVerticalLines(false);
        table.setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 248, 248));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.setSelectionBackground(SELECT_BG);
        table.setSelectionForeground(TEXT_DARK);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(105);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        // Pagination bottom bar
        JPanel pPaging = new JPanel(new BorderLayout(6, 0));
        pPaging.setBackground(Color.WHITE);
        pPaging.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)));

        btnAdd = mkColorBtn("Thêm khách hàng", GOLD_COLOR, MAIN_BLUE);
        pPaging.add(btnAdd, BorderLayout.WEST);

        JPanel pNavRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pNavRow.setBackground(Color.WHITE);
        btnPrevPage = mkColorBtn("< Trang trước", Color.WHITE, TEXT_DARK);
        btnNextPage = mkColorBtn("Trang tiếp >", Color.WHITE, TEXT_DARK);
        cbPageSize = new JComboBox<>(new String[]{"10", "20", "50"});
        cbPageSize.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbPageSize.setPreferredSize(new Dimension(55, 28));
        JLabel lblShow = new JLabel("Hiển thị:");
        lblShow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pNavRow.add(btnPrevPage);
        pNavRow.add(btnNextPage);
        pNavRow.add(lblShow);
        pNavRow.add(cbPageSize);
        pPaging.add(pNavRow, BorderLayout.EAST);

        panel.add(pPaging, BorderLayout.SOUTH);
        return panel;
    }

    // ── RIGHT: form + action buttons ──────────────────────────────────────────
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JLabel hdr = new JLabel("  THÔNG TIN CHI TIẾT KHÁCH HÀNG");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hdr.setForeground(TEXT_DARK);
        hdr.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(10, 6, 10, 6)));
        panel.add(hdr, BorderLayout.NORTH);

        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setBackground(Color.WHITE);
        pForm.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0; gbc.insets = new Insets(0, 0, 12, 0);

        txtMaKH = mkField();
        txtMaKH.setEditable(false);
        txtMaKH.setBackground(new Color(245, 245, 245));
        txtMaKH.setForeground(new Color(100, 100, 100));
        gbc.gridy = 0; pForm.add(mkFieldGroup("Mã khách hàng:", txtMaKH), gbc);

        txtTenKH = mkField();
        gbc.gridy = 1; pForm.add(mkFieldGroup("Tên khách hàng *", txtTenKH), gbc);

        txtSoDT = mkField();
        gbc.gridy = 2; pForm.add(mkFieldGroup("Số điện thoại:", txtSoDT), gbc);

        gbc.gridy = 3; gbc.weighty = 1;
        pForm.add(Box.createVerticalGlue(), gbc);

        panel.add(pForm, BorderLayout.CENTER);

        // Action buttons at bottom
        JPanel pBtns = new JPanel(new GridLayout(1, 3, 8, 0));
        pBtns.setBackground(Color.WHITE);
        pBtns.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(10, 12, 10, 12)));
        btnViewInvoice = mkColorBtn("XEM CHI TIẾT HÓA ĐƠN", MAIN_BLUE, Color.WHITE);
        btnUpdate      = mkColorBtn("CẬP NHẬT THÔNG TIN", GOLD_COLOR, MAIN_BLUE);
        btnDelete      = mkColorBtn("XÓA KHÁCH HÀNG", Color.decode("#E74C3C"), Color.WHITE);
        pBtns.add(btnViewInvoice);
        pBtns.add(btnUpdate);
        pBtns.add(btnDelete);
        panel.add(pBtns, BorderLayout.SOUTH);

        return panel;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JPanel mkFieldGroup(String label, JComponent field) {
        JPanel g = new JPanel(new BorderLayout(0, 4));
        g.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        g.add(lbl, BorderLayout.NORTH);
        g.add(field, BorderLayout.CENTER);
        return g;
    }

    private JLabel mkLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        l.setPreferredSize(new Dimension(145, 28));
        return l;
    }

    private JTextField mkField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(180, 32));
        return f;
    }

    private JButton mkColorBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg.equals(Color.WHITE)
                        ? Color.WHITE
                        : (getModel().isPressed() ? bg.darker() : bg));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (bg.equals(Color.WHITE)) {
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 10, 7, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Events ────────────────────────────────────────────────────────────────
    private void initEvents() {
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row == -1) return;
                String maKH = getValAt(row, 0);
                txtMaKH.setText(maKH);
                txtTenKH.setText(getValAt(row, 1));
                txtSoDT.setText(getValAt(row, 2));
                Color readOnlyBg = new Color(245, 245, 245);
                txtTenKH.setEditable(false); txtTenKH.setBackground(readOnlyBg);
                txtSoDT.setEditable(false);  txtSoDT.setBackground(readOnlyBg);
            }
            private String getValAt(int r, int c) {
                Object v = tableModel.getValueAt(r, c); return v == null ? "" : v.toString();
            }
        });

        btnAdd.addActionListener(e -> {
            if (!txtTenKH.isEditable()) {
                // Đang ở chế độ xem → chuyển sang chế độ thêm mới
                txtMaKH.setText(kh_dao.getNextMaKH());
                txtTenKH.setText(""); txtTenKH.setEditable(true); txtTenKH.setBackground(Color.WHITE);
                txtSoDT.setText(""); txtSoDT.setEditable(true);  txtSoDT.setBackground(Color.WHITE);
                table.clearSelection();
                return;
            }
            addKhachHang();
        });
        btnUpdate.addActionListener(e -> {
            if (!txtTenKH.isEditable()) {
                // Bật chỉnh sửa để người dùng có thể thay đổi, bấm lại để lưu
                txtTenKH.setEditable(true); txtTenKH.setBackground(Color.WHITE);
                txtSoDT.setEditable(true);  txtSoDT.setBackground(Color.WHITE);
                return;
            }
            updateKhachHang();
        });
        btnDelete.addActionListener(e -> deleteKhachHang());
        btnClear.addActionListener(e -> { txtSearch.setText(""); currentPage = 1; loadDataToTable(); resetForm(); });
        btnSearch.addActionListener(e -> searchKhachHang());
        btnViewInvoice.addActionListener(e -> showInvoiceDialog());

        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; applyPage(); }
        });
        btnNextPage.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) allData.size() / pageSize);
            if (currentPage < totalPages) { currentPage++; applyPage(); }
        });
        cbPageSize.addActionListener(e -> {
            pageSize = Integer.parseInt((String) cbPageSize.getSelectedItem());
            currentPage = 1;
            applyPage();
        });
    }

    public void refreshData() { loadDataToTable(); resetForm(); }

    private void resetForm() {
        txtMaKH.setText(kh_dao.getNextMaKH());
        txtTenKH.setText(""); txtTenKH.setEditable(true); txtTenKH.setBackground(Color.WHITE);
        txtSoDT.setText(""); txtSoDT.setEditable(true); txtSoDT.setBackground(Color.WHITE);
        table.clearSelection();
    }

    private void loadDataToTable() {
        List<KhachHang> ds = kh_dao.getAllKhachHang();
        allData = ds != null ? ds : new ArrayList<>();
        currentPage = 1;
        applyPage();
    }

    private void applyPage() {
        tableModel.setRowCount(0);
        int total = allData.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        if (currentPage > totalPages && totalPages > 0) currentPage = totalPages;
        int from = (currentPage - 1) * pageSize;
        int to   = Math.min(from + pageSize, total);
        for (int i = from; i < to; i++) {
            KhachHang kh = allData.get(i);
            tableModel.addRow(new Object[]{kh.getMaKH(), kh.getTenKH(), kh.getSoDT()});
        }
        if (lblTotal != null) lblTotal.setText("Tổng: " + total + " khách hàng");
    }

    private void showInvoiceDialog() {
        String maKH = txtMaKH.getText().trim();
        if (maKH.isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn khách hàng để xem hóa đơn!"); return; }

        String[] ivCols = {"Mã HĐ", "Ngày lập", "Tổng tiền", "Trạng thái"};
        invoiceModel = new DefaultTableModel(ivCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable ivTable = new JTable(invoiceModel);
        ivTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ivTable.setRowHeight(28);
        ivTable.setGridColor(new Color(235, 235, 235));
        ivTable.setShowVerticalLines(false);
        ivTable.setBackground(Color.WHITE);
        ivTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        ivTable.getTableHeader().setBackground(new Color(248, 248, 248));
        ivTable.getTableHeader().setPreferredSize(new Dimension(0, 32));
        ivTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        ivTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        ivTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        ivTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val == null ? "" : val.toString();
                if (!sel) setForeground("Đã thanh toán".equals(s) ? GREEN_STATUS : RED_STATUS);
                else setForeground(TEXT_DARK);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        loadInvoiceHistory(maKH);

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Lịch sử hóa đơn — " + txtTenKH.getText().trim(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());
        dlg.add(new JScrollPane(ivTable), BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBottom.add(btnClose);
        dlg.add(pBottom, BorderLayout.SOUTH);

        dlg.setSize(520, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void loadInvoiceHistory(String maKH) {
        if (invoiceModel == null || maKH == null || maKH.isEmpty()) return;
        invoiceModel.setRowCount(0);
        List<HoaDon> all = hd_dao.getAllHoaDon();
        if (all == null) return;
        for (HoaDon hd : all) {
            if (hd.getKhachHang() != null && maKH.equals(hd.getKhachHang().getMaKH())) {
                String ngay = hd.getNgayLap() != null ? dateSdf.format(hd.getNgayLap()) : "";
                invoiceModel.addRow(new Object[]{
                        hd.getMaHD(), ngay,
                        String.format("%,.0fđ", hd.getTongTien()),
                        hd.isTrangThai() ? "Đã thanh toán" : "Chưa thanh toán"
                });
            }
        }
    }

    private void addKhachHang() {
        if (!validateForAdd()) return;
        String ma  = kh_dao.getNextMaKH();
        String ten = formatName(txtTenKH.getText().trim());
        String sdt = txtSoDT.getText().trim();
        KhachHang kh = new KhachHang(ma, ten, sdt);
        if (kh_dao.addKhachHang(kh)) {
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công! Mã: " + ma);
            loadDataToTable(); resetForm();
        } else JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại!");
    }

    private void updateKhachHang() {
        String ma = txtMaKH.getText().trim();
        if (ma.isEmpty() || ma.equals(kh_dao.getNextMaKH())) {
            JOptionPane.showMessageDialog(this, "Chọn khách hàng cần cập nhật từ bảng!"); return;
        }
        if (!validateForUpdate(ma)) return;
        String ten = formatName(txtTenKH.getText().trim());
        String sdt = txtSoDT.getText().trim();
        KhachHang kh = new KhachHang(ma, ten, sdt);
        if (kh_dao.updateKhachHang(kh)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDataToTable();
        } else JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
    }

    private void deleteKhachHang() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn khách hàng cần xóa!"); return; }
        String ma = tableModel.getValueAt(row, 0).toString();
        int c = JOptionPane.showConfirmDialog(this, "Xóa khách hàng " + ma + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (kh_dao.deleteKhachHang(ma)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadDataToTable(); resetForm();
            } else JOptionPane.showMessageDialog(this, "Xóa thất bại!");
        }
    }

    private void searchKhachHang() {
        String s = txtSearch.getText().trim();
        if (s.isEmpty()) { loadDataToTable(); return; }
        List<KhachHang> ds = kh_dao.getAllKhachHang();
        allData = new ArrayList<>();
        if (ds != null) {
            for (KhachHang kh : ds) {
                if (kh.getMaKH().toLowerCase().contains(s.toLowerCase())
                        || kh.getTenKH().toLowerCase().contains(s.toLowerCase())
                        || kh.getSoDT().contains(s)) {
                    allData.add(kh);
                }
            }
        }
        currentPage = 1;
        applyPage();
    }

    private String formatName(String name) {
        if (name == null || name.isEmpty()) return "";
        String[] words = name.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private boolean validateForAdd() {
        String ten = txtTenKH.getText().trim();
        String sdt = txtSoDT.getText().trim();
        if (ten.isEmpty()) { JOptionPane.showMessageDialog(this, "Tên KH không được trống!"); return false; }
        if (!sdt.matches("^0\\d{9}$")) { JOptionPane.showMessageDialog(this, "SĐT phải bắt đầu bằng 0, đủ 10 số!"); return false; }
        List<KhachHang> ds = kh_dao.getAllKhachHang();
        if (ds != null) {
            for (KhachHang kh : ds) {
                if (kh.getSoDT().equals(sdt)) { JOptionPane.showMessageDialog(this, "SĐT đã tồn tại!"); return false; }
            }
        }
        return true;
    }

    private boolean validateForUpdate(String currentMa) {
        String ten = txtTenKH.getText().trim();
        String sdt = txtSoDT.getText().trim();
        if (ten.isEmpty()) { JOptionPane.showMessageDialog(this, "Tên KH không được trống!"); return false; }
        if (!sdt.matches("^0\\d{9}$")) { JOptionPane.showMessageDialog(this, "SĐT phải bắt đầu bằng 0, đủ 10 số!"); return false; }
        List<KhachHang> ds = kh_dao.getAllKhachHang();
        if (ds != null) {
            for (KhachHang kh : ds) {
                if (!kh.getMaKH().equalsIgnoreCase(currentMa)) {
                    if (kh.getSoDT().equals(sdt)) { JOptionPane.showMessageDialog(this, "SĐT đã tồn tại!"); return false; }
                }
            }
        }
        return true;
    }
}
