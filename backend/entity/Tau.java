package entity;

public class Tau {
    private String maTau;
    private String tenTau;
    private String trangThai;
    private String ghiChu;

    public Tau() {
    }

    public Tau(String maTau, String tenTau, String trangThai, String ghiChu) {
        this.maTau = maTau;
        this.tenTau = tenTau;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaTau() {
        return maTau;
    }

    public void setMaTau(String maTau) {
        this.maTau = maTau;
    }

    public String getTenTau() {
        return tenTau;
    }

    public void setTenTau(String tenTau) {
        this.tenTau = tenTau;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return tenTau;
    }
}