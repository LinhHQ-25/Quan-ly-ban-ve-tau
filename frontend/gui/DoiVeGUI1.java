package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import connect_DB.Connect_DB;

public class DoiVeGUI1 extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color NAVY = GuiTheme.NAVY;
    private static final Color NEW_FG = GuiTheme.NAVY;
    private static final Font FONT_B14 = new Font("Segoe UI", Font.BOLD, 14);

    // --- DỮ LIỆU TĨNH ---
    private static String s_maVe = "";
    private static String[] s_dataCu = new String[0];
    private static String s_maChuyenMoi = "", s_ngayDiMoi = "", s_gheDiMoi = "", s_maToaMoi = "";
    private static String s_chuyenVeMoi = "—", s_ngayVeMoi = "—", s_gheVeMoi = "—";

    public static void setDonDoiKhuHoi(String maVe, String[] dataCu,
                                       String maChuyenMoi, String ngayDi, String gheDi, String maToaMoi,
                                       String chuyenVe, String ngayVe) {
        s_maVe         = maVe;
        s_dataCu       = dataCu.clone();
        s_maChuyenMoi  = maChuyenMoi;
        s_ngayDiMoi    = ngayDi;
        s_gheDiMoi     = gheDi;
        s_maToaMoi     = maToaMoi;
        s_chuyenVeMoi  = chuyenVe;
        s_ngayVeMoi    = ngayVe;
        s_gheVeMoi     = "—";
    }

    private final AppFrame appFrame;
    private JPanel pnlTabController, pnlCardContainer;
    private CardLayout cardLayout;
    private final boolean[] activeDi = {true}, activeVe = {false};

    private JLabel valMaVeDi, valChuyenDi, valToaDi, valGheDi, valNgayDi, valGaDi_Di, valGaDenDi;
    private JLabel valMaVeVe, valChuyenVe, valToaVe, valGheVe, valNgayVe, valGaDi_Ve, valGaDenVe;
    private JLabel oldMaVeDi, oldChuyenDi, oldToaDi, oldGheDi, oldNgayDi, oldGaDi_Di_old, oldGaDenDi_old;
    private JLabel oldMaVeVe, oldChuyenVe, oldToaVe, oldGheVe, oldNgayVe, oldGaDi_Ve_old, oldGaDenVe_old;
    private JLabel lbChenhLech, lbTongThu;
    private long tongLePhi = 0;
    private long giaVeMoi  = 0;

    public DoiVeGUI1(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);

        JPanel pnlPage = new JPanel(new BorderLayout(0, 10));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        stack.add(buildSuccessBox());
        stack.add(Box.createVerticalStrut(15));
        stack.add(buildTabController());
        stack.add(buildCompareCard());

        pnlPage.add(new JScrollPane(stack) {{ setBorder(null); setOpaque(false); getViewport().setOpaque(false); }}, BorderLayout.CENTER);
        pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);
        add(pnlPage, BorderLayout.CENTER);
    }

    public void refresh() {
        boolean isKhuHoi = !s_chuyenVeMoi.equals("—") && !s_chuyenVeMoi.isEmpty();
        pnlTabController.setVisible(isKhuHoi);

        String oldGaDi  = safe(s_dataCu, 1);
        String oldGaDen = safe(s_dataCu, 2);

        String[] gasMoi = queryGaFromChuyen(s_maChuyenMoi);
        String newGaDi  = gasMoi[0];
        String newGaDen = gasMoi[1];

        oldMaVeDi.setText(s_maVe);
        oldChuyenDi.setText(safe(s_dataCu, 0));
        oldToaDi.setText(extractToaFromDB(safe(s_dataCu, 7)));
        oldGheDi.setText(extractGheFromDB(safe(s_dataCu, 7)));
        oldNgayDi.setText(safe(s_dataCu, 5));
        oldGaDi_Di_old.setText(oldGaDi);
        oldGaDenDi_old.setText(oldGaDen);

        valMaVeDi.setText(s_maVe);
        valChuyenDi.setText(s_maChuyenMoi);
        valToaDi.setText(s_maToaMoi);
        valGheDi.setText(extractGheFromGheStr(s_gheDiMoi));
        valNgayDi.setText(s_ngayDiMoi);
        valGaDi_Di.setText(newGaDi);
        valGaDenDi.setText(newGaDen);

        if (isKhuHoi) {
            valMaVeVe.setText(s_maVe + "-VE");
            valChuyenVe.setText(s_chuyenVeMoi);
            valToaVe.setText("—");
            valGheVe.setText("—");
            valNgayVe.setText(s_ngayVeMoi);
            valGaDi_Ve.setText(oldGaDen);
            valGaDenVe.setText(oldGaDi);
        } else {
            activeDi[0] = true; activeVe[0] = false;
            cardLayout.show(pnlCardContainer, "DI");
        }

        calcPriceAndRefresh();
        revalidate(); repaint();
    }

    private String[] queryGaFromChuyen(String maChuyenTau) {
        String sql = "SELECT gDi.tenGa AS gaDi, gDen.tenGa AS gaDen FROM ChiTietChuyenTau dt " +
                "JOIN Ga gDi ON dt.maGaDi = gDi.maGa JOIN Ga gDen ON dt.maGaDen = gDen.maGa WHERE dt.maChuyenTau = ?";
        try (Connection conn = Connect_DB.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new String[]{ rs.getString("gaDi"), rs.getString("gaDen") }; }
        } catch (Exception e) {} return new String[]{"—", "—"};
    }

    private void calcPriceAndRefresh() {
        // ── ĐẦU VÀO ──────────────────────────────────────────────────────────
        long   giaVeCu     = 0;
        try { giaVeCu = (long) Double.parseDouble(safe(s_dataCu, 8).replaceAll("[^0-9.]", "")); } catch (Exception ignored) {}

        final long   giaVeCoBan  = 500_000L;          // cố định
        final double cuLy        = 1.0;                // tạm thời = 1, bổ sung sau khi có DB
        final long   phiDoiVe    = 30_000L;            // lệ phí cố định

        // hệ số loại chỗ: query từ DB (TOA_THUONG=1.0, TOA_VIP=1.5)
        String maToaCu  = extractMaToaFromMaGheDB(safe(s_dataCu, 7));
        double heSoCu   = queryHeSoToa(maToaCu);
        double heSoMoi  = queryHeSoToa(s_maToaMoi);

        // ── BƯỚC 1 — giá trị thực của vé mới (chưa tính phí) ────────────────
        long giaTriVeMoi = Math.round(giaVeCoBan * cuLy * heSoMoi);
        // Ví dụ: 500.000 * 1 * 1.5 = 750.000 đ

        // ── BƯỚC 2 — 2 kịch bản ─────────────────────────────────────────────
        long giaVeDoi;   // số tiền khách phải bù (tongLePhi)

        if (giaTriVeMoi >= giaVeCu) {
            // Kịch bản A — ngang giá hoặc nâng hạng
            // giaVeMoi (lưu DB) = giaTriVeMoi + phiDoiVe
            giaVeMoi = giaTriVeMoi + phiDoiVe;
            // giaVeDoi (khách bù) = giaVeMoi - giaVeCu
            giaVeDoi = giaVeMoi - giaVeCu;
            // VD: 750k + 30k = 780k → 780k - 500k = 280k
        } else {
            // Kịch bản B — xuống hạng
            // Chỉ thu phí cố định, không hoàn tiền thừa
            giaVeDoi = phiDoiVe;
            // giaVeMoi (lưu DB) = giaVeCu + phiDoiVe (giữ nguyên giá trị kinh tế)
            giaVeMoi = giaVeCu + phiDoiVe;
        }

        // ── BƯỚC 3 — ràng buộc an toàn ──────────────────────────────────────
        // giaVeDoi KHÔNG BAO GIỜ âm, tối thiểu = phiDoiVe
        tongLePhi = Math.max(giaVeDoi, phiDoiVe);

        // ── HIỂN THỊ ─────────────────────────────────────────────────────────
        long chenhLech = giaTriVeMoi - giaVeCu;
        String dauChenhlech = chenhLech > 0 ? "+" : "";
        lbChenhLech.setText("Chênh lệch hạng ghế: " + dauChenhlech + fmtTien(chenhLech));
        lbChenhLech.setForeground(chenhLech > 0 ? new Color(180, 60, 0)
                : chenhLech < 0 ? new Color(30, 120, 60)
                  : GuiTheme.SUB_TEXT);

        lbTongThu.setText("Tổng tiền cần thanh toán: " + fmtTien(tongLePhi));
        lbTongThu.setForeground(tongLePhi > phiDoiVe ? new Color(180, 60, 0) : GuiTheme.TEXT);
    }

    private String extractMaToaFromMaGheDB(String maGhe) {
        if (maGhe == null || maGhe.length() < 4) return "";
        int tIdx = maGhe.indexOf('T'); return (tIdx > 0) ? maGhe.substring(tIdx) : "";
    }

    private double queryHeSoToa(String maToaTau) {
        if (maToaTau == null || maToaTau.isEmpty()) return 1.0;
        String sql = "SELECT heSoLoaiToa FROM ToaTau WHERE maToaTau = ?";
        try (Connection conn = Connect_DB.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maToaTau);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getDouble("heSoLoaiToa"); }
        } catch (Exception e) {} return 1.0;
    }

    private JPanel buildCompareCard() {
        JPanel card = buildCard("So sánh chi tiết lộ trình");
        cardLayout = new CardLayout();
        pnlCardContainer = new JPanel(cardLayout); pnlCardContainer.setOpaque(false);

        valMaVeDi=fieldLabel(GuiTheme.TEXT); valChuyenDi=fieldLabel(NEW_FG); valToaDi=fieldLabel(NEW_FG); valGheDi=fieldLabel(NEW_FG); valNgayDi=fieldLabel(NEW_FG); valGaDi_Di=fieldLabel(GuiTheme.TEXT); valGaDenDi=fieldLabel(GuiTheme.TEXT);
        valMaVeVe=fieldLabel(GuiTheme.TEXT); valChuyenVe=fieldLabel(NEW_FG); valToaVe=fieldLabel(NEW_FG); valGheVe=fieldLabel(NEW_FG); valNgayVe=fieldLabel(NEW_FG); valGaDi_Ve=fieldLabel(GuiTheme.TEXT); valGaDenVe=fieldLabel(GuiTheme.TEXT);
        oldMaVeDi=fieldLabel(GuiTheme.SUB_TEXT); oldChuyenDi=fieldLabel(GuiTheme.SUB_TEXT); oldToaDi=fieldLabel(GuiTheme.SUB_TEXT); oldGheDi=fieldLabel(GuiTheme.SUB_TEXT); oldNgayDi=fieldLabel(GuiTheme.SUB_TEXT); oldGaDi_Di_old=fieldLabel(GuiTheme.SUB_TEXT); oldGaDenDi_old=fieldLabel(GuiTheme.SUB_TEXT);
        oldMaVeVe=fieldLabel(GuiTheme.SUB_TEXT); oldChuyenVe=fieldLabel(GuiTheme.SUB_TEXT); oldToaVe=fieldLabel(GuiTheme.SUB_TEXT); oldGheVe=fieldLabel(GuiTheme.SUB_TEXT); oldNgayVe=fieldLabel(GuiTheme.SUB_TEXT); oldGaDi_Ve_old=fieldLabel(GuiTheme.SUB_TEXT); oldGaDenVe_old=fieldLabel(GuiTheme.SUB_TEXT);

        JPanel gridDi = createCompareGrid(true, oldMaVeDi, oldChuyenDi, oldToaDi, oldGheDi, oldNgayDi, oldGaDi_Di_old, oldGaDenDi_old, valMaVeDi, valChuyenDi, valToaDi, valGheDi, valNgayDi, valGaDi_Di, valGaDenDi);
        JPanel gridVe = createCompareGrid(false, oldMaVeVe, oldChuyenVe, oldToaVe, oldGheVe, oldNgayVe, oldGaDi_Ve_old, oldGaDenVe_old, valMaVeVe, valChuyenVe, valToaVe, valGheVe, valNgayVe, valGaDi_Ve, valGaDenVe);

        pnlCardContainer.add(gridDi, "DI"); pnlCardContainer.add(gridVe, "VE");

        JPanel pnlFooter = new JPanel(new GridLayout(2, 1, 0, 5)); pnlFooter.setOpaque(false);
        lbChenhLech = new JLabel("Chênh lệch: 0 đ"); lbChenhLech.setFont(FONT_B14); lbChenhLech.setForeground(GuiTheme.SUB_TEXT);
        lbTongThu = new JLabel("Tổng thu: 30.000 đ"); lbTongThu.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
        pnlFooter.add(lbChenhLech); pnlFooter.add(lbTongThu);

        card.add(pnlCardContainer, BorderLayout.CENTER);
        card.add(pnlFooter, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCompareGrid(boolean isDi, JLabel oldMaVe, JLabel oldChuyen, JLabel oldToa, JLabel oldGhe, JLabel oldNgay, JLabel oldGa1, JLabel oldGa2, JLabel vMa, JLabel vCh, JLabel vTo, JLabel vGh, JLabel vNgay, JLabel vGaDi, JLabel vGaDen) {
        JPanel grid = new JPanel(new GridBagLayout()); grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridy = 0; gbc.gridx = 1; grid.add(headerLabel("HIỆN TẠI (CŨ)", GuiTheme.SUB_TEXT), gbc); gbc.gridx = 3; grid.add(headerLabel("ĐỔI SANG (MỚI)", NEW_FG), gbc);
        addGridRow(grid, 1, "Mã vé", oldMaVe, vMa); addGridRow(grid, 2, "Chuyến", oldChuyen, vCh); addGridRow(grid, 3, "Toa", oldToa, vTo); addGridRow(grid, 4, "Ghế", oldGhe, vGh); addGridRow(grid, 5, isDi ? "Ngày đi" : "Ngày về", oldNgay, vNgay); addGridRow(grid, 6, "Ga đi", oldGa1, vGaDi); addGridRow(grid, 7, "Ga đến", oldGa2, vGaDen);
        return grid;
    }

    private void addGridRow(JPanel grid, int y, String title, JLabel oldV, JLabel newV) {
        GridBagConstraints gbc = new GridBagConstraints(); gbc.gridy = y; gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.weightx = 0.15; grid.add(new JLabel(title) {{ setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13)); setForeground(GuiTheme.SUB_TEXT); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; grid.add(oldV, gbc);
        gbc.gridx = 2; gbc.weightx = 0.05; grid.add(new JLabel("→", SwingConstants.CENTER) {{ setFont(new Font("Segoe UI", Font.BOLD, 16)); setForeground(BORDER); }}, gbc);
        gbc.gridx = 3; gbc.weightx = 0.4; grid.add(newV, gbc);
    }

    private JPanel buildTabController() {
        pnlTabController = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); pnlTabController.setOpaque(false);
        pnlTabController.add(makeTabButton("Chiều đi", activeDi, () -> { activeDi[0]=true; activeVe[0]=false; cardLayout.show(pnlCardContainer, "DI"); pnlTabController.repaint(); }));
        pnlTabController.add(makeTabButton("Chiều về", activeVe, () -> { activeDi[0]=false; activeVe[0]=true; cardLayout.show(pnlCardContainer, "VE"); pnlTabController.repaint(); }));
        return pnlTabController;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10)); p.setOpaque(false); p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
        JButton btnBack = makeSecondaryButton("Quay lại", 120, 38); btnBack.addActionListener(e -> appFrame.showCard("doi-ve-step-1"));
        JButton btnConfirm = makeNavyButton("Tiếp tục thanh toán", 170, 38); btnConfirm.addActionListener(e -> handleConfirm());
        p.add(btnBack); p.add(btnConfirm); return p;
    }

    private void handleConfirm() {
        String maGheDbDi = getMaGheMoiDB(s_gheDiMoi.split(",")[0].trim());
        String hienThiDi = s_maToaMoi + " - " + extractGheFromGheStr(s_gheDiMoi);
        DoiVeGUI2.setDuLieuThanhToan(s_maVe, s_dataCu, s_maChuyenMoi, s_ngayDiMoi, maGheDbDi, hienThiDi, tongLePhi, giaVeMoi);
        appFrame.showCard("doi-ve-step-3");
    }

    private String extractToa(String full) { try { return "Toa " + Integer.parseInt(full.split("-")[0].trim().substring(1, 3)); } catch(Exception e) { return "—"; } }
    private String extractGhe(String full) { try { return full.split("-")[1].trim(); } catch(Exception e) { return "—"; } }
    private int extractToaNum(String full) { try { return Integer.parseInt(full.split("-")[0].trim().substring(1, 3)); } catch(Exception e) { return 1; } }
    private String getMaGheMoiDB(String fullStr) { if (fullStr == null || !fullStr.contains("-")) return fullStr; String[] parts = fullStr.split("-"); return parts[1].trim() + parts[0].trim(); }
    private String extractGheFromGheStr(String gheStr) { if (gheStr == null || gheStr.isEmpty() || !gheStr.contains("-")) return "—"; try { return gheStr.split(",")[0].split("-")[1].trim(); } catch (Exception e) { return "—"; } }
    private String extractToaFromDB(String maGhe) { if (maGhe == null || maGhe.length() < 4) return "—"; try { return maGhe.substring(3); } catch (Exception e) { return "—"; } }
    private String extractGheFromDB(String maGhe) { if (maGhe == null || maGhe.length() < 3) return "—"; try { return maGhe.substring(0, 3); } catch (Exception e) { return "—"; } }
    private int extractToaNumFromDB(String maGhe) { try { return Integer.parseInt(maGhe.substring(3, 5)); } catch (Exception e) { return 1; } }
    private String fmtTien(long a) { return String.format("%,d đ", a).replace(",", "."); }
    private String safe(String[] a, int i) { return (a!=null && i<a.length && a[i]!=null) ? a[i] : "—"; }

    private JPanel buildSuccessBox() { JPanel p = new JPanel(new BorderLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(236, 252, 240)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); g2.dispose(); } }; p.setBorder(new EmptyBorder(16, 20, 16, 20)); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55)); JLabel msg = new JLabel("Thông tin hợp lệ. Vui lòng xác nhận lộ trình đổi vé."); msg.setFont(FONT_B14); msg.setForeground(new Color(30, 130, 70)); msg.setHorizontalAlignment(SwingConstants.CENTER); p.add(msg, BorderLayout.CENTER); return p; }
    private JPanel buildCard(String t) { JPanel card = new JPanel(new BorderLayout(0, 12)); card.setBackground(Color.WHITE); card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(20, 24, 20, 24))); JLabel lbTitle = new JLabel(t); lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16)); card.add(lbTitle, BorderLayout.NORTH); return card; }
    private JLabel headerLabel(String t, Color c) { JLabel lb = new JLabel(t); lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13)); lb.setForeground(c); lb.setHorizontalAlignment(SwingConstants.CENTER); return lb; }
    private JLabel fieldLabel(Color c) { JLabel lb = new JLabel("—"); lb.setFont(FONT_B14); lb.setForeground(c); lb.setOpaque(true); lb.setBackground(GuiTheme.SEARCH_FIELD_BG); lb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, false), new EmptyBorder(8, 12, 8, 12))); return lb; }
    private JButton makeNavyButton(String t, int w, int h) { JButton btn = new JButton(t) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(NAVY); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); g2.setColor(Color.WHITE); g2.setFont(FONT_B14); FontMetrics fm = g2.getFontMetrics(); g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2); g2.dispose(); } }; btn.setPreferredSize(new Dimension(w, h)); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn; }
    private JButton makeSecondaryButton(String t, int w, int h) { JButton btn = new JButton(t) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(new Color(240, 243, 248)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); g2.setColor(BORDER); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8); g2.setColor(GuiTheme.TEXT); g2.setFont(FONT_B14); FontMetrics fm = g2.getFontMetrics(); g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose(); } }; btn.setPreferredSize(new Dimension(w, h)); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn; }
    private JButton makeTabButton(String t, boolean[] s, Runnable r) { JButton btn = new JButton(t) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(s[0] ? NAVY : Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); g2.setColor(s[0] ? Color.WHITE : NAVY); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8); g2.setFont(FONT_B14); FontMetrics fm = g2.getFontMetrics(); g2.drawString(getText(), (getWidth() - fm.stringWidth(getText()))/2, (getHeight() + fm.getAscent() - fm.getDescent())/2); g2.dispose(); } }; btn.setPreferredSize(new Dimension(100, 30)); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.addActionListener(e -> r.run()); return btn; }
}