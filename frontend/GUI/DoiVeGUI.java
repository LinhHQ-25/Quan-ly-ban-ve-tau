package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import de.wannawork.jcalendar.JCalendarComboBox;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

/**
 * DoiVeGUI – Bước 2 của flow đổi vé.
 * Flow: DoiTraGUI (chọn vé) → DoiVeGUI (xem vé + nhập thông tin đổi) → DoiVeGUI1 (xác nhận)
 *
 * Nghiệp vụ đổi vé:
 *   - Chỉ áp dụng vé cá nhân.
 *   - Phải trước giờ tàu ≥ 24h.
 *   - Phí cố định: 30.000đ/vé.
 *   - Vé nhóm: KHÔNG được đổi.
 */
public class DoiVeGUI extends JPanel {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final Color BORDER  = new Color(210, 215, 224);
    private static final Color WARN_FG = new Color(180, 60, 0);
    private static final Color OK_FG   = new Color(30, 120, 60);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat  SDF = new SimpleDateFormat("dd/MM/yyyy");

    // ── Dữ liệu vé được chọn từ DoiTraGUI ────────────────────────────────
    // data: [chuyenTau, gaDi, gaDen, loaiVe, ngayGioKH, soLuong, ghe, giaTien]
    private static String   s_maVe = "";
    private static String[] s_data = new String[0];

    /** DoiTraGUI gọi hàm này trước khi showCard("doi-ve") */
    public static void setVeDuocChon(String maVe, String[] data) {
        s_maVe = maVe;
        s_data = data.clone();
    }

    // ── State ─────────────────────────────────────────────────────────────
    private final AppFrame appFrame;

    // Form vé hiện tại (chỉ đọc)
    private JTextField tfMaVe, tfChuyen, tfGaDi, tfGaDen,
            tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia;

    // Form đổi sang (người dùng nhập/chọn)
    private JComboBox<String>  cbChuyenMoi, cbGheMoi;
    private JCalendarComboBox  calNgayMoi;      // ← JCalendar picker thay TextField
    private JTextField         tfLePhi;

    // Trạng thái & warning
    private JLabel   lbTrangThai, lbWarning;
    private JButton  btnXacNhan;

    // ── Constructor ───────────────────────────────────────────────────────
    public DoiVeGUI(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(GuiTheme.LIGHT_BG);

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(
                GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT,
                GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        page.add(buildHeader());
        page.add(Box.createVerticalStrut(14));
        page.add(buildCurrentInfoCard());
        page.add(Box.createVerticalStrut(10));
        page.add(buildChangeFormCard());
        page.add(Box.createVerticalStrut(14));
        page.add(buildButtonRow());

        JScrollPane outer = new JScrollPane(page);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);
        add(outer, BorderLayout.CENTER);

        refresh();
    }

    /**
     * Gọi sau setVeDuocChon() để nạp lại form.
     * AppFrame gọi refresh() khi showCard("doi-ve").
     */
    public void refresh() {
        fillCurrentInfo();
        validateDoiVe();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel title = new JLabel("ĐỔI VÉ TÀU");
        title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, GuiTheme.PAGE_TITLE_SIZE));
        title.setForeground(GuiTheme.TEXT);

        JLabel sub = new JLabel("Kiểm tra thông tin vé và nhập thông tin chuyến muốn đổi sang.");
        sub.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, GuiTheme.PAGE_SUBTITLE_SIZE));
        sub.setForeground(GuiTheme.SUB_TEXT);

        p.add(title, BorderLayout.NORTH);
        p.add(sub,   BorderLayout.SOUTH);
        setStretch(p);
        return p;
    }

    /** Card trên: thông tin vé hiện tại — chỉ đọc */
    private JPanel buildCurrentInfoCard() {
        JPanel card = buildCard("Thông tin vé hiện tại");

        tfMaVe    = readField(); tfChuyen  = readField();
        tfGaDi    = readField(); tfGaDen   = readField();
        tfNgayGio = readField(); tfLoai    = readField();
        tfGhe     = readField(); tfSoLuong = readField();
        tfGia     = readField();

        JPanel grid = new JPanel(new GridLayout(3, 4, 12, 10));
        grid.setOpaque(false);

        // Hàng 1
        grid.add(fieldBox("Mã vé",       tfMaVe));
        grid.add(fieldBox("Chuyến tàu",  tfChuyen));
        grid.add(fieldBox("Ga đi",       tfGaDi));
        grid.add(fieldBox("Ga đến",      tfGaDen));
        // Hàng 2
        grid.add(fieldBox("Ngày/Giờ KH", tfNgayGio));
        grid.add(fieldBox("Loại vé",     tfLoai));
        grid.add(fieldBox("Số ghế",      tfGhe));
        grid.add(fieldBox("Số lượng",    tfSoLuong));
        // Hàng 3
        grid.add(fieldBox("Đơn giá",     tfGia));

        // Ô điều kiện đổi
        lbTrangThai = new JLabel("—");
        lbTrangThai.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbTrangThai.setForeground(GuiTheme.SUB_TEXT);
        JPanel ttCell = new JPanel(new BorderLayout(0, 4));
        ttCell.setOpaque(false);
        JLabel ttLabel = new JLabel("Điều kiện đổi");
        ttLabel.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        ttLabel.setForeground(GuiTheme.SUB_TEXT);
        ttCell.add(ttLabel,     BorderLayout.NORTH);
        ttCell.add(lbTrangThai, BorderLayout.CENTER);
        grid.add(ttCell);

        grid.add(new JLabel());
        grid.add(new JLabel());

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /** Card dưới: thông tin muốn đổi sang — người dùng nhập */
    private JPanel buildChangeFormCard() {
        JPanel card = buildCard("Thông tin đổi sang");

        // ComboBox chuyến mới
        cbChuyenMoi = new JComboBox<>(new String[]{"SE1","SE3","SE5","SE7","SE9","SE19","TN2","TN4"});
        cbChuyenMoi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        cbChuyenMoi.setBackground(Color.WHITE);

        // JCalendarComboBox – chọn ngày mới
        calNgayMoi = buildDatePicker();

        // ComboBox ghế mới
        cbGheMoi = new JComboBox<>(new String[]{
                "A01","A02","A03","A04","A05",
                "B01","B02","B03","B04","B05",
                "C01","C02","C03","D01","D02"
        });
        cbGheMoi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        cbGheMoi.setBackground(Color.WHITE);

        // Lệ phí — chỉ đọc
        tfLePhi = readField();
        tfLePhi.setText("30.000 đ  (cố định / vé)");
        tfLePhi.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));

        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        grid.add(fieldBox("Chuyến mới",   cbChuyenMoi));
        grid.add(fieldBox("Ngày mới",     calNgayMoi));
        grid.add(fieldBox("Ghế mới",      cbGheMoi));
        grid.add(fieldBox("Lệ phí đổi",  tfLePhi));

        // Warning label
        lbWarning = new JLabel(" ");
        lbWarning.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbWarning.setForeground(WARN_FG);
        lbWarning.setBorder(new EmptyBorder(6, 0, 0, 0));

        card.add(grid,      BorderLayout.CENTER);
        card.add(lbWarning, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        setStretch(p);

        JButton btnBack = secondaryBtn("← Quay lại", 130, 34);
        btnBack.addActionListener(e -> appFrame.showCard("doi-tra"));

        btnXacNhan = navyBtn("Tiếp tục →", 140, 34);
        btnXacNhan.setEnabled(false);
        btnXacNhan.addActionListener(e -> handleDoiVe());

        p.add(btnBack);
        p.add(btnXacNhan);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOGIC
    // ══════════════════════════════════════════════════════════════════════

    /** Đổ dữ liệu từ s_data vào các field chỉ đọc */
    private void fillCurrentInfo() {
        if (s_data.length < 8) { clearCurrentInfo(); return; }

        tfMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        tfChuyen .setText(s_data[0]);
        tfGaDi   .setText(s_data[1]);
        tfGaDen  .setText(s_data[2]);
        tfLoai   .setText(s_data[3]);
        tfNgayGio.setText(s_data[4]);
        tfSoLuong.setText(s_data[5]);
        tfGhe    .setText(s_data[6]);
        try {
            long gia = Long.parseLong(s_data[7]);
            tfGia.setText(String.format("%,d đ", gia).replace(",", "."));
        } catch (Exception e) { tfGia.setText(s_data[7]); }

        cbChuyenMoi.setSelectedItem(s_data[0]);
    }

    /** Validate nghiệp vụ và cập nhật trạng thái + nút */
    private void validateDoiVe() {
        if (s_data.length < 8 || s_maVe.isEmpty()) {
            lbTrangThai.setText("Chưa có vé được chọn");
            lbTrangThai.setForeground(GuiTheme.SUB_TEXT);
            lbWarning.setText(" ");
            btnXacNhan.setEnabled(false);
            return;
        }

        boolean nhom      = s_data[3].toLowerCase().contains("nhóm");
        long    gioConLai = tinhGio(s_data[4]);

        if (nhom) {
            lbTrangThai.setText("Vé nhóm — Không được đổi");
            lbTrangThai.setForeground(new Color(180, 30, 30));
            lbWarning.setText("Vé nhóm không được phép đổi theo quy định.");
            lbWarning.setForeground(WARN_FG);
            btnXacNhan.setEnabled(false);
        } else if (gioConLai < 24) {
            lbTrangThai.setText("Quá hạn — còn " + Math.max(gioConLai, 0) + "h (cần ≥ 24h)");
            lbTrangThai.setForeground(new Color(180, 30, 30));
            lbWarning.setText("Quá hạn đổi vé. Yêu cầu phải thực hiện trước giờ tàu ít nhất 24 giờ.");
            lbWarning.setForeground(WARN_FG);
            btnXacNhan.setEnabled(false);
        } else {
            lbTrangThai.setText("Hợp lệ — còn " + gioConLai + " giờ");
            lbTrangThai.setForeground(OK_FG);
            lbWarning.setText(" ");
            btnXacNhan.setEnabled(true);
        }
    }

    private void clearCurrentInfo() {
        for (JTextField tf : new JTextField[]{tfMaVe, tfChuyen, tfGaDi, tfGaDen,
                tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia})
            if (tf != null) tf.setText("—");
    }

    /** Xử lý khi nhấn "Tiếp tục" — validate ngày mới rồi chuyển sang DoiVeGUI1 */
    private void handleDoiVe() {
        // Lấy ngày từ JCalendarComboBox và format sang dd/MM/yyyy
        Calendar cal = calNgayMoi.getCalendar();
        if (cal == null) { showWarn("Vui lòng chọn Ngày chuyến mới."); return; }

        // Ngày mới phải sau hôm nay
        if (!cal.after(Calendar.getInstance())) {
            showWarn("Ngày mới phải sau thời điểm hiện tại.");
            return;
        }

        // Ghép thành chuỗi dd/MM/yyyy HH:mm (giờ mặc định 00:00 vì picker chỉ có ngày)
        String ngayText = SDF.format(cal.getTime()) + " 00:00";

        DoiVeGUI1.setDonDoi(
                s_maVe,
                s_data,
                (String) cbChuyenMoi.getSelectedItem(),
                ngayText,
                (String) cbGheMoi.getSelectedItem()
        );
        appFrame.showCard("doi-ve-step-2");
    }

    private void showWarn(String msg) {
        lbWarning.setText("  " + msg);
        lbWarning.setForeground(WARN_FG);
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS – Nghiệp vụ
    // ══════════════════════════════════════════════════════════════════════

    private static long tinhGio(String ngayGio) {
        try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(ngayGio, FMT)); }
        catch (Exception e) { return -1; }
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS – UI
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Tạo JCalendarComboBox để chọn ngày mới.
     * Dùng lib JCalendar đã cài sẵn.
     */
    private JCalendarComboBox buildDatePicker() {
        JCalendarComboBox chooser = new JCalendarComboBox(
                Calendar.getInstance(),
                new Locale("vi", "VN"),
                new SimpleDateFormat("dd/MM/yyyy"));
        chooser.setPreferredSize(new Dimension(160, 28));
        chooser.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        chooser.setBorder(new LineBorder(BORDER, 1, true));
        chooser.setDateFormat(new SimpleDateFormat("dd/MM/yyyy"));
        chooser.setBackground(GuiTheme.SEARCH_FIELD_BG);
        styleCalendarChooser(chooser);
        return chooser;
    }

    /** Áp style cho các component con bên trong JCalendarComboBox */
    private void styleCalendarChooser(JCalendarComboBox chooser) {
        chooser.setOpaque(false);
        for (Component c : chooser.getComponents()) {
            if (c instanceof JTextField tf) {
                tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                tf.setForeground(GuiTheme.TEXT);
                tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
                tf.setBorder(new EmptyBorder(2, 6, 2, 6));
            }
            if (c instanceof AbstractButton btn) {
                btn.setBackground(GuiTheme.SEARCH_FIELD_BG);
            }
        }
    }

    private JTextField readField() {
        JTextField tf = new JTextField("—");
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tf.setEditable(false);
        tf.setForeground(GuiTheme.TEXT);
        tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(2, 6, 2, 6)));
        return tf;
    }

    private JPanel fieldBox(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lb = new JLabel(label);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(GuiTheme.SUB_TEXT);
        p.add(lb,   BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCard(String titleText) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel lbTitle = new JLabel(titleText);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbTitle.setForeground(GuiTheme.TEXT);
        lbTitle.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(lbTitle, BorderLayout.NORTH);
        return card;
    }

    private JButton navyBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled()           ? new Color(180, 190, 205)
                        : getModel().isPressed() ? GuiTheme.NAVY_DARK
                          : getModel().isRollover()? GuiTheme.NAVY_HOVER
                            :                          GuiTheme.NAVY;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton secondaryBtn(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? new Color(220, 225, 235)
                        : getModel().isRollover()? new Color(235, 239, 246)
                          :                          new Color(240, 243, 248);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(GuiTheme.TEXT);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static void setStretch(JPanel p) {
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 16));
    }
}