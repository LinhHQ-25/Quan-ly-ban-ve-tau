package gui;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import dao.DonDoiTraVeDAO;

public final class DoiTraGUI extends JPanel {

	private static final Color BORDER  = new Color(210, 215, 224);
	private static final Color PRIMARY = new Color(37, 69, 121);
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final AppFrame appFrame;
	private final DonDoiTraVeDAO donDoiTraVeDAO = new DonDoiTraVeDAO();

	private DefaultTableModel tableModel;
	private JTable table;
	private JTextField txtSearch;

	/** Cache vé – layout xem DonDoiTraVeDAO#timKiemVe Javadoc */
	private final Map<String, String[]> veCache = new java.util.LinkedHashMap<>();

	// =========================================================
	// CONSTRUCTOR
	// =========================================================
	public DoiTraGUI(AppFrame appFrame) {
		this.appFrame = appFrame;
		setLayout(new BorderLayout());
		setBackground(GuiTheme.LIGHT_BG);

		JPanel pnlPage = new JPanel(new BorderLayout(0, 4));
		pnlPage.setOpaque(false);
		pnlPage.setBorder(new EmptyBorder(0, 0, 0, 0));

		pnlPage.add(buildNoteBox(),   BorderLayout.NORTH);
		pnlPage.add(buildCenter(),    BorderLayout.CENTER);
		pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);
		add(pnlPage, BorderLayout.CENTER);

		loadData("");
	}

	// =========================================================
	// PUBLIC API
	// =========================================================
	public void refresh() {
		if (txtSearch != null) txtSearch.setText("");
		loadData("");
	}

	// =========================================================
	// DATA – GỌI QUA DAO
	// =========================================================
	private void loadData(String keyword) {
		tableModel.setRowCount(0);
		veCache.clear();

		Map<String, String[]> data = donDoiTraVeDAO.timKiemVe(keyword);
		veCache.putAll(data);

		for (Map.Entry<String, String[]> entry : data.entrySet()) {
			String   maVe = entry.getKey();
			String[] d    = entry.getValue();
			tableModel.addRow(new Object[]{
					maVe,
					d[0],  // maChuyenTau
					d[1],  // gaDi
					d[2],  // gaDen
					"KHU_HOI".equalsIgnoreCase(d[3]) ? "Khứ hồi" : "Một chiều",
					d[4],  // chieuVe
					d[5],  // ngayGioKH
					d[11], // ngayMua
					d[6],  // soLuong (cột ẩn – index 8, bị removeColumn bên dưới)
					d[7]   // maGhe
			});
		}

		if (data.isEmpty() && keyword != null && !keyword.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"Không tìm thấy vé nào phù hợp hoặc vé chưa được thanh toán!",
					"Thông báo", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// =========================================================
	// LOGIC
	// =========================================================
	private void handleGoiDoiVe() {
		String[] d = getSelectedData();
		if (d == null) return;

		if (laNhom(d)) { warn("Vé đổi không hợp lệ"); return; }
		if (tinhGio(d) < 24) { warn("Vé đổi không hợp lệ"); return; }

		DoiVeGUI.setVeDuocChon(getSelectedMaVe(), d);
		appFrame.showCard("doi-ve");
	}

	private void handleGoiTraVe() {
		String[] d = getSelectedData();
		if (d == null) return;

		long gio = tinhGio(d);
		if (laNhom(d)) {
			if (gio < 24) { warn("Không đủ điều kiện trả vé!"); return; }
		} else {
			if (gio < 12) { warn("Không đủ điều kiện trả vé!"); return; }
		}

		TraVeGUI.setVeDuocChon(getSelectedMaVe(), d);
		appFrame.showCard("tra-ve");
	}

	private String getSelectedMaVe() {
		int row = table.getSelectedRow();
		if (row < 0) return null;
		row = table.convertRowIndexToModel(row);
		return (String) tableModel.getValueAt(row, 0);
	}

	private String[] getSelectedData() {
		String maVe = getSelectedMaVe();
		if (maVe == null) { warn("Vui lòng chọn một vé trong bảng danh sách trước!"); return null; }
		return veCache.get(maVe);
	}

	private long tinhGio(String[] d) {
		try {
			LocalDateTime thoiGian;
			try {
				thoiGian = LocalDateTime.parse(d[5], FMT);
			} catch (Exception ex1) {
				thoiGian = LocalDateTime.parse(d[5],
						DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
			}
			return ChronoUnit.HOURS.between(LocalDateTime.now(), thoiGian);
		} catch (Exception ex) {
			ex.printStackTrace();
			return Long.MAX_VALUE;
		}
	}

	private boolean laNhom(String[] d) {
		try { return Integer.parseInt(d[6]) > 1; }
		catch (Exception e) { return false; }
	}

	private void warn(String msg) {
		JOptionPane.showMessageDialog(this, msg, "Không thể thực hiện", JOptionPane.WARNING_MESSAGE);
	}

	// =========================================================
	// UI BUILDERS
	// =========================================================
	private JPanel buildNoteBox() {
		JPanel p = new JPanel() {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(220, 245, 255));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.setColor(new Color(52, 123, 255));
				g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
				g2.dispose();
			}
		};
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(new EmptyBorder(4, 6, 4, 6));

		Color c    = Color.BLACK;
		Font bold  = GuiTheme.font("Segoe UI", Font.BOLD,  14);
		Font plain = GuiTheme.font("Segoe UI", Font.PLAIN, 14);

		addNote(p, "QUY ĐỊNH ĐỔI / TRẢ VÉ", bold, c);
		p.add(Box.createVerticalStrut(4));
		addNote(p, "1. ĐỔI VÉ:", plain, c);
		addNote(p, "\t- Chỉ áp dụng vé cá nhân, trước giờ tàu ít nhất 24 giờ. Phí: 30.000đ/vé", plain, c);
		addNote(p, "2. TRẢ VÉ:", plain, c);
		addNote(p, "\t- Vé cá nhân: ≥ 48h (10%), 12–48h (20%), < 12h: không hoàn", plain, c);
		addNote(p, "\t- Vé nhóm:    ≥ 72h (20%), 24–72h (30%), < 24h: không hoàn", plain, c);
		return p;
	}

	private void addNote(JPanel p, String text, Font f, Color c) {
		JLabel lb = new JLabel(text);
		lb.setFont(f); lb.setForeground(c); lb.setAlignmentX(LEFT_ALIGNMENT);
		p.add(lb);
	}

	private JPanel buildCenter() {
		JPanel p = new JPanel(new BorderLayout(0, 4));
		p.setOpaque(false);

		// Search bar
		JPanel searchBar = new JPanel(new GridBagLayout());
		searchBar.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0; gbc.insets = new Insets(0, 0, 0, 10);

		JLabel lb = new JLabel("Nhập mã vé cần đổi/trả:");
		lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		lb.setForeground(GuiTheme.TEXT);

		txtSearch = new JTextField();
		txtSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

		JButton btnSearch = buildNavyButton("Tìm kiếm", 130, 38);
		btnSearch.setIcon(GuiIcons.loadIcon(DoiTraGUI.class, "/Images/traCuu.png", 16, 16));
		btnSearch.setPreferredSize(new Dimension(120, 32));
		btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
		txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));

		gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
		searchBar.add(lb, gbc);
		gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
		searchBar.add(wrapTextField(txtSearch), gbc);
		gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
		searchBar.add(btnSearch, gbc);

		// Table – 10 cột, cột SL (index 8) sẽ bị ẩn
		tableModel = new DefaultTableModel(
				new Object[]{"Mã vé","Mã Chuyến","Ga đi","Ga đến","Loại vé","Chiều Vé",
				             "Ngày/Giờ KH","Ngày/Giờ Mua Vé","SL","Ghế"}, 0) {
			public boolean isCellEditable(int r, int c) { return false; }
		};
		table = new JTable(tableModel);
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(28);
		table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

		DefaultTableCellRenderer leftR = new DefaultTableCellRenderer();
		leftR.setHorizontalAlignment(SwingConstants.LEFT);
		leftR.setBorder(new EmptyBorder(0, 8, 0, 8));
		for (int i = 0; i < table.getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setCellRenderer(leftR);

		int[] widths = {100, 100, 100, 100, 85, 75, 120, 120, 45, 90};
		for (int i = 0; i < widths.length && i < table.getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
		table.getTableHeader().setBackground(Color.WHITE);
		table.getTableHeader().setForeground(GuiTheme.TEXT);
		table.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));

		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(center);
		table.getColumnModel().getColumn(5).setCellRenderer(center);
		table.getColumnModel().getColumn(8).setCellRenderer(center);

		// Ẩn cột SL (index 8)
		table.getColumnModel().removeColumn(table.getColumnModel().getColumn(8));

		table.setForeground(GuiTheme.TEXT);
		table.setGridColor(new Color(230, 233, 238));
		table.setSelectionBackground(new Color(207, 222, 243));
		table.setSelectionForeground(GuiTheme.TEXT);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new LineBorder(BORDER, 1, true));
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setPreferredSize(new Dimension(0, 180));

		JPanel tablePanel = new JPanel(new BorderLayout(0, 6));
		tablePanel.setOpaque(false);
		tablePanel.add(buildSectionTitle("Danh sách vé"), BorderLayout.NORTH);
		tablePanel.add(scroll, BorderLayout.CENTER);

		p.add(searchBar,  BorderLayout.NORTH);
		p.add(tablePanel, BorderLayout.CENTER);
		return p;
	}

	private JPanel buildButtonRow() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4));
		p.setOpaque(false);
		p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

		JButton btnTra = buildNavyButton("Trả vé", 0, 38);
		JButton btnDoi = buildNavyButton("Đổi vé", 0, 38);
		btnTra.addActionListener(e -> handleGoiTraVe());
		btnDoi.addActionListener(e -> handleGoiDoiVe());

		p.add(btnTra);
		p.add(btnDoi);
		return p;
	}

	private JPanel buildSectionTitle(String title) {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pnl.setOpaque(false);
		pnl.setBorder(new EmptyBorder(4, 0, 4, 0));
		JLabel lb = new JLabel(title, SwingConstants.CENTER);
		lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
		lb.setForeground(Color.WHITE);
		lb.setOpaque(true);
		lb.setBackground(PRIMARY);
		lb.setPreferredSize(new Dimension(220, 26));
		lb.setBorder(null);
		pnl.add(lb);
		return pnl;
	}

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
		tf.setOpaque(false); tf.setBorder(null);
		p.add(tf, BorderLayout.CENTER);
		return p;
	}

	private JButton buildNavyButton(String text, int w, int h) {
		JButton btn = new JButton(text) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(
						getModel().isPressed()    ? GuiTheme.NAVY_DARK
						: getModel().isRollover() ? GuiTheme.NAVY_HOVER
						: GuiTheme.NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

				Icon icon = getIcon();
				Font font = GuiTheme.font("Segoe UI", Font.PLAIN, 14);
				g2.setFont(font);
				FontMetrics fm = g2.getFontMetrics();

				int gap        = 8;
				int textWidth  = fm.stringWidth(getText());
				int iconWidth  = (icon != null) ? icon.getIconWidth() : 0;
				int totalWidth = iconWidth + (icon != null ? gap : 0) + textWidth;
				int startX     = (getWidth() - totalWidth) / 2;

				if (icon != null) {
					icon.paintIcon(this, g2, startX, (getHeight() - icon.getIconHeight()) / 2);
					startX += iconWidth + gap;
				}

				g2.setColor(Color.WHITE);
				g2.drawString(getText(), startX, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};

		if (w > 0) btn.setPreferredSize(new Dimension(w, h));
		else        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, h));

		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return btn;
	}
}