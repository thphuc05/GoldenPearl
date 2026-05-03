package dao;

import connectDB.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.NhanVien;
import entity.DonDatBan;
import util.SQLLogger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                hd.setThoiGian(rs.getTime("thoiGian"));
                hd.setTongTien(rs.getDouble("tongTien"));
                hd.setTrangThai(rs.getBoolean("trangThai"));
                try { hd.setTienCoc(rs.getDouble("tienCoc")); } catch (Exception ignored) {}

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
        int n = 0;
        try {
            String sql = "INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, hd.getMaHD());
            statement.setTimestamp(2, new Timestamp(hd.getNgayLap().getTime()));
            if (hd.getThoiGian() != null) {
                statement.setTime(3, hd.getThoiGian());
            } else {
                statement.setTime(3, new Time(hd.getNgayLap().getTime()));
            }
            statement.setDouble(4, hd.getTongTien());
            statement.setBoolean(5, hd.isTrangThai());
            if (hd.getDonDatBan() != null) {
                statement.setString(6, hd.getDonDatBan().getMaDon());
            } else {
                statement.setNull(6, Types.VARCHAR);
            }
            if (hd.getNhanVien() != null) {
                statement.setString(7, hd.getNhanVien().getMaNV());
            } else {
                statement.setNull(7, Types.VARCHAR);
            }
            statement.setString(8, hd.getKhachHang().getMaKH());
            if (hd.getKhuyenMai() != null) {
                statement.setString(9, hd.getKhuyenMai().getMaKM());
            } else {
                statement.setNull(9, Types.VARCHAR);
            }
            statement.setDouble(10, hd.getTienCoc());
            n = statement.executeUpdate();
            if (n > 0) {
                String maDon = hd.getDonDatBan() != null ? SQLLogger.str(hd.getDonDatBan().getMaDon()) : "NULL";
                String maNV  = hd.getNhanVien()  != null ? SQLLogger.str(hd.getNhanVien().getMaNV())   : "NULL";
                String maKM  = hd.getKhuyenMai() != null ? SQLLogger.str(hd.getKhuyenMai().getMaKM())  : "NULL";
                SQLLogger.log(
                    "INSERT INTO HoaDon (maHD, ngayLap, thoiGian, tongTien, trangThai, maDon, maNV, maKH, maKM, tienCoc) VALUES (" +
                    SQLLogger.str(hd.getMaHD()) + ", " + SQLLogger.ts(hd.getNgayLap()) + ", " +
                    (hd.getThoiGian() != null ? SQLLogger.str(hd.getThoiGian().toString()) : SQLLogger.ts(hd.getNgayLap())) + ", " +
                    SQLLogger.num(hd.getTongTien()) + ", " + SQLLogger.bit(hd.isTrangThai()) + ", " +
                    maDon + ", " + maNV + ", " + SQLLogger.str(hd.getKhachHang().getMaKH()) + ", " +
                    maKM + ", " + SQLLogger.num(hd.getTienCoc()) + ");");
            }
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

    public String getNextMaHD() {
        String latest = getLatestMaHD();
        if (latest == null || latest.isEmpty()) return "HD001";
        try {
            int num = Integer.parseInt(latest.replaceAll("[^0-9]", "")) + 1;
            return String.format("HD%03d", num);
        } catch (NumberFormatException e) {
            return "HD001";
        }
    }

    public boolean updateStatus(String maHD, boolean status) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement stmt = con.prepareStatement("UPDATE HoaDon SET trangThai = ? WHERE maHD = ?");
            stmt.setBoolean(1, status);
            stmt.setString(2, maHD);
            n = stmt.executeUpdate();
            if (n > 0) SQLLogger.log(
                "UPDATE HoaDon SET trangThai = " + SQLLogger.bit(status) +
                " WHERE maHD = " + SQLLogger.str(maHD) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean updateTongTien(String maHD, double total) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement stmt = con.prepareStatement("UPDATE HoaDon SET tongTien = ? WHERE maHD = ?");
            stmt.setDouble(1, total);
            stmt.setString(2, maHD);
            n = stmt.executeUpdate();
            if (n > 0) SQLLogger.log(
                "UPDATE HoaDon SET tongTien = " + SQLLogger.num(total) +
                " WHERE maHD = " + SQLLogger.str(maHD) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    public boolean deleteHoaDon(String maHD) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM HoaDon WHERE maHD = ?");
            stmt.setString(1, maHD);
            n = stmt.executeUpdate();
            if (n > 0) SQLLogger.log("DELETE FROM HoaDon WHERE maHD = " + SQLLogger.str(maHD) + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    public HoaDon getHoaDonByMa(String maHD) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, nv.tenNV, kh.tenKH " +
                         "FROM HoaDon hd " +
                         "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                         "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                         "WHERE hd.maHD = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, maHD);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return mapFull(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HoaDon getHoaDonByMaDon(String maDon) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.*, nv.tenNV, kh.tenKH " +
                         "FROM HoaDon hd " +
                         "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                         "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                         "WHERE hd.maDon = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, maDon);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return mapFull(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HoaDon mapFull(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getString("maHD"));
        hd.setNgayLap(rs.getTimestamp("ngayLap"));
        hd.setThoiGian(rs.getTime("thoiGian"));
        hd.setTongTien(rs.getDouble("tongTien"));
        hd.setTrangThai(rs.getBoolean("trangThai"));
        try { hd.setTienCoc(rs.getDouble("tienCoc")); } catch (Exception ignored) {}

        String maDon = rs.getString("maDon");
        if (maDon != null) {
            DonDatBan don = new DonDatBan();
            don.setMaDon(maDon);
            hd.setDonDatBan(don);
        }

        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        try { nv.setTenNV(rs.getString("tenNV")); } catch (Exception ignored) {}
        hd.setNhanVien(nv);

        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        try { kh.setTenKH(rs.getString("tenKH")); } catch (Exception ignored) {}
        hd.setKhachHang(kh);

        return hd;
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
                dsHD.add(mapFull(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsHD;
    }

    public Map<String, String> getKhuVucMapForAllHoaDon() {
        Map<String, String> map = new HashMap<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT hd.maHD, kv.tenKV " +
                         "FROM HoaDon hd " +
                         "JOIN DonDatBan ddb ON hd.maDon = ddb.maDon " +
                         "JOIN Ban b ON ddb.maBan = b.maBan " +
                         "JOIN KhuVuc kv ON b.maKV = kv.maKV";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) map.put(rs.getString("maHD"), rs.getString("tenKV"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
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
