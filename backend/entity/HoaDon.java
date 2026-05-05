package entity;

import java.time.LocalDateTime;

public class HoaDon {
    private String maHoaDon;
    private LocalDateTime ngayLapHD;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private double tienNhan;
    private PhuongThucThanhToan phuongThucThanhToan;

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, LocalDateTime ngayLapHD, NhanVien nhanVien, KhachHang khachHang, double tienNhan, PhuongThucThanhToan phuongThucThanhToan) {
        this.maHoaDon = maHoaDon;
        this.ngayLapHD = ngayLapHD;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.tienNhan = tienNhan;
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public LocalDateTime getNgayLapHD() {
        return ngayLapHD;
    }

    public void setNgayLapHD(LocalDateTime ngayLapHD) {
        this.ngayLapHD = ngayLapHD;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public double getTienNhan() {
        return tienNhan;
    }

    public void setTienNhan(double tienNhan) {
        this.tienNhan = tienNhan;
    }

    public PhuongThucThanhToan getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(PhuongThucThanhToan phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public double tinhTongTien() {
        return 0;
    }

    public double tinhTienThua() {
        return tienNhan - tinhTongTien();
    }

    @Override
    public String toString() {
        return maHoaDon;
    }
}