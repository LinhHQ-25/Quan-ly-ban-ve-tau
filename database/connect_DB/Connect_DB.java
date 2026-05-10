package connect_DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class quản lý kết nối đến SQL Server
 * Sử dụng Singleton Pattern
 */
public class Connect_DB {
    private static Connection con = null;
    private static final Connect_DB instance = new Connect_DB();
    
    // Thông tin kết nối
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyBanVeTau;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789";

    // Constructor private để implement Singleton
    private Connect_DB() {
        // Load SQL Server JDBC Driver
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy instance duy nhất của ConnectDB
     * @return ConnectDB instance
     */
    public static Connect_DB getInstance() {
        return instance;
    }

    /**
     * Tạo kết nối mới đến database
     */
    public void connect() {
        try {
            // Nếu chưa có hoặc đã đóng thì mở lại
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Kết nối SQL Server thành công!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Không thể kết nối SQL Server:");
            e.printStackTrace();
        }
    }

    /**
     * Lấy Connection hiện tại (tự động tạo mới nếu chưa có hoặc đã đóng)
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            // Kiểm tra và tạo mới nếu cần
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    /**
     * Đóng kết nối đến database
     */
    public void disconnect() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                con = null; // Set về null sau khi đóng
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra trạng thái kết nối
     * @return true nếu đang kết nối, false nếu không
     */
    public boolean isConnected() {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Test kết nối
     */
    public static void main(String[] args) {
        Connect_DB connectDB = Connect_DB.getInstance();
        
        // Test lấy connection
        Connection connection = connectDB.getConnection();

        // Đóng kết nối
        connectDB.disconnect();
    }
}