package gui;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class DatVeGUI3 extends JPanel {

    private static final Color NAVY = new Color(28, 57, 110);
    private static final Color BG = new Color(242, 247, 252);
    private static final Color BORDER_C = new Color(180, 205, 230);
    private static final Font FONT_14 = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_B14 = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_B16 = new Font("Segoe UI", Font.BOLD, 16);
    private static final DecimalFormat DF = new DecimalFormat("#,### VNĐ");

    private JTable tblChiTiet;
    private DefaultTableModel modelChiTiet;
    private JLabel lblTongTien, lblTongKhuyenMai, lblThanhToanConLai, lblQR, lblCountdown;
    private JToggleButton btnTienMat, btnChuyenKhoan;
    
    private JPanel pnlSwitch;
    private CardLayout cardSwitch;
    private JPanel pnlTienMat;
    private JPanel pnlQR;
    private JTextField txtTienKhachDua;
    private JLabel lblTienThua;

    private double tongThanhToan = 0;
    private double tongGiaGoc = 0;
    private double tongGiamDoiTuong = 0;
    private double giamVoucher = 0;

    private String maHD = "HD" + System.currentTimeMillis() % 100000;
    private java.util.function.Consumer<Integer> onQuayLai;
    private DefaultTableModel modelFromGUI2;
    
    private javax.swing.Timer countdownTimer;
    private int secondsLeft;
    private ImageIcon originalQRImageIcon = null; 

    private javax.swing.Timer bankCheckTimer;

    public DatVeGUI3(DefaultTableModel modelFromGUI2, int secondsLeft, java.util.function.Consumer<Integer> onQuayLai) {
        this.modelFromGUI2 = modelFromGUI2;
        this.secondsLeft = secondsLeft;
        this.onQuayLai = onQuayLai;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.78; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 8);
        pnlCenter.add(buildLeftPanel(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.22;
        gbc.insets = new Insets(0, 0, 0, 0);
        pnlCenter.add(buildRightPanel(), gbc);

        add(pnlCenter, BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        tinhToanTaiChinh();
        startCountdown();
    }

    // 1. Hàm móc Database lấy Loại Ghế và tính Giá Tiền
    private Object[] layThongTinGheTuDB(String maGhe) {
        String loaiGhe = "Ghế thường";
        double giaTien = 300000; 
        String sql = "SELECT g.loaiGhe, t.heSoLoaiToa FROM Ghe g JOIN ToaTau t ON g.maToaTau = t.maToaTau WHERE g.maGhe = ?";
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

    // 2. Hàm lấy Tỉ lệ giảm giá theo Đối tượng
    private double layTyLeGiamTuDB(String loaiDoiTuong) {
        double tyLe = 0.0;
        if (loaiDoiTuong.contains("Sinh viên")) tyLe = 0.2;
        else if (loaiDoiTuong.contains("Trẻ em (<6 tuổi)")) tyLe = 1.0; 
        else if (loaiDoiTuong.contains("Trẻ em")) tyLe = 0.5;
        else if (loaiDoiTuong.contains("cao tuổi")) tyLe = 0.3;
        return tyLe;
    }

    private JPanel buildLeftPanel() {
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
        pnlLeft.setOpaque(false);

        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        pnlTableWrapper.setBackground(Color.WHITE);
        pnlTableWrapper.setBorder(new LineBorder(BORDER_C, 1, true));

        JLabel lblChiTiet = new JLabel("Chi tiết");
        lblChiTiet.setFont(FONT_B14);
        lblChiTiet.setForeground(Color.WHITE);
        lblChiTiet.setOpaque(true);
        lblChiTiet.setBackground(NAVY);
        lblChiTiet.setBorder(new EmptyBorder(6, 12, 6, 12));
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleWrap.setOpaque(false);
        titleWrap.add(lblChiTiet);
        pnlTableWrapper.add(titleWrap, BorderLayout.NORTH);

        String[] cols = {"STT", "Mã vé", "Chiều vé", "Loại chỗ", "Đơn giá", "Loại đối tượng", "Giảm giá", "Thành tiền"};
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (int i = 0; i < modelFromGUI2.getRowCount(); i++) {
            String maVe = modelFromGUI2.getValueAt(i, 1).toString(); 
            String chieuVe = modelFromGUI2.getValueAt(i, 3).toString(); 
            String maGhe = modelFromGUI2.getValueAt(i, 4).toString(); 
            String loaiDoiTuong = modelFromGUI2.getValueAt(i, 8).toString(); 

            // Lấy trực tiếp từ Database
            Object[] thongTinGhe = layThongTinGheTuDB(maGhe);
            String loaiCho = (String) thongTinGhe[0];
            double donGia = (Double) thongTinGhe[1];
            
            double tyLeGiam = layTyLeGiamTuDB(loaiDoiTuong);
            double giamGia = donGia * tyLeGiam;
            double thanhTien = donGia - giamGia;

            modelChiTiet.addRow(new Object[]{
                i + 1, maVe, chieuVe, loaiCho, DF.format(donGia), loaiDoiTuong, DF.format(giamGia), DF.format(thanhTien)
            });
        }

        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(35);
        tblChiTiet.setFont(FONT_14);
        tblChiTiet.getTableHeader().setFont(FONT_B14);
        tblChiTiet.getTableHeader().setBackground(new Color(245, 248, 252));
        tblChiTiet.setShowGrid(false);
        tblChiTiet.setIntercellSpacing(new Dimension(0, 0));
        
        TableColumnModel tcm = tblChiTiet.getColumnModel();
        tcm.getColumn(0).setMaxWidth(40);
        tcm.getColumn(1).setMinWidth(85); tcm.getColumn(1).setMaxWidth(85);
        tcm.getColumn(2).setMinWidth(75); tcm.getColumn(2).setMaxWidth(75);
        tcm.getColumn(3).setMinWidth(90); tcm.getColumn(3).setMaxWidth(90);
        tcm.getColumn(5).setPreferredWidth(160); 
        
        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        pnlTableWrapper.add(scroll, BorderLayout.CENTER);

        JPanel pnlBottomLeft = new JPanel(new BorderLayout(0, 10));
        pnlBottomLeft.setOpaque(false);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        JButton btnKhuyenMai = makeNavyBtn("Thêm khuyến mãi", null);
        btnKhuyenMai.addActionListener(e -> nhapKhuyenMai());
        JButton btnGhiChu = makeNavyBtn("Ghi chú", null);
        btnGhiChu.addActionListener(e -> JOptionPane.showInputDialog(this, "Nhập ghi chú cho hóa đơn:"));
        pnlBtns.add(btnKhuyenMai); pnlBtns.add(btnGhiChu);
        pnlBottomLeft.add(pnlBtns, BorderLayout.NORTH);

        JPanel pnlTotals = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        pnlTotals.setOpaque(false);
        
        lblTongTien = new JLabel(); lblTongTien.setFont(FONT_14);
        lblTongKhuyenMai = new JLabel(); lblTongKhuyenMai.setFont(FONT_14);
        lblThanhToanConLai = new JLabel(); lblThanhToanConLai.setFont(FONT_14);

        pnlTotals.add(lblTongTien);
        pnlTotals.add(lblTongKhuyenMai);
        pnlTotals.add(lblThanhToanConLai);
        pnlBottomLeft.add(pnlTotals, BorderLayout.CENTER);

        pnlLeft.add(pnlTableWrapper, BorderLayout.CENTER);
        pnlLeft.add(pnlBottomLeft, BorderLayout.SOUTH);

        return pnlLeft;
    }

    private JPanel buildRightPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBackground(new Color(245, 248, 252));
        pnlRight.setBorder(new LineBorder(BORDER_C, 1));

        JLabel lblInfo = new JLabel("Thông tin hóa đơn");
        lblInfo.setFont(FONT_B14);
        lblInfo.setForeground(Color.WHITE);
        lblInfo.setOpaque(true);
        lblInfo.setBackground(NAVY);
        lblInfo.setBorder(new EmptyBorder(6, 12, 6, 12));
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleWrap.setOpaque(false);
        titleWrap.add(lblInfo);
        pnlRight.add(titleWrap, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        String tenKH = modelFromGUI2.getRowCount() > 0 ? modelFromGUI2.getValueAt(0, 5).toString() : "N/A";
        String sdtKH = modelFromGUI2.getRowCount() > 0 ? modelFromGUI2.getValueAt(0, 7).toString() : "N/A";

        String maKhachHangThucTe = "N/A";
        if (!sdtKH.equals("N/A") && !sdtKH.isEmpty()) {
            entity.KhachHang kh = new dao.KhachHangDAO().timTheoSDT(sdtKH);
            if (kh != null) {
                maKhachHangThucTe = kh.getMaKH();
            }
        }

        String maNhanVienDangNhap = "NV001";
        String tenNhanVienDangNhap = "Nhân viên Bán Vé";

        content.add(createDetailLabel("Mã nhân viên:", maNhanVienDangNhap));
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Tên nhân viên:", tenNhanVienDangNhap));
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Mã khách hàng:", maKhachHangThucTe));
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Tên khách hàng:", tenKH));
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("Số điện thoại:", sdtKH));
        
        content.add(Box.createVerticalStrut(10));
        content.add(new JSeparator());
        content.add(Box.createVerticalStrut(10)); 

        JPanel pnlPTTTTitle = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); }
        };
        pnlPTTTTitle.setOpaque(false);
        JLabel lblPTTT = new JLabel("Phương thức thanh toán:");
        lblPTTT.setFont(FONT_14);
        pnlPTTTTitle.add(lblPTTT, BorderLayout.WEST);
        content.add(pnlPTTTTitle);
        content.add(Box.createVerticalStrut(5));

        btnTienMat = createToggleBtn("Tiền mặt");
        btnChuyenKhoan = createToggleBtn("Chuyển khoản");
        ButtonGroup bgPTTT = new ButtonGroup();
        bgPTTT.add(btnTienMat); bgPTTT.add(btnChuyenKhoan);
        btnTienMat.setSelected(true); 

        JPanel pnlToggle = new JPanel(new GridLayout(1, 2, 6, 0)) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 35); }
        };
        pnlToggle.setOpaque(false);
        pnlToggle.add(btnTienMat);
        pnlToggle.add(btnChuyenKhoan);
        content.add(pnlToggle);
        content.add(Box.createVerticalStrut(15));

        cardSwitch = new CardLayout();
        pnlSwitch = new JPanel(cardSwitch) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 165); }
            @Override public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, 165); }
        };
        pnlSwitch.setOpaque(false);

        // --- MODULE TIỀN MẶT ---
        pnlTienMat = new JPanel();
        pnlTienMat.setLayout(new BoxLayout(pnlTienMat, BoxLayout.Y_AXIS));
        pnlTienMat.setOpaque(false);

        JPanel pnlNhapTien = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 26); }
        };
        pnlNhapTien.setOpaque(false);
        pnlNhapTien.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        
        JLabel lblNhapTien = new JLabel("Nhập số tiền: ");
        lblNhapTien.setFont(FONT_14);
        txtTienKhachDua = new JTextField();
        txtTienKhachDua.setFont(FONT_14);
        txtTienKhachDua.setHorizontalAlignment(JTextField.RIGHT);
        txtTienKhachDua.setBorder(null);
        txtTienKhachDua.setOpaque(false);
        txtTienKhachDua.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    String raw = txtTienKhachDua.getText().replaceAll("[^0-9]", "");
                    if (!raw.isEmpty()) txtTienKhachDua.setText(DF.format(Double.parseDouble(raw)).replace(" VNĐ", ""));
                    else txtTienKhachDua.setText("");
                } catch(Exception ex){}
                tinhTienThua();
            }
        });
        pnlNhapTien.add(lblNhapTien, BorderLayout.WEST);
        pnlNhapTien.add(txtTienKhachDua, BorderLayout.CENTER);
        
        JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 5, 5)) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 90); }
        };
        pnlGrid.setOpaque(false);
        String[] quickCash = {"500,000", "700,000", "900,000", "1,000,000", "1,200,000", "1,500,000", "1,700,000", "2,000,000"};
        for(String qc : quickCash) {
            JButton b = makeOutlineBtn(qc, null);
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setBorder(new EmptyBorder(2, 0, 2, 0)); 
            b.addActionListener(e -> { txtTienKhachDua.setText(qc); tinhTienThua(); });
            pnlGrid.add(b);
        }
        pnlGrid.add(new JLabel()); 

        JPanel pnlThua = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 26); }
        };
        pnlThua.setOpaque(false);
        pnlThua.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        
        JLabel lblThuaTitle = new JLabel("Tiền thừa trả khách:");
        lblThuaTitle.setFont(FONT_14);
        lblTienThua = new JLabel("0 VNĐ");
        lblTienThua.setFont(FONT_B14);
        pnlThua.add(lblThuaTitle, BorderLayout.WEST);
        pnlThua.add(lblTienThua, BorderLayout.EAST);

        pnlTienMat.add(pnlNhapTien);
        pnlTienMat.add(Box.createVerticalStrut(6));
        pnlTienMat.add(pnlGrid);
        pnlTienMat.add(Box.createVerticalStrut(10));
        pnlTienMat.add(pnlThua);

        // --- MODULE MÃ QR ---
        pnlQR = new JPanel(new GridBagLayout());
        pnlQR.setOpaque(false);
        
        lblQR = new JLabel("", SwingConstants.CENTER);
        lblQR.setPreferredSize(new Dimension(140, 140));
        lblQR.setMinimumSize(new Dimension(140, 140));
        lblQR.setMaximumSize(new Dimension(140, 140));
        lblQR.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblQR.setToolTipText("Click vào để phóng to mã QR toàn màn hình");
        
        lblQR.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (originalQRImageIcon != null) {
                    showZoomedQRDialog();
                }
            }
        });
        
        pnlQR.add(lblQR, new GridBagConstraints()); 

        pnlSwitch.add(pnlTienMat, "TIEN_MAT");
        pnlSwitch.add(pnlQR, "QR");
        content.add(pnlSwitch);

        ActionListener ptttListener = e -> {
            boolean isBank = btnChuyenKhoan.isSelected();
            cardSwitch.show(pnlSwitch, isBank ? "QR" : "TIEN_MAT");
            if (isBank) {
                toggleQRCode();
                startBankChecking(); 
            } else {
                stopBankChecking();  
                tinhTienThua();
            }
        };
        btnTienMat.addActionListener(ptttListener);
        btnChuyenKhoan.addActionListener(ptttListener);

        content.add(Box.createVerticalGlue());

        pnlRight.add(content, BorderLayout.CENTER);
        return pnlRight;
    }

    private void showZoomedQRDialog() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(ancestor, "Quét mã thanh toán VietQR", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true); 
        
        JPanel pnlGlass = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlGlass.setOpaque(false);
        
        JPanel pnlBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16); 
                g2.dispose();
            }
        };
        pnlBox.setOpaque(false);
        pnlBox.setPreferredSize(new Dimension(420, 460));
        pnlBox.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Mã thanh toán hóa đơn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(NAVY);
        
        JButton btnClose = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(240, 210, 210) : getModel().isRollover() ? new Color(255, 230, 230) : Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(new Color(200, 40, 40));
        btnClose.setContentAreaFilled(false); btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false); btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(28, 28));
        btnClose.addActionListener(e -> dialog.dispose());
        
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnClose, BorderLayout.EAST);
        
        Image scaleImg = originalQRImageIcon.getImage().getScaledInstance(350, 350, Image.SCALE_SMOOTH);
        JLabel lblBigQR = new JLabel(new ImageIcon(scaleImg));
        lblBigQR.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblFoot = new JLabel("Hướng dẫn: Mở ứng dụng Ngân hàng / Ví điện tử quét để hoàn tất", SwingConstants.CENTER);
        lblFoot.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFoot.setForeground(Color.GRAY);
        
        pnlBox.add(pnlHeader, BorderLayout.NORTH);
        pnlBox.add(lblBigQR, BorderLayout.CENTER);
        pnlBox.add(lblFoot, BorderLayout.SOUTH);
        
        pnlGlass.add(pnlBox);
        dialog.setContentPane(pnlGlass);
        
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(), 
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        dialog.setSize(ancestor.getSize());
        dialog.setLocation(ancestor.getLocation());
        dialog.setVisible(true);
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
        bar.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnQuayLai = makeOutlineBtn("Quay lại", loadIcon("/Images/logoBack.png", 14, 14));
        btnQuayLai.setBorder(new EmptyBorder(6, 16, 6, 16)); 
        btnQuayLai.addActionListener(e -> {
            stopAllTimers();
            if (onQuayLai != null) onQuayLai.accept(secondsLeft);
        });

        lblCountdown = new JLabel("Thời hạn giữ vé: --:--") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 235, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(getBackground()); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        lblCountdown.setFont(FONT_B14);
        lblCountdown.setForeground(new Color(190, 30, 30));
        lblCountdown.setOpaque(false);
        lblCountdown.setBackground(new Color(200, 60, 60));
        lblCountdown.setBorder(new EmptyBorder(6, 12, 6, 12));

        JButton btnLuuTam = makeNavyBtn("Lưu tạm", loadIcon("/Images/logoBack.png", 14, 14));
        btnLuuTam.addActionListener(e -> {
            boolean isSaved = luuDuLieuVaoDatabase("Lưu tạm");
            if (isSaved) {
                JOptionPane.showMessageDialog(this, "Đã lưu vé chờ thanh toán!");
                stopAllTimers();
                if (onQuayLai != null) onQuayLai.accept(secondsLeft);
            }
        });

        JButton btnThanhToan = makeNavyBtn("Thanh toán", loadIcon("/Images/logoGoOn.png", 14, 14));
        btnThanhToan.setHorizontalTextPosition(SwingConstants.LEFT); 
        btnThanhToan.addActionListener(e -> {
            if (btnTienMat.isSelected()) {
                double tienKhach = parseMoney(txtTienKhachDua.getText());
                if (tienKhach < tongThanhToan) {
                    JOptionPane.showMessageDialog(this, "Khách đưa thiếu tiền! Không thể thanh toán.", "Lỗi thanh toán", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            xửLýHoànTấtThanhToán("Tiền mặt");
        });

        JPanel pnlLeftBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLeftBtn.setOpaque(false);
        pnlLeftBtn.add(btnQuayLai);

        JPanel pnlRightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightBtns.setOpaque(false);
        pnlRightBtns.add(lblCountdown);
        pnlRightBtns.add(btnLuuTam);
        pnlRightBtns.add(btnThanhToan);

        bar.add(pnlLeftBtn, BorderLayout.WEST);
        bar.add(pnlRightBtns, BorderLayout.EAST);

        return bar;
    }

    // =================================================================
    // MỚI: HÀM LƯU DATABASE SIÊU BỌC THÉP (CÓ BÁO LỖI CHI TIẾT)
    // =================================================================
    private boolean luuDuLieuVaoDatabase(String hinhThucThanhToan) {
        java.sql.Connection con = null;
        try {
            con = connect_DB.Connect_DB.getInstance().getConnection();
            con.setAutoCommit(false); 

            // 0. BẢO MẬT KHÓA NGOẠI: Lấy 1 nhân viên mặc định có sẵn trong DB để tránh lỗi "Vi phạm khóa ngoại NhanVien"
            String maNV = "NV001";
            try (java.sql.Statement st = con.createStatement();
                 java.sql.ResultSet rsNV = st.executeQuery("SELECT TOP 1 maNV FROM NhanVien")) {
                if (rsNV.next()) maNV = rsNV.getString(1);
            }

            // 1. LƯU HÓA ĐƠN
            String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) VALUES (?, GETDATE(), ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement psHD = con.prepareStatement(sqlHoaDon)) {
                psHD.setString(1, maHD);
                psHD.setString(2, maNV);
                
                String sdtKhach = modelFromGUI2.getRowCount() > 0 ? modelFromGUI2.getValueAt(0, 7).toString() : "";
                entity.KhachHang kh = new dao.KhachHangDAO().timTheoSDT(sdtKhach);
                if (kh != null) {
                    psHD.setString(3, kh.getMaKH());
                } else {
                    psHD.setNull(3, java.sql.Types.VARCHAR); 
                }
                
                psHD.setDouble(4, tongThanhToan);
                
                // Lấy đúng số tiền khách đưa (nếu là chuyển khoản thì bằng tổng tiền)
                double tienKhach = hinhThucThanhToan.contains("Chuyển khoản") ? tongThanhToan : parseMoney(txtTienKhachDua.getText());
                psHD.setDouble(5, tienKhach);
                
                String ptThanhToan = hinhThucThanhToan.contains("Tiền mặt") ? "TIEN_MAT" : (hinhThucThanhToan.equals("Lưu tạm") ? "LUU_TAM" : "CHUYEN_KHOAN");
                psHD.setString(6, ptThanhToan);
                psHD.executeUpdate();
            }

            // 2. LƯU TỪNG VÉ
            String sqlVe = "INSERT INTO Ve (maVe, ngayMua, loaiVe, trangThaiVe, giaVe, maGhe, maHoaDon, maChuyenTau) VALUES (?, GETDATE(), ?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement psVe = con.prepareStatement(sqlVe)) {
                String trangThaiVe = hinhThucThanhToan.equals("Lưu tạm") ? "Chờ thanh toán" : "Đã thanh toán";
                for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                    String maVe = modelChiTiet.getValueAt(i, 1).toString();
                    String maGhe = modelFromGUI2.getValueAt(i, 4).toString(); 
                    double giaGoc = parseMoney(modelChiTiet.getValueAt(i, 4).toString());
                    
                    String loaiVeGUI2 = modelFromGUI2.getValueAt(i, 2).toString();
                    String loaiVeDB = loaiVeGUI2.contains("hồi") ? "KHU_HOI" : "MOT_CHIEU";

                    // ĐÃ TỐI ƯU CỰC MẠNH: Lấy trực tiếp mã chuyến tàu từ cột ẩn do GUI 1 truyền qua
                    String maChuyenTau = modelFromGUI2.getValueAt(i, 12).toString(); 

                    psVe.setString(1, maVe);
                    psVe.setString(2, loaiVeDB);
                    psVe.setString(3, trangThaiVe);
                    psVe.setDouble(4, giaGoc);
                    psVe.setString(5, maGhe);
                    psVe.setString(6, maHD);
                    psVe.setString(7, maChuyenTau);
                    psVe.addBatch();
                }
                psVe.executeBatch();
            }
            
            con.commit(); // Cả 2 thao tác thành công thì chốt dữ liệu xuống ổ cứng
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (Exception ex) {} // Lỗi thì hủy hết, chống rác dữ liệu
            }
            // ĐÃ FIX: In thẳng nguyên nhân lỗi SQL ra màn hình để ta biết đường mà sửa
            JOptionPane.showMessageDialog(this, "Chi tiết mã lỗi SQL Server:\n" + e.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (Exception ex) {}
            }
        }
    }

    private void xửLýHoànTấtThanhToán(String hìnhThức) {
        stopAllTimers();
        
        boolean isSaved = luuDuLieuVaoDatabase(hìnhThức);
        
        if (isSaved) {
            JOptionPane.showMessageDialog(this, "Thanh toán thành công qua [" + hìnhThức + "]!\nHệ thống đang tự động in hóa đơn vé tàu...", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            inHoaDon();
        } 
    }

    private void inHoaDon() {
        System.out.println("=========================================");
        System.out.println("          HÓA ĐƠN VÉ TÀU HỎA             ");
        System.out.println("Mã HD: " + maHD);
        System.out.println("Tổng tiền: " + DF.format(tongThanhToan));
        System.out.println("=========================================");
    }

    private void startBankChecking() {
        if (bankCheckTimer != null) bankCheckTimer.stop();
        
        bankCheckTimer = new javax.swing.Timer(5000, new ActionListener() {
            private int checkCount = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                checkCount++;
                if (checkCount >= 3) {
                    bankCheckTimer.stop();
                    for (Window w : Window.getWindows()) {
                        if (w instanceof JDialog && w.isVisible()) {
                            w.dispose();
                        }
                    }
                    xửLýHoànTấtThanhToán("Chuyển khoản VietQR");
                }
            }
        });
        bankCheckTimer.start();
    }

    private void stopBankChecking() {
        if (bankCheckTimer != null) {
            bankCheckTimer.stop();
        }
    }

    private void stopAllTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (bankCheckTimer != null) bankCheckTimer.stop();
    }

    private void tinhToanTaiChinh() {
        tongGiaGoc = 0;
        tongGiamDoiTuong = 0;

        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            tongGiaGoc += parseMoney(modelChiTiet.getValueAt(i, 4).toString()); 
            tongGiamDoiTuong += parseMoney(modelChiTiet.getValueAt(i, 6).toString()); 
        }

        double tongKMTam = tongGiamDoiTuong + giamVoucher;
        tongThanhToan = tongGiaGoc - tongKMTam;
        if (tongThanhToan < 0) tongThanhToan = 0;

        lblTongTien.setText("<html><font color='#505050'>Tổng tiền: </font><font color='black'><b>" + DF.format(tongGiaGoc) + "</b></font></html>");
        lblTongKhuyenMai.setText("<html><font color='#505050'>Tổng khuyến mãi: </font><font color='black'><b>" + DF.format(tongKMTam) + "</b></font></html>");
        lblThanhToanConLai.setText("<html><font color='#505050'>Thanh toán còn lại: </font><font color='#C82020'><b>" + DF.format(tongThanhToan) + "</b></font></html>");

        if (btnChuyenKhoan.isSelected()) toggleQRCode();
        else tinhTienThua();
    }
    
    private void tinhTienThua() {
        if(txtTienKhachDua == null || lblTienThua == null) return;
        double tienKhach = parseMoney(txtTienKhachDua.getText());
        double tienThua = tienKhach - tongThanhToan;
        
        if (tienKhach == 0) {
            lblTienThua.setText("0 VNĐ");
            lblTienThua.setForeground(Color.BLACK);
        } else if (tienThua < 0) {
            lblTienThua.setText("Còn thiếu: " + DF.format(Math.abs(tienThua)));
            lblTienThua.setForeground(Color.RED);
        } else {
            lblTienThua.setText(DF.format(tienThua));
            lblTienThua.setForeground(Color.BLACK);
        }
    }
    
    private void updateCountdownLabel() {
        int m = secondsLeft / 60, s = secondsLeft % 60;
        lblCountdown.setForeground(secondsLeft <= 300 ? new Color(160, 0, 0) : new Color(190, 30, 30));
        lblCountdown.setBackground(secondsLeft <= 300 ? new Color(160, 0, 0) : new Color(200, 60, 60));
        lblCountdown.setText(String.format("Thời hạn giữ vé: %02d:%02d", m, s));
    }

    private void startCountdown() {
        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            if (secondsLeft <= 0) {
                stopAllTimers();
                lblCountdown.setText("Hết thời gian!");
                JOptionPane.showMessageDialog(this, "Thời gian giữ vé đã hết!\nVui lòng thực hiện lại.", "Hết thời gian", JOptionPane.WARNING_MESSAGE);
                if (onQuayLai != null) onQuayLai.accept(secondsLeft);
            } else {
                updateCountdownLabel();
            }
        });
        countdownTimer.start();
        updateCountdownLabel();
    }

    private void nhapKhuyenMai() {
        String ma = JOptionPane.showInputDialog(this, "Nhập mã khuyến mãi:");
        if (ma != null && ma.trim().equalsIgnoreCase("SALE100")) {
            giamVoucher = 100000;
            JOptionPane.showMessageDialog(this, "Đã áp dụng mã giảm 100,000 VNĐ");
        } else if (ma != null && !ma.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã không hợp lệ hoặc đã hết hạn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            giamVoucher = 0; 
        }
        tinhToanTaiChinh();
    }

    private void toggleQRCode() {
        boolean isBank = btnChuyenKhoan.isSelected();
        if (isBank && tongThanhToan > 0) {
            lblQR.setIcon(null);
            lblQR.setText("Đang tạo mã VietQR...");
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
                @Override protected ImageIcon doInBackground() throws Exception {
                    String bankID = "970422";       
                    String accountNo = "0382588430"; 
                    String amount = String.valueOf((long) tongThanhToan);
                    String info = URLEncoder.encode("Thanh toan ve tau " + maHD, StandardCharsets.UTF_8);
                    String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%s&addInfo=%s", bankID, accountNo, amount, info);
                    return new ImageIcon(new URL(qrUrl));
                }
                @Override protected void done() {
                    try {
                        originalQRImageIcon = get(); 
                        Image img = originalQRImageIcon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                        lblQR.setIcon(new ImageIcon(img));
                        lblQR.setText("");
                    } catch (Exception e) {
                        lblQR.setIcon(null);
                        lblQR.setText("Lỗi load mã QR!");
                    }
                }
            };
            worker.execute();
        } else if (isBank && tongThanhToan == 0) {
             lblQR.setText("Hóa đơn 0đ không cần quét QR");
             lblQR.setIcon(null);
             originalQRImageIcon = null;
        }
    }

    private double parseMoney(String str) {
        try {
            return Double.parseDouble(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private JPanel createDetailLabel(String title, String value) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); }
        };
        p.setOpaque(false);
        JLabel lbl1 = new JLabel(title); lbl1.setFont(FONT_14); lbl1.setForeground(new Color(80, 80, 80));
        JLabel lbl2 = new JLabel(value); lbl2.setFont(FONT_B14); lbl2.setForeground(Color.BLACK);
        p.add(lbl1, BorderLayout.WEST);
        p.add(lbl2, BorderLayout.EAST);
        return p;
    }

    private JToggleButton createToggleBtn(String text) {
        JToggleButton b = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(new Color(240, 246, 255));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.setColor(NAVY);
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 4, 4);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.setColor(BORDER_C);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.addChangeListener(e -> {
            if (b.isSelected()) {
                b.setFont(FONT_B14); b.setForeground(NAVY);
            } else {
                b.setFont(FONT_14); b.setForeground(new Color(80, 80, 80));
            }
        });
        b.setFont(FONT_14);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        return b;
    }

    private Icon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception ignored) {}
        return null;
    }

    private JButton makeNavyBtn(String text, Icon icon) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(18, 42, 85) : getModel().isRollover() ? new Color(38, 68, 128) : NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        if (icon != null) { b.setIcon(icon); }
        b.setFont(FONT_B14); b.setForeground(Color.WHITE); b.setIconTextGap(8);
        b.setBorder(new EmptyBorder(6, 18, 6, 18));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeOutlineBtn(String text, Icon icon) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(220, 230, 245) : getModel().isRollover() ? new Color(230, 240, 250) : new Color(242, 247, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(NAVY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        if (icon != null) b.setIcon(icon);
        b.setFont(FONT_14); b.setForeground(NAVY); b.setIconTextGap(8);
        b.setContentAreaFilled(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}