package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
    private SmartFilterCard cardHnay;
    private SmartFilterCard cardSapChay;
    private SmartFilterCard cardTre;
    private String activeCard = "ALL";

    DanhSachChuyenDiGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BorderLayout(0, 4));
        pnlPage.setBorder(new EmptyBorder(
            -9,
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
        JPanel pnlSection = new JPanel(new BorderLayout(0, 5));
        pnlSection.setOpaque(false);
        pnlSection.add(buildSectionTitle("Thông tin tra cứu"), BorderLayout.NORTH);

        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 5));
        pnlOuter.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        cboGaDi = buildCombo();
        gbc.gridx = 0; pnlGrid.add(buildField("Ga đi:", cboGaDi), gbc);
        
        cboGaDen = buildCombo();
        gbc.gridx = 1; pnlGrid.add(buildField("Ga đến:", cboGaDen), gbc);
        
        dcNgayDi = buildDateField();
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày đi:", dcNgayDi), gbc);
        
        cboTau = buildCombo();
        gbc.gridx = 3; pnlGrid.add(buildField("Mã /Tên tàu:", cboTau), gbc);
        
        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        pnlSection.add(pnlOuter, BorderLayout.CENTER);
        return pnlSection;
    }

    private JPanel buildSmartFilters() {
        JPanel pnlFilters = new JPanel(new GridLayout(1, 0, 15, 0));
        pnlFilters.setOpaque(false);

        cardHnay = new SmartFilterCard("Khởi hành hôm nay", "0", new Color(40, 167, 69));
        cardSapChay = new SmartFilterCard("Sắp xuất phát < 2h", "0", new Color(220, 53, 69));
        cardTre = new SmartFilterCard("Bị trễ - Delayed", "0", new Color(253, 126, 20));

        cardHnay.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeCard = "HN"; loadDataToTable();
            }
        });
        cardSapChay.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeCard = "SC"; loadDataToTable();
            }
        });
        cardTre.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeCard = "TR"; loadDataToTable();
            }
        });

        pnlFilters.add(cardHnay);
        pnlFilters.add(cardSapChay);
        pnlFilters.add(cardTre);

        return pnlFilters;
    }

    private JPanel buildField(String label, Component comp) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 4));
        pnlField.setOpaque(false);
        JLabel lbField = new JLabel(label);
        lbField.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
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
        GuiTheme.setupRoundedComponent(cmb);
        cmb.setPreferredSize(new Dimension(128, 30));
        return cmb;
    }

    private JDateChooser buildDateField() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setPreferredSize(new Dimension(128, 30));
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(GuiTheme.SEARCH_FIELD_BG);
        GuiTheme.setupRoundedComponent(dc);
        return dc;
    }



    private JPanel buildActionBlock() {
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.setBorder(new EmptyBorder(0, 0, 0, 0));

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);

        btnSearch.addActionListener(e -> loadDataToTable());

        btnReset.addActionListener(e -> {
            cboGaDi.setSelectedIndex(0);
            cboGaDen.setSelectedIndex(0);
            cboTau.setSelectedIndex(0);
            dcNgayDi.setDate(null);
            activeCard = "ALL";
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
        btn.setPreferredSize(new Dimension(120, 30));
        btn.setMaximumSize(new Dimension(120, 30));
        btn.setForeground(Color.WHITE);
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(DanhSachChuyenDiGUI.class, "/Images/traCuu.png", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(DanhSachChuyenDiGUI.class, "/Images/logoLammoi.png", 16, 16));
        
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
        pnlOuter.setOpaque(false);
        JPanel pnlActions = buildActionBlock();
        pnlOuter.add(buildSectionTitleWithActions("Danh sách chuyến đi", pnlActions), BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
            new Object[] { "STT", "Ga đi", "Ga đến", "Ngày đi", "Ngày đến", "Giờ đi - Giờ đến", "Tàu", "isDelayed" },
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblData = new JTable(tblModel);
        tblData.setRowHeight(36);
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
            String sql = "SELECT ct.maChuyenTau, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
                         "dt.thoiGianKhoiHanh, dt.thoiGianDuKien, t.tenTau " +
                         "FROM ChuyenTau ct " +
                         "JOIN ChiTietChuyenTau dt ON ct.maChuyenTau = dt.maChuyenTau " +
                         "JOIN Ga gDi ON dt.maGaDi = gDi.maGa " +
                         "JOIN Ga gDen ON dt.maGaDen = gDen.maGa " +
                         "JOIN Tau t ON ct.maTau = t.maTau " +
                         "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? AND t.tenTau LIKE ?";
            
            if (dcNgayDi.getDate() != null) {
                sql += " AND CAST(dt.thoiGianKhoiHanh AS DATE) = ?";
            }
            
            if (activeCard.equals("HN")) {
                sql += " AND CAST(dt.thoiGianKhoiHanh AS DATE) = CAST(GETDATE() AS DATE)";
            } else if (activeCard.equals("SC")) {
                sql += " AND dt.thoiGianKhoiHanh > GETDATE() AND dt.thoiGianKhoiHanh <= DATEADD(hour, 2, GETDATE())";
            } else if (activeCard.equals("TR")) {
                sql += " AND ct.trangThai = N'Bị trễ'";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + (cboGaDi.getSelectedItem() != null ? cboGaDi.getSelectedItem().toString() : "") + "%");
            stmt.setString(2, "%" + (cboGaDen.getSelectedItem() != null ? cboGaDen.getSelectedItem().toString() : "") + "%");
            stmt.setString(3, "%" + (cboTau.getSelectedItem() != null ? cboTau.getSelectedItem().toString() : "") + "%");

            if (dcNgayDi.getDate() != null) {
                stmt.setDate(4, new java.sql.Date(dcNgayDi.getDate().getTime()));
            }

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
            String sql1 = "SELECT COUNT(*) FROM ChiTietChuyenTau WHERE CAST(thoiGianKhoiHanh AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement pst = conn.prepareStatement(sql1); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardHnay.setCount(String.valueOf(rs.getInt(1)));
            }
            
            // Sắp chạy < 2h
            String sql2 = "SELECT COUNT(*) FROM ChiTietChuyenTau WHERE thoiGianKhoiHanh BETWEEN GETDATE() AND DATEADD(hour, 2, GETDATE())";
            try (PreparedStatement pst = conn.prepareStatement(sql2); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardSapChay.setCount(String.valueOf(rs.getInt(1)));
            }
            
            // Bị trễ (thoiGianKhoiHanh > thoiGianDuKien)
            String sql3 = "SELECT COUNT(*) FROM ChiTietChuyenTau WHERE thoiGianKhoiHanh > thoiGianDuKien";
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
        JLabel lb = new JLabel(title, SwingConstants.CENTER);
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lb.setForeground(Color.WHITE);
        lb.setOpaque(true);
        lb.setBackground(PRIMARY);
        lb.setPreferredSize(new Dimension(220, 26));
        lb.setBorder(null);
        pnl.add(lb);
        return pnl;
    }

    private JPanel buildSectionTitleWithActions(String title, JPanel pnlActions) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        JLabel lb = new JLabel(title, SwingConstants.CENTER);
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lb.setForeground(Color.WHITE);
        lb.setOpaque(true);
        lb.setBackground(PRIMARY);
        lb.setPreferredSize(new Dimension(220, 26));
        lb.setBorder(null);
        
        pnl.add(lb, BorderLayout.WEST);
        if (pnlActions != null) {
            pnl.add(pnlActions, BorderLayout.EAST);
        }
        return pnl;
    }

    private static final class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color stroke;
        private final float strokeWidth;

        private RoundedPanel(int arc, Color fill, Color stroke, float strokeWidth) {
            this.arc = arc;
            this.fill = fill;
            this.stroke = stroke;
            this.strokeWidth = strokeWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(stroke);
            g2.setStroke(new java.awt.BasicStroke(strokeWidth));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
