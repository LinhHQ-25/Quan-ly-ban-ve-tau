package entity;

public class Ga {
    private String maGa;
    private String tenGa;
    private String diaChi;
    private String tinhThanh;
    private double heSoCuLy; // Thêm mới

    public Ga() {}

    // Constructor cũ — giữ nguyên để không break code khác
    public Ga(String maGa, String tenGa, String diaChi, String tinhThanh) {
        this.maGa = maGa;
        this.tenGa = tenGa;
        this.diaChi = diaChi;
        this.tinhThanh = tinhThanh;
    }

    // Constructor mới cho GaDAO.mapRow()
    public Ga(String maGa, String tenGa, String diaChi, String tinhThanh, double heSoCuLy) {
        this.maGa = maGa;
        this.tenGa = tenGa;
        this.diaChi = diaChi;
        this.tinhThanh = tinhThanh;
        this.heSoCuLy = heSoCuLy;
    }

    public String getMaGa() { return maGa; }
    public void setMaGa(String maGa) { this.maGa = maGa; }

    public String getTenGa() { return tenGa; }
    public void setTenGa(String tenGa) { this.tenGa = tenGa; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getTinhThanh() { return tinhThanh; }
    public void setTinhThanh(String tinhThanh) { this.tinhThanh = tinhThanh; }

    public double getHeSoCuLy() { return heSoCuLy; }
    public void setHeSoCuLy(double heSoCuLy) { this.heSoCuLy = heSoCuLy; }

    @Override
    public String toString() { return tenGa; }
}