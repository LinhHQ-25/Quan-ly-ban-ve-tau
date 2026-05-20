package util;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class MaTuDong {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DDMMYY = DateTimeFormatter.ofPattern("ddMMyy");
    private static final DateTimeFormatter MMYY = DateTimeFormatter.ofPattern("MMyy");

    private MaTuDong() {
    }

    public static String taoMaKhachHang(Connection con) throws Exception {
        return taoMaKhongTrung(con, "SELECT 1 FROM KhachHang WHERE maKH = ?", "KH", 6);
    }

    public static String taoMaHoaDon(Connection con, LocalDate ngayLap) throws Exception {
        return taoMaKhongTrung(con, "SELECT 1 FROM HoaDon WHERE maHoaDon = ?", "HD" + ngayLap.format(DDMMYY) + "-", 4);
    }

    public static String taoMaVe(Connection con) throws Exception {
        return taoMaKhongTrung(con, "SELECT 1 FROM Ve WHERE maVe = ?", "", 9);
    }

    public static String taoMaDon(Connection con, LocalDate ngayLap) throws Exception {
        return taoMaKhongTrung(con, "SELECT 1 FROM DonDoiTraVe WHERE maDon = ?", "DT-" + ngayLap.format(MMYY) + "-", 4);
    }

    private static String taoMaKhongTrung(Connection con, String sqlKiemTra, String prefix, int soKyTu) throws Exception {
        while (true) {
            String ma = prefix + randomPart(soKyTu);
            try (PreparedStatement ps = con.prepareStatement(sqlKiemTra)) {
                ps.setString(1, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return ma;
                }
            }
        }
    }

    private static String randomPart(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
