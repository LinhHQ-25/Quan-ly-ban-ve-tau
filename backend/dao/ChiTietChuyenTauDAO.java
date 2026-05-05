package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.ChiTietChuyenTau;
import entity.ChuyenTau;
import entity.Ga;

public class ChiTietChuyenTauDAO implements DAO<ChiTietChuyenTau, String> {

    @Override
    public List<ChiTietChuyenTau> selectAll() {
        List<ChiTietChuyenTau> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietChuyenTau";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChuyenTau ct = new ChuyenTau(); ct.setMaChuyen(rs.getString("maChuyen"));
                Ga gaDi = new Ga(); gaDi.setMaGa(rs.getString("maGaDi"));
                Ga gaDen = new Ga(); gaDen.setMaGa(rs.getString("maGaDen"));
                list.add(new ChiTietChuyenTau(rs.getTimestamp("thoiGianKhoiHanh") != null ? rs.getTimestamp("thoiGianKhoiHanh").toLocalDateTime() : null, rs.getTimestamp("thoiGianDuKien") != null ? rs.getTimestamp("thoiGianDuKien").toLocalDateTime() : null, ct, gaDen, gaDi));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ChiTietChuyenTau selectById(String id) {
        String sql = "SELECT * FROM ChiTietChuyenTau WHERE maChuyen = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChuyenTau ct = new ChuyenTau(); ct.setMaChuyen(rs.getString("maChuyen"));
                    Ga gaDi = new Ga(); gaDi.setMaGa(rs.getString("maGaDi"));
                    Ga gaDen = new Ga(); gaDen.setMaGa(rs.getString("maGaDen"));
                    return new ChiTietChuyenTau(rs.getTimestamp("thoiGianKhoiHanh") != null ? rs.getTimestamp("thoiGianKhoiHanh").toLocalDateTime() : null, rs.getTimestamp("thoiGianDuKien") != null ? rs.getTimestamp("thoiGianDuKien").toLocalDateTime() : null, ct, gaDen, gaDi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(ChiTietChuyenTau entity) {
        String sql = "INSERT INTO ChiTietChuyenTau (thoiGianKhoiHanh, thoiGianDuKien, maChuyen, maGaDi, maGaDen) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, entity.getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianKhoiHanh()) : null);
            ps.setTimestamp(2, entity.getThoiGianDuKien() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianDuKien()) : null);
            ps.setString(3, entity.getChuyenTau() != null ? entity.getChuyenTau().getMaChuyen() : null);
            ps.setString(4, entity.getGaDi() != null ? entity.getGaDi().getMaGa() : null);
            ps.setString(5, entity.getGaDen() != null ? entity.getGaDen().getMaGa() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(ChiTietChuyenTau entity) {
        String sql = "UPDATE ChiTietChuyenTau SET thoiGianKhoiHanh = ?, thoiGianDuKien = ?, maGaDi = ?, maGaDen = ? WHERE maChuyen = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, entity.getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianKhoiHanh()) : null);
            ps.setTimestamp(2, entity.getThoiGianDuKien() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianDuKien()) : null);
            ps.setString(3, entity.getGaDi() != null ? entity.getGaDi().getMaGa() : null);
            ps.setString(4, entity.getGaDen() != null ? entity.getGaDen().getMaGa() : null);
            ps.setString(5, entity.getChuyenTau() != null ? entity.getChuyenTau().getMaChuyen() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM ChiTietChuyenTau WHERE maChuyen = ?";
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