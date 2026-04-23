package dao;

import connectDB.ConnectDB;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.SanPham;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChiTietHoaDon_DAO {
    public List<ChiTietHoaDon> getChiTietByMaHD(String maHD) {
        List<ChiTietHoaDon> dsCTHD = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE maHD = ?";
        try {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, maHD);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String maMon = rs.getString("maMon");
                int soLuong = rs.getInt("soLuong");
                double donGia = rs.getDouble("donGia");
                String ghiChu = rs.getString("ghiChu");
                double thanhTien = rs.getDouble("thanhTien");

                SanPham sp = new SanPham();
                sp.setMaMon(maMon);

                HoaDon hd = new HoaDon();
                hd.setMaHD(maHD);

                ChiTietHoaDon ct = new ChiTietHoaDon(sp, hd, soLuong, donGia, ghiChu, thanhTien);
                dsCTHD.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsCTHD;
    }

    public boolean create(ChiTietHoaDon ct) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            stmt = con.prepareStatement("INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES(?, ?, ?, ?, ?, ?)");
            stmt.setString(1, ct.getHoaDon().getMaHD());
            stmt.setString(2, ct.getMonAn().getMaMon());
            stmt.setInt(3, ct.getSoLuong());
            stmt.setDouble(4, ct.getDonGia());
            stmt.setString(5, ct.getGhiChu());
            stmt.setDouble(6, ct.getThanhTien());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public Map<String, Integer> getTop5SellingDishes() {
        Map<String, Integer> topDishes = new HashMap<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT TOP 5 sp.tenMon, SUM(ct.soLuong) as totalQty " +
                         "FROM ChiTietHoaDon ct JOIN SanPham sp ON ct.maMon = sp.maMon " +
                         "GROUP BY sp.tenMon ORDER BY totalQty DESC";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                topDishes.put(rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topDishes;
    }
}
