package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

final class ProfileIcon extends JPanel {
    ProfileIcon() {
        setOpaque(false);
        setPreferredSize(new Dimension(48, 48));
        setMinimumSize(new Dimension(48, 48));
        setMaximumSize(new Dimension(48, 48));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(122, 149, 182));
        g2.fillOval(16, 5, 16, 16);
        g2.fillRoundRect(10, 22, 28, 18, 14, 14);
        g2.dispose();
    }
}
