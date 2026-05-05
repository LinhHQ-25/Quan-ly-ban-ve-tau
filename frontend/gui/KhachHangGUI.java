package gui;

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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

final class KhachHangGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color FIELD_BG = new Color(141, 184, 219);
    private static final Color PRIMARY = new Color(71, 71, 156);

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

        gbc.gridx = 0; pnlGrid.add(buildField("Mã khách hàng:", buildCombo("KH001", "KH002", "KH003")), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("Họ và tên:", buildCombo("Nguyễn Văn A", "Trần Thị B", "Lê Văn C")), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày sinh:", buildDateField()), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Số điện thoại:", buildCombo("0909000001", "0909000002", "0909000003")), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0; pnlGrid.add(buildField("CCCD:", buildCombo("079099000111", "079099000222")), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("Email:", buildCombo("nguyenvana@gmail.com", "tranthib@gmail.com")), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày đăng ký:", buildDateField()), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Điểm tích lũy:", buildSeatField()), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0; pnlGrid.add(buildField("Loại khách hàng:", buildCombo("", "Dưới 6 tuổi", "Từ 6 đến dưới 10 tuổi", "Từ 60 tuổi trở lên", "Sinh viên", "Người lớn")), gbc);
        gbc.gridx = 1; pnlGrid.add(buildUtilBlock(), gbc);
        
        gbc.gridx = 2; 
        gbc.gridwidth = 2; 
        gbc.anchor = GridBagConstraints.SOUTH;
        pnlGrid.add(buildActionBlock(), gbc);

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        return pnlOuter;
    }

    private JPanel buildField(String label, java.awt.Component comp) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 4));
        pnlField.setOpaque(false);
        JLabel lbField = new JLabel(label);
        lbField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbField.setForeground(GuiTheme.TEXT);
        pnlField.add(lbField, BorderLayout.NORTH);
        pnlField.add(comp, BorderLayout.CENTER);
        return pnlField;
    }

    private JTextField buildCombo(String... values) {
        JTextField txtField = new JTextField(values.length > 0 ? values[0] : "");
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setForeground(GuiTheme.SEARCH_FIELD_TEXT);
        txtField.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        txtField.setPreferredSize(new Dimension(160, 28));
        return txtField;
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

    private JPanel buildSeatField() {
        JSpinner spnSeat = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        spnSeat.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        spnSeat.setPreferredSize(new Dimension(95, 28));
        spnSeat.setBorder(new LineBorder(new Color(188, 197, 208), 1, true));
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);
        pnlWrap.add(spnSeat, BorderLayout.CENTER);
        return pnlWrap;
    }

    private JPanel buildTypeBlock() {
        JPanel pnlBlock = new JPanel(new BorderLayout(0, 4));
        pnlBlock.setOpaque(false);
        JLabel lbBlock = new JLabel("Nhóm khách");
        lbBlock.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbBlock.setForeground(GuiTheme.TEXT);
        JPanel pnlOptions = new JPanel();
        pnlOptions.setOpaque(false);
        pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
        ButtonGroup grp = new ButtonGroup();
        JRadioButton rdoA = buildRadio("Cá nhân");
        JRadioButton rdoB = buildRadio("Doanh nghiệp");
        grp.add(rdoA);
        grp.add(rdoB);
        rdoA.setSelected(true);
        pnlOptions.add(rdoA);
        pnlOptions.add(rdoB);
        pnlBlock.add(lbBlock, BorderLayout.NORTH);
        pnlBlock.add(pnlOptions, BorderLayout.CENTER);
        return pnlBlock;
    }

    private JPanel buildUtilBlock() {
        JPanel pnlBlock = new JPanel(new BorderLayout(0, 4));
        pnlBlock.setOpaque(false);
        JLabel lbBlock = new JLabel("Trạng thái");
        lbBlock.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbBlock.setForeground(GuiTheme.TEXT);
        JPanel pnlOptions = new JPanel();
        pnlOptions.setOpaque(false);
        pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
        pnlOptions.add(buildCheck("Đang hoạt động", true));
        pnlOptions.add(buildCheck("Tạm khóa", true));
        pnlOptions.add(buildCheck("Khách VIP", false));
        pnlBlock.add(lbBlock, BorderLayout.NORTH);
        pnlBlock.add(pnlOptions, BorderLayout.CENTER);
        return pnlBlock;
    }

    private JPanel buildActionBlock() {
        JPanel pnlBlock = new JPanel(new BorderLayout());
        pnlBlock.setOpaque(false);
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        pnlButtons.setOpaque(false);
        JButton btnReset = new JButton("Xóa bộ lọc");
        styleButton(btnReset, new Color(244, 246, 250), new Color(72, 72, 190), new Color(145, 145, 145));
        JButton btnSearch = new JButton("Tra cứu");
        styleButton(btnSearch, PRIMARY, Color.WHITE, PRIMARY);
        pnlButtons.add(btnReset);
        pnlButtons.add(btnSearch);
        pnlBlock.add(pnlButtons, BorderLayout.CENTER);
        return pnlBlock;
    }

    private static JRadioButton buildRadio(String text) {
        JRadioButton rdo = new JRadioButton(text);
        rdo.setOpaque(false);
        rdo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        rdo.setForeground(GuiTheme.TEXT);
        rdo.setFocusPainted(false);
        return rdo;
    }

    private static JCheckBox buildCheck(String text, boolean selected) {
        JCheckBox chk = new JCheckBox(text, selected);
        chk.setOpaque(false);
        chk.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        chk.setForeground(GuiTheme.TEXT);
        chk.setFocusPainted(false);
        return chk;
    }

    private static void styleButton(JButton btn, Color bg, Color fg, Color border) {
        btn.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new LineBorder(border, 1, true));
        btn.setPreferredSize(new Dimension(96, 34));
    }

    private JPanel buildTablePanel() {
        DefaultTableModel tblModel = new DefaultTableModel(
            new Object[] { "STT", "Mã khách", "Họ và tên", "Ngày sinh", "CCCD", "Số ĐT", "Email", "Loại khách", "Trạng thái" },
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
        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 9) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(40);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(80);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(130);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(160);
                tblData.getColumnModel().getColumn(7).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(8).setPreferredWidth(100);
            }
        });
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);
        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }
}
