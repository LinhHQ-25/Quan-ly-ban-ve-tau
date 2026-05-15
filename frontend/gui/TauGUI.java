package gui;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import connect_DB.Connect_DB;

final class TauGUI extends JPanel {
    private static final Color BORDER = GuiTheme.SEARCH_FIELD_BORDER;
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JTextField txtMaTau, txtTenTau, txtMinToa, txtMinGhe;
    private JComboBox<String> cboStatus;

    TauGUI() {
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

        pnlPage.add(buildFilterPanel(), BorderLayout.NORTH);
        
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 12));
        pnlCenter.setOpaque(false);
        pnlCenter.add(buildIdleTrainsSection(), BorderLayout.NORTH);
        pnlCenter.add(buildTablePanel(), BorderLayout.CENTER);
        
        pnlPage.add(pnlCenter, BorderLayout.CENTER);

        add(pnlPage, BorderLayout.CENTER);
        
        loadDataToTable();
        loadIdleTrains();
    }

    private JPanel pnlIdleCards;
    private JPanel buildIdleTrainsSection() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitle.setOpaque(false);
        JLabel lbl = new JLabel("Tàu tại ga (Đang rãnh)");
        lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(GuiTheme.TEXT);
        pnlTitle.add(lbl);
        pnl.add(pnlTitle, BorderLayout.NORTH);

        pnlIdleCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlIdleCards.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(pnlIdleCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 160));
        scroll.getHorizontalScrollBar().setUnitIncrement(15);
        
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private void loadIdleTrains() {
        if (pnlIdleCards == null) return;
        pnlIdleCards.removeAll();
        
        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            // Lấy các tàu không có chuyến nào trong tương lai
            String sql = "SELECT t.* FROM Tau t WHERE t.trangThai = N'Đang hoạt động' " +
                         "AND NOT EXISTS (SELECT 1 FROM ChuyenTau ct JOIN ChiTietChuyenTau cct ON ct.maChuyenTau = cct.maChuyenTau " +
                         "WHERE ct.maTau = t.maTau AND cct.thoiGianKhoiHanh > GETDATE())";
            
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                boolean hasIdle = false;
                while (rs.next()) {
                    hasIdle = true;
                    pnlIdleCards.add(new TrainCard(
                        rs.getString("maTau"),
                        rs.getString("tenTau"),
                        rs.getInt("soToa"),
                        rs.getInt("tongSoGhe")
                    ));
                }
                if (!hasIdle) {
                    JLabel lbl = new JLabel("Hiện không có tàu nào đang rãnh.");
                    lbl.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 13));
                    lbl.setForeground(GuiTheme.SUB_TEXT);
                    pnlIdleCards.add(lbl);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        pnlIdleCards.revalidate();
        pnlIdleCards.repaint();
    }

    private JPanel buildFilterPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 5));
        
        JLabel lblTitle = new JLabel("Thông tin tra cứu");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(GuiTheme.TEXT);
        lblTitle.setBorder(new EmptyBorder(10, 15, 0, 15));
        lblTitle.setIcon(GuiIcons.loadIcon(TauGUI.class, "filter", 18, 18));
        lblTitle.setIconTextGap(8);
        pnlOuter.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        txtMaTau = buildTextField(160);
        gbc.gridx = 0; pnlGrid.add(buildField("Mã tàu:", txtMaTau), gbc);
        
        txtTenTau = buildTextField(160);
        gbc.gridx = 1; pnlGrid.add(buildField("Tên tàu:", txtTenTau), gbc);
        
        cboStatus = buildStatusCombo();
        gbc.gridx = 2; pnlGrid.add(buildField("Trạng thái:", cboStatus), gbc);

        gbc.gridy = 1;
        txtMinToa = buildTextField(160);
        gbc.gridx = 0; pnlGrid.add(buildField("Số toa tối thiểu:", txtMinToa), gbc);
        
        txtMinGhe = buildTextField(160);
        gbc.gridx = 1; pnlGrid.add(buildField("Số ghế tối thiểu:", txtMinGhe), gbc);

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        JPanel pnlAction = buildActionBlock();
        pnlAction.setOpaque(false);
        pnlOuter.add(pnlAction, BorderLayout.SOUTH);
        
        return pnlOuter;
    }

    private JPanel buildField(String label, Component comp) {
        JPanel pnlField = new JPanel(new BorderLayout(8, 0));
        pnlField.setOpaque(false);
        JLabel lbField = new JLabel(label);
        lbField.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbField.setForeground(GuiTheme.NAVY);
        lbField.setPreferredSize(new Dimension(120, 30));
        pnlField.add(lbField, BorderLayout.WEST);
        pnlField.add(comp, BorderLayout.CENTER);
        return pnlField;
    }

    private JTextField buildTextField(int width) {
        JTextField txtField = new JTextField();
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setForeground(GuiTheme.TEXT);
        txtField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true),
            new EmptyBorder(2, 6, 2, 6)
        ));
        txtField.setPreferredSize(new Dimension(104, 30));
        return txtField;
    }

    private JComboBox<String> buildStatusCombo() {
        JComboBox<String> cbo = new JComboBox<>(new String[] { "", "Hoạt động", "Bảo trì" });
        cbo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cbo.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cbo.setForeground(GuiTheme.TEXT);
        cbo.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        cbo.setPreferredSize(new Dimension(104, 30));
        return cbo;
    }

    private JPanel buildActionBlock() {
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlButtons.setOpaque(false);
        pnlButtons.setBorder(new EmptyBorder(0, 0, 5, 0));

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);

        btnSearch.addActionListener(e -> loadDataToTable());
        btnReset.addActionListener(e -> {
            txtMaTau.setText(""); txtTenTau.setText("");
            txtMinToa.setText(""); txtMinGhe.setText("");
            cboStatus.setSelectedIndex(0);
            loadDataToTable();
        });

        pnlButtons.add(btnSearch);
        pnlButtons.add(btnReset);
        return pnlButtons;
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
        btn.setForeground(Color.WHITE);
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(TauGUI.class, "search", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(TauGUI.class, "reset", 16, 16));
        
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlWrap = new JPanel(new BorderLayout(0, 8));
        pnlWrap.setOpaque(false);
        pnlWrap.add(buildSectionTitle("Danh sách tàu"), BorderLayout.NORTH);

        String[] cols = {"STT", "Mã tàu", "Tên tàu", "Số toa", "Tổng số ghế", "Trạng thái", "Ghi chú"};
        tblModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblData = new JTable(tblModel);
        tblData.setRowHeight(32);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblData.setShowVerticalLines(false);
        tblData.setSelectionBackground(new Color(232, 240, 254));
        tblData.setSelectionForeground(GuiTheme.TEXT);
        
        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, row, col);
                if (!s) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    c.setForeground(GuiTheme.TEXT);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };

        for (int i = 0; i < tblData.getColumnCount(); i++) {
            tblData.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
        }
        ((DefaultTableCellRenderer)tblData.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);

        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 7) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(50);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(200);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(80);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(200);
            }
        });

        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    private void loadDataToTable() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT * FROM Tau WHERE maTau LIKE ? AND tenTau LIKE ?";
            String statusFilter = (String) cboStatus.getSelectedItem();
            if (statusFilter != null && !statusFilter.isEmpty()) {
                sql += " AND trangThai = ?";
            }
            
            String minToa = txtMinToa.getText().trim();
            if (!minToa.isEmpty()) sql += " AND soToa >= " + minToa;
            
            String minGhe = txtMinGhe.getText().trim();
            if (!minGhe.isEmpty()) sql += " AND tongSoGhe >= " + minGhe;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaTau.getText().trim() + "%");
            stmt.setString(2, "%" + txtTenTau.getText().trim() + "%");
            if (statusFilter != null && !statusFilter.isEmpty()) {
                // Sync with localized status in DB
                String dbStatus = statusFilter.equals("Hoạt động") ? "Đang hoạt động" : "Bảo trì";
                stmt.setString(3, dbStatus);
            }

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            boolean hasGhiChu = false;
            try {
                rs.findColumn("ghiChu");
                hasGhiChu = true;
            } catch (SQLException e) {}

            while (rs.next()) {
                tblModel.addRow(new Object[] {
                    stt++, rs.getString("maTau"), rs.getString("tenTau"),
                    rs.getInt("soToa"), rs.getInt("tongSoGhe"),
                    rs.getString("trangThai"), hasGhiChu ? rs.getString("ghiChu") : ""
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private JPanel buildSectionTitle(String title) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel lb = new JLabel(title);
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lb.setForeground(Color.WHITE);
        lb.setOpaque(true);
        lb.setBackground(PRIMARY);
        lb.setBorder(new EmptyBorder(6, 12, 6, 12));
        pnl.add(lb);
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
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private final class TrainCard extends JPanel {
        TrainCard(String id, String name, int toa, int ghe) {
            setPreferredSize(new Dimension(180, 140));
            setBackground(Color.WHITE);
            setLayout(new BorderLayout());
            setBorder(new LineBorder(new Color(230, 233, 238), 1, true));

            JPanel pnlInfo = new JPanel();
            pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
            pnlInfo.setOpaque(false);
            pnlInfo.setBorder(new EmptyBorder(12, 12, 12, 12));

            JLabel lblId = new JLabel(id);
            lblId.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
            lblId.setForeground(GuiTheme.NAVY);

            JLabel lblName = new JLabel(name);
            lblName.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
            lblName.setForeground(GuiTheme.TEXT);

            JLabel lblDetails = new JLabel(toa + " Toa | " + ghe + " Ghế");
            lblDetails.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
            lblDetails.setForeground(GuiTheme.SUB_TEXT);

            pnlInfo.add(lblId);
            pnlInfo.add(Box.createVerticalStrut(4));
            pnlInfo.add(lblName);
            pnlInfo.add(Box.createVerticalStrut(8));
            pnlInfo.add(lblDetails);

            JButton btnAction = new JButton("Điều động") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnAction.setForeground(Color.WHITE);
            btnAction.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 10));
            btnAction.setPreferredSize(new Dimension(0, 28));
            btnAction.setContentAreaFilled(false); btnAction.setBorderPainted(false); btnAction.setFocusPainted(false);
            btnAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAction.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng điều động tàu " + id + " đang được phát triển!"));

            add(pnlInfo, BorderLayout.CENTER);
            add(btnAction, BorderLayout.SOUTH);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
            g2.dispose();
        }
    }
}
