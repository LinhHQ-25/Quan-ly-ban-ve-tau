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
import java.util.*;
import java.util.List;

public class QLyNhanVienGUI extends JPanel {

    // ── CONSTANTS ────────────────────────────────────────────────
    private static final Color NAVY       = GuiTheme.NAVY;
    private static final Color LIGHT_BG   = GuiTheme.LIGHT_BG;
    private static final Color BORDER_C   = new Color(210, 215, 224);
    private static final Color RED_DEL    = new Color(220, 53, 69);
    private static final Color TAG_ALL_BG = new Color(228, 234, 255);
    private static final Color TAG_ALL_FG = new Color(60,  80,  180);
    private static final Color TAG_ACT_BG = new Color(220, 252, 231);
    private static final Color TAG_ACT_FG = new Color(22,  130,  60);
    private static final Color TAG_OFF_BG = new Color(255, 237, 213);
    private static final Color TAG_OFF_FG = new Color(180,  80,   0);

    // ── STATE ────────────────────────────────────────────────────
    private String filterStatus = "ALL";
    private String searchText   = "";

    // ── COMPONENTS ───────────────────────────────────────────────
    private JTable             table;
    private DefaultTableModel  tableModel;
    private JTextField         tfSearch;
    private JPopupMenu         popupMenu;
    private JMenuItem          menuSua, menuXoa;
    private int                popupRow = -1;
    private final List<Object[]> allRows = new ArrayList<>();

    // ── CONSTRUCTOR ──────────────────────────────────────────────
    public QLyNhanVienGUI() {
        setLayout(new BorderLayout());
        setBackground(LIGHT_BG);

        initPopupMenu();

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(buildTableArea(),  BorderLayout.CENTER);
        centerWrap.add(buildBottomBar(),  BorderLayout.SOUTH);

        add(buildTopBar(),  BorderLayout.NORTH);
        add(centerWrap,     BorderLayout.CENTER);

        loadData();
    }

    // ── POPUP MENU ───────────────────────────────────────────────
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
        popupMenu.addSeparator();
        popupMenu.add(menuXoa);
    }

    // ── TOP BAR ──────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 12, 0));

        // Filter tags
        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tagPanel.setOpaque(false);
        tagPanel.setBorder(new EmptyBorder(GuiTheme.PAGE_PAD_TOP, GuiTheme.PAGE_PAD_LEFT, 0, 0));
        tagPanel.add(makeFilterTag("Tất cả",         "ALL",      TAG_ALL_BG, TAG_ALL_FG));
        tagPanel.add(makeFilterTag("Đang làm việc",  "ACTIVE",   TAG_ACT_BG, TAG_ACT_FG));
        tagPanel.add(makeFilterTag("Ngừng làm việc", "INACTIVE", TAG_OFF_BG, TAG_OFF_FG));
        top.add(tagPanel, BorderLayout.WEST);

        // Search + buttons
        JPanel rightBar = new JPanel(new BorderLayout(12, 0));
        rightBar.setOpaque(false);
        rightBar.setBorder(new EmptyBorder(GuiTheme.PAGE_PAD_TOP, 0, 0, GuiTheme.PAGE_PAD_LEFT));

        tfSearch = new JTextField();
        tfSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        tfSearch.setPreferredSize(new Dimension(220, 38));
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C, 1, true), new EmptyBorder(4, 10, 4, 10)));
        tfSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applySearch(); }
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);
        JButton btnThem = makeRoundBtn("+ Thêm nhân viên", true, 160, 38);
        btnThem.addActionListener(e -> openFormThem());
        btnBar.add(tfSearch);
        btnBar.add(btnThem);

        rightBar.add(btnBar, BorderLayout.EAST);
        top.add(rightBar, BorderLayout.EAST);
        return top;
    }

    // ── BOTTOM BAR ───────────────────────────────────────────────
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(8, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        JButton btnExcel = makeRoundBtn("Xuất Excel", false, 120, 38);
        btnExcel.addActionListener(e -> xuatExcel());

        JPanel padBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        padBar.setOpaque(false);
        padBar.add(btnExcel);
        bar.add(padBar, BorderLayout.EAST);
        return bar;
    }

    // ── TABLE ────────────────────────────────────────────────────
    private JPanel buildTableArea() {
        String[] cols = {"Mã NV", "Họ và tên", "Giới tính", "Ngày sinh",
                "Số điện thoại", "Email", "Chức vụ", "Trạng thái", ""};
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

        table.setAutoCreateRowSorter(true);
        table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 233, 238));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 244, 250));
        header.setForeground(GuiTheme.TEXT);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));

        // Center renderer cho cột 0-5
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i <= 5; i++) table.getColumnModel().getColumn(i).setCellRenderer(center);

        // Render cột Chức vụ (6) - badge
        table.getColumnModel().getColumn(6).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            String txt = val == null ? "" : val.toString();
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER);
            lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
            lbl.setOpaque(true);
            boolean isSelected = tbl.isRowSelected(row);
            if (isSelected) {
                lbl.setBackground(new Color(219, 234, 254));
                lbl.setForeground(GuiTheme.TEXT);
            } else {
                lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                lbl.setForeground(GuiTheme.TEXT);
            }
            return lbl;
        });

        // Render cột Trạng thái (7) - badge màu rounded như KhuyenMaiGUI
        table.getColumnModel().getColumn(7).setCellRenderer((tbl, val, sel, foc, row, col) -> {
            String txt = val == null ? "" : val.toString();
            final Color bg = "Đang làm việc".equals(txt) ? TAG_ACT_BG : TAG_OFF_BG;
            final Color fg = "Đang làm việc".equals(txt) ? TAG_ACT_FG : TAG_OFF_FG;
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 8, 10, 10);
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
            lbl.setBackground(tbl.isRowSelected(row) ? new Color(219, 234, 254)
                    : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return lbl;
        });

        // Column widths
        int[] widths = {80, 160, 80, 100, 120, 160, 90, 130, 0};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (i == widths.length - 1) table.getColumnModel().getColumn(i).setMaxWidth(0);
        }

        // Mouse listener cho popup + double click
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) table.setRowSelectionInterval(row, row);
                if (SwingUtilities.isRightMouseButton(e) && row >= 0) {
                    popupRow = table.convertRowIndexToModel(row);
                    popupMenu.show(table, e.getX(), e.getY());
                }
                if (e.getClickCount() == 2 && row >= 0) {
                    openFormSua(table.convertRowIndexToModel(row));
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, 0, GuiTheme.PAGE_PAD_LEFT));
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // ── LOAD DATA ────────────────────────────────────────────────
    public void loadData() {
        allRows.clear();
        // Query đúng theo cấu trúc DB: NhanVien + TaiKhoan (ngayDangNhap, ngayDangXuat)
        String sql = "SELECT nv.maNV, nv.hoTenNV, nv.ngaySinh, nv.gioiTinh, " +
                "nv.soDT, nv.email, nv.loaiNV, " +
                "tk.ngayDangNhap, tk.ngayDangXuat " +
                "FROM NhanVien nv " +
                "LEFT JOIN TaiKhoan tk ON nv.maNV = tk.maNV " +
                "ORDER BY nv.maNV ASC";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maNV    = rs.getString("maNV");
                String hoTenNV = rs.getString("hoTenNV") == null ? "" : rs.getString("hoTenNV").trim();

                String ngaySinhStr = "";
                java.sql.Date sqlDate = rs.getDate("ngaySinh");
                if (sqlDate != null) ngaySinhStr = sdf.format(new java.util.Date(sqlDate.getTime()));

                boolean gioiTinh = rs.getBoolean("gioiTinh");
                String soDT      = nvl(rs.getString("soDT"));
                String email     = nvl(rs.getString("email"));
                String loaiNV    = rs.getString("loaiNV");

                // Chức vụ
                String chucVu = "NHAN_VIEN_BAN_VE".equals(loaiNV) ? "Bán vé"
                        : "NHAN_VIEN_QUAN_LY".equals(loaiNV) ? "Quản lý"
                          : (loaiNV != null ? loaiNV : "");

                // Trạng thái dựa trên ngayDangNhap vs ngayDangXuat
                Timestamp tLogin  = rs.getTimestamp("ngayDangNhap");
                Timestamp tLogout = rs.getTimestamp("ngayDangXuat");
                String ttDisplay;
                if (tLogin != null && (tLogout == null || tLogin.after(tLogout))) {
                    ttDisplay = "Đang làm việc";
                } else {
                    ttDisplay = "Ngừng làm việc";
                }

                allRows.add(new Object[]{
                        maNV, hoTenNV, gioiTinh ? "Nam" : "Nữ",
                        ngaySinhStr, soDT, email, chucVu, ttDisplay, ""
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            String tt = row[7].toString();
            if ("ACTIVE".equals(filterStatus)   && !"Đang làm việc".equals(tt))  continue;
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

    private void renderPage() {
        tableModel.setRowCount(0);
        for (Object[] row : getFilteredRows()) tableModel.addRow(row);
    }

    // ── ACTIONS ──────────────────────────────────────────────────
    private void openFormThem() {
        NhanVienFormDialog dialog = new NhanVienFormDialog(getParentFrame(), null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData();
    }

    private void openFormSua(int modelRow) {
        String maNV = tableModel.getValueAt(modelRow, 0).toString();
        NhanVienFormDialog dialog = new NhanVienFormDialog(getParentFrame(), maNV);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) loadData();
    }

    private void xoaNhanVien(int modelRow) {
        String maNV = tableModel.getValueAt(modelRow, 0).toString();
        String ten  = tableModel.getValueAt(modelRow, 1).toString();
        int choice  = JOptionPane.showConfirmDialog(this,
                "<html><b>Xóa nhân viên " + ten + " (" + maNV + ")?</b><br>" +
                        "Hành động này không thể hoàn tác.</html>",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM NhanVien WHERE maNV = ?")) {
            ps.setString(1, maNV);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Đã xóa nhân viên " + maNV + ".",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatExcel() {
        JOptionPane.showMessageDialog(this, "Chức năng xuất Excel đang phát triển.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Tự sinh mã NV: NV001, NV002, ... ────────────────────────
    static String sinhMaNVMoi() {
        Set<Integer> used = new HashSet<>();
        try (Connection con = Connect_DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT maNV FROM NhanVien WHERE maNV LIKE 'NV%'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                try { used.add(Integer.parseInt(rs.getString("maNV").substring(2))); }
                catch (Exception ignored) {}
            }
        } catch (Exception e) { e.printStackTrace(); }
        for (int i = 1; i <= 999; i++) {
            if (!used.contains(i)) return String.format("NV%03d", i);
        }
        return "NV001";
    }

    // ── HELPERS ──────────────────────────────────────────────────
    private Frame getParentFrame() {
        Container c = getParent();
        while (c != null && !(c instanceof Frame)) c = c.getParent();
        return (Frame) c;
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    static JButton makeRoundBtn(String text, boolean primary, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(primary ? Color.WHITE : GuiTheme.TEXT);
        btn.setBackground(primary ? GuiTheme.NAVY : Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(primary ? GuiTheme.NAVY : BORDER_C, 1, true),
                new EmptyBorder(0, 14, 0, 14)));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeFilterTag(String label, String status, Color bg, Color fg) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = filterStatus.equals(status);
                g2.setColor(active ? fg : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(active ? Color.WHITE : fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(130, 30));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            filterStatus = status;
            repaint();
            applySearch();
        });
        return btn;
    }

    // =========================================================
    // INNER CLASS: Dialog Thêm / Sửa Nhân viên
    // =========================================================
    static class NhanVienFormDialog extends JDialog {

        private boolean confirmed = false;
        private final String editMaNV;

        private JTextField    tfMaNV, tfHoTen, tfSoDT, tfEmail, tfDiaChi, tfCCCD;
        private JComboBox<String> cbGioiTinh, cbLoaiNV, cbTrangThai;
        private JDateChooser  dcNgaySinh;

        NhanVienFormDialog(Frame owner, String maNV) {
            super(owner, maNV == null ? "Thêm nhân viên mới" : "Chỉnh sửa nhân viên", true);
            this.editMaNV = maNV;
            setSize(540, 520);
            setResizable(false);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            getContentPane().setBackground(GuiTheme.LIGHT_BG);
            add(buildForm(),          BorderLayout.CENTER);
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

            tfMaNV     = inputField();
            tfHoTen    = inputField();
            dcNgaySinh = buildDateChooser();
            tfSoDT     = inputField();
            tfEmail    = inputField();
            tfDiaChi   = inputField();
            tfCCCD     = inputField();
            cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
            // Chức vụ chỉ Bán vé, disabled
            cbLoaiNV   = new JComboBox<>(new String[]{"Bán vé"});
            cbLoaiNV.setEnabled(false);
            cbLoaiNV.setBackground(new Color(245, 247, 250));

            styleCombo(cbGioiTinh);
            styleCombo(cbLoaiNV);

            if (editMaNV == null) {
                // Tự sinh mã NV
                tfMaNV.setText(QLyNhanVienGUI.sinhMaNVMoi());
                tfMaNV.setEditable(false);
                tfMaNV.setBackground(new Color(245, 247, 250));
            } else {
                tfMaNV.setEditable(false);
                tfMaNV.setBackground(new Color(245, 247, 250));
            }

            addRow(p, g, 0, "Mã NV",          tfMaNV);
            addRow(p, g, 1, "Họ và tên *",    tfHoTen);
            addRow(p, g, 2, "Ngày sinh",      dcNgaySinh);
            addRow(p, g, 3, "Giới tính",      cbGioiTinh);
            addRow(p, g, 4, "Số điện thoại",  tfSoDT);
            addRow(p, g, 5, "Email",          tfEmail);
            addRow(p, g, 6, "Địa chỉ",        tfDiaChi);
            addRow(p, g, 7, "CCCD",           tfCCCD);
            addRow(p, g, 8, "Chức vụ",        cbLoaiNV);

            // Trạng thái — chỉ hiển thị khi Sửa nhân viên
            if (editMaNV != null) {
                cbTrangThai = new JComboBox<>(new String[]{"Đang làm việc", "Ngừng làm việc"});
                styleCombo(cbTrangThai);
                addRow(p, g, 9, "Trạng thái", cbTrangThai);
            }

            return p;
        }

        private JDateChooser buildDateChooser() {
            JDateChooser dc = new JDateChooser();
            dc.setDateFormatString("dd/MM/yyyy");
            dc.setDate(null);
            dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
            dc.setBackground(Color.WHITE);
            dc.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(210, 215, 224), 1, false),
                    new EmptyBorder(2, 4, 2, 4)));
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
            JButton btnSave = QLyNhanVienGUI.makeRoundBtn(
                    editMaNV == null ? "Thêm mới" : "Lưu thay đổi", true, 140, 38);
            btnSave.addActionListener(e -> saveNhanVien());
            p.add(btnCancel);
            p.add(btnSave);
            return p;
        }

        private void loadNhanVien(String maNV) {
            String sql = "SELECT maNV, hoTenNV, ngaySinh, gioiTinh, soDT, email, diaChi, soCCCD, loaiNV " +
                    "FROM NhanVien WHERE maNV = ?";
            try (Connection con = Connect_DB.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maNV);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tfMaNV.setText(maNV);
                    tfHoTen.setText(nvl(rs.getString("hoTenNV")));
                    java.sql.Date sqlDate = rs.getDate("ngaySinh");
                    if (sqlDate != null) dcNgaySinh.setDate(new java.util.Date(sqlDate.getTime()));
                    cbGioiTinh.setSelectedItem(rs.getBoolean("gioiTinh") ? "Nam" : "Nữ");
                    tfSoDT.setText(nvl(rs.getString("soDT")));
                    tfEmail.setText(nvl(rs.getString("email")));
                    tfDiaChi.setText(nvl(rs.getString("diaChi")));
                    tfCCCD.setText(nvl(rs.getString("soCCCD")));
                    // cbLoaiNV disabled, luôn hiện Bán vé
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        private void saveNhanVien() {
            String maNV  = tfMaNV.getText().trim();
            String hoTen = tfHoTen.getText().trim();
            if (maNV.isEmpty() || hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Họ tên.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.util.Date uDate = dcNgaySinh.getDate();
            String ngaySinh = uDate != null ? new SimpleDateFormat("yyyy-MM-dd").format(uDate) : null;
            boolean gioiTinh = "Nam".equals(cbGioiTinh.getSelectedItem());
            String soDT  = tfSoDT.getText().trim();
            String email = tfEmail.getText().trim();
            String diaChi = tfDiaChi.getText().trim();
            String cccd   = tfCCCD.getText().trim();
            String loaiNV = "NHAN_VIEN_BAN_VE"; // luôn là bán vé

            try (Connection con = Connect_DB.getConnection()) {
                if (editMaNV == null) {
                    String sql = "INSERT INTO NhanVien (maNV, hoTenNV, ngaySinh, gioiTinh, soDT, email, diaChi, soCCCD, loaiNV) " +
                            "VALUES (?,?,?,?,?,?,?,?,?)";
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
                    String sql = "UPDATE NhanVien SET hoTenNV=?, ngaySinh=?, gioiTinh=?, soDT=?, email=?, diaChi=?, soCCCD=? " +
                            "WHERE maNV=?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setNString(1, hoTen);
                        if (ngaySinh != null) ps.setDate(2, java.sql.Date.valueOf(ngaySinh));
                        else ps.setNull(2, Types.DATE);
                        ps.setBoolean(3, gioiTinh);
                        ps.setString(4, soDT);
                        ps.setString(5, email);
                        ps.setNString(6, diaChi);
                        ps.setString(7, cccd);
                        ps.setString(8, editMaNV);
                        ps.executeUpdate();
                    }
                    // Cập nhật trạng thái trong TaiKhoan
                    boolean dangLamViec = "Đang làm việc".equals(cbTrangThai.getSelectedItem());
                    if (dangLamViec) {
                        // Đặt ngayDangNhap = NOW, ngayDangXuat = NULL → trạng thái ACTIVE
                        String sqlTK = "UPDATE TaiKhoan SET ngayDangNhap = GETDATE(), ngayDangXuat = NULL WHERE maNV = ?";
                        try (PreparedStatement ps2 = con.prepareStatement(sqlTK)) {
                            ps2.setString(1, editMaNV);
                            ps2.executeUpdate();
                        }
                    } else {
                        // Đặt ngayDangXuat = NOW (sau ngayDangNhap) → trạng thái INACTIVE
                        String sqlTK = "UPDATE TaiKhoan SET ngayDangXuat = GETDATE() WHERE maNV = ?";
                        try (PreparedStatement ps2 = con.prepareStatement(sqlTK)) {
                            ps2.setString(1, editMaNV);
                            ps2.executeUpdate();
                        }
                    }
                }
                confirmed = true;
                JOptionPane.showMessageDialog(this,
                        editMaNV == null ? "Thêm nhân viên thành công!" : "Cập nhật thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        private static String nvl(String s) { return s == null ? "" : s; }
    }
}