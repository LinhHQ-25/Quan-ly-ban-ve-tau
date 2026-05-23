package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TraVeGUI extends JPanel {
    private static final Color BORDER          = new Color(210, 215, 224);
    private static final Color NAVY            = GuiTheme.NAVY;
    private static final Color BG              = new Color(242, 247, 252);
    private static final Color WARN_FG         = new Color(180, 60, 0);
    private static final Color OK_FG           = new Color(30, 120, 60);
    private static final Color RED_FG          = new Color(180, 30, 30);
    private static final Font  FONT_14         = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font  FONT_B14        = new Font("Segoe UI", Font.BOLD, 14);
    private static final int   FIELD_H         = 36;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static String   s_maVe = "";
    private static String[] s_data = new String[0];

    public static void setVeDuocChon(String maVe, String[] data) {
        s_maVe = maVe;
        s_data = data.clone();
    }

    private final AppFrame appFrame;
    private JTextField tfMaVe, tfKhachHang, tfChuyen, tfGaDi, tfGaDen, tfNgayGio, tfLoai, tfMaGhe, tfViTri, tfGia;
    private JLabel     lbStatTong, lbStatPhi, lbStatHoan, lbDieuKien, lbWarning;
    private JPanel     statPanelPhi;
    private JComboBox<String> cbLyDo;
    private JTextField txtLyDoKhac, txtGhiChuNV;
    private JButton    btnXacNhan;

    public TraVeGUI(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel pnlPage = new JPanel(new BorderLayout(0, 4));
        pnlPage.setOpaque(false);
        pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, 0, GuiTheme.PAGE_PAD_LEFT));

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        // 1. Panel Thông tin vé (Top)
        gbc.gridy = 0; gbc.insets = new Insets(10, 0, 15, 0);
        pnlCenter.add(buildTopInfoPanel(), gbc);

        // 2. Panel Chi tiết (Bottom)
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 0, 0);
        pnlCenter.add(buildBottomDetailPanel(), gbc);

        // 3. Panel độn (Filler) dãn nốt chiều dọc
        gbc.gridy = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        pnlCenter.add(filler, gbc);

        pnlPage.add(pnlCenter, BorderLayout.CENTER);
        pnlPage.add(buildButtonRow(), BorderLayout.SOUTH);
        add(pnlPage, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        fillInfo();
        calcFee();
    }

    private JPanel buildTopInfoPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 15));
        outer.setBackground(Color.WHITE);
        outer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(12, 14, 15, 14)
        ));

        JLabel lbTitle = new JLabel("Thông tin vé trả");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbTitle.setForeground(NAVY);
        lbTitle.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        outer.add(lbTitle, BorderLayout.NORTH);

        tfMaVe      = readField();
        tfKhachHang = readField();
        tfChuyen    = readField();
        tfGaDi      = readField();
        tfGaDen     = readField();
        tfNgayGio   = readField();
        tfLoai      = readField();
        tfMaGhe     = readField();
        tfViTri     = readField();
        tfGia       = readField();

        lbDieuKien = new JLabel("—");
        lbDieuKien.setFont(FONT_B14);
        lbDieuKien.setForeground(GuiTheme.SUB_TEXT);

        JPanel infoGrid = new JPanel(new GridLayout(2, 4, 15, 15));
        infoGrid.setOpaque(false);

        infoGrid.add(fieldBox("Mã vé", tfMaVe));
        infoGrid.add(fieldBox("Khách hàng", tfKhachHang));
        infoGrid.add(fieldBox("Mã chuyến", tfChuyen));
        infoGrid.add(fieldBox("Loại vé", tfLoai));

        infoGrid.add(fieldBox("Ngày/Giờ KH", tfNgayGio));
        infoGrid.add(fieldBox("Đơn giá", tfGia));
        infoGrid.add(fieldBox("Ga đi", tfGaDi));
        infoGrid.add(fieldBox("Ga đến", tfGaDen));

        outer.add(infoGrid, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildBottomDetailPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel pnlFinance = new JPanel(new BorderLayout(0, 15));
        pnlFinance.setBackground(Color.WHITE);
        pnlFinance.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(15, 14, 15, 14)));

        lbStatTong = statLabel(GuiTheme.TEXT, Font.BOLD, 22);
        lbStatPhi  = statLabel(new Color(180, 60, 0), Font.BOLD, 22);
        lbStatHoan = statLabel(OK_FG, Font.BOLD, 22);

        JPanel statRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statRow.setOpaque(false);
        statRow.add(buildStatCard("Tiền vé", lbStatTong, new Color(248,250,252), BORDER, GuiTheme.TEXT));
        statPanelPhi = buildStatCard("Phí trả", lbStatPhi, new Color(254, 249, 235), new Color(251, 207, 100), new Color(160,100,0));
        statRow.add(statPanelPhi);
        statRow.add(buildStatCard("Hoàn lại", lbStatHoan, new Color(240, 253, 244), new Color(134, 239, 172), OK_FG));

        JPanel dieuKienRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dieuKienRow.setOpaque(false);
        JLabel lbDKTitle = new JLabel("Điều kiện trả vé:  ");
        lbDKTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbDKTitle.setForeground(GuiTheme.SUB_TEXT);
        dieuKienRow.add(lbDKTitle);
        dieuKienRow.add(lbDieuKien);

        pnlFinance.add(statRow, BorderLayout.CENTER);
        pnlFinance.add(dieuKienRow, BorderLayout.SOUTH);

        JPanel pnlReason = new JPanel(new BorderLayout(0, 12));
        pnlReason.setBackground(Color.WHITE);
        pnlReason.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(15, 14, 15, 14)));

        cbLyDo = new JComboBox<>(new String[]{"Thay đổi kế hoạch", "Bận việc đột xuất", "Lý do sức khỏe", "Khác"});
        cbLyDo.setFont(FONT_14); cbLyDo.setBackground(Color.WHITE);
        cbLyDo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(2, 4, 2, 4)));
        cbLyDo.setPreferredSize(new Dimension(200, FIELD_H));

        txtLyDoKhac = new JTextField();
        txtLyDoKhac.setFont(FONT_14); txtLyDoKhac.setVisible(false);
        txtLyDoKhac.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 10, 4, 10)));

        cbLyDo.addActionListener(e -> {
            boolean isKhac = "Khác".equals(cbLyDo.getSelectedItem());
            txtLyDoKhac.setVisible(isKhac);
            if (!isKhac) txtLyDoKhac.setText("");
        });

        lbWarning = new JLabel(" ");
        lbWarning.setFont(FONT_14); lbWarning.setForeground(WARN_FG);

        JPanel lyDoWrapper = new JPanel(new BorderLayout(15, 0));
        lyDoWrapper.setOpaque(false);
        lyDoWrapper.add(cbLyDo, BorderLayout.WEST);
        lyDoWrapper.add(txtLyDoKhac, BorderLayout.CENTER);

        pnlReason.add(fieldBox("Lý do trả vé (Khách hàng cung cấp)", lyDoWrapper), BorderLayout.NORTH);
        pnlReason.add(labelBox("Trạng thái", lbWarning), BorderLayout.CENTER);

        JPanel pnlGhiChu = new JPanel(new BorderLayout());
        pnlGhiChu.setBackground(Color.WHITE);
        pnlGhiChu.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(15, 14, 15, 14)));

        txtGhiChuNV = new JTextField("Không có");
        txtGhiChuNV.setFont(FONT_14);
        txtGhiChuNV.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 10, 4, 10)));

        pnlGhiChu.add(fieldBox("Ghi chú của Nhân viên (Tùy chọn)", txtGhiChuNV), BorderLayout.CENTER);

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 15, 0);
        outer.add(pnlFinance, gbc);

        gbc.gridy = 1;
        outer.add(pnlReason, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);
        outer.add(pnlGhiChu, gbc);

        return outer;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        p.setOpaque(false); p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JButton btnBack = makeOutlineBtn("Quay lại", 130, 38);
        btnBack.addActionListener(e -> appFrame.showCard("doi-tra"));

        btnXacNhan = makeNavyBtn("Xác nhận trả vé", 130, 38);
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(e -> handleTiepTuc());

        p.add(btnBack); p.add(btnXacNhan);
        return p;
    }

    private void fillInfo() {
        if (s_data.length < 9) { clearInfo(); return; }
        tfMaVe.setText(s_maVe.isEmpty() ? "—" : s_maVe);
        tfChuyen.setText(s_data[0]); tfGaDi.setText(s_data[1]); tfGaDen.setText(s_data[2]);
        String loaiVeChuan = "MOT_CHIEU".equalsIgnoreCase(s_data[3]) ? "Một chiều"
                : "KHU_HOI".equalsIgnoreCase(s_data[3])   ? "Khứ hồi"
                  : s_data[3];
        tfLoai.setText(loaiVeChuan); tfNgayGio.setText(s_data[5]);
        tfMaGhe.setText(s_data[7]);

        tfViTri.setText(s_data.length > 9 ? s_data[9] : "—");
        tfKhachHang.setText(s_data.length > 10 ? s_data[10] : "—");

        try {
            long gia = (long) Double.parseDouble(s_data[8]);
            tfGia.setText(String.format("%,d đ", gia).replace(",", "."));
        } catch (Exception e) { tfGia.setText(s_data[8]); }
    }

    private void calcFee() {
        if (s_data.length < 9 || s_maVe.isEmpty()) {
            lbDieuKien.setText("—"); lbStatTong.setText("—"); lbStatPhi.setText("—"); lbStatHoan.setText("—");
            lbWarning.setText(" "); btnXacNhan.setEnabled(false); return;
        }

        long gio = tinhGio(s_data[5]);
        long soLuong = 1, donGia = 0;
        try { soLuong = Long.parseLong(s_data[6].replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
        try { donGia = (long) Double.parseDouble(s_data[8]); } catch (Exception ignored) {}

        boolean nhom = soLuong >= 2;
        long tongTien = soLuong * donGia;
        lbStatTong.setText(fmtTien(tongTien));

        int phiPct; boolean hopLe; String dieuKien;
        if (nhom) {
            if      (gio >= 72) { phiPct = 20; hopLe = true;  dieuKien = "Hợp lệ — phí 20% (vé nhóm)"; }
            else if (gio >= 24) { phiPct = 30; hopLe = true;  dieuKien = "Hợp lệ — phí 30% (vé nhóm)"; }
            else                { phiPct = -1; hopLe = false; dieuKien = "Quá hạn — dưới 24h (vé nhóm)"; }
        } else {
            if      (gio >= 48) { phiPct = 10; hopLe = true;  dieuKien = "Hợp lệ — phí 10%"; }
            else if (gio >= 12) { phiPct = 20; hopLe = true;  dieuKien = "Hợp lệ — phí 20%"; }
            else                { phiPct = -1; hopLe = false; dieuKien = "Quá hạn — dưới 12h"; }
        }

        lbDieuKien.setText(dieuKien); lbDieuKien.setForeground(hopLe ? OK_FG : RED_FG);
        if (hopLe) {
            long phiTien = Math.round(tongTien * phiPct / 100.0);
            updateStatTitle(statPanelPhi, "Phí trả (" + phiPct + "%)");
            lbStatPhi.setText(fmtTien(phiTien)); lbStatPhi.setForeground(new Color(160,100,0));
            lbStatHoan.setText(fmtTien(tongTien - phiTien)); lbStatHoan.setForeground(OK_FG);
            lbWarning.setText("Vé hợp lệ. Nhấn 'Tiếp tục' để xác nhận."); lbWarning.setForeground(OK_FG);
            btnXacNhan.setEnabled(true);
        } else {
            updateStatTitle(statPanelPhi, "Phí trả");
            lbStatPhi.setText("Không hoàn"); lbStatPhi.setForeground(RED_FG);
            lbStatHoan.setText("0 đ"); lbStatHoan.setForeground(RED_FG);
            lbWarning.setText("Không đủ điều kiện hoàn tiền."); lbWarning.setForeground(WARN_FG);
            btnXacNhan.setEnabled(false);
        }
    }

    private void clearInfo() {
        for (JTextField tf : new JTextField[]{tfMaVe, tfKhachHang, tfChuyen, tfGaDi, tfGaDen, tfNgayGio, tfLoai, tfMaGhe, tfViTri, tfGia})
            if (tf != null) tf.setText("—");
    }

    private void handleTiepTuc() {
        String lyDo = cbLyDo.getSelectedItem().toString();
        if ("Khác".equals(lyDo)) {
            if (txtLyDoKhac.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,"Vui lòng nhập lý do!","Yêu cầu nhập liệu",JOptionPane.WARNING_MESSAGE);
                txtLyDoKhac.requestFocus(); return;
            }
            lyDo = txtLyDoKhac.getText().trim();
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Xác nhận trả vé " + s_maVe + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        String maDon;
        try (java.sql.Connection conn = connect_DB.Connect_DB.getInstance().getConnection()) {
            maDon = util.MaTuDong.taoMaDon(conn, java.time.LocalDate.now());
        } catch (Exception e) { return; }

        long tienHoanKhach = 0;
        try { tienHoanKhach = Long.parseLong(lbStatHoan.getText().replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        long phiHuyVe = 0;
        try { phiHuyVe = Long.parseLong(lbStatPhi.getText().replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        String sqlInsertHD = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) " +
                "SELECT ?, GETDATE(), hd.maNV, hd.maKH, ?, 0, N'Hoàn tiền' " +
                "FROM HoaDon hd JOIN Ve v ON v.maHoaDon = hd.maHoaDon WHERE v.maVe = ?";

        String sqlUpdateVe  = "UPDATE Ve SET trangThaiVe = N'Đã hủy', maHoaDon = ? WHERE maVe = ?";
        String sqlInsertDon = "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, tienHoanTra, loaiDon, maVe) VALUES (?, ?, GETDATE(), ?, 'DON_TRA', ?)";

        try (java.sql.Connection conn = connect_DB.Connect_DB.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (java.sql.PreparedStatement psHD  = conn.prepareStatement(sqlInsertHD);
                 java.sql.PreparedStatement psVe  = conn.prepareStatement(sqlUpdateVe);
                 java.sql.PreparedStatement psDon = conn.prepareStatement(sqlInsertDon)) {

                psHD.setString(1, maDon);
                psHD.setLong  (2, phiHuyVe);
                psHD.setString(3, s_maVe);
                int hdRows = psHD.executeUpdate();
                if (hdRows == 0) throw new Exception("Không tìm được hóa đơn gốc của vé " + s_maVe);

                psVe.setString(1, maDon);
                psVe.setString(2, s_maVe);
                if (psVe.executeUpdate() == 0) throw new Exception("Không tìm thấy vé " + s_maVe);

                psDon.setString(1, maDon);
                psDon.setLong  (2, phiHuyVe);
                psDon.setLong  (3, tienHoanKhach);
                psDon.setString(4, s_maVe);
                psDon.executeUpdate();

                conn.commit();
                xuatHoaDonPDF(maDon);

                JOptionPane.showMessageDialog(this,
                        "<html><div style='padding:6px'><b>Trả vé thành công!</b><br><br>" +
                                "Mã đơn: <b>" + maDon + "</b><br>" +
                                "Phí giữ lại: <b>" + fmtTien(phiHuyVe) + "</b><br>" +
                                "Hoàn trả khách: <b>" + fmtTien(tienHoanKhach) + "</b></div></html>",
                        "Hoàn tất", JOptionPane.PLAIN_MESSAGE);
                appFrame.showCard("doi-tra");

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static long tinhGio(String s) {
        try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(s, FMT)); }
        catch (Exception e) { return -1; }
    }

    private JPanel buildStatCard(String title, JLabel valueLb, Color bg, Color border, Color titleColor) {
        JPanel p = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(border); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose();
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(15,10,15,10));
        JLabel lbTt = new JLabel(title); lbTt.setFont(FONT_14); lbTt.setForeground(titleColor); lbTt.setHorizontalAlignment(SwingConstants.CENTER); lbTt.setName("stat-title");
        valueLb.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbTt, BorderLayout.NORTH); p.add(valueLb, BorderLayout.CENTER); return p;
    }

    private void updateStatTitle(JPanel card, String newTitle) {
        for (Component c : card.getComponents()) if (c instanceof JLabel lb && "stat-title".equals(lb.getName())) { lb.setText(newTitle); break; }
    }

    private JLabel statLabel(Color color, int style, int size) {
        JLabel lb = new JLabel("—"); lb.setFont(new Font("Segoe UI", style, size)); lb.setForeground(color); lb.setHorizontalAlignment(SwingConstants.CENTER); return lb;
    }

    private JTextField readField() {
        JTextField tf = new JTextField("—"); tf.setFont(FONT_14); tf.setEditable(false);
        tf.setForeground(GuiTheme.TEXT); tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1), new EmptyBorder(4,10,4,10)));
        tf.setPreferredSize(new Dimension(0, FIELD_H)); return tf;
    }

    private JPanel fieldBox(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6)); p.setOpaque(false);
        JLabel lb = new JLabel(label); lb.setFont(FONT_14); lb.setForeground(GuiTheme.SUB_TEXT);
        p.add(lb, BorderLayout.NORTH); p.add(comp, BorderLayout.CENTER); return p;
    }

    private JPanel labelBox(String labelText, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(0, 6)); p.setOpaque(false);
        JLabel lb = new JLabel(labelText); lb.setFont(FONT_14); lb.setForeground(GuiTheme.SUB_TEXT);
        p.add(lb, BorderLayout.NORTH); p.add(value, BorderLayout.CENTER); return p;
    }

    private static String fmtTien(long a) { return String.format("%,d đ", a).replace(",", "."); }

    private JButton makeNavyBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled() ? new Color(180,190,205) : getModel().isPressed() ? GuiTheme.NAVY_DARK : getModel().isRollover() ? GuiTheme.NAVY_HOVER : NAVY;
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(Color.WHITE); g2.setFont(FONT_14); FontMetrics fm=g2.getFontMetrics(); String t=getText();
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w,h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }

    private JButton makeOutlineBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg=getModel().isPressed()?new Color(220,225,235):getModel().isRollover()?new Color(235,239,246):new Color(240,243,248);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(GuiTheme.TEXT); g2.setFont(FONT_14); FontMetrics fm=g2.getFontMetrics(); String t=getText();
                g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w,h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }

    private void xuatHoaDonPDF(String maDon) {
        try (java.sql.Connection conn = connect_DB.Connect_DB.getConnection()) {
            String sqlHD = "SELECT h.tongTien, h.tienNhan, h.phuongThucThanhToan, k.hoTenKH, k.sdt FROM HoaDon h LEFT JOIN KhachHang k ON h.maKH = k.maKH WHERE h.maHoaDon = ?";
            String tenKH = "Khách vãng lai", sdtKH = "", hinhThuc = "";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlHD)) {
                ps.setString(1, maDon);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tenKH = rs.getString("hoTenKH") != null ? rs.getString("hoTenKH") : "Khách vãng lai";
                        sdtKH = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                        hinhThuc = rs.getString("phuongThucThanhToan") != null ? rs.getString("phuongThucThanhToan") : "";
                    }
                }
            }

            String sqlDon = "SELECT tienBu, tienHoanTra FROM DonDoiTraVe WHERE maDon = ?";
            double phiTraVe = 0, tienHoanThucTe = 0;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlDon)) {
                ps.setString(1, maDon);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        phiTraVe = rs.getDouble("tienBu");
                        tienHoanThucTe = rs.getDouble("tienHoanTra");
                    }
                }
            }

            String sqlVe = "SELECT v.maVe, v.loaiVe, v.giaVe, g.loaiGhe, gaDi.tenGa AS gaDi, gaDen.tenGa AS gaDen FROM Ve v JOIN Ghe g ON v.maGhe = g.maGhe JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau JOIN Ga gaDi ON dt.maGaDi = gaDi.maGa JOIN Ga gaDen ON dt.maGaDen = gaDen.maGa WHERE v.maHoaDon = ?";

            java.io.File folder = new java.io.File("HoaDon");
            if (!folder.exists()) folder.mkdir();
            java.io.File pdfFile = new java.io.File(folder, maDon + ".pdf");

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(pdfFile));
            document.open();

            com.itextpdf.text.pdf.BaseFont bf = com.itextpdf.text.pdf.BaseFont.createFont("c:/windows/fonts/arial.ttf", com.itextpdf.text.pdf.BaseFont.IDENTITY_H, com.itextpdf.text.pdf.BaseFont.EMBEDDED);
            com.itextpdf.text.Font fontTitle = new com.itextpdf.text.Font(bf, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fontBold = new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fontNormal = new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fontItalic = new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.ITALIC);

            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("HÓA ĐƠN GIÁ TRỊ GIA TĂNG", fontTitle);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);

            String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
            com.itextpdf.text.Paragraph dateP = new com.itextpdf.text.Paragraph("Ngày xuất: " + dateStr, fontItalic);
            dateP.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(dateP);
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            document.add(new com.itextpdf.text.Paragraph("Đơn vị bán hàng: CÔNG TY CỔ PHẦN VẬN TẢI ĐƯỜNG SẮT", fontBold));
            document.add(new com.itextpdf.text.Paragraph("Mã số thuế: 0100106264", fontNormal));
            document.add(new com.itextpdf.text.Paragraph("Địa chỉ: 113 Nguyễn Đình Thụ, Tuy Phước, Gia Lai", fontNormal));
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            document.add(new com.itextpdf.text.Paragraph("Họ tên người mua hàng: " + tenKH, fontBold));
            document.add(new com.itextpdf.text.Paragraph("Điện thoại: " + sdtKH, fontNormal));
            document.add(new com.itextpdf.text.Paragraph("Hình thức thanh toán: " + hinhThuc + "          Mã HĐ: " + maDon, fontNormal));
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3.5f, 2f, 2.3f, 1.8f, 2f, 1f, 1f, 2f, 2.2f});
            for (String h : new String[]{"STT","Tên dịch vụ","Loại vé","Mã vé","Chiều","Giá vé","ĐVT","SL","Phụ phí","Thành tiền"}) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h, fontBold));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                cell.setPaddingBottom(6);
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
                table.addCell(cell);
            }

            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            int stt = 1;
            double tongTienVe = 0;
            double tongPhuPhi = 0;

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlVe)) {
                ps.setString(1, maDon);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double giaVe = rs.getDouble("giaVe");
                        double phuPhiDong = phiTraVe;
                        double thanhTien = giaVe - phuPhiDong;
                        tongTienVe += giaVe;
                        tongPhuPhi += phuPhiDong;

                        String lv = rs.getString("loaiVe");
                        String loaiVeHienThi = "MOT_CHIEU".equals(lv) ? "Một chiều" : "KHU_HOI".equals(lv) ? "Khứ hồi" : (lv != null ? lv : "");

                        pdfAddCell(table, fontNormal, String.valueOf(stt++), com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, "Vé HK trực tiếp tại nhà ga", com.itextpdf.text.Element.ALIGN_LEFT);
                        pdfAddCell(table, fontNormal, loaiVeHienThi, com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, rs.getString("maVe") != null ? rs.getString("maVe") : "", com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, rs.getString("gaDi") + " → " + rs.getString("gaDen"), com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, df.format(giaVe), com.itextpdf.text.Element.ALIGN_RIGHT);
                        pdfAddCell(table, fontNormal, "Vé", com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, "1", com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, df.format(phuPhiDong), com.itextpdf.text.Element.ALIGN_RIGHT);
                        pdfAddCell(table, fontNormal, df.format(thanhTien), com.itextpdf.text.Element.ALIGN_RIGHT);
                    }
                }
            }

            document.add(table);
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            double tongTienHoan = tongTienVe - tongPhuPhi;
            com.itextpdf.text.Paragraph pGiaVe = new com.itextpdf.text.Paragraph("Tiền vé: " + df.format(tongTienVe), fontNormal);
            pGiaVe.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pGiaVe);

            com.itextpdf.text.Paragraph pPhi = new com.itextpdf.text.Paragraph("Phụ phí (phí trả vé): " + df.format(tongPhuPhi), fontNormal);
            pPhi.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pPhi);

            com.itextpdf.text.Paragraph pTong = new com.itextpdf.text.Paragraph("Tổng tiền hoàn: " + df.format(tongTienHoan), fontBold);
            pTong.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pTong);

            com.itextpdf.text.Paragraph pChu = new com.itextpdf.text.Paragraph("Bằng chữ: " + docSoThanh((long) tongTienHoan), fontItalic);
            pChu.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pChu);

            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));
            document.add(new com.itextpdf.text.Paragraph("Ghi chú: ......................................................................................................................................", fontNormal));
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            com.itextpdf.text.pdf.PdfPTable signTable = new com.itextpdf.text.pdf.PdfPTable(2);
            signTable.setWidthPercentage(100);
            com.itextpdf.text.pdf.PdfPCell cBuyer = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Người mua hàng\n(Ký, ghi rõ họ tên)", fontNormal));
            com.itextpdf.text.pdf.PdfPCell cSeller = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Người bán hàng\n(Ký, ghi rõ họ tên)", fontNormal));
            cBuyer.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER); cBuyer.setBorder(com.itextpdf.text.pdf.PdfPCell.NO_BORDER);
            cSeller.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER); cSeller.setBorder(com.itextpdf.text.pdf.PdfPCell.NO_BORDER);
            signTable.addCell(cBuyer);
            signTable.addCell(cSeller);
            document.add(signTable);

            document.close();
            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pdfAddCell(com.itextpdf.text.pdf.PdfPTable table, com.itextpdf.text.Font font, String text, int align) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
        cell.setPaddingBottom(4);
        table.addCell(cell);
    }

    private String docSoThanh(long so) {
        if (so == 0) return "Không đồng";
        String[] donVi = {"", "nghìn", "triệu", "tỷ"};
        String[] chu = {"", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
        if (so < 0) return "Âm " + docSoThanh(-so);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (so > 0) {
            int nhom = (int)(so % 1000);
            if (nhom != 0) {
                String phan = docNhom(nhom, chu) + (donVi[i].isEmpty() ? "" : " " + donVi[i]);
                sb.insert(0, (sb.length() > 0 ? " " : "") + phan);
            }
            so /= 1000; i++;
        }
        String result = sb.toString().trim();
        return Character.toUpperCase(result.charAt(0)) + result.substring(1) + " đồng";
    }

    private String docNhom(int n, String[] chu) {
        int tram = n / 100, chuc = (n % 100) / 10, dv = n % 10;
        StringBuilder s = new StringBuilder();
        if (tram > 0) s.append(chu[tram]).append(" trăm");
        if (chuc > 1) {
            s.append(s.length() > 0 ? " " : "").append(chu[chuc]).append(" mươi");
            if (dv > 0) s.append(" ").append(dv == 1 ? "mốt" : dv == 5 ? "lăm" : chu[dv]);
        } else if (chuc == 1) {
            s.append(s.length() > 0 ? " " : "").append("mười");
            if (dv > 0) s.append(" ").append(dv == 5 ? "lăm" : chu[dv]);
        } else if (dv > 0) {
            if (tram > 0) s.append(" lẻ");
            s.append(" ").append(chu[dv]);
        }
        return s.toString().trim();
    }
}