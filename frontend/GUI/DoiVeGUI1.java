package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * DoiVeGUI1 – Màn hình xác nhận đổi vé (bước 2).
 * Flow: DoiVeGUI → DoiVeGUI1
 *
 * Hiển thị so sánh vé cũ vs vé mới + lệ phí. Người dùng xác nhận để hoàn tất.
 * Dữ liệu được nhận qua setDonDoi() từ DoiVeGUI.
 * AppFrame gọi refresh() mỗi khi showCard("doi-ve-step-2").
 */
public class DoiVeGUI1 extends JPanel {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final Color BORDER    = new Color(210, 215, 224);
    private static final Color OK_FG     = new Color(30, 120, 60);
    private static final Color OK_BG     = new Color(236, 252, 240);
    private static final Color OK_BORDER = new Color(160, 215, 175);

    // ── Dữ liệu tĩnh nhận từ DoiVeGUI ────────────────────────────────────
    private static String   s_maVe       = "";
    private static String[] s_data       = new String[0];
    private static String   s_chuyenMoi  = "";
    private static String   s_ngayMoi    = "";
    private static String   s_gheMoi     = "";

    /** DoiVeGUI gọi hàm này trước khi showCard("doi-ve-step-2") */
    public static void setDonDoi(String maVe, String[] data,
                                 String chuyenMoi, String ngayMoi, String gheMoi) {
        s_maVe      = maVe;
        s_data      = data.clone();
        s_chuyenMoi = chuyenMoi;
        s_ngayMoi   = ngayMoi;
        s_gheMoi    = gheMoi;
    }

    // ── State ──────────────────────────────────────────────────────────────
    private final AppFrame appFrame;

    // Labels vé cũ (cập nhật qua refresh)
    private JLabel valMaVe, valChuyen, valGaDi, valGaDen, valNgayGio, valLoai, valSoLuong;
    // Labels vé mới (cập nhật qua refresh)
    private JLabel valChuyenMoi, valNgayMoi, valGheMoi;
    // Label mã vé trong fee card
    private JLabel valMaVeFee;


    // ── Constructor ────────────────────────────────────────────────────────
    public DoiVeGUI1(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(
                GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT,
                GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        page.add(buildHeader());
        page.add(Box.createVerticalStrut(12));
        page.add(buildSuccessBox());
        page.add(Box.createVerticalStrut(12));
        page.add(buildCompareCard());
        page.add(Box.createVerticalStrut(8));
        page.add(buildFeeCard());
        page.add(Box.createVerticalStrut(16));
        page.add(buildButtonRow());

        JScrollPane outer = new JScrollPane(page);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);
        add(outer, BorderLayout.CENTER);
    }

    /**
     * AppFrame gọi hàm này mỗi khi showCard("doi-ve-step-2").
     * Nạp lại tất cả dữ liệu từ DoiVeGUI vào các label hiển thị.
     */
    public void refresh() {
        // Vé cũ
        valMaVe    .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        valChuyen  .setText(safe(s_data, 0));
        valGaDi    .setText(safe(s_data, 1));
        valGaDen   .setText(safe(s_data, 2));
        valLoai    .setText(safe(s_data, 3));
        valNgayGio .setText(safe(s_data, 4));
        valSoLuong .setText(safe(s_data, 5));

        // Vé mới
        valChuyenMoi.setText(s_chuyenMoi.isEmpty() ? "—" : s_chuyenMoi);
        valNgayMoi  .setText(s_ngayMoi  .isEmpty() ? "—" : s_ngayMoi);
        valGheMoi   .setText(s_gheMoi   .isEmpty() ? "—" : s_gheMoi);

        // Fee card
        valMaVeFee.setText(s_maVe.isEmpty() ? "—" : s_maVe);

        revalidate();
        repaint();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel title = new JLabel("XÁC NHẬN ĐỔI VÉ");
        title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, GuiTheme.PAGE_TITLE_SIZE));
        title.setForeground(GuiTheme.TEXT);

        JLabel sub = new JLabel("Vui lòng kiểm tra lại thông tin vé cũ và vé mới trước khi xác nhận.");
        sub.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, GuiTheme.PAGE_SUBTITLE_SIZE));
        sub.setForeground(GuiTheme.SUB_TEXT);

        p.add(title, BorderLayout.NORTH);
        p.add(sub,   BorderLayout.SOUTH);
        fixAlignAndMax(p);
        return p;
    }

    /** Banner xanh nhắc kiểm tra */
    private JPanel buildSuccessBox() {
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
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel icon = new JLabel("ℹ");
        icon.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
        icon.setForeground(OK_FG);

        JLabel msg = new JLabel(
                "Thông tin hợp lệ. Sau khi xác nhận, lệ phí 30.000 đ sẽ được thu tại quầy.");
        msg.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        msg.setForeground(OK_FG);

        p.add(icon);
        p.add(msg);
        fixAlignAndMax(p);
        return p;
    }

    /** Card so sánh vé cũ → vé mới với mũi tên ở giữa */
    private JPanel buildCompareCard() {
        JPanel card = buildCard("So sánh vé cũ và vé mới");

        JPanel outer = new JPanel(new BorderLayout(16, 0));
        outer.setOpaque(false);

        // Khởi tạo labels vé cũ
        valMaVe    = valueLabel(GuiTheme.SUB_TEXT);
        valChuyen  = valueLabel(GuiTheme.SUB_TEXT);
        valGaDi    = valueLabel(GuiTheme.SUB_TEXT);
        valGaDen   = valueLabel(GuiTheme.SUB_TEXT);
        valNgayGio = valueLabel(GuiTheme.SUB_TEXT);
        valLoai    = valueLabel(GuiTheme.SUB_TEXT);
        valSoLuong = valueLabel(GuiTheme.SUB_TEXT);

        JPanel colOld = buildInfoColumn(
                "Vé hiện tại  (cũ)",
                new String[]{"Mã vé", "Chuyến tàu", "Ga đi", "Ga đến", "Ngày/Giờ KH", "Loại vé", "Số lượng"},
                new JLabel[]{valMaVe, valChuyen, valGaDi, valGaDen, valNgayGio, valLoai, valSoLuong},
                GuiTheme.SUB_TEXT
        );

        // Mũi tên
        JLabel arrow = new JLabel(" ");
        arrow.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 30));
        arrow.setForeground(GuiTheme.NAVY);
        arrow.setHorizontalAlignment(SwingConstants.CENTER);
        arrow.setVerticalAlignment(SwingConstants.CENTER);

        // Khởi tạo labels vé mới
        valChuyenMoi = valueLabel(GuiTheme.TEXT);
        valNgayMoi   = valueLabel(GuiTheme.TEXT);
        valGheMoi    = valueLabel(GuiTheme.TEXT);

        JPanel colNew = buildInfoColumn(
                "Vé đổi sang  (mới)",
                new String[]{"Chuyến mới", "Ngày/Giờ mới", "Ghế mới"},
                new JLabel[]{valChuyenMoi, valNgayMoi, valGheMoi},
                GuiTheme.NAVY
        );

        outer.add(colOld,  BorderLayout.WEST);
        outer.add(arrow,   BorderLayout.CENTER);
        outer.add(colNew,  BorderLayout.EAST);

        card.add(outer, BorderLayout.CENTER);
        return card;
    }

    /** Card lệ phí */
    private JPanel buildFeeCard() {
        JPanel card = buildCard("Lệ phí đổi vé");
        valMaVeFee = new JLabel("—");
        valMaVeFee.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        valMaVeFee.setForeground(GuiTheme.TEXT);
        valMaVeFee.setBackground(GuiTheme.SEARCH_FIELD_BG);
        valMaVeFee.setOpaque(true);
        valMaVeFee.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(2, 6, 2, 6))
        );
        valMaVeFee.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(1, 6, 1, 6)
        ));

        Dimension size = new Dimension(0, 22);
        valMaVeFee.setPreferredSize(size);
        valMaVeFee.setMinimumSize(size);
        valMaVeFee.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 0));
        grid.setOpaque(false);
        grid.add(infoCell("Mã vé",         valMaVeFee));
        grid.add(infoCell("Lệ phí đổi vé", makeStaticValueLabel("30.000 đ / vé")));
        grid.add(infoCell("Hình thức thu",  makeStaticValueLabel("Thu khi đổi tại quầy")));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        fixAlignAndMax(p);

        JButton btnBack = makeSecondaryButton("← Quay lại", 130, 34);
        btnBack.addActionListener(e -> appFrame.showCard("doi-ve"));

        JButton btnConfirm = makeNavyButton("Xác nhận đổi vé", 160, 34);
        btnConfirm.addActionListener(e -> handleConfirm());

        p.add(btnBack);
        p.add(btnConfirm);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOGIC
    // ══════════════════════════════════════════════════════════════════════

    private void handleConfirm() {
        int choice = JOptionPane.showConfirmDialog(this,
                "<html><div style='padding:6px'>" +
                        "<b>Xác nhận đổi vé " + s_maVe + "?</b><br><br>" +
                        "Chuyến mới: <b>" + s_chuyenMoi + "</b><br>" +
                        "Ngày/Giờ mới: <b>" + s_ngayMoi + "</b><br>" +
                        "Ghế mới: <b>" + s_gheMoi + "</b><br>" +
                        "Lệ phí: <b>30.000 đ</b> (thu tại quầy)" +
                        "</div></html>",
                "Xác nhận đổi vé",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) return;

        JOptionPane.showMessageDialog(this,
                "<html><div style='text-align:center;padding:10px'>" +
                        "<b style='font-size:16px;color:#1e7840'>✔  Đổi vé thành công!</b><br><br>" +
                        "Mã vé <b>" + s_maVe + "</b> đã được đổi sang chuyến <b>" + s_chuyenMoi + "</b>.<br>" +
                        "Ngày khởi hành mới: <b>" + s_ngayMoi + "</b> — Ghế: <b>" + s_gheMoi + "</b><br><br>" +
                        "Lệ phí <b>30.000 đ</b> sẽ được thu khi đến quầy lấy vé." +
                        "</div></html>",
                "Hoàn tất", JOptionPane.PLAIN_MESSAGE);

        appFrame.showCard("doi-tra");
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS – UI
    // ══════════════════════════════════════════════════════════════════════

    /** Tạo JLabel giá trị có thể cập nhật sau */
    private JLabel valueLabel(Color textColor) {
        JLabel lb = new JLabel("—");
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lb.setForeground(textColor);
        return lb;
    }

    /** Tạo JLabel giá trị tĩnh với viền (dùng trong fee card) */
    private JLabel makeStaticValueLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lb.setOpaque(true);
        lb.setBackground(GuiTheme.SEARCH_FIELD_BG);

        lb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(1, 6, 1, 6) // 🔥 giảm padding
        ));

        Dimension size = new Dimension(0, 22); // 🔥 giảm chiều cao xuống
        lb.setPreferredSize(size);
        lb.setMinimumSize(size);
        lb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22)); // 🔥 chặn không cho bị giãn

        return lb;
    }

    /** Cột thông tin label-value dùng JLabel[] */
    private JPanel buildInfoColumn(String title, String[] labels, JLabel[] values, Color titleColor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(260, 0));

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbTitle.setForeground(titleColor);
        lbTitle.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbTitle);
        p.add(Box.createVerticalStrut(8));

        for (int i = 0; i < labels.length; i++) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            row.setAlignmentX(LEFT_ALIGNMENT);

            JLabel lb = new JLabel(labels[i] + ":");
            lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
            lb.setForeground(GuiTheme.SUB_TEXT);
            lb.setPreferredSize(new Dimension(110, 20));

            row.add(lb,        BorderLayout.WEST);
            row.add(values[i], BorderLayout.CENTER);
            p.add(row);
            p.add(Box.createVerticalStrut(4));
        }
        return p;
    }

    /** Panel label trên / JLabel value dưới (dùng trong fee card) */
    private JPanel infoCell(String label, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel lb = new JLabel(label);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(GuiTheme.SUB_TEXT);

        p.add(lb,         BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCard(String titleText) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
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
                g2.drawString(txt,
                        (getWidth()  - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                g2.drawString(txt,
                        (getWidth()  - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static void fixAlignAndMax(JPanel p) {
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 20));
    }

    /** Lấy giá trị an toàn từ mảng, trả "—" nếu out-of-bounds */
    private static String safe(String[] arr, int idx) {
        return (arr != null && idx < arr.length && arr[idx] != null) ? arr[idx] : "—";
    }
}