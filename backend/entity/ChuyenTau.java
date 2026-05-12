package entity;

public class ChuyenTau {
    private String maChuyenTau;
    private String ghiChu;
    private Tau tau;
    private TrangThaiChuyenTau trangThai;

    public ChuyenTau() {
    }

    public ChuyenTau(String maChuyenTau, String ghiChu, Tau tau, TrangThaiChuyenTau trangThai) {
        this.maChuyenTau = maChuyenTau;
        this.ghiChu = ghiChu;
        this.tau = tau;
        this.trangThai = trangThai;
    }

    public String getMaChuyenTau() {
        return maChuyenTau;
    }

    public void setMaChuyenTau(String maChuyenTau) {
        this.maChuyenTau = maChuyenTau;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Tau getTau() {
        return tau;
    }

    public void setTau(Tau tau) {
        this.tau = tau;
    }

    public TrangThaiChuyenTau getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiChuyenTau trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return maChuyenTau;
    }
}