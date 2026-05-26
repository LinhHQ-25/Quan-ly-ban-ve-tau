package gui;

import java.awt.Dialog;
import javax.swing.KeyStroke;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
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
            lbWarning.setText("Vé hợp lệ. Nhấn 'Xác nhận trả vé' để tiến hành hoàn tiền."); lbWarning.setForeground(OK_FG);
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
        int choice = JOptionPane.showConfirmDialog(this,
                "Xác nhận trả vé " + s_maVe + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        String maDon;
        try (Connection conn = connect_DB.Connect_DB.getInstance().getConnection()) {
            maDon = util.MaTuDong.taoMaDon(conn, java.time.LocalDate.now());
        } catch (Exception e) { return; }

        String currentNV = service.AuthService.getCurrentMaNV();
        if (currentNV == null || currentNV.trim().isEmpty()) {
            currentNV = "NV001";
        }

        long tienHoanKhach = 0;
        try { tienHoanKhach = Long.parseLong(lbStatHoan.getText().replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        long phiHuyVe = 0;
        try { phiHuyVe = Long.parseLong(lbStatPhi.getText().replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        String sqlInsertHD = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) " +
                "SELECT ?, GETDATE(), ?, hd.maKH, ?, 0, N'Hoàn tiền' " +
                "FROM HoaDon hd JOIN Ve v ON v.maHoaDon = hd.maHoaDon WHERE v.maVe = ?";

        String sqlUpdateVe  = "UPDATE Ve SET trangThaiVe = N'Đã hủy', maHoaDon = ? WHERE maVe = ?";
        String sqlInsertDon = "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, tienHoanTra, loaiDon, maVe) VALUES (?, ?, GETDATE(), ?, 'DON_TRA', ?)";

        final String maDonFinal = maDon;
        final long tienHoanFinal = tienHoanKhach;
        final long phiFinal = phiHuyVe;

        try (Connection conn = connect_DB.Connect_DB.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (java.sql.PreparedStatement psHD  = conn.prepareStatement(sqlInsertHD);
                 java.sql.PreparedStatement psVe  = conn.prepareStatement(sqlUpdateVe);
                 java.sql.PreparedStatement psDon = conn.prepareStatement(sqlInsertDon)) {

                psHD.setString(1, maDon);
                psHD.setString(2, currentNV);
                psHD.setLong  (3, phiHuyVe);
                psHD.setString(4, s_maVe);
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

                xuatHoaDonPDF(maDon, tienHoanKhach, phiHuyVe);

                // ── Popup thành công (giống DatVeGUI3) ──────────────────────────
                java.awt.Window ancestor = javax.swing.SwingUtilities.getWindowAncestor(this);
                javax.swing.JDialog dialog = new javax.swing.JDialog(ancestor, "",
                        java.awt.Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setUndecorated(true);

                final float[] alpha = { 0f };
                final int[] frame   = { 0 };
                final int TOTAL_FRAMES = 30;

                javax.swing.JPanel glass = new javax.swing.JPanel(new java.awt.GridBagLayout()) {
                    @Override protected void paintComponent(java.awt.Graphics g) {
                        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                        g2.setComposite(java.awt.AlphaComposite.getInstance(
                                java.awt.AlphaComposite.SRC_OVER, 0.25f));
                        g2.setColor(new java.awt.Color(10, 20, 50));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.dispose();
                    }
                };
                glass.setOpaque(false);

                javax.swing.JPanel box = new javax.swing.JPanel(
                        new java.awt.BorderLayout(0, 12)) {
                    @Override protected void paintComponent(java.awt.Graphics g) {
                        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setComposite(java.awt.AlphaComposite.getInstance(
                                java.awt.AlphaComposite.SRC_OVER, alpha[0]));
                        g2.setColor(java.awt.Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                        int cx = getWidth() / 2, cy = 70, r = 38;
                        float progress = Math.min(1f, (float) frame[0] / TOTAL_FRAMES);
                        g2.setColor(new java.awt.Color(220, 245, 220));
                        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                        g2.setColor(new java.awt.Color(34, 170, 70));
                        g2.setStroke(new java.awt.BasicStroke(3f));
                        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                        if (progress > 0) {
                            g2.setStroke(new java.awt.BasicStroke(4f,
                                    java.awt.BasicStroke.CAP_ROUND,
                                    java.awt.BasicStroke.JOIN_ROUND));
                            int x1 = cx - 18, y1 = cy,
                                    xMid = cx - 6, yMid = cy + 14,
                                    x2 = cx + 20, y2 = cy - 16;
                            float p1 = Math.min(1f, progress / 0.5f);
                            g2.drawLine(x1, y1,
                                    (int)(x1 + (xMid - x1) * p1),
                                    (int)(y1 + (yMid - y1) * p1));
                            if (progress > 0.5f) {
                                float p2 = (progress - 0.5f) / 0.5f;
                                g2.drawLine(xMid, yMid,
                                        (int)(xMid + (x2 - xMid) * p2),
                                        (int)(yMid + (y2 - yMid) * p2));
                            }
                        }
                        g2.dispose();
                    }
                };
                box.setOpaque(false);
                box.setPreferredSize(new java.awt.Dimension(320, 240));
                box.setBorder(new javax.swing.border.EmptyBorder(140, 24, 24, 24));

                javax.swing.JLabel lblMsg = new javax.swing.JLabel(
                        "<html><div style='text-align:center;'>" +
                                "<b style='font-size:15px;color:#1c396e;'>Trả vé thành công!</b><br>" +
                                "<span style='font-size:13px;color:#888;'>Đang xuất hóa đơn hoàn tiền...</span>" +
                                "</div></html>",
                        javax.swing.SwingConstants.CENTER);
                box.add(lblMsg, java.awt.BorderLayout.CENTER);
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
                    if (frame[0] >= TOTAL_FRAMES) animTimer.stop();
                });
                animTimer.start();

                javax.swing.Timer closeTimer = new javax.swing.Timer(2500, ev -> {
                    animTimer.stop();
                    dialog.dispose();
                    appFrame.showCard("doi-tra");
                });
                closeTimer.setRepeats(false);
                closeTimer.start();

                dialog.setVisible(true);
                // ────────────────────────────────────────────────────────────────

            } catch (Exception ex) { conn.rollback(); throw ex; }
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

    private void xuatHoaDonPDF(String maDon, long tienHoanKhach, long phiHuyVe) {
        try (Connection conn = connect_DB.Connect_DB.getConnection()) {
            String sqlHD = "SELECT h.phuongThucThanhToan, k.hoTenKH, k.sdt FROM HoaDon h LEFT JOIN KhachHang k ON h.maKH = k.maKH WHERE h.maHoaDon = ?";
            String tenKH = "Khách vãng lai", sdtKH = "", hinhThuc = "Tiền mặt";
            try (PreparedStatement ps = conn.prepareStatement(sqlHD)) {
                ps.setString(1, maDon);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tenKH = rs.getString("hoTenKH") != null ? rs.getString("hoTenKH") : "Khách vãng lai";
                        sdtKH = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                    }
                }
            }

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
            document.add(new com.itextpdf.text.Paragraph("Nhân viên thực hiện trả vé: " + (service.AuthService.getCurrentHoTen() != null ? service.AuthService.getCurrentHoTen() : "N/A"), fontItalic));
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
            long tongTienVe = 0;
            try { tongTienVe = Long.parseLong(s_data[8].replaceAll("[^0-9]", "")); } catch(Exception ignored){}

            String loaiVe = s_data.length > 3 ? s_data[3] : "";
            String loaiVeHienThi = "MOT_CHIEU".equals(loaiVe) ? "Một chiều" : "KHU_HOI".equals(loaiVe) ? "Khứ hồi" : loaiVe;
            String chieu = s_data.length > 4 ? s_data[4] : "";

            pdfAddCell(table, fontNormal, "1", com.itextpdf.text.Element.ALIGN_CENTER);
            pdfAddCell(table, fontNormal, "Hủy vé trực tiếp", com.itextpdf.text.Element.ALIGN_LEFT);
            pdfAddCell(table, fontNormal, loaiVeHienThi, com.itextpdf.text.Element.ALIGN_CENTER);
            pdfAddCell(table, fontNormal, s_maVe, com.itextpdf.text.Element.ALIGN_CENTER);
            pdfAddCell(table, fontNormal, chieu, com.itextpdf.text.Element.ALIGN_CENTER);
            pdfAddCell(table, fontNormal, df.format(tongTienVe), com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfAddCell(table, fontNormal, "Vé", com.itextpdf.text.Element.ALIGN_CENTER);
            pdfAddCell(table, fontNormal, "1", com.itextpdf.text.Element.ALIGN_CENTER);
            pdfAddCell(table, fontNormal, df.format(phiHuyVe), com.itextpdf.text.Element.ALIGN_RIGHT);
            pdfAddCell(table, fontNormal, df.format(tienHoanKhach), com.itextpdf.text.Element.ALIGN_RIGHT);

            document.add(table);
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            com.itextpdf.text.Paragraph pGiaVe = new com.itextpdf.text.Paragraph("Tổng tiền: " + df.format(tongTienVe) + " VNĐ", fontBold);
            pGiaVe.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pGiaVe);

            com.itextpdf.text.Paragraph pPhi = new com.itextpdf.text.Paragraph("Tổng phụ phí (phí trả vé): " + df.format(phiHuyVe) + " VNĐ", fontBold);
            pPhi.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pPhi);

            com.itextpdf.text.Paragraph pTong = new com.itextpdf.text.Paragraph("Còn lại (Tiền hoàn khách): " + df.format(tienHoanKhach) + " VNĐ", fontBold);
            pTong.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pTong);

            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            com.itextpdf.text.Phrase phraseTienChu = new com.itextpdf.text.Phrase();
            phraseTienChu.add(new com.itextpdf.text.Chunk("Số tiền viết bằng chữ: ", fontNormal));
            phraseTienChu.add(new com.itextpdf.text.Chunk(docTien(tienHoanKhach), fontItalic));
            document.add(new com.itextpdf.text.Paragraph(phraseTienChu));

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

    private String docBaSo(int n, boolean hasPrefix) {
        String[] digits = { "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };
        int t = n / 100, c = (n % 100) / 10, d = n % 10; StringBuilder res = new StringBuilder();
        if (hasPrefix || t > 0) { res.append(digits[t]).append(" trăm "); if (c == 0 && d > 0) res.append("lẻ "); }
        if (c > 1) { res.append(digits[c]).append(" mươi "); if (d == 1) res.append("mốt"); else if (d == 5) res.append("lăm"); else if (d > 0) res.append(digits[d]); }
        else if (c == 1) { res.append("mười "); if (d == 5) res.append("lăm"); else if (d > 0) res.append(digits[d]); }
        else if (d > 0) { res.append(digits[d]); }
        return res.toString().trim();
    }

    private String docTien(long number) {
        if (number == 0) return "Không đồng";
        String[] units = { "", "nghìn", "triệu", "tỷ" }; StringBuilder result = new StringBuilder();
        long temp = number; int unitIndex = 0;
        while (temp > 0) {
            int group = (int) (temp % 1000); temp /= 1000;
            if (group > 0) { String groupStr = docBaSo(group, temp > 0); result.insert(0, groupStr + " " + units[unitIndex] + " "); }
            unitIndex++;
        }
        String finalStr = result.toString().replaceAll("\\s+", " ").trim();
        return finalStr.substring(0, 1).toUpperCase() + finalStr.substring(1) + " đồng";
    }
}