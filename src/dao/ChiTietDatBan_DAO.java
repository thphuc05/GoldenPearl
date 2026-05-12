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
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, maDon);
            st.setString(2, maBan);
            int n = st.executeUpdate();
            if (n > 0) {
                SQLLogger.log("INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES ("
                        + SQLLogger.str(maDon) + ", " + SQLLogger.str(maBan) + ");");
            }
            return n > 0;
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
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, maDon);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                result.add(mapBan(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ── Kiểm tra bàn có bị trùng lịch không ─────────────────────────────

    /**
     * Kiểm tra một bàn đã có đơn đặt chưa trong cùng ngày + khung giờ.
     *
     * @param maBan            mã bàn cần kiểm tra
     * @param ngayDen          ngày đặt (chỉ so sánh phần ngày)
     * @param khungGio         "SANG" / "CHIEU" / "TOI"
     * @param loaiTrangThai    false = đơn chưa hoàn thành (chưa thanh toán)
     * @return true nếu bàn đã bị đặt trong khung giờ đó
     */
    public boolean isBanBiTrungLich(String maBan, Date ngayDen,
                                    String khungGio, boolean loaiTrangThai) {
        Connection con = ConnectDB.getConnection();
        try {
            // So sánh ngày theo DAY/MONTH/YEAR để tránh lệch giờ
            String sql = "SELECT COUNT(*) FROM ChiTietDatBan ct "
                    + "JOIN DonDatBan d ON ct.maDonDatBan = d.maDon "
                    + "WHERE ct.maBan = ? "
                    + "  AND d.trangThai = ? "
                    + "  AND d.khungGio  = ? "
                    + "  AND CAST(d.thoiGianDen AS DATE) = CAST(? AS DATE)";
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, maBan);
            st.setBoolean(2, loaiTrangThai);
            st.setString(3, khungGio);
            st.setTimestamp(4, new Timestamp(ngayDen.getTime()));
            ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kiểm tra bàn có bị đặt (đơn chưa thanh toán) trong khung giờ không.
     * Đây là overload tiện dùng nhất (loaiTrangThai = false).
     */
    public boolean isBanBiTrungLich(String maBan, Date ngayDen, String khungGio) {
        return isBanBiTrungLich(maBan, ngayDen, khungGio, false);
    }

    // ── Xóa liên kết bàn khỏi đơn ────────────────────────────────────────

    /**
     * Xóa 1 bàn cụ thể ra khỏi đơn đặt bàn.
     */
    public boolean xoaBanKhoiDon(String maDon, String maBan) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "DELETE FROM ChiTietDatBan WHERE maDonDatBan = ? AND maBan = ?";
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, maDon);
            st.setString(2, maBan);
            int n = st.executeUpdate();
            if (n > 0) {
                SQLLogger.log("DELETE FROM ChiTietDatBan WHERE maDonDatBan = "
                        + SQLLogger.str(maDon) + " AND maBan = " + SQLLogger.str(maBan) + ";");
            }
            return n > 0;
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
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, maDon);
            int n = st.executeUpdate();
            if (n > 0) {
                SQLLogger.log("DELETE FROM ChiTietDatBan WHERE maDonDatBan = "
                        + SQLLogger.str(maDon) + ";");
            }
            return n > 0;
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