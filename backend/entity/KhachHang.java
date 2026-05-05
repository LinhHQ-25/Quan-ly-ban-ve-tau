package entity;

import java.time.LocalDate;

public class KhachHang {
    private String maKH;
    private String hoTenKH;
    private String cccd;
    private String sdt;
    private String email;
    private LocalDate namSinh;
    private Boolean laSinhVien;

    public KhachHang() {
    }

    public KhachHang(String maKH, String hoTenKH, String cccd, String sdt, String email, LocalDate namSinh, Boolean laSinhVien) {
        this.maKH = maKH;
        this.hoTenKH = hoTenKH;
        this.cccd = cccd;
        this.sdt = sdt;
        this.email = email;
        this.namSinh = namSinh;
        this.laSinhVien = laSinhVien;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getHoTenKH() {
        return hoTenKH;
    }

    public void setHoTenKH(String hoTenKH) {
        this.hoTenKH = hoTenKH;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getNamSinh() {
        return namSinh;
    }

    public void setNamSinh(LocalDate namSinh) {
        this.namSinh = namSinh;
    }

    public Boolean getLaSinhVien() {
        return laSinhVien;
    }

    public void setLaSinhVien(Boolean laSinhVien) {
        this.laSinhVien = laSinhVien;
    }

    public LoaiKhachHang xacDinhLoaiKhachHang() {
        if (namSinh == null) return LoaiKhachHang.NGUOI_LON;
        int age = LocalDate.now().getYear() - namSinh.getYear();
        if (age < 6) return LoaiKhachHang.DUOI_6_TUOI;
        if (age < 10) return LoaiKhachHang.TU_6_TOI_DUOI_10;
        if (age >= 60) return LoaiKhachHang.TU_60_TRO_LEN;
        if (Boolean.TRUE.equals(laSinhVien)) return LoaiKhachHang.SINH_VIEN;
        return LoaiKhachHang.NGUOI_LON;
    }

    @Override
    public String toString() {
        return hoTenKH;
    }
}