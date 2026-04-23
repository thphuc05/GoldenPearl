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
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                LoaiSanPham loai = new LoaiSanPham();
                // Match SQL column names but map to new Entity methods
                loai.setMaLoai(rs.getString("maDanhMuc")); 
                loai.setTenLoai(rs.getString("tenDanhMuc"));
                loai.setMoTa(rs.getString("moTa"));
                dsLoai.add(loai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLoai;
    }
}
