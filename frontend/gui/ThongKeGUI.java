package gui;

import dao.HoaDonDAO;
import dao.VeDAO;

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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class ThongKeGUI extends JPanel {

    private static final Color BORDER   = new Color(210, 215, 224);
    private static final Color PRIMARY  = new Color(71, 71, 156);

    // ── Dữ liệu trạng thái thống kê ──────────────────────────────────────────
    private String tenNhanVien = "Nhân viên"; // Dùng để xuất PDF
    private long   doanhThu    = 0;
    private int    veBan       = 0;
    private int    veHuy       = 0;

    // ── Tham chiếu để cập nhật UI ────────────────────────────────────────────
    private ChartPanel chartPanel;
    private final JLabel[] lblStatValues = new JLabel[3];
    private DefaultTableModel tblModel;

    public ThongKeGUI() {
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

    // Cập nhật trạng thái để nút Xuất PDF có thể lấy dữ liệu
    public void setThongKe(String tenNV, long dt, int vb, int vh) {
        this.tenNhanVien = tenNV;
        this.doanhThu    = dt;
        this.veBan       = vb;
        this.veHuy       = vh;
    }

    // ── CORE: Hàm Load Data Tối Ưu (Theo Ngày & Ca) ──────────────────────────
    public void loadData(LocalDate ngay, String ca, String tenNV) {
        // Sử dụng Background Thread để truy vấn DB không làm treo UI (Đơ màn hình)
        new Thread(() -> {
            try {
                // 1. Kéo dữ liệu từ tầng DAO
                long dt   = VeDAO.getTongDoanhThuTheoCa(ngay, ca);
                int vb    = VeDAO.getSoLuongVeTheoCa(ngay, ca, "DA_THANH_TOAN");
                int vh    = VeDAO.getSoLuongVeTheoCa(ngay, ca, "DA_HUY");
                int[] ghe = VeDAO.getSoGheTheoLoaiTheoCa(ngay, ca);

                List<Object[]> hdList = HoaDonDAO.getDanhSachHoaDonTheoCa(ngay, ca);

                // 2. Cập nhật giao diện BẮT BUỘC phải đưa về Event Dispatch Thread (EDT)
                SwingUtilities.invokeLater(() -> {
                    // Lưu dữ liệu để Export PDF
                    setThongKe(tenNV, dt, vb, vh);

                    // Render các thẻ Tóm tắt (Summary Cards)
                    lblStatValues[0].setText(String.format("%,.0f đ", (double) dt));
                    lblStatValues[1].setText(vb + " vé");
                    lblStatValues[2].setText(vh + " vé");

                    // Render Chart Donut
                    chartPanel.setData(ghe[0], ghe[1], ghe[2]);

                    // Render Table Hóa đơn
                    tblModel.setRowCount(0); // Clear dữ liệu cũ
                    for (Object[] row : hdList) {
                        // row[4] là tổng tiền, format lại trước khi lên UI
                        row[4] = String.format("%,.0f đ", (Double) row[4]);
                        tblModel.addRow(row);
                    }
                });

            } catch (SQLException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                ThongKeGUI.this,
                                "Lỗi tải dữ liệu thống kê:\n" + e.getMessage(),
                                "Lỗi kết nối",
                                JOptionPane.ERROR_MESSAGE
                        )
                );
            }
        }, "ThongKe-LoadThread").start();
    }

    // ── 1. SUMMARY BAR ────────────────────────────────────────────────────────
    private JPanel buildSummaryBar() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(6, 0, 6, 0));

        pnl.add(buildStatCard("Tổng doanh thu", "0 đ",  new Color(71,  71, 156), 0));
        pnl.add(Box.createHorizontalStrut(16));
        pnl.add(buildStatCard("Vé đã bán",      "0 vé", new Color(34, 139,  87), 1));
        pnl.add(Box.createHorizontalStrut(16));
        pnl.add(buildStatCard("Vé đã hủy",      "0 vé", new Color(210, 50,  50), 2));
        pnl.add(Box.createHorizontalGlue());

        return pnl;
    }

    private JPanel buildStatCard(String label, String value, Color accent, int idx) {
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

        lblStatValues[idx] = lbValue;

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
        // Cập nhật lại 5 Cột chuẩn xác theo yêu cầu
        tblModel = new DefaultTableModel(
                new Object[]{"Mã HĐ", "Ngày bán vé", "Khách hàng", "Số ghế", "Tổng tiền"},
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

        // Căn lề cho Data để giao diện tinh tế hơn
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblData.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Mã HĐ
        tblData.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Số Ghế

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblData.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Tổng tiền

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        spnScroll.setPreferredSize(new Dimension(10000, 300));

        // Chia tỷ lệ cột cho hợp lý
        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 5) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(90);  // Mã HĐ
                tblData.getColumnModel().getColumn(1).setPreferredWidth(140); // Ngày bán
                tblData.getColumnModel().getColumn(2).setPreferredWidth(180); // Khách hàng
                tblData.getColumnModel().getColumn(3).setPreferredWidth(80);  // Số ghế
                tblData.getColumnModel().getColumn(4).setPreferredWidth(130); // Tổng tiền
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
        JButton btn = new JButton("Tạo báo cáo") {
            private float hoverProgress = 0f;
            private boolean isHovered = false;
            private Timer timer;

            {
                timer = new Timer(15, e -> {
                    if (isHovered && hoverProgress < 1f) {
                        hoverProgress += 0.1f;
                        if (hoverProgress >= 1f) { hoverProgress = 1f; timer.stop(); }
                        repaint();
                    } else if (!isHovered && hoverProgress > 0f) {
                        hoverProgress -= 0.1f;
                        if (hoverProgress <= 0f) { hoverProgress = 0f; timer.stop(); }
                        repaint();
                    }
                });

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true;  timer.start(); }
                    @Override public void mouseExited(MouseEvent e)  { isHovered = false; timer.start(); }
                });
            }

            private Color blend(Color c1, Color c2, float ratio) {
                float ir = 1.0f - ratio;
                int r = Math.min(255, (int) (c1.getRed()   * ir + c2.getRed()   * ratio));
                int g = Math.min(255, (int) (c1.getGreen() * ir + c2.getGreen() * ratio));
                int b = Math.min(255, (int) (c1.getBlue()  * ir + c2.getBlue()  * ratio));
                return new Color(r, g, b);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color normal  = GuiTheme.NAVY;
                Color hover   = GuiTheme.NAVY_HOVER;
                Color pressed = GuiTheme.NAVY.darker();
                Color bg = getModel().isPressed() ? pressed : blend(normal, hover, hoverProgress);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(155, 40));

        btn.addActionListener(e -> {
            int[] d = chartPanel.getData();
            // Thầy giữ nguyên logic xuất báo cáo của em
            BaoCaoPDF.export(tenNhanVien, doanhThu, veBan, veHuy, d[1], d[2], d[0]);
        });

        return btn;
    }
}

// ── BIỂU ĐỒ DONUT (Giữ nguyên 100%) ──────────────────────────────────────────
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
            if (selectedSeg == i) {
                double m = Math.toRadians(sa + arcs[i] / 2.0);
                ox = (int)(Math.cos(m)*6); oy = (int)(-Math.sin(m)*6);
            }
            g2.setColor(draw);
            g2.fillArc(paintCx+ox-OUTER_R, paintCy+oy-OUTER_R, OUTER_R*2, OUTER_R*2, sa, arcs[i]);
            sa += arcs[i];
        }
        g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2.5f));
        for (int s : startAngles) {
            double r = Math.toRadians(s);
            g2.drawLine(paintCx, paintCy,
                    paintCx + (int)((OUTER_R+8)*Math.cos(r)),
                    paintCy - (int)((OUTER_R+8)*Math.sin(r)));
        }
        g2.setStroke(new BasicStroke(1f)); g2.setColor(Color.WHITE);
        g2.fillOval(paintCx-INNER_R, paintCy-INNER_R, INNER_R*2, INNER_R*2);

        if (selectedSeg >= 0) {
            String nm = LABELS[selectedSeg];
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9)); g2.setColor(new Color(140,142,158));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(nm, paintCx-fm.stringWidth(nm)/2, paintCy-5);
            String val = String.valueOf(data[selectedSeg]);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.setColor(emp ? new Color(80,85,105) : full[selectedSeg]);
            fm = g2.getFontMetrics();
            g2.drawString(val, paintCx-fm.stringWidth(val)/2, paintCy+13);
        } else {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9)); g2.setColor(new Color(160,162,175));
            FontMetrics fm = g2.getFontMetrics(); String top = "TỔNG";
            g2.drawString(top, paintCx-fm.stringWidth(top)/2, paintCy-5);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18)); g2.setColor(new Color(50,55,75));
            fm = g2.getFontMetrics(); String tot = String.valueOf(total);
            g2.drawString(tot, paintCx-fm.stringWidth(tot)/2, paintCy+13);
        }

        int lt = paintCy+OUTER_R+16, dotH = 10, gap = 12;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fmL = g2.getFontMetrics();
        int totW = 0;
        for (String l : LABELS) totW += dotH+4+fmL.stringWidth(l)+gap; totW -= gap;
        int lx = (w-totW)/2;
        for (int i = 0; i < LABELS.length; i++) {
            Color dot = emp ? empty[i] : full[i];
            if (selectedSeg >= 0 && selectedSeg != i) dot = fade(dot);
            g2.setColor(dot); g2.fillRoundRect(lx, lt, dotH, dotH, 4, 4);
            Color txt = (selectedSeg == i) ? (emp ? new Color(60,65,85) : full[i]) : new Color(100,105,125);
            if (selectedSeg >= 0 && selectedSeg != i) txt = new Color(180,182,195);
            g2.setColor(txt);
            g2.setFont(new Font("Segoe UI", selectedSeg == i ? Font.BOLD : Font.PLAIN, 10));
            g2.drawString(LABELS[i], lx+dotH+4, lt+dotH-1);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lx += dotH+4+fmL.stringWidth(LABELS[i])+gap;
        }
        g2.dispose();
    }
}