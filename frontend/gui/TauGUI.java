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
        
        // Phần Center chứa Bộ lọc ở TRÊN và Bảng ở DƯỚI
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 12));
        pnlCenter.setOpaque(false);
        pnlCenter.add(buildFilterPanel(), BorderLayout.NORTH);
        pnlCenter.add(buildTablePanel(), BorderLayout.CENTER);
        
        pnlPage.add(pnlCenter, BorderLayout.CENTER);

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
        scroll.setPreferredSize(new Dimension(0, 120)); // Chiều cao tối ưu cho thiết kế card mới
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

                // Add to table
                tblModel.addRow(new Object[] {
                    stt++, maTau, tenTau, soToa, tongSoGhe, trangThai, ghiChu
                });

                // Add to card panel
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

    // TÁI TẠO CARD TÀU ĐỘC ĐÁO THEO HÌNH DẠNG "ĐẦU TÀU" NHƯ ẢNH MẪU
    private final class TrainCard extends JPanel {
        TrainCard(String id, String name, int toa, int ghe, String status) {
            setPreferredSize(new Dimension(110, 90));
            setOpaque(false);
            setLayout(null); // Sử dụng absolute layout để định vị chính xác tuyệt đối các nhãn

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
            lblStatus.setForeground(new Color(27, 85, 131)); // Màu chữ xanh dương đậm sang trọng
            lblStatus.setBounds(10, 26, 90, 44);
            add(lblStatus);

            // 3. Sự kiện Click chuột vào card để tự động điền Mã tàu vào bộ lọc thông tin
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (txtMaTau.getText().trim().equals(id)) {
                        txtMaTau.setText("");
                    } else {
                        txtMaTau.setText(id);
                    }
                    loadDataToTableAndCards();
                }
            });
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Màu của thân tàu (Khung ngoài): Màu Xám cho chiếc đầu tiên hoặc khi được click chọn, màu Xanh Dương cho các chiếc khác
            Color bodyColor;
            String currentFilter = txtMaTau.getText().trim();
            
            // Nếu card này đang được click chọn (hoặc nếu là chiếc đầu tiên và bộ lọc trống)
            if (currentFilter.equals(tblModel.getValueAt(0, 1)) && tblModel.getRowCount() > 0 && currentFilter.equals(tblModel.getValueAt(0, 1)) && currentFilter.isEmpty()) {
                bodyColor = new Color(156, 163, 175); // Màu xám nhạt cao cấp
            } else if (!currentFilter.isEmpty() && currentFilter.equals(tblModel.getValueAt(0, 1))) {
                bodyColor = new Color(156, 163, 175);
            } else if (tblModel.getRowCount() > 0 && tblModel.getValueAt(0, 1).equals(tblModel.getValueAt(0, 1)) && currentFilter.isEmpty() && pnlTrainCards.getComponent(0) == this) {
                // Chiếc card đầu tiên mặc định xám như ảnh mẫu khi chưa lọc gì
                bodyColor = new Color(156, 163, 175);
            } else if (!currentFilter.isEmpty() && currentFilter.equalsIgnoreCase((String)tblModel.getValueAt(0, 1))) {
                bodyColor = new Color(156, 163, 175);
            } else {
                bodyColor = new Color(70, 130, 180); // Màu xanh dương dịu mắt (Steel Blue)
            }

            // A. Vẽ thân tàu chính (Rounded Rectangle)
            g2.setColor(bodyColor);
            g2.fillRoundRect(5, 12, 100, 66, 16, 16);

            // B. Vẽ 2 bánh xe nhỏ màu trắng ở đáy thân tàu (wheels)
            g2.setColor(Color.WHITE);
            // Bánh xe trái
            g2.fillOval(25, 74, 10, 10);
            g2.setColor(bodyColor);
            g2.drawOval(25, 74, 10, 10);

            // Bánh xe phải
            g2.setColor(Color.WHITE);
            g2.fillOval(75, 74, 10, 10);
            g2.setColor(bodyColor);
            g2.drawOval(75, 74, 10, 10);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
