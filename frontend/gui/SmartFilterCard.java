package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class SmartFilterCard extends JPanel {
    private JLabel lblCount;

    public SmartFilterCard(String title, String count, Color accentColor) {
        setLayout(new BorderLayout(15, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(12, 15, 12, 20)
            )
        ));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(GuiTheme.TEXT);

        lblCount = new JLabel(count);
        lblCount.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
        lblCount.setForeground(accentColor);

        add(lblTitle, BorderLayout.CENTER);
        add(lblCount, BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setBackground(new Color(248, 249, 250));
            }
            @Override public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
            }
        });
    }

    public void setCount(String count) {
        lblCount.setText(count);
    }
}
