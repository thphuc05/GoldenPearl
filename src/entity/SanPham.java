package entity;

public class SanPham {
    private String maMon;
    private String tenMon;
    private double giaGoc;
    private double giaBan;
    private String moTa;
    private boolean trangThai;
    private LoaiSanPham loaiSanPham;
    private String hinhAnh;

    public SanPham() {}

    public SanPham(String maMon, String tenMon, double giaGoc, double giaBan, String moTa, boolean trangThai, LoaiSanPham loaiSanPham, String hinhAnh) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.giaGoc = giaGoc;
        this.giaBan = giaBan;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.loaiSanPham = loaiSanPham;
        this.hinhAnh = hinhAnh;
    }

    public String getMaMon() { return maMon; }
    public void setMaMon(String maMon) { this.maMon = maMon; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public double getGiaGoc() { return giaGoc; }
    public void setGiaGoc(double giaGoc) { this.giaGoc = giaGoc; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public double getDonGia() { return giaBan; }
    public void setDonGia(double donGia) { this.giaBan = donGia; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public LoaiSanPham getLoaiSanPham() { return loaiSanPham; }
    public void setLoaiSanPham(LoaiSanPham loaiSanPham) { this.loaiSanPham = loaiSanPham; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public void capNhatGia(double giaMoi) { this.giaBan = giaMoi; }

    @Override
    public String toString() {
        return tenMon;
    }
}
