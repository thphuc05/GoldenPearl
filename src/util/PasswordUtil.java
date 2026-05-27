package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordUtil {

    private static final int    ITERATIONS = 65_536;
    private static final int    KEY_BITS   = 256;
    private static final String ALGORITHM  = "PBKDF2WithHmacSHA256";
    private static final String PREFIX     = "pbkdf2:";

    /** Băm mật khẩu. Kết quả có thể lưu thẳng vào DB. */
    public static String hash(String plaintext) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(plaintext.toCharArray(), salt);
            return PREFIX
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Không thể băm mật khẩu", e);
        }
    }

    /**
     * Kiểm tra mật khẩu nhập vào với giá trị đã lưu trong DB.
     * Hỗ trợ backward-compat: nếu DB vẫn còn plaintext thì so sánh trực tiếp.
     */
    public static boolean verify(String plaintext, String stored) {
        if (stored == null || plaintext == null) return false;
        if (!stored.startsWith(PREFIX)) {
            return false;
        }
        try {
            String[] parts = stored.split(":");
            // parts[0]="pbkdf2", parts[1]=salt_b64, parts[2]=hash_b64
            byte[] salt     = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual   = pbkdf2(plaintext.toCharArray(), salt);
            return slowEquals(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    /** True nếu giá trị đã được hash (không phải plaintext). */
    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }

    // ── private ──────────────────────────────────────────────────────────────

    private static byte[] pbkdf2(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /** So sánh constant-time để chống timing attack. */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
