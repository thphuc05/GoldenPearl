package entity;

import java.util.Date;
import java.sql.Time;

public class HoaDon {
    private String maHD;
    private Date ngayLap;
    private Time thoiGian;
    private double tongTien;
    private boolean trangThai;
    private DonDatBan donDatBan;
    private NhanVien nhanVien;
    private KhuyenMai khuyenMai;
    private KhachHang khachHang;
    private double tienCoc;

    public HoaDon() {}

    public HoaDon(String maHD, Date ngayLap, double tongTien, boolean trangThai, DonDatBan donDatBan, NhanVien nhanVien, KhuyenMai khuyenMai, KhachHang khachHang) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.donDatBan = donDatBan;
        this.nhanVien = nhanVien;
        this.khuyenMai = khuyenMai;
        this.khachHang = khachHang;
    }

    public HoaDon(String maHD, Date ngayLap, Time thoiGian, double tongTien, boolean trangThai, DonDatBan donDatBan, NhanVien nhanVien, KhuyenMai khuyenMai, KhachHang khachHang) {
        this(maHD, ngayLap, tongTien, trangThai, donDatBan, nhanVien, khuyenMai, khachHang);
        this.thoiGian = thoiGian;
    }

    public HoaDon(String maHD, Date ngayLap, Time thoiGian, double tongTien, boolean trangThai, DonDatBan donDatBan, NhanVien nhanVien, KhuyenMai khuyenMai, KhachHang khachHang, double tienCoc) {
        this(maHD, ngayLap, thoiGian, tongTien, trangThai, donDatBan, nhanVien, khuyenMai, khachHang);
        this.tienCoc = tienCoc;
    }

    public double getTienCoc() { return tienCoc; }
    public void setTienCoc(double tienCoc) { this.tienCoc = tienCoc; }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public Time getThoiGian() { return thoiGian; }
    public void setThoiGian(Time thoiGian) { this.thoiGian = thoiGian; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public DonDatBan getDonDatBan() { return donDatBan; }
    public void setDonDatBan(DonDatBan donDatBan) { this.donDatBan = donDatBan; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public void dongHoaDon() {}
    public double tinhTienTruocKM() { return 0; }
    public double tinhVAT() { return 0; }
    public double tinhTongTien() { return 0; }

    @Override
    public String toString() {
        return "HoaDon [maHD=" + maHD + ", ngayLap=" + ngayLap + ", tongTien=" + tongTien + "]";
    }
}
