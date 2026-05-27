package gui;

import connectDB.ConnectDB;
import dao.*;
import service.DatBanService;
import entity.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class QuanLyDatBan_DB extends JPanel {

    // ── constants ─────────────────────────────────────────────────────────
    private static final double TIEN_COC    = 500_000.0;
    private static final Color  MAIN_BLUE   = Color.decode("#0B3D59");
    private static final Color  GREEN_OK    = Color.decode("#27AE60");
    private static final Color  RED_DANG    = Color.decode("#E74C3C");
    private static final Color  BG_LIGHT    = Color.decode("#F0F2F5");
    private static final Color  TEXT_DARK   = Color.decode("#2C3E50");
    private static final Color  BORDER_CLR  = Color.decode("#DDE1E7");
    private static final DecimalFormat FMT  = new DecimalFormat("#,###");

    // ── DAOs ──────────────────────────────────────────────────────────────
    private final Ban_DAO            banDAO  = new Ban_DAO();
    private final KhachHang_DAO      khDAO   = new KhachHang_DAO();
    private final DonDatBan_DAO      ddbDAO  = new DonDatBan_DAO();
    private final HoaDon_DAO         hdDAO   = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO  cthdDAO = new ChiTietHoaDon_DAO();
    private final SanPham_DAO        spDAO   = new SanPham_DAO();
    private final ChiTietDatBan_DAO  ctdbDAO = new ChiTietDatBan_DAO();

    // ── Service ───────────────────────────────────────────────────────────
    private final DatBanService datBanService = new DatBanService();

    // ── state ─────────────────────────────────────────────────────────────
    private final NhanVien      currentNV;
    private final List<Ban>     selectedBans;
    private final String        selectedTime;
    private final Date          selectedBookingDate;
    private final BookingListener listener;
    /** true = khách ăn ngay, false = đặt trước */
    private final boolean       walkIn;

    // booking widgets
    private JLabel              lblBookingTitle, lblSlotDisplay, lblRoiDisplay;
    private JTextField          txtTenKH, txtSdtKH, txtGhiChu;
    private JPanel              pCartRows;
    private JLabel              lblCartTotal;
    private Map<String,Integer> bookingCart = new LinkedHashMap<>();

    // ── Callback ──────────────────────────────────────────────────────────
    public interface BookingListener {
        void onBookingSuccess();
        void onCancel();
    }

    // ── Constructor: đặt trước (walkIn = false) ───────────────────────────
    public QuanLyDatBan_DB(NhanVien nhanVien, List<Ban> bans, Date bookingDate,
                            String selectedTime, BookingListener listener) {
        this(nhanVien, bans, bookingDate, selectedTime, false, listener);
    }

    // ── Constructor chính ─────────────────────────────────────────────────
    public QuanLyDatBan_DB(NhanVien nhanVien, List<Ban> bans, Date bookingDate,
                            String selectedTime, boolean walkIn, BookingListener listener) {
        this.currentNV          = nhanVien;
        this.selectedBans       = bans != null ? bans : new ArrayList<>();
        this.selectedBookingDate = bookingDate;
        this.walkIn             = walkIn;
        this.listener           = listener;
        this.selectedTime       = walkIn
                ? new SimpleDateFormat("HH:mm").format(new Date())
                : (selectedTime != null && !selectedTime.isEmpty() ? selectedTime : "11:00");

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        buildUI();
    }

    // ── UI ────────────────────────────────────────────────────────────────
    private void buildUI() {
        // ── Header ────────────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(walkIn ? new Color(232, 245, 233) : new Color(245, 247, 250));
        hdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_CLR),
                new EmptyBorder(10, 14, 10, 14)));

        String tenBan = selectedBans.stream()
                .map(b -> "Bàn " + b.getSoBan())
                .collect(java.util.stream.Collectors.joining(", "));
        int tongSucChua = selectedBans.stream().mapToInt(Ban::getSucChua).sum();
        String loai = selectedBans.size() == 1 && selectedBans.get(0).getLoaiBan() != null
                ? selectedBans.get(0).getLoaiBan()
                : (selectedBans.size() > 1 ? "Nhiều bàn" : "Thường");

        String titleText = walkIn
                ? "ĂN NGAY  ·  " + tenBan + "  ·  " + loai + "  ·  " + tongSucChua + " người"
                : tenBan + "  ·  " + loai + "  ·  " + tongSucChua + " người";
        lblBookingTitle = new JLabel(titleText);
        lblBookingTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBookingTitle.setForeground(walkIn ? GREEN_OK : MAIN_BLUE);
        hdr.add(lblBookingTitle, BorderLayout.WEST);

        JLabel sub = walkIn
                ? new JLabel("| Không cần cọc  ·  Ngồi & ăn ngay")
                : new JLabel("| Tiền cọc bắt buộc: " + FMT.format(TIEN_COC) + "đ");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sub.setForeground(walkIn ? GREEN_OK : RED_DANG);
        hdr.add(sub, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── Body ──────────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Thông tin giờ
        body.add(sectionLabel(walkIn ? "THỜI GIAN PHỤC VỤ" : "THÔNG TIN ĐẶT BÀN"));
        int minsForDisplay;
        if      (tongSucChua <= 2) minsForDisplay = 90;
        else if (tongSucChua <= 4) minsForDisplay = 120;
        else if (tongSucChua <= 8) minsForDisplay = 180;
        else                       minsForDisplay = 240;
        Date tgDenDisplay = walkIn ? new Date()
                : buildBookingDateTime(selectedBookingDate, selectedTime);
        Date tgRoiDisplay = new Date(tgDenDisplay.getTime() + (long) minsForDisplay * 60_000);
        String roiStr = new SimpleDateFormat("HH:mm").format(tgRoiDisplay);

        String slotText = walkIn
                ? "  Ngay bây giờ: " + selectedTime + "     Dự kiến rời: " + roiStr
                : "  Ngày: " + new SimpleDateFormat("dd/MM/yyyy").format(selectedBookingDate)
                        + "     Giờ đến: " + selectedTime + "     Dự kiến rời: " + roiStr;

        lblSlotDisplay = new JLabel(slotText);
        lblSlotDisplay.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSlotDisplay.setForeground(walkIn ? GREEN_OK : MAIN_BLUE);
        lblSlotDisplay.setBorder(new CompoundBorder(
                new LineBorder(walkIn ? new Color(150, 220, 160) : new Color(180, 210, 240), 1),
                new EmptyBorder(7, 12, 7, 12)));
        lblSlotDisplay.setOpaque(true);
        lblSlotDisplay.setBackground(walkIn ? new Color(232, 255, 232) : new Color(236, 244, 255));
        lblSlotDisplay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        lblSlotDisplay.setAlignmentX(0f);
        body.add(lblSlotDisplay);
        body.add(Box.createVerticalStrut(8));
        body.add(hsep());

        // Thông tin khách hàng
        body.add(sectionLabel("THÔNG TIN KHÁCH HÀNG"));
        String tenLabel = walkIn ? "Tên khách  (Tùy chọn)" : "Tên khách  *";
        String sdtLabel = walkIn ? "Số điện thoại  (Tùy chọn)" : "Số điện thoại  *";
        body.add(fieldRow(tenLabel, txtTenKH = inputField()));
        body.add(Box.createVerticalStrut(6));
        body.add(fieldRow(sdtLabel, txtSdtKH = inputField()));
        body.add(Box.createVerticalStrut(6));
        body.add(fieldRow("Ghi chú", txtGhiChu = inputField()));
        body.add(Box.createVerticalStrut(4));
        body.add(hsep());

        txtSdtKH.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                String sdt = txtSdtKH.getText().trim();
                if (!sdt.isEmpty() && txtTenKH.getText().trim().isEmpty()) {
                    KhachHang kh = khDAO.getKhachHangBySdt(sdt);
                    if (kh != null) txtTenKH.setText(kh.getTenKH());
                }
            }
        });

        // Gọi món trước
        JButton btnPreorder = new JButton("GỌI MÓN TRƯỚC  (Tùy chọn)");
        btnPreorder.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPreorder.setBackground(new Color(236, 244, 255));
        btnPreorder.setForeground(MAIN_BLUE);
        btnPreorder.setFocusPainted(false);
        btnPreorder.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnPreorder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnPreorder.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPreorder.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPreorder.addActionListener(e -> {
            new PreorderDishDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    spDAO, bookingCart, updatedCart -> {
                bookingCart.clear();
                bookingCart.putAll(updatedCart);
                refreshCartPanel();
            }).setVisible(true);
        });
        body.add(btnPreorder);
        body.add(Box.createVerticalStrut(4));

        pCartRows = new JPanel(new GridBagLayout());
        pCartRows.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(pCartRows);

        lblCartTotal = new JLabel();
        lblCartTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCartTotal.setForeground(RED_DANG);
        lblCartTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCartTotal.setBorder(new EmptyBorder(3, 4, 6, 4));
        lblCartTotal.setVisible(false);
        body.add(lblCartTotal);
        body.add(hsep());

        refreshCartPanel();

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(14);
        add(bodyScroll, BorderLayout.CENTER);

        // ── Button row ────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(10, 14, 10, 14)));
        btnRow.setBackground(Color.WHITE);

        JButton bHuy = actionBtn("HỦY", Color.WHITE, TEXT_DARK, true);
        Color okColor = walkIn ? GREEN_OK : MAIN_BLUE;
        String okLabel = walkIn ? "NHẬN BÀN" : "XÁC NHẬN ĐẶT BÀN";
        JButton bOk = actionBtn(okLabel, okColor, Color.WHITE, false);

        bHuy.addActionListener(e -> listener.onCancel());
        bOk.addActionListener(e -> doConfirmBooking());

        btnRow.add(bHuy);
        btnRow.add(bOk);
        add(btnRow, BorderLayout.SOUTH);
    }

    // ── Cart refresh ──────────────────────────────────────────────────────
    // Header + tất cả data rows dùng CÙNG 1 GridBagLayout → cột tự đều nhau
    private void refreshCartPanel() {
        if (pCartRows == null) return;
        pCartRows.removeAll();
        pCartRows.setBackground(new Color(240, 244, 250)); // nền header

        Font hf = new Font("Segoe UI", Font.BOLD, 13);
        Font df = new Font("Segoe UI", Font.PLAIN, 14);
        Color hc = new Color(80, 100, 130);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;

        // ── Header (gridy=0) ──────────────────────────────────────────────
        g.gridy = 0;
        g.gridx = 0; g.weightx = 1.0; g.insets = new Insets(5, 6, 5, 2);
        JLabel h1 = new JLabel("Tên món"); h1.setFont(hf); h1.setForeground(hc);
        pCartRows.add(h1, g);

        g.gridx = 1; g.weightx = 0; g.insets = new Insets(5, 2, 5, 2);
        JLabel h2 = new JLabel("Số lượng", SwingConstants.CENTER);
        h2.setFont(hf); h2.setForeground(hc);
        h2.setPreferredSize(new Dimension(90, 22)); h2.setMinimumSize(new Dimension(90, 22));
        pCartRows.add(h2, g);

        g.gridx = 2; g.insets = new Insets(5, 2, 5, 2);
        JLabel h3 = new JLabel("Đơn giá", SwingConstants.RIGHT);
        h3.setFont(hf); h3.setForeground(hc);
        h3.setPreferredSize(new Dimension(85, 22)); h3.setMinimumSize(new Dimension(85, 22));
        pCartRows.add(h3, g);

        g.gridx = 3; g.insets = new Insets(5, 2, 5, 6);
        JLabel h4 = new JLabel("Thành tiền", SwingConstants.RIGHT);
        h4.setFont(hf); h4.setForeground(hc);
        h4.setPreferredSize(new Dimension(95, 22)); h4.setMinimumSize(new Dimension(95, 22));
        pCartRows.add(h4, g);

        // ── Separator (gridy=1) ───────────────────────────────────────────
        g.gridy = 1; g.gridx = 0; g.gridwidth = 4; g.weightx = 1.0; g.insets = new Insets(0, 0, 0, 0);
        JSeparator sep = new JSeparator(); sep.setForeground(BORDER_CLR);
        pCartRows.add(sep, g);
        g.gridwidth = 1;

        int lastRow;

        if (bookingCart.isEmpty()) {
            g.gridy = 2; g.gridx = 0; g.gridwidth = 4; g.weightx = 1.0; g.insets = new Insets(6, 6, 6, 6);
            JLabel empty = new JLabel("Chưa gọi món trước");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(new Color(130, 130, 130));
            empty.setOpaque(true); empty.setBackground(Color.WHITE);
            pCartRows.add(empty, g);
            g.gridwidth = 1;
            lastRow = 3;
            if (lblCartTotal != null) lblCartTotal.setVisible(false);
        } else {
            List<SanPham> allSP = spDAO.getAllSanPham();
            Map<String, SanPham> spMap = new HashMap<>();
            for (SanPham sp : allSP) spMap.put(sp.getMaMon(), sp);
            double total = 0;
            int idx = 0;

            for (Map.Entry<String, Integer> entry : bookingCart.entrySet()) {
                SanPham sp = spMap.get(entry.getKey());
                if (sp == null) continue;
                int qty = entry.getValue();
                total += sp.getGiaBan() * qty;
                int r = 2 + idx;

                // Name
                g.gridy = r; g.gridx = 0; g.weightx = 1.0; g.insets = new Insets(3, 6, 3, 2);
                JLabel lName = new JLabel(sp.getTenMon()); lName.setFont(df);
                lName.setOpaque(true); lName.setBackground(Color.WHITE);
                pCartRows.add(lName, g);

                // Qty [−] n [+]
                g.gridx = 1; g.weightx = 0; g.insets = new Insets(2, 2, 2, 2);
                JPanel qPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
                qPanel.setBackground(Color.WHITE);
                qPanel.setPreferredSize(new Dimension(90, 28)); qPanel.setMinimumSize(new Dimension(90, 28));
                JButton btnMinus = cartQtyBtn("−");
                JLabel lblQty = new JLabel(String.valueOf(qty), SwingConstants.CENTER);
                lblQty.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblQty.setPreferredSize(new Dimension(28, 24));
                JButton btnPlus = cartQtyBtn("+");
                final String maMon = sp.getMaMon();
                btnPlus.addActionListener(e -> {
                    bookingCart.put(maMon, bookingCart.getOrDefault(maMon, 0) + 1);
                    refreshCartPanel();
                });
                btnMinus.addActionListener(e -> {
                    int cur = bookingCart.getOrDefault(maMon, 0);
                    if (cur <= 1) bookingCart.remove(maMon);
                    else bookingCart.put(maMon, cur - 1);
                    refreshCartPanel();
                });
                qPanel.add(btnMinus); qPanel.add(lblQty); qPanel.add(btnPlus);
                pCartRows.add(qPanel, g);

                // Unit price
                g.gridx = 2; g.insets = new Insets(3, 2, 3, 2);
                JLabel lDG = new JLabel(FMT.format(sp.getGiaBan()) + "đ", SwingConstants.RIGHT);
                lDG.setFont(df);
                lDG.setPreferredSize(new Dimension(85, 24)); lDG.setMinimumSize(new Dimension(85, 24));
                lDG.setOpaque(true); lDG.setBackground(Color.WHITE);
                pCartRows.add(lDG, g);

                // Total
                g.gridx = 3; g.insets = new Insets(3, 2, 3, 6);
                JLabel lTT = new JLabel(FMT.format(sp.getGiaBan() * qty) + "đ", SwingConstants.RIGHT);
                lTT.setFont(new Font("Segoe UI", Font.BOLD, 14)); lTT.setForeground(RED_DANG);
                lTT.setPreferredSize(new Dimension(95, 24)); lTT.setMinimumSize(new Dimension(95, 24));
                lTT.setOpaque(true); lTT.setBackground(Color.WHITE);
                pCartRows.add(lTT, g);

                idx++;
            }
            lastRow = 2 + idx;
            if (lblCartTotal != null) {
                lblCartTotal.setText("  Tổng gọi trước: " + FMT.format(total) + "đ");
                lblCartTotal.setVisible(true);
            }
        }

        // Filler row: đẩy nội dung lên trên, không để GridBagLayout dàn đều theo chiều dọc
        g.gridy = lastRow; g.gridx = 0; g.gridwidth = 4;
        g.weightx = 1.0; g.weighty = 1.0; g.fill = GridBagConstraints.BOTH; g.insets = new Insets(0, 0, 0, 0);
        pCartRows.add(new JLabel(), g);

        pCartRows.revalidate();
        pCartRows.repaint();
        if (pCartRows.getParent() != null) pCartRows.getParent().revalidate();
    }

    private JButton cartQtyBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setPreferredSize(new Dimension(26, 26));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setFocusPainted(false);
        b.setBackground(BG_LIGHT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Entry point ───────────────────────────────────────────────────────
    private void doConfirmBooking() {
        if (selectedBans.isEmpty()) { msg("Không có bàn nào được chọn!"); return; }
        if (walkIn) {
            doWalkIn();
        } else {
            doReservation();
        }
    }

    // ── Luồng đặt trước ───────────────────────────────────────────────────
    private void doReservation() {
        DatBanService.DatBanRequest req = new DatBanService.DatBanRequest(
                selectedBans, selectedBookingDate, selectedTime,
                txtTenKH.getText().trim(), txtSdtKH.getText().trim(),
                txtGhiChu.getText().trim(), bookingCart);
        try {
            HoaDon hd = datBanService.datBanTruoc(req, currentNV);

            int tongSucChua = selectedBans.stream().mapToInt(Ban::getSucChua).sum();
            int mins = DatBanService.tinhGioRoiDuKien(tongSucChua);
            Date tgDen = DatBanService.buildDateTime(selectedBookingDate, selectedTime);
            Date tgRoi = new Date(tgDen.getTime() + (long) mins * 60_000);
            String tenBan = selectedBans.stream()
                    .map(b -> "Bàn " + b.getSoBan())
                    .collect(java.util.stream.Collectors.joining(", "));

            JOptionPane.showMessageDialog(this,
                    "Đặt bàn thành công!\nBàn: " + tenBan
                            + "\nNgày: " + new SimpleDateFormat("dd/MM/yyyy").format(selectedBookingDate)
                            + "\nGiờ đến: " + selectedTime
                            + "\nDự kiến rời: " + new SimpleDateFormat("HH:mm").format(tgRoi)
                            + "\nTiền cọc: " + FMT.format(TIEN_COC) + "đ",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            listener.onBookingSuccess();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            msg(ex.getMessage());
        } catch (Exception ex) {
            msg("Lỗi hệ thống khi lưu đặt bàn, vui lòng thử lại!\n" + ex.getMessage());
        }
    }

    // ── Luồng ăn ngay ─────────────────────────────────────────────────────
    private void doWalkIn() {
        DatBanService.DatBanRequest req = new DatBanService.DatBanRequest(
                selectedBans, new Date(), selectedTime,
                txtTenKH.getText().trim(), txtSdtKH.getText().trim(),
                txtGhiChu.getText().trim(), bookingCart);
        try {
            HoaDon hd = datBanService.anNgay(req, currentNV);

            int tongSucChua = selectedBans.stream().mapToInt(Ban::getSucChua).sum();
            int mins = DatBanService.tinhGioRoiDuKien(tongSucChua);
            Date tgRoi = new Date(System.currentTimeMillis() + (long) mins * 60_000);
            String tenBan = selectedBans.stream()
                    .map(b -> "Bàn " + b.getSoBan())
                    .collect(java.util.stream.Collectors.joining(", "));
            String tenKhach = hd.getKhachHang() != null ? hd.getKhachHang().getTenKH() : "Khách vãng lai";

            JOptionPane.showMessageDialog(this,
                    "Nhận bàn thành công!\n"
                            + "Bàn: " + tenBan + "\n"
                            + "Khách: " + tenKhach + "\n"
                            + "Giờ vào: " + selectedTime + "\n"
                            + "Dự kiến rời: " + new SimpleDateFormat("HH:mm").format(tgRoi),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            listener.onBookingSuccess();
        } catch (IllegalArgumentException ex) {
            msg(ex.getMessage());
        } catch (Exception ex) {
            msg("Lỗi hệ thống khi nhận bàn, vui lòng thử lại!\n" + ex.getMessage());
        }
    }

    // ── Time helpers (delegate sang DatBanService) ────────────────────────
    private static boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        Calendar c1 = Calendar.getInstance(); c1.setTime(d1);
        Calendar c2 = Calendar.getInstance(); c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
    private static boolean isToday(Date d) { return DatBanService.isToday(d); }
    private static Date buildBookingDateTime(Date date, String timeStr) {
        return DatBanService.buildDateTime(date, timeStr);
    }

    // ── UI helpers ────────────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(100, 120, 150));
        l.setAlignmentX(0f);
        l.setBorder(new EmptyBorder(7, 0, 4, 0));
        return l;
    }
    private JSeparator hsep() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setForeground(BORDER_CLR);
        return s;
    }
    private JPanel fieldRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        lbl.setPreferredSize(new Dimension(190, 24));
        row.add(lbl, BorderLayout.WEST);
        if (field != null) row.add(field, BorderLayout.CENTER);
        return row;
    }
    private JTextField inputField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1), new EmptyBorder(3, 8, 3, 8)));
        return f;
    }
    private JButton actionBtn(String text, Color bg, Color fg, boolean outlined) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
        b.setBorder(outlined ? new LineBorder(BORDER_CLR) : new EmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void msg(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }


    // ── PreorderDishDialog ────────────────────────────────────────────────
    interface CartCallback { void onConfirm(Map<String,Integer> cart); }

    private static class PreorderDishDialog extends JDialog {
        private final SanPham_DAO spDAO;
        private final Map<String,Integer> initialCart;
        private final CartCallback callback;
        private final Map<String,Integer> cart = new LinkedHashMap<>();
        private DefaultTableModel tmCart;
        private JLabel lblTotal;

        PreorderDishDialog(Frame owner, SanPham_DAO spDAO,
                           Map<String,Integer> initialCart, CartCallback callback) {
            super(owner, "Gọi món trước", true);
            this.spDAO = spDAO; this.initialCart = initialCart; this.callback = callback;
            cart.putAll(initialCart);
            setSize(880, 580);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            build();
        }

        private void build() {
            List<SanPham> allSP = spDAO.getAllSanPham();
            Map<String, List<SanPham>> byLoai = new LinkedHashMap<>();
            for (SanPham sp : allSP) {
                if (!sp.isTrangThai()) continue;
                String loai = (sp.getLoaiSanPham() != null) ? sp.getLoaiSanPham().getTenLoai() : "Khác";
                byLoai.computeIfAbsent(loai, k -> new ArrayList<>()).add(sp);
            }

            JPanel catPanel = new JPanel(new BorderLayout(0, 6));
            catPanel.setPreferredSize(new Dimension(190, 0));
            catPanel.setBackground(BG_LIGHT);
            catPanel.setBorder(new EmptyBorder(8, 8, 8, 4));
            JLabel catTitle = new JLabel("Danh mục");
            catTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            catPanel.add(catTitle, BorderLayout.NORTH);

            JPanel dishGrid = new JPanel(new GridLayout(0, 3, 8, 8));
            dishGrid.setBackground(Color.WHITE);
            dishGrid.setBorder(new EmptyBorder(6, 6, 6, 6));

            JPanel catList = new JPanel(new GridLayout(0, 1, 0, 4));
            catList.setOpaque(false);
            ButtonGroup bg = new ButtonGroup();
            String[] first = {byLoai.keySet().stream().findFirst().orElse(null)};
            for (String cat : byLoai.keySet()) {
                JToggleButton tb = new JToggleButton(cat) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(isSelected() ? MAIN_BLUE : new Color(210, 234, 255));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.dispose();
                        setForeground(isSelected() ? Color.WHITE : TEXT_DARK);
                        super.paintComponent(g);
                    }
                };
                tb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                tb.setFocusPainted(false); tb.setContentAreaFilled(false);
                tb.setOpaque(false); tb.setBorderPainted(false);
                tb.setBorder(new EmptyBorder(6, 10, 6, 10));
                if (cat.equals(first[0])) tb.setSelected(true);
                bg.add(tb); catList.add(tb);
                tb.addActionListener(e -> {
                    dishGrid.removeAll();
                    for (SanPham sp : byLoai.getOrDefault(cat, new ArrayList<>()))
                        dishGrid.add(buildDishCard(sp, allSP));
                    dishGrid.revalidate(); dishGrid.repaint();
                });
            }
            catPanel.add(new JScrollPane(catList), BorderLayout.CENTER);
            if (first[0] != null)
                for (SanPham sp : byLoai.getOrDefault(first[0], new ArrayList<>()))
                    dishGrid.add(buildDishCard(sp, allSP));

            JScrollPane dishScroll = new JScrollPane(dishGrid);

            tmCart = new DefaultTableModel(
                    new String[]{"Tên món", "SL", "Đơn giá", "Thành tiền"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable tCart = new JTable(tmCart);
            styleTableStatic(tCart);
            tCart.setPreferredScrollableViewportSize(new Dimension(0, 100));

            lblTotal = new JLabel("Tổng gọi món: 0đ");
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTotal.setForeground(RED_DANG);

            JButton bCancel = btnStatic("Hủy",     Color.WHITE, TEXT_DARK, true);
            JButton bOk     = btnStatic("XÁC NHẬN", MAIN_BLUE, Color.WHITE, false);
            bCancel.addActionListener(e -> dispose());
            bOk.addActionListener(e -> { callback.onConfirm(new LinkedHashMap<>(cart)); dispose(); });

            JPanel southBottom = new JPanel(new BorderLayout(8, 0));
            southBottom.setOpaque(false);
            southBottom.add(lblTotal, BorderLayout.WEST);
            JPanel bRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            bRow.setOpaque(false); bRow.add(bCancel); bRow.add(bOk);
            southBottom.add(bRow, BorderLayout.EAST);

            JPanel south = new JPanel(new BorderLayout(0, 6));
            south.setBorder(new EmptyBorder(6, 8, 8, 8));
            south.setBackground(Color.WHITE);
            south.add(new JScrollPane(tCart), BorderLayout.CENTER);
            south.add(southBottom, BorderLayout.SOUTH);

            JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catPanel, dishScroll);
            topSplit.setDividerLocation(190); topSplit.setDividerSize(4); topSplit.setBorder(null);
            JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, south);
            mainSplit.setDividerLocation(330); mainSplit.setDividerSize(4); mainSplit.setBorder(null);
            add(mainSplit, BorderLayout.CENTER);

            refreshCart(allSP);
        }

        private JPanel buildDishCard(SanPham sp, List<SanPham> allSP) {
            RoundedPanel card = new RoundedPanel(10, Color.WHITE);
            card.setLayout(new BorderLayout(4, 4));
            card.setBorder(new CompoundBorder(new LineBorder(BORDER_CLR, 1), new EmptyBorder(7, 8, 7, 8)));
            JLabel name = new JLabel("<html><b>" + sp.getTenMon() + "</b><br>"
                    + "<font color='#E74C3C'>" + FMT.format(sp.getGiaBan()) + "đ</font></html>");
            name.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            card.add(name, BorderLayout.CENTER);

            JPanel qr = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
            qr.setOpaque(false);
            JButton minus = qtyBtnS("−");
            JLabel cnt = new JLabel(String.valueOf(cart.getOrDefault(sp.getMaMon(), 0)));
            cnt.setFont(new Font("Segoe UI", Font.BOLD, 14));
            cnt.setPreferredSize(new Dimension(26, 24));
            cnt.setHorizontalAlignment(SwingConstants.CENTER);
            JButton plus = qtyBtnS("+");
            plus.addActionListener(e -> {
                int q = cart.getOrDefault(sp.getMaMon(), 0) + 1;
                cart.put(sp.getMaMon(), q); cnt.setText(String.valueOf(q)); refreshCart(allSP);
            });
            minus.addActionListener(e -> {
                int q = cart.getOrDefault(sp.getMaMon(), 0);
                if (q > 0) {
                    if (--q == 0) cart.remove(sp.getMaMon()); else cart.put(sp.getMaMon(), q);
                    cnt.setText(String.valueOf(q)); refreshCart(allSP);
                }
            });
            qr.add(minus); qr.add(cnt); qr.add(plus);
            card.add(qr, BorderLayout.SOUTH);
            return card;
        }

        private void refreshCart(List<SanPham> allSP) {
            if (tmCart == null) return;
            tmCart.setRowCount(0);
            double total = 0;
            Map<String,SanPham> map = new HashMap<>();
            for (SanPham sp : allSP) map.put(sp.getMaMon(), sp);
            for (Map.Entry<String,Integer> e : cart.entrySet()) {
                SanPham sp = map.get(e.getKey()); if (sp == null) continue;
                double tt = sp.getGiaBan() * e.getValue();
                tmCart.addRow(new Object[]{
                        sp.getTenMon(), e.getValue(),
                        FMT.format(sp.getGiaBan()) + "đ",
                        FMT.format(tt) + "đ"
                });
                total += tt;
            }
            if (lblTotal != null) lblTotal.setText("Tổng gọi món: " + FMT.format(total) + "đ");
        }

        private static void styleTableStatic(JTable t) {
            t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
            t.setRowHeight(28); t.setShowGrid(false);
            t.setIntercellSpacing(new Dimension(0, 0));
            t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
        private static JButton btnStatic(String text, Color bg, Color fg, boolean outlined) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(160, 36));
            b.setBorder(outlined
                    ? new CompoundBorder(new LineBorder(BORDER_CLR), new EmptyBorder(7, 24, 7, 24))
                    : new EmptyBorder(7, 24, 7, 24));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }
        private static JButton qtyBtnS(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Segoe UI", Font.BOLD, 15));
            b.setPreferredSize(new Dimension(28, 28));
            b.setMargin(new Insets(0, 0, 0, 0)); b.setFocusPainted(false);
            b.setBackground(BG_LIGHT);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fillColor;
        RoundedPanel(int radius, Color fill) {
            this.radius = radius; this.fillColor = fill; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
