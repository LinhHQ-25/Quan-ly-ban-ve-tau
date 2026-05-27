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

final class TauGUI extends JPanel {
    // Static instance để QuanLyTauGUI có thể notify refresh
    private static TauGUI instance;

    /** Gọi từ QuanLyTauGUI sau khi thêm/sửa/xóa tàu, toa, ghế */
    static void notifyDataChanged() {
        if (instance != null) {
            SwingUtilities.invokeLater(instance::loadDataToTableAndCards);
        }
    }

    private static final Color BORDER = GuiTheme.SEARCH_FIELD_BORDER;
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    private JLabel lblResults;
    
    private JTextField txtMaTau, txtTenTau;
    private JComboBox<String> cboStatus;
    
    private JPanel pnlTrainCards;

    // Các thành phần cho chế độ xem chi tiết Toa & Ghế
    private CardLayout cardLayout;
    private JPanel pnlCenterCard;
    
    private JPanel pnlDetailView;
    private JPanel pnlToaCards;
    private JLabel lblDetailTitle;
    private JLabel lblDetailSummary;
    
    private DefaultTableModel tblSeatModel;
    private JTable tblSeatData;
    private JPanel pnlSeatGridContainer;
    
    private String selectedTrainIdForDetail = "";
    private String selectedToaIdForDetail = "";
    private JComboBox<String> cboChuyenTau;

    TauGUI() {
        instance = this;
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

        // Đặt Danh sách thẻ tàu lên TRƯỚC bộ lọc thông tin (BorderLayout.NORTH)
        pnlPage.add(buildTrainCardsSection(), BorderLayout.NORTH);
        
        // Sử dụng CardLayout để chuyển đổi linh hoạt giữa Tìm kiếm và Xem chi tiết Toa
        cardLayout = new CardLayout();
        pnlCenterCard = new JPanel(cardLayout);
        pnlCenterCard.setOpaque(false);

        // Card 1: Chế độ tìm kiếm tàu
        JPanel pnlSearchView = new JPanel(new BorderLayout(0, 4));
        pnlSearchView.setOpaque(false);
        pnlSearchView.add(buildFilterPanel(), BorderLayout.NORTH);
        pnlSearchView.add(buildTablePanel(), BorderLayout.CENTER);
        pnlCenterCard.add(pnlSearchView, "search");

        // Card 2: Chế độ xem chi tiết Toa & Ghế
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

        // Header badge đồng bộ với các panel khác (Danh sách toa tàu, Danh sách ghế)
        JLabel lblTitle = new JLabel("TRẠNG THÁI ĐỘI TÀU TRỰC TUYẾN");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(44, 82, 150));
        lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlLegend = buildTrainStatusLegend();
        pnlHeader.add(pnlLegend, BorderLayout.EAST);

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

    private JPanel buildTrainStatusLegend() {
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlLegend.setOpaque(false);

        pnlLegend.add(createTrainStatusLegendItem("Sẵn sàng", new Color(222, 247, 236), new Color(3, 84, 63)));
        pnlLegend.add(createTrainStatusLegendItem("Đang chạy", new Color(219, 234, 254), new Color(29, 78, 216)));
        pnlLegend.add(createTrainStatusLegendItem("Bảo trì kỹ thuật", new Color(254, 243, 199), new Color(180, 83, 9)));
        pnlLegend.add(createTrainStatusLegendItem("Bảo trì", new Color(254, 226, 226), new Color(220, 38, 38)));

        return pnlLegend;
    }

    private JPanel createTrainStatusLegendItem(String text, Color bg, Color fg) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnl.setOpaque(false);

        JLabel lblBadge = new JLabel(text.toUpperCase(), SwingConstants.CENTER) {
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
        lblBadge.setOpaque(false);
        lblBadge.setBackground(bg);
        lblBadge.setForeground(fg);
        lblBadge.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 8));
        lblBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        pnl.add(lblBadge);
        return pnl;
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

        // Empty spacer to occupy the 4th column
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
        JComboBox<String> cbo = new JComboBox<>(new String[] { "Tất cả", "Sẵn sàng", "Đang chạy", "Bảo trì kỹ thuật", "Bảo trì" });
        cbo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cbo.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cbo.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(cbo);
        cbo.setPreferredSize(new Dimension(150, 30));
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
        btn.setPreferredSize(new Dimension(120, 30));
        btn.setForeground(Color.WHITE);
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(TauGUI.class, "/Images/traCuu.png", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(TauGUI.class, "/Images/logoLammoi.png", 16, 16));
        
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

    // THIẾT KẾ TRANG CHI TIẾT TOA & GHẾ
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
        pnlTitleRow.add(lblDetailTitle, BorderLayout.WEST);

        lblDetailSummary = new JLabel("");
        lblDetailSummary.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblDetailSummary.setForeground(new Color(120, 130, 140));
        pnlTitleRow.add(lblDetailSummary, BorderLayout.EAST);

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
        scroll.setPreferredSize(new Dimension(0, 100));
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

        JPanel pnlSeatActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSeatActions.setOpaque(false);
        
        cboChuyenTau = new JComboBox<>();
        cboChuyenTau.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        cboChuyenTau.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cboChuyenTau.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(cboChuyenTau);
        cboChuyenTau.setPreferredSize(new Dimension(300, 30));
        cboChuyenTau.addActionListener(e -> loadSeatsOfToa());
        
        JLabel lblXemChuyen = new JLabel("Xem chuyến:");
        lblXemChuyen.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lblXemChuyen.setForeground(GuiTheme.NAVY);
        
        pnlSeatActions.add(lblXemChuyen);
        pnlSeatActions.add(cboChuyenTau);
        
        pnlHeader.add(pnlSeatActions, BorderLayout.EAST);

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

        // Dummy model/table to keep compilation happy and prevent exceptions
        tblSeatModel = new DefaultTableModel();
        tblSeatData = new JTable(tblSeatModel);

        pnlSeatGridContainer = new JPanel(new BorderLayout());
        pnlSeatGridContainer.setOpaque(false);
        pnlSeatGridContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        pnlOuter.add(pnlSeatGridContainer, BorderLayout.CENTER);
        return pnlOuter;
    }

    // CHUYỂN ĐỔI CHẾ ĐỘ XEM
    private void showDetailView(String trainId) {
        selectedTrainIdForDetail = trainId;
        lblDetailTitle.setText("DANH SÁCH TOA TÀU - " + trainId);
        selectedToaIdForDetail = ""; // Reset toa đã chọn

        loadChuyenTauOfTrain(trainId);
        // Tải các Toa của tàu này
        loadToaCards();

        // Chuyển sang Card Xem Chi Tiết
        cardLayout.show(pnlCenterCard, "detail");

        // Vẽ lại danh sách card tàu ở trên để cập nhật màu highlight xám
        if (pnlTrainCards != null) {
            pnlTrainCards.repaint();
        }
    }

    private void showSearchView() {
        selectedTrainIdForDetail = "";
        
        // Quay lại Card Tìm Kiếm
        cardLayout.show(pnlCenterCard, "search");

        // Vẽ lại danh sách card tàu để xóa màu highlight xám
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
                if (firstToaId.isEmpty()) {
                    firstToaId = maToa;
                }

                boolean isSel = maToa.equals(selectedToaIdForDetail);
                String loaiToa = rs.getString("loaiToa");
                pnlToaCards.add(new ToaCard(maToa, soToa, loaiToa, isSel));
                hasToa = true;
            }

            lblDetailSummary.setText("");

            if (hasToa) {
                // Nếu chưa có toa nào được chọn hoặc toa được chọn không nằm trong tàu này, chọn toa đầu tiên
                if (selectedToaIdForDetail.isEmpty() || !isToaInTrain(selectedToaIdForDetail)) {
                    selectedToaIdForDetail = firstToaId;
                    
                    // Vẽ lại toàn bộ các card toa để cập nhật nhãn được chọn
                    pnlToaCards.removeAll();
                    rs = stmt.executeQuery(); // Chạy lại query
                    while (rs.next()) {
                        String maToa = rs.getString("maToaTau");
                        int soToa = rs.getInt("soToa");
                        String loaiToa2 = rs.getString("loaiToa");
                        boolean isSel = maToa.equals(selectedToaIdForDetail);
                        pnlToaCards.add(new ToaCard(maToa, soToa, loaiToa2, isSel));
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

    private void loadChuyenTauOfTrain(String trainId) {
        new dao.ChuyenTauDAO().syncVoyageStatuses();
        if (cboChuyenTau == null) return;
        cboChuyenTau.removeAllItems();
        cboChuyenTau.addItem("Trạng thái kỹ thuật (Thiết lập)");

        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT ct.maChuyenTau, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, "
                       + "cct.thoiGianKhoiHanh, cct.thoiGianDuKien "
                       + "FROM ChuyenTau ct "
                       + "JOIN ChiTietChuyenTau cct ON ct.maChuyenTau = cct.maChuyenTau "
                       + "JOIN Ga gDi ON cct.maGaDi = gDi.maGa "
                       + "JOIN Ga gDen ON cct.maGaDen = gDen.maGa "
                       + "WHERE ct.maTau = ? AND ct.trangThai <> 'HUY' "
                       + "ORDER BY cct.thoiGianKhoiHanh DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, trainId);
            ResultSet rs = stmt.executeQuery();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                String maChuyen = rs.getString("maChuyenTau");
                String gaDi = rs.getString("gaDi");
                String gaDen = rs.getString("gaDen");
                Timestamp tgDi = rs.getTimestamp("thoiGianKhoiHanh");
                
                String display = String.format("%s | %s -> %s | %s", 
                    maChuyen, gaDi, gaDen, sdf.format(tgDi));
                cboChuyenTau.addItem(display);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSeatsOfToa() {
        if (pnlSeatGridContainer == null) return;
        pnlSeatGridContainer.removeAll();

        if (selectedToaIdForDetail.isEmpty()) {
            pnlSeatGridContainer.revalidate();
            pnlSeatGridContainer.repaint();
            return;
        }

        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        String selectedTripItem = cboChuyenTau != null ? (String) cboChuyenTau.getSelectedItem() : null;
        boolean viewTripOccupancy = selectedTripItem != null && !selectedTripItem.equals("Trạng thái kỹ thuật (Thiết lập)");
        String maChuyenTauSelected = "";
        java.util.Set<String> gheDaDatSet = new java.util.HashSet<>();
        
        if (viewTripOccupancy) {
            int pos = selectedTripItem.indexOf(" | ");
            if (pos > 0) {
                maChuyenTauSelected = selectedTripItem.substring(0, pos);
                try {
                    gheDaDatSet.addAll(new dao.VeDAO().layDanhSachMaGheDaDat(maChuyenTauSelected));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        List<SeatInfo> seatList = new ArrayList<>();
        int soToa = 1;
        String loaiToa = "";

        try {
            String sql = "SELECT g.maGhe, g.soGhe, g.loaiGhe, t.soToa, t.loaiToa, g.trangThai " +
                         "FROM Ghe g " +
                         "JOIN ToaTau t ON g.maToaTau = t.maToaTau " +
                         "WHERE t.maToaTau = ? " +
                         "ORDER BY CAST(g.soGhe AS INT) ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, selectedToaIdForDetail);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        SeatInfo s = new SeatInfo();
                        s.maGhe = rs.getString("maGhe");
                        s.soGhe = rs.getString("soGhe");
                        s.loaiGhe = rs.getString("loaiGhe");
                        s.soToa = rs.getInt("soToa");
                        s.loaiToa = rs.getString("loaiToa");
                        s.trangThai = rs.getString("trangThai");
                        seatList.add(s);
                        
                        soToa = s.soToa;
                        loaiToa = s.loaiToa;
                    }
                }
            }
        } catch (SQLException e) {
            try {
                String sql = "SELECT g.maGhe, g.soGhe, g.loaiGhe, t.soToa, t.loaiToa, g.trangThai " +
                             "FROM Ghe g " +
                             "JOIN ToaTau t ON g.maToaTau = t.maToaTau " +
                             "WHERE t.maToaTau = ? " +
                             "ORDER BY g.soGhe ASC";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, selectedToaIdForDetail);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            SeatInfo s = new SeatInfo();
                            s.maGhe = rs.getString("maGhe");
                            s.soGhe = rs.getString("soGhe");
                            s.loaiGhe = rs.getString("loaiGhe");
                            s.soToa = rs.getInt("soToa");
                            s.loaiToa = rs.getString("loaiToa");
                            s.trangThai = rs.getString("trangThai");
                            seatList.add(s);
                            
                            soToa = s.soToa;
                            loaiToa = s.loaiToa;
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        if (seatList.isEmpty()) {
            JLabel lblEmpty = new JLabel("Không có cấu hình ghế nào cho toa này.", SwingConstants.CENTER);
            lblEmpty.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 14));
            lblEmpty.setForeground(GuiTheme.SUB_TEXT);
            pnlSeatGridContainer.add(lblEmpty, BorderLayout.CENTER);
            pnlSeatGridContainer.revalidate();
            pnlSeatGridContainer.repaint();
            return;
        }

        // Robust sorting
        seatList.sort((s1, s2) -> {
            try {
                return Integer.compare(Integer.parseInt(s1.soGhe), Integer.parseInt(s2.soGhe));
            } catch (NumberFormatException e) {
                return s1.soGhe.compareTo(s2.soGhe);
            }
        });

        int totalSeats = seatList.size();
        int occupiedSeats = 0;
        int brokenSeats = 0;
        for (SeatInfo seat : seatList) {
            boolean isBaoTri = "BAO_TRI".equals(seat.trangThai) || "Bảo trì".equalsIgnoreCase(seat.trangThai);
            if (isBaoTri) {
                brokenSeats++;
            } else if (viewTripOccupancy && gheDaDatSet.contains(seat.maGhe)) {
                occupiedSeats++;
            }
        }
        int remainingSeats = totalSeats - occupiedSeats - brokenSeats;

        // Build Title
        String loaiToaStr = loaiToa;
        if (loaiToa != null) {
            if (loaiToa.equalsIgnoreCase("TOA_VIP") || loaiToa.equalsIgnoreCase("VIP")) {
                loaiToaStr = "Toa VIP (Giường nằm)";
            } else if (loaiToa.equalsIgnoreCase("TOA_THUONG") || loaiToa.equalsIgnoreCase("THUONG")) {
                if (!seatList.isEmpty()) {
                    String firstSeatLoai = seatList.get(0).loaiGhe;
                    if ("GHE_MEM".equalsIgnoreCase(firstSeatLoai)) {
                        loaiToaStr = "Toa Ghế mềm";
                    } else if ("GHE_CUNG".equalsIgnoreCase(firstSeatLoai)) {
                        loaiToaStr = "Toa Ghế cứng";
                    } else {
                        loaiToaStr = "Toa thường";
                    }
                } else {
                    loaiToaStr = "Toa thường";
                }
            }
        }

        JLabel lblCarriageTitle = new JLabel(String.format("Toa tàu số %d: %s", soToa, loaiToaStr), SwingConstants.CENTER);
        lblCarriageTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblCarriageTitle.setForeground(new Color(44, 82, 150));
        lblCarriageTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        pnlSeatGridContainer.add(lblCarriageTitle, BorderLayout.NORTH);

        // Carriage outline border
        JPanel pnlCarriage = new JPanel(new BorderLayout());
        pnlCarriage.setBackground(new Color(245, 247, 250));
        pnlCarriage.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 220), 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        int cols = (int) Math.ceil((double) totalSeats / 2);
        JPanel pnlGrid = new JPanel(new GridLayout(2, cols, 8, 8));
        pnlGrid.setOpaque(false);

        for (SeatInfo seat : seatList) {
            boolean isBaoTri = "BAO_TRI".equals(seat.trangThai) || "Bảo trì".equalsIgnoreCase(seat.trangThai);
            Color color;
            Color hoverColor;
            String statusDesc;

            if (isBaoTri) {
                color = new Color(229, 0, 0); // Red
                hoverColor = new Color(255, 50, 50);
                statusDesc = "Bảo trì";
            } else if (viewTripOccupancy) {
                if (gheDaDatSet.contains(seat.maGhe)) {
                    color = new Color(192, 192, 192); // Grey
                    hoverColor = new Color(210, 210, 210);
                    statusDesc = "Đã mua";
                } else {
                    color = new Color(70, 130, 180); // Blue
                    hoverColor = new Color(90, 150, 200);
                    statusDesc = "Còn trống";
                }
            } else {
                color = new Color(70, 130, 180); // Blue (Active)
                hoverColor = new Color(90, 150, 200);
                statusDesc = "Còn trống";
            }

            JButton btnSeat = new JButton(seat.soGhe);
            btnSeat.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
            btnSeat.setForeground(Color.WHITE);
            btnSeat.setBackground(color);
            btnSeat.setFocusPainted(false);
            btnSeat.setBorderPainted(false);
            btnSeat.setContentAreaFilled(true);
            btnSeat.setOpaque(true);
            btnSeat.setPreferredSize(new Dimension(60, 42));
            btnSeat.setToolTipText(String.format("Ghế %s | Loại: %s | Trạng thái: %s", seat.soGhe, loaiGheToVietnamese(seat.loaiGhe), statusDesc));

            Color normalColor = color;
            btnSeat.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btnSeat.setBackground(hoverColor); }
                @Override public void mouseExited(MouseEvent e) { btnSeat.setBackground(normalColor); }
            });

            // Action details
            if (!viewTripOccupancy) {
                // Technical setup mode - view only for normal staff
                btnSeat.addActionListener(evt -> {
                    JOptionPane.showMessageDialog(this,
                        String.format("Thông tin ghế:\n- Số ghế: %s (Mã: %s)\n- Loại ghế: %s\n- Trạng thái vật lý: %s\n(Nhân viên không có quyền thay đổi trạng thái kỹ thuật)", 
                                      seat.soGhe, seat.maGhe, loaiGheToVietnamese(seat.loaiGhe), statusDesc),
                        "Chi tiết ghế ngồi (Xem chi tiết)",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            } else {
                // View voyage mode
                btnSeat.addActionListener(evt -> {
                    JOptionPane.showMessageDialog(this,
                        String.format("Thông tin ghế:\n- Số ghế: %s (Mã: %s)\n- Loại ghế: %s\n- Trạng thái đặt vé: %s", 
                                      seat.soGhe, seat.maGhe, loaiGheToVietnamese(seat.loaiGhe), statusDesc),
                        "Chi tiết ghế ngồi",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            }

            pnlGrid.add(btnSeat);
        }

        JPanel pnlGridCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlGridCenter.setOpaque(false);
        pnlGridCenter.add(pnlGrid);
        pnlCarriage.add(pnlGridCenter, BorderLayout.CENTER);
        pnlSeatGridContainer.add(pnlCarriage, BorderLayout.CENTER);

        // Build Legend row exactly like the image
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlLegend.setOpaque(false);

        JLabel lblRemaining = new JLabel(String.format("Tổng số ghế còn lại: %d/%d", remainingSeats, totalSeats));
        lblRemaining.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lblRemaining.setForeground(GuiTheme.TEXT);
        pnlLegend.add(lblRemaining);

        pnlLegend.add(createLegendItem("Đã mua", new Color(192, 192, 192)));
        pnlLegend.add(createLegendItem("Còn trống", new Color(70, 130, 180)));
        pnlLegend.add(createLegendItem("Bảo trì", new Color(229, 0, 0)));

        pnlSeatGridContainer.add(pnlLegend, BorderLayout.SOUTH);

        // Sync with original lblDetailSummary
        if (viewTripOccupancy) {
            lblDetailSummary.setText(String.format("Đã bán: %d/%d ghế | Còn trống: %d ghế", 
                occupiedSeats, totalSeats, remainingSeats));
        } else {
            lblDetailSummary.setText("Tổng số ghế: " + totalSeats);
        }

        pnlSeatGridContainer.revalidate();
        pnlSeatGridContainer.repaint();
    }

    private JPanel createLegendItem(String labelText, Color color) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnl.setOpaque(false);
        
        JPanel pnlColor = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(color);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        pnlColor.setPreferredSize(new Dimension(20, 12));
        pnlColor.setBackground(color);
        
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(GuiTheme.TEXT);
        
        pnl.add(pnlColor);
        pnl.add(lbl);
        return pnl;
    }

    private static class SeatInfo {
        String maGhe;
        String soGhe;
        String loaiGhe;
        int soToa;
        String loaiToa;
        String trangThai;
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

        java.util.Map<String, String> dynamicStatusMap = new java.util.HashMap<>();
        try {
            for (Object[] row : new dao.tauDAO().getTauTrangThaiDong(null)) {
                dynamicStatusMap.put((String) row[0], (String) row[4]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT * FROM Tau WHERE maTau LIKE ? AND tenTau LIKE ?";
            String statusFilter = (String) cboStatus.getSelectedItem();

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaTau.getText().trim() + "%");
            stmt.setString(2, "%" + txtTenTau.getText().trim() + "%");

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            boolean hasGhiChu = false;
            try {
                rs.findColumn("ghiChu");
                hasGhiChu = true;
            } catch (SQLException e) {}

            boolean hasTrains = false;
            while (rs.next()) {
                String maTau = rs.getString("maTau");
                String tenTau = rs.getString("tenTau");
                int soToa = rs.getInt("soToa");
                int tongSoGhe = rs.getInt("tongSoGhe");
                String trangThaiStatic = rs.getString("trangThai");
                String ghiChu = hasGhiChu ? rs.getString("ghiChu") : "";

                String trangThaiDynamic = trangThaiStatic;
                if (trangThaiStatic.equals("Đang hoạt động")) {
                    trangThaiDynamic = dynamicStatusMap.getOrDefault(maTau, "Sẵn sàng");
                } else if (trangThaiStatic.equals("Bảo trì")) {
                    trangThaiDynamic = "Bảo trì";
                }

                // Filter in Java dynamically
                if (statusFilter != null && !statusFilter.equals("Tất cả")) {
                    if (!statusFilter.equalsIgnoreCase(trangThaiDynamic)) {
                        continue;
                    }
                }

                hasTrains = true;
                // Thêm vào bảng
                tblModel.addRow(new Object[] {
                    stt++, maTau, tenTau, soToa, tongSoGhe, trangThaiDynamic, ghiChu
                });

                // Thêm vào panel card tàu
                if (pnlTrainCards != null) {
                    pnlTrainCards.add(new TrainCard(maTau, tenTau, soToa, tongSoGhe, trangThaiDynamic));
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

    // CARD TÀU MẪU ĐẦU TÀU HOẠT HỌA CỰC KỲ ĐẸP MẮT
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

            String statusText;
            if (status.equalsIgnoreCase("Sẵn sàng")) {
                statusText = "SẴN SÀNG";
            } else if (status.equalsIgnoreCase("Đang chạy")) {
                statusText = "ĐANG CHẠY";
            } else if (status.equalsIgnoreCase("Bảo trì kỹ thuật")) {
                statusText = "BẢO TRÌ KT";
            } else {
                statusText = "BẢO TRÌ";
            }
            
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
            
            if (status.equalsIgnoreCase("Sẵn sàng")) {
                lblStatus.setBackground(new Color(222, 247, 236));
                lblStatus.setForeground(new Color(3, 84, 63));
            } else if (status.equalsIgnoreCase("Đang chạy")) {
                lblStatus.setBackground(new Color(219, 234, 254));
                lblStatus.setForeground(new Color(29, 78, 216));
            } else if (status.equalsIgnoreCase("Bảo trì kỹ thuật")) {
                lblStatus.setBackground(new Color(254, 243, 199));
                lblStatus.setForeground(new Color(180, 83, 9));
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

            Color bgCard;
            Color borderColor;
            float borderStroke;
            Color topBarColor;
            Color iconColor;

            if (status.equalsIgnoreCase("Sẵn sàng")) {
                bgCard = isSelected ? new Color(230, 248, 235) : (isHovered ? new Color(240, 253, 244) : new Color(245, 255, 248));
                borderColor = isSelected ? new Color(34, 197, 94) : (isHovered ? new Color(74, 222, 128) : new Color(187, 247, 208));
                topBarColor = new Color(34, 197, 94);
                iconColor = new Color(22, 163, 74);
            } else if (status.equalsIgnoreCase("Đang chạy")) {
                bgCard = isSelected ? new Color(230, 240, 255) : (isHovered ? new Color(248, 250, 255) : new Color(243, 248, 255));
                borderColor = isSelected ? new Color(50, 100, 220) : (isHovered ? new Color(120, 160, 240) : new Color(218, 230, 255));
                topBarColor = new Color(59, 130, 246);
                iconColor = new Color(37, 99, 235);
            } else if (status.equalsIgnoreCase("Bảo trì kỹ thuật")) {
                bgCard = isSelected ? new Color(255, 248, 220) : (isHovered ? new Color(255, 251, 235) : new Color(255, 253, 245));
                borderColor = isSelected ? new Color(245, 158, 11) : (isHovered ? new Color(251, 191, 36) : new Color(253, 230, 138));
                topBarColor = new Color(245, 158, 11);
                iconColor = new Color(217, 119, 6);
            } else { // Bảo trì (static)
                bgCard = isSelected ? new Color(255, 235, 235) : (isHovered ? new Color(255, 245, 245) : new Color(255, 250, 250));
                borderColor = isSelected ? new Color(239, 68, 68) : (isHovered ? new Color(248, 113, 113) : new Color(254, 215, 215));
                topBarColor = new Color(239, 68, 68);
                iconColor = new Color(220, 38, 38);
            }

            if (isSelected) {
                borderStroke = 2.5f;
            } else if (isHovered) {
                borderStroke = 1.5f;
            } else {
                borderStroke = 1.0f;
            }

            g2.setColor(bgCard);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderStroke));
            g2.drawRoundRect((int)(borderStroke/2), (int)(borderStroke/2), getWidth() - (int)borderStroke - 1, getHeight() - (int)borderStroke - 1, 12, 12);

            g2.setColor(topBarColor);
            g2.fillRoundRect(2, 0, getWidth() - 4, 4, 4, 4);

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

    // CARD TOA TÀU - đồng bộ với QuanLyTauGUI
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

            setPreferredSize(new Dimension(85, 90));
            setOpaque(false);
            setLayout(new BorderLayout(0, 4));
            setBorder(new EmptyBorder(46, 5, 5, 5));

            JLabel lblToa = new JLabel("Toa " + String.format("%02d", soToa), SwingConstants.CENTER);
            lblToa.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
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
            ImageIcon imgIcon = GuiIcons.loadIcon(TauGUI.class, iconPath, 32, 36);
            if (imgIcon != null) {
                g2.drawImage(imgIcon.getImage(), (getWidth() - 32) / 2, 8, null);
            } else {
                drawToaIcon(g2, (getWidth() - 32) / 2, 8, 32, 36, iconColor);
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
                if (status.equalsIgnoreCase("Sẵn sàng") || status.equalsIgnoreCase("Hoạt động") || status.equalsIgnoreCase("Đang hoạt động") || status.equalsIgnoreCase("Còn trống")) {
                    label.setBackground(new Color(222, 247, 236)); // Light green
                    label.setForeground(new Color(3, 84, 63)); // Dark green
                } else if (status.equalsIgnoreCase("Đang chạy")) {
                    label.setBackground(new Color(219, 234, 254)); // Light blue
                    label.setForeground(new Color(29, 78, 216)); // Dark blue
                } else if (status.equalsIgnoreCase("Bảo trì kỹ thuật")) {
                    label.setBackground(new Color(254, 243, 199)); // Light amber/orange
                    label.setForeground(new Color(180, 83, 9)); // Dark orange
                } else if (status.equalsIgnoreCase("Bảo trì") || status.equalsIgnoreCase("Đang sử dụng")) {
                    label.setBackground(new Color(253, 232, 232)); // Light red
                    label.setForeground(new Color(224, 36, 36)); // Dark red
                } else {
                    label.setBackground(new Color(243, 244, 246)); // Light gray
                    label.setForeground(new Color(55, 65, 81)); // Dark gray
                }
            }
            return panel;
        }
    }
}
