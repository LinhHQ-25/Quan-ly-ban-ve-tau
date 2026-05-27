package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import connect_DB.Connect_DB;
import entity.DonDoiTraVe;
import entity.LoaiDon;
import entity.Ve;
import util.MaTuDong;

public class DonDoiTraVeDAO implements DAO<DonDoiTraVe, String> {

    // =========================================================
    // CRUD – GIỮ NGUYÊN, không thay đổi
    // =========================================================

    @Override
    public List<DonDoiTraVe> selectAll() {
        List<DonDoiTraVe> list = new ArrayList<>();
        String sql = "SELECT * FROM DonDoiTraVe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Ve ve = new Ve();
                ve.setMaVe(rs.getString("maVe"));
                list.add(new DonDoiTraVe(
                        rs.getString("maDon"),
                        rs.getDouble("tienBu"),
                        rs.getTimestamp("ngayLap") != null ? rs.getTimestamp("ngayLap").toLocalDateTime() : null,
                        rs.getDouble("tienHoanTra"),
                        rs.getString("loaiDon") != null ? LoaiDon.valueOf(rs.getString("loaiDon")) : null,
                        ve));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public DonDoiTraVe selectById(String id) {
        String sql = "SELECT * FROM DonDoiTraVe WHERE maDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ve ve = new Ve();
                    ve.setMaVe(rs.getString("maVe"));
                    return new DonDoiTraVe(
                            rs.getString("maDon"),
                            rs.getDouble("tienBu"),
                            rs.getTimestamp("ngayLap") != null ? rs.getTimestamp("ngayLap").toLocalDateTime() : null,
                            rs.getDouble("tienHoanTra"),
                            rs.getString("loaiDon") != null ? LoaiDon.valueOf(rs.getString("loaiDon")) : null,
                            ve);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insert(DonDoiTraVe entity) {
        String sql = "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, tienHoanTra, loaiDon, maVe) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (entity.getMaDon() == null || entity.getMaDon().trim().isEmpty()) {
                java.time.LocalDate ngayLap = entity.getNgayLap() != null
                        ? entity.getNgayLap().toLocalDate()
                        : java.time.LocalDate.now();
                entity.setMaDon(MaTuDong.taoMaDon(con, ngayLap));
            }
            ps.setString(1, entity.getMaDon());
            ps.setDouble(2, entity.getTienBu());
            ps.setTimestamp(3, entity.getNgayLap() != null ? Timestamp.valueOf(entity.getNgayLap()) : null);
            ps.setDouble(4, entity.getTienHoanTra());
            ps.setString(5, entity.getLoaiDon() != null ? entity.getLoaiDon().name() : null);
            ps.setString(6, entity.getVe() != null ? entity.getVe().getMaVe() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(DonDoiTraVe entity) {
        String sql = "UPDATE DonDoiTraVe SET tienBu = ?, ngayLap = ?, tienHoanTra = ?, loaiDon = ?, maVe = ? WHERE maDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, entity.getTienBu());
            ps.setTimestamp(2, entity.getNgayLap() != null ? Timestamp.valueOf(entity.getNgayLap()) : null);
            ps.setDouble(3, entity.getTienHoanTra());
            ps.setString(4, entity.getLoaiDon() != null ? entity.getLoaiDon().name() : null);
            ps.setString(5, entity.getVe() != null ? entity.getVe().getMaVe() : null);
            ps.setString(6, entity.getMaDon());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM DonDoiTraVe WHERE maDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================
    // METHOD MỚI – phục vụ DoiTraGUI
    // Không ảnh hưởng các class khác đang dùng CRUD phía trên.
    // =========================================================

    /**
     * Tìm kiếm vé có thể đổi/trả theo từ khóa mã vé.
     *
     * Trả về Map<maVe, String[]> với layout:
     *   [0]  maChuyenTau
     *   [1]  gaDi
     *   [2]  gaDen
     *   [3]  rawLoaiVe      ("MOT_CHIEU" | "KHU_HOI")
     *   [4]  chieuVe        ("Chiều đi" | "Chiều về")
     *   [5]  ngayGioKH      (dd/MM/yyyy HH:mm)
     *   [6]  soLuongVe      (String số)
     *   [7]  maGhe
     *   [8]  giaVe          (String số)
     *   [9]  soGhe
     *   [10] tenKH
     *   [11] ngayMua        (dd/MM/yyyy HH:mm)
     *
     * @param keyword Từ khóa tìm kiếm mã vé (rỗng = lấy tất cả)
     * @return Map kết quả, rỗng nếu không tìm thấy
     */
    public Map<String, String[]> timKiemVe(String keyword) {
        Map<String, String[]> result = new LinkedHashMap<>();

        String kw = (keyword == null) ? "" : keyword.trim().toUpperCase();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        String sql =
                "SELECT v.maVe, ct.maChuyenTau AS maChuyenTau, " +
                "       gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
                "       v.loaiVe, dt.thoiGianKhoiHanh, v.giaVe, v.maGhe, " +
                "       g.soGhe, kh.hoTenKH, " +
                "       dt.maGaDi, dt.maGaDen, v.ngayMua, " +
                "       1 AS soLuongVe " +
                "FROM Ve v " +
                "JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                "JOIN ChuyenTau ct        ON dt.maChuyenTau = ct.maChuyenTau " +
                "JOIN Tau t               ON ct.maTau = t.maTau " +
                "JOIN Ga gDi              ON dt.maGaDi  = gDi.maGa " +
                "JOIN Ga gDen             ON dt.maGaDen = gDen.maGa " +
                "JOIN Ghe g               ON v.maGhe = g.maGhe " +
                "LEFT JOIN HoaDon hd      ON v.maHoaDon = hd.maHoaDon " +
                "LEFT JOIN KhachHang kh   ON hd.maKH = kh.maKH " +
                "WHERE v.maVe LIKE ? " +
                "  AND v.trangThaiVe = N'Đã thanh toán' " +
                "  AND (dt.maGaDi = 'DIEUTRI' OR dt.maGaDen = 'DIEUTRI') " +
                "ORDER BY dt.thoiGianKhoiHanh DESC";

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + kw + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maVe = rs.getString("maVe");
                    if (result.containsKey(maVe)) continue;

                    String maChuyenTau = rs.getString("maChuyenTau");
                    String gaDi        = rs.getString("gaDi");
                    String gaDen       = rs.getString("gaDen");
                    String rawLoaiVe   = rs.getString("loaiVe");
                    String maGaDiRaw   = rs.getString("maGaDi");

                    String chieuVe;
                    if ("MOT_CHIEU".equalsIgnoreCase(rawLoaiVe)) {
                        chieuVe = "Chiều đi";
                    } else {
                        chieuVe = "DIEUTRI".equals(maGaDiRaw) ? "Chiều đi" : "Chiều về";
                    }

                    String maGhe   = rs.getString("maGhe");
                    String soGhe   = rs.getString("soGhe");
                    String tenKH   = rs.getString("hoTenKH");
                    if (tenKH == null) tenKH = "Khách vãng lai";

                    String giaVe   = String.valueOf(rs.getLong("giaVe"));
                    String soLuong = String.valueOf(rs.getInt("soLuongVe"));

                    Timestamp tsKH   = rs.getTimestamp("thoiGianKhoiHanh");
                    String ngayGioKH = (tsKH  != null) ? sdf.format(tsKH)  : "";

                    Timestamp tsMua  = rs.getTimestamp("ngayMua");
                    String ngayMua   = (tsMua != null) ? sdf.format(tsMua) : "";

                    result.put(maVe, new String[]{
                            maChuyenTau,  // [0]
                            gaDi,         // [1]
                            gaDen,        // [2]
                            rawLoaiVe,    // [3]
                            chieuVe,      // [4]
                            ngayGioKH,    // [5]
                            soLuong,      // [6]
                            maGhe,        // [7]
                            giaVe,        // [8]
                            soGhe,        // [9]
                            tenKH,        // [10]
                            ngayMua       // [11]
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    /**
     * Insert đơn trả vé trong một transaction đang mở.
     * Con truyền vào từ ngoài, không tự đóng.
     */
    public boolean insertTrongTransaction(Connection con, String maDon,
            long tienBu, long tienHoanTra, String maVe) throws Exception {
        String sql = "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, tienHoanTra, loaiDon, maVe) "
                   + "VALUES (?, ?, GETDATE(), ?, 'DON_TRA', ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDon);
            ps.setLong  (2, tienBu);
            ps.setLong  (3, tienHoanTra);
            ps.setString(4, maVe);
            return ps.executeUpdate() > 0;
        }
    }
    /**
     * Lấy loại đối tượng của khách hàng mua vé (theo maVe).
     * Trả về "Người lớn" nếu không tìm được.
     */
    public String getLoaiDoiTuongTheoMaVe(String maVe) {
        String sql =
            "SELECT ISNULL(kh.loaiKhachHang, N'Người lớn') " +
            "FROM Ve v " +
            "LEFT JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
            "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
            "WHERE v.maVe = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString(1);
                    return (val != null && !val.isBlank()) ? val : "Người lớn";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Người lớn";
    }
}