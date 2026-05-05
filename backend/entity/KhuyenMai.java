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

    public KhuyenMai() {
    }

    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, Boolean trangThai, String moTaChiTiet, double tiLeGiamGia, LoaiKhachHang loaiKhachHang, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.trangThai = trangThai;
        this.moTaChiTiet = moTaChiTiet;
        this.tiLeGiamGia = tiLeGiamGia;
        this.loaiKhachHang = loaiKhachHang;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public String getMoTaChiTiet() {
        return moTaChiTiet;
    }

    public void setMoTaChiTiet(String moTaChiTiet) {
        this.moTaChiTiet = moTaChiTiet;
    }

    public double getTiLeGiamGia() {
        return tiLeGiamGia;
    }

    public void setTiLeGiamGia(double tiLeGiamGia) {
        this.tiLeGiamGia = tiLeGiamGia;
    }

    public LoaiKhachHang getLoaiKhachHang() {
        return loaiKhachHang;
    }

    public void setLoaiKhachHang(LoaiKhachHang loaiKhachHang) {
        this.loaiKhachHang = loaiKhachHang;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    @Override
    public String toString() {
        return tenKhuyenMai;
    }
}