package gui;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class AppFrame extends JFrame {
	private final CardLayout cardLayout = new CardLayout();
	private final JPanel contentCards = new JPanel(cardLayout);
	private final Map<String, SidebarButton> routeButtons = new LinkedHashMap<>();
	private final Map<String, SidebarButton> searchSubButtons = new LinkedHashMap<>();
	private final JLabel headerTitle = new JLabel("THÔNG TIN CÁ NHÂN");
	private LoadingPanel loadingPanel;
	// References để gọi refresh() khi navigate
	private DoiVeGUI  doiVeGUI;
	private TraVeGUI  traVeGUI;
	private DoiVeGUI1 doiVeGUI1;
	private TraVeGUI1 traVeGUI1;

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
		} catch (Exception ignored) {
		}

		JPanel root = new JPanel(new BorderLayout());
		root.setBackground(GuiTheme.LIGHT_BG);
		setContentPane(root);
		root.add(buildSidebar(), BorderLayout.WEST);
		root.add(buildMainArea(), BorderLayout.CENTER);

		registerCards();
		showCard("login"); // mặc định mở trang hồ sơ
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
		logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
		logoArea.setAlignmentX(Component.LEFT_ALIGNMENT);

		int logoHeight = 70;
		logoArea.setMaximumSize(new Dimension(GuiTheme.SIDEBAR_W, logoHeight));
		logoArea.setPreferredSize(new Dimension(GuiTheme.SIDEBAR_W, logoHeight));
		logoArea.setBorder(new EmptyBorder(0, 0, 0, 0));

		// Tạo Label Logo
		JLabel labelLogo = new JLabel(GuiIcons.loadIcon(AppFrame.class, "/Images/logoTrain.png", 80, 80));

		labelLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

		labelLogo.setBorder(new EmptyBorder(0, 20, 0, 0));

		logoArea.add(labelLogo);
		sb.add(logoArea);
		JSeparator sep = new JSeparator();
		sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		sep.setForeground(new Color(200, 200, 200));

		sb.add(sep);

		searchMainButton = mkBtn("tra-cuu", "Tra cứu", "/Images/traCuu.png", true);
		// TODO: thay "/Image/iconTraCuu.png" bằng icon tra cứu thực tế
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
		sb.add(mkBtn("dat-ve", "Đặt vé tàu", "/Images/trainTicket.png", false));
		sb.add(mkBtn("doi-tra", "Đổi/Trả vé", "/Images/change.png", false));
		sb.add(mkBtn("thong-ke", "Thống kê ca làm", "/Images/ThongKe.png", false));
		sb.add(mkBtn("ho-tro", "Hỗ trợ", "/Images/HoTro.png", false));
		sb.add(Box.createVerticalGlue());
		sb.add(Box.createVerticalStrut(16));
		SidebarButton logout = new SidebarButton("Đăng xuất", false, "/Images/DangXuat.png");
		logout.addActionListener(e -> {
			int choice = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận",
					JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				// Chỉ cần gọi hàm showCard chuyển về lá bài "login"
				showCard("login");
			}
		});
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
		routeButtons.forEach((r, b) -> b.setActive(false));
		searchMainButton.setActive(searchExpanded || isSearchCard(activeCard));
		revalidate();
		repaint();
	}

	public void showCard(String card) {
		activeCard = card;
		cardLayout.show(contentCards, card);

		boolean isLogin = card.equals("login");

		// Tự động tìm và giấu Sidebar đi nếu đang ở trang Login
		BorderLayout layout = (BorderLayout) getContentPane().getLayout();
		Component sidebar = layout.getLayoutComponent(BorderLayout.WEST);
		if (sidebar != null) {
			sidebar.setVisible(!isLogin);
		}

		// Tự động tìm và giấu thanh Header (chỗ có tên nhân viên) đi
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
		if ("doi-ve".equals(card)       && doiVeGUI  != null) doiVeGUI .refresh();
		if ("tra-ve".equals(card)       && traVeGUI  != null) traVeGUI .refresh();
		if ("doi-ve-step-2".equals(card) && doiVeGUI1 != null) doiVeGUI1.refresh();
		if ("tra-ve-step-2".equals(card) && traVeGUI1 != null) traVeGUI1.refresh();
	}

	private void updateTitle(String card) {
		switch (card) {
			case "home":
				headerTitle.setText("THÔNG TIN CÁ NHÂN");
				break;
			case "tra-cuu-chuyen":
				headerTitle.setText("DANH SÁCH CHUYẾN ĐI");
				break;
			case "tra-cuu-tau":
				headerTitle.setText("TRA CỨU TÀU");
				break;
			case "tra-cuu-ve":
				headerTitle.setText("TRA CỨU VÉ");
				break;
			case "tra-cuu-khach":
				headerTitle.setText("TRA CỨU KHÁCH HÀNG");
				break;
			case "dat-ve":
				headerTitle.setText("ĐẶT VÉ TÀU");
				break;
			case "doi-tra":
				headerTitle.setText("ĐỔI/TRẢ VÉ");
				break;
			case "thong-ke":
				headerTitle.setText("THỐNG KÊ CA LÀM" + "");
				break;
			case "ho-tro":
				headerTitle.setText("HỖ TRỢ");
				break;
			case "tra-ve":
				headerTitle.setText("TRẢ VÉ");
				break;
			case "tra-ve-step-2":
				headerTitle.setText("TRẢ VÉ");
				break;
			case "doi-ve":
				headerTitle.setText("ĐỔI VÉ");
				break;
			case "doi-ve-step-2":
				headerTitle.setText("ĐỔI VÉ");
				break;
			default:
				headerTitle.setText("THÔNG TIN CÁ NHÂN");
		}
	}

	private void updateSidebarState(String card) {
		searchExpanded = isSearchCard(card);
		searchSubmenuPanel.setVisible(searchExpanded);
		routeButtons.forEach((r, b) -> b.setActive(false));
		searchSubButtons.forEach((r, b) -> b.setActive(false));
		searchMainButton.setActive(false);
		if ("home".equals(card)) {
			return;
		}
		if (isSearchCard(card)) {
			searchMainButton.setActive(true);
			searchSubmenuPanel.setVisible(true);
			SidebarButton sub = searchSubButtons.get(card);
			if (sub != null)
				sub.setActive(true);
		} else {
			String targetButtonKey = card;
			if (card.startsWith("doi-ve") || card.startsWith("tra-ve")) {
				targetButtonKey = "doi-tra";
			}
			SidebarButton btn = routeButtons.get(targetButtonKey);
			if (btn != null) {
				btn.setActive(true);
			}
		}
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
		JLabel name = new JLabel("Tên nhân viên");
		name.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
		name.setForeground(GuiTheme.SUB_TEXT);
		pt.add(role);
		pt.add(name);
		profile.add(pt, BorderLayout.CENTER);

		profile.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				showCard("home");
			}
		});

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
		contentCards.add(new LoginPanel(this), "login");

		// THÊM MÀN HÌNH LOADING VÀO ĐÂY:
		loadingPanel = new LoadingPanel();
		contentCards.add(loadingPanel, "loading");

		contentCards.setBackground(GuiTheme.LIGHT_BG);
		contentCards.add(new HomeGUI(), "home");
		contentCards.add(new LoginPanel(this), "login");
		contentCards.setBackground(GuiTheme.LIGHT_BG);
		contentCards.add(new HomeGUI(), "home");
		contentCards.add(new DanhSachChuyenDiGUI(), "tra-cuu-chuyen");
		contentCards.add(new TauGUI(), "tra-cuu-tau");
		contentCards.add(new VeGUI(), "tra-cuu-ve");
		contentCards.add(new KhachHangGUI(), "tra-cuu-khach");
		contentCards.add(new DatVeGUI(), "dat-ve");
		contentCards.add(new ThongKeGUI(), "thong-ke");
		contentCards.add(new HoTroGUI(), "ho-tro");
		contentCards.add(new DoiTraGUI(this), "doi-tra");

		doiVeGUI = new DoiVeGUI(this);
		traVeGUI = new TraVeGUI(this);
		contentCards.add(traVeGUI, "tra-ve");
		traVeGUI1 = new TraVeGUI1(this);
		contentCards.add(traVeGUI1, "tra-ve-step-2");
		contentCards.add(doiVeGUI, "doi-ve");
		doiVeGUI1 = new DoiVeGUI1(this);
		contentCards.add(doiVeGUI1, "doi-ve-step-2");
	}

	public void onLoginSuccess(boolean isAdmin) {
		if (isAdmin) {
			AppFrameManager managerFrame = new AppFrameManager();
			managerFrame.setVisible(true);
			this.dispose(); 
		} else {
			// 1. Phủ mờ
			JPanel glassPane = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					g.setColor(new Color(0, 0, 0, 200)); // 200 là tối đậm như ý bác
					g.fillRect(0, 0, getWidth(), getHeight());
				}
			};
			glassPane.setOpaque(false);
			this.setGlassPane(glassPane);
			glassPane.setVisible(true);

			// 2. Hiện Popup
			MoCaDialog dialog = new MoCaDialog(this);
			dialog.setVisible(true); 

			// 3. Tắt phủ mờ
			glassPane.setVisible(false);

			// 4. KIỂM TRA: Bấm xác nhận thì nhảy vào màn hình Loading
			if (dialog.isConfirmed()) {
				showCard("loading"); // Nhảy qua thẻ Loading trắng tinh
				
				// Gọi hàm chạy thanh phần trăm, xong 100% thì mới vô "home"
				loadingPanel.startLoading(() -> {
					showCard("dat-ve");
				});
				
			} else {
				showCard("login"); // Hủy thì quay lại đăng nhập
			}
		}
	}
}