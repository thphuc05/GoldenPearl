package dao;

import connectDB.ConnectDB;
import entity.DonDatBan;
import entity.KhachHang;
import entity.NhanVien;
import entity.Ban;
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
                DonDatBan ddb = new DonDatBan();
                ddb.setMaDon(rs.getString("maDon"));
                ddb.setThoiGianDat(rs.getTimestamp("thoiGianDat"));
                ddb.setThoiGianDen(rs.getTimestamp("thoiGianDen"));
                ddb.setSoLuongKhach(rs.getInt("soLuongKhach"));

                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                ddb.setKhachHang(kh);

                ddb.setTrangThai(rs.getBoolean("trangThai"));

                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                ddb.setNhanVien(nv);

                Ban ban = new Ban();
                ban.setMaBan(rs.getString("maBan"));
                ddb.setBan(ban);

                dsDon.add(ddb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsDon;
    }

    public DonDatBan getDonDatBanByMa(String ma) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        try {
            String sql = "SELECT * FROM DonDatBan WHERE maDon = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                DonDatBan ddb = new DonDatBan();
                ddb.setMaDon(rs.getString("maDon"));
                ddb.setThoiGianDat(rs.getTimestamp("thoiGianDat"));
                ddb.setThoiGianDen(rs.getTimestamp("thoiGianDen"));
                ddb.setSoLuongKhach(rs.getInt("soLuongKhach"));

                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                ddb.setKhachHang(kh);

                ddb.setTrangThai(rs.getBoolean("trangThai"));

                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                ddb.setNhanVien(nv);

                Ban ban = new Ban();
                ban.setMaBan(rs.getString("maBan"));
                ddb.setBan(ban);

                return ddb;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addDonDatBan(DonDatBan ddb) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "INSERT INTO DonDatBan (maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, maBan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, ddb.getMaDon());
            statement.setTimestamp(2, new Timestamp(ddb.getThoiGianDat().getTime()));
            statement.setTimestamp(3, new Timestamp(ddb.getThoiGianDen().getTime()));
            statement.setInt(4, ddb.getSoLuongKhach());
            statement.setString(5, ddb.getKhachHang().getMaKH());
            statement.setBoolean(6, ddb.isTrangThai());
            statement.setString(7, ddb.getNhanVien().getMaNV());
            statement.setString(8, ddb.getBan().getMaBan());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean updateDonDatBan(DonDatBan ddb) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "UPDATE DonDatBan SET thoiGianDat = ?, thoiGianDen = ?, soLuongKhach = ?, maKH = ?, trangThai = ?, maNV = ?, maBan = ? WHERE maDon = ?";
            statement = con.prepareStatement(sql);
            statement.setTimestamp(1, new Timestamp(ddb.getThoiGianDat().getTime()));
            statement.setTimestamp(2, new Timestamp(ddb.getThoiGianDen().getTime()));
            statement.setInt(3, ddb.getSoLuongKhach());
            statement.setString(4, ddb.getKhachHang().getMaKH());
            statement.setBoolean(5, ddb.isTrangThai());
            statement.setString(6, ddb.getNhanVien().getMaNV());
            statement.setString(7, ddb.getBan().getMaBan());
            statement.setString(8, ddb.getMaDon());
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean deleteDonDatBan(String ma) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "DELETE FROM DonDatBan WHERE maDon = ?";
            statement = con.prepareStatement(sql);
            statement.setString(1, ma);
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
