package src.entity;

public enum LoaiSanPham {
	Tra("Trà"),
	SinhTo("Sinh tố"),
	NuocUongDongChai("Nước uống đóng chai"),
	Yogurt("YOGURT"),
	Cafe("Cafe"),
	Banh("Bánh"),
	TraSua("Trà sữa");
	
	private final String moTa;
	
	LoaiSanPham(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }

    public static LoaiSanPham fromString(String value) {
        for (LoaiSanPham tt : LoaiSanPham.values()) {
            if (tt.moTa.equalsIgnoreCase(value) || tt.name().equalsIgnoreCase(value)) {
                return tt;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái: " + value);
    }
	

}
