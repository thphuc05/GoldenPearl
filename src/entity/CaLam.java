package entity;
import java.sql.Time;

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

    // Getters & Setters
    public String getMaCa() { return maCa; }
    public void setMaCa(String maCa) { this.maCa = maCa; }
    public String getTenCa() { return tenCa; }
    public void setTenCa(String tenCa) { this.tenCa = tenCa; }
    public Time getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(Time gioBatDau) { this.gioBatDau = gioBatDau; }
    public Time getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(Time gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public String getDisplayName() {
        String gbd = (gioBatDau != null) ? gioBatDau.toString().substring(0, 5) : "??:??";
        String gkt = (gioKetThuc != null) ? gioKetThuc.toString().substring(0, 5) : "??:??";
        return tenCa + " (" + gbd + " - " + gkt + ")";
    }
    @Override
    public String toString() {
        // Lấy chuỗi giờ bắt đầu và kết thúc (substring để bỏ phần giây :00)
        String start = (gioBatDau != null) ? gioBatDau.toString().substring(0, 5) : "--:--";
        String end = (gioKetThuc != null) ? gioKetThuc.toString().substring(0, 5) : "--:--";

        // Trả về định dạng: Ca Sáng (06:00 - 14:00)
        return tenCa + " (" + start + " - " + end + ")";
    }
}