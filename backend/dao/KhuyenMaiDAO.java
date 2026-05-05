package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.KhuyenMai;
import entity.LoaiKhachHang;

public class KhuyenMaiDAO implements DAO<KhuyenMai, String> {

    @Override
    public List<KhuyenMai> selectAll() {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new KhuyenMai(rs.getString("maKhuyenMai"), rs.getString("tenKhuyenMai"), rs.getBoolean("trangThai"), rs.getString("moTaChiTiet"), rs.getDouble("tiLeGiamGia"), rs.getString("loaiKhachHang") != null ? LoaiKhachHang.valueOf(rs.getString("loaiKhachHang")) : null, rs.getTimestamp("thoiGianBatDau") != null ? rs.getTimestamp("thoiGianBatDau").toLocalDateTime() : null, rs.getTimestamp("thoiGianKetThuc") != null ? rs.getTimestamp("thoiGianKetThuc").toLocalDateTime() : null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public KhuyenMai selectById(String id) {
        String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhuyenMai(rs.getString("maKhuyenMai"), rs.getString("tenKhuyenMai"), rs.getBoolean("trangThai"), rs.getString("moTaChiTiet"), rs.getDouble("tiLeGiamGia"), rs.getString("loaiKhachHang") != null ? LoaiKhachHang.valueOf(rs.getString("loaiKhachHang")) : null, rs.getTimestamp("thoiGianBatDau") != null ? rs.getTimestamp("thoiGianBatDau").toLocalDateTime() : null, rs.getTimestamp("thoiGianKetThuc") != null ? rs.getTimestamp("thoiGianKetThuc").toLocalDateTime() : null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(KhuyenMai entity) {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, trangThai, moTaChiTiet, tiLeGiamGia, loaiKhachHang, thoiGianBatDau, thoiGianKetThuc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaKhuyenMai());
            ps.setString(2, entity.getTenKhuyenMai());
            ps.setBoolean(3, entity.getTrangThai());
            ps.setString(4, entity.getMoTaChiTiet());
            ps.setDouble(5, entity.getTiLeGiamGia());
            ps.setString(6, entity.getLoaiKhachHang() != null ? entity.getLoaiKhachHang().name() : null);
            ps.setTimestamp(7, entity.getThoiGianBatDau() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianBatDau()) : null);
            ps.setTimestamp(8, entity.getThoiGianKetThuc() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianKetThuc()) : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(KhuyenMai entity) {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, trangThai = ?, moTaChiTiet = ?, tiLeGiamGia = ?, loaiKhachHang = ?, thoiGianBatDau = ?, thoiGianKetThuc = ? WHERE maKhuyenMai = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getTenKhuyenMai());
            ps.setBoolean(2, entity.getTrangThai());
            ps.setString(3, entity.getMoTaChiTiet());
            ps.setDouble(4, entity.getTiLeGiamGia());
            ps.setString(5, entity.getLoaiKhachHang() != null ? entity.getLoaiKhachHang().name() : null);
            ps.setTimestamp(6, entity.getThoiGianBatDau() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianBatDau()) : null);
            ps.setTimestamp(7, entity.getThoiGianKetThuc() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianKetThuc()) : null);
            ps.setString(8, entity.getMaKhuyenMai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
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
}