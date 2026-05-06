package entity;

public class KhachHang {
    private String maKH;
    private String tenKH;
    private String soDT;

    public KhachHang() {}

    public KhachHang(String maKH, String tenKH, String soDT) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.soDT = soDT;
    }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }

    public String getSoDT() { return soDT; }
    public void setSoDT(String soDT) { this.soDT = soDT; }

    // Các phương thức nghiệp vụ từ diagram
    public double tinhTongChi() { return 0; }
    public int tichDiem() { return 0; }
    public int tinhDiemTichLuy() { return 0; }

    @Override
    public String toString() {
        return "KhachHang [maKH=" + maKH + ", tenKH=" + tenKH + ", soDT=" + soDT + "]";
    }
}
