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
import java.time.LocalDate;
import java.time.ZoneId;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import dao.KhachHangDAO;
import entity.KhachHang;

final class KhachHangGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color PRIMARY = new Color(71, 71, 156);

    private DefaultTableModel tblModel;
    private JTable tblData;
    
    private JTextField txtMaKH, txtHoTen, txtSdt, txtCCCD, txtEmail;
    private JDateChooser dcNamSinh;
    private JComboBox<String> cboDoiTuong;
    
    private KhachHangDAO khDAO = new KhachHangDAO();

    KhachHangGUI() {
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
        pnlPage.add(buildCRUDBlock(), BorderLayout.SOUTH);

        add(pnlPage, BorderLayout.CENTER);
        
        loadDataToTable();
    }

    private JPanel buildFilterPanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout(20, 0));
        pnlOuter.setOpaque(false);
        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);
        pnlGrid.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true),
            "Thông tin khách hàng",
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

        txtMaKH = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("Mã khách hàng:", txtMaKH), gbc);
        
        txtHoTen = buildTextField();
        gbc.gridx = 1; pnlGrid.add(buildField("Họ và tên:", txtHoTen), gbc);
        
        dcNamSinh = buildDateField();
        gbc.gridx = 2; pnlGrid.add(buildField("Năm sinh:", dcNamSinh), gbc);
        
        txtSdt = buildTextField();
        gbc.gridx = 3; pnlGrid.add(buildField("Số điện thoại:", txtSdt), gbc);

        gbc.gridy = 1;
        txtCCCD = buildTextField();
        gbc.gridx = 0; pnlGrid.add(buildField("CCCD:", txtCCCD), gbc);
        
        txtEmail = buildTextField();
        gbc.gridx = 1; pnlGrid.add(buildField("Email:", txtEmail), gbc);
        
        cboDoiTuong = buildCombo("", "DUOI_6_TUOI", "TU_6_TOI_DUOI_10", "TU_60_TRO_LEN", "SINH_VIEN", "NGUOI_LON");
        gbc.gridx = 2; pnlGrid.add(buildField("Đối tượng:", cboDoiTuong), gbc);
        
        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        
        JPanel pnlAction = buildSearchBlock();
        pnlAction.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true),
            "Tra cứu",
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
        JPanel pnlField = new JPanel(new BorderLayout(0, 6));
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

    private JPanel buildSearchBlock() {
        JPanel pnlButtons = new JPanel();
        pnlButtons.setOpaque(false);
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.Y_AXIS));
        pnlButtons.setBorder(new EmptyBorder(10, 15, 10, 15));

        JButton btnSearch = buildNavyButton("Tìm kiếm", GuiTheme.NAVY, GuiTheme.NAVY_HOVER);
        JButton btnReset = buildNavyButton("Xóa trắng", new Color(110, 125, 156), new Color(130, 145, 176));

        btnSearch.addActionListener(e -> loadDataToTable());

        btnReset.addActionListener(e -> {
            txtMaKH.setText("");
            txtHoTen.setText("");
            txtSdt.setText("");
            txtCCCD.setText("");
            txtEmail.setText("");
            cboDoiTuong.setSelectedIndex(0);
            dcNamSinh.setDate(null);
            loadDataToTable();
        });

        pnlButtons.add(btnReset);
        pnlButtons.add(Box.createVerticalStrut(8));
        pnlButtons.add(btnSearch);
        return pnlButtons;
    }

    private JPanel buildCRUDBlock() {
        JPanel pnlCRUD = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlCRUD.setOpaque(false);
        
        JButton btnAdd = buildNavyButton("Thêm mới", new Color(46, 125, 50), new Color(60, 145, 65));
        JButton btnUpdate = buildNavyButton("Cập nhật", new Color(25, 118, 210), new Color(33, 150, 243));
        JButton btnDelete = buildNavyButton("Xóa bỏ", new Color(198, 40, 40), new Color(229, 57, 53));

        btnAdd.addActionListener(e -> performAdd());
        btnUpdate.addActionListener(e -> performUpdate());
        btnDelete.addActionListener(e -> performDelete());

        pnlCRUD.add(btnAdd);
        pnlCRUD.add(btnUpdate);
        pnlCRUD.add(btnDelete);
        
        return pnlCRUD;
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
        pnlWrap.add(buildSectionTitle("Danh sách khách hàng"), BorderLayout.NORTH);

        tblModel = new DefaultTableModel(
            new Object[] { "STT", "Mã khách", "Họ và tên", "Năm sinh", "CCCD", "Số ĐT", "Email", "Đối tượng" },
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

        tblData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fillFieldsFromTable();
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblData.getColumnModel().getColumn(0).setCellRenderer(center);
        
        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        
        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 8) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(40);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(160);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(200);
                tblData.getColumnModel().getColumn(7).setPreferredWidth(120);
            }
        });
        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    private void fillFieldsFromTable() {
        int row = tblData.getSelectedRow();
        if (row != -1) {
            txtMaKH.setText(tblModel.getValueAt(row, 1).toString());
            txtHoTen.setText(tblModel.getValueAt(row, 2).toString());
            
            try {
                String namSinhStr = tblModel.getValueAt(row, 3).toString();
                if (!namSinhStr.isEmpty()) {
                    dcNamSinh.setDate(new SimpleDateFormat("dd/MM/yyyy").parse(namSinhStr));
                }
            } catch (Exception ex) {
                dcNamSinh.setDate(null);
            }
            
            txtCCCD.setText(tblModel.getValueAt(row, 4).toString());
            txtSdt.setText(tblModel.getValueAt(row, 5).toString());
            txtEmail.setText(tblModel.getValueAt(row, 6).toString());
            cboDoiTuong.setSelectedItem(tblModel.getValueAt(row, 7).toString());
        }
    }

    private void loadDataToTable() {
        if (tblModel == null) return;
        tblModel.setRowCount(0);
        Connection conn = Connect_DB.getInstance().getConnection();
        if (conn == null) return;

        try {
            String sql = "SELECT * FROM KhachHang WHERE maKH LIKE ? AND hoTenKH LIKE ? AND sdt LIKE ? AND cccd LIKE ? AND email LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + txtMaKH.getText().trim() + "%");
            stmt.setString(2, "%" + txtHoTen.getText().trim() + "%");
            stmt.setString(3, "%" + txtSdt.getText().trim() + "%");
            stmt.setString(4, "%" + txtCCCD.getText().trim() + "%");
            stmt.setString(5, "%" + txtEmail.getText().trim() + "%");

            ResultSet rs = stmt.executeQuery();
            int stt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                java.sql.Date namSinhDate = rs.getDate("namSinh");
                String namSinhStr = (namSinhDate != null) ? sdf.format(namSinhDate) : "";
                
                boolean laSV = rs.getBoolean("laSinhVien");
                LocalDate birth = (namSinhDate != null) ? namSinhDate.toLocalDate() : null;
                
                KhachHang temp = new KhachHang();
                temp.setNamSinh(birth);
                temp.setLaSinhVien(laSV);
                String doiTuong = temp.xacDinhLoaiKhachHang().toString();

                tblModel.addRow(new Object[] {
                    stt++,
                    rs.getString("maKH"),
                    rs.getString("hoTenKH"),
                    namSinhStr,
                    rs.getString("cccd"),
                    rs.getString("sdt"),
                    rs.getString("email"),
                    doiTuong
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performAdd() {
        KhachHang kh = getKhachHangFromFields();
        if (kh == null) return;
        if (khDAO.insert(kh)) {
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm khách hàng!");
        }
    }

    private void performUpdate() {
        KhachHang kh = getKhachHangFromFields();
        if (kh == null) return;
        if (khDAO.update(kh)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!");
        }
    }

    private void performDelete() {
        int row = tblData.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!");
            return;
        }
        String maKH = tblModel.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa khách hàng " + maKH + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (khDAO.delete(maKH)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa!");
            }
        }
    }

    private KhachHang getKhachHangFromFields() {
        String ma = txtMaKH.getText().trim();
        String ten = txtHoTen.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String sdt = txtSdt.getText().trim();
        String email = txtEmail.getText().trim();
        
        LocalDate birth = null;
        if (dcNamSinh.getDate() != null) {
            birth = dcNamSinh.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        
        String dt = (String) cboDoiTuong.getSelectedItem();
        boolean laSV = "SINH_VIEN".equals(dt);

        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã và Tên khách hàng!");
            return null;
        }
        
        return new KhachHang(ma, ten, cccd, sdt, email, birth, laSV);
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
