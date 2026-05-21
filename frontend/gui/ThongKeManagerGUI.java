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
    private BarChartPanelMgr   barChart;
    private DonutChartPanelMgr donutChart;
    private DefaultTableModel  staffModel;

    public ThongKeManagerGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        page.add(Box.createVerticalStrut(16));
        page.add(buildFilterBar());
        page.add(Box.createVerticalStrut(16));
        page.add(buildKpiRow());
        page.add(Box.createVerticalStrut(16));
        page.add(buildChartsRow());
        page.add(Box.createVerticalStrut(16));
        page.add(buildStaffTable());
        page.add(Box.createVerticalStrut(16));

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
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58)); // Nới rộng thanh điều hướng

        JLabel lbLoai = lbl("Xem theo:", Font.BOLD, 14); // Giữ tối thiểu 14

        cboLoai = new JComboBox<>(new String[]{"Tháng", "Năm"});
        styleCombo(cboLoai, 110, 36);

        Integer[] months = {1,2,3,4,5,6,7,8,9,10,11,12};
        cboThang = new JComboBox<>(months);
        cboThang.setSelectedItem(LocalDate.now().getMonthValue());
        styleCombo(cboThang, 80, 36);

        int thisYear = LocalDate.now().getYear();
        Integer[] years = new Integer[6];
        for (int i = 0; i < 6; i++) years[i] = thisYear - i;
        cboNam = new JComboBox<>(years);
        styleCombo(cboNam, 95, 36);

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
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105)); // Nới chiều cao hàng lên 105 cho chữ to đứng vừa

        String[] titles = {"TỔNG DOANH THU VÉ TÀU", "TỔNG SỐ VÉ ĐÃ BÁN", "TỔNG SỐ VÉ ĐÃ HỦY", "DOANH THU TRUNG BÌNH/NGÀY"};
        String[] inits  = {"0 đ", "0 vé", "0 vé", "0 đ"};
        Color[]  acc    = {new Color(71,71,156), new Color(34,139,87), new Color(210,50,50), new Color(34,120,180)};

        for (int i = 0; i < 4; i++) {
            pnl.add(buildKpiCard(titles[i], inits[i], acc[i], i));
            if (i < 3) pnl.add(Box.createHorizontalStrut(16));
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
                g2.fillRect(0, 0, 6, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 20, 14, 20));
        card.setPreferredSize(new Dimension(270, 105)); // Nới rộng layout chứa chữ kích thước lớn
        card.setMaximumSize(new Dimension(270, 105));

        JLabel lbT = lbl(title, Font.BOLD, 14); // Đẩy tiêu đề nhỏ từ 11 lên hẳn 14
        lbT.setForeground(new Color(135, 142, 158));

        JLabel lbV = lbl(value, Font.BOLD, 24); // Đẩy số liệu từ 22 lên hẳn 24 để tạo tương phản đồ họa
        lbV.setForeground(new Color(32, 38, 58));

        lblKpi[idx] = lbV;
        card.add(lbT);
        card.add(Box.createVerticalStrut(8));
        card.add(lbV);
        return card;
    }

    // ═══════════════════════════ CHARTS ROW ══════════════════════════════════
    private JPanel buildChartsRow() {
        JPanel pnl = new JPanel(new BorderLayout(16, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380)); // Tăng từ 300 lên 380 tránh đè chữ biểu đồ tròn

        barChart   = new BarChartPanelMgr();
        donutChart = new DonutChartPanelMgr();

        pnl.add(wrapCard(barChart,   "XU HƯỚNG DOANH THU THEO KỲ PHÂN TÍCH"), BorderLayout.CENTER);
        pnl.add(wrapCard(donutChart, "CƠ CẤU PHÂN LOẠI VÉ THEO HẠNG GHẾ"), BorderLayout.EAST);
        donutChart.setPreferredSize(new Dimension(310, 340)); // Nới rộng layout biểu đồ tròn chứa chữ to
        return pnl;
    }

    private JPanel wrapCard(JPanel inner, String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 18, 12, 18)
        ));
        JLabel lbT = lbl(title, Font.BOLD, 14); // Giữ nguyên 14 cấu trúc phân cấp đồ họa
        lbT.setForeground(new Color(32, 38, 58));
        lbT.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(lbT,  BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════ STAFF TABLE ═════════════════════════════════
    private JPanel buildStaffTable() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280)); // Tăng size chứa bảng rộng rãi hơn

        JLabel lbT = lbl("BẢNG XẾP HẠNG DOANH SỐ NHÂN VIÊN XUẤT SẮC", Font.BOLD, 14); // Nâng từ 13 lên 14
        lbT.setForeground(new Color(32, 38, 58));

        staffModel = new DefaultTableModel(
            new Object[]{"Thứ hạng", "Mã Nhân Viên", "Họ Tên Nhân Viên", "Số Lượng Vé Bán", "Doanh Thu Mang Về"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(staffModel);
        tbl.setRowHeight(38); // Nâng từ 34 lên hẳn 38 giúp font 14 đứng thoải mái, không lo bị chạm viền dòng
        tbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); // Font chữ bảng: 14
        tbl.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14)); // Font tiêu đề bảng: 14
        tbl.getTableHeader().setBackground(new Color(36, 48, 76)); // Màu nền tối sang trọng đồng bộ hệ thống điều hành
        tbl.getTableHeader().setForeground(Color.WHITE);
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(230, 235, 242));
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
        
        tbl.getColumnModel().getColumn(0).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(120);
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
                    for (int d = 1; d <= from.lengthOfMonth(); d++) {
                        LocalDate day = LocalDate.of(nam, thang, d);
                        barLabels.add(String.valueOf(d));
                        barVals.add(queryDoanhThu(day, day));
                    }
                } else {
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
                final long dt = doanhThu, cb = tbNgay;
                final int  vb = veBan,    vh = veHuy;
                SwingUtilities.invokeLater(() -> {
                    lblKpi[0].setText(String.format("%,.0f đ", (double) dt));
                    lblKpi[1].setText(vb + " vé");
                    lblKpi[2].setText(vh + " vé");
                    lblKpi[3].setText(String.format("%,.0f đ", (double) cb));

                    barChart.setData(barLabels, barVals, isThang ? "Ngày" : "Tháng");
                    donutChart.setData(gheData[0], gheData[1], gheData[2]);

                    staffModel.setRowCount(0);
                    int rank = 1;
                    for (Object[] row : staffData) {
                        String medal = rank == 1 ? "🥇 Hạng 1" : rank == 2 ? "🥈 Hạng 2" : rank == 3 ? "🥉 Hạng 3" : "  " + rank;
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
        cb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); // Đồng bộ font combo lên 14
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
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14)); // Nâng font nút lên 14
        btn.setPreferredSize(new Dimension(150, 36)); // Nới rộng nút cho vừa chữ
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
        int padL = 65, padR = 20, padT = 20, padB = 40;
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

        int padL = 70, padR = 20, padT = 25, padB = 40; // Mở rộng lề trái cho nhãn tiền triệu lớn
        int W = getWidth() - padL - padR;
        int H = getHeight() - padT - padB;
        int n = values.size();

        long maxVal = values.stream().mapToLong(v -> v).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        // Grid lines + Y labels
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); // Nâng font nhãn Y lên 14
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = padT + H - (i * H / gridLines);
            long val = maxVal * i / gridLines;
            g2.setColor(new Color(235, 237, 243));
            g2.drawLine(padL, y, padL + W, y);
            g2.setColor(new Color(130, 135, 155));
            String s = val >= 1_000_000 ? String.format("%,.1fM", val / 1_000_000.0)
                     : val >= 1_000     ? String.format("%,.0fK", val / 1_000.0)
                     : String.valueOf(val);
            g2.drawString(s, padL - g2.getFontMetrics().stringWidth(s) - 6, y + 5);
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
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14)); // Nâng font tooltip lên 14
                int tw = g2.getFontMetrics().stringWidth(tip);
                g2.setColor(new Color(32, 38, 58));
                g2.drawString(tip, bx + (int)(bw/2) - tw/2, by - 6);
            }

            // X label
            g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); // Nâng nhãn trục X lên 14
            g2.setColor(new Color(110, 115, 135));
            boolean show = (n <= 12) || (i % Math.max(1, n / 10) == 0);
            if (show) {
                String lb = labels.get(i);
                int lw = g2.getFontMetrics().stringWidth(lb);
                g2.drawString(lb, bx + (int)(bw/2) - lw/2, padT + H + 22);
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
        int R = Math.min(W, H) / 2 - 45; // Co nhỏ bán kính hình tròn để dành chỗ cho font chữ chú thích lớn bên dưới
        int r = (int)(R * 0.58);
        int cx = W / 2, cy = R + 20;
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

        // Tổng số ở giữa
        g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16)); // Nâng size tâm từ 14 lên 16
        g2.setColor(new Color(32, 38, 58));
        String tot = String.valueOf(total);
        int tw = g2.getFontMetrics().stringWidth(tot);
        g2.drawString(tot, cx - tw/2, cy + 5);
        
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); // Nâng chữ "vé" lên 14
        g2.setColor(new Color(135, 142, 158));
        String sub = "vé";
        int sw = g2.getFontMetrics().stringWidth(sub);
        g2.drawString(sub, cx - sw/2, cy + 20);

        // Chú thích (Legend) phía bên dưới biểu đồ
        int legendY = cy + R + 24;
        int legendStep = 42; // Tăng khoảng cách dòng từ 28 lên 42 để chữ 14 đứng thoải mái
        for (int i = 0; i < 3; i++) {
            double pct = (total == 0) ? 0 : (data[i] * 100.0 / total);
            int ly = legendY + i * legendStep;
            
            g2.setColor(COLORS[i]);
            g2.fillRoundRect(16, ly, 14, 14, 4, 4); // Tăng size khối màu marker
            
            g2.setColor(new Color(50, 55, 75));
            g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14)); // Nâng font tên loại ghế lên 14
            g2.drawString(LABELS[i], 38, ly + 12);
            
            g2.setColor(new Color(130, 135, 155));
            g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); // Nâng font chỉ số phần trăm/vé lên 14
            g2.drawString(String.format("%.1f%% (%d vé)", pct, data[i]), 145, ly + 12);
        }
        g2.dispose();
    }
}