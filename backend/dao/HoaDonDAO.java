package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhuongThucThanhToan;

public class HoaDonDAO implements DAO<HoaDon, String> {

    @Override
    public List<HoaDon> selectAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                list.add(new HoaDon(rs.getString("maHoaDon"),
                        rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime() : null,
                        nv, kh, rs.getDouble("tongTien"), rs.getDouble("tienNhan"),
                        rs.getString("phuongThucThanhToan") != null ? PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan")) : null));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public HoaDon selectById(String id) {
        String sql = "SELECT * FROM HoaDon WHERE maHoaDon = ?";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    KhachHang kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    return new HoaDon(rs.getString("maHoaDon"),
                            rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime() : null,
                            nv, kh, rs.getDouble("tongTien"), rs.getDouble("tienNhan"),
                            rs.getString("phuongThucThanhToan") != null ? PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan")) : null);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean insert(HoaDon entity) {
        String sql = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaHoaDon());
            ps.setTimestamp(2, entity.getNgayLapHD() != null ? java.sql.Timestamp.valueOf(entity.getNgayLapHD()) : null);
            ps.setString(3, entity.getNhanVien() != null ? entity.getNhanVien().getMaNV() : null);
            ps.setString(4, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setDouble(5, entity.getTongTien());
            ps.setDouble(6, entity.getTienNhan());
            ps.setString(7, entity.getPhuongThucThanhToan() != null ? entity.getPhuongThucThanhToan().name() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean update(HoaDon entity) {
        String sql = "UPDATE HoaDon SET ngayLapHD = ?, maNV = ?, maKH = ?, tongTien = ?, tienNhan = ?, phuongThucThanhToan = ? WHERE maHoaDon = ?";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, entity.getNgayLapHD() != null ? java.sql.Timestamp.valueOf(entity.getNgayLapHD()) : null);
            ps.setString(2, entity.getNhanVien() != null ? entity.getNhanVien().getMaNV() : null);
            ps.setString(3, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setDouble(4, entity.getTongTien());
            ps.setDouble(5, entity.getTienNhan());
            ps.setString(6, entity.getPhuongThucThanhToan() != null ? entity.getPhuongThucThanhToan().name() : null);
            ps.setString(7, entity.getMaHoaDon());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM HoaDon WHERE maHoaDon = ?";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public static List<Object[]> getDanhSachHoaDonTheoCa(java.time.LocalDate ngay, String ca, String maNV) throws SQLException {
        String timeCondition = ca.equalsIgnoreCase("Sáng")
                ? " BETWEEN '00:00:00' AND '11:59:59'"
                : " BETWEEN '12:00:00' AND '23:59:59'";

        String sql = "SELECT h.maHoaDon, " +
                "       CONVERT(varchar, h.ngayLapHD, 108) AS gioBan, " +
                "       k.hoTenKH, " +
                "       (SELECT TOP 1 g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS loaiGhe, " +
                "       (SELECT COUNT(*) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS soGhe, " +
                "       (SELECT ISNULL(SUM(v.giaVe), 0) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS tongTien " +
                "FROM HoaDon h " +
                "JOIN KhachHang k ON h.maKH = k.maKH " +
                "WHERE CAST(h.ngayLapHD AS DATE) = ? AND h.maNV = ? " +
                "AND CAST(h.ngayLapHD AS TIME)" + timeCondition +
                " ORDER BY h.ngayLapHD DESC";

        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("gioBan"),
                            rs.getString("hoTenKH"),
                            rs.getString("loaiGhe"),
                            rs.getInt("soGhe"),
                            rs.getDouble("tongTien")
                    });
                }
            }
        }
        return rows;
    }

    // THÊM MỚI - lấy danh sách hóa đơn có vé hủy
    public static List<Object[]> getDanhSachHoaDonHuyTheoCa(java.time.LocalDate ngay, String ca, String maNV) throws SQLException {
        String timeCondition = ca.equalsIgnoreCase("Sáng")
                ? " BETWEEN '00:00:00' AND '11:59:59'"
                : " BETWEEN '12:00:00' AND '23:59:59'";

        String sql = "SELECT h.maHoaDon, " +
                "       CONVERT(varchar, h.ngayLapHD, 108) AS gioBan, " +
                "       k.hoTenKH, " +
                "       (SELECT TOP 1 g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã hủy') AS loaiGhe, " +
                "       (SELECT COUNT(*) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã hủy') AS soGhe, " +
                "       (SELECT ISNULL(SUM(v.giaVe), 0) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã hủy') AS tongTien " +
                "FROM HoaDon h " +
                "JOIN KhachHang k ON h.maKH = k.maKH " +
                "WHERE CAST(h.ngayLapHD AS DATE) = ? AND h.maNV = ? " +
                "AND CAST(h.ngayLapHD AS TIME)" + timeCondition +
                " AND EXISTS (SELECT 1 FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã hủy')" +
                " ORDER BY h.ngayLapHD DESC";

        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("gioBan"),
                            rs.getString("hoTenKH"),
                            rs.getString("loaiGhe"),
                            rs.getInt("soGhe"),
                            rs.getDouble("tongTien")
                    });
                }
            }
        }
        return rows;
    }
}