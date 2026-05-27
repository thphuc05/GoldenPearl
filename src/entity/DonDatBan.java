package entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DonDatBan {
    private String maDon;
    private Date thoiGianDat;
    private Date thoiGianDen;
    private Date thoiGianDuKienRoi;
    private int soLuongKhach;
    private KhachHang khachHang;
    private boolean trangThai;
    private NhanVien nhanVien;
    private List<Ban> dsBan = new ArrayList<>();
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

    public Date getThoiGianDen()                                    { return thoiGianDen; }
    public void setThoiGianDen(Date thoiGianDen)                    { this.thoiGianDen = thoiGianDen; }

    public Date getThoiGianDuKienRoi()                              { return thoiGianDuKienRoi; }
    public void setThoiGianDuKienRoi(Date thoiGianDuKienRoi)        { this.thoiGianDuKienRoi = thoiGianDuKienRoi; }

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

    /** Tính giờ dự kiến rời dựa theo soLuongKhach (không lưu field). */
    public Date computeThoiGianDuKienRoi() {
        if (thoiGianDen == null) return null;
        int minutes;
        if (soLuongKhach <= 2)      minutes = 90;
        else if (soLuongKhach <= 4) minutes = 120;
        else if (soLuongKhach <= 8) minutes = 180;
        else                        minutes = 240;
        return new Date(thoiGianDen.getTime() + (long) minutes * 60 * 1000);
    }

    /** Tính và lưu vào field thoiGianDuKienRoi — gọi trước khi INSERT/UPDATE DB. */
    public void computeAndSetThoiGianDuKienRoi() {
        this.thoiGianDuKienRoi = computeThoiGianDuKienRoi();
    }

    public double tinhTienCoc() {
        return (dsBan == null || dsBan.isEmpty()) ? 500_000 : (long) dsBan.size() * 500_000;
    }
    public boolean kiemTraTinhTrang()  { return true; }

    @Override
    public String toString() {
        return "DonDatBan [maDon=" + maDon
                + ", thoiGianDat=" + thoiGianDat
                + ", khachHang=" + khachHang
                + ", ban=" + getDanhSachTenBan() + "]";
    }
}