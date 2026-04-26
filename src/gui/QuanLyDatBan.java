package gui;

import entity.*;
import dao.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class QuanLyDatBan extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel tableGrid;

    // DAOs
    private Ban_DAO ban_dao = new Ban_DAO();
    private KhachHang_DAO khachHang_dao = new KhachHang_DAO();
    private DonDatBan_DAO donDatBan_dao = new DonDatBan_DAO();
    private SanPham_DAO sanPham_dao = new SanPham_DAO();

    // UI Components
    private JPanel tableSelectionPanel, orderSelectionPanel;
    private JPanel itemGrid;
    private JLabel lblTableID;
    private JTextField txtCustName, txtCustPhone, txtCustNote;
    private JButton btnConfirmBooking, btnCancel;

    // Image Colors
    private final Color BLUE_DARK = Color.decode("#2874A6");
    private final Color ORANGE_MAIN = Color.decode("#E67E22");
    private final Color GOLD_COLOR = Color.decode("#C5A059");
    private final Color BG_LIGHT = Color.decode("#F2F3F4");
    private final Color TEXT_GRAY = Color.decode("#7F8C8D");

    private Ban selectedBan = null;

    public QuanLyDatBan() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initTableSelectionPanel();
        initOrderSelectionPanel();

        mainPanel.add(tableSelectionPanel, "TableSelection");
        mainPanel.add(orderSelectionPanel, "OrderSelection");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "TableSelection");
    }

    private void initTableSelectionPanel() {
        tableSelectionPanel = new JPanel(new BorderLayout());
        tableSelectionPanel.setBackground(Color.decode("#0B3D59")); // Đồng bộ màu xanh chính
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 80));
        JLabel title = new JLabel("HỆ THỐNG ĐẶT BÀN", SwingConstants.CENTER);
        title.setFont(new Font("Inter Bold", Font.BOLD, 32));
        title.setForeground(Color.decode("#C5A059")); // Màu vàng đồng đồng bộ
        header.add(title, BorderLayout.CENTER);
        tableSelectionPanel.add(header, BorderLayout.NORTH);

        tableGrid = new JPanel(new GridLayout(0, 6, 25, 25)); // Tăng lên 6 cột cho màn hình rộng
        tableGrid.setOpaque(false);
        tableGrid.setBorder(new EmptyBorder(30, 50, 30, 50));
        loadTables();

        JScrollPane scroll = new JScrollPane(tableGrid);
        scroll.setBorder(null);
        tableSelectionPanel.add(scroll, BorderLayout.CENTER);
    }

    private void initOrderSelectionPanel() {
        orderSelectionPanel = new JPanel(new BorderLayout());
        orderSelectionPanel.setBackground(Color.WHITE);

        // 1. Header Blue
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BLUE_DARK);
        header.setPreferredSize(new Dimension(0, 70));
        lblTableID = new JLabel("ĐANG PHỤC VỤ: BÀN SỐ 1", SwingConstants.CENTER);
        lblTableID.setFont(new Font("Inter Bold", Font.BOLD, 28));
        lblTableID.setForeground(Color.WHITE);
        header.add(lblTableID, BorderLayout.CENTER);
        orderSelectionPanel.add(header, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 2. Left: Menu Area (2 columns of horizontal cards like image)
        JPanel leftArea = new JPanel(new BorderLayout());
        leftArea.setOpaque(false);
        
        // Custom titled border with orange lines
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        JLabel lblMenuTitle = new JLabel("THỰC ĐƠN NHÀ HÀNG");
        lblMenuTitle.setFont(new Font("Inter Bold", Font.BOLD, 16));
        lblMenuTitle.setForeground(ORANGE_MAIN);
        titlePanel.add(lblMenuTitle);
        leftArea.add(titlePanel, BorderLayout.NORTH);

        itemGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        itemGrid.setOpaque(false);
        loadItems();

        JScrollPane itemScroll = new JScrollPane(itemGrid);
        itemScroll.setBorder(null);
        itemScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftArea.add(itemScroll, BorderLayout.CENTER);
        mainContent.add(leftArea, BorderLayout.CENTER);

        // 3. Right: Customer Form
        JPanel rightArea = new JPanel(new BorderLayout());
        rightArea.setPreferredSize(new Dimension(380, 0));
        rightArea.setOpaque(false);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblCustTitle = new JLabel("THÔNG TIN KHÁCH HÀNG");
        lblCustTitle.setFont(new Font("Inter Bold", Font.BOLD, 18));
        lblCustTitle.setForeground(BLUE_DARK);
        lblCustTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblCustTitle);
        form.add(Box.createVerticalStrut(20));

        form.add(createFieldLabel("Họ và Tên"));
        txtCustName = createStyledField("Trần Thị B");
        form.add(txtCustName);
        form.add(Box.createVerticalStrut(15));

        form.add(createFieldLabel("Số Điện Thoại"));
        txtCustPhone = createStyledField("0912345678");
        form.add(txtCustPhone);
        form.add(Box.createVerticalStrut(15));

        form.add(createFieldLabel("Ghi Chú Đặc Biệt"));
        txtCustNote = createStyledField("Không dị ứng");
        form.add(txtCustNote);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(40, 0, 0, 0));

        btnConfirmBooking = new JButton("XÁC NHẬN ĐẶT BÀN");
        btnConfirmBooking.setBackground(ORANGE_MAIN);
        btnConfirmBooking.setForeground(Color.WHITE);
        btnConfirmBooking.setFont(new Font("Inter Bold", Font.BOLD, 16));
        btnConfirmBooking.setPreferredSize(new Dimension(0, 50));
        btnConfirmBooking.setFocusPainted(false);
        btnConfirmBooking.setBorderPainted(false);

        btnCancel = new JButton("HỦY VÀ QUAY LẠI");
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(BLUE_DARK);
        btnCancel.setFont(new Font("Inter Bold", Font.BOLD, 16));
        btnCancel.setBorder(new LineBorder(BLUE_DARK, 2));
        btnCancel.setFocusPainted(false);

        btnConfirmBooking.addActionListener(e -> handleBooking());
        btnCancel.addActionListener(e -> cardLayout.show(mainPanel, "TableSelection"));

        btnPanel.add(btnConfirmBooking);
        btnPanel.add(btnCancel);

        rightArea.add(form, BorderLayout.NORTH);
        rightArea.add(btnPanel, BorderLayout.SOUTH);
        mainContent.add(rightArea, BorderLayout.EAST);

        orderSelectionPanel.add(mainContent, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter", Font.BOLD, 14));
        l.setForeground(Color.BLACK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    private JTextField createStyledField(String placeholder) {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        f.setPreferredSize(new Dimension(0, 45));
        f.setFont(new Font("Inter", Font.PLAIN, 15));

        Color defaultBorderColor = new Color(210, 210, 210);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(defaultBorderColor, 1),
            new EmptyBorder(0, 10, 0, 10)
        ));
        f.setForeground(TEXT_GRAY);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);

        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ORANGE_MAIN, 2),
                    new EmptyBorder(0, 10, 0, 10)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(defaultBorderColor, 1),
                    new EmptyBorder(0, 10, 0, 10)
                ));
            }
        });

        return f;
    }

    private void loadItems() {
        itemGrid.removeAll();
        List<SanPham> dsSP = sanPham_dao.getAllSanPham();
        for (SanPham sp : dsSP) {
            itemGrid.add(createHorizontalItemCard(sp));
        }
    }

    private JPanel createHorizontalItemCard(SanPham sp) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        card.setPreferredSize(new Dimension(350, 120));

        // Image Placeholder
        JPanel imgBox = new JPanel(new BorderLayout());
        imgBox.setPreferredSize(new Dimension(100, 0));
        imgBox.setBackground(new Color(245, 245, 245));
        imgBox.add(new JLabel("ẢNH", SwingConstants.CENTER));
        card.add(imgBox, BorderLayout.WEST);

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(10, 0, 10, 10));

        JLabel name = new JLabel(sp.getTenMon());
        name.setFont(new Font("Inter Bold", Font.BOLD, 17));
        
        JTextArea desc = new JTextArea("Mô tả món ăn thơm ngon, hấp dẫn phục vụ tại bàn...");
        desc.setFont(new Font("Inter", Font.PLAIN, 12));
        desc.setForeground(TEXT_GRAY);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);

        JLabel price = new JLabel(String.format("%,.0f", sp.getDonGia()));
        price.setFont(new Font("Inter Bold", Font.BOLD, 15));
        price.setForeground(ORANGE_MAIN);

        info.add(name);
        info.add(Box.createVerticalStrut(5));
        info.add(desc);
        info.add(Box.createVerticalGlue());
        
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(price, BorderLayout.WEST);
        
        JButton btnAdd = new JButton("+");
        btnAdd.setFont(new Font("Arial", Font.BOLD, 20));
        btnAdd.setForeground(ORANGE_MAIN);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottom.add(btnAdd, BorderLayout.EAST);
        
        info.add(bottom);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private void loadTables() {
        tableGrid.removeAll();
        List<Ban> dsBan = ban_dao.getAllBan();
        for (Ban ban : dsBan) {
            JButton b = new JButton("BÀN " + ban.getSoBan());
            b.setBackground(ORANGE_MAIN);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Inter Bold", Font.BOLD, 16));
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Thiết lập viền mặc định
            b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

            // Thêm hiệu ứng di chuột (hover)
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    b.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 4));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                }
            });

            b.addActionListener(e -> {
                selectedBan = ban;
                lblTableID.setText("ĐANG PHỤC VỤ: BÀN SỐ " + ban.getSoBan());
                cardLayout.show(mainPanel, "OrderSelection");
            });
            tableGrid.add(b);
        }
    }

    private void handleBooking() {
        if (txtCustName.getText().isEmpty() || txtCustPhone.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        ban_dao.updateTinhTrangBan(selectedBan.getMaBan(), TrangThaiBan.DaDuocDat);
        JOptionPane.showMessageDialog(this, "Đặt bàn thành công!");
        loadTables();
        cardLayout.show(mainPanel, "TableSelection");
    }
}
