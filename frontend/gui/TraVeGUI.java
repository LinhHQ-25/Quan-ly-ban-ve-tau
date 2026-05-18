package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import connect_DB.Connect_DB;

public class TraVeGUI extends JPanel {
    private static final Color BORDER         = new Color(210, 215, 224);
    private static final Color WARN_FG        = new Color(180, 60, 0);
    private static final Color OK_FG          = new Color(30, 120, 60);
    private static final Color RED_FG         = new Color(180, 30, 30);
    private static final Color STAT_OK_BG     = new Color(240, 253, 244);
    private static final Color STAT_OK_BORDER = new Color(134, 239, 172);
    private static final Color STAT_FEE_BG    = new Color(254, 249, 235);
    private static final Color STAT_FEE_BORDER= new Color(251, 207, 100);
    private static final int   FIELD_H        = 36;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static String   s_maVe = "";
    private static String[] s_data = new String[0];

    public static void setVeDuocChon(String maVe, String[] data) {
        s_maVe = maVe; s_data = data.clone();
    }

    private final AppFrame appFrame;
    private JTextField tfMaVe, tfChuyen, tfGaDi, tfGaDen,
            tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia;
    private JLabel lbStatTong, lbStatPhi, lbStatHoan;
    private JPanel statPanelPhi;

    private JComboBox<String> cbLyDo;
    private JTextField txtLyDoKhac; // Ô nhập liệu khi chọn "Khác"

    private JLabel lbDieuKien, lbWarning;
    private JButton btnXacNhan;

    public TraVeGUI(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);

        JPanel pnlPage = new JPanel(new BorderLayout(0, 10));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(
                0,
                GuiTheme.PAGE_PAD_LEFT,
                GuiTheme.PAGE_PAD_BOTTOM,
                GuiTheme.PAGE_PAD_LEFT
        ));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.add(buildInfoCard());
        stack.add(Box.createVerticalStrut(15));
        stack.add(buildFeeCard());

        JPanel centerAlign = new JPanel(new BorderLayout());
        centerAlign.setOpaque(false);
        centerAlign.add(stack, BorderLayout.NORTH);

        pnlPage.add(centerAlign,      BorderLayout.CENTER);
        pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);
        add(pnlPage, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() { fillInfo(); calcFee(); }

    // BUILDERS
    private JPanel buildInfoCard() {
        JPanel card = buildCard("Thông tin vé cần trả");
        tfMaVe    = readField(); tfChuyen  = readField();
        tfGaDi    = readField(); tfGaDen   = readField();
        tfNgayGio = readField(); tfLoai    = readField();
        tfGhe     = readField(); tfSoLuong = readField();
        tfGia     = readField();

        lbDieuKien = new JLabel("—");
        lbDieuKien.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbDieuKien.setForeground(GuiTheme.SUB_TEXT);
        lbDieuKien.setAlignmentX(LEFT_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(3, 4, 12, 10));
        grid.setOpaque(false);
        grid.add(fieldBox("Mã vé",        tfMaVe));
        grid.add(fieldBox("Chuyến tàu",   tfChuyen));
        grid.add(fieldBox("Ga đi",        tfGaDi));
        grid.add(fieldBox("Ga đến",       tfGaDen));
        grid.add(fieldBox("Ngày/Giờ",     tfNgayGio));
        grid.add(fieldBox("Loại vé",      tfLoai));
        grid.add(fieldBox("Số ghế",       tfGhe));
        grid.add(fieldBox("Số lượng",     tfSoLuong));
        grid.add(fieldBox("Đơn giá",      tfGia));
        grid.add(labelBox("Điều kiện trả", lbDieuKien));
        grid.add(new JLabel()); grid.add(new JLabel());
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFeeCard() {
        JPanel card = buildCard("Thông tin hoàn trả");
        lbStatTong = statLabel(GuiTheme.TEXT,              Font.BOLD, 20);
        lbStatPhi  = statLabel(new Color(180, 60, 0),      Font.BOLD, 20);
        lbStatHoan = statLabel(OK_FG,                      Font.BOLD, 20);

        JPanel statRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statRow.setOpaque(false);
        statRow.add(buildStatCard("Tổng tiền vé",  lbStatTong, new Color(248,250,252), BORDER,GuiTheme.TEXT));
        statPanelPhi = buildStatCard("Phí trả (10%)", lbStatPhi, STAT_FEE_BG, STAT_FEE_BORDER, new Color(160,100,0));
        statRow.add(statPanelPhi);
        statRow.add(buildStatCard("Tiền hoàn lại", lbStatHoan, STAT_OK_BG,  STAT_OK_BORDER,  OK_FG));

        // --- TẠO COMBOBOX VÀ TEXTBOX LÝ DO ---
        cbLyDo = new JComboBox<>(new String[]{"Bận việc", "Ốm", "Thay đổi kế hoạch", "Khác"});
        cbLyDo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cbLyDo.setBackground(Color.WHITE);
        cbLyDo.setPreferredSize(new Dimension(160, FIELD_H)); // Cố định độ rộng combobox

        txtLyDoKhac = new JTextField();
        txtLyDoKhac.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtLyDoKhac.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false), new EmptyBorder(4, 10, 4, 10)));
        txtLyDoKhac.setVisible(false); // Ban đầu ẩn text box đi

        JPanel pnlLyDoContainer = new JPanel(new BorderLayout(10, 0));
        pnlLyDoContainer.setOpaque(false);
        pnlLyDoContainer.add(cbLyDo, BorderLayout.WEST);
        pnlLyDoContainer.add(txtLyDoKhac, BorderLayout.CENTER);

        // Sự kiện: Ẩn/Hiện Text Box khi đổi giá trị Combobox
        cbLyDo.addActionListener(e -> {
            boolean isKhac = "Khác".equals(cbLyDo.getSelectedItem());
            txtLyDoKhac.setVisible(isKhac);
            if (isKhac) {
                txtLyDoKhac.requestFocus();
            } else {
                txtLyDoKhac.setText(""); // Xóa text nếu không chọn "Khác"
            }
            pnlLyDoContainer.revalidate();
            pnlLyDoContainer.repaint();
        });

        lbWarning = new JLabel(" ");
        lbWarning.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbWarning.setForeground(WARN_FG);

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(20, 0, 5, 0));
        bottomRow.add(fieldBox("Lý do trả vé", pnlLyDoContainer)); // Đưa cả Container vào thay vì chỉ cbLyDo
        bottomRow.add(labelBox("Điều kiện",    lbWarning));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(statRow);
        content.add(bottomRow);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        p.setOpaque(false);
        p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JButton btnBack = secondaryBtn("Quay lại", 130, 38);
        btnBack.addActionListener(e -> appFrame.showCard("doi-tra"));

        btnXacNhan = navyBtn("Yêu cầu trả vé", 130, 38);
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(e -> handleTraVe());

        p.add(btnBack); p.add(btnXacNhan);
        return p;
    }

    // LOGIC
    private void fillInfo() {
        if (s_data.length < 8) { clearInfo(); return; }
        tfMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        tfChuyen .setText(s_data[0]); tfGaDi   .setText(s_data[1]);
        tfGaDen  .setText(s_data[2]); tfLoai   .setText(s_data[3]);
        tfNgayGio.setText(s_data[4]); tfSoLuong.setText(s_data[5]);
        tfGhe    .setText(s_data[6]);
        try {
            String cleanGia = s_data[7].split("\\.")[0].replaceAll("[^0-9]", "");
            tfGia.setText(String.format("%,d đ", Long.parseLong(cleanGia)).replace(",","."));
        } catch (Exception e) {
            tfGia.setText(s_data[7]);
        }
    }

    private void calcFee() {
        if (s_data.length < 8 || s_maVe.isEmpty()) {
            lbDieuKien.setText("Chưa có vé được chọn"); lbDieuKien.setForeground(GuiTheme.SUB_TEXT);
            lbStatTong.setText("—"); lbStatPhi.setText("—"); lbStatHoan.setText("—");
            lbWarning.setText(" "); btnXacNhan.setEnabled(false); return;
        }

        long gio = tinhGio(s_data[4]);
        long soLuong = 1, donGia = 0;
        try {
            soLuong = Long.parseLong(s_data[5].replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {}

        boolean nhom = soLuong >= 2; // Logic nhận diện vé nhóm dựa vào số lượng

        try {
            String cleanGia = s_data[7].split("\\.")[0];
            cleanGia = cleanGia.replaceAll("[^0-9]", "");
            donGia = Long.parseLong(cleanGia);
        } catch (Exception ignored) {}

        long tongTien = soLuong * donGia;
        lbStatTong.setText(fmtTien(tongTien));

        int phiPct; boolean hopLe; String dieuKien;
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
            updateStatTitle(statPanelPhi, "Phí trả (" + phiPct + "%)");
            lbStatPhi .setText(fmtTien(phiTien));
            lbStatHoan.setText(fmtTien(tongTien - phiTien));
            lbStatPhi .setForeground(new Color(160, 100, 0));
            lbStatHoan.setForeground(OK_FG);
            lbWarning.setText(" "); btnXacNhan.setEnabled(true);
        } else {
            updateStatTitle(statPanelPhi, "Phí trả");
            lbStatPhi .setText("Không hoàn"); lbStatPhi .setForeground(RED_FG);
            lbStatHoan.setText("0 đ");         lbStatHoan.setForeground(RED_FG);
            lbWarning.setText("  Vé " + (nhom ? "nhóm < 24h" : "cá nhân < 12h")
                    + " — không được hoàn tiền. Còn: " + Math.max(gio, 0) + "h.");
            lbWarning.setForeground(WARN_FG); btnXacNhan.setEnabled(false);
        }
    }

    private void clearInfo() {
        for (JTextField tf : new JTextField[]{tfMaVe,tfChuyen,tfGaDi,tfGaDen,tfNgayGio,tfLoai,tfGhe,tfSoLuong,tfGia})
            if (tf != null) tf.setText("—");
    }

    private void handleTraVe() {
        // Lấy lý do trả vé gửi qua màn hình sau nếu cần thiết
        String lyDo = cbLyDo.getSelectedItem().toString();
        if ("Khác".equals(lyDo) && !txtLyDoKhac.getText().trim().isEmpty()) {
            lyDo = txtLyDoKhac.getText().trim();
        }

        TraVeGUI1.setDonTra(s_maVe, s_data, lbStatPhi.getText(), lbStatHoan.getText());
        appFrame.showCard("tra-ve-step-2");
    }

    private static long tinhGio(String s) {
        try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(s, FMT)); }
        catch (Exception e) { return -1; }
    }

    // UI HELPERS
    private JPanel buildStatCard(String title, JLabel valueLb, Color bg, Color border, Color titleColor) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(border); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbTitle.setForeground(titleColor);
        lbTitle.setAlignmentX(CENTER_ALIGNMENT);
        lbTitle.setName("stat-title");

        valueLb.setAlignmentX(CENTER_ALIGNMENT);
        valueLb.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(lbTitle); p.add(Box.createVerticalStrut(8)); p.add(valueLb);
        return p;
    }

    private void updateStatTitle(JPanel statCard, String newTitle) {
        for (Component c : statCard.getComponents()) {
            if (c instanceof JLabel lb && "stat-title".equals(lb.getName())) {
                lb.setText(newTitle); break;
            }
        }
    }

    private JLabel statLabel(Color color, int style, int size) {
        JLabel lb = new JLabel("—");
        lb.setFont(GuiTheme.font("Segoe UI", style, size));
        lb.setForeground(color);
        lb.setHorizontalAlignment(SwingConstants.CENTER);
        return lb;
    }

    private JTextField readField() {
        JTextField tf = new JTextField("—");
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tf.setEditable(false); tf.setForeground(GuiTheme.TEXT);
        tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false), new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, FIELD_H));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return tf;
    }

    private JPanel fieldBox(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lb = new JLabel(label);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lb.setForeground(GuiTheme.SUB_TEXT);
        lb.setAlignmentX(LEFT_ALIGNMENT);
        comp.setAlignmentX(LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        p.add(lb); p.add(Box.createVerticalStrut(6)); p.add(comp);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        return p;
    }

    private JPanel labelBox(String labelText, JLabel value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lb = new JLabel(labelText);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lb.setForeground(GuiTheme.SUB_TEXT);
        lb.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        value.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        p.add(lb); p.add(Box.createVerticalStrut(6)); p.add(value);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        return p;
    }

    private JPanel buildCard(String titleText) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(20, 24, 20, 24)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel lbTitle = new JLabel(titleText);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbTitle.setForeground(GuiTheme.TEXT);
        lbTitle.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(lbTitle, BorderLayout.NORTH);
        return card;
    }

    private static String fmtTien(long amount) { return String.format("%,d đ", amount).replace(",", "."); }

    private JButton navyBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled() ? new Color(180,190,205)
                        : getModel().isPressed() ? GuiTheme.NAVY_DARK
                          : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY;
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);

                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth()-fm.stringWidth(txt))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
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
                Color bg = getModel().isPressed() ? new Color(220,225,235)
                        : getModel().isRollover() ? new Color(235,239,246) : new Color(240,243,248);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(GuiTheme.TEXT);

                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth()-fm.stringWidth(txt))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}