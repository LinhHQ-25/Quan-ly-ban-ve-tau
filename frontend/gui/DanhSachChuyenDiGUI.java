package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import connect_DB.Connect_DB;

final class DanhSachChuyenDiGUI extends JPanel {
    private static final Color BORDER = GuiTheme.SEARCH_FIELD_BORDER;
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JComboBox<String> cboGaDi, cboGaDen, cboTau;
    private JDateChooser dcNgayDi;
    private JSpinner spnSeat;
    private JRadioButton rdoThuong, rdoVip;

    private SmartFilterCard cardHnay;
    private SmartFilterCard cardSapChay;
    private SmartFilterCard cardTre;

    DanhSachChuyenDiGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BorderLayout(0, 12));
        pnlPage.setBorder(new EmptyBorder(
            0,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 15));
        pnlTop.setOpaque(false);
        pnlTop.add(buildSmartFilters(), BorderLayout.NORTH);
        pnlTop.add(buildFilterPanel(), BorderLayout.CENTER);

        pnlPage.add(pnlTop, BorderLayout.NORTH);
        pnlPage.add(buildTablePanel(), BorderLayout.CENTER);

        add(pnlPage, BorderLayout.CENTER);
        
        initFilterData();
        loadDataToTable();
        updateSmartFilters();
    }

    private void initFilterData() {
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        try {
            // Load Ga
            String sqlGa = "SELECT tenGa FROM Ga";
            ResultSet rsGa = conn.createStatement().executeQuery(sqlGa);
            cboGaDi.removeAllItems();
            cboGaDen.removeAllItems();
            cboGaDi.addItem("");
            cboGaDen.addItem("");
            while (rsGa.next()) {
                String name = rsGa.getString("tenGa");
                cboGaDi.addItem(name);
                cboGaDen.addItem(name);
            }

            // Load Tau
            String sqlTau = "SELECT tenTau FROM Tau";
            ResultSet rsTau = conn.createStatement().executeQuery(sqlTau);
            cboTau.removeAllItems();
            cboTau.addItem("");
            while (rsTau.next()) {
                cboTau.addItem(rsTau.getString("tenTau"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel buildFilterPanel() {
        RoundedShadowPanel pnlOuter = new RoundedShadowPanel();
        pnlOuter.setLayout(new BorderLayout(0, 5));
        
        // Tiêu đề
        JLabel lblTitle = new JLabel("Thông tin tra cứu");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(GuiTheme.TEXT);
        lblTitle.setBorder(new EmptyBorder(10, 15, 0, 15));
        lblTitle.setIcon(GuiIcons.loadIcon(DanhSachChuyenDiGUI.class, "filter", 18, 18));
        lblTitle.setIconTextGap(8);
        pnlOuter.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        cboGaDi = buildCombo();
        gbc.gridx = 0; pnlGrid.add(buildField("Ga đi:", cboGaDi), gbc);
        
        cboGaDen = buildCombo();
        gbc.gridx = 1; pnlGrid.add(buildField("Ga đến:", cboGaDen), gbc);
        
        dcNgayDi = buildDateField();
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày đi:", dcNgayDi), gbc);
        
        gbc.gridy = 1;
        cboTau = buildCombo();
        gbc.gridx = 0; pnlGrid.add(buildField("Mã /Tên tàu:", cboTau), gbc);

        spnSeat = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        spnSeat.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        spnSeat.setPreferredSize(new Dimension(160, 32));
        spnSeat.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        gbc.gridx = 1; pnlGrid.add(buildField("Ghế trống tối thiểu:", spnSeat), gbc);
        
        gbc.gridx = 2;
        pnlGrid.add(buildTypeBlock(), gbc);
        
        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        JPanel pnlAction = buildActionBlock();
        pnlAction.setOpaque(false);
        pnlOuter.add(pnlAction, BorderLayout.SOUTH);
        
        return pnlOuter;
    }

    private JPanel buildSmartFilters() {
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlFilters.setOpaque(false);

        cardHnay = new SmartFilterCard("Khởi hành hôm nay", "0", new Color(40, 167, 69)); // Xanh lá
        cardSapChay = new SmartFilterCard("Sắp xuất phát < 2h", "0", new Color(220, 53, 69)); // Đỏ
        cardTre = new SmartFilterCard("Bị trễ - Delayed", "0", new Color(253, 126, 20)); // Cam đỏ

        pnlFilters.add(cardHnay);
        pnlFilters.add(cardSapChay);
        pnlFilters.add(cardTre);
        return pnlFilters;
    }

    private JPanel buildField(String label, Component comp) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 4));
        pnlField.setOpaque(false);
        JLabel lbField = new JLabel(label);
        lbField.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lbField.setForeground(GuiTheme.NAVY);
        pnlField.add(lbField, BorderLayout.NORTH);
        pnlField.add(comp, BorderLayout.CENTER);
        return pnlField;
    }

    private JComboBox<String> buildCombo(String... values) {
        JComboBox<String> cmb = new JComboBox<>(values);
        cmb.setEditable(true);
        cmb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cmb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cmb.setForeground(GuiTheme.TEXT);
        cmb.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        cmb.setPreferredSize(new Dimension(160, 32));
        return cmb;
    }

    private JDateChooser buildDateField() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setPreferredSize(new Dimension(160, 32));
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(GuiTheme.SEARCH_FIELD_BG);
        dc.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        return dc;
    }

    private JPanel buildTypeBlock() {
        JPanel pnlBlock = new JPanel(new BorderLayout(0, 4));
        pnlBlock.setOpaque(false);
        JLabel lbBlock = new JLabel("Loại toa");
        lbBlock.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lbBlock.setForeground(GuiTheme.NAVY);
        JPanel pnlOptions = new JPanel();
        pnlOptions.setOpaque(false);
        pnlOptions.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        ButtonGroup grp = new ButtonGroup();
        rdoThuong = buildRadio("Toa thường");
        rdoVip = buildRadio("Toa Vip");
        grp.add(rdoThuong);
        grp.add(rdoVip);
        rdoThuong.setSelected(true);
        pnlOptions.add(rdoThuong);
        pnlOptions.add(rdoVip);
        pnlBlock.add(lbBlock, BorderLayout.NORTH);
        pnlBlock.add(pnlOptions, BorderLayout.CENTER);
        return pnlBlock;
    }

    private JPanel buildActionBlock() {
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlButtons.setOpaque(false);
        pnlButtons.setBorder(new EmptyBorder(0, 0, 5, 0));

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);

        btnSearch.addActionListener(e -> loadDataToTable());

        btnReset.addActionListener(e -> {
            cboGaDi.setSelectedIndex(0);
            cboGaDen.setSelectedIndex(0);
            cboTau.setSelectedIndex(0);
            dcNgayDi.setDate(null);
            spnSeat.setValue(0);
            rdoThuong.setSelected(true);
            loadDataToTable();
        });

        pnlButtons.add(btnSearch);
        pnlButtons.add(btnReset);
        return pnlButtons;
    }

    private JRadioButton buildRadio(String text) {
        JRadioButton rdo = new JRadioButton(text);
        rdo.setOpaque(false);
        rdo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        rdo.setForeground(GuiTheme.TEXT);
        return rdo;
    }

    private JButton buildNavyButton(String text, Color baseColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? baseColor.darker()
                    : getModel().isRollover() ? hoverColor : baseColor);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(130, 36));
        btn.setMaximumSize(new Dimension(130, 36));
        btn.setForeground(Color.WHITE);
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(DanhSachChuyenDiGUI.class, "search", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(DanhSachChuyenDiGUI.class, "reset", 16, 16));
        
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
        pnlOuter.setOpaque(false);
        pnlOuter.add(buildSectionTitle("Danh sách chuyến đi"), BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
            new Object[] { "STT", "Ga đi", "Ga đến", "Ngày đi", "Ngày đến", "Giờ đi - Giờ đến", "Tàu", "isDelayed" },
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblData = new JTable(tblModel);
        tblData.setRowHeight(32);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.setForeground(GuiTheme.TEXT);
        
        // Ẩn grid dọc, dùng grid ngang mờ
        tblData.setShowVerticalLines(false);
        tblData.setShowHorizontalLines(true);
        tblData.setGridColor(new Color(240, 240, 240));
        tblData.setIntercellSpacing(new Dimension(0, 1));
        
        tblData.setSelectionBackground(new Color(230, 242, 255)); // Highlight xanh nhạt
        tblData.setSelectionForeground(GuiTheme.NAVY);
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblData.getTableHeader().setBackground(Color.WHITE);
        tblData.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        // Hide "isDelayed" column
        tblData.getColumnModel().removeColumn(tblData.getColumnModel().getColumn(7));

        // Zebra striping and delayed highlight
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                boolean isDelayed = (Boolean) table.getModel().getValueAt(table.convertRowIndexToModel(row), 7);

                if (isSelected) {
                    c.setBackground(new Color(230, 242, 255));
                    c.setForeground(GuiTheme.NAVY);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    if (isDelayed) {
                        c.setForeground(new Color(253, 126, 20)); // Cam đỏ
                        c.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setForeground(GuiTheme.TEXT);
                        c.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < tblData.getColumnCount(); i++) {
            tblData.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
        ((DefaultTableCellRenderer)tblData.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        
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
        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private void loadDataToTable() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT ct.maChuyen, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
                         "ct.thoiGianKhoiHanh, ct.thoiGianDuKien, t.tenTau " +
                         "FROM ChuyenTau ct " +
                         "JOIN Ga gDi ON ct.gaDi = gDi.maGa " +
                         "JOIN Ga gDen ON ct.gaDen = gDen.maGa " +
                         "JOIN Tau t ON ct.maTau = t.maTau " +
                         "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? AND t.tenTau LIKE ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + (cboGaDi.getSelectedItem() != null ? cboGaDi.getSelectedItem().toString() : "") + "%");
            stmt.setString(2, "%" + (cboGaDen.getSelectedItem() != null ? cboGaDen.getSelectedItem().toString() : "") + "%");
            stmt.setString(3, "%" + (cboTau.getSelectedItem() != null ? cboTau.getSelectedItem().toString() : "") + "%");

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            
            while (rs.next()) {
                java.sql.Timestamp khoiHanh = rs.getTimestamp("thoiGianKhoiHanh");
                java.sql.Timestamp denDuKien = rs.getTimestamp("thoiGianDuKien");
                
                String ngayDi = khoiHanh != null ? sdfDate.format(khoiHanh) : "";
                String ngayDen = denDuKien != null ? sdfDate.format(denDuKien) : "";
                String gioDiGioDen = (khoiHanh != null ? sdfTime.format(khoiHanh) : "") + " - " + 
                                     (denDuKien != null ? sdfTime.format(denDuKien) : "");

                boolean isDelayed = false;
                if (khoiHanh != null && denDuKien != null) {
                    if (khoiHanh.after(denDuKien)) {
                        isDelayed = true;
                    }
                }

                tblModel.addRow(new Object[] {
                    stt++,
                    rs.getString("gaDi"),
                    rs.getString("gaDen"),
                    ngayDi,
                    ngayDen,
                    gioDiGioDen,
                    rs.getString("tenTau"),
                    isDelayed
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSmartFilters() {
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        try {
            // Hôm nay
            String sql1 = "SELECT COUNT(*) FROM ChuyenTau WHERE CAST(thoiGianKhoiHanh AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement pst = conn.prepareStatement(sql1); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardHnay.setCount(String.valueOf(rs.getInt(1)));
            }
            
            // Sắp chạy < 2h
            String sql2 = "SELECT COUNT(*) FROM ChuyenTau WHERE thoiGianKhoiHanh BETWEEN GETDATE() AND DATEADD(hour, 2, GETDATE())";
            try (PreparedStatement pst = conn.prepareStatement(sql2); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardSapChay.setCount(String.valueOf(rs.getInt(1)));
            }
            
            // Bị trễ (thoiGianKhoiHanh > thoiGianDuKien)
            String sql3 = "SELECT COUNT(*) FROM ChuyenTau WHERE thoiGianKhoiHanh > thoiGianDuKien";
            try (PreparedStatement pst = conn.prepareStatement(sql3); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardTre.setCount(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel buildSectionTitle(String title) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel lb = new JLabel(title);
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lb.setForeground(Color.WHITE);
        lb.setOpaque(true);
        lb.setBackground(PRIMARY);
        lb.setBorder(new EmptyBorder(6, 12, 6, 12));
        pnl.add(lb);
        return pnl;
    }
}
