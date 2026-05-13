package entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DonDatBan {
    private String maDon;
    private Date thoiGianDat;
    private Date thoiGianDen;
    private int soLuongKhach;
    private KhachHang khachHang;
    private boolean trangThai;
    private NhanVien nhanVien;
    private List<Ban> dsBan = new ArrayList<>();   // ← thay thế Ban ban đơn
    private String khungGio;
    private String ghiChu;

    public DonDatBan() {}

    public DonDatBan(String maDon, Date thoiGianDat, Date thoiGianDen,
                     int soLuongKhach, KhachHang khachHang,
                     boolean trangThai, NhanVien nhanVien, List<Ban> dsBan) {
        this.maDon         = maDon;
        this.thoiGianDat   = thoiGianDat;
        this.thoiGianDen   = thoiGianDen;
        this.soLuongKhach  = soLuongKhach;
        this.khachHang     = khachHang;
        this.trangThai     = trangThai;
        this.nhanVien      = nhanVien;
        this.dsBan         = dsBan != null ? dsBan : new ArrayList<>();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public String getMaDon()                        { return maDon; }
    public void   setMaDon(String maDon)            { this.maDon = maDon; }

    public Date getThoiGianDat()                    { return thoiGianDat; }
    public void setThoiGianDat(Date thoiGianDat)    { this.thoiGianDat = thoiGianDat; }

    public Date getThoiGianDen()                    { return thoiGianDen; }
    public void setThoiGianDen(Date thoiGianDen)    { this.thoiGianDen = thoiGianDen; }

    public int  getSoLuongKhach()                   { return soLuongKhach; }
    public void setSoLuongKhach(int soLuongKhach)   { this.soLuongKhach = soLuongKhach; }

    public KhachHang getKhachHang()                 { return khachHang; }
    public void      setKhachHang(KhachHang kh)     { this.khachHang = kh; }

    public boolean isTrangThai()                    { return trangThai; }
    public void    setTrangThai(boolean trangThai)  { this.trangThai = trangThai; }

    public NhanVien getNhanVien()                   { return nhanVien; }
    public void     setNhanVien(NhanVien nv)        { this.nhanVien = nv; }

    public List<Ban> getDsBan()                     { return dsBan; }
    public void      setDsBan(List<Ban> dsBan)      { this.dsBan = dsBan != null ? dsBan : new ArrayList<>(); }

    public String getKhungGio()                     { return khungGio; }
    public void   setKhungGio(String khungGio)      { this.khungGio = khungGio; }

    public String getGhiChu()                       { return ghiChu; }
    public void   setGhiChu(String ghiChu)          { this.ghiChu = ghiChu; }

    // ── Backward-compat: code khác gọi getBan() / setBan() vẫn hoạt động ─
    /** Trả về bàn đầu tiên trong danh sách (hoặc null nếu chưa có bàn nào). */
    public Ban getBan() {
        return dsBan.isEmpty() ? null : dsBan.get(0);
    }
    /** Gán đúng 1 bàn vào danh sách (dùng khi chỉ cần 1 bàn). */
    public void setBan(Ban ban) {
        dsBan.clear();
        if (ban != null) dsBan.add(ban);
    }

    // ── Helper methods ────────────────────────────────────────────────────

    /**
     * Tổng sức chứa của tất cả các bàn trong đơn.
     */
    public int getTongSucChua() {
        return dsBan.stream().mapToInt(Ban::getSucChua).sum();
    }

    /**
     * Chuỗi tên các bàn, ví dụ: "Bàn 1, Bàn 3, Bàn 5"
     */
    public String getDanhSachTenBan() {
        return dsBan.stream()
                .map(b -> "Bàn " + b.getSoBan())
                .collect(Collectors.joining(", "));
    }

    public double tinhTienCoc()        { return 0; }
    public boolean kiemTraTinhTrang()  { return true; }

    @Override
    public String toString() {
        return "DonDatBan [maDon=" + maDon
                + ", thoiGianDat=" + thoiGianDat
                + ", khachHang=" + khachHang
                + ", ban=" + getDanhSachTenBan() + "]";
    }
}