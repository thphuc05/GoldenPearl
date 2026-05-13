package dao;

import connectDB.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.ChucVu;
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
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                String maxMa = rs.getString(1);
                int num = Integer.parseInt(maxMa.substring(prefix.length())) + 1;
                ma = String.format("%s%03d", prefix, num);
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ma;
    }

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> dsNV = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            System.err.println("❌ Lỗi: Connection is NULL");
            return null; // Trả về null để UI hiện thông báo lỗi kết nối
        }
        
        Statement statement = null;
        try {
            String sql = "SELECT * FROM NhanVien";
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                count++;
                try {
                    String maNV = rs.getString("maNV");
                    String tenNV = rs.getString("tenNV");
                    String soDT = rs.getString("soDT");
                    String soCCCD = rs.getString("soCCCD");
                    String chucVuStr = rs.getString("chucVu");
                    boolean trangThai = rs.getBoolean("trangThai");
                    String maTK = rs.getString("maTK");

                    ChucVu cv = ChucVu.fromString(chucVuStr);

                    TaiKhoan tk = null;
                    if (maTK != null) {
                        tk = new TaiKhoan();
                        tk.setMaTK(maTK);
                    }

                    dsNV.add(new NhanVien(maNV, tenNV, soDT, soCCCD, cv, trangThai, tk));
                } catch (Exception e) {
                    System.err.println("⚠️ Lỗi dòng " + count + ": " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dsNV;
    }

    public List<NhanVien> searchNhanVien(String searchVal) {
        List<NhanVien> dsNV = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return dsNV;
        
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM NhanVien WHERE maNV LIKE ? OR tenNV LIKE ? OR soDT LIKE ?";
            statement = con.prepareStatement(sql);
            String val = "%" + searchVal + "%";
            statement.setString(1, val);
            statement.setString(2, val);
            statement.setString(3, val);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String maNV = rs.getString("maNV");
                String tenNV = rs.getString("tenNV");
                String soDT = rs.getString("soDT");
                String soCCCD = rs.getString("soCCCD");
                String chucVuStr = rs.getString("chucVu");
                boolean trangThai = rs.getBoolean("trangThai");
                String maTK = rs.getString("maTK");
                ChucVu cv = ChucVu.fromString(chucVuStr);
                TaiKhoan tk = (maTK != null) ? new TaiKhoan(maTK, null, null, null) : null;
                dsNV.add(new NhanVien(maNV, tenNV, soDT, soCCCD, cv, trangThai, tk));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dsNV;
    }

    public NhanVien getNhanVienByMaTK(String maTK) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM NhanVien WHERE maTK = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, maTK);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String maNV = rs.getString("maNV");
                String tenNV = rs.getString("tenNV");
                String soDT = rs.getString("soDT");
                String soCCCD = rs.getString("soCCCD");
                String chucVuStr = rs.getString("chucVu");
                boolean trangThai = rs.getBoolean("trangThai");
                ChucVu cv = ChucVu.fromString(chucVuStr);
                TaiKhoan tk = new TaiKhoan();
                tk.setMaTK(maTK);
                return new NhanVien(maNV, tenNV, soDT, soCCCD, cv, trangThai, tk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean addNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        PreparedStatement statement = null;
        try {
            String sql = "INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES (?, ?, ?, ?, ?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, nv.getMaNV());
            statement.setString(2, nv.getTenNV());
            statement.setString(3, nv.getSoDT());
            statement.setString(4, nv.getSoCCCD());
            statement.setString(5, nv.getChucVu().toDatabaseValue());
            statement.setBoolean(6, nv.isTrangThai());
            if (nv.getTaiKhoan() != null) {
                statement.setString(7, nv.getTaiKhoan().getMaTK());
            } else {
                statement.setNull(7, Types.VARCHAR);
            }
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean updateNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        PreparedStatement statement = null;
        try {
            String sql = "UPDATE NhanVien SET tenNV = ?, soDT = ?, soCCCD = ?, chucVu = ?, trangThai = ?, maTK = ? WHERE maNV = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, nv.getTenNV());
            statement.setString(2, nv.getSoDT());
            statement.setString(3, nv.getSoCCCD());
            statement.setString(4, nv.getChucVu().toDatabaseValue());
            statement.setBoolean(5, nv.isTrangThai());
            if (nv.getTaiKhoan() != null) {
                statement.setString(6, nv.getTaiKhoan().getMaTK());
            } else {
                statement.setNull(6, Types.VARCHAR);
            }
            statement.setString(7, nv.getMaNV());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean deleteNhanVien(String ma) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        PreparedStatement statement = null;
        try {
            String sql = "DELETE FROM NhanVien WHERE maNV = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
