package util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EmailService {

    private static final long OTP_TTL_MS = 5 * 60 * 1000L; // 5 phút

    // OTP store: email → [code, expireAt]
    private static final Map<String, long[]> otpStore = new HashMap<>();

    // ── Tạo và gửi OTP ───────────────────────────────────────────────────────
    public static void sendOTP(String toEmail) throws Exception {
        Properties mailProps = loadMailProps();

        String from     = mailProps.getProperty("mail.from");
        String password = mailProps.getProperty("mail.password");

        if (from == null || from.isBlank() || from.startsWith("your_gmail")) {
            throw new Exception("Chưa cấu hình mail.from trong db.properties");
        }
        if (password == null || password.isBlank() || password.startsWith("xxxx")) {
            throw new Exception("Chưa cấu hình mail.password trong db.properties");
        }

        String code = generateCode();
        otpStore.put(toEmail.toLowerCase(),
                new long[]{Long.parseLong(code), System.currentTimeMillis() + OTP_TTL_MS});

        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth",            "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host",            "smtp.gmail.com");
        smtpProps.put("mail.smtp.port",            "587");
        smtpProps.put("mail.smtp.ssl.trust",       "smtp.gmail.com");
        smtpProps.put("mail.smtp.connectiontimeout", "10000");
        smtpProps.put("mail.smtp.timeout",           "10000");

        // Bỏ khoảng trắng trong App Password nếu người dùng để nguyên "xxxx xxxx xxxx xxxx"
        final String cleanPassword = password.replace(" ", "");
        final String senderEmail   = from.trim();

        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, cleanPassword);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(senderEmail));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        msg.setSubject("Golden Pearl - Ma xac nhan dat lai mat khau");
        msg.setContent(buildHtml(code), "text/html; charset=UTF-8");

        Transport.send(msg);
        System.out.println("✅ Đã gửi OTP tới: " + toEmail);
    }

    // ── Kiểm tra OTP ─────────────────────────────────────────────────────────
    public static boolean verifyOTP(String email, String inputCode) {
        long[] entry = otpStore.get(email.toLowerCase());
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry[1]) {
            otpStore.remove(email.toLowerCase()); return false;
        }
        boolean match = String.valueOf((long) entry[0]).equals(inputCode.trim());
        if (match) otpStore.remove(email.toLowerCase());
        return match;
    }

    // ── private ──────────────────────────────────────────────────────────────

    private static String generateCode() {
        return String.valueOf(new SecureRandom().nextInt(900_000) + 100_000);
    }

    private static Properties loadMailProps() {
        Properties props = new Properties();

        // Thử đọc từ classpath (khi chạy từ IntelliJ / JAR)
        try (InputStream in = EmailService.class.getResourceAsStream("/connectDB/db.properties")) {
            if (in != null) {
                props.load(in);
                System.out.println("✅ Đọc mail config từ classpath thành công");
                return props;
            }
        } catch (IOException ignored) {}

        // Fallback: đọc trực tiếp từ thư mục src (khi classpath chưa được copy)
        String[] paths = {
            "src/connectDB/db.properties",
            "connectDB/db.properties"
        };
        for (String path : paths) {
            try (FileInputStream fis = new FileInputStream(path)) {
                props.load(fis);
                System.out.println("✅ Đọc mail config từ: " + path);
                return props;
            } catch (IOException ignored) {}
        }

        System.err.println("❌ Không tìm thấy db.properties!");
        return props;
    }

    private static String buildHtml(String code) {
        return "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto;"
             + "border:1px solid #ddd;border-radius:12px;overflow:hidden'>"
             + "<div style='background:#0B3D59;padding:20px 24px'>"
             + "<h2 style='color:#fff;margin:0'>Golden Pearl</h2></div>"
             + "<div style='padding:28px 24px'>"
             + "<p style='font-size:15px;color:#333'>Xin chao,</p>"
             + "<p style='font-size:15px;color:#333'>Ma xac nhan dat lai mat khau cua ban la:</p>"
             + "<div style='font-size:38px;font-weight:bold;letter-spacing:10px;"
             + "color:#0B3D59;text-align:center;padding:16px 0'>" + code + "</div>"
             + "<p style='font-size:13px;color:#888'>Ma co hieu luc trong <b>5 phut</b>.</p>"
             + "</div></div>";
    }
}
