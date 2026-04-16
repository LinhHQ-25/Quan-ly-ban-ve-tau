package GUI;

import java.awt.Color;
import java.awt.Font;

final class GuiTheme {
    static final Color NAVY = new Color(37, 69, 121);
    static final Color NAVY_DARK = new Color(28, 52, 92);
    static final Color NAVY_HOVER = new Color(46, 85, 147);
    static final Color LIGHT_BG = new Color(245, 247, 251);
    static final Color PANEL_BG = Color.WHITE;
    static final Color TEXT = new Color(58, 58, 58);
    static final Color SUB_TEXT = new Color(110, 110, 110);
    static final Color ACCENT = new Color(96, 145, 214);
    static final Color SEARCH_FIELD_BG = new Color(220, 235, 248);
    static final Color SEARCH_FIELD_BORDER = new Color(186, 209, 231);
    static final Color SEARCH_FIELD_TEXT = new Color(50, 50, 50);
    static final Color SUBMENU_BG = SEARCH_FIELD_BG;
    static final Color SUBMENU_HOVER = new Color(210, 228, 245);
    static final Color SUBMENU_SELECTED = new Color(198, 221, 243);

    static final int SIDEBAR_W = 200;
    static final int LEFT_PAD = 14;
    static final int SUBMENU_LEFT_PAD = 28;
    static final int PAGE_PAD_TOP = 28;
    static final int PAGE_PAD_LEFT = 30;
    static final int PAGE_PAD_BOTTOM = 30;
    static final int PAGE_TITLE_SIZE = 24;
    static final int PAGE_SUBTITLE_SIZE = 16;
    static final int PAGE_CARD_BORDER_RADIUS = 1;

    private GuiTheme() {}

    static Font font(String family, int style, int size) {
        return new Font(family, style, size);
    }
}
