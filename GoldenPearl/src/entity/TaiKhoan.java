package entity;

public class TaiKhoan {
    private String maTK;
    private String tenTK;
    private String matKhau;
    private String vaiTro;

    public TaiKhoan() {}

    public TaiKhoan(String maTK, String tenTK, String matKhau, String vaiTro) {
        this.maTK = maTK;
        this.tenTK = tenTK;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
    }

    public String getMaTK() { return maTK; }
    public void setMaTK(String maTK) { this.maTK = maTK; }

    public String getTenTK() { return tenTK; }
    public void setTenTK(String tenTK) { this.tenTK = tenTK; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    @Override
    public String toString() {
        return "TaiKhoan [maTK=" + maTK + ", tenTK=" + tenTK + ", vaiTro=" + vaiTro + "]";
    }
}
