package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;

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
		setTitle("Quản lý bán vé tàu");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 700);
		setLocationRelativeTo(null);
		setMinimumSize(new Dimension(1000, 600));

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

		int logoHeight = 82;
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
		// TODO: thay "/Image/iconDanhSach.png" bằng icon thực tế
		mkSub("tra-cuu-tau", "Tàu", "/Images/Tau.png");
		// TODO: thay "/Image/iconTau.png" bằng icon thực tế
		mkSub("tra-cuu-ve", "Vé", "/Images/Ve.png");
		// TODO: thay "/Image/iconVe.png" bằng icon thực tế
		mkSub("tra-cuu-khach", "Khách hàng", "/Images/KhachHang.png");
		// TODO: thay "/Image/iconKhach.png" bằng icon thực tế
		sb.add(searchSubmenuPanel);

		sb.add(mkBtn("dat-ve", "Đặt vé tàu", "/Images/trainTicket.png", false));
		// TODO: thay "/Image/iconDatVe.png" bằng icon thực tế
		sb.add(mkBtn("doi-tra", "Đổi/Trả vé", "/Images/change.png", false));
		// TODO: thay "/Image/iconDoiTra.png" bằng icon thực tế
		sb.add(mkBtn("thong-ke", "Thống kê ca làm", "/Images/ThongKe.png", false));
		// TODO: thay "/Image/iconThongKe.png" bằng icon thực tế
		sb.add(mkBtn("ho-tro", "Hỗ trợ", "/Images/HoTro.png", false));
		// TODO: thay "/Image/iconHoTro.png" bằng icon thực tế

		sb.add(Box.createVerticalGlue());
		sb.add(Box.createVerticalStrut(16));

		// TODO: thay "/Image/iconDangXuat.png" bằng icon đăng xuất thực tế
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
		searchMainButton.setActive(searchExpanded || isSearchCard(activeCard));
		revalidate();
		repaint();
	}

	private void showCard(String card) {
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

		// Chỉ cập nhật tiêu đề và menu nếu không phải trang login
		if (!isLogin) {
			updateTitle(card);
			updateSidebarState(card);
		}
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
		default:
			headerTitle.setText("THÔNG TIN CÁ NHÂN");
		}
	}

	private void updateSidebarState(String card) {
		searchExpanded = isSearchCard(card);
		searchSubmenuPanel.setVisible(searchExpanded);

		// Tắt tất cả trước
		routeButtons.forEach((r, b) -> b.setActive(false));
		searchSubButtons.forEach((r, b) -> b.setActive(false));
		searchMainButton.setActive(false);

		if ("home".equals(card)) {
			return;
		}

		if (isSearchCard(card)) {
			searchMainButton.setActive(true);
			SidebarButton sub = searchSubButtons.get(card);
			if (sub != null)
				sub.setActive(true);
		} else {
			SidebarButton btn = routeButtons.get(card);
			if (btn != null)
				btn.setActive(true);
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
		h.setPreferredSize(new Dimension(0, 82));
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

		topRow.add(headerTitle, BorderLayout.WEST);
		topRow.add(profile, BorderLayout.EAST);

		// Đường kẻ ngang dưới header
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		sep.setForeground(new Color(210, 215, 224));
		sep.setBackground(Color.WHITE);

		h.add(topRow, BorderLayout.CENTER);
		h.add(sep, BorderLayout.SOUTH);
		return h;
	}

	private void registerCards() {
		contentCards.add(new LoginPanel(this), "login");
		contentCards.setBackground(GuiTheme.LIGHT_BG);
		contentCards.add(new HomeGUI(), "home");
		contentCards.add(new DanhSachChuyenDiGUI(), "tra-cuu-chuyen");
		contentCards.add(new TauGUI(), "tra-cuu-tau");
		contentCards.add(new VeGUI(), "tra-cuu-ve");
		contentCards.add(new KhachHangGUI(), "tra-cuu-khach");
		contentCards.add(new DatVeGUI(), "dat-ve");
		contentCards.add(new DoiTraGUI(), "doi-tra");
		contentCards.add(new ThongKeGUI(), "thong-ke");
		contentCards.add(new HoTroGUI(), "ho-tro");
	}

	public void onLoginSuccess() {
		showCard("home"); // Chuyển sang lá bài Trang chủ mượt mà
	}
}