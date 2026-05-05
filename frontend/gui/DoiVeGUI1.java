package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * DoiVeGUI1 – Màn hình xác nhận đổi vé (bước 2).
 * REDESIGN: Cân bằng 2 cột
 *  - Gộp lệ phí vào card so sánh (phần dưới)
 *  - Tóm tắt thay đổi ngắn gọn (SE8→SE6, Ghế 5→Ghế 12)
 *  - 2 cột vé cũ / vé mới cân bằng ngang nhau (50/50)
 */
public class DoiVeGUI1 extends JPanel {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final Color BORDER    = new Color(210, 215, 224);
    private static final Color OK_FG     = new Color(30, 120, 60);
    private static final Color OK_BG     = new Color(236, 252, 240);
    private static final Color OK_BORDER = new Color(160, 215, 175);
    private static final Color NEW_FG    = new Color(37, 69, 121); // NAVY
    private static final int   FIELD_H   = 26;

    // ── Dữ liệu tĩnh ──────────────────────────────────────────────────────
    private static String   s_maVe      = "";
    private static String[] s_data      = new String[0];
    private static String   s_chuyenMoi = "";
    private static String   s_ngayMoi   = "";
    private static String   s_gheMoi    = "";

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

    // Vé cũ
    private JLabel valMaVe, valChuyen, valGaDi, valGaDen, valNgayGio, valLoai;
    // Vé mới
    private JLabel valChuyenMoi, valGaDiMoi, valGaDenMoi, valNgayMoi, valLoaiMoi, valGheMoi;
    // Tóm tắt thay đổi
    private JLabel lbThayDoi;

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

        page.add(buildSuccessBox());
        page.add(Box.createVerticalStrut(10));
        page.add(buildCompareCard());
        page.add(Box.createVerticalStrut(13));
        page.add(buildButtonRow());
        page.add(Box.createVerticalGlue());

        JScrollPane outer = new JScrollPane(page);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);
        add(outer, BorderLayout.CENTER);
    }

    public void refresh() {
        valMaVe    .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        valChuyen  .setText(safe(s_data, 0));
        valGaDi    .setText(safe(s_data, 1));
        valGaDen   .setText(safe(s_data, 2));
        valLoai    .setText(safe(s_data, 3));
        valNgayGio .setText(safe(s_data, 4));

        valChuyenMoi.setText(s_chuyenMoi.isEmpty() ? "—" : s_chuyenMoi);
        valGaDiMoi  .setText(safe(s_data, 1));
        valGaDenMoi .setText(safe(s_data, 2));
        valLoaiMoi  .setText(safe(s_data, 3));
        valNgayMoi  .setText(s_ngayMoi.isEmpty()   ? "—" : s_ngayMoi);
        valGheMoi   .setText(s_gheMoi.isEmpty()    ? "—" : s_gheMoi);

        lbThayDoi.setText(buildThayDoi());

        revalidate();
        repaint();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ══════════════════════════════════════════════════════════════════════

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
        p.setBorder(new EmptyBorder(10, 16, 10, 16));
        p.setAlignmentX(LEFT_ALIGNMENT);
        // FIX: hardcode height thay vì dùng getPreferredSize() trước khi add vào hierarchy
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel icon = new JLabel("ℹ");
        icon.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
        icon.setForeground(OK_FG);

        JLabel msg = new JLabel(
                "Thông tin hợp lệ. Lệ phí 30.000 đ / vé · Thu tại quầy.");
        msg.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        msg.setForeground(OK_FG);

        p.add(icon);
        p.add(Box.createHorizontalStrut(10));
        p.add(msg);
        return p;
    }

    private JPanel buildCompareCard() {
        JPanel card = buildCard("So sánh vé cũ → vé mới");

        // ── Khởi tạo labels ──
        valMaVe     = fieldLabel(GuiTheme.SUB_TEXT);
        valChuyen   = fieldLabel(GuiTheme.SUB_TEXT);
        valGaDi     = fieldLabel(GuiTheme.SUB_TEXT);
        valGaDen    = fieldLabel(GuiTheme.SUB_TEXT);
        valNgayGio  = fieldLabel(GuiTheme.SUB_TEXT);
        valLoai     = fieldLabel(GuiTheme.SUB_TEXT);

        valChuyenMoi = fieldLabel(NEW_FG);
        valGaDiMoi   = fieldLabel(GuiTheme.TEXT);
        valGaDenMoi  = fieldLabel(GuiTheme.TEXT);
        valLoaiMoi   = fieldLabel(GuiTheme.TEXT);
        valNgayMoi   = fieldLabel(NEW_FG);
        valGheMoi    = fieldLabel(NEW_FG);

        JPanel colOld = buildColumn("Vé hiện tại (cũ)", GuiTheme.SUB_TEXT,
                new String[]{"Mã vé", "Chuyến tàu", "Ga đi", "Ga đến", "Ngày/Giờ KH", "Loại vé"},
                new JLabel[]{valMaVe, valChuyen, valGaDi, valGaDen, valNgayGio, valLoai});

        JLabel arrow = new JLabel("→");
        arrow.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 24));
        arrow.setForeground(NEW_FG);
        arrow.setHorizontalAlignment(SwingConstants.CENTER);
        arrow.setVerticalAlignment(SwingConstants.CENTER);

        JPanel colNew = buildColumn("Vé đổi sang (mới)", NEW_FG,
                new String[]{"Chuyến mới", "Ga đi", "Ga đến", "Loại vé", "Ngày/Giờ mới", "Ghế mới"},
                new JLabel[]{valChuyenMoi, valGaDiMoi, valGaDenMoi, valLoaiMoi, valNgayMoi, valGheMoi});

        // GridBagLayout để 2 cột thực sự 50/50
        JPanel twoCol = new JPanel(new GridBagLayout());
        twoCol.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        gc.gridx = 0; gc.weightx = 1; gc.insets = new Insets(0, 0, 0, 0);
        twoCol.add(colOld, gc);

        gc.gridx = 1; gc.weightx = 0; gc.insets = new Insets(0, 8, 0, 8);
        twoCol.add(arrow, gc);

        gc.gridx = 2; gc.weightx = 1; gc.insets = new Insets(0, 0, 0, 0);
        twoCol.add(colNew, gc);

        // ── Dải lệ phí + tóm tắt thay đổi ──
        lbThayDoi = new JLabel("—");
        lbThayDoi.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbThayDoi.setForeground(NEW_FG);

        JPanel feeStrip = new JPanel(new GridLayout(1, 2, 20, 0));
        feeStrip.setOpaque(false);
        feeStrip.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(10, 0, 0, 0)));

        JPanel feeLeft = new JPanel();
        feeLeft.setLayout(new BoxLayout(feeLeft, BoxLayout.Y_AXIS));
        feeLeft.setOpaque(false);
        JLabel lbFeeTitle = new JLabel("Lệ phí đổi vé");
        lbFeeTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbFeeTitle.setForeground(GuiTheme.SUB_TEXT);
        JLabel lbFeeVal = new JLabel("30.000 đ / vé  ·  Thu tại quầy");
        lbFeeVal.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbFeeVal.setForeground(new Color(160, 80, 0));
        feeLeft.add(lbFeeTitle);
        feeLeft.add(Box.createVerticalStrut(2));
        feeLeft.add(lbFeeVal);

        JPanel feeRight = new JPanel();
        feeRight.setLayout(new BoxLayout(feeRight, BoxLayout.Y_AXIS));
        feeRight.setOpaque(false);
        JLabel lbChangeTitle = new JLabel("Thay đổi");
        lbChangeTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbChangeTitle.setForeground(GuiTheme.SUB_TEXT);
        feeRight.add(lbChangeTitle);
        feeRight.add(Box.createVerticalStrut(2));
        feeRight.add(lbThayDoi);

        feeStrip.add(feeLeft);
        feeStrip.add(feeRight);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(twoCol);
        content.add(Box.createVerticalStrut(10));
        content.add(feeStrip);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);

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

    private String buildThayDoi() {
        String oldChuyen = safe(s_data, 0);
        String oldGhe    = safe(s_data, 6);
        StringBuilder sb = new StringBuilder();
        if (!oldChuyen.equals("—") && !s_chuyenMoi.isEmpty())
            sb.append(oldChuyen).append(" → ").append(s_chuyenMoi);
        if (!oldGhe.equals("—") && !s_gheMoi.isEmpty()) {
            if (sb.length() > 0) sb.append("  ·  ");
            sb.append("Ghế ").append(oldGhe).append(" → Ghế ").append(s_gheMoi);
        }
        return sb.length() > 0 ? sb.toString() : "—";
    }

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
                        "Mã vé <b>" + s_maVe + "</b> đã đổi sang <b>" + s_chuyenMoi + "</b>.<br>" +
                        "Ngày: <b>" + s_ngayMoi + "</b>  —  Ghế: <b>" + s_gheMoi + "</b><br><br>" +
                        "Lệ phí <b>30.000 đ</b> sẽ thu khi đến quầy." +
                        "</div></html>",
                "Hoàn tất", JOptionPane.PLAIN_MESSAGE);

        appFrame.showCard("doi-tra");
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildColumn(String title, Color titleColor,
                               String[] labels, JLabel[] values) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbTitle.setForeground(titleColor);
        lbTitle.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbTitle);
        p.add(Box.createVerticalStrut(8));

        for (int i = 0; i < labels.length; i++) {
            JLabel lb = new JLabel(labels[i]);
            lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
            lb.setForeground(GuiTheme.SUB_TEXT);
            lb.setAlignmentX(LEFT_ALIGNMENT);
            p.add(lb);
            p.add(Box.createVerticalStrut(2));

            values[i].setAlignmentX(LEFT_ALIGNMENT);
            values[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
            p.add(values[i]);
            p.add(Box.createVerticalStrut(6));
        }
        return p;
    }

    private JLabel fieldLabel(Color color) {
        JLabel lb = new JLabel("—");
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lb.setForeground(color);
        lb.setOpaque(true);
        lb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        lb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(2, 6, 2, 6)));
        lb.setPreferredSize(new Dimension(0, FIELD_H));
        lb.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return lb;
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

    private static String safe(String[] arr, int idx) {
        return (arr != null && idx < arr.length && arr[idx] != null) ? arr[idx] : "—";
    }
}