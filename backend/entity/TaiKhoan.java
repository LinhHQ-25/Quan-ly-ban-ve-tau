package entity;

import java.time.LocalDateTime;

public class TaiKhoan {
    private String maTaiKhoan;
    private String tenTaiKhoan;
    private String matKhau;
    private NhanVien nhanVien;
    private LocalDateTime ngayDangNhap;
    private LocalDateTime ngayDangXuat;

    public TaiKhoan() {
    }

    public TaiKhoan(String maTaiKhoan, String tenTaiKhoan, String matKhau, NhanVien nhanVien, LocalDateTime ngayDangNhap, LocalDateTime ngayDangXuat) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenTaiKhoan = tenTaiKhoan;
        this.matKhau = matKhau;
        this.nhanVien = nhanVien;
        this.ngayDangNhap = ngayDangNhap;
        this.ngayDangXuat = ngayDangXuat;
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getTenTaiKhoan() {
        return tenTaiKhoan;
    }

    public void setTenTaiKhoan(String tenTaiKhoan) {
        this.tenTaiKhoan = tenTaiKhoan;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public LocalDateTime getNgayDangNhap() {
        return ngayDangNhap;
    }

    public void setNgayDangNhap(LocalDateTime ngayDangNhap) {
        this.ngayDangNhap = ngayDangNhap;
    }

    public LocalDateTime getNgayDangXuat() {
        return ngayDangXuat;
    }

    public void setNgayDangXuat(LocalDateTime ngayDangXuat) {
        this.ngayDangXuat = ngayDangXuat;
    }

    @Override
    public String toString() {
        return tenTaiKhoan;
    }
}