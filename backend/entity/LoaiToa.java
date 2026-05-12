package entity;

public enum LoaiToa {
    TOA_THUONG("Toa thường"),
    TOA_VIP("Toa VIP");

    private final String moTa;

    LoaiToa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return moTa;
    }
}