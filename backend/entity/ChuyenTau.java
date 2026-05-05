package entity;

public class ChuyenTau {
    private String maChuyen;
    private String ghiChu;
    private Tau tau;
    private TrangThaiChuyenTau trangThai;

    public ChuyenTau() {
    }

    public ChuyenTau(String maChuyen, String ghiChu, Tau tau, TrangThaiChuyenTau trangThai) {
        this.maChuyen = maChuyen;
        this.ghiChu = ghiChu;
        this.tau = tau;
        this.trangThai = trangThai;
    }

    public String getMaChuyen() {
        return maChuyen;
    }

    public void setMaChuyen(String maChuyen) {
        this.maChuyen = maChuyen;
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
        return maChuyen;
    }
}