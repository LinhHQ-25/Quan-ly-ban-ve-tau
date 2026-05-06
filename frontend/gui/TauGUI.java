package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import connect_DB.Connect_DB;
import entity.Tau;

final class TauGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color CARD_BORDER = new Color(125, 192, 225);
    private static final Color ACTIVE_BG = new Color(229, 244, 234);
    private static final Color ACTIVE_TEXT = new Color(31, 125, 70);
    private static final Color PRIMARY = new Color(71, 71, 156);
    private static final Color MAINTAIN_BG = new Color(252, 239, 220);
    private static final Color MAINTAIN_TEXT = new Color(166, 107, 24);
    private static final int CARD_WIDTH = 118;
    private static final int CARD_HEIGHT = 82;
    private static final int CARD_GAP_X = 26;
    private static final int VISIBLE_CARD_COLUMNS = 4;

    private DefaultTableModel tblModel;
    private JTable tblData;
    private JPanel pnlCards;
    private JScrollPane scrollCards;
    
    private JTextField txtMaTau, txtTenTau;
    private JComboBox<String> cboStatus;

    TauGUI() {
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

        JPanel pnlTop = new JPanel(new BorderLayout(0, 12));
        pnlTop.setOpaque(false);
        pnlTop.add(buildTrainPreviewPanel(), BorderLayout.NORTH);
        pnlTop.add(buildFilterPanel(), BorderLayout.CENTER);

        pnlPage.add(pnlTop, BorderLayout.NORTH);
        pnlPage.add(buildTablePanel(), BorderLayout.CENTER);

        add(pnlPage, BorderLayout.CENTER);
        
        loadTrainCards();
        loadDataToTable();
    }

    private JScrollPane buildTrainPreviewPanel() {
        pnlCards = new JPanel(new FlowLayout(FlowLayout.LEFT, CARD_GAP_X, 0));
        pnlCards.setOpaque(false);

        scrollCards = new JScrollPane(
            pnlCards,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrollCards.setOpaque(false);
        scrollCards.getViewport().setOpaque(false);
        scrollCards.setBorder(BorderFactory.createEmptyBorder());
        scrollCards.getHorizontalScrollBar().setUnitIncrement(22);
        scrollCards.setPreferredSize(new Dimension(
            VISIBLE_CARD_COLUMNS * CARD_WIDTH + (VISIBLE_CARD_COLUMNS - 1) * CARD_GAP_X + 4,
            CARD_HEIGHT + 20
        ));
        return scrollCards;
    }

    private void loadTrainCards() {
        pnlCards.removeAll();
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT * FROM Tau";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pnlCards.add(buildTrainCard(
                    rs.getString("maTau"),
                    rs.getString("tenTau"),
                    rs.getString("trangThai")
                ));
            }
            pnlCards.revalidate();
            pnlCards.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel buildTrainCard(String code, String name, String status) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(2, 2, 2, 2));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        JPanel shell = new RoundedPanel(18, Color.WHITE, CARD_BORDER, 2f);
        shell.setLayout(new BorderLayout(0, 4));
        shell.setBorder(new EmptyBorder(7, 8, 7, 8));

        JPanel imagePanel = new RoundedPanel(14, new Color(238, 247, 253), CARD_BORDER, 1.5f);
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(96, 24));

        JLabel image = new JLabel(loadTrainIcon(), SwingConstants.CENTER);
        imagePanel.add(image, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 3));
        footer.setOpaque(false);

        JLabel lbCode = new JLabel(code, SwingConstants.CENTER);
        lbCode.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        lbCode.setForeground(GuiTheme.TEXT);

        JLabel lbName = new JLabel(name, SwingConstants.CENTER);
        lbName.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 9));
        lbName.setForeground(GuiTheme.SUB_TEXT);

        JLabel lbStatus = buildStatusTag(status);

        footer.add(lbCode, BorderLayout.NORTH);
        footer.add(lbName, BorderLayout.CENTER);
        footer.add(lbStatus, BorderLayout.SOUTH);

        shell.add(imagePanel, BorderLayout.NORTH);
        shell.add(footer, BorderLayout.CENTER);
        card.add(shell, BorderLayout.CENTER);
        return card;
    }

    private Icon loadTrainIcon() {
        return GuiIcons.loadIcon(TauGUI.class, "/Images/logoTrain.png", 58, 20);
    }

    private JLabel buildStatusTag(String status) {
        String displayStatus = "HOAT_DONG".equals(status) || "Hoạt động".equals(status) ? "Hoạt động" : "Bảo trì";
        Color bg = "Hoạt động".equals(displayStatus) ? ACTIVE_BG : MAINTAIN_BG;
        Color fg = "Hoạt động".equals(displayStatus) ? ACTIVE_TEXT : MAINTAIN_TEXT;

        JLabel lbStatus = new JLabel(displayStatus, SwingConstants.CENTER);
        lbStatus.setOpaque(true);
        lbStatus.setBackground(bg);
        lbStatus.setForeground(fg);
        lbStatus.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 10));
        lbStatus.setBorder(new EmptyBorder(3, 8, 3, 8));
        return lbStatus;
    }

    private JPanel buildFilterPanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout(20, 0));
        pnlOuter.setOpaque(false);

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);
        pnlGrid.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER, 1, true),
            "Thông tin tra cứu",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            GuiTheme.font("Segoe UI", Font.BOLD, 13),
            PRIMARY
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        gbc.gridy = 0;
        gbc.gridx = 0;
        txtMaTau = buildTextField(160);
        pnlGrid.add(buildField("Mã tàu", txtMaTau), gbc);

        gbc.gridx = 1;
        txtTenTau = buildTextField(180);
        pnlGrid.add(buildField("Tên tàu:", txtTenTau), gbc);

        gbc.gridx = 2;
        cboStatus = buildStatusCombo();
        pnlGrid.add(buildField("Trạng thái:", cboStatus), gbc);

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        JPanel pnlAction = buildActionBlock();
        pnlAction.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER, 1, true),
            "Thao tác",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            GuiTheme.font("Segoe UI", Font.BOLD, 13),
            PRIMARY
        ));
        
        // Wrap action panel to prevent it from stretching vertically too much if needed, 
        // or just use BorderLayout.EAST as is but ensure buttons are centered.
        JPanel pnlActionWrapper = new JPanel(new GridBagLayout());
        pnlActionWrapper.setOpaque(false);
        pnlActionWrapper.add(pnlAction);
        
        pnlOuter.add(pnlActionWrapper, BorderLayout.EAST);
        return pnlOuter;
    }

    private JPanel buildField(String label, Component comp) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 4));
        pnlField.setOpaque(false);
        JLabel lbField = new JLabel(label);
        lbField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbField.setForeground(PRIMARY);
        pnlField.add(lbField, BorderLayout.NORTH);
        pnlField.add(comp, BorderLayout.CENTER);
        return pnlField;
    }

    private JTextField buildTextField(int width) {
        JTextField txtField = new JTextField();
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setForeground(PRIMARY);
        txtField.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        txtField.setPreferredSize(new Dimension(width, 28));
        return txtField;
    }

    private JComboBox<String> buildStatusCombo() {
        JComboBox<String> cbo = new JComboBox<>(new String[] { "", "Hoạt động", "Bảo trì" });
        cbo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cbo.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cbo.setForeground(PRIMARY);
        cbo.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        cbo.setPreferredSize(new Dimension(120, 28));
        return cbo;
    }

    private JPanel buildActionBlock() {
        JPanel pnlButtons = new JPanel();
        pnlButtons.setOpaque(false);
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.Y_AXIS));
        pnlButtons.setBorder(new EmptyBorder(10, 15, 10, 15));

        JButton btnSearch = buildNavyButton("Tra cứu", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa bộ lọc", new Color(110, 125, 156), new Color(130, 145, 176));

        btnSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadDataToTable();
            }
        });

        btnReset.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                txtMaTau.setText("");
                txtTenTau.setText("");
                cboStatus.setSelectedIndex(0);
                loadDataToTable();
            }
        });

        pnlButtons.add(btnReset);
        pnlButtons.add(Box.createVerticalStrut(8));
        pnlButtons.add(btnSearch);
        return pnlButtons;
    }

    private JButton buildNavyButton(String text, Color baseColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? baseColor.darker()
                    : getModel().isRollover() ? hoverColor : baseColor);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,(getWidth()-fm.stringWidth(text))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(115, 32));
        btn.setMaximumSize(new Dimension(115, 32));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlWrap = new JPanel(new BorderLayout(0, 8));
        pnlWrap.setOpaque(false);
        pnlWrap.add(buildSectionTitle("Danh sách tàu"), BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
            new Object[] { "STT", "Mã tàu", "Tên tàu", "Trạng thái", "Ghi chú" },
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tblData = new JTable(tblModel);
        tblData.setRowHeight(28);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.setForeground(GuiTheme.TEXT);
        tblData.setGridColor(new Color(230, 233, 238));
        tblData.setSelectionBackground(new Color(207, 209, 214));
        tblData.setSelectionForeground(GuiTheme.TEXT);
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblData.getTableHeader().setBackground(Color.WHITE);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblData.getColumnModel().getColumn(0).setCellRenderer(center);
        tblData.getColumnModel().getColumn(1).setCellRenderer(center);
        tblData.getColumnModel().getColumn(3).setCellRenderer(center);

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);

        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 5) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(60);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(250);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(150);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(300);
            }
        });

        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    private void loadDataToTable() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT * FROM Tau WHERE maTau LIKE ? AND tenTau LIKE ?";
            String statusFilter = (String) cboStatus.getSelectedItem();
            if (statusFilter != null && !statusFilter.isEmpty()) {
                sql += " AND trangThai = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaTau.getText().trim() + "%");
            stmt.setString(2, "%" + txtTenTau.getText().trim() + "%");
            if (statusFilter != null && !statusFilter.isEmpty()) {
                String dbStatus = statusFilter.equals("Hoạt động") ? "HOAT_DONG" : "BAO_TRI";
                stmt.setString(3, dbStatus);
            }

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            boolean hasGhiChu = false;
            try {
                rs.findColumn("ghiChu");
                hasGhiChu = true;
            } catch (SQLException e) {
                // Column doesn't exist
            }

            while (rs.next()) {
                String status = rs.getString("trangThai");
                String displayStatus = "HOAT_DONG".equals(status) ? "Hoạt động" : "Bảo trì";
                tblModel.addRow(new Object[] {
                    stt++,
                    rs.getString("maTau"),
                    rs.getString("tenTau"),
                    displayStatus,
                    hasGhiChu ? rs.getString("ghiChu") : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
