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
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BorderLayout(0, 12));
        pnlPage.setBorder(new EmptyBorder(
            10,
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
        JPanel pnlSearchView = new JPanel(new BorderLayout(0, 12));
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
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitle.setOpaque(false);
        JLabel lbl = new JLabel("Danh sách tàu");
        lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(GuiTheme.TEXT);
        pnlTitle.add(lbl);
        pnl.add(pnlTitle, BorderLayout.NORTH);

        pnlTrainCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlTrainCards.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(pnlTrainCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 120));
        scroll.getHorizontalScrollBar().setUnitIncrement(15);
        
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel buildFilterPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout(0, 5));
        
        JLabel lblTitle = new JLabel("Thông tin tra cứu");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(GuiTheme.TEXT);
        lblTitle.setBorder(new EmptyBorder(10, 15, 0, 15));
        lblTitle.setIcon(GuiIcons.loadIcon(TauGUI.class, "/Images/traCuu.png", 18, 18));
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

        btnSearch.addActionListener(e -> loadDataToTableAndCards());
        btnReset.addActionListener(e -> {
            txtMaTau.setText(""); txtTenTau.setText("");
            txtMinToa.setText(""); txtMinGhe.setText("");
            cboStatus.setSelectedIndex(0);
            loadDataToTableAndCards();
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
        
        if (text.contains("Tìm kiếm")) btn.setIcon(GuiIcons.loadIcon(TauGUI.class, "/Images/traCuu.png", 16, 16));
        else if (text.contains("Xóa trắng")) btn.setIcon(GuiIcons.loadIcon(TauGUI.class, "/Images/logoLammoi.png", 16, 16));
        
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

    // THIẾT KẾ TRANG CHI TIẾT TOA & GHẾ
    private JPanel buildDetailView() {
        pnlDetailView = new JPanel(new BorderLayout(0, 12));
        pnlDetailView.setOpaque(false);

        // Header của Chi tiết Toa
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Nhãn tiêu đề tàu và Nút quay lại
        JPanel pnlLeftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeftHeader.setOpaque(false);

        JButton btnBack = new JButton("Quay lại") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY.darker() : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        btnBack.setPreferredSize(new Dimension(90, 26));
        btnBack.setContentAreaFilled(false); btnBack.setBorderPainted(false); btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> showSearchView());

        lblDetailTitle = new JLabel("Tàu SEA0001");
        lblDetailTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lblDetailTitle.setForeground(Color.WHITE);
        lblDetailTitle.setOpaque(true);
        lblDetailTitle.setBackground(PRIMARY);
        lblDetailTitle.setBorder(new EmptyBorder(4, 10, 4, 10));

        pnlLeftHeader.add(btnBack);
        pnlLeftHeader.add(lblDetailTitle);
        pnlHeader.add(pnlLeftHeader, BorderLayout.WEST);

        // Tổng số toa bên phải
        lblDetailSummary = new JLabel("Tổng số toa: 0");
        lblDetailSummary.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lblDetailSummary.setForeground(GuiTheme.TEXT);
        pnlHeader.add(lblDetailSummary, BorderLayout.EAST);

        pnlDetailView.add(pnlHeader, BorderLayout.NORTH);

        // Khung trượt hiển thị danh sách các toa tàu hoạt họa
        pnlToaCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        pnlToaCards.setOpaque(false);

        JScrollPane scroll = new JScrollPane(pnlToaCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 95));
        scroll.getHorizontalScrollBar().setUnitIncrement(15);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setOpaque(false);
        pnlContent.add(scroll, BorderLayout.NORTH);
        pnlContent.add(buildSeatTablePanel(), BorderLayout.CENTER);

        pnlDetailView.add(pnlContent, BorderLayout.CENTER);

        return pnlDetailView;
    }

    private JPanel buildSeatTablePanel() {
        JPanel pnlWrap = new JPanel(new BorderLayout(0, 8));
        pnlWrap.setOpaque(false);
        pnlWrap.add(buildSectionTitle("Danh sách ghế trong toa"), BorderLayout.NORTH);

        String[] cols = {"STT", "Mã ghế", "Loại ghế", "Số toa"};
        tblSeatModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSeatData = new JTable(tblSeatModel);
        tblSeatData.setRowHeight(32);
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
            tblSeatData.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
        }
        ((DefaultTableCellRenderer)tblSeatData.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane spnScroll = new JScrollPane(tblSeatData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);

        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    // CHUYỂN ĐỔI CHẾ ĐỘ XEM
    private void showDetailView(String trainId) {
        selectedTrainIdForDetail = trainId;
        lblDetailTitle.setText("Tàu " + trainId);
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
                pnlToaCards.add(new ToaCard(maToa, soToa, isSel));
                hasToa = true;
            }

            lblDetailSummary.setText("Tổng số toa: " + count);

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
                        boolean isSel = maToa.equals(selectedToaIdForDetail);
                        pnlToaCards.add(new ToaCard(maToa, soToa, isSel));
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
            String sql = "SELECT g.maGhe, g.soGhe, g.loaiGhe, t.soToa " +
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
                    rs.getString("loaiGhe"),
                    rs.getInt("soToa")
                });
            }
        } catch (SQLException e) {
            // Trường hợp cast soGhe sang INT thất bại (ví dụ có chứa chữ cái), fallback sắp xếp chuỗi thường
            try {
                String sql = "SELECT g.maGhe, g.soGhe, g.loaiGhe, t.soToa " +
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
                        rs.getString("loaiGhe"),
                        rs.getInt("soToa")
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
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

    // CARD TÀU MẪU ĐẦU TÀU HOẠT HỌA CỰC KỲ ĐẸP MẮT
    private final class TrainCard extends JPanel {
        private final String cardId;

        TrainCard(String id, String name, int toa, int ghe, String status) {
            this.cardId = id;
            setPreferredSize(new Dimension(110, 90));
            setOpaque(false);
            setLayout(null);

            // 1. Nhãn mã tàu bo tròn nằm nhô lên ở góc trên cùng
            JLabel lblId = new JLabel(id, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(210, 214, 219));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblId.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 10));
            lblId.setForeground(Color.BLACK);
            lblId.setBounds(20, 3, 70, 18);
            add(lblId);

            // 2. Khung màu trắng bo tròn ở giữa chứa chữ "Hoạt Động" hoặc "Bảo Trì"
            String textStatus = status.contains("hoạt động") || status.contains("Hoạt động") ? "Hoạt Động" : "Bảo Trì";
            JLabel lblStatus = new JLabel(textStatus, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblStatus.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            lblStatus.setForeground(new Color(27, 85, 131));
            lblStatus.setBounds(10, 26, 90, 44);
            add(lblStatus);

            // Click vào card Tàu để nhảy sang trang xem chi tiết các Toa tàu trong tàu đó!
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDetailView(cardId);
                }
            });
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Màu của thân tàu chính
            Color bodyColor;
            if (cardId.equals(selectedTrainIdForDetail)) {
                bodyColor = new Color(156, 163, 175); // Màu xám cho tàu đang chọn
            } else {
                bodyColor = new Color(70, 130, 180); // Màu xanh dương cho các tàu khác
            }

            // A. Vẽ thân tàu chính
            g2.setColor(bodyColor);
            g2.fillRoundRect(5, 12, 100, 66, 16, 16);

            // B. Vẽ 2 bánh xe nhỏ màu trắng
            g2.setColor(Color.WHITE);
            g2.fillOval(25, 74, 10, 10);
            g2.setColor(bodyColor);
            g2.drawOval(25, 74, 10, 10);

            g2.setColor(Color.WHITE);
            g2.fillOval(75, 74, 10, 10);
            g2.setColor(bodyColor);
            g2.drawOval(75, 74, 10, 10);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // CARD TOA TÀU HOẠT HỌA THEO PHÁC THẢO DỄ THƯƠNG
    private final class ToaCard extends JPanel {
        private final String maToa;
        private final int soToa;
        private final boolean isSelected;

        ToaCard(String maToa, int soToa, boolean isSelected) {
            this.maToa = maToa;
            this.soToa = soToa;
            this.isSelected = isSelected;

            setPreferredSize(new Dimension(65, 80));
            setOpaque(false);
            setLayout(null);

            // Nhãn chữ ghi tên toa ở dưới, ví dụ "T15"
            JLabel lblToa = new JLabel("T" + String.format("%02d", soToa), SwingConstants.CENTER);
            lblToa.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            lblToa.setForeground(isSelected ? PRIMARY : new Color(80, 90, 100));
            lblToa.setBounds(0, 62, 65, 18);
            add(lblToa);

            // Click chọn toa để hiển thị danh sách các ghế trong toa đó lên JTable bên dưới!
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedToaIdForDetail = maToa;
                    loadSeatsOfToa();
                    loadToaCards(); // Reload để vẽ lại viền được chọn
                }
            });
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color color = isSelected ? PRIMARY : new Color(108, 117, 125);

            // 1. Vẽ chiếc ống khói/nhô lên nhỏ trên nóc toa tàu
            g2.setColor(color);
            g2.fillRoundRect(27, 2, 10, 6, 2, 2);

            // 2. Vẽ thân toa tàu dạng hình chữ nhật bo tròn góc (khung chính)
            g2.fillRoundRect(4, 8, 56, 38, 12, 12);

            // 3. Vẽ 3 ô cửa sổ nhỏ màu trắng bên trong
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(11, 14, 8, 20, 3, 3);
            g2.fillRoundRect(24, 14, 8, 20, 3, 3);
            g2.fillRoundRect(37, 14, 8, 20, 3, 3);

            // 4. Vẽ 2 bánh xe nhỏ màu trắng bên dưới
            g2.setColor(Color.WHITE);
            g2.fillOval(14, 44, 10, 10);
            g2.setColor(color);
            g2.drawOval(14, 44, 10, 10);

            g2.setColor(Color.WHITE);
            g2.fillOval(40, 44, 10, 10);
            g2.setColor(color);
            g2.drawOval(40, 44, 10, 10);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
