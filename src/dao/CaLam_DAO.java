package dao;

import connectDB.ConnectDB;
import entity.CaLam;
import util.SQLLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho entity {@link CaLam}.
 *
 * <p>Bảng DB: CaLam (maCa, tenCa, gioBatDau, gioKetThuc, trangThai)
 */
public class CaLam_DAO {

    // ── CRUD ──────────────────────────────────────────────────────────────

    public List<CaLam> getAllCaLam() {
        List<CaLam> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM CaLam ORDER BY gioBatDau");
            while (rs.next()) ds.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /** Chỉ lấy các ca đang hoạt động (trangThai = 1). */
    public List<CaLam> getActiveCaLam() {
        List<CaLam> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT * FROM CaLam WHERE trangThai = 1 ORDER BY gioBatDau");
            while (rs.next()) ds.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public CaLam getCaLamByMa(String maCa) {
        Connection con = ConnectDB.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM CaLam WHERE maCa = ?");
            ps.setString(1, maCa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean create(CaLam ca) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO CaLam (maCa, tenCa, gioBatDau, gioKetThuc) VALUES (?,?,?,?)");
            ps.setString(1, ca.getMaCa());
            ps.setNString(2, ca.getTenCa());
            ps.setTime(3, ca.getGioBatDau());
            ps.setTime(4, ca.getGioKetThuc());
            n = ps.executeUpdate();
            if (n > 0) SQLLogger.log(
                    "INSERT INTO CaLam (maCa,tenCa,gioBatDau,gioKetThuc) VALUES ("
                            + SQLLogger.str(ca.getMaCa()) + "," + SQLLogger.nStr(ca.getTenCa()) + ","
                            + SQLLogger.str(ca.getGioBatDau().toString()) + ","
                            + SQLLogger.str(ca.getGioKetThuc().toString()) + ","
                            );
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    public boolean update(CaLam ca) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE CaLam SET tenCa=?, gioBatDau=?, gioKetThuc=? WHERE maCa=?");
            ps.setNString(1, ca.getTenCa());
            ps.setTime(2, ca.getGioBatDau());
            ps.setTime(3, ca.getGioKetThuc());
            ps.setString(4, ca.getMaCa());
            n = ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    public boolean delete(String maCa) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM CaLam WHERE maCa = ?");
            ps.setString(1, maCa);
            n = ps.executeUpdate();
            if (n > 0) SQLLogger.log("DELETE FROM CaLam WHERE maCa = " + SQLLogger.str(maCa) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    // ── Auto-detect current shift ──────────────────────────────────────────

    /**
     * Tự động xác định ca hiện tại dựa theo giờ hệ thống.
     * Trả về ca có gioBatDau <= NOW < gioKetThuc.
     * Nếu không tìm thấy → trả về ca đầu tiên trong danh sách (fallback).
     */
    public CaLam getCurrentCaLam() {
        Connection con = ConnectDB.getConnection();
        try {
            // So sánh giờ hiện tại với khoảng thời gian của từng ca
            String sql = "SELECT TOP 1 * FROM CaLam " +
                    "WHERE trangThai = 1 " +
                    "AND CAST(GETDATE() AS TIME) >= gioBatDau " +
                    "AND CAST(GETDATE() AS TIME) < gioKetThuc " +
                    "ORDER BY gioBatDau";
            ResultSet rs = con.createStatement().executeQuery(sql);
            if (rs.next()) return map(rs);

            // Fallback: lấy ca đầu tiên
            rs = con.createStatement().executeQuery(
                    "SELECT TOP 1 * FROM CaLam WHERE trangThai = 1 ORDER BY gioBatDau");
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Auto-generate maCa ───────────────────────────────────────────────

    public String getNextMaCa() {
        Connection con = ConnectDB.getConnection();
        try {
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT TOP 1 maCa FROM CaLam ORDER BY maCa DESC");
            if (rs.next()) {
                String last = rs.getString(1);
                int num = Integer.parseInt(last.replaceAll("[^0-9]", "")) + 1;
                return String.format("CA%03d", num);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "CA001";
    }

    // ── SQL untuk migration (gọi từ ConnectDB) ──────────────────────────────

    public static String getMigrationSQL() {
        return "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_NAME = 'CaLam') " +
                "CREATE TABLE CaLam (" +
                "  maCa       VARCHAR(10)   NOT NULL PRIMARY KEY," +
                "  tenCa      NVARCHAR(50)  NOT NULL," +
                "  gioBatDau  TIME          NOT NULL," +
                "  gioKetThuc TIME          NOT NULL," +
                ")";
    }

    /** Dữ liệu mẫu 3 ca mặc định. */
    public static String getSeedSQL() {
        return "IF NOT EXISTS (SELECT 1 FROM CaLam) " +
                "INSERT INTO CaLam (maCa,tenCa,gioBatDau,gioKetThuc,trangThai) VALUES " +
                "('CA001',N'Ca sáng','06:00','14:00',1)," +
                "('CA002',N'Ca chiều','14:00','22:00',1)," +
                "('CA003',N'Ca tối','22:00','06:00',1)";
    }

    // ── Mapper ──────────────────────────────────────────────────────────────

    private CaLam map(ResultSet rs) throws SQLException {
        CaLam ca = new CaLam();
        ca.setMaCa(rs.getString("maCa"));
        ca.setTenCa(rs.getNString("tenCa"));
        ca.setGioBatDau(rs.getTime("gioBatDau"));
        ca.setGioKetThuc(rs.getTime("gioKetThuc"));
        // XÓA DÒNG NÀY ĐI: ca.setTrangThai(rs.getBoolean("trangThai"));
        return ca;
    }
}
