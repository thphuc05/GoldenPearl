package dao;

import connectDB.ConnectDB;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.SanPham;
import util.SQLLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChiTietHoaDon_DAO {
    public List<ChiTietHoaDon> getChiTietByMaHD(String maHD) {
        List<ChiTietHoaDon> dsCTHD = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT ct.*, sp.tenMon FROM ChiTietHoaDon ct JOIN SanPham sp ON ct.maMon = sp.maMon WHERE ct.maHD = ?";
        try {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, maHD);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String maMon = rs.getString("maMon");
                String tenMon = rs.getString("tenMon");
                int soLuong = rs.getInt("soLuong");
                double donGia = rs.getDouble("donGia");
                String ghiChu = rs.getString("ghiChu");
                double thanhTien = rs.getDouble("thanhTien");

                SanPham sp = new SanPham();
                sp.setMaMon(maMon);
                sp.setTenMon(tenMon);

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
            if (n > 0) SQLLogger.log(
                "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, donGia, ghiChu, thanhTien) VALUES (" +
                SQLLogger.str(ct.getHoaDon().getMaHD()) + ", " + SQLLogger.str(ct.getMonAn().getMaMon()) + ", " +
                ct.getSoLuong() + ", " + SQLLogger.num(ct.getDonGia()) + ", " +
                SQLLogger.nStr(ct.getGhiChu()) + ", " + SQLLogger.num(ct.getThanhTien()) + ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean existsChiTiet(String maHD, String maMon) {
        Connection con = ConnectDB.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement(
                "SELECT COUNT(*) FROM ChiTietHoaDon WHERE maHD = ? AND maMon = ?");
            stmt.setString(1, maHD);
            stmt.setString(2, maMon);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateSoLuong(String maHD, String maMon, int soLuong, double thanhTien) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement stmt = con.prepareStatement(
                "UPDATE ChiTietHoaDon SET soLuong = ?, thanhTien = ? WHERE maHD = ? AND maMon = ?");
            stmt.setInt(1, soLuong);
            stmt.setDouble(2, thanhTien);
            stmt.setString(3, maHD);
            stmt.setString(4, maMon);
            n = stmt.executeUpdate();
            if (n > 0) SQLLogger.log(
                "UPDATE ChiTietHoaDon SET soLuong = " + soLuong +
                ", thanhTien = " + SQLLogger.num(thanhTien) +
                " WHERE maHD = " + SQLLogger.str(maHD) +
                " AND maMon = " + SQLLogger.str(maMon) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    public boolean deleteByMaHD(String maHD) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHD = ?");
            stmt.setString(1, maHD);
            n = stmt.executeUpdate();
            if (n > 0) SQLLogger.log("DELETE FROM ChiTietHoaDon WHERE maHD = " + SQLLogger.str(maHD) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
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

    public double getProfitByDateRange(Timestamp start, Timestamp end) {
        double profit = 0;
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT SUM(ct.soLuong * (ct.donGia - sp.giaGoc)) " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.maMon = sp.maMon " +
                     "JOIN HoaDon hd ON ct.maHD = hd.maHD " +
                     "WHERE hd.ngayLap BETWEEN ? AND ? AND hd.trangThai = 1";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                profit = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profit;
    }

    public Map<String, Double> getProfitGroupedByMaHD(Timestamp start, Timestamp end) {
        Map<String, Double> result = new HashMap<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT ct.maHD, SUM(ct.soLuong * (ct.donGia - sp.giaGoc)) " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.maMon = sp.maMon " +
                     "JOIN HoaDon hd ON ct.maHD = hd.maHD " +
                     "WHERE hd.ngayLap BETWEEN ? AND ? AND hd.trangThai = 1 " +
                     "GROUP BY ct.maHD";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) result.put(rs.getString(1), rs.getDouble(2));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public Map<String, Double> getRevenueByCategoryInDateRange(Timestamp start, Timestamp end) {
        Map<String, Double> result = new LinkedHashMap<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT lsp.tenDanhMuc, SUM(ct.thanhTien) " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.maMon = sp.maMon " +
                     "JOIN LoaiSanPham lsp ON sp.maDanhMuc = lsp.maDanhMuc " +
                     "JOIN HoaDon hd ON ct.maHD = hd.maHD " +
                     "WHERE hd.ngayLap BETWEEN ? AND ? AND hd.trangThai = 1 " +
                     "GROUP BY lsp.tenDanhMuc";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
