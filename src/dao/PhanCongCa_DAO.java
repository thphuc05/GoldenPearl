package dao;

import connectDB.ConnectDB;
import entity.CaLam;
import entity.NhanVien;
import entity.PhanCongCa;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class PhanCongCa_DAO {

    public List<PhanCongCa> getByWeek(Date startDate, Date endDate) {
        List<PhanCongCa> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;
        String sql = "SELECT pc.maPhanCong, pc.maCa, pc.maNV, pc.ngayLam, "
                + "ca.tenCa, ca.gioBatDau, ca.gioKetThuc, nv.tenNV "
                + "FROM PhanCongCa pc "
                + "JOIN CaLam ca ON pc.maCa = ca.maCa "
                + "JOIN NhanVien nv ON pc.maNV = nv.maNV "
                + "WHERE pc.ngayLam >= ? AND pc.ngayLam <= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CaLam ca = new CaLam(
                            rs.getString("maCa"), rs.getString("tenCa"),
                            rs.getTime("gioBatDau"), rs.getTime("gioKetThuc"));
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    nv.setTenNV(rs.getString("tenNV"));
                    PhanCongCa pc = new PhanCongCa(rs.getInt("maPhanCong"), ca, nv, rs.getDate("ngayLam"));
                    ds.add(pc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean addPhanCong(String maCa, String maNV, java.sql.Date ngayLam) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            String check = "SELECT 1 FROM PhanCongCa WHERE maCa=? AND maNV=? AND ngayLam=?";
            try (PreparedStatement ps = con.prepareStatement(check)) {
                ps.setString(1, maCa);
                ps.setString(2, maNV);
                ps.setDate(3, ngayLam);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return false;
                }
            }
            String sql = "INSERT INTO PhanCongCa (maCa, maNV, ngayLam) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maCa);
                ps.setString(2, maNV);
                ps.setDate(3, ngayLam);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePhanCong(int maPhanCong) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhanCongCa WHERE maPhanCong=?")) {
            ps.setInt(1, maPhanCong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePhanCongByKey(String maCa, String maNV, java.sql.Date ngayLam) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM PhanCongCa WHERE maCa=? AND maNV=? AND ngayLam=?")) {
            ps.setString(1, maCa);
            ps.setString(2, maNV);
            ps.setDate(3, ngayLam);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getMigrationSQL() {
        return "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='PhanCongCa') "
                + "CREATE TABLE PhanCongCa ("
                + "maPhanCong INT IDENTITY(1,1) PRIMARY KEY, "
                + "maCa VARCHAR(10) NOT NULL, "
                + "maNV VARCHAR(50) NOT NULL, "
                + "ngayLam DATE NOT NULL, "
                + "CONSTRAINT UQ_PhanCong UNIQUE (maCa, maNV, ngayLam))";
    }
}
