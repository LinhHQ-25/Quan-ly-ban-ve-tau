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

final class DoiTraGUI extends JPanel {

	private static final Color BORDER      = new Color(210, 215, 224);
	private static final Color PRIMARY     = new Color(71, 71, 156);
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	// ── PHÍ THEO QUY ĐỊNH ──────────────────────────────────────────────────
	// Đổi vé cá nhân: 30.000đ cố định, trước >= 24h. Vé nhóm: không được đổi.
	// Trả vé cá nhân: >= 48h → 10%; 12–48h → 20%; < 12h → không hoàn
	// Trả vé nhóm  : >= 72h → 20%; 24–72h → 30%; < 24h → không hoàn
	private static final long DOI_PHI_CO_DINH = 30_000L;

	// ── DỮ LIỆU GIẢ ────────────────────────────────────────────────────────
	// maVe → { chuyenTau, gaDi, gaDen, loaiVe, ngayGioKH (dd/MM/yyyy HH:mm), soLuong, ghe, giaTien }
	// Ngày khởi hành được đặt tương đối so với thời điểm chạy để dễ test
	private static final Map<String, String[]> FAKE_DATA = new LinkedHashMap<>();
	static {
		// Cá nhân – khởi hành sau ~30 giờ (hợp lệ đổi & trả >=48h không)
		LocalDateTime kh1 = LocalDateTime.now().plusHours(30);
		FAKE_DATA.put("27CT30", new String[]{
				"SE5", "Diêu Trì", "Sài Gòn", "Vé cá nhân",
				kh1.format(FMT), "01", "B05", "450000"
		});
		// Cá nhân – khởi hành sau ~60 giờ (hợp lệ đổi & trả 10%)
		LocalDateTime kh2 = LocalDateTime.now().plusHours(60);
		FAKE_DATA.put("13HN05", new String[]{
				"SE1", "Hà Nội", "Đà Nẵng", "Vé cá nhân",
				kh2.format(FMT), "01", "A12", "320000"
		});
		// Nhóm – khởi hành sau ~80 giờ (hợp lệ trả 20%)
		LocalDateTime kh3 = LocalDateTime.now().plusHours(80);
		FAKE_DATA.put("08DN12", new String[]{
				"SE3", "Đà Nẵng", "Nha Trang", "Vé nhóm",
				kh3.format(FMT), "05", "C03", "610000"
		});
		// Cá nhân – khởi hành sau ~10 giờ (quá hạn, không hoàn)
		LocalDateTime kh4 = LocalDateTime.now().plusHours(10);
		FAKE_DATA.put("21NT07", new String[]{
				"TN2", "Nha Trang", "TP.HCM", "Vé cá nhân",
				kh4.format(FMT), "01", "D08", "275000"
		});
	}

	// ── STATE ───────────────────────────────────────────────────────────────
	private DefaultTableModel tableModel;
	private JTable table;
	private JTextField txtSearch;

	// Form đổi
	private JComboBox<String> cbChuyen;
	private JComboBox<String> cbGhe;
	private JTextField tfNgayDoi, tfLePhi;

	// Form trả
	private JComboBox<String> cbLyDo;
	private JTextField tfPhiTra, tfHoanLai, tfTrangThaiTra;

	// Panel chứa form
	private JPanel pnlDoi, pnlTra;

	public DoiTraGUI() {
		setLayout(new BorderLayout());
		setBackground(GuiTheme.LIGHT_BG);

		JPanel pnlPage = new JPanel();
		pnlPage.setLayout(new BoxLayout(pnlPage, BoxLayout.Y_AXIS));
		pnlPage.setOpaque(false);
		pnlPage.setBorder(new EmptyBorder(
				GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT,
				GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

		pnlPage.add(buildHeader("ĐỔI / TRẢ VÉ", "Dùng để đổi hoặc trả vé đã đặt."));
		pnlPage.add(Box.createVerticalStrut(12));
		pnlPage.add(buildSearchBar());
		pnlPage.add(Box.createVerticalStrut(12));
		pnlPage.add(buildTicketTable());
		pnlPage.add(Box.createVerticalStrut(12));
		pnlPage.add(buildActionPanel());

		add(pnlPage, BorderLayout.CENTER);
		reloadTable("");
	}

	// ── HEADER ──────────────────────────────────────────────────────────────
	private JPanel buildHeader(String title, String subtitle) {
		JPanel pnl = new JPanel(new BorderLayout(0, 6));
		pnl.setOpaque(false);
		JLabel lbTitle = new JLabel(title);
		lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, GuiTheme.PAGE_TITLE_SIZE));
		lbTitle.setForeground(GuiTheme.TEXT);
		JLabel lbSub = new JLabel(subtitle);
		lbSub.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, GuiTheme.PAGE_SUBTITLE_SIZE));
		lbSub.setForeground(GuiTheme.SUB_TEXT);
		pnl.add(lbTitle, BorderLayout.NORTH);
		pnl.add(lbSub,   BorderLayout.SOUTH);
		return pnl;
	}

	// ── SEARCH BAR ──────────────────────────────────────────────────────────
	private JPanel buildSearchBar() {
		JPanel pnl = new JPanel(new GridBagLayout());
		pnl.setOpaque(false);

		GridBagConstraints g = new GridBagConstraints();
		g.insets = new Insets(0, 0, 0, 10);
		g.gridy = 0;

		JLabel lbLabel = new JLabel("Nhập mã vé cần đổi/trả:");
		lbLabel.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		lbLabel.setForeground(GuiTheme.TEXT);

		txtSearch = new JTextField();
		txtSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

		JButton btn = buildActionButton("Tìm kiếm");
		btn.addActionListener(e -> reloadTable(txtSearch.getText().trim()));
		txtSearch.addActionListener(e -> reloadTable(txtSearch.getText().trim()));

		// Label
		g.gridx = 0;
		g.weightx = 0;
		g.fill = GridBagConstraints.NONE;
		pnl.add(lbLabel, g);

		// TextField (co giãn)
		g.gridx = 1;
		g.weightx = 1;
		g.fill = GridBagConstraints.HORIZONTAL;
		pnl.add(wrapFieldFluid(txtSearch), g);

		// Button
		g.gridx = 2;
		g.weightx = 0;
		g.fill = GridBagConstraints.NONE;
		pnl.add(btn, g);

		return pnl;
	}

	private JPanel wrapFieldFluid(JTextField tf) {
		JPanel p = new JPanel(new BorderLayout()) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int arc = Math.min(getHeight(), 20); // bo góc theo chiều cao

				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

				g2.setColor(new Color(180, 205, 230));
				g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);

				g2.dispose();
			}
		};

		p.setOpaque(false);
		p.setBorder(new EmptyBorder(6, 12, 6, 12));
		p.setMinimumSize(new Dimension(100, 36));
		p.setPreferredSize(new Dimension(200, 36));

		tf.setOpaque(false);
		tf.setBorder(null);

		p.add(tf, BorderLayout.CENTER);
		return p;
	}

	// ── TABLE ───────────────────────────────────────────────────────────────
	private JPanel buildTicketTable() {
		JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
		pnlOuter.setOpaque(false);
		pnlOuter.add(buildSectionTitle("Danh sách vé"), BorderLayout.NORTH);

		tableModel = new DefaultTableModel(
				new Object[]{"Mã vé","Chuyến tàu","Ga đi","Ga đến","Loại vé","Ngày/Giờ KH","SL","Ghế"}, 0) {
			public boolean isCellEditable(int r, int c) { return false; }
		};

		table = new JTable(tableModel);
		table.setRowHeight(28);
		table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
		table.setForeground(GuiTheme.TEXT);
		table.setGridColor(new Color(230, 233, 238));
		table.setSelectionBackground(new Color(207, 209, 214));
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

		table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting()) fillFormFromSelection();
		});

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new LineBorder(BORDER, 1, true));
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setPreferredSize(new Dimension(10000, 130));

		JPanel pnlWrap = new JPanel(new BorderLayout());
		pnlWrap.setOpaque(false);
		pnlWrap.add(scroll, BorderLayout.CENTER);
		pnlOuter.add(pnlWrap, BorderLayout.CENTER);
		return pnlOuter;
	}

	// ── ACTION PANEL ────────────────────────────────────────────────────────
	private JPanel buildActionPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setOpaque(false);

		JRadioButton rdoDoi = buildRadio("Đổi vé");
		JRadioButton rdoTra = buildRadio("Trả vé");
		ButtonGroup group = new ButtonGroup();
		group.add(rdoDoi);
		group.add(rdoTra);
		rdoDoi.setSelected(true);

		JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
		pnlRadio.setOpaque(false);
		pnlRadio.add(rdoDoi);
		pnlRadio.add(rdoTra);
		pnl.add(pnlRadio);
		pnl.add(Box.createVerticalStrut(8));

		pnlDoi = buildFormDoi();
		pnlTra = buildFormTra();
		pnlTra.setVisible(false);

		rdoDoi.addActionListener(e -> { pnlDoi.setVisible(true);  pnlTra.setVisible(false); fillFormFromSelection(); });
		rdoTra.addActionListener(e -> { pnlDoi.setVisible(false); pnlTra.setVisible(true);  fillFormFromSelection(); });

		pnl.add(pnlDoi);
		pnl.add(pnlTra);
		return pnl;
	}

	// ── FORM ĐỔI ─────────────────────────────────────────────────────────────
	// Quy định: chỉ vé cá nhân, trước >= 24h, phí cố định 30.000đ/vé
	private JPanel buildFormDoi() {
		JPanel pnl = baseFormPanel("Thông tin đổi vé  —  Chỉ áp dụng vé cá nhân, trước giờ tàu \u2265 24 giờ");

		cbChuyen  = new JComboBox<>(new String[]{"SE1","SE3","SE5","SE19","TN2"});
		cbGhe     = new JComboBox<>(new String[]{"A01","A12","B05","C03","D08"});
		tfNgayDoi = makeTextField("");
		tfLePhi   = makeTextField("30.000 \u0111  (c\u1ed1 \u0111\u1ecbnh/v\u00e9)");
		tfLePhi.setEditable(false);

		JPanel grid = new JPanel(new GridBagLayout());
		grid.setOpaque(false);
		GridBagConstraints g = baseGbc();

// Cho các cột co giãn đều
		g.weightx = 1;
		g.fill = GridBagConstraints.HORIZONTAL;

// ===== HÀNG 1 =====
		g.gridx = 0; g.gridy = 0;
		grid.add(label("Chuyến mới:"), g);

		g.gridx = 1;
		grid.add(wrapCombo(cbChuyen), g);

		g.gridx = 2;
		grid.add(label("Ghế mới:"), g);

		g.gridx = 3;
		grid.add(wrapCombo(cbGhe), g);

// ===== HÀNG 2 =====
		g.gridx = 0; g.gridy = 1;
		grid.add(label("Ngày/giờ mới:"), g);

		g.gridx = 1;
		grid.add(wrapField(tfNgayDoi), g);

		g.gridx = 2;
		grid.add(label("Lệ phí đổi:"), g);

		g.gridx = 3;
		grid.add(wrapField(tfLePhi), g);

		pnl.add(grid);
		pnl.add(buildConfirmRow("X\u00e1c nh\u1eadn \u0111\u1ed5i", this::handleDoiVe));
		return pnl;
	}

	// ── FORM TRẢ ─────────────────────────────────────────────────────────────
	// Quy định:
	//   Cá nhân: >=48h → phí 10%; 12-48h → phí 20%; <12h → không hoàn
	//   Nhóm   : >=72h → phí 20%; 24-72h → phí 30%; <24h → không hoàn
	private JPanel buildFormTra() {
		JPanel pnl = baseFormPanel("Th\u00f4ng tin tr\u1ea3 v\u00e9");

		cbLyDo         = new JComboBox<>(new String[]{"B\u1eadn vi\u1ec7c","Ốm", "Thay \u0111\u1ed5i k\u1ebf ho\u1ea1ch","Kh\u00e1c"});
		tfPhiTra       = makeTextField(""); tfPhiTra.setEditable(false);
		tfHoanLai      = makeTextField(""); tfHoanLai.setEditable(false);
		tfTrangThaiTra = makeTextField(""); tfTrangThaiTra.setEditable(false);
		cbLyDo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

		JPanel grid = new JPanel(new GridBagLayout());
		grid.setOpaque(false);
		GridBagConstraints g = baseGbc();

		g.weightx = 1;
		g.fill = GridBagConstraints.HORIZONTAL;

// HÀNG 1
		g.gridx = 0; g.gridy = 0;
		grid.add(label("Lý do trả vé:"), g);

		g.gridx = 1;
		grid.add(wrapCombo(cbLyDo), g);

		g.gridx = 2;
		grid.add(label("Trạng thái:"), g);

		g.gridx = 3;
		grid.add(wrapField(tfTrangThaiTra), g);

// HÀNG 2
		g.gridx = 0; g.gridy = 1;
		grid.add(label("Phí trả vé:"), g);

		g.gridx = 1;
		grid.add(wrapField(tfPhiTra), g);

		g.gridx = 2;
		grid.add(label("Tiền hoàn lại:"), g);

		g.gridx = 3;
		grid.add(wrapField(tfHoanLai), g);

		pnl.add(grid);
		pnl.add(buildConfirmRow("X\u00e1c nh\u1eadn tr\u1ea3", this::handleTraVe));
		return pnl;
	}

	// ── LOGIC ───────────────────────────────────────────────────────────────

	/** Tính giờ còn lại đến giờ tàu khởi hành (âm = đã qua) */
	private long tinhGioConLai(String[] d) {
		try {
			LocalDateTime kh = LocalDateTime.parse(d[4], FMT);
			return ChronoUnit.HOURS.between(LocalDateTime.now(), kh);
		} catch (Exception ex) {
			return -1;
		}
	}

	private boolean laNhom(String[] d) {
		return d[3].toLowerCase().contains("nhóm");
	}

	/** Nạp lại table theo keyword */
	private void reloadTable(String keyword) {
		tableModel.setRowCount(0);
		String kw = keyword.toUpperCase();
		for (Map.Entry<String, String[]> entry : FAKE_DATA.entrySet()) {
			String maVe = entry.getKey();
			String[] d  = entry.getValue();
			if (kw.isEmpty() || maVe.toUpperCase().contains(kw)) {
				tableModel.addRow(new Object[]{maVe, d[0], d[1], d[2], d[3], d[4], d[5], d[6]});
			}
		}
	}

	/** Điền form từ dòng đang chọn */
	private void fillFormFromSelection() {
		int row = table.getSelectedRow();
		if (row < 0) return;

		String maVe = (String) tableModel.getValueAt(row, 0);
		String[] d  = FAKE_DATA.get(maVe);
		if (d == null) return;

		long gioConLai = tinhGioConLai(d);
		boolean nhom   = laNhom(d);

		// --- Điền form đổi ---
		cbChuyen.setSelectedItem(d[0]);
		cbGhe.setSelectedItem(d[6]);
		tfNgayDoi.setText(d[4]);
		tfLePhi.setText("30.000 đ  (cố định/vé)");

		// --- Điền form trả ---
		if (nhom) {
			// Vé nhóm
			if (gioConLai >= 72) {
				setHoanLai(d, 20);
				tfPhiTra.setText("20%");
				tfTrangThaiTra.setText("Hợp lệ — phí 20%");
			} else if (gioConLai >= 24) {
				setHoanLai(d, 30);
				tfPhiTra.setText("30%");
				tfTrangThaiTra.setText("Hợp lệ — phí 30%");
			} else {
				tfPhiTra.setText("—");
				tfHoanLai.setText("Không được hoàn tiền");
				tfTrangThaiTra.setText("Quá hạn (< 24h)");
			}
		} else {
			// Vé cá nhân
			if (gioConLai >= 48) {
				setHoanLai(d, 10);
				tfPhiTra.setText("10%");
				tfTrangThaiTra.setText("Hợp lệ — phí 10%");
			} else if (gioConLai >= 12) {
				setHoanLai(d, 20);
				tfPhiTra.setText("20%");
				tfTrangThaiTra.setText("Hợp lệ — phí 20%");
			} else {
				tfPhiTra.setText("—");
				tfHoanLai.setText("Không được hoàn tiền");
				tfTrangThaiTra.setText("Quá hạn (< 12h)");
			}
		}
	}

	/** Tính và set tiền hoàn lại = giá × số lượng × (1 - phiPhan/100) */
	private void setHoanLai(String[] d, int phiPhan) {
		try {
			long donGia  = Long.parseLong(d[7]);
			int  soLuong = Integer.parseInt(d[5].trim());
			long tong    = donGia * soLuong;
			long hoan    = Math.round(tong * (1.0 - phiPhan / 100.0));
			tfHoanLai.setText(String.format("%,d VNĐ", hoan).replace(",", "."));
		} catch (NumberFormatException ex) {
			tfHoanLai.setText("Lỗi tính toán");
		}
	}

	/** Xử lý khi bấm "Xác nhận đổi" */
	private void handleDoiVe() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showWarn("Vui lòng chọn một vé trong danh sách."); return;
		}
		String maVe = (String) tableModel.getValueAt(row, 0);
		String[] d  = FAKE_DATA.get(maVe);
		if (d == null) return;

		// Kiểm tra vé nhóm
		if (laNhom(d)) {
			showWarn("Không áp dụng đổi vé đối với vé nhóm."); return;
		}

		// Kiểm tra thời gian: phải trước >= 24h
		long gioConLai = tinhGioConLai(d);
		if (gioConLai < 24) {
			showWarn("Không thể đổi vé.\nYêu cầu đổi phải thực hiện trước giờ tàu khởi hành ít nhất 24 giờ.\n"
					+ "Hiện còn: " + gioConLai + " giờ.");
			return;
		}

		// Kiểm tra ngày mới nhập
		String ngayMoi = tfNgayDoi.getText().trim();
		try {
			LocalDateTime.parse(ngayMoi, FMT);
		} catch (Exception ex) {
			showWarn("Ngày/giờ mới không hợp lệ.\nVui lòng nhập đúng định dạng dd/MM/yyyy HH:mm."); return;
		}

		// Lập phiếu xác nhận
		String phieu = String.format(
				"PHIẾU ĐỔI VÉ\n─────────────────────────────\n"
						+ "Mã vé       : %s\n"
						+ "Chuyến cũ   : %s  (%s → %s)\n"
						+ "Giờ KH cũ   : %s\n"
						+ "Chuyến mới  : %s\n"
						+ "Ghế mới     : %s\n"
						+ "Giờ KH mới  : %s\n"
						+ "Lệ phí đổi  : 30.000 VNĐ\n"
						+ "─────────────────────────────\n"
						+ "Nhân viên xác nhận và thu phí 30.000 VNĐ.",
				maVe, d[0], d[1], d[2], d[4],
				cbChuyen.getSelectedItem(), cbGhe.getSelectedItem(), ngayMoi);

		int ok = JOptionPane.showConfirmDialog(this, phieu, "Xác nhận đổi vé",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (ok == JOptionPane.OK_OPTION) {
			// Cập nhật FAKE_DATA (thực tế gọi DAO)
			d[0] = (String) cbChuyen.getSelectedItem();
			d[4] = ngayMoi;
			d[6] = (String) cbGhe.getSelectedItem();
			reloadTable(txtSearch.getText().trim());
			JOptionPane.showMessageDialog(this, "Đổi vé thành công!\nMã vé: " + maVe,
					"Thành công", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/** Xử lý khi bấm "Xác nhận trả" */
	private void handleTraVe() {
		int row = table.getSelectedRow();
		if (row < 0) {
			showWarn("Vui lòng chọn một vé trong danh sách."); return;
		}
		String maVe = (String) tableModel.getValueAt(row, 0);
		String[] d  = FAKE_DATA.get(maVe);
		if (d == null) return;

		long    gioConLai = tinhGioConLai(d);
		boolean nhom      = laNhom(d);

		// Kiểm tra điều kiện thời gian
		boolean hopLe;
		if (nhom)  hopLe = gioConLai >= 24;
		else       hopLe = gioConLai >= 12;

		if (!hopLe) {
			String han = nhom ? "24 giờ (vé nhóm)" : "12 giờ (vé cá nhân)";
			showWarn("Không thể trả vé.\nYêu cầu trả phải trước giờ tàu khởi hành ít nhất " + han + ".\n"
					+ "Hiện còn: " + gioConLai + " giờ.");
			return;
		}

		// Lập phiếu xác nhận
		String phieu = String.format(
				"PHIẾU TRẢ VÉ\n─────────────────────────────\n"
						+ "Mã vé         : %s\n"
						+ "Loại vé       : %s\n"
						+ "Chuyến tàu    : %s  (%s → %s)\n"
						+ "Giờ KH        : %s\n"
						+ "Lý do trả     : %s\n"
						+ "Phí trả vé    : %s\n"
						+ "Tiền hoàn lại : %s\n"
						+ "─────────────────────────────\n"
						+ "Nhân viên xác nhận và hoàn tiền mặt cho hành khách.",
				maVe, d[3], d[0], d[1], d[2], d[4],
				cbLyDo.getSelectedItem(), tfPhiTra.getText(), tfHoanLai.getText());

		int ok = JOptionPane.showConfirmDialog(this, phieu, "Xác nhận trả vé",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (ok == JOptionPane.OK_OPTION) {
			// Xóa khỏi FAKE_DATA (thực tế gọi DAO)
			FAKE_DATA.remove(maVe);
			reloadTable(txtSearch.getText().trim());
			JOptionPane.showMessageDialog(this, "Trả vé thành công!\nMã vé: " + maVe,
					"Thành công", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// ── UI HELPERS ────────────────────────────────────────────────────────────

	/** GridBagConstraints mặc định: không stretch, padding đều */
	private GridBagConstraints baseGbc() {
		GridBagConstraints g = new GridBagConstraints();
		g.anchor  = GridBagConstraints.WEST;
		g.fill    = GridBagConstraints.NONE;
		g.insets  = new Insets(6, 0, 6, 12);
		g.gridwidth = 1;
		return g;
	}

	/** Label căn phải để thẳng hàng với field bên cạnh */
	private JLabel label(String text) {
		JLabel lb = new JLabel(text);
		lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		lb.setForeground(new Color(55, 55, 55));
		lb.setHorizontalAlignment(SwingConstants.RIGHT);
		lb.setPreferredSize(new Dimension(130, 22));
		return lb;
	}

	private void showWarn(String msg) {
		JOptionPane.showMessageDialog(this, msg, "Kh\u00f4ng th\u1ec3 th\u1ef1c hi\u1ec7n", JOptionPane.WARNING_MESSAGE);
	}

	private JPanel baseFormPanel(String titleText) {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBackground(new Color(242, 247, 252));
		pnl.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(10, 15, 10, 15)));
		JLabel title = new JLabel(titleText);
		title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
		title.setForeground(GuiTheme.TEXT);
		pnl.add(title);
		pnl.add(Box.createVerticalStrut(6));
		return pnl;
	}

	private JPanel wrapField(JTextField tf) { return wrapField(tf, 160); }
	private JPanel wrapField(JTextField tf, int width) {
		JPanel p = new JPanel(new BorderLayout()) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(tf.isEnabled() && tf.isEditable() ? Color.WHITE : new Color(220, 235, 248));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(new Color(180, 205, 230));
				g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
				g2.dispose();
			}
		};
		p.setOpaque(false);
		p.setBorder(new EmptyBorder(6, 12, 6, 12));
		p.setPreferredSize(new Dimension(width, 36));
		tf.setOpaque(false);
		tf.setBorder(null);
		tf.setForeground(new Color(50, 50, 50));
		p.add(tf, BorderLayout.CENTER);
		return p;
	}

	private JPanel wrapCombo(JComboBox<String> cb) {
		JPanel p = new JPanel(new BorderLayout()) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(new Color(180, 205, 230));
				g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
				g2.dispose();
			}
		};
		p.setOpaque(false);
		p.setBorder(new EmptyBorder(4, 8, 4, 4));
		p.setPreferredSize(new Dimension(160, 36));
		cb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		cb.setOpaque(false);
		cb.setBorder(null);
		p.add(cb, BorderLayout.CENTER);
		return p;
	}

	private JTextField makeTextField(String text) {
		JTextField tf = new JTextField(text);
		tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		return tf;
	}

	private JPanel buildConfirmRow(String label, Runnable action) {
		JPanel outer = new JPanel(new BorderLayout());
		outer.setOpaque(false);
		outer.setBorder(new EmptyBorder(18, 0, 0, 0));
		JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		btnWrap.setOpaque(false);
		JButton btn = buildActionButton(label);
		btn.addActionListener(e -> action.run());
		btnWrap.add(btn);
		outer.add(btnWrap, BorderLayout.EAST);
		return outer;
	}

	private JButton buildActionButton(String label) {
		JButton btn = new JButton(label) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed()  ? GuiTheme.NAVY_DARK
						: getModel().isRollover() ? GuiTheme.NAVY_HOVER
						  :                          GuiTheme.NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(Color.WHITE);
				g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
				FontMetrics fm = g2.getFontMetrics();
				String txt = getText();
				g2.drawString(txt,
						(getWidth()  - fm.stringWidth(txt)) / 2,
						(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};
		btn.setPreferredSize(new Dimension(150, 36));
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return btn;
	}

	private static JRadioButton buildRadio(String text) {
		JRadioButton rdo = new JRadioButton(text);
		rdo.setOpaque(false);
		rdo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		rdo.setForeground(GuiTheme.TEXT);
		rdo.setFocusPainted(false);
		return rdo;
	}

	private JPanel buildSectionTitle(String title) {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setOpaque(false);
		JLabel lb = new JLabel(title);
		lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
		lb.setForeground(GuiTheme.TEXT);
		pnl.add(lb, BorderLayout.WEST);
		return pnl;
	}
}