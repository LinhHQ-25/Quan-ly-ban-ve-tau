package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.ToaTau;
import entity.Tau;
import entity.LoaiToa;

public class ToaTauDAO implements DAO<ToaTau, String> {

    @Override
    public List<ToaTau> selectAll() {
        List<ToaTau> list = new ArrayList<>();
        String sql = "SELECT * FROM ToaTau";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tau tau = new Tau();
                tau.setMaTau(rs.getString("maTau"));
                list.add(new ToaTau(rs.getString("maToaTau"), rs.getInt("soToa"), rs.getInt("soLuongGhe"), rs.getString("loaiToa") != null ? LoaiToa.valueOf(rs.getString("loaiToa")) : null, tau, rs.getDouble("heSoLoaiToa")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ToaTau selectById(String id) {
        String sql = "SELECT * FROM ToaTau WHERE maToaTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tau tau = new Tau();
                    tau.setMaTau(rs.getString("maTau"));
                    return new ToaTau(rs.getString("maToaTau"), rs.getInt("soToa"), rs.getInt("soLuongGhe"), rs.getString("loaiToa") != null ? LoaiToa.valueOf(rs.getString("loaiToa")) : null, tau, rs.getDouble("heSoLoaiToa"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(ToaTau entity) {
        String sql = "INSERT INTO ToaTau (maToaTau, soToa, soLuongGhe, loaiToa, maTau, heSoLoaiToa) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaToaTau());
            ps.setInt(2, entity.getSoToa());
            ps.setInt(3, entity.getSoLuongGhe());
            ps.setString(4, entity.getLoaiToa() != null ? entity.getLoaiToa().name() : null);
            ps.setString(5, entity.getTau() != null ? entity.getTau().getMaTau() : null);
            ps.setDouble(6, entity.getHeSoLoaiToa());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(ToaTau entity) {
        String sql = "UPDATE ToaTau SET soToa = ?, soLuongGhe = ?, loaiToa = ?, maTau = ?, heSoLoaiToa = ? WHERE maToaTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, entity.getSoToa());
            ps.setInt(2, entity.getSoLuongGhe());
            ps.setString(3, entity.getLoaiToa() != null ? entity.getLoaiToa().name() : null);
            ps.setString(4, entity.getTau() != null ? entity.getTau().getMaTau() : null);
            ps.setDouble(5, entity.getHeSoLoaiToa());
            ps.setString(6, entity.getMaToaTau());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM ToaTau WHERE maToaTau = ?";
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