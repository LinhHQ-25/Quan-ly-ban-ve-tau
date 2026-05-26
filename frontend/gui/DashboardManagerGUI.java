package gui;

import connect_DB.Connect_DB;
import dao.VeDAO;
import dao.ChuyenTauDAO;
import java.awt.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class DashboardManagerGUI extends JPanel {

    private static final Color NAVY = new Color(37, 69, 121);
    private static final Color LIGHT_BG = new Color(245, 247, 251);
    private static final Color BORDER_C = new Color(210, 215, 224);

    private JLabel lblDoanhThuCmp, lblVeBanCmp, lblVeHuyCmp;
    private JLabel lblDoanhThu, lblVeBan, lblVeHuy;
    private RealtimeOccupancyChart barChart;
    private RealtimeDonutChart donutChart;
    private RealtimeHourlyLineChart lineChart;

    public DashboardManagerGUI() {
        setBackground(LIGHT_BG);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.weightx = 1.0;

        // --- ROW 1: KPI CARDS (Bỏ nhân viên, ép chiều cao fit nội dung) ---
        gbc.gridy = 0; 
        gbc.weighty = 0.0; // Ép khít theo chiều dọc
        add(buildRow1_KPI(), gbc);

        // --- ROW 2: BIỂU ĐỒ TRÒN & CỘT NGANG (Chia đều 50% không gian còn lại) ---
        gbc.gridy = 1; 
        gbc.weighty = 0.5; 
        add(buildRow2_Charts(), gbc);

        // --- ROW 3: BIỂU ĐỒ ĐƯỜNG TUYẾN TÍNH KHUNG GIỜ (Chia đều 50% còn lại) ---
        gbc.gridy = 2; 
        gbc.weighty = 0.5; 
        gbc.insets = new Insets(0, 0, 0, 0); 
        add(buildRow3_LineChart(), gbc);

        loadRealtimeData();
    }

    private JPanel buildRow1_KPI() {
        JPanel row = new JPanel(new GridLayout(1, 3, 15, 0));
        row.setOpaque(false);

        lblDoanhThu = new JLabel("0 đ");
        lblVeBan    = new JLabel("0 vé");
        lblVeHuy    = new JLabel("0 vé");

        lblDoanhThuCmp = new JLabel("--");
        lblVeBanCmp    = new JLabel("--");
        lblVeHuyCmp    = new JLabel("--");

        row.add(createKpiCard("DOANH THU HÔM NAY",      lblDoanhThu, new Color(46, 204, 113),  lblDoanhThuCmp));
        row.add(createKpiCard("VÉ BÁN RA HÔM NAY",      lblVeBan,    new Color(52, 152, 219),  lblVeBanCmp));
        row.add(createKpiCard("VÉ HỦY / TRẢ HÔM NAY",   lblVeHuy,    new Color(231, 76, 60),   lblVeHuyCmp));
        return row;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accent, JLabel cmpLabel) {
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new MatteBorder(0, 5, 0, 0, accent));

        // Phần trên: title + giá trị
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(12, 20, 4, 10)); // padding trên

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(130, 135, 155));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(NAVY);

        topPanel.add(lblTitle);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(valueLabel);

        // Phần dưới: cmp label sát đáy
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 20, 8, 10)); // padding dưới

        cmpLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bottomPanel.add(cmpLabel);

        card.add(topPanel,    BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH); // Sát đáy thật sự
        return card;
    }
    private JPanel buildRow2_Charts() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH; g.weighty = 1.0;

        barChart = new RealtimeOccupancyChart();
        JPanel leftCard = wrapCard(barChart, "TỶ LỆ LẤP ĐẦY TÀU (CÁC CHUYẾN SẮP KHỞI HÀNH)");
        g.gridx = 0; g.weightx = 0.65; g.insets = new Insets(0, 0, 0, 15);
        row.add(leftCard, g);

        donutChart = new RealtimeDonutChart();
        JPanel rightCard = wrapCard(donutChart, "PHÂN LOẠI GHẾ (HÔM NAY)");
        g.gridx = 1; g.weightx = 0.35; g.insets = new Insets(0, 0, 0, 0);
        row.add(rightCard, g);

        return row;
    }

    private JPanel buildRow3_LineChart() {
        lineChart = new RealtimeHourlyLineChart();
        return wrapCard(lineChart, "LƯU LƯỢNG VÉ BÁN THEO KHUNG GIỜ VÀO HÔM NAY");
    }

    private JPanel wrapCard(Component inner, String title) {
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C, 1, true), new EmptyBorder(15, 15, 15, 15)));
        
        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblT.setForeground(NAVY);
        card.add(lblT, BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }
    private void loadRealtimeData() {
        new Thread(() -> {
            try {
                java.time.LocalDate homNay  = java.time.LocalDate.now();
                java.time.LocalDate homQua  = homNay.minusDays(1);

                // ── KPI hôm nay ──
                long doanhThuHN;
                long[] pttt = VeDAO.getDoanhThuPTTTTheoKhoang(homNay, homNay);
                doanhThuHN = pttt[0] + pttt[1]; // tiền mặt + chuyển khoản
                int veBanHN = VeDAO.getSoVeTheoKhoang(homNay, homNay, "Đã thanh toán");
                int veHuyHN = VeDAO.getSoVeHuyTheoKhoang(homNay, homNay);

                final long fDt = doanhThuHN;
                final int  fVb = veBanHN, fVh = veHuyHN;
                SwingUtilities.invokeLater(() -> {
                    lblDoanhThu.setText(String.format("%,d đ", fDt).replace(",", "."));
                    lblVeBan.setText(fVb + " vé");
                    lblVeHuy.setText(fVh + " vé");
                });

                // ── KPI hôm qua để so sánh ──
                long[] ptttHQ = VeDAO.getDoanhThuPTTTTheoKhoang(homQua, homQua);
                long doanhThuHQ = ptttHQ[0] + ptttHQ[1];
                int veBanHQ = VeDAO.getSoVeTheoKhoang(homQua, homQua, "Đã thanh toán");
                int veHuyHQ = VeDAO.getSoVeHuyTheoKhoang(homQua, homQua);

                final long fDtHQ = doanhThuHQ;
                final int  fVbHQ = veBanHQ, fVhHQ = veHuyHQ;
                SwingUtilities.invokeLater(() -> {
                    lblDoanhThuCmp.setText(formatPct(fDt, fDtHQ));
                    lblVeBanCmp.setText(formatInt(fVb, fVbHQ));
                    lblVeHuyCmp.setText(formatIntInvert(fVh, fVhHQ));
                });

                // ── Top 5 chuyến tàu tỷ lệ lấp đầy ──
                List<Object[]> top5 = new ChuyenTauDAO().getTop5ChuyenLapDay();
                List<String>  tauNames = new ArrayList<>();
                List<Integer> tauCap   = new ArrayList<>();
                List<Integer> tauSold  = new ArrayList<>();
                for (Object[] row : top5) {
                    tauNames.add((String)  row[0]);
                    tauCap.add((Integer)   row[1]);
                    tauSold.add((Integer)  row[2]);
                }
                SwingUtilities.invokeLater(() -> barChart.setData(tauNames, tauCap, tauSold));

                // ── Phân loại ghế hôm nay ──
                int[] ghe = VeDAO.getSoGheTheoLoaiTheoKhoang(homNay, homNay);
                // ghe[0]=cứng, ghe[1]=nằm, ghe[2]=mềm
                SwingUtilities.invokeLater(() -> donutChart.setData(ghe[0], ghe[1], ghe[2]));

                // ── Vé bán theo giờ hôm nay ──
                int[] hourly = VeDAO.getVeBanTheoGioHomNay();
                SwingUtilities.invokeLater(() -> lineChart.setData(hourly));

            } catch (SQLException e) { e.printStackTrace(); }
        }).start();
    }
    // ===================================================================================
    // INNER GRAPHICS CLASSES
    // ===================================================================================
    class RealtimeOccupancyChart extends JPanel {
        private List<String> names = new ArrayList<>();
        private List<Integer> caps = new ArrayList<>();
        private List<Integer> solds = new ArrayList<>();

        public RealtimeOccupancyChart() { setOpaque(false); }
        public void setData(List<String> n, List<Integer> c, List<Integer> s) { this.names=n; this.caps=c; this.solds=s; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(names.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int size = names.size();
            int barHeight = Math.min(22, (h - 20) / size - 8);
            
            for (int i = 0; i < size; i++) {
                int y = 10 + i * (barHeight + 12);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(NAVY);
                g2.drawString(names.get(i), 5, y + barHeight - 4);
                
             // Sửa dòng maxW cũ thành:
                int maxW = w - 160; // Tăng thêm khoảng trống để chứa chữ số (từ 120 lên 160)
                // Bác sửa 2 dòng này: lấy trực tiếp từ list truyền vào thay vì tính lại
                int cap = caps.get(i); 
                int sold = solds.get(i);

                // Tính tỷ lệ dựa trên con số thực tế 240
                int soldW = (cap == 0) ? 0 : (int)((double)sold / cap * maxW);

                g2.setColor(new Color(235, 238, 245));
                g2.fillRoundRect(70, y, maxW, barHeight, 8, 8);

                g2.setColor(new Color(52, 152, 219));
                if(soldW > 0) g2.fillRoundRect(70, y, soldW, barHeight, 8, 8);

                g2.setColor(Color.DARK_GRAY);
                // Tỷ lệ phần trăm tính theo cap (240)
             // Thêm hàm MIN để ép tỷ lệ tối đa là 100%
                int pct = (cap == 0) ? 0 : Math.min(100, (sold * 100 / cap));
                g2.drawString(pct + "% (" + sold + "/" + cap + ")", w - 95, y + barHeight - 4);
            }
        }
    }

    class RealtimeDonutChart extends JPanel {
        private int[] data = {0, 0, 0};
        private final Color[] colors = {
            new Color(52, 152, 219),   // Ghế cứng - Xanh dương
            new Color(46, 204, 113),   // Ghế nằm  - Xanh lá
            new Color(241, 196, 15)    // Ghế mềm  - Vàng
        };
        private final String[] labels = {"Ghế cứng", "Ghế nằm", "Ghế mềm"};

        public RealtimeDonutChart() { setOpaque(false); }
        public void setData(int gc, int gn, int gm) {
            data[0] = gc; data[1] = gn; data[2] = gm;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int total = data[0] + data[1] + data[2];

            // --- Layout: 60% trái cho donut, 40% phải cho legend ---
            int chartAreaW = (int)(w * 0.60);
            int legendAreaX = chartAreaW;
            int legendAreaW = w - chartAreaW;

            // Donut size: fit trong vùng trái
            int size = Math.min(chartAreaW, h) - 24;
            int cx = (chartAreaW - size) / 2;
            int cy = (h - size) / 2;

            // --- Vẽ donut ---
            if (total == 0) {
                g2.setColor(new Color(220, 225, 235));
                g2.fillOval(cx, cy, size, size);
            } else {
                int startAngle = 90;
                for (int i = 0; i < 3; i++) {
                    if (data[i] == 0) continue;
                    int arc = (int) Math.round((double) data[i] / total * 360);
                    g2.setColor(colors[i]);
                    g2.fillArc(cx, cy, size, size, startAngle, -arc);
                    startAngle -= arc;
                }
            }

            // Lỗ donut
            g2.setColor(Color.WHITE);
            int inner = (int)(size * 0.56);
            int ix = cx + (size - inner) / 2;
            int iy = cy + (size - inner) / 2;
            g2.fillOval(ix, iy, inner, inner);

            // Chữ tổng ở giữa donut
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(NAVY);
            String totalTxt = total == 0 ? "0 vé" : total + " vé";
            int tw = g2.getFontMetrics().stringWidth(totalTxt);
            g2.drawString(totalTxt, cx + size / 2 - tw / 2, cy + size / 2 + 5);

         // --- Vẽ Legend dọc bên phải ---
         // Mỗi mục cần: 14px (tên) + 20px (số vé) + 16px (%) + 16px (khoảng cách) = ~66px
         int itemH = Math.min(h / 3, 70); // Mỗi mục tối đa 70px, chia đều cho 3
         int totalLegendH = 3 * itemH;
         int startY = (h - totalLegendH) / 2; // Căn giữa theo chiều dọc

         for (int i = 0; i < 3; i++) {
             int lx = legendAreaX + 12;
             int ly = startY + i * itemH;

             // Ô màu vuông bo góc
             g2.setColor(colors[i]);
             g2.fillRoundRect(lx, ly + 2, 12, 12, 4, 4);

             // Tên loại ghế
             g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
             g2.setColor(new Color(100, 105, 120));
             g2.drawString(labels[i], lx + 18, ly + 13);

             // Số vé (bold)
             g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
             g2.setColor(NAVY);
             g2.drawString(data[i] + " vé", lx + 18, ly + 31);

             // Tỷ lệ %
             g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
             g2.setColor(new Color(130, 135, 155));
             String pct = total == 0 ? "0%" : (data[i] * 100 / total) + "%";
             g2.drawString(pct, lx + 18, ly + 47);

             // Đường kẻ phân cách (trừ mục cuối)
             if (i < 2) {
                 g2.setColor(new Color(230, 233, 240));
                 g2.drawLine(lx, ly + itemH - 6, lx + legendAreaW - 20, ly + itemH - 6);
             }
         }
        }
    }
    class RealtimeHourlyLineChart extends JPanel {
        private int[] hourlyData = new int[24];
        
        private static final int HOUR_START = 6;
        private static final int HOUR_END = 18;
        private static final int HOUR_COUNT = HOUR_END - HOUR_START + 1; // 13 điểm: 6h->18h

        public RealtimeHourlyLineChart() { setOpaque(false); }
        public void setData(int[] data) { this.hourlyData = data; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 45, padB = 30, padT = 15, padR = 15;

            // Tìm max trong khung 6h-18h
            int rawMax = 0;
            for (int i = HOUR_START; i <= HOUR_END; i++)
                if (hourlyData[i] > rawMax) rawMax = hourlyData[i];

         // Tính step Y đẹp
            int step = 1;
            if (rawMax == 0)      step = 10; // ← thêm dòng này lên đầu
            else if (rawMax > 50)  step = 20;
            else if (rawMax > 20) step = 10;
            else if (rawMax > 10) step = 5;
            else if (rawMax > 5)  step = 2;
            else step = 1;

            // Làm tròn max lên bội số của step, thêm 1 bậc trên đỉnh
            int max = (rawMax == 0) ? 50 : ((rawMax / step) + 2) * step;
            int gridLines = max / step; // số đường lưới ngang

            // Vẽ trục
            g2.setColor(new Color(220, 225, 235));
            g2.drawLine(padL, padT, padL, h - padB);
            g2.drawLine(padL, h - padB, w - padR, h - padB);

            int chartW = w - padL - padR;
            int chartH = h - padB - padT;
            int stepX = chartW / (HOUR_COUNT - 1);

            // Lưới ngang + nhãn trục Y (số nguyên đẹp)
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 1; i <= gridLines; i++) {
                int val = i * step;
                int yGrid = h - padB - (int)((double) val / max * chartH);
                if (yGrid < padT) break; // Không vẽ ra ngoài vùng
                g2.setColor(new Color(230, 235, 245));
                g2.drawLine(padL, yGrid, w - padR, yGrid);
                g2.setColor(Color.GRAY);
                String lbl = String.valueOf(val);
                int lw = g2.getFontMetrics().stringWidth(lbl);
                g2.drawString(lbl, padL - lw - 5, yGrid + 4);
            }

            // Tính tọa độ 6h -> 18h
            int[] xs = new int[HOUR_COUNT];
            int[] ys = new int[HOUR_COUNT];
            Path2D path = new Path2D.Float();

            for (int i = 0; i < HOUR_COUNT; i++) {
                int hour = HOUR_START + i;
                xs[i] = padL + i * stepX;
                ys[i] = h - padB - (int)((double) hourlyData[hour] / max * chartH);

                if (i == 0) path.moveTo(xs[i], ys[i]);
                else path.lineTo(xs[i], ys[i]);

                // Nhãn trục X: mỗi giờ (6,7,8,...,18)
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(Color.GRAY);
                String lbl = hour + "h";
                int lw = g2.getFontMetrics().stringWidth(lbl);
                g2.drawString(lbl, xs[i] - lw / 2, h - padB + 16);
            }

            // Vẽ đường line
            g2.setColor(new Color(46, 204, 113));
            g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);

            // Chấm tròn tại các giờ có dữ liệu
            for (int i = 0; i < HOUR_COUNT; i++) {
                int hour = HOUR_START + i;
                if (hourlyData[hour] > 0) {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                    g2.setColor(new Color(46, 204, 113));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(xs[i] - 4, ys[i] - 4, 8, 8);
                }
            }
        }
    }

    class RoundedPanel extends JPanel {
        private int radius; private Color bgColor;
        public RoundedPanel(int radius, Color bgColor) { this.radius = radius; this.bgColor = bgColor; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor); g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius); super.paintComponent(g);
        }
    }
 // Doanh thu: hiển thị % thay đổi
 private String formatPct(long today, long yesterday) {
     if (yesterday == 0) return "<html><font color='gray'>-- vs hôm qua</font></html>";
     double pct = (double)(today - yesterday) / yesterday * 100;
     boolean isUp = pct >= 0;
     String arrow = isUp ? "▲" : "▼";
     String color = isUp ? "#27ae60" : "#c0392b";
     return String.format(
             "<html><font color='%s'><b>%s %.1f%%</b></font> vs hôm qua</html>",
             color, arrow, Math.abs(pct)
     );
 }

    private String formatInt(long today, long yesterday) {
        long diff = today - yesterday;
        boolean isUp = diff >= 0;
        String arrow = isUp ? "▲" : "▼";
        String color = isUp ? "#27ae60" : "#c0392b";
        return String.format(
                "<html><font color='%s'><b>%s %d vé</b></font> vs hôm qua</html>",
                color, arrow, Math.abs(diff)
        );
    }
    // Tô màu: xanh = tốt, đỏ = xấuD
    private String formatIntInvert(long today, long yesterday) {
        long diff = today - yesterday;
        boolean isUp = diff >= 0;
        String arrow = isUp ? "▲" : "▼";
        // Vé hủy tăng → đỏ (xấu), giảm → xanh (tốt)
        String color = isUp ? "#c0392b" : "#27ae60";
        return String.format(
                "<html><font color='%s'><b>%s %d vé</b></font> vs hôm qua</html>",
                color, arrow, Math.abs(diff)
        );
    }
}