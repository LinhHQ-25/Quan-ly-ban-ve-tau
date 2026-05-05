package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * TraVeGUI – Bước 2 của flow trả vé.
 * REDESIGN: Compact + Nổi bật
 *  - 3 stat cards nổi bật (Tổng tiền vé / Phí trả / Tiền hoàn lại)
 *  - Điều kiện trả chuyển vào card thông tin vé (xoá ô trống dư)
 *  - Lý do trả vé + Điều kiện xếp cùng hàng bên dưới stat cards
 */
public class TraVeGUI extends JPanel {

    // ── Colors ──────────────────────────────────────────────────────────────
    private static final Color BORDER    = new Color(210, 215, 224);
    private static final Color WARN_FG   = new Color(180, 60, 0);
    private static final Color OK_FG     = new Color(30, 120, 60);
    private static final Color RED_FG    = new Color(180, 30, 30);
    private static final Color STAT_OK_BG     = new Color(240, 253, 244);
    private static final Color STAT_OK_BORDER = new Color(134, 239, 172);
    private static final Color STAT_RED_BG    = new Color(255, 241, 242);
    private static final Color STAT_RED_BORDER= new Color(252, 165, 165);
    private static final Color STAT_FEE_BG    = new Color(254, 249, 235);
    private static final Color STAT_FEE_BORDER= new Color(251, 207, 100);
    private static final int   FIELD_H  = 28;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Static data từ DoiTraGUI ────────────────────────────────────────────
    private static String   s_maVe = "";
    private static String[] s_data = new String[0];

    public static void setVeDuocChon(String maVe, String[] data) {
        s_maVe = maVe;
        s_data = data.clone();
    }

    // ── State ───────────────────────────────────────────────────────────────
    private final AppFrame appFrame;

    private JTextField tfMaVe, tfChuyen, tfGaDi, tfGaDen,
            tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia;

    // Stat cards
    private JLabel lbStatTong, lbStatPhi, lbStatHoan;
    private JPanel statPanelPhi, statPanelHoan;

    // Bottom row
    private JComboBox<String> cbLyDo;
    private JLabel lbDieuKien;
    private JLabel lbWarning;
    private JButton btnXacNhan;

    // ── Constructor ─────────────────────────────────────────────────────────
    public TraVeGUI(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(
                GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT,
                GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        page.add(buildInfoCard());
        page.add(Box.createVerticalStrut(10));
        page.add(buildFeeCard());
        page.add(Box.createVerticalStrut(14));
        page.add(buildButtonRow());

        JScrollPane outer = new JScrollPane(page);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);
        add(outer, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        fillInfo();
        calcFee();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ══════════════════════════════════════════════════════════════════════

    /** Card thông tin vé – 4 cột x 2 hàng + Đơn giá + Điều kiện trả */
    private JPanel buildInfoCard() {
        JPanel card = buildCard("Thông tin vé cần trả");

        tfMaVe    = readField(); tfChuyen  = readField();
        tfGaDi    = readField(); tfGaDen   = readField();
        tfNgayGio = readField(); tfLoai    = readField();
        tfGhe     = readField(); tfSoLuong = readField();
        tfGia     = readField();

        // Điều kiện trả – hiển thị ngay trong card info (không để trống)
        lbDieuKien = new JLabel("—");
        lbDieuKien.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbDieuKien.setForeground(GuiTheme.SUB_TEXT);
        lbDieuKien.setAlignmentX(LEFT_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(3, 4, 12, 10));
        grid.setOpaque(false);

        // Hàng 1
        grid.add(fieldBox("Mã vé",       tfMaVe));
        grid.add(fieldBox("Chuyến tàu",  tfChuyen));
        grid.add(fieldBox("Ga đi",       tfGaDi));
        grid.add(fieldBox("Ga đến",      tfGaDen));
        // Hàng 2
        grid.add(fieldBox("Ngày/Giờ",   tfNgayGio));
        grid.add(fieldBox("Loại vé",     tfLoai));
        grid.add(fieldBox("Số ghế",      tfGhe));
        grid.add(fieldBox("Số lượng",    tfSoLuong));
        // Hàng 3: Đơn giá + Điều kiện trả (chiếm 2 ô), còn 2 ô trống
        grid.add(fieldBox("Đơn giá",     tfGia));
        grid.add(labelBox("Điều kiện trả", lbDieuKien));
        grid.add(new JLabel());
        grid.add(new JLabel());

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /** Card hoàn trả – 3 stat cards nổi bật + hàng Lý do / Warning */
    private JPanel buildFeeCard() {
        JPanel card = buildCard("Thông tin hoàn trả");

        // ── 3 stat cards ──
        lbStatTong = statValueLabel(GuiTheme.TEXT, Font.BOLD, 18);
        lbStatPhi  = statValueLabel(new Color(180, 60, 0), Font.BOLD, 18);
        lbStatHoan = statValueLabel(OK_FG, Font.BOLD, 18);

        JPanel statRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statRow.setOpaque(false);

        JPanel statTong = buildStatCard("Tổng tiền vé",   lbStatTong,
                new Color(248, 250, 252), BORDER,              GuiTheme.TEXT);
        statPanelPhi  = buildStatCard("Phí trả (10%)",  lbStatPhi,
                STAT_FEE_BG, STAT_FEE_BORDER, new Color(160, 100, 0));
        statPanelHoan = buildStatCard("Tiền hoàn lại",  lbStatHoan,
                STAT_OK_BG, STAT_OK_BORDER,  OK_FG);

        statRow.add(statTong);
        statRow.add(statPanelPhi);
        statRow.add(statPanelHoan);

        // ── Hàng Lý do + Warning ──
        cbLyDo = new JComboBox<>(new String[]{"Bận việc", "Ốm", "Thay đổi kế hoạch", "Khác"});
        cbLyDo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        cbLyDo.setBackground(Color.WHITE);
        cbLyDo.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        cbLyDo.setPreferredSize(new Dimension(0, FIELD_H));

        lbWarning = new JLabel(" ");
        lbWarning.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbWarning.setForeground(WARN_FG);

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        bottomRow.add(fieldBox("Lý do trả vé", cbLyDo));
        bottomRow.add(labelBox("Điều kiện", lbWarning));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(statRow);
        content.add(bottomRow);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        setStretch(p);

        JButton btnBack = secondaryBtn("← Quay lại", 130, 34);
        btnBack.addActionListener(e -> appFrame.showCard("doi-tra"));

        btnXacNhan = navyBtn("Yêu cầu trả vé →", 160, 34);
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(e -> handleTraVe());

        p.add(btnBack);
        p.add(btnXacNhan);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOGIC
    // ══════════════════════════════════════════════════════════════════════

    private void fillInfo() {
        if (s_data.length < 8) { clearInfo(); return; }
        tfMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        tfChuyen .setText(s_data[0]);
        tfGaDi   .setText(s_data[1]);
        tfGaDen  .setText(s_data[2]);
        tfLoai   .setText(s_data[3]);
        tfNgayGio.setText(s_data[4]);
        tfSoLuong.setText(s_data[5]);
        tfGhe    .setText(s_data[6]);
        try {
            long gia = Long.parseLong(s_data[7]);
            tfGia.setText(String.format("%,d đ", gia).replace(",", "."));
        } catch (Exception e) { tfGia.setText(s_data[7]); }
    }

    private void calcFee() {
        if (s_data.length < 8 || s_maVe.isEmpty()) {
            lbDieuKien.setText("Chưa có vé được chọn");
            lbDieuKien.setForeground(GuiTheme.SUB_TEXT);
            lbStatTong.setText("—"); lbStatPhi.setText("—"); lbStatHoan.setText("—");
            lbWarning.setText(" ");
            btnXacNhan.setEnabled(false);
            return;
        }

        boolean nhom = s_data[3].toLowerCase().contains("nhóm");
        long    gio  = tinhGio(s_data[4]);

        long soLuong = 1, donGia = 0;
        try { soLuong = Long.parseLong(s_data[5].trim()); } catch (Exception ignored) {}
        try { donGia  = Long.parseLong(s_data[7].trim()); } catch (Exception ignored) {}
        long tongTien = soLuong * donGia;

        lbStatTong.setText(fmtTien(tongTien));

        int     phiPct;
        boolean hopLe;
        String  dieuKien;

        if (nhom) {
            if      (gio >= 72) { phiPct = 20; hopLe = true;  dieuKien = "Hợp lệ — phí 20% (vé nhóm)"; }
            else if (gio >= 24) { phiPct = 30; hopLe = true;  dieuKien = "Hợp lệ — phí 30% (vé nhóm)"; }
            else                { phiPct = -1; hopLe = false; dieuKien = "Quá hạn — dưới 24h (vé nhóm)"; }
        } else {
            if      (gio >= 48) { phiPct = 10; hopLe = true;  dieuKien = "Hợp lệ — phí 10%"; }
            else if (gio >= 12) { phiPct = 20; hopLe = true;  dieuKien = "Hợp lệ — phí 20%"; }
            else                { phiPct = -1; hopLe = false; dieuKien = "Quá hạn — dưới 12h"; }
        }

        lbDieuKien.setText(dieuKien);
        lbDieuKien.setForeground(hopLe ? OK_FG : RED_FG);

        if (hopLe) {
            long phiTien = Math.round(tongTien * phiPct / 100.0);
            long hoan    = tongTien - phiTien;

            // Cập nhật label tiêu đề phí
            updateStatTitle(statPanelPhi, "Phí trả (" + phiPct + "%)");

            lbStatPhi .setText(fmtTien(phiTien));
            lbStatHoan.setText(fmtTien(hoan));

            lbWarning.setText(" ");
            btnXacNhan.setEnabled(true);

            // Màu bình thường
            lbStatPhi .setForeground(new Color(160, 100, 0));
            lbStatHoan.setForeground(OK_FG);
        } else {
            updateStatTitle(statPanelPhi, "Phí trả");
            lbStatPhi .setText("Không hoàn");
            lbStatPhi .setForeground(RED_FG);
            lbStatHoan.setText("0 đ");
            lbStatHoan.setForeground(RED_FG);

            lbWarning.setText("⛔  Vé " + (nhom ? "nhóm < 24h" : "cá nhân < 12h")
                    + " — không được hoàn tiền. Còn: " + Math.max(gio, 0) + "h.");
            lbWarning.setForeground(WARN_FG);
            btnXacNhan.setEnabled(false);
        }
    }

    private void clearInfo() {
        for (JTextField tf : new JTextField[]{
                tfMaVe, tfChuyen, tfGaDi, tfGaDen,
                tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia})
            if (tf != null) tf.setText("—");
    }

    private void handleTraVe() {
        String phiText  = lbStatPhi.getText();
        String hoanText = lbStatHoan.getText();
        TraVeGUI1.setDonTra(s_maVe, s_data, phiText, hoanText);
        appFrame.showCard("tra-ve-step-2");
    }

    private static long tinhGio(String ngayGio) {
        try {
            return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(ngayGio, FMT));
        } catch (Exception e) { return -1; }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

    /** Stat card hình chữ nhật bo góc với tiêu đề + giá trị lớn */
    private JPanel buildStatCard(String title, JLabel valueLb, Color bg, Color border, Color titleColor) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbTitle.setForeground(titleColor);
        lbTitle.setAlignmentX(CENTER_ALIGNMENT);
        lbTitle.setName("stat-title"); // để tìm lại khi cập nhật

        valueLb.setAlignmentX(CENTER_ALIGNMENT);
        valueLb.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lbSub = new JLabel(" ");
        lbSub.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
        lbSub.setForeground(titleColor);
        lbSub.setAlignmentX(CENTER_ALIGNMENT);

        p.add(lbTitle);
        p.add(Box.createVerticalStrut(6));
        p.add(valueLb);
        p.add(lbSub);
        return p;
    }

    /** Cập nhật tiêu đề stat card theo phiPct */
    private void updateStatTitle(JPanel statCard, String newTitle) {
        for (Component c : statCard.getComponents()) {
            if (c instanceof JLabel lb && "stat-title".equals(lb.getName())) {
                lb.setText(newTitle);
                break;
            }
        }
    }

    private JLabel statValueLabel(Color color, int style, int size) {
        JLabel lb = new JLabel("—");
        lb.setFont(GuiTheme.font("Segoe UI", style, size));
        lb.setForeground(color);
        lb.setHorizontalAlignment(SwingConstants.CENTER);
        return lb;
    }

    private JTextField readField() {
        JTextField tf = new JTextField("—");
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tf.setEditable(false);
        tf.setForeground(GuiTheme.TEXT);
        tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(2, 6, 2, 6)));
        tf.setPreferredSize(new Dimension(0, FIELD_H));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return tf;
    }

    private JPanel fieldBox(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lb = new JLabel(label);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(GuiTheme.SUB_TEXT);
        lb.setAlignmentX(LEFT_ALIGNMENT);

        comp.setAlignmentX(LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));

        p.add(lb);
        p.add(Box.createVerticalStrut(4));
        p.add(comp);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return p;
    }

    /** Ô label + JLabel (không phải JTextField) */
    private JPanel labelBox(String labelText, JLabel value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lb = new JLabel(labelText);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(GuiTheme.SUB_TEXT);
        lb.setAlignmentX(LEFT_ALIGNMENT);

        value.setAlignmentX(LEFT_ALIGNMENT);
        value.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));

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
        lbTitle.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(lbTitle, BorderLayout.NORTH);
        return card;
    }

    private static String fmtTien(long amount) {
        return String.format("%,d đ", amount).replace(",", ".");
    }

    private JButton navyBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled()           ? new Color(180, 190, 205)
                        : getModel().isPressed()  ? GuiTheme.NAVY_DARK
                          : getModel().isRollover() ? GuiTheme.NAVY_HOVER
                            :                           GuiTheme.NAVY;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton secondaryBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? new Color(220, 225, 235)
                        : getModel().isRollover() ? new Color(235, 239, 246)
                          :                           new Color(240, 243, 248);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(GuiTheme.TEXT);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static void setStretch(JPanel p) {
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 16));
    }
}