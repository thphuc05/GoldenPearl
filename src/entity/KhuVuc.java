package src.entity;

import java.util.Objects;

public class KhuVuc {
    private String maKV;
    private String tenKV;
    private String moTa;

    public KhuVuc(String maKV, String tenKV, String moTa) {
        this.maKV = maKV;
        this.tenKV = tenKV;
        this.moTa = moTa;
    }

    public KhuVuc() {
    }

    
    public void themBan() {
        System.out.println("Thêm bàn mới vào khu vực: " + this.tenKV);
    }

    public String getMaKV() {
        return maKV;
    }

    public void setMaKV(String maKV) {
        this.maKV = maKV;
    }

    public String getTenKV() {
        return tenKV;
    }

    public void setTenKV(String tenKV) {
        this.tenKV = tenKV;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return tenKV;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhuVuc khuVuc = (KhuVuc) o;
        return Objects.equals(maKV, khuVuc.maKV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKV);
    }
}