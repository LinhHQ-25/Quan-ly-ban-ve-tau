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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import connect_DB.Connect_DB;

final class VeGUI extends JPanel {
    private static final Color BORDER = GuiTheme.SEARCH_FIELD_BORDER;
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JTextField txtMaVe, txtHoTen, txtCccd, txtGaDi, txtGaDen, txtViTri;
    private JComboBox<String> cboLoaiVe, cboTrangThai;
    private JDateChooser dcNgayMua;
    
    private SmartFilterCard cardChoThanhToan;
    private SmartFilterCard cardKhoiHanhGan;

    VeGUI() {
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

        loadDataToTable();
        updateSmartFilters();
    }

    private JPanel buildFilterPanel() {
        RoundedShadowPanel pnlOuter = new RoundedShadowPanel();
        pnlOuter.setLayout(new BorderLayout(0, 5));
        
        // Tiêu đề
        JLabel lblTitle = new JLabel("Thông tin tra cứu");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(GuiTheme.TEXT);
        lblTitle.setBorder(new EmptyBorder(10, 15, 0, 15));
        lblTitle.setIcon(GuiIcons.loadIcon(VeGUI.class, "filter", 18, 18));
        lblTitle.setIconTextGap(8);
        pnlOuter.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        txtMaVe = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Mã vé:", txtMaVe), gbc);
        
        cboLoaiVe = buildCombo("", "Một chiều", "Khứ hồi");
        gbc.gridx = 1; pnlGrid.add(buildField("Loại vé:", cboLoaiVe), gbc);
        
        dcNgayMua = buildDateField();
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày mua:", dcNgayMua), gbc);
        
        cboTrangThai = buildCombo("", "Chờ thanh toán", "Đã thanh toán", "Đã hủy");
        gbc.gridx = 3; pnlGrid.add(buildField("Trạng thái vé:", cboTrangThai), gbc);

        gbc.gridy = 1;
        txtHoTen = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Họ tên khách:", txtHoTen), gbc);
        
        txtCccd = buildTextField();
        gbc.gridx = 1; pnlGrid.add(buildField("CCCD / SĐT:", txtCccd), gbc);
        
        txtGaDi = buildTextField();
        gbc.gridx = 2; pnlGrid.add(buildField("Ga đi:", txtGaDi), gbc);
        
        txtGaDen = buildTextField();
        gbc.gridx = 3; pnlGrid.add(buildField("Ga đến:", txtGaDen), gbc);

        gbc.gridy = 2;
        txtViTri = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Vị trí ghế:", txtViTri), gbc);
        
        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        JPanel pnlAction = buildActionBlock();
        pnlAction.setOpaque(false);
        pnlOuter.add(pnlAction, BorderLayout.SOUTH);
        
        return pnlOuter;
    }

    private JPanel buildSmartFilters() {
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlFilters.setOpaque(false);

        cardChoThanhToan = new SmartFilterCard("Vé chờ thanh toán", "0", new Color(253, 126, 20));
        cardKhoiHanhGan = new SmartFilterCard("Sắp khởi hành < 24h", "0", new Color(220, 53, 69));

        cardChoThanhToan.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                cboTrangThai.setSelectedItem("Chờ thanh toán");
                loadDataToTable();
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
        lbField.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lbField.setForeground(GuiTheme.NAVY);
        pnlField.add(lbField, BorderLayout.NORTH);
        pnlField.add(comp, BorderLayout.CENTER);
        return pnlField;
    }

    private JTextField buildTextField() {
        JTextField txtField = new JTextField();
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setForeground(GuiTheme.TEXT);
        txtField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true),
            new EmptyBorder(2, 8, 2, 8)
        ));
        txtField.setPreferredSize(new Dimension(160, 32));
        return txtField;
    }

    private JComboBox<String> buildCombo(String... values) {
        JComboBox<String> cmb = new JComboBox<>(values);
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

    private JPanel buildActionBlock() {
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlButtons.setOpaque(false);
        pnlButtons.setBorder(new EmptyBorder(0, 0, 5, 0));
        
        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);

        btnSearch.addActionListener(e -> loadDataToTable());

        btnReset.addActionListener(e -> {
            txtMaVe.setText("");
            txtHoTen.setText("");
            txtCccd.setText("");
            txtGaDi.setText("");
            txtGaDen.setText("");
            txtViTri.setText("");
            cboLoaiVe.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            dcNgayMua.setDate(null);
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
        btn.setPreferredSize(new Dimension(130, 36));
        btn.setMaximumSize(new Dimension(130, 36));
        btn.setForeground(Color.WHITE);
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        
        // Add icons based on text
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(VeGUI.class, "search", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(VeGUI.class, "reset", 16, 16));
        
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
        pnlOuter.setOpaque(false);
        pnlOuter.add(buildSectionTitle("Danh sách vé tàu"), BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
            new Object[] { "STT", "Mã vé", "Tên khách", "Loại vé", "Khởi hành", "Ga đi - Ga đến", "Vị trí", "Ngày mua", "Trạng thái" },
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

        // Zebra striping renderer
        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    c.setForeground(GuiTheme.TEXT);
                }
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
            if (tblData.getColumnModel().getColumnCount() >= 9) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(40);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(140);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(140);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(140);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(7).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(8).setPreferredWidth(90);
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
            String sql = "SELECT v.maVe, kh.hoTenKH, v.loaiVe, ct.thoiGianKhoiHanh, " +
                         "gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, v.maGhe, v.ngayMua, v.trangThaiVe " +
                         "FROM Ve v " +
                         "JOIN KhachHang kh ON v.maKH = kh.maKH " +
                         "JOIN ChuyenTau ct ON v.maChuyen = ct.maChuyen " +
                         "JOIN Ga gDi ON ct.gaDi = gDi.maGa " +
                         "JOIN Ga gDen ON ct.gaDen = gDen.maGa " +
                         "WHERE v.maVe LIKE ? AND kh.hoTenKH LIKE ? AND v.maGhe LIKE ?";
            
            String loaiVe = (String) cboLoaiVe.getSelectedItem();
            if (loaiVe != null && !loaiVe.isEmpty()) {
                sql += " AND v.loaiVe = ?";
            }
            
            String trangThai = (String) cboTrangThai.getSelectedItem();
            if (trangThai != null && !trangThai.isEmpty()) {
                sql += " AND v.trangThaiVe = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaVe.getText().trim() + "%");
            stmt.setString(2, "%" + txtHoTen.getText().trim() + "%");
            stmt.setString(3, "%" + txtViTri.getText().trim() + "%");
            
            int idx = 4;
            if (loaiVe != null && !loaiVe.isEmpty()) {
                String enumValue = loaiVe.equals("Một chiều") ? "MOT_CHIEU" : "KHU_HOI";
                stmt.setString(idx++, enumValue);
            }
            if (trangThai != null && !trangThai.isEmpty()) {
                String enumValue = "";
                if (trangThai.equals("Chờ thanh toán")) enumValue = "CHO_THANH_TOAN";
                else if (trangThai.equals("Đã thanh toán")) enumValue = "DA_THANH_TOAN";
                else if (trangThai.equals("Đã hủy")) enumValue = "DA_HUY";
                stmt.setString(idx++, enumValue);
            }

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                String khoiHanh = rs.getTimestamp("thoiGianKhoiHanh") != null ? sdf.format(rs.getTimestamp("thoiGianKhoiHanh")) : "";
                String ngayMua = rs.getTimestamp("ngayMua") != null ? sdf.format(rs.getTimestamp("ngayMua")) : "";
                String tuyenDuong = rs.getString("gaDi") + " - " + rs.getString("gaDen");
                
                tblModel.addRow(new Object[] {
                    stt++,
                    rs.getString("maVe"),
                    rs.getString("hoTenKH"),
                    translateEnum(rs.getString("loaiVe")),
                    khoiHanh,
                    tuyenDuong,
                    rs.getString("maGhe"),
                    ngayMua,
                    translateEnum(rs.getString("trangThaiVe"))
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
            String sql1 = "SELECT COUNT(*) FROM Ve WHERE trangThaiVe = 'CHO_THANH_TOAN'";
            try (PreparedStatement pst = conn.prepareStatement(sql1); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardChoThanhToan.setCount(String.valueOf(rs.getInt(1)));
            }
            
            String sql2 = "SELECT COUNT(v.maVe) FROM Ve v JOIN ChuyenTau ct ON v.maChuyen = ct.maChuyen " +
                          "WHERE ct.thoiGianKhoiHanh >= GETDATE() AND ct.thoiGianKhoiHanh <= DATEADD(hour, 24, GETDATE())";
            try (PreparedStatement pst = conn.prepareStatement(sql2); ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cardKhoiHanhGan.setCount(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String translateEnum(String value) {
        if (value == null) return "";
        switch (value) {
            case "MOT_CHIEU": return "Một chiều";
            case "KHU_HOI": return "Khứ hồi";
            case "DA_THANH_TOAN": return "Đã thanh toán";
            case "CHO_THANH_TOAN": return "Chờ thanh toán";
            case "DA_HUY": return "Đã hủy";
            default: return value;
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

class SmartFilterCard extends JPanel {
    private JLabel lblCount;

    SmartFilterCard(String title, String count, Color accentColor) {
        setLayout(new BorderLayout(15, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(12, 15, 12, 20)
            )
        ));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(GuiTheme.TEXT);

        lblCount = new JLabel(count);
        lblCount.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 22));
        lblCount.setForeground(accentColor);

        add(lblTitle, BorderLayout.CENTER);
        add(lblCount, BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setBackground(new Color(248, 249, 250));
            }
            @Override public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
            }
        });
    }

    public void setCount(String count) {
        lblCount.setText(count);
    }
}
