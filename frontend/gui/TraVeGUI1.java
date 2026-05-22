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
    private static final Color NAVY         = GuiTheme.NAVY;
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
    private static String   s_lyDo    = "";

    public static void setDonTra(String maVe, String[] data, String phi, String hoanLai, String lyDo) {
        s_maVe    = maVe;
        s_data    = data.clone();
        s_phi     = phi;
        s_hoanLai = hoanLai;
        s_lyDo    = lyDo;
    }

    private final AppFrame appFrame;
    private JLabel lbBannerHoan, lbBannerSub;
    private JLabel valMaVe, valChuyen, valGaDi, valGaDen, valNgayGio, valLoai;
    private JLabel valGhe, valSoLuong, valLyDo, valTongTien, valPhi;

    public TraVeGUI1(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel pnlPage = new JPanel(new BorderLayout(0, 4));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, 0, GuiTheme.PAGE_PAD_LEFT));

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.70; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(0, 0, 0, 10);
        pnlCenter.add(buildLeftPanel(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.30; gbc.insets = new Insets(0, 0, 0, 0);
        pnlCenter.add(buildRightPanel(), gbc);

        pnlPage.add(pnlCenter, BorderLayout.CENTER);
        pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);
        add(pnlPage, BorderLayout.CENTER);
    }

    public void refresh() {
        valMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        valChuyen .setText(safe(s_data, 0));
        valGaDi   .setText(safe(s_data, 1));
        valGaDen  .setText(safe(s_data, 2));
        valLoai   .setText(safe(s_data, 3));
        valNgayGio.setText(safe(s_data, 5));
        valSoLuong.setText(safe(s_data, 6));
        valGhe    .setText(safe(s_data, 7));
        valLyDo   .setText(s_lyDo.isEmpty() ? "—" : s_lyDo);

        long tongTien = 0;
        try {
            long soLuong = Long.parseLong(s_data[6].replaceAll("[^0-9]", ""));
            long donGia  = Long.parseLong(s_data[8].split("\\.")[0].replaceAll("[^0-9]", ""));
            tongTien = soLuong * donGia;
        } catch (Exception ignored) {}

        valTongTien.setText(tongTien > 0 ? fmtTien(tongTien) : "—");
        valPhi.setText(s_phi.isEmpty() ? "—" : s_phi);
        lbBannerHoan.setText(s_hoanLai.isEmpty() ? "—" : s_hoanLai);
        lbBannerSub.setText("Phí trả: " + s_phi + "  ·  Hoàn trong 3–5 ngày");
        revalidate(); repaint();
    }

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15)); p.setOpaque(false);
        JPanel alertBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(OK_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(OK_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose();
            }
        };
        alertBox.setLayout(new BorderLayout()); alertBox.setOpaque(false); alertBox.setBorder(new EmptyBorder(12,20,12,20));
        JLabel msg = new JLabel("Vui lòng kiểm tra kỹ thông tin trước khi xác nhận trả vé");
        msg.setFont(FONT_B14); msg.setForeground(OK_FG); msg.setHorizontalAlignment(SwingConstants.CENTER); alertBox.add(msg, BorderLayout.CENTER);

        JPanel card = new JPanel(new BorderLayout(0, 20)); card.setBackground(Color.WHITE); card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(20,24,20,24)));
        JLabel lbTitle = new JLabel("Thông tin vé trả"); lbTitle.setFont(new Font("Segoe UI",Font.BOLD,16)); lbTitle.setForeground(NAVY); lbTitle.setBorder(new MatteBorder(0,0,1,0,BORDER)); card.add(lbTitle, BorderLayout.NORTH);

        valMaVe    = infoLabel(GuiTheme.TEXT);  valChuyen  = infoLabel(GuiTheme.TEXT);
        valGaDi    = infoLabel(GuiTheme.TEXT);  valGaDen   = infoLabel(GuiTheme.TEXT);
        valNgayGio = infoLabel(GuiTheme.TEXT);  valLoai    = infoLabel(GuiTheme.TEXT);
        valGhe     = infoLabel(GuiTheme.TEXT);  valSoLuong = infoLabel(GuiTheme.TEXT);
        valLyDo    = infoLabel(GuiTheme.TEXT);
        valTongTien= infoLabel(GuiTheme.TEXT);  valPhi     = infoLabel(new Color(160,100,0));

        JPanel grid = new JPanel(new GridLayout(3, 4, 16, 12)); grid.setOpaque(false);
        grid.add(infoCell("Mã vé",       valMaVe)); grid.add(infoCell("Mã chuyến",   valChuyen)); grid.add(infoCell("Ga đi",       valGaDi)); grid.add(infoCell("Ga đến",      valGaDen));
        grid.add(infoCell("Ngày/Giờ KH", valNgayGio)); grid.add(infoCell("Loại vé",     valLoai)); grid.add(infoCell("Số ghế",      valGhe)); grid.add(infoCell("Số lượng",    valSoLuong));
        grid.add(infoCell("Lý do trả",   valLyDo)); grid.add(new JLabel()); grid.add(infoCell("Tổng tiền vé",valTongTien)); grid.add(infoCell("Phí trả vé",  valPhi));

        JPanel contentWrapper = new JPanel(new BorderLayout(0, 20)); contentWrapper.setOpaque(false); contentWrapper.add(grid, BorderLayout.NORTH);

        JPanel historyBox = new JPanel(new BorderLayout(0, 8)); historyBox.setBackground(new Color(248, 250, 252)); historyBox.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(14, 18, 14, 18)));
        JLabel lblHistTitle = new JLabel("Lịch sử đổi/trả của vé này"); lblHistTitle.setFont(FONT_B14); lblHistTitle.setForeground(NAVY); historyBox.add(lblHistTitle, BorderLayout.NORTH);
        historyBox.add(buildHistoryRows(), BorderLayout.CENTER); contentWrapper.add(historyBox, BorderLayout.CENTER); card.add(contentWrapper, BorderLayout.CENTER);

        p.add(alertBox, BorderLayout.NORTH); p.add(card, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(Color.WHITE); p.setBorder(new LineBorder(BORDER, 1));
        JLabel lblHdr = new JLabel("Tiền hoàn trả"); lblHdr.setFont(FONT_B14); lblHdr.setForeground(Color.WHITE); lblHdr.setOpaque(true); lblHdr.setBackground(NAVY); lblHdr.setBorder(new EmptyBorder(8,16,8,16));
        JPanel tw = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); tw.setOpaque(false); tw.add(lblHdr); p.add(tw, BorderLayout.NORTH);

        JPanel content = new JPanel(); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS)); content.setOpaque(false); content.setBorder(new EmptyBorder(20, 15, 20, 15));
        JPanel hoanBanner = new JPanel() {
            @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(HOAN_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.setColor(HOAN_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose(); }
        };
        hoanBanner.setOpaque(false); hoanBanner.setLayout(new BoxLayout(hoanBanner, BoxLayout.Y_AXIS)); hoanBanner.setBorder(new EmptyBorder(20,12,20,12)); hoanBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel lbTieuDe = new JLabel("Tổng tiền hoàn lại"); lbTieuDe.setFont(FONT_14); lbTieuDe.setForeground(OK_FG); lbTieuDe.setAlignmentX(CENTER_ALIGNMENT);
        lbBannerHoan = new JLabel("—"); lbBannerHoan.setFont(new Font("Segoe UI",Font.BOLD,26)); lbBannerHoan.setForeground(OK_FG); lbBannerHoan.setAlignmentX(CENTER_ALIGNMENT); lbBannerHoan.setHorizontalAlignment(SwingConstants.CENTER);
        lbBannerSub = new JLabel(" "); lbBannerSub.setFont(new Font("Segoe UI",Font.PLAIN,12)); lbBannerSub.setForeground(new Color(60,150,90)); lbBannerSub.setAlignmentX(CENTER_ALIGNMENT);

        hoanBanner.add(lbTieuDe); hoanBanner.add(Box.createVerticalStrut(8)); hoanBanner.add(lbBannerHoan); hoanBanner.add(Box.createVerticalStrut(6)); hoanBanner.add(lbBannerSub);

        content.add(hoanBanner); content.add(Box.createVerticalStrut(25)); content.add(createDetailLabel("Hình thức:", "Chuyển khoản / Tiền mặt")); content.add(Box.createVerticalStrut(10)); content.add(createDetailLabel("Thời gian:", "3–5 ngày làm việc")); content.add(Box.createVerticalGlue());
        content.add(new JSeparator()); content.add(Box.createVerticalStrut(15)); content.add(createDetailLabel("Hotline hỗ trợ:", "1900 1234"));
        p.add(content, BorderLayout.CENTER); return p;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4)); p.setOpaque(false); p.setBorder(new MatteBorder(1,0,0,0,BORDER));
        JButton btnBack = makeOutlineBtn("Quay lại", 130, 38); btnBack.addActionListener(e -> appFrame.showCard("tra-ve"));
        JButton btnDone = makeNavyBtn("Xác nhận hoàn tiền", 170, 38); btnDone.addActionListener(e -> handleDone());
        p.add(btnBack); p.add(btnDone); return p;
    }

    // --- LOGIC GHI NHẬN HÓA ĐƠN HOÀN TIỀN ĐÃ ĐƯỢC CHỈNH LẠI ĐÚNG YÊU CẦU ---
    private void handleDone() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Xác nhận trả vé " + s_maVe + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        String maDon;
        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            maDon = MaTuDong.taoMaDon(conn, LocalDate.now());
        } catch (Exception e) { return; }

        long tienHoanKhach = 0; // 80% (số dương)
        try { tienHoanKhach = Long.parseLong(s_hoanLai.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        long phiHuyVe = 0; // 20% (Tiền lơi)
        try { phiHuyVe = Long.parseLong(s_phi.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        // SQL: INSERT Hóa đơn mới với tongTien ÂM (trừ vào doanh thu bán)
        String sqlInsertHD = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) " +
                "SELECT ?, GETDATE(), hd.maNV, hd.maKH, ?, 0, N'Hoàn tiền' " +
                "FROM HoaDon hd JOIN Ve v ON v.maHoaDon = hd.maHoaDon WHERE v.maVe = ?";

        String sqlUpdateVe = "UPDATE Ve SET trangThaiVe = N'Đã hủy', maHoaDon = ? WHERE maVe = ?";
        String sqlInsertDon = "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, tienHoanTra, loaiDon, maVe) VALUES (?, ?, GETDATE(), ?, 'DON_TRA', ?)";

        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psHD = conn.prepareStatement(sqlInsertHD);
                 PreparedStatement psVe = conn.prepareStatement(sqlUpdateVe);
                 PreparedStatement psDon = conn.prepareStatement(sqlInsertDon)) {

                psHD.setString(1, maDon);
                psHD.setLong(2, -tienHoanKhach); // <--- LƯU SỐ ÂM ĐỂ TRỪ DOANH THU
                psHD.setString(3, s_maVe);
                psHD.executeUpdate();

                psVe.setString(1, maDon);
                psVe.setString(2, s_maVe);
                psVe.executeUpdate();

                psDon.setString(1, maDon);
                psDon.setLong(2, phiHuyVe); // Ghi nhận tiền lơi (phí phạt)
                psDon.setLong(3, tienHoanKhach);
                psDon.setString(4, s_maVe);
                psDon.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Trả vé thành công!");
                appFrame.showCard("doi-tra");
            } catch (Exception ex) { conn.rollback(); throw ex; }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JLabel infoLabel(Color color) { JLabel lb = new JLabel("—"); lb.setFont(FONT_B14); lb.setForeground(color); lb.setOpaque(true); lb.setBackground(GuiTheme.SEARCH_FIELD_BG); lb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1), new EmptyBorder(6,10,6,10))); lb.setPreferredSize(new Dimension(0, FIELD_H)); lb.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H)); return lb; }
    private JPanel infoCell(String label, JLabel value) { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); JLabel lb = new JLabel(label); lb.setFont(FONT_14); lb.setForeground(GuiTheme.SUB_TEXT); lb.setAlignmentX(LEFT_ALIGNMENT); value.setAlignmentX(LEFT_ALIGNMENT); p.add(lb); p.add(Box.createVerticalStrut(4)); p.add(value); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58)); return p; }
    private JPanel createDetailLabel(String title, String value) { JPanel p = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); } }; p.setOpaque(false); p.add(new JLabel(title) {{ setFont(FONT_14); setForeground(GuiTheme.SUB_TEXT); }}, BorderLayout.WEST); p.add(new JLabel(value) {{ setFont(FONT_B14); setForeground(GuiTheme.TEXT); }}, BorderLayout.EAST); return p; }

    private JPanel buildHistoryRows() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); p.setBorder(new EmptyBorder(8, 0, 0, 0));
        String sql = "SELECT d.maDon, d.loaiDon, d.ngayLap, d.tienBu, d.tienHoanTra FROM DonDoiTraVe d WHERE d.maVe = ? ORDER BY d.ngayLap DESC";
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        try (java.sql.Connection conn = connect_DB.Connect_DB.getInstance().getConnection(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s_maVe);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai   = "DON_DOI".equals(rs.getString("loaiDon")) ? "Đổi vé" : "Trả vé";
                    String ngay   = rs.getTimestamp("ngayLap") != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("ngayLap")) : "—";
                    long tienBu   = (long) rs.getDouble("tienBu"); long tienHoan = (long) rs.getDouble("tienHoanTra");
                    String tien   = tienBu > 0 ? "Bù: " + fmtTien(tienBu) : "Hoàn: " + fmtTien(tienHoan);
                    rows.add(new String[]{ rs.getString("maDon"), loai, ngay, tien });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (rows.isEmpty()) { JLabel none = new JLabel("Chưa có lịch sử đổi/trả cho vé này."); none.setFont(new Font("Segoe UI", Font.ITALIC, 13)); none.setForeground(GuiTheme.SUB_TEXT); p.add(none); return p; }
        Color[] rowBgs = { Color.WHITE, new Color(248, 250, 252) }; Color dOrange  = new Color(160, 100, 0); Color dRed     = new Color(180, 30, 30);
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i); boolean isDoi = r[1].equals("Đổi vé");
            JPanel row = new JPanel(new GridLayout(1, 4, 10, 0)); row.setBackground(rowBgs[i % 2]); row.setBorder(new EmptyBorder(6, 0, 6, 0)); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel lMaDon = new JLabel(r[0]); lMaDon.setFont(new Font("Segoe UI", Font.BOLD, 13)); JLabel lLoai  = new JLabel(r[1]); lLoai.setFont(new Font("Segoe UI", Font.BOLD, 13)); lLoai.setForeground(isDoi ? dOrange : dRed); JLabel lNgay  = new JLabel(r[2]); lNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lNgay.setForeground(GuiTheme.SUB_TEXT); JLabel lTien  = new JLabel(r[3]); lTien.setFont(new Font("Segoe UI", Font.BOLD, 13)); lTien.setForeground(isDoi ? dOrange : new Color(30, 120, 60));
            row.add(lMaDon); row.add(lLoai); row.add(lNgay); row.add(lTien); p.add(row);
            if (i < rows.size() - 1) { JSeparator sep = new JSeparator(); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); sep.setForeground(BORDER); p.add(sep); }
        } return p;
    }

    private static String fmtTien(long a) { return String.format("%,d đ", a).replace(",", "."); }
    private static String safe(String[] a, int i) { return (a!=null&&i<a.length&&a[i]!=null)?a[i]:"—"; }
    private JButton makeNavyBtn(String text, int w, int h) { JButton b = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isPressed()?GuiTheme.NAVY_DARK:getModel().isRollover()?GuiTheme.NAVY_HOVER:NAVY); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(Color.WHITE); g2.setFont(FONT_14); FontMetrics fm=g2.getFontMetrics(); String t=getText(); g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose(); } }; b.setPreferredSize(new Dimension(w,h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
    private JButton makeOutlineBtn(String text, int w, int h) { JButton b = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); Color bg=getModel().isPressed()?new Color(220,225,235):getModel().isRollover()?new Color(235,239,246):new Color(240,243,248); g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.setColor(GuiTheme.TEXT); g2.setFont(FONT_14); FontMetrics fm=g2.getFontMetrics(); String t=getText(); g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose(); } }; b.setPreferredSize(new Dimension(w,h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
}