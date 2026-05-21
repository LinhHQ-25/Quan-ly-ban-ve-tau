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
    private double giaVe; // Thêm trường giaVe

    public Ve() {
    }

    public Ve(String maVe, Ghe ghe, LocalDateTime ngayMua, LoaiVe loaiVe, String trangThaiVe, HoaDon hoaDon, KhachHang khachHang, KhuyenMai khuyenMai, ChiTietChuyenTau chiTietChuyenTau, double giaVe) {
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

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public Ghe getGhe() { return ghe; }
    public void setGhe(Ghe ghe) { this.ghe = ghe; }
    
    // Helper để DAO lấy mã ghế
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
    
    // Helper để DAO lấy mã hóa đơn
    public String getMaHoaDon() {
        return hoaDon != null ? hoaDon.getMaHoaDon() : null;
    }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }

    public ChiTietChuyenTau getChiTietChuyenTau() { return chiTietChuyenTau; }
    public void setChiTietChuyenTau(ChiTietChuyenTau chiTietChuyenTau) { this.chiTietChuyenTau = chiTietChuyenTau; }
    
    // Helper để DAO lấy mã chuyến tàu
    public String getMaChuyenTau() {
        return chiTietChuyenTau != null ? chiTietChuyenTau.getMaChuyenTau() : null;
    }

    public double getGiaVe() { return giaVe; }
    public void setGiaVe(double giaVe) { this.giaVe = giaVe; }

    @Override
    public double tinhGiaVe() {
        return giaVe;
    }

    @Override
    public String toString() {
        return maVe;
    }
    
}