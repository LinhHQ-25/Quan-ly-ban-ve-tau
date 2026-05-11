package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import connect_DB.Connect_DB;

public class DoiVeGUI extends JPanel {
    private static final Color BORDER      = new Color(210, 215, 224);
    private static final Color WARN_FG     = new Color(180, 60, 0);
    private static final Color OK_FG       = new Color(30, 120, 60);
    private static final Color NAVY        = GuiTheme.NAVY;
    private static final Color ACCENT      = new Color(0, 120, 215);
    private static final Color ACCENT_LITE = new Color(224, 240, 255);
    private static final Color NAVY_SEL    = new Color(100, 160, 230);
    private static final Color SEAT_OK     = new Color(28, 57, 110);
    private static final Color SEAT_TAKEN  = new Color(180, 190, 210);
    private static final Color SEAT_SEL    = new Color(100, 180, 255);
    private static final int   FIELD_H     = 28;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static String   s_maVe = "";
    private static String[] s_data = new String[0];
    public static void setVeDuocChon(String maVe, String[] data) {
        s_maVe = maVe;
        s_data = data.clone();
    }

    // ── Danh sách chuyến từ DB: [maChuyen, tenTau, thoiGianKH, thoiGianDen] ──
    private final java.util.List<String[]> chuyenList = new ArrayList<>();

    private final AppFrame appFrame;
    private int trangChuyen = 0;
    private int chuyenIdx = -1, toaIdx = -1;
    private final Set<Integer> gheChon = new LinkedHashSet<>();
    private int toaCount = 10;

    private JTextField tfMaVe, tfChuyen, tfGaDi, tfGaDen, tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia;
    private JLabel lbTrangThai;

    private JPanel pnlChuyenTable;
    private JButton btnPrevChuyen, btnNextChuyen;

    private JPanel pnlToaContainer;
    private JPanel pnlToaScroll;

    private JPanel pnlGheContainer;
    private JPanel pnlGhe;
    private JLabel lblToaHienTai, lblToaTrong;

    private JLabel  lbWarning;
    private JButton btnTiepTuc;

    public DoiVeGUI(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);
        loadChuyenFromDB();

        JPanel pnlPage = new JPanel(new BorderLayout(0, 10));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(
                0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT
        ));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.add(buildCurrentInfoCard());
        stack.add(Box.createVerticalStrut(15));
        stack.add(buildChuyenSection());
        stack.add(Box.createVerticalStrut(15));
        stack.add(buildToaGheSection());

        JScrollPane outer = new JScrollPane(stack);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);

        pnlPage.add(outer,          BorderLayout.CENTER);
        pnlPage.add(buildBottomBar(), BorderLayout.SOUTH);
        add(pnlPage, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        fillCurrentInfo();
        validateDoiVe();
        gheChon.clear();
        chuyenIdx = 0; toaIdx = 0; trangChuyen = 0;
        if (!chuyenList.isEmpty()) toaCount = getToaCountForChuyen(chuyenIdx);
        refreshChuyen();
        refreshToaGhe();
        updateBottomBar();
    }

    // DATA LAYER
    private void loadChuyenFromDB() {
        chuyenList.clear();
        String sql = "SELECT ct.maChuyen, t.tenTau, ct.thoiGianKhoiHanh, ct.thoiGianDuKien " +
                "FROM ChuyenTau ct JOIN Tau t ON ct.maTau = t.maTau " +
                "ORDER BY ct.thoiGianKhoiHanh";

        try (Connection conn = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                Timestamp kh = rs.getTimestamp("thoiGianKhoiHanh");
                Timestamp dd = rs.getTimestamp("thoiGianDuKien");
                chuyenList.add(new String[]{
                        rs.getString("maChuyen"),
                        rs.getString("tenTau"),
                        kh != null ? sdf.format(kh) : "",
                        dd != null ? sdf.format(dd) : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getToaCountForChuyen(int ci) {
        if (ci < 0 || ci >= chuyenList.size()) return 10;
        int count = 0;
        String sql = "SELECT COUNT(*) FROM ToaTau tt " +
                "JOIN ChuyenTau ct ON tt.maTau = ct.maTau " +
                "WHERE ct.maChuyen = ?";
        try (Connection conn = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chuyenList.get(ci)[0]);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return Math.max(count, 1);
    }

    private Set<Integer> gheDaDatFromDB(int ci, int ti) {
        Set<Integer> result = new LinkedHashSet<>();
        if (ci < 0 || ci >= chuyenList.size()) return result;
        String sql = "SELECT v.maGhe FROM Ve v " +
                "JOIN Ghe g ON v.maGhe = g.maGhe " +
                "JOIN ToaTau tt ON g.maToaTau = tt.maToaTau " +
                "JOIN ChuyenTau ct ON tt.maTau = ct.maTau " +
                "WHERE ct.maChuyen = ? AND tt.soToa = ? " +
                "AND v.trangThaiVe IN ('CHO_THANH_TOAN', 'DA_THANH_TOAN')";
        try (Connection conn = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chuyenList.get(ci)[0]);
            ps.setInt(2, ti + 1);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String maGhe = rs.getString("maGhe");
                try {
                    result.add(Integer.parseInt(maGhe.replaceAll("[^0-9]", "")));
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // UI BUILDERS
    private JPanel buildCurrentInfoCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel lb = new JLabel("Thông tin vé hiện tại");
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lb.setForeground(GuiTheme.TEXT);
        lb.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(lb, BorderLayout.NORTH);

        tfMaVe = readField(); tfChuyen  = readField();
        tfGaDi = readField(); tfGaDen   = readField();
        tfNgayGio = readField(); tfLoai = readField();
        tfGhe  = readField(); tfSoLuong = readField();
        tfGia  = readField();

        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 8));
        grid.setOpaque(false);
        grid.add(fieldBox("Mã vé",       tfMaVe));
        grid.add(fieldBox("Chuyến tàu",  tfChuyen));
        grid.add(fieldBox("Ga đi",       tfGaDi));
        grid.add(fieldBox("Ga đến",      tfGaDen));
        grid.add(fieldBox("Ngày/Giờ KH", tfNgayGio));
        grid.add(fieldBox("Loại vé",     tfLoai));
        grid.add(fieldBox("Số ghế",      tfGhe));
        grid.add(fieldBox("Số lượng",    tfSoLuong));

        JPanel row3 = new JPanel(new GridLayout(1, 4, 12, 0));
        row3.setOpaque(false);
        row3.add(fieldBox("Đơn giá", tfGia));

        lbTrangThai = new JLabel("—");
        lbTrangThai.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbTrangThai.setForeground(GuiTheme.SUB_TEXT);

        JPanel ttCell = new JPanel();
        ttCell.setLayout(new BoxLayout(ttCell, BoxLayout.Y_AXIS));
        ttCell.setOpaque(false);
        JLabel ttLbl = new JLabel("Điều kiện đổi");
        ttLbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        ttLbl.setForeground(GuiTheme.SUB_TEXT);
        ttLbl.setAlignmentX(LEFT_ALIGNMENT);
        lbTrangThai.setAlignmentX(LEFT_ALIGNMENT);
        ttCell.add(ttLbl);
        ttCell.add(Box.createVerticalStrut(4));
        ttCell.add(lbTrangThai);
        row3.add(ttCell);
        row3.add(new JLabel()); row3.add(new JLabel());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(grid);
        content.add(Box.createVerticalStrut(8));
        content.add(row3);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildChuyenSection() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Chọn chuyến tàu mới");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(GuiTheme.TEXT);
        wrapper.add(lblTitle, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout(2, 0));
        row.setOpaque(false);

        btnPrevChuyen = makeNavArrow("‹");
        btnNextChuyen = makeNavArrow("›");

        btnPrevChuyen.addActionListener(e -> {
            if (trangChuyen > 0) { trangChuyen--; refreshChuyen(); }
        });
        btnNextChuyen.addActionListener(e -> {
            if ((trangChuyen + 1) * 5 < chuyenList.size()) { trangChuyen++; refreshChuyen(); }
        });

        pnlChuyenTable = new JPanel();
        pnlChuyenTable.setOpaque(false);

        row.add(btnPrevChuyen, BorderLayout.WEST);
        row.add(pnlChuyenTable, BorderLayout.CENTER);
        row.add(btnNextChuyen, BorderLayout.EAST);
        row.setPreferredSize(new Dimension(0, 140));

        wrapper.add(row, BorderLayout.CENTER);
        return wrapper;
    }

    private void refreshChuyen() {
        if (pnlChuyenTable == null) return;
        pnlChuyenTable.removeAll();

        if (chuyenList.isEmpty()) {
            btnPrevChuyen.setVisible(false);
            btnNextChuyen.setVisible(false);
            pnlChuyenTable.setLayout(new GridBagLayout());
            JLabel empty = new JLabel("Không có chuyến tàu nào.");
            empty.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            pnlChuyenTable.add(empty);
        } else {
            btnPrevChuyen.setVisible(true);
            btnNextChuyen.setVisible(true);

            pnlChuyenTable.setLayout(new GridLayout(1, 5, 10, 0));
            int from = trangChuyen * 5;
            int to = Math.min(from + 5, chuyenList.size());

            for (int i = from; i < to; i++) {
                pnlChuyenTable.add(buildTrainCard(i));
            }
            // Điền panel rỗng cho đủ layout
            for (int i = to - from; i < 5; i++) {
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                pnlChuyenTable.add(empty);
            }
            btnPrevChuyen.setEnabled(trangChuyen > 0);
            btnNextChuyen.setEnabled((trangChuyen + 1) * 5 < chuyenList.size());
        }
        pnlChuyenTable.revalidate();
        pnlChuyenTable.repaint();
    }

    private JPanel buildTrainCard(int ci) {
        boolean sel = (ci == chuyenIdx);
        String[] ch = chuyenList.get(ci);

        String timeDi = ch[2].length() > 10 ? ch[2].substring(11) : ch[2];
        String timeDen = ch[3].length() > 10 ? ch[3].substring(11) : ch[3];

        JPanel info = new JPanel(new GridLayout(4, 1, 0, 0));
        info.setOpaque(false);
        String[] labels = {"TG đi:", timeDi, "TG đến:", timeDen};
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setFont(GuiTheme.font("Segoe UI", Font.BOLD, (i % 2 == 0) ? 10 : 11));
            l.setForeground((i % 2 == 0) ? new Color(100, 100, 100) : Color.BLACK);
            info.add(l);
        }

        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            private boolean isHover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { isHover = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        chuyenIdx = ci;
                        toaIdx = 0;
                        gheChon.clear();
                        toaCount = getToaCountForChuyen(chuyenIdx);
                        refreshChuyen();
                        refreshToaGhe();
                        updateBottomBar();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int trainH = h - 20;

                Color mainColor = new Color(160, 160, 160);
                if (sel) mainColor = ACCENT;
                else if (isHover) mainColor = new Color(255, 200, 0);

                g2.setColor(mainColor);
                g2.fillRoundRect(2, 2, w - 4, trainH, 25, 25);

                g2.setColor(Color.WHITE);
                int infoH = info.getPreferredSize().height + 10;
                int infoW = w - 16;
                g2.fillRoundRect(8, 32, infoW, infoH, 12, 12);

                g2.setColor(Color.WHITE);
                g2.fillOval(w/4 - 8, trainH - 14, 16, 16);
                g2.fillOval(3*w/4 - 8, trainH - 14, 16, 16);

                g2.setColor(Color.BLACK);
                int railY = h - 15;
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(15, railY, 5, h - 2);
                g2.drawLine(w - 15, railY, w - 5, h - 2);
                for (int i = 0; i <= 3; i++) {
                    int y = railY + (i * 4);
                    g2.drawLine(15 - (i*2), y, w - 15 + (i*2), y);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel badge = new JLabel(ch[1], SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g); g2.dispose();
            }
        };
        badge.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);
        card.add(badgeWrap, BorderLayout.NORTH);

        JPanel infoWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 5));
        infoWrapper.setOpaque(false);
        infoWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
        infoWrapper.add(info);

        card.add(infoWrapper, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildToaGheSection() {
        JPanel sec = new JPanel(new BorderLayout(0, 10));
        sec.setOpaque(false);
        sec.setAlignmentX(LEFT_ALIGNMENT);

        // --- SECTION TOA ---
        pnlToaContainer = new JPanel(new BorderLayout(0, 4));
        pnlToaContainer.setOpaque(false);

        JLabel ttlToa = new JLabel("Danh sách toa tàu");
        ttlToa.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        ttlToa.setForeground(GuiTheme.TEXT);
        pnlToaContainer.add(ttlToa, BorderLayout.NORTH);

        pnlToaScroll = new JPanel();
        pnlToaScroll.setLayout(new BoxLayout(pnlToaScroll, BoxLayout.X_AXIS));
        pnlToaScroll.setOpaque(false);

        JScrollPane toaSP = new JScrollPane(pnlToaScroll, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        toaSP.setBorder(null);
        toaSP.setOpaque(false);
        toaSP.getViewport().setOpaque(false);
        toaSP.setPreferredSize(new Dimension(0, 100));
        toaSP.getHorizontalScrollBar().setUnitIncrement(20);

        pnlToaContainer.add(toaSP, BorderLayout.CENTER);
        sec.add(pnlToaContainer, BorderLayout.NORTH);

        // --- SECTION GHẾ ---
        pnlGheContainer = new JPanel(new BorderLayout(0, 4));
        pnlGheContainer.setOpaque(false);

        lblToaHienTai = new JLabel("Toa: --", SwingConstants.CENTER);
        lblToaHienTai.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lblToaHienTai.setForeground(NAVY);
        pnlGheContainer.add(lblToaHienTai, BorderLayout.NORTH);

        JPanel pnlGheWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 205, 225));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                g2.dispose();
            }
        };
        pnlGheWrapper.setOpaque(false);
        pnlGheWrapper.setBorder(new EmptyBorder(4, 4, 4, 4));

        pnlGhe = new JPanel();
        pnlGhe.setOpaque(false);
        pnlGheWrapper.add(pnlGhe, BorderLayout.CENTER);
        pnlGheContainer.add(pnlGheWrapper, BorderLayout.CENTER);

        JPanel botLegendArea = new JPanel(new BorderLayout());
        botLegendArea.setOpaque(false);
        botLegendArea.setBorder(new EmptyBorder(4, 0, 2, 0));

        lblToaTrong = new JLabel("Số ghế còn trống: --", SwingConstants.CENTER);
        lblToaTrong.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblToaTrong.setForeground(GuiTheme.TEXT);

        JPanel legendBoxes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        legendBoxes.setOpaque(false);
        legendBoxes.add(makeLegend(SEAT_SEL, "Đang chọn"));
        legendBoxes.add(makeLegend(SEAT_OK, "Còn trống"));
        legendBoxes.add(makeLegend(SEAT_TAKEN, "Đã đặt"));

        JPanel leftDummy = new JPanel();
        leftDummy.setOpaque(false);
        leftDummy.setPreferredSize(legendBoxes.getPreferredSize());

        botLegendArea.add(leftDummy, BorderLayout.WEST);
        botLegendArea.add(lblToaTrong, BorderLayout.CENTER);
        botLegendArea.add(legendBoxes, BorderLayout.EAST);

        pnlGheContainer.add(botLegendArea, BorderLayout.SOUTH);
        sec.add(pnlGheContainer, BorderLayout.CENTER);

        return sec;
    }

    private void refreshToaGhe() {
        if (chuyenIdx < 0 || chuyenIdx >= chuyenList.size()) {
            if (pnlToaContainer != null) pnlToaContainer.setVisible(false);
            if (pnlGheContainer != null) pnlGheContainer.setVisible(false);
            revalidate(); repaint();
            return;
        }

        if (pnlToaContainer != null) pnlToaContainer.setVisible(true);

        pnlToaScroll.removeAll();
        int gap = 14;
        pnlToaScroll.add(Box.createHorizontalStrut(gap));
        for (int t = 0; t < toaCount; t++) {
            pnlToaScroll.add(buildToaIcon("Toa " + (t + 1), t == toaIdx, t));
            if (t < toaCount - 1) pnlToaScroll.add(Box.createHorizontalStrut(gap));
        }
        pnlToaScroll.add(Box.createHorizontalStrut(gap));
        pnlToaScroll.revalidate();
        pnlToaScroll.repaint();

        if (toaIdx < 0) {
            if (pnlGheContainer != null) pnlGheContainer.setVisible(false);
            revalidate(); repaint();
            return;
        }

        if (pnlGheContainer != null) pnlGheContainer.setVisible(true);

        Set<Integer> daDat = gheDaDatFromDB(chuyenIdx, toaIdx);
        lblToaHienTai.setText("Toa: " + (toaIdx + 1));
        int trong = 0;
        for (int g = 1; g <= 28; g++) if (!daDat.contains(g)) trong++;
        lblToaTrong.setText("Số ghế còn trống: " + trong + "/28");

        pnlGhe.removeAll();
        pnlGhe.setLayout(new GridLayout(2, 14, 4, 4));
        pnlGhe.setBorder(new EmptyBorder(6, 10, 6, 10));

        int need = getSoLuong();

        for (int i = 1; i <= 28; i++) {
            final int gNum = i;
            final boolean taken = daDat.contains(i);
            JButton btn = new JButton(String.valueOf(i)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = taken ? SEAT_TAKEN : gheChon.contains(gNum) ? SEAT_SEL : SEAT_OK;
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                    g2.setColor(Color.WHITE);
                    g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    String t = getText();
                    g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            btn.setPreferredSize(new Dimension(30, 26));
            btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
            if (!taken) {
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.addActionListener(ev -> {
                    if (gheChon.contains(gNum)) gheChon.remove(gNum);
                    else if (gheChon.size() < need) gheChon.add(gNum);
                    refreshToaGhe();
                    updateBottomBar();
                });
            }
            pnlGhe.add(btn);
        }
        pnlGhe.revalidate(); pnlGhe.repaint();
        revalidate(); repaint();
    }

    private JPanel buildToaIcon(String maToa, boolean sel, int ti) {
        JPanel p = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(sel ? new Color(210, 232, 255) : new Color(235, 242, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                if (sel) {
                    g2.setColor(NAVY_SEL);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 7, 7);
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(3, 3, 3, 3));
        p.setMaximumSize(new Dimension(58, 78));
        p.setPreferredSize(new Dimension(58, 78));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel icon = new JLabel();
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(52, 44));

        Icon toaImg = loadAndScaleIcon("/Images/logoToaTau.png", 56, 36);
        if (toaImg != null) {
            icon.setIcon(toaImg);
        } else {
            icon.setText("Toa");
            icon.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        }

        JLabel lbl = new JLabel(maToa, SwingConstants.CENTER);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(sel ? NAVY : new Color(55, 75, 115));

        p.add(icon, BorderLayout.CENTER);
        p.add(lbl, BorderLayout.SOUTH);
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                toaIdx = ti;
                gheChon.clear();
                refreshToaGhe();
                updateBottomBar();
            }
        });
        return p;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        lbWarning = new JLabel(" ");
        lbWarning.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lbWarning.setForeground(WARN_FG);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        left.setBackground(Color.WHITE);
        left.add(lbWarning);

        JButton btnBack = makeOutlineBtn("Quay lại", 130, 38);
        btnBack.addActionListener(e -> appFrame.showCard("doi-tra"));

        btnTiepTuc = makeNavyBtn("Tiếp tục", 130, 38);
        btnTiepTuc.setEnabled(false);
        btnTiepTuc.addActionListener(e -> handleTiepTuc());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setBackground(Color.WHITE);
        right.add(btnBack);
        right.add(btnTiepTuc);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void updateBottomBar() {
        if (btnTiepTuc == null) return;
        int need = getSoLuong();
        boolean valid = gheChon.size() >= need && lbTrangThai != null && lbTrangThai.getText().startsWith("Hợp lệ");

        btnTiepTuc.setEnabled(valid);
        if (lbWarning != null) {
            if (!valid && lbTrangThai != null && !lbTrangThai.getText().startsWith("Hợp lệ")) {
                lbWarning.setText("Vé không đủ điều kiện đổi");
            } else if (!valid) {
                lbWarning.setText("Đã chọn " + gheChon.size() + "/" + need + " ghế. Chọn đủ để tiếp tục.");
            } else {
                lbWarning.setText("Đã chọn " + gheChon.size() + "/" + need + " ghế. Thông tin hợp lệ.");
                lbWarning.setForeground(OK_FG);
            }
        }
    }

    // LOGIC
    private void fillCurrentInfo() {
        if (s_data.length < 8) { clearFields(); return; }
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

    private void validateDoiVe() {
        if (lbTrangThai == null) return;
        if (s_data.length < 8 || s_maVe.isEmpty()) {
            lbTrangThai.setText("Chưa có vé được chọn");
            lbTrangThai.setForeground(GuiTheme.SUB_TEXT);
            return;
        }

        boolean nhom = s_data[3].toLowerCase().contains("nhóm");
        long gioTong = tinhGio(s_data[4]);

        long gioThuc = Math.max(gioTong, 0);
        long d = gioThuc / 24;
        long h = gioThuc % 24;

        String timeStr = "";
        if (d > 0) {
            timeStr = d + " ngày" + (h > 0 ? " " + h + " giờ" : "");
        } else {
            timeStr = h + " giờ";
        }

        if (nhom) {
            lbTrangThai.setText("Vé nhóm — Không được đổi");
            lbTrangThai.setForeground(new Color(180, 30, 30));
        } else if (gioTong < 24) {
            lbTrangThai.setText("Quá hạn — còn " + timeStr + " (cần ≥ 24h)");
            lbTrangThai.setForeground(new Color(180, 30, 30));
        } else {
            lbTrangThai.setText("Hợp lệ — còn " + timeStr);
            lbTrangThai.setForeground(OK_FG);
        }
    }

    private void handleTiepTuc() {
        if (gheChon.isEmpty() || chuyenList.isEmpty()) return;
        String[] ch = chuyenList.get(chuyenIdx);
        String chuyenMoi = ch[1];
        String ngayMoi = ch[2];

        String tenToa = "Toa " + (toaIdx + 1);
        StringBuilder gheSb = new StringBuilder();
        for (int g : gheChon) {
            if (gheSb.length() > 0) gheSb.append(", ");
            gheSb.append(tenToa).append(" - G").append(String.format("%02d", g));
        }
        DoiVeGUI1.setDonDoi(s_maVe, s_data, chuyenMoi, ngayMoi, gheSb.toString());
        appFrame.showCard("doi-ve-step-2");
    }

    private void clearFields() {
        for (JTextField tf : new JTextField[]{tfMaVe,tfChuyen,tfGaDi,tfGaDen,tfNgayGio,tfLoai,tfGhe,tfSoLuong,tfGia})
            if (tf != null) tf.setText("—");
    }

    private int getSoLuong() {
        if (s_data.length > 5) {
            try {
                return Integer.parseInt(s_data[5].replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
        }
        return 1;
    }

    private static long tinhGio(String s) {
        try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(s, FMT)); }
        catch (Exception e) { return -1; }
    }

    // UI HELPERS
    private JTextField readField() {
        JTextField tf = new JTextField("—");
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tf.setEditable(false); tf.setForeground(GuiTheme.TEXT);
        tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false), new EmptyBorder(2, 6, 2, 6)));
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
        p.add(lb); p.add(Box.createVerticalStrut(4)); p.add(comp);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return p;
    }

    private JButton makeNavArrow(String t) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? new Color(215, 228, 248) : new Color(238, 240, 246));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isEnabled() ? NAVY : new Color(175, 185, 205));
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(26, 78));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel makeLegend(Color c, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3); g2.dispose();
            }
        };
        box.setPreferredSize(new Dimension(14, 14)); box.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(GuiTheme.TEXT);
        p.add(box); p.add(l);
        return p;
    }

    private JButton makeOutlineBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(198,215,242)
                        : getModel().isRollover() ? new Color(212,228,250) : new Color(226,236,252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(NAVY);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth()-fm.stringWidth(txt))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w, h));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeNavyBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
                        : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Icon loadAndScaleIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                Image scaledImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải icon: " + path);
        }
        return null;
    }
}