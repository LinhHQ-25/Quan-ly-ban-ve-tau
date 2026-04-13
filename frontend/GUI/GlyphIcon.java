package GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

final class GlyphIcon implements Icon {
    private final int width;
    private final int height;
    private final String glyph;

    GlyphIcon(int width, int height, String glyph) {
        this.width = width;
        this.height = height;
        this.glyph = glyph;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(x, y, width - 1, height - 1, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, Math.max(10, height - 4)));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int tx = x + (width - fm.stringWidth(glyph)) / 2;
        int ty = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(glyph, tx, ty);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
