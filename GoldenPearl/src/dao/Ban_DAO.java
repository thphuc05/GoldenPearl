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

        // ✅ THÊM kiểm tra null
        if (con == null) {
            System.err.println("❌ Không có kết nối DB, bỏ qua getAllBan()");
            return dsBan;
        }

        try {
            String sql = "SELECT * FROM Ban";
            Statement statement = con.createStatement();
            // ... phần còn lại giữ nguyên
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsBan;
    }
    public boolean updateTinhTrangBan(String maBan, TrangThaiBan tinhTrang) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        if (con == null) return false; // ✅ thêm dòng này
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
