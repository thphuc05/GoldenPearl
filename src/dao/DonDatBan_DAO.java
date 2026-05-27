package dao;

import connectDB.ConnectDB;
import entity.Ban;
import entity.DonDatBan;
import entity.KhachHang;
import entity.KhuVuc;
import entity.NhanVien;
import entity.TrangThaiBan;
import util.SQLLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho DonDatBan.
 * <p>
 * Một DonDatBan liên kết nhiều Ban thông qua bảng trung gian ChiTietDatBan.
 * Không dùng cột maBan trực tiếp trong bảng DonDatBan nữa.
 * Không dùng NhomBan / maNhom.
 * </p>
 *
 * Pseudo flow khi thêm mới:
 * <pre>
 *   INSERT DonDatBan (không có cột maBan)
 *   for each Ban in dsBan:
 *       INSERT ChiTietDatBan(maDon, maBan)
 *       UPDATE Ban.maTinhTrang = DAT_TRUOC
 * </pre>
 */
public class DonDatBan_DAO {

    private final ChiTietDatBan_DAO ctdbDAO = new ChiTietDatBan_DAO();
    private final Ban_DAO banDAO = new Ban_DAO();

    // ── Lấy tất cả đơn (không kèm dsBan – dùng cho listing nhanh) ────────

    public List<DonDatBan> getAllDonDatBan() {
        List<DonDatBan> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        try {
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM DonDatBan")) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return ds;
    }

    /**
     * Lấy tất cả đơn kèm danh sách bàn bằng 1 JOIN query thay vì N+1.
     * Giảm từ (1 + N_đơn) queries xuống còn 1 query duy nhất.
     */
    public List<DonDatBan> getAllDonDatBanWithBan() {
        Map<String, DonDatBan> donMap = new LinkedHashMap<>();
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT "
                    + "d.maDon, d.thoiGianDat, d.thoiGianDen, d.thoiGianDuKienRoi, "
                    + "d.soLuongKhach, d.maKH, d.trangThai, d.maNV, d.ghiChu, "
                    + "b.maBan AS b_maBan, b.soBan AS b_soBan, b.sucChua AS b_sucChua, "
                    + "b.loaiBan AS b_loaiBan, b.maKV AS b_maKV, b.maTinhTrang AS b_maTinhTrang "
                    + "FROM DonDatBan d "
                    + "LEFT JOIN ChiTietDatBan ct ON d.maDon = ct.maDonDatBan "
                    + "LEFT JOIN Ban b ON ct.maBan = b.maBan "
                    + "ORDER BY d.maDon";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String maDon = rs.getString("maDon");
                    DonDatBan don = donMap.get(maDon);
                    if (don == null) {
                        don = mapRow(rs);
                        don.setDsBan(new ArrayList<>());
                        donMap.put(maDon, don);
                    }
                    String maBan = rs.getString("b_maBan");
                    if (maBan != null) {
                        Ban ban = new Ban();
                        ban.setMaBan(maBan);
                        ban.setSoBan(rs.getInt("b_soBan"));
                        ban.setSucChua(rs.getInt("b_sucChua"));
                        ban.setLoaiBan(rs.getString("b_loaiBan"));
                        KhuVuc kv = new KhuVuc();
                        kv.setMaKV(rs.getString("b_maKV"));
                        ban.setKhuVuc(kv);
                        ban.setTinhTrangBan(TrangThaiBan.fromString(rs.getString("b_maTinhTrang")));
                        don.getDsBan().add(ban);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return new ArrayList<>(donMap.values());
    }

    // ── Lấy theo mã đơn ──────────────────────────────────────────────────

    public DonDatBan getDonDatBanByMa(String ma) {
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement st = con.prepareStatement(
                    "SELECT * FROM DonDatBan WHERE maDon = ?")) {
                st.setString(1, ma);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        DonDatBan d = mapRow(rs);
                        d.setDsBan(ctdbDAO.getDsBanCuaDon(ma));
                        return d;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return null;
    }

    // ── Lấy đơn đang hoạt động theo maBan ────────────────────────────────

    /**
     * Tìm đơn chưa thanh toán (trangThai=0) mới nhất có chứa bàn này.
     * Thay thế cột maBan cũ bằng JOIN qua ChiTietDatBan.
     */
    public DonDatBan getDonDatBanByMaBan(String maBan) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "SELECT TOP 1 d.* FROM DonDatBan d "
                    + "JOIN ChiTietDatBan ct ON d.maDon = ct.maDonDatBan "
                    + "WHERE ct.maBan = ? AND d.trangThai = 0 "
                    + "ORDER BY d.thoiGianDat DESC";
            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, maBan);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        DonDatBan d = mapRow(rs);
                        d.setDsBan(ctdbDAO.getDsBanCuaDon(d.getMaDon()));
                        return d;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return null;
    }

    // ── Lấy đơn theo SĐT khách ───────────────────────────────────────────

    public DonDatBan getDonDatBanTheoSdtKhach(String sdt, java.util.Date thoiGianDen,
                                              KhachHang_DAO khDAO) {
        KhachHang kh = khDAO.getKhachHangBySdt(sdt);
        if (kh == null) return null;
        for (DonDatBan d : getAllDonDatBan()) {
            if (!d.isTrangThai()
                    && d.getKhachHang() != null
                    && d.getKhachHang().getMaKH().equals(kh.getMaKH())
                    && d.getThoiGianDen() != null
                    && d.getThoiGianDen().equals(thoiGianDen)) {
                return d;
            }
        }
        return null;
    }

    // ── Sinh mã đơn mới ───────────────────────────────────────────────────

    public String getNextMaDon() {
        Connection con = ConnectDB.getConnection();
        String ma = "DDB001";
        try {
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT MAX(maDon) FROM DonDatBan")) {
                if (rs.next() && rs.getString(1) != null) {
                    int num = Integer.parseInt(rs.getString(1).substring(3)) + 1;
                    ma = String.format("DDB%03d", num);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { ConnectDB.closeConnection(); }
        return ma;
    }

    // ── Thêm đơn + liên kết nhiều bàn ────────────────────────────────────

    /**
     * Thêm DonDatBan và tạo liên kết với tất cả bàn trong dsBan.
     * Toàn bộ chạy trong 1 transaction với UPDLOCK+HOLDLOCK để chống race condition
     * khi 2 nhân viên đặt cùng bàn cùng lúc (kể cả từ 2 máy khác nhau).
     *
     * @param ddb đơn đặt bàn, phải có dsBan không rỗng
     * @return true nếu thêm thành công; false nếu trùng lịch hoặc lỗi DB
     */
    public boolean addDonDatBan(DonDatBan ddb) {
        if (ddb.getDsBan() == null || ddb.getDsBan().isEmpty()) {
            System.err.println("[DonDatBan_DAO] dsBan rỗng – không thể thêm đơn!");
            return false;
        }

        if (ddb.getThoiGianDuKienRoi() == null) ddb.computeAndSetThoiGianDuKienRoi();

        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);

            // ── 1. Kiểm tra trùng lịch với UPDLOCK+HOLDLOCK ─────────────
            // Giữ lock cho đến khi commit → ngăn máy khác INSERT trùng bàn
            String conflictSql = "SELECT TOP 1 d.maDon "
                    + "FROM DonDatBan d WITH (UPDLOCK, HOLDLOCK) "
                    + "JOIN ChiTietDatBan ct WITH (UPDLOCK, HOLDLOCK) ON d.maDon = ct.maDonDatBan "
                    + "WHERE ct.maBan = ? AND d.trangThai = 0 "
                    + "AND d.thoiGianDen < ? AND d.thoiGianDuKienRoi > ?";
            Timestamp newDen  = new Timestamp(ddb.getThoiGianDen().getTime());
            Timestamp newRoi  = new Timestamp(ddb.getThoiGianDuKienRoi().getTime());
            for (Ban ban : ddb.getDsBan()) {
                try (PreparedStatement chk = con.prepareStatement(conflictSql)) {
                    chk.setString(1, ban.getMaBan());
                    chk.setTimestamp(2, newRoi);
                    chk.setTimestamp(3, newDen);
                    try (ResultSet rs = chk.executeQuery()) {
                        if (rs.next()) {
                            con.rollback();
                            System.err.println("[DonDatBan_DAO] Bàn " + ban.getMaBan()
                                    + " đã có đơn trùng lịch – hủy tạo đơn.");
                            return false;
                        }
                    }
                }
            }

            // ── 2. INSERT DonDatBan ──────────────────────────────────────
            String sql = "INSERT INTO DonDatBan "
                    + "(maDon, thoiGianDat, thoiGianDen, thoiGianDuKienRoi, soLuongKhach, "
                    + " maKH, trangThai, maNV, ghiChu) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, ddb.getMaDon());
                st.setTimestamp(2, new Timestamp(ddb.getThoiGianDat().getTime()));
                st.setTimestamp(3, newDen);
                st.setTimestamp(4, newRoi);
                st.setInt(5, ddb.getSoLuongKhach());
                if (ddb.getKhachHang() != null)
                    st.setString(6, ddb.getKhachHang().getMaKH());
                else
                    st.setNull(6, Types.VARCHAR);
                st.setBoolean(7, ddb.isTrangThai());
                if (ddb.getNhanVien() != null)
                    st.setString(8, ddb.getNhanVien().getMaNV());
                else
                    st.setNull(8, Types.VARCHAR);
                st.setString(9, ddb.getGhiChu() != null ? ddb.getGhiChu() : "");
                if (st.executeUpdate() <= 0) { con.rollback(); return false; }
            }

            // ── 3. INSERT ChiTietDatBan (inline, không qua sub-DAO) ─────
            // Dùng cùng connection để tránh sub-DAO closeConnection() giữa transaction
            String ctSql = "INSERT INTO ChiTietDatBan (maDonDatBan, maBan) VALUES (?, ?)";
            for (Ban ban : ddb.getDsBan()) {
                try (PreparedStatement st = con.prepareStatement(ctSql)) {
                    st.setString(1, ddb.getMaDon());
                    st.setString(2, ban.getMaBan());
                    st.executeUpdate();
                }
            }

            con.commit();

            // Log
            String maNV = ddb.getNhanVien() != null
                    ? SQLLogger.str(ddb.getNhanVien().getMaNV()) : "NULL";
            SQLLogger.log("INSERT INTO DonDatBan (...) VALUES ("
                    + SQLLogger.str(ddb.getMaDon()) + ", "
                    + SQLLogger.ts(ddb.getThoiGianDat()) + ", "
                    + SQLLogger.ts(ddb.getThoiGianDen()) + ", "
                    + SQLLogger.ts(ddb.getThoiGianDuKienRoi()) + ", "
                    + ddb.getSoLuongKhach() + ", "
                    + (ddb.getKhachHang() != null ? SQLLogger.str(ddb.getKhachHang().getMaKH()) : "NULL") + ", "
                    + SQLLogger.bit(ddb.isTrangThai()) + ", "
                    + maNV + ", "
                    + SQLLogger.str(ddb.getGhiChu()) + ");");

            return true;

        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ignored) {}
            ConnectDB.closeConnection();
        }
    }

    // ── Cập nhật đơn ─────────────────────────────────────────────────────

    public boolean updateDonDatBan(DonDatBan ddb) {
        Connection con = ConnectDB.getConnection();
        try {
            // Tính lại thoiGianDuKienRoi nếu giờ đến hoặc số khách thay đổi
            ddb.computeAndSetThoiGianDuKienRoi();

            String sql = "UPDATE DonDatBan SET "
                    + "thoiGianDat = ?, thoiGianDen = ?, thoiGianDuKienRoi = ?, soLuongKhach = ?, "
                    + "maKH = ?, trangThai = ?, maNV = ?, ghiChu = ? "
                    + "WHERE maDon = ?";
            try (PreparedStatement st = con.prepareStatement(sql)) {
                st.setTimestamp(1, new Timestamp(ddb.getThoiGianDat().getTime()));
                st.setTimestamp(2, new Timestamp(ddb.getThoiGianDen().getTime()));
                st.setTimestamp(3, new Timestamp(ddb.getThoiGianDuKienRoi().getTime()));
                st.setInt(4, ddb.getSoLuongKhach());
                if (ddb.getKhachHang() != null) st.setString(5, ddb.getKhachHang().getMaKH());
                else st.setNull(5, Types.VARCHAR);
                st.setBoolean(6, ddb.isTrangThai());
                if (ddb.getNhanVien() != null)
                    st.setString(7, ddb.getNhanVien().getMaNV());
                else
                    st.setNull(7, Types.VARCHAR);
                st.setString(8, ddb.getGhiChu() != null ? ddb.getGhiChu() : "");
                st.setString(9, ddb.getMaDon());
                int n = st.executeUpdate();
                if (n > 0) {
                    String maNV = ddb.getNhanVien() != null
                            ? SQLLogger.str(ddb.getNhanVien().getMaNV()) : "NULL";
                    SQLLogger.log("UPDATE DonDatBan SET "
                            + "thoiGianDat = " + SQLLogger.ts(ddb.getThoiGianDat()) + ", "
                            + "thoiGianDen = " + SQLLogger.ts(ddb.getThoiGianDen()) + ", "
                            + "thoiGianDuKienRoi = " + SQLLogger.ts(ddb.getThoiGianDuKienRoi()) + ", "
                            + "soLuongKhach = " + ddb.getSoLuongKhach() + ", "
                            + "maKH = " + (ddb.getKhachHang() != null ? SQLLogger.str(ddb.getKhachHang().getMaKH()) : "NULL") + ", "
                            + "trangThai = " + SQLLogger.bit(ddb.isTrangThai()) + ", "
                            + "maNV = " + maNV
                            + " WHERE maDon = " + SQLLogger.str(ddb.getMaDon()) + ";");
                }
                return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally { ConnectDB.closeConnection(); }
    }

    // ── Xóa đơn (kèm xóa ChiTietDatBan) ─────────────────────────────────

    public boolean deleteDonDatBan(String ma) {
        ctdbDAO.xoaTatCaBanCuaDon(ma);
        Connection con = ConnectDB.getConnection();
        try {
            // Gỡ FK_HD_Don: bỏ liên kết HoaDon trước khi xóa DonDatBan
            try (PreparedStatement unlinkHD = con.prepareStatement(
                    "UPDATE HoaDon SET maDon = NULL WHERE maDon = ?")) {
                unlinkHD.setString(1, ma);
                unlinkHD.executeUpdate();
            }

            try (PreparedStatement st = con.prepareStatement(
                    "DELETE FROM DonDatBan WHERE maDon = ?")) {
                st.setString(1, ma);
                int n = st.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "DELETE FROM DonDatBan WHERE maDon = " + SQLLogger.str(ma) + ";");
                return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally { ConnectDB.closeConnection(); }
    }

    public boolean updateTrangThai(String maDon, boolean trangThai) {
        Connection con = ConnectDB.getConnection();
        try {
            try (PreparedStatement st = con.prepareStatement(
                    "UPDATE DonDatBan SET trangThai = ? WHERE maDon = ?")) {
                st.setBoolean(1, trangThai);
                st.setString(2, maDon);
                int n = st.executeUpdate();
                if (n > 0) SQLLogger.log(
                        "UPDATE DonDatBan SET trangThai = " + (trangThai ? 1 : 0)
                        + " WHERE maDon = " + SQLLogger.str(maDon) + ";");
                return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally { ConnectDB.closeConnection(); }
    }

    // ── mapRow ────────────────────────────────────────────────────────────

    /**
     * Map một hàng ResultSet thành DonDatBan (không kèm dsBan).
     * Gọi getDsBanCuaDon() riêng nếu cần.
     */
    private DonDatBan mapRow(ResultSet rs) throws SQLException {
        DonDatBan d = new DonDatBan();
        d.setMaDon(rs.getString("maDon"));
        d.setThoiGianDat(rs.getTimestamp("thoiGianDat"));
        d.setThoiGianDen(rs.getTimestamp("thoiGianDen"));
        try { d.setThoiGianDuKienRoi(rs.getTimestamp("thoiGianDuKienRoi")); } catch (Exception ignored) {}
        d.setSoLuongKhach(rs.getInt("soLuongKhach"));
        d.setTrangThai(rs.getBoolean("trangThai"));
        try { d.setGhiChu(rs.getString("ghiChu")); } catch (Exception ignored) {}

        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        d.setKhachHang(kh);

        try {
            String maNV = rs.getString("maNV");
            if (maNV != null) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(maNV);
                d.setNhanVien(nv);
            }
        } catch (Exception ignored) {}

        // dsBan không map ở đây – gọi ctdbDAO.getDsBanCuaDon() khi cần
        return d;
    }
}
