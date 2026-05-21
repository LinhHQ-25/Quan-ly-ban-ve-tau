package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.Ghe;
import entity.TrangThaiGhe;
import entity.ToaTau;
import entity.LoaiGhe;

public class GheDAO implements DAO<Ghe, String> {

    @Override
    public List<Ghe> selectAll() {
        List<Ghe> list = new ArrayList<>();
        String sql = "SELECT * FROM Ghe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ToaTau tt = new ToaTau();
                tt.setMaToaTau(rs.getString("maToaTau"));
                TrangThaiGhe ttGhe = TrangThaiGhe.tuMoTa(rs.getString("trangThai"));
                list.add(new Ghe(rs.getString("maGhe"), rs.getString("soGhe"), rs.getString("loaiGhe") != null ? LoaiGhe.valueOf(rs.getString("loaiGhe")) : null, tt, ttGhe));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Ghe selectById(String id) {
        String sql = "SELECT * FROM Ghe WHERE maGhe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ToaTau tt = new ToaTau();
                    tt.setMaToaTau(rs.getString("maToaTau"));
                    TrangThaiGhe ttGhe = TrangThaiGhe.tuMoTa(rs.getString("trangThai"));
                    return new Ghe(rs.getString("maGhe"), rs.getString("soGhe"), rs.getString("loaiGhe") != null ? LoaiGhe.valueOf(rs.getString("loaiGhe")) : null, tt, ttGhe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(Ghe entity) {
        String sql = "INSERT INTO Ghe (maGhe, soGhe, loaiGhe, maToaTau, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaGhe());
            ps.setString(2, entity.getSoGhe());
            ps.setString(3, entity.getLoaiGhe() != null ? entity.getLoaiGhe().name() : null);
            ps.setString(4, entity.getToaTau() != null ? entity.getToaTau().getMaToaTau() : null);
            ps.setString(5, entity.getTrangThai() != null ? entity.getTrangThai().toString() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Ghe entity) {
        String sql = "UPDATE Ghe SET soGhe = ?, loaiGhe = ?, maToaTau = ?, trangThai = ? WHERE maGhe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getSoGhe());
            ps.setString(2, entity.getLoaiGhe() != null ? entity.getLoaiGhe().name() : null);
            ps.setString(3, entity.getToaTau() != null ? entity.getToaTau().getMaToaTau() : null);
            ps.setString(4, entity.getTrangThai() != null ? entity.getTrangThai().toString() : null);
            ps.setString(5, entity.getMaGhe());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Ghe WHERE maGhe = ?";
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