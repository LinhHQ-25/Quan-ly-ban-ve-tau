package gui;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import connect_DB.Connect_DB;

public class DatVeGUI extends JPanel {

    // {tên, dotX%, dotY%, labelX%, labelY%} 428x561
    private static final Object[][] GA_DATA = {
        {"Ngọc Hồi",   0.425, 0.168,   0.621, 0.169},
        {"Phủ Lý",     0.442, 0.193,   0.551, 0.193},
        {"Nam Định",   0.467, 0.206,   0.530, 0.214},
        {"Ninh Bình",  0.456, 0.223,   0.507, 0.235},
        {"Thanh Hóa",  0.439, 0.253,   0.469, 0.264},
        {"Vinh",       0.425, 0.319,   0.460, 0.317},
        {"Hà Tĩnh",    0.456, 0.344,   0.493, 0.337},
        {"Vũng Áng",   0.491, 0.367,   0.516, 0.362},
        {"Đồng Hới",   0.512, 0.406,   0.544, 0.398},
        {"Đông Hà",    0.551, 0.442,   0.577, 0.426},
        {"Huế",        0.589, 0.469,   0.614, 0.453},
        {"Đà Nẵng",    0.638, 0.496,   0.664, 0.478},
        {"Tam Kỳ",     0.668, 0.531,   0.696, 0.524},
        {"Quảng Ngãi", 0.701, 0.565,   0.734, 0.561},
        {"Bồng Sơn",   0.715, 0.601,   0.750, 0.602},
        {"Diêu Trì",   0.724, 0.642,   0.762, 0.647},
        {"Tuy Hòa",    0.734, 0.697,   0.766, 0.693},
        {"Khánh Hòa",  0.724, 0.743,   0.771, 0.749},
        {"Tháp Chàm",  0.720, 0.788,   0.771, 0.791},
        {"Phan Rí",    0.687, 0.822,   0.713, 0.838},
        {"Long Thành", 0.568, 0.848,   0.643, 0.873},
        {"Thủ Thiêm",  0.521, 0.857,   0.600, 0.907}
    };

    private static final String GA_DI_MAC_DINH   = "Diêu Trì";
    private static final Color  CLR_ROUTE_NORMAL = new Color(30,  100, 190, 210);
    private static final Color  CLR_ROUTE_SEL    = new Color(220,  55,  40, 230);
    private static final Color  CLR_DOT_DI       = new Color(215,  60,  45);
    private static final Color  CLR_DOT_DEN      = new Color( 30, 115, 205);

    private JTextField        txtGaDi;
    private JTextField        txtGaDen;
    private JComboBox<String> cbGaDen;
    private JRadioButton      rbMotChieu, rbKhuHoi;
    private JDateChooser      dcNgayDi, dcNgayVe;
    private JTextField        txtSoLuong;
    private MapPanel          mapPanel;

    private String[]   gaDenList;
    private JPopupMenu autocompletePopup;
    private JList<String>            suggestList;
    private DefaultListModel<String> suggestModel;
    private boolean suppressDocListener = false;

    public DatVeGUI() {
        setLayout(new GridLayout(1, 2, 0, 0));
        setBackground(Color.WHITE);
        add(buildLeftPanel());
        add(buildRightPanel());
    }

    private JPanel buildLeftPanel() {
        mapPanel = new MapPanel();
        mapPanel.setOnGaSelected(gaName -> {
            if (!gaName.equals(GA_DI_MAC_DINH) && txtGaDen != null)
                selectGaDen(gaName);
        });
        return mapPanel;
    }

    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(242, 247, 252));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(36, 44, 28, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("Vui lòng điền/chọn thông tin vào đây", SwingConstants.CENTER);
        title.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 17));
        title.setForeground(new Color(45, 45, 45));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 26, 0);
        form.add(title, gbc);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        txtGaDi = new JTextField(GA_DI_MAC_DINH);
        txtGaDi.setEditable(false); txtGaDi.setEnabled(false);
        txtGaDi.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        addRow(form, gbc, 1, "Ga đi:", wrapField(txtGaDi, true), 14);

        gaDenList = buildGaDenList();
        cbGaDen = new JComboBox<>(gaDenList);
        cbGaDen.setVisible(false);
        cbGaDen.setSelectedIndex(-1); 
        addRow(form, gbc, 2, "Ga đến:", buildGaDenAutocomplete(), 14);

        rbMotChieu = new JRadioButton("Một chiều", true);
        rbKhuHoi   = new JRadioButton("Khứ hồi");
        styleRadio(rbMotChieu); styleRadio(rbKhuHoi);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMotChieu); bg.add(rbKhuHoi);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(rbMotChieu);
        radioPanel.add(Box.createHorizontalStrut(20));
        radioPanel.add(rbKhuHoi);
        rbMotChieu.addActionListener(e -> syncNgayVe());
        rbKhuHoi  .addActionListener(e -> setNgayVeEnabled(true));
        addRow(form, gbc, 3, "Loại vé:", radioPanel, 14);

        dcNgayDi = buildDateChooser(true);
        dcNgayDi.addPropertyChangeListener("date", evt -> {
            if (rbMotChieu != null && rbMotChieu.isSelected()) syncNgayVe();
        });
        addRow(form, gbc, 4, "Ngày đi:", wrapDC(dcNgayDi), 14);

        dcNgayVe = buildDateChooser(false);
        addRow(form, gbc, 5, "Ngày về:", wrapDC(dcNgayVe), 14);

        addRow(form, gbc, 6, "Số lượng:", buildSoLuongPanel(), 10);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrap.setOpaque(false);

        JButton btnTim = buildTimButton();
        btnTim.addActionListener(e -> openKetQua());
        btnWrap.add(btnTim);
        form.add(btnWrap, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    // --- HÀM KIỂM TRA DATABASE ---
    private boolean checkChuyenTonTai(String tenGaDi, String tenGaDen, String ngayDiStr) {
        String sql = "SELECT TOP 1 1 FROM ChuyenTau c " +
                     "JOIN Ga gDi ON c.gaDi = gDi.maGa " +
                     "JOIN Ga gDen ON c.gaDen = gDen.maGa " +
                     "WHERE gDi.tenGa LIKE ? AND gDen.tenGa LIKE ? " +
                     "AND CONVERT(VARCHAR, c.thoiGianKhoiHanh, 103) = ?";
        try (Connection con = Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setNString(1, "%" + tenGaDi + "%");
            ps.setNString(2, "%" + tenGaDen + "%");
            ps.setString(3, ngayDiStr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openKetQua() {
        String gaDen  = txtGaDen.getText().trim();
        if (gaDen.isEmpty()) {
        	JOptionPane.showMessageDialog(this, "Vui lòng chọn Ga đến!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        	return;
        }
        if (dcNgayDi.getDate() == null) {
        	JOptionPane.showMessageDialog(this, "Vui lòng chọn Ngày đi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        	return;
        }
        
        String loaiVe = rbMotChieu.isSelected() ? "Một chiều" : "Khứ hồi";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String ngayDi = sdf.format(dcNgayDi.getDate());
        
        String ngayVe = "";
        if (rbKhuHoi.isSelected() && dcNgayVe.getDate() != null) {
        	ngayVe = sdf.format(dcNgayVe.getDate());
        } else {
        	ngayVe = ngayDi;
        }

        // Kiểm tra khứ hồi phải chọn ngày về
        if (rbKhuHoi.isSelected() && dcNgayVe.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Ngày về!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra chuyến đi
        boolean coChuyenDi = checkChuyenTonTai(GA_DI_MAC_DINH, gaDen, ngayDi);

        // Nếu khứ hồi, kiểm tra thêm chuyến về
        if (rbKhuHoi.isSelected()) {
            boolean coChuyenVe = checkChuyenTonTai(gaDen, GA_DI_MAC_DINH, ngayVe);
            if (!coChuyenDi && !coChuyenVe) {
                JOptionPane.showMessageDialog(this,
                    "Không có chuyến tàu phù hợp cho cả chiều đi và chiều về!\n" +
                    "Chiều đi: " + ngayDi + " từ " + GA_DI_MAC_DINH + " đến " + gaDen + "\n" +
                    "Chiều về: " + ngayVe + " từ " + gaDen + " đến " + GA_DI_MAC_DINH,
                    "Không có chuyến", JOptionPane.WARNING_MESSAGE);
                return;
            } else if (!coChuyenDi) {
                JOptionPane.showMessageDialog(this,
                    "Không có chuyến tàu phù hợp cho chiều đi!\n" +
                    "Ngày " + ngayDi + " từ " + GA_DI_MAC_DINH + " đến " + gaDen,
                    "Không có chuyến đi", JOptionPane.WARNING_MESSAGE);
                return;
            } else if (!coChuyenVe) {
                JOptionPane.showMessageDialog(this,
                    "Không có chuyến tàu phù hợp cho chiều về!\n" +
                    "Ngày " + ngayVe + " từ " + gaDen + " đến " + GA_DI_MAC_DINH,
                    "Không có chuyến về", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        JPanel nextPanel;
        if (coChuyenDi) {
            // Mở trang kết quả có tàu
            nextPanel = new DatVeGUI1(
                GA_DI_MAC_DINH, gaDen, loaiVe, ngayDi, ngayVe, getSoLuong(),
                () -> swapBack()
            );
        } else {
            // ĐÃ SỬA: Mở trang báo không có tàu bằng class DatVeGUI0
            nextPanel = new DatVeGUI0(
                GA_DI_MAC_DINH, gaDen, loaiVe, ngayDi, ngayVe, getSoLuong(),
                () -> swapBack()
            );
        }

        Container parent = getParent();
        if (parent != null) {
            LayoutManager lm = parent.getLayout();
            if (lm instanceof CardLayout) {
                parent.add(nextPanel, "datveGUI_next");
                ((CardLayout) lm).show(parent, "datveGUI_next");
            } else {
                parent.remove(this);
                parent.add(nextPanel, BorderLayout.CENTER);
                parent.revalidate();
                parent.repaint();
            }
        }
    }

    private void swapBack() {
        Container parent = getParent();
        if (parent == null) return;
        LayoutManager lm = parent.getLayout();
        if (lm instanceof CardLayout) {
            ((CardLayout) lm).show(parent, "dat-ve");
        } else {
            for (Component c : parent.getComponents()) {
                // ĐÃ SỬA: Dọn dẹp cả DatVeGUI1 và DatVeGUI0 để tránh rác
                if (c instanceof DatVeGUI1 || c instanceof DatVeGUI0) {
                    parent.remove(c);
                }
            }
            parent.add(this, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        }
    }

    private JComponent buildGaDenAutocomplete() {
        txtGaDen = new JTextField("");
        txtGaDen.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtGaDen.setOpaque(false);
        txtGaDen.setBorder(null);
        txtGaDen.setForeground(new Color(50, 50, 50));
        txtGaDen.setCursor(new Cursor(Cursor.TEXT_CURSOR));

        JButton btnArrow = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getParent() != null ? getParent().getBackground() : Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                int cx = getWidth()/2, cy = getHeight()/2;
                int[] xs = {cx-5, cx+5, cx};
                int[] ys = {cy-3, cy-3, cy+3};
                g2.setColor(new Color(100, 130, 165));
                g2.fillPolygon(xs, ys, 3);
                g2.dispose();
            }
        };
        btnArrow.setPreferredSize(new Dimension(28, 28));
        btnArrow.setContentAreaFilled(false);
        btnArrow.setBorderPainted(false);
        btnArrow.setFocusPainted(false);
        btnArrow.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel gaDenWrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                boolean focused = txtGaDen.hasFocus();
                g2.setColor(focused ? new Color(80, 140, 210) : new Color(180, 205, 230));
                g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        gaDenWrap.setOpaque(false);
        gaDenWrap.setBorder(new EmptyBorder(6, 12, 6, 6));
        gaDenWrap.add(txtGaDen, BorderLayout.CENTER);
        gaDenWrap.add(btnArrow, BorderLayout.EAST);

        txtGaDen.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { gaDenWrap.repaint(); }
            @Override public void focusLost(FocusEvent e) {
                gaDenWrap.repaint();
                SwingUtilities.invokeLater(() -> {
                    if (autocompletePopup == null || !autocompletePopup.isVisible()) {
                        String cur = txtGaDen.getText().trim();
                        String sel = (String) cbGaDen.getSelectedItem();
                        boolean exact = Arrays.stream(gaDenList).anyMatch(g -> g.equalsIgnoreCase(cur));
                        if (!exact) {
                            suppressDocListener = true;
                            txtGaDen.setText(sel != null ? sel : "");
                            suppressDocListener = false;
                        }
                    }
                });
            }
        });

        suggestModel = new DefaultListModel<>();
        for (String g : gaDenList) suggestModel.addElement(g);

        suggestList = new JList<>(suggestModel);
        suggestList.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        suggestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestList.setFixedCellHeight(28);
        suggestList.setBorder(new EmptyBorder(2, 8, 2, 8));
        suggestList.setBackground(Color.WHITE);
        suggestList.setSelectionBackground(new Color(210, 228, 248));
        suggestList.setSelectionForeground(new Color(30, 70, 140));
        suggestList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String item  = value.toString();
                String query = suppressDocListener ? "" : txtGaDen.getText().trim();
                if (!query.isEmpty()) {
                    int pos = indexOfIgnoreAccent(item, query);
                    if (pos >= 0) {
                        String pre  = item.substring(0, pos);
                        String mid  = item.substring(pos, Math.min(pos + query.length(), item.length()));
                        String post = item.substring(Math.min(pos + query.length(), item.length()));
                        lbl.setText("<html>" + escHtml(pre)
                            + "<b><u>" + escHtml(mid) + "</u></b>"
                            + escHtml(post) + "</html>");
                    }
                }
                lbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
                return lbl;
            }
        });

        JScrollPane scrollGa = new JScrollPane(suggestList);
        scrollGa.setBorder(null);
        scrollGa.getVerticalScrollBar().setUnitIncrement(16);

        autocompletePopup = new JPopupMenu();
        autocompletePopup.setLayout(new BorderLayout());
        autocompletePopup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(160, 195, 230), 1),
            BorderFactory.createEmptyBorder(2, 0, 2, 0)));
        autocompletePopup.add(scrollGa, BorderLayout.CENTER);

        txtGaDen.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { onTextChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { onTextChanged(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        txtGaDen.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (!autocompletePopup.isVisible()) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER)
                        showAllSuggestions(gaDenWrap);
                    return;
                }
                int size = suggestModel.getSize();
                int idx  = suggestList.getSelectedIndex();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        suggestList.setSelectedIndex(Math.min(idx+1, size-1));
                        suggestList.ensureIndexIsVisible(suggestList.getSelectedIndex());
                        e.consume(); break;
                    case KeyEvent.VK_UP:
                        suggestList.setSelectedIndex(Math.max(idx-1, 0));
                        suggestList.ensureIndexIsVisible(suggestList.getSelectedIndex());
                        e.consume(); break;
                    case KeyEvent.VK_ENTER:
                        commitSelection(); e.consume(); break;
                    case KeyEvent.VK_ESCAPE:
                        autocompletePopup.setVisible(false); e.consume(); break;
                }
            }
        });

        suggestList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { commitSelection(); }
        });

        btnArrow.addActionListener(e -> {
            if (autocompletePopup.isVisible()) autocompletePopup.setVisible(false);
            else showAllSuggestions(gaDenWrap);
        });

        return gaDenWrap;
    }

    private void onTextChanged() {
        if (suppressDocListener) return;
        String query = txtGaDen.getText().trim();
        suggestModel.clear();
        if (query.isEmpty()) {
            for (String g : gaDenList) suggestModel.addElement(g);
        } else {
            List<String> prefix = new ArrayList<>(), contain = new ArrayList<>();
            for (String g : gaDenList) {
                int pos = indexOfIgnoreAccent(g, query);
                if (pos == 0) prefix.add(g); else if (pos > 0) contain.add(g);
            }
            for (String g : prefix)  suggestModel.addElement(g);
            for (String g : contain) suggestModel.addElement(g);
        }
        if (suggestModel.isEmpty()) { autocompletePopup.setVisible(false); return; }
        suggestList.setSelectedIndex(0);
        suggestList.ensureIndexIsVisible(0);
        Container parent = txtGaDen.getParent();
        if (parent instanceof JComponent) showPopupBelow((JComponent) parent);
    }

    private void showAllSuggestions(JComponent anchor) {
        suppressDocListener = true;
        suggestModel.clear();
        for (String g : gaDenList) suggestModel.addElement(g);
        suppressDocListener = false;
        String cur = (String) cbGaDen.getSelectedItem();
        int selIdx = cur != null ? indexInModel(cur) : 0;
        suggestList.setSelectedIndex(Math.max(selIdx, 0));
        suggestList.ensureIndexIsVisible(Math.max(selIdx, 0));
        showPopupBelow(anchor);
        txtGaDen.requestFocusInWindow();
    }

    private void showPopupBelow(JComponent anchor) {
        int popH = Math.min(suggestModel.getSize() * 28 + 6, 200);
        autocompletePopup.setPopupSize(anchor.getWidth(), popH);
        if (!autocompletePopup.isVisible()) {
            autocompletePopup.show(anchor, 0, anchor.getHeight());
            SwingUtilities.invokeLater(() -> txtGaDen.requestFocusInWindow());
        } else {
            autocompletePopup.setPopupSize(anchor.getWidth(), popH);
            autocompletePopup.revalidate(); autocompletePopup.repaint();
        }
    }

    private void commitSelection() {
        String sel = suggestList.getSelectedValue();
        if (sel == null && suggestModel.getSize() > 0) sel = suggestModel.get(0);
        if (sel != null) selectGaDen(sel);
        autocompletePopup.setVisible(false);
    }

    private void selectGaDen(String gaName) {
        suppressDocListener = true;
        txtGaDen.setText(gaName);
        suppressDocListener = false;
        cbGaDen.setSelectedItem(gaName);
        if (mapPanel != null) mapPanel.setSelectedGaDen(gaName);
        autocompletePopup.setVisible(false);
    }

    private int indexOfIgnoreAccent(String haystack, String needle) {
        String h = removeAccent(haystack.toLowerCase());
        String n = removeAccent(needle.toLowerCase());
        return h.indexOf(n);
    }

    private String removeAccent(String s) {
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                         .replace("đ", "d").replace("Đ", "D");
    }

    private String escHtml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private int indexInModel(String value) {
        for (int i = 0; i < suggestModel.getSize(); i++)
            if (suggestModel.get(i).equals(value)) return i;
        return -1;
    }

    private void syncNgayVe() {
        setNgayVeEnabled(false);
        if (dcNgayDi != null && dcNgayVe != null) dcNgayVe.setDate(dcNgayDi.getDate());
    }

    private JDateChooser buildDateChooser(boolean enabled) {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setDate(null); // Đã sửa: Để trống ngày
        dc.setEnabled(enabled);
        dc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        dc.setBackground(enabled ? Color.WHITE : new Color(225, 235, 245));
        dc.setBorder(null);
        Component editor = dc.getDateEditor().getUiComponent();
        if (editor instanceof JComponent) ((JComponent) editor).setBorder(null);
        return dc;
    }

    private JPanel wrapDC(JDateChooser dc) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dc.isEnabled() ? Color.WHITE : new Color(225, 235, 245));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 10, 4, 6));
        dc.setOpaque(false);
        p.add(dc, BorderLayout.CENTER);
        return p;
    }

    private String[] buildGaDenList() {
        List<String> list = new ArrayList<>();
        for (Object[] ga : GA_DATA)
            if (!ga[0].equals(GA_DI_MAC_DINH)) list.add((String) ga[0]);
        return list.toArray(new String[0]);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row,
                        String labelText, JComponent comp, int bottomGap) {
        gbc.gridy = row; gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.weightx = 0.34;
        gbc.insets = new Insets(0, 0, bottomGap, 0);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(55, 55, 55));
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.66;
        p.add(comp, gbc);
    }

    private JPanel wrapField(JTextField tf, boolean disabled) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(disabled ? new Color(220, 235, 248) : Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 12, 6, 12));
        tf.setOpaque(false); tf.setBorder(null);
        tf.setForeground(new Color(50, 50, 50));
        tf.setPreferredSize(new Dimension(0, 24));
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        rb.setForeground(new Color(50, 50, 50));
    }

    private void setNgayVeEnabled(boolean enabled) {
        dcNgayVe.setEnabled(enabled);
        dcNgayVe.setBackground(enabled ? Color.WHITE : new Color(225, 235, 245));
        Component editor = dcNgayVe.getDateEditor().getUiComponent();
        if (editor != null) editor.setEnabled(enabled);
        if (dcNgayVe.getParent() != null) dcNgayVe.getParent().repaint();
    }

    private JPanel buildSoLuongPanel() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(180, 205, 230));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(2, 6, 2, 6));

        JButton btnMinus = buildCountBtn("−");
        txtSoLuong = new JTextField("1", 3);
        txtSoLuong.setHorizontalAlignment(SwingConstants.CENTER);
        txtSoLuong.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtSoLuong.setOpaque(false);
        txtSoLuong.setBorder(null);
        txtSoLuong.setPreferredSize(new Dimension(38, 30));
        txtSoLuong.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) e.consume();
            }
        });
        txtSoLuong.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { clampSoLuong(); }
        });
        JButton btnPlus = buildCountBtn("+");

        btnMinus.addActionListener(e -> {
            int v = getSoLuong(); if (v > 1) txtSoLuong.setText(String.valueOf(v - 1));
        });
        btnPlus.addActionListener(e -> {
            int v = getSoLuong(); if (v < 99) txtSoLuong.setText(String.valueOf(v + 1));
        });

        wrapper.add(btnMinus,   BorderLayout.WEST);
        wrapper.add(txtSoLuong, BorderLayout.CENTER);
        wrapper.add(btnPlus,    BorderLayout.EAST);

        JPanel outer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        outer.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(110, 34));
        outer.add(wrapper);
        return outer;
    }

    public int getSoLuong() {
        try { return Math.max(1, Math.min(99, Integer.parseInt(txtSoLuong.getText().trim()))); }
        catch (NumberFormatException e) { return 1; }
    }

    private void clampSoLuong() { txtSoLuong.setText(String.valueOf(getSoLuong())); }

    private JButton buildCountBtn(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(200,218,240)
                    : getModel().isRollover() ? new Color(220,233,248) : Color.WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(80, 130, 180));
                g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label,(getWidth()-fm.stringWidth(label))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildTimButton() {
        JButton btn = new JButton("Tìm chuyến") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
                    : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt,(getWidth()-fm.stringWidth(txt))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private class MapPanel extends JPanel {
        private String   selectedGaDen = null;
        private String   hoveredGa     = null;
        private Consumer<String> onGaSelected;
        private Image    mapImage      = null;

        MapPanel() {
            setBackground(new Color(200, 225, 245));
            loadImage();
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    String hit = hitTest(e.getX(), e.getY());
                    if (!Objects.equals(hit, hoveredGa)) {
                        hoveredGa = hit;
                        boolean canClick = hit != null && !hit.equals(GA_DI_MAC_DINH);
                        setCursor(canClick ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    String hit = hitTest(e.getX(), e.getY());
                    if (hit != null && !hit.equals(GA_DI_MAC_DINH)) {
                        selectedGaDen = hit; repaint();
                        if (onGaSelected != null) onGaSelected.accept(hit);
                    }
                }
            });
        }

        void setOnGaSelected(Consumer<String> cb) { this.onGaSelected = cb; }
        void setSelectedGaDen(String ga)          { this.selectedGaDen = ga; repaint(); }

        private void loadImage() {
            for (String path : new String[]{"/Images/BanDo.png", "/BanDo.png"}) {
                try {
                    java.net.URL url = getClass().getResource(path);
                    if (url != null) { mapImage = new ImageIcon(url).getImage(); return; }
                } catch (Exception ignored) {}
            }
        }

        private Point toScreen(double xPct, double yPct) {
            return new Point((int)(xPct*getWidth()), (int)(yPct*getHeight()));
        }

        private String hitTest(int mx, int my) {
            for (Object[] ga : GA_DATA) {
                Point p = toScreen((double)ga[1], (double)ga[2]);
                if (Math.hypot(mx-p.x, my-p.y) <= 10) return (String) ga[0];
            }
            return null;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (mapImage != null) g2.drawImage(mapImage, 0, 0, getWidth(), getHeight(), this);
            else { g2.setColor(new Color(140,185,150)); g2.fillRect(0,0,getWidth(),getHeight()); }

            int idxDi = -1, idxDen = -1;
            for (int i = 0; i < GA_DATA.length; i++) {
                if (GA_DATA[i][0].equals(GA_DI_MAC_DINH)) idxDi = i;
                if (GA_DATA[i][0].equals(selectedGaDen))  idxDen = i;
            }
            int hlFrom = -1, hlTo = -1;
            if (idxDi >= 0 && idxDen >= 0) {
                hlFrom = Math.min(idxDi, idxDen); hlTo = Math.max(idxDi, idxDen);
            }

            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < GA_DATA.length-1; i++) {
                Point p1 = toScreen((double)GA_DATA[i][1],   (double)GA_DATA[i][2]);
                Point p2 = toScreen((double)GA_DATA[i+1][1], (double)GA_DATA[i+1][2]);
                g2.setColor((hlFrom>=0 && i>=hlFrom && i<hlTo) ? CLR_ROUTE_SEL : CLR_ROUTE_NORMAL);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            Font fNormal = new Font("Segoe UI", Font.PLAIN, 11);
            Font fBold   = new Font("Segoe UI", Font.BOLD,  11);

            for (Object[] ga : GA_DATA) {
                String name = (String)ga[0];
                Point  pt   = toScreen((double)ga[1], (double)ga[2]);
                boolean isDi = name.equals(GA_DI_MAC_DINH), isDen = name.equals(selectedGaDen),
                        isHov = name.equals(hoveredGa);
                int dotR; Color dotFill, dotBorder;
                if      (isDi)  { dotR=7; dotFill=CLR_DOT_DI;  dotBorder=new Color(150,25,10); }
                else if (isDen) { dotR=7; dotFill=CLR_DOT_DEN; dotBorder=new Color(10,55,135); }
                else if (isHov) { dotR=5; dotFill=new Color(110,175,235); dotBorder=new Color(40,90,160); }
                else            { dotR=4; dotFill=Color.WHITE;  dotBorder=new Color(40,90,160,170); }
                g2.setColor(dotBorder);
                g2.fillOval(pt.x-dotR-2, pt.y-dotR-2, (dotR+2)*2, (dotR+2)*2);
                g2.setColor(dotFill);
                g2.fillOval(pt.x-dotR, pt.y-dotR, dotR*2, dotR*2);
            }

            g2.setStroke(new BasicStroke(0.7f));
            for (Object[] ga : GA_DATA) {
                String name = (String)ga[0];
                Point  lp   = toScreen((double)ga[3], (double)ga[4]);
                boolean isDi = name.equals(GA_DI_MAC_DINH), isDen = name.equals(selectedGaDen);
                Font useFont = (isDi||isDen) ? fBold : fNormal;
                g2.setFont(useFont);
                FontMetrics fmu = g2.getFontMetrics(useFont);
                int ty = lp.y + fmu.getAscent()/2;
                g2.setColor(isDi ? new Color(165,25,8) : isDen ? new Color(15,65,155) : new Color(20,50,90));
                g2.drawString(name, lp.x, ty);
            }
            g2.dispose();
        }
    }
}