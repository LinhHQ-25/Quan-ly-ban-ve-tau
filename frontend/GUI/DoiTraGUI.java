package GUI;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;

final class DoiTraGUI extends JPanel {

	private static final Color BORDER = new Color(210, 215, 224);
	private static final Color PRIMARY = new Color(71, 71, 156);

	// ── DỮ LIỆU GIẢ ────────────────────────────────────────────────────────
	// maVe → { chuyenTau, gaDi, gaDen, loaiVe, ngayKH, soLuong, ghe, giaTien }
	private static final Map<String, String[]> FAKE_DATA = new LinkedHashMap<>();
	static {
		FAKE_DATA.put("27CT30",
				new String[] { "SE5", "Diêu Trì", "Sài Gòn", "Vé nhóm", "29/08/2026", "01", "B05", "450.000" });
		FAKE_DATA.put("13HN05",
				new String[] { "SE1", "Hà Nội", "Đà Nẵng", "Vé thường", "15/09/2026", "02", "A12", "320.000" });
		FAKE_DATA.put("08DN12",
				new String[] { "SE3", "Đà Nẵng", "Nha Trang", "Vé VIP", "02/10/2026", "01", "C03", "610.000" });
		FAKE_DATA.put("21NT07",
				new String[] { "TN2", "Nha Trang", "TP.HCM", "Vé thường", "10/10/2026", "03", "D08", "275.000" });
		FAKE_DATA.put("30HCM9",
				new String[] { "SE19", "TP.HCM", "Hà Nội", "Vé VIP", "20/11/2026", "01", "A01", "850.000" });
	}

	// ── STATE ───────────────────────────────────────────────────────────────
	private DefaultTableModel tableModel;
	private JTable table;
	private JTextField txtSearch;

	// Form đổi
	private JComboBox<String> cbChuyen;
	private JComboBox<String> cbGhe;
	private JTextField tfNgayDoi, tfGiaCu, tfGiaMoi, tfChenhLech;

	// Form trả
	private JComboBox<String> cbLyDo;
	private JTextField tfPhiTra, tfHoanLai;

	// Panel chứa form (để repaint khi switch)
	private JPanel pnlDoi, pnlTra;

	public DoiTraGUI() {
		setLayout(new BorderLayout());
		setBackground(GuiTheme.LIGHT_BG);

		JPanel pnlPage = new JPanel();
		pnlPage.setLayout(new BoxLayout(pnlPage, BoxLayout.Y_AXIS));
		pnlPage.setOpaque(false);
		pnlPage.setBorder(new EmptyBorder(GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM,
				GuiTheme.PAGE_PAD_LEFT));

		pnlPage.add(buildHeader("ĐỔI / TRẢ VÉ", "Dùng để đổi hoặc trả vé đã đặt."));
		pnlPage.add(Box.createVerticalStrut(12));
		pnlPage.add(buildSearchBar());
		pnlPage.add(Box.createVerticalStrut(12));
		pnlPage.add(buildTicketTable());
		pnlPage.add(Box.createVerticalStrut(12));
		pnlPage.add(buildActionPanel());

		add(pnlPage, BorderLayout.NORTH);

		// Nạp toàn bộ dữ liệu ban đầu
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
		pnl.add(lbSub, BorderLayout.SOUTH);
		return pnl;
	}

	// ── SEARCH BAR ──────────────────────────────────────────────────────────
	private JPanel buildSearchBar() {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
		pnl.setOpaque(false);

		JLabel lbLabel = new JLabel("Nhập mã vé cần đổi/trả:");
		lbLabel.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		lbLabel.setForeground(GuiTheme.TEXT);

		txtSearch = new JTextField();
		txtSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		txtSearch.setOpaque(false);
		txtSearch.setBorder(null);
		txtSearch.setPreferredSize(new Dimension(200, 24));

		JButton btn = buildActionButton("Tìm kiếm");
		btn.addActionListener(e -> reloadTable(txtSearch.getText().trim()));

		// Tìm ngay khi gõ
		txtSearch.addActionListener(e -> reloadTable(txtSearch.getText().trim()));

		pnl.add(lbLabel);
		pnl.add(wrapField(txtSearch));
		pnl.add(btn);
		return pnl;
	}

	// ── TABLE ───────────────────────────────────────────────────────────────
	private JPanel buildTicketTable() {
		JPanel pnlOuter = new JPanel(new BorderLayout(0, 8));
		pnlOuter.setOpaque(false);
		pnlOuter.add(buildSectionTitle("Danh sách vé"), BorderLayout.NORTH);

		tableModel = new DefaultTableModel(
				new Object[] { "Mã vé", "Chuyến tàu", "Ga đi", "Ga đến", "Loại vé", "Ngày KH", "SL" }, 0) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
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

		// Chọn dòng → điền form
		table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting())
				fillFormFromSelection();
		});

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new LineBorder(BORDER, 1, true));
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setPreferredSize(new Dimension(10000, 110));

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

		JRadioButton rdoTra = buildRadio("Trả vé");
		JRadioButton rdoDoi = buildRadio("Đổi vé");
		ButtonGroup group = new ButtonGroup();
		group.add(rdoTra);
		group.add(rdoDoi);
		rdoDoi.setSelected(true);

		JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
		pnlRadio.setOpaque(false);
		pnlRadio.add(rdoDoi);
		pnlRadio.add(rdoTra);
		pnl.add(pnlRadio);
		pnl.add(Box.createVerticalStrut(8));

		pnlDoi = buildFormDoi();
		pnlTra = buildFormTra();
		pnlTra.setVisible(false);

		rdoTra.addActionListener(e -> {
			pnlTra.setVisible(true);
			pnlDoi.setVisible(false);
		});
		rdoDoi.addActionListener(e -> {
			pnlTra.setVisible(false);
			pnlDoi.setVisible(true);
		});

		pnl.add(pnlDoi);
		pnl.add(pnlTra);
		return pnl;
	}

	// ── FORM ĐỔI ────────────────────────────────────────────────────────────
	private JPanel buildFormDoi() {
		JPanel pnl = baseFormPanel("Thông tin vé mới");

		cbChuyen = new JComboBox<>(new String[] { "SE1", "SE3", "SE5", "SE19", "TN2" });
		cbGhe = new JComboBox<>(new String[] { "A01", "A12", "B05", "C03", "D08" });
		tfNgayDoi = makeTextField("");
		tfGiaCu = makeTextField("");
		tfGiaMoi = makeTextField("");
		tfChenhLech = makeTextField("");

		// Tự tính chênh lệch khi đổi giá mới
		tfGiaMoi.addCaretListener(e -> recalcChenhLech());

		JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
		row1.setOpaque(false);
		row1.add(buildField("Chuyến:", wrapCombo(cbChuyen)));
		row1.add(buildField("Ghế:", wrapCombo(cbGhe)));
		row1.add(buildField("Ngày:", wrapField(tfNgayDoi)));

		JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
		row2.setOpaque(false);
		row2.add(buildField("Giá cũ:", wrapField(tfGiaCu)));
		row2.add(buildField("Giá mới:", wrapField(tfGiaMoi)));
		row2.add(buildField("Chênh lệch:", wrapField(tfChenhLech)));

		pnl.add(row1);
		pnl.add(row2);
		pnl.add(buildConfirmRow("Xác nhận"));
		return pnl;
	}

	// ── FORM TRẢ ────────────────────────────────────────────────────────────
	private JPanel buildFormTra() {
		JPanel pnl = baseFormPanel("Thông tin trả vé");

		cbLyDo = new JComboBox<>(new String[] { "Bận việc", "Ốm", "Khác" });
		tfPhiTra = makeTextField("");
		tfHoanLai = makeTextField("");
		tfHoanLai.setEditable(false);

		cbLyDo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

		JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
		row1.setOpaque(false);
		row1.add(buildField("Lý do trả vé:", wrapCombo(cbLyDo)));

		JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
		row2.setOpaque(false);
		row2.add(buildField("Phí trả vé:", wrapField(tfPhiTra)));
		row2.add(buildField("Tiền hoàn lại:", wrapField(tfHoanLai)));

		// Tự tính hoàn lại khi đổi phí
		tfPhiTra.addCaretListener(e -> recalcHoanLai());

		pnl.add(row1);
		pnl.add(row2);
		pnl.add(buildConfirmRow("Xác nhận"));
		return pnl;
	}

	// ── LOGIC ───────────────────────────────────────────────────────────────

	/** Nạp lại table theo keyword (rỗng = hiện tất cả) */
	private void reloadTable(String keyword) {
		tableModel.setRowCount(0);
		String kw = keyword.toUpperCase();
		for (Map.Entry<String, String[]> entry : FAKE_DATA.entrySet()) {
			String maVe = entry.getKey();
			String[] d = entry.getValue();
			if (kw.isEmpty() || maVe.toUpperCase().contains(kw)) {
				tableModel.addRow(new Object[] { maVe, d[0], d[1], d[2], d[3], d[4], d[5] });
			}
		}
	}

	/** Điền form đổi + trả từ dòng đang chọn */
	private void fillFormFromSelection() {
		int row = table.getSelectedRow();
		if (row < 0)
			return;

		String maVe = (String) tableModel.getValueAt(row, 0);
		String[] d = FAKE_DATA.get(maVe);
		if (d == null)
			return;

		// Form đổi
		cbChuyen.setSelectedItem(d[0]);
		cbGhe.setSelectedItem(d[6]);
		tfNgayDoi.setText(d[4]);
		tfGiaCu.setText(d[7]);
		tfGiaMoi.setText(d[7]); // mặc định bằng giá cũ
		tfChenhLech.setText("0");

		// Form trả — phí 10%, tính hoàn lại
		tfPhiTra.setText("10%");
		recalcHoanLai();
	}

	/** Tự tính chênh lệch = giá mới - giá cũ */
	private void recalcChenhLech() {
		try {
			long cu = Long.parseLong(tfGiaCu.getText().replace(".", "").trim());
			long moi = Long.parseLong(tfGiaMoi.getText().replace(".", "").trim());
			long cl = moi - cu;
			tfChenhLech.setText(String.format("%,d", cl).replace(",", "."));
		} catch (NumberFormatException ignored) {
			tfChenhLech.setText("");
		}
	}

	/** Tự tính tiền hoàn lại = giá gốc × (1 - phí%) */
	private void recalcHoanLai() {
		try {
			int row = table.getSelectedRow();
			if (row < 0)
				return;
			String maVe = (String) tableModel.getValueAt(row, 0);
			String[] d = FAKE_DATA.get(maVe);
			if (d == null)
				return;

			long gia = Long.parseLong(d[7].replace(".", ""));
			String phiStr = tfPhiTra.getText().replace("%", "").trim();
			double phi = Double.parseDouble(phiStr) / 100.0;
			long hoan = Math.round(gia * (1 - phi));
			tfHoanLai.setText(String.format("%,d VNĐ", hoan).replace(",", "."));
		} catch (NumberFormatException ignored) {
			tfHoanLai.setText("");
		}
	}

	// ── UI HELPERS ───────────────────────────────────────────────────────────

	private JPanel baseFormPanel(String titleText) {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBackground(new Color(242, 247, 252));
		pnl.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(10, 15, 10, 15)));
		JLabel title = new JLabel(titleText);
		title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 15));
		title.setForeground(GuiTheme.TEXT);
		pnl.add(title);
		pnl.add(Box.createVerticalStrut(6));
		return pnl;
	}

	private JPanel buildField(String label, JComponent comp) {
		JPanel pnl = new JPanel(new BorderLayout(0, 4));
		pnl.setOpaque(false);
		JLabel lb = new JLabel(label);
		lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		lb.setForeground(new Color(55, 55, 55));
		pnl.add(lb, BorderLayout.NORTH);
		pnl.add(comp, BorderLayout.CENTER);
		return pnl;
	}

	private JPanel wrapField(JTextField tf) {
		JPanel p = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(tf.isEnabled() && tf.isEditable() ? Color.WHITE : new Color(220, 235, 248));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(new Color(180, 205, 230));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
				g2.dispose();
			}
		};
		p.setOpaque(false);
		p.setBorder(new EmptyBorder(6, 12, 6, 12));
		p.setPreferredSize(new Dimension(160, 36));
		tf.setOpaque(false);
		tf.setBorder(null);
		tf.setForeground(new Color(50, 50, 50));
		p.add(tf, BorderLayout.CENTER);
		return p;
	}

	private JPanel wrapCombo(JComboBox<String> cb) {
		JPanel p = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(new Color(180, 205, 230));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
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

	private JPanel buildConfirmRow(String label) {
		JPanel outer = new JPanel(new BorderLayout());
		outer.setOpaque(false);
		outer.setBorder(new EmptyBorder(18, 0, 0, 0));
		JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		btnWrap.setOpaque(false);
		btnWrap.add(buildActionButton(label));
		outer.add(btnWrap, BorderLayout.EAST);
		return outer;
	}

	private JButton buildActionButton(String label) {
		JButton btn = new JButton(label) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
						: getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(Color.WHITE);
				g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
				FontMetrics fm = g2.getFontMetrics();
				String txt = getText();
				g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
						(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};
		btn.setPreferredSize(new Dimension(130, 36));
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