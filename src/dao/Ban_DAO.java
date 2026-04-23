package dao;

import connectDB.ConnectDB;
import entity.Ban;
import entity.KhuVuc;
import entity.TrangThaiBan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Ban_DAO {
    public List<Ban> getAllBan() {
        List<Ban> dsBan = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM Ban";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Ban ban = new Ban();
                ban.setMaBan(rs.getString("maBan"));
                ban.setSoBan(rs.getInt("soBan"));
                ban.setSucChua(rs.getInt("sucChua"));
                ban.setLoaiBan(rs.getString("loaiBan"));

                KhuVuc kv = new KhuVuc();
                kv.setMaKV(rs.getString("maKV"));
                ban.setKhuVuc(kv);

                String maTinhTrang = rs.getString("maTinhTrang");
                ban.setTinhTrangBan(TrangThaiBan.fromString(maTinhTrang));

                dsBan.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsBan;
    }

    public boolean updateTinhTrangBan(String maBan, TrangThaiBan tinhTrang) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "UPDATE Ban SET maTinhTrang = ? WHERE maBan = ?";
            statement = con.prepareStatement(sql);
            // Assuming the DB uses the codes like 'TRONG', 'DANG_SD', etc.
            // Or use the name of the enum
            String dbValue = "TRONG";
            if (tinhTrang == TrangThaiBan.DaDuocDat) dbValue = "DAT_TRUOC";
            else if (tinhTrang == TrangThaiBan.DangDuocSuDung) dbValue = "DANG_SD";
            
            statement.setString(1, dbValue);
            statement.setString(2, maBan);
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
