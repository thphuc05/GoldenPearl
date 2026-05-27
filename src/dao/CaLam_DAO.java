package dao;

import connectDB.ConnectDB;
import entity.CaLam;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CaLam_DAO {

    public List<CaLam> getAll() {
        List<CaLam> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM CaLam ORDER BY gioBatDau")) {
            while (rs.next()) {
                ds.add(new CaLam(
                        rs.getString("maCa"),
                        rs.getString("tenCa"),
                        rs.getTime("gioBatDau"),
                        rs.getTime("gioKetThuc")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return ds;
    }

    /** Trả về ca làm khớp với giờ hiện tại, null nếu không tìm thấy. */
    public CaLam getCaHienTai() {
        Calendar cal = Calendar.getInstance();
        int nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        for (CaLam ca : getAll()) {
            if (ca.getGioBatDau() == null || ca.getGioKetThuc() == null) continue;
            int start = ca.getGioBatDau().toLocalTime().getHour() * 60 + ca.getGioBatDau().toLocalTime().getMinute();
            int end   = ca.getGioKetThuc().toLocalTime().getHour() * 60 + ca.getGioKetThuc().toLocalTime().getMinute();
            if (nowMin >= start && nowMin < end) return ca;
        }
        return null;
    }
}
