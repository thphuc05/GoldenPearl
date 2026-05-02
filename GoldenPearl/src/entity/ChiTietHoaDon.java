package entity;

public class ChiTietHoaDon {
    private SanPham monAn;
    private HoaDon hoaDon;
    private int soLuong;
    private double donGia;
    private String ghiChu;
    private double thanhTien;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(SanPham monAn, HoaDon hoaDon, int soLuong, double donGia, String ghiChu, double thanhTien) {
        this.monAn = monAn;
        this.hoaDon = hoaDon;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.ghiChu = ghiChu;
        this.thanhTien = thanhTien;
    }

    public SanPham getMonAn() { return monAn; }
    public void setMonAn(SanPham monAn) { this.monAn = monAn; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public double tinhThanhTien() { return soLuong * donGia; }

    @Override
    public String toString() {
        return "ChiTietHoaDon [monAn=" + monAn + ", soLuong=" + soLuong + ", thanhTien=" + thanhTien + "]";
    }
}
