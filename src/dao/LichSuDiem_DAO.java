package dao;

import connectDB.ConnectDB;
import entity.LichSuDiem;
import java.sql.*;
import java.util.Date;

public class LichSuDiem_DAO {

    public String getNextMaGD() {
        Connection con = ConnectDB.getConnection();
        String ma = "GD001";
        if (con == null) return ma;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(maGiaoDich) FROM LichSuDiem")) {
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(2)) + 1;
                ma = String.format("GD%03d", num);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ma;
    }

    /** Tổng điểm hiện tại của khách (tính dẫn xuất từ lịch sử). */
    public int getTongDiem(String maKH) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return 0;
        try {
            String sql = "SELECT COALESCE(SUM(soGiaoDich), 0) FROM LichSuDiem WHERE maKH = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maKH);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Ghi một giao dịch điểm mới. */
    public boolean addGiaoDich(LichSuDiem lsd) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        try {
            String sql = "INSERT INTO LichSuDiem " +
                         "(maGiaoDich, maKH, maHD, soGiaoDich, loai, thoiGian, ghiChu) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, lsd.getMaGiaoDich());
                ps.setString(2, lsd.getMaKH());
                ps.setString(3, lsd.getMaHD());
                ps.setInt   (4, lsd.getSoGiaoDich());
                ps.setNString(5, lsd.getLoai());
                ps.setTimestamp(6, new Timestamp(
                        lsd.getThoiGian() != null ? lsd.getThoiGian().getTime() : System.currentTimeMillis()));
                ps.setNString(7, lsd.getGhiChu());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
