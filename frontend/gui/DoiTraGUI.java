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

		// SỬA: Giảm khoảng cách dọc từ 12 xuống 4 để nút sát phần bảng hơn
		pnlPage.setLayout(new BorderLayout(0, 4));

		// SỬA: Đổi tham số thứ 3 (khoảng cách đáy) từ GuiTheme.PAGE_PAD_BOTTOM thành 0
		pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, 0, GuiTheme.PAGE_PAD_LEFT));

		pnlPage.add(buildNoteBox(),    BorderLayout.NORTH);
		pnlPage.add(buildCenter(),     BorderLayout.CENTER);
		pnlPage.add(buildButtonRow(),  BorderLayout.SOUTH);
		add(pnlPage, BorderLayout.CENTER);
		loadDataFromDB("");
	}

	// Thay thế hàm refresh() cũ trong DoiTraGUI.java bằng đoạn này:
	public void refresh() {
		// 1. Xóa nội dung người dùng đã nhập ở ô tìm kiếm
		if (txtSearch != null) {
			txtSearch.setText("");
		}

		// 2. Tải lại toàn bộ dữ liệu mặc định ban đầu từ Database
		loadDataFromDB("");
	}

	// UI BUILDERS
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
		p.setBorder(new EmptyBorder(6, 10, 6, 10));
		Color c = new Color(0, 0, 0);
		Font bold  = GuiTheme.font("Segoe UI", Font.BOLD,  14);
		Font plain = GuiTheme.font("Segoe UI", Font.PLAIN, 14);
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
		btnSearch.setIcon(GuiIcons.loadIcon(DoiTraGUI.class, "/Images/traCuu.png", 16, 16));
		btnSearch.setPreferredSize(new Dimension(120, 32));
		btnSearch.addActionListener(e -> loadDataFromDB(txtSearch.getText().trim()));
		txtSearch.addActionListener(e -> loadDataFromDB(txtSearch.getText().trim()));

		gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
		searchBar.add(lb, gbc);
		gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
		searchBar.add(wrapTextField(txtSearch), gbc);
		gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
		searchBar.add(btnSearch, gbc);

		tableModel = new DefaultTableModel(
				new Object[]{"Mã vé","Mã Chuyến","Ga đi","Ga đến","Loại vé","Chiều Vé","Ngày/Giờ KH","Ngày/Giờ Mua Vé","SL","Ghế"}, 0) {
			public boolean isCellEditable(int r, int c) { return false; }
		};
		table = new JTable(tableModel);

		// Dòng này chính là tính năng cho phép click vào tiêu đề cột để Sắp xếp (Tăng/Giảm dần)
		table.setAutoCreateRowSorter(true);

		table.setRowHeight(28);
		table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

		// Căn lề trái + cố định độ rộng cột cân đối
		javax.swing.table.DefaultTableCellRenderer _leftR = new javax.swing.table.DefaultTableCellRenderer();
		_leftR.setHorizontalAlignment(SwingConstants.LEFT);
		_leftR.setBorder(new EmptyBorder(0, 8, 0, 8));
		for (int _i = 0; _i < table.getColumnCount(); _i++) {
			table.getColumnModel().getColumn(_i).setCellRenderer(_leftR);
		}
		// Mã vé | Mã Chuyến | Ga đi | Ga đến | Loại vé | Chiều | Ngày/Giờ KH | Ngày/Giờ Mua Vé | SL | Ghế
		int[] _widths = {100, 100, 100, 100, 85, 75, 120, 120, 45, 90};
		for (int _i = 0; _i < _widths.length && _i < table.getColumnCount(); _i++) {
			table.getColumnModel().getColumn(_i).setPreferredWidth(_widths[_i]);
		}
		table.getTableHeader().setReorderingAllowed(false);
		table.setForeground(GuiTheme.TEXT);
		table.setGridColor(new Color(230, 233, 238));
		table.setSelectionBackground(new Color(207, 222, 243));
		table.setSelectionForeground(GuiTheme.TEXT);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// --- CẤU HÌNH HEADER (TIÊU ĐỀ CỘT) ---
		table.getTableHeader().setReorderingAllowed(false); // Không cho kéo thả đổi vị trí cột
		table.getTableHeader().setResizingAllowed(false);   // THÊM DÒNG NÀY: Không cho kéo dãn độ rộng cột
		table.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
		table.getTableHeader().setBackground(Color.WHITE);
		table.getTableHeader().setForeground(GuiTheme.TEXT);
		table.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));
		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(center);
		table.getColumnModel().getColumn(5).setCellRenderer(center);
		table.getColumnModel().getColumn(8).setCellRenderer(center);

		// ---> THÊM ĐÚNG 1 DÒNG NÀY ĐỂ ẨN CỘT SỐ LƯỢNG (Cột số 8) <---
		table.getColumnModel().removeColumn(table.getColumnModel().getColumn(8));

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
		// SỬA: Giảm vgap từ 10 xuống còn 4 (tham số thứ 3) giúp panel mỏng lại và sát đáy hơn
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4));
		p.setOpaque(false);
		p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

		JButton btnTra = buildNavyButton("Trả vé", 130, 38);
		JButton btnDoi = buildNavyButton("Đổi vé", 130, 38);

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

	// =========================================================
	// DATA
	// =========================================================
	private void loadDataFromDB(String keyword) {
		tableModel.setRowCount(0);
		veCache.clear();

		String searchKw = (keyword == null) ? "" : keyword.trim();

		// --- LẤY CẢ CHIỀU ĐI VÀ CHIỀU VỀ ---
		String sql =
				"SELECT v.maVe, ct.maChuyenTau AS maChuyenTau, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
						"v.loaiVe, dt.thoiGianKhoiHanh, v.giaVe, v.maGhe, g.soGhe, kh.hoTenKH, " +
						"dt.maGaDi, dt.maGaDen, v.ngayMua, " +
						"1 AS soLuongVe " +
						"FROM Ve v " +
						"JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
						"JOIN ChuyenTau ct ON dt.maChuyenTau = ct.maChuyenTau " +
						"JOIN Tau t ON ct.maTau = t.maTau " +
						"JOIN Ga gDi ON dt.maGaDi = gDi.maGa " +
						"JOIN Ga gDen ON dt.maGaDen = gDen.maGa " +
						"JOIN Ghe g ON v.maGhe = g.maGhe " +  // JOIN bảng Ghế
						"LEFT JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " + // JOIN Hóa Đơn
						"LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " + // JOIN Khách Hàng
						"WHERE v.maVe LIKE ? AND v.trangThaiVe = N'Đã thanh toán' " +
						"AND (dt.maGaDi = 'DIEUTRI' OR dt.maGaDen = 'DIEUTRI') " +
						"ORDER BY dt.thoiGianKhoiHanh DESC";

		try (Connection conn = Connect_DB.getInstance().getConnection();
		     PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, "%" + searchKw.toUpperCase() + "%");

			try (ResultSet rs = stmt.executeQuery()) {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
				boolean found = false;

				while (rs.next()) {
					String maVe = rs.getString("maVe");
					if (veCache.containsKey(maVe)) continue;

					found = true;
					String maChuyenTau = rs.getString("maChuyenTau");
					String gaDi        = rs.getString("gaDi");
					String gaDen       = rs.getString("gaDen");
					String rawLoaiVe   = rs.getString("loaiVe");

					String maGaDiRaw = rs.getString("maGaDi");
					String chieuVe;
					if ("MOT_CHIEU".equalsIgnoreCase(rawLoaiVe)) {
						chieuVe = "Chiều đi"; // vé 1 chiều luôn là chiều đi
					} else {
						chieuVe = "DIEUTRI".equals(maGaDiRaw) ? "Chiều đi" : "Chiều về";
					}

					String maGhe   = rs.getString("maGhe");
					String soGhe   = rs.getString("soGhe"); // Lấy thêm số ghế (Vị trí)
					String tenKH   = rs.getString("hoTenKH"); // Lấy thêm tên khách hàng
					if (tenKH == null) tenKH = "Khách vãng lai"; // Fallback nếu rỗng

					String giaVe  = String.valueOf(rs.getLong("giaVe"));
					Timestamp ts = rs.getTimestamp("thoiGianKhoiHanh");
					String ngayGio = ts != null ? sdf.format(ts) : "";
					Timestamp tsMua = rs.getTimestamp("ngayMua");
					String ngayMua = tsMua != null ? sdf.format(tsMua) : "";
					String soLuong = String.valueOf(rs.getInt("soLuongVe"));

					// SỬA: Truyền thêm soGhe và tenKH vào cuối mảng veCache (index 9 và 10)
					veCache.put(maVe, new String[]{maChuyenTau, gaDi, gaDen, rawLoaiVe, chieuVe, ngayGio, soLuong, maGhe, giaVe, soGhe, tenKH});

					// Hiển thị lên bảng (Vẫn giữ nguyên số lượng cột trên UI, 2 cột mới chỉ lưu ẩn dưới cache)
					tableModel.addRow(new Object[]{maVe, maChuyenTau, gaDi, gaDen, (rawLoaiVe.equals("KHU_HOI") ? "Khứ hồi" : "Một chiều"), chieuVe, ngayGio, ngayMua, soLuong, maGhe});
				}

				if (!found && !searchKw.isEmpty()) {
					JOptionPane.showMessageDialog(this, "Không tìm thấy vé nào phù hợp hoặc vé chưa được thanh toán!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Lỗi truy vấn cơ sở dữ liệu: " + e.getMessage(),
					"Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	// =========================================================
	// LOGIC
	// =========================================================
	private void handleGoiDoiVe() {
		String[] d = getSelectedData();
		if (d == null) return;
		String maVe = getSelectedMaVe();

		if (laNhom(d)) {
			warn("Vé đổi không hợp lệ");
			return;
		}

		if (tinhGio(d) < 24) {
			warn("Vé đổi không hợp lệ");
			return;
		}
		DoiVeGUI.setVeDuocChon(maVe, d);
		appFrame.showCard("doi-ve");
	}

	private void handleGoiTraVe() {
		String[] d = getSelectedData();
		if (d == null) return;
		String maVe = getSelectedMaVe();

		// ĐIỀU KIỆN TRẢ VÉ: Cá nhân >= 12h, Nhóm >= 24h
		long gio = tinhGio(d);
		if (laNhom(d)) {
			if (gio < 24) {
				warn("Không đủ điều kiện trả vé!");
				return;
			}
		} else {
			if (gio < 12) {
				warn("Không đủ điều kiện trả vé!");
				return;
			}
		}

		TraVeGUI.setVeDuocChon(maVe, d);
		appFrame.showCard("tra-ve");
	}

	private String getSelectedMaVe() {
		int row = table.getSelectedRow();
		if(row >= 0) {
			row = table.convertRowIndexToModel(row);
			return (String) tableModel.getValueAt(row, 0);
		}
		return null;
	}

	private String[] getSelectedData() {
		String maVe = getSelectedMaVe();
		if (maVe == null) { warn("Vui lòng chọn một vé trong bảng danh sách trước!"); return null; }
		return veCache.get(maVe);
	}

	private long tinhGio(String[] d) {
		try {
			String ngayGio = d[5];
			LocalDateTime thoiGian;
			try {
				thoiGian = LocalDateTime.parse(ngayGio, FMT);
			} catch (Exception ex1) {
				// Thử thêm format có giây
				DateTimeFormatter fmtWithSec = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
				thoiGian = LocalDateTime.parse(ngayGio, fmtWithSec);
			}
			return ChronoUnit.HOURS.between(LocalDateTime.now(), thoiGian);
		} catch (Exception ex) {
			ex.printStackTrace();
			return Long.MAX_VALUE; // Không parse được → không chặn, để nghiệp vụ khác xử lý
		}
	}

	private boolean laNhom(String[] d) {
		try {
			return Integer.parseInt(d[6]) > 1;  // > 1 tức là từ 2 vé trở lên trong hóa đơn
		} catch (Exception e) {
			return false;
		}
	}

	private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Không thể thực hiện", JOptionPane.WARNING_MESSAGE); }

	// =========================================================
	// UI HELPERS
	// =========================================================
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

			@Override
			protected void paintComponent(Graphics g) {

				Graphics2D g2 = (Graphics2D) g.create();

				g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON
				);

				// Background
				g2.setColor(
						getModel().isPressed()
								? GuiTheme.NAVY_DARK
								: getModel().isRollover()
								  ? GuiTheme.NAVY_HOVER
								  : GuiTheme.NAVY
				);

				g2.fillRoundRect(
						0,
						0,
						getWidth(),
						getHeight(),
						12,
						12
				);

				// =========================
				// DRAW ICON + TEXT
				// =========================

				Icon icon = getIcon();

				Font font = GuiTheme.font(
						"Segoe UI",
						Font.PLAIN,
						14
				);

				g2.setFont(font);

				FontMetrics fm = g2.getFontMetrics();

				int iconTextGap = 8;

				int textWidth =
						fm.stringWidth(getText());

				int iconWidth =
						(icon != null)
								? icon.getIconWidth()
								: 0;

				int totalWidth =
						iconWidth +
								(icon != null ? iconTextGap : 0) +
								textWidth;

				int startX =
						(getWidth() - totalWidth) / 2;

				// ===== DRAW ICON =====

				if (icon != null) {

					int iconY =
							(getHeight() - icon.getIconHeight()) / 2;

					icon.paintIcon(
							this,
							g2,
							startX,
							iconY
					);

					startX += iconWidth + iconTextGap;
				}

				// ===== DRAW TEXT =====

				g2.setColor(Color.WHITE);

				int textY =
						(getHeight()
								+ fm.getAscent()
								- fm.getDescent()) / 2;

				g2.drawString(
						getText(),
						startX,
						textY
				);

				g2.dispose();
			}
		};

		btn.setPreferredSize(new Dimension(w, h));

		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);

		btn.setCursor(
				new Cursor(Cursor.HAND_CURSOR)
		);

		return btn;
	}
}