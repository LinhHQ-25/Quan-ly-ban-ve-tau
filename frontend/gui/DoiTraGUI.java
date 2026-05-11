package gui;

import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import connect_DB.Connect_DB;

public final class DoiTraGUI extends JPanel {

	private static final Color BORDER = new Color(210, 215, 224);
	private static final Color PRIMARY = new Color(37, 69, 121);
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private final AppFrame appFrame;
	private DefaultTableModel tableModel;
	private JTable table;
	private JTextField txtSearch;
	// Cache dữ liệu vé từ DB
	private final java.util.Map<String, String[]> veCache = new java.util.LinkedHashMap<>();

	public DoiTraGUI(AppFrame appFrame) {
		this.appFrame = appFrame;
		setLayout(new BorderLayout());
		setBackground(GuiTheme.LIGHT_BG);
		JPanel pnlPage = new JPanel();
		pnlPage.setOpaque(false);
		pnlPage.setLayout(new BorderLayout(0, 12));
		pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));
		pnlPage.add(buildNoteBox(),    BorderLayout.NORTH);
		pnlPage.add(buildCenter(),     BorderLayout.CENTER);
		pnlPage.add(buildButtonRow(),  BorderLayout.SOUTH);
		add(pnlPage, BorderLayout.CENTER);
		loadDataFromDB("");
	}
	// UI BUILDERS
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
		Color c = new Color(120, 75, 0);
		Font bold  = GuiTheme.font("Segoe UI", Font.BOLD,  13);
		Font plain = GuiTheme.font("Segoe UI", Font.PLAIN, 13);
		addNote(p, "QUY ĐỊNH ĐỔI / TRẢ VÉ", bold,  c);
		p.add(Box.createVerticalStrut(4));
		addNote(p, "1. ĐỔI VÉ:", plain, c);
		addNote(p, "\t- Chỉ áp dụng vé cá nhân, trước giờ tàu ít nhất 24 giờ. Phí: 30.000 đ / vé", plain, c);
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
		JPanel p = new JPanel(new BorderLayout(0, 8));
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
		btnSearch.addActionListener(e -> loadDataFromDB(txtSearch.getText().trim()));
		txtSearch.addActionListener(e -> loadDataFromDB(txtSearch.getText().trim()));
		gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
		searchBar.add(lb, gbc);
		gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
		searchBar.add(wrapTextField(txtSearch), gbc);
		gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
		searchBar.add(btnSearch, gbc);
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
		table.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
		table.getTableHeader().setBackground(Color.WHITE);
		table.getTableHeader().setForeground(GuiTheme.TEXT);
		table.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));
		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(center);
		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new LineBorder(BORDER, 1, true));
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setPreferredSize(new Dimension(0, 180));
		JPanel tablePanel = new JPanel(new BorderLayout(0, 6));
		tablePanel.setOpaque(false);
		tablePanel.add(buildSectionTitle("Danh sách vé"), BorderLayout.NORTH);
		tablePanel.add(scroll, BorderLayout.CENTER);
		p.add(searchBar, BorderLayout.NORTH);
		p.add(tablePanel, BorderLayout.CENTER);
		return p;
	}

	private JPanel buildButtonRow() {
		// FlowLayout căn phải, khoảng cách chiều ngang giữa các nút là 15px (tùy chỉnh), chiều dọc 10px
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
		p.setOpaque(false);
		p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER)); // Viền mờ ở trên cùng tạo đường phân cách

		// Tạo 2 nút với hàm buildNavyButton đã được làm mới ở trên
		JButton btnTra = buildNavyButton("Trả vé", 130, 38);
		JButton btnDoi = buildNavyButton("Đổi vé", 130, 38);

		// Gắn sự kiện
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
		JLabel lb = new JLabel(title);
		lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
		lb.setForeground(Color.WHITE);
		lb.setOpaque(true);
		lb.setBackground(PRIMARY);
		lb.setBorder(new EmptyBorder(5, 12, 5, 12));
		pnl.add(lb);
		return pnl;
	}
	// DATA
	// DATA
	private void loadDataFromDB(String keyword) {
		// Xóa dữ liệu cũ trên bảng và cache
		tableModel.setRowCount(0);
		veCache.clear();

		// Đảm bảo keyword không bị null (nếu null thì coi như chuỗi rỗng để lấy tất cả)
		String searchKw = (keyword == null) ? "" : keyword.trim();

		String sql =
				"SELECT v.maVe, t.tenTau, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
						"v.loaiVe, ct.thoiGianKhoiHanh, v.giaVe, v.maGhe " +
						"FROM Ve v " +
						"JOIN ChuyenTau ct ON v.maChuyen = ct.maChuyen " +
						"JOIN Tau t ON ct.maTau = t.maTau " +
						"JOIN Ga gDi ON ct.gaDi = gDi.maGa " +
						"JOIN Ga gDen ON ct.gaDen = gDen.maGa " +
						"WHERE v.maVe LIKE ? AND v.trangThaiVe = 'DA_THANH_TOAN' " +
						"ORDER BY ct.thoiGianKhoiHanh DESC"; // Sắp xếp giảm dần để vé mới mua lên đầu (Tùy chọn)

		try (Connection conn = Connect_DB.getInstance().getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {

			// Nếu searchKw là "", nó sẽ thành "%%" -> Lấy toàn bộ vé
			stmt.setString(1, "%" + searchKw.toUpperCase() + "%");

			try (ResultSet rs = stmt.executeQuery()) {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
				boolean found = false;

				while (rs.next()) {
					found = true;
					String maVe    = rs.getString("maVe");
					String tenTau  = rs.getString("tenTau");
					String gaDi    = rs.getString("gaDi");
					String gaDen   = rs.getString("gaDen");
					String loaiVe  = rs.getString("loaiVe");
					String maGhe   = rs.getString("maGhe");
					String giaVe   = rs.getString("giaVe");

					Timestamp ts = rs.getTimestamp("thoiGianKhoiHanh");
					String ngayGio = ts != null ? sdf.format(ts) : "";
					String soLuong = "1";

					veCache.put(maVe, new String[]{tenTau, gaDi, gaDen, loaiVe, ngayGio, soLuong, maGhe, giaVe});
					tableModel.addRow(new Object[]{maVe, tenTau, gaDi, gaDen, loaiVe, ngayGio, soLuong, maGhe});
				}

				// Chỉ hiển thị popup cảnh báo nếu người dùng có nhập từ khóa tìm kiếm
				if (!found && !searchKw.isEmpty()) {
					JOptionPane.showMessageDialog(this, "Không tìm thấy vé nào phù hợp hoặc vé chưa được thanh toán!",
							"Thông báo", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Lỗi truy vấn cơ sở dữ liệu: " + e.getMessage(),
					"Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}
	// LOGIC
	private void handleGoiDoiVe() {
		String[] d = getSelectedData(); if (d == null) return;
		String maVe = getSelectedMaVe();
		if (laNhom(d)) { warn("Không áp dụng đổi vé đối với vé nhóm."); return; }
		if (tinhGio(d) < 24) {
			warn("Không thể đổi vé.\nYêu cầu đổi phải trước giờ tàu ít nhất 24 giờ.\nHiện còn: " + tinhGio(d) + " giờ.");
			return;
		}
		DoiVeGUI.setVeDuocChon(maVe, d);
		appFrame.showCard("doi-ve");
	}
	private void handleGoiTraVe() {
		String[] d = getSelectedData(); if (d == null) return;
		TraVeGUI.setVeDuocChon(getSelectedMaVe(), d);
		appFrame.showCard("tra-ve");
	}
	private String getSelectedMaVe() {
		int row = table.getSelectedRow();
		return row < 0 ? null : (String) tableModel.getValueAt(row, 0);
	}
	private String[] getSelectedData() {
		String maVe = getSelectedMaVe();
		if (maVe == null) { warn("Vui lòng chọn một vé trong bảng danh sách trước!"); return null; }
		return veCache.get(maVe);
	}
	private long tinhGio(String[] d) {
		try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(d[4], FMT)); }
		catch (Exception ex) { return -1; }
	}
	private boolean laNhom(String[] d) { return d[3].toLowerCase().contains("nhóm"); }
	private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Không thể thực hiện", JOptionPane.WARNING_MESSAGE); }
	// UI HELPERS
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
	// UI HELPERS
	// ... (Giữ nguyên hàm wrapTextField) ...

	private JButton buildNavyButton(String text, int w, int h) {
		JButton btn = new JButton(text) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
						: getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(Color.WHITE);

				// Cập nhật font size lên 14 theo mẫu
				g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

				FontMetrics fm = g2.getFontMetrics();
				// Lấy chuỗi text ra biến riêng theo mẫu buildTimButton
				String txt = getText();
				g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
						(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};
		// Giữ lại tham số w, h để dùng chung cho các nút có kích thước khác nhau
		btn.setPreferredSize(new Dimension(w, h));
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return btn;
	}
}