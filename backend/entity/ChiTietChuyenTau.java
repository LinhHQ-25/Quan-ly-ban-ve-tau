package entity;

import java.time.LocalDateTime;

public class ChiTietChuyenTau {
    private LocalDateTime thoiGianKhoiHanh;
    private LocalDateTime thoiGianDuKien;
    private ChuyenTau chuyenTau;
    private Ga gaDen;
    private Ga gaDi;

    public ChiTietChuyenTau() {
    }

    public ChiTietChuyenTau(LocalDateTime thoiGianKhoiHanh, LocalDateTime thoiGianDuKien, ChuyenTau chuyenTau, Ga gaDi, Ga gaDen) {
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.thoiGianDuKien = thoiGianDuKien;
        this.chuyenTau = chuyenTau;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
    }

    public LocalDateTime getThoiGianKhoiHanh() {
        return thoiGianKhoiHanh;
    }

    public void setThoiGianKhoiHanh(LocalDateTime thoiGianKhoiHanh) {
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
    }

    public LocalDateTime getThoiGianDuKien() {
        return thoiGianDuKien;
    }

    public void setThoiGianDuKien(LocalDateTime thoiGianDuKien) {
        this.thoiGianDuKien = thoiGianDuKien;
    }

    public ChuyenTau getChuyenTau() {
        return chuyenTau;
    }

    public void setChuyenTau(ChuyenTau chuyenTau) {
        this.chuyenTau = chuyenTau;
    }
    
    // Helper để lấy mã Chuyến tàu
    public String getMaChuyenTau() {
        return chuyenTau != null ? chuyenTau.getMaChuyenTau() : null;
    }

    public Ga getGaDen() {
        return gaDen;
    }

    public void setGaDen(Ga gaDen) {
        this.gaDen = gaDen;
    }

    public Ga getGaDi() {
        return gaDi;
    }

    public void setGaDi(Ga gaDi) {
        this.gaDi = gaDi;
    }
    
    // Helper để lấy mã ga
    public String getMaGaDi() {
        return gaDi != null ? gaDi.getMaGa() : null;
    }
    
    public String getMaGaDen() {
        return gaDen != null ? gaDen.getMaGa() : null;
    }
}