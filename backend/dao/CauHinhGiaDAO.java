package dao;

import connect_DB.Connect_DB;
import java.sql.*;

public class CauHinhGiaDAO {

    public double getGiaCoBan() {
        String sql = "SELECT giaTri FROM CauHinhGia WHERE maCauHinh = 'GIA_CO_BAN'";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 300000;
    }

    public boolean updateGiaCoBan(double giaTri) {
        String sql = "UPDATE CauHinhGia SET giaTri = ? WHERE maCauHinh = 'GIA_CO_BAN'";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, giaTri);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}