package src.entity;

public class TaiKhoan {
    private String maTK;
    private String tenTK;
    private String matKhau;
    private String vaiTro;

    public TaiKhoan(String vaiTro, String matKhau, String tenTK, String maTK) {
        this.vaiTro = vaiTro;
        this.matKhau = matKhau;
        this.tenTK = tenTK;
        this.maTK = maTK;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getTenTK() {
        return tenTK;
    }

    public void setTenTK(String tenTK) {
        this.tenTK = tenTK;
    }

    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }

    @Override
    public String toString() {
        return "taiKhoan" +
                "[maTK=" + maTK +
                ", tenTK=" + tenTK +
                ", matKhau=" + matKhau +
                ", vaiTro=" + vaiTro +
                ']';
    }
}
