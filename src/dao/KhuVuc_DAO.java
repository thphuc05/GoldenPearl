package dao;

import connectDB.ConnectDB;
import entity.KhuVuc;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhuVuc_DAO {
    public List<KhuVuc> getAllKhuVuc() {
        List<KhuVuc> dsKV = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM KhuVuc";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                KhuVuc kv = new KhuVuc();
                kv.setMaKV(rs.getString("maKV"));
                kv.setTenKV(rs.getString("tenKV"));
                kv.setMoTa(rs.getString("moTa"));
                dsKV.add(kv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsKV;
    }

    public KhuVuc getKhuVucTheoMa(String maKV) {
        KhuVuc kv = null;
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM KhuVuc WHERE maKV = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, maKV);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                kv = new KhuVuc();
                kv.setMaKV(rs.getString("maKV"));
                kv.setTenKV(rs.getString("tenKV"));
                kv.setMoTa(rs.getString("moTa"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kv;
    }

    public boolean create(KhuVuc kv) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "INSERT INTO KhuVuc VALUES (?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, kv.getMaKV());
            statement.setString(2, kv.getTenKV());
            statement.setString(3, kv.getMoTa());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean update(KhuVuc kv) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "UPDATE KhuVuc SET tenKV = ?, moTa = ? WHERE maKV = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, kv.getTenKV());
            statement.setString(2, kv.getMoTa());
            statement.setString(3, kv.getMaKV());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean delete(String maKV) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "DELETE FROM KhuVuc WHERE maKV = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, maKV);
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
