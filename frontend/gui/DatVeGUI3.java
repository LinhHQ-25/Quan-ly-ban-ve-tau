package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class DatVeGUI3 extends JPanel {

    private static final Color  NAVY    = new Color(28, 57, 110);
    private static final Color  BG      = new Color(242, 247, 252);
    private static final Color  BORDER_C= new Color(180, 205, 230);
    private static final Font   FONT_14 = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font   FONT_B14= new Font("Segoe UI", Font.BOLD,  14);
    private static final DecimalFormat DF = new DecimalFormat("#,### VNĐ");

    // ── Tài khoản thụ hưởng ──────────────────────────────────────
    private static final String BANK_ID     = "MB";          // Mã ngân hàng VietQR
    private static final String ACCOUNT_NO  = "0382588430";  // STK MBBank
    private static final String ACCOUNT_NAME= "MB Bank";

    private JTable             tblChiTiet;
    private DefaultTableModel  modelChiTiet;
    private JLabel             lblTongTien, lblTongKhuyenMai, lblThanhToanConLai;
    private JLabel             lblQR, lblCountdown;
    private JToggleButton      btnTienMat, btnChuyenKhoan;

    private JPanel     pnlSwitch;
    private CardLayout cardSwitch;
    private JPanel     pnlTienMat, pnlQR;
    private JTextField txtTienKhachDua;
    private JLabel     lblTienThua;

    private double tongThanhToan = 0, tongGiaGoc = 0;
    private double tongGiamDoiTuong = 0, giamVoucher = 0;

    private String maHD = "HD" + System.currentTimeMillis() % 100000;

    private java.util.function.Consumer<Integer> onQuayLai;
    private Runnable   onHuyVe;
    private DefaultTableModel modelFromGUI2;

    private javax.swing.Timer countdownTimer;
    private int               secondsLeft;
    private ImageIcon         originalQRImageIcon = null;
    private javax.swing.Timer bankCheckTimer;

    // ── Constructor ───────────────────────────────────────────────
    public DatVeGUI3(DefaultTableModel modelFromGUI2, int secondsLeft,
                     java.util.function.Consumer<Integer> onQuayLai,
                     Runnable onHuyVe) {
        this.modelFromGUI2 = modelFromGUI2;
        this.secondsLeft   = secondsLeft;
        this.onQuayLai     = onQuayLai;
        this.onHuyVe       = onHuyVe;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.78; gbc.weighty = 1;
        gbc.fill  = GridBagConstraints.BOTH; gbc.insets = new Insets(0,0,0,8);
        pnlCenter.add(buildLeftPanel(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.22; gbc.insets = new Insets(0,0,0,0);
        pnlCenter.add(buildRightPanel(), gbc);

        add(pnlCenter,        BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        tinhToanTaiChinh();
        startCountdown();
    }

    public DatVeGUI3(DefaultTableModel modelFromGUI2, int secondsLeft,
                     java.util.function.Consumer<Integer> onQuayLai) {
        this(modelFromGUI2, secondsLeft, onQuayLai, null);
    }

    // ── DB helpers ────────────────────────────────────────────────
    private Object[] layThongTinGheTuDB(String maGhe) {
        String loaiGhe = "Ghế thường"; double giaTien = 300000;
        String sql = "SELECT g.loaiGhe, t.heSoLoaiToa FROM Ghe g " +
                     "JOIN ToaTau t ON g.maToaTau = t.maToaTau WHERE g.maGhe = ?";
        try (java.sql.Connection con = connect_DB.Connect_DB.getInstance().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGhe);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    loaiGhe = rs.getString("loaiGhe");
                    double heSo = rs.getDouble("heSoLoaiToa");
                    giaTien = 300000 * (heSo > 0 ? heSo : 1.0);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new Object[]{loaiGhe, giaTien};
    }

    private double layTyLeGiamTuDB(String loaiDoiTuong) {
        if (loaiDoiTuong.contains("Sinh viên"))        return 0.2;
        if (loaiDoiTuong.contains("Trẻ em (<6 tuổi)")) return 1.0;
        if (loaiDoiTuong.contains("Trẻ em"))           return 0.5;
        if (loaiDoiTuong.contains("cao tuổi"))         return 0.3;
        return 0.0;
    }

    // ── LEFT PANEL ────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
        pnlLeft.setOpaque(false);

        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        pnlTableWrapper.setBackground(Color.WHITE);
        pnlTableWrapper.setBorder(new LineBorder(BORDER_C, 1, true));

        JLabel lblChiTiet = new JLabel("Chi tiết");
        lblChiTiet.setFont(FONT_B14); lblChiTiet.setForeground(Color.WHITE);
        lblChiTiet.setOpaque(true);   lblChiTiet.setBackground(NAVY);
        lblChiTiet.setBorder(new EmptyBorder(6,12,6,12));
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        titleWrap.setOpaque(false); titleWrap.add(lblChiTiet);
        pnlTableWrapper.add(titleWrap, BorderLayout.NORTH);

        String[] cols = {"STT","Mã vé","Chiều vé","Loại chỗ","Đơn giá",
                         "Loại đối tượng","Giảm giá","Thành tiền"};
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (int i = 0; i < modelFromGUI2.getRowCount(); i++) {
            String maVe         = modelFromGUI2.getValueAt(i, 1).toString();
            String chieuVe      = modelFromGUI2.getValueAt(i, 3).toString();
            String maGhe        = modelFromGUI2.getValueAt(i, 4).toString();
            String loaiDoiTuong = modelFromGUI2.getValueAt(i, 8).toString();
            Object[] info = layThongTinGheTuDB(maGhe);
            String loaiCho = (String) info[0]; double donGia = (Double) info[1];
            double giamGia = donGia * layTyLeGiamTuDB(loaiDoiTuong);
            modelChiTiet.addRow(new Object[]{
                i+1, maVe, chieuVe, loaiCho, DF.format(donGia),
                loaiDoiTuong, DF.format(giamGia), DF.format(donGia - giamGia)
            });
        }

        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(35); tblChiTiet.setFont(FONT_14);
        tblChiTiet.getTableHeader().setFont(FONT_B14);
        tblChiTiet.getTableHeader().setBackground(new Color(245,248,252));
        tblChiTiet.setShowGrid(false);
        tblChiTiet.setIntercellSpacing(new Dimension(0,0));
        TableColumnModel tcm = tblChiTiet.getColumnModel();
        tcm.getColumn(0).setMaxWidth(40);
        tcm.getColumn(1).setMinWidth(85);  tcm.getColumn(1).setMaxWidth(85);
        tcm.getColumn(2).setMinWidth(75);  tcm.getColumn(2).setMaxWidth(75);
        tcm.getColumn(3).setMinWidth(90);  tcm.getColumn(3).setMaxWidth(90);
        tcm.getColumn(5).setPreferredWidth(160);

        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        pnlTableWrapper.add(scroll, BorderLayout.CENTER);

        JPanel pnlBottomLeft = new JPanel(new BorderLayout(0,10));
        pnlBottomLeft.setOpaque(false);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        pnlBtns.setOpaque(false);
        JButton btnKhuyenMai = makeNavyBtn("Thêm khuyến mãi", null);
        btnKhuyenMai.addActionListener(e -> nhapKhuyenMai());
        JButton btnGhiChu = makeNavyBtn("Ghi chú", null);
        btnGhiChu.addActionListener(e ->
            JOptionPane.showInputDialog(this, "Nhập ghi chú cho hóa đơn:"));
        pnlBtns.add(btnKhuyenMai); pnlBtns.add(btnGhiChu);
        pnlBottomLeft.add(pnlBtns, BorderLayout.NORTH);

        JPanel pnlTotals = new JPanel(new FlowLayout(FlowLayout.RIGHT,20,5));
        pnlTotals.setOpaque(false);
        lblTongTien        = new JLabel(); lblTongTien.setFont(FONT_14);
        lblTongKhuyenMai   = new JLabel(); lblTongKhuyenMai.setFont(FONT_14);
        lblThanhToanConLai = new JLabel(); lblThanhToanConLai.setFont(FONT_14);
        pnlTotals.add(lblTongTien);
        pnlTotals.add(lblTongKhuyenMai);
        pnlTotals.add(lblThanhToanConLai);
        pnlBottomLeft.add(pnlTotals, BorderLayout.CENTER);

        pnlLeft.add(pnlTableWrapper, BorderLayout.CENTER);
        pnlLeft.add(pnlBottomLeft,   BorderLayout.SOUTH);
        return pnlLeft;
    }

    // ── RIGHT PANEL ───────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBackground(new Color(245,248,252));
        pnlRight.setBorder(new LineBorder(BORDER_C,1));

        JLabel lblInfo = new JLabel("Thông tin hóa đơn");
        lblInfo.setFont(FONT_B14); lblInfo.setForeground(Color.WHITE);
        lblInfo.setOpaque(true);   lblInfo.setBackground(NAVY);
        lblInfo.setBorder(new EmptyBorder(6,12,6,12));
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        titleWrap.setOpaque(false); titleWrap.add(lblInfo);
        pnlRight.add(titleWrap, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10,10,10,10));

        String tenKH = modelFromGUI2.getRowCount() > 0
            ? modelFromGUI2.getValueAt(0, 5).toString() : "N/A";
        String sdtKH = modelFromGUI2.getRowCount() > 0
            ? modelFromGUI2.getValueAt(0, 7).toString() : "N/A";
        String maKH  = "N/A";
        try {
            if (!sdtKH.equals("N/A") && !sdtKH.isEmpty()) {
                entity.KhachHang kh = new dao.KhachHangDAO().timTheoSDT(sdtKH);
                if (kh != null) maKH = kh.getMaKH();
            }
        } catch (Exception ignored) {}

        content.add(createDetailLabel("Mã nhân viên:",   "NV001"));
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Tên nhân viên:",  "Nhân viên Bán Vé"));
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Mã khách hàng:",  maKH));
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Tên khách hàng:", tenKH));
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Số điện thoại:",  sdtKH));

        content.add(Box.createVerticalStrut(10));
        content.add(new JSeparator());
        content.add(Box.createVerticalStrut(10));

        JPanel pnlPTTTTitle = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        pnlPTTTTitle.setOpaque(false);
        JLabel lblPTTT = new JLabel("Phương thức thanh toán:");
        lblPTTT.setFont(FONT_14);
        pnlPTTTTitle.add(lblPTTT, BorderLayout.WEST);
        content.add(pnlPTTTTitle);
        content.add(Box.createVerticalStrut(5));

        btnTienMat     = createToggleBtn("Tiền mặt");
        btnChuyenKhoan = createToggleBtn("Chuyển khoản");
        new ButtonGroup() {{ add(btnTienMat); add(btnChuyenKhoan); }};
        btnTienMat.setSelected(true);

        JPanel pnlToggle = new JPanel(new GridLayout(1,2,6,0)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 35);
            }
        };
        pnlToggle.setOpaque(false);
        pnlToggle.add(btnTienMat); pnlToggle.add(btnChuyenKhoan);
        content.add(pnlToggle);
        content.add(Box.createVerticalStrut(15));

        cardSwitch = new CardLayout();
        pnlSwitch  = new JPanel(cardSwitch) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        };
        pnlSwitch.setOpaque(false);

        // ── Tiền mặt ──
        pnlTienMat = new JPanel();
        pnlTienMat.setLayout(new BoxLayout(pnlTienMat, BoxLayout.Y_AXIS));
        pnlTienMat.setOpaque(false);

        JPanel pnlNhapTien = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 26);
            }
        };
        pnlNhapTien.setOpaque(false);
        pnlNhapTien.setBorder(new MatteBorder(0,0,1,0,Color.GRAY));
        JLabel lblNhapTien = new JLabel("Nhập số tiền: "); lblNhapTien.setFont(FONT_14);
        txtTienKhachDua = new JTextField();
        txtTienKhachDua.setFont(FONT_14);
        txtTienKhachDua.setHorizontalAlignment(JTextField.RIGHT);
        txtTienKhachDua.setBorder(null); txtTienKhachDua.setOpaque(false);
        txtTienKhachDua.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    String raw = txtTienKhachDua.getText().replaceAll("[^0-9]","");
                    if (!raw.isEmpty())
                        txtTienKhachDua.setText(DF.format(Double.parseDouble(raw)).replace(" VNĐ",""));
                    else txtTienKhachDua.setText("");
                } catch (Exception ex) {}
                tinhTienThua();
            }
        });
        pnlNhapTien.add(lblNhapTien,     BorderLayout.WEST);
        pnlNhapTien.add(txtTienKhachDua, BorderLayout.CENTER);

        JPanel pnlGrid = new JPanel(new GridLayout(3,3,5,5)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 90);
            }
        };
        pnlGrid.setOpaque(false);
        String[] quickCash = {"500,000","700,000","900,000","1,000,000","1,200,000",
                              "1,500,000","1,700,000","2,000,000"};
        for (String qc : quickCash) {
            JButton b = makeOutlineBtn(qc, null);
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setBorder(new EmptyBorder(2,0,2,0));
            b.addActionListener(e -> { txtTienKhachDua.setText(qc); tinhTienThua(); });
            pnlGrid.add(b);
        }
        pnlGrid.add(new JLabel());

        JPanel pnlThua = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 26);
            }
        };
        pnlThua.setOpaque(false);
        pnlThua.setBorder(new MatteBorder(0,0,1,0,Color.GRAY));
        JLabel lblThuaTitle = new JLabel("Tiền thừa trả khách:"); lblThuaTitle.setFont(FONT_14);
        lblTienThua = new JLabel("0 VNĐ"); lblTienThua.setFont(FONT_B14);
        pnlThua.add(lblThuaTitle, BorderLayout.WEST);
        pnlThua.add(lblTienThua,  BorderLayout.EAST);

        pnlTienMat.add(pnlNhapTien);
        pnlTienMat.add(Box.createVerticalStrut(6));
        pnlTienMat.add(pnlGrid);
        pnlTienMat.add(Box.createVerticalStrut(10));
        pnlTienMat.add(pnlThua);

        // ── QR ──
        pnlQR = new JPanel(new BorderLayout());
        pnlQR.setOpaque(false);
        lblQR = new JLabel("", SwingConstants.CENTER);
        lblQR.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblQR.setToolTipText("Click để phóng to mã QR");
        lblQR.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (originalQRImageIcon != null) showZoomedQRDialog();
            }
        });
        pnlQR.add(lblQR, BorderLayout.CENTER);
        pnlQR.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (originalQRImageIcon != null) scaleAndSetQR();
            }
        });

        pnlSwitch.add(pnlTienMat, "TIEN_MAT");
        pnlSwitch.add(pnlQR,      "QR");
        content.add(pnlSwitch);

        ActionListener ptttListener = ev -> {
            boolean isBank = btnChuyenKhoan.isSelected();
            cardSwitch.show(pnlSwitch, isBank ? "QR" : "TIEN_MAT");
            if (isBank) { toggleQRCode(); startBankChecking(); }
            else        { stopBankChecking(); tinhTienThua(); }
        };
        btnTienMat.addActionListener(ptttListener);
        btnChuyenKhoan.addActionListener(ptttListener);

        content.add(Box.createVerticalGlue());
        pnlRight.add(content, BorderLayout.CENTER);
        return pnlRight;
    }

    // ── BOTTOM BAR ────────────────────────────────────────────────
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new MatteBorder(1,0,0,0,BORDER_C));

        JButton btnQuayLai = makeOutlineBtn("Quay lại", loadIcon("/Images/logoBack.png",14,14));
        btnQuayLai.setBorder(new EmptyBorder(6,16,6,16));
        btnQuayLai.addActionListener(e -> {
            stopAllTimers();
            if (onQuayLai != null) onQuayLai.accept(secondsLeft);
        });

        JButton btnHuyVe = new JButton("Hủy vé") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? new Color(160,20,20) :
                            getModel().isRollover() ? new Color(200,40,40) :
                                                      new Color(210,30,40));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnHuyVe.setFont(FONT_B14); btnHuyVe.setForeground(Color.WHITE);
        btnHuyVe.setBorder(new EmptyBorder(6,16,6,16));
        btnHuyVe.setContentAreaFilled(false); btnHuyVe.setBorderPainted(false);
        btnHuyVe.setFocusPainted(false); btnHuyVe.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuyVe.addActionListener(e -> showHuyVePopup());

        lblCountdown = new JLabel("Thời hạn giữ vé: --:--") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,235,235));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.setColor(getBackground()); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        lblCountdown.setFont(FONT_B14); lblCountdown.setForeground(new Color(190,30,30));
        lblCountdown.setOpaque(false);  lblCountdown.setBackground(new Color(200,60,60));
        lblCountdown.setBorder(new EmptyBorder(6,12,6,12));

        JButton btnLuuTam = makeNavyBtn("Lưu tạm", null);
        btnLuuTam.addActionListener(e -> {
            boolean ok = luuDuLieuVaoDatabase("Lưu tạm");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã lưu vé chờ thanh toán!");
                stopAllTimers();
                if (onQuayLai != null) onQuayLai.accept(secondsLeft);
            }
        });

        JButton btnThanhToan = makeNavyBtn("Thanh toán", loadIcon("/Images/logoGoOn.png",14,14));
        btnThanhToan.setHorizontalTextPosition(SwingConstants.LEFT);
        btnThanhToan.addActionListener(e -> {
            if (btnTienMat.isSelected()) showXacNhanThanhToanPopup();
            // Chuyển khoản: xử lý tự động qua bankCheckTimer
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        left.setBackground(Color.WHITE);
        left.add(btnQuayLai); left.add(btnHuyVe);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4));
        right.setBackground(Color.WHITE);
        right.add(lblCountdown); right.add(btnLuuTam); right.add(btnThanhToan);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── POPUP HỦY VÉ ─────────────────────────────────────────────
    private void showHuyVePopup() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog  = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        // Panel nền mờ (không che hoàn toàn)
        JPanel glass = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                g2.setColor(new Color(10, 20, 50));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);

        JPanel box = buildPopupBox(380, 210);
        box.setBorder(new EmptyBorder(28,32,24,32));

        JLabel lblIcon = new JLabel("⚠", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        lblIcon.setForeground(new Color(220,100,0));

        JLabel lblMsg = new JLabel(
            "<html><div style='text-align:center;'>"
            + "<b style='font-size:14px;color:#1c396e;'>Xác nhận hủy vé?</b><br><br>"
            + "<span style='font-size:13px;color:#555;'>Tất cả thông tin đặt vé sẽ bị xóa.<br>"
            + "Bạn sẽ được chuyển về trang tìm chuyến.</span>"
            + "</div></html>", SwingConstants.CENTER);

        JPanel topContent = new JPanel(new BorderLayout(0,10));
        topContent.setOpaque(false);
        topContent.add(lblIcon, BorderLayout.NORTH);
        topContent.add(lblMsg,  BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1,2,12,0));
        btnRow.setOpaque(false);

        JButton btnNo = makeOutlineBtn("Không, quay lại", null);
        btnNo.setFont(FONT_14);
        btnNo.setBorder(new EmptyBorder(10,16,10,16));
        btnNo.addActionListener(ev -> dialog.dispose());

        JButton btnYes = makeRedBtn("Xác nhận");
        btnYes.addActionListener(ev -> {
            dialog.dispose();
            stopAllTimers();
            quayVeTrangDau();
        });

        btnRow.add(btnNo); btnRow.add(btnYes);
        box.add(topContent, BorderLayout.CENTER);
        box.add(btnRow,     BorderLayout.SOUTH);
        glass.add(box);

        setupAndShowDialog(dialog, glass, ancestor);
    }

    // ── POPUP XÁC NHẬN THANH TOÁN TIỀN MẶT ──────────────────────
    private void showXacNhanThanhToanPopup() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog  = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        JPanel glass = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                g2.setColor(new Color(10,20,50));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);

        double tienKhach   = parseMoney(txtTienKhachDua.getText());
        String tienKhachStr= tienKhach > 0 ? DF.format(tienKhach) : "(chưa nhập)";
        double tienThua    = tienKhach > 0 ? tienKhach - tongThanhToan : 0;

        // Tính chiều cao động dựa trên số dòng nội dung
        JPanel box = buildPopupBox(400, tienKhach > 0 ? 240 : 220);
        box.setBorder(new EmptyBorder(28,32,24,32));

        String thuaHtml = tienKhach > 0
            ? "<br><span style='font-size:14px;color:#555;'>Tiền thừa: <b style='color:#1a7a30;'>"
              + DF.format(Math.max(0, tienThua)) + "</b></span>"
            : "";
        String thieu = tienKhach > 0 && tienThua < 0
            ? "<br><span style='font-size:14px;color:#cc0000;'>⚠ Tiền khách đưa chưa đủ!</span>"
            : "";

        JLabel lblMsg = new JLabel(
            "<html><div style='text-align:center;'>"
            + "<b style='font-size:15px;color:#1c396e;'>Xác nhận thanh toán tiền mặt</b>"
            + "<br><br>"
            + "<span style='font-size:14px;color:#555;'>Tổng thanh toán: "
            + "<b style='color:#c82020;'>" + DF.format(tongThanhToan) + "</b></span><br>"
            + "<span style='font-size:14px;color:#555;'>Khách đưa: <b>"
            + tienKhachStr + "</b></span>"
            + thuaHtml + thieu
            + "</div></html>", SwingConstants.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1,2,12,0));
        btnRow.setOpaque(false);

        JButton btnNo = makeOutlineBtn("Hủy", null);
        btnNo.setFont(FONT_14);
        btnNo.setBorder(new EmptyBorder(10,16,10,16));
        btnNo.addActionListener(ev -> dialog.dispose());

        JButton btnYes = makeNavyBtn("Xác nhận", null);
        btnYes.setFont(FONT_B14);
        btnYes.setBorder(new EmptyBorder(10,16,10,16));
        btnYes.addActionListener(ev -> {
            dialog.dispose();
            xuLyHoanTatThanhToan("Tiền mặt");
        });

        btnRow.add(btnNo); btnRow.add(btnYes);
        box.add(lblMsg,  BorderLayout.CENTER);
        box.add(btnRow,  BorderLayout.SOUTH);
        glass.add(box);

        setupAndShowDialog(dialog, glass, ancestor);
    }

    private void showThanhCongPopup(Runnable onDone) {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog  = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        final float[] alpha = {0f}; // animation fade in

        JPanel glass = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Mờ nhẹ — vẫn thấy nền
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g2.setColor(new Color(10, 20, 50));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);

        final int[] frame = {0};
        final int TOTAL_FRAMES = 30;

        JPanel box = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Popup hiện dần theo alpha
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha[0]));
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                int cx = getWidth()/2, cy = 70, r = 38;
                float progress = Math.min(1f, (float) frame[0] / TOTAL_FRAMES);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha[0]));
                g2.setColor(new Color(220, 245, 220));
                g2.fillOval(cx-r, cy-r, r*2, r*2);
                g2.setColor(new Color(34, 170, 70));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(cx-r, cy-r, r*2, r*2);

                if (progress > 0) {
                    g2.setColor(new Color(34, 170, 70));
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int x1=cx-18, y1=cy, xMid=cx-6, yMid=cy+14, x2=cx+20, y2=cy-16;
                    float p1 = Math.min(1f, progress/0.5f);
                    g2.drawLine(x1, y1, (int)(x1+(xMid-x1)*p1), (int)(y1+(yMid-y1)*p1));
                    if (progress > 0.5f) {
                        float p2 = (progress-0.5f)/0.5f;
                        g2.drawLine(xMid, yMid, (int)(xMid+(x2-xMid)*p2), (int)(yMid+(y2-yMid)*p2));
                    }
                }
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(320, 240));
        box.setBorder(new EmptyBorder(140, 24, 24, 24));

        JLabel lblMsg = new JLabel(
            "<html><div style='text-align:center;'>"
            + "<b style='font-size:15px;color:#1c396e;'>Thanh toán thành công!</b><br>"
            + "<span style='font-size:13px;color:#888;'>Đang chuyển về trang chính...</span>"
            + "</div></html>", SwingConstants.CENTER);
        box.add(lblMsg, BorderLayout.CENTER);
        glass.add(box);

        dialog.setContentPane(glass);
        dialog.setSize(ancestor.getSize());
        dialog.setLocation(ancestor.getLocation());

        // Timer vẽ tick + fade in popup
        javax.swing.Timer animTimer = new javax.swing.Timer(16, null);
        animTimer.addActionListener(ev -> {
            frame[0]++;
            // Fade in trong 20 frame đầu
            alpha[0] = Math.min(1f, frame[0] / 20f);
            glass.repaint();
            box.repaint();
            if (frame[0] >= TOTAL_FRAMES) animTimer.stop();
        });
        animTimer.start();

        // Tự đóng sau 4.5 giây (thêm 1s so với trước)
        javax.swing.Timer closeTimer = new javax.swing.Timer(4500, ev -> {
            animTimer.stop();
            dialog.dispose();
            if (onDone != null) onDone.run();
        });
        closeTimer.setRepeats(false);
        closeTimer.start();

        dialog.setVisible(true);
    }

    // ── ZOOM QR ───────────────────────────────────────────────────
    private void showZoomedQRDialog() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog  = new JDialog(ancestor, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        JPanel glass = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2.setColor(new Color(10,20,50));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);

        JPanel box = buildPopupBox(420, 480);
        box.setBorder(new EmptyBorder(10,20,20,20));

        JPanel pnlHeader = new JPanel(new BorderLayout()); pnlHeader.setOpaque(false);
        JLabel lblTitle  = new JLabel("Mã thanh toán hóa đơn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblTitle.setForeground(NAVY);
        JButton btnClose = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillOval(0,0,getWidth(),getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnClose.setFont(new Font("Segoe UI",Font.BOLD,14)); btnClose.setForeground(new Color(200,40,40));
        btnClose.setContentAreaFilled(false); btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false); btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(28,28));
        btnClose.addActionListener(e -> dialog.dispose());
        pnlHeader.add(lblTitle, BorderLayout.WEST); pnlHeader.add(btnClose, BorderLayout.EAST);

        Image scaleImg = originalQRImageIcon.getImage().getScaledInstance(350,350,Image.SCALE_SMOOTH);
        JLabel lblBigQR = new JLabel(new ImageIcon(scaleImg)); lblBigQR.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblFoot = new JLabel("Mở ứng dụng ngân hàng/ví điện tử để quét mã", SwingConstants.CENTER);
        lblFoot.setFont(new Font("Segoe UI",Font.ITALIC,11)); lblFoot.setForeground(Color.GRAY);

        box.add(pnlHeader, BorderLayout.NORTH);
        box.add(lblBigQR,  BorderLayout.CENTER);
        box.add(lblFoot,   BorderLayout.SOUTH);
        glass.add(box);

        setupAndShowDialog(dialog, glass, ancestor);
    }

    // ── QR SCALE ─────────────────────────────────────────────────
    private void scaleAndSetQR() {
        if (originalQRImageIcon == null) return;
        int size = pnlQR.getWidth()>40 ? pnlQR.getWidth()-20 : 160;
        size = Math.min(size, pnlQR.getHeight()>40 ? pnlQR.getHeight()-20 : size);
        Image img = originalQRImageIcon.getImage().getScaledInstance(size,size,Image.SCALE_SMOOTH);
        lblQR.setIcon(new ImageIcon(img)); lblQR.setText("");
    }

    // ── BANK CHECKING THỰC TẾ (MBBank qua SePay / casso) ────────
    /**
     * Gọi API kiểm tra giao dịch của MBBank.
     * Sử dụng Casso webhook API (https://casso.vn) hoặc SePay —
     * đây là giải pháp phổ biến để webhook giao dịch ngân hàng VN.
     *
     * Thay YOUR_CASSO_API_KEY bằng API key thực từ casso.vn sau khi
     * kết nối tài khoản MBBank 0382588430.
     *
     * Endpoint: GET https://oauth.casso.vn/v2/transactions?page=1&pageSize=20
     * Header: Authorization: apikey YOUR_CASSO_API_KEY
     */
    private static final String CASSO_API_KEY = "AK_CS.69d49310536411f1ad2d7bbf51f870c4.1OR4aZOPpK4BslQXgsgNQGlFiMwe8EDKc6Tuva6vzVcTf7ssLfssoXfn5vVKU27z4bemHq6E"; // ← thay key thực tại đây

    private void startBankChecking() {
        stopBankChecking();
        System.out.println("[BANK] startBankChecking called");
        bankCheckTimer = new javax.swing.Timer(5000, e -> {
            System.out.println("[BANK] Timer tick - checking...");
            boolean received = checkBankReceived();
            System.out.println("[BANK] received=" + received);
            if (received) {
                bankCheckTimer.stop();
                xuLyHoanTatThanhToan("Chuyển khoản VietQR");
            }
        });
        bankCheckTimer.start();
    }

    /**
     * Kiểm tra tài khoản MBBank 0382588430 đã nhận đủ tiền chưa.
     * So khớp: nội dung chuyển khoản chứa maHD VÀ số tiền >= tongThanhToan.
     *
     * API Casso trả về JSON dạng:
     * { "error": 0, "data": { "records": [ { "amount": 240000,
     *   "description": "Thanh toan ve tau HD12345", ... } ] } }
     */
 
    private boolean checkBankReceived() {
        System.out.println("[CASSO] called, key starts with: " + CASSO_API_KEY.substring(0, Math.min(8, CASSO_API_KEY.length())));
        if (CASSO_API_KEY.equals("YOUR_CASSO_API_KEY")) {
            System.out.println("[CASSO] key chưa set!");
            return false;
        }
        try {
            URL url = new URL("https://oauth.casso.vn/v2/transactions?page=1&pageSize=20&sort=DESC");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "apikey " + CASSO_API_KEY);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            System.out.println("[CASSO] HTTP code: " + code);

            BufferedReader br = new BufferedReader(
                new InputStreamReader(
                    code == 200 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String json = sb.toString();
            System.out.println("[CASSO] Response: " + json.substring(0, Math.min(300, json.length())));

            if (code != 200) return false;

            String jsonLower = json.toLowerCase();
            boolean coMaHD = jsonLower.contains(maHD.toLowerCase());
            boolean coTien  = jsonLower.contains(String.valueOf((long) tongThanhToan));
            System.out.println("[CASSO] maHD=" + maHD + " coMaHD=" + coMaHD + " coTien=" + coTien);
            return coMaHD && coTien;

        } catch (Exception e) {
            System.out.println("[CASSO] Exception: " + e.getClass().getName() + " - " + e.getMessage());
            return false;
        }
    }

    private void stopBankChecking() {
        if (bankCheckTimer != null) bankCheckTimer.stop();
    }

    // ── HOÀN TẤT THANH TOÁN ──────────────────────────────────────
    private void xuLyHoanTatThanhToan(String hinhThuc) {
        stopAllTimers();
        boolean ok = luuDuLieuVaoDatabase(hinhThuc);
        if (ok) {
            showThanhCongPopup(() -> {
                inHoaDon();
                quayVeTrangDau();
            });
        }
    }

    /**
     * Quay về trang DatVeGUI (card "dat-ve") và reset form.
     */
    private void quayVeTrangDau() {
        Container parent = getParent();
        if (parent == null) {
            if (onHuyVe != null) { onHuyVe.run(); return; }
            if (onQuayLai != null) { onQuayLai.accept(0); return; }
            return;
        }
        LayoutManager lm = parent.getLayout();
        if (lm instanceof CardLayout) {
            // Tìm và reset DatVeGUI trước khi show
            for (Component c : parent.getComponents()) {
                if (c instanceof DatVeGUI) {
                    ((DatVeGUI) c).resetForm();
                }
            }
            ((CardLayout) lm).show(parent, "dat-ve");
        } else {
            // BorderLayout hoặc khác
            if (onHuyVe != null) { onHuyVe.run(); return; }
            if (onQuayLai != null) { onQuayLai.accept(0); }
        }
    }

    // ── DATABASE ─────────────────────────────────────────────────
    private boolean luuDuLieuVaoDatabase(String hinhThucThanhToan) {
        java.sql.Connection con = null;
        try {
            con = connect_DB.Connect_DB.getInstance().getConnection();
            con.setAutoCommit(false);

            String maNV = "NV001";
            try (java.sql.Statement st = con.createStatement();
                 java.sql.ResultSet rsNV = st.executeQuery("SELECT TOP 1 maNV FROM NhanVien")) {
                if (rsNV.next()) maNV = rsNV.getString(1);
            }

            String sqlHD = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, " +
                           "tienNhan, phuongThucThanhToan) VALUES (?, GETDATE(), ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement psHD = con.prepareStatement(sqlHD)) {
                psHD.setString(1, maHD); psHD.setString(2, maNV);
                String sdtKhach = modelFromGUI2.getRowCount()>0
                    ? modelFromGUI2.getValueAt(0,7).toString() : "";
                try {
                    entity.KhachHang kh = new dao.KhachHangDAO().timTheoSDT(sdtKhach);
                    if (kh!=null) psHD.setString(3, kh.getMaKH());
                    else          psHD.setNull(3, java.sql.Types.VARCHAR);
                } catch (Exception e2) { psHD.setNull(3, java.sql.Types.VARCHAR); }
                psHD.setDouble(4, tongThanhToan);
                double tienKhach = hinhThucThanhToan.contains("Chuyển khoản")
                    ? tongThanhToan : parseMoney(txtTienKhachDua.getText());
                if (tienKhach <= 0) tienKhach = tongThanhToan;
                psHD.setDouble(5, tienKhach);
                String pt = hinhThucThanhToan.contains("Tiền mặt") ? "TIEN_MAT"
                          : hinhThucThanhToan.equals("Lưu tạm")    ? "LUU_TAM"
                                                                    : "CHUYEN_KHOAN";
                psHD.setString(6, pt);
                psHD.executeUpdate();
            }

            String sqlVe = "INSERT INTO Ve (maVe, ngayMua, loaiVe, trangThaiVe, giaVe, " +
                           "maGhe, maHoaDon, maChuyenTau) VALUES (?, GETDATE(), ?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement psVe = con.prepareStatement(sqlVe)) {
                String trangThai = hinhThucThanhToan.equals("Lưu tạm")
                    ? "Chờ thanh toán" : "Đã thanh toán";
                for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                    psVe.setString(1, modelChiTiet.getValueAt(i,1).toString());
                    psVe.setString(2, modelFromGUI2.getValueAt(i,2).toString().contains("hồi")
                        ? "KHU_HOI" : "MOT_CHIEU");
                    psVe.setString(3, trangThai);
                    psVe.setDouble(4, parseMoney(modelChiTiet.getValueAt(i,4).toString()));
                    psVe.setString(5, modelFromGUI2.getValueAt(i,4).toString());
                    psVe.setString(6, maHD);
                    psVe.setString(7, modelFromGUI2.getValueAt(i,12).toString());
                    psVe.addBatch();
                }
                psVe.executeBatch();
            }

            con.commit(); return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con!=null) try { con.rollback(); } catch (Exception ex) {}
            JOptionPane.showMessageDialog(this,"Lỗi database:\n"+e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            if (con!=null) try { con.setAutoCommit(true); con.close(); } catch (Exception ex) {}
        }
    }

    private void inHoaDon() {
        System.out.println("==========================================");
        System.out.println("          HÓA ĐƠN VÉ TÀU HỎA             ");
        System.out.println("Mã HD: " + maHD);
        System.out.println("Tổng tiền: " + DF.format(tongThanhToan));
        System.out.println("==========================================");
    }

    // ── TÍNH TOÁN ────────────────────────────────────────────────
    private void tinhToanTaiChinh() {
        tongGiaGoc = 0; tongGiamDoiTuong = 0;
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            tongGiaGoc       += parseMoney(modelChiTiet.getValueAt(i,4).toString());
            tongGiamDoiTuong += parseMoney(modelChiTiet.getValueAt(i,6).toString());
        }
        double tongKM = tongGiamDoiTuong + giamVoucher;
        tongThanhToan = Math.max(0, tongGiaGoc - tongKM);

        lblTongTien.setText("<html><font color='#505050'>Tổng tiền: </font><b>"
            + DF.format(tongGiaGoc) + "</b></html>");
        lblTongKhuyenMai.setText("<html><font color='#505050'>Tổng KM: </font><b>"
            + DF.format(tongKM) + "</b></html>");
        lblThanhToanConLai.setText("<html><font color='#505050'>Còn lại: </font><b>"
            + "<font color='#C82020'>" + DF.format(tongThanhToan) + "</font></b></html>");

        if (btnChuyenKhoan != null && btnChuyenKhoan.isSelected()) toggleQRCode();
        else tinhTienThua();
    }

    private void tinhTienThua() {
        if (txtTienKhachDua==null || lblTienThua==null) return;
        double tk   = parseMoney(txtTienKhachDua.getText());
        double thua = tk - tongThanhToan;
        if      (tk==0)    { lblTienThua.setText("0 VNĐ");                                   lblTienThua.setForeground(Color.BLACK); }
        else if (thua < 0) { lblTienThua.setText("Thiếu: "+DF.format(Math.abs(thua)));       lblTienThua.setForeground(Color.RED); }
        else               { lblTienThua.setText(DF.format(thua));                            lblTienThua.setForeground(new Color(0,140,0)); }
    }

    private void nhapKhuyenMai() {
        String ma = JOptionPane.showInputDialog(this, "Nhập mã khuyến mãi:");
        if (ma!=null && ma.trim().equalsIgnoreCase("SALE100")) {
            giamVoucher = 100000;
            JOptionPane.showMessageDialog(this, "Đã áp dụng giảm 100,000 VNĐ");
        } else if (ma!=null && !ma.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"Mã không hợp lệ!","Lỗi",JOptionPane.ERROR_MESSAGE);
        } else { giamVoucher = 0; }
        tinhToanTaiChinh();
    }

    private void toggleQRCode() {
        if (!btnChuyenKhoan.isSelected()) return;
        if (tongThanhToan <= 0) { lblQR.setIcon(null); lblQR.setText("Hóa đơn 0đ"); return; }
        lblQR.setIcon(null); lblQR.setText("Đang tạo mã VietQR...");
        new SwingWorker<ImageIcon, Void>() {
            @Override protected ImageIcon doInBackground() throws Exception {
                // Nội dung chuyển khoản chứa maHD để checkBankReceived() so khớp
                String info = URLEncoder.encode("Thanh toan ve tau " + maHD, StandardCharsets.UTF_8);
                // Dùng bank ID chuẩn VietQR cho MBBank = "970422"
                String url  = String.format(
                    "https://img.vietqr.io/image/970422-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
                    ACCOUNT_NO, (long) tongThanhToan, info,
                    URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8));
                return new ImageIcon(new java.net.URL(url));
            }
            @Override protected void done() {
                try { originalQRImageIcon = get(); scaleAndSetQR(); }
                catch (Exception e) { lblQR.setIcon(null); lblQR.setText("Lỗi tạo QR!"); }
            }
        }.execute();
    }

    // ── COUNTDOWN ────────────────────────────────────────────────
    private void startCountdown() {
        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            if (secondsLeft <= 0) {
                stopAllTimers();
                lblCountdown.setText("Hết thời gian!");
                JOptionPane.showMessageDialog(this,
                    "Thời gian giữ vé đã hết!\nVui lòng thực hiện lại.",
                    "Hết thời gian", JOptionPane.WARNING_MESSAGE);
                if (onQuayLai != null) onQuayLai.accept(0);
            } else updateCountdownLabel();
        });
        countdownTimer.start();
        updateCountdownLabel();
    }

    private void updateCountdownLabel() {
        int m = secondsLeft/60, s = secondsLeft%60;
        lblCountdown.setForeground(secondsLeft<=300 ? new Color(160,0,0) : new Color(190,30,30));
        lblCountdown.setBackground(secondsLeft<=300 ? new Color(160,0,0) : new Color(200,60,60));
        lblCountdown.setText(String.format("Thời hạn giữ vé: %02d:%02d",m,s));
    }

    private void stopAllTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (bankCheckTimer  != null) bankCheckTimer.stop();
    }

    // ── UI HELPERS ────────────────────────────────────────────────
    /** Panel box popup bo góc trắng */
    private JPanel buildPopupBox(int w, int h) {
        JPanel box = new JPanel(new BorderLayout(0,16)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(w, h));
        return box;
    }

    /** Gán content pane, đăng ký ESC, set size theo ancestor rồi show */
    private void setupAndShowDialog(JDialog dialog, JPanel glass, Window ancestor) {
        dialog.setContentPane(glass);
        dialog.getRootPane().registerKeyboardAction(ev -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setSize(ancestor.getSize());
        dialog.setLocation(ancestor.getLocation());
        dialog.setVisible(true);
    }

    private double parseMoney(String str) {
        try { return Double.parseDouble(str.replaceAll("[^0-9]","")); }
        catch (Exception e) { return 0; }
    }

    private JPanel createDetailLabel(String title, String value) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        JLabel l1 = new JLabel(title); l1.setFont(FONT_14); l1.setForeground(new Color(80,80,80));
        JLabel l2 = new JLabel(value); l2.setFont(FONT_B14); l2.setForeground(Color.BLACK);
        p.add(l1, BorderLayout.WEST); p.add(l2, BorderLayout.EAST);
        return p;
    }

    private JToggleButton createToggleBtn(String text) {
        JToggleButton b = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(new Color(240,246,255)); g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4);
                    g2.setColor(NAVY); g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,4,4);
                } else {
                    g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),4,4);
                    g2.setColor(BORDER_C); g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,4,4);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.addChangeListener(e -> {
            b.setFont(b.isSelected() ? FONT_B14 : FONT_14);
            b.setForeground(b.isSelected() ? NAVY : new Color(80,80,80));
        });
        b.setFont(FONT_14); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6,12,6,12));
        return b;
    }

    private Icon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url!=null)
                return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH));
        } catch (Exception ignored) {}
        return null;
    }

    private JButton makeNavyBtn(String text, Icon icon) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(18,42,85)
                    : getModel().isRollover() ? new Color(38,68,128) : NAVY);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        if (icon!=null) { b.setIcon(icon); b.setHorizontalTextPosition(SwingConstants.LEFT); }
        b.setFont(FONT_B14); b.setForeground(Color.WHITE); b.setIconTextGap(8);
        b.setBorder(new EmptyBorder(6,18,6,18));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeOutlineBtn(String text, Icon icon) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(220,230,245)
                    : getModel().isRollover() ? new Color(230,240,250) : new Color(242,247,252));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.setColor(NAVY); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        if (icon!=null) b.setIcon(icon);
        b.setFont(FONT_14); b.setForeground(NAVY); b.setIconTextGap(8);
        b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeRedBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(160,20,20)
                    : getModel().isRollover() ? new Color(200,40,40) : new Color(210,30,40));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(FONT_B14); b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(10,16,10,16));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}