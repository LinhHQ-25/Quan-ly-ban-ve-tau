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
    
    private String selectedTrainIdForDetail = "";
    private String selectedToaIdForDetail = "";

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

        JPanel line = new JPanel();
        line.setBackground(new Color(230, 235, 245));
        line.setPreferredSize(new java.awt.Dimension(0, 1));
        pnlHeader.add(line, BorderLayout.SOUTH);

        pnlOuter.add(pnlHeader, BorderLayout.NORTH);

        // Đồng bộ cột với QuanLyTauGUI: thêm Số ghế + Trạng thái
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
            if (i == 5) { // Cột Trạng thái
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

    // CHUYỂN ĐỔI CHẾ ĐỘ XEM
    private void showDetailView(String trainId) {
        selectedTrainIdForDetail = trainId;
        lblDetailTitle.setText("DANH SÁCH TOA TÀU - " + trainId);
        selectedToaIdForDetail = ""; // Reset toa đã chọn

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

    private void loadSeatsOfToa() {
        if (tblSeatModel == null) return;
        tblSeatModel.setRowCount(0);

        if (selectedToaIdForDetail.isEmpty()) return;

        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            // Đồng bộ với QuanLyTauGUI: thêm soGhe + trangThai
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

                // Thêm vào bảng
                tblModel.addRow(new Object[] {
                    stt++, maTau, tenTau, soToa, tongSoGhe, trangThai, ghiChu
                });

                // Thêm vào panel card tàu
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
            ImageIcon imgIcon = GuiIcons.loadIcon(TauGUI.class, iconPath, 20, 24);
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
                    label.setBackground(new Color(222, 247, 236)); // Light green
                    label.setForeground(new Color(3, 84, 63)); // Dark green
                } else if (status.contains("Bảo trì") || status.contains("bảo trì")) {
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
