package gui;

import dao.HoaDon_DAO;
import dao.ChiTietHoaDon_DAO;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class QuanLyThongKe extends JPanel {
    private JTextField txtFromDate, txtToDate, txtSearch;
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
        
        JLabel lblTitle = new JLabel("BÁO CÁO DOANH THU NHÀ HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 32));
        lblTitle.setForeground(GOLD_COLOR);
        pHeader.add(lblTitle, BorderLayout.CENTER);
        
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

        // Events
        btnSearch.addActionListener(e -> loadStatistics());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            initDates();
            loadStatistics();
        });
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng xuất báo cáo đang được phát triển!"));
    }

    public void refreshData() {
        txtSearch.setText("");
        initDates();
        loadStatistics();
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_COLOR, 1), "BỘ LỌC TÌM KIẾM");
        border.setTitleColor(GOLD_COLOR);
        border.setTitleFont(new Font("Inter Bold", Font.BOLD, 14));
        panel.setBorder(border);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mã hóa đơn
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(createLabel("Mã hóa đơn:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.2;
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(150, 35));
        panel.add(txtSearch, gbc);

        // Từ ngày
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(createLabel("Từ ngày:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.2;
        txtFromDate = createReadOnlyDateField();
        JButton btnPickFrom = createCalendarButton(txtFromDate);
        JPanel pFrom = new JPanel(new BorderLayout());
        pFrom.add(txtFromDate, BorderLayout.CENTER);
        pFrom.add(btnPickFrom, BorderLayout.EAST);
        panel.add(pFrom, gbc);

        // Đến ngày
        gbc.gridx = 4; gbc.weightx = 0;
        panel.add(createLabel("Đến ngày:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.2;
        txtToDate = createReadOnlyDateField();
        JButton btnPickTo = createCalendarButton(txtToDate);
        JPanel pTo = new JPanel(new BorderLayout());
        pTo.add(txtToDate, BorderLayout.CENTER);
        pTo.add(btnPickTo, BorderLayout.EAST);
        panel.add(pTo, gbc);
        
        btnSearch = createStyledButton("THỰC HIỆN");
        btnRefresh = createStyledButton("TẢI LẠI");
        btnExport = createStyledButton("XUẤT EXCEL");
        
        gbc.gridx = 6; gbc.weightx = 0;
        panel.add(btnSearch, gbc);
        gbc.gridx = 7;
        panel.add(btnRefresh, gbc);
        gbc.gridx = 8;
        panel.add(btnExport, gbc);

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
        
        table.setBackground(Color.decode("#EBF5FB"));
        table.setForeground(MAIN_BLUE);
        table.setSelectionBackground(GOLD_COLOR);
        table.setSelectionForeground(MAIN_BLUE);
        table.setGridColor(new Color(255, 255, 255, 30));
        table.setShowVerticalLines(false);

        // Center Alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    setForeground(MAIN_BLUE);
                } else {
                    setForeground(Color.BLACK);
                }
                return c;
            }
        };
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
        // Right Alignment for Currency
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Inter Bold", Font.BOLD, 14));
                if (isSelected) {
                    setForeground(MAIN_BLUE);
                } else {
                    setForeground(GOLD_COLOR);
                }
                return c;
            }
        };
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        // Default renderer for other columns to fix selection color
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    setForeground(MAIN_BLUE);
                } else {
                    setForeground(Color.BLACK);
                }
                return c;
            }
        });

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
            List<HoaDon> dsHD = new ArrayList<>();
            String maSearch = txtSearch.getText().trim();

            Date fromDate = sdf.parse(txtFromDate.getText());
            Date toDate = sdf.parse(txtToDate.getText());

            // Normalize dates
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date start = cal.getTime();

            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date end = cal.getTime();

            if (!maSearch.isEmpty()) {
                // Search by MaHD with Date Range validation
                HoaDon hdFound = hd_dao.getHoaDonByMa(maSearch);
                if (hdFound != null) {
                    Date lap = hdFound.getNgayLap();
                    if (lap != null && !lap.before(start) && !lap.after(end)) {
                        dsHD.add(hdFound);
                    } else {
                        JOptionPane.showMessageDialog(this, "Hóa đơn mã '" + maSearch + "' không nằm trong khoảng ngày đã chọn!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn mã: " + maSearch);
                }
            } else {
                // Search by Date Range
                dsHD = hd_dao.getHoaDonByDateRange(start, end);
            }

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
                        hd.getNhanVien() != null ? hd.getNhanVien().getTenNV() : "N/A",
                        hd.getKhachHang() != null ? hd.getKhachHang().getTenKH() : "N/A",
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hợp lệ.");
        }
    }

    private JTextField createReadOnlyDateField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        f.setBackground(Color.WHITE);
        f.setForeground(MAIN_BLUE);
        f.setFont(new Font("Inter Bold", Font.BOLD, 14));
        f.setPreferredSize(new Dimension(100, 35));
        f.setHorizontalAlignment(SwingConstants.CENTER);
        f.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 1));
        return f;
    }

    private JButton createCalendarButton(JTextField target) {
        JButton btn = new JButton("📅");
        btn.setPreferredSize(new Dimension(45, 35));
        btn.setBackground(GOLD_COLOR);
        btn.setForeground(MAIN_BLUE);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            CustomDatePicker dialog = new CustomDatePicker((JFrame) SwingUtilities.getWindowAncestor(this), target);
            dialog.setVisible(true);
        });
        return btn;
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

    class CustomDatePicker extends JDialog {
        private JTextField targetField;
        private Calendar cal;
        private JPanel daysPanel;
        private JLabel monthLabel;
        private final Color CAL_BG = Color.decode("#EBF5FB"); 
        private final Color DAY_TEXT = Color.decode("#0B3D59");

        public CustomDatePicker(JFrame parent, JTextField target) {
            super(parent, "Chọn ngày", true);
            this.targetField = target;
            this.cal = Calendar.getInstance();
            
            try {
                if(!target.getText().isEmpty()) cal.setTime(sdf.parse(target.getText()));
            } catch(Exception e) {}

            setSize(320, 380);
            setLocationRelativeTo(target);
            setLayout(new BorderLayout());
            getContentPane().setBackground(CAL_BG);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(MAIN_BLUE);
            header.setBorder(new EmptyBorder(5, 5, 5, 5));

            JButton btnPrev = createNavButton("<");
            JButton btnNext = createNavButton(">");
            
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setForeground(GOLD_COLOR);
            monthLabel.setFont(new Font("Inter Bold", Font.BOLD, 18));
            
            updateHeader();

            btnPrev.addActionListener(e -> { cal.add(Calendar.MONTH, -1); updateCalendar(); });
            btnNext.addActionListener(e -> { cal.add(Calendar.MONTH, 1); updateCalendar(); });

            header.add(btnPrev, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(btnNext, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
            daysPanel.setBackground(CAL_BG);
            daysPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            updateCalendar();
            add(daysPanel, BorderLayout.CENTER);
        }

        private JButton createNavButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Inter Bold", Font.BOLD, 16));
            btn.setForeground(GOLD_COLOR);
            btn.setContentAreaFilled(false);
            btn.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 1));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(45, 30));
            return btn;
        }

        private void updateHeader() {
            SimpleDateFormat monthYearSdf = new SimpleDateFormat("MMMM yyyy");
            monthLabel.setText(monthYearSdf.format(cal.getTime()).toUpperCase());
        }

        private void updateCalendar() {
            daysPanel.removeAll();
            updateHeader();

            String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
            for(String name : dayNames) {
                JLabel l = new JLabel(name, SwingConstants.CENTER);
                l.setFont(new Font("Inter Bold", Font.BOLD, 13));
                l.setForeground(DAY_TEXT);
                daysPanel.add(l);
            }

            Calendar temp = (Calendar) cal.clone();
            temp.set(Calendar.DAY_OF_MONTH, 1);
            int startDay = temp.get(Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

            for(int i=0; i<startDay; i++) daysPanel.add(new JLabel(""));

            Calendar today = Calendar.getInstance();

            for(int i=1; i<=daysInMonth; i++) {
                final int day = i;
                JButton btnDay = new JButton(String.valueOf(i));
                btnDay.setFont(new Font("Inter", Font.BOLD, 14));
                btnDay.setFocusPainted(false);
                btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                btnDay.setBackground(Color.WHITE);
                btnDay.setForeground(DAY_TEXT);
                btnDay.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,20)));

                temp.set(Calendar.DAY_OF_MONTH, day);
                if (sdf.format(temp.getTime()).equals(targetField.getText())) {
                    btnDay.setBackground(GOLD_COLOR);
                    btnDay.setForeground(MAIN_BLUE);
                } else if (sdf.format(temp.getTime()).equals(sdf.format(today.getTime()))) {
                    btnDay.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 2));
                }

                btnDay.addActionListener(e -> {
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    targetField.setText(sdf.format(cal.getTime()));
                    dispose();
                });

                btnDay.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        if (!btnDay.getBackground().equals(GOLD_COLOR)) {
                            btnDay.setBackground(new Color(255, 255, 255, 180));
                            btnDay.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 1));
                        }
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        if (!btnDay.getBackground().equals(GOLD_COLOR)) {
                            btnDay.setBackground(Color.WHITE);
                            btnDay.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,20)));
                        }
                    }
                });

                daysPanel.add(btnDay);
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }
}
