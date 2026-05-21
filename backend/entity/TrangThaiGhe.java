package entity;

public enum TrangThaiGhe {
    HOAT_DONG("Hoạt động"),
    BAO_TRI("Bảo trì");

    private final String moTa;

    TrangThaiGhe(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }

    @Override
    public String toString() {
        return moTa;
    }

    public static TrangThaiGhe tuMoTa(String moTa) {
        if (moTa == null) return HOAT_DONG;
        for (TrangThaiGhe status : values()) {
            if (status.moTa.equalsIgnoreCase(moTa) || status.name().equalsIgnoreCase(moTa)) {
                return status;
            }
        }
        return HOAT_DONG;
    }
}
