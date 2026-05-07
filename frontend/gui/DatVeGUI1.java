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

import connect_DB.Connect_DB;

public class DatVeGUI1 extends JPanel {

	private static final Color NAVY = new Color(28, 57, 110);
	private static final Color NAVY_LIGHT = new Color(44, 82, 150);
	private static final Color NAVY_SEL = new Color(100, 160, 230); 
	private static final Color BG = new Color(242, 247, 252);
	private static final Color BORDER_C = new Color(180, 205, 230);
	private static final Color SEAT_OK = new Color(28, 57, 110); 
	private static final Color SEAT_TAKEN = new Color(180, 190, 210); 
	private static final Color SEAT_SEL = new Color(140, 185, 255); 

	private final String gaDi, gaDen, loaiVe, ngayDi, ngayVe;
	private final int soLuong;
	private final boolean motChieu;
	private final Runnable onQuayLai;

	private String[][] CHUYEN_FULL;
	private String[][] CHUYEN_FILTERED;

	// Dữ liệu riêng cho chiều về
	private String[][] CHUYEN_FULL_VE;
	private String[][] CHUYEN_FILTERED_VE;

	// Trạng thái đang xem chiều đi hay chiều về
	private boolean dangXemChieuVe = false;

	// Lưu trạng thái riêng cho mỗi chiều
	private int trangDi = 0, chuyenIdxDi = -1, toaIdxDi = -1;
	private final Set<Integer> gheChonDi = new LinkedHashSet<>();
	private int trangVe = 0, chuyenIdxVe = -1, toaIdxVe = -1;
	private final Set<Integer> gheChonVe = new LinkedHashSet<>();

	private int trang = 0;
	private int chuyenIdx = -1;
	private int toaIdx = -1;
	private final Set<Integer> gheChon = new LinkedHashSet<>();

	private JPanel pnlChuyen;
	private JPanel pnlToaScroll;
	private JPanel pnlGhe;
	private JLabel lblTenToa, lblGheTrong, lblGheDaChon;
	private JButton btnPrev, btnNext, btnAction;
	
	private JPanel pnlToaContainer; 
	private JPanel pnlGheContainer;
	
	// Label tiêu đề chiều đi/về ở center
	private JLabel lblChieuHeader;
	// Nút toggle chiều đi/về
	private JButton btnChieuToggle;
	
	// Đưa ComboBox ra global để nút "Làm mới" có thể reset nó
	private JComboBox<String> cbKhungGio;

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

		CHUYEN_FULL = loadChuyenFromDB(gaDi, gaDen, ngayDi);

		if (CHUYEN_FULL == null || CHUYEN_FULL.length == 0) {
			CHUYEN_FULL = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayDi, ngayDi, "" } };
		}
		
		CHUYEN_FILTERED = CHUYEN_FULL;

		// Load dữ liệu chiều về (chỉ khi khứ hồi)
		if (!motChieu && ngayVe != null && !ngayVe.isEmpty()) {
			CHUYEN_FULL_VE = loadChuyenFromDB(gaDen, gaDi, ngayVe);
			if (CHUYEN_FULL_VE == null || CHUYEN_FULL_VE.length == 0) {
				CHUYEN_FULL_VE = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayVe, ngayVe, "" } };
			}
		} else {
			CHUYEN_FULL_VE = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayVe, ngayVe, "" } };
		}
		CHUYEN_FILTERED_VE = CHUYEN_FULL_VE; 

		setLayout(new BorderLayout());
		setBackground(BG);
		add(buildTopBar(), BorderLayout.NORTH);
		add(buildCenter(), BorderLayout.CENTER);
		add(buildBotBar(), BorderLayout.SOUTH);
	}

	private String[][] loadChuyenFromDB(String tenGaDi, String tenGaDen, String ngayDiStr) {
		List<String[]> list = new ArrayList<>();
		
		String sql = "SELECT c.maChuyen, t.tenTau, c.thoiGianKhoiHanh, c.thoiGianDuKien " +
					 "FROM ChuyenTau c " +
					 "JOIN Tau t ON c.maTau = t.maTau " +
					 "JOIN Ga gDi ON c.gaDi = gDi.maGa " +
					 "JOIN Ga gDen ON c.gaDen = gDen.maGa " +
					 "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? " +
					 "AND CONVERT(VARCHAR, c.thoiGianKhoiHanh, 103) = ? ORDER BY c.thoiGianKhoiHanh ASC";

		try (Connection con = Connect_DB.getInstance().getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			 
			ps.setNString(1, "%" + tenGaDi + "%");
			ps.setNString(2, "%" + tenGaDen + "%");
			ps.setString(3, ngayDiStr);
			
			ResultSet rs = ps.executeQuery();
			
			DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
			DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

			while (rs.next()) {
				LocalDateTime tgDi = rs.getTimestamp("thoiGianKhoiHanh").toLocalDateTime();
				LocalDateTime tgDen = rs.getTimestamp("thoiGianDuKien").toLocalDateTime();
				
				list.add(new String[]{ 
					rs.getString("tenTau"),     
					tgDi.format(dateTimeFmt),   
					tgDen.format(dateTimeFmt),  
					tgDi.format(dateFmt),       
					tgDen.format(dateFmt),      
					rs.getString("maChuyen")    
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list.toArray(new String[0][]);
	}
	
	private void applyFilter(String khungGio) {
		String[][] currentFull = dangXemChieuVe ? CHUYEN_FULL_VE : CHUYEN_FULL;
		String currentNgay = dangXemChieuVe ? ngayVe : ngayDi;

	    if (currentFull == null || currentFull.length == 0 || currentFull[0][0].equals("Không có chuyến")) {
	        if (dangXemChieuVe) CHUYEN_FILTERED_VE = currentFull;
	        else CHUYEN_FILTERED = currentFull;
	    } else if (khungGio.equals("Tất cả")) {
	        if (dangXemChieuVe) CHUYEN_FILTERED_VE = currentFull;
	        else CHUYEN_FILTERED = currentFull;
	    } else {
	        List<String[]> filteredList = new ArrayList<>();
	        for (String[] ch : currentFull) {
	            try {
	                String timeStr = ch[1];
	                int hour = Integer.parseInt(timeStr.substring(11, 13));
	                boolean match = false;
	                
	                switch (khungGio) {
	                    case "Sáng":  match = (hour >= 4 && hour < 12); break;   
	                    case "Trưa":  match = (hour >= 12 && hour < 14); break;  
	                    case "Chiều": match = (hour >= 14 && hour < 18); break;  
	                    case "Tối":   match = (hour >= 18 || hour < 4); break;   
	                }
	                if (match) filteredList.add(ch);
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
	        
	        if (filteredList.isEmpty()) {
	        	String[][] empty = new String[][] { { "Không có chuyến", "--:--", "--:--", currentNgay, currentNgay, "" } };
	            if (dangXemChieuVe) CHUYEN_FILTERED_VE = empty;
	            else CHUYEN_FILTERED = empty;
	        } else {
	        	String[][] arr = filteredList.toArray(new String[0][]);
	            if (dangXemChieuVe) CHUYEN_FILTERED_VE = arr;
	            else CHUYEN_FILTERED = arr;
	        }
	    }
	    
	    trang = 0;
	    chuyenIdx = -1;
	    toaIdx = -1;
	    gheChon.clear();
	    
	    refreshChuyen();
	    refreshToaGhe();
	    updateActionBtn();
	}

	private String[] buildToaList(int ci) {
		String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
		if (filtered == null || filtered.length == 0 || filtered[0][0].equals("Không có chuyến") || ci == -1) return new String[]{"--"};
		
		String tenTau = filtered[ci][0];
		List<String> toaList = new ArrayList<>();
		String sql = "SELECT t.maToaTau FROM ToaTau t JOIN Tau tau ON t.maTau = tau.maTau WHERE tau.tenTau = ? ORDER BY t.soToa";

		try (Connection con = Connect_DB.getInstance().getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setNString(1, tenTau);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				toaList.add(rs.getString("maToaTau"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (toaList.isEmpty()) toaList.add("Toa Mặc Định");
		return toaList.toArray(new String[0]);
	}

	private Set<Integer> gheDaDat(int ci, int ti) {
		Set<Integer> booked = new LinkedHashSet<>();
		String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;
		if (filtered == null || filtered.length == 0 || filtered[0][0].equals("Không có chuyến") || ci == -1 || ti == -1) return booked;

		String[] toaList = buildToaList(ci);
		if (toaList.length == 0 || ti >= toaList.length) return booked;

		String maChuyen = filtered[ci].length > 5 ? filtered[ci][5] : "";
		String maToa = toaList[ti];

		String sql = "SELECT g.maGhe FROM Ve v " +
					 "JOIN Ghe g ON v.maGhe = g.maGhe " +
					 "WHERE v.maChuyen = ? AND g.maToaTau = ? " +
					 "AND v.trangThaiVe IN ('DA_THANH_TOAN', 'CHO_THANH_TOAN')";

		try (Connection con = Connect_DB.getInstance().getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, maChuyen);
			ps.setString(2, maToa);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String maGhe = rs.getString("maGhe");
				String soGheStr = maGhe.replaceAll("\\D+", ""); 
				if (!soGheStr.isEmpty()) {
					booked.add(Integer.parseInt(soGheStr));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return booked;
	}

	private JPanel buildTopBar() {
	    JPanel bar = new JPanel(new GridBagLayout());
	    bar.setBackground(Color.WHITE);
	    
	    bar.setBorder(BorderFactory.createCompoundBorder(
	        new MatteBorder(0, 0, 1, 0, new Color(210, 215, 224)),
	        new EmptyBorder(0, 0, 5, 0) 
	    ));

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.VERTICAL;

	    JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    info.setBackground(Color.WHITE);

	    String[] lbls = { "Ga đi:", "Ga đến:", "Loại vé:", "Ngày đi:", "Ngày về:", "Số lượng:" };
	    String[] vals = { gaDi, gaDen, loaiVe, ngayDi, ngayVe, String.valueOf(soLuong) };
	    boolean[] isDimmed = { false, false, false, false, motChieu, false };

	    for (int i = 0; i < lbls.length; i++) {
	        info.add(addInfoCell(lbls[i], vals[i], isDimmed[i], 6));
	    }

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
	    buttonPanel.setBorder(null);

	    Icon iconChieuVe = loadAndScaleIcon("/Images/logoKhuhoi.png", 14, 14);
	    Icon iconLamMoi = loadAndScaleIcon("/Images/logoLammoi.png", 14, 14);

	    btnChieuToggle = buildStyledButton("Chiều về", iconChieuVe);
	    btnChieuToggle.setPreferredSize(new Dimension(btnChieuToggle.getPreferredSize().width, 28));
	    btnChieuToggle.setEnabled(!motChieu);

	    JButton btnLamMoi = buildStyledButton("Làm mới", iconLamMoi);
	    btnLamMoi.setPreferredSize(new Dimension(btnLamMoi.getPreferredSize().width, 28));
	    
	    // Nút toggle Chiều về / Chiều đi
	    btnChieuToggle.addActionListener(e -> {
	    	if (!dangXemChieuVe) {
	    		switchToChieuVe();
	    	} else {
	    		switchToChieuDi();
	    	}
	    });
	    
	    // CẬP NHẬT: Nút Làm mới giờ sẽ cập nhật CSDL và reset trang
	    btnLamMoi.addActionListener(e -> { 
	    	if (!dangXemChieuVe) {
		    	CHUYEN_FULL = loadChuyenFromDB(gaDi, gaDen, ngayDi);
				if (CHUYEN_FULL == null || CHUYEN_FULL.length == 0) {
					CHUYEN_FULL = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayDi, ngayDi, "" } };
				}
	    	} else {
		    	CHUYEN_FULL_VE = loadChuyenFromDB(gaDen, gaDi, ngayVe);
				if (CHUYEN_FULL_VE == null || CHUYEN_FULL_VE.length == 0) {
					CHUYEN_FULL_VE = new String[][] { { "Không có chuyến", "--:--", "--:--", ngayVe, ngayVe, "" } };
				}
	    	}
			if (cbKhungGio != null) {
				cbKhungGio.setSelectedIndex(0); // Trigger applyFilter("Tất cả")
			}
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

	    if (dimmed) {
	        v.setForeground(new Color(180, 180, 180));
	    } else {
	        v.setForeground(Color.BLACK);
	    }

	    v.setBorder(new CompoundBorder(
	        new LineBorder(new Color(200, 200, 200), 1), 
	        new EmptyBorder(2, 12, 2, 12)
	    ));

	    cell.add(l, BorderLayout.NORTH);
	    cell.add(v, BorderLayout.CENTER);
	    return cell;
	}

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
	                g2.setColor(NAVY_LIGHT);
	            } else if (getModel().isRollover()) {
	                g2.setColor(new Color(60, 95, 165));
	            } else {
	                g2.setColor(NAVY);
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
	    
	    btn.setBorder(new EmptyBorder(3, 15, 3, 15)); 
	    return btn;
	}

	private JPanel buildCenter() {
	    JPanel c = new JPanel(new BorderLayout(0, 0));
	    c.setBackground(BG);

	    JPanel headerWrapper = new JPanel(new BorderLayout(0, 0));
	    headerWrapper.setBackground(BG);
	    headerWrapper.setBorder(new EmptyBorder(5, 0, 5, 0)); 

	    String textHeader = "Chiều đi : Ngày " + ngayDi + " từ " + gaDi + " đến " + gaDen;
	    lblChieuHeader = new JLabel(textHeader);
	    lblChieuHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
	    lblChieuHeader.setForeground(Color.WHITE);
	    lblChieuHeader.setOpaque(true);
	    lblChieuHeader.setBackground(NAVY_LIGHT);
	    lblChieuHeader.setBorder(new EmptyBorder(6, 12, 6, 12));

	    JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    pnlTitle.setOpaque(false);
	    pnlTitle.add(lblChieuHeader);
	    headerWrapper.add(pnlTitle, BorderLayout.WEST);

	    JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
	    pnlFilter.setOpaque(false);
	    
	    JLabel lblFilter = new JLabel("Khung giờ:");
	    lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
	    lblFilter.setForeground(new Color(50, 50, 50));
	    
	    cbKhungGio = new JComboBox<>(new String[]{"Tất cả", "Sáng", "Trưa", "Chiều", "Tối"});
	    cbKhungGio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
	    cbKhungGio.setBackground(Color.WHITE);
	    cbKhungGio.setPreferredSize(new Dimension(110, 26));
	    cbKhungGio.addActionListener(e -> applyFilter((String) cbKhungGio.getSelectedItem()));
	    
	    pnlFilter.add(lblFilter);
	    pnlFilter.add(cbKhungGio);

	    JPanel rightAlignWrapper = new JPanel(new GridBagLayout());
	    rightAlignWrapper.setOpaque(false);
	    rightAlignWrapper.add(pnlFilter);
	    headerWrapper.add(rightAlignWrapper, BorderLayout.EAST);

	    c.add(headerWrapper, BorderLayout.NORTH);

	    JPanel body = new JPanel(new BorderLayout(0, 6));
	    body.setBackground(BG);
	    body.setBorder(new EmptyBorder(8, 10, 4, 10));

	    JPanel rowContainer = buildChuyenRow();
	    rowContainer.setPreferredSize(new Dimension(0, 140)); 

	    body.add(rowContainer, BorderLayout.NORTH);
	    body.add(buildToaGheSection(), BorderLayout.CENTER);

	    c.add(body, BorderLayout.CENTER);
	    return c;
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
		String currentNgay = dangXemChieuVe ? ngayVe : ngayDi;
		
		// NẾU KHÔNG CÓ CHUYẾN -> ĐỔI LAYOUT HIỂN THỊ LỖI BÊN TRONG BẢNG TÀU
		if (filtered == null || filtered.length == 0 || filtered[0][0].equals("Không có chuyến")) {
			btnPrev.setVisible(false);
			btnNext.setVisible(false);
			
			pnlChuyen.setLayout(new GridBagLayout());
			JPanel centerContent = new JPanel(new GridBagLayout());
			centerContent.setOpaque(false);
			
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.CENTER;

			JLabel lblPlaceholder = new JLabel();
			lblPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
			lblPlaceholder.setPreferredSize(new Dimension(60, 60)); 
			Icon iconKhongChuyen = loadAndScaleIcon("/Images/khongcochuyen.png", 60, 60);
			if (iconKhongChuyen != null) lblPlaceholder.setIcon(iconKhongChuyen);
			centerContent.add(lblPlaceholder, gc);

			gc.gridy = 1;
			gc.insets = new Insets(5, 0, 0, 0);
			String msg = "<html><div style='text-align: center;'>Rất tiếc, hiện không có chuyến tàu nào phù hợp<br>trong thời gian được chọn</div></html>";
			JLabel lblMessage = new JLabel(msg);
			lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 15));
			lblMessage.setForeground(new Color(30, 30, 30));
			centerContent.add(lblMessage, gc);

			pnlChuyen.add(centerContent);
		} 
		// CÓ CHUYẾN -> HIỂN THỊ CÁC THẺ TÀU
		else {
			btnPrev.setVisible(true);
			btnNext.setVisible(true);
			
			pnlChuyen.setLayout(new GridLayout(1, 5, 10, 0));
			int from = trang * 5, to = Math.min(from + 5, filtered.length);
			for (int i = from; i < to; i++)
				pnlChuyen.add(buildCard(i));
			for (int i = to - from; i < 5; i++) {
				JPanel empty = new JPanel();
				empty.setOpaque(false);
				pnlChuyen.add(empty);
			}
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
	    String[] labels = {"TG đi:", ch[1], "TG đến:", ch[2]};
	    for (int i = 0; i < labels.length; i++) {
	        JLabel l = new JLabel(labels[i]);
	        
	        l.setFont(new Font("Segoe UI", Font.BOLD, (i % 2 == 0) ? 10 : 11));
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
	                    chuyenIdx = ci; toaIdx = -1; gheChon.clear();
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

	            g2.setColor(mainColor);
	            g2.fillRoundRect(2, 2, w - 4, trainH, 25, 25);

	            g2.setColor(Color.WHITE);
	            int infoH = info.getPreferredSize().height + 10; 
	            int infoW = w - 16;
	            g2.fillRoundRect(8, 32, infoW, infoH, 12, 12);

	            g2.setColor(Color.WHITE);
	            g2.fillOval(w/4 - 8, trainH - 14, 16, 16);
	            g2.fillOval(3*w/4 - 8, trainH - 14, 16, 16);

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

	    JPanel infoWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 5));
	    infoWrapper.setOpaque(false);
	    infoWrapper.setBorder(new EmptyBorder(5, 0, 0, 0)); 
	    infoWrapper.add(info);

	    card.add(infoWrapper, BorderLayout.CENTER);

	    return card;
	}

	private JPanel buildToaGheSection() {
		JPanel sec = new JPanel(new BorderLayout(0, 0));
		sec.setBackground(BG);

		pnlToaContainer = new JPanel(new BorderLayout(0, 4)); 
		pnlToaContainer.setBackground(BG);

		JPanel ttlWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ttlWrapper.setBackground(BG);
		JLabel ttl = new JLabel("Danh sách toa tàu");
		ttl.setFont(new Font("Segoe UI", Font.BOLD, 13));
		ttl.setForeground(Color.WHITE);
		ttl.setOpaque(true);
		ttl.setBackground(NAVY_LIGHT);
		ttl.setBorder(new EmptyBorder(6, 12, 6, 12));
		ttlWrapper.add(ttl);
		
		pnlToaContainer.add(ttlWrapper, BorderLayout.NORTH);

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
		
		pnlToaContainer.add(toaSP, BorderLayout.CENTER);
		sec.add(pnlToaContainer, BorderLayout.NORTH);

		pnlGheContainer = new JPanel(new BorderLayout(0, 2)); 
		pnlGheContainer.setBackground(BG);
		pnlGheContainer.setBorder(new EmptyBorder(0, 0, 0, 0)); 

		lblTenToa = new JLabel("Toa thường: --", SwingConstants.CENTER);
		lblTenToa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblTenToa.setForeground(NAVY);
		pnlGheContainer.add(lblTenToa, BorderLayout.NORTH);

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
		
		pnlGheContainer.add(pnlGheWrapper, BorderLayout.CENTER);

		JPanel botLegendArea = new JPanel(new BorderLayout());
		botLegendArea.setBackground(BG);
		botLegendArea.setBorder(new EmptyBorder(4, 0, 2, 0));

		lblGheTrong = new JLabel("Số ghế còn trống: --", SwingConstants.CENTER);
		lblGheTrong.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblGheTrong.setForeground(Color.BLACK);
		
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

		pnlGheContainer.add(botLegendArea, BorderLayout.SOUTH);
		sec.add(pnlGheContainer, BorderLayout.CENTER);

		refreshToaGhe();
		return sec;
	}

	private void refreshToaGhe() {
		if (chuyenIdx == -1) {
			if (pnlToaContainer != null) pnlToaContainer.setVisible(false);
			if (pnlGheContainer != null) pnlGheContainer.setVisible(false);
			this.revalidate();
			this.repaint();
			return;
		}
		
		if (pnlToaContainer != null) pnlToaContainer.setVisible(true);

		String[] toaList = buildToaList(chuyenIdx);
		String[][] filtered = dangXemChieuVe ? CHUYEN_FILTERED_VE : CHUYEN_FILTERED;

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

		if (toaIdx == -1) {
			if (pnlGheContainer != null) pnlGheContainer.setVisible(false);
			this.revalidate();
			this.repaint();
			return;
		}
		
		if (pnlGheContainer != null) pnlGheContainer.setVisible(true);

		Set<Integer> dadat = gheDaDat(chuyenIdx, toaIdx);
		lblTenToa.setText("Toa: " + toaList[toaIdx]); 
		int trong = 0;
		for (int g = 1; g <= 28; g++)
			if (!dadat.contains(g))
				trong++;
		lblGheTrong.setText("Số ghế còn trống: " + trong + "/28");

		pnlGhe.removeAll();
		pnlGhe.setLayout(new GridLayout(2, 14, 4, 4)); 
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
					g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
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
		
		this.revalidate();
		this.repaint();
	}

	private void switchToChieuVe() {
		// Lưu trạng thái chiều đi
		trangDi = trang;
		chuyenIdxDi = chuyenIdx;
		toaIdxDi = toaIdx;
		gheChonDi.clear(); gheChonDi.addAll(gheChon);

		// Chuyển sang chiều về
		dangXemChieuVe = true;
		trang = trangVe;
		chuyenIdx = chuyenIdxVe;
		toaIdx = toaIdxVe;
		gheChon.clear(); gheChon.addAll(gheChonVe);

		// Cập nhật tiêu đề
		if (lblChieuHeader != null) {
			lblChieuHeader.setText("Chiều về : Ngày " + ngayVe + " từ " + gaDen + " đến " + gaDi);
		}
		// Đổi nút
		if (btnChieuToggle != null) {
			btnChieuToggle.setText("Chiều đi");
		}
		// Reset filter combo
		if (cbKhungGio != null) cbKhungGio.setSelectedIndex(0);

		refreshChuyen();
		refreshToaGhe();
		updateActionBtn();
		updateGheDaChon();
	}

	private void switchToChieuDi() {
		// Lưu trạng thái chiều về
		trangVe = trang;
		chuyenIdxVe = chuyenIdx;
		toaIdxVe = toaIdx;
		gheChonVe.clear(); gheChonVe.addAll(gheChon);

		// Chuyển về chiều đi
		dangXemChieuVe = false;
		trang = trangDi;
		chuyenIdx = chuyenIdxDi;
		toaIdx = toaIdxDi;
		gheChon.clear(); gheChon.addAll(gheChonDi);

		// Cập nhật tiêu đề
		if (lblChieuHeader != null) {
			lblChieuHeader.setText("Chiều đi : Ngày " + ngayDi + " từ " + gaDi + " đến " + gaDen);
		}
		// Đổi nút
		if (btnChieuToggle != null) {
			btnChieuToggle.setText("Chiều về");
		}
		// Reset filter combo
		if (cbKhungGio != null) cbKhungGio.setSelectedIndex(0);

		refreshChuyen();
		refreshToaGhe();
		updateActionBtn();
		updateGheDaChon();
	}

	private JPanel buildToaIcon(String maToa, boolean sel, int ti) {
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

		JLabel icon = new JLabel();
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		icon.setPreferredSize(new Dimension(52, 44));

		Icon toaImg = loadAndScaleIcon("/Images/logoToaTau.png", 56, 36);
		if (toaImg != null) {
			icon.setIcon(toaImg);
		} else {
			icon.setText("Lỗi ảnh"); 
		}

		JLabel lbl = new JLabel(maToa, SwingConstants.CENTER);
		lbl.setFont(new Font("Segoe UI", Font.PLAIN, 9));
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

	private JPanel buildBotBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

		Icon icQuayLai = loadAndScaleIcon("/Images/logoBack.png", 14, 14);
		JButton btnQuay = makeOutlineBtn("Quay lại", icQuayLai);
		btnQuay.addActionListener(e -> {
			if (onQuayLai != null)
				onQuayLai.run();
		});

		lblGheDaChon = new JLabel("  Số vé đã chọn: 0/" + soLuong);
		lblGheDaChon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblGheDaChon.setForeground(new Color(50, 70, 110));

		Icon icChonNhanh = loadAndScaleIcon("/Images/logoGhe.png", 14, 14);
		btnAction = makeNavyBtn("Chọn nhanh", icChonNhanh);
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
		b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
		b.setFont(new Font("Segoe UI", Font.BOLD, 12));
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