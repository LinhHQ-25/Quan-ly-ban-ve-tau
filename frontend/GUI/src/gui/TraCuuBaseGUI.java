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

abstract class TraCuuBaseGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color FIELD_BG = new Color(141, 184, 219);
    private static final Color PRIMARY = new Color(71, 71, 156);

    protected TraCuuBaseGUI(String title, String subtitle) {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        page.add(header(title, subtitle));
        page.add(Box.createVerticalStrut(12));
        page.add(filterPanel());
        page.add(Box.createVerticalStrut(12));
        page.add(tablePanel());

        add(page, BorderLayout.NORTH);
    }

    private JPanel header(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(GuiTheme.font("Segoe UI", Font.BOLD, GuiTheme.PAGE_TITLE_SIZE));
        t.setForeground(GuiTheme.TEXT);
        JLabel s = new JLabel(subtitle);
        s.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, GuiTheme.PAGE_SUBTITLE_SIZE));
        s.setForeground(GuiTheme.SUB_TEXT);
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    private JPanel filterPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 10, 14);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        c.weightx = 1.0;

        c.gridx = 0; grid.add(field("Ga đi:", combo("Từ Sơn", "Hà Nội", "Đà Nẵng", "Phú Thọ")), c);
        c.gridx = 1; grid.add(field("Ga đến:", combo("Phú Thọ", "Hải Phòng", "Nha Trang", "TP.HCM")), c);
        c.gridx = 2; grid.add(field("Ngày đi:", dateField()), c);
        c.gridx = 3; grid.add(field("Mã /Tên tàu", combo("Train 011", "Train 012", "Train 013")), c);

        c.gridy = 1;
        c.gridx = 0; grid.add(field("Ghế trống", seatField()), c);
        c.gridx = 1; grid.add(typeBlock(), c);
        c.gridx = 2; grid.add(utilBlock(), c);
        c.gridx = 3; grid.add(actionBlock(), c);

        outer.add(grid, BorderLayout.CENTER);
        return outer;
    }

    private JPanel field(String label, java.awt.Component comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(GuiTheme.TEXT);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JComboBox<String> combo(String... values) {
        JComboBox<String> box = new JComboBox<>(values);
        box.setEditable(true);
        box.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(FIELD_BG);
        box.setPreferredSize(new Dimension(160, 28));
        box.setBorder(new LineBorder(new Color(188, 197, 208), 1, true));
        return box;
    }

    private JPanel dateField() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(145, 28));
        field.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(FIELD_BG);
        field.setBorder(new LineBorder(new Color(188, 197, 208), 1, true));
        wrap.add(field, BorderLayout.CENTER);
        JButton btn = new JButton("▣");
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setFocusable(false);
        btn.setBorder(new LineBorder(new Color(188, 197, 208), 1, true));
        btn.setBackground(new Color(230, 233, 238));
        wrap.add(btn, BorderLayout.EAST);
        return wrap;
    }

    private JPanel seatField() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        spinner.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        spinner.setPreferredSize(new Dimension(95, 28));
        spinner.setBorder(new LineBorder(new Color(188, 197, 208), 1, true));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(spinner, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel typeBlock() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel("Loại toa");
        l.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(GuiTheme.TEXT);
        JPanel opts = new JPanel();
        opts.setOpaque(false);
        opts.setLayout(new BoxLayout(opts, BoxLayout.Y_AXIS));
        ButtonGroup g = new ButtonGroup();
        JRadioButton a = radio("Toa thường");
        JRadioButton b = radio("Toa Vip");
        g.add(a); g.add(b); a.setSelected(true);
        opts.add(a); opts.add(b);
        p.add(l, BorderLayout.NORTH); p.add(opts, BorderLayout.CENTER);
        return p;
    }

    private JPanel utilBlock() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel("Tiện ích");
        l.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(GuiTheme.TEXT);
        JPanel opts = new JPanel();
        opts.setOpaque(false);
        opts.setLayout(new BoxLayout(opts, BoxLayout.Y_AXIS));
        opts.add(check("Giường nằm", true));
        opts.add(check("Điều hòa", true));
        opts.add(check("Ăn uống", false));
        p.add(l, BorderLayout.NORTH); p.add(opts, BorderLayout.CENTER);
        return p;
    }

    private JPanel actionBlock() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        buttons.setOpaque(false);
        JButton reset = new JButton("Xóa bộ lọc");
        style(reset, new Color(244, 246, 250), new Color(72, 72, 190), new Color(145, 145, 145));
        JButton search = new JButton("Tra cứu");
        style(search, PRIMARY, Color.WHITE, PRIMARY);
        buttons.add(reset);
        buttons.add(search);
        p.add(buttons, BorderLayout.CENTER);
        return p;
    }

    private static JRadioButton radio(String text) {
        JRadioButton r = new JRadioButton(text);
        r.setOpaque(false);
        r.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        r.setForeground(GuiTheme.TEXT);
        r.setFocusPainted(false);
        return r;
    }

    private static JCheckBox check(String text, boolean selected) {
        JCheckBox c = new JCheckBox(text, selected);
        c.setOpaque(false);
        c.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        c.setForeground(GuiTheme.TEXT);
        c.setFocusPainted(false);
        return c;
    }

    private static void style(JButton button, Color bg, Color fg, Color border) {
        button.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(new LineBorder(border, 1, true));
        button.setPreferredSize(new Dimension(96, 34));
    }

    private JPanel tablePanel() {
        DefaultTableModel model = new DefaultTableModel(new Object[] { "STT", "Ga đi", "Ga đến", "Ngày đi", "Ngày đến", "Giờ đi - Giờ đến", "Tàu" }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(GuiTheme.TEXT);
        table.setGridColor(new Color(230, 233, 238));
        table.setSelectionBackground(new Color(207, 209, 214));
        table.setSelectionForeground(GuiTheme.TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(GuiTheme.TEXT);
        table.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(10000, 300));
        SwingUtilities.invokeLater(() -> {
            if (table.getColumnModel().getColumnCount() >= 7) {
                table.getColumnModel().getColumn(0).setPreferredWidth(50);
                table.getColumnModel().getColumn(1).setPreferredWidth(120);
                table.getColumnModel().getColumn(2).setPreferredWidth(120);
                table.getColumnModel().getColumn(3).setPreferredWidth(100);
                table.getColumnModel().getColumn(4).setPreferredWidth(100);
                table.getColumnModel().getColumn(5).setPreferredWidth(150);
                table.getColumnModel().getColumn(6).setPreferredWidth(100);
            }
        });
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }
}