package gui;

import dao.NhanVien_DAO;
import entity.ChucVu;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class QuanLyNhanVien extends JPanel {
    private JTextField txtMaNV, txtTenNV, txtSoDT, txtSoCCCD, txtSearch;
    private JComboBox<String> cbChucVu, cbTrangThai;
    private JButton btnAdd, btnUpdate, btnRemove, btnReset, btnClear, btnSearch;
    private JButton btnTabNhanVien, btnTabQuanLy;
    private JPanel listContainer;
    private NhanVien selectedNhanVien;
    private NhanVien_DAO nv_dao;
    private String currentFilter = "NHAN_VIEN";
    private int loadListVersion = 0;

    private final Color MAIN_BLUE  = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_DARK  = Color.decode("#333333");
    private final Color SELECTED_BG = Color.decode("#EBF5FB");
    private final Color BORDER_COLOR = Color.decode("#E0E0E0");

    public QuanLyNhanVien() {
        try { connectDB.ConnectDB.getInstance().connect(); } catch (Exception e) { e.printStackTrace(); }
        nv_dao = new NhanVien_DAO();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 30));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setBorder(new EmptyBorder(18, 0, 14, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pMain = new JPanel(new GridLayout(1, 2, 16, 0));
        pMain.setBackground(Color.WHITE);
        pMain.setBorder(new EmptyBorder(0, 16, 16, 16));
        pMain.add(createLeftPanel());
        pMain.add(createRightPanel());
        add(pMain, BorderLayout.CENTER);

        initEvents();
        setActiveTab(btnTabNhanVien);

        clearInputs();
        loadList();
    }

    // ---- LEFT: danh sách ----
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JLabel lbl = new JLabel("  DANH SÁCH NHÂN VIÊN");
        lbl.setFont(new Font("Inter Bold", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        lbl.setBorder(new EmptyBorder(12, 4, 10, 4));
        panel.add(lbl, BorderLayout.NORTH);

        JPanel pInner = new JPanel(new BorderLayout());
        pInner.setBackground(Color.WHITE);

        JPanel pTabs = new JPanel(new GridLayout(1, 2, 6, 0));
        pTabs.setBackground(Color.WHITE);
        pTabs.setBorder(new EmptyBorder(0, 10, 8, 10));
        btnTabNhanVien = makeTabBtn("Nhân Viên");
        btnTabQuanLy   = makeTabBtn("Quản Lý");
        pTabs.add(btnTabNhanVien);
        pTabs.add(btnTabQuanLy);
        pInner.add(pTabs, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);
        listContainer.setBorder(new EmptyBorder(4, 8, 4, 8));
        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        pInner.add(scroll, BorderLayout.CENTER);

        panel.add(pInner, BorderLayout.CENTER);
        return panel;
    }

    // ---- RIGHT: chi tiết ----
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JLabel lbl = new JLabel("  THÔNG TIN CHI TIẾT NHÂN VIÊN");
        lbl.setFont(new Font("Inter Bold", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        lbl.setBorder(new EmptyBorder(12, 4, 8, 4));
        panel.add(lbl, BorderLayout.NORTH);

        JPanel pContent = new JPanel(new BorderLayout(0, 10));
        pContent.setBackground(Color.WHITE);
        pContent.setBorder(new EmptyBorder(8, 20, 14, 20));

        // Avatar
        JPanel pAvatar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        pAvatar.setBackground(Color.WHITE);
        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblAvatar.setPreferredSize(new Dimension(62, 62));
        lblAvatar.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        pAvatar.add(lblAvatar);
        pContent.add(pAvatar, BorderLayout.NORTH);

        // Fields: 3 rows × 2 cols, label above field
        JPanel pFields = new JPanel(new GridLayout(3, 2, 16, 12));
        pFields.setBackground(Color.WHITE);

        txtMaNV = mkField();
        txtMaNV.setEditable(false);
        txtMaNV.setBackground(new Color(245, 245, 245));
        txtMaNV.setForeground(new Color(100, 100, 100));
        pFields.add(mkFieldGroup("Mã nhân viên:", txtMaNV));

        txtTenNV = mkField();
        pFields.add(mkFieldGroup("Họ tên:", txtTenNV));

        txtSoDT = mkField();
        pFields.add(mkFieldGroup("Số điện thoại:", txtSoDT));

        txtSoCCCD = mkField();
        pFields.add(mkFieldGroup("Số CCCD:", txtSoCCCD));

        cbChucVu = new JComboBox<>();
        for (ChucVu cv : ChucVu.values()) cbChucVu.addItem(cv.getTenHienThi());
        styleCombo(cbChucVu);
        pFields.add(mkFieldGroup("Chức vụ:", cbChucVu));

        cbTrangThai = new JComboBox<>(new String[]{"Đang làm việc"});
        styleCombo(cbTrangThai);
        pFields.add(mkFieldGroup("Trạng thái:", cbTrangThai));

        pContent.add(pFields, BorderLayout.CENTER);

        // Bottom: buttons + search
        JPanel pBottom = new JPanel(new BorderLayout(0, 0));
        pBottom.setBackground(Color.WHITE);

        JPanel pBtns = new JPanel(new GridLayout(1, 5, 6, 0));
        pBtns.setBackground(Color.WHITE);
        btnAdd    = mkColorBtn("Thêm nhân viên", MAIN_BLUE, Color.WHITE);
        btnUpdate = mkColorBtn("Cập nhật", GOLD_COLOR, MAIN_BLUE);
        btnRemove = mkColorBtn("Xóa nhân viên", Color.decode("#E74C3C"), Color.WHITE);
        btnReset  = mkColorBtn("Xóa trắng", Color.WHITE, TEXT_DARK);
        btnClear  = mkColorBtn("Làm mới", Color.WHITE, TEXT_DARK);
        pBtns.add(btnAdd); pBtns.add(btnUpdate); pBtns.add(btnRemove);
        pBtns.add(btnReset); pBtns.add(btnClear);
        pBottom.add(pBtns, BorderLayout.NORTH);

        // Search section
        JPanel pSearchSection = new JPanel(new BorderLayout(0, 6));
        pSearchSection.setBackground(Color.WHITE);
        pSearchSection.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblSearchHeader = new JLabel("TÌM KIẾM");
        lblSearchHeader.setFont(new Font("Inter Bold", Font.BOLD, 13));
        lblSearchHeader.setForeground(MAIN_BLUE);
        lblSearchHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(0, 0, 6, 0)));
        pSearchSection.add(lblSearchHeader, BorderLayout.NORTH);

        JPanel pSearchRow = new JPanel(new BorderLayout(8, 0));
        pSearchRow.setBackground(Color.WHITE);
        JLabel lblSearchLbl = new JLabel("Tìm kiếm (Mã/SĐT):");
        lblSearchLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSearchLbl.setForeground(TEXT_DARK);
        txtSearch = mkField();
        btnSearch = mkColorBtn("Tìm kiếm", MAIN_BLUE, Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(90, 30));
        pSearchRow.add(lblSearchLbl, BorderLayout.WEST);
        pSearchRow.add(txtSearch, BorderLayout.CENTER);
        pSearchRow.add(btnSearch, BorderLayout.EAST);
        pSearchSection.add(pSearchRow, BorderLayout.CENTER);

        pBottom.add(pSearchSection, BorderLayout.SOUTH);
        pContent.add(pBottom, BorderLayout.SOUTH);
        panel.add(pContent, BorderLayout.CENTER);
        return panel;
    }

    // ---- helpers ----
    private JButton makeTabBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setPreferredSize(new Dimension(0, 34));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel mkLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JTextField mkField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(0, 26));
        return f;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(0, 26));
    }

    private JButton mkBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(GOLD_COLOR);
        btn.setForeground(MAIN_BLUE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 8, 6, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 10, 7, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

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

    // ---- tabs ----
    private void setActiveTab(JButton active) {
        btnTabNhanVien.setBackground(Color.WHITE); btnTabNhanVien.setForeground(TEXT_DARK);
        btnTabQuanLy.setBackground(Color.WHITE);   btnTabQuanLy.setForeground(TEXT_DARK);
        active.setBackground(MAIN_BLUE); active.setForeground(Color.WHITE);
    }

    // ---- events ----
    private void initEvents() {
        btnTabNhanVien.addActionListener(e -> { currentFilter = "NHAN_VIEN"; setActiveTab(btnTabNhanVien); clearInputs(); loadList(); });
        btnTabQuanLy.addActionListener(e -> { currentFilter = "QUAN_LY"; setActiveTab(btnTabQuanLy); clearInputs(); loadList(); });
        btnAdd.addActionListener(e -> addNhanVien());
        btnUpdate.addActionListener(e -> updateNhanVien());
        btnRemove.addActionListener(e -> deleteNhanVien());
        btnReset.addActionListener(e -> clearInputs());
        btnClear.addActionListener(e -> { txtSearch.setText(""); clearInputs(); loadList(); });
        btnSearch.addActionListener(e -> searchNhanVien());

        cbChucVu.addActionListener(e -> {
            if (cbChucVu.getSelectedItem() == null) return;
            if (selectedNhanVien != null) return;
            if (txtTenNV.getText().trim().isEmpty()) return;
            ChucVu cv = ChucVu.fromString(cbChucVu.getSelectedItem().toString());
            String prefix = cv == ChucVu.QUAN_LY ? "QL" : "NV";
            txtMaNV.setText(nv_dao.getNextMaByPrefix(prefix));
        });
    }

    // ---- load list ----
    public void refreshData() { loadList(); }

    private void loadList() {
        final String filter = currentFilter;
        final int version = ++loadListVersion;
        listContainer.removeAll();
        listContainer.revalidate();
        listContainer.repaint();
        new SwingWorker<List<NhanVien>, Void>() {
            @Override
            protected List<NhanVien> doInBackground() {
                return nv_dao.getAllNhanVien();
            }
            @Override
            protected void done() {
                if (version != loadListVersion) return;
                try {
                    List<NhanVien> ds = get();
                    listContainer.removeAll();
                    if (ds == null) { listContainer.revalidate(); listContainer.repaint(); return; }
                    long countNV = ds.stream().filter(n -> n.getChucVu() == ChucVu.NHAN_VIEN).count();
                    long countQL = ds.stream().filter(n -> n.getChucVu() == ChucVu.QUAN_LY).count();
                    btnTabNhanVien.setText("Nhân Viên (" + countNV + ")");
                    btnTabQuanLy.setText("Quản Lý (" + countQL + ")");
                    for (NhanVien nv : ds) {
                        boolean match = "NHAN_VIEN".equals(filter)
                                ? nv.getChucVu() == ChucVu.NHAN_VIEN
                                : nv.getChucVu() == ChucVu.QUAN_LY;
                        if (!match) continue;
                        listContainer.add(makeEmployeeCard(nv));
                        listContainer.add(Box.createVerticalStrut(4));
                    }
                    listContainer.revalidate();
                    listContainer.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private JPanel makeEmployeeCard(NhanVien nv) {
        boolean isSelected = selectedNhanVien != null && selectedNhanVien.getMaNV().equals(nv.getMaNV());
        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = selectedNhanVien != null && selectedNhanVien.getMaNV().equals(nv.getMaNV());
                g2.setColor(sel ? SELECTED_BG : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblMa = new JLabel(nv.getMaNV());
        lblMa.setFont(new Font("Inter Bold", Font.BOLD, 13));
        lblMa.setForeground(MAIN_BLUE);
        lblMa.setPreferredSize(new Dimension(52, 18));

        JLabel lblTen = new JLabel(nv.getTenNV());
        lblTen.setFont(new Font("Inter", Font.PLAIN, 13));
        lblTen.setForeground(TEXT_DARK);

        card.add(lblMa, BorderLayout.WEST);
        card.add(lblTen, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedNhanVien = nv;
                fillForm(nv);
                listContainer.repaint();
            }
            @Override public void mouseEntered(MouseEvent e) { card.repaint(); }
            @Override public void mouseExited(MouseEvent e) { card.repaint(); }
        });
        return card;
    }

    private void fillForm(NhanVien nv) {
        txtMaNV.setText(nv.getMaNV());
        txtTenNV.setText(nv.getTenNV());
        txtSoDT.setText(nv.getSoDT() != null ? nv.getSoDT() : "");
        txtSoCCCD.setText(nv.getSoCCCD() != null ? nv.getSoCCCD() : "");
        cbChucVu.setSelectedItem(nv.getChucVu().getTenHienThi());
        cbTrangThai.setSelectedIndex(0);
    }

    private void clearInputs() {
        selectedNhanVien = null;
        txtMaNV.setText("");
        txtTenNV.setText(""); txtSoDT.setText(""); txtSoCCCD.setText("");
        cbChucVu.setSelectedIndex(0); cbTrangThai.setSelectedIndex(0);
        listContainer.repaint();
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

    private void addNhanVien() {
        if (!validateData(true)) return;
        String ten  = formatName(txtTenNV.getText().trim());
        String sdt  = txtSoDT.getText().trim();
        String cccd = txtSoCCCD.getText().trim();
        boolean tt  = "Đang làm việc".equals(cbTrangThai.getSelectedItem().toString());
        ChucVu cv = ChucVu.fromString(cbChucVu.getSelectedItem().toString());
        String prefix = cv == ChucVu.QUAN_LY ? "QL" : "NV";
        String maNV = nv_dao.getNextMaByPrefix(prefix);
        NhanVien nv = new NhanVien(maNV, ten, sdt, cccd, cv, tt, null);
        if (nv_dao.addNhanVien(nv)) {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
            loadList(); clearInputs();
        } else JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!");
    }

    private void updateNhanVien() {
        String ma = txtMaNV.getText().trim();
        if (ma.isEmpty()) {
            if (cbChucVu.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn chức vụ!"); return; }
            ChucVu cvAuto = ChucVu.fromString(cbChucVu.getSelectedItem().toString());
            String prefixAuto = cvAuto == ChucVu.QUAN_LY ? "QL" : "NV";
            ma = nv_dao.getNextMaByPrefix(prefixAuto);
            txtMaNV.setText(ma);
        }
        if (!validateData(false)) return;
        String ten  = formatName(txtTenNV.getText().trim());
        String sdt  = txtSoDT.getText().trim();
        String cccd = txtSoCCCD.getText().trim();
        boolean tt  = "Đang làm việc".equals(cbTrangThai.getSelectedItem().toString());
        NhanVien nv = new NhanVien(ma, ten, sdt, cccd,
                ChucVu.fromString(cbChucVu.getSelectedItem().toString()), tt, null);
        List<NhanVien> ds = nv_dao.getAllNhanVien();
        final String maFinal = ma;
        boolean exists = ds != null && ds.stream().anyMatch(n -> n.getMaNV().equals(maFinal));
        boolean ok = exists ? nv_dao.updateNhanVien(nv) : nv_dao.addNhanVien(nv);
        if (ok) {
            JOptionPane.showMessageDialog(this, exists ? "Cập nhật thành công!" : "Thêm mới thành công!");
            loadList(); clearInputs();
        } else JOptionPane.showMessageDialog(this, "Lưu thất bại!");
    }

    private void deleteNhanVien() {
        if (selectedNhanVien == null) { JOptionPane.showMessageDialog(this, "Chọn nhân viên cần xóa!"); return; }
        int c = JOptionPane.showConfirmDialog(this,
                "Xóa nhân viên " + selectedNhanVien.getMaNV() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION && nv_dao.deleteNhanVien(selectedNhanVien.getMaNV())) {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadList(); clearInputs();
        }
    }

    private void searchNhanVien() {
        String s = txtSearch.getText().trim();
        if (s.isEmpty()) { loadList(); return; }
        listContainer.removeAll();
        List<NhanVien> ds = nv_dao.getAllNhanVien();
        if (ds == null) { listContainer.revalidate(); listContainer.repaint(); return; }
        boolean found = false;
        for (NhanVien nv : ds) {
            if (nv.getMaNV().equalsIgnoreCase(s)
                    || (nv.getSoDT() != null && nv.getSoDT().equals(s))
                    || nv.getTenNV().toLowerCase().contains(s.toLowerCase())) {
                listContainer.add(makeEmployeeCard(nv));
                listContainer.add(Box.createVerticalStrut(4));
                found = true;
            }
        }
        if (!found) { JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên!"); loadList(); }
        listContainer.revalidate(); listContainer.repaint();
    }

    private boolean validateData(boolean isAdd) {
        String ma   = txtMaNV.getText().trim();
        String ten  = txtTenNV.getText().trim();
        String sdt  = txtSoDT.getText().trim();
        String cccd = txtSoCCCD.getText().trim();
        if (ten.isEmpty()) { JOptionPane.showMessageDialog(this, "Tên không được để trống!"); return false; }
        if (!sdt.matches("^0\\d{9}$")) { JOptionPane.showMessageDialog(this, "SĐT phải bắt đầu bằng 0, đủ 10 số!"); return false; }
        if (!cccd.matches("^\\d{12}$")) { JOptionPane.showMessageDialog(this, "CCCD phải có đúng 12 số!"); return false; }
        List<NhanVien> ds = nv_dao.getAllNhanVien();
        if (ds != null) {
            for (NhanVien nv : ds) {
                if (nv.getSoDT() != null && nv.getSoDT().equals(sdt) && (isAdd || !nv.getMaNV().equalsIgnoreCase(ma)))
                    { JOptionPane.showMessageDialog(this, "SĐT đã tồn tại!"); return false; }
                if (nv.getSoCCCD() != null && nv.getSoCCCD().equals(cccd) && (isAdd || !nv.getMaNV().equalsIgnoreCase(ma)))
                    { JOptionPane.showMessageDialog(this, "CCCD đã tồn tại!"); return false; }
                // maNV is auto-generated — no duplicate check needed
            }
        }
        return true;
    }
}
