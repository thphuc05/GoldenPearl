package entity;

public class SanPham {
    private String maMon;
    private String tenMon;
    private double donGia;
    private String moTa;
    private boolean trangThai;
    private LoaiSanPham loaiSanPham;
    private String hinhAnh;

    public SanPham() {}

    public SanPham(String maMon, String tenMon, double donGia, String moTa, boolean trangThai, LoaiSanPham loaiSanPham, String hinhAnh) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.donGia = donGia;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.loaiSanPham = loaiSanPham;
        this.hinhAnh = hinhAnh;
    }

    public String getMaMon() { return maMon; }
    public void setMaMon(String maMon) { this.maMon = maMon; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public LoaiSanPham getLoaiSanPham() { return loaiSanPham; }
    public void setLoaiSanPham(LoaiSanPham loaiSanPham) { this.loaiSanPham = loaiSanPham; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public void capNhatGia(double giaMoi) { this.donGia = giaMoi; }

    @Override
    public String toString() {
        return tenMon;
    }
}
