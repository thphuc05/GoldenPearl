package dao;

import connectDB.ConnectDB;
import entity.CaLam;

import java.sql.*;
import java.util.ArrayList;
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
        }
        return ds;
    }
}
