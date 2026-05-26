package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import connect_DB.Connect_DB;
import entity.HoaDon;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhuongThucThanhToan;

public class HoaDonDAO implements DAO<HoaDon, String> {

    @Override
    public List<HoaDon> selectAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                list.add(new HoaDon(rs.getString("maHoaDon"),
                        rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime() : null,
                        nv, kh, rs.getDouble("tongTien"), rs.getDouble("tienNhan"),
                        rs.getString("phuongThucThanhToan") != null ? PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan")) : null));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public HoaDon selectById(String id) {
        String sql = "SELECT * FROM HoaDon WHERE maHoaDon = ?";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    KhachHang kh = new KhachHang();
                    kh.setMaKH(rs.getString("maKH"));
                    return new HoaDon(rs.getString("maHoaDon"),
                            rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime() : null,
                            nv, kh, rs.getDouble("tongTien"), rs.getDouble("tienNhan"),
                            rs.getString("phuongThucThanhToan") != null ? PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan")) : null);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean insert(HoaDon entity) {
        String sql = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entity.getMaHoaDon());
            ps.setTimestamp(2, entity.getNgayLapHD() != null ? java.sql.Timestamp.valueOf(entity.getNgayLapHD()) : null);
            ps.setString(3, entity.getNhanVien() != null ? entity.getNhanVien().getMaNV() : null);
            ps.setString(4, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setDouble(5, entity.getTongTien());
            ps.setDouble(6, entity.getTienNhan());
            ps.setString(7, entity.getPhuongThucThanhToan() != null ? entity.getPhuongThucThanhToan().name() : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean update(HoaDon entity) {
        String sql = "UPDATE HoaDon SET ngayLapHD = ?, maNV = ?, maKH = ?, tongTien = ?, tienNhan = ?, phuongThucThanhToan = ? WHERE maHoaDon = ?";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, entity.getNgayLapHD() != null ? java.sql.Timestamp.valueOf(entity.getNgayLapHD()) : null);
            ps.setString(2, entity.getNhanVien() != null ? entity.getNhanVien().getMaNV() : null);
            ps.setString(3, entity.getKhachHang() != null ? entity.getKhachHang().getMaKH() : null);
            ps.setDouble(4, entity.getTongTien());
            ps.setDouble(5, entity.getTienNhan());
            ps.setString(6, entity.getPhuongThucThanhToan() != null ? entity.getPhuongThucThanhToan().name() : null);
            ps.setString(7, entity.getMaHoaDon());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM HoaDon WHERE maHoaDon = ?";
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public static List<Object[]> getDanhSachHoaDonTheoCa(java.time.LocalDate ngay, String ca, String maNV) throws SQLException {
        String timeCondition = ca.equalsIgnoreCase("Sáng")
                ? " BETWEEN '00:00:00' AND '11:59:59'"
                : " BETWEEN '12:00:00' AND '23:59:59'";

        String sql = "SELECT h.maHoaDon, " +
                "       CONVERT(varchar, h.ngayLapHD, 108) AS gioBan, " +
                "       k.hoTenKH, " +
                "       (SELECT TOP 1 g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS loaiGhe, " +
                "       (SELECT COUNT(*) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS soGhe, " +
                "       (SELECT ISNULL(SUM(v2.giaVe), 0) FROM Ve v2 " +
                "        WHERE v2.maHoaDon = h.maHoaDon " +
                "        AND v2.trangThaiVe = N'Đã thanh toán') AS tongTien, " +
                "       ISNULL(h.phuongThucThanhToan, '') AS phuongThuc " +
                "FROM HoaDon h " +
                "JOIN KhachHang k ON h.maKH = k.maKH " +
                "WHERE CAST(h.ngayLapHD AS DATE) = ? AND h.maNV = ? " +
                "AND CAST(h.ngayLapHD AS TIME)" + timeCondition +
                // Chỉ lấy hóa đơn có ít nhất 1 vé đã thanh toán
                // → loại hóa đơn lưu tạm (Chờ thanh toán) và hóa đơn đã trả hết vé
                " AND EXISTS (SELECT 1 FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán')" +
                " ORDER BY h.ngayLapHD DESC";

        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("gioBan"),
                            rs.getString("hoTenKH"),
                            rs.getString("loaiGhe"),
                            rs.getInt("soGhe"),
                            rs.getDouble("tongTien"),
                            rs.getString("phuongThuc")
                    });
                }
            }
        }
        return rows;
    }
    // ── MỚI: load HĐ bán (thường + đổi) của nhân viên trong ngày hôm nay, không lọc ca ──
    public static List<Object[]> getDanhSachHoaDonHomNay(String maNV) throws SQLException {
        java.time.LocalDate ngay = java.time.LocalDate.now();
        String sql = "SELECT h.maHoaDon, " +
                "       CONVERT(varchar, h.ngayLapHD, 108) AS gioBan, " +
                "       k.hoTenKH, " +
                "       (SELECT TOP 1 g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS loaiGhe, " +
                "       (SELECT COUNT(*) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS soGhe, " +
                "       (SELECT ISNULL(SUM(v2.giaVe), 0) FROM Ve v2 " +
                "        WHERE v2.maHoaDon = h.maHoaDon " +
                "        AND v2.trangThaiVe = N'Đã thanh toán') AS tongTien, " +
                "       ISNULL(h.phuongThucThanhToan, '') AS phuongThuc " +
                "FROM HoaDon h " +
                "JOIN KhachHang k ON h.maKH = k.maKH " +
                "WHERE CAST(h.ngayLapHD AS DATE) = ? AND h.maNV = ? " +
                " AND EXISTS (SELECT 1 FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán')" +
                " ORDER BY h.ngayLapHD DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("gioBan"),
                            rs.getString("hoTenKH"),
                            rs.getString("loaiGhe"),
                            rs.getInt("soGhe"),
                            rs.getDouble("tongTien"),
                            rs.getString("phuongThuc")
                    });
                }
            }
        }
        return rows;
    }

    // ── MỚI: load HĐ hủy (vé trả) của nhân viên trong ngày hôm nay, không lọc ca ──
    public static List<Object[]> getDanhSachHoaDonHuyHomNay(String maNV) throws SQLException {
        java.time.LocalDate ngay = java.time.LocalDate.now();
        String sql = "SELECT h.maHoaDon, " +
                "       CONVERT(varchar, h.ngayLapHD, 108) AS gioBan, " +
                "       k.hoTenKH, " +
                "       (SELECT TOP 1 g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY')) AS loaiGhe, " +
                "       (SELECT COUNT(*) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY')) AS soGhe, " +
                "       h.tongTien AS tongTien, " +
                "       ISNULL(h.phuongThucThanhToan, '') AS phuongThuc " +
                "FROM HoaDon h " +
                "JOIN KhachHang k ON h.maKH = k.maKH " +
                "WHERE CAST(h.ngayLapHD AS DATE) = ? AND h.maNV = ? " +
                " AND EXISTS (SELECT 1 FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY'))" +
                " ORDER BY h.ngayLapHD DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("gioBan"),
                            rs.getString("hoTenKH"),
                            rs.getString("loaiGhe"),
                            rs.getInt("soGhe"),
                            rs.getDouble("tongTien"),
                            rs.getString("phuongThuc")
                    });
                }
            }
        }
        return rows;
    }

    //load hoa don huy
    public static List<Object[]> getDanhSachHoaDonHuyTheoCa(java.time.LocalDate ngay, String ca, String maNV) throws SQLException {
        String timeCondition = ca.equalsIgnoreCase("Sáng")
                ? " BETWEEN '00:00:00' AND '11:59:59'"
                : " BETWEEN '12:00:00' AND '23:59:59'";

        String sql = "SELECT h.maHoaDon, " +
                "       CONVERT(varchar, h.ngayLapHD, 108) AS gioBan, " +
                "       k.hoTenKH, " +
                // THAY: thêm cả 'DA_HUY' vào điều kiện loaiGhe
                "       (SELECT TOP 1 g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY')) AS loaiGhe, " +
                // THAY: thêm cả 'DA_HUY' vào điều kiện soGhe
                "       (SELECT COUNT(*) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY')) AS soGhe, " +
                // Lấy trực tiếp h.tongTien (phí phạt đã lưu đúng khi trả vé)
                "       h.tongTien AS tongTien, " +
                "       ISNULL(h.phuongThucThanhToan, '') AS phuongThuc " +
                "FROM HoaDon h " +
                "JOIN KhachHang k ON h.maKH = k.maKH " +
                "WHERE CAST(h.ngayLapHD AS DATE) = ? AND h.maNV = ? " +
                "AND CAST(h.ngayLapHD AS TIME)" + timeCondition +
                // THAY: thêm cả 'DA_HUY' vào EXISTS
                " AND EXISTS (SELECT 1 FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY'))" +
                " ORDER BY h.ngayLapHD DESC";

        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            ps.setString(2, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("gioBan"),
                            rs.getString("hoTenKH"),
                            rs.getString("loaiGhe"),
                            rs.getInt("soGhe"),
                            rs.getDouble("tongTien"),
                            rs.getString("phuongThuc")
                    });
                }
            }
        }
        return rows;
    }
 // ── QUẢN LÝ: danh sách HĐ bán theo khoảng thời gian (tất cả NV) ──
    public static List<Object[]> getDanhSachHoaDonTheoKhoang(java.time.LocalDate from, java.time.LocalDate to) throws java.sql.SQLException {
        String sql = "SELECT h.maHoaDon, " +
        		"       CONVERT(varchar, h.ngayLapHD, 103) + ' ' + CONVERT(varchar, h.ngayLapHD, 108) AS gioBan, " +
                "       k.hoTenKH, " +
                "       (SELECT TOP 1 g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS loaiGhe, " +
                "       (SELECT COUNT(*) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán') AS soGhe, " +
                "       (SELECT ISNULL(SUM(v2.giaVe), 0) FROM Ve v2 " +
                "        WHERE v2.maHoaDon = h.maHoaDon " +
                "        AND v2.trangThaiVe = N'Đã thanh toán') AS tongTien, " +
                "       ISNULL(h.phuongThucThanhToan, '') AS phuongThuc " +
                "FROM HoaDon h " +
                "JOIN KhachHang k ON h.maKH = k.maKH " +
                "WHERE CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                " AND EXISTS (SELECT 1 FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe = N'Đã thanh toán')" +
                " ORDER BY h.ngayLapHD DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("gioBan"),
                            rs.getString("hoTenKH"),
                            rs.getString("loaiGhe"),
                            rs.getInt("soGhe"),
                            rs.getDouble("tongTien"),
                            rs.getString("phuongThuc")
                    });
                }
            }
        }
        return rows;
    }

    // ── QUẢN LÝ: danh sách HĐ hủy theo khoảng thời gian (tất cả NV) ──
    public static List<Object[]> getDanhSachHoaDonHuyTheoKhoang(java.time.LocalDate from, java.time.LocalDate to) throws java.sql.SQLException {
        String sql = "SELECT h.maHoaDon, " +
        		"       CONVERT(varchar, h.ngayLapHD, 103) + ' ' + CONVERT(varchar, h.ngayLapHD, 108) AS gioBan, " +
                "       k.hoTenKH, " +
                "       (SELECT TOP 1 g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY')) AS loaiGhe, " +
                "       (SELECT COUNT(*) FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY')) AS soGhe, " +
                "       h.tongTien AS tongTien, " +
                "       ISNULL(h.phuongThucThanhToan, '') AS phuongThuc " +
                "FROM HoaDon h " +
                "JOIN KhachHang k ON h.maKH = k.maKH " +
                "WHERE CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                " AND ISNULL(h.phuongThucThanhToan,'') <> 'LUU_TAM' " +
                " AND EXISTS (SELECT 1 FROM Ve v WHERE v.maHoaDon = h.maHoaDon AND v.trangThaiVe IN (N'Đã hủy', 'DA_HUY'))" +
                " ORDER BY h.ngayLapHD DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("gioBan"),
                            rs.getString("hoTenKH"),
                            rs.getString("loaiGhe"),
                            rs.getInt("soGhe"),
                            rs.getDouble("tongTien"),
                            rs.getString("phuongThuc")
                    });
                }
            }
        }
        return rows;
    }

    // ── QUẢN LÝ: doanh thu theo từng NV theo khoảng thời gian ──
    public static List<Object[]> getDoanhThuNhanVienTheoKhoang(java.time.LocalDate from, java.time.LocalDate to) throws java.sql.SQLException {
        String sql = "SELECT nv.maNV, nv.hoTenNV, COUNT(v.maVe) AS soBan, ISNULL(SUM(v.giaVe), 0) AS doanhThu " +
                "FROM NhanVien nv " +
                "JOIN HoaDon h ON nv.maNV = h.maNV " +
                "JOIN Ve v ON h.maHoaDon = v.maHoaDon " +
                "WHERE v.trangThaiVe = N'Đã thanh toán' " +
                "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                "GROUP BY nv.maNV, nv.hoTenNV " +
                "ORDER BY doanhThu DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("maNV"),
                            rs.getString("hoTenNV"),
                            rs.getInt("soBan"),
                            rs.getLong("doanhThu")
                    });
                }
            }
        }
        return rows;
    }
    /**
     * Insert hóa đơn trong một transaction đang mở (con truyền vào, không tự đóng).
     * Dùng trong luuDuLieuVaoDatabase() của DatVeGUI3.
     */
    public boolean insertTrongTransaction(Connection con, String maHD, String maNV, String maKH,
                                          double tongTien, double tienNhan,
                                          String phuongThucThanhToan) throws Exception {
        String sql = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) "
                   + "VALUES (?, GETDATE(), ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setString(2, maNV);
            if (maKH != null) ps.setString(3, maKH);
            else              ps.setNull(3, java.sql.Types.VARCHAR);
            ps.setDouble(4, tongTien);
            ps.setDouble(5, tienNhan);
            ps.setString(6, phuongThucThanhToan);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Update hóa đơn trong một transaction đang mở (con truyền vào, không tự đóng).
     * Dùng trong luuDuLieuVaoDatabase() của DatVeGUI3 để gộp hóa đơn lưu tạm.
     */
    public boolean updateTrongTransaction(Connection con, String maHD, String maNV, String maKH,
                                          double tongTien, double tienNhan,
                                          String phuongThucThanhToan) throws Exception {
        String sql = "UPDATE HoaDon SET ngayLapHD = GETDATE(), maNV = ?, maKH = ?, tongTien = ?, tienNhan = ?, phuongThucThanhToan = ? "
                   + "WHERE maHoaDon = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            if (maKH != null) ps.setString(2, maKH);
            else              ps.setNull(2, java.sql.Types.VARCHAR);
            ps.setDouble(3, tongTien);
            ps.setDouble(4, tienNhan);
            ps.setString(5, phuongThucThanhToan);
            ps.setString(6, maHD);
            return ps.executeUpdate() > 0;
        }
    }

    public static Object[] getChiTietHoaDon(String maHD) throws SQLException {
        String sql =
                "SELECT TOP 1 " +
                        "    hd.maHoaDon, " +
                        "    hd.ngayLapHD, " +
                        "    hd.phuongThucThanhToan, " +
                        "    hd.tongTien, " +
                        "    kh.hoTenKH, " +
                        "    kh.sdt, " +
                        "    CASE WHEN kh.laSinhVien = 1 THEN N'Sinh viên' ELSE N'Người lớn' END AS doiTuong, " +
                        "    tau.tenTau, " +
                        "    gaDi.tenGa + N' -> ' + gaDen.tenGa AS loTrinh, " +
                        "    ctct.thoiGianKhoiHanh, " +
                        "    g.soGhe, " +
                        "    g.loaiGhe, " +
                        "    v.trangThaiVe, " +
                        "    (SELECT COUNT(*) FROM Ve v2 WHERE v2.maHoaDon = hd.maHoaDon) AS soVe " +
                        "FROM HoaDon hd " +
                        "LEFT JOIN KhachHang kh          ON hd.maKH = kh.maKH " +
                        "LEFT JOIN Ve v                  ON v.maHoaDon = hd.maHoaDon " +
                        "LEFT JOIN Ghe g                 ON v.maGhe = g.maGhe " +
                        "LEFT JOIN ToaTau tt             ON g.maToaTau = tt.maToaTau " +
                        "LEFT JOIN ChuyenTau ct          ON v.maChuyenTau = ct.maChuyenTau " +
                        "LEFT JOIN Tau tau               ON ct.maTau = tau.maTau " +
                        "LEFT JOIN ChiTietChuyenTau ctct ON ctct.maChuyenTau = v.maChuyenTau " +
                        "LEFT JOIN Ga gaDi               ON ctct.maGaDi = gaDi.maGa " +
                        "LEFT JOIN Ga gaDen              ON ctct.maGaDen = gaDen.maGa " +
                        "WHERE hd.maHoaDon = ?";

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getString("maHoaDon"),              // [0]
                            rs.getTimestamp("ngayLapHD"),          // [1]
                            rs.getString("phuongThucThanhToan"),   // [2]
                            rs.getDouble("tongTien"),              // [3]
                            rs.getString("hoTenKH"),               // [4]
                            rs.getString("sdt"),                   // [5]
                            rs.getString("doiTuong"),              // [6]
                            rs.getString("tenTau"),                // [7]
                            rs.getString("loTrinh"),               // [8]
                            rs.getTimestamp("thoiGianKhoiHanh"),   // [9]
                            rs.getString("soGhe"),                 // [10]
                            rs.getString("loaiGhe"),               // [11]
                            rs.getString("trangThaiVe"),           // [12]
                            rs.getInt("soVe")                      // [13]
                    };
                }
            }
        }
        return null;
    }
    // Lấy thông tin chung của hóa đơn (không JOIN vé)
    public static Object[] getThongTinHoaDon(String maHD) throws SQLException {
        String sql =
                "SELECT hd.maHoaDon, hd.ngayLapHD, hd.phuongThucThanhToan, " +
                        "       (SELECT ISNULL(SUM(v2.giaVe), 0) FROM Ve v2 " +
                        "        WHERE v2.maHoaDon = hd.maHoaDon " +
                        "        AND v2.trangThaiVe = N'Đã thanh toán') AS tongTien, " +
                        "       kh.hoTenKH, kh.sdt, " +
                        "       CASE WHEN kh.laSinhVien = 1 THEN N'Sinh viên' ELSE N'Người lớn' END AS doiTuong " +
                        "FROM HoaDon hd " +
                        "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                        "WHERE hd.maHoaDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Object[]{
                        rs.getString("maHoaDon"),
                        rs.getTimestamp("ngayLapHD"),
                        rs.getString("phuongThucThanhToan"),
                        rs.getDouble("tongTien"),
                        rs.getString("hoTenKH"),
                        rs.getString("sdt"),
                        rs.getString("doiTuong")
                };
            }
        }
        return null;
    }

    // Lấy danh sách tất cả vé của hóa đơn
    public static List<Object[]> getDanhSachVeTheoHoaDon(String maHD) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
                "SELECT v.maVe, v.trangThaiVe, v.giaVe, v.loaiVe, " +
                        "       g.soGhe, g.loaiGhe, " +
                        "       tau.tenTau, " +
                        "       gaDi.tenGa + N' -> ' + gaDen.tenGa AS loTrinh, " +
                        "       ctct.thoiGianKhoiHanh " +
                        "FROM Ve v " +
                        "LEFT JOIN Ghe g                 ON v.maGhe = g.maGhe " +
                        "LEFT JOIN ChuyenTau ct          ON v.maChuyenTau = ct.maChuyenTau " +
                        "LEFT JOIN Tau tau               ON ct.maTau = tau.maTau " +
                        "LEFT JOIN ChiTietChuyenTau ctct ON ctct.maChuyenTau = v.maChuyenTau " +
                        "LEFT JOIN Ga gaDi               ON ctct.maGaDi = gaDi.maGa " +
                        "LEFT JOIN Ga gaDen              ON ctct.maGaDen = gaDen.maGa " +
                        "WHERE v.maHoaDon = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Object[]{
                        rs.getString("maVe"),                   // [0]
                        rs.getString("trangThaiVe"),            // [1]
                        rs.getDouble("giaVe"),                  // [2]
                        rs.getString("loaiVe"),                 // [3]
                        rs.getString("soGhe"),                  // [4]
                        rs.getString("loaiGhe"),                // [5]
                        rs.getString("tenTau"),                 // [6]
                        rs.getString("loTrinh"),                // [7]
                        rs.getTimestamp("thoiGianKhoiHanh")     // [8]
                });
            }
        }
        return list;
    }
}