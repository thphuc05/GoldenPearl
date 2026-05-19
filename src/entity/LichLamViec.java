package entity;

import java.util.Date;

/**
 * Entity đại diện cho lịch làm việc.
 */
public class LichLamViec {

    private int      maLich;
    private NhanVien nhanVien;
    private CaLam    caLam;
    private Date     ngayLam;
    private String   ghiChu;

    public LichLamViec() {}

    // Constructor 5 tham số (đầy đủ)
    public LichLamViec(int maLich, NhanVien nhanVien, CaLam caLam, Date ngayLam, String ghiChu) {
        this.maLich   = maLich;
        this.nhanVien = nhanVien;
        this.caLam    = caLam;
        this.ngayLam  = ngayLam;
        this.ghiChu   = ghiChu;
    }

    // --- CONSTRUCTOR MỚI: Dùng cho logic tạo mã tự động ---
    // Constructor này giúp khớp với lệnh: new LichLamViec(0, nv, caPhucVuLuu, ngay);
    public LichLamViec(int maLich, NhanVien nhanVien, CaLam caLam, Date ngayLam) {
        this.maLich   = maLich;
        this.nhanVien = nhanVien;
        this.caLam    = caLam;
        this.ngayLam  = ngayLam;
        this.ghiChu   = ""; // Mặc định ghi chú rỗng nếu không truyền vào
    }

    // ── Getters & Setters ───────────────────────────────────────────────────

    public int getMaLich() { return maLich; }
    public void setMaLich(int maLich) { this.maLich = maLich; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public CaLam getCaLam() { return caLam; }
    public void setCaLam(CaLam caLam) { this.caLam = caLam; }

    public Date getNgayLam() { return ngayLam; }
    public void setNgayLam(Date ngayLam) { this.ngayLam = ngayLam; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return "LichLamViec [maLich=" + maLich
                + ", nv=" + (nhanVien != null ? nhanVien.getMaNV() : "null")
                + ", ca=" + (caLam    != null ? caLam.getTenCa()   : "null")
                + ", ngay=" + ngayLam + "]";
    }
}