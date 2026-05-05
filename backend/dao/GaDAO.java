package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.Ga;

public class GaDAO implements DAO<Ga, String> {

    @Override
    public List<Ga> selectAll() {
        List<Ga> list = new ArrayList<>();
        String sql = "SELECT * FROM Ga";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Ga(rs.getString("maGa"), rs.getString("tenGa"), rs.getString("diaChi"), rs.getString("tinhThanh")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Ga selectById(String id) {
        String sql = "SELECT * FROM Ga WHERE maGa = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ga(rs.getString("maGa"), rs.getString("tenGa"), rs.getString("diaChi"), rs.getString("tinhThanh"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(Ga entity) {
        String sql = "INSERT INTO Ga (maGa, tenGa, diaChi, tinhThanh) VALUES (?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaGa());
            ps.setString(2, entity.getTenGa());
            ps.setString(3, entity.getDiaChi());
            ps.setString(4, entity.getTinhThanh());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Ga entity) {
        String sql = "UPDATE Ga SET tenGa = ?, diaChi = ?, tinhThanh = ? WHERE maGa = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getTenGa());
            ps.setString(2, entity.getDiaChi());
            ps.setString(3, entity.getTinhThanh());
            ps.setString(4, entity.getMaGa());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Ga WHERE maGa = ?";
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