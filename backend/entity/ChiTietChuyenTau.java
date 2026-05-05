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

    public ChiTietChuyenTau(LocalDateTime thoiGianKhoiHanh, LocalDateTime thoiGianDuKien, ChuyenTau chuyenTau, Ga gaDen, Ga gaDi) {
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.thoiGianDuKien = thoiGianDuKien;
        this.chuyenTau = chuyenTau;
        this.gaDen = gaDen;
        this.gaDi = gaDi;
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

    @Override
    public String toString() {
        return chuyenTau.getMaChuyen() + " (" + gaDi.getTenGa() + " - " + gaDen.getTenGa() + ")";
    }
}