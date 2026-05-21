package connect_DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

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
                
                // Tự động kiểm tra và nâng cấp cấu trúc cơ sở dữ liệu
                checkAndUpdateDatabaseSchema(test);
            }
        } catch (SQLException e) {
            System.err.println("Không thể kết nối SQL Server:");
            e.printStackTrace();
        }
    }

    /**
     * Tự động kiểm tra và thêm cột 'trangThai' vào bảng 'Ghe' nếu chưa có
     */
    private void checkAndUpdateDatabaseSchema(Connection conn) {
        try {
            boolean columnExists = false;
            String checkQuery = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Ghe' AND COLUMN_NAME = 'trangThai'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkQuery)) {
                if (rs.next()) {
                    columnExists = true;
                }
            }
            
            if (!columnExists) {
                System.out.println("[Database Migration] Cột 'trangThai' chưa tồn tại trong bảng 'Ghe'. Bắt đầu tự động cập nhật cấu trúc...");
                try (Statement stmt = conn.createStatement()) {
                    String alterQuery = "ALTER TABLE Ghe ADD trangThai nvarchar(50) NULL;";
                    stmt.executeUpdate(alterQuery);
                    System.out.println("[Database Migration] Thêm cột 'trangThai' thành công.");
                    
                    // Thiết lập trạng thái các ghế hiện tại thành 'Hoạt động'
                    String updateQuery = "UPDATE Ghe SET trangThai = N'Hoạt động' WHERE trangThai IS NULL;";
                    stmt.executeUpdate(updateQuery);
                    System.out.println("[Database Migration] Thiết lập trạng thái các ghế hiện tại thành 'Hoạt động' thành công.");
                }
            }
            
            // Dọn dẹp trạng thái các ghế bị lỗi phông hoặc null
            try (Statement stmt = conn.createStatement()) {
                String cleanGheQuery = "UPDATE Ghe SET trangThai = N'Hoạt động' WHERE trangThai IS NULL OR trangThai NOT IN (N'Hoạt động', N'Bảo trì');";
                int rowsUpdated = stmt.executeUpdate(cleanGheQuery);
                if (rowsUpdated > 0) {
                    // statuses cleaned silently
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Migration] Lỗi khi tự động kiểm tra và cập nhật cấu trúc bảng Ghe:");
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