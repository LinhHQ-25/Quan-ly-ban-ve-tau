package dao;

import connect_DB.Connect_DB;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChuyenTauDAO {

    /**
     * Lấy thông tin chuyến tàu để in lên vé PDF.
     */
    public Object[] getThongTinChuyenChoVe(String maChuyenTau) {
        String sql = "SELECT t.tenTau, ct.thoiGianKhoiHanh, "
                   + "g1.tenGa AS GaDi, g2.tenGa AS GaDen "
                   + "FROM ChuyenTau c "
                   + "JOIN Tau t ON c.maTau = t.maTau "
                   + "JOIN ChiTietChuyenTau ct ON c.maChuyenTau = ct.maChuyenTau "
                   + "JOIN Ga g1 ON ct.maGaDi = g1.maGa "
                   + "JOIN Ga g2 ON ct.maGaDen = g2.maGa "
                   + "WHERE c.maChuyenTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String ngayDi = "", gioDi = "";
                    Timestamp ts = rs.getTimestamp("thoiGianKhoiHanh");
                    if (ts != null) {
                        ngayDi = new SimpleDateFormat("dd/MM/yyyy").format(ts);
                        gioDi  = new SimpleDateFormat("HH:mm").format(ts);
                    }
                    return new Object[]{
                        rs.getString("tenTau"),
                        rs.getString("GaDi"),
                        rs.getString("GaDen"),
                        ngayDi, gioDi
                    };
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new Object[]{ "", "", "", "", "" };
    }

    /**
     * Lưu điều động tàu mới.
     */
    public boolean saveDieuDong(String maChuyenTau, String maTau,
                                 String maGaDi, String maGaDen,
                                 Timestamp tsDi, Timestamp tsDen) {
        Connection con = null;
        try {
            con = Connect_DB.getInstance().getConnection();
            con.setAutoCommit(false);

            String sql1 = "INSERT INTO ChuyenTau(maChuyenTau, ghiChu, maTau, trangThai) "
                        + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql1)) {
                ps.setString(1, maChuyenTau);
                ps.setString(2, "Điều động mới");
                ps.setString(3, maTau);
                ps.setString(4, "CHUAN_BI");
                ps.executeUpdate();
            }

            String sql2 = "INSERT INTO ChiTietChuyenTau"
                        + "(maChuyenTau, thoiGianKhoiHanh, thoiGianDuKien, maGaDi, maGaDen) "
                        + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql2)) {
                ps.setString(1, maChuyenTau);
                ps.setTimestamp(2, tsDi);
                ps.setTimestamp(3, tsDen);
                ps.setString(4, maGaDi);
                ps.setString(5, maGaDen);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) try { con.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    /**
     * Xóa chuyến tàu.
     */
    public boolean deleteChuyenTau(String maChuyenTau) {
        Connection con = null;
        try {
            con = Connect_DB.getInstance().getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ChiTietChuyenTau WHERE maChuyenTau = ?")) {
                ps.setString(1, maChuyenTau);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ChuyenTau WHERE maChuyenTau = ?")) {
                ps.setString(1, maChuyenTau);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) try { con.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    /**
     * Lấy dữ liệu 1 chuyến để load lên form Cập nhật.
     */
    public Object[] getChuyenTauForUpdate(String maChuyenTau) {
        String sql = "SELECT ct.maTau, t.tenTau, dt.maGaDi, dt.maGaDen, "
                   + "dt.thoiGianKhoiHanh, dt.thoiGianDuKien, ct.trangThai "
                   + "FROM ChuyenTau ct "
                   + "JOIN ChiTietChuyenTau dt ON ct.maChuyenTau = dt.maChuyenTau "
                   + "JOIN Tau t ON ct.maTau = t.maTau "
                   + "WHERE ct.maChuyenTau = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Object[]{
                        rs.getString("maTau"),
                        rs.getString("tenTau"),
                        rs.getString("maGaDi"),
                        rs.getString("maGaDen"),
                        rs.getTimestamp("thoiGianKhoiHanh"),
                        rs.getTimestamp("thoiGianDuKien"),
                        rs.getString("trangThai")
                    };
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Cập nhật chuyến tàu.
     */
    public boolean updateChuyenTau(String maChuyenTau, String maGaDi, String maGaDen,
                                    Timestamp tsDi, Timestamp tsDen, String trangThai) {
        Connection con = null;
        try {
            con = Connect_DB.getInstance().getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ChuyenTau SET trangThai = ? WHERE maChuyenTau = ?")) {
                ps.setString(1, trangThai);
                ps.setString(2, maChuyenTau);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ChiTietChuyenTau "
                  + "SET thoiGianKhoiHanh=?, thoiGianDuKien=?, maGaDi=?, maGaDen=? "
                  + "WHERE maChuyenTau=?")) {
                ps.setTimestamp(1, tsDi);
                ps.setTimestamp(2, tsDen);
                ps.setString(3, maGaDi);
                ps.setString(4, maGaDen);
                ps.setString(5, maChuyenTau);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            if (con != null) try { con.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    /**
     * Tìm kiếm danh sách chuyến tàu cho bảng.
     */
    public List<Object[]> searchChuyenTau(String filterGaDi, String filterGaDen,
                                           String filterTau, java.sql.Date ngayDi) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT ct.maChuyenTau, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, "
                   + "dt.thoiGianKhoiHanh, dt.thoiGianDuKien, t.tenTau, ct.trangThai "
                   + "FROM ChuyenTau ct "
                   + "JOIN ChiTietChuyenTau dt ON ct.maChuyenTau = dt.maChuyenTau "
                   + "JOIN Ga gDi  ON dt.maGaDi  = gDi.maGa "
                   + "JOIN Ga gDen ON dt.maGaDen = gDen.maGa "
                   + "JOIN Tau t   ON ct.maTau   = t.maTau "
                   + "WHERE gDi.tenGa  LIKE ? "
                   + "  AND gDen.tenGa LIKE ? "
                   + "  AND t.tenTau   LIKE ? "
                   + (ngayDi != null ? " AND CAST(dt.thoiGianKhoiHanh AS DATE) = ?" : "")
                   + " ORDER BY dt.thoiGianKhoiHanh DESC";

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + filterGaDi  + "%");
            ps.setString(2, "%" + filterGaDen + "%");
            ps.setString(3, "%" + filterTau   + "%");
            if (ngayDi != null) ps.setDate(4, ngayDi);

            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp khoiHanh  = rs.getTimestamp("thoiGianKhoiHanh");
                    Timestamp denDuKien = rs.getTimestamp("thoiGianDuKien");
                    String ngayDiStr  = khoiHanh  != null ? sdfDate.format(khoiHanh)  : "";
                    String ngayDenStr = denDuKien != null ? sdfDate.format(denDuKien) : "";
                    String gioDiDen   = (khoiHanh  != null ? sdfTime.format(khoiHanh)  : "")
                                      + " - "
                                      + (denDuKien != null ? sdfTime.format(denDuKien) : "");
                    String dbStatus = rs.getString("trangThai");
                    String localizedStatus = switch (dbStatus != null ? dbStatus : "") {
                        case "CHUAN_BI"  -> "Chuẩn bị";
                        case "DANG_CHAY" -> "Đang chạy";
                        case "DA_DEN"    -> "Đã đến";
                        case "HUY"       -> "Bị hủy";
                        default          -> dbStatus != null ? dbStatus : "";
                    };
                    result.add(new Object[]{
                        rs.getString("gaDi"),
                        rs.getString("gaDen"),
                        ngayDiStr, ngayDenStr, gioDiDen,
                        rs.getString("tenTau"),
                        localizedStatus,
                        rs.getString("maChuyenTau")
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // =====================================================================
    // 3 HÀM MỚI — dùng cho DatVeGUI và DatVeGUI1
    // =====================================================================

    /**
     * Kiểm tra có chuyến tàu phù hợp không.
     * minTime = null → không lọc theo giờ.
     */
    public boolean checkChuyenTonTai(String tenGaDi, String tenGaDen,
                                      String ngayDiStr, java.util.Date minTime) {
        String sql = "SELECT TOP 1 1 FROM ChuyenTau c "
                   + "JOIN ChiTietChuyenTau dt ON c.maChuyenTau = dt.maChuyenTau "
                   + "JOIN Ga gDi  ON dt.maGaDi  = gDi.maGa "
                   + "JOIN Ga gDen ON dt.maGaDen = gDen.maGa "
                   + "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? "
                   + "AND CONVERT(VARCHAR, dt.thoiGianKhoiHanh, 103) = ? "
                   + (minTime != null ? "AND dt.thoiGianKhoiHanh >= ?" : "");

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, "%" + tenGaDi  + "%");
            ps.setNString(2, "%" + tenGaDen + "%");
            ps.setString(3, ngayDiStr);
            if (minTime != null)
                ps.setTimestamp(4, new Timestamp(minTime.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy thời gian đến của chuyến đi SỚM NHẤT còn hợp lệ.
     * Dùng để tính minTime cho chiều về khi khứ hồi cùng ngày.
     * Trả về null nếu không có chuyến nào.
     */
    public java.util.Date layThoiGianDenSomNhat(String tenGaDi, String tenGaDen,
                                                 String ngayDiStr, java.util.Date minTime) {
        String sql = "SELECT TOP 1 dt.thoiGianDuKien FROM ChuyenTau c "
                   + "JOIN ChiTietChuyenTau dt ON c.maChuyenTau = dt.maChuyenTau "
                   + "JOIN Ga gDi  ON dt.maGaDi  = gDi.maGa "
                   + "JOIN Ga gDen ON dt.maGaDen = gDen.maGa "
                   + "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? "
                   + "AND CONVERT(VARCHAR, dt.thoiGianKhoiHanh, 103) = ? "
                   + (minTime != null ? "AND dt.thoiGianKhoiHanh >= ?" : "")
                   + " ORDER BY dt.thoiGianKhoiHanh ASC";

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, "%" + tenGaDi  + "%");
            ps.setNString(2, "%" + tenGaDen + "%");
            ps.setString(3, ngayDiStr);
            if (minTime != null)
                ps.setTimestamp(4, new Timestamp(minTime.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getTimestamp("thoiGianDuKien");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Load danh sách chuyến tàu từ DB theo ga đi, ga đến, ngày.
     * Dùng trong DatVeGUI1 thay cho hàm loadChuyenFromDB() nội bộ.
     * Mỗi phần tử: {tenTau, tgDi, tgDen, ngayDi, ngayDen, maChuyen}
     */
    public String[][] loadChuyenFromDB(String tenGaDi, String tenGaDen, String ngayDiStr) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT ct.maChuyenTau AS maChuyen, t.tenTau, "
                   + "dt.thoiGianKhoiHanh, dt.thoiGianDuKien "
                   + "FROM ChuyenTau ct "
                   + "JOIN ChiTietChuyenTau dt ON ct.maChuyenTau = dt.maChuyenTau "
                   + "JOIN Tau t ON ct.maTau = t.maTau "
                   + "JOIN Ga gDi  ON dt.maGaDi  = gDi.maGa "
                   + "JOIN Ga gDen ON dt.maGaDen = gDen.maGa "
                   + "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? "
                   + "AND CONVERT(VARCHAR, dt.thoiGianKhoiHanh, 103) = ? "
                   + "ORDER BY dt.thoiGianKhoiHanh ASC";

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, "%" + tenGaDi  + "%");
            ps.setNString(2, "%" + tenGaDen + "%");
            ps.setString(3, ngayDiStr);
            ResultSet rs = ps.executeQuery();
            DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter dateFmt     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            while (rs.next()) {
                LocalDateTime tgDi  = rs.getTimestamp("thoiGianKhoiHanh").toLocalDateTime();
                LocalDateTime tgDen = rs.getTimestamp("thoiGianDuKien").toLocalDateTime();
                list.add(new String[]{
                    rs.getString("tenTau"),
                    tgDi.format(dateTimeFmt),
                    tgDen.format(dateTimeFmt),
                    tgDi.format(dateFmt),
                    tgDen.format(dateFmt),
                    rs.getString("maChuyen")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(new String[0][]);
    }
}