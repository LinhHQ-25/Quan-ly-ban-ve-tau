package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.Ve;
import entity.Ghe;
import entity.HoaDon;
import entity.KhachHang;
import entity.ChiTietChuyenTau;
import entity.LoaiVe;

public class VeDAO implements DAO<Ve, String> {

    @Override
    public List<Ve> selectAll() {
        List<Ve> list = new ArrayList<>();
        String sql = "SELECT * FROM Ve";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Ghe ghe = new Ghe(); ghe.setMaGhe(rs.getString("maGhe"));
                HoaDon hd = new HoaDon(); hd.setMaHoaDon(rs.getString("maHoaDon"));
                KhachHang kh = new KhachHang(); kh.setMaKH(rs.getString("maKH"));
                ChiTietChuyenTau gaDi = new ChiTietChuyenTau(); gaDi.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanhGaDi") != null ? rs.getTimestamp("thoiGianKhoiHanhGaDi").toLocalDateTime() : null);
                ChiTietChuyenTau gaDen = new ChiTietChuyenTau(); gaDen.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanhGaDen") != null ? rs.getTimestamp("thoiGianKhoiHanhGaDen").toLocalDateTime() : null);
                
                list.add(new Ve(rs.getString("maVe"), ghe, rs.getTimestamp("ngayMua") != null ? rs.getTimestamp("ngayMua").toLocalDateTime() : null, rs.getString("loaiVe") != null ? LoaiVe.valueOf(rs.getString("loaiVe")) : null, rs.getBoolean("trangThaiVe"), hd, kh, gaDi, gaDen));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Ve selectById(String id) {
        String sql = "SELECT * FROM Ve WHERE maVe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ghe ghe = new Ghe(); ghe.setMaGhe(rs.getString("maGhe"));
                    HoaDon hd = new HoaDon(); hd.setMaHoaDon(rs.getString("maHoaDon"));
                    KhachHang kh = new KhachHang(); kh.setMaKH(rs.getString("maKH"));
                    ChiTietChuyenTau gaDi = new ChiTietChuyenTau(); gaDi.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanhGaDi") != null ? rs.getTimestamp("thoiGianKhoiHanhGaDi").toLocalDateTime() : null);
                    ChiTietChuyenTau gaDen = new ChiTietChuyenTau(); gaDen.setThoiGianKhoiHanh(rs.getTimestamp("thoiGianKhoiHanhGaDen") != null ? rs.getTimestamp("thoiGianKhoiHanhGaDen").toLocalDateTime() : null);

                    return new Ve(rs.getString("maVe"), ghe, rs.getTimestamp("ngayMua") != null ? rs.getTimestamp("ngayMua").toLocalDateTime() : null, rs.getString("loaiVe") != null ? LoaiVe.valueOf(rs.getString("loaiVe")) : null, rs.getBoolean("trangThaiVe"), hd, kh, gaDi, gaDen);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(Ve entity) {
        String sql = "INSERT INTO Ve (maVe, maGhe, ngayMua, loaiVe, trangThaiVe, maHoaDon, maKH, thoiGianKhoiHanhGaDi, thoiGianKhoiHanhGaDen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaVe());
            ps.setString(2, entity.getGhe() != null ? entity.getGhe().getMaGhe() : null);
            ps.setTimestamp(3, entity.getNgayMua() != null ? java.sql.Timestamp.valueOf(entity.getNgayMua()) : null);
            ps.setString(4, entity.getLoaiVe() != null ? entity.getLoaiVe().name() : null);
            ps.setBoolean(5, entity.getTrangThaiVe());
            ps.setString(6, entity.getHoaDon() != null ? entity.getHoaDon().getMaHoaDon() : null);
            ps.setString(7, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setTimestamp(8, entity.getGaDi() != null && entity.getGaDi().getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getGaDi().getThoiGianKhoiHanh()) : null);
            ps.setTimestamp(9, entity.getGaDen() != null && entity.getGaDen().getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getGaDen().getThoiGianKhoiHanh()) : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Ve entity) {
        String sql = "UPDATE Ve SET maGhe = ?, ngayMua = ?, loaiVe = ?, trangThaiVe = ?, maHoaDon = ?, maKH = ?, thoiGianKhoiHanhGaDi = ?, thoiGianKhoiHanhGaDen = ? WHERE maVe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getGhe() != null ? entity.getGhe().getMaGhe() : null);
            ps.setTimestamp(2, entity.getNgayMua() != null ? java.sql.Timestamp.valueOf(entity.getNgayMua()) : null);
            ps.setString(3, entity.getLoaiVe() != null ? entity.getLoaiVe().name() : null);
            ps.setBoolean(4, entity.getTrangThaiVe());
            ps.setString(5, entity.getHoaDon() != null ? entity.getHoaDon().getMaHoaDon() : null);
            ps.setString(6, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setTimestamp(7, entity.getGaDi() != null && entity.getGaDi().getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getGaDi().getThoiGianKhoiHanh()) : null);
            ps.setTimestamp(8, entity.getGaDen() != null && entity.getGaDen().getThoiGianKhoiHanh() != null ? java.sql.Timestamp.valueOf(entity.getGaDen().getThoiGianKhoiHanh()) : null);
            ps.setString(9, entity.getMaVe());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Ve WHERE maVe = ?";
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