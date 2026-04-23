package gui;

import dao.HoaDon_DAO;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class QuanLyHoaDon extends JPanel {
    private JTextField txtSearch, txtFromDate, txtToDate;
    private JButton btnSearch, btnRefresh, btnViewDetail, btnPrint;
    private JTable table;
    private DefaultTableModel tableModel;
    private HoaDon_DAO hd_dao;

    private final Color MAIN_BLUE = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_WHITE = Color.WHITE;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public QuanLyHoaDon() {
        hd_dao = new HoaDon_DAO();
        setLayout(new BorderLayout());
        setBackground(MAIN_BLUE);

        // Header
        JLabel lblTitle = new JLabel("QUẢN LÝ HÓA ĐƠN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 32));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        lblTitle.setForeground(GOLD_COLOR);
        add(lblTitle, BorderLayout.NORTH);

        // Main content
        JPanel pMain = new JPanel(new BorderLayout());
        pMain.setOpaque(false);
        pMain.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Filter panel
        JPanel pFilter = new JPanel(new GridBagLayout());
        pFilter.setOpaque(false);
        javax.swing.border.TitledBorder filterBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(GOLD_COLOR), "Bộ lọc tìm kiếm");
        filterBorder.setTitleColor(GOLD_COLOR);
        pFilter.setBorder(filterBorder);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblMa = new JLabel("Mã hóa đơn:");
        lblMa.setForeground(TEXT_WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        pFilter.add(lblMa, gbc);
        
        gbc.gridx = 1;
        txtSearch = new JTextField(15);
        pFilter.add(txtSearch, gbc);

        JLabel lblFrom = new JLabel("Từ ngày:");
        lblFrom.setForeground(TEXT_WHITE);
        gbc.gridx = 2;
        pFilter.add(lblFrom, gbc);
        
        gbc.gridx = 3;
        txtFromDate = new JTextField(10);
        pFilter.add(txtFromDate, gbc);

        JLabel lblTo = new JLabel("Đến ngày:");
        lblTo.setForeground(TEXT_WHITE);
        gbc.gridx = 4;
        pFilter.add(lblTo, gbc);
        
        gbc.gridx = 5;
        txtToDate = new JTextField(10);
        pFilter.add(txtToDate, gbc);

        gbc.gridx = 6;
        btnSearch = createStyledButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(120, 35));
        pFilter.add(btnSearch, gbc);

        pMain.add(pFilter, BorderLayout.NORTH);

        // Table panel
        String[] columnNames = {"Mã HĐ", "Ngày Lập", "Nhân Viên", "Khách Hàng", "Tổng Tiền", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 15));
        table.getTableHeader().setBackground(GOLD_COLOR);
        table.getTableHeader().setForeground(MAIN_BLUE);
        
        // Custom table colors
        table.setBackground(new Color(255, 255, 255, 230)); // Slightly transparent white
        table.setSelectionBackground(GOLD_COLOR);
        table.setSelectionForeground(MAIN_BLUE);

        JScrollPane scrollPane = new JScrollPane(table);
        javax.swing.border.TitledBorder tableBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(GOLD_COLOR), "Danh sách hóa đơn");
        tableBorder.setTitleColor(GOLD_COLOR);
        scrollPane.setBorder(tableBorder);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        pMain.add(scrollPane, BorderLayout.CENTER);

        // Control panel
        JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        pControl.setOpaque(false);
        
        btnRefresh = createStyledButton("Làm mới");
        btnViewDetail = createStyledButton("Xem chi tiết");
        btnPrint = createStyledButton("In hóa đơn");
        
        pControl.add(btnRefresh);
        pControl.add(btnViewDetail);
        pControl.add(btnPrint);

        pMain.add(pControl, BorderLayout.SOUTH);

        add(pMain, BorderLayout.CENTER);

        // Add 2 hardcoded examples as requested
        addExampleData();

        // Load real data from DB
        loadDataFromDB();

        // Events
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            txtFromDate.setText("");
            txtToDate.setText("");
            loadDataFromDB();
        });

        btnSearch.addActionListener(e -> searchHoaDon());
        
        btnViewDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String maHD = tableModel.getValueAt(row, 0).toString();
                JOptionPane.showMessageDialog(this, "Xem chi tiết hóa đơn: " + maHD);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xem!");
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 14));
        btn.setBackground(GOLD_COLOR);
        btn.setForeground(MAIN_BLUE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void addExampleData() {
        tableModel.addRow(new Object[]{
            "HD001", "20/04/2026 14:30", "NV001", "KH001", "1,500,000đ", "Đã thanh toán"
        });
        tableModel.addRow(new Object[]{
            "HD002", "21/04/2026 18:45", "NV002", "KH002", "850,000đ", "Đã thanh toán"
        });
    }

    private void loadDataFromDB() {
        List<HoaDon> dsHD = hd_dao.getAllHoaDon();
        if (dsHD == null) return;
        
        for (HoaDon hd : dsHD) {
            boolean exists = false;
            for(int i=0; i<tableModel.getRowCount(); i++) {
                if(tableModel.getValueAt(i, 0).equals(hd.getMaHD())) {
                    exists = true;
                    break;
                }
            }
            if(!exists) {
                String maNV = (hd.getNhanVien() != null) ? hd.getNhanVien().getMaNV() : "";
                String tenKH = (hd.getKhachHang() != null) ? hd.getKhachHang().getMaKH() : "Khách vãng lai";
                
                tableModel.addRow(new Object[]{
                    hd.getMaHD(),
                    hd.getNgayLap() != null ? sdf.format(hd.getNgayLap()) : "",
                    maNV,
                    tenKH,
                    String.format("%,.0fđ", hd.getTongTien()),
                    hd.isTrangThai() ? "Đã thanh toán" : "Chưa thanh toán"
                });
            }
        }
    }

    private void searchHoaDon() {
        String searchVal = txtSearch.getText().trim();
        if (searchVal.isEmpty()) {
            loadDataFromDB();
            return;
        }

        // Simple filtering for now
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            if (!tableModel.getValueAt(i, 0).toString().contains(searchVal)) {
                tableModel.removeRow(i);
            }
        }
    }
}
