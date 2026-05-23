package gui;

import connect_DB.Connect_DB;
import dao.HoaDonDAO;
import dao.VeDAO;
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

    private static final Color BORDER_C = new Color(210, 215, 224);
    private static final Color PRIMARY  = new Color(71, 71, 156);
    private static final Color LIGHT_BG = new Color(245, 247, 251);

    // ── KPI ──────────────────────────────────────────────────────────────────
    // [0]=Tổng lợi nhuận [1]=Vé đã bán [2]=Vé đã hủy [3]=Tiền mặt [4]=Chuyển khoản
    private final JLabel[] lblKpi = new JLabel[5];
    private JPanel cardVeBan, cardVeHuy;
    private String currentFilter = null;

    // ── Dữ liệu ──────────────────────────────────────────────────────────────
    private List<Object[]> hdBanList = new ArrayList<>();
    private List<Object[]> hdHuyList = new ArrayList<>();
    private long kLoiNhuan, kTienMat, kCK;
    private int  kVeBan, kVeHuy;
    private LocalDate filterFrom, filterTo;

    // ── Bộ lọc ───────────────────────────────────────────────────────────────
    private JComboBox<String> cboPeriod;
    // Spinner tháng/năm cho "Tháng" và "Năm"
    private JPanel extraPanel;
    private JComboBox<Integer> cboThang, cboNam, cboNamYear;

    // ── Bảng + Donut ─────────────────────────────────────────────────────────
    private DefaultTableModel tblModel;
    private DonutChartMgr     donutChart;

    // ── Bảng NV ──────────────────────────────────────────────────────────────
    private DefaultTableModel staffModel;

    // =========================================================================
    public ThongKeManagerGUI() { this(null); }

    public ThongKeManagerGUI(AppFrameManager appFrame) {
        setBackground(LIGHT_BG);
        setLayout(new BorderLayout());

        // Wrap toàn bộ vào scroll
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel pnlTop = new JPanel();
        pnlTop.setOpaque(false);
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.add(buildFilterBar());
        pnlTop.add(Box.createVerticalStrut(8));
        pnlTop.add(buildSummaryBar());
        pnlTop.add(Box.createVerticalStrut(8));

        page.add(pnlTop);
        page.add(buildMiddleRow());
        page.add(Box.createVerticalStrut(8));
        page.add(buildStaffCard());
        page.add(Box.createVerticalStrut(8));
        page.add(buildBottomBar(appFrame));

     // Wrap page trong 1 panel fit-width để tránh scroll ngang
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override public boolean isOptimizedDrawingEnabled() { return false; }
        };
        wrapper.setOpaque(false);
        wrapper.add(page, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        // Ẩn thanh cuộn, scroll mượt bằng chuột
        JScrollBar vBar = scroll.getVerticalScrollBar();
        vBar.setUnitIncrement(20);
        vBar.setBlockIncrement(80);
        vBar.setPreferredSize(new Dimension(0, 0));
        vBar.setMaximumSize(new Dimension(0, 0));
        vBar.setMinimumSize(new Dimension(0, 0));

        // Scroll mượt bằng trackpad / chuột
        scroll.addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            int val = vBar.getValue() + notches * 30;
            val = Math.max(vBar.getMinimum(), Math.min(val, vBar.getMaximum()));
            vBar.setValue(val);
        });

        add(scroll, BorderLayout.CENTER);
        SwingUtilities.invokeLater(this::applyPeriod);
    }

    // =========================================================================
    // BỘ LỌC
    // =========================================================================
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lbl = new JLabel("Thống kê:");
        lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(60, 65, 90));

        cboPeriod = new JComboBox<>(new String[]{"Hôm nay","Tuần này","Tháng","Năm","Tùy chọn"});
        cboPeriod.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        cboPeriod.setBackground(Color.WHITE);
        cboPeriod.setPreferredSize(new Dimension(120, 32));

        // Panel phụ: hiện thêm combo tháng/năm tùy loại chọn
        extraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        extraPanel.setOpaque(false);
        extraPanel.setVisible(false);

        cboPeriod.addActionListener(e -> onPeriodChange());

        JButton btnLoc = buildNavyButton("  Lọc  ");
        btnLoc.addActionListener(e -> applyPeriod());

        bar.add(lbl);
        bar.add(cboPeriod);
        bar.add(extraPanel);
        bar.add(btnLoc);
        return bar;
    }

    private void onPeriodChange() {
        String sel = (String) cboPeriod.getSelectedItem();
        extraPanel.removeAll();
        extraPanel.setVisible(false);

        int now = LocalDate.now().getYear();
        int nowM = LocalDate.now().getMonthValue();

        switch (sel) {
            case "Tháng" -> {
                cboThang = makeIntCombo(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12}, 80);
                cboThang.setSelectedItem(nowM);
                cboNam = makeIntCombo(yearArray(), 90);
                extraPanel.add(makeLabel("Tháng:")); extraPanel.add(cboThang);
                extraPanel.add(makeLabel("Năm:"));   extraPanel.add(cboNam);
                extraPanel.setVisible(true);
            }
            case "Năm" -> {
                cboNamYear = makeIntCombo(yearArray(), 90);
                extraPanel.add(makeLabel("Năm:")); extraPanel.add(cboNamYear);
                extraPanel.setVisible(true);
            }
            case "Tùy chọn" -> {
                JSpinner spFrom = makeDateSpinner(LocalDate.now().withDayOfMonth(1));
                JSpinner spTo   = makeDateSpinner(LocalDate.now());
                extraPanel.putClientProperty("spFrom", spFrom);
                extraPanel.putClientProperty("spTo",   spTo);
                extraPanel.add(makeLabel("Từ:"));  extraPanel.add(spFrom);
                extraPanel.add(makeLabel("đến:")); extraPanel.add(spTo);
                extraPanel.setVisible(true);
            }
        }
        extraPanel.revalidate();
        extraPanel.repaint();
    }

    private void applyPeriod() {
        String sel = (String) cboPeriod.getSelectedItem();
        LocalDate now = LocalDate.now();
        switch (sel) {
            case "Hôm nay"  -> { filterFrom = now; filterTo = now; }
            case "Tuần này" -> { filterFrom = now.minusDays(now.getDayOfWeek().getValue()-1); filterTo = now; }
            case "Tháng"    -> {
                int m = cboThang!=null ? (Integer)cboThang.getSelectedItem() : now.getMonthValue();
                int y = cboNam!=null   ? (Integer)cboNam.getSelectedItem()   : now.getYear();
                filterFrom = LocalDate.of(y,m,1);
                filterTo   = filterFrom.withDayOfMonth(filterFrom.lengthOfMonth());
            }
            case "Năm"      -> {
                int y = cboNamYear!=null ? (Integer)cboNamYear.getSelectedItem() : now.getYear();
                filterFrom = LocalDate.of(y,1,1); filterTo = LocalDate.of(y,12,31);
            }
            case "Tùy chọn" -> {
                JSpinner spFrom = (JSpinner) extraPanel.getClientProperty("spFrom");
                JSpinner spTo   = (JSpinner) extraPanel.getClientProperty("spTo");
                if (spFrom!=null) filterFrom = toLD(spFrom); else filterFrom = now;
                if (spTo!=null)   filterTo   = toLD(spTo);   else filterTo   = now;
            }
            default -> { filterFrom = now; filterTo = now; }
        }
        loadAll();
    }

    private LocalDate toLD(JSpinner sp) {
        return ((java.util.Date)sp.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    // =========================================================================
    private JPanel buildSummaryBar() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        row.add(buildStatCard("Tổng lợi nhuận", "0 đ", new Color(71, 71, 156), 0, false));
        cardVeBan = buildStatCard("Vé đã bán", "0 vé", new Color(34, 139, 87), 1, true);
        row.add(cardVeBan);
        cardVeHuy = buildStatCard("Vé đã hủy (đã hoàn tiền)", "0 vé", new Color(210, 50, 50), 2, true);
        row.add(cardVeHuy);
        row.add(buildStatCard("Tiền mặt", "0 đ", new Color(180, 120, 30), 3, false));
        row.add(buildStatCard("Chuyển khoản", "0 đ", new Color(30, 140, 160), 4, false));

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        pnl.add(row);
        return pnl;
    }

    private JPanel buildStatCard(String label, String value, Color accent, int idx, boolean clickable) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                boolean sel = (idx==1 && "ban".equals(currentFilter))
                           || (idx==2 && "huy".equals(currentFilter));
                g2.setColor(sel ? accent : new Color(220,224,232));
                g2.setStroke(new BasicStroke(sel ? 2f : 1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setColor(accent); g2.setStroke(new BasicStroke(1f));
                g2.fillRect(0,0,5,getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10,18,10,22));
        card.setPreferredSize(new Dimension(220,78));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,78));
        if (clickable) card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbLbl = new JLabel(label.toUpperCase());
        lbLbl.setFont(GuiTheme.font("Segoe UI",Font.BOLD,12));
        lbLbl.setForeground(new Color(130,135,155));

        JLabel lbVal = new JLabel(value);
        lbVal.setFont(GuiTheme.font("Segoe UI",Font.BOLD,23));
        lbVal.setForeground(new Color(28,32,52));
        lblKpi[idx] = lbVal;

        card.add(lbLbl); card.add(Box.createVerticalStrut(2)); card.add(lbVal);

        if (clickable) {
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (idx==1) {
                        currentFilter = "ban".equals(currentFilter) ? null : "ban";
                        renderTable("ban".equals(currentFilter) ? hdBanList : hdBanList);
                    } else {
                        if ("huy".equals(currentFilter)) { currentFilter=null; renderTable(hdBanList); }
                        else { currentFilter="huy"; renderTable(hdHuyList); }
                    }
                    if (cardVeBan!=null) cardVeBan.repaint();
                    if (cardVeHuy!=null) cardVeHuy.repaint();
                }
            });
        }
        return card;
    }

    private JPanel buildMiddleRow() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 395));

        pnl.add(buildTransactionTable(), BorderLayout.CENTER);

        donutChart = new DonutChartMgr();
        JPanel donutCard = wrapCard(donutChart, "Phân loại vé theo ghế");
        donutCard.setPreferredSize(new Dimension(300, 395));
        donutCard.setMinimumSize(new Dimension(300, 395));
        pnl.add(donutCard, BorderLayout.EAST);
        return pnl;
    }

    private JPanel buildTransactionTable() {
        tblModel = new DefaultTableModel(
            new Object[]{"Mã HĐ","Ngày giờ bán","Khách hàng","Hình thức TT","Số vé","Tổng tiền"},0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tbl = new JTable(tblModel);
        tbl.setRowHeight(36);
        tbl.setFont(GuiTheme.font("Segoe UI",Font.PLAIN,14));
        tbl.getTableHeader().setFont(GuiTheme.font("Segoe UI",Font.BOLD,14));
        tbl.setShowVerticalLines(false);
        tbl.setSelectionBackground(PRIMARY); tbl.setSelectionForeground(Color.WHITE);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);

        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable t,Object v,boolean s,boolean f,int row,int col){
                Component c=super.getTableCellRendererComponent(t,v,s,f,row,col);
                if(!s) c.setBackground(row%2==0?Color.WHITE:new Color(250,250,250));
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for(int i=0;i<tbl.getColumnCount();i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(zebra);
        ((DefaultTableCellRenderer)tbl.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        int[] colW={120,130,130,110,55,110};
        for(int i=0;i<colW.length;i++) tbl.getColumnModel().getColumn(i).setPreferredWidth(colW[i]);

        JScrollPane sp=new JScrollPane(tbl);
        sp.setBorder(null); sp.getViewport().setBackground(Color.WHITE);
        return wrapCard(sp,"Danh sách giao dịch");
    }

    private void renderTable(List<Object[]> list){
        tblModel.setRowCount(0);
        for(Object[] row:list){
            String tien=row[5] instanceof Double ? fmtMoney(((Double)row[5]).longValue()) : String.valueOf(row[5]);
            String pttt="huy".equals(currentFilter) ? "Hoàn tiền" : normPTTT(row[6]!=null?row[6].toString():"");
            tblModel.addRow(new Object[]{row[0],row[1],row[2],pttt,row[4],tien});
        }
    }
    // =========================================================================
    // BẢNG NHÂN VIÊN
    // =========================================================================
    private JPanel buildStaffCard(){
        staffModel=new DefaultTableModel(
            new Object[]{"Mã NV","Họ tên","Vé đã bán","Doanh thu"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tbl=new JTable(staffModel);
        tbl.setRowHeight(34);
        tbl.setFont(GuiTheme.font("Segoe UI",Font.PLAIN,13));
        tbl.getTableHeader().setFont(GuiTheme.font("Segoe UI",Font.BOLD,13));
        tbl.setShowVerticalLines(false); tbl.setGridColor(new Color(242,244,248));
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);

        DefaultTableCellRenderer r=new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                setHorizontalAlignment(col==1?SwingConstants.LEFT:SwingConstants.CENTER);
                boolean isTong=row==t.getRowCount()-1;
                if(!sel) setBackground(isTong?new Color(235,240,252):row%2==0?Color.WHITE:new Color(250,251,253));
                setFont(isTong?GuiTheme.font("Segoe UI",Font.BOLD,13):GuiTheme.font("Segoe UI",Font.PLAIN,13));
                setForeground(isTong?PRIMARY:new Color(42,45,66));
                return this;
            }
        };
        for(int i=0;i<tbl.getColumnCount();i++) tbl.getColumnModel().getColumn(i).setCellRenderer(r);
        ((DefaultTableCellRenderer)tbl.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(0).setMaxWidth(100);

        JScrollPane sp=new JScrollPane(tbl);
        sp.setBorder(null); sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(0,180));

        JPanel card=wrapCard(sp,"Doanh thu theo nhân viên");
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,240));
        return card;
    }

    // =========================================================================
    // BOTTOM BAR
    // =========================================================================
    private JPanel buildBottomBar(AppFrameManager appFrame){
        JPanel bar=new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        bar.setBorder(new EmptyBorder(8,0,0,16));

        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        right.setOpaque(false);

        JButton btnExport=buildNavyButton("Xuất báo cáo kết ca");
        btnExport.addActionListener(e->doExport());

        JButton btnDash=buildOutlineButton("Dashboard →");
        btnDash.addActionListener(e->{
            Window w=SwingUtilities.getWindowAncestor(this);
            if(w instanceof AppFrameManager) ((AppFrameManager)w).showCard("dashboard");
            else if(appFrame!=null) appFrame.showCard("dashboard");
        });

        right.add(btnExport); right.add(btnDash);
        bar.add(right,BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // LOAD DATA
    // =========================================================================
    private void loadAll() {
        LocalDate from = filterFrom != null ? filterFrom : LocalDate.now();
        LocalDate to   = filterTo   != null ? filterTo   : LocalDate.now();

        new Thread(() -> {
            try {
                // Dùng đúng DAO như nhân viên
                List<Object[]> banList  = HoaDonDAO.getDanhSachHoaDonTheoKhoang(from, to);
                List<Object[]> huyList  = HoaDonDAO.getDanhSachHoaDonHuyTheoKhoang(from, to);
                List<Object[]> staffList= HoaDonDAO.getDoanhThuNhanVienTheoKhoang(from, to);

                int    vb   = VeDAO.getSoVeTheoKhoang(from, to, "Đã thanh toán");
                int    vh   = VeDAO.getSoVeHuyTheoKhoang(from, to);
                int[]  ghe  = VeDAO.getSoGheTheoLoaiTheoKhoang(from, to);
                long[] pttt = VeDAO.getDoanhThuPTTTTheoKhoang(from, to);

                // Lợi nhuận = tổng HĐ bán + phí phạt HĐ hủy (hoàn tiền)
                long ln = 0;
                for (Object[] row : banList) ln += ((Double) row[5]).longValue();
                long phiPhat = 0;
                for (Object[] row : huyList) phiPhat += ((Double) row[5]).longValue();
                final long loiFinal = ln + phiPhat;
                final long tmF = pttt[0], ckF = pttt[1];
                final int vbF = vb, vhF = vh;

                SwingUtilities.invokeLater(() -> {
                    kLoiNhuan = loiFinal; kVeBan = vbF; kVeHuy = vhF;
                    kTienMat  = tmF;      kCK    = ckF;

                    lblKpi[0].setText(fmtMoney(loiFinal));
                    lblKpi[1].setText(vbF + " vé");
                    lblKpi[2].setText(vhF + " vé");
                    if (lblKpi[3] != null) lblKpi[3].setText(fmtMoney(tmF));
                    if (lblKpi[4] != null) lblKpi[4].setText(fmtMoney(ckF));

                    hdBanList = banList; hdHuyList = huyList;
                    currentFilter = null;
                    if (cardVeBan != null) cardVeBan.repaint();
                    if (cardVeHuy != null) cardVeHuy.repaint();
                    renderTable(hdBanList);

                    donutChart.setData(ghe[0], ghe[1], ghe[2]);

                    staffModel.setRowCount(0);
                    long tongDoanhThuNV = 0;
                    for (Object[] row : staffList) {
                        long dt = ((Number) row[3]).longValue();
                        tongDoanhThuNV += dt;
                        staffModel.addRow(new Object[]{
                            row[0], row[1], row[2] + " vé",
                            fmtMoney(dt)
                        });
                    }
                    staffModel.addRow(new Object[]{"", "TỔNG CỘNG", vbF + " vé", fmtMoney(tongDoanhThuNV)});
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

 




    /**
     * [0]=maHD [1]=gioBan [2]=tenKH [3]=loaiGhe/note [4]=soVe [5]=tongTien(Double) [6]=pttt
     * Nếu huy=true: tongTien = phiPhat từ cột HoaDon.phiPhat
     * (đổi tên cột nếu DB của bạn khác)
     */




    // =========================================================================
    // EXPORT PDF
    // =========================================================================
    private void doExport(){
        int[] d=donutChart.getData();
        String strFrom=filterFrom!=null?filterFrom.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")):"";
        String strTo  =filterTo  !=null?filterTo  .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")):"";

        // Gom data nhân viên (bỏ dòng TỔNG CỘNG cuối)
        List<Object[]> staffRows=new ArrayList<>();
        for(int i=0;i<staffModel.getRowCount()-1;i++)
            staffRows.add(new Object[]{
                staffModel.getValueAt(i,0), staffModel.getValueAt(i,1),
                staffModel.getValueAt(i,2), staffModel.getValueAt(i,3)
            });

        BaoCaoPDF.exportManager("Quản lý",strFrom,strTo,
                kLoiNhuan,kVeBan,kVeHuy,kTienMat,kCK,
                d[1],d[2],d[0],staffRows);
    }

    // =========================================================================
    // HELPERS UI
    private JPanel wrapCard(Component inner,String title){
        JPanel card=new JPanel(new BorderLayout(0,8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C,1,true),new EmptyBorder(12,14,10,14)));
        if(title!=null && !title.isEmpty()){
            JLabel lbT=new JLabel(title);
            lbT.setFont(GuiTheme.font("Segoe UI",Font.BOLD,13));
            lbT.setForeground(new Color(60,65,90));
            card.add(lbT,BorderLayout.NORTH);
        }
        card.add(inner,BorderLayout.CENTER);
        return card;
    }

    private JButton buildNavyButton(String text){
        JButton btn=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()?PRIMARY.darker():PRIMARY);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),15,15);
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI",Font.BOLD,13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);btn.setBorderPainted(false);btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Dimension pref=btn.getPreferredSize();
        btn.setPreferredSize(new Dimension(pref.width+30,34));
        return btn;
    }

    private JButton buildOutlineButton(String text){
        Color navy=new Color(37,69,121);
        JButton btn=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()?navy:Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(navy);g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setColor(getModel().isPressed()?Color.WHITE:navy);
                g2.setFont(getFont());FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI",Font.BOLD,12));
        btn.setContentAreaFilled(false);btn.setBorderPainted(false);btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130,34));
        return btn;
    }

    private JLabel makeLabel(String text){
        JLabel l=new JLabel(text);
        l.setFont(GuiTheme.font("Segoe UI",Font.BOLD,12));
        l.setForeground(new Color(70,75,100));
        return l;
    }

    private JComboBox<Integer> makeIntCombo(Integer[] items,int width){
        JComboBox<Integer> cb=new JComboBox<>(items);
        cb.setFont(GuiTheme.font("Segoe UI",Font.PLAIN,12));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(width,30));
        return cb;
    }

    private JSpinner makeDateSpinner(LocalDate init){
        java.util.Date d=java.util.Date.from(init.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        JSpinner sp=new JSpinner(new SpinnerDateModel(d,null,null,java.util.Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp,"dd/MM/yyyy"));
        sp.setFont(GuiTheme.font("Segoe UI",Font.PLAIN,12));
        sp.setPreferredSize(new Dimension(110,30));
        return sp;
    }

    private Integer[] yearArray(){
        int y=LocalDate.now().getYear();
        return new Integer[]{y,y-1,y-2,y-3,y-4,y-5};
    }

    private static String fmtMoney(long v){return String.format("%,d đ",v).replace(",",".");}
    private static String normPTTT(String s){
        if(s.equalsIgnoreCase("TIEN_MAT")) return "Tiền mặt";
        if(s.equalsIgnoreCase("CHUYEN_KHOAN")||s.toLowerCase().contains("vietqr")) return "Chuyển khoản";
        if(s.toLowerCase().contains("hoàn tiền")) return "Hoàn tiền";
        if(s.equalsIgnoreCase("LUU_TAM")) return "Lưu tạm";
        return s;
    }
    private static boolean isCK(String s){
        if(s==null)return false;String l=s.toLowerCase();
        return l.contains("chuyen_khoan")||l.contains("chuyển khoản")||l.contains("vietqr");
    }
}

// =============================================================================
// DONUT CHART (copy từ ThongKeGUI, không đổi)
// =============================================================================
class DonutChartMgr extends JPanel {
    private static final Color[] COLORS={new Color(88,130,210),new Color(60,179,113),new Color(255,165,50)};
    private static final String[] LABELS={"Ghế cứng","Giường nằm","Ghế mềm"};
    private int[] data={0,0,0};

    public DonutChartMgr(){setBackground(Color.WHITE);}
    public void setData(int gc,int gn,int gm){data[0]=gc;data[1]=gn;data[2]=gm;repaint();}
    public int[] getData(){return data;}

    @Override protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int W=getWidth(),H=getHeight();
        int R=Math.min(W,H-140)/2-6,r=(int)(R*0.55);
        int cx=W/2,cy=R+16;
        int total=data[0]+data[1]+data[2];
        int sa=90;
        for(int i=0;i<3;i++){
            int arc=total==0?120:(i==2
                ?360-(int)Math.round(360.0*data[0]/total)-(int)Math.round(360.0*data[1]/total)
                :(int)Math.round(360.0*data[i]/total));
            g2.setColor(COLORS[i]);
            g2.fillArc(cx-R,cy-R,R*2,R*2,sa,arc);
            sa+=arc;
        }
        g2.setColor(Color.WHITE); g2.fillOval(cx-r,cy-r,r*2,r*2);
        // Tổng giữa donut
        String tot=String.valueOf(total);
        g2.setFont(GuiTheme.font("Segoe UI",Font.BOLD,16));
        g2.setColor(new Color(28,32,52));
        g2.drawString(tot,cx-g2.getFontMetrics().stringWidth(tot)/2,cy+5);
        g2.setFont(GuiTheme.font("Segoe UI",Font.PLAIN,10));
        g2.setColor(new Color(130,135,155));
        g2.drawString("vé",cx-g2.getFontMetrics().stringWidth("vé")/2,cy+17);
        // Legend
        int legendY=cy+R+18,step=30;
        for(int i=0;i<3;i++){
            double pct=total==0?0:data[i]*100.0/total;
            int ly=legendY+i*step;
            g2.setColor(COLORS[i]); g2.fillRoundRect(16,ly,12,12,4,4);
            g2.setColor(new Color(50,55,75));
            g2.setFont(GuiTheme.font("Segoe UI",Font.BOLD,12));
            g2.drawString(LABELS[i],34,ly+11);
            g2.setColor(new Color(120,125,145));
            g2.setFont(GuiTheme.font("Segoe UI",Font.PLAIN,11));
            g2.drawString(String.format("%.1f%% (%d vé)",pct,data[i]),34,ly+23);
        }
        g2.dispose();
    }
}