package entity;

import java.util.Date;
import java.sql.Time;

public class HoaDon {

    private String maHD;
    private Date   ngayLap;
    private Time   thoiGian;
    private double tongTien;

    /** Trạng thái thanh toán chi tiết – THAY THẾ boolean trangThai cũ. */
    private TrangThaiThanhToan trangThaiThanhToan = TrangThaiThanhToan.CHUA_THANH_TOAN;

    /** Hình thức thanh toán – NULL khi chưa thanh toán. */
    private HinhThucThanhToan hinhThucThanhToan;

    private DonDatBan donDatBan;
    private NhanVien  nhanVien;
    private KhuyenMai khuyenMai;
    private KhachHang khachHang;
    private double    tienCoc;
    private String    maCa;
    private Date      thoiGianRoiThucTe;
    private String    ghiChu;

    // ── Constructors ──────────────────────────────────────────────────────

    public HoaDon() {}

    /** Constructor đầy đủ mới. */
    public HoaDon(String maHD, Date ngayLap, Time thoiGian, double tongTien,
                  TrangThaiThanhToan trangThaiThanhToan, HinhThucThanhToan hinhThucThanhToan,
                  DonDatBan donDatBan, NhanVien nhanVien, KhuyenMai khuyenMai,
                  KhachHang khachHang, double tienCoc) {
        this.maHD               = maHD;
        this.ngayLap            = ngayLap;
        this.thoiGian           = thoiGian;
        this.tongTien           = tongTien;
        this.trangThaiThanhToan = trangThaiThanhToan != null ? trangThaiThanhToan : TrangThaiThanhToan.CHUA_THANH_TOAN;
        this.hinhThucThanhToan  = hinhThucThanhToan;
        this.donDatBan          = donDatBan;
        this.nhanVien           = nhanVien;
        this.khuyenMai          = khuyenMai;
        this.khachHang          = khachHang;
        this.tienCoc            = tienCoc;
    }

    // ── Getters & Setters – trường mới ──────────────────────────────────────

    public TrangThaiThanhToan getTrangThaiThanhToan() {
        return trangThaiThanhToan != null ? trangThaiThanhToan : TrangThaiThanhToan.CHUA_THANH_TOAN;
    }
    public void setTrangThaiThanhToan(TrangThaiThanhToan v) {
        this.trangThaiThanhToan = v != null ? v : TrangThaiThanhToan.CHUA_THANH_TOAN;
    }

    public HinhThucThanhToan getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(HinhThucThanhToan v) { this.hinhThucThanhToan = v; }


    public boolean isTrangThai() { return getTrangThaiThanhToan().isFullyPaid(); }

    // ── Getters & Setters ───────────────────────────────────────────────────

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public Time getThoiGian() { return thoiGian; }
    public void setThoiGian(Time thoiGian) { this.thoiGian = thoiGian; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public DonDatBan getDonDatBan() { return donDatBan; }
    public void setDonDatBan(DonDatBan donDatBan) { this.donDatBan = donDatBan; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public double getTienCoc() { return tienCoc; }
    public void setTienCoc(double tienCoc) { this.tienCoc = tienCoc; }

    public String getMaCa() { return maCa; }
    public void setMaCa(String maCa) { this.maCa = maCa; }

    public Date getThoiGianRoiThucTe() { return thoiGianRoiThucTe; }
    public void setThoiGianRoiThucTe(Date thoiGianRoiThucTe) { this.thoiGianRoiThucTe = thoiGianRoiThucTe; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    // ── Business helpers ────────────────────────────────────────────────────

    public void dongHoaDon() {}
    public double tinhTienTruocKM() { return 0; }
    public double tinhVAT() { return 0; }
    public double tinhTongTien() { return 0; }

    @Override
    public String toString() {
        return "HoaDon [maHD=" + maHD + ", ngayLap=" + ngayLap
                + ", tongTien=" + tongTien
                + ", trangThai=" + getTrangThaiThanhToan().getDisplay() + "]";
    }
}
