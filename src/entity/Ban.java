package src.entity;

import java.util.Objects;

public class Ban {
    private String maBan;
    private int soBan;
    private int sucChua;
    private String loaiBan;
    private KhuVuc khuVuc; 
    private TinhTrangBan tinhTrangBan; 

    public Ban(String maBan, int soBan, int sucChua, String loaiBan, KhuVuc khuVuc, TinhTrangBan tinhTrangBan) {
        this.maBan = maBan;
        this.soBan = soBan;
        this.sucChua = sucChua;
        this.loaiBan = loaiBan;
        this.khuVuc = khuVuc;
        this.tinhTrangBan = tinhTrangBan;
    }

    public Ban() {
    }

    public void capNhatTrangThai() {
    }

    public void datBan() {
    }

    public void giaiPhongBan() {
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        this.maBan = maBan;
    }

    public int getSoBan() {
        return soBan;
    }

    public void setSoBan(int soBan) {
        this.soBan = soBan;
    }

    public int getSucChua() {
        return sucChua;
    }

    public void setSucChua(int sucChua) {
        this.sucChua = sucChua;
    }

    public String getLoaiBan() {
        return loaiBan;
    }

    public void setLoaiBan(String loaiBan) {
        this.loaiBan = loaiBan;
    }

    public KhuVuc getKhuVuc() {
        return khuVuc;
    }

    public void setKhuVuc(KhuVuc khuVuc) {
        this.khuVuc = khuVuc;
    }

    public TinhTrangBan getTinhTrangBan() {
        return tinhTrangBan;
    }

    public void setTinhTrangBan(TinhTrangBan tinhTrangBan) {
        this.tinhTrangBan = tinhTrangBan;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maBan);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Ban other = (Ban) obj;
        return Objects.equals(maBan, other.maBan);
    }

    @Override
    public String toString() {
        return "Ban [maBan=" + maBan + ", soBan=" + soBan + ", sucChua=" + sucChua + 
               ", loaiBan=" + loaiBan + ", tinhTrang=" + (tinhTrangBan != null ? tinhTrangBan.getTenTinhTrang() : "N/A") + "]";
    }
}