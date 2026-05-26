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
                Tau t = new Tau();
                t.setMaTau(rs.getString("maTau"));
                t.setTenTau(rs.getString("tenTau"));
                t.setTrangThai(rs.getString("trangThai"));
                t.setGhiChu(rs.getString("ghiChu"));
                t.setSoToa(rs.getInt("soToa"));
                t.setTongSoGhe(rs.getInt("tongSoGhe"));
                list.add(t);
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
                    Tau t = new Tau();
                    t.setMaTau(rs.getString("maTau"));
                    t.setTenTau(rs.getString("tenTau"));
                    t.setTrangThai(rs.getString("trangThai"));
                    t.setGhiChu(rs.getString("ghiChu"));
                    t.setSoToa(rs.getInt("soToa"));
                    t.setTongSoGhe(rs.getInt("tongSoGhe"));
                    return t;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(Tau entity) {
        String sql = "INSERT INTO Tau (maTau, tenTau, trangThai, ghiChu, soToa, tongSoGhe) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaTau());
            ps.setString(2, entity.getTenTau());
            ps.setString(3, entity.getTrangThai());
            ps.setString(4, entity.getGhiChu());
            ps.setInt(5, entity.getSoToa());
            ps.setInt(6, entity.getTongSoGhe());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Tau entity) {
        String sql = "UPDATE Tau SET tenTau = ?, trangThai = ?, ghiChu = ?, soToa = ?, tongSoGhe = ? WHERE maTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getTenTau());
            ps.setString(2, entity.getTrangThai());
            ps.setString(3, entity.getGhiChu());
            ps.setInt(4, entity.getSoToa());
            ps.setInt(5, entity.getTongSoGhe());
            ps.setString(6, entity.getMaTau());
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
    /**
     * Tàu rảnh theo ngày cụ thể — không có lịch khởi hành vào ngày đó
     */
    public List<Tau> getTauRanhTheoNgay(java.sql.Date ngay) {
        List<Tau> list = new ArrayList<>();
        String sql = "SELECT t.* FROM Tau t "
                   + "WHERE t.trangThai = N'Đang hoạt động' "
                   + "AND NOT EXISTS ( "
                   + "    SELECT 1 FROM ChuyenTau ct "
                   + "    JOIN ChiTietChuyenTau cct ON ct.maChuyenTau = cct.maChuyenTau "
                   + "    WHERE ct.maTau = t.maTau "
                   + "      AND CAST(cct.thoiGianKhoiHanh AS DATE) = ? "
                   + ")";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, ngay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tau t = new Tau();
                    t.setMaTau(rs.getString("maTau"));
                    t.setTenTau(rs.getString("tenTau"));
                    t.setTrangThai(rs.getString("trangThai"));
                    t.setGhiChu(rs.getString("ghiChu"));
                    t.setSoToa(rs.getInt("soToa"));
                    t.setTongSoGhe(rs.getInt("tongSoGhe"));
                    list.add(t);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Tau> getTauRanhMacDinh() {
        List<Tau> list = new ArrayList<>();
        String sql = "SELECT t.* FROM Tau t "
                   + "WHERE t.trangThai = N'Đang hoạt động' "
                   + "AND NOT EXISTS ( "
                   + "    SELECT 1 FROM ChuyenTau ct "
                   + "    JOIN ChiTietChuyenTau cct ON ct.maChuyenTau = cct.maChuyenTau "
                   + "    WHERE ct.maTau = t.maTau "
                   + "      AND cct.thoiGianKhoiHanh > GETDATE() "
                   + ")";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tau t = new Tau();
                t.setMaTau(rs.getString("maTau"));
                t.setTenTau(rs.getString("tenTau"));
                t.setTrangThai(rs.getString("trangThai"));
                t.setGhiChu(rs.getString("ghiChu"));
                t.setSoToa(rs.getInt("soToa"));
                t.setTongSoGhe(rs.getInt("tongSoGhe"));
                list.add(t);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}