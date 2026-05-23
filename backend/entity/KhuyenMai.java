package entity;

import java.time.LocalDateTime;

public class KhuyenMai {
	private String maKhuyenMai;
	private String tenKhuyenMai;
	private Boolean trangThai;
	private String moTaChiTiet;
	private double tiLeGiamGia;
	private LoaiKhachHang loaiKhachHang;
	private LocalDateTime thoiGianBatDau;
	private LocalDateTime thoiGianKetThuc;
	private long dieuKienToiThieu; // 0 = không có điều kiện tổng tiền

	public KhuyenMai() {
	}

	/** Constructor cũ — giữ nguyên để không break code hiện tại */
	public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, Boolean trangThai, String moTaChiTiet, double tiLeGiamGia,
			LoaiKhachHang loaiKhachHang, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc) {
		this.maKhuyenMai = maKhuyenMai;
		this.tenKhuyenMai = tenKhuyenMai;
		this.trangThai = trangThai;
		this.moTaChiTiet = moTaChiTiet;
		this.tiLeGiamGia = tiLeGiamGia;
		this.loaiKhachHang = loaiKhachHang;
		this.thoiGianBatDau = thoiGianBatDau;
		this.thoiGianKetThuc = thoiGianKetThuc;
		this.dieuKienToiThieu = 0;
	}

	/** Constructor mới — có thêm dieuKienToiThieu */
	public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, Boolean trangThai, String moTaChiTiet, double tiLeGiamGia,
			LoaiKhachHang loaiKhachHang, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
			long dieuKienToiThieu) {
		this.maKhuyenMai = maKhuyenMai;
		this.tenKhuyenMai = tenKhuyenMai;
		this.trangThai = trangThai;
		this.moTaChiTiet = moTaChiTiet;
		this.tiLeGiamGia = tiLeGiamGia;
		this.loaiKhachHang = loaiKhachHang;
		this.thoiGianBatDau = thoiGianBatDau;
		this.thoiGianKetThuc = thoiGianKetThuc;
		this.dieuKienToiThieu = dieuKienToiThieu;
	}

	// ── Getters / Setters ──────────────────────────────────────────────────────

	public String getMaKhuyenMai() {
		return maKhuyenMai;
	}

	public void setMaKhuyenMai(String v) {
		this.maKhuyenMai = v;
	}

	public String getTenKhuyenMai() {
		return tenKhuyenMai;
	}

	public void setTenKhuyenMai(String v) {
		this.tenKhuyenMai = v;
	}

	public Boolean getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(Boolean v) {
		this.trangThai = v;
	}

	public String getMoTaChiTiet() {
		return moTaChiTiet;
	}

	public void setMoTaChiTiet(String v) {
		this.moTaChiTiet = v;
	}

	public double getTiLeGiamGia() {
		return tiLeGiamGia;
	}

	public void setTiLeGiamGia(double v) {
		this.tiLeGiamGia = v;
	}

	public LoaiKhachHang getLoaiKhachHang() {
		return loaiKhachHang;
	}

	public void setLoaiKhachHang(LoaiKhachHang v) {
		this.loaiKhachHang = v;
	}

	public LocalDateTime getThoiGianBatDau() {
		return thoiGianBatDau;
	}

	public void setThoiGianBatDau(LocalDateTime v) {
		this.thoiGianBatDau = v;
	}

	public LocalDateTime getThoiGianKetThuc() {
		return thoiGianKetThuc;
	}

	public void setThoiGianKetThuc(LocalDateTime v) {
		this.thoiGianKetThuc = v;
	}

	/** Điều kiện tổng tiền vé tối thiểu (VNĐ). 0 = không có điều kiện. */
	public long getDieuKienToiThieu() {
		return dieuKienToiThieu;
	}

	public void setDieuKienToiThieu(long v) {
		this.dieuKienToiThieu = v;
	}

	@Override
	public String toString() {
		return tenKhuyenMai;
	}
}