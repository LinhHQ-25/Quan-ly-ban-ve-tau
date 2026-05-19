package connect_DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class quản lý kết nối đến SQL Server
 */
public class Connect_DB {
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

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo kết nối (Giữ lại để tương thích với code cũ)
     */
    public void connect() {
        try (Connection test = getConnection()) {
            if (test != null && !test.isClosed()) {
                System.out.println("Kết nối SQL Server thành công!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Không thể kết nối SQL Server:");
            e.printStackTrace();
        }
    }

    /**
     * Đóng kết nối (Giữ lại để tương thích với code cũ)
     */
    public void disconnect() {
        // Vì giờ chúng ta dùng try-with-resources ở các file GUI,
        // connection sẽ tự động đóng. Hàm này không cần làm gì cả.
    }

    /**
     * Kiểm tra trạng thái kết nối (Giữ lại để tương thích với code cũ)
     */
    public boolean isConnected() {
        try (Connection test = getConnection()) {
            return test != null && !test.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

}