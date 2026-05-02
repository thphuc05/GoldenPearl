package gui;

import dao.TaiKhoan_DAO;
import entity.TaiKhoan;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;


public class ForgotPassword extends JFrame {
    TaiKhoan_DAO dao = new TaiKhoan_DAO();
    private String foundMaTK;
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
    private JLabel usernameLabel = new JLabel("Tên người dùng");
    private JLabel phoneLabel = new JLabel("Số điện thoại");
    private JLabel IDLabel = new JLabel("CCCD");
    private JLabel lblStatus = new JLabel();

    private JTextField txtUsername = new JTextField(30);
    private JTextField txtPhoneNum = new JTextField(30);
    private JTextField txtID = new JTextField(30);

    private JButton btnSendRequest = new JButton("KIỂM TRA");
    private JButton btnConfirm = new JButton("XÁC NHẬN");
    private JButton btnBack = new JButton("QUAY LẠI");

    private static final int SCREEN_WIDTH = 1180;
    private static final int SCREEN_HEIGHT = 820;

    public ForgotPassword() {
        super("Quên mật khẩu - Golden Pearl");
        initUI();
        initEvents();
        setVisible(true);
        SwingUtilities.updateComponentTreeUI(this);
        SwingUtilities.updateComponentTreeUI(panel);
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
        int panelHeight = 375;
        int x = (SCREEN_WIDTH - panelWidth) / 2;
        int y = (SCREEN_HEIGHT - panelHeight) / 2 + 30;

        panel.setOpaque(false);
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        panel.setMaximumSize(new Dimension(panelWidth, panelHeight));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nội dung bên trong panel
        usernameLabel.setBounds(50, 50, 300, 50);
        usernameLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));

        phoneLabel.setBounds(50, 130, 250, 50);
        phoneLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));

        IDLabel.setBounds(50, 210, 300, 50);
        IDLabel.setFont(new Font("Inter Bold", Font.BOLD, 30));

        lblStatus.setFont(new Font("Inter Medium", Font.PLAIN, 14));

        txtUsername.setBounds(300, 50, 360, 50);
        txtUsername.setFont(new Font("Inter Medium", Font.PLAIN, 23));
        txtPhoneNum.setBounds(300, 130, 220,50);
        txtPhoneNum.setFont(new Font("Inter Medium", Font.PLAIN, 23));
        txtID.setBounds(300, 210, 360,50);
        txtID.setFont(new Font("Inter Medium", Font.PLAIN, 23));

        btnSendRequest.setBounds(530, 129, 130, 53);
        btnSendRequest.setFont(new Font("Inter Bold", Font.BOLD, 14));
        btnSendRequest.setForeground(Color.WHITE);
        btnSendRequest.setBackground(Color.decode("#FF5F1F"));

        // Nút chức năng phía dưới
        btnBack.setBounds(100, 300, 240, 50);
        btnBack.setBackground(Color.BLACK);
        btnBack.setForeground(Color.WHITE);

        btnConfirm.setBounds(410, 300, 181, 50);

        // panel.add(screenTitle); - Tắt do kích cỡ màn hình chưa là cuối cùng
        panel.add(usernameLabel);
        panel.add(phoneLabel);
        panel.add(IDLabel);
        panel.add(lblStatus);
        panel.add(txtUsername);
        panel.add(txtPhoneNum);
        panel.add(txtID);
        panel.add(btnSendRequest);
        panel.add(btnBack);
        panel.add(btnConfirm);

        // Không hiên thị field CCCD cho đến khi kiểm tra có tài khoản tồn tại
        IDLabel.setVisible(false);
        txtID.setVisible(false);
        btnConfirm.setEnabled(false);

        bg.add(panel);
        setVisible(true);
    }

    private void initEvents() {
        // Nút gửi yêu cầu kiểm tra
        btnSendRequest.addActionListener(e -> {
            foundMaTK = null;
            txtID.setText("");
            txtID.setVisible(false);
            IDLabel.setVisible(false);
            lblStatus.setText("");

            TaiKhoan tk = dao.checkQuenTK(txtUsername.getText().trim(), txtPhoneNum.getText().trim());
            if (tk != null) {
                foundMaTK = tk.getMaTK();
                lblStatus.setText("Tải khoản hợp lệ đã tìm thấy, hãy nhập số CCCD để đặt lại mật khẩu");
                lblStatus.setForeground(Color.GREEN);
                lblStatus.setBounds(140, 170, 700, 50);
                IDLabel.setVisible(true);
                txtID.transferFocus();
                txtID.setVisible(true);
                btnConfirm.setVisible(true);
                btnConfirm.setForeground(Color.WHITE);
                btnConfirm.setBackground(Color.decode("#FF5F1F"));
            } else {
                lblStatus.setText("Không tìm thấy tài khoản, hãy đảm bảo tên người dùng và số điện thoại chính xác");
                lblStatus.setBounds(90, 170, 700, 50);
                lblStatus.setForeground(Color.RED);
            }
            revalidate();
            repaint();
        });

        txtID.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { toggleConfirm(); }
            public void removeUpdate(DocumentEvent e) { toggleConfirm(); }
            public void changedUpdate(DocumentEvent e) { toggleConfirm(); }

            private void toggleConfirm() {
                btnConfirm.setEnabled(!txtID.getText().trim().isEmpty());
            }
        });

        // Nút quay lại
        btnBack.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(Login::new);
        });

        // Nút xác nhận
        btnConfirm.addActionListener(e -> {
            boolean success = dao.resetMatKhau(foundMaTK, txtID.getText());
            if (success) {
                JOptionPane.showMessageDialog(this, "Mật khẩu đã được đặt lại thành: 123456");
                dispose();
                new Login();
            } else {
                lblStatus.setText("Số CCCD không đúng");
                lblStatus.setForeground(Color.RED);
            }
        });
    }

    // Styling class
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
