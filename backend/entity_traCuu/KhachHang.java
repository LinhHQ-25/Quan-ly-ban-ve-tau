package entity_traCuu;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

public class KhachHang {
    public enum LoaiKhachHang {
        DUOI_6_TUOI,
        TU_6_TOI_DUOI_10,
        TU_60_TRO_LEN,
        SINH_VIEN,
        NGUOI_LON
    }

    private String maKH;
    private String hoTenKH;
    private String cccd;
    private String sdt;
    private String email;
    private LocalDate namSinh;
    private Boolean laSinhVien;
    
    // Các trường dữ liệu khác lấy từ giao diện
    private Date ngayDangKy;
    private int diemTichLuy;
    private String nhomKhach; 
    private String trangThai; 

    public KhachHang() {
    }

    public KhachHang(String maKH, String hoTenKH, String cccd, String sdt, String email, LocalDate namSinh,
            Boolean laSinhVien, Date ngayDangKy, int diemTichLuy, String nhomKhach, String trangThai) {
        this.maKH = maKH;
        this.hoTenKH = hoTenKH;
        this.cccd = cccd;
        this.sdt = sdt;
        this.email = email;
        this.namSinh = namSinh;
        this.laSinhVien = laSinhVien;
        this.ngayDangKy = ngayDangKy;
        this.diemTichLuy = diemTichLuy;
        this.nhomKhach = nhomKhach;
        this.trangThai = trangThai;
    }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getHoTenKH() { return hoTenKH; }
    public void setHoTenKH(String hoTenKH) { this.hoTenKH = hoTenKH; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getNamSinh() { return namSinh; }
    public void setNamSinh(LocalDate namSinh) { this.namSinh = namSinh; }

    public Boolean getLaSinhVien() { return laSinhVien; }
    public void setLaSinhVien(Boolean laSinhVien) { this.laSinhVien = laSinhVien; }

    public Date getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(Date ngayDangKy) { this.ngayDangKy = ngayDangKy; }

    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    public String getNhomKhach() { return nhomKhach; }
    public void setNhomKhach(String nhomKhach) { this.nhomKhach = nhomKhach; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LoaiKhachHang xacDinhLoaiKhachHang() {
        if (namSinh == null) return LoaiKhachHang.NGUOI_LON;
        
        int age = Period.between(namSinh, LocalDate.now()).getYears();
        
        if (age < 6) {
            return LoaiKhachHang.DUOI_6_TUOI;
        } else if (age >= 6 && age < 10) {
            return LoaiKhachHang.TU_6_TOI_DUOI_10;
        } else if (age >= 60) {
            return LoaiKhachHang.TU_60_TRO_LEN;
        } else if (Boolean.TRUE.equals(laSinhVien)) {
            return LoaiKhachHang.SINH_VIEN;
        }
        
        return LoaiKhachHang.NGUOI_LON;
    }

    @Override
    public String toString() {
        return "KhachHang [maKH=" + maKH + ", hoTenKH=" + hoTenKH + ", cccd=" + cccd + ", sdt=" + sdt + ", email="
                + email + ", namSinh=" + namSinh + ", laSinhVien=" + laSinhVien + "]";
    }
}
