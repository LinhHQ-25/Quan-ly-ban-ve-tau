package gui;

import connect_DB.Connect_DB;
import service.AuthService;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * DashboardManagerGUI — Trang tổng quan dành cho Quản lý.
 *
 * Bố cục:
 *  ┌─ TopBar ──────────────────────────────────────────────────────────────┐
 *  │  Pill: Hôm nay | Tuần | Tháng | Năm | Khác    [Xuất BC] [Thống kê →]│
 *  ├─ KPI Row (5 card) ───────────────────────────────────────────────────┤
 *  │  Doanh thu · Vé bán · Vé hủy · Tiền mặt · Chuyển khoản              │
 *  ├─ Middle Row ─────────────────────────────────────────────────────────┤
 *  │  BarChart (doanh thu TM + CK theo ngày)  |  Donut (loại ghế)        │
 *  ├─ Bottom Row ─────────────────────────────────────────────────────────┤
 *  │  LineChart (số vé bán / vé hủy theo ngày)                            │
 *  └───────────────────────────────────────────────────────────────────────┘
 */
public class DashboardManagerGUI extends JPanel {

    // ── Màu sắc (giữ nguyên palette của project) ─────────────────────────────
    private static final Color NAVY     = new Color(37, 69, 121);
    private static final Color LIGHT_BG = new Color(245, 247, 251);
    private static final Color BORDER_C = new Color(210, 215, 224);
    private static final Color TM_COLOR = new Color(52, 152, 219);   // xanh dương — tiền mặt
    private static final Color CK_COLOR = new Color(39, 174, 96);    // xanh lá — chuyển khoản
    private static final Color SELL_CLR = new Color(52, 152, 219);   // đường vé bán
    private static final Color HUY_CLR  = new Color(231, 76, 60);    // đường vé hủy

    private static final Color[] KPI_ACCENTS = {
        new Color(37,  69, 121),   // doanh thu
        new Color(34, 139,  87),   // vé bán
        new Color(210,  50,  50),  // vé hủy
        new Color(180, 120,  30),  // tiền mặt
        new Color( 30, 140, 160),  // chuyển khoản
    };

    // ── Bộ lọc ────────────────────────────────────────────────────────────────
    private static final String[] PERIOD_LABELS = {"Hôm nay","Tuần này","Tháng","Năm","Khác"};
    private JToggleButton[] periodBtns;
    private ButtonGroup     periodGroup;
    private JPanel          extraFilterPanel;
    private JComboBox<Integer> cboThang, cboNam, cboNamYear;
    private JSpinner        spFrom, spTo;

    // ── KPI ───────────────────────────────────────────────────────────────────
    private final JLabel[] kpiValues = new JLabel[5];

    // ── Charts ────────────────────────────────────────────────────────────────
    private DashBarChart  barChart;
    private DashDonutChart donutChart;
    private DashLineChart lineChart;

    // ── Lưu dữ liệu để xuất BC ───────────────────────────────────────────────
    private long kDoanhThu, kTienMat, kCK;
    private int  kVeBan, kVeHuy;

    private AppFrameManager appFrame;

    // =========================================================================
    public DashboardManagerGUI() { this(null); }

    public DashboardManagerGUI(AppFrameManager appFrame) {
        this.appFrame = appFrame;
        setBackground(LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(14, GuiTheme.PAGE_PAD_LEFT,
                GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        page.add(buildTopBar());
        page.add(Box.createVerticalStrut(14));
        page.add(buildKpiRow());
        page.add(Box.createVerticalStrut(14));
        page.add(buildMiddleRow());
        page.add(Box.createVerticalStrut(14));
        page.add(buildBottomRow());
        page.add(Box.createVerticalStrut(14));

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> selectPeriod(0));
    }

    // =========================================================================
    // TOP BAR
    // =========================================================================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Pill group
        JPanel pillGroup = new JPanel(new GridLayout(1, PERIOD_LABELS.length));
        pillGroup.setBackground(Color.WHITE);
        pillGroup.setOpaque(true);
        pillGroup.setBorder(new LineBorder(BORDER_C, 1, true));
        pillGroup.setPreferredSize(new Dimension(340, 34));

        periodBtns = new JToggleButton[PERIOD_LABELS.length];
        periodGroup = new ButtonGroup();
        for (int i = 0; i < PERIOD_LABELS.length; i++) {
            final int idx = i;
            JToggleButton btn = new JToggleButton(PERIOD_LABELS[i]) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected()) {
                        g2.setColor(NAVY);
                        g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                        g2.setColor(Color.WHITE);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(new Color(80, 90, 120));
                    }
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> selectPeriod(idx));
            periodBtns[i] = btn;
            periodGroup.add(btn);
            pillGroup.add(btn);
        }

        extraFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        extraFilterPanel.setOpaque(false);
        extraFilterPanel.setVisible(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(pillGroup);
        left.add(Box.createHorizontalStrut(10));
        left.add(extraFilterPanel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(buildNavyButton("  Xuất báo cáo  ", this::doExport));
        right.add(buildOutlineButton("Thống kê  →", this::goThongKe));

        bar.add(left,  BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void selectPeriod(int idx) {
        periodBtns[idx].setSelected(true);
        extraFilterPanel.removeAll();
        extraFilterPanel.setVisible(false);

        int thisYear  = LocalDate.now().getYear();
        int thisMonth = LocalDate.now().getMonthValue();

        switch (idx) {
            case 2 -> {
                cboThang = comboOf(java.util.stream.IntStream.rangeClosed(1,12).boxed().toArray(Integer[]::new));
                cboThang.setSelectedItem(thisMonth);
                cboNam   = comboOf(yearArray());
                extraFilterPanel.add(label("Tháng:")); extraFilterPanel.add(cboThang);
                extraFilterPanel.add(label("Năm:"));   extraFilterPanel.add(cboNam);
                extraFilterPanel.setVisible(true);
            }
            case 3 -> {
                cboNamYear = comboOf(yearArray());
                extraFilterPanel.add(label("Năm:")); extraFilterPanel.add(cboNamYear);
                extraFilterPanel.setVisible(true);
            }
            case 4 -> {
                spFrom = dateSpinner(LocalDate.now().withDayOfMonth(1));
                spTo   = dateSpinner(LocalDate.now());
                extraFilterPanel.add(label("Từ:"));  extraFilterPanel.add(spFrom);
                extraFilterPanel.add(label("đến:")); extraFilterPanel.add(spTo);
                extraFilterPanel.setVisible(true);
            }
        }
        extraFilterPanel.revalidate();
        extraFilterPanel.repaint();
        loadAll(idx);
    }

    // =========================================================================
    // KPI ROW (5 cards)
    // =========================================================================
    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

        String[] labels = {"Tổng doanh thu","Vé đã bán","Vé đã hủy","Tiền mặt","Chuyển khoản"};
        String[] inits  = {"0 đ","0 vé","0 vé","0 đ","0 đ"};
        for (int i = 0; i < 5; i++) row.add(buildKpiCard(labels[i], inits[i], KPI_ACCENTS[i], i));
        return row;
    }

    private JPanel buildKpiCard(String label, String initVal, Color accent, int idx) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow nhẹ
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(2, 3, getWidth()-2, getHeight()-2, 12, 12);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-3, 12, 12);
                // Viền
                g2.setColor(BORDER_C);
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth()-3, getHeight()-4, 12, 12);
                // Thanh màu bên trái
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight()-4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 16, 10, 12));

        JLabel lbLabel = new JLabel(label.toUpperCase());
        lbLabel.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 10));
        lbLabel.setForeground(new Color(130, 135, 155));
        lbLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbVal = new JLabel(initVal);
        lbVal.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
        lbVal.setForeground(new Color(22, 28, 52));
        lbVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiValues[idx] = lbVal;

        card.add(lbLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(lbVal);
        return card;
    }

    // =========================================================================
    // MIDDLE ROW: BarChart (trái) + Donut (phải)
    // =========================================================================
    private JPanel buildMiddleRow() {
        JPanel pnl = new JPanel(new BorderLayout(12, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        barChart = new DashBarChart();
        JPanel barCard = wrapCard(barChart, "Doanh thu hàng ngày (Tiền mặt + Chuyển khoản)");
        pnl.add(barCard, BorderLayout.CENTER);

        donutChart = new DashDonutChart();
        JPanel donutCard = wrapCard(donutChart, "Phân loại vé đã bán");
        donutCard.setPreferredSize(new Dimension(270, 300));
        pnl.add(donutCard, BorderLayout.EAST);
        return pnl;
    }

    // =========================================================================
    // BOTTOM ROW: LineChart (vé bán / vé hủy)
    // =========================================================================
    private JPanel buildBottomRow() {
        lineChart = new DashLineChart();
        JPanel card = wrapCard(lineChart, "Biểu đồ số lượng vé bán & vé hủy theo ngày");
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        return card;
    }

    // =========================================================================
    // LOAD DATA
    // =========================================================================
    private void loadAll(int periodIdx) {
        new Thread(() -> {
            try {
                LocalDate[] range = getRange(periodIdx);
                LocalDate from = range[0], to = range[1];

                long doanhThu  = queryDoanhThu(from, to);
                int  veBan     = queryVeTheoTrangThai(from, to, "Đã thanh toán");
                int  veHuy     = queryVeHuy(from, to);
                long[] pttt    = queryDoanhThuPTTT(from, to);
                long tienMat   = pttt[0], ck = pttt[1];
                int[] gheData  = queryGheTheoLoai(from, to);

                // Dữ liệu theo ngày cho chart
                List<LocalDate>  days  = getDaysInRange(from, to);
                long[] tmByDay  = queryDoanhThuByDay(from, to, false);
                long[] ckByDay  = queryDoanhThuByDay(from, to, true);
                int[]  banByDay = queryVeByDay(from, to, false);
                int[]  huyByDay = queryVeByDay(from, to, true);

                SwingUtilities.invokeLater(() -> {
                    kDoanhThu = doanhThu;
                    kVeBan = veBan; kVeHuy = veHuy;
                    kTienMat = tienMat; kCK = ck;

                    kpiValues[0].setText(formatMoney(doanhThu));
                    kpiValues[1].setText(veBan + " vé");
                    kpiValues[2].setText(veHuy + " vé");
                    kpiValues[3].setText(formatMoney(tienMat));
                    kpiValues[4].setText(formatMoney(ck));

                    barChart.setData(days, tmByDay, ckByDay);
                    donutChart.setData(gheData[0], gheData[1], gheData[2]);
                    lineChart.setData(days, banByDay, huyByDay);
                });
            } catch (SQLException ex) { ex.printStackTrace(); }
        }).start();
    }

    // =========================================================================
    // SQL QUERIES
    // =========================================================================
    private long queryDoanhThu(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT ISNULL(SUM(v.giaVe),0) FROM Ve v " +
                     "JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe=N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0L; }
        }
    }

    private long[] queryDoanhThuPTTT(LocalDate from, LocalDate to) throws SQLException {
        long[] res = {0L, 0L};
        String sql = "SELECT h.phuongThucThanhToan, ISNULL(SUM(v.giaVe),0) " +
                     "FROM Ve v JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe=N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY h.phuongThucThanhToan";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString(1);
                    long   v = rs.getLong(2);
                    if (isCK(p)) res[1] += v; else res[0] += v;
                }
            }
        }
        return res;
    }

    private int queryVeTheoTrangThai(LocalDate from, LocalDate to, String tt) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe=? AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tt);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private int queryVeHuy(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe IN (N'Đã hủy','DA_HUY') " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private int[] queryGheTheoLoai(LocalDate from, LocalDate to) throws SQLException {
        int[] res = {0,0,0};
        String sql = "SELECT g.loaiGhe, COUNT(*) FROM Ve v " +
                     "JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "JOIN Ghe g ON v.maGhe=g.maGhe " +
                     "WHERE v.trangThaiVe=N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY g.loaiGhe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String l = rs.getString(1); int c = rs.getInt(2);
                    if (l.equalsIgnoreCase("Ghế cứng"))    res[0] = c;
                    else if (l.equalsIgnoreCase("Giường nằm")) res[1] = c;
                    else if (l.equalsIgnoreCase("Ghế mềm"))   res[2] = c;
                }
            }
        }
        return res;
    }

    /** Doanh thu theo từng ngày trong khoảng. isCk=false→tiền mặt, true→CK */
    private long[] queryDoanhThuByDay(LocalDate from, LocalDate to, boolean isCk) throws SQLException {
        List<LocalDate> days = getDaysInRange(from, to);
        long[] res = new long[days.size()];
        if (days.isEmpty()) return res;

        String ptttFilter = isCk
            ? "AND (h.phuongThucThanhToan LIKE '%CHUYEN_KHOAN%' OR h.phuongThucThanhToan LIKE '%vietqr%')"
            : "AND (h.phuongThucThanhToan NOT LIKE '%CHUYEN_KHOAN%' AND h.phuongThucThanhToan NOT LIKE '%vietqr%')";

        String sql = "SELECT CAST(h.ngayLapHD AS DATE) AS ngay, ISNULL(SUM(v.giaVe),0) " +
                     "FROM Ve v JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe=N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     ptttFilter +
                     " GROUP BY CAST(h.ngayLapHD AS DATE)";

        Map<LocalDate, Long> map = new HashMap<>();
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getDate(1).toLocalDate(), rs.getLong(2));
            }
        }
        for (int i = 0; i < days.size(); i++) res[i] = map.getOrDefault(days.get(i), 0L);
        return res;
    }

    /** Số vé theo từng ngày. isCk false→bán, true→hủy */
    private int[] queryVeByDay(LocalDate from, LocalDate to, boolean huy) throws SQLException {
        List<LocalDate> days = getDaysInRange(from, to);
        int[] res = new int[days.size()];
        if (days.isEmpty()) return res;

        String tt = huy ? "IN (N'Đã hủy','DA_HUY')" : "= N'Đã thanh toán'";
        String sql = "SELECT CAST(h.ngayLapHD AS DATE) AS ngay, COUNT(*) " +
                     "FROM Ve v JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe " + tt +
                     " AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY CAST(h.ngayLapHD AS DATE)";

        Map<LocalDate, Integer> map = new HashMap<>();
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getDate(1).toLocalDate(), rs.getInt(2));
            }
        }
        for (int i = 0; i < days.size(); i++) res[i] = map.getOrDefault(days.get(i), 0);
        return res;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private LocalDate[] getRange(int idx) {
        LocalDate now = LocalDate.now();
        return switch (idx) {
            case 0 -> new LocalDate[]{now, now};
            case 1 -> new LocalDate[]{now.minusDays(now.getDayOfWeek().getValue()-1), now};
            case 2 -> {
                int m = cboThang!=null ? (Integer)cboThang.getSelectedItem() : now.getMonthValue();
                int y = cboNam!=null   ? (Integer)cboNam.getSelectedItem()   : now.getYear();
                LocalDate f = LocalDate.of(y, m, 1);
                yield new LocalDate[]{f, f.withDayOfMonth(f.lengthOfMonth())};
            }
            case 3 -> {
                int y = cboNamYear!=null ? (Integer)cboNamYear.getSelectedItem() : now.getYear();
                yield new LocalDate[]{LocalDate.of(y,1,1), LocalDate.of(y,12,31)};
            }
            case 4 -> {
                LocalDate f = spFrom!=null ? ((java.util.Date)spFrom.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate() : now.withDayOfMonth(1);
                LocalDate t = spTo!=null   ? ((java.util.Date)spTo.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate() : now;
                yield new LocalDate[]{f, t};
            }
            default -> new LocalDate[]{now, now};
        };
    }

    private static List<LocalDate> getDaysInRange(LocalDate from, LocalDate to) {
        List<LocalDate> list = new ArrayList<>();
        LocalDate cur = from;
        while (!cur.isAfter(to)) { list.add(cur); cur = cur.plusDays(1); }
        return list;
    }

    private void doExport() {
        int[] d = donutChart.getData();
        BaoCaoPDF.export("Quản lý", kDoanhThu, kDoanhThu, kVeBan, kVeHuy, d[1], d[2], d[0]);
    }

    private void goThongKe() {
        if (appFrame != null) appFrame.showCard("thong-ke");
    }

    private JPanel wrapCard(Component inner, String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(12, 14, 12, 14)));
        JLabel lbT = new JLabel(title);
        lbT.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbT.setForeground(new Color(50, 58, 90));
        card.add(lbT,   BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JButton buildNavyButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? NAVY.darker() : NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(145, 34));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JButton buildOutlineButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? NAVY : getModel().isRollover() ? new Color(240,244,252) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(NAVY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(getModel().isPressed() ? Color.WHITE : NAVY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 34));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(70, 75, 100));
        return l;
    }

    @SuppressWarnings("unchecked")
    private <T> JComboBox<T> comboOf(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(90, 30));
        return cb;
    }

    private JSpinner dateSpinner(LocalDate init) {
        java.util.Date d = java.util.Date.from(
            init.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        JSpinner sp = new JSpinner(new SpinnerDateModel(d, null, null, java.util.Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        sp.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        sp.setPreferredSize(new Dimension(110, 30));
        return sp;
    }

    private Integer[] yearArray() {
        int y = LocalDate.now().getYear();
        return new Integer[]{y, y-1, y-2, y-3, y-4, y-5};
    }

    private static String formatMoney(long v) {
        return String.format("%,d đ", v).replace(",", ".");
    }

    private static boolean isCK(String s) {
        if (s == null) return false;
        String l = s.toLowerCase();
        return l.contains("chuyen_khoan") || l.contains("chuyển khoản")
            || l.contains("chuyen khoan") || l.contains("vietqr");
    }
}


// =============================================================================
//  INNER CHART CLASSES (đặt cùng file hoặc tách ra package gui)
// =============================================================================

/**
 * DashBarChart — Biểu đồ cột doanh thu theo ngày, 2 màu TM/CK xếp chồng.
 */
class DashBarChart extends JPanel {

    private static final Color TM_C = new Color(52, 152, 219);
    private static final Color CK_C = new Color(39, 174, 96);
    private static final Color GRID = new Color(240, 242, 246);
    private static final Color AXIS = new Color(180, 185, 200);

    private List<LocalDate> days   = new ArrayList<>();
    private long[]          tmData = {};
    private long[]          ckData = {};

    public DashBarChart() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(100, 240));
    }

    public void setData(List<LocalDate> d, long[] tm, long[] ck) {
        this.days   = d;
        this.tmData = tm;
        this.ckData = ck;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int padL = 62, padR = 18, padT = 18, padB = 42;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;

        if (days.isEmpty()) {
            drawEmpty(g2, w, h);
            g2.dispose();
            return;
        }

        // Tính max
        long maxVal = 1;
        for (int i = 0; i < days.size(); i++) {
            long total = (i < tmData.length ? tmData[i] : 0) + (i < ckData.length ? ckData[i] : 0);
            if (total > maxVal) maxVal = total;
        }
        // Làm tròn max lên
        maxVal = roundUp(maxVal);

        // Vẽ grid lines + labels trục Y
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = padT + chartH - (int)((double) i / gridLines * chartH);
            g2.setColor(GRID);
            g2.drawLine(padL, y, padL + chartW, y);
            g2.setColor(AXIS);
            long labelVal = maxVal * i / gridLines;
            String lbl = formatShort(labelVal);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, padL - fm.stringWidth(lbl) - 5, y + fm.getAscent()/2 - 1);
        }

        // Trục
        g2.setColor(AXIS);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(padL, padT, padL, padT + chartH);
        g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

        // Cột — gom nhóm nếu quá nhiều ngày (>35 → chỉ vẽ theo tuần)
        int n = days.size();
        boolean grouped = n > 35;
        int showN = grouped ? (n + 6) / 7 : n;
        float barW = (float) chartW / showN;
        float barGap = Math.max(1, barW * 0.18f);

        for (int i = 0; i < showN; i++) {
            long tm = 0, ck = 0;
            if (grouped) {
                // gom theo tuần
                int start = i * 7, end = Math.min(start + 7, n);
                for (int j = start; j < end; j++) {
                    if (j < tmData.length) tm += tmData[j];
                    if (j < ckData.length) ck += ckData[j];
                }
            } else {
                if (i < tmData.length) tm = tmData[i];
                if (i < ckData.length) ck = ckData[i];
            }

            long total = tm + ck;
            if (total == 0) continue;

            int x = padL + (int)(i * barW + barGap / 2);
            int bw = (int)(barW - barGap);
            if (bw < 1) bw = 1;

            // CK (dưới)
            int hCK = (int)((double) ck / maxVal * chartH);
            // TM (trên)
            int hTM = (int)((double) tm / maxVal * chartH);

            int yBottom = padT + chartH;

            // Vẽ CK
            if (hCK > 0) {
                g2.setColor(CK_C);
                g2.fillRoundRect(x, yBottom - hCK, bw, hCK, 3, 3);
            }
            // Vẽ TM trên
            if (hTM > 0) {
                g2.setColor(TM_C);
                g2.fillRoundRect(x, yBottom - hCK - hTM, bw, hTM, 3, 3);
            }
        }

        // Label trục X
        g2.setColor(new Color(130, 135, 155));
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        int labelStep = Math.max(1, showN / 10);
        for (int i = 0; i < showN; i += labelStep) {
            LocalDate d = grouped ? days.get(Math.min(i*7, n-1)) : days.get(i);
            String lbl = grouped ? ("T" + d.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR))
                                 : String.format("%02d", d.getDayOfMonth());
            int x = padL + (int)(i * barW + barW/2 - fm.stringWidth(lbl)/2);
            g2.drawString(lbl, x, padT + chartH + 14);
        }

        // Legend
        drawLegend(g2, w, h);
        g2.dispose();
    }

    private void drawLegend(Graphics2D g2, int w, int h) {
        int legendX = w - 180, legendY = 4;
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
        // TM
        g2.setColor(TM_C);
        g2.fillRoundRect(legendX, legendY + 2, 12, 12, 3, 3);
        g2.setColor(new Color(60, 65, 90));
        g2.drawString("Tiền mặt", legendX + 17, legendY + 12);
        // CK
        g2.setColor(CK_C);
        g2.fillRoundRect(legendX + 90, legendY + 2, 12, 12, 3, 3);
        g2.setColor(new Color(60, 65, 90));
        g2.drawString("Chuyển khoản", legendX + 107, legendY + 12);
    }

    private void drawEmpty(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(190, 195, 210));
        g2.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 13));
        String msg = "Chưa có dữ liệu";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg))/2, h/2);
    }

    private static long roundUp(long v) {
        if (v <= 0) return 1_000_000;
        long mag = (long) Math.pow(10, (int) Math.log10(v));
        return ((v / mag) + 1) * mag;
    }

    private static String formatShort(long v) {
        if (v >= 1_000_000_000) return String.format("%.0fT", v / 1_000_000_000.0);
        if (v >= 1_000_000)     return String.format("%.0fM", v / 1_000_000.0);
        if (v >= 1_000)         return String.format("%.0fK", v / 1_000.0);
        return String.valueOf(v);
    }
}


/**
 * DashDonutChart — Biểu đồ tròn donut phân loại ghế.
 * (Tương tự DonutChartMgr nhưng style đồng bộ với Dashboard)
 */
class DashDonutChart extends JPanel {

    private static final Color[] COLORS = {
        new Color(37,  99, 235),   // Ghế cứng — xanh đậm
        new Color(16, 185, 129),   // Giường nằm — xanh lá
        new Color(245, 158,  11),  // Ghế mềm — cam vàng
    };
    private static final String[] LABELS = {"Ghế cứng", "Giường nằm", "Ghế mềm"};
    private static final int OUTER_R = 78, INNER_R = 48;

    private int[] data = {0, 0, 0};
    private int   hoverIdx = -1;

    public DashDonutChart() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(240, 240));
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int old = hoverIdx;
                hoverIdx = detectSector(e.getPoint());
                if (old != hoverIdx) repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoverIdx = -1; repaint();
            }
        });
    }

    public void setData(int gc, int gn, int gm) {
        data[0] = gc; data[1] = gn; data[2] = gm;
        repaint();
    }

    public int[] getData() { return data; }

    private int detectSector(Point p) {
        int cx = getWidth()/2, cy = OUTER_R + 20;
        double dist = p.distance(cx, cy);
        if (dist < INNER_R || dist > OUTER_R) return -1;
        int total = data[0]+data[1]+data[2];
        if (total == 0) return -1;
        double angle = Math.toDegrees(Math.atan2(cy - p.y, p.x - cx));
        if (angle < 0) angle += 360;
        double mapped = (450 - angle) % 360;
        double cur = 0;
        for (int i = 0; i < 3; i++) {
            double arc = 360.0 * data[i] / total;
            if (mapped >= cur && mapped < cur + arc) return i;
            cur += arc;
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cx = getWidth()/2, cy = OUTER_R + 20;
        int total = data[0]+data[1]+data[2];
        int sa = 90;

        for (int i = 0; i < 3; i++) {
            int arc = (total == 0) ? 120 : (i == 2
                ? 360 - Math.round((float)360*data[0]/total) - Math.round((float)360*data[1]/total)
                : Math.round((float)360*data[i]/total));

            int offset = (hoverIdx == i) ? 5 : 0;
            double midAngle = Math.toRadians(sa + arc / 2.0);
            int ox = (int)(offset * Math.cos(midAngle + Math.PI));
            int oy = (int)(offset * Math.sin(midAngle + Math.PI));

            Color c = COLORS[i];
            if (hoverIdx != -1 && hoverIdx != i) c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 70);
            g2.setColor(c);
            g2.fillArc(cx - OUTER_R + ox, cy - OUTER_R + oy, OUTER_R*2, OUTER_R*2, sa, arc);
            sa += arc;
        }

        // Lỗ giữa
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - INNER_R, cy - INNER_R, INNER_R*2, INNER_R*2);

        // Tổng ở giữa
        g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
        g2.setColor(new Color(22, 28, 52));
        String tot = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(tot, cx - fm.stringWidth(tot)/2, cy + fm.getAscent()/2 - 2);
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(130, 135, 155));
        String lbl = "vé";
        g2.drawString(lbl, cx - g2.getFontMetrics().stringWidth(lbl)/2, cy + fm.getAscent()/2 + 13);

        // Legend
        int legendY = cy + OUTER_R + 18;
        for (int i = 0; i < 3; i++) {
            double pct = total == 0 ? 0 : data[i] * 100.0 / total;
            Color c = COLORS[i];
            if (hoverIdx != -1 && hoverIdx != i) c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 70);
            int iy = legendY + i * 30;
            g2.setColor(c);
            g2.fillRoundRect(18, iy, 13, 13, 4, 4);
            g2.setColor(new Color(40, 45, 70));
            g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            g2.drawString(LABELS[i], 38, iy + 11);
            g2.setColor(new Color(110, 115, 140));
            g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(String.format("%.0f%% (%d vé)", pct, data[i]), 38, iy + 24);
        }
        g2.dispose();
    }
}


/**
 * DashLineChart — Biểu đồ đường: vé bán (xanh) & vé hủy (đỏ) theo ngày.
 */
class DashLineChart extends JPanel {

    private static final Color SELL_C = new Color(37,  99, 235);
    private static final Color HUY_C  = new Color(231, 76,  60);
    private static final Color GRID   = new Color(240, 242, 246);
    private static final Color AXIS   = new Color(180, 185, 200);

    private List<LocalDate> days    = new ArrayList<>();
    private int[]           banData = {};
    private int[]           huyData = {};

    public DashLineChart() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(100, 200));
    }

    public void setData(List<LocalDate> d, int[] ban, int[] huy) {
        this.days    = d;
        this.banData = ban;
        this.huyData = huy;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int padL = 48, padR = 20, padT = 22, padB = 40;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;

        int n = days.size();
        if (n == 0) {
            drawEmpty(g2, w, h);
            g2.dispose();
            return;
        }

        // Max
        int maxVal = 1;
        for (int i = 0; i < n; i++) {
            if (i < banData.length && banData[i] > maxVal) maxVal = banData[i];
            if (i < huyData.length && huyData[i] > maxVal) maxVal = huyData[i];
        }
        maxVal = (int)(Math.ceil(maxVal / 5.0) * 5);
        if (maxVal == 0) maxVal = 10;

        // Grid + Y labels
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = padT + chartH - (int)((double) i / gridLines * chartH);
            g2.setColor(GRID);
            g2.drawLine(padL, y, padL + chartW, y);
            g2.setColor(AXIS);
            String lbl = String.valueOf(maxVal * i / gridLines);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, padL - fm.stringWidth(lbl) - 5, y + fm.getAscent()/2 - 1);
        }

        // Trục
        g2.setColor(AXIS);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(padL, padT, padL, padT + chartH);
        g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

        // Hàm tính tọa độ điểm
        float step = n <= 1 ? chartW : (float) chartW / (n - 1);

        // Vẽ vùng fill mờ bên dưới đường vé bán
        if (n > 1 && banData.length >= n) {
            int[] xp = new int[n + 2], yp = new int[n + 2];
            for (int i = 0; i < n; i++) {
                xp[i] = padL + (int)(i * step);
                yp[i] = padT + chartH - (int)((double) banData[i] / maxVal * chartH);
            }
            xp[n] = padL + chartW; yp[n] = padT + chartH;
            xp[n+1] = padL;        yp[n+1] = padT + chartH;
            Color fillC = new Color(SELL_C.getRed(), SELL_C.getGreen(), SELL_C.getBlue(), 30);
            g2.setColor(fillC);
            g2.fillPolygon(xp, yp, n+2);
        }

        // Vẽ đường + điểm
        drawLine(g2, n, step, padL, padT, chartH, maxVal, banData, SELL_C);
        drawLine(g2, n, step, padL, padT, chartH, maxVal, huyData, HUY_C);

        // X labels
        g2.setColor(new Color(130, 135, 155));
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        int labelStep = Math.max(1, n / 10);
        for (int i = 0; i < n; i += labelStep) {
            String lbl = String.format("%02d", days.get(i).getDayOfMonth());
            int x = padL + (int)(i * step) - fm.stringWidth(lbl)/2;
            g2.drawString(lbl, x, padT + chartH + 14);
        }

        // Legend
        drawLegend(g2, w);
        g2.dispose();
    }

    private void drawLine(Graphics2D g2, int n, float step,
                          int padL, int padT, int chartH, int maxVal,
                          int[] data, Color color) {
        if (data == null || data.length < n) return;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int[] xs = new int[n], ys = new int[n];
        for (int i = 0; i < n; i++) {
            xs[i] = padL + (int)(i * step);
            ys[i] = padT + chartH - (int)((double) data[i] / maxVal * chartH);
        }

        // Đường cong smooth (Catmull-Rom approximation với quadratic)
        Path2D path = new Path2D.Float();
        path.moveTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) {
            float ctrlX = (xs[i-1] + xs[i]) / 2.0f;
            path.curveTo(ctrlX, ys[i-1], ctrlX, ys[i], xs[i], ys[i]);
        }
        g2.draw(path);

        // Điểm tròn
        for (int i = 0; i < n; i++) {
            g2.setColor(Color.WHITE);
            g2.fillOval(xs[i]-3, ys[i]-3, 6, 6);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(xs[i]-3, ys[i]-3, 6, 6);
        }
    }

    private void drawLegend(Graphics2D g2, int w) {
        int legendX = w - 210, legendY = 4;
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(SELL_C);
        g2.drawLine(legendX, legendY+7, legendX+18, legendY+7);
        g2.fillOval(legendX+6, legendY+4, 7, 7);
        g2.setColor(new Color(50, 55, 80));
        g2.drawString("Số lượng vé bán", legendX+23, legendY+12);

        g2.setColor(HUY_C);
        g2.drawLine(legendX+110, legendY+7, legendX+128, legendY+7);
        g2.fillOval(legendX+116, legendY+4, 7, 7);
        g2.setColor(new Color(50, 55, 80));
        g2.drawString("Số vé hủy", legendX+133, legendY+12);
    }

    private void drawEmpty(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(190, 195, 210));
        g2.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 13));
        String msg = "Chưa có dữ liệu";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg))/2, h/2);
    }
}
