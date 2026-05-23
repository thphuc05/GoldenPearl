package connectDB;

import dao.ChiTietDatBan_DAO;
import dao.PhanCongCa_DAO;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Quản lý kết nối JDBC theo từng thread (ThreadLocal).
 * Mỗi thread (EDT, SwingWorker) có Connection riêng — tránh race condition.
 *
 * Migration chỉ chạy 1 lần sau khi kết nối đầu tiên.
 */
public class ConnectDB {

    private static final ThreadLocal<Connection> threadLocalCon = new ThreadLocal<>();
    private static volatile boolean migrationsDone = false;
    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() { return instance; }

    // ── Kết nối thủ công (Login.java gọi khi khởi động) ─────────────────────
    public void connect() throws SQLException {
        Connection con = getConnection(); // sẽ tạo connection mới nếu chưa có
        if (!migrationsDone) {
            synchronized (ConnectDB.class) {
                if (!migrationsDone) {
                    runMigrations(con);
                    migrationsDone = true;
                }
            }
        }
    }

    // ── Lấy connection của thread hiện tại (tạo mới nếu chưa có / đã đóng) ──
    public static Connection getConnection() {
        try {
            Connection con = threadLocalCon.get();
            if (con == null || con.isClosed()) {
                con = openConnection();
                threadLocalCon.set(con);
                // Chạy migration lần đầu (double-checked locking)
                if (!migrationsDone) {
                    synchronized (ConnectDB.class) {
                        if (!migrationsDone) {
                            runMigrations(con);
                            migrationsDone = true;
                        }
                    }
                }
            }
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Đóng connection của thread hiện tại (gọi sau transaction hoặc khi done) ─
    public static void closeConnection() {
        Connection con = threadLocalCon.get();
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) {}
            threadLocalCon.remove();
        }
    }

    public void disconnect() {
        closeConnection();
    }

    // ── private ──────────────────────────────────────────────────────────────

    private static Connection openConnection() throws SQLException {
        try {
            Properties props = loadProperties();
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection con = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password"));
            System.out.println("✅ Kết nối Database thành công! (thread: " + Thread.currentThread().getName() + ")");
            return con;
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy thư viện JDBC Driver!");
            throw new SQLException("JDBC Driver not found", e);
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = ConnectDB.class.getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("⚠️ Không tìm thấy db.properties, dùng giá trị mặc định.");
                props.setProperty("db.url", "jdbc:sqlserver://localhost:1433;"
                        + "databaseName=GoldenPearlDB_Test;"
                        + "encrypt=false;trustServerCertificate=true;loginTimeout=30;");
                props.setProperty("db.username", "sa");
                props.setProperty("db.password", "sapassword");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    private static void runMigrations(Connection con) {
        String[] migrations = {

                // ── ChiTietDatBan (tạo nếu chưa có) ────────────────────────────
                ChiTietDatBan_DAO.getMigrationSQL(),

                // ── loaiBan ──────────────────────────────────────────────────────
                "UPDATE Ban SET loaiBan = N'Thường' "
                        + "WHERE loaiBan IS NULL "
                        + "AND maBan IN ('B001','B002','B003','B004','B005','B006','B007',"
                        +              "'B008','B009','B010','B011','B012','B013','B014','B015')",
                "UPDATE Ban SET loaiBan = N'VIP' "
                        + "WHERE loaiBan IS NULL "
                        + "AND maBan IN ('B016','B017','B018','B019','B020','B021','B022')",

                // ── ghiChu DonDatBan ─────────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='ghiChu') "
                        + "ALTER TABLE DonDatBan ADD ghiChu NVARCHAR(500) NULL",

                // ── thoiGianDuKienRoi DonDatBan ──────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='thoiGianDuKienRoi') "
                        + "ALTER TABLE DonDatBan ADD thoiGianDuKienRoi DATETIME NULL",

                // ── Migrate maBan cũ → ChiTietDatBan ────────────────────────────
                "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='maBan') "
                        + "EXEC('INSERT INTO ChiTietDatBan (maDonDatBan, maBan) "
                        +      "SELECT maDon, maBan FROM DonDatBan "
                        +      "WHERE maBan IS NOT NULL "
                        +      "AND NOT EXISTS ("
                        +        "SELECT 1 FROM ChiTietDatBan ct "
                        +        "WHERE ct.maDonDatBan = DonDatBan.maDon "
                        +        "AND ct.maBan = DonDatBan.maBan"
                        +      ")')",

                // ── tienCoc HoaDon ───────────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='tienCoc') "
                        + "ALTER TABLE HoaDon ADD tienCoc FLOAT DEFAULT 0",

                // ── trangThaiThanhToan HoaDon ────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='trangThaiThanhToan') "
                        + "ALTER TABLE HoaDon ADD trangThaiThanhToan NVARCHAR(30) "
                        + "NOT NULL DEFAULT N'Chưa thanh toán'",

                // ── hinhThucThanhToan HoaDon ─────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='hinhThucThanhToan') "
                        + "ALTER TABLE HoaDon ADD hinhThucThanhToan NVARCHAR(30) NULL",

                // ── maCa HoaDon ──────────────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='maCa') "
                        + "ALTER TABLE HoaDon ADD maCa VARCHAR(10) NULL",

                // ── PhanCongCa table ─────────────────────────────────────────────
                PhanCongCa_DAO.getMigrationSQL(),

                // ── thoiGianRoiThucTe HoaDon ─────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='thoiGianRoiThucTe') "
                        + "ALTER TABLE HoaDon ADD thoiGianRoiThucTe DATETIME NULL",

                // ── Migrate trangThai bit → trangThaiThanhToan ───────────────────
                "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='trangThai') "
                        + "EXEC('UPDATE HoaDon SET trangThaiThanhToan = "
                        +      "CASE WHEN trangThai = 1 THEN N''Đã thanh toán'' "
                        +           "ELSE N''Chưa thanh toán'' END "
                        +      "WHERE trangThaiThanhToan = N''Chưa thanh toán'' AND trangThai = 1')",

                // ── email NhanVien ────────────────────────────────────────────
                "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME='NhanVien' AND COLUMN_NAME='email') "
                        + "ALTER TABLE NhanVien ADD email NVARCHAR(100) NULL"
        };

        try (Statement st = con.createStatement()) {
            for (String sql : migrations) {
                try {
                    st.execute(sql);
                } catch (SQLException e) {
                    System.err.println("⚠️ Migration warning: " + e.getMessage());
                }
            }
            System.out.println("✅ Migrations hoàn thành.");
        } catch (SQLException e) {
            System.err.println("❌ Không thể tạo Statement: " + e.getMessage());
        }
    }
}
