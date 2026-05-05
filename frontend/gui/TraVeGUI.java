package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * TraVeGUI – Bước 2 của flow trả vé.
 * Flow: DoiTraGUI (chọn vé) → TraVeGUI (xem vé + tính phí) → TraVeGUI1 (xác nhận)
 *
 * KHÔNG có search bar, note box, hay table.
 * Nhận vé được chọn qua TraVeGUI.setVeDuocChon(maVe, data).
 *
 * Nghiệp vụ trả vé:
 *   Vé cá nhân: ≥ 48h → phí 10% | 12–<48h → phí 20% | < 12h → không hoàn
 *   Vé nhóm   : ≥ 72h → phí 20% | 24–<72h → phí 30% | < 24h → không hoàn
 */
public class TraVeGUI extends JPanel {

    private static final Color BORDER  = new Color(210, 215, 224);
    private static final Color WARN_FG = new Color(180, 60, 0);
    private static final Color OK_FG   = new Color(30, 120, 60);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Dữ liệu vé nhận từ DoiTraGUI ───────────────────────────────────────
    // data: [chuyenTau, gaDi, gaDen, loaiVe, ngayGioKH, soLuong, trangThai, giaTien]
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
    private JTextField        tfTongTien, tfPhi, tfHoanLai;
    private JLabel            lbTrangThai;
    private JComboBox<String> cbLyDo;
    private JLabel            lbWarning;
    private JButton           btnXacNhan;

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

        page.add(buildHeader());
        page.add(Box.createVerticalStrut(14));
        page.add(buildCurrentInfoCard());
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

    /** AppFrame gọi khi showCard("tra-ve") */
    public void refresh() {
        fillCurrentInfo();
        calcFee();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel title = new JLabel("TRẢ VÉ TÀU");
        title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, GuiTheme.PAGE_TITLE_SIZE));
        title.setForeground(GuiTheme.TEXT);

        JLabel sub = new JLabel("Kiểm tra thông tin vé và xác nhận yêu cầu trả vé bên dưới.");
        sub.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, GuiTheme.PAGE_SUBTITLE_SIZE));
        sub.setForeground(GuiTheme.SUB_TEXT);

        p.add(title, BorderLayout.NORTH);
        p.add(sub,   BorderLayout.SOUTH);
        setStretch(p);
        return p;
    }

    private JPanel buildCurrentInfoCard() {
        JPanel card = buildCard("Thông tin vé cần trả");

        tfMaVe    = readField(); tfChuyen  = readField();
        tfGaDi    = readField(); tfGaDen   = readField();
        tfNgayGio = readField(); tfLoai    = readField();
        tfGhe     = readField(); tfSoLuong = readField();
        tfGia     = readField();

        JPanel grid = new JPanel(new GridLayout(3, 4, 12, 10));
        grid.setOpaque(false);

        // Hàng 1
        grid.add(fieldBox("Mã vé",       tfMaVe));
        grid.add(fieldBox("Chuyến tàu",  tfChuyen));
        grid.add(fieldBox("Ga đi",       tfGaDi));
        grid.add(fieldBox("Ga đến",      tfGaDen));
        // Hàng 2
        grid.add(fieldBox("Ngày/Giờ KH", tfNgayGio));
        grid.add(fieldBox("Loại vé",     tfLoai));
        grid.add(fieldBox("Số ghế",      tfGhe));
        grid.add(fieldBox("Số lượng",    tfSoLuong));
        // Hàng 3
        grid.add(fieldBox("Đơn giá",     tfGia));

        // Ô điều kiện trả — nổi bật
        lbTrangThai = new JLabel("—");
        lbTrangThai.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbTrangThai.setForeground(GuiTheme.SUB_TEXT);
        JPanel ttCell = new JPanel(new BorderLayout(0, 4));
        ttCell.setOpaque(false);
        JLabel ttLabel = new JLabel("Điều kiện trả");
        ttLabel.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        ttLabel.setForeground(GuiTheme.SUB_TEXT);
        ttCell.add(ttLabel,     BorderLayout.NORTH);
        ttCell.add(lbTrangThai, BorderLayout.CENTER);
        grid.add(ttCell);

        grid.add(new JLabel());
        grid.add(new JLabel());

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFeeCard() {
        JPanel card = buildCard("Thông tin hoàn trả");

        tfTongTien = readField();
        tfPhi      = readField();
        tfHoanLai  = readField();
        tfHoanLai.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tfHoanLai.setForeground(OK_FG);

        cbLyDo = new JComboBox<>(new String[]{"Bận việc", "Ốm", "Thay đổi kế hoạch", "Khác"});
        cbLyDo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        cbLyDo.setBackground(Color.WHITE);

        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        grid.add(fieldBox("Tổng tiền vé",  tfTongTien));
        grid.add(fieldBox("Phí trả vé",    tfPhi));
        grid.add(fieldBox("Tiền hoàn lại", tfHoanLai));
        grid.add(fieldBox("Lý do trả vé",  cbLyDo));

        lbWarning = new JLabel(" ");
        lbWarning.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbWarning.setForeground(WARN_FG);
        lbWarning.setBorder(new EmptyBorder(6, 0, 0, 0));

        card.add(grid,      BorderLayout.CENTER);
        card.add(lbWarning, BorderLayout.SOUTH);
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

    private void fillCurrentInfo() {
        if (s_data.length < 8) { clearCurrentInfo(); return; }
        // data: [chuyenTau[0], gaDi[1], gaDen[2], loaiVe[3], ngayGioKH[4], soLuong[5], trangThai[6], giaTien[7]]
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
            lbTrangThai.setText("Chưa có vé được chọn");
            lbTrangThai.setForeground(GuiTheme.SUB_TEXT);
            tfTongTien.setText("—"); tfPhi.setText("—"); tfHoanLai.setText("—");
            btnXacNhan.setEnabled(false);
            return;
        }

        boolean nhom = s_data[3].toLowerCase().contains("nhóm");
        long    gio  = tinhGio(s_data[4]);

        long soLuong = 1, donGia = 0;
        try { soLuong = Long.parseLong(s_data[5].trim()); } catch (Exception ignored) {}
        try { donGia  = Long.parseLong(s_data[7].trim()); } catch (Exception ignored) {}
        long tongTien = soLuong * donGia;
        tfTongTien.setText(String.format("%,d VNĐ", tongTien).replace(",", "."));

        int     phiPct;
        boolean hopLe;
        String  trangThai;

        if (nhom) {
            if      (gio >= 72) { phiPct = 20; hopLe = true;  trangThai = "Hợp lệ — phí 20%"; }
            else if (gio >= 24) { phiPct = 30; hopLe = true;  trangThai = "Hợp lệ — phí 30%"; }
            else                { phiPct = -1; hopLe = false; trangThai = "Quá hạn — dưới 24h (vé nhóm)"; }
        } else {
            if      (gio >= 48) { phiPct = 10; hopLe = true;  trangThai = "Hợp lệ — phí 10%"; }
            else if (gio >= 12) { phiPct = 20; hopLe = true;  trangThai = "Hợp lệ — phí 20%"; }
            else                { phiPct = -1; hopLe = false; trangThai = "Quá hạn — dưới 12h (vé cá nhân)"; }
        }

        lbTrangThai.setText(trangThai);
        lbTrangThai.setForeground(hopLe ? OK_FG : new Color(180, 30, 30));

        if (hopLe) {
            long hoan = Math.round(tongTien * (1.0 - phiPct / 100.0));
            tfPhi    .setText(phiPct + "%  (" + String.format("%,d VNĐ", tongTien - hoan).replace(",", ".") + ")");
            tfHoanLai.setText(String.format("%,d VNĐ", hoan).replace(",", "."));
            tfHoanLai.setForeground(OK_FG);
            lbWarning.setText(" ");
            btnXacNhan.setEnabled(true);
        } else {
            tfPhi    .setText("Không hoàn tiền");
            tfHoanLai.setText("0 VNĐ");
            tfHoanLai.setForeground(new Color(180, 30, 30));
            lbWarning.setText("⛔  " + (nhom
                    ? "Vé nhóm phải trả trước giờ tàu ít nhất 24 giờ."
                    : "Vé cá nhân phải trả trước giờ tàu ít nhất 12 giờ.")
                    + "  Hiện còn: " + Math.max(gio, 0) + " giờ.");
            lbWarning.setForeground(WARN_FG);
            btnXacNhan.setEnabled(false);
        }
    }

    private void clearCurrentInfo() {
        for (JTextField tf : new JTextField[]{
                tfMaVe, tfChuyen, tfGaDi, tfGaDen,
                tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia})
            if (tf != null) tf.setText("—");
    }

    private void handleTraVe() {
        TraVeGUI1.setDonTra(s_maVe, s_data, tfPhi.getText(), tfHoanLai.getText());
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

    private JTextField readField() {
        JTextField tf = new JTextField("—");
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tf.setEditable(false);
        tf.setForeground(GuiTheme.TEXT);
        tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(2, 6, 2, 6)));
        return tf;
    }

    private JPanel fieldBox(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lb = new JLabel(label);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(GuiTheme.SUB_TEXT);
        p.add(lb,   BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
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

    private JButton navyBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled()            ? new Color(180, 190, 205)
                        : getModel().isPressed()  ? GuiTheme.NAVY_DARK
                          : getModel().isRollover() ? GuiTheme.NAVY_HOVER
                            :                          GuiTheme.NAVY;
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
                          :                          new Color(240, 243, 248);
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