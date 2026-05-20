package connectDB;

import dao.ChiTietDatBan_DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quản lý kết nối JDBC duy nhất và chạy auto-migration khi khởi động.
 *
 * <p>Migration được thiết kế idempotent (IF NOT EXISTS) – an toàn khi chạy lại nhiều lần.
 *
 * <p>Thứ tự migration:
 * <ol>
 *   <li>tienCoc vào HoaDon</li>
 *   <li>khungGio vào DonDatBan</li>
 *   <li>loaiBan vào Ban</li>
 *   <li>ghiChu vào DonDatBan</li>
 *   <li>ChiTietDatBan</li>
 *   <li>trangThaiThanhToan (VARCHAR) vào HoaDon</li>
 *   <li>hinhThucThanhToan vào HoaDon</li>
 *   <li>Migrate dữ liệu cũ trangThai bit → trangThaiThanhToan varchar</li>
 * </ol>
 */
public class ConnectDB {
    private static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() { return instance; }

    public void connect() throws SQLException {
        if (con != null && !con.isClosed()) return;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=GoldenPearlDB;"
                    + "encrypt=false;trustServerCertificate=true;loginTimeout=30;";
            con = DriverManager.getConnection(url, "sa", "sapassword");
            System.out.println("✅ Kết nối Database thành công!");
            runMigrations();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy thư viện JDBC Driver!");
            e.printStackTrace();
        }
    }

    private void runMigrations() {
        String[] migrations = {

                // ── tienCoc ─────────────────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='tienCoc') "
                        + "ALTER TABLE HoaDon ADD tienCoc FLOAT DEFAULT 0",

                // ── khungGio ─────────────────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='khungGio') "
                        + "ALTER TABLE DonDatBan ADD khungGio NVARCHAR(50)",

                // ── loaiBan ──────────────────────────────────────────────────────
                "UPDATE Ban SET loaiBan = N'Thường' "
                        + "WHERE maBan IN ('B001','B002','B003','B004','B005','B006','B007',"
                        +                 "'B008','B009','B010','B011','B012','B013','B014','B015')",
                "UPDATE Ban SET loaiBan = N'VIP' "
                        + "WHERE maBan IN ('B016','B017','B018','B019','B020','B021','B022')",

                // ── ghiChu DonDatBan ─────────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='ghiChu') "
                        + "ALTER TABLE DonDatBan ADD ghiChu NVARCHAR(500) NULL",

                // ── ChiTietDatBan ────────────────────────────────────────────────
                ChiTietDatBan_DAO.getMigrationSQL(),

                // ── Migrate maBan cũ → ChiTietDatBan ────────────────────────────
                "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='maBan') "
                        + "INSERT INTO ChiTietDatBan (maDonDatBan, maBan) "
                        + "SELECT maDon, maBan FROM DonDatBan "
                        + "WHERE maBan IS NOT NULL "
                        + "AND NOT EXISTS ("
                        +   "SELECT 1 FROM ChiTietDatBan ct "
                        +   "WHERE ct.maDonDatBan = DonDatBan.maDon "
                        +   "AND ct.maBan = DonDatBan.maBan"
                        + ")",

                // ── trangThaiThanhToan ───────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='trangThaiThanhToan') "
                        + "ALTER TABLE HoaDon ADD trangThaiThanhToan NVARCHAR(30) "
                        + "NOT NULL DEFAULT N'Chưa thanh toán'",

                // ── hinhThucThanhToan ────────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='hinhThucThanhToan') "
                        + "ALTER TABLE HoaDon ADD hinhThucThanhToan NVARCHAR(30) NULL",

                // ── Migrate trangThai bit → trangThaiThanhToan ───────────────────
                "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='trangThai') "
                        + "UPDATE HoaDon SET trangThaiThanhToan = "
                        + "  CASE WHEN trangThai = 1 THEN N'Đã thanh toán' "
                        + "       ELSE N'Chưa thanh toán' END "
                        + "WHERE trangThaiThanhToan = N'Chưa thanh toán' AND trangThai = 1"
        };

        try (Statement st = con.createStatement()) {
            for (String sql : migrations) st.execute(sql);
            System.out.println("✅ Migrations hoàn thành.");
        } catch (SQLException e) {
            System.err.println("⚠️ Migration warning: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (con != null) {
            try { con.close(); con = null; }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) instance.connect();
        } catch (SQLException e) { e.printStackTrace(); }
        return con;
    }
}