package dao;

import connectDB.ConnectDB;
import entity.*;
import util.SQLLogger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoaDon_DAO {

    // ══════════════════════════════════════════════════════════════════════
    //  READ
    // ══════════════════════════════════════════════════════════════════════

    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.* FROM HoaDon hd";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) ds.add(mapBasic(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return ds;
    }

    public HoaDon getHoaDonByMa(String maHD) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, nv.tenNV, kh.tenKH, kh.soDT AS khSoDT " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "WHERE hd.maHD = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHD);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapFull(rs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return null;
    }

    public HoaDon getHoaDonByMaDon(String maDon) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, nv.tenNV, kh.tenKH, kh.soDT AS khSoDT " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "WHERE hd.maDon = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapFull(rs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return null;
    }

    public List<HoaDon> getHoaDonByDateRange(java.util.Date fromDate, java.util.Date toDate) {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, nv.tenNV, kh.tenKH, kh.soDT AS khSoDT " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "WHERE hd.ngayLap BETWEEN ? AND ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setTimestamp(1, new Timestamp(fromDate.getTime()));
                ps.setTimestamp(2, new Timestamp(toDate.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ds.add(mapFull(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return ds;
    }

    public List<HoaDon> getHoaDonByMaCa(String maCa) {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, nv.tenNV, kh.tenKH, kh.soDT AS khSoDT " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "WHERE hd.maCa = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maCa);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ds.add(mapFull(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return ds;
    }

    public List<HoaDon> getHoaDonByMaCaToday(String maCa) {
        List<HoaDon> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, nv.tenNV, kh.tenKH, kh.soDT AS khSoDT " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                    "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                    "WHERE hd.maCa = ? AND CAST(hd.ngayLap AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maCa);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ds.add(mapFull(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return ds;
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
            try (PreparedStatement ps = con.prepareStatement(sql)) {
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
                setNullable(ps, 9, hd.getKhachHang() != null ? hd.getKhachHang().getMaKH() : null);
                setNullable(ps, 10, hd.getKhuyenMai() != null ? hd.getKhuyenMai().getMaKM() : null);
                ps.setDouble(11, hd.getTienCoc());
                setNullable(ps, 12, hd.getMaCa());
                n = ps.executeUpdate();
                if (n > 0) logCreate(hd);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return n > 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UPDATE
    // ══════════════════════════════════════════════════════════════════════

    public boolean updateThanhToan(String maHD, TrangThaiThanhToan trangThai,
                                   HinhThucThanhToan hinhThuc) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            boolean isDone = trangThai == TrangThaiThanhToan.DA_THANH_TOAN;
            String sql = isDone
                    ? "UPDATE HoaDon SET trangThaiThanhToan=?, hinhThucThanhToan=?, thoiGianRoiThucTe=GETDATE() WHERE maHD=?"
                    : "UPDATE HoaDon SET trangThaiThanhToan=?, hinhThucThanhToan=? WHERE maHD=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setNString(1, trangThai.getDisplay());
                if (hinhThuc != null) ps.setNString(2, hinhThuc.getDisplay());
                else                  ps.setNull(2, Types.NVARCHAR);
                ps.setString(3, maHD);
                n = ps.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "UPDATE HoaDon SET trangThaiThanhToan=" + SQLLogger.nStr(trangThai.getDisplay())
                                + ", hinhThucThanhToan=" + (hinhThuc != null ? SQLLogger.nStr(hinhThuc.getDisplay()) : "NULL")
                                + (isDone ? ", thoiGianRoiThucTe=GETDATE()" : "")
                                + " WHERE maHD=" + SQLLogger.str(maHD) + ";");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return n > 0;
    }

    public boolean updateTrangThai(String maHD, TrangThaiThanhToan trangThai) {
        return updateThanhToan(maHD, trangThai, null);
    }

    @Deprecated
    public boolean updateStatus(String maHD, boolean status) {
        return updateTrangThai(maHD, TrangThaiThanhToan.fromBoolean(status));
    }

    public boolean updateThoiGianRoi(String maHD) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE HoaDon SET thoiGianRoiThucTe = GETDATE() WHERE maHD = ?")) {
                ps.setString(1, maHD);
                n = ps.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "UPDATE HoaDon SET thoiGianRoiThucTe = GETDATE() WHERE maHD = "
                                + SQLLogger.str(maHD) + ";");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return n > 0;
    }

    public boolean updateKhachHangVaGhiChu(String maHD, String maKH, String ghiChu) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE HoaDon SET maKH=?, ghiChu=? WHERE maHD=?")) {
                ps.setString(1, maKH);
                ps.setNString(2, ghiChu);
                ps.setString(3, maHD);
                n = ps.executeUpdate();
                if (n > 0) SQLLogger.log("UPDATE HoaDon SET maKH=" + SQLLogger.str(maKH)
                        + ", ghiChu=" + SQLLogger.nStr(ghiChu)
                        + " WHERE maHD=" + SQLLogger.str(maHD) + ";");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return n > 0;
    }

    public boolean updateTongTien(String maHD, double total) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement ps = con.prepareStatement("UPDATE HoaDon SET tongTien=? WHERE maHD=?")) {
                ps.setDouble(1, total);
                ps.setString(2, maHD);
                n = ps.executeUpdate();
                if (n > 0) SQLLogger.log("UPDATE HoaDon SET tongTien=" + SQLLogger.num(total)
                        + " WHERE maHD=" + SQLLogger.str(maHD) + ";");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return n > 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════════════════════

    public boolean deleteHoaDon(String maHD) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM HoaDon WHERE maHD=?")) {
                ps.setString(1, maHD);
                n = ps.executeUpdate();
                if (n > 0) SQLLogger.log("DELETE FROM HoaDon WHERE maHD=" + SQLLogger.str(maHD) + ";");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return n > 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  KEY GENERATION
    // ══════════════════════════════════════════════════════════════════════

    public String getLatestMaHD() {
        Connection con = ConnectDB.getConnection();
        try {
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT TOP 1 maHD FROM HoaDon ORDER BY maHD DESC")) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
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
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT ISNULL(SUM(tongTien),0) FROM HoaDon " +
                            "WHERE ngayLap BETWEEN ? AND ? AND trangThaiThanhToan = N'Đã thanh toán'")) {
                ps.setTimestamp(1, new Timestamp(fromDate.getTime()));
                ps.setTimestamp(2, new Timestamp(toDate.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble(1);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return 0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MAP HELPERS
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
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) map.put(rs.getString("maHD"), rs.getString("tenKV"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
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
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
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
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return map;
    }

    /**
     * Trả về map maHD -> "HH:mm → HH:mm" (giờ đến – giờ rời dự kiến từ DonDatBan).
     * Dùng cột thoiGianDuKienRoi đã lưu trong DB.
     */
    public Map<String, String> getKhungGioMapForAllHoaDon() {
        Map<String, String> map = new HashMap<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.maHD, ddb.thoiGianDen, ddb.thoiGianDuKienRoi, ddb.soLuongKhach " +
                    "FROM HoaDon hd JOIN DonDatBan ddb ON hd.maDon = ddb.maDon";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
                while (rs.next()) {
                    Timestamp tgDen = rs.getTimestamp("thoiGianDen");
                    if (tgDen == null) continue;
                    Timestamp tgRoi = rs.getTimestamp("thoiGianDuKienRoi");
                    if (tgRoi == null) {
                        // Fallback: tính từ soLuongKhach nếu cột chưa có dữ liệu
                        int sl = rs.getInt("soLuongKhach");
                        int minutes = sl <= 2 ? 90 : sl <= 4 ? 120 : sl <= 8 ? 180 : 240;
                        tgRoi = new Timestamp(tgDen.getTime() + (long) minutes * 60_000);
                    }
                    map.put(rs.getString("maHD"),
                            timeFmt.format(tgDen) + " → " + timeFmt.format(tgRoi));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return map;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private HoaDon mapBasic(ResultSet rs) throws SQLException {
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
        try { hd.setMaCa(rs.getString("maCa")); } catch (Exception ignored) {}
        try { hd.setThoiGianRoiThucTe(rs.getTimestamp("thoiGianRoiThucTe")); } catch (Exception ignored) {}
        try { hd.setGhiChu(rs.getNString("ghiChu")); } catch (Exception ignored) {}

        String maDon = rs.getString("maDon");
        if (maDon != null) {
            DonDatBan don = new DonDatBan();
            don.setMaDon(maDon);
            hd.setDonDatBan(don);
        }

        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        hd.setNhanVien(nv);

        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        hd.setKhachHang(kh);
        return hd;
    }

    private HoaDon mapFull(ResultSet rs) throws SQLException {
        HoaDon hd = mapBasic(rs);
        if (hd.getNhanVien() != null)
            try { hd.getNhanVien().setTenNV(rs.getNString("tenNV")); } catch (Exception ignored) {}
        if (hd.getKhachHang() != null) {
            try { hd.getKhachHang().setTenKH(rs.getNString("tenKH")); } catch (Exception ignored) {}
            try { hd.getKhachHang().setSoDT(rs.getString("khSoDT")); } catch (Exception ignored) {}
        }
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
        String ht    = hd.getHinhThucThanhToan() != null
                ? SQLLogger.nStr(hd.getHinhThucThanhToan().getDisplay()) : "NULL";
        SQLLogger.log(
                "INSERT INTO HoaDon (maHD,ngayLap,thoiGian,tongTien,trangThaiThanhToan,hinhThucThanhToan,maDon,maNV,maKH,maKM,tienCoc) VALUES ("
                        + SQLLogger.str(hd.getMaHD()) + "," + SQLLogger.ts(hd.getNgayLap()) + ","
                        + SQLLogger.str(hd.getThoiGian() != null ? hd.getThoiGian().toString() : "") + ","
                        + SQLLogger.num(hd.getTongTien()) + ","
                        + SQLLogger.nStr(hd.getTrangThaiThanhToan().getDisplay()) + "," + ht + ","
                        + maDon + "," + maNV + "," + (hd.getKhachHang() != null ? SQLLogger.str(hd.getKhachHang().getMaKH()) : "NULL") + ","
                        + maKM + "," + SQLLogger.num(hd.getTienCoc()) + ");");
    }
}
