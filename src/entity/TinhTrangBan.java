package src.entity;

import java.util.Objects;

public class TinhTrangBan {
    private String maTinhTrang;
    private String tenTinhTrang;

    public TinhTrangBan(String maTinhTrang, String tenTinhTrang) {
        this.maTinhTrang = maTinhTrang;
        this.tenTinhTrang = tenTinhTrang;
    }

    public TinhTrangBan() {
    }

    public String getMaTinhTrang() {
        return maTinhTrang;
    }

    public void setMaTinhTrang(String maTinhTrang) {
        this.maTinhTrang = maTinhTrang;
    }

    public String getTenTinhTrang() {
        return tenTinhTrang;
    }

    public void setTenTinhTrang(String tenTinhTrang) {
        this.tenTinhTrang = tenTinhTrang;
    }

    @Override
    public String toString() {
        return tenTinhTrang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TinhTrangBan that = (TinhTrangBan) o;
        return Objects.equals(maTinhTrang, that.maTinhTrang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maTinhTrang);
    }
}