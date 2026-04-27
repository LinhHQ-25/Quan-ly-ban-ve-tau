package GUI;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DatVeGUI extends JPanel {

    // {tên, dotX%, dotY%, labelX%, labelY%} 428x561
    // dotX/Y = tọa độ điểm tròn trên bản đồ
    // labelX/Y = tọa độ tên ga (có thể chỉnh độc lập)
	private static final Object[][] GA_DATA = {
		    {"Ngọc Hồi",   0.425, 0.168,   0.621, 0.169},// x≈182, y≈94  a=266 b=95
		    {"Phủ Lý",     0.442, 0.193,   0.551, 0.193},// x≈189, y≈108 a=236 b=108
		    {"Nam Định",   0.467, 0.206,   0.530, 0.214},// x≈200, y≈116 a=227 b=120
		    {"Ninh Bình",  0.456, 0.223,   0.507, 0.235},// x≈195, y≈125 a=217 b=132
		    {"Thanh Hóa",  0.439, 0.253,   0.469, 0.264},// x≈188, y≈142 a=201 b=148
		    {"Vinh",       0.425, 0.319,   0.460, 0.317},// x≈182, y≈179 a=197 b=178
		    {"Hà Tĩnh",    0.456, 0.344,   0.493, 0.337},// x≈195, y≈193 a=211 b=189
		    {"Vũng Áng",   0.491, 0.367,   0.516, 0.362},// x≈210, y≈206 a=221 b=203
		    {"Đồng Hới",   0.512, 0.406,   0.544, 0.398},// x≈219, y≈228 a=233 b=223
		    {"Đông Hà",    0.551, 0.442,   0.577, 0.426},// x≈236, y≈248 a=247 b=239
		    {"Huế",        0.589, 0.469,   0.614, 0.453},// x≈252, y≈263 a=263 b=254
		    {"Đà Nẵng",    0.638, 0.496,   0.664, 0.478},// x≈273, y≈278 a=284 b=268
		    {"Tam Kỳ",     0.668, 0.531,   0.696, 0.524},// x≈286, y≈298 a=298 b=294
		    {"Quảng Ngãi", 0.701, 0.565,   0.734, 0.561},// x≈300, y≈317 a=314 b=315
		    {"Bồng Sơn",   0.715, 0.601,   0.750, 0.602},// x≈306, y≈337 a=321 b=338
		    {"Diêu Trì",   0.724, 0.642,   0.762, 0.647},// x≈310, y≈360 a=326 b=363
		    {"Tuy Hòa",    0.734, 0.697,   0.766, 0.693},// x≈314, y≈391 a=328 b=389
		    {"Khánh Hòa",  0.724, 0.743,   0.771, 0.749},// x≈310, y≈417 a=330 b=420
		    {"Tháp Chàm",  0.720, 0.788,   0.771, 0.791},// x≈308, y≈442 a=330 b=444
		    {"Phan Rí",    0.687, 0.822,   0.713, 0.838},// x≈294, y≈461 a=305 b=470
		    {"Long Thành", 0.568, 0.848,   0.643, 0.873},// x≈243, y≈476 a=275 b=490
		    {"Thủ Thiêm",  0.521, 0.857,   0.600, 0.907} // x≈223, y≈481 a=257 b=509
		};

    private static final String GA_DI_MAC_DINH   = "Diêu Trì";
    private static final Color  CLR_ROUTE_NORMAL = new Color(30,  100, 190, 210);
    private static final Color  CLR_ROUTE_SEL    = new Color(220,  55,  40, 230);
    private static final Color  CLR_DOT_DI       = new Color(215,  60,  45);
    private static final Color  CLR_DOT_DEN      = new Color( 30, 115, 205);

    // Form fields
    private JTextField        txtGaDi;
    private JTextField        txtGaDen;     // hiển thị tên ga đến
    private JComboBox<String> cbGaDen;      // dùng nội bộ để lưu danh sách + popup
    private JRadioButton      rbMotChieu, rbKhuHoi;
    private JDateChooser      dcNgayDi, dcNgayVe;
    private JTextField        txtSoLuong;   // ← nhập được + tăng/giảm
    private MapPanel          mapPanel;

    public DatVeGUI() {
        setLayout(new GridLayout(1, 2, 0, 0));
        setBackground(Color.WHITE);
        add(buildLeftPanel());
        add(buildRightPanel());
    }

    // =====================================================
    // PANEL TRÁI
    // =====================================================
    private JPanel buildLeftPanel() {
        mapPanel = new MapPanel();
        mapPanel.setOnGaSelected(gaName -> {
            if (!gaName.equals(GA_DI_MAC_DINH) && cbGaDen != null) {
                cbGaDen.setSelectedItem(gaName);
                if (txtGaDen != null) txtGaDen.setText(gaName);
            }
        });
        return mapPanel;
    }

    // =====================================================
    // PANEL PHẢI
    // =====================================================
    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(242, 247, 252));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(36, 44, 28, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // ── Tiêu đề: căn GIỮA, font 17 ──
        JLabel title = new JLabel("Vui lòng điền/chọn thông tin vào đây", SwingConstants.CENTER);
        title.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 17));
        title.setForeground(new Color(45, 45, 45));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 26, 0);
        form.add(title, gbc);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        txtGaDi = new JTextField(GA_DI_MAC_DINH);
        txtGaDi.setEditable(false);
        txtGaDi.setEnabled(false);
        txtGaDi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        addRow(form, gbc, 1, "Ga đi:", wrapField(txtGaDi, true), 14);

        String[] gaDenList = buildGaDenList();
        cbGaDen = new JComboBox<>(gaDenList);
        cbGaDen.setVisible(false);

        txtGaDen = new JTextField(gaDenList.length > 0 ? gaDenList[0] : "");
        txtGaDen.setEditable(false);
        txtGaDen.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));

        // Nút mũi tên ▼ tự vẽ
        JButton btnArrow = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getParent() != null ? getParent().getBackground() : Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Vẽ mũi tên nhỏ
                int cx = getWidth() / 2, cy = getHeight() / 2;
                int[] xs = {cx - 5, cx + 5, cx};
                int[] ys = {cy - 3, cy - 3, cy + 3};
                g2.setColor(new Color(100, 130, 165));
                g2.fillPolygon(xs, ys, 3);
                g2.dispose();
            }
        };
        btnArrow.setPreferredSize(new Dimension(28, 28));
        btnArrow.setContentAreaFilled(false);
        btnArrow.setBorderPainted(false);
        btnArrow.setFocusPainted(false);
        btnArrow.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Bọc txtGaDen + btnArrow vào panel vẽ khung giống ga đi
        JPanel gaDenWrap = new JPanel(new BorderLayout()) {
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
        gaDenWrap.setOpaque(false);
        gaDenWrap.setBorder(new EmptyBorder(6, 12, 6, 6));
        txtGaDen.setOpaque(false);
        txtGaDen.setBorder(null);
        txtGaDen.setForeground(new Color(50, 50, 50));
        gaDenWrap.add(txtGaDen, BorderLayout.CENTER);
        gaDenWrap.add(btnArrow, BorderLayout.EAST);

        // ── Dropdown gọn: JList bọc trong JScrollPane, hiện qua JPopupMenu ──
        JList<String> listGa = new JList<>(gaDenList);
        listGa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        listGa.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listGa.setSelectedIndex(0);
        listGa.setFixedCellHeight(28);
        listGa.setBorder(new EmptyBorder(2, 8, 2, 8));
        listGa.setBackground(Color.WHITE);
        listGa.setSelectionBackground(new Color(210, 228, 248));
        listGa.setSelectionForeground(new Color(30, 70, 140));

        JScrollPane scrollGa = new JScrollPane(listGa);
        scrollGa.setBorder(null);
        scrollGa.getVerticalScrollBar().setUnitIncrement(16);

        JPopupMenu popup = new JPopupMenu();
        popup.setLayout(new BorderLayout());
        popup.setBorder(BorderFactory.createLineBorder(new Color(180, 205, 230), 1));
        popup.add(scrollGa, BorderLayout.CENTER);

        // Chọn item bằng click chuột
        listGa.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                String sel = listGa.getSelectedValue();
                if (sel != null) {
                    txtGaDen.setText(sel);
                    cbGaDen.setSelectedItem(sel);
                    if (mapPanel != null) mapPanel.setSelectedGaDen(sel);
                    popup.setVisible(false);
                }
            }
        });

        // Mở popup rộng bằng đúng ô ga đến, cao tối đa 200px
        Runnable openDropdown = () -> {
            popup.setPopupSize(gaDenWrap.getWidth(), 200);
            int idx = Arrays.asList(gaDenList).indexOf(txtGaDen.getText());
            if (idx >= 0) listGa.setSelectedIndex(idx);
            listGa.ensureIndexIsVisible(Math.max(0, idx));
            popup.show(gaDenWrap, 0, gaDenWrap.getHeight());
        };

        txtGaDen.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openDropdown.run(); }
        });
        txtGaDen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnArrow.addActionListener(e -> openDropdown.run());

        addRow(form, gbc, 2, "Ga đến:", gaDenWrap, 14);

        // ── LOẠI VÉ ──
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
        rbMotChieu.addActionListener(e -> syncNgayVe());
        rbKhuHoi  .addActionListener(e -> setNgayVeEnabled(true));
        addRow(form, gbc, 3, "Loại vé:", radioPanel, 14);

        // ── NGÀY ĐI ──
        dcNgayDi = buildDateChooser(true);
        // Khi ngày đi thay đổi → nếu đang 1 chiều thì sync ngày về
        dcNgayDi.addPropertyChangeListener("date", evt -> {
            if (rbMotChieu != null && rbMotChieu.isSelected()) syncNgayVe();
        });
        addRow(form, gbc, 4, "Ngày đi:", wrapDC(dcNgayDi), 14);

        // ── NGÀY VỀ (disabled ban đầu, sync với ngày đi khi 1 chiều) ──
        dcNgayVe = buildDateChooser(false);
        addRow(form, gbc, 5, "Ngày về:", wrapDC(dcNgayVe), 14);

        // ── SỐ LƯỢNG: JTextField nhập được + nút ±  ──
        addRow(form, gbc, 6, "Số lượng:", buildSoLuongPanel(), 10);

        // ── NÚT TÌM CHUYẾN: căn GIỮA ──
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(buildTimButton());
        form.add(btnWrap, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    // ── Sync ngày về = ngày đi (khi chọn 1 chiều) ──
    private void syncNgayVe() {
        setNgayVeEnabled(false);
        if (dcNgayDi != null && dcNgayVe != null)
            dcNgayVe.setDate(dcNgayDi.getDate());
    }

    // =====================================================
    // JDateChooser
    // =====================================================
    private JDateChooser buildDateChooser(boolean enabled) {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setDate(new java.util.Date());
        dc.setEnabled(enabled);
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(enabled ? Color.WHITE : new Color(225, 235, 245));
        dc.setBorder(null);
        Component editor = dc.getDateEditor().getUiComponent();
        if (editor instanceof JComponent) ((JComponent) editor).setBorder(null);
        return dc;
    }

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
    // SỐ LƯỢNG — JTextField nhập trực tiếp + nút − / +
    // =====================================================
    private JPanel buildSoLuongPanel() {
        // Panel ngoài bo góc
        JPanel wrapper = new JPanel(new BorderLayout()) {
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

        // Nút −
        JButton btnMinus = buildCountBtn("−");
        // TextField nhập số
        txtSoLuong = new JTextField("1", 3);
        txtSoLuong.setHorizontalAlignment(SwingConstants.CENTER);
        txtSoLuong.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtSoLuong.setOpaque(false);
        txtSoLuong.setBorder(null);
        txtSoLuong.setPreferredSize(new Dimension(38, 30));
        // Chỉ cho nhập số
        txtSoLuong.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) e.consume();
            }
        });
        txtSoLuong.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { clampSoLuong(); }
        });
        // Nút +
        JButton btnPlus = buildCountBtn("+");

        btnMinus.addActionListener(e -> {
            int v = getSoLuong();
            if (v > 1) txtSoLuong.setText(String.valueOf(v - 1));
        });
        btnPlus.addActionListener(e -> {
            int v = getSoLuong();
            if (v < 99) txtSoLuong.setText(String.valueOf(v + 1));
        });

        wrapper.add(btnMinus,    BorderLayout.WEST);
        wrapper.add(txtSoLuong,  BorderLayout.CENTER);
        wrapper.add(btnPlus,     BorderLayout.EAST);

        // Bọc lại để FlowLayout trái
        JPanel outer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        outer.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(110, 34));
        outer.add(wrapper);
        return outer;
    }

    private int getSoLuong() {
        try { return Math.max(1, Math.min(99, Integer.parseInt(txtSoLuong.getText().trim()))); }
        catch (NumberFormatException e) { return 1; }
    }

    private void clampSoLuong() {
        txtSoLuong.setText(String.valueOf(getSoLuong()));
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
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =====================================================
    // INNER CLASS: MapPanel
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
                        setCursor(canClick ? new Cursor(Cursor.HAND_CURSOR)
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

        // Toàn bộ panel = vùng vẽ (ảnh tràn viền)
        private Point toScreen(double xPct, double yPct) {
            return new Point((int)(xPct * getWidth()), (int)(yPct * getHeight()));
        }

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Vẽ ảnh bản đồ tràn viền
            if (mapImage != null) {
                g2.drawImage(mapImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(new Color(140, 185, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
 
            }

            // Xác định vùng highlight
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

            // Vẽ đường ray
            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < GA_DATA.length - 1; i++) {
                Point p1 = toScreen((double)GA_DATA[i][1],   (double)GA_DATA[i][2]);
                Point p2 = toScreen((double)GA_DATA[i+1][1], (double)GA_DATA[i+1][2]);
                boolean hl = hlFrom >= 0 && i >= hlFrom && i < hlTo;
                g2.setColor(hl ? CLR_ROUTE_SEL : CLR_ROUTE_NORMAL);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            // ── Pass 1: vẽ các điểm ga ──
            Font fNormal = new Font("Segoe UI", Font.PLAIN, 11);
            Font fBold   = new Font("Segoe UI", Font.BOLD,  11);

            for (Object[] ga : GA_DATA) {
                String name = (String) ga[0];
                Point  pt   = toScreen((double)ga[1], (double)ga[2]);
                boolean isDi      = name.equals(GA_DI_MAC_DINH);
                boolean isDen     = name.equals(selectedGaDen);
                boolean isHovered = name.equals(hoveredGa);
                int dotR;
                Color dotFill, dotBorder;
                if (isDi) {
                    dotR = 7; dotFill = CLR_DOT_DI;  dotBorder = new Color(150, 25, 10);
                } else if (isDen) {
                    dotR = 7; dotFill = CLR_DOT_DEN; dotBorder = new Color(10, 55, 135);
                } else if (isHovered) {
                    dotR = 5; dotFill = new Color(110, 175, 235); dotBorder = new Color(40, 90, 160);
                } else {
                    dotR = 4; dotFill = Color.WHITE;  dotBorder = new Color(40, 90, 160, 170);
                }
                g2.setColor(dotBorder);
                g2.fillOval(pt.x-dotR-2, pt.y-dotR-2, (dotR+2)*2, (dotR+2)*2);
                g2.setColor(dotFill);
                g2.fillOval(pt.x-dotR, pt.y-dotR, dotR*2, dotR*2);
            }

            // ── Pass 2: label theo tọa độ riêng (cột 3,4 trong GA_DATA) ──
            g2.setStroke(new BasicStroke(0.7f));
            for (int i = 0; i < GA_DATA.length; i++) {
                String name = (String) GA_DATA[i][0];
                Point  pt   = toScreen((double)GA_DATA[i][1], (double)GA_DATA[i][2]);
                Point  lp   = toScreen((double)GA_DATA[i][3], (double)GA_DATA[i][4]);
                boolean isDi  = name.equals(GA_DI_MAC_DINH);
                boolean isDen = name.equals(selectedGaDen);

                Font       useFont = (isDi || isDen) ? fBold : fNormal;
                g2.setFont(useFont);
                FontMetrics fmu = g2.getFontMetrics(useFont);
                int tw = fmu.stringWidth(name);
                int ty = lp.y + fmu.getAscent() / 2;


                // Chữ
                g2.setColor(isDi  ? new Color(165, 25, 8)
                           : isDen ? new Color(15, 65, 155)
                           :         new Color(20, 50, 90));
                g2.drawString(name, lp.x, ty);
            }
            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.dispose();
        }
    }
}
