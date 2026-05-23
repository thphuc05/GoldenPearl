package dao;

import connectDB.ConnectDB;
import entity.CaLamViec;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CaLamViec_DAO {

    public List<CaLamViec> getAll() {
        List<CaLamViec> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) return ds;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM CaLamViec ORDER BY gioVao")) {
            while (rs.next()) {
                ds.add(new CaLamViec(
                        rs.getString("maCa"),
                        rs.getString("tenCa"),
                        rs.getString("gioVao"),
                        rs.getString("gioRa")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public static String getMigrationCreateTable() {
        return "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='CaLamViec') "
                + "CREATE TABLE CaLamViec ("
                + "maCa VARCHAR(10) PRIMARY KEY, "
                + "tenCa NVARCHAR(50) NOT NULL, "
                + "gioVao VARCHAR(10), "
                + "gioRa VARCHAR(10))";
    }

    public static String getMigrationSeedSang() {
        return "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='CaLamViec') "
                + "IF NOT EXISTS (SELECT 1 FROM CaLamViec WHERE maCa='CA_SANG') "
                + "INSERT INTO CaLamViec (maCa, tenCa, gioVao, gioRa) "
                + "VALUES ('CA_SANG', N'Ca Sáng', '06:00', '12:00')";
    }

    public static String getMigrationSeedChieu() {
        return "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='CaLamViec') "
                + "IF NOT EXISTS (SELECT 1 FROM CaLamViec WHERE maCa='CA_CHIEU') "
                + "INSERT INTO CaLamViec (maCa, tenCa, gioVao, gioRa) "
                + "VALUES ('CA_CHIEU', N'Ca Chiều', '12:00', '16:00')";
    }
}
