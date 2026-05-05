package GUI;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;

/**
 * DoiTraGUI – Màn hình tìm & chọn vé để Đổi hoặc Trả.
 * Flow: DoiTraGUI → DoiVeGUI → DoiVeGUI1
 *       DoiTraGUI → TraVeGUI  → TraVeGUI1
 *
 * Màn hình này chỉ chịu trách nhiệm:
 *   1. Hiển thị bảng danh sách vé (có thể lọc theo mã vé).
 *   2. Validate sơ bộ điều kiện nghiệp vụ.
 *   3. Truyền dữ liệu sang màn hình tiếp theo rồi navigate.
 */
public final class DoiTraGUI extends JPanel {

	// ── Constants ────────────────────────────────────────────────────────────
	private static final Color BORDER = new Color(210, 215, 224);
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	// ── Dữ liệu giả ─────────────────────────────────────────────────────────
	// maVe → [chuyenTau, gaDi, gaDen, loaiVe, ngayGioKH, soLuong, ghe, giaTien]
	static final Map<String, String[]> FAKE_DATA = new LinkedHashMap<>();

	public static Map<String, String[]> getFakeData() { return FAKE_DATA; }

	static {
		LocalDateTime kh1 = LocalDateTime.now().plusHours(30);
		FAKE_DATA.put("27CT30", new String[]{"SE5", "Diêu Trì", "Sài Gòn", "Vé cá nhân", kh1.format(FMT), "01", "B05", "450000"});

		LocalDateTime kh2 = LocalDateTime.now().plusHours(60);
		FAKE_DATA.put("13HN05", new String[]{"SE1", "Hà Nội", "Đà Nẵng", "Vé cá nhân", kh2.format(FMT), "01", "A12", "320000"});

		LocalDateTime kh3 = LocalDateTime.now().plusHours(80);
		FAKE_DATA.put("08DN12", new String[]{"SE3", "Đà Nẵng", "Nha Trang", "Vé nhóm", kh3.format(FMT), "05", "C03", "610000"});

		LocalDateTime kh4 = LocalDateTime.now().plusHours(10);
		FAKE_DATA.put("21NT07", new String[]{"TN2", "Nha Trang", "TP.HCM", "Vé cá nhân", kh4.format(FMT), "01", "D08", "275000"});
	}

	// ── State ────────────────────────────────────────────────────────────────
	private final AppFrame appFrame;
	private DefaultTableModel tableModel;
	private JTable table;
	private JTextField txtSearch;

	// ── Constructor ──────────────────────────────────────────────────────────
	public DoiTraGUI(AppFrame appFrame) {
		this.appFrame = appFrame;
		setLayout(new BorderLayout());
		setBackground(GuiTheme.LIGHT_BG);

		JPanel page = new JPanel();
		page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
		page.setOpaque(false);
		page.setBorder(new EmptyBorder(
				GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT,
				GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

		page.add(buildNoteBox());
		page.add(Box.createVerticalStrut(12));
		page.add(buildSearchBar());
		page.add(Box.createVerticalStrut(10));
		page.add(buildTicketTable());
		page.add(Box.createVerticalStrut(12));
		page.add(buildButtonRow());

		add(page, BorderLayout.CENTER);
		reloadTable("");
	}

	// ══════════════════════════════════════════════════════════════════════
	// UI BUILDERS
	// ══════════════════════════════════════════════════════════════════════
	/** Hộp vàng tóm tắt quy định */
	private JPanel buildNoteBox() {
		JPanel p = new JPanel() {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(255, 248, 220));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.setColor(new Color(220, 190, 100));
				g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
				g2.dispose();
			}
		};
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(new EmptyBorder(10, 14, 10, 14));

		Color txtColor = new Color(120, 75, 0);
		Font  bold  = GuiTheme.font("Segoe UI", Font.BOLD,  13);
		Font  plain = GuiTheme.font("Segoe UI", Font.PLAIN, 13);
		addNoteRow(p, "QUY ĐỊNH ĐỔI / TRẢ VÉ", bold,  txtColor);
		p.add(Box.createVerticalStrut(4));
		addNoteRow(p, "1. ĐỔI VÉ:", plain, txtColor);
		addNoteRow(p, "	- Chỉ áp dụng vé cá nhân, trước giờ tàu ít nhất 24 giờ", plain, txtColor);
		addNoteRow(p, "	- Phí: 30.000 đồng mỗi vé", plain, txtColor);
		addNoteRow(p, "2. TRẢ VÉ", plain, txtColor);
		addNoteRow(p, "	- Vé cá nhân: từ 48 giờ trở lên (10%), từ 12 đến dưới 48 giờ (20%), dưới 12 giờ: không hoàn", plain, txtColor);
		addNoteRow(p, "	- Vé nhóm: từ 72 giờ trở lên (20%), từ 24 đến dưới 72 giờ (30%), dưới 24 giờ: không hoàn", plain, txtColor);
		fixStretch(p);
		return p;
	}

	private void addNoteRow(JPanel p, String text, Font font, Color color) {
		JLabel lb = new JLabel(text);
		lb.setFont(font);
		lb.setForeground(color);
		lb.setAlignmentX(LEFT_ALIGNMENT);
		p.add(lb);
	}

	/** Thanh tìm kiếm */
	private JPanel buildSearchBar() {
		JPanel p = new JPanel(new GridBagLayout());
		p.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy  = 0;
		gbc.insets = new Insets(0, 0, 0, 10);

		JLabel lb = new JLabel("Nhập mã vé cần đổi/trả:");
		lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		lb.setForeground(GuiTheme.TEXT);

		txtSearch = new JTextField();
		txtSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

		JButton btnSearch = buildNavyButton("Tìm kiếm", 110, 32);
		btnSearch.addActionListener(e -> reloadTable(txtSearch.getText().trim()));
		txtSearch.addActionListener(e -> reloadTable(txtSearch.getText().trim()));

		gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
		p.add(lb, gbc);

		gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
		p.add(wrapTextField(txtSearch), gbc);

		gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
		p.add(btnSearch, gbc);

		fixStretch(p);
		return p;
	}

	/** Bảng danh sách vé */
	private JPanel buildTicketTable() {
		tableModel = new DefaultTableModel(
				new Object[]{"Mã vé","Chuyến","Ga đi","Ga đến","Loại vé","Ngày/Giờ KH","SL","Ghế"}, 0) {
			public boolean isCellEditable(int r, int c) { return false; }
		};

		table = new JTable(tableModel);
		table.setRowHeight(28);
		table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
		table.setForeground(GuiTheme.TEXT);
		table.setGridColor(new Color(230, 233, 238));
		table.setSelectionBackground(new Color(207, 222, 243));
		table.setSelectionForeground(GuiTheme.TEXT);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
		table.getTableHeader().setBackground(Color.WHITE);
		table.getTableHeader().setForeground(GuiTheme.TEXT);
		table.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));

		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(center);

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new LineBorder(BORDER, 1, true));
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setPreferredSize(new Dimension(10000, 150));

		JPanel p = new JPanel(new BorderLayout(0, 6));
		p.setOpaque(false);

		JLabel lbTitle = new JLabel("Danh sách vé");
		lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
		lbTitle.setForeground(GuiTheme.TEXT);
		p.add(lbTitle, BorderLayout.NORTH);
		p.add(scroll,  BorderLayout.CENTER);
		fixStretch(p);
		return p;
	}

	/** Hàng nút Đổi vé / Trả vé */
	private JPanel buildButtonRow() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		p.setOpaque(false);

		JButton btnTra = buildNavyButton("Trả vé →", 120, 34);
		JButton btnDoi = buildNavyButton("Đổi vé →", 120, 34);

		btnDoi.addActionListener(e -> handleGoiDoiVe());
		btnTra.addActionListener(e -> handleGoiTraVe());

		p.add(btnTra);
		p.add(btnDoi);
		fixStretch(p);
		return p;
	}

	// ══════════════════════════════════════════════════════════════════════
	// LOGIC
	// ══════════════════════════════════════════════════════════════════════

	private void handleGoiDoiVe() {
		String[] d = getSelectedData(); if (d == null) return;
		String maVe = getSelectedMaVe();

		if (laNhom(d)) { warn("Không áp dụng đổi vé đối với vé nhóm."); return; }
		if (tinhGio(d) < 24) {
			warn("Không thể đổi vé.\nYêu cầu đổi phải thực hiện trước giờ tàu ít nhất 24 giờ.\n"
					+ "Hiện còn: " + tinhGio(d) + " giờ.");
			return;
		}
		DoiVeGUI.setVeDuocChon(maVe, d);
		appFrame.showCard("doi-ve");
	}

	private void handleGoiTraVe() {
		String[] d = getSelectedData(); if (d == null) return;
		String maVe = getSelectedMaVe();
		TraVeGUI.setVeDuocChon(maVe, d);
		appFrame.showCard("tra-ve");
	}

	/** Tải lại bảng theo từ khoá (rỗng = hiển thị tất cả) */
	private void reloadTable(String keyword) {
		tableModel.setRowCount(0);
		String kw = keyword.toUpperCase();
		for (Map.Entry<String, String[]> e : FAKE_DATA.entrySet()) {
			String maVe = e.getKey();
			String[] d  = e.getValue();
			if (kw.isEmpty() || maVe.toUpperCase().contains(kw))
				tableModel.addRow(new Object[]{maVe, d[0], d[1], d[2], d[3], d[4], d[5], d[6]});
		}
	}

	// ── Helpers nghiệp vụ ───────────────────────────────────────────────────

	private String getSelectedMaVe() {
		int row = table.getSelectedRow();
		return row < 0 ? null : (String) tableModel.getValueAt(row, 0);
	}

	/** Trả null và hiện cảnh báo nếu chưa chọn hàng */
	private String[] getSelectedData() {
		String maVe = getSelectedMaVe();
		if (maVe == null) { warn("Vui lòng chọn một vé trong danh sách trước."); return null; }
		return FAKE_DATA.get(maVe);
	}

	private long tinhGio(String[] d) {
		try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(d[4], FMT)); }
		catch (Exception ex) { return -1; }
	}

	private boolean laNhom(String[] d) {
		return d[3].toLowerCase().contains("nhóm");
	}

	private void warn(String msg) {
		JOptionPane.showMessageDialog(this, msg, "Không thể thực hiện", JOptionPane.WARNING_MESSAGE);
	}

	// ══════════════════════════════════════════════════════════════════════
	// UI HELPERS
	// ══════════════════════════════════════════════════════════════════════

	/** TextField bọc trong panel bo góc */
	private JPanel wrapTextField(JTextField tf) {
		JPanel p = new JPanel(new BorderLayout()) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
				g2.setColor(new Color(180, 205, 230));
				g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
				g2.dispose();
			}
		};
		p.setOpaque(false);
		p.setBorder(new EmptyBorder(5, 12, 5, 12));
		p.setPreferredSize(new Dimension(200, 36));
		tf.setOpaque(false);
		tf.setBorder(null);
		p.add(tf, BorderLayout.CENTER);
		return p;
	}

	private JButton buildNavyButton(String text, int w, int h) {
		JButton btn = new JButton(text) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed()  ? GuiTheme.NAVY_DARK
						: getModel().isRollover() ? GuiTheme.NAVY_HOVER
						  : GuiTheme.NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(Color.WHITE);
				g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
				FontMetrics fm = g2.getFontMetrics();
				String txt = getText();
				g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
						(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};
		btn.setPreferredSize(new Dimension(w, h));
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return btn;
	}

	private static void fixStretch(JPanel p) {
		p.setAlignmentX(LEFT_ALIGNMENT);
		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 20));
	}
}