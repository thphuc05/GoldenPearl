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
            // Câu lệnh SQL gốc bạn đã chạy thành công hồi sáng
            String sql = "SELECT sp.*, lsp.tenDanhMuc FROM SanPham sp JOIN LoaiSanPham lsp ON sp.maDanhMuc = lsp.maDanhMuc";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaMon(rs.getString("maMon"));
                sp.setTenMon(rs.getString("tenMon"));
                sp.setGiaGoc(rs.getDouble("giaGoc"));
                sp.setGiaBan(rs.getDouble("giaBan"));
                sp.setTrangThai(rs.getBoolean("trangThai"));
                
                // Khôi phục cơ chế an toàn: Bỏ qua nếu cột không tồn tại
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
            String sql = "INSERT INTO SanPham (maMon, tenMon, giaGoc, giaBan, trangThai, maDanhMuc) VALUES (?, ?, ?, ?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, sp.getMaMon());
            statement.setString(2, sp.getTenMon());
            statement.setDouble(3, sp.getGiaGoc());
            statement.setDouble(4, sp.getGiaBan());
            statement.setBoolean(5, sp.isTrangThai());
            statement.setString(6, sp.getLoaiSanPham().getMaLoai());
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
            String sql = "UPDATE SanPham SET tenMon = ?, giaGoc = ?, giaBan = ?, trangThai = ?, maDanhMuc = ? WHERE maMon = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, sp.getTenMon());
            statement.setDouble(2, sp.getGiaGoc());
            statement.setDouble(3, sp.getGiaBan());
            statement.setBoolean(4, sp.isTrangThai());
            statement.setString(5, sp.getLoaiSanPham().getMaLoai());
            statement.setString(6, sp.getMaMon());
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
