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
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        pForm.add(createLabel("Mã khách hàng:"), gbc);
        gbc.gridx = 1;
        txtMaKH = new JTextField(20);
        txtMaKH.setEditable(false);
        pForm.add(txtMaKH, gbc);

        gbc.gridx = 2;
        pForm.add(createLabel("Tên khách hàng:"), gbc);
        gbc.gridx = 3;
        txtTenKH = new JTextField(20);
        pForm.add(txtTenKH, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        pForm.add(createLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1;
        txtSoDT = new JTextField(20);
        pForm.add(txtSoDT, gbc);

        gbc.gridx = 2;
        pForm.add(createLabel("Email:"), gbc);
        gbc.gridx = 3;
        txtEmail = new JTextField(20);
        pForm.add(txtEmail, gbc);

        pMain.add(pForm, BorderLayout.NORTH);

        // Table panel
        String[] columnNames = {"Mã KH", "Tên khách hàng", "Số điện thoại", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 15));
        table.getTableHeader().setBackground(GOLD_COLOR);
        table.getTableHeader().setForeground(MAIN_BLUE);
        
        table.setBackground(new Color(255, 255, 255, 240));
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
                    txtMaKH.setText(tableModel.getValueAt(row, 0).toString());
                    txtTenKH.setText(tableModel.getValueAt(row, 1).toString());
                    txtSoDT.setText(tableModel.getValueAt(row, 2).toString());
                    txtEmail.setText(tableModel.getValueAt(row, 3).toString());
                }
            }
        });

        btnAdd.addActionListener(e -> addKhachHang());
        btnUpdate.addActionListener(e -> updateKhachHang());
        btnDelete.addActionListener(e -> deleteKhachHang());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchKhachHang());

        // Load data initially
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
                tableModel.addRow(new Object[]{kh.getMaKH(), kh.getTenKH(), kh.getSoDT(), kh.getEmail()});
            }
        }
    }

    private void addKhachHang() {
        String ten = txtTenKH.getText().trim();
        String sdt = txtSoDT.getText().trim();
        String email = txtEmail.getText().trim();

        if (ten.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tên và Số điện thoại!");
            return;
        }

        if (!sdt.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải có 10-11 chữ số!");
            return;
        }

        // Tự sinh mã khách hàng dựa trên số lượng hiện tại
        String maKH = "KH" + String.format("%03d", kh_dao.getAllKhachHang().size() + 1);

        KhachHang kh = new KhachHang(maKH, ten, sdt, email);
        if (kh_dao.addKhachHang(kh)) {
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công và đã lưu vào SQL!");
            loadDataToTable();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại!");
        }
    }

    private void updateKhachHang() {
        String ma = txtMaKH.getText();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần cập nhật từ bảng!");
            return;
        }

        String ten = txtTenKH.getText().trim();
        String sdt = txtSoDT.getText().trim();
        String email = txtEmail.getText().trim();

        KhachHang kh = new KhachHang(ma, ten, sdt, email);
        if (kh_dao.updateKhachHang(kh)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
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
