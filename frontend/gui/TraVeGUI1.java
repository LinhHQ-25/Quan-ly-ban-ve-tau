package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import connect_DB.Connect_DB;
import util.MaTuDong;

public class TraVeGUI1 extends JPanel {
    private static final Color BORDER       = new Color(210, 215, 224);
    private static final Color NAVY         = new Color(28, 57, 110);
    private static final Color BG           = new Color(242, 247, 252);
    private static final Color OK_FG        = new Color(30, 120, 60);
    private static final Color OK_BG        = new Color(236, 252, 240);
    private static final Color OK_BORDER    = new Color(160, 215, 175);
    private static final Color HOAN_BG      = new Color(220, 252, 231);
    private static final Color HOAN_BORDER  = new Color(134, 239, 172);
    private static final Font  FONT_14      = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font  FONT_B14     = new Font("Segoe UI", Font.BOLD, 14);
    private static final int   FIELD_H      = 36;

    private static String   s_maVe    = "";
    private static String[] s_data    = new String[0];
    private static String   s_phi     = "";
    private static String   s_hoanLai = "";
    private static String   s_lyDo   = "";

    public static void setDonTra(String maVe, String[] data, String phi, String hoanLai, String lyDo) {
        s_maVe    = maVe;
        s_data    = data.clone();
        s_phi     = phi;
        s_hoanLai = hoanLai;
        s_lyDo    = lyDo;
    }

    private final AppFrame appFrame;
    private JLabel lbBannerHoan, lbBannerSub;
    private JLabel valMaVe, valChuyen, valGaDi, valGaDen, valNgayGio, valLoai, valTongTien, valPhi;

    public TraVeGUI1(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel pnlPage = new JPanel(new BorderLayout(0, 0));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Left: banner + info card (75%)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.75; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(0, 0, 0, 10);
        pnlCenter.add(buildLeftPanel(), gbc);

        // Right: confirm panel (25%)
        gbc.gridx = 1; gbc.weightx = 0.25; gbc.insets = new Insets(0, 0, 0, 0);
        pnlCenter.add(buildRightPanel(), gbc);

        pnlPage.add(pnlCenter, BorderLayout.CENTER);
        pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);
        add(pnlPage, BorderLayout.CENTER);
    }

    public void refresh() {
        // Cập nhật labels — index đã fix: [0]=maChuyenTau, [1]=gaDi, [2]=gaDen,
        // [3]=loaiVe, [4]=chieuVe, [5]=ngayGio, [6]=soLuong, [7]=maGhe, [8]=giaVe
        valMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        valChuyen .setText(safe(s_data, 0));
        valGaDi   .setText(safe(s_data, 1));
        valGaDen  .setText(safe(s_data, 2));
        valLoai   .setText(safe(s_data, 3));
        valNgayGio.setText(safe(s_data, 5));

        long tongTien = 0;
        try {
            long soLuong = Long.parseLong(s_data[6].replaceAll("[^0-9]", ""));
            long donGia  = Long.parseLong(s_data[8].split("\\.")[0].replaceAll("[^0-9]", ""));
            tongTien = soLuong * donGia;
        } catch (Exception ignored) {}

        valTongTien.setText(tongTien > 0 ? fmtTien(tongTien) : "—");
        valPhi.setText(s_phi.isEmpty() ? "—" : s_phi);
        lbBannerHoan.setText(s_hoanLai.isEmpty() ? "—" : s_hoanLai);
        lbBannerSub.setText("Phí trả: " + s_phi + "  ·  Hoàn trong 3–5 ngày làm việc");
        revalidate(); repaint();
    }

    // ===================== LEFT PANEL =====================
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        // Alert box
        JPanel alertBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(OK_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(OK_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
            }
        };
        alertBox.setLayout(new BorderLayout()); alertBox.setOpaque(false); alertBox.setBorder(new EmptyBorder(14,20,14,20));
        JLabel msg = new JLabel("Vui lòng kiểm tra kỹ thông tin trước khi xác nhận trả vé");
        msg.setFont(FONT_B14); msg.setForeground(OK_FG); msg.setHorizontalAlignment(SwingConstants.CENTER);
        alertBox.add(msg, BorderLayout.CENTER);

        // Info card
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(20,24,20,24)));

        JLabel lbTitle = new JLabel("Thông tin vé trả");
        lbTitle.setFont(new Font("Segoe UI",Font.BOLD,16)); lbTitle.setForeground(GuiTheme.TEXT);
        lbTitle.setBorder(new EmptyBorder(0,0,8,0));
        card.add(lbTitle, BorderLayout.NORTH);

        valMaVe    = infoLabel(GuiTheme.TEXT);  valChuyen  = infoLabel(GuiTheme.TEXT);
        valGaDi    = infoLabel(GuiTheme.TEXT);  valGaDen   = infoLabel(GuiTheme.TEXT);
        valNgayGio = infoLabel(GuiTheme.TEXT);  valLoai    = infoLabel(GuiTheme.TEXT);
        valTongTien= infoLabel(GuiTheme.TEXT);  valPhi     = infoLabel(new Color(160,100,0));

        JPanel grid = new JPanel(new GridLayout(2, 4, 16, 12));
        grid.setOpaque(false);
        grid.add(infoCell("Mã vé",       valMaVe));
        grid.add(infoCell("Mã chuyến",   valChuyen));
        grid.add(infoCell("Ga đi",       valGaDi));
        grid.add(infoCell("Ga đến",      valGaDen));
        grid.add(infoCell("Ngày/Giờ KH", valNgayGio));
        grid.add(infoCell("Loại vé",     valLoai));
        grid.add(infoCell("Tổng tiền vé",valTongTien));
        grid.add(infoCell("Phí trả vé",  valPhi));
        card.add(grid, BorderLayout.CENTER);

        p.add(alertBox, BorderLayout.NORTH);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    // ===================== RIGHT PANEL =====================
    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 248, 252));
        p.setBorder(new LineBorder(BORDER_C(), 1));

        JLabel lblHdr = new JLabel("Tiền hoàn trả");
        lblHdr.setFont(FONT_B14); lblHdr.setForeground(Color.WHITE);
        lblHdr.setOpaque(true); lblHdr.setBackground(NAVY); lblHdr.setBorder(new EmptyBorder(6,12,6,12));
        JPanel tw = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); tw.setOpaque(false); tw.add(lblHdr);
        p.add(tw, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false); content.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Hoàn banner
        JPanel hoanBanner = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HOAN_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(HOAN_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.dispose();
            }
        };
        hoanBanner.setOpaque(false); hoanBanner.setLayout(new BoxLayout(hoanBanner, BoxLayout.Y_AXIS));
        hoanBanner.setBorder(new EmptyBorder(20,12,20,12));
        hoanBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel lbTieuDe = new JLabel("Tiền hoàn lại cho bạn");
        lbTieuDe.setFont(FONT_14); lbTieuDe.setForeground(OK_FG); lbTieuDe.setAlignmentX(CENTER_ALIGNMENT);

        lbBannerHoan = new JLabel("—");
        lbBannerHoan.setFont(new Font("Segoe UI",Font.BOLD,28)); lbBannerHoan.setForeground(OK_FG);
        lbBannerHoan.setAlignmentX(CENTER_ALIGNMENT); lbBannerHoan.setHorizontalAlignment(SwingConstants.CENTER);

        lbBannerSub = new JLabel(" ");
        lbBannerSub.setFont(new Font("Segoe UI",Font.PLAIN,12)); lbBannerSub.setForeground(new Color(60,150,90));
        lbBannerSub.setAlignmentX(CENTER_ALIGNMENT);

        hoanBanner.add(lbTieuDe); hoanBanner.add(Box.createVerticalStrut(6));
        hoanBanner.add(lbBannerHoan); hoanBanner.add(Box.createVerticalStrut(4)); hoanBanner.add(lbBannerSub);

        content.add(hoanBanner);
        content.add(Box.createVerticalStrut(16));
        content.add(createDetailLabel("Phí trả:", s_phi.isEmpty() ? "—" : s_phi));
        content.add(Box.createVerticalStrut(6));
        content.add(createDetailLabel("Hoàn tiền sau:", "3–5 ngày làm việc"));
        content.add(Box.createVerticalGlue());
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private Color BORDER_C() { return new Color(180, 205, 230); }

    // ===================== BUTTON ROW =====================
    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        p.setOpaque(false); p.setBorder(new MatteBorder(1,0,0,0,BORDER));

        JButton btnBack = makeOutlineBtn("Quay lại", 130, 38);
        btnBack.addActionListener(e -> appFrame.showCard("tra-ve"));

        JButton btnDone = makeNavyBtn("Xác nhận trả vé", 160, 38);
        btnDone.addActionListener(e -> handleDone());

        p.add(btnBack); p.add(btnDone);
        return p;
    }

    // ===================== LOGIC =====================
    private void handleDone() {
        int choice = JOptionPane.showConfirmDialog(this,
                "<html><div style='padding:6px'><b>Xác nhận trả vé " + s_maVe + "?</b><br><br>" +
                        "Phí trả: <b>" + s_phi + "</b><br>" +
                        "Tiền hoàn lại: <b>" + s_hoanLai + "</b><br>" +
                        "Tiền sẽ được hoàn trong 3–5 ngày làm việc.</div></html>",
                "Xác nhận trả vé", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        // Tạo maDon (format DT-mmyy-4ký tự)
        String maDon;
        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            maDon = MaTuDong.taoMaDon(conn, LocalDate.now());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Lỗi tạo mã đơn: " + e.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Tính tiền hoàn
        long tienHoan = 0;
        try { tienHoan = Long.parseLong(s_hoanLai.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
        long tienPhi  = 0;
        try { tienPhi  = Long.parseLong(s_phi.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        // UPDATE Ve: trangThaiVe = N'Đã hủy'
        String sqlUpdateVe = "UPDATE Ve SET trangThaiVe = N'Đã hủy' WHERE maVe = ?";
        // INSERT DonDoiTraVe: loaiDon = 'DON_TRA'
        String sqlInsertDon =
                "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, tienHoanTra, loaiDon, maVe) " +
                        "VALUES (?, 0, GETDATE(), ?, 'DON_TRA', ?)";

        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateVe)) {
                    ps.setString(1, s_maVe);
                    int rows = ps.executeUpdate();
                    if (rows == 0) throw new Exception("Không tìm thấy vé " + s_maVe);
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlInsertDon)) {
                    ps.setString(1, maDon);
                    ps.setLong  (2, tienHoan);
                    ps.setString(3, s_maVe);
                    ps.executeUpdate();
                }
                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "<html><div style='text-align:center;padding:8px'>" +
                                "<b style='font-size:16px;color:#1e7840'>Trả vé thành công!</b><br><br>" +
                                "Mã đơn trả: <b>" + maDon + "</b><br>" +
                                "Mã vé <b>" + s_maVe + "</b> đã hủy trên hệ thống.<br>" +
                                "Tiền hoàn <b>" + s_hoanLai + "</b> sẽ chuyển trong 3–5 ngày làm việc." +
                                "</div></html>", "Hoàn tất", JOptionPane.PLAIN_MESSAGE);
                appFrame.showCard("doi-tra");

            } catch (Exception ex) { conn.rollback(); throw ex; }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Lỗi: " + e.getMessage(),"Lỗi Hệ Thống",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== UI HELPERS =====================
    private JLabel infoLabel(Color color) {
        JLabel lb = new JLabel("—"); lb.setFont(FONT_14); lb.setForeground(color);
        lb.setOpaque(true); lb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        lb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1), new EmptyBorder(6,10,6,10)));
        lb.setPreferredSize(new Dimension(0, FIELD_H)); lb.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return lb;
    }

    private JPanel infoCell(String label, JLabel value) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false);
        JLabel lb = new JLabel(label); lb.setFont(FONT_14); lb.setForeground(GuiTheme.SUB_TEXT); lb.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lb); p.add(Box.createVerticalStrut(6)); p.add(value);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58)); return p;
    }

    private JPanel createDetailLabel(String title, String value) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); }
        };
        p.setOpaque(false);
        p.add(new JLabel(title) {{ setFont(FONT_14); setForeground(new Color(80,80,80)); }}, BorderLayout.WEST);
        p.add(new JLabel(value) {{ setFont(FONT_B14); setForeground(Color.BLACK); }}, BorderLayout.EAST);
        return p;
    }

    private static String fmtTien(long a) { return String.format("%,d đ", a).replace(",", "."); }
    private static String safe(String[] a, int i) { return (a!=null&&i<a.length&&a[i]!=null)?a[i]:"—"; }

    private JButton makeNavyBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()?GuiTheme.NAVY_DARK:getModel().isRollover()?GuiTheme.NAVY_HOVER:GuiTheme.NAVY);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12); g2.setColor(Color.WHITE); g2.setFont(FONT_14);
                FontMetrics fm=g2.getFontMetrics(); String t=getText();
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w,h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }

    private JButton makeOutlineBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg=getModel().isPressed()?new Color(220,225,235):getModel().isRollover()?new Color(235,239,246):new Color(240,243,248);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.setColor(GuiTheme.TEXT); g2.setFont(FONT_14); FontMetrics fm=g2.getFontMetrics(); String t=getText();
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w,h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }
}