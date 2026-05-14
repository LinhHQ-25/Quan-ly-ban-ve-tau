package gui;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import connect_DB.Connect_DB;

public class DatVeGUI2 extends JPanel {

	private static final Color NAVY = new Color(28, 57, 110);
	private static final Color BG = new Color(242, 247, 252);
	private static final Color BORDER_C = new Color(180, 205, 230);
	private static final Font FONT_14 = new Font("Segoe UI", Font.PLAIN, 14);
	private static final Font FONT_BOLD_14 = new Font("Segoe UI", Font.BOLD, 14);

	private JTextField txtSdt, txtHoTen, txtIdCard, txtEmail, txtNamSinh;
	private JComboBox<String> cbIdType, cbLoaiDoiTuong;
	private JTable tblVe;
	private DefaultTableModel modelVe;
	private JButton btnXacNhan, btnQuayLai, btnTiepTuc;

	private List<String> danhSachGhe;
	private String loaiVe;
	private Runnable onQuayLai;

	public DatVeGUI2(List<String> danhSachGhe, String loaiVe, Runnable onQuayLai) {
		this.danhSachGhe = danhSachGhe;
		this.loaiVe = loaiVe;
		this.onQuayLai = onQuayLai;

		// KHÔNG MARGIN / PADDING THEO YÊU CẦU
		setLayout(new BorderLayout(0, 0));
		setBackground(BG);
		setBorder(new EmptyBorder(0, 0, 0, 0));

		add(buildTopForm(), BorderLayout.NORTH);
		add(buildCenterTable(), BorderLayout.CENTER);
		add(buildBotBar(), BorderLayout.SOUTH);
		
		initTableData();
	}

	// =====================================================================
	// KHUNG 1: FORM NHẬP THÔNG TIN (CHUẨN FIGMA)
	// =====================================================================
	private JPanel buildTopForm() {
		JPanel pnlWrapper = new JPanel(new BorderLayout(0, 0));
		pnlWrapper.setOpaque(false);
		pnlWrapper.setBorder(new EmptyBorder(10, 10, 5, 10)); // Đệm nhẹ viền ngoài cho form thở

		// Tab Header "Thông tin hành khách/vé"
		JPanel titlePnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePnl.setOpaque(false);
		JLabel lblTitle = new JLabel("Thông tin hành khách/vé");
		lblTitle.setFont(FONT_BOLD_14);
		lblTitle.setForeground(Color.WHITE);
		lblTitle.setOpaque(true);
		lblTitle.setBackground(NAVY);
		lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
		titlePnl.add(lblTitle);
		pnlWrapper.add(titlePnl, BorderLayout.NORTH);

		// Nội dung Form (Background trắng, viền xanh)
		JPanel pnlForm = new JPanel(new GridBagLayout());
		pnlForm.setBackground(Color.WHITE);
		pnlForm.setBorder(new LineBorder(BORDER_C, 1));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.WEST;

		// --- Khởi tạo Components ---
		txtSdt = createTextField();
		txtSdt.addFocusListener(new FocusAdapter() {
			@Override public void focusLost(FocusEvent e) { checkKhachHang(); }
		});
		txtSdt.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) { if(e.getKeyCode() == KeyEvent.VK_ENTER) checkKhachHang(); }
		});
		
		txtHoTen = createTextField();
		
		// Combo Chọn CCCD / Hộ chiếu bọc chung Textfield
		cbIdType = new JComboBox<>(new String[]{"CCCD", "Hộ chiếu"});
		cbIdType.setFont(FONT_14);
		cbIdType.setBackground(Color.WHITE);
		cbIdType.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_C));
		txtIdCard = new JTextField();
		txtIdCard.setFont(FONT_14);
		txtIdCard.setBorder(new EmptyBorder(0, 5, 0, 5));
		
		JPanel pnlIdCard = new JPanel(new BorderLayout());
		pnlIdCard.setBackground(Color.WHITE);
		pnlIdCard.setBorder(new LineBorder(BORDER_C, 1));
		pnlIdCard.setPreferredSize(new Dimension(200, 32));
		pnlIdCard.add(cbIdType, BorderLayout.WEST);
		pnlIdCard.add(txtIdCard, BorderLayout.CENTER);

		txtEmail = createTextField();
		
		cbLoaiDoiTuong = new JComboBox<>(new String[]{
			"Người lớn", "Trẻ em (<6 tuổi)", "Trẻ em (6-10 tuổi)", "Sinh viên", "Người cao tuổi"
		});
		cbLoaiDoiTuong.setFont(FONT_14);
		cbLoaiDoiTuong.setBackground(Color.WHITE);
		cbLoaiDoiTuong.setPreferredSize(new Dimension(150, 32));

		txtNamSinh = createTextField();
		
		btnXacNhan = new JButton("Xác nhận");
		btnXacNhan.setFont(FONT_BOLD_14);
		btnXacNhan.setBackground(NAVY);
		btnXacNhan.setForeground(Color.WHITE);
		btnXacNhan.setFocusPainted(false);
		btnXacNhan.setBorder(new EmptyBorder(6, 20, 6, 20));
		btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnXacNhan.addActionListener(e -> capNhatVaoBang());

		// --- Layout Row 1 ---
		gbc.weightx = 0.15; addFormItem(pnlForm, gbc, 0, 0, "Số điện thoại", txtSdt);
		gbc.weightx = 0.25; addFormItem(pnlForm, gbc, 1, 0, "Họ và tên", txtHoTen);
		gbc.weightx = 0.25; addFormItem(pnlForm, gbc, 2, 0, "CCCD/Hộ chiếu", pnlIdCard);
		gbc.weightx = 0.2;  addFormItem(pnlForm, gbc, 3, 0, "Email", txtEmail);
		gbc.weightx = 0.15; addFormItem(pnlForm, gbc, 4, 0, "Loại đối tượng", cbLoaiDoiTuong);

		// --- Layout Row 2 ---
		gbc.weightx = 0.15; addFormItem(pnlForm, gbc, 0, 1, "Năm sinh", txtNamSinh);
		
		// Căn nút Xác nhận sang phải cùng
		gbc.gridx = 4; gbc.gridy = 1; 
		gbc.weightx = 0.15;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.fill = GridBagConstraints.NONE;
		pnlForm.add(btnXacNhan, gbc);

		pnlWrapper.add(pnlForm, BorderLayout.CENTER);
		return pnlWrapper;
	}

	private JTextField createTextField() {
		JTextField txt = new JTextField();
		txt.setPreferredSize(new Dimension(150, 32));
		txt.setFont(FONT_14);
		txt.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(BORDER_C),
				new EmptyBorder(2, 8, 2, 8)));
		return txt;
	}

	private void addFormItem(JPanel pnl, GridBagConstraints gbc, int x, int y, String label, JComponent comp) {
		gbc.gridx = x; gbc.gridy = y; 
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JPanel wrap = new JPanel(new BorderLayout(0, 4));
		wrap.setOpaque(false);
		JLabel lbl = new JLabel(label);
		lbl.setFont(FONT_14);
		lbl.setForeground(new Color(40, 40, 40));
		wrap.add(lbl, BorderLayout.NORTH);
		wrap.add(comp, BorderLayout.CENTER);
		pnl.add(wrap, gbc);
	}

	// =====================================================================
	// KHUNG 2: BẢNG DANH SÁCH VÉ (CAO TỐI ĐA)
	// =====================================================================
	private JPanel buildCenterTable() {
		JPanel pnlWrapper = new JPanel(new BorderLayout(0, 0));
		pnlWrapper.setOpaque(false);
		pnlWrapper.setBorder(new EmptyBorder(5, 10, 10, 10));

		// Tab Header "Danh sách vé"
		JPanel titlePnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePnl.setOpaque(false);
		JLabel lblTitle = new JLabel("Danh sách vé");
		lblTitle.setFont(FONT_BOLD_14);
		lblTitle.setForeground(Color.WHITE);
		lblTitle.setOpaque(true);
		lblTitle.setBackground(NAVY);
		lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
		titlePnl.add(lblTitle);
		pnlWrapper.add(titlePnl, BorderLayout.NORTH);

		// Tạo Bảng theo chuẩn Figma
		String[] cols = {"STT", "Mã vé", "Loại vé", "Mã ghế", "Họ tên", "CCCD/Hộ chiếu", "SĐT", "Loại đối tượng"};
		modelVe = new DefaultTableModel(cols, 0) {
			@Override public boolean isCellEditable(int row, int column) { return false; }
		};
		tblVe = new JTable(modelVe);
		tblVe.setRowHeight(32);
		tblVe.setFont(FONT_14);
		tblVe.setSelectionBackground(new Color(210, 230, 255));
		
		JTableHeader header = tblVe.getTableHeader();
		header.setFont(FONT_BOLD_14);
		header.setBackground(new Color(245, 245, 245));
		header.setPreferredSize(new Dimension(0, 36));

		tblVe.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && tblVe.getSelectedRow() != -1) {
				loadRowToForm(tblVe.getSelectedRow());
			}
		});

		JScrollPane scroll = new JScrollPane(tblVe);
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setBorder(new LineBorder(BORDER_C, 1));

		pnlWrapper.add(scroll, BorderLayout.CENTER);
		return pnlWrapper;
	}

	// =====================================================================
	// PHÁT SINH MÃ VÉ NGẪU NHIÊN 9 KÝ TỰ & KHÔNG TRÙNG SQL
	// =====================================================================
	private String generateUniqueMaVe() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random rnd = new Random();
		while (true) {
			StringBuilder sb = new StringBuilder(9);
			for (int i = 0; i < 9; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
			String code = sb.toString();
			if (!isMaVeExists(code)) return code;
		}
	}

	private boolean isMaVeExists(String maVe) {
		String sql = "SELECT 1 FROM Ve WHERE maVe = ?";
		try (Connection con = Connect_DB.getInstance().getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, maVe);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
			return true; // Trả về true để vòng lặp sinh lại mã khác cho an toàn nếu lỗi DB
		}
	}

	private void initTableData() {
		for (int i = 0; i < danhSachGhe.size(); i++) {
			modelVe.addRow(new Object[]{
				i + 1, 
				generateUniqueMaVe(), // Tự động phát sinh mã vé 9 ký tự
				loaiVe, 
				danhSachGhe.get(i),   
				"", "", "", ""        
			});
		}
		if (tblVe.getRowCount() > 0) {
			tblVe.setRowSelectionInterval(0, 0); 
		}
	}

	// =====================================================================
	// LOGIC ĐIỀN FORM VÀ KIỂM TRA SQL
	// =====================================================================
	private void checkKhachHang() {
		String phone = txtSdt.getText().trim();
		if (phone.isEmpty()) return;

		String sql = "SELECT hoTenKH, cccd, email, namSinh, laSinhVien FROM KhachHang WHERE sdt = ?";
		try (Connection con = Connect_DB.getInstance().getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			
			ps.setString(1, phone);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				txtHoTen.setText(rs.getString("hoTenKH"));
				String cccd = rs.getString("cccd");
				if(cccd != null) {
					txtIdCard.setText(cccd);
					cbIdType.setSelectedIndex(cccd.matches(".*[a-zA-Z]+.*") ? 1 : 0);
				}
				txtEmail.setText(rs.getString("email"));
				java.sql.Date dob = rs.getDate("namSinh");
				if (dob != null) {
					LocalDate ld = dob.toLocalDate();
					txtNamSinh.setText(String.format("%02d/%02d/%04d", ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear()));
					int age = LocalDate.now().getYear() - ld.getYear();
					if (rs.getBoolean("laSinhVien")) cbLoaiDoiTuong.setSelectedItem("Sinh viên");
					else if (age < 6) cbLoaiDoiTuong.setSelectedItem("Trẻ em (<6 tuổi)");
					else if (age <= 10) cbLoaiDoiTuong.setSelectedItem("Trẻ em (6-10 tuổi)");
					else if (age >= 60) cbLoaiDoiTuong.setSelectedItem("Người cao tuổi");
					else cbLoaiDoiTuong.setSelectedItem("Người lớn");
				}
			} else {
				txtHoTen.setText("");
				txtIdCard.setText("");
				txtEmail.setText("");
				txtNamSinh.setText("");
				cbLoaiDoiTuong.setSelectedIndex(0); 
			}
		} catch (Exception ex) { ex.printStackTrace(); }
	}

	private void capNhatVaoBang() {
		int row = tblVe.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 ghế trong bảng dưới để điền thông tin!");
			return;
		}
		if(txtHoTen.getText().trim().isEmpty() || txtSdt.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Họ tên và Số điện thoại không được để trống!");
			return;
		}

		modelVe.setValueAt(txtHoTen.getText().trim(), row, 4);
		modelVe.setValueAt(txtIdCard.getText().trim(), row, 5);
		modelVe.setValueAt(txtSdt.getText().trim(), row, 6);
		modelVe.setValueAt(cbLoaiDoiTuong.getSelectedItem().toString(), row, 7);

		if (row < tblVe.getRowCount() - 1) {
			tblVe.setRowSelectionInterval(row + 1, row + 1);
		} else {
			JOptionPane.showMessageDialog(this, "Đã điền đủ thông tin cho tất cả các vé!");
		}
	}

	private void loadRowToForm(int row) {
		String hoten = (String) modelVe.getValueAt(row, 4);
		String cccd = (String) modelVe.getValueAt(row, 5);
		String sdt = (String) modelVe.getValueAt(row, 6);
		String loaiDt = (String) modelVe.getValueAt(row, 7);

		txtHoTen.setText(hoten != null ? hoten : "");
		txtIdCard.setText(cccd != null ? cccd : "");
		txtSdt.setText(sdt != null ? sdt : "");
		if (loaiDt != null && !loaiDt.isEmpty()) cbLoaiDoiTuong.setSelectedItem(loaiDt);
		
		if(sdt != null && !sdt.isEmpty()) checkKhachHang(); 
		else { txtEmail.setText(""); txtNamSinh.setText(""); }
	}

	// =====================================================================
	// KHUNG 3: BOTTOM BAR (CLONE CHUẨN XÁC TỪ DATVEGUI1 Y CHANG YÊU CẦU)
	// =====================================================================
	private JPanel buildBotBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

		btnQuayLai = makeOutlineBtn("Quay lại", loadAndScaleIcon("/Images/logoBack.png", 14, 14));
		btnQuayLai.addActionListener(e -> { if (onQuayLai != null) onQuayLai.run(); });

		btnTiepTuc = makeNavyBtn("Tiếp tục", loadAndScaleIcon("/Images/logoGoOn.png", 14, 14));
		btnTiepTuc.addActionListener(e -> {
			for(int i=0; i<tblVe.getRowCount(); i++) {
				String ten = (String) tblVe.getValueAt(i, 4);
				if(ten == null || ten.isEmpty()) {
					JOptionPane.showMessageDialog(this, "Bạn chưa điền đủ thông tin hành khách cho vé số " + (i+1));
					tblVe.setRowSelectionInterval(i, i);
					return;
				}
			}
			JOptionPane.showMessageDialog(this, "Thông tin hợp lệ!\nChuyển sang màn hình Thanh Toán...");
			// TODO: Gọi logic sang trang GUI3 ở đây
		});

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); 
		left.setBackground(Color.WHITE); 
		left.add(btnQuayLai);

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4)); 
		right.setBackground(Color.WHITE);
		right.add(btnTiepTuc);

		bar.add(left, BorderLayout.WEST);
		bar.add(right, BorderLayout.EAST);
		return bar;
	}

	// Các hàm vẽ nút y hệt DatVeGUI1
	private Icon loadAndScaleIcon(String path, int w, int h) {
		try {
			java.net.URL url = getClass().getResource(path);
			if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
		} catch (Exception ignored) {}
		return null;
	}

	private JButton makeOutlineBtn(String text, Icon icon) {
		JButton b = new JButton(text) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				boolean en = isEnabled();
				g2.setColor(en ? (getModel().isPressed() ? new Color(198, 215, 242) : getModel().isRollover() ? new Color(212, 228, 250) : new Color(226, 236, 252)) : new Color(238, 241, 248));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(en ? NAVY : new Color(175, 185, 205));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				g2.dispose(); super.paintComponent(g);
			}
		};
		b.setIcon(icon); b.setFont(FONT_14); b.setForeground(NAVY);
		b.setIconTextGap(8); b.setBorder(new EmptyBorder(6, 16, 6, 16));
		b.setContentAreaFilled(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JButton makeNavyBtn(String text, Icon icon) {
		JButton b = new JButton(text) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? new Color(18, 42, 85) : getModel().isRollover() ? new Color(38, 68, 128) : NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.dispose(); super.paintComponent(g);
			}
		};
		b.setIcon(icon); b.setFont(FONT_BOLD_14); b.setForeground(Color.WHITE);
		b.setHorizontalTextPosition(SwingConstants.LEFT); // Chữ bên trái icon
		b.setIconTextGap(8); b.setBorder(new EmptyBorder(6, 16, 6, 16));
		b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}
}