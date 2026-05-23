package entity;

import java.util.Date;

public class PhanCongCa {
    private int maPhanCong;
    private CaLam caLam;
    private NhanVien nhanVien;
    private Date ngayLam;

    public PhanCongCa() {}

    public PhanCongCa(int maPhanCong, CaLam caLam, NhanVien nhanVien, Date ngayLam) {
        this.maPhanCong = maPhanCong;
        this.caLam = caLam;
        this.nhanVien = nhanVien;
        this.ngayLam = ngayLam;
    }

    public int getMaPhanCong() { return maPhanCong; }
    public void setMaPhanCong(int maPhanCong) { this.maPhanCong = maPhanCong; }

    public CaLam getCaLam() { return caLam; }
    public void setCaLam(CaLam caLam) { this.caLam = caLam; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public Date getNgayLam() { return ngayLam; }
    public void setNgayLam(Date ngayLam) { this.ngayLam = ngayLam; }
}
