package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DatVeGUI0 extends JPanel {

    private static final Object[][] GA_DATA = {
        {"Ngọc Hồi",   0.425, 0.168,   0.621, 0.169},
        {"Phủ Lý",     0.442, 0.193,   0.551, 0.193},
        {"Nam Định",   0.467, 0.206,   0.530, 0.214},
        {"Ninh Bình",  0.456, 0.223,   0.507, 0.235},
        {"Thanh Hóa",  0.439, 0.253,   0.469, 0.264},
        {"Vinh",       0.425, 0.319,   0.460, 0.317},
        {"Hà Tĩnh",    0.456, 0.344,   0.493, 0.337},
        {"Vũng Áng",   0.491, 0.367,   0.516, 0.362},
        {"Đồng Hới",   0.512, 0.406,   0.544, 0.398},
        {"Đông Hà",    0.551, 0.442,   0.577, 0.426},
        {"Huế",        0.589, 0.469,   0.614, 0.453},
        {"Đà Nẵng",    0.638, 0.496,   0.664, 0.478},
        {"Tam Kỳ",     0.668, 0.531,   0.696, 0.524},
        {"Quảng Ngãi", 0.701, 0.565,   0.734, 0.561},
        {"Bồng Sơn",   0.715, 0.601,   0.750, 0.602},
        {"Diêu Trì",   0.724, 0.642,   0.762, 0.647},
        {"Tuy Hòa",    0.734, 0.697,   0.766, 0.693},
        {"Khánh Hòa",  0.724, 0.743,   0.771, 0.749},
        {"Tháp Chàm",  0.720, 0.788,   0.771, 0.791},
        {"Phan Rí",    0.687, 0.822,   0.713, 0.838},
        {"Long Thành", 0.568, 0.848,   0.643, 0.873},
        {"Thủ Thiêm",  0.521, 0.857,   0.600, 0.907}
    };

    private static final String GA_DI_MAC_DINH   = "Diêu Trì";
    private static final Color  CLR_ROUTE_NORMAL = new Color(30,  100, 190, 210);
    private static final Color  CLR_ROUTE_SEL    = new Color(220,  55,  40, 230);
    private static final Color  CLR_DOT_DI       = new Color(215,  60,  45);
    private static final Color  CLR_DOT_DEN      = new Color( 30, 115, 205);

    private MapPanel mapPanel;
    private final String gaDen;
    private final String ngayDi;
    private final Runnable onQuayLai;

    public DatVeGUI0(String gaDi, String gaDen, String loaiVe, String ngayDi, String ngayVe, int soLuong, Runnable onQuayLai) {
        this.gaDen = gaDen;
        this.ngayDi = ngayDi;
        this.onQuayLai = onQuayLai;

        setLayout(new GridLayout(1, 2, 0, 0));
        setBackground(Color.WHITE);
        
        add(buildLeftPanel());
        add(buildRightPanel());
    }

    private JPanel buildLeftPanel() {
        mapPanel = new MapPanel();
        mapPanel.setSelectedGaDen(gaDen); 
        return mapPanel;
    }

    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(242, 247, 252));

        // Gom toàn bộ nội dung vào 1 panel để canh giữa tuyệt đối
        JPanel centerContent = new JPanel(new GridBagLayout());
        centerContent.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.CENTER;

        // 1. NHÃN ICON (ĐÃ FIX LỖI NULL POINTER TẠI ĐÂY)
        JLabel lblPlaceholder = new JLabel();
        lblPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        lblPlaceholder.setPreferredSize(new Dimension(250, 120)); 
        
        // Load icon an toàn, nếu không có ảnh sẽ không bị sập app
        Icon ic = loadAndScaleIcon("/Images/khongcochuyen.png", 80, 80);
        if (ic != null) {
            lblPlaceholder.setIcon(ic);
        } else {
            lblPlaceholder.setText("[Không tìm thấy icon]");
        }
        
        gc.insets = new Insets(0, 0, 0, 0);
        centerContent.add(lblPlaceholder, gc);

        // 2. TEXT THÔNG BÁO
        gc.gridy = 1;
        gc.insets = new Insets(5, 0, 110, 0);
        String msg = "<html><div style='text-align: center;'>Rất tiếc, hiện không có chuyến tàu nào phù hợp<br>ngày " + ngayDi + "</div></html>";
        JLabel lblMessage = new JLabel(msg);
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblMessage.setForeground(Color.BLACK);
        centerContent.add(lblMessage, gc);

        // 3. NÚT QUAY LẠI 
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 0, 0);
        Icon icQuayLai = loadAndScaleIcon("/Images/logoBack.png", 14, 14);
        JButton btnQuay = makeOutlineBtn("Quay lại", icQuayLai);
        btnQuay.addActionListener(e -> {
            if (onQuayLai != null) onQuayLai.run();
        });
        centerContent.add(btnQuay, gc);

        outer.add(centerContent, BorderLayout.CENTER);
        return outer;
    }

    private Icon loadAndScaleIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                Image scaledImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } catch (Exception e) {}
        return null;
    }

    private JButton makeOutlineBtn(String text, Icon icon) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean en = isEnabled();
                g2.setColor(en
                        ? (getModel().isPressed() ? new Color(198, 215, 242)
                                : getModel().isRollover() ? new Color(212, 228, 250) : new Color(226, 236, 252))
                        : new Color(238, 241, 248));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(en ? new Color(28, 57, 110) : new Color(175, 185, 205));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g); 
            }
        };
        if (icon != null) b.setIcon(icon);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setForeground(new Color(28, 57, 110));
        b.setIconTextGap(8); 
        b.setBorder(new EmptyBorder(6, 16, 6, 16)); 
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private class MapPanel extends JPanel {
        private String   selectedGaDen = null;
        private Image    mapImage      = null;

        MapPanel() {
            setBackground(new Color(200, 225, 245));
            loadImage();
        }

        void setSelectedGaDen(String ga) { this.selectedGaDen = ga; repaint(); }

        private void loadImage() {
            for (String path : new String[]{"/Images/BanDo.png", "/BanDo.png"}) {
                try {
                    java.net.URL url = getClass().getResource(path);
                    if (url != null) { mapImage = new ImageIcon(url).getImage(); return; }
                } catch (Exception ignored) {}
            }
        }

        private Point toScreen(double xPct, double yPct) {
            return new Point((int)(xPct*getWidth()), (int)(yPct*getHeight()));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (mapImage != null) g2.drawImage(mapImage, 0, 0, getWidth(), getHeight(), this);
            else { g2.setColor(new Color(140,185,150)); g2.fillRect(0,0,getWidth(),getHeight()); }

            int idxDi = -1, idxDen = -1;
            for (int i = 0; i < GA_DATA.length; i++) {
                if (GA_DATA[i][0].equals(GA_DI_MAC_DINH)) idxDi = i;
                if (GA_DATA[i][0].equals(selectedGaDen))  idxDen = i;
            }
            int hlFrom = -1, hlTo = -1;
            if (idxDi >= 0 && idxDen >= 0) {
                hlFrom = Math.min(idxDi, idxDen); hlTo = Math.max(idxDi, idxDen);
            }

            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < GA_DATA.length-1; i++) {
                Point p1 = toScreen((double)GA_DATA[i][1],   (double)GA_DATA[i][2]);
                Point p2 = toScreen((double)GA_DATA[i+1][1], (double)GA_DATA[i+1][2]);
                g2.setColor((hlFrom>=0 && i>=hlFrom && i<hlTo) ? CLR_ROUTE_SEL : CLR_ROUTE_NORMAL);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            Font fNormal = new Font("Segoe UI", Font.PLAIN, 11);
            Font fBold   = new Font("Segoe UI", Font.BOLD,  11);

            for (Object[] ga : GA_DATA) {
                String name = (String)ga[0];
                Point  pt   = toScreen((double)ga[1], (double)ga[2]);
                boolean isDi = name.equals(GA_DI_MAC_DINH), isDen = name.equals(selectedGaDen);
                int dotR; Color dotFill, dotBorder;
                if      (isDi)  { dotR=7; dotFill=CLR_DOT_DI;  dotBorder=new Color(150,25,10); }
                else if (isDen) { dotR=7; dotFill=CLR_DOT_DEN; dotBorder=new Color(10,55,135); }
                else            { dotR=4; dotFill=Color.WHITE;  dotBorder=new Color(40,90,160,170); }
                g2.setColor(dotBorder);
                g2.fillOval(pt.x-dotR-2, pt.y-dotR-2, (dotR+2)*2, (dotR+2)*2);
                g2.setColor(dotFill);
                g2.fillOval(pt.x-dotR, pt.y-dotR, dotR*2, dotR*2);
            }

            g2.setStroke(new BasicStroke(0.7f));
            for (Object[] ga : GA_DATA) {
                String name = (String)ga[0];
                Point  lp   = toScreen((double)ga[3], (double)ga[4]);
                boolean isDi = name.equals(GA_DI_MAC_DINH), isDen = name.equals(selectedGaDen);
                Font useFont = (isDi||isDen) ? fBold : fNormal;
                g2.setFont(useFont);
                FontMetrics fmu = g2.getFontMetrics(useFont);
                int ty = lp.y + fmu.getAscent()/2;
                g2.setColor(isDi ? new Color(165,25,8) : isDen ? new Color(15,65,155) : new Color(20,50,90));
                g2.drawString(name, lp.x, ty);
            }
            g2.dispose();
        }
    }
}