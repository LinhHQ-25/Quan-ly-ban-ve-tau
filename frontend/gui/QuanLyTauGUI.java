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
import entity.TrangThaiGhe;

public final class QuanLyTauGUI extends JPanel {
    private static final Color BORDER = GuiTheme.SEARCH_FIELD_BORDER;
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    private JLabel lblResults;
    
    private JTextField txtMaTau, txtTenTau;
    private JComboBox<String> cboStatus;
    
    private JPanel pnlTrainCards;

    // Center CardLayout navigation components
    private CardLayout cardLayout;
    private JPanel pnlCenterCard;
    
    private JPanel pnlDetailView;
    private JPanel pnlToaCards;
    private JLabel lblDetailTitle;
    private JLabel lblDetailSummary;
    
    private DefaultTableModel tblSeatModel;
    private JTable tblSeatData;
    
    private String selectedTrainIdForDetail = "";
    private String selectedToaIdForDetail = "";

    public QuanLyTauGUI() {
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

        // Train cards ribbon panel (NORTH)
        pnlPage.add(buildTrainCardsSection(), BorderLayout.NORTH);
        
        // CardLayout to easily switch between Search/List mode and Detail view
        cardLayout = new CardLayout();
        pnlCenterCard = new JPanel(cardLayout);
        pnlCenterCard.setOpaque(false);

        // Card 1: Train Search & Manage View
        JPanel pnlSearchView = new JPanel(new BorderLayout(0, 4));
        pnlSearchView.setOpaque(false);
        pnlSearchView.add(buildFilterPanel(), BorderLayout.NORTH);
        pnlSearchView.add(buildTablePanel(), BorderLayout.CENTER);
        pnlCenterCard.add(pnlSearchView, "search");

        // Card 2: Carriage & Seat management detail view
        pnlCenterCard.add(buildDetailView(), "detail");
        
        pnlPage.add(pnlCenterCard, BorderLayout.CENTER);
        add(pnlPage, BorderLayout.CENTER);
        
        loadDataToTableAndCards();
    }

    private JPanel buildTrainCardsSection() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("TRẠNG THÁI ĐỘI TÀU TRỰC TUYẾN");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(GuiTheme.NAVY);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

        pnlTrainCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlTrainCards.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(pnlTrainCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 97));
        scroll.getHorizontalScrollBar().setUnitIncrement(15);
        
        pnlOuter.add(scroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private JPanel buildFilterPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Title Row
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Thông tin tra cứu");
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

        txtMaTau = buildTextField("Nhập mã tàu");
        gbc.gridx = 0; pnlGrid.add(buildField("Mã tàu:", txtMaTau), gbc);
        
        txtTenTau = buildTextField("Nhập tên tàu");
        gbc.gridx = 1; pnlGrid.add(buildField("Tên tàu:", txtTenTau), gbc);
        
        cboStatus = buildStatusCombo();
        gbc.gridx = 2; pnlGrid.add(buildField("Trạng thái:", cboStatus), gbc);

        JPanel pnlSpacer = new JPanel();
        pnlSpacer.setOpaque(false);
        gbc.gridx = 3; pnlGrid.add(pnlSpacer, gbc);

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);

        // Buttons Row
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlButtons.setOpaque(false);

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);

        btnSearch.addActionListener(e -> loadDataToTableAndCards());
        btnReset.addActionListener(e -> {
            txtMaTau.setText(""); txtTenTau.setText("");
            cboStatus.setSelectedIndex(0);
            loadDataToTableAndCards();
        });

        pnlButtons.add(btnReset);
        pnlButtons.add(btnSearch);
        
        pnlOuter.add(pnlButtons, BorderLayout.SOUTH);

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

    private JTextField buildTextField(String placeholder) {
        JTextField txtField = new JTextFieldWithPlaceholder(placeholder);
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(txtField);
        txtField.setPreferredSize(new Dimension(104, 30));
        return txtField;
    }

    private JComboBox<String> buildStatusCombo() {
        JComboBox<String> cbo = new JComboBox<>(new String[] { "Tất cả", "Hoạt động", "Bảo trì" });
        cbo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cbo.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cbo.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(cbo);
        cbo.setPreferredSize(new Dimension(104, 30));
        return cbo;
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
        btn.setPreferredSize(new Dimension(135, 30));
        btn.setForeground(Color.WHITE);
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(QuanLyTauGUI.class, "/Images/traCuu.png", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(QuanLyTauGUI.class, "/Images/logoLammoi.png", 16, 16));
        else if (text.contains("Xóa")) btn.setIcon(GuiIcons.loadIcon(QuanLyTauGUI.class, "/Images/logoThungRac.png", 16, 16));
        else if (text.contains("Quay lại")) btn.setIcon(GuiIcons.loadIcon(QuanLyTauGUI.class, "/Images/logoBack.png", 16, 16));
        
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

        JLabel lblTitle = new JLabel("Danh sách tàu");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(44, 82, 150));
        lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
        pnlTitleRow.add(lblTitle, BorderLayout.WEST);

        // Actions panel for management
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);
        
        JButton btnAddTrain = buildNavyButton("Thêm tàu mới", new Color(40, 167, 69), new Color(33, 136, 56));
        JButton btnUpdateTrain = buildNavyButton("Cập nhật tàu", new Color(240, 120, 0), new Color(220, 100, 0));
        
        btnAddTrain.addActionListener(e -> showAddTrainDialog());
        btnUpdateTrain.addActionListener(e -> showUpdateTrainDialog());
        
        pnlActions.add(btnAddTrain);
        pnlActions.add(btnUpdateTrain);
        
        pnlTitleRow.add(pnlActions, BorderLayout.CENTER);

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

        String[] cols = {"STT", "Mã tàu", "Tên tàu", "Số toa", "Tổng số ghế", "Trạng thái", "Ghi chú"};
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
        
        // Double-click row to open detail view
        tblData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblData.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = tblData.convertRowIndexToModel(row);
                        String maTau = (String) tblModel.getValueAt(modelRow, 1);
                        showDetailView(maTau);
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
            if (i == 5) {
                tblData.getColumnModel().getColumn(i).setCellRenderer(new StatusBadgeRenderer());
            } else {
                tblData.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
            }
        }
        ((DefaultTableCellRenderer)tblData.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(null);
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

        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private JPanel buildDetailView() {
        pnlDetailView = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlDetailView.setLayout(new BorderLayout(0, 10));
        pnlDetailView.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JPanel pnlTitleRow = new JPanel(new BorderLayout());
        pnlTitleRow.setOpaque(false);

        lblDetailTitle = new JLabel("DANH SÁCH TOA TÀU");
        lblDetailTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblDetailTitle.setForeground(Color.WHITE);
        lblDetailTitle.setOpaque(true);
        lblDetailTitle.setBackground(new Color(44, 82, 150));
        lblDetailTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
             // Carriage Management Actions inside the title header
        JPanel pnlCarriageActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlCarriageActions.setOpaque(false);
        
        JButton btnAddToa = buildNavyButton("Thêm toa", new Color(40, 167, 69), new Color(33, 136, 56));
        JButton btnUpdateToa = buildNavyButton("Cập nhật toa", new Color(240, 120, 0), new Color(220, 100, 0));
        JButton btnDeleteToa = buildNavyButton("Xóa toa", new Color(220, 53, 69), new Color(200, 35, 51));
        
        btnAddToa.addActionListener(e -> showAddToaDialog());
        btnUpdateToa.addActionListener(e -> showUpdateToaDialog());
        btnDeleteToa.addActionListener(e -> deleteSelectedToa());
        
        pnlCarriageActions.add(btnAddToa);
        pnlCarriageActions.add(btnUpdateToa);
        pnlCarriageActions.add(btnDeleteToa);
        
        pnlTitleRow.add(lblDetailTitle, BorderLayout.WEST);
        pnlTitleRow.add(pnlCarriageActions, BorderLayout.EAST);
 
        lblDetailSummary = new JLabel("");
        lblDetailSummary.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblDetailSummary.setForeground(new Color(120, 130, 140));

        pnlHeader.add(pnlTitleRow, BorderLayout.CENTER);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlDetailView.add(pnlHeader, BorderLayout.NORTH);

        pnlToaCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        pnlToaCards.setOpaque(false);

        JScrollPane scroll = new JScrollPane(pnlToaCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 77));
        scroll.getHorizontalScrollBar().setUnitIncrement(15);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setOpaque(false);
        pnlContent.add(scroll, BorderLayout.NORTH);
        pnlContent.add(buildSeatTablePanel(), BorderLayout.CENTER);

        pnlDetailView.add(pnlContent, BorderLayout.CENTER);
        return pnlDetailView;
    }

    private JPanel buildSeatTablePanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 10));
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách ghế trong toa");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(44, 82, 150));
        lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // Seat toggle button inside the seat title header
        JPanel pnlSeatActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSeatActions.setOpaque(false);
        
        JButton btnToggleStatus = buildNavyButton("Đổi trạng thái ghế", new Color(240, 120, 0), new Color(220, 100, 0));
        btnToggleStatus.addActionListener(e -> toggleSelectedSeatStatus());
        pnlSeatActions.add(btnToggleStatus);
        
        pnlHeader.add(pnlSeatActions, BorderLayout.EAST);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

        String[] cols = {"STT", "Mã ghế", "Số ghế", "Loại ghế", "Số toa", "Trạng thái"};
        tblSeatModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSeatData = new JTable(tblSeatModel);
        tblSeatData.setRowHeight(36);
        tblSeatData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblSeatData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblSeatData.setShowVerticalLines(false);
        tblSeatData.setSelectionBackground(new Color(232, 240, 254));
        tblSeatData.setSelectionForeground(GuiTheme.TEXT);

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

        for (int i = 0; i < tblSeatData.getColumnCount(); i++) {
            if (i == 5) {
                tblSeatData.getColumnModel().getColumn(i).setCellRenderer(new StatusBadgeRenderer());
            } else {
                tblSeatData.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
            }
        }
        ((DefaultTableCellRenderer)tblSeatData.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane spnScroll = new JScrollPane(tblSeatData);
        spnScroll.setBorder(null);
        spnScroll.getViewport().setBackground(Color.WHITE);

        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private void showDetailView(String trainId) {
        selectedTrainIdForDetail = trainId;
        lblDetailTitle.setText("DANH SÁCH TOA TÀU - " + trainId);
        selectedToaIdForDetail = ""; // Reset carriage selection

        loadToaCards();
        cardLayout.show(pnlCenterCard, "detail");

        if (pnlTrainCards != null) {
            pnlTrainCards.repaint();
        }
    }

    private void showSearchView() {
        selectedTrainIdForDetail = "";
        cardLayout.show(pnlCenterCard, "search");

        if (pnlTrainCards != null) {
            pnlTrainCards.repaint();
        }
    }

    private void loadToaCards() {
        if (pnlToaCards == null) return;
        pnlToaCards.removeAll();

        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT * FROM ToaTau WHERE maTau = ? ORDER BY soToa ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selectedTrainIdForDetail);
            ResultSet rs = stmt.executeQuery();

            boolean hasToa = false;
            String firstToaId = "";
            int count = 0;
            while (rs.next()) {
                count++;
                String maToa = rs.getString("maToaTau");
                int soToa = rs.getInt("soToa");
                String loaiToa = rs.getString("loaiToa");
                if (firstToaId.isEmpty()) {
                    firstToaId = maToa;
                }

                boolean isSel = maToa.equals(selectedToaIdForDetail);
                pnlToaCards.add(new ToaCard(maToa, soToa, loaiToa, isSel));
                hasToa = true;
            }

            lblDetailSummary.setText("");

            if (hasToa) {
                if (selectedToaIdForDetail.isEmpty() || !isToaInTrain(selectedToaIdForDetail)) {
                    selectedToaIdForDetail = firstToaId;
                    pnlToaCards.removeAll();
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        String maToa = rs.getString("maToaTau");
                        int soToa = rs.getInt("soToa");
                        String loaiToa = rs.getString("loaiToa");
                        boolean isSel = maToa.equals(selectedToaIdForDetail);
                        pnlToaCards.add(new ToaCard(maToa, soToa, loaiToa, isSel));
                    }
                }
                loadSeatsOfToa();
            } else {
                selectedToaIdForDetail = "";
                if (tblSeatModel != null) {
                    tblSeatModel.setRowCount(0);
                }
                JLabel lbl = new JLabel("Tàu này chưa có cấu hình toa nào.");
                lbl.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 13));
                lbl.setForeground(GuiTheme.SUB_TEXT);
                pnlToaCards.add(lbl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        pnlToaCards.revalidate();
        pnlToaCards.repaint();
    }

    private boolean isToaInTrain(String toaId) {
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return false;
        try {
            String sql = "SELECT COUNT(*) FROM ToaTau WHERE maToaTau = ? AND maTau = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, toaId);
            stmt.setString(2, selectedTrainIdForDetail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadSeatsOfToa() {
        if (tblSeatModel == null) return;
        tblSeatModel.setRowCount(0);

        if (selectedToaIdForDetail.isEmpty()) return;

        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT g.maGhe, g.soGhe, g.loaiGhe, t.soToa, g.trangThai " +
                         "FROM Ghe g " +
                         "JOIN ToaTau t ON g.maToaTau = t.maToaTau " +
                         "WHERE t.maToaTau = ? " +
                         "ORDER BY CAST(g.soGhe AS INT) ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selectedToaIdForDetail);
            ResultSet rs = stmt.executeQuery();

            int stt = 1;
            while (rs.next()) {
                tblSeatModel.addRow(new Object[] {
                    stt++,
                    rs.getString("maGhe"),
                    rs.getString("soGhe"),
                    loaiGheToVietnamese(rs.getString("loaiGhe")),
                    rs.getInt("soToa"),
                    TrangThaiGhe.tuMoTa(rs.getString("trangThai")).toString()
                });
            }
        } catch (SQLException e) {
            try {
                String sql = "SELECT g.maGhe, g.soGhe, g.loaiGhe, t.soToa, g.trangThai " +
                             "FROM Ghe g " +
                             "JOIN ToaTau t ON g.maToaTau = t.maToaTau " +
                             "WHERE t.maToaTau = ? " +
                             "ORDER BY g.soGhe ASC";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, selectedToaIdForDetail);
                ResultSet rs = stmt.executeQuery();

                int stt = 1;
                while (rs.next()) {
                    tblSeatModel.addRow(new Object[] {
                        stt++,
                        rs.getString("maGhe"),
                        rs.getString("soGhe"),
                        loaiGheToVietnamese(rs.getString("loaiGhe")),
                        rs.getInt("soToa"),
                        TrangThaiGhe.tuMoTa(rs.getString("trangThai")).toString()
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String loaiGheToVietnamese(String loaiGhe) {
        if (loaiGhe == null) return "";
        switch (loaiGhe) {
            case "GHE_CUNG":   return "Ghế cứng";
            case "GHE_MEM":    return "Ghế mềm";
            case "GIUONG_NAM": return "Giường nằm";
            default:           return loaiGhe;
        }
    }

    private void loadDataToTableAndCards() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        
        if (pnlTrainCards != null) {
            pnlTrainCards.removeAll();
        }

        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT * FROM Tau WHERE maTau LIKE ? AND tenTau LIKE ?";
            String statusFilter = (String) cboStatus.getSelectedItem();
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Tất cả")) {
                sql += " AND trangThai = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaTau.getText().trim() + "%");
            stmt.setString(2, "%" + txtTenTau.getText().trim() + "%");
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Tất cả")) {
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

            boolean hasTrains = false;
            while (rs.next()) {
                hasTrains = true;
                String maTau = rs.getString("maTau");
                String tenTau = rs.getString("tenTau");
                int soToa = rs.getInt("soToa");
                int tongSoGhe = rs.getInt("tongSoGhe");
                String trangThai = rs.getString("trangThai");
                String ghiChu = hasGhiChu ? rs.getString("ghiChu") : "";

                tblModel.addRow(new Object[] {
                    stt++, maTau, tenTau, soToa, tongSoGhe, trangThai, ghiChu
                });

                if (pnlTrainCards != null) {
                    pnlTrainCards.add(new TrainCard(maTau, tenTau, soToa, tongSoGhe, trangThai));
                }
            }

            if (!hasTrains && pnlTrainCards != null) {
                JLabel lbl = new JLabel("Hiện không có tàu nào phù hợp với bộ lọc.");
                lbl.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 13));
                lbl.setForeground(GuiTheme.SUB_TEXT);
                pnlTrainCards.add(lbl);
            }
            if (lblResults != null) {
                lblResults.setText("");
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (pnlTrainCards != null) {
            pnlTrainCards.revalidate();
            pnlTrainCards.repaint();
        }
    }

    private void showAddTrainDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm tàu mới", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(450, 280);
        dlg.setLocationRelativeTo(this);
        
        JPanel pnlContent = new JPanel(new GridBagLayout());
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JTextField txtGhiChuDlg = new JTextField();
        txtGhiChuDlg.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(txtGhiChuDlg);
        
        JComboBox<String> cboStatusDlg = new JComboBox<>(new String[] { "Hoạt động", "Bảo trì" });
        cboStatusDlg.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(cboStatusDlg);
        
        gbc.gridy = 0;
        gbc.gridx = 0; pnlContent.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; pnlContent.add(txtGhiChuDlg, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0; pnlContent.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1; pnlContent.add(cboStatusDlg, gbc);
        
        dlg.add(pnlContent, BorderLayout.CENTER);
        
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBtns.setBackground(new Color(245, 247, 250));
        pnlBtns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));
        
        JButton btnCancel = buildNavyButton("Hủy bỏ", new Color(120, 130, 140), new Color(100, 110, 120));
        JButton btnSave = buildNavyButton("Xác nhận", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String ghiChu = txtGhiChuDlg.getText().trim();
            String status = cboStatusDlg.getSelectedItem().toString().equals("Hoạt động") ? "Đang hoạt động" : "Bảo trì";
            
            Connection conn = Connect_DB.getInstance().getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(dlg, "Kết nối cơ sở dữ liệu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int count = 0;
                String countSql = "SELECT COUNT(*) FROM Tau";
                try (PreparedStatement ps = conn.prepareStatement(countSql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }
                }
                
                int nextIdx = count + 1;
                String maTau = "SEVN" + nextIdx;
                String tenTau = "tàu SE " + nextIdx;
                
                String checkSql = "SELECT COUNT(*) FROM Tau WHERE maTau = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, maTau);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            while (true) {
                                nextIdx++;
                                maTau = "SEVN" + nextIdx;
                                tenTau = "tàu SE " + nextIdx;
                                ps.setString(1, maTau);
                                try (ResultSet rs2 = ps.executeQuery()) {
                                    if (rs2.next() && rs2.getInt(1) == 0) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    conn.setAutoCommit(false);
                    String sqlTau = "INSERT INTO Tau (maTau, tenTau, soToa, tongSoGhe, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps1 = conn.prepareStatement(sqlTau)) {
                        ps1.setString(1, maTau);
                        ps1.setString(2, tenTau);
                        ps1.setInt(3, 12);
                        ps1.setInt(4, 240);
                        ps1.setString(5, status);
                        ps1.setString(6, ghiChu);
                        ps1.executeUpdate();
                    }
 
                    String sqlToa = "INSERT INTO ToaTau (maToaTau, soToa, soLuongGhe, loaiToa, maTau, heSoLoaiToa) VALUES (?, ?, ?, ?, ?, ?)";
                    String sqlGhe = "INSERT INTO Ghe (maGhe, soGhe, loaiGhe, maToaTau, trangThai) VALUES (?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement psToa = conn.prepareStatement(sqlToa);
                         PreparedStatement psGhe = conn.prepareStatement(sqlGhe)) {
                         
                        for (int soToa = 1; soToa <= 12; soToa++) {
                            String maToa = maTau + "_T" + String.format("%02d", soToa);
                            int soLuongGhe = 20;
                            String loaiToa;
                            double heSoToa;
                            String loaiGhe;
                            
                            if (soToa <= 4) {
                                loaiToa = "TOA_THUONG";
                                heSoToa = 1.0;
                                loaiGhe = "GHE_CUNG";
                            } else if (soToa <= 8) {
                                loaiToa = "TOA_VIP";
                                heSoToa = 1.5;
                                loaiGhe = "GIUONG_NAM";
                            } else {
                                loaiToa = "TOA_VIP";
                                heSoToa = 1.5;
                                loaiGhe = "GHE_MEM";
                            }
                            
                            psToa.setString(1, maToa);
                            psToa.setInt(2, soToa);
                            psToa.setInt(3, soLuongGhe);
                            psToa.setString(4, loaiToa);
                            psToa.setString(5, maTau);
                            psToa.setDouble(6, heSoToa);
                            psToa.executeUpdate();
                            
                            for (int soGhe = 1; soGhe <= soLuongGhe; soGhe++) {
                                String maGhe = maToa + "_G" + String.format("%02d", soGhe);
                                psGhe.setString(1, maGhe);
                                psGhe.setString(2, String.valueOf(soGhe));
                                psGhe.setString(3, loaiGhe);
                                psGhe.setString(4, maToa);
                                psGhe.setString(5, "Hoạt động");
                                psGhe.executeUpdate();
                            }
                        }
                    }
                    
                    conn.commit();
                    JOptionPane.showMessageDialog(dlg, "Thêm tàu mới thành công!\nTự động khởi tạo 12 toa và 240 ghế hoạt động.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    loadDataToTableAndCards();
                    TauGUI.notifyDataChanged(); // Đồng bộ sang màn hình tra cứu tàu
                } catch (Exception ex) {
                    conn.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dlg, "Lỗi khi khởi tạo cấu trúc tàu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        
        dlg.setVisible(true);
    }

    private void showUpdateTrainDialog() {
        int selectedRow = tblData.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tàu trong bảng để cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tblData.convertRowIndexToModel(selectedRow);
        String maTau = (String) tblModel.getValueAt(modelRow, 1);
        String tenTau = (String) tblModel.getValueAt(modelRow, 2);
        String trangThai = (String) tblModel.getValueAt(modelRow, 5);
        String ghiChu = (String) tblModel.getValueAt(modelRow, 6);
        
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cập nhật tàu - " + maTau, true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(450, 320);
        dlg.setLocationRelativeTo(this);
        
        JPanel pnlContent = new JPanel(new GridBagLayout());
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JTextField txtTenTauDlg = new JTextField(tenTau);
        txtTenTauDlg.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(txtTenTauDlg);
        
        JTextField txtGhiChuDlg = new JTextField(ghiChu);
        txtGhiChuDlg.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(txtGhiChuDlg);
        
        JComboBox<String> cboStatusDlg = new JComboBox<>(new String[] { "Hoạt động", "Bảo trì" });
        cboStatusDlg.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(cboStatusDlg);
        cboStatusDlg.setSelectedItem(trangThai.contains("Bảo trì") ? "Bảo trì" : "Hoạt động");
        
        gbc.gridy = 0;
        gbc.gridx = 0; pnlContent.add(new JLabel("Mã tàu:"), gbc);
        JLabel lblMa = new JLabel(maTau);
        lblMa.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1; pnlContent.add(lblMa, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0; pnlContent.add(new JLabel("Tên tàu:"), gbc);
        gbc.gridx = 1; pnlContent.add(txtTenTauDlg, gbc);
        
        gbc.gridy = 2;
        gbc.gridx = 0; pnlContent.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; pnlContent.add(txtGhiChuDlg, gbc);
        
        gbc.gridy = 3;
        gbc.gridx = 0; pnlContent.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1; pnlContent.add(cboStatusDlg, gbc);
        
        dlg.add(pnlContent, BorderLayout.CENTER);
        
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBtns.setBackground(new Color(245, 247, 250));
        pnlBtns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));
        
        JButton btnCancel = buildNavyButton("Hủy bỏ", new Color(120, 130, 140), new Color(100, 110, 120));
        JButton btnSave = buildNavyButton("Cập nhật", new Color(240, 120, 0), new Color(220, 100, 0));
        
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String updatedTen = txtTenTauDlg.getText().trim();
            String updatedGhiChu = txtGhiChuDlg.getText().trim();
            String updatedStatus = cboStatusDlg.getSelectedItem().toString().equals("Hoạt động") ? "Đang hoạt động" : "Bảo trì";
            
            if (updatedTen.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Tên tàu không được bỏ trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Connection conn = Connect_DB.getInstance().getConnection();
            if (conn == null) return;
            
            try {
                String sql = "UPDATE Tau SET tenTau = ?, trangThai = ?, ghiChu = ? WHERE maTau = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, updatedTen);
                    ps.setString(2, updatedStatus);
                    ps.setString(3, updatedGhiChu);
                    ps.setString(4, maTau);
                    ps.executeUpdate();
                }
                
                JOptionPane.showMessageDialog(dlg, "Cập nhật thông tin tàu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadDataToTableAndCards();
                TauGUI.notifyDataChanged(); // Đồng bộ sang màn hình tra cứu tàu
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg, "Cập nhật thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        
        dlg.setVisible(true);
    }

    private void showAddToaDialog() {
        if (selectedTrainIdForDetail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có tàu nào được chọn để thêm toa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        
        int nextSoToa = 1;
        try {
            String sql = "SELECT MAX(soToa) FROM ToaTau WHERE maTau = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, selectedTrainIdForDetail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nextSoToa = rs.getInt(1) + 1;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm toa mới vào " + selectedTrainIdForDetail, true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(450, 360);
        dlg.setLocationRelativeTo(this);
        
        JPanel pnlContent = new JPanel(new GridBagLayout());
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JSpinner spnSoToa = new JSpinner(new SpinnerNumberModel(nextSoToa, 1, 100, 1));
        spnSoToa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(spnSoToa);
        
        JComboBox<String> cboLoaiToa = new JComboBox<>(new String[] { "Toa thường", "Toa VIP" });
        cboLoaiToa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(cboLoaiToa);
        
        JComboBox<String> cboLoaiGhe = new JComboBox<>(new String[] { "Ghế cứng", "Ghế mềm", "Giường nằm" });
        cboLoaiGhe.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(cboLoaiGhe);
        
        JSpinner spnSoGhe = new JSpinner(new SpinnerNumberModel(28, 1, 100, 1));
        spnSoGhe.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(spnSoGhe);
        
        JSpinner spnHeSo = new JSpinner(new SpinnerNumberModel(1.2, 0.1, 5.0, 0.1));
        spnHeSo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(spnHeSo);
        
        cboLoaiToa.addActionListener(e -> {
            if (cboLoaiToa.getSelectedItem().toString().equals("Toa VIP")) {
                cboLoaiGhe.setSelectedItem("Giường nằm");
                spnSoGhe.setValue(14);
                spnHeSo.setValue(1.5);
            } else {
                cboLoaiGhe.setSelectedItem("Ghế mềm");
                spnSoGhe.setValue(28);
                spnHeSo.setValue(1.2);
            }
        });
        
        cboLoaiGhe.addActionListener(e -> {
            String val = cboLoaiGhe.getSelectedItem().toString();
            if (val.equals("Ghế cứng")) {
                cboLoaiToa.setSelectedItem("Toa thường");
                spnSoGhe.setValue(28);
                spnHeSo.setValue(1.0);
            } else if (val.equals("Ghế mềm")) {
                cboLoaiToa.setSelectedItem("Toa thường");
                spnSoGhe.setValue(28);
                spnHeSo.setValue(1.2);
            } else {
                cboLoaiToa.setSelectedItem("Toa VIP");
                spnSoGhe.setValue(14);
                spnHeSo.setValue(1.5);
            }
        });
        
        gbc.gridy = 0;
        gbc.gridx = 0; pnlContent.add(new JLabel("Số toa:"), gbc);
        gbc.gridx = 1; pnlContent.add(spnSoToa, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0; pnlContent.add(new JLabel("Loại toa:"), gbc);
        gbc.gridx = 1; pnlContent.add(cboLoaiToa, gbc);
        
        gbc.gridy = 2;
        gbc.gridx = 0; pnlContent.add(new JLabel("Loại ghế:"), gbc);
        gbc.gridx = 1; pnlContent.add(cboLoaiGhe, gbc);
        
        gbc.gridy = 3;
        gbc.gridx = 0; pnlContent.add(new JLabel("Số lượng ghế:"), gbc);
        gbc.gridx = 1; pnlContent.add(spnSoGhe, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0; pnlContent.add(new JLabel("Hệ số toa:"), gbc);
        gbc.gridx = 1; pnlContent.add(spnHeSo, gbc);
        
        dlg.add(pnlContent, BorderLayout.CENTER);
        
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBtns.setBackground(new Color(245, 247, 250));
        pnlBtns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));
        
        JButton btnCancel = buildNavyButton("Hủy bỏ", new Color(120, 130, 140), new Color(100, 110, 120));
        JButton btnSave = buildNavyButton("Xác nhận", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            int soToa = (int) spnSoToa.getValue();
            String loaiToaStr = cboLoaiToa.getSelectedItem().toString().equals("Toa thường") ? "TOA_THUONG" : "TOA_VIP";
            String loaiGheStr = cboLoaiGhe.getSelectedItem().toString();
            String loaiGheEnum = loaiGheStr.equals("Ghế cứng") ? "GHE_CUNG" : loaiGheStr.equals("Ghế mềm") ? "GHE_MEM" : "GIUONG_NAM";
            int soGhe = (int) spnSoGhe.getValue();
            double heSo = (double) spnHeSo.getValue();
            
            String maToa = selectedTrainIdForDetail + "_T" + String.format("%02d", soToa);
            
            try {
                String checkSql = "SELECT COUNT(*) FROM ToaTau WHERE maTau = ? AND soToa = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, selectedTrainIdForDetail);
                    ps.setInt(2, soToa);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(dlg, "Số toa này đã tồn tại trong tàu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
                
                conn.setAutoCommit(false);
                try {
                    String sqlToa = "INSERT INTO ToaTau (maToaTau, soToa, soLuongGhe, loaiToa, maTau, heSoLoaiToa) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlToa)) {
                        ps.setString(1, maToa);
                        ps.setInt(2, soToa);
                        ps.setInt(3, soGhe);
                        ps.setString(4, loaiToaStr);
                        ps.setString(5, selectedTrainIdForDetail);
                        ps.setDouble(6, heSo);
                        ps.executeUpdate();
                    }
                    
                    String sqlGhe = "INSERT INTO Ghe (maGhe, soGhe, loaiGhe, maToaTau, trangThai) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlGhe)) {
                        for (int i = 1; i <= soGhe; i++) {
                            String maGhe = maToa + "_G" + String.format("%02d", i);
                            ps.setString(1, maGhe);
                            ps.setString(2, String.valueOf(i));
                            ps.setString(3, loaiGheEnum);
                            ps.setString(4, maToa);
                            ps.setString(5, "Hoạt động");
                            ps.executeUpdate();
                        }
                    }
                    
                    String sqlUpdateTrain = "UPDATE Tau SET soToa = soToa + 1, tongSoGhe = tongSoGhe + ? WHERE maTau = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUpdateTrain)) {
                        ps.setInt(1, soGhe);
                        ps.setString(2, selectedTrainIdForDetail);
                        ps.executeUpdate();
                    }
                    
                    conn.commit();
                    JOptionPane.showMessageDialog(dlg, "Thêm toa và tự động sinh " + soGhe + " ghế thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    
                    selectedToaIdForDetail = maToa;
                    loadToaCards();
                    loadDataToTableAndCards();
                    TauGUI.notifyDataChanged(); // Đồng bộ sang màn hình tra cứu tàu
                } catch (Exception ex) {
                    conn.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dlg, "Lỗi khi thêm toa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        
        dlg.setVisible(true);
    }

    private void showUpdateToaDialog() {
        if (selectedToaIdForDetail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một toa để cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        
        int soToa = 0;
        String loaiToa = "";
        double heSo = 0;
        int soGhe = 0;
        String loaiGhe = "";
        
        try {
            String sql = "SELECT * FROM ToaTau WHERE maToaTau = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, selectedToaIdForDetail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        soToa = rs.getInt("soToa");
                        loaiToa = rs.getString("loaiToa");
                        heSo = rs.getDouble("heSoLoaiToa");
                        soGhe = rs.getInt("soLuongGhe");
                    }
                }
            }
            
            String sqlGhe = "SELECT TOP 1 loaiGhe FROM Ghe WHERE maToaTau = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlGhe)) {
                ps.setString(1, selectedToaIdForDetail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        loaiGhe = rs.getString("loaiGhe");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cập nhật toa - " + selectedToaIdForDetail, true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(450, 360);
        dlg.setLocationRelativeTo(this);
        
        JPanel pnlContent = new JPanel(new GridBagLayout());
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JSpinner spnSoToa = new JSpinner(new SpinnerNumberModel(soToa, 1, 100, 1));
        spnSoToa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(spnSoToa);
        
        JComboBox<String> cboLoaiToa = new JComboBox<>(new String[] { "Toa thường", "Toa VIP" });
        cboLoaiToa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(cboLoaiToa);
        cboLoaiToa.setSelectedItem(loaiToa.equals("TOA_VIP") ? "Toa VIP" : "Toa thường");
        
        JComboBox<String> cboLoaiGhe = new JComboBox<>(new String[] { "Ghế cứng", "Ghế mềm", "Giường nằm" });
        cboLoaiGhe.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(cboLoaiGhe);
        String matchGhe = loaiGhe != null ? (loaiGhe.equals("GHE_CUNG") ? "Ghế cứng" : loaiGhe.equals("GHE_MEM") ? "Ghế mềm" : "Giường nằm") : "Ghế mềm";
        cboLoaiGhe.setSelectedItem(matchGhe);
        
        JLabel lblSoGhe = new JLabel(String.valueOf(soGhe));
        lblSoGhe.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        
        JSpinner spnHeSo = new JSpinner(new SpinnerNumberModel(heSo, 0.1, 5.0, 0.1));
        spnHeSo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        GuiTheme.setupRoundedComponent(spnHeSo);
        
        cboLoaiToa.addActionListener(e -> {
            if (cboLoaiToa.getSelectedItem().toString().equals("Toa VIP")) {
                cboLoaiGhe.setSelectedItem("Giường nằm");
                spnHeSo.setValue(1.5);
            } else {
                cboLoaiGhe.setSelectedItem("Ghế mềm");
                spnHeSo.setValue(1.2);
            }
        });
        
        cboLoaiGhe.addActionListener(e -> {
            String val = cboLoaiGhe.getSelectedItem().toString();
            if (val.equals("Ghế cứng")) {
                cboLoaiToa.setSelectedItem("Toa thường");
                spnHeSo.setValue(1.0);
            } else if (val.equals("Ghế mềm")) {
                cboLoaiToa.setSelectedItem("Toa thường");
                spnHeSo.setValue(1.2);
            } else {
                cboLoaiToa.setSelectedItem("Toa VIP");
                spnHeSo.setValue(1.5);
            }
        });
        
        gbc.gridy = 0;
        gbc.gridx = 0; pnlContent.add(new JLabel("Số toa:"), gbc);
        gbc.gridx = 1; pnlContent.add(spnSoToa, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0; pnlContent.add(new JLabel("Loại toa:"), gbc);
        gbc.gridx = 1; pnlContent.add(cboLoaiToa, gbc);
        
        gbc.gridy = 2;
        gbc.gridx = 0; pnlContent.add(new JLabel("Loại ghế:"), gbc);
        gbc.gridx = 1; pnlContent.add(cboLoaiGhe, gbc);
        
        gbc.gridy = 3;
        gbc.gridx = 0; pnlContent.add(new JLabel("Số lượng ghế:"), gbc);
        gbc.gridx = 1; pnlContent.add(lblSoGhe, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0; pnlContent.add(new JLabel("Hệ số toa:"), gbc);
        gbc.gridx = 1; pnlContent.add(spnHeSo, gbc);
        
        dlg.add(pnlContent, BorderLayout.CENTER);
        
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBtns.setBackground(new Color(245, 247, 250));
        pnlBtns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));
        
        JButton btnCancel = buildNavyButton("Hủy bỏ", new Color(120, 130, 140), new Color(100, 110, 120));
        JButton btnSave = buildNavyButton("Cập nhật", new Color(240, 120, 0), new Color(220, 100, 0));
        
        btnCancel.addActionListener(e -> dlg.dispose());
        final int oldSoToa = soToa;
        btnSave.addActionListener(e -> {
            int newSoToa = (int) spnSoToa.getValue();
            String loaiToaStr = cboLoaiToa.getSelectedItem().toString().equals("Toa thường") ? "TOA_THUONG" : "TOA_VIP";
            String loaiGheStr = cboLoaiGhe.getSelectedItem().toString();
            String loaiGheEnum = loaiGheStr.equals("Ghế cứng") ? "GHE_CUNG" : loaiGheStr.equals("Ghế mềm") ? "GHE_MEM" : "GIUONG_NAM";
            double heSoVal = (double) spnHeSo.getValue();
            
            try {
                if (newSoToa != oldSoToa) {
                    String checkSql = "SELECT COUNT(*) FROM ToaTau WHERE maTau = ? AND soToa = ?";
                    try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                        ps.setString(1, selectedTrainIdForDetail);
                        ps.setInt(2, newSoToa);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                JOptionPane.showMessageDialog(dlg, "Số toa " + newSoToa + " đã tồn tại trong tàu này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                }
                
                conn.setAutoCommit(false);
                try {
                    String sqlUpdateToa = "UPDATE ToaTau SET soToa = ?, loaiToa = ?, heSoLoaiToa = ? WHERE maToaTau = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUpdateToa)) {
                        ps.setInt(1, newSoToa);
                        ps.setString(2, loaiToaStr);
                        ps.setDouble(3, heSoVal);
                        ps.setString(4, selectedToaIdForDetail);
                        ps.executeUpdate();
                    }
                    
                    String sqlUpdateSeats = "UPDATE Ghe SET loaiGhe = ? WHERE maToaTau = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUpdateSeats)) {
                        ps.setString(1, loaiGheEnum);
                        ps.setString(2, selectedToaIdForDetail);
                        ps.executeUpdate();
                    }
                    
                    conn.commit();
                    JOptionPane.showMessageDialog(dlg, "Cập nhật toa tàu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    
                    loadToaCards();
                    loadSeatsOfToa();
                    TauGUI.notifyDataChanged(); // Đồng bộ sang màn hình tra cứu tàu
                } catch (Exception ex) {
                    conn.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dlg, "Lỗi khi cập nhật toa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        
        dlg.setVisible(true);
    }

    private void deleteSelectedToa() {
        if (selectedToaIdForDetail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một toa để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa Toa này? Hành động này sẽ XÓA TOÀN BỘ GHẾ ngồi trong toa!",
            "Xác nhận xóa toa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (choice != JOptionPane.YES_OPTION) return;
        
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        
        try {
            int soGheCount = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT soLuongGhe FROM ToaTau WHERE maToaTau = ?")) {
                ps.setString(1, selectedToaIdForDetail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        soGheCount = rs.getInt(1);
                    }
                }
            }
            
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Ghe WHERE maToaTau = ?")) {
                    ps.setString(1, selectedToaIdForDetail);
                    ps.executeUpdate();
                }
                
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ToaTau WHERE maToaTau = ?")) {
                    ps.setString(1, selectedToaIdForDetail);
                    ps.executeUpdate();
                }
                
                try (PreparedStatement ps = conn.prepareStatement("UPDATE Tau SET soToa = soToa - 1, tongSoGhe = tongSoGhe - ? WHERE maTau = ?")) {
                    ps.setInt(1, soGheCount);
                    ps.setString(2, selectedTrainIdForDetail);
                    ps.executeUpdate();
                }
                
                conn.commit();
                JOptionPane.showMessageDialog(this, "Xóa toa tàu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                selectedToaIdForDetail = "";
                loadToaCards();
                loadDataToTableAndCards();
                TauGUI.notifyDataChanged(); // Đồng bộ sang màn hình tra cứu tàu
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Xóa toa thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleSelectedSeatStatus() {
        int selectedRow = tblSeatData.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một ghế ngồi trên bảng để đổi trạng thái!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tblSeatData.convertRowIndexToModel(selectedRow);
        String maGhe = (String) tblSeatModel.getValueAt(modelRow, 1);
        String trangThai = (String) tblSeatModel.getValueAt(modelRow, 5);
        
        TrangThaiGhe currentStatus = TrangThaiGhe.tuMoTa(trangThai);
        TrangThaiGhe nextStatus = currentStatus == TrangThaiGhe.HOAT_DONG ? TrangThaiGhe.BAO_TRI : TrangThaiGhe.HOAT_DONG;
        
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        
        try {
            String sql = "UPDATE Ghe SET trangThai = ? WHERE maGhe = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nextStatus.toString());
                ps.setString(2, maGhe);
                ps.executeUpdate();
            }
            
            JOptionPane.showMessageDialog(this, "Đã chuyển đổi trạng thái của ghế " + maGhe + " sang \"" + nextStatus + "\"!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadSeatsOfToa();
            TauGUI.notifyDataChanged(); // Đồng bộ trạng thái ghế sang màn hình tra cứu tàu
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đổi trạng thái ghế thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        private final String cardId;
        private final String cardName;
        private final int soToa;
        private final int tongSoGhe;
        private final String status;
        private final boolean isSelected;
        private boolean isHovered = false;

        TrainCard(String id, String name, int toa, int ghe, String status) {
            this.cardId = id;
            this.cardName = name;
            this.soToa = toa;
            this.tongSoGhe = ghe;
            this.status = status;
            this.isSelected = id.equals(selectedTrainIdForDetail);

            setPreferredSize(new Dimension(67, 95));
            setOpaque(false);
            setLayout(new BorderLayout(0, 4));
            setBorder(new EmptyBorder(38, 5, 5, 5));

            JLabel lblId = new JLabel(id, SwingConstants.CENTER);
            lblId.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 10));
            lblId.setForeground(new Color(27, 38, 59));
            add(lblId, BorderLayout.CENTER);

            boolean isActive = status.contains("hoạt động") || status.contains("Hoạt động") || status.contains("Đang hoạt động");
            String statusText = isActive ? "HOẠT ĐỘNG" : "BẢO TRÌ";
            
            JLabel lblStatus = new JLabel(statusText, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblStatus.setOpaque(false);
            lblStatus.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 7));
            lblStatus.setBorder(new EmptyBorder(2, 6, 2, 6));
            
            if (isActive) {
                lblStatus.setBackground(new Color(218, 230, 255));
                lblStatus.setForeground(new Color(40, 100, 220));
            } else {
                lblStatus.setBackground(new Color(254, 226, 226));
                lblStatus.setForeground(new Color(220, 38, 38));
            }
            
            JPanel pnlStatusWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnlStatusWrap.setOpaque(false);
            pnlStatusWrap.add(lblStatus);
            add(pnlStatusWrap, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (cardId.equals(selectedTrainIdForDetail)) {
                        showSearchView();
                    } else {
                        showDetailView(cardId);
                    }
                }

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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean isSelected = cardId.equals(selectedTrainIdForDetail);

            boolean isActive = status.contains("hoạt động") || status.contains("Hoạt động") || status.contains("Đang hoạt động");
            
            Color bgCard;
            if (isSelected) {
                bgCard = new Color(230, 240, 255);
            } else if (isHovered) {
                bgCard = new Color(248, 250, 255);
            } else {
                bgCard = isActive ? new Color(243, 248, 255) : new Color(255, 245, 245);
            }
            g2.setColor(bgCard);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            Color borderColor;
            float borderStroke;
            if (isSelected) {
                borderColor = new Color(50, 100, 220);
                borderStroke = 2.5f;
            } else if (isHovered) {
                borderColor = new Color(120, 160, 240);
                borderStroke = 1.5f;
            } else {
                borderColor = isActive ? new Color(218, 230, 255) : new Color(254, 215, 215);
                borderStroke = 1.0f;
            }
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderStroke));
            g2.drawRoundRect((int)(borderStroke/2), (int)(borderStroke/2), getWidth() - (int)borderStroke - 1, getHeight() - (int)borderStroke - 1, 12, 12);

            Color topBarColor = isActive ? new Color(50, 120, 220) : new Color(220, 38, 38);
            g2.setColor(topBarColor);
            g2.fillRoundRect(2, 0, getWidth() - 4, 4, 4, 4);

            Color iconColor = isActive ? new Color(40, 100, 220) : new Color(220, 38, 38);
            int iconHeight = Math.min(28, Math.max(24, getHeight() - 56));
            drawTrainIcon(g2, (getWidth() - 22) / 2, 7, 22, iconHeight, iconColor);

            g2.dispose();
            super.paintComponent(g);
        }

        private void drawTrainIcon(Graphics2D g2, int x, int y, int w, int h, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            g2.drawRoundRect(x + 2, y + 2, w - 4, h - 6, 6, 6);
            g2.drawRect(x + 6, y + 8, w - 12, 10);
            g2.drawLine(x + w / 2, y + 8, x + w / 2, y + 18);
            
            g2.fillOval(x + 7, y + 24, 5, 5);
            g2.fillOval(x + w - 12, y + 24, 5, 5);
        }
    }

    private final class ToaCard extends JPanel {
        private final String maToa;
        private final int soToa;
        private final String loaiToa;
        private final boolean isSelected;

        ToaCard(String maToa, int soToa, String loaiToa, boolean isSelected) {
            this.maToa = maToa;
            this.soToa = soToa;
            this.loaiToa = loaiToa;
            this.isSelected = isSelected;

            setPreferredSize(new Dimension(53, 76));
            setOpaque(false);
            setLayout(new BorderLayout(0, 4));
            setBorder(new EmptyBorder(34, 4, 5, 4));

            JLabel lblToa = new JLabel("Toa " + String.format("%02d", soToa), SwingConstants.CENTER);
            lblToa.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 9));
            lblToa.setForeground(isSelected ? new Color(40, 100, 220) : new Color(80, 90, 100));
            add(lblToa, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedToaIdForDetail = maToa;
                    loadSeatsOfToa();
                    loadToaCards();
                }
            });
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bgCard = isSelected ? new Color(240, 245, 255) : Color.WHITE;
            g2.setColor(bgCard);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            Color borderColor = isSelected ? new Color(50, 100, 220) : new Color(220, 225, 235);
            float borderStroke = isSelected ? 2.5f : 1.0f;
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderStroke));
            g2.drawRoundRect((int)(borderStroke/2), (int)(borderStroke/2), getWidth() - (int)borderStroke - 1, getHeight() - (int)borderStroke - 1, 10, 10);

            Color topBarColor = isSelected ? new Color(50, 120, 220) : new Color(156, 163, 175);
            g2.setColor(topBarColor);
            g2.fillRoundRect(2, 0, getWidth() - 4, 3, 3, 3);

            Color iconColor = isSelected ? new Color(40, 100, 220) : new Color(156, 163, 175);
            boolean isVip = "TOA_VIP".equals(loaiToa) || (loaiToa != null && loaiToa.toUpperCase().contains("VIP"));
            String iconPath = isVip ? "/Images/logoToaVip.png" : "/Images/logoToaThuong.png";
            ImageIcon imgIcon = GuiIcons.loadIcon(QuanLyTauGUI.class, iconPath, 20, 24);
            if (imgIcon != null) {
                g2.drawImage(imgIcon.getImage(), (getWidth() - 20) / 2, 7, null);
            } else {
                drawToaIcon(g2, (getWidth() - 20) / 2, 7, 20, 24, iconColor);
            }

            g2.dispose();
            super.paintComponent(g);
        }

        private void drawToaIcon(Graphics2D g2, int x, int y, int w, int h, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            g2.drawRoundRect(x + 2, y + 2, w - 4, h - 10, 4, 4);
            g2.drawRect(x + 6, y + 7, 8, 8);
            g2.drawRect(x + w - 14, y + 7, 8, 8);
            
            g2.drawOval(x + 6, y + h - 8, 6, 6);
            g2.drawOval(x + w - 12, y + h - 8, 6, 6);
            
            g2.drawLine(x + 12, y + h - 5, x + w - 12, y + h - 5);
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

    private static final class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        public StatusBadgeRenderer() {
            panel.setOpaque(false);
            label.setOpaque(false);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
            label.setBorder(new EmptyBorder(4, 10, 4, 10));
            panel.add(label);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String status = value != null ? value.toString() : "";
            label.setText(status.toUpperCase());
            
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else {
                panel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                if (status.contains("Hoạt động") || status.contains("hoạt động") || status.contains("Đang hoạt động")) {
                    label.setBackground(new Color(222, 247, 236));
                    label.setForeground(new Color(3, 84, 63));
                } else if (status.contains("Bảo trì") || status.contains("bảo trì")) {
                    label.setBackground(new Color(253, 232, 232));
                    label.setForeground(new Color(224, 36, 36));
                } else {
                    label.setBackground(new Color(243, 244, 246));
                    label.setForeground(new Color(55, 65, 81));
                }
            }
            return panel;
        }
    }
}
