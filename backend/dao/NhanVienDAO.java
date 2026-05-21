package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.NhanVien;
import entity.LoaiNhanVien;

public class NhanVienDAO implements DAO<NhanVien, String> {

    @Override
    public List<NhanVien> selectAll() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new NhanVien(rs.getString("maNV"), rs.getString("hoTenNV"), rs.getString("email"), rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toLocalDate() : null, rs.getString("soDT"), rs.getBoolean("gioiTinh"), rs.getString("diaChi"), rs.getString("soCCCD"), rs.getString("loaiNV") != null ? LoaiNhanVien.valueOf(rs.getString("loaiNV")) : null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public NhanVien selectById(String id) {
        String sql = "SELECT * FROM NhanVien WHERE maNV = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new NhanVien(rs.getString("maNV"), rs.getString("hoTenNV"), rs.getString("email"), rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toLocalDate() : null, rs.getString("soDT"), rs.getBoolean("gioiTinh"), rs.getString("diaChi"), rs.getString("soCCCD"), rs.getString("loaiNV") != null ? LoaiNhanVien.valueOf(rs.getString("loaiNV")) : null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(NhanVien entity) {
        String sql = "INSERT INTO NhanVien (maNV, hoTenNV, email, ngaySinh, soDT, gioiTinh, diaChi, soCCCD, loaiNV) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaNV());
            ps.setString(2, entity.getHoTenNV());
            ps.setString(3, entity.getEmail());
            ps.setDate(4, entity.getNgaySinh() != null ? java.sql.Date.valueOf(entity.getNgaySinh()) : null);
            ps.setString(5, entity.getSoDT());
            ps.setBoolean(6, entity.getGioiTinh());
            ps.setString(7, entity.getDiaChi());
            ps.setString(8, entity.getSoCCCD());
            ps.setString(9, entity.getLoaiNV() != null ? entity.getLoaiNV().name() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(NhanVien entity) {
        String sql = "UPDATE NhanVien SET hoTenNV = ?, email = ?, ngaySinh = ?, soDT = ?, gioiTinh = ?, diaChi = ?, soCCCD = ?, loaiNV = ? WHERE maNV = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getHoTenNV());
            ps.setString(2, entity.getEmail());
            ps.setDate(3, entity.getNgaySinh() != null ? java.sql.Date.valueOf(entity.getNgaySinh()) : null);
            ps.setString(4, entity.getSoDT());
            ps.setBoolean(5, entity.getGioiTinh());
            ps.setString(6, entity.getDiaChi());
            ps.setString(7, entity.getSoCCCD());
            ps.setString(8, entity.getLoaiNV() != null ? entity.getLoaiNV().name() : null);
            ps.setString(9, entity.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM NhanVien WHERE maNV = ?";
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