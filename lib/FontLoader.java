package lib;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;

public class FontLoader {
    public static void registerFont(String filePath) {
        try {
            File fontFile = new File(filePath); // File font

            if (!fontFile.exists()) {
                System.err.println("❌ Font không tìm thấy ở: " + fontFile.getAbsolutePath());
                return;
            }

            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);

            System.out.println("✅ Font đã thêm: " + font.getFontName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}