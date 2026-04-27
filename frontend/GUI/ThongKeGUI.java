package GUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class ThongKeGUI extends JPanel {
    private static final Color BORDER   = new Color(210, 215, 224);
    private static final Color FIELD_BG = new Color(141, 184, 219);
    private static final Color PRIMARY  = new Color(71, 71, 156);

    // ── Dữ liệu thống kê ─────────────────────────────────────────────────────
    private String tenNhanVien = "Nhân viên";
    private long   doanhThu   = 0;
    private int    veBan      = 0;
    private int    veHuy      = 0;

    /** Reference tới biểu đồ để đọc số ghế khi xuất PDF */
    private ChartPanel chartPanel;

    ThongKeGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BoxLayout(pnlPage, BoxLayout.Y_AXIS));
        pnlPage.setBorder(new EmptyBorder(
            GuiTheme.PAGE_PAD_TOP,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(buildSummaryBar());
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(buildTableWithChart());

        add(pnlPage,          BorderLayout.NORTH);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    // ── Setter để controller truyền dữ liệu vào ──────────────────────────────
    public void setThongKe(String tenNV, long dt, int vb, int vh) {
        this.tenNhanVien = tenNV;
        this.doanhThu    = dt;
        this.veBan       = vb;
        this.veHuy       = vh;
    }

    // ── 1. SUMMARY BAR ────────────────────────────────────────────────────────
    private JPanel buildSummaryBar() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(6, 0, 6, 0));

        pnl.add(buildStatCard("Tổng doanh thu", "0 đ",  new Color(71,  71, 156)));
        pnl.add(Box.createHorizontalStrut(16));
        pnl.add(buildStatCard("Vé đã bán",      "0 vé", new Color(34, 139,  87)));
        pnl.add(Box.createHorizontalStrut(16));
        pnl.add(buildStatCard("Vé đã hủy",      "0 vé", new Color(210, 50,  50)));
        pnl.add(Box.createHorizontalGlue());

        return pnl;
    }

    private JPanel buildStatCard(String label, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(220, 224, 232));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
                g2.fillRect(0, 0, 3, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 18, 10, 22));
        card.setPreferredSize(new Dimension(210, 68));
        card.setMaximumSize(new Dimension(210, 68));

        JLabel lbLabel = new JLabel(label.toUpperCase());
        lbLabel.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
        lbLabel.setForeground(new Color(130, 135, 155));
        lbLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbValue = new JLabel(value);
        lbValue.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 21));
        lbValue.setForeground(new Color(28, 32, 52));
        lbValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lbLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(lbValue);
        return card;
    }

    // ── 2. TABLE + CHART ──────────────────────────────────────────────────────
    private JPanel buildTableWithChart() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);
        chartPanel = new ChartPanel();
        pnl.add(buildTablePanel(), BorderLayout.CENTER);
        pnl.add(chartPanel,        BorderLayout.EAST);
        return pnl;
    }

    private JPanel buildTablePanel() {
        DefaultTableModel tblModel = new DefaultTableModel(
            new Object[]{"Mã HĐ", "Ga đi", "Ga đến", "Ngày bán vé", "Số lượng HK"},
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tblData = new JTable(tblModel);
        tblData.setRowHeight(28);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.setForeground(GuiTheme.TEXT);
        tblData.setGridColor(new Color(230, 233, 238));
        tblData.setSelectionBackground(new Color(207, 209, 214));
        tblData.setSelectionForeground(GuiTheme.TEXT);
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setResizingAllowed(false);
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.getTableHeader().setBackground(Color.WHITE);
        tblData.getTableHeader().setForeground(GuiTheme.TEXT);
        tblData.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblData.getColumnModel().getColumn(0).setCellRenderer(center);
        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        spnScroll.setPreferredSize(new Dimension(10000, 300));
        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 7) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(50);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(150);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(100);
            }
        });
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);
        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    // ── 3. BOTTOM BAR – nút Xuất PDF ─────────────────────────────────────────
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(8, GuiTheme.PAGE_PAD_LEFT, 14, GuiTheme.PAGE_PAD_LEFT));
        bar.add(buildExportButton(), BorderLayout.EAST);
        return bar;
    }

    private JButton buildExportButton() {
        final Color BG_NORMAL  = new Color(30,  90, 200);
        final Color BG_HOVER   = new Color(20,  70, 170);
        final Color BG_PRESSED = new Color(14,  52, 135);

        boolean[] hovered = {false};

        JButton btn = new JButton("Tạo báo cáo") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // subtle drop shadow
                g2.setColor(new Color(0, 0, 0, 28));
                g2.fillRoundRect(2, 4, getWidth() - 2, getHeight() - 3, 10, 10);
                // body
                Color bg = getModel().isPressed() ? BG_PRESSED : hovered[0] ? BG_HOVER : BG_NORMAL;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);
                // top gloss
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, getWidth() - 2, (getHeight() - 2) / 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(155, 40));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  btn.repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered[0] = false; btn.repaint(); }
        });

        btn.addActionListener(e -> {
            int[] d = chartPanel.getData(); // [0]=gheCung, [1]=giuong, [2]=gheMem
            BaoCaoPDF.export(tenNhanVien, doanhThu, veBan, veHuy,
                d[1], d[2], d[0]);
        });

        return btn;
    }
}

// ── BIỂU ĐỒ DONUT ────────────────────────────────────────────────────────────
class ChartPanel extends JPanel {

    private static final Color C_GHE_CUNG   = new Color( 88, 130, 210);
    private static final Color C_GIUONG     = new Color( 60, 179, 113);
    private static final Color C_GHE_MEM    = new Color(255, 165,  50);
    private static final Color C_GHE_CUNG_E = new Color(210, 220, 240);
    private static final Color C_GIUONG_E   = new Color(200, 235, 215);
    private static final Color C_GHE_MEM_E  = new Color(250, 230, 200);

    private static final String[] LABELS = {"Ghế cứng", "Giường", "Ghế mềm"};

    private int[] data = {0, 0, 0};
    private int selectedSeg = -1;
    private int paintCx, paintCy;
    private static final int OUTER_R = 70, INNER_R = 44;

    public ChartPanel() {
        setPreferredSize(new Dimension(230, 275));
        setBackground(Color.WHITE);
        setBorder(new LineBorder(new Color(210, 215, 224), 1, true));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int dx = e.getX() - paintCx, dy = e.getY() - paintCy;
                if (Math.hypot(dx, dy) >= INNER_R && Math.hypot(dx, dy) <= OUTER_R + 8) {
                    double angle = Math.toDegrees(Math.atan2(-dy, dx));
                    if (angle < 0) angle += 360;
                    double rot = (angle - 90 + 360) % 360;
                    int[] arcs = computeArcs(data[0] + data[1] + data[2]);
                    double cum = 0; int hit = -1;
                    for (int i = 0; i < 3; i++) { cum += arcs[i]; if (rot < cum) { hit = i; break; } }
                    selectedSeg = (hit == selectedSeg) ? -1 : hit;
                } else { selectedSeg = -1; }
                repaint();
            }
        });
    }

    public void setData(int gheCung, int giuong, int gheMem) {
        data[0] = gheCung; data[1] = giuong; data[2] = gheMem;
        selectedSeg = -1; repaint();
    }

    public int[] getData() { return new int[]{data[0], data[1], data[2]}; }

    private int[] computeArcs(int total) {
        if (total == 0) return new int[]{120, 120, 120};
        int[] a = new int[3]; int rem = 360;
        for (int i = 0; i < 2; i++) { a[i] = (int) Math.round(360.0 * data[i] / total); rem -= a[i]; }
        a[2] = rem; return a;
    }

    private Color fade(Color c) {
        return new Color((c.getRed() + 510) / 3, (c.getGreen() + 510) / 3, (c.getBlue() + 510) / 3);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(); paintCx = w / 2; paintCy = OUTER_R + 22;

        int total = data[0] + data[1] + data[2];
        int[] arcs = computeArcs(total); boolean emp = (total == 0);
        Color[] full  = {C_GHE_CUNG,   C_GIUONG,   C_GHE_MEM  };
        Color[] empty = {C_GHE_CUNG_E, C_GIUONG_E, C_GHE_MEM_E};

        int sa = 90; int[] startAngles = new int[3];
        for (int i = 0; i < 3; i++) {
            startAngles[i] = sa;
            Color base = emp ? empty[i] : full[i];
            Color draw = (selectedSeg >= 0 && selectedSeg != i) ? fade(base) : base;
            int ox = 0, oy = 0;
            if (selectedSeg == i) { double m = Math.toRadians(sa + arcs[i] / 2.0); ox = (int)(Math.cos(m)*6); oy = (int)(-Math.sin(m)*6); }
            g2.setColor(draw);
            g2.fillArc(paintCx+ox-OUTER_R, paintCy+oy-OUTER_R, OUTER_R*2, OUTER_R*2, sa, arcs[i]);
            sa += arcs[i];
        }
        g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2.5f));
        for (int s : startAngles) { double r = Math.toRadians(s); g2.drawLine(paintCx, paintCy, paintCx+(int)((OUTER_R+8)*Math.cos(r)), paintCy-(int)((OUTER_R+8)*Math.sin(r))); }
        g2.setStroke(new BasicStroke(1f)); g2.setColor(Color.WHITE);
        g2.fillOval(paintCx-INNER_R, paintCy-INNER_R, INNER_R*2, INNER_R*2);

        if (selectedSeg >= 0) {
            String nm = LABELS[selectedSeg];
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9)); g2.setColor(new Color(140,142,158));
            FontMetrics fm = g2.getFontMetrics(); g2.drawString(nm, paintCx-fm.stringWidth(nm)/2, paintCy-5);
            String val = String.valueOf(data[selectedSeg]);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18)); g2.setColor(emp ? new Color(80,85,105) : full[selectedSeg]);
            fm = g2.getFontMetrics(); g2.drawString(val, paintCx-fm.stringWidth(val)/2, paintCy+13);
        } else {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9)); g2.setColor(new Color(160,162,175));
            FontMetrics fm = g2.getFontMetrics(); String top="TỔNG";
            g2.drawString(top, paintCx-fm.stringWidth(top)/2, paintCy-5);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18)); g2.setColor(new Color(50,55,75));
            fm = g2.getFontMetrics(); String tot=String.valueOf(total);
            g2.drawString(tot, paintCx-fm.stringWidth(tot)/2, paintCy+13);
        }

        int lt=paintCy+OUTER_R+16, dotH=10, gap=12;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fmL = g2.getFontMetrics();
        int totW=0; for (String l:LABELS) totW+=dotH+4+fmL.stringWidth(l)+gap; totW-=gap;
        int lx=(w-totW)/2;
        for (int i=0;i<LABELS.length;i++) {
            Color dot=emp?empty[i]:full[i]; if(selectedSeg>=0&&selectedSeg!=i) dot=fade(dot);
            g2.setColor(dot); g2.fillRoundRect(lx,lt,dotH,dotH,4,4);
            Color txt=(selectedSeg==i)?(emp?new Color(60,65,85):full[i]):new Color(100,105,125);
            if(selectedSeg>=0&&selectedSeg!=i) txt=new Color(180,182,195);
            g2.setColor(txt); g2.setFont(new Font("Segoe UI",selectedSeg==i?Font.BOLD:Font.PLAIN,10));
            g2.drawString(LABELS[i],lx+dotH+4,lt+dotH-1);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
            lx+=dotH+4+fmL.stringWidth(LABELS[i])+gap;
        }
        g2.dispose();
    }
}
