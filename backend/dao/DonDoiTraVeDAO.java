package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.DonDoiTraVe;
import entity.Ve;
import entity.LoaiDon;
import util.MaTuDong;

public class DonDoiTraVeDAO implements DAO<DonDoiTraVe, String> {

    @Override
    public List<DonDoiTraVe> selectAll() {
        List<DonDoiTraVe> list = new ArrayList<>();
        String sql = "SELECT * FROM DonDoiTraVe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Ve ve = new Ve(); ve.setMaVe(rs.getString("maVe"));
                list.add(new DonDoiTraVe(rs.getString("maDon"), rs.getDouble("tienBu"), rs.getTimestamp("ngayLap") != null ? rs.getTimestamp("ngayLap").toLocalDateTime() : null, rs.getDouble("tienHoanTra"), rs.getString("loaiDon") != null ? LoaiDon.valueOf(rs.getString("loaiDon")) : null, ve));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public DonDoiTraVe selectById(String id) {
        String sql = "SELECT * FROM DonDoiTraVe WHERE maDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ve ve = new Ve(); ve.setMaVe(rs.getString("maVe"));
                    return new DonDoiTraVe(rs.getString("maDon"), rs.getDouble("tienBu"), rs.getTimestamp("ngayLap") != null ? rs.getTimestamp("ngayLap").toLocalDateTime() : null, rs.getDouble("tienHoanTra"), rs.getString("loaiDon") != null ? LoaiDon.valueOf(rs.getString("loaiDon")) : null, ve);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(DonDoiTraVe entity) {
        String sql = "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, tienHoanTra, loaiDon, maVe) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (entity.getMaDon() == null || entity.getMaDon().trim().isEmpty()) {
                java.time.LocalDate ngayLap = entity.getNgayLap() != null ? entity.getNgayLap().toLocalDate() : java.time.LocalDate.now();
                entity.setMaDon(MaTuDong.taoMaDon(con, ngayLap));
            }
            ps.setString(1, entity.getMaDon());
            ps.setDouble(2, entity.getTienBu());
            ps.setTimestamp(3, entity.getNgayLap() != null ? java.sql.Timestamp.valueOf(entity.getNgayLap()) : null);
            ps.setDouble(4, entity.getTienHoanTra());
            ps.setString(5, entity.getLoaiDon() != null ? entity.getLoaiDon().name() : null);
            ps.setString(6, entity.getVe() != null ? entity.getVe().getMaVe() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(DonDoiTraVe entity) {
        String sql = "UPDATE DonDoiTraVe SET tienBu = ?, ngayLap = ?, tienHoanTra = ?, loaiDon = ?, maVe = ? WHERE maDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, entity.getTienBu());
            ps.setTimestamp(2, entity.getNgayLap() != null ? java.sql.Timestamp.valueOf(entity.getNgayLap()) : null);
            ps.setDouble(3, entity.getTienHoanTra());
            ps.setString(4, entity.getLoaiDon() != null ? entity.getLoaiDon().name() : null);
            ps.setString(5, entity.getVe() != null ? entity.getVe().getMaVe() : null);
            ps.setString(6, entity.getMaDon());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM DonDoiTraVe WHERE maDon = ?";
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
