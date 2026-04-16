package GUI;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DatVeGUI extends JPanel {

    private JComboBox<String> cbGaDen;
    private JTextField txtGaDi;
    private JTextField txtNgayDi;
    private JTextField txtNgayVe;
    private JSpinner spinnerSoLuong;
    private JRadioButton rbMotChieu, rbKhuHoi;

    public DatVeGUI() {
        setLayout(new GridLayout(1, 2, 0, 0));
        setBackground(Color.WHITE);

        add(buildLeftPanel());
        add(buildRightPanel());
    }

    // =====================================================
    // PANEL TRÁI: ẢNH BẢN ĐỒ CHIẾM TOÀN BỘ
    // =====================================================
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(180, 210, 230));

        // JLabel này sẽ đóng vai trò là container chứa ảnh và các điểm ga
        JLabel mapLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Nạp ảnh thực tế từ thư mục Images
                try {
                    java.net.URL imgURL = getClass().getResource("/Images/BanDo.png");
                    if (imgURL != null) {
                        Image img = new ImageIcon(imgURL).getImage();
                        // Vẽ ảnh co giãn theo toàn bộ kích thước của Label (Tràn viền)
                        g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        // Nếu không tìm thấy ảnh, vẽ nền xám báo lỗi
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(Color.RED);
                        g2.drawString("LỖI: Không tìm thấy /Images/BanDo.png", 20, 30);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                g2.dispose();
            }
        };

        // Quan trọng: Layout null để đặt các chấm ga (Map Points) theo tọa độ x,y
        mapLabel.setLayout(null);

        // TODO: Tại đây bạn hãy add các điểm ga vào mapLabel
        // Ví dụ: mapLabel.add(createMapPoint("Diêu Trì", 305, 455));

        p.add(mapLabel, BorderLayout.CENTER);
        return p;
    }

    // =====================================================
    // PANEL PHẢI: FORM ĐẶT VÉ
    // =====================================================
    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(242, 247, 252));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(40, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 18, 0);

        // Tiêu đề
        JLabel title = new JLabel("Vui lòng điền/chọn thông tin vào đây");
        title.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 16));
        title.setForeground(new Color(60, 60, 60));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 28, 0);
        form.add(title, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 14, 0);

        // --- GA ĐI: disable, mặc định Diêu Trì ---
        txtGaDi = new JTextField("Diêu Trì");
        txtGaDi.setEditable(false);
        txtGaDi.setEnabled(false);
        txtGaDi.setBackground(new Color(220, 235, 248));
        txtGaDi.setForeground(new Color(50, 50, 50));
        txtGaDi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        addRow(form, gbc, 1, "Ga đi:", wrapField(txtGaDi));

        // --- GA ĐẾN: combobox sổ xuống ---
        cbGaDen = new JComboBox<>(new String[]{
            "Phủ Lý", "Nam Định", "Ninh Bình", "Thanh Hóa",
            "Vinh", "Hà Tĩnh", "Vũng Áng", "Đồng Hới",
            "Đông Hà", "Huế", "Đà Nẵng", "Tam Kỳ",
            "Quảng Ngãi", "Bồng Sơn", "Tuy Hòa",
            "Khánh Hòa", "Tháp Chàm", "Phan Rí",
            "Long Thành", "Thủ Thiêm", "Ngọc Hồi"
        });
        cbGaDen.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cbGaDen.setBackground(Color.WHITE);
        cbGaDen.setBorder(null);
        addRow(form, gbc, 2, "Ga đến:", wrapCombo(cbGaDen));

        // --- LOẠI VÉ: radio ---
        rbMotChieu = new JRadioButton("Một chiều", true);
        rbKhuHoi   = new JRadioButton("Khứ hồi");
        rbMotChieu.setOpaque(false);
        rbKhuHoi.setOpaque(false);
        rbMotChieu.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        rbKhuHoi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMotChieu); bg.add(rbKhuHoi);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(rbMotChieu);
        radioPanel.add(Box.createHorizontalStrut(16));
        radioPanel.add(rbKhuHoi);

        // Khi chọn một chiều: disable ngày về
        rbMotChieu.addActionListener(e -> setNgayVeEnabled(false));
        rbKhuHoi.addActionListener(e -> setNgayVeEnabled(true));

        addRow(form, gbc, 3, "Loại vé:", radioPanel);

        // --- NGÀY ĐI: date picker ---
        txtNgayDi = createDateField();
        JPanel ngayDiPanel = buildDatePickerPanel(txtNgayDi);
        addRow(form, gbc, 4, "Ngày đi:", ngayDiPanel);

        // --- NGÀY VỀ: date picker (disabled mặc định vì 1 chiều) ---
        txtNgayVe = createDateField();
        JPanel ngayVePanel = buildDatePickerPanel(txtNgayVe);
        setNgayVeEnabled(false);
        addRow(form, gbc, 5, "Ngày về:", ngayVePanel);

        // --- SỐ LƯỢNG: nút - / số / + ---
        JPanel soLuongPanel = buildSoLuongPanel();
        addRow(form, gbc, 6, "Số lượng:", soLuongPanel);

        // --- NÚT TÌM CHUYẾN ---
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(12, 0, 0, 0);
        JButton btnTim = buildTimButton();
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(btnTim);
        form.add(btnWrap, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    // =====================================================
    // HELPER: thêm 1 hàng label + component vào form
    // =====================================================
    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0.32;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(55, 55, 55));
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.68;
        p.add(comp, gbc);
    }

    // =====================================================
    // HELPER: bọc JTextField trong khung bo góc
    // =====================================================
    private JPanel wrapField(JTextField tf) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tf.isEnabled() ? Color.WHITE : new Color(220, 235, 248));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(5, 12, 5, 12));
        tf.setOpaque(false);
        tf.setBorder(null);
        tf.setPreferredSize(new Dimension(0, 26));
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    // =====================================================
    // HELPER: bọc JComboBox trong khung bo góc
    // =====================================================
    private JPanel wrapCombo(JComboBox<String> cb) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(3, 8, 3, 8));
        cb.setOpaque(false);
        cb.setBorder(null);
        p.add(cb, BorderLayout.CENTER);
        return p;
    }

    // =====================================================
    // DATE FIELD: ô nhập ngày + icon lịch → mở calendar popup
    // =====================================================
    private JTextField createDateField() {
        LocalDate today = LocalDate.now();
        JTextField tf = new JTextField(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tf.setEditable(false);
        tf.setBackground(Color.WHITE);
        return tf;
    }

    private JPanel buildDatePickerPanel(JTextField tf) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tf.isEnabled() ? Color.WHITE : new Color(225, 235, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(5, 12, 5, 10));

        tf.setOpaque(false);
        tf.setBorder(null);

        // Icon lịch (dùng ký tự Unicode làm placeholder)
        // TODO: thay bằng GuiIcons.loadIcon(..., "/Image/iconCalendar.png", 16, 16) khi có ảnh
        JLabel iconLich = new JLabel("📅");
        iconLich.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        iconLich.setCursor(new Cursor(Cursor.HAND_CURSOR));
        iconLich.setBorder(new EmptyBorder(0, 6, 0, 0));

        // Click icon → mở popup calendar
        MouseAdapter openCal = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (tf.isEnabled()) showCalendarPopup(tf, wrapper);
            }
        };
        iconLich.addMouseListener(openCal);
        tf.addMouseListener(openCal);

        wrapper.add(tf, BorderLayout.CENTER);
        wrapper.add(iconLich, BorderLayout.EAST);
        return wrapper;
    }

    // =====================================================
    // CALENDAR POPUP
    // =====================================================
    private void showCalendarPopup(JTextField target, JComponent anchor) {
        JDialog popup = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), false);
        popup.setUndecorated(true);
        popup.setBackground(Color.WHITE);

        JPanel cal = new JPanel(new BorderLayout(0, 4));
        cal.setBackground(Color.WHITE);
        cal.setBorder(BorderFactory.createLineBorder(new Color(180, 205, 230), 1));
        cal.setPreferredSize(new Dimension(240, 200));

        // Parse ngày hiện tại trong field
        String[] parts = target.getText().split("/");
        final int[] cur = {
            parts.length == 3 ? Integer.parseInt(parts[2]) : LocalDate.now().getYear(),
            parts.length == 3 ? Integer.parseInt(parts[1]) : LocalDate.now().getMonthValue(),
            parts.length == 3 ? Integer.parseInt(parts[0]) : LocalDate.now().getDayOfMonth()
        };
        // cur[0]=year, cur[1]=month, cur[2]=day

        JPanel[] dayGridHolder = {null};
        JLabel[] monthLabelHolder = {null};

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GuiTheme.NAVY);
        header.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel lblMonth = new JLabel();
        lblMonth.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblMonth.setForeground(Color.WHITE);
        lblMonth.setHorizontalAlignment(SwingConstants.CENTER);
        monthLabelHolder[0] = lblMonth;

        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        styleNavBtn(btnPrev);
        styleNavBtn(btnNext);

        header.add(btnPrev, BorderLayout.WEST);
        header.add(lblMonth, BorderLayout.CENTER);
        header.add(btnNext, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(4, 6, 6, 6));

        // Build day headers
        JPanel dayHeaders = new JPanel(new GridLayout(1, 7, 2, 0));
        dayHeaders.setBackground(Color.WHITE);
        String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        for (String d : days) {
            JLabel dl = new JLabel(d, SwingConstants.CENTER);
            dl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
            dl.setForeground(GuiTheme.SUB_TEXT);
            dayHeaders.add(dl);
        }

        body.add(dayHeaders, BorderLayout.NORTH);

        // Rebuild day grid
        Runnable rebuildGrid = () -> {
            if (dayGridHolder[0] != null) body.remove(dayGridHolder[0]);
            JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
            grid.setBackground(Color.WHITE);
            dayGridHolder[0] = grid;

            monthLabelHolder[0].setText(
                String.format("Tháng %d / %d", cur[1], cur[0])
            );

            Calendar c = Calendar.getInstance();
            c.set(cur[0], cur[1] - 1, 1);
            int firstDay = c.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
            int maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = 0; i < firstDay; i++) grid.add(new JLabel(""));
            for (int d = 1; d <= maxDay; d++) {
                final int day = d;
                JButton btn = new JButton(String.valueOf(d));
                btn.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.setOpaque(true);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                if (d == cur[2]) {
                    btn.setBackground(GuiTheme.NAVY);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(GuiTheme.TEXT);
                    btn.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) { btn.setBackground(GuiTheme.LIGHT_BG); }
                        public void mouseExited(MouseEvent e)  { btn.setBackground(Color.WHITE); }
                    });
                }
                btn.addActionListener(e -> {
                    cur[2] = day;
                    target.setText(String.format("%02d/%02d/%04d", day, cur[1], cur[0]));
                    popup.dispose();
                });
                grid.add(btn);
            }

            body.add(grid, BorderLayout.CENTER);
            body.revalidate();
            body.repaint();
        };

        btnPrev.addActionListener(e -> {
            cur[1]--;
            if (cur[1] < 1) { cur[1] = 12; cur[0]--; }
            rebuildGrid.run();
        });
        btnNext.addActionListener(e -> {
            cur[1]++;
            if (cur[1] > 12) { cur[1] = 1; cur[0]++; }
            rebuildGrid.run();
        });

        rebuildGrid.run();
        cal.add(header, BorderLayout.NORTH);
        cal.add(body, BorderLayout.CENTER);

        popup.add(cal);
        popup.pack();

        // Hiện popup ngay dưới ô nhập
        Point loc = anchor.getLocationOnScreen();
        popup.setLocation(loc.x, loc.y + anchor.getHeight());
        popup.setVisible(true);

        // Tự đóng khi click ra ngoài
        popup.addWindowFocusListener(new WindowAdapter() {
            public void windowLostFocus(WindowEvent e) { popup.dispose(); }
        });
    }

    private void styleNavBtn(JButton btn) {
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // =====================================================
    // SỐ LƯỢNG: nút [ - ] [ số ] [ + ]
    // =====================================================
    private JPanel buildSoLuongPanel() {
        final int[] count = {1};

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);

        JButton btnMinus = buildCountBtn("-");
        JLabel lblCount = new JLabel("  1  ", SwingConstants.CENTER);
        lblCount.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lblCount.setPreferredSize(new Dimension(36, 32));
        JButton btnPlus  = buildCountBtn("+");

        btnMinus.addActionListener(e -> {
            if (count[0] > 1) { count[0]--; lblCount.setText("  " + count[0] + "  "); }
        });
        btnPlus.addActionListener(e -> {
            if (count[0] < 99) { count[0]++; lblCount.setText("  " + count[0] + "  "); }
        });

        // Bọc trong khung bo góc
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(2, 4, 2, 4));
        wrapper.add(btnMinus);
        wrapper.add(lblCount);
        wrapper.add(btnPlus);

        p.add(wrapper);
        return p;
    }

    private JButton buildCountBtn(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? new Color(200, 218, 240)
                    : getModel().isRollover() ? new Color(220, 233, 248) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(100, 150, 190));
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label,
                    (getWidth() - fm.stringWidth(label)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =====================================================
    // NÚT TÌM CHUYẾN bo góc
    // =====================================================
    private JButton buildTimButton() {
        JButton btn = new JButton("Tìm chuyến") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? GuiTheme.NAVY_DARK
                    : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt,
                    (getWidth() - fm.stringWidth(txt)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =====================================================
    // BẬT/TẮT NGÀY VỀ
    // =====================================================
    private void setNgayVeEnabled(boolean enabled) {
        txtNgayVe.setEnabled(enabled);
        txtNgayVe.setForeground(enabled ? GuiTheme.TEXT : GuiTheme.SUB_TEXT);
        if (txtNgayVe.getParent() != null) txtNgayVe.getParent().repaint();
    }
}