package entity;

public class Ban {
    private String maBan;
    private int soBan;
    private int sucChua;
    private String loaiBan;
    private KhuVuc khuVuc;
    private TrangThaiBan tinhTrangBan;

    public Ban() {}

    public Ban(String maBan, int soBan, int sucChua, String loaiBan, KhuVuc khuVuc, TrangThaiBan tinhTrangBan) {
        this.maBan = maBan;
        this.soBan = soBan;
        this.sucChua = sucChua;
        this.loaiBan = loaiBan;
        this.khuVuc = khuVuc;
        this.tinhTrangBan = tinhTrangBan;
    }

    public String getMaBan() { return maBan; }
    public void setMaBan(String maBan) { this.maBan = maBan; }

    public int getSoBan() { return soBan; }
    public void setSoBan(int soBan) { this.soBan = soBan; }

    public int getSucChua() { return sucChua; }
    public void setSucChua(int sucChua) { this.sucChua = sucChua; }

    public String getLoaiBan() { return loaiBan; }
    public void setLoaiBan(String loaiBan) { this.loaiBan = loaiBan; }

    public KhuVuc getKhuVuc() { return khuVuc; }
    public void setKhuVuc(KhuVuc khuVuc) { this.khuVuc = khuVuc; }

    public TrangThaiBan getTinhTrangBan() { return tinhTrangBan; }
    public void setTinhTrangBan(TrangThaiBan tinhTrangBan) { this.tinhTrangBan = tinhTrangBan; }

    @Override
    public String toString() {
        return "Ban [maBan=" + maBan + ", soBan=" + soBan + ", loaiBan=" + loaiBan + "]";
    }
}
