package gui;
import service.AuthService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class AppFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final Map<String, SidebarButton> routeButtons = new LinkedHashMap<>();
    private final Map<String, SidebarButton> searchSubButtons = new LinkedHashMap<>();
    private final JLabel headerTitle = new JLabel("THÔNG TIN CÁ NHÂN");
    private LoadingPanel loadingPanel;
    private JLabel lblName;
    // References để gọi refresh() khi navigate
    private DoiVeGUI  doiVeGUI;
    private TraVeGUI  traVeGUI;
    private DoiVeGUI1 doiVeGUI1;
    private TraVeGUI1 traVeGUI1;
    private DoiVeGUI0 doiVeGUI0;
    private DoiTraGUI doiTraGUI;
    private ThongKeGUI thongKeGUI; // THÊM MỚI
    private HomeGUI homeGUI;
    private DatVeGUI datVeGUI;
    private LoginPanel loginPanel;
    private DoiVeGUI2 doiVeGUI2;;

// Trong hàm khởi tạo / registerCards

    private JPanel searchSubmenuPanel;
    private SidebarButton searchMainButton;
    private boolean searchExpanded;
    private String activeCard = "home";

    public AppFrame() {
        setTitle("Quản lý bán vé tàu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setMinimumSize(new Dimension(1366, 768));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GuiTheme.LIGHT_BG);
        setContentPane(root);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);

        registerCards();
        showCard("login");
        registerGlobalShortcuts();
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
        logoArea.setBackground(GuiTheme.NAVY);
        logoArea.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));
        logoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        int logoHeight = 70;
        logoArea.setMaximumSize(new Dimension(GuiTheme.SIDEBAR_W, logoHeight));
        logoArea.setPreferredSize(new Dimension(GuiTheme.SIDEBAR_W, logoHeight));

        JLabel labelLogo = new JLabel(GuiIcons.loadIcon(AppFrame.class, "/Images/logoTrain.png", 70, 70));
        logoArea.add(labelLogo);
        sb.add(logoArea);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(200, 200, 200));
        sb.add(sep);

        searchMainButton = mkBtn("tra-cuu", "Tra cứu", "/Images/traCuu.png", true);
        searchMainButton.setToolTipText("Ctrl + F  —  Tra cứu");
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
        mkSub("tra-cuu-chuyen", "Danh sách chuyến đi", "/Images/DanhSach.png");
        mkSub("tra-cuu-tau", "Tàu", "/Images/Tau.png");
        mkSub("tra-cuu-ve", "Vé", "/Images/Ve.png");
        mkSub("tra-cuu-khach", "Khách hàng", "/Images/KhachHang.png");
        sb.add(searchSubmenuPanel);

        sb.add(mkBtn("dat-ve",    "Đặt vé tàu",      "/Images/trainTicket.png", false));
        sb.add(mkBtn("doi-tra",   "Đổi/Trả vé",      "/Images/change.png",      false));
        sb.add(mkBtn("thong-ke",  "Thống kê ca làm",  "/Images/ThongKe.png",     false));
        sb.add(mkBtn("ho-tro",    "Hỗ trợ",           "/Images/HoTro.png",       false));
        sb.add(Box.createVerticalGlue());
        sb.add(Box.createVerticalStrut(16));

        SidebarButton logout = new SidebarButton("Đăng xuất", false, "/Images/DangXuat.png");
        logout.setToolTipText("Ctrl + L");
        logout.addActionListener(e -> doLogout());
        sb.add(logout);

        return sb;
    }

    private SidebarButton mkBtn(String route, String label, String iconPath, boolean isSearchMain) {
        SidebarButton btn = new SidebarButton(label, false, iconPath);
        if (!isSearchMain) {
            btn.addActionListener(e -> showCard(route));
        }
        // Tooltip phím tắt
        String shortcut = getShortcutLabel(route);
        if (shortcut != null) btn.setToolTipText(shortcut);
        routeButtons.put(route, btn);
        return btn;
    }

    private void mkSub(String route, String label, String iconPath) {
        SidebarButton btn = new SidebarButton(label, true, iconPath);
        btn.addActionListener(e -> showCard(route));
        // Tooltip phím tắt
        String shortcut = getShortcutLabel(route);
        if (shortcut != null) btn.setToolTipText(shortcut);
        searchSubButtons.put(route, btn);
        searchSubmenuPanel.add(btn);
    }

    /** Trả về chuỗi gợi ý phím tắt cho từng route */
    private String getShortcutLabel(String route) {
        switch (route) {
            case "tra-cuu":        return "Ctrl + F";
            case "tra-cuu-chuyen": return "Ctrl + D";
            case "tra-cuu-tau":    return "Ctrl + T";
            case "tra-cuu-ve":     return "Ctrl + V";
            case "tra-cuu-khach":  return "Ctrl + K";
            case "dat-ve":         return "Ctrl + B";
            case "doi-tra":        return "Ctrl + R";
            case "thong-ke":       return "Ctrl + G";
            case "ho-tro":         return "F1";

            default:               return null;
        }
    }

    /** Xác nhận và thực hiện đăng xuất */
    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            showCard("login");
        }
    }

    /** Đăng ký tất cả phím tắt toàn cục */
    private void registerGlobalShortcuts() {
        JPanel root = (JPanel) getContentPane();
        javax.swing.InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = root.getActionMap();

        // Helper để bind
        java.util.function.BiConsumer<KeyStroke, Runnable> bind = (ks, action) -> {
            String key = ks.toString();
            im.put(ks, key);
            am.put(key, new AbstractAction() {
                public void actionPerformed(ActionEvent e) { action.run(); }
            });
        };

        int CTRL = KeyEvent.CTRL_DOWN_MASK;

        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_F, CTRL), () -> {
            if (!"login".equals(activeCard)) toggleSearch();
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_D, CTRL), () -> {
            if (!"login".equals(activeCard)) showCard("tra-cuu-chuyen");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_T, CTRL), () -> {
            if (!"login".equals(activeCard)) showCard("tra-cuu-tau");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_V, CTRL), () -> {
            if (!"login".equals(activeCard)) showCard("tra-cuu-ve");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_K, CTRL), () -> {
            if (!"login".equals(activeCard)) showCard("tra-cuu-khach");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_B, CTRL), () -> {
            if (!"login".equals(activeCard)) showCard("dat-ve");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_R, CTRL), () -> {
            if (!"login".equals(activeCard)) showCard("doi-tra");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_H, CTRL), () -> {
            if (!"login".equals(activeCard)) showCard("home");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_G, CTRL), () -> {
            if (!"login".equals(activeCard)) showCard("thong-ke");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_L, CTRL), () -> {
            if (!"login".equals(activeCard)) doLogout();
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), () -> {
            if (!"login".equals(activeCard)) showCard("ho-tro");
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), () -> {
            if (!"login".equals(activeCard)) showCard(activeCard); // refresh
        });
        bind.accept(KeyStroke.getKeyStroke(KeyEvent.VK_L, CTRL), () -> {
            if (!"login".equals(activeCard)) {
                int choice = JOptionPane.showConfirmDialog(AppFrame.this,
                        "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) showCard("login");
            }
        });
    }

    private void toggleSearch() {
        searchExpanded = !searchExpanded;
        searchSubmenuPanel.setVisible(searchExpanded);
        routeButtons.forEach((r, b) -> b.setActive(false));
        searchMainButton.setActive(searchExpanded || isSearchCard(activeCard));
        revalidate();
        repaint();
    }

    public void showCard(String card) {
        activeCard = card;
        cardLayout.show(contentCards, card);

        boolean isLogin = card.equals("login");

        BorderLayout layout = (BorderLayout) getContentPane().getLayout();
        Component sidebar = layout.getLayoutComponent(BorderLayout.WEST);
        if (sidebar != null) {
            sidebar.setVisible(!isLogin);
        }

        Component mainArea = layout.getLayoutComponent(BorderLayout.CENTER);
        if (mainArea instanceof JPanel) {
            BorderLayout mainLayout = (BorderLayout) ((JPanel) mainArea).getLayout();
            Component header = mainLayout.getLayoutComponent(BorderLayout.NORTH);
            if (header != null) {
                header.setVisible(!isLogin);
            }
        }

        if (!isLogin) {
            updateTitle(card);
            updateSidebarState(card);
        }

        if ("doi-ve".equals(card)        && doiVeGUI  != null) doiVeGUI .refresh();
        if ("doi-ve-step-1".equals(card) && doiVeGUI0 != null) doiVeGUI0.refresh();
        if ("doi-ve-step-2".equals(card) && doiVeGUI1 != null) doiVeGUI1.refresh();
        if ("doi-ve-step-3".equals(card) && doiVeGUI2 != null) doiVeGUI2.refresh();
        if ("tra-ve".equals(card)        && traVeGUI  != null) traVeGUI .refresh();
        if ("tra-ve-step-2".equals(card) && traVeGUI1 != null) traVeGUI1.refresh();
        if ("doi-tra".equals(card)       && doiTraGUI != null) doiTraGUI.refresh();
        if ("dat-ve".equals(card)  && datVeGUI   != null) datVeGUI.refresh();
        if ("login".equals(card)   && loginPanel != null) loginPanel.refresh();
        if ("home".equals(card)    && homeGUI    != null) homeGUI.refresh();
        if ("thong-ke".equals(card)  && thongKeGUI != null) thongKeGUI.loadData();
    }

    private void updateTitle(String card) {
        switch (card) {
            case "home":           headerTitle.setText("THÔNG TIN CÁ NHÂN");      break;
            case "tra-cuu-chuyen": headerTitle.setText("DANH SÁCH CHUYẾN ĐI");    break;
            case "tra-cuu-tau":    headerTitle.setText("TRA CỨU TÀU");             break;
            case "tra-cuu-ve":     headerTitle.setText("TRA CỨU VÉ");              break;
            case "tra-cuu-khach":  headerTitle.setText("TRA CỨU KHÁCH HÀNG");     break;
            case "dat-ve":         headerTitle.setText("ĐẶT VÉ TÀU");              break;
            case "doi-tra":        headerTitle.setText("ĐỔI/TRẢ VÉ");              break;
            case "thong-ke":       headerTitle.setText("THỐNG KÊ CA LÀM");         break;
            case "ho-tro":         headerTitle.setText("HỖ TRỢ");                  break;
            case "tra-ve":
            case "tra-ve-step-2":  headerTitle.setText("TRẢ VÉ");                  break;
            case "doi-ve":
            case "doi-ve-step-2":  headerTitle.setText("ĐỔI VÉ");                  break;
            default:               headerTitle.setText("THÔNG TIN CÁ NHÂN");
        }
    }

    private void updateSidebarState(String card) {
        searchExpanded = isSearchCard(card);
        searchSubmenuPanel.setVisible(searchExpanded);
        routeButtons.forEach((r, b) -> b.setActive(false));
        searchSubButtons.forEach((r, b) -> b.setActive(false));
        searchMainButton.setActive(false);

        if ("home".equals(card)) return;

        if (isSearchCard(card)) {
            searchMainButton.setActive(true);
            searchSubmenuPanel.setVisible(true);
            SidebarButton sub = searchSubButtons.get(card);
            if (sub != null) sub.setActive(true);
        } else {
            String targetButtonKey = card;
            if (card.startsWith("doi-ve") || card.startsWith("tra-ve")) {
                targetButtonKey = "doi-tra";
            }
            SidebarButton btn = routeButtons.get(targetButtonKey);
            if (btn != null) btn.setActive(true);
        }
    }

    private boolean isSearchCard(String c) {
        return searchSubButtons.containsKey(c);
    }

    public void showTemporaryCard(Component panel, String cardName) {
        contentCards.add(panel, cardName);
        cardLayout.show(contentCards, cardName);
        updateTitle(cardName);
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
        h.setPreferredSize(new Dimension(0, 72));
        h.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(180, 185, 195)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        headerTitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 28));
        headerTitle.setForeground(GuiTheme.TEXT);
        headerTitle.setBorder(new EmptyBorder(0, 28, 0, 0));

        JPanel profile = new JPanel(new BorderLayout(10, 0));
        profile.setOpaque(false);
        profile.setBorder(new EmptyBorder(10, 0, 10, 22));
        profile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ProfileIcon profileIcon = new ProfileIcon();
        profile.add(profileIcon, BorderLayout.WEST);

        JPanel pt = new JPanel();
        pt.setOpaque(false);
        pt.setLayout(new BoxLayout(pt, BoxLayout.Y_AXIS));
        JLabel role = new JLabel("Nhân viên");
        role.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 16));
        role.setForeground(GuiTheme.TEXT);
        lblName = new JLabel(AuthService.getCurrentHoTen());
        lblName.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(GuiTheme.NAVY);
        lblName.setBorder(new EmptyBorder(4, 0, 0, 0));
        pt.add(role);
        pt.add(lblName);
        profile.add(pt, BorderLayout.CENTER);

        profile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showCard("home");
            }
        });
        profile.setToolTipText("Ctrl + H");

        JPanel rightContent = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        rightContent.setOpaque(false);
        rightContent.add(new DigitalClockWidget());
        rightContent.add(profile);

        topRow.add(headerTitle, BorderLayout.WEST);
        topRow.add(rightContent, BorderLayout.EAST);

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(210, 215, 224));
        sep.setBackground(Color.WHITE);

        h.add(topRow, BorderLayout.CENTER);
        h.add(sep, BorderLayout.SOUTH);
        return h;
    }

    private void registerCards() {
        loadingPanel = new LoadingPanel();
        contentCards.add(loadingPanel, "loading");
        contentCards.setBackground(GuiTheme.LIGHT_BG);
        datVeGUI = new DatVeGUI();
        contentCards.add(datVeGUI, "dat-ve");
        loginPanel = new LoginPanel(this);
        contentCards.add(loginPanel, "login");
        homeGUI = new HomeGUI();
        contentCards.add(homeGUI, "home");
        contentCards.add(new DanhSachChuyenDiGUI(),  "tra-cuu-chuyen");
        contentCards.add(new TauGUI(),               "tra-cuu-tau");
        contentCards.add(new VeGUI(),                "tra-cuu-ve");
        contentCards.add(new KhachHangGUI(),         "tra-cuu-khach");
        contentCards.add(new HoTroGUI(),             "ho-tro");
        doiTraGUI = new DoiTraGUI(this);
        contentCards.add(doiTraGUI,        "doi-tra");

        // THAY ĐỔI: Lưu reference ThongKeGUI để truyền tiền mở ca
        thongKeGUI = new ThongKeGUI();
        contentCards.add(thongKeGUI, "thong-ke");

        doiVeGUI = new DoiVeGUI(this);
        traVeGUI = new TraVeGUI(this);
        contentCards.add(traVeGUI,               "tra-ve");
        traVeGUI1 = new TraVeGUI1(this);
        contentCards.add(traVeGUI1,              "tra-ve-step-2");
        contentCards.add(doiVeGUI,               "doi-ve");
        doiVeGUI1 = new DoiVeGUI1(this);
        contentCards.add(doiVeGUI1,              "doi-ve-step-2");
        doiVeGUI0 = new DoiVeGUI0(this);
        contentCards.add(doiVeGUI0,              "doi-ve-step-1");
        doiVeGUI2 = new DoiVeGUI2(this);
        contentCards.add(doiVeGUI2, "doi-ve-step-3");
    }

    public void onLoginSuccess(boolean isAdmin) {
        lblName.setText(AuthService.getCurrentHoTen());
        if (isAdmin) {
            AppFrameManager managerFrame = new AppFrameManager();
            managerFrame.setVisible(true);
            this.dispose();
        } else {
            // 1. Phủ mờ
            JPanel glassPane = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(0, 0, 0, 200));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            glassPane.setOpaque(false);
            this.setGlassPane(glassPane);
            glassPane.setVisible(true);

            // 2. Hiện popup mở ca
            MoCaDialog dialog = new MoCaDialog(this);
            dialog.setVisible(true);

            // 3. Tắt phủ mờ
            glassPane.setVisible(false);

            // 4. Xử lý kết quả
            if (dialog.isConfirmed()) {
                homeGUI.refresh();
                // THÊM MỚI: Parse tiền mở ca và truyền vào ThongKeGUI
                long tienMoCa = Long.parseLong(dialog.getTienMoCa().replaceAll("[^\\d]", ""));
                thongKeGUI.setTienMoCa(tienMoCa);

                showCard("loading");
                loadingPanel.startLoading(() -> showCard("dat-ve"));
            } else {
                showCard("login");
            }
        }
    }
}