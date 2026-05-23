package dao;

import connectDB.ConnectDB;
import entity.Ban;
import entity.KhuVuc;
import entity.TrangThaiBan;
import util.SQLLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO cho bảng trung gian ChiTietDatBan(maDonDatBan, maBan).
 * <p>
 * Một DonDatBan có thể liên kết nhiều bàn thông qua bảng này.
 * Không sử dụng NhomBan hay maNhom – mỗi đơn đặt bàn tự quản lý
 * danh sách bàn của mình.
 * </p>
 */
public class ChiTietDatBan_DAO {

    // ── DDL tự động tạo bảng khi chưa tồn tại ────────────────────────────

    /**
     * Gọi một lần khi khởi động (hoặc trong ConnectDB.runMigrations).
     * Tạo bảng ChiTietDatBan nếu chưa có.
     */
    public static String getMigrationSQL() {
        return "IF NOT EXISTS ("
                + "SELECT 1 FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_NAME = 'ChiTietDatBan'"
                + ") "
                + "CREATE TABLE ChiTietDatBan ("
                + "  maDonDatBan NVARCHAR(20) NOT NULL, "
                + "  maBan       NVARCHAR(20) NOT NULL, "
                + "  PRIMARY KEY (maDonDatBan, maBan), "
                + "  FOREIGN KEY (maDonDatBan) REFERENCES DonDatBan(maDon), "
                + "  FOREIGN KEY (maBan)       REFERENCES Ban(maBan)"
                + ")";
    }

    // ── Thêm bàn vào đơn ─────────────────────────────────────────────────

    /**
     * Thêm một dòng (maDon, maBan) vào ChiTietDatBan.
     *
     * @return true nếu insert thành công
     */
    public boolean themBanVaoDon(String maDon, String maBan) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES (?, ?)";
            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, maDon);
                st.setString(2, maBan);
                int n = st.executeUpdate();
                if (n > 0) {
                    SQLLogger.log("INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ("
                            + SQLLogger.str(maDon) + ", " + SQLLogger.str(maBan) + ");");
                }
                return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Lấy danh sách bàn của đơn ────────────────────────────────────────

    /**
     * Trả về danh sách Ban đầy đủ thông tin của một đơn đặt bàn.
     */
    public List<Ban> getDsBanCuaDon(String maDon) {
        List<Ban> result = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT b.* FROM Ban b "
                    + "JOIN ChiTietDatBan ct ON b.maBan = ct.maBan "
                    + "WHERE ct.maDonDatBan = ?";
            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, maDon);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapBan(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ── Kiểm tra bàn có bị trùng lịch không ─────────────────────────────

    /**
     * Kiểm tra bàn có bị trùng với một khoảng thời gian mới không.
     * Overlap khi: thoiGianDen_new < tgRoi_existing AND tgDen_existing < thoiGianDuKienRoi_new
     *
     * @param maBan              mã bàn cần kiểm tra
     * @param thoiGianDen        giờ đến của đơn mới (full datetime)
     * @param thoiGianDuKienRoi  giờ dự kiến rời của đơn mới (full datetime)
     * @return true nếu bàn đã bị đặt trùng khoảng thời gian đó
     */
    public boolean isBanBiTrungLich(String maBan, Date thoiGianDen, Date thoiGianDuKienRoi) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT COUNT(*) FROM ChiTietDatBan ct "
                    + "JOIN DonDatBan d ON ct.maDonDatBan = d.maDon "
                    + "WHERE ct.maBan = ? "
                    + "  AND d.trangThai = 0 "
                    + "  AND d.thoiGianDen < ? "
                    + "  AND d.thoiGianDuKienRoi > ?";
            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, maBan);
                st.setTimestamp(2, new Timestamp(thoiGianDuKienRoi.getTime()));
                st.setTimestamp(3, new Timestamp(thoiGianDen.getTime()));
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Xóa liên kết bàn khỏi đơn ────────────────────────────────────────

    /**
     * Xóa 1 bàn cụ thể ra khỏi đơn đặt bàn.
     */
    public boolean xoaBanKhoiDon(String maDon, String maBan) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "DELETE FROM ChiTietDatBan WHERE maDonDatBan = ? AND maBan = ?";
            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, maDon);
                st.setString(2, maBan);
                int n = st.executeUpdate();
                if (n > 0) {
                    SQLLogger.log("DELETE FROM ChiTietDatBan WHERE maDonDatBan = "
                            + SQLLogger.str(maDon) + " AND maBan = " + SQLLogger.str(maBan) + ";");
                }
                return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa toàn bộ bàn liên kết với đơn (dùng khi hủy đơn hoặc xóa đơn).
     */
    public boolean xoaTatCaBanCuaDon(String maDon) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "DELETE FROM ChiTietDatBan WHERE maDonDatBan = ?";
            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, maDon);
                int n = st.executeUpdate();
                if (n > 0) {
                    SQLLogger.log("DELETE FROM ChiTietDatBan WHERE maDonDatBan = "
                            + SQLLogger.str(maDon) + ";");
                }
                return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    private Ban mapBan(ResultSet rs) throws SQLException {
        Ban b = new Ban();
        b.setMaBan(rs.getString("maBan"));
        b.setSoBan(rs.getInt("soBan"));
        b.setSucChua(rs.getInt("sucChua"));
        b.setLoaiBan(rs.getString("loaiBan"));
        KhuVuc kv = new KhuVuc();
        kv.setMaKV(rs.getString("maKV"));
        b.setKhuVuc(kv);
        b.setTinhTrangBan(TrangThaiBan.fromString(rs.getString("maTinhTrang")));
        return b;
    }
}
