package dao;

import connectDB.ConnectDB;
import entity.TaiKhoan;
import java.sql.*;

public class TaiKhoan_DAO {
    public TaiKhoan checkLogin(String tenTK, String matKhau) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM TaiKhoan WHERE tenTK = ? AND matKhau = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, tenTK);
            statement.setString(2, matKhau);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String maTK = rs.getString("maTK");
                String vaiTro = rs.getString("vaiTro");
                return new TaiKhoan(maTK, tenTK, matKhau, vaiTro);
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

    public TaiKhoan getTaiKhoanByTenTK(String tenTK) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM TaiKhoan WHERE tenTK = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, tenTK);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String maTK = rs.getString("maTK");
                String matKhau = rs.getString("matKhau");
                String vaiTro = rs.getString("vaiTro");
                return new TaiKhoan(maTK, tenTK, matKhau, vaiTro);
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

    public boolean updateMatKhau(String tenTK, String matKhauMoi) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, matKhauMoi);
            statement.setString(2, tenTK);
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return n > 0;
    }
}
