package entity;

public class TinhTrangBan {
    private String maTinhTrang;
    private String tenTinhTrang;

    public TinhTrangBan() {}

    public TinhTrangBan(String maTinhTrang, String tenTinhTrang) {
        this.maTinhTrang = maTinhTrang;
        this.tenTinhTrang = tenTinhTrang;
    }

    public String getMaTinhTrang() { return maTinhTrang; }
    public void setMaTinhTrang(String maTinhTrang) { this.maTinhTrang = maTinhTrang; }

    public String getTenTinhTrang() { return tenTinhTrang; }
    public void setTenTinhTrang(String tenTinhTrang) { this.tenTinhTrang = tenTinhTrang; }

    public void capNhatTrangThai() {}

    @Override
    public String toString() {
        return "TinhTrangBan [maTinhTrang=" + maTinhTrang + ", tenTinhTrang=" + tenTinhTrang + "]";
    }
}
