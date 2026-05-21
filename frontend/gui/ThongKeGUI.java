package gui;

import dao.HoaDonDAO;
import dao.VeDAO;
import service.AuthService;

import java.awt.*;
import java.awt.event.*;
import java.awt.Desktop;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

public final class ThongKeGUI extends JPanel {

    private static final Color BORDER  = new Color(210, 215, 224);
    private static final Color PRIMARY = new Color(71, 71, 156);

    private long doanhThu = 0;
    private long loiNhuan = 0;
    private long tienMoCa = 0;
    private int  veBan    = 0;
    private int  veHuy    = 0;

    // ── THÊM MỚI ──
    private List<Object[]> hdBanList  = new ArrayList<>();
    private List<Object[]> hdHuyList  = new ArrayList<>();
    private String currentFilter      = null; // null / "ban" / "huy"
    private JPanel cardVeBan;
    private JPanel cardVeHuy;
    // ──────────────

    private ChartPanel chartPanel;
    private final JLabel[] lblStatValues = new JLabel[4];
    private DefaultTableModel tblModel;

    private TableRowSorter<DefaultTableModel> sorter;
    private JTable tblData;

    public void setTienMoCa(long tien) {
        this.tienMoCa = tien;
    }

    public ThongKeGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BoxLayout(pnlPage, BoxLayout.Y_AXIS));
        pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(buildSummaryBar());
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(buildTableWithChart());

        chartPanel.setOnFilterListener(type -> {
            if (type == null) sorter.setRowFilter(null);
            else              sorter.setRowFilter(RowFilter.regexFilter(type, 3));
        });

        add(pnlPage, BorderLayout.NORTH);
        add(buildBottomBar(), BorderLayout.SOUTH);

        // Tự động load dữ liệu hôm nay theo nhân viên đang đăng nhập
        loadData();
    }

    public void loadData() {
        String maNV = AuthService.getCurrentMaNV();
        if (maNV == null || maNV.isEmpty()) return; // chưa đăng nhập

        new Thread(() -> {
            try {
                int   vh  = VeDAO.getSoVeHuyHomNay(maNV);
                int[] ghe = VeDAO.getSoGheTheoLoaiHomNay(maNV);
                List<Object[]> hdList    = HoaDonDAO.getDanhSachHoaDonHomNay(maNV);
                List<Object[]> hdHuyTemp = HoaDonDAO.getDanhSachHoaDonHuyHomNay(maNV);

                long ln = 0;
                int  vb = 0;
                for (Object[] row : hdList) {
                    ln += ((Double) row[5]).longValue();
                    vb += (int) row[4];
                }
                final long loiNhuanFinal = ln;
                final long doanhThuFinal = ln + tienMoCa;
                final int  veBanFinal    = vb;

                SwingUtilities.invokeLater(() -> {
                    this.loiNhuan      = loiNhuanFinal;
                    this.doanhThu      = doanhThuFinal;
                    this.veBan         = veBanFinal;
                    this.veHuy         = vh;
                    this.hdBanList     = hdList;
                    this.hdHuyList     = hdHuyTemp;
                    this.currentFilter = null;

                    lblStatValues[0].setText(String.format("%,.0f đ", (double) doanhThuFinal));
                    lblStatValues[1].setText(String.format("%,.0f đ", (double) loiNhuanFinal));
                    lblStatValues[2].setText(veBanFinal + " vé");
                    lblStatValues[3].setText(vh + " vé");

                    chartPanel.setData(ghe[0], ghe[1], ghe[2]);
                    hienThiBang(hdBanList);

                    if (cardVeBan != null) cardVeBan.repaint();
                    if (cardVeHuy != null) cardVeHuy.repaint();
                });
            } catch (SQLException e) { e.printStackTrace(); }
        }).start();
    }

    // ── THÊM MỚI ──
    private void hienThiBang(List<Object[]> list) {
        tblModel.setRowCount(0);
        sorter.setRowFilter(null);
        for (Object[] row : list) {
            Object tongTien = row[5] instanceof Double
                ? String.format("%,.0f đ", (Double) row[5])
                : row[5];
            tblModel.addRow(new Object[]{row[0], row[1], row[2], row[3], row[4], tongTien});
        }
    }
    // ──────────────

    private JPanel buildSummaryBar() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setOpaque(false);
        pnl.add(buildStatCard("Tổng doanh thu", "0 đ",  new Color(71,  71, 156), 0, false));
        pnl.add(Box.createHorizontalStrut(16));
        pnl.add(buildStatCard("Tổng lợi nhuận", "0 đ",  new Color(34, 120, 180), 1, false));
        pnl.add(Box.createHorizontalStrut(16));
        cardVeBan = buildStatCard("Vé đã bán",   "0 vé", new Color(34, 139,  87), 2, true);
        pnl.add(cardVeBan);
        pnl.add(Box.createHorizontalStrut(16));
        cardVeHuy = buildStatCard("Vé đã hủy",   "0 vé", new Color(210,  50,  50), 3, true);
        pnl.add(cardVeHuy);
        pnl.add(Box.createHorizontalGlue());
        return pnl;
    }

    private JPanel buildStatCard(String label, String value, Color accent, int idx, boolean clickable) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                boolean selected = (idx == 2 && "ban".equals(currentFilter))
                                || (idx == 3 && "huy".equals(currentFilter));
                g2.setColor(selected ? accent : new Color(220, 224, 232));
                g2.setStroke(new BasicStroke(selected ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1f));
                g2.fillRect(0, 0, 5, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 18, 10, 22));
        card.setPreferredSize(new Dimension(220, 78));
        card.setMaximumSize(new Dimension(220, 78));
        if (clickable) card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbLabel = new JLabel(label.toUpperCase());
        lbLabel.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lbLabel.setForeground(new Color(130, 135, 155));

        JLabel lbValue = new JLabel(value);
        lbValue.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 23));
        lbValue.setForeground(new Color(28, 32, 52));

        lblStatValues[idx] = lbValue;
        card.add(lbLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(lbValue);

        if (clickable) {
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (idx == 2) {
                        currentFilter = "ban".equals(currentFilter) ? null : "ban";
                        hienThiBang(hdBanList);
                    } else {
                        if ("huy".equals(currentFilter)) {
                            currentFilter = null;
                            hienThiBang(hdBanList);
                        } else {
                            currentFilter = "huy";
                            hienThiBang(hdHuyList);
                        }
                    }
                    if (cardVeBan != null) cardVeBan.repaint();
                    if (cardVeHuy != null) cardVeHuy.repaint();
                }
            });
        }
        return card;
    }

    private JPanel buildTableWithChart() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);
        chartPanel = new ChartPanel();
        pnl.add(buildTablePanel(), BorderLayout.CENTER);
        pnl.add(chartPanel,        BorderLayout.EAST);
        return pnl;
    }

    private JPanel buildTablePanel() {
        tblModel = new DefaultTableModel(
                new Object[]{"Mã HĐ", "Giờ bán", "Khách hàng", "Loại ghế", "Số vé", "Tổng tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblData = new JTable(tblModel);
        sorter  = new TableRowSorter<>(tblModel);
        tblData.setRowSorter(sorter);

        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setResizingAllowed(false);

        tblData.setRowHeight(36);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        tblData.setShowVerticalLines(false);
        tblData.setSelectionBackground(new Color(71, 71, 156));
        tblData.setSelectionForeground(Color.WHITE);

        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, row, col);
                if (!s) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < tblData.getColumnCount(); i++)
            tblData.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
        ((DefaultTableCellRenderer) tblData.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        spnScroll.setPreferredSize(new Dimension(10000, 300));

        // Click vào hàng → mở PDF hóa đơn
        tblData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblData.getSelectedRow();
                if (row < 0) return;
                int modelRow = tblData.convertRowIndexToModel(row);
                String maHoaDon = (String) tblModel.getValueAt(modelRow, 0);
                if (maHoaDon != null) openPDF(maHoaDon);
            }
        });

        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);
        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    private void openPDF(String maHoaDon) {
        File pdfFile = new File("HoaDon", maHoaDon + ".pdf");
        if (!pdfFile.exists()) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy file hóa đơn:\n" + pdfFile.getAbsolutePath(),
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Desktop.getDesktop().open(pdfFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể mở file PDF: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(8, GuiTheme.PAGE_PAD_LEFT, 14, GuiTheme.PAGE_PAD_LEFT));
        bar.add(buildExportButton(), BorderLayout.EAST);
        return bar;
    }

    private JButton buildExportButton() {
        JButton btn = buildNavyButton("Xuất báo cáo kết ca", null);
        btn.addActionListener(e -> {
            int[] d = chartPanel.getData();
            String tenNV = AuthService.getCurrentHoTen() != null ? AuthService.getCurrentHoTen() : "";
            BaoCaoPDF.export(tenNV, doanhThu, loiNhuan, veBan, veHuy, d[1], d[2], d[0]);
        });
        return btn;
    }

    private JButton buildNavyButton(String text, String iconPath) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY.darker() : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                int startX = 14;
                if (iconPath != null) {
                    int iconSize = 18;
                    Icon icon = GuiIcons.loadIcon(ThongKeGUI.class, iconPath, iconSize, iconSize);
                    if (icon != null) {
                        int iconY = (getHeight() - icon.getIconHeight()) / 2;
                        icon.paintIcon(this, g2, startX, iconY);
                        startX += icon.getIconWidth() + 8;
                    }
                }

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                if (iconPath == null) {
                    startX = (getWidth() - fm.stringWidth(getText())) / 2;
                }
                g2.drawString(getText(), startX, textY);
                g2.dispose();
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Dimension pref = btn.getPreferredSize();
        int w = pref.width + (iconPath != null ? 35 : 30);
        btn.setPreferredSize(new Dimension(w, 34));

        return btn;
    }
}

class ChartPanel extends JPanel {
    private static final Color[]  COLORS = { new Color(88, 130, 210), new Color(60, 179, 113), new Color(255, 165, 50) };
    private static final String[] LABELS = {"Ghế cứng", "Giường nằm", "Ghế mềm"};
    private static final int LEGEND_STEP = 38;
    private int[] data = {0, 0, 0};
    private static final int OUTER_R = 70, INNER_R = 44;
    private int selectedIdx = -1;

    public interface FilterListener { void onFilter(String type); }
    private FilterListener listener;

    public ChartPanel() {
        setPreferredSize(new Dimension(245, 380));
        setBackground(Color.WHITE);
        setBorder(new LineBorder(new Color(210, 215, 224), 1, true));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int old = selectedIdx;
                selectedIdx = detectIndex(e.getPoint());
                if (old == selectedIdx) selectedIdx = -1;
                repaint();
                if (listener != null) listener.onFilter(selectedIdx == -1 ? null : LABELS[selectedIdx]);
            }
        });
    }

    private int detectIndex(Point p) {
        int cx = getWidth() / 2, cy = OUTER_R + 25;
        double dist = p.distance(cx, cy);
        if (dist >= INNER_R && dist <= OUTER_R) {
            double angle = Math.toDegrees(Math.atan2(cy - p.y, p.x - cx));
            if (angle < 0) angle += 360;
            double mappedAngle = (450 - angle) % 360;
            int total = data[0] + data[1] + data[2];
            if (total == 0) return -1;
            double cur = 0;
            for (int i = 0; i < 3; i++) {
                double arc = 360.0 * data[i] / total;
                if (mappedAngle >= cur && mappedAngle < cur + arc) return i;
                cur += arc;
            }
        }
        int legendY = cy + OUTER_R + 20;
        for (int i = 0; i < 3; i++) {
            Rectangle r = new Rectangle(20, legendY + i * LEGEND_STEP - 4, 200, LEGEND_STEP - 4);
            if (r.contains(p)) return i;
        }
        return -1;
    }

    public void setOnFilterListener(FilterListener l) { this.listener = l; }
    public void setData(int gc, int gn, int gm) { data[0]=gc; data[1]=gn; data[2]=gm; selectedIdx=-1; repaint(); }
    public int[] getData() { return data; }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int cx = getWidth()/2, cy = OUTER_R + 25;
        int total = data[0]+data[1]+data[2];
        int sa = 90;
        for (int i=0; i<3; i++) {
            int arc = (total==0) ? 120 : (int)Math.round(360.0*data[i]/total);
            if(i==2 && total!=0) arc = 360 - (int)Math.round(360.0*data[0]/total) - (int)Math.round(360.0*data[1]/total);
            Color c = COLORS[i];
            if(selectedIdx!=-1 && selectedIdx!=i) c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60);
            g2.setColor(c);
            g2.fillArc(cx-OUTER_R, cy-OUTER_R, OUTER_R*2, OUTER_R*2, sa, arc);
            sa+=arc;
        }
        g2.setColor(Color.WHITE); g2.fillOval(cx-INNER_R, cy-INNER_R, INNER_R*2, INNER_R*2);
        int legendY = cy + OUTER_R + 20;
        for (int i=0; i<3; i++) {
            double pct = (total==0) ? 0 : (data[i]*100.0/total);
            Color c = COLORS[i];
            if(selectedIdx!=-1 && selectedIdx!=i) c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60);
            int itemY = legendY + i * LEGEND_STEP;
            g2.setColor(c); g2.fillRoundRect(22, itemY, 14, 14, 4, 4);
            g2.setColor(new Color(50, 55, 75)); g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
            g2.drawString(LABELS[i], 44, itemY + 12);
            g2.setColor(new Color(120, 125, 145)); g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
            g2.drawString(String.format("%.1f%%", pct) + " (" + data[i] + " vé)", 44, itemY + 28);
            if (selectedIdx == i) {
                g2.setColor(COLORS[i]); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(16, itemY - 5, 210, LEGEND_STEP - 2, 6, 6);
            }
        }
        g2.dispose();
    }
}
//i