package gui;

import connect_DB.Connect_DB;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiGUI extends JPanel {

    // =========================================================
    // CONSTANTS — thống nhất với GuiTheme & các GUI trước
    // =========================================================
    private static final Color NAVY        = GuiTheme.NAVY;
    private static final Color LIGHT_BG    = GuiTheme.LIGHT_BG;
    private static final Color BORDER_C    = new Color(210, 215, 224);
    private static final Color GREEN_ADD   = new Color(34, 197, 94);
    private static final Color GREEN_HOVER = new Color(22, 163, 74);
    private static final Color GREEN_PRESS = new Color(15, 130, 60);
    private static final Color RED_DEL     = new Color(220, 53, 69);
    private static final Color EXCEL_BG    = new Color(240, 243, 248);
    private static final Color TAG_ALL_BG  = new Color(228, 234, 255);
    private static final Color TAG_ALL_FG  = new Color(60, 80, 180);
    private static final Color TAG_ACT_BG  = new Color(220, 252, 231);
    private static final Color TAG_ACT_FG  = new Color(22, 130, 60);
    private static final Color TAG_OFF_BG  = new Color(255, 237, 213);
    private static final Color TAG_OFF_FG  = new Color(180, 80, 0);
    private static final int   BTN_W       = 140;
    private static final int   BTN_H       = 38;
    private static final int   PAGE_SIZE   = 10;

    // =========================================================
    // STATE
    // =========================================================
    private String filterStatus = "ALL";   // ALL | ACTIVE | INACTIVE
    private String searchText   = "";
    private int    currentPage  = 0;

    // =========================================================
    // COMPONENTS
    // =========================================================
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;
    private JLabel lblPageInfo;
    private JButton btnPrev, btnNext;
    private JPanel pnlPageNumbers;

    private final List<Object[]> allRows = new ArrayList<>();

    // Menu Popup 3 chấm
    private JPopupMenu popupMenu;
    private JMenuItem menuSua, menuXoa;
    private int popupRow = -1; // Lưu lại row đang được click 3 chấm

    public KhuyenMaiGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        initPopupMenu();

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildPaginationFull(),BorderLayout.SOUTH);

        loadData();
    }

    private void initPopupMenu() {
        popupMenu = new JPopupMenu();
        menuSua = new JMenuItem("Sửa thông tin");
        menuXoa = new JMenuItem("Xóa khuyến mãi");

        menuSua.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        menuXoa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        menuXoa.setForeground(RED_DEL);

        menuSua.addActionListener(e -> {
            if (popupRow >= 0) openFormSua(popupRow);
        });

        menuXoa.addActionListener(e -> {
            if (popupRow >= 0) xoaKhuyenMai(popupRow);
        });

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

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBtns.setOpaque(false);

        JButton btnThem = makeColorBtn("+ Thêm khuyến mãi", GREEN_ADD, GREEN_HOVER, GREEN_PRESS, Color.WHITE, BTN_W + 10, BTN_H);
        btnThem.addActionListener(e -> openFormThem());

        JButton btnExcel = makeOutlineBtn("Xuất Excel", 110, BTN_H);
        btnExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng đang phát triển"));

        leftBtns.add(btnThem);
        leftBtns.add(btnExcel);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lblSearch.setForeground(GuiTheme.SUB_TEXT);

        tfSearch = new JTextField(18);
        tfSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tfSearch.setPreferredSize(new Dimension(220, BTN_H));
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C, 1, false),
                new EmptyBorder(4, 10, 4, 10)));
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
        });

        rightBtns.add(lblSearch);
        rightBtns.add(tfSearch);

        row2.add(leftBtns,  BorderLayout.WEST);
        row2.add(rightBtns, BorderLayout.EAST);
        top.add(row2);
        top.add(Box.createVerticalStrut(10));

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(LEFT_ALIGNMENT);

        row3.add(makeFilterTag("Tất cả",          "ALL",      TAG_ALL_BG, TAG_ALL_FG));
        row3.add(makeFilterTag("Đang áp dụng",    "ACTIVE",   TAG_ACT_BG, TAG_ACT_FG));
        row3.add(makeFilterTag("Ngừng áp dụng",   "INACTIVE", TAG_OFF_BG, TAG_OFF_FG));

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

        // Render cột Trạng thái
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
            if (tbl.isRowSelected(row)) lbl.setBackground(new Color(219, 234, 254));
            else lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            return lbl;
        });

        // Render cột Thao tác (3 chấm)
        table.getColumnModel().getColumn(7).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel("⋮", SwingConstants.CENTER);
            lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
            lbl.setForeground(new Color(100, 110, 130));
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lbl.setOpaque(true);
            lbl.setBackground(tbl.isRowSelected(row) ? new Color(219, 234, 254) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return lbl;
        });

        int[] widths = {80, 220, 100, 140, 120, 120, 140, 80};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Sự kiện click bảng
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 7) { 
                    // Click vào cột 3 chấm
                    popupRow = r;
                    table.setRowSelectionInterval(r, r);
                    Rectangle cellRect = table.getCellRect(r, c, true);
                    popupMenu.show(table, cellRect.x + cellRect.width / 2, cellRect.y + cellRect.height / 2);
                } else if (e.getClickCount() == 2 && r >= 0) {
                    // Double click để sửa
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

    private JPanel buildPaginationFull() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblPageInfo = new JLabel("", SwingConstants.LEFT);
        lblPageInfo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblPageInfo.setForeground(GuiTheme.SUB_TEXT);

        pnlPageNumbers = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        pnlPageNumbers.setOpaque(false);

        btnPrev = makePageBtn("‹");
        btnNext = makePageBtn("›");

        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; renderPage(); } });
        btnNext.addActionListener(e -> {
            if (currentPage < getTotalPages() - 1) { currentPage++; renderPage(); }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(btnPrev);
        rightPanel.add(pnlPageNumbers);
        rightPanel.add(btnNext);

        bar.add(lblPageInfo, BorderLayout.WEST);
        bar.add(rightPanel,  BorderLayout.EAST);
        return bar;
    }

    private void loadData() {
        allRows.clear();
        String sql = "SELECT maKhuyenMai, tenKhuyenMai, tiLeGiamGia, loaiKhachHang, thoiGianBatDau, thoiGianKetThuc, trangThai FROM KhuyenMai ORDER BY maKhuyenMai ASC";

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            while (rs.next()) {
                String maKM     = rs.getString("maKhuyenMai");
                String tenKM    = rs.getString("tenKhuyenMai");
                double tiLe     = rs.getDouble("tiLeGiamGia");
                String loaiKH   = rs.getString("loaiKhachHang");
                Date bd         = rs.getDate("thoiGianBatDau");
                Date kt         = rs.getDate("thoiGianKetThuc");
                int ttInt       = rs.getInt("trangThai");

                String tileStr  = String.format("%.0f%%", tiLe * 100);
                String bdStr    = bd != null ? sdf.format(bd) : "";
                String ktStr    = kt != null ? sdf.format(kt) : "";
                String ttStr    = (ttInt == 1) ? "Đang áp dụng" : "Ngừng áp dụng";

                allRows.add(new Object[]{ maKM, tenKM, tileStr, loaiKH, bdStr, ktStr, ttStr, "" });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        applySearch();
    }

    private void applySearch() {
        searchText = tfSearch == null ? "" : tfSearch.getText().trim().toLowerCase();
        currentPage = 0;
        renderPage();
    }

    private List<Object[]> getFilteredRows() {
        List<Object[]> result = new ArrayList<>();
        for (Object[] row : allRows) {
            String tt = row[6].toString();
            if ("ACTIVE".equals(filterStatus) && !"Đang áp dụng".equals(tt)) continue;
            if ("INACTIVE".equals(filterStatus) && !"Ngừng áp dụng".equals(tt)) continue;

            if (!searchText.isEmpty()) {
                boolean match = false;
                for (int i=0; i<row.length-1; i++) {
                    if (row[i] != null && row[i].toString().toLowerCase().contains(searchText)) {
                        match = true; break;
                    }
                }
                if (!match) continue;
            }
            result.add(row);
        }
        return result;
    }

    private int getTotalPages() {
        int total = getFilteredRows().size();
        return Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
    }

    private void renderPage() {
        tableModel.setRowCount(0);
        List<Object[]> filtered = getFilteredRows();

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());
        for (int i = from; i < to; i++) tableModel.addRow(filtered.get(i));

        int totalPages = getTotalPages();
        int totalRows  = filtered.size();

        if (lblPageInfo != null) lblPageInfo.setText(String.format("Hiển thị %d–%d / %d khuyến mãi", Math.min(from + 1, totalRows), to, totalRows));
        if (btnPrev != null) btnPrev.setEnabled(currentPage > 0);
        if (btnNext != null) btnNext.setEnabled(currentPage < totalPages - 1);

        renderPageNumbers(totalPages);
    }

    private void renderPageNumbers(int totalPages) {
        if (pnlPageNumbers == null) return;
        pnlPageNumbers.removeAll();
        int start = Math.max(0, currentPage - 2);
        int end   = Math.min(totalPages, start + 5);
        start = Math.max(0, end - 5);

        for (int i = start; i < end; i++) {
            final int page = i;
            boolean isCur = (i == currentPage);
            JButton pb = new JButton(String.valueOf(i + 1)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isCur ? NAVY : (getModel().isRollover() ? new Color(230, 236, 248) : Color.WHITE));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    if (!isCur) {
                        g2.setColor(BORDER_C);
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                    }
                    g2.setColor(isCur ? Color.WHITE : GuiTheme.TEXT);
                    g2.setFont(GuiTheme.font("Segoe UI", isCur ? Font.BOLD : Font.PLAIN, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    String t = getText();
                    g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            pb.setPreferredSize(new Dimension(36, 32));
            pb.setContentAreaFilled(false); pb.setBorderPainted(false); pb.setFocusPainted(false);
            pb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (!isCur) pb.addActionListener(e -> { currentPage = page; renderPage(); });
            pnlPageNumbers.add(pb);
        }
        pnlPageNumbers.revalidate();
        pnlPageNumbers.repaint();
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
        String maKM = tableModel.getValueAt(row, 0).toString();
        String tenKM = tableModel.getValueAt(row, 1).toString();

        int ch = JOptionPane.showConfirmDialog(this, "Xóa khuyến mãi " + tenKM + " (" + maKM + ")?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ch == JOptionPane.YES_OPTION) {
            try (Connection con = Connect_DB.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM KhuyenMai WHERE maKhuyenMai = ?")) {
                ps.setString(1, maKM);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage());
            }
        }
    }

    private Frame getParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return (w instanceof Frame) ? (Frame) w : null;
    }

    // --- UI Helpers ---
    private JButton makeColorBtn(String text, Color bg, Color hover, Color press, Color fg, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!isEnabled() ? new Color(180, 180, 180) : getModel().isPressed() ? press : getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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

    private JButton makeOutlineBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(198,215,242) : getModel().isRollover() ? new Color(212,228,250) : EXCEL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(NAVY); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(NAVY);
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w, h));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makePageBtn(String label) {
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (getModel().isRollover() ? new Color(230,236,248) : Color.WHITE) : new Color(245, 246, 248));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(BORDER_C); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(isEnabled() ? NAVY : new Color(180,180,180));
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(36, 32));
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
            filterStatus = status; currentPage = 0; renderPage();
            Container parent = b.getParent(); if (parent != null) parent.repaint();
        });
        return b;
    }

    // =========================================================
    // INNER CLASS: Dialog Thêm / Sửa Khuyến Mãi
    // =========================================================
    static class KhuyenMaiFormDialog extends JDialog {
        private boolean confirmed = false;
        private final String editMaKM;

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

            add(buildForm(), BorderLayout.CENTER);
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
            tfThoiGianBD = inputField(); tfThoiGianBD.setToolTipText("Định dạng: yyyy-MM-dd");
            tfThoiGianKT = inputField(); tfThoiGianKT.setToolTipText("Định dạng: yyyy-MM-dd");
            
            taMoTa = new JTextArea(3, 20);
            taMoTa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            taMoTa.setLineWrap(true); taMoTa.setWrapStyleWord(true);
            JScrollPane spMoTa = new JScrollPane(taMoTa);
            spMoTa.setBorder(new LineBorder(new Color(210, 215, 224), 1));

            cbLoaiKH = new JComboBox<>(new String[]{"TẤT CẢ", "SINH_VIEN", "TU_60_TRO_LEN"});
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
            addRow(p, g, 4, "Thời gian bắt đầu", tfThoiGianBD);
            addRow(p, g, 5, "Thời gian kết thúc", tfThoiGianKT);
            
            g.gridy = 6; g.gridx = 0; g.weightx = 0.35;
            JLabel lbMoTa = new JLabel("Mô tả"); lbMoTa.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13)); lbMoTa.setForeground(GuiTheme.SUB_TEXT);
            p.add(lbMoTa, g);
            g.gridx = 1; g.weightx = 0.65; p.add(spMoTa, g);

            addRow(p, g, 7, "Trạng thái", cbTrangThai);
            return p;
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
            g.gridy = row; g.gridx = 0; g.weightx = 0.35;
            JLabel lb = new JLabel(label); lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13)); lb.setForeground(GuiTheme.SUB_TEXT);
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
            JButton btnCancel = makeOutlineDialogBtn("Hủy", 110, 36); btnCancel.addActionListener(e -> dispose());
            JButton btnSave = makeNavyDialogBtn(editMaKM == null ? "Thêm mới" : "Lưu thay đổi", 140, 36); btnSave.addActionListener(e -> saveKhuyenMai());
            p.add(btnCancel); p.add(btnSave);
            return p;
        }

        private void loadKhuyenMai(String maKM) {
            String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
            try (Connection con = Connect_DB.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maKM);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tfMaKM.setText(maKM);
                    tfTenKM.setText(rs.getString("tenKhuyenMai"));
                    tfTiLe.setText(String.valueOf(rs.getDouble("tiLeGiamGia")));
                    cbLoaiKH.setSelectedItem(rs.getString("loaiKhachHang"));
                    tfThoiGianBD.setText(rs.getDate("thoiGianBatDau") != null ? rs.getDate("thoiGianBatDau").toString() : "");
                    tfThoiGianKT.setText(rs.getDate("thoiGianKetThuc") != null ? rs.getDate("thoiGianKetThuc").toString() : "");
                    taMoTa.setText(rs.getString("moTaChiTiet"));
                    cbTrangThai.setSelectedIndex(rs.getInt("trangThai") == 1 ? 0 : 1);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        private void saveKhuyenMai() {
            String maKM = tfMaKM.getText().trim();
            String tenKM = tfTenKM.getText().trim();
            if (maKM.isEmpty() || tenKM.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã KM và Tên KM.", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
            }

            double tiLe = 0.0;
            try { tiLe = Double.parseDouble(tfTiLe.getText().trim()); } catch (Exception ignored) {}
            String loaiKH = cbLoaiKH.getSelectedItem().toString();
            String bd = tfThoiGianBD.getText().trim().isEmpty() ? null : tfThoiGianBD.getText().trim();
            String kt = tfThoiGianKT.getText().trim().isEmpty() ? null : tfThoiGianKT.getText().trim();
            String moTa = taMoTa.getText().trim();
            int trangThai = cbTrangThai.getSelectedIndex() == 0 ? 1 : 0;

            try (Connection con = Connect_DB.getInstance().getConnection()) {
                if (editMaKM == null) {
                    String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, trangThai, moTaChiTiet, tiLeGiamGia, loaiKhachHang, thoiGianBatDau, thoiGianKetThuc) VALUES (?,?,?,?,?,?,?,?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maKM); ps.setNString(2, tenKM); ps.setInt(3, trangThai); ps.setNString(4, moTa); ps.setDouble(5, tiLe); ps.setString(6, loaiKH);
                        if (bd != null) ps.setDate(7, java.sql.Date.valueOf(bd)); else ps.setNull(7, Types.DATE);
                        if (kt != null) ps.setDate(8, java.sql.Date.valueOf(kt)); else ps.setNull(8, Types.DATE);
                        ps.executeUpdate();
                    }
                } else {
                    String sql = "UPDATE KhuyenMai SET tenKhuyenMai=?, trangThai=?, moTaChiTiet=?, tiLeGiamGia=?, loaiKhachHang=?, thoiGianBatDau=?, thoiGianKetThuc=? WHERE maKhuyenMai=?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setNString(1, tenKM); ps.setInt(2, trangThai); ps.setNString(3, moTa); ps.setDouble(4, tiLe); ps.setString(5, loaiKH);
                        if (bd != null) ps.setDate(6, java.sql.Date.valueOf(bd)); else ps.setNull(6, Types.DATE);
                        if (kt != null) ps.setDate(7, java.sql.Date.valueOf(kt)); else ps.setNull(7, Types.DATE);
                        ps.setString(8, editMaKM);
                        ps.executeUpdate();
                    }
                }
                confirmed = true;
                JOptionPane.showMessageDialog(this, editMaKM == null ? "Thêm mới thành công!" : "Cập nhật thành công!");
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        private static JButton makeNavyDialogBtn(String text, int w, int h) {
            JButton btn = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(Color.WHITE); g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                    FontMetrics fm = g2.getFontMetrics(); g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            btn.setPreferredSize(new Dimension(w, h)); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
        }

        private static JButton makeOutlineDialogBtn(String text, int w, int h) {
            JButton b = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed() ? new Color(198,215,242) : getModel().isRollover() ? new Color(212,228,250) : new Color(240,243,248));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                    g2.setColor(GuiTheme.NAVY); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                    g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                    FontMetrics fm = g2.getFontMetrics(); g2.setColor(GuiTheme.NAVY); g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            b.setPreferredSize(new Dimension(w, h)); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
        }
    }
}