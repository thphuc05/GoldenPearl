package gui;

import dao.LoaiSanPham_DAO;
import dao.SanPham_DAO;
import entity.LoaiSanPham;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class QuanLyMonAn extends JPanel {
    private JTextField txtMaMon, txtTenMon, txtDonGia, txtMoTa, txtSearch;
    private JComboBox<String> cbDanhMuc, cbTrangThai;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnChooseImage;
    private JLabel lblImageDisplay;
    private JTable table;
    private DefaultTableModel tableModel;
    private SanPham_DAO sp_dao;
    private LoaiSanPham_DAO lsp_dao;
    private List<LoaiSanPham> dsLoai;
    private String selectedImagePath = "";

    // Luxury Theme Colors
    private final Color MAIN_BLUE = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_WHITE = Color.WHITE;

    public QuanLyMonAn() {
        sp_dao = new SanPham_DAO();
        lsp_dao = new LoaiSanPham_DAO();
        
        setLayout(new BorderLayout());
        setBackground(MAIN_BLUE);

        // Header Title
        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC THỰC ĐƠN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 32));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        lblTitle.setForeground(GOLD_COLOR);
        add(lblTitle, BorderLayout.NORTH);

        // Main content area
        JPanel pMain = new JPanel(new BorderLayout(0, 20));
        pMain.setOpaque(false);
        pMain.setBorder(new EmptyBorder(10, 25, 25, 25));

        // Upper Section: Information & Image
        JPanel pUpper = new JPanel(new BorderLayout(25, 0));
        pUpper.setOpaque(false);

        // Left: Image Preview
        JPanel pImageContainer = new JPanel(new BorderLayout(0, 10));
        pImageContainer.setOpaque(false);
        pImageContainer.setPreferredSize(new Dimension(280, 280));
        
        lblImageDisplay = new JLabel("HÌNH ẢNH MÓN ĂN", SwingConstants.CENTER);
        lblImageDisplay.setPreferredSize(new Dimension(250, 250));
        lblImageDisplay.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 3));
        lblImageDisplay.setForeground(GOLD_COLOR);
        lblImageDisplay.setFont(new Font("Inter Medium", Font.BOLD, 14));
        
        btnChooseImage = createStyledButton("TẢI ẢNH LÊN");
        btnChooseImage.setPreferredSize(new Dimension(250, 45));
        
        pImageContainer.add(lblImageDisplay, BorderLayout.CENTER);
        pImageContainer.add(btnChooseImage, BorderLayout.SOUTH);
        pUpper.add(pImageContainer, BorderLayout.WEST);

        // Right: Information Form
        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setOpaque(false);
        TitledBorder formBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_COLOR), "CHI TIẾT MÓN ĂN");
        formBorder.setTitleColor(GOLD_COLOR);
        formBorder.setTitleFont(new Font("Inter Bold", Font.BOLD, 18));
        pForm.setBorder(formBorder);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        pForm.add(createLabel("Mã món:"), gbc);
        gbc.gridx = 1;
        txtMaMon = new JTextField(18);
        txtMaMon.setEditable(false);
        txtMaMon.setFont(new Font("Inter", Font.BOLD, 14));
        pForm.add(txtMaMon, gbc);

        gbc.gridx = 2;
        pForm.add(createLabel("Tên món:"), gbc);
        gbc.gridx = 3;
        txtTenMon = new JTextField(18);
        pForm.add(txtTenMon, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        pForm.add(createLabel("Đơn giá (VNĐ):"), gbc);
        gbc.gridx = 1;
        txtDonGia = new JTextField(18);
        pForm.add(txtDonGia, gbc);

        gbc.gridx = 2;
        pForm.add(createLabel("Danh mục:"), gbc);
        gbc.gridx = 3;
        cbDanhMuc = new JComboBox<>();
        loadCategories();
        pForm.add(cbDanhMuc, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        pForm.add(createLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        cbTrangThai = new JComboBox<>(new String[]{"Còn món", "Hết món"});
        pForm.add(cbTrangThai, gbc);

        gbc.gridx = 2;
        pForm.add(createLabel("Mô tả:"), gbc);
        gbc.gridx = 3;
        txtMoTa = new JTextField(18);
        pForm.add(txtMoTa, gbc);

        pUpper.add(pForm, BorderLayout.CENTER);
        pMain.add(pUpper, BorderLayout.NORTH);

        // Table Section
        String[] columnNames = {"Mã món", "Tên món", "Danh mục", "Đơn giá", "Trạng thái", "Hình ảnh"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Inter", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 16));
        table.getTableHeader().setBackground(GOLD_COLOR);
        table.getTableHeader().setForeground(MAIN_BLUE);
        
        table.setBackground(new Color(255, 255, 255, 245));
        table.setSelectionBackground(GOLD_COLOR);
        table.setSelectionForeground(MAIN_BLUE);

        JScrollPane scrollPane = new JScrollPane(table);
        TitledBorder tableBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_COLOR), "DANH SÁCH THỰC ĐƠN NHÀ HÀNG");
        tableBorder.setTitleColor(GOLD_COLOR);
        tableBorder.setTitleFont(new Font("Inter Bold", Font.BOLD, 18));
        scrollPane.setBorder(tableBorder);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        pMain.add(scrollPane, BorderLayout.CENTER);

        // Control Section: Buttons & Search
        JPanel pControl = new JPanel(new BorderLayout());
        pControl.setOpaque(false);
        pControl.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Buttons Panel
        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        pButtons.setOpaque(false);
        btnAdd = createStyledButton("THÊM MÓN");
        btnUpdate = createStyledButton("CẬP NHẬT");
        btnDelete = createStyledButton("XÓA MÓN");
        btnClear = createStyledButton("LÀM MỚI");
        pButtons.add(btnAdd);
        pButtons.add(btnUpdate);
        pButtons.add(btnDelete);
        pButtons.add(btnClear);
        pControl.add(pButtons, BorderLayout.NORTH);

        // Search Panel
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pSearch.setOpaque(false);
        pSearch.add(createLabel("Tìm kiếm món ăn:"));
        txtSearch = new JTextField(35);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        btnSearch = createStyledButton("TÌM KIẾM");
        btnSearch.setPreferredSize(new Dimension(140, 40));
        pSearch.add(txtSearch);
        pSearch.add(btnSearch);
        pControl.add(pSearch, BorderLayout.SOUTH);

        pMain.add(pControl, BorderLayout.SOUTH);

        add(pMain, BorderLayout.CENTER);

        // Table Click Event
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    txtMaMon.setText(tableModel.getValueAt(row, 0).toString());
                    txtTenMon.setText(tableModel.getValueAt(row, 1).toString());
                    cbDanhMuc.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                    String giaStr = tableModel.getValueAt(row, 3).toString().replace("đ", "").replace(",", "");
                    txtDonGia.setText(giaStr);
                    cbTrangThai.setSelectedItem(tableModel.getValueAt(row, 4).toString());
                    
                    selectedImagePath = (tableModel.getValueAt(row, 5) != null) ? tableModel.getValueAt(row, 5).toString() : "";
                    updateImageDisplay(selectedImagePath);
                    
                    // Load description from full data list
                    List<SanPham> ds = sp_dao.getAllSanPham();
                    for(SanPham sp : ds) {
                        if(sp.getMaMon().equals(txtMaMon.getText())) {
                            txtMoTa.setText(sp.getMoTa());
                            break;
                        }
                    }
                }
            }
        });

        // Button Events
        btnChooseImage.addActionListener(e -> chooseImage());
        btnAdd.addActionListener(e -> addMonAn());
        btnUpdate.addActionListener(e -> updateMonAn());
        btnDelete.addActionListener(e -> deleteMonAn());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchMonAn());

        // Initialize table with SQL data
        loadDataToTable();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_WHITE);
        lbl.setFont(new Font("Inter Medium", Font.BOLD, 15));
        return lbl;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 15));
        btn.setBackground(GOLD_COLOR);
        btn.setForeground(MAIN_BLUE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setBackground(GOLD_COLOR); }
        });
        
        return btn;
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            updateImageDisplay(selectedImagePath);
        }
    }

    private void updateImageDisplay(String path) {
        if (path == null || path.isEmpty()) {
            lblImageDisplay.setIcon(null);
            lblImageDisplay.setText("KHÔNG CÓ ẢNH");
            return;
        }
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            lblImageDisplay.setIcon(new ImageIcon(img));
            lblImageDisplay.setText("");
        } catch (Exception e) {
            lblImageDisplay.setText("LỖI ĐỊNH DẠNG");
        }
    }

    private void loadCategories() {
        dsLoai = lsp_dao.getAllLoaiSanPham();
        cbDanhMuc.removeAllItems();
        if (dsLoai != null) {
            for (LoaiSanPham lsp : dsLoai) {
                cbDanhMuc.addItem(lsp.getTenLoai());
            }
        }
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<SanPham> dsSP = sp_dao.getAllSanPham();
        if (dsSP != null) {
            for (SanPham sp : dsSP) {
                tableModel.addRow(new Object[]{
                    sp.getMaMon(),
                    sp.getTenMon(),
                    sp.getLoaiSanPham().getTenLoai(),
                    String.format("%,.0fđ", sp.getDonGia()),
                    sp.isTrangThai() ? "Còn món" : "Hết món",
                    sp.getHinhAnh()
                });
            }
        }
    }

    private void addMonAn() {
        try {
            String ten = txtTenMon.getText().trim();
            double gia = Double.parseDouble(txtDonGia.getText().trim().replace(",", ""));
            String moTa = txtMoTa.getText().trim();
            boolean trangThai = cbTrangThai.getSelectedItem().toString().equals("Còn món");
            
            String tenDanhMuc = cbDanhMuc.getSelectedItem().toString();
            LoaiSanPham loaiSelected = null;
            for(LoaiSanPham l : dsLoai) {
                if(l.getTenLoai().equals(tenDanhMuc)) {
                    loaiSelected = l;
                    break;
                }
            }

            // Auto-generate ID: SPxxx
            String latest = sp_dao.getLatestMaMon();
            int num = Integer.parseInt(latest.substring(2)) + 1;
            String maMoi = "SP" + String.format("%03d", num);

            SanPham sp = new SanPham(maMoi, ten, gia, moTa, trangThai, loaiSelected, selectedImagePath);
            if (sp_dao.addSanPham(sp)) {
                JOptionPane.showMessageDialog(this, "Thêm món ăn mới vào SQL thành công!");
                loadDataToTable();
                clearFields();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu nhập không hợp lệ! Vui lòng kiểm tra lại đơn giá.");
        }
    }

    private void updateMonAn() {
        try {
            String ma = txtMaMon.getText();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn cần cập nhật!");
                return;
            }
            
            String ten = txtTenMon.getText().trim();
            double gia = Double.parseDouble(txtDonGia.getText().trim().replace(",", ""));
            String moTa = txtMoTa.getText().trim();
            boolean trangThai = cbTrangThai.getSelectedItem().toString().equals("Còn món");
            
            String tenDanhMuc = cbDanhMuc.getSelectedItem().toString();
            LoaiSanPham loaiSelected = null;
            for(LoaiSanPham l : dsLoai) {
                if(l.getTenLoai().equals(tenDanhMuc)) {
                    loaiSelected = l;
                    break;
                }
            }

            SanPham sp = new SanPham(ma, ten, gia, moTa, trangThai, loaiSelected, selectedImagePath);
            if (sp_dao.updateSanPham(sp)) {
                JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu SQL thành công!");
                loadDataToTable();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật! Vui lòng kiểm tra dữ liệu.");
        }
    }

    private void deleteMonAn() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn cần xóa!");
            return;
        }
        
        String ma = tableModel.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa món: " + ma + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (sp_dao.deleteSanPham(ma)) {
                JOptionPane.showMessageDialog(this, "Đã xóa món ăn khỏi SQL!");
                loadDataToTable();
                clearFields();
            }
        }
    }

    private void clearFields() {
        txtMaMon.setText("");
        txtTenMon.setText("");
        txtDonGia.setText("");
        txtMoTa.setText("");
        cbTrangThai.setSelectedIndex(0);
        cbDanhMuc.setSelectedIndex(0);
        txtSearch.setText("");
        selectedImagePath = "";
        updateImageDisplay("");
        table.clearSelection();
        loadDataToTable();
    }

    private void searchMonAn() {
        String s = txtSearch.getText().trim().toLowerCase();
        if (s.isEmpty()) {
            loadDataToTable();
            return;
        }
        
        tableModel.setRowCount(0);
        List<SanPham> dsSP = sp_dao.getAllSanPham();
        if (dsSP != null) {
            for (SanPham sp : dsSP) {
                if (sp.getMaMon().toLowerCase().contains(s) || sp.getTenMon().toLowerCase().contains(s)) {
                    tableModel.addRow(new Object[]{
                        sp.getMaMon(), sp.getTenMon(), sp.getLoaiSanPham().getTenLoai(),
                        String.format("%,.0fđ", sp.getDonGia()),
                        sp.isTrangThai() ? "Còn món" : "Hết món", sp.getHinhAnh()
                    });
                }
            }
        }
    }
}
