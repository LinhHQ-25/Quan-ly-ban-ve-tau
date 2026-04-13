package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

class PlaceholderPanel extends JPanel {
    PlaceholderPanel(String title, String description) {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());
        setBorder(new LineBorder(new Color(210, 215, 224), 1, true));

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(28, 30, 30, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(GuiTheme.font("Segoe UI", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(GuiTheme.TEXT);

        JLabel descLabel = new JLabel("<html><div style='width:520px;'>" + description + "</div></html>");
        descLabel.setFont(GuiTheme.font("Segoe UI", java.awt.Font.PLAIN, 16));
        descLabel.setForeground(GuiTheme.SUB_TEXT);

        box.add(titleLabel);
        box.add(Box.createVerticalStrut(12));
        box.add(descLabel);
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
