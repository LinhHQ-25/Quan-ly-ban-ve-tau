package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * DoiVeGUI – Bước 2 của flow đổi vé.
 * Flow: DoiTraGUI → DoiVeGUI (xem vé + chọn chuyến/toa/ghế mới) → DoiVeGUI1 (xác nhận)
 *
 * THAY ĐỔI SO VỚI CŨ:
 *  - Card thông tin vé: fix textbox không phình dọc (BoxLayout + setMaximumSize)
 *  - Phần chọn chuyến: thiết kế dạng TABLE (danh sách hàng ngang) thay vì card tàu
 *    → khác hẳn DatVeGUI1 đang dùng card hình tàu lửa.
 *  - Phần chọn toa: dạng dropdown/stepper thay vì scroll ngang icon toa.
 *  - Phần chọn ghế: giữ nguyên grid ghế nhưng bọc trong card trắng riêng.
 */
public class DoiVeGUI extends JPanel {

    // ── Colors ──────────────────────────────────────────────────────────────
    private static final Color BORDER      = new Color(210, 215, 224);
    private static final Color WARN_FG     = new Color(180, 60, 0);
    private static final Color OK_FG       = new Color(30, 120, 60);
    private static final Color BG          = GuiTheme.LIGHT_BG;
    private static final Color NAVY        = new Color(28, 57, 110);
    private static final Color ACCENT      = new Color(0, 120, 215);
    private static final Color ACCENT_LITE = new Color(224, 240, 255);
    private static final Color SEAT_OK     = new Color(28, 57, 110);
    private static final Color SEAT_TAKEN  = new Color(180, 190, 210);
    private static final Color SEAT_SEL    = new Color(100, 180, 255);
    private static final int   FIELD_H     = 28;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Dữ liệu vé nhận từ DoiTraGUI ────────────────────────────────────────
    private static String   s_maVe = "";
    private static String[] s_data = new String[0];

    public static void setVeDuocChon(String maVe, String[] data) {
        s_maVe = maVe;
        s_data = data.clone();
    }

    // ── Dữ liệu chuyến tàu mẫu ──────────────────────────────────────────────
    private static final String[][] CHUYEN = {
            {"SE8",  "28/04/2026 06:00", "29/04/2026 16:10"},
            {"SE6",  "28/04/2026 08:40", "29/04/2026 19:12"},
            {"SE10", "28/04/2026 13:20", "30/04/2026 04:25"},
            {"TN1",  "28/04/2026 15:25", "30/04/2026 04:55"},
            {"SE4",  "28/04/2026 20:35", "30/04/2026 05:45"},
            {"SE2",  "28/04/2026 20:55", "30/04/2026 10:25"},
            {"SE12", "28/04/2026 22:55", "30/04/2026 12:05"},
            {"SE5",  "29/04/2026 05:30", "30/04/2026 15:40"},
            {"TN3",  "29/04/2026 11:00", "30/04/2026 21:10"},
    };
    private static final int[] TOA_COUNT = {11, 11, 10, 12, 13, 10, 12, 11, 12};

    // ── State ────────────────────────────────────────────────────────────────
    private final AppFrame appFrame;
    private int chuyenIdx = 0;
    private int toaIdx    = 0;
    private final Set<Integer> gheChon = new LinkedHashSet<>();

    // ── Widgets — card vé hiện tại ───────────────────────────────────────────
    private JTextField tfMaVe, tfChuyen, tfGaDi, tfGaDen,
            tfNgayGio, tfLoai, tfGhe, tfSoLuong, tfGia;
    private JLabel lbTrangThai;

    // ── Widgets — chọn chuyến mới (TABLE) ───────────────────────────────────
    private JPanel      pnlChuyenTable;   // body bảng chuyến
    private JScrollPane chuyenScroll;

    // ── Widgets — chọn toa (STEPPER) ────────────────────────────────────────
    private JLabel  lblToaHienTai;
    private JButton btnToaPrev, btnToaNext;
    private JLabel  lblToaTrong;

    // ── Widgets — ghế ────────────────────────────────────────────────────────
    private JPanel pnlGhe;
    private JLabel lblGheDaChon;

    // ── Bottom bar ───────────────────────────────────────────────────────────
    private JLabel  lbWarning;
    private JButton btnTiepTuc;

    // ── Constructor ──────────────────────────────────────────────────────────
    public DoiVeGUI(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(
                GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT,
                GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));
        page.add(buildCurrentInfoCard());
        page.add(Box.createVerticalStrut(10));
        page.add(buildChuyenCard());       // ← TABLE chuyến (khác DatVeGUI1)
        page.add(Box.createVerticalStrut(8));
        page.add(buildToaStepperCard());   // ← STEPPER toa (khác DatVeGUI1)
        page.add(Box.createVerticalStrut(8));
        page.add(buildGheCard());          // ← grid ghế trong card trắng
        page.add(Box.createVerticalStrut(10));
        page.add(buildBottomBar());

        JScrollPane outer = new JScrollPane(page);
        outer.setBorder(null);
        outer.getViewport().setOpaque(false);
        outer.setOpaque(false);
        add(outer, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        fillCurrentInfo();
        validateDoiVe();
        gheChon.clear();
        chuyenIdx = 0; toaIdx = 0;
        refreshChuyenTable();
        refreshToaStepper();
        refreshGhe();
        updateBottomBar();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARD THÔNG TIN VÉ HIỆN TẠI (fix phình dọc)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildCurrentInfoCard() {
        JPanel card = buildWhiteCard("Thông tin vé hiện tại");

        tfMaVe    = readField(); tfChuyen  = readField();
        tfGaDi    = readField(); tfGaDen   = readField();
        tfNgayGio = readField(); tfLoai    = readField();
        tfGhe     = readField(); tfSoLuong = readField();
        tfGia     = readField();

        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 8));
        grid.setOpaque(false);
        grid.add(fieldBox("Mã vé",       tfMaVe));
        grid.add(fieldBox("Chuyến tàu",  tfChuyen));
        grid.add(fieldBox("Ga đi",       tfGaDi));
        grid.add(fieldBox("Ga đến",      tfGaDen));
        grid.add(fieldBox("Ngày/Giờ KH", tfNgayGio));
        grid.add(fieldBox("Loại vé",     tfLoai));
        grid.add(fieldBox("Số ghế",      tfGhe));
        grid.add(fieldBox("Số lượng",    tfSoLuong));

        JPanel row3 = new JPanel(new GridLayout(1, 4, 12, 0));
        row3.setOpaque(false);
        row3.add(fieldBox("Đơn giá", tfGia));

        lbTrangThai = new JLabel("—");
        lbTrangThai.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbTrangThai.setForeground(GuiTheme.SUB_TEXT);

        JPanel ttCell = new JPanel();
        ttCell.setLayout(new BoxLayout(ttCell, BoxLayout.Y_AXIS));
        ttCell.setOpaque(false);
        JLabel ttLbl = new JLabel("Điều kiện đổi");
        ttLbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        ttLbl.setForeground(GuiTheme.SUB_TEXT);
        ttLbl.setAlignmentX(LEFT_ALIGNMENT);
        lbTrangThai.setAlignmentX(LEFT_ALIGNMENT);
        ttCell.add(ttLbl);
        ttCell.add(Box.createVerticalStrut(4));
        ttCell.add(lbTrangThai);

        row3.add(ttCell);
        row3.add(new JLabel()); row3.add(new JLabel());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(grid);
        content.add(Box.createVerticalStrut(8));
        content.add(row3);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARD CHỌN CHUYẾN — dạng TABLE (khác DatVeGUI1 dùng card hình tàu)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildChuyenCard() {
        JPanel card = buildWhiteCard("Chọn chuyến tàu mới");

        // Header bảng
        JPanel header = new JPanel(new GridLayout(1, 4, 0, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(6, 10, 6, 10));
        for (String col : new String[]{"Tàu", "Giờ khởi hành", "Giờ đến", "Thời gian"}) {
            JLabel l = new JLabel(col);
            l.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            l.setForeground(Color.WHITE);
            header.add(l);
        }

        // Body bảng — các hàng chuyến
        pnlChuyenTable = new JPanel();
        pnlChuyenTable.setLayout(new BoxLayout(pnlChuyenTable, BoxLayout.Y_AXIS));
        pnlChuyenTable.setBackground(Color.WHITE);

        chuyenScroll = new JScrollPane(pnlChuyenTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chuyenScroll.setBorder(null);
        chuyenScroll.setPreferredSize(new Dimension(0, 220));
        chuyenScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(new LineBorder(BORDER, 1, false));
        tableWrap.add(header,       BorderLayout.NORTH);
        tableWrap.add(chuyenScroll, BorderLayout.CENTER);

        card.add(tableWrap, BorderLayout.CENTER);
        return card;
    }

    private void refreshChuyenTable() {
        if (pnlChuyenTable == null) return;
        pnlChuyenTable.removeAll();
        for (int i = 0; i < CHUYEN.length; i++) {
            pnlChuyenTable.add(buildChuyenRow(i));
        }
        pnlChuyenTable.revalidate();
        pnlChuyenTable.repaint();
    }

    /** Một hàng trong bảng chuyến */
    private JPanel buildChuyenRow(int ci) {
        boolean sel = (ci == chuyenIdx);
        String[] ch = CHUYEN[ci];

        // Tính thời gian hành trình
        String tgHanhTrinh = "--";
        try {
            LocalDateTime di   = LocalDateTime.parse(ch[1], FMT);
            LocalDateTime den  = LocalDateTime.parse(ch[2], FMT);
            long mins = ChronoUnit.MINUTES.between(di, den);
            tgHanhTrinh = (mins / 60) + "h " + (mins % 60) + "m";
        } catch (Exception ignored) {}

        final String tg = tgHanhTrinh;
        final int idx  = ci;

        JPanel row = new JPanel(new GridLayout(1, 4, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(sel ? ACCENT_LITE : (ci % 2 == 0 ? Color.WHITE : new Color(249, 251, 254)));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(8, 10, 8, 10)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Cột 1: badge tên tàu
        JPanel badgeCol = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeCol.setOpaque(false);
        JLabel badge = new JLabel(ch[0]);
        badge.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(sel ? Color.WHITE : NAVY);
        badge.setOpaque(true);
        badge.setBackground(sel ? ACCENT : ACCENT_LITE);
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        badgeCol.add(badge);

        // Cột 2, 3, 4: thông tin
        JLabel lGioDi  = rowLabel(ch[1], sel);
        JLabel lGioDen = rowLabel(ch[2], sel);
        JLabel lTG     = rowLabel(tg,    sel);
        lTG.setForeground(sel ? ACCENT : GuiTheme.SUB_TEXT);

        row.add(badgeCol);
        row.add(lGioDi);
        row.add(lGioDen);
        row.add(lTG);

        // Click chọn chuyến
        MouseAdapter ma = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                chuyenIdx = idx; toaIdx = 0; gheChon.clear();
                refreshChuyenTable();
                refreshToaStepper();
                refreshGhe();
                updateBottomBar();
            }
        };
        row.addMouseListener(ma);
        for (Component c : row.getComponents()) c.addMouseListener(ma);

        return row;
    }

    private JLabel rowLabel(String text, boolean sel) {
        JLabel l = new JLabel(text);
        l.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(sel ? NAVY : GuiTheme.TEXT);
        return l;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARD CHỌN TOA — dạng STEPPER (khác DatVeGUI1 dùng scroll icon toa)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildToaStepperCard() {
        JPanel card = buildWhiteCard("Chọn toa");

        // Stepper: [ ‹ ]  Toa SE801 (3/11)  [ › ]
        JPanel stepper = new JPanel();
        stepper.setLayout(new BoxLayout(stepper, BoxLayout.X_AXIS));
        stepper.setOpaque(false);
        stepper.setAlignmentX(LEFT_ALIGNMENT);

        btnToaPrev = makeArrowBtn("‹");
        btnToaNext = makeArrowBtn("›");
        btnToaPrev.addActionListener(e -> {
            if (toaIdx > 0) { toaIdx--; gheChon.clear(); refreshToaStepper(); refreshGhe(); updateBottomBar(); }
        });
        btnToaNext.addActionListener(e -> {
            if (toaIdx < TOA_COUNT[chuyenIdx] - 1) { toaIdx++; gheChon.clear(); refreshToaStepper(); refreshGhe(); updateBottomBar(); }
        });

        lblToaHienTai = new JLabel("—");
        lblToaHienTai.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lblToaHienTai.setForeground(NAVY);
        lblToaHienTai.setHorizontalAlignment(SwingConstants.CENTER);
        lblToaHienTai.setBorder(new EmptyBorder(0, 16, 0, 16));

        lblToaTrong = new JLabel("—");
        lblToaTrong.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lblToaTrong.setForeground(GuiTheme.SUB_TEXT);
        lblToaTrong.setBorder(new EmptyBorder(0, 12, 0, 0));

        stepper.add(btnToaPrev);
        stepper.add(lblToaHienTai);
        stepper.add(btnToaNext);
        stepper.add(Box.createHorizontalStrut(20));
        stepper.add(lblToaTrong);

        // Ghi chú màu
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        legend.setOpaque(false);
        legend.add(makeLegend(SEAT_SEL,   "Đang chọn"));
        legend.add(makeLegend(SEAT_OK,    "Còn trống"));
        legend.add(makeLegend(SEAT_TAKEN, "Đã đặt"));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(stepper, BorderLayout.WEST);
        top.add(legend,  BorderLayout.EAST);

        card.add(top, BorderLayout.CENTER);
        return card;
    }

    private void refreshToaStepper() {
        if (lblToaHienTai == null) return;
        int n       = TOA_COUNT[chuyenIdx];
        String maToa = CHUYEN[chuyenIdx][0] + String.format("%02d", toaIdx + 1);
        lblToaHienTai.setText("Toa  " + maToa + "  (" + (toaIdx + 1) + "/" + n + ")");
        btnToaPrev.setEnabled(toaIdx > 0);
        btnToaNext.setEnabled(toaIdx < n - 1);

        Set<Integer> daDat = gheDaDat(chuyenIdx, toaIdx);
        int trong = 0;
        for (int g = 1; g <= 28; g++) if (!daDat.contains(g)) trong++;
        lblToaTrong.setText("Còn " + trong + "/28 ghế trống");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CARD GHẾ — grid trong card trắng riêng
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildGheCard() {
        JPanel card = buildWhiteCard("Chọn ghế");

        pnlGhe = new JPanel();
        pnlGhe.setOpaque(false);

        // Label số ghế đã chọn bên trong card
        lblGheDaChon = new JLabel("Đã chọn: 0/" + getSoLuong() + " ghế");
        lblGheDaChon.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lblGheDaChon.setForeground(GuiTheme.SUB_TEXT);
        lblGheDaChon.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setOpaque(false);
        content.add(pnlGhe,       BorderLayout.CENTER);
        content.add(lblGheDaChon, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void refreshGhe() {
        if (pnlGhe == null) return;
        Set<Integer> daDat = gheDaDat(chuyenIdx, toaIdx);
        int soLuongChon = getSoLuong();

        pnlGhe.removeAll();
        pnlGhe.setLayout(new GridLayout(2, 14, 4, 6));
        pnlGhe.setBorder(new EmptyBorder(4, 4, 4, 4));

        for (int i = 1; i <= 28; i++) {
            final int gNum  = i;
            final boolean taken = daDat.contains(i);
            JButton btn = new JButton(String.valueOf(i)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = taken ? SEAT_TAKEN : gheChon.contains(gNum) ? SEAT_SEL : SEAT_OK;
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(Color.WHITE);
                    g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    String t = getText();
                    g2.drawString(t, (getWidth()-fm.stringWidth(t))/2,
                            (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            btn.setPreferredSize(new Dimension(32, 28));
            btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
            if (!taken) {
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.addActionListener(ev -> {
                    if (gheChon.contains(gNum)) gheChon.remove(gNum);
                    else if (gheChon.size() < soLuongChon) gheChon.add(gNum);
                    refreshGhe();
                    updateBottomBar();
                });
            }
            pnlGhe.add(btn);
        }
        pnlGhe.revalidate(); pnlGhe.repaint();
        updateBottomBar();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BOTTOM BAR
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(6, 0, 6, 0)
        ));
        bar.setAlignmentX(LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JButton btnBack = makeOutlineBtn("← Quay lại");
        btnBack.addActionListener(e -> appFrame.showCard("doi-tra"));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(Color.WHITE);
        left.add(btnBack);

        lbWarning = new JLabel(" ");
        lbWarning.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lbWarning.setForeground(WARN_FG);

        btnTiepTuc = makeNavyBtn("Tiếp tục →", 140, 32);
        btnTiepTuc.setEnabled(false);
        btnTiepTuc.addActionListener(e -> handleTiepTuc());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.WHITE);
        right.add(lbWarning);
        right.add(btnTiepTuc);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void updateBottomBar() {
        if (btnTiepTuc == null) return;
        int need = getSoLuong();
        if (lblGheDaChon != null)
            lblGheDaChon.setText("Đã chọn: " + gheChon.size() + "/" + need + " ghế");
        boolean valid = gheChon.size() >= need
                && lbTrangThai != null
                && lbTrangThai.getText().startsWith("✔");
        btnTiepTuc.setEnabled(valid);
        if (lbWarning != null) {
            if (!valid && lbTrangThai != null && !lbTrangThai.getText().startsWith("✔")) {
                lbWarning.setText("Vé không đủ điều kiện đổi");
            } else if (!valid) {
                lbWarning.setText("Chọn đủ " + need + " ghế để tiếp tục");
            } else {
                lbWarning.setText(" ");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LOGIC
    // ══════════════════════════════════════════════════════════════════════════
    private void fillCurrentInfo() {
        if (s_data.length < 8) { clearFields(); return; }
        tfMaVe   .setText(s_maVe.isEmpty() ? "—" : s_maVe);
        tfChuyen .setText(s_data[0]);
        tfGaDi   .setText(s_data[1]);
        tfGaDen  .setText(s_data[2]);
        tfLoai   .setText(s_data[3]);
        tfNgayGio.setText(s_data[4]);
        tfSoLuong.setText(s_data[5]);
        tfGhe    .setText(s_data[6]);
        try { tfGia.setText(String.format("%,d đ", Long.parseLong(s_data[7])).replace(",",".")); }
        catch (Exception e) { tfGia.setText(s_data[7]); }
    }

    private void validateDoiVe() {
        if (lbTrangThai == null) return;
        if (s_data.length < 8 || s_maVe.isEmpty()) {
            lbTrangThai.setText("Chưa có vé được chọn");
            lbTrangThai.setForeground(GuiTheme.SUB_TEXT);
            return;
        }
        boolean nhom = s_data[3].toLowerCase().contains("nhóm");
        long    gio  = tinhGio(s_data[4]);
        if (nhom) {
            lbTrangThai.setText("✘  Vé nhóm — Không được đổi");
            lbTrangThai.setForeground(new Color(180, 30, 30));
        } else if (gio < 24) {
            lbTrangThai.setText("✘  Quá hạn — còn " + Math.max(gio,0) + "h (cần ≥ 24h)");
            lbTrangThai.setForeground(new Color(180, 30, 30));
        } else {
            lbTrangThai.setText("✔  Hợp lệ — còn " + gio + " giờ");
            lbTrangThai.setForeground(OK_FG);
        }
    }

    private void handleTiepTuc() {
        if (gheChon.isEmpty()) return;
        String[] ch     = CHUYEN[chuyenIdx];
        String   tenToa = ch[0] + String.format("%02d", toaIdx + 1);
        StringBuilder gheSb = new StringBuilder();
        for (int g : gheChon) {
            if (gheSb.length() > 0) gheSb.append(", ");
            gheSb.append(tenToa).append("-G").append(String.format("%02d", g));
        }
        DoiVeGUI1.setDonDoi(s_maVe, s_data, ch[0], ch[1], gheSb.toString());
        appFrame.showCard("doi-ve-step-2");
    }

    private void clearFields() {
        for (JTextField tf : new JTextField[]{tfMaVe,tfChuyen,tfGaDi,tfGaDen,
                tfNgayGio,tfLoai,tfGhe,tfSoLuong,tfGia})
            if (tf != null) tf.setText("—");
    }

    private int getSoLuong() {
        if (s_data.length > 5) {
            try { return Integer.parseInt(s_data[5].trim()); } catch (Exception ignored) {}
        }
        return 1;
    }

    private static long tinhGio(String s) {
        try { return ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.parse(s, FMT)); }
        catch (Exception e) { return -1; }
    }

    private static Set<Integer> gheDaDat(int ci, int ti) {
        Random rnd = new Random((long)ci * 100 + ti);
        Set<Integer> s = new LinkedHashSet<>();
        for (int g = 1; g <= 28; g++) if (rnd.nextDouble() < 0.3) s.add(g);
        return s;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** FIX: set cả preferredSize lẫn maximumSize để chặn phình dọc */
    private JTextField readField() {
        JTextField tf = new JTextField("—");
        tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tf.setEditable(false); tf.setForeground(GuiTheme.TEXT);
        tf.setBackground(GuiTheme.SEARCH_FIELD_BG);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false), new EmptyBorder(2, 6, 2, 6)));
        tf.setPreferredSize(new Dimension(0, FIELD_H));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        return tf;
    }

    /** FIX: Dùng BoxLayout Y thay vì BorderLayout để không giãn dọc */
    private JPanel fieldBox(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lb = new JLabel(label);
        lb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(GuiTheme.SUB_TEXT);
        lb.setAlignmentX(LEFT_ALIGNMENT);
        comp.setAlignmentX(LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_H));
        p.add(lb);
        p.add(Box.createVerticalStrut(4));
        p.add(comp);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return p;
    }

    private JPanel buildWhiteCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel lb = new JLabel(title);
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lb.setForeground(GuiTheme.TEXT);
        lb.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(lb, BorderLayout.NORTH);
        return card;
    }

    /** Nút mũi tên cho stepper toa */
    private JButton makeArrowBtn(String symbol) {
        JButton b = new JButton(symbol) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? ACCENT_LITE : new Color(238, 240, 246));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isEnabled() ? ACCENT : new Color(175, 185, 205));
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(symbol,
                        (getWidth()-fm.stringWidth(symbol))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(32, 32));
        b.setMaximumSize(new Dimension(32, 32));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel makeLegend(Color c, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.dispose();
            }
        };
        box.setPreferredSize(new Dimension(14, 14)); box.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(GuiTheme.TEXT);
        p.add(box); p.add(l);
        return p;
    }

    private JButton makeOutlineBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(198,215,242)
                        : getModel().isRollover()  ? new Color(212,228,250) : new Color(226,236,252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(NAVY); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13)); g2.setColor(NAVY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(110, 30));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeNavyBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled()           ? new Color(180,190,205)
                        : getModel().isPressed() ? GuiTheme.NAVY_DARK
                          : getModel().isRollover()? GuiTheme.NAVY_HOVER : GuiTheme.NAVY;
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w, h));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void stretch(JPanel p) {
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 16));
    }
}