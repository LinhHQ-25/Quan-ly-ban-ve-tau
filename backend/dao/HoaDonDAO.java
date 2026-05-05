package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhuongThucThanhToan;

public class HoaDonDAO implements DAO<HoaDon, String> {

    @Override
    public List<HoaDon> selectAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                list.add(new HoaDon(rs.getString("maHoaDon"), rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime() : null, nv, kh, rs.getDouble("tienNhan"), rs.getString("phuongThucThanhToan") != null ? PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan")) : null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public HoaDon selectById(String id) {
        String sql = "SELECT * FROM HoaDon WHERE maHoaDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    KhachHang kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    return new HoaDon(rs.getString("maHoaDon"), rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime() : null, nv, kh, rs.getDouble("tienNhan"), rs.getString("phuongThucThanhToan") != null ? PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan")) : null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(HoaDon entity) {
        String sql = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tienNhan, phuongThucThanhToan) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaHoaDon());
            ps.setTimestamp(2, entity.getNgayLapHD() != null ? java.sql.Timestamp.valueOf(entity.getNgayLapHD()) : null);
            ps.setString(3, entity.getNhanVien() != null ? entity.getNhanVien().getMaNV() : null);
            ps.setString(4, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setDouble(5, entity.getTienNhan());
            ps.setString(6, entity.getPhuongThucThanhToan() != null ? entity.getPhuongThucThanhToan().name() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(HoaDon entity) {
        String sql = "UPDATE HoaDon SET ngayLapHD = ?, maNV = ?, maKH = ?, tienNhan = ?, phuongThucThanhToan = ? WHERE maHoaDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, entity.getNgayLapHD() != null ? java.sql.Timestamp.valueOf(entity.getNgayLapHD()) : null);
            ps.setString(2, entity.getNhanVien() != null ? entity.getNhanVien().getMaNV() : null);
            ps.setString(3, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setDouble(4, entity.getTienNhan());
            ps.setString(5, entity.getPhuongThucThanhToan() != null ? entity.getPhuongThucThanhToan().name() : null);
            ps.setString(6, entity.getMaHoaDon());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM HoaDon WHERE maHoaDon = ?";
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