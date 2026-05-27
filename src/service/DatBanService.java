package service;

import dao.*;
import entity.*;
import util.Constants;

import java.sql.Timestamp;
import java.util.*;
import java.util.Date;

/**
 * Service layer cho nghiệp vụ đặt bàn.
 * GUI chỉ thu thập input và hiển thị kết quả — toàn bộ logic nằm ở đây.
 */
public class DatBanService {

    private final KhachHang_DAO     khDAO   = new KhachHang_DAO();
    private final DonDatBan_DAO     ddbDAO  = new DonDatBan_DAO();
    private final HoaDon_DAO        hdDAO   = new HoaDon_DAO();
    private final ChiTietHoaDon_DAO cthdDAO = new ChiTietHoaDon_DAO();
    private final Ban_DAO           banDAO  = new Ban_DAO();
    private final CaLam_DAO         caDAO   = new CaLam_DAO();
    private final SanPham_DAO       spDAO   = new SanPham_DAO();

    // ── Tính giờ rời dự kiến theo sức chứa ──────────────────────────────────
    public static int tinhGioRoiDuKien(int sucChua) {
        if (sucChua <= 2) return Constants.THOI_GIAN_2_KHACH;
        if (sucChua <= 4) return Constants.THOI_GIAN_4_KHACH;
        if (sucChua <= 8) return Constants.THOI_GIAN_8_KHACH;
        return Constants.THOI_GIAN_NHIEU_KHACH;
    }

    // ── Kiểm tra trùng lịch ──────────────────────────────────────────────────
    public DonDatBan kiemTraTrungLich(String maBan, Date tgDenNew, Date tgRoiNew) {
        List<DonDatBan> allDons = ddbDAO.getAllDonDatBanWithBan();
        for (DonDatBan d : allDons) {
            if (d.isTrangThai()) continue;
            boolean hasBan = d.getDsBan().stream()
                    .anyMatch(b -> b.getMaBan().equals(maBan));
            if (!hasBan) continue;
            Date tgDen = d.getThoiGianDen();
            Date tgRoi = d.computeThoiGianDuKienRoi();
            if (tgDen == null || tgRoi == null) continue;
            if (tgDenNew.before(tgRoi) && tgDen.before(tgRoiNew)) return d;
        }
        return null;
    }

    // ── Request DTO ──────────────────────────────────────────────────────────
    public static class DatBanRequest {
        public final List<Ban>            dsBan;
        public final Date                 ngayDat;
        public final String               thoiGian;   // "11:00"
        public final String               tenKH;
        public final String               sdtKH;
        public final String               ghiChu;
        public final Map<String,Integer>  bookingCart; // maMon → soLuong

        public DatBanRequest(List<Ban> dsBan, Date ngayDat, String thoiGian,
                             String tenKH, String sdtKH, String ghiChu,
                             Map<String,Integer> bookingCart) {
            this.dsBan       = dsBan;
            this.ngayDat     = ngayDat;
            this.thoiGian    = thoiGian;
            this.tenKH       = tenKH;
            this.sdtKH       = sdtKH;
            this.ghiChu      = ghiChu;
            this.bookingCart = bookingCart;
        }
    }

    // ── Đặt bàn trước (có cọc) ───────────────────────────────────────────────
    /**
     * @return HoaDon cọc đã lưu vào DB
     * @throws Exception nếu có lỗi validate hoặc DB
     */
    public HoaDon datBanTruoc(DatBanRequest req, NhanVien nv) throws Exception {
        // 1. Validate
        if (req.tenKH.isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập tên khách hàng!");
        if (!req.sdtKH.matches("0\\d{9}"))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)!");

        Date thoiGianDen = buildDateTime(req.ngayDat, req.thoiGian);
        if (isToday(req.ngayDat) && thoiGianDen.before(new Date()))
            throw new IllegalArgumentException("Giờ " + req.thoiGian + " hôm nay đã qua! Vui lòng chọn giờ khác.");

        int tongSucChua = req.dsBan.stream().mapToInt(Ban::getSucChua).sum();
        int mins = tinhGioRoiDuKien(tongSucChua);
        Date thoiGianDuKienRoi = new Date(thoiGianDen.getTime() + (long) mins * 60_000);

        // 2. Kiểm tra trùng lịch
        for (Ban ban : req.dsBan) {
            DonDatBan conflict = kiemTraTrungLich(ban.getMaBan(), thoiGianDen, thoiGianDuKienRoi);
            if (conflict != null)
                throw new IllegalStateException("Bàn " + ban.getSoBan()
                        + " đã có đơn đặt trùng lịch!\nGiờ đến: " + req.thoiGian + " — " + req.ngayDat);
        }

        // 3. Tìm / tạo khách hàng
        KhachHang kh = khDAO.getKhachHangBySdt(req.sdtKH);
        if (kh == null) {
            kh = new KhachHang();
            kh.setMaKH(khDAO.getNextMaKH());
            kh.setTenKH(req.tenKH);
            kh.setSoDT(req.sdtKH);
            khDAO.addKhachHang(kh);
        }

        // 4. Tạo đơn đặt bàn
        Timestamp now = new Timestamp(System.currentTimeMillis());
        DonDatBan don = new DonDatBan();
        don.setMaDon(ddbDAO.getNextMaDon());
        don.setThoiGianDat(now);
        don.setThoiGianDen(thoiGianDen);
        don.setThoiGianDuKienRoi(thoiGianDuKienRoi);
        don.setSoLuongKhach(tongSucChua);
        don.setKhachHang(kh);
        don.setNhanVien(nv);
        don.setDsBan(new ArrayList<>(req.dsBan));
        don.setTrangThai(false);
        don.setGhiChu(req.ghiChu);
        ddbDAO.addDonDatBan(don);

        // 5. Tính tiền gọi món trước
        List<SanPham> allSP = spDAO.getAllSanPham();
        Map<String,SanPham> spMap = new HashMap<>();
        for (SanPham sp : allSP) spMap.put(sp.getMaMon(), sp);
        double foodTotal = 0;
        for (Map.Entry<String,Integer> e : req.bookingCart.entrySet()) {
            SanPham sp = spMap.get(e.getKey());
            if (sp != null) foodTotal += sp.getGiaBan() * e.getValue();
        }

        // 6. Tạo hóa đơn cọc
        HoaDon hd = new HoaDon();
        hd.setMaHD(hdDAO.getNextMaHD());
        hd.setNgayLap(now);
        hd.setThoiGian(new java.sql.Time(now.getTime()));
        double tienCoc = req.dsBan.size() * Constants.TIEN_COC_MAC_DINH;
        hd.setTongTien(tienCoc + foodTotal);
        hd.setTrangThaiThanhToan(TrangThaiThanhToan.DA_COC);
        hd.setDonDatBan(don);
        hd.setNhanVien(nv);
        hd.setKhachHang(kh);
        hd.setTienCoc(tienCoc);
        entity.CaLam ca = caDAO.getCaHienTai();
        if (ca != null) hd.setMaCa(ca.getMaCa());

        // 7. Lưu HĐ + món + cập nhật bàn (mỗi DAO tự quản lý connection qua ThreadLocal)
        if (!hdDAO.create(hd)) throw new RuntimeException("Lỗi tạo hóa đơn");
        for (Map.Entry<String,Integer> e : req.bookingCart.entrySet()) {
            SanPham sp = spMap.get(e.getKey());
            if (sp == null || e.getValue() <= 0) continue;
            double tt = sp.getGiaBan() * e.getValue();
            HoaDon ref = new HoaDon(); ref.setMaHD(hd.getMaHD());
            if (!cthdDAO.create(new ChiTietHoaDon(sp, ref, e.getValue(), sp.getGiaBan(), "", tt)))
                throw new RuntimeException("Lỗi thêm chi tiết món");
        }
        if (isToday(req.ngayDat)) {
            for (Ban ban : req.dsBan)
                banDAO.updateTinhTrangBan(ban.getMaBan(), TrangThaiBan.DaDuocDat);
        }
        return hd;
    }

    // ── Ăn ngay (không cọc) ─────────────────────────────────────────────────
    /**
     * @return HoaDon đã lưu vào DB
     * @throws Exception nếu có lỗi validate hoặc DB
     */
    public HoaDon anNgay(DatBanRequest req, NhanVien nv) throws Exception {
        String sdt = req.sdtKH;
        if (!sdt.isEmpty() && !sdt.matches("0\\d{9}"))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)!");

        // Tìm / tạo khách hàng
        KhachHang kh;
        if (!sdt.isEmpty()) {
            kh = khDAO.getKhachHangBySdt(sdt);
            if (kh == null) {
                kh = new KhachHang();
                kh.setMaKH(khDAO.getNextMaKH());
                kh.setTenKH(req.tenKH.isEmpty() ? "Khách vãng lai" : req.tenKH);
                kh.setSoDT(sdt);
                khDAO.addKhachHang(kh);
            }
        } else {
            kh = khDAO.getKhachHangBySdt("0000000000");
            if (kh == null) {
                kh = new KhachHang();
                kh.setMaKH(khDAO.getNextMaKH());
                kh.setTenKH("Khách vãng lai");
                kh.setSoDT("0000000000");
                khDAO.addKhachHang(kh);
            }
        }

        int tongSucChua = req.dsBan.stream().mapToInt(Ban::getSucChua).sum();
        int mins = tinhGioRoiDuKien(tongSucChua);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Date thoiGianDuKienRoi = new Date(now.getTime() + (long) mins * 60_000);

        DonDatBan don = new DonDatBan();
        don.setMaDon(ddbDAO.getNextMaDon());
        don.setThoiGianDat(now);
        don.setThoiGianDen(now);
        don.setThoiGianDuKienRoi(thoiGianDuKienRoi);
        don.setSoLuongKhach(tongSucChua);
        don.setKhachHang(kh);
        don.setNhanVien(nv);
        don.setDsBan(new ArrayList<>(req.dsBan));
        don.setTrangThai(false);
        don.setGhiChu(req.ghiChu);
        ddbDAO.addDonDatBan(don);

        List<SanPham> allSP = spDAO.getAllSanPham();
        Map<String,SanPham> spMap = new HashMap<>();
        for (SanPham sp : allSP) spMap.put(sp.getMaMon(), sp);
        double foodTotal = 0;
        for (Map.Entry<String,Integer> e : req.bookingCart.entrySet()) {
            SanPham sp = spMap.get(e.getKey());
            if (sp != null) foodTotal += sp.getGiaBan() * e.getValue();
        }

        HoaDon hd = new HoaDon();
        hd.setMaHD(hdDAO.getNextMaHD());
        hd.setNgayLap(now);
        hd.setThoiGian(new java.sql.Time(now.getTime()));
        hd.setTongTien(foodTotal);
        hd.setTrangThaiThanhToan(TrangThaiThanhToan.CHUA_THANH_TOAN);
        hd.setDonDatBan(don);
        hd.setNhanVien(nv);
        hd.setKhachHang(kh);
        hd.setTienCoc(0);
        entity.CaLam ca = caDAO.getCaHienTai();
        if (ca != null) hd.setMaCa(ca.getMaCa());

        if (!hdDAO.create(hd)) throw new RuntimeException("Lỗi tạo hóa đơn");
        for (Map.Entry<String,Integer> e : req.bookingCart.entrySet()) {
            SanPham sp = spMap.get(e.getKey());
            if (sp == null || e.getValue() <= 0) continue;
            double tt = sp.getGiaBan() * e.getValue();
            HoaDon ref = new HoaDon(); ref.setMaHD(hd.getMaHD());
            if (!cthdDAO.create(new ChiTietHoaDon(sp, ref, e.getValue(), sp.getGiaBan(), "", tt)))
                throw new RuntimeException("Lỗi thêm chi tiết món");
        }
        for (Ban ban : req.dsBan)
            banDAO.updateTinhTrangBan(ban.getMaBan(), TrangThaiBan.DangDuocSuDung);
        return hd;
    }

    // ── Hủy đặt bàn ─────────────────────────────────────────────────────────
    /**
     * Hủy đơn đặt bàn. Nếu hoanCoc=true → HOAN_TIEN, ngược lại → DA_HUY.
     * Luôn xóa đơn và reset trạng thái bàn về Trống.
     */
    public void huyDatBan(DonDatBan don, boolean hoanCoc) throws Exception {
        // Đánh dấu đơn đã hủy (trangThai = true), giữ nguyên trong DB
        ddbDAO.updateTrangThai(don.getMaDon(), true);
        // Cập nhật trạng thái hóa đơn: hoàn cọc hoặc mất cọc
        HoaDon hd = hdDAO.getHoaDonByMaDon(don.getMaDon());
        if (hd != null) {
            TrangThaiThanhToan tt = hoanCoc
                    ? TrangThaiThanhToan.HOAN_TIEN
                    : TrangThaiThanhToan.DA_HUY;
            hdDAO.updateTrangThai(hd.getMaHD(), tt);
        }
        // Reset bàn về Trống
        for (Ban b : don.getDsBan()) {
            banDAO.updateTinhTrangBan(b.getMaBan(), TrangThaiBan.Trong);
        }
    }

    // ── Time helpers ─────────────────────────────────────────────────────────
    public static Date buildDateTime(Date date, String timeStr) {
        String[] parts = timeStr.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static boolean isToday(Date d) {
        if (d == null) return false;
        Calendar c1 = Calendar.getInstance(); c1.setTime(d);
        Calendar c2 = Calendar.getInstance();
        return c1.get(Calendar.YEAR)        == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
