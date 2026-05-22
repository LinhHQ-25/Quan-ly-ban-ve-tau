package gui;

import service.AuthService;
import connect_DB.Connect_DB;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class HoSoQuanLyGUI extends JPanel {

	private static final Color COLOR_DISABLED = new Color(245, 245, 245);
	private static final Color COLOR_ENABLED = Color.WHITE;
	private static final Color COLOR_BTN_NORMAL = new Color(240, 240, 240);
	private static final Color COLOR_BTN_SAVE = new Color(52, 152, 219);
	private static final Color RED_BADGE = new Color(220, 53, 69);
	private static final Color COLOR_PRIMARY = new Color(26, 46, 68);
	private static final Color COLOR_BG = new Color(235, 238, 243);
	private static final Font FONT_14 = new Font("Segoe UI", Font.PLAIN, 14);
	private static final Font FONT_B14 = new Font("Segoe UI", Font.BOLD, 14);
	private static final Font FONT_B15 = new Font("Segoe UI", Font.BOLD, 15);

	private JTextField tfMaNV, tfVaiTro, tfHoTen, tfNgaySinh, tfGioiTinh, tfSdt, tfEmail, tfDiaChi;
	private RoundedPanel btnUpdatePanel;
	private JLabel lblUpdateBtn;
	private boolean isEditMode = false;

	private String currentMaNV = AuthService.getCurrentMaNV();
	private DefaultTableModel modelLichSu;
	private JButton btnThongBao;

	private JWindow dropdownWindow;
	private JWindow compareWindow;

	public HoSoQuanLyGUI() {
		setBackground(COLOR_BG);
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(5, 10, 10, 10));

		JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlTitle.setOpaque(false);
		JLabel lblTitle = new JLabel("HỒ SƠ QUẢN LÝ (TRƯỞNG GA)");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
		lblTitle.setForeground(COLOR_PRIMARY);
		pnlTitle.add(lblTitle);
		add(pnlTitle, BorderLayout.NORTH);

		// THAY GridLayout bằng GridBagLayout để kiểm soát tỉ lệ
		JPanel pnlMain = new JPanel(new GridBagLayout());
		pnlMain.setOpaque(false);
		GridBagConstraints gbcMain = new GridBagConstraints();
		gbcMain.fill = GridBagConstraints.BOTH;
		gbcMain.weighty = 1.0;
		gbcMain.insets = new Insets(0, 0, 0, 8);

		gbcMain.gridx = 0;
		gbcMain.weightx = 0.5; // 50/50 giống HomeGUI
		pnlMain.add(buildPersonalInfoCard(), gbcMain);

		gbcMain.gridx = 1;
		gbcMain.weightx = 0.5;
		gbcMain.insets = new Insets(0, 0, 0, 0);
		pnlMain.add(buildHistoryCard(), gbcMain);

		add(pnlMain, BorderLayout.CENTER);
		add(buildFooterCard(), BorderLayout.SOUTH);
		refresh();

		javax.swing.Timer autoRefresh = new javax.swing.Timer(10000, e -> updateBadgeCount());
		autoRefresh.start();
	}

	// =========================================================================
	// CARD 1: THÔNG TIN CÁ NHÂN
	// =========================================================================
	private JPanel buildPersonalInfoCard() {
		RoundedPanel card = new RoundedPanel(15, Color.WHITE);
		card.setLayout(new BorderLayout());
		card.setBorder(new EmptyBorder(12, 15, 12, 15));
		// XÓA dòng card.setMinimumSize(...)

		JLabel title = new JLabel("THÔNG TIN CHI TIẾT CÁ NHÂN");
		title.setFont(new Font("Segoe UI", Font.BOLD, 16));
		title.setBorder(new EmptyBorder(0, 0, 10, 0));
		card.add(title, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 0.5; // ĐỒNG BỘ với HomeGUI (0.5 thay vì 1.0)

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
		lblUpdateBtn = new JLabel("Cập nhật thông tin");
		lblUpdateBtn.setFont(FONT_B14);
		lblUpdateBtn.setForeground(COLOR_PRIMARY);
		btnUpdatePanel.add(lblUpdateBtn);
		btnUpdatePanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				handleUpdateBtn();
			}

			public void mouseEntered(MouseEvent e) {
				btnUpdatePanel.setBgColor(isEditMode ? new Color(41, 128, 185) : new Color(220, 220, 220));
				btnUpdatePanel.repaint();
			}

			public void mouseExited(MouseEvent e) {
				btnUpdatePanel.setBgColor(isEditMode ? COLOR_BTN_SAVE : COLOR_BTN_NORMAL);
				btnUpdatePanel.repaint();
			}
		});
		btnPanel.add(btnUpdatePanel);
		card.add(btnPanel, BorderLayout.SOUTH);
		return card;
	}

	private JTextField makeTextField(boolean disabled) {
	    JTextField tf = new JTextField();
	    tf.setFont(FONT_14);
	    tf.setEditable(!disabled);
	    tf.setEnabled(true);  // LUÔN true, không bao giờ setEnabled(false)
	    tf.setBackground(disabled ? COLOR_DISABLED : COLOR_ENABLED);
	    tf.setBorder(BorderFactory.createCompoundBorder(
	        new LineBorder(new Color(210, 215, 224), 1, true),
	        new EmptyBorder(6, 10, 6, 10)));
	    tf.setForeground(new Color(30, 30, 30));
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
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 13)); // ĐỒNG BỘ với HomeGUI (13 thay vì 14)
		lbl.setForeground(Color.DARK_GRAY);
		wrapper.add(lbl, BorderLayout.NORTH);
		wrapper.add(tf, BorderLayout.CENTER);
		parent.add(wrapper, gbc);
	}

	private void handleUpdateBtn() {
		if (!isEditMode) {
			isEditMode = true;
			setFieldsEditable(true);
			lblUpdateBtn.setText("Lưu cập nhật");
			lblUpdateBtn.setForeground(Color.WHITE);
			btnUpdatePanel.setBgColor(COLOR_BTN_SAVE);
			btnUpdatePanel.repaint();
		} else {
			if (saveManagerData()) {
				isEditMode = false;
				setFieldsEditable(false);
				lblUpdateBtn.setText("Cập nhật thông tin");
				lblUpdateBtn.setForeground(COLOR_PRIMARY);
				btnUpdatePanel.setBgColor(COLOR_BTN_NORMAL);
				btnUpdatePanel.repaint();
				showCustomPopup("Cập nhật hồ sơ quản lý thành công!", true);
			}
		}
	}

	private void setFieldsEditable(boolean editable) {
	    for (JTextField tf : new JTextField[]{tfHoTen, tfNgaySinh, tfGioiTinh, tfSdt, tfEmail, tfDiaChi}) {
	        tf.setEditable(editable);
	        tf.setEnabled(true);  // LUÔN true
	        tf.setBackground(editable ? COLOR_ENABLED : COLOR_DISABLED);
	        tf.setForeground(new Color(30, 30, 30));
	    }
	}

	// =========================================================================
	// CARD 2: LỊCH SỬ & CHUÔNG THÔNG BÁO
	// =========================================================================
	private JPanel buildHistoryCard() {
		RoundedPanel card = new RoundedPanel(15, Color.WHITE);
		card.setLayout(new BorderLayout());
		card.setBorder(new EmptyBorder(12, 15, 12, 15));

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setOpaque(false);
		headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel title = new JLabel("LỊCH SỬ HOẠT ĐỘNG GẦN ĐÂY");
		title.setFont(new Font("Segoe UI", Font.BOLD, 16));
		headerPanel.add(title, BorderLayout.WEST);

		btnThongBao = createBellButton("/Images/logoThongBao.png", 0);
		btnThongBao.addActionListener(e -> toggleDropdown());
		headerPanel.add(btnThongBao, BorderLayout.EAST);
		card.add(headerPanel, BorderLayout.NORTH);

		String[] cols = { "Thời gian", "Hành động thao tác", "Trạng thái" };
		modelLichSu = new DefaultTableModel(cols, 0) {
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		JTable table = new JTable(modelLichSu);
		table.setFont(FONT_14);
		table.setRowHeight(30);
		table.setShowGrid(true);
		table.setGridColor(new Color(230, 230, 230));

		JTableHeader header = table.getTableHeader();
		header.setFont(FONT_B14);
		header.setBackground(new Color(245, 245, 245));
		header.setPreferredSize(new Dimension(0, 36));

		table.getColumnModel().getColumn(0).setPreferredWidth(140);
		table.getColumnModel().getColumn(1).setPreferredWidth(260);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new LineBorder(new Color(220, 220, 220), 1));
		card.add(scroll, BorderLayout.CENTER);
		return card;
	}

	// =========================================================================
	// DROPDOWN THÔNG BÁO
	// =========================================================================
	private void toggleDropdown() {
		if (dropdownWindow != null && dropdownWindow.isVisible()) {
			dropdownWindow.setVisible(false);
			return;
		}
		buildAndShowDropdown();
	}

	private void buildAndShowDropdown() {
		Window owner = SwingUtilities.getWindowAncestor(this);
		dropdownWindow = new JWindow(owner);
		dropdownWindow.setBackground(new Color(0, 0, 0, 0));

		JPanel outer = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(0, 0, 0, 35));
				g2.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 14, 14);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 14, 14);
				g2.setColor(new Color(210, 215, 224));
				g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 14, 14);
				g2.dispose();
			}
		};
		outer.setOpaque(false);
		outer.setBorder(new EmptyBorder(8, 8, 10, 13));

		// Header dropdown — tiêu đề là "Thông báo"
		JPanel dropHeader = new JPanel(new BorderLayout());
		dropHeader.setOpaque(false);
		dropHeader.setBorder(new EmptyBorder(0, 4, 8, 4));

		JLabel dropTitle = new JLabel("Thông báo");
		dropTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
		dropTitle.setForeground(COLOR_PRIMARY);
		dropHeader.add(dropTitle, BorderLayout.WEST);

		ImageIcon iconClose = new ImageIcon(new ImageIcon(getClass().getResource("/Images/logoClose.png")).getImage()
				.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JLabel lblClose = new JLabel(iconClose);
		lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblClose.setOpaque(true);
		lblClose.setBackground(new Color(0, 0, 0, 0));
		lblClose.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				dropdownWindow.setVisible(false);
			}

			public void mouseEntered(MouseEvent e) {
				lblClose.setBackground(new Color(220, 220, 220));
				lblClose.repaint();
			}

			public void mouseExited(MouseEvent e) {
				lblClose.setBackground(new Color(0, 0, 0, 0));
				lblClose.repaint();
			}
		});
		dropHeader.add(lblClose, BorderLayout.EAST);
		outer.add(dropHeader, BorderLayout.NORTH);

		// Danh sách thông báo
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setOpaque(false);

		java.util.List<String[]> yeuCauList = loadYeuCauList();

		if (yeuCauList.isEmpty()) {
			JLabel lblEmpty = new JLabel("  Không có thông báo nào");
			lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
			lblEmpty.setForeground(Color.GRAY);
			lblEmpty.setBorder(new EmptyBorder(16, 8, 16, 8));
			listPanel.add(lblEmpty);
		} else {
			for (String[] row : yeuCauList) {
				listPanel.add(buildNotifCard(row));
				listPanel.add(Box.createVerticalStrut(4));
			}
		}

		JScrollPane scroll = new JScrollPane(listPanel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		int listH = Math.min(yeuCauList.size() * 76 + 10, 320);
		scroll.setPreferredSize(new Dimension(460, yeuCauList.isEmpty() ? 60 : listH));
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		outer.add(scroll, BorderLayout.CENTER);

		dropdownWindow.getContentPane().add(outer);
		dropdownWindow.pack();
		dropdownWindow.setSize(480, scroll.getPreferredSize().height + 72);

		Point btnLoc = btnThongBao.getLocationOnScreen();
		int x = btnLoc.x + btnThongBao.getWidth() - dropdownWindow.getWidth();
		int y = btnLoc.y + btnThongBao.getHeight() + 4;
		dropdownWindow.setLocation(x, y);
		dropdownWindow.setVisible(true);
	}

	private JPanel buildNotifCard(String[] row) {
		String maNV = row[0];
		String hoTen = row[1] != null ? row[1] : "";
		String sdt = row[2] != null ? row[2] : "";
		String email = row[3] != null ? row[3] : "";
		String diaChi = row[4] != null ? row[4] : "";

		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(true);
		card.setBackground(new Color(240, 246, 255));
		card.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(new Color(220, 230, 245), 1),
				new EmptyBorder(12, 15, 12, 15)));
		card.setCursor(new Cursor(Cursor.HAND_CURSOR));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Thu gọn tối đa chiều cao

		// YÊU CẦU: Chỉ hiển thị 1 dòng duy nhất, bỏ hết mã NV, họ tên, avatar
		JLabel lblOnly = new JLabel("Yêu cầu cập nhật thông tin");
		lblOnly.setFont(FONT_B14); // In đậm một xíu cho dễ nhìn
		lblOnly.setForeground(COLOR_PRIMARY);

		card.add(lblOnly, BorderLayout.CENTER);

		// Sự kiện hover đổi màu và click mở chi tiết vẫn giữ nguyên
		card.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				card.setBackground(new Color(225, 238, 255));
				card.repaint();
			}

			public void mouseExited(MouseEvent e) {
				card.setBackground(new Color(240, 246, 255));
				card.repaint();
			}

			public void mouseClicked(MouseEvent e) {
				dropdownWindow.setVisible(false);
				showCompareOverlay(maNV, hoTen, sdt, email, diaChi);
			}
		});
		return card;
	}

	// =========================================================================
	// OVERLAY CHI TIẾT — không scroll, font Segoe UI, có nút Từ chối/Phê duyệt
	// =========================================================================
	private void showCompareOverlay(String maNV, String hoTenMoi, String sdtMoi, String emailMoi, String diaChiMoi) {
		if (compareWindow != null)
			compareWindow.dispose();

		// Lấy thông tin cũ từ DB
		String[] oldData = { "", "", "", "" };
		try (Connection con = Connect_DB.getConnection()) {
			PreparedStatement ps = con
					.prepareStatement("SELECT hoTenNV, soDT, email, diaChi FROM NhanVien WHERE maNV = ?");
			ps.setString(1, maNV);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				oldData[0] = rs.getString("hoTenNV") != null ? rs.getString("hoTenNV") : "";
				oldData[1] = rs.getString("soDT") != null ? rs.getString("soDT") : "";
				oldData[2] = rs.getString("email") != null ? rs.getString("email") : "";
				oldData[3] = rs.getString("diaChi") != null ? rs.getString("diaChi") : "";
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		String[] newVals = { hoTenMoi, sdtMoi, emailMoi, diaChiMoi };
		String[] fields = { "Họ và tên", "Số điện thoại", "Email", "Địa chỉ" };

		Window owner = SwingUtilities.getWindowAncestor(this);
		compareWindow = new JWindow(owner);
		compareWindow.setBackground(new Color(0, 0, 0, 0));

		// Nền mờ
		JPanel overlay = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(new Color(0, 0, 0, 110));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		overlay.setOpaque(false);

		// Card trắng — kích thước cố định đủ hiển thị không cần scroll
		RoundedPanel card = new RoundedPanel(16, Color.WHITE);
		card.setLayout(new BorderLayout());
		card.setPreferredSize(new Dimension(580, 420));

		// Header card
		JPanel cardHeader = new JPanel(new BorderLayout());
		cardHeader.setBackground(COLOR_PRIMARY);
		cardHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

		// YÊU CẦU 2: ĐỒNG BỘ FONT CHỮ TRÊN TIÊU ĐỀ LÀ SEGOE UI CHUẨN
		JLabel lblH = new JLabel("Chi tiết yêu cầu – Nhân viên " + maNV);
		lblH.setFont(FONT_B15);
		lblH.setForeground(Color.WHITE);
		cardHeader.add(lblH, BorderLayout.WEST);

		// YÊU CẦU 3: THAY ĐỔI CHỮ "x" THÀNH LOGOCLOSE TRÊN GÓC PHẢI
		ImageIcon iconCloseDetail = new ImageIcon(new ImageIcon(getClass().getResource("/Images/logoClose.png")).getImage()
				.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JLabel lblX = new JLabel(iconCloseDetail);
		lblX.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblX.setOpaque(false);
		lblX.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				compareWindow.dispose();
			}
		});
		cardHeader.add(lblX, BorderLayout.EAST);
		card.add(cardHeader, BorderLayout.NORTH);

		// Body: GridBagLayout — không scroll, đủ chỗ cho 4 hàng
		JPanel body = new JPanel(new GridBagLayout());
		body.setBackground(Color.WHITE);
		body.setBorder(new EmptyBorder(18, 22, 10, 22));
		GridBagConstraints g = new GridBagConstraints();
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(5, 5, 5, 5);

		// Tiêu đề 3 cột
		Color colHeaderBg = new Color(245, 248, 252);
		String[] colTitles = { "Trường thông tin", "Thông tin hiện tại", "Thông tin mới" };
		double[] colWeights = { 0.26, 0.37, 0.37 };
		for (int c = 0; c < 3; c++) {
			JLabel lbl = new JLabel(colTitles[c], SwingConstants.CENTER);
			// YÊU CẦU 2: ĐỒNG BỘ TOÀN BỘ FONT CHỮ TRONG CHI TIẾT SANG SEGOE UI CHUẨN
			lbl.setFont(FONT_B14);
			lbl.setForeground(COLOR_PRIMARY);
			lbl.setOpaque(true);
			lbl.setBackground(colHeaderBg);
			lbl.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(210, 215, 224), 1),
					new EmptyBorder(8, 10, 8, 10)));
			g.gridx = c;
			g.gridy = 0;
			g.weightx = colWeights[c];
			body.add(lbl, g);
		}

		// 4 hàng dữ liệu
		for (int i = 0; i < fields.length; i++) {
			g.gridy = i + 1;
			boolean changed = !newVals[i].equals(oldData[i]);

			// Cột tên trường
			JLabel lblField = makeCellLabel(fields[i], false, false);
			g.gridx = 0;
			g.weightx = 0.26;
			body.add(lblField, g);

			// Cột thông tin cũ
			JLabel lblOld = makeCellLabel(oldData[i], false, false);
			g.gridx = 1;
			g.weightx = 0.37;
			body.add(lblOld, g);

			// Cột thông tin mới — highlight nếu thay đổi
			JLabel lblNew = makeCellLabel(newVals[i], changed, true);
			g.gridx = 2;
			g.weightx = 0.37;
			body.add(lblNew, g);
		}

		card.add(body, BorderLayout.CENTER);

		// Footer: Từ chối | Phê duyệt
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
		footer.setBackground(new Color(248, 250, 252));
		footer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(228, 232, 238), 1),
				new EmptyBorder(2, 10, 2, 10)));

		// YÊU CẦU 4: BỎ LOGO ICON, CHỈ ĐỂ CHỮ, TỰ CO GIÃN ĐỘ RỘNG VỪA KHÍT THEO TEXT KHÔNG BỊ CỐ ĐỊNH SIZE
		RoundedPanel btnTuChoi = makeActionBtn("Từ chối", RED_BADGE, Color.WHITE);
		RoundedPanel btnDuyet = makeActionBtn("Phê duyệt", new Color(39, 174, 96), Color.WHITE);

		final String[] newValsFinal = newVals;
		btnTuChoi.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				xuLyYeuCau(maNV, newValsFinal, "Từ chối");
				compareWindow.dispose();
				refresh();
				showCustomPopup("Đã TỪ CHỐI yêu cầu của nhân viên " + maNV + "!", false);
			}
		});
		btnDuyet.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				xuLyYeuCau(maNV, newValsFinal, "Đã duyệt");
				compareWindow.dispose();
				refresh();
				showCustomPopup("Đã PHÊ DUYỆT yêu cầu của nhân viên " + maNV + "!", true);
			}
		});

		footer.add(btnTuChoi);
		footer.add(btnDuyet);
		card.add(footer, BorderLayout.SOUTH);

		overlay.add(card);
		compareWindow.getContentPane().add(overlay);
		if (owner != null)
			compareWindow.setBounds(owner.getBounds());
		else {
			compareWindow.setSize(900, 650);
			compareWindow.setLocationRelativeTo(null);
		}
		compareWindow.setVisible(true);

		overlay.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (!card.getBounds().contains(e.getPoint()))
					compareWindow.dispose();
			}
		});
	}

	private JLabel makeCellLabel(String text, boolean highlight, boolean isNew) {
		JLabel lbl = new JLabel(text != null ? text : "");
		// YÊU CẦU 2: ĐỒNG BỘ TOÀN BỘ FONT CHỮ Ô DỮ LIỆU ĐỀU LÀ SEGOE UI CHUẨN
		lbl.setFont(highlight ? FONT_B14 : FONT_14);
		lbl.setForeground(highlight ? new Color(39, 174, 96) : new Color(60, 60, 60));
		lbl.setOpaque(highlight && isNew);
		if (highlight && isNew)
			lbl.setBackground(new Color(232, 248, 240));
		lbl.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(highlight ? new Color(39, 174, 96) : new Color(220, 220, 220), 1),
				new EmptyBorder(9, 10, 9, 10)));
		return lbl;
	}

	// =========================================================================
	// CARD 3: FOOTER
	// =========================================================================
	private JPanel buildFooterCard() {
		RoundedPanel card = new RoundedPanel(15, Color.WHITE);
		card.setLayout(new BorderLayout());
		card.setBorder(new EmptyBorder(8, 15, 10, 15));

		JLabel title = new JLabel("THAO TÁC CÀI ĐẶT");
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
		btnGrid.add(createFooterActionBtn("Cài đặt hệ thống", "",
				() -> showCustomPopup("Chức năng đang được phát triển!", false)));
		card.add(btnGrid, BorderLayout.CENTER);
		return card;
	}

	private JPanel createFooterActionBtn(String title, String subTxt, Runnable onClick) {
		RoundedPanel btn = new RoundedPanel(12, new Color(238, 246, 255));
		btn.setLayout(new BorderLayout(15, 0));
		btn.setBorder(new EmptyBorder(10, 15, 10, 15));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setOpaque(false);
		JLabel lblT = new JLabel(title);
		lblT.setFont(FONT_B15);
		lblT.setForeground(COLOR_PRIMARY);
		textPanel.add(lblT);
		if (!subTxt.isEmpty()) {
			textPanel.add(Box.createVerticalStrut(3));
			JLabel lblS = new JLabel(subTxt);
			lblS.setFont(FONT_14);
			lblS.setForeground(Color.GRAY);
			textPanel.add(lblS);
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

	// =========================================================================
	// DATABASE — lịch sử lưu DB, không mất sau đăng xuất
	// =========================================================================
	public void refresh() {
		currentMaNV = AuthService.getCurrentMaNV();
		if (currentMaNV == null || currentMaNV.isEmpty())
			currentMaNV = "NV001";
		loadManagerData();
		loadLichSuHoatDong(); // load từ DB
		updateBadgeCount();
	}

	private void updateBadgeCount() {
		int count = 0;
		try (Connection con = Connect_DB.getConnection()) {
			PreparedStatement ps = con
					.prepareStatement("SELECT COUNT(*) FROM YeuCauCapNhat WHERE trangThai = 'Chờ duyệt'");
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				count = rs.getInt(1);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		Container parent = btnThongBao.getParent();
		if (parent != null) {
			parent.remove(btnThongBao);
			final int fc = count;
			btnThongBao = createBellButton("/Images/logoThongBao.png", fc);
			btnThongBao.addActionListener(e -> toggleDropdown());
			parent.add(btnThongBao, BorderLayout.EAST);
			parent.revalidate();
			parent.repaint();
		}
	}

	private java.util.List<String[]> loadYeuCauList() {
		java.util.List<String[]> list = new java.util.ArrayList<>();
		try (Connection con = Connect_DB.getConnection()) {
			PreparedStatement ps = con.prepareStatement("SELECT maNV, hoTenMoi, sdtMoi, emailMoi, diaChiMoi "
					+ "FROM YeuCauCapNhat WHERE trangThai = 'Chờ duyệt' ORDER BY ngayTao DESC");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new String[] { rs.getString("maNV"), rs.getString("hoTenMoi"), rs.getString("sdtMoi"),
						rs.getString("emailMoi"), rs.getString("diaChiMoi") });
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return list;
	}

	private void loadManagerData() {
		try (Connection con = Connect_DB.getConnection()) {
			if (con == null)
				return;
			PreparedStatement ps = con
					.prepareStatement("SELECT maNV, hoTenNV, loaiNV, ngaySinh, gioiTinh, soDT, email, diaChi "
							+ "FROM NhanVien WHERE maNV = ?");
			ps.setString(1, currentMaNV);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				tfMaNV.setText(rs.getString("maNV"));
				tfHoTen.setText(rs.getString("hoTenNV"));
				String loai = rs.getString("loaiNV");
				tfVaiTro.setText("NHAN_VIEN_QUAN_LY".equalsIgnoreCase(loai) ? "Quản lý" : "Bán vé");
				java.sql.Date ns = rs.getDate("ngaySinh");
				tfNgaySinh.setText(ns != null ? new SimpleDateFormat("dd/MM/yyyy").format(ns) : "");
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

	/** Load lịch sử từ DB — không mất sau đăng xuất */
	private void loadLichSuHoatDong() {
		if (modelLichSu == null)
			return;
		modelLichSu.setRowCount(0);
		try (Connection con = Connect_DB.getConnection()) {
			PreparedStatement ps = con.prepareStatement("SELECT TOP 50 thoiGian, hanhDong, trangThai "
					+ "FROM LichSuHoatDong WHERE maNV = ? ORDER BY thoiGian DESC");
			ps.setString(1, currentMaNV);
			ResultSet rs = ps.executeQuery();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			while (rs.next()) {
				Timestamp ts = rs.getTimestamp("thoiGian");
				modelLichSu.addRow(new Object[] { ts != null ? sdf.format(ts) : "", rs.getString("hanhDong"),
						rs.getString("trangThai") });
			}
		} catch (SQLException ex) {
			// Bảng chưa tồn tại → bỏ qua, không crash
			ex.printStackTrace();
		}
	}

	private boolean saveManagerData() {
		try (Connection con = Connect_DB.getConnection()) {
			if (con == null)
				return false;
			PreparedStatement ps = con
					.prepareStatement("UPDATE NhanVien SET hoTenNV=?, soDT=?, email=?, diaChi=? WHERE maNV=?");
			ps.setString(1, tfHoTen.getText().trim());
			ps.setString(2, tfSdt.getText().trim());
			ps.setString(3, tfEmail.getText().trim());
			ps.setString(4, tfDiaChi.getText().trim());
			ps.setString(5, currentMaNV);
			ps.executeUpdate();
			ghiLogDB(con, "Cập nhật hồ sơ cá nhân");
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			showCustomPopup("Lỗi: " + ex.getMessage(), false);
			return false;
		}
	}

	private void xuLyYeuCau(String maNVDuocDuyet, String[] newVals, String action) {
		try (Connection con = Connect_DB.getConnection()) {
			if ("Đã duyệt".equals(action)) {
				PreparedStatement ps1 = con
						.prepareStatement("UPDATE NhanVien SET hoTenNV=?, soDT=?, email=?, diaChi=? WHERE maNV=?");
				ps1.setString(1, newVals[0]);
				ps1.setString(2, newVals[1]);
				ps1.setString(3, newVals[2]);
				ps1.setString(4, newVals[3]);
				ps1.setString(5, maNVDuocDuyet);
				ps1.executeUpdate();
			}
			PreparedStatement ps2 = con
					.prepareStatement("UPDATE YeuCauCapNhat SET trangThai=? WHERE maNV=? AND trangThai='Chờ duyệt'");
			ps2.setString(1, action);
			ps2.setString(2, maNVDuocDuyet);
			ps2.executeUpdate();
			ghiLogDB(con, action + " yêu cầu cập nhật hồ sơ của NV " + maNVDuocDuyet);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/** Ghi log vào DB — bền vững qua các phiên đăng nhập */
	private void ghiLogDB(Connection con, String hanhDong) throws SQLException {
		// Tạo maLog tự động
		String maLog = "LOG" + System.currentTimeMillis() % 1000000;
		try {
			PreparedStatement ps = con
					.prepareStatement("INSERT INTO LichSuHoatDong (maLog, maNV, thoiGian, hanhDong, trangThai) "
							+ "VALUES (?, ?, GETDATE(), ?, N'Thành công')");
			ps.setString(1, maLog);
			ps.setString(2, currentMaNV);
			ps.setNString(3, hanhDong);
			ps.executeUpdate();
		} catch (SQLException ex) {
			// Bảng chưa tồn tại → bỏ qua
		}
		// Cập nhật UI ngay lập tức
		String ts = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
		if (modelLichSu != null)
			modelLichSu.insertRow(0, new Object[] { ts, hanhDong, "Thành công" });
	}

	// =========================================================================
	// UI UTILITIES
	// =========================================================================
	// YÊU CẦU 4: BỎ CỐ ĐỊNH KÍCH THƯỚC NÚT (SỬ DỤNG FlowLayout(CENTER)), THAY ĐỔI COMPONENT ĐỂ TỰ ĐỘNG CO GIÃN ĐỘ RỘNG THEO CHỮ KHÔNG CÓ ICON
	private RoundedPanel makeActionBtn(String text, Color bg, Color fg) {
		RoundedPanel p = new RoundedPanel(8, bg);
		p.setBorder(new EmptyBorder(9, 20, 9, 20));
		p.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		p.setCursor(new Cursor(Cursor.HAND_CURSOR));
		JLabel lbl = new JLabel(text);
		lbl.setFont(FONT_B14);
		lbl.setForeground(fg);
		p.add(lbl);
		p.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				p.setBgColor(bg.darker());
				p.repaint();
			}

			public void mouseExited(MouseEvent e) {
				p.setBgColor(bg);
				p.repaint();
			}
		});
		return p;
	}

	private void showCustomPopup(String message, boolean isSuccess) {
		JDialog dlg = new JDialog();
		dlg.setUndecorated(true);
		dlg.setBackground(new Color(0, 0, 0, 0));
		dlg.setAlwaysOnTop(true);
		Color bg = isSuccess ? new Color(39, 174, 96) : new Color(231, 76, 60);

		JPanel pnl = new JPanel() {
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
			}
		};
		pnl.setOpaque(false);
		pnl.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 14));
		JLabel lblIcon = new JLabel(isSuccess ? "✔" : "✕");
		lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblIcon.setForeground(Color.WHITE);
		JLabel lblMsg = new JLabel(message);
		lblMsg.setFont(FONT_B14);
		lblMsg.setForeground(Color.WHITE);
		pnl.add(lblIcon);
		pnl.add(lblMsg);
		dlg.add(pnl);
		dlg.pack();
		dlg.setMinimumSize(new Dimension(dlg.getWidth() + 10, dlg.getHeight()));
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		dlg.setLocation(screen.width - dlg.getWidth() - 20, screen.height - dlg.getHeight() - 60);
		new Timer(2800, e -> dlg.dispose()) {
			{
				setRepeats(false);
				start();
			}
		};
		dlg.setVisible(true);
	}

	private JButton createBellButton(String iconPath, int badgeCount) {
		JButton btn = new JButton() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (getModel().isRollover()) {
					g2.setColor(new Color(235, 235, 235));
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				}
				super.paintComponent(g);
				if (badgeCount > 0) {
					int bs = 18, x = getWidth() - bs - 2, y = 2;
					g2.setColor(RED_BADGE);
					g2.fillOval(x, y, bs, bs);
					g2.setColor(Color.WHITE);
					g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
					FontMetrics fm = g2.getFontMetrics();
					String t = badgeCount > 99 ? "99+" : String.valueOf(badgeCount);
					g2.drawString(t, x + (bs - fm.stringWidth(t)) / 2, y + (bs - fm.getHeight()) / 2 + fm.getAscent());
				}
				g2.dispose();
			}
		};
		try {
			java.net.URL url = getClass().getResource(iconPath);
			if (url != null)
				btn.setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
		} catch (Exception ignored) {
		}
		btn.setPreferredSize(new Dimension(45, 45));
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setToolTipText(badgeCount > 0 ? "Bạn có " + badgeCount + " yêu cầu cần duyệt" : "Không có thông báo mới");
		return btn;
	}

	// =========================================================================
	// ROUNDED PANEL
	// =========================================================================
	class RoundedPanel extends JPanel {
		private int radius;
		private Color bgColor;

		public RoundedPanel(int r, Color c) {
			radius = r;
			bgColor = c;
			setOpaque(false);
		}

		public void setBgColor(Color c) {
			bgColor = c;
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