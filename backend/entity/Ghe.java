package entity;

public class Ghe {
    private String maGhe;
    private String soGhe;
    private LoaiGhe loaiGhe;
    private ToaTau toaTau;

    private TrangThaiGhe trangThai;

    public Ghe() {
        this.trangThai = TrangThaiGhe.HOAT_DONG;
    }

    public Ghe(String maGhe, String soGhe, LoaiGhe loaiGhe, ToaTau toaTau) {
        this(maGhe, soGhe, loaiGhe, toaTau, TrangThaiGhe.HOAT_DONG);
    }

    public Ghe(String maGhe, String soGhe, LoaiGhe loaiGhe, ToaTau toaTau, TrangThaiGhe trangThai) {
        this.maGhe = maGhe;
        this.soGhe = soGhe;
        this.loaiGhe = loaiGhe;
        this.toaTau = toaTau;
        this.trangThai = trangThai != null ? trangThai : TrangThaiGhe.HOAT_DONG;
    }

    public String getMaGhe() {
        return maGhe;
    }

    public void setMaGhe(String maGhe) {
        this.maGhe = maGhe;
    }

    public String getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(String soGhe) {
        this.soGhe = soGhe;
    }

    public LoaiGhe getLoaiGhe() {
        return loaiGhe;
    }

    public void setLoaiGhe(LoaiGhe loaiGhe) {
        this.loaiGhe = loaiGhe;
    }
    public ToaTau getToaTau() {
        return toaTau;
    }

    public void setToaTau(ToaTau toaTau) {
        this.toaTau = toaTau;
    }

    public TrangThaiGhe getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiGhe trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return soGhe;
    }
}