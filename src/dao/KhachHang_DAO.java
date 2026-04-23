package dao;

import connectDB.ConnectDB;
import entity.KhachHang;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHang_DAO {
    public List<KhachHang> getAllKhachHang() {
        List<KhachHang> dsKH = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        Statement statement = null;
        try {
            String sql = "SELECT * FROM KhachHang";
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String maKH = rs.getString("maKH");
                String tenKH = rs.getString("tenKH");
                String soDT = rs.getString("soDT");
                String email = rs.getString("email");
                dsKH.add(new KhachHang(maKH, tenKH, soDT, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsKH;
    }

    public KhachHang getKhachHangByMa(String ma) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM KhachHang WHERE maKH = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String tenKH = rs.getString("tenKH");
                String soDT = rs.getString("soDT");
                String email = rs.getString("email");
                return new KhachHang(ma, tenKH, soDT, email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KhachHang getKhachHangBySdt(String sdt) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM KhachHang WHERE soDT = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, sdt);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String maKH = rs.getString("maKH");
                String tenKH = rs.getString("tenKH");
                String email = rs.getString("email");
                return new KhachHang(maKH, tenKH, sdt, email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "INSERT INTO KhachHang (maKH, tenKH, soDT, email) VALUES (?, ?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, kh.getMaKH());
            statement.setString(2, kh.getTenKH());
            statement.setString(3, kh.getSoDT());
            statement.setString(4, kh.getEmail());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean updateKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "UPDATE KhachHang SET tenKH = ?, soDT = ?, email = ? WHERE maKH = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, kh.getTenKH());
            statement.setString(2, kh.getSoDT());
            statement.setString(3, kh.getEmail());
            statement.setString(4, kh.getMaKH());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean deleteKhachHang(String ma) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "DELETE FROM KhachHang WHERE maKH = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
