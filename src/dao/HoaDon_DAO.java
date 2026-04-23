package dao;

import connectDB.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.NhanVien;
import entity.DonDatBan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDon_DAO {
    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> dsHD = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM HoaDon";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHD(rs.getString("maHD"));
                hd.setNgayLap(rs.getTimestamp("ngayLap"));
                hd.setTongTien(rs.getDouble("tongTien"));
                hd.setTrangThai(rs.getBoolean("trangThai"));
                
                DonDatBan don = new DonDatBan();
                don.setMaDon(rs.getString("maDon"));
                hd.setDonDatBan(don);

                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                hd.setNhanVien(nv);

                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                hd.setKhachHang(kh);

                String maKM = rs.getString("maKM");
                if (maKM != null) {
                    KhuyenMai km = new KhuyenMai();
                    km.setMaKM(maKM);
                    hd.setKhuyenMai(km);
                }

                dsHD.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsHD;
    }

    public boolean create(HoaDon hd) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement statement = null;
        int n = 0;
        try {
            String sql = "INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, maDon, maNV, maKH, maKM) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            statement = con.prepareStatement(sql);
            statement.setString(1, hd.getMaHD());
            statement.setTimestamp(2, new Timestamp(hd.getNgayLap().getTime()));
            statement.setDouble(3, hd.getTongTien());
            statement.setBoolean(4, hd.isTrangThai());
            if (hd.getDonDatBan() != null) {
                statement.setString(5, hd.getDonDatBan().getMaDon());
            } else {
                statement.setNull(5, Types.VARCHAR);
            }
            statement.setString(6, hd.getNhanVien().getMaNV());
            statement.setString(7, hd.getKhachHang().getMaKH());
            if (hd.getKhuyenMai() != null) {
                statement.setString(8, hd.getKhuyenMai().getMaKM());
            } else {
                statement.setNull(8, Types.VARCHAR);
            }
            n = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public String getLatestMaHD() {
        Connection con = ConnectDB.getConnection();
        String maHD = "";
        try {
            String sql = "SELECT TOP 1 maHD FROM HoaDon ORDER BY maHD DESC";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                maHD = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maHD;
    }

    public boolean updateStatus(String maHD, boolean status) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            stmt = con.prepareStatement("UPDATE HoaDon SET trangThai = ? WHERE maHD = ?");
            stmt.setBoolean(1, status);
            stmt.setString(2, maHD);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public HoaDon getHoaDonByMa(String maHD) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT * FROM HoaDon WHERE maHD = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, maHD);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHD(rs.getString("maHD"));
                hd.setNgayLap(rs.getTimestamp("ngayLap"));
                hd.setTongTien(rs.getDouble("tongTien"));
                hd.setTrangThai(rs.getBoolean("trangThai"));
                
                DonDatBan don = new DonDatBan();
                don.setMaDon(rs.getString("maDon"));
                hd.setDonDatBan(don);

                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                hd.setNhanVien(nv);

                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                hd.setKhachHang(kh);

                String maKM = rs.getString("maKM");
                if (maKM != null) {
                    KhuyenMai km = new KhuyenMai();
                    km.setMaKM(maKM);
                    hd.setKhuyenMai(km);
                }
                return hd;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<HoaDon> getHoaDonByDateRange(java.util.Date fromDate, java.util.Date toDate) {
        List<HoaDon> dsHD = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, nv.tenNV, kh.tenKH " +
                         "FROM HoaDon hd " +
                         "JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                         "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                         "WHERE hd.ngayLap BETWEEN ? AND ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setTimestamp(1, new Timestamp(fromDate.getTime()));
            statement.setTimestamp(2, new Timestamp(toDate.getTime()));
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHD(rs.getString("maHD"));
                hd.setNgayLap(rs.getTimestamp("ngayLap"));
                hd.setTongTien(rs.getDouble("tongTien"));
                hd.setTrangThai(rs.getBoolean("trangThai"));
                
                DonDatBan don = new DonDatBan();
                don.setMaDon(rs.getString("maDon"));
                hd.setDonDatBan(don);

                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setTenNV(rs.getString("tenNV"));
                hd.setNhanVien(nv);

                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                kh.setTenKH(rs.getString("tenKH"));
                hd.setKhachHang(kh);

                dsHD.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsHD;
    }

    public double getRevenueByDateRange(java.util.Date fromDate, java.util.Date toDate) {
        double total = 0;
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT SUM(tongTien) FROM HoaDon WHERE ngayLap BETWEEN ? AND ? AND trangThai = 1";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setTimestamp(1, new Timestamp(fromDate.getTime()));
            statement.setTimestamp(2, new Timestamp(toDate.getTime()));
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }
}
