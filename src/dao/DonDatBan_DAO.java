package dao;

import connectDB.ConnectDB;
import entity.Ban;
import entity.DonDatBan;
import entity.KhachHang;
import entity.NhanVien;
import util.SQLLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM DonDatBan");
            while (rs.next()) ds.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /**
     * Lấy tất cả đơn kèm danh sách bàn đầy đủ.
     * Dùng khi cần dsBan (vd: tô màu sơ đồ bàn).
     */
    public List<DonDatBan> getAllDonDatBanWithBan() {
        List<DonDatBan> ds = getAllDonDatBan();
        for (DonDatBan d : ds) {
            d.setDsBan(ctdbDAO.getDsBanCuaDon(d.getMaDon()));
        }
        return ds;
    }

    // ── Lấy theo mã đơn ──────────────────────────────────────────────────
    public DonDatBan getDonDatBanByMa(String ma) {
        Connection con = ConnectDB.getConnection();
        try {
            PreparedStatement st = con.prepareStatement(
                    "SELECT * FROM DonDatBan WHERE maDon = ?");
            st.setString(1, ma);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                DonDatBan d = mapRow(rs);
                d.setDsBan(ctdbDAO.getDsBanCuaDon(ma));
                return d;
            }
        } catch (SQLException e) { e.printStackTrace(); }
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
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, maBan);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                DonDatBan d = mapRow(rs);
                d.setDsBan(ctdbDAO.getDsBanCuaDon(d.getMaDon()));
                return d;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Lấy đơn theo SĐT khách ───────────────────────────────────────────

    public DonDatBan getDonDatBanTheoSdtKhach(String sdt, java.util.Date thoiGianDen,
                                              String khungGio, KhachHang_DAO khDAO) {
        KhachHang kh = khDAO.getKhachHangBySdt(sdt);
        if (kh == null) return null;
        for (DonDatBan d : getAllDonDatBan()) {
            if (!d.isTrangThai()
                    && d.getKhungGio() != null && d.getKhungGio().equals(khungGio)
                    && d.getKhachHang() != null
                    && d.getKhachHang().getMaKH().equals(kh.getMaKH())) {
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
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT MAX(maDon) FROM DonDatBan");
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(3)) + 1;
                ma = String.format("DDB%03d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ma;
    }

    // ── Thêm đơn + liên kết nhiều bàn ────────────────────────────────────

    /**
     * Thêm DonDatBan và tạo liên kết với tất cả bàn trong dsBan.
     * <ol>
     *   <li>INSERT DonDatBan (không có cột maBan)</li>
     *   <li>INSERT ChiTietDatBan cho mỗi bàn</li>
     *   <li>UPDATE trạng thái mỗi bàn thành DaDuocDat (nếu đặt ngày hôm nay)</li>
     * </ol>
     *
     * @param ddb đơn đặt bàn, phải có dsBan không rỗng
     * @return true nếu thêm thành công
     */
    public boolean addDonDatBan(DonDatBan ddb) {
        if (ddb.getDsBan() == null || ddb.getDsBan().isEmpty()) {
            System.err.println("[DonDatBan_DAO] dsBan rỗng – không thể thêm đơn!");
            return false;
        }

        Connection con = ConnectDB.getConnection();
        try {
            // ── 1. INSERT DonDatBan ──────────────────────────────────────
            String sql = "INSERT INTO DonDatBan "
                    + "(maDon, thoiGianDat, thoiGianDen, soLuongKhach, "
                    + " maKH, trangThai, maNV, khungGio, ghiChu) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, ddb.getMaDon());
            st.setTimestamp(2, new Timestamp(ddb.getThoiGianDat().getTime()));
            st.setTimestamp(3, new Timestamp(ddb.getThoiGianDen().getTime()));
            st.setInt(4, ddb.getSoLuongKhach());
            st.setString(5, ddb.getKhachHang().getMaKH());
            st.setBoolean(6, ddb.isTrangThai());
            if (ddb.getNhanVien() != null)
                st.setString(7, ddb.getNhanVien().getMaNV());
            else
                st.setNull(7, Types.VARCHAR);
            st.setString(8, ddb.getKhungGio());
            st.setString(9, ddb.getGhiChu() != null ? ddb.getGhiChu() : "");
            int n = st.executeUpdate();
            if (n <= 0) return false;

            // Log SQL
            String maNV = ddb.getNhanVien() != null
                    ? SQLLogger.str(ddb.getNhanVien().getMaNV()) : "NULL";
            SQLLogger.log("INSERT INTO DonDatBan "
                    + "(maDon, thoiGianDat, thoiGianDen, soLuongKhach, maKH, trangThai, maNV, khungGio, ghiChu) VALUES ("
                    + SQLLogger.str(ddb.getMaDon()) + ", "
                    + SQLLogger.ts(ddb.getThoiGianDat()) + ", "
                    + SQLLogger.ts(ddb.getThoiGianDen()) + ", "
                    + ddb.getSoLuongKhach() + ", "
                    + SQLLogger.str(ddb.getKhachHang().getMaKH()) + ", "
                    + SQLLogger.bit(ddb.isTrangThai()) + ", "
                    + maNV + ", "
                    + SQLLogger.str(ddb.getKhungGio()) + ", "
                    + SQLLogger.str(ddb.getGhiChu()) + ");");

            // ── 2. INSERT ChiTietDatBan cho mỗi bàn ─────────────────────
            for (Ban ban : ddb.getDsBan()) {
                ctdbDAO.themBanVaoDon(ddb.getMaDon(), ban.getMaBan());
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Cập nhật đơn ─────────────────────────────────────────────────────

    public boolean updateDonDatBan(DonDatBan ddb) {
        Connection con = ConnectDB.getConnection();
        try {
            String sql = "UPDATE DonDatBan SET "
                    + "thoiGianDat = ?, thoiGianDen = ?, soLuongKhach = ?, "
                    + "maKH = ?, trangThai = ?, maNV = ?, ghiChu = ? "
                    + "WHERE maDon = ?";
            PreparedStatement st = con.prepareStatement(sql);
            st.setTimestamp(1, new Timestamp(ddb.getThoiGianDat().getTime()));
            st.setTimestamp(2, new Timestamp(ddb.getThoiGianDen().getTime()));
            st.setInt(3, ddb.getSoLuongKhach());
            st.setString(4, ddb.getKhachHang().getMaKH());
            st.setBoolean(5, ddb.isTrangThai());
            if (ddb.getNhanVien() != null)
                st.setString(6, ddb.getNhanVien().getMaNV());
            else
                st.setNull(6, Types.VARCHAR);
            st.setString(7, ddb.getGhiChu() != null ? ddb.getGhiChu() : "");
            st.setString(8, ddb.getMaDon());
            int n = st.executeUpdate();
            if (n > 0) {
                String maNV = ddb.getNhanVien() != null
                        ? SQLLogger.str(ddb.getNhanVien().getMaNV()) : "NULL";
                SQLLogger.log("UPDATE DonDatBan SET "
                        + "thoiGianDat = " + SQLLogger.ts(ddb.getThoiGianDat()) + ", "
                        + "thoiGianDen = " + SQLLogger.ts(ddb.getThoiGianDen()) + ", "
                        + "soLuongKhach = " + ddb.getSoLuongKhach() + ", "
                        + "maKH = " + SQLLogger.str(ddb.getKhachHang().getMaKH()) + ", "
                        + "trangThai = " + SQLLogger.bit(ddb.isTrangThai()) + ", "
                        + "maNV = " + maNV
                        + " WHERE maDon = " + SQLLogger.str(ddb.getMaDon()) + ";");
            }
            return n > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Xóa đơn (kèm xóa ChiTietDatBan) ─────────────────────────────────

    public boolean deleteDonDatBan(String ma) {
        // Xóa liên kết bàn trước (FK constraint)
        ctdbDAO.xoaTatCaBanCuaDon(ma);
        Connection con = ConnectDB.getConnection();
        try {
            PreparedStatement st = con.prepareStatement(
                    "DELETE FROM DonDatBan WHERE maDon = ?");
            st.setString(1, ma);
            int n = st.executeUpdate();
            if (n > 0) SQLLogger.log(
                    "DELETE FROM DonDatBan WHERE maDon = " + SQLLogger.str(ma) + ";");
            return n > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        d.setSoLuongKhach(rs.getInt("soLuongKhach"));
        d.setTrangThai(rs.getBoolean("trangThai"));
        try { d.setKhungGio(rs.getString("khungGio")); } catch (Exception ignored) {}
        try { d.setGhiChu(rs.getString("ghiChu"));     } catch (Exception ignored) {}

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