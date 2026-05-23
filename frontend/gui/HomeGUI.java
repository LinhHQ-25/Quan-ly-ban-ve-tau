package gui;

import service.AuthService;
import connect_DB.Connect_DB;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class HomeGUI extends JPanel {

	private JTextField tfMaNV, tfVaiTro, tfHoTen, tfNgaySinh, tfGioiTinh, tfSdt, tfEmail, tfDiaChi;
	private RoundedPanel btnUpdatePanel;
	private JLabel lblUpdateBtn;
	private boolean isEditMode = false;
	private boolean isWaitingForApproval = false;

	private static final Color COLOR_DISABLED = new Color(245, 245, 245);
	private static final Color COLOR_ENABLED = Color.WHITE;
	private static final Color COLOR_BTN_NORMAL = new Color(240, 240, 240);
	private static final Color COLOR_BTN_SAVE = new Color(52, 152, 219);
	private static final Color COLOR_BTN_WAIT = new Color(180, 180, 180);

	private String currentMaNV = AuthService.getCurrentMaNV();
	private DefaultTableModel scheduleModel;

	public HomeGUI() {
		setBackground(new Color(235, 238, 243));
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(5, 10, 10, 10));

		JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlTitle.setOpaque(false);
		JLabel lblTitle = new JLabel("HỒ SƠ CÁ NHÂN - NHÂN VIÊN BÁN VÉ");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblTitle.setForeground(new Color(26, 46, 68));
		pnlTitle.add(lblTitle);
		add(pnlTitle, BorderLayout.NORTH);

		JPanel pnlMain = new JPanel(new GridLayout(1, 2, 15, 0));
		pnlMain.setOpaque(false);
		pnlMain.add(buildPersonalInfoCard());
		pnlMain.add(buildScheduleCard());
		add(pnlMain, BorderLayout.CENTER);

		add(buildFooterCard(), BorderLayout.SOUTH);

		refresh();
	}

	// =========================================================================
	// CARD 1: THÔNG TIN CÁ NHÂN
	// =========================================================================
	private JPanel buildPersonalInfoCard() {
		RoundedPanel card = new RoundedPanel(15, Color.WHITE);
		card.setLayout(new BorderLayout());
		card.setBorder(new EmptyBorder(12, 15, 12, 15));

		JLabel title = new JLabel("THÔNG TIN CHI TIẾT CÁ NHÂN");
		title.setFont(new Font("Segoe UI", Font.BOLD, 16));
		title.setBorder(new EmptyBorder(0, 0, 10, 0));
		card.add(title, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 0.5;

		tfMaNV = makeTextField(true);
		tfVaiTro = makeTextField(true);
		addFieldRow(form, gbc, 0, 0, "Mã nhân viên", tfMaNV, 1);
		addFieldRow(form, gbc, 1, 0, "Vai trò", tfVaiTro, 1);

		tfHoTen = makeTextField(true);
		addFieldRow(form, gbc, 0, 1, "Họ và tên", tfHoTen, 2);

		tfNgaySinh = makeTextField(true);
		tfGioiTinh = makeTextField(true);
		addFieldRow(form, gbc, 0, 2, "Ngày sinh", tfNgaySinh, 1);
		addFieldRow(form, gbc, 1, 2, "Giới tính", tfGioiTinh, 1);

		tfSdt = makeTextField(true);
		tfEmail = makeTextField(true);
		addFieldRow(form, gbc, 0, 3, "Số điện thoại", tfSdt, 1);
		addFieldRow(form, gbc, 1, 3, "Email", tfEmail, 1);

		tfDiaChi = makeTextField(true);
		addFieldRow(form, gbc, 0, 4, "Địa chỉ", tfDiaChi, 2);

		card.add(form, BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
		btnPanel.setOpaque(false);

		btnUpdatePanel = new RoundedPanel(8, COLOR_BTN_NORMAL);
		btnUpdatePanel.setBorder(new EmptyBorder(8, 18, 8, 18));
		btnUpdatePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

		lblUpdateBtn = new JLabel("Yêu cầu cập nhật");
		lblUpdateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblUpdateBtn.setForeground(new Color(26, 46, 68));
		btnUpdatePanel.add(lblUpdateBtn);

		btnUpdatePanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (!isWaitingForApproval)
					handleUpdateBtn();
			}

			public void mouseEntered(MouseEvent e) {
				if (!isWaitingForApproval) {
					btnUpdatePanel.setBgColor(isEditMode ? new Color(41, 128, 185) : new Color(220, 220, 220));
					btnUpdatePanel.repaint();
				}
			}

			public void mouseExited(MouseEvent e) {
				if (!isWaitingForApproval) {
					btnUpdatePanel.setBgColor(isEditMode ? COLOR_BTN_SAVE : COLOR_BTN_NORMAL);
					btnUpdatePanel.repaint();
				}
			}
		});

		btnPanel.add(btnUpdatePanel);
		card.add(btnPanel, BorderLayout.SOUTH);
		return card;
	}

	private void handleUpdateBtn() {
		if (!isEditMode) {
			isEditMode = true;
			setFieldsEditable(true);
			lblUpdateBtn.setText("Gửi yêu cầu xét duyệt");
			lblUpdateBtn.setForeground(Color.WHITE);
			btnUpdatePanel.setBgColor(COLOR_BTN_SAVE);
			btnUpdatePanel.repaint();
		} else {
			if (guiYeuCauCapNhat()) {
				isEditMode = false;
				setFieldsEditable(false);
				isWaitingForApproval = true;
				lblUpdateBtn.setText("Đang đợi xét duyệt...");
				lblUpdateBtn.setForeground(Color.WHITE);
				btnUpdatePanel.setBgColor(COLOR_BTN_WAIT);
				btnUpdatePanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				btnUpdatePanel.repaint();

				loadEmployeeData(); // Load lại data cũ khi chưa duyệt
				showCustomPopup("Đã gửi yêu cầu cập nhật hồ sơ đến Quản lý!", true);
			}
		}
	}

	private void setFieldsEditable(boolean editable) {
		// Cho phép sửa tất cả trừ Mã nhân viên
		for (JTextField tf : new JTextField[] { tfHoTen, tfVaiTro, tfNgaySinh, tfGioiTinh, tfSdt, tfEmail, tfDiaChi }) {
			tf.setEditable(editable);
			tf.setEnabled(editable);
			tf.setBackground(editable ? COLOR_ENABLED : COLOR_DISABLED);
		}
	}

	// =========================================================================
	// CARD 2: LỊCH LÀM VIỆC (Đã khôi phục)
	// =========================================================================
	private JPanel buildScheduleCard() {
		RoundedPanel card = new RoundedPanel(15, Color.WHITE);
		card.setLayout(new BorderLayout());
		card.setBorder(new EmptyBorder(12, 15, 12, 15));

		JLabel title = new JLabel("LỊCH LÀM VIỆC TUẦN NÀY");
		title.setFont(new Font("Segoe UI", Font.BOLD, 16));
		title.setBorder(new EmptyBorder(0, 0, 10, 0));
		card.add(title, BorderLayout.NORTH);

		String[] cols = { "STT", "Mã ca", "Tên ca", "Bắt đầu", "Kết thúc", "Ngày" };
		scheduleModel = new DefaultTableModel(cols, 0) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		JTable table = new JTable(scheduleModel);
		table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		table.setRowHeight(28);
		table.setShowGrid(true);
		table.setGridColor(new Color(230, 230, 230));

		JTableHeader header = table.getTableHeader();
		header.setFont(new Font("Segoe UI", Font.BOLD, 14));
		header.setBackground(new Color(245, 245, 245));

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new LineBorder(new Color(220, 220, 220), 1));
		card.add(scroll, BorderLayout.CENTER);

		return card;
	}

	// =========================================================================
	// CARD 3: FOOTER (Đã khôi phục đủ 2 nút bấm)
	// =========================================================================
	private JPanel buildFooterCard() {
		RoundedPanel card = new RoundedPanel(15, Color.WHITE);
		card.setLayout(new BorderLayout());
		card.setBorder(new EmptyBorder(8, 15, 10, 15));

		JLabel title = new JLabel("THAO TÁC CA LÀM");
		title.setFont(new Font("Segoe UI", Font.BOLD, 15));
		title.setBorder(new EmptyBorder(0, 0, 8, 0));
		card.add(title, BorderLayout.NORTH);

		JPanel btnGrid = new JPanel(new GridLayout(1, 2, 15, 0));
		btnGrid.setOpaque(false);

		btnGrid.add(createFooterActionBtn("Đổi mật khẩu", "", () -> {
			Window owner = SwingUtilities.getWindowAncestor(this);
			if (owner instanceof JFrame)
				new DoiMatKhauDialog((JFrame) owner);
		}));
		btnGrid.add(createFooterActionBtn("Chốt ca / Bàn giao ca", "", () -> {
			JOptionPane.showMessageDialog(this, "Tính năng chốt ca đang được phát triển!", "Thông báo",
					JOptionPane.INFORMATION_MESSAGE);
		}));

		card.add(btnGrid, BorderLayout.CENTER);
		return card;
	}

	// =========================================================================
	// XỬ LÝ DATABASE
	// =========================================================================
	public void refresh() {
		currentMaNV = AuthService.getCurrentMaNV();
		if (currentMaNV == null || currentMaNV.isEmpty())
			currentMaNV = "NV001";

		loadEmployeeData();
		loadScheduleData();
		kiemTraTrangThaiYeuCau();
	}

	private void loadEmployeeData() {
		try (Connection con = Connect_DB.getConnection()) {
			if (con == null)
				return;
			String sql = "SELECT maNV, hoTenNV, loaiNV, ngaySinh, gioiTinh, soDT, email, diaChi FROM NhanVien WHERE maNV = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, currentMaNV);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				tfMaNV.setText(rs.getString("maNV"));
				tfHoTen.setText(rs.getString("hoTenNV"));

				String loai = rs.getString("loaiNV");
				tfVaiTro.setText("NHAN_VIEN_QUAN_LY".equals(loai) ? "Quản lý" : "Bán vé");

				java.sql.Date ngaySinh = rs.getDate("ngaySinh");
				tfNgaySinh
						.setText(ngaySinh != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(ngaySinh) : "");

				Object gt = rs.getObject("gioiTinh");
				tfGioiTinh.setText(gt == null ? "" : (rs.getBoolean("gioiTinh") ? "Nam" : "Nữ"));

				tfSdt.setText(rs.getString("soDT") != null ? rs.getString("soDT") : "");
				tfEmail.setText(rs.getString("email") != null ? rs.getString("email") : "");
				tfDiaChi.setText(rs.getString("diaChi") != null ? rs.getString("diaChi") : "");
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	private void loadScheduleData() {
		if (scheduleModel == null)
			return;
		scheduleModel.setRowCount(0);
		try (Connection con = Connect_DB.getConnection()) {
			if (con == null)
				return;
			String sql = "SELECT ROW_NUMBER() OVER (ORDER BY l.ngayLam) AS stt, "
					+ "c.maCa, c.tenCa, CONVERT(varchar(5), c.gioBatDau, 108) AS batDau, "
					+ "CONVERT(varchar(5), c.gioKetThuc, 108) AS ketThuc, CONVERT(varchar(10), l.ngayLam, 23) AS ngayLam "
					+ "FROM LichLamViec l JOIN CaLamViec c ON l.maCa = c.maCa "
					+ "WHERE l.maNV = ? AND l.ngayLam >= DATEADD(DAY, 2 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) "
					+ "AND l.ngayLam < DATEADD(DAY, 9 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) ORDER BY l.ngayLam";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, currentMaNV);
			ResultSet rs = ps.executeQuery();
			int stt = 1;
			while (rs.next()) {
				scheduleModel.addRow(new Object[] { stt++, rs.getString("maCa"), rs.getString("tenCa"),
						rs.getString("batDau"), rs.getString("ketThuc"), rs.getString("ngayLam") });
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	private void kiemTraTrangThaiYeuCau() {
		try (Connection con = Connect_DB.getConnection()) {
			// Kiểm tra bảng YeuCauCapNhat có tồn tại không
			DatabaseMetaData meta = con.getMetaData();
			ResultSet tables = meta.getTables(null, null, "YeuCauCapNhat", null);
			if (!tables.next()) return; // Bảng chưa tồn tại, bỏ qua

			String sql = "SELECT trangThai FROM YeuCauCapNhat WHERE maNV = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, currentMaNV);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String status = rs.getString("trangThai");
				if (status.equals("Chờ duyệt")) {
					isWaitingForApproval = true;
					lblUpdateBtn.setText("Đang đợi xét duyệt...");
					lblUpdateBtn.setForeground(Color.WHITE);
					btnUpdatePanel.setBgColor(COLOR_BTN_WAIT);
					btnUpdatePanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				} else if (status.equals("Đã duyệt")) {
					xoaYeuCauCapNhat(con);
					resetNutBam();
					loadEmployeeData();
					showCustomPopup("Quản lý đã PHÊ DUYỆT yêu cầu cập nhật thông tin của bạn!", true);
				} else if (status.equals("Từ chối")) {
					xoaYeuCauCapNhat(con);
					resetNutBam();
					showCustomPopup("Quản lý đã TỪ CHỐI yêu cầu cập nhật thông tin của bạn!", false);
				}
			} else {
				resetNutBam();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean guiYeuCauCapNhat() {
		try (Connection con = Connect_DB.getConnection()) {
			String checkSql = "SELECT COUNT(*) FROM YeuCauCapNhat WHERE maNV = ?";
			PreparedStatement checkPs = con.prepareStatement(checkSql);
			checkPs.setString(1, currentMaNV);
			ResultSet rs = checkPs.executeQuery();
			rs.next();

			String sql = rs.getInt(1) > 0
					? "UPDATE YeuCauCapNhat SET hoTenMoi=?, sdtMoi=?, emailMoi=?, diaChiMoi=?, trangThai='Chờ duyệt' WHERE maNV=?"
					: "INSERT INTO YeuCauCapNhat (hoTenMoi, sdtMoi, emailMoi, diaChiMoi, trangThai, maNV) VALUES (?, ?, ?, ?, 'Chờ duyệt', ?)";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, tfHoTen.getText().trim());
			ps.setString(2, tfSdt.getText().trim());
			ps.setString(3, tfEmail.getText().trim());
			ps.setString(4, tfDiaChi.getText().trim());
			ps.setString(5, currentMaNV);
			ps.executeUpdate();
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private void xoaYeuCauCapNhat(Connection con) throws SQLException {
		PreparedStatement ps = con.prepareStatement("DELETE FROM YeuCauCapNhat WHERE maNV = ?");
		ps.setString(1, currentMaNV);
		ps.executeUpdate();
	}

	private void resetNutBam() {
		isWaitingForApproval = false;
		isEditMode = false;
		lblUpdateBtn.setText("Yêu cầu cập nhật");
		lblUpdateBtn.setForeground(new Color(26, 46, 68));
		btnUpdatePanel.setBgColor(COLOR_BTN_NORMAL);
		btnUpdatePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void showCustomPopup(String message, boolean isSuccess) {
		JDialog dialog = new JDialog();
		dialog.setUndecorated(true);
		dialog.setBackground(new Color(0, 0, 0, 0));
		dialog.setAlwaysOnTop(true);
		Color bgColor = isSuccess ? new Color(46, 204, 113) : new Color(231, 76, 60);
		JPanel pnl = new JPanel() {
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(bgColor);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
			}
		};
		pnl.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
		JLabel lblMsg = new JLabel(message);
		lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblMsg.setForeground(Color.WHITE);
		pnl.add(lblMsg);
		dialog.add(pnl);
		dialog.pack();

		// Đặt vị trí góc dưới phải màn hình
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = screenSize.width - dialog.getWidth() - 20;
		int y = screenSize.height - dialog.getHeight() - 60; // 60 để tránh taskbar
		dialog.setLocation(x, y);

		Timer timer = new Timer(3000, e -> dialog.dispose());
		timer.setRepeats(false);
		timer.start();
		dialog.setVisible(true);
	}

	private JTextField makeTextField(boolean disabled) {
		JTextField tf = new JTextField();
		tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tf.setEditable(!disabled);
		tf.setEnabled(!disabled);
		tf.setBackground(disabled ? COLOR_DISABLED : COLOR_ENABLED);
		tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(210, 215, 224), 1, true),
				new EmptyBorder(6, 10, 6, 10)));
		tf.setDisabledTextColor(Color.BLACK);
		return tf;
	}

	private void addFieldRow(JPanel parent, GridBagConstraints gbc, int col, int row, String label, JTextField tf,
	                         int width) {
		gbc.gridx = col;
		gbc.gridy = row;
		gbc.gridwidth = width;
		JPanel wrapper = new JPanel(new BorderLayout(0, 4));
		wrapper.setOpaque(false);
		JLabel lbl = new JLabel(label);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lbl.setForeground(Color.DARK_GRAY);
		wrapper.add(lbl, BorderLayout.NORTH);
		wrapper.add(tf, BorderLayout.CENTER);
		parent.add(wrapper, gbc);
	}

	private JPanel createFooterActionBtn(String title, String subTxt, Runnable onClick) {
		RoundedPanel btn = new RoundedPanel(12, new Color(238, 246, 255));
		btn.setLayout(new BorderLayout(15, 0));
		btn.setBorder(new EmptyBorder(10, 15, 10, 15));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setOpaque(false);
		JLabel lblTitle = new JLabel(title);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblTitle.setForeground(new Color(26, 46, 68));
		textPanel.add(lblTitle);
		if (!subTxt.isEmpty()) {
			textPanel.add(Box.createVerticalStrut(3));
			JLabel lblSub = new JLabel(subTxt);
			lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			lblSub.setForeground(Color.GRAY);
			textPanel.add(lblSub);
		}
		btn.add(textPanel, BorderLayout.CENTER);
		btn.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (onClick != null)
					onClick.run();
			}

			public void mouseEntered(MouseEvent e) {
				btn.setBgColor(new Color(220, 235, 250));
				btn.repaint();
			}

			public void mouseExited(MouseEvent e) {
				btn.setBgColor(new Color(238, 246, 255));
				btn.repaint();
			}
		});
		return btn;
	}

	class RoundedPanel extends JPanel {
		private int radius;
		private Color bgColor;

		public RoundedPanel(int radius, Color bgColor) {
			this.radius = radius;
			this.bgColor = bgColor;
			setOpaque(false);
		}

		public void setBgColor(Color color) {
			this.bgColor = color;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(bgColor);
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
			g2.setColor(new Color(210, 215, 224));
			g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
			g2.dispose();
			super.paintComponent(g);
		}
	}
}