package entity;

public class CaLamViec {
    private String maCa;
    private String tenCa;
    private String gioVao;
    private String gioRa;

    public CaLamViec() {}

    public CaLamViec(String maCa, String tenCa, String gioVao, String gioRa) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioVao = gioVao;
        this.gioRa = gioRa;
    }

    public String getMaCa() { return maCa; }
    public void setMaCa(String maCa) { this.maCa = maCa; }

    public String getTenCa() { return tenCa; }
    public void setTenCa(String tenCa) { this.tenCa = tenCa; }

    public String getGioVao() { return gioVao; }
    public void setGioVao(String gioVao) { this.gioVao = gioVao; }

    public String getGioRa() { return gioRa; }
    public void setGioRa(String gioRa) { this.gioRa = gioRa; }

    @Override
    public String toString() { return tenCa; }
}
