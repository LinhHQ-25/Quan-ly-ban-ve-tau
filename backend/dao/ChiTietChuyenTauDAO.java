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
                ChuyenTau ct = new ChuyenTau(); ct.setMaChuyenTau(rs.getString("maChuyenTau"));
                Ga gaDi = new Ga(); gaDi.setMaGa(rs.getString("maGaDi"));
                Ga gaDen = new Ga(); gaDen.setMaGa(rs.getString("maGaDen"));
                
                ChiTietChuyenTau dt = new ChiTietChuyenTau();
                dt.setChuyenTau(ct);
                dt.setGaDi(gaDi);
                dt.setGaDen(gaDen);
                dt.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanh") != null ? rs.getTimestamp("thoiGianKhoiHanh").toLocalDateTime() : null);
                dt.setThoiGianDuKien(rs.getTimestamp("thoiGianDuKien") != null ? rs.getTimestamp("thoiGianDuKien").toLocalDateTime() : null);
                
                list.add(dt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ChiTietChuyenTau selectById(String maChuyenTau) {
        String sql = "SELECT * FROM ChiTietChuyenTau WHERE maChuyenTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChuyenTau ct = new ChuyenTau(); ct.setMaChuyenTau(rs.getString("maChuyenTau"));
                    Ga gaDi = new Ga(); gaDi.setMaGa(rs.getString("maGaDi"));
                    Ga gaDen = new Ga(); gaDen.setMaGa(rs.getString("maGaDen"));
                    
                    ChiTietChuyenTau dt = new ChiTietChuyenTau();
                    dt.setChuyenTau(ct);
                    dt.setGaDi(gaDi);
                    dt.setGaDen(gaDen);
                    dt.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanh") != null ? rs.getTimestamp("thoiGianKhoiHanh").toLocalDateTime() : null);
                    dt.setThoiGianDuKien(rs.getTimestamp("thoiGianDuKien") != null ? rs.getTimestamp("thoiGianDuKien").toLocalDateTime() : null);
                    
                    return dt;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(ChiTietChuyenTau entity) {
        String sql = "INSERT INTO ChiTietChuyenTau (maChuyenTau, maGaDi, maGaDen, thoiGianKhoiHanh, thoiGianDuKien) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getChuyenTau() != null ? entity.getChuyenTau().getMaChuyenTau() : null);
            ps.setString(2, entity.getGaDi() != null ? entity.getGaDi().getMaGa() : null);
            ps.setString(3, entity.getGaDen() != null ? entity.getGaDen().getMaGa() : null);
            ps.setTimestamp(4, entity.getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianKhoiHanh()) : null);
            ps.setTimestamp(5, entity.getThoiGianDuKien() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianDuKien()) : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(ChiTietChuyenTau entity) {
        String sql = "UPDATE ChiTietChuyenTau SET maGaDi = ?, maGaDen = ?, thoiGianKhoiHanh = ?, thoiGianDuKien = ? WHERE maChuyenTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getGaDi() != null ? entity.getGaDi().getMaGa() : null);
            ps.setString(2, entity.getGaDen() != null ? entity.getGaDen().getMaGa() : null);
            ps.setTimestamp(3, entity.getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianKhoiHanh()) : null);
            ps.setTimestamp(4, entity.getThoiGianDuKien() != null ? java.sql.Timestamp.valueOf(entity.getThoiGianDuKien()) : null);
            ps.setString(5, entity.getChuyenTau() != null ? entity.getChuyenTau().getMaChuyenTau() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String maChuyenTau) {
        String sql = "DELETE FROM ChiTietChuyenTau WHERE maChuyenTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}