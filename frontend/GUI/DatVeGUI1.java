package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * DatVeGUI1 — JPanel kết quả tìm chuyến, swap vào content area của AppFrame.
 * Nhận Runnable onQuayLai để callback về DatVeGUI.
 */
public class DatVeGUI1 extends JPanel {

	// ══ Màu ══════════════════════════════════════════════
	// Định nghĩa bảng màu theo thiết kế Figma (Xanh Navy, Xám nhạt, Xanh sáng...)
	private static final Color NAVY = new Color(28, 57, 110);
	private static final Color NAVY_LIGHT = new Color(44, 82, 150);
	private static final Color NAVY_SEL = new Color(100, 160, 230); // Màu viền card đang chọn
	private static final Color BG = new Color(242, 247, 252);
	private static final Color BORDER_C = new Color(180, 205, 230);
	private static final Color SEAT_OK = new Color(28, 57, 110); // Màu xanh đậm của ghế trống
	private static final Color SEAT_TAKEN = new Color(180, 190, 210); // Màu xám của ghế đã đặt
	private static final Color SEAT_SEL = new Color(140, 185, 255); // Màu xanh nhạt của ghế đang chọn

	// ══ Params ═══════════════════════════════════════════
	private final String gaDi, gaDen, loaiVe, ngayDi, ngayVe;
	private final int soLuong;
	private final boolean motChieu;
	private final Runnable onQuayLai;

	// ══ Dữ liệu giả ══════════════════════════════════════
	// Danh sách dữ liệu hiển thị trên các thẻ chuyến tàu (Mã tàu, giờ đi/đến)
	private static final String[][] CHUYEN = {
			{ "SE8", "28/04/2026 06:00", "29/04/2026 16:10", "28/04/2026", "29/04/2026" },
			{ "SE6", "28/04/2026 08:40", "29/04/2026 19:12", "28/04/2026", "29/04/2026" },
			{ "SE10", "28/04/2026 13:20", "30/04/2026 04:25", "28/04/2026", "30/04/2026" },
			{ "TN1", "28/04/2026 15:25", "30/04/2026 04:55", "28/04/2026", "30/04/2026" },
			{ "SE4", "28/04/2026 20:35", "30/04/2026 05:45", "28/04/2026", "30/04/2026" },
			{ "SE2", "28/04/2026 20:55", "30/04/2026 10:25", "28/04/2026", "30/04/2026" },
			{ "SE12", "28/04/2026 22:55", "30/04/2026 12:05", "28/04/2026", "30/04/2026" },
			{ "SE5", "29/04/2026 05:30", "30/04/2026 15:40", "29/04/2026", "30/04/2026" },
			{ "TN3", "29/04/2026 11:00", "30/04/2026 21:10", "29/04/2026", "30/04/2026" }, };
	private static final int[] TOA_COUNT = { 11, 11, 10, 12, 13, 10, 12, 11, 12 };

	private String[] buildToaList(int ci) {
		int n = TOA_COUNT[ci];
		String[] t = new String[n];
		for (int i = 0; i < n; i++)
			t[i] = CHUYEN[ci][0] + String.format("%02d", i + 1);
		return t;
	}

	private Set<Integer> gheDaDat(int ci, int ti) {
		Random rnd = new Random((long) ci * 100 + ti);
		Set<Integer> s = new LinkedHashSet<>();
		for (int g = 1; g <= 28; g++)
			if (rnd.nextDouble() < 0.3)
				s.add(g);
		return s;
	}

	// ══ State ════════════════════════════════════════════
	private int trang = 0;
	private int chuyenIdx = 0;
	private int toaIdx = 0;
	private final Set<Integer> gheChon = new LinkedHashSet<>();

	// ══ Components ═══════════════════════════════════════
	private JPanel pnlChuyen;
	private JPanel pnlToaScroll;
	private JPanel pnlGhe;
	private JLabel lblTenToa, lblGheTrong, lblGheDaChon;
	private JButton btnPrev, btnNext, btnAction;

	// ═════════════════════════════════════════════════════
	public DatVeGUI1(String gaDi, String gaDen, String loaiVe, String ngayDi, String ngayVe, int soLuong,
			Runnable onQuayLai) {
		this.gaDi = gaDi;
		this.gaDen = gaDen;
		this.loaiVe = loaiVe;
		this.ngayDi = ngayDi;
		this.ngayVe = ngayVe;
		this.soLuong = soLuong;
		this.onQuayLai = onQuayLai;
		this.motChieu = loaiVe.contains("chiều") || loaiVe.contains("Chiều");

		setLayout(new BorderLayout());
		setBackground(BG);
		// buildTopBar(): Thực hiện vẽ dải màu trắng chứa các ô "Ga đi", "Ga đến"... ở
		// trên cùng
		add(buildTopBar(), BorderLayout.NORTH);
		// buildCenter(): Thực hiện vẽ vùng tiêu đề xanh đậm và toàn bộ khu vực chọn
		// tàu/toa/ghế
		add(buildCenter(), BorderLayout.CENTER);
		// buildBotBar(): Thực hiện vẽ thanh dưới cùng chứa nút "Quay lại", "Số vé chọn"
		// và "Chọn nhanh"
		add(buildBotBar(), BorderLayout.SOUTH);
	}

	// ══ THANH THÔNG TIN TRÊN ════════════════════════════
	private JPanel buildTopBar() {
	    JPanel bar = new JPanel(new GridBagLayout());
	    bar.setBackground(Color.WHITE);
	    
	    // Header padding dưới 5px so với dải màu xanh
	    bar.setBorder(BorderFactory.createCompoundBorder(
	        new MatteBorder(0, 0, 1, 0, new Color(210, 215, 224)),
	        new EmptyBorder(0, 0, 5, 0) 
	    ));

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.VERTICAL;

	    // ── KHỐI THÔNG TIN BÊN TRÁI ──
	    JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    info.setBackground(Color.WHITE);

	    String[] lbls = { "Ga đi:", "Ga đến:", "Loại vé:", "Ngày đi:", "Ngày về:", "Số lượng:" };
	    String[] vals = { gaDi, gaDen, loaiVe, ngayDi, ngayDi, String.valueOf(soLuong) };
	    boolean[] isDimmed = { false, false, false, false, motChieu, false };

	    for (int i = 0; i < lbls.length; i++) {
	        info.add(addInfoCell(lbls[i], vals[i], isDimmed[i], 6));
	    }

	    gbc.gridx = 0; 
	    gbc.weightx = 1.0;
	    gbc.anchor = GridBagConstraints.WEST;
	    bar.add(info, gbc);

	    // ── KHỐI NÚT BẤM BÊN PHẢI (CĂN CHỈNH TUYỆT ĐỐI) ──
	    // Dùng BoxLayout để xếp chồng một khoảng trống phía trên 2 nút
	    JPanel rightWrapper = new JPanel();
	    rightWrapper.setLayout(new BoxLayout(rightWrapper, BoxLayout.Y_AXIS));
	    rightWrapper.setBackground(Color.WHITE);

	    // 1. Tạo khoảng trống bằng đúng chiều cao của nhãn (khoảng 19-20px)
	    // Con số 19px thường khớp với Font 14 + khoảng cách mặc định của BorderLayout
	    rightWrapper.add(Box.createVerticalStrut(19)); 

	    // 2. Panel chứa 2 nút bấm
	    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
	    buttonPanel.setBackground(Color.WHITE);
	    buttonPanel.setBorder(null);

	    Icon iconChieuVe = loadAndScaleIcon("/Images/logoKhuhoi.png", 14, 14);
	    Icon iconLamMoi = loadAndScaleIcon("/Images/logoLammoi.png", 14, 14);

	    JButton btnChieuVe = buildStyledButton("Chiều về", iconChieuVe);
	    // Độ cao 25px để khớp với độ cao của ô khung v bên trái
	    btnChieuVe.setPreferredSize(new Dimension(btnChieuVe.getPreferredSize().width, 28));
	    btnChieuVe.setEnabled(!motChieu);

	    JButton btnLamMoi = buildStyledButton("Làm mới", iconLamMoi);
	    btnLamMoi.setPreferredSize(new Dimension(btnLamMoi.getPreferredSize().width, 28));
	    btnLamMoi.addActionListener(e -> { if (onQuayLai != null) onQuayLai.run(); });

	    buttonPanel.add(btnChieuVe);
	    buttonPanel.add(btnLamMoi);
	    
	    rightWrapper.add(buttonPanel);

	    gbc.gridx = 1; 
	    gbc.weightx = 0;
	    gbc.anchor = GridBagConstraints.NORTH; 
	    gbc.insets = new Insets(0, 0, 0, 10);
	    bar.add(rightWrapper, gbc);

	    return bar;
	}

	private JPanel addInfoCell(String label, String value, boolean dimmed, int rightGap) {
	    JPanel cell = new JPanel(new BorderLayout(0, 0));
	    cell.setBackground(Color.WHITE);
	    cell.setBorder(new EmptyBorder(0, 0, 0, rightGap));

	    JLabel l = new JLabel(label, SwingConstants.CENTER);
	    l.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
	    l.setForeground(GuiTheme.SUB_TEXT);

	    JLabel v = new JLabel(value, SwingConstants.CENTER);
	    v.setFont(new Font("Segoe UI", Font.PLAIN, 14));
	    v.setOpaque(true);
	    v.setBackground(Color.WHITE);

	    if (dimmed) {
	        v.setForeground(new Color(180, 180, 180));
	    } else {
	        v.setForeground(Color.BLACK);
	    }

	    // Giữ khung co giãn và chữ đen như thiết kế
	    v.setBorder(new CompoundBorder(
	        new LineBorder(new Color(200, 200, 200), 1), 
	        new EmptyBorder(2, 12, 2, 12)
	    ));

	    cell.add(l, BorderLayout.NORTH);
	    cell.add(v, BorderLayout.CENTER);
	    return cell;
	}

	/**
	 * Hàm bổ trợ tải ảnh và thu nhỏ mượt mà
	 */
	private Icon loadAndScaleIcon(String path, int w, int h) {
		try {
			java.net.URL url = getClass().getResource(path);
			if (url != null) {
				Image img = new ImageIcon(url).getImage();
				Image scaledImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
				return new ImageIcon(scaledImg);
			}
		} catch (Exception e) {
			System.err.println("Lỗi tải icon: " + path);
		}
		return null;
	}
	
	private JButton buildStyledButton(String text, Icon icon) {
	    JButton btn = new JButton(text) {
	        @Override
	        protected void paintComponent(Graphics g) {
	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	            
	            if (!isEnabled()) {
	                g2.setColor(new Color(130, 150, 185)); 
	            } else if (getModel().isPressed()) {
	                g2.setColor(GuiTheme.NAVY_DARK);
	            } else if (getModel().isRollover()) {
	                g2.setColor(GuiTheme.NAVY_HOVER);
	            } else {
	                g2.setColor(GuiTheme.NAVY);
	            }

	            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

	            g2.setColor(Color.WHITE); 
	            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
	            
	            FontMetrics fm = g2.getFontMetrics();
	            int textWidth = fm.stringWidth(getText());
	            int iconWidth = (getIcon() != null) ? getIcon().getIconWidth() : 0;
	            int gap = (getIcon() != null) ? 8 : 0;
	            
	            int startX = (getWidth() - (iconWidth + gap + textWidth)) / 2;
	            int centerY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
	            
	            if (getIcon() != null) {
	                int iconY = (getHeight() - getIcon().getIconHeight()) / 2;
	                getIcon().paintIcon(this, g2, startX, iconY);
	                g2.drawString(getText(), startX + iconWidth + gap, centerY);
	            } else {
	                g2.drawString(getText(), (getWidth() - textWidth) / 2, centerY);
	            }
	            g2.dispose();
	        }
	    };
	    
	    btn.setIcon(icon);
	    btn.setContentAreaFilled(false);
	    btn.setBorderPainted(false);
	    btn.setFocusPainted(false);
	    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    
	    // Chiều cao nút cố định khoảng 26px để khớp với chiều cao khung v bên trái
	    btn.setBorder(new EmptyBorder(3, 15, 3, 15)); 
	    
	    return btn;
	}
	// ══ VÙNG GIỮA ═══════════════════════════════════════
	private JPanel buildCenter() {
	    JPanel c = new JPanel(new BorderLayout(0, 0));
	    c.setBackground(BG);

	    // --- PHẦN HEADER CHIỀU ĐI (Nền xanh ôm khít, xóa khoảng cách thừa) ---
	    JPanel headerWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    headerWrapper.setBackground(BG);
	    headerWrapper.setBorder(new EmptyBorder(5, 0, 5, 0)); 

	    // Sử dụng chuỗi text sạch, chỉ dùng 1 dấu cách duy nhất
	    String textHeader = "Chiều đi: Ngày " + ngayDi + " từ " + gaDi + " đến " + gaDen;
	    JLabel lblChieu = new JLabel(textHeader);
	    
	    // Ép font Segoe UI Bold size 13 chuẩn
	    lblChieu.setFont(new Font("Segoe UI", Font.BOLD, 13));
	    lblChieu.setForeground(Color.WHITE);
	    lblChieu.setOpaque(true);
	    lblChieu.setBackground(NAVY_LIGHT);
	    
	    // Border: 6px trên dưới, 12px trái phải để dải màu không quá thô
	    lblChieu.setBorder(new EmptyBorder(6, 12, 6, 12));

	    headerWrapper.add(lblChieu);
	    c.add(headerWrapper, BorderLayout.NORTH);

	    // --- PHẦN NỘI DUNG CHÍNH (BODY) ---
	    JPanel body = new JPanel(new BorderLayout(0, 6));
	    body.setBackground(BG);
	    body.setBorder(new EmptyBorder(8, 10, 4, 10));

	    body.add(buildChuyenRow(), BorderLayout.NORTH);
	    body.add(buildToaGheSection(), BorderLayout.CENTER);

	    c.add(body, BorderLayout.CENTER);
	    return c;
	}

	// ══ 6 THẺ CHUYẾN + MŨI TÊN ══════════════════════════
	private JPanel buildChuyenRow() {
	    JPanel row = new JPanel(new BorderLayout(2, 0));
	    row.setBackground(BG);

	    // Nút điều hướng mũi tên
	    btnPrev = makeNavArrow("‹");
	    btnNext = makeNavArrow("›");
	    
	    btnPrev.addActionListener(e -> {
	        if (trang > 0) {
	            trang--;
	            refreshChuyen();
	        }
	    });
	    
	    btnNext.addActionListener(e -> {
	        // Thay đổi logic kiểm tra theo số lượng 5 chuyến mỗi trang
	        if ((trang + 1) * 5 < CHUYEN.length) {
	            trang++;
	            refreshChuyen();
	        }
	    });

	    // SỬA ĐỔI: Chuyển GridLayout từ 6 cột xuống 5 cột để card rộng rãi hơn
	    pnlChuyen = new JPanel(new GridLayout(1, 5, 10, 0)); 
	    pnlChuyen.setBackground(BG);
	    refreshChuyen();

	    row.add(btnPrev, BorderLayout.WEST);
	    row.add(pnlChuyen, BorderLayout.CENTER);
	    row.add(btnNext, BorderLayout.EAST);
	    return row;
	}

	private void refreshChuyen() {
		pnlChuyen.removeAll();
		int from = trang * 5, to = Math.min(from + 5, CHUYEN.length);
		for (int i = from; i < to; i++)
			pnlChuyen.add(buildCard(i));
		for (int i = to - from; i < 5; i++) {
			JPanel empty = new JPanel();
			empty.setOpaque(false);
			pnlChuyen.add(empty);
		}
		btnPrev.setEnabled(trang > 0);
		btnNext.setEnabled((trang + 1) * 5 < CHUYEN.length);
		pnlChuyen.revalidate();
		pnlChuyen.repaint();
	}

	private JPanel buildCard(int ci) {
	    boolean sel = (ci == chuyenIdx);
	    String[] ch = CHUYEN[ci];

	    JPanel card = new JPanel(new BorderLayout(0, 0)) {
	        private boolean isHover = false;
	        {
	            addMouseListener(new MouseAdapter() {
	                @Override public void mouseEntered(MouseEvent e) { isHover = true; repaint(); }
	                @Override public void mouseExited(MouseEvent e) { isHover = false; repaint(); }
	                @Override public void mouseClicked(MouseEvent e) {
	                    chuyenIdx = ci; toaIdx = 0; gheChon.clear();
	                    refreshChuyen(); refreshToaGhe(); updateActionBtn();
	                }
	            });
	        }

	        @Override
	        protected void paintComponent(Graphics g) {
	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            
	            int w = getWidth(), h = getHeight();
	            int trainH = h - 20; 

	            Color mainColor = new Color(160, 160, 160);
	            if (sel) mainColor = new Color(0, 150, 215);
	            else if (isHover) mainColor = new Color(255, 200, 0);

	            // 1. Vẽ thân tàu
	            g2.setColor(mainColor);
	            g2.fillRoundRect(2, 2, w - 4, trainH, 25, 25);

	            // 2. VẼ Ô TRẮNG: Nới cao thêm để không tràn chữ
	            g2.setColor(Color.WHITE);
	            // y=28 (đưa sát lên trên), chiều cao nới rộng (trainH - 45)
	            g2.fillRoundRect(8, 28, w - 16, trainH - 45, 12, 12);

	            // 3. Vẽ đèn tàu (Dịch xuống sát mép dưới thân)
	            g2.setColor(Color.WHITE);
	            g2.fillOval(w/4 - 8, trainH - 14, 16, 16);
	            g2.fillOval(3*w/4 - 8, trainH - 14, 16, 16);

	            // 4. Vẽ đường ray
	            g2.setColor(Color.BLACK);
	            int railY = h - 15;
	            g2.setStroke(new BasicStroke(1.5f));
	            g2.drawLine(15, railY, 5, h - 2);
	            g2.drawLine(w - 15, railY, w - 5, h - 2);
	            for (int i = 0; i <= 3; i++) {
	                int y = railY + (i * 4);
	                g2.drawLine(15 - (i*2), y, w - 15 + (i*2), y);
	            }
	            g2.dispose();
	        }
	    };
	    card.setOpaque(false);
	    card.setCursor(new Cursor(Cursor.HAND_CURSOR));

	    // Badge tên tàu bo tròn
	    JLabel badge = new JLabel(ch[0], SwingConstants.CENTER) {
	        @Override protected void paintComponent(Graphics g) {
	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            g2.setColor(Color.WHITE);
	            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
	            super.paintComponent(g); g2.dispose();
	        }
	    };
	    badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
	    badge.setPreferredSize(new Dimension(55, 22));
	    
	    // Panel chứa badge đưa sát lên trên
	    JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
	    badgeWrap.setOpaque(false);
	    badgeWrap.add(badge);
	    card.add(badgeWrap, BorderLayout.NORTH);

	    // --- PHẦN CHỮ: SÁT LÊN TRÊN VÀ KHÔNG PADDING ---
	    JPanel info = new JPanel(new GridLayout(4, 1, 0, 0)); 
	    info.setOpaque(false);
	    // Top = 0px để sát lên trên, Bottom = 15px để chừa chỗ cho đèn tàu phía dưới
	    info.setBorder(new EmptyBorder(0, 14, 15, 10));

	    String[] labels = {"TG đi:", ch[1], "TG đến:", ch[2]};
	    for (int i = 0; i < labels.length; i++) {
	        JLabel l = new JLabel(labels[i]);
	        // Giữ cỡ chữ 10 và 12 như bạn muốn
	        l.setFont(new Font("Segoe UI", Font.BOLD, (i % 2 == 0) ? 10 : 12));
	        l.setForeground((i % 2 == 0) ? new Color(100, 100, 100) : Color.BLACK);
	        l.setHorizontalAlignment(SwingConstants.LEFT);
	        info.add(l);
	    }
	    card.add(info, BorderLayout.CENTER);

	    return card;
	}

	private JPanel buildTrainIcon(boolean sel) {
		// paintComponent: Thực hiện vẽ đồ họa chi tiết của icon toa tàu (Thân tàu, cửa
		// sổ, bánh xe)
		JPanel p = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth(), h = getHeight();
				// Thân tàu
				g2.setColor(sel ? NAVY_SEL : new Color(100, 130, 175));
				g2.fillRoundRect(3, 2, w - 6, h - 14, 8, 8);
				// Cửa sổ
				g2.setColor(new Color(210, 228, 255));
				g2.fillRoundRect(6, 6, 9, 6, 2, 2);
				g2.fillRoundRect(w / 2 - 3, 6, 9, 6, 2, 2);
				// Bánh
				g2.setColor(new Color(40, 40, 60));
				g2.fillOval(4, h - 12, 9, 9);
				g2.fillOval(w - 13, h - 12, 9, 9);
				// Đèn
				Color lampCol = sel ? new Color(255, 220, 80) : new Color(220, 210, 120);
				g2.setColor(lampCol);
				g2.fillOval(w - 11, h / 2 - 3, 5, 5);
				g2.dispose();
			}
		};
		p.setOpaque(false);
		p.setPreferredSize(new Dimension(52, 40));
		return p;
	}

	// ══ TOA + GHẾ ════════════════════════════════════════
	private JPanel buildToaGheSection() {
		JPanel sec = new JPanel(new BorderLayout(0, 4));
		sec.setBackground(BG);

		// ttl: Nhãn chữ "Danh sách toa tàu"
		JLabel ttl = new JLabel("  Danh sách toa tàu");
		ttl.setFont(fb(12));
		ttl.setForeground(new Color(45, 58, 90));
		sec.add(ttl, BorderLayout.NORTH);

		// ── Scroll toa ngang ──
		// pnlToaScroll: Thực hiện vẽ hàng ngang các biểu tượng toa tàu (SE801,
		// SE802...)
		pnlToaScroll = new JPanel();
		pnlToaScroll.setLayout(new BoxLayout(pnlToaScroll, BoxLayout.X_AXIS));
		pnlToaScroll.setBackground(BG);
		pnlToaScroll.setBorder(new EmptyBorder(4, 4, 4, 4));

		JScrollPane toaSP = new JScrollPane(pnlToaScroll, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		toaSP.setBorder(null);
		toaSP.setBackground(BG);
		toaSP.setPreferredSize(new Dimension(0, 86));
		toaSP.getHorizontalScrollBar().setUnitIncrement(20);
		sec.add(toaSP, BorderLayout.CENTER);

		// ── Ghế ──
		JPanel gheSec = new JPanel(new BorderLayout(0, 3));
		gheSec.setBackground(BG);

		// lblTenToa: Thực hiện vẽ nhãn chữ "Toa thường: SE801"
		lblTenToa = new JLabel("  Toa thường: --");
		lblTenToa.setFont(fb(12));
		lblTenToa.setForeground(NAVY_LIGHT);
		gheSec.add(lblTenToa, BorderLayout.NORTH);

		// pnlGhe: Thực hiện vẽ bảng lưới các ô ghế ngồi số từ 1 đến 28
		pnlGhe = new JPanel();
		pnlGhe.setBackground(Color.WHITE);
		pnlGhe.setBorder(BorderFactory.createLineBorder(BORDER_C));
		gheSec.add(pnlGhe, BorderLayout.CENTER);

		// Chú thích
		// legend: Thực hiện vẽ dòng "Số ghế còn trống", "Đang chọn", "Còn trống", "Đã
		// đặt" ở dưới bảng ghế
		JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
		legend.setBackground(BG);
		lblGheTrong = new JLabel("  Số ghế còn trống: --");
		lblGheTrong.setFont(f(11));
		lblGheTrong.setForeground(new Color(70, 85, 110));
		legend.add(lblGheTrong);
		legend.add(makeLegend(SEAT_SEL, "Đang chọn"));
		legend.add(makeLegend(SEAT_OK, "Còn trống"));
		legend.add(makeLegend(SEAT_TAKEN, "Đã đặt"));
		gheSec.add(legend, BorderLayout.SOUTH);

		sec.add(gheSec, BorderLayout.SOUTH);

		refreshToaGhe();
		return sec;
	}

	private void refreshToaGhe() {
		String[] toaList = buildToaList(chuyenIdx);

		// Vẽ toa
		pnlToaScroll.removeAll();
		pnlToaScroll.add(Box.createHorizontalStrut(4));
		for (int t = 0; t < toaList.length; t++) {
			pnlToaScroll.add(buildToaIcon(toaList[t], t == toaIdx, t));
			pnlToaScroll.add(Box.createHorizontalStrut(4));
		}
		pnlToaScroll.revalidate();
		pnlToaScroll.repaint();

		// Vẽ ghế
		Set<Integer> dadat = gheDaDat(chuyenIdx, toaIdx);
		lblTenToa.setText("  Toa thường: " + toaList[toaIdx]);
		int trong = 0;
		for (int g = 1; g <= 28; g++)
			if (!dadat.contains(g))
				trong++;
		lblGheTrong.setText("  Số ghế còn trống: " + trong + "/28");

		pnlGhe.removeAll();
		pnlGhe.setLayout(new GridLayout(2, 14, 3, 3));
		pnlGhe.setBorder(new EmptyBorder(7, 10, 7, 10));

		// Đoạn loop này thực hiện tạo ra từng nút bấm tương ứng với số ghế (1, 2, 3...)
		// và đổ màu theo trạng thái
		for (int i = 1; i <= 28; i++) {
			final int gNum = i;
			final boolean taken = dadat.contains(i);
			JButton btn = new JButton(String.valueOf(i)) {
				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					Color bg = taken ? SEAT_TAKEN : gheChon.contains(gNum) ? SEAT_SEL : SEAT_OK;
					g2.setColor(bg);
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
					g2.setColor(Color.WHITE);
					g2.setFont(f(10));
					FontMetrics fm = g2.getFontMetrics();
					String t = getText();
					g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
							(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
					g2.dispose();
				}
			};
			btn.setPreferredSize(new Dimension(30, 26));
			btn.setContentAreaFilled(false);
			btn.setBorderPainted(false);
			btn.setFocusPainted(false);
			if (!taken) {
				btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
				btn.addActionListener(ev -> {
					if (gheChon.contains(gNum))
						gheChon.remove(gNum);
					else if (gheChon.size() < soLuong)
						gheChon.add(gNum);
					refreshToaGhe();
					updateGheDaChon();
					updateActionBtn();
				});
			}
			pnlGhe.add(btn);
		}
		pnlGhe.revalidate();
		pnlGhe.repaint();
		updateGheDaChon();
	}

	private JPanel buildToaIcon(String maToa, boolean sel, int ti) {
		// paintComponent: Thực hiện vẽ icon của từng toa tàu trong danh sách (Hình chữ
		// nhật xanh, cửa sổ, bánh xe)
		JPanel p = new JPanel(new BorderLayout(0, 0)) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(sel ? new Color(210, 232, 255) : new Color(235, 242, 252));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
				if (sel) {
					g2.setColor(NAVY_SEL);
					g2.setStroke(new BasicStroke(1.5f));
					g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 7, 7);
				}
				g2.dispose();
			}
		};
		p.setOpaque(false);
		p.setBorder(new EmptyBorder(3, 3, 3, 3));
		p.setMaximumSize(new Dimension(58, 78));
		p.setPreferredSize(new Dimension(58, 78));
		p.setCursor(new Cursor(Cursor.HAND_CURSOR));

		JPanel icon = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth(), h = getHeight();
				g2.setColor(sel ? NAVY : new Color(75, 105, 155));
				g2.fillRoundRect(3, 2, w - 6, h - 12, 6, 6);
				// Cửa sổ
				g2.setColor(new Color(205, 225, 255));
				g2.fillRect(6, 6, 8, 5);
				g2.fillRect(w / 2 - 2, 6, 8, 5);
				// Bánh
				g2.setColor(new Color(35, 35, 55));
				g2.fillOval(4, h - 10, 7, 7);
				g2.fillOval(w - 11, h - 10, 7, 7);
				g2.dispose();
			}
		};
		icon.setOpaque(false);
		icon.setPreferredSize(new Dimension(52, 44));

		// lbl: Thực hiện vẽ mã tên toa dưới icon (Ví dụ: SE801)
		JLabel lbl = new JLabel(maToa, SwingConstants.CENTER);
		lbl.setFont(f(9));
		lbl.setForeground(sel ? NAVY : new Color(55, 75, 115));

		p.add(icon, BorderLayout.CENTER);
		p.add(lbl, BorderLayout.SOUTH);
		p.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				toaIdx = ti;
				gheChon.clear();
				refreshToaGhe();
				updateActionBtn();
			}
		});
		return p;
	}

	// ══ THANH DƯỚI ════════════════════════════════════════
	private JPanel buildBotBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

		// btnQuay: Thực hiện vẽ nút "Quay lại" ở góc trái phía dưới
		JButton btnQuay = makeOutlineBtn("↩ Quay lại");
		btnQuay.addActionListener(e -> {
			if (onQuayLai != null)
				onQuayLai.run();
		});

		// lblGheDaChon: Thực hiện vẽ nhãn hiển thị "Số vé đã chọn: 0/1" ở góc phải
		lblGheDaChon = new JLabel("  Số vé đã chọn: 0/" + soLuong);
		lblGheDaChon.setFont(f(12));
		lblGheDaChon.setForeground(new Color(50, 70, 110));

		// btnAction: Thực hiện vẽ nút "Chọn nhanh" màu xanh Navy ở góc phải phía dưới
		btnAction = makeNavyBtn("⇒ Chọn nhanh");
		btnAction.addActionListener(e -> {
			if (gheChon.size() >= soLuong) {
				JOptionPane.showMessageDialog(this, "Đã chọn " + soLuong + " ghế!\nTiến hành thanh toán...",
						"Xác nhận đặt vé", JOptionPane.INFORMATION_MESSAGE);
			} else {
				gheChon.clear();
				Set<Integer> dadat = gheDaDat(chuyenIdx, toaIdx);
				for (int g = 1; g <= 28 && gheChon.size() < soLuong; g++)
					if (!dadat.contains(g))
						gheChon.add(g);
				refreshToaGhe();
				updateGheDaChon();
				updateActionBtn();
			}
		});

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 7));
		left.setBackground(Color.WHITE);
		right.setBackground(Color.WHITE);
		left.add(btnQuay);
		right.add(lblGheDaChon);
		right.add(btnAction);
		bar.add(left, BorderLayout.WEST);
		bar.add(right, BorderLayout.EAST);
		return bar;
	}

	private void updateGheDaChon() {
		if (lblGheDaChon != null)
			lblGheDaChon.setText("  Số vé đã chọn: " + gheChon.size() + "/" + soLuong);
	}

	private void updateActionBtn() {
		if (btnAction == null)
			return;
		if (gheChon.size() >= soLuong) {
			btnAction.setText("Tiếp tục →");
		} else {
			btnAction.setText("⇒ Chọn nhanh");
		}
		btnAction.repaint();
	}

	// ══ HELPERS ══════════════════════════════════════════
	private JButton makeNavArrow(String t) {
		JButton b = new JButton(t) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(isEnabled() ? new Color(215, 228, 248) : new Color(238, 240, 246));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(isEnabled() ? NAVY : new Color(175, 185, 205));
				g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
				FontMetrics fm = g2.getFontMetrics();
				g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2,
						(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};
		b.setPreferredSize(new Dimension(26, 78));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JButton makeOutlineBtn(String text) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				boolean en = isEnabled();
				g2.setColor(en
						? (getModel().isPressed() ? new Color(198, 215, 242)
								: getModel().isRollover() ? new Color(212, 228, 250) : new Color(226, 236, 252))
						: new Color(238, 241, 248));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(en ? NAVY : new Color(175, 185, 205));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
				g2.setFont(f(12));
				g2.setColor(en ? NAVY : new Color(155, 165, 185));
				FontMetrics fm = g2.getFontMetrics();
				g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
						(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};
		b.setPreferredSize(new Dimension(112, 30));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JButton makeNavyBtn(String text) {
		JButton b = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? new Color(18, 42, 85)
						: getModel().isRollover() ? new Color(38, 68, 128) : NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.setColor(Color.WHITE);
				g2.setFont(fb(12));
				FontMetrics fm = g2.getFontMetrics();
				g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
						(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};
		b.setPreferredSize(new Dimension(136, 32));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JPanel makeLegend(Color c, String txt) {
		// makeLegend: Thực hiện vẽ một ô vuông màu nhỏ kèm nhãn chữ cho phần chú thích
		// (Legend)
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		p.setBackground(BG);
		JPanel box = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(c);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
				g2.dispose();
			}
		};
		box.setPreferredSize(new Dimension(13, 13));
		box.setOpaque(false);
		JLabel l = new JLabel(txt);
		l.setFont(f(10));
		l.setForeground(new Color(75, 88, 110));
		p.add(box);
		p.add(l);
		return p;
	}

	private Font f(int s) {
		return new Font("Segoe UI", Font.PLAIN, s);
	}

	private Font fb(int s) {
		return new Font("Segoe UI", Font.BOLD, s);
	}

}