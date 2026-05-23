package gui;

import connect_DB.Connect_DB;
import service.AuthService;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * ThongKeManagerGUI — Thống kê doanh thu cho Quản lý.
 *
 * Bố cục:
 *  - TopBar  : bộ lọc thời gian (Hôm nay | Tuần này | Tháng | Năm | Khác)
 *              + nút "Xuất báo cáo" + nút "Dashboard →"
 *  - KPI row : 6 card (Doanh thu · Lợi nhuận · Vé bán · Vé hủy · Tiền mặt · CK)
 *  - Middle  : bảng giao dịch (trái) + biểu đồ donut ghế (phải)
 *  - Bottom  : bảng Top nhân viên
 *
 * Nút "Dashboard" gọi AppFrameManager.getInstance().showCard("dashboard").
 */
public class ThongKeManagerGUI extends JPanel {

    // ── Màu sắc ───────────────────────────────────────────────────────────────
    private static final Color BORDER_C  = new Color(210, 215, 224);
    private static final Color NAVY      = new Color(37,  69, 121);
    private static final Color LIGHT_BG  = new Color(245, 247, 251);

    private static final Color[] KPI_ACCENTS = {
        new Color(34, 120, 180),   // lợi nhuận
        new Color(34, 139,  87),   // vé bán
        new Color(210,  50,  50),  // vé hủy
        new Color(180, 120,  30),  // tiền mặt
        new Color( 30, 140, 160),  // chuyển khoản
    };

    // ── Bộ lọc ────────────────────────────────────────────────────────────────
    private static final String[] PERIOD_LABELS = {"Hôm nay","Tuần này","Tháng","Năm","Khác"};
    private JToggleButton[] periodBtns;
    private ButtonGroup     periodGroup;
    private JPanel          extraFilterPanel;  // chứa combo tháng/năm hoặc date picker
    private JComboBox<Integer> cboThang, cboNam, cboNamYear;
    private JSpinner        spFrom, spTo;      // cho "Khác"

    // ── KPI ───────────────────────────────────────────────────────────────────
    private final JLabel[] kpiValues = new JLabel[5];
    private JPanel kpiVeBan, kpiVeHuy;
    private String currentTableFilter = null;  // null / "ban" / "huy"

    // ── Bảng giao dịch ────────────────────────────────────────────────────────
    private DefaultTableModel tblModel;
    private List<Object[]>    hdBanList = new ArrayList<>();
    private List<Object[]>    hdHuyList = new ArrayList<>();

    // ── Biểu đồ donut ─────────────────────────────────────────────────────────
    private DonutChartMgr donutChart;

    // ── Bảng top NV ───────────────────────────────────────────────────────────
    private DefaultTableModel staffModel;

    // ── Giá trị KPI (lưu để xuất BC) ─────────────────────────────────────────
    private long kLoiNhuan, kTienMat, kCK;
    private int  kVeBan, kVeHuy;

    // ── Reference về AppFrameManager để gọi showCard ─────────────────────────
    private AppFrameManager appFrame;

    // =========================================================================
    public ThongKeManagerGUI() {
        this(null);
    }

    public ThongKeManagerGUI(AppFrameManager appFrame) {
        this.appFrame = appFrame;
        setBackground(LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(14, GuiTheme.PAGE_PAD_LEFT,
                                        GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        page.add(buildTopBar());
        page.add(Box.createVerticalStrut(12));
        page.add(buildKpiRow());
        page.add(Box.createVerticalStrut(12));
        page.add(buildMiddleRow());
        page.add(Box.createVerticalStrut(12));
        page.add(buildStaffCard());
        page.add(Box.createVerticalStrut(14));

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> selectPeriod(0)); // mặc định "Hôm nay"
    }

    // =========================================================================
    // TOP BAR
    // =========================================================================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // ── Trái: pill group + extra filter ──────────────────────────────────
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        // Pill group (rounded toggle buttons)
        JPanel pillGroup = new JPanel(new GridLayout(1, PERIOD_LABELS.length));
        pillGroup.setOpaque(false);
        pillGroup.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            null));
        pillGroup.setBackground(Color.WHITE);
        pillGroup.setOpaque(true);
        pillGroup.setPreferredSize(new Dimension(330, 34));

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
                        g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
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
        pillGroup.setPreferredSize(new Dimension(330, 34));

        // Extra filter (combo tháng/năm hoặc date range)
        extraFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        extraFilterPanel.setOpaque(false);
        extraFilterPanel.setVisible(false);

        left.add(pillGroup);
        left.add(Box.createHorizontalStrut(10));
        left.add(extraFilterPanel);

        // ── Phải: nút Xuất báo cáo + Dashboard ──────────────────────────────
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(buildNavyButton("  Xuất báo cáo  ", true,  this::doExport));
        right.add(buildOutlineButton("Dashboard  →",         this::goDashboard));

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
            case 2 -> { // Tháng
                cboThang = comboOf(java.util.stream.IntStream.rangeClosed(1,12).boxed().toArray(Integer[]::new));
                cboThang.setSelectedItem(thisMonth);
                cboNam   = comboOf(yearArray());
                extraFilterPanel.add(label("Tháng:")); extraFilterPanel.add(cboThang);
                extraFilterPanel.add(label("Năm:"));   extraFilterPanel.add(cboNam);
                extraFilterPanel.setVisible(true);
            }
            case 3 -> { // Năm
                cboNamYear = comboOf(yearArray());
                extraFilterPanel.add(label("Năm:")); extraFilterPanel.add(cboNamYear);
                extraFilterPanel.setVisible(true);
            }
            case 4 -> { // Khác — date spinner
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
    // KPI ROW
    // =========================================================================
    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        String[] labels = {"Tổng lợi nhuận","Vé đã bán","Vé đã hủy","Tiền mặt","Chuyển khoản"};
        String[] inits  = {"0 đ","0 vé","0 vé","0 đ","0 đ"};

        for (int i = 0; i < 5; i++) {
            JPanel card = buildKpiCard(labels[i], inits[i], KPI_ACCENTS[i], i);
            if (i == 1) kpiVeBan = card;
            if (i == 2) kpiVeHuy = card;
            row.add(card);
        }
        return row;
    }

    private JPanel buildKpiCard(String label, String initVal, Color accent, int idx) {
        boolean clickable = (idx == 1 || idx == 2);
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                boolean sel = (idx==1 && "ban".equals(currentTableFilter))
                           || (idx==2 && "huy".equals(currentTableFilter));
                g2.setColor(sel ? accent : BORDER_C);
                g2.setStroke(new BasicStroke(sel ? 1.8f : 0.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);

                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1f));
                g2.fillRect(0, 0, 5, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 16, 10, 10));
        if (clickable) card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbLabel = new JLabel(label.toUpperCase());
        lbLabel.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 10));
        lbLabel.setForeground(new Color(130, 135, 155));

        JLabel lbVal = new JLabel(initVal);
        lbVal.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
        lbVal.setForeground(new Color(28, 32, 52));
        kpiValues[idx] = lbVal;

        card.add(lbLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(lbVal);

        if (idx == 2) {
            JLabel hint = new JLabel("Nhấn để lọc");
            hint.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
            hint.setForeground(new Color(160, 165, 185));
            card.add(hint);
        }
        if (idx == 2) {
            JLabel hint = new JLabel("Nhấn để lọc");
            hint.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
            hint.setForeground(new Color(160, 165, 185));
            card.add(hint);
        }

        if (clickable) {
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (idx == 1) {
                        currentTableFilter = "ban".equals(currentTableFilter) ? null : "ban";
                        renderTable("ban".equals(currentTableFilter) ? hdBanList : hdBanList);
                    } else {
                        if ("huy".equals(currentTableFilter)) {
                            currentTableFilter = null;
                            renderTable(hdBanList);
                        } else {
                            currentTableFilter = "huy";
                            renderTable(hdHuyList);
                        }
                    }
                    if (kpiVeBan != null) kpiVeBan.repaint();
                    if (kpiVeHuy != null) kpiVeHuy.repaint();
                }
            });
        }
        return card;
    }

    // =========================================================================
    // MIDDLE ROW: bảng giao dịch + donut
    // =========================================================================
    private JPanel buildMiddleRow() {
        JPanel pnl = new JPanel(new BorderLayout(12, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        pnl.add(buildTransactionTable(), BorderLayout.CENTER);

        donutChart = new DonutChartMgr();
        JPanel donutCard = wrapCard(donutChart, "Phân loại vé theo ghế");
        donutCard.setPreferredSize(new Dimension(268, 280));
        pnl.add(donutCard, BorderLayout.EAST);
        return pnl;
    }

    private JPanel buildTransactionTable() {
        tblModel = new DefaultTableModel(
            new Object[]{"Mã HĐ","Giờ bán","Khách hàng","Hình thức TT","Số vé","Tổng tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(tblModel);
        tbl.setRowHeight(34);
        tbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setBackground(new Color(248, 249, 252));
        tbl.getTableHeader().setForeground(new Color(90, 95, 120));
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(242, 244, 248));
        tbl.setSelectionBackground(NAVY);
        tbl.setSelectionForeground(Color.WHITE);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);

        DefaultTableCellRenderer zebraR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(col == 2 ? SwingConstants.LEFT : SwingConstants.CENTER);
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 253));
                return this;
            }
        };
        for (int i = 0; i < tbl.getColumnCount(); i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(zebraR);
        ((DefaultTableCellRenderer) tbl.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.CENTER);

        // Độ rộng cột
        int[] colW = {120, 70, 150, 110, 55, 110};
        for (int i = 0; i < colW.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(colW[i]);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);

        return wrapCard(sp, "Danh sách giao dịch");
    }

    private void renderTable(List<Object[]> list) {
        tblModel.setRowCount(0);
        for (Object[] row : list) {
            String tiền = row[5] instanceof Double
                ? formatMoney(((Double) row[5]).longValue())
                : String.valueOf(row[5]);
            String pttt = normalizePTTT(row[6] != null ? row[6].toString() : "");
            tblModel.addRow(new Object[]{row[0], row[1], row[2], pttt, row[4], tiền});
        }
    }

    private static String normalizePTTT(String s) {
        if (s.equalsIgnoreCase("TIEN_MAT"))    return "Tiền mặt";
        if (s.equalsIgnoreCase("CHUYEN_KHOAN") || s.toLowerCase().contains("vietqr")) return "Chuyển khoản";
        if (s.toLowerCase().contains("hoàn tiền")) return "Hoàn tiền";
        return s;
    }

    // =========================================================================
    // BẢNG TOP NHÂN VIÊN
    // =========================================================================
    private JPanel buildStaffCard() {
        staffModel = new DefaultTableModel(
            new Object[]{"Mã NV","Họ tên","Vé đã bán","Doanh thu"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = new JTable(staffModel);
        tbl.setRowHeight(34);
        tbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setBackground(new Color(248, 249, 252));
        tbl.getTableHeader().setForeground(new Color(90, 95, 120));
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(242, 244, 248));
        tbl.setSelectionBackground(new Color(37, 69, 121, 40));
        tbl.setSelectionForeground(GuiTheme.TEXT);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(col == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                boolean isTong = row == t.getRowCount() - 1;
                if (!sel) {
                    setBackground(isTong ? new Color(235, 240, 252) : row % 2 == 0 ? Color.WHITE : new Color(250, 251, 253));
                }
                setFont(isTong
                    ? GuiTheme.font("Segoe UI", Font.BOLD, 13)
                    : GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                setForeground(isTong ? new Color(37, 69, 121) : new Color(42, 45, 66));
                return this;
            }
        };
        for (int i = 0; i < tbl.getColumnCount(); i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(r);
        ((DefaultTableCellRenderer) tbl.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.CENTER);

        tbl.getColumnModel().getColumn(0).setMaxWidth(100);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(0, 200));

        JPanel card = wrapCard(sp, "Doanh thu theo nhân viên");
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
                int soNgay = (int)(to.toEpochDay() - from.toEpochDay()) + 1;

                long loiNhuan   = queryDoanhThu(from, to);
                int  veBan      = queryVeTheoTrangThai(from, to, "Đã thanh toán");
                int  veHuy      = queryVeHuy(from, to);
                long[] pttt     = queryDoanhThuPTTT(from, to);
                long tienMat    = pttt[0], ck = pttt[1];
                int[] gheData   = queryGheTheoLoai(from, to);

                List<Object[]> banList  = queryHoaDonDetail(from, to, false);
                List<Object[]> huyList  = queryHoaDonDetail(from, to, true);
                List<Object[]> allStaff = queryAllStaff(from, to);

                SwingUtilities.invokeLater(() -> {
                    kLoiNhuan = loiNhuan;
                    kVeBan = veBan; kVeHuy = veHuy;
                    kTienMat = tienMat; kCK = ck;

                    kpiValues[0].setText(formatMoney(loiNhuan));
                    kpiValues[1].setText(veBan + " vé");
                    kpiValues[2].setText(veHuy + " vé");
                    kpiValues[3].setText(formatMoney(tienMat));
                    kpiValues[4].setText(formatMoney(ck));

                    hdBanList = banList;
                    hdHuyList = huyList;
                    currentTableFilter = null;
                    if (kpiVeBan != null) kpiVeBan.repaint();
                    if (kpiVeHuy != null) kpiVeHuy.repaint();
                    renderTable(hdBanList);

                    donutChart.setData(gheData[0], gheData[1], gheData[2]);

                    staffModel.setRowCount(0);
                    for (Object[] row : allStaff) {
                        staffModel.addRow(new Object[]{
                            row[0], row[1], row[2] + " vé",
                            formatMoney(((Number) row[3]).longValue())
                        });
                    }
                    // Dòng tổng cộng
                    staffModel.addRow(new Object[]{
                        "", "TỔNG CỘNG", veBan + " vé", formatMoney(loiNhuan)
                    });
                });
            } catch (SQLException ex) { ex.printStackTrace(); }
        }).start();
    }

    private LocalDate[] getRange(int periodIdx) {
        LocalDate now = LocalDate.now();
        return switch (periodIdx) {
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
        // [0]=tiền mặt, [1]=chuyển khoản
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
                    if (isCK(p)) res[1] += v;
                    else         res[0] += v;
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
                    String l = rs.getString(1);
                    int    c = rs.getInt(2);
                    if (l.equalsIgnoreCase("Ghế cứng"))    res[0]=c;
                    else if (l.equalsIgnoreCase("Giường nằm")) res[1]=c;
                    else if (l.equalsIgnoreCase("Ghế mềm"))   res[2]=c;
                }
            }
        }
        return res;
    }

    /**
     * Trả về danh sách hóa đơn dạng Object[]:
     * [0]=maHD [1]=gioBan [2]=tenKH [3]=loaiGhe [4]=soVe [5]=tongTien(Double) [6]=phuongThucTT
     */
    private List<Object[]> queryHoaDonDetail(LocalDate from, LocalDate to, boolean huy) throws SQLException {
        String trangThai = huy ? "IN (N'Đã hủy','DA_HUY')" : "= N'Đã thanh toán'";
        String sql = "SELECT h.maHoaDon, " +
                     "  CONVERT(varchar,h.ngayLapHD,108) AS gioBan, " +
                     "  ISNULL(kh.hoTenKH, N'Khách lẻ') AS tenKH, " +
                     "  (SELECT TOP 1 g.loaiGhe FROM Ve vv JOIN Ghe g ON vv.maGhe=g.maGhe " +
                     "   WHERE vv.maHoaDon=h.maHoaDon AND vv.trangThaiVe " + trangThai + ") AS loaiGhe, " +
                     "  (SELECT COUNT(*) FROM Ve vv WHERE vv.maHoaDon=h.maHoaDon AND vv.trangThaiVe " + trangThai + ") AS soVe, " +
                     "  ISNULL(SUM(v.giaVe),0) AS tongTien, " +
                     "  h.phuongThucThanhToan " +
                     "FROM HoaDon h " +
                     "LEFT JOIN KhachHang kh ON h.maKH=kh.maKH " +
                     "JOIN Ve v ON h.maHoaDon=v.maHoaDon AND v.trangThaiVe " + trangThai +
                     " WHERE CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY h.maHoaDon,h.ngayLapHD,kh.hoTenKH,h.phuongThucThanhToan " +
                     "ORDER BY h.ngayLapHD DESC";
        List<Object[]> list = new ArrayList<>();
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("maHoaDon"),
                        rs.getString("gioBan"),
                        rs.getString("tenKH"),
                        rs.getString("loaiGhe"),
                        rs.getInt("soVe"),
                        rs.getDouble("tongTien"),
                        rs.getString("phuongThucThanhToan")
                    });
                }
            }
        }
        return list;
    }

    private List<Object[]> queryAllStaff(LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nv.maNV, nv.hoTenNV, COUNT(v.maVe) AS soBan, " +
                     "  ISNULL(SUM(v.giaVe),0) AS doanhThu " +
                     "FROM NhanVien nv JOIN HoaDon h ON nv.maNV=h.maNV " +
                     "JOIN Ve v ON h.maHoaDon=v.maHoaDon " +
                     "WHERE v.trangThaiVe=N'Đã thanh toán' " +
                     "AND CAST(h.ngayLapHD AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY nv.maNV,nv.hoTenNV ORDER BY nv.hoTenNV";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Object[]{
                        rs.getString("maNV"), rs.getString("hoTenNV"),
                        rs.getInt("soBan"),   rs.getLong("doanhThu")
                    });
            }
        }
        return list;
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================
    private void doExport() {
        int[] d = donutChart.getData(); // [gheCung, giuong, gheMem]
        BaoCaoPDF.export("Quản lý", kLoiNhuan, kLoiNhuan, kVeBan, kVeHuy, d[1], d[2], d[0]);
    }

    private void goDashboard() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof AppFrameManager) { ((AppFrameManager) w).showCard("dashboard"); }
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private JPanel wrapCard(Component inner, String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(12, 14, 10, 14)));
        JLabel lbT = new JLabel(title);
        lbT.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbT.setForeground(new Color(60, 65, 90));
        card.add(lbT,  BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /** Nút nền navy — giống nút "Xuất báo cáo" bên ThongKeGUI */
    private JButton buildNavyButton(String text, boolean withIcon, Runnable action) {
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
        btn.setPreferredSize(new Dimension(140, 34));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    /** Nút viền navy — dùng cho "Dashboard →" */
    private JButton buildOutlineButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed() || getModel().isRollover()) {
                    g2.setColor(getModel().isPressed() ? NAVY : new Color(240, 244, 252));
                } else {
                    g2.setColor(Color.WHITE);
                }
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
        cb.setPreferredSize(new Dimension(items instanceof Integer[] && items.length==6 ? 82 : 90, 30));
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
// DONUT CHART
// =============================================================================
class DonutChartMgr extends JPanel {
    private static final Color[] COLORS = {
        new Color(88, 130, 210), new Color(60, 179, 113), new Color(255, 165, 50)
    };
    private static final String[] LABELS = {"Ghế cứng", "Giường nằm", "Ghế mềm"};
    private int[] data = {0, 0, 0};

    public DonutChartMgr() { setBackground(Color.WHITE); }

    public void setData(int gc, int gn, int gm) {
        data[0]=gc; data[1]=gn; data[2]=gm; repaint();
    }

    public int[] getData() { return data; }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W=getWidth(), H=getHeight();
        int R=Math.min(W, H-90)/2-6;
        int r=(int)(R*0.55);
        int cx=W/2, cy=R+16;
        int total=data[0]+data[1]+data[2];

        int sa=90;
        for (int i=0; i<3; i++) {
            int arc=(total==0)?120:(i==2
                ? 360-(int)Math.round(360.0*data[0]/total)-(int)Math.round(360.0*data[1]/total)
                : (int)Math.round(360.0*data[i]/total));
            g2.setColor(COLORS[i]);
            g2.fillArc(cx-R,cy-R,R*2,R*2,sa,arc);
            sa+=arc;
        }
        g2.setColor(Color.WHITE);
        g2.fillOval(cx-r,cy-r,r*2,r*2);

        // Tổng ở giữa
        g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
        g2.setColor(new Color(28,32,52));
        String tot=String.valueOf(total);
        g2.drawString(tot, cx - g2.getFontMetrics().stringWidth(tot)/2, cy+5);
        g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(130,135,155));
        g2.drawString("vé", cx-g2.getFontMetrics().stringWidth("vé")/2, cy+17);

        // Legend
        int legendY=cy+R+18, step=28;
        for (int i=0; i<3; i++) {
            double pct=(total==0)?0:(data[i]*100.0/total);
            int ly=legendY+i*step;
            g2.setColor(COLORS[i]);
            g2.fillRoundRect(16,ly,12,12,4,4);
            g2.setColor(new Color(50,55,75));
            g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            g2.drawString(LABELS[i],34,ly+11);
            g2.setColor(new Color(120,125,145));
            g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(String.format("%.1f%% (%d vé)",pct,data[i]),34,ly+23);
        }
        g2.dispose();
    }
}