package entity;

import java.sql.Time;
import java.text.SimpleDateFormat;

public class CaLam {
    private String maCa;
    private String tenCa;
    private Time gioBatDau;
    private Time gioKetThuc;

    public CaLam() {}

    public CaLam(String maCa, String tenCa, Time gioBatDau, Time gioKetThuc) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public String getMaCa() { return maCa; }
    public void setMaCa(String maCa) { this.maCa = maCa; }

    public String getTenCa() { return tenCa; }
    public void setTenCa(String tenCa) { this.tenCa = tenCa; }

    public Time getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(Time gioBatDau) { this.gioBatDau = gioBatDau; }

    public Time getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(Time gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public String getKhungGio() {
        if (gioBatDau == null || gioKetThuc == null) return "";
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        return fmt.format(gioBatDau) + " - " + fmt.format(gioKetThuc);
    }

    @Override
    public String toString() { return tenCa != null ? tenCa : maCa; }
}
