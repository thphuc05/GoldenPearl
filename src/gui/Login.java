package src.gui;
import com.formdev.flatlaf.*;
import lib.FontLoader;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;


public class Login {
    // Cửa sổ chính
    private JFrame frame = new JFrame();

    // Khung đăng nhập - override để có khung trong
    private JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            // Tạo thành phần G2D để có chất lượng tốt hơn
            Graphics2D g2d = (Graphics2D) g.create();

            // Bật khử răng
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Cài đặt màu
            g2d.setColor(new Color(255, 255, 255, 153));

            // Draw a rounded rectangle to match your image style
            // (x, y, width, height, arcWidth, arcHeight)
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            g2d.dispose();
            super.paintComponent(g);
        }
    };

    // Label
    private JLabel screenTitle = new JLabel("Trình quản lý nhà ");
    private JLabel restaurantName = new JGradientLabel("GOLDEN PEARL");
    private JLabel userNameLabel = new JLabel("Tên đăng nhập");
    private JLabel passwordLabel = new JLabel("Mật khẩu");

    // Trường thông tin
    private JTextField txtUsername = new JTextField(30); // Trường tên người dùng
    private JPasswordField txtPassword = new JPasswordField(30); // Trường mật khẩu

    // Nút đăng nhập chính
    private JButton btnLogin = new JButton("ĐĂNG NHẬP"); // Nút đăng nhập
    private JButton btnForget = new JButton("QUÊN MẬT KHẨU");  // Nút quên mật

    // Nút ở trang reset mật khau
    private JButton btnBack = new JButton("Quay lại"); //Quay lai

    // Thông số mặc định - nếu chỉ chạy trên một model IPad
    private static final int SCREEN_WIDTH = 1180; // Chiều dài màn hình
    private static final int SCREEN_HEIGHT = 820; // Chiều cao màn hình

    // Constructor
    Login() {
        // Thêm font
        FontLoader.registerFont("GoldenPearl/data/fonts/InstrumentSerif-Regular.ttf");
        FontLoader.registerFont("GoldenPearl/data/fonts/Inter-Medium.otf");
        FontLoader.registerFont("GoldenPearl/data/fonts/Inter-Bold.otf");

        FlatLaf.registerCustomDefaultsSource(new File("GoldenPearl/themes/DefaultTheme.properties"));
        try {
            // Đọc file properties (style) và gán thành map
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("GoldenPearl/themes/DefaultTheme.properties")) {
                props.load(fis);
            }

            // Chuyển các thuộc tính trong file và Flatlaf hiểu
            com.formdev.flatlaf.FlatLaf.setGlobalExtraDefaults((Map) props);

            System.out.println("✅ Style mặc định áp dụng");
        } catch (Exception e) {
            System.err.println("❌ Thất bại trong việc tải thuộc tính: " + e.getMessage());
        }

        // Sau khi load được thuộc tính, setup
        FlatLightLaf.setup();

        // Cửa sổ phần mềm
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        frame.setResizable(false); // Khoá cứng size của màn hình
        frame.getContentPane().setLayout(null); // Tắt layout tự động, tất cả các nút cần phải tự chỉnh vị trí

        // Background
        JPanelWithBackground bg = null;
        try {
            bg = new JPanelWithBackground("GoldenPearl/data/image/LoginBG.jpg");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (bg != null) {
            // ⛔ CẦN PHẢI SET KÍCH THƯỚC MÀN HÌNH THỦ CÔNG (do setLayout(null) của frame)
            bg.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            // Chuyển bg thành null để có thể nhét panel
            bg.setLayout(null);

            // Thêm bg vào panel
            frame.getContentPane().add(bg);
        }

        // Panel đăng
        int panelWidth = 704;
        int panelHeight = 300;
        panel.setOpaque(false);
        panel.setLayout(null);
        int x = (SCREEN_WIDTH - panelWidth) / 2;
        int y = (SCREEN_HEIGHT - panelHeight) / 2 + 30;
        panel.setBounds(x, y, panelWidth, panelHeight);

        // Tiêu đề màn hình - màn hình dăng
        screenTitle.setBounds((SCREEN_WIDTH - 360) / 2, 100, 390, 45);
        screenTitle.setFont(new Font("Inter Bold", Font.BOLD, 35));
        bg.add(screenTitle);

        // Tên nhà hàng
        restaurantName.setBounds((SCREEN_WIDTH - 700) / 2, 135, 750, 120);
        restaurantName.setFont(new Font("Instrument Serif Regular", Font.BOLD, 120));
        bg.add(restaurantName);

        // Labels (bên trái)
        userNameLabel.setBounds(50, 50, 250, 50);  // "Số Điện Thoại"
        userNameLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));
        passwordLabel.setBounds(135, 128, 250, 50); // "Mã xác nhận"
        passwordLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));

        // Field thông tin (bên )
        txtUsername.setBounds(300, 50, 350, 50);
        txtUsername.setFont(new Font("Inter Medium", Font.PLAIN, 23));
        txtPassword.setBounds(300, 130, 350, 50);

        // Nút
        btnForget.setBounds(100, 220, 236, 50);    // "QUAY LẠI"
        btnForget.setBackground(Color.BLACK);
        btnLogin.setBounds(410, 220 , 181, 50);   // "ĐĂNG NHẬP"

        // Panel add
        panel.add(userNameLabel);
        panel.add(txtUsername);
        panel.add(passwordLabel);
        panel.add(txtPassword);
        panel.add(btnForget);
        panel.add(btnLogin);

        bg.add(panel);
        // ⛔ NẾU NHƯ CÓ DÙNG FLATLAF VÀ SETLAYOUT(NULL), CẦN CÓ THÊM CÁI NÀY ĐỂ NÓ CẬP NHẬT LẠI TRANG
        SwingUtilities.updateComponentTreeUI(frame);
        frame.setVisible(true);
    }

    // Main
    public static void main(String[] args) {
        Login login = new Login();
    }

    // Panel với background
    public class JPanelWithBackground extends JPanel {
        private final Image backgroundImage; // Final do chỉ dùng cho màn hình đăng nhập

        public JPanelWithBackground(String fileName) throws IOException {
            backgroundImage = ImageIO.read(new File(fileName));
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(backgroundImage, 0, 0, this);
        }
    }

    // Gradient cho label
    public class JGradientLabel extends JLabel {
        private Color color1 = Color.decode("#FF4B2B");
        private Color color2 = Color.decode("#FFAD06");

        public JGradientLabel(String text) {
            super(text);
        }

        public void setGradient(Color c1, Color c2) {
            this.color1 = c1;
            this.color2 = c2;
            repaint();
        }

        @Override
        // Override để có thể thêm gradient cho label
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Lấy thuộc tính font để căn
            FontMetrics fm = g2.getFontMetrics();
            int x = getInsets().left;
            int y = fm.getAscent();

            // Gradient theo chiều ngang
            GradientPaint gp = new GradientPaint(
                    0, 0, color1,
                    getWidth(), 0, color2
            );

            // Áp đặt gradient cho mỗi chữ
            g2.setPaint(gp);

            // drawString: In chữ với gradient
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }
}
