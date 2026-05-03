package entity_traCuu;

import java.util.Date;

public class ChuyenTau {
    private String maChuyen;
    private Tau tau; // Tham chiếu đến Tàu để lấy Tên tàu
    private String gaDi;
    private String gaDen;
    private Date thoiGianKhoiHanh;
    private Date thoiGianDen;
    private int soGheTrong;
    private String loaiToa;
    private String tienIch;
    private String trangThai;

    public ChuyenTau() {
    }

    public ChuyenTau(String maChuyen, Tau tau, String gaDi, String gaDen, Date thoiGianKhoiHanh, Date thoiGianDen,
            int soGheTrong, String loaiToa, String tienIch, String trangThai) {
        this.maChuyen = maChuyen;
        this.tau = tau;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.thoiGianDen = thoiGianDen;
        this.soGheTrong = soGheTrong;
        this.loaiToa = loaiToa;
        this.tienIch = tienIch;
        this.trangThai = trangThai;
    }

    public String getMaChuyen() { return maChuyen; }
    public void setMaChuyen(String maChuyen) { this.maChuyen = maChuyen; }

    public Tau getTau() { return tau; }
    public void setTau(Tau tau) { this.tau = tau; }

    public String getGaDi() { return gaDi; }
    public void setGaDi(String gaDi) { this.gaDi = gaDi; }

    public String getGaDen() { return gaDen; }
    public void setGaDen(String gaDen) { this.gaDen = gaDen; }

    public Date getThoiGianKhoiHanh() { return thoiGianKhoiHanh; }
    public void setThoiGianKhoiHanh(Date thoiGianKhoiHanh) { this.thoiGianKhoiHanh = thoiGianKhoiHanh; }

    public Date getThoiGianDen() { return thoiGianDen; }
    public void setThoiGianDen(Date thoiGianDen) { this.thoiGianDen = thoiGianDen; }

    public int getSoGheTrong() { return soGheTrong; }
    public void setSoGheTrong(int soGheTrong) { this.soGheTrong = soGheTrong; }

    public String getLoaiToa() { return loaiToa; }
    public void setLoaiToa(String loaiToa) { this.loaiToa = loaiToa; }

    public String getTienIch() { return tienIch; }
    public void setTienIch(String tienIch) { this.tienIch = tienIch; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
