package dao;

import connectDB.ConnectDB;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.SanPham;
import entity.TrangThaiMon;
import util.SQLLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChiTietHoaDon_DAO {

    // ── Đọc chi tiết hóa đơn (kèm trạng thái món) ───────────────────────────
    public List<ChiTietHoaDon> getChiTietByMaHD(String maHD) {
        List<ChiTietHoaDon> dsCTHD = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT ct.*, sp.tenMon FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.maMon = sp.maMon WHERE ct.maHD = ?";
        try {
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, maHD);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        dsCTHD.add(mapRow(rs, maHD, null));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsCTHD;
    }

    public List<ChiTietHoaDon> getChiTietByMaHDWithLoai(String maHD) {
        List<ChiTietHoaDon> dsCTHD = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT ct.*, sp.tenMon, lsp.tenDanhMuc " +
                "FROM ChiTietHoaDon ct " +
                "JOIN SanPham sp ON ct.maMon = sp.maMon " +
                "LEFT JOIN LoaiSanPham lsp ON sp.maDanhMuc = lsp.maDanhMuc " +
                "WHERE ct.maHD = ?";
        try {
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                statement.setString(1, maHD);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String tenDanhMuc = rs.getString("tenDanhMuc");
                        ChiTietHoaDon ct = mapRow(rs, maHD, tenDanhMuc);
                        dsCTHD.add(ct);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsCTHD;
    }

    // ── Dùng cho màn hình bếp: lấy tất cả món chưa hoàn thành ───────────────
    // Trả về danh sách ChiTietHoaDon có thêm thông tin soBan qua SanPham.moTa (tạm dùng)
    // Thực tế: dùng MonBep DTO bên dưới
    public List<MonBep> getDsMonChoXuLy() {
        List<MonBep> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql =
            "SELECT ct.maHD, ct.maMon, sp.tenMon, ct.soLuong, ct.thoiGianGoi, ct.ghiChu, ct.trangThaiMon, " +
            "       COALESCE(MIN(b.soBan), 0) AS soBan, " +
            "       COALESCE(MIN(b.maBan), '') AS maBan " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDon hd ON ct.maHD = hd.maHD " +
            "JOIN SanPham sp ON ct.maMon = sp.maMon " +
            "LEFT JOIN ChiTietDatBan ctdb ON ctdb.maDonDatBan = hd.maDon " +
            "LEFT JOIN Ban b ON ctdb.maBan = b.maBan " +
            "WHERE ct.trangThaiMon IN (N'CHO_XU_LY', N'DANG_LAM') " +
            "  AND (hd.trangThai IS NULL OR hd.trangThai = 0) " +
            "GROUP BY ct.maHD, ct.maMon, sp.tenMon, ct.soLuong, ct.thoiGianGoi, ct.ghiChu, ct.trangThaiMon " +
            "ORDER BY soBan ASC, ct.thoiGianGoi ASC";
        try {
            try (PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MonBep m = new MonBep();
                    m.maHD        = rs.getString("maHD");
                    m.maMon       = rs.getString("maMon");
                    m.tenMon      = rs.getString("tenMon");
                    m.soLuong     = rs.getInt("soLuong");
                    m.thoiGianGoi = rs.getTimestamp("thoiGianGoi");
                    m.ghiChu      = rs.getString("ghiChu");
                    m.trangThaiMon = TrangThaiMon.fromString(rs.getString("trangThaiMon"));
                    m.soBan       = rs.getInt("soBan");
                    m.maBan       = rs.getString("maBan");
                    ds.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // ── Cập nhật trạng thái một món (bếp thao tác) ──────────────────────────
    public boolean updateTrangThaiMon(String maHD, String maMon, TrangThaiMon trangThai) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE ChiTietHoaDon SET trangThaiMon = ? WHERE maHD = ? AND maMon = ?")) {
                stmt.setString(1, trangThai.toDatabaseValue());
                stmt.setString(2, maHD);
                stmt.setString(3, maMon);
                n = stmt.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "UPDATE ChiTietHoaDon SET trangThaiMon = " + SQLLogger.str(trangThai.toDatabaseValue()) +
                        " WHERE maHD = " + SQLLogger.str(maHD) + " AND maMon = " + SQLLogger.str(maMon) + ";");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────
    public boolean create(ChiTietHoaDon ct) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien, trangThaiMon, thoiGianGoi) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, GETDATE())")) {
                stmt.setString(1, ct.getHoaDon().getMaHD());
                stmt.setString(2, ct.getMonAn().getMaMon());
                stmt.setInt(3, ct.getSoLuong());
                stmt.setDouble(4, ct.getDonGia());
                stmt.setString(5, ct.getGhiChu());
                stmt.setDouble(6, ct.getThanhTien());
                TrangThaiMon ttm = ct.getTrangThaiMon() != null ? ct.getTrangThaiMon() : TrangThaiMon.CHO_XU_LY;
                stmt.setString(7, ttm.toDatabaseValue());
                n = stmt.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien, trangThaiMon, thoiGianGoi) VALUES (" +
                        SQLLogger.str(ct.getHoaDon().getMaHD()) + ", " + SQLLogger.str(ct.getMonAn().getMaMon()) + ", " +
                        ct.getSoLuong() + ", " + SQLLogger.num(ct.getDonGia()) + ", " +
                        SQLLogger.nStr(ct.getGhiChu()) + ", " + SQLLogger.num(ct.getThanhTien()) + ", " +
                        SQLLogger.str(ttm.toDatabaseValue()) + ", GETDATE());");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean existsChiTiet(String maHD, String maMon) {
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement stmt = con.prepareStatement(
                    "SELECT COUNT(*) FROM ChiTietHoaDon WHERE maHD = ? AND maMon = ?")) {
                stmt.setString(1, maHD);
                stmt.setString(2, maMon);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Khi waiter tăng số lượng → reset trangThaiMon = CHO_XU_LY để bếp biết có thêm
    public boolean updateSoLuong(String maHD, String maMon, int soLuong, double thanhTien) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE ChiTietHoaDon SET soLuong = ?, thanhTien = ?, " +
                    "trangThaiMon = N'CHO_XU_LY', thoiGianGoi = GETDATE() " +
                    "WHERE maHD = ? AND maMon = ?")) {
                stmt.setInt(1, soLuong);
                stmt.setDouble(2, thanhTien);
                stmt.setString(3, maHD);
                stmt.setString(4, maMon);
                n = stmt.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "UPDATE ChiTietHoaDon SET soLuong = " + soLuong +
                        ", thanhTien = " + SQLLogger.num(thanhTien) +
                        ", trangThaiMon = 'CHO_XU_LY', thoiGianGoi = GETDATE()" +
                        " WHERE maHD = " + SQLLogger.str(maHD) +
                        " AND maMon = " + SQLLogger.str(maMon) + ";");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    public boolean deleteByMaHDAndMaMon(String maHD, String maMon) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement stmt = con.prepareStatement(
                    "DELETE FROM ChiTietHoaDon WHERE maHD = ? AND maMon = ?")) {
                stmt.setString(1, maHD);
                stmt.setString(2, maMon);
                n = stmt.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "DELETE FROM ChiTietHoaDon WHERE maHD = " + SQLLogger.str(maHD) +
                        " AND maMon = " + SQLLogger.str(maMon) + ";");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    public boolean deleteByMaHD(String maHD) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            try (PreparedStatement stmt = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHD = ?")) {
                stmt.setString(1, maHD);
                n = stmt.executeUpdate();
                if (n > 0) SQLLogger.log("DELETE FROM ChiTietHoaDon WHERE maHD = " + SQLLogger.str(maHD) + ";");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    // ── Analytics ────────────────────────────────────────────────────────────
    public double getProfitByDateRange(Timestamp start, Timestamp end) {
        double profit = 0;
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT SUM(ct.soLuong * (ct.donGia - sp.giaGoc)) " +
                "FROM ChiTietHoaDon ct " +
                "JOIN SanPham sp ON ct.maMon = sp.maMon " +
                "JOIN HoaDon hd ON ct.maHD = hd.maHD " +
                "WHERE hd.ngayLap BETWEEN ? AND ? AND hd.trangThaiThanhToan = N'Đã thanh toán'";
        try {
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setTimestamp(1, start);
                stmt.setTimestamp(2, end);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) profit = rs.getDouble(1);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return profit;
    }

    public Map<String, Double> getProfitGroupedByMaHD(Timestamp start, Timestamp end) {
        Map<String, Double> result = new HashMap<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT ct.maHD, SUM(ct.soLuong * (ct.donGia - sp.giaGoc)) " +
                "FROM ChiTietHoaDon ct " +
                "JOIN SanPham sp ON ct.maMon = sp.maMon " +
                "JOIN HoaDon hd ON ct.maHD = hd.maHD " +
                "WHERE hd.ngayLap BETWEEN ? AND ? AND hd.trangThaiThanhToan = N'Đã thanh toán' " +
                "GROUP BY ct.maHD";
        try {
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setTimestamp(1, start);
                stmt.setTimestamp(2, end);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) result.put(rs.getString(1), rs.getDouble(2));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public Map<String, Double> getRevenueByCategoryInDateRange(Timestamp start, Timestamp end) {
        Map<String, Double> result = new LinkedHashMap<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT lsp.tenDanhMuc, SUM(ct.thanhTien) " +
                "FROM ChiTietHoaDon ct " +
                "JOIN SanPham sp ON ct.maMon = sp.maMon " +
                "JOIN LoaiSanPham lsp ON sp.maDanhMuc = lsp.maDanhMuc " +
                "JOIN HoaDon hd ON ct.maHD = hd.maHD " +
                "WHERE hd.ngayLap BETWEEN ? AND ? AND hd.trangThaiThanhToan = N'Đã thanh toán' " +
                "GROUP BY lsp.tenDanhMuc";
        try {
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setTimestamp(1, start);
                stmt.setTimestamp(2, end);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) result.put(rs.getString(1), rs.getDouble(2));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public Map<String, Integer> getTop5SellingDishesByDateRange(Timestamp start, Timestamp end) {
        Map<String, Integer> topDishes = new LinkedHashMap<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT TOP 5 sp.tenMon, SUM(ct.soLuong) as totalQty " +
                    "FROM ChiTietHoaDon ct " +
                    "JOIN SanPham sp ON ct.maMon = sp.maMon " +
                    "JOIN HoaDon hd ON ct.maHD = hd.maHD " +
                    "WHERE hd.ngayLap BETWEEN ? AND ? AND hd.trangThaiThanhToan = N'Đã thanh toán' " +
                    "GROUP BY sp.tenMon ORDER BY totalQty DESC";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setTimestamp(1, start);
                stmt.setTimestamp(2, end);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) topDishes.put(rs.getString(1), rs.getInt(2));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return topDishes;
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private ChiTietHoaDon mapRow(ResultSet rs, String maHD, String tenDanhMuc) throws SQLException {
        String maMon      = rs.getString("maMon");
        String tenMon     = rs.getString("tenMon");
        int soLuong       = rs.getInt("soLuong");
        double donGia     = rs.getDouble("donGia");
        String ghiChu     = rs.getString("ghiChu");
        double thanhTien  = rs.getDouble("thanhTien");

        String trangThaiStr = null;
        try { trangThaiStr = rs.getString("trangThaiMon"); } catch (SQLException ignored) {}

        Timestamp thoiGianGoi = null;
        try { thoiGianGoi = rs.getTimestamp("thoiGianGoi"); } catch (SQLException ignored) {}

        SanPham sp = new SanPham();
        sp.setMaMon(maMon);
        sp.setTenMon(tenMon);
        if (tenDanhMuc != null) {
            entity.LoaiSanPham loai = new entity.LoaiSanPham();
            loai.setTenLoai(tenDanhMuc);
            sp.setLoaiSanPham(loai);
        }

        HoaDon hd = new HoaDon();
        hd.setMaHD(maHD);

        ChiTietHoaDon ct = new ChiTietHoaDon(sp, hd, soLuong, donGia, ghiChu, thanhTien);
        ct.setTrangThaiMon(TrangThaiMon.fromString(trangThaiStr));
        ct.setThoiGianGoi(thoiGianGoi);
        return ct;
    }

    // ── DTO dùng cho màn hình bếp ────────────────────────────────────────────
    public static class MonBep {
        public String maHD;
        public String maMon;
        public String tenMon;
        public int soLuong;
        public Timestamp thoiGianGoi;
        public String ghiChu;
        public TrangThaiMon trangThaiMon;
        public int soBan;
        public String maBan;
    }
}
