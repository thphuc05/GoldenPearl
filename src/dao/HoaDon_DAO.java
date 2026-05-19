package dao;

import connectDB.ConnectDB;
import entity.*;
import util.SQLLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho entity {@link HoaDon} – đã nâng cấp hỗ trợ:
 * <ul>
 *   <li>trangThaiThanhToan (VARCHAR thay boolean)</li>
 *   <li>hinhThucThanhToan (VARCHAR nullable)</li>
 *   <li>maCa (FK → CaLam, nullable cho dữ liệu cũ)</li>
 * </ul>
 *
 * <p><b>Tương thích ngược:</b> Phương thức {@code updateStatus(String, boolean)} vẫn còn
 * nhưng được chuyển hướng sang {@code updateTrangThai(String, TrangThaiThanhToan)}.
 */
public class HoaDon_DAO {

    // ══════════════════════════════════════════════════════════════════════
    //  READ
    // ══════════════════════════════════════════════════════════════════════

    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, ddb.khungGio, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN DonDatBan ddb ON hd.maDon = ddb.maDon " +
                    "LEFT JOIN CaLam c ON hd.maCa = c.maCa";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) ds.add(mapBasic(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public HoaDon getHoaDonByMa(String maHD) {
        Connection con = ConnectDB.getConnection();
        try {
            // SỬA TẠI ĐÂY: Thêm ddb.khungGio và LEFT JOIN DonDatBan
            String sql = "SELECT hd.*, ddb.khungGio, nv.tenNV, kh.tenKH, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN DonDatBan ddb ON hd.maDon = ddb.maDon " +
                    "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "LEFT JOIN CaLam c ON hd.maCa = c.maCa " +
                    "WHERE hd.maHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapFull(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public HoaDon getHoaDonByMaDon(String maDon) {
        Connection con = ConnectDB.getConnection();
        try {
            // SỬA TẠI ĐÂY: Thêm ddb.khungGio và LEFT JOIN DonDatBan
            String sql = "SELECT hd.*, ddb.khungGio, nv.tenNV, kh.tenKH, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN DonDatBan ddb ON hd.maDon = ddb.maDon " +
                    "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "LEFT JOIN CaLam c ON hd.maCa = c.maCa " +
                    "WHERE hd.maDon = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maDon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapFull(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<HoaDon> getHoaDonByDateRange(java.util.Date fromDate, java.util.Date toDate) {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            // SỬA TẠI ĐÂY: Thêm ddb.khungGio và LEFT JOIN DonDatBan
            String sql = "SELECT hd.*, ddb.khungGio, nv.tenNV, kh.tenKH, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN DonDatBan ddb ON hd.maDon = ddb.maDon " +
                    "JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "LEFT JOIN CaLam c ON hd.maCa = c.maCa " +
                    "WHERE hd.ngayLap BETWEEN ? AND ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(fromDate.getTime()));
            ps.setTimestamp(2, new Timestamp(toDate.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(mapFull(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /** Lọc hóa đơn theo ca làm. */
    public List<HoaDon> getHoaDonByCa(String maCa) {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            // SỬA TẠI ĐÂY: Thêm ddb.khungGio và LEFT JOIN DonDatBan
            String sql = "SELECT hd.*, ddb.khungGio, nv.tenNV, kh.tenKH, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN DonDatBan ddb ON hd.maDon = ddb.maDon " +
                    "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "JOIN CaLam c ON hd.maCa = c.maCa " +
                    "WHERE hd.maCa = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(mapFull(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /** Thống kê doanh thu theo ca làm (chỉ tính hóa đơn DA_THANH_TOAN). */
    public double getRevenueByCa(String maCa) {
        Connection con = ConnectDB.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT ISNULL(SUM(tongTien),0) FROM HoaDon " +
                            "WHERE maCa = ? AND trangThaiThanhToan = N'Đã thanh toán'");
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CREATE
    // ══════════════════════════════════════════════════════════════════════

    public boolean create(HoaDon hd) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            String sql = "INSERT INTO HoaDon " +
                    "(maHD, ngayLap, thoiGian, tongTien, trangThaiThanhToan, hinhThucThanhToan, " +
                    " maDon, maNV, maKH, maKM, tienCoc, maCa) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2, new Timestamp(hd.getNgayLap().getTime()));
            ps.setTime(3, hd.getThoiGian() != null ? hd.getThoiGian()
                    : new Time(hd.getNgayLap().getTime()));
            ps.setDouble(4, hd.getTongTien());
            ps.setNString(5, hd.getTrangThaiThanhToan().getDisplay());
            if (hd.getHinhThucThanhToan() != null)
                ps.setNString(6, hd.getHinhThucThanhToan().getDisplay());
            else
                ps.setNull(6, Types.NVARCHAR);
            setNullable(ps, 7, hd.getDonDatBan() != null ? hd.getDonDatBan().getMaDon() : null);
            setNullable(ps, 8, hd.getNhanVien()  != null ? hd.getNhanVien().getMaNV()   : null);
            ps.setString(9, hd.getKhachHang().getMaKH());
            setNullable(ps, 10, hd.getKhuyenMai() != null ? hd.getKhuyenMai().getMaKM() : null);
            ps.setDouble(11, hd.getTienCoc());
            setNullable(ps, 12, hd.getCaLam() != null ? hd.getCaLam().getMaCa() : null);
            n = ps.executeUpdate();
            if (n > 0) logCreate(hd);
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UPDATE
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Cập nhật trạng thái thanh toán + hình thức thanh toán cùng lúc.
     * Gọi khi thanh toán hóa đơn.
     */
    public boolean updateThanhToan(String maHD, TrangThaiThanhToan trangThai,
                                   HinhThucThanhToan hinhThuc) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE HoaDon SET trangThaiThanhToan=?, hinhThucThanhToan=? WHERE maHD=?");
            ps.setNString(1, trangThai.getDisplay());
            if (hinhThuc != null) ps.setNString(2, hinhThuc.getDisplay());
            else                  ps.setNull(2, Types.NVARCHAR);
            ps.setString(3, maHD);
            n = ps.executeUpdate();
            if (n > 0) SQLLogger.log(
                    "UPDATE HoaDon SET trangThaiThanhToan=" + SQLLogger.nStr(trangThai.getDisplay())
                            + ", hinhThucThanhToan=" + (hinhThuc != null ? SQLLogger.nStr(hinhThuc.getDisplay()) : "NULL")
                            + " WHERE maHD=" + SQLLogger.str(maHD) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    /** Chỉ cập nhật trạng thái thanh toán. */
    public boolean updateTrangThai(String maHD, TrangThaiThanhToan trangThai) {
        return updateThanhToan(maHD, trangThai, null);
    }

    /**
     * @deprecated Tương thích ngược. Dùng {@link #updateTrangThai(String, TrangThaiThanhToan)}.
     */
    @Deprecated
    public boolean updateStatus(String maHD, boolean status) {
        return updateTrangThai(maHD, TrangThaiThanhToan.fromBoolean(status));
    }

    public boolean updateTongTien(String maHD, double total) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE HoaDon SET tongTien=? WHERE maHD=?");
            ps.setDouble(1, total);
            ps.setString(2, maHD);
            n = ps.executeUpdate();
            if (n > 0) SQLLogger.log("UPDATE HoaDon SET tongTien=" + SQLLogger.num(total)
                    + " WHERE maHD=" + SQLLogger.str(maHD) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    /** Gán ca làm cho hóa đơn. */
    public boolean updateCaLam(String maHD, String maCa) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE HoaDon SET maCa=? WHERE maHD=?");
            setNullable(ps, 1, maCa);
            ps.setString(2, maHD);
            n = ps.executeUpdate();
            if (n > 0) SQLLogger.log("UPDATE HoaDon SET maCa=" + SQLLogger.str(maCa)
                    + " WHERE maHD=" + SQLLogger.str(maHD) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════════════════════

    public boolean deleteHoaDon(String maHD) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM HoaDon WHERE maHD=?");
            ps.setString(1, maHD);
            n = ps.executeUpdate();
            if (n > 0) SQLLogger.log("DELETE FROM HoaDon WHERE maHD=" + SQLLogger.str(maHD) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  KEY GENERATION
    // ══════════════════════════════════════════════════════════════════════

    public String getLatestMaHD() {
        Connection con = ConnectDB.getConnection();
        try {
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT TOP 1 maHD FROM HoaDon ORDER BY maHD DESC");
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    public String getNextMaHD() {
        String latest = getLatestMaHD();
        if (latest == null || latest.isEmpty()) return "HD001";
        try {
            int num = Integer.parseInt(latest.replaceAll("[^0-9]", "")) + 1;
            return String.format("HD%03d", num);
        } catch (NumberFormatException e) { return "HD001"; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REVENUE
    // ══════════════════════════════════════════════════════════════════════

    public double getRevenueByDateRange(java.util.Date fromDate, java.util.Date toDate) {
        Connection con = ConnectDB.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT ISNULL(SUM(tongTien),0) FROM HoaDon " +
                            "WHERE ngayLap BETWEEN ? AND ? AND trangThaiThanhToan = N'Đã thanh toán'");
            ps.setTimestamp(1, new Timestamp(fromDate.getTime()));
            ps.setTimestamp(2, new Timestamp(toDate.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MAP HELPERS (dùng cho bảng UI)
    // ══════════════════════════════════════════════════════════════════════

    public Map<String, String> getKhuVucMapForAllHoaDon() {
        Map<String, String> map = new HashMap<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.maHD, MIN(kv.tenKV) AS tenKV " +
                    "FROM HoaDon hd " +
                    "JOIN DonDatBan ddb ON hd.maDon = ddb.maDon " +
                    "JOIN ChiTietDatBan ct ON ddb.maDon = ct.maDonDatBan " +
                    "JOIN Ban b ON ct.maBan = b.maBan " +
                    "JOIN KhuVuc kv ON b.maKV = kv.maKV " +
                    "GROUP BY hd.maHD";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) map.put(rs.getString("maHD"), rs.getString("tenKV"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public Map<String, String> getDsBanDisplayForAllHoaDon() {
        Map<String, String> map = new HashMap<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.maHD, b.soBan " +
                    "FROM HoaDon hd " +
                    "JOIN DonDatBan ddb ON hd.maDon = ddb.maDon " +
                    "JOIN ChiTietDatBan ct ON ddb.maDon = ct.maDonDatBan " +
                    "JOIN Ban b ON ct.maBan = b.maBan " +
                    "ORDER BY hd.maHD, b.soBan";
            ResultSet rs = con.createStatement().executeQuery(sql);
            Map<String, List<Integer>> temp = new HashMap<>();
            while (rs.next()) {
                String maHD = rs.getString("maHD");
                int soBan   = rs.getInt("soBan");
                temp.computeIfAbsent(maHD, k -> new ArrayList<>()).add(soBan);
            }
            for (Map.Entry<String, List<Integer>> e : temp.entrySet()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < e.getValue().size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append("Bàn ").append(e.getValue().get(i));
                }
                map.put(e.getKey(), sb.toString());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    /** Map cơ bản (không JOIN NhanVien, KhachHang đầy đủ). */
    private HoaDon mapBasic(ResultSet rs) throws SQLException {
        String maDon = rs.getString("maDon");
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getString("maHD"));
        hd.setNgayLap(rs.getTimestamp("ngayLap"));
        hd.setThoiGian(rs.getTime("thoiGian"));
        hd.setTongTien(rs.getDouble("tongTien"));

        String tt = rs.getNString("trangThaiThanhToan");
        hd.setTrangThaiThanhToan(TrangThaiThanhToan.fromDisplay(tt));

        String ht = rs.getNString("hinhThucThanhToan");
        hd.setHinhThucThanhToan(HinhThucThanhToan.fromDisplay(ht));

        try { hd.setTienCoc(rs.getDouble("tienCoc")); } catch (Exception ignored) {}

        if (maDon != null) {
            DonDatBan don = new DonDatBan();
            don.setMaDon(maDon);
            // [MỚI] Đọc khungGio từ JOIN DonDatBan
            try { don.setKhungGio(rs.getString("khungGio")); } catch (Exception ignored) {}
            hd.setDonDatBan(don);
        }

        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        hd.setNhanVien(nv);

        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        hd.setKhachHang(kh);

        String maCa = rs.getString("maCa");
        if (maCa != null && !maCa.isEmpty()) {
            CaLam ca = new CaLam();
            ca.setMaCa(maCa);
            try { ca.setTenCa(rs.getNString("tenCa")); } catch (Exception ignored) {}
            try { ca.setGioBatDau(rs.getTime("gioBatDau")); } catch (Exception ignored) {}
            try { ca.setGioKetThuc(rs.getTime("gioKetThuc")); } catch (Exception ignored) {}
            hd.setCaLam(ca);
        }
        return hd;
    }

    /** Map đầy đủ (có JOIN tenNV, tenKH). */
    private HoaDon mapFull(ResultSet rs) throws SQLException {
        HoaDon hd = mapBasic(rs);
        if (hd.getNhanVien() != null)
            try { hd.getNhanVien().setTenNV(rs.getNString("tenNV")); } catch (Exception ignored) {}
        if (hd.getKhachHang() != null)
            try { hd.getKhachHang().setTenKH(rs.getNString("tenKH")); } catch (Exception ignored) {}
        return hd;
    }

    private void setNullable(PreparedStatement ps, int idx, String val) throws SQLException {
        if (val != null) ps.setString(idx, val);
        else             ps.setNull(idx, Types.VARCHAR);
    }

    private void logCreate(HoaDon hd) {
        String maDon = hd.getDonDatBan() != null ? SQLLogger.str(hd.getDonDatBan().getMaDon()) : "NULL";
        String maNV  = hd.getNhanVien()  != null ? SQLLogger.str(hd.getNhanVien().getMaNV())   : "NULL";
        String maKM  = hd.getKhuyenMai() != null ? SQLLogger.str(hd.getKhuyenMai().getMaKM())  : "NULL";
        String maCa  = hd.getCaLam()     != null ? SQLLogger.str(hd.getCaLam().getMaCa())      : "NULL";
        String ht    = hd.getHinhThucThanhToan() != null
                ? SQLLogger.nStr(hd.getHinhThucThanhToan().getDisplay()) : "NULL";
        SQLLogger.log(
                "INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc,maCa) VALUES ("
                        + SQLLogger.str(hd.getMaHD()) + "," + SQLLogger.ts(hd.getNgayLap()) + ","
                        + SQLLogger.str(hd.getThoiGian() != null ? hd.getThoiGian().toString() : "") + ","
                        + SQLLogger.num(hd.getTongTien()) + ","
                        + SQLLogger.nStr(hd.getTrangThaiThanhToan().getDisplay()) + "," + ht + ","
                        + maDon + "," + maNV + "," + SQLLogger.str(hd.getKhachHang().getMaKH()) + ","
                        + maKM + "," + SQLLogger.num(hd.getTienCoc()) + "," + maCa + ");");
    }
}