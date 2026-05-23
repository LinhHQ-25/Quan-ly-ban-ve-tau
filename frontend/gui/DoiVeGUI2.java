package gui;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import connect_DB.Connect_DB;
import util.MaTuDong;

public class DoiVeGUI2 extends JPanel {

    private static final Color NAVY     = new Color(28, 57, 110);
    private static final Color BG       = new Color(242, 247, 252);
    private static final Color BORDER_C = new Color(180, 205, 230);
    private static final Font  FONT_14  = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font  FONT_B14 = new Font("Segoe UI", Font.BOLD, 14);
    private static final DecimalFormat DF = new DecimalFormat("#,### VNĐ");

    private static String   s_maVe          = "";
    private static String[] s_dataCu         = new String[0];
    private static String   s_chuyenMoi     = "";
    private static String   s_ngayMoi       = "";
    private static String   s_gheMoiDB      = "";
    private static String   s_gheMoiHienThi = "";
    private static long     s_tongThu       = 0;
    private static long     s_giaVeMoi      = 0;

    public static void setDuLieuThanhToan(String maVe, String[] dataCu,
                                          String chuyenMoi, String ngayMoi, String gheMoiDB, String gheMoiHienThi,
                                          long tongThu, long giaVeMoi) {
        s_maVe          = maVe;
        s_dataCu        = (dataCu != null) ? dataCu.clone() : new String[0];
        s_chuyenMoi     = chuyenMoi;
        s_ngayMoi       = ngayMoi;
        s_gheMoiDB      = gheMoiDB;
        s_gheMoiHienThi = gheMoiHienThi;
        s_tongThu       = tongThu; // Giá đổi vé
        s_giaVeMoi      = giaVeMoi; // Giá vé mới đã + 30k
    }

    private final AppFrame appFrame;
    private JTable            tblChiTiet;
    private DefaultTableModel modelChiTiet;
    private JLabel            lblTongTien, lblTongKhuyenMai, lblThanhToanConLai;
    private JLabel            lblQR, lblCountdown;
    private JToggleButton     btnTienMat, btnChuyenKhoan;
    private JPanel            pnlSwitch;
    private CardLayout        cardSwitch;
    private JTextField        txtTienKhachDua;
    private JLabel            lblTienThua;
    private javax.swing.Timer countdownTimer, bankCheckTimer;
    private int               secondsLeft = 600;
    private ImageIcon         originalQRImageIcon = null;

    public DoiVeGUI2(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.78; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(0, 0, 0, 8);
        pnlCenter.add(buildLeftPanel(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.22; gbc.insets = new Insets(0, 0, 0, 0);
        pnlCenter.add(buildRightPanel(), gbc);

        add(pnlCenter, BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    public void refresh() {
        secondsLeft = 600;
        startCountdown();

        modelChiTiet.setRowCount(0);

        long giaVeCu = 0;
        try { giaVeCu = Long.parseLong(s_dataCu[8].replaceAll("[^0-9]", "")); } catch (Exception e) {}

        modelChiTiet.addRow(new Object[]{
                1, s_maVe, s_chuyenMoi, s_gheMoiHienThi, s_ngayMoi,
                DF.format(giaVeCu), DF.format(s_giaVeMoi), DF.format(s_tongThu)
        });

        tinhToanTaiChinh();

        if (s_tongThu <= 0) {
            btnChuyenKhoan.setEnabled(false);
            btnTienMat.setSelected(true);
            cardSwitch.show(pnlSwitch, "TIEN_MAT");
            txtTienKhachDua.setText("0");
            txtTienKhachDua.setEnabled(false);
            lblTienThua.setText(DF.format(Math.abs(s_tongThu)));
            lblTienThua.setForeground(new Color(30, 120, 60));
        } else {
            btnChuyenKhoan.setEnabled(true);
            txtTienKhachDua.setEnabled(true);
            txtTienKhachDua.setText("");
            lblTienThua.setText("0 VNĐ");
            lblTienThua.setForeground(Color.BLACK);
            if (btnChuyenKhoan.isSelected()) toggleQRCode();
        }
    }

    private JPanel buildLeftPanel() {
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10)); pnlLeft.setOpaque(false);
        JPanel pnlTableWrapper = new JPanel(new BorderLayout()); pnlTableWrapper.setBackground(Color.WHITE); pnlTableWrapper.setBorder(new LineBorder(BORDER_C, 1, true));
        JLabel lblChiTiet = new JLabel("Chi tiết Hóa đơn Đổi Vé"); lblChiTiet.setFont(FONT_B14); lblChiTiet.setForeground(Color.WHITE); lblChiTiet.setOpaque(true); lblChiTiet.setBackground(NAVY); lblChiTiet.setBorder(new EmptyBorder(6, 12, 6, 12));
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); titleWrap.setOpaque(false); titleWrap.add(lblChiTiet); pnlTableWrapper.add(titleWrap, BorderLayout.NORTH);

        String[] cols = {"STT","Mã vé","Mã chuyến mới","Ghế mới","Ngày/Giờ KH","Giá vé cũ","Giá vé mới","Giá đổi vé"};
        modelChiTiet = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        tblChiTiet = new JTable(modelChiTiet) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) { c.setBackground(new Color(210, 228, 245)); c.setForeground(Color.BLACK); }
                else { c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255)); c.setForeground(Color.BLACK); } return c;
            }
        };
        tblChiTiet.setRowHeight(35); tblChiTiet.setFont(FONT_14); tblChiTiet.getTableHeader().setFont(FONT_B14); tblChiTiet.getTableHeader().setBackground(new Color(245, 248, 252)); tblChiTiet.setShowGrid(false); tblChiTiet.setIntercellSpacing(new Dimension(0, 0));
        TableColumnModel tcm = tblChiTiet.getColumnModel(); tcm.getColumn(0).setMaxWidth(40); tcm.getColumn(1).setMinWidth(80); tcm.getColumn(1).setMaxWidth(80); tcm.getColumn(2).setMinWidth(100);

        JScrollPane scroll = new JScrollPane(tblChiTiet); scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Color.WHITE); pnlTableWrapper.add(scroll, BorderLayout.CENTER);

        JPanel pnlBottomLeft = new JPanel(new BorderLayout(0, 10)); pnlBottomLeft.setOpaque(false);
        JPanel pnlTotals = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5)); pnlTotals.setOpaque(false);

        lblTongTien        = new JLabel(); lblTongTien.setFont(FONT_14);
        lblTongKhuyenMai   = new JLabel(); lblTongKhuyenMai.setFont(FONT_14);
        lblThanhToanConLai = new JLabel(); lblThanhToanConLai.setFont(FONT_14);

        pnlTotals.add(lblTongTien); pnlTotals.add(lblTongKhuyenMai); pnlTotals.add(lblThanhToanConLai);
        pnlBottomLeft.add(pnlTotals, BorderLayout.CENTER); pnlLeft.add(pnlTableWrapper, BorderLayout.CENTER); pnlLeft.add(pnlBottomLeft, BorderLayout.SOUTH);
        return pnlLeft;
    }

    private JPanel buildRightPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout()); pnlRight.setBackground(new Color(245, 248, 252)); pnlRight.setBorder(new LineBorder(BORDER_C, 1));
        JLabel lblInfo = new JLabel("Thông tin thanh toán"); lblInfo.setFont(FONT_B14); lblInfo.setForeground(Color.WHITE); lblInfo.setOpaque(true); lblInfo.setBackground(NAVY); lblInfo.setBorder(new EmptyBorder(6, 12, 6, 12));
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); titleWrap.setOpaque(false); titleWrap.add(lblInfo); pnlRight.add(titleWrap, BorderLayout.NORTH);

        JPanel content = new JPanel(); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS)); content.setOpaque(false); content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.add(createDetailLabel("Người xử lý:", "NV001 - Nhân viên Bán Vé")); content.add(Box.createVerticalStrut(4)); content.add(createDetailLabel("Mã vé đổi:", s_maVe.isEmpty() ? "—" : s_maVe)); content.add(Box.createVerticalStrut(10)); content.add(new JSeparator()); content.add(Box.createVerticalStrut(10));

        JPanel pnlPTTTTitle = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); } };
        pnlPTTTTitle.setOpaque(false); pnlPTTTTitle.add(new JLabel("Phương thức thanh toán:") {{ setFont(FONT_14); }}, BorderLayout.WEST); content.add(pnlPTTTTitle); content.add(Box.createVerticalStrut(5));

        btnTienMat = createToggleBtn("Tiền mặt"); btnChuyenKhoan = createToggleBtn("Chuyển khoản"); ButtonGroup bg = new ButtonGroup(); bg.add(btnTienMat); bg.add(btnChuyenKhoan); btnTienMat.setSelected(true);
        JPanel pnlToggle = new JPanel(new GridLayout(1, 2, 6, 0)) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 35); } }; pnlToggle.setOpaque(false); pnlToggle.add(btnTienMat); pnlToggle.add(btnChuyenKhoan); content.add(pnlToggle); content.add(Box.createVerticalStrut(15));

        cardSwitch = new CardLayout(); pnlSwitch = new JPanel(cardSwitch) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 175); } @Override public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, 175); } }; pnlSwitch.setOpaque(false);

        JPanel pnlTienMat = new JPanel(); pnlTienMat.setLayout(new BoxLayout(pnlTienMat, BoxLayout.Y_AXIS)); pnlTienMat.setOpaque(false);
        JPanel pnlNhapTien = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 26); } }; pnlNhapTien.setOpaque(false); pnlNhapTien.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        txtTienKhachDua = new JTextField(); txtTienKhachDua.setFont(FONT_14); txtTienKhachDua.setHorizontalAlignment(JTextField.RIGHT); txtTienKhachDua.setBorder(null); txtTienKhachDua.setOpaque(false);
        txtTienKhachDua.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { try { String raw = txtTienKhachDua.getText().replaceAll("[^0-9]", ""); txtTienKhachDua.setText(raw.isEmpty() ? "" : DF.format(Double.parseDouble(raw)).replace(" VNĐ", "")); } catch (Exception ex) {} tinhTienThua(); } });
        pnlNhapTien.add(new JLabel("Khách đưa: ") {{ setFont(FONT_14); }}, BorderLayout.WEST); pnlNhapTien.add(txtTienKhachDua, BorderLayout.CENTER);

        JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 5, 5)) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 90); } }; pnlGrid.setOpaque(false);
        for (String qc : new String[]{"30,000","50,000","100,000","200,000","500,000","1,000,000","1,500,000","2,000,000"}) {
            JButton b = makeOutlineBtn(qc, null); b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setBorder(new EmptyBorder(2, 0, 2, 0)); b.addActionListener(e -> { txtTienKhachDua.setText(qc); tinhTienThua(); }); pnlGrid.add(b);
        } pnlGrid.add(new JLabel());

        JPanel pnlThua = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 26); } }; pnlThua.setOpaque(false); pnlThua.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        lblTienThua = new JLabel("0 VNĐ"); lblTienThua.setFont(FONT_B14); pnlThua.add(new JLabel("Tiền thừa/Hoàn:") {{ setFont(FONT_14); }}, BorderLayout.WEST); pnlThua.add(lblTienThua, BorderLayout.EAST);
        pnlTienMat.add(pnlNhapTien); pnlTienMat.add(Box.createVerticalStrut(6)); pnlTienMat.add(pnlGrid); pnlTienMat.add(Box.createVerticalStrut(10)); pnlTienMat.add(pnlThua);

        JPanel pnlQR = new JPanel(new GridBagLayout()); pnlQR.setOpaque(false);
        lblQR = new JLabel("", SwingConstants.CENTER); lblQR.setPreferredSize(new Dimension(140, 140)); lblQR.setMinimumSize(new Dimension(140, 140)); lblQR.setMaximumSize(new Dimension(140, 140)); lblQR.setCursor(new Cursor(Cursor.HAND_CURSOR)); lblQR.setToolTipText("Click vào để phóng to mã QR");
        lblQR.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (originalQRImageIcon != null) showZoomedQRDialog(); }}); pnlQR.add(lblQR, new GridBagConstraints());

        pnlSwitch.add(pnlTienMat, "TIEN_MAT"); pnlSwitch.add(pnlQR, "QR"); content.add(pnlSwitch);

        ActionListener ptttListener = e -> { boolean isBank = btnChuyenKhoan.isSelected(); cardSwitch.show(pnlSwitch, isBank ? "QR" : "TIEN_MAT"); if (isBank) { toggleQRCode(); startBankChecking(); } else { stopBankChecking(); tinhTienThua(); } };
        btnTienMat.addActionListener(ptttListener); btnChuyenKhoan.addActionListener(ptttListener);
        content.add(Box.createVerticalGlue()); pnlRight.add(content, BorderLayout.CENTER);
        return pnlRight;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout()); bar.setOpaque(false); bar.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 0, 0, BORDER_C), new EmptyBorder(15, 0, 0, 0)));
        JButton btnQuayLai = makeOutlineBtn("Quay lại", loadIcon("/Images/logoBack.png", 14, 14)); btnQuayLai.setBorder(new EmptyBorder(6, 16, 6, 16)); btnQuayLai.addActionListener(e -> { stopAllTimers(); appFrame.showCard("doi-ve-step-2"); });

        lblCountdown = new JLabel("Thời hạn giữ vé: --:--") { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(255, 235, 235)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6); g2.setColor(getBackground()); g2.setStroke(new BasicStroke(1.2f)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6); g2.dispose(); super.paintComponent(g); } };
        lblCountdown.setFont(FONT_B14); lblCountdown.setForeground(new Color(190, 30, 30)); lblCountdown.setOpaque(false); lblCountdown.setBackground(new Color(200, 60, 60)); lblCountdown.setBorder(new EmptyBorder(6, 12, 6, 12));

        JButton btnThanhToan = makeNavyBtn("Xác nhận & Đổi vé", loadIcon("/Images/logoGoOn.png", 14, 14)); btnThanhToan.setHorizontalTextPosition(SwingConstants.LEFT);
        btnThanhToan.addActionListener(e -> { if (s_tongThu > 0 && btnTienMat.isSelected()) { if (parseMoney(txtTienKhachDua.getText()) < s_tongThu) { JOptionPane.showMessageDialog(this, "Khách đưa thiếu tiền!", "Lỗi thanh toán", JOptionPane.ERROR_MESSAGE); return; } } xuLyHoanTatThanhToan(btnTienMat.isSelected() ? "Tiền mặt" : "Chuyển khoản"); });

        JPanel pnlL = new JPanel(new FlowLayout(FlowLayout.LEFT,  0, 0)); pnlL.setOpaque(false); pnlL.add(btnQuayLai);
        JPanel pnlR = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); pnlR.setOpaque(false); pnlR.add(lblCountdown); pnlR.add(btnThanhToan);
        bar.add(pnlL, BorderLayout.WEST); bar.add(pnlR, BorderLayout.EAST); return bar;
    }

    private void tinhToanTaiChinh() {
        long giaVeCu = 0; try { giaVeCu = Long.parseLong(s_dataCu[8].replaceAll("[^0-9]", "")); } catch (Exception e) {}
        lblTongTien.setText("<html><font color='#505050'>Giá vé cũ: </font><b>" + DF.format(giaVeCu) + "</b></html>");
        lblTongKhuyenMai.setText("<html><font color='#505050'>Giá vé mới: </font><b>" + DF.format(s_giaVeMoi) + "</b></html>");
        if (s_tongThu > 0) lblThanhToanConLai.setText("<html><font color='#505050'>Khách bù (Giá đổi): </font><font color='#C82020'><b>" + DF.format(s_tongThu) + "</b></font></html>");
        else lblThanhToanConLai.setText("<html><font color='#505050'>Hoàn trả khách: </font><font color='#1E783C'><b>" + DF.format(Math.abs(s_tongThu)) + "</b></font></html>");
        if (btnChuyenKhoan != null && btnChuyenKhoan.isSelected()) toggleQRCode(); else tinhTienThua();
    }

    private void tinhTienThua() {
        if (s_tongThu <= 0 || txtTienKhachDua == null || lblTienThua == null) return;
        double tienKhach = parseMoney(txtTienKhachDua.getText()); double tienThua  = tienKhach - s_tongThu;
        if (tienKhach == 0) { lblTienThua.setText("0 VNĐ"); lblTienThua.setForeground(Color.BLACK); }
        else if (tienThua < 0) { lblTienThua.setText("Còn thiếu: " + DF.format(Math.abs(tienThua))); lblTienThua.setForeground(Color.RED); }
        else { lblTienThua.setText(DF.format(tienThua)); lblTienThua.setForeground(Color.BLACK); }
    }

    // --- LOGIC HOÀN TẤT ĐỔI VÉ ---
    private void xuLyHoanTatThanhToan(String hinhThuc) {
        stopAllTimers();
        String maDon;
        try (Connection conn = Connect_DB.getInstance().getConnection()) { maDon = MaTuDong.taoMaDon(conn, LocalDate.now()); } catch (Exception e) { return; }

        // Mấu chốt 1: Lưu hóa đơn mới, thống kê bằng đúng tổng thu
        String sqlInsertHD =
                "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) " +
                        "SELECT ?, GETDATE(), hd.maNV, hd.maKH, ?, ?, ? " +
                        "FROM HoaDon hd JOIN Ve v ON v.maHoaDon = hd.maHoaDon WHERE v.maVe = ?";

        String sqlFindGhe = "SELECT TOP 1 maGhe FROM Ghe WHERE maToaTau = ? AND soGhe = ?";

        // Mấu chốt 2: Cập nhật thông tin vé cũ, nhưng giữ nguyên Trạng Thái Đã Thanh Toán
        // VÀ chuyển maHoaDon của vé sang mã Hóa đơn mới
        String sqlUpdateVe =
                "UPDATE Ve SET maChuyenTau = ?, maGhe = ?, giaVe = ?, maHoaDon = ?, trangThaiVe = N'Đã thanh toán' WHERE maVe = ?";
        String sqlInsertDon =
                "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, tienHoanTra, loaiDon, maVe) " +
                        "VALUES (?, ?, GETDATE(), 0, 'DON_DOI', ?)";

        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlInsertHD)) {
                    ps.setString(1, maDon);
                    ps.setLong  (2, s_giaVeMoi); // Lưu tổng tiền cuối (vé cũ + tiền bù)
                    ps.setLong  (3, s_tongThu);  // Tiền nhận = tiền khách bù thêm
                    ps.setString(4, hinhThuc);
                    ps.setString(5, s_maVe);
                    ps.executeUpdate();
                }

                String maGheThuc = s_gheMoiDB;
                try {
                    int tIdx = s_gheMoiDB.indexOf('T');
                    if (tIdx > 1) {
                        String soGheNum = String.valueOf(Integer.parseInt(s_gheMoiDB.substring(1, tIdx)));
                        String maToaPart = s_gheMoiDB.substring(tIdx);
                        try (PreparedStatement psFindGhe = conn.prepareStatement(sqlFindGhe)) {
                            psFindGhe.setString(1, maToaPart); psFindGhe.setString(2, soGheNum);
                            try (ResultSet rs = psFindGhe.executeQuery()) {
                                if (rs.next()) maGheThuc = rs.getString("maGhe");
                                else throw new Exception("Không tìm thấy ghế " + s_gheMoiDB + " trong DB!");
                            }
                        }
                    }
                } catch (NumberFormatException ignored) {}

                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateVe)) {
                    ps.setString(1, s_chuyenMoi);
                    ps.setString(2, maGheThuc);
                    ps.setLong  (3, s_giaVeMoi);
                    ps.setString(4, maDon);
                    ps.setString(5, s_maVe);
                    int rows = ps.executeUpdate();
                    if (rows == 0) throw new Exception("Không tìm thấy vé " + s_maVe);
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlInsertDon)) {
                    ps.setString(1, maDon); ps.setLong(2, s_tongThu); ps.setString(3, s_maVe); ps.executeUpdate();
                }

                conn.commit();
                xuatHoaDonPDF(maDon);
                xuatVePDF(s_maVe);
                JOptionPane.showMessageDialog(this, "Đổi vé thành công!\nMã hóa đơn mới: " + maDon, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                appFrame.showCard("doi-tra");

            } catch (Exception ex) { conn.rollback(); throw ex; }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleQRCode() { if (btnChuyenKhoan.isSelected() && s_tongThu > 0) { lblQR.setIcon(null); lblQR.setText("Đang tạo mã VietQR..."); new SwingWorker<ImageIcon, Void>() { @Override protected ImageIcon doInBackground() throws Exception { String info = URLEncoder.encode("Thanh toan doi ve " + s_maVe, StandardCharsets.UTF_8); String url  = String.format("https://img.vietqr.io/image/970422-0382588430-compact2.png?amount=%d&addInfo=%s", s_tongThu, info); return new ImageIcon(new URL(url)); } @Override protected void done() { try { originalQRImageIcon = get(); lblQR.setIcon(new ImageIcon(originalQRImageIcon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH))); lblQR.setText(""); } catch (Exception e) { lblQR.setIcon(null); lblQR.setText("Lỗi load QR!"); } } }.execute(); } }
    private void showZoomedQRDialog() { Window ancestor = SwingUtilities.getWindowAncestor(this); JDialog dialog = new JDialog(ancestor, "Quét mã thanh toán", Dialog.ModalityType.APPLICATION_MODAL); dialog.setUndecorated(true); JPanel glass = new JPanel(new GridBagLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(new Color(0,0,0,150)); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose(); } }; glass.setOpaque(false); JPanel box = new JPanel(new BorderLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16); g2.dispose(); } }; box.setOpaque(false); box.setPreferredSize(new Dimension(420,460)); box.setBorder(new EmptyBorder(10,20,20,20)); JPanel hdr = new JPanel(new BorderLayout()); hdr.setOpaque(false); JLabel ttl = new JLabel("Mã thanh toán Đổi vé"); ttl.setFont(new Font("Segoe UI",Font.BOLD,15)); ttl.setForeground(NAVY); JButton close = new JButton("✕") { @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isRollover()?new Color(255,230,230):Color.WHITE); g2.fillOval(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g); } }; close.setFont(new Font("Segoe UI",Font.BOLD,14)); close.setForeground(new Color(200,40,40)); close.setContentAreaFilled(false); close.setBorderPainted(false); close.setFocusPainted(false); close.setPreferredSize(new Dimension(28,28)); close.addActionListener(e->dialog.dispose()); hdr.add(ttl,BorderLayout.WEST); hdr.add(close,BorderLayout.EAST); JLabel bigQR = new JLabel(new ImageIcon(originalQRImageIcon.getImage().getScaledInstance(350,350,Image.SCALE_SMOOTH))); bigQR.setHorizontalAlignment(SwingConstants.CENTER); JLabel foot = new JLabel("Mở ứng dụng Ngân hàng quét để hoàn tất",SwingConstants.CENTER); foot.setFont(new Font("Segoe UI",Font.ITALIC,11)); foot.setForeground(Color.GRAY); box.add(hdr,BorderLayout.NORTH); box.add(bigQR,BorderLayout.CENTER); box.add(foot,BorderLayout.SOUTH); glass.add(box); dialog.setContentPane(glass); dialog.getRootPane().registerKeyboardAction(e->dialog.dispose(),KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW); dialog.setSize(ancestor.getSize()); dialog.setLocation(ancestor.getLocation()); dialog.setVisible(true); }
    private void startBankChecking() { if (bankCheckTimer != null) bankCheckTimer.stop(); bankCheckTimer = new javax.swing.Timer(5000, new ActionListener() { private int n = 0; @Override public void actionPerformed(ActionEvent e) { if (++n >= 3) { bankCheckTimer.stop(); for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isVisible()) w.dispose(); xuLyHoanTatThanhToan("Chuyển khoản VietQR"); } } }); bankCheckTimer.start(); }
    private void stopBankChecking() { if (bankCheckTimer != null) bankCheckTimer.stop(); }
    private void stopAllTimers() { if (countdownTimer != null) countdownTimer.stop(); if (bankCheckTimer != null) bankCheckTimer.stop(); }
    private void startCountdown() { if (countdownTimer != null) countdownTimer.stop(); countdownTimer = new javax.swing.Timer(1000, e -> { if (--secondsLeft <= 0) { stopAllTimers(); lblCountdown.setText("Hết thời gian!"); JOptionPane.showMessageDialog(this,"Hết thời gian giữ vé!","Hết thời gian",JOptionPane.WARNING_MESSAGE); appFrame.showCard("doi-tra"); } else { int m=secondsLeft/60,s=secondsLeft%60; lblCountdown.setForeground(secondsLeft<=300?new Color(160,0,0):new Color(190,30,30)); lblCountdown.setText(String.format("Thời hạn giữ vé: %02d:%02d",m,s)); } }); countdownTimer.start(); int m=secondsLeft/60,s=secondsLeft%60; lblCountdown.setText(String.format("Thời hạn giữ vé: %02d:%02d",m,s)); }
    private JPanel createDetailLabel(String title, String value) { JPanel p = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); } }; p.setOpaque(false); p.add(new JLabel(title) {{ setFont(FONT_14); setForeground(new Color(80,80,80)); }}, BorderLayout.WEST); p.add(new JLabel(value) {{ setFont(FONT_B14); setForeground(Color.BLACK); }}, BorderLayout.EAST); return p; }
    private JToggleButton createToggleBtn(String text) { JToggleButton b = new JToggleButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); if (isSelected()) { g2.setColor(new Color(240,246,255)); g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4); g2.setColor(NAVY); g2.setStroke(new BasicStroke(1.8f)); g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,4,4); } else { g2.setColor(isEnabled()?Color.WHITE:new Color(240,240,240)); g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4); g2.setColor(BORDER_C); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,4,4); } g2.dispose(); super.paintComponent(g); } }; b.addChangeListener(e -> { if(b.isSelected()){b.setFont(FONT_B14);b.setForeground(NAVY);}else{b.setFont(FONT_14);b.setForeground(new Color(80,80,80));} }); b.setFont(FONT_14); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(6,12,6,12)); return b; }
    private JButton makeNavyBtn(String text, Icon icon) { JButton b = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isPressed()?new Color(18,42,85):getModel().isRollover()?new Color(38,68,128):NAVY); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.dispose(); super.paintComponent(g); } }; if (icon!=null) b.setIcon(icon); b.setFont(FONT_B14); b.setForeground(Color.WHITE); b.setIconTextGap(8); b.setBorder(new EmptyBorder(6,18,6,18)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
    private JButton makeOutlineBtn(String text, Icon icon) { JButton b = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isPressed()?new Color(220,230,245):getModel().isRollover()?new Color(230,240,250):new Color(242,247,252)); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6); g2.setColor(NAVY); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6); g2.dispose(); super.paintComponent(g); } }; if (icon!=null) b.setIcon(icon); b.setFont(FONT_14); b.setForeground(NAVY); b.setIconTextGap(8); b.setContentAreaFilled(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
    private double parseMoney(String s) { try { return Double.parseDouble(s.replaceAll("[^0-9]","")); } catch (Exception e) { return 0; } }
    private Icon loadIcon(String path, int w, int h) { try { URL u=getClass().getResource(path); if(u!=null) return new ImageIcon(new ImageIcon(u).getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH)); } catch(Exception ignored){} return null; }






    private void xuatHoaDonPDF(String maDon) {
        try (java.sql.Connection conn = connect_DB.Connect_DB.getConnection()) {
            String sqlHD = "SELECT h.tongTien, h.tienNhan, h.phuongThucThanhToan, " +
                    "k.hoTenKH, k.sdt " +
                    "FROM HoaDon h LEFT JOIN KhachHang k ON h.maKH = k.maKH " +
                    "WHERE h.maHoaDon = ?";
            String tenKH = "Khách vãng lai", sdtKH = "", hinhThuc = "";
            double tongTien = 0;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlHD)) {
                ps.setString(1, maDon);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tenKH    = rs.getString("hoTenKH")             != null ? rs.getString("hoTenKH")             : "Khách vãng lai";
                        sdtKH    = rs.getString("sdt")                 != null ? rs.getString("sdt")                 : "";
                        hinhThuc = rs.getString("phuongThucThanhToan") != null ? rs.getString("phuongThucThanhToan") : "";
                        tongTien = rs.getDouble("tongTien");
                    }
                }
            }

            String sqlVe = "SELECT v.maVe, v.loaiVe, v.giaVe, g.loaiGhe, " +
                    "gaDi.tenGa AS gaDi, gaDen.tenGa AS gaDen " +
                    "FROM Ve v " +
                    "JOIN Ghe g              ON v.maGhe       = g.maGhe " +
                    "JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                    "JOIN Ga gaDi            ON dt.maGaDi      = gaDi.maGa " +
                    "JOIN Ga gaDen           ON dt.maGaDen     = gaDen.maGa " +
                    "WHERE v.maHoaDon = ?";

            java.io.File folder = new java.io.File("HoaDon");
            if (!folder.exists()) folder.mkdir();
            java.io.File pdfFile = new java.io.File(folder, maDon + ".pdf");

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(pdfFile));
            document.open();

            com.itextpdf.text.pdf.BaseFont bf = com.itextpdf.text.pdf.BaseFont.createFont(
                    "c:/windows/fonts/arial.ttf",
                    com.itextpdf.text.pdf.BaseFont.IDENTITY_H,
                    com.itextpdf.text.pdf.BaseFont.EMBEDDED);
            com.itextpdf.text.Font fontTitle  = new com.itextpdf.text.Font(bf, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fontBold   = new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.BOLD);
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
            for (String h : new String[]{"STT","Tên dịch vụ","Loại vé","Mã vé","Chiều","Giá vé","ĐVT","SL","Khuyến mãi","Thành tiền"}) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h, fontBold));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                cell.setPaddingBottom(6);
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
                table.addCell(cell);
            }

            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            int stt = 1;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlVe)) {
                ps.setString(1, maDon);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double giaVe = rs.getDouble("giaVe");
                        String lv = rs.getString("loaiVe");
                        String loaiVeHienThi = "MOT_CHIEU".equals(lv) ? "Một chiều"
                                : "KHU_HOI".equals(lv)   ? "Khứ hồi"
                                  : (lv != null ? lv : "");
                        pdfAddCell(table, fontNormal, String.valueOf(stt++), com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, "Vé HK trực tiếp tại nhà ga", com.itextpdf.text.Element.ALIGN_LEFT);
                        pdfAddCell(table, fontNormal, loaiVeHienThi, com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, rs.getString("maVe") != null ? rs.getString("maVe") : "", com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, rs.getString("gaDi") + " → " + rs.getString("gaDen"), com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, df.format(giaVe), com.itextpdf.text.Element.ALIGN_RIGHT);
                        pdfAddCell(table, fontNormal, "Vé", com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, "1", com.itextpdf.text.Element.ALIGN_CENTER);
                        pdfAddCell(table, fontNormal, "0", com.itextpdf.text.Element.ALIGN_RIGHT);
                        pdfAddCell(table, fontNormal, df.format(giaVe), com.itextpdf.text.Element.ALIGN_RIGHT);
                    }
                }
            }
            document.add(table);
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            com.itextpdf.text.Paragraph pTong = new com.itextpdf.text.Paragraph("Tổng tiền: " + df.format(tongTien), fontBold);
            pTong.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pTong);

            com.itextpdf.text.Paragraph pChu = new com.itextpdf.text.Paragraph("Bằng chữ: " + docSoThanh((long) tongTien), fontItalic);
            pChu.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(pChu);

            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));
            document.add(new com.itextpdf.text.Paragraph("Ghi chú: ......................................................................................................................................", fontNormal));
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));
            document.add(new com.itextpdf.text.Paragraph(" ", fontNormal));

            com.itextpdf.text.pdf.PdfPTable signTable = new com.itextpdf.text.pdf.PdfPTable(2);
            signTable.setWidthPercentage(100);
            com.itextpdf.text.pdf.PdfPCell cBuyer  = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Người mua hàng\n(Ký, ghi rõ họ tên)", fontNormal));
            com.itextpdf.text.pdf.PdfPCell cSeller = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Người bán hàng\n(Ký, ghi rõ họ tên)", fontNormal));
            cBuyer.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);  cBuyer.setBorder(com.itextpdf.text.pdf.PdfPCell.NO_BORDER);
            cSeller.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER); cSeller.setBorder(com.itextpdf.text.pdf.PdfPCell.NO_BORDER);
            signTable.addCell(cBuyer);
            signTable.addCell(cSeller);
            document.add(signTable);

            document.close();
            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Lỗi xuất PDF: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
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
        String[] chu   = {"", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
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

    private void xuatVePDF(String maVe) {
        try (java.sql.Connection conn = connect_DB.Connect_DB.getConnection()) {
            // Query thông tin vé
            String sql = "SELECT v.loaiVe, v.maGhe, k.hoTenKH, k.cccd, " +
                    "t.tenTau, ct.thoiGianKhoiHanh, " +
                    "gaDi.tenGa AS gaDi, gaDen.tenGa AS gaDen, " +
                    "g.soGhe, tt.soToa, g.loaiGhe " +
                    "FROM Ve v " +
                    "JOIN KhachHang k         ON v.maKH         = k.maKH " +
                    "JOIN ChiTietChuyenTau ct  ON v.maChuyenTau  = ct.maChuyenTau " +
                    "JOIN ChuyenTau c          ON ct.maChuyenTau = c.maChuyenTau " +
                    "JOIN Tau t                ON c.maTau        = t.maTau " +
                    "JOIN Ga gaDi             ON ct.maGaDi       = gaDi.maGa " +
                    "JOIN Ga gaDen            ON ct.maGaDen      = gaDen.maGa " +
                    "JOIN Ghe g               ON v.maGhe         = g.maGhe " +
                    "JOIN ToaTau tt            ON g.maToaTau      = tt.maToaTau " +
                    "WHERE v.maVe = ?";

            String loaiVe="", tenKH="", cccd="", tenTau="", ngayDi="", gioDi="";
            String gaDi="", gaDen="", soGhe="", soToa="", loaiGheRaw="";

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maVe);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        loaiVe  = rs.getString("loaiVe")  != null ? rs.getString("loaiVe")  : "";
                        tenKH   = rs.getString("hoTenKH") != null ? rs.getString("hoTenKH") : "";
                        String rawCccd = rs.getString("cccd");
                        cccd = cheCCCDVe(rawCccd != null ? rawCccd : "");
                        tenTau  = rs.getString("tenTau")  != null ? rs.getString("tenTau")  : "";
                        gaDi    = rs.getString("gaDi")    != null ? rs.getString("gaDi")    : "";
                        gaDen   = rs.getString("gaDen")   != null ? rs.getString("gaDen")   : "";
                        soGhe   = rs.getString("soGhe")   != null ? rs.getString("soGhe")   : "";
                        soToa   = String.valueOf(rs.getInt("soToa"));
                        loaiGheRaw = rs.getString("loaiGhe") != null ? rs.getString("loaiGhe") : "";
                        java.sql.Timestamp ts = rs.getTimestamp("thoiGianKhoiHanh");
                        if (ts != null) {
                            ngayDi = new java.text.SimpleDateFormat("dd/MM/yyyy").format(ts);
                            gioDi  = new java.text.SimpleDateFormat("HH:mm").format(ts);
                        }
                    }
                }
            }

            // Chuẩn hóa loại vé
            String loaiVeHienThi = "MOT_CHIEU".equals(loaiVe) ? "Một chiều"
                    : "KHU_HOI".equals(loaiVe)   ? "Khứ hồi" : loaiVe;
            String loaiGheHienThi = switch (loaiGheRaw.trim()) {
                case "GHE_CUNG"   -> "Ghế cứng";
                case "GHE_MEM"    -> "Ghế mềm";
                case "GIUONG_NAM" -> "Giường nằm";
                default           -> loaiGheRaw;
            };
            String hangVe = "Giường nằm".equals(loaiGheHienThi) ? "VIP" : "Thường";

            // Tạo PDF - cùng khổ trang với DatVeGUI3
            java.io.File folder = new java.io.File("Ve");
            if (!folder.exists()) folder.mkdir();
            java.io.File pdfFile = new java.io.File(folder, "Ve_" + maVe + ".pdf");

            com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(240, 580);
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document(pageSize, 10, 10, 12, 12);
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(pdfFile));
            doc.open();

            com.itextpdf.text.pdf.BaseFont bf = com.itextpdf.text.pdf.BaseFont.createFont(
                    "c:/windows/fonts/arial.ttf", com.itextpdf.text.pdf.BaseFont.IDENTITY_H, com.itextpdf.text.pdf.BaseFont.EMBEDDED);
            com.itextpdf.text.Font fCongTy  = new com.itextpdf.text.Font(bf, 10, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fGaTen   = new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fTieuDe  = new com.itextpdf.text.Font(bf, 12, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fSub     = new com.itextpdf.text.Font(bf,  8, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fGaLabel = new com.itextpdf.text.Font(bf,  8, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fGaValue = new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fLabel   = new com.itextpdf.text.Font(bf,  9, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fValue   = new com.itextpdf.text.Font(bf,  9, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fMaVe    = new com.itextpdf.text.Font(bf,  8, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.GRAY);

            com.itextpdf.text.pdf.PdfPTable wrap = new com.itextpdf.text.pdf.PdfPTable(1);
            wrap.setWidthPercentage(100);
            com.itextpdf.text.pdf.PdfPCell wc = new com.itextpdf.text.pdf.PdfPCell();
            wc.setBorder(com.itextpdf.text.Rectangle.BOX);
            wc.setBorderWidth(0.8f);
            wc.setPaddingLeft(10); wc.setPaddingRight(10);
            wc.setPaddingTop(10);  wc.setPaddingBottom(10);

            // Header
            com.itextpdf.text.Paragraph pCty = new com.itextpdf.text.Paragraph("TỔNG CÔNG TY ĐƯỜNG SẮT VIỆT NAM", fCongTy);
            pCty.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            wc.addElement(pCty);

            com.itextpdf.text.Paragraph pGaTen = new com.itextpdf.text.Paragraph("GA DIÊU TRÌ", fGaTen);
            pGaTen.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            wc.addElement(pGaTen);

            // Đường kẻ
            com.itextpdf.text.pdf.PdfPTable lineTable = new com.itextpdf.text.pdf.PdfPTable(1);
            lineTable.setWidthPercentage(100); lineTable.setSpacingBefore(4); lineTable.setSpacingAfter(4);
            com.itextpdf.text.pdf.PdfPCell lineCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(""));
            lineCell.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
            lineCell.setBorderWidth(0.5f);
            lineCell.setBorderColor(com.itextpdf.text.BaseColor.GRAY);
            lineCell.setPaddingBottom(0);
            lineTable.addCell(lineCell);
            wc.addElement(lineTable);

            com.itextpdf.text.Paragraph pTieuDe = new com.itextpdf.text.Paragraph("VÉ LÊN TÀU HỎA", fTieuDe);
            pTieuDe.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            wc.addElement(pTieuDe);

            com.itextpdf.text.Paragraph pBoarding = new com.itextpdf.text.Paragraph("BOARDING TICKET", fSub);
            pBoarding.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            pBoarding.setSpacingAfter(5);
            wc.addElement(pBoarding);

            // Barcode
            com.itextpdf.text.pdf.Barcode128 barcode = new com.itextpdf.text.pdf.Barcode128();
            barcode.setCode(maVe);
            barcode.setBarHeight(32f); barcode.setX(1.0f);
            barcode.setBaseline(0f); barcode.setAltText("");
            java.awt.Image awtImg = barcode.createAwtImage(java.awt.Color.BLACK, java.awt.Color.WHITE);
            com.itextpdf.text.Image imgBar = com.itextpdf.text.Image.getInstance(awtImg, null);
            imgBar.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            imgBar.scaleToFit(240f, 38f);
            wc.addElement(imgBar);

            com.itextpdf.text.Paragraph pMaVe = new com.itextpdf.text.Paragraph("Mã vé/TicketID: " + maVe, fMaVe);
            pMaVe.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            pMaVe.setSpacingAfter(6);
            wc.addElement(pMaVe);

            // Ga đi / Ga đến
            com.itextpdf.text.pdf.PdfPTable gaTable = new com.itextpdf.text.pdf.PdfPTable(2);
            gaTable.setWidthPercentage(100); gaTable.setSpacingBefore(2); gaTable.setSpacingAfter(2);

            com.itextpdf.text.pdf.PdfPCell cGaDi = new com.itextpdf.text.pdf.PdfPCell();
            cGaDi.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); cGaDi.setPaddingLeft(16); cGaDi.setPaddingBottom(2);
            com.itextpdf.text.Paragraph pDiLbl = new com.itextpdf.text.Paragraph("Ga đi", fGaLabel);
            pDiLbl.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT); cGaDi.addElement(pDiLbl);
            com.itextpdf.text.Paragraph pDiVal = new com.itextpdf.text.Paragraph(gaDi.toUpperCase(), fGaValue);
            pDiVal.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT); cGaDi.addElement(pDiVal);
            gaTable.addCell(cGaDi);

            com.itextpdf.text.pdf.PdfPCell cGaDen = new com.itextpdf.text.pdf.PdfPCell();
            cGaDen.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); cGaDen.setPaddingRight(16); cGaDen.setPaddingBottom(2);
            com.itextpdf.text.Paragraph pDenLbl = new com.itextpdf.text.Paragraph("Ga đến", fGaLabel);
            pDenLbl.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT); cGaDen.addElement(pDenLbl);
            com.itextpdf.text.Paragraph pDenVal = new com.itextpdf.text.Paragraph(gaDen.toUpperCase(), fGaValue);
            pDenVal.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT); cGaDen.addElement(pDenVal);
            gaTable.addCell(cGaDen);

            com.itextpdf.text.pdf.PdfPCell cSep = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(""));
            cSep.setColspan(2); cSep.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
            cSep.setBorderWidth(0.5f); cSep.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY); cSep.setPaddingBottom(3);
            gaTable.addCell(cSep);
            wc.addElement(gaTable);

            // Thông tin chi tiết
            com.itextpdf.text.pdf.PdfPTable infoTable = new com.itextpdf.text.pdf.PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.35f, 1.65f});
            infoTable.setSpacingBefore(3);

            String[][] details = {
                    {"Số hiệu tàu/Train ID:", tenTau},
                    {"Ngày khởi hành/Date:", ngayDi},
                    {"Giờ khởi hành/Time:", gioDi},
                    {"Số Toa/Coach:", soToa},
                    {"Loại ghế/Type:", loaiGheHienThi},
                    {"Số ghế/Seat:", soGhe},
                    {"Loại vé/Ticket:", loaiVeHienThi},
                    {"Hạng vé/Class:", hangVe},
                    {"Họ Tên/Name:", tenKH.toUpperCase()},
                    {"Giấy tờ/Passport:", cccd},
            };

            for (String[] d : details) {
                com.itextpdf.text.pdf.PdfPCell cL = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(d[0], fLabel));
                cL.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); cL.setPaddingLeft(14); cL.setPaddingBottom(3);
                infoTable.addCell(cL);
                com.itextpdf.text.pdf.PdfPCell cR = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(d[1], fValue));
                cR.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); cR.setPaddingLeft(30); cR.setPaddingBottom(3);
                infoTable.addCell(cR);
            }
            wc.addElement(infoTable);

            wrap.addCell(wc);
            doc.add(wrap);
            doc.close();

            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Lỗi xuất vé PDF: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private String cheCCCDVe(String cccd) {
        if (cccd != null && cccd.length() > 4) {
            int length = cccd.length();
            if (length == 12) return cccd.substring(0, 4) + "****" + cccd.substring(8);
            else if (length > 6) { int v = (length-4)/2; return cccd.substring(0,v)+"****"+cccd.substring(v+4); }
        }
        return cccd;
    }

}