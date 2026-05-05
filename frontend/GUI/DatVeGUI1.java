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

	    JPanel headerWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    headerWrapper.setBackground(BG);
	    headerWrapper.setBorder(new EmptyBorder(5, 0, 5, 0)); 

	    String textHeader = "Chiều đi: Ngày " + ngayDi + " từ " + gaDi + " đến " + gaDen;
	    JLabel lblChieu = new JLabel(textHeader);
	    lblChieu.setFont(new Font("Segoe UI", Font.BOLD, 13));
	    lblChieu.setForeground(Color.WHITE);
	    lblChieu.setOpaque(true);
	    lblChieu.setBackground(NAVY_LIGHT);
	    lblChieu.setBorder(new EmptyBorder(6, 12, 6, 12));

	    headerWrapper.add(lblChieu);
	    c.add(headerWrapper, BorderLayout.NORTH);

	    JPanel body = new JPanel(new BorderLayout(0, 6));
	    body.setBackground(BG);
	    body.setBorder(new EmptyBorder(8, 10, 4, 10));

	    // Nới rộng chiều cao hàng tàu lên 175 để đầu tàu có không gian dài ra, không bị ép chữ
	    JPanel rowContainer = buildChuyenRow();
	    rowContainer.setPreferredSize(new Dimension(0, 140)); 

	    body.add(rowContainer, BorderLayout.NORTH);
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

	    // Tạo Panel nội dung chữ trước để lấy kích thước thực tế
	    JPanel info = new JPanel(new GridLayout(4, 1, 0, 0)); 
	    info.setOpaque(false);
	    String[] labels = {"TG đi:", ch[1], "TG đến:", ch[2]};
	    for (int i = 0; i < labels.length; i++) {
	        JLabel l = new JLabel(labels[i]);
	        l.setFont(new Font("Segoe UI", Font.BOLD, (i % 2 == 0) ? 10 : 12));
	        l.setForeground((i % 2 == 0) ? new Color(100, 100, 100) : Color.BLACK);
	        l.setHorizontalAlignment(SwingConstants.LEFT);
	        info.add(l);
	    }

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

	            // 2. VẼ KHUNG TRẮNG ÔM KHÍT
	            g2.setColor(Color.WHITE);
	            // Lấy chiều cao thực tế của cụm chữ + 10px padding cho đẹp
	            int infoH = info.getPreferredSize().height + 10; 
	            int infoW = w - 16;
	            // Vẽ khung trắng bắt đầu từ y=32, cao đúng bằng infoH
	            g2.fillRoundRect(8, 32, infoW, infoH, 12, 12);

	            // 3. Đèn tàu (vị trí cố định phía dưới)
	            g2.setColor(Color.WHITE);
	            g2.fillOval(w/4 - 8, trainH - 14, 16, 16);
	            g2.fillOval(3*w/4 - 8, trainH - 14, 16, 16);

	            // 4. Đường ray
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

	    // Badge tên tàu
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
	    
	    JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
	    badgeWrap.setOpaque(false);
	    badgeWrap.add(badge);
	    card.add(badgeWrap, BorderLayout.NORTH);

	    // Đưa Panel chữ vào Wrapper để ôm khít trong khung trắng
	    JPanel infoWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 5));
	    infoWrapper.setOpaque(false);
	    // Căn chỉnh lề trên là 5px để lọt lòng vào ô trắng có y=32
	    infoWrapper.setBorder(new EmptyBorder(5, 0, 0, 0)); 
	    infoWrapper.add(info);

	    card.add(infoWrapper, BorderLayout.CENTER);

	    return card;
	}

	// ══ TOA + GHẾ ════════════════════════════════════════
	private JPanel buildToaGheSection() {
		JPanel sec = new JPanel(new BorderLayout(0, 0));
		sec.setBackground(BG);

		// --- Khối Top: Tiêu đề và Thanh cuộn ---
		JPanel topWrapper = new JPanel(new BorderLayout(0, 4)); 
		topWrapper.setBackground(BG);

		JPanel ttlWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ttlWrapper.setBackground(BG);
		JLabel ttl = new JLabel("Danh sách toa tàu");
		ttl.setFont(fb(13));
		ttl.setForeground(Color.WHITE);
		ttl.setOpaque(true);
		ttl.setBackground(NAVY_LIGHT);
		ttl.setBorder(new EmptyBorder(6, 12, 6, 12));
		ttlWrapper.add(ttl);
		
		topWrapper.add(ttlWrapper, BorderLayout.NORTH);

		pnlToaScroll = new JPanel();
		pnlToaScroll.setLayout(new BoxLayout(pnlToaScroll, BoxLayout.X_AXIS));
		pnlToaScroll.setBackground(BG);
		pnlToaScroll.setBorder(new EmptyBorder(0, 0, 0, 0)); 

		JScrollPane toaSP = new JScrollPane(pnlToaScroll, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		toaSP.setBorder(null);
		toaSP.setBackground(BG);
		toaSP.setPreferredSize(new Dimension(0, 100));
		toaSP.getHorizontalScrollBar().setUnitIncrement(20);
		
		topWrapper.add(toaSP, BorderLayout.CENTER);
		sec.add(topWrapper, BorderLayout.NORTH);

		// --- Khối Bottom: Toa thường, Danh sách ghế và Chú thích ---
		JPanel gheSec = new JPanel(new BorderLayout(0, 2)); 
		gheSec.setBackground(BG);
		gheSec.setBorder(new EmptyBorder(0, 0, 0, 0)); 

		lblTenToa = new JLabel("Toa thường: --", SwingConstants.CENTER);
		lblTenToa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblTenToa.setForeground(NAVY);
		gheSec.add(lblTenToa, BorderLayout.NORTH);

		JPanel pnlGheWrapper = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(200, 205, 225)); 
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15); 
				g2.dispose();
			}
		};
		pnlGheWrapper.setBackground(BG);
		pnlGheWrapper.setBorder(new EmptyBorder(4, 4, 4, 4)); 
		
		pnlGhe = new JPanel();
		pnlGhe.setBackground(BG);
		pnlGheWrapper.add(pnlGhe, BorderLayout.CENTER);
		
		gheSec.add(pnlGheWrapper, BorderLayout.CENTER);

		// --- 3. Chú thích (CĂN GIỮA TUYỆT ĐỐI VÀ ÉP SÁT LỀ PHẢI) ---
		JPanel botLegendArea = new JPanel(new BorderLayout());
		botLegendArea.setBackground(BG);
		// Xóa padding trái/phải về 0 để legend sát mép khung
		botLegendArea.setBorder(new EmptyBorder(4, 0, 2, 0));

		lblGheTrong = new JLabel("Số ghế còn trống: --", SwingConstants.CENTER);
		lblGheTrong.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblGheTrong.setForeground(Color.BLACK);
		
		// Cụm 3 ô màu bên phải: giảm khoảng cách giữa chúng xuống 6px cho sát nhau
		JPanel legendBoxes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		legendBoxes.setBackground(BG);
		legendBoxes.add(makeLegend(SEAT_SEL, "Đang chọn"));
		legendBoxes.add(makeLegend(SEAT_OK, "Còn trống"));
		legendBoxes.add(makeLegend(SEAT_TAKEN, "Đã đặt"));

		// TẠO ĐỐI TRỌNG BÊN TRÁI: Cục panel tàng hình có chiều ngang bằng đúng legendBoxes
		JPanel leftDummy = new JPanel();
		leftDummy.setBackground(BG);
		leftDummy.setPreferredSize(legendBoxes.getPreferredSize());

		// Gắn vào: Trái tàng hình - Giữa chữ - Phải 3 nút
		botLegendArea.add(leftDummy, BorderLayout.WEST);
		botLegendArea.add(lblGheTrong, BorderLayout.CENTER);
		botLegendArea.add(legendBoxes, BorderLayout.EAST);

		gheSec.add(botLegendArea, BorderLayout.SOUTH);
		sec.add(gheSec, BorderLayout.CENTER);

		refreshToaGhe();
		return sec;
	}

	private void refreshToaGhe() {
		String[] toaList = buildToaList(chuyenIdx);

		// Vẽ toa
		pnlToaScroll.removeAll();
		int gap = 14; 
		pnlToaScroll.add(Box.createHorizontalStrut(gap)); 
		for (int t = 0; t < toaList.length; t++) {
			pnlToaScroll.add(buildToaIcon(toaList[t], t == toaIdx, t));
			if (t < toaList.length - 1) {
				pnlToaScroll.add(Box.createHorizontalStrut(gap)); 
			}
		}
		pnlToaScroll.add(Box.createHorizontalStrut(gap)); 

		pnlToaScroll.revalidate();
		pnlToaScroll.repaint();

		// Vẽ ghế
		Set<Integer> dadat = gheDaDat(chuyenIdx, toaIdx);
		// ĐÃ XÓA KHOẢNG TRẮNG ĐỂ CĂN GIỮA CHUẨN
		lblTenToa.setText("Toa thường: " + toaList[toaIdx]); 
		int trong = 0;
		for (int g = 1; g <= 28; g++)
			if (!dadat.contains(g))
				trong++;
		// ĐÃ XÓA KHOẢNG TRẮNG ĐỂ CĂN GIỮA CHUẨN
		lblGheTrong.setText("Số ghế còn trống: " + trong + "/28");

		pnlGhe.removeAll();
		pnlGhe.setLayout(new GridLayout(2, 14, 4, 4)); // Giãn cách giữa các ghế
		pnlGhe.setBorder(new EmptyBorder(6, 10, 6, 10)); 

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
		JPanel p = new JPanel(new BorderLayout(0, 0)) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				// Giữ lại nền xanh nhạt khi chọn toa
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

		// --- ĐÃ SỬA: Dùng JLabel chứa ảnh thay vì panel vẽ tay ---
		JLabel icon = new JLabel();
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		icon.setPreferredSize(new Dimension(52, 44));

		// Load ảnh (nhớ đảm bảo file ảnh là logoToaTau.png nằm trong thư mục /Images)
		Icon toaImg = loadAndScaleIcon("/Images/logoToaTau.png", 56, 36);
		if (toaImg != null) {
			icon.setIcon(toaImg);
		} else {
			icon.setText("Lỗi ảnh"); // Sẽ hiện chữ này nếu sai đường dẫn hoặc sai tên file
		}

		// Nhãn hiển thị mã toa (SE801...)
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

		JButton btnQuay = makeOutlineBtn("↩ Quay lại");
		btnQuay.addActionListener(e -> {
			if (onQuayLai != null)
				onQuayLai.run();
		});

		lblGheDaChon = new JLabel("  Số vé đã chọn: 0/" + soLuong);
		lblGheDaChon.setFont(f(12));
		lblGheDaChon.setForeground(new Color(50, 70, 110));

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

		// SỬA TẠI ĐÂY: Đổi số 7 thành 2 để ép nút bấm sát mép trên/dưới của footer
		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
		
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
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		p.setBackground(BG);
		JPanel box = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(c);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3); // Bo góc ô màu 3px
				g2.dispose();
			}
		};
		// Phóng to ô màu lên xíu cho cân đối với cỡ chữ 14
		box.setPreferredSize(new Dimension(16, 16)); 
		box.setOpaque(false);
		
		JLabel l = new JLabel(txt);
		l.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Nâng lên chữ cỡ 14
		l.setForeground(Color.BLACK);
		
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