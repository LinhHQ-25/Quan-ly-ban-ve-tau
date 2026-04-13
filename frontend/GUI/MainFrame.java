package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MainFrame extends JFrame {

    private static final Color NAVY             = new Color(37, 69, 121);
    private static final Color NAVY_DARK        = new Color(28, 52, 92);
    private static final Color NAVY_HOVER       = new Color(46, 85, 147);
    private static final Color LIGHT_BG         = new Color(245, 247, 251);
    private static final Color PANEL_BG         = Color.WHITE;
    private static final Color TEXT             = new Color(58, 58, 58);
    private static final Color SUB_TEXT         = new Color(110, 110, 110);
    private static final Color ACCENT           = new Color(96, 145, 214);
    private static final Color SUBMENU_BG       = new Color(226, 228, 232);
    private static final Color SUBMENU_HOVER    = new Color(216, 218, 223);
    private static final Color SUBMENU_SELECTED = new Color(200, 203, 210);

    // Sidebar rộng 260px để "Danh sách chuyến đi" không bị cắt
    private static final int SIDEBAR_W   = 260;
    // Padding trái duy nhất cho TẤT CẢ các nút (kể cả submenu)
    private static final int LEFT_PAD    = 14;
    // Submenu cần thụt vào nhẹ hơn để giống mẫu
    private static final int SUBMENU_LEFT_PAD = 28;

    private final CardLayout cardLayout       = new CardLayout();
    private final JPanel     contentCards     = new JPanel(cardLayout);
    private final Map<String, SidebarButton> routeButtons     = new LinkedHashMap<>();
    private final Map<String, SidebarButton> searchSubButtons = new LinkedHashMap<>();
    private final JLabel headerTitle = new JLabel("THÔNG TIN CÁ NHÂN");

    private JPanel        searchSubmenuPanel;
    private SidebarButton searchMainButton;
    private boolean       searchExpanded;
    private String        activeCard = "home";

    public MainFrame() {
        setTitle("Quản lý bán vé tàu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LIGHT_BG);
        setContentPane(root);
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
        registerCards();
        showCard("home");
    }

    // ═══════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setBackground(NAVY);
        sb.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sb.setMinimumSize(new Dimension(SIDEBAR_W, 0));
        sb.setMaximumSize(new Dimension(SIDEBAR_W, Integer.MAX_VALUE));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(new EmptyBorder(0, 0, 18, 0));

        // ── Logo ──
        JPanel logoArea = new JPanel();
        logoArea.setBackground(NAVY_DARK);
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.X_AXIS));
        logoArea.setAlignmentX(LEFT_ALIGNMENT);
        logoArea.setPreferredSize(new Dimension(SIDEBAR_W, 90));
        logoArea.setMinimumSize(new Dimension(SIDEBAR_W, 90));
        logoArea.setMaximumSize(new Dimension(SIDEBAR_W, 90));
        logoArea.setBorder(new EmptyBorder(0, LEFT_PAD, 0, 8));

        Icon logoIcon = loadIcon("/Image/train.jpg", 38, 38);
        JLabel logoLbl;
        if (logoIcon != null) {
            logoLbl = new JLabel(logoIcon);
        } else {
            logoLbl = new JLabel("T");
            logoLbl.setFont(font("Segoe UI", Font.BOLD, 30));
            logoLbl.setForeground(Color.WHITE);
        }
        logoArea.add(logoLbl);
        sb.add(logoArea);
        sb.add(Box.createVerticalStrut(10));

        // ── Nav ──
        sb.add(mkBtn("home",     "Trang chủ",  "/Image/train.jpg",       false));
        searchMainButton = mkBtn("tra-cuu", "Tra cứu", "/Image/iconTraCuu.png", true);
        searchMainButton.addActionListener(e -> toggleSearch());
        sb.add(searchMainButton);

        // ── Submenu ──
        searchSubmenuPanel = new JPanel();
        searchSubmenuPanel.setLayout(new BoxLayout(searchSubmenuPanel, BoxLayout.Y_AXIS));
        searchSubmenuPanel.setOpaque(true);
        searchSubmenuPanel.setBackground(SUBMENU_BG);
        searchSubmenuPanel.setVisible(false);
        searchSubmenuPanel.setAlignmentX(LEFT_ALIGNMENT);
        searchSubmenuPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        // 4 items × 38px
        int subH = 4 * 38;
        searchSubmenuPanel.setPreferredSize(new Dimension(SIDEBAR_W, subH));
        searchSubmenuPanel.setMinimumSize(new Dimension(SIDEBAR_W, subH));
        searchSubmenuPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, subH));

        mkSub("tra-cuu-chuyen", "Danh sách chuyến đi", "/images/list.png");
        mkSub("tra-cuu-tau",    "Tàu",                 "/images/train-small.png");
        mkSub("tra-cuu-ve",     "Vé",                  "/images/ticket.png");
        mkSub("tra-cuu-khach",  "Khách hàng",          "/images/customer.png");
        sb.add(searchSubmenuPanel);

        sb.add(mkBtn("dat-ve",   "Đặt vé tàu", "/images/book.png",       false));
        sb.add(mkBtn("doi-tra",  "Đổi/Trả vé", "/images/exchange.png",   false));
        sb.add(mkBtn("thong-ke", "Báo cáo ca làm",   "/images/statistics.png", false));
        sb.add(mkBtn("ho-tro",   "Hỗ trợ",     "/images/help.png",       false));

        sb.add(Box.createVerticalGlue());
        sb.add(Box.createVerticalStrut(16));

        // ── Đăng xuất ──
        SidebarButton logout = new SidebarButton("Đăng xuất", false);
        logout.addActionListener(e -> showCard("home"));
        sb.add(logout);

        return sb;
    }

    /** Tạo nút chính (route) */
    private SidebarButton mkBtn(String route, String label, String iconPath, boolean isSearchMain) {
        SidebarButton btn = new SidebarButton(label, false);
        Icon icon = loadIcon(iconPath, 18, 18);
        if (icon != null) { btn.setIcon(icon); btn.setIconTextGap(8); }
        if (!isSearchMain) btn.addActionListener(e -> showCard(route));
        routeButtons.put(route, btn);
        return btn;
    }

    /** Tạo nút submenu — KHÔNG indent thêm, chỉ nhỏ hơn + nền khác */
    private void mkSub(String route, String label, String iconPath) {
        SidebarButton btn = new SidebarButton(label, true, iconPath);
        btn.addActionListener(e -> showCard(route));
        searchSubButtons.put(route, btn);
        searchSubmenuPanel.add(btn);
    }

    // ── Sidebar state ──

    private void toggleSearch() {
        searchExpanded = !searchExpanded;
        searchSubmenuPanel.setVisible(searchExpanded);
        searchMainButton.setActive(searchExpanded || isSearchCard(activeCard));
        revalidate(); repaint();
    }

    private void showCard(String card) {
        activeCard = card;
        cardLayout.show(contentCards, card);
        updateTitle(card);
        updateSidebarState(card);
    }

    private void updateTitle(String card) {
        switch (card) {
            case "home":     headerTitle.setText("THÔNG TIN CÁ NHÂN"); break;
            case "dat-ve":   headerTitle.setText("ĐẶT VÉ TÀU");        break;
            case "doi-tra":  headerTitle.setText("ĐỔI/TRẢ VÉ");        break;
            case "thong-ke": headerTitle.setText("THỐNG KÊ");           break;
            case "ho-tro":   headerTitle.setText("HỖ TRỢ");            break;
            default: headerTitle.setText(isSearchCard(card) ? "TRA CỨU" : "THÔNG TIN CÁ NHÂN");
        }
    }

    private void updateSidebarState(String card) {
        searchExpanded = isSearchCard(card);
        searchSubmenuPanel.setVisible(searchExpanded);
        routeButtons.forEach((r, b) ->
            b.setActive(r.equals(card) || ("tra-cuu".equals(r) && isSearchCard(card))));
        searchSubButtons.forEach((r, b) -> b.setActive(r.equals(card)));
        searchMainButton.setActive(searchExpanded || isSearchCard(card));
    }

    private boolean isSearchCard(String c) { return searchSubButtons.containsKey(c); }

    // ═══════════════════════════════════════════
    //  MAIN AREA
    // ═══════════════════════════════════════════

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(LIGHT_BG);
        main.add(buildTopHeader(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(LIGHT_BG);
        center.setBorder(new EmptyBorder(14, 14, 14, 14));
        center.add(contentCards, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 224)));
        h.setPreferredSize(new Dimension(0, 82));

        headerTitle.setFont(font("Segoe UI", Font.PLAIN, 28));
        headerTitle.setForeground(TEXT);
        headerTitle.setBorder(new EmptyBorder(0, 28, 0, 0));

        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setOpaque(false);
        profile.setBorder(new EmptyBorder(10, 0, 10, 22));
        profile.add(new ProfileIcon(), BorderLayout.WEST);
        JPanel pt = new JPanel();
        pt.setOpaque(false); pt.setLayout(new BoxLayout(pt, BoxLayout.Y_AXIS));
        JLabel role = new JLabel("Nhân viên");
        role.setFont(font("Segoe UI", Font.PLAIN, 16)); role.setForeground(TEXT);
        JLabel name = new JLabel("Tên nhân viên");
        name.setFont(font("Segoe UI", Font.PLAIN, 13)); name.setForeground(SUB_TEXT);
        pt.add(role); pt.add(name);
        profile.add(pt, BorderLayout.CENTER);

        h.add(headerTitle, BorderLayout.WEST);
        h.add(profile,     BorderLayout.EAST);
        return h;
    }

    private void registerCards() {
        contentCards.setBackground(LIGHT_BG);
        contentCards.add(new DashboardPanel(),                                                                                                        "home");
        contentCards.add(new PlaceholderPanel("TRA CỨU CHUYẾN ĐI",  "Dùng để tìm danh sách chuyến, điểm đi, điểm đến và giờ chạy."),   "tra-cuu-chuyen");
        contentCards.add(new PlaceholderPanel("TRA CỨU TÀU",        "Dùng để xem thông tin tàu, số toa, ghế và trạng thái vận hành."), "tra-cuu-tau");
        contentCards.add(new PlaceholderPanel("TRA CỨU VÉ",         "Dùng để tra cứu vé theo mã vé, mã đơn hoặc hành khách."),         "tra-cuu-ve");
        contentCards.add(new PlaceholderPanel("TRA CỨU KHÁCH HÀNG", "Dùng để xem thông tin khách hàng và lịch sử đặt vé."),            "tra-cuu-khach");
        contentCards.add(new PlaceholderPanel("TRANG ĐẶT VÉ",       "Dùng để chọn chuyến tàu, ghế và tạo đơn đặt vé."),               "dat-ve");
        contentCards.add(new PlaceholderPanel("TRANG ĐỔI/TRẢ VÉ",  "Dùng để tìm vé đã đặt và xử lý đổi hoặc trả vé."),               "doi-tra");
        contentCards.add(new PlaceholderPanel("TRANG THỐNG KÊ",     "Dùng để xem báo cáo ca làm, doanh thu và số lượng vé."),          "thong-ke");
        contentCards.add(new PlaceholderPanel("TRANG HỖ TRỢ",       "Dùng để hiển thị hướng dẫn và liên hệ hỗ trợ."),                 "ho-tro");
    }

    // ═══════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════

    private Font font(String f, int s, int z) { return new Font(f, s, z); }

    private Icon loadIcon(String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url == null) return fallbackIcon(path, w, h);
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private Icon fallbackIcon(String path, int w, int h) {
        String key = path == null ? "" : path.toLowerCase();
        String glyph;
        if (key.contains("search")) {
            glyph = "⌕";
        } else if (key.contains("home")) {
            glyph = "⌂";
        } else if (key.contains("list")) {
            glyph = "≣";
        } else if (key.contains("train")) {
            glyph = "T";
        } else if (key.contains("ticket")) {
            glyph = "✉";
        } else if (key.contains("customer")) {
            glyph = "☺";
        } else if (key.contains("book")) {
            glyph = "B";
        } else if (key.contains("exchange")) {
            glyph = "↔";
        } else if (key.contains("statistics")) {
            glyph = "▥";
        } else if (key.contains("help")) {
            glyph = "?";
        } else if (key.contains("logout")) {
            glyph = "↪";
        } else {
            glyph = "•";
        }
        return new GlyphIcon(w, h, glyph);
    }

    private JPanel sectionTitle(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true); p.setBackground(new Color(238, 242, 248));
        p.setBorder(new EmptyBorder(10, 18, 10, 18));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JLabel l = new JLabel(text);
        l.setFont(font("Segoe UI", Font.PLAIN, 18)); l.setForeground(TEXT);
        p.add(l, BorderLayout.WEST); return p;
    }

    private void addLabel(JPanel p, java.awt.GridBagConstraints g, int x, int y, String t) {
        g.gridx = x; g.gridy = y; g.weightx = 0;
        JLabel l = new JLabel(t); l.setFont(font("Segoe UI", Font.PLAIN, 15)); l.setForeground(TEXT);
        p.add(l, g);
    }

    private void addField(JPanel p, java.awt.GridBagConstraints g, int x, int y, String t) {
        g.gridx = x; g.gridy = y; g.weightx = (x == 1 || x == 3) ? 1.0 : 0.0;
        JTextField f = new JTextField(t);
        f.setFont(font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(new LineBorder(new Color(210, 215, 224), 1, true));
        f.setBackground(Color.WHITE); f.setPreferredSize(new Dimension(180, 30));
        p.add(f, g);
    }

    private JPanel infoRow(String lbl, String val) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        JLabel l = new JLabel(lbl); l.setFont(font("Segoe UI", Font.PLAIN, 15));
        l.setForeground(TEXT); l.setPreferredSize(new Dimension(130, 24));
        JLabel v = new JLabel(val); v.setFont(font("Segoe UI", Font.PLAIN, 15)); v.setForeground(TEXT);
        row.add(l, BorderLayout.WEST); row.add(v, BorderLayout.CENTER); return row;
    }

    // ═══════════════════════════════════════════
    //  INNER PANELS
    // ═══════════════════════════════════════════

    private class DashboardPanel extends JPanel {
        DashboardPanel() {
            setBackground(LIGHT_BG); setLayout(new BorderLayout(0, 14));
            JLabel welcome = new JLabel("Xin chào, Nhân viên!");
            welcome.setFont(font("Segoe UI", Font.PLAIN, 20)); welcome.setForeground(TEXT);
            JPanel topInfo = new JPanel(new BorderLayout()); topInfo.setOpaque(false);
            topInfo.add(welcome, BorderLayout.WEST);

            JPanel infoBox = new JPanel(new BorderLayout());
            infoBox.setBackground(PANEL_BG);
            infoBox.setBorder(new LineBorder(new Color(210, 215, 224), 1, true));

            JPanel pp = new JPanel(new BorderLayout()); pp.setOpaque(false);
            pp.setBorder(new EmptyBorder(12, 18, 14, 18)); pp.add(buildPersonal(), BorderLayout.CENTER);
            JPanel wp = new JPanel(new BorderLayout()); wp.setOpaque(false);
            wp.setBorder(new EmptyBorder(0, 18, 18, 18)); wp.add(buildWork(), BorderLayout.CENTER);
            JPanel body = new JPanel(); body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.add(pp); body.add(wp);
            infoBox.add(body, BorderLayout.CENTER);
            add(topInfo, BorderLayout.NORTH); add(infoBox, BorderLayout.CENTER);
        }

        private JPanel buildPersonal() {
            JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
            panel.add(sectionTitle("THÔNG TIN CÁ NHÂN"), BorderLayout.NORTH);
            JPanel form = new JPanel(new java.awt.GridBagLayout()); form.setOpaque(false);
            form.setBorder(new EmptyBorder(14, 0, 0, 0));
            java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
            g.insets = new java.awt.Insets(6, 6, 6, 6);
            g.fill   = java.awt.GridBagConstraints.HORIZONTAL;
            addLabel(form,g,0,0,"Mã nhân viên:"); addField(form,g,1,0,"NV001");
            addLabel(form,g,2,0,"Vai trò:");       addField(form,g,3,0,"Nhân viên");
            addLabel(form,g,0,1,"Họ và tên:");     addField(form,g,1,1,"Nguyễn Văn A");
            addLabel(form,g,2,1,"Ngày sinh:");     addField(form,g,3,1,"01/01/2000");
            addLabel(form,g,0,2,"Giới tính:");     addField(form,g,1,2,"Nam");
            addLabel(form,g,2,2,"Số điện thoại:"); addField(form,g,3,2,"0909 999 999");
            addLabel(form,g,0,3,"Email:");         addField(form,g,1,3,"nhanvien@tau.vn");
            addLabel(form,g,2,3,"");               addField(form,g,3,3,"");
            addLabel(form,g,0,4,"Địa chỉ:");       addField(form,g,1,4,"123 Đường Sắt, TP.HCM");
            panel.add(form, BorderLayout.CENTER); return panel;
        }

        private JPanel buildWork() {
            JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
            panel.add(sectionTitle("THÔNG TIN CÔNG VIỆC"), BorderLayout.NORTH);
            JPanel c = new JPanel(); c.setOpaque(false);
            c.setBorder(new EmptyBorder(16, 6, 0, 6));
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
            c.add(infoRow("Ca làm việc:", "Ca sáng (06:00 - 14:00)"));
            c.add(Box.createVerticalStrut(8));
            c.add(infoRow("Giờ vào làm:", "09:03:51"));
            c.add(Box.createVerticalStrut(8));
            c.add(infoRow("Trạng thái:", "Đang làm việc"));
            panel.add(c, BorderLayout.CENTER); return panel;
        }
    }

    private class PlaceholderPanel extends JPanel {
        PlaceholderPanel(String title, String desc) {
            setBackground(LIGHT_BG); setLayout(new BorderLayout());
            setBorder(new LineBorder(new Color(210, 215, 224), 1, true));
            JPanel box = new JPanel(); box.setOpaque(false);
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBorder(new EmptyBorder(28, 30, 30, 30));
            JLabel tl = new JLabel(title); tl.setFont(font("Segoe UI", Font.BOLD, 24)); tl.setForeground(TEXT);
            JLabel dl = new JLabel("<html><div style='width:520px;'>" + desc + "</div></html>");
            dl.setFont(font("Segoe UI", Font.PLAIN, 16)); dl.setForeground(SUB_TEXT);
            box.add(tl); box.add(Box.createVerticalStrut(12)); box.add(dl);
            box.add(Box.createVerticalStrut(20));
            JPanel hint = new JPanel(new BorderLayout());
            hint.setBackground(Color.WHITE);
            hint.setBorder(new LineBorder(new Color(225, 229, 236), 1, true));
            hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            hint.add(new JLabel("   Nội dung sẽ được phát triển sau"), BorderLayout.WEST);
            box.add(hint); add(box, BorderLayout.NORTH);
        }
    }

    // ═══════════════════════════════════════════
    //  SIDEBAR BUTTON
    //  – Tất cả nút dùng cùng LEFT_PAD
    //  – Submenu chỉ khác: nền, font nhỏ hơn, chiều cao thấp hơn
    // ═══════════════════════════════════════════

    private class SidebarButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color activeBg;
        private boolean active;

        /** isSubmenu = true → nền xám, font nhỏ hơn, cao 38px */
        SidebarButton(String text, boolean isSubmenu) {
            this(text, isSubmenu, null);
        }

        SidebarButton(String text, boolean isSubmenu, String iconPath) {
            setAlignmentX(LEFT_ALIGNMENT);
            int h = isSubmenu ? 38 : 52;
            setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            setPreferredSize(new Dimension(SIDEBAR_W, h));
            setMinimumSize(new Dimension(SIDEBAR_W, h));

            setFont(font("Segoe UI", Font.PLAIN, isSubmenu ? 13 : 15));
            setForeground(isSubmenu ? new Color(40, 40, 40) : Color.WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(true);
            setContentAreaFilled(true);

            int leftPad = isSubmenu ? SUBMENU_LEFT_PAD : LEFT_PAD;
            setBorder(new EmptyBorder(0, leftPad, 0, 8));

            normalBg = isSubmenu ? SUBMENU_BG   : NAVY;
            hoverBg  = isSubmenu ? SUBMENU_HOVER : NAVY_HOVER;
            activeBg = isSubmenu ? SUBMENU_SELECTED : ACCENT;
            setBackground(normalBg);
            setText(text);
            if (isSubmenu && iconPath != null) {
                Icon icon = loadIcon(iconPath, 14, 14);
                if (icon != null) {
                    setIcon(icon);
                    setIconTextGap(6);
                }
            }

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { if (!active) setBackground(hoverBg); }
                public void mouseExited (java.awt.event.MouseEvent e) { if (!active) setBackground(normalBg); }
            });
        }

        void setActive(boolean a) { active = a; setBackground(a ? activeBg : normalBg); }
        void setExpanded(boolean b) { /* reserved */ }
    }

    // ═══════════════════════════════════════════
    //  PROFILE ICON
    // ═══════════════════════════════════════════

    private static class ProfileIcon extends JPanel {
        ProfileIcon() {
            setOpaque(false);
            setPreferredSize(new Dimension(48, 48));
            setMinimumSize(new Dimension(48, 48));
            setMaximumSize(new Dimension(48, 48));
        }
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

    private static class GlyphIcon implements Icon {
        private final int width;
        private final int height;
        private final String glyph;

        GlyphIcon(int width, int height, String glyph) {
            this.width = width;
            this.height = height;
            this.glyph = glyph;
        }

        public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
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

        public int getIconWidth() { return width; }
        public int getIconHeight() { return height; }
    }
}
