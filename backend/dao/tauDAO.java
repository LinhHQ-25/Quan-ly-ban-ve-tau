package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.Tau;

public class tauDAO implements DAO<Tau, String> {

    @Override
    public List<Tau> selectAll() {
        List<Tau> list = new ArrayList<>();
        String sql = "SELECT * FROM Tau";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Tau(rs.getString("maTau"), rs.getString("tenTau"), rs.getInt("soLuongToa")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Tau selectById(String id) {
        String sql = "SELECT * FROM Tau WHERE maTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Tau(rs.getString("maTau"), rs.getString("tenTau"), rs.getInt("soLuongToa"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(Tau entity) {
        String sql = "INSERT INTO Tau (maTau, tenTau, soLuongToa) VALUES (?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaTau());
            ps.setString(2, entity.getTenTau());
            ps.setInt(3, entity.getSoLuongToa());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Tau entity) {
        String sql = "UPDATE Tau SET tenTau = ?, soLuongToa = ? WHERE maTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getTenTau());
            ps.setInt(2, entity.getSoLuongToa());
            ps.setString(3, entity.getMaTau());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Tau WHERE maTau = ?";
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