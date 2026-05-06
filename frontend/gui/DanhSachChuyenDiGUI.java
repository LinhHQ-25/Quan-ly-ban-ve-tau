package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import connect_DB.Connect_DB;

final class DanhSachChuyenDiGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JComboBox<String> cboGaDi, cboGaDen, cboTau;
    private JDateChooser dcNgayDi;
    private JSpinner spnSeat;
    private JRadioButton rdoThuong, rdoVip;

    DanhSachChuyenDiGUI() {
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
        
        initFilterData();
        loadDataToTable();
    }

    private void initFilterData() {
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;
        try {
            // Load Ga
            String sqlGa = "SELECT tenGa FROM Ga";
            ResultSet rsGa = conn.createStatement().executeQuery(sqlGa);
            cboGaDi.removeAllItems();
            cboGaDen.removeAllItems();
            cboGaDi.addItem("");
            cboGaDen.addItem("");
            while (rsGa.next()) {
                String name = rsGa.getString("tenGa");
                cboGaDi.addItem(name);
                cboGaDen.addItem(name);
            }

            // Load Tau
            String sqlTau = "SELECT tenTau FROM Tau";
            ResultSet rsTau = conn.createStatement().executeQuery(sqlTau);
            cboTau.removeAllItems();
            cboTau.addItem("");
            while (rsTau.next()) {
                cboTau.addItem(rsTau.getString("tenTau"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        cboGaDi = buildCombo();
        gbc.gridx = 0; pnlGrid.add(buildField("Ga đi:", cboGaDi), gbc);
        
        cboGaDen = buildCombo();
        gbc.gridx = 1; pnlGrid.add(buildField("Ga đến:", cboGaDen), gbc);
        
        dcNgayDi = buildDateField();
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày đi:", dcNgayDi), gbc);
        
        cboTau = buildCombo();
        gbc.gridx = 3; pnlGrid.add(buildField("Mã /Tên tàu:", cboTau), gbc);

        gbc.gridy = 1;
        spnSeat = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        gbc.gridx = 0; pnlGrid.add(buildField("Ghế trống tối thiểu:", spnSeat), gbc);
        
        gbc.gridx = 1;
        pnlGrid.add(buildTypeBlock(), gbc);
        
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

    private JComboBox<String> buildCombo(String... values) {
        JComboBox<String> cmb = new JComboBox<>(values);
        cmb.setEditable(true);
        cmb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cmb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cmb.setForeground(PRIMARY);
        cmb.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        cmb.setPreferredSize(new Dimension(160, 28));
        return cmb;
    }

    private JDateChooser buildDateField() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setPreferredSize(new Dimension(160, 28));
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(GuiTheme.SEARCH_FIELD_BG);
        dc.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        return dc;
    }

    private JPanel buildTypeBlock() {
        JPanel pnlBlock = new JPanel(new BorderLayout(0, 4));
        pnlBlock.setOpaque(false);
        JLabel lbBlock = new JLabel("Loại toa");
        lbBlock.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbBlock.setForeground(PRIMARY);
        JPanel pnlOptions = new JPanel();
        pnlOptions.setOpaque(false);
        pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
        ButtonGroup grp = new ButtonGroup();
        rdoThuong = buildRadio("Toa thường");
        rdoVip = buildRadio("Toa Vip");
        grp.add(rdoThuong);
        grp.add(rdoVip);
        rdoThuong.setSelected(true);
        pnlOptions.add(rdoThuong);
        pnlOptions.add(rdoVip);
        pnlBlock.add(lbBlock, BorderLayout.NORTH);
        pnlBlock.add(pnlOptions, BorderLayout.CENTER);
        return pnlBlock;
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
                cboGaDi.setSelectedIndex(0);
                cboGaDen.setSelectedIndex(0);
                cboTau.setSelectedIndex(0);
                dcNgayDi.setDate(null);
                spnSeat.setValue(0);
                rdoThuong.setSelected(true);
                loadDataToTable();
            }
        });

        pnlButtons.add(btnReset);
        pnlButtons.add(Box.createVerticalStrut(8));
        pnlButtons.add(btnSearch);
        return pnlButtons;
    }

    private JRadioButton buildRadio(String text) {
        JRadioButton rdo = new JRadioButton(text);
        rdo.setOpaque(false);
        rdo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        rdo.setForeground(GuiTheme.TEXT);
        return rdo;
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
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
        pnlOuter.setOpaque(false);
        pnlOuter.add(buildSectionTitle("Danh sách chuyến đi"), BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
            new Object[] { "STT", "Ga đi", "Ga đến", "Ngày đi", "Ngày đến", "Giờ đi - Giờ đến", "Tàu" },
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblData = new JTable(tblModel);
        tblData.setRowHeight(28);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.setForeground(GuiTheme.TEXT);
        tblData.setGridColor(new Color(230, 233, 238));
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        tblData.getTableHeader().setBackground(Color.WHITE);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblData.getColumnModel().getColumn(0).setCellRenderer(center);
        
        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        
        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 7) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(50);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(150);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(100);
            }
        });
        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private void loadDataToTable() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT ct.maChuyen, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
                         "ct.thoiGianKhoiHanh, ct.thoiGianDuKien, t.tenTau " +
                         "FROM ChuyenTau ct " +
                         "JOIN Ga gDi ON ct.gaDi = gDi.maGa " +
                         "JOIN Ga gDen ON ct.gaDen = gDen.maGa " +
                         "JOIN Tau t ON ct.maTau = t.maTau " +
                         "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? AND t.tenTau LIKE ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + (cboGaDi.getSelectedItem() != null ? cboGaDi.getSelectedItem().toString() : "") + "%");
            stmt.setString(2, "%" + (cboGaDen.getSelectedItem() != null ? cboGaDen.getSelectedItem().toString() : "") + "%");
            stmt.setString(3, "%" + (cboTau.getSelectedItem() != null ? cboTau.getSelectedItem().toString() : "") + "%");

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

                tblModel.addRow(new Object[] {
                    stt++,
                    rs.getString("gaDi"),
                    rs.getString("gaDen"),
                    ngayDi,
                    ngayDen,
                    gioDiGioDen,
                    rs.getString("tenTau")
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
}
