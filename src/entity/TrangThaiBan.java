package src.entity;

public enum TrangThaiBan {
	Trong("Trống"),
	DaDuocDat("Đã được đặt"),
	DangDuocSuDung("Đang được sử dụng");

	private final String moTa;
	
	TrangThaiBan(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }

    public static TrangThaiBan fromString(String value) {
        for (TrangThaiBan tt : TrangThaiBan.values()) {
            if (tt.moTa.equalsIgnoreCase(value) || tt.name().equalsIgnoreCase(value)) {
                return tt;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái: " + value);
    }
}
	
	

