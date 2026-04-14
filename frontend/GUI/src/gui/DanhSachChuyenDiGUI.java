package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

final class DanhSachChuyenDiGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color FIELD_BG = new Color(141, 184, 219);
    private static final Color PRIMARY = new Color(71, 71, 156);

    DanhSachChuyenDiGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BoxLayout(pnlPage, BoxLayout.Y_AXIS));
        pnlPage.setBorder(new EmptyBorder(
            GuiTheme.PAGE_PAD_TOP,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));

        pnlPage.add(buildHeader("DANH SÁCH CHUYẾN ĐI", "Dùng để tìm danh sách chuyến, điểm đi, điểm đến và giờ chạy."));
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(buildFilterPanel());
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(buildTablePanel());

        add(pnlPage, BorderLayout.NORTH);
    }

    private JPanel buildHeader(String title, String subtitle) {
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 6));
        pnlHeader.setOpaque(false);
        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, GuiTheme.PAGE_TITLE_SIZE));
        lbTitle.setForeground(GuiTheme.TEXT);
        JLabel lbSubtitle = new JLabel(subtitle);
        lbSubtitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, GuiTheme.PAGE_SUBTITLE_SIZE));
        lbSubtitle.setForeground(GuiTheme.SUB_TEXT);
        pnlHeader.add(lbTitle, BorderLayout.NORTH);
        pnlHeader.add(lbSubtitle, BorderLayout.SOUTH);
        return pnlHeader;
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

                gbc.gridx = 0; pnlGrid.add(buildField("Ga đi:", buildCombo("Từ Sơn", "Hà Nội", "Đà Nẵng", "Phú Thọ")), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("Ga đến:", buildCombo("Phú Thọ", "Hải Phòng", "Nha Trang", "TP.HCM")), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày đi:", buildDateField()), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Mã /Tên tàu", buildCombo("Train 011", "Train 012", "Train 013")), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0; pnlGrid.add(buildField("Ghế trống", buildSeatField()), gbc);
        gbc.gridx = 1; pnlGrid.add(buildTypeBlock(), gbc);
        gbc.gridx = 2; pnlGrid.add(buildUtilBlock(), gbc);
        gbc.gridx = 3; pnlGrid.add(buildActionBlock(), gbc);

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

    private JComboBox<String> buildCombo(String... values) {
        JComboBox<String> cboField = new JComboBox<>(values);
        cboField.setEditable(true);
        cboField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cboField.setBackground(FIELD_BG);
        cboField.setPreferredSize(new Dimension(160, 28));
        cboField.setBorder(new LineBorder(new Color(188, 197, 208), 1, true));
        return cboField;
    }

    private JPanel buildDateField() {
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);
        JTextField txtDate = new JTextField();
        txtDate.setPreferredSize(new Dimension(145, 28));
        txtDate.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtDate.setBackground(FIELD_BG);
        txtDate.setBorder(new LineBorder(new Color(188, 197, 208), 1, true));
        pnlWrap.add(txtDate, BorderLayout.CENTER);
        JButton btnDate = new JButton("▣");
        btnDate.setPreferredSize(new Dimension(28, 28));
        btnDate.setFocusable(false);
        btnDate.setBorder(new LineBorder(new Color(188, 197, 208), 1, true));
        btnDate.setBackground(new Color(230, 233, 238));
        pnlWrap.add(btnDate, BorderLayout.EAST);
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
        JLabel lbBlock = new JLabel("Loại toa");
        lbBlock.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbBlock.setForeground(GuiTheme.TEXT);
        JPanel pnlOptions = new JPanel();
        pnlOptions.setOpaque(false);
        pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
        ButtonGroup grp = new ButtonGroup();
        JRadioButton rdoA = buildRadio("Toa thường");
        JRadioButton rdoB = buildRadio("Toa Vip");
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
        JLabel lbBlock = new JLabel("Tiện ích");
        lbBlock.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbBlock.setForeground(GuiTheme.TEXT);
        JPanel pnlOptions = new JPanel();
        pnlOptions.setOpaque(false);
        pnlOptions.setLayout(new BoxLayout(pnlOptions, BoxLayout.Y_AXIS));
        pnlOptions.add(buildCheck("Giường nằm", true));
        pnlOptions.add(buildCheck("Điều hòa", true));
        pnlOptions.add(buildCheck("Ăn uống", false));
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
            new Object[] { "STT", "Ga đi", "Ga đến", "Ngày đi", "Ngày đến", "Giờ đi - Giờ đến", "Tàu" },
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
        spnScroll.setPreferredSize(new Dimension(10000, 300));
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
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);
        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }
}