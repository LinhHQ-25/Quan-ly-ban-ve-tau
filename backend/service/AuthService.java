package service;

import connect_DB.Connect_DB;
import java.sql.*;
import java.time.LocalDateTime;

public class AuthService {

    public enum LoginResult {
        SUCCESS_QUAN_LY,
        SUCCESS_BAN_VE,
        WRONG_PASSWORD,
        ACCOUNT_NOT_FOUND,
        ACCOUNT_LOCKED,
        DB_ERROR
    }

    // Lưu thông tin phiên đăng nhập hiện tại
    private static String currentMaNV;
    private static String currentHoTen;

    public static LoginResult login(String maNV, String matKhau) {
        String sql = """
            SELECT tk.matKhau, nv.loaiNV, nv.hoTenNV
            FROM TaiKhoan tk
            JOIN NhanVien nv ON tk.maNV = nv.maNV
            WHERE tk.maNV = ?
            """;

        try (Connection conn = Connect_DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return LoginResult.ACCOUNT_NOT_FOUND;
            if (!rs.getString("matKhau").equals(matKhau)) return LoginResult.WRONG_PASSWORD;

            // Lưu thông tin phiên
            currentMaNV  = maNV;
            currentHoTen = rs.getString("hoTenNV");
            String loaiNV = rs.getString("loaiNV");

            // Ghi ngayDangNhap
            ghiThoiGian(maNV, "ngayDangNhap");

            return "NHAN_VIEN_QUAN_LY".equals(loaiNV)
                    ? LoginResult.SUCCESS_QUAN_LY
                    : LoginResult.SUCCESS_BAN_VE;

        } catch (SQLException e) {
            e.printStackTrace();
            return LoginResult.DB_ERROR;
        }
    }

    public static void logout() {
        if (currentMaNV == null) return;
        ghiThoiGian(currentMaNV, "ngayDangXuat");
        currentMaNV  = null;
        currentHoTen = null;
    }

    private static void ghiThoiGian(String maNV, String tenCot) {
        String sql = "UPDATE TaiKhoan SET " + tenCot + " = ? WHERE maNV = ?";
        try (Connection conn = Connect_DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, maNV);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Getters cho các màn hình khác dùng
    public static String getCurrentMaNV()  { return currentMaNV; }
    public static String getCurrentHoTen() { return currentHoTen; }
}