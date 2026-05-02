package entity;

import java.util.Date;

public class KhuyenMai {
    private String maKM;
    private String tenKM;
    private double phanTramGiam;
    private Date ngayBatDau;
    private Date ngayKetThuc;

    public KhuyenMai() {}

    public KhuyenMai(String maKM, String tenKM, double phanTramGiam, Date ngayBatDau, Date ngayKetThuc) {
        this.maKM = maKM;
        this.tenKM = tenKM;
        this.phanTramGiam = phanTramGiam;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getMaKM() { return maKM; }
    public void setMaKM(String maKM) { this.maKM = maKM; }

    public String getTenKM() { return tenKM; }
    public void setTenKM(String tenKM) { this.tenKM = tenKM; }

    public double getPhanTramGiam() { return phanTramGiam; }
    public void setPhanTramGiam(double phanTramGiam) { this.phanTramGiam = phanTramGiam; }

    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public boolean kiemTraHieuLuc() { return true; }

    @Override
    public String toString() {
        return "KhuyenMai [maKM=" + maKM + ", tenKM=" + tenKM + ", phanTramGiam=" + phanTramGiam + "]";
    }
}
