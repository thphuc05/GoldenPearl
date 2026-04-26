package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
            String url = "jdbc:sqlserver://localhost:1433;databaseName=GoldenPearlDB;encrypt=true;trustServerCertificate=true;loginTimeout=30;";
            String user = "sa";
            String password = "sapassword";
            con = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Kết nối Database thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy thư viện JDBC Driver!");
            e.printStackTrace();
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
