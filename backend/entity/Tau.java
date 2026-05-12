package entity;

public class Tau {
    private String maTau;
    private String tenTau;
    private String trangThai;
    private String ghiChu;
    private int soToa;
    private int tongSoGhe;

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

    public int getSoToa() {
        return soToa;
    }

    public void setSoToa(int soToa) {
        this.soToa = soToa;
    }

    public int getTongSoGhe() {
        return tongSoGhe;
    }

    public void setTongSoGhe(int tongSoGhe) {
        this.tongSoGhe = tongSoGhe;
    }

    @Override
    public String toString() {
        return tenTau;
    }
}