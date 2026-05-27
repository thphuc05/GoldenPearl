package dao;

import connectDB.ConnectDB;
import entity.Ban;
import entity.KhuVuc;
import entity.TrangThaiBan;
import util.SQLLogger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Ban_DAO {
    public List<Ban> getAllBan() {
        List<Ban> dsBan = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM Ban ORDER BY soBan ASC";
            try (Statement statement = con.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return dsBan;
    }

    public Ban getBanByMa(String maBan) {
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement st = con.prepareStatement("SELECT * FROM Ban WHERE maBan = ?")) {
                st.setString(1, maBan);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        Ban ban = new Ban();
                        ban.setMaBan(rs.getString("maBan"));
                        ban.setSoBan(rs.getInt("soBan"));
                        ban.setSucChua(rs.getInt("sucChua"));
                        ban.setLoaiBan(rs.getString("loaiBan"));
                        KhuVuc kv = new KhuVuc();
                        kv.setMaKV(rs.getString("maKV"));
                        ban.setKhuVuc(kv);
                        ban.setTinhTrangBan(TrangThaiBan.fromString(rs.getString("maTinhTrang")));
                        return ban;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return null;
    }

    public String getNextMaBan() {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT MAX(maBan) FROM Ban";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    String max = rs.getString(1);
                    if (max != null && max.toUpperCase().startsWith("BAN")) {
                        int num = Integer.parseInt(max.substring(3));
                        return String.format("BAN%03d", num + 1);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return "BAN001";
    }

    public boolean isSoBanExists(int soBan) {
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM Ban WHERE soBan = ?")) {
                st.setInt(1, soBan);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return false;
    }

    public boolean addBan(Ban ban) {
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement st = con.prepareStatement(
                "INSERT INTO Ban (maBan, soBan, sucChua, loaiBan, maKV, maTinhTrang) VALUES (?, ?, ?, ?, ?, 'TRONG')")) {
                st.setString(1, ban.getMaBan());
                st.setInt(2, ban.getSoBan());
                st.setInt(3, ban.getSucChua());
                st.setString(4, ban.getLoaiBan());
                st.setString(5, ban.getKhuVuc().getMaKV());
                return st.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return false;
    }

    public boolean updateBan(Ban ban) {
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement st = con.prepareStatement(
                    "UPDATE Ban SET soBan=?, sucChua=?, loaiBan=?, maKV=? WHERE maBan=?")) {
                st.setInt(1, ban.getSoBan());
                st.setInt(2, ban.getSucChua());
                st.setString(3, ban.getLoaiBan());
                st.setString(4, ban.getKhuVuc().getMaKV());
                st.setString(5, ban.getMaBan());
                return st.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return false;
    }

    public boolean deleteBan(String maBan) {
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement st = con.prepareStatement("DELETE FROM Ban WHERE maBan=?")) {
                st.setString(1, maBan);
                return st.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return false;
    }

    public boolean updateTinhTrangBan(String maBan, TrangThaiBan tinhTrang) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            String sql = "UPDATE Ban SET maTinhTrang = ? WHERE maBan = ?";
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                // Assuming the DB uses the codes like 'TRONG', 'DANG_SD', etc.
                // Or use the name of the enum
                String dbValue = "TRONG";
                if (tinhTrang == TrangThaiBan.DaDuocDat) dbValue = "DAT_TRUOC";
                else if (tinhTrang == TrangThaiBan.DangDuocSuDung) dbValue = "DANG_SD";
                else if (tinhTrang == TrangThaiBan.DangDonDep) dbValue = "DANG_DON";
                else if (tinhTrang == TrangThaiBan.BaoTri) dbValue = "BAO_TRI";

                statement.setString(1, dbValue);
                statement.setString(2, maBan);
                n = statement.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "UPDATE Ban SET maTinhTrang = " + SQLLogger.str(dbValue) +
                                " WHERE maBan = " + SQLLogger.str(maBan) + ";");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { ConnectDB.closeConnection(); }
        return n > 0;
    }
}
