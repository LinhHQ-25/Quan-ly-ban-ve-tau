package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.KhuyenMai;
import entity.LoaiKhachHang;

public class KhuyenMaiDAO {

    public List<KhuyenMai> selectAll() {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai ORDER BY maKhuyenMai DESC";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public KhuyenMai selectById(String id) {
        String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, trangThai, moTaChiTiet, tiLeGiamGia, loaiKhachHang, thoiGianBatDau, thoiGianKetThuc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return execute(sql, km, true);
    }

    public boolean update(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai=?, trangThai=?, moTaChiTiet=?, tiLeGiamGia=?, loaiKhachHang=?, thoiGianBatDau=?, thoiGianKetThuc=? WHERE maKhuyenMai=?";
        return execute(sql, km, false);
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private KhuyenMai mapRow(ResultSet rs) throws SQLException {
        String loaiStr = rs.getString("loaiKhachHang");
        return new KhuyenMai(
            rs.getString("maKhuyenMai"),
            rs.getString("tenKhuyenMai"),
            rs.getBoolean("trangThai"),
            rs.getString("moTaChiTiet"),
            rs.getDouble("tiLeGiamGia"),
            (loaiStr != null && !loaiStr.isEmpty()) ? LoaiKhachHang.valueOf(loaiStr) : null,
            rs.getTimestamp("thoiGianBatDau") != null ? rs.getTimestamp("thoiGianBatDau").toLocalDateTime() : null,
            rs.getTimestamp("thoiGianKetThuc") != null ? rs.getTimestamp("thoiGianKetThuc").toLocalDateTime() : null
        );
    }

    private boolean execute(String sql, KhuyenMai km, boolean isInsert) {
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = isInsert ? 1 : 0;
            if (isInsert) ps.setString(1, km.getMaKhuyenMai());
            
            ps.setNString(i + 1, km.getTenKhuyenMai());
            ps.setBoolean(i + 2, km.getTrangThai());
            ps.setNString(i + 3, km.getMoTaChiTiet());
            ps.setDouble(i + 4, km.getTiLeGiamGia());
            ps.setString(i + 5, km.getLoaiKhachHang() != null ? km.getLoaiKhachHang().name() : null);
            ps.setTimestamp(i + 6, km.getThoiGianBatDau() != null ? Timestamp.valueOf(km.getThoiGianBatDau()) : null);
            ps.setTimestamp(i + 7, km.getThoiGianKetThuc() != null ? Timestamp.valueOf(km.getThoiGianKetThuc()) : null);
            
            if (!isInsert) ps.setString(8, km.getMaKhuyenMai());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}