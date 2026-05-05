package entity;

import java.time.LocalDateTime;

public class DonDoiTraVe {
    private String maDon;
    private double tienBu;
    private LocalDateTime ngayLap;
    private double tienHoanTra;
    private LoaiDon loaiDon;
    private Ve ve;

    public DonDoiTraVe() {
    }

    public DonDoiTraVe(String maDon, double tienBu, LocalDateTime ngayLap, double tienHoanTra, LoaiDon loaiDon, Ve ve) {
        this.maDon = maDon;
        this.tienBu = tienBu;
        this.ngayLap = ngayLap;
        this.tienHoanTra = tienHoanTra;
        this.loaiDon = loaiDon;
        this.ve = ve;
    }

    public String getMaDon() {
        return maDon;
    }

    public void setMaDon(String maDon) {
        this.maDon = maDon;
    }

    public double getTienBu() {
        return tienBu;
    }

    public void setTienBu(double tienBu) {
        this.tienBu = tienBu;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public double getTienHoanTra() {
        return tienHoanTra;
    }

    public void setTienHoanTra(double tienHoanTra) {
        this.tienHoanTra = tienHoanTra;
    }

    public LoaiDon getLoaiDon() {
        return loaiDon;
    }

    public void setLoaiDon(LoaiDon loaiDon) {
        this.loaiDon = loaiDon;
    }

    public Ve getVe() {
        return ve;
    }

    public void setVe(Ve ve) {
        this.ve = ve;
    }

    @Override
    public String toString() {
        return maDon;
    }
}