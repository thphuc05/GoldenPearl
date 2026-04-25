package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Objects;
import dao.NhanVien_DAO;
import entity.NhanVien;
import entity.TaiKhoan;
import javax.swing.*;
import java.awt.*;

public class TrangChu extends JFrame {
    private JPanel sidebar;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JButton lastSelectedButton;
    private TaiKhoan taiKhoan;
    private NhanVien nhanVien;

    // Màu sắc chủ đạo theo ảnh
    private final Color MAIN_BLUE = Color.decode("#0B3D59"); // Xanh đậm nền chính
    private final Color GOLD_COLOR = Color.decode("#C5A059"); // Vàng đồng
    private final Color SIDEBAR_BG = Color.decode("#F0F4F8"); // Xám xanh nhạt sidebar
    private final Color USER_INFO_BG = Color.decode("#1A4D6D"); // Xanh cho panel user
    private final Color TEXT_WHITE = Color.WHITE;
    private final Color TEXT_DARK_BLUE = Color.decode("#0B3D59");

    public TrangChu(TaiKhoan tk) {
        super("Hệ thống quản lý nhà hàng Golden Pearl");
        this.taiKhoan = tk;
        if (tk != null) {
            this.nhanVien = new NhanVien_DAO().getNhanVienByMaTK(tk.getMaTK());
        }
        loadFonts();
        initUI();
    }

    public TrangChu() {
        this(null);
    }
    private void loadFonts() {
        lib.FontLoader.registerFont("data/fonts/InstrumentSerif-Regular.ttf");
        lib.FontLoader.registerFont("data/fonts/Inter-Bold.otf");
        lib.FontLoader.registerFont("data/fonts/Inter-Medium.otf");
    }

    private void initUI() {
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Sidebar
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Content Area
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(MAIN_BLUE);
        contentArea.add(createWelcomePanel(), "TrangChủ");
        contentArea.add(new QuanLyDatBan(), "ĐặtBàn");
        contentArea.add(new QuanLyHoaDon(), "HóaĐơn");
        contentArea.add(new QuanLyNhanVien(), "NhânViên");
        contentArea.add(new QuanLyKhachHang(), "KháchHàng");
        contentArea.add(new QuanLyMonAn(), "MónĂn");
        contentArea.add(new QuanLyThongKe(), "ThốngKê");

        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(SIDEBAR_BG);
        panel.setPreferredSize(new Dimension(350, getHeight()));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 0, 0, 20)));

        // User Info Panel
        JPanel userOuterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        userOuterPanel.setOpaque(false);
        JPanel userPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(USER_INFO_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(GOLD_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        userPanel.setPreferredSize(new Dimension(310, 120));
        userPanel.setLayout(new BorderLayout(15, 0));
        userPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        userPanel.setOpaque(false);

        JLabel lblUserIcon = new JLabel(getScaledIcon("data/icons/people_300dp_FFFFFF.png", 60, 60)); // Placeholder for user icon
        JPanel userInfo = new JPanel(new GridLayout(3, 1));
        userInfo.setOpaque(false);

        String vaiTroStr = (taiKhoan != null) ? taiKhoan.getVaiTro() : "N/A";
        String tenTKStr = (taiKhoan != null) ? taiKhoan.getTenTK() : "Chưa đăng nhập";

        JLabel lblRole = new JLabel("Vai trò: " + vaiTroStr);
        lblRole.setForeground(new Color(255, 255, 255, 200));
        lblRole.setFont(new Font("Inter", Font.PLAIN, 14));
        JLabel lblName = new JLabel(tenTKStr);
        lblName.setForeground(TEXT_WHITE);
        lblName.setFont(new Font("Inter Bold", Font.BOLD, 18));

        JButton btnLogoutSmall = new JButton("Đăng xuất");
        btnLogoutSmall.setFont(new Font("Inter", Font.PLAIN, 12));
        btnLogoutSmall.setForeground(new Color(255, 255, 255, 180));
        btnLogoutSmall.setBackground(new Color(255, 255, 255, 20));
        btnLogoutSmall.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50)));
        btnLogoutSmall.setFocusPainted(false);
        btnLogoutSmall.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogoutSmall.addActionListener(e -> {
            dispose();
            new Login();
        });
        userInfo.add(lblRole);
        userInfo.add(lblName);
        userInfo.add(btnLogoutSmall);
        userPanel.add(lblUserIcon, BorderLayout.WEST);
        userPanel.add(userInfo, BorderLayout.CENTER);
        userOuterPanel.add(userPanel);
        panel.add(userOuterPanel);
        panel.add(Box.createVerticalStrut(10));

        // Sidebar Buttons
        JButton btnHome = createSidebarButton("Trang chủ", "home_filled_300dp_FFFFFF.png", e -> showCard("TrangChủ", (JButton)e.getSource()));
        panel.add(btnHome);
        lastSelectedButton = btnHome;
        btnHome.setBackground(Color.WHITE);
        btnHome.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, GOLD_COLOR),
                BorderFactory.createEmptyBorder(0, 15, 0, 10)
        ));

        panel.add(createSidebarButton("Đặt bàn", "menu_open_300dp_FFFFFF.png", e -> showCard("ĐặtBàn", (JButton)e.getSource())));
        panel.add(createSidebarButton("Quản lý hoá đơn", "receipt_300dp_FFFFFF.png", e -> showCard("HóaĐơn", (JButton)e.getSource())));
        panel.add(createSidebarButton("Quản lý nhân viên", "badge_300dp_FFFFFF.png", e -> showCard("NhânViên", (JButton)e.getSource())));
        panel.add(createSidebarButton("Quản lý khách hàng", "people_300dp_FFFFFF.png", e -> showCard("KháchHàng", (JButton)e.getSource())));
        panel.add(createSidebarButton("Quản lý món ăn", "dinner_dining_300dp_FFFFFF.png", e -> showCard("MónĂn", (JButton)e.getSource())));
        panel.add(createSidebarButton("Thống kê doanh thu", "attach_money_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png", e -> showCard("ThốngKê", (JButton)e.getSource())));

        panel.add(Box.createVerticalGlue());
        JButton btnLogout = createSidebarButton("Đăng xuất", "attach_money_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png", e -> {
            dispose();
            new Login();
        });
        panel.add(btnLogout);
        panel.add(Box.createVerticalStrut(20));

        return panel;

    }

    private void showCard(String cardName, JButton source) {
        cardLayout.show(contentArea, cardName);
        if (lastSelectedButton != null) {
            lastSelectedButton.setBackground(SIDEBAR_BG);
            lastSelectedButton.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
        }
        source.setBackground(Color.WHITE);
        source.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, GOLD_COLOR),
                BorderFactory.createEmptyBorder(0, 15, 0, 10)
        ));
        lastSelectedButton = source;
    }

    private JButton createSidebarButton(String text, String iconName, ActionListener action) {
        JButton btn = new JButton(text);
        // Tint the white icon to dark blue for the light sidebar
        ImageIcon icon = getScaledIcon("data/icons/" + iconName, 24, 24);
        if (icon != null) {
            btn.setIcon(tintIcon(icon, TEXT_DARK_BLUE));
        }
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btn.setPreferredSize(new Dimension(350, 55));
        btn.setFont(new Font("Inter Medium", Font.PLAIN, 16));
        btn.setForeground(TEXT_DARK_BLUE);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(20);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != lastSelectedButton) btn.setBackground(new Color(255, 255, 255, 180));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != lastSelectedButton) btn.setBackground(SIDEBAR_BG);
            }
        });

        btn.addActionListener(action);
        return btn;
    }

    private ImageIcon tintIcon(ImageIcon icon, Color color) {
        Image img = icon.getImage();
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.setComposite(AlphaComposite.SrcAtop);
        g2d.setColor(color);
        g2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        g2d.dispose();
        return new ImageIcon(bufferedImage);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BLUE);

        // Header Section
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 20, 50));

        // Logo & Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        titlePanel.setOpaque(false);
        // Placeholder for the shell logo - using text or a generic icon if needed
        JLabel lblLogo = new JLabel(getScaledIcon("data/icons/star_half_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png", 60, 60)); // Should be shell logo
        JLabel lblTitle = new JLabel("GOLDEN PEARL RESTAURANT");
        lblTitle.setFont(new Font("Instrument Serif", Font.PLAIN, 56));
        lblTitle.setForeground(GOLD_COLOR);
        titlePanel.add(lblLogo);
        titlePanel.add(lblTitle);
        headerPanel.add(titlePanel);

        headerPanel.add(Box.createVerticalStrut(10));

        JLabel lblSlogan = new JLabel("Nơi tinh hoa ẩm thực hội tụ - Nâng tầm trải nghiệm thượng lưu", SwingConstants.CENTER);
        lblSlogan.setFont(new Font("Inter", Font.ITALIC, 22));
        lblSlogan.setForeground(GOLD_COLOR);
        lblSlogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(lblSlogan);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainContent = new JPanel();
        mainContent.setOpaque(false);
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(BorderFactory.createEmptyBorder(0, 80, 40, 80));

        // Intro Text
        JTextArea txtIntro = new JTextArea(
                "Nhà hàng Golden Pearl xin chào quý đối tác và đội ngũ quản lý. " +
                        "Với sứ mệnh mang đến không gian ẩm thực tinh tế bậc nhất, hệ thống quản lý này " +
                        "được thiết kế để tối ưu hóa quy trình vận hành và nâng cao chất lượng dịch vụ. " +
                        "Chúc bạn có một ngày làm việc hiệu quả và tràn đầy năng lượng cùng Golden Pearl!"
        );
        txtIntro.setFont(new Font("Inter", Font.PLAIN, 18));
        txtIntro.setEditable(false);
        txtIntro.setLineWrap(true);
        txtIntro.setWrapStyleWord(true);
        txtIntro.setOpaque(false);
        txtIntro.setForeground(TEXT_WHITE);
        txtIntro.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtIntro.setMaximumSize(new Dimension(900, 150));
        mainContent.add(txtIntro);
        mainContent.add(Box.createVerticalStrut(50));

        // Grid Functions
        JPanel functionsPanel = new JPanel(new BorderLayout());
        functionsPanel.setOpaque(false);

        JLabel lblFunctionsTitle = new JLabel("HỆ THỐNG CHỨC NĂNG");
        lblFunctionsTitle.setFont(new Font("Inter Bold", Font.BOLD, 28));
        lblFunctionsTitle.setForeground(TEXT_WHITE);
        lblFunctionsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        functionsPanel.add(lblFunctionsTitle, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 30, 30));
        gridPanel.setOpaque(false);

        gridPanel.add(createLuxuryPillButton("ĐẶT BÀN", "menu_open_300dp_FFFFFF.png", "ĐặtBàn"));
        gridPanel.add(createLuxuryPillButton("QUẢN LÝ MÓN ĂN", "dinner_dining_300dp_FFFFFF.png", "MónĂn"));
        gridPanel.add(createLuxuryPillButton("QUẢN LÝ HÓA ĐƠN", "receipt_300dp_FFFFFF.png", "HóaĐơn"));
        gridPanel.add(createLuxuryPillButton("QUẢN LÝ NHÂN VIÊN", "badge_300dp_FFFFFF.png", "NhânViên"));
        gridPanel.add(createLuxuryPillButton("QUẢN LÝ KHÁCH HÀNG", "people_300dp_FFFFFF.png", "KháchHàng"));
        gridPanel.add(createLuxuryPillButton("THỐNG KÊ DOANH THU", "attach_money_300dp_FFFFFF_FILL0_wght400_GRAD0_opsz48.png", "ThốngKê"));

        functionsPanel.add(gridPanel, BorderLayout.CENTER);
        mainContent.add(functionsPanel);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createLuxuryPillButton(String text, String iconName, String cardName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(MAIN_BLUE.brighter());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                } else {
                    g2.setColor(MAIN_BLUE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(GOLD_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter Medium", Font.BOLD, 16));
        btn.setForeground(TEXT_WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setIcon(getScaledIcon("data/icons/" + iconName, 24, 24));
        btn.setIconTextGap(15);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(280, 80));

        btn.addActionListener(e -> cardLayout.show(contentArea, cardName));
        return btn;
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(MAIN_BLUE);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter Bold", Font.BOLD, 26));
        label.setForeground(TEXT_WHITE);
        panel.add(label);
        return panel;
    }

    private ImageIcon getScaledIcon(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new TrangChu().setVisible(true);
        });
    }
}
