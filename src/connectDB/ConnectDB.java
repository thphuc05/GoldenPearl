package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectDB {
    private static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() throws SQLException {
        if (con != null && !con.isClosed()) {
            return;
        }
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Quay lại chuỗi kết nối cũ, chỉ đổi mật khẩu thành sapassword
            String url = "jdbc:sqlserver://localhost:1433;databaseName=GoldenPearlDB;encrypt=false;trustServerCertificate=true;loginTimeout=30;";
            String user = "sa";
            String password = "sapassword";
            con = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Kết nối Database thành công!");
            runMigrations();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy thư viện JDBC Driver!");
            e.printStackTrace();
        }
    }

    private void runMigrations() {
        String[] migrations = {
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='HoaDon' AND COLUMN_NAME='tienCoc') " +
                "ALTER TABLE HoaDon ADD tienCoc FLOAT DEFAULT 0",
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='khungGio') " +
                "ALTER TABLE DonDatBan ADD khungGio NVARCHAR(50)",
            "UPDATE Ban SET loaiBan = N'Thường' WHERE maBan IN ('B001','B002','B003','B004','B005','B006','B007','B008','B009','B010','B011','B012','B013','B014','B015')",
            "UPDATE Ban SET loaiBan = N'VIP' WHERE maBan IN ('B016','B017','B018','B019','B020','B021','B022')",
            "IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='DonDatBan' AND COLUMN_NAME='ghiChu') ALTER TABLE DonDatBan ADD ghiChu NVARCHAR(500) NULL"
        };
        try (Statement st = con.createStatement()) {
            for (String sql : migrations) st.execute(sql);
        } catch (SQLException e) {
            System.err.println("⚠️ Migration warning: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                instance.connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
}
