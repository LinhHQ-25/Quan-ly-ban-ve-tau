package gui;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import dao.ChuyenTauDAO;
import connect_DB.Connect_DB;
import dao.VeDAO;

public class DatVeGUI1 extends JPanel {

	private static final Color NAVY = new Color(28, 57, 110);
	private static final Color NAVY_LIGHT = new Color(44, 82, 150);
	private static final Color NAVY_SEL = new Color(100, 160, 230);
	private static final Color BG = new Color(242, 247, 252);
	private static final Color BORDER_C = new Color(180, 205, 230);
	private static final Color SEAT_OK = new Color(28, 57, 110);
	private static final Color SEAT_TAKEN = new Color(180, 190, 210);
	private static final Color SEAT_SEL = new Color(140, 185, 255);
	private final ChuyenTauDAO chuyenTauDAO = new ChuyenTauDAO();
	private final String gaDi, gaDen, loaiVe, ngayDi, ngayVe;
	private final int soLuong;
	private final boolean motChieu;

	private final Runnable onQuayLai;

	private String[][] CHUYEN_FULL;
	private String[][] CHUYEN_FILTERED;
	private String[][] CHUYEN_FULL_VE;
	private String[][] CHUYEN_FILTERED_VE;

	private boolean dangXemChieuVe = false;
	private LocalDateTime selectedArrivalDi = null;
	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private int trangDi = 0, chuyenIdxDi = -1;
	private final Set<String> gheChonDi = new LinkedHashSet<>();
	private int trangVe = 0, chuyenIdxVe = -1;
	private final Set<String> gheChonVe = new LinkedHashSet<>();
	private boolean manualToaSelected = false;
	private int trang = 0;
	private int chuyenIdx = -1;
	private final Set<String> gheChon = new LinkedHashSet<>();

	private SmoothScrollPanel pnlScrollContent;
	private JPanel pnlChuyenWrapper;
	private JPanel pnlToaHeaderWrapper;
	private JPanel hdrToa;
	private JPanel stickyHeaderContainer;

	private JPanel pnlAllToas;
	private JScrollPane toaScrollPane;

	private List<JPanel> toaBlocks = new ArrayList<>();
	private List<String> toaMaToas = new ArrayList<>();
	private List<String> toaLoaiToas = new ArrayList<>();
	private List<JLabel> toaLogos = new ArrayList<>();
	private int currentVisibleToaIndex = -1;
	private String activeMaToa = null;

	private JLabel lblGheTrong, lblGheDaChon, lblChieuHeader;
	private JButton btnPrev, btnNext, btnAction, btnHuy, btnChieuToggle,btnLamMoi;
	private JComboBox<String> cbKhungGio, cbLoaiToa;
	private JPanel pnlChuyen;

	private final Font SEAT_FONT = new Font("Segoe UI", Font.BOLD, 12);
	private final Font ARROW_FONT = new Font("Segoe UI", Font.BOLD, 18);

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

		CHUYEN_FULL = chuyenTauDAO.loadChuyenFromDB(gaDi, gaDen, ngayDi);
		CHUYEN_FULL = filterChuyenDi(CHUYEN_FULL, ngayDi);
		if (CHUYEN_FULL == null || CHUYEN_FULL.length == 0)
			CHUYEN_FULL = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayDi, ngayDi, "" } };
		CHUYEN_FILTERED = CHUYEN_FULL;

		if (!motChieu && ngayVe != null && !ngayVe.isEmpty()) {
			CHUYEN_FULL_VE = chuyenTauDAO.loadChuyenFromDB(gaDen, gaDi, ngayVe);
			if (CHUYEN_FULL_VE == null || CHUYEN_FULL_VE.length == 0)
				CHUYEN_FULL_VE = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayVe, ngayVe, "" } };
		} else {
			CHUYEN_FULL_VE = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayVe, ngayVe, "" } };
		}
		CHUYEN_FILTERED_VE = CHUYEN_FULL_VE;

		setLayout(new BorderLayout());
		setBackground(BG);
		add(buildTopBar(), BorderLayout.NORTH);
		add(buildCenter(), BorderLayout.CENTER);
		add(buildBotBar(), BorderLayout.SOUTH);

		SwingUtilities.invokeLater(() -> {
			pnlAllToas.revalidate();
			pnlAllToas.repaint();
		});
	}

	/**
	 * Lọc chuyến đi: chỉ giữ các chuyến khởi hành sau now + 15 phút (chỉ áp dụng
	 * nếu ngày đi là hôm nay)
	 */
	private String[][] filterChuyenDi(String[][] data, String ngayDi) {
		if (data == null || data.length == 0)
			return data;

		// Kiểm tra ngày đi có phải hôm nay không
		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		if (!ngayDi.equals(today))
			return data; // Không phải hôm nay → không lọc

		LocalDateTime cutoff = LocalDateTime.now().plusMinutes(15);
		List<String[]> result = new ArrayList<>();
		for (String[] ch : data) {
			try {
				LocalDateTime tgDi = LocalDateTime.parse(ch[1], fmt);
				if (!tgDi.isBefore(cutoff))
					result.add(ch);
			} catch (Exception ex) {
				result.add(ch);
			}
		}
		return result.toArray(new String[0][]);
	}

	/**
	 * Lọc chuyến về: chỉ giữ các chuyến khởi hành sau tgDenChuyenDi + 30 phút
	 */
	private String[][] filterChuyenVe(String[][] data, LocalDateTime tgDenChuyenDi) {
		if (data == null || data.length == 0 || tgDenChuyenDi == null)
			return data;

		LocalDateTime cutoff = tgDenChuyenDi.plusMinutes(30);
		List<String[]> result = new ArrayList<>();
		for (String[] ch : data) {
			try {
				LocalDateTime tgDi = LocalDateTime.parse(ch[1], fmt);
				if (!tgDi.isBefore(cutoff))
					result.add(ch);
			} catch (Exception ex) {
				result.add(ch);
			}
		}
		return result.toArray(new String[0][]);
	}


	private void applyFilter(String khungGio) {
		String[][] dataToFilter = dangXemChieuVe ? CHUYEN_FULL_VE : CHUYEN_FULL;
		String[][] result;

		if (dataToFilter == null || dataToFilter.length == 0 || dataToFilter[0][0].equals("Không có chuyến")) {
			result = dataToFilter;
		} else {
			List<String[]> filteredList = new ArrayList<>();
			for (String[] ch : dataToFilter) {
				try {
					LocalDateTime tgDi = LocalDateTime.parse(ch[1], fmt);
					if (dangXemChieuVe && selectedArrivalDi != null && tgDi.isBefore(selectedArrivalDi))
						continue;
					boolean match = khungGio.equals("Tất cả");
					if (!match) {
						int hour = tgDi.getHour();
						switch (khungGio) {
						case "Sáng":
							match = (hour >= 4 && hour < 12);
							break;
						case "Trưa":
							match = (hour >= 12 && hour < 14);
							break;
						case "Chiều":
							match = (hour >= 14 && hour < 18);
							break;
						case "Tối":
							match = (hour >= 18 || hour < 4);
							break;
						}
					}
					if (match)
						filteredList.add(ch);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (filteredList.isEmpty()) {
				String ngay = dangXemChieuVe ? ngayVe : ngayDi;
				result = new String[][] { { "Không có chuyến", "--:--", "--:--", ngay, ngay, "" } };
			} else
				result = filteredList.toArray(new String[0][]);
		}

		if (dangXemChieuVe)
			CHUYEN_FILTERED_VE = result;
		else
			CHUYEN_FILTERED = result;

		refreshChuyen();
		refreshToaGhe();
		updateActionBtn();
	}

	private String[] buildToaList(int ci) {
		String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
		if (filtered == null || filtered.length == 0 || filtered[0][0].equals("Không có chuyến") || ci == -1)
			return new String[] { "--" };

		String tenTau = filtered[ci][0];
		List<String> toaList = new ArrayList<>();
		String sql = "SELECT t.maToaTau FROM ToaTau t JOIN Tau tau ON t.maTau = tau.maTau WHERE tau.tenTau = ? ORDER BY t.soToa";
		try (Connection con = Connect_DB.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setNString(1, tenTau);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				toaList.add(rs.getString("maToaTau"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (toaList.isEmpty())
			toaList.add("Toa Mặc Định");
		return toaList.toArray(new String[0]);
	}

	private Set<Integer> gheDaDat(String maChuyen, String maToa) {
		Set<Integer> booked = new LinkedHashSet<>();
		if (maChuyen == null || maToa == null || maChuyen.isEmpty())
			return booked;

		List<String> danhSachGheDaDatCuaChuyen = new VeDAO().layDanhSachMaGheDaDat(maChuyen);

		for (String fullMaGhe : danhSachGheDaDatCuaChuyen) {
			if (fullMaGhe != null && fullMaGhe.endsWith(maToa)) {
				try {
					String soGheStr = fullMaGhe.substring(1, 3);
					booked.add(Integer.parseInt(soGheStr));
				} catch (Exception e) {
				}
			}
		}
		return booked;
	}

	private String getLoaiToa(String maToa) {
		int soToa;
		try {
			soToa = Integer.parseInt(maToa.substring(1, 3));
		} catch (Exception e) {
			soToa = 1;
		}
		// Khớp với SQL: toa 1-4 = GHE_CUNG, toa 5-8 = GIUONG_NAM (VIP), toa 9-12 = GHE_MEM
		return soToa <= 4 ? "Thường (Ghế cứng)" : soToa <= 8 ? "VIP" : "Thường (Ghế mềm)";
	}

	private boolean hasAdjacent(Set<Integer> booked, int k, int maxSeats) {
		if (k <= 1)
			return (maxSeats - booked.size()) >= 1;
		int consecutiveCount = 0;
		for (int i = 1; i <= maxSeats; i++) {
			if (!booked.contains(i)) {
				consecutiveCount++;
				if (consecutiveCount >= k)
					return true;
			} else
				consecutiveCount = 0;
		}
		return false;
	}

	private int getGroup(int empty, boolean adj, boolean enough) {
		if (empty == 0)
			return 4;
		if (enough && adj)
			return 1;
		if (enough)
			return 2;
		return 3;
	}

	private JPanel buildTopBar() {
		JPanel bar = new JPanel(new GridBagLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(210, 215, 224)),
				new EmptyBorder(0, 0, 5, 0)));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;

		JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		info.setBackground(Color.WHITE);

		String[] lbls = { "Ga đi:", "Ga đến:", "Loại vé:", "Ngày đi:", "Ngày về:", "Số lượng:" };
		String[] vals = { gaDi, gaDen, loaiVe, ngayDi, ngayVe, String.valueOf(soLuong) };
		boolean[] isDimmed = { false, false, false, false, motChieu, false };

		for (int i = 0; i < lbls.length; i++)
			info.add(addInfoCell(lbls[i], vals[i], isDimmed[i], 6));

		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		bar.add(info, gbc);

		JPanel rightWrapper = new JPanel();
		rightWrapper.setLayout(new BoxLayout(rightWrapper, BoxLayout.Y_AXIS));
		rightWrapper.setBackground(Color.WHITE);
		rightWrapper.add(Box.createVerticalStrut(19));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(Color.WHITE);

		btnChieuToggle = buildStyledButton("Chiều về", loadAndScaleIcon("/Images/logoKhuhoi.png", 14, 14));
		btnChieuToggle.setPreferredSize(new Dimension(btnChieuToggle.getPreferredSize().width, 28));
		btnChieuToggle.setEnabled(!motChieu);
		btnChieuToggle.setVisible(false); // Ẩn nút khỏi giao diện, logic vẫn giữ nguyên
		btnChieuToggle.addActionListener(e -> {
			if (!dangXemChieuVe)
				switchToChieuVe();
			else
				switchToChieuDi();
		});

		btnLamMoi = buildStyledButton("Làm mới", loadAndScaleIcon("/Images/logoLammoi.png", 14, 14));
		btnLamMoi.setPreferredSize(new Dimension(btnLamMoi.getPreferredSize().width, 28));
		btnLamMoi.addActionListener(e -> {
		    CHUYEN_FULL = chuyenTauDAO.loadChuyenFromDB(gaDi, gaDen, ngayDi);
		    CHUYEN_FULL = filterChuyenDi(CHUYEN_FULL, ngayDi);  // ← THÊM DÒNG NÀY
		    if (CHUYEN_FULL == null || CHUYEN_FULL.length == 0)
		        CHUYEN_FULL = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayDi, ngayDi, "" } };
		    CHUYEN_FILTERED = CHUYEN_FULL;

		    if (!motChieu) {
		        CHUYEN_FULL_VE = chuyenTauDAO.loadChuyenFromDB(gaDen, gaDi, ngayVe);
		        // Không filter theo selectedArrivalDi vì làm mới = reset
		        // nhưng vẫn lọc theo giờ thực nếu là hôm nay:
		        if (selectedArrivalDi != null)
		            CHUYEN_FULL_VE = filterChuyenVe(CHUYEN_FULL_VE, selectedArrivalDi);
		        if (CHUYEN_FULL_VE == null || CHUYEN_FULL_VE.length == 0)
		            CHUYEN_FULL_VE = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayVe, ngayVe, "" } };
		        CHUYEN_FILTERED_VE = CHUYEN_FULL_VE;
		        selectedArrivalDi = null;  // reset sau khi đã dùng
		    }
		    trang = 0;
		    chuyenIdx = -1;
		    activeMaToa = null;
		    gheChon.clear();
		    if (cbKhungGio != null) cbKhungGio.setSelectedIndex(0);
		    refreshChuyen();
		    refreshToaGhe();
		    updateActionBtn();
		    updateGheDaChon();
		});

		buttonPanel.add(btnChieuToggle);
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
		l.setForeground(new Color(110, 115, 125));

		JLabel v = new JLabel(value, SwingConstants.CENTER);
		v.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		v.setOpaque(true);
		v.setBackground(Color.WHITE);
		v.setForeground(dimmed ? new Color(180, 180, 180) : Color.BLACK);
		v.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200), 1), new EmptyBorder(2, 12, 2, 12)));

		cell.add(l, BorderLayout.NORTH);
		cell.add(v, BorderLayout.CENTER);
		return cell;
	}

	private Icon loadAndScaleIcon(String path, int w, int h) {
		try {
			java.net.URL url = getClass().getResource(path);
			if (url != null)
				return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
		} catch (Exception ignored) {
		}
		return null;
	}

	private JButton buildStyledButton(String text, Icon icon) {
		JButton btn = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (!isEnabled())
					g2.setColor(new Color(130, 150, 185));
				else if (getModel().isPressed())
					g2.setColor(NAVY_LIGHT);
				else if (getModel().isRollover())
					g2.setColor(new Color(60, 95, 165));
				else
					g2.setColor(NAVY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(Color.WHITE);
				g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
				FontMetrics fm = g2.getFontMetrics();
				int tw = fm.stringWidth(getText());
				int iw = (getIcon() != null) ? getIcon().getIconWidth() : 0;
				int gap = (getIcon() != null) ? 8 : 0;
				int startX = (getWidth() - (iw + gap + tw)) / 2;
				int centerY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
				if (getIcon() != null) {
					int iconY = (getHeight() - getIcon().getIconHeight()) / 2;
					getIcon().paintIcon(this, g2, startX, iconY);
					g2.drawString(getText(), startX + iw + gap, centerY);
				} else
					g2.drawString(getText(), (getWidth() - tw) / 2, centerY);
				g2.dispose();
			}
		};
		btn.setIcon(icon);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setBorder(new EmptyBorder(3, 15, 3, 15));
		return btn;
	}

	class SmoothScrollPanel extends JPanel implements Scrollable {
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 16;
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return Math.max(visibleRect.height * 2 / 3, 60);
		}

		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}

	private JPanel buildCenter() {
		JPanel c = new JPanel(new BorderLayout(0, 0));
		c.setBackground(BG);

		JPanel topHeaderWrapper = new JPanel(new BorderLayout(0, 0));
		topHeaderWrapper.setBackground(BG);
		topHeaderWrapper.setBorder(new EmptyBorder(5, 10, 5, 10));

		lblChieuHeader = new JLabel("Chiều đi : Ngày " + ngayDi + " từ " + gaDi + " đến " + gaDen);
		lblChieuHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblChieuHeader.setForeground(Color.WHITE);
		lblChieuHeader.setOpaque(true);
		lblChieuHeader.setBackground(NAVY_LIGHT);
		lblChieuHeader.setBorder(new EmptyBorder(6, 12, 6, 12));
		topHeaderWrapper.add(new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
			{
				setOpaque(false);
				add(lblChieuHeader);
			}
		}, BorderLayout.WEST);

		JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlFilter.setOpaque(false);
		JLabel lblFilter = new JLabel("Khung giờ:");
		lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
		cbKhungGio = new JComboBox<>(new String[] { "Tất cả", "Sáng", "Trưa", "Chiều", "Tối" });
		cbKhungGio.setFocusable(false);
		cbKhungGio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		cbKhungGio.setBackground(Color.WHITE);
		cbKhungGio.setPreferredSize(new Dimension(110, 26));
		cbKhungGio.addActionListener(e -> {
			trang = 0;
			chuyenIdx = -1;
			activeMaToa = null;
			gheChon.clear();
			applyFilter((String) cbKhungGio.getSelectedItem());
		});

		pnlFilter.add(lblFilter);
		pnlFilter.add(cbKhungGio);
		topHeaderWrapper.add(new JPanel(new GridBagLayout()) {
			{
				setOpaque(false);
				add(pnlFilter);
			}
		}, BorderLayout.EAST);
		c.add(topHeaderWrapper, BorderLayout.NORTH);

		JPanel body = new JPanel(new BorderLayout(0, 0));
		body.setBackground(BG);
		body.setBorder(new EmptyBorder(0, 10, 0, 10));

		stickyHeaderContainer = new JPanel(new BorderLayout());
		stickyHeaderContainer.setOpaque(false);
		body.add(stickyHeaderContainer, BorderLayout.NORTH);

		pnlScrollContent = new SmoothScrollPanel();
		pnlScrollContent.setLayout(new BoxLayout(pnlScrollContent, BoxLayout.Y_AXIS));
		pnlScrollContent.setBackground(BG);

		pnlChuyenWrapper = buildChuyenRow();
		pnlChuyenWrapper.setMinimumSize(new Dimension(0, 140));
		pnlChuyenWrapper.setPreferredSize(new Dimension(0, 140));
		pnlChuyenWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
		pnlScrollContent.add(pnlChuyenWrapper);

		pnlScrollContent.add(Box.createVerticalStrut(6));

		pnlToaHeaderWrapper = new JPanel(new BorderLayout());
		pnlToaHeaderWrapper.setOpaque(false);
		hdrToa = buildToaHeader();
		pnlToaHeaderWrapper.add(hdrToa, BorderLayout.CENTER);
		pnlScrollContent.add(pnlToaHeaderWrapper);

		pnlAllToas = new JPanel();
		pnlAllToas.setLayout(new BoxLayout(pnlAllToas, BoxLayout.Y_AXIS));
		pnlAllToas.setBackground(BG);
		pnlScrollContent.add(pnlAllToas);

		toaScrollPane = new JScrollPane(pnlScrollContent);
		toaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		toaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		toaScrollPane.setBorder(new LineBorder(BORDER_C, 1));

		toaScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		toaScrollPane.getVerticalScrollBar().setBlockIncrement(120);
		toaScrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);

		final double[] currentPos = { 0 };
		final double[] targetPos = { 0 };
		final boolean[] timerRunning = { false };
		final javax.swing.Timer[] timer = { null };

		Runnable startAnim = () -> {
			if (timerRunning[0])
				return;
			timerRunning[0] = true;
			timer[0] = new javax.swing.Timer(8, null);
			timer[0].addActionListener(tick -> {
				double diff = targetPos[0] - currentPos[0];
				if (Math.abs(diff) < 0.5) {
					currentPos[0] = targetPos[0];
					timerRunning[0] = false;
					timer[0].stop();
					handleScrollEvents();
				} else {
					currentPos[0] += diff * 0.18;
					toaScrollPane.getVerticalScrollBar().setValue((int) Math.round(currentPos[0]));
				}
			});
			timer[0].start();
		};

		toaScrollPane.addMouseWheelListener(e -> {
			e.consume();
			JScrollBar bar = toaScrollPane.getVerticalScrollBar();
			if (!timerRunning[0])
				currentPos[0] = bar.getValue();
			targetPos[0] += e.getUnitsToScroll() * 35;
			targetPos[0] = Math.max(bar.getMinimum(),
					Math.min(bar.getMaximum() - bar.getVisibleAmount(), targetPos[0]));
			startAnim.run();
		});

		toaScrollPane.getViewport().addChangeListener(e -> {
			if (!timerRunning[0])
				handleScrollEvents();
		});

		body.add(toaScrollPane, BorderLayout.CENTER);

		JPanel botLegendArea = new JPanel(new BorderLayout());
		botLegendArea.setBackground(BG);
		botLegendArea.setBorder(new EmptyBorder(6, 0, 4, 0));

		lblGheTrong = new JLabel("Số ghế còn trống: --/--", SwingConstants.CENTER);
		lblGheTrong.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblGheTrong.setForeground(NAVY);

		JPanel legendBoxes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		legendBoxes.setBackground(BG);
		legendBoxes.add(makeLegend(SEAT_SEL, "Đang chọn"));
		legendBoxes.add(makeLegend(SEAT_OK, "Còn trống"));
		legendBoxes.add(makeLegend(SEAT_TAKEN, "Đã đặt"));

		JPanel leftDummy = new JPanel();
		leftDummy.setBackground(BG);
		leftDummy.setPreferredSize(legendBoxes.getPreferredSize());

		botLegendArea.add(leftDummy, BorderLayout.WEST);
		botLegendArea.add(lblGheTrong, BorderLayout.CENTER);
		botLegendArea.add(legendBoxes, BorderLayout.EAST);

		body.add(botLegendArea, BorderLayout.SOUTH);

		c.add(body, BorderLayout.CENTER);
		refreshToaGhe();
		return c;
	}

	private boolean stickyActive = false;

	private void handleScrollEvents() {
		int scrollY = toaScrollPane.getViewport().getViewPosition().y;
		int threshold = pnlChuyenWrapper.getHeight() + 6;
		if (threshold <= 6)
			threshold = 146;

		boolean shouldSticky = scrollY >= threshold;

		if (shouldSticky != stickyActive) {
			stickyActive = shouldSticky;

			if (shouldSticky) {
				Dimension sz = hdrToa.getPreferredSize();
				pnlToaHeaderWrapper.setPreferredSize(sz);
				pnlToaHeaderWrapper.setMinimumSize(sz);
				pnlToaHeaderWrapper.setMaximumSize(sz);

				stickyHeaderContainer.add(hdrToa, BorderLayout.CENTER);
				stickyHeaderContainer.revalidate();
				stickyHeaderContainer.repaint();
			} else {
				pnlToaHeaderWrapper.setPreferredSize(null);
				pnlToaHeaderWrapper.setMinimumSize(null);
				pnlToaHeaderWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

				pnlToaHeaderWrapper.add(hdrToa, BorderLayout.CENTER);
				pnlToaHeaderWrapper.revalidate();
				pnlToaHeaderWrapper.repaint();

				stickyHeaderContainer.removeAll();
				stickyHeaderContainer.revalidate();
				stickyHeaderContainer.repaint();
			}
		}

		updateVisibleToa();
	}

	private JPanel buildToaHeader() {
		JPanel hdr = new JPanel(new BorderLayout());
		hdr.setBackground(BG);
		hdr.setBorder(new EmptyBorder(10, 0, 10, 0));

		JLabel ttl = new JLabel("Danh sách toa tàu");
		ttl.setFont(new Font("Segoe UI", Font.BOLD, 14));
		ttl.setForeground(Color.WHITE);
		ttl.setOpaque(true);
		ttl.setBackground(NAVY_LIGHT);
		ttl.setBorder(new EmptyBorder(6, 12, 6, 12));
		hdr.add(new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
			{
				setOpaque(false);
				add(ttl);
			}
		}, BorderLayout.WEST);

		JPanel rightHdr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		rightHdr.setOpaque(false);
		JLabel lblLoaiToa = new JLabel("Loại toa:");
		lblLoaiToa.setFont(new Font("Segoe UI", Font.BOLD, 13));

		cbLoaiToa = new JComboBox<>(new String[] { "Tất cả", "Thường (Ghế cứng)", "Thường (Ghế mềm)", "VIP" });
		cbLoaiToa.setFocusable(false);
		cbLoaiToa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		cbLoaiToa.setBackground(Color.WHITE);
		cbLoaiToa.addActionListener(e -> {
			toaScrollPane.getVerticalScrollBar().setValue(0);
			refreshToaGhe();
		});

		rightHdr.add(lblLoaiToa);
		rightHdr.add(cbLoaiToa);
		hdr.add(rightHdr, BorderLayout.EAST);
		return hdr;
	}

	private JPanel buildChuyenRow() {
		JPanel row = new JPanel(new BorderLayout(2, 0));
		row.setBackground(BG);
		btnPrev = makeNavArrow("‹");
		btnNext = makeNavArrow("›");
		btnPrev.addActionListener(e -> {
			if (trang > 0) {
				trang--;
				refreshChuyen();
			}
		});
		btnNext.addActionListener(e -> {
			if ((trang + 1) * 5 < CHUYEN_FILTERED.length) {
				trang++;
				refreshChuyen();
			}
		});

		pnlChuyen = new JPanel();
		pnlChuyen.setBackground(BG);
		refreshChuyen();

		row.add(btnPrev, BorderLayout.WEST);
		row.add(pnlChuyen, BorderLayout.CENTER);
		row.add(btnNext, BorderLayout.EAST);
		return row;
	}

	private void refreshChuyen() {
		pnlChuyen.removeAll();
		String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
		if (filtered == null || filtered.length == 0 || filtered[0][0].equals("Không có chuyến")) {
			btnPrev.setVisible(false);
			btnNext.setVisible(false);
			pnlChuyen.setLayout(new GridBagLayout());
			JPanel centerContent = new JPanel(new GridBagLayout());
			centerContent.setOpaque(false);
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
			gc.anchor = GridBagConstraints.CENTER;
			JLabel lblPlaceholder = new JLabel(loadAndScaleIcon("/Images/khongcochuyen.png", 60, 60));
			centerContent.add(lblPlaceholder, gc);
			gc.gridy = 1;
			gc.insets = new Insets(5, 0, 0, 0);
			JLabel lblMessage = new JLabel(
					"<html><div style='text-align: center;'>Rất tiếc, hiện không có chuyến tàu nào phù hợp</div></html>");
			lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 15));
			centerContent.add(lblMessage, gc);
			pnlChuyen.add(centerContent);
		} else {
			btnPrev.setVisible(true);
			btnNext.setVisible(true);
			pnlChuyen.setLayout(new GridLayout(1, 5, 10, 0));
			int from = trang * 5, to = Math.min(from + 5, filtered.length);
			for (int i = from; i < to; i++)
				pnlChuyen.add(buildCard(i));
			for (int i = to - from; i < 5; i++)
				pnlChuyen.add(new JPanel() {
					{
						setOpaque(false);
					}
				});
			btnPrev.setEnabled(trang > 0);
			btnNext.setEnabled((trang + 1) * 5 < filtered.length);
		}
		pnlChuyen.revalidate();
		pnlChuyen.repaint();
	}

	private JPanel buildCard(int ci) {
		boolean sel = (ci == chuyenIdx);
		String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
		String[] ch = filtered[ci];

		JPanel info = new JPanel(new GridLayout(4, 1, 0, 0));
		info.setOpaque(false);
		String[] labels = { "TG đi:", ch[1], "TG đến:", ch[2] };

		Font fontLabelNormal = new Font("Segoe UI", Font.BOLD, 10);
		Font fontLabelData = new Font("Segoe UI", Font.BOLD, 11);

		for (int i = 0; i < labels.length; i++) {
			JLabel l = new JLabel(labels[i]);
			l.setFont((i % 2 == 0) ? fontLabelNormal : fontLabelData);
			l.setForeground((i % 2 == 0) ? new Color(100, 100, 100) : Color.BLACK);
			info.add(l);
		}

		JPanel card = new JPanel(new BorderLayout(0, 0)) {
			private boolean isHover = false;
			{
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						isHover = true;
						repaint();
						setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

					@Override
					public void mouseExited(MouseEvent e) {
						isHover = false;
						repaint();
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}

					@Override
					public void mouseClicked(MouseEvent e) {
						if (!motChieu) {
							try {
								if (!dangXemChieuVe) {
									selectedArrivalDi = LocalDateTime.parse(ch[2], fmt);
									// Lọc lại CHUYEN_FULL_VE theo thời gian đến của chuyến đi
									CHUYEN_FULL_VE = chuyenTauDAO.loadChuyenFromDB(gaDen, gaDi, ngayVe);
									CHUYEN_FULL_VE = filterChuyenVe(CHUYEN_FULL_VE, selectedArrivalDi);
									if (CHUYEN_FULL_VE == null || CHUYEN_FULL_VE.length == 0)
										CHUYEN_FULL_VE = new String[][] {
												{ "Không có chuyến", "--:--", "--:--", ngayVe, ngayVe, "" } };
									CHUYEN_FILTERED_VE = CHUYEN_FULL_VE;
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						chuyenIdx = ci;
						activeMaToa = null;
						gheChon.clear();
						toaScrollPane.getVerticalScrollBar().setValue(0);
						refreshChuyen();
						SwingUtilities.invokeLater(() -> {
							refreshToaGhe();
							updateActionBtn();
						});
					}
				});
			}

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth(), h = getHeight(), trainH = h - 20;
				Color mainColor = sel ? new Color(0, 150, 215)
						: isHover ? new Color(255, 200, 80) : new Color(160, 160, 160);

				g2.setColor(mainColor);
				g2.fillRoundRect(2, 2, w - 4, trainH, 25, 25);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(8, 32, w - 16, info.getPreferredSize().height + 10, 12, 12);
				g2.setColor(Color.WHITE);
				g2.fillOval(w / 4 - 8, trainH - 14, 16, 16);
				g2.fillOval(3 * w / 4 - 8, trainH - 14, 16, 16);

				g2.setColor(Color.BLACK);
				int railY = h - 15;
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawLine(15, railY, 5, h - 2);
				g2.drawLine(w - 15, railY, w - 5, h - 2);
				for (int i = 0; i <= 3; i++) {
					int y = railY + (i * 4);
					g2.drawLine(15 - (i * 2), y, w - 15 + (i * 2), y);
				}
				g2.dispose();
			}
		};
		card.setOpaque(false);

		JLabel badge = new JLabel(ch[5], SwingConstants.CENTER) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				super.paintComponent(g);
				g2.dispose();
			}
		};
		badge.setFont(fontLabelData);
		badge.setBorder(new EmptyBorder(4, 8, 4, 8));

		JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
		badgeWrap.setOpaque(false);
		badgeWrap.add(badge);
		card.add(badgeWrap, BorderLayout.NORTH);

		JPanel infoWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 5));
		infoWrapper.setOpaque(false);
		infoWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
		infoWrapper.add(info);
		card.add(infoWrapper, BorderLayout.CENTER);

		return card;
	}

	private void refreshToaGhe() {
		pnlAllToas.removeAll();
		toaBlocks.clear();
		toaMaToas.clear();
		toaLoaiToas.clear();
		toaLogos.clear();
		currentVisibleToaIndex = -1;
		if (chuyenIdx == -1) {
			lblGheTrong.setText("Số ghế còn trống: --/--");
			pnlAllToas.revalidate();
			pnlAllToas.repaint();
			return;
		}

		String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
		String maChuyen = filtered[chuyenIdx][5];
		String[] toaList = buildToaList(chuyenIdx);
		String filter = (String) cbLoaiToa.getSelectedItem();

		List<String> validToas = new ArrayList<>();
		for (String maToa : toaList) {
			String loai = getLoaiToa(maToa);
			if (!filter.equals("Tất cả") && !loai.equals(filter))
				continue;
			validToas.add(maToa);
		}

		validToas.sort((toa1, toa2) -> {
			String loai1 = getLoaiToa(toa1);
			String loai2 = getLoaiToa(toa2);
			int max1 = loai1.equals("VIP") ? 18 : 28;
			int max2 = loai2.equals("VIP") ? 18 : 28;
			Set<Integer> booked1 = gheDaDat(maChuyen, toa1);
			Set<Integer> booked2 = gheDaDat(maChuyen, toa2);
			boolean adj1 = hasAdjacent(booked1, soLuong, max1);
			boolean adj2 = hasAdjacent(booked2, soLuong, max2);
			int group1 = getGroup(max1 - booked1.size(), adj1, (max1 - booked1.size()) >= soLuong);
			int group2 = getGroup(max2 - booked2.size(), adj2, (max2 - booked2.size()) >= soLuong);

			if (group1 != group2)
				return Integer.compare(group1, group2);
			int num1 = Integer.parseInt(toa1.substring(1, 3));
			int num2 = Integer.parseInt(toa2.substring(1, 3));
			return Integer.compare(num1, num2);
		});

		for (String maToa : validToas) {
			String loai = getLoaiToa(maToa);
			JPanel block = buildSingleToaBlock(maChuyen, maToa, loai);
			toaBlocks.add(block);
			toaMaToas.add(maToa);
			toaLoaiToas.add(loai);
			pnlAllToas.add(block);
		}

		pnlAllToas.setBorder(new EmptyBorder(10, 0, 200, 0));
		pnlAllToas.revalidate();
		pnlAllToas.repaint();
		SwingUtilities.invokeLater(this::updateVisibleToa);
	}

	private JPanel buildSingleToaBlock(String maChuyen, String maToa, String loaiToa) {
		JPanel wrapper = new JPanel(new BorderLayout(0, 0)) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
			}
		};
		wrapper.setBackground(BG);
		wrapper.setBorder(new EmptyBorder(15, 20, 15, 20));

		JPanel hdr = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		hdr.setOpaque(false);
		hdr.setBorder(new EmptyBorder(0, 0, 5, 0));

		JLabel lblLogo = new JLabel();
		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
		boolean isVip = loaiToa.equals("VIP");
		Icon icon = loadAndScaleIcon(isVip ? "/Images/logoToaVIP.png" : "/Images/logoToaThuong.png", 56, 36);
		if (icon != null)
			lblLogo.setIcon(icon);
		else {
			lblLogo.setText(isVip ? "[VIP]" : "[Thường]");
			lblLogo.setBorder(new LineBorder(Color.GRAY));
		}
		toaLogos.add(lblLogo);
		String prefix = isVip ? "Giường nằm: " : (loaiToa.contains("cứng") ? "Ghế cứng: " : "Ghế mềm: ");
		JLabel lblTitle = new JLabel(prefix + maToa);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
		lblTitle.setForeground(NAVY);

		JPanel activePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6)) {
			@Override
			protected void paintComponent(Graphics g) {
				if (activeMaToa != null && activeMaToa.equals(maToa)) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(new Color(220, 235, 255));
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
					g2.setColor(NAVY_SEL);
					g2.setStroke(new BasicStroke(2f));
					g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
					g2.dispose();
				}
				super.paintComponent(g);
			}
		};
		activePanel.setOpaque(false);
		activePanel.add(lblLogo);
		activePanel.add(lblTitle);
		hdr.add(activePanel);
		wrapper.add(hdr, BorderLayout.NORTH);

		int maxSeats = isVip ? 18 : 28;
		int cols = maxSeats / 2;

		JPanel grid = new JPanel(new GridLayout(2, cols, 6, 6)) {
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
		grid.setBackground(Color.WHITE);
		grid.setBorder(new EmptyBorder(12, 15, 12, 15));
		final String maToaFinal = maToa;
		Set<Integer> dadat = gheDaDat(maChuyen, maToa);
		for (int i = 1; i <= maxSeats; i++) {
			int gNum = i;
			boolean taken = dadat.contains(gNum);
			String seatId = String.format("G%02d%s", gNum, maToa);

			JButton btn = new JButton(String.valueOf(i)) {
				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					Color bg = taken ? SEAT_TAKEN : gheChon.contains(seatId) ? SEAT_SEL : SEAT_OK;
					g2.setColor(bg);
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
					g2.setColor(Color.WHITE);
					g2.setFont(SEAT_FONT);
					FontMetrics fm = g2.getFontMetrics(SEAT_FONT);
					g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
							(getHeight() + fm.getAscent() - fm.getDescent()) / 2);
					g2.dispose();
				}
			};
			btn.setPreferredSize(new Dimension(65, 45));
			btn.setContentAreaFilled(false);
			btn.setBorderPainted(false);
			btn.setFocusPainted(false);

			if (!taken) {
				btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
				btn.addActionListener(ev -> {
				    if (gheChon.contains(seatId))
				        gheChon.remove(seatId);
				    else {
				        if (gheChon.size() >= soLuong) {
				            // Xóa ghế đầu tiên (cũ nhất) trong queue
				            String oldest = gheChon.iterator().next();
				            gheChon.remove(oldest);
				        }
				        gheChon.add(seatId);
				    }

					if (!maToaFinal.equals(activeMaToa)) {
						int idx = toaMaToas.indexOf(maToaFinal);
						if (idx >= 0) {
							currentVisibleToaIndex = idx;
							activeMaToa = maToaFinal;
							manualToaSelected = true;
							String loai = toaLoaiToas.get(idx);
							String[][] cur = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
							String maChuyenHienTai = (chuyenIdx >= 0 && cur != null && chuyenIdx < cur.length)
									? cur[chuyenIdx][5]
									: "";

							Set<Integer> dadatInner = gheDaDat(maChuyenHienTai, maToaFinal);
							int maxSeatsInner = loai.equals("VIP") ? 18 : 28;
							int trong = 0;
							for (int g = 1; g <= maxSeatsInner; g++)
								if (!dadatInner.contains(g))
									trong++;
							lblGheTrong.setText("Số ghế còn trống: " + trong + "/" + maxSeatsInner);
							updateVisibleToa();
						}
					}
					SwingUtilities.invokeLater(() -> pnlAllToas.repaint());
					updateGheDaChon();
					updateActionBtn();
				});
			}
			grid.add(btn);
		}

		JPanel gridCenterWrapper = new JPanel(new GridBagLayout());
		gridCenterWrapper.setOpaque(false);
		gridCenterWrapper.setBorder(new EmptyBorder(0, 0, 10, 0));
		gridCenterWrapper.add(grid);
		wrapper.add(gridCenterWrapper, BorderLayout.CENTER);
		return wrapper;
	}

	private void updateVisibleToa() {
		if (toaBlocks.isEmpty() || toaLogos.isEmpty())
			return;

		JViewport viewport = toaScrollPane.getViewport();
		Rectangle viewRect = viewport.getViewRect();
		int centerY = viewRect.y + viewRect.height / 2;

		int closestIndex = -1;
		int minDiff = Integer.MAX_VALUE;
		for (int i = 0; i < toaBlocks.size(); i++) {
			JPanel block = toaBlocks.get(i);
			int blockCenterY = pnlAllToas.getY() + block.getY() + block.getHeight() / 2;
			int diff = Math.abs(blockCenterY - centerY);
			if (diff < minDiff) {
				minDiff = diff;
				closestIndex = i;
			}
		}

		if (closestIndex == -1)
			return;

		if (manualToaSelected) {
			manualToaSelected = false;
			int manualIdx = toaMaToas.indexOf(activeMaToa);
			if (manualIdx >= 0)
				closestIndex = manualIdx;
		} else {
			if (closestIndex == currentVisibleToaIndex)
				return;
			currentVisibleToaIndex = closestIndex;
			activeMaToa = toaMaToas.get(closestIndex);
		}

		for (int i = 0; i < toaBlocks.size(); i++) {
			JPanel block = toaBlocks.get(i);
			Component hdrComp = ((BorderLayout) block.getLayout()).getLayoutComponent(BorderLayout.NORTH);
			if (!(hdrComp instanceof JPanel))
				continue;
			JPanel hdrPanel = (JPanel) hdrComp;
			for (Component c : hdrPanel.getComponents()) {
				if (!(c instanceof JPanel))
					continue;
				JPanel ap = (JPanel) c;
				ap.repaint();
				for (Component inner : ap.getComponents()) {
					if (!(inner instanceof JLabel))
						continue;
					JLabel lbl = (JLabel) inner;
					if (lbl.getIcon() != null)
						continue;
					String rawText = lbl.getText().replace("", "");
					if (i == closestIndex) {
						lbl.setText("" + rawText);
						lbl.setForeground(new Color(0, 100, 200));
						lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
					} else {
						lbl.setText(rawText);
						lbl.setForeground(NAVY);
						lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
					}
				}
			}
		}

		String activeLoai = toaLoaiToas.get(closestIndex);
		String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
		if (chuyenIdx < 0 || filtered == null || chuyenIdx >= filtered.length)
			return;
		Set<Integer> dadat = gheDaDat(filtered[chuyenIdx][5], activeMaToa);
		int maxSeats = activeLoai.equals("VIP") ? 18 : 28;
		int trong = 0;
		for (int g = 1; g <= maxSeats; g++)
			if (!dadat.contains(g))
				trong++;
		lblGheTrong.setText("Số ghế còn trống: " + trong + "/" + maxSeats);
	}

	private void switchToChieuVe() {
		trangDi = trang;
		chuyenIdxDi = chuyenIdx;
		gheChonDi.clear();
		gheChonDi.addAll(gheChon);
		dangXemChieuVe = true;
		applyFilter(cbKhungGio.getSelectedItem().toString());
		trang = trangVe;
		chuyenIdx = chuyenIdxVe;
		gheChon.clear();
		gheChon.addAll(gheChonVe);
		lblChieuHeader.setText("Chiều về : Ngày " + ngayVe + " từ " + gaDen + " đến " + gaDi);
		btnChieuToggle.setText("Chiều đi");
		refreshChuyen();
		refreshToaGhe();
		updateActionBtn();
		updateGheDaChon();
	}

	private void switchToChieuDi() {
		trangVe = trang;
		chuyenIdxVe = chuyenIdx;
		gheChonVe.clear();
		gheChonVe.addAll(gheChon);
		dangXemChieuVe = false;
		trang = trangDi;
		chuyenIdx = chuyenIdxDi;
		gheChon.clear();
		gheChon.addAll(gheChonDi);
		lblChieuHeader.setText("Chiều đi : Ngày " + ngayDi + " từ " + gaDi + " đến " + gaDen);
		btnChieuToggle.setText("Chiều về");
		refreshChuyen();
		refreshToaGhe();
		updateActionBtn();
		updateGheDaChon();
	}

	// ĐÃ SỬA: Hàm này đón thêm danh sách Mã Chuyến
	private void chuyenSangGUI2(List<String> listGheChon, List<String> listMaChuyen, List<String> listThoiGian, String gaDi, String gaDen) {
		DatVeGUI2 gui2 = new DatVeGUI2(listGheChon, listMaChuyen, listThoiGian, gaDi, gaDen, loaiVe, () -> {
			Container parent = getParent();
			if (parent != null) {
				LayoutManager lm = parent.getLayout();
				if (lm instanceof CardLayout) {
					((CardLayout) lm).show(parent, "datveGUI_next");
				} else {
					for (Component c : parent.getComponents())
						if (c instanceof DatVeGUI2)
							parent.remove(c);
					parent.add(this, BorderLayout.CENTER);
					parent.revalidate();
					parent.repaint();
				}
			}
		});

		Container parent = getParent();
		if (parent != null) {
			LayoutManager lm = parent.getLayout();
			if (lm instanceof CardLayout) {
				parent.add(gui2, "datveGUI2_thongtin");
				((CardLayout) lm).show(parent, "datveGUI2_thongtin");
			} else {
				parent.remove(this);
				parent.add(gui2, BorderLayout.CENTER);
				parent.revalidate();
				parent.repaint();
			}
		}
	}

	private JPanel buildBotBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

		JButton btnQuayLai = makeOutlineBtn("Quay lại", loadAndScaleIcon("/Images/logoBack.png", 14, 14));
		btnQuayLai.addActionListener(e -> {
			if (!motChieu && dangXemChieuVe)
				switchToChieuDi();
			else if (onQuayLai != null)
				onQuayLai.run();
		});

		lblGheDaChon = new JLabel("  Số vé đã chọn: 0/" + soLuong);
		lblGheDaChon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblGheDaChon.setForeground(new Color(50, 70, 110));

		btnHuy = makeRedBtn("Hủy", loadAndScaleIcon("/Images/logoThungRac.png", 14, 14));
		btnHuy.addActionListener(e -> {
			gheChon.clear();
			updateGheDaChon();
			updateActionBtn();
			pnlAllToas.repaint();
			updateVisibleToa();
		});

		btnAction = makeNavyBtn("Chọn nhanh", loadAndScaleIcon("/Images/logoGhe.png", 14, 14));
		btnAction.addActionListener(e -> {
			if (gheChon.size() >= soLuong) {
				if (!motChieu && !dangXemChieuVe)
					switchToChieuVe();
				else {
					if (!motChieu && gheChonDi.size() < soLuong) {
						JOptionPane.showMessageDialog(this, "Bạn chưa chọn đủ vé chiều đi!", "Cảnh báo",
								JOptionPane.WARNING_MESSAGE);
						switchToChieuDi();
						return;
					}

					// Thay thế đoạn tạo list ở btnAction bằng đoạn này:
					List<String> listGhe = new ArrayList<>();
					List<String> listMaChuyen = new ArrayList<>();
					List<String> listThoiGian = new ArrayList<>(); // Thêm list thời gian

					if (motChieu) {
						listGhe.addAll(gheChon);
						String maC = CHUYEN_FILTERED[chuyenIdx][5];
						String tgDi = CHUYEN_FILTERED[chuyenIdx][1]; // Cột 1 là thời gian đi
						for (int i = 0; i < gheChon.size(); i++) {
							listMaChuyen.add(maC);
							listThoiGian.add(tgDi);
						}
					} else {
						// Chiều đi
						listGhe.addAll(gheChonDi);
						String maCDi = CHUYEN_FILTERED[chuyenIdxDi][5];
						String tgDi = CHUYEN_FILTERED[chuyenIdxDi][1];
						for (int i = 0; i < gheChonDi.size(); i++) {
							listMaChuyen.add(maCDi);
							listThoiGian.add(tgDi);
						}
						// Chiều về
						gheChonVe.clear();
						gheChonVe.addAll(gheChon);
						listGhe.addAll(gheChonVe);
						String maCVe = CHUYEN_FILTERED_VE[chuyenIdx][5];
						String tgVe = CHUYEN_FILTERED_VE[chuyenIdx][1];
						for (int i = 0; i < gheChonVe.size(); i++) {
							listMaChuyen.add(maCVe);
							listThoiGian.add(tgVe);
						}
					}
					// Chuyền đầy đủ dữ liệu sang GUI 2
					chuyenSangGUI2(listGhe, listMaChuyen, listThoiGian, gaDi, gaDen);
				}
			} else {
				String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
				if (chuyenIdx < 0 || chuyenIdx >= filtered.length)
					return;
				String maChuyen = filtered[chuyenIdx][5];
				gheChon.clear();
				for (int i = 0; i < toaMaToas.size(); i++) {
					if (gheChon.size() >= soLuong)
						break;
					String maToa = toaMaToas.get(i);
					String loaiToa = toaLoaiToas.get(i);
					int maxSeats = loaiToa.equals("VIP") ? 18 : 28;
					Set<Integer> dadat = gheDaDat(maChuyen, maToa);
					for (int g = 1; g <= maxSeats; g++) {
						if (gheChon.size() >= soLuong)
							break;
						String seatId = String.format("G%02d%s", g, maToa);
						if (!dadat.contains(g))
							gheChon.add(seatId);
					}
				}
				pnlAllToas.repaint();
				updateGheDaChon();
				updateActionBtn();
				updateVisibleToa();
			}
		});

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
		left.setBackground(Color.WHITE);
		left.add(btnQuayLai);
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
		right.setBackground(Color.WHITE);
		right.add(lblGheDaChon);
		right.add(btnHuy);
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
			btnAction.setText("Tiếp tục");
			btnAction.setIcon(loadAndScaleIcon("/Images/logoGoOn.png", 14, 14));
			btnAction.setHorizontalTextPosition(SwingConstants.LEFT);
		} else {
			btnAction.setText("Chọn nhanh");
			btnAction.setIcon(loadAndScaleIcon("/Images/logoGhe.png", 14, 14));
			btnAction.setHorizontalTextPosition(SwingConstants.RIGHT);
		}
		btnAction.revalidate();
		btnAction.repaint();
	}

	private JButton makeNavArrow(String t) {
		JButton b = new JButton(t) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(isEnabled() ? new Color(215, 228, 248) : new Color(238, 240, 246));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2.setColor(isEnabled() ? NAVY : new Color(175, 185, 205));
				g2.setFont(ARROW_FONT);
				FontMetrics fm = g2.getFontMetrics(ARROW_FONT);
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

	private JButton makeOutlineBtn(String text, Icon icon) {
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
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setIcon(icon);
		b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		b.setForeground(NAVY);
		b.setIconTextGap(8);
		b.setBorder(new EmptyBorder(6, 16, 6, 16));
		b.setContentAreaFilled(false);
		b.setFocusPainted(false);
		b.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setIcon(icon);
		b.setFont(new Font("Segoe UI", Font.BOLD, 14));
		b.setForeground(Color.WHITE);
		b.setIconTextGap(8);
		b.setBorder(new EmptyBorder(6, 16, 6, 16));
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
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
				g2.setColor(getModel().isPressed() ? new Color(180, 20, 30)
						: getModel().isRollover() ? new Color(220, 40, 50) : new Color(210, 30, 40));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setIcon(icon);
		b.setFont(new Font("Segoe UI", Font.BOLD, 14));
		b.setForeground(Color.WHITE);
		b.setIconTextGap(8);
		b.setBorder(new EmptyBorder(6, 16, 6, 16));
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
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
				g2.dispose();
			}
		};
		box.setPreferredSize(new Dimension(16, 16));
		box.setOpaque(false);
		JLabel l = new JLabel(txt);
		l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		l.setForeground(Color.BLACK);
		p.add(box);
		p.add(l);
		return p;
	}
}