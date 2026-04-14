package GUI;

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

        JLabel lbWelcome = new JLabel("Xin chào, Nhân viên!");
        lbWelcome.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 20));
        lbWelcome.setForeground(GuiTheme.TEXT);

        JPanel pnlTopInfo = new JPanel(new BorderLayout());
        pnlTopInfo.setOpaque(false);
        pnlTopInfo.add(lbWelcome, BorderLayout.WEST);

        JPanel pnlInfoBox = new JPanel(new BorderLayout());
        pnlInfoBox.setBackground(GuiTheme.PANEL_BG);
        pnlInfoBox.setBorder(new LineBorder(new Color(210, 215, 224), 1, true));

        JPanel pnlBody = new JPanel();
        pnlBody.setOpaque(false);
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.add(buildPersonalInfo());
        pnlBody.add(buildWorkInfo());

        pnlInfoBox.add(pnlBody, BorderLayout.CENTER);
        add(pnlTopInfo, BorderLayout.NORTH);
        add(pnlInfoBox, BorderLayout.CENTER);
    }

    private JPanel buildPersonalInfo() {
        JPanel pnlSection = new JPanel(new BorderLayout());
        pnlSection.setOpaque(false);
        pnlSection.add(sectionTitle("THÔNG TIN CÁ NHÂN"), BorderLayout.NORTH);

        JPanel pnlForm = new JPanel();
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new EmptyBorder(14, 0, 0, 0));
        pnlForm.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 6, 6, 6);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        addLabel(pnlForm, gbc, 0, 0, "Mã nhân viên:");
        addField(pnlForm, gbc, 1, 0, "NV001");
        addLabel(pnlForm, gbc, 2, 0, "Vai trò:");
        addField(pnlForm, gbc, 3, 0, "Nhân viên");

        addLabel(pnlForm, gbc, 0, 1, "Họ và tên:");
        addField(pnlForm, gbc, 1, 1, "Nguyễn Văn A");
        addLabel(pnlForm, gbc, 2, 1, "Ngày sinh:");
        addField(pnlForm, gbc, 3, 1, "01/01/2000");

        addLabel(pnlForm, gbc, 0, 2, "Giới tính:");
        addField(pnlForm, gbc, 1, 2, "Nam");
        addLabel(pnlForm, gbc, 2, 2, "Số điện thoại:");
        addField(pnlForm, gbc, 3, 2, "0909 999 999");

        addLabel(pnlForm, gbc, 0, 3, "Email:");
        addField(pnlForm, gbc, 1, 3, "nhanvien@tau.vn");
        addLabel(pnlForm, gbc, 2, 3, "");
        addField(pnlForm, gbc, 3, 3, "");

        addLabel(pnlForm, gbc, 0, 4, "Địa chỉ:");
        addField(pnlForm, gbc, 1, 4, "123 Đường Sắt, TP.HCM");

        pnlSection.add(pnlForm, BorderLayout.CENTER);
        return pnlSection;
    }

    private JPanel buildWorkInfo() {
        JPanel pnlSection = new JPanel(new BorderLayout());
        pnlSection.setOpaque(false);
        pnlSection.add(sectionTitle("THÔNG TIN CÔNG VIỆC"), BorderLayout.NORTH);

        JPanel pnlContent = new JPanel();
        pnlContent.setOpaque(false);
        pnlContent.setBorder(new EmptyBorder(16, 6, 0, 6));
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.add(infoRow("Ca làm việc:", "Ca sáng (06:00 - 14:00)"));
        pnlContent.add(Box.createVerticalStrut(8));
        pnlContent.add(infoRow("Giờ vào làm:", "09:03:51"));
        pnlContent.add(Box.createVerticalStrut(8));
        pnlContent.add(infoRow("Trạng thái:", "Đang làm việc"));
        pnlSection.add(pnlContent, BorderLayout.CENTER);
        return pnlSection;
    }

    private JPanel sectionTitle(String text) {
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(true);
        pnlTitle.setBackground(new Color(238, 242, 248));
        pnlTitle.setBorder(new EmptyBorder(10, 18, 10, 18));
        pnlTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JLabel lbTitle = new JLabel(text);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 18));
        lbTitle.setForeground(GuiTheme.TEXT);
        pnlTitle.add(lbTitle, BorderLayout.WEST);
        return pnlTitle;
    }

    private void addLabel(JPanel pnlForm, java.awt.GridBagConstraints gbc, int x, int y, String text) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        JLabel lb = new JLabel(text);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        lb.setForeground(GuiTheme.TEXT);
        pnlForm.add(lb, gbc);
    }

    private void addField(JPanel pnlForm, java.awt.GridBagConstraints gbc, int x, int y, String text) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = (x == 1 || x == 3) ? 1.0 : 0.0;
        JTextField txtField = new JTextField(text);
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBorder(new LineBorder(new Color(210, 215, 224), 1, true));
        txtField.setBackground(Color.WHITE);
        txtField.setPreferredSize(new Dimension(180, 30));
        pnlForm.add(txtField, gbc);
    }

    private JPanel infoRow(String labelText, String valueText) {
        JPanel pnlRow = new JPanel(new BorderLayout());
        pnlRow.setOpaque(false);
        JLabel lbLabel = new JLabel(labelText);
        lbLabel.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        lbLabel.setForeground(GuiTheme.TEXT);
        lbLabel.setPreferredSize(new Dimension(130, 24));
        JLabel lbValue = new JLabel(valueText);
        lbValue.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        lbValue.setForeground(GuiTheme.TEXT);
        pnlRow.add(lbLabel, BorderLayout.WEST);
        pnlRow.add(lbValue, BorderLayout.CENTER);
        return pnlRow;
    }
}