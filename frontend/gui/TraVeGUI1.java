package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * TraVeGUI1 – Màn hình xác nhận kết quả trả vé.
 * REDESIGN: Gộp + Highlight hoàn tiền
 * - Banner "Tiền hoàn lại cho bạn" nổi bật màu xanh
 * - Gộp 2 card (info + chi phí) thành 1 card "Thông tin vé + chi phí"
 * - Padding đồng bộ với các GUI khác
 */
public class TraVeGUI1 extends JPanel {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final Color BORDER       = new Color(210, 215, 224);
    private static final Color OK_FG        = new Color(30, 120, 60);
    private static final Color OK_BG        = new Color(236, 252, 240);
    private static final Color OK_BORDER    = new Color(160, 215, 175);
    private static final Color HOAN_BG      = new Color(220, 252, 231);
    private static final Color HOAN_BORDER  = new Color(134, 239, 172);
    private static final int   FIELD_H      = 28;

    // ── Dữ liệu tĩnh ──────────────────────────────────────────────────────
    private static String   s_maVe    = "";
    private static String[] s_data    = new String[0];
    private static String   s_phi     = "";
    private static String   s_hoanLai = "";

    public static void setDonTra(String maVe, String[] data, String phi, String hoanLai) {
        s_maVe    = maVe;
        s_data    = data.clone();
        s_phi     = phi;
        s_hoanLai = hoanLai;
    }

    // ── State ──────────────────────────────────────────────────────────────
    private final AppFrame appFrame;

    // Banner highlight
    private JLabel lbBannerHoan;
    private JLabel lbBannerSub;

    // Fields trong card gộp
    private JLabel valMaVe, valChuyen, valGaDi, valGaDen,
            valNgayGio, valLoai, valTongTien, valPhi;

    // ── Constructor ────────────────────────────────────────────────────────
    public TraVeGUI1(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);

        // ── ĐỒNG BỘ PADDING VỚI CÁC MÀN HÌNH KHÁC ──
        JPanel pnlPage = new JPanel(new BorderLayout(0, 10));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(
                0,
                GuiTheme.PAGE_PAD_LEFT,
                GuiTheme.PAGE_PAD_BOTTOM,
                GuiTheme.PAGE_PAD_LEFT
        ));

        // Stack chứa các thông tin (căn giữa)
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        stack.add(buildAlertBox());
        stack.add(Box.createVerticalStrut(10));
        stack.add(buildHoanBanner());
        stack.add(Box.createVerticalStrut(10));
        stack.add(buildMergedCard());

        JScrollPane outer = new JScrollPane(stack);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);

        // Đẩy phần cuộn vào giữa, nút bấm xuống đáy
        pnlPage.add(outer, BorderLayout.CENTER);
        pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);

        add(pnlPage, BorderLayout.CENTER);
    }

    public void refresh() {
        valMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        valChuyen .setText(safe(s_data, 0));
        valGaDi   .setText(safe(s_data, 1));
        valGaDen  .setText(safe(s_data, 2));
        valNgayGio.setText(safe(s_data, 4));
        valLoai   .setText(safe(s_data, 3));

        // ── ĐÃ SỬA LỖI TÍNH TIỀN ──
        long tongTien = 0;
        try {
            // Lấy số lượng (xóa mọi ký tự không phải số)
            long soLuong = Long.parseLong(s_data[5].replaceAll("[^0-9]", ""));

            // Lấy đơn giá (cắt phần thập phân .00 trước, sau đó xóa ký tự rác)
            String cleanGia = s_data[7].split("\\.")[0].replaceAll("[^0-9]", "");
            long donGia = Long.parseLong(cleanGia);

            tongTien = soLuong * donGia;
        } catch (Exception ignored) {}

        valTongTien.setText(tongTien > 0 ? fmtTien(tongTien) : "—");
        valPhi     .setText(s_phi.isEmpty() ? "—" : s_phi);

        // Banner
        lbBannerHoan.setText(s_hoanLai.isEmpty() ? "—" : s_hoanLai);
        lbBannerSub .setText(buildSubText());

        revalidate();
        repaint();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildAlertBox() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(OK_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(OK_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        // Thay BoxLayout.X_AXIS bằng BorderLayout để dễ căn giữa
        p.setLayout(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Đã xóa biến 'icon' chứa ký tự "ℹ"

        JLabel msg = new JLabel("Vui lòng kiểm tra lại thông tin...");
        msg.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13)); // In đậm một chút cho đẹp
        msg.setForeground(OK_FG);
        msg.setHorizontalAlignment(SwingConstants.CENTER); // Căn giữa nội dung

        p.add(msg, BorderLayout.CENTER);

        // Không dùng fixAlignAndMax nữa vì BorderLayout đã tự lo việc dàn trang
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private JPanel buildHoanBanner() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HOAN_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(HOAN_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lbTitle = new JLabel("Tiền hoàn lại cho bạn");
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lbTitle.setForeground(OK_FG);
        lbTitle.setAlignmentX(CENTER_ALIGNMENT);

        lbBannerHoan = new JLabel("—");
        lbBannerHoan.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 28));
        lbBannerHoan.setForeground(OK_FG);
        lbBannerHoan.setAlignmentX(CENTER_ALIGNMENT);
        lbBannerHoan.setHorizontalAlignment(SwingConstants.CENTER);

        lbBannerSub = new JLabel(" ");
        lbBannerSub.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbBannerSub.setForeground(new Color(60, 150, 90));
        lbBannerSub.setAlignmentX(CENTER_ALIGNMENT);

        p.add(lbTitle);
        p.add(Box.createVerticalStrut(4));
        p.add(lbBannerHoan);
        p.add(Box.createVerticalStrut(2));
        p.add(lbBannerSub);
        fixAlignAndMax(p);
        return p;
    }

    private JPanel buildMergedCard() {
        JPanel card = buildCard("Thông tin vé + chi phí");

        valMaVe    = infoLabel(GuiTheme.TEXT);
        valChuyen  = infoLabel(GuiTheme.TEXT);
        valGaDi    = infoLabel(GuiTheme.TEXT);
        valGaDen   = infoLabel(GuiTheme.TEXT);
        valNgayGio = infoLabel(GuiTheme.TEXT);
        valLoai    = infoLabel(GuiTheme.TEXT);
        valTongTien= infoLabel(GuiTheme.TEXT);
        valPhi     = infoLabel(new Color(160, 100, 0));

        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 10));
        grid.setOpaque(false);

        grid.add(infoCell("Mã vé",       valMaVe));
        grid.add(infoCell("Chuyến tàu",  valChuyen));
        grid.add(infoCell("Ga đi",       valGaDi));
        grid.add(infoCell("Ga đến",      valGaDen));

        grid.add(infoCell("Ngày/Giờ KH", valNgayGio));
        grid.add(infoCell("Loại vé",     valLoai));
        grid.add(infoCell("Tổng tiền vé",valTongTien));
        grid.add(infoCell("Phí trả vé",  valPhi));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setOpaque(false);
        // Thêm MatteBorder phía trên giống màn hình khác
        p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JButton btnBack = makeSecondaryButton("← Quay lại", 130, 34);
        btnBack.addActionListener(e -> appFrame.showCard("tra-ve"));

        JButton btnDone = makeNavyButton("Xác nhận trả vé", 150, 34);
        btnDone.addActionListener(e -> handleDone());

        p.add(btnBack);
        p.add(btnDone);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOGIC
    // ══════════════════════════════════════════════════════════════════════

    private String buildSubText() {
        StringBuilder sb = new StringBuilder();
        if (s_phi.contains("%")) {
            int idx = s_phi.indexOf('%');
            if (idx > 0) {
                String pct = s_phi.substring(0, idx).trim();
                String digits = pct.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) sb.append("Phí trả: ").append(digits).append("%");
            }
        } else if (!s_phi.isBlank() && !s_phi.equals("—")) {
            sb.append("Phí trả: ").append(s_phi);
        }
        sb.append("  ·  Chuyển trong 3–5 ngày làm việc");
        return sb.toString();
    }

    private void handleDone() {
        int choice = JOptionPane.showConfirmDialog(this,
                "<html><div style='padding:6px'>" +
                        "<b>Xác nhận trả vé " + s_maVe + "?</b><br><br>" +
                        "Phí trả: <b>" + s_phi + "</b><br>" +
                        "Tiền hoàn lại: <b>" + s_hoanLai + "</b><br>" +
                        "Tiền sẽ được hoàn trong 3–5 ngày làm việc." +
                        "</div></html>",
                "Xác nhận trả vé",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) return;

        JOptionPane.showMessageDialog(this,
                "<html><div style='text-align:center;padding:8px'>" +
                        "<b style='font-size:16px;color:#1e7840'>Trả vé thành công!</b><br><br>" +
                        "Mã vé <b>" + s_maVe + "</b> đã được xử lý trả.<br>" +
                        "Tiền hoàn <b>" + s_hoanLai + "</b> sẽ được chuyển trong 3–5 ngày làm việc." +
                        "</div></html>",
                "Hoàn tất", JOptionPane.PLAIN_MESSAGE);

        // TODO: Cập nhật CSDL trạng thái vé thành "DA_HUY" và Insert vào DonDoiTraVe tại đây.

        appFrame.showCard("doi-tra");
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private JLabel infoLabel(Color color) {
        JLabel lb = new JLabel("—");
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lb.setForeground(color);
        lb.setOpaque(true);
        lb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        lb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(4, 8, 4, 8)));
        lb.setPreferredSize(new Dimension(0, FIELD_H));
        lb.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return lb;
    }

    private JPanel infoCell(String label, JLabel value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lb = new JLabel(label);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(GuiTheme.SUB_TEXT);
        lb.setAlignmentX(LEFT_ALIGNMENT);

        value.setAlignmentX(LEFT_ALIGNMENT);

        p.add(lb);
        p.add(Box.createVerticalStrut(4));
        p.add(value);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return p;
    }

    private JPanel buildCard(String titleText) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel lbTitle = new JLabel(titleText);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbTitle.setForeground(GuiTheme.TEXT);
        lbTitle.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(lbTitle, BorderLayout.NORTH);
        return card;
    }

    private JButton makeNavyButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? GuiTheme.NAVY_DARK
                        : getModel().isRollover() ? GuiTheme.NAVY_HOVER
                          : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeSecondaryButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? new Color(220, 225, 235)
                        : getModel().isRollover() ? new Color(235, 239, 246)
                          : new Color(240, 243, 248);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(GuiTheme.TEXT);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static void fixAlignAndMax(JPanel p) {
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 20));
    }

    private static String fmtTien(long amount) {
        return String.format("%,d đ", amount).replace(",", ".");
    }

    private static String safe(String[] arr, int idx) {
        return (arr != null && idx < arr.length && arr[idx] != null) ? arr[idx] : "—";
    }
}