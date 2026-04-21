package src.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class HoaDon {
	private String maHoaDon;
	private Ban ban;
	private NhanVien nhanVienBan;
	private boolean trangThaiThanhToan;
	private KhachHang khachHang;
	private LocalDateTime thoiGianVao;
	private LocalDateTime thoiGianRa;


	public HoaDon(String maHoaDon, Ban ban, NhanVien nhanVienBan, boolean trangThaiThanhToan, KhachHang khachHang,
			LocalDateTime thoiGianVao, LocalDateTime thoiGianRa) {
		super();
		this.maHoaDon = maHoaDon;
		this.ban = ban;
		this.nhanVienBan = nhanVienBan;
		this.trangThaiThanhToan = trangThaiThanhToan;
		this.khachHang = khachHang;
		this.thoiGianVao = thoiGianVao;
		this.thoiGianRa = thoiGianRa;
		
	}
	public HoaDon(String string) {
		this.maHoaDon = string;
	}
	public Ban getBan() {
		return ban;
	}
	public void setBan(Ban ban) {
		this.ban = ban;
	}
	public NhanVien getNhanVienBan() {
		return nhanVienBan;
	}
	public void setNhanVienBan(NhanVien nhanVienBan) {
		this.nhanVienBan = nhanVienBan;
	}
	public boolean isTrangThaiThanhToan() {
		return trangThaiThanhToan;
	}
	public void setTrangThaiThanhToan(boolean trangThaiThanhToan) {
		this.trangThaiThanhToan = trangThaiThanhToan;
	}
	public KhachHang getKhachHang() {
		return khachHang;
	}
	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}
	public LocalDateTime getThoiGianVao() {
		return thoiGianVao;
	}
	public void setThoiGianVao(LocalDateTime thoiGianVao) {
		this.thoiGianVao = thoiGianVao;
	}
	public LocalDateTime getThoiGianRa() {
		return thoiGianRa;
	}
	public void setThoiGianRa(LocalDateTime thoiGianRa) {
		this.thoiGianRa = thoiGianRa;
	}
	public String getMaHoaDon() {
		return maHoaDon;
	}
	@Override
	public int hashCode() {
		return Objects.hash(maHoaDon);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HoaDon other = (HoaDon) obj;
		return Objects.equals(maHoaDon, other.maHoaDon);
	}
	@Override
	public String toString() {
		return "HoaDon [maHoaDon=" + maHoaDon + ", ban=" + ban + ", nhanVienBan=" + nhanVienBan
				+ ", trangThaiThanhToan=" + trangThaiThanhToan + ", khachHang=" + khachHang + ", thoiGianVao="
				+ thoiGianVao + ", thoiGianRa=" + thoiGianRa + "]";
	}
	
}
