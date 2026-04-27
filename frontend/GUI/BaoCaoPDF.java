package GUI;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BaoCaoPDF {

    // ── Kích thước trang A4 (points) ─────────────────────────────────────────
    private static final float W  = PDRectangle.A4.getWidth();   // 595.28
    private static final float H  = PDRectangle.A4.getHeight();  // 841.89
    private static final float M  = 42f;   // lề trái / phải
    private static final float CW = W - 2 * M;  // chiều rộng nội dung

    // ── Bảng màu (RGB 0–1) ───────────────────────────────────────────────────
    private static final float[] NAVY   = {0.10f, 0.13f, 0.50f};   // #1A2180
    private static final float[] NAVY_L = {0.13f, 0.17f, 0.62f};   // header accent
    private static final float[] WHITE  = {1f, 1f, 1f};
    private static final float[] LGRAY  = {0.94f, 0.95f, 0.97f};
    private static final float[] MGRAY  = {0.80f, 0.82f, 0.86f};
    private static final float[] DTEXT  = {0.10f, 0.12f, 0.18f};
    private static final float[] MTEXT  = {0.45f, 0.47f, 0.54f};

    // ─────────────────────────────────────────────────────────────────────────
    public static void export(String tenNhanVien,
                               long doanhThu, int veBan, int veHuy,
                               int soGiuong, int soGheMem, int soGheCung) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType0Font fReg  = loadFont(doc, false);
            PDType0Font fBold = loadFont(doc, true);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                LocalDateTime now   = LocalDateTime.now();
                String strDate      = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String strTime      = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                NumberFormat nf     = NumberFormat.getInstance(new Locale("vi", "VN"));
                String strDoanh     = nf.format(doanhThu) + " \u0111";

                float y = H;  // cursor từ đỉnh xuống

                // ── HEADER BAND ──────────────────────────────────────────────
                float hdrH = 72f;
                fillRect(cs, 0, H - hdrH, W, hdrH, NAVY);
                fillRect(cs, 0, H - 5, W, 5, NAVY_L);
                fillRect(cs, M, H - hdrH, CW, 0.8f, new float[]{1f, 1f, 1f, 0.25f});

                // ── Tiêu đề trong header ──────────────────────────────────────
                drawTextCentered(cs, fBold, 17f, W, H - 30f, WHITE,
                        "HỆ THỐNG QUẢN LÝ BÁN VÉ TÀU HỎA");
                drawTextCentered(cs, fReg,  11f, W, H - 52f, new float[]{0.72f, 0.80f, 1.0f},
                        "GA DIÊU TRÌ");

                y = H - hdrH - 22f;

                // ── Tên báo cáo ───────────────────────────────────────────────
                drawTextCentered(cs, fBold, 13.5f, W, y, NAVY,
                        "BÁO CÁO THỐNG KÊ CA LÀM VIỆC");
                y -= 9f;
                drawHLine(cs, M, y, CW, MGRAY, 0.6f);
                y -= 18f;

                // ── INFO ROW ────────────────────────
                fillRect(cs, M, y - 9f, CW, 27f, LGRAY);
                drawRect(cs, M, y - 9f, CW, 27f, MGRAY, 0.5f);

                float col3 = CW / 3f;
                drawLabelValue(cs, fReg, fBold, M + 10f, y + 4f, 10f,
                        "Nhân viên: ", tenNhanVien);
                drawLabelValue(cs, fReg, fBold, M + col3 + 10f, y + 4f, 10f,
                        "Ngày xuất: ", strDate);
                drawLabelValue(cs, fReg, fBold, M + col3 * 2f + 10f, y + 4f, 10f,
                        "Giờ xuất: ", strTime);
                y -= 28f;

                // ── SECTION 1 ────────────────────────
                y = drawSectionHeader(cs, fBold, M, y, CW,
                        "TỔNG QUAN CA LÀM VIỆC");
                y -= 10f;

                float bw = CW / 3f - 7f, bh = 70f;
                drawStatCard(cs, fReg, fBold,
                        M,              y - bh, bw, bh,
                        "TỔNG DOANH THU", strDoanh,
                        new float[]{0.10f,0.13f,0.50f},
                        new float[]{0.93f,0.94f,0.99f});
                drawStatCard(cs, fReg, fBold,
                        M + bw + 10.5f, y - bh, bw, bh,
                        "VÉ ĐÃ BÁN", veBan + " v\u00e9",
                        new float[]{0.08f,0.48f,0.20f},
                        new float[]{0.92f,0.98f,0.93f});
                drawStatCard(cs, fReg, fBold,
                        M + (bw+10.5f)*2, y - bh, bw, bh,
                        "VÉ ĐÃ HỦY", veHuy + " v\u00e9",
                        new float[]{0.70f,0.14f,0.14f},
                        new float[]{0.99f,0.92f,0.92f});
                y -= bh + 22f;

                // ── SECTION 2 ─────────────────────────────────
                y = drawSectionHeader(cs, fBold, M, y, CW,
                        "PHÂN LOẠI VÉ THEO LOẠI GHẾ");
                y -= 10f;

                float bw2 = CW / 3f - 7f, bh2 = 70f;
                drawStatCard(cs, fReg, fBold,
                        M,               y - bh2, bw2, bh2,
                        "GIƯỜNG", soGiuong + " v\u00e9",
                        new float[]{0.25f,0.45f,0.78f},
                        new float[]{0.92f,0.95f,0.99f});
                drawStatCard(cs, fReg, fBold,
                        M + bw2 + 10.5f, y - bh2, bw2, bh2,
                        "GHẾ MỀM", soGheMem + " v\u00e9",
                        new float[]{0.84f,0.50f,0.06f},
                        new float[]{0.99f,0.96f,0.90f});
                drawStatCard(cs, fReg, fBold,
                        M + (bw2+10.5f)*2, y - bh2, bw2, bh2,
                        "GHẾ CỨNG", soGheCung + " v\u00e9",
                        new float[]{0.12f,0.56f,0.33f},
                        new float[]{0.91f,0.98f,0.93f});
                y -= bh2 + 24f;

                // ── SUMMARY LINE ──────────────────────────────────────────────
                int tongGhe = soGiuong + soGheMem + soGheCung;
                if (tongGhe > 0) {
                    fillRect(cs, M, y - 8f, CW, 24f, LGRAY);
                    drawRect(cs, M, y - 8f, CW, 24f, MGRAY, 0.5f);
                    String summaryLeft = "T\u1ED5ng h\u1EE3p: " + veBan + " v\u00e9 b\u00e1n  \u2022  "
                            + veHuy + " v\u00e9 h\u1EE7y";
                    String summaryRight = "Doanh thu: " + strDoanh;
                    drawText(cs, fReg, 9.5f, M + 10f, y + 3f, MTEXT, summaryLeft);
                    float rw = textWidth(fReg, 9.5f, summaryRight);
                    drawText(cs, fBold, 9.5f, M + CW - rw - 10f, y + 3f, NAVY, summaryRight);
                }

                // ── FOOTER ────────────────────────────────────────────────────
                drawHLine(cs, M, 58f, CW, MGRAY, 0.5f);
                String footer = "Xu\u1EA5t l\u00fac: " + strDate + " " + strTime
                        + "   \u2022   H\u1EC7 th\u1ED1ng qu\u1EA3n l\u00fd v\u00e9 t\u00e0u h\u1ECFa ga Di\u00eau Tr\u00ec"
                        + "   \u2022   T\u00e0i li\u1EC7u n\u1ED9i b\u1ED9";
                drawTextCentered(cs, fReg, 8f, W, 43f,
                        new float[]{0.58f, 0.58f, 0.62f}, footer);

                // page number
                drawText(cs, fReg, 8f, W - M - 20f, 43f,
                        new float[]{0.58f, 0.58f, 0.62f}, "1 / 1");
            }

            // ── Lưu & mở file ────────────────────────────────────────────────
            LocalDateTime timeNow = LocalDateTime.now();
            String stamp = timeNow.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            File outFile = new File(
                System.getProperty("user.home") + "/Desktop/ThongKe_" + stamp + ".pdf");
            doc.save(outFile);

            JOptionPane.showMessageDialog(null,
                    "<html><b>Xu\u1EA5t PDF th\u00e0nh c\u00f4ng!</b><br>"
                    + outFile.getAbsolutePath() + "</html>",
                    "Th\u00e0nh c\u00f4ng", JOptionPane.INFORMATION_MESSAGE);

            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(outFile);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "L\u1ED7i xu\u1EA5t PDF:\n" + ex.getMessage(),
                    "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ── Font loader (hỗ trợ tiếng Việt qua Arial) ────────────────────────────
    private static PDType0Font loadFont(PDDocument doc, boolean bold) throws IOException {
        String[] candidates = bold
            ? new String[]{
                "C:/Windows/Fonts/arialbd.ttf",
                "C:/Windows/Fonts/Arial Bold.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                "/System/Library/Fonts/Supplemental/Arial Bold.ttf"}
            : new String[]{
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/Arial.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/System/Library/Fonts/Supplemental/Arial.ttf"};

        for (String path : candidates) {
            File f = new File(path);
            if (f.exists()) return PDType0Font.load(doc, f);
        }
        
        String res = bold ? "/fonts/ArialBold.ttf" : "/fonts/Arial.ttf";
        // Lưu ý: Nếu bạn dùng Java 8, hãy sửa 'var' thành 'java.io.InputStream'
        java.io.InputStream stream = BaoCaoPDF.class.getResourceAsStream(res);
        if (stream != null) return PDType0Font.load(doc, stream);

        throw new IOException(
            "Kh\u00f4ng t\u00ecm th\u1EA5y font ti\u1EBFng Vi\u1EC7t.\n"
            + "Vui l\u00f2ng \u0111\u1EB7t arial.ttf v\u00e0 arialbd.ttf v\u00e0o C:/Windows/Fonts/");
    }

    // ═════════════════════════════════ HELPERS ════════════════════════════════

    private static void fillRect(PDPageContentStream cs,
            float x, float y, float w, float h, float[] rgb) throws IOException {
        cs.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private static void drawRect(PDPageContentStream cs,
            float x, float y, float w, float h, float[] rgb, float lw) throws IOException {
        cs.setStrokingColor(rgb[0], rgb[1], rgb[2]);
        cs.setLineWidth(lw);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    private static void drawHLine(PDPageContentStream cs,
            float x, float y, float w, float[] rgb, float lw) throws IOException {
        cs.setStrokingColor(rgb[0], rgb[1], rgb[2]);
        cs.setLineWidth(lw);
        cs.moveTo(x, y);
        cs.lineTo(x + w, y);
        cs.stroke();
    }

    private static void drawText(PDPageContentStream cs,
            PDType0Font font, float size, float x, float y,
            float[] rgb, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private static void drawTextCentered(PDPageContentStream cs,
            PDType0Font font, float size, float pageW, float y,
            float[] rgb, String text) throws IOException {
        float tw = textWidth(font, size, text);
        drawText(cs, font, size, (pageW - tw) / 2f, y, rgb, text);
    }

    private static float textWidth(PDType0Font font, float size, String text)
            throws IOException {
        return font.getStringWidth(text) / 1000f * size;
    }

    private static void drawLabelValue(PDPageContentStream cs,
            PDType0Font fReg, PDType0Font fBold,
            float x, float y, float size,
            String label, String value) throws IOException {
        drawText(cs, fReg, size, x, y, MTEXT, label);
        float lw = textWidth(fReg, size, label);
        drawText(cs, fBold, size, x + lw, y, DTEXT, value);
    }

    private static float drawSectionHeader(PDPageContentStream cs,
            PDType0Font fBold, float x, float y, float w, String title)
            throws IOException {
        float barH = 22f;
        float[] bg = {
            NAVY[0] * 0.12f + 0.88f,
            NAVY[1] * 0.12f + 0.88f,
            NAVY[2] * 0.12f + 0.88f
        };
        fillRect(cs, x, y - barH + 2f, w, barH, bg);
        fillRect(cs, x, y - barH + 2f, 5f, barH, NAVY);
        drawText(cs, fBold, 9.5f, x + 12f, y - 10f, NAVY, title);
        return y - barH;
    }

    private static void drawStatCard(PDPageContentStream cs,
            PDType0Font fReg, PDType0Font fBold,
            float x, float y, float w, float h,
            String label, String value,
            float[] accent, float[] bg) throws IOException {
        fillRect(cs, x, y, w, h, bg);
        float[] bdr = {
            accent[0] * 0.55f + 0.45f,
            accent[1] * 0.55f + 0.45f,
            accent[2] * 0.55f + 0.45f
        };
        drawRect(cs, x, y, w, h, bdr, 0.6f);
        fillRect(cs, x, y, 5f, h, accent);
        drawText(cs, fReg, 8f, x + 12f, y + h - 18f, MTEXT, label);
        drawHLine(cs, x + 12f, y + h - 24f, w - 22f, MGRAY, 0.4f);
        float valSize = value.length() > 14 ? 13.5f : 17f;
        float valY    = y + (h / 2f) - valSize * 0.35f;
        drawText(cs, fBold, valSize, x + 12f, valY, accent, value);
    }
}