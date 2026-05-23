package dao;

import connectDB.ConnectDB;
import entity.KhuyenMai;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMai_DAO {

    private KhuyenMai mapRow(ResultSet rs) throws SQLException {
        KhuyenMai km = new KhuyenMai();
        km.setMaKM(rs.getString("maKM"));
        km.setTenKM(rs.getNString("tenKM"));
        km.setPhanTramGiam(rs.getDouble("phanTramGiam"));
        km.setNgayBatDau(rs.getDate("ngayBatDau"));
        km.setNgayKetThuc(rs.getDate("ngayKetThuc"));
        return km;
    }

    public List<KhuyenMai> getAll() {
        List<KhuyenMai> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;
        try {
            try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM KhuyenMai ORDER BY ngayBatDau DESC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public List<KhuyenMai> getKhuyenMaiHoatDong() {
        List<KhuyenMai> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;
        try {
            try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM KhuyenMai " +
                "WHERE CAST(GETDATE() AS DATE) BETWEEN ngayBatDau AND ngayKetThuc");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public String generateMaKM() {
        Connection con = ConnectDB.getConnection();
        if (con == null) return "KM001";
        try {
            try (PreparedStatement ps = con.prepareStatement(
                "SELECT MAX(maKM) FROM KhuyenMai");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) {
                    String last = rs.getString(1);
                    int num = Integer.parseInt(last.replaceAll("[^0-9]", "")) + 1;
                    return String.format("KM%03d", num);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "KM001";
    }

    public boolean add(KhuyenMai km) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO KhuyenMai (maKM, tenKM, phanTramGiam, ngayBatDau, ngayKetThuc) " +
                "VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, km.getMaKM());
                ps.setNString(2, km.getTenKM());
                ps.setDouble(3, km.getPhanTramGiam());
                ps.setDate(4, km.getNgayBatDau() != null ? new java.sql.Date(km.getNgayBatDau().getTime()) : null);
                ps.setDate(5, km.getNgayKetThuc() != null ? new java.sql.Date(km.getNgayKetThuc().getTime()) : null);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(KhuyenMai km) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            try (PreparedStatement ps = con.prepareStatement(
                "UPDATE KhuyenMai SET tenKM=?, phanTramGiam=?, ngayBatDau=?, ngayKetThuc=? " +
                "WHERE maKM=?")) {
                ps.setNString(1, km.getTenKM());
                ps.setDouble(2, km.getPhanTramGiam());
                ps.setDate(3, km.getNgayBatDau() != null ? new java.sql.Date(km.getNgayBatDau().getTime()) : null);
                ps.setDate(4, km.getNgayKetThuc() != null ? new java.sql.Date(km.getNgayKetThuc().getTime()) : null);
                ps.setString(5, km.getMaKM());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(String maKM) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM KhuyenMai WHERE maKM=?")) {
                ps.setString(1, maKM);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
