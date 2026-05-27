package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.swing.border.EmptyBorder;
import connect_DB.Connect_DB;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileOutputStream;

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
                     "hd.maHoaDon, hd.ngayLapHD, hd.tongTien, hd.phuongThucThanhToan, v.maChuyenTau " +
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
        String viTri = tenToa + " - Ghế " + maGhe + " (" + normalizeLoaiGhe(loaiGhe) + ")";
        
        final String maHD = rs.getString("maHoaDon");
        Timestamp ngayLapHD = rs.getTimestamp("ngayLapHD");
        String strNgayLap = (ngayLapHD != null) ? SDF_L.format(ngayLapHD) : "";
        String hinhThucThanhToan = rs.getString("phuongThucThanhToan");
        if ("LUU_TAM".equals(hinhThucThanhToan)) hinhThucThanhToan = "Lưu tạm chờ thanh toán";
        else if ("TIEN_MAT".equals(hinhThucThanhToan)) hinhThucThanhToan = "Tiền mặt";
        else hinhThucThanhToan = "Chuyển khoản";
        
        double giaVe = rs.getDouble("giaVe");
        if (giaVe < 1000) {
            giaVe *= 1000;
        }
        
        boolean isChoThanhToan = "Chờ thanh toán".equals(trangThaiVe);
        boolean isHopLe = !"Đã hủy".equals(trangThaiVe) && !"DA_HUY".equals(trangThaiVe);
        
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
        btnInVe.setEnabled("Đã thanh toán".equals(trangThaiVe));
        JButton btnThanhToan = createStyledButton("Thanh toán", new Color(40, 160, 60), 120);
        btnThanhToan.setEnabled(isChoThanhToan);
        JButton btnDong = createStyledButton("Đóng", new Color(90, 95, 100), 90);
        
        btnDong.addActionListener(e -> dispose());
        btnInVe.addActionListener(e -> inVeLai());
        
        btnThanhToan.addActionListener(e -> {
            dispose();
            
            DefaultTableModel modelForThanhToan = new DefaultTableModel(
                new Object[]{"STT", "Mã vé", "Loại vé (Chiều)", "Chiều", "Mã Ghế", "Tên KH", "CCCD", "SĐT", "Loại KH", "Mã Toa", "Mã Tàu", "Tên Tàu", "Mã Chuyến Tàu", "Mã Ga Đến"}, 0
            );
            
            String sqlAll = "SELECT v.maVe, v.loaiVe, v.maGhe, kh.hoTenKH, kh.cccd, kh.sdt, " +
                            "toa.maToaTau, t.tenTau, v.maChuyenTau, dt.maGaDen, hd.ngayLapHD " +
                            "FROM Ve v " +
                            "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                            "LEFT JOIN KhachHang kh ON COALESCE(v.maKH, hd.maKH) = kh.maKH " +
                            "JOIN Ghe g ON v.maGhe = g.maGhe " +
                            "JOIN ToaTau toa ON g.maToaTau = toa.maToaTau " +
                            "JOIN Tau t ON toa.maTau = t.maTau " +
                            "JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                            "WHERE v.maHoaDon = ?";
            
            try (Connection con = Connect_DB.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlAll)) {
                ps.setString(1, maHD);
                ResultSet rsAll = ps.executeQuery();
                
                int stt = 1;
                Timestamp curNgayLapHD = null;
                while (rsAll.next()) {
                    if (curNgayLapHD == null) {
                        curNgayLapHD = rsAll.getTimestamp("ngayLapHD");
                    }
                    
                    String curMaVe = rsAll.getString("maVe");
                    String loaiVeRaw = rsAll.getString("loaiVe");
                    String chieu = "MOT_CHIEU".equals(loaiVeRaw) ? "Chiều đi" : "Chiều về";
                    String curMaGhe = rsAll.getString("maGhe");
                    String curHoTenKH = rsAll.getString("hoTenKH") != null ? rsAll.getString("hoTenKH") : "N/A";
                    String curCccd = rsAll.getString("cccd") != null ? rsAll.getString("cccd") : "";
                    String curSdt = rsAll.getString("sdt") != null ? rsAll.getString("sdt") : "";
                    String maToa = rsAll.getString("maToaTau");
                    String curTenTau = rsAll.getString("tenTau");
                    String maChuyen = rsAll.getString("maChuyenTau");
                    String maGaDen = rsAll.getString("maGaDen");
                    
                    modelForThanhToan.addRow(new Object[]{
                        stt++, curMaVe, loaiVeRaw, chieu, curMaGhe, curHoTenKH, curCccd, curSdt, "Người lớn", maToa, "", curTenTau, maChuyen, maGaDen
                    });
                }
                
                if (stt == 1 || curNgayLapHD == null) {
                    JOptionPane.showMessageDialog(mainFrame, "Không tìm thấy danh sách vé thuộc hóa đơn này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                long diff = new java.util.Date().getTime() - curNgayLapHD.getTime();
                int secondsLeft = (int) (30 * 60 - diff / 1000);
                if (secondsLeft < 0) secondsLeft = 0;
                
                DatVeGUI3 panel3 = new DatVeGUI3(modelForThanhToan, secondsLeft, (sec) -> {
                    mainFrame.showCard("tra-cuu-ve");
                });
                panel3.setMaHD(maHD);
                
                mainFrame.showTemporaryCard(panel3, "thanh-toan-lai");
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Lỗi khi nạp dữ liệu thanh toán: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
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

    private void inVeLai() {
        String sql = "SELECT v.maVe, v.loaiVe, kh.hoTenKH, kh.cccd, " +
                     "g.soGhe, g.loaiGhe, toa.soToa, t.tenTau, " +
                     "gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
                     "dt.thoiGianKhoiHanh " +
                     "FROM Ve v " +
                     "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                     "LEFT JOIN KhachHang kh ON COALESCE(v.maKH, hd.maKH) = kh.maKH " +
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
                String maVeStr = rs.getString("maVe");
                String loaiVeRaw = rs.getString("loaiVe");
                String loaiVe = "MOT_CHIEU".equals(loaiVeRaw) ? "Một chiều" : "Khứ hồi";
                String soGhe = rs.getString("soGhe");
                String rawGhe = rs.getString("loaiGhe");
                String loaiCho = "Ghế cứng";
                if (rawGhe != null) {
                    loaiCho = switch (rawGhe.trim()) {
                        case "GHE_CUNG" -> "Ghế cứng";
                        case "GHE_MEM" -> "Ghế mềm";
                        case "GIUONG_NAM" -> "Giường nằm";
                        default -> rawGhe;
                    };
                }
                String hangVe = loaiCho.equals("Giường nằm") ? "VIP" : "Thường";
                String tenKH = rs.getString("hoTenKH") != null ? rs.getString("hoTenKH") : "N/A";
                String cccdRaw = rs.getString("cccd") != null ? rs.getString("cccd") : "";
                String cccd = cheCCCD(cccdRaw);
                String tenTau = rs.getString("tenTau");
                String gaDi = rs.getString("gaDi");
                String gaDen = rs.getString("gaDen");
                String maToaTau = String.valueOf(rs.getInt("soToa"));
                
                String ngayDi = "";
                String gioDi = "";
                Timestamp ts = rs.getTimestamp("thoiGianKhoiHanh");
                if (ts != null) {
                    ngayDi = new SimpleDateFormat("dd/MM/yyyy").format(ts);
                    gioDi = new SimpleDateFormat("HH:mm").format(ts);
                }

                File folder = new File("Ve");
                if (!folder.exists())
                    folder.mkdir();

                com.itextpdf.text.pdf.BaseFont bf = com.itextpdf.text.pdf.BaseFont.createFont("c:/windows/fonts/arial.ttf", com.itextpdf.text.pdf.BaseFont.IDENTITY_H, com.itextpdf.text.pdf.BaseFont.EMBEDDED);
                com.itextpdf.text.Font fCongTy = new com.itextpdf.text.Font(bf, 10, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Font fGaTen = new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Font fTieuDe = new com.itextpdf.text.Font(bf, 12, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(bf, 8, com.itextpdf.text.Font.NORMAL);
                com.itextpdf.text.Font fGaLabel = new com.itextpdf.text.Font(bf, 8, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.GRAY);
                com.itextpdf.text.Font fGaValue = new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Font fLabel = new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.NORMAL);
                com.itextpdf.text.Font fValue = new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Font fMaVe = new com.itextpdf.text.Font(bf, 8, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.GRAY);

                com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(240, 580);
                File pdfFile = new File(folder, "Ve_" + maVeStr + ".pdf");
                com.itextpdf.text.Document doc = new com.itextpdf.text.Document(pageSize, 10, 10, 12, 12);
                com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));
                doc.open();

                // ── WRAPPER ────────────────────────────────────────────────────
                com.itextpdf.text.pdf.PdfPTable wrap = new com.itextpdf.text.pdf.PdfPTable(1);
                wrap.setWidthPercentage(100);
                com.itextpdf.text.pdf.PdfPCell wc = new com.itextpdf.text.pdf.PdfPCell();
                wc.setBorder(com.itextpdf.text.Rectangle.BOX);
                wc.setBorderWidth(0.8f);
                wc.setPaddingLeft(10);
                wc.setPaddingRight(10);
                wc.setPaddingTop(10);
                wc.setPaddingBottom(10);

                // ── 1. HEADER ──────────────────────────────────────────────────
                com.itextpdf.text.Paragraph pCty = new com.itextpdf.text.Paragraph("TỔNG CÔNG TY ĐƯỜNG SẮT VIỆT NAM", fCongTy);
                pCty.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                wc.addElement(pCty);

                com.itextpdf.text.Paragraph pGaTen = new com.itextpdf.text.Paragraph("GA DIÊU TRÌ", fGaTen);
                pGaTen.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                wc.addElement(pGaTen);

                // Đường kẻ ngang mỏng
                com.itextpdf.text.pdf.PdfPTable lineTable = new com.itextpdf.text.pdf.PdfPTable(1);
                lineTable.setWidthPercentage(100);
                lineTable.setSpacingBefore(4);
                lineTable.setSpacingAfter(4);
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

                // ── 2. BARCODE ─────────────────────────────────────────────────
                com.itextpdf.text.pdf.Barcode128 barcode = new com.itextpdf.text.pdf.Barcode128();
                barcode.setCode(maVeStr);
                barcode.setBarHeight(32f);
                barcode.setX(1.0f);
                barcode.setBaseline(0f);
                barcode.setAltText("");
                java.awt.Image awtImg = barcode.createAwtImage(java.awt.Color.BLACK, java.awt.Color.WHITE);
                com.itextpdf.text.Image imgBar = com.itextpdf.text.Image.getInstance(awtImg, null);
                imgBar.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                imgBar.scaleToFit(240f, 38f);
                wc.addElement(imgBar);

                // Mã vé dưới barcode
                com.itextpdf.text.Paragraph pMaVe = new com.itextpdf.text.Paragraph("Mã vé/TicketID: " + maVeStr, fMaVe);
                pMaVe.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                pMaVe.setSpacingAfter(6);
                wc.addElement(pMaVe);

                // ── 3. GA ĐI / GA ĐẾN ─────────────────────────────────────────
                com.itextpdf.text.pdf.PdfPTable gaTable = new com.itextpdf.text.pdf.PdfPTable(2);
                gaTable.setWidthPercentage(100);
                gaTable.setSpacingBefore(2);
                gaTable.setSpacingAfter(2);

                // Ga đi — căn trái, padding trái
                com.itextpdf.text.pdf.PdfPCell cGaDi = new com.itextpdf.text.pdf.PdfPCell();
                cGaDi.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cGaDi.setPaddingLeft(16);
                cGaDi.setPaddingBottom(2);
                com.itextpdf.text.Paragraph pDiLbl = new com.itextpdf.text.Paragraph("Ga đi", fGaLabel);
                pDiLbl.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                cGaDi.addElement(pDiLbl);
                com.itextpdf.text.Paragraph pDiVal = new com.itextpdf.text.Paragraph(gaDi.toUpperCase(), fGaValue);
                pDiVal.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                cGaDi.addElement(pDiVal);
                gaTable.addCell(cGaDi);

                // Ga đến — căn phải, padding phải
                com.itextpdf.text.pdf.PdfPCell cGaDen = new com.itextpdf.text.pdf.PdfPCell();
                cGaDen.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                cGaDen.setPaddingRight(16);
                cGaDen.setPaddingBottom(2);
                com.itextpdf.text.Paragraph pDenLbl = new com.itextpdf.text.Paragraph("Ga đến", fGaLabel);
                pDenLbl.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                cGaDen.addElement(pDenLbl);
                com.itextpdf.text.Paragraph pDenVal = new com.itextpdf.text.Paragraph(gaDen.toUpperCase(), fGaValue);
                pDenVal.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                cGaDen.addElement(pDenVal);
                gaTable.addCell(cGaDen);

                // Đường kẻ dưới ga đi/đến
                com.itextpdf.text.pdf.PdfPCell cSep = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(""));
                cSep.setColspan(2);
                cSep.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
                cSep.setBorderWidth(0.5f);
                cSep.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                cSep.setPaddingBottom(3);
                gaTable.addCell(cSep);

                wc.addElement(gaTable);

                // ── 4. THÔNG TIN CHI TIẾT ──────────────────────────────────────
                com.itextpdf.text.pdf.PdfPTable infoTable = new com.itextpdf.text.pdf.PdfPTable(2);
                infoTable.setWidthPercentage(100);
                infoTable.setWidths(new float[] { 1.35f, 1.65f });
                infoTable.setSpacingBefore(3);

                String[][] details = {
                    { "Số hiệu tàu/Train ID:", tenTau },
                    { "Ngày khởi hành/Date:", ngayDi },
                    { "Giờ khởi hành/Time:", gioDi },
                    { "Số Toa/Coach:", maToaTau },
                    { "Loại Toa/Type:", loaiCho },
                    { "Số ghế/Seat:", soGhe },
                    { "Loại vé/Ticket:", loaiVe },
                    { "Hạng vé/Class:", hangVe },
                    { "Họ Tên/Name:", tenKH.toUpperCase() },
                    { "Giấy tờ/Passport:", cccd },
                };

                for (String[] d : details) {
                    com.itextpdf.text.pdf.PdfPCell cL = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(d[0], fLabel));
                    cL.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                    cL.setPaddingLeft(14);
                    cL.setPaddingBottom(3);
                    infoTable.addCell(cL);

                    com.itextpdf.text.pdf.PdfPCell cR = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(d[1], fValue));
                    cR.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                    cR.setPaddingLeft(30);
                    cR.setPaddingBottom(3);
                    infoTable.addCell(cR);
                }
                wc.addElement(infoTable);

                wrap.addCell(wc);
                doc.add(wrap);
                doc.close();

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu vé để in!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi in lại vé: " + e.getMessage());
        }
    }

    private String cheCCCD(String cccd) {
        if (cccd != null && cccd.length() > 4) {
            int length = cccd.length();
            if (length == 12) {
                return cccd.substring(0, 4) + "****" + cccd.substring(8);
            } else if (length > 6) {
                int visible = (length - 4) / 2;
                return cccd.substring(0, visible) + "****" + cccd.substring(visible + 4);
            }
        }
        return cccd;
    }

    private String normalizeLoaiGhe(String loaiGhe) {
        if (loaiGhe == null) return "";
        String s = loaiGhe.toUpperCase().trim();
        if (s.contains("CUNG")) return "Ghế cứng";
        if (s.contains("MEM")) return "Ghế mềm";
        if (s.contains("NAM") || s.contains("GIUONG")) return "Giường nằm";
        return loaiGhe;
    }
}
