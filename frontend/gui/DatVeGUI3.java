package gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import util.MaTuDong;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Currency;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class DatVeGUI3 extends JPanel {

	private static final Color NAVY = new Color(28, 57, 110);
	private static final Color BG = new Color(242, 247, 252);
	private static final Color BORDER_C = new Color(180, 205, 230);
	private static final java.awt.Font FONT_14 = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
	private static final java.awt.Font FONT_B14 = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
	private static final DecimalFormat DF = new DecimalFormat("#,###");

	private static final String BANK_ID = "MB";
	private static final String ACCOUNT_NO = "0382588430";
	private static final String ACCOUNT_NAME = "MB Bank";
	private static final String CASSO_API_KEY = "AK_CS.69d49310536411f1ad2d7bbf51f870c4.1OR4aZOPpK4BslQXgsgNQGlFiMwe8EDKc6Tuva6vzVcTf7ssLfssoXfn5vVKU27z4bemHq6E";

	private JTable tblChiTiet;
	private DefaultTableModel modelChiTiet;
	private JLabel lblTongTien, lblTongKhuyenMai, lblThanhToanConLai, lblQR, lblCountdown;
	private JToggleButton btnTienMat, btnChuyenKhoan;
	private JPanel pnlSwitch, pnlTienMat, pnlQR;
	private CardLayout cardSwitch;
	private JTextField txtTienKhachDua;
	private JLabel lblTienThua;
	private double tongThanhToan = 0, tongGiaGoc = 0, tongGiamDoiTuong = 0, giamVoucher = 0;
	private String maHD = taoMaHoaDon();
	private java.util.function.Consumer<Integer> onQuayLai;
	private Runnable onHuyVe;
	private DefaultTableModel modelFromGUI2;
	private javax.swing.Timer countdownTimer, bankCheckTimer;
	private int secondsLeft;
	private ImageIcon originalQRImageIcon = null;

	public DatVeGUI3(DefaultTableModel modelFromGUI2, int secondsLeft, java.util.function.Consumer<Integer> onQuayLai,
			Runnable onHuyVe) {
		this.modelFromGUI2 = modelFromGUI2;
		this.secondsLeft = secondsLeft;
		this.onQuayLai = onQuayLai;
		this.onHuyVe = onHuyVe;
		setLayout(new BorderLayout(0, 0));
		setBackground(BG);
		JPanel pnlCenter = new JPanel(new GridBagLayout());
		pnlCenter.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.78;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 8);
		pnlCenter.add(buildLeftPanel(), gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.22;
		gbc.insets = new Insets(0, 0, 0, 0);
		pnlCenter.add(buildRightPanel(), gbc);
		add(pnlCenter, BorderLayout.CENTER);
		add(buildBottomBar(), BorderLayout.SOUTH);
		tinhToanTaiChinh();
		startCountdown();
	}

	public DatVeGUI3(DefaultTableModel modelFromGUI2, int secondsLeft, java.util.function.Consumer<Integer> onQuayLai) {
		this(modelFromGUI2, secondsLeft, onQuayLai, null);
	}

	private Object[] tinhGiaVeChoVe(String maGhe, String maGaDen, String loaiDoiTuong) {
		// --- 1. Lấy heSoLoaiToa + loaiGhe từ DB ---
		double heSoLoaiToa = 1.0;
		String loaiGheHienThi = "Ghế cứng";
		String sqlGhe = "SELECT g.loaiGhe, t.heSoLoaiToa " + "FROM Ghe g JOIN ToaTau t ON g.maToaTau = t.maToaTau "
				+ "WHERE g.maGhe = ?";
		try (java.sql.Connection con = connect_DB.Connect_DB.getInstance().getConnection();
				java.sql.PreparedStatement ps = con.prepareStatement(sqlGhe)) {
			ps.setString(1, maGhe);
			try (java.sql.ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					heSoLoaiToa = rs.getDouble("heSoLoaiToa");
					String raw = rs.getString("loaiGhe");
					if (raw != null) {
						loaiGheHienThi = switch (raw.trim()) {
						case "GHE_CUNG" -> "Ghế cứng";
						case "GHE_MEM" -> "Ghế mềm";
						case "GIUONG_NAM" -> "Giường nằm";
						default -> raw;
						};
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// --- 2. Lấy heSoCuLy: dùng tên ga đến để tra từ bảng ChiTietChuyenTau hoặc Ga
		// ---
		// Thử lấy từ cột heSoKhoangCach trong Ga trước, fallback sang giá trị mặc định
		// theo loại toa
		double heSoCuLy = tinhHeSoCuLyMacDinh(heSoLoaiToa);
		if (maGaDen != null && !maGaDen.isEmpty()) {
			// Thử các tên cột có thể có trong bảng Ga
			for (String col : new String[] { "heSoCuLy", "heSoKhoangCach", "heSo", "khoangCach" }) {
				try (java.sql.Connection con = connect_DB.Connect_DB.getInstance().getConnection();
						java.sql.PreparedStatement ps = con
								.prepareStatement("SELECT " + col + " FROM Ga WHERE maGa = ?")) {
					ps.setString(1, maGaDen);
					try (java.sql.ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							heSoCuLy = rs.getDouble(1);
							break;
						}
					}
				} catch (Exception ignored) {
				}
			}
		}

		// --- 3. Tạo Ve entity và dùng tinhGiaVe() ---
		entity.Ve ve = new entity.Ve();
		ve.setHeSoCuLy(heSoCuLy);
		ve.setHeSoLoaiToa(heSoLoaiToa);
		ve.setLoaiDoiTuong(loaiDoiTuong);

		double giaGoc = ve.tinhGiaGoc();
		double tyLeGiam = ve.layTyLeGiamDoiTuong();
		double giamGia = giaGoc * tyLeGiam;
		double thanhTien = Math.max(0, giaGoc - giamGia);

		return new Object[] { loaiGheHienThi, giaGoc, giamGia, thanhTien };
	}

	/** Trả về heSoCuLy mặc định dựa vào heSoLoaiToa (khi không tìm được từ DB) */
	private double tinhHeSoCuLyMacDinh(double heSoLoaiToa) {
		// Fallback: giữ nguyên hệ số cũ = 1.2
		return 1.2;
	}

	private JPanel buildLeftPanel() {
		JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
		pnlLeft.setOpaque(false);
		JPanel pnlTableWrapper = new JPanel(new BorderLayout());
		pnlTableWrapper.setBackground(Color.WHITE);
		pnlTableWrapper.setBorder(new LineBorder(BORDER_C, 1, true));
		JLabel lblChiTiet = new JLabel("Chi tiết");
		lblChiTiet.setFont(FONT_B14);
		lblChiTiet.setForeground(Color.WHITE);
		lblChiTiet.setOpaque(true);
		lblChiTiet.setBackground(NAVY);
		lblChiTiet.setBorder(new EmptyBorder(6, 12, 6, 12));
		JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titleWrap.setOpaque(false);
		titleWrap.add(lblChiTiet);
		pnlTableWrapper.add(titleWrap, BorderLayout.NORTH);

		String[] cols = { "STT", "Mã vé", "Chiều vé", "Loại chỗ", "Đơn giá", "Loại đối tượng", "Giảm giá",
				"Thành tiền" };
		modelChiTiet = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		for (int i = 0; i < modelFromGUI2.getRowCount(); i++) {
			String maVe = modelFromGUI2.getValueAt(i, 1).toString();
			String chieuVe = modelFromGUI2.getValueAt(i, 3).toString();
			String maGhe = modelFromGUI2.getValueAt(i, 4).toString();
			String loaiDoiTuong = modelFromGUI2.getValueAt(i, 8).toString();

			String maGaDen = "";
			try {
				if (modelFromGUI2.getColumnCount() > 13)
					maGaDen = modelFromGUI2.getValueAt(i, 13).toString();
			} catch (Exception ex) {
			}

			Object[] info = tinhGiaVeChoVe(maGhe, maGaDen, loaiDoiTuong);
			String loaiCho = (String) info[0];
			double donGia = (Double) info[1];
			double giamGia = (Double) info[2];
			double thanhTien = (Double) info[3];

			modelChiTiet.addRow(new Object[] { i + 1, maVe, chieuVe, loaiCho, DF.format(donGia) + " VNĐ", loaiDoiTuong,
					DF.format(giamGia) + " VNĐ", DF.format(thanhTien) + " VNĐ" });
		}

		tblChiTiet = new JTable(modelChiTiet) {
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
		tblChiTiet.setRowHeight(35);
		tblChiTiet.setFont(FONT_14);
		tblChiTiet.getTableHeader().setFont(FONT_B14);
		tblChiTiet.getTableHeader().setBackground(new Color(245, 248, 252));
		tblChiTiet.setShowGrid(false);
		tblChiTiet.setIntercellSpacing(new Dimension(0, 0));
		TableColumnModel tcm = tblChiTiet.getColumnModel();
		tcm.getColumn(0).setMaxWidth(40);
		tcm.getColumn(1).setMinWidth(85);
		tcm.getColumn(1).setMaxWidth(85);
		tcm.getColumn(2).setMinWidth(75);
		tcm.getColumn(2).setMaxWidth(75);
		tcm.getColumn(3).setMinWidth(90);
		tcm.getColumn(3).setMaxWidth(90);
		tcm.getColumn(5).setPreferredWidth(160);

		JScrollPane scroll = new JScrollPane(tblChiTiet);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(Color.WHITE);
		pnlTableWrapper.add(scroll, BorderLayout.CENTER);

		JPanel pnlBottomLeft = new JPanel(new BorderLayout(0, 10));
		pnlBottomLeft.setOpaque(false);
		JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlBtns.setOpaque(false);
		JButton btnKhuyenMai = makeNavyBtn("Thêm khuyến mãi", null);
		btnKhuyenMai.addActionListener(e -> nhapKhuyenMai());
		JButton btnGhiChu = makeNavyBtn("Ghi chú", null);
		btnGhiChu.addActionListener(e -> JOptionPane.showInputDialog(this, "Nhập ghi chú cho hóa đơn:"));
		pnlBtns.add(btnKhuyenMai);
		pnlBtns.add(btnGhiChu);
		pnlBottomLeft.add(pnlBtns, BorderLayout.NORTH);

		JPanel pnlTotals = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
		pnlTotals.setOpaque(false);
		lblTongTien = new JLabel();
		lblTongTien.setFont(FONT_14);
		lblTongKhuyenMai = new JLabel();
		lblTongKhuyenMai.setFont(FONT_14);
		lblThanhToanConLai = new JLabel();
		lblThanhToanConLai.setFont(FONT_14);
		pnlTotals.add(lblTongTien);
		pnlTotals.add(lblTongKhuyenMai);
		pnlTotals.add(lblThanhToanConLai);
		pnlBottomLeft.add(pnlTotals, BorderLayout.CENTER);
		pnlLeft.add(pnlTableWrapper, BorderLayout.CENTER);
		pnlLeft.add(pnlBottomLeft, BorderLayout.SOUTH);
		return pnlLeft;
	}

	private JPanel buildRightPanel() {
		JPanel pnlRight = new JPanel(new BorderLayout());
		pnlRight.setBackground(new Color(245, 248, 252));
		pnlRight.setBorder(new LineBorder(BORDER_C, 1));
		JLabel lblInfo = new JLabel("Thông tin hóa đơn");
		lblInfo.setFont(FONT_B14);
		lblInfo.setForeground(Color.WHITE);
		lblInfo.setOpaque(true);
		lblInfo.setBackground(NAVY);
		lblInfo.setBorder(new EmptyBorder(6, 12, 6, 12));
		JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titleWrap.setOpaque(false);
		titleWrap.add(lblInfo);
		pnlRight.add(titleWrap, BorderLayout.NORTH);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);
		content.setBorder(new EmptyBorder(10, 10, 10, 10));

		String tenKH = modelFromGUI2.getRowCount() > 0 ? modelFromGUI2.getValueAt(0, 5).toString() : "N/A";
		String sdtKH = modelFromGUI2.getRowCount() > 0 ? modelFromGUI2.getValueAt(0, 7).toString() : "N/A";
		String maKH = "N/A";
		try {
			if (!sdtKH.equals("N/A") && !sdtKH.isEmpty()) {
				entity.KhachHang kh = new dao.KhachHangDAO().timTheoSDT(sdtKH);
				if (kh != null)
					maKH = kh.getMaKH();
			}
		} catch (Exception ignored) {
		}

		content.add(createDetailLabel("Mã nhân viên:", "NV001"));
		content.add(Box.createVerticalStrut(4));
		content.add(createDetailLabel("Tên nhân viên:", "Nhân viên Bán Vé"));
		content.add(Box.createVerticalStrut(4));
		content.add(createDetailLabel("Mã khách hàng:", maKH));
		content.add(Box.createVerticalStrut(4));
		content.add(createDetailLabel("Tên khách hàng:", tenKH));
		content.add(Box.createVerticalStrut(4));
		content.add(createDetailLabel("Số điện thoại:", sdtKH));
		content.add(Box.createVerticalStrut(10));
		content.add(new JSeparator());
		content.add(Box.createVerticalStrut(10));

		JPanel pnlPTTTTitle = new JPanel(new BorderLayout()) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
			}
		};
		pnlPTTTTitle.setOpaque(false);
		JLabel lblPTTT = new JLabel("Phương thức thanh toán:");
		lblPTTT.setFont(FONT_14);
		pnlPTTTTitle.add(lblPTTT, BorderLayout.WEST);
		content.add(pnlPTTTTitle);
		content.add(Box.createVerticalStrut(5));

		btnTienMat = createToggleBtn("Tiền mặt");
		btnChuyenKhoan = createToggleBtn("Chuyển khoản");
		new ButtonGroup() {
			{
				add(btnTienMat);
				add(btnChuyenKhoan);
			}
		};
		btnTienMat.setSelected(true);

		JPanel pnlToggle = new JPanel(new GridLayout(1, 2, 6, 0)) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, 35);
			}
		};
		pnlToggle.setOpaque(false);
		pnlToggle.add(btnTienMat);
		pnlToggle.add(btnChuyenKhoan);
		content.add(pnlToggle);
		content.add(Box.createVerticalStrut(15));

		cardSwitch = new CardLayout();
		pnlSwitch = new JPanel(cardSwitch) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
			}
		};
		pnlSwitch.setOpaque(false);

		pnlTienMat = new JPanel();
		pnlTienMat.setLayout(new BoxLayout(pnlTienMat, BoxLayout.Y_AXIS));
		pnlTienMat.setOpaque(false);

		JPanel pnlNhapTien = new JPanel(new BorderLayout()) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, 26);
			}
		};
		pnlNhapTien.setOpaque(false);
		pnlNhapTien.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		JLabel lblNhapTien = new JLabel("Nhập số tiền: ");
		lblNhapTien.setFont(FONT_14);
		txtTienKhachDua = new JTextField();
		txtTienKhachDua.setFont(FONT_14);
		txtTienKhachDua.setHorizontalAlignment(JTextField.RIGHT);
		txtTienKhachDua.setBorder(null);
		txtTienKhachDua.setOpaque(false);
		txtTienKhachDua.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				try {
					String raw = txtTienKhachDua.getText().replaceAll("[^0-9]", "");
					if (!raw.isEmpty())
						txtTienKhachDua.setText(DF.format(Double.parseDouble(raw)));
					else
						txtTienKhachDua.setText("");
				} catch (Exception ex) {
				}
				tinhTienThua();
			}
		});
		pnlNhapTien.add(lblNhapTien, BorderLayout.WEST);
		pnlNhapTien.add(txtTienKhachDua, BorderLayout.CENTER);

		JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 5, 5)) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, 90);
			}
		};
		pnlGrid.setOpaque(false);
		String[] quickCash = { "500,000", "700,000", "900,000", "1,000,000", "1,200,000", "1,500,000", "1,700,000",
				"2,000,000" };
		for (String qc : quickCash) {
			JButton b = makeOutlineBtn(qc, null);
			b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
			b.setBorder(new EmptyBorder(2, 0, 2, 0));
			b.addActionListener(e -> {
				txtTienKhachDua.setText(qc);
				tinhTienThua();
			});
			pnlGrid.add(b);
		}
		pnlGrid.add(new JLabel());

		JPanel pnlThua = new JPanel(new BorderLayout()) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, 26);
			}
		};
		pnlThua.setOpaque(false);
		pnlThua.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		JLabel lblThuaTitle = new JLabel("Tiền thừa trả khách:");
		lblThuaTitle.setFont(FONT_14);
		lblTienThua = new JLabel("0 VNĐ");
		lblTienThua.setFont(FONT_B14);
		pnlThua.add(lblThuaTitle, BorderLayout.WEST);
		pnlThua.add(lblTienThua, BorderLayout.EAST);

		pnlTienMat.add(pnlNhapTien);
		pnlTienMat.add(Box.createVerticalStrut(6));
		pnlTienMat.add(pnlGrid);
		pnlTienMat.add(Box.createVerticalStrut(10));
		pnlTienMat.add(pnlThua);

		pnlQR = new JPanel(new BorderLayout());
		pnlQR.setOpaque(false);
		lblQR = new JLabel("", SwingConstants.CENTER);
		lblQR.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblQR.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (originalQRImageIcon != null)
					showZoomedQRDialog();
			}
		});
		pnlQR.add(lblQR, BorderLayout.CENTER);
		pnlQR.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (originalQRImageIcon != null)
					scaleAndSetQR();
			}
		});

		pnlSwitch.add(pnlTienMat, "TIEN_MAT");
		pnlSwitch.add(pnlQR, "QR");
		content.add(pnlSwitch);

		ActionListener ptttListener = ev -> {
			boolean isBank = btnChuyenKhoan.isSelected();
			cardSwitch.show(pnlSwitch, isBank ? "QR" : "TIEN_MAT");
			if (isBank) {
				toggleQRCode();
				startBankChecking();
			} else {
				stopBankChecking();
				tinhTienThua();
			}
		};
		btnTienMat.addActionListener(ptttListener);
		btnChuyenKhoan.addActionListener(ptttListener);

		content.add(Box.createVerticalGlue());
		pnlRight.add(content, BorderLayout.CENTER);
		return pnlRight;
	}

	private JPanel buildBottomBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
		JButton btnQuayLai = makeOutlineBtn("Quay lại", loadIcon("/Images/logoBack.png", 14, 14));
		btnQuayLai.setBorder(new EmptyBorder(6, 16, 6, 16));
		btnQuayLai.addActionListener(e -> {
			stopAllTimers();
			if (onQuayLai != null)
				onQuayLai.accept(secondsLeft);
		});

		JButton btnHuyVe = makeRedBtn("Hủy vé", loadIcon("/Images/logoThungRac.png", 14, 14));
		btnHuyVe.addActionListener(e -> showHuyVePopup());

		lblCountdown = new JLabel("Thời hạn giữ vé: --:--") {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(255, 235, 235));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
				g2.setColor(getBackground());
				g2.setStroke(new BasicStroke(1.2f));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		lblCountdown.setFont(FONT_B14);
		lblCountdown.setForeground(new Color(190, 30, 30));
		lblCountdown.setOpaque(false);
		lblCountdown.setBackground(new Color(200, 60, 60));
		lblCountdown.setBorder(new EmptyBorder(6, 12, 6, 12));

		JButton btnLuuTam = makeNavyBtn("Lưu tạm", null);
		btnLuuTam.addActionListener(e -> {
			if (luuDuLieuVaoDatabase("Lưu tạm")) {
				JOptionPane.showMessageDialog(this, "Đã lưu vé chờ thanh toán!");
				stopAllTimers();
				if (onQuayLai != null)
					onQuayLai.accept(secondsLeft);
			}
		});

		JButton btnThanhToan = makeNavyBtn("Thanh toán", loadIcon("/Images/logoGoOn.png", 14, 14));
		btnThanhToan.setHorizontalTextPosition(SwingConstants.LEFT);
		btnThanhToan.addActionListener(e -> {
			if (btnTienMat.isSelected()) {
				showXacNhanThanhToanPopup();
			}
			// Chuyển khoản: không làm gì — bankCheckTimer tự động check qua API
		});

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		left.setBackground(Color.WHITE);
		left.add(btnQuayLai);
		left.add(btnHuyVe);
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
		right.setBackground(Color.WHITE);
		right.add(lblCountdown);
		right.add(btnLuuTam);
		right.add(btnThanhToan);
		bar.add(left, BorderLayout.WEST);
		bar.add(right, BorderLayout.EAST);
		return bar;
	}

	private void showHuyVePopup() {
		Window ancestor = SwingUtilities.getWindowAncestor(this);
		JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setUndecorated(true);
		JPanel glass = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
				g2.setColor(new Color(10, 20, 50));
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.dispose();
			}
		};
		glass.setOpaque(false);
		JPanel box = buildPopupBox(380, 210);
		box.setBorder(new EmptyBorder(28, 32, 24, 32));
		JLabel lblIcon = new JLabel("⚠", SwingConstants.CENTER);
		lblIcon.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 36));
		lblIcon.setForeground(new Color(220, 100, 0));
		JLabel lblMsg = new JLabel(
				"<html><div style='text-align:center;'><b style='font-size:14px;color:#1c396e;'>Xác nhận hủy vé?</b><br><br><span style='font-size:13px;color:#555;'>Tất cả thông tin đặt vé sẽ bị xóa.<br>Bạn sẽ được chuyển về trang tìm chuyến.</span></div></html>",
				SwingConstants.CENTER);
		JPanel topContent = new JPanel(new BorderLayout(0, 10));
		topContent.setOpaque(false);
		topContent.add(lblIcon, BorderLayout.NORTH);
		topContent.add(lblMsg, BorderLayout.CENTER);
		JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
		btnRow.setOpaque(false);
		JButton btnNo = makeOutlineBtn("Không, quay lại", null);
		btnNo.addActionListener(ev -> dialog.dispose());
		JButton btnYes = makeRedBtn("Xác nhận", null);
		btnYes.addActionListener(ev -> {
			dialog.dispose();
			stopAllTimers();
			quayVeTrangDau();
		});
		btnRow.add(btnNo);
		btnRow.add(btnYes);
		box.add(topContent, BorderLayout.CENTER);
		box.add(btnRow, BorderLayout.SOUTH);
		glass.add(box);
		setupAndShowDialog(dialog, glass, ancestor);
	}

	private void showXacNhanThanhToanPopup() {
		Window ancestor = SwingUtilities.getWindowAncestor(this);
		JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setUndecorated(true);
		JPanel glass = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
				g2.setColor(new Color(10, 20, 50));
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.dispose();
			}
		};
		glass.setOpaque(false);
		double tienKhach = parseMoney(txtTienKhachDua.getText());
		String tienKhachStr = tienKhach > 0 ? DF.format(tienKhach) + " VNĐ" : "(chưa nhập)";
		double tienThua = tienKhach > 0 ? tienKhach - tongThanhToan : 0;
		JPanel box = buildPopupBox(400, tienKhach > 0 ? 240 : 220);
		box.setBorder(new EmptyBorder(28, 32, 24, 32));
		String thuaHtml = tienKhach > 0
				? "<br><span style='font-size:14px;color:#555;'>Tiền thừa: <b style='color:#1a7a30;'>"
						+ DF.format(Math.max(0, tienThua)) + " VNĐ</b></span>"
				: "";
		String thieu = tienKhach > 0 && tienThua < 0
				? "<br><span style='font-size:14px;color:#cc0000;'>⚠ Tiền khách đưa chưa đủ!</span>"
				: "";
		JLabel lblMsg = new JLabel(
				"<html><div style='text-align:center;'><b style='font-size:15px;color:#1c396e;'>Xác nhận thanh toán tiền mặt</b><br><br><span style='font-size:14px;color:#555;'>Tổng thanh toán: <b style='color:#c82020;'>"
						+ DF.format(tongThanhToan)
						+ " VNĐ</b></span><br><span style='font-size:14px;color:#555;'>Khách đưa: <b>" + tienKhachStr
						+ "</b></span>" + thuaHtml + thieu + "</div></html>",
				SwingConstants.CENTER);
		JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
		btnRow.setOpaque(false);
		JButton btnNo = makeOutlineBtn("Hủy", null);
		btnNo.addActionListener(ev -> dialog.dispose());
		JButton btnYes = makeNavyBtn("Xác nhận", null);
		btnYes.addActionListener(ev -> {
			dialog.dispose();
			xuLyHoanTatThanhToan("Tiền mặt");
		});
		btnRow.add(btnNo);
		btnRow.add(btnYes);
		box.add(lblMsg, BorderLayout.CENTER);
		box.add(btnRow, BorderLayout.SOUTH);
		glass.add(box);
		setupAndShowDialog(dialog, glass, ancestor);
	}

	private void showThanhCongPopup(Runnable onDone) {
		Window ancestor = SwingUtilities.getWindowAncestor(this);
		JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setUndecorated(true);
		final float[] alpha = { 0f };
		JPanel glass = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
				g2.setColor(new Color(10, 20, 50));
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.dispose();
			}
		};
		glass.setOpaque(false);
		final int[] frame = { 0 };
		final int TOTAL_FRAMES = 30;
		JPanel box = new JPanel(new BorderLayout(0, 12)) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha[0]));
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
				int cx = getWidth() / 2, cy = 70, r = 38;
				float progress = Math.min(1f, (float) frame[0] / TOTAL_FRAMES);
				g2.setColor(new Color(220, 245, 220));
				g2.fillOval(cx - r, cy - r, r * 2, r * 2);
				g2.setColor(new Color(34, 170, 70));
				g2.setStroke(new BasicStroke(3f));
				g2.drawOval(cx - r, cy - r, r * 2, r * 2);
				if (progress > 0) {
					g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					int x1 = cx - 18, y1 = cy, xMid = cx - 6, yMid = cy + 14, x2 = cx + 20, y2 = cy - 16;
					float p1 = Math.min(1f, progress / 0.5f);
					g2.drawLine(x1, y1, (int) (x1 + (xMid - x1) * p1), (int) (y1 + (yMid - y1) * p1));
					if (progress > 0.5f) {
						float p2 = (progress - 0.5f) / 0.5f;
						g2.drawLine(xMid, yMid, (int) (xMid + (x2 - xMid) * p2), (int) (yMid + (y2 - yMid) * p2));
					}
				}
				g2.dispose();
			}
		};
		box.setOpaque(false);
		box.setPreferredSize(new Dimension(320, 240));
		box.setBorder(new EmptyBorder(140, 24, 24, 24));
		JLabel lblMsg = new JLabel(
				"<html><div style='text-align:center;'><b style='font-size:15px;color:#1c396e;'>Thanh toán thành công!</b><br><span style='font-size:13px;color:#888;'>Đang xuất hóa đơn điện tử...</span></div></html>",
				SwingConstants.CENTER);
		box.add(lblMsg, BorderLayout.CENTER);
		glass.add(box);
		dialog.setContentPane(glass);
		dialog.setSize(ancestor.getSize());
		dialog.setLocation(ancestor.getLocation());
		javax.swing.Timer animTimer = new javax.swing.Timer(16, null);
		animTimer.addActionListener(ev -> {
			frame[0]++;
			alpha[0] = Math.min(1f, frame[0] / 20f);
			glass.repaint();
			box.repaint();
			if (frame[0] >= TOTAL_FRAMES)
				animTimer.stop();
		});
		animTimer.start();
		javax.swing.Timer closeTimer = new javax.swing.Timer(2500, ev -> {
			animTimer.stop();
			dialog.dispose();
			if (onDone != null)
				onDone.run();
		});
		closeTimer.setRepeats(false);
		closeTimer.start();
		dialog.setVisible(true);
	}

	private void showZoomedQRDialog() {
		Window ancestor = SwingUtilities.getWindowAncestor(this);
		JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setUndecorated(true);
		JPanel glass = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(new Color(10, 20, 50, 150));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		glass.setOpaque(false);
		JPanel box = buildPopupBox(420, 480);
		box.setBorder(new EmptyBorder(10, 20, 20, 20));
		JPanel pnlHeader = new JPanel(new BorderLayout());
		pnlHeader.setOpaque(false);
		JLabel lblTitle = new JLabel("Mã thanh toán hóa đơn");
		lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
		lblTitle.setForeground(NAVY);
		JButton btnClose = new JButton("✕") {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillOval(0, 0, getWidth(), getHeight());
				g2.dispose();
				super.paintComponent(g);
			}
		};
		btnClose.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
		btnClose.setForeground(new Color(200, 40, 40));
		btnClose.setContentAreaFilled(false);
		btnClose.setBorderPainted(false);
		btnClose.setFocusPainted(false);
		btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnClose.setPreferredSize(new Dimension(28, 28));
		btnClose.addActionListener(e -> dialog.dispose());
		pnlHeader.add(lblTitle, BorderLayout.WEST);
		pnlHeader.add(btnClose, BorderLayout.EAST);
		java.awt.Image scaleImg = originalQRImageIcon.getImage()
			    .getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
			JLabel lblBigQR = new JLabel(new ImageIcon(scaleImg));
		lblBigQR.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lblFoot = new JLabel("Mở ứng dụng ngân hàng/ví điện tử để quét mã", SwingConstants.CENTER);
		lblFoot.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
		lblFoot.setForeground(Color.GRAY);
		box.add(pnlHeader, BorderLayout.NORTH);
		box.add(lblBigQR, BorderLayout.CENTER);
		box.add(lblFoot, BorderLayout.SOUTH);
		glass.add(box);
		setupAndShowDialog(dialog, glass, ancestor);
	}

	private void scaleAndSetQR() {
		if (originalQRImageIcon == null)
			return;
		int size = pnlQR.getWidth() > 40 ? pnlQR.getWidth() - 20 : 160;
		size = Math.min(size, pnlQR.getHeight() > 40 ? pnlQR.getHeight() - 20 : size);
		java.awt.Image img = originalQRImageIcon.getImage()
			    .getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH);
			lblQR.setIcon(new ImageIcon(img));
		lblQR.setText("");
	}

	private void startBankChecking() {
		stopBankChecking();
		bankCheckTimer = new javax.swing.Timer(2000, e -> {
			if (checkBankReceived()) {
				bankCheckTimer.stop();
				xuLyHoanTatThanhToan("Chuyển khoản VietQR");
			}
		});
		bankCheckTimer.start();
	}

	private boolean checkBankReceived() {
	    if (CASSO_API_KEY.equals("YOUR_CASSO_API_KEY"))
	        return false;
	    try {
	        URL url = new URL("https://oauth.casso.vn/v2/transactions?page=1&pageSize=20&sort=DESC");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Authorization", "apikey " + CASSO_API_KEY);
	        conn.setConnectTimeout(5000);
	        conn.setReadTimeout(5000);
	        int code = conn.getResponseCode();
	        if (code != 200) return false;

	        BufferedReader br = new BufferedReader(new InputStreamReader(
	                conn.getInputStream(), StandardCharsets.UTF_8));
	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = br.readLine()) != null) sb.append(line);
	        br.close();
	        String jsonLower = sb.toString().toLowerCase();
	        String maHDNoDash = maHD.replace("-", "").toLowerCase();
	        return jsonLower.contains(maHDNoDash)
	            && jsonLower.contains(String.valueOf((long) tongThanhToan));
	    } catch (Exception e) {
	        return false;
	    }
	}

	private void stopBankChecking() {
		if (bankCheckTimer != null)
			bankCheckTimer.stop();
	}

	private void xuLyHoanTatThanhToan(String hinhThuc) {
	    stopAllTimers();
	    if (luuDuLieuVaoDatabase(hinhThuc)) {
	        showThanhCongPopup(() -> {
	            taoVePDF();              // xuất vé lên tàu
	            taoHoaDonPDF(hinhThuc); // xuất hóa đơn
	            quayVeTrangDau();
	        });
	    }
	}
	// Đặt class này ngoài class DatVeGUI3 hoặc để là static bên trong
	static class RoundBorder implements PdfPCellEvent {
	    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
	        PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
	        cb.setLineWidth(1.2f);
	        cb.setRGBColorStroke(0, 0, 0); 
	        // Vẽ bo góc sát mép cell
	        cb.roundRectangle(position.getLeft() + 1, position.getBottom() + 1, 
	                          position.getWidth() - 2, position.getHeight() - 2, 10);
	        cb.stroke();
	    }
	}
		// --- HÀM TẠO VÉ PDF MỚI (CHUẨN TỶ LỆ 100% ẢNH MẪU) ---
		private void taoVePDF() {
			try {
				File folder = new File("Ve");
				if (!folder.exists()) folder.mkdir();

				// Font chữ (Đảm bảo font này có thật trên máy của bác)
				BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				Font fHeader = new Font(bf, 13, Font.BOLD);
				Font fGaTo = new Font(bf, 15, Font.BOLD);
				Font fTitle = new Font(bf, 14, Font.BOLD);
				Font fSub = new Font(bf, 11, Font.NORMAL);
				Font fLabel = new Font(bf, 11, Font.NORMAL);
				Font fValue = new Font(bf, 11, Font.BOLD);
				Font fGaLabel = new Font(bf, 11, Font.NORMAL);
				Font fGaName = new Font(bf, 13, Font.BOLD);

				for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
					String maVeStr = modelChiTiet.getValueAt(i, 1).toString();
					String loaiVe = modelFromGUI2.getValueAt(i, 2).toString();
					String maGhe = modelFromGUI2.getValueAt(i, 4).toString();
					String tenKH = modelFromGUI2.getValueAt(i, 5).toString();
					String cccd = cheCCCD(modelFromGUI2.getValueAt(i, 6).toString());
					String loaiCho = modelChiTiet.getValueAt(i, 3).toString();
					String hangVe = loaiCho.equals("Giường nằm") ? "VIP" : "Thường";

					// Dùng Mã Chuyến (Cột 12) để tự động truy vấn DB lấy thông tin sạch, tránh lỗi Cột
					String maChuyen = modelFromGUI2.getValueAt(i, 12).toString();
					String tenTau = "", ngayDi = "", gioDi = "", gaDi = "", gaDen = "", maToaTau = "", soGhe = "";

					try (java.sql.Connection con = connect_DB.Connect_DB.getInstance().getConnection()) {
						String sqlCT = "SELECT t.tenTau, ct.thoiGianKhoiHanh, g1.tenGa AS GaDi, g2.tenGa AS GaDen "
								+ "FROM ChuyenTau c JOIN Tau t ON c.maTau = t.maTau "
								+ "JOIN ChiTietChuyenTau ct ON c.maChuyenTau = ct.maChuyenTau "
								+ "JOIN Ga g1 ON ct.maGaDi = g1.maGa JOIN Ga g2 ON ct.maGaDen = g2.maGa "
								+ "WHERE c.maChuyenTau = ?";
						try (java.sql.PreparedStatement ps = con.prepareStatement(sqlCT)) {
							ps.setString(1, maChuyen);
							try (java.sql.ResultSet rs = ps.executeQuery()) {
								if (rs.next()) {
									tenTau = rs.getString("tenTau");
									gaDi = rs.getString("GaDi");
									gaDen = rs.getString("GaDen");
									java.sql.Timestamp ts = rs.getTimestamp("thoiGianKhoiHanh");
									if (ts != null) {
										ngayDi = new SimpleDateFormat("dd/MM/yyyy").format(ts);
										gioDi = new SimpleDateFormat("HH:mm").format(ts);
									}
								}
							}
						}
						String sqlGhe = "SELECT g.soGhe, t.soToa FROM Ghe g JOIN ToaTau t ON g.maToaTau = t.maToaTau WHERE g.maGhe = ?";
						try (java.sql.PreparedStatement ps = con.prepareStatement(sqlGhe)) {
							ps.setString(1, maGhe);
							try (java.sql.ResultSet rs = ps.executeQuery()) {
								if (rs.next()) {
									soGhe = rs.getString("soGhe");
									maToaTau = String.valueOf(rs.getInt("soToa"));
								}
							}
						}
					} catch (Exception ignored) {
					}

					// Đổi Sài Gòn thành Diêu Trì theo yêu cầu của bác
					if (gaDi.equalsIgnoreCase("Sài Gòn")) gaDi = "Diêu Trì";

					// THIẾT LẬP KÍCH THƯỚC TRANG VỪA ĐÚNG KHỔ VÉ MÁY IN (Rộng 280, Cao 520)
					Rectangle pageSize = new Rectangle(280, 520);
					File pdfFile = new File(folder, "Ve_" + maVeStr + ".pdf");
					Document doc = new Document(pageSize, 10, 10, 15, 15);
					PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));
					doc.open();

					// --- TẠO BẢNG BỌC NGOÀI (ÁP DỤNG BO GÓC) ---
					PdfPTable wrapperTable = new PdfPTable(1);
					wrapperTable.setWidthPercentage(100);
					PdfPCell wrapperCell = new PdfPCell();
					wrapperCell.setBorder(Rectangle.NO_BORDER); // Đừng set border ở đây, để RoundBorder vẽ
					wrapperCell.setCellEvent(new RoundBorder()); // <--- DÒNG NÀY LÀ QUAN TRỌNG NHẤT
					wrapperCell.setPadding(15);

					// 1. HEADER
					Paragraph p1 = new Paragraph("TỔNG CÔNG TY ĐƯỜNG SẮT VIỆT NAM", fHeader);
					p1.setAlignment(Element.ALIGN_CENTER);
					wrapperCell.addElement(p1);

					Paragraph p2 = new Paragraph("GA DIÊU TRÌ", fGaTo);
					p2.setAlignment(Element.ALIGN_CENTER);
					wrapperCell.addElement(p2);

					Paragraph pLine = new Paragraph("---------------------------------------------------------", fSub);
					pLine.setAlignment(Element.ALIGN_CENTER);
					wrapperCell.addElement(pLine);

					Paragraph p3 = new Paragraph("VÉ LÊN TÀU HỎA", fTitle);
					p3.setAlignment(Element.ALIGN_CENTER);
					wrapperCell.addElement(p3);

					Paragraph p4 = new Paragraph("BOARDING TICKET", fSub);
					p4.setAlignment(Element.ALIGN_CENTER);
					p4.setSpacingAfter(8);
					wrapperCell.addElement(p4);

					// 2. BARCODE CHUẨN FORM
					com.itextpdf.text.pdf.Barcode128 barcode = new com.itextpdf.text.pdf.Barcode128();
					barcode.setCode(maVeStr);
					barcode.setBarHeight(45f);
					barcode.setX(1.3f);
					barcode.setBaseline(12f);
					barcode.setFont(bf);
					barcode.setSize(10f);
					barcode.setAltText("Mã vé/TicketID: " + maVeStr);
					
					// Ép chuẩn màu để không lỗi thư viện
					java.awt.Image awtImage = barcode.createAwtImage(java.awt.Color.BLACK, java.awt.Color.WHITE);
					com.itextpdf.text.Image imgBar = com.itextpdf.text.Image.getInstance(awtImage, null);
					imgBar.setAlignment(Element.ALIGN_CENTER);
					imgBar.setSpacingAfter(12);
					wrapperCell.addElement(imgBar);

					// 3. KHỐI GA ĐI - GA ĐẾN
					PdfPTable gaTable = new PdfPTable(2);
					gaTable.setWidthPercentage(100);
					gaTable.setSpacingAfter(8);

					PdfPCell cGaDi = new PdfPCell();
					cGaDi.setBorder(Rectangle.NO_BORDER);
					cGaDi.addElement(new Paragraph("Ga đi", fGaLabel));
					cGaDi.addElement(new Paragraph(gaDi.toUpperCase(), fGaName));

					PdfPCell cGaDen = new PdfPCell();
					cGaDen.setBorder(Rectangle.NO_BORDER);
					Paragraph pDenLbl = new Paragraph("Ga đến", fGaLabel);
					pDenLbl.setAlignment(Element.ALIGN_RIGHT);
					cGaDen.addElement(pDenLbl);
					Paragraph pDenVal = new Paragraph(gaDen.toUpperCase(), fGaName);
					pDenVal.setAlignment(Element.ALIGN_RIGHT);
					cGaDen.addElement(pDenVal);

					gaTable.addCell(cGaDi);
					gaTable.addCell(cGaDen);
					wrapperCell.addElement(gaTable);

					// 4. DANH SÁCH THÔNG TIN CHI TIẾT
					PdfPTable infoTable = new PdfPTable(2);
					infoTable.setWidthPercentage(100);
					infoTable.setWidths(new float[] { 1.1f, 1.9f });

					String[][] details = { 
							{ "Số hiệu tàu/Train ID:", tenTau }, 
							{ "Ngày khởi hành/Date:", ngayDi },
							{ "Giờ khởi hành/Time:", gioDi }, 
							{ "Số Toa/Coach:", maToaTau }, 
							{ "Loại Toa/Type:", loaiCho },
							{ "Số ghế/Seat:", soGhe }, 
							{ "Loại vé/Ticket:", loaiVe }, 
							{ "Hạng vé/Class:", hangVe },
							{ "Họ Tên/Name:", tenKH.toUpperCase() }, 
							{ "Giấy tờ/Passport:", cccd } 
					};

					for (String[] d : details) {
						PdfPCell cL = new PdfPCell(new Phrase(d[0], fLabel));
						cL.setBorder(Rectangle.NO_BORDER);
						cL.setPaddingBottom(5);
						infoTable.addCell(cL);

						PdfPCell cR = new PdfPCell(new Phrase(d[1], fValue));
						cR.setBorder(Rectangle.NO_BORDER);
						cR.setPaddingBottom(5);
						infoTable.addCell(cR);
					}

					wrapperCell.addElement(infoTable);
					wrapperTable.addCell(wrapperCell);
					
					// Nạp bảng viền bo góc vào PDF
					doc.add(wrapperTable);
					doc.close();

					if (i == modelChiTiet.getRowCount() - 1 && Desktop.isDesktopSupported()) {
						Desktop.getDesktop().open(pdfFile);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	private PdfPCell createLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPaddingBottom(3);
        return cell;
    }

    private PdfPCell createValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPaddingBottom(3);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
	private String cheCCCD(String cccd) {
        if (cccd != null && cccd.length() > 4) {
            int length = cccd.length();
            if (length == 12) { // Định dạng CCCD thông thường
                return cccd.substring(0, 4) + "****" + cccd.substring(8);
            } else if (length > 6) { // Handling other typical lengths
                int visible = (length - 4) / 2;
                return cccd.substring(0, visible) + "****" + cccd.substring(visible + 4);
            }
        }
        return cccd; // Return as is if too short
    }
	private String docBaSo(int n, boolean hasPrefix) {
		String[] digits = { "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };
		int t = n / 100, c = (n % 100) / 10, d = n % 10;
		StringBuilder res = new StringBuilder();
		if (hasPrefix || t > 0) {
			res.append(digits[t]).append(" trăm ");
			if (c == 0 && d > 0)
				res.append("lẻ ");
		}
		if (c > 1) {
			res.append(digits[c]).append(" mươi ");
			if (d == 1)
				res.append("mốt");
			else if (d == 5)
				res.append("lăm");
			else if (d > 0)
				res.append(digits[d]);
		} else if (c == 1) {
			res.append("mười ");
			if (d == 5)
				res.append("lăm");
			else if (d > 0)
				res.append(digits[d]);
		} else if (d > 0) {
			res.append(digits[d]);
		}
		return res.toString().trim();
	}

	private String docTien(long number) {
		if (number == 0)
			return "Không đồng";
		String[] units = { "", "nghìn", "triệu", "tỷ" };
		StringBuilder result = new StringBuilder();
		long temp = number;
		int unitIndex = 0;
		while (temp > 0) {
			int group = (int) (temp % 1000);
			temp /= 1000;
			if (group > 0) {
				String groupStr = docBaSo(group, temp > 0);
				result.insert(0, groupStr + " " + units[unitIndex] + " ");
			}
			unitIndex++;
		}
		String finalStr = result.toString().replaceAll("\\s+", " ").trim();
		return finalStr.substring(0, 1).toUpperCase() + finalStr.substring(1) + " đồng";
	}

	private void taoHoaDonPDF(String hinhThuc) {
		try {
			File folder = new File("HoaDon");
			if (!folder.exists())
				folder.mkdir();
			File pdfFile = new File(folder, maHD + ".pdf");

			Document document = new Document(PageSize.A4);
			PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
			document.open();

			BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font fontTitle = new Font(bf, 16, Font.BOLD);
			Font fontBold = new Font(bf, 11, Font.BOLD);
			Font fontNormal = new Font(bf, 11, Font.NORMAL);
			Font fontItalic = new Font(bf, 11, Font.ITALIC);

			Paragraph title = new Paragraph("HÓA ĐƠN GIÁ TRỊ GIA TĂNG", fontTitle);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
			Paragraph dateP = new Paragraph("Ngày xuất: " + dateStr, fontItalic);
			dateP.setAlignment(Element.ALIGN_CENTER);
			document.add(dateP);
			document.add(new Paragraph(" ", fontNormal));
			document.add(new Paragraph(" ", fontNormal));

			document.add(new Paragraph("Đơn vị bán hàng: CÔNG TY CỔ PHẦN VẬN TẢI ĐƯỜNG SẮT", fontBold));
			document.add(new Paragraph("Mã số thuế: 0100106264", fontNormal));
			document.add(new Paragraph("Địa chỉ: 113 Nguyễn Đình Thụ, Tuy Phước, Gia Lai", fontNormal));
			document.add(new Paragraph(" ", fontNormal));

			String tenKH = modelFromGUI2.getRowCount() > 0 ? modelFromGUI2.getValueAt(0, 5).toString() : "Khách lẻ";
			String sdtKH = modelFromGUI2.getRowCount() > 0 ? modelFromGUI2.getValueAt(0, 7).toString() : "";
			document.add(new Paragraph("Họ tên người mua hàng: " + tenKH, fontBold));
			document.add(new Paragraph("Điện thoại: " + sdtKH, fontNormal));
			document.add(new Paragraph("Hình thức thanh toán: " + hinhThuc + "          Mã HĐ: " + maHD, fontNormal));
			document.add(new Paragraph(" ", fontNormal));

			PdfPTable table = new PdfPTable(10);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 1f, 3.5f, 2f, 2.3f, 1.8f, 2f, 1f, 1f, 2f, 2.2f });

			String[] headers = { "STT", "Tên dịch vụ", "Loại vé", "Mã vé", "Chiều", "Giá vé", "ĐVT", "SL", "Khuyến mãi",
					"Thành tiền" };
			for (String h : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(h, fontBold));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setPaddingBottom(6);
				cell.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
				table.addCell(cell);
			}

			for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
				PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(i + 1), fontNormal));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c1);

				PdfPCell c2 = new PdfPCell(new Phrase("Ve HK truc tiep tai nha ga", fontNormal));
				c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c2);

				PdfPCell c3 = new PdfPCell(new Phrase(modelFromGUI2.getValueAt(i, 2).toString(), fontNormal));
				c3.setHorizontalAlignment(Element.ALIGN_CENTER);
				c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c3);

				PdfPCell c4 = new PdfPCell(new Phrase(modelChiTiet.getValueAt(i, 1).toString(), fontNormal));
				c4.setHorizontalAlignment(Element.ALIGN_CENTER);
				c4.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c4);

				PdfPCell c5 = new PdfPCell(new Phrase(modelChiTiet.getValueAt(i, 2).toString(), fontNormal));
				c5.setHorizontalAlignment(Element.ALIGN_CENTER);
				c5.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c5);

				String donGiaStr = modelChiTiet.getValueAt(i, 4).toString().replace(" VNĐ", "");
				PdfPCell c6 = new PdfPCell(new Phrase(donGiaStr, fontNormal));
				c6.setHorizontalAlignment(Element.ALIGN_RIGHT);
				c6.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c6);

				PdfPCell c7 = new PdfPCell(new Phrase("Vé", fontNormal));
				c7.setHorizontalAlignment(Element.ALIGN_CENTER);
				c7.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c7);

				PdfPCell c8 = new PdfPCell(new Phrase("1", fontNormal));
				c8.setHorizontalAlignment(Element.ALIGN_CENTER);
				c8.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c8);

				String kmStr = modelChiTiet.getValueAt(i, 6).toString().replace(" VNĐ", "");
				PdfPCell c9 = new PdfPCell(new Phrase(kmStr, fontNormal));
				c9.setHorizontalAlignment(Element.ALIGN_RIGHT);
				c9.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c9);

				String thanhTienStr = modelChiTiet.getValueAt(i, 7).toString().replace(" VNĐ", "");
				PdfPCell c10 = new PdfPCell(new Phrase(thanhTienStr, fontNormal));
				c10.setHorizontalAlignment(Element.ALIGN_RIGHT);
				c10.setVerticalAlignment(Element.ALIGN_MIDDLE);
				table.addCell(c10);
			}
			document.add(table);
			document.add(new Paragraph(" ", fontNormal));

			Paragraph pTongTien = new Paragraph("Tổng tiền: " + DF.format(tongGiaGoc) + " VNĐ", fontBold);
			pTongTien.setAlignment(Element.ALIGN_RIGHT);
			document.add(pTongTien);

			Paragraph pTongKM = new Paragraph("Tổng khuyến mãi: " + DF.format(tongGiamDoiTuong + giamVoucher) + " VNĐ",
					fontBold);
			pTongKM.setAlignment(Element.ALIGN_RIGHT);
			document.add(pTongKM);

			Paragraph pConLai = new Paragraph("Còn lại: " + DF.format(tongThanhToan) + " VNĐ", fontBold);
			pConLai.setAlignment(Element.ALIGN_RIGHT);
			document.add(pConLai);

			document.add(new Paragraph(" ", fontNormal));

			Phrase phraseTienChu = new Phrase();
			phraseTienChu.add(new com.itextpdf.text.Chunk("Số tiền viết bằng chữ: ", fontNormal));
			phraseTienChu.add(new com.itextpdf.text.Chunk(docTien((long) tongThanhToan), fontItalic));
			document.add(new Paragraph(phraseTienChu));

			document.add(new Paragraph(
					"Ghi chú: ......................................................................................................................................",
					fontNormal));
			document.add(new Paragraph(" ", fontNormal));
			document.add(new Paragraph(" ", fontNormal));

			PdfPTable signTable = new PdfPTable(2);
			signTable.setWidthPercentage(100);
			PdfPCell cellBuyer = new PdfPCell(new Phrase("Người mua hàng\n(Ký, ghi rõ họ tên)", fontNormal));
			cellBuyer.setHorizontalAlignment(Element.ALIGN_CENTER);
			cellBuyer.setBorder(PdfPCell.NO_BORDER);
			PdfPCell cellSeller = new PdfPCell(new Phrase("Người bán hàng\n(Ký, ghi rõ họ tên)", fontNormal));
			cellSeller.setHorizontalAlignment(Element.ALIGN_CENTER);
			cellSeller.setBorder(PdfPCell.NO_BORDER);
			signTable.addCell(cellBuyer);
			signTable.addCell(cellSeller);
			document.add(signTable);

			document.close();
			if (Desktop.isDesktopSupported())
				Desktop.getDesktop().open(pdfFile);
		} catch (Exception e) {
		}
	}

	private void quayVeTrangDau() {
		Container parent = getParent();
		if (parent == null) {
			if (onHuyVe != null) {
				onHuyVe.run();
			}
			return;
		}
		LayoutManager lm = parent.getLayout();
		if (lm instanceof CardLayout) {
			for (Component c : parent.getComponents()) {
				if (c instanceof DatVeGUI)
					((DatVeGUI) c).resetForm();
			}
			((CardLayout) lm).show(parent, "dat-ve");
		} else {
			if (onHuyVe != null) {
				onHuyVe.run();
			}
		}
	}

	private boolean luuDuLieuVaoDatabase(String hinhThucThanhToan) {
		java.sql.Connection con = null;
		try {
			con = connect_DB.Connect_DB.getInstance().getConnection();
			con.setAutoCommit(false);
			String maNV = "NV001";
			try (java.sql.Statement st = con.createStatement();
					java.sql.ResultSet rsNV = st.executeQuery("SELECT TOP 1 maNV FROM NhanVien")) {
				if (rsNV.next())
					maNV = rsNV.getString(1);
			}
			String maKH = null;
			String sdtKhach = modelFromGUI2.getRowCount() > 0 ? modelFromGUI2.getValueAt(0, 7).toString() : "";
			try {
				entity.KhachHang kh = new dao.KhachHangDAO().timTheoSDT(sdtKhach);
				if (kh != null)
					maKH = kh.getMaKH();
			} catch (Exception e2) {
				maKH = null;
			}
			String sqlHD = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) VALUES (?, GETDATE(), ?, ?, ?, ?, ?)";
			try (java.sql.PreparedStatement psHD = con.prepareStatement(sqlHD)) {
				psHD.setString(1, maHD);
				psHD.setString(2, maNV);
				if (maKH != null)
					psHD.setString(3, maKH);
				else
					psHD.setNull(3, java.sql.Types.VARCHAR);

				// Đảm bảo cập nhật tổng tiền bằng đúng số tiền "Còn lại"
				psHD.setDouble(4, tongThanhToan);

				double tienKhach = hinhThucThanhToan.contains("Chuyển khoản") ? tongThanhToan
						: parseMoney(txtTienKhachDua.getText());
				if (tienKhach <= 0)
					tienKhach = tongThanhToan;
				psHD.setDouble(5, tienKhach);
				String pt = hinhThucThanhToan.contains("Tiền mặt") ? "TIEN_MAT"
						: hinhThucThanhToan.equals("Lưu tạm") ? "LUU_TAM" : "CHUYEN_KHOAN";
				psHD.setString(6, pt);
				psHD.executeUpdate();
			}
			String sqlVe = "INSERT INTO Ve (maVe, ngayMua, loaiVe, trangThaiVe, giaVe, maGhe, maHoaDon, maChuyenTau, maKH, maKhuyenMai) VALUES (?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, NULL)";
			try (java.sql.PreparedStatement psVe = con.prepareStatement(sqlVe)) {
				String trangThai = hinhThucThanhToan.equals("Lưu tạm") ? "Chờ thanh toán" : "Đã thanh toán";

				int totalTickets = modelChiTiet.getRowCount();
				boolean isKhuHoi = modelFromGUI2.getRowCount() > 0
						&& modelFromGUI2.getValueAt(0, 2).toString().toLowerCase().contains("hồi");

				for (int i = 0; i < totalTickets; i++) {
					String maVeHienTai = modelChiTiet.getValueAt(i, 1).toString();

					psVe.setString(1, maVeHienTai);
					psVe.setString(2, isKhuHoi ? "KHU_HOI" : "MOT_CHIEU");
					psVe.setString(3, trangThai);

					// Cập nhật giá vé trong bảng chi tiết khớp với "Thành tiền" đã giảm
					psVe.setDouble(4, parseMoney(modelChiTiet.getValueAt(i, 7).toString()));

					psVe.setString(5, modelFromGUI2.getValueAt(i, 4).toString());
					psVe.setString(6, maHD);
					psVe.setString(7, modelFromGUI2.getValueAt(i, 12).toString());
					if (maKH != null)
						psVe.setString(8, maKH);
					else
						psVe.setNull(8, java.sql.Types.VARCHAR);

					psVe.addBatch();
				}
				psVe.executeBatch();
			}
			con.commit();
			return true;
		} catch (Exception e) {
			if (con != null)
				try {
					con.rollback();
				} catch (Exception ex) {
				}
			return false;
		} finally {
			if (con != null)
				try {
					con.setAutoCommit(true);
					con.close();
				} catch (Exception ex) {
				}
		}
	}

	private void tinhToanTaiChinh() {
		tongGiaGoc = 0;
		tongGiamDoiTuong = 0;
		for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
			tongGiaGoc += parseMoney(modelChiTiet.getValueAt(i, 4).toString());
			tongGiamDoiTuong += parseMoney(modelChiTiet.getValueAt(i, 6).toString());
		}
		double tongKM = tongGiamDoiTuong + giamVoucher;
		tongThanhToan = Math.max(0, tongGiaGoc - tongKM);
		lblTongTien.setText(
				"<html><font color='#505050'>Tổng tiền: </font><b>" + DF.format(tongGiaGoc) + " VNĐ</b></html>");
		lblTongKhuyenMai
				.setText("<html><font color='#505050'>Tổng KM: </font><b>" + DF.format(tongKM) + " VNĐ</b></html>");
		lblThanhToanConLai.setText("<html><font color='#505050'>Còn lại: </font><b><font color='#C82020'>"
				+ DF.format(tongThanhToan) + " VNĐ</font></b></html>");
		if (btnChuyenKhoan != null && btnChuyenKhoan.isSelected())
			toggleQRCode();
		else
			tinhTienThua();
	}

	private void tinhTienThua() {
		if (txtTienKhachDua == null || lblTienThua == null)
			return;
		double tk = parseMoney(txtTienKhachDua.getText());
		double thua = tk - tongThanhToan;
		if (tk == 0) {
			lblTienThua.setText("0 VNĐ");
			lblTienThua.setForeground(Color.BLACK);
		} else if (thua < 0) {
			lblTienThua.setText("Thiếu: " + DF.format(Math.abs(thua)) + " VNĐ");
			lblTienThua.setForeground(Color.RED);
		} else {
			lblTienThua.setText(DF.format(thua) + " VNĐ");
			lblTienThua.setForeground(new Color(0, 140, 0));
		}
	}

	private void nhapKhuyenMai() {
		String ma = JOptionPane.showInputDialog(this, "Nhập mã khuyến mãi:");
		if (ma != null && ma.trim().equalsIgnoreCase("SALE100")) {
			giamVoucher = 100000;
		} else if (ma != null && !ma.trim().isEmpty()) {
			giamVoucher = 0;
		} else {
			giamVoucher = 0;
		}
		tinhToanTaiChinh();
	}

	private void toggleQRCode() {
		if (!btnChuyenKhoan.isSelected())
			return;
		if (tongThanhToan <= 0) {
			lblQR.setIcon(null);
			lblQR.setText("Hóa đơn 0đ");
			return;
		}
		lblQR.setIcon(null);
		lblQR.setText("Đang tạo mã VietQR...");
		new SwingWorker<ImageIcon, Void>() {
			@Override
			protected ImageIcon doInBackground() throws Exception {
				String info = URLEncoder.encode("Thanh toan ve tau " + maHD, StandardCharsets.UTF_8);
				String url = String.format(
						"https://img.vietqr.io/image/970422-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
						ACCOUNT_NO, (long) tongThanhToan, info,
						URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8));
				return new ImageIcon(new java.net.URL(url));
			}

			@Override
			protected void done() {
				try {
					originalQRImageIcon = get();
					scaleAndSetQR();
				} catch (Exception e) {
					lblQR.setIcon(null);
					lblQR.setText("Lỗi tạo QR!");
				}
			}
		}.execute();
	}

	private void startCountdown() {
		if (countdownTimer != null)
			countdownTimer.stop();
		countdownTimer = new javax.swing.Timer(1000, e -> {
			secondsLeft--;
			if (secondsLeft <= 0) {
				stopAllTimers();
				lblCountdown.setText("Hết thời gian!");
				if (onQuayLai != null)
					onQuayLai.accept(0);
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

	private void stopAllTimers() {
		if (countdownTimer != null)
			countdownTimer.stop();
		if (bankCheckTimer != null)
			bankCheckTimer.stop();
	}

	private JPanel buildPopupBox(int w, int h) {
		JPanel box = new JPanel(new BorderLayout(0, 16)) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
				g2.dispose();
			}
		};
		box.setOpaque(false);
		box.setPreferredSize(new Dimension(w, h));
		return box;
	}

	private void setupAndShowDialog(JDialog dialog, JPanel glass, Window ancestor) {
		dialog.setContentPane(glass);
		dialog.getRootPane().registerKeyboardAction(ev -> dialog.dispose(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		dialog.setSize(ancestor.getSize());
		dialog.setLocation(ancestor.getLocation());
		dialog.setVisible(true);
	}

	private double parseMoney(String str) {
		try {
			return Double.parseDouble(str.replaceAll("[^0-9]", ""));
		} catch (Exception e) {
			return 0;
		}
	}

	private String taoMaHoaDon() {
		try (java.sql.Connection con = connect_DB.Connect_DB.getInstance().getConnection()) {
			return MaTuDong.taoMaHoaDon(con, LocalDate.now());
		} catch (Exception e) {
			return "HD" + new SimpleDateFormat("ddMMyy").format(new Date()) + "-"
					+ String.format("%04d", System.currentTimeMillis() % 10000);
		}
	}

	private JPanel createDetailLabel(String title, String value) {
		JPanel p = new JPanel(new BorderLayout()) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
			}
		};
		p.setOpaque(false);
		JLabel l1 = new JLabel(title);
		l1.setFont(FONT_14);
		l1.setForeground(new Color(80, 80, 80));
		JLabel l2 = new JLabel(value);
		l2.setFont(FONT_B14);
		l2.setForeground(Color.BLACK);
		p.add(l1, BorderLayout.WEST);
		p.add(l2, BorderLayout.EAST);
		return p;
	}

	private JToggleButton createToggleBtn(String text) {
		JToggleButton b = new JToggleButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (isSelected()) {
					g2.setColor(new Color(240, 246, 255));
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
					g2.setColor(NAVY);
					g2.setStroke(new BasicStroke(1.8f));
					g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 4, 4);
				} else {
					g2.setColor(Color.WHITE);
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
					g2.setColor(BORDER_C);
					g2.setStroke(new BasicStroke(1f));
					g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
				}
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.addChangeListener(e -> {
			b.setFont(b.isSelected() ? FONT_B14 : FONT_14);
			b.setForeground(b.isSelected() ? NAVY : new Color(80, 80, 80));
		});
		b.setFont(FONT_14);
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		b.setBorder(new EmptyBorder(6, 12, 6, 12));
		return b;
	}

	private Icon loadIcon(String path, int w, int h) {
		try {
			java.net.URL url = getClass().getResource(path);
			if (url != null)
				return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH));
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
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
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
				g2.setColor(getModel().isPressed() ? new Color(220, 230, 245)
						: getModel().isRollover() ? new Color(230, 240, 250) : new Color(242, 247, 252));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
				g2.setColor(NAVY);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		if (icon != null)
			b.setIcon(icon);
		b.setFont(FONT_14);
		b.setForeground(NAVY);
		b.setIconTextGap(8);
		b.setContentAreaFilled(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JButton makeRedBtn(String text, Icon icon) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? new Color(160, 20, 20)
						: getModel().isRollover() ? new Color(200, 40, 40) : new Color(210, 30, 40));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
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
		b.setBorder(new EmptyBorder(6, 16, 6, 16));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}
}
