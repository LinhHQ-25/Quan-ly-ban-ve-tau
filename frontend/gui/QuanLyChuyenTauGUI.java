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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        pnlPage.setLayout(new BorderLayout(0, 8));
        pnlPage.setBorder(new EmptyBorder(
            -9,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));

        // Phần Filter ở trên cùng
        pnlPage.add(buildFilterPanel(), BorderLayout.NORTH);

        // Phần Center chứa IdleCards và Table
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 8));
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
        // Ga: giữ thứ tự địa lý theo tuyến Bắc-Nam
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        try {
            String sqlGa = "SELECT maGa, tenGa FROM Ga ORDER BY " +
                "CASE tenGa " +
                "WHEN N'Hà Nội' THEN 1 " +
                "WHEN N'Phủ Lý' THEN 2 " +
                "WHEN N'Nam Định' THEN 3 " +
                "WHEN N'Ninh Bình' THEN 4 " +
                "WHEN N'Thanh Hóa' THEN 5 " +
                "WHEN N'Vinh' THEN 6 " +
                "WHEN N'Đồng Hới' THEN 7 " +
                "WHEN N'Đông Hà' THEN 8 " +
                "WHEN N'Huế' THEN 9 " +
                "WHEN N'Đà Nẵng' THEN 10 " +
                "WHEN N'Tam Kỳ' THEN 11 " +
                "WHEN N'Quảng Ngãi' THEN 12 " +
                "WHEN N'Diêu Trì' THEN 13 " +
                "WHEN N'Tuy Hòa' THEN 14 " +
                "WHEN N'Nha Trang' THEN 15 " +
                "WHEN N'Tháp Chàm' THEN 16 " +
                "WHEN N'Bình Thuận' THEN 17 " +
                "WHEN N'Long Khánh' THEN 18 " +
                "WHEN N'Biên Hòa' THEN 19 " +
                "WHEN N'Dĩ An' THEN 20 " +
                "WHEN N'Sài Gòn' THEN 21 " +
                "ELSE 22 END ASC";
            ResultSet rsGa = conn.createStatement().executeQuery(sqlGa);
            while (rsGa.next())
                mapGa.put(rsGa.getString("tenGa"), rsGa.getString("maGa"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Tàu: dùng DAO
        new dao.tauDAO().selectAll()
            .forEach(tau -> listTau.add(tau.getTenTau()));
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

    private JPanel buildSearchBlock() {
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);
        
        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        btnSearch.setIcon(GuiIcons.loadIcon(QuanLyChuyenTauGUI.class, "/Images/traCuu.png", 16, 16));
        btnSearch.setPreferredSize(new Dimension(120, 32));
        
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        btnReset.setIcon(GuiIcons.loadIcon(QuanLyChuyenTauGUI.class, "/Images/logoLammoi.png", 16, 16));
        btnReset.setPreferredSize(new Dimension(120, 32));

        btnSearch.addActionListener(e -> {
            loadDataToTable();
            loadIdleTrains();
        });

        btnReset.addActionListener(e -> {
            cboGaDiFilter.setSelectedItem("Diêu Trì");
            cboGaDenFilter.setSelectedIndex(0);
            cboTauFilter.setSelectedIndex(0);
            dcNgayDi.setDate(null);
            loadDataToTable();
            loadIdleTrains();
        });

        pnlButtons.add(btnSearch);
        pnlButtons.add(btnReset);
        return pnlButtons;
    }

    private JPanel buildFilterPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Integrated Header Title
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Thông tin tra cứu chuyến tàu");
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
        gbc.gridy = 0;
        
        cboGaDiFilter = buildCombo(getGaListWithEmpty());
        cboGaDiFilter.setSelectedItem("Diêu Trì");
        cboGaDiFilter.setEnabled(false);
        cboGaDenFilter = buildCombo(getGaListWithEmpty());
        cboTauFilter = buildCombo(getTauListWithEmpty());
        dcNgayDi = new JDateChooser();
        dcNgayDi.setDateFormatString("dd/MM/yyyy");
        dcNgayDi.setPreferredSize(new Dimension(128, 30));
        dcNgayDi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dcNgayDi.setBackground(GuiTheme.SEARCH_FIELD_BG);
        GuiTheme.setupRoundedComponent(dcNgayDi);
        
        gbc.gridx = 0; pnlGrid.add(buildField("Ga đi:", cboGaDiFilter), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("Ga đến:", cboGaDenFilter), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Tàu di chuyển:", cboTauFilter), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Ngày khởi hành:", dcNgayDi), gbc);
        
        pnlOuter.add(pnlGrid, BorderLayout.CENTER);

        // Buttons Row in SOUTH
        pnlOuter.add(buildSearchBlock(), BorderLayout.SOUTH);
        
        return pnlOuter;
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

    private JComboBox<String> buildCombo(String[] values) {
        JComboBox<String> cmb = new JComboBox<>(values);
        cmb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cmb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cmb.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(cmb);
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
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Integrated Header
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách tàu tại ga");
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

        pnlIdleCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlIdleCards.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(pnlIdleCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 115));
        scroll.getHorizontalScrollBar().setUnitIncrement(15);
        
        pnlOuter.add(scroll, BorderLayout.CENTER);
        return pnlOuter;
    }
    private void loadIdleTrains() {
        if (pnlIdleCards == null) return;
        pnlIdleCards.removeAll();

        java.util.Date selectedDate = dcNgayDi.getDate();
        dao.tauDAO tauDao = new dao.tauDAO();

        java.util.List<Object[]> listTau = tauDao.getTauTrangThaiDong(
            selectedDate != null ? new java.sql.Date(selectedDate.getTime()) : null
        );

        boolean hasRanh = false;
        for (Object[] row : listTau) {
            String tinhTrang = (String) row[4];
            // Nếu người dùng chọn lọc ngày cụ thể, chỉ hiển thị những tàu "Sẵn sàng"
            if (selectedDate != null && !tinhTrang.equals("Sẵn sàng")) {
                continue;
            }
            
            pnlIdleCards.add(new TrainCard(
                (String) row[0],
                (String) row[1],
                (int) row[2],
                (int) row[3],
                tinhTrang
            ));
            hasRanh = true;
        }

        if (!hasRanh) {
            JLabel lbl = new JLabel("Hiện không có tàu nào rảnh phù hợp.");
            lbl.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 13));
            lbl.setForeground(GuiTheme.SUB_TEXT);
            pnlIdleCards.add(lbl);
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
        cboGaDiPopup.setSelectedItem("Diêu Trì");
        cboGaDiPopup.setEnabled(false);
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
        
        // Thiết lập giá trị mặc định từ bộ lọc tra cứu (nếu có)
        String filterGaDen = (String) cboGaDenFilter.getSelectedItem();
        if (filterGaDen != null && !filterGaDen.isEmpty()) {
            cboGaDenPopup.setSelectedItem(filterGaDen);
        }
        
        // Khởi hành
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblDi = new JLabel("Khởi hành:"); lblDi.setFont(labelFont); lblDi.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblDi, gbc);
        
        JPanel pnlTimeDi = new JPanel(new BorderLayout(8, 0)); pnlTimeDi.setOpaque(false);
        JDateChooser dcDi = new JDateChooser(); dcDi.setDateFormatString("dd/MM/yyyy"); styleField.apply(dcDi);
        if (dcNgayDi.getDate() != null) {
            dcDi.setDate(dcNgayDi.getDate());
        } else {
            dcDi.setDate(new java.util.Date());
        }
        JSpinner spDiTime = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor deDi = new JSpinner.DateEditor(spDiTime, "HH:mm"); spDiTime.setEditor(deDi); styleField.apply(spDiTime);
        pnlTimeDi.add(dcDi, BorderLayout.CENTER); pnlTimeDi.add(spDiTime, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(pnlTimeDi, gbc);
        
        // Dự kiến đến (Khóa chỉnh sửa, hệ thống tự động điền)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblDen = new JLabel("Dự kiến đến:"); lblDen.setFont(labelFont); lblDen.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblDen, gbc);
        
        JPanel pnlTimeDen = new JPanel(new BorderLayout(8, 0)); pnlTimeDen.setOpaque(false);
        JDateChooser dcDen = new JDateChooser(); dcDen.setDateFormatString("dd/MM/yyyy"); styleField.apply(dcDen);
        JSpinner spDenTime = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor deDen = new JSpinner.DateEditor(spDenTime, "HH:mm"); spDenTime.setEditor(deDen); styleField.apply(spDenTime);
        pnlTimeDen.add(dcDen, BorderLayout.CENTER); pnlTimeDen.add(spDenTime, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(pnlTimeDen, gbc);
        
        // Vô hiệu hóa chỉnh sửa thủ công cho thời gian dự kiến đến
        dcDen.setEnabled(false);
        spDenTime.setEnabled(false);
        
        // Đăng ký bộ lắng nghe tự động cập nhật thời gian đến dự kiến
        java.awt.event.ActionListener autoUpdateArrival = ev -> {
            capNhatThoiGianDenTuDong(cboGaDiPopup, cboGaDenPopup, dcDi, spDiTime, dcDen, spDenTime);
        };
        cboGaDiPopup.addActionListener(autoUpdateArrival);
        cboGaDenPopup.addActionListener(autoUpdateArrival);
        
        dcDi.getDateEditor().addPropertyChangeListener("date", ev -> {
            capNhatThoiGianDenTuDong(cboGaDiPopup, cboGaDenPopup, dcDi, spDiTime, dcDen, spDenTime);
        });
        
        spDiTime.addChangeListener(ev -> {
            capNhatThoiGianDenTuDong(cboGaDiPopup, cboGaDenPopup, dcDi, spDiTime, dcDen, spDenTime);
        });
        
        // Chạy tính toán tự động lần đầu
        capNhatThoiGianDenTuDong(cboGaDiPopup, cboGaDenPopup, dcDi, spDiTime, dcDen, spDenTime);

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
            
            // Kiểm tra ràng buộc nghỉ/bảo trì 5 tiếng sau chuyến chạy trước đó
            Timestamp tsDenGanNhat = new dao.ChuyenTauDAO().getThoiGianDenGanNhat(maTau, tsDi);
            if (tsDenGanNhat != null) {
                long fiveHoursInMillis = 5L * 3600 * 1000;
                if (tsDi.getTime() - tsDenGanNhat.getTime() < fiveHoursInMillis) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    String gapTimeStr = sdf.format(tsDenGanNhat);
                    String readyTimeStr = sdf.format(new Timestamp(tsDenGanNhat.getTime() + fiveHoursInMillis));
                    JOptionPane.showMessageDialog(dialog, 
                        "❌ Lỗi: Tàu đang trong thời gian bảo trì kỹ thuật và nghỉ dưỡng sau chuyến đi trước!\n"
                        + "- Về ga gần nhất lúc: " + gapTimeStr + "\n"
                        + "- Thời gian nghỉ tối thiểu: 5 tiếng\n"
                        + "- Thời gian sẵn sàng khởi hành tiếp theo: " + readyTimeStr,
                        "Cảnh báo bảo trì", JOptionPane.ERROR_MESSAGE);
                    return;
                }
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

    private boolean saveDieuDong(String maChuyenTau, String maTau,
            String maGaDi, String maGaDen,
            Timestamp tsDi, Timestamp tsDen) {
return new dao.ChuyenTauDAO().saveDieuDong(
maChuyenTau, maTau, maGaDi, maGaDen, tsDi, tsDen);
}

    private JPanel buildTablePanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Integrated Header Title
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách chuyến tàu");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(44, 82, 150));
        lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        
        JPanel pnlTableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlTableActions.setOpaque(false);
        
        JButton btnAutoSchedule = buildNavyButton("Tự động lập lịch", new Color(40, 167, 69), new Color(33, 136, 56));
        btnAutoSchedule.setPreferredSize(new Dimension(150, 32));
        btnAutoSchedule.addActionListener(e -> showAutoScheduleDialog());
        
        JButton btnUpdate = buildNavyButton("Cập nhật chuyến đi", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        btnUpdate.setPreferredSize(new Dimension(160, 32));
        
        JButton btnDelete = buildNavyButton("Xóa chuyến đi", new Color(220, 53, 69), new Color(200, 35, 51));
        btnDelete.setPreferredSize(new Dimension(140, 32));
        
        btnDelete.addActionListener(e -> deleteSelectedTrip());
        btnUpdate.addActionListener(e -> updateSelectedTrip());
        
        pnlTableActions.add(btnAutoSchedule);
        pnlTableActions.add(btnUpdate);
        pnlTableActions.add(btnDelete);
        
        pnlHeader.add(pnlTableActions, BorderLayout.EAST);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

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
        spnScroll.setBorder(null);
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
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một chuyến đi trên bảng để xóa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tblData.convertRowIndexToModel(row);
        String maChuyenTau = (String) tblModel.getValueAt(modelRow, 8);
        String tenTau      = (String) tblModel.getValueAt(modelRow, 6);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa chuyến đi của tàu " + tenTau
                + "?\nHành động này không thể hoàn tác.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = new dao.ChuyenTauDAO().deleteChuyenTau(maChuyenTau);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Xóa chuyến đi thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            loadDataToTable();
            loadIdleTrains();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa chuyến đi này vì đã có vé hoặc hóa đơn liên kết!",
                    "Lỗi Xóa", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedTrip() {
        int row = tblData.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một chuyến đi trên bảng để cập nhật!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tblData.convertRowIndexToModel(row);
        String maChuyenTau = (String) tblModel.getValueAt(modelRow, 8);

        Object[] data = new dao.ChuyenTauDAO().getChuyenTauForUpdate(maChuyenTau);
        if (data == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy thông tin chuyến tàu!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        showUpdateDialog(
            maChuyenTau,
            (String)    data[0],   // maTau
            (String)    data[1],   // tenTau
            (String)    data[2],   // maGaDi
            (String)    data[3],   // maGaDen
            (Timestamp) data[4],   // thoiGianKhoiHanh
            (Timestamp) data[5],   // thoiGianDuKien
            (String)    data[6]    // trangThai
        );
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
            
            boolean ok = new dao.ChuyenTauDAO().updateChuyenTau(
                    maChuyenTau,
                    getMaGa(gaDi), getMaGa(gaDen),
                    tsDi, tsDen,
                    mapTrangThai.get((String) cboTrangThai.getSelectedItem()));
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Cập nhật chuyến đi thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDataToTable();
                loadIdleTrains();
            } else {
                JOptionPane.showMessageDialog(dialog, "Có lỗi xảy ra khi cập nhật!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
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

        String filterGaDi  = cboGaDiFilter.getSelectedItem()  != null
                             ? cboGaDiFilter.getSelectedItem().toString()  : "";
        String filterGaDen = cboGaDenFilter.getSelectedItem() != null
                             ? cboGaDenFilter.getSelectedItem().toString() : "";
        String filterTau   = cboTauFilter.getSelectedItem()   != null
                             ? cboTauFilter.getSelectedItem().toString()   : "";
        java.sql.Date ngayDi = dcNgayDi.getDate() != null
                               ? new java.sql.Date(dcNgayDi.getDate().getTime()) : null;

        java.util.List<Object[]> rows = new dao.ChuyenTauDAO()
                .searchChuyenTau(filterGaDi, filterGaDen, filterTau, ngayDi);

        int stt = 1;
        for (Object[] r : rows) {
            tblModel.addRow(new Object[]{
                stt++,
                r[0],  // gaDi
                r[1],  // gaDen
                r[2],  // ngayDi
                r[3],  // ngayDen
                r[4],  // gioDiGioDen
                r[5],  // tenTau
                r[6],  // trangThaiLocalized
                r[7]   // maChuyenTau (ẩn)
            });
        }
    }

    private final class TrainCard extends JPanel {
        private boolean isHovered = false;

        TrainCard(String id, String name, int toa, int ghe, String tinhTrang) {
            setPreferredSize(new Dimension(130, 95));
            setBackground(Color.WHITE);
            setLayout(new BorderLayout());
            setBorder(null);

            JPanel pnlInfo = new JPanel();
            pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
            pnlInfo.setOpaque(false);
            pnlInfo.setBorder(new EmptyBorder(6, 10, 2, 10));

            JLabel lblId = new JLabel(id);
            lblId.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            lblId.setForeground(GuiTheme.NAVY);

            JLabel lblName = new JLabel(name);
            lblName.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
            lblName.setForeground(GuiTheme.TEXT);

            JLabel lblDetails = new JLabel(toa + " Toa | " + ghe + " Ghế");
            lblDetails.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
            lblDetails.setForeground(GuiTheme.SUB_TEXT);

            pnlInfo.add(lblId);
            pnlInfo.add(Box.createVerticalStrut(2));
            pnlInfo.add(lblName);
            pnlInfo.add(Box.createVerticalStrut(4));
            pnlInfo.add(lblDetails);

            boolean isReady = tinhTrang.equals("Sẵn sàng");
            String btnText = isReady ? "Điều động" : (tinhTrang.contains("Bảo trì") ? "Bảo trì" : tinhTrang);
            Color btnColor = isReady ? GuiTheme.NAVY 
                                     : (tinhTrang.equals("Đang chạy") || tinhTrang.equals("Bận chạy") ? new Color(220, 53, 69) : new Color(240, 173, 78));
            Color btnColorHover = isReady ? GuiTheme.NAVY_HOVER : btnColor;

            JButton btnAction = new JButton(btnText) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? btnColorHover : btnColor);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnAction.setForeground(Color.WHITE);
            btnAction.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 9));
            btnAction.setPreferredSize(new Dimension(0, 22));
            btnAction.setContentAreaFilled(false); btnAction.setBorderPainted(false); btnAction.setFocusPainted(false);
            
            if (isReady) {
                btnAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnAction.addActionListener(e -> showDieuDongDialog(id, name));
            } else {
                btnAction.setEnabled(false);
            }

            add(pnlInfo, BorderLayout.CENTER);
            add(btnAction, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color bgCard = isHovered ? new Color(248, 250, 255) : Color.WHITE;
            g2.setColor(bgCard);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            
            Color borderColor = isHovered ? new Color(120, 160, 240) : new Color(230, 233, 238);
            float borderStroke = isHovered ? 1.5f : 1.0f;
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderStroke));
            g2.drawRoundRect((int)(borderStroke/2), (int)(borderStroke/2), getWidth() - (int)borderStroke - 1, getHeight() - (int)borderStroke - 1, 12, 12);
            
            g2.dispose();
        }
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

    private void showAutoScheduleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tự động lập lịch định kỳ", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
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

        // 1. Tàu di chuyển
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblTau = new JLabel("Chọn tàu:"); lblTau.setFont(labelFont); lblTau.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblTau, gbc);
        
        java.util.List<String> listTenTau = new java.util.ArrayList<>();
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn != null) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT tenTau FROM Tau WHERE trangThai = N'Đang hoạt động'")) {
                while (rs.next()) {
                    listTenTau.add(rs.getString("tenTau"));
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
        if (listTenTau.isEmpty()) {
            listTenTau.add("Không có tàu khả dụng");
        }
        
        JComboBox<String> cboTauPopup = new JComboBox<>(listTenTau.toArray(new String[0])); styleField.apply(cboTauPopup);
        cboTauPopup.setPreferredSize(new Dimension(0, 32));
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(cboTauPopup, gbc);
        
        // 2. Ga đi
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblGaDi = new JLabel("Ga đi:"); lblGaDi.setFont(labelFont); lblGaDi.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblGaDi, gbc);
        JComboBox<String> cboGaDiPopup = new JComboBox<>(getGaList()); styleField.apply(cboGaDiPopup);
        cboGaDiPopup.setPreferredSize(new Dimension(0, 32));
        cboGaDiPopup.setSelectedItem("Diêu Trì");
        cboGaDiPopup.setEnabled(false);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(cboGaDiPopup, gbc);

        // 3. Ga đến
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblGaDen = new JLabel("Ga đến:"); lblGaDen.setFont(labelFont); lblGaDen.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblGaDen, gbc);
        JComboBox<String> cboGaDenPopup = new JComboBox<>(getGaList()); styleField.apply(cboGaDenPopup);
        cboGaDenPopup.setPreferredSize(new Dimension(0, 32));
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(cboGaDenPopup, gbc);
        
        // 4. Lập lịch từ ngày - đến ngày
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblTuNgay = new JLabel("Từ ngày:"); lblTuNgay.setFont(labelFont); lblTuNgay.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblTuNgay, gbc);
        
        JPanel pnlRange = new JPanel(new java.awt.GridLayout(1, 2, 10, 0)); pnlRange.setOpaque(false);
        JDateChooser dcTu = new JDateChooser(); dcTu.setDateFormatString("dd/MM/yyyy"); styleField.apply(dcTu);
        dcTu.setDate(new java.util.Date());
        JDateChooser dcD = new JDateChooser(); dcD.setDateFormatString("dd/MM/yyyy"); styleField.apply(dcD);
        dcD.setDate(new java.util.Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000));
        pnlRange.add(dcTu);
        pnlRange.add(dcD);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(pnlRange, gbc);
        
        // 5. Giờ khởi hành chuyến đầu
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblGioDi = new JLabel("Giờ chạy chuyến đầu:"); lblGioDi.setFont(labelFont); lblGioDi.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblGioDi, gbc);
        
        JSpinner spDiTime = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor deDi = new JSpinner.DateEditor(spDiTime, "HH:mm"); spDiTime.setEditor(deDi); styleField.apply(spDiTime);
        java.util.Calendar calDefault = java.util.Calendar.getInstance();
        calDefault.set(java.util.Calendar.HOUR_OF_DAY, 8); calDefault.set(java.util.Calendar.MINUTE, 0);
        spDiTime.setValue(calDefault.getTime());
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(spDiTime, gbc);
        
        // 6. Thời gian di chuyển (giờ)
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        JLabel lblTravel = new JLabel("Thời gian di chuyển (giờ):"); lblTravel.setFont(labelFont); lblTravel.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblTravel, gbc);
        JSpinner spTravel = new JSpinner(new SpinnerNumberModel(12, 1, 48, 1)); styleField.apply(spTravel);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(spTravel, gbc);
        
        // 7. Thời gian nghỉ tại ga đến (giờ)
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        JLabel lblRest = new JLabel("Thời gian nghỉ tại ga đến (giờ):"); lblRest.setFont(labelFont); lblRest.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblRest, gbc);
        JSpinner spRest = new JSpinner(new SpinnerNumberModel(5, 5, 24, 1)); styleField.apply(spRest);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(spRest, gbc);
        
        // 8. Thời gian nghỉ tại ga đi (giờ)
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.3;
        JLabel lblRestHome = new JLabel("Thời gian nghỉ tại ga đi (giờ):"); lblRestHome.setFont(labelFont); lblRestHome.setForeground(GuiTheme.NAVY);
        pnlForm.add(lblRestHome, gbc);
        JSpinner spRestHome = new JSpinner(new SpinnerNumberModel(5, 5, 24, 1)); styleField.apply(spRestHome);
        gbc.gridx = 1; gbc.weightx = 0.7;
        pnlForm.add(spRestHome, gbc);
        
        // Đăng ký bộ lắng nghe tự động cập nhật thời gian di chuyển trong Lập lịch định kỳ
        java.awt.event.ActionListener autoUpdateTravel = ev -> {
            String gaDi = (String) cboGaDiPopup.getSelectedItem();
            String gaDen = (String) cboGaDenPopup.getSelectedItem();
            if (gaDi != null && gaDen != null && !gaDi.equals(gaDen)) {
                double hrs = tinhThoiGianChay(gaDi, gaDen);
                spTravel.setValue((int) Math.round(hrs));
            }
        };
        cboGaDiPopup.addActionListener(autoUpdateTravel);
        cboGaDenPopup.addActionListener(autoUpdateTravel);
        
        // Chạy lần đầu để tính toán ngay
        String gaDiInit = (String) cboGaDiPopup.getSelectedItem();
        String gaDenInit = (String) cboGaDenPopup.getSelectedItem();
        if (gaDiInit != null && gaDenInit != null && !gaDiInit.equals(gaDenInit)) {
            double hrs = tinhThoiGianChay(gaDiInit, gaDenInit);
            spTravel.setValue((int) Math.round(hrs));
        }

        // Buttons
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtn.setBackground(new Color(245, 247, 250));
        pnlBtn.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton btnSave = new JButton("Tự động sinh lịch") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(27, 94, 32) : getModel().isRollover() ? new Color(60, 145, 65) : new Color(46, 125, 50));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSave.setPreferredSize(new Dimension(175, 36));
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
            String name = (String) cboTauPopup.getSelectedItem();
            String gaDi = (String) cboGaDiPopup.getSelectedItem();
            String gaDen = (String) cboGaDenPopup.getSelectedItem();
            if (name == null || gaDi == null || gaDen == null || gaDi.equals(gaDen)) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn tàu, ga đi và ga đến hợp lệ (phải khác nhau)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.util.Date dateTu = dcTu.getDate();
            java.util.Date dateDen = dcD.getDate();
            if (dateTu == null || dateDen == null || dateTu.after(dateDen)) {
                JOptionPane.showMessageDialog(dialog, "Ngày bắt đầu và kết thúc không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int travelHrs = (int) spTravel.getValue();
            int restHrs = (int) spRest.getValue(); // Nghỉ tại ga đến
            int restHomeHrs = (int) spRestHome.getValue(); // Nghỉ tại ga đi (ga nhà)
            java.util.Date timeDi = (java.util.Date) spDiTime.getValue();
            java.util.Calendar calTime = java.util.Calendar.getInstance(); calTime.setTime(timeDi);
            int hourDi = calTime.get(java.util.Calendar.HOUR_OF_DAY);
            int minuteDi = calTime.get(java.util.Calendar.MINUTE);
            
            String maTau = "";
            try (Connection c = Connect_DB.getInstance().getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT maTau FROM Tau WHERE tenTau = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) maTau = rs.getString("maTau");
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
            
            if (maTau.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Không tìm thấy thông tin tàu trong CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.util.List<entity.ChuyenTau> listChuyen = new java.util.ArrayList<>();
            java.util.List<entity.ChiTietChuyenTau> listChiTiet = new java.util.ArrayList<>();
            
            java.util.Calendar currentCursor = java.util.Calendar.getInstance();
            currentCursor.setTime(dateTu);
            currentCursor.set(java.util.Calendar.HOUR_OF_DAY, hourDi);
            currentCursor.set(java.util.Calendar.MINUTE, minuteDi);
            currentCursor.set(java.util.Calendar.SECOND, 0);
            currentCursor.set(java.util.Calendar.MILLISECOND, 0);
            
            java.util.Calendar limitTime = java.util.Calendar.getInstance();
            limitTime.setTime(dateDen);
            limitTime.set(java.util.Calendar.HOUR_OF_DAY, 23);
            limitTime.set(java.util.Calendar.MINUTE, 59);
            limitTime.set(java.util.Calendar.SECOND, 59);
            limitTime.set(java.util.Calendar.MILLISECOND, 999);
            
            entity.Tau tObj = new entity.Tau(); tObj.setMaTau(maTau);
            entity.Ga gaDiObj = new entity.Ga(); gaDiObj.setMaGa(getMaGa(gaDi));
            entity.Ga gaDenObj = new entity.Ga(); gaDenObj.setMaGa(getMaGa(gaDen));
            
            int tripCount = 0;
            dao.ChuyenTauDAO ctDao = new dao.ChuyenTauDAO();
            
            while (true) {
                java.util.Calendar calKhoiHanhOut = (java.util.Calendar) currentCursor.clone();
                java.util.Calendar calDuKienOut = (java.util.Calendar) calKhoiHanhOut.clone();
                calDuKienOut.add(java.util.Calendar.HOUR_OF_DAY, travelHrs);
                
                Timestamp tsKhoiHanhOut = new Timestamp(calKhoiHanhOut.getTimeInMillis());
                Timestamp tsDuKienOut = new Timestamp(calDuKienOut.getTimeInMillis());
                
                java.util.Calendar calKhoiHanhRet = (java.util.Calendar) calDuKienOut.clone();
                calKhoiHanhRet.add(java.util.Calendar.HOUR_OF_DAY, restHrs);
                java.util.Calendar calDuKienRet = (java.util.Calendar) calKhoiHanhRet.clone();
                calDuKienRet.add(java.util.Calendar.HOUR_OF_DAY, travelHrs);
                
                Timestamp tsKhoiHanhRet = new Timestamp(calKhoiHanhRet.getTimeInMillis());
                Timestamp tsDuKienRet = new Timestamp(calDuKienRet.getTimeInMillis());
                
                // Nếu thời gian về ga của chuyến khứ hồi này vượt quá ngày kết thúc được chọn, dừng sinh
                if (calDuKienRet.after(limitTime)) {
                    break;
                }
                
                // Kiểm tra xem tàu có bận chạy hay bảo trì trong các khoảng thời gian này không
                boolean isOutConflict = ctDao.isTauBanTrongKhoang(maTau, tsKhoiHanhOut, new Timestamp(tsDuKienOut.getTime() + 5L * 3600 * 1000));
                boolean isRetConflict = ctDao.isTauBanTrongKhoang(maTau, tsKhoiHanhRet, new Timestamp(tsDuKienRet.getTime() + 5L * 3600 * 1000));
                
                if (isOutConflict || isRetConflict) {
                    // Nếu xung đột lịch, bỏ qua cả chu kỳ khứ hồi này và nhảy sang chu kỳ tuần hoàn kế tiếp
                    currentCursor.add(java.util.Calendar.HOUR_OF_DAY, travelHrs + restHrs + travelHrs + restHomeHrs);
                    continue;
                }
                
                // 1. Tạo chuyến đi (Outbound)
                String maChuyenOut = "CT" + (System.currentTimeMillis() + tripCount++);
                if (maChuyenOut.length() > 20) maChuyenOut = maChuyenOut.substring(0, 20);
                entity.ChuyenTau cOut = new entity.ChuyenTau(maChuyenOut, "Lịch tự động định kỳ", tObj, entity.TrangThaiChuyenTau.CHUAN_BI);
                entity.ChiTietChuyenTau ctOut = new entity.ChiTietChuyenTau(
                    tsKhoiHanhOut.toLocalDateTime(),
                    tsDuKienOut.toLocalDateTime(),
                    cOut, gaDiObj, gaDenObj
                );
                listChuyen.add(cOut);
                listChiTiet.add(ctOut);
                
                // 2. Tạo chuyến về (Return)
                String maChuyenRet = "CT" + (System.currentTimeMillis() + tripCount++);
                if (maChuyenRet.length() > 20) maChuyenRet = maChuyenRet.substring(0, 20);
                entity.ChuyenTau cRet = new entity.ChuyenTau(maChuyenRet, "Lịch tự động định kỳ", tObj, entity.TrangThaiChuyenTau.CHUAN_BI);
                entity.ChiTietChuyenTau ctRet = new entity.ChiTietChuyenTau(
                    tsKhoiHanhRet.toLocalDateTime(),
                    tsDuKienRet.toLocalDateTime(),
                    cRet, gaDenObj, gaDiObj
                );
                listChuyen.add(cRet);
                listChiTiet.add(ctRet);
                
                // Cộng chu kỳ hoàn tất để chuyển sang chuyến tiếp theo: Đi + Nghỉ ga đến + Về + Nghỉ ga đi
                currentCursor.add(java.util.Calendar.HOUR_OF_DAY, travelHrs + restHrs + travelHrs + restHomeHrs);
            }
            
            if (listChuyen.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Không sinh được lịch trình nào do toàn bộ các ngày được chọn đều bị xung đột với lịch chạy sẵn có của tàu này!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            boolean ok = ctDao.saveMultipleChuyenTau(listChuyen, listChiTiet);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Tự động sinh thành công " + listChuyen.size() + " chuyến tàu định kỳ cho cả 2 chiều!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDataToTable();
                loadIdleTrains();
            } else {
                JOptionPane.showMessageDialog(dialog, "Lỗi xảy ra khi lưu lịch trình tự động vào CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        pnlBtn.add(btnCancel);
        pnlBtn.add(btnSave);
        
        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private int getGaIndex(String tenGa) {
        if (tenGa == null) return 0;
        switch (tenGa.trim()) {
            case "Hà Nội": return 1;
            case "Phủ Lý": return 2;
            case "Nam Định": return 3;
            case "Ninh Bình": return 4;
            case "Thanh Hóa": return 5;
            case "Vinh": return 6;
            case "Đồng Hới": return 7;
            case "Đông Hà": return 8;
            case "Huế": return 9;
            case "Đà Nẵng": return 10;
            case "Tam Kỳ": return 11;
            case "Quảng Ngãi": return 12;
            case "Diêu Trì": return 13;
            case "Tuy Hòa": return 14;
            case "Nha Trang": return 15;
            case "Tháp Chàm": return 16;
            case "Bình Thuận": return 17;
            case "Long Khánh": return 18;
            case "Biên Hòa": return 19;
            case "Dĩ An": return 20;
            case "Sài Gòn": return 21;
            default: return 22;
        }
    }

    private double tinhThoiGianChay(String tenGaDi, String tenGaDen) {
        int idxDi = getGaIndex(tenGaDi);
        int idxDen = getGaIndex(tenGaDen);
        return Math.abs(idxDen - idxDi) * 1.5;
    }

    private void capNhatThoiGianDenTuDong(JComboBox<String> cboDi, JComboBox<String> cboDen, 
                                         JDateChooser dcDi, JSpinner spDi, 
                                         JDateChooser dcDen, JSpinner spDen) {
        String gaDi = (String) cboDi.getSelectedItem();
        String gaDen = (String) cboDen.getSelectedItem();
        if (gaDi == null || gaDen == null || gaDi.isEmpty() || gaDen.isEmpty() || gaDi.equals(gaDen)) {
            return;
        }
        
        java.util.Date dateDi = dcDi.getDate();
        if (dateDi == null) return;
        
        java.util.Date timeDi = (java.util.Date) spDi.getValue();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(dateDi);
        java.util.Calendar tCal = java.util.Calendar.getInstance();
        tCal.setTime(timeDi);
        cal.set(java.util.Calendar.HOUR_OF_DAY, tCal.get(java.util.Calendar.HOUR_OF_DAY));
        cal.set(java.util.Calendar.MINUTE, tCal.get(java.util.Calendar.MINUTE));
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        
        double travelHrs = tinhThoiGianChay(gaDi, gaDen);
        int hours = (int) travelHrs;
        int minutes = (int) ((travelHrs - hours) * 60);
        
        cal.add(java.util.Calendar.HOUR_OF_DAY, hours);
        cal.add(java.util.Calendar.MINUTE, minutes);
        
        dcDen.setDate(cal.getTime());
        spDen.setValue(cal.getTime());
    }
}
