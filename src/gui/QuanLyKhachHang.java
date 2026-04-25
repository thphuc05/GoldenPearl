package gui;

import dao.KhachHang_DAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class QuanLyKhachHang extends JPanel {
    private JTextField txtMaKH, txtTenKH, txtSoDT, txtEmail, txtSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private KhachHang_DAO kh_dao;

    private final Color MAIN_BLUE = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_WHITE = Color.WHITE;

    public QuanLyKhachHang() {
        kh_dao = new KhachHang_DAO();
        setLayout(new BorderLayout());
        setBackground(MAIN_BLUE);

        // Header
        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG", SwingConstants.CENTER);
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
                BorderFactory.createLineBorder(GOLD_COLOR), "Thông tin khách hàng");
        formBorder.setTitleColor(GOLD_COLOR);
        pForm.setBorder(formBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 25, 12, 25); // Tăng padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

// Row 0
        gbc.gridy = 0;

// Cột 0: Nhãn Mã KH
        gbc.gridx = 0; gbc.weightx = 0;
        pForm.add(createLabel("Mã khách hàng:"), gbc);

// Cột 1: Ô nhập Mã KH (Giãn rộng)
        gbc.gridx = 1; gbc.weightx = 0.5;
        txtMaKH = new JTextField();
        txtMaKH.setPreferredSize(new Dimension(300, 35)); // Tăng kích thước
        txtMaKH.setEditable(true);
        pForm.add(txtMaKH, gbc);

// Cột 2: Nhãn Tên KH
        gbc.gridx = 2; gbc.weightx = 0;
        pForm.add(createLabel("Tên khách hàng:"), gbc);

// Cột 3: Ô nhập Tên KH (Giãn rộng)
        gbc.gridx = 3; gbc.weightx = 0.5;
        txtTenKH = new JTextField();
        txtTenKH.setPreferredSize(new Dimension(300, 35));
        pForm.add(txtTenKH, gbc);

// Row 1
        gbc.gridy = 1;

// Cột 0: Nhãn SĐT
        gbc.gridx = 0; gbc.weightx = 0;
        pForm.add(createLabel("Số điện thoại:"), gbc);

// Cột 1: Ô nhập SĐT
        gbc.gridx = 1; gbc.weightx = 0.5;
        txtSoDT = new JTextField();
        txtSoDT.setPreferredSize(new Dimension(300, 35));
        pForm.add(txtSoDT, gbc);

// Cột 2: Nhãn Email
        gbc.gridx = 2; gbc.weightx = 0;
        pForm.add(createLabel("Email:"), gbc);

// Cột 3: Ô nhập Email
        gbc.gridx = 3; gbc.weightx = 0.5;
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(300, 35));
        pForm.add(txtEmail, gbc);
        pForm.add(txtEmail, gbc);

        pMain.add(pForm, BorderLayout.NORTH);

        // Table panel
        String[] columnNames = {"Mã KH", "Tên khách hàng", "Số điện thoại", "Email"};
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
                BorderFactory.createLineBorder(GOLD_COLOR), "Danh sách khách hàng");
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
        btnAdd = createStyledButton("Thêm khách hàng");
        btnUpdate = createStyledButton("Cập nhật");
        btnDelete = createStyledButton("Xóa");
        btnClear = createStyledButton("Làm mới");
        pButtons.add(btnAdd);
        pButtons.add(btnUpdate);
        pButtons.add(btnDelete);
        pButtons.add(btnClear);
        pControl.add(pButtons, BorderLayout.NORTH);

        // Search
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pSearch.setOpaque(false);
        JLabel lblSearch = createLabel("Tìm kiếm (SĐT/Tên):");
        pSearch.add(lblSearch);
        txtSearch = new JTextField(25);
        btnSearch = createStyledButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(120, 35));
        pSearch.add(txtSearch);
        pSearch.add(btnSearch);
        pControl.add(pSearch, BorderLayout.SOUTH);

        pMain.add(pControl, BorderLayout.SOUTH);

        add(pMain, BorderLayout.CENTER);

        // Events
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    txtMaKH.setText(getValueOrEmpty(row, 0));
                    txtTenKH.setText(getValueOrEmpty(row, 1));
                    txtSoDT.setText(getValueOrEmpty(row, 2));
                    txtEmail.setText(getValueOrEmpty(row, 3));
                }
            }
            
            private String getValueOrEmpty(int row, int col) {
                Object val = tableModel.getValueAt(row, col);
                return (val == null) ? "" : val.toString();
            }
        });

        btnAdd.addActionListener(e -> addKhachHang());
        btnUpdate.addActionListener(e -> updateKhachHang());
        btnDelete.addActionListener(e -> deleteKhachHang());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchKhachHang());

        // Đã gỡ bỏ loadDataToTable() để tăng tốc đăng nhập
    }

    public void refreshData() {
        loadDataToTable();
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
        List<KhachHang> dsKH = kh_dao.getAllKhachHang();
        if (dsKH != null) {
            for (KhachHang kh : dsKH) {
                tableModel.addRow(new Object[]{
                    kh.getMaKH(), 
                    kh.getTenKH(), 
                    kh.getSoDT(), 
                    (kh.getEmail() == null) ? "" : kh.getEmail()
                });
            }
        }
    }

    private void addKhachHang() {
        if (!validateData(true)) return;

        String maKH = txtMaKH.getText().trim();
        String ten = formatName(txtTenKH.getText().trim());
        String sdt = txtSoDT.getText().trim();
        String email = txtEmail.getText().trim();

        KhachHang kh = new KhachHang(maKH, ten, sdt, email);
        if (kh_dao.addKhachHang(kh)) {
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
            loadDataToTable();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại!");
        }
    }

    private void updateKhachHang() {
        String ma = txtMaKH.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã khách hàng cần cập nhật!");
            txtMaKH.requestFocus();
            return;
        }

        if (!validateData(false)) return;

        // Kiểm tra xem khách hàng có tồn tại trong SQL không
        boolean exists = false;
        List<KhachHang> ds = kh_dao.getAllKhachHang();
        for (KhachHang kh : ds) {
            if (kh.getMaKH().equalsIgnoreCase(ma)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng mã " + ma + " để cập nhật!");
            return;
        }

        String ten = formatName(txtTenKH.getText().trim());
        String sdt = txtSoDT.getText().trim();
        String email = txtEmail.getText().trim();

        KhachHang kh = new KhachHang(ma, ten, sdt, email);
        if (kh_dao.updateKhachHang(kh)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin vào SQL thành công!");
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
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

    private boolean validateData(boolean isAdd) {
        String ma = txtMaKH.getText().trim();
        String ten = txtTenKH.getText().trim();
        String sdt = txtSoDT.getText().trim();
        String email = txtEmail.getText().trim();

        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã khách hàng không được để trống!");
            txtMaKH.requestFocus();
            return false;
        }

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên khách hàng không được để trống!");
            txtTenKH.requestFocus();
            return false;
        }

        if (!sdt.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số!");
            txtSoDT.requestFocus();
            return false;
        }

        if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!");
            txtEmail.requestFocus();
            return false;
        }

        // Kiểm tra trùng lặp trong Database
        List<KhachHang> ds = kh_dao.getAllKhachHang();
        if (ds != null) {
            for (KhachHang kh : ds) {
                // Kiểm tra trùng Mã khi thêm mới
                if (isAdd && kh.getMaKH().equalsIgnoreCase(ma)) {
                    JOptionPane.showMessageDialog(this, "Mã khách hàng " + ma + " đã tồn tại!");
                    txtMaKH.requestFocus();
                    return false;
                }
                // Kiểm tra trùng SĐT (loại trừ khách hàng đang cập nhật)
                if (kh.getSoDT().equals(sdt) && (isAdd || !kh.getMaKH().equalsIgnoreCase(ma))) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại " + sdt + " đã được đăng ký bởi khách hàng " + kh.getMaKH() + "!");
                    txtSoDT.requestFocus();
                    return false;
                }
                // Kiểm tra trùng Email
                if (!email.isEmpty() && email.equalsIgnoreCase(kh.getEmail()) && (isAdd || !kh.getMaKH().equalsIgnoreCase(ma))) {
                    JOptionPane.showMessageDialog(this, "Email " + email + " đã được đăng ký bởi khách hàng " + kh.getMaKH() + "!");
                    txtEmail.requestFocus();
                    return false;
                }
            }
        }
        return true;
    }

    private void deleteKhachHang() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!");
            return;
        }

        String ma = tableModel.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (kh_dao.deleteKhachHang(ma)) {
                JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công!");
                loadDataToTable();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa khách hàng thất bại!");
            }
        }
    }

    private void clearFields() {
        txtMaKH.setText("");
        txtTenKH.setText("");
        txtSoDT.setText("");
        txtEmail.setText("");
        txtSearch.setText("");
        table.clearSelection();
        loadDataToTable();
    }

    private void searchKhachHang() {
        String searchVal = txtSearch.getText().trim();
        if (searchVal.isEmpty()) {
            loadDataToTable();
            return;
        }

        tableModel.setRowCount(0);
        List<KhachHang> dsKH = kh_dao.getAllKhachHang();
        for (KhachHang kh : dsKH) {
            if (kh.getMaKH().contains(searchVal) || kh.getSoDT().contains(searchVal) || kh.getTenKH().toLowerCase().contains(searchVal.toLowerCase())) {
                tableModel.addRow(new Object[]{kh.getMaKH(), kh.getTenKH(), kh.getSoDT(), kh.getEmail()});
            }
        }
    }
}
