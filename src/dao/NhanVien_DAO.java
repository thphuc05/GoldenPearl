package dao;

import connectDB.ConnectDB;
import entity.ChucVu;
import entity.NhanVien;
import entity.TaiKhoan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVien_DAO {

    public String getNextMaNV() {
        return getNextMaByPrefix("NV");
    }

    public String getNextMaByPrefix(String prefix) {
        Connection con = ConnectDB.getConnection();
        String ma = prefix + "001";
        if (con == null) return ma;
        try {
            String sql = "SELECT MAX(maNV) FROM NhanVien WHERE maNV LIKE ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, prefix + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getString(1) != null) {
                        int num = Integer.parseInt(rs.getString(1).substring(prefix.length())) + 1;
                        ma = String.format("%s%03d", prefix, num);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return ma;
    }

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> dsNV = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM NhanVien")) {
            while (rs.next()) {
                try { dsNV.add(mapRow(rs)); }
                catch (Exception e) { System.err.println("⚠️ Lỗi map NhanVien: " + e.getMessage()); }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return dsNV;
    }

    public List<NhanVien> searchNhanVien(String searchVal) {
        List<NhanVien> dsNV = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return dsNV;
        try {
            String sql = "SELECT * FROM NhanVien WHERE maNV LIKE ? OR tenNV LIKE ? OR soDT LIKE ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                String val = "%" + searchVal + "%";
                ps.setString(1, val); ps.setString(2, val); ps.setString(3, val);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) dsNV.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return dsNV;
    }

    public NhanVien getNhanVienByMaTK(String maTK) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;
        try {
            String sql = "SELECT * FROM NhanVien WHERE maTK = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maTK);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return null;
    }

    /** Tìm nhân viên theo email — dùng cho chức năng quên mật khẩu */
    public NhanVien getNhanVienByEmail(String email) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;
        try {
            String sql = "SELECT * FROM NhanVien WHERE email = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return null;
    }

    public boolean addNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            String sql = "INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, email, chucVu, trangThai, maTK) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nv.getMaNV());
                ps.setString(2, nv.getTenNV());
                ps.setString(3, nv.getSoDT());
                ps.setString(4, nv.getSoCCCD());
                ps.setString(5, nv.getEmail());
                ps.setString(6, nv.getChucVu().toDatabaseValue());
                ps.setBoolean(7, nv.isTrangThai());
                if (nv.getTaiKhoan() != null) ps.setString(8, nv.getTaiKhoan().getMaTK());
                else ps.setNull(8, Types.VARCHAR);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally { ConnectDB.closeConnection(); }
    }

    public boolean updateNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            String sql = "UPDATE NhanVien SET tenNV=?, soDT=?, soCCCD=?, email=?, chucVu=?, trangThai=?, maTK=? WHERE maNV=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nv.getTenNV());
                ps.setString(2, nv.getSoDT());
                ps.setString(3, nv.getSoCCCD());
                ps.setString(4, nv.getEmail());
                ps.setString(5, nv.getChucVu().toDatabaseValue());
                ps.setBoolean(6, nv.isTrangThai());
                if (nv.getTaiKhoan() != null) ps.setString(7, nv.getTaiKhoan().getMaTK());
                else ps.setNull(7, Types.VARCHAR);
                ps.setString(8, nv.getMaNV());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally { ConnectDB.closeConnection(); }
    }

    public boolean deleteNhanVien(String ma) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            String sql = "DELETE FROM NhanVien WHERE maNV = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ma);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally { ConnectDB.closeConnection(); }
    }

    public boolean setTrangThai(String maNV, boolean trangThai) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            String sql = "UPDATE NhanVien SET trangThai = ? WHERE maNV = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBoolean(1, trangThai);
                ps.setString(2, maNV);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return false;
    }

    // ── private helper ────────────────────────────────────────────────────────
    private NhanVien mapRow(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien(
                rs.getString("maNV"),
                rs.getString("tenNV"),
                rs.getString("soDT"),
                rs.getString("soCCCD"),
                ChucVu.fromString(rs.getString("chucVu")),
                rs.getBoolean("trangThai"),
                null);
        // email có thể chưa tồn tại trên DB cũ → dùng try/catch
        try { nv.setEmail(rs.getString("email")); } catch (Exception e) { System.err.println("WARN mapRow: " + e.getMessage()); }
        String maTK = rs.getString("maTK");
        if (maTK != null) {
            TaiKhoan tk = new TaiKhoan();
            tk.setMaTK(maTK);
            nv.setTaiKhoan(tk);
        }
        return nv;
    }
}
