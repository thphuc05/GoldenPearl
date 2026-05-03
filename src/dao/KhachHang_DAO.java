package dao;

import connectDB.ConnectDB;
import entity.KhachHang;
import util.SQLLogger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHang_DAO {
    public String getNextMaKH() {
        Connection con = ConnectDB.getConnection();
        String ma = "KH001";
        if (con == null) return ma;
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(maKH) FROM KhachHang");
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(2)) + 1;
                ma = String.format("KH%03d", num);
            }
            st.close();
        } catch (SQLException e) { e.printStackTrace(); }
        return ma;
    }

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
                dsKH.add(new KhachHang(maKH, tenKH, soDT));
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
                return new KhachHang(ma, tenKH, soDT);
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
                return new KhachHang(maKH, tenKH, sdt);
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
            String sql = "INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES (?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, kh.getMaKH());
            statement.setString(2, kh.getTenKH());
            statement.setString(3, kh.getSoDT());
            n = statement.executeUpdate();
            if (n > 0) SQLLogger.log(
                "INSERT INTO KhachHang (maKH, tenKH, soDT) VALUES (" +
                SQLLogger.str(kh.getMaKH()) + ", " + SQLLogger.nStr(kh.getTenKH()) + ", " +
                SQLLogger.str(kh.getSoDT()) + ");");
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
            String sql = "UPDATE KhachHang SET tenKH = ?, soDT = ? WHERE maKH = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, kh.getTenKH());
            statement.setString(2, kh.getSoDT());
            statement.setString(3, kh.getMaKH());
            n = statement.executeUpdate();
            if (n > 0) SQLLogger.log(
                "UPDATE KhachHang SET tenKH = " + SQLLogger.nStr(kh.getTenKH()) +
                ", soDT = " + SQLLogger.str(kh.getSoDT()) +
                " WHERE maKH = " + SQLLogger.str(kh.getMaKH()) + ";");
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
            if (n > 0) SQLLogger.log("DELETE FROM KhachHang WHERE maKH = " + SQLLogger.str(ma) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
