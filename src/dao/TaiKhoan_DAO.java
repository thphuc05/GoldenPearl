package dao;

import connectDB.ConnectDB;
import entity.TaiKhoan;
import util.PasswordUtil;
import java.sql.*;

public class TaiKhoan_DAO {

    public TaiKhoan checkLogin(String tenTK, String matKhau) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM TaiKhoan WHERE tenTK = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tenTK);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stored = rs.getString("matKhau");
                        if (PasswordUtil.verify(matKhau, stored)) {
                            if (!PasswordUtil.isHashed(stored)) updateMatKhau(tenTK, matKhau);
                            return mapRow(rs, tenTK);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TaiKhoan getTaiKhoanByMaTK(String maTK) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM TaiKhoan WHERE maTK = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maTK);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs, rs.getString("tenTK"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TaiKhoan getTaiKhoanByTenTK(String tenTK) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM TaiKhoan WHERE tenTK = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tenTK);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs, tenTK);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createTaiKhoan(String maTK, String tenTK, String vaiTro) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "INSERT INTO TaiKhoan (maTK, tenTK, matKhau, vaiTro) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maTK);
                ps.setString(2, tenTK);
                ps.setString(3, PasswordUtil.hash("123456"));
                ps.setString(4, vaiTro);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isTenTKExists(String tenTK) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT 1 FROM TaiKhoan WHERE tenTK = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tenTK);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getNextMaTK() {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT MAX(maTK) FROM TaiKhoan WHERE maTK LIKE 'TK%'";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) {
                    int num = Integer.parseInt(rs.getString(1).substring(2)) + 1;
                    return String.format("TK%03d", num);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "TK001";
    }

    public boolean updateMatKhau(String tenTK, String matKhauMoi) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, PasswordUtil.hash(matKhauMoi));
                ps.setString(2, tenTK);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMatKhauByMaTK(String maTK, String matKhauMoi) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTK = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, PasswordUtil.hash(matKhauMoi));
                ps.setString(2, maTK);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public TaiKhoan checkQuenTK(String tenTK, String soDT) {
        String sql = "SELECT tk.* FROM TaiKhoan tk "
                + "JOIN NhanVien nv ON tk.maTK = nv.maTK "
                + "WHERE tk.tenTK = ? AND nv.soDT = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, tenTK);
            ps.setString(2, soDT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs, rs.getString("tenTK"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean resetMatKhau(String maTK, String soCCCD) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? "
                + "WHERE maTK = ? AND maTK IN "
                + "(SELECT maTK FROM NhanVien WHERE soCCCD = ?)";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash("123456"));
            ps.setString(2, maTK);
            ps.setString(3, soCCCD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private TaiKhoan mapRow(ResultSet rs, String tenTK) throws SQLException {
        return new TaiKhoan(
                rs.getString("maTK"),
                tenTK,
                rs.getString("matKhau"),
                rs.getString("vaiTro"));
    }
}
