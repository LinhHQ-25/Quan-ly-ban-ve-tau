package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TraVeGUI extends JPanel {
    private static final Color BORDER          = new Color(210, 215, 224);
    private static final Color NAVY            = new Color(28, 57, 110);
    private static final Color BG              = new Color(242, 247, 252);
    private static final Color WARN_FG         = new Color(180, 60, 0);
    private static final Color OK_FG           = new Color(30, 120, 60);
    private static final Color RED_FG          = new Color(180, 30, 30);
    private static final Color STAT_OK_BG      = new Color(240, 253, 244);
    private static final Color STAT_OK_BORDER  = new Color(134, 239, 172);
    private static final Color STAT_FEE_BG     = new Color(254, 249, 235);
    private static final Color STAT_FEE_BORDER = new Color(251, 207, 100);
    private static final Font  FONT_14         = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font  FONT_B14        = new Font("Segoe UI", Font.BOLD, 14);
    private static final int   FIELD_H         = 36;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static String   s_maVe = "";
    private static String[] s_data = new String[0];

    public static void setVeDuocChon(String maVe, String[] data) {
        s_maVe = maVe; s_data = data.clone();
    }

    private final AppFrame appFrame;
    private JTextField tfMaVe, tfChuyen, tfGaDi, tfGaDen, tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia;
    private JLabel     lbStatTong, lbStatPhi, lbStatHoan, lbDieuKien, lbWarning;
    private JPanel     statPanelPhi;
    private JComboBox<String> cbLyDo;
    private JTextField txtLyDoKhac;
    private JButton    btnXacNhan;

    public TraVeGUI(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel pnlPage = new JPanel(new BorderLayout(0, 0));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        pnlCenter.add(buildMainCard(), gbc);

        pnlPage.add(pnlCenter, BorderLayout.CENTER);
        pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);
        add(pnlPage, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() { fillInfo(); calcFee(); }

    // ===================== MAIN CARD =====================
    private JPanel buildMainCard() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setBackground(Color.WHITE);
        outer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(20, 24, 20, 24)));

        // Header
        JLabel lbTitle = new JLabel("Thông tin trả vé");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); lbTitle.setForeground(GuiTheme.TEXT);
        lbTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        outer.add(lbTitle, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // --- INFO GRID ---
        tfMaVe = readField(); tfChuyen  = readField(); tfGaDi  = readField(); tfGaDen   = readField();
        tfNgayGio = readField(); tfLoai = readField(); tfGhe   = readField(); tfSoLuong = readField();
        tfGia = readField();

        lbDieuKien = new JLabel("—");
        lbDieuKien.setFont(FONT_B14); lbDieuKien.setForeground(GuiTheme.SUB_TEXT);

        JPanel infoGrid = new JPanel(new GridLayout(3, 4, 16, 12));
        infoGrid.setOpaque(false);
        infoGrid.add(fieldBox("Mã vé",       tfMaVe));
        infoGrid.add(fieldBox("Mã chuyến",   tfChuyen));
        infoGrid.add(fieldBox("Ga đi",       tfGaDi));
        infoGrid.add(fieldBox("Ga đến",      tfGaDen));
        infoGrid.add(fieldBox("Ngày/Giờ KH", tfNgayGio));
        infoGrid.add(fieldBox("Loại vé",     tfLoai));
        infoGrid.add(fieldBox("Số ghế",      tfGhe));
        infoGrid.add(fieldBox("Số lượng",    tfSoLuong));
        infoGrid.add(fieldBox("Đơn giá",     tfGia));
        infoGrid.add(labelBox("Điều kiện",   lbDieuKien));
        infoGrid.add(new JLabel()); infoGrid.add(new JLabel());
        content.add(infoGrid);
        content.add(Box.createVerticalStrut(20));

        // --- STAT CARDS ---
        lbStatTong = statLabel(GuiTheme.TEXT,         Font.BOLD, 20);
        lbStatPhi  = statLabel(new Color(180, 60, 0), Font.BOLD, 20);
        lbStatHoan = statLabel(OK_FG,                 Font.BOLD, 20);

        JPanel statRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statRow.setOpaque(false); statRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statRow.add(buildStatCard("Tổng tiền vé",  lbStatTong, new Color(248,250,252), BORDER, GuiTheme.TEXT));
        statPanelPhi = buildStatCard("Phí trả", lbStatPhi, STAT_FEE_BG, STAT_FEE_BORDER, new Color(160,100,0));
        statRow.add(statPanelPhi);
        statRow.add(buildStatCard("Tiền hoàn lại", lbStatHoan, STAT_OK_BG, STAT_OK_BORDER, OK_FG));
        content.add(statRow);
        content.add(Box.createVerticalStrut(16));

        // --- LÝ DO ---
        cbLyDo = new JComboBox<>(new String[]{"Bận việc","Ốm","Thay đổi kế hoạch","Khác"});
        cbLyDo.setFont(FONT_14); cbLyDo.setBackground(Color.WHITE);
        cbLyDo.setPreferredSize(new Dimension(180, FIELD_H));
        cbLyDo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(2, 4, 2, 4)));

        txtLyDoKhac = new JTextField();
        txtLyDoKhac.setFont(FONT_14); txtLyDoKhac.setVisible(false);
        txtLyDoKhac.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 10, 4, 10)));

        cbLyDo.addActionListener(e -> {
            boolean isKhac = "Khác".equals(cbLyDo.getSelectedItem());
            txtLyDoKhac.setVisible(isKhac);
            if (!isKhac) txtLyDoKhac.setText("");
        });

        lbWarning = new JLabel(" ");
        lbWarning.setFont(FONT_14); lbWarning.setForeground(WARN_FG);

        JPanel lyDoWrapper = new JPanel(new BorderLayout(10, 0));
        lyDoWrapper.setOpaque(false);
        lyDoWrapper.add(cbLyDo, BorderLayout.WEST);
        lyDoWrapper.add(txtLyDoKhac, BorderLayout.CENTER);

        JPanel bottomGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomGrid.setOpaque(false); bottomGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        bottomGrid.add(fieldBox("Lý do trả vé", lyDoWrapper));
        bottomGrid.add(labelBox("Thông báo", lbWarning));
        content.add(bottomGrid);

        outer.add(content, BorderLayout.CENTER);
        return outer;
    }

    // ===================== BUTTON ROW =====================
    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        p.setOpaque(false); p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JButton btnBack = makeOutlineBtn("Quay lại", 130, 38);
        btnBack.addActionListener(e -> appFrame.showCard("doi-tra"));

        btnXacNhan = makeNavyBtn("Tiếp tục", 130, 38);
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(e -> handleTiepTuc());

        p.add(btnBack); p.add(btnXacNhan);
        return p;
    }

    // ===================== LOGIC =====================
    private void fillInfo() {
        if (s_data.length < 9) { clearInfo(); return; }
        tfMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        tfChuyen .setText(s_data[0]); tfGaDi.setText(s_data[1]); tfGaDen.setText(s_data[2]);
        tfLoai   .setText(s_data[3]); tfNgayGio.setText(s_data[5]); tfSoLuong.setText(s_data[6]);
        tfGhe    .setText(s_data[7]);
        try { tfGia.setText(String.format("%,d đ", Long.parseLong(s_data[8].split("\\.")[0].replaceAll("[^0-9]",""))).replace(",",".")); }
        catch (Exception e) { tfGia.setText(s_data[8]); }
    }

    private void calcFee() {
        if (s_data.length < 9 || s_maVe.isEmpty()) {
            lbDieuKien.setText("Chưa có vé được chọn"); lbDieuKien.setForeground(GuiTheme.SUB_TEXT);
            lbStatTong.setText("—"); lbStatPhi.setText("—"); lbStatHoan.setText("—");
            lbWarning.setText(" "); btnXacNhan.setEnabled(false); return;
        }

        long gio = tinhGio(s_data[5]);
        long soLuong = 1, donGia = 0;
        try { soLuong = Long.parseLong(s_data[6].replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
        try { donGia = Long.parseLong(s_data[8].split("\\.")[0].replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        boolean nhom     = soLuong >= 2;
        long    tongTien = soLuong * donGia;
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

        lbDieuKien.setText(dieuKien); lbDieuKien.setForeground(hopLe ? OK_FG : RED_FG);
        if (hopLe) {
            long phiTien = Math.round(tongTien * phiPct / 100.0);
            updateStatTitle(statPanelPhi, "Phí trả (" + phiPct + "%)");
            lbStatPhi .setText(fmtTien(phiTien));  lbStatPhi .setForeground(new Color(160,100,0));
            lbStatHoan.setText(fmtTien(tongTien - phiTien)); lbStatHoan.setForeground(OK_FG);
            lbWarning.setText("  Vé hợp lệ. Nhấn 'Tiếp tục' để xác nhận trả vé.");
            lbWarning.setForeground(OK_FG); btnXacNhan.setEnabled(true);
        } else {
            updateStatTitle(statPanelPhi, "Phí trả");
            lbStatPhi .setText("Không hoàn"); lbStatPhi .setForeground(RED_FG);
            lbStatHoan.setText("0 đ");         lbStatHoan.setForeground(RED_FG);
            lbWarning.setText("  Không đủ điều kiện hoàn tiền. Còn: " + Math.max(gio, 0) + "h.");
            lbWarning.setForeground(WARN_FG); btnXacNhan.setEnabled(false);
        }
    }

    private void clearInfo() {
        for (JTextField tf : new JTextField[]{tfMaVe,tfChuyen,tfGaDi,tfGaDen,tfNgayGio,tfLoai,tfGhe,tfSoLuong,tfGia})
            if (tf != null) tf.setText("—");
    }

    private void handleTiepTuc() {
        String lyDo = cbLyDo.getSelectedItem().toString();
        if ("Khác".equals(lyDo)) {
            if (txtLyDoKhac.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,"Vui lòng nhập lý do!","Yêu cầu nhập liệu",JOptionPane.WARNING_MESSAGE);
                txtLyDoKhac.requestFocus(); return;
            }
            lyDo = txtLyDoKhac.getText().trim();
        }
        TraVeGUI1.setDonTra(s_maVe, s_data, lbStatPhi.getText(), lbStatHoan.getText(), lyDo);
        appFrame.showCard("tra-ve-step-2");
    }

    private static long tinhGio(String s) {
        try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(s, FMT)); }
        catch (Exception e) { return -1; }
    }

    // ===================== UI HELPERS =====================
    private JPanel buildStatCard(String title, JLabel valueLb, Color bg, Color border, Color titleColor) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(border); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(14,16,14,16));
        JLabel lbTt = new JLabel(title); lbTt.setFont(FONT_14); lbTt.setForeground(titleColor);
        lbTt.setAlignmentX(CENTER_ALIGNMENT); lbTt.setName("stat-title");
        valueLb.setAlignmentX(CENTER_ALIGNMENT); valueLb.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbTt); p.add(Box.createVerticalStrut(8)); p.add(valueLb);
        return p;
    }

    private void updateStatTitle(JPanel card, String newTitle) {
        for (Component c : card.getComponents())
            if (c instanceof JLabel lb && "stat-title".equals(lb.getName())) { lb.setText(newTitle); break; }
    }

    private JLabel statLabel(Color color, int style, int size) {
        JLabel lb = new JLabel("—"); lb.setFont(new Font("Segoe UI", style, size));
        lb.setForeground(color); lb.setHorizontalAlignment(SwingConstants.CENTER); return lb;
    }

    private JTextField readField() {
        JTextField tf = new JTextField("—"); tf.setFont(FONT_14); tf.setEditable(false);
        tf.setForeground(GuiTheme.TEXT); tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1), new EmptyBorder(6,10,6,10)));
        tf.setPreferredSize(new Dimension(0, FIELD_H)); tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return tf;
    }

    private JPanel fieldBox(String label, JComponent comp) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false);
        JLabel lb = new JLabel(label); lb.setFont(FONT_14); lb.setForeground(GuiTheme.SUB_TEXT); lb.setAlignmentX(LEFT_ALIGNMENT);
        comp.setAlignmentX(LEFT_ALIGNMENT); comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        p.add(lb); p.add(Box.createVerticalStrut(6)); p.add(comp);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58)); return p;
    }

    private JPanel labelBox(String labelText, JLabel value) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false);
        JLabel lb = new JLabel(labelText); lb.setFont(FONT_14); lb.setForeground(GuiTheme.SUB_TEXT); lb.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT); value.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        p.add(lb); p.add(Box.createVerticalStrut(6)); p.add(value);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58)); return p;
    }

    private static String fmtTien(long a) { return String.format("%,d đ", a).replace(",", "."); }

    private JButton makeNavyBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled() ? new Color(180,190,205) : getModel().isPressed() ? GuiTheme.NAVY_DARK : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY;
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(Color.WHITE); g2.setFont(FONT_14); FontMetrics fm=g2.getFontMetrics(); String t=getText();
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w,h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }

    private JButton makeOutlineBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()?new Color(220,225,235):getModel().isRollover()?new Color(235,239,246):new Color(240,243,248);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.setColor(GuiTheme.TEXT); g2.setFont(FONT_14); FontMetrics fm=g2.getFontMetrics(); String t=getText();
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w,h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }
}