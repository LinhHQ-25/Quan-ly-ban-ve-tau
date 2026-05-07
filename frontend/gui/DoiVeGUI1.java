package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * DoiVeGUI1 – Xác nhận đổi vé.
 * Đã fix: Bỏ icon, hiển thị đầy đủ chi tiết từng hàng (Mã, Chuyến, Toa, Ga, Loại, Ghế...)
 * và gióng thẳng hàng tuyệt đối bằng GridBagLayout.
 */
public class DoiVeGUI1 extends JPanel {

    private static final Color BORDER    = new Color(210, 215, 224);
    private static final Color OK_FG     = new Color(30, 120, 60);
    private static final Color OK_BG     = new Color(236, 252, 240);
    private static final Color OK_BORDER = new Color(160, 215, 175);
    private static final Color NEW_FG    = GuiTheme.NAVY;

    private static String   s_maVe      = "";
    private static String[] s_data      = new String[0];
    private static String   s_chuyenMoi = "";
    private static String   s_ngayMoi   = "";
    private static String   s_gheMoi    = "";

    public static void setDonDoi(String maVe, String[] data,
                                 String chuyenMoi, String ngayMoi, String gheMoi) {
        s_maVe = maVe; s_data = data.clone();
        s_chuyenMoi = chuyenMoi; s_ngayMoi = ngayMoi; s_gheMoi = gheMoi;
    }

    private final AppFrame appFrame;
    private JLabel valMaVe, valChuyen, valToa, valGaDi, valGaDen, valLoai, valGhe, valNgayGio;
    private JLabel valMaVeMoi, valChuyenMoi, valToaMoi, valGaDiMoi, valGaDenMoi, valLoaiMoi, valGheMoi, valNgayMoi;
    private JLabel lbThayDoi;

    public DoiVeGUI1(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);

        // ── Padding đồng bộ ──
        JPanel pnlPage = new JPanel(new BorderLayout(0, 10));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(
                0,
                GuiTheme.PAGE_PAD_LEFT,
                GuiTheme.PAGE_PAD_BOTTOM,
                GuiTheme.PAGE_PAD_LEFT
        ));

        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setOpaque(false);

        centerWrapper.add(buildSuccessBox());
        centerWrapper.add(Box.createVerticalStrut(10));
        centerWrapper.add(buildCompareCard());

        pnlPage.add(centerWrapper,      BorderLayout.CENTER);
        pnlPage.add(buildButtonRow(),   BorderLayout.SOUTH);

        add(pnlPage, BorderLayout.CENTER);
    }

    public void refresh() {
        String oldGheStr = safe(s_data, 6);

        // Cột Vé Cũ
        valMaVe    .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        valChuyen  .setText(safe(s_data, 0));
        valToa     .setText(extractToa(oldGheStr));
        valGaDi    .setText(safe(s_data, 1));
        valGaDen   .setText(safe(s_data, 2));
        valLoai    .setText(safe(s_data, 3));
        valGhe     .setText(extractGhe(oldGheStr));
        valNgayGio .setText(safe(s_data, 4));

        // Cột Vé Mới (Giữ nguyên Mã vé, Ga, Loại vé)
        valMaVeMoi  .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        valChuyenMoi.setText(s_chuyenMoi.isEmpty() ? "—" : s_chuyenMoi);
        valToaMoi   .setText(extractToa(s_gheMoi));
        valGaDiMoi  .setText(safe(s_data, 1));
        valGaDenMoi .setText(safe(s_data, 2));
        valLoaiMoi  .setText(safe(s_data, 3));
        valGheMoi   .setText(extractGhe(s_gheMoi));
        valNgayMoi  .setText(s_ngayMoi.isEmpty() ? "—" : s_ngayMoi);

        lbThayDoi.setText(buildThayDoi());
        revalidate(); repaint();
    }

    // ══════════════════════════════════════════════════════════════════════
    // BUILDERS
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
        p.setLayout(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Bỏ Icon, chữ Căn giữa hoàn toàn
        JLabel msg = new JLabel("Thông tin hợp lệ. Lệ phí 30.000 đ / vé · Thu tại quầy.");
        msg.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        msg.setForeground(OK_FG);
        msg.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(msg, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCompareCard() {
        JPanel card = buildCard("So sánh chi tiết vé");

        // Khởi tạo fields Cũ (Màu xám)
        valMaVe      = fieldLabel(GuiTheme.SUB_TEXT);
        valChuyen    = fieldLabel(GuiTheme.SUB_TEXT);
        valToa       = fieldLabel(GuiTheme.SUB_TEXT);
        valGaDi      = fieldLabel(GuiTheme.SUB_TEXT);
        valGaDen     = fieldLabel(GuiTheme.SUB_TEXT);
        valLoai      = fieldLabel(GuiTheme.SUB_TEXT);
        valGhe       = fieldLabel(GuiTheme.SUB_TEXT);
        valNgayGio   = fieldLabel(GuiTheme.SUB_TEXT);

        // Khởi tạo fields Mới (Màu xanh Navy cho phần bị thay đổi)
        valMaVeMoi   = fieldLabel(GuiTheme.TEXT); // Mã giữ nguyên
        valChuyenMoi = fieldLabel(NEW_FG);
        valToaMoi    = fieldLabel(NEW_FG);
        valGaDiMoi   = fieldLabel(GuiTheme.TEXT); // Ga giữ nguyên
        valGaDenMoi  = fieldLabel(GuiTheme.TEXT); // Ga giữ nguyên
        valLoaiMoi   = fieldLabel(GuiTheme.TEXT); // Loại giữ nguyên
        valGheMoi    = fieldLabel(NEW_FG);
        valNgayMoi   = fieldLabel(NEW_FG);

        // Dùng GridBagLayout để căn chỉnh các cột thẳng tắp
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(0, 0, 15, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);

        // Hàng Tiêu Đề
        gbc.gridy = 0;
        gbc.gridx = 1; gbc.weightx = 0.4; grid.add(headerLabel("VÉ HIỆN TẠI (CŨ)", GuiTheme.SUB_TEXT), gbc);
        gbc.gridx = 2; gbc.weightx = 0.05; grid.add(new JLabel(""), gbc); // Cột chứa mũi tên
        gbc.gridx = 3; gbc.weightx = 0.4; grid.add(headerLabel("VÉ ĐỔI SANG (MỚI)", NEW_FG), gbc);

        // Các Hàng Dữ Liệu Tách Rời (Ghi hết ra)
        addGridRow(grid, 1, "Mã vé",       valMaVe,    valMaVeMoi);
        addGridRow(grid, 2, "Chuyến tàu",  valChuyen,  valChuyenMoi);
        addGridRow(grid, 3, "Ga đi",       valGaDi,    valGaDiMoi);
        addGridRow(grid, 4, "Ga đến",      valGaDen,   valGaDenMoi);
        addGridRow(grid, 5, "Loại vé",     valLoai,    valLoaiMoi);
        addGridRow(grid, 6, "Ghế",         valGhe,     valGheMoi);
        addGridRow(grid, 7, "Ngày/Giờ KH", valNgayGio, valNgayMoi);

        // Khối chứa lệ phí & Tóm tắt thay đổi (Phần dưới cùng của Card)
        lbThayDoi = new JLabel("—");
        lbThayDoi.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbThayDoi.setForeground(NEW_FG);

        JPanel feeStrip = new JPanel(new GridLayout(1, 2, 20, 0));
        feeStrip.setOpaque(false);
        feeStrip.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(12, 0, 0, 0)));

        JPanel feeLeft = new JPanel();
        feeLeft.setLayout(new BoxLayout(feeLeft, BoxLayout.Y_AXIS));
        feeLeft.setOpaque(false);
        JLabel lbFT = new JLabel("Lệ phí đổi vé");
        lbFT.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbFT.setForeground(GuiTheme.SUB_TEXT);
        JLabel lbFV = new JLabel("30.000 đ / vé  ·  Thu tại quầy");
        lbFV.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbFV.setForeground(new Color(160, 80, 0));
        feeLeft.add(lbFT); feeLeft.add(Box.createVerticalStrut(4)); feeLeft.add(lbFV);

        JPanel feeRight = new JPanel();
        feeRight.setLayout(new BoxLayout(feeRight, BoxLayout.Y_AXIS));
        feeRight.setOpaque(false);
        JLabel lbCT = new JLabel("Thay đổi chính");
        lbCT.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbCT.setForeground(GuiTheme.SUB_TEXT);
        feeRight.add(lbCT); feeRight.add(Box.createVerticalStrut(4)); feeRight.add(lbThayDoi);

        feeStrip.add(feeLeft); feeStrip.add(feeRight);

        // Gộp Grid và FeeStrip vào Content
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(grid, BorderLayout.CENTER);
        content.add(feeStrip, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void addGridRow(JPanel grid, int y, String title, JLabel valOld, JLabel valNew) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.gridy = y;

        // Cột Tiêu đề dòng
        gbc.gridx = 0; gbc.weightx = 0.15;
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(GuiTheme.SUB_TEXT);
        grid.add(lblTitle, gbc);

        // Cột Giá trị cũ
        gbc.gridx = 1; gbc.weightx = 0.4;
        grid.add(valOld, gbc);

        // Cột Mũi tên →
        gbc.gridx = 2; gbc.weightx = 0.05;
        JLabel arrow = new JLabel("→");
        arrow.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
        arrow.setForeground(BORDER);
        arrow.setHorizontalAlignment(SwingConstants.CENTER);
        grid.add(arrow, gbc);

        // Cột Giá trị mới
        gbc.gridx = 3; gbc.weightx = 0.4;
        grid.add(valNew, gbc);
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setOpaque(false);
        p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JButton btnBack = makeSecondaryButton("← Quay lại", 130, 34);
        btnBack.addActionListener(e -> appFrame.showCard("doi-ve"));

        JButton btnConfirm = makeNavyButton("Xác nhận đổi vé", 160, 34);
        btnConfirm.addActionListener(e -> handleConfirm());

        p.add(btnBack); p.add(btnConfirm);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOGIC & DATA PARSING
    // ══════════════════════════════════════════════════════════════════════

    private String extractToa(String fullStr) {
        if (fullStr == null || fullStr.equals("—")) return "—";
        if (fullStr.contains("-")) {
            return fullStr.split("-")[0].trim();
        }
        return "—";
    }

    private String extractGhe(String fullStr) {
        if (fullStr == null || fullStr.equals("—")) return "—";
        if (fullStr.contains("-")) {
            return fullStr.split("-")[1].trim();
        }
        return fullStr;
    }

    private String buildThayDoi() {
        String oldChuyen = safe(s_data, 0);
        String oldGhe = extractGhe(safe(s_data, 6));
        String newGhe = extractGhe(s_gheMoi);

        StringBuilder sb = new StringBuilder();
        if (!oldChuyen.equals("—") && !s_chuyenMoi.isEmpty() && !oldChuyen.equals(s_chuyenMoi))
            sb.append(oldChuyen).append(" → ").append(s_chuyenMoi);

        if (!oldGhe.equals("—") && !newGhe.isEmpty() && !newGhe.equals("—")) {
            if (sb.length() > 0) sb.append("  ·  ");
            sb.append("Ghế ").append(oldGhe).append(" → ").append(newGhe);
        }
        return sb.length() > 0 ? sb.toString() : "Chỉ đổi lịch, không đổi số hiệu";
    }

    private void handleConfirm() {
        int ch = JOptionPane.showConfirmDialog(this,
                "<html><div style='padding:6px'><b>Xác nhận đổi vé " + s_maVe + "?</b><br><br>"
                        + "Chuyến mới: <b>" + s_chuyenMoi + "</b><br>"
                        + "Ngày/Giờ mới: <b>" + s_ngayMoi + "</b><br>"
                        + "Ghế mới: <b>" + s_gheMoi + "</b><br>"
                        + "Lệ phí: <b>30.000 đ</b> (thu tại quầy)</div></html>",
                "Xác nhận đổi vé", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ch != JOptionPane.OK_OPTION) return;

        JOptionPane.showMessageDialog(this,
                "<html><div style='text-align:center;padding:10px'>"
                        + "<b style='font-size:16px;color:#1e7840'>✔  Đổi vé thành công!</b><br><br>"
                        + "Mã vé <b>" + s_maVe + "</b> đã được áp dụng cho chuyến <b>" + s_chuyenMoi + "</b>.<br>"
                        + "Ngày: <b>" + s_ngayMoi + "</b>  —  Ghế mới: <b>" + s_gheMoi + "</b><br><br>"
                        + "Lệ phí <b>30.000 đ</b> sẽ thu khi đến quầy.</div></html>",
                "Hoàn tất", JOptionPane.PLAIN_MESSAGE);

        appFrame.showCard("doi-tra");
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private JLabel headerLabel(String title, Color color) {
        JLabel lb = new JLabel(title);
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lb.setForeground(color);
        lb.setHorizontalAlignment(SwingConstants.CENTER);
        return lb;
    }

    private JLabel fieldLabel(Color color) {
        JLabel lb = new JLabel("—");
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lb.setForeground(color);
        lb.setOpaque(true);
        lb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        lb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false), new EmptyBorder(5, 10, 5, 10)));
        return lb;
    }

    private JPanel buildCard(String titleText) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
        JLabel lbTitle = new JLabel(titleText);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
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
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
                        : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
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
                Color bg = getModel().isPressed() ? new Color(220,225,235)
                        : getModel().isRollover() ? new Color(235,239,246) : new Color(240,243,248);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(GuiTheme.TEXT);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
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