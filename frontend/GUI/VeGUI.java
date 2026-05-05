package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.awt.Component;
import com.toedter.calendar.JDateChooser;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

final class VeGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color FIELD_BG = new Color(141, 184, 219);
    private static final Color PRIMARY = new Color(71, 71, 156);

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
    }



    private JPanel buildFilterPanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout());
        pnlOuter.setOpaque(false);

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 10, 14);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        gbc.gridx = 0; pnlGrid.add(buildField("Mã vé:", buildTextField()), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("Loại vé:", buildCombo("", "MOT_CHIEU", "KHU_HOI")), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày mua:", buildDateField()), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Trạng thái vé:", buildCombo("", "CHO_THANH_TOAN", "DA_THANH_TOAN", "DA_HUY")), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0; pnlGrid.add(buildField("Họ tên khách:", buildTextField()), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("CCCD / SĐT:", buildTextField()), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Ga đi:", buildTextField()), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Ga đến:", buildTextField()), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0; pnlGrid.add(buildField("Mã ghế:", buildTextField()), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlGrid.add(buildActionBlock(), gbc);

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        return pnlOuter;
    }

    private JPanel buildField(String label, java.awt.Component comp) {
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

    private javax.swing.JComboBox<String> buildCombo(String... values) {
        javax.swing.JComboBox<String> cmb = new javax.swing.JComboBox<>(values);
        cmb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cmb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cmb.setForeground(PRIMARY);
        cmb.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        cmb.setPreferredSize(new Dimension(160, 28));
        cmb.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setForeground(PRIMARY);
                return c;
            }
        });
        return cmb;
    }

    private JPanel buildDateField() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setDate(new java.util.Date());
        dc.setPreferredSize(new Dimension(160, 28));
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(GuiTheme.SEARCH_FIELD_BG);
        dc.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        
        Component editor = dc.getDateEditor().getUiComponent();
        editor.setBackground(GuiTheme.SEARCH_FIELD_BG);
        if (editor instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) editor).setBorder(null);
        }
        
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);
        pnlWrap.add(dc, BorderLayout.CENTER);
        return pnlWrap;
    }

    private JPanel buildActionBlock() {
        JPanel pnlBlock = new JPanel(new BorderLayout());
        pnlBlock.setOpaque(false);
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        pnlButtons.setOpaque(false);

        JButton btnReset = buildNavyButton("Xóa bộ lọc");
        JButton btnSearch = buildNavyButton("Tra cứu");

        pnlButtons.add(btnReset);
        pnlButtons.add(btnSearch);
        pnlBlock.add(pnlButtons, BorderLayout.CENTER);
        return pnlBlock;
    }

    private JButton buildNavyButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
                    : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,(getWidth()-fm.stringWidth(text))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
        pnlOuter.setOpaque(false);
        pnlOuter.add(buildSectionTitle("Danh sách vé tàu"), BorderLayout.NORTH);

        DefaultTableModel tblModel = new DefaultTableModel(
            new Object[] { "STT", "Mã vé", "Tên khách", "Loại vé", "Khởi hành", "Ga đi - Ga đến", "Vị trí", "Ngày mua", "Trạng thái" },
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable tblData = new JTable(tblModel);
        tblData.setRowHeight(28);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.setForeground(GuiTheme.TEXT);
        tblData.setGridColor(new Color(230, 233, 238));
        tblData.setSelectionBackground(new Color(207, 209, 214));
        tblData.setSelectionForeground(GuiTheme.TEXT);
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.getTableHeader().setBackground(Color.WHITE);
        tblData.getTableHeader().setForeground(GuiTheme.TEXT);
        tblData.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblData.getColumnModel().getColumn(0).setCellRenderer(center);
        tblData.getColumnModel().getColumn(1).setCellRenderer(center);
        tblData.getColumnModel().getColumn(4).setCellRenderer(center);
        tblData.getColumnModel().getColumn(5).setCellRenderer(center);
        tblData.getColumnModel().getColumn(6).setCellRenderer(center);

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
