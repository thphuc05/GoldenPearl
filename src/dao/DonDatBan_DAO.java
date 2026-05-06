package dao;

import connectDB.ConnectDB;
import entity.DonDatBan;
import entity.KhachHang;
import entity.NhanVien;
import entity.Ban;
import util.SQLLogger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonDatBan_DAO {
    public List<DonDatBan> getAllDonDatBan() {
        List<DonDatBan> dsDon = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM DonDatBan";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                DonDatBan ddb = mapRow(rs);
                dsDon.add(ddb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsDon;
    }

    public DonDatBan getDonDatBanByMa(String ma) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM DonDatBan WHERE maDon = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DonDatBan getDonDatBanByMaBan(String maBan) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT TOP 1 * FROM DonDatBan WHERE maBan = ? AND trangThai = 0 ORDER BY thoiGianDat DESC";
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, maBan);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public String getNextMaDon() {
        Connection con = ConnectDB.getConnection();
        String ma = "DDB001";
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(maDon) FROM DonDatBan");
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(3)) + 1;
                ma = String.format("DDB%03d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ma;
    }

    public boolean addDonDatBan(DonDatBan ddb) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            String sql = "INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, ddb.getMaDon());
            statement.setTimestamp(2, new Timestamp(ddb.getThoiGianDat().getTime()));
            statement.setTimestamp(3, new Timestamp(ddb.getThoiGianDen().getTime()));
            statement.setInt(4, ddb.getSoLuongKhach());
            statement.setString(5, ddb.getKhachHang().getMaKH());
            statement.setBoolean(6, ddb.isTrangThai());
            if (ddb.getNhanVien() != null) {
                statement.setString(7, ddb.getNhanVien().getMaNV());
            } else {
                statement.setNull(7, Types.VARCHAR);
            }
            statement.setString(8, ddb.getBan().getMaBan());
            statement.setString(9, ddb.getKhungGio());
            statement.setString(10, ddb.getGhiChu() != null ? ddb.getGhiChu() : "");
            n = statement.executeUpdate();
            if (n > 0) {
                String maNV = ddb.getNhanVien() != null ? SQLLogger.str(ddb.getNhanVien().getMaNV()) : "NULL";
                SQLLogger.log(
                    "INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan, khungGio) VALUES (" +
                    SQLLogger.str(ddb.getMaDon()) + ", " + SQLLogger.ts(ddb.getThoiGianDat()) + ", " +
                    SQLLogger.ts(ddb.getThoiGianDen()) + ", " + ddb.getSoLuongKhach() + ", " +
                    SQLLogger.str(ddb.getKhachHang().getMaKH()) + ", " + SQLLogger.bit(ddb.isTrangThai()) + ", " +
                    maNV + ", " + SQLLogger.str(ddb.getBan().getMaBan()) + ", " +
                    SQLLogger.str(ddb.getKhungGio()) + ");");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean updateDonDatBan(DonDatBan ddb) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            String sql = "UPDATE DonDatBan SET thoiGianDat = ?, thoiGianDen = ?, soLuongKhach = ?, maKH = ?, trangThai = ?, maNV = ?, maBan = ?, ghiChu = ? WHERE maDon = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setTimestamp(1, new Timestamp(ddb.getThoiGianDat().getTime()));
            statement.setTimestamp(2, new Timestamp(ddb.getThoiGianDen().getTime()));
            statement.setInt(3, ddb.getSoLuongKhach());
            statement.setString(4, ddb.getKhachHang().getMaKH());
            statement.setBoolean(5, ddb.isTrangThai());
            if (ddb.getNhanVien() != null) {
                statement.setString(6, ddb.getNhanVien().getMaNV());
            } else {
                statement.setNull(6, Types.VARCHAR);
            }
            statement.setString(7, ddb.getBan().getMaBan());
            statement.setString(8, ddb.getGhiChu() != null ? ddb.getGhiChu() : "");
            statement.setString(9, ddb.getMaDon());
            n = statement.executeUpdate();
            if (n > 0) {
                String maNV = ddb.getNhanVien() != null ? SQLLogger.str(ddb.getNhanVien().getMaNV()) : "NULL";
                SQLLogger.log(
                    "UPDATE DonDatBan SET thoiGianDat = " + SQLLogger.ts(ddb.getThoiGianDat()) +
                    ", thoiGianDen = " + SQLLogger.ts(ddb.getThoiGianDen()) +
                    ", soLuongKhach = " + ddb.getSoLuongKhach() +
                    ", maKH = " + SQLLogger.str(ddb.getKhachHang().getMaKH()) +
                    ", trangThai = " + SQLLogger.bit(ddb.isTrangThai()) +
                    ", maNV = " + maNV +
                    ", maBan = " + SQLLogger.str(ddb.getBan().getMaBan()) +
                    " WHERE maDon = " + SQLLogger.str(ddb.getMaDon()) + ";");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean deleteDonDatBan(String ma) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            String sql = "DELETE FROM DonDatBan WHERE maDon = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            n = statement.executeUpdate();
            if (n > 0) SQLLogger.log("DELETE FROM DonDatBan WHERE maDon = " + SQLLogger.str(ma) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    private DonDatBan mapRow(ResultSet rs) throws SQLException {
        DonDatBan ddb = new DonDatBan();
        ddb.setMaDon(rs.getString("maDon"));
        ddb.setThoiGianDat(rs.getTimestamp("thoiGianDat"));
        ddb.setThoiGianDen(rs.getTimestamp("thoiGianDen"));
        ddb.setSoLuongKhach(rs.getInt("soLuongKhach"));
        ddb.setTrangThai(rs.getBoolean("trangThai"));
        try { ddb.setKhungGio(rs.getString("khungGio")); } catch (Exception ignored) {}
        try { ddb.setGhiChu(rs.getString("ghiChu")); }    catch (Exception ignored) {}
        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        ddb.setKhachHang(kh);
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        ddb.setNhanVien(nv);
        Ban ban = new Ban();
        ban.setMaBan(rs.getString("maBan"));
        ddb.setBan(ban);
        return ddb;
    }
}
