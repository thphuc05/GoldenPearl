package gui;

import dao.LoaiSanPham_DAO;
import dao.SanPham_DAO;
import entity.LoaiSanPham;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class QuanLyMonAn extends JPanel {
    private JTextField txtMaMon, txtTenMon, txtGiaGoc, txtGiaBan;
    private JTextField txtSearchMa, txtSearchTen;
    private JComboBox<String> cbDanhMuc, cbTrangThai;
    private JButton btnAdd, btnUpdate, btnRemove, btnClearInputs, btnClear, btnSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private SanPham_DAO sp_dao;
    private LoaiSanPham_DAO lsp_dao;
    private List<LoaiSanPham> dsLoai;
    private boolean isFiltering = false;
    private boolean isEditingExisting = false;

    private final Color MAIN_BLUE   = Color.decode("#0B3D59");
    private final Color GOLD_COLOR  = Color.decode("#C5A059");
    private final Color TEXT_DARK   = Color.decode("#333333");
    private final Color BORDER_COLOR = Color.decode("#E0E0E0");
    private final Color SELECT_BG   = Color.decode("#EBF5FB");

    public QuanLyMonAn() {
        sp_dao  = new SanPham_DAO();
        lsp_dao = new LoaiSanPham_DAO();

        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        add(createTopSection(), BorderLayout.NORTH);
        add(createCenterSection(), BorderLayout.CENTER);
        add(createBottomSection(), BorderLayout.SOUTH);
        bindEvents();
    }

    // ============ TOP: title + search ============
    private JPanel createTopSection() {
        JPanel top = new JPanel(new BorderLayout(0, 0));
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(14, 16, 8, 16));

        JLabel lblTitle = new JLabel("QUẢN LÝ MÓN ĂN");
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_DARK);
        top.add(lblTitle, BorderLayout.WEST);

        JPanel pSearch = new JPanel(new GridBagLayout());
        pSearch.setBackground(Color.WHITE);
        pSearch.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_COLOR), "BỘ LỌC TÌM KIẾM",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Inter Bold", Font.BOLD, 12), TEXT_DARK));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pSearch.add(mkLbl("Mã món:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtSearchMa = mkField(150);
        pSearch.add(txtSearchMa, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        pSearch.add(mkLbl("Tên món:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtSearchTen = mkField(150);
        pSearch.add(txtSearchTen, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        btnSearch = mkBtn("Tìm", GOLD_COLOR, MAIN_BLUE);
        pSearch.add(btnSearch, gbc);

        top.add(pSearch, BorderLayout.EAST);
        return top;
    }

    // ============ CENTER: table ============
    private JPanel createCenterSection() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(0, 16, 0, 16));
        String[] cols = {"Mã món", "Tên món", "Danh mục", "Giá gốc", "Giá bán", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 248, 248));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(235, 235, 235));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(SELECT_BG);
        table.setSelectionForeground(TEXT_DARK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "DANH SÁCH MÓN ĂN");
        tb.setTitleFont(new Font("Inter Bold", Font.BOLD, 13));
        tb.setTitleColor(TEXT_DARK);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(tb);
        center.add(scroll, BorderLayout.CENTER);
        return center;
    }

    // ============ BOTTOM: detail form ============
    private JPanel createBottomSection() {
        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(new EmptyBorder(8, 16, 14, 16));
        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setBackground(Color.WHITE);
        TitledBorder fb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "THÔNG TIN CHI TIẾT MÓN ĂN");
        fb.setTitleFont(new Font("Inter Bold", Font.BOLD, 13));
        fb.setTitleColor(TEXT_DARK);
        pForm.setBorder(fb);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Mã món + Tên món
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pForm.add(mkLbl("Mã món:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        txtMaMon = mkField(0);
        txtMaMon.setEditable(false);
        txtMaMon.setBackground(new Color(245, 245, 245));
        pForm.add(txtMaMon, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        pForm.add(mkLbl("Tên món:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        txtTenMon = mkField(0);
        pForm.add(txtTenMon, gbc);

        // Row 1: Giá gốc + Giá bán
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pForm.add(mkLbl("Giá gốc (VNĐ):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        txtGiaGoc = mkField(0);
        pForm.add(txtGiaGoc, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        pForm.add(mkLbl("Giá bán:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        txtGiaBan = mkField(0);
        pForm.add(txtGiaBan, gbc);

        // Row 2: Danh mục + Trạng thái
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        pForm.add(mkLbl("Danh mục:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        cbDanhMuc = new JComboBox<>();
        styleCombo(cbDanhMuc);
        loadCategories();
        pForm.add(cbDanhMuc, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        pForm.add(mkLbl("Trạng thái:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        cbTrangThai = new JComboBox<>(new String[]{"Còn món", "Hết món"});
        styleCombo(cbTrangThai);
        pForm.add(cbTrangThai, gbc);

        // Row 3: Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; gbc.weightx = 1;
        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pBtns.setBackground(Color.WHITE);
        btnAdd         = mkBtn("Thêm món",  GOLD_COLOR,    MAIN_BLUE);
        btnUpdate      = mkBtn("Cập nhật",  MAIN_BLUE,     Color.WHITE);
        btnRemove      = mkBtn("Hủy món",   new Color(220, 53, 69), Color.WHITE);
        btnClearInputs = mkBtn("Xóa trắng", Color.WHITE,   TEXT_DARK);
        btnClearInputs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 17, 6, 17)));
        btnClear       = mkBtn("Làm mới",   Color.WHITE,   TEXT_DARK);
        btnClear.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 17, 6, 17)));
        pBtns.add(btnAdd); pBtns.add(btnUpdate); pBtns.add(btnRemove);
        pBtns.add(btnClearInputs); pBtns.add(btnClear);
        pForm.add(pBtns, gbc);

        bottom.add(pForm, BorderLayout.CENTER);
        return bottom;
    }

    // ============ events ============
    private void bindEvents() {
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) fillFormFromRow(row);
            }
        });

        btnSearch.addActionListener(e -> searchMonAn());
        btnAdd.addActionListener(e -> addMonAn());
        btnUpdate.addActionListener(e -> updateMonAn());
        btnRemove.addActionListener(e -> deleteMonAn());
        btnClearInputs.addActionListener(e -> clearInputs());
        btnClear.addActionListener(e -> {
            txtSearchMa.setText(""); txtSearchTen.setText("");
            clearInputs();
            loadDataToTable();
        });

        cbDanhMuc.addActionListener(e -> {
            if (isFiltering || cbDanhMuc.getSelectedIndex() == -1) return;
            filterByCategory();
            if (!isEditingExisting) {
                LoaiSanPham loai = getSelectedLoai();
                if (loai != null) txtMaMon.setText(generateNextMaMon(loai));
            }
        });
    }

    private void fillFormFromRow(int row) {
        isEditingExisting = true;
        isFiltering = true;
        txtMaMon.setText(getVal(row, 0));
        txtTenMon.setText(getVal(row, 1));
        cbDanhMuc.setSelectedItem(getVal(row, 2));
        isFiltering = false;
        txtGiaGoc.setText(getVal(row, 3).replace("đ", "").replace(",", ""));
        txtGiaBan.setText(getVal(row, 4).replace("đ", "").replace(",", ""));
        cbTrangThai.setSelectedItem(getVal(row, 5));
    }

    public void refreshData() { loadDataToTable(); loadCategories(); }

    // ---- data ----
    private void loadCategories() {
        isFiltering = true;
        dsLoai = lsp_dao.getAllLoaiSanPham();
        cbDanhMuc.removeAllItems();
        if (dsLoai != null) {
            for (LoaiSanPham l : dsLoai) cbDanhMuc.addItem(l.getTenLoai());
        }
        cbDanhMuc.setSelectedIndex(-1);
        isFiltering = false;
    }

    private void loadDataToTable() {
        new SwingWorker<List<SanPham>, Void>() {
            @Override
            protected List<SanPham> doInBackground() {
                return sp_dao.getAllSanPham();
            }
            @Override
            protected void done() {
                try {
                    List<SanPham> ds = get();
                    tableModel.setRowCount(0);
                    if (ds != null) {
                        for (SanPham sp : ds) {
                            tableModel.addRow(new Object[]{
                                    sp.getMaMon(), sp.getTenMon(),
                                    sp.getLoaiSanPham().getTenLoai(),
                                    String.format("%,.0fđ", sp.getGiaGoc()),
                                    String.format("%,.0fđ", sp.getGiaBan()),
                                    sp.isTrangThai() ? "Còn món" : "Hết món"
                            });
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void filterByCategory() {
        if (cbDanhMuc.getSelectedItem() == null) return;
        String cat = cbDanhMuc.getSelectedItem().toString();
        tableModel.setRowCount(0);
        List<SanPham> ds = sp_dao.getAllSanPham();
        if (ds != null) {
            for (SanPham sp : ds) {
                if (sp.getLoaiSanPham().getTenLoai().equals(cat)) {
                    tableModel.addRow(new Object[]{sp.getMaMon(), sp.getTenMon(),
                            sp.getLoaiSanPham().getTenLoai(),
                            String.format("%,.0fđ", sp.getGiaGoc()),
                            String.format("%,.0fđ", sp.getGiaBan()),
                            sp.isTrangThai() ? "Còn món" : "Hết món"});
                }
            }
        }
    }

    private void searchMonAn() {
        String keyMa  = txtSearchMa.getText().trim().toLowerCase();
        String keyTen = txtSearchTen.getText().trim().toLowerCase();
        if (keyMa.isEmpty() && keyTen.isEmpty()) { loadDataToTable(); return; }
        tableModel.setRowCount(0);
        List<SanPham> ds = sp_dao.getAllSanPham();
        if (ds != null) {
            for (SanPham sp : ds) {
                boolean matchMa  = keyMa.isEmpty()  || sp.getMaMon().toLowerCase().contains(keyMa);
                boolean matchTen = keyTen.isEmpty() || sp.getTenMon().toLowerCase().contains(keyTen);
                if (matchMa && matchTen) {
                    tableModel.addRow(new Object[]{sp.getMaMon(), sp.getTenMon(),
                            sp.getLoaiSanPham().getTenLoai(),
                            String.format("%,.0fđ", sp.getGiaGoc()),
                            String.format("%,.0fđ", sp.getGiaBan()),
                            sp.isTrangThai() ? "Còn món" : "Hết món"});
                }
            }
        }
    }

    private void addMonAn() {
        try {
            String ma = txtMaMon.getText().trim(), ten = txtTenMon.getText().trim();
            if (ma.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục để tạo mã món!"); return; }
            if (ten.isEmpty()) { JOptionPane.showMessageDialog(this, "Tên món không được trống!"); return; }
            List<SanPham> ds = sp_dao.getAllSanPham();
            for (SanPham sp : ds) {
                if (sp.getMaMon().equalsIgnoreCase(ma)) { JOptionPane.showMessageDialog(this, "Mã món đã tồn tại!"); return; }
                if (sp.getTenMon().equalsIgnoreCase(ten)) { JOptionPane.showMessageDialog(this, "Tên món đã tồn tại!"); return; }
            }
            double giaGoc = Double.parseDouble(txtGiaGoc.getText().trim().replace(",", ""));
            double giaBan = Double.parseDouble(txtGiaBan.getText().trim().replace(",", ""));
            boolean tt = "Còn món".equals(cbTrangThai.getSelectedItem().toString());
            LoaiSanPham loai = getSelectedLoai();
            SanPham sp = new SanPham(ma, ten, giaGoc, giaBan, "", tt, loai, "");
            if (sp_dao.addSanPham(sp)) { JOptionPane.showMessageDialog(this, "Thêm món thành công!"); loadDataToTable(); clearInputs(); }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!"); }
    }

    private void updateMonAn() {
        try {
            String ma = txtMaMon.getText().trim();
            if (ma.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập mã món cần cập nhật!"); return; }
            String ten = txtTenMon.getText().trim();
            double giaGoc = Double.parseDouble(txtGiaGoc.getText().trim().replace(",", ""));
            double giaBan = Double.parseDouble(txtGiaBan.getText().trim().replace(",", ""));
            boolean tt = "Còn món".equals(cbTrangThai.getSelectedItem().toString());
            LoaiSanPham loai = getSelectedLoai();
            SanPham sp = new SanPham(ma, ten, giaGoc, giaBan, "", tt, loai, "");
            if (sp_dao.updateSanPham(sp)) { JOptionPane.showMessageDialog(this, "Cập nhật thành công!"); loadDataToTable(); }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi cập nhật!"); }
    }

    private void deleteMonAn() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn món cần xóa!"); return; }
        String ma = tableModel.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Xóa món " + ma + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (sp_dao.deleteSanPham(ma)) { loadDataToTable(); clearInputs(); }
        }
    }

    private String generateNextMaMon(LoaiSanPham loai) {
        // "LSP_KV" → prefix "SP_KV", then find max sequence number among existing items
        String prefix = "SP_" + loai.getMaLoai().substring("LSP_".length());
        List<SanPham> all = sp_dao.getAllSanPham();
        int maxNum = 0;
        for (SanPham sp : all) {
            String ma = sp.getMaMon();
            if (ma.startsWith(prefix)) {
                try {
                    int n = Integer.parseInt(ma.substring(prefix.length()));
                    if (n > maxNum) maxNum = n;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%02d", prefix, maxNum + 1);
    }

    private void clearInputs() {
        isEditingExisting = false;
        txtMaMon.setText(""); txtTenMon.setText(""); txtGiaGoc.setText(""); txtGiaBan.setText("");
        cbTrangThai.setSelectedIndex(0);
        isFiltering = true; cbDanhMuc.setSelectedIndex(-1); isFiltering = false;
        table.clearSelection();
    }

    private LoaiSanPham getSelectedLoai() {
        if (cbDanhMuc.getSelectedItem() == null || dsLoai == null) return null;
        String ten = cbDanhMuc.getSelectedItem().toString();
        for (LoaiSanPham l : dsLoai) if (l.getTenLoai().equals(ten)) return l;
        return null;
    }

    private String getVal(int row, int col) {
        Object v = tableModel.getValueAt(row, col); return v == null ? "" : v.toString();
    }

    // ---- helpers ----
    private JLabel mkLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter Bold", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JTextField mkField(int w) {
        JTextField f = new JTextField();
        f.setFont(new Font("Inter", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.setBackground(Color.WHITE);
        if (w > 0) f.setPreferredSize(new Dimension(w, 34));
        else f.setPreferredSize(new Dimension(0, 34));
        return f;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Inter", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(0, 34));
    }

    private JButton mkBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
