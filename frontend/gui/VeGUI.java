package gui;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import com.toedter.calendar.JDateChooser;
import connect_DB.Connect_DB;

final class VeGUI extends JPanel {
    private static final Color BORDER = GuiTheme.SEARCH_FIELD_BORDER;
    private static final Color PRIMARY = new Color(71, 71, 156);

    private JTextField txtMaVe, txtHoTen, txtCccd, txtGaDi, txtGaDen, txtViTri;
    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JComboBox<String> cboLoaiVe, cboTrangThai;
    private JDateChooser dcNgayMua;
    
    private SmartFilterCard cardChoThanhToan;
    private SmartFilterCard cardKhoiHanhGan;
    private String activeCard = "ALL";

    VeGUI() {
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

        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.setOpaque(false);
        pnlTop.add(buildSmartFilters(), BorderLayout.NORTH);
        pnlTop.add(buildFilterPanel(), BorderLayout.CENTER);

        pnlPage.add(pnlTop, BorderLayout.NORTH);
        pnlPage.add(buildTablePanel(), BorderLayout.CENTER);

        add(pnlPage, BorderLayout.CENTER);

        loadDataToTable();
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
        
        // Hàng 0
        gbc.gridy = 0;
        txtMaVe = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Mã vé:", txtMaVe), gbc);
        
        cboLoaiVe = buildCombo("", "Một chiều", "Khứ hồi");
        gbc.gridx = 1; pnlGrid.add(buildField("Loại vé:", cboLoaiVe), gbc);
        
        cboTrangThai = buildCombo("", "Chờ thanh toán", "Đã thanh toán", "Đã hủy");
        gbc.gridx = 2; pnlGrid.add(buildField("Trạng thái vé:", cboTrangThai), gbc);

        txtHoTen = buildTextField();
        gbc.gridx = 3; pnlGrid.add(buildField("Họ tên khách:", txtHoTen), gbc);
        
        txtCccd = buildTextField();
        gbc.gridx = 4; pnlGrid.add(buildField("CCCD / SĐT:", txtCccd), gbc);
        
        // Hàng 1
        gbc.gridy = 1;
        dcNgayMua = buildDateField();
        gbc.gridx = 0; pnlGrid.add(buildField("Ngày mua:", dcNgayMua), gbc);

        txtGaDi = buildTextField();
        gbc.gridx = 1; pnlGrid.add(buildField("Ga đi:", txtGaDi), gbc);
        
        txtGaDen = buildTextField();
        gbc.gridx = 2; pnlGrid.add(buildField("Ga đến:", txtGaDen), gbc);

        txtViTri = buildTextField();
        gbc.gridx = 3; pnlGrid.add(buildField("Vị trí ghế:", txtViTri), gbc);

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        pnlSection.add(pnlOuter, BorderLayout.CENTER);
        return pnlSection;
    }

    private JPanel buildSmartFilters() {
        JPanel pnlFilters = new JPanel(new GridLayout(1, 0, 15, 0));
        pnlFilters.setOpaque(false);

        cardChoThanhToan = new SmartFilterCard("Vé chờ thanh toán", "0", new Color(253, 126, 20));
        cardKhoiHanhGan = new SmartFilterCard("Sắp khởi hành < 5h", "0", new Color(220, 53, 69));

        cardChoThanhToan.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeCard = "CHO"; loadDataToTable();
            }
        });
        cardKhoiHanhGan.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeCard = "SAP"; loadDataToTable();
            }
        });

        pnlFilters.add(cardChoThanhToan);
        pnlFilters.add(cardKhoiHanhGan);

        return pnlFilters;
    }

    private JPanel buildField(String label, Component comp) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 4));
        pnlField.setOpaque(false);
        JLabel lbField = new JLabel(label);
        lbField.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbField.setForeground(GuiTheme.NAVY);
        pnlField.add(lbField, BorderLayout.NORTH);
        pnlField.add(comp, BorderLayout.CENTER);
        return pnlField;
    }

    private JTextField buildTextField() {
        JTextField tf = new JTextField();
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(tf);
        tf.setPreferredSize(new Dimension(104, 30));
        return tf;
    }

    private JComboBox<String> buildCombo(String... values) {
        JComboBox<String> cmb = new JComboBox<>(values);
        cmb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cmb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cmb.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(cmb);
        cmb.setPreferredSize(new Dimension(104, 30));
        return cmb;
    }

    private JDateChooser buildDateField() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setPreferredSize(new Dimension(104, 30));
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(GuiTheme.SEARCH_FIELD_BG);
        GuiTheme.setupRoundedComponent(dc);
        return dc;
    }

    private JPanel buildActionBlock() {
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);

        btnSearch.addActionListener(e -> { activeCard = "ALL"; loadDataToTable(); });
        btnReset.addActionListener(e -> {
            txtMaVe.setText(""); txtHoTen.setText(""); txtCccd.setText("");
            txtGaDi.setText(""); txtGaDen.setText(""); txtViTri.setText("");
            cboLoaiVe.setSelectedIndex(0); cboTrangThai.setSelectedIndex(0);
            dcNgayMua.setDate(null); activeCard = "ALL";
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(VeGUI.class, "/Images/traCuu.png", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(VeGUI.class, "/Images/logoLammoi.png", 16, 16));
        
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 10));
        pnlOuter.setOpaque(false);
        JPanel pnlActions = buildActionBlock();
        pnlOuter.add(buildSectionTitleWithActions("Danh sách vé tàu", pnlActions), BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
                new Object[]{"STT", "Mã vé", "Khách hàng", "Loại vé", "Loại ghế", "Khởi hành", "Tuyến đường", "Vị trí", "Ngày mua", "Trạng thái"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String status = v != null ? v.toString() : "";
                if (!s) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    if (status.equals("Đã thanh toán")) {
                        c.setForeground(new Color(40, 167, 69)); // Green
                        c.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
                    } else if (status.equals("Chờ thanh toán")) {
                        c.setForeground(new Color(253, 126, 20)); // Orange
                        c.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
                    } else if (status.equals("Đã hủy")) {
                        c.setForeground(new Color(220, 53, 69)); // Red
                        c.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setForeground(GuiTheme.TEXT);
                        c.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                    }
                } else {
                    c.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
                }
                return c;
            }
        };

        for (int i = 0; i < tblData.getColumnCount(); i++) {
            if (i == 9) {
                tblData.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
            } else {
                tblData.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
            }
        }
        
        tblData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblData.getSelectedRow();
                    if (row != -1) {
                        String maVe = (String) tblModel.getValueAt(row, 1);
                        AppFrame appFrame = (AppFrame) SwingUtilities.getWindowAncestor(VeGUI.this);
                        if (appFrame != null) {
                            ChiTietVeDialog dialog = new ChiTietVeDialog(appFrame, maVe);
                            dialog.setVisible(true);
                        }
                    }
                }
            }
        });
        
        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 10) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(40);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(80);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(150);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(80);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(180);
                tblData.getColumnModel().getColumn(7).setPreferredWidth(60);
                tblData.getColumnModel().getColumn(8).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(9).setPreferredWidth(110);
            }
        });
        ((DefaultTableCellRenderer)tblData.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private void xoaVeHetHan() {
        String sqlRename = "UPDATE Ve SET trangThaiVe = N'Chờ thanh toán' WHERE trangThaiVe = N'Chưa thanh toán'";
        String sql = "UPDATE Ve SET trangThaiVe = N'Đã hủy' WHERE trangThaiVe = N'Chờ thanh toán' AND DATEDIFF(minute, ngayMua, GETDATE()) >= 30";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement psRename = con.prepareStatement(sqlRename);
             PreparedStatement ps = con.prepareStatement(sql)) {
            psRename.executeUpdate();
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDataToTable() {
        if (tblModel == null) return;
        xoaVeHetHan();
        tblModel.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT v.maVe, kh.hoTenKH, v.loaiVe, g.loaiGhe, dt.thoiGianKhoiHanh, " +
                         "gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, v.maGhe, hd.ngayLapHD, v.trangThaiVe " +
                         "FROM Ve v " +
                         "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                         "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                         "JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                         "JOIN Ga gDi ON dt.maGaDi = gDi.maGa " +
                         "JOIN Ga gDen ON dt.maGaDen = gDen.maGa " +
                         "JOIN Ghe g ON v.maGhe = g.maGhe " +
                         "WHERE v.maVe LIKE ? AND kh.hoTenKH LIKE ? AND v.maGhe LIKE ? AND gDi.tenGa LIKE ? AND gDen.tenGa LIKE ?";
            
            if (activeCard.equals("CHO")) {
                sql += " AND v.trangThaiVe = N'Chờ thanh toán'";
            } else if (activeCard.equals("SAP")) {
                sql += " AND v.trangThaiVe = N'Đã thanh toán' AND dt.thoiGianKhoiHanh >= GETDATE() AND dt.thoiGianKhoiHanh <= DATEADD(hour, 5, GETDATE())";
            }

            String trangThai = (String) cboTrangThai.getSelectedItem();
            if (trangThai != null && !trangThai.isEmpty()) sql += " AND v.trangThaiVe = ?";
            
            String loaiVe = (String) cboLoaiVe.getSelectedItem();
            if (loaiVe != null && !loaiVe.isEmpty()) sql += " AND v.loaiVe = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaVe.getText().trim() + "%");
            stmt.setString(2, "%" + txtHoTen.getText().trim() + "%");
            stmt.setString(3, "%" + txtViTri.getText().trim() + "%");
            stmt.setString(4, "%" + txtGaDi.getText().trim() + "%");
            stmt.setString(5, "%" + txtGaDen.getText().trim() + "%");
            
            int idx = 6;
            if (trangThai != null && !trangThai.isEmpty()) stmt.setString(idx++, trangThai);
            if (loaiVe != null && !loaiVe.isEmpty()) stmt.setString(idx++, loaiVe);

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                String khoiHanh = rs.getTimestamp("thoiGianKhoiHanh") != null ? sdf.format(rs.getTimestamp("thoiGianKhoiHanh")) : "";
                String ngayMua = rs.getTimestamp("ngayLapHD") != null ? sdf.format(rs.getTimestamp("ngayLapHD")) : "";
                tblModel.addRow(new Object[] {
                    stt++, rs.getString("maVe"), rs.getString("hoTenKH"), rs.getString("loaiVe"),
                    rs.getString("loaiGhe"), khoiHanh, rs.getString("gaDi") + " - " + rs.getString("gaDen"),
                    rs.getString("maGhe"), ngayMua, rs.getString("trangThaiVe")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        updateSmartFilters();
    }

    private void updateSmartFilters() {
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        try {
            String sql1 = "SELECT COUNT(*) FROM Ve WHERE trangThaiVe = N'Chờ thanh toán'";
            try (PreparedStatement pst = conn.prepareStatement(sql1); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardChoThanhToan.setCount(String.valueOf(rs.getInt(1)));
            }
            String sql2 = "SELECT COUNT(v.maVe) FROM Ve v JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                          "WHERE v.trangThaiVe = N'Đã thanh toán' AND dt.thoiGianKhoiHanh >= GETDATE() AND dt.thoiGianKhoiHanh <= DATEADD(hour, 5, GETDATE())";
            try (PreparedStatement pst = conn.prepareStatement(sql2); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardKhoiHanhGan.setCount(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
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
}
