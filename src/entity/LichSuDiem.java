package entity;

import java.util.Date;

public class LichSuDiem {
    private String maGiaoDich;
    private String maKH;
    private String maHD;
    private int    soGiaoDich;   // dương = tích lũy, âm = tiêu điểm
    private String loai;         // "TichLuy" | "DoiVoucher"
    private Date   thoiGian;
    private String ghiChu;

    public LichSuDiem() {}

    public LichSuDiem(String maGiaoDich, String maKH, String maHD,
                      int soGiaoDich, String loai, Date thoiGian, String ghiChu) {
        this.maGiaoDich = maGiaoDich;
        this.maKH       = maKH;
        this.maHD       = maHD;
        this.soGiaoDich = soGiaoDich;
        this.loai       = loai;
        this.thoiGian   = thoiGian;
        this.ghiChu     = ghiChu;
    }

    public String getMaGiaoDich()              { return maGiaoDich; }
    public void   setMaGiaoDich(String v)      { maGiaoDich = v; }

    public String getMaKH()                    { return maKH; }
    public void   setMaKH(String v)            { maKH = v; }

    public String getMaHD()                    { return maHD; }
    public void   setMaHD(String v)            { maHD = v; }

    public int    getSoGiaoDich()              { return soGiaoDich; }
    public void   setSoGiaoDich(int v)         { soGiaoDich = v; }

    public String getLoai()                    { return loai; }
    public void   setLoai(String v)            { loai = v; }

    public Date   getThoiGian()                { return thoiGian; }
    public void   setThoiGian(Date v)          { thoiGian = v; }

    public String getGhiChu()                  { return ghiChu; }
    public void   setGhiChu(String v)          { ghiChu = v; }
}
