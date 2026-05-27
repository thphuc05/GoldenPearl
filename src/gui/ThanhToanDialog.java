package gui;

import dao.HoaDon_DAO;
import dao.KhachHang_DAO;
import dao.KhuyenMai_DAO;
import dao.LichSuDiem_DAO;
import service.ThanhToanService;
import entity.*;
import util.PdfHoaDon;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Panel thanh toán — nhúng trực tiếp vào QuanLyDatBan thay vì popup riêng.
 */
public class ThanhToanDialog extends JPanel {

    // ── Cấu hình VietQR ──────────────────────────────────────────────────────
    private static final String BANK_BIN     = "970418";
    private static final String BANK_NAME    = "BIDV";
    private static final String ACCOUNT_NO   = "6211344691";
    private static final String ACCOUNT_NAME = "GOLDEN PEARL RESTAURANT";

    private static final Color MAIN_BLUE  = Color.decode("#0B3D59");
    private static final Color GOLD_COLOR = Color.decode("#C5A059");
    private static final Color BG_LIGHT   = Color.decode("#F0F2F5");
    private static final Color BORDER_CLR = Color.decode("#DDE1E7");
    private static final Color TEXT_DARK  = Color.decode("#2C3E50");
    private static final Color SUCCESS    = Color.decode("#27AE60");
    private static final DecimalFormat FMT = new DecimalFormat("#,###");

    // ── Core data ─────────────────────────────────────────────────────────────
    private final HoaDon              hoaDon;
    private final List<ChiTietHoaDon> chiTietList;
    private final double              tongTien;
    private final double              tienCoc;
    private final double              conLai;
    private final HoaDon_DAO          hdDAO;
    private final KhuyenMai_DAO       kmDAO  = new KhuyenMai_DAO();
    private final LichSuDiem_DAO      lsdDAO = new LichSuDiem_DAO();
    private final KhachHang_DAO       khDAO  = new KhachHang_DAO();
    private final Runnable            onSuccess;
    private final Runnable            onBack;
    private final String              tenBan;
    private final NhanVien            currentNV;

    // ── KH labels (cập nhật sau khi đổi KH) ──────────────────────────────────
    private JLabel lblMaKH;
    private JLabel lblTenKH;
    private JLabel lblSdtKH;

    // ── Discount state ────────────────────────────────────────────────────────
    private KhuyenMai selectedKM          = null;
    private int       selectedVoucherDiem = 0;
    private int       tongDiemKH          = 0;
    private double    giamKM              = 0;
    private double    giamDiem            = 0;
    private double    conLaiSauGiam;

    // ── Payment state ─────────────────────────────────────────────────────────
    private HinhThucThanhToan selectedHinhThuc = HinhThucThanhToan.TIEN_MAT;
    private JButton           btnTienMat;
    private JButton           btnChuyenKhoan;
    private JPanel            paymentCardPanel;
    private CardLayout        cardLayout;
    private JTextField        txtTienKhachDua;
    private JLabel            lblTienThua;
    private JLabel            lblAmtQR;
    private JLabel            lblCassoStatus;
    private JButton           btnManualConfirm;
    private JButton           btnPay;

    // ── Dynamic footer labels ─────────────────────────────────────────────────
    private JLabel lblFooterGiamKM;
    private JLabel lblFooterGiamDiem;
    private JLabel lblFooterConLai;

    // ── Dynamic breakdown dưới bảng món ──────────────────────────────────────
    private JPanel pBreakdownGiamKM;    // hàng "Giảm KM"   — ẩn khi = 0
    private JPanel pBreakdownGiamDiem;  // hàng "Giảm điểm" — ẩn khi = 0
    private JLabel lblBreakdownGiamKM;
    private JLabel lblBreakdownGiamDiem;
    private JLabel lblBreakdownThucTra;

    // ── Constructor ───────────────────────────────────────────────────────────

    public ThanhToanDialog(HoaDon hoaDon, List<ChiTietHoaDon> chiTietList,
                           double tongTien, HoaDon_DAO hdDAO, String tenBan,
                           NhanVien currentNV,
                           Runnable onSuccess, Runnable onBack) {
        this.hoaDon      = hoaDon;
        this.chiTietList = chiTietList;
        this.tongTien    = tongTien;
        this.hdDAO       = hdDAO;
        this.tenBan      = tenBan;
        this.currentNV   = currentNV;
        this.onSuccess   = onSuccess;
        this.onBack      = onBack;
        this.tienCoc     = hoaDon.getTienCoc();
        this.conLai      = Math.max(0, tongTien - tienCoc);
        this.conLaiSauGiam = conLai;

        KhachHang kh = hoaDon.getKhachHang();
        if (kh != null && !"0000000000".equals(kh.getSoDT())) {
            tongDiemKH = lsdDAO.getTongDiem(kh.getMaKH());
        }

        initUI();
    }

    // ── UI assembly ───────────────────────────────────────────────────────────

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(BG_LIGHT);
        body.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));

        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.gridx = 0; gbcL.gridy = 0; gbcL.weightx = 0.57; gbcL.weighty = 1.0;
        gbcL.fill = GridBagConstraints.BOTH; gbcL.insets = new Insets(0, 0, 0, 8);
        body.add(buildLeftPanel(), gbcL);

        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.gridx = 1; gbcR.gridy = 0; gbcR.weightx = 0.43; gbcR.weighty = 1.0;
        gbcR.fill = GridBagConstraints.BOTH; gbcR.insets = new Insets(0, 8, 0, 0);
        body.add(buildRightPanel(), gbcR);

        add(body, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GOLD_COLOR);
        p.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel title = new JLabel("THANH TOÁN HÓA ĐƠN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel maHD = new JLabel("Mã HĐ: " + hoaDon.getMaHD());
        maHD.setForeground(MAIN_BLUE);
        maHD.setFont(new Font("Segoe UI", Font.BOLD, 14));

        p.add(title, BorderLayout.WEST);
        p.add(maHD,  BorderLayout.EAST);
        return p;
    }

    // ── Left panel ────────────────────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_LIGHT);
        p.add(sectionLabel("Chi Tiết Món Ăn"), BorderLayout.NORTH);

        String[] cols = {"STT", "Tên Món", "SL", "Đơn Giá", "Thành Tiền", "Giờ gọi", "Ghi chú"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
        int stt = 1;
        for (ChiTietHoaDon ct : chiTietList) {
            String tenMon   = ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "—";
            String gioGoi   = ct.getThoiGianGoi() != null ? timeFmt.format(ct.getThoiGianGoi()) : "";
            String ghiChu   = (ct.getGhiChu() != null && !ct.getGhiChu().isEmpty()) ? ct.getGhiChu() : "";
            model.addRow(new Object[]{
                stt++, tenMon, ct.getSoLuong(),
                FMT.format(ct.getDonGia()) + "đ",
                FMT.format(ct.getThanhTien()) + "đ",
                gioGoi, ghiChu
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(BORDER_CLR);
        table.setShowGrid(true);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(Color.decode("#E8F4FD"));
        table.setSelectionForeground(TEXT_DARK);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(MAIN_BLUE);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 38));
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        // STT=40, TênMón=0(flex), SL=42, ĐơnGiá=100, ThànhTiền=110, GiờGọi=58, GhiChú=0(flex)
        int[] widths = {40, 0, 42, 100, 110, 58, 0};
        for (int i = 0; i < widths.length; i++)
            if (widths[i] > 0) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(0).setCellRenderer(center); // STT
        table.getColumnModel().getColumn(2).setCellRenderer(center); // SL
        table.getColumnModel().getColumn(3).setCellRenderer(right);  // Đơn giá
        table.getColumnModel().getColumn(4).setCellRenderer(right);  // Thành tiền
        table.getColumnModel().getColumn(5).setCellRenderer(center); // Giờ gọi

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.getViewport().setBackground(Color.WHITE);
        p.add(scroll, BorderLayout.CENTER);

        p.add(buildBreakdownPanel(), BorderLayout.SOUTH);
        return p;
    }

    // ── Breakdown panel dưới bảng món ────────────────────────────────────────

    private JPanel buildBreakdownPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_LIGHT);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR),
                BorderFactory.createEmptyBorder(6, 8, 4, 8)));

        GridBagConstraints gk = new GridBagConstraints();
        gk.anchor = GridBagConstraints.WEST; gk.weightx = 1.0;
        gk.fill   = GridBagConstraints.HORIZONTAL; gk.insets = new Insets(2, 0, 2, 12);

        GridBagConstraints gv = new GridBagConstraints();
        gv.anchor = GridBagConstraints.EAST; gv.gridx = 1;
        gv.insets = new Insets(2, 0, 2, 0);

        // Hàng: số món + tổng gốc
        gk.gridy = 0; gv.gridy = 0;
        p.add(breakdownKey(chiTietList.size() + " món  —  Tổng tiền gốc:", false), gk);
        p.add(breakdownVal(FMT.format(tongTien) + "đ", TEXT_DARK, false), gv);

        // Hàng: Giảm KM (ẩn/hiện động)
        gk.gridy = 1; gv.gridy = 1;
        JLabel kGiamKM = breakdownKey("Giảm khuyến mãi:", false);
        lblBreakdownGiamKM = breakdownVal("—", new Color(0xE67E22), false);
        pBreakdownGiamKM = wrapRow(kGiamKM, lblBreakdownGiamKM);
        gk.gridwidth = 2; gk.gridx = 0;
        p.add(pBreakdownGiamKM, gk);
        gk.gridwidth = 1; gk.gridx = 0;

        // Hàng: Giảm điểm (ẩn/hiện động)
        gk.gridy = 2; gv.gridy = 2;
        JLabel kGiamDiem = breakdownKey("Giảm điểm thưởng:", false);
        lblBreakdownGiamDiem = breakdownVal("—", new Color(0xE67E22), false);
        pBreakdownGiamDiem = wrapRow(kGiamDiem, lblBreakdownGiamDiem);
        gk.gridwidth = 2; gk.gridx = 0;
        p.add(pBreakdownGiamDiem, gk);
        gk.gridwidth = 1; gk.gridx = 0;

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        gk.gridy = 3; gk.gridwidth = 2; gk.insets = new Insets(4, 0, 4, 0);
        p.add(sep, gk);
        gk.gridwidth = 1; gk.insets = new Insets(2, 0, 2, 12);

        // Hàng: Thực trả
        gk.gridy = 4; gv.gridy = 4;
        p.add(breakdownKey("Thực trả:", true), gk);
        lblBreakdownThucTra = breakdownVal(FMT.format(conLaiSauGiam) + "đ", Color.decode("#E74C3C"), true);
        p.add(lblBreakdownThucTra, gv);

        // Ẩn discount rows lúc khởi tạo (chưa chọn gì)
        pBreakdownGiamKM.setVisible(false);
        pBreakdownGiamDiem.setVisible(false);

        return p;
    }

    private JLabel breakdownKey(String text, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JLabel breakdownVal(String text, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(color);
        return l;
    }

    private JPanel wrapRow(JLabel key, JLabel val) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG_LIGHT);
        row.add(key, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        return row;
    }

    // ── Right panel ───────────────────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG_LIGHT);

        // Top: info card + discount (NORTH — lấy preferred height)
        JPanel topCards = new JPanel(new GridLayout(2, 1, 0, 8));
        topCards.setBackground(BG_LIGHT);
        topCards.add(buildInfoCard());
        topCards.add(buildDiscountSection());
        p.add(topCards, BorderLayout.NORTH);

        // Pay section: toggle luôn hiển thị (NORTH), content scroll (CENTER)
        JPanel paySection = new JPanel(new BorderLayout(0, 4));
        paySection.setBackground(BG_LIGHT);

        JPanel payHeader = new JPanel(new BorderLayout(0, 4));
        payHeader.setBackground(BG_LIGHT);
        payHeader.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        payHeader.add(sectionLabel("Hình Thức Thanh Toán"), BorderLayout.NORTH);
        payHeader.add(buildMethodToggle(), BorderLayout.CENTER);
        paySection.add(payHeader, BorderLayout.NORTH);   // toggle luôn có chỗ

        cardLayout = new CardLayout();
        paymentCardPanel = new JPanel(cardLayout);
        paymentCardPanel.setBackground(BG_LIGHT);
        paymentCardPanel.add(buildTienMatPanel(),     "TIEN_MAT");
        paymentCardPanel.add(buildChuyenKhoanPanel(), "CHUYEN_KHOAN");

        JScrollPane payScroll = new JScrollPane(paymentCardPanel);
        payScroll.setBorder(null);
        payScroll.getViewport().setBackground(BG_LIGHT);
        payScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        payScroll.getVerticalScrollBar().setUnitIncrement(12);
        paySection.add(payScroll, BorderLayout.CENTER);  // content scroll nếu cần

        p.add(paySection, BorderLayout.CENTER);
        return p;
    }

    // ── Info card ─────────────────────────────────────────────────────────────

    private JPanel buildInfoCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Thông Tin Hóa Đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(MAIN_BLUE);
        title.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1.0;
        g.insets = new Insets(0, 4, 8, 4);
        card.add(title, g);

        g.gridwidth = 1; g.insets = new Insets(2, 4, 2, 4);
        NhanVien  nv = hoaDon.getNhanVien();
        KhachHang kh = hoaDon.getKhachHang();

        // NV rows (không cần ref)
        Object[][] nvRows = {
            {"Mã NV:",  nv != null ? nv.getMaNV()  : "—"},
            {"Tên NV:", nv != null ? nv.getTenNV() : "—"},
        };
        for (int i = 0; i < nvRows.length; i++) {
            g.gridx = 0; g.gridy = i + 1; g.weightx = 0;
            JLabel key = new JLabel((String) nvRows[i][0]);
            key.setFont(new Font("Segoe UI", Font.BOLD, 13));
            key.setForeground(TEXT_DARK);
            card.add(key, g);
            g.gridx = 1; g.weightx = 1.0;
            JLabel val = new JLabel((String) nvRows[i][1]);
            val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            val.setForeground(TEXT_DARK);
            card.add(val, g);
        }

        // KH rows — lưu ref để cập nhật khi đổi KH
        String[][] khRows = {
            {"Mã KH:",  kh != null ? kh.getMaKH()  : "—"},
            {"Tên KH:", kh != null ? kh.getTenKH() : "—"},
            {"SĐT KH:", kh != null ? kh.getSoDT()  : "—"},
        };
        JLabel[] khVals = new JLabel[3];
        for (int i = 0; i < khRows.length; i++) {
            g.gridx = 0; g.gridy = i + 3; g.weightx = 0;
            JLabel key = new JLabel(khRows[i][0]);
            key.setFont(new Font("Segoe UI", Font.BOLD, 13));
            key.setForeground(TEXT_DARK);
            card.add(key, g);
            g.gridx = 1; g.weightx = 1.0;
            khVals[i] = new JLabel(khRows[i][1]);
            khVals[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            khVals[i].setForeground(TEXT_DARK);
            card.add(khVals[i], g);
        }
        lblMaKH  = khVals[0];
        lblTenKH = khVals[1];
        lblSdtKH = khVals[2];

        // Nút "Đổi KH" — chỉ hiện nếu Quản lý
        if (isQuanLy(currentNV)) {
            JButton btnDoiKH = new JButton("Đổi khách hàng");
            btnDoiKH.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnDoiKH.setForeground(Color.WHITE);
            btnDoiKH.setBackground(MAIN_BLUE);
            btnDoiKH.setFocusPainted(false);
            btnDoiKH.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnDoiKH.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            btnDoiKH.addActionListener(e -> showDoiKhachHangDialog());
            g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.weightx = 1.0;
            g.insets = new Insets(8, 4, 2, 4);
            card.add(btnDoiKH, g);
        }

        return card;
    }

    // ── Đổi khách hàng ───────────────────────────────────────────────────────

    private static boolean isQuanLy(NhanVien nv) {
        if (nv == null) return false;
        TaiKhoan tk = nv.getTaiKhoan();
        if (tk == null) return false;
        String vaiTro = tk.getVaiTro();
        return "QUAN_LY".equalsIgnoreCase(vaiTro)
                || "QL".equalsIgnoreCase(vaiTro)
                || "Quản Lý".equalsIgnoreCase(vaiTro);
    }

    private void showDoiKhachHangDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = owner instanceof Frame
                ? new JDialog((Frame) owner, "Đổi khách hàng", true)
                : new JDialog((Dialog) owner, "Đổi khách hàng", true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel lbl = new JLabel("SĐT khách hàng mới:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JTextField txtSdt = new JTextField(14);
        txtSdt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputRow.add(lbl);
        inputRow.add(txtSdt);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnCancel = new JButton("Hủy");
        JButton btnOk = new JButton("Xác nhận");
        btnOk.setBackground(MAIN_BLUE);
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);
        btnRow.add(btnCancel);
        btnRow.add(btnOk);

        dlg.add(inputRow, BorderLayout.CENTER);
        dlg.add(btnRow,   BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnOk.addActionListener(e -> {
            String sdt = txtSdt.getText().trim();
            if (!sdt.matches("0\\d{9}")) {
                JOptionPane.showMessageDialog(dlg,
                        "SĐT không hợp lệ (10 số, bắt đầu bằng 0)!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dlg.dispose();
            btnOk.setEnabled(false);
            new SwingWorker<KhachHang, Void>() {
                @Override protected KhachHang doInBackground() throws Exception {
                    return ThanhToanService.doiKhachHang(hoaDon, sdt, currentNV, khDAO, hdDAO);
                }
                @Override protected void done() {
                    try {
                        KhachHang khMoi = get();
                        lblMaKH.setText(khMoi.getMaKH() != null ? khMoi.getMaKH() : "—");
                        lblTenKH.setText(khMoi.getTenKH() != null ? khMoi.getTenKH() : "—");
                        lblSdtKH.setText(khMoi.getSoDT() != null ? khMoi.getSoDT() : "—");
                        JOptionPane.showMessageDialog(ThanhToanDialog.this,
                                "Đổi khách hàng thành công!\n"
                                        + khMoi.getMaKH() + " — " + khMoi.getTenKH(),
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(ThanhToanDialog.this,
                                cause.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        btnOk.setEnabled(true);
                    }
                }
            }.execute();
        });

        dlg.pack();
        dlg.setMinimumSize(new Dimension(360, 0));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── Discount section ──────────────────────────────────────────────────────

    private JPanel buildDiscountSection() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Khuyến Mãi & Điểm Thưởng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(MAIN_BLUE);
        title.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1.0;
        g.insets = new Insets(0, 4, 8, 4);
        card.add(title, g);

        g.gridwidth = 1; g.insets = new Insets(3, 4, 3, 4);

        // Khuyến mãi lễ
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        JLabel lblKM = new JLabel("Khuyến mãi lễ:");
        lblKM.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblKM.setForeground(TEXT_DARK);
        card.add(lblKM, g);

        List<KhuyenMai> dsKM = kmDAO.getKhuyenMaiHoatDong();
        String[] kmItems = new String[dsKM.size() + 1];
        kmItems[0] = "Không áp dụng";
        for (int i = 0; i < dsKM.size(); i++) {
            KhuyenMai km = dsKM.get(i);
            kmItems[i + 1] = km.getTenKM() + "  (–" + (int) km.getPhanTramGiam() + "%)";
        }
        JComboBox<String> cbKM = new JComboBox<>(kmItems);
        cbKM.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbKM.addActionListener(e -> {
            int idx = cbKM.getSelectedIndex();
            selectedKM = idx > 0 ? dsKM.get(idx - 1) : null;
            recalcConLai();
        });
        g.gridx = 1; g.weightx = 1.0;
        card.add(cbKM, g);

        // Voucher điểm
        g.gridx = 0; g.gridy = 2; g.weightx = 0;
        JLabel lblDiem = new JLabel("Voucher điểm:");
        lblDiem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDiem.setForeground(TEXT_DARK);
        card.add(lblDiem, g);

        KhachHang kh = hoaDon.getKhachHang();
        boolean isVangLai = kh == null || "0000000000".equals(kh.getSoDT());

        List<int[]> tiers = new ArrayList<>();
        tiers.add(new int[]{0, 0});
        if (!isVangLai) {
            if (tongDiemKH >= 500)  tiers.add(new int[]{500,   50_000});
            if (tongDiemKH >= 1000) tiers.add(new int[]{1000, 100_000});
            if (tongDiemKH >= 2000) tiers.add(new int[]{2000, 200_000});
        }

        String pointsInfo = isVangLai ? "(Khách vãng lai)" : "(" + tongDiemKH + " điểm hiện có)";
        String[] diemItems = new String[tiers.size()];
        diemItems[0] = "Không dùng  " + pointsInfo;
        for (int i = 1; i < tiers.size(); i++) {
            int[] t = tiers.get(i);
            diemItems[i] = t[0] + " điểm  →  Giảm " + FMT.format(t[1]) + "đ";
        }

        JComboBox<String> cbDiem = new JComboBox<>(diemItems);
        cbDiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDiem.setEnabled(!isVangLai && tongDiemKH >= 500);
        cbDiem.addActionListener(e -> {
            int idx = cbDiem.getSelectedIndex();
            selectedVoucherDiem = idx > 0 ? tiers.get(idx)[0] : 0;
            recalcConLai();
        });
        g.gridx = 1; g.weightx = 1.0;
        card.add(cbDiem, g);

        return card;
    }

    private void recalcConLai() {
        giamKM   = selectedKM != null
                ? Math.floor(tongTien * selectedKM.getPhanTramGiam() / 100.0) : 0;
        giamDiem = selectedVoucherDiem >= 2000 ? 200_000 :
                   selectedVoucherDiem >= 1000 ? 100_000 :
                   selectedVoucherDiem >= 500  ?  50_000 : 0;
        conLaiSauGiam = Math.max(0, tongTien - tienCoc - giamKM - giamDiem);

        // Footer
        if (lblFooterGiamKM   != null) lblFooterGiamKM.setText(giamKM   > 0 ? "-" + FMT.format(giamKM)   + "đ" : "—");
        if (lblFooterGiamDiem != null) lblFooterGiamDiem.setText(giamDiem > 0 ? "-" + FMT.format(giamDiem) + "đ" : "—");
        if (lblFooterConLai   != null) lblFooterConLai.setText(FMT.format(conLaiSauGiam) + "đ");

        // Breakdown dưới bảng món
        if (lblBreakdownGiamKM != null) {
            lblBreakdownGiamKM.setText(giamKM > 0 ? "−" + FMT.format(giamKM) + "đ" : "");
            pBreakdownGiamKM.setVisible(giamKM > 0);
        }
        if (lblBreakdownGiamDiem != null) {
            lblBreakdownGiamDiem.setText(giamDiem > 0 ? "−" + FMT.format(giamDiem) + "đ" : "");
            pBreakdownGiamDiem.setVisible(giamDiem > 0);
        }
        if (lblBreakdownThucTra != null)
            lblBreakdownThucTra.setText(FMT.format(conLaiSauGiam) + "đ");

        // Panel thanh toán
        if (lblAmtQR        != null) lblAmtQR.setText("Số tiền: " + FMT.format(conLaiSauGiam) + "đ");
        if (txtTienKhachDua != null) txtTienKhachDua.setText(FMT.format(conLaiSauGiam));
        updateTienThua();
    }

    // ── Method toggle ─────────────────────────────────────────────────────────

    private JPanel buildMethodToggle() {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setBackground(BG_LIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        btnTienMat     = makeToggleBtn("Tiền mặt",     true);
        btnChuyenKhoan = makeToggleBtn("Chuyển khoản", false);
        btnTienMat.addActionListener(e     -> switchMethod(HinhThucThanhToan.TIEN_MAT));
        btnChuyenKhoan.addActionListener(e -> switchMethod(HinhThucThanhToan.CHUYEN_KHOAN));

        p.add(btnTienMat);
        p.add(btnChuyenKhoan);
        return p;
    }

    private JButton makeToggleBtn(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyToggleStyle(btn, active);
        return btn;
    }

    private void applyToggleStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(MAIN_BLUE);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_DARK);
            btn.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        }
    }

    private void switchMethod(HinhThucThanhToan method) {
        selectedHinhThuc = method;
        applyToggleStyle(btnTienMat,     method == HinhThucThanhToan.TIEN_MAT);
        applyToggleStyle(btnChuyenKhoan, method == HinhThucThanhToan.CHUYEN_KHOAN);
        cardLayout.show(paymentCardPanel,
                method == HinhThucThanhToan.TIEN_MAT ? "TIEN_MAT" : "CHUYEN_KHOAN");
    }

    // ── Tiền mặt panel ────────────────────────────────────────────────────────

    private JPanel buildTienMatPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel inputRow = new JPanel(new BorderLayout(0, 6));
        inputRow.setBackground(Color.WHITE);
        JLabel inputLbl = new JLabel("Khách đưa (đ):");
        inputLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputLbl.setForeground(TEXT_DARK);

        txtTienKhachDua = new JTextField(FMT.format(conLaiSauGiam));
        txtTienKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 17));
        txtTienKhachDua.setForeground(MAIN_BLUE);
        txtTienKhachDua.setHorizontalAlignment(JTextField.RIGHT);
        txtTienKhachDua.setPreferredSize(new Dimension(0, 46));
        txtTienKhachDua.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        txtTienKhachDua.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { updateTienThua(); }
        });

        inputRow.add(inputLbl,        BorderLayout.NORTH);
        inputRow.add(txtTienKhachDua, BorderLayout.CENTER);

        long[] quickAmounts = {50_000, 100_000, 200_000, 500_000, 1_000_000, 2_000_000};
        JPanel quickPanel = new JPanel(new GridLayout(2, 3, 6, 6));
        quickPanel.setBackground(Color.WHITE);
        for (long amount : quickAmounts) {
            JButton btn = new JButton(FMT.format(amount) + "đ");
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setBackground(BG_LIGHT);
            btn.setForeground(TEXT_DARK);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> { txtTienKhachDua.setText(FMT.format(amount)); updateTienThua(); });
            quickPanel.add(btn);
        }

        JPanel thuaRow = new JPanel(new BorderLayout(10, 0));
        thuaRow.setBackground(Color.WHITE);
        thuaRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)));

        JLabel thuaKey = new JLabel("Tiền thừa trả khách:");
        thuaKey.setFont(new Font("Segoe UI", Font.BOLD, 13));
        thuaKey.setForeground(TEXT_DARK);

        lblTienThua = new JLabel("0đ");
        lblTienThua.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTienThua.setForeground(SUCCESS);
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);

        thuaRow.add(thuaKey,    BorderLayout.WEST);
        thuaRow.add(lblTienThua, BorderLayout.CENTER);

        p.add(inputRow,   BorderLayout.NORTH);
        p.add(quickPanel, BorderLayout.CENTER);
        p.add(thuaRow,    BorderLayout.SOUTH);

        updateTienThua();
        return p;
    }

    private void updateTienThua() {
        if (lblTienThua == null) return;
        try {
            String raw = txtTienKhachDua.getText().replaceAll("[^0-9]", "");
            double khachDua = raw.isEmpty() ? 0 : Double.parseDouble(raw);
            double thua = khachDua - conLaiSauGiam;
            if (thua < 0) {
                lblTienThua.setText("Thiếu " + FMT.format(-thua) + "đ");
                lblTienThua.setForeground(Color.decode("#E74C3C"));
            } else {
                lblTienThua.setText(FMT.format(thua) + "đ");
                lblTienThua.setForeground(SUCCESS);
            }
        } catch (NumberFormatException ignored) {}
    }

    // ── Chuyển khoản panel ────────────────────────────────────────────────────

    private JPanel buildChuyenKhoanPanel() {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        // QR code (trái) — cố định 160x160
        JLabel ckQrLabel = new JLabel("Đang tải QR...", SwingConstants.CENTER);
        ckQrLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        ckQrLabel.setForeground(Color.GRAY);
        ckQrLabel.setPreferredSize(new Dimension(160, 160));
        ckQrLabel.setMinimumSize(new Dimension(160, 160));
        ckQrLabel.setMaximumSize(new Dimension(160, 160));
        ckQrLabel.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        ckQrLabel.setBackground(BG_LIGHT);
        ckQrLabel.setOpaque(true);

        // Info panel (phải) — GridBagLayout kiểm soát spacing chặt
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor  = GridBagConstraints.NORTHWEST;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx   = 0;

        // Số tiền
        lblAmtQR = new JLabel("Số tiền: " + FMT.format(conLaiSauGiam) + "đ");
        lblAmtQR.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAmtQR.setForeground(MAIN_BLUE);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 8, 0);
        infoPanel.add(lblAmtQR, gc);

        // Thông tin ngân hàng
        String[][] bankInfo = {
            {"Ngân hàng:",    BANK_NAME},
            {"Số tài khoản:", ACCOUNT_NO},
            {"Chủ TK:",       ACCOUNT_NAME},
        };
        for (int i = 0; i < bankInfo.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            row.setBackground(Color.WHITE);
            JLabel k = new JLabel(bankInfo[i][0] + " ");
            k.setFont(new Font("Segoe UI", Font.BOLD, 12));
            k.setForeground(TEXT_DARK);
            JLabel v = new JLabel(bankInfo[i][1]);
            v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            v.setForeground(MAIN_BLUE);
            row.add(k); row.add(v);
            gc.gridy = i + 1; gc.insets = new Insets(0, 0, 4, 0);
            infoPanel.add(row, gc);
        }

        // Đường kẻ phân cách
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        gc.gridy = 4; gc.insets = new Insets(8, 0, 8, 0);
        infoPanel.add(sep, gc);

        // Casso status
        lblCassoStatus = new JLabel("", SwingConstants.LEFT);
        lblCassoStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCassoStatus.setVisible(false);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 6, 0);
        infoPanel.add(lblCassoStatus, gc);

        // Nút xác nhận
        btnManualConfirm = new JButton("Xác nhận đã nhận tiền") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? SUCCESS.darker() : SUCCESS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnManualConfirm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnManualConfirm.setForeground(Color.WHITE);
        btnManualConfirm.setFocusPainted(false);
        btnManualConfirm.setBorderPainted(false);
        btnManualConfirm.setContentAreaFilled(false);
        btnManualConfirm.setPreferredSize(new Dimension(0, 38));
        btnManualConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnManualConfirm.setVisible(false);
        btnManualConfirm.addActionListener(e -> completePayment());
        gc.gridy = 6; gc.insets = new Insets(0, 0, 0, 0);
        infoPanel.add(btnManualConfirm, gc);

        // Filler đẩy nội dung lên trên
        gc.gridy = 7; gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel(); filler.setOpaque(false);
        infoPanel.add(filler, gc);

        p.add(ckQrLabel, BorderLayout.WEST);
        p.add(infoPanel, BorderLayout.CENTER);

        fetchVietQR(ckQrLabel, (long) conLaiSauGiam, "Thanh toan " + hoaDon.getMaHD());
        return p;
    }

    private void fetchVietQR(JLabel target, long amount, String description) {
        new SwingWorker<ImageIcon, Void>() {
            @Override protected ImageIcon doInBackground() throws Exception {
                String enc  = URLEncoder.encode(description,  StandardCharsets.UTF_8.name());
                String encN = URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8.name());
                String url  = "https://img.vietqr.io/image/"
                        + BANK_BIN + "-" + ACCOUNT_NO + "-compact2.png"
                        + "?amount=" + amount + "&addInfo=" + enc + "&accountName=" + encN;
                BufferedImage raw = ImageIO.read(new URL(url));
                if (raw == null) return null;
                return new ImageIcon(raw.getScaledInstance(175, 175, Image.SCALE_SMOOTH));
            }
            @Override protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) { target.setIcon(icon); target.setText(null); }
                    else target.setText("<html><center>Không tải được QR<br>Kiểm tra kết nối</center></html>");
                } catch (Exception ignored) {
                    target.setText("<html><center>Không tải được QR<br>Kiểm tra kết nối</center></html>");
                }
            }
        }.execute();
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(MAIN_BLUE);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        summary.setOpaque(false);

        summary.add(footerStat("Tổng tiền", FMT.format(tongTien) + "đ", GOLD_COLOR, null));
        summary.add(footerDivider());
        summary.add(footerStat("Tiền cọc",  FMT.format(tienCoc)  + "đ", Color.WHITE, null));
        summary.add(footerDivider());

        JLabel[] refKM = new JLabel[1];
        summary.add(footerStat("Giảm KM", "—", Color.WHITE, refKM));
        lblFooterGiamKM = refKM[0];
        summary.add(footerDivider());

        JLabel[] refDiem = new JLabel[1];
        summary.add(footerStat("Giảm điểm", "—", Color.WHITE, refDiem));
        lblFooterGiamDiem = refDiem[0];
        summary.add(footerDivider());

        JLabel[] refConLai = new JLabel[1];
        summary.add(footerStat("Còn lại", FMT.format(conLaiSauGiam) + "đ", GOLD_COLOR, refConLai));
        lblFooterConLai = refConLai[0];

        footer.add(summary, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnBack = new JButton("Quay lại");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBack.setPreferredSize(new Dimension(120, 42));
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(MAIN_BLUE);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> onBack.run());

        btnPay = new JButton("THANH TOÁN VÀ XUẤT HÓA ĐƠN");
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnPay.setPreferredSize(new Dimension(350, 42));
        btnPay.setBackground(GOLD_COLOR);
        btnPay.setForeground(Color.WHITE);
        btnPay.setFocusPainted(false);
        btnPay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPay.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnPay.addActionListener(e -> doConfirmPayment());

        btnRow.add(btnBack);
        btnRow.add(btnPay);
        footer.add(btnRow, BorderLayout.EAST);
        return footer;
    }

    private JPanel footerStat(String label, String value, Color valueColor, JLabel[] refOut) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(255, 255, 255, 170));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 16));
        val.setForeground(valueColor);
        p.add(lbl); p.add(val);
        if (refOut != null) refOut[0] = val;
        return p;
    }

    private JSeparator footerDivider() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setForeground(new Color(255, 255, 255, 60));
        sep.setPreferredSize(new Dimension(1, 36));
        return sep;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(MAIN_BLUE);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    // ── Payment action ────────────────────────────────────────────────────────

    private void doConfirmPayment() {
        if (selectedHinhThuc == HinhThucThanhToan.TIEN_MAT) {
            String raw = txtTienKhachDua.getText().replaceAll("[^0-9]", "");
            double khachDua = raw.isEmpty() ? 0 : Double.parseDouble(raw);
            if (khachDua < conLaiSauGiam) {
                JOptionPane.showMessageDialog(this,
                        "Số tiền khách đưa chưa đủ!\nCòn thiếu: " + FMT.format(conLaiSauGiam - khachDua) + "đ",
                        "Chưa đủ tiền", JOptionPane.WARNING_MESSAGE);
                return;
            }
            completePayment();
        } else {
            // Chuyển khoản: xác nhận thủ công thu ngân
            int res = JOptionPane.showConfirmDialog(this,
                    "Xác nhận đã nhận chuyển khoản\n" + FMT.format((long) conLaiSauGiam) + "đ từ khách?",
                    "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.YES_OPTION) completePayment();
        }
    }

    private void completePayment() {
        btnPay.setEnabled(false);
        btnManualConfirm.setEnabled(false);

        double tongTienThucThu = tienCoc + conLaiSauGiam;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                ThanhToanService.thanhToan(
                        hoaDon, tongTienThucThu, selectedHinhThuc,
                        selectedVoucherDiem, giamDiem,
                        hdDAO, lsdDAO);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    btnPay.setEnabled(true);
                    btnManualConfirm.setEnabled(true);
                    JOptionPane.showMessageDialog(ThanhToanDialog.this,
                            "Lỗi khi lưu thanh toán. Vui lòng thử lại.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(ThanhToanDialog.this,
                        "Thanh toán thành công!\nHóa đơn: " + hoaDon.getMaHD()
                        + "\nSố tiền: " + FMT.format(tongTienThucThu) + "đ",
                        "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);

                // Cập nhật lại tongTien và hinhThucThanhToan trong object trước khi in PDF
                hoaDon.setTongTien(tongTienThucThu);
                hoaDon.setHinhThucThanhToan(selectedHinhThuc);
                hoaDon.setTrangThaiThanhToan(TrangThaiThanhToan.DA_THANH_TOAN);

                // In hóa đơn PDF trên background, không block UI
                new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() {
                        PdfHoaDon.xuatVaMo(hoaDon, chiTietList, tenBan);
                        return null;
                    }
                    @Override protected void done() { /* tự mở file PDF */ }
                }.execute();

                onSuccess.run();
            }
        }.execute();
    }
}
