package dao;

import connectDB.ConnectDB;
import entity.SanPham;
import entity.LoaiSanPham;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SanPham_DAO {
    public List<SanPham> getAllSanPham() {
        List<SanPham> dsSP = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            // Note: SQL currently doesn't have moTa or hinhAnh columns in SanPham table based on GoldenPearlDB.sql
            // If they are missing in SQL, this will throw an error. 
            // I'll keep them but be aware they might need to be added to SQL.
            String sql = "SELECT sp.*, lsp.tenDanhMuc FROM SanPham sp JOIN LoaiSanPham lsp ON sp.maDanhMuc = lsp.maDanhMuc";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaMon(rs.getString("maMon"));
                sp.setTenMon(rs.getString("tenMon"));
                sp.setDonGia(rs.getDouble("donGia"));
                sp.setTrangThai(rs.getBoolean("trangThai"));
                
                // Handling potentially missing columns in SQL safely or assuming they exist
                try { sp.setMoTa(rs.getString("moTa")); } catch (Exception e) {}
                try { sp.setHinhAnh(rs.getString("hinhAnh")); } catch (Exception e) {}

                LoaiSanPham loai = new LoaiSanPham();
                loai.setMaLoai(rs.getString("maDanhMuc"));
                loai.setTenLoai(rs.getString("tenDanhMuc"));
                sp.setLoaiSanPham(loai);

                dsSP.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsSP;
    }

    public boolean addSanPham(SanPham sp) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "INSERT INTO SanPham (maMon, tenMon, donGia, trangThai, maDanhMuc) VALUES (?, ?, ?, ?, ?)";
            // Add moTa and hinhAnh if they are in SQL
            // String sql = "INSERT INTO SanPham (maMon, tenMon, donGia, moTa, trangThai, maDanhMuc, hinhAnh) VALUES (?, ?, ?, ?, ?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, sp.getMaMon());
            statement.setString(2, sp.getTenMon());
            statement.setDouble(3, sp.getDonGia());
            statement.setBoolean(4, sp.isTrangThai());
            statement.setString(5, sp.getLoaiSanPham().getMaLoai());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean updateSanPham(SanPham sp) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "UPDATE SanPham SET tenMon = ?, donGia = ?, trangThai = ?, maDanhMuc = ? WHERE maMon = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, sp.getTenMon());
            statement.setDouble(2, sp.getDonGia());
            statement.setBoolean(3, sp.isTrangThai());
            statement.setString(4, sp.getLoaiSanPham().getMaLoai());
            statement.setString(5, sp.getMaMon());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean deleteSanPham(String ma) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "DELETE FROM SanPham WHERE maMon = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public String getLatestMaMon() {
        Connection con = ConnectDB.getConnection();
        String ma = "SP000";
        try {
            String sql = "SELECT TOP 1 maMon FROM SanPham ORDER BY maMon DESC";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                ma = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ma;
    }
}
