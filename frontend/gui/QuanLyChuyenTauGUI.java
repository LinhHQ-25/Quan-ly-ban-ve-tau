package gui;

import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import connect_DB.Connect_DB;

final class QuanLyChuyenTauGUI extends JPanel {
    private static final Color BORDER = GuiTheme.SEARCH_FIELD_BORDER;
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JComboBox<String> cboGaDiFilter, cboGaDenFilter, cboTauFilter;
    private JDateChooser dcNgayDi;
    
    private JPanel pnlIdleCards;
    
    private java.util.Map<String, String> mapGa = new java.util.LinkedHashMap<>();
    private java.util.List<String> listTau = new java.util.ArrayList<>();

    QuanLyChuyenTauGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        initData();

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BorderLayout(0, 12));
        pnlPage.setBorder(new EmptyBorder(
            0,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));

        // Phần Filter ở trên cùng
        pnlPage.add(buildFilterPanel(), BorderLayout.NORTH);

        // Phần Center chứa IdleCards và Table
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 12));
        pnlCenter.setOpaque(false);
        pnlCenter.add(buildIdleTrainsSection(), BorderLayout.NORTH);
        pnlCenter.add(buildTablePanel(), BorderLayout.CENTER);

        pnlPage.add(pnlCenter, BorderLayout.CENTER);

        add(pnlPage, BorderLayout.CENTER);
        
        loadDataToTable();
        loadIdleTrains();
    }

    private String[] getGaList() {
        return mapGa.keySet().toArray(new String[0]);
    }
    
    private String[] getGaListWithEmpty() {
        java.util.List<String> res = new java.util.ArrayList<>();
        res.add("");
        res.addAll(mapGa.keySet());
        return res.toArray(new String[0]);
    }

    private String[] getTauListWithEmpty() {
        java.util.List<String> res = new java.util.ArrayList<>();
        res.add("");
        res.addAll(listTau);
        return res.toArray(new String[0]);
    }
    
    private String getMaGa(String tenGa) {
        return mapGa.get(tenGa);
    }

    private void initData() {
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        try {
            String sqlGa = "SELECT maGa, tenGa FROM Ga";
            ResultSet rsGa = conn.createStatement().executeQuery(sqlGa);
            while (rsGa.next()) {
                mapGa.put(rsGa.getString("tenGa"), rsGa.getString("maGa"));
            }
            
            String sqlTau = "SELECT tenTau FROM Tau";
            ResultSet rsTau = conn.createStatement().executeQuery(sqlTau);
            while (rsTau.next()) {
                listTau.add(rsTau.getString("tenTau"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel buildFilterPanel() {
        RoundedShadowPanel pnlOuter = new RoundedShadowPanel();
        pnlOuter.setLayout(new BorderLayout(0, 5));
        
        JLabel lblTitle = new JLabel("Thông tin tra cứu chuyến đi & tàu");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(GuiTheme.TEXT);
        lblTitle.setBorder(new EmptyBorder(10, 15, 0, 15));
        lblTitle.setIcon(GuiIcons.loadIcon(QuanLyChuyenTauGUI.class, "/Images/traCuu.png", 18, 18));
        lblTitle.setIconTextGap(8);
        pnlOuter.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;
        
        cboGaDiFilter = buildCombo(getGaListWithEmpty());
        cboGaDenFilter = buildCombo(getGaListWithEmpty());
        cboTauFilter = buildCombo(getTauListWithEmpty());
        dcNgayDi = new JDateChooser();
        dcNgayDi.setDateFormatString("dd/MM/yyyy");
        dcNgayDi.setPreferredSize(new Dimension(128, 30));
        dcNgayDi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dcNgayDi.setBackground(GuiTheme.SEARCH_FIELD_BG);
        dcNgayDi.setBorder(new LineBorder(BORDER, 1, true));
        
        gbc.gridx = 0; pnlGrid.add(buildField("Ga đi:", cboGaDiFilter), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("Ga đến:", cboGaDenFilter), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Tàu di chuyển:", cboTauFilter), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Ngày khởi hành:", dcNgayDi), gbc);
        
        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlAction.setOpaque(false);
        pnlAction.setBorder(new EmptyBorder(0, 0, 5, 0));

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        btnSearch.setIcon(GuiIcons.loadIcon(QuanLyChuyenTauGUI.class, "/Images/traCuu.png", 16, 16));
        
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        btnReset.setIcon(GuiIcons.loadIcon(QuanLyChuyenTauGUI.class, "/Images/logoLammoi.png", 16, 16));

        btnSearch.addActionListener(e -> {
            loadDataToTable();
            loadIdleTrains();
        });

        btnReset.addActionListener(e -> {
            cboGaDiFilter.setSelectedIndex(0);
            cboGaDenFilter.setSelectedIndex(0);
            cboTauFilter.setSelectedIndex(0);
            dcNgayDi.setDate(null);
            loadDataToTable();
            loadIdleTrains();
        });

        pnlAction.add(btnSearch);
        pnlAction.add(btnReset);
        pnlOuter.add(pnlAction, BorderLayout.SOUTH);
        
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

    private JComboBox<String> buildCombo(String[] values) {
        JComboBox<String> cmb = new JComboBox<>(values);
        cmb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cmb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cmb.setForeground(GuiTheme.TEXT);
        cmb.setBorder(new LineBorder(BORDER, 1, true));
        cmb.setPreferredSize(new Dimension(128, 30));
        return cmb;
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
        btn.setPreferredSize(new Dimension(150, 32));
        btn.setForeground(Color.WHITE);
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        return btn;
    }

    private JPanel buildIdleTrainsSection() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitle.setOpaque(false);
        JLabel lbl = new JLabel("Danh sách tàu tại ga (Đang rảnh)");
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

        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        java.util.Date selectedDate = dcNgayDi.getDate();
        String sql;
        PreparedStatement stmt = null;
        
        try {
            if (selectedDate != null) {
                // Tàu chưa có lịch di chuyển vào ngày này
                sql = "SELECT t.* FROM Tau t " +
                      "WHERE t.trangThai = N'Đang hoạt động' " +
                      "AND NOT EXISTS ( " +
                      "    SELECT 1 FROM ChuyenTau ct " +
                      "    JOIN ChiTietChuyenTau cct ON ct.maChuyenTau = cct.maChuyenTau " +
                      "    WHERE ct.maTau = t.maTau " +
                      "      AND CAST(cct.thoiGianKhoiHanh AS DATE) = ? " +
                      ")";
                stmt = conn.prepareStatement(sql);
                stmt.setDate(1, new java.sql.Date(selectedDate.getTime()));
            } else {
                // Mặc định hiện các tàu đang rảnh trong tương lai
                sql = "SELECT t.* FROM Tau t " +
                      "WHERE t.trangThai = N'Đang hoạt động' " +
                      "AND NOT EXISTS ( " +
                      "    SELECT 1 FROM ChuyenTau ct " +
                      "    JOIN ChiTietChuyenTau cct ON ct.maChuyenTau = cct.maChuyenTau " +
                      "    WHERE ct.maTau = t.maTau " +
                      "      AND cct.thoiGianKhoiHanh > GETDATE() " +
                      ")";
                stmt = conn.prepareStatement(sql);
            }

            ResultSet rs = stmt.executeQuery();
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
                JLabel lbl = new JLabel("Hiện không có tàu nào rảnh phù hợp.");
                lbl.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 13));
                lbl.setForeground(GuiTheme.SUB_TEXT);
                pnlIdleCards.add(lbl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignored) {}
            }
        }

        pnlIdleCards.revalidate();
        pnlIdleCards.repaint();
    }

    private void showDieuDongDialog(String maTau, String tenTau) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Điều động tàu: " + tenTau, true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        
        Font labelFont = GuiTheme.font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = GuiTheme.font("Segoe UI", Font.PLAIN, 14);
        Color fieldBg = new Color(248, 250, 252);
        
        java.util.function.Function<JComponent, Void> styleField = comp -> {
            comp.setFont(fieldFont);
            comp.setBackground(fieldBg);
            if (comp instanceof JComboBox) ((JComboBox<?>)comp).setBorder(new LineBorder(BORDER, 1, true));
            if (comp instanceof JTextField) {
                ((JTextField)comp).setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true), new EmptyBorder(4, 8, 4, 8)));
            }
            return null;
        };

        // Tàu di chuyển
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblTau = new JLabel("Tàu di chuyển:"); lblTau.setFont(labelFont); lblTau.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblTau, gbc);
        JTextField txtTau = new JTextField(tenTau);
        txtTau.setEditable(false); styleField.apply(txtTau);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(txtTau, gbc);
        
        // Ga đi
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblGaDi = new JLabel("Ga đi:"); lblGaDi.setFont(labelFont); lblGaDi.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblGaDi, gbc);
        JComboBox<String> cboGaDiPopup = new JComboBox<>(getGaList()); styleField.apply(cboGaDiPopup);
        cboGaDiPopup.setPreferredSize(new Dimension(0, 32));
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(cboGaDiPopup, gbc);

        // Ga đến
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblGaDen = new JLabel("Ga đến:"); lblGaDen.setFont(labelFont); lblGaDen.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblGaDen, gbc);
        JComboBox<String> cboGaDenPopup = new JComboBox<>(getGaList()); styleField.apply(cboGaDenPopup);
        cboGaDenPopup.setPreferredSize(new Dimension(0, 32));
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(cboGaDenPopup, gbc);
        
        // Khởi hành
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblDi = new JLabel("Khởi hành:"); lblDi.setFont(labelFont); lblDi.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblDi, gbc);
        
        JPanel pnlTimeDi = new JPanel(new BorderLayout(8, 0)); pnlTimeDi.setOpaque(false);
        JDateChooser dcDi = new JDateChooser(); dcDi.setDateFormatString("dd/MM/yyyy"); styleField.apply(dcDi);
        if (dcNgayDi.getDate() != null) dcDi.setDate(dcNgayDi.getDate());
        JSpinner spDiTime = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor deDi = new JSpinner.DateEditor(spDiTime, "HH:mm"); spDiTime.setEditor(deDi); styleField.apply(spDiTime);
        pnlTimeDi.add(dcDi, BorderLayout.CENTER); pnlTimeDi.add(spDiTime, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(pnlTimeDi, gbc);
        
        // Dự kiến đến
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblDen = new JLabel("Dự kiến đến:"); lblDen.setFont(labelFont); lblDen.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblDen, gbc);
        
        JPanel pnlTimeDen = new JPanel(new BorderLayout(8, 0)); pnlTimeDen.setOpaque(false);
        JDateChooser dcDen = new JDateChooser(); dcDen.setDateFormatString("dd/MM/yyyy"); styleField.apply(dcDen);
        if (dcNgayDi.getDate() != null) dcDen.setDate(dcNgayDi.getDate());
        JSpinner spDenTime = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor deDen = new JSpinner.DateEditor(spDenTime, "HH:mm"); spDenTime.setEditor(deDen); styleField.apply(spDenTime);
        pnlTimeDen.add(dcDen, BorderLayout.CENTER); pnlTimeDen.add(spDenTime, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(pnlTimeDen, gbc);

        // Buttons
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtn.setBackground(new Color(245, 247, 250));
        pnlBtn.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton btnSave = new JButton("Xác nhận Điều động") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(27, 94, 32) : getModel().isRollover() ? new Color(60, 145, 65) : new Color(46, 125, 50));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSave.setPreferredSize(new Dimension(170, 36));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btnSave.setContentAreaFilled(false); btnSave.setBorderPainted(false); btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton btnCancel = new JButton("Hủy") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(200, 200, 200) : getModel().isRollover() ? new Color(220, 220, 220) : Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.setColor(new Color(180, 180, 180));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnCancel.setForeground(GuiTheme.TEXT);
        btnCancel.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btnCancel.setContentAreaFilled(false); btnCancel.setBorderPainted(false); btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String gaDi = (String) cboGaDiPopup.getSelectedItem();
            String gaDen = (String) cboGaDenPopup.getSelectedItem();
            if (gaDi == null || gaDen == null || gaDi.equals(gaDen)) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn ga đi và ga đến hợp lệ (phải khác nhau)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.util.Date dateDi = dcDi.getDate();
            java.util.Date timeDi = (java.util.Date) spDiTime.getValue();
            java.util.Date dateDen = dcDen.getDate();
            java.util.Date timeDen = (java.util.Date) spDenTime.getValue();
            
            if (dateDi == null || dateDen == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn ngày giờ khởi hành và ngày giờ dự kiến đến!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.util.Calendar calDi = java.util.Calendar.getInstance(); calDi.setTime(dateDi);
            java.util.Calendar tDi = java.util.Calendar.getInstance(); tDi.setTime(timeDi);
            calDi.set(java.util.Calendar.HOUR_OF_DAY, tDi.get(java.util.Calendar.HOUR_OF_DAY));
            calDi.set(java.util.Calendar.MINUTE, tDi.get(java.util.Calendar.MINUTE));
            calDi.set(java.util.Calendar.SECOND, 0);
            
            java.util.Calendar calDen = java.util.Calendar.getInstance(); calDen.setTime(dateDen);
            java.util.Calendar tDen = java.util.Calendar.getInstance(); tDen.setTime(timeDen);
            calDen.set(java.util.Calendar.HOUR_OF_DAY, tDen.get(java.util.Calendar.HOUR_OF_DAY));
            calDen.set(java.util.Calendar.MINUTE, tDen.get(java.util.Calendar.MINUTE));
            calDen.set(java.util.Calendar.SECOND, 0);
            
            Timestamp tsDi = new Timestamp(calDi.getTimeInMillis());
            Timestamp tsDen = new Timestamp(calDen.getTimeInMillis());
            
            if (!tsDen.after(tsDi)) {
                JOptionPane.showMessageDialog(dialog, "Thời gian dự kiến đến phải sau thời gian khởi hành!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String maChuyenTau = "CT" + System.currentTimeMillis();
            if (maChuyenTau.length() > 20) maChuyenTau = maChuyenTau.substring(0, 20);
            
            if (saveDieuDong(maChuyenTau, maTau, getMaGa(gaDi), getMaGa(gaDen), tsDi, tsDen)) {
                JOptionPane.showMessageDialog(dialog, "Điều động tàu thành công!\nChuyến tàu " + maChuyenTau + " đã được lên lịch.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDataToTable();
                loadIdleTrains();
            } else {
                JOptionPane.showMessageDialog(dialog, "Có lỗi xảy ra khi lưu vào cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        pnlBtn.add(btnCancel);
        pnlBtn.add(btnSave);
        
        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean saveDieuDong(String maChuyenTau, String maTau, String maGaDi, String maGaDen, Timestamp tsDi, Timestamp tsDen) {
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return false;
        try {
            conn.setAutoCommit(false);
            String sql1 = "INSERT INTO ChuyenTau(maChuyenTau, ghiChu, maTau, trangThai) VALUES (?, ?, ?, ?)";
            PreparedStatement pst1 = conn.prepareStatement(sql1);
            pst1.setString(1, maChuyenTau);
            pst1.setString(2, "Điều động mới");
            pst1.setString(3, maTau);
            pst1.setString(4, "CHUAN_BI");
            pst1.executeUpdate();
            
            String sql2 = "INSERT INTO ChiTietChuyenTau(maChuyenTau, thoiGianKhoiHanh, thoiGianDuKien, maGaDi, maGaDen) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst2 = conn.prepareStatement(sql2);
            pst2.setString(1, maChuyenTau);
            pst2.setTimestamp(2, tsDi);
            pst2.setTimestamp(3, tsDen);
            pst2.setString(4, maGaDi);
            pst2.setString(5, maGaDen);
            pst2.executeUpdate();
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch(Exception ex){}
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch(Exception ex){}
        }
    }

    private JPanel buildTablePanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
        pnlOuter.setOpaque(false);
        
        JPanel pnlTitleWrap = new JPanel(new BorderLayout());
        pnlTitleWrap.setOpaque(false);
        pnlTitleWrap.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLeft.setOpaque(false);
        JLabel lb = new JLabel("Danh sách chuyến tàu");
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lb.setForeground(Color.WHITE);
        lb.setOpaque(true);
        lb.setBackground(PRIMARY);
        lb.setBorder(new EmptyBorder(6, 12, 6, 12));
        pnlLeft.add(lb);
        pnlTitleWrap.add(pnlLeft, BorderLayout.WEST);

        JPanel pnlTableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlTableActions.setOpaque(false);
        JButton btnUpdate = buildNavyButton("Cập nhật chuyến đi", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        btnUpdate.setPreferredSize(new Dimension(160, 32));
        JButton btnDelete = buildNavyButton("Xóa chuyến đi", new Color(220, 53, 69), new Color(200, 35, 51));
        btnDelete.setPreferredSize(new Dimension(140, 32));
        
        btnDelete.addActionListener(e -> deleteSelectedTrip());
        btnUpdate.addActionListener(e -> updateSelectedTrip());
        
        pnlTableActions.add(btnUpdate);
        pnlTableActions.add(btnDelete);
        pnlTitleWrap.add(pnlTableActions, BorderLayout.EAST);

        pnlOuter.add(pnlTitleWrap, BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
            new Object[] { "STT", "Ga đi", "Ga đến", "Ngày khởi hành", "Ngày đến dự kiến", "Giờ đi - Giờ đến", "Tên tàu", "Trạng thái", "maChuyenTau" },
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblData = new JTable(tblModel);
        tblData.setRowHeight(32);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.setForeground(GuiTheme.TEXT);
        
        // Hide the internal "maChuyenTau" column
        tblData.getColumnModel().removeColumn(tblData.getColumnModel().getColumn(8));
        
        tblData.setShowVerticalLines(false);
        tblData.setShowHorizontalLines(true);
        tblData.setGridColor(new Color(240, 240, 240));
        tblData.setIntercellSpacing(new Dimension(0, 1));
        
        tblData.setSelectionBackground(new Color(230, 242, 255));
        tblData.setSelectionForeground(GuiTheme.NAVY);
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblData.getTableHeader().setBackground(Color.WHITE);
        tblData.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isSelected) {
                    c.setBackground(new Color(230, 242, 255));
                    c.setForeground(GuiTheme.NAVY);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    c.setForeground(GuiTheme.TEXT);
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
            if (tblData.getColumnModel().getColumnCount() >= 8) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(50);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(150);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(7).setPreferredWidth(120);
            }
        });
        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private void deleteSelectedTrip() {
        int row = tblData.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một chuyến đi trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tblData.convertRowIndexToModel(row);
        String maChuyenTau = (String) tblModel.getValueAt(modelRow, 8);
        String tenTau = (String) tblModel.getValueAt(modelRow, 6);
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa chuyến đi của tàu " + tenTau + "?\nHành động này không thể hoàn tác.", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        try {
            conn.setAutoCommit(false);
            
            String sql1 = "DELETE FROM ChiTietChuyenTau WHERE maChuyenTau = ?";
            PreparedStatement pst1 = conn.prepareStatement(sql1);
            pst1.setString(1, maChuyenTau);
            pst1.executeUpdate();
            
            String sql2 = "DELETE FROM ChuyenTau WHERE maChuyenTau = ?";
            PreparedStatement pst2 = conn.prepareStatement(sql2);
            pst2.setString(1, maChuyenTau);
            pst2.executeUpdate();
            
            conn.commit();
            JOptionPane.showMessageDialog(this, "Xóa chuyến đi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadDataToTable();
            loadIdleTrains();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch(Exception ignored){}
            JOptionPane.showMessageDialog(this, "Không thể xóa chuyến đi này vì đã có vé hoặc hóa đơn liên kết với chuyến đi này!", "Lỗi Xóa", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { conn.setAutoCommit(true); } catch(Exception ignored){}
        }
    }

    private void updateSelectedTrip() {
        int row = tblData.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một chuyến đi trên bảng để cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tblData.convertRowIndexToModel(row);
        String maChuyenTau = (String) tblModel.getValueAt(modelRow, 8);
        
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        
        try {
            String sql = "SELECT ct.maTau, t.tenTau, dt.maGaDi, dt.maGaDen, dt.thoiGianKhoiHanh, dt.thoiGianDuKien, ct.trangThai " +
                         "FROM ChuyenTau ct " +
                         "JOIN ChiTietChuyenTau dt ON ct.maChuyenTau = dt.maChuyenTau " +
                         "JOIN Tau t ON ct.maTau = t.maTau " +
                         "WHERE ct.maChuyenTau = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, maChuyenTau);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String maTau = rs.getString("maTau");
                String tenTau = rs.getString("tenTau");
                String maGaDi = rs.getString("maGaDi");
                String maGaDen = rs.getString("maGaDen");
                Timestamp thoiGianKhoiHanh = rs.getTimestamp("thoiGianKhoiHanh");
                Timestamp thoiGianDuKien = rs.getTimestamp("thoiGianDuKien");
                String trangThai = rs.getString("trangThai");
                
                showUpdateDialog(maChuyenTau, maTau, tenTau, maGaDi, maGaDen, thoiGianKhoiHanh, thoiGianDuKien, trangThai);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showUpdateDialog(String maChuyenTau, String maTau, String tenTau, String maGaDi, String maGaDen, Timestamp thoiGianKhoiHanh, Timestamp thoiGianDuKien, String trangThai) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cập nhật chuyến tàu: " + tenTau, true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        
        Font labelFont = GuiTheme.font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = GuiTheme.font("Segoe UI", Font.PLAIN, 14);
        Color fieldBg = new Color(248, 250, 252);
        
        java.util.function.Function<JComponent, Void> styleField = comp -> {
            comp.setFont(fieldFont); comp.setBackground(fieldBg);
            if (comp instanceof JComboBox) ((JComboBox<?>)comp).setBorder(new LineBorder(BORDER, 1, true));
            if (comp instanceof JTextField) ((JTextField)comp).setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(4, 8, 4, 8)));
            return null;
        };

        // Tàu di chuyển
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblTau = new JLabel("Tàu di chuyển:"); lblTau.setFont(labelFont); lblTau.setForeground(GuiTheme.NAVY); pnlForm.add(lblTau, gbc);
        JTextField txtTau = new JTextField(tenTau); txtTau.setEditable(false); styleField.apply(txtTau);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlForm.add(txtTau, gbc);
        
        // Ga đi
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblGaDi = new JLabel("Ga đi:"); lblGaDi.setFont(labelFont); lblGaDi.setForeground(GuiTheme.NAVY); pnlForm.add(lblGaDi, gbc);
        JComboBox<String> cboGaDiPopup = new JComboBox<>(getGaList()); styleField.apply(cboGaDiPopup);
        cboGaDiPopup.setPreferredSize(new Dimension(0, 32));
        for (String tenGa : getGaList()) { if (getMaGa(tenGa).equals(maGaDi)) cboGaDiPopup.setSelectedItem(tenGa); }
        gbc.gridx = 1; gbc.weightx = 0.7; pnlForm.add(cboGaDiPopup, gbc);

        // Ga đến
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblGaDen = new JLabel("Ga đến:"); lblGaDen.setFont(labelFont); lblGaDen.setForeground(GuiTheme.NAVY); pnlForm.add(lblGaDen, gbc);
        JComboBox<String> cboGaDenPopup = new JComboBox<>(getGaList()); styleField.apply(cboGaDenPopup);
        cboGaDenPopup.setPreferredSize(new Dimension(0, 32));
        for (String tenGa : getGaList()) { if (getMaGa(tenGa).equals(maGaDen)) cboGaDenPopup.setSelectedItem(tenGa); }
        gbc.gridx = 1; gbc.weightx = 0.7; pnlForm.add(cboGaDenPopup, gbc);
        
        // Khởi hành
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblDi = new JLabel("Khởi hành:"); lblDi.setFont(labelFont); lblDi.setForeground(GuiTheme.NAVY); pnlForm.add(lblDi, gbc);
        JPanel pnlTimeDi = new JPanel(new BorderLayout(8, 0)); pnlTimeDi.setOpaque(false);
        JDateChooser dcDi = new JDateChooser(); dcDi.setDateFormatString("dd/MM/yyyy"); styleField.apply(dcDi); dcDi.setDate(new java.util.Date(thoiGianKhoiHanh.getTime()));
        JSpinner spDiTime = new JSpinner(new SpinnerDateModel()); JSpinner.DateEditor deDi = new JSpinner.DateEditor(spDiTime, "HH:mm"); spDiTime.setEditor(deDi); styleField.apply(spDiTime);
        spDiTime.setValue(new java.util.Date(thoiGianKhoiHanh.getTime()));
        pnlTimeDi.add(dcDi, BorderLayout.CENTER); pnlTimeDi.add(spDiTime, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlForm.add(pnlTimeDi, gbc);
        
        // Dự kiến đến
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblDen = new JLabel("Dự kiến đến:"); lblDen.setFont(labelFont); lblDen.setForeground(GuiTheme.NAVY); pnlForm.add(lblDen, gbc);
        JPanel pnlTimeDen = new JPanel(new BorderLayout(8, 0)); pnlTimeDen.setOpaque(false);
        JDateChooser dcDen = new JDateChooser(); dcDen.setDateFormatString("dd/MM/yyyy"); styleField.apply(dcDen); dcDen.setDate(new java.util.Date(thoiGianDuKien.getTime()));
        JSpinner spDenTime = new JSpinner(new SpinnerDateModel()); JSpinner.DateEditor deDen = new JSpinner.DateEditor(spDenTime, "HH:mm"); spDenTime.setEditor(deDen); styleField.apply(spDenTime);
        spDenTime.setValue(new java.util.Date(thoiGianDuKien.getTime()));
        pnlTimeDen.add(dcDen, BorderLayout.CENTER); pnlTimeDen.add(spDenTime, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlForm.add(pnlTimeDen, gbc);

        // Trạng thái
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        JLabel lblTrangThai = new JLabel("Trạng thái:"); lblTrangThai.setFont(labelFont); lblTrangThai.setForeground(GuiTheme.NAVY); pnlForm.add(lblTrangThai, gbc);
        
        JComboBox<String> cboTrangThai = new JComboBox<>(); styleField.apply(cboTrangThai);
        cboTrangThai.setPreferredSize(new Dimension(0, 32));
        java.util.Map<String, String> mapTrangThai = new java.util.LinkedHashMap<>();
        mapTrangThai.put("Chuẩn bị", "CHUAN_BI");
        mapTrangThai.put("Đang chạy", "DANG_CHAY");
        mapTrangThai.put("Đã đến", "DA_DEN");
        mapTrangThai.put("Bị hủy", "HUY");
        for (String tt : mapTrangThai.keySet()) cboTrangThai.addItem(tt);
        
        String displayStatus = "Chuẩn bị";
        if (trangThai != null) {
            for (java.util.Map.Entry<String, String> entry : mapTrangThai.entrySet()) {
                if (entry.getValue().equals(trangThai)) { displayStatus = entry.getKey(); break; }
            }
        }
        cboTrangThai.setSelectedItem(displayStatus);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlForm.add(cboTrangThai, gbc);

        // Buttons
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtn.setBackground(new Color(245, 247, 250));
        pnlBtn.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton btnSave = new JButton("Lưu Thay Đổi") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(27, 94, 32) : getModel().isRollover() ? new Color(60, 145, 65) : new Color(46, 125, 50));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSave.setPreferredSize(new Dimension(140, 36));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btnSave.setContentAreaFilled(false); btnSave.setBorderPainted(false); btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton btnCancel = new JButton("Hủy") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(200, 200, 200) : getModel().isRollover() ? new Color(220, 220, 220) : Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.setColor(new Color(180, 180, 180));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnCancel.setForeground(GuiTheme.TEXT);
        btnCancel.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btnCancel.setContentAreaFilled(false); btnCancel.setBorderPainted(false); btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String gaDi = (String) cboGaDiPopup.getSelectedItem();
            String gaDen = (String) cboGaDenPopup.getSelectedItem();
            if (gaDi == null || gaDen == null || gaDi.equals(gaDen)) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn ga đi và ga đến hợp lệ (phải khác nhau)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.util.Date dateDi = dcDi.getDate();
            java.util.Date timeDi = (java.util.Date) spDiTime.getValue();
            java.util.Date dateDen = dcDen.getDate();
            java.util.Date timeDen = (java.util.Date) spDenTime.getValue();
            
            if (dateDi == null || dateDen == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn ngày giờ khởi hành và ngày giờ dự kiến đến!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.util.Calendar calDi = java.util.Calendar.getInstance(); calDi.setTime(dateDi);
            java.util.Calendar tDi = java.util.Calendar.getInstance(); tDi.setTime(timeDi);
            calDi.set(java.util.Calendar.HOUR_OF_DAY, tDi.get(java.util.Calendar.HOUR_OF_DAY));
            calDi.set(java.util.Calendar.MINUTE, tDi.get(java.util.Calendar.MINUTE));
            calDi.set(java.util.Calendar.SECOND, 0);
            
            java.util.Calendar calDen = java.util.Calendar.getInstance(); calDen.setTime(dateDen);
            java.util.Calendar tDen = java.util.Calendar.getInstance(); tDen.setTime(timeDen);
            calDen.set(java.util.Calendar.HOUR_OF_DAY, tDen.get(java.util.Calendar.HOUR_OF_DAY));
            calDen.set(java.util.Calendar.MINUTE, tDen.get(java.util.Calendar.MINUTE));
            calDen.set(java.util.Calendar.SECOND, 0);
            
            Timestamp tsDi = new Timestamp(calDi.getTimeInMillis());
            Timestamp tsDen = new Timestamp(calDen.getTimeInMillis());
            
            if (!tsDen.after(tsDi)) {
                JOptionPane.showMessageDialog(dialog, "Thời gian dự kiến đến phải sau thời gian khởi hành!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String selectedTT = mapTrangThai.get((String) cboTrangThai.getSelectedItem());
            
            Connection con = Connect_DB.getInstance().getConnection();
            try {
                con.setAutoCommit(false);
                String u1 = "UPDATE ChuyenTau SET trangThai = ? WHERE maChuyenTau = ?";
                PreparedStatement pu1 = con.prepareStatement(u1);
                pu1.setString(1, selectedTT);
                pu1.setString(2, maChuyenTau);
                pu1.executeUpdate();
                
                String u2 = "UPDATE ChiTietChuyenTau SET thoiGianKhoiHanh = ?, thoiGianDuKien = ?, maGaDi = ?, maGaDen = ? WHERE maChuyenTau = ?";
                PreparedStatement pu2 = con.prepareStatement(u2);
                pu2.setTimestamp(1, tsDi);
                pu2.setTimestamp(2, tsDen);
                pu2.setString(3, getMaGa(gaDi));
                pu2.setString(4, getMaGa(gaDen));
                pu2.setString(5, maChuyenTau);
                pu2.executeUpdate();
                
                con.commit();
                JOptionPane.showMessageDialog(dialog, "Cập nhật chuyến đi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDataToTable();
                loadIdleTrains();
            } catch (SQLException ex) {
                try { con.rollback(); } catch(Exception ignored){}
                JOptionPane.showMessageDialog(dialog, "Có lỗi xảy ra khi cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } finally {
                try { con.setAutoCommit(true); } catch(Exception ignored){}
            }
        });
        
        pnlBtn.add(btnCancel);
        pnlBtn.add(btnSave);
        
        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadDataToTable() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT ct.maChuyenTau, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
                         "dt.thoiGianKhoiHanh, dt.thoiGianDuKien, t.tenTau, ct.trangThai " +
                         "FROM ChuyenTau ct " +
                         "JOIN ChiTietChuyenTau dt ON ct.maChuyenTau = dt.maChuyenTau " +
                         "JOIN Ga gDi ON dt.maGaDi = gDi.maGa " +
                         "JOIN Ga gDen ON dt.maGaDen = gDen.maGa " +
                         "JOIN Tau t ON ct.maTau = t.maTau " +
                         "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? AND t.tenTau LIKE ?";
            
            if (dcNgayDi.getDate() != null) {
                sql += " AND CAST(dt.thoiGianKhoiHanh AS DATE) = ?";
            }
            
            sql += " ORDER BY dt.thoiGianKhoiHanh DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String filterGaDi = (cboGaDiFilter.getSelectedItem() != null) ? cboGaDiFilter.getSelectedItem().toString() : "";
            String filterGaDen = (cboGaDenFilter.getSelectedItem() != null) ? cboGaDenFilter.getSelectedItem().toString() : "";
            String filterTau = (cboTauFilter.getSelectedItem() != null) ? cboTauFilter.getSelectedItem().toString() : "";
            
            stmt.setString(1, "%" + filterGaDi + "%");
            stmt.setString(2, "%" + filterGaDen + "%");
            stmt.setString(3, "%" + filterTau + "%");
            
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

                String dbStatus = rs.getString("trangThai");
                String localizedStatus = dbStatus;
                if (dbStatus != null) {
                    switch (dbStatus) {
                        case "CHUAN_BI": localizedStatus = "Chuẩn bị"; break;
                        case "DANG_CHAY": localizedStatus = "Đang chạy"; break;
                        case "DA_DEN": localizedStatus = "Đã đến"; break;
                        case "HUY": localizedStatus = "Bị hủy"; break;
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
                    localizedStatus,
                    rs.getString("maChuyenTau")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            
            btnAction.addActionListener(e -> showDieuDongDialog(id, name));

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
