package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.KhachHang;

public class KhachHangDAO implements DAO<KhachHang, String> {

    @Override
    public List<KhachHang> selectAll() {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new KhachHang(rs.getString("maKH"), rs.getString("hoTenKH"), rs.getString("cccd"), rs.getString("sdt"), rs.getString("email"), rs.getDate("namSinh") != null ? rs.getDate("namSinh").toLocalDate() : null, rs.getBoolean("laSinhVien")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public KhachHang selectById(String id) {
        String sql = "SELECT * FROM KhachHang WHERE maKH = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(rs.getString("maKH"), rs.getString("hoTenKH"), rs.getString("cccd"), rs.getString("sdt"), rs.getString("email"), rs.getDate("namSinh") != null ? rs.getDate("namSinh").toLocalDate() : null, rs.getBoolean("laSinhVien"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public KhachHang timTheoSDT(String sdt) {
        String sql = "SELECT * FROM KhachHang WHERE sdt = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(rs.getString("maKH"), rs.getString("hoTenKH"), rs.getString("cccd"), rs.getString("sdt"), rs.getString("email"), rs.getDate("namSinh") != null ? rs.getDate("namSinh").toLocalDate() : null, rs.getBoolean("laSinhVien"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(KhachHang entity) {
        String sql = "INSERT INTO KhachHang (maKH, hoTenKH, cccd, sdt, email, namSinh, laSinhVien) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaKH());
            ps.setString(2, entity.getHoTenKH());
            ps.setString(3, entity.getCccd());
            ps.setString(4, entity.getSdt());
            ps.setString(5, entity.getEmail());
            ps.setDate(6, entity.getNamSinh() != null ? java.sql.Date.valueOf(entity.getNamSinh()) : null);
            ps.setBoolean(7, entity.getLaSinhVien());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(KhachHang entity) {
        String sql = "UPDATE KhachHang SET hoTenKH = ?, cccd = ?, email = ?, namSinh = ?, laSinhVien = ? WHERE sdt = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getHoTenKH());
            ps.setString(2, entity.getCccd());
            ps.setString(3, entity.getEmail());
            ps.setDate(4, entity.getNamSinh() != null ? java.sql.Date.valueOf(entity.getNamSinh()) : null);
            ps.setBoolean(5, entity.getLaSinhVien());
            ps.setString(6, entity.getSdt());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM KhachHang WHERE maKH = ?";
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