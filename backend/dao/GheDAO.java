package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.Ghe;
import entity.LoaiGhe;
import entity.ToaTau;

public class GheDAO implements DAO<Ghe, String> {

    /** Parse loaiGhe an toàn: thử valueOf trước, nếu fail thì map tiếng Việt cũ */
    private LoaiGhe parseLoaiGhe(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LoaiGhe.valueOf(raw.trim()); // GHE_CUNG, GHE_MEM, GIUONG_NAM
        } catch (IllegalArgumentException e) {
            // Fallback map tiếng Việt (dữ liệu cũ trước khi chạy SQL migrate)
            return switch (raw.trim()) {
                case "Ghế cứng"   -> LoaiGhe.GHE_CUNG;
                case "Ghế mềm"    -> LoaiGhe.GHE_MEM;
                case "Giường nằm" -> LoaiGhe.GIUONG_NAM;
                default            -> null;
            };
        }
    }

    private Ghe mapRow(ResultSet rs) throws Exception {
        ToaTau tt = new ToaTau();
        tt.setMaToaTau(rs.getString("maToaTau"));
        return new Ghe(
            rs.getString("maGhe"),
            rs.getString("soGhe"),
            parseLoaiGhe(rs.getString("loaiGhe")),
            tt
        );
    }

    @Override
    public List<Ghe> selectAll() {
        List<Ghe> list = new ArrayList<>();
        String sql = "SELECT * FROM Ghe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Ghe selectById(String id) {
        String sql = "SELECT * FROM Ghe WHERE maGhe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Lấy Ghe kèm đầy đủ thông tin ToaTau (heSoLoaiToa) — dùng để tính giá vé.
     */
    public Ghe selectByIdWithToa(String maGhe) {
        String sql = "SELECT g.maGhe, g.soGhe, g.loaiGhe, g.maToaTau, "
                   + "t.soToa, t.soLuongGhe, t.loaiToa, t.heSoLoaiToa, t.maTau "
                   + "FROM Ghe g JOIN ToaTau t ON g.maToaTau = t.maToaTau "
                   + "WHERE g.maGhe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGhe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ToaTau tt = new ToaTau();
                    tt.setMaToaTau(rs.getString("maToaTau"));
                    tt.setSoToa(rs.getInt("soToa"));
                    tt.setSoLuongGhe(rs.getInt("soLuongGhe"));
                    tt.setHeSoLoaiToa(rs.getDouble("heSoLoaiToa"));
                    return new Ghe(
                        rs.getString("maGhe"),
                        rs.getString("soGhe"),
                        parseLoaiGhe(rs.getString("loaiGhe")),
                        tt
                    );
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean insert(Ghe entity) {
        String sql = "INSERT INTO Ghe (maGhe, soGhe, loaiGhe, maToaTau) VALUES (?,?,?,?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaGhe());
            ps.setString(2, entity.getSoGhe());
            ps.setString(3, entity.getLoaiGhe() != null ? entity.getLoaiGhe().name() : null);
            ps.setString(4, entity.getToaTau()  != null ? entity.getToaTau().getMaToaTau() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean update(Ghe entity) {
        String sql = "UPDATE Ghe SET soGhe=?, loaiGhe=?, maToaTau=? WHERE maGhe=?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getSoGhe());
            ps.setString(2, entity.getLoaiGhe() != null ? entity.getLoaiGhe().name() : null);
            ps.setString(3, entity.getToaTau()  != null ? entity.getToaTau().getMaToaTau() : null);
            ps.setString(4, entity.getMaGhe());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Ghe WHERE maGhe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
    /**
     * Lấy {soGhe, soToa} để in lên vé PDF.
     * Trả về Object[]{String soGhe, String soToa} hoặc {"", ""} nếu không tìm thấy.
     */
    public Object[] getSoGheVaSoToa(String maGhe) {
        String sql = "SELECT g.soGhe, t.soToa "
                   + "FROM Ghe g JOIN ToaTau t ON g.maToaTau = t.maToaTau "
                   + "WHERE g.maGhe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGhe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Object[]{ rs.getString("soGhe"), String.valueOf(rs.getInt("soToa")) };
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new Object[]{ "", "" };
    }
}