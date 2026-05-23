package entity;

public enum TrangThaiMon {
    CHO_XU_LY("Chờ xử lý"),
    DANG_LAM("Đang làm"),
    DA_XONG("Đã xong"),
    DA_PHUC_VU("Đã phục vụ");

    private final String tenHienThi;

    TrangThaiMon(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() { return tenHienThi; }

    public static TrangThaiMon fromString(String s) {
        if (s == null) return CHO_XU_LY;
        switch (s.trim().toUpperCase()) {
            case "CHO_XU_LY":  return CHO_XU_LY;
            case "DANG_LAM":   return DANG_LAM;
            case "DA_XONG":    return DA_XONG;
            case "DA_PHUC_VU": return DA_PHUC_VU;
            default:           return CHO_XU_LY;
        }
    }

    public String toDatabaseValue() { return this.name(); }

    @Override
    public String toString() { return tenHienThi; }
}
