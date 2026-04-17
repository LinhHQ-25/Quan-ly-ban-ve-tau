package GUI;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DatVeGUI extends JPanel {

    private static final Object[][] GA_DATA = {
        {"Ngọc Hồi",   0.460, 0.165},
        {"Phủ Lý",     0.510, 0.205},
        {"Nam Định",   0.540, 0.230},
        {"Ninh Bình",  0.515, 0.245},
        {"Thanh Hóa",  0.490, 0.285},
        {"Vinh",       0.460, 0.355},
        {"Hà Tĩnh",    0.480, 0.385},
        {"Vũng Áng",   0.510, 0.415},
        {"Đồng Hới",   0.550, 0.455},
        {"Đông Hà",    0.600, 0.495},
        {"Huế",        0.640, 0.525},
        {"Đà Nẵng",    0.690, 0.565},
        {"Tam Kỳ",     0.710, 0.595},
        {"Quảng Ngãi", 0.730, 0.625},
        {"Bồng Sơn",   0.725, 0.675},
        {"Diêu Trì",   0.720, 0.725},   // ← GA ĐI MẶC ĐỊNH
        {"Tuy Hòa",    0.740, 0.775},
        {"Khánh Hòa",  0.750, 0.815},
        {"Tháp Chàm",  0.720, 0.855},
        {"Phan Rí",    0.680, 0.875},
        {"Long Thành", 0.600, 0.905},
        {"Thủ Thiêm",  0.490, 0.905},
    };

    private static final String GA_DI_MAC_DINH   = "Diêu Trì";
    private static final Color  CLR_ROUTE_NORMAL = new Color(30,  100, 190, 210);
    private static final Color  CLR_ROUTE_SEL    = new Color(220,  55,  40, 230);
    private static final Color  CLR_DOT_DI       = new Color(215,  60,  45);
    private static final Color  CLR_DOT_DEN      = new Color( 30, 115, 205);

    // Form fields
    private JTextField       txtGaDi;
    private JComboBox<String> cbGaDen;
    private JRadioButton     rbMotChieu, rbKhuHoi;
    private JDateChooser     dcNgayDi, dcNgayVe;
    private final int[]      soLuong = {1};
    private JLabel           lblSoLuong;
    private MapPanel         mapPanel;

    public DatVeGUI() {
        setLayout(new GridLayout(1, 2, 0, 0));
        setBackground(Color.WHITE);
        add(buildLeftPanel());
        add(buildRightPanel());
    }

    // =====================================================
    // PANEL TRÁI: bản đồ click-able
    // =====================================================
    private JPanel buildLeftPanel() {
        mapPanel = new MapPanel();
        mapPanel.setOnGaSelected(gaName -> {
            if (!gaName.equals(GA_DI_MAC_DINH) && cbGaDen != null)
                cbGaDen.setSelectedItem(gaName);
        });
        return mapPanel;
    }

    // =====================================================
    // PANEL PHẢI: form đặt vé
    // =====================================================
    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(242, 247, 252));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(36, 44, 28, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Tiêu đề
        JLabel title = new JLabel("Vui lòng điền/chọn thông tin vào đây");
        title.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        title.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 22, 0);
        form.add(title, gbc);
        gbc.gridwidth = 1;

        // GA ĐI (disabled)
        txtGaDi = new JTextField(GA_DI_MAC_DINH);
        txtGaDi.setEditable(false); txtGaDi.setEnabled(false);
        txtGaDi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        addRow(form, gbc, 1, "Ga đi:", wrapField(txtGaDi, true), 14);

        // GA ĐẾN
        cbGaDen = new JComboBox<>(buildGaDenList());
        cbGaDen.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cbGaDen.setBackground(Color.WHITE); cbGaDen.setBorder(null);
        cbGaDen.addActionListener(e -> {
            if (mapPanel != null)
                mapPanel.setSelectedGaDen((String) cbGaDen.getSelectedItem());
        });
        addRow(form, gbc, 2, "Ga đến:", wrapCombo(cbGaDen), 14);

        // LOẠI VÉ
        rbMotChieu = new JRadioButton("Một chiều", true);
        rbKhuHoi   = new JRadioButton("Khứ hồi");
        styleRadio(rbMotChieu); styleRadio(rbKhuHoi);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMotChieu); bg.add(rbKhuHoi);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(rbMotChieu);
        radioPanel.add(Box.createHorizontalStrut(20));
        radioPanel.add(rbKhuHoi);
        rbMotChieu.addActionListener(e -> setNgayVeEnabled(false));
        rbKhuHoi  .addActionListener(e -> setNgayVeEnabled(true));
        addRow(form, gbc, 3, "Loại vé:", radioPanel, 14);

        // NGÀY ĐI
        dcNgayDi = buildDateChooser(true);
        addRow(form, gbc, 4, "Ngày đi:", wrapDC(dcNgayDi), 14);

        // NGÀY VỀ (disabled ban đầu)
        dcNgayVe = buildDateChooser(false);
        addRow(form, gbc, 5, "Ngày về:", wrapDC(dcNgayVe), 14);

        // SỐ LƯỢNG
        addRow(form, gbc, 6, "Số lượng:", buildSoLuongPanel(), 10);

        // NÚT TÌM CHUYẾN
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(18, 0, 0, 0);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(buildTimButton());
        form.add(btnWrap, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    // =====================================================
    // JDateChooser (thư viện JCalendar/jcalendar)
    // =====================================================
    private JDateChooser buildDateChooser(boolean enabled) {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setDate(new java.util.Date());
        dc.setEnabled(enabled);
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(enabled ? Color.WHITE : new Color(225, 235, 245));
        dc.setBorder(null);
        // Xóa border của text editor bên trong
        Component editor = dc.getDateEditor().getUiComponent();
        if (editor instanceof JComponent) ((JComponent) editor).setBorder(null);
        return dc;
    }

    /** Bọc JDateChooser trong khung bo góc tự vẽ */
    private JPanel wrapDC(JDateChooser dc) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dc.isEnabled() ? Color.WHITE : new Color(225, 235, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 10, 4, 6));
        dc.setOpaque(false);
        p.add(dc, BorderLayout.CENTER);
        return p;
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private String[] buildGaDenList() {
        List<String> list = new ArrayList<>();
        for (Object[] ga : GA_DATA)
            if (!ga[0].equals(GA_DI_MAC_DINH)) list.add((String) ga[0]);
        return list.toArray(new String[0]);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row,
                        String labelText, JComponent comp, int bottomGap) {
        gbc.gridy = row; gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.weightx = 0.34;
        gbc.insets = new Insets(0, 0, bottomGap, 0);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(55, 55, 55));
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.66;
        p.add(comp, gbc);
    }

    private JPanel wrapField(JTextField tf, boolean disabled) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(disabled ? new Color(220, 235, 248) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 12, 6, 12));
        tf.setOpaque(false); tf.setBorder(null);
        tf.setForeground(new Color(50, 50, 50));
        tf.setPreferredSize(new Dimension(0, 24));
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    private JPanel wrapCombo(JComboBox<String> cb) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 8, 4, 4));
        cb.setOpaque(false); cb.setBorder(null);
        p.add(cb, BorderLayout.CENTER);
        return p;
    }

    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        rb.setForeground(new Color(50, 50, 50));
    }

    private void setNgayVeEnabled(boolean enabled) {
        dcNgayVe.setEnabled(enabled);
        dcNgayVe.setBackground(enabled ? Color.WHITE : new Color(225, 235, 245));
        Component editor = dcNgayVe.getDateEditor().getUiComponent();
        if (editor != null) editor.setEnabled(enabled);
        if (dcNgayVe.getParent() != null) dcNgayVe.getParent().repaint();
    }

    // =====================================================
    // SỐ LƯỢNG
    // =====================================================
    private JPanel buildSoLuongPanel() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(2, 6, 2, 6));

        lblSoLuong = new JLabel("  1  ", SwingConstants.CENTER);
        lblSoLuong.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lblSoLuong.setPreferredSize(new Dimension(34, 30));
        JButton btnMinus = buildCountBtn("−");
        JButton btnPlus  = buildCountBtn("+");

        btnMinus.addActionListener(e -> {
            if (soLuong[0] > 1) { soLuong[0]--; lblSoLuong.setText("  " + soLuong[0] + "  "); }
        });
        btnPlus.addActionListener(e -> {
            if (soLuong[0] < 99) { soLuong[0]++; lblSoLuong.setText("  " + soLuong[0] + "  "); }
        });
        wrapper.add(btnMinus); wrapper.add(lblSoLuong); wrapper.add(btnPlus);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false); p.add(wrapper);
        return p;
    }

    private JButton buildCountBtn(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(200,218,240)
                    : getModel().isRollover() ? new Color(220,233,248) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(80, 130, 180));
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, (getWidth()-fm.stringWidth(label))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildTimButton() {
        JButton btn = new JButton("Tìm chuyến") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
                    : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (getWidth()-fm.stringWidth(txt))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(120, 36));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =====================================================
    // INNER CLASS: MapPanel — bản đồ có thể click
    // =====================================================
    private class MapPanel extends JPanel {

        private String   selectedGaDen = null;
        private String   hoveredGa     = null;
        private Consumer<String> onGaSelected;
        private Image    mapImage      = null;

        MapPanel() {
            setBackground(new Color(200, 225, 245));
            loadImage();

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    String hit = hitTest(e.getX(), e.getY());
                    if (!Objects.equals(hit, hoveredGa)) {
                        hoveredGa = hit;
                        boolean canClick = hit != null && !hit.equals(GA_DI_MAC_DINH);
                        setCursor(canClick
                            ? new Cursor(Cursor.HAND_CURSOR)
                            : new Cursor(Cursor.DEFAULT_CURSOR));
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    String hit = hitTest(e.getX(), e.getY());
                    if (hit != null && !hit.equals(GA_DI_MAC_DINH)) {
                        selectedGaDen = hit;
                        repaint();
                        if (onGaSelected != null) onGaSelected.accept(hit);
                    }
                }
            });
        }

        void setOnGaSelected(Consumer<String> cb) { this.onGaSelected = cb; }
        void setSelectedGaDen(String ga)          { this.selectedGaDen = ga; repaint(); }

        private void loadImage() {
            for (String path : new String[]{"/Images/BanDo.png", "/BanDo.png"}) {
                try {
                    java.net.URL url = getClass().getResource(path);
                    if (url != null) { mapImage = new ImageIcon(url).getImage(); return; }
                } catch (Exception ignored) {}
            }
        }

        /**
         * Ảnh bản đồ được vẽ giữ tỉ lệ gốc (387:398), căn giữa trong panel.
         * Phương thức này trả về Rectangle mà ảnh thực sự chiếm.
         */
        private Rectangle getDrawRect() {
            // Trả về toàn bộ kích thước của Panel hiện tại
            return new Rectangle(0, 0, getWidth(), getHeight());
        }

        /** % tọa độ → pixel thực trên panel */
        private Point toScreen(double xPct, double yPct) {
            Rectangle r = getDrawRect();
            return new Point(r.x + (int)(xPct * r.width),
                             r.y + (int)(yPct * r.height));
        }

        /** Kiểm tra pixel chuột có trúng ga nào không (vùng hit 10px) */
        private String hitTest(int mx, int my) {
            for (Object[] ga : GA_DATA) {
                Point p = toScreen((double)ga[1], (double)ga[2]);
                if (Math.hypot(mx - p.x, my - p.y) <= 10) return (String) ga[0];
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // Bật khử răng cưa để ảnh bị kéo giãn nhìn vẫn mịn
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            Rectangle r = getDrawRect();

            // Ảnh bản đồ - Vẽ tràn khung
            if (mapImage != null) {
                g2.drawImage(mapImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(new Color(140, 185, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.DARK_GRAY);
                g2.drawString("Không tìm thấy ảnh!", 20, 20);
            }

            // ── Xác định vùng đường nối highlight ──
            int idxDi = -1, idxDen = -1;
            for (int i = 0; i < GA_DATA.length; i++) {
                if (GA_DATA[i][0].equals(GA_DI_MAC_DINH))  idxDi  = i;
                if (GA_DATA[i][0].equals(selectedGaDen))   idxDen = i;
            }
            int hlFrom = -1, hlTo = -1;
            if (idxDi >= 0 && idxDen >= 0) {
                hlFrom = Math.min(idxDi, idxDen);
                hlTo   = Math.max(idxDi, idxDen);
            }

            // ── Vẽ đường ray ──
            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < GA_DATA.length - 1; i++) {
                Point p1 = toScreen((double)GA_DATA[i][1],   (double)GA_DATA[i][2]);
                Point p2 = toScreen((double)GA_DATA[i+1][1], (double)GA_DATA[i+1][2]);
                boolean hl = hlFrom >= 0 && i >= hlFrom && i < hlTo;
                g2.setColor(hl ? CLR_ROUTE_SEL : CLR_ROUTE_NORMAL);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            // ── Vẽ điểm ga + label ──
            Font fNormal = new Font("Segoe UI", Font.PLAIN, 10);
            Font fBold   = new Font("Segoe UI", Font.BOLD,  10);
            g2.setFont(fNormal);
            FontMetrics fm = g2.getFontMetrics(fNormal);

            for (Object[] ga : GA_DATA) {
                String name = (String) ga[0];
                Point  pt   = toScreen((double) ga[1], (double) ga[2]);

                boolean isDi       = name.equals(GA_DI_MAC_DINH);
                boolean isDen      = name.equals(selectedGaDen);
                boolean isHovered  = name.equals(hoveredGa);

                // Kích thước và màu điểm
                int    dotR;
                Color  dotFill, dotBorder;
                if (isDi) {
                    dotR = 7; dotFill = CLR_DOT_DI; dotBorder = new Color(150, 25, 10);
                } else if (isDen) {
                    dotR = 7; dotFill = CLR_DOT_DEN; dotBorder = new Color(10, 55, 135);
                } else if (isHovered) {
                    dotR = 5; dotFill = new Color(110, 175, 235); dotBorder = new Color(40, 90, 160);
                } else {
                    dotR = 4; dotFill = Color.WHITE; dotBorder = new Color(40, 90, 160, 170);
                }

                // Vòng ngoài border
                g2.setColor(dotBorder);
                g2.fillOval(pt.x - dotR - 2, pt.y - dotR - 2, (dotR+2)*2, (dotR+2)*2);
                // Chấm trong
                g2.setColor(dotFill);
                g2.fillOval(pt.x - dotR, pt.y - dotR, dotR*2, dotR*2);

                // Label
                Font   useFont = (isDi || isDen) ? fBold : fNormal;
                g2.setFont(useFont);
                FontMetrics fmu = g2.getFontMetrics(useFont);
                int tw = fmu.stringWidth(name);
                int tx = pt.x + dotR + 4;
                int ty = pt.y + fmu.getAscent()/2 - 1;
                if (tx + tw > r.x + r.width - 4) tx = pt.x - tw - dotR - 3;

                // Nền label mờ
                g2.setColor(new Color(255, 255, 255, 175));
                g2.fillRoundRect(tx-2, ty-fmu.getAscent()+1, tw+4, fmu.getHeight()-1, 3, 3);
                // Chữ
                g2.setColor(isDi ? new Color(165, 25, 8)
                    : isDen ? new Color(15, 65, 155)
                    : new Color(20, 50, 90));
                g2.drawString(name, tx, ty);

                g2.setFont(fNormal); // reset
            }

            g2.dispose();
        }
    }
}