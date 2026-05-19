package dao;

import connectDB.ConnectDB;
import entity.CaLam;
import entity.LichLamViec;
import entity.NhanVien;
import util.SQLLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho entity {@link LichLamViec}.
 *
 * <p>Bảng DB: LichLamViec (maLich IDENTITY, maNV, maCa, ngayLam, ghiChu)
 * Unique constraint: (maNV, maCa, ngayLam) – mỗi nhân viên chỉ có 1 lịch / ca / ngày.
 */
public class LichLamViec_DAO {

    public List<LichLamViec> getAll() {
        List<LichLamViec> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT l.*, nv.tenNV, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                    "FROM LichLamViec l " +
                    "JOIN NhanVien nv ON l.maNV = nv.maNV " +
                    "JOIN CaLam c ON l.maCa = c.maCa " +
                    "ORDER BY l.ngayLam DESC, c.gioBatDau";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) ds.add(mapFull(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /** Lấy lịch làm theo nhân viên. */
    public List<LichLamViec> getByNhanVien(String maNV) {
        List<LichLamViec> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT l.*, nv.tenNV, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                    "FROM LichLamViec l " +
                    "JOIN NhanVien nv ON l.maNV = nv.maNV " +
                    "JOIN CaLam c ON l.maCa = c.maCa " +
                    "WHERE l.maNV = ? ORDER BY l.ngayLam DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(mapFull(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /** Lấy danh sách nhân viên có lịch làm trong một ca + ngày cụ thể. */
    public List<LichLamViec> getByCaAndDate(String maCa, java.util.Date ngay) {
        List<LichLamViec> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT l.*, nv.tenNV, c.tenCa, c.gioBatDau, c.gioKetThuc " +
                    "FROM LichLamViec l " +
                    "JOIN NhanVien nv ON l.maNV = nv.maNV " +
                    "JOIN CaLam c ON l.maCa = c.maCa " +
                    "WHERE l.maCa = ? AND CAST(l.ngayLam AS DATE) = CAST(? AS DATE)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maCa);
            ps.setDate(2, new java.sql.Date(ngay.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(mapFull(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public String generateMaCa(CaLam ca, java.util.Date ngay) {
        // Quy tắc: CA + [S/C/T] + DDMMYY
        String type = ca.getTenCa().toLowerCase().contains("sáng") ? "S" :
                (ca.getTenCa().toLowerCase().contains("chiều") ? "C" : "T");
        return "CA" + type + new java.text.SimpleDateFormat("ddMMyy").format(ngay);
    }

    public boolean create(LichLamViec l) {
        Connection con = ConnectDB.getConnection();
        try {
            // SQL insert thông thường
            String sql = "INSERT INTO LichLamViec (maNV, maCa, ngayLam, ghiChu) VALUES (?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, l.getNhanVien().getMaNV());
            // Lấy mã đã được generate từ trước đó ở GUI truyền vào
            ps.setString(2, l.getCaLam().getMaCa());
            ps.setDate(3, new java.sql.Date(l.getNgayLam().getTime()));
            ps.setString(4, l.getGhiChu());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật lịch làm việc (delete rồi insert lại vì unique constraint).
     * @return true nếu thành công
     */
    public boolean update(int maLich, LichLamViec newData) {
        boolean deleted = delete(maLich);
        if (!deleted) return false;
        return create(newData);
    }

    /** Lấy lịch làm trong khoảng ngày (cho view lịch tuần). */
    public List<LichLamViec> getByDateRange(java.util.Date fromDate, java.util.Date toDate) {
        List<LichLamViec> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT l.*, nv.tenNV, c.tenCa, c.gioBatDau, c.gioKetThuc, c.trangThai " +
                    "FROM LichLamViec l " +
                    "JOIN NhanVien nv ON l.maNV = nv.maNV " +
                    "JOIN CaLam c ON l.maCa = c.maCa " +
                    "WHERE CAST(l.ngayLam AS DATE) BETWEEN CAST(? AS DATE) AND CAST(? AS DATE) " +
                    "ORDER BY l.ngayLam, c.gioBatDau";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(fromDate.getTime()));
            ps.setDate(2, new java.sql.Date(toDate.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(mapFull(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean delete(int maLich) {
        Connection con = ConnectDB.getConnection();
        int n = 0;
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM LichLamViec WHERE maLich = ?");
            ps.setInt(1, maLich);
            n = ps.executeUpdate();
            if (n > 0) SQLLogger.log("DELETE FROM LichLamViec WHERE maLich = " + maLich + ";");
        } catch (SQLException e) { e.printStackTrace(); }
        return n > 0;
    }

    // ── Migration SQL ────────────────────────────────────────────────────────

    public static String getMigrationSQL() {
        return "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_NAME = 'LichLamViec') " +
                "CREATE TABLE LichLamViec (" +
                "  maLich  INT          IDENTITY(1,1) PRIMARY KEY," +
                "  maNV    VARCHAR(10)  NOT NULL REFERENCES NhanVien(maNV)," +
                "  maCa    VARCHAR(10)  NOT NULL REFERENCES CaLam(maCa)," +
                "  ngayLam DATE         NOT NULL," +
                "  ghiChu  NVARCHAR(500) NULL," +
                "  CONSTRAINT UQ_LichLamViec UNIQUE (maNV, maCa, ngayLam)" +
                ")";
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private LichLamViec mapFull(ResultSet rs) throws SQLException {
        LichLamViec l = new LichLamViec();
        l.setMaLich(rs.getInt("maLich"));

        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getString("maNV"));
        try { nv.setTenNV(rs.getNString("tenNV")); } catch (Exception ignored) {}
        l.setNhanVien(nv);

        CaLam ca = new CaLam();
        ca.setMaCa(rs.getString("maCa"));
        try { ca.setTenCa(rs.getNString("tenCa")); } catch (Exception ignored) {}
        try { ca.setGioBatDau(rs.getTime("gioBatDau")); } catch (Exception ignored) {}
        try { ca.setGioKetThuc(rs.getTime("gioKetThuc")); } catch (Exception ignored) {}
        l.setCaLam(ca);

        l.setNgayLam(rs.getDate("ngayLam"));
        try { l.setGhiChu(rs.getNString("ghiChu")); } catch (Exception ignored) {}
        return l;
    }
}
