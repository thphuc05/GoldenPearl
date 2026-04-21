package src.entity;

import java.util.Objects;

public class KhachHang {
	private String maKhachHang;
	private String tenKhachHang;
	private String sDT;
	private int diemTichLuy;
	private boolean KHDK;
	public KhachHang(String maKhachHang, String tenKhachHang, String sDT, int diemTichLuy, boolean laKHDK) {
		super();
		this.maKhachHang = maKhachHang;
		this.tenKhachHang = tenKhachHang;
		this.sDT = sDT;
		this.diemTichLuy = diemTichLuy;
		this.KHDK = laKHDK;
	}
	public KhachHang(String ma) {
		this.maKhachHang=ma;
		this.tenKhachHang = "";
		this.sDT = "";
		this.diemTichLuy = 0;
		this.KHDK = false;
	}
	public KhachHang(String ma,String tenKH, String sdtKH) {
		this.maKhachHang=ma;
		this.tenKhachHang = tenKH;
		this.sDT = sdtKH;
		this.diemTichLuy = 0;
		this.KHDK = false;
	}
	public KhachHang(String maKhachHang, String tenKhachHang, String sDT, boolean b) {
		this.maKhachHang = maKhachHang;
		this.tenKhachHang = tenKhachHang;
		this.sDT = sDT;
		this.diemTichLuy = 0;
		this.KHDK = b;
	}
	public void congDiemTichLuy(int diemTichLuy) {
		this.diemTichLuy+=diemTichLuy;
	}
	public String getTenKhachHang() {
		return tenKhachHang;
	}
	public void setTenKhachHang(String tenKhachHang) {
		this.tenKhachHang = tenKhachHang;
	}
	public String getsDT() {
		return sDT;
	}
	public void setsDT(String sDT) {
		this.sDT = sDT;
	}
	public int getDiemTichLuy() {
		return diemTichLuy;
	}
	public void setDiemTichLuy(int diemTichLuy) {
		this.diemTichLuy = diemTichLuy;
	}
	public boolean isLaKHDK() {
		return KHDK;
	}
	public void setKHDK(boolean laKHDK) {
		this.KHDK = laKHDK;
	}
	public String getMaKhachHang() {
		return maKhachHang;
	}
	@Override
	public int hashCode() {
		return Objects.hash(maKhachHang);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KhachHang other = (KhachHang) obj;
		return Objects.equals(maKhachHang, other.maKhachHang);
	}
	@Override
	public String toString() {
		return "KhachHang [maKhachHang=" + maKhachHang + ", tenKhachHang=" + tenKhachHang + ", sDT=" + sDT
				+ ", diemTichLuy=" + diemTichLuy + ", laKHDK=" + KHDK + "]";
	}
	
}
