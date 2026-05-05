package entity;

public class Ghe {
    private String maGhe;
    private String soGhe;
    private LoaiGhe loaiGhe;
    private ToaTau toaTau;

    public Ghe() {
    }

    public Ghe(String maGhe, String soGhe, LoaiGhe loaiGhe, ToaTau toaTau) {
        this.maGhe = maGhe;
        this.soGhe = soGhe;
        this.loaiGhe = loaiGhe;
        this.toaTau = toaTau;
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

    @Override
    public String toString() {
        return soGhe;
    }
}