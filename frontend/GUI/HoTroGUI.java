package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

final class HoTroGUI extends JPanel {
    HoTroGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        JLabel title = new JLabel("TRANG HỖ TRỢ");
        title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, GuiTheme.PAGE_TITLE_SIZE));
        title.setForeground(GuiTheme.TEXT);

        JLabel desc = new JLabel("Dùng để hiển thị hướng dẫn và liên hệ hỗ trợ.");
        desc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, GuiTheme.PAGE_SUBTITLE_SIZE));
        desc.setForeground(GuiTheme.SUB_TEXT);

        box.add(title);
        box.add(Box.createVerticalStrut(12));
        box.add(desc);
        box.add(Box.createVerticalStrut(20));

        JPanel hint = new JPanel(new BorderLayout());
        hint.setBackground(Color.WHITE);
        hint.setBorder(new LineBorder(new Color(225, 229, 236), 1, true));
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        hint.add(new JLabel("   Nội dung sẽ được phát triển sau"), BorderLayout.WEST);

        box.add(hint);
        add(box, BorderLayout.NORTH);
    }
}