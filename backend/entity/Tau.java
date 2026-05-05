package entity;

public class Tau {
    private String maTau;
    private String tenTau;
    private int soLuongToa;

    public Tau() {
    }

    public Tau(String maTau, String tenTau, int soLuongToa) {
        this.maTau = maTau;
        this.tenTau = tenTau;
        this.soLuongToa = soLuongToa;
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

    public int getSoLuongToa() {
        return soLuongToa;
    }

    public void setSoLuongToa(int soLuongToa) {
        this.soLuongToa = soLuongToa;
    }

    @Override
    public String toString() {
        return tenTau;
    }
}