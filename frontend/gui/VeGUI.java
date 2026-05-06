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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import connect_DB.Connect_DB;

final class VeGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JTextField txtMaVe, txtHoTen, txtCccd, txtGaDi, txtGaDen, txtViTri;
    private JComboBox<String> cboLoaiVe, cboTrangThai;
    private JDateChooser dcNgayMua;

    VeGUI() {
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

        loadDataToTable();
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

        txtMaVe = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Mã vé:", txtMaVe), gbc);
        
        cboLoaiVe = buildCombo("", "MOT_CHIEU", "KHU_HOI");
        gbc.gridx = 1; pnlGrid.add(buildField("Loại vé:", cboLoaiVe), gbc);
        
        dcNgayMua = buildDateField();
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày mua:", dcNgayMua), gbc);
        
        cboTrangThai = buildCombo("", "CHO_THANH_TOAN", "DA_THANH_TOAN", "DA_HUY");
        gbc.gridx = 3; pnlGrid.add(buildField("Trạng thái vé:", cboTrangThai), gbc);

        gbc.gridy = 1;
        txtHoTen = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Họ tên khách:", txtHoTen), gbc);
        
        txtCccd = buildTextField();
        gbc.gridx = 1; pnlGrid.add(buildField("CCCD / SĐT:", txtCccd), gbc);
        
        txtGaDi = buildTextField();
        gbc.gridx = 2; pnlGrid.add(buildField("Ga đi:", txtGaDi), gbc);
        
        txtGaDen = buildTextField();
        gbc.gridx = 3; pnlGrid.add(buildField("Ga đến:", txtGaDen), gbc);

        gbc.gridy = 2;
        txtViTri = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Vị trí ghế:", txtViTri), gbc);
        
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

    private JTextField buildTextField() {
        JTextField txtField = new JTextField();
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setForeground(PRIMARY);
        txtField.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        txtField.setPreferredSize(new Dimension(160, 28));
        return txtField;
    }

    private JComboBox<String> buildCombo(String... values) {
        JComboBox<String> cmb = new JComboBox<>(values);
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
                txtMaVe.setText("");
                txtHoTen.setText("");
                txtCccd.setText("");
                txtGaDi.setText("");
                txtGaDen.setText("");
                txtViTri.setText("");
                cboLoaiVe.setSelectedIndex(0);
                cboTrangThai.setSelectedIndex(0);
                dcNgayMua.setDate(null);
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
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
        pnlOuter.setOpaque(false);
        pnlOuter.add(buildSectionTitle("Danh sách vé tàu"), BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
            new Object[] { "STT", "Mã vé", "Tên khách", "Loại vé", "Khởi hành", "Ga đi - Ga đến", "Vị trí", "Ngày mua", "Trạng thái" },
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

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);

        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 9) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(40);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(140);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(140);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(140);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(7).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(8).setPreferredWidth(90);
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
            String sql = "SELECT v.maVe, kh.hoTenKH, v.loaiVe, ct.thoiGianKhoiHanh, " +
                         "gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, v.maGhe, v.ngayMua, v.trangThaiVe " +
                         "FROM Ve v " +
                         "JOIN KhachHang kh ON v.maKH = kh.maKH " +
                         "JOIN ChuyenTau ct ON v.maChuyen = ct.maChuyen " +
                         "JOIN Ga gDi ON ct.gaDi = gDi.maGa " +
                         "JOIN Ga gDen ON ct.gaDen = gDen.maGa " +
                         "WHERE v.maVe LIKE ? AND kh.hoTenKH LIKE ? AND v.maGhe LIKE ?";
            
            String loaiVe = (String) cboLoaiVe.getSelectedItem();
            if (loaiVe != null && !loaiVe.isEmpty()) sql += " AND v.loaiVe = ?";
            
            String trangThai = (String) cboTrangThai.getSelectedItem();
            if (trangThai != null && !trangThai.isEmpty()) sql += " AND v.trangThaiVe = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaVe.getText().trim() + "%");
            stmt.setString(2, "%" + txtHoTen.getText().trim() + "%");
            stmt.setString(3, "%" + txtViTri.getText().trim() + "%");
            
            int idx = 4;
            if (loaiVe != null && !loaiVe.isEmpty()) stmt.setString(idx++, loaiVe);
            if (trangThai != null && !trangThai.isEmpty()) stmt.setString(idx++, trangThai);

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                String khoiHanh = rs.getTimestamp("thoiGianKhoiHanh") != null ? sdf.format(rs.getTimestamp("thoiGianKhoiHanh")) : "";
                String ngayMua = rs.getTimestamp("ngayMua") != null ? sdf.format(rs.getTimestamp("ngayMua")) : "";
                String tuyenDuong = rs.getString("gaDi") + " - " + rs.getString("gaDen");
                
                tblModel.addRow(new Object[] {
                    stt++,
                    rs.getString("maVe"),
                    rs.getString("hoTenKH"),
                    rs.getString("loaiVe"),
                    khoiHanh,
                    tuyenDuong,
                    rs.getString("maGhe"),
                    ngayMua,
                    rs.getString("trangThaiVe")
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
