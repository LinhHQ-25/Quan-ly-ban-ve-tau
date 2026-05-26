package gui;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
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
    private JLabel lblResults;
    
    // For Search Card ("LIST")
    private JTextField txtSearchMaKH, txtSearchHoTen, txtSearchSdt, txtSearchCCCD, txtSearchEmail;
    private JDateChooser dcSearchNamSinh;
    private JComboBox<String> cboSearchDoiTuong;

    // For Detail/Update Card ("DETAIL")
    private JTextField txtDetailMaKH, txtDetailHoTen, txtDetailSdt, txtDetailCCCD, txtDetailEmail;
    private JDateChooser dcDetailNamSinh;
    private JComboBox<String> cboDetailDoiTuong;

    private DefaultTableModel tblModelInvoices;
    private JTable tblDataInvoices;

    private CardLayout cardLayout;
    private JPanel pnlMainContainer;
    
    private String selectedMaKH;

    KhachHangGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        pnlMainContainer = new JPanel(cardLayout);
        pnlMainContainer.setOpaque(false);

        // 1. Setup LIST view card
        JPanel pnlListCard = new JPanel(new BorderLayout(0, 12));
        pnlListCard.setOpaque(false);
        pnlListCard.setBorder(new EmptyBorder(
            -9,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));
        pnlListCard.add(buildFilterPanel(), BorderLayout.NORTH);
        pnlListCard.add(buildTablePanel(), BorderLayout.CENTER);

        // 2. Setup DETAIL view card
        JPanel pnlDetailCard = new JPanel(new BorderLayout(0, 12));
        pnlDetailCard.setOpaque(false);
        pnlDetailCard.setBorder(new EmptyBorder(
            -9,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));
        pnlDetailCard.add(buildCustomerDetailPanel(), BorderLayout.NORTH);
        pnlDetailCard.add(buildInvoiceTablePanel(), BorderLayout.CENTER);

        pnlMainContainer.add(pnlListCard, "LIST");
        pnlMainContainer.add(pnlDetailCard, "DETAIL");

        add(pnlMainContainer, BorderLayout.CENTER);
        
        loadDataToTable();
    }

    private JPanel buildFilterPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Title Row
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Thông tin tra cứu & Cập nhật");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(44, 82, 150));
        lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

        // Fields Grid
        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Hàng 0
        gbc.gridy = 0;
        txtSearchMaKH = buildTextField("Nhập mã khách hàng");
        gbc.gridx = 0; pnlGrid.add(buildField("Mã khách hàng:", txtSearchMaKH), gbc);
        
        txtSearchHoTen = buildTextField("Nhập họ tên");
        gbc.gridx = 1; pnlGrid.add(buildField("Họ và tên:", txtSearchHoTen), gbc);
        
        txtSearchSdt = buildTextField("Nhập số điện thoại");
        gbc.gridx = 2; pnlGrid.add(buildField("Số điện thoại:", txtSearchSdt), gbc);
        
        txtSearchCCCD = buildTextField("Nhập CCCD");
        gbc.gridx = 3; pnlGrid.add(buildField("CCCD:", txtSearchCCCD), gbc);
        
        // Hàng 1
        gbc.gridy = 1;
        dcSearchNamSinh = buildDateField();
        gbc.gridx = 0; pnlGrid.add(buildField("Năm sinh:", dcSearchNamSinh), gbc);
        
        txtSearchEmail = buildTextField("Nhập email");
        gbc.gridx = 1; pnlGrid.add(buildField("Email:", txtSearchEmail), gbc);
        
        cboSearchDoiTuong = buildCombo("Tất cả", "Dưới 6 tuổi", "Từ 6 đến dưới 10 tuổi", "Từ 60 tuổi trở lên", "Sinh viên", "Người lớn");
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        pnlGrid.add(buildField("Đối tượng:", cboSearchDoiTuong), gbc);
        gbc.gridwidth = 1;

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);

        // Buttons Row
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlButtons.setOpaque(false);

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);

        btnSearch.addActionListener(e -> loadDataToTable());
        btnReset.addActionListener(e -> {
            txtSearchMaKH.setText(""); txtSearchHoTen.setText(""); txtSearchSdt.setText("");
            txtSearchCCCD.setText(""); txtSearchEmail.setText(""); cboSearchDoiTuong.setSelectedIndex(0);
            dcSearchNamSinh.setDate(null);
            loadDataToTable();
        });

        pnlButtons.add(btnReset);
        pnlButtons.add(btnSearch);
        
        pnlOuter.add(pnlButtons, BorderLayout.SOUTH);

        return pnlOuter;
    }

    private JPanel buildCustomerDetailPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Title Row
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Cập nhật thông tin khách hàng");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(44, 82, 150));
        lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

        // Fields Grid
        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Hàng 0
        gbc.gridy = 0;
        txtDetailMaKH = buildTextField("Mã khách hàng (tự động)");
        txtDetailMaKH.setEditable(false);
        txtDetailMaKH.setBackground(new Color(240, 240, 240));
        gbc.gridx = 0; pnlGrid.add(buildField("Mã khách hàng:", txtDetailMaKH), gbc);
        
        txtDetailHoTen = buildTextField("Nhập họ tên");
        gbc.gridx = 1; pnlGrid.add(buildField("Họ và tên:", txtDetailHoTen), gbc);
        
        txtDetailSdt = buildTextField("Nhập số điện thoại");
        gbc.gridx = 2; pnlGrid.add(buildField("Số điện thoại:", txtDetailSdt), gbc);
        
        txtDetailCCCD = buildTextField("Nhập CCCD");
        gbc.gridx = 3; pnlGrid.add(buildField("CCCD:", txtDetailCCCD), gbc);
        
        // Hàng 1
        gbc.gridy = 1;
        dcDetailNamSinh = buildDateField();
        gbc.gridx = 0; pnlGrid.add(buildField("Năm sinh:", dcDetailNamSinh), gbc);
        
        txtDetailEmail = buildTextField("Nhập email");
        gbc.gridx = 1; pnlGrid.add(buildField("Email:", txtDetailEmail), gbc);
        
        cboDetailDoiTuong = buildCombo("Người lớn", "Dưới 6 tuổi", "Từ 6 đến dưới 10 tuổi", "Từ 60 tuổi trở lên", "Sinh viên");
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        pnlGrid.add(buildField("Đối tượng:", cboDetailDoiTuong), gbc);
        gbc.gridwidth = 1;

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);

        // Buttons Row
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlButtons.setOpaque(false);

        JButton btnBack = buildNavyButton("Quay lại", new Color(110, 117, 124), new Color(90, 96, 102));
        JButton btnUpdate = buildNavyButton("Cập nhật", new Color(40, 160, 60), new Color(30, 140, 50));

        btnBack.addActionListener(e -> {
            cardLayout.show(pnlMainContainer, "LIST");
            loadDataToTable();
        });
        btnUpdate.addActionListener(e -> capNhatKhachHangDetail());

        pnlButtons.add(btnBack);
        pnlButtons.add(btnUpdate);
        
        pnlOuter.add(pnlButtons, BorderLayout.SOUTH);

        return pnlOuter;
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

    private JTextField buildTextField(String placeholder) {
        JTextField txtField = new JTextFieldWithPlaceholder(placeholder);
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(txtField);
        txtField.setPreferredSize(new Dimension(104, 30));
        return txtField;
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

    private JButton buildNavyButton(String text, Color baseColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? baseColor.darker()
                    : getModel().isRollover() ? hoverColor : baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(120, 30));
        btn.setForeground(Color.WHITE);
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(KhachHangGUI.class, "/Images/traCuu.png", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(KhachHangGUI.class, "/Images/logoLammoi.png", 16, 16));
        else if (text.contains("Cập nhật")) btn.setIcon(GuiIcons.loadIcon(KhachHangGUI.class, "/Images/logoLammoi.png", 16, 16));
        else if (text.contains("Quay lại")) btn.setIcon(GuiIcons.loadIcon(KhachHangGUI.class, "/Images/quayLai.png", 16, 16));
        
        return btn;
    }

    private JPanel buildTablePanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JPanel pnlTitleRow = new JPanel(new BorderLayout());
        pnlTitleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách khách hàng (Nhấn đúp vào khách hàng để xem lịch sử hóa đơn)");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(44, 82, 150));
        lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
        pnlTitleRow.add(lblTitle, BorderLayout.WEST);

        lblResults = new JLabel("");
        lblResults.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblResults.setForeground(new Color(120, 130, 140));
        pnlTitleRow.add(lblResults, BorderLayout.EAST);

        pnlHeader.add(pnlTitleRow, BorderLayout.CENTER);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

        String[] cols = {"STT", "Mã khách", "Họ và tên", "Năm sinh", "CCCD", "Số ĐT", "Email", "Đối tượng"};
        tblModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblData = new JTable(tblModel);
        tblData.setRowHeight(36);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblData.setShowVerticalLines(false);
        tblData.setSelectionBackground(new Color(232, 240, 254));
        tblData.setSelectionForeground(GuiTheme.TEXT);
        
        tblData.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseClicked(MouseEvent e) { 
                if (e.getClickCount() == 2) {
                    int row = tblData.getSelectedRow();
                    if (row != -1) {
                        selectedMaKH = tblModel.getValueAt(row, 1).toString();
                        fillDetailFieldsFromTable(row);
                        loadInvoicesForCustomer(selectedMaKH);
                        cardLayout.show(pnlMainContainer, "DETAIL");
                    }
                } else {
                    fillFieldsFromTable();
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
        spnScroll.setBorder(null);
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

        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private void fillDetailFieldsFromTable(int row) {
        if (row != -1) {
            String maKH = tblModel.getValueAt(row, 1).toString();
            txtDetailMaKH.setText(maKH);
            txtDetailHoTen.setText(tblModel.getValueAt(row, 2).toString());
            try {
                String namSinhStr = tblModel.getValueAt(row, 3).toString();
                if (!namSinhStr.isEmpty()) dcDetailNamSinh.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(namSinhStr));
            } catch (Exception ex) { dcDetailNamSinh.setDate(null); }
            txtDetailCCCD.setText(tblModel.getValueAt(row, 4).toString());
            txtDetailSdt.setText(tblModel.getValueAt(row, 5).toString());
            txtDetailEmail.setText(tblModel.getValueAt(row, 6).toString());
            cboDetailDoiTuong.setSelectedItem(tblModel.getValueAt(row, 7).toString());
        }
    }

    private void fillFieldsFromTable() {
        int row = tblData.getSelectedRow();
        if (row != -1) {
            String maKH = tblModel.getValueAt(row, 1).toString();
            txtSearchMaKH.setText(maKH);
            txtSearchHoTen.setText(tblModel.getValueAt(row, 2).toString());
            try {
                String namSinhStr = tblModel.getValueAt(row, 3).toString();
                if (!namSinhStr.isEmpty()) dcSearchNamSinh.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(namSinhStr));
            } catch (Exception ex) { dcSearchNamSinh.setDate(null); }
            txtSearchCCCD.setText(tblModel.getValueAt(row, 4).toString());
            txtSearchSdt.setText(tblModel.getValueAt(row, 5).toString());
            txtSearchEmail.setText(tblModel.getValueAt(row, 6).toString());
            cboSearchDoiTuong.setSelectedItem(tblModel.getValueAt(row, 7).toString());
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
            stmt.setString(1, "%" + txtSearchMaKH.getText().trim() + "%");
            stmt.setString(2, "%" + txtSearchHoTen.getText().trim() + "%");
            stmt.setString(3, "%" + txtSearchSdt.getText().trim() + "%");
            stmt.setString(4, "%" + txtSearchCCCD.getText().trim() + "%");
            stmt.setString(5, "%" + txtSearchEmail.getText().trim() + "%");

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            while (rs.next()) {
                java.sql.Date namSinhDate = rs.getDate("namSinh");
                
                // Lọc theo năm sinh
                if (dcSearchNamSinh.getDate() != null) {
                    java.util.Date selectedDate = dcSearchNamSinh.getDate();
                    SimpleDateFormat compareSdf = new SimpleDateFormat("dd/MM/yyyy");
                    if (namSinhDate == null || !compareSdf.format(namSinhDate).equals(compareSdf.format(selectedDate))) {
                        continue;
                    }
                }
                
                String namSinhStr = (namSinhDate != null) ? sdf.format(namSinhDate) : "";
                
                boolean laSV = rs.getBoolean("laSinhVien");
                LocalDate birth = (namSinhDate != null) ? namSinhDate.toLocalDate() : null;
                
                KhachHang temp = new KhachHang();
                temp.setNamSinh(birth);
                temp.setLaSinhVien(laSV);
                String doiTuong = translateDoiTuong(temp.xacDinhLoaiKhachHang().toString());

                // Lọc theo đối tượng
                String selectedDoiTuong = (String) cboSearchDoiTuong.getSelectedItem();
                if (selectedDoiTuong != null && !selectedDoiTuong.isEmpty() && !selectedDoiTuong.equals("Tất cả")) {
                    if (!doiTuong.equalsIgnoreCase(selectedDoiTuong)) {
                        continue;
                    }
                }

                tblModel.addRow(new Object[] {
                    stt++, rs.getString("maKH"), rs.getString("hoTenKH"),
                    namSinhStr, rs.getString("cccd"), rs.getString("sdt"),
                    rs.getString("email"), doiTuong
                });
            }
            if (lblResults != null) {
                lblResults.setText("");
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

    private void capNhatKhachHangDetail() {
        String maKH = txtDetailMaKH.getText().trim();
        String hoTen = txtDetailHoTen.getText().trim();
        String sdt = txtDetailSdt.getText().trim();
        String cccd = txtDetailCCCD.getText().trim();
        String email = txtDetailEmail.getText().trim();
        java.util.Date namSinh = dcDetailNamSinh.getDate();
        String doiTuong = (String) cboDetailDoiTuong.getSelectedItem();

        if (maKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hoTen.isEmpty() || sdt.isEmpty() || cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên, Số điện thoại và CCCD không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = Connect_DB.getInstance().getConnection()) {
            String sql = "UPDATE KhachHang SET hoTenKH = ?, namSinh = ?, cccd = ?, sdt = ?, email = ?, laSinhVien = ? WHERE maKH = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, hoTen);
                if (namSinh != null) {
                    ps.setDate(2, new java.sql.Date(namSinh.getTime()));
                } else {
                    ps.setNull(2, java.sql.Types.DATE);
                }
                ps.setString(3, cccd);
                ps.setString(4, sdt);
                ps.setString(5, email);
                ps.setBoolean(6, "Sinh viên".equals(doiTuong));
                ps.setString(7, maKH);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thông tin khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng để cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildInvoiceTablePanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JPanel pnlTitleRow = new JPanel(new BorderLayout());
        pnlTitleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Lịch sử hóa đơn của khách hàng");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(44, 82, 150));
        lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
        pnlTitleRow.add(lblTitle, BorderLayout.WEST);

        pnlHeader.add(pnlTitleRow, BorderLayout.CENTER);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

        String[] cols = {"STT", "Mã hóa đơn", "Ngày lập", "Hình thức thanh toán", "Nhân viên lập", "Trạng thái", "Thao tác"};
        tblModelInvoices = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        tblDataInvoices = new JTable(tblModelInvoices);
        tblDataInvoices.setRowHeight(36);
        tblDataInvoices.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblDataInvoices.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblDataInvoices.setShowVerticalLines(false);
        tblDataInvoices.setSelectionBackground(new Color(232, 240, 254));
        tblDataInvoices.setSelectionForeground(GuiTheme.TEXT);

        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, row, col);
                if (!s) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    c.setForeground(GuiTheme.TEXT);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (col == 5) {
                    String val = (v != null) ? v.toString() : "";
                    if ("Đã thanh toán".equals(val)) {
                        c.setForeground(new Color(40, 140, 60));
                        c.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setForeground(new Color(230, 110, 0));
                        c.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < tblDataInvoices.getColumnCount(); i++) {
            if (i == 6) {
                tblDataInvoices.getColumnModel().getColumn(i).setCellRenderer(new ButtonsRenderer());
                tblDataInvoices.getColumnModel().getColumn(i).setCellEditor(new ButtonsEditor(this));
            } else {
                tblDataInvoices.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
            }
        }
        ((DefaultTableCellRenderer)tblDataInvoices.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane spnScroll = new JScrollPane(tblDataInvoices);
        spnScroll.setBorder(null);
        spnScroll.getViewport().setBackground(Color.WHITE);

        SwingUtilities.invokeLater(() -> {
            if (tblDataInvoices.getColumnModel().getColumnCount() >= 7) {
                tblDataInvoices.getColumnModel().getColumn(0).setPreferredWidth(40);
                tblDataInvoices.getColumnModel().getColumn(1).setPreferredWidth(120);
                tblDataInvoices.getColumnModel().getColumn(2).setPreferredWidth(150);
                tblDataInvoices.getColumnModel().getColumn(3).setPreferredWidth(180);
                tblDataInvoices.getColumnModel().getColumn(4).setPreferredWidth(150);
                tblDataInvoices.getColumnModel().getColumn(5).setPreferredWidth(120);
                tblDataInvoices.getColumnModel().getColumn(6).setPreferredWidth(220);
            }
        });

        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private void loadInvoicesForCustomer(String maKH) {
        if (tblModelInvoices == null) return;
        tblModelInvoices.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        // Tránh lỗi dữ liệu bằng cách cập nhật vé hết hạn trước khi load danh sách hóa đơn
        try {
            dao.VeDAO.capNhatTrangThaiVeHetHan();
        } catch (Exception ignored) {}

        String sql = "SELECT hd.maHoaDon, hd.ngayLapHD, hd.tongTien, hd.phuongThucThanhToan, nv.hoTenNV " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                     "WHERE hd.maKH = ? " +
                     "ORDER BY hd.ngayLapHD DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maKH);
            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            while (rs.next()) {
                String curMaHD = rs.getString("maHoaDon");
                Timestamp curNgayLap = rs.getTimestamp("ngayLapHD");
                String pttt = rs.getString("phuongThucThanhToan");
                String nv = rs.getString("hoTenNV") != null ? rs.getString("hoTenNV") : "N/A";
                
                String trangThai = "LUU_TAM".equals(pttt) ? "Chờ thanh toán" : "Đã thanh toán";
                String hinhThuc = "LUU_TAM".equals(pttt) ? "Lưu tạm chờ thanh toán" :
                                  "TIEN_MAT".equals(pttt) ? "Tiền mặt" : "Chuyển khoản";
                                  
                tblModelInvoices.addRow(new Object[] {
                    stt++, curMaHD, (curNgayLap != null ? sdf.format(curNgayLap) : ""),
                    hinhThuc, nv, trangThai, ""
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void thanhToanToanBoHoaDon(String maHD) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (!(parentWindow instanceof AppFrame)) {
            JOptionPane.showMessageDialog(this, "Tính năng thanh toán chỉ khả dụng cho tài khoản nhân viên quầy bán vé!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        AppFrame mainFrame = (AppFrame) parentWindow;
        
        DefaultTableModel modelForThanhToan = new DefaultTableModel(
            new Object[]{"STT", "Mã vé", "Loại vé (Chiều)", "Chiều", "Mã Ghế", "Tên KH", "CCCD", "SĐT", "Loại KH", "Mã Toa", "Mã Tàu", "Tên Tàu", "Mã Chuyến Tàu", "Mã Ga Đến"}, 0
        );
        
        String sql = "SELECT v.maVe, v.loaiVe, v.maGhe, kh.hoTenKH, kh.cccd, kh.sdt, " +
                     "toa.maToaTau, t.tenTau, v.maChuyenTau, dt.maGaDen, hd.ngayLapHD " +
                     "FROM Ve v " +
                     "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                     "LEFT JOIN KhachHang kh ON COALESCE(v.maKH, hd.maKH) = kh.maKH " +
                     "JOIN Ghe g ON v.maGhe = g.maGhe " +
                     "JOIN ToaTau toa ON g.maToaTau = toa.maToaTau " +
                     "JOIN Tau t ON toa.maTau = t.maTau " +
                     "JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                     "WHERE v.maHoaDon = ?";
                     
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();
            
            int stt = 1;
            Timestamp ngayLapHD = null;
            while (rs.next()) {
                if (ngayLapHD == null) {
                    ngayLapHD = rs.getTimestamp("ngayLapHD");
                }
                
                String maVe = rs.getString("maVe");
                String loaiVeRaw = rs.getString("loaiVe");
                String loaiVe = "MOT_CHIEU".equals(loaiVeRaw) ? "Chiều đi" : "Chiều về";
                String maGhe = rs.getString("maGhe");
                String hoTenKH = rs.getString("hoTenKH") != null ? rs.getString("hoTenKH") : "N/A";
                String cccd = rs.getString("cccd") != null ? rs.getString("cccd") : "";
                String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                String maToa = rs.getString("maToaTau");
                String tenTau = rs.getString("tenTau");
                String maChuyen = rs.getString("maChuyenTau");
                String maGaDen = rs.getString("maGaDen");
                
                modelForThanhToan.addRow(new Object[]{
                    stt++, maVe, loaiVeRaw, loaiVe, maGhe, hoTenKH, cccd, sdt, "Người lớn", maToa, "", tenTau, maChuyen, maGaDen
                });
            }
            
            if (stt == 1 || ngayLapHD == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy danh sách vé thuộc hóa đơn này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            long diff = new java.util.Date().getTime() - ngayLapHD.getTime();
            int secondsLeft = (int) (30 * 60 - diff / 1000);
            if (secondsLeft < 0) secondsLeft = 0;
            
            DatVeGUI3 panel3 = new DatVeGUI3(modelForThanhToan, secondsLeft, (sec) -> {
                mainFrame.showCard("tra-cuu-khach");
            });
            panel3.setMaHD(maHD);
            
            mainFrame.showTemporaryCard(panel3, "thanh-toan-lai-khach");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi nạp dữ liệu thanh toán: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JButton createActionComponentButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(220, 220, 220));
                } else if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.setColor(isEnabled() ? Color.WHITE : new Color(140, 140, 140));
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(95, 26));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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

    private static final class JTextFieldWithPlaceholder extends JTextField {
        private final String placeholder;
        private JTextFieldWithPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 160, 160));
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                int padding = getInsets().left;
                g2.drawString(placeholder, padding + 4, g.getFontMetrics().getAscent() + (getHeight() - g.getFontMetrics().getHeight()) / 2);
                g2.dispose();
            }
        }
    }

    private static final class ButtonsRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        private final JButton btnChiTiet = createActionComponentButton("Chi tiết", new Color(44, 82, 150));
        private final JButton btnThanhToan = createActionComponentButton("Thanh toán", new Color(40, 160, 60));

        public ButtonsRenderer() {
            panel.setOpaque(true);
            panel.add(btnChiTiet);
            panel.add(btnThanhToan);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
            }
            
            String status = table.getValueAt(row, 5).toString();
            btnThanhToan.setEnabled("Chờ thanh toán".equals(status));
            
            return panel;
        }
    }

    private static final class ButtonsEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        private final JButton btnChiTiet = createActionComponentButton("Chi tiết", new Color(44, 82, 150));
        private final JButton btnThanhToan = createActionComponentButton("Thanh toán", new Color(40, 160, 60));
        private final KhachHangGUI parentGui;
        private String currentMaHD;
        private JTable currentTable;

        public ButtonsEditor(KhachHangGUI parentGui) {
            this.parentGui = parentGui;
            panel.setOpaque(true);
            panel.add(btnChiTiet);
            panel.add(btnThanhToan);

            btnChiTiet.addActionListener(e -> {
                fireEditingStopped();
                Frame parentFrame = JOptionPane.getFrameForComponent(currentTable);
                LichSuMuaVeDialog dialog = new LichSuMuaVeDialog(parentFrame, currentMaHD, true);
                dialog.setVisible(true);
            });

            btnThanhToan.addActionListener(e -> {
                fireEditingStopped();
                parentGui.thanhToanToanBoHoaDon(currentMaHD);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentTable = table;
            panel.setBackground(table.getSelectionBackground());
            currentMaHD = table.getValueAt(row, 1).toString();
            String status = table.getValueAt(row, 5).toString();
            btnThanhToan.setEnabled("Chờ thanh toán".equals(status));
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
