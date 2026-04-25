package gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import lib.FontLoader;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ForgotPassword extends JFrame {
    // Khung mờ bo tròn (giống Login)
    private JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(255, 255, 255, 153));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2d.dispose();
            super.paintComponent(g);
        }
    };

    private JLabel screenTitle = new JLabel("Khôi phục mật khẩu");
    private JLabel restaurantName = new JGradientLabel("GOLDEN PEARL");
    private JLabel phoneLabel = new JLabel("Số điện thoại");
    private JLabel otpLabel = new JLabel("Mã xác nhận");

    private JTextField txtPhone = new JTextField(30);
    private JTextField txtOTP = new JTextField(30);

    private JButton btnSendOTP = new JButton("GỬI MÃ");
    private JButton btnConfirm = new JButton("XÁC NHẬN");
    private JButton btnBack = new JButton("QUAY LẠI");

    private static final int SCREEN_WIDTH = 1180;
    private static final int SCREEN_HEIGHT = 820;

    public ForgotPassword() {
        super("Quên mật khẩu - Golden Pearl");
        initConfiguration();
        initUI();
        initEvents();
    }

    private void initConfiguration() {
        FontLoader.registerFont("data/fonts/InstrumentSerif-Regular.ttf");
        FontLoader.registerFont("data/fonts/Inter-Medium.otf");
        FontLoader.registerFont("data/fonts/Inter-Bold.otf");

        try {
            Properties props = new Properties();
            File themeFile = new File("themes/DefaultTheme.properties");
            if (themeFile.exists()) {
                try (FileInputStream fis = new FileInputStream(themeFile)) {
                    props.load(fis);
                }
                FlatLaf.registerCustomDefaultsSource(themeFile);
                com.formdev.flatlaf.FlatLaf.setGlobalExtraDefaults((Map) props);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FlatLightLaf.setup();
    }

    private void initUI() {
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Phóng to toàn màn hình
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        getContentPane().setLayout(new BorderLayout());

        // Background
        JPanelWithBackground bg;
        try {
            bg = new JPanelWithBackground("data/image/LoginBG.jpg");
        } catch (IOException e) {
            bg = new JPanelWithBackground();
        }
        bg.setLayout(new GridBagLayout()); // Dùng GridBagLayout để căn giữa
        getContentPane().add(bg, BorderLayout.CENTER);

        // Container chính
        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        // Tiêu đề
        screenTitle.setFont(new Font("Inter Bold", Font.BOLD, 35));
        screenTitle.setForeground(Color.BLACK);
        screenTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        screenTitle.setHorizontalAlignment(SwingConstants.CENTER);

        restaurantName.setFont(new Font("Instrument Serif Regular", Font.BOLD, 120));
        restaurantName.setAlignmentX(Component.CENTER_ALIGNMENT);
        restaurantName.setHorizontalAlignment(SwingConstants.CENTER);
        restaurantName.setPreferredSize(new Dimension(800, 130));
        restaurantName.setMaximumSize(new Dimension(800, 130));

        // Panel chính
        int panelWidth = 704;
        int panelHeight = 350;
        panel.setOpaque(false);
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        panel.setMaximumSize(new Dimension(panelWidth, panelHeight));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nội dung bên trong panel
        phoneLabel.setBounds(50, 50, 250, 50);
        phoneLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));
        phoneLabel.setForeground(Color.BLACK);
        txtPhone.setBounds(300, 50, 350, 50);
        txtPhone.setFont(new Font("Inter Medium", Font.PLAIN, 23));

        otpLabel.setBounds(50, 130, 250, 50);
        otpLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));
        otpLabel.setForeground(Color.BLACK);
        txtOTP.setBounds(300, 130, 230, 50);
        txtOTP.setFont(new Font("Inter Medium", Font.PLAIN, 23));

        btnSendOTP.setBounds(540, 130, 110, 50);
        btnSendOTP.setFont(new Font("Inter Bold", Font.BOLD, 14));

        // Nút chức năng phía dưới
        btnBack.setBounds(100, 240, 236, 50);
        btnBack.setBackground(Color.BLACK);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Inter Bold", Font.BOLD, 20));

        btnConfirm.setBounds(410, 240, 181, 50);
        btnConfirm.setFont(new Font("Inter Bold", Font.BOLD, 20));

        panel.add(phoneLabel);
        panel.add(txtPhone);
        panel.add(otpLabel);
        panel.add(txtOTP);
        panel.add(btnSendOTP);
        panel.add(btnBack);
        panel.add(btnConfirm);

        centerContainer.add(screenTitle);
        centerContainer.add(restaurantName);
        centerContainer.add(Box.createVerticalStrut(20));
        centerContainer.add(panel);

        bg.add(centerContainer, new GridBagConstraints());

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void initEvents() {
        btnBack.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(Login::new);
        });

        btnConfirm.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Tính năng xác nhận đang được xây dựng!");
        });
    }

    // --- Inner Classes ---
    public class JPanelWithBackground extends JPanel {
        private Image backgroundImage;
        public JPanelWithBackground(String fileName) throws IOException {
            backgroundImage = ImageIO.read(new File(fileName));
        }
        public JPanelWithBackground() {}
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public class JGradientLabel extends JLabel {
        private Color color1 = Color.decode("#FF4B2B");
        private Color color2 = Color.decode("#FFAD06");
        public JGradientLabel(String text) { super(text); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = fm.getAscent();
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
            g2.setPaint(gp);
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}
