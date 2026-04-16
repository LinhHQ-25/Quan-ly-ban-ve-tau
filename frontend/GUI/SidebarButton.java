package GUI;

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
        setForeground(isSubmenu ? GuiTheme.SEARCH_FIELD_TEXT : Color.WHITE);
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
            java.net.URL imgURL = SidebarButton.class.getResource(iconPath);
            if (imgURL != null) {
                javax.swing.ImageIcon tempIcon = new javax.swing.ImageIcon(imgURL);
                double originalW = tempIcon.getIconWidth();
                double originalH = tempIcon.getIconHeight();
                double ratio = originalW / originalH;

                // 1. TĂNG SIZE CĂN BẢN
                int baseSize = isSubmenu ? 16 : 24; // Tăng nhẹ từ 22 lên 24
                
                int targetW, targetH;

                // 2. CÔNG THỨC THÔNG MINH: 
                // Nếu là ảnh nằm ngang (ratio > 1.2), cho phép nó dài hơn baseSize một chút
                if (ratio > 1.2) { 
                    targetW = (int) (baseSize * 1.3); // Cho phép dài ra 30% để nhìn cho rõ
                    targetH = (int) (targetW / ratio);
                    
                    // Nếu sau khi dài ra mà vẫn quá cao, thì hãm lại
                    if (targetH > baseSize) {
                        targetH = baseSize;
                        targetW = (int) (targetH * ratio);
                    }
                } else {
                    // Với icon vuông hoặc đứng (Kính lúp, Đổi trả, Thống kê)
                    targetH = baseSize;
                    targetW = (int) (targetH * ratio);
                }

                // 3. Load icon
                Icon finalIcon = GuiIcons.loadIcon(SidebarButton.class, iconPath, targetW, targetH);
                setIcon(finalIcon);

                // 4. CỐ ĐỊNH KHOẢNG CÁCH ĐỂ LABEL THẲNG HÀNG
                // Vì icon giờ có thể rộng tới ~31px (24 * 1.3), ta nên để dành 40px cho icon
                int reservedSpace = isSubmenu ? 30 : 40;
                setIconTextGap(reservedSpace - targetW);
            }
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
