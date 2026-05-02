package entity;

public enum ChucVu {
    NHAN_VIEN("Nhân Viên", "NV"),
    QUAN_LY("Quản Lý", "QL");

    private final String tenHienThi;   // Tên để hiển thị trên UI
    private final String tenDatabase;  // Tên lưu trong CSDL

    private ChucVu(String tenHienThi, String tenDatabase) {
        this.tenHienThi = tenHienThi;
        this.tenDatabase = tenDatabase;
    }

    // Lấy tên hiển thị (dùng cho UI)
    public String getTenHienThi() {
        return tenHienThi;
    }

    //  Chuyển từ chuỗi (từ DB hoặc UI) sang Enum
    public static ChucVu fromString(String ten) {
        if (ten == null) return NHAN_VIEN;
        ten = ten.trim();

        for (ChucVu cv : ChucVu.values()) {
            if (cv.tenHienThi.equalsIgnoreCase(ten) || 
                cv.name().equalsIgnoreCase(ten))
                return cv;
        }

        //  Thêm hỗ trợ cho các giá trị viết tắt
        switch (ten.toUpperCase()) {
            case "NV":
            case "NHANVIEN":
            case "NHÂN VIÊN":
                return NHAN_VIEN;
            case "QL":
            case "QUANLY":
            case "QUẢN LÝ":
                return QUAN_LY;
            default:
                throw new IllegalArgumentException("Không tồn tại chức vụ: " + ten);
        }
    }

    public String toDatabaseValue() {
        return tenDatabase;
    }

    @Override
    public String toString() {
        return tenHienThi;
    }
}
