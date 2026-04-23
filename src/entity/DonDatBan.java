package entity;

import java.util.Date;

public class DonDatBan {
    private String maDon;
    private Date thoiGianDat;
    private Date thoiGianDen;
    private int soLuongKhach;
    private KhachHang khachHang;
    private boolean trangThai;
    private NhanVien nhanVien;
    private Ban ban;

    public DonDatBan() {}

    public DonDatBan(String maDon, Date thoiGianDat, Date thoiGianDen, int soLuongKhach, KhachHang khachHang, boolean trangThai, NhanVien nhanVien, Ban ban) {
        this.maDon = maDon;
        this.thoiGianDat = thoiGianDat;
        this.thoiGianDen = thoiGianDen;
        this.soLuongKhach = soLuongKhach;
        this.khachHang = khachHang;
        this.trangThai = trangThai;
        this.nhanVien = nhanVien;
        this.ban = ban;
    }

    public String getMaDon() { return maDon; }
    public void setMaDon(String maDon) { this.maDon = maDon; }

    public Date getThoiGianDat() { return thoiGianDat; }
    public void setThoiGianDat(Date thoiGianDat) { this.thoiGianDat = thoiGianDat; }

    public Date getThoiGianDen() { return thoiGianDen; }
    public void setThoiGianDen(Date thoiGianDen) { this.thoiGianDen = thoiGianDen; }

    public int getSoLuongKhach() { return soLuongKhach; }
    public void setSoLuongKhach(int soLuongKhach) { this.soLuongKhach = soLuongKhach; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public Ban getBan() { return ban; }
    public void setBan(Ban ban) { this.ban = ban; }

    public double tinhTienCoc() { return 0; }
    public boolean kiemTraTinhTrang() { return true; }

    @Override
    public String toString() {
        return "DonDatBan [maDon=" + maDon + ", thoiGianDat=" + thoiGianDat + ", khachHang=" + khachHang + "]";
    }
}
