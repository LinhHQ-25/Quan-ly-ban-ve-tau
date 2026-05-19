package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.swing.border.EmptyBorder;
import connect_DB.Connect_DB;
import javax.swing.table.DefaultTableModel;

public class ChiTietVeDialog extends JDialog {
    private String maVe;
    private AppFrame mainFrame;
    private static final DecimalFormat DF = new DecimalFormat("#,### VNĐ");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat SDF_L = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public ChiTietVeDialog(AppFrame parent, String maVe) {
        super(parent, "Chi tiết vé", true);
        this.maVe = maVe;
        this.mainFrame = parent;
        
        setSize(850, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);
        
        loadDataAndBuildUI();
    }
    
    private void loadDataAndBuildUI() {
        String sql = "SELECT v.maVe, v.trangThaiVe, v.loaiVe, v.ngayMua, v.giaVe, " +
                     "kh.hoTenKH, kh.sdt, " +
                     "dt.thoiGianKhoiHanh, t.tenTau, " +
                     "gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
                     "g.maGhe, g.loaiGhe, toa.maToaTau, " +
                     "hd.ngayLapHD, hd.tongTien, hd.phuongThucThanhToan, v.maChuyenTau " +
                     "FROM Ve v " +
                     "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                     "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                     "JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                     "JOIN Ga gDi ON dt.maGaDi = gDi.maGa " +
                     "JOIN Ga gDen ON dt.maGaDen = gDen.maGa " +
                     "JOIN Ghe g ON v.maGhe = g.maGhe " +
                     "JOIN ToaTau toa ON g.maToaTau = toa.maToaTau " +
                     "JOIN Tau t ON toa.maTau = t.maTau " +
                     "WHERE v.maVe = ?";
                     
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                buildUI(rs);
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin vé!");
                dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void buildUI(ResultSet rs) throws Exception {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(new Color(180, 205, 230), 2));
        
        String trangThaiVe = rs.getString("trangThaiVe");
        final String hoTenKH = rs.getString("hoTenKH") != null ? rs.getString("hoTenKH") : "N/A";
        final String soDT = rs.getString("sdt") != null ? rs.getString("sdt") : "N/A";
        
        String tenTau = rs.getString("tenTau");
        String gaDi = rs.getString("gaDi");
        String gaDen = rs.getString("gaDen");
        String tuyen = gaDi + " -> " + gaDen;
        Timestamp thoiGianKhoiHanh = rs.getTimestamp("thoiGianKhoiHanh");
        String strKhoiHanh = (thoiGianKhoiHanh != null) ? SDF.format(thoiGianKhoiHanh) : "";
        String maGhe = rs.getString("maGhe");
        String loaiGhe = rs.getString("loaiGhe");
        String tenToa = rs.getString("maToaTau");
        String viTri = tenToa + " - Ghế " + maGhe + " (" + loaiGhe + ")";
        
        Timestamp ngayLapHD = rs.getTimestamp("ngayLapHD");
        String strNgayLap = (ngayLapHD != null) ? SDF_L.format(ngayLapHD) : "";
        String hinhThucThanhToan = rs.getString("phuongThucThanhToan");
        if ("LUU_TAM".equals(hinhThucThanhToan)) hinhThucThanhToan = "Lưu tạm chờ thanh toán";
        else if ("TIEN_MAT".equals(hinhThucThanhToan)) hinhThucThanhToan = "Tiền mặt";
        else hinhThucThanhToan = "Chuyển khoản";
        
        double giaVe = rs.getDouble("giaVe");
        
        boolean isChoThanhToan = "Chờ thanh toán".equals(trangThaiVe);
        boolean isHopLe = !"Đã hủy".equals(trangThaiVe);
        
        // Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlHeader.setBackground(Color.WHITE);
        JLabel lblBadge = new JLabel(" " + trangThaiVe + " ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(6, 12, 6, 12)
        ));
        lblBadge.setOpaque(true);
        if ("Đã thanh toán".equals(trangThaiVe)) {
            lblBadge.setBackground(new Color(235, 248, 235));
            lblBadge.setForeground(new Color(40, 140, 60));
        } else if (isChoThanhToan) {
            lblBadge.setBackground(new Color(255, 246, 230));
            lblBadge.setForeground(new Color(230, 110, 0));
        } else {
            lblBadge.setBackground(new Color(250, 230, 230));
            lblBadge.setForeground(new Color(200, 40, 40));
        }
        pnlHeader.add(lblBadge);
        
        // Body (Grid)
        JPanel pnlBody = new JPanel(new GridLayout(1, 2, 25, 0));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 25, 20, 25));
        
        JPanel pnlKhach = new JPanel();
        pnlKhach.setLayout(new BoxLayout(pnlKhach, BoxLayout.Y_AXIS));
        pnlKhach.setBackground(Color.WHITE);
        JLabel lblT1 = new JLabel("THÔNG TIN KHÁCH HÀNG");
        lblT1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblT1.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlKhach.add(lblT1);
        pnlKhach.add(Box.createVerticalStrut(15));
        pnlKhach.add(createInfoRow("Họ và tên:", hoTenKH));
        pnlKhach.add(Box.createVerticalStrut(10));
        pnlKhach.add(createInfoRow("Số điện thoại:", soDT));
        pnlKhach.add(Box.createVerticalStrut(10));
        pnlKhach.add(createInfoRow("Đối tượng:", "Người lớn"));
        pnlKhach.add(Box.createVerticalStrut(10));
        pnlKhach.add(createInfoRow("Tình trạng:", isHopLe ? "Hợp lệ" : "Không hợp lệ"));
        
        JPanel pnlChuyen = new JPanel();
        pnlChuyen.setLayout(new BoxLayout(pnlChuyen, BoxLayout.Y_AXIS));
        pnlChuyen.setBackground(Color.WHITE);
        JLabel lblT2 = new JLabel("THÔNG TIN CHUYẾN ĐI");
        lblT2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblT2.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlChuyen.add(lblT2);
        pnlChuyen.add(Box.createVerticalStrut(15));
        pnlChuyen.add(createInfoRow("Tàu:", tenTau));
        pnlChuyen.add(Box.createVerticalStrut(10));
        pnlChuyen.add(createInfoRow("Lộ trình:", tuyen));
        pnlChuyen.add(Box.createVerticalStrut(10));
        pnlChuyen.add(createInfoRow("Khởi hành:", strKhoiHanh));
        pnlChuyen.add(Box.createVerticalStrut(10));
        pnlChuyen.add(createInfoRow("Vị trí:", viTri));
        
        pnlBody.add(pnlKhach);
        pnlBody.add(pnlChuyen);
        
        // Giao Dịch
        JPanel pnlGiaoDich = new JPanel();
        pnlGiaoDich.setLayout(new BoxLayout(pnlGiaoDich, BoxLayout.Y_AXIS));
        pnlGiaoDich.setBackground(Color.WHITE);
        pnlGiaoDich.setBorder(new EmptyBorder(0, 25, 20, 25));
        
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlGiaoDich.add(sep);
        pnlGiaoDich.add(Box.createVerticalStrut(20));
        
        JLabel lblT3 = new JLabel("THÔNG TIN GIAO DỊCH");
        lblT3.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblT3.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlGiaoDich.add(lblT3);
        pnlGiaoDich.add(Box.createVerticalStrut(15));
        pnlGiaoDich.add(createInfoRow("Thời gian lập vé:", strNgayLap));
        pnlGiaoDich.add(Box.createVerticalStrut(10));
        pnlGiaoDich.add(createInfoRow("Kênh bán:", "Tại quầy"));
        pnlGiaoDich.add(Box.createVerticalStrut(10));
        pnlGiaoDich.add(createInfoRow("Hình thức thanh toán:", hinhThucThanhToan));
        pnlGiaoDich.add(Box.createVerticalStrut(10));
        
        JPanel pnlTien = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTien.setBackground(Color.WHITE);
        pnlTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblTongTien = new JLabel("Tổng tiền: ");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTongTien.setForeground(new Color(60, 60, 60));
        JLabel lblGia = new JLabel(DF.format(giaVe));
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pnlTien.add(lblTongTien);
        pnlTien.add(lblGia);
        pnlGiaoDich.add(pnlTien);
        
        // Footer (Buttons)
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(new Color(245, 248, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 230, 240)));
        
        JButton btnInVe = createStyledButton("In lại vé", new Color(40, 110, 200), 100);
        JButton btnThanhToan = createStyledButton("Thanh toán", new Color(40, 160, 60), 120);
        btnThanhToan.setEnabled(isChoThanhToan);
        JButton btnDong = createStyledButton("Đóng", new Color(90, 95, 100), 90);
        
        btnDong.addActionListener(e -> dispose());
        btnInVe.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đang in vé..."));
        
        String loaiVe = rs.getString("loaiVe");
        String chieuVe = "MOT_CHIEU".equals(loaiVe) ? "Chiều đi" : "Chiều về"; 
        String maChuyenTau = rs.getString("maChuyenTau");
        String maToaTau = rs.getString("maToaTau");
        
        btnThanhToan.addActionListener(e -> {
            dispose();
            
            DefaultTableModel modelForThanhToan = new DefaultTableModel(
                new Object[]{"STT", "Mã vé", "Loại vé (Chiều)", "Chiều", "Mã Ghế", "Tên KH", "CCCD", "SĐT", "Loại KH", "Mã Toa", "Mã Tàu", "Tên Tàu", "Mã Chuyến Tàu"}, 0
            );
            modelForThanhToan.addRow(new Object[]{
                1, maVe, loaiVe, chieuVe, maGhe, hoTenKH, "", soDT, "Người lớn", maToaTau, "", tenTau, maChuyenTau
            });
            
            long diff = new java.util.Date().getTime() - ngayLapHD.getTime();
            int secondsLeft = (int) (30 * 60 - diff / 1000);
            if (secondsLeft < 0) secondsLeft = 0;
            
            DatVeGUI3 panel3 = new DatVeGUI3(modelForThanhToan, secondsLeft, (sec) -> {
                mainFrame.showCard("tra-cuu-ve");
            });
            
            mainFrame.showTemporaryCard(panel3, "thanh-toan-lai");
        });
        
        pnlFooter.add(btnInVe);
        pnlFooter.add(btnThanhToan);
        pnlFooter.add(btnDong);
        
        pnlBody.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlGiaoDich.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel pnlCenter = new JPanel();
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.add(pnlBody);
        pnlCenter.add(pnlGiaoDich);
        
        root.add(pnlHeader, BorderLayout.NORTH);
        root.add(pnlCenter, BorderLayout.CENTER);
        root.add(pnlFooter, BorderLayout.SOUTH);
        
        setContentPane(root);
    }
    
    private JPanel createInfoRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lbl1 = new JLabel(label + "  ");
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl1.setForeground(new Color(60, 60, 60));
        
        JLabel lbl2 = new JLabel(value);
        lbl2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        if (label.contains("Tình trạng")) {
            if ("Hợp lệ".equals(value)) {
                lbl2.setForeground(new Color(40, 150, 40));
            } else {
                lbl2.setForeground(Color.RED);
            }
            lbl2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }
        
        p.add(lbl1, BorderLayout.WEST);
        p.add(lbl2, BorderLayout.CENTER);
        return p;
    }
    
    private JButton createStyledButton(String text, Color bgColor, int width) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(200, 200, 200));
                } else if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(width, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
