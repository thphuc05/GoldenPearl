package entity;

/**
 * Enum biểu diễn trạng thái thanh toán của hóa đơn.
 *
 * <p>Được lưu xuống DB dưới dạng String (VARCHAR) để dễ đọc và tương thích
 * với dữ liệu cũ (cột trangThai BOOLEAN cũ sẽ được migrate tự động qua ConnectDB).
 *
 * Mapping từ boolean cũ:
 *   true  → DA_THANH_TOAN
 *   false → CHUA_THANH_TOAN
 */
public enum TrangThaiThanhToan {

    CHUA_THANH_TOAN("Chưa thanh toán"),
    DA_THANH_TOAN("Đã thanh toán"),
    DA_COC("Đã cọc"),
    HOAN_TIEN("Hoàn tiền"),
    DA_HUY("Đã hủy");

    private final String display;

    TrangThaiThanhToan(String display) {
        this.display = display;
    }

    /** Chuỗi hiển thị tiếng Việt. */
    public String getDisplay() {
        return display;
    }

    /** Chuyển từ chuỗi display → enum (dùng khi đọc DB). */
    public static TrangThaiThanhToan fromDisplay(String display) {
        if (display == null) return CHUA_THANH_TOAN;
        for (TrangThaiThanhToan t : values()) {
            if (t.display.equalsIgnoreCase(display.trim())) return t;
        }
        return CHUA_THANH_TOAN;
    }

    /** Chuyển từ boolean cũ → enum (dùng khi migrate dữ liệu cũ). */
    public static TrangThaiThanhToan fromBoolean(boolean paid) {
        return paid ? DA_THANH_TOAN : CHUA_THANH_TOAN;
    }

    /** Kiểm tra hóa đơn đã được thanh toán đầy đủ chưa. */
    public boolean isFullyPaid() {
        return this == DA_THANH_TOAN;
    }

    @Override
    public String toString() {
        return display;
    }
}
