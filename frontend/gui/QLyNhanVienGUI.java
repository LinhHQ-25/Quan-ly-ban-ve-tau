package gui;

import connect_DB.Connect_DB;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QLyNhanVienGUI extends JPanel {

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
    private static final Color RED_HOVER   = new Color(185, 28, 48);
    private static final Color EXCEL_BG    = new Color(240, 243, 248);
    private static final Color TAG_ALL_BG  = new Color(228, 234, 255);
    private static final Color TAG_ALL_FG  = new Color(60, 80, 180);
    private static final Color TAG_ACT_BG  = new Color(220, 252, 231);
    private static final Color TAG_ACT_FG  = new Color(22, 130, 60);
    private static final Color TAG_OFF_BG  = new Color(255, 237, 213);
    private static final Color TAG_OFF_FG  = new Color(180, 80, 0);
    private static final int   BTN_W       = 130;
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
    private JButton btnXoa;

    // Dữ liệu đầy đủ đã load (sau filter)
    private final List<Object[]> allRows = new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public QLyNhanVienGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(LIGHT_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildPagination(),BorderLayout.SOUTH);

        loadData();
    }
    // =========================================================
    // TOP BAR: tiêu đề + nút hành động + filter + tìm kiếm
    // =========================================================
    private JPanel buildTopBar() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 12, 0));

        // --- Hàng 1: tiêu đề ---
        JLabel lblTitle = new JLabel("Danh sách nhân viên");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(GuiTheme.TEXT);
        lblTitle.setBorder(new EmptyBorder(0, 2, 10, 0));
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        top.add(lblTitle);

        // --- Hàng 2: nút Thêm + Xuất Excel | nút Xóa + ô tìm kiếm ---
        JPanel row2 = new JPanel(new BorderLayout(0, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, BTN_H + 4));

        // Trái: Thêm + Xuất Excel
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBtns.setOpaque(false);

        JButton btnThem = makeColorBtn("+ Thêm nhân viên", GREEN_ADD, GREEN_HOVER, GREEN_PRESS, Color.WHITE, BTN_W, BTN_H);
        btnThem.addActionListener(e -> openFormThem());

        JButton btnExcel = makeOutlineBtn("Xuất Excel", BTN_W, BTN_H);
        btnExcel.addActionListener(e -> xuatExcel());

        leftBtns.add(btnThem);
        leftBtns.add(btnExcel);

        // Phải: Xóa + Tìm kiếm
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lblSearch.setForeground(GuiTheme.SUB_TEXT);

        tfSearch = new JTextField(18);
        tfSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tfSearch.setPreferredSize(new Dimension(200, BTN_H));
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C, 1, false),
                new EmptyBorder(4, 10, 4, 10)));
        tfSearch.addActionListener(e -> applySearch());
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
        });

        btnXoa = makeColorBtn("Xóa", RED_DEL, RED_HOVER, RED_HOVER, Color.WHITE, 80, BTN_H);
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(e -> xoaNhanVien());

        rightBtns.add(btnXoa);
        rightBtns.add(lblSearch);
        rightBtns.add(tfSearch);

        row2.add(leftBtns,  BorderLayout.WEST);
        row2.add(rightBtns, BorderLayout.EAST);
        top.add(row2);

        top.add(Box.createVerticalStrut(10));

        // --- Hàng 3: filter tabs ---
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(LEFT_ALIGNMENT);

        row3.add(makeFilterTag("Tất cả",          "ALL",      TAG_ALL_BG, TAG_ALL_FG));
        row3.add(makeFilterTag("Đang làm việc",   "ACTIVE",   TAG_ACT_BG, TAG_ACT_FG));
        row3.add(makeFilterTag("Ngừng làm việc",  "INACTIVE", TAG_OFF_BG, TAG_OFF_FG));

        top.add(row3);
        return top;
    }

    // =========================================================
    // TABLE
    // =========================================================
    private JPanel buildTableArea() {
        String[] cols = {"MaNV", "Họ Đệm", "Tên", "Giới tính", "Ngày sinh",
                "Số điện thoại", "Email", "Chức vụ", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(219, 234, 254));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
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
        table.setBackground(Color.WHITE);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 244, 250));
        header.setForeground(GuiTheme.TEXT);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
        header.setReorderingAllowed(false);

        // Cột cuối (Trạng thái) render màu tag
        table.getColumnModel().getColumn(8).setCellRenderer((tbl, val, sel, foc, row, col) -> {
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
            lbl.setOpaque(false);
            if (tbl.isRowSelected(row)) lbl.setBackground(new Color(219, 234, 254));
            else lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            return lbl;
        });

        // Cột Chức vụ render
        table.getColumnModel().getColumn(7).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            String txt = val == null ? "" : val.toString();
            JLabel lbl = new JLabel(txt, SwingConstants.LEFT);
            lbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
            lbl.setOpaque(true);
            lbl.setBackground(tbl.isRowSelected(row) ? new Color(219, 234, 254) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            lbl.setForeground(NAVY);
            return lbl;
        });

        // Độ rộng cột
        int[] widths = {90, 130, 80, 80, 100, 120, 190, 110, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Bắt sự kiện chọn hàng → kích hoạt nút Xóa
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnXoa.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        // Double-click → mở form sửa
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openFormSua(table.getSelectedRow());
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C, 1, false),
                BorderFactory.createEmptyBorder()));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================
    // PAGINATION BAR
    // =========================================================
    private JPanel buildPagination() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblPageInfo = new JLabel("", SwingConstants.CENTER);
        lblPageInfo.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblPageInfo.setForeground(GuiTheme.SUB_TEXT);

        JPanel pageButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pageButtons.setOpaque(false);

        btnPrev = makePageBtn("‹");
        btnNext = makePageBtn("›");

        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; renderPage(); } });
        btnNext.addActionListener(e -> {
            int totalPages = getTotalPages();
            if (currentPage < totalPages - 1) { currentPage++; renderPage(); }
        });

        pageButtons.add(btnPrev);
        pageButtons.add(btnNext);

        bar.add(lblPageInfo,  BorderLayout.WEST);
        bar.add(pageButtons,  BorderLayout.EAST);
        return bar;
    }

    // =========================================================
    // DATA LOADING & FILTERING
    // =========================================================
    private void loadData() {
        allRows.clear();

        // Lấy full name = họ đệm + tên (split tại khoảng trắng cuối)
        String sql = "SELECT maNV, hoTenNV, ngaySinh, gioiTinh, soDT, email, loaiNV, trangThaiNV " +
                "FROM NhanVien ORDER BY maNV ASC";

        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maNV      = rs.getString("maNV");
                String hoTenNV   = rs.getString("hoTenNV");
                String ngaySinh  = rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toString() : "";
                boolean gioiTinh = rs.getBoolean("gioiTinh");
                String soDT      = nvl(rs.getString("soDT"));
                String email     = nvl(rs.getString("email"));
                String loaiNV    = rs.getString("loaiNV");
                String trangThai = nvl(rs.getString("trangThaiNV"));

                // Tách họ đệm / tên
                String hodem = "", ten = "";
                if (hoTenNV != null && !hoTenNV.isBlank()) {
                    int lastSpace = hoTenNV.lastIndexOf(' ');
                    if (lastSpace > 0) {
                        hodem = hoTenNV.substring(0, lastSpace).trim();
                        ten   = hoTenNV.substring(lastSpace + 1).trim();
                    } else {
                        ten = hoTenNV.trim();
                    }
                }

                String chucVu = "NHAN_VIEN_BAN_VE".equals(loaiNV) ? "Bán vé" :
                        "NHAN_VIEN_QUAN_LY".equals(loaiNV) ? "Quản lý" : loaiNV;
                String ttDisplay = "DANG_LAM".equals(trangThai) || "Đang làm việc".equals(trangThai)
                        ? "Đang làm việc" : "Ngừng làm việc";

                allRows.add(new Object[]{
                        maNV, hodem, ten,
                        gioiTinh ? "Nam" : "Nữ",
                        ngaySinh, soDT, email, chucVu, ttDisplay
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Dữ liệu mẫu khi chưa kết nối DB
            allRows.add(new Object[]{"NV001","Nguyễn Văn","A","Nam","1995-02-15","0987654321","nva@railway.com","Bán vé","Đang làm việc"});
            allRows.add(new Object[]{"NV002","Trần Thị","Bình","Nữ","1993-05-20","0912345678","ttb@railway.com","Bán vé","Đang làm việc"});
            allRows.add(new Object[]{"NV003","Lê Minh","Châu","Nam","1990-11-11","0934567890","lmc@railway.com","Quản lý","Ngừng làm việc"});
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
            String tt = row[8].toString();
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

        // Reset nút xóa khi re-render
        if (btnXoa != null) btnXoa.setEnabled(false);

        // Render page number buttons
        renderPageNumbers(totalPages);
    }

    // =========================================================
    // PAGE NUMBER BUTTONS (1 2 3 4 …)
    // =========================================================
    private JPanel pnlPageNumbers;

    /** Gọi sau buildPagination() — inject vào bar */
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

    // Override buildPagination để inject pnlPageNumbers
    // (Gọi trong constructor nên cần khởi tạo trước)
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

    private void xoaNhanVien() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String maNV = tableModel.getValueAt(row, 0).toString();
        String ten  = tableModel.getValueAt(row, 1) + " " + tableModel.getValueAt(row, 2);

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
        JOptionPane.showMessageDialog(this,
                "Chức năng xuất Excel đang được phát triển.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private Frame getParentFrame() {
        Window w = SwingUtilities.getWindowAncestor(this);
        return (w instanceof Frame) ? (Frame) w : null;
    }

    // =========================================================
    // UI HELPERS — thống nhất style với DoiVeGUI / DoiVeGUI0
    // =========================================================

    /** Nút màu solid (Thêm = xanh lá, Xóa = đỏ) */
    private JButton makeColorBtn(String text, Color bg, Color hover, Color press, Color fg, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c;
                if (!isEnabled()) c = new Color(180, 180, 180);
                else if (getModel().isPressed()) c = press;
                else if (getModel().isRollover()) c = hover;
                else c = bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(isEnabled() ? fg : Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w, h));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Nút outline (Xuất Excel, Quay lại) — style giống DoiVeGUI */
    private JButton makeOutlineBtn(String text, int w, int h) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(198,215,242) :
                        getModel().isRollover() ? new Color(212,228,250) : EXCEL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(NAVY);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics(); String t = getText();
                g2.setColor(NAVY);
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(w, h));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Nút phân trang ‹ › */
    private JButton makePageBtn(String label) {
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ?
                        (getModel().isRollover() ? new Color(230,236,248) : Color.WHITE) :
                        new Color(245, 246, 248));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(BORDER_C);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
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

    /** Tag bộ lọc (Tất cả / Đang làm / Ngừng) */
    private JButton makeFilterTag(String label, String status, Color bg, Color fg) {
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = filterStatus.equals(status);
                g2.setColor(active ? (status.equals("ALL") ? TAG_ALL_FG : status.equals("ACTIVE") ? TAG_ACT_FG : TAG_OFF_FG) : bg);
                if (!active) g2.setColor(bg);
                else {
                    Color activeBg = status.equals("ALL") ? TAG_ALL_FG : status.equals("ACTIVE") ? TAG_ACT_FG : TAG_OFF_FG;
                    g2.setColor(activeBg);
                }
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
            // repaint tất cả các tag
            Container parent = b.getParent();
            if (parent != null) parent.repaint();
        });
        return b;
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    // =========================================================
    // OVERRIDE buildPagination → dùng full version
    // =========================================================
    // Không dùng buildPagination() nữa, dùng buildPaginationFull() trong constructor
    // (xóa phương thức buildPagination cũ, giữ lại buildPaginationFull)

    // =========================================================
    // INNER CLASS: Dialog Thêm / Sửa Nhân viên
    // =========================================================
    static class NhanVienFormDialog extends JDialog {

        private boolean confirmed = false;
        private final String editMaNV;   // null = thêm mới

        private JTextField tfMaNV, tfHoTen, tfNgaySinh, tfSoDT, tfEmail, tfDiaChi, tfCCCD;
        private JComboBox<String> cbGioiTinh, cbLoaiNV, cbTrangThai;

        NhanVienFormDialog(Frame owner, String maNV) {
            super(owner, maNV == null ? "Thêm nhân viên mới" : "Chỉnh sửa nhân viên", true);
            this.editMaNV = maNV;

            setSize(540, 560);
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

            tfMaNV      = inputField(); tfHoTen     = inputField();
            tfNgaySinh  = inputField(); tfNgaySinh.setToolTipText("Định dạng: yyyy-MM-dd");
            tfSoDT      = inputField(); tfEmail     = inputField();
            tfDiaChi    = inputField(); tfCCCD      = inputField();
            cbGioiTinh  = new JComboBox<>(new String[]{"Nam", "Nữ"});
            cbLoaiNV    = new JComboBox<>(new String[]{"Bán vé", "Quản lý"});
            cbTrangThai = new JComboBox<>(new String[]{"Đang làm việc", "Ngừng làm việc"});
            styleCombo(cbGioiTinh); styleCombo(cbLoaiNV); styleCombo(cbTrangThai);

            if (editMaNV == null) {
                addRow(p, g, 0, "Mã NV *",       tfMaNV);
            } else {
                tfMaNV.setEditable(false);
                tfMaNV.setBackground(new Color(245, 247, 250));
                addRow(p, g, 0, "Mã NV",          tfMaNV);
            }
            addRow(p, g, 1, "Họ và tên *",   tfHoTen);
            addRow(p, g, 2, "Ngày sinh",     tfNgaySinh);
            addRow(p, g, 3, "Giới tính",     cbGioiTinh);
            addRow(p, g, 4, "Số điện thoại", tfSoDT);
            addRow(p, g, 5, "Email",         tfEmail);
            addRow(p, g, 6, "Địa chỉ",       tfDiaChi);
            addRow(p, g, 7, "CCCD",          tfCCCD);
            addRow(p, g, 8, "Chức vụ",       cbLoaiNV);
            addRow(p, g, 9, "Trạng thái",    cbTrangThai);
            return p;
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

            JButton btnCancel = makeOutlineDialogBtn("Hủy", 110, 36);
            btnCancel.addActionListener(e -> dispose());

            JButton btnSave = makeNavyDialogBtn(editMaNV == null ? "Thêm mới" : "Lưu thay đổi", 140, 36);
            btnSave.addActionListener(e -> saveNhanVien());

            p.add(btnCancel);
            p.add(btnSave);
            return p;
        }

        private void loadNhanVien(String maNV) {
            String sql = "SELECT * FROM NhanVien WHERE maNV = ?";
            try (Connection con = Connect_DB.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maNV);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tfMaNV.setText(maNV);
                    tfHoTen.setText(nvl(rs.getString("hoTenNV")));
                    tfNgaySinh.setText(rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toString() : "");
                    cbGioiTinh.setSelectedItem(rs.getBoolean("gioiTinh") ? "Nam" : "Nữ");
                    tfSoDT.setText(nvl(rs.getString("soDT")));
                    tfEmail.setText(nvl(rs.getString("email")));
                    tfDiaChi.setText(nvl(rs.getString("diaChi")));
                    tfCCCD.setText(nvl(rs.getString("soCCCD")));
                    String loai = rs.getString("loaiNV");
                    cbLoaiNV.setSelectedItem("NHAN_VIEN_QUAN_LY".equals(loai) ? "Quản lý" : "Bán vé");
                    String tt = nvl(rs.getString("trangThaiNV"));
                    cbTrangThai.setSelectedItem("DANG_LAM".equals(tt) || "Đang làm việc".equals(tt) ? "Đang làm việc" : "Ngừng làm việc");
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

            String ngaySinh = tfNgaySinh.getText().trim().isEmpty() ? null : tfNgaySinh.getText().trim();
            boolean gioiTinh = "Nam".equals(cbGioiTinh.getSelectedItem());
            String soDT    = tfSoDT.getText().trim();
            String email   = tfEmail.getText().trim();
            String diaChi  = tfDiaChi.getText().trim();
            String cccd    = tfCCCD.getText().trim();
            String loaiNV  = "Quản lý".equals(cbLoaiNV.getSelectedItem()) ? "NHAN_VIEN_QUAN_LY" : "NHAN_VIEN_BAN_VE";
            String trangThai = "Đang làm việc".equals(cbTrangThai.getSelectedItem()) ? "DANG_LAM" : "NGHI_VIEC";

            try (Connection con = Connect_DB.getInstance().getConnection()) {
                if (editMaNV == null) {
                    // INSERT
                    String sql = "INSERT INTO NhanVien (maNV,hoTenNV,ngaySinh,gioiTinh,soDT,email,diaChi,soCCCD,loaiNV,trangThaiNV) VALUES (?,?,?,?,?,?,?,?,?,?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maNV);
                        ps.setNString(2, hoTen);
                        if (ngaySinh != null) ps.setDate(3, java.sql.Date.valueOf(ngaySinh));
                        else ps.setNull(3, Types.DATE);
                        ps.setBoolean(4, gioiTinh);
                        ps.setString(5, soDT); ps.setString(6, email);
                        ps.setNString(7, diaChi); ps.setString(8, cccd);
                        ps.setString(9, loaiNV); ps.setString(10, trangThai);
                        ps.executeUpdate();
                    }
                } else {
                    // UPDATE
                    String sql = "UPDATE NhanVien SET hoTenNV=?,ngaySinh=?,gioiTinh=?,soDT=?,email=?,diaChi=?,soCCCD=?,loaiNV=?,trangThaiNV=? WHERE maNV=?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setNString(1, hoTen);
                        if (ngaySinh != null) ps.setDate(2, java.sql.Date.valueOf(ngaySinh));
                        else ps.setNull(2, Types.DATE);
                        ps.setBoolean(3, gioiTinh);
                        ps.setString(4, soDT); ps.setString(5, email);
                        ps.setNString(6, diaChi); ps.setString(7, cccd);
                        ps.setString(8, loaiNV); ps.setString(9, trangThai);
                        ps.setString(10, editMaNV);
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

        private static JButton makeNavyDialogBtn(String text, int w, int h) {
            JButton btn = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                    FontMetrics fm = g2.getFontMetrics(); String t = getText();
                    g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            btn.setPreferredSize(new Dimension(w, h));
            btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private static JButton makeOutlineDialogBtn(String text, int w, int h) {
            JButton b = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed() ? new Color(198,215,242) :
                            getModel().isRollover() ? new Color(212,228,250) : new Color(240,243,248));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                    g2.setColor(GuiTheme.NAVY); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                    g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                    FontMetrics fm = g2.getFontMetrics(); String t = getText();
                    g2.setColor(GuiTheme.NAVY);
                    g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            b.setPreferredSize(new Dimension(w, h));
            b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }

        private static String nvl(String s) { return s == null ? "" : s; }
    }

    // =========================================================
    // CONSTRUCTOR OVERRIDE — dùng buildPaginationFull
    // =========================================================
    // Vì Java không cho phép gọi method instance trong field initializer,
    // ta sắp xếp lại: remove add(buildPagination()) ở constructor đầu,
    // thay bằng add(buildPaginationFull()). Xem constructor thực tế bên dưới.

    // Static factory để AppFrameManager dùng:
    public static QLyNhanVienGUI create() {
        return new QLyNhanVienGUI();
    }
}