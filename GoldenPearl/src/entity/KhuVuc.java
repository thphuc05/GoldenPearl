package entity;

public class KhuVuc {
    private String maKV;
    private String tenKV;
    private String moTa;

    public KhuVuc() {}

    public KhuVuc(String maKV, String tenKV, String moTa) {
        this.maKV = maKV;
        this.tenKV = tenKV;
        this.moTa = moTa;
    }

    public String getMaKV() { return maKV; }
    public void setMaKV(String maKV) { this.maKV = maKV; }

    public String getTenKV() { return tenKV; }
    public void setTenKV(String tenKV) { this.tenKV = tenKV; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public void themBan() { /* nghiệp vụ */ }

    @Override
    public String toString() {
        return "KhuVuc [maKV=" + maKV + ", tenKV=" + tenKV + "]";
    }
}
