package entity_traCuu;

public class Tau {
    private String maTau;
    private String tenTau;
    private int soToa;
    private int namSanXuat;
    private String trangThai; // Đang hoạt động, Bảo trì
    private String ghiChu;

    public Tau() {
    }

    public Tau(String maTau, String tenTau, int soToa, int namSanXuat, String trangThai, String ghiChu) {
        this.maTau = maTau;
        this.tenTau = tenTau;
        this.soToa = soToa;
        this.namSanXuat = namSanXuat;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaTau() { return maTau; }
    public void setMaTau(String maTau) { this.maTau = maTau; }

    public String getTenTau() { return tenTau; }
    public void setTenTau(String tenTau) { this.tenTau = tenTau; }

    public int getSoToa() { return soToa; }
    public void setSoToa(int soToa) { this.soToa = soToa; }

    public int getNamSanXuat() { return namSanXuat; }
    public void setNamSanXuat(int namSanXuat) { this.namSanXuat = namSanXuat; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
