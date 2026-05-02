package entity;

public class NhanVien {
    private String maNV;
    private String tenNV;
    private String soDT;
    private String soCCCD;
    private ChucVu chucVu;
    private boolean trangThai;
    private TaiKhoan taiKhoan;

    public NhanVien() {}

    public NhanVien(String maNV, String tenNV, String soDT, String soCCCD, ChucVu chucVu, boolean trangThai, TaiKhoan taiKhoan) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.soDT = soDT;
        this.soCCCD = soCCCD;
        this.chucVu = chucVu;
        this.trangThai = trangThai;
        this.taiKhoan = taiKhoan;
    }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public String getSoDT() { return soDT; }
    public void setSoDT(String soDT) { this.soDT = soDT; }

    public String getSoCCCD() { return soCCCD; }
    public void setSoCCCD(String soCCCD) { this.soCCCD = soCCCD; }

    public ChucVu getChucVu() { return chucVu; }
    public void setChucVu(ChucVu chucVu) { this.chucVu = chucVu; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    @Override
    public String toString() {
        return "NhanVien [maNV=" + maNV + ", tenNV=" + tenNV + ", chucVu=" + chucVu + ", trangThai=" + trangThai + "]";
    }
}
