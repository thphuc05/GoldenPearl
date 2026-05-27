package service;

import dao.HoaDon_DAO;
import dao.KhachHang_DAO;
import dao.LichSuDiem_DAO;
import entity.*;
import entity.LoaiGiaoDich;
import util.Constants;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Service layer cho nghiệp vụ thanh toán.
 * Tách khỏi ThanhToanDialog để GUI chỉ lo hiển thị.
 */
public class ThanhToanService {

    // ── Tính tổng tiền theo danh sách món ────────────────────────────────────
    public static double tinhTongTien(List<ChiTietHoaDon> cths) {
        double total = 0;
        for (ChiTietHoaDon ct : cths) total += ct.getDonGia() * ct.getSoLuong();
        return total;
    }

    // ── Tính giảm giá khuyến mãi ─────────────────────────────────────────────
    public static double tinhGiamKM(double tongTien, KhuyenMai km) {
        if (km == null) return 0;
        return Math.floor(tongTien * km.getPhanTramGiam() / 100.0);
    }

    // ── Tính giảm điểm theo bậc voucher ──────────────────────────────────────
    public static double tinhGiamDiem(int soDiem) {
        if (soDiem >= Constants.DIEM_VOUCHER_LON)  return Constants.GIAM_VOUCHER_LON;
        if (soDiem >= Constants.DIEM_VOUCHER_VUA)  return Constants.GIAM_VOUCHER_VUA;
        if (soDiem >= Constants.DIEM_VOUCHER_NHO)  return Constants.GIAM_VOUCHER_NHO;
        return 0;
    }

    // ── Tính số tiền còn lại khách phải trả ──────────────────────────────────
    public static double tinhConLai(double tongTien, double tienCoc,
                                    double giamKM, double giamDiem) {
        return Math.max(0, tongTien - tienCoc - giamKM - giamDiem);
    }

    // ── Tính điểm tích lũy sau thanh toán ────────────────────────────────────
    public static int tinhDiemTich(double tongTien) {
        return (int) Math.floor(tongTien * 0.0001);
    }

    // ── Đổi khách hàng trên hóa đơn (chỉ QL, chỉ khi chưa/đã cọc) ──────────
    public static KhachHang doiKhachHang(HoaDon hd, String sdtMoi, NhanVien nvHienTai,
                                          KhachHang_DAO khDAO, HoaDon_DAO hdDAO) throws Exception {
        TaiKhoan tk = nvHienTai != null ? nvHienTai.getTaiKhoan() : null;
        String vaiTro = tk != null ? tk.getVaiTro() : "";
        boolean isQL = "QUAN_LY".equalsIgnoreCase(vaiTro)
                || "QL".equalsIgnoreCase(vaiTro)
                || "Quản Lý".equalsIgnoreCase(vaiTro);
        if (!isQL) throw new SecurityException("Chỉ Quản lý mới có thể đổi khách hàng!");

        TrangThaiThanhToan tt = hd.getTrangThaiThanhToan();
        if (tt != TrangThaiThanhToan.CHUA_THANH_TOAN && tt != TrangThaiThanhToan.DA_COC)
            throw new IllegalStateException("Chỉ đổi được KH khi hóa đơn chưa thanh toán hoặc đã cọc!");

        KhachHang khMoi = khDAO.getKhachHangBySdt(sdtMoi);
        if (khMoi == null) throw new IllegalArgumentException("Không tìm thấy KH với SĐT " + sdtMoi + "!");

        String maKHCu = hd.getKhachHang() != null ? hd.getKhachHang().getMaKH() : "—";
        String log = "Đổi KH: " + maKHCu + " → " + khMoi.getMaKH()
                + " lúc " + new SimpleDateFormat("HH:mm dd/MM").format(new Date())
                + " (QL: " + nvHienTai.getMaNV() + ")";
        String existing = hd.getGhiChu();
        String ghiChu = (existing != null && !existing.isEmpty()) ? existing + " | " + log : log;

        if (!hdDAO.updateKhachHangVaGhiChu(hd.getMaHD(), khMoi.getMaKH(), ghiChu))
            throw new RuntimeException("Lỗi khi cập nhật DB!");

        hd.setKhachHang(khMoi);
        hd.setGhiChu(ghiChu);
        return khMoi;
    }

    // ── Xác nhận thanh toán và ghi DB ────────────────────────────────────────
    /**
     * Cập nhật HoaDon → ĐÃ_THANH_TOÁN, ghi LichSuDiem (tiêu + tích).
     * Ném RuntimeException nếu DB thất bại.
     */
    public static void thanhToan(HoaDon hd,
                                  double tongThucThu,
                                  HinhThucThanhToan hinhThuc,
                                  int selectedVoucherDiem,
                                  double giamDiem,
                                  HoaDon_DAO hdDAO,
                                  LichSuDiem_DAO lsdDAO) {
        hdDAO.updateTongTien(hd.getMaHD(), tongThucThu);
        hdDAO.updateThanhToan(hd.getMaHD(), TrangThaiThanhToan.DA_THANH_TOAN, hinhThuc);

        KhachHang kh = hd.getKhachHang();
        if (kh == null || "0000000000".equals(kh.getSoDT())) return;

        DecimalFormat fmt = new DecimalFormat("#,###");

        if (selectedVoucherDiem > 0) {
            LichSuDiem tieu = new LichSuDiem();
            tieu.setMaGiaoDich(lsdDAO.getNextMaGD());
            tieu.setMaKH(kh.getMaKH());
            tieu.setMaHD(hd.getMaHD());
            tieu.setSoGiaoDich(-selectedVoucherDiem);
            tieu.setLoai(LoaiGiaoDich.DOI_VOUCHER);
            tieu.setThoiGian(new Date());
            tieu.setGhiChu("Đổi voucher " + fmt.format(giamDiem) + "đ — HĐ " + hd.getMaHD());
            lsdDAO.addGiaoDich(tieu);
        }

        int diemTich = tinhDiemTich(hd.getTongTien());
        if (diemTich > 0) {
            LichSuDiem tich = new LichSuDiem();
            tich.setMaGiaoDich(lsdDAO.getNextMaGD());
            tich.setMaKH(kh.getMaKH());
            tich.setMaHD(hd.getMaHD());
            tich.setSoGiaoDich(diemTich);
            tich.setLoai(LoaiGiaoDich.TICH_LUY);
            tich.setThoiGian(new Date());
            tich.setGhiChu("Tích " + diemTich + " điểm từ HĐ " + hd.getMaHD());
            lsdDAO.addGiaoDich(tich);
        }
    }
}
