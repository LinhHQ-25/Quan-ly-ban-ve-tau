package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.KhuyenMai;
import entity.LoaiKhachHang;

public class KhuyenMaiDAO {

	// Lấy toàn bộ danh sách khuyến mãi
	public List<KhuyenMai> selectAll() {
		List<KhuyenMai> list = new ArrayList<>();
		String sql = "SELECT * FROM KhuyenMai ORDER BY maKhuyenMai ASC";
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(mapRow(rs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// Lấy một khuyến mãi theo mã — dùng cho dialog Sửa
	public KhuyenMai selectById(String maKhuyenMai) {
		String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, maKhuyenMai);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return mapRow(rs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Thêm mới — có thêm dieuKienToiThieu (param thứ 9)
	public boolean insert(KhuyenMai km) {
		String sql = "INSERT INTO KhuyenMai " + "(maKhuyenMai, tenKhuyenMai, trangThai, moTaChiTiet, "
				+ " tiLeGiamGia, loaiKhachHang, thoiGianBatDau, thoiGianKetThuc, dieuKienToiThieu) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, km.getMaKhuyenMai());
			ps.setNString(2, km.getTenKhuyenMai());
			ps.setBoolean(3, km.getTrangThai());
			ps.setNString(4, km.getMoTaChiTiet());
			ps.setDouble(5, km.getTiLeGiamGia());
			ps.setString(6, km.getLoaiKhachHang() != null ? km.getLoaiKhachHang().name() : null);
			ps.setTimestamp(7, km.getThoiGianBatDau() != null ? Timestamp.valueOf(km.getThoiGianBatDau()) : null);
			ps.setTimestamp(8, km.getThoiGianKetThuc() != null ? Timestamp.valueOf(km.getThoiGianKetThuc()) : null);
			ps.setLong(9, km.getDieuKienToiThieu());
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Cập nhật — có thêm dieuKienToiThieu
	public boolean update(KhuyenMai km) {
		String sql = "UPDATE KhuyenMai " + "SET tenKhuyenMai=?, trangThai=?, moTaChiTiet=?, tiLeGiamGia=?, "
				+ "    loaiKhachHang=?, thoiGianBatDau=?, thoiGianKetThuc=?, dieuKienToiThieu=? "
				+ "WHERE maKhuyenMai=?";
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setNString(1, km.getTenKhuyenMai());
			ps.setBoolean(2, km.getTrangThai());
			ps.setNString(3, km.getMoTaChiTiet());
			ps.setDouble(4, km.getTiLeGiamGia());
			ps.setString(5, km.getLoaiKhachHang() != null ? km.getLoaiKhachHang().name() : null);
			ps.setTimestamp(6, km.getThoiGianBatDau() != null ? Timestamp.valueOf(km.getThoiGianBatDau()) : null);
			ps.setTimestamp(7, km.getThoiGianKetThuc() != null ? Timestamp.valueOf(km.getThoiGianKetThuc()) : null);
			ps.setLong(8, km.getDieuKienToiThieu());
			ps.setString(9, km.getMaKhuyenMai());
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Xóa — giữ nguyên
	public boolean delete(String id) {
		String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, id);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// mapRow — đọc thêm dieuKienToiThieu, fallback 0 nếu cột chưa có
	private KhuyenMai mapRow(ResultSet rs) throws SQLException {
		String loaiStr = rs.getString("loaiKhachHang");
		long dieuKien = 0;
		try {
			dieuKien = rs.getLong("dieuKienToiThieu");
		} catch (SQLException ignored) {
		}

		KhuyenMai km = new KhuyenMai(rs.getString("maKhuyenMai"), rs.getString("tenKhuyenMai"),
				rs.getBoolean("trangThai"), rs.getString("moTaChiTiet"), rs.getDouble("tiLeGiamGia"),
				(loaiStr != null && !loaiStr.isEmpty()) ? LoaiKhachHang.valueOf(loaiStr) : null,
				rs.getTimestamp("thoiGianBatDau") != null ? rs.getTimestamp("thoiGianBatDau").toLocalDateTime() : null,
				rs.getTimestamp("thoiGianKetThuc") != null ? rs.getTimestamp("thoiGianKetThuc").toLocalDateTime()
						: null);
		km.setDieuKienToiThieu(dieuKien);
		return km;
	}
}