package entity;

public enum LoaiGhe {
    GHE_MEM("Ghế mềm"),
    GHE_CUNG("Ghế cứng"),
    GIUONG_NAM("Giường nằm");

    private final String moTa;

    LoaiGhe(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return moTa;
    }
}