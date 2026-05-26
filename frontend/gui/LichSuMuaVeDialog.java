package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import connect_DB.Connect_DB;

public class LichSuMuaVeDialog extends JDialog {
    private String maKH;
    private String maHoaDon;
    private boolean isHoaDon = false;
    private static final DecimalFormat DF = new DecimalFormat("#,### VNĐ");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    private DefaultTableModel tblModel;
    private JTable tblData;
    private JLabel lblSummary;

    public LichSuMuaVeDialog(Frame parent, String id, boolean isHoaDon) {
        super(parent, isHoaDon ? "Chi tiết hóa đơn" : "Lịch sử mua vé", true);
        this.isHoaDon = isHoaDon;
        if (isHoaDon) {
            this.maHoaDon = id;
        } else {
            this.maKH = id;
        }
        
        setSize(800, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);
        
        buildUI();
        loadHistoryData();
    }

    public LichSuMuaVeDialog(Frame parent, String maKH) {
        this(parent, maKH, false);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(new Color(180, 205, 230), 2));
        
        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(245, 248, 252));
        pnlHeader.setBorder(new EmptyBorder(12, 20, 12, 20));
        
        JLabel lblTitle = new JLabel(isHoaDon ? "Chi tiết hóa đơn" : "Lịch sử mua vé");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(40, 60, 90));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        root.add(pnlHeader, BorderLayout.NORTH);
        
        // Center Table
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        String[] cols = {
            "STT", 
            "Mã Vé", 
            "<html><center>Chuyến đi<br/>(Ga đi &rarr; Ga đến)</center></html>", 
            "Thời gian", 
            "Toa/Ghế", 
            "Giá vé", 
            "Trạng thái"
        };
        
        tblModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        
        tblData = new JTable(tblModel);
        tblData.setRowHeight(36);
        tblData.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblData.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblData.setShowVerticalLines(false);
        tblData.setSelectionBackground(new Color(232, 240, 254));
        tblData.setSelectionForeground(Color.BLACK);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, row, col);
                if (!s) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 252, 255));
                    c.setForeground(Color.BLACK);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // Color status column
                if (col == 6) {
                    String status = (v != null) ? v.toString() : "";
                    if ("Đã thanh toán".equals(status)) {
                        c.setForeground(new Color(40, 140, 60));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else if ("Chờ thanh toán".equals(status)) {
                        c.setForeground(new Color(230, 110, 0));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setForeground(Color.RED);
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    }
                }
                return c;
            }
        };
        
        for (int i = 0; i < tblData.getColumnCount(); i++) {
            tblData.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        ((DefaultTableCellRenderer)tblData.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        JScrollPane spn = new JScrollPane(tblData);
        spn.setBorder(new LineBorder(new Color(220, 230, 240), 1, true));
        spn.getViewport().setBackground(Color.WHITE);
        pnlCenter.add(spn, BorderLayout.CENTER);
        
        // Summary & Buttons
        JPanel pnlBottom = new JPanel(new BorderLayout(0, 15));
        pnlBottom.setBackground(Color.WHITE);
        
        lblSummary = new JLabel("Tổng số chuyến đã đi: 0 | Tổng số tiền đã đóng: 0 VNĐ");
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSummary.setForeground(new Color(60, 60, 60));
        pnlBottom.add(lblSummary, BorderLayout.NORTH);
        
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setBackground(Color.WHITE);
        
        JButton btnDong = createStyledButton("Đóng", new Color(90, 95, 100), 90);
        btnDong.addActionListener(e -> dispose());
        pnlButtons.add(btnDong);
        pnlBottom.add(pnlButtons, BorderLayout.SOUTH);
        
        pnlCenter.add(pnlBottom, BorderLayout.SOUTH);
        
        root.add(pnlCenter, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void loadHistoryData() {
        String sql = "SELECT v.maVe, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, dt.thoiGianKhoiHanh, " +
                     "toa.soToa, g.soGhe, v.giaVe, v.trangThaiVe " +
                     "FROM Ve v " +
                     "JOIN HoaDon hd ON v.maHoaDon = hd.maHoaDon " +
                     "JOIN ChiTietChuyenTau dt ON v.maChuyenTau = dt.maChuyenTau " +
                     "JOIN Ga gDi ON dt.maGaDi = gDi.maGa " +
                     "JOIN Ga gDen ON dt.maGaDen = gDen.maGa " +
                     "JOIN Ghe g ON v.maGhe = g.maGhe " +
                     "JOIN ToaTau toa ON g.maToaTau = toa.maToaTau " +
                     (isHoaDon ? "WHERE hd.maHoaDon = ? " : "WHERE hd.maKH = ? ") +
                     "ORDER BY dt.thoiGianKhoiHanh DESC";
                     
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isHoaDon ? maHoaDon : maKH);
            ResultSet rs = ps.executeQuery();
            
            int stt = 1;
            int totalTrips = 0;
            double totalPaid = 0;
            
            while (rs.next()) {
                String maVe = rs.getString("maVe");
                String gaDi = rs.getString("gaDi");
                String gaDen = rs.getString("gaDen");
                java.sql.Timestamp thoiGian = rs.getTimestamp("thoiGianKhoiHanh");
                int soToa = rs.getInt("soToa");
                String soGhe = rs.getString("soGhe");
                double giaVe = rs.getDouble("giaVe");
                String trangThai = rs.getString("trangThaiVe");
                
                String chuyenDi = gaDi + " -> " + gaDen;
                String thoiGianStr = thoiGian != null ? SDF.format(thoiGian) : "";
                String toaGhe = "Toa " + soToa + " - Ghế " + soGhe;
                
                tblModel.addRow(new Object[]{
                    stt++,
                    maVe,
                    chuyenDi,
                    thoiGianStr,
                    toaGhe,
                    DF.format(giaVe),
                    trangThai
                });
                
                if ("Đã thanh toán".equals(trangThai)) {
                    totalTrips++;
                    totalPaid += giaVe;
                }
            }
            
            if (isHoaDon) {
                lblSummary.setText("Tổng số vé: " + (stt - 1) + " | Tổng tiền thanh toán: " + DF.format(totalPaid));
            } else {
                lblSummary.setText("Tổng số chuyến đã đi: " + totalTrips + " | Tổng số tiền đã đóng: " + DF.format(totalPaid));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử mua vé!");
        }
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
