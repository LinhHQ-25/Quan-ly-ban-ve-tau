package entity;

import java.time.LocalDate;

public class NhanVien {
    private String maNV;
    private String hoTenNV;
    private String email;
    private LocalDate ngaySinh;
    private String soDT;
    private Boolean gioiTinh;
    private String diaChi;
    private String soCCCD;
    private LoaiNhanVien loaiNV;

    public NhanVien() {
    }

    public NhanVien(String maNV, String hoTenNV, String email, LocalDate ngaySinh, String soDT, Boolean gioiTinh, String diaChi, String soCCCD, LoaiNhanVien loaiNV) {
        this.maNV = maNV;
        this.hoTenNV = hoTenNV;
        this.email = email;
        this.ngaySinh = ngaySinh;
        this.soDT = soDT;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi;
        this.soCCCD = soCCCD;
        this.loaiNV = loaiNV;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTenNV() {
        return hoTenNV;
    }

    public void setHoTenNV(String hoTenNV) {
        this.hoTenNV = hoTenNV;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getSoDT() {
        return soDT;
    }

    public void setSoDT(String soDT) {
        this.soDT = soDT;
    }

    public Boolean getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(Boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSoCCCD() {
        return soCCCD;
    }

    public void setSoCCCD(String soCCCD) {
        this.soCCCD = soCCCD;
    }

    public LoaiNhanVien getLoaiNV() {
        return loaiNV;
    }

    public void setLoaiNV(LoaiNhanVien loaiNV) {
        this.loaiNV = loaiNV;
    }

    @Override
    public String toString() {
        return hoTenNV;
    }
}