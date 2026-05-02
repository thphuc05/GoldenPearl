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
    private JComboBox<String> cbChucVu;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private NhanVien_DAO nv_dao;

    private final Color MAIN_BLUE = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_WHITE = Color.WHITE;

    public QuanLyNhanVien() {
        nv_dao = new NhanVien_DAO();
        setLayout(new BorderLayout());
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
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        pForm.add(createLabel("Mã nhân viên:"), gbc);
        gbc.gridx = 1;
        txtMaNV = new JTextField(20);
        txtMaNV.setEditable(false);
        pForm.add(txtMaNV, gbc);

        gbc.gridx = 2;
        pForm.add(createLabel("Tên nhân viên:"), gbc);
        gbc.gridx = 3;
        txtTenNV = new JTextField(20);
        pForm.add(txtTenNV, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        pForm.add(createLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1;
        txtSoDT = new JTextField(20);
        pForm.add(txtSoDT, gbc);

        gbc.gridx = 2;
        pForm.add(createLabel("Số CCCD:"), gbc);
        gbc.gridx = 3;
        txtSoCCCD = new JTextField(20);
        pForm.add(txtSoCCCD, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        pForm.add(createLabel("Chức vụ:"), gbc);
        gbc.gridx = 1;
        cbChucVu = new JComboBox<>(new String[]{"QUAN_LY", "NHAN_VIEN", "DAU_BEP", "TIEP_TAN"});
        pForm.add(cbChucVu, gbc);

        pMain.add(pForm, BorderLayout.NORTH);

        // Table panel
        String[] columnNames = {"Mã NV", "Họ tên", "SĐT", "Số CCCD", "Chức vụ"};
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
        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        pButtons.setOpaque(false);
        btnAdd = createStyledButton("Thêm nhân viên");
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
        pSearch.add(createLabel("Tìm kiếm (Mã/Tên):"));
        txtSearch = new JTextField(30);
        btnSearch = createStyledButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(140, 40));
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
                    txtMaNV.setText(tableModel.getValueAt(row, 0).toString());
                    txtTenNV.setText(tableModel.getValueAt(row, 1).toString());
                    txtSoDT.setText(tableModel.getValueAt(row, 2).toString());
                    txtSoCCCD.setText(tableModel.getValueAt(row, 3).toString());
                    cbChucVu.setSelectedItem(tableModel.getValueAt(row, 4).toString());
                }
            }
        });

        btnAdd.addActionListener(e -> addNhanVien());
        btnUpdate.addActionListener(e -> updateNhanVien());
        btnDelete.addActionListener(e -> deleteNhanVien());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchNhanVien());

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
        List<NhanVien> dsNV = nv_dao.getAllNhanVien();
        if (dsNV != null) {
            for (NhanVien nv : dsNV) {
                tableModel.addRow(new Object[]{
                    nv.getMaNV(),
                    nv.getTenNV(),
                    nv.getSoDT(),
                    nv.getSoCCCD(),
                    nv.getChucVu()
                });
            }
        }
    }

    private void addNhanVien() {
        String ten = txtTenNV.getText().trim();
        String sdt = txtSoDT.getText().trim();
        String cccd = txtSoCCCD.getText().trim();
        String chucVu = cbChucVu.getSelectedItem().toString();

        if (ten.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tên và SĐT!");
            return;
        }

        // Tự sinh mã dựa trên database
        String maNV = "NV" + String.format("%03d", nv_dao.getAllNhanVien().size() + 1);
        
        NhanVien nv = new NhanVien(maNV, ten, sdt, cccd, ChucVu.fromString(chucVu), true, null);
        if (nv_dao.addNhanVien(nv)) {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công vào SQL!");
            loadDataToTable();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!");
        }
    }

    private void updateNhanVien() {
        String ma = txtMaNV.getText();
        if (ma.isEmpty()) return;
        
        NhanVien nv = new NhanVien(ma, txtTenNV.getText(), txtSoDT.getText(), txtSoCCCD.getText(), ChucVu.fromString(cbChucVu.getSelectedItem().toString()), true, null);
        if (nv_dao.updateNhanVien(nv)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDataToTable();
        }
    }

    private void deleteNhanVien() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String ma = tableModel.getValueAt(row, 0).toString();
        if (nv_dao.deleteNhanVien(ma)) {
            JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!");
            loadDataToTable();
            clearFields();
        }
    }

    private void clearFields() {
        txtMaNV.setText("");
        txtTenNV.setText("");
        txtSoDT.setText("");
        txtSoCCCD.setText("");
        cbChucVu.setSelectedIndex(0);
        txtSearch.setText("");
        table.clearSelection();
        loadDataToTable();
    }

    private void searchNhanVien() {
        String s = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        List<NhanVien> dsNV = nv_dao.getAllNhanVien();
        if (dsNV != null) {
            for (NhanVien nv : dsNV) {
                if (nv.getMaNV().toLowerCase().contains(s) || nv.getTenNV().toLowerCase().contains(s)) {
                    tableModel.addRow(new Object[]{
                        nv.getMaNV(), nv.getTenNV(), nv.getSoDT(), nv.getSoCCCD(), nv.getChucVu()
                    });
                }
            }
        }
    }
}
