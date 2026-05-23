package gui;
import service.AuthService;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AppFrameManager extends JFrame {
	private final CardLayout cardLayout = new CardLayout();
	private final JPanel contentCards = new JPanel(cardLayout);
	private final Map<String, SidebarButton> routeButtons = new LinkedHashMap<>();
	private final JLabel headerTitle = new JLabel("TỔNG QUAN HỆ THỐNG");
	private JLabel lblName;
	public AppFrameManager() {
		setTitle("Hệ thống quản lý bán vé tàu - Dành cho Quản lý");
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

		// Mặc định hiển thị Dashboard đầu tiên.
		showCard("dashboard");
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
		logoArea.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5)); // Lề 5px cho vừa khít
		logoArea.setAlignmentX(Component.LEFT_ALIGNMENT);

		int logoHeight = 70;
		logoArea.setMaximumSize(new Dimension(GuiTheme.SIDEBAR_W, logoHeight));
		logoArea.setPreferredSize(new Dimension(GuiTheme.SIDEBAR_W, logoHeight));
		JLabel labelLogo = new JLabel(GuiIcons.loadIcon(AppFrameManager.class, "/Images/logoTrain.png", 70, 70));
		logoArea.add(labelLogo);
		sb.add(logoArea);

		JSeparator sep = new JSeparator();
		sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		sep.setForeground(new Color(200, 200, 200));
		sb.add(sep);
		sb.add(Box.createVerticalStrut(10));

		// Menu Quản lý
		sb.add(mkBtn("ql-nhanvien", "Quản lý nhân viên", "/Images/iconNV.png"));
		sb.add(mkBtn("ql-calam", "Quản lý ca làm", "/Images/logoCaLam.png"));
		sb.add(mkBtn("ql-tau", "Quản lý tàu", "/Images/Tau.png"));
		sb.add(mkBtn("ql-chuyentau", "Quản lý chuyến tàu", "/Images/iconChuyenTau.png"));
		sb.add(mkBtn("ql-khuyenmai", "Quản lý khuyến mãi", "/Images/KhuyenMai.png"));
		sb.add(mkBtn("thong-ke", "Thống kê", "/Images/ThongKe.png"));
		sb.add(mkBtn("ho-tro", "Hỗ trợ", "/Images/HoTro.png"));

		sb.add(Box.createVerticalGlue());
		sb.add(Box.createVerticalStrut(16));

		// Nút Đăng xuất
		SidebarButton logout = new SidebarButton("Đăng xuất", false, "/Images/DangXuat.png");
		logout.addActionListener(e -> {
			int choice = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				new AppFrame().setVisible(true);
				this.dispose();
			}
		});logout.setToolTipText("Ctrl + L");
		sb.add(logout);
		return sb;
		
	}

	private SidebarButton mkBtn(String route, String label, String iconPath) {
	    SidebarButton btn = new SidebarButton(label, false, iconPath);
	    btn.addActionListener(e -> showCard(route));
	    String shortcut = getShortcutLabel(route);
	    if (shortcut != null) btn.setToolTipText(shortcut);
	    routeButtons.put(route, btn);
	    return btn;
	}
	private String getShortcutLabel(String route) {
	    switch (route) {
	        case "ql-nhanvien":  return "Ctrl + N";
	        case "ql-calam":     return "Ctrl + C";
	        case "ql-tau":       return "Ctrl + U";
	        case "ql-chuyentau": return "Ctrl + T";
	        case "ql-khuyenmai": return "Ctrl + M";
	        case "thong-ke":     return "Ctrl + G";
	        case "ho-tro":       return "F1";
	        default:             return null;
	    }
	}

	private void registerGlobalShortcuts() {
	    JPanel root = (JPanel) getContentPane();
	    javax.swing.InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	    javax.swing.ActionMap am = root.getActionMap();

	    java.util.function.BiConsumer<KeyStroke, Runnable> bind = (ks, action) -> {
	        String key = ks.toString();
	        im.put(ks, key);
	        am.put(key, new AbstractAction() {
	            public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); }
	        });
	    };

	    int CTRL = java.awt.event.KeyEvent.CTRL_DOWN_MASK;

	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, CTRL), () -> showCard("ql-nhanvien"));
	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, CTRL), () -> showCard("ql-calam"));
	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, CTRL), () -> showCard("ql-tau"));
	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, CTRL), () -> showCard("ql-chuyentau"));
	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, CTRL), () -> showCard("ql-khuyenmai"));
	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, CTRL), () -> showCard("thong-ke"));
	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, CTRL), () -> showCard("ho-so"));
	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0),   () -> showCard("ho-tro"));
	    bind.accept(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, CTRL), () -> {
	        int choice = JOptionPane.showConfirmDialog(AppFrameManager.this,
	                "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
	        if (choice == JOptionPane.YES_OPTION) {
	            new AppFrame().setVisible(true);
	            dispose();
	        }
	    });
	}
	public void showCard(String card) {
		cardLayout.show(contentCards, card);
		updateTitle(card);

		// Tắt hết trạng thái sáng của các nút
		routeButtons.forEach((r, b) -> b.setActive(false));
		// Chỉ làm sáng nút nếu nút đó có tồn tại trên menu
		if (routeButtons.containsKey(card)) {
			routeButtons.get(card).setActive(true);
		}
	}

	private void updateTitle(String card) {
		switch (card) {
			case "dashboard":    headerTitle.setText("TỔNG QUAN HỆ THỐNG"); break;
			case "ql-nhanvien":  headerTitle.setText("QUẢN LÝ NHÂN VIÊN"); break;
			case "ql-calam":     headerTitle.setText("QUẢN LÝ CA LÀM VIỆC"); break;
			case "ql-tau":       headerTitle.setText("QUẢN LÝ THÔNG TIN TÀU"); break;
			case "ql-chuyentau": headerTitle.setText("QUẢN LÝ CHUYẾN TÀU"); break;
			case "ql-khuyenmai": headerTitle.setText("QUẢN LÝ KHUYẾN MÃI"); break;
			case "thong-ke":     headerTitle.setText("THỐNG KÊ DOANH THU"); break;
			case "ho-tro":       headerTitle.setText("HỖ TRỢ"); break;
			case "ho-so":        headerTitle.setText("HỒ SƠ QUẢN LÝ"); break;
		}
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

		// Profile Area
		JPanel profile = new JPanel(new BorderLayout(10, 0));
		profile.setOpaque(false);
		profile.setBorder(new EmptyBorder(10, 0, 10, 22));
		profile.setCursor(new Cursor(Cursor.HAND_CURSOR));

		profile.add(new ProfileIcon(), BorderLayout.WEST);

		JPanel pt = new JPanel();
		pt.setOpaque(false);
		pt.setLayout(new BoxLayout(pt, BoxLayout.Y_AXIS));

		JLabel role = new JLabel("Quản lý");
		role.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 16));
		role.setForeground(GuiTheme.TEXT);

		lblName = new JLabel(AuthService.getCurrentHoTen());
		lblName.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
		lblName.setForeground(GuiTheme.NAVY);
		lblName.setBorder(new EmptyBorder(4, 0, 0, 0)); 

		pt.add(role);
		pt.add(lblName);
		profile.add(pt, BorderLayout.CENTER);

		// Click vào profile để xem Hồ sơ quản lý
		profile.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override public void mouseClicked(java.awt.event.MouseEvent e) {
				showCard("ho-so");
			}
		
		});profile.setToolTipText("Ctrl + H");

		// Thêm Đồng hồ kế bên Profile
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
		// Đăng ký các khung trắng (Stub)
		contentCards.setBackground(GuiTheme.LIGHT_BG);
		contentCards.add(new DashboardManagerGUI(), "dashboard"); 
		contentCards.add(new QLyNhanVienGUI(), "ql-nhanvien");
		contentCards.add(new QuanLyCaLamGUI(), "ql-calam");
		contentCards.add(new QuanLyTauGUI(), "ql-tau");
		contentCards.add(new QuanLyChuyenTauGUI(), "ql-chuyentau");
		contentCards.add(new KhuyenMaiGUI(), "ql-khuyenmai");
		contentCards.add(new ThongKeManagerGUI(), "thong-ke");
		contentCards.add(new HoTroManagerGUI(), "ho-tro");
		contentCards.add(new HoSoQuanLyGUI(), "ho-so");
	}

	// Hàm tiện ích tạo trang trống tạm thời
	private JPanel createBlankPage(String text) {
		JPanel p = new JPanel(new GridBagLayout());
		p.setBackground(Color.WHITE);
		JLabel lbl = new JLabel(text);
		lbl.setFont(new Font("Segoe UI", Font.ITALIC, 20));
		lbl.setForeground(Color.GRAY);
		p.add(lbl);
		return p;
	}
}