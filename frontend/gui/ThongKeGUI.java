package gui;

import dao.HoaDonDAO;
import dao.VeDAO;
import service.AuthService;
import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * ThongKeGUI — Thống kê ca làm việc cho nhân viên bán vé
 * Thiết kế modern enterprise: stat cards, donut chart, bảng đẹp
 */
public final class ThongKeGUI extends JPanel {

    // ── Màu sắc ──────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(0xF5F7FB);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color PRIMARY     = new Color(0x4F46E5);
    private static final Color SUCCESS     = new Color(0x22C55E);
    private static final Color WARNING     = new Color(0xF59E0B);
    private static final Color DANGER      = new Color(0xEF4444);
    private static final Color INFO        = new Color(0x06B6D4);
    private static final Color TXT_MAIN    = new Color(0x111827);
    private static final Color TXT_SUB     = new Color(0x6B7280);
    private static final Color TXT_MUTED   = new Color(0x9CA3AF);
    private static final Color BORDER_C    = new Color(0xE5E7EB);
    private static final Color ROW_ODD     = new Color(0xFAFAFB);
    private static final Color ROW_HOVER   = new Color(0xF5F3FF);

    private static final DecimalFormat FMT_MONEY = new DecimalFormat("#,### đ");
    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BOLD13  = new Font("Segoe UI", Font.BOLD, 13);

    // ── State ─────────────────────────────────────────────────────────────────
    private long   doanhThu  = 0, loiNhuan = 0, tienMoCa = 0;
    private int    veBan     = 0, veHuy    = 0;
    private List<Object[]> hdBanList  = new ArrayList<>();
    private List<Object[]> hdHuyList  = new ArrayList<>();
    private String currentFilter = null; // null / "ban" / "huy"

    // ── UI Components ─────────────────────────────────────────────────────────
    private final JLabel[] lblStatVal = new JLabel[4];
    private final JLabel[] lblStatSub = new JLabel[4];
    private TKDonutChart   donutChart;
    private DefaultTableModel tblModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTable tblData;
    private JDateChooser  dcNgay;
    private JComboBox<String> cboCa;
    private JLabel lblInsight1, lblInsight2, lblInsight3;
    private JPanel[] statCards = new JPanel[4];
    private boolean[] cardSelected = {false, false, false, false};

    // ── Config cards ──────────────────────────────────────────────────────────
    private static final String[] CARD_TITLES = {
        "TỔNG DOANH THU", "LỢI NHUẬN CA", "VÉ ĐÃ BÁN", "VÉ ĐÃ HỦY"
    };
    private static final String[] CARD_ICONS  = { "💰", "📈", "🎫", "🚫" };
    private static final Color[] CARD_ACCENT  = { PRIMARY, SUCCESS, INFO, DANGER };
    private static final Color[] CARD_LIGHT   = {
        new Color(0xEEF2FF), new Color(0xDCFCE7),
        new Color(0xCFFAFE), new Color(0xFEE2E2)
    };

    public ThongKeGUI() {
        setBackground(BG);
        setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(buildPage());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new ModernScrollUI());
        add(scroll, BorderLayout.CENTER);

        // Load dữ liệu hôm nay, ca sáng mặc định
        SwingUtilities.invokeLater(() -> {
            dcNgay.setDate(new java.util.Date());
            loadData(LocalDate.now(), "Sáng");
        });
    }

    public void setTienMoCa(long tien) {
        this.tienMoCa = tien;
    }

    // ═════════════════════════ BUILD PAGE ════════════════════════════════════
    private JPanel buildPage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(8, GuiTheme.PAGE_PAD_LEFT, 24, GuiTheme.PAGE_PAD_LEFT));

        page.add(Box.createVerticalStrut(4));
        page.add(buildFilterBar());
        page.add(Box.createVerticalStrut(16));
        page.add(buildStatCards());
        page.add(Box.createVerticalStrut(14));
        page.add(buildInsightPanel());
        page.add(Box.createVerticalStrut(14));
        page.add(buildMainContent());
        page.add(Box.createVerticalStrut(14));
        page.add(buildBottomBar());

        return page;
    }

    // ═════════════════════════ FILTER BAR ════════════════════════════════════
    private JPanel buildFilterBar() {
        JPanel card = roundCard(54);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));

        JLabel lbNgay = boldLabel("Ngày kết ca:", 13);
        dcNgay = new JDateChooser();
        dcNgay.setDateFormatString("dd/MM/yyyy");
        dcNgay.setPreferredSize(new Dimension(155, 34));
        dcNgay.setBackground(CARD_BG);
        dcNgay.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(0, 6, 0, 0)
        ));
        JTextField de = (JTextField) dcNgay.getDateEditor().getUiComponent();
        de.setFont(FONT_BODY); de.setBackground(CARD_BG);
        de.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        JLabel lbCa = boldLabel("Ca làm:", 13);
        cboCa = new JComboBox<>(new String[]{"Sáng", "Chiều"});
        cboCa.setPreferredSize(new Dimension(100, 34));
        cboCa.setFont(FONT_BODY);
        cboCa.setBackground(CARD_BG);

        JButton btnLoad = mkPrimaryBtn("🔍  Kiểm tra doanh thu", 200, 36);
        btnLoad.addActionListener(e -> {
            if (dcNgay.getDate() != null) {
                LocalDate d = dcNgay.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                loadData(d, (String) cboCa.getSelectedItem());
            }
        });

        // Separator
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(BORDER_C);

        card.add(lbNgay); card.add(dcNgay);
        card.add(lbCa);   card.add(cboCa);
        card.add(sep);
        card.add(btnLoad);
        return card;
    }

    // ═════════════════════════ STAT CARDS ════════════════════════════════════
    private JPanel buildStatCards() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JPanel card = buildOneStatCard(i);
            statCards[i] = card;
            row.add(card);
            if (i < 3) row.add(Box.createHorizontalStrut(14));
        }
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private JPanel buildOneStatCard(int idx) {
        // Clickable cho vé bán (idx=2) và vé hủy (idx=3)
        boolean clickable = (idx == 2 || idx == 3);
        Color accent = CARD_ACCENT[idx];
        Color light  = CARD_LIGHT[idx];

        JPanel card = new JPanel() {
            private float hover = 0f;
            private Timer ht;
            {
                setOpaque(false);
                if (clickable) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) { animHover(true); }
                        public void mouseExited(MouseEvent e)  { animHover(false); }
                        public void mouseClicked(MouseEvent e) {
                            handleCardClick(idx);
                        }
                    });
                }
            }
            void animHover(boolean in) {
                if (ht != null) ht.stop();
                ht = new Timer(10, ev -> {
                    hover = in ? Math.min(1f, hover + 0.12f) : Math.max(0f, hover - 0.12f);
                    repaint();
                    if ((in && hover >= 1f) || (!in && hover <= 0f)) ht.stop();
                });
                ht.start();
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int pad = 6;
                float lift = hover * 3f;
                // Shadow
                int sh = (int)(4 + hover * 6);
                for (int s = sh; s > 0; s--) {
                    float a = 0.04f * s;
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(a * 255)));
                    g2.fill(new RoundRectangle2D.Float(pad + s * 0.4f, pad + s * 0.6f - lift,
                        getWidth() - pad * 2 - s, getHeight() - pad * 2 - s, 16, 16));
                }
                // Card bg
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(pad, pad - lift, getWidth() - pad * 2, getHeight() - pad * 2, 16, 16));
                // Top accent bar
                g2.setColor(cardSelected[idx] ? accent : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
                g2.fill(new RoundRectangle2D.Float(pad, pad - lift, 4, getHeight() - pad * 2, 4, 4));
                // Selected glow border
                if (cardSelected[idx]) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(pad + 1, pad - lift + 1,
                        getWidth() - pad * 2 - 2, getHeight() - pad * 2 - 2, 15, 15));
                }
                // Icon circle
                int iconSz = 42;
                int iconX = getWidth() - pad - iconSz - 14;
                int iconY = (int)(pad - lift + (getHeight() - pad * 2 - iconSz) / 2);
                g2.setColor(light);
                g2.fill(new Ellipse2D.Float(iconX, iconY, iconSz, iconSz));
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                g2.setColor(accent);
                FontMetrics fm = g2.getFontMetrics();
                String ico = CARD_ICONS[idx];
                g2.drawString(ico, iconX + (iconSz - fm.stringWidth(ico)) / 2,
                    iconY + (iconSz + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };

        card.setPreferredSize(new Dimension(230, 115));
        card.setMaximumSize(new Dimension(260, 115));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 12, 70));

        JLabel lbTitle = new JLabel(CARD_TITLES[idx]);
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbTitle.setForeground(TXT_SUB);
        lbTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbVal = new JLabel("---");
        lbVal.setFont(new Font("Segoe UI", Font.BOLD, idx < 2 ? 20 : 26));
        lbVal.setForeground(TXT_MAIN);
        lbVal.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblStatVal[idx] = lbVal;

        JLabel lbSub = new JLabel(" ");
        lbSub.setFont(FONT_SMALL);
        lbSub.setForeground(TXT_MUTED);
        lbSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblStatSub[idx] = lbSub;

        card.add(lbTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(lbVal);
        card.add(Box.createVerticalStrut(2));
        card.add(lbSub);
        return card;
    }

    private void handleCardClick(int idx) {
        if (idx == 2) {
            // Toggle vé bán
            boolean nowSelected = !cardSelected[2];
            cardSelected[2] = nowSelected;
            cardSelected[3] = false;
            if (nowSelected) hienThiBang(hdBanList, false);
            else             hienThiBang(hdBanList, false);
        } else if (idx == 3) {
            // Toggle vé hủy
            boolean nowSelected = !cardSelected[3];
            cardSelected[3] = nowSelected;
            cardSelected[2] = false;
            if (nowSelected) hienThiBang(hdHuyList, true);
            else             hienThiBang(hdBanList, false);
        }
        for (JPanel c : statCards) c.repaint();
    }

    // ═════════════════════════ INSIGHT PANEL ═════════════════════════════════
    private JPanel buildInsightPanel() {
        JPanel card = roundCard(54);
        card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
        card.setBorder(new EmptyBorder(10, 18, 10, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JLabel ico = new JLabel("💡");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        lblInsight1 = new JLabel("Nhấn 'Kiểm tra doanh thu' để xem thống kê ca làm việc");
        lblInsight1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInsight1.setForeground(PRIMARY);

        lblInsight2 = new JLabel("");
        lblInsight2.setFont(FONT_SMALL);
        lblInsight2.setForeground(TXT_SUB);

        lblInsight3 = new JLabel("");
        lblInsight3.setFont(FONT_SMALL);
        lblInsight3.setForeground(TXT_MUTED);

        JSeparator s1 = mkVSep(), s2 = mkVSep();

        card.add(ico);
        card.add(Box.createHorizontalStrut(10));
        card.add(lblInsight1);
        card.add(Box.createHorizontalStrut(16));
        card.add(s1);
        card.add(Box.createHorizontalStrut(16));
        card.add(lblInsight2);
        card.add(Box.createHorizontalStrut(16));
        card.add(s2);
        card.add(Box.createHorizontalStrut(16));
        card.add(lblInsight3);
        card.add(Box.createHorizontalGlue());
        return card;
    }

    private JSeparator mkVSep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setMaximumSize(new Dimension(1, 28));
        s.setPreferredSize(new Dimension(1, 28));
        s.setForeground(BORDER_C);
        return s;
    }

    // ═════════════════════════ MAIN CONTENT ══════════════════════════════════
    private JPanel buildMainContent() {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        row.add(buildTableCard(), BorderLayout.CENTER);
        donutChart = new TKDonutChart();
        JPanel chartWrap = wrapInCard(donutChart, "Phân loại vé theo ghế");
        chartWrap.setPreferredSize(new Dimension(250, 0));
        row.add(chartWrap, BorderLayout.EAST);
        return row;
    }

    // ═════════════════════════ TABLE CARD ════════════════════════════════════
    private JPanel buildTableCard() {
        // Header row with title + search
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = boldLabel("Chi tiết giao dịch", 14);
        title.setForeground(TXT_MAIN);

        JTextField search = createSearchField();
        header.add(title, BorderLayout.WEST);
        header.add(search, BorderLayout.EAST);

        // Table
        tblModel = new DefaultTableModel(
            new Object[]{"Mã HĐ", "Giờ bán", "Khách hàng", "Loại ghế", "Số vé", "Tổng tiền"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblData = createStyledTable();
        sorter  = new TableRowSorter<>(tblModel);
        tblData.setRowSorter(sorter);

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(search.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(search.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(search.getText()); }
        });

        JScrollPane sp = new JScrollPane(tblData);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(CARD_BG);
        sp.getVerticalScrollBar().setUI(new ModernScrollUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollUI());

        JPanel card = roundCard(0);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(header, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JTable createStyledTable() {
        JTable t = new JTable(tblModel) {
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
                    c.setBackground(row == hovRow ? ROW_HOVER
                        : (row % 2 == 0 ? CARD_BG : ROW_ODD));
                }
                return c;
            }
        };
        t.setFont(FONT_BODY);
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xF3F4F6));
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ROW_HOVER);
        t.setSelectionForeground(TXT_MAIN);
        t.setFocusable(false);

        // Header
        JTableHeader h = t.getTableHeader();
        h.setFont(FONT_BOLD13);
        h.setBackground(new Color(0xF9FAFB));
        h.setForeground(TXT_SUB);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_C));
        h.setPreferredSize(new Dimension(0, 40));
        h.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Cell renderer
        DefaultTableCellRenderer cellR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                setFont(col == 2 ? FONT_BODY : FONT_BODY);
                setHorizontalAlignment(col == 2 ? LEFT : CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF3F4F6)),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));

                // Format tổng tiền
                if (col == 5 && v instanceof String) {
                    setForeground(new Color(0x16A34A));
                    setFont(FONT_BOLD13);
                } else {
                    setForeground(TXT_MAIN);
                    setFont(FONT_BODY);
                }
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(cellR);

        // Col widths
        t.getColumnModel().getColumn(0).setPreferredWidth(90);
        t.getColumnModel().getColumn(1).setPreferredWidth(80);
        t.getColumnModel().getColumn(2).setPreferredWidth(160);
        t.getColumnModel().getColumn(3).setPreferredWidth(110);
        t.getColumnModel().getColumn(4).setPreferredWidth(65);
        t.getColumnModel().getColumn(5).setPreferredWidth(130);

        return t;
    }

    private void applyFilter(String text) {
        if (text == null || text.isBlank()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text.trim()));
    }

    // ═════════════════════════ BOTTOM BAR ════════════════════════════════════
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JButton btnExport = mkPrimaryBtn("📄  Xuất báo cáo kết ca", 210, 36);
        btnExport.addActionListener(e -> {
            int[] d = donutChart.getData();
            BaoCaoPDF.export(
                AuthService.getCurrentHoTen() != null ? AuthService.getCurrentHoTen() : "---",
                doanhThu, loiNhuan, veBan, veHuy, d[1], d[2], d[0]
            );
        });

        bar.add(btnExport, BorderLayout.EAST);
        return bar;
    }

    // ═════════════════════════ LOAD DATA ═════════════════════════════════════
    public void loadData(LocalDate ngay, String ca) {
        // Show loading state
        for (JLabel l : lblStatVal) l.setText("...");

        new Thread(() -> {
            try {
                int    vh   = VeDAO.getSoVeHuyTheoCa(ngay, ca, getCurrentMaNV());
                int[]  ghe  = VeDAO.getSoGheTheoLoaiTheoCa(ngay, ca, getCurrentMaNV());
                List<Object[]> hdList    = HoaDonDAO.getDanhSachHoaDonTheoCa(ngay, ca, getCurrentMaNV());
                List<Object[]> hdHuyTemp = HoaDonDAO.getDanhSachHoaDonHuyTheoCa(ngay, ca, getCurrentMaNV());

                long ln = 0; int vb = 0;
                for (Object[] row : hdList) {
                    ln += ((Double) row[5]).longValue();
                    vb += (int) row[4];
                }
                final long loiNhuanFinal = ln;
                final long doanhThuFinal = ln + tienMoCa;
                final int  veBanFinal    = vb;
                final int  veHuyFinal    = vh;

                SwingUtilities.invokeLater(() -> {
                    doanhThu    = doanhThuFinal;
                    loiNhuan    = loiNhuanFinal;
                    veBan       = veBanFinal;
                    veHuy       = veHuyFinal;
                    hdBanList   = hdList;
                    hdHuyList   = hdHuyTemp;
                    cardSelected[0] = cardSelected[1] = cardSelected[2] = cardSelected[3] = false;

                    // Update stat cards
                    lblStatVal[0].setText(formatShort(doanhThuFinal));
                    lblStatVal[1].setText(formatShort(loiNhuanFinal));
                    lblStatVal[2].setText(veBanFinal + " vé");
                    lblStatVal[3].setText(veHuyFinal + " vé");

                    // Tỉ lệ hủy
                    int total = veBanFinal + veHuyFinal;
                    if (total > 0) {
                        double tiLeHuy = veHuyFinal * 100.0 / total;
                        lblStatSub[3].setText(String.format("Tỉ lệ hủy: %.1f%%", tiLeHuy));
                    }
                    lblStatSub[0].setText("Tiền mở ca: " + FMT_MONEY.format(tienMoCa));
                    lblStatSub[1].setText("Từ " + FMT_MONEY.format(loiNhuanFinal) + " giao dịch");
                    lblStatSub[2].setText(hdList.size() + " hóa đơn");

                    // Chart
                    donutChart.setData(ghe[0], ghe[1], ghe[2]);

                    // Table default: show vé bán
                    hienThiBang(hdBanList, false);

                    // Insights
                    updateInsights(veBanFinal, veHuyFinal, loiNhuanFinal, ghe);

                    // Repaint cards
                    for (JPanel c : statCards) c.repaint();
                });
            } catch (SQLException ex) { ex.printStackTrace(); }
        }).start();
    }

    private void hienThiBang(List<Object[]> list, boolean isHuy) {
        tblModel.setRowCount(0);
        for (Object[] row : list) {
            String tongTienStr = row[5] instanceof Double
                ? FMT_MONEY.format(((Double) row[5]).longValue())
                : String.valueOf(row[5]);
            tblModel.addRow(new Object[]{ row[0], row[1], row[2], row[3], row[4], tongTienStr });
        }
    }

    private void updateInsights(int vb, int vh, long ln, int[] ghe) {
        // Insight 1: tình trạng vé
        if (vb + vh == 0) {
            lblInsight1.setText("Chưa có giao dịch trong ca này");
        } else {
            lblInsight1.setText(String.format("✅  Đã bán %d vé — Doanh thu %s", vb, FMT_MONEY.format(ln)));
        }

        // Insight 2: loại ghế phổ biến
        String[] loaiGhe = {"Ghế cứng", "Giường nằm", "Ghế mềm"};
        int maxGhe = 0, maxIdx = 0;
        for (int i = 0; i < 3; i++) { if (ghe[i] > maxGhe) { maxGhe = ghe[i]; maxIdx = i; } }
        if (maxGhe > 0)
            lblInsight2.setText("🏆  Bán chạy nhất: " + loaiGhe[maxIdx] + " (" + maxGhe + " vé)");
        else
            lblInsight2.setText("");

        // Insight 3: tỉ lệ hủy
        if (vb + vh > 0) {
            double tiLeHuy = vh * 100.0 / (vb + vh);
            String color = tiLeHuy > 20 ? "⚠️" : "📊";
            lblInsight3.setText(color + "  Tỉ lệ hủy: " + String.format("%.1f%%", tiLeHuy));
        } else {
            lblInsight3.setText("");
        }
    }

    private String getCurrentMaNV() {
        String m = AuthService.getCurrentMaNV();
        return m != null ? m : "NV001";
    }

    // ═════════════════════════ HELPERS ════════════════════════════════════════
    private static String formatShort(long v) {
        if (v >= 1_000_000_000) return String.format("%.1fB đ", v / 1_000_000_000.0);
        if (v >= 1_000_000)     return String.format("%.1fM đ", v / 1_000_000.0);
        if (v >= 1_000)         return String.format("%,.0f đ", (double) v);
        return v + " đ";
    }

    private static JPanel roundCard(int minH) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                for (int i = 5; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 3 + i));
                    g2.fill(new RoundRectangle2D.Float(i * 0.5f, i, getWidth() - i, getHeight() - i, 14, 14));
                }
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 6, getHeight() - 6, 14, 14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        if (minH > 0) {
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, minH));
            p.setPreferredSize(new Dimension(300, minH));
        }
        return p;
    }

    private static JPanel wrapInCard(JPanel inner, String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 5; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 3 + i));
                    g2.fill(new RoundRectangle2D.Float(i * 0.5f, i, getWidth() - i, getHeight() - i, 14, 14));
                }
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 6, getHeight() - 6, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        JLabel lbT = new JLabel(title);
        lbT.setFont(FONT_BOLD13);
        lbT.setForeground(TXT_MAIN);
        card.add(lbT, BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private static JLabel boldLabel(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setForeground(TXT_MAIN);
        return l;
    }

    private static JButton mkPrimaryBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()
                    ? new Color(0x4338CA) : getModel().isRollover()
                    ? new Color(0x6366F1) : PRIMARY;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD13);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(w, h));
        return btn;
    }

    private static JTextField createSearchField() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF9FAFB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2.setColor(new Color(0xD1D5DB));
                    g2.drawString("🔍  Tìm kiếm...", 10, getHeight() / 2 + 4);
                }
                g2.dispose();
            }
        };
        f.setFont(FONT_BODY);
        f.setOpaque(false);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        f.setPreferredSize(new Dimension(220, 32));
        return f;
    }

    // ═════════════════════════ SCROLLBAR UI ══════════════════════════════════
    static class ModernScrollUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor  = new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 80);
            trackColor  = new Color(0xF3F4F6);
        }
        @Override protected JButton createDecreaseButton(int o) { return ghost(); }
        @Override protected JButton createIncreaseButton(int o) { return ghost(); }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8));
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        private JButton ghost() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// DONUT CHART — riêng cho ThongKeGUI
// ═══════════════════════════════════════════════════════════════════════════════
class TKDonutChart extends JPanel {

    private static final Color[] COLORS = {
        new Color(0x4F46E5), new Color(0x06B6D4), new Color(0xF59E0B)
    };
    private static final String[] LABELS = { "Ghế cứng", "Giường nằm", "Ghế mềm" };
    private static final int LEGEND_STEP = 42;

    private int[] data = {0, 0, 0};
    private int selectedIdx = -1;
    private float animProg = 0f;
    private Timer animTimer;

    // Filter callback
    public interface FilterListener { void onFilter(String type); }
    private FilterListener listener;

    public TKDonutChart() {
        setOpaque(false);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(220, 340));

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int idx = detectIndex(e.getPoint());
                selectedIdx = (idx == selectedIdx) ? -1 : idx;
                repaint();
                if (listener != null)
                    listener.onFilter(selectedIdx == -1 ? null : LABELS[selectedIdx]);
            }
        });
    }

    public void setData(int gc, int gn, int gm) {
        data[0] = gc; data[1] = gn; data[2] = gm;
        selectedIdx = -1;
        animProg = 0f;
        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(14, e -> {
            animProg = Math.min(1f, animProg + 0.04f);
            repaint();
            if (animProg >= 1f) animTimer.stop();
        });
        animTimer.start();
    }

    public int[] getData() { return data; }
    public void setOnFilterListener(FilterListener l) { this.listener = l; }

    private int detectIndex(Point p) {
        int cx = getWidth() / 2, cy = 100;
        int OUTER_R = 78, INNER_R = 48;
        double dist = p.distance(cx, cy);
        if (dist >= INNER_R && dist <= OUTER_R) {
            double angle = Math.toDegrees(Math.atan2(cy - p.y, p.x - cx));
            if (angle < 0) angle += 360;
            double mapped = (450 - angle) % 360;
            int total = data[0] + data[1] + data[2];
            if (total == 0) return -1;
            double cur = 0;
            for (int i = 0; i < 3; i++) {
                double arc = 360.0 * data[i] / total;
                if (mapped >= cur && mapped < cur + arc) return i;
                cur += arc;
            }
        }
        int legendY = cy + 80 + 24;
        for (int i = 0; i < 3; i++) {
            Rectangle r = new Rectangle(10, legendY + i * LEGEND_STEP - 4, 200, LEGEND_STEP - 2);
            if (r.contains(p)) return i;
        }
        return -1;
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int OUTER_R = 78, INNER_R = 48;
        int cx = getWidth() / 2, cy = 100;
        int total = data[0] + data[1] + data[2];

        float eased = ease(animProg);

        // Draw arcs
        double sa = 90;
        for (int i = 0; i < 3; i++) {
            double arc = (total == 0) ? 120 : 360.0 * data[i] / total;
            arc *= eased;
            Color c = COLORS[i];
            if (selectedIdx != -1 && selectedIdx != i)
                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 55);

            // Shadow
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
            g2.fill(new Arc2D.Double(cx - OUTER_R + 2, cy - OUTER_R + 2,
                OUTER_R * 2, OUTER_R * 2, sa, arc, Arc2D.PIE));
            g2.setColor(c);
            g2.fill(new Arc2D.Double(cx - OUTER_R, cy - OUTER_R,
                OUTER_R * 2, OUTER_R * 2, sa, arc, Arc2D.PIE));
            sa += arc;
        }

        // Inner hole
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(cx - INNER_R, cy - INNER_R, INNER_R * 2, INNER_R * 2));

        // Center text
        if (animProg >= 0.9f) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.setColor(new Color(0x111827));
            FontMetrics fm = g2.getFontMetrics();
            String tot = String.valueOf(total);
            g2.drawString(tot, cx - fm.stringWidth(tot) / 2, cy + 6);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(0x6B7280));
            String sub = "vé bán";
            fm = g2.getFontMetrics();
            g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy + 20);
        }

        // Legend
        int legendY = cy + OUTER_R + 24;
        for (int i = 0; i < 3; i++) {
            double pct = (total == 0) ? 0 : (data[i] * 100.0 / total);
            Color c = COLORS[i];
            if (selectedIdx != -1 && selectedIdx != i)
                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 80);

            int ly = legendY + i * LEGEND_STEP;

            // Selected highlight
            if (selectedIdx == i) {
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 15));
                g2.fill(new RoundRectangle2D.Float(8, ly - 6, getWidth() - 16, LEGEND_STEP - 4, 8, 8));
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(8, ly - 6, getWidth() - 16, LEGEND_STEP - 4, 8, 8));
            }

            // Dot
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(16, ly, 12, 12, 4, 4));

            // Label
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(0x374151));
            g2.drawString(LABELS[i], 34, ly + 11);

            // Sub
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(0x6B7280));
            g2.drawString(String.format("%.1f%%  (%d vé)", pct, data[i]), 34, ly + 25);
        }

        g2.dispose();
    }

    private float ease(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
}