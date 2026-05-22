package gui;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;
import entity.LoaiKhachHang;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KhuyenMaiGUI extends JPanel {

    private static final Color NAVY       = GuiTheme.NAVY;
    private static final Color LIGHT_BG   = GuiTheme.LIGHT_BG;
    static final         Color BORDER_C   = new Color(210, 215, 224);
    private static final Color RED_DEL    = new Color(220, 53, 69);
    private static final Color TAG_ALL_BG = new Color(228, 234, 255);
    private static final Color TAG_ALL_FG = new Color(60, 80, 180);
    private static final Color TAG_ACT_BG = new Color(220, 252, 231);
    private static final Color TAG_ACT_FG = new Color(22, 130, 60);
    private static final Color TAG_OFF_BG = new Color(255, 237, 213);
    private static final Color TAG_OFF_FG = new Color(180, 80, 0);
    private static final Color TAG_SAP_BG = new Color(219, 234, 254);
    private static final Color TAG_SAP_FG = new Color(37, 99, 235);
    private static final int   BTN_H      = 38;

    static final String[]        LOAI_KH_VIET = {
            "Tất cả", "Trẻ em (<6 tuổi)", "Trẻ em (6-10 tuổi)",
            "Người cao tuổi", "Sinh viên", "Người lớn"
    };
    static final LoaiKhachHang[] LOAI_KH_ENUM = {
            null, LoaiKhachHang.DUOI_6_TUOI, LoaiKhachHang.TU_6_TOI_DUOI_10,
            LoaiKhachHang.TU_60_TRO_LEN, LoaiKhachHang.SINH_VIEN, LoaiKhachHang.NGUOI_LON
    };

    static String loaiKHToViet(LoaiKhachHang loai) {
        if (loai == null) return "Tất cả";
        for (int i = 1; i < LOAI_KH_ENUM.length; i++)
            if (LOAI_KH_ENUM[i] == loai) return LOAI_KH_VIET[i];
        return loai.name();
    }

    static LoaiKhachHang vietToLoaiKH(String viet) {
        for (int i = 0; i < LOAI_KH_VIET.length; i++)
            if (LOAI_KH_VIET[i].equals(viet)) return LOAI_KH_ENUM[i];
        return null;
    }

    static String computeStatus(LocalDateTime bd, LocalDateTime kt) {
        LocalDate today = LocalDate.now();
        LocalDate start = bd != null ? bd.toLocalDate() : null;
        LocalDate end   = kt != null ? kt.toLocalDate() : null;
        if (start == null && end == null) return "Ngừng áp dụng";
        if (start != null && end != null) {
            if (!today.isBefore(start) && !today.isAfter(end)) return "Đang áp dụng";
            if (today.isAfter(end)) return "Ngừng áp dụng";
            return "Sắp áp dụng";
        }
        if (start != null) return !today.isBefore(start) ? "Đang áp dụng" : "Sắp áp dụng";
        return !today.isAfter(end) ? "Đang áp dụng" : "Ngừng áp dụng";
    }

    private final KhuyenMaiDAO      dao          = new KhuyenMaiDAO();
    private       String            filterStatus = "ALL";
    private       String            searchText   = "";
    private       JTable            table;
    private       DefaultTableModel tableModel;
    private       JTextField        tfSearch;
    private final List<Object[]>    allRows      = new ArrayList<>();
    private       JPopupMenu        popupMenu;
    private       JMenuItem         menuSua, menuXoa;
    private       int               popupRow     = -1;

    public KhuyenMaiGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        initPopupMenu();

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(buildTableArea(), BorderLayout.CENTER);
        centerWrap.add(buildBottomBar(), BorderLayout.SOUTH);

        add(buildTopBar(),  BorderLayout.NORTH);
        add(centerWrap,     BorderLayout.CENTER);
        loadData();
    }

    private void initPopupMenu() {
        popupMenu = new JPopupMenu();
        menuSua   = new JMenuItem("Sửa thông tin");
        menuXoa   = new JMenuItem("Xóa khuyến mãi");
        menuSua.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        menuXoa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        menuXoa.setForeground(RED_DEL);
        menuSua.addActionListener(e -> { if (popupRow >= 0) openFormSua(popupRow); });
        menuXoa.addActionListener(e -> { if (popupRow >= 0) xoaKhuyenMai(popupRow); });
        popupMenu.add(menuSua);
        popupMenu.add(menuXoa);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel row = new JPanel(new BorderLayout(0, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tagPanel.setOpaque(false);
        tagPanel.add(makeFilterTag("Tất cả",        "ALL",      TAG_ALL_BG, TAG_ALL_FG));
        tagPanel.add(makeFilterTag("Đang áp dụng",  "ACTIVE",   TAG_ACT_BG, TAG_ACT_FG));
        tagPanel.add(makeFilterTag("Sắp áp dụng",   "UPCOMING", TAG_SAP_BG, TAG_SAP_FG));
        tagPanel.add(makeFilterTag("Ngừng áp dụng", "INACTIVE", TAG_OFF_BG, TAG_OFF_FG));

        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchRight.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lblSearch.setForeground(GuiTheme.SUB_TEXT);

        tfSearch = new JTextField(18);
        tfSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tfSearch.setBorder(null);
        tfSearch.setOpaque(false);
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
        });

        JPanel searchWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.setColor(BORDER_C);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(220, BTN_H));
        searchWrapper.setBorder(new EmptyBorder(4, 12, 4, 12));
        searchWrapper.add(tfSearch, BorderLayout.CENTER);

        searchRight.add(lblSearch);
        searchRight.add(searchWrapper);
        row.add(tagPanel,    BorderLayout.WEST);
        row.add(searchRight, BorderLayout.EAST);
        top.add(row);
        return top;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        bar.setOpaque(false);
        JButton btnThem = new JButton("+ Thêm khuyến mãi") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
                        : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btnThem.setPreferredSize(new Dimension(170, 42));
        btnThem.setContentAreaFilled(false); btnThem.setBorderPainted(false);
        btnThem.setFocusPainted(false); btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThem.addActionListener(e -> openFormThem());
        bar.add(btnThem);
        return bar;
    }

    private JPanel buildTableArea() {
        String[] cols = {"Mã KM", "Tên khuyến mãi", "Tỉ lệ giảm", "Loại KH",
                "Bắt đầu", "Kết thúc", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) c.setBackground(new Color(219, 234, 254));
                else c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                c.setForeground(GuiTheme.TEXT);
                if (c instanceof JLabel) ((JLabel) c).setBorder(new EmptyBorder(6, 12, 6, 12));
                return c;
            }
        };
        table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 244, 250));
        header.setForeground(GuiTheme.TEXT);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
        header.setReorderingAllowed(false);

        table.getColumnModel().getColumn(6).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            String txt = val == null ? "" : val.toString();
            Color bg, fg;
            switch (txt) {
                case "Đang áp dụng": bg = TAG_ACT_BG; fg = TAG_ACT_FG; break;
                case "Sắp áp dụng":  bg = TAG_SAP_BG; fg = TAG_SAP_FG; break;
                default:              bg = TAG_OFF_BG; fg = TAG_OFF_FG; break;
            }
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(fg); g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                            (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(5, 8, 5, 8));
            lbl.setOpaque(false);
            return lbl;
        });

        table.getColumnModel().getColumn(7).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel("...", SwingConstants.CENTER);
            lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
            lbl.setForeground(new Color(100, 110, 130));
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lbl.setOpaque(true);
            lbl.setBackground(tbl.isRowSelected(row) ? new Color(219,234,254)
                    : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return lbl;
        });

        int[] widths = {70, 200, 90, 160, 110, 110, 130, 60};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 7) {
                    popupRow = r;
                    table.setRowSelectionInterval(r, r);
                    Rectangle cell = table.getCellRect(r, c, true);
                    popupMenu.show(table, cell.x + cell.width/2, cell.y + cell.height/2);
                } else if (e.getClickCount() == 2 && r >= 0) {
                    openFormSua(r);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C, 1, false), BorderFactory.createEmptyBorder()));
        sp.getViewport().setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadData() {
        allRows.clear();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (KhuyenMai km : dao.selectAll()) {
            // Hiển thị tỉ lệ dạng số nguyên + % (ví dụ: "20%")
            int pct    = (int) Math.round(km.getTiLeGiamGia() * 100);
            String tileStr = pct + "%";
            String loaiKH  = loaiKHToViet(km.getLoaiKhachHang());
            String bdStr   = km.getThoiGianBatDau()  != null ? km.getThoiGianBatDau().toLocalDate().format(sdf)  : "";
            String ktStr   = km.getThoiGianKetThuc() != null ? km.getThoiGianKetThuc().toLocalDate().format(sdf) : "";
            String ttStr   = computeStatus(km.getThoiGianBatDau(), km.getThoiGianKetThuc());
            allRows.add(new Object[]{ km.getMaKhuyenMai(), km.getTenKhuyenMai(), tileStr, loaiKH, bdStr, ktStr, ttStr, "" });
        }
        applySearch();
    }

    private void applySearch() {
        searchText = tfSearch == null ? "" : tfSearch.getText().trim().toLowerCase();
        renderPage();
    }

    private List<Object[]> getFilteredRows() {
        List<Object[]> result = new ArrayList<>();
        for (Object[] row : allRows) {
            String tt = row[6].toString();
            if ("ACTIVE".equals(filterStatus)   && !"Đang áp dụng".equals(tt)) continue;
            if ("UPCOMING".equals(filterStatus) && !"Sắp áp dụng".equals(tt))  continue;
            if ("INACTIVE".equals(filterStatus) && !"Ngừng áp dụng".equals(tt)) continue;
            if (!searchText.isEmpty()) {
                boolean match = false;
                for (int i = 0; i < row.length-1; i++)
                    if (row[i] != null && row[i].toString().toLowerCase().contains(searchText)) { match = true; break; }
                if (!match) continue;
            }
            result.add(row);
        }
        return result;
    }

    private void renderPage() {
        tableModel.setRowCount(0);
        for (Object[] row : getFilteredRows()) tableModel.addRow(row);
    }

    private void openFormThem() {
        KhuyenMaiFormDialog d = new KhuyenMaiFormDialog(getParentFrame(), null);
        d.setVisible(true);
        if (d.isConfirmed()) loadData();
    }

    private void openFormSua(int tableRow) {
        String maKM = tableModel.getValueAt(tableRow, 0).toString();
        KhuyenMaiFormDialog d = new KhuyenMaiFormDialog(getParentFrame(), maKM);
        d.setVisible(true);
        if (d.isConfirmed()) loadData();
    }

    private void xoaKhuyenMai(int row) {
        String maKM  = tableModel.getValueAt(row, 0).toString();
        String tenKM = tableModel.getValueAt(row, 1).toString();
        int ch = JOptionPane.showConfirmDialog(this,
                "Xóa khuyến mãi " + tenKM + " (" + maKM + ")?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ch == JOptionPane.YES_OPTION) {
            if (dao.delete(maKM)) { JOptionPane.showMessageDialog(this, "Xóa thành công!"); loadData(); }
            else JOptionPane.showMessageDialog(this, "Lỗi khi xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Frame getParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return (w instanceof Frame) ? (Frame) w : null;
    }

    private JButton makeFilterTag(String label, String status, Color bg, Color fg) {
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = filterStatus.equals(status);
                Color activeFg = status.equals("ACTIVE")   ? TAG_ACT_FG
                        : status.equals("UPCOMING") ? TAG_SAP_FG
                        : status.equals("INACTIVE") ? TAG_OFF_FG : TAG_ALL_FG;
                g2.setColor(active ? activeFg : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(active ? Color.WHITE : fg);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(new JLabel(label).getPreferredSize().width + 32, 32));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            filterStatus = status; renderPage();
            Container parent = b.getParent(); if (parent != null) parent.repaint();
        });
        return b;
    }

    // =========================================================
    // INNER CLASS: Dialog Thêm / Sửa
    // =========================================================
    static class KhuyenMaiFormDialog extends JDialog {
        private boolean confirmed = false;
        private final String editMaKM;
        private final KhuyenMaiDAO dao = new KhuyenMaiDAO();

        private JTextField        tfMaKM, tfTenKM, tfTiLe;
        private JTextArea         taMoTa;
        private JComboBox<String> cbLoaiKH, cbTrangThai;
        private JDateChooser      dcBatDau, dcKetThuc;

        KhuyenMaiFormDialog(Frame owner, String maKM) {
            super(owner, maKM == null ? "Thêm khuyến mãi mới" : "Chỉnh sửa khuyến mãi", true);
            this.editMaKM = maKM;
            setResizable(false);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            getContentPane().setBackground(GuiTheme.LIGHT_BG);
            add(buildForm(),          BorderLayout.CENTER);
            add(buildDialogButtons(), BorderLayout.SOUTH);
            if (maKM != null) loadKhuyenMai(maKM);
            // pack() để dialog tự co theo nội dung, tránh khoảng trống thừa
            pack();
            setMinimumSize(new Dimension(520, 0));
            setLocationRelativeTo(owner);
        }

        boolean isConfirmed() { return confirmed; }

        private JDateChooser buildDateChooser() {
            JDateChooser dc = new JDateChooser();
            dc.setDateFormatString("dd/MM/yyyy");
            dc.setDate(null);
            dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            dc.setBackground(Color.WHITE);
            dc.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(210, 215, 224), 1),
                    new EmptyBorder(2, 4, 2, 4)));
            dc.setPreferredSize(new Dimension(0, 34));
            Component editor = dc.getDateEditor().getUiComponent();
            if (editor instanceof JComponent) ((JComponent) editor).setBorder(null);
            dc.addPropertyChangeListener("date", evt -> SwingUtilities.invokeLater(this::updateAutoStatus));
            return dc;
        }

        private JPanel buildForm() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(Color.WHITE);
            // Padding cân đối: trên/dưới bằng nhau
            p.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(210, 215, 224)),
                    new EmptyBorder(16, 24, 16, 24)));

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(5, 0, 5, 0);

            tfMaKM  = inputField();
            tfTenKM = inputField();

            // Ô nhập tỉ lệ + ký hiệu %
            tfTiLe = inputField();
            tfTiLe.setToolTipText("Nhập số từ 1 đến 100");
            JLabel lblPct = new JLabel("%");
            lblPct.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
            lblPct.setBorder(new EmptyBorder(0, 6, 0, 0));
            JPanel pnlTiLe = new JPanel(new BorderLayout(4, 0));
            pnlTiLe.setOpaque(false);
            pnlTiLe.add(tfTiLe,  BorderLayout.CENTER);
            pnlTiLe.add(lblPct,  BorderLayout.EAST);

            dcBatDau  = buildDateChooser();
            dcKetThuc = buildDateChooser();

            taMoTa = new JTextArea(3, 20);
            taMoTa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            taMoTa.setLineWrap(true); taMoTa.setWrapStyleWord(true);
            JScrollPane spMoTa = new JScrollPane(taMoTa);
            spMoTa.setBorder(new LineBorder(new Color(210, 215, 224), 1));

            cbLoaiKH    = new JComboBox<>(LOAI_KH_VIET);
            cbTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Sắp áp dụng", "Ngừng áp dụng"});
            cbLoaiKH.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            cbLoaiKH.setBackground(Color.WHITE);
            cbTrangThai.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            cbTrangThai.setBackground(Color.WHITE);
            cbTrangThai.setEnabled(false);

            int row = 0;
            // Mã KM chỉ hiện khi chỉnh sửa (read-only)
            if (editMaKM != null) {
                tfMaKM.setEditable(false);
                tfMaKM.setBackground(new Color(245, 247, 250));
                addRow(p, g, row++, "Mã KM",              tfMaKM);
            }
            addRow(p, g, row++, "Tên khuyến mãi *",       tfTenKM);
            addRow(p, g, row++, "Tỉ lệ giảm (1 - 100)",   pnlTiLe);
            addRow(p, g, row++, "Loại khách hàng",         cbLoaiKH);
            addRow(p, g, row++, "Ngày bắt đầu *",          dcBatDau);
            addRow(p, g, row++, "Ngày kết thúc *",         dcKetThuc);

            g.gridy = row++; g.gridx = 0; g.weightx = 0.35;
            JLabel lbMoTa = new JLabel("Mô tả");
            lbMoTa.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
            lbMoTa.setForeground(GuiTheme.SUB_TEXT);
            p.add(lbMoTa, g);
            g.gridx = 1; g.weightx = 0.65; p.add(spMoTa, g);

            addRow(p, g, row, "Trạng thái", cbTrangThai);
            return p;
        }

        private void updateAutoStatus() {
            if (cbTrangThai == null) return;
            cbTrangThai.setSelectedItem(computeStatus(
                    toLocalDateTime(dcBatDau.getDate()),
                    toLocalDateTime(dcKetThuc.getDate())));
        }

        private LocalDateTime toLocalDateTime(Date date) {
            if (date == null) return null;
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
            g.gridy = row; g.gridx = 0; g.weightx = 0.35;
            JLabel lb = new JLabel(label);
            lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
            lb.setForeground(GuiTheme.SUB_TEXT);
            p.add(lb, g);
            g.gridx = 1; g.weightx = 0.65; p.add(field, g);
        }

        private JTextField inputField() {
            JTextField tf = new JTextField();
            tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            tf.setPreferredSize(new Dimension(0, 34));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(210, 215, 224), 1),
                    new EmptyBorder(4, 10, 4, 10)));
            return tf;
        }

        private JPanel buildDialogButtons() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            p.setBackground(Color.WHITE);
            JButton btnCancel = makeRoundBtn("Hủy",  false, 110, 38);
            JButton btnSave   = makeRoundBtn(editMaKM == null ? "Thêm mới" : "Lưu thay đổi", true, 140, 38);
            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> saveKhuyenMai());
            p.add(btnCancel); p.add(btnSave);
            return p;
        }

        private void loadKhuyenMai(String maKM) {
            KhuyenMai km = dao.selectById(maKM);
            if (km == null) return;
            tfMaKM.setText(km.getMaKhuyenMai());
            tfTenKM.setText(km.getTenKhuyenMai());
            // Hiển thị dạng số nguyên (ví dụ: 0.2 → "20")
            int pct = (int) Math.round(km.getTiLeGiamGia() * 100);
            tfTiLe.setText(String.valueOf(pct));
            cbLoaiKH.setSelectedItem(loaiKHToViet(km.getLoaiKhachHang()));
            if (km.getThoiGianBatDau()  != null)
                dcBatDau.setDate(java.sql.Timestamp.valueOf(km.getThoiGianBatDau()));
            if (km.getThoiGianKetThuc() != null)
                dcKetThuc.setDate(java.sql.Timestamp.valueOf(km.getThoiGianKetThuc()));
            taMoTa.setText(km.getMoTaChiTiet());
            cbTrangThai.setSelectedItem(computeStatus(km.getThoiGianBatDau(), km.getThoiGianKetThuc()));
        }

        // Tự sinh mã KMXXXXXX (6 số ngẫu nhiên), đảm bảo không trùng DB
        private String generateMaKhuyenMai() {
            java.util.Random rnd = new java.util.Random();
            String ma;
            do {
                ma = String.format("KM%06d", rnd.nextInt(1_000_000));
            } while (dao.selectById(ma) != null);
            return ma;
        }

        private void saveKhuyenMai() {
            // Tạo mới → tự sinh mã; chỉnh sửa → giữ mã cũ
            String maKM  = (editMaKM == null) ? generateMaKhuyenMai() : tfMaKM.getText().trim();
            String tenKM = tfTenKM.getText().trim();

            if (tenKM.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên khuyến mãi.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Tỉ lệ nhập dạng số nguyên 1-100, lưu DB dạng thập phân 0.01-1.0
            int pctInput = 0;
            try { pctInput = Integer.parseInt(tfTiLe.getText().trim()); } catch (Exception ignored) {}
            if (pctInput < 1 || pctInput > 100) {
                JOptionPane.showMessageDialog(this,
                        "Tỉ lệ giảm phải là số nguyên từ 1 đến 100.\nVí dụ: nhập 20 tương đương giảm 20%.",
                        "Tỉ lệ không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double tiLe = pctInput / 100.0; // chuyển về thập phân để lưu DB

            LoaiKhachHang loaiKH = vietToLoaiKH(cbLoaiKH.getSelectedItem().toString());

            LocalDateTime bd = toLocalDateTime(dcBatDau.getDate());
            LocalDateTime kt = toLocalDateTime(dcKetThuc.getDate());

            if (bd == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Ngày bắt đầu.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
            }
            if (kt == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Ngày kết thúc.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
            }
            if (!bd.isBefore(kt)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước ngày kết thúc.",
                        "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE); return;
            }

            boolean trangThai = "Đang áp dụng".equals(computeStatus(bd, kt));
            String  moTa      = taMoTa.getText().trim();

            KhuyenMai km = new KhuyenMai(maKM, tenKM, trangThai, moTa, tiLe, loaiKH, bd, kt);
            boolean ok = (editMaKM == null) ? dao.insert(km) : dao.update(km);

            if (ok) {
                confirmed = true;
                JOptionPane.showMessageDialog(this,
                        editMaKM == null ? "Thêm mới thành công!" : "Cập nhật thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        private static JButton makeRoundBtn(String text, boolean filled, int w, int h) {
            JButton b = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (filled) {
                        g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
                                : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                        g2.setColor(Color.WHITE);
                    } else {
                        g2.setColor(getModel().isPressed() ? new Color(228, 235, 250)
                                : getModel().isRollover() ? new Color(240, 245, 255) : Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.setColor(GuiTheme.NAVY);
                        g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
                        g2.setColor(GuiTheme.NAVY);
                    }
                    g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
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
    }
}