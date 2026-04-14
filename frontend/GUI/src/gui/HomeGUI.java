package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

final class HomeGUI extends JPanel {
    HomeGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout(0, 14));

        JLabel welcome = new JLabel("Xin chào, Nhân viên!");
        welcome.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 20));
        welcome.setForeground(GuiTheme.TEXT);

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);
        topInfo.add(welcome, BorderLayout.WEST);

        JPanel infoBox = new JPanel(new BorderLayout());
        infoBox.setBackground(GuiTheme.PANEL_BG);
        infoBox.setBorder(new LineBorder(new Color(210, 215, 224), 1, true));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildPersonalInfo());
        body.add(buildWorkInfo());

        infoBox.add(body, BorderLayout.CENTER);
        add(topInfo, BorderLayout.NORTH);
        add(infoBox, BorderLayout.CENTER);
    }

    private JPanel buildPersonalInfo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(sectionTitle("THÔNG TIN CÁ NHÂN"), BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 0, 0, 0));
        form.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new java.awt.Insets(6, 6, 6, 6);
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;

        addLabel(form, g, 0, 0, "Mã nhân viên:");
        addField(form, g, 1, 0, "NV001");
        addLabel(form, g, 2, 0, "Vai trò:");
        addField(form, g, 3, 0, "Nhân viên");

        addLabel(form, g, 0, 1, "Họ và tên:");
        addField(form, g, 1, 1, "Nguyễn Văn A");
        addLabel(form, g, 2, 1, "Ngày sinh:");
        addField(form, g, 3, 1, "01/01/2000");

        addLabel(form, g, 0, 2, "Giới tính:");
        addField(form, g, 1, 2, "Nam");
        addLabel(form, g, 2, 2, "Số điện thoại:");
        addField(form, g, 3, 2, "0909 999 999");

        addLabel(form, g, 0, 3, "Email:");
        addField(form, g, 1, 3, "nhanvien@tau.vn");
        addLabel(form, g, 2, 3, "");
        addField(form, g, 3, 3, "");

        addLabel(form, g, 0, 4, "Địa chỉ:");
        addField(form, g, 1, 4, "123 Đường Sắt, TP.HCM");

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildWorkInfo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(sectionTitle("THÔNG TIN CÔNG VIỆC"), BorderLayout.NORTH);

        JPanel c = new JPanel();
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(16, 6, 0, 6));
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(infoRow("Ca làm việc:", "Ca sáng (06:00 - 14:00)"));
        c.add(Box.createVerticalStrut(8));
        c.add(infoRow("Giờ vào làm:", "09:03:51"));
        c.add(Box.createVerticalStrut(8));
        c.add(infoRow("Trạng thái:", "Đang làm việc"));
        panel.add(c, BorderLayout.CENTER);
        return panel;
    }

    private JPanel sectionTitle(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(new Color(238, 242, 248));
        p.setBorder(new EmptyBorder(10, 18, 10, 18));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JLabel l = new JLabel(text);
        l.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 18));
        l.setForeground(GuiTheme.TEXT);
        p.add(l, BorderLayout.WEST);
        return p;
    }

    private void addLabel(JPanel p, java.awt.GridBagConstraints g, int x, int y, String t) {
        g.gridx = x; g.gridy = y; g.weightx = 0;
        JLabel l = new JLabel(t);
        l.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        l.setForeground(GuiTheme.TEXT);
        p.add(l, g);
    }

    private void addField(JPanel p, java.awt.GridBagConstraints g, int x, int y, String t) {
        g.gridx = x; g.gridy = y; g.weightx = (x == 1 || x == 3) ? 1.0 : 0.0;
        JTextField f = new JTextField(t);
        f.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(new LineBorder(new Color(210, 215, 224), 1, true));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(180, 30));
        p.add(f, g);
    }

    private JPanel infoRow(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(GuiTheme.TEXT);
        label.setPreferredSize(new Dimension(130, 24));
        JLabel value = new JLabel(valueText);
        value.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        value.setForeground(GuiTheme.TEXT);
        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }
}
