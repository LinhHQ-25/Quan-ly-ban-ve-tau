package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.Ga;

public class GaDAO implements DAO<Ga, String> {

    /** Map một ResultSet row → Ga (dùng chung để tránh lặp code) */
    private Ga mapRow(ResultSet rs) throws Exception {
        return new Ga(
            rs.getString("maGa"),
            rs.getString("tenGa"),
            rs.getString("diaChi"),
            rs.getString("tinhThanh"),
            rs.getDouble("heSoCuLy")
        );
    }

    @Override
    public List<Ga> selectAll() {
        List<Ga> list = new ArrayList<>();
        String sql = "SELECT * FROM Ga";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Ga selectById(String id) {
        String sql = "SELECT * FROM Ga WHERE maGa = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Tìm ga theo tên (dùng cho GUI tìm kiếm) */
    public Ga timTheoTen(String tenGa) {
        String sql = "SELECT * FROM Ga WHERE tenGa LIKE ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, "%" + tenGa.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean insert(Ga entity) {
        String sql = "INSERT INTO Ga (maGa, tenGa, diaChi, tinhThanh, heSoCuLy) VALUES (?,?,?,?,?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaGa());
            ps.setNString(2, entity.getTenGa());
            ps.setNString(3, entity.getDiaChi());
            ps.setNString(4, entity.getTinhThanh());
            ps.setDouble(5, entity.getHeSoCuLy());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean update(Ga entity) {
        String sql = "UPDATE Ga SET tenGa=?, diaChi=?, tinhThanh=?, heSoCuLy=? WHERE maGa=?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, entity.getTenGa());
            ps.setNString(2, entity.getDiaChi());
            ps.setNString(3, entity.getTinhThanh());
            ps.setDouble(4, entity.getHeSoCuLy());
            ps.setString(5, entity.getMaGa());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Ga WHERE maGa = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
    /** Lấy heSoCuLy theo maGa, trả về 1.2 nếu không tìm thấy (fallback mặc định) */
    public double getHeSoCuLy(String maGa) {
        if (maGa == null || maGa.isBlank()) return 1.2;
        Ga ga = selectById(maGa);
        return (ga != null) ? ga.getHeSoCuLy() : 1.2;
    }
}