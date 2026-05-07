package gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Đã thêm 'public' để các lớp khác trong dự án có thể truy cập.
 */
public final class GuiTheme {
    // Màu sắc chủ đạo
    public static final Color NAVY = new Color(37, 69, 121);
    public static final Color NAVY_DARK = new Color(28, 52, 92);
    public static final Color NAVY_HOVER = new Color(46, 85, 147);
    public static final Color LIGHT_BG = new Color(245, 247, 251);
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color TEXT = new Color(58, 58, 58);
    public static final Color SUB_TEXT = new Color(110, 110, 110);
    public static final Color ACCENT = new Color(96, 145, 214);

    // Màu cho thanh tìm kiếm
    public static final Color SEARCH_FIELD_BG = new Color(220, 235, 248);
    public static final Color SEARCH_FIELD_BORDER = new Color(186, 209, 231);
    public static final Color SEARCH_FIELD_TEXT = new Color(50, 50, 50);

    // Màu cho Menu con
    public static final Color SUBMENU_BG = SEARCH_FIELD_BG;
    public static final Color SUBMENU_HOVER = new Color(210, 228, 245);
    public static final Color SUBMENU_SELECTED = new Color(198, 221, 243);

    // Kích thước cấu hình giao diện (Padding/Margin)
    public static final int SIDEBAR_W = 200;
    public static final int LEFT_PAD = 14;
    public static final int SUBMENU_LEFT_PAD = 28;
    public static final int PAGE_PAD_TOP = 28;
    public static final int PAGE_PAD_LEFT = 30;
    public static final int PAGE_PAD_BOTTOM = 30;
    public static final int PAGE_TITLE_SIZE = 24;
    public static final int PAGE_SUBTITLE_SIZE = 16;
    public static final int PAGE_CARD_BORDER_RADIUS = 1;

    private GuiTheme() {}

    // Hàm tiện ích tạo Font nhanh
    public static Font font(String family, int style, int size) {
        return new Font(family, style, size);
    }
}