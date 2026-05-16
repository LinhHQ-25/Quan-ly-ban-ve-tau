package gui;

import connect_DB.Connect_DB;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class QLyNhanVienGUI extends JPanel {

    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final Color NAVY        = GuiTheme.NAVY;
    private static final Color LIGHT_BG    = GuiTheme.LIGHT_BG;
    private static final Color BORDER_C    = new Color(210, 215, 224);
    private static final Color RED_DEL     = new Color(220, 53, 69);
    private static final Color TAG_ALL_BG  = new Color(228, 234, 255);
    private static final Color TAG_ALL_FG  = new Color(60, 80, 180);
    private static final Color TAG_ACT_BG  = new Color(220, 252, 231);
    private static final Color TAG_ACT_FG  = new Color(22, 130, 60);
    private static final Color TAG_OFF_BG  = new Color(255, 237, 213);
    private static final Color TAG_OFF_FG  = new Color(180, 80, 0);
    private static final int   BTN_H       = 38;
    private static final int   PAGE_SIZE   = 10;

    // =========================================================
    // STATE
    // =========================================================
    private String filterStatus = "ALL";
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

    private JPopupMenu popupMenu;
    private JMenuItem  menuSua, menuXoa;
    private int        popupRow = -1;

    private final List<Object[]> allRows = new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public QLyNhanVienGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        initPopupMenu();

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(buildTableArea(), BorderLayout.CENTER);
        centerWrap.add(buildBottomBar(), BorderLayout.SOUTH);

        add(buildTopBar(), BorderLayout.NORTH);
        add(centerWrap,    BorderLayout.CENTER);

        loadData();
    }

    // =========================================================
    // POPUP MENU
    // =========================================================
    private void initPopupMenu() {
        popupMenu = new JPopupMenu();
        menuSua   = new JMenuItem("Sửa thông tin");
        menuXoa   = new JMenuItem("Xóa nhân viên");

        menuSua.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        menuXoa.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        menuXoa.setForeground(RED_DEL);

        menuSua.addActionListener(e -> { if (popupRow >= 0) openFormSua(popupRow); });
        menuXoa.addActionListener(e -> { if (popupRow >= 0) xoaNhanVien(popupRow); });

        popupMenu.add(menuSua);
        popupMenu.add(menuXoa);
    }

    // =========================================================
    // TOP BAR: Filter Tags + Rounded Search Box
    // =========================================================
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(0, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tagPanel.setOpaque(false);
        tagPanel.add(makeFilterTag("Tất cả",         "ALL",      TAG_ALL_BG, TAG_ALL_FG));
        tagPanel.add(makeFilterTag("Đang làm việc",  "ACTIVE",   TAG_ACT_BG, TAG_ACT_FG));
        tagPanel.add(makeFilterTag("Ngừng làm việc", "INACTIVE", TAG_OFF_BG, TAG_OFF_FG));

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

        searchRight.add(lblSearch);
        searchRight.add(searchWrapper);

        top.add(tagPanel,    BorderLayout.WEST);
        top.add(searchRight, BorderLayout.EAST);
        return top;
    }

    // =========================================================
    // BOTTOM BAR
    // =========================================================
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel leftPaging = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPaging.setOpaque(false);

        lblPageInfo = new JLabel("", SwingConstants.LEFT);
        lblPageInfo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblPageInfo.setForeground(GuiTheme.SUB_TEXT);

        pnlPageNumbers = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        pnlPageNumbers.setOpaque(false);

        btnPrev = makePageBtn("‹");
        btnNext = makePageBtn("›");
        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; renderPage(); } });
        btnNext.addActionListener(e -> { if (currentPage < getTotalPages() - 1) { currentPage++; renderPage(); } });

        leftPaging.add(lblPageInfo);
        leftPaging.add(btnPrev);
        leftPaging.add(pnlPageNumbers);
        leftPaging.add(btnNext);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBtns.setOpaque(false);

        JButton btnExcel = makeRoundBtn("Xuất Excel", false, 120, 40);
        btnExcel.addActionListener(e -> xuatExcel());

        JButton btnThem = makeRoundBtn("+ Thêm nhân viên", true, 160, 40);
        btnThem.addActionListener(e -> openFormThem());

        rightBtns.add(btnExcel);
        rightBtns.add(btnThem);

        bar.add(leftPaging, BorderLayout.WEST);
        bar.add(rightBtns,  BorderLayout.EAST);
        return bar;
    }

    // =========================================================
    // TABLE
    // =========================================================
    private JPanel buildTableArea() {
        // ĐÃ SỬA: Đổi tên MaNV thành "Mã Nhân Viên", gộp Họ đệm + Tên thành "Họ và tên" (tổng 9 cột)
        String[] cols = {"Mã Nhân Viên", "Họ và tên", "Giới tính", "Ngày sinh",
                "Số điện thoại", "Email", "Chức vụ", "Trạng thái", "Thao tác"};
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

        // ĐÃ SỬA: Hiển thị lưới rõ ràng giữa các ô
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 233, 238));
        table.setIntercellSpacing(new Dimension(1, 1));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false); // (Đã có sẵn) Không cho phép kéo thả đổi chỗ cột
        header.setResizingAllowed(false);   // THÊM DÒNG NÀY: Khóa không cho kéo giãn/thu hẹp độ rộng cột
        header.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 244, 250));
        header.setForeground(GuiTheme.TEXT);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
        header.setReorderingAllowed(false);

        // Render cột Trạng thái (cột thứ 7 - index 7)
        table.getColumnModel().getColumn(7).setCellRenderer((tbl, val, sel, foc, row, col) -> {

            String txt = val == null ? "" : val.toString();
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = txt.equals("Đang làm việc") ? TAG_ACT_BG : TAG_OFF_BG;
                    Color fg = txt.equals("Đang làm việc") ? TAG_ACT_FG : TAG_OFF_FG;
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
            lbl.setOpaque(true);
            lbl.setBackground(tbl.isRowSelected(row) ? new Color(219, 234, 254) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return lbl;
        });

        // Render cột Chức vụ (cột thứ 6 - index 6)
        table.getColumnModel().getColumn(6).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            String txt = val == null ? "" : val.toString();
            JLabel lbl = new JLabel(txt, SwingConstants.LEFT);
            lbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
            lbl.setOpaque(true);
            lbl.setBackground(tbl.isRowSelected(row) ? new Color(219, 234, 254) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            lbl.setForeground(NAVY);
            return lbl;
        });

        // Render cột ⋮ (Thao tác - index 8)
        table.getColumnModel().getColumn(8).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel("...", SwingConstants.CENTER);
            lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
            lbl.setForeground(new Color(100, 110, 130));
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lbl.setOpaque(true);
            lbl.setBackground(tbl.isRowSelected(row) ? new Color(219, 234, 254) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return lbl;
        });

        // Độ rộng 9 cột
        int[] widths = {100, 160, 70, 90, 110, 170, 100, 110, 60};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        }

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                // Cột Thao tác hiện là index 8
                if (r >= 0 && c == 8) {
                    popupRow = r;
                    table.setRowSelectionInterval(r, r);
                    Rectangle cell = table.getCellRect(r, c, true);
                    popupMenu.show(table, cell.x + cell.width / 2, cell.y + cell.height / 2);
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

    // =========================================================
    // DATA LOADING & FILTERING
    // =========================================================
    private void loadData() {
        allRows.clear();
        String sql = "SELECT maNV, hoTenNV, ngaySinh, gioiTinh, soDT, email, loaiNV FROM NhanVien ORDER BY maNV ASC";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maNV      = rs.getString("maNV");
                String hoTenNV   = rs.getString("hoTenNV") == null ? "" : rs.getString("hoTenNV").trim();

                String ngaySinhStr = "";
                Date sqlDate = rs.getDate("ngaySinh");
                if (sqlDate != null) {
                    ngaySinhStr = sdf.format(new java.util.Date(sqlDate.getTime()));
                }

                boolean gioiTinh = rs.getBoolean("gioiTinh");
                String soDT      = nvl(rs.getString("soDT"));
                String email     = nvl(rs.getString("email"));
                String loaiNV    = rs.getString("loaiNV");

                String ttDisplay = "Đang làm việc";
                String chucVu = "NHAN_VIEN_BAN_VE".equals(loaiNV) ? "Bán vé" : "NHAN_VIEN_QUAN_LY".equals(loaiNV) ? "Quản lý" : loaiNV;

                // Gộp họ đệm và tên -> lấy thẳng hoTenNV
                allRows.add(new Object[]{
                        maNV, hoTenNV, gioiTinh ? "Nam" : "Nữ",
                        ngaySinhStr, soDT, email, chucVu, ttDisplay, ""
                });
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
            String tt = row[7].toString(); // Trạng thái dời sang index 7
            if ("ACTIVE".equals(filterStatus) && !"Đang làm việc".equals(tt)) continue;
            if ("INACTIVE".equals(filterStatus) && !"Ngừng làm việc".equals(tt)) continue;

            if (!searchText.isEmpty()) {
                boolean match = false;
                for (Object cell : row) {
                    if (cell != null && cell.toString().toLowerCase().contains(searchText)) {
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

        if (lblPageInfo != null) {
            lblPageInfo.setText(String.format("Hiển thị %d–%d / %d nhân viên", Math.min(from + 1, totalRows), to, totalRows));
        }
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
                    FontMetrics fm = g2.getFontMetrics(); String t = getText();
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

    // =========================================================
    // ACTIONS
    // =========================================================
    private void openFormThem() {
        NhanVienFormDialog dialog = new NhanVienFormDialog(getParentFrame(), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData();
    }

    private void openFormSua(int tableRow) {
        String maNV = tableModel.getValueAt(tableRow, 0).toString();
        NhanVienFormDialog dialog = new NhanVienFormDialog(getParentFrame(), maNV);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData();
    }

    private void xoaNhanVien(int row) {
        String maNV = tableModel.getValueAt(row, 0).toString();
        String ten  = tableModel.getValueAt(row, 1).toString();

        int ch = JOptionPane.showConfirmDialog(this,
                "<html><b>Xóa nhân viên " + ten + " (" + maNV + ")?</b><br>" +
                        "Hành động này không thể hoàn tác.</html>",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ch != JOptionPane.YES_OPTION) return;

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM NhanVien WHERE maNV = ?")) {
            ps.setString(1, maNV);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Đã xóa nhân viên " + maNV + ".", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatExcel() {
        JOptionPane.showMessageDialog(this, "Chức năng xuất Excel đang được phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private Frame getParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return (w instanceof Frame) ? (Frame) w : null;
    }

    // =========================================================
    // UI HELPERS
    // =========================================================
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
                FontMetrics fm = g2.getFontMetrics(); String t = getText();
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(36, 32));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                FontMetrics fm = g2.getFontMetrics(); String t = getText();
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        Dimension d = new Dimension(new JLabel(label).getPreferredSize().width + 32, 32);
        b.setPreferredSize(d);
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            filterStatus = status;
            currentPage = 0;
            renderPage();
            Container parent = b.getParent();
            if (parent != null) parent.repaint();
        });
        return b;
    }

    public static JButton makeRoundBtn(String text, boolean filled, int w, int h) {
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
                    g2.setColor(getModel().isPressed() ? new Color(198, 215, 242)
                            : getModel().isRollover() ? new Color(212, 228, 250) : new Color(240, 243, 248));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(GuiTheme.NAVY);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                }
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w, h));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    // =========================================================
    // INNER CLASS: Dialog Thêm / Sửa Nhân viên
    // =========================================================
    static class NhanVienFormDialog extends JDialog {

        private boolean confirmed = false;
        private final String editMaNV;

        private JTextField tfMaNV, tfHoTen, tfSoDT, tfEmail, tfDiaChi, tfCCCD;
        private JComboBox<String> cbGioiTinh, cbLoaiNV;
        private JDateChooser dcNgaySinh;

        NhanVienFormDialog(Frame owner, String maNV) {
            super(owner, maNV == null ? "Thêm nhân viên mới" : "Chỉnh sửa nhân viên", true);
            this.editMaNV = maNV;

            setSize(540, 520);
            setResizable(false);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            getContentPane().setBackground(GuiTheme.LIGHT_BG);

            add(buildForm(), BorderLayout.CENTER);
            add(buildDialogButtons(), BorderLayout.SOUTH);

            if (maNV != null) loadNhanVien(maNV);
        }

        boolean isConfirmed() { return confirmed; }

        private JPanel buildForm() {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(210, 215, 224)),
                    new EmptyBorder(20, 24, 16, 24)));

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(6, 0, 6, 0);

            tfMaNV      = inputField();
            tfHoTen     = inputField();
            dcNgaySinh  = buildDateChooser(true); // Dùng JDateChooser
            tfSoDT      = inputField();
            tfEmail     = inputField();
            tfDiaChi    = inputField();
            tfCCCD      = inputField();
            cbGioiTinh  = new JComboBox<>(new String[]{"Nam", "Nữ"});
            cbLoaiNV    = new JComboBox<>(new String[]{"Bán vé", "Quản lý"});

            styleCombo(cbGioiTinh); styleCombo(cbLoaiNV);

            if (editMaNV == null) {
                addRow(p, g, 0, "Mã NV *",       tfMaNV);
            } else {
                tfMaNV.setEditable(false);
                tfMaNV.setBackground(new Color(245, 247, 250));
                addRow(p, g, 0, "Mã NV",          tfMaNV);
            }
            addRow(p, g, 1, "Họ và tên *",   tfHoTen);
            addRow(p, g, 2, "Ngày sinh",     dcNgaySinh);
            addRow(p, g, 3, "Giới tính",     cbGioiTinh);
            addRow(p, g, 4, "Số điện thoại", tfSoDT);
            addRow(p, g, 5, "Email",         tfEmail);
            addRow(p, g, 6, "Địa chỉ",       tfDiaChi);
            addRow(p, g, 7, "CCCD",          tfCCCD);
            addRow(p, g, 8, "Chức vụ",       cbLoaiNV);

            return p;
        }

        private JDateChooser buildDateChooser(boolean enabled) {
            JDateChooser dc = new JDateChooser();
            dc.setDateFormatString("dd/MM/yyyy");
            dc.setDate(null);
            dc.setEnabled(enabled);
            dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            dc.setBackground(enabled ? Color.WHITE : new Color(245, 247, 250));
            dc.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_C, 1, false), new EmptyBorder(2, 4, 2, 4)));

            Component editor = dc.getDateEditor().getUiComponent();
            if (editor instanceof JComponent) ((JComponent) editor).setBorder(null);
            return dc;
        }

        private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
            g.gridy = row; g.gridx = 0; g.weightx = 0.35;
            JLabel lb = new JLabel(label);
            lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
            lb.setForeground(GuiTheme.SUB_TEXT);
            p.add(lb, g);
            g.gridx = 1; g.weightx = 0.65;
            p.add(field, g);
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

        private void styleCombo(JComboBox<String> cb) {
            cb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            cb.setBackground(Color.WHITE);
            cb.setPreferredSize(new Dimension(0, 34));
        }

        private JPanel buildDialogButtons() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            p.setBackground(Color.WHITE);

            JButton btnCancel = QLyNhanVienGUI.makeRoundBtn("Hủy", false, 110, 38);
            btnCancel.addActionListener(e -> dispose());

            JButton btnSave = QLyNhanVienGUI.makeRoundBtn(editMaNV == null ? "Thêm mới" : "Lưu thay đổi", true, 140, 38);
            btnSave.addActionListener(e -> saveNhanVien());

            p.add(btnCancel);
            p.add(btnSave);
            return p;
        }

        private void loadNhanVien(String maNV) {
            String sql = "SELECT maNV, hoTenNV, ngaySinh, gioiTinh, soDT, email, diaChi, soCCCD, loaiNV FROM NhanVien WHERE maNV = ?";
            try (Connection con = Connect_DB.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maNV);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tfMaNV.setText(maNV);
                    tfHoTen.setText(nvl(rs.getString("hoTenNV")));

                    Date sqlDate = rs.getDate("ngaySinh");
                    if (sqlDate != null) {
                        dcNgaySinh.setDate(new java.util.Date(sqlDate.getTime()));
                    } else {
                        dcNgaySinh.setDate(null);
                    }

                    cbGioiTinh.setSelectedItem(rs.getBoolean("gioiTinh") ? "Nam" : "Nữ");
                    tfSoDT.setText(nvl(rs.getString("soDT")));
                    tfEmail.setText(nvl(rs.getString("email")));
                    tfDiaChi.setText(nvl(rs.getString("diaChi")));
                    tfCCCD.setText(nvl(rs.getString("soCCCD")));
                    String loai = rs.getString("loaiNV");
                    cbLoaiNV.setSelectedItem("NHAN_VIEN_QUAN_LY".equals(loai) ? "Quản lý" : "Bán vé");
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        private void saveNhanVien() {
            String maNV   = tfMaNV.getText().trim();
            String hoTen  = tfHoTen.getText().trim();
            if (maNV.isEmpty() || hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã NV và Họ tên.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Lấy chuỗi ngày sinh định dạng chuẩn DB (yyyy-MM-dd)
            java.util.Date uDate = dcNgaySinh.getDate();
            String ngaySinh = null;
            if (uDate != null) {
                ngaySinh = new SimpleDateFormat("yyyy-MM-dd").format(uDate);
            }

            boolean gioiTinh = "Nam".equals(cbGioiTinh.getSelectedItem());
            String soDT    = tfSoDT.getText().trim();
            String email   = tfEmail.getText().trim();
            String diaChi  = tfDiaChi.getText().trim();
            String cccd    = tfCCCD.getText().trim();
            String loaiNV  = "Quản lý".equals(cbLoaiNV.getSelectedItem()) ? "NHAN_VIEN_QUAN_LY" : "NHAN_VIEN_BAN_VE";

            try (Connection con = Connect_DB.getInstance().getConnection()) {
                if (editMaNV == null) {
                    String sql = "INSERT INTO NhanVien (maNV, hoTenNV, ngaySinh, gioiTinh, soDT, email, diaChi, soCCCD, loaiNV) VALUES (?,?,?,?,?,?,?,?,?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maNV);
                        ps.setNString(2, hoTen);
                        if (ngaySinh != null) ps.setDate(3, java.sql.Date.valueOf(ngaySinh));
                        else ps.setNull(3, Types.DATE);
                        ps.setBoolean(4, gioiTinh);
                        ps.setString(5, soDT);
                        ps.setString(6, email);
                        ps.setNString(7, diaChi);
                        ps.setString(8, cccd);
                        ps.setString(9, loaiNV);
                        ps.executeUpdate();
                    }
                } else {
                    String sql = "UPDATE NhanVien SET hoTenNV=?, ngaySinh=?, gioiTinh=?, soDT=?, email=?, diaChi=?, soCCCD=?, loaiNV=? WHERE maNV=?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setNString(1, hoTen);
                        if (ngaySinh != null) ps.setDate(2, java.sql.Date.valueOf(ngaySinh));
                        else ps.setNull(2, Types.DATE);
                        ps.setBoolean(3, gioiTinh);
                        ps.setString(4, soDT);
                        ps.setString(5, email);
                        ps.setNString(6, diaChi);
                        ps.setString(7, cccd);
                        ps.setString(8, loaiNV);
                        ps.setString(9, editMaNV);
                        ps.executeUpdate();
                    }
                }
                confirmed = true;
                JOptionPane.showMessageDialog(this,
                        editMaNV == null ? "Thêm nhân viên thành công!" : "Cập nhật thành công!",
                        "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static QLyNhanVienGUI create() {
        return new QLyNhanVienGUI();
    }
}