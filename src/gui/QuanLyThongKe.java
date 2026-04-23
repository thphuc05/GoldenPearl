package gui;

import dao.HoaDon_DAO;
import dao.ChiTietHoaDon_DAO;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class QuanLyThongKe extends JPanel {
    private JTextField txtFromDate, txtToDate;
    private JButton btnSearch, btnRefresh, btnExport;
    private JLabel lblTotalRevenue, lblTotalInvoices, lblAvgOrderValue, lblBestSeller;
    private JTable table;
    private DefaultTableModel tableModel;
    private HoaDon_DAO hd_dao;
    private ChiTietHoaDon_DAO ct_dao;

    private final Color MAIN_BLUE = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_WHITE = Color.WHITE;
    private final Color CARD_BG = Color.decode("#1A4D6D");
    private final Color LIGHT_BLUE = Color.decode("#2E6A8E");
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private final DecimalFormat df = new DecimalFormat("#,### VNĐ");

    public QuanLyThongKe() {
        hd_dao = new HoaDon_DAO();
        ct_dao = new ChiTietHoaDon_DAO();

        setLayout(new BorderLayout());
        setBackground(MAIN_BLUE);

        // Header Section
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);
        pHeader.setBorder(new EmptyBorder(20, 30, 10, 30));
        
        JLabel lblTitle = new JLabel("BÁO CÁO DOANH THU NHÀ HÀNG");
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 28));
        lblTitle.setForeground(GOLD_COLOR);
        pHeader.add(lblTitle, BorderLayout.WEST);
        
        add(pHeader, BorderLayout.NORTH);

        // Main Content Section
        JPanel pMain = new JPanel(new BorderLayout(0, 20));
        pMain.setOpaque(false);
        pMain.setBorder(new EmptyBorder(10, 30, 30, 30));

        // 1. Filter Panel (Top)
        JPanel pFilter = createFilterPanel();
        pMain.add(pFilter, BorderLayout.NORTH);

        // 2. Statistics & Table (Center)
        JPanel pCenter = new JPanel(new BorderLayout(0, 25));
        pCenter.setOpaque(false);

        // Cards Panel
        JPanel pCards = createCardsPanel();
        pCenter.add(pCards, BorderLayout.NORTH);

        // Table Panel
        JPanel pTable = createTablePanel();
        pCenter.add(pTable, BorderLayout.CENTER);

        pMain.add(pCenter, BorderLayout.CENTER);

        add(pMain, BorderLayout.CENTER);

        // Initialize Data
        initDates();
        loadStatistics();

        // Events
        btnSearch.addActionListener(e -> loadStatistics());
        btnRefresh.addActionListener(e -> {
            initDates();
            loadStatistics();
        });
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng xuất báo cáo đang được phát triển!"));
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15));
        panel.setBackground(CARD_BG);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_COLOR, 1), "BỘ LỌC THỜI GIAN");
        border.setTitleColor(GOLD_COLOR);
        border.setTitleFont(new Font("Inter Bold", Font.BOLD, 14));
        panel.setBorder(border);

        txtFromDate = new JTextField(12);
        txtToDate = new JTextField(12);
        
        panel.add(createLabel("Từ ngày:"));
        panel.add(txtFromDate);
        panel.add(createLabel("Đến ngày:"));
        panel.add(txtToDate);
        
        btnSearch = createStyledButton("THỰC HIỆN");
        btnRefresh = createStyledButton("TẢI LẠI");
        btnExport = createStyledButton("XUẤT EXCEL");
        
        panel.add(btnSearch);
        panel.add(btnRefresh);
        panel.add(btnExport);

        return panel;
    }

    private JPanel createCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 25, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 130));

        lblTotalRevenue = new JLabel("0 VNĐ");
        lblTotalInvoices = new JLabel("0");
        lblAvgOrderValue = new JLabel("0 VNĐ");
        lblBestSeller = new JLabel("N/A");

        panel.add(createStatCard("TỔNG DOANH THU", lblTotalRevenue, "attach_money_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png", new Color(46, 204, 113)));
        panel.add(createStatCard("TỔNG HÓA ĐƠN", lblTotalInvoices, "receipt_300dp_FFFFFF.png", new Color(52, 152, 219)));
        panel.add(createStatCard("TRUNG BÌNH/HĐ", lblAvgOrderValue, "star_half_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png", new Color(241, 196, 15)));
        panel.add(createStatCard("MÓN BÁN CHẠY", lblBestSeller, "dinner_dining_300dp_FFFFFF.png", new Color(231, 76, 60)));

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String iconName, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Accent line at the bottom
                g2d.setColor(accentColor);
                g2d.fillRect(0, getHeight() - 5, getWidth(), 5);
            }
        };
        card.setLayout(new BorderLayout(10, 5));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(255, 255, 255, 180));
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 13));
        card.add(lblTitle, BorderLayout.NORTH);

        valueLabel.setForeground(TEXT_WHITE);
        valueLabel.setFont(new Font("Inter Bold", Font.BOLD, 20));
        card.add(valueLabel, BorderLayout.CENTER);

        try {
            String path = "data/icons/" + iconName;
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                JLabel lblIcon = new JLabel(new ImageIcon(img));
                card.add(lblIcon, BorderLayout.EAST);
            }
        } catch (Exception e) {}

        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = {"STT", "Mã Hóa Đơn", "Thời Gian Lập", "Nhân Viên Lập", "Khách Hàng", "Trạng Thái", "Tổng Tiền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 15));
        table.getTableHeader().setBackground(GOLD_COLOR);
        table.getTableHeader().setForeground(MAIN_BLUE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        
        table.setBackground(MAIN_BLUE);
        table.setForeground(TEXT_WHITE);
        table.setSelectionBackground(LIGHT_BLUE);
        table.setSelectionForeground(TEXT_WHITE);
        table.setGridColor(new Color(255, 255, 255, 30));
        table.setShowVerticalLines(false);

        // Center Alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setOpaque(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
        // Right Alignment for Currency
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setForeground(GOLD_COLOR);
                setFont(new Font("Inter Bold", Font.BOLD, 14));
                return c;
            }
        };
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setOpaque(false);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(MAIN_BLUE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 20)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void initDates() {
        Calendar cal = Calendar.getInstance();
        txtToDate.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.MONTH, -1);
        txtFromDate.setText(sdf.format(cal.getTime()));
    }

    private void loadStatistics() {
        try {
            Date fromDate = sdf.parse(txtFromDate.getText());
            Date toDate = sdf.parse(txtToDate.getText());

            // Normalize dates
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            fromDate = cal.getTime();

            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            toDate = cal.getTime();

            List<HoaDon> dsHD = hd_dao.getHoaDonByDateRange(fromDate, toDate);
            tableModel.setRowCount(0);

            double totalRevenue = 0;
            int totalInvoices = 0;

            if (dsHD.isEmpty()) {
                lblTotalRevenue.setText("0 VNĐ");
                lblTotalInvoices.setText("0");
                lblAvgOrderValue.setText("0 VNĐ");
                lblBestSeller.setText("N/A");
                return;
            }

            for (int i = 0; i < dsHD.size(); i++) {
                HoaDon hd = dsHD.get(i);
                if (hd.isTrangThai()) {
                    totalRevenue += hd.getTongTien();
                    totalInvoices++;
                }

                Object[] row = {
                        i + 1,
                        hd.getMaHD(),
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(hd.getNgayLap()),
                        hd.getNhanVien().getTenNV(),
                        hd.getKhachHang().getTenKH(),
                        hd.isTrangThai() ? "Đã thanh toán" : "Chưa thanh toán",
                        df.format(hd.getTongTien())
                };
                tableModel.addRow(row);
            }

            lblTotalRevenue.setText(df.format(totalRevenue));
            lblTotalInvoices.setText(String.valueOf(totalInvoices));
            double avg = totalInvoices > 0 ? totalRevenue / totalInvoices : 0;
            lblAvgOrderValue.setText(df.format(avg));

            // Get Top Seller
            Map<String, Integer> topDishes = ct_dao.getTop5SellingDishes();
            if (!topDishes.isEmpty()) {
                String best = topDishes.entrySet().iterator().next().getKey();
                lblBestSeller.setText(best.toUpperCase());
            } else {
                lblBestSeller.setText("N/A");
            }

        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ngày theo định dạng dd/MM/yyyy");
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_WHITE);
        label.setFont(new Font("Inter Medium", Font.BOLD, 13));
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 12));
        btn.setBackground(GOLD_COLOR);
        btn.setForeground(MAIN_BLUE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(GOLD_COLOR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(GOLD_COLOR);
            }
        });

        return btn;
    }
}
