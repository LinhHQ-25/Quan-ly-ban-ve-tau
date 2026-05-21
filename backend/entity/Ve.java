package entity;

import java.time.LocalDateTime;

public class Ve implements TinhGiaVe {
    private String maVe;
    private Ghe ghe;
    private LocalDateTime ngayMua;
    private LoaiVe loaiVe;
    private String trangThaiVe;
    private HoaDon hoaDon;
    private KhachHang khachHang;
    private KhuyenMai khuyenMai;
    private ChiTietChuyenTau chiTietChuyenTau;
    private double giaVe;

    // Thêm 3 field mới để tính giá
    private double heSoCuLy    = 1.0;
    private double heSoLoaiToa = 1.0;
    private String loaiDoiTuong = "";

    public Ve() {}

    public Ve(String maVe, Ghe ghe, LocalDateTime ngayMua, LoaiVe loaiVe, String trangThaiVe,
              HoaDon hoaDon, KhachHang khachHang, KhuyenMai khuyenMai,
              ChiTietChuyenTau chiTietChuyenTau, double giaVe) {
        this.maVe = maVe;
        this.ghe = ghe;
        this.ngayMua = ngayMua;
        this.loaiVe = loaiVe;
        this.trangThaiVe = trangThaiVe;
        this.hoaDon = hoaDon;
        this.khachHang = khachHang;
        this.khuyenMai = khuyenMai;
        this.chiTietChuyenTau = chiTietChuyenTau;
        this.giaVe = giaVe;
    }

    // ── Getters/Setters cũ ──────────────────────────────────────
    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public Ghe getGhe() { return ghe; }
    public void setGhe(Ghe ghe) { this.ghe = ghe; }

    public String getMaGhe() {
        return ghe != null ? ghe.getMaGhe() : null;
    }

    public LocalDateTime getNgayMua() { return ngayMua; }
    public void setNgayMua(LocalDateTime ngayMua) { this.ngayMua = ngayMua; }

    public LoaiVe getLoaiVe() { return loaiVe; }
    public void setLoaiVe(LoaiVe loaiVe) { this.loaiVe = loaiVe; }

    public String getTrangThaiVe() { return trangThaiVe; }
    public void setTrangThaiVe(String trangThaiVe) { this.trangThaiVe = trangThaiVe; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public String getMaHoaDon() {
        return hoaDon != null ? hoaDon.getMaHoaDon() : null;
    }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }

    public ChiTietChuyenTau getChiTietChuyenTau() { return chiTietChuyenTau; }
    public void setChiTietChuyenTau(ChiTietChuyenTau chiTietChuyenTau) {
        this.chiTietChuyenTau = chiTietChuyenTau;
    }

    public String getMaChuyenTau() {
        return chiTietChuyenTau != null ? chiTietChuyenTau.getMaChuyenTau() : null;
    }

    public double getGiaVe() { return giaVe; }
    public void setGiaVe(double giaVe) { this.giaVe = giaVe; }

    // ── Setters mới cho tính giá ────────────────────────────────
    public void setHeSoCuLy(double heSoCuLy)       { this.heSoCuLy = heSoCuLy; }
    public void setHeSoLoaiToa(double heSoLoaiToa) { this.heSoLoaiToa = heSoLoaiToa; }
    public void setLoaiDoiTuong(String loaiDoiTuong) { this.loaiDoiTuong = loaiDoiTuong; }

    // ── Implement TinhGiaVe ─────────────────────────────────────
    @Override
    public double tinhGiaVe() {
        return giaVe;
    }

    @Override
    public double tinhGiaGoc() {
        return 300000 * heSoCuLy * heSoLoaiToa;
    }

    @Override
    public double layTyLeGiamDoiTuong() {
        if (loaiDoiTuong == null) return 0.0;
        if (loaiDoiTuong.contains("Sinh viên"))  return 0.08;
        if (loaiDoiTuong.contains("<6 tuổi"))    return 1.0;
        if (loaiDoiTuong.contains("Trẻ em"))     return 0.5;
        if (loaiDoiTuong.contains("cao tuổi"))   return 0.3;
        return 0.0;
    }

    @Override
    public String toString() { return maVe; }
}