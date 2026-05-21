package service;

import java.sql.*;
import java.util.Random;
import connect_DB.Connect_DB;

public class PasswordResetService {
    private static String pendingOtp;
    private static long   otpExpiry;
    private static String pendingMaNV;

    /** Bước 1: Kiểm tra xem mã nhân viên có tồn tại trong DB không */
    public static boolean checkMaNV(String maNV) {
        String sql = "SELECT 1 FROM NhanVien WHERE maNV = ?";
        try (Connection conn = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Có data trả về true, không có trả về false
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Bước 2: Đối chiếu email có khớp với mã NV không, khớp thì gửi OTP */
    public static boolean verifyAndSendOtp(String maNV, String inputEmail) {
        String sql = "SELECT email FROM NhanVien WHERE maNV = ?"; 
        
        // Thêm check kết nối
        if (Connect_DB.getInstance().getConnection() == null) {
            return false;
        }

        try (Connection conn = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            
            if (!rs.next()) {
                return false; 
            }

            String dbEmail = rs.getString("email");
            
            if (dbEmail == null || !dbEmail.equalsIgnoreCase(inputEmail)) {
                return false; 
            }

            String otp = String.format("%06d", new Random().nextInt(999999));
            pendingOtp   = otp;
            pendingMaNV  = maNV;
            otpExpiry    = System.currentTimeMillis() + 180_000;
            
            EmailService.sendOtp(dbEmail, otp);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Bước 3: Xác thực OTP người dùng nhập */
    public static boolean verifyOtp(String inputOtp) {
        return pendingOtp != null
            && pendingOtp.equals(inputOtp)
            && System.currentTimeMillis() < otpExpiry;
    }

    /** Bước 4: Cập nhật mật khẩu mới */
    public static boolean resetPassword(String newPassword) {
        // 1. Kiểm tra lại tên cột trong bảng NhanVien của bác (phải chính xác 100%)
    	String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maNV = ?";
        
        try (Connection conn = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, pendingMaNV); // Cần đảm bảo biến này vẫn tồn tại
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void clearOtp() {
        pendingOtp = null; // Xóa mã cũ để không ai dùng được nữa
    }
}