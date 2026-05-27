package gui;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import com.itextpdf.text.Rectangle;
import connect_DB.Connect_DB;
import service.AuthService;
import util.MaTuDong;

public class DoiVeGUI2 extends JPanel {

	private static final Color NAVY = new Color(28, 57, 110);
	private static final Color BG = new Color(242, 247, 252);
	private static final Color BORDER_C = new Color(180, 205, 230);
	private static final java.awt.Font FONT_14 = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
	private static final java.awt.Font FONT_B14 = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
	private static final DecimalFormat DF = new DecimalFormat("#,###");

	private static final String ACCOUNT_NO = "0382588430";
	private static final String ACCOUNT_NAME = "MB Bank";
	private static final String CASSO_API_KEY = "AK_CS.69d49310536411f1ad2d7bbf51f870c4.1OR4aZOPpK4BslQXgsgNQGlFiMwe8EDKc6Tuva6vzVcTf7ssLfssoXfn5vVKU27z4bemHq6E";

	// ── Dữ liệu tĩnh từ DoiVeGUI1 ───────────────────────────────────────
	private static String s_maVe = "";
	private static String[] s_dataCu = new String[0];
	private static String s_chuyenMoi = "";
	private static String s_ngayMoi = "";
	private static String s_gheMoiDB = "";
	private static String s_gheMoiHienThi = "";
	private static long s_tongThu = 0;
	private static long s_giaVeMoi = 0;
	// Thêm: loại chỗ và chiều vé từ DoiVeGUI1
	private static String s_loaiCho = "";
	private static String s_chieuVe = "";
	private static String s_gaDi = "";
	private static String s_gaDen = "";
	private static String s_maVeMoi = "";
	private static long s_giaVeMoiThuc = 0;
	public static void setGiaVeMoiThuc(long gia) {
	    s_giaVeMoiThuc = gia;
	}
	public static void setMaVeMoi(String maVeMoi) {
	    s_maVeMoi = (maVeMoi != null) ? maVeMoi : "";
	}
	public static void setDuLieuThanhToan(String maVe, String[] dataCu, String chuyenMoi, String ngayMoi,
			String gheMoiDB, String gheMoiHienThi, long tongThu, long giaVeMoi) {
		s_maVe = maVe;
		s_dataCu = (dataCu != null) ? dataCu.clone() : new String[0];
		s_chuyenMoi = chuyenMoi;
		s_ngayMoi = ngayMoi;
		s_gheMoiDB = gheMoiDB;
		s_gheMoiHienThi = gheMoiHienThi;
		s_tongThu = tongThu;
		s_giaVeMoi = giaVeMoi;
	}

	public static void setGaDiGaDen(String gaDi, String gaDen) {
		s_gaDi = (gaDi != null) ? gaDi : "";
		s_gaDen = (gaDen != null) ? gaDen : "";
	}

	// Overload: truyền thêm loaiCho và chieuVe
	public static void setDuLieuThanhToan(String maVe, String[] dataCu, String chuyenMoi, String ngayMoi,
			String gheMoiDB, String gheMoiHienThi, long tongThu, long giaVeMoi, String loaiCho, String chieuVe) {
		setDuLieuThanhToan(maVe, dataCu, chuyenMoi, ngayMoi, gheMoiDB, gheMoiHienThi, tongThu, giaVeMoi);
		s_loaiCho = (loaiCho != null) ? loaiCho : "";
		s_chieuVe = (chieuVe != null) ? chieuVe : "";
	}

	private final AppFrame appFrame;
	private String maVeMoiHienThi = "";
	private String ghiChu = "";

	private JTable tblChiTiet;
	private DefaultTableModel modelChiTiet;

	private JLabel lblMaNV_Val, lblTenNV_Val, lblMaKH_Val, lblTenKH_Val, lblSdtKH_Val;
	private JLabel lblTongTien, lblPhiDoiVe, lblKhachBu, lblThanhToanConLai;
	private JLabel lblQR, lblCountdown;
	private JToggleButton btnTienMat, btnChuyenKhoan;
	private JPanel pnlSwitch, pnlTienMat, pnlQR;
	private CardLayout cardSwitch;
	private JTextField txtTienKhachDua;
	private JLabel lblTienThua;
	private javax.swing.Timer countdownTimer, bankCheckTimer;
	private int secondsLeft = 600;
	private ImageIcon originalQRImageIcon = null;

	private String tenKH = "Khách vãng lai", sdtKH = "N/A", maKH = "N/A";
	private String maDon = taoMaHoaDon();

	public DoiVeGUI2(AppFrame appFrame) {
		this.appFrame = appFrame;
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
	}

	public void refresh() {
		System.out.println("DEBUG s_gheMoiDB = " + s_gheMoiDB);
		secondsLeft = 600;
		maDon = taoMaHoaDon();
		maVeMoiHienThi = "";
		ghiChu = "";
		startCountdown();
		loadKhachHangInfo();

		lblMaNV_Val.setText(nvOrNA(AuthService.getCurrentMaNV()));
		lblTenNV_Val.setText(nvOrNA(AuthService.getCurrentHoTen()));
		lblMaKH_Val.setText(nvOrNA(maKH));
		lblTenKH_Val.setText(nvOrNA(tenKH));
		lblSdtKH_Val.setText(nvOrNA(sdtKH));

		maVeMoiHienThi = !s_maVeMoi.isEmpty() ? s_maVeMoi
	               : "VE" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();

		// ── Điền bảng chi tiết ─────────────────────────────────────────
		modelChiTiet.setRowCount(0);

		// Lấy giá vé mới (sau giảm đối tượng) = s_giaVeMoi - phí đổi vé
		long phiDoiVe = 30_000L;

		// Loại chỗ: ưu tiên s_loaiCho, fallback từ dataCu
		String loaiCho = queryLoaiGheTheoMaToa(s_gheMoiDB);
		if (loaiCho.isEmpty()) loaiCho = !s_loaiCho.isEmpty() ? s_loaiCho : safeData(3);
		long giaVeMoiThuc = s_giaVeMoiThuc > 0 ? s_giaVeMoiThuc : s_giaVeMoi - phiDoiVe;
		// Chiều vé: ưu tiên s_chieuVe, fallback từ dataCu
		String chieuVe = !s_chieuVe.isEmpty() ? s_chieuVe : safeData(4);

		String gaDi = !s_gaDi.isEmpty() ? s_gaDi : "—";
		String gaDen = !s_gaDen.isEmpty() ? s_gaDen : "—";
		modelChiTiet.addRow(
				new Object[] { 1, maVeMoiHienThi, chieuVe, gaDi, gaDen, loaiCho, DF.format(giaVeMoiThuc) + " VNĐ" });

		tinhToanTaiChinh();

		// Tiền mặt mặc định
		btnTienMat.setSelected(true);
		cardSwitch.show(pnlSwitch, "TIEN_MAT");
		txtTienKhachDua.setText("");
		txtTienKhachDua.setEnabled(s_tongThu > 0);
		lblTienThua.setText("0 VNĐ");
		lblTienThua.setForeground(Color.BLACK);
	}

	private String nvOrNA(String s) {
		return (s != null && !s.isEmpty()) ? s : "N/A";
	}

	private String safeData(int i) {
		return (s_dataCu != null && i < s_dataCu.length && s_dataCu[i] != null) ? s_dataCu[i] : "";
	}

	private void loadKhachHangInfo() {
		String sql = "SELECT kh.maKH, kh.hoTenKH, kh.sdt FROM Ve v " + "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon "
				+ "JOIN KhachHang kh ON hd.maKH = kh.maKH WHERE v.maVe = ?";
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, s_maVe);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					maKH = rs.getString("maKH");
					tenKH = rs.getString("hoTenKH");
					sdtKH = rs.getString("sdt");
				}
			}
		} catch (Exception ignored) {
		}
	}
	private String queryLoaiGheTheoMaVe(String maVe) {
	    String sql = "SELECT g.loaiGhe FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe WHERE v.maVe = ?";
	    try (Connection con = Connect_DB.getInstance().getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setString(1, maVe);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                String raw = rs.getString("loaiGhe");
	                return switch (raw == null ? "" : raw.trim()) {
	                    case "GHE_CUNG"   -> "Ghế cứng";
	                    case "GHE_MEM"    -> "Ghế mềm";
	                    case "GIUONG_NAM" -> "Giường nằm";
	                    default           -> raw;
	                };
	            }
	        }
	    } catch (Exception ignored) {}
	    return "";
	}
	private String queryLoaiGheTheoMaToa(String maGheDB) {
	    if (maGheDB == null || maGheDB.isEmpty()) return "";
	    int tIdx = maGheDB.indexOf('T');
	    if (tIdx < 0) return "";
	    String maToaTau = maGheDB.substring(tIdx);

	    String sql = "SELECT soToa FROM ToaTau WHERE maToaTau = ?";
	    try (Connection con = Connect_DB.getInstance().getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setString(1, maToaTau);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                int soToa = rs.getInt("soToa");
	                if (soToa <= 4)      return "Ghế cứng";
	                else if (soToa <= 8) return "Giường nằm";
	                else                 return "Ghế mềm";
	            }
	        }
	    } catch (Exception ignored) {}
	    return "";
	}
	// ════════════════════════════════════════════════════════════════════
	// BUILD LEFT PANEL — bảng chi tiết (giống DatVeGUI3)
	// ════════════════════════════════════════════════════════════════════
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

		// Cột: STT | Mã vé mới | Chiều vé | Loại chỗ | Đơn giá
		String[] cols = { "STT", "Mã vé mới", "Chiều vé", "Ga đi", "Ga đến", "Loại chỗ", "Đơn giá" };
		modelChiTiet = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

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
		// Xóa hết các dòng tcm.getColumn cũ, thay bằng:
		tcm.getColumn(0).setMaxWidth(40);
		tcm.getColumn(1).setMinWidth(110);
		tcm.getColumn(1).setMaxWidth(140);
		tcm.getColumn(2).setMinWidth(70);
		tcm.getColumn(2).setMaxWidth(90);
		tcm.getColumn(3).setPreferredWidth(120);
		tcm.getColumn(4).setPreferredWidth(120);
		tcm.getColumn(5).setPreferredWidth(110);
		tcm.getColumn(6).setPreferredWidth(110);

		DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
		centerR.setHorizontalAlignment(SwingConstants.CENTER);
		tblChiTiet.getColumnModel().getColumn(0).setCellRenderer(centerR);
		tblChiTiet.getColumnModel().getColumn(2).setCellRenderer(centerR);

		DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
		rightR.setHorizontalAlignment(SwingConstants.RIGHT);
		tblChiTiet.getColumnModel().getColumn(6).setCellRenderer(rightR);

		JScrollPane scroll = new JScrollPane(tblChiTiet);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(Color.WHITE);
		pnlTableWrapper.add(scroll, BorderLayout.CENTER);

		// ── Bottom: nút + tổng tiền ──────────────────────────────────
		JPanel pnlBottomLeft = new JPanel(new BorderLayout(0, 6));
		pnlBottomLeft.setOpaque(false);

		JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlBtns.setOpaque(false);
		JButton btnGhiChu = makeNavyBtn("Ghi chú", null);
		btnGhiChu.addActionListener(e -> {
			String input = JOptionPane.showInputDialog(this, "Nhập ghi chú cho hóa đơn đổi vé:", ghiChu);
			if (input != null)
				ghiChu = input.trim();
		});
		pnlBtns.add(btnGhiChu);

		JPanel pnlTotals = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
		pnlTotals.setOpaque(false);
		lblTongTien = new JLabel();
		lblTongTien.setFont(FONT_14);
		lblPhiDoiVe = new JLabel();
		lblPhiDoiVe.setFont(FONT_14);
		lblKhachBu = new JLabel();
		lblKhachBu.setFont(FONT_14);
		lblThanhToanConLai = new JLabel();
		lblThanhToanConLai.setFont(FONT_B14);
		pnlTotals.add(lblTongTien);
		pnlTotals.add(lblPhiDoiVe);
		pnlTotals.add(lblKhachBu);
		pnlTotals.add(lblThanhToanConLai);

		JPanel pnlBtnTotals = new JPanel(new BorderLayout());
		pnlBtnTotals.setOpaque(false);
		pnlBtnTotals.add(pnlBtns, BorderLayout.NORTH);
		pnlBtnTotals.add(pnlTotals, BorderLayout.SOUTH);
		pnlBottomLeft.add(pnlBtnTotals, BorderLayout.SOUTH);

		pnlLeft.add(pnlTableWrapper, BorderLayout.CENTER);
		pnlLeft.add(pnlBottomLeft, BorderLayout.SOUTH);
		return pnlLeft;
	}

	// ════════════════════════════════════════════════════════════════════
	// BUILD RIGHT PANEL — thông tin hóa đơn (y chang DatVeGUI3)
	// ════════════════════════════════════════════════════════════════════
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

		// Khởi tạo label trước để refresh() gọi được
		lblMaNV_Val = new JLabel("N/A");
		lblMaNV_Val.setFont(FONT_B14);
		lblTenNV_Val = new JLabel("N/A");
		lblTenNV_Val.setFont(FONT_B14);
		lblMaKH_Val = new JLabel("N/A");
		lblMaKH_Val.setFont(FONT_B14);
		lblTenKH_Val = new JLabel("N/A");
		lblTenKH_Val.setFont(FONT_B14);
		lblSdtKH_Val = new JLabel("N/A");
		lblSdtKH_Val.setFont(FONT_B14);

		content.add(createDetailRow("Mã nhân viên:", lblMaNV_Val));
		content.add(Box.createVerticalStrut(4));
		content.add(createDetailRow("Tên nhân viên:", lblTenNV_Val));
		content.add(Box.createVerticalStrut(4));
		content.add(createDetailRow("Mã khách hàng:", lblMaKH_Val));
		content.add(Box.createVerticalStrut(4));
		content.add(createDetailRow("Tên khách hàng:", lblTenKH_Val));
		content.add(Box.createVerticalStrut(4));
		content.add(createDetailRow("Số điện thoại:", lblSdtKH_Val));
		content.add(Box.createVerticalStrut(10));
		content.add(new JSeparator());
		content.add(Box.createVerticalStrut(10));

		// Phương thức thanh toán
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

		// Card switch
		cardSwitch = new CardLayout();
		pnlSwitch = new JPanel(cardSwitch) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
			}
		};
		pnlSwitch.setOpaque(false);

		// ── Tiền mặt panel ──────────────────────────────────────────
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
					txtTienKhachDua.setText(raw.isEmpty() ? "" : DF.format(Double.parseDouble(raw)));
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

		// ── QR panel ────────────────────────────────────────────────
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

	// ════════════════════════════════════════════════════════════════════
	// BUILD BOTTOM BAR
	// ════════════════════════════════════════════════════════════════════
	private JPanel buildBottomBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

		JButton btnQuayLai = makeOutlineBtn("Quay lại", null);
		btnQuayLai.setBorder(new EmptyBorder(6, 16, 6, 16));
		btnQuayLai.addActionListener(e -> {
			stopAllTimers();
			appFrame.showCard("doi-ve-step-2");
		});

		JButton btnHuy = makeRedBtn("Hủy đổi vé", null);
		btnHuy.addActionListener(e -> showHuyPopup());

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

		JButton btnThanhToan = makeNavyBtn("Xác nhận & Đổi vé", null);
		btnThanhToan.addActionListener(e -> {
			if (btnTienMat.isSelected()) {
				showXacNhanThanhToanPopup();
			}
			// Chuyển khoản: bankCheckTimer tự động xử lý
		});

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		left.setBackground(Color.WHITE);
		left.add(btnQuayLai);
		left.add(btnHuy);
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
		right.setBackground(Color.WHITE);
		right.add(lblCountdown);
		right.add(btnThanhToan);
		bar.add(left, BorderLayout.WEST);
		bar.add(right, BorderLayout.EAST);
		return bar;
	}

	// ════════════════════════════════════════════════════════════════════
	// TÍNH TOÁN TÀI CHÍNH
	// ════════════════════════════════════════════════════════════════════
	private void tinhToanTaiChinh() {
		long phiDoiVe = 30_000L;
		long giaVeMoiThuc = s_giaVeMoiThuc > 0 ? s_giaVeMoiThuc : s_giaVeMoi - phiDoiVe;
		long khachBu = s_tongThu;

		lblTongTien.setText(
				"<html><font color='#505050'>Giá vé mới: </font><b>" + DF.format(giaVeMoiThuc) + " VNĐ</b></html>");
		lblPhiDoiVe
				.setText("<html><font color='#505050'>Phí đổi: </font><b>" + DF.format(phiDoiVe) + " VNĐ</b></html>");

		if (khachBu > 0) {
			long chenhLech = s_giaVeMoi - phiDoiVe - (s_giaVeMoi - s_tongThu - phiDoiVe); // = s_tongThu - phiDoiVe thực
																							// ra là giaVeMoiThuc -
																							// giaVeCu
			// Đơn giản hơn: chenhLech = s_tongThu - phiDoiVe
			long chenhLechVal = s_tongThu - phiDoiVe;
			if (chenhLechVal >= 0) {
				lblKhachBu.setText("<html><font color='#505050'>Chênh lệch: </font><b><font color='#C82020'>+"
						+ DF.format(chenhLechVal) + " VNĐ</font></b></html>");
			} else {
				lblKhachBu.setText("<html><font color='#505050'>Chênh lệch: </font><b><font color='#1a7a30'>"
						+ DF.format(chenhLechVal) + " VNĐ</font></b></html>");
			}
		} else {
			lblKhachBu.setText("<html><font color='#505050'>Hoàn khách: </font><b><font color='#1a7a30'>"
					+ DF.format(Math.abs(khachBu)) + " VNĐ</font></b></html>");
		}

		lblThanhToanConLai.setText("<html><font color='#505050'>Tổng bù: </font><b><font color='#C82020'>"
				+ DF.format(khachBu) + " VNĐ</font></b></html>");

		if (btnChuyenKhoan != null && btnChuyenKhoan.isSelected())
			toggleQRCode();
		else
			tinhTienThua();
	}

	private void tinhTienThua() {
		if (txtTienKhachDua == null || lblTienThua == null)
			return;
		if (s_tongThu <= 0) {
			lblTienThua.setText(DF.format(Math.abs(s_tongThu)) + " VNĐ");
			lblTienThua.setForeground(new Color(30, 120, 60));
			return;
		}
		double tk = parseMoney(txtTienKhachDua.getText());
		double thua = tk - s_tongThu;
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

	// ════════════════════════════════════════════════════════════════════
	// POPUPS
	// ════════════════════════════════════════════════════════════════════
	private void showHuyPopup() {
		Window ancestor = SwingUtilities.getWindowAncestor(this);
		JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setUndecorated(true);
		JPanel glass = buildGlassPanel(0.45f);
		JPanel box = buildPopupBox(380, 235);
		box.setBorder(new EmptyBorder(28, 32, 24, 32));

		JLabel lblMsg = new JLabel("<html><div style='text-align:center;'>"
				+ "<b style='font-size:14px;color:#1c396e;'>Xác nhận hủy quá trình đổi vé?</b>"
				+ "<br><br><span style='font-size:13px;color:#555;'>Bạn sẽ được đưa về trang quản lý đổi/trả.</span>"
				+ "</div></html>", SwingConstants.CENTER);

		JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
		btnRow.setOpaque(false);
		JButton btnNo = makeOutlineBtn("Không, quay lại", null);
		btnNo.addActionListener(ev -> dialog.dispose());
		JButton btnYes = makeRedBtn("Xác nhận", null);
		btnYes.addActionListener(ev -> {
			dialog.dispose();
			stopAllTimers();
			maVeMoiHienThi = "";
			appFrame.showCard("doi-tra");
		});
		btnRow.add(btnNo);
		btnRow.add(btnYes);
		box.add(lblMsg, BorderLayout.CENTER);
		box.add(btnRow, BorderLayout.SOUTH);
		glass.add(box);
		setupAndShowDialog(dialog, glass, ancestor);
	}

	private void showXacNhanThanhToanPopup() {
		Window ancestor = SwingUtilities.getWindowAncestor(this);
		JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setUndecorated(true);
		JPanel glass = buildGlassPanel(0.45f);

		double tienKhach = parseMoney(txtTienKhachDua.getText());
		String tienKhachStr = tienKhach > 0 ? DF.format(tienKhach) + " VNĐ" : "(chưa nhập)";
		double tienThua = tienKhach > 0 ? tienKhach - s_tongThu : 0;

		JPanel box = buildPopupBox(400, tienKhach > 0 ? 240 : 220);
		box.setBorder(new EmptyBorder(28, 32, 24, 32));

		String thuaHtml = tienKhach > 0
				? "<br><span style='font-size:14px;color:#555;'>Tiền thừa: <b style='color:#1a7a30;'>"
						+ DF.format(Math.max(0, tienThua)) + " VNĐ</b></span>"
				: "";
		String thieu = tienKhach > 0 && tienThua < 0
				? "<br><span style='font-size:14px;color:#cc0000;'>⚠ Tiền khách đưa chưa đủ!</span>"
				: "";

		JLabel lblMsg = new JLabel("<html><div style='text-align:center;'>"
				+ "<b style='font-size:15px;color:#1c396e;'>Xác nhận thanh toán tiền mặt</b>"
				+ "<br><br><span style='font-size:14px;color:#555;'>Cần thanh toán: <b style='color:#c82020;'>"
				+ DF.format(s_tongThu) + " VNĐ</b></span>"
				+ "<br><span style='font-size:14px;color:#555;'>Khách đưa: <b>" + tienKhachStr + "</b></span>"
				+ thuaHtml + thieu + "</div></html>", SwingConstants.CENTER);

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
					int x1 = cx - 18, y1 = cy, xM = cx - 6, yM = cy + 14, x2 = cx + 20, y2 = cy - 16;
					float p1 = Math.min(1f, progress / 0.5f);
					g2.drawLine(x1, y1, (int) (x1 + (xM - x1) * p1), (int) (y1 + (yM - y1) * p1));
					if (progress > 0.5f) {
						float p2 = (progress - 0.5f) / 0.5f;
						g2.drawLine(xM, yM, (int) (xM + (x2 - xM) * p2), (int) (yM + (y2 - yM) * p2));
					}
				}
				g2.dispose();
			}
		};
		box.setOpaque(false);
		box.setPreferredSize(new Dimension(320, 240));
		box.setBorder(new EmptyBorder(140, 24, 24, 24));
		JLabel lblMsg = new JLabel(
				"<html><div style='text-align:center;'><b style='font-size:15px;color:#1c396e;'>Đổi vé thành công!</b>"
						+ "<br><span style='font-size:13px;color:#888;'>Đang xuất hóa đơn điện tử...</span></div></html>",
				SwingConstants.CENTER);
		box.add(lblMsg, BorderLayout.CENTER);
		glass.add(box);
		dialog.setContentPane(glass);
		dialog.setSize(ancestor.getSize());
		dialog.setLocation(ancestor.getLocation());
		javax.swing.Timer animTimer = new javax.swing.Timer(16, ev -> {
			frame[0]++;
			alpha[0] = Math.min(1f, frame[0] / 20f);
			glass.repaint();
			box.repaint();
			if (frame[0] >= TOTAL_FRAMES)
				((javax.swing.Timer) ev.getSource()).stop();
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
		JLabel lblTitle = new JLabel("Mã thanh toán Đổi vé");
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
		java.awt.Image scaleImg = originalQRImageIcon.getImage().getScaledInstance(350, 350,
				java.awt.Image.SCALE_SMOOTH);
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

	// ════════════════════════════════════════════════════════════════════
	// QR / BANK CHECK / COUNTDOWN
	// ════════════════════════════════════════════════════════════════════
	private void toggleQRCode() {
		if (!btnChuyenKhoan.isSelected())
			return;
		if (s_tongThu <= 0) {
			lblQR.setIcon(null);
			lblQR.setText("Hóa đơn 0đ");
			return;
		}
		lblQR.setIcon(null);
		lblQR.setText("Đang tạo mã VietQR...");
		new SwingWorker<ImageIcon, Void>() {
			@Override
			protected ImageIcon doInBackground() throws Exception {
				String info = URLEncoder.encode("Thanh toan doi ve " + maDon, StandardCharsets.UTF_8);
				String url = String.format(
						"https://img.vietqr.io/image/970422-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
						ACCOUNT_NO, s_tongThu, info, URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8));
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

	private void scaleAndSetQR() {
		if (originalQRImageIcon == null)
			return;
		int size = pnlQR.getWidth() > 40 ? pnlQR.getWidth() - 20 : 160;
		size = Math.min(size, pnlQR.getHeight() > 40 ? pnlQR.getHeight() - 20 : size);
		lblQR.setIcon(new ImageIcon(
				originalQRImageIcon.getImage().getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH)));
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

	private void stopBankChecking() {
		if (bankCheckTimer != null)
			bankCheckTimer.stop();
	}

	private boolean checkBankReceived() {
		try {
			URL url = new URL("https://oauth.casso.vn/v2/transactions?page=1&pageSize=20&sort=DESC");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "apikey " + CASSO_API_KEY);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			if (conn.getResponseCode() != 200)
				return false;
			BufferedReader br = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line);
			br.close();
			String json = sb.toString().toLowerCase();
			return json.contains(maDon.replace("-", "").toLowerCase()) && json.contains(String.valueOf(s_tongThu));
		} catch (Exception e) {
			return false;
		}
	}

	private void startCountdown() {
		if (countdownTimer != null)
			countdownTimer.stop();
		countdownTimer = new javax.swing.Timer(1000, e -> {
			secondsLeft--;
			if (secondsLeft <= 0) {
				stopAllTimers();
				lblCountdown.setText("Hết thời gian!");
				JOptionPane.showMessageDialog(this, "Hết thời gian giữ vé!", "Hết thời gian",
						JOptionPane.WARNING_MESSAGE);
				appFrame.showCard("doi-tra");
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

	// ════════════════════════════════════════════════════════════════════
	// XỬ LÝ HOÀN TẤT THANH TOÁN & LƯU DB
	// ════════════════════════════════════════════════════════════════════
	private void xuLyHoanTatThanhToan(String hinhThuc) {
		stopAllTimers();
		String maVeMoi = maVeMoiHienThi;
		String nv = nvOrNA(AuthService.getCurrentMaNV());

		String sqlInsertHD = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) "
				+ "SELECT ?, GETDATE(), ?, hd.maKH, ?, ?, ? "
				+ "FROM HoaDon hd JOIN Ve v ON v.maHoaDon = hd.maHoaDon WHERE v.maVe = ?";
		String sqlFindGhe = "SELECT TOP 1 maGhe FROM Ghe WHERE maToaTau = ? AND soGhe = ?";
		String sqlUpdateVeCu = "UPDATE Ve SET trangThaiVe = N'Đã đổi' WHERE maVe = ?";
		String sqlInsertDon = "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, loaiDon, maVe) VALUES (?, ?, GETDATE(), 'DON_DOI', ?)";
		String sqlInsertVeMoi = "INSERT INTO Ve (maVe, ngayMua, loaiVe, trangThaiVe, giaVe, maGhe, maHoaDon, maChuyenTau, maKH) "
				+ "SELECT ?, GETDATE(), loaiVe, N'Đã thanh toán', ?, ?, ?, ?, maKH FROM Ve WHERE maVe = ?";

		try (Connection conn = Connect_DB.getInstance().getConnection()) {
			conn.setAutoCommit(false);
			try {
				long giaVeCuForHD = 0;
				try {
					giaVeCuForHD = Long.parseLong(safeData(8).replaceAll("[^0-9]", ""));
				} catch (Exception ignored) {
				}
				long tongTienHD = s_tongThu;

				try (PreparedStatement ps = conn.prepareStatement(sqlInsertHD)) {
					ps.setString(1, maDon);
					ps.setString(2, nv);
					ps.setLong(3, tongTienHD);
					ps.setLong(4,
							hinhThuc.equals("Tiền mặt") ? (long) parseMoney(txtTienKhachDua.getText()) : s_tongThu);
					ps.setString(5, hinhThuc.equals("Tiền mặt") ? "TIEN_MAT" : "CHUYEN_KHOAN");
					ps.setString(6, s_maVe);
					if (ps.executeUpdate() == 0)
						throw new Exception("Không tìm thấy dữ liệu hóa đơn gốc!");
				}

				String maGheThuc = s_gheMoiDB;
				try {
					int tIdx = s_gheMoiDB.indexOf('T');
					if (tIdx > 1) {
						String soGheNum = String.valueOf(Integer.parseInt(s_gheMoiDB.substring(1, tIdx)));
						String maToaPart = s_gheMoiDB.substring(tIdx);
						try (PreparedStatement psFG = conn.prepareStatement(sqlFindGhe)) {
							psFG.setString(1, maToaPart);
							psFG.setString(2, soGheNum);
							try (ResultSet rs = psFG.executeQuery()) {
								if (rs.next())
									maGheThuc = rs.getString("maGhe");
								else
									throw new Exception("Không tìm thấy ghế mới trong DB!");
							}
						}
					}
				} catch (NumberFormatException ignored) {
				}

				try (PreparedStatement ps = conn.prepareStatement(sqlUpdateVeCu)) {
					ps.setString(1, s_maVe);
					ps.executeUpdate();
				}
				try (PreparedStatement ps = conn.prepareStatement(sqlInsertDon)) {
					ps.setString(1, maDon);
					ps.setLong(2, s_tongThu);
					ps.setString(3, s_maVe);
					ps.executeUpdate();
				}
				try (PreparedStatement ps = conn.prepareStatement(sqlInsertVeMoi)) {
					ps.setString(1, maVeMoi);
					ps.setLong(2, s_giaVeMoi);
					ps.setString(3, maGheThuc);
					ps.setString(4, maDon);
					ps.setString(5, s_chuyenMoi);
					ps.setString(6, s_maVe);
					ps.executeUpdate();
				}
				conn.commit();

				showThanhCongPopup(() -> {
					taoHoaDonPDF(maDon, hinhThuc, maVeMoi);
					taoVePDF(maVeMoi);
					appFrame.showCard("doi-tra");
				});
			} catch (Exception ex) {
				conn.rollback();
				throw ex;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	// ════════════════════════════════════════════════════════════════════
	// XUẤT PDF (giữ nguyên logic cũ)
	// ════════════════════════════════════════════════════════════════════
	private void taoHoaDonPDF(String maDonLuu, String hinhThuc, String maVeMoi) {
		try {
			File folder = new File("HoaDon");
			if (!folder.exists())
				folder.mkdir();
			File pdfFile = new File(folder, maDonLuu + ".pdf");
			Document document = new Document(PageSize.A4);
			PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
			document.open();
			BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font fontTitle = new Font(bf, 16, Font.BOLD);
			Font fontBold = new Font(bf, 11, Font.BOLD);
			Font fontNormal = new Font(bf, 11, Font.NORMAL);
			Font fontItalic = new Font(bf, 11, Font.ITALIC);
			Font fontVAT = new Font(bf, 13, Font.ITALIC, BaseColor.DARK_GRAY);

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
			document.add(new Paragraph("Nhân viên: " + nvOrNA(AuthService.getCurrentHoTen()) + "   Mã NV: "
					+ nvOrNA(AuthService.getCurrentMaNV()), fontNormal));
			document.add(new Paragraph(" ", fontNormal));
			document.add(new Paragraph("Họ tên người mua: " + tenKH, fontBold));
			document.add(new Paragraph("Điện thoại: " + sdtKH, fontNormal));
			document.add(new Paragraph("Hình thức TT: " + hinhThuc + "   Mã HĐ: " + maDonLuu, fontNormal));
			document.add(new Paragraph("Ghi chú: "
					+ (ghiChu.isEmpty() ? "....................................................................."
							: ghiChu),
					fontNormal));
			document.add(new Paragraph(" ", fontNormal));

			long giaVeCu = 0;
			try {
				giaVeCu = Long.parseLong(safeData(8).replaceAll("[^0-9]", ""));
			} catch (Exception ignored) {
			}
			long phiDoi = 30_000L;
			long giaVeMoiThuc = s_giaVeMoiThuc > 0 ? s_giaVeMoiThuc : s_giaVeMoi - phiDoi;
			String loaiCho = queryLoaiGheTheoMaToa(s_gheMoiDB);
			if (loaiCho.isEmpty()) loaiCho = !s_loaiCho.isEmpty() ? s_loaiCho : safeData(3);
			String chieuVe = !s_chieuVe.isEmpty() ? s_chieuVe : safeData(4);

			PdfPTable table = new PdfPTable(10);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 0.6f, 3f, 1.8f, 1.8f, 1.8f, 1.8f, 1.8f, 0.8f, 1.6f, 2f });
			for (String h : new String[] { "STT", "Tên dịch vụ", "Mã vé mới", "Ga đi", "Ga đến", "Loại chỗ", "Đơn giá", "SL", "Phí đổi", "Tổng bù" }) {
				PdfPCell cell = new PdfPCell(new Phrase(h, fontBold));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setPaddingBottom(6);
				cell.setBackgroundColor(new BaseColor(245, 245, 245));
				table.addCell(cell);
			}
			pdfAddCell(table, fontNormal, "1",                          Element.ALIGN_CENTER); // STT
			pdfAddCell(table, fontNormal, "Đổi vé HK trực tiếp tại ga",Element.ALIGN_LEFT);   // Tên dịch vụ
			pdfAddCell(table, fontNormal, maVeMoi,                      Element.ALIGN_CENTER); // Mã vé mới
			pdfAddCell(table, fontNormal, !s_gaDi.isEmpty()  ? s_gaDi  : "—", Element.ALIGN_CENTER); // Ga đi
			pdfAddCell(table, fontNormal, !s_gaDen.isEmpty() ? s_gaDen : "—", Element.ALIGN_CENTER); // Ga đến
			pdfAddCell(table, fontNormal, loaiCho,                      Element.ALIGN_CENTER); // Loại chỗ
			pdfAddCell(table, fontNormal, DF.format(giaVeMoiThuc),      Element.ALIGN_RIGHT);  // Đơn giá
			pdfAddCell(table, fontNormal, "1",                          Element.ALIGN_CENTER); // SL
			pdfAddCell(table, fontNormal, DF.format(phiDoi),            Element.ALIGN_RIGHT);  // Phí đổi
			pdfAddCell(table, fontNormal, DF.format(s_tongThu),         Element.ALIGN_RIGHT);  // Tổng bù
			document.add(table);
			document.add(new Paragraph(" ", fontNormal));

			Paragraph p1 = new Paragraph("Giá vé cũ: " + DF.format(giaVeCu) + " VNĐ", fontBold);
			p1.setAlignment(Element.ALIGN_RIGHT);
			document.add(p1);
			Paragraph p2 = new Paragraph("Giá vé mới: " + DF.format(giaVeMoiThuc) + " VNĐ", fontBold);
			p2.setAlignment(Element.ALIGN_RIGHT);
			document.add(p2);
			Paragraph p3 = new Paragraph("Phí đổi vé: " + DF.format(phiDoi) + " VNĐ", fontBold);
			p3.setAlignment(Element.ALIGN_RIGHT);
			document.add(p3);
			Paragraph p4 = new Paragraph("Tổng tiền bù: " + DF.format(s_tongThu) + " VNĐ", fontBold);
			p4.setAlignment(Element.ALIGN_RIGHT);
			document.add(p4);
			Paragraph pVAT = new Paragraph("(Hóa đơn đã bao gồm thuế VAT)", fontVAT);
			pVAT.setAlignment(Element.ALIGN_CENTER);
			pVAT.setSpacingBefore(6);
			pVAT.setSpacingAfter(4);
			document.add(pVAT);
			document.add(new Paragraph(" ", fontNormal));
			Phrase phraseTienChu = new Phrase();
			phraseTienChu.add(new Chunk("Số tiền viết bằng chữ: ", fontNormal));
			phraseTienChu.add(new Chunk(docTien(s_tongThu), fontItalic));
			document.add(new Paragraph(phraseTienChu));
			document.add(new Paragraph(" ", fontNormal));
			document.add(new Paragraph(" ", fontNormal));
			PdfPTable signTable = new PdfPTable(2);
			signTable.setWidthPercentage(100);
			PdfPCell cB = new PdfPCell(new Phrase("Người mua hàng\n(Ký, ghi rõ họ tên)", fontNormal));
			cB.setHorizontalAlignment(Element.ALIGN_CENTER);
			cB.setBorder(PdfPCell.NO_BORDER);
			PdfPCell cS = new PdfPCell(new Phrase("Người bán hàng\n(Ký, ghi rõ họ tên)", fontNormal));
			cS.setHorizontalAlignment(Element.ALIGN_CENTER);
			cS.setBorder(PdfPCell.NO_BORDER);
			signTable.addCell(cB);
			signTable.addCell(cS);
			document.add(signTable);
			document.close();
			if (Desktop.isDesktopSupported())
				Desktop.getDesktop().open(pdfFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void taoVePDF(String maVeLuu) {
		try {
			File folder = new File("Ve");
			if (!folder.exists())
				folder.mkdir();
			BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font fCongTy = new Font(bf, 10, Font.BOLD);
			Font fGaTen = new Font(bf, 11, Font.BOLD);
			Font fTieuDe = new Font(bf, 12, Font.BOLD);
			Font fSub = new Font(bf, 8, Font.NORMAL);
			Font fGaLabel = new Font(bf, 8, Font.NORMAL, BaseColor.GRAY);
			Font fGaValue = new Font(bf, 11, Font.BOLD);
			Font fLabel = new Font(bf, 9, Font.NORMAL);
			Font fValue = new Font(bf, 9, Font.BOLD);
			Font fMaVe = new Font(bf, 8, Font.NORMAL, BaseColor.GRAY);

			String loaiVe = "", tenKHT = "", cccd = "", tenTau = "", ngayDi = "", gioDi = "", gaDi = "", gaDen = "",
					soGhe = "", soToa = "", loaiGheRaw = "";
			try (Connection conn = Connect_DB.getInstance().getConnection()) {
				String sql = "SELECT v.loaiVe, k.hoTenKH, k.cccd, t.tenTau, ct.thoiGianKhoiHanh, "
						+ "gaDi.tenGa AS gaDi, gaDen.tenGa AS gaDen, g.soGhe, tt.soToa, g.loaiGhe "
						+ "FROM Ve v LEFT JOIN KhachHang k ON v.maKH=k.maKH "
						+ "JOIN ChiTietChuyenTau ct ON v.maChuyenTau=ct.maChuyenTau "
						+ "JOIN ChuyenTau c ON ct.maChuyenTau=c.maChuyenTau " + "JOIN Tau t ON c.maTau=t.maTau "
						+ "JOIN Ga gaDi ON ct.maGaDi=gaDi.maGa " + "JOIN Ga gaDen ON ct.maGaDen=gaDen.maGa "
						+ "JOIN Ghe g ON v.maGhe=g.maGhe " + "JOIN ToaTau tt ON g.maToaTau=tt.maToaTau WHERE v.maVe=?";
				try (PreparedStatement ps = conn.prepareStatement(sql)) {
					ps.setString(1, maVeLuu);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							loaiVe = nvOrNA(rs.getString("loaiVe"));
							tenKHT = rs.getString("hoTenKH") != null ? rs.getString("hoTenKH") : "Khách vãng lai";
							cccd = cheCCCD(rs.getString("cccd") != null ? rs.getString("cccd") : "");
							tenTau = nvOrNA(rs.getString("tenTau"));
							gaDi = nvOrNA(rs.getString("gaDi"));
							gaDen = nvOrNA(rs.getString("gaDen"));
							soGhe = nvOrNA(rs.getString("soGhe"));
							soToa = String.valueOf(rs.getInt("soToa"));
							loaiGheRaw = nvOrNA(rs.getString("loaiGhe"));
							java.sql.Timestamp ts = rs.getTimestamp("thoiGianKhoiHanh");
							if (ts != null) {
								ngayDi = new SimpleDateFormat("dd/MM/yyyy").format(ts);
								gioDi = new SimpleDateFormat("HH:mm").format(ts);
							}
						}
					}
				}
			}
			String loaiVeHT = "MOT_CHIEU".equals(loaiVe) ? "Một chiều" : "KHU_HOI".equals(loaiVe) ? "Khứ hồi" : loaiVe;
			String loaiGheHT = switch (loaiGheRaw.trim()) {
			case "GHE_CUNG" -> "Ghế cứng";
			case "GHE_MEM" -> "Ghế mềm";
			case "GIUONG_NAM" -> "Giường nằm";
			default -> loaiGheRaw;
			};
			String hangVe = "Giường nằm".equals(loaiGheHT) ? "VIP" : "Thường";

			File pdfFile = new File(folder, "Ve_" + maVeLuu + ".pdf");
			Rectangle pageSize = new Rectangle(240, 600);
			Document doc = new Document(pageSize, 10, 10, 12, 12);
			PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));
			doc.open();

			PdfPTable wrap = new PdfPTable(1);
			wrap.setWidthPercentage(100);
			PdfPCell wc = new PdfPCell();
			wc.setBorder(Rectangle.BOX);
			wc.setBorderWidth(0.8f);
			wc.setPaddingLeft(10);
			wc.setPaddingRight(10);
			wc.setPaddingTop(10);
			wc.setPaddingBottom(10);

			Paragraph pCty = new Paragraph("TỔNG CÔNG TY ĐƯỜNG SẮT VIỆT NAM", fCongTy);
			pCty.setAlignment(Element.ALIGN_CENTER);
			wc.addElement(pCty);
			Paragraph pGaTen = new Paragraph("GA DIÊU TRÌ", fGaTen);
			pGaTen.setAlignment(Element.ALIGN_CENTER);
			wc.addElement(pGaTen);

			PdfPTable lineT = new PdfPTable(1);
			lineT.setWidthPercentage(100);
			lineT.setSpacingBefore(4);
			lineT.setSpacingAfter(4);
			PdfPCell lC = new PdfPCell(new Phrase(""));
			lC.setBorder(Rectangle.BOTTOM);
			lC.setBorderWidth(0.5f);
			lC.setBorderColor(BaseColor.GRAY);
			lC.setPaddingBottom(0);
			lineT.addCell(lC);
			wc.addElement(lineT);

			Paragraph pTD = new Paragraph("VÉ LÊN TÀU HỎA", fTieuDe);
			pTD.setAlignment(Element.ALIGN_CENTER);
			wc.addElement(pTD);
			Paragraph pBoard = new Paragraph("BOARDING TICKET (ĐỔI VÉ)", fSub);
			pBoard.setAlignment(Element.ALIGN_CENTER);
			pBoard.setSpacingAfter(5);
			wc.addElement(pBoard);

			com.itextpdf.text.pdf.Barcode128 bc = new com.itextpdf.text.pdf.Barcode128();
			bc.setCode(maVeLuu);
			bc.setBarHeight(32f);
			bc.setX(1.0f);
			bc.setBaseline(0f);
			bc.setAltText("");
			java.awt.Image awtImg = bc.createAwtImage(java.awt.Color.BLACK, java.awt.Color.WHITE);
			com.itextpdf.text.Image imgBar = com.itextpdf.text.Image.getInstance(awtImg, null);
			imgBar.setAlignment(Element.ALIGN_CENTER);
			imgBar.scaleToFit(240f, 38f);
			wc.addElement(imgBar);

			Paragraph pMV = new Paragraph("Mã vé/TicketID: " + maVeLuu, fMaVe);
			pMV.setAlignment(Element.ALIGN_CENTER);
			pMV.setSpacingAfter(6);
			wc.addElement(pMV);

			PdfPTable gaT = new PdfPTable(2);
			gaT.setWidthPercentage(100);
			gaT.setSpacingBefore(2);
			gaT.setSpacingAfter(2);
			PdfPCell cDi = new PdfPCell();
			cDi.setBorder(Rectangle.NO_BORDER);
			cDi.setPaddingLeft(16);
			cDi.setPaddingBottom(2);
			cDi.addElement(new Paragraph("Ga đi", fGaLabel) {
				{
					setAlignment(Element.ALIGN_LEFT);
				}
			});
			cDi.addElement(new Paragraph(gaDi.toUpperCase(), fGaValue) {
				{
					setAlignment(Element.ALIGN_LEFT);
				}
			});
			gaT.addCell(cDi);
			PdfPCell cDen = new PdfPCell();
			cDen.setBorder(Rectangle.NO_BORDER);
			cDen.setPaddingRight(16);
			cDen.setPaddingBottom(2);
			cDen.addElement(new Paragraph("Ga đến", fGaLabel) {
				{
					setAlignment(Element.ALIGN_RIGHT);
				}
			});
			cDen.addElement(new Paragraph(gaDen.toUpperCase(), fGaValue) {
				{
					setAlignment(Element.ALIGN_RIGHT);
				}
			});
			gaT.addCell(cDen);
			PdfPCell cSep = new PdfPCell(new Phrase(""));
			cSep.setColspan(2);
			cSep.setBorder(Rectangle.BOTTOM);
			cSep.setBorderWidth(0.5f);
			cSep.setBorderColor(BaseColor.LIGHT_GRAY);
			cSep.setPaddingBottom(3);
			gaT.addCell(cSep);
			wc.addElement(gaT);

			PdfPTable infoT = new PdfPTable(2);
			infoT.setWidthPercentage(100);
			infoT.setWidths(new float[] { 1.35f, 1.65f });
			infoT.setSpacingBefore(3);
			String[][] det = { { "Số hiệu tàu/Train ID:", tenTau }, { "Ngày khởi hành/Date:", ngayDi },
					{ "Giờ khởi hành/Time:", gioDi }, { "Số Toa/Coach:", soToa }, { "Loại toa/Type:", loaiGheHT },
					{ "Số ghế/Seat:", soGhe }, { "Loại vé/Ticket:", loaiVeHT }, { "Hạng vé/Class:", hangVe },
					{ "Họ Tên/Name:", tenKHT.toUpperCase() }, { "Giấy tờ/Passport:", cccd },
					{ "Loại giao dịch:", "ĐỔI VÉ" } };
			for (String[] d : det) {
				PdfPCell cL = new PdfPCell(new Phrase(d[0], fLabel));
				cL.setBorder(Rectangle.NO_BORDER);
				cL.setPaddingLeft(14);
				cL.setPaddingBottom(3);
				infoT.addCell(cL);
				PdfPCell cR = new PdfPCell(new Phrase(d[1], fValue));
				cR.setBorder(Rectangle.NO_BORDER);
				cR.setPaddingLeft(30);
				cR.setPaddingBottom(3);
				infoT.addCell(cR);
			}
			wc.addElement(infoT);
			wrap.addCell(wc);
			doc.add(wrap);
			doc.close();
			if (Desktop.isDesktopSupported())
				Desktop.getDesktop().open(pdfFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void pdfAddCell(PdfPTable table, Font font, String text, int align) {
		PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
		cell.setHorizontalAlignment(align);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPaddingBottom(4);
		table.addCell(cell);
	}

	// ════════════════════════════════════════════════════════════════════
	// HELPERS
	// ════════════════════════════════════════════════════════════════════
	private JPanel createDetailRow(String title, JLabel valueLabel) {
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
		p.add(l1, BorderLayout.WEST);
		p.add(valueLabel, BorderLayout.EAST);
		return p;
	}

	private JPanel buildGlassPanel(float opacity) {
		return new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				g2.setColor(new Color(10, 20, 50));
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.dispose();
			}
		};
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
		try (Connection con = Connect_DB.getInstance().getConnection()) {
			return MaTuDong.taoMaDon(con, LocalDate.now());
		} catch (Exception e) {
			return "DT" + new SimpleDateFormat("MMyy").format(new Date()) + "-"
					+ String.format("%04d", System.currentTimeMillis() % 10000);
		}
	}

	private String cheCCCD(String cccd) {
		if (cccd != null && cccd.length() > 4) {
			int len = cccd.length();
			if (len == 12)
				return cccd.substring(0, 4) + "****" + cccd.substring(8);
			else if (len > 6) {
				int v = (len - 4) / 2;
				return cccd.substring(0, v) + "****" + cccd.substring(v + 4);
			}
		}
		return cccd;
	}

	private String docBaSo(int n, boolean hasPrefix) {
		String[] d = { "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };
		int t = n / 100, c = (n % 100) / 10, dd = n % 10;
		StringBuilder res = new StringBuilder();
		if (hasPrefix || t > 0) {
			res.append(d[t]).append(" trăm ");
			if (c == 0 && dd > 0)
				res.append("lẻ ");
		}
		if (c > 1) {
			res.append(d[c]).append(" mươi ");
			if (dd == 1)
				res.append("mốt");
			else if (dd == 5)
				res.append("lăm");
			else if (dd > 0)
				res.append(d[dd]);
		} else if (c == 1) {
			res.append("mười ");
			if (dd == 5)
				res.append("lăm");
			else if (dd > 0)
				res.append(d[dd]);
		} else if (dd > 0)
			res.append(d[dd]);
		return res.toString().trim();
	}

	private String docTien(long number) {
		if (number == 0)
			return "Không đồng";
		String[] units = { "", "nghìn", "triệu", "tỷ" };
		StringBuilder result = new StringBuilder();
		long temp = number;
		int ui = 0;
		while (temp > 0) {
			int g = (int) (temp % 1000);
			temp /= 1000;
			if (g > 0) {
				result.insert(0, docBaSo(g, temp > 0) + " " + units[ui] + " ");
			}
			ui++;
		}
		String s = result.toString().replaceAll("\\s+", " ").trim();
		return s.substring(0, 1).toUpperCase() + s.substring(1) + " đồng";
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