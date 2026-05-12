package entity;

public enum TrangThaiChuyenTau {
    CHUAN_BI("Sẵn sàng"),
    DANG_CHAY("Đang chạy"),
    DA_DEN("Đã đến"),
    HUY("Đã hủy");

    private final String moTa;

    TrangThaiChuyenTau(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return moTa;
    }

    public static TrangThaiChuyenTau fromString(String text) {
        for (TrangThaiChuyenTau b : TrangThaiChuyenTau.values()) {
            if (b.moTa.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}