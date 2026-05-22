package gui;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import connect_DB.Connect_DB;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Xuất hóa đơn PDF dùng chung cho đổi vé và trả vé,
 * y chang format hóa đơn mua vé bình thường.
 */
public class HoaDonPDFExporter {

    private static final DecimalFormat DF = new DecimalFormat("#,###");

    /**
     * Xuất PDF hóa đơn theo maDon (đổi hoặc trả vé).
     * Query tất cả dữ liệu từ DB rồi tạo PDF vào thư mục HoaDon/.
     */
    public static void xuatPDF(String maDon) {
        try (Connection conn = Connect_DB.getInstance().getConnection()) {

            // 1. Thông tin hóa đơn + khách hàng + nhân viên
            String sqlHD =
                    "SELECT h.maHoaDon, h.ngayLapHD, h.tongTien, h.tienNhan, h.phuongThucThanhToan, " +
                            "       k.hoTenKH, k.soDienThoai, nv.hoTenNV " +
                            "FROM HoaDon h " +
                            "LEFT JOIN KhachHang k  ON h.maKH = k.maKH " +
                            "LEFT JOIN NhanVien  nv ON h.maNV = nv.maNV " +
                            "WHERE h.maHoaDon = ?";

            String tenKH = "Khách vãng lai", sdtKH = "", hinhThuc = "", tenNV = "";
            double tongTien = 0, tienNhan = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlHD)) {
                ps.setString(1, maDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tenKH    = rs.getString("hoTenKH")    != null ? rs.getString("hoTenKH")    : "Khách vãng lai";
                        sdtKH    = rs.getString("soDienThoai") != null ? rs.getString("soDienThoai") : "";
                        hinhThuc = rs.getString("phuongThucThanhToan") != null ? rs.getString("phuongThucThanhToan") : "";
                        tenNV    = rs.getString("hoTenNV")    != null ? rs.getString("hoTenNV")    : "";
                        tongTien = rs.getDouble("tongTien");
                        tienNhan = rs.getDouble("tienNhan");
                    }
                }
            }

            // 2. Danh sách vé trong hóa đơn
            String sqlVe =
                    "SELECT v.maVe, v.loaiVe, v.giaVe, g.loaiGhe, " +
                            "       gaDi.tenGa AS gaDi, gaDen.tenGa AS gaDen, " +
                            "       dt.thoiGianKhoiHanh " +
                            "FROM Ve v " +
                            "JOIN Ghe g              ON v.maGhe       = g.maGhe " +
                            "JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                            "JOIN Ga gaDi            ON dt.maGaDi      = gaDi.maGa " +
                            "JOIN Ga gaDen           ON dt.maGaDen     = gaDen.maGa " +
                            "WHERE v.maHoaDon = ?";

            // 3. Tạo PDF
            File folder = new File("HoaDon");
            if (!folder.exists()) folder.mkdir();
            File pdfFile = new File(folder, maDon + ".pdf");

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle  = new Font(bf, 16, Font.BOLD);
            Font fontBold   = new Font(bf, 11, Font.BOLD);
            Font fontNormal = new Font(bf, 11, Font.NORMAL);
            Font fontItalic = new Font(bf, 11, Font.ITALIC);

            // Tiêu đề
            Paragraph title = new Paragraph("HÓA ĐƠN GIÁ TRỊ GIA TĂNG", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            Paragraph dateP = new Paragraph("Ngày xuất: " + dateStr, fontItalic);
            dateP.setAlignment(Element.ALIGN_CENTER);
            document.add(dateP);
            document.add(new Paragraph(" ", fontNormal));
            document.add(new Paragraph(" ", fontNormal));

            // Thông tin đơn vị
            document.add(new Paragraph("Đơn vị bán hàng: CÔNG TY CỔ PHẦN VẬN TẢI ĐƯỜNG SẮT", fontBold));
            document.add(new Paragraph("Mã số thuế: 0100106264", fontNormal));
            document.add(new Paragraph("Địa chỉ: 113 Nguyễn Đình Thụ, Tuy Phước, Gia Lai", fontNormal));
            document.add(new Paragraph(" ", fontNormal));

            // Thông tin khách hàng
            document.add(new Paragraph("Họ tên người mua hàng: " + tenKH, fontBold));
            document.add(new Paragraph("Điện thoại: " + sdtKH, fontNormal));
            document.add(new Paragraph("Hình thức thanh toán: " + hinhThuc + "          Mã HĐ: " + maDon, fontNormal));
            document.add(new Paragraph(" ", fontNormal));

            // Bảng vé
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2f, 2f, 2f, 2f, 1.5f, 2f});

            for (String h : new String[]{"STT", "Mã vé", "Loại vé", "Ga đi", "Ga đến", "Loại ghế", "Thành tiền"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontBold));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPaddingBottom(6);
                cell.setBackgroundColor(new BaseColor(245, 245, 245));
                table.addCell(cell);
            }

            int stt = 1;
            try (PreparedStatement ps = conn.prepareStatement(sqlVe)) {
                ps.setString(1, maDon);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maVe    = rs.getString("maVe");
                        String loaiVe  = rs.getString("loaiVe");
                        String gaDi    = rs.getString("gaDi");
                        String gaDen   = rs.getString("gaDen");
                        String loaiGhe = rs.getString("loaiGhe");
                        double giaVe   = rs.getDouble("giaVe");

                        addCell(table, fontNormal, String.valueOf(stt++), Element.ALIGN_CENTER);
                        addCell(table, fontNormal, maVe,    Element.ALIGN_LEFT);
                        addCell(table, fontNormal, loaiVe,  Element.ALIGN_CENTER);
                        addCell(table, fontNormal, gaDi,    Element.ALIGN_LEFT);
                        addCell(table, fontNormal, gaDen,   Element.ALIGN_LEFT);
                        addCell(table, fontNormal, loaiGhe, Element.ALIGN_CENTER);
                        addCell(table, fontNormal, DF.format(giaVe) + " VNĐ", Element.ALIGN_RIGHT);
                    }
                }
            }
            document.add(table);
            document.add(new Paragraph(" ", fontNormal));

            // Tổng tiền
            Paragraph pTong = new Paragraph("Tổng tiền: " + DF.format(tongTien) + " VNĐ", fontBold);
            pTong.setAlignment(Element.ALIGN_RIGHT);
            document.add(pTong);

            if (tienNhan > 0) {
                Paragraph pNhan = new Paragraph("Tiền nhận: " + DF.format(tienNhan) + " VNĐ", fontBold);
                pNhan.setAlignment(Element.ALIGN_RIGHT);
                document.add(pNhan);
            }

            document.add(new Paragraph(" ", fontNormal));
            document.add(new Paragraph(
                    "Ghi chú: ......................................................................................................................................",
                    fontNormal));
            document.add(new Paragraph(" ", fontNormal));
            document.add(new Paragraph(" ", fontNormal));

            // Ký tên
            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);
            PdfPCell cellBuyer = new PdfPCell(new Phrase("Người mua hàng\n(Ký, ghi rõ họ tên)", fontNormal));
            cellBuyer.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellBuyer.setBorder(PdfPCell.NO_BORDER);
            PdfPCell cellSeller = new PdfPCell(new Phrase("Người bán hàng\n(Ký, ghi rõ họ tên)", fontNormal));
            cellSeller.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellSeller.setBorder(PdfPCell.NO_BORDER);
            signTable.addCell(cellBuyer);
            signTable.addCell(cellSeller);
            document.add(signTable);

            document.close();
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addCell(PdfPTable table, Font font, String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingBottom(4);
        table.addCell(cell);
    }
}