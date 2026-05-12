package entity;

public enum LoaiVe {
    MOT_CHIEU("Một chiều"),
    KHU_HOI("Khứ hồi");

    private final String moTa;

    LoaiVe(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }

    @Override
    public String toString() {
        return moTa;
    }

    public static LoaiVe fromString(String text) {
        for (LoaiVe b : LoaiVe.values()) {
            if (b.moTa.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}