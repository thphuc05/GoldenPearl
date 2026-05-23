package gui;

import dao.NhanVien_DAO;
import dao.TaiKhoan_DAO;
import entity.ChucVu;
import entity.NhanVien;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class QuanLyNhanVien extends JPanel {
    private JTextField txtMaNV, txtTenNV, txtSoDT, txtSoCCCD, txtEmail, txtTenTK, txtSearch;
    private JComboBox<String> cbChucVu, cbTrangThai;
    private JButton btnAdd, btnUpdate, btnChoNghi, btnReset, btnClear, btnSearch;
    private JButton btnTabNhanVien, btnTabQuanLy, btnTabBep, btnTabDaNghi;
    private JPanel listContainer;
    private NhanVien selectedNhanVien;
    private NhanVien_DAO nv_dao;
    private TaiKhoan_DAO tk_dao;
    private String currentFilter = "NHAN_VIEN";
    private int loadListVersion = 0;

    private final Color MAIN_BLUE  = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_DARK  = Color.decode("#333333");
    private final Color SELECTED_BG = Color.decode("#EBF5FB");
    private final Color BORDER_COLOR = Color.decode("#E0E0E0");

    private static final Color BG_LIGHT = Color.decode("#F0F2F5");

    public QuanLyNhanVien() {
        try { connectDB.ConnectDB.getInstance().connect(); } catch (Exception e) { e.printStackTrace(); }
        nv_dao = new NhanVien_DAO();
        tk_dao = new TaiKhoan_DAO();
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(true);
        pHeader.setBackground(MAIN_BLUE);
        pHeader.setBorder(new EmptyBorder(10, 24, 10, 24));
        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(GOLD_COLOR);
        JLabel lblSub = new JLabel("Quản lý thông tin và phân quyền nhân viên");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(180, 200, 220));
        JPanel pTBox = new JPanel(); pTBox.setLayout(new BoxLayout(pTBox, BoxLayout.Y_AXIS)); pTBox.setOpaque(false);
        pTBox.add(lblTitle); pTBox.add(Box.createVerticalStrut(2)); pTBox.add(lblSub);
        pHeader.add(pTBox, BorderLayout.WEST);
        add(pHeader, BorderLayout.NORTH);

        JPanel pMain = new JPanel(new GridLayout(1, 2, 16, 0));
        pMain.setOpaque(false);
        pMain.setBorder(new EmptyBorder(0, 20, 20, 20));
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
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        JLabel lbl = new JLabel("  DANH SÁCH NHÂN VIÊN");
        lbl.setFont(new Font("Inter Bold", Font.BOLD, 16));
        lbl.setForeground(MAIN_BLUE);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 6, 12, 6)));
        panel.add(lbl, BorderLayout.NORTH);

        JPanel pInner = new JPanel(new BorderLayout());
        pInner.setOpaque(false);

        JPanel pTabs = new JPanel(new GridLayout(1, 4, 6, 0));
        pTabs.setOpaque(false);
        pTabs.setBorder(new EmptyBorder(8, 10, 8, 10));
        btnTabNhanVien = makeTabBtn("Nhân Viên");
        btnTabQuanLy   = makeTabBtn("Quản Lý");
        btnTabBep      = makeTabBtn("Nhà Bếp");
        btnTabDaNghi   = makeTabBtn("Đã Nghỉ");
        pTabs.add(btnTabNhanVien);
        pTabs.add(btnTabQuanLy);
        pTabs.add(btnTabBep);
        pTabs.add(btnTabDaNghi);
        pInner.add(pTabs, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);
        listContainer.setBorder(new EmptyBorder(6, 10, 6, 10));
        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        pInner.add(scroll, BorderLayout.CENTER);

        panel.add(pInner, BorderLayout.CENTER);
        return panel;
    }

    // ---- RIGHT: chi tiết ----
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        JLabel lbl = new JLabel("  THÔNG TIN CHI TIẾT NHÂN VIÊN");
        lbl.setFont(new Font("Inter Bold", Font.BOLD, 16));
        lbl.setForeground(MAIN_BLUE);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 6, 12, 6)));
        panel.add(lbl, BorderLayout.NORTH);

        JPanel pContent = new JPanel(new BorderLayout(0, 12));
        pContent.setOpaque(false);
        pContent.setBorder(new EmptyBorder(12, 20, 16, 20));

        // Avatar
        JPanel pAvatar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        pAvatar.setBackground(Color.WHITE);
        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblAvatar.setPreferredSize(new Dimension(62, 62));
        lblAvatar.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        pAvatar.add(lblAvatar);
        pContent.add(pAvatar, BorderLayout.NORTH);

        // Fields: 5 rows × 2 cols, label above field
        JPanel pFields = new JPanel(new GridLayout(5, 2, 16, 10));
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

        txtEmail = mkField();
        pFields.add(mkFieldGroup("Email:", txtEmail));

        txtTenTK = mkField();
        pFields.add(mkFieldGroup("Tên đăng nhập:", txtTenTK));

        cbChucVu = new JComboBox<>();
        for (ChucVu cv : ChucVu.values()) cbChucVu.addItem(cv.getTenHienThi());
        styleCombo(cbChucVu);
        pFields.add(mkFieldGroup("Chức vụ:", cbChucVu));

        cbTrangThai = new JComboBox<>(new String[]{"Đang làm việc", "Đã nghỉ"});
        styleCombo(cbTrangThai);
        pFields.add(mkFieldGroup("Trạng thái:", cbTrangThai));

        JPanel pPwNote = new JPanel(new BorderLayout());
        pPwNote.setOpaque(false);
        JLabel lblPwNote = new JLabel("Mật khẩu mặc định: 123456");
        lblPwNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPwNote.setForeground(new Color(150, 150, 150));
        pPwNote.add(lblPwNote, BorderLayout.CENTER);
        pFields.add(pPwNote);

        pContent.add(pFields, BorderLayout.CENTER);

        // Bottom: buttons + search
        JPanel pBottom = new JPanel(new BorderLayout(0, 0));
        pBottom.setBackground(Color.WHITE);

        JPanel pBtns = new JPanel(new GridLayout(1, 5, 6, 0));
        pBtns.setBackground(Color.WHITE);
        btnAdd     = mkColorBtn("Thêm nhân viên", MAIN_BLUE, Color.WHITE);
        btnUpdate  = mkColorBtn("Cập nhật", GOLD_COLOR, MAIN_BLUE);
        btnChoNghi = mkColorBtn("Cho nghỉ", Color.decode("#E67E22"), Color.WHITE);
        btnReset   = mkColorBtn("Xóa trắng", Color.WHITE, TEXT_DARK);
        btnClear   = mkColorBtn("Làm mới", Color.WHITE, TEXT_DARK);
        pBtns.add(btnAdd); pBtns.add(btnUpdate); pBtns.add(btnChoNghi);
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
        btn.setFont(new Font("Inter Bold", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setPreferredSize(new Dimension(0, 38));
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
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(0, 36));
        return f;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(0, 36));
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
                g2.setColor(bg.equals(Color.WHITE) ? BORDER_COLOR : bg.darker());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel mkFieldGroup(String label, JComponent field) {
        JPanel g = new JPanel(new BorderLayout(0, 5));
        g.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        g.add(lbl, BorderLayout.NORTH);
        g.add(field, BorderLayout.CENTER);
        return g;
    }

    // ---- tabs ----
    private void setActiveTab(JButton active) {
        for (JButton b : new JButton[]{btnTabNhanVien, btnTabQuanLy, btnTabBep, btnTabDaNghi}) {
            b.setBackground(Color.WHITE); b.setForeground(TEXT_DARK);
        }
        active.setBackground(MAIN_BLUE); active.setForeground(Color.WHITE);
    }

    // ---- events ----
    private void initEvents() {
        btnTabNhanVien.addActionListener(e -> { currentFilter = "NHAN_VIEN"; setActiveTab(btnTabNhanVien); clearInputs(); loadList(); });
        btnTabQuanLy.addActionListener(e -> { currentFilter = "QUAN_LY"; setActiveTab(btnTabQuanLy); clearInputs(); loadList(); });
        btnTabBep.addActionListener(e -> { currentFilter = "BEP"; setActiveTab(btnTabBep); clearInputs(); loadList(); });
        btnTabDaNghi.addActionListener(e -> { currentFilter = "DA_NGHI"; setActiveTab(btnTabDaNghi); clearInputs(); loadList(); });
        btnAdd.addActionListener(e -> addNhanVien());
        btnUpdate.addActionListener(e -> updateNhanVien());
        btnChoNghi.addActionListener(e -> choNghi());
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
                    long countNV    = ds.stream().filter(n -> n.isTrangThai() && n.getChucVu() == ChucVu.NHAN_VIEN).count();
                    long countQL    = ds.stream().filter(n -> n.isTrangThai() && n.getChucVu() == ChucVu.QUAN_LY).count();
                    long countBep   = ds.stream().filter(n -> n.isTrangThai() && n.getChucVu() == ChucVu.BEP).count();
                    long countNghi  = ds.stream().filter(n -> !n.isTrangThai()).count();
                    btnTabNhanVien.setText("Nhân Viên (" + countNV + ")");
                    btnTabQuanLy.setText("Quản Lý (" + countQL + ")");
                    btnTabBep.setText("Nhà Bếp (" + countBep + ")");
                    btnTabDaNghi.setText("Đã Nghỉ (" + countNghi + ")");
                    for (NhanVien nv : ds) {
                        boolean match;
                        if ("DA_NGHI".equals(filter)) {
                            match = !nv.isTrangThai();
                        } else if ("BEP".equals(filter)) {
                            match = nv.isTrangThai() && nv.getChucVu() == ChucVu.BEP;
                        } else if ("NHAN_VIEN".equals(filter)) {
                            match = nv.isTrangThai() && nv.getChucVu() == ChucVu.NHAN_VIEN;
                        } else {
                            match = nv.isTrangThai() && nv.getChucVu() == ChucVu.QUAN_LY;
                        }
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

    private static final Color INACTIVE_BG = new Color(255, 220, 220);

    private JPanel makeEmployeeCard(NhanVien nv) {
        boolean isSelected = selectedNhanVien != null && selectedNhanVien.getMaNV().equals(nv.getMaNV());
        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = selectedNhanVien != null && selectedNhanVien.getMaNV().equals(nv.getMaNV());
                Color base = !nv.isTrangThai() ? INACTIVE_BG : Color.WHITE;
                g2.setColor(sel ? SELECTED_BG : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblMa = new JLabel(nv.getMaNV());
        lblMa.setFont(new Font("Inter Bold", Font.BOLD, 15));
        lblMa.setForeground(MAIN_BLUE);
        lblMa.setPreferredSize(new Dimension(62, 20));

        JLabel lblTen = new JLabel(nv.getTenNV());
        lblTen.setFont(new Font("Inter", Font.PLAIN, 15));
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
        txtEmail.setText(nv.getEmail() != null ? nv.getEmail() : "");
        // Hiện tên TK nếu đã có, đặt read-only để không sửa nhầm
        if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getMaTK() != null) {
            TaiKhoan tk = tk_dao.getTaiKhoanByMaTK(nv.getTaiKhoan().getMaTK());
            txtTenTK.setText(tk != null ? tk.getTenTK() : "");
            txtTenTK.setEditable(false);
            txtTenTK.setBackground(new Color(245, 245, 245));
        } else {
            txtTenTK.setText("");
            txtTenTK.setEditable(true);
            txtTenTK.setBackground(Color.WHITE);
        }
        cbChucVu.setSelectedItem(nv.getChucVu().getTenHienThi());
        cbTrangThai.setSelectedIndex(nv.isTrangThai() ? 0 : 1);
    }

    private void clearInputs() {
        selectedNhanVien = null;
        txtMaNV.setText(""); txtTenNV.setText("");
        txtSoDT.setText(""); txtSoCCCD.setText("");
        txtEmail.setText("");
        txtTenTK.setText(""); txtTenTK.setEditable(true); txtTenTK.setBackground(Color.WHITE);
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
        String ten    = formatName(txtTenNV.getText().trim());
        String sdt    = txtSoDT.getText().trim();
        String cccd   = txtSoCCCD.getText().trim();
        String email  = txtEmail.getText().trim().toLowerCase();
        String tenTK  = txtTenTK.getText().trim();
        boolean tt    = "Đang làm việc".equals(cbTrangThai.getSelectedItem().toString());
        ChucVu cv     = ChucVu.fromString(cbChucVu.getSelectedItem().toString());
        String prefix = cv == ChucVu.QUAN_LY ? "QL" : "NV";
        String maNV   = nv_dao.getNextMaByPrefix(prefix);

        // Tạo TaiKhoan trước
        TaiKhoan tk = null;
        if (!tenTK.isEmpty()) {
            if (tk_dao.isTenTKExists(tenTK)) {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập '" + tenTK + "' đã tồn tại!");
                return;
            }
            String maTK   = tk_dao.getNextMaTK();
            String vaiTro = cv == ChucVu.QUAN_LY ? "QUAN_LY" : (cv == ChucVu.BEP ? "BEP" : "NHAN_VIEN");
            if (tk_dao.createTaiKhoan(maTK, tenTK, vaiTro)) {
                tk = new TaiKhoan(maTK, tenTK, null, vaiTro);
            } else {
                JOptionPane.showMessageDialog(this, "Tạo tài khoản thất bại!");
                return;
            }
        }

        NhanVien nv = new NhanVien(maNV, ten, sdt, cccd, cv, tt, tk);
        nv.setEmail(email.isEmpty() ? null : email);
        if (nv_dao.addNhanVien(nv)) {
            String msg = "Thêm nhân viên thành công!";
            if (tk != null) msg += "\nTài khoản: " + tenTK + "\nMật khẩu mặc định: 123456";
            JOptionPane.showMessageDialog(this, msg);
            loadList(); clearInputs();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!");
        }
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
        String ten   = formatName(txtTenNV.getText().trim());
        String sdt   = txtSoDT.getText().trim();
        String cccd  = txtSoCCCD.getText().trim();
        String email = txtEmail.getText().trim().toLowerCase();
        boolean tt   = "Đang làm việc".equals(cbTrangThai.getSelectedItem().toString());
        NhanVien nv  = new NhanVien(ma, ten, sdt, cccd,
                ChucVu.fromString(cbChucVu.getSelectedItem().toString()), tt, null);
        nv.setEmail(email.isEmpty() ? null : email);
        List<NhanVien> ds = nv_dao.getAllNhanVien();
        final String maFinal = ma;
        boolean exists = ds != null && ds.stream().anyMatch(n -> n.getMaNV().equals(maFinal));
        boolean ok = exists ? nv_dao.updateNhanVien(nv) : nv_dao.addNhanVien(nv);
        if (ok) {
            JOptionPane.showMessageDialog(this, exists ? "Cập nhật thành công!" : "Thêm mới thành công!");
            loadList(); clearInputs();
        } else JOptionPane.showMessageDialog(this, "Lưu thất bại!");
    }

    private void choNghi() {
        if (selectedNhanVien == null) { JOptionPane.showMessageDialog(this, "Chọn nhân viên cần cho nghỉ!"); return; }
        int c = JOptionPane.showConfirmDialog(this,
                "Cho nghỉ nhân viên " + selectedNhanVien.getTenNV() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION && nv_dao.setTrangThai(selectedNhanVien.getMaNV(), false)) {
            JOptionPane.showMessageDialog(this, "Đã cho nhân viên nghỉ!");
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
        String ma    = txtMaNV.getText().trim();
        String ten   = txtTenNV.getText().trim();
        String sdt   = txtSoDT.getText().trim();
        String cccd  = txtSoCCCD.getText().trim();
        String email = txtEmail.getText().trim();
        if (ten.isEmpty()) { JOptionPane.showMessageDialog(this, "Tên không được để trống!"); return false; }
        if (!sdt.matches("^0\\d{9}$")) { JOptionPane.showMessageDialog(this, "SĐT phải bắt đầu bằng 0, đủ 10 số!"); return false; }
        if (!cccd.matches("^\\d{12}$")) { JOptionPane.showMessageDialog(this, "CCCD phải có đúng 12 số!"); return false; }
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng!"); return false;
        }
        String tenTK = txtTenTK.getText().trim();
        if (!tenTK.isEmpty() && tenTK.length() < 4) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập phải có ít nhất 4 ký tự!"); return false;
        }
        if (!tenTK.isEmpty() && !tenTK.matches("^[a-zA-Z0-9_.]+$")) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập chỉ được chứa chữ, số, dấu _ hoặc ."); return false;
        }
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
