package gui;

import com.formdev.flatlaf.FlatLaf;
import lib.FontLoader;
import themes.DefaultTheme;

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
            UIManager.put("Label.foreground", Color.WHITE);
            Properties props = new Properties();
            File themeFile = new File("themes/DefaultTheme.properties");
            if (themeFile.exists()) {
                try (FileInputStream fis = new FileInputStream(themeFile)) {
                    props.load(fis);
                }
                FlatLaf.registerCustomDefaultsSource(themeFile);
                FlatLaf.setGlobalExtraDefaults((Map) props);
            }
            DefaultTheme.setup(); //
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        screenTitle.setBounds((SCREEN_WIDTH - 450) / 2, 220, 450, 45);
        screenTitle.setFont(new Font("Inter Bold", Font.BOLD, 35));
        screenTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        screenTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // Panel chính
        int panelWidth = 704;
        int panelHeight = 300;
        int x = (SCREEN_WIDTH - panelWidth) / 2;
        int y = (SCREEN_HEIGHT - panelHeight) / 2 + 30;

        panel.setOpaque(false);
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        panel.setMaximumSize(new Dimension(panelWidth, panelHeight));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nội dung bên trong panel
        phoneLabel.setBounds(50, 50, 250, 50);
        phoneLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));
        txtPhone.setBounds(300, 50, 350, 50);
        txtPhone.setFont(new Font("Inter Medium", Font.PLAIN, 23));

        otpLabel.setBounds(50, 130, 250, 50);
        otpLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));
        txtOTP.setBounds(300, 130, 230, 50);
        txtOTP.setFont(new Font("Inter Medium", Font.PLAIN, 23));

        btnSendOTP.setBounds(540, 130, 110, 50);
        btnSendOTP.setFont(new Font("Inter Bold", Font.BOLD, 14));
        btnSendOTP.setForeground(Color.WHITE);
        btnSendOTP.setBackground(Color.decode("#FF5F1F"));

        // Nút chức năng phía dưới
        btnBack.setBounds(100, 220, 236, 50);
        btnBack.setBackground(Color.BLACK);
        btnBack.setForeground(Color.WHITE);

        btnConfirm.setBounds(410, 220, 181, 50);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setBackground(Color.decode("#FF5F1F"));

        // panel.add(screenTitle); - Tắt do kích cỡ màn hình chưa là cuối cùng
        panel.add(phoneLabel);
        panel.add(txtPhone);
        panel.add(otpLabel);
        panel.add(txtOTP);
        panel.add(btnSendOTP);
        panel.add(btnBack);
        panel.add(btnConfirm);

        bg.add(panel);
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
}
