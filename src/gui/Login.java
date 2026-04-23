package gui;

import com.formdev.flatlaf.*;
import lib.FontLoader;
import connectDB.ConnectDB;
import dao.TaiKhoan_DAO;
import entity.TaiKhoan;

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
    private JFrame frame = new JFrame("Hệ thống quản lý nhà hàng Golden Pearl");

    // Khung đăng nhập - override để có khung bo tròn trong suốt
    private JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Màu trắng với độ trong suốt 153 (~60%)
            g2d.setColor(new Color(255, 255, 255, 153));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2d.dispose();
            super.paintComponent(g);
        }
    };

    // Label
    private JLabel screenTitle = new JLabel("Trình quản lý nhà hàng");
    private JLabel restaurantName = new JGradientLabel("GOLDEN PEARL");
    private JLabel userNameLabel = new JLabel("Tên đăng nhập");
    private JLabel passwordLabel = new JLabel("Mật khẩu");

    // Trường thông tin
    private JTextField txtUsername = new JTextField(30);
    private JPasswordField txtPassword = new JPasswordField(30);

    // Nút chức năng
    private JButton btnLogin = new JButton("ĐĂNG NHẬP");
    private JButton btnForget = new JButton("QUÊN MẬT KHẨU");

    // Thông số màn hình cố định
    private static final int SCREEN_WIDTH = 1180;
    private static final int SCREEN_HEIGHT = 820;

    public Login() {
        initConfiguration();
        initUI();
        initEvents();
    }

    /**
     * Cấu hình Font và Theme (FlatLaf)
     */
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
                FlatLaf.registerCustomDefaultsSource(new File("themes/DefaultTheme.properties"));
                com.formdev.flatlaf.FlatLaf.setGlobalExtraDefaults((Map) props);
                System.out.println("✅ Style mặc định đã được áp dụng");
            }
        } catch (Exception e) {
            System.err.println("❌ Thất bại trong việc tải thuộc tính giao diện: " + e.getMessage());
        }

        // Khởi tạo FlatLaf
        FlatLightLaf.setup();
    }

    /**
     * Thiết lập giao diện (giữ nguyên layout cũ)
     */
    private void initUI() {
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // Căn giữa màn hình
        frame.getContentPane().setLayout(null);

        // Background
        JPanelWithBackground bg;
        try {
            bg = new JPanelWithBackground("data/image/LoginBG.jpg");
            bg.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            bg.setLayout(null);
            frame.getContentPane().add(bg);
        } catch (IOException e) {
            System.err.println("❌ Không tìm thấy ảnh nền: " + e.getMessage());
            bg = new JPanelWithBackground(); // Panel trống nếu lỗi ảnh
            bg.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            bg.setLayout(null);
            frame.getContentPane().add(bg);
        }

        // Tiêu đề (Phần chữ trên Panel)
        screenTitle.setBounds((SCREEN_WIDTH - 450) / 2, 100, 450, 45);
        screenTitle.setFont(new Font("Inter Bold", Font.BOLD, 35));
        screenTitle.setHorizontalAlignment(SwingConstants.CENTER);
        bg.add(screenTitle);

        restaurantName.setBounds((SCREEN_WIDTH - 750) / 2, 135, 750, 120);
        restaurantName.setFont(new Font("Instrument Serif Regular", Font.BOLD, 120));
        restaurantName.setHorizontalAlignment(SwingConstants.CENTER);
        bg.add(restaurantName);

        // Panel đăng nhập (Khung mờ)
        int panelWidth = 704;
        int panelHeight = 300;
        int x = (SCREEN_WIDTH - panelWidth) / 2;
        int y = (SCREEN_HEIGHT - panelHeight) / 2 + 30;
        
        panel.setOpaque(false);
        panel.setLayout(null);
        panel.setBounds(x, y, panelWidth, panelHeight);

        // Labels bên trong panel
        userNameLabel.setBounds(50, 50, 250, 50);
        userNameLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));
        passwordLabel.setBounds(50, 130, 250, 50);
        passwordLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));

        // Input Fields
        txtUsername.setBounds(300, 50, 350, 50);
        txtUsername.setFont(new Font("Inter Medium", Font.PLAIN, 23));
        txtPassword.setBounds(300, 130, 350, 50);

        // Buttons
        btnForget.setBounds(100, 220, 236, 50);
        btnForget.setBackground(Color.BLACK);
        btnForget.setForeground(Color.WHITE);
        
        btnLogin.setBounds(410, 220, 181, 50);

        // Add components to panel
        panel.add(userNameLabel);
        panel.add(txtUsername);
        panel.add(passwordLabel);
        panel.add(txtPassword);
        panel.add(btnForget);
        panel.add(btnLogin);

        bg.add(panel);

        // Cập nhật UI và hiển thị
        SwingUtilities.updateComponentTreeUI(frame);
        frame.setVisible(true);
    }

    /**
     * Khởi tạo xử lý sự kiện
     */
    private void initEvents() {
        // Kết nối Database khi khởi tạo
        try {
            ConnectDB.getInstance().connect();
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(frame, 
                "❌ Không thể kết nối SQL Server!\n" +
                "Vui lòng kiểm tra:\n" +
                "1. SQL Server đã được chạy chưa?\n" +
                "2. Mật khẩu 'sa' trong ConnectDB.java đã đúng chưa?\n" +
                "3. Database 'GoldenPearlDB' đã được tạo chưa?\n\n" +
                "Chi tiết lỗi: " + e.getMessage(), 
                "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
        }

        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());
            
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            // Disable buttons and show loading
            btnLogin.setEnabled(false);
            btnLogin.setText("ĐANG XỬ LÝ...");

            new SwingWorker<TaiKhoan, Void>() {
                @Override
                protected TaiKhoan doInBackground() throws Exception {
                    // This runs on a separate thread
                    TaiKhoan_DAO taiKhoan_dao = new TaiKhoan_DAO();
                    return taiKhoan_dao.checkLogin(user, pass);
                }

                @Override
                protected void done() {
                    // This runs back on the UI thread
                    try {
                        TaiKhoan tk = get();
                        if (tk != null) {
                            JOptionPane.showMessageDialog(frame, "Đăng nhập thành công!");
                            frame.dispose();
                            new TrangChu(tk).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Tên đăng nhập hoặc mật khẩu sai!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Lỗi kết nối database: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("ĐĂNG NHẬP");
                    }
                }
            }.execute();
        });

        btnForget.addActionListener(e -> {
            frame.dispose();
            new ForgotPassword().setVisible(true);
        });
    }

    public static void main(String[] args) {
        // Chạy giao diện trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(Login::new);
    }

    // --- INNER CLASSES ---

    public class JPanelWithBackground extends JPanel {
        private Image backgroundImage;

        public JPanelWithBackground(String fileName) throws IOException {
            backgroundImage = ImageIO.read(new File(fileName));
        }
        
        public JPanelWithBackground() {} // Constructor dự phòng

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

        public JGradientLabel(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2; // Căn giữa chữ
            int y = fm.getAscent();

            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
            g2.setPaint(gp);
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}
