package dao;

import connectDB.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.ChucVu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVien_DAO {
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> dsNV = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        Statement statement = null;
        try {
            String sql = "SELECT * FROM NhanVien";
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsNV;
    }

    public NhanVien getNhanVienByMa(String ma) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM NhanVien WHERE maNV = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
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

                return new NhanVien(ma, tenNV, soDT, soCCCD, cv, trangThai, tk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public NhanVien getNhanVienByMaTK(String maTK) {
        Connection con = ConnectDB.getConnection();
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
        }
        return null;
    }

    public boolean addNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "INSERT INTO NhanVien (maNV, tenNV, soDT, soCCCD, chucVu, trangThai, maTK) VALUES (?, ?, ?, ?, ?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, nv.getMaNV());
            statement.setString(2, nv.getTenNV());
            statement.setString(3, nv.getSoDT());
            statement.setString(4, nv.getSoCCCD());
            statement.setString(5, nv.getChucVu().getTenHienThi());
            statement.setBoolean(6, nv.isTrangThai());
            if (nv.getTaiKhoan() != null) {
                statement.setString(7, nv.getTaiKhoan().getMaTK());
            } else {
                statement.setNull(7, Types.VARCHAR);
            }
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean updateNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "UPDATE NhanVien SET tenNV = ?, soDT = ?, soCCCD = ?, chucVu = ?, trangThai = ?, maTK = ? WHERE maNV = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, nv.getTenNV());
            statement.setString(2, nv.getSoDT());
            statement.setString(3, nv.getSoCCCD());
            statement.setString(4, nv.getChucVu().getTenHienThi());
            statement.setBoolean(5, nv.isTrangThai());
            if (nv.getTaiKhoan() != null) {
                statement.setString(6, nv.getTaiKhoan().getMaTK());
            } else {
                statement.setNull(6, Types.VARCHAR);
            }
            statement.setString(7, nv.getMaNV());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean deleteNhanVien(String ma) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "DELETE FROM NhanVien WHERE maNV = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
