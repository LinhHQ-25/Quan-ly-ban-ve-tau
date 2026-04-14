package gui;

import java.awt.Image;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

final class GuiIcons {
    private GuiIcons() {}

    static Icon loadIcon(Class<?> anchor, String path, int width, int height) {
        URL url = anchor.getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return fallbackIcon(path, width, height);
    }

    private static Icon fallbackIcon(String path, int width, int height) {
        String key = path == null ? "" : path.toLowerCase();
        String glyph;
        if (key.contains("search")) glyph = "⌕";
        else if (key.contains("home")) glyph = "⌂";
        else if (key.contains("list")) glyph = "≣";
        else if (key.contains("train")) glyph = "T";
        else if (key.contains("ticket")) glyph = "✉";
        else if (key.contains("customer")) glyph = "☺";
        else if (key.contains("book")) glyph = "B";
        else if (key.contains("exchange")) glyph = "↔";
        else if (key.contains("statistics")) glyph = "▥";
        else if (key.contains("help")) glyph = "?";
        else if (key.contains("logout")) glyph = "↪";
        else glyph = "•";
        return new GlyphIcon(width, height, glyph);
    }
}
