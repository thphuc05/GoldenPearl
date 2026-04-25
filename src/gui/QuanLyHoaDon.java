package gui;

import dao.HoaDon_DAO;
import dao.ChiTietHoaDon_DAO;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;
import entity.ChiTietHoaDon;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class QuanLyHoaDon extends JPanel {
    private JTextField txtSearch, txtFromDate, txtToDate;
    private JButton btnSearch, btnRefresh, btnViewDetail, btnPrint;
    private JTable table;
    private DefaultTableModel tableModel;
    private HoaDon_DAO hd_dao;
    private ChiTietHoaDon_DAO ct_dao;

    private final Color MAIN_BLUE = Color.decode("#0B3D59");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color TEXT_WHITE = Color.WHITE;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy");

    public QuanLyHoaDon() {
        hd_dao = new HoaDon_DAO();
        ct_dao = new ChiTietHoaDon_DAO();
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
        TitledBorder filterBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_COLOR), "BỘ LỌC TÌM KIẾM");
        filterBorder.setTitleColor(GOLD_COLOR);
        filterBorder.setTitleFont(new Font("Inter Bold", Font.BOLD, 14));
        pFilter.setBorder(filterBorder);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mã hóa đơn
        gbc.gridx = 0; gbc.gridy = 0;
        pFilter.add(createLabel("Mã hóa đơn:"), gbc);
        gbc.gridx = 1;
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(180, 35));
        pFilter.add(txtSearch, gbc);

        // Từ ngày
        gbc.gridx = 2;
        pFilter.add(createLabel("Từ ngày:"), gbc);
        gbc.gridx = 3;
        txtFromDate = createReadOnlyDateField();
        JButton btnPickFrom = createCalendarButton(txtFromDate);
        JPanel pFrom = new JPanel(new BorderLayout());
        pFrom.add(txtFromDate, BorderLayout.CENTER);
        pFrom.add(btnPickFrom, BorderLayout.EAST);
        pFilter.add(pFrom, gbc);

        // Đến ngày
        gbc.gridx = 4;
        pFilter.add(createLabel("Đến ngày:"), gbc);
        gbc.gridx = 5;
        txtToDate = createReadOnlyDateField();
        JButton btnPickTo = createCalendarButton(txtToDate);
        JPanel pTo = new JPanel(new BorderLayout());
        pTo.add(txtToDate, BorderLayout.CENTER);
        pTo.add(btnPickTo, BorderLayout.EAST);
        pFilter.add(pTo, gbc);

        gbc.gridx = 6;
        btnSearch = createStyledButton("TÌM KIẾM");
        pFilter.add(btnSearch, gbc);

        pMain.add(pFilter, BorderLayout.NORTH);

        // Table panel
        String[] columnNames = {"Mã HĐ", "Ngày Lập", "Nhân Viên", "Khách Hàng", "Tổng Tiền", "Trạng Thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Inter Bold", Font.BOLD, 15));
        table.getTableHeader().setBackground(GOLD_COLOR);
        table.getTableHeader().setForeground(MAIN_BLUE);
        table.setBackground(Color.WHITE);
        table.setForeground(MAIN_BLUE);
        table.setSelectionBackground(GOLD_COLOR);
        table.setSelectionForeground(MAIN_BLUE);

        JScrollPane scrollPane = new JScrollPane(table);
        TitledBorder tableBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(GOLD_COLOR), "DANH SÁCH HÓA ĐƠN");
        tableBorder.setTitleColor(GOLD_COLOR);
        tableBorder.setTitleFont(new Font("Inter Bold", Font.BOLD, 14));
        scrollPane.setBorder(tableBorder);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        pMain.add(scrollPane, BorderLayout.CENTER);

        // Control panel
        JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        pControl.setOpaque(false);

        btnRefresh = createStyledButton("QUAY LẠI");
        btnViewDetail = createStyledButton("XEM CHI TIẾT");
        btnPrint = createStyledButton("IN HÓA ĐƠN");

        pControl.add(btnRefresh);
        pControl.add(btnViewDetail);
        pControl.add(btnPrint);

        pMain.add(pControl, BorderLayout.SOUTH);
        add(pMain, BorderLayout.CENTER);

        // Initial default dates
        txtFromDate.setText(dateOnlySdf.format(new Date()));
        txtToDate.setText(dateOnlySdf.format(new Date()));

        // Events
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            txtFromDate.setText(dateOnlySdf.format(new Date()));
            txtToDate.setText(dateOnlySdf.format(new Date()));
            loadDataFromDB();
        });

        btnSearch.addActionListener(e -> searchHoaDon());

        btnViewDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String maHD = tableModel.getValueAt(row, 0).toString();
                HoaDon hd = hd_dao.getHoaDonByMa(maHD);
                if (hd != null) {
                    List<ChiTietHoaDon> dsCT = ct_dao.getChiTietByMaHD(maHD);
                    InvoiceDialog dialog = new InvoiceDialog((JFrame) SwingUtilities.getWindowAncestor(this), hd, dsCT, true);
                    dialog.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xem chi tiết!");
            }
        });

        btnPrint.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String maHD = tableModel.getValueAt(row, 0).toString();
                HoaDon hd = hd_dao.getHoaDonByMa(maHD);
                if (hd != null) {
                    List<ChiTietHoaDon> dsCT = ct_dao.getChiTietByMaHD(maHD);
                    InvoiceDialog dialog = new InvoiceDialog((JFrame) SwingUtilities.getWindowAncestor(this), hd, dsCT, false);
                    dialog.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần in!");
            }
        });
    }

    public void refreshData() {
        loadDataFromDB();
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
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_WHITE);
        l.setFont(new Font("Inter Medium", Font.PLAIN, 14));
        return l;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter Bold", Font.BOLD, 14));
        btn.setBackground(GOLD_COLOR);
        btn.setForeground(MAIN_BLUE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(170, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadDataFromDB() {
        tableModel.setRowCount(0);
        List<HoaDon> dsHD = hd_dao.getAllHoaDon();
        if (dsHD == null) return;
        for (HoaDon hd : dsHD) addHoaDonToTable(hd);
    }

    private void addHoaDonToTable(HoaDon hd) {
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

    private void searchHoaDon() {
        String searchVal = txtSearch.getText().trim();
        tableModel.setRowCount(0);

        try {
            Date fromDate = dateOnlySdf.parse(txtFromDate.getText());
            Date toDate = dateOnlySdf.parse(txtToDate.getText());

            Calendar cFrom = Calendar.getInstance();
            cFrom.setTime(fromDate);
            cFrom.set(Calendar.HOUR_OF_DAY, 0);
            cFrom.set(Calendar.MINUTE, 0);
            cFrom.set(Calendar.SECOND, 0);

            Calendar cTo = Calendar.getInstance();
            cTo.setTime(toDate);
            cTo.set(Calendar.HOUR_OF_DAY, 23);
            cTo.set(Calendar.MINUTE, 59);
            cTo.set(Calendar.SECOND, 59);

            Date start = cFrom.getTime();
            Date end = cTo.getTime();

            if (!searchVal.isEmpty()) {
                HoaDon hd = hd_dao.getHoaDonByMa(searchVal);
                if (hd != null) {
                    Date lap = hd.getNgayLap();
                    if (lap != null && !lap.before(start) && !lap.after(end)) {
                        addHoaDonToTable(hd);
                    } else {
                        JOptionPane.showMessageDialog(this, "Hóa đơn mã '" + searchVal + "' không nằm trong khoảng ngày đã chọn!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Mã hóa đơn '" + searchVal + "' không tồn tại!");
                }
            } else {
                List<HoaDon> ds = hd_dao.getHoaDonByDateRange(start, end);
                if (ds != null && !ds.isEmpty()) {
                    for (HoaDon hd : ds) addHoaDonToTable(hd);
                } else {
                    JOptionPane.showMessageDialog(this, "Không có hóa đơn trong khoảng ngày này!");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày!");
            ex.printStackTrace();
        }
    }
    class InvoiceDialog extends JDialog {
        public InvoiceDialog(JFrame parent, HoaDon hd, List<ChiTietHoaDon> dsCT, boolean isDetail) {
            super(parent, isDetail ? "Chi Tiết Hóa Đơn" : "Phiếu Thanh Toán", true);
            
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int height = (int)(screenSize.height * 0.7);
            setSize(450, height);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(new Color(240, 240, 240));

            JPanel pOuter = new JPanel(new GridBagLayout());
            pOuter.setOpaque(false);
            pOuter.setBorder(new EmptyBorder(20, 10, 20, 10));

            JPanel pInvoice = new JPanel();
            pInvoice.setLayout(new BoxLayout(pInvoice, BoxLayout.Y_AXIS));
            pInvoice.setBackground(Color.WHITE);
            pInvoice.setPreferredSize(new Dimension(380, 800)); 
            pInvoice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(30, 25, 30, 25)
            ));

            JLabel lblHeader = new JLabel(isDetail ? "CHI TIẾT HÓA ĐƠN" : "GOLDEN PEARL RESTAURANT", SwingConstants.CENTER);
            lblHeader.setFont(new Font("Inter Bold", Font.BOLD, 22));
            lblHeader.setForeground(isDetail ? MAIN_BLUE : Color.BLACK);
            lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblAddr = new JLabel("Địa chỉ: ...........................................................", SwingConstants.CENTER);
            lblAddr.setFont(new Font("Inter", Font.PLAIN, 12));
            lblAddr.setForeground(Color.BLACK);
            lblAddr.setAlignmentX(Component.CENTER_ALIGNMENT);
            if (isDetail) lblAddr.setVisible(false);

            JLabel lblPh = new JLabel("SĐT: ...........................................................", SwingConstants.CENTER);
            lblPh.setFont(new Font("Inter", Font.PLAIN, 12));
            lblPh.setForeground(Color.BLACK);
            lblPh.setAlignmentX(Component.CENTER_ALIGNMENT);
            if (isDetail) lblPh.setVisible(false);

            JLabel lblTitle = new JLabel(isDetail ? "THÔNG TIN THANH TOÁN" : "HÓA ĐƠN THANH TOÁN", SwingConstants.CENTER);
            lblTitle.setFont(new Font("Inter Bold", Font.BOLD, 20));
            lblTitle.setForeground(Color.BLACK);
            lblTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
            lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            pInvoice.add(lblHeader);
            if (!isDetail) {
                pInvoice.add(lblAddr);
                pInvoice.add(lblPh);
            }
            pInvoice.add(lblTitle);
            pInvoice.add(createDashedLine());

            JPanel pInfo = new JPanel(new GridLayout(0, 1, 0, 5));
            pInfo.setOpaque(false);
            pInfo.add(createInvoiceLine("Mã hóa đơn:", hd.getMaHD()));
            pInfo.add(createInvoiceLine("Ngày lập:", sdf.format(hd.getNgayLap())));
            pInfo.add(createInvoiceLine("Nhân viên:", (hd.getNhanVien() != null ? hd.getNhanVien().getTenNV() : "Admin")));
            pInfo.add(createInvoiceLine("Khách hàng:", (hd.getKhachHang() != null ? hd.getKhachHang().getTenKH() : "Khách vãng lai")));
            pInvoice.add(pInfo);
            pInvoice.add(Box.createVerticalStrut(15));
            pInvoice.add(createDashedLine());

            JPanel pTableHeader = new JPanel(new BorderLayout());
            pTableHeader.setOpaque(false);
            JLabel h1 = new JLabel("Tên món (SL x Giá)"); h1.setFont(new Font("Inter Bold", Font.BOLD, 13));
            h1.setForeground(Color.BLACK);
            JLabel h2 = new JLabel("Thành tiền"); h2.setFont(new Font("Inter Bold", Font.BOLD, 13));
            h2.setForeground(Color.BLACK);
            pTableHeader.add(h1, BorderLayout.WEST);
            pTableHeader.add(h2, BorderLayout.EAST);
            pInvoice.add(pTableHeader);
            pInvoice.add(Box.createVerticalStrut(10));

            for (ChiTietHoaDon ct : dsCT) {
                JPanel pItem = new JPanel(new BorderLayout());
                pItem.setOpaque(false);
                String dishName = (ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "Món ẩn");
                JLabel name = new JLabel("<html><body style='width: 160px; color: black'><b>" + dishName + "</b><br/>" + 
                                         ct.getSoLuong() + " x " + String.format("%,.0f", ct.getDonGia()) + "</body></html>");
                JLabel price = new JLabel(String.format("%,.0fđ", ct.getThanhTien()));
                price.setForeground(Color.BLACK);
                price.setFont(new Font("Inter Bold", Font.BOLD, 13));
                pItem.add(name, BorderLayout.WEST);
                pItem.add(price, BorderLayout.EAST);
                pInvoice.add(pItem);
                pInvoice.add(Box.createVerticalStrut(10));
            }

            pInvoice.add(createDashedLine());

            JPanel pTotal = new JPanel(new BorderLayout());
            pTotal.setOpaque(false);
            JLabel t1 = new JLabel("TỔNG TIỀN:");
            t1.setFont(new Font("Inter Bold", Font.BOLD, 13)); // Thu nhỏ cỡ chữ xuống 13
            t1.setForeground(Color.BLACK);
            JLabel t2 = new JLabel(String.format("%,.0f VNĐ", hd.getTongTien()));
            t2.setFont(new Font("Inter Bold", Font.BOLD, 13)); // Thu nhỏ cỡ chữ xuống 13
            t2.setForeground(Color.RED);
            pTotal.add(t1, BorderLayout.WEST);
            pTotal.add(t2, BorderLayout.EAST);
            pInvoice.add(pTotal);

            pInvoice.add(createDashedLine()); // Xóa bỏ Box.createVerticalStrut(30)

            JLabel lblWifi = new JLabel("Wifi: GOLDEN PEARL  -  Mật khẩu: xincamon", SwingConstants.CENTER);
            lblWifi.setFont(new Font("Inter", Font.ITALIC, 11));
            lblWifi.setForeground(Color.BLACK);
            lblWifi.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel lblWish = new JLabel("Kính chúc Quý khách nhiều sức khỏe và niềm vui!", SwingConstants.CENTER);
            lblWish.setFont(new Font("Inter", Font.ITALIC, 12));
            lblWish.setForeground(Color.BLACK);
            lblWish.setBorder(new EmptyBorder(10, 0, 5, 0));
            lblWish.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblBye = new JLabel("HẸN GẶP LẠI QUÝ KHÁCH!", SwingConstants.CENTER);
            lblBye.setFont(new Font("Inter Bold", Font.BOLD, 13));
            lblBye.setForeground(Color.BLACK);
            lblBye.setAlignmentX(Component.CENTER_ALIGNMENT);

            if (!isDetail) {
                pInvoice.add(lblWifi);
                pInvoice.add(lblWish);
                pInvoice.add(lblBye);
            }

            pOuter.add(pInvoice);
            JScrollPane scroll = new JScrollPane(pOuter);
            scroll.setBorder(null);
            scroll.setBackground(new Color(240, 240, 240));
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            add(scroll, BorderLayout.CENTER);

            JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
            pButtons.setBackground(isDetail ? Color.WHITE : MAIN_BLUE);

            if (isDetail) {
                JButton btnClose = createInvoiceButton("ĐÓNG", MAIN_BLUE);
                btnClose.addActionListener(e -> dispose());
                pButtons.add(btnClose);
            } else {
                JButton btnHuy = createInvoiceButton("HỦY", new Color(231, 76, 60));
                btnHuy.addActionListener(e -> dispose());

                JButton btnInAction = createInvoiceButton("IN HÓA ĐƠN", new Color(46, 204, 113));
                btnInAction.addActionListener(e -> {
                    JOptionPane.showMessageDialog(this, "Hóa đơn đang được in...");
                    dispose();
                });

                pButtons.add(btnHuy);
                pButtons.add(btnInAction);
            }
            add(pButtons, BorderLayout.SOUTH);
        }

        private JButton createInvoiceButton(String text, Color bg) {
            JButton btn = new JButton(text);
            btn.setPreferredSize(new Dimension(140, 40));
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Inter Bold", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private JPanel createInvoiceLine(String label, String value) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JLabel l = new JLabel(label);
            l.setFont(new Font("Inter", Font.PLAIN, 12));
            l.setForeground(Color.BLACK);
            JLabel v = new JLabel(value);
            v.setFont(new Font("Inter Bold", Font.BOLD, 12));
            v.setForeground(Color.BLACK);
            p.add(l, BorderLayout.WEST);
            p.add(v, BorderLayout.EAST);
            return p;
        }

        private JLabel createDashedLine() {
            JLabel l = new JLabel("----------------------------------");
            l.setForeground(Color.BLACK);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            return l;
        }
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
                if(!target.getText().isEmpty()) cal.setTime(dateOnlySdf.parse(target.getText()));
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
                if (dateOnlySdf.format(temp.getTime()).equals(targetField.getText())) {
                    btnDay.setBackground(GOLD_COLOR);
                    btnDay.setForeground(MAIN_BLUE);
                } else if (dateOnlySdf.format(temp.getTime()).equals(dateOnlySdf.format(today.getTime()))) {
                    btnDay.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 2));
                }

                btnDay.addActionListener(e -> {
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    targetField.setText(dateOnlySdf.format(cal.getTime()));
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
