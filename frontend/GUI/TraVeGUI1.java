package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * TraVeGUI1 – Màn hình xác nhận kết quả trả vé.
 * Flow: TraVeGUI → TraVeGUI1
 *
 * Hiển thị lại toàn bộ thông tin vé vừa trả + tiền hoàn.
 * Dữ liệu được truyền từ TraVeGUI qua setDonTra().
 * AppFrame gọi refresh() mỗi khi showCard("tra-ve-step-2").
 */
public class TraVeGUI1 extends JPanel {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final Color BORDER     = new Color(210, 215, 224);
    private static final Color OK_FG      = new Color(30, 120, 60);
    private static final Color OK_BG      = new Color(236, 252, 240);
    private static final Color OK_BORDER  = new Color(160, 215, 175);

    // ── Dữ liệu tĩnh được TraVeGUI set trước khi chuyển màn ──────────────
    private static String   s_maVe    = "";
    private static String[] s_data    = new String[0];
    private static String   s_phi     = "";
    private static String   s_hoanLai = "";

    /** TraVeGUI gọi hàm này trước khi showCard("tra-ve-step-2") */
    public static void setDonTra(String maVe, String[] data, String phi, String hoanLai) {
        s_maVe    = maVe;
        s_data    = data.clone();
        s_phi     = phi;
        s_hoanLai = hoanLai;
    }

    // ── State ──────────────────────────────────────────────────────────────
    private final AppFrame appFrame;

    // Labels thông tin vé (cập nhật qua refresh)
    private JLabel valMaVe, valChuyen, valGaDi, valGaDen, valNgayGio, valLoai, valSoLuong;
    // Labels chi phí (cập nhật qua refresh)
    private JLabel valTongTien, valPhi, valHoanLai;

    // ── Constructor ────────────────────────────────────────────────────────
    public TraVeGUI1(AppFrame appFrame) {
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
        page.add(Box.createVerticalStrut(12));
        page.add(buildSuccessBox());
        page.add(Box.createVerticalStrut(12));
        page.add(buildInfoCard());
        page.add(Box.createVerticalStrut(12));
        page.add(buildFeeCard());
        page.add(Box.createVerticalStrut(16));
        page.add(buildButtonRow());

        JScrollPane outer = new JScrollPane(page);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);
        add(outer, BorderLayout.CENTER);
    }

    /**
     * AppFrame gọi hàm này mỗi khi showCard("tra-ve-step-2").
     * Nạp lại tất cả dữ liệu từ TraVeGUI vào các label hiển thị.
     */
    public void refresh() {
        // Thông tin vé
        valMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        valChuyen .setText(safe(s_data, 0));
        valGaDi   .setText(safe(s_data, 1));
        valGaDen  .setText(safe(s_data, 2));
        valNgayGio.setText(safe(s_data, 4));
        valLoai   .setText(safe(s_data, 3));
        valSoLuong.setText(safe(s_data, 5));

        // Chi phí
        long tongTien = 0;
        try {
            tongTien = Long.parseLong(s_data[7]) * Long.parseLong(s_data[5]);
        } catch (Exception ignored) {}
        valTongTien.setText(tongTien > 0
                ? String.format("%,d VNĐ", tongTien).replace(",", ".") : "—");
        valPhi    .setText(s_phi.isEmpty()     ? "—" : s_phi);
        valHoanLai.setText(s_hoanLai.isEmpty() ? "—" : s_hoanLai);

        revalidate();
        repaint();
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("XÁC NHẬN TRẢ VÉ");
        title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, GuiTheme.PAGE_TITLE_SIZE));
        title.setForeground(GuiTheme.TEXT);

        JLabel sub = new JLabel("Vui lòng kiểm tra lại thông tin trước khi hoàn tất.");
        sub.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, GuiTheme.PAGE_SUBTITLE_SIZE));
        sub.setForeground(GuiTheme.SUB_TEXT);

        p.add(title, BorderLayout.NORTH);
        p.add(sub,   BorderLayout.SOUTH);
        fixAlignAndMax(p);
        return p;
    }

    /** Ô thông báo xanh */
    private JPanel buildSuccessBox() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(OK_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(OK_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel icon = new JLabel("ℹ");
        icon.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(OK_FG);

        JLabel msg = new JLabel("Vui lòng kiểm tra lại thông tin. Sau khi hoàn tất, yêu cầu sẽ được xử lý trong 3–5 ngày làm việc.");
        msg.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        msg.setForeground(OK_FG);

        p.add(icon);
        p.add(msg);
        fixAlignAndMax(p);
        return p;
    }

    /** Card thông tin chi tiết vé — dùng JLabel có thể cập nhật */
    private JPanel buildInfoCard() {
        JPanel card = buildCard("Thông tin vé cần trả");

        // Khởi tạo labels
        valMaVe    = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);
        valChuyen  = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);
        valGaDi    = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);
        valGaDen   = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);
        valNgayGio = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);
        valLoai    = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);
        valSoLuong = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);

        String[] labels = {"Mã vé", "Chuyến tàu", "Ga đi", "Ga đến", "Ngày/Giờ KH", "Loại vé", "Số lượng"};
        JLabel[] values = {valMaVe, valChuyen, valGaDi, valGaDen, valNgayGio, valLoai, valSoLuong};

        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 10));
        grid.setOpaque(false);

        for (int i = 0; i < labels.length; i++) {
            grid.add(infoCell(labels[i], values[i]));
        }
        grid.add(new JLabel()); // ô cuối trống

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /** Card phí & tiền hoàn — dùng JLabel có thể cập nhật */
    private JPanel buildFeeCard() {
        JPanel card = buildCard("Chi phí hoàn trả");

        valTongTien = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);
        valPhi      = infoValueLabel(GuiTheme.TEXT, Font.PLAIN);
        valHoanLai  = infoValueLabel(new Color(30, 120, 60), Font.BOLD);

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);
        grid.add(infoCell("Tổng tiền vé",  valTongTien));
        grid.add(infoCell("Phí trả vé",    valPhi));
        grid.add(infoCell("Tiền hoàn lại", valHoanLai));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        fixAlignAndMax(p);

        JButton btnBack = makeSecondaryButton("← Quay lại", 130, 34);
        btnBack.addActionListener(e -> appFrame.showCard("tra-ve"));

        JButton btnDone = makeNavyButton("Xác nhận trả vé", 150, 34);
        btnDone.addActionListener(e -> handleDone());

        p.add(btnBack);
        p.add(btnDone);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOGIC
    // ══════════════════════════════════════════════════════════════════════

    private void handleDone() {
        int choice = JOptionPane.showConfirmDialog(this,
                "<html><div style='padding:6px'>" +
                        "<b>Xác nhận trả vé " + s_maVe + "?</b><br><br>" +
                        "Phí trả: <b>" + s_phi + "</b><br>" +
                        "Tiền hoàn lại: <b>" + s_hoanLai + "</b><br>" +
                        "Tiền sẽ được hoàn trong 3–5 ngày làm việc." +
                        "</div></html>",
                "Xác nhận trả vé",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) return;

        JOptionPane.showMessageDialog(this,
                "<html><div style='text-align:center;padding:8px'>" +
                        "<b style='font-size:16px;color:#1e7840'>Trả vé thành công!</b><br><br>" +
                        "Mã vé <b>" + s_maVe + "</b> đã được xử lý trả.<br>" +
                        "Tiền hoàn <b>" + s_hoanLai + "</b> sẽ được chuyển trong 3–5 ngày làm việc." +
                        "</div></html>",
                "Hoàn tất", JOptionPane.PLAIN_MESSAGE);

        appFrame.showCard("doi-tra");
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS – UI
    // ══════════════════════════════════════════════════════════════════════

    /** Tạo JLabel giá trị có border (style infoCell) có thể cập nhật sau */
    private JLabel infoValueLabel(Color textColor, int fontStyle) {
        JLabel lb = new JLabel("—");
        lb.setFont(GuiTheme.font("Segoe UI", fontStyle, 13));
        lb.setForeground(textColor);
        lb.setBackground(GuiTheme.SEARCH_FIELD_BG);
        lb.setOpaque(true);
        lb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return lb;
    }

    /** Panel label trên / JLabel value dưới */
    private JPanel infoCell(String label, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel lb = new JLabel(label);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(GuiTheme.SUB_TEXT);

        p.add(lb,         BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCard(String titleText) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel lbTitle = new JLabel(titleText);
        lbTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lbTitle.setForeground(GuiTheme.TEXT);
        lbTitle.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(lbTitle, BorderLayout.NORTH);
        return card;
    }

    private JButton makeNavyButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? GuiTheme.NAVY_DARK
                        : getModel().isRollover() ? GuiTheme.NAVY_HOVER
                          : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt,
                        (getWidth()  - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
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

    private JButton makeSecondaryButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? new Color(220, 225, 235)
                        : getModel().isRollover() ? new Color(235, 239, 246)
                          : new Color(240, 243, 248);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(GuiTheme.TEXT);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt,
                        (getWidth()  - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
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

    private static void fixAlignAndMax(JPanel p) {
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 20));
    }

    /** Lấy giá trị an toàn từ mảng, trả "—" nếu out-of-bounds */
    private static String safe(String[] arr, int idx) {
        return (arr != null && idx < arr.length && arr[idx] != null) ? arr[idx] : "—";
    }
}