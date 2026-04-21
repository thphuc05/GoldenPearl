package src.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class DonDatBan {
	private String maDonDatBan;
	private KhachHang khachHang;
	private Ban ban;
	private LocalDateTime thoiGian;
	private boolean daNhan;
	public DonDatBan(String maDonDatHang, KhachHang khachHang, Ban ban, LocalDateTime thoiGian,boolean daNhan) {
		super();
		this.maDonDatBan = maDonDatHang;
		this.khachHang = khachHang;
		this.ban = ban;
		this.thoiGian = thoiGian;
		this.daNhan=daNhan;
	}
	
	public DonDatBan() {
		super();
	}

	public boolean isDaNhan() {
		return daNhan;
	}

	public void setDaNhan(boolean daNhan) {
		this.daNhan = daNhan;
	}
	public KhachHang getKhachHang() {
		return khachHang;
	}
	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}
	public Ban getBan() {
		return ban;
	}
	public void setBan(Ban ban) {
		this.ban = ban;
	}
	public LocalDateTime getThoiGian() {
		return thoiGian;
	}
	public void setThoiGian(LocalDateTime thoiGian) {
		this.thoiGian = thoiGian;
	}
	public String getMaDonDatBan() {
		return maDonDatBan;
	}
	
	public void setMaDonDatBan(String maDonDatBan) {
		this.maDonDatBan = maDonDatBan;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maDonDatBan);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DonDatBan other = (DonDatBan) obj;
		return Objects.equals(maDonDatBan, other.maDonDatBan);
	}
	@Override
	public String toString() {
		return "DonDatBan [maDonDatHang=" + maDonDatBan + ", khachHang=" + khachHang + ", ban=" + ban + ", thoiGian="
				+ thoiGian + "]";
	}
	
}
