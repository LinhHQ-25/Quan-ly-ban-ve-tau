package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class AppFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final Map<String, SidebarButton> routeButtons = new LinkedHashMap<>();
    private final Map<String, SidebarButton> searchSubButtons = new LinkedHashMap<>();
    private final JLabel headerTitle = new JLabel("THÔNG TIN CÁ NHÂN");

    private JPanel searchSubmenuPanel;
    private SidebarButton searchMainButton;
    private boolean searchExpanded;
    private String activeCard = "home";

    public AppFrame() {
        setTitle("Hệ thống bán vé tàu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 680));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GuiTheme.LIGHT_BG);
        setContentPane(root);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);

        registerCards();
        showCard("home");
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setBackground(GuiTheme.NAVY);
        sb.setPreferredSize(new Dimension(GuiTheme.SIDEBAR_W, 0));
        sb.setMinimumSize(new Dimension(GuiTheme.SIDEBAR_W, 0));
        sb.setMaximumSize(new Dimension(GuiTheme.SIDEBAR_W, Integer.MAX_VALUE));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel logoArea = new JPanel();
        logoArea.setBackground(GuiTheme.NAVY_DARK);
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.X_AXIS));
        logoArea.setAlignmentX(LEFT_ALIGNMENT);
        logoArea.setPreferredSize(new Dimension(GuiTheme.SIDEBAR_W, 90));
        logoArea.setMinimumSize(new Dimension(GuiTheme.SIDEBAR_W, 90));
        logoArea.setMaximumSize(new Dimension(GuiTheme.SIDEBAR_W, 90));
        logoArea.setBorder(new EmptyBorder(0, GuiTheme.LEFT_PAD, 0, 8));

        Icon logoIcon = GuiIcons.loadIcon(AppFrame.class, "/Image/train.jpg", 38, 38);
        logoArea.add(new JLabel(logoIcon));
        sb.add(logoArea);
        sb.add(Box.createVerticalStrut(10));

        sb.add(mkBtn("home", "Trang chủ", "/Image/train.jpg", false));
        searchMainButton = mkBtn("tra-cuu", "Tra cứu", "/Image/iconTraCuu.png", true);
        searchMainButton.addActionListener(e -> toggleSearch());
        sb.add(searchMainButton);

        searchSubmenuPanel = new JPanel();
        searchSubmenuPanel.setLayout(new BoxLayout(searchSubmenuPanel, BoxLayout.Y_AXIS));
        searchSubmenuPanel.setOpaque(true);
        searchSubmenuPanel.setBackground(GuiTheme.SUBMENU_BG);
        searchSubmenuPanel.setVisible(false);
        searchSubmenuPanel.setAlignmentX(LEFT_ALIGNMENT);
        searchSubmenuPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        int subH = 4 * 38;
        searchSubmenuPanel.setPreferredSize(new Dimension(GuiTheme.SIDEBAR_W, subH));
        searchSubmenuPanel.setMinimumSize(new Dimension(GuiTheme.SIDEBAR_W, subH));
        searchSubmenuPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, subH));

        mkSub("tra-cuu-chuyen", "Danh sách chuyến đi", "/images/list.png");
        mkSub("tra-cuu-tau", "Tàu", "/images/train-small.png");
        mkSub("tra-cuu-ve", "Vé", "/images/ticket.png");
        mkSub("tra-cuu-khach", "Khách hàng", "/images/customer.png");
        sb.add(searchSubmenuPanel);

        sb.add(mkBtn("dat-ve", "Đặt vé tàu", "/images/book.png", false));
        sb.add(mkBtn("doi-tra", "Đổi/Trả vé", "/images/exchange.png", false));
        sb.add(mkBtn("thong-ke", "Thống kê", "/images/statistics.png", false));
        sb.add(mkBtn("ho-tro", "Hỗ trợ", "/images/help.png", false));

        sb.add(Box.createVerticalGlue());
        sb.add(Box.createVerticalStrut(16));

        SidebarButton logout = new SidebarButton("Đăng xuất", false);
        logout.addActionListener(e -> showCard("home"));
        sb.add(logout);
        return sb;
    }

    private SidebarButton mkBtn(String route, String label, String iconPath, boolean isSearchMain) {
        SidebarButton btn = new SidebarButton(label, false, iconPath);
        if (!isSearchMain) {
            btn.addActionListener(e -> showCard(route));
        }
        routeButtons.put(route, btn);
        return btn;
    }

    private void mkSub(String route, String label, String iconPath) {
        SidebarButton btn = new SidebarButton(label, true, iconPath);
        btn.addActionListener(e -> showCard(route));
        searchSubButtons.put(route, btn);
        searchSubmenuPanel.add(btn);
    }

    private void toggleSearch() {
        searchExpanded = !searchExpanded;
        searchSubmenuPanel.setVisible(searchExpanded);
        searchMainButton.setActive(searchExpanded || isSearchCard(activeCard));
        revalidate();
        repaint();
    }

    private void showCard(String card) {
        activeCard = card;
        cardLayout.show(contentCards, card);
        updateTitle(card);
        updateSidebarState(card);
    }

    private void updateTitle(String card) {
        switch (card) {
            case "home":
                headerTitle.setText("THÔNG TIN CÁ NHÂN");
                break;
            case "dat-ve":
                headerTitle.setText("ĐẶT VÉ TÀU");
                break;
            case "doi-tra":
                headerTitle.setText("ĐỔI/TRẢ VÉ");
                break;
            case "thong-ke":
                headerTitle.setText("THỐNG KÊ");
                break;
            case "ho-tro":
                headerTitle.setText("HỖ TRỢ");
                break;
            default:
                headerTitle.setText(isSearchCard(card) ? "TRA CỨU" : "THÔNG TIN CÁ NHÂN");
        }
    }

    private void updateSidebarState(String card) {
        searchExpanded = isSearchCard(card);
        searchSubmenuPanel.setVisible(searchExpanded);
        routeButtons.forEach((r, b) -> b.setActive(r.equals(card) || ("tra-cuu".equals(r) && isSearchCard(card))));
        searchSubButtons.forEach((r, b) -> b.setActive(r.equals(card)));
        searchMainButton.setActive(searchExpanded || isSearchCard(card));
    }

    private boolean isSearchCard(String c) {
        return searchSubButtons.containsKey(c);
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(GuiTheme.LIGHT_BG);
        main.add(buildTopHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(GuiTheme.LIGHT_BG);
        center.setBorder(new EmptyBorder(14, 14, 14, 14));
        center.add(contentCards, BorderLayout.CENTER);

        main.add(center, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 224)));
        h.setPreferredSize(new Dimension(0, 82));

        headerTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 28));
        headerTitle.setForeground(GuiTheme.TEXT);
        headerTitle.setBorder(new EmptyBorder(0, 28, 0, 0));

        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setOpaque(false);
        profile.setBorder(new EmptyBorder(10, 0, 10, 22));
        profile.add(new ProfileIcon(), BorderLayout.WEST);

        JPanel pt = new JPanel();
        pt.setOpaque(false);
        pt.setLayout(new BoxLayout(pt, BoxLayout.Y_AXIS));
        JLabel role = new JLabel("Nhân viên");
        role.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 16));
        role.setForeground(GuiTheme.TEXT);
        JLabel name = new JLabel("Tên nhân viên");
        name.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        name.setForeground(GuiTheme.SUB_TEXT);
        pt.add(role);
        pt.add(name);
        profile.add(pt, BorderLayout.CENTER);

        h.add(headerTitle, BorderLayout.WEST);
        h.add(profile, BorderLayout.EAST);
        return h;
    }

    private void registerCards() {
        contentCards.setBackground(GuiTheme.LIGHT_BG);
        contentCards.add(new HoSo_GUI(), "home");
        contentCards.add(new TraCuu_GUI(), "tra-cuu-chuyen");
        contentCards.add(new TraCuu_GUI(), "tra-cuu-tau");
        contentCards.add(new TraCuu_GUI(), "tra-cuu-ve");
        contentCards.add(new TraCuu_GUI(), "tra-cuu-khach");
        contentCards.add(new DatVe_GUI(), "dat-ve");
        contentCards.add(new DoiTraVe_GUI(), "doi-tra");
        contentCards.add(new ThongKe_GUI(), "thong-ke");
        contentCards.add(new HoTro_GUI(), "ho-tro");
    }
}
