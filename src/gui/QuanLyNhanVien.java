package gui;

import dao.NhanVien_DAO;
import entity.NhanVien;
import entity.ChucVu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class QuanLyNhanVien extends JPanel {
    private JTextField txtMaNV, txtTenNV, txtSoDT, txtSoCCCD, txtSearch;
    private JComboBox<String> cbChucVu, cbTrangThai;
    private JButton btnAdd, btnUpdate, btnRemove, btnReset, btnClear, btnSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private NhanVien_DAO nv_dao;

    private final Color MAIN_BLUE = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_WHITE = Color.WHITE;

    public QuanLyNhanVien() {
        try {
            connectDB.ConnectDB.getInstance().connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        nv_dao = new NhanVien_DAO();
        setLayout(new BorderLayout()); // Sử dụng BorderLayout truyền thống
        setBackground(MAIN_BLUE);

        // Header
        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 32));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        lblTitle.setForeground(GOLD_COLOR);
        add(lblTitle, BorderLayout.NORTH);

        // Main content
        JPanel pMain = new JPanel(new BorderLayout());
        pMain.setOpaque(false);
        pMain.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Form panel
        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setOpaque(false);
        TitledBorder formBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_COLOR), "Thông tin chi tiết nhân viên");
        formBorder.setTitleColor(GOLD_COLOR);
        pForm.setBorder(formBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Ép giãn ngang
        gbc.anchor = GridBagConstraints.WEST;

// Row 0
        gbc.gridy = 0;

// Mã NV
        gbc.gridx = 0; gbc.weightx = 0;
        pForm.add(createLabel("Mã nhân viên:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; // Giãn 50% hàng
        txtMaNV = new JTextField();
        txtMaNV.setPreferredSize(new Dimension(300, 35));
        txtMaNV.setEditable(true);
        pForm.add(txtMaNV, gbc);

// Tên NV
        gbc.gridx = 2; gbc.weightx = 0;
        pForm.add(createLabel("Tên nhân viên:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; // Giãn 50% hàng
        txtTenNV = new JTextField();
        txtTenNV.setPreferredSize(new Dimension(300, 35));
        pForm.add(txtTenNV, gbc);

// Row 1
        gbc.gridy = 1;

// Số điện thoại
        gbc.gridx = 0; gbc.weightx = 0;
        pForm.add(createLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        txtSoDT = new JTextField();
        txtSoDT.setPreferredSize(new Dimension(300, 35));
        pForm.add(txtSoDT, gbc);

// Số CCCD
        gbc.gridx = 2; gbc.weightx = 0;
        pForm.add(createLabel("Số CCCD:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        txtSoCCCD = new JTextField();
        txtSoCCCD.setPreferredSize(new Dimension(300, 35));
        pForm.add(txtSoCCCD, gbc);

// Row 2
        gbc.gridy = 2;

// Chức vụ
        gbc.gridx = 0; gbc.weightx = 0;
        pForm.add(createLabel("Chức vụ:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        cbChucVu = new JComboBox<>();
        for (ChucVu cv : ChucVu.values()) {
            cbChucVu.addItem(cv.getTenHienThi());
        }
        cbChucVu.setPreferredSize(new Dimension(300, 35));
        cbChucVu.setBackground(Color.WHITE);
        cbChucVu.setForeground(MAIN_BLUE);
        // Đảm bảo danh sách xổ xuống có màu hiển thị tốt
        cbChucVu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? GOLD_COLOR : Color.WHITE);
                setForeground(MAIN_BLUE);
                return this;
            }
        });
        pForm.add(cbChucVu, gbc);

        // Trạng thái
        gbc.gridx = 2; gbc.weightx = 0;
        pForm.add(createLabel("Trạng thái:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        cbTrangThai = new JComboBox<>(new String[]{"Đang làm việc", "Nghỉ việc"});
        cbTrangThai.setPreferredSize(new Dimension(300, 35));
        cbTrangThai.setBackground(Color.WHITE);
        cbTrangThai.setForeground(MAIN_BLUE);
        cbTrangThai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? GOLD_COLOR : Color.WHITE);
                setForeground(MAIN_BLUE);
                return this;
            }
        });
        pForm.add(cbTrangThai, gbc);

        pMain.add(pForm, BorderLayout.NORTH);

        // Table panel
        String[] columnNames = {"Mã NV", "Họ tên", "SĐT", "Số CCCD", "Chức vụ", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 15));
        table.getTableHeader().setBackground(GOLD_COLOR);
        table.getTableHeader().setForeground(MAIN_BLUE);
        
        table.setBackground(Color.decode("#EBF5FB"));
        table.setForeground(MAIN_BLUE);
        table.setSelectionBackground(GOLD_COLOR);
        table.setSelectionForeground(MAIN_BLUE);

        JScrollPane scrollPane = new JScrollPane(table);
        TitledBorder tableBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_COLOR), "Danh sách nhân viên nhà hàng");
        tableBorder.setTitleColor(GOLD_COLOR);
        scrollPane.setBorder(tableBorder);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        pMain.add(scrollPane, BorderLayout.CENTER);

        // Control panel
        JPanel pControl = new JPanel(new BorderLayout());
        pControl.setOpaque(false);
        pControl.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Buttons
        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pButtons.setOpaque(false);
        btnAdd = createStyledButton("Thêm nhân viên");
        btnUpdate = createStyledButton("Cập nhật");
        btnRemove = createStyledButton("Xóa nhân viên");
        btnReset = createStyledButton("Xóa trắng");
        btnClear = createStyledButton("Làm mới");
        pButtons.add(btnAdd);
        pButtons.add(btnUpdate);
        pButtons.add(btnRemove);
        pButtons.add(btnReset);
        pButtons.add(btnClear);
        pControl.add(pButtons, BorderLayout.NORTH);

        // Search
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pSearch.setOpaque(false);
        pSearch.add(createLabel("Tìm kiếm (Mã/SDT):"));
        txtSearch = new JTextField(30);
        btnSearch = createStyledButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(140, 40));
        pSearch.add(txtSearch);
        pSearch.add(btnSearch);
        pControl.add(pSearch, BorderLayout.SOUTH);

        pMain.add(pControl, BorderLayout.SOUTH);
        add(pMain, BorderLayout.CENTER);

        // Khởi tạo sự kiện
        initEvents();
        
        // 1. Tự động nạp dữ liệu ngay khi mở giao diện
        loadDataToTable();

        // 2. Tự động cập nhật lại bảng mỗi 30 giây (Polling)
        Timer autoRefreshTimer = new Timer(30000, e -> {
            // Chỉ tự động nạp lại nếu người dùng không đang nhập liệu (các ô đang trống)
            if (txtTenNV.getText().trim().isEmpty() && txtSearch.getText().trim().isEmpty()) {
                loadDataToTable();
            }
        });
        autoRefreshTimer.start();
    }

    public void refreshData() {
        loadDataToTable();
    }

    private void initEvents() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    txtMaNV.setText(tableModel.getValueAt(row, 0).toString());
                    txtTenNV.setText(tableModel.getValueAt(row, 1).toString());
                    txtSoDT.setText(tableModel.getValueAt(row, 2).toString());
                    txtSoCCCD.setText(tableModel.getValueAt(row, 3).toString());
                    cbChucVu.setSelectedItem(tableModel.getValueAt(row, 4).toString());
                    cbTrangThai.setSelectedItem(tableModel.getValueAt(row, 5).toString());
                }
            }
        });

        btnAdd.addActionListener(e -> addNhanVien());
        btnUpdate.addActionListener(e -> updateNhanVien());
        btnRemove.addActionListener(e -> deleteNhanVien()); // Xóa khỏi SQL
        btnReset.addActionListener(e -> clearInputs()); // Chỉ xóa ô nhập
        btnClear.addActionListener(e -> { // Làm mới: Hiện tất cả
            txtSearch.setText("");
            clearInputs();
            loadDataToTable();
        });
        btnSearch.addActionListener(e -> searchNhanVien());

        // Lọc thời gian thực khi chọn Chức vụ hoặc Trạng thái
        cbChucVu.addActionListener(e -> filterData());
        cbTrangThai.addActionListener(e -> filterData());
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_WHITE);
        lbl.setFont(new Font("Inter Medium", Font.PLAIN, 14));
        return lbl;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 14));
        btn.setBackground(GOLD_COLOR);
        btn.setForeground(MAIN_BLUE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<NhanVien> dsNV = nv_dao.getAllNhanVien();
        
        if (dsNV == null) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu! Vui lòng kiểm tra lại cấu hình SQL Server.", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (NhanVien nv : dsNV) {
            tableModel.addRow(new Object[]{
                nv.getMaNV(),
                nv.getTenNV(),
                nv.getSoDT(),
                nv.getSoCCCD(),
                nv.getChucVu().getTenHienThi(),
                nv.isTrangThai() ? "Đang làm việc" : "Nghỉ việc"
            });
        }
    }

    private String formatName(String name) {
        if (name == null || name.isEmpty()) return "";
        String[] words = name.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void filterData() {
        if (!txtMaNV.getText().isEmpty() || !txtTenNV.getText().isEmpty()) return;
        
        String cvFilter = cbChucVu.getSelectedItem().toString();
        String ttFilter = cbTrangThai.getSelectedItem().toString();
        
        tableModel.setRowCount(0);
        List<NhanVien> dsNV = nv_dao.getAllNhanVien();
        if (dsNV == null) return;
        
        for (NhanVien nv : dsNV) {
            boolean matchesCV = cvFilter.equals(nv.getChucVu().getTenHienThi());
            boolean matchesTT = ttFilter.equals(nv.isTrangThai() ? "Đang làm việc" : "Nghỉ việc");
            
            if (matchesCV && matchesTT) {
                tableModel.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getTenNV(),
                    nv.getSoDT(),
                    nv.getSoCCCD(),
                    nv.getChucVu().getTenHienThi(),
                    nv.isTrangThai() ? "Đang làm việc" : "Nghỉ việc"
                });
            }
        }
    }

    private void addNhanVien() {
        if (!validateData(true)) return;

        String ten = formatName(txtTenNV.getText().trim());
        String sdt = txtSoDT.getText().trim();
        String cccd = txtSoCCCD.getText().trim();
        String chucVuTen = cbChucVu.getSelectedItem().toString();
        boolean trangThai = cbTrangThai.getSelectedItem().toString().equals("Đang làm việc");

        String maNV = nv_dao.getNextMaNV();
        
        NhanVien nv = new NhanVien(maNV, ten, sdt, cccd, ChucVu.fromString(chucVuTen), trangThai, null);
        if (nv_dao.addNhanVien(nv)) {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
            loadDataToTable();
            clearInputs();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!");
        }
    }

    private void updateNhanVien() {
        String ma = txtMaNV.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã nhân viên cần cập nhật!");
            txtMaNV.requestFocus();
            return;
        }
        
        if (!validateData(false)) return;

        // Kiểm tra xem mã này có tồn tại trong SQL không
        List<NhanVien> ds = nv_dao.getAllNhanVien();
        boolean exists = false;
        if (ds != null) {
            for (NhanVien nv : ds) {
                if (nv.getMaNV().equalsIgnoreCase(ma)) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên mã " + ma + " để cập nhật!");
            return;
        }

        String ten = formatName(txtTenNV.getText().trim());
        String sdt = txtSoDT.getText().trim();
        String cccd = txtSoCCCD.getText().trim();
        String chucVuTen = cbChucVu.getSelectedItem().toString();
        boolean trangThai = cbTrangThai.getSelectedItem().toString().equals("Đang làm việc");

        NhanVien nv = new NhanVien(ma, ten, sdt, cccd, ChucVu.fromString(chucVuTen), trangThai, null);
        if (nv_dao.updateNhanVien(nv)) {
            JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu vào SQL thành công!");
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
    }

    private void deleteNhanVien() {
        // Giữ lại hàm này phòng hờ nhưng nút Xóa đã chuyển sang clearInputs theo yêu cầu
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên dưới bảng để xóa!");
            return;
        }
        String ma = tableModel.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên " + ma + " khỏi hệ thống?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (nv_dao.deleteNhanVien(ma)) {
                JOptionPane.showMessageDialog(this, "Đã xóa nhân viên khỏi SQL thành công!");
                loadDataToTable();
                clearInputs();
            }
        }
    }

    private void clearInputs() {
        txtMaNV.setText("");
        txtTenNV.setText("");
        txtSoDT.setText("");
        txtSoCCCD.setText("");
        cbChucVu.setSelectedIndex(0);
        cbTrangThai.setSelectedIndex(0);
        table.clearSelection();
    }

    private void clearFields() {
        clearInputs();
        txtSearch.setText("");
        loadDataToTable();
    }

    private void searchNhanVien() {
        String s = txtSearch.getText().trim();
        if (s.isEmpty()) {
            loadDataToTable();
            return;
        }
        
        tableModel.setRowCount(0);
        List<NhanVien> dsAll = nv_dao.getAllNhanVien();
        if (dsAll == null) return;
        
        boolean found = false;
        for (NhanVien nv : dsAll) {
            // Tìm chính xác theo mã hoặc SĐT
            if (nv.getMaNV().equalsIgnoreCase(s) || nv.getSoDT().equals(s)) {
                tableModel.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getTenNV(),
                    nv.getSoDT(),
                    nv.getSoCCCD(),
                    nv.getChucVu().getTenHienThi(),
                    nv.isTrangThai() ? "Đang làm việc" : "Nghỉ việc"
                });
                found = true;
            }
        }
        
        if (!found) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên với Mã/SĐT: " + s);
            loadDataToTable();
        }
    }

    private boolean validateData(boolean isAdd) {
        String ma = txtMaNV.getText().trim();
        String ten = txtTenNV.getText().trim();
        String sdt = txtSoDT.getText().trim();
        String cccd = txtSoCCCD.getText().trim();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhân viên không được để trống!");
            txtTenNV.requestFocus();
            return false;
        }
        
        if (!sdt.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số!");
            txtSoDT.requestFocus();
            return false;
        }
        
        if (!cccd.matches("^\\d{12}$")) {
            JOptionPane.showMessageDialog(this, "Số CCCD phải có đúng 12 chữ số!");
            txtSoCCCD.requestFocus();
            return false;
        }

        // Kiểm tra trùng lặp trong Database
        List<NhanVien> ds = nv_dao.getAllNhanVien();
        if (ds != null) {
            for (NhanVien nv : ds) {
                // Kiểm tra trùng SĐT
                if (nv.getSoDT().equals(sdt) && (isAdd || !nv.getMaNV().equalsIgnoreCase(ma))) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại " + sdt + " đã tồn tại!");
                    txtSoDT.requestFocus();
                    return false;
                }
                // Kiểm tra trùng CCCD
                if (nv.getSoCCCD().equals(cccd) && (isAdd || !nv.getMaNV().equalsIgnoreCase(ma))) {
                    JOptionPane.showMessageDialog(this, "Số CCCD " + cccd + " đã tồn tại!");
                    txtSoCCCD.requestFocus();
                    return false;
                }
                // Kiểm tra trùng Mã khi thêm mới (nếu người dùng tự nhập mã)
                if (isAdd && !ma.isEmpty() && nv.getMaNV().equalsIgnoreCase(ma)) {
                    JOptionPane.showMessageDialog(this, "Mã nhân viên " + ma + " đã tồn tại!");
                    txtMaNV.requestFocus();
                    return false;
                }
            }
        }
        
        return true;
    }
}
