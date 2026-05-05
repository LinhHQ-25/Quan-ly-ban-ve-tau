package entity;

public class ToaTau {
    private String maToaTau;
    private int soToa;
    private int soLuongGhe;
    private LoaiToa loaiToa;
    private Tau tau;
    private double heSoLoaiToa;

    public ToaTau() {
    }

    public ToaTau(String maToaTau, int soToa, int soLuongGhe, LoaiToa loaiToa, Tau tau, double heSoLoaiToa) {
        this.maToaTau = maToaTau;
        this.soToa = soToa;
        this.soLuongGhe = soLuongGhe;
        this.loaiToa = loaiToa;
        this.tau = tau;
        this.heSoLoaiToa = heSoLoaiToa;
    }

    public String getMaToaTau() {
        return maToaTau;
    }

    public void setMaToaTau(String maToaTau) {
        this.maToaTau = maToaTau;
    }

    public int getSoToa() {
        return soToa;
    }

    public void setSoToa(int soToa) {
        this.soToa = soToa;
    }

    public int getSoLuongGhe() {
        return soLuongGhe;
    }

    public void setSoLuongGhe(int soLuongGhe) {
        this.soLuongGhe = soLuongGhe;
    }

    public LoaiToa getLoaiToa() {
        return loaiToa;
    }

    public void setLoaiToa(LoaiToa loaiToa) {
        this.loaiToa = loaiToa;
    }

    public Tau getTau() {
        return tau;
    }

    public void setTau(Tau tau) {
        this.tau = tau;
    }

    public double getHeSoLoaiToa() {
        return heSoLoaiToa;
    }

    public void setHeSoLoaiToa(double heSoLoaiToa) {
        this.heSoLoaiToa = heSoLoaiToa;
    }

    @Override
    public String toString() {
        return maToaTau;
    }
}