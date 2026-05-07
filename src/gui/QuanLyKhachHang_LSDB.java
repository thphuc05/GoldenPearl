package gui;

import dao.ChiTietHoaDon_DAO;
import dao.HoaDon_DAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class QLKH_Lskh extends JDialog {

    private final Color MAIN_BLUE    = Color.decode("#0B3D59");
    private final Color GOLD_COLOR   = Color.decode("#C5A059");
    private final Color TEXT_DARK    = Color.decode("#333333");
    private final Color BORDER_COLOR = Color.decode("#E0E0E0");
    private final Color GREEN_STATUS = Color.decode("#27AE60");
    private final Color RED_STATUS   = Color.decode("#E74C3C");

    private final SimpleDateFormat dateSdf = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat dtFmt   = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final HoaDon_DAO hd_dao;
    private final ChiTietHoaDon_DAO ct_dao;
    private final String maKH;
    private final String tenKH;

    public QLKH_Lskh(Window parent, String maKH, String tenKH, HoaDon_DAO hd_dao, ChiTietHoaDon_DAO ct_dao) {
        super(parent, "Lịch sử hóa đơn — " + tenKH, ModalityType.APPLICATION_MODAL);
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.hd_dao = hd_dao;
        this.ct_dao = ct_dao;

        setSize(860, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // Build invoice list for the customer
        List<HoaDon> dsHD = new ArrayList<>();
        List<HoaDon> all  = hd_dao.getAllHoaDon();
        if (all != null) {
            for (HoaDon hd : all) {
                if (hd.getKhachHang() != null && maKH.equals(hd.getKhachHang().getMaKH())) {
                    dsHD.add(hd);
                }
            }
        }

        // -- Left: invoice table
        String[] ivCols = {"Mã HĐ", "Ngày lập", "Tổng tiền", "Trạng thái"};
        DefaultTableModel ivModel = new DefaultTableModel(ivCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable ivTable = new JTable(ivModel);
        ivTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ivTable.setRowHeight(30);
        ivTable.setGridColor(new Color(235, 235, 235));
        ivTable.setShowVerticalLines(false);
        ivTable.setBackground(Color.WHITE);
        ivTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        ivTable.getTableHeader().setBackground(new Color(248, 248, 248));
        ivTable.getTableHeader().setPreferredSize(new Dimension(0, 32));
        ivTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        ivTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        ivTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        ivTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                                                                     boolean sel, boolean focus, int r2, int c2) {
                super.getTableCellRendererComponent(t, val, sel, focus, r2, c2);
                String s = val == null ? "" : val.toString();
                if (!sel) setForeground("Đã thanh toán".equals(s) ? GREEN_STATUS : RED_STATUS);
                else setForeground(TEXT_DARK);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        for (HoaDon hd : dsHD) {
            ivModel.addRow(new Object[]{
                    hd.getMaHD(),
                    hd.getNgayLap() != null ? dateSdf.format(hd.getNgayLap()) : "",
                    String.format("%,.0fđ", hd.getTongTien()),
                    hd.isTrangThai() ? "Đã thanh toán" : "Chưa thanh toán"
            });
        }

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("DANH SÁCH HÓA ĐƠN"));
        leftPanel.add(new JScrollPane(ivTable), BorderLayout.CENTER);

        // -- Right: detail panel
        JLabel lblMaHD    = mkDetailLabel();
        JLabel lblNgay    = mkDetailLabel();
        JLabel lblNV      = mkDetailLabel();
        JLabel lblKH      = mkDetailLabel();
        JLabel lblTong    = mkDetailLabel();
        JLabel lblCoc     = mkDetailLabel();
        JLabel lblTongCong = mkDetailLabel();
        JLabel lblTT      = mkDetailLabel();

        JPanel infoGrid = new JPanel(new GridLayout(8, 1, 0, 4));
        infoGrid.setBackground(Color.WHITE);
        infoGrid.setBorder(new EmptyBorder(8, 10, 4, 10));
        infoGrid.add(lblMaHD); infoGrid.add(lblNgay); infoGrid.add(lblNV);
        infoGrid.add(lblKH); infoGrid.add(lblTong); infoGrid.add(lblCoc);
        infoGrid.add(lblTongCong); infoGrid.add(lblTT);

        String[] ctCols = {"STT", "Tên món", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel ctModel = new DefaultTableModel(ctCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable ctTable = new JTable(ctModel);
        ctTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ctTable.setRowHeight(28);
        ctTable.setGridColor(new Color(235, 235, 235));
        ctTable.setShowVerticalLines(false);
        ctTable.setBackground(Color.WHITE);
        ctTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createTitledBorder("CHI TIẾT HÓA ĐƠN"));
        rightPanel.add(infoGrid, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(ctTable), BorderLayout.CENTER);

        // -- Button row at bottom
        JButton btnViewDetail = new JButton("XEM CHI TIẾT");
        btnViewDetail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnViewDetail.setBackground(GOLD_COLOR);
        btnViewDetail.setForeground(MAIN_BLUE);
        btnViewDetail.setFocusPainted(false);

        JButton btnClose = new JButton("ĐÓNG");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnRow.setBackground(Color.WHITE);
        btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        btnRow.add(btnViewDetail);
        btnRow.add(btnClose);

        // -- Fill detail when invoice row selected
        ivTable.getSelectionModel().addListSelectionListener(e2 -> {
            if (e2.getValueIsAdjusting()) return;
            int r2 = ivTable.getSelectedRow();
            if (r2 == -1) return;
            String hdId = ivModel.getValueAt(r2, 0).toString();
            HoaDon hd = hd_dao.getHoaDonByMa(hdId);
            List<ChiTietHoaDon> dsCT = ct_dao.getChiTietByMaHD(hdId);
            if (hd == null) return;
            lblMaHD.setText("Mã HĐ: " + hd.getMaHD());
            lblNgay.setText("Ngày lập: " + (hd.getNgayLap() != null ? dtFmt.format(hd.getNgayLap()) : ""));
            lblNV.setText("Nhân viên: " + (hd.getNhanVien() != null
                    ? hd.getNhanVien().getMaNV() + " - " + hd.getNhanVien().getTenNV() : ""));
            lblKH.setText("Khách hàng: " + (hd.getKhachHang() != null ? hd.getKhachHang().getMaKH() : ""));
            lblTong.setText("Tổng tiền: " + String.format("%,.0fđ", hd.getTongTien()));

            double hdCoc = hd.getTienCoc();
            double hdTongCong = hd.getTongTien() - hdCoc;
            lblCoc.setText("Tiền cọc: " + String.format("%,.0fđ", hdCoc));
            lblTongCong.setText("Tổng cộng: " + String.format("%,.0fđ", hdTongCong));
            lblTongCong.setForeground(hdTongCong < 0 ? RED_STATUS : TEXT_DARK);

            boolean paid = hd.isTrangThai();
            lblTT.setText("Trạng thái: " + (paid ? "Đã thanh toán" : "Chưa thanh toán"));
            lblTT.setForeground(paid ? GREEN_STATUS : Color.decode("#E67E22"));

            ctModel.setRowCount(0);
            if (dsCT != null) {
                int stt = 1;
                for (ChiTietHoaDon ct : dsCT) {
                    ctModel.addRow(new Object[]{stt++,
                            ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "",
                            ct.getSoLuong(),
                            String.format("%,.0fđ", ct.getDonGia()),
                            String.format("%,.0fđ", ct.getThanhTien())});
                }
            }
        });

        btnViewDetail.addActionListener(e -> {
            int r2 = ivTable.getSelectedRow();
            if (r2 == -1) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn để xem chi tiết!"); return; }
            String hdId = ivModel.getValueAt(r2, 0).toString();
            HoaDon hd = hd_dao.getHoaDonByMa(hdId);
            if (hd != null) {
                List<ChiTietHoaDon> dsCT = ct_dao.getChiTietByMaHD(hdId);
                // Gọi class InvoiceDialog hiện có của bạn
                new QLDB_GoiMon(this, hd, dsCT, true).setVisible(true);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(320);
        split.setDividerSize(4);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
        add(btnRow, BorderLayout.SOUTH);

        if (!dsHD.isEmpty()) {
            ivTable.setRowSelectionInterval(0, 0);
        }
    }

    private JLabel mkDetailLabel() {
        JLabel l = new JLabel();
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_DARK);
        return l;
    }
}