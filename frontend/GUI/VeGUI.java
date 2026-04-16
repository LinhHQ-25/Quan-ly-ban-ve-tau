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

import de.wannawork.jcalendar.JCalendarComboBox;

final class VeGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color FIELD_BG = new Color(141, 184, 219);
    private static final Color PRIMARY = new Color(71, 71, 156);

    VeGUI() {
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

        pnlPage.add(buildHeader(
            "TRA CỨU VÉ",
            "Dùng để xem danh sách vé theo khách hàng, mã vé hoặc trạng thái vé."
        ));
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

        gbc.gridx = 0; pnlGrid.add(buildField("Mã vé:", buildTextField()), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("Họ tên:", buildTextField()), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Ngày sinh:", buildDateField()), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Số điện thoại:", buildTextField()), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0; pnlGrid.add(buildField("CCCD:", buildTextField()), gbc);
        gbc.gridx = 1; pnlGrid.add(buildField("Email:", buildTextField()), gbc);
        gbc.gridx = 2; pnlGrid.add(buildField("Loại khách hàng:", buildCombo("", "Thường", "Thành viên", "VIP")), gbc);
        gbc.gridx = 3; pnlGrid.add(buildField("Trạng thái vé:", buildCombo("", "Đã thanh toán", "Chờ thanh toán", "Đã hủy")), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
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
        lbField.setForeground(GuiTheme.TEXT);
        pnlField.add(lbField, BorderLayout.NORTH);
        pnlField.add(comp, BorderLayout.CENTER);
        return pnlField;
    }

    private JTextField buildTextField() {
        JTextField txtField = new JTextField();
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        txtField.setPreferredSize(new Dimension(160, 28));
        return txtField;
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
        JCalendarComboBox chooser = new JCalendarComboBox(Calendar.getInstance(), new Locale("vi", "VN"), new SimpleDateFormat("dd/MM/yyyy"));
        chooser.setPreferredSize(new Dimension(160, 28));
        chooser.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        chooser.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        chooser.setDateFormat(new SimpleDateFormat("dd/MM/yyyy"));
        chooser.setBackground(GuiTheme.SEARCH_FIELD_BG);
        styleCalendarChooser(chooser);
        return chooser;
    }

    private void styleCalendarChooser(java.awt.Container container) {
        container.setBackground(GuiTheme.SEARCH_FIELD_BG);
        if (container instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) container).setOpaque(true);
        }
        for (java.awt.Component child : container.getComponents()) {
            child.setBackground(GuiTheme.SEARCH_FIELD_BG);
            if (child instanceof java.awt.Container) {
                styleCalendarChooser((java.awt.Container) child);
            }
        }
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
        JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
        pnlOuter.setOpaque(false);
        pnlOuter.add(buildSectionTitle("Danh sách vé tàu"), BorderLayout.NORTH);

        DefaultTableModel tblModel = new DefaultTableModel(
            new Object[] { "STT", "Mã vé", "Tên khách hàng", "CCCD", "Thời gian khởi hành", "Vị trí toa - ghế", "Trạng thái" },
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
        spnScroll.setPreferredSize(new Dimension(10000, 300));

        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 7) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(50);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(170);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(130);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(170);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(140);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(110);
            }
        });

        pnlOuter.add(spnScroll, BorderLayout.CENTER);
        return pnlOuter;
    }

    private JPanel buildSectionTitle(String title) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        JLabel lb = new JLabel(title);
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
        lb.setForeground(GuiTheme.TEXT);
        pnl.add(lb, BorderLayout.WEST);
        return pnl;
    }
}
