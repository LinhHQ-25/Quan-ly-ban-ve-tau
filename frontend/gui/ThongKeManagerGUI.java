package gui;

import connect_DB.Connect_DB;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ThongKeManagerGUI extends JPanel {

    // ── Màu sắc ───────────────────────────────────────────────────────────────
    private static final Color BORDER_C = new Color(210, 215, 224);
    private static final Color[] CHART_COLORS = {
        new Color(71, 71, 156), new Color(34, 139, 87), new Color(210, 50, 50),
        new Color(34, 120, 180), new Color(220, 150, 30), new Color(120, 60, 180),
        new Color(30, 160, 160), new Color(200, 80, 40), new Color(80, 160, 80),
        new Color(160, 90, 120), new Color(50, 100, 200), new Color(180, 120, 40)
    };

    // ── UI Components ─────────────────────────────────────────────────────────
    private JComboBox<String>  cboLoai;
    private JComboBox<Integer> cboThang;
    private JComboBox<Integer> cboNam;

    private final JLabel[] lblKpi = new JLabel[4];
    private BarChartPanelMgr  barChart;
    private DonutChartPanelMgr donutChart;
    private DefaultTableModel  staffModel;

    public ThongKeManagerGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        page.add(Box.createVerticalStrut(14));
        page.add(buildFilterBar());
        page.add(Box.createVerticalStrut(14));
        page.add(buildKpiRow());
        page.add(Box.createVerticalStrut(14));
        page.add(buildChartsRow());
        page.add(Box.createVerticalStrut(14));
        page.add(buildStaffTable());
        page.add(Box.createVerticalStrut(14));

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::loadAll);
    }

    // ═══════════════════════════ FILTER BAR ══════════════════════════════════
    private JPanel buildFilterBar() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel lbLoai = lbl("Xem theo:", Font.BOLD, 14);

        cboLoai = new JComboBox<>(new String[]{"Tháng", "Năm"});
        styleCombo(cboLoai, 100, 34);

        Integer[] months = {1,2,3,4,5,6,7,8,9,10,11,12};
        cboThang = new JComboBox<>(months);
        cboThang.setSelectedItem(LocalDate.now().getMonthValue());
        styleCombo(cboThang, 72, 34);

        int thisYear = LocalDate.now().getYear();
        Integer[] years = new Integer[6];
        for (int i = 0; i < 6; i++) years[i] = thisYear - i;
        cboNam = new JComboBox<>(years);
        styleCombo(cboNam, 88, 34);

        JButton btnLoc = navyBtn("Xem báo cáo");
        btnLoc.addActionListener(e -> loadAll());

        cboLoai.addActionListener(e -> {
            cboThang.setVisible("Tháng".equals(cboLoai.getSelectedItem()));
            pnl.revalidate(); pnl.repaint();
        });

        pnl.add(lbLoai); pnl.add(cboLoai);
        pnl.add(cboThang);
        pnl.add(lbl("Năm:", Font.BOLD, 14)); pnl.add(cboNam);
        pnl.add(btnLoc);
        return pnl;
    }

    // ═══════════════════════════ KPI CARDS ═══════════════════════════════════
    private JPanel buildKpiRow() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

        String[] titles = {"TỔNG DOANH THU", "TỔNG VÉ ĐÃ BÁN", "TỔNG VÉ ĐÃ HỦY", "DOANH THU TB/NGÀY"};
        String[] inits  = {"0 đ", "0 vé", "0 vé", "0 đ"};
        Color[]  acc    = {new Color(71,71,156), new Color(34,139,87), new Color(210,50,50), new Color(34,120,180)};

        for (int i = 0; i < 4; i++) {
            pnl.add(buildKpiCard(titles[i], inits[i], acc[i], i));
            if (i < 3) pnl.add(Box.createHorizontalStrut(14));
        }
        pnl.add(Box.createHorizontalGlue());
        return pnl;
    }

    private JPanel buildKpiCard(String title, String value, Color accent, int idx) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_C);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(accent);
                g2.fillRect(0, 0, 5, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 20, 12, 20));
        card.setPreferredSize(new Dimension(230, 80));
        card.setMaximumSize(new Dimension(230, 80));

        JLabel lbT = lbl(title, Font.BOLD, 11);
        lbT.setForeground(new Color(130, 135, 155));

        JLabel lbV = lbl(value, Font.BOLD, 22);
        lbV.setForeground(new Color(28, 32, 52));

        lblKpi[idx] = lbV;
        card.add(lbT);
        card.add(Box.createVerticalStrut(4));
        card.add(lbV);
        return card;
    }

    // ═══════════════════════════ CHARTS ROW ══════════════════════════════════
    private JPanel buildChartsRow() {
        JPanel pnl = new JPanel(new BorderLayout(14, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        barChart   = new BarChartPanelMgr();
        donutChart = new DonutChartPanelMgr();

        pnl.add(wrapCard(barChart,   "Doanh thu theo kỳ"), BorderLayout.CENTER);
        pnl.add(wrapCard(donutChart, "Phân loại vé theo ghế"), BorderLayout.EAST);
        donutChart.setPreferredSize(new Dimension(260, 270));
        return pnl;
    }

    private JPanel wrapCard(JPanel inner, String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(14, 16, 10, 16)
        ));
        JLabel lbT = lbl(title, Font.BOLD, 14);
        lbT.setForeground(new Color(60, 65, 90));
        lbT.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(lbT,  BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════ STAFF TABLE ═════════════════════════════════
    private JPanel buildStaffTable() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        JLabel lbT = lbl("TOP NHÂN VIÊN THEO DOANH THU", Font.BOLD, 13);
        lbT.setForeground(new Color(60, 65, 90));

        staffModel = new DefaultTableModel(
            new Object[]{"#", "Mã NV", "Họ tên", "Vé đã bán", "Doanh thu"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(staffModel);
        tbl.setRowHeight(34);
        tbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setBackground(new Color(245, 247, 252));
        tbl.getTableHeader().setForeground(new Color(80, 85, 110));
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(235, 237, 243));
        tbl.setSelectionBackground(new Color(71, 71, 156, 30));
        tbl.setSelectionForeground(GuiTheme.TEXT);

        // Renderer zebra + căn giữa + màu top 3
        DefaultTableCellRenderer render = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(col == 2 ? SwingConstants.LEFT : SwingConstants.CENTER);
                if (!sel) {
                    if (row == 0) c.setBackground(new Color(255, 248, 220));
                    else if (row == 1) c.setBackground(new Color(245, 245, 245));
                    else if (row == 2) c.setBackground(new Color(255, 245, 235));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 253));
                }
                return c;
            }
        };
        for (int i = 0; i < tbl.getColumnCount(); i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(render);
        tbl.getColumnModel().getColumn(0).setMaxWidth(40);
        tbl.getColumnModel().getColumn(1).setMaxWidth(90);
        ((DefaultTableCellRenderer) tbl.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);

        card.add(lbT, BorderLayout.NORTH);
        card.add(sp,  BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════ LOAD DATA ═══════════════════════════════════
    private void loadAll() {
        boolean isThang = "Tháng".equals(cboLoai.getSelectedItem());
        int thang = (Integer) cboThang.getSelectedItem();
        int nam   = (Integer) cboNam.getSelectedItem();

        new Thread(() -> {
            try {
                // Xác định khoảng ngày
                LocalDate from, to;
                int soNgay;
                if (isThang) {
                    from   = LocalDate.of(nam, thang, 1);
                    to     = from.withDayOfMonth(from.lengthOfMonth());
                    soNgay = from.lengthOfMonth();
                } else {
                    from   = LocalDate.of(nam, 1, 1);
                    to     = LocalDate.of(nam, 12, 31);
                    soNgay = 365;
                }

                // ── 1. KPI ──────────────────────────────────────────────────
                long   doanhThu = queryDoanhThu(from, to);
                int    veBan    = queryVeTheoTrangThai(from, to, "Đã thanh toán");
                int    veHuy    = queryVeHuy(from, to);
                long   tbNgay   = soNgay > 0 ? doanhThu / soNgay : 0;

                // ── 2. Bar chart data ────────────────────────────────────────
                List<String> barLabels = new ArrayList<>();
                List<Long>   barVals   = new ArrayList<>();
                if (isThang) {
                    // Doanh thu từng ngày trong tháng
                    for (int d = 1; d <= from.lengthOfMonth(); d++) {
                        LocalDate day = LocalDate.of(nam, thang, d);
                        barLabels.add(String.valueOf(d));
                        barVals.add(queryDoanhThu(day, day));
                    }
                } else {
                    // Doanh thu từng tháng trong năm
                    for (int m = 1; m <= 12; m++) {
                        LocalDate mFrom = LocalDate.of(nam, m, 1);
                        LocalDate mTo   = mFrom.withDayOfMonth(mFrom.lengthOfMonth());
                        barLabels.add("T" + m);
                        barVals.add(queryDoanhThu(mFrom, mTo));
                    }
                }

                // ── 3. Donut chart data ──────────────────────────────────────
                int[] gheData = queryGheTheoLoai(from, to);

                // ── 4. Staff table ───────────────────────────────────────────
                List<Object[]> staffData = queryTopStaff(from, to);

                // ── Update UI ────────────────────────────────────────────────
                final long dt = doanhThu, tb = tbNgay;
                final int  vb = veBan,    vh = veHuy;
                SwingUtilities.invokeLater(() -> {
                    lblKpi[0].setText(String.format("%,.0f đ", (double) dt));
                    lblKpi[1].setText(vb + " vé");
                    lblKpi[2].setText(vh + " vé");
                    lblKpi[3].setText(String.format("%,.0f đ", (double) tb));

                    barChart.setData(barLabels, barVals, isThang ? "Ngày" : "Tháng");
                    donutChart.setData(gheData[0], gheData[1], gheData[2]);

                    staffModel.setRowCount(0);
                    int rank = 1;
                    for (Object[] row : staffData) {
                        String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : String.valueOf(rank);
                        staffModel.addRow(new Object[]{
                            medal, row[0], row[1], row[2] + " vé",
                            String.format("%,.0f đ", ((Number) row[3]).doubleValue())
                        });
                        rank++;
                    }
                });
            } catch (SQLException ex) { ex.printStackTrace(); }
        }).start();
    }

    // ═══════════════════════════ SQL QUERIES ═════════════════════════════════
    private long queryDoanhThu(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT ISNULL(SUM(v.giaVe), 0) FROM Ve v " +
                     "JOIN HoaDon h ON v.maHoaDon = h.maHoaDon " +
                     "WHERE v.trangThaiVe = N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    private int queryVeTheoTrangThai(LocalDate from, LocalDate to, String tt) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v " +
                     "JOIN HoaDon h ON v.maHoaDon = h.maHoaDon " +
                     "WHERE v.trangThaiVe = ? " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tt);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int queryVeHuy(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v " +
                     "JOIN HoaDon h ON v.maHoaDon = h.maHoaDon " +
                     "WHERE v.trangThaiVe IN (N'Đã hủy', 'DA_HUY') " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int[] queryGheTheoLoai(LocalDate from, LocalDate to) throws SQLException {
        int[] res = {0, 0, 0};
        String sql = "SELECT g.loaiGhe, COUNT(*) as sl FROM Ve v " +
                     "JOIN HoaDon h ON v.maHoaDon = h.maHoaDon " +
                     "JOIN Ghe g ON v.maGhe = g.maGhe " +
                     "WHERE v.trangThaiVe = N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY g.loaiGhe";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai = rs.getString("loaiGhe");
                    if (loai.equalsIgnoreCase("Ghế cứng"))   res[0] = rs.getInt("sl");
                    else if (loai.equalsIgnoreCase("Giường nằm")) res[1] = rs.getInt("sl");
                    else if (loai.equalsIgnoreCase("Ghế mềm"))   res[2] = rs.getInt("sl");
                }
            }
        }
        return res;
    }

    private List<Object[]> queryTopStaff(LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nv.maNV, nv.hoTenNV, " +
                     "  COUNT(v.maVe) as soBan, " +
                     "  ISNULL(SUM(v.giaVe), 0) as doanhThu " +
                     "FROM NhanVien nv " +
                     "JOIN HoaDon h ON nv.maNV = h.maNV " +
                     "JOIN Ve v ON h.maHoaDon = v.maHoaDon " +
                     "WHERE v.trangThaiVe = N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY nv.maNV, nv.hoTenNV " +
                     "ORDER BY doanhThu DESC";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("maNV"),
                        rs.getString("hoTenNV"),
                        rs.getInt("soBan"),
                        rs.getLong("doanhThu")
                    });
                }
            }
        }
        return list;
    }

    // ═══════════════════════════ HELPERS UI ══════════════════════════════════
    private JLabel lbl(String text, int style, int size) {
        return new JLabel(text) {{
            setFont(GuiTheme.font("Segoe UI", style, size));
        }};
    }

    private void styleCombo(JComboBox<?> cb, int w, int h) {
        cb.setPreferredSize(new Dimension(w, h));
        cb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
    }

    private JButton navyBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY.darker() : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(140, 34));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BAR CHART
// ═══════════════════════════════════════════════════════════════════════════════
class BarChartPanelMgr extends JPanel {
    private List<String> labels = new ArrayList<>();
    private List<Long>   values = new ArrayList<>();
    private String xLabel = "";
    private int hoveredIdx = -1;

    public BarChartPanelMgr() {
        setBackground(Color.WHITE);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int idx = getBarIndexAt(e.getX());
                if (idx != hoveredIdx) { hoveredIdx = idx; repaint(); }
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredIdx = -1; repaint();
            }
        });
    }

    public void setData(List<String> labels, List<Long> values, String xLabel) {
        this.labels = labels;
        this.values = values;
        this.xLabel = xLabel;
        repaint();
    }

    private int getBarIndexAt(int x) {
        if (values.isEmpty()) return -1;
        int padL = 60, padR = 20, padT = 20, padB = 40;
        int w = getWidth() - padL - padR;
        int n = values.size();
        float barW = (float) w / n;
        int idx = (int) ((x - padL) / barW);
        return (idx >= 0 && idx < n) ? idx : -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int padL = 60, padR = 20, padT = 20, padB = 40;
        int W = getWidth() - padL - padR;
        int H = getHeight() - padT - padB;
        int n = values.size();

        long maxVal = values.stream().mapToLong(v -> v).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        // Grid lines + Y labels
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = padT + H - (i * H / gridLines);
            long val = maxVal * i / gridLines;
            g2.setColor(new Color(235, 237, 243));
            g2.drawLine(padL, y, padL + W, y);
            g2.setColor(new Color(150, 155, 170));
            String s = val >= 1_000_000 ? String.format("%,.0fM", val / 1_000_000.0)
                     : val >= 1_000     ? String.format("%,.0fK", val / 1_000.0)
                     : String.valueOf(val);
            g2.drawString(s, padL - g2.getFontMetrics().stringWidth(s) - 4, y + 4);
        }

        // Bars
        float barW = (float) W / n;
        float gap  = Math.max(2, barW * 0.15f);
        float bw   = barW - gap * 2;

        for (int i = 0; i < n; i++) {
            long v   = values.get(i);
            int bh   = (int) ((double) v / maxVal * H);
            int bx   = (int) (padL + i * barW + gap);
            int by   = padT + H - bh;

            Color base = new Color(71, 71, 156);
            Color bar  = (i == hoveredIdx)
                    ? base.brighter()
                    : new Color(base.getRed(), base.getGreen(), base.getBlue(), 210);
            g2.setColor(bar);
            g2.fillRoundRect(bx, by, (int) bw, bh, 4, 4);

            // Hiện giá trị khi hover
            if (i == hoveredIdx && v > 0) {
                String tip = v >= 1_000_000 ? String.format("%,.1fM", v / 1_000_000.0)
                           : v >= 1_000     ? String.format("%,.0fK", v / 1_000.0)
                           : String.valueOf(v);
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 10));
                int tw = g2.getFontMetrics().stringWidth(tip);
                g2.setColor(new Color(40, 45, 70));
                g2.drawString(tip, bx + (int)(bw/2) - tw/2, by - 4);
            }

            // X label (hiện cách nhau để không chồng)
            g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(130, 135, 155));
            boolean show = (n <= 12) || (i % Math.max(1, n / 12) == 0);
            if (show) {
                String lb = labels.get(i);
                int lw = g2.getFontMetrics().stringWidth(lb);
                g2.drawString(lb, bx + (int)(bw/2) - lw/2, padT + H + 16);
            }
        }

        g2.dispose();
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// DONUT CHART
// ═══════════════════════════════════════════════════════════════════════════════
class DonutChartPanelMgr extends JPanel {
    private static final Color[] COLORS = {
        new Color(88, 130, 210), new Color(60, 179, 113), new Color(255, 165, 50)
    };
    private static final String[] LABELS = {"Ghế cứng", "Giường nằm", "Ghế mềm"};
    private int[] data = {0, 0, 0};

    public DonutChartPanelMgr() { setBackground(Color.WHITE); }

    public void setData(int gc, int gn, int gm) {
        data[0] = gc; data[1] = gn; data[2] = gm;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();
        int R = Math.min(W, H) / 2 - 20;
        int r = (int)(R * 0.55);
        int cx = W / 2, cy = R + 16;
        int total = data[0] + data[1] + data[2];

        int sa = 90;
        for (int i = 0; i < 3; i++) {
            int arc = (total == 0) ? 120 : (int) Math.round(360.0 * data[i] / total);
            if (i == 2 && total != 0)
                arc = 360 - (int)Math.round(360.0*data[0]/total) - (int)Math.round(360.0*data[1]/total);
            g2.setColor(COLORS[i]);
            g2.fillArc(cx-R, cy-R, R*2, R*2, sa, arc);
            sa += arc;
        }
        // Lỗ giữa
        g2.setColor(Color.WHITE);
        g2.fillOval(cx-r, cy-r, r*2, r*2);

        // Tổng ở giữa
        g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
        g2.setColor(new Color(40, 45, 70));
        String tot = String.valueOf(total);
        int tw = g2.getFontMetrics().stringWidth(tot);
        g2.drawString(tot, cx - tw/2, cy + 6);
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(130, 135, 155));
        String sub = "vé";
        int sw = g2.getFontMetrics().stringWidth(sub);
        g2.drawString(sub, cx - sw/2, cy + 18);

        // Legend
        int legendY = cy + R + 18;
        int legendStep = 28;
        for (int i = 0; i < 3; i++) {
            double pct = (total == 0) ? 0 : (data[i] * 100.0 / total);
            int ly = legendY + i * legendStep;
            g2.setColor(COLORS[i]);
            g2.fillRoundRect(16, ly, 12, 12, 4, 4);
            g2.setColor(new Color(50, 55, 75));
            g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            g2.drawString(LABELS[i], 34, ly + 11);
            g2.setColor(new Color(120, 125, 145));
            g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(String.format("%.1f%% (%d vé)", pct, data[i]), 34, ly + 23);
        }
        g2.dispose();
    }
}