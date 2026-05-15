package gui;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;
import entity.LoaiKhachHang;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiGUI extends JPanel {

    private static final Color NAVY        = GuiTheme.NAVY;
    private static final Color LIGHT_BG    = GuiTheme.LIGHT_BG;
    private static final Color BORDER_C    = new Color(210, 215, 224);
    private static final Color GREEN_ADD   = new Color(34, 197, 94);
    private static final Color GREEN_HOVER = new Color(22, 163, 74);
    private static final Color GREEN_PRESS = new Color(15, 130, 60);
    private static final Color RED_DEL     = new Color(220, 53, 69);
    private static final Color TAG_ALL_BG  = new Color(228, 234, 255);
    private static final Color TAG_ALL_FG  = new Color(60, 80, 180);
    private static final Color TAG_ACT_BG  = new Color(220, 252, 231);
    private static final Color TAG_ACT_FG  = new Color(22, 130, 60);
    private static final Color TAG_OFF_BG  = new Color(255, 237, 213);
    private static final Color TAG_OFF_FG  = new Color(180, 80, 0);
    private static final int   BTN_W       = 140;
    private static final int   BTN_H       = 38;

    // =========================================================
    // Mapping LoaiKhachHang <-> Tiếng Việt
    // =========================================================
    static final String[] LOAI_KH_VIET = {
        "Tất cả", "Dưới 6 tuổi", "Từ 6 đến dưới 10 tuổi",
        "Từ 60 tuổi trở lên", "Sinh viên", "Người lớn"
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

    // =========================================================
    private final KhuyenMaiDAO dao = new KhuyenMaiDAO();

    private String filterStatus = "ALL";
    private String searchText   = "";

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;

    private final List<Object[]> allRows = new ArrayList<>();

    private JPopupMenu popupMenu;
    private JMenuItem menuSua, menuXoa;
    private int popupRow = -1;

    public KhuyenMaiGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        initPopupMenu();
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        // Đã xóa buildPaginationFull()

        loadData();
    }

    private void initPopupMenu() {
        popupMenu = new JPopupMenu();
        menuSua = new JMenuItem("Sửa thông tin");
        menuXoa = new JMenuItem("Xóa khuyến mãi");

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

        JLabel lblTitle = new JLabel("Danh sách khuyến mãi");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(GuiTheme.TEXT);
        lblTitle.setBorder(new EmptyBorder(0, 2, 10, 0));
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        top.add(lblTitle);

        JPanel row2 = new JPanel(new BorderLayout(0, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, BTN_H + 4));

        // Chỉ còn nút Thêm, bỏ Xuất Excel
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBtns.setOpaque(false);
        JButton btnThem = makeColorBtn("+ Thêm khuyến mãi", GREEN_ADD, GREEN_HOVER, GREEN_PRESS, Color.WHITE, BTN_W + 10, BTN_H);
        btnThem.addActionListener(e -> openFormThem());
        leftBtns.add(btnThem);

        // Search field bo góc như login
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

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

        // Wrapper bo góc 20 giống login
        JPanel searchWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(BORDER_C);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(220, BTN_H));
        searchWrapper.setBorder(new EmptyBorder(4, 12, 4, 12));
        searchWrapper.add(tfSearch, BorderLayout.CENTER);

        rightBtns.add(lblSearch);
        rightBtns.add(searchWrapper);

        row2.add(leftBtns,  BorderLayout.WEST);
        row2.add(rightBtns, BorderLayout.EAST);
        top.add(row2);
        top.add(Box.createVerticalStrut(10));

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(LEFT_ALIGNMENT);
        row3.add(makeFilterTag("Đang áp dụng",  "ACTIVE",   TAG_ACT_BG, TAG_ACT_FG));
        row3.add(makeFilterTag("Ngừng áp dụng", "INACTIVE", TAG_OFF_BG, TAG_OFF_FG));
        top.add(row3);
        return top;
    }

    private JPanel buildTableArea() {
        String[] cols = {"Mã KM", "Tên khuyến mãi", "Tỉ lệ giảm", "Loại KH", "Bắt đầu", "Kết thúc", "Trạng thái", "Thao tác"};
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
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = txt.equals("Đang áp dụng") ? TAG_ACT_BG : TAG_OFF_BG;
                    Color fg = txt.equals("Đang áp dụng") ? TAG_ACT_FG : TAG_OFF_FG;
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(fg);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(5, 8, 5, 8));
            lbl.setOpaque(false);
            return lbl;
        });

        table.getColumnModel().getColumn(7).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel("⋮", SwingConstants.CENTER);
            lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
            lbl.setForeground(new Color(100, 110, 130));
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lbl.setOpaque(true);
            lbl.setBackground(tbl.isRowSelected(row) ? new Color(219, 234, 254) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return lbl;
        });

        int[] widths = {80, 220, 100, 150, 120, 120, 140, 80};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 7) {
                    popupRow = r;
                    table.setRowSelectionInterval(r, r);
                    Rectangle cellRect = table.getCellRect(r, c, true);
                    popupMenu.show(table, cellRect.x + cellRect.width / 2, cellRect.y + cellRect.height / 2);
                } else if (e.getClickCount() == 2 && r >= 0) {
                    openFormSua(r);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_C, 1, false), BorderFactory.createEmptyBorder()));
        sp.getViewport().setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================
    // DATA
    // =========================================================
    private void loadData() {
        allRows.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (KhuyenMai km : dao.selectAll()) {
            String tileStr = String.format("%.0f%%", km.getTiLeGiamGia() * 100);
            String loaiKH  = loaiKHToViet(km.getLoaiKhachHang()); // Tiếng Việt
            String bdStr   = km.getThoiGianBatDau()  != null ? sdf.format(java.sql.Timestamp.valueOf(km.getThoiGianBatDau()))  : "";
            String ktStr   = km.getThoiGianKetThuc() != null ? sdf.format(java.sql.Timestamp.valueOf(km.getThoiGianKetThuc())) : "";
            String ttStr   = km.getTrangThai() ? "Đang áp dụng" : "Ngừng áp dụng";
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
            if ("ACTIVE".equals(filterStatus)   && !"Đang áp dụng".equals(tt))  continue;
            if ("INACTIVE".equals(filterStatus) && !"Ngừng áp dụng".equals(tt)) continue;
            if (!searchText.isEmpty()) {
                boolean match = false;
                for (int i = 0; i < row.length - 1; i++)
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
        KhuyenMaiFormDialog dialog = new KhuyenMaiFormDialog(getParentFrame(), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData();
    }

    private void openFormSua(int tableRow) {
        String maKM = tableModel.getValueAt(tableRow, 0).toString();
        KhuyenMaiFormDialog dialog = new KhuyenMaiFormDialog(getParentFrame(), maKM);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData();
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

    // =========================================================
    // UI HELPERS — radius 20 như nút đăng nhập
    // =========================================================
    private JButton makeColorBtn(String text, Color bg, Color hover, Color press, Color fg, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!isEnabled() ? new Color(180,180,180) : getModel().isPressed() ? press : getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // radius 20
                g2.setColor(isEnabled() ? fg : Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w, h));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeFilterTag(String label, String status, Color bg, Color fg) {
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = filterStatus.equals(status);
                g2.setColor(active ? (status.equals("ALL") ? TAG_ALL_FG : status.equals("ACTIVE") ? TAG_ACT_FG : TAG_OFF_FG) : bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(active ? Color.WHITE : fg);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(new JLabel(label).getPreferredSize().width + 32, 32));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private JTextField tfMaKM, tfTenKM, tfTiLe, tfThoiGianBD, tfThoiGianKT;
        private JTextArea taMoTa;
        private JComboBox<String> cbLoaiKH, cbTrangThai;

        KhuyenMaiFormDialog(Frame owner, String maKM) {
            super(owner, maKM == null ? "Thêm khuyến mãi mới" : "Chỉnh sửa khuyến mãi", true);
            this.editMaKM = maKM;
            setSize(540, 580);
            setResizable(false);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            getContentPane().setBackground(GuiTheme.LIGHT_BG);
            add(buildForm(),          BorderLayout.CENTER);
            add(buildDialogButtons(), BorderLayout.SOUTH);
            if (maKM != null) loadKhuyenMai(maKM);
        }

        boolean isConfirmed() { return confirmed; }

        private JPanel buildForm() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(210, 215, 224)),
                    new EmptyBorder(20, 24, 16, 24)));

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 0, 6, 0);

            tfMaKM       = inputField();
            tfTenKM      = inputField();
            tfTiLe       = inputField(); tfTiLe.setToolTipText("Ví dụ: 0.2 (tương đương 20%)");
            tfThoiGianBD = inputField(); tfThoiGianBD.setToolTipText("Định dạng: dd/MM/yyyy");
            tfThoiGianKT = inputField(); tfThoiGianKT.setToolTipText("Định dạng: dd/MM/yyyy");

            taMoTa = new JTextArea(3, 20);
            taMoTa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            taMoTa.setLineWrap(true); taMoTa.setWrapStyleWord(true);
            JScrollPane spMoTa = new JScrollPane(taMoTa);
            spMoTa.setBorder(new LineBorder(new Color(210, 215, 224), 1));

            // Combobox loại KH bằng tiếng Việt, đủ tất cả enum
            cbLoaiKH    = new JComboBox<>(LOAI_KH_VIET);
            cbTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Ngừng áp dụng"});
            cbLoaiKH.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); cbLoaiKH.setBackground(Color.WHITE);
            cbTrangThai.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); cbTrangThai.setBackground(Color.WHITE);

            if (editMaKM == null) {
                addRow(p, g, 0, "Mã KM *", tfMaKM);
            } else {
                tfMaKM.setEditable(false); tfMaKM.setBackground(new Color(245, 247, 250));
                addRow(p, g, 0, "Mã KM", tfMaKM);
            }
            addRow(p, g, 1, "Tên khuyến mãi *", tfTenKM);
            addRow(p, g, 2, "Tỉ lệ giảm (0.1 - 1)", tfTiLe);
            addRow(p, g, 3, "Loại khách hàng", cbLoaiKH);
            addRow(p, g, 4, "Ngày bắt đầu", tfThoiGianBD);
            addRow(p, g, 5, "Ngày kết thúc", tfThoiGianKT);

            g.gridy = 6; g.gridx = 0; g.weightx = 0.35;
            JLabel lbMoTa = new JLabel("Mô tả");
            lbMoTa.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13)); lbMoTa.setForeground(GuiTheme.SUB_TEXT);
            p.add(lbMoTa, g);
            g.gridx = 1; g.weightx = 0.65; p.add(spMoTa, g);

            addRow(p, g, 7, "Trạng thái", cbTrangThai);
            return p;
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
            g.gridy = row; g.gridx = 0; g.weightx = 0.35;
            JLabel lb = new JLabel(label);
            lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13)); lb.setForeground(GuiTheme.SUB_TEXT);
            p.add(lb, g);
            g.gridx = 1; g.weightx = 0.65; p.add(field, g);
        }

        private JTextField inputField() {
            JTextField tf = new JTextField();
            tf.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14)); tf.setPreferredSize(new Dimension(0, 34));
            tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(210, 215, 224), 1), new EmptyBorder(4, 10, 4, 10)));
            return tf;
        }

        private JPanel buildDialogButtons() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12)); p.setBackground(Color.WHITE);
            JButton btnCancel = makeOutlineDialogBtn("Hủy", 110, 36);
            btnCancel.addActionListener(e -> dispose());
            JButton btnSave = makeNavyDialogBtn(editMaKM == null ? "Thêm mới" : "Lưu thay đổi", 140, 36);
            btnSave.addActionListener(e -> saveKhuyenMai());
            p.add(btnCancel); p.add(btnSave);
            return p;
        }

        private void loadKhuyenMai(String maKM) {
            KhuyenMai km = dao.selectById(maKM);
            if (km == null) return;
            tfMaKM.setText(km.getMaKhuyenMai());
            tfTenKM.setText(km.getTenKhuyenMai());
            tfTiLe.setText(String.valueOf(km.getTiLeGiamGia()));
            cbLoaiKH.setSelectedItem(loaiKHToViet(km.getLoaiKhachHang())); // Tiếng Việt
            // Ngày định dạng dd/MM/yyyy
            tfThoiGianBD.setText(km.getThoiGianBatDau()  != null ? km.getThoiGianBatDau().toLocalDate().format(FMT)  : "");
            tfThoiGianKT.setText(km.getThoiGianKetThuc() != null ? km.getThoiGianKetThuc().toLocalDate().format(FMT) : "");
            taMoTa.setText(km.getMoTaChiTiet());
            cbTrangThai.setSelectedIndex(km.getTrangThai() ? 0 : 1);
        }

        private void saveKhuyenMai() {
            String maKM  = tfMaKM.getText().trim();
            String tenKM = tfTenKM.getText().trim();
            if (maKM.isEmpty() || tenKM.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã KM và Tên KM.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double tiLe = 0.0;
            try { tiLe = Double.parseDouble(tfTiLe.getText().trim()); } catch (Exception ignored) {}

            // Tiếng Việt -> enum
            LoaiKhachHang loaiKH = vietToLoaiKH(cbLoaiKH.getSelectedItem().toString());

            // Parse ngày dd/MM/yyyy
            LocalDateTime bd = null, kt = null;
            try { String s = tfThoiGianBD.getText().trim(); if (!s.isEmpty()) bd = LocalDate.parse(s, FMT).atStartOfDay(); } catch (Exception ignored) {}
            try { String s = tfThoiGianKT.getText().trim(); if (!s.isEmpty()) kt = LocalDate.parse(s, FMT).atStartOfDay(); } catch (Exception ignored) {}

            String moTa    = taMoTa.getText().trim();
            boolean trangThai = cbTrangThai.getSelectedIndex() == 0;

            KhuyenMai km = new KhuyenMai(maKM, tenKM, trangThai, moTa, tiLe, loaiKH, bd, kt);
            boolean ok = (editMaKM == null) ? dao.insert(km) : dao.update(km);

            if (ok) {
                confirmed = true;
                JOptionPane.showMessageDialog(this, editMaKM == null ? "Thêm mới thành công!" : "Cập nhật thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Nút dialog radius 20
        private static JButton makeNavyDialogBtn(String text, int w, int h) {
            JButton btn = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(Color.WHITE); g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            btn.setPreferredSize(new Dimension(w, h));
            btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private static JButton makeOutlineDialogBtn(String text, int w, int h) {
            JButton b = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed() ? new Color(198,215,242) : getModel().isRollover() ? new Color(212,228,250) : new Color(240,243,248));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                    g2.setColor(GuiTheme.NAVY); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                    g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.setColor(GuiTheme.NAVY);
                    g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            b.setPreferredSize(new Dimension(w, h));
            b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }
    }
}