package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.toedter.calendar.JDateChooser;
import connect_DB.Connect_DB;

public class DoiVeGUI extends JPanel {
    private static final Color BORDER      = new Color(210, 215, 224);
    private static final Color WARN_FG     = new Color(180, 60, 0);
    private static final Color OK_FG       = new Color(30, 120, 60);
    private static final Color NAVY        = GuiTheme.NAVY;
    private static final int   FIELD_H     = 32;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static String   s_maVe = "";
    private static String[] s_data = new String[0];

    public static void setVeDuocChon(String maVe, String[] data) {
        s_maVe = maVe;
        s_data = data.clone();
    }

    private final AppFrame appFrame;

    // Các trường thông tin vé cũ
    private JTextField tfGaDi, tfGaDen, tfLoai, tfNgayGio, tfNgayVe, tfSoLuong, tfGia;
    private JLabel lbMaVeCu, lbTrangThai, lbWarning;

    // Các trường chọn thông tin vé mới
    private JComboBox<String> cbGaDi, cbGaDen, cbLoaiVe, cbSoLuong;
    private JDateChooser dcNgayDi, dcNgayVe;
    private JButton btnTiepTuc;

    public DoiVeGUI(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);

        // SỬA: Giảm khoảng cách dọc từ 10 xuống còn 4
        JPanel pnlPage = new JPanel(new BorderLayout(0, 4));
        pnlPage.setOpaque(false);
        // SỬA: Thay GuiTheme.PAGE_PAD_BOTTOM bằng 0 để bỏ khoảng đệm dưới đáy
        pnlPage.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, 0, GuiTheme.PAGE_PAD_LEFT));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        stack.add(buildOldTicketCard());
        stack.add(Box.createVerticalStrut(15));
        stack.add(buildNewTicketCard());

        JPanel alignTop = new JPanel(new BorderLayout());
        alignTop.setOpaque(false);
        alignTop.add(stack, BorderLayout.NORTH);

        JScrollPane outer = new JScrollPane(alignTop);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);

        pnlPage.add(outer, BorderLayout.CENTER);
        pnlPage.add(buildBottomBar(), BorderLayout.SOUTH);
        add(pnlPage, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        fillCurrentInfo();
        validateDoiVe();
        updateBottomBar();
    }

    private JPanel buildOldTicketCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel headerPane = new JPanel(new BorderLayout());
        headerPane.setOpaque(false);
        headerPane.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lbTitle = new JLabel("Thông tin vé cũ");
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbTitle.setForeground(GuiTheme.TEXT);

        lbMaVeCu = new JLabel("Mã vé: —");
        lbMaVeCu.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbMaVeCu.setForeground(NAVY);

        headerPane.add(lbTitle, BorderLayout.WEST);
        headerPane.add(lbMaVeCu, BorderLayout.EAST);
        card.add(headerPane, BorderLayout.NORTH);

        tfGaDi    = readField(); tfGaDen   = readField();
        tfLoai    = readField(); tfNgayGio = readField();
        tfNgayVe  = readField(); tfSoLuong = readField();

        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 15));
        grid.setOpaque(false);
        grid.add(disabledFieldBox("Ga đi", tfGaDi));
        grid.add(disabledFieldBox("Ga đến", tfGaDen));
        grid.add(disabledFieldBox("Loại vé", tfLoai));
        grid.add(disabledFieldBox("Ngày đi", tfNgayGio));
        grid.add(disabledFieldBox("Ngày về", tfNgayVe));
        grid.add(disabledFieldBox("Số lượng", tfSoLuong));

        lbTrangThai = new JLabel("—");
        lbTrangThai.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbTrangThai.setForeground(GuiTheme.SUB_TEXT);

        JPanel ttCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ttCell.setOpaque(false);
        JLabel ttLbl = new JLabel("Điều kiện đổi: ");
        ttLbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        ttLbl.setForeground(GuiTheme.SUB_TEXT);
        ttCell.add(ttLbl);
        ttCell.add(lbTrangThai);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(grid);
        content.add(Box.createVerticalStrut(15));
        content.add(ttCell);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildNewTicketCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(16, 20, 16, 20)));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lb = new JLabel("Chọn thông tin vé mới");
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lb.setForeground(GuiTheme.TEXT);
        lb.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(lb, BorderLayout.NORTH);

        String[] dsGa = loadDanhSachGa();
        String[] dsLoaiVe = {"Một chiều", "Khứ hồi"};
        String[] dsSoLuong = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

        cbGaDi    = createComboBox(dsGa);
        cbGaDen   = createComboBox(dsGa);
        cbLoaiVe  = createComboBox(dsLoaiVe);
        dcNgayDi  = buildDateChooser(true);
        dcNgayVe  = buildDateChooser(false);
        cbSoLuong = createComboBox(dsSoLuong);

        // Vô hiệu hóa các trường bắt buộc phải giữ nguyên như vé cũ
        cbGaDi.setEnabled(false);
        cbLoaiVe.setEnabled(false);
        cbSoLuong.setEnabled(false);

        // Cập nhật giao diện cho giống disabled field
        cbGaDi.setBackground(new Color(245, 247, 250));
        cbLoaiVe.setBackground(new Color(245, 247, 250));
        cbSoLuong.setBackground(new Color(245, 247, 250));

        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 15));
        grid.setOpaque(false);
        grid.add(newFieldBox("Ga đi", cbGaDi));
        grid.add(newFieldBox("Ga đến", cbGaDen));
        grid.add(newFieldBox("Loại vé", cbLoaiVe));
        grid.add(newFieldBox("Ngày đi", dcNgayDi));
        grid.add(newFieldBox("Ngày về", dcNgayVe));
        grid.add(newFieldBox("Số lượng", cbSoLuong));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);

        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(12, 15, 12, 15)));

        lbWarning = new JLabel(" ");
        lbWarning.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbWarning.setForeground(WARN_FG);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        left.setBackground(Color.WHITE);
        left.add(lbWarning);

        JButton btnBack = makeOutlineBtn("Quay lại", 130, 38);
        btnBack.addActionListener(e -> appFrame.showCard("doi-tra"));

        btnTiepTuc = makeNavyBtn("Tiếp tục", 130, 38);
        btnTiepTuc.setIcon(
                GuiIcons.loadIcon(
                        DoiTraGUI.class,
                        "/Images/logoGoOn.png",
                        16,
                        16));
        btnTiepTuc.setEnabled(false);
        btnTiepTuc.addActionListener(e -> handleTiepTuc());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4));
        right.setBackground(Color.WHITE);
        right.add(btnBack);
        right.add(btnTiepTuc);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void updateBottomBar() {
        if (btnTiepTuc == null) return;
        boolean valid = lbTrangThai != null && lbTrangThai.getText().startsWith("Hợp lệ");
        btnTiepTuc.setEnabled(valid);
        if (lbWarning != null) {
            if (!valid && lbTrangThai != null && !lbTrangThai.getText().startsWith("Hợp lệ")) {
                lbWarning.setText("Vé cũ không đủ điều kiện đổi");
            } else {
                lbWarning.setText("Vé hợp lệ. Vui lòng chọn thông tin chuyến mới.");
                lbWarning.setForeground(OK_FG);
            }
        }
    }

    private void fillCurrentInfo() {
        if (s_data.length < 9) { clearFields(); return; }

        lbMaVeCu.setText("Mã vé: " + (s_maVe.isEmpty() ? "—" : s_maVe));
        tfGaDi   .setText(s_data[1]);
        tfGaDen  .setText(s_data[2]);
        String _rawLoai = s_data[3];
        tfLoai   .setText("MOT_CHIEU".equalsIgnoreCase(_rawLoai) ? "Một chiều"
                : "KHU_HOI".equalsIgnoreCase(_rawLoai)   ? "Khứ hồi"
                  : _rawLoai);
        tfSoLuong.setText(s_data[6]);

        // Auto select the new fields
        cbGaDi.setSelectedItem(s_data[1]);
        cbGaDen.setSelectedItem(s_data[2]);
        // Chuẩn hóa loaiVe: MOT_CHIEU -> Một chiều, KHU_HOI -> Khứ hồi
        String loaiVeChuan = "MOT_CHIEU".equalsIgnoreCase(s_data[3]) ? "Một chiều"
                : "KHU_HOI".equalsIgnoreCase(s_data[3])   ? "Khứ hồi"
                  : s_data[3];
        cbLoaiVe.setSelectedItem(loaiVeChuan);
        cbSoLuong.setSelectedItem(s_data[6]);

        boolean isKhuHoi = "KHU_HOI".equalsIgnoreCase(s_data[3]) || "Khứ hồi".equalsIgnoreCase(s_data[3]);
        boolean isChieuVe = "Chiều về".equals(s_data[4]);
        if (isChieuVe) {
            tfNgayGio.setText("—");
            tfNgayVe .setText(s_data[5]);
        } else {
            tfNgayGio.setText(s_data[5]);
            tfNgayVe .setText("—");
        }
        boolean ngayDiEnabled = !isChieuVe;
        boolean ngayVeEnabled = isKhuHoi && isChieuVe;

        dcNgayDi.setEnabled(ngayDiEnabled);
        dcNgayDi.setBackground(ngayDiEnabled ? Color.WHITE : new Color(245, 247, 250));
        dcNgayVe.setEnabled(ngayVeEnabled);
        dcNgayVe.setBackground(ngayVeEnabled ? Color.WHITE : new Color(245, 247, 250));

        java.util.Date today = new java.util.Date(); // Lấy ngày hiện tại của hệ thống

        dcNgayDi.setMinSelectableDate(today);
        if (isKhuHoi) {
            dcNgayVe.setMinSelectableDate(today);
        }

        dcNgayDi.setDate(null);
        dcNgayVe.setDate(null);
    }

    private void validateDoiVe() {
        if (lbTrangThai == null) return;
        if (s_data.length < 9 || s_maVe.isEmpty()) {
            lbTrangThai.setText("Chưa có vé được chọn");
            lbTrangThai.setForeground(GuiTheme.SUB_TEXT); return;
        }

        // Nếu số lượng > 1 thì là vé nhóm
        boolean nhom = Integer.parseInt(s_data[6].replaceAll("[^0-9]", "")) > 1;
        long gioTong = tinhGio(s_data[5]);
        long gioThuc = Math.max(gioTong, 0);
        long d = gioThuc / 24; long h = gioThuc % 24;
        String timeStr = (d > 0) ? (d + " ngày" + (h > 0 ? " " + h + " giờ" : "")) : (h + " giờ");

        if (nhom) {
            lbTrangThai.setText("Vé nhóm — Không được đổi");
            lbTrangThai.setForeground(new Color(180, 30, 30));
        } else if (gioTong < 24) {
            lbTrangThai.setText("Quá hạn — còn " + timeStr + " (cần ≥ 24h)");
            lbTrangThai.setForeground(new Color(180, 30, 30));
        } else {
            lbTrangThai.setText("Hợp lệ — còn " + timeStr);
            lbTrangThai.setForeground(OK_FG);
        }
    }

    private void handleTiepTuc() {
        String gaDiMoi = cbGaDi.getSelectedItem().toString();
        String gaDenMoi = cbGaDen.getSelectedItem().toString();
        String loaiVeMoi = cbLoaiVe.getSelectedItem().toString();
        int soLuongMoi = Integer.parseInt(cbSoLuong.getSelectedItem().toString());
        boolean isChieuVe = "Chiều về".equals(s_data.length > 4 ? s_data[4] : "");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String ngayDiMoi  = (!isChieuVe && dcNgayDi.getDate() != null) ? sdf.format(dcNgayDi.getDate()) : "";
        String ngayVeMoi  = (isChieuVe  && dcNgayVe.getDate() != null) ? sdf.format(dcNgayVe.getDate()) : "";

        if (gaDiMoi.equals(gaDenMoi)) {
            JOptionPane.showMessageDialog(this, "Ga đi và Ga đến không được trùng nhau!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isChieuVe && ngayDiMoi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày đi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isChieuVe && ngayVeMoi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày về!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DoiVeGUI0.setTieuChiMoi(s_maVe, s_data, gaDiMoi, gaDenMoi, loaiVeMoi, ngayDiMoi, ngayVeMoi, soLuongMoi);
        appFrame.showCard("doi-ve-step-1");
    }

    private void clearFields() {
        if (lbMaVeCu != null) lbMaVeCu.setText("Mã vé: —");
        for (JTextField tf : new JTextField[]{tfGaDi, tfGaDen, tfLoai, tfNgayGio, tfNgayVe, tfSoLuong}) {
            if (tf != null) tf.setText("—");
        }
    }

    private static long tinhGio(String s) {
        try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(s, FMT)); }
        catch (Exception e) { return -1; }
    }

    // --- LOAD TỪ DB ---
    private String[] loadDanhSachGa() {
        List<String> listGa = new ArrayList<>();
        String sql = "SELECT tenGa FROM Ga ORDER BY tenGa ASC";
        try (Connection conn = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) listGa.add(rs.getString("tenGa"));
        } catch (SQLException e) { e.printStackTrace(); }
        if (listGa.isEmpty()) return new String[]{"Sài Gòn", "Nha Trang", "Đà Nẵng", "Huế", "Hà Nội"};
        return listGa.toArray(new String[0]);
    }

    // --- UI HELPERS ---

    private JDateChooser buildDateChooser(boolean enabled) {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setDate(null);
        dc.setEnabled(enabled);
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(enabled ? Color.WHITE : new Color(245, 247, 250));
        dc.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false), new EmptyBorder(2, 4, 2, 4)));

        Component editor = dc.getDateEditor().getUiComponent();
        if (editor instanceof JComponent) ((JComponent) editor).setBorder(null);
        return dc;
    }

    private JTextField readField() {
        JTextField tf = new JTextField("—");
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tf.setEditable(false);
        tf.setForeground(new Color(120, 120, 120));
        tf.setBackground(new Color(245, 247, 250));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false), new EmptyBorder(4, 8, 4, 8)));
        tf.setPreferredSize(new Dimension(0, FIELD_H));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return tf;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cb.setBackground(Color.WHITE); cb.setForeground(GuiTheme.TEXT);
        cb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false), new EmptyBorder(2, 4, 2, 4)));
        cb.setPreferredSize(new Dimension(0, FIELD_H));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return cb;
    }

    private JPanel disabledFieldBox(String label, JTextField tf) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false);
        JLabel lb = new JLabel(label); lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lb.setForeground(GuiTheme.SUB_TEXT); lb.setAlignmentX(LEFT_ALIGNMENT);
        tf.setAlignmentX(LEFT_ALIGNMENT); tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        p.add(lb); p.add(Box.createVerticalStrut(6)); p.add(tf); return p;
    }

    private JPanel newFieldBox(String label, JComponent comp) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false);
        JLabel lb = new JLabel(label); lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lb.setForeground(GuiTheme.NAVY); lb.setAlignmentX(LEFT_ALIGNMENT);
        comp.setAlignmentX(LEFT_ALIGNMENT); comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        p.add(lb); p.add(Box.createVerticalStrut(6)); p.add(comp); return p;
    }

    private JButton makeOutlineBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(198,215,242) : getModel().isRollover() ? new Color(212,228,250) : new Color(226,236,252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(NAVY); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics(); String txt = getText();
                g2.drawString(txt, (getWidth()-fm.stringWidth(txt))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w, h)); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeNavyBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(
                        getModel().isPressed()
                                ? GuiTheme.NAVY_DARK
                                : getModel().isRollover()
                                  ? GuiTheme.NAVY_HOVER
                                  : GuiTheme.NAVY);

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // ===== DRAW ICON + TEXT =====
                Font font = GuiTheme.font("Segoe UI", Font.BOLD, 14);
                g2.setFont(font);
                g2.setColor(Color.WHITE);

                FontMetrics fm = g2.getFontMetrics();

                Icon icon = getIcon();

                int iconTextGap = 8;

                int textWidth = fm.stringWidth(getText());
                int iconWidth = (icon != null) ? icon.getIconWidth() : 0;

                int totalWidth = textWidth +
                        (icon != null ? iconWidth + iconTextGap : 0);

                int startX = (getWidth() - totalWidth) / 2;

                // Draw icon
                if (icon != null) {
                    int iconY = (getHeight() - icon.getIconHeight()) / 2;

                    icon.paintIcon(
                            this,
                            g2,
                            startX,
                            iconY
                    );

                    startX += iconWidth + iconTextGap;
                }

                // Draw text
                int textY = (getHeight()
                        + fm.getAscent()
                        - fm.getDescent()) / 2;

                g2.drawString(getText(), startX, textY);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(w, h));

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }
}