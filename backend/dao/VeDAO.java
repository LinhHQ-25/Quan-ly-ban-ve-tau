package dao;

import connect_DB.Connect_DB;
import entity.Ve;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VeDAO {

    private static String getHourCondition(String ca) {
        return ca.equalsIgnoreCase("Sáng")
                ? " AND CAST(v.ngayMua AS TIME) BETWEEN '00:00:00' AND '11:59:59'"
                : " AND CAST(v.ngayMua AS TIME) BETWEEN '12:00:00' AND '23:59:59'";
    }

    public static long getTongDoanhThuTheoCa(java.time.LocalDate ngay, String ca, String maNV) throws SQLException {
        String sql = "SELECT ISNULL(SUM(v.giaVe), 0) FROM Ve v " +
                "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                "WHERE CAST(v.ngayMua AS DATE) = ? " +
                "AND hd.maNV = ? " +
                "AND v.trangThaiVe = N'Đã thanh toán'" + getHourCondition(ca);
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    public static int getSoLuongVeTheoCa(java.time.LocalDate ngay, String ca, String maNV, String trangThai) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v " +
                "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                "WHERE CAST(v.ngayMua AS DATE) = ? AND hd.maNV = ? AND v.trangThaiVe = ?" + getHourCondition(ca);
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            ps.setString(3, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    // Thêm method mới này vào VeDAO, đặt ngay sau getSoLuongVeTheoCa
    public static int getSoVeHuyTheoCa(java.time.LocalDate ngay, String ca, String maNV) throws SQLException {
        // Đếm cả 2 trạng thái: 'Đã hủy' (hết hạn tự động) và 'DA_HUY' (trả vé thủ công)
        String sql = "SELECT COUNT(*) FROM Ve v " +
                "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                "WHERE CAST(v.ngayMua AS DATE) = ? AND hd.maNV = ? " +
                "AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY')" + getHourCondition(ca);
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static int[] getSoGheTheoLoaiTheoCa(java.time.LocalDate ngay, String ca, String maNV) throws SQLException {
        int[] result = {0, 0, 0};
        String sql = "SELECT g.loaiGhe, COUNT(*) as sl FROM Ve v " +
                "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                "JOIN Ghe g ON v.maGhe = g.maGhe " +
                "WHERE CAST(v.ngayMua AS DATE) = ? AND hd.maNV = ? " +
                "AND v.trangThaiVe = N'Đã thanh toán'" + getHourCondition(ca) +
                " GROUP BY g.loaiGhe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai = rs.getString("loaiGhe");
                    if ("GHE_CUNG".equalsIgnoreCase(loai) || "Ghế cứng".equalsIgnoreCase(loai)) result[0] = rs.getInt("sl");
                    else if ("GIUONG_NAM".equalsIgnoreCase(loai) || "Giường nằm".equalsIgnoreCase(loai)) result[1] = rs.getInt("sl");
                    else if ("GHE_MEM".equalsIgnoreCase(loai) || "Ghế mềm".equalsIgnoreCase(loai)) result[2] = rs.getInt("sl");
                }
            }
        }
        return result;
    }

    // ── MỚI: đếm vé hủy hôm nay theo nhân viên, không lọc ca ──
    public static int getSoVeHuyHomNay(String maNV) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v " +
                "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                "WHERE CAST(v.ngayMua AS DATE) = CAST(GETDATE() AS DATE) AND hd.maNV = ? " +
                "AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY')";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── MỚI: đếm vé theo loại ghế hôm nay, không lọc ca ──
    public static int[] getSoGheTheoLoaiHomNay(String maNV) throws SQLException {
        int[] result = {0, 0, 0};
        String sql = "SELECT g.loaiGhe, COUNT(*) as sl FROM Ve v " +
                "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                "JOIN Ghe g ON v.maGhe = g.maGhe " +
                "WHERE CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE) AND hd.maNV = ? " +
                "AND v.trangThaiVe = N'Đã thanh toán'" +
                " GROUP BY g.loaiGhe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai = rs.getString("loaiGhe");
                    if ("GHE_CUNG".equalsIgnoreCase(loai) || "Ghế cứng".equalsIgnoreCase(loai)) result[0] = rs.getInt("sl");
                    else if ("GIUONG_NAM".equalsIgnoreCase(loai) || "Giường nằm".equalsIgnoreCase(loai)) result[1] = rs.getInt("sl");
                    else if ("GHE_MEM".equalsIgnoreCase(loai) || "Ghế mềm".equalsIgnoreCase(loai)) result[2] = rs.getInt("sl");
                }
            }
        }
        return result;
    }

    public static void capNhatTrangThaiVeHetHan() {
        String sqlRename = "UPDATE Ve SET trangThaiVe = N'Chờ thanh toán' WHERE trangThaiVe = N'Chưa thanh toán'";
        String sql = "UPDATE Ve SET trangThaiVe = N'Đã hủy' WHERE trangThaiVe = N'Chờ thanh toán' AND DATEDIFF(minute, ngayMua, GETDATE()) >= 30";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement psRename = con.prepareStatement(sqlRename);
             PreparedStatement ps = con.prepareStatement(sql)) {
            psRename.executeUpdate();
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> layDanhSachMaGheDaDat(String maChuyenTau) {
        capNhatTrangThaiVeHetHan();
        List<String> listGheDaDat = new ArrayList<>();
        String sql = "SELECT maGhe FROM Ve WHERE maChuyenTau = ? AND trangThaiVe IN (N'Đã thanh toán', 'DA_THANH_TOAN', N'Chờ thanh toán')";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listGheDaDat.add(rs.getString("maGhe"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listGheDaDat;
    }

    public boolean insert(Ve v) {
        String sql = "INSERT INTO Ve (maVe, ngayMua, loaiVe, trangThaiVe, giaVe, maGhe, maHoaDon, maChuyenTau) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getMaVe());
            ps.setTimestamp(2, v.getNgayMua() != null ? Timestamp.valueOf(v.getNgayMua()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(3, v.getLoaiVe() != null ? v.getLoaiVe().toString() : null);
            ps.setString(4, v.getTrangThaiVe() != null ? v.getTrangThaiVe() : null);
            ps.setDouble(5, v.getGiaVe());
            ps.setString(6, v.getMaGhe());
            ps.setString(7, v.getMaHoaDon());
            ps.setString(8, v.getMaChuyenTau());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Ve v) {
        String sql = "UPDATE Ve SET ngayMua=?, loaiVe=?, trangThaiVe=?, giaVe=?, maGhe=?, maHoaDon=?, maChuyenTau=? WHERE maVe=?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(v.getNgayMua()));
            ps.setString(2, v.getLoaiVe() != null ? v.getLoaiVe().toString() : null);
            ps.setString(3, v.getTrangThaiVe());
            ps.setDouble(4, v.getGiaVe());
            ps.setString(5, v.getMaGhe());
            ps.setString(6, v.getMaHoaDon());
            ps.setString(7, v.getMaChuyenTau());
            ps.setString(8, v.getMaVe());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM Ve WHERE maVe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
 // ── QUẢN LÝ: đếm vé theo trạng thái trong khoảng thời gian ──
    public static int getSoVeTheoKhoang(java.time.LocalDate from, java.time.LocalDate to, String trangThai) throws java.sql.SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v " +
                "JOIN HoaDon h ON v.maHoaDon = h.maHoaDon " +
                "WHERE v.trangThaiVe = ? " +
                "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── QUẢN LÝ: đếm vé hủy trong khoảng thời gian ──
    public static int getSoVeHuyTheoKhoang(java.time.LocalDate from, java.time.LocalDate to) throws java.sql.SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v " +
                "JOIN HoaDon h ON v.maHoaDon = h.maHoaDon " +
                "WHERE v.trangThaiVe IN (N'Đã hủy', 'DA_HUY') " +
                "AND ISNULL(h.phuongThucThanhToan,'') <> 'LUU_TAM' " +
                "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── QUẢN LÝ: đếm ghế theo loại trong khoảng thời gian ──
    public static int[] getSoGheTheoLoaiTheoKhoang(java.time.LocalDate from, java.time.LocalDate to) throws java.sql.SQLException {
        int[] result = {0, 0, 0};
        String sql = "SELECT g.loaiGhe, COUNT(*) as sl FROM Ve v " +
                "JOIN HoaDon h ON v.maHoaDon = h.maHoaDon " +
                "JOIN Ghe g ON v.maGhe = g.maGhe " +
                "WHERE v.trangThaiVe = N'Đã thanh toán' " +
                "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                "GROUP BY g.loaiGhe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai = rs.getString("loaiGhe");
                    if ("GHE_CUNG".equalsIgnoreCase(loai) || "Ghế cứng".equalsIgnoreCase(loai))       result[0] = rs.getInt("sl");
                    else if ("GIUONG_NAM".equalsIgnoreCase(loai) || "Giường nằm".equalsIgnoreCase(loai)) result[1] = rs.getInt("sl");
                    else if ("GHE_MEM".equalsIgnoreCase(loai) || "Ghế mềm".equalsIgnoreCase(loai))     result[2] = rs.getInt("sl");
                }
            }
        }
        return result;
    }

    // ── QUẢN LÝ: tổng tiền mặt và chuyển khoản theo khoảng thời gian ──
    public static long[] getDoanhThuPTTTTheoKhoang(java.time.LocalDate from, java.time.LocalDate to) throws java.sql.SQLException {
        long[] res = {0L, 0L}; // [0]=tiền mặt, [1]=chuyển khoản
        String sql = "SELECT h.phuongThucThanhToan, ISNULL(SUM(h.tongTien), 0) " +
                "FROM HoaDon h " +
                "WHERE ISNULL(h.phuongThucThanhToan,'') <> 'LUU_TAM' " +
                "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                "AND EXISTS (SELECT 1 FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') " +
                "GROUP BY h.phuongThucThanhToan";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString(1);
                    long   v = rs.getLong(2);
                    if (p != null && (p.toLowerCase().contains("chuyen_khoan") ||
                            p.toLowerCase().contains("vietqr"))) res[1] += v;
                    else res[0] += v;
                }
            }
        }
        return res;
    }
}