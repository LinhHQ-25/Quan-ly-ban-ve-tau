package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dao.ChiTietChuyenTauDAO;
import dao.DonDoiTraVeDAO;
import dao.GaDAO;
import dao.ToaTauDAO;
import entity.ChiTietChuyenTau;
import entity.Ga;
import entity.ToaTau;

public class DoiVeGUI1 extends JPanel {
	private static final Color BORDER = new Color(210, 215, 224);
	private static final Color NAVY   = GuiTheme.NAVY;
	private static final Color NEW_FG = GuiTheme.NAVY;
	private static final Font  FONT_B14 = new Font("Segoe UI", Font.BOLD, 13);
	private long giaVeMoiSauGiam = 0;
	// --- DỮ LIỆU TĨNH ---
	private static String   s_maVe       = "";
	private static String[] s_dataCu     = new String[0];
	private static String   s_maChuyenMoi = "", s_ngayDiMoi = "", s_gheDiMoi = "", s_maToaMoi = "";
	private static String   s_chuyenVeMoi = "—", s_ngayVeMoi = "—", s_gheVeMoi = "—";
	private static String s_maVeMoi = "";
	// Thêm vào sau s_chieuVe
	private static String s_gaDi = "";
	private static String s_gaDen = "";
	public static void setDonDoiKhuHoi(String maVe, String[] dataCu,
			String maChuyenMoi, String ngayDi, String gheDi, String maToaMoi,
			String chuyenVe, String ngayVe) {
		s_maVe        = maVe;
		s_dataCu      = dataCu.clone();
		s_maChuyenMoi = maChuyenMoi;
		s_ngayDiMoi   = ngayDi;
		s_gheDiMoi    = gheDi;
		s_maToaMoi    = maToaMoi;
		s_chuyenVeMoi = chuyenVe;
		s_ngayVeMoi   = ngayVe;
		s_gheVeMoi    = "—";
	}

	private final AppFrame appFrame;
	private JPanel      pnlTabController, pnlCardContainer;
	private CardLayout  cardLayout;
	private final boolean[] activeDi = {true}, activeVe = {false};

	private JLabel valMaVeDi, valChuyenDi, valToaDi, valGheDi, valNgayDi, valGaDi_Di, valGaDenDi;
	private JLabel valMaVeVe, valChuyenVe, valToaVe, valGheVe, valNgayVe, valGaDi_Ve, valGaDenVe;
	private JLabel oldMaVeDi, oldChuyenDi, oldToaDi, oldGheDi, oldNgayDi, oldGaDi_Di_old, oldGaDenDi_old;
	private JLabel oldMaVeVe, oldChuyenVe, oldToaVe, oldGheVe, oldNgayVe, oldGaDi_Ve_old, oldGaDenVe_old;
	private JLabel lbChenhLech, lbTongThu, lbGiaVeCu, lbGiaVeMoi, lbPhuPhi;
	private long tongLePhi = 0, giaVeMoi = 0;

	public DoiVeGUI1(AppFrame appFrame) {
		this.appFrame = appFrame;
		setLayout(new BorderLayout());
		setBackground(GuiTheme.LIGHT_BG);

		// ── Page wrapper: padding nhỏ sát khung ──────────────────────────────
		JPanel pnlPage = new JPanel(new BorderLayout(0, 0));
		pnlPage.setOpaque(false);
		// Giảm padding ngang từ PAGE_PAD_LEFT xuống còn 10px
		pnlPage.setBorder(new EmptyBorder(4, 10, 0, 10));

		JPanel stack = new JPanel();
		stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
		stack.setOpaque(false);

		stack.add(buildTabController());
		stack.add(Box.createVerticalStrut(4));
		stack.add(buildCompareCard());

		// Bỏ JScrollPane, dùng trực tiếp để không tạo khoảng thừa
		pnlPage.add(stack, BorderLayout.CENTER);
		pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);
		add(pnlPage, BorderLayout.CENTER);
	}

	public void refresh() {
		try (Connection con = connect_DB.Connect_DB.getInstance().getConnection()) {
	        s_maVeMoi = util.MaTuDong.taoMaVe(con);
	    } catch (Exception e) {
	        e.printStackTrace();
	        s_maVeMoi = "";
	    }
		boolean isKhuHoi = !s_chuyenVeMoi.equals("—") && !s_chuyenVeMoi.isEmpty();
		pnlTabController.setVisible(isKhuHoi);

		String oldGaDi  = safe(s_dataCu, 1);
		String oldGaDen = safe(s_dataCu, 2);

		String[] gasMoi  = queryGaFromChuyen(s_maChuyenMoi);
		String newGaDi   = gasMoi[0];
		String newGaDen  = gasMoi[1];

		oldMaVeDi.setText(s_maVe);
		oldChuyenDi.setText(safe(s_dataCu, 0));
		oldToaDi.setText(extractToaFromDB(safe(s_dataCu, 7)));
		oldGheDi.setText(extractGheFromDB(safe(s_dataCu, 7)));
		oldNgayDi.setText(safe(s_dataCu, 5));
		oldGaDi_Di_old.setText(oldGaDi);
		oldGaDenDi_old.setText(oldGaDen);

		valMaVeDi.setText(!s_maVeMoi.isEmpty() ? s_maVeMoi : "Chưa phát sinh");
		valChuyenDi.setText(s_maChuyenMoi);
		valToaDi.setText(s_maToaMoi);
		valGheDi.setText(extractGheFromGheStr(s_gheDiMoi));
		valNgayDi.setText(s_ngayDiMoi);
		valGaDi_Di.setText(newGaDi);
		valGaDenDi.setText(newGaDen);

		if (isKhuHoi) {
			valMaVeVe.setText(s_maVe + "-VE");
			valChuyenVe.setText(s_chuyenVeMoi);
			valToaVe.setText("—");
			valGheVe.setText("—");
			valNgayVe.setText(s_ngayVeMoi);
			valGaDi_Ve.setText(oldGaDen);
			valGaDenVe.setText(oldGaDi);
		} else {
			activeDi[0] = true;
			activeVe[0] = false;
			cardLayout.show(pnlCardContainer, "DI");
		}

		calcPriceAndRefresh();
		revalidate();
		repaint();
	}

	private String[] queryGaFromChuyen(String maChuyenTau) {
		ChiTietChuyenTauDAO ctDao = new ChiTietChuyenTauDAO();
		GaDAO gaDao = new GaDAO();
		ChiTietChuyenTau ct = ctDao.selectById(maChuyenTau);
		if (ct != null) {
			Ga gaDi  = gaDao.selectById(ct.getGaDi().getMaGa());
			Ga gaDen = gaDao.selectById(ct.getGaDen().getMaGa());
			return new String[]{
				(gaDi  != null) ? gaDi.getTenGa()  : "—",
				(gaDen != null) ? gaDen.getTenGa() : "—"
			};
		}
		return new String[]{"—", "—"};
	}

	// ══════════════════════════════════════════════════════════════════════
//  TÍNH GIÁ VÉ — đồng nhất với công thức entity.Ve trong DatVeGUI3
// ══════════════════════════════════════════════════════════════════════

private void calcPriceAndRefresh() {
    final long phiDoiVe = 30_000L;

    // ── Giá vé CŨ: lấy thẳng từ DB (s_dataCu[8] = giaVe đã lưu) ──────
    long giaVeCu = 0;
    try { giaVeCu = Long.parseLong(safe(s_dataCu, 8).replaceAll("[^0-9]", "")); }
    catch (Exception ignored) {}

    // ── Giá vé MỚI: tính qua entity.Ve, đồng nhất DatVeGUI3 ──────────
    giaVeMoiSauGiam = tinhGiaVeMoi();

    // ── Chênh lệch & tổng thu ─────────────────────────────────────────
    long chenhLech = giaVeMoiSauGiam - giaVeCu;
    tongLePhi      = Math.max(chenhLech + phiDoiVe, phiDoiVe);
    giaVeMoi       = giaVeCu + tongLePhi;

    // ── Cập nhật nhãn ─────────────────────────────────────────────────
    lbGiaVeCu.setText(fmtTien(giaVeCu));
    lbGiaVeCu.setForeground(GuiTheme.TEXT);

    lbGiaVeMoi.setText(fmtTien(giaVeMoiSauGiam));
    lbGiaVeMoi.setForeground(GuiTheme.TEXT);

    String moTa = (chenhLech == 0 ? "Không đổi"
                 : chenhLech > 0  ? "Nâng hạng"
                                  : "Hạ hạng")
                + "  " + (chenhLech >= 0 ? "+" : "") + fmtTien(chenhLech);
    lbChenhLech.setText(moTa);
    lbChenhLech.setForeground(
        chenhLech > 0 ? new Color(180, 60, 0)
      : chenhLech < 0 ? new Color(30, 120, 60)
                      : GuiTheme.SUB_TEXT);

    lbPhuPhi.setText(fmtTien(phiDoiVe));
    lbPhuPhi.setForeground(GuiTheme.TEXT);

    lbTongThu.setText(fmtTien(tongLePhi));
    lbTongThu.setForeground(
        tongLePhi > phiDoiVe ? new Color(180, 60, 0) : GuiTheme.TEXT);
}

/**
 * Tính giá vé MỚI theo đúng công thức entity.Ve của DatVeGUI3:
 *   giaGoc = 300_000 * heSoCuLy * heSoLoaiToa
 *   thanhTien = giaGoc * (1 - tyLeGiamDoiTuong)
 *
 * heSoCuLy   → GaDAO.getHeSoCuLy(maGaDen của chuyến mới)
 * heSoLoaiToa → ToaTauDAO.selectById(s_maToaMoi).getHeSoLoaiToa()
 * loaiDoiTuong → lấy từ DB theo maVe cũ
 */
private long tinhGiaVeMoi() {
    // 1. heSoLoaiToa — từ toa mới
    double heSoLoaiToa = queryHeSoToa(s_maToaMoi);

    // 2. heSoCuLy — từ ga đến của chuyến mới
    double heSoCuLy = 1.0;
    String maGaDen = queryMaGaDenFromChuyen(s_maChuyenMoi);
    if (!maGaDen.isEmpty()) {
        double h = new dao.GaDAO().getHeSoCuLy(maGaDen);
        if (h > 0) heSoCuLy = h;
    }

    // 3. Tính giá gốc — KHÔNG áp khuyến mãi/giảm giá đối tượng
    entity.Ve ve = new entity.Ve();
    ve.setHeSoCuLy(heSoCuLy);
    ve.setHeSoLoaiToa(heSoLoaiToa);

    return Math.round(ve.tinhGiaGoc()); // ← chỉ lấy giá gốc, bỏ tyLeGiam
}

/**
 * Lấy maGa (mã DB) của ga đến từ mã chuyến tàu.
 */
private String queryMaGaDenFromChuyen(String maChuyenTau) {
    if (maChuyenTau == null || maChuyenTau.equals("—") || maChuyenTau.isEmpty())
        return "";
    ChiTietChuyenTau ct = new ChiTietChuyenTauDAO().selectById(maChuyenTau);
    if (ct != null && ct.getGaDen() != null)
        return ct.getGaDen().getMaGa();
    return "";
}
private String queryLoaiDoiTuongTheoMaVe(String maVe) {
    String sql = "SELECT kh.laSinhVien FROM Ve v "
               + "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon "
               + "JOIN KhachHang kh ON hd.maKH = kh.maKH "
               + "WHERE v.maVe = ?";
    try (Connection con = connect_DB.Connect_DB.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, maVe);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBoolean("laSinhVien") ? "Sinh viên" : "Người lớn";
            }
        }
    } catch (Exception e) { e.printStackTrace(); }
    return "Người lớn";
}
/**
 * Lấy heSoLoaiToa từ mã toa tàu.
 */
private double queryHeSoToa(String maToaTau) {
    if (maToaTau == null || maToaTau.isEmpty()) return 1.0;
    ToaTau toa = new ToaTauDAO().selectById(maToaTau);
    return (toa != null) ? toa.getHeSoLoaiToa() : 1.0;
}

	// ══════════════════════════════════════════════════════════════════════
	//  BUILD UI
	// ══════════════════════════════════════════════════════════════════════

	private JPanel buildCompareCard() {
		JPanel card = buildCard("SO SÁNH CHI TIẾT THÔNG TIN VÉ");

		cardLayout      = new CardLayout();
		pnlCardContainer = new JPanel(cardLayout);
		pnlCardContainer.setOpaque(false);

		// --- value labels (mới) ---
		valMaVeDi   = fieldLabel(GuiTheme.TEXT);   valMaVeVe   = fieldLabel(GuiTheme.TEXT);
		valChuyenDi = fieldLabel(NEW_FG);           valChuyenVe = fieldLabel(NEW_FG);
		valToaDi    = fieldLabel(NEW_FG);           valToaVe    = fieldLabel(NEW_FG);
		valGheDi    = fieldLabel(NEW_FG);           valGheVe    = fieldLabel(NEW_FG);
		valNgayDi   = fieldLabel(NEW_FG);           valNgayVe   = fieldLabel(NEW_FG);
		valGaDi_Di  = fieldLabel(GuiTheme.TEXT);    valGaDi_Ve  = fieldLabel(GuiTheme.TEXT);
		valGaDenDi  = fieldLabel(GuiTheme.TEXT);    valGaDenVe  = fieldLabel(GuiTheme.TEXT);

		// --- value labels (cũ) ---
		oldMaVeDi      = fieldLabel(GuiTheme.SUB_TEXT); oldMaVeVe      = fieldLabel(GuiTheme.SUB_TEXT);
		oldChuyenDi    = fieldLabel(GuiTheme.SUB_TEXT); oldChuyenVe    = fieldLabel(GuiTheme.SUB_TEXT);
		oldToaDi       = fieldLabel(GuiTheme.SUB_TEXT); oldToaVe       = fieldLabel(GuiTheme.SUB_TEXT);
		oldGheDi       = fieldLabel(GuiTheme.SUB_TEXT); oldGheVe       = fieldLabel(GuiTheme.SUB_TEXT);
		oldNgayDi      = fieldLabel(GuiTheme.SUB_TEXT); oldNgayVe      = fieldLabel(GuiTheme.SUB_TEXT);
		oldGaDi_Di_old = fieldLabel(GuiTheme.SUB_TEXT); oldGaDi_Ve_old = fieldLabel(GuiTheme.SUB_TEXT);
		oldGaDenDi_old = fieldLabel(GuiTheme.SUB_TEXT); oldGaDenVe_old = fieldLabel(GuiTheme.SUB_TEXT);

		JPanel gridDi = createCompareGrid(true,
				oldMaVeDi, oldChuyenDi, oldToaDi, oldGheDi, oldNgayDi, oldGaDi_Di_old, oldGaDenDi_old,
				valMaVeDi, valChuyenDi, valToaDi, valGheDi, valNgayDi, valGaDi_Di, valGaDenDi);
		JPanel gridVe = createCompareGrid(false,
				oldMaVeVe, oldChuyenVe, oldToaVe, oldGheVe, oldNgayVe, oldGaDi_Ve_old, oldGaDenVe_old,
				valMaVeVe, valChuyenVe, valToaVe, valGheVe, valNgayVe, valGaDi_Ve, valGaDenVe);

		pnlCardContainer.add(gridDi, "DI");
		pnlCardContainer.add(gridVe, "VE");

		// ── Footer giá vé: layout 2 cột, insets nhỏ ─────────────────────────
		JPanel pnlFooter = new JPanel(new GridBagLayout());
		pnlFooter.setOpaque(false);
		pnlFooter.setBorder(new EmptyBorder(8, 0, 0, 0));

		GridBagConstraints f = new GridBagConstraints();
		f.fill   = GridBagConstraints.HORIZONTAL;
		// insets nhỏ hơn để footer gọn
		f.insets = new Insets(2, 6, 2, 6);

		lbGiaVeCu   = new JLabel("—");
		lbGiaVeMoi  = new JLabel("—");
		lbChenhLech = new JLabel("—");
		lbPhuPhi    = new JLabel("30.000 đ");
		lbTongThu   = new JLabel("—");

		Font fLabel = GuiTheme.font("Segoe UI", Font.PLAIN,  12);
		Font fValue = GuiTheme.font("Segoe UI", Font.BOLD,   12);
		Font fTotal = GuiTheme.font("Segoe UI", Font.BOLD,   14);

		String[] rowLabels = {
			"Giá vé hiện tại:", "Giá vé mới:", "Chênh lệch:",
			"Phụ phí đổi vé:", "Tổng tiền thanh toán:"
		};
		JLabel[] rowValues = { lbGiaVeCu, lbGiaVeMoi, lbChenhLech, lbPhuPhi, lbTongThu };

		for (int i = 0; i < rowLabels.length; i++) {
			JLabel lblName = new JLabel(rowLabels[i]);
			lblName.setFont(fLabel);
			lblName.setForeground(GuiTheme.SUB_TEXT);

			boolean isTotal = (i == 4);
			rowValues[i].setFont(isTotal ? fTotal : fValue);
			rowValues[i].setForeground(GuiTheme.TEXT);
			rowValues[i].setHorizontalAlignment(SwingConstants.RIGHT);
			rowValues[i].setOpaque(true);
			rowValues[i].setBackground(i % 2 == 0 ? new Color(245,248,252) : Color.WHITE);
			// Padding bên trong value cell nhỏ hơn
			rowValues[i].setBorder(BorderFactory.createCompoundBorder(
					new LineBorder(new Color(210,215,224), 1, false),
					new EmptyBorder(3, 10, 3, 10)));

			if (isTotal) {
				f.gridx = 0; f.gridy = i * 2 - 1; f.gridwidth = 2; f.weightx = 1.0;
				JSeparator sep = new JSeparator();
				sep.setForeground(new Color(180,205,230));
				pnlFooter.add(sep, f);
				f.gridwidth = 1;
			}

			f.gridx = 0; f.gridy = i * 2; f.weightx = 0.35;
			pnlFooter.add(lblName, f);
			f.gridx = 1; f.weightx = 0.65;
			pnlFooter.add(rowValues[i], f);
		}

		card.add(pnlCardContainer, BorderLayout.CENTER);
		card.add(pnlFooter, BorderLayout.SOUTH);
		return card;
	}

	private JPanel createCompareGrid(boolean isDi,
			JLabel oldMaVe, JLabel oldChuyen, JLabel oldToa, JLabel oldGhe, JLabel oldNgay,
			JLabel oldGa1, JLabel oldGa2,
			JLabel vMa, JLabel vCh, JLabel vTo, JLabel vGh, JLabel vNgay,
			JLabel vGaDi, JLabel vGaDen) {

		JPanel grid = new JPanel(new GridBagLayout());
		grid.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill   = GridBagConstraints.HORIZONTAL;
		// Insets nhỏ để grid không chiếm quá nhiều chiều cao
		gbc.insets = new Insets(3, 8, 3, 8);
		gbc.gridy  = 0;
		gbc.gridx  = 1; grid.add(headerLabel("THÔNG TIN VÉ HIỆN TẠI (CŨ)", GuiTheme.SUB_TEXT), gbc);
		gbc.gridx  = 3; grid.add(headerLabel("THÔNG TIN VÉ ĐỔI (MỚI)", NEW_FG), gbc);

		addGridRow(grid, 1, "Mã vé",                   oldMaVe,   vMa);
		addGridRow(grid, 2, "Mã chuyến",               oldChuyen, vCh);
		addGridRow(grid, 3, "Toa",                     oldToa,    vTo);
		addGridRow(grid, 4, "Ghế",                     oldGhe,    vGh);
		addGridRow(grid, 5, isDi ? "Ngày đi" : "Ngày về", oldNgay, vNgay);
		addGridRow(grid, 6, "Ga đi",                   oldGa1,    vGaDi);
		addGridRow(grid, 7, "Ga đến",                  oldGa2,    vGaDen);
		return grid;
	}

	private void addGridRow(JPanel grid, int y, String title, JLabel oldV, JLabel newV) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy  = y;
		// Insets nhỏ hơn (3 thay vì 5)
		gbc.insets = new Insets(3, 5, 3, 5);
		gbc.fill   = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0; gbc.weightx = 0.15;
		grid.add(new JLabel(title) {{
			setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
			setForeground(GuiTheme.SUB_TEXT);
		}}, gbc);

		gbc.gridx = 1; gbc.weightx = 0.4;
		grid.add(oldV, gbc);

		gbc.gridx = 2; gbc.weightx = 0.05;
		grid.add(new JLabel("→", SwingConstants.CENTER) {{
			setFont(new Font("Segoe UI", Font.BOLD, 14));
			setForeground(BORDER);
		}}, gbc);

		gbc.gridx = 3; gbc.weightx = 0.4;
		grid.add(newV, gbc);
	}

	private JPanel buildTabController() {
		pnlTabController = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
		pnlTabController.setOpaque(false);
		pnlTabController.add(makeTabButton("Chiều đi", activeDi, () -> {
			activeDi[0] = true;  activeVe[0] = false;
			cardLayout.show(pnlCardContainer, "DI");
			pnlTabController.repaint();
		}));
		pnlTabController.add(makeTabButton("Chiều về", activeVe, () -> {
			activeDi[0] = false; activeVe[0] = true;
			cardLayout.show(pnlCardContainer, "VE");
			pnlTabController.repaint();
		}));
		return pnlTabController;
	}

	private JPanel buildButtonRow() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
		p.setOpaque(false);
		p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
		JButton btnBack    = makeSecondaryButton("Quay lại", 120, 36);
		JButton btnConfirm = makeNavyButton("Tiếp tục thanh toán", 170, 36);
		btnBack.addActionListener(e -> appFrame.showCard("doi-ve-step-1"));
		btnConfirm.addActionListener(e -> handleConfirm());
		p.add(btnBack);
		p.add(btnConfirm);
		return p;
	}

	private void handleConfirm() {
	    String maGheDbDi = getMaGheMoiDB(s_gheDiMoi.split(",")[0].trim());
	    String hienThiDi = s_maToaMoi + " - " + extractGheFromGheStr(s_gheDiMoi);

	    String[] gasMoi = queryGaFromChuyen(s_maChuyenMoi);
	    DoiVeGUI2.setGaDiGaDen(gasMoi[0], gasMoi[1]);
	    DoiVeGUI2.setMaVeMoi(s_maVeMoi); // dùng mã đã sinh từ refresh()
	    DoiVeGUI2.setGiaVeMoiThuc(giaVeMoiSauGiam);
	    DoiVeGUI2.setDuLieuThanhToan(s_maVe, s_dataCu, s_maChuyenMoi, s_ngayDiMoi,
	            maGheDbDi, hienThiDi, tongLePhi, giaVeMoi);
	    appFrame.showCard("doi-ve-step-3");
	}
	// ── Helpers ──────────────────────────────────────────────────────────

	private String getMaGheMoiDB(String fullStr) {
		if (fullStr == null || !fullStr.contains("-")) return fullStr;
		String[] parts = fullStr.split("-");
		return parts[1].trim() + parts[0].trim();
	}

	private String extractGheFromGheStr(String gheStr) {
		if (gheStr == null || gheStr.isEmpty() || !gheStr.contains("-")) return "—";
		try { return gheStr.split(",")[0].split("-")[1].trim(); }
		catch (Exception e) { return "—"; }
	}

	private String extractToaFromDB(String maGhe) {
		if (maGhe == null || maGhe.length() < 4) return "—";
		try { return maGhe.substring(3); } catch (Exception e) { return "—"; }
	}

	private String extractGheFromDB(String maGhe) {
		if (maGhe == null || maGhe.length() < 3) return "—";
		try { return maGhe.substring(0, 3); } catch (Exception e) { return "—"; }
	}

	private String fmtTien(long a) {
		return String.format("%,d đ", a).replace(",", ".");
	}

	private String safe(String[] a, int i) {
		return (a != null && i < a.length && a[i] != null) ? a[i] : "—";
	}

	// ── Factory helpers ───────────────────────────────────────────────────

	private JPanel buildCard(String t) {
		JPanel card = new JPanel(new BorderLayout(0, 8));
		card.setBackground(Color.WHITE);
		// Giảm padding trong card: 12 top/bottom, 16 left/right
		card.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(BORDER, 1, true),
				new EmptyBorder(12, 16, 12, 16)));
		JLabel lbTitle = new JLabel(t, SwingConstants.CENTER);
		lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
		card.add(lbTitle, BorderLayout.NORTH);
		return card;
	}

	private JLabel headerLabel(String t, Color c) {
		JLabel lb = new JLabel(t);
		lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
		lb.setForeground(c);
		lb.setHorizontalAlignment(SwingConstants.CENTER);
		return lb;
	}

	private JLabel fieldLabel(Color c) {
		JLabel lb = new JLabel("—");
		lb.setFont(FONT_B14);
		lb.setForeground(c);
		lb.setOpaque(true);
		lb.setBackground(GuiTheme.SEARCH_FIELD_BG);
		// Padding bên trong field label nhỏ hơn: 5 top/bottom (thay vì 8)
		lb.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(BORDER, 1, false),
				new EmptyBorder(5, 10, 5, 10)));
		return lb;
	}

	private JButton makeNavyButton(String t, int w, int h) {
		JButton btn = new JButton(t) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(Color.WHITE);
				g2.setFont(FONT_B14);
				FontMetrics fm = g2.getFontMetrics();
				g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
						(getHeight()+fm.getAscent()-fm.getDescent())/2);
				g2.dispose();
			}
		};
		btn.setPreferredSize(new Dimension(w, h));
		btn.setContentAreaFilled(false); btn.setBorderPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return btn;
	}

	private JButton makeSecondaryButton(String t, int w, int h) {
		JButton btn = new JButton(t) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(new Color(240,243,248));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(BORDER);
				g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
				g2.setColor(GuiTheme.TEXT);
				g2.setFont(FONT_B14);
				FontMetrics fm = g2.getFontMetrics();
				g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
						(getHeight()+fm.getAscent()-fm.getDescent())/2);
				g2.dispose();
			}
		};
		btn.setPreferredSize(new Dimension(w, h));
		btn.setContentAreaFilled(false); btn.setBorderPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return btn;
	}

	private JButton makeTabButton(String t, boolean[] s, Runnable r) {
		JButton btn = new JButton(t) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(s[0] ? NAVY : Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(s[0] ? Color.WHITE : NAVY);
				g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
				g2.setFont(FONT_B14);
				FontMetrics fm = g2.getFontMetrics();
				g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
						(getHeight()+fm.getAscent()-fm.getDescent())/2);
				g2.dispose();
			}
		};
		btn.setPreferredSize(new Dimension(100, 28));
		btn.setContentAreaFilled(false); btn.setBorderPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.addActionListener(e -> r.run());
		return btn;
	}
}