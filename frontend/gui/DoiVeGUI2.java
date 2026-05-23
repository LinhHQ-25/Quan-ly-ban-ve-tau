package gui;

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
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;

import connect_DB.Connect_DB;
import service.AuthService;
import util.MaTuDong;

public class DoiVeGUI2 extends JPanel {

    private static final Color NAVY     = new Color(28, 57, 110);
    private static final Color BG       = new Color(242, 247, 252);
    private static final Color BORDER_C = new Color(180, 205, 230);
    private static final java.awt.Font FONT_14  = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
    private static final java.awt.Font FONT_B14 = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
    private static final DecimalFormat DF = new DecimalFormat("#,### VNĐ");
    private static final DecimalFormat DF_NO_CURRENCY = new DecimalFormat("#,###");

    private static final String BANK_ID = "MB";
    private static final String ACCOUNT_NO = "0382588430";
    private static final String ACCOUNT_NAME = "MB Bank";
    private static final String CASSO_API_KEY = "AK_CS.69d49310536411f1ad2d7bbf51f870c4.1OR4aZOPpK4BslQXgsgNQGlFiMwe8EDKc6Tuva6vzVcTf7ssLfssoXfn5vVKU27z4bemHq6E";

    private static String   s_maVe          = "";
    private        String   maVeMoiHienThi  = "";
    private static String[] s_dataCu        = new String[0];
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
        s_tongThu       = tongThu;
        s_giaVeMoi      = giaVeMoi;
    }

    private final AppFrame appFrame;
    private JTable            tblChiTiet;
    private DefaultTableModel modelChiTiet;

    // Các label tài chính
    private JLabel            lblGiaVeCu, lblGiaVeMoi, lblPhiDoiVe, lblKhachBu, lblTongTienTT;
    private JLabel            lblQR, lblCountdown;
    private JToggleButton     btnTienMat, btnChuyenKhoan;
    private JPanel            pnlSwitch, pnlTienMat, pnlQR;
    private CardLayout        cardSwitch;
    private JTextField        txtTienKhachDua;
    private JLabel            lblTienThua;
    private javax.swing.Timer countdownTimer, bankCheckTimer;
    private int               secondsLeft = 600;
    private ImageIcon         originalQRImageIcon = null;

    private String tenKH = "Khách vãng lai", sdtKH = "N/A", maKH = "N/A";
    private String maDon = taoMaHoaDon();

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
        loadKhachHangInfo();

        if (maVeMoiHienThi.isEmpty()) {
            maVeMoiHienThi = "VE" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        }

        modelChiTiet.setRowCount(0);

        long giaVeCu = 0;
        try { giaVeCu = Long.parseLong(s_dataCu[8].replaceAll("[^0-9]", "")); }
        catch (Exception e) {
            try { giaVeCu = Long.parseLong(s_dataCu[7].replaceAll("[^0-9]", "")); } catch (Exception ex) {}
        }

        long tienBu   = s_tongThu;
        long tongTien = giaVeCu + tienBu;

        modelChiTiet.addRow(new Object[]{
                1, s_maVe, maVeMoiHienThi, s_chuyenMoi, s_gheMoiHienThi, s_ngayMoi,
                DF.format(giaVeCu),
                DF.format(s_giaVeMoi),
                DF.format(tongTien)
        });

        tinhToanTaiChinh(giaVeCu);

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

    private void loadKhachHangInfo() {
        String sql = "SELECT kh.maKH, kh.hoTenKH, kh.sdt FROM Ve v " +
                "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                "JOIN KhachHang kh ON hd.maKH = kh.maKH WHERE v.maVe = ?";
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
        } catch (Exception e) {}
    }

    private JPanel buildLeftPanel() {
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10)); pnlLeft.setOpaque(false);
        JPanel pnlTableWrapper = new JPanel(new BorderLayout()); pnlTableWrapper.setBackground(Color.WHITE); pnlTableWrapper.setBorder(new LineBorder(BORDER_C, 1, true));

        JLabel lblChiTiet = new JLabel("Chi tiết Hóa đơn Đổi Vé");
        lblChiTiet.setFont(FONT_B14); lblChiTiet.setForeground(Color.WHITE); lblChiTiet.setOpaque(true); lblChiTiet.setBackground(NAVY); lblChiTiet.setBorder(new EmptyBorder(6, 12, 6, 12));
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); titleWrap.setOpaque(false); titleWrap.add(lblChiTiet); pnlTableWrapper.add(titleWrap, BorderLayout.NORTH);

        String[] cols = {"STT","Mã vé cũ","Mã vé mới","Mã chuyến","Ghế mới","Ngày/Giờ KH","Giá vé cũ","Giá vé mới","Tổng tiền"};
        modelChiTiet = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        tblChiTiet = new JTable(modelChiTiet) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) { c.setBackground(new Color(210, 228, 245)); c.setForeground(Color.BLACK); }
                else { c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255)); c.setForeground(Color.BLACK); } return c;
            }
        };
        tblChiTiet.setRowHeight(35); tblChiTiet.setFont(FONT_14); tblChiTiet.getTableHeader().setFont(FONT_B14); tblChiTiet.getTableHeader().setBackground(new Color(245, 248, 252)); tblChiTiet.setShowGrid(false); tblChiTiet.setIntercellSpacing(new Dimension(0, 0));

        TableColumnModel tcm = tblChiTiet.getColumnModel();
        tcm.getColumn(0).setMaxWidth(40);
        tcm.getColumn(1).setMinWidth(85); tcm.getColumn(1).setMaxWidth(85);
        tcm.getColumn(2).setMinWidth(85); tcm.getColumn(2).setMaxWidth(85);
        tcm.getColumn(3).setMinWidth(90);

        JScrollPane scroll = new JScrollPane(tblChiTiet); scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Color.WHITE); pnlTableWrapper.add(scroll, BorderLayout.CENTER);

        JPanel pnlBottomLeft = new JPanel(new BorderLayout(0, 10)); pnlBottomLeft.setOpaque(false);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); pnlBtns.setOpaque(false);
        JButton btnGhiChu = makeNavyBtn("Ghi chú", null);
        btnGhiChu.addActionListener(e -> JOptionPane.showInputDialog(this, "Nhập ghi chú cho hóa đơn đổi vé:"));
        pnlBtns.add(btnGhiChu);
        pnlBottomLeft.add(pnlBtns, BorderLayout.NORTH);

        JPanel pnlTotals = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 5)); pnlTotals.setOpaque(false);
        lblGiaVeCu    = new JLabel(); lblGiaVeCu.setFont(FONT_14);
        lblGiaVeMoi   = new JLabel(); lblGiaVeMoi.setFont(FONT_14);
        lblPhiDoiVe   = new JLabel(); lblPhiDoiVe.setFont(FONT_14);
        lblKhachBu    = new JLabel(); lblKhachBu.setFont(FONT_14);
        lblTongTienTT = new JLabel(); lblTongTienTT.setFont(FONT_B14);

        pnlTotals.add(lblGiaVeCu);
        pnlTotals.add(lblGiaVeMoi);
        pnlTotals.add(lblPhiDoiVe);
        pnlTotals.add(lblKhachBu);
        pnlTotals.add(lblTongTienTT);

        pnlBottomLeft.add(pnlTotals, BorderLayout.CENTER); pnlLeft.add(pnlTableWrapper, BorderLayout.CENTER); pnlLeft.add(pnlBottomLeft, BorderLayout.SOUTH);
        return pnlLeft;
    }

    private JPanel buildRightPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout()); pnlRight.setBackground(new Color(245, 248, 252)); pnlRight.setBorder(new LineBorder(BORDER_C, 1));
        JLabel lblInfo = new JLabel("Thông tin hóa đơn"); lblInfo.setFont(FONT_B14); lblInfo.setForeground(Color.WHITE); lblInfo.setOpaque(true); lblInfo.setBackground(NAVY); lblInfo.setBorder(new EmptyBorder(6, 12, 6, 12));
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); titleWrap.setOpaque(false); titleWrap.add(lblInfo); pnlRight.add(titleWrap, BorderLayout.NORTH);

        JPanel content = new JPanel(); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS)); content.setOpaque(false); content.setBorder(new EmptyBorder(10, 10, 10, 10));

        String maNV = AuthService.getCurrentMaNV() != null ? AuthService.getCurrentMaNV() : "N/A";
        String tenNV = AuthService.getCurrentHoTen() != null ? AuthService.getCurrentHoTen() : "N/A";

        content.add(createDetailLabel("Mã nhân viên:", maNV)); content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Tên nhân viên:", tenNV)); content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Mã khách hàng:", maKH)); content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Tên khách hàng:", tenKH)); content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Số điện thoại:", sdtKH)); content.add(Box.createVerticalStrut(10));
        content.add(new JSeparator()); content.add(Box.createVerticalStrut(10));

        JPanel pnlPTTTTitle = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); } };
        pnlPTTTTitle.setOpaque(false); pnlPTTTTitle.add(new JLabel("Phương thức thanh toán:") {{ setFont(FONT_14); }}, BorderLayout.WEST); content.add(pnlPTTTTitle); content.add(Box.createVerticalStrut(5));

        btnTienMat = createToggleBtn("Tiền mặt"); btnChuyenKhoan = createToggleBtn("Chuyển khoản");
        ButtonGroup bg = new ButtonGroup(); bg.add(btnTienMat); bg.add(btnChuyenKhoan); btnTienMat.setSelected(true);
        JPanel pnlToggle = new JPanel(new GridLayout(1, 2, 6, 0)) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 35); } }; pnlToggle.setOpaque(false); pnlToggle.add(btnTienMat); pnlToggle.add(btnChuyenKhoan); content.add(pnlToggle); content.add(Box.createVerticalStrut(15));

        cardSwitch = new CardLayout(); pnlSwitch = new JPanel(cardSwitch) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 175); } @Override public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, 175); } }; pnlSwitch.setOpaque(false);

        pnlTienMat = new JPanel(); pnlTienMat.setLayout(new BoxLayout(pnlTienMat, BoxLayout.Y_AXIS)); pnlTienMat.setOpaque(false);
        JPanel pnlNhapTien = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 26); } }; pnlNhapTien.setOpaque(false); pnlNhapTien.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        txtTienKhachDua = new JTextField(); txtTienKhachDua.setFont(FONT_14); txtTienKhachDua.setHorizontalAlignment(JTextField.RIGHT); txtTienKhachDua.setBorder(null); txtTienKhachDua.setOpaque(false);
        txtTienKhachDua.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { try { String raw = txtTienKhachDua.getText().replaceAll("[^0-9]", ""); txtTienKhachDua.setText(raw.isEmpty() ? "" : DF_NO_CURRENCY.format(Double.parseDouble(raw))); } catch (Exception ex) {} tinhTienThua(); } });
        pnlNhapTien.add(new JLabel("Khách đưa: ") {{ setFont(FONT_14); }}, BorderLayout.WEST); pnlNhapTien.add(txtTienKhachDua, BorderLayout.CENTER);

        JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 5, 5)) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 90); } }; pnlGrid.setOpaque(false);
        for (String qc : new String[]{"30,000","50,000","100,000","200,000","500,000","1,000,000","1,500,000","2,000,000"}) {
            JButton b = makeOutlineBtn(qc, null); b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12)); b.setBorder(new EmptyBorder(2, 0, 2, 0)); b.addActionListener(e -> { txtTienKhachDua.setText(qc); tinhTienThua(); }); pnlGrid.add(b);
        } pnlGrid.add(new JLabel());

        JPanel pnlThua = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 26); } }; pnlThua.setOpaque(false); pnlThua.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        lblTienThua = new JLabel("0 VNĐ"); lblTienThua.setFont(FONT_B14); pnlThua.add(new JLabel("Tiền thừa trả khách:") {{ setFont(FONT_14); }}, BorderLayout.WEST); pnlThua.add(lblTienThua, BorderLayout.EAST);
        pnlTienMat.add(pnlNhapTien); pnlTienMat.add(Box.createVerticalStrut(6)); pnlTienMat.add(pnlGrid); pnlTienMat.add(Box.createVerticalStrut(10)); pnlTienMat.add(pnlThua);

        pnlQR = new JPanel(new BorderLayout()); pnlQR.setOpaque(false);
        lblQR = new JLabel("", SwingConstants.CENTER); lblQR.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblQR.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (originalQRImageIcon != null) showZoomedQRDialog(); }});
        pnlQR.add(lblQR, BorderLayout.CENTER);
        pnlQR.addComponentListener(new ComponentAdapter() { @Override public void componentResized(ComponentEvent e) { if (originalQRImageIcon != null) scaleAndSetQR(); } });

        pnlSwitch.add(pnlTienMat, "TIEN_MAT"); pnlSwitch.add(pnlQR, "QR"); content.add(pnlSwitch);

        ActionListener ptttListener = e -> { boolean isBank = btnChuyenKhoan.isSelected(); cardSwitch.show(pnlSwitch, isBank ? "QR" : "TIEN_MAT"); if (isBank) { toggleQRCode(); startBankChecking(); } else { stopBankChecking(); tinhTienThua(); } };
        btnTienMat.addActionListener(ptttListener); btnChuyenKhoan.addActionListener(ptttListener);
        content.add(Box.createVerticalGlue()); pnlRight.add(content, BorderLayout.CENTER);
        return pnlRight;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout()); bar.setBackground(Color.WHITE); bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
        JButton btnQuayLai = makeOutlineBtn("Quay lại", null); btnQuayLai.setBorder(new EmptyBorder(6, 16, 6, 16)); btnQuayLai.addActionListener(e -> { stopAllTimers(); appFrame.showCard("doi-ve-step-2"); });

        JButton btnHuy = makeRedBtn("Hủy đổi vé", null);
        btnHuy.addActionListener(e -> showHuyPopup());

        lblCountdown = new JLabel("Thời hạn giữ vé: --:--") { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(255, 235, 235)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6); g2.setColor(getBackground()); g2.setStroke(new BasicStroke(1.2f)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6); g2.dispose(); super.paintComponent(g); } };
        lblCountdown.setFont(FONT_B14); lblCountdown.setForeground(new Color(190, 30, 30)); lblCountdown.setOpaque(false); lblCountdown.setBackground(new Color(200, 60, 60)); lblCountdown.setBorder(new EmptyBorder(6, 12, 6, 12));

        JButton btnThanhToan = makeNavyBtn("Xác nhận & Đổi vé", null);
        btnThanhToan.addActionListener(e -> {
            if (s_tongThu > 0 && btnTienMat.isSelected()) {
                if (parseMoney(txtTienKhachDua.getText()) < s_tongThu) {
                    JOptionPane.showMessageDialog(this, "Khách đưa thiếu tiền!", "Lỗi thanh toán", JOptionPane.ERROR_MESSAGE); return;
                }
            }
            showXacNhanThanhToanPopup();
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); left.setBackground(Color.WHITE); left.add(btnQuayLai); left.add(btnHuy);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4)); right.setBackground(Color.WHITE); right.add(lblCountdown); right.add(btnThanhToan);
        bar.add(left, BorderLayout.WEST); bar.add(right, BorderLayout.EAST); return bar;
    }

    private void showHuyPopup() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        JPanel glass = new JPanel(new GridBagLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f)); g2.setColor(new Color(10, 20, 50)); g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose(); } }; glass.setOpaque(false);
        JPanel box = buildPopupBox(380, 235); box.setBorder(new EmptyBorder(28, 32, 24, 32));
        JLabel lblIcon = new JLabel("⚠", SwingConstants.CENTER); lblIcon.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 36)); lblIcon.setForeground(new Color(220, 100, 0));
        JLabel lblMsg = new JLabel("<html><div style='text-align:center;'><b style='font-size:14px;color:#1c396e;'>Xác nhận hủy quá trình đổi vé?</b><br><br><span style='font-size:13px;color:#555;'>Bạn sẽ được đưa về trang chủ quản lý đổi trả.</span></div></html>", SwingConstants.CENTER);
        JPanel topContent = new JPanel(new BorderLayout(0, 10)); topContent.setOpaque(false); topContent.add(lblIcon, BorderLayout.NORTH); topContent.add(lblMsg, BorderLayout.CENTER);
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0)); btnRow.setOpaque(false);
        JButton btnNo = makeOutlineBtn("Không", null); btnNo.addActionListener(ev -> dialog.dispose());
        JButton btnYes = makeRedBtn("Hủy Đổi", null); btnYes.addActionListener(ev -> { dialog.dispose(); stopAllTimers(); maVeMoiHienThi = "";
            appFrame.showCard("doi-tra"); });
        btnRow.add(btnNo); btnRow.add(btnYes); box.add(topContent, BorderLayout.CENTER); box.add(btnRow, BorderLayout.SOUTH); glass.add(box);
        setupAndShowDialog(dialog, glass, ancestor);
    }

    private void showXacNhanThanhToanPopup() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        JPanel glass = new JPanel(new GridBagLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f)); g2.setColor(new Color(10, 20, 50)); g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose(); } }; glass.setOpaque(false);
        double tienKhach = parseMoney(txtTienKhachDua.getText());
        String tienKhachStr = tienKhach > 0 ? DF.format(tienKhach) : "(chưa nhập)";
        double tienThua = tienKhach > 0 ? tienKhach - s_tongThu : 0;
        JPanel box = buildPopupBox(400, tienKhach > 0 ? 240 : 220); box.setBorder(new EmptyBorder(28, 32, 24, 32));
        String thuaHtml = tienKhach > 0 ? "<br><span style='font-size:14px;color:#555;'>Tiền thừa: <b style='color:#1a7a30;'>" + DF.format(Math.max(0, tienThua)) + "</b></span>" : "";
        String thieu = tienKhach > 0 && tienThua < 0 ? "<br><span style='font-size:14px;color:#cc0000;'>⚠ Tiền khách đưa chưa đủ!</span>" : "";
        JLabel lblMsg = new JLabel("<html><div style='text-align:center;'><b style='font-size:15px;color:#1c396e;'>Xác nhận thanh toán tiền mặt</b><br><br><span style='font-size:14px;color:#555;'>Cần thanh toán: <b style='color:#c82020;'>" + DF.format(s_tongThu) + "</b></span><br><span style='font-size:14px;color:#555;'>Khách đưa: <b>" + tienKhachStr + "</b></span>" + thuaHtml + thieu + "</div></html>", SwingConstants.CENTER);
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0)); btnRow.setOpaque(false);
        JButton btnNo = makeOutlineBtn("Hủy", null); btnNo.addActionListener(ev -> dialog.dispose());
        JButton btnYes = makeNavyBtn("Xác nhận", null); btnYes.addActionListener(ev -> { dialog.dispose(); xuLyHoanTatThanhToan(btnTienMat.isSelected() ? "Tiền mặt" : "Chuyển khoản"); });
        btnRow.add(btnNo); btnRow.add(btnYes); box.add(lblMsg, BorderLayout.CENTER); box.add(btnRow, BorderLayout.SOUTH); glass.add(box);
        setupAndShowDialog(dialog, glass, ancestor);
    }

    private void showThanhCongPopup(Runnable onDone) {
        Window ancestor = SwingUtilities.getWindowAncestor(this); JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL); dialog.setUndecorated(true);
        final float[] alpha = { 0f }; JPanel glass = new JPanel(new GridBagLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f)); g2.setColor(new Color(10, 20, 50)); g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose(); } }; glass.setOpaque(false);
        final int[] frame = { 0 }; final int TOTAL_FRAMES = 30;
        JPanel box = new JPanel(new BorderLayout(0, 12)) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha[0])); g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); int cx = getWidth() / 2, cy = 70, r = 38; float progress = Math.min(1f, (float) frame[0] / TOTAL_FRAMES); g2.setColor(new Color(220, 245, 220)); g2.fillOval(cx - r, cy - r, r * 2, r * 2); g2.setColor(new Color(34, 170, 70)); g2.setStroke(new BasicStroke(3f)); g2.drawOval(cx - r, cy - r, r * 2, r * 2); if (progress > 0) { g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); int x1 = cx - 18, y1 = cy, xMid = cx - 6, yMid = cy + 14, x2 = cx + 20, y2 = cy - 16; float p1 = Math.min(1f, progress / 0.5f); g2.drawLine(x1, y1, (int) (x1 + (xMid - x1) * p1), (int) (y1 + (yMid - y1) * p1)); if (progress > 0.5f) { float p2 = (progress - 0.5f) / 0.5f; g2.drawLine(xMid, yMid, (int) (xMid + (x2 - xMid) * p2), (int) (yMid + (y2 - yMid) * p2)); } } g2.dispose(); } };
        box.setOpaque(false); box.setPreferredSize(new Dimension(320, 240)); box.setBorder(new EmptyBorder(140, 24, 24, 24));
        JLabel lblMsg = new JLabel("<html><div style='text-align:center;'><b style='font-size:15px;color:#1c396e;'>Đổi vé thành công!</b><br><span style='font-size:13px;color:#888;'>Đang xuất hóa đơn điện tử...</span></div></html>", SwingConstants.CENTER);
        box.add(lblMsg, BorderLayout.CENTER); glass.add(box); dialog.setContentPane(glass); dialog.setSize(ancestor.getSize()); dialog.setLocation(ancestor.getLocation());
        javax.swing.Timer animTimer = new javax.swing.Timer(16, ev -> { frame[0]++; alpha[0] = Math.min(1f, frame[0] / 20f); glass.repaint(); box.repaint(); if (frame[0] >= TOTAL_FRAMES) ((javax.swing.Timer)ev.getSource()).stop(); }); animTimer.start();
        javax.swing.Timer closeTimer = new javax.swing.Timer(2500, ev -> { animTimer.stop(); dialog.dispose(); if (onDone != null) onDone.run(); }); closeTimer.setRepeats(false); closeTimer.start(); dialog.setVisible(true);
    }

    private void showZoomedQRDialog() {
        Window ancestor = SwingUtilities.getWindowAncestor(this); JDialog dialog = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL); dialog.setUndecorated(true);
        JPanel glass = new JPanel(new GridBagLayout()) { @Override protected void paintComponent(Graphics g) { g.setColor(new Color(10, 20, 50, 150)); g.fillRect(0, 0, getWidth(), getHeight()); } }; glass.setOpaque(false);
        JPanel box = buildPopupBox(420, 480); box.setBorder(new EmptyBorder(10, 20, 20, 20));
        JPanel pnlHeader = new JPanel(new BorderLayout()); pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Mã thanh toán Đổi vé"); lblTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15)); lblTitle.setForeground(NAVY);
        JButton btnClose = new JButton("✕") { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.WHITE); g2.fillOval(0, 0, getWidth(), getHeight()); g2.dispose(); super.paintComponent(g); } };
        btnClose.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14)); btnClose.setForeground(new Color(200, 40, 40)); btnClose.setContentAreaFilled(false); btnClose.setBorderPainted(false); btnClose.setFocusPainted(false); btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnClose.setPreferredSize(new Dimension(28, 28)); btnClose.addActionListener(e -> dialog.dispose());
        pnlHeader.add(lblTitle, BorderLayout.WEST); pnlHeader.add(btnClose, BorderLayout.EAST);
        java.awt.Image scaleImg = originalQRImageIcon.getImage().getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH); JLabel lblBigQR = new JLabel(new ImageIcon(scaleImg)); lblBigQR.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lblFoot = new JLabel("Mở ứng dụng ngân hàng/ví điện tử để quét mã", SwingConstants.CENTER); lblFoot.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11)); lblFoot.setForeground(Color.GRAY);
        box.add(pnlHeader, BorderLayout.NORTH); box.add(lblBigQR, BorderLayout.CENTER); box.add(lblFoot, BorderLayout.SOUTH); glass.add(box); setupAndShowDialog(dialog, glass, ancestor);
    }

    private void tinhToanTaiChinh(long giaVeCu) {
        long phiDoiVe = 30000L;
        long tienKhachBu = s_tongThu;
        long tongTien = giaVeCu + tienKhachBu;

        lblGiaVeCu.setText("Giá vé cũ: " + DF.format(giaVeCu));
        lblGiaVeMoi.setText("Giá vé mới: " + DF.format(s_giaVeMoi));
        lblPhiDoiVe.setText("Phí đổi vé: " + DF.format(phiDoiVe));

        lblKhachBu.setText("Tiền khách bù: " + DF.format(tienKhachBu));
        if (tienKhachBu >= 0) {
            lblKhachBu.setForeground(new Color(200, 30, 30));
        } else {
            lblKhachBu.setText("Tiền hoàn khách: " + DF.format(Math.abs(tienKhachBu)));
            lblKhachBu.setForeground(new Color(30, 120, 60));
        }

        lblTongTienTT.setText("Tổng tiền (Vé mới + Bù): " + DF.format(tongTien));

        lblGiaVeCu.setForeground(Color.DARK_GRAY);
        lblGiaVeMoi.setForeground(Color.DARK_GRAY);
        lblPhiDoiVe.setForeground(Color.DARK_GRAY);
        lblTongTienTT.setForeground(NAVY);

        if (btnChuyenKhoan != null && btnChuyenKhoan.isSelected()) toggleQRCode();
        else tinhTienThua();
    }

    private void tinhTienThua() {
        if (s_tongThu <= 0 || txtTienKhachDua == null || lblTienThua == null) return;
        double tk = parseMoney(txtTienKhachDua.getText()); double thua = tk - s_tongThu;
        if (tk == 0) { lblTienThua.setText("0 VNĐ"); lblTienThua.setForeground(Color.BLACK); }
        else if (thua < 0) { lblTienThua.setText("Thiếu: " + DF.format(Math.abs(thua))); lblTienThua.setForeground(Color.RED); }
        else { lblTienThua.setText(DF.format(thua)); lblTienThua.setForeground(new Color(0, 140, 0)); }
    }

    private void toggleQRCode() {
        if (!btnChuyenKhoan.isSelected()) return;
        if (s_tongThu <= 0) { lblQR.setIcon(null); lblQR.setText("Hóa đơn 0đ"); return; }
        lblQR.setIcon(null); lblQR.setText("Đang tạo mã VietQR...");
        new SwingWorker<ImageIcon, Void>() {
            @Override protected ImageIcon doInBackground() throws Exception {
                String info = URLEncoder.encode("Thanh toan doi ve " + maDon, StandardCharsets.UTF_8);
                String url = String.format("https://img.vietqr.io/image/970422-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s", ACCOUNT_NO, s_tongThu, info, URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8));
                return new ImageIcon(new java.net.URL(url));
            }
            @Override protected void done() { try { originalQRImageIcon = get(); scaleAndSetQR(); } catch (Exception e) { lblQR.setIcon(null); lblQR.setText("Lỗi tạo QR!"); } }
        }.execute();
    }

    private void scaleAndSetQR() {
        if (originalQRImageIcon == null) return;
        int size = pnlQR.getWidth() > 40 ? pnlQR.getWidth() - 20 : 160; size = Math.min(size, pnlQR.getHeight() > 40 ? pnlQR.getHeight() - 20 : size);
        java.awt.Image img = originalQRImageIcon.getImage().getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH); lblQR.setIcon(new ImageIcon(img)); lblQR.setText("");
    }

    private void startBankChecking() {
        stopBankChecking();
        bankCheckTimer = new javax.swing.Timer(2000, e -> { if (checkBankReceived()) { bankCheckTimer.stop(); for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isVisible()) w.dispose(); xuLyHoanTatThanhToan("Chuyển khoản VietQR"); } }); bankCheckTimer.start();
    }
    private void stopBankChecking() { if (bankCheckTimer != null) bankCheckTimer.stop(); }
    private boolean checkBankReceived() {
        if (CASSO_API_KEY.equals("YOUR_CASSO_API_KEY")) return false;
        try { URL url = new URL("https://oauth.casso.vn/v2/transactions?page=1&pageSize=20&sort=DESC"); HttpURLConnection conn = (HttpURLConnection) url.openConnection(); conn.setRequestMethod("GET"); conn.setRequestProperty("Authorization", "apikey " + CASSO_API_KEY); conn.setConnectTimeout(5000); conn.setReadTimeout(5000); int code = conn.getResponseCode(); if (code != 200) return false; BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)); StringBuilder sb = new StringBuilder(); String line; while ((line = br.readLine()) != null) sb.append(line); br.close(); String jsonLower = sb.toString().toLowerCase(); String maHDNoDash = maDon.replace("-", "").toLowerCase(); return jsonLower.contains(maHDNoDash) && jsonLower.contains(String.valueOf((long) s_tongThu)); } catch (Exception e) { return false; }
    }

    private void startCountdown() {
        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            if (secondsLeft <= 0) { stopAllTimers(); lblCountdown.setText("Hết thời gian!"); JOptionPane.showMessageDialog(this, "Hết thời gian giữ vé!", "Hết thời gian", JOptionPane.WARNING_MESSAGE); appFrame.showCard("doi-tra"); } else updateCountdownLabel();
        }); countdownTimer.start(); updateCountdownLabel();
    }
    private void updateCountdownLabel() { int m = secondsLeft / 60, s = secondsLeft % 60; lblCountdown.setForeground(secondsLeft <= 300 ? new Color(160, 0, 0) : new Color(190, 30, 30)); lblCountdown.setBackground(secondsLeft <= 300 ? new Color(160, 0, 0) : new Color(200, 60, 60)); lblCountdown.setText(String.format("Thời hạn giữ vé: %02d:%02d", m, s)); }
    private void stopAllTimers() { if (countdownTimer != null) countdownTimer.stop(); if (bankCheckTimer != null) bankCheckTimer.stop(); }
    private JPanel buildPopupBox(int w, int h) { JPanel box = new JPanel(new BorderLayout(0, 16)) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16); g2.dispose(); } }; box.setOpaque(false); box.setPreferredSize(new Dimension(w, h)); return box; }
    private void setupAndShowDialog(JDialog dialog, JPanel glass, Window ancestor) { dialog.setContentPane(glass); dialog.getRootPane().registerKeyboardAction(ev -> dialog.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW); dialog.setSize(ancestor.getSize()); dialog.setLocation(ancestor.getLocation()); dialog.setVisible(true); }
    private double parseMoney(String str) { try { return Double.parseDouble(str.replaceAll("[^0-9]", "")); } catch (Exception e) { return 0; } }
    private String taoMaHoaDon() { try (Connection con = Connect_DB.getInstance().getConnection()) { return MaTuDong.taoMaDon(con, LocalDate.now()); } catch (Exception e) { return "DT" + new SimpleDateFormat("MMyy").format(new Date()) + "-" + String.format("%04d", System.currentTimeMillis() % 10000); } }

    private JPanel createDetailLabel(String title, String value) { JPanel p = new JPanel(new BorderLayout()) { @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); } }; p.setOpaque(false); JLabel l1 = new JLabel(title); l1.setFont(FONT_14); l1.setForeground(new Color(80, 80, 80)); JLabel l2 = new JLabel(value); l2.setFont(FONT_B14); l2.setForeground(Color.BLACK); p.add(l1, BorderLayout.WEST); p.add(l2, BorderLayout.EAST); return p; }
    private JToggleButton createToggleBtn(String text) { JToggleButton b = new JToggleButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); if (isSelected()) { g2.setColor(new Color(240, 246, 255)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4); g2.setColor(NAVY); g2.setStroke(new BasicStroke(1.8f)); g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 4, 4); } else { g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4); g2.setColor(BORDER_C); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4); } g2.dispose(); super.paintComponent(g); } }; b.addChangeListener(e -> { b.setFont(b.isSelected() ? FONT_B14 : FONT_14); b.setForeground(b.isSelected() ? NAVY : new Color(80, 80, 80)); }); b.setFont(FONT_14); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(6, 12, 6, 12)); return b; }
    private JButton makeNavyBtn(String text, Icon icon) { JButton b = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isPressed() ? new Color(18, 42, 85) : getModel().isRollover() ? new Color(38, 68, 128) : NAVY); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); g2.dispose(); super.paintComponent(g); } }; if (icon != null) { b.setIcon(icon); b.setHorizontalTextPosition(SwingConstants.LEFT); } b.setFont(FONT_B14); b.setForeground(Color.WHITE); b.setIconTextGap(8); b.setBorder(new EmptyBorder(6, 18, 6, 18)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
    private JButton makeOutlineBtn(String text, Icon icon) { JButton b = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isPressed() ? new Color(220, 230, 245) : getModel().isRollover() ? new Color(230, 240, 250) : new Color(242, 247, 252)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6); g2.setColor(NAVY); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6); g2.dispose(); super.paintComponent(g); } }; if (icon != null) b.setIcon(icon); b.setFont(FONT_14); b.setForeground(NAVY); b.setIconTextGap(8); b.setContentAreaFilled(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }
    private JButton makeRedBtn(String text, Icon icon) { JButton b = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isPressed() ? new Color(160, 20, 20) : getModel().isRollover() ? new Color(200, 40, 40) : new Color(210, 30, 40)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); g2.dispose(); super.paintComponent(g); } }; if (icon != null) { b.setIcon(icon); b.setHorizontalTextPosition(SwingConstants.LEFT); } b.setFont(FONT_B14); b.setForeground(Color.WHITE); b.setIconTextGap(8); b.setBorder(new EmptyBorder(6, 16, 6, 16)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b; }

    // ========================================================================================
    // LOGIC ĐỔI VÉ TRƯỜNG PHÁI 2: CẬP NHẬT VÉ CŨ THÀNH "ĐÃ ĐỔI", CẤP MÃ VÉ MỚI
    // ========================================================================================
    private void xuLyHoanTatThanhToan(String hinhThuc) {
        stopAllTimers();

        // Tự động sinh Mã Vé Mới tinh
        String maVeMoi = maVeMoiHienThi.isEmpty() ? ("VE" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase()) : maVeMoiHienThi;

        String nv = AuthService.getCurrentMaNV() != null ? AuthService.getCurrentMaNV() : "NV001";

        // 1. Tạo Hóa Đơn thu khoản tiền bù thêm
        String sqlInsertHD =
                "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) " +
                        "SELECT ?, GETDATE(), ?, hd.maKH, ?, ?, ? " +
                        "FROM HoaDon hd JOIN Ve v ON v.maHoaDon = hd.maHoaDon WHERE v.maVe = ?";

        String sqlFindGhe = "SELECT TOP 1 maGhe FROM Ghe WHERE maToaTau = ? AND soGhe = ?";

        // 2. Chuyển Vé Cũ thành "Đã đổi"
        String sqlUpdateVeCu = "UPDATE Ve SET trangThaiVe = N'Đã đổi' WHERE maVe = ?";

        // 3. Ghi nhận Lịch sử Đổi Vé
        String sqlInsertDon =
                "INSERT INTO DonDoiTraVe (maDon, tienBu, ngayLap, loaiDon, maVe) " +
                        "VALUES (?, ?, GETDATE(), 'DON_DOI', ?)";

        // 4. Sinh Vé Mới với các thông số mới, gán vào Hóa đơn mới lập ở trên
        String sqlInsertVeMoi =
                "INSERT INTO Ve (maVe, ngayMua, loaiVe, trangThaiVe, giaVe, maGhe, maHoaDon, maChuyenTau, maKH) " +
                        "SELECT ?, GETDATE(), loaiVe, N'Đã thanh toán', ?, ?, ?, ?, maKH " +
                        "FROM Ve WHERE maVe = ?";

        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Tính tổng tiền HĐ = giá vé cũ + bù
                long giaVeCuForHD = 0;
                try { giaVeCuForHD = Long.parseLong(s_dataCu[8].replaceAll("[^0-9]", "")); }
                catch (Exception e) {
                    try { giaVeCuForHD = Long.parseLong(s_dataCu[7].replaceAll("[^0-9]", "")); } catch (Exception ex) {}
                }
                long tongTienHD = giaVeCuForHD + s_tongThu;

                try (PreparedStatement ps = conn.prepareStatement(sqlInsertHD)) {
                    ps.setString(1, maDon);
                    ps.setString(2, nv);
                    ps.setLong  (3, tongTienHD);
                    ps.setLong  (4, hinhThuc.equals("Tiền mặt") ? (long)parseMoney(txtTienKhachDua.getText()) : s_tongThu);
                    ps.setString(5, hinhThuc.equals("Tiền mặt") ? "TIEN_MAT" : "CHUYEN_KHOAN");
                    ps.setString(6, s_maVe);
                    int rows = ps.executeUpdate();
                    if(rows == 0) throw new Exception("Không tìm thấy dữ liệu hóa đơn gốc!");
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
                                else throw new Exception("Không tìm thấy ghế mới trong DB!");
                            }
                        }
                    }
                } catch (NumberFormatException ignored) {}

                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateVeCu)) {
                    ps.setString(1, s_maVe);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlInsertDon)) {
                    ps.setString(1, maDon); ps.setLong(2, s_tongThu); ps.setString(3, s_maVe); ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlInsertVeMoi)) {
                    ps.setString(1, maVeMoi);
                    ps.setLong  (2, s_giaVeMoi);
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

            } catch (Exception ex) { conn.rollback(); throw ex; }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taoHoaDonPDF(String maDonLuu, String hinhThuc, String maVeMoi) {
        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            File folder = new File("HoaDon");
            if (!folder.exists()) folder.mkdir();
            File pdfFile = new File(folder, maDonLuu + ".pdf");

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle = new Font(bf, 16, Font.BOLD);
            Font fontBold = new Font(bf, 11, Font.BOLD);
            Font fontNormal = new Font(bf, 11, Font.NORMAL);
            Font fontItalic = new Font(bf, 11, Font.ITALIC);

            Paragraph title = new Paragraph("HÓA ĐƠN GIÁ TRỊ GIA TĂNG (ĐỔI VÉ)", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            Paragraph dateP = new Paragraph("Ngày xuất: " + dateStr, fontItalic);
            dateP.setAlignment(Element.ALIGN_CENTER);
            document.add(dateP); document.add(new Paragraph(" ", fontNormal));

            document.add(new Paragraph("Đơn vị bán hàng: CÔNG TY CỔ PHẦN VẬN TẢI ĐƯỜNG SẮT", fontBold));
            document.add(new Paragraph("Mã số thuế: 0100106264", fontNormal));
            document.add(new Paragraph("Địa chỉ: 113 Nguyễn Đình Thụ, Tuy Phước, Gia Lai", fontNormal));
            document.add(new Paragraph(" ", fontNormal));

            document.add(new Paragraph("Họ tên người mua hàng: " + tenKH, fontBold));
            document.add(new Paragraph("Điện thoại: " + sdtKH, fontNormal));
            document.add(new Paragraph("Hình thức thanh toán: " + hinhThuc + "          Mã HĐ: " + maDonLuu, fontNormal));
            document.add(new Paragraph(" ", fontNormal));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 0.6f, 1.8f, 1.6f, 1.6f, 1.4f, 1.6f, 1.8f, 1.8f, 1.8f });

            for (String h : new String[]{"STT", "Dịch vụ", "Vé cũ", "Vé mới", "Phí đổi", "Tiền bù", "Giá vé cũ", "Giá vé mới", "Tổng tiền"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontBold));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPaddingBottom(6);
                cell.setBackgroundColor(new com.itextpdf.text.BaseColor(245, 245, 245));
                table.addCell(cell);
            }

            long giaVeCu = 0;
            try { giaVeCu = Long.parseLong(s_dataCu[8].replaceAll("[^0-9]", "")); }
            catch (Exception e) {
                try { giaVeCu = Long.parseLong(s_dataCu[7].replaceAll("[^0-9]", "")); } catch (Exception ex) {}
            }
            long phiDoi  = 30000L;
            long tongCuoi = giaVeCu + s_tongThu;

            String[] rowData = {"1", "Đổi vé tàu", s_maVe, maVeMoi,
                    DF.format(phiDoi),  DF.format(s_tongThu),
                    DF.format(giaVeCu), DF.format(s_giaVeMoi),
                    DF.format(tongCuoi)};
            for (int i = 0; i < rowData.length; i++) {
                PdfPCell c = new PdfPCell(new Phrase(rowData[i], fontNormal));
                c.setHorizontalAlignment(i >= 4 ? Element.ALIGN_RIGHT : Element.ALIGN_CENTER);
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(c);
            }
            document.add(table); document.add(new Paragraph(" ", fontNormal));

            Paragraph pTongCuoi = new Paragraph("Tổng tiền: " + DF.format(tongCuoi), fontBold);
            pTongCuoi.setAlignment(Element.ALIGN_RIGHT);
            document.add(pTongCuoi);

            document.add(new Paragraph(" ", fontNormal));

            Phrase phraseTienChu = new Phrase();
            phraseTienChu.add(new com.itextpdf.text.Chunk("Số tiền viết bằng chữ: ", fontNormal));
            phraseTienChu.add(new com.itextpdf.text.Chunk(docTien((long) s_tongThu), fontItalic));
            document.add(new Paragraph(phraseTienChu));

            document.add(new Paragraph("Ghi chú: ......................................................................................................................................", fontNormal));
            document.add(new Paragraph(" ", fontNormal));

            PdfPTable signTable = new PdfPTable(2); signTable.setWidthPercentage(100);
            PdfPCell cellBuyer = new PdfPCell(new Phrase("Người mua hàng\n(Ký, ghi rõ họ tên)", fontNormal)); cellBuyer.setHorizontalAlignment(Element.ALIGN_CENTER); cellBuyer.setBorder(PdfPCell.NO_BORDER);
            PdfPCell cellSeller = new PdfPCell(new Phrase("Người bán hàng\n(Ký, ghi rõ họ tên)", fontNormal)); cellSeller.setHorizontalAlignment(Element.ALIGN_CENTER); cellSeller.setBorder(PdfPCell.NO_BORDER);
            signTable.addCell(cellBuyer); signTable.addCell(cellSeller); document.add(signTable);

            document.close();
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(pdfFile);
        } catch (Exception e) {}
    }

    private void taoVePDF(String maVeLuu) {
        try (Connection conn = Connect_DB.getInstance().getConnection()) {
            String sql = "SELECT v.loaiVe, v.maGhe, k.hoTenKH, k.cccd, " +
                    "t.tenTau, ct.thoiGianKhoiHanh, " +
                    "gaDi.tenGa AS gaDi, gaDen.tenGa AS gaDen, " +
                    "g.soGhe, tt.soToa, g.loaiGhe " +
                    "FROM Ve v " +
                    "LEFT JOIN KhachHang k         ON v.maKH         = k.maKH " +
                    "JOIN ChiTietChuyenTau ct  ON v.maChuyenTau  = ct.maChuyenTau " +
                    "JOIN ChuyenTau c          ON ct.maChuyenTau = c.maChuyenTau " +
                    "JOIN Tau t                ON c.maTau        = t.maTau " +
                    "JOIN Ga gaDi             ON ct.maGaDi       = gaDi.maGa " +
                    "JOIN Ga gaDen            ON ct.maGaDen      = gaDen.maGa " +
                    "JOIN Ghe g               ON v.maGhe         = g.maGhe " +
                    "JOIN ToaTau tt            ON g.maToaTau      = tt.maToaTau " +
                    "WHERE v.maVe = ?";

            String loaiVe="", tenKHT="", cccd="", tenTau="", ngayDi="", gioDi="";
            String gaDi="", gaDen="", soGhe="", soToa="", loaiGheRaw="";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maVeLuu);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        loaiVe  = rs.getString("loaiVe")  != null ? rs.getString("loaiVe")  : "";
                        tenKHT   = rs.getString("hoTenKH") != null ? rs.getString("hoTenKH") : "Khách vãng lai";
                        String rawCccd = rs.getString("cccd");
                        cccd = cheCCCD(rawCccd != null ? rawCccd : "");
                        tenTau  = rs.getString("tenTau")  != null ? rs.getString("tenTau")  : "";
                        gaDi    = rs.getString("gaDi")    != null ? rs.getString("gaDi")    : "";
                        gaDen   = rs.getString("gaDen")   != null ? rs.getString("gaDen")   : "";
                        soGhe   = rs.getString("soGhe")   != null ? rs.getString("soGhe")   : "";
                        soToa   = String.valueOf(rs.getInt("soToa"));
                        loaiGheRaw = rs.getString("loaiGhe") != null ? rs.getString("loaiGhe") : "";
                        java.sql.Timestamp ts = rs.getTimestamp("thoiGianKhoiHanh");
                        if (ts != null) {
                            ngayDi = new SimpleDateFormat("dd/MM/yyyy").format(ts);
                            gioDi  = new SimpleDateFormat("HH:mm").format(ts);
                        }
                    }
                }
            }

            String loaiVeHienThi = "MOT_CHIEU".equals(loaiVe) ? "Một chiều" : "KHU_HOI".equals(loaiVe) ? "Khứ hồi" : loaiVe;
            String loaiGheHienThi = switch (loaiGheRaw.trim()) {
                case "GHE_CUNG"   -> "Ghế cứng";
                case "GHE_MEM"    -> "Ghế mềm";
                case "GIUONG_NAM" -> "Giường nằm";
                default           -> loaiGheRaw;
            };
            String hangVe = "Giường nằm".equals(loaiGheHienThi) ? "VIP" : "Thường";

            File folder = new File("Ve");
            if (!folder.exists()) folder.mkdir();
            File pdfFile = new File(folder, "Ve_" + maVeLuu + ".pdf");

            Rectangle pageSize = new Rectangle(240, 580);
            Document doc = new Document(pageSize, 10, 10, 12, 12);
            PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));
            doc.open();

            BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fCongTy  = new Font(bf, 10, Font.BOLD);
            Font fGaTen   = new Font(bf, 11, Font.BOLD);
            Font fTieuDe  = new Font(bf, 12, Font.BOLD);
            Font fSub     = new Font(bf,  8, Font.NORMAL);
            Font fGaLabel = new Font(bf,  8, Font.NORMAL, BaseColor.GRAY);
            Font fGaValue = new Font(bf, 11, Font.BOLD);
            Font fLabel   = new Font(bf,  9, Font.NORMAL);
            Font fValue   = new Font(bf,  9, Font.BOLD);
            Font fMaVe    = new Font(bf,  8, Font.NORMAL, BaseColor.GRAY);

            PdfPTable wrap = new PdfPTable(1); wrap.setWidthPercentage(100);
            PdfPCell wc = new PdfPCell(); wc.setBorder(Rectangle.BOX); wc.setBorderWidth(0.8f); wc.setPaddingLeft(10); wc.setPaddingRight(10); wc.setPaddingTop(10); wc.setPaddingBottom(10);

            Paragraph pCty = new Paragraph("TỔNG CÔNG TY ĐƯỜNG SẮT VIỆT NAM", fCongTy); pCty.setAlignment(Element.ALIGN_CENTER); wc.addElement(pCty);
            Paragraph pGaTen = new Paragraph("GA DIÊU TRÌ", fGaTen); pGaTen.setAlignment(Element.ALIGN_CENTER); wc.addElement(pGaTen);

            PdfPTable lineTable = new PdfPTable(1); lineTable.setWidthPercentage(100); lineTable.setSpacingBefore(4); lineTable.setSpacingAfter(4);
            PdfPCell lineCell = new PdfPCell(new Phrase("")); lineCell.setBorder(Rectangle.BOTTOM); lineCell.setBorderWidth(0.5f); lineCell.setBorderColor(BaseColor.GRAY); lineCell.setPaddingBottom(0); lineTable.addCell(lineCell); wc.addElement(lineTable);

            Paragraph pTieuDe = new Paragraph("VÉ LÊN TÀU HỎA", fTieuDe); pTieuDe.setAlignment(Element.ALIGN_CENTER); wc.addElement(pTieuDe);
            Paragraph pBoarding = new Paragraph("BOARDING TICKET", fSub); pBoarding.setAlignment(Element.ALIGN_CENTER); pBoarding.setSpacingAfter(5); wc.addElement(pBoarding);

            com.itextpdf.text.pdf.Barcode128 barcode = new com.itextpdf.text.pdf.Barcode128(); barcode.setCode(maVeLuu); barcode.setBarHeight(32f); barcode.setX(1.0f); barcode.setBaseline(0f); barcode.setAltText("");
            java.awt.Image awtImg = barcode.createAwtImage(java.awt.Color.BLACK, java.awt.Color.WHITE);
            com.itextpdf.text.Image imgBar = com.itextpdf.text.Image.getInstance(awtImg, null); imgBar.setAlignment(Element.ALIGN_CENTER); imgBar.scaleToFit(240f, 38f); wc.addElement(imgBar);

            Paragraph pMaVe = new Paragraph("Mã vé/TicketID: " + maVeLuu, fMaVe); pMaVe.setAlignment(Element.ALIGN_CENTER); pMaVe.setSpacingAfter(6); wc.addElement(pMaVe);

            PdfPTable gaTable = new PdfPTable(2); gaTable.setWidthPercentage(100); gaTable.setSpacingBefore(2); gaTable.setSpacingAfter(2);
            PdfPCell cGaDi = new PdfPCell(); cGaDi.setBorder(Rectangle.NO_BORDER); cGaDi.setPaddingLeft(16); cGaDi.setPaddingBottom(2); Paragraph pDiLbl = new Paragraph("Ga đi", fGaLabel); pDiLbl.setAlignment(Element.ALIGN_LEFT); cGaDi.addElement(pDiLbl); Paragraph pDiVal = new Paragraph(gaDi.toUpperCase(), fGaValue); pDiVal.setAlignment(Element.ALIGN_LEFT); cGaDi.addElement(pDiVal); gaTable.addCell(cGaDi);
            PdfPCell cGaDen = new PdfPCell(); cGaDen.setBorder(Rectangle.NO_BORDER); cGaDen.setPaddingRight(16); cGaDen.setPaddingBottom(2); Paragraph pDenLbl = new Paragraph("Ga đến", fGaLabel); pDenLbl.setAlignment(Element.ALIGN_RIGHT); cGaDen.addElement(pDenLbl); Paragraph pDenVal = new Paragraph(gaDen.toUpperCase(), fGaValue); pDenVal.setAlignment(Element.ALIGN_RIGHT); cGaDen.addElement(pDenVal); gaTable.addCell(cGaDen);
            PdfPCell cSep = new PdfPCell(new Phrase("")); cSep.setColspan(2); cSep.setBorder(Rectangle.BOTTOM); cSep.setBorderWidth(0.5f); cSep.setBorderColor(BaseColor.LIGHT_GRAY); cSep.setPaddingBottom(3); gaTable.addCell(cSep); wc.addElement(gaTable);

            PdfPTable infoTable = new PdfPTable(2); infoTable.setWidthPercentage(100); infoTable.setWidths(new float[]{1.35f, 1.65f}); infoTable.setSpacingBefore(3);
            String[][] details = { {"Số hiệu tàu/Train ID:", tenTau}, {"Ngày khởi hành/Date:", ngayDi}, {"Giờ khởi hành/Time:", gioDi}, {"Số Toa/Coach:", soToa}, {"Loại ghế/Type:", loaiGheHienThi}, {"Số ghế/Seat:", soGhe}, {"Loại vé/Ticket:", loaiVeHienThi}, {"Hạng vé/Class:", hangVe}, {"Họ Tên/Name:", tenKHT.toUpperCase()}, {"Giấy tờ/Passport:", cccd}, };
            for (String[] d : details) { PdfPCell cL = new PdfPCell(new Phrase(d[0], fLabel)); cL.setBorder(Rectangle.NO_BORDER); cL.setPaddingLeft(14); cL.setPaddingBottom(3); infoTable.addCell(cL); PdfPCell cR = new PdfPCell(new Phrase(d[1], fValue)); cR.setBorder(Rectangle.NO_BORDER); cR.setPaddingLeft(30); cR.setPaddingBottom(3); infoTable.addCell(cR); }
            wc.addElement(infoTable); wrap.addCell(wc); doc.add(wrap); doc.close();

            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(pdfFile);
        } catch (Exception e) {}
    }

    private String cheCCCD(String cccd) {
        if (cccd != null && cccd.length() > 4) {
            int length = cccd.length();
            if (length == 12) return cccd.substring(0, 4) + "****" + cccd.substring(8);
            else if (length > 6) { int v = (length-4)/2; return cccd.substring(0,v)+"****"+cccd.substring(v+4); }
        }
        return cccd;
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