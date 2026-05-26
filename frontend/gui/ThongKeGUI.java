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
    private final JLabel[] lblStatValues = new JLabel[6];
    private long doanhThuTienMat = 0;
    private long doanhThuCK      = 0;
    private DefaultTableModel tblModel;

    private TableRowSorter<DefaultTableModel> sorter;
    private JTable tblData;

    public void setTienMoCa(long tien) {
        this.tienMoCa = tien;
        loadData(); // load lại để tính doanhThu = loiNhuan + tienMoCa.
    }

    public ThongKeGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        // Panel phía trên: summary bar
        JPanel pnlTop = new JPanel();
        pnlTop.setOpaque(false);
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, 0, GuiTheme.PAGE_PAD_LEFT));
        pnlTop.add(Box.createVerticalStrut(12));
        pnlTop.add(buildSummaryBar());
        pnlTop.add(Box.createVerticalStrut(12));

        // Panel giữa: bảng + biểu đồ — fill hết phần còn lại
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));
        pnlCenter.add(buildTableWithChart(), BorderLayout.CENTER);


        add(pnlTop,            BorderLayout.NORTH);
        add(pnlCenter,         BorderLayout.CENTER);
        add(buildBottomBar(),  BorderLayout.SOUTH);

        // Tự động load dữ liệu hôm nay theo nhân viên đang đăng nhập
        loadData();
    }

    private static boolean isCK(String pttt) {
        if (pttt == null) return false;
        String s = pttt.toLowerCase();
        return s.contains("chuyen_khoan") || s.contains("chuyển khoản") || s.contains("chuyen khoan") || s.contains("vietqr") || s.equals("chuyen_khoan");
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

                long ln = 0, tm = 0, ck = 0;
                int  vb = 0;
                for (Object[] row : hdList) {
                    long t = ((Double) row[5]).longValue();
                    ln += t;
                    vb += (int) row[4];
                    String pttt = row[6] != null ? row[6].toString() : "";
                    if (isCK(pttt)) ck += t;
                    else tm += t;
                }
                // Cộng phí phạt trả vé vào doanh thu và lợi nhuận
                for (Object[] row : hdHuyTemp) {
                    long t = ((Double) row[5]).longValue();
                    ln += t;
                    String pttt = row[6] != null ? row[6].toString() : "";
                    if (isCK(pttt)) ck += t;
                    else tm += t;
                }
                final long loiNhuanFinal = ln;
                final long doanhThuFinal = ln + tienMoCa;
                final long tmFinal = tm, ckFinal = ck;
                final int  veBanFinal    = vb;

                SwingUtilities.invokeLater(() -> {
                    this.loiNhuan         = loiNhuanFinal;
                    this.doanhThu         = doanhThuFinal;
                    this.veBan            = veBanFinal;
                    this.veHuy            = vh;
                    this.hdBanList        = hdList;
                    this.hdHuyList        = hdHuyTemp;
                    this.doanhThuTienMat  = tmFinal;
                    this.doanhThuCK       = ckFinal;
                    this.currentFilter    = null;

                    lblStatValues[0].setText(String.format("%,.0f đ", (double) (tienMoCa + tmFinal)));
                    lblStatValues[1].setText(String.format("%,.0f đ", (double) loiNhuanFinal));
                    lblStatValues[2].setText(veBanFinal + " vé");
                    lblStatValues[3].setText(vh + " vé");
                    if (lblStatValues[4] != null) lblStatValues[4].setText(String.format("%,.0f đ", (double) tmFinal).replace(",", "."));
                    if (lblStatValues[5] != null) lblStatValues[5].setText(String.format("%,.0f đ", (double) ckFinal).replace(",", "."));

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
                    ? String.format("%,.0f đ", (Double) row[5]).replace(",", ".")
                    : row[5];
            // Chuẩn hóa tên hình thức thanh toán
            String pttt = row[6] != null ? row[6].toString() : "";
            if ("TIEN_MAT".equalsIgnoreCase(pttt)) pttt = "Tiền mặt";
            else if ("CHUYEN_KHOAN".equalsIgnoreCase(pttt) || pttt.toLowerCase().contains("vietqr")) pttt = "Chuyển khoản";
            else if (pttt.toLowerCase().contains("hoàn tiền")) pttt = "Hoàn tiền";
            tblModel.addRow(new Object[]{row[0], row[1], row[2], pttt, row[4], tongTien});
        }
    }
    // ──────────────

    private JPanel buildSummaryBar() {
        // Hàng 1: Tổng doanh thu | Vé đã bán | Tiền mặt
        JPanel row1 = new JPanel(new GridLayout(1, 3, 12, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        row1.add(buildStatCard("Số dư két", "0 đ",  new Color(71,  71, 156), 0, false));
        cardVeBan = buildStatCard("Vé đã bán", "0 vé", new Color(34, 139, 87), 2, true);
        row1.add(cardVeBan);
        row1.add(buildStatCard("Tiền mặt",      "0 đ", new Color(180, 120, 30), 4, false));

        // Hàng 2: Tổng lợi nhuận | Vé đã hủy | Chuyển khoản
        JPanel row2 = new JPanel(new GridLayout(1, 3, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        row2.add(buildStatCard("Tổng doanh thu", "0 đ",  new Color(34, 120, 180), 1, false));
        cardVeHuy = buildStatCard("Vé đã hủy", "0 vé", new Color(210, 50, 50), 3, true);
        row2.add(cardVeHuy);
        row2.add(buildStatCard("Chuyển khoản",   "0 đ", new Color(30, 140, 160), 5, false));

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        pnl.add(row1);
        pnl.add(Box.createVerticalStrut(10));
        pnl.add(row2);
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
                new Object[]{"Mã HĐ", "Giờ bán", "Khách hàng", "Hình thức TT", "Số vé", "Tổng tiền"}, 0) {
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
        tblData.setSelectionBackground(new Color(207, 222, 243));
        tblData.setSelectionForeground(Color.BLACK);

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

        // Bóp cột: Mã HĐ | Giờ bán | Khách hàng | Hình thức TT | Số vé | Tổng tiền
        tblData.getColumnModel().getColumn(0).setPreferredWidth(130); // Mã HĐ
        tblData.getColumnModel().getColumn(1).setPreferredWidth(70);  // Giờ bán
        tblData.getColumnModel().getColumn(2).setPreferredWidth(140); // Khách hàng
        tblData.getColumnModel().getColumn(3).setPreferredWidth(110); // Hình thức TT
        tblData.getColumnModel().getColumn(4).setPreferredWidth(55);  // Số vé
        tblData.getColumnModel().getColumn(5).setPreferredWidth(110); // Tổng tiền
        ((DefaultTableCellRenderer) tblData.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        // height tự fill theo CENTER layout

        // Click vào hàng → mở PDF hóa đơn
// Click vào hàng → hiện dialog chi tiết hóa đơn
        tblData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblData.getSelectedRow();
                if (row < 0) return;
                int modelRow = tblData.convertRowIndexToModel(row);
                showHoaDonDetail(modelRow);
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
    private void showHoaDonDetail(int modelRow) {
        String maHD = (String) tblModel.getValueAt(modelRow, 0);
        if (maHD == null) return;
        boolean isHuy = "huy".equals(currentFilter);

        new Thread(() -> {
            try {
                Object[] hdInfo     = HoaDonDAO.getThongTinHoaDon(maHD);
                List<Object[]> veList = HoaDonDAO.getDanhSachVeTheoHoaDon(maHD);
                SwingUtilities.invokeLater(() -> buildAndShowDialog(maHD, hdInfo, veList, isHuy));
            } catch (SQLException ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Chi tiết lỗi:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void buildAndShowDialog(String maHD, Object[] d, List<Object[]> veList, boolean isHuy) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn", true);
        dlg.setSize(660, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 28, 16, 28));

        // ── Badge trạng thái ──
        JPanel pnlBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBadge.setOpaque(false);
        JLabel badge = new JLabel(isHuy ? "  Đã hủy  " : "  Đã thanh toán  ");
        badge.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        badge.setOpaque(true);
        badge.setBackground(isHuy ? new Color(220, 53, 69) : new Color(34, 139, 87));
        badge.setForeground(Color.WHITE);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        pnlBadge.add(badge);

        // ── Thông tin khách hàng + giao dịch (2 cột) ──
        String pttt = d != null && d[2] != null ? d[2].toString() : "—";
        if ("TIEN_MAT".equalsIgnoreCase(pttt))         pttt = "Tiền mặt";
        else if ("CHUYEN_KHOAN".equalsIgnoreCase(pttt)) pttt = "Chuyển khoản";
        String tongTien = d != null
                ? String.format("%,.0f VNĐ", (Double) d[3]).replace(",", ".")
                : "—";
        String thoiGianLap = d != null && d[1] != null
                ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(d[1])
                : "—";

        JPanel pnlCols = new JPanel(new GridLayout(1, 2, 24, 0));
        pnlCols.setOpaque(false);

        JPanel pnlLeft = buildInfoSection("THÔNG TIN KHÁCH HÀNG", new String[][]{
                {"Họ và tên:",     d != null && d[4] != null ? d[4].toString() : "—"},
                {"Số điện thoại:", d != null && d[5] != null ? d[5].toString() : "—"},
                {"Đối tượng:",     d != null && d[6] != null ? d[6].toString() : "—"},
        }, false);

        JPanel pnlRight = buildInfoSection("THÔNG TIN GIAO DỊCH", new String[][]{
                {"Thời gian lập:", thoiGianLap},
                {"Hình thức TT:",  pttt},
                {"Tổng tiền:",     tongTien},
        }, false);

        pnlCols.add(pnlLeft);
        pnlCols.add(pnlRight);

        // ── Separator ──
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(210, 215, 224));

        // ── Bảng danh sách vé ──
        JLabel lbVe = new JLabel("DANH SÁCH VÉ (" + veList.size() + " vé)");
        lbVe.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbVe.setForeground(new Color(40, 45, 70));
        lbVe.setBorder(new EmptyBorder(8, 0, 6, 0));

        DefaultTableModel veModel = new DefaultTableModel(
                new Object[]{"Mã vé", "Tàu", "Lộ trình", "Khởi hành", "Ghế", "Giá vé"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM HH:mm");
        for (Object[] ve : veList) {
            String trangThaiVe = ve[1] != null ? ve[1].toString() : "—";
            String giaVe = String.format("%,.0f đ", (Double) ve[2]).replace(",", ".");
            String gioKH = ve[8] != null ? sdf.format(ve[8]) : "—";
            String soGhe = ve[4] != null
                    ? "Ghế " + ve[4] + " (" + formatLoaiGhe(ve[5] != null ? ve[5].toString() : "") + ")"
                    : "—";
            veModel.addRow(new Object[]{
                    ve[0], ve[6], ve[7], gioKH, soGhe, giaVe
            });
        }

        JTable tblVe = new JTable(veModel);
        tblVe.setRowHeight(32);
        tblVe.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        tblVe.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        tblVe.setShowVerticalLines(false);
        tblVe.setSelectionBackground(new Color(207, 222, 243));
        tblVe.getTableHeader().setReorderingAllowed(false);

        // Căn giữa tất cả cột
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tblVe.getColumnCount(); i++)
            tblVe.getColumnModel().getColumn(i).setCellRenderer(center);

        // Width cột
        tblVe.getColumnModel().getColumn(0).setPreferredWidth(90);  // Mã vé
        tblVe.getColumnModel().getColumn(1).setPreferredWidth(70);  // Tàu
        tblVe.getColumnModel().getColumn(2).setPreferredWidth(130); // Lộ trình
        tblVe.getColumnModel().getColumn(3).setPreferredWidth(80);  // Khởi hành
        tblVe.getColumnModel().getColumn(4).setPreferredWidth(110); // Ghế
        tblVe.getColumnModel().getColumn(5).setPreferredWidth(70);  // Giá vé

        JScrollPane spVe = new JScrollPane(tblVe);
        spVe.setBorder(new LineBorder(new Color(210, 215, 224), 1, true));
        spVe.setPreferredSize(new Dimension(600, Math.min(veList.size() * 32 + 30, 160)));

        // ── Gom phần giữa ──
        JPanel pnlMid = new JPanel();
        pnlMid.setLayout(new BoxLayout(pnlMid, BoxLayout.Y_AXIS));
        pnlMid.setOpaque(false);
        pnlMid.add(pnlCols);
        pnlMid.add(Box.createVerticalStrut(10));
        pnlMid.add(sep);
        pnlMid.add(lbVe);
        pnlMid.add(spVe);

        // ── Nút ──
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtn.setOpaque(false);
        pnlBtn.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnIn = buildNavyButton("In lại hoá đơn", null);
        btnIn.addActionListener(e -> openPDF(maHD));

        JButton btnDong = new JButton("Đóng") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(80,85,100) : new Color(100,105,120));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnDong.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btnDong.setContentAreaFilled(false);
        btnDong.setBorderPainted(false);
        btnDong.setFocusPainted(false);
        btnDong.setPreferredSize(new Dimension(90, 34));
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(e -> dlg.dispose());

        pnlBtn.add(btnIn);
        pnlBtn.add(btnDong);

        root.add(pnlBadge, BorderLayout.NORTH);
        root.add(pnlMid,   BorderLayout.CENTER);
        root.add(pnlBtn,   BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // Helper: tạo 1 section thông tin
    private JPanel buildInfoSection(String title, String[][] rows, boolean highlightLast) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);

        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbTitle.setForeground(new Color(40, 45, 70));
        pnl.add(lbTitle);
        pnl.add(Box.createVerticalStrut(8));

        for (int i = 0; i < rows.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
            row.setOpaque(false);
            JLabel lbKey = new JLabel(rows[i][0] + " ");
            lbKey.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
            lbKey.setForeground(new Color(70, 75, 95));
            JLabel lbVal = new JLabel(rows[i][1]);
            lbVal.setFont(GuiTheme.font("Segoe UI",
                    (i == rows.length - 1 && !highlightLast) ? Font.BOLD : Font.PLAIN, 13));
            // "Không hợp lệ" tô đỏ, "Hợp lệ" tô xanh
            if (highlightLast && i == rows.length - 1)
                lbVal.setForeground(new Color(220, 53, 69));
            else
                lbVal.setForeground(new Color(30, 35, 55));
            row.add(lbKey);
            row.add(lbVal);
            pnl.add(row);
        }
        return pnl;
    }

    // Helper: dịch loại ghế
    private String formatLoaiGhe(String loai) {
        return switch (loai.toUpperCase()) {
            case "GHE_CUNG"   -> "Ghế cứng";
            case "GHE_MEM"    -> "Ghế mềm";
            case "GIUONG_NAM" -> "Giường nằm";
            default           -> loai;
        };
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
    private static final int OUTER_R = 75, INNER_R = 46;
    private int selectedIdx = -1;

    public interface FilterListener { void onFilter(String type); }
    private FilterListener listener;

    public ChartPanel() {
    	setPreferredSize(new Dimension(260, 380));
        setBackground(Color.WHITE);
        setBorder(new LineBorder(new Color(210, 215, 224), 1, true));
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
            g2.setColor(new Color(50, 55, 75)); g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            g2.drawString(LABELS[i], 44, itemY + 12);
            g2.setColor(new Color(120, 125, 145)); g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
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