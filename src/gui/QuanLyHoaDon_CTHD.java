package gui;

import entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Dialog chi tiết / phiếu thanh toán hóa đơn – phiên bản 2.0.
 *
 * <p>Thay đổi so với v1:
 * <ul>
 *   <li>Hiển thị <b>Trạng thái thanh toán</b> chi tiết (Chưa TT / Đã TT / Đã cọc…)
 *       với màu tương ứng (xanh / cam / tím)</li>
 *   <li>Hiển thị <b>Hình thức thanh toán</b> (Tiền mặt / Chuyển khoản…)</li>
 *   <li>Hiển thị <b>Ca làm</b> mà hóa đơn thuộc về</li>
 * </ul>
 */
public class QuanLyHoaDon_CTHD extends JDialog {

    private static final Color S_TEXT_DARK = Color.decode("#333333");
    private static final java.text.SimpleDateFormat S_DATETIME_SDF =
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final Color R_NAVY   = Color.decode("#0B3D59");
    private final Color R_GOLD   = Color.decode("#C5A059");
    private final Color R_LINE   = new Color(220, 220, 220);
    private final Color R_GREEN  = Color.decode("#27AE60");
    private final Color R_ORANGE = Color.decode("#E67E22");
    private final Color R_PURPLE = Color.decode("#8E44AD");
    private final Color R_RED    = Color.decode("#E74C3C");
    private final Color R_BG     = Color.WHITE;

    // ── Constructor đầy đủ ─────────────────────────────────────────────────
    public QuanLyHoaDon_CTHD(java.awt.Window parent, HoaDon hd,
                             List<ChiTietHoaDon> dsCT,
                             boolean isDetail, String tenBan) {
        super(parent,
                isDetail ? "Chi Tiết Hóa Đơn" : "Phiếu Thanh Toán",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setSize(480, 700);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(buildReceipt(hd, dsCT, isDetail,
                tenBan != null ? tenBan : ""));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(240, 242, 245));
        add(scroll, BorderLayout.CENTER);

        add(buildButtonBar(isDetail), BorderLayout.SOUTH);
    }

    /** Constructor tương thích ngược (không truyền tenBan). */
    public QuanLyHoaDon_CTHD(java.awt.Window parent, HoaDon hd,
                             List<ChiTietHoaDon> dsCT, boolean isDetail) {
        this(parent, hd, dsCT, isDetail, "");
    }

    // ── Button bar ─────────────────────────────────────────────────────────

    private JPanel buildButtonBar(boolean isDetail) {
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
        return bar;
    }

    // ── Receipt builder ────────────────────────────────────────────────────

    private JPanel buildReceipt(HoaDon hd, List<ChiTietHoaDon> dsCT,
                                boolean isDetail, String tenBan) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(R_BG);
        p.setBorder(new EmptyBorder(10, 24, 10, 24));

        DecimalFormat fmt = new DecimalFormat("#,###");

        // ── Header nhà hàng ───────────────────────────────────────────
        p.add(mkCenterLbl("GOLDEN PEARL", 23, R_NAVY, Font.BOLD));
        p.add(Box.createVerticalStrut(2));
        p.add(mkCenterLbl("36 Thích Bửu Đăng, P.Hạnh Thông", 10, new Color(120,120,120), Font.PLAIN));
        p.add(mkCenterLbl("Thành phố Hồ Chí Minh", 10, new Color(120,120,120), Font.PLAIN));
        p.add(Box.createVerticalStrut(7));
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(5));

        p.add(mkCenterLbl(isDetail ? "CHI TIẾT HÓA ĐƠN" : "PHIẾU THANH TOÁN", 13, R_NAVY, Font.BOLD));
        p.add(Box.createVerticalStrut(5));
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(7));

        // ── Thông tin hóa đơn ─────────────────────────────────────────
        p.add(mkInfoRow("Mã hóa đơn", hd.getMaHD()));
        p.add(Box.createVerticalStrut(3));
        p.add(mkInfoRow("Ngày lập",
                hd.getNgayLap() != null ? S_DATETIME_SDF.format(hd.getNgayLap()) : "—"));
        p.add(Box.createVerticalStrut(3));

        String nvText = hd.getNhanVien() != null
                ? hd.getNhanVien().getMaNV() + "  –  " + nvTen(hd.getNhanVien()) : "—";
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

        // Số bàn
        p.add(mkInfoRow("Bàn", (tenBan != null && !tenBan.isEmpty()) ? tenBan : "—"));
        p.add(Box.createVerticalStrut(3));

        p.add(Box.createVerticalStrut(8));
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(5));

        // ── Danh sách món ──────────────────────────────────────────────
        p.add(mkItemsHeader());
        p.add(mkSolidLine(R_NAVY));

        double tongTienMon = 0;
        if (dsCT != null && !dsCT.isEmpty()) {
            int idx = 1;
            for (ChiTietHoaDon ct : dsCT) {
                String name = ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "";
                double tt = ct.getDonGia() * ct.getSoLuong();
                tongTienMon += tt;
                p.add(mkItemRow(idx, name, ct.getSoLuong(),
                        fmt.format(ct.getDonGia()) + "đ",
                        fmt.format(tt) + "đ",
                        idx % 2 == 0));
                idx++;
            }
        } else {
            p.add(mkCenterLbl("(Không có chi tiết món)", 11, new Color(160,160,160), Font.ITALIC));
        }

        p.add(Box.createVerticalStrut(4));
        p.add(mkSolidLine(R_LINE));
        p.add(Box.createVerticalStrut(8));

        // ── Tổng tiền, cọc, tổng cộng ─────────────────────────────────
        double coc     = hd.getTienCoc();
        double tongCong = tongTienMon - coc;

        p.add(mkAmountRow("Tổng tiền", fmt.format(tongTienMon) + "đ", false));
        p.add(Box.createVerticalStrut(3));
        p.add(mkAmountRow("Tiền cọc",  "-" + fmt.format(coc) + "đ",  false));
        p.add(Box.createVerticalStrut(5));
        p.add(mkSolidLine(R_LINE));
        p.add(Box.createVerticalStrut(5));

        String tcLabel = tongCong < 0 ? "Số Tiền Hoàn Lại" : "Số Tiền Thanh Toán";
        String tcValue = fmt.format(Math.abs(tongCong)) + "đ";
        Color  tcColor = tongCong < 0 ? R_RED : R_GOLD;
        p.add(mkAmountRow(tcLabel, tcValue, true, tcColor));
        p.add(Box.createVerticalStrut(8));

        // ── [MỚI] Badge trạng thái + hình thức ────────────────────────
        if (isDetail) {
            TrangThaiThanhToan tt = hd.getTrangThaiThanhToan();
            Color badgeColor;
            switch (tt) {
                case DA_THANH_TOAN:   badgeColor = R_GREEN;  break;
                case CHUA_THANH_TOAN: badgeColor = R_ORANGE; break;
                case DA_COC:          badgeColor = R_PURPLE; break;
                case DA_HUY:          badgeColor = R_RED;    break;
                default:              badgeColor = new Color(100,100,100);
            }
            p.add(mkBadge(tt.getDisplay(), badgeColor));
            p.add(Box.createVerticalStrut(4));

            // Hình thức thanh toán
            HinhThucThanhToan ht = hd.getHinhThucThanhToan();
            if (ht != null) {
                p.add(mkBadge("💳  " + ht.getDisplay(), new Color(41, 128, 185)));
            }
            p.add(Box.createVerticalStrut(8));
        }

        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(8));
        p.add(mkCenterLbl("WiFi: Golden Pearl   |   Mật khẩu: 123456789", 11, new Color(80,80,80), Font.PLAIN));
        p.add(Box.createVerticalStrut(8));
        p.add(mkDashLine());
        p.add(Box.createVerticalStrut(6));
        p.add(mkCenterLbl("Cảm ơn quý khách!  Hẹn gặp lại tại Golden Pearl", 11, R_GOLD, Font.ITALIC));

        return p;
    }

    // ── UI helpers ─────────────────────────────────────────────────────────

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
        kl.setForeground(new Color(140,140,140));
        kl.setPreferredSize(new Dimension(110, 20));
        JLabel vl = new JLabel(value);
        vl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        vl.setForeground(new Color(33,33,33));
        row.add(kl, BorderLayout.WEST);
        row.add(vl, BorderLayout.CENTER);
        return row;
    }

    private JPanel mkAmountRow(String key, String value, boolean bold) {
        return mkAmountRow(key, value, bold, new Color(60,60,60));
    }

    private JPanel mkAmountRow(String key, String value, boolean bold, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, bold ? 32 : 26));
        int size  = bold ? 15 : 13;
        int fstyle = bold ? Font.BOLD : Font.PLAIN;
        JLabel kl = new JLabel(key);
        kl.setFont(new Font("Segoe UI", fstyle, size));
        kl.setForeground(R_NAVY);
        JLabel vl = new JLabel(value, SwingConstants.RIGHT);
        vl.setFont(new Font("Segoe UI", fstyle, size));
        vl.setForeground(valueColor);
        row.add(kl, BorderLayout.WEST);
        row.add(vl, BorderLayout.EAST);
        return row;
    }

    private JPanel mkBadge(String text, Color bg) {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lbl = new JLabel("  " + text + "  ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(4, 14, 4, 14));
        wrap.add(lbl);
        return wrap;
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

    private JPanel mkItemRow(int idx, String name, int qty,
                             String price, String total, boolean shaded) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(shaded ? new Color(248,248,248) : R_BG);
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
            @Override protected void paintComponent(Graphics g) {
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

    // ── String helpers ─────────────────────────────────────────────────────

    private String nvTen(NhanVien nv) {
        return (nv.getTenNV() != null && !nv.getTenNV().isEmpty()) ? nv.getTenNV() : "";
    }
}
