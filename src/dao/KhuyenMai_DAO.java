package dao;

import connectDB.ConnectDB;
import entity.KhuyenMai;

import java.sql.*;


public class KhuyenMai_DAO {
    public KhuyenMai getKhuyenMaiByMa(String maKM) {
        KhuyenMai voucher = null;
        Connection con = ConnectDB.getConnection();

        try {
            String sql = "SELECT * FROM KhuyenMai WHERE maKM = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maKM);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                voucher = new KhuyenMai(
                        rs.getString("maKM"),
                        rs.getString("tenKM"),
                        rs.getFloat("phanTramGiam"),
                        rs.getDate("ngayBatDau"),
                        rs.getDate("ngayKetThuc")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return voucher;
    }

    /**
     * Kiểm tra xem có voucher nào đang áp dụng không
     */
    public boolean isVoucherActive(String maKM) {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT COUNT(*) FROM KhuyenMai " +
                    "WHERE maKM = ? AND GETDATE() BETWEEN ngayBatDau AND ngayKetThuc";

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maKM);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy voucher áp dụng cho ngày cụ thể
     */
    public KhuyenMai getVoucherByDate(Object ngayLap) {
        KhuyenMai voucher = null;
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT TOP 1 * FROM KhuyenMai " +
                    "WHERE ? BETWEEN ngayBatDau AND ngayKetThuc " +
                    "ORDER BY phanTramGiam DESC";

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setObject(1, ngayLap);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                voucher = new KhuyenMai(
                        rs.getString("maKM"),
                        rs.getString("tenKM"),
                        rs.getFloat("phanTramGiam"),
                        rs.getDate("ngayBatDau"),
                        rs.getDate("ngayKetThuc")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return voucher;
    }
}
