package GUI;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import de.wannawork.jcalendar.JCalendarComboBox;

final class DoiTraGUI extends JPanel {
	private static final Font FONT_LABEL = GuiTheme.font("Segoe UI", Font.PLAIN, 15);
	private static final Font FONT_INPUT = GuiTheme.font("Segoe UI", Font.PLAIN, 16);
	private static final Font FONT_BUTTON = GuiTheme.font("Segoe UI", Font.BOLD, 15);
	private static final int FIELD_HEIGHT = 36;
	private static final int BUTTON_HEIGHT = 40;
	private static final Color BORDER = new Color(210, 215, 224);
	private static final Color PRIMARY = new Color(71, 71, 156);

	public DoiTraGUI() {
		setLayout(new BorderLayout());
		setBackground(GuiTheme.LIGHT_BG);

		JPanel pnlPage = new JPanel();
		pnlPage.setLayout(new BoxLayout(pnlPage, BoxLayout.Y_AXIS));
		pnlPage.setOpaque(false);
		pnlPage.setBorder(new EmptyBorder(20, 20, 20, 20));

		pnlPage.add(buildHeader());
		pnlPage.add(Box.createVerticalStrut(10));
		pnlPage.add(buildSearchBar());
		pnlPage.add(Box.createVerticalStrut(10));
		pnlPage.add(buildTicketTable());
		pnlPage.add(Box.createVerticalStrut(10));
		pnlPage.add(buildActionPanel());

		add(pnlPage, BorderLayout.NORTH);
	}

	// HEADER
	private JPanel buildHeader() {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setOpaque(false);
		JLabel title = new JLabel("ĐỔI / TRẢ VÉ");
		title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 22));
		pnl.add(title, BorderLayout.WEST);
		return pnl;
	}

	// THANH TIM KIEM
	private JPanel buildSearchBar() {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
		pnl.setOpaque(false);

		JLabel timkiem = new JLabel("Nhập mã vé cần đổi/trả:");
		timkiem.setFont(FONT_LABEL);

		JTextField txt = new JTextField("27CT30");
		txt.setFont(FONT_INPUT);
		txt.setPreferredSize(new Dimension(350, FIELD_HEIGHT));

		JButton btn = new JButton("Tìm kiếm");
		styleButton(btn);

		pnl.add(timkiem);
		pnl.add(txt);
		pnl.add(btn);

		return pnl;
	}

	// ================= TABLE =================
	private JPanel buildTicketTable() {
		DefaultTableModel model = new DefaultTableModel(
				new Object[] { "Mã vé", "Chuyến tàu", "Ga đi", "Ga đến", "Loại vé", "Ngày KH", "SL" }, 0) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		model.addRow(new Object[] { "27CT30", "SE5", "Diêu Trì", "Sài Gòn", "Vé nhóm", "29/08/2026", "01" });

		JTable table = new JTable(model);
		styleTable(table);

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new LineBorder(BORDER, 1, true));
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setPreferredSize(new Dimension(1000, 80));

		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setOpaque(false);
		pnl.add(scroll);

		return pnl;
	}

	// ================= ACTION =================
	private JPanel buildActionPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setOpaque(false);

		// radio
		JPanel radio = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 5));
		radio.setOpaque(false);

		JRadioButton rdoTra = new JRadioButton("Trả vé");
		JRadioButton rdoDoi = new JRadioButton("Đổi vé");
		rdoTra.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
		rdoDoi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
		ButtonGroup group = new ButtonGroup();
		group.add(rdoTra);
		group.add(rdoDoi);
		rdoDoi.setSelected(true);
		radio.add(rdoTra);
		radio.add(rdoDoi);
		pnl.add(radio);
		// form
		JPanel pnlDoi = buildFormDoi();
		JPanel pnlTra = buildFormTra();

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
		rdoTra.setFont(FONT_LABEL);
		rdoDoi.setFont(FONT_LABEL);
		return pnl;
	}

	// ================= FORM ĐỔI =================
	private JPanel buildFormDoi() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBackground(Color.WHITE);
		pnl.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(10, 15, 10, 15) // 🔥 giảm
																											// padding
		));

		// TITLE
		JLabel title = new JLabel("Thông tin vé mới", SwingConstants.CENTER);
		title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16)); // 🔥 nhỏ lại
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnl.add(title);

		pnl.add(Box.createVerticalStrut(8)); // 🔥 giảm spacing

		// ROW 1
		JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 8));
		row1.setOpaque(false);

		row1.add(createField("Chuyến:", new JComboBox<>(new String[] { "SE5" })));
		row1.add(createField("Ghế:", new JComboBox<>(new String[] { "B05" })));
		row1.add(createField("Ngày:", new JTextField("29/08/2026")));

		pnl.add(row1);

		// ROW 2
		JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 2));
		row2.setOpaque(false);

		row2.add(createField("Giá cũ:", new JTextField("450.000")));
		row2.add(createField("Giá mới:", new JTextField("500.000")));

		pnl.add(row2);

		// ROW 3
		JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 2));
		row3.setOpaque(false);

		row3.add(createField("Chênh lệch:", new JTextField("50.000")));

		pnl.add(row3);

		// BUTTON
		JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
		pnlBtn.setOpaque(false);

		JButton btn = new JButton("Xác nhận");
		styleButton(btn);
		btn.setPreferredSize(new Dimension(120, 34)); // 🔥 nhỏ lại

		pnlBtn.add(btn);
		pnl.add(pnlBtn);

		return pnl;
	}

	// ================= FORM TRẢ =================
	private JPanel buildFormTra() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setBackground(Color.WHITE);
		pnl.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(10, 15, 10, 15)));

		// ===== TITLE =====
		JLabel title = new JLabel("Thông tin trả vé", SwingConstants.CENTER);
		title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnl.add(title);

		pnl.add(Box.createVerticalStrut(8));

		// ===== ROW 1 =====
		JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
		row1.setOpaque(false);

		row1.add(createField("Lý do trả vé:", new JComboBox<>(new String[] { "Bận việc", "Ốm", "Khác" })));

		pnl.add(row1);

		// ===== ROW 2 =====
		JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 5));
		row2.setOpaque(false);

		row2.add(createField("Phí trả vé:", new JTextField("10%")));
		row2.add(createField("Tiền hoàn lại:", new JTextField("405.000 VNĐ")));

		pnl.add(row2);

		pnl.add(Box.createVerticalStrut(5));

		// ===== BUTTON =====
		JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
		pnlBtn.setOpaque(false);

		JButton btn = new JButton("Xác nhận");
		styleButton(btn);
		btn.setPreferredSize(new Dimension(120, 34));

		pnlBtn.add(btn);

		pnl.add(pnlBtn);

		return pnl;
	}

	// ================= HELPER =================
	private GridBagConstraints baseGbc() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 10, 5, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		return gbc;
	}

	private void addField(JPanel pnl, GridBagConstraints gbc, int x, int y, String label, JComponent comp) {
		JLabel lb = new JLabel(label);
		lb.setFont(FONT_LABEL);

		gbc.gridx = x * 2;
		gbc.gridy = y;
		pnl.add(lb, gbc);

		gbc.gridx = x * 2 + 1;

		comp.setFont(FONT_INPUT); // 🔥 fix font
		comp.setPreferredSize(new Dimension(180, FIELD_HEIGHT)); // 🔥 fix height

		pnl.add(comp, gbc);
	}

	private void styleButton(JButton btn) {
		btn.setFont(FONT_BUTTON);
		btn.setBackground(PRIMARY);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setBorder(new LineBorder(PRIMARY, 1, true));
		btn.setPreferredSize(new Dimension(130, BUTTON_HEIGHT));
	}

	private void styleTable(JTable table) {
		table.setRowHeight(28);
		table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
		table.setGridColor(new Color(230, 233, 238));
		table.setSelectionBackground(new Color(207, 209, 214));

		JTableHeader header = table.getTableHeader();
		header.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
		header.setBackground(Color.WHITE);
		header.setBorder(new LineBorder(BORDER, 1, true));

		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(center);
	}

	private JPanel createField(String label, JComponent comp) {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.setOpaque(false);

		JLabel lb = new JLabel(label);
		lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13)); //

		comp.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
		comp.setPreferredSize(new Dimension(130, 28)); //

		pnl.add(lb);
		pnl.add(Box.createVerticalStrut(2));
		pnl.add(comp);

		return pnl;
	}
}