package gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import dao.NhanVien_DAO;
import dao.TaiKhoan_DAO;
import entity.NhanVien;
import lib.FontLoader;
import util.EmailService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ForgotPassword extends JFrame {

    // ── Bước hiện tại: 1 = nhập email, 2 = nhập OTP + mật khẩu mới ──────────
    private int step = 1;
    private NhanVien foundNhanVien = null;

    // ── Panel khung mờ bo tròn ────────────────────────────────────────────────
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

    // ── Labels ────────────────────────────────────────────────────────────────
    private JLabel screenTitle    = new JLabel("Khôi phục mật khẩu");
    private JLabel restaurantName = new JGradientLabel("GOLDEN PEARL");
    private JLabel lblStep        = new JLabel("Bước 1 / 2 – Nhập email tài khoản");

    // ── Bước 1 ────────────────────────────────────────────────────────────────
    private JLabel     emailLabel  = new JLabel("Email");
    private JTextField txtEmail    = new JTextField(30);
    private JButton    btnSendOTP  = new JButton("GỬI MÃ OTP");

    // ── Bước 2 ────────────────────────────────────────────────────────────────
    private JLabel         otpLabel    = new JLabel("Mã OTP");
    private JTextField     txtOTP      = new JTextField(30);
    private JLabel         newPwLabel  = new JLabel("Mật khẩu mới");
    private JPasswordField txtNewPw    = new JPasswordField(30);
    private JLabel         confirmLabel = new JLabel("Xác nhận MK");
    private JPasswordField txtConfirm  = new JPasswordField(30);
    private JButton        btnConfirm  = new JButton("ĐỔI MẬT KHẨU");

    // ── Chung ─────────────────────────────────────────────────────────────────
    private JButton btnBack = new JButton("QUAY LẠI");

    public ForgotPassword() {
        super("Quên mật khẩu – Golden Pearl");
        initConfiguration();
        initUI();
        initEvents();
    }

    // ── Cấu hình font + theme ─────────────────────────────────────────────────
    private void initConfiguration() {
        FontLoader.registerFont("data/fonts/InstrumentSerif-Regular.ttf");
        FontLoader.registerFont("data/fonts/Inter-Medium.otf");
        FontLoader.registerFont("data/fonts/Inter-Bold.otf");
        try {
            Properties props = new Properties();
            File themeFile = new File("themes/DefaultTheme.properties");
            if (themeFile.exists()) {
                try (FileInputStream fis = new FileInputStream(themeFile)) { props.load(fis); }
                FlatLaf.registerCustomDefaultsSource(themeFile);
                com.formdev.flatlaf.FlatLaf.setGlobalExtraDefaults((Map) props);
            }
        } catch (Exception ignored) {}
        FlatLightLaf.setup();
    }

    // ── Giao diện ─────────────────────────────────────────────────────────────
    private void initUI() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        getContentPane().setLayout(new BorderLayout());

        JPanelWithBackground bg;
        try {
            bg = new JPanelWithBackground("data/image/Mẫu 1/LoginBG.jpg");
        } catch (IOException e) {
            bg = new JPanelWithBackground();
        }
        bg.setLayout(new GridBagLayout());
        getContentPane().add(bg, BorderLayout.CENTER);

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

        // Panel khung
        panel.setOpaque(false);
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(704, 380));
        panel.setMaximumSize(new Dimension(704, 380));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        buildStep1UI();

        centerContainer.add(screenTitle);
        centerContainer.add(restaurantName);
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(1, 20));
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        centerContainer.add(spacer);
        centerContainer.add(panel);

        bg.add(centerContainer, new GridBagConstraints());
        SwingUtilities.updateComponentTreeUI(this);
    }

    // ── Bước 1: nhập email ────────────────────────────────────────────────────
    private void buildStep1UI() {
        panel.removeAll();

        lblStep.setBounds(50, 15, 600, 30);
        lblStep.setFont(new Font("Inter Bold", Font.BOLD, 16));
        lblStep.setForeground(new Color(60, 120, 200));
        lblStep.setText("Bước 1 / 2 – Nhập email tài khoản");
        panel.add(lblStep);

        emailLabel.setBounds(50, 70, 250, 50);
        emailLabel.setFont(new Font("Inter Bold", Font.BOLD, 28));
        emailLabel.setForeground(Color.BLACK);
        panel.add(emailLabel);

        txtEmail.setBounds(300, 70, 350, 50);
        txtEmail.setFont(new Font("Inter Medium", Font.PLAIN, 22));
        panel.add(txtEmail);

        btnBack.setBounds(100, 280, 200, 50);
        btnBack.setBackground(Color.BLACK);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Inter Bold", Font.BOLD, 18));
        panel.add(btnBack);

        btnSendOTP.setBounds(360, 280, 240, 50);
        btnSendOTP.setFont(new Font("Inter Bold", Font.BOLD, 18));
        panel.add(btnSendOTP);

        panel.revalidate();
        panel.repaint();
    }

    // ── Bước 2: nhập OTP + mật khẩu mới ─────────────────────────────────────
    private void buildStep2UI(String email) {
        panel.removeAll();

        lblStep.setBounds(50, 15, 600, 30);
        lblStep.setFont(new Font("Inter Bold", Font.BOLD, 16));
        lblStep.setForeground(new Color(40, 160, 80));
        lblStep.setText("Bước 2 / 2 – OTP đã gửi tới: " + email);
        panel.add(lblStep);

        otpLabel.setBounds(50, 60, 240, 45);
        otpLabel.setFont(new Font("Inter Bold", Font.BOLD, 26));
        otpLabel.setForeground(Color.BLACK);
        panel.add(otpLabel);
        txtOTP.setBounds(300, 60, 350, 45);
        txtOTP.setFont(new Font("Inter Medium", Font.PLAIN, 22));
        panel.add(txtOTP);

        newPwLabel.setBounds(50, 125, 240, 45);
        newPwLabel.setFont(new Font("Inter Bold", Font.BOLD, 26));
        newPwLabel.setForeground(Color.BLACK);
        panel.add(newPwLabel);
        txtNewPw.setBounds(300, 125, 350, 45);
        panel.add(txtNewPw);

        confirmLabel.setBounds(50, 190, 240, 45);
        confirmLabel.setFont(new Font("Inter Bold", Font.BOLD, 26));
        confirmLabel.setForeground(Color.BLACK);
        panel.add(confirmLabel);
        txtConfirm.setBounds(300, 190, 350, 45);
        panel.add(txtConfirm);

        btnBack.setBounds(100, 295, 200, 50);
        panel.add(btnBack);

        btnConfirm.setBounds(360, 295, 240, 50);
        btnConfirm.setFont(new Font("Inter Bold", Font.BOLD, 18));
        panel.add(btnConfirm);

        panel.revalidate();
        panel.repaint();
    }

    // ── Sự kiện ───────────────────────────────────────────────────────────────
    private void initEvents() {
        btnBack.addActionListener(e -> {
            if (step == 2) {
                step = 1;
                foundNhanVien = null;
                buildStep1UI();
            } else {
                dispose();
                SwingUtilities.invokeLater(Login::new);
            }
        });

        btnSendOTP.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            if (email.isEmpty()) {
                warn("Vui lòng nhập địa chỉ email!"); return;
            }
            if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
                warn("Địa chỉ email không hợp lệ!"); return;
            }

            btnSendOTP.setEnabled(false);
            btnSendOTP.setText("ĐANG GỬI...");

            new SwingWorker<NhanVien, Void>() {
                @Override
                protected NhanVien doInBackground() throws Exception {
                    NhanVien_DAO dao = new NhanVien_DAO();
                    NhanVien nv = dao.getNhanVienByEmail(email);
                    if (nv == null) return null;
                    EmailService.sendOTP(email);
                    return nv;
                }

                @Override
                protected void done() {
                    try {
                        NhanVien nv = get();
                        if (nv == null) {
                            warn("Không tìm thấy nhân viên với email này!");
                        } else {
                            foundNhanVien = nv;
                            step = 2;
                            buildStep2UI(email);
                        }
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        cause.printStackTrace();
                        warn("Không thể gửi email!\nKiểm tra lại:\n"
                                + "1. Cấu hình mail trong db.properties\n"
                                + "2. Kết nối internet\n\nLỗi: " + cause.getMessage());
                    } finally {
                        btnSendOTP.setEnabled(true);
                        btnSendOTP.setText("GỬI MÃ OTP");
                    }
                }
            }.execute();
        });

        btnConfirm.addActionListener(e -> {
            String otp     = txtOTP.getText().trim();
            String newPw   = new String(txtNewPw.getPassword());
            String confirm = new String(txtConfirm.getPassword());
            String email   = lblStep.getText().replace("Bước 2 / 2 – OTP đã gửi tới: ", "").trim();

            if (otp.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
                warn("Vui lòng điền đầy đủ thông tin!"); return;
            }
            if (newPw.length() < 6) {
                warn("Mật khẩu mới phải có ít nhất 6 ký tự!"); return;
            }
            if (!newPw.equals(confirm)) {
                warn("Xác nhận mật khẩu không khớp!"); return;
            }
            if (!EmailService.verifyOTP(email, otp)) {
                warn("Mã OTP sai hoặc đã hết hạn!\nVui lòng quay lại và gửi mã mới."); return;
            }

            TaiKhoan_DAO dao = new TaiKhoan_DAO();
            boolean ok = dao.updateMatKhauByMaTK(foundNhanVien.getTaiKhoan().getMaTK(), newPw);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Đổi mật khẩu thành công!\nVui lòng đăng nhập lại.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                SwingUtilities.invokeLater(Login::new);
            } else {
                warn("Lỗi hệ thống khi cập nhật mật khẩu. Vui lòng thử lại!");
            }
        });
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    // ── Inner classes (giống Login) ───────────────────────────────────────────
    public class JPanelWithBackground extends JPanel {
        private Image backgroundImage;
        public JPanelWithBackground(String fileName) throws IOException {
            backgroundImage = ImageIO.read(new File(fileName));
        }
        public JPanelWithBackground() {}
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null)
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public class JGradientLabel extends JLabel {
        private Color color1 = Color.decode("#FF4B2B");
        private Color color2 = Color.decode("#FFAD06");
        public JGradientLabel(String text) { super(text); }
        @Override protected void paintComponent(Graphics g) {
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
