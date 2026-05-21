package gui;

import connect_DB.Connect_DB;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.SwingConstants;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * ThongKeManagerGUI — Dashboard thống kê dành cho Quản lý
 * Modern analytics: KPI cards, bar chart, donut chart, top staff table
 */
public class ThongKeManagerGUI extends JPanel {

    // ── Màu ──────────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(0xF5F7FB);
    private static final Color CARD      = Color.WHITE;
    private static final Color PRIMARY   = new Color(0x4F46E5);
    private static final Color SUCCESS   = new Color(0x22C55E);
    private static final Color WARNING   = new Color(0xF59E0B);
    private static final Color DANGER    = new Color(0xEF4444);
    private static final Color INFO      = new Color(0x06B6D4);
    private static final Color PURPLE    = new Color(0x8B5CF6);
    private static final Color TXT       = new Color(0x111827);
    private static final Color TXT_SUB   = new Color(0x6B7280);
    private static final Color TXT_MUT   = new Color(0x9CA3AF);
    private static final Color BORDER_C  = new Color(0xE5E7EB);
    private static final Color ROW_ODD   = new Color(0xFAFAFB);
    private static final Color ROW_HOV   = new Color(0xF5F3FF);

    private static final DecimalFormat FMT = new DecimalFormat("#,### đ");

    // ── UI Components ─────────────────────────────────────────────────────────
    private JComboBox<String>  cboLoai;
    private JComboBox<Integer> cboThang, cboNam;
    private final JLabel[]     lblKpi    = new JLabel[6];
    private final JLabel[]     lblKpiSub = new JLabel[6];
    private MgrBarChart        barChart;
    private MgrDonutChart      donutChart;
    private DefaultTableModel  staffModel;
    private JLabel             lblPeriod;

    // KPI config
    private static final String[] KPI_TITLES = {
        "TỔNG DOANH THU", "DOANH THU TB/NGÀY", "TỔNG VÉ ĐÃ BÁN",
        "TỔNG VÉ HỦY",    "TỈ LỆ HỦY",         "TỔNG HÓA ĐƠN"
    };
    private static final String[] KPI_ICONS = { "💰","📊","🎫","🚫","📉","🧾" };
    private static final Color[]  KPI_ACC   = { PRIMARY, INFO, SUCCESS, DANGER, WARNING, PURPLE };
    private static final Color[]  KPI_LIGHT = {
        new Color(0xEEF2FF), new Color(0xCFFAFE), new Color(0xDCFCE7),
        new Color(0xFEE2E2), new Color(0xFEF3C7), new Color(0xEDE9FE)
    };

    public ThongKeManagerGUI() {
        setBackground(BG);
        setLayout(new BorderLayout());

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(8, GuiTheme.PAGE_PAD_LEFT, 28, GuiTheme.PAGE_PAD_LEFT));

        page.add(Box.createVerticalStrut(4));
        page.add(buildFilterBar());
        page.add(Box.createVerticalStrut(14));
        page.add(buildKpiRow());
        page.add(Box.createVerticalStrut(14));
        page.add(buildChartsRow());
        page.add(Box.createVerticalStrut(14));
        page.add(buildStaffCard());
        page.add(Box.createVerticalStrut(14));

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new ThongKeGUI.ModernScrollUI());
        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::loadAll);
    }

    // ═════════════════════ FILTER BAR ══════════════════════════════════════
    private JPanel buildFilterBar() {
        JPanel card = mkCard(52);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 9));

        cboLoai = mkCombo(new String[]{"Tháng", "Năm"}, 100);
        Integer[] months = {1,2,3,4,5,6,7,8,9,10,11,12};
        cboThang = mkCombo(months, 80);
        cboThang.setSelectedItem(LocalDate.now().getMonthValue());
        int y = LocalDate.now().getYear();
        Integer[] years = {y, y-1, y-2, y-3, y-4, y-5};
        cboNam = mkCombo(years, 90);

        lblPeriod = new JLabel();
        lblPeriod.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPeriod.setForeground(TXT_SUB);

        JButton btnView = mkPrimBtn("📊  Xem báo cáo", 160, 34);
        btnView.addActionListener(e -> loadAll());

        cboLoai.addActionListener(e -> {
            cboThang.setVisible("Tháng".equals(cboLoai.getSelectedItem()));
        });

        card.add(boldLbl("Xem theo:"));
        card.add(cboLoai);
        card.add(cboThang);
        card.add(boldLbl("Năm:"));
        card.add(cboNam);
        card.add(btnView);
        card.add(Box.createHorizontalStrut(6));
        card.add(lblPeriod);
        return card;
    }

    // ═════════════════════ KPI ROW (2 hàng × 3 card) ═══════════════════════
    private JPanel buildKpiRow() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        JPanel row1 = buildKpiRowLine(0, 3);
        JPanel row2 = buildKpiRowLine(3, 6);
        col.add(row1);
        col.add(Box.createVerticalStrut(12));
        col.add(row2);
        return col;
    }

    private JPanel buildKpiRowLine(int from, int to) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        for (int i = from; i < to; i++) {
            row.add(buildKpiCard(i));
            if (i < to - 1) row.add(Box.createHorizontalStrut(13));
        }
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private JPanel buildKpiCard(int idx) {
        Color accent = KPI_ACC[idx];
        Color light  = KPI_LIGHT[idx];

        JPanel card = new JPanel() {
            private float hov = 0f;
            private Timer ht;
            {
                setOpaque(false);
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { anim(true); }
                    public void mouseExited(MouseEvent e)  { anim(false); }
                });
            }
            void anim(boolean in) {
                if (ht != null) ht.stop();
                ht = new Timer(10, ev -> {
                    hov = in ? Math.min(1f, hov + 0.12f) : Math.max(0f, hov - 0.12f);
                    repaint();
                    if ((in && hov >= 1f) || (!in && hov <= 0f)) ht.stop();
                });
                ht.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int pad = 5;
                float lift = hov * 2.5f;
                int sh = (int)(3 + hov * 5);
                for (int s = sh; s > 0; s--) {
                    float a = 0.035f * s;
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(a * 255)));
                    g2.fill(new RoundRectangle2D.Float(pad + s * 0.4f, pad + s * 0.5f - lift,
                        getWidth() - pad * 2 - s, getHeight() - pad * 2 - s, 14, 14));
                }
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Float(pad, pad - lift, getWidth() - pad * 2, getHeight() - pad * 2, 14, 14));
                // Left accent bar
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(pad, pad - lift, 4, getHeight() - pad * 2, 3, 3));
                // Icon bg
                int isz = 38, ix = getWidth() - pad - isz - 14, iy = (int)(pad - lift + (getHeight() - pad * 2 - isz) / 2);
                g2.setColor(light);
                g2.fill(new Ellipse2D.Float(ix, iy, isz, isz));
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2.setColor(accent);
                FontMetrics fm = g2.getFontMetrics();
                String ico = KPI_ICONS[idx];
                g2.drawString(ico, ix + (isz - fm.stringWidth(ico)) / 2, iy + (isz + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 12, 65));
        card.setPreferredSize(new Dimension(230, 98));
        card.setMaximumSize(new Dimension(300, 98));

        JLabel lbT = new JLabel(KPI_TITLES[idx]);
        lbT.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbT.setForeground(TXT_SUB);
        lbT.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbV = new JLabel("---");
        lbV.setFont(new Font("Segoe UI", Font.BOLD, idx < 2 ? 20 : 22));
        lbV.setForeground(TXT);
        lbV.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblKpi[idx] = lbV;

        JLabel lbS = new JLabel(" ");
        lbS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbS.setForeground(TXT_MUT);
        lbS.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblKpiSub[idx] = lbS;

        card.add(lbT);
        card.add(Box.createVerticalStrut(4));
        card.add(lbV);
        card.add(Box.createVerticalStrut(1));
        card.add(lbS);
        return card;
    }

    // ═════════════════════ CHARTS ROW ══════════════════════════════════════
    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));

        barChart   = new MgrBarChart();
        donutChart = new MgrDonutChart();

        JPanel barCard = wrapChart(barChart, "Doanh thu theo kỳ", true);
        JPanel donCard = wrapChart(donutChart, "Phân loại vé theo ghế", false);
        donCard.setPreferredSize(new Dimension(255, 290));

        row.add(barCard,  BorderLayout.CENTER);
        row.add(donCard,  BorderLayout.EAST);
        return row;
    }

    private JPanel wrapChart(JPanel inner, String title, boolean stretch) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 4; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 3 + i));
                    g2.fill(new RoundRectangle2D.Float(i * 0.5f, i, getWidth() - i, getHeight() - i, 14, 14));
                }
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 6, getHeight() - 6, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel lbT = new JLabel(title);
        lbT.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbT.setForeground(TXT);
        lbT.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(lbT,   BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════ STAFF TABLE ═════════════════════════════════════
    private JPanel buildStaffCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 4; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 3 + i));
                    g2.fill(new RoundRectangle2D.Float(i * 0.5f, i, getWidth() - i, getHeight() - i, 14, 14));
                }
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 6, getHeight() - 6, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel lbT = new JLabel("🏆  TOP NHÂN VIÊN THEO DOANH THU");
        lbT.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbT.setForeground(TXT);

        staffModel = new DefaultTableModel(
            new Object[]{"Hạng", "Mã NV", "Họ tên", "Vé đã bán", "Doanh thu"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(staffModel) {
            int hovRow = -1;
            {
                addMouseMotionListener(new MouseMotionAdapter() {
                    public void mouseMoved(MouseEvent e) {
                        int r = rowAtPoint(e.getPoint());
                        if (r != hovRow) { hovRow = r; repaint(); }
                    }
                });
                addMouseListener(new MouseAdapter() {
                    public void mouseExited(MouseEvent e) { hovRow = -1; repaint(); }
                });
            }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    if (row == hovRow) c.setBackground(ROW_HOV);
                    else if (row == 0) c.setBackground(new Color(0xFFFBEB));
                    else if (row == 1) c.setBackground(new Color(0xF9FAFB));
                    else if (row == 2) c.setBackground(new Color(0xFFF7ED));
                    else c.setBackground(row % 2 == 0 ? CARD : ROW_ODD);
                }
                return c;
            }
        };
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setRowHeight(38);
        tbl.setShowGrid(false);
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(new Color(0xF3F4F6));
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setSelectionBackground(ROW_HOV);
        tbl.setFocusable(false);

        JTableHeader h = tbl.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setBackground(new Color(0xF9FAFB));
        h.setForeground(TXT_SUB);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_C));
        h.setPreferredSize(new Dimension(0, 38));
        h.setReorderingAllowed(false);

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, row, col);
                setHorizontalAlignment(col == 2 ? SwingConstants.LEFT : SwingConstants.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF3F4F6)),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                // Revenue bold green
                if (col == 4) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    setForeground(new Color(0x16A34A));
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    setForeground(TXT);
                }
                return c;
            }
        };
        for (int i = 0; i < tbl.getColumnCount(); i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(cr);
        tbl.getColumnModel().getColumn(0).setMaxWidth(55);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(200);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(100);
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(CARD);
        sp.getVerticalScrollBar().setUI(new ThongKeGUI.ModernScrollUI());

        card.add(lbT, BorderLayout.NORTH);
        card.add(sp,  BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════ LOAD DATA ═══════════════════════════════════════
    private void loadAll() {
        boolean isThang = "Tháng".equals(cboLoai.getSelectedItem());
        int thang = (Integer) cboThang.getSelectedItem();
        int nam   = (Integer) cboNam.getSelectedItem();

        for (JLabel l : lblKpi) l.setText("...");
        lblPeriod.setText("Đang tải...");

        new Thread(() -> {
            try {
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

                long   dt   = qDoanhThu(from, to);
                int    vb   = qVe(from, to, "Đã thanh toán");
                int    vh   = qVeHuy(from, to);
                long   tbng = soNgay > 0 ? dt / soNgay : 0;
                int    hd   = qHoaDon(from, to);
                double tiLe = (vb + vh) > 0 ? vh * 100.0 / (vb + vh) : 0;

                // Bar chart data
                List<String> barLabels = new ArrayList<>();
                List<Long>   barVals   = new ArrayList<>();
                if (isThang) {
                    for (int d = 1; d <= from.lengthOfMonth(); d++) {
                        LocalDate day = LocalDate.of(nam, thang, d);
                        barLabels.add(String.valueOf(d));
                        barVals.add(qDoanhThu(day, day));
                    }
                } else {
                    for (int m = 1; m <= 12; m++) {
                        LocalDate mf = LocalDate.of(nam, m, 1);
                        LocalDate mt = mf.withDayOfMonth(mf.lengthOfMonth());
                        barLabels.add("T" + m);
                        barVals.add(qDoanhThu(mf, mt));
                    }
                }

                int[] ghe          = qGhe(from, to);
                List<Object[]> top = qTopStaff(from, to);

                final String period = isThang
                    ? String.format("Tháng %d/%d  ·  %d ngày", thang, nam, soNgay)
                    : String.format("Năm %d  ·  12 tháng", nam);

                final long dtF = dt, tbF = tbng;
                final int  vbF = vb, vhF = vh, hdF = hd;
                final double tlF = tiLe;

                SwingUtilities.invokeLater(() -> {
                    lblKpi[0].setText(fmtShort(dtF));
                    lblKpi[1].setText(fmtShort(tbF));
                    lblKpi[2].setText(vbF + " vé");
                    lblKpi[3].setText(vhF + " vé");
                    lblKpi[4].setText(String.format("%.1f%%", tlF));
                    lblKpi[5].setText(String.valueOf(hdF));

                    lblKpiSub[0].setText(FMT.format(dtF));
                    lblKpiSub[2].setText("Từ " + hdF + " hóa đơn");
                    lblKpiSub[4].setText(tlF > 20 ? "⚠️ Cần cải thiện" : "✅ Tốt");

                    lblPeriod.setText("📅  " + period);

                    barChart.setData(barLabels, barVals);
                    donutChart.setData(ghe[0], ghe[1], ghe[2]);

                    staffModel.setRowCount(0);
                    int rank = 1;
                    for (Object[] r : top) {
                        String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank;
                        staffModel.addRow(new Object[]{
                            medal, r[0], r[1], r[2] + " vé",
                            FMT.format(((Number) r[3]).longValue())
                        });
                        rank++;
                    }
                });
            } catch (SQLException ex) { ex.printStackTrace(); }
        }).start();
    }

    // ═════════════════════ SQL ═════════════════════════════════════════════
    private long qDoanhThu(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT ISNULL(SUM(v.giaVe),0) FROM Ve v " +
                     "JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe=N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection c = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
    private int qVe(LocalDate from, LocalDate to, String tt) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe=? AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection c = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tt); ps.setDate(2, Date.valueOf(from)); ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    private int qVeHuy(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ve v JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "WHERE v.trangThaiVe IN (N'Đã hủy','DA_HUY') " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection c = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    private int qHoaDon(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE CAST(ngayLapHD AS DATE) BETWEEN ? AND ?";
        try (Connection c = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    private int[] qGhe(LocalDate from, LocalDate to) throws SQLException {
        int[] res = {0, 0, 0};
        String sql = "SELECT g.loaiGhe, COUNT(*) sl FROM Ve v " +
                     "JOIN HoaDon h ON v.maHoaDon=h.maHoaDon " +
                     "JOIN Ghe g ON v.maGhe=g.maGhe " +
                     "WHERE v.trangThaiVe=N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? GROUP BY g.loaiGhe";
        try (Connection c = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String l = rs.getString("loaiGhe");
                if ("Ghế cứng".equalsIgnoreCase(l))    res[0] = rs.getInt("sl");
                else if ("Giường nằm".equalsIgnoreCase(l)) res[1] = rs.getInt("sl");
                else if ("Ghế mềm".equalsIgnoreCase(l))   res[2] = rs.getInt("sl");
            }
        }
        return res;
    }
    private List<Object[]> qTopStaff(LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nv.maNV, nv.hoTenNV, COUNT(v.maVe) soBan, ISNULL(SUM(v.giaVe),0) dt " +
                     "FROM NhanVien nv JOIN HoaDon h ON nv.maNV=h.maNV " +
                     "JOIN Ve v ON h.maHoaDon=v.maHoaDon " +
                     "WHERE v.trangThaiVe=N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY nv.maNV, nv.hoTenNV ORDER BY dt DESC";
        try (Connection c = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{ rs.getString("maNV"), rs.getString("hoTenNV"),
                    rs.getInt("soBan"), rs.getLong("dt") });
            }
        }
        return list;
    }

    // ═════════════════════ HELPERS ═════════════════════════════════════════
    private static String fmtShort(long v) {
        if (v >= 1_000_000_000) return String.format("%.1fB đ", v / 1_000_000_000.0);
        if (v >= 1_000_000)     return String.format("%.1fM đ", v / 1_000_000.0);
        if (v >= 1_000)         return String.format("%,.0f đ", (double) v);
        return v + " đ";
    }
    private static JPanel mkCard(int h) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 4; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 3 + i));
                    g2.fill(new RoundRectangle2D.Float(i * 0.5f, i, getWidth() - i, getHeight() - i, 14, 14));
                }
                g2.setColor(CARD);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 6, getHeight() - 6, 14, 14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        if (h > 0) { p.setMaximumSize(new Dimension(Integer.MAX_VALUE, h)); p.setPreferredSize(new Dimension(300, h)); }
        return p;
    }
    private static <T> JComboBox<T> mkCombo(T[] items, int w) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(CARD);
        c.setPreferredSize(new Dimension(w, 34));
        return c;
    }
    private static JLabel boldLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TXT);
        return l;
    }
    private static JButton mkPrimBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? new Color(0x4338CA)
                    : getModel().isRollover() ? new Color(0x6366F1) : PRIMARY;
                g2.setColor(base);
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(w, h));
        return btn;
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BAR CHART — Manager version với animation + hover tooltip
// ═══════════════════════════════════════════════════════════════════════════════
class MgrBarChart extends JPanel {
    private List<String> labels = new ArrayList<>();
    private List<Long>   values = new ArrayList<>();
    private int  hovIdx = -1;
    private float animProg = 0f;
    private Timer animTimer;

    private static final Color PRIMARY = new Color(0x4F46E5);
    private static final Color HOVER_C = new Color(0x6366F1);

    public MgrBarChart() {
        setOpaque(false);
        setBackground(Color.WHITE);
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int idx = barAt(e.getX());
                if (idx != hovIdx) { hovIdx = idx; repaint(); }
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) { hovIdx = -1; repaint(); }
        });
    }

    public void setData(List<String> labels, List<Long> values) {
        this.labels = labels; this.values = values;
        animProg = 0f;
        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(14, e -> {
            animProg = Math.min(1f, animProg + 0.035f);
            repaint();
            if (animProg >= 1f) animTimer.stop();
        });
        animTimer.start();
    }

    private int barAt(int x) {
        if (values.isEmpty()) return -1;
        int padL = 54, padR = 12;
        int w = getWidth() - padL - padR;
        int n = values.size();
        float bw = (float) w / n;
        int idx = (int)((x - padL) / bw);
        return (idx >= 0 && idx < n) ? idx : -1;
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values.isEmpty()) { drawEmpty(g); return; }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int padL = 54, padR = 12, padT = 18, padB = 36;
        int W = getWidth() - padL - padR, H = getHeight() - padT - padB;
        int n = values.size();
        long maxV = values.stream().mapToLong(v -> v).max().orElse(1);
        if (maxV == 0) maxV = 1;

        float eased = ease(animProg);

        // Grid
        int gridLines = 4;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= gridLines; i++) {
            int gy = padT + H - (i * H / gridLines);
            g2.setColor(new Color(0xF3F4F6));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 3}, 0));
            g2.drawLine(padL, gy, padL + W, gy);
            g2.setStroke(new BasicStroke(1f));
            long val = maxV * i / gridLines;
            String s = fmtV(val);
            g2.setColor(new Color(0x9CA3AF));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, padL - fm.stringWidth(s) - 4, gy + 4);
        }

        float bw = (float) W / n;
        float gap = Math.max(2, bw * 0.2f);
        float bwActual = bw - gap * 2;

        for (int i = 0; i < n; i++) {
            long v  = values.get(i);
            int bh  = (int)((double) v / maxV * H * eased);
            if (bh < 0) bh = 0;
            float bx = padL + i * bw + gap;
            int   by = padT + H - bh;

            boolean hov = (i == hovIdx);
            Color barC = hov ? HOVER_C : PRIMARY;

            // Shadow
            g2.setColor(new Color(barC.getRed(), barC.getGreen(), barC.getBlue(), 25));
            g2.fill(new RoundRectangle2D.Float(bx + 2, by + 2, bwActual, bh, 6, 6));

            // Gradient bar
            if (bh > 0) {
                GradientPaint gp = new GradientPaint(bx, by,
                    new Color(barC.getRed(), barC.getGreen(), barC.getBlue(), 200),
                    bx, by + bh, barC);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(bx, by, bwActual, bh, 6, 6));
                g2.setPaint(null);
            }

            // Hover tooltip
            if (hov && v > 0 && animProg >= 0.9f) {
                String tip = fmtV(v);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(tip) + 10, th = 20;
                float tx = bx + bwActual / 2 - tw / 2f;
                float ty = by - th - 4;
                // Bubble
                g2.setColor(new Color(0x1E293B));
                g2.fill(new RoundRectangle2D.Float(tx, ty, tw, th, 6, 6));
                g2.setColor(Color.WHITE);
                g2.drawString(tip, tx + 5, ty + th - 5);
            }

            // X label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(0x6B7280));
            boolean show = (n <= 15) || (i % Math.max(1, n / 12) == 0);
            if (show && i < labels.size()) {
                String lb = labels.get(i);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lb, bx + bwActual / 2 - fm.stringWidth(lb) / 2f, padT + H + 16);
            }
        }
        g2.dispose();
    }

    private void drawEmpty(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.setColor(new Color(0xD1D5DB));
        String m = "Chưa có dữ liệu";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(m, (getWidth() - fm.stringWidth(m)) / 2, getHeight() / 2);
        g2.dispose();
    }

    private static String fmtV(long v) {
        if (v >= 1_000_000_000) return String.format("%.1fB", v / 1_000_000_000.0);
        if (v >= 1_000_000)     return String.format("%.1fM", v / 1_000_000.0);
        if (v >= 1_000)         return String.format("%.0fK", v / 1_000.0);
        return String.valueOf(v);
    }

    private float ease(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// DONUT CHART — Manager version
// ═══════════════════════════════════════════════════════════════════════════════
class MgrDonutChart extends JPanel {
    private static final Color[] COLORS = {
        new Color(0x4F46E5), new Color(0x06B6D4), new Color(0xF59E0B)
    };
    private static final String[] LABELS = { "Ghế cứng", "Giường nằm", "Ghế mềm" };
    private static final int STEP = 36;

    private int[] data = {0, 0, 0};
    private float animProg = 0f;
    private Timer animTimer;

    public MgrDonutChart() {
        setOpaque(false);
        setBackground(Color.WHITE);
    }

    public void setData(int gc, int gn, int gm) {
        data[0] = gc; data[1] = gn; data[2] = gm;
        animProg = 0f;
        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(14, e -> {
            animProg = Math.min(1f, animProg + 0.04f);
            repaint();
            if (animProg >= 1f) animTimer.stop();
        });
        animTimer.start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int W = getWidth(), H = getHeight();
        int R = Math.min(W, H) / 2 - 18;
        int r = (int)(R * 0.55f);
        int cx = W / 2, cy = R + 14;
        int total = data[0] + data[1] + data[2];

        float eased = ease(animProg);
        double sa = 90;
        for (int i = 0; i < 3; i++) {
            double arc = (total == 0) ? 120 : 360.0 * data[i] / total;
            arc *= eased;
            g2.setColor(new Color(COLORS[i].getRed(), COLORS[i].getGreen(), COLORS[i].getBlue(), 30));
            g2.fill(new Arc2D.Double(cx - R + 2, cy - R + 2, R * 2, R * 2, sa, arc, Arc2D.PIE));
            g2.setColor(COLORS[i]);
            g2.fill(new Arc2D.Double(cx - R, cy - R, R * 2, R * 2, sa, arc, Arc2D.PIE));
            sa += arc;
        }
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));

        if (animProg >= 0.9f) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.setColor(new Color(0x111827));
            FontMetrics fm = g2.getFontMetrics();
            String tot = String.valueOf(total);
            g2.drawString(tot, cx - fm.stringWidth(tot) / 2, cy + 5);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(0x6B7280));
            String sub = "vé";
            fm = g2.getFontMetrics();
            g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy + 18);
        }

        int lY = cy + R + 18;
        for (int i = 0; i < 3; i++) {
            double pct = (total == 0) ? 0 : (data[i] * 100.0 / total);
            int ly = lY + i * STEP;
            g2.setColor(COLORS[i]);
            g2.fill(new RoundRectangle2D.Float(14, ly, 11, 11, 4, 4));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(0x374151));
            g2.drawString(LABELS[i], 32, ly + 10);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(0x6B7280));
            g2.drawString(String.format("%.1f%%  (%d vé)", pct, data[i]), 32, ly + 23);
        }
        g2.dispose();
    }

    private float ease(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
}