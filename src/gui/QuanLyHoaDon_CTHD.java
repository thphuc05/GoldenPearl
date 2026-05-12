package gui;

import entity.ChiTietHoaDon;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class QuanLyHoaDon_CTHD extends JDialog {
    private static final Color S_TEXT_DARK = Color.decode("#333333");
    private static final java.text.SimpleDateFormat S_DATETIME_SDF =
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final Color R_NAVY  = Color.decode("#0B3D59");
    private final Color R_GOLD  = Color.decode("#C5A059");
    private final Color R_LINE  = new Color(220, 220, 220);
    private final Color R_GREEN = Color.decode("#27AE60");
    private final Color R_BG    = Color.WHITE;

    private JPanel receiptPanel;

    // ── Constructor đầy đủ (có tenBan) ───────────────────────────────────────
    public QuanLyHoaDon_CTHD(java.awt.Window parent, HoaDon hd,
                             java.util.List<ChiTietHoaDon> dsCT,
                             boolean isDetail, String tenBan) {
        super(parent, isDetail ? "Chi Tiết Hóa Đơn" : "Phiếu Thanh Toán",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setSize(460, 648);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        receiptPanel = buildReceipt(hd, dsCT, isDetail, tenBan != null ? tenBan : "");

        JScrollPane scroll = new JScrollPane(receiptPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(240, 242, 245));
        add(scroll, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bar.setBackground(R_BG);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, R_LINE));

        if (isDetail) {
            JButton btnClose = mkDialogBtn("ĐÓNG", Color.WHITE, S_TEXT_DARK);
            btnClose.setBorder(BorderFactory.createLineBorder(R_LINE));
            btnClose.addActionListener(e -> dispose());
            bar.add(btnClose);
        } else {
            JButton btnCancel = mkDialogBtn("HỦY", Color.WHITE, S_TEXT_DARK);
            btnCancel.setBorder(BorderFactory.createLineBorder(R_LINE));
            btnCancel.addActionListener(e -> dispose());
            JButton btnConfirm = mkDialogBtn("XÁC NHẬN", R_NAVY, Color.WHITE);
            btnConfirm.addActionListener(e -> {
                JOptionPane.showMessageDialog(QuanLyHoaDon_CTHD.this,
                        "In hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            });
            bar.add(btnCancel);
            bar.add(btnConfirm);
        }
        add(bar, BorderLayout.SOUTH);
    }

    // ── Overload tương thích ngược (không có tenBan) ──────────────────────────
    // Dùng cho QuanLyKhachHang_LSDB và bất kỳ chỗ nào chưa truyền tenBan
    public QuanLyHoaDon_CTHD(java.awt.Window parent, HoaDon hd,
                             java.util.List<ChiTietHoaDon> dsCT, boolean isDetail) {
        this(parent, hd, dsCT, isDetail, "");
    }

    private JPanel buildReceipt(HoaDon hd, List<ChiTietHoaDon> dsCT,
                                boolean isDetail, String tenBan) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(R_BG);
        p.setBorder(new EmptyBorder(10, 24, 10, 24));

        DecimalFormat numFmt = new DecimalFormat("#,###");

        // ── Restaurant header ─────────────────────────────────────────
        p.add(mkCenterLbl("GOLDEN PEARL", 23, R_NAVY, Font.BOLD));
        p.add(Box.createVerticalStrut(2));
        p.add(mkCenterLbl("36 Thích Bửu Đăng, P.Hạnh Thông", 10, new Color(120, 120, 120), Font.PLAIN));
        p.add(mkCenterLbl("Thành phố Hồ Chí Minh", 10, new Color(120, 120, 120), Font.PLAIN));
        p.add(Box.createVerticalStrut(7));
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(5));

        // ── Receipt type ──────────────────────────────────────────────
        p.add(mkCenterLbl(isDetail ? "CHI TIẾT HÓA ĐƠN" : "PHIẾU THANH TOÁN", 13, R_NAVY, Font.BOLD));
        p.add(Box.createVerticalStrut(5));
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(7));

        // ── Invoice info ──────────────────────────────────────────────
        p.add(mkInfoRow("Mã hóa đơn", hd.getMaHD()));
        p.add(Box.createVerticalStrut(3));
        p.add(mkInfoRow("Ngày lập", hd.getNgayLap() != null ? S_DATETIME_SDF.format(hd.getNgayLap()) : "—"));
        p.add(Box.createVerticalStrut(3));
        String nvText = hd.getNhanVien() != null
                ? hd.getNhanVien().getMaNV() + "  –  " + hd.getNhanVien().getTenNV() : "—";
        p.add(mkInfoRow("Nhân viên", nvText));
        p.add(Box.createVerticalStrut(3));
        String khText;
        if (hd.getKhachHang() == null) {
            khText = "Khách vãng lai";
        } else {
            khText = hd.getKhachHang().getMaKH();
            String ten = hd.getKhachHang().getTenKH();
            if (ten != null && !ten.isEmpty()) khText += "  –  " + ten;
        }
        p.add(mkInfoRow("Khách hàng", khText));
        p.add(Box.createVerticalStrut(3));

        // ── [MỚI] Số bàn ─────────────────────────────────────────────
        String banDisplay = (tenBan != null && !tenBan.isEmpty()) ? tenBan : "—";
        p.add(mkInfoRow("Bàn", banDisplay));
        p.add(Box.createVerticalStrut(8));
        // ─────────────────────────────────────────────────────────────
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(5));

        // ── Items header ──────────────────────────────────────────────
        p.add(mkItemsHeader());
        p.add(mkSolidLine(R_NAVY));

        // ── Item rows ─────────────────────────────────────────────────
        double tongTienMon = 0;

        if (dsCT != null && !dsCT.isEmpty()) {
            int idx = 1;
            for (ChiTietHoaDon ct : dsCT) {
                String name = ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "";
                double thanhTienThucTe = ct.getDonGia() * ct.getSoLuong();
                tongTienMon += thanhTienThucTe;
                p.add(mkItemRow(idx, name, ct.getSoLuong(),
                        numFmt.format(ct.getDonGia()) + "đ",
                        numFmt.format(thanhTienThucTe) + "đ",
                        idx % 2 == 0));
                idx++;
            }
        } else {
            p.add(mkCenterLbl("(Không có chi tiết món)", 11, new Color(160, 160, 160), Font.ITALIC));
        }

        p.add(Box.createVerticalStrut(4));
        p.add(mkSolidLine(R_LINE));
        p.add(Box.createVerticalStrut(8));

        // ── Total ─────────────────────────────────────────────────────
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel kLbl = new JLabel("Tổng tiền");
        kLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        kLbl.setForeground(R_NAVY);
        JLabel vLbl = new JLabel(numFmt.format(tongTienMon) + "đ", SwingConstants.RIGHT);
        vLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        vLbl.setForeground(new Color(60, 60, 60));
        totalRow.add(kLbl, BorderLayout.WEST);
        totalRow.add(vLbl, BorderLayout.EAST);
        p.add(totalRow);
        p.add(Box.createVerticalStrut(3));

        // ── Tiền cọc ──────────────────────────────────────────────────
        double coc = hd.getTienCoc();
        JPanel cocRow = new JPanel(new BorderLayout());
        cocRow.setOpaque(false);
        cocRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel cocK = new JLabel("Tiền cọc");
        cocK.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cocK.setForeground(R_NAVY);
        JLabel cocV = new JLabel("-" + numFmt.format(coc) + "đ", SwingConstants.RIGHT);
        cocV.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cocV.setForeground(new Color(100, 100, 100));
        cocRow.add(cocK, BorderLayout.WEST);
        cocRow.add(cocV, BorderLayout.EAST);
        p.add(cocRow);
        p.add(Box.createVerticalStrut(5));
        p.add(mkSolidLine(R_LINE));
        p.add(Box.createVerticalStrut(5));

        // ── Tổng cộng ─────────────────────────────────────────────────
        double tongCong = tongTienMon - coc;
        JPanel tongCongRow = new JPanel(new BorderLayout());
        tongCongRow.setOpaque(false);
        tongCongRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel tcK = new JLabel();
        tcK.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tcK.setForeground(R_NAVY);
        JLabel tcV = new JLabel("", SwingConstants.RIGHT);
        tcV.setFont(new Font("Segoe UI", Font.BOLD, 16));
        if (tongCong < 0) {
            tcK.setText("Số Tiền Hoàn Lại");
            tcV.setText(numFmt.format(Math.abs(tongCong)) + "đ");
            tcV.setForeground(Color.decode("#E74C3C"));
        } else {
            tcK.setText("Số Tiền Thanh Toán");
            tcV.setText(numFmt.format(tongCong) + "đ");
            tcV.setForeground(R_GOLD);
        }
        tongCongRow.add(tcK, BorderLayout.WEST);
        tongCongRow.add(tcV, BorderLayout.EAST);
        p.add(tongCongRow);
        p.add(Box.createVerticalStrut(7));

        // ── Status badge ──────────────────────────────────────────────
        boolean paid = hd.isTrangThai();
        if (isDetail) {
            JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            statusWrap.setOpaque(false);
            statusWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            JLabel sLbl = new JLabel(paid ? "  ĐÃ THANH TOÁN  " : "  CHƯA THANH TOÁN  ");
            sLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            sLbl.setForeground(Color.WHITE);
            sLbl.setBackground(paid ? R_GREEN : Color.decode("#E67E22"));
            sLbl.setOpaque(true);
            sLbl.setBorder(new EmptyBorder(4, 12, 4, 12));
            statusWrap.add(sLbl);
            p.add(statusWrap);
        }
        p.add(Box.createVerticalStrut(10));
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(8));

        // ── WiFi ──────────────────────────────────────────────────────
        p.add(mkCenterLbl("WiFi: Golden Pearl", 11, new Color(80, 80, 80), Font.PLAIN));
        p.add(Box.createVerticalStrut(4));
        p.add(mkCenterLbl("Mật khẩu: 123456789", 11, new Color(80, 80, 80), Font.PLAIN));
        p.add(Box.createVerticalStrut(8));
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(6));

        // ── Footer ────────────────────────────────────────────────────
        p.add(mkCenterLbl("Cảm ơn quý khách!  Hẹn gặp lại tại Golden Pearl", 11, R_GOLD, Font.ITALIC));

        return p;
    }

    private JLabel mkCenterLbl(String text, int size, Color color, int style) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, size + 12));
        return l;
    }

    private JPanel mkInfoRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel kl = new JLabel(key + ":");
        kl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        kl.setForeground(new Color(140, 140, 140));
        kl.setPreferredSize(new Dimension(105, 20));
        JLabel vl = new JLabel(value);
        vl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        vl.setForeground(new Color(33, 33, 33));
        row.add(kl, BorderLayout.WEST);
        row.add(vl, BorderLayout.CENTER);
        return row;
    }

    private JLabel mkRLabel(String text, int fixedW, int align, Color fg, int style) {
        JLabel l = new JLabel(text, align);
        l.setFont(new Font("Segoe UI", style, 11));
        l.setForeground(fg);
        if (fixedW > 0) {
            l.setMinimumSize(new Dimension(fixedW, 0));
            l.setPreferredSize(new Dimension(fixedW, 20));
            l.setMaximumSize(new Dimension(fixedW, Integer.MAX_VALUE));
        } else {
            l.setMinimumSize(new Dimension(20, 0));
            l.setPreferredSize(new Dimension(60, 20));
            l.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        return l;
    }

    private JPanel mkItemsHeader() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(R_NAVY);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(new EmptyBorder(5, 4, 5, 4));
        row.add(mkRLabel("#",          22, SwingConstants.CENTER, Color.WHITE, Font.BOLD));
        row.add(Box.createHorizontalStrut(4));
        row.add(mkRLabel("Tên món ăn", -1, SwingConstants.LEFT,   Color.WHITE, Font.BOLD));
        row.add(Box.createHorizontalStrut(4));
        row.add(mkRLabel("SL",         26, SwingConstants.CENTER, Color.WHITE, Font.BOLD));
        row.add(Box.createHorizontalStrut(4));
        row.add(mkRLabel("Đơn giá",    74, SwingConstants.RIGHT,  Color.WHITE, Font.BOLD));
        row.add(Box.createHorizontalStrut(4));
        row.add(mkRLabel("Thành tiền", 84, SwingConstants.RIGHT,  Color.WHITE, Font.BOLD));
        return row;
    }

    private JPanel mkItemRow(int idx, String name, int qty, String price, String total, boolean shaded) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(shaded ? new Color(248, 248, 248) : R_BG);
        row.setOpaque(true);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setBorder(new EmptyBorder(4, 4, 4, 4));
        Color dark = Color.decode("#333333");
        row.add(mkRLabel(String.valueOf(idx), 22, SwingConstants.CENTER, dark,   Font.PLAIN));
        row.add(Box.createHorizontalStrut(4));
        row.add(mkRLabel(name,                -1, SwingConstants.LEFT,   dark,   Font.PLAIN));
        row.add(Box.createHorizontalStrut(4));
        row.add(mkRLabel(String.valueOf(qty), 26, SwingConstants.CENTER, dark,   Font.PLAIN));
        row.add(Box.createHorizontalStrut(4));
        row.add(mkRLabel(price,               74, SwingConstants.RIGHT,  dark,   Font.PLAIN));
        row.add(Box.createHorizontalStrut(4));
        row.add(mkRLabel(total,               84, SwingConstants.RIGHT,  R_NAVY, Font.BOLD));
        return row;
    }

    private JPanel mkDashLine() {
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(R_LINE);
                float[] dash = {5f, 4f};
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dash, 0));
                g2.drawLine(0, 1, getWidth(), 1);
            }
        };
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        line.setPreferredSize(new Dimension(400, 3));
        return line;
    }

    private JPanel mkSolidLine(Color color) {
        JPanel line = new JPanel();
        line.setBackground(color);
        line.setOpaque(true);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(400, 1));
        return line;
    }

    private JButton mkDialogBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 22, 8, 22));
        btn.setPreferredSize(new Dimension(0, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}