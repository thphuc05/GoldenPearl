package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import entity.*;
import dao.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * QuanLyDatBan – full rewrite
 *
 * Cards:
 *   "TableSelection"  – chon ban (filter + grid)
 *   "CustomerForm"    – thong tin khach hang
 *   "OrderSelection"  – goi mon
 *   "Payment"         – thanh toan
 */
public class QuanLyDatBan extends JPanel {

    // ── Navigation ──────────────────────────────────────────────────────────
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     mainPanel  = new JPanel(cardLayout);

    // ── DAOs ────────────────────────────────────────────────────────────────
    private final Ban_DAO      ban_dao      = new Ban_DAO();
    private final SanPham_DAO  sanPham_dao  = new SanPham_DAO();

    // ── Theme ────────────────────────────────────────────────────────────────
    private static final Color NAVY         = Color.decode("#0B3D59");
    private static final Color NAVY_LIGHT   = Color.decode("#0A324A");
    private static final Color GOLD         = Color.decode("#C5A059");
    private static final Color ORANGE       = Color.decode("#E67E22");
    private static final Color GREEN_SEL    = Color.decode("#27AE60");  // ban duoc chon
    private static final Color RED_BUSY     = Color.decode("#E74C3C");  // dang phuc vu
    private static final Color YELLOW_BOOKED= Color.decode("#F39C12");  // da dat
    private static final Color BLUE_FREE    = Color.decode("#5B6FCC");  // con trong
    private static final Color BG_LIGHT     = Color.decode("#F2F3F4");
    private static final Color TEXT_GRAY    = Color.decode("#7F8C8D");
    private static final Color ROW_ALT      = Color.decode("#EBF5FB");
    private static final Font  FONT_BOLD_16 = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font  FONT_PLAIN_14= new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font  FONT_BOLD_14 = new Font("Segoe UI", Font.BOLD, 14);

    // ── State ────────────────────────────────────────────────────────────────
    private boolean          multiSelectMode = false;
    private final List<Ban>  selectedBans    = new ArrayList<>();
    private boolean          depositPaid     = false;   // co tien coc khong
    private static final long DEPOSIT        = 500_000L;

    // Mon an da chon: SanPham -> so luong
    private final Map<SanPham, Integer> orderMap = new LinkedHashMap<>();

    // ── Panel 1 widgets ──────────────────────────────────────────────────────
    private JPanel             tableGrid;
    private JPanel             multiSelectBar;
    private JComboBox<String>  cboKhuVuc;
    private JComboBox<String>  cboKhungGio;
    private DatePicker         datePicker;

    // ── Panel 2 widgets (CustomerForm) ──────────────────────────────────────
    private JTextField txtInfoBan, txtInfoKhuVuc, txtInfoKhungGio, txtInfoNgay;
    private JTextField txtCustName, txtCustPhone;
    private JTextArea  txtCustNote;

    // ── Panel 3 widgets (OrderSelection) ────────────────────────────────────
    private JLabel lblOrderBan;
    private JPanel menuTableBody;
    private JPanel orderListBody;
    private JLabel lblOrderTotal;

    // ── Panel 4 widgets (Payment) ────────────────────────────────────────────
    private JLabel       lblPayInfo;
    private JLabel       lblPayDeposit, lblPayFood, lblPaySubtotal;
    private JLabel       lblPayDiscount, lblPayFinal;
    private JComboBox<String> cboVoucher;
    private JPanel       paymentPanel;

    // ════════════════════════════════════════════════════════════════════════
    public QuanLyDatBan() {
        setLayout(new BorderLayout());
        buildAllPanels();
        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "TableSelection");
    }

    private void buildAllPanels() {
        JPanel p1 = buildTableSelectionPanel();
        JPanel p2 = buildCustomerFormPanel();
        JPanel p3 = buildOrderSelectionPanel();
        JPanel p4 = buildPaymentPanel();

        mainPanel.add(p1, "TableSelection");
        mainPanel.add(p2, "CustomerForm");
        mainPanel.add(p3, "OrderSelection");
        mainPanel.add(p4, "Payment");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL 1 – CHON BAN
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTableSelectionPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(NAVY);

        // ── Header title ────────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setPreferredSize(new Dimension(0, 68));
        JLabel title = new JLabel("HỆ THỐNG ĐẶT BÀN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(GOLD);
        hdr.add(title, BorderLayout.CENTER);

        // ── Filter bar ──────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(hdr,             BorderLayout.NORTH);
        top.add(buildFilterBar(),BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);

        // ── Grid ban ────────────────────────────────────────────────────────
        tableGrid = new JPanel(new GridLayout(0, 4, 18, 18));
        tableGrid.setOpaque(false);
        tableGrid.setBorder(new EmptyBorder(22, 36, 10, 36));
        loadTables();

        JScrollPane scroll = new JScrollPane(tableGrid);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        root.add(scroll, BorderLayout.CENTER);

        // ── South: multi-select bar + legend ────────────────────────────────
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        multiSelectBar = buildMultiSelectBar();
        south.add(multiSelectBar, BorderLayout.NORTH);
        south.add(buildLegendBar(), BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);

        tableSelectionPanel = root;
        return root;
    }

    // Giu bien de co the goi loadTables() tu ngoai
    private JPanel tableSelectionPanel;

    // ── Filter bar (GridLayout 1x4 – chia deu chieu ngang) ─────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 0, 0));
        bar.setBackground(NAVY_LIGHT);
        bar.setPreferredSize(new Dimension(0, 54));

        cboKhuVuc   = new JComboBox<>(new String[]{"Thường", "Cao cấp"});
        cboKhungGio = new JComboBox<>(new String[]{
                "Trưa: 10h - 14h", "Chiều: 15h - 19h", "Tối: 19h30 - 23h"});
        datePicker  = buildDatePicker();

        bar.add(filterCell("Khu Vực",   cboKhuVuc));
        bar.add(filterCell("Khung Giờ", cboKhungGio));
        bar.add(filterCell("Ngày",      datePicker));

        // Nut chon nhieu ban
        JButton btnMulti = navButton("Chọn Nhiều Bàn", GOLD, NAVY);
        btnMulti.addActionListener(e -> toggleMultiSelect());
        JPanel cell = new JPanel(new GridBagLayout());
        cell.setBackground(NAVY_LIGHT);
        cell.setBorder(new MatteBorder(0, 1, 0, 0, GOLD.darker()));
        cell.add(btnMulti);
        bar.add(cell);
        return bar;
    }

    private DatePicker buildDatePicker() {
        DatePickerSettings s = new DatePickerSettings();
        DatePicker dp = new DatePicker(s);
        s.setAllowKeyboardEditing(false);          // khong cho nhap tay sai dinh dang
        s.setFormatForDatesCommonEra("dd/MM/yyyy");
        s.setDateRangeLimits(LocalDate.now(), null); // chi chon tu hom nay tro do
        dp.setDate(LocalDate.now());
        return dp;
    }

    private JPanel filterCell(String label, JComponent widget) {
        JPanel cell = new JPanel(new GridBagLayout());
        cell.setBackground(NAVY_LIGHT);
        cell.setBorder(new MatteBorder(0, 0, 0, 1, GOLD.darker()));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 8, 0, 4);

        JLabel lbl = new JLabel(label + " :");
        lbl.setFont(FONT_PLAIN_14);
        lbl.setForeground(Color.WHITE);
        g.gridx = 0; cell.add(lbl, g);

        widget.setFont(FONT_BOLD_14);
        widget.setPreferredSize(new Dimension(160, 30));
        g.gridx = 1; g.insets = new Insets(0, 4, 0, 8);
        cell.add(widget, g);
        return cell;
    }

    // ── Multi-select bar ────────────────────────────────────────────────────
    private JPanel buildMultiSelectBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 7));
        bar.setBackground(Color.decode("#0D2F45"));
        bar.setVisible(false);

        JLabel hint = new JLabel("Click vào bàn để chọn hoặc bỏ chọn");
        hint.setForeground(GOLD); hint.setFont(FONT_PLAIN_14);

        JButton btnCancel = navButton("Hủy", RED_BUSY, Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(130, 36));
        btnCancel.addActionListener(e -> cancelMultiSelect());

        JButton btnBook = navButton("Đặt Bàn", ORANGE, Color.WHITE);
        btnBook.setPreferredSize(new Dimension(130, 36));
        btnBook.addActionListener(e -> proceedMultiBooking());

        bar.add(hint); bar.add(btnCancel); bar.add(btnBook);
        return bar;
    }

    private JPanel buildLegendBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 36, 8));
        bar.setBackground(NAVY);
        bar.add(legendItem(RED_BUSY,     "Đang Phục Vụ"));
        bar.add(legendItem(YELLOW_BOOKED,"Đã Đặt"));
        bar.add(legendItem(BLUE_FREE,    "Còn Trống"));
        bar.add(legendItem(GREEN_SEL,    "Đang Chọn"));
        return bar;
    }

    private JPanel legendItem(Color c, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JPanel box = new JPanel(); box.setBackground(c);
        box.setPreferredSize(new Dimension(60, 24));
        p.add(box);
        JLabel l = new JLabel(text); l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(l);
        return p;
    }

    // ── Load / build nut ban ─────────────────────────────────────────────────
    private void loadTables() {
        tableGrid.removeAll();
        List<Ban> list = ban_dao.getAllBan();
        for (Ban ban : list) tableGrid.add(buildTableCard(ban));
        tableGrid.revalidate();
        tableGrid.repaint();
    }

    /**
     * Card bao gom:
     *  - Nut ban (toan bo card)
     *  - Neu ban da dat  → them nut nho "Goi mon" o duoi
     */
    private JPanel buildTableCard(Ban ban) {
        boolean isSelected = selectedBans.contains(ban);
        boolean isBooked   = ban.getTinhTrangBan() == TrangThaiBan.DaDuocDat;
        boolean isBusy     = ban.getTinhTrangBan() == TrangThaiBan.DangDuocSuDung;

        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(2, 2, 2, 2));

        // ── Nut ban chinh ───────────────────────────────────────────────────
        Color baseBg = isBusy ? RED_BUSY : (isBooked ? YELLOW_BOOKED : BLUE_FREE);
        Color btnBg  = isSelected ? GREEN_SEL : baseBg;

        JButton btnBan = new JButton("<html><center>ban " + ban.getSoBan() + "</center></html>");
        btnBan.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        btnBan.setBackground(btnBg);
        btnBan.setForeground(Color.WHITE);
        btnBan.setFocusPainted(false);
        btnBan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBan.setPreferredSize(new Dimension(150, 120));
        btnBan.setBorder(isSelected
                ? new LineBorder(GOLD, 4)
                : new LineBorder(Color.WHITE, 2));

        // Hover
        btnBan.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!selectedBans.contains(ban))
                    btnBan.setBorder(new LineBorder(GOLD, 3));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!selectedBans.contains(ban))
                    btnBan.setBorder(new LineBorder(Color.WHITE, 2));
            }
        });

        btnBan.addActionListener(e -> {
            if (multiSelectMode) {
                // Toggle chon / bo chon + doi mau ngay lap tuc
                if (selectedBans.contains(ban)) {
                    selectedBans.remove(ban);
                    btnBan.setBackground(baseBg);
                    btnBan.setBorder(new LineBorder(Color.WHITE, 2));
                } else {
                    selectedBans.add(ban);
                    btnBan.setBackground(GREEN_SEL);
                    btnBan.setBorder(new LineBorder(GOLD, 4));
                }
            } else {
                // Chon 1 ban → form thong tin
                selectedBans.clear();
                selectedBans.add(ban);
                openCustomerForm(false);
            }
        });
        card.add(btnBan, BorderLayout.CENTER);

        // ── Nut "Goi mon" cho ban da dat ────────────────────────────────────
        if (isBooked || isBusy) {
            JButton btnOrder = new JButton("Gọi Món");
            btnOrder.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnOrder.setBackground(NAVY);
            btnOrder.setForeground(GOLD);
            btnOrder.setFocusPainted(false);
            btnOrder.setBorder(new LineBorder(GOLD, 1));
            btnOrder.setPreferredSize(new Dimension(150, 28));
            btnOrder.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOrder.addActionListener(e -> {
                selectedBans.clear();
                selectedBans.add(ban);
                depositPaid = false;
                orderMap.clear();
                openOrderSelection();
            });
            card.add(btnOrder, BorderLayout.SOUTH);
        }

        return card;
    }

    // ── Multi-select logic ───────────────────────────────────────────────────
    private void toggleMultiSelect() {
        multiSelectMode = true;
        selectedBans.clear();
        multiSelectBar.setVisible(true);
        loadTables();
    }

    private void cancelMultiSelect() {
        multiSelectMode = false;
        selectedBans.clear();
        multiSelectBar.setVisible(false);
        loadTables();
    }

    private void proceedMultiBooking() {
        if (selectedBans.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 bàn!");
            return;
        }
        multiSelectMode = false;
        multiSelectBar.setVisible(false);
        openCustomerForm(false);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL 2 – FORM THONG TIN KHACH HANG  (full page)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildCustomerFormPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(NAVY);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(NAVY);
        hdr.setPreferredSize(new Dimension(0, 68));
        JLabel title = new JLabel("THÔNG TIN ĐẶT BÀN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(GOLD);
        hdr.add(title, BorderLayout.CENTER);
        root.add(hdr, BorderLayout.NORTH);

        // ── Form card (trang – full) ─────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(8, 12, 8, 12);
        g.weightx = 1;

        // Section 1: tu dong dien
        addSectionTitle(card, g, 0, "Thông Tin Đặt Bàn");
        txtInfoBan      = addDisabledRow(card, g, 1, "Bàn");
        txtInfoKhuVuc   = addDisabledRow(card, g, 2, "Khu vực");
        txtInfoKhungGio = addDisabledRow(card, g, 3, "Khung giờ");
        txtInfoNgay     = addDisabledRow(card, g, 4, "Ngày");

        // Section 2: nhap tay
        addSectionTitle(card, g, 5, "Thông tin khách hàng");
        txtCustName  = addInputRow(card, g, 6, "Tên khách hàng");
        txtCustPhone = addInputRow(card, g, 7, "Số điện thoại");

        // Ghi chu
        g.gridy = 8; g.gridx = 0; g.gridwidth = 1;
        card.add(fieldLabel("Ghi chú"), g);
        g.gridx = 1;
        txtCustNote = new JTextArea(4, 28);
        txtCustNote.setForeground(Color.black);
        txtCustNote.setFont(FONT_PLAIN_14);
        txtCustNote.setLineWrap(true);
        txtCustNote.setWrapStyleWord(true);
        txtCustNote.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 210, 210), 1),
                new EmptyBorder(6, 10, 6, 10)));
        card.add(new JScrollPane(txtCustNote), g);

        // Filler de day xuong
        g.gridy = 9; g.gridx = 0; g.gridwidth = 2; g.weighty = 1;
        card.add(Box.createVerticalGlue(), g);
        g.weighty = 0;

        // ── 3 nut o duoi ───────────────────────────────────────────────────
        g.gridy = 10; g.gridx = 0; g.gridwidth = 2;
        g.insets = new Insets(20, 20, 20, 20);
        JPanel btnRow = new JPanel(new GridLayout(1, 3, 16, 0));
        btnRow.setOpaque(false);

        JButton btnBack    = navButton("Quay Lại",          Color.WHITE,  NAVY, true);
        JButton btnDeposit = navButton("Đặt Bàn ",  ORANGE,       Color.WHITE, false);
        JButton btnFull    = navButton("Đặt bàn và gọi món",   NAVY,         GOLD, false);

        btnBack.addActionListener(e -> {
            selectedBans.clear();
            loadTables();
            cardLayout.show(mainPanel, "TableSelection");
        });
        btnDeposit.addActionListener(e -> handleConfirmBooking(true));
        btnFull.addActionListener(e    -> handleConfirmBooking(false));

        btnRow.add(btnBack);
        btnRow.add(btnDeposit);
        btnRow.add(btnFull);
        card.add(btnRow, g);

        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private void openCustomerForm(boolean skipForm) {
        // Dien thong tin tu dong
        StringBuilder banStr = new StringBuilder("Bàn");
        for (int i = 0; i < selectedBans.size(); i++) {
            if (i > 0) banStr.append(", ");
            banStr.append(selectedBans.get(i).getSoBan());
        }
        txtInfoBan.setText(banStr.toString());
        txtInfoKhuVuc.setText((String) cboKhuVuc.getSelectedItem());
        txtInfoKhungGio.setText((String) cboKhungGio.getSelectedItem());
        LocalDate d = datePicker.getDate();
        txtInfoNgay.setText(d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        txtCustName.setText(""); txtCustPhone.setText(""); txtCustNote.setText("");
        cardLayout.show(mainPanel, "CustomerForm");
    }

    private void handleConfirmBooking(boolean depositOnly) {
        if (txtCustName.getText().trim().isEmpty() || txtCustPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên và số điện thoại!");
            return;
        }
        // Cap nhat trang thai ban
        for (Ban ban : selectedBans)
            ban_dao.updateTinhTrangBan(ban.getMaBan(), TrangThaiBan.DaDuocDat);

        depositPaid = depositOnly;
        orderMap.clear();

        if (depositOnly) {
            // Chi dat ban -> sang trang thanh toan tien coc
            refreshPaymentPanel();
            cardLayout.show(mainPanel, "Payment");
        } else {
            // Dat ban va goi mon
            openOrderSelection();
        }
        loadTables();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL 3 – GOI MON
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildOrderSelectionPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(NAVY);
        hdr.setPreferredSize(new Dimension(0, 60));
        lblOrderBan = new JLabel("Goi món – Bàn ?", SwingConstants.LEFT);
        lblOrderBan.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblOrderBan.setForeground(GOLD);
        lblOrderBan.setBorder(new EmptyBorder(0, 20, 0, 0));
        hdr.add(lblOrderBan, BorderLayout.WEST);
        root.add(hdr, BorderLayout.NORTH);

        // Body: LEFT = menu | RIGHT = gio hang
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);

        // LEFT – bang mon an
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BG_LIGHT);
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 2, new Color(220, 220, 220)));
        leftPanel.add(buildColHeader(NAVY, Color.WHITE,
                        new int[]{30, 30, 20, 10}, new String[]{"Tên món", "Mô tả", "Đơn giá", "+"}),
                BorderLayout.NORTH);

        menuTableBody = new JPanel();
        menuTableBody.setLayout(new BoxLayout(menuTableBody, BoxLayout.Y_AXIS));
        menuTableBody.setBackground(Color.WHITE);
        JScrollPane ms = new JScrollPane(menuTableBody);
        ms.setBorder(null); ms.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(ms, BorderLayout.CENTER);
        body.add(leftPanel, BorderLayout.CENTER);

        // RIGHT – gio hang
        JPanel right = new JPanel(new BorderLayout());
        right.setPreferredSize(new Dimension(310, 0));
        right.setBackground(Color.WHITE);

        JPanel rightTitle = new JPanel(new BorderLayout());
        rightTitle.setBackground(NAVY);
        rightTitle.setPreferredSize(new Dimension(0, 44));
        JLabel lbl = new JLabel("Thực đơn chọn", SwingConstants.CENTER);
        lbl.setFont(FONT_BOLD_16); lbl.setForeground(GOLD);
        rightTitle.add(lbl, BorderLayout.CENTER);
        right.add(rightTitle, BorderLayout.NORTH);

        orderListBody = new JPanel();
        orderListBody.setLayout(new BoxLayout(orderListBody, BoxLayout.Y_AXIS));
        orderListBody.setBackground(Color.WHITE);
        JScrollPane os = new JScrollPane(orderListBody);
        os.setBorder(null); os.getVerticalScrollBar().setUnitIncrement(12);
        right.add(os, BorderLayout.CENTER);

        // Tong tien + nut Thanh toan
        JPanel rightBottom = new JPanel(new BorderLayout());
        rightBottom.setBackground(new Color(245, 240, 230));
        rightBottom.setBorder(new MatteBorder(2, 0, 0, 0, new Color(200, 200, 200)));

        JPanel totalRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        totalRow.setOpaque(false);
        JLabel lblTotalLbl = new JLabel("Tổng tiền:");
        lblTotalLbl.setFont(FONT_BOLD_14); lblTotalLbl.setForeground(NAVY);
        lblOrderTotal = new JLabel("0 VND");
        lblOrderTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblOrderTotal.setForeground(ORANGE);
        totalRow.add(lblTotalLbl); totalRow.add(lblOrderTotal);
        rightBottom.add(totalRow, BorderLayout.NORTH);

        JPanel orderBtns = new JPanel(new GridLayout(1, 2, 8, 0));
        orderBtns.setOpaque(false);
        orderBtns.setBorder(new EmptyBorder(0, 10, 10, 10));
        JButton btnBack  = navButton(" Quay Lại", Color.WHITE, NAVY, true);
        JButton btnPay   = navButton("Thanh Toán", NAVY, Color.WHITE, false);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "CustomerForm"));
        btnPay.addActionListener(e  -> { refreshPaymentPanel(); cardLayout.show(mainPanel, "Payment"); });
        orderBtns.add(btnBack); orderBtns.add(btnPay);
        rightBottom.add(orderBtns, BorderLayout.SOUTH);
        right.add(rightBottom, BorderLayout.SOUTH);

        body.add(right, BorderLayout.EAST);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private void openOrderSelection() {
        if (!selectedBans.isEmpty()) {
            StringBuilder sb = new StringBuilder("Gọi món ");
            for (int i = 0; i < selectedBans.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("Bàn ").append(selectedBans.get(i).getSoBan());
            }
            lblOrderBan.setText(sb.toString());
        }
        loadMenuRows();
        refreshOrderList();
        cardLayout.show(mainPanel, "OrderSelection");
    }

    private void loadMenuRows() {
        menuTableBody.removeAll();
        List<SanPham> list = sanPham_dao.getAllSanPham();
        for (int i = 0; i < list.size(); i++) {
            menuTableBody.add(buildMenuRow(list.get(i), i % 2 == 0 ? Color.WHITE : ROW_ALT));
            menuTableBody.add(thinDivider());
        }
        menuTableBody.revalidate();
        menuTableBody.repaint();
    }

    private JPanel buildMenuRow(SanPham sp, Color bg) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(bg);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        row.setPreferredSize(new Dimension(0, 54));

        // Ten + Mo ta
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 0));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(4, 10, 4, 4));
        JLabel lblName = new JLabel(sp.getTenMon());
        lblName.setFont(FONT_BOLD_14);
        JLabel lblDesc = new JLabel("Món ngon đặc trưng");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(TEXT_GRAY);
        info.add(lblName); info.add(lblDesc);

        // Gia
        JLabel lblPrice = new JLabel(String.format("%,.0f d", sp.getDonGia()), SwingConstants.CENTER);
        lblPrice.setFont(FONT_BOLD_14); lblPrice.setForeground(ORANGE);
        lblPrice.setPreferredSize(new Dimension(100, 0));

        // Nut +
        JButton btnAdd = circleBtn("+", ORANGE);
        btnAdd.setPreferredSize(new Dimension(48, 48));
        btnAdd.addActionListener(e -> { orderMap.merge(sp, 1, Integer::sum); refreshOrderList(); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        right.setOpaque(false);
        right.add(lblPrice); right.add(btnAdd);

        row.add(info,  BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void refreshOrderList() {
        orderListBody.removeAll();
        long total = 0;

        if (orderMap.isEmpty()) {
            JLabel empty = new JLabel("Chưa chọn món", SwingConstants.CENTER);
            empty.setForeground(TEXT_GRAY);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            orderListBody.add(Box.createVerticalStrut(16));
            orderListBody.add(empty);
        } else {
            for (Map.Entry<SanPham, Integer> en : orderMap.entrySet()) {
                orderListBody.add(buildOrderRow(en.getKey(), en.getValue()));
                orderListBody.add(thinDivider());
                total += (long)(en.getKey().getDonGia() * en.getValue());
            }
        }

        lblOrderTotal.setText(String.format("%,.0f VND", (double) total));
        orderListBody.revalidate();
        orderListBody.repaint();
    }

    private JPanel buildOrderRow(SanPham sp, int qty) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setPreferredSize(new Dimension(0, 48));
        row.setBorder(new EmptyBorder(0, 10, 0, 6));

        JLabel lblName = new JLabel(sp.getTenMon());
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(lblName, BorderLayout.CENTER);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 8));
        ctrl.setOpaque(false);
        JButton btnM = circleBtn("-", NAVY);
        JLabel  lblQ = new JLabel(String.valueOf(qty), SwingConstants.CENTER);
        lblQ.setPreferredSize(new Dimension(26, 26)); lblQ.setFont(FONT_BOLD_14);
        JButton btnP = circleBtn("+", ORANGE);
        btnM.addActionListener(e -> {
            int cur = orderMap.getOrDefault(sp, 0);
            if (cur <= 1) orderMap.remove(sp); else orderMap.put(sp, cur - 1);
            refreshOrderList();
        });
        btnP.addActionListener(e -> { orderMap.merge(sp, 1, Integer::sum); refreshOrderList(); });
        ctrl.add(btnM); ctrl.add(lblQ); ctrl.add(btnP);
        row.add(ctrl, BorderLayout.EAST);
        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANEL 4 – THANH TOAN
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildPaymentPanel() {
        paymentPanel = new JPanel(new BorderLayout());
        paymentPanel.setBackground(Color.WHITE);

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(NAVY);
        hdr.setPreferredSize(new Dimension(0, 60));
        JLabel title = new JLabel("THANH TOÁN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(GOLD);
        hdr.add(title, BorderLayout.CENTER);
        paymentPanel.add(hdr, BorderLayout.NORTH);

        // Body: LEFT = voucher | RIGHT = hoa don
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(Color.WHITE);

        // LEFT – voucher
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(270, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(0, 0, 0, 2, new Color(220, 220, 220)));

        JPanel vTitleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        vTitleBar.setBackground(ORANGE);
        JLabel vTitle = new JLabel("Voucher giảm giá");
        vTitle.setFont(FONT_BOLD_16); vTitle.setForeground(Color.WHITE);
        vTitleBar.add(vTitle);
        leftPanel.add(vTitleBar, BorderLayout.NORTH);

        JPanel vContent = new JPanel();
        vContent.setLayout(new BoxLayout(vContent, BoxLayout.Y_AXIS));
        vContent.setBackground(new Color(255, 248, 240));
        vContent.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel vHint = new JLabel("Chọn 1 voucher:");
        vHint.setFont(FONT_BOLD_14); vHint.setForeground(NAVY);
        vHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        vContent.add(vHint);
        vContent.add(Box.createVerticalStrut(10));

        // Radio group – chi chon 1
        cboVoucher = new JComboBox<>(new String[]{
                "-- Không sử dụng --",
                "GIAM5  – giảm 5%  (Đơn >= 300K)",
                "GIAM8  – giảm 8%  (Đơn >= 450K)",
                "GIAM10 – giảm 10% (Đơn >= 800K)"
        });
        cboVoucher.setFont(FONT_PLAIN_14);
        cboVoucher.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cboVoucher.setAlignmentX(Component.LEFT_ALIGNMENT);
        vContent.add(cboVoucher);
        vContent.add(Box.createVerticalStrut(14));

        JButton btnApply = navButton("Áp Dụng", NAVY, Color.WHITE, false);
        btnApply.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnApply.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnApply.addActionListener(e -> applyVoucher());
        vContent.add(btnApply);

        leftPanel.add(vContent, BorderLayout.CENTER);
        body.add(leftPanel, BorderLayout.WEST);

        // RIGHT – hoa don
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);

        // Sub-header: so ban + khach
        lblPayInfo = new JLabel("", SwingConstants.LEFT);
        lblPayInfo.setFont(FONT_BOLD_14); lblPayInfo.setForeground(NAVY);
        lblPayInfo.setBackground(new Color(245, 240, 230)); lblPayInfo.setOpaque(true);
        lblPayInfo.setBorder(new EmptyBorder(8, 16, 8, 16));
        rightPanel.add(lblPayInfo, BorderLayout.NORTH);

        // Bang chi tiet tien
        JPanel billDetail = new JPanel();
        billDetail.setLayout(new BoxLayout(billDetail, BoxLayout.Y_AXIS));
        billDetail.setBackground(Color.WHITE);
        billDetail.setBorder(new EmptyBorder(16, 20, 16, 20));

        lblPayDeposit  = makeBillRow("Tiền cọc:", "0 VND");
        lblPayFood     = makeBillRow("Tiền món ăn:", "0 VND");
        lblPaySubtotal = makeBillRow("Tạm Tính:", "0 VND");

        JLabel divider = new JLabel("  ─────────────────────────────");
        divider.setForeground(new Color(200, 200, 200));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPayDiscount = makeBillRow("Giảm giá:", "0 VND");
        lblPayFinal    = makeBillRow("TỔNG THANH TOÁN:", "0 VND");
        lblPayFinal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblPayFinal.setForeground(ORANGE);

        billDetail.add(addBillLine("Tiền cọc",   lblPayDeposit));
        billDetail.add(Box.createVerticalStrut(8));
        billDetail.add(addBillLine("Tiền món ăn", lblPayFood));
        billDetail.add(Box.createVerticalStrut(8));
        billDetail.add(addBillLine("Tạm Tính",    lblPaySubtotal));
        billDetail.add(Box.createVerticalStrut(6));
        billDetail.add(divider);
        billDetail.add(Box.createVerticalStrut(6));
        billDetail.add(addBillLine("Giảm giá",    lblPayDiscount));
        billDetail.add(Box.createVerticalStrut(10));
        billDetail.add(addBillLine("TỔNG THANH TOÁN", lblPayFinal));

        JScrollPane bs = new JScrollPane(billDetail); bs.setBorder(null);
        rightPanel.add(bs, BorderLayout.CENTER);

        // Nut xac nhan
        JPanel payBtns = new JPanel(new GridLayout(1, 2, 12, 0));
        payBtns.setBackground(new Color(245, 240, 230));
        payBtns.setBorder(new EmptyBorder(12, 16, 14, 16));
        JButton btnBack    = navButton("Quay Lại",         Color.WHITE, NAVY, true);
        JButton btnConfirm = navButton("Xác nhận thanh toán", ORANGE,     Color.WHITE, false);
        btnBack.addActionListener(e -> cardLayout.show(mainPanel,
                orderMap.isEmpty() ? "TableSelection" : "OrderSelection"));
        btnConfirm.addActionListener(e -> handleFinalPayment());
        payBtns.add(btnBack); payBtns.add(btnConfirm);
        rightPanel.add(payBtns, BorderLayout.SOUTH);

        body.add(rightPanel, BorderLayout.CENTER);
        paymentPanel.add(body, BorderLayout.CENTER);
        return paymentPanel;
    }

    /** Tao 1 hang trong bang hoa don (label trai + value phai) */
    private JPanel addBillLine(String label, JLabel valueLabel) {
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BOLD_14); lbl.setForeground(NAVY);
        line.add(lbl,        BorderLayout.WEST);
        line.add(valueLabel, BorderLayout.EAST);
        return line;
    }

    private JLabel makeBillRow(String text, String val) {
        JLabel l = new JLabel(val, SwingConstants.RIGHT);
        l.setFont(FONT_BOLD_14); l.setForeground(Color.decode("#1A252F"));
        return l;
    }

    /** Lam moi trang thanh toan truoc khi hien */
    private void refreshPaymentPanel() {
        // Thong tin ban + khach
        StringBuilder banStr = new StringBuilder();
        for (int i = 0; i < selectedBans.size(); i++) {
            if (i > 0) banStr.append(", ");
            banStr.append("Ban ").append(selectedBans.get(i).getSoBan());
        }
        String custName = (txtCustName != null && !txtCustName.getText().trim().isEmpty())
                ? txtCustName.getText().trim() : "Khách lễ";
        lblPayInfo.setText("  " + banStr + "  |  Khách: " + custName);

        long depositAmt = depositPaid ? DEPOSIT : 0L;
        long foodAmt    = orderMap.entrySet().stream()
                .mapToLong(e -> (long)(e.getKey().getDonGia() * e.getValue())).sum();
        long subtotal   = depositAmt + foodAmt;

        lblPayDeposit.setText(depositAmt > 0
                ? String.format("%,.0f VND", (double) depositAmt) : "Không có");
        lblPayFood.setText(foodAmt > 0
                ? String.format("%,.0f VND", (double) foodAmt) : "Chưa gọi món");
        lblPaySubtotal.setText(String.format("%,.0f VND", (double) subtotal));
        lblPayDiscount.setText("0 VND");
        lblPayFinal.setText(String.format("%,.0f VND", (double) subtotal));

        // Reset voucher
        if (cboVoucher != null) cboVoucher.setSelectedIndex(0);
    }

    private void applyVoucher() {
        int idx = cboVoucher.getSelectedIndex();
        double[] rates = {0, 0.05, 0.08, 0.10};

        long depositAmt = depositPaid ? DEPOSIT : 0L;
        long foodAmt    = orderMap.entrySet().stream()
                .mapToLong(e -> (long)(e.getKey().getDonGia() * e.getValue())).sum();
        long subtotal = depositAmt + foodAmt;

        // Kiem tra dieu kien voucher
        long[] minOrder = {0, 300_000, 450_000, 800_000};
        if (idx > 0 && subtotal < minOrder[idx]) {
            JOptionPane.showMessageDialog(this,
                    "Đơn hàng chưa đạt giá trị tối thiểu cho voucher này!");
            return;
        }

        long discount = (long)(subtotal * rates[idx]);
        long finalAmt = subtotal - discount;

        lblPayDiscount.setText(discount > 0
                ? String.format("-%,.0f VND", (double) discount) : "0 VND");
        lblPayDiscount.setForeground(discount > 0 ? GREEN_SEL : Color.decode("#1A252F"));
        lblPayFinal.setText(String.format("%,.0f VND", (double) finalAmt));
    }

    private void handleFinalPayment() {
        if (selectedBans.isEmpty()) return;
        for (Ban ban : selectedBans)
            ban_dao.updateTinhTrangBan(ban.getMaBan(), TrangThaiBan.Trong);
        JOptionPane.showMessageDialog(this, "Thanh Toán Thành Công.");
        orderMap.clear(); selectedBans.clear(); depositPaid = false;
        loadTables();
        cardLayout.show(mainPanel, "TableSelection");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ════════════════════════════════════════════════════════════════════════

    // form helpers
    private void addSectionTitle(JPanel p, GridBagConstraints g, int row, String text) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 2;
        g.insets = new Insets(18, 12, 4, 12);
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD_16); l.setForeground(NAVY);
        l.setBorder(new MatteBorder(0, 0, 2, 0, GOLD));
        p.add(l, g);
        g.gridwidth = 1; g.insets = new Insets(8, 12, 8, 12);
    }

    private JTextField addDisabledRow(JPanel p, GridBagConstraints g, int row, String label) {
        g.gridy = row; g.gridx = 0;
        p.add(fieldLabel(label), g);
        JTextField f = new JTextField();
        f.setFont(FONT_PLAIN_14);
        f.setBackground(new Color(245, 245, 245));
        f.setForeground(TEXT_GRAY);
        f.setEditable(false);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(6, 10, 6, 10)));
        f.setPreferredSize(new Dimension(300, 38));
        g.gridx = 1; p.add(f, g);
        return f;
    }

    private JTextField addInputRow(JPanel p, GridBagConstraints g, int row, String label) {
        g.gridy = row; g.gridx = 0;
        p.add(fieldLabel(label), g);
        JTextField f = new JTextField();
        f.setFont(FONT_PLAIN_14);
        f.setPreferredSize(new Dimension(300, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 210, 210), 1),
                new EmptyBorder(6, 10, 6, 10)));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ORANGE, 2), new EmptyBorder(6, 10, 6, 10)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(210, 210, 210), 1), new EmptyBorder(6, 10, 6, 10)));
            }
        });
        g.gridx = 1; p.add(f, g);
        return f;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD_14);
        l.setForeground(Color.decode("#1A252F"));
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    // table header with proportional columns using GridBagLayout
    private JPanel buildColHeader(Color bg, Color fg, int[] weights, String[] cols) {
        JPanel h = new JPanel(new GridBagLayout());
        h.setBackground(bg);
        h.setPreferredSize(new Dimension(0, 42));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH; g.gridy = 0;
        for (int i = 0; i < cols.length; i++) {
            g.gridx = i; g.weightx = weights[i];
            JLabel lbl = new JLabel(cols[i], SwingConstants.CENTER);
            lbl.setFont(FONT_BOLD_14); lbl.setForeground(fg);
            lbl.setBorder(new EmptyBorder(0, 6, 0, 6));
            h.add(lbl, g);
        }
        return h;
    }

    // thin horizontal separator
    private JSeparator thinDivider() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(230, 230, 230));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    // rounded circle button
    private JButton circleBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setPreferredSize(new Dimension(32, 32));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // navigation button (outlined or filled)
    private JButton navButton(String text, Color bg, Color fg) {
        return navButton(text, bg, fg, false);
    }

    private JButton navButton(String text, Color bg, Color fg, boolean outlined) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD_14);
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0, 42));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (outlined) b.setBorder(new LineBorder(NAVY, 2));
        else          b.setBorderPainted(false);
        return b;
    }
}