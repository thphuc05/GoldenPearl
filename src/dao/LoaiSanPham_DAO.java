package dao;

import connectDB.ConnectDB;
import entity.LoaiSanPham;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiSanPham_DAO {
    public List<LoaiSanPham> getAllLoaiSanPham() {
        List<LoaiSanPham> dsLoai = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM LoaiSanPham";
            try (Statement statement = con.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {
                while (rs.next()) {
                    LoaiSanPham loai = new LoaiSanPham();
                    // Match SQL column names but map to new Entity methods
                    loai.setMaLoai(rs.getString("maDanhMuc"));
                    loai.setTenLoai(rs.getString("tenDanhMuc"));
                    loai.setMoTa(rs.getString("moTa"));
                    dsLoai.add(loai);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLoai;
    }

    public boolean addLoaiSanPham(LoaiSanPham loai) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "INSERT INTO LoaiSanPham (maDanhMuc, tenDanhMuc, moTa) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, loai.getMaLoai());
                ps.setString(2, loai.getTenLoai());
                ps.setString(3, loai.getMoTa());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
