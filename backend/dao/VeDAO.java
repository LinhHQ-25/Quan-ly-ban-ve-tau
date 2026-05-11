package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.Ve;
import entity.Ghe;
import entity.HoaDon;
import entity.KhachHang;
import entity.ChiTietChuyenTau;
import entity.LoaiVe;

public class VeDAO implements DAO<Ve, String> {

    @Override
    public List<Ve> selectAll() {
        List<Ve> list = new ArrayList<>();
        String sql = "SELECT * FROM Ve";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Ghe ghe = new Ghe(); ghe.setMaGhe(rs.getString("maGhe"));
                HoaDon hd = new HoaDon(); hd.setMaHoaDon(rs.getString("maHoaDon"));
                KhachHang kh = new KhachHang(); kh.setMaKH(rs.getString("maKH"));
                ChiTietChuyenTau gaDi = new ChiTietChuyenTau(); gaDi.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanhGaDi") != null ? rs.getTimestamp("thoiGianKhoiHanhGaDi").toLocalDateTime() : null);
                ChiTietChuyenTau gaDen = new ChiTietChuyenTau(); gaDen.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanhGaDen") != null ? rs.getTimestamp("thoiGianKhoiHanhGaDen").toLocalDateTime() : null);
                
                list.add(new Ve(rs.getString("maVe"), ghe, rs.getTimestamp("ngayMua") != null ? rs.getTimestamp("ngayMua").toLocalDateTime() : null, rs.getString("loaiVe") != null ? LoaiVe.valueOf(rs.getString("loaiVe")) : null, rs.getBoolean("trangThaiVe"), hd, kh, gaDi, gaDen));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Ve selectById(String id) {
        String sql = "SELECT * FROM Ve WHERE maVe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ghe ghe = new Ghe(); ghe.setMaGhe(rs.getString("maGhe"));
                    HoaDon hd = new HoaDon(); hd.setMaHoaDon(rs.getString("maHoaDon"));
                    KhachHang kh = new KhachHang(); kh.setMaKH(rs.getString("maKH"));
                    ChiTietChuyenTau gaDi = new ChiTietChuyenTau(); gaDi.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanhGaDi") != null ? rs.getTimestamp("thoiGianKhoiHanhGaDi").toLocalDateTime() : null);
                    ChiTietChuyenTau gaDen = new ChiTietChuyenTau(); gaDen.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanhGaDen") != null ? rs.getTimestamp("thoiGianKhoiHanhGaDen").toLocalDateTime() : null);

                    return new Ve(rs.getString("maVe"), ghe, rs.getTimestamp("ngayMua") != null ? rs.getTimestamp("ngayMua").toLocalDateTime() : null, rs.getString("loaiVe") != null ? LoaiVe.valueOf(rs.getString("loaiVe")) : null, rs.getBoolean("trangThaiVe"), hd, kh, gaDi, gaDen);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(Ve entity) {
        String sql = "INSERT INTO Ve (maVe, maGhe, ngayMua, loaiVe, trangThaiVe, maHoaDon, maKH, thoiGianKhoiHanhGaDi, thoiGianKhoiHanhGaDen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaVe());
            ps.setString(2, entity.getGhe() != null ? entity.getGhe().getMaGhe() : null);
            ps.setTimestamp(3, entity.getNgayMua() != null ? java.sql.Timestamp.valueOf(entity.getNgayMua()) : null);
            ps.setString(4, entity.getLoaiVe() != null ? entity.getLoaiVe().name() : null);
            ps.setBoolean(5, entity.getTrangThaiVe());
            ps.setString(6, entity.getHoaDon() != null ? entity.getHoaDon().getMaHoaDon() : null);
            ps.setString(7, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setTimestamp(8, entity.getGaDi() != null && entity.getGaDi().getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getGaDi().getThoiGianKhoiHanh()) : null);
            ps.setTimestamp(9, entity.getGaDen() != null && entity.getGaDen().getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getGaDen().getThoiGianKhoiHanh()) : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Ve entity) {
        String sql = "UPDATE Ve SET maGhe = ?, ngayMua = ?, loaiVe = ?, trangThaiVe = ?, maHoaDon = ?, maKH = ?, thoiGianKhoiHanhGaDi = ?, thoiGianKhoiHanhGaDen = ? WHERE maVe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getGhe() != null ? entity.getGhe().getMaGhe() : null);
            ps.setTimestamp(2, entity.getNgayMua() != null ? java.sql.Timestamp.valueOf(entity.getNgayMua()) : null);
            ps.setString(3, entity.getLoaiVe() != null ? entity.getLoaiVe().name() : null);
            ps.setBoolean(4, entity.getTrangThaiVe());
            ps.setString(5, entity.getHoaDon() != null ? entity.getHoaDon().getMaHoaDon() : null);
            ps.setString(6, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setTimestamp(7, entity.getGaDi() != null && entity.getGaDi().getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getGaDi().getThoiGianKhoiHanh()) : null);
            ps.setTimestamp(8, entity.getGaDen() != null && entity.getGaDen().getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getGaDen().getThoiGianKhoiHanh()) : null);
            ps.setString(9, entity.getMaVe());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Ve WHERE maVe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private static String getHourCondition(String ca) {
        return ca.equalsIgnoreCase("Sáng")
                ? " AND CAST(ngayMua AS TIME) BETWEEN '00:00:00' AND '11:59:59'"
                : " AND CAST(ngayMua AS TIME) BETWEEN '12:00:00' AND '23:59:59'";
    }

    public static long getTongDoanhThuTheoCa(java.time.LocalDate ngay, String ca) throws SQLException {
        String sql = "SELECT ISNULL(SUM(giaVe), 0) FROM Ve " +
                "WHERE CAST(ngayMua AS DATE) = ? " +
                "AND trangThaiVe = 'DA_THANH_TOAN'" + getHourCondition(ca);
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    public static int getSoLuongVeTheoCa(java.time.LocalDate ngay, String ca, String trangThai) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve " +
                "WHERE CAST(ngayMua AS DATE) = ? AND trangThaiVe = ?" + getHourCondition(ca);
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static int[] getSoGheTheoLoaiTheoCa(java.time.LocalDate ngay, String ca) throws SQLException {
        int[] result = {0, 0, 0};
        String sql = "SELECT g.loaiGhe, COUNT(*) as sl FROM Ve v " +
                "JOIN Ghe g ON v.maGhe = g.maGhe " +
                "WHERE CAST(v.ngayMua AS DATE) = ? " +
                "AND v.trangThaiVe = 'DA_THANH_TOAN'" + getHourCondition(ca) +
                " GROUP BY g.loaiGhe";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai = rs.getString("loaiGhe");
                    if (loai.equals("GHE_CUNG")) result[0] = rs.getInt("sl");
                    else if (loai.equals("GIUONG_NAM")) result[1] = rs.getInt("sl");
                    else if (loai.equals("GHE_MEM")) result[2] = rs.getInt("sl");
                }
            }
        }
        return result;
    }
}