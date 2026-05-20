package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class DoiVeGUI1 extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color NAVY = GuiTheme.NAVY;
    private static final Color NEW_FG = GuiTheme.NAVY;
    private static final Font FONT_B14 = new Font("Segoe UI", Font.BOLD, 14);

    // --- DỮ LIỆU TĨNH ---
    private static String s_maVe = "";
    private static String[] s_dataCu = new String[0];
    private static String s_chuyenDiMoi = "", s_ngayDiMoi = "", s_gheDiMoi = "";
    private static String s_chuyenVeMoi = "—", s_ngayVeMoi = "—", s_gheVeMoi = "—";

    public static void setDonDoiKhuHoi(String maVe, String[] dataCu,
                                       String chuyenDi, String ngayDi, String gheDi,
                                       String chuyenVe, String ngayVe, String gheVe) {
        s_maVe = maVe; s_dataCu = dataCu.clone();
        s_chuyenDiMoi = chuyenDi; s_ngayDiMoi = ngayDi; s_gheDiMoi = gheDi;
        s_chuyenVeMoi = chuyenVe; s_ngayVeMoi = ngayVe; s_gheVeMoi = gheVe;
    }

    private final AppFrame appFrame;
    private JPanel pnlTabController, pnlCardContainer;
    private CardLayout cardLayout;
    private final boolean[] activeDi = {true}, activeVe = {false};

    // Labels cập nhật động - cột MỚI
    private JLabel valMaVeDi, valChuyenDi, valToaDi, valGheDi, valNgayDi, valGaDi_Di, valGaDenDi;
    private JLabel valMaVeVe, valChuyenVe, valToaVe, valGheVe, valNgayVe, valGaDi_Ve, valGaDenVe;
    // Labels cập nhật động - cột CŨ
    private JLabel oldMaVeDi, oldChuyenDi, oldToaDi, oldGheDi, oldNgayDi, oldGaDi_Di_old, oldGaDenDi_old;
    private JLabel oldMaVeVe, oldChuyenVe, oldToaVe, oldGheVe, oldNgayVe, oldGaDi_Ve_old, oldGaDenVe_old;
    private JLabel lbChenhLech, lbTongThu;
    private long tongLePhi = 30000;

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
        pnlTabController.setVisible(isKhuHoi); // Ẩn hiện tab dựa vào vé 1 chiều hay khứ hồi

        String oldGaDi = safe(s_dataCu, 1);
        String oldGaDen = safe(s_dataCu, 2);

        // --- ĐỔ DỮ LIỆU CỘT CŨ - CHIỀU ĐI ---
        oldMaVeDi.setText(s_maVe);
        oldChuyenDi.setText(safe(s_dataCu, 0));
        oldToaDi.setText(extractToaFromDB(safe(s_dataCu, 7)));
        oldGheDi.setText(extractGheFromDB(safe(s_dataCu, 7)));
        oldNgayDi.setText(safe(s_dataCu, 5));
        oldGaDi_Di_old.setText(oldGaDi);
        oldGaDenDi_old.setText(oldGaDen);

        // --- ĐỔ DỮ LIỆU CỘT CŨ - CHIỀU VỀ (ga đảo ngược) ---
        oldMaVeVe.setText(s_maVe);
        oldChuyenVe.setText(safe(s_dataCu, 0));
        oldToaVe.setText(extractToaFromDB(safe(s_dataCu, 7)));
        oldGheVe.setText(extractGheFromDB(safe(s_dataCu, 7)));
        oldNgayVe.setText("—");
        oldGaDi_Ve_old.setText(oldGaDen);
        oldGaDenVe_old.setText(oldGaDi);

        // --- ĐỔ DỮ LIỆU CỘT MỚI - CHIỀU ĐI ---
        valMaVeDi.setText(s_maVe);
        valChuyenDi.setText(s_chuyenDiMoi);
        valToaDi.setText(extractToa(s_gheDiMoi));
        valGheDi.setText(extractGhe(s_gheDiMoi));
        valNgayDi.setText(s_ngayDiMoi);
        valGaDi_Di.setText(oldGaDi);
        valGaDenDi.setText(oldGaDen);

        // --- ĐỔ DỮ LIỆU TAB CHIỀU VỀ ---
        if (isKhuHoi) {
            valMaVeVe.setText(s_maVe + "-VE");
            valChuyenVe.setText(s_chuyenVeMoi);
            valToaVe.setText(extractToa(s_gheVeMoi));
            valGheVe.setText(extractGhe(s_gheVeMoi));
            valNgayVe.setText(s_ngayVeMoi);
            // Tab chiều về: Ga đi và Ga đến đổi ngược lại
            valGaDi_Ve.setText(oldGaDen);
            valGaDenVe.setText(oldGaDi);
        } else {
            // Nếu là 1 chiều, ép luôn luôn hiện Tab Đi
            activeDi[0] = true; activeVe[0] = false;
            cardLayout.show(pnlCardContainer, "DI");
        }

        calcPriceAndRefresh();
        revalidate(); repaint();
    }

    private void calcPriceAndRefresh() {
        long oldPrice = 0;
        try { oldPrice = Long.parseLong(safe(s_dataCu, 8).replaceAll("[^0-9]", "")); } catch(Exception e) {}

        double oldFactor = (extractToaNumFromDB(safe(s_dataCu, 7)) >= 9) ? 1.2 : 1.0;
        double newFactorDi = (extractToaNum(s_gheDiMoi) >= 9) ? 1.2 : 1.0;

        double basePrice = oldPrice / oldFactor;
        long diff = Math.round((basePrice * newFactorDi) - oldPrice);

        if (!s_chuyenVeMoi.equals("—")) {
            double newFactorVe = (extractToaNum(s_gheVeMoi) >= 9) ? 1.2 : 1.0;
            diff += Math.round((basePrice * newFactorVe) - oldPrice);
        }

        tongLePhi = 30000 + diff;
        lbChenhLech.setText("Chênh lệch hạng ghế: " + (diff > 0 ? "+" : "") + fmtTien(diff));
        lbTongThu.setText("Tổng thu: " + fmtTien(tongLePhi));
        lbTongThu.setForeground(tongLePhi >= 0 ? new Color(180, 60, 0) : new Color(30, 120, 60));
    }

    private JPanel buildCompareCard() {
        JPanel card = buildCard("So sánh chi tiết lộ trình");
        cardLayout = new CardLayout();
        pnlCardContainer = new JPanel(cardLayout); pnlCardContainer.setOpaque(false);

        // Khởi tạo Label chiều đi - cột MỚI
        valMaVeDi=fieldLabel(GuiTheme.TEXT); valChuyenDi=fieldLabel(NEW_FG); valToaDi=fieldLabel(NEW_FG); valGheDi=fieldLabel(NEW_FG); valNgayDi=fieldLabel(NEW_FG); valGaDi_Di=fieldLabel(GuiTheme.TEXT); valGaDenDi=fieldLabel(GuiTheme.TEXT);
        // Khởi tạo Label chiều về - cột MỚI
        valMaVeVe=fieldLabel(GuiTheme.TEXT); valChuyenVe=fieldLabel(NEW_FG); valToaVe=fieldLabel(NEW_FG); valGheVe=fieldLabel(NEW_FG); valNgayVe=fieldLabel(NEW_FG); valGaDi_Ve=fieldLabel(GuiTheme.TEXT); valGaDenVe=fieldLabel(GuiTheme.TEXT);
        // Khởi tạo Label chiều đi - cột CŨ
        oldMaVeDi=fieldLabel(GuiTheme.SUB_TEXT); oldChuyenDi=fieldLabel(GuiTheme.SUB_TEXT); oldToaDi=fieldLabel(GuiTheme.SUB_TEXT); oldGheDi=fieldLabel(GuiTheme.SUB_TEXT); oldNgayDi=fieldLabel(GuiTheme.SUB_TEXT); oldGaDi_Di_old=fieldLabel(GuiTheme.SUB_TEXT); oldGaDenDi_old=fieldLabel(GuiTheme.SUB_TEXT);
        // Khởi tạo Label chiều về - cột CŨ
        oldMaVeVe=fieldLabel(GuiTheme.SUB_TEXT); oldChuyenVe=fieldLabel(GuiTheme.SUB_TEXT); oldToaVe=fieldLabel(GuiTheme.SUB_TEXT); oldGheVe=fieldLabel(GuiTheme.SUB_TEXT); oldNgayVe=fieldLabel(GuiTheme.SUB_TEXT); oldGaDi_Ve_old=fieldLabel(GuiTheme.SUB_TEXT); oldGaDenVe_old=fieldLabel(GuiTheme.SUB_TEXT);

        // Xây dựng 2 Grid
        JPanel gridDi = createCompareGrid(true,
                oldMaVeDi, oldChuyenDi, oldToaDi, oldGheDi, oldNgayDi, oldGaDi_Di_old, oldGaDenDi_old,
                valMaVeDi, valChuyenDi, valToaDi, valGheDi, valNgayDi, valGaDi_Di, valGaDenDi);
        JPanel gridVe = createCompareGrid(false,
                oldMaVeVe, oldChuyenVe, oldToaVe, oldGheVe, oldNgayVe, oldGaDi_Ve_old, oldGaDenVe_old,
                valMaVeVe, valChuyenVe, valToaVe, valGheVe, valNgayVe, valGaDi_Ve, valGaDenVe);

        pnlCardContainer.add(gridDi, "DI");
        pnlCardContainer.add(gridVe, "VE");

        JPanel pnlFooter = new JPanel(new GridLayout(2, 1, 0, 5)); pnlFooter.setOpaque(false);
        lbChenhLech = new JLabel("Chênh lệch: 0 đ"); lbChenhLech.setFont(FONT_B14); lbChenhLech.setForeground(GuiTheme.SUB_TEXT);
        lbTongThu = new JLabel("Tổng thu: 30.000 đ"); lbTongThu.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
        pnlFooter.add(lbChenhLech); pnlFooter.add(lbTongThu);

        card.add(pnlCardContainer, BorderLayout.CENTER);
        card.add(pnlFooter, BorderLayout.SOUTH);
        return card;
    }

    // Biến boolean isDi quyết định nhãn hiển thị là "Ngày đi" hay "Ngày về"
    private JPanel createCompareGrid(boolean isDi,
                                     JLabel oldMaVe, JLabel oldChuyen, JLabel oldToa, JLabel oldGhe, JLabel oldNgay, JLabel oldGa1, JLabel oldGa2,
                                     JLabel vMa, JLabel vCh, JLabel vTo, JLabel vGh, JLabel vNgay, JLabel vGaDi, JLabel vGaDen) {
        JPanel grid = new JPanel(new GridBagLayout()); grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 10, 5, 10);

        gbc.gridy = 0; gbc.gridx = 1; grid.add(headerLabel("HIỆN TẠI (CŨ)", GuiTheme.SUB_TEXT), gbc);
        gbc.gridx = 3; grid.add(headerLabel("ĐỔI SANG (MỚI)", NEW_FG), gbc);

        String textNgay = isDi ? "Ngày đi" : "Ngày về";

        addGridRow(grid, 1, "Mã vé", oldMaVe, vMa);
        addGridRow(grid, 2, "Chuyến", oldChuyen, vCh);
        addGridRow(grid, 3, "Toa", oldToa, vTo);
        addGridRow(grid, 4, "Ghế", oldGhe, vGh);
        addGridRow(grid, 5, textNgay, oldNgay, vNgay);
        addGridRow(grid, 6, "Ga đi", oldGa1, vGaDi);
        addGridRow(grid, 7, "Ga đến", oldGa2, vGaDen);
        return grid;
    }

    private void addGridRow(JPanel grid, int y, String title, JLabel oldV, JLabel newV) {
        GridBagConstraints gbc = new GridBagConstraints(); gbc.gridy = y; gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
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
        // Chỉ đẩy sang màn hình thanh toán, logic DB thực hiện ở DoiVeGUI2
        String maGheDbDi = getMaGheMoiDB(s_gheDiMoi);
        String hienThiDi = extractToa(s_gheDiMoi) + " - " + extractGhe(s_gheDiMoi);
        DoiVeGUI2.setDuLieuThanhToan(s_maVe, s_dataCu, s_chuyenDiMoi, s_ngayDiMoi, maGheDbDi, hienThiDi, tongLePhi);
        appFrame.showCard("doi-ve-step-3");
    }

    // --- CÁC HÀM HELPERS ---
    private String extractToa(String full) { try { return "Toa " + Integer.parseInt(full.split("-")[0].trim().substring(1, 3)); } catch(Exception e) { return "—"; } }
    private String extractGhe(String full) { try { return full.split("-")[1].trim(); } catch(Exception e) { return "—"; } }
    private int extractToaNum(String full) { try { return Integer.parseInt(full.split("-")[0].trim().substring(1, 3)); } catch(Exception e) { return 1; } }
    private String getMaGheMoiDB(String fullStr) { if (fullStr == null || !fullStr.contains("-")) return fullStr; String[] parts = fullStr.split("-"); return parts[1].trim() + parts[0].trim(); }

    // Parse maGhe thẳng từ DB: "G05T03SEVN001" → toa = "T03SEVN001", ghe = "G05"
    private String extractToaFromDB(String maGhe) {
        if (maGhe == null || maGhe.length() < 4) return "—";
        try { return maGhe.substring(3); } catch (Exception e) { return "—"; } // bỏ "G05" → "T03SEVN001"
    }
    private String extractGheFromDB(String maGhe) {
        if (maGhe == null || maGhe.length() < 3) return "—";
        try { return maGhe.substring(0, 3); } catch (Exception e) { return "—"; } // lấy "G05"
    }
    private int extractToaNumFromDB(String maGhe) {
        try { return Integer.parseInt(maGhe.substring(3, 5)); } catch (Exception e) { return 1; } // lấy "03" → 3
    }
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