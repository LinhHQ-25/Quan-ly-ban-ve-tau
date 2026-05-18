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
import javax.swing.table.*;

import connect_DB.Connect_DB;

public class DatVeGUI2 extends JPanel {

	private static final Color NAVY = new Color(28, 57, 110);
	private static final Color BG = new Color(242, 247, 252);
	private static final Color BORDER_C = new Color(180, 205, 230);
	private static final Color ERR_C = new Color(220, 50, 50);
	private static final Font FONT_14 = new Font("Segoe UI", Font.PLAIN, 14);
	private static final Font FONT_B14 = new Font("Segoe UI", Font.BOLD, 14);

	// ── Regex ràng buộc ──────────────────────────────────────────
	private static final String REGEX_SDT = "^0[35789][0-9]{8}$";
	private static final String REGEX_HOTEN = "^[\\p{L}\\s\\-']{2,50}$";
	private static final String REGEX_CCCD = "^[0-9]{12}$";
	private static final String REGEX_HC = "^[A-Z][0-9A-Z]{7}$";
	private static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
	private static final String REGEX_NAM = "^(19|20)\\d{2}$";
	private static final String REGEX_NGAY = "^\\d{2}/\\d{2}/(19|20)\\d{2}$";

	// ── Form fields ──────────────────────────────────────────────
	private JTextField txtSdt, txtHoTen, txtIdCard, txtEmail, txtNamSinh;
	private JTextField txtLoaiDoiTuong;
	private JRadioButton rdoCccd, rdoHoChieu;
	private JComboBox<String> cbLoaiDoiTuong;
	private JCheckBox chkSinhVien;
	private JButton btnXacNhan, btnQuayLai, btnTiepTuc;

	// ── Table ────────────────────────────────────────────────────
	private JTable tblVe;
	private DefaultTableModel modelVe;
	private JComboBox<String> cbFilterChieu;

	// ── Timer ────────────────────────────────────────────────────
	private JLabel lblCountdown;
	private javax.swing.Timer countdownTimer;
	private int secondsLeft = 30 * 60;

	// ── Data ─────────────────────────────────────────────────────
	private List<String> danhSachGhe;
	private String loaiVe;
	private Runnable onQuayLai;
	private boolean khachTonTai = false;

	// ── Column indices ───────────────────────────────────────────
	private static final int COL_STT = 0;
	private static final int COL_MAVE = 1;
	private static final int COL_LOAIVE = 2;
	private static final int COL_CHIEU = 3;
	private static final int COL_MAGHE = 4;
	private static final int COL_HOTEN = 5;
	private static final int COL_CCCD = 6;
	private static final int COL_SDT = 7;
	private static final int COL_LOAIDT = 8;
	// Thêm cột email & namSinh ẩn trong model để load lại form
	private static final int COL_EMAIL = 9;
	private static final int COL_NAMSINH = 10;
	private static final int COL_LASISV = 11;

	// ── Flag tránh loadRowToForm trigger checkKhachHang lặp vòng ─
	private boolean isLoadingRow = false;

	// ─────────────────────────────────────────────────────────────
	public DatVeGUI2(List<String> danhSachGhe, String loaiVe, Runnable onQuayLai) {
		this.danhSachGhe = danhSachGhe;
		this.loaiVe = loaiVe;
		this.onQuayLai = onQuayLai;

		setLayout(new BorderLayout(0, 0));
		setBackground(BG);

		add(buildTopForm(), BorderLayout.NORTH);
		add(buildCenterTable(), BorderLayout.CENTER);
		add(buildBotBar(), BorderLayout.SOUTH);

		initTableData();
		startCountdown();
	}

	// =========================================================
	// FORM
	// =========================================================
	private JPanel buildTopForm() {
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		wrapper.setBorder(new EmptyBorder(10, 10, 5, 10));

		// Title bar
		JPanel titlePnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePnl.setOpaque(false);
		JLabel lblTitle = new JLabel("Thông tin hành khách/vé");
		lblTitle.setFont(FONT_B14);
		lblTitle.setForeground(Color.WHITE);
		lblTitle.setOpaque(true);
		lblTitle.setBackground(NAVY);
		lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
		titlePnl.add(lblTitle);
		wrapper.add(titlePnl, BorderLayout.NORTH);

		JPanel pnlForm = new JPanel(new GridBagLayout());
		pnlForm.setBackground(Color.WHITE);
		pnlForm.setBorder(new LineBorder(BORDER_C, 1));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 10, 6, 10);
		gbc.anchor = GridBagConstraints.WEST;

		// ── SỐ ĐIỆN THOẠI ──────────────────────────────────────
		txtSdt = createTextField();
		txtSdt.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE)
					e.consume();
				if (txtSdt.getText().length() >= 10 && c != KeyEvent.VK_BACK_SPACE)
					e.consume();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String phone = txtSdt.getText().trim();
					// 1. Validate regex
					if (!phone.matches(REGEX_SDT)) {
						showErrMsg(txtSdt, "Vui lòng kiểm tra thông tin \"Số điện thoại\"");
						return;
					}
					// 2. Tra DB
					String sql = "SELECT hoTenKH, cccd, email, namSinh, laSinhVien FROM KhachHang WHERE sdt = ?";
					boolean found = false;
					try (Connection con = Connect_DB.getInstance().getConnection();
							PreparedStatement ps = con.prepareStatement(sql)) {
						ps.setString(1, phone);
						ResultSet rs = ps.executeQuery();
						if (rs.next()) {
							found = true;
							khachTonTai = true;
							// Fill toàn bộ thông tin lên form
							txtHoTen.setText(rs.getString("hoTenKH"));
							String cccd2 = rs.getString("cccd");
							if (cccd2 != null && !cccd2.isEmpty()) {
								txtIdCard.setText(cccd2);
								boolean isHc = cccd2.matches(REGEX_HC);
								rdoHoChieu.setSelected(isHc);
								rdoCccd.setSelected(!isHc);
							} else {
								txtIdCard.setText("");
								rdoCccd.setSelected(true);
							}
							txtEmail.setText(rs.getString("email") != null ? rs.getString("email") : "");
							java.sql.Date dob = rs.getDate("namSinh");
							if (dob != null) {
								LocalDate ld = dob.toLocalDate();
								txtNamSinh.setText(String.format("%02d/%02d/%04d", ld.getDayOfMonth(),
										ld.getMonthValue(), ld.getYear()));
								boolean laSV = rs.getBoolean("laSinhVien");
								chkSinhVien.setSelected(laSV);
								if (laSV)
									setLoaiDoiTuong("Sinh viên");
								else {
									int age = LocalDate.now().getYear() - ld.getYear();
									if (age < 6)
										setLoaiDoiTuong("Trẻ em (<6 tuổi)");
									else if (age <= 10)
										setLoaiDoiTuong("Trẻ em (6-10 tuổi)");
									else if (age >= 60)
										setLoaiDoiTuong("Người cao tuổi");
									else
										setLoaiDoiTuong("Người lớn");
								}
							} else
								txtNamSinh.setText("");
							setFormEditable(true);
							btnXacNhan.requestFocus();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					if (!found) {
						// Không có trong DB → clear và nhảy sang họ tên
						khachTonTai = false;
						txtHoTen.setText("");
						txtIdCard.setText("");
						txtEmail.setText("");
						txtNamSinh.setText("");
						txtLoaiDoiTuong.setText("");
						chkSinhVien.setSelected(false);
						rdoCccd.setSelected(true);
						setFormEditable(true);
						txtHoTen.requestFocus();
					}
				}
			}
		});

		// ── HỌ TÊN ─────────────────────────────────────────────
		txtHoTen = createTextField();
		txtHoTen.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String hoten = txtHoTen.getText().trim();
					if (hoten.isEmpty()) {
						showErrMsg(txtHoTen, "Vui lòng kiểm tra thông tin \"Họ và tên\"");
						return;
					}
					if (!hoten.matches(REGEX_HOTEN)) {
						showErrMsg(txtHoTen, "Vui lòng kiểm tra thông tin \"Họ và tên\"");
						return;
					}
					txtIdCard.requestFocus();
				}
			}
		});

		// ── CCCD / HỘ CHIẾU ────────────────────────────────────
		txtIdCard = createTextField();
		txtIdCard.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String id = txtIdCard.getText().trim();
					if (id.isEmpty()) {
						showErrMsg(txtIdCard,
								"Vui lòng kiểm tra thông tin \"" + (rdoCccd.isSelected() ? "CCCD" : "Hộ chiếu") + "\"");
						return;
					}
					if (rdoCccd.isSelected()) {
						if (!id.matches(REGEX_CCCD)) {
							showErrMsg(txtIdCard, "Vui lòng kiểm tra thông tin \"CCCD\"");
							return;
						}
					} else {
						if (!id.matches(REGEX_HC)) {
							showErrMsg(txtIdCard, "Vui lòng kiểm tra thông tin \"Hộ chiếu\"");
							return;
						}
					}
					txtEmail.requestFocus();
				}
			}
		});

		rdoCccd = new JRadioButton("CCCD", true);
		rdoHoChieu = new JRadioButton("Hộ chiếu", false);
		rdoCccd.setFont(FONT_14);
		rdoCccd.setOpaque(false);
		rdoHoChieu.setFont(FONT_14);
		rdoHoChieu.setOpaque(false);

		ButtonGroup grpId = new ButtonGroup();
		grpId.add(rdoCccd);
		grpId.add(rdoHoChieu);

		// ĐÚNG THỨ TỰ — pnlRadioRow trước, pnlIdCard sau
		JPanel pnlRadioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		pnlRadioRow.setOpaque(false);
		pnlRadioRow.add(rdoCccd);
		pnlRadioRow.add(rdoHoChieu);

		/// THAY TOÀN BỘ pnlIdCard
		JPanel pnlIdCard = new JPanel(new GridBagLayout());
		pnlIdCard.setOpaque(false);

		GridBagConstraints idGbc = new GridBagConstraints();
		idGbc.gridx = 0;
		idGbc.gridy = 0;
		idGbc.weightx = 1;
		idGbc.weighty = 0;
		idGbc.fill = GridBagConstraints.HORIZONTAL;
		idGbc.anchor = GridBagConstraints.NORTH;
		pnlIdCard.add(pnlRadioRow, idGbc);

		// SAU
		idGbc.gridy = 1;
		idGbc.weighty = 0;
		idGbc.anchor = GridBagConstraints.NORTH;
		idGbc.insets = new Insets(-5, 0, 0, 0);
		idGbc.fill = GridBagConstraints.HORIZONTAL;
		pnlIdCard.add(txtIdCard, idGbc);

		// Thêm ngay sau dòng trên
		idGbc.gridy = 2;
		idGbc.weighty = 1;
		idGbc.fill = GridBagConstraints.VERTICAL;
		pnlIdCard.add(Box.createVerticalGlue(), idGbc);

		// ── EMAIL ───────────────────────────────────────────────
		txtEmail = createTextField();
		txtEmail.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String email = txtEmail.getText().trim();
					if (email.isEmpty()) {
						showErrMsg(txtEmail, "Vui lòng kiểm tra thông tin \"Email\"");
						return;
					}
					if (!email.matches(REGEX_EMAIL)) {
						showErrMsg(txtEmail, "Vui lòng kiểm tra thông tin \"Email\"");
						return;
					}
					txtNamSinh.requestFocus();
				}
			}
		});

		// ── LOẠI ĐỐI TƯỢNG ─────────────────────────────────────
		txtLoaiDoiTuong = new JTextField("");
		txtLoaiDoiTuong.setFont(FONT_14);
		txtLoaiDoiTuong.setEditable(false);
		txtLoaiDoiTuong.setPreferredSize(new Dimension(150, 32));
		txtLoaiDoiTuong.setBackground(new Color(245, 246, 248));
		txtLoaiDoiTuong.setForeground(new Color(80, 80, 80));
		txtLoaiDoiTuong
				.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_C), new EmptyBorder(2, 8, 2, 8)));

		cbLoaiDoiTuong = new JComboBox<>(
				new String[] { "Người lớn", "Trẻ em (<6 tuổi)", "Trẻ em (6-10 tuổi)", "Người cao tuổi" });
		cbLoaiDoiTuong.setVisible(false);
		cbLoaiDoiTuong.addActionListener(e -> {
			Object sel = cbLoaiDoiTuong.getSelectedItem();
			txtLoaiDoiTuong.setText(sel != null ? sel.toString() : "");
		});

		chkSinhVien = new JCheckBox("Sinh viên");
		chkSinhVien.setFont(FONT_14);
		chkSinhVien.setOpaque(false);
		chkSinhVien.addActionListener(e -> {
			if (chkSinhVien.isSelected())
				setLoaiDoiTuong("Sinh viên");
			else
				tinhLoaiDoiTuongTuNamSinh();
		});

		JPanel pnlLoaiDT = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		pnlLoaiDT.setOpaque(false);
		pnlLoaiDT.add(chkSinhVien);
		pnlLoaiDT.add(txtLoaiDoiTuong);
		pnlLoaiDT.add(cbLoaiDoiTuong);

		// ── NĂM SINH ────────────────────────────────────────────
		txtNamSinh = createTextField();
		txtNamSinh.setToolTipText("Nhập năm sinh (vd: 1995) hoặc ngày đầy đủ (vd: 15/03/1995)");
		txtNamSinh.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (c == KeyEvent.VK_BACK_SPACE)
					return;
				if (!Character.isDigit(c) && c != '/')
					e.consume();
				if (txtNamSinh.getText().length() >= 10)
					e.consume();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() != KeyEvent.VK_ENTER)
					return;
				String ns = txtNamSinh.getText().trim();
				// Validate năm sinh
				if (ns.isEmpty() || (!ns.matches(REGEX_NAM) && !ns.matches(REGEX_NGAY))) {
					showErrMsg(txtNamSinh, "Vui lòng kiểm tra thông tin \"Năm sinh\"");
					return;
				}
				if (chkSinhVien.isSelected())
					setLoaiDoiTuong("Sinh viên");
				else
					tinhLoaiDoiTuongTuNamSinh();

				xacNhanVaDuaXuongBang();
			}
		});
		txtNamSinh.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!chkSinhVien.isSelected())
					tinhLoaiDoiTuongTuNamSinh();
			}
		});

		// Nút Làm mới (Dùng chung style makeNavyBtn giống nút Tiếp tục)
        JButton btnLamMoi = makeNavyBtn("Làm mới", loadIcon("/Images/logoLammoi.png", 14, 14));
        // Lệnh này ép chữ sang phải -> Icon tự động bị đẩy sang CẠNH TRÁI
        btnLamMoi.setHorizontalTextPosition(SwingConstants.RIGHT); 
        btnLamMoi.addActionListener(e -> {
            txtSdt.setText(""); txtHoTen.setText(""); txtIdCard.setText("");
            txtEmail.setText(""); txtNamSinh.setText(""); txtLoaiDoiTuong.setText("");
            chkSinhVien.setSelected(false); rdoCccd.setSelected(true);
            setFormEditable(true); khachTonTai = false; txtSdt.requestFocus();
        });

        // Nút Xác nhận (Dùng chung style makeNavyBtn giống nút Tiếp tục)
        btnXacNhan = makeNavyBtn("Xác nhận", loadIcon("/Images/logoXacNhan.png", 14, 14));
        // Lệnh này ép chữ sang phải -> Icon tự động bị đẩy sang CẠNH TRÁI
        btnXacNhan.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnXacNhan.addActionListener(e -> xacNhanVaDuaXuongBang());

        JPanel pnlActionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActionBtns.setOpaque(false);
        pnlActionBtns.add(btnLamMoi);
        pnlActionBtns.add(btnXacNhan);

		gbc.weightx = 0.15;
		addFormItem(pnlForm, gbc, 0, 0, "Số điện thoại", txtSdt);
		gbc.weightx = 0.25;
		addFormItem(pnlForm, gbc, 1, 0, "Họ và tên", txtHoTen);
		gbc.weightx = 0.28;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 6, 10);
		pnlForm.add(pnlIdCard, gbc);
		gbc.weightx = 0.22;
		addFormItem(pnlForm, gbc, 3, 0, "Email", txtEmail);

		gbc.weightx = 0.15;
		addFormItem(pnlForm, gbc, 0, 1, "Năm sinh", txtNamSinh);
		gbc.weightx = 0.45;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JPanel wrapLoai = new JPanel(new BorderLayout(0, 4));
		wrapLoai.setOpaque(false);
		JLabel lblLoai = new JLabel("Loại đối tượng");
		lblLoai.setFont(FONT_14);
		lblLoai.setForeground(new Color(40, 40, 40));
		wrapLoai.add(lblLoai, BorderLayout.NORTH);
		wrapLoai.add(pnlLoaiDT, BorderLayout.CENTER);
		pnlForm.add(wrapLoai, gbc);
		gbc.gridwidth = 1;

		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.weightx = 0.22;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(10, 10, 10, 10);
		pnlForm.add(pnlActionBtns, gbc);

		wrapper.add(pnlForm, BorderLayout.CENTER);
		return wrapper;
	}

	// =========================================================
	// VALIDATE – bắt buộc đủ thông tin, thông báo chuẩn
	// =========================================================
	/**
	 * Trả về true nếu hợp lệ, false nếu có lỗi (đã hiển thị thông báo).
	 */
	private boolean validateForm() {
		String sdt = txtSdt.getText().trim();
		String hoten = txtHoTen.getText().trim();
		String id = txtIdCard.getText().trim();
		String email = txtEmail.getText().trim();
		String ns = txtNamSinh.getText().trim();
		String loaiDT = txtLoaiDoiTuong.getText().trim();

		// 1. Số điện thoại – bắt buộc
		if (sdt.isEmpty()) {
			showErr(txtSdt, "Số điện thoại");
			return false;
		}
		if (!sdt.matches(REGEX_SDT)) {
			showErrMsg(txtSdt, "Vui lòng kiểm tra thông tin \"Số điện thoại\"");
			return false;
		}

		// 2. Họ và tên – bắt buộc
		if (hoten.isEmpty()) {
			showErr(txtHoTen, "Họ và tên");
			return false;
		}
		if (!hoten.matches(REGEX_HOTEN)) {
			showErrMsg(txtHoTen, "Vui lòng kiểm tra thông tin \"Họ và tên\"");
			return false;
		}

		// 3. CCCD / Hộ chiếu – bắt buộc
		if (id.isEmpty()) {
			showErr(txtIdCard, rdoCccd.isSelected() ? "CCCD" : "Hộ chiếu");
			return false;
		}
		if (rdoCccd.isSelected()) {
			if (!id.matches(REGEX_CCCD)) {
				showErrMsg(txtIdCard, "Vui lòng kiểm tra thông tin \"CCCD\"");
				return false;
			}
		} else {
			if (!id.matches(REGEX_HC)) {
				showErrMsg(txtIdCard, "Vui lòng kiểm tra thông tin \"Hộ chiếu\"");
				return false;
			}
		}

		// 4. Email – bắt buộc
		if (email.isEmpty()) {
			showErr(txtEmail, "Email");
			return false;
		}
		if (!email.matches(REGEX_EMAIL)) {
			showErrMsg(txtEmail, "Vui lòng kiểm tra thông tin \"Email\"");
			return false;
		}

		// 5. Năm sinh – bắt buộc
		if (ns.isEmpty()) {
			showErr(txtNamSinh, "Năm sinh");
			return false;
		}
		if (!ns.matches(REGEX_NAM) && !ns.matches(REGEX_NGAY)) {
			showErrMsg(txtNamSinh, "Vui lòng kiểm tra thông tin \"Năm sinh\"");
			return false;
		}

		// 6. Loại đối tượng – bắt buộc
		if (loaiDT.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Vui lòng kiểm tra thông tin \"Loại đối tượng\"", "Thiếu thông tin",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	/** Hiện lỗi "thiếu thông tin" cho field trống */
	private void showErr(JTextField field, String fieldName) {
		showErrMsg(field, "Vui lòng kiểm tra thông tin \"" + fieldName + "\"");
	}

	/** Hiện lỗi tùy chỉnh và highlight border đỏ */
	private void showErrMsg(JTextField field, String msg) {
		JOptionPane.showMessageDialog(this, msg, "Thông tin không hợp lệ", JOptionPane.WARNING_MESSAGE);
		field.requestFocus();
		field.selectAll();
		field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ERR_C, 2), new EmptyBorder(2, 8, 2, 8)));
		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				field.setBorder(
						BorderFactory.createCompoundBorder(new LineBorder(BORDER_C), new EmptyBorder(2, 8, 2, 8)));
				field.removeFocusListener(this);
			}
		});
	}

	// =========================================================
	// LOGIC FORM – Tra cứu khách hàng theo SĐT
	// =========================================================
	private void checkKhachHang() {
		String phone = txtSdt.getText().trim();
		if (phone.isEmpty())
			return;

		String sql = "SELECT hoTenKH, cccd, email, namSinh, laSinhVien FROM KhachHang WHERE sdt = ?";
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, phone);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				// ── Khách tồn tại → đẩy thông tin lên form ────────
				khachTonTai = true;
				txtHoTen.setText(rs.getString("hoTenKH"));

				String cccd = rs.getString("cccd");
				if (cccd != null && !cccd.isEmpty()) {
					txtIdCard.setText(cccd);
					boolean isHc = cccd.matches("^[A-Z][0-9A-Z]{7}$");
					rdoHoChieu.setSelected(isHc);
					rdoCccd.setSelected(!isHc);
				} else {
					txtIdCard.setText("");
					rdoCccd.setSelected(true);
				}

				txtEmail.setText(rs.getString("email") != null ? rs.getString("email") : "");

				java.sql.Date dob = rs.getDate("namSinh");
				if (dob != null) {
					LocalDate ld = dob.toLocalDate();
					txtNamSinh.setText(
							String.format("%02d/%02d/%04d", ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear()));
					boolean laSV = rs.getBoolean("laSinhVien");
					chkSinhVien.setSelected(laSV);
					if (laSV) {
						setLoaiDoiTuong("Sinh viên");
					} else {
						int age = LocalDate.now().getYear() - ld.getYear();
						if (age < 6)
							setLoaiDoiTuong("Trẻ em (<6 tuổi)");
						else if (age <= 10)
							setLoaiDoiTuong("Trẻ em (6-10 tuổi)");
						else if (age >= 60)
							setLoaiDoiTuong("Người cao tuổi");
						else
							setLoaiDoiTuong("Người lớn");
					}
				} else {
					txtNamSinh.setText("");
				}
				setFormEditable(true);
			} else {
				// ── Khách chưa có → clear form, cho nhập mới ───────
				khachTonTai = false;
				txtHoTen.setText("");
				txtIdCard.setText("");
				txtEmail.setText("");
				txtNamSinh.setText("");
				txtLoaiDoiTuong.setText("");
				chkSinhVien.setSelected(false);
				rdoCccd.setSelected(true);
				setFormEditable(true);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void setFormEditable(boolean editable) {
		txtHoTen.setEditable(editable);
		txtIdCard.setEditable(editable);
		txtEmail.setEditable(editable);
		txtNamSinh.setEditable(editable);
		rdoCccd.setEnabled(editable);
		rdoHoChieu.setEnabled(editable);
		chkSinhVien.setEnabled(editable);
		Color bg = editable ? Color.WHITE : new Color(240, 240, 240);
		txtHoTen.setBackground(bg);
		txtIdCard.setBackground(bg);
		txtEmail.setBackground(bg);
		txtNamSinh.setBackground(bg);
	}

	// =========================================================
	// XÁC NHẬN – cập nhật bảng + DB
	// =========================================================
	/** Capitalize chữ cái đầu mỗi từ, các chữ còn lại thường */
	private String capitalizeWords(String s) {
		if (s == null || s.isEmpty())
			return s;
		String[] parts = s.trim().split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (String p : parts) {
			if (!p.isEmpty()) {
				sb.append(Character.toUpperCase(p.charAt(0)));
				if (p.length() > 1)
					sb.append(p.substring(1).toLowerCase());
			}
			sb.append(' ');
		}
		return sb.toString().trim();
	}

	// =========================================================
	// POPUP XÁC NHẬN THÔNG TIN → ĐƯA XUỐNG BẢNG
	// =========================================================
	private void xacNhanVaDuaXuongBang() {
		if (!validateForm())
			return;

		String hoten = capitalizeWords(txtHoTen.getText().trim());

		// --- GIAO DIỆN POPUP PHẲNG TỐI GIẢN & HIỆN ĐẠI ---
		JPanel pnlMsg = new JPanel();
		pnlMsg.setLayout(new BoxLayout(pnlMsg, BoxLayout.Y_AXIS));
		pnlMsg.setBackground(Color.WHITE);
		pnlMsg.setBorder(new EmptyBorder(15, 30, 15, 30));

		JLabel lblTitle = new JLabel("Xác nhận thông tin hành khách?");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
		lblTitle.setForeground(NAVY);
		lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel lblSub = new JLabel("Dữ liệu sẽ được cập nhật vào danh sách vé.");
		lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblSub.setForeground(new Color(100, 100, 100));
		lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

		pnlMsg.add(lblTitle);
		pnlMsg.add(Box.createVerticalStrut(10));
		pnlMsg.add(lblSub);

		JButton btnOk = new JButton("Xác nhận") {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? new Color(18, 42, 85)
						: getModel().isRollover() ? new Color(38, 68, 128) : NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		btnOk.setFont(FONT_B14);
		btnOk.setForeground(Color.WHITE);
		btnOk.setContentAreaFilled(false);
		btnOk.setBorderPainted(false);
		btnOk.setFocusPainted(false);
		btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnOk.setPreferredSize(new Dimension(100, 34));

		JButton btnCancel = new JButton("Hủy") {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? new Color(220, 220, 220)
						: getModel().isRollover() ? new Color(235, 235, 235) : Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
				g2.setColor(new Color(180, 180, 180));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		btnCancel.setFont(FONT_B14);
		btnCancel.setForeground(new Color(80, 80, 80));
		btnCancel.setContentAreaFilled(false);
		btnCancel.setBorderPainted(false);
		btnCancel.setFocusPainted(false);
		btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnCancel.setPreferredSize(new Dimension(80, 34));

		Object[] options = { btnOk, btnCancel };
		// Dùng PLAIN_MESSAGE để triệt tiêu hoàn toàn cái icon mặc định xấu xí
		JOptionPane optionPane = new JOptionPane(pnlMsg, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				options, options[0]);
		optionPane.setBackground(Color.WHITE);

		JDialog dialog = optionPane.createDialog(this, "Xác nhận");

		btnOk.addActionListener(e -> {
			dialog.dispose();
			txtHoTen.setText(hoten);
			capNhatVaoBang();
		});

		btnCancel.addActionListener(e -> dialog.dispose());

		dialog.setVisible(true);
	}

	private void capNhatVaoBang() {
		int row = tblVe.getSelectedRow();
		if (row == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 vé trong bảng!");
			return;
		}
		if (!validateForm())
			return;

		int modelRow = tblVe.getRowSorter() != null ? tblVe.convertRowIndexToModel(row) : row;

		// Capitalize họ tên trước khi lưu, đồng thời cập nhật luôn lên field
		String hotenRaw = txtHoTen.getText().trim();
		String hotenCap = capitalizeWords(hotenRaw);
		txtHoTen.setText(hotenCap); // hiển thị dạng hoa trên form

		String hoten = hotenCap;
		String cccd = txtIdCard.getText().trim();
		String sdt = txtSdt.getText().trim();
		String email = txtEmail.getText().trim();
		String ns = txtNamSinh.getText().trim();
		String loaiDT = cbLoaiDoiTuong.getSelectedItem() != null ? cbLoaiDoiTuong.getSelectedItem().toString() : "";
		boolean laSV = chkSinhVien.isSelected();

		// Cập nhật model bảng (các cột hiển thị)
		modelVe.setValueAt(hoten, modelRow, COL_HOTEN);
		modelVe.setValueAt(cccd, modelRow, COL_CCCD);
		modelVe.setValueAt(sdt, modelRow, COL_SDT);
		modelVe.setValueAt(loaiDT, modelRow, COL_LOAIDT);
		// Lưu thêm email / namSinh / laSV vào các cột ẩn để load lại form
		modelVe.setValueAt(email, modelRow, COL_EMAIL);
		modelVe.setValueAt(ns, modelRow, COL_NAMSINH);
		modelVe.setValueAt(laSV, modelRow, COL_LASISV);

		// Khứ hồi → điền cả cặp chiều về / chiều đi
		boolean khuHoi = loaiVe != null && (loaiVe.contains("hồi") || loaiVe.contains("Hồi"));
		if (khuHoi) {
			int half = modelVe.getRowCount() / 2;
			int pairRow = modelRow < half ? modelRow + half : modelRow - half;
			if (pairRow >= 0 && pairRow < modelVe.getRowCount()) {
				modelVe.setValueAt(hoten, pairRow, COL_HOTEN);
				modelVe.setValueAt(cccd, pairRow, COL_CCCD);
				modelVe.setValueAt(sdt, pairRow, COL_SDT);
				modelVe.setValueAt(loaiDT, pairRow, COL_LOAIDT);
				modelVe.setValueAt(email, pairRow, COL_EMAIL);
				modelVe.setValueAt(ns, pairRow, COL_NAMSINH);
				modelVe.setValueAt(laSV, pairRow, COL_LASISV);
			}
		}

		// Lưu / cập nhật DB
		luuHoacCapNhatKhachHang(sdt, hoten, cccd, email, ns, laSV);

		// Chuyển sang vé tiếp theo
		int nextView = row + 1;
		if (nextView < tblVe.getRowCount()) {
			tblVe.setRowSelectionInterval(nextView, nextView);
		} else {
			JOptionPane.showMessageDialog(this, "Đã điền đủ thông tin cho tất cả các vé!");
		}
	}

	// =========================================================
	// LƯU / CẬP NHẬT KHÁCH HÀNG – bao gồm tự tạo mã KHxxx
	// =========================================================
	private void luuHoacCapNhatKhachHang(String sdt, String hoTen, String cccd, String email, String ns, boolean laSV) {
		java.sql.Date ngaySinh = parseNgaySinh(ns);

		if (khachTonTai) {
			// UPDATE
			String sql = "UPDATE KhachHang SET hoTenKH=?, cccd=?, email=?, namSinh=?, laSinhVien=? WHERE sdt=?";
			try (Connection con = Connect_DB.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setNString(1, hoTen);
				ps.setString(2, cccd);
				ps.setString(3, email);
				ps.setDate(4, ngaySinh);
				ps.setBoolean(5, laSV);
				ps.setString(6, sdt);
				ps.executeUpdate();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			// INSERT – sinh mã KH không trùng
			String maKH = generateUniqueMaKH();
			String sql = "INSERT INTO KhachHang(maKH, sdt, hoTenKH, cccd, email, namSinh, laSinhVien) "
					+ "VALUES(?,?,?,?,?,?,?)";
			try (Connection con = Connect_DB.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, maKH);
				ps.setString(2, sdt);
				ps.setNString(3, hoTen);
				ps.setString(4, cccd);
				ps.setString(5, email);
				ps.setDate(6, ngaySinh);
				ps.setBoolean(7, laSV);
				ps.executeUpdate();
				khachTonTai = true;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/** Parse chuỗi ngày sinh DD/MM/YYYY hoặc YYYY → java.sql.Date */
	private java.sql.Date parseNgaySinh(String ns) {
		if (ns == null || ns.isEmpty())
			return null;
		try {
			if (ns.matches(REGEX_NGAY)) {
				String[] p = ns.split("/");
				return java.sql.Date
						.valueOf(LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0])));
			} else if (ns.matches(REGEX_NAM)) {
				return java.sql.Date.valueOf(LocalDate.of(Integer.parseInt(ns), 1, 1));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/** Sinh mã khách hàng KHxxx không trùng DB */
	private String generateUniqueMaKH() {
		Random rnd = new Random();
		while (true) {
			String maKH = String.format("KH%03d", rnd.nextInt(1000));
			if (!isMaKHExists(maKH))
				return maKH;
		}
	}

	private boolean isMaKHExists(String maKH) {
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT 1 FROM KhachHang WHERE maKH = ?")) {
			ps.setString(1, maKH);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	// =========================================================
	// LOAD HÀNG BẢNG → FORM (đầy đủ toàn bộ thông tin)
	// =========================================================
	private void loadRowToForm(int viewRow) {
		isLoadingRow = true;
		try {
			int row = tblVe.getRowSorter() != null ? tblVe.convertRowIndexToModel(viewRow) : viewRow;

			String hoten = nullToEmpty(modelVe.getValueAt(row, COL_HOTEN));
			String cccd = nullToEmpty(modelVe.getValueAt(row, COL_CCCD));
			String sdt = nullToEmpty(modelVe.getValueAt(row, COL_SDT));
			String loaiDT = nullToEmpty(modelVe.getValueAt(row, COL_LOAIDT));
			String email = nullToEmpty(modelVe.getValueAt(row, COL_EMAIL));
			String ns = nullToEmpty(modelVe.getValueAt(row, COL_NAMSINH));
			Object svObj = modelVe.getValueAt(row, COL_LASISV);
			boolean laSV = svObj instanceof Boolean && (Boolean) svObj;

			// Điền vào form
			txtSdt.setText(sdt);
			txtHoTen.setText(hoten);
			txtIdCard.setText(cccd);
			txtEmail.setText(email);
			txtNamSinh.setText(ns);
			chkSinhVien.setSelected(laSV);

			// Radio CCCD / HC
			if (!cccd.isEmpty()) {
				boolean isHc = cccd.matches(REGEX_HC);
				rdoHoChieu.setSelected(isHc);
				rdoCccd.setSelected(!isHc);
			} else {
				rdoCccd.setSelected(true);
			}

			// Loại đối tượng
			if (!loaiDT.isEmpty())
				setLoaiDoiTuong(loaiDT);
			else
				txtLoaiDoiTuong.setText("");

			setFormEditable(true);

			// Nếu vé này chưa có SĐT → không cần tra DB, chỉ để form trống
			if (!sdt.isEmpty()) {
				// Kiểm tra khách tồn tại (cập nhật flag khachTonTai)
				String sql = "SELECT 1 FROM KhachHang WHERE sdt = ?";
				try (Connection con = Connect_DB.getInstance().getConnection();
						PreparedStatement ps = con.prepareStatement(sql)) {
					ps.setString(1, sdt);
					ResultSet rs = ps.executeQuery();
					khachTonTai = rs.next();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				khachTonTai = false;
			}
		} finally {
			isLoadingRow = false;
		}
	}

	private String nullToEmpty(Object obj) {
		return obj == null ? "" : obj.toString();
	}

	// =========================================================
	// TÍNH LOẠI ĐỐI TƯỢNG TỪ NĂM SINH
	// =========================================================
	private void tinhLoaiDoiTuongTuNamSinh() {
		String ns = txtNamSinh.getText().trim();
		if (ns.isEmpty()) {
			txtLoaiDoiTuong.setText("");
			return;
		}
		try {
			int namSinh = ns.contains("/") ? Integer.parseInt(ns.split("/")[ns.split("/").length - 1])
					: Integer.parseInt(ns);
			int age = LocalDate.now().getYear() - namSinh;
			if (age < 6)
				setLoaiDoiTuong("Trẻ em (<6 tuổi)");
			else if (age <= 10)
				setLoaiDoiTuong("Trẻ em (6-10 tuổi)");
			else if (age >= 60)
				setLoaiDoiTuong("Người cao tuổi");
			else
				setLoaiDoiTuong("Người lớn");
		} catch (Exception ex) {
			setLoaiDoiTuong("Người lớn");
		}
	}

	private void setLoaiDoiTuong(String loai) {
		boolean found = false;
		for (int i = 0; i < cbLoaiDoiTuong.getItemCount(); i++)
			if (cbLoaiDoiTuong.getItemAt(i).equals(loai)) {
				found = true;
				break;
			}
		if (!found)
			cbLoaiDoiTuong.addItem(loai);
		cbLoaiDoiTuong.setSelectedItem(loai);
		txtLoaiDoiTuong.setText(loai);
	}

	// =========================================================
	// BẢNG VÉ
	// =========================================================
	private JPanel buildCenterTable() {
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		wrapper.setBorder(new EmptyBorder(5, 10, 5, 10));

		// Header row
		JPanel headerRow = new JPanel(new BorderLayout());
		headerRow.setOpaque(false);
		JPanel leftH = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		leftH.setOpaque(false);
		JLabel lblTitle = new JLabel("Danh sách vé");
		lblTitle.setFont(FONT_B14);
		lblTitle.setForeground(Color.WHITE);
		lblTitle.setOpaque(true);
		lblTitle.setBackground(NAVY);
		lblTitle.setBorder(new EmptyBorder(6, 15, 6, 15));
		leftH.add(lblTitle);

		JPanel rightH = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightH.setOpaque(false);
		JLabel lblFilter = new JLabel("Lọc chiều:");
		lblFilter.setFont(FONT_B14);
		cbFilterChieu = new JComboBox<>(new String[] { "Tất cả", "Chiều đi", "Chiều về" });
		cbFilterChieu.setFont(FONT_14);
		cbFilterChieu.setBackground(Color.WHITE);
		cbFilterChieu.setPreferredSize(new Dimension(110, 28));
		cbFilterChieu.addActionListener(e -> applyFilter());
		rightH.add(lblFilter);
		rightH.add(cbFilterChieu);

		headerRow.add(leftH, BorderLayout.WEST);
		headerRow.add(rightH, BorderLayout.EAST);
		wrapper.add(headerRow, BorderLayout.NORTH);

		// Cột hiển thị + các cột ẩn (email, namSinh, laSV)
		String[] cols = { "STT", "Mã vé", "Loại vé", "Chiều vé", "Mã ghế", "Họ tên", "CCCD/Hộ chiếu", "SĐT",
				"Loại đối tượng", "email_hidden", "namsinh_hidden", "lasisv_hidden" };
		modelVe = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		tblVe = new JTable(modelVe) {
			@Override
			public Component prepareRenderer(TableCellRenderer r, int row, int col) {
				Component c = super.prepareRenderer(r, row, col);
				if (isRowSelected(row)) {
					c.setBackground(new Color(210, 228, 245));
					c.setForeground(Color.BLACK);
				} else {
					c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
					c.setForeground(Color.BLACK);
				}
				return c;
			}
		};
		tblVe.setRowHeight(32);
		tblVe.setFont(FONT_14);
		tblVe.setShowGrid(false);
		tblVe.setIntercellSpacing(new Dimension(0, 0));

		JTableHeader header = tblVe.getTableHeader();
		header.setFont(FONT_B14);
		header.setBackground(new Color(235, 241, 252));
		header.setForeground(NAVY);
		header.setPreferredSize(new Dimension(0, 36));
		header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));

		// Độ rộng cột hiển thị
		int[] widths = { 35, 90, 80, 80, 110, 140, 120, 100, 120, 0, 0, 0 };
		for (int i = 0; i < widths.length; i++) {
			tblVe.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
			if (i >= 9) { // Ẩn 3 cột cuối
				tblVe.getColumnModel().getColumn(i).setMinWidth(0);
				tblVe.getColumnModel().getColumn(i).setMaxWidth(0);
				tblVe.getColumnModel().getColumn(i).setWidth(0);
			}
		}

		// Click bảng → load lên form
		tblVe.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && tblVe.getSelectedRow() != -1)
				loadRowToForm(tblVe.getSelectedRow());
		});

		JScrollPane scroll = new JScrollPane(tblVe);
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setBorder(new LineBorder(BORDER_C, 1));
		wrapper.add(scroll, BorderLayout.CENTER);
		return wrapper;
	}

	private void applyFilter() {
		String filter = (String) cbFilterChieu.getSelectedItem();
		if (tblVe.getRowSorter() == null)
			tblVe.setRowSorter(new TableRowSorter<>(modelVe));
		TableRowSorter<?> sorter = (TableRowSorter<?>) tblVe.getRowSorter();
		sorter.setRowFilter("Tất cả".equals(filter) ? null : RowFilter.regexFilter(filter, COL_CHIEU));
	}

	// =========================================================
	// SINH MÃ VÉ & KHỞI TẠO BẢNG
	// =========================================================
	private String generateUniqueMaVe() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random rnd = new Random();
		while (true) {
			StringBuilder sb = new StringBuilder(9);
			for (int i = 0; i < 9; i++)
				sb.append(chars.charAt(rnd.nextInt(chars.length())));
			String code = sb.toString();
			if (!isMaVeExists(code))
				return code;
		}
	}

	private boolean isMaVeExists(String maVe) {
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT 1 FROM Ve WHERE maVe = ?")) {
			ps.setString(1, maVe);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private void initTableData() {
		boolean khuHoi = loaiVe != null && (loaiVe.contains("hồi") || loaiVe.contains("Hồi"));
		int half = khuHoi ? danhSachGhe.size() / 2 : danhSachGhe.size();
		for (int i = 0; i < danhSachGhe.size(); i++) {
			String chieu = !khuHoi ? "Chiều đi" : (i < half ? "Chiều đi" : "Chiều về");
			// 12 cột: 9 hiển thị + 3 ẩn (email, namSinh, laSV)
			modelVe.addRow(new Object[] { i + 1, generateUniqueMaVe(), loaiVe, chieu, danhSachGhe.get(i), "", "", "",
					"", "", "", false });
		}
		if (tblVe.getRowCount() > 0)
			tblVe.setRowSelectionInterval(0, 0);
	}

	// =========================================================
	// ĐỒNG HỒ ĐẾM NGƯỢC
	// =========================================================
	private void startCountdown() {
		countdownTimer = new javax.swing.Timer(1000, e -> {
			secondsLeft--;
			if (secondsLeft <= 0) {
				countdownTimer.stop();
				lblCountdown.setText("Hết thời gian!");
				JOptionPane.showMessageDialog(this, "Thời gian giữ vé đã hết!\nVui lòng thực hiện lại.",
						"Hết thời gian", JOptionPane.WARNING_MESSAGE);
				if (onQuayLai != null)
					onQuayLai.run();
			} else
				updateCountdownLabel();
		});
		countdownTimer.start();
		updateCountdownLabel();
	}

	private void updateCountdownLabel() {
		int m = secondsLeft / 60, s = secondsLeft % 60;
		lblCountdown.setForeground(secondsLeft <= 300 ? new Color(160, 0, 0) : new Color(190, 30, 30));
		lblCountdown.setBackground(secondsLeft <= 300 ? new Color(160, 0, 0) : new Color(200, 60, 60));
		lblCountdown.setText(String.format("Thời hạn giữ vé: %02d:%02d", m, s));
	}

	// =========================================================
	// BOTTOM BAR
	// =========================================================
	private JPanel buildBotBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

		btnQuayLai = makeOutlineBtn("Quay lại", loadIcon("/Images/logoBack.png", 14, 14));
		btnQuayLai.addActionListener(e -> {
			if (countdownTimer != null)
				countdownTimer.stop();
			if (onQuayLai != null)
				onQuayLai.run();
		});

		lblCountdown = new JLabel("Thời hạn giữ vé: 30:00") {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(255, 235, 235));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(getBackground());
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		lblCountdown.setFont(FONT_B14);
		lblCountdown.setForeground(new Color(190, 30, 30));
		lblCountdown.setOpaque(false);
		lblCountdown.setBackground(new Color(200, 60, 60));
		lblCountdown.setBorder(new EmptyBorder(6, 14, 6, 14));

		btnTiepTuc = makeNavyBtn("Tiếp tục", loadIcon("/Images/logoGoOn.png", 14, 14));
		btnTiepTuc.addActionListener(e -> {
			for (int i = 0; i < modelVe.getRowCount(); i++) {
				String ten = (String) modelVe.getValueAt(i, COL_HOTEN);
				if (ten == null || ten.isEmpty()) {
					JOptionPane.showMessageDialog(this, "Chưa điền đủ thông tin cho vé số " + (i + 1));
					for (int v = 0; v < tblVe.getRowCount(); v++) {
						int mr = tblVe.getRowSorter() != null ? tblVe.convertRowIndexToModel(v) : v;
						if (mr == i) {
							tblVe.setRowSelectionInterval(v, v);
							break;
						}
					}
					return;
				}
			}
			if (countdownTimer != null)
				countdownTimer.stop();
			JOptionPane.showMessageDialog(this, "Thông tin hợp lệ!\nChuyển sang màn hình Thanh Toán...");
			// TODO: sang GUI3
		});

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		left.setBackground(Color.WHITE);
		left.add(btnQuayLai);
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
		right.setBackground(Color.WHITE);
		right.add(lblCountdown);
		right.add(Box.createHorizontalStrut(16));
		right.add(btnTiepTuc);

		bar.add(left, BorderLayout.WEST);
		bar.add(right, BorderLayout.EAST);
		return bar;
	}

	// =========================================================
	// HELPERS
	// =========================================================
	private JTextField createTextField() {
		JTextField txt = new JTextField();
		txt.setPreferredSize(new Dimension(150, 32));
		txt.setFont(FONT_14);
		txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_C), new EmptyBorder(2, 8, 2, 8)));
		return txt;
	}

	private void addFormItem(JPanel pnl, GridBagConstraints gbc, int x, int y, String label, JComponent comp) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		JPanel wrap = new JPanel(new BorderLayout(0, 4));
		wrap.setOpaque(false);
		JLabel lbl = new JLabel(label);
		lbl.setFont(FONT_14);
		lbl.setForeground(new Color(40, 40, 40));
		wrap.add(lbl, BorderLayout.NORTH);
		wrap.add(comp, BorderLayout.CENTER);
		pnl.add(wrap, gbc);
	}

	private Icon loadIcon(String path, int w, int h) {
		try {
			java.net.URL url = getClass().getResource(path);
			if (url != null)
				return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
		} catch (Exception ignored) {
		}
		return null;
	}

	private JButton makeNavyBtn(String text, Icon icon) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? new Color(18, 42, 85)
						: getModel().isRollover() ? new Color(38, 68, 128) : NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		if (icon != null) {
			b.setIcon(icon);
			b.setHorizontalTextPosition(SwingConstants.LEFT);
		}
		b.setFont(FONT_B14);
		b.setForeground(Color.WHITE);
		b.setIconTextGap(8);
		b.setBorder(new EmptyBorder(6, 18, 6, 18));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JButton makeOutlineBtn(String text, Icon icon) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? new Color(198, 215, 242)
						: getModel().isRollover() ? new Color(212, 228, 250) : new Color(226, 236, 252));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(NAVY);
				g2.setStroke(new BasicStroke(1.2f));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		if (icon != null)
			b.setIcon(icon);
		b.setFont(FONT_14);
		b.setForeground(NAVY);
		b.setIconTextGap(8);
		b.setBorder(new EmptyBorder(6, 16, 6, 16));
		b.setContentAreaFilled(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}
}