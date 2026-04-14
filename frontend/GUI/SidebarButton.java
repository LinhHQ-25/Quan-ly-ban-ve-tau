package gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

final class SidebarButton extends JButton {
    private final Color normalBg;
    private final Color hoverBg;
    private final Color activeBg;
    private boolean active;

    SidebarButton(String text, boolean isSubmenu) {
        this(text, isSubmenu, null);
    }

    SidebarButton(String text, boolean isSubmenu, String iconPath) {
        setAlignmentX(LEFT_ALIGNMENT);
        int h = isSubmenu ? 38 : 52;
        setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        setPreferredSize(new Dimension(GuiTheme.SIDEBAR_W, h));
        setMinimumSize(new Dimension(GuiTheme.SIDEBAR_W, h));

        setFont(GuiTheme.font("Segoe UI", java.awt.Font.PLAIN, isSubmenu ? 13 : 15));
        setForeground(isSubmenu ? new Color(40, 40, 40) : Color.WHITE);
        setHorizontalAlignment(SwingConstants.LEFT);
        setHorizontalTextPosition(SwingConstants.RIGHT);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setContentAreaFilled(true);

        int leftPad = isSubmenu ? GuiTheme.SUBMENU_LEFT_PAD : GuiTheme.LEFT_PAD;
        setBorder(new EmptyBorder(0, leftPad, 0, 8));

        normalBg = isSubmenu ? GuiTheme.SUBMENU_BG : GuiTheme.NAVY;
        hoverBg = isSubmenu ? GuiTheme.SUBMENU_HOVER : GuiTheme.NAVY_HOVER;
        activeBg = isSubmenu ? GuiTheme.SUBMENU_SELECTED : GuiTheme.ACCENT;
        setBackground(normalBg);
        setText(text);

        if (iconPath != null) {
            Icon icon = GuiIcons.loadIcon(SidebarButton.class, iconPath, isSubmenu ? 14 : 18, isSubmenu ? 14 : 18);
            setIcon(icon);
            setIconTextGap(isSubmenu ? 6 : 8);
        }

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!active) setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!active) setBackground(normalBg);
            }
        });
    }

    void setActive(boolean active) {
        this.active = active;
        setBackground(active ? activeBg : normalBg);
    }

    void setExpanded(boolean expanded) {
        // kept for compatibility with the previous implementation
    }
}
