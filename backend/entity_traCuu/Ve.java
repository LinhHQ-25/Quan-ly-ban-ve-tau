package entity_traCuu;

import java.time.LocalDateTime;

public class Ve {
    public enum LoaiVe {
        MOT_CHIEU,
        KHU_HOI
    }

    private String maVe;
    private Ghe ghe;
    private LocalDateTime ngayMua;
    private LoaiVe loaiVe;
    private Boolean trangThaiVe;
    private HoaDon hoaDon;
    private KhachHang khachHang;
    private ChiTietChuyenTau gaDi;
    private ChiTietChuyenTau gaDen;

    public Ve() {
    }

    public Ve(String maVe, Ghe ghe, LocalDateTime ngayMua, LoaiVe loaiVe, Boolean trangThaiVe, HoaDon hoaDon,
            KhachHang khachHang, ChiTietChuyenTau gaDi, ChiTietChuyenTau gaDen) {
        this.maVe = maVe;
        this.ghe = ghe;
        this.ngayMua = ngayMua;
        this.loaiVe = loaiVe;
        this.trangThaiVe = trangThaiVe;
        this.hoaDon = hoaDon;
        this.khachHang = khachHang;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
    }

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public Ghe getGhe() { return ghe; }
    public void setGhe(Ghe ghe) { this.ghe = ghe; }

    public LocalDateTime getNgayMua() { return ngayMua; }
    public void setNgayMua(LocalDateTime ngayMua) { this.ngayMua = ngayMua; }

    public LoaiVe getLoaiVe() { return loaiVe; }
    public void setLoaiVe(LoaiVe loaiVe) { this.loaiVe = loaiVe; }

    public Boolean getTrangThaiVe() { return trangThaiVe; }
    public void setTrangThaiVe(Boolean trangThaiVe) { this.trangThaiVe = trangThaiVe; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public ChiTietChuyenTau getGaDi() { return gaDi; }
    public void setGaDi(ChiTietChuyenTau gaDi) { this.gaDi = gaDi; }

    public ChiTietChuyenTau getGaDen() { return gaDen; }
    public void setGaDen(ChiTietChuyenTau gaDen) { this.gaDen = gaDen; }

    public double tinhGiaVe() {
        // TODO: Logic tính giá vé
        return 0.0;
    }

    @Override
    public String toString() {
        return "Ve [maVe=" + maVe + ", ngayMua=" + ngayMua + ", loaiVe=" + loaiVe + ", trangThaiVe=" + trangThaiVe + "]";
    }
}
