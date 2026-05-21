package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;

public class ChuyenTauDAO {

    // =========================================================================
    // ─── THÀNH PHẦN NÂNG CẤP DÀNH CHO NHÀ QUẢN LÝ (THỐNG KÊ VĨ MÔ) ───
    // =========================================================================
    
    /**
     * Lấy danh sách hiệu suất khai thác của từng chuyến tàu (Số vé bán và doanh thu thu về)
     */
    public static List<Object[]> getThongKeHieuSuatChuyenTau(java.time.LocalDate tuNgay, java.time.LocalDate denNgay) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT v.maChuyenTau, " +
                     "       COUNT(v.maVe) AS soVeBan, " +
                     "       ISNULL(SUM(v.giaVe), 0) AS doanhThu " +
                     "FROM Ve v " +
                     "WHERE CAST(v.ngayMua AS DATE) BETWEEN ? AND ? AND v.trangThaiVe = N'Đã thanh toán' " +
                     "GROUP BY v.maChuyenTau " +
                     "ORDER BY doanhThu DESC";

        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("maChuyenTau"),
                        rs.getInt("soVeBan"),
                        rs.getDouble("doanhThu")
                    });
                }
            }
        }
        return list;
    }
}