package gui;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import com.toedter.calendar.JDateChooser;
import connect_DB.Connect_DB;
import entity.KhachHang;

final class KhachHangGUI extends JPanel {
    private static final Color BORDER = GuiTheme.SEARCH_FIELD_BORDER;
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JTextField txtMaKH, txtHoTen, txtSdt, txtCCCD, txtEmail;
    private JDateChooser dcNamSinh;
    private JComboBox<String> cboDoiTuong;

    KhachHangGUI() {
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
        pnlPage.add(buildTablePanel(), BorderLayout.CENTER);

        add(pnlPage, BorderLayout.CENTER);
        
        loadDataToTable();
    }

    private JPanel buildFilterPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 5));
        
        JLabel lblTitle = new JLabel("Thông tin tra cứu");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(GuiTheme.TEXT);
        lblTitle.setBorder(new EmptyBorder(10, 15, 0, 15));
        lblTitle.setIcon(GuiIcons.loadIcon(KhachHangGUI.class, "filter", 18, 18));
        lblTitle.setIconTextGap(8);
        pnlOuter.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        txtMaKH = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Mã khách hàng:", txtMaKH), gbc);
        
        txtHoTen = buildTextField();
        gbc.gridx = 1; pnlGrid.add(buildField("Họ và tên:", txtHoTen), gbc);
        
        dcNamSinh = buildDateField();
        gbc.gridx = 2; pnlGrid.add(buildField("Năm sinh:", dcNamSinh), gbc);
        
        txtSdt = buildTextField();
        gbc.gridx = 3; pnlGrid.add(buildField("Số điện thoại:", txtSdt), gbc);

        gbc.gridy = 1;
        txtCCCD = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("CCCD:", txtCCCD), gbc);
        
        txtEmail = buildTextField();
        gbc.gridx = 1; pnlGrid.add(buildField("Email:", txtEmail), gbc);
        
        cboDoiTuong = buildCombo("", "Dưới 6 tuổi", "Từ 6 đến dưới 10 tuổi", "Từ 60 tuổi trở lên", "Sinh viên", "Người lớn");
        gbc.gridx = 2; pnlGrid.add(buildField("Đối tượng:", cboDoiTuong), gbc);
        
        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        JPanel pnlAction = buildSearchBlock();
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
        lbField.setPreferredSize(new Dimension(125, 30));
        pnlField.add(lbField, BorderLayout.WEST);
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
            new EmptyBorder(2, 6, 2, 6)
        ));
        txtField.setPreferredSize(new Dimension(104, 30));
        return txtField;
    }

    private JComboBox<String> buildCombo(String... values) {
        JComboBox<String> cmb = new JComboBox<>(values);
        cmb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cmb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cmb.setForeground(GuiTheme.TEXT);
        cmb.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        cmb.setPreferredSize(new Dimension(104, 30));
        return cmb;
    }

    private JDateChooser buildDateField() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setPreferredSize(new Dimension(104, 30));
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(GuiTheme.SEARCH_FIELD_BG);
        dc.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        return dc;
    }

    private JPanel buildSearchBlock() {
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlButtons.setOpaque(false);
        pnlButtons.setBorder(new EmptyBorder(0, 0, 5, 0));

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);

        btnSearch.addActionListener(e -> loadDataToTable());
        btnReset.addActionListener(e -> {
            txtMaKH.setText(""); txtHoTen.setText(""); txtSdt.setText("");
            txtCCCD.setText(""); txtEmail.setText(""); cboDoiTuong.setSelectedIndex(0);
            dcNamSinh.setDate(null); loadDataToTable();
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
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(KhachHangGUI.class, "/Images/traCuu.png", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(KhachHangGUI.class, "/Images/logoLammoi.png", 16, 16));
        
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlWrap = new JPanel(new BorderLayout(0, 8));
        pnlWrap.setOpaque(false);
        pnlWrap.add(buildSectionTitle("Danh sách khách hàng"), BorderLayout.NORTH);

        String[] cols = {"STT", "Mã khách", "Họ và tên", "Năm sinh", "CCCD", "Số ĐT", "Email", "Đối tượng"};
        tblModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblData = new JTable(tblModel);
        tblData.setRowHeight(28);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblData.setShowVerticalLines(false);
        tblData.setSelectionBackground(new Color(232, 240, 254));
        tblData.setSelectionForeground(GuiTheme.TEXT);
        
        tblData.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseClicked(MouseEvent e) { 
                fillFieldsFromTable(); 
                if (e.getClickCount() == 2) {
                    int row = tblData.getSelectedRow();
                    if (row != -1) {
                        String maKH = tblModel.getValueAt(row, 1).toString();
                        Window parentWindow = SwingUtilities.getWindowAncestor(KhachHangGUI.this);
                        if (parentWindow instanceof Frame) {
                            LichSuMuaVeDialog dialog = new LichSuMuaVeDialog((Frame) parentWindow, maKH);
                            dialog.setVisible(true);
                        }
                    }
                }
            }
        });

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
            if (tblData.getColumnModel().getColumnCount() >= 8) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(40);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(160);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(200);
                tblData.getColumnModel().getColumn(7).setPreferredWidth(120);
            }
        });

        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    private void fillFieldsFromTable() {
        int row = tblData.getSelectedRow();
        if (row != -1) {
            txtMaKH.setText(tblModel.getValueAt(row, 1).toString());
            txtHoTen.setText(tblModel.getValueAt(row, 2).toString());
            try {
                String namSinhStr = tblModel.getValueAt(row, 3).toString();
                if (!namSinhStr.isEmpty()) dcNamSinh.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(namSinhStr));
            } catch (Exception ex) { dcNamSinh.setDate(null); }
            txtCCCD.setText(tblModel.getValueAt(row, 4).toString());
            txtSdt.setText(tblModel.getValueAt(row, 5).toString());
            txtEmail.setText(tblModel.getValueAt(row, 6).toString());
            cboDoiTuong.setSelectedItem(tblModel.getValueAt(row, 7).toString());
        }
    }

    private void loadDataToTable() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT * FROM KhachHang WHERE maKH LIKE ? AND hoTenKH LIKE ? AND sdt LIKE ? AND cccd LIKE ? AND email LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaKH.getText().trim() + "%");
            stmt.setString(2, "%" + txtHoTen.getText().trim() + "%");
            stmt.setString(3, "%" + txtSdt.getText().trim() + "%");
            stmt.setString(4, "%" + txtCCCD.getText().trim() + "%");
            stmt.setString(5, "%" + txtEmail.getText().trim() + "%");

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            while (rs.next()) {
                java.sql.Date namSinhDate = rs.getDate("namSinh");
                String namSinhStr = (namSinhDate != null) ? sdf.format(namSinhDate) : "";
                
                boolean laSV = rs.getBoolean("laSinhVien");
                LocalDate birth = (namSinhDate != null) ? namSinhDate.toLocalDate() : null;
                
                KhachHang temp = new KhachHang();
                temp.setNamSinh(birth);
                temp.setLaSinhVien(laSV);
                String doiTuong = translateDoiTuong(temp.xacDinhLoaiKhachHang().toString());

                tblModel.addRow(new Object[] {
                    stt++, rs.getString("maKH"), rs.getString("hoTenKH"),
                    namSinhStr, rs.getString("cccd"), rs.getString("sdt"),
                    rs.getString("email"), doiTuong
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private String translateDoiTuong(String value) {
        if (value == null) return "";
        switch (value) {
            case "DUOI_6_TUOI": return "Dưới 6 tuổi";
            case "TU_6_TOI_DUOI_10": return "Từ 6 đến dưới 10 tuổi";
            case "TU_60_TRO_LEN": return "Từ 60 tuổi trở lên";
            case "SINH_VIEN": return "Sinh viên";
            case "NGUOI_LON": return "Người lớn";
            default: return value;
        }
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
}
