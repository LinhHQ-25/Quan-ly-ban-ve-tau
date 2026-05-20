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
    public static final Color SEARCH_FIELD_BG = Color.WHITE;
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

    // Thêm viền bo tròn 12px dùng chung cho các trường nhập liệu (vẽ thụt lề 1px để tránh bị clip ở góc trên/trái)
    public static final javax.swing.border.Border roundedBorder = javax.swing.BorderFactory.createCompoundBorder(
        new javax.swing.border.Border() {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SEARCH_FIELD_BORDER);
                g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 12, 12);
                g2.dispose();
            }

            @Override
            public java.awt.Insets getBorderInsets(java.awt.Component c) {
                return new java.awt.Insets(1, 1, 1, 1);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        },
        javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6)
    );

    /**
     * Cấu hình bo tròn 12px cho các JComponent nhập liệu (JTextField, JComboBox, JDateChooser, JSpinner).
     */
    public static void setupRoundedComponent(javax.swing.JComponent comp) {
        if (comp instanceof javax.swing.JTextField) {
            comp.setBorder(roundedBorder);
        } else if (comp instanceof javax.swing.JComboBox) {
            javax.swing.JComboBox<?> combo = (javax.swing.JComboBox<?>) comp;
            
            // Sử dụng BasicComboBoxUI phẳng để loại bỏ hoàn toàn viền hình chữ nhật mặc định của Windows
            combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override
                protected javax.swing.JButton createArrowButton() {
                    javax.swing.JButton btn = new javax.swing.JButton() {
                        @Override
                        protected void paintComponent(java.awt.Graphics g) {
                            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(GuiTheme.TEXT);
                            // Vẽ mũi tên tam giác nhỏ tinh tế
                            int w = getWidth();
                            int h = getHeight();
                            int arrowSize = 5;
                            int cx = w / 2;
                            int cy = h / 2;
                            int[] xs = {cx - arrowSize, cx + arrowSize, cx};
                            int[] ys = {cy - arrowSize / 2, cy - arrowSize / 2, cy + arrowSize / 2};
                            g2.fillPolygon(xs, ys, 3);
                            g2.dispose();
                        }
                    };
                    btn.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                    btn.setContentAreaFilled(false);
                    btn.setFocusPainted(false);
                    btn.setOpaque(false);
                    return btn;
                }
                
                @Override
                public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c.getBackground());
                    // Vẽ nền thụt lề 1px khớp hoàn toàn với viền bo tròn
                    g2.fillRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 12, 12);
                    g2.dispose();
                    super.paint(g, c);
                }
                
                @Override
                public void paintCurrentValueBackground(java.awt.Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
                    // Không vẽ nền chữ nhật mặc định để tránh đè lên góc bo tròn
                }
            });
            
            combo.setBorder(roundedBorder);
            combo.setBackground(SEARCH_FIELD_BG);
            combo.setForeground(TEXT);
            combo.setOpaque(false);
            
            // Định dạng cho editor component nếu combobox cho phép gõ (editable)
            java.awt.Component editor = combo.getEditor().getEditorComponent();
            if (editor instanceof javax.swing.JComponent) {
                javax.swing.JComponent jEditor = (javax.swing.JComponent) editor;
                jEditor.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 0));
                jEditor.setOpaque(false);
                jEditor.setBackground(SEARCH_FIELD_BG);
                jEditor.setForeground(TEXT);
            }
        } else if (comp instanceof javax.swing.JSpinner) {
            comp.setBorder(roundedBorder);
            comp.setOpaque(false);
            javax.swing.JSpinner spinner = (javax.swing.JSpinner) comp;
            
            // Xóa viền của textfield bên trong Spinner
            if (spinner.getEditor() instanceof javax.swing.JSpinner.DefaultEditor) {
                javax.swing.JTextField tf = ((javax.swing.JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
                tf.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 0));
                tf.setOpaque(false);
                tf.setBackground(SEARCH_FIELD_BG);
            }
            
            // Làm phẳng các nút tăng giảm bên trong Spinner
            for (java.awt.Component child : spinner.getComponents()) {
                if (child instanceof javax.swing.JButton) {
                    javax.swing.JButton btn = (javax.swing.JButton) child;
                    btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 0, 2));
                    btn.setContentAreaFilled(false);
                    btn.setFocusPainted(false);
                }
            }
        } else if (comp.getClass().getName().equals("com.toedter.calendar.JDateChooser")) {
            comp.setBorder(roundedBorder);
            comp.setOpaque(false);
            try {
                // Sử dụng reflection để tránh lỗi compile nếu import có vấn đề
                Object editor = comp.getClass().getMethod("getDateEditor").invoke(comp);
                java.awt.Component uiComp = (java.awt.Component) editor.getClass().getMethod("getUiComponent").invoke(editor);
                if (uiComp instanceof javax.swing.JComponent) {
                    javax.swing.JComponent jUiComp = (javax.swing.JComponent) uiComp;
                    jUiComp.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 0));
                    jUiComp.setOpaque(false);
                    jUiComp.setBackground(SEARCH_FIELD_BG);
                }
                
                // Ẩn nền của nút lịch để không đè lên viền bo tròn
                for (java.awt.Component child : comp.getComponents()) {
                    if (child instanceof javax.swing.JButton) {
                        javax.swing.JButton btn = (javax.swing.JButton) child;
                        btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4));
                        btn.setContentAreaFilled(false);
                        btn.setFocusPainted(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private GuiTheme() {}

    // Hàm tiện ích tạo Font nhanh
    public static Font font(String family, int style, int size) {
        return new Font(family, style, size);
    }
}