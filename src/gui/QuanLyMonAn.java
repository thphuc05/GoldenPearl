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
    private JTextField txtMaMon, txtTenMon, txtDonGia, txtHinhAnh, txtSearch;
    private JComboBox<String> cbDanhMuc, cbTrangThai;
    private JButton btnAdd, btnUpdate, btnRemove, btnClearInputs, btnClear, btnSearch, btnChooseImage;
    private JLabel lblImageDisplay;
    private JTable table;
    private DefaultTableModel tableModel;
    private SanPham_DAO sp_dao;
    private LoaiSanPham_DAO lsp_dao;
    private List<LoaiSanPham> dsLoai;
    private String selectedImagePath = "";
    private boolean isFiltering = false;

    // Luxury Theme Colors
    private final Color MAIN_BLUE = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_WHITE = Color.WHITE;

    public QuanLyMonAn() {
        sp_dao = new SanPham_DAO();
        lsp_dao = new LoaiSanPham_DAO();
        
        setLayout(new BorderLayout());
        setBackground(MAIN_BLUE);

        // Header Section
        JLabel lblTitle = new JLabel("QUẢN LÝ MÓN ĂN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 32));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        lblTitle.setForeground(GOLD_COLOR);
        add(lblTitle, BorderLayout.NORTH);

        // Main Container
        JPanel pMain = new JPanel(new BorderLayout());
        pMain.setOpaque(false);
        pMain.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Upper Section: Information & Image
        JPanel pUpper = new JPanel(new BorderLayout(25, 0));
        pUpper.setOpaque(false);

        // Left: Image Preview
        JPanel pImageContainer = new JPanel(new BorderLayout(0, 10));
        pImageContainer.setOpaque(false);
        pImageContainer.setPreferredSize(new Dimension(280, 280));
        
        lblImageDisplay = new JLabel("CLICK ĐỂ TẢI ẢNH", SwingConstants.CENTER);
        lblImageDisplay.setPreferredSize(new Dimension(250, 250));
        lblImageDisplay.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 3));
        lblImageDisplay.setForeground(GOLD_COLOR);
        lblImageDisplay.setFont(new Font("Inter Medium", Font.BOLD, 14));
        lblImageDisplay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
        gbc.weightx = 0;
        pForm.add(createLabel("Mã món:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        txtMaMon = new JTextField(30);
        txtMaMon.setEditable(true);
        txtMaMon.setFont(new Font("Inter", Font.BOLD, 14));
        pForm.add(txtMaMon, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        pForm.add(createLabel("Tên món:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        txtTenMon = new JTextField(30);
        pForm.add(txtTenMon, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        pForm.add(createLabel("Đơn giá (VNĐ):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        txtDonGia = new JTextField(30);
        pForm.add(txtDonGia, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        pForm.add(createLabel("Danh mục:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        cbDanhMuc = new JComboBox<>();
        cbDanhMuc.setBackground(Color.WHITE);
        cbDanhMuc.setForeground(MAIN_BLUE);
        cbDanhMuc.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? GOLD_COLOR : Color.WHITE);
                setForeground(MAIN_BLUE);
                return this;
            }
        });
        loadCategories();
        pForm.add(cbDanhMuc, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        pForm.add(createLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        cbTrangThai = new JComboBox<>(new String[]{"Còn món", "Hết món"});
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

        gbc.gridx = 2;
        gbc.weightx = 0;
        pForm.add(createLabel("Đường dẫn ảnh:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        txtHinhAnh = new JTextField(30);
        txtHinhAnh.setEditable(false);
        pForm.add(txtHinhAnh, gbc);

        pUpper.add(pForm, BorderLayout.CENTER);
        pMain.add(pUpper, BorderLayout.NORTH);

        // Table Section
        String[] columnNames = {"Mã món", "Tên món", "Danh mục", "Đơn giá", "Trạng thái", "Hình ảnh"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Inter", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 16));
        table.getTableHeader().setBackground(GOLD_COLOR);
        table.getTableHeader().setForeground(MAIN_BLUE);
        
        table.setBackground(Color.decode("#EBF5FB"));
        table.setForeground(MAIN_BLUE);
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
        JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pButtons.setOpaque(false);
        btnAdd = createStyledButton("THÊM MÓN");
        btnUpdate = createStyledButton("CẬP NHẬT");
        btnRemove = createStyledButton("XÓA MÓN");
        btnClearInputs = createStyledButton("XÓA TRẮNG");
        btnClear = createStyledButton("LÀM MỚI");
        pButtons.add(btnAdd);
        pButtons.add(btnUpdate);
        pButtons.add(btnRemove);
        pButtons.add(btnClearInputs);
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

        // Events Section
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String ma = getValueOrEmpty(row, 0);
                    txtMaMon.setText(ma);
                    txtTenMon.setText(getValueOrEmpty(row, 1));
                    
                    isFiltering = true;
                    cbDanhMuc.setSelectedItem(getValueOrEmpty(row, 2));
                    isFiltering = false;

                    String giaStr = getValueOrEmpty(row, 3).replace("đ", "").replace(",", "");
                    txtDonGia.setText(giaStr);
                    cbTrangThai.setSelectedItem(getValueOrEmpty(row, 4));
                    
                    // Lấy đường dẫn đầy đủ từ danh sách dữ liệu thực tế
                    List<SanPham> ds = sp_dao.getAllSanPham();
                    for(SanPham sp : ds) {
                        if(sp.getMaMon().equals(ma)) {
                            selectedImagePath = (sp.getHinhAnh() == null) ? "" : sp.getHinhAnh();
                            txtHinhAnh.setText(selectedImagePath);
                            updateImageDisplay(selectedImagePath);
                            break;
                        }
                    }
                }
            }
            
            private String getValueOrEmpty(int row, int col) {
                Object val = tableModel.getValueAt(row, col);
                return (val == null) ? "" : val.toString();
            }
        });

        lblImageDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseImage();
            }
        });

        // Button Events
        btnChooseImage.addActionListener(e -> chooseImage());
        btnAdd.addActionListener(e -> addMonAn());
        btnUpdate.addActionListener(e -> updateMonAn());
        btnRemove.addActionListener(e -> deleteMonAn());
        btnClearInputs.addActionListener(e -> clearInputs());
        btnClear.addActionListener(e -> {
            isFiltering = true;
            clearInputs();
            txtSearch.setText("");
            cbDanhMuc.setSelectedIndex(-1);
            isFiltering = false;
            loadDataToTable();
        });
        btnSearch.addActionListener(e -> searchMonAn());
        
        cbDanhMuc.addActionListener(e -> {
            if (!isFiltering && cbDanhMuc.getSelectedIndex() != -1 && txtMaMon.getText().isEmpty()) {
                filterByCategory();
            }
        });

        loadDataToTable();
    }

    public void refreshData() {
        loadDataToTable();
        loadCategories();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_WHITE);
        lbl.setFont(new Font("Inter Medium", Font.BOLD, 15));
        return lbl;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 14));
        btn.setBackground(GOLD_COLOR);
        btn.setForeground(MAIN_BLUE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(170, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setBackground(GOLD_COLOR); }
        });
        
        return btn;
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser("data/image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            txtHinhAnh.setText(selectedImagePath);
            updateImageDisplay(selectedImagePath);
        }
    }

    private void updateImageDisplay(String path) {
        if (path == null || path.isEmpty()) {
            lblImageDisplay.setIcon(null);
            lblImageDisplay.setText("CLICK ĐỂ TẢI ẢNH");
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
        isFiltering = true;
        dsLoai = lsp_dao.getAllLoaiSanPham();
        cbDanhMuc.removeAllItems();
        if (dsLoai != null) {
            for (LoaiSanPham lsp : dsLoai) {
                cbDanhMuc.addItem(lsp.getTenLoai());
            }
        }
        cbDanhMuc.setSelectedIndex(-1);
        isFiltering = false;
    }

    private String getFileName(String path) {
        if (path == null || path.isEmpty()) return "Không có ảnh";
        return new File(path).getName();
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
                    getFileName(sp.getHinhAnh())
                });
            }
        }
    }

    private void filterByCategory() {
        String cat = cbDanhMuc.getSelectedItem().toString();
        tableModel.setRowCount(0);
        List<SanPham> dsSP = sp_dao.getAllSanPham();
        if (dsSP != null) {
            for (SanPham sp : dsSP) {
                if (sp.getLoaiSanPham().getTenLoai().equals(cat)) {
                    tableModel.addRow(new Object[]{
                        sp.getMaMon(), sp.getTenMon(), sp.getLoaiSanPham().getTenLoai(),
                        String.format("%,.0fđ", sp.getDonGia()),
                        sp.isTrangThai() ? "Còn món" : "Hết món", 
                        getFileName(sp.getHinhAnh())
                    });
                }
            }
        }
    }

    private void addMonAn() {
        try {
            String ma = txtMaMon.getText().trim();
            String ten = txtTenMon.getText().trim();
            
            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mã và Tên món không được để trống!");
                return;
            }

            // Duplicate Check
            List<SanPham> ds = sp_dao.getAllSanPham();
            for(SanPham item : ds) {
                if(item.getMaMon().equalsIgnoreCase(ma)) {
                    JOptionPane.showMessageDialog(this, "Mã món " + ma + " đã tồn tại!");
                    return;
                }
                if(item.getTenMon().equalsIgnoreCase(ten)) {
                    JOptionPane.showMessageDialog(this, "Tên món '" + ten + "' đã tồn tại!");
                    return;
                }
            }

            double gia = Double.parseDouble(txtDonGia.getText().trim().replace(",", ""));
            boolean trangThai = cbTrangThai.getSelectedItem().toString().equals("Còn món");
            
            String tenDanhMuc = cbDanhMuc.getSelectedItem().toString();
            LoaiSanPham loaiSelected = null;
            for(LoaiSanPham l : dsLoai) {
                if(l.getTenLoai().equals(tenDanhMuc)) {
                    loaiSelected = l;
                    break;
                }
            }

            SanPham sp = new SanPham(ma, ten, gia, "", trangThai, loaiSelected, selectedImagePath);
            if (sp_dao.addSanPham(sp)) {
                JOptionPane.showMessageDialog(this, "Thêm món ăn mới thành công!");
                loadDataToTable();
                clearInputs();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu nhập không hợp lệ! Vui lòng kiểm tra lại đơn giá.");
        }
    }

    private void updateMonAn() {
        try {
            String ma = txtMaMon.getText().trim();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã món để cập nhật!");
                return;
            }
            
            String ten = txtTenMon.getText().trim();
            double gia = Double.parseDouble(txtDonGia.getText().trim().replace(",", ""));
            boolean trangThai = cbTrangThai.getSelectedItem().toString().equals("Còn món");
            
            String tenDanhMuc = cbDanhMuc.getSelectedItem().toString();
            LoaiSanPham loaiSelected = null;
            for(LoaiSanPham l : dsLoai) {
                if(l.getTenLoai().equals(tenDanhMuc)) {
                    loaiSelected = l;
                    break;
                }
            }

            SanPham sp = new SanPham(ma, ten, gia, "", trangThai, loaiSelected, selectedImagePath);
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn cần xóa dưới bảng!");
            return;
        }
        
        String ma = tableModel.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Xóa món " + ma + " khỏi SQL?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (sp_dao.deleteSanPham(ma)) {
                loadDataToTable();
                clearInputs();
            }
        }
    }

    private void clearInputs() {
        txtMaMon.setText("");
        txtTenMon.setText("");
        txtDonGia.setText("");
        txtHinhAnh.setText("");
        cbTrangThai.setSelectedIndex(0);
        isFiltering = true;
        cbDanhMuc.setSelectedIndex(-1);
        isFiltering = false;
        selectedImagePath = "";
        updateImageDisplay("");
        table.clearSelection();
    }

    private void clearFields() {
        clearInputs();
        txtSearch.setText("");
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
                        sp.isTrangThai() ? "Còn món" : "Hết món", 
                        getFileName(sp.getHinhAnh())
                    });
                }
            }
        }
    }
}
