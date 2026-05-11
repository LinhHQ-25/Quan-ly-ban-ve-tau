package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class RoundedShadowPanel extends JPanel {
    private int cornerRadius = 16;
    private Color backgroundColor = Color.WHITE;
    private int shadowSize = 5;

    public RoundedShadowPanel() {
        setOpaque(false);
        setBorder(new EmptyBorder(4, shadowSize + 5, shadowSize + 7, shadowSize + 5));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int shadowOffsetY = 4;

        // Draw soft shadow (nhẹ hơn, opacity giảm)
        for (int i = 0; i < shadowSize; i++) {
            g2.setColor(new Color(0, 0, 0, 2 + (shadowSize - i))); 
            g2.fillRoundRect(i, i + shadowOffsetY, getWidth() - i * 2, getHeight() - i * 2 - shadowOffsetY, cornerRadius + shadowSize, cornerRadius + shadowSize);
        }

        // Draw main background (dịch lên trên để che khuất hoàn toàn bóng ở cạnh trên)
        g2.setColor(backgroundColor);
        g2.fillRoundRect(shadowSize, 2, getWidth() - shadowSize * 2, getHeight() - shadowSize - 4, cornerRadius, cornerRadius);

        g2.dispose();
    }
}
