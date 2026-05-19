package entity;

/**
 * Enum biểu diễn hình thức thanh toán.
 *
 * <p>Được lưu xuống DB dưới dạng String (VARCHAR).
 * Nếu hóa đơn chưa thanh toán thì cột này là NULL.
 */
public enum HinhThucThanhToan {

    TIEN_MAT("Tiền mặt"),
    CHUYEN_KHOAN("Chuyển khoản");

    private final String display;

    HinhThucThanhToan(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    /** Chuyển từ chuỗi display → enum. Trả về null nếu không tìm thấy. */
    public static HinhThucThanhToan fromDisplay(String display) {
        if (display == null || display.trim().isEmpty()) return null;
        for (HinhThucThanhToan h : values()) {
            if (h.display.equalsIgnoreCase(display.trim())) return h;
        }
        return null;
    }

    @Override
    public String toString() {
        return display;
    }
}
