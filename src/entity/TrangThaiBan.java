package entity;

public enum TrangThaiBan {
	Trong("Trống"),
	DaDuocDat("Đã được đặt"),
	DangDuocSuDung("Đang được sử dụng"),
	BaoTri("Bảo trì");

	private final String moTa;

	TrangThaiBan(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }

    public static TrangThaiBan fromString(String value) {
        if (value == null) return Trong;
        for (TrangThaiBan tt : TrangThaiBan.values()) {
            if (tt.moTa.equalsIgnoreCase(value) || tt.name().equalsIgnoreCase(value)) {
                return tt;
            }
        }
        if (value.equals("TRONG")) return Trong;
        if (value.equals("DAT_TRUOC")) return DaDuocDat;
        if (value.equals("DANG_SD")) return DangDuocSuDung;
        if (value.equals("BAO_TRI")) return BaoTri;
        return Trong;
    }
}
