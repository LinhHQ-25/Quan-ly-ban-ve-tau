package gui;

import dao.CauHinhGiaDAO;
import dao.GaDAO;
import dao.ToaTauDAO;
import entity.Ga;
import entity.LoaiToa;
import entity.ToaTau;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CauHinhHeThongGUI extends JPanel {

    private static final Color NAVY    = new Color(28, 57, 110);
    private static final Color BG      = new Color(242, 247, 252);
    private static final Color BORDER_C = new Color(180, 205, 230);
    private static final java.awt.Font FONT_14  = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);
    private static final java.awt.Font FONT_B14 = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
    private static final java.awt.Font FONT_13  = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13);
    private static final java.awt.Font FONT_B13 = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);
    private static final DecimalFormat DF = new DecimalFormat("#,###");

    private final CauHinhGiaDAO cauHinhDAO = new CauHinhGiaDAO();
    private final GaDAO         gaDAO      = new GaDAO();
    private final ToaTauDAO     toaTauDAO  = new ToaTauDAO();

    // Giá cơ bản
    private JLabel lblGiaHienTai;
    private double giaCoBanHienTai = 300000;

    // Bảng hệ số loại toa — group by loaiToa, lấy heSo trung bình
    private DefaultTableModel modelToa;
    // Bảng hệ số cự ly ga
    private DefaultTableModel modelGa;

    // Cache dữ liệu
    private List<ToaTau> dsToa = new ArrayList<>();
    private List<Ga>     dsGa  = new ArrayList<>();

    public CauHinhHeThongGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 0, 16));

        add(buildContent(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        loadData();
    }

    // ── Load dữ liệu từ DB ───────────────────────────────────────────────────

    private void loadData() {
        giaCoBanHienTai = cauHinhDAO.getGiaCoBan();
        lblGiaHienTai.setText(DF.format(giaCoBanHienTai) + " VNĐ");

     // Hệ số loại ghế — load từ DB
        modelToa.setRowCount(0);
        String sqlToa = "SELECT g.loaiGhe, AVG(t.heSoLoaiToa) as heSo " +
                        "FROM Ghe g JOIN ToaTau t ON g.maToaTau = t.maToaTau " +
                        "GROUP BY g.loaiGhe";
        try (java.sql.Connection con = connect_DB.Connect_DB.getInstance().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sqlToa);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String loaiEnum = rs.getString("loaiGhe");
                double heSo = rs.getDouble("heSo");
                String tenLoai = switch (loaiEnum) {
                    case "GHE_CUNG"   -> "Ghế cứng";
                    case "GHE_MEM"    -> "Ghế mềm";
                    case "GIUONG_NAM" -> "Giường nằm";
                    default -> loaiEnum;
                };
                long viDuGia = Math.round(giaCoBanHienTai * 1.2 * heSo);
                modelToa.addRow(new Object[]{
                    tenLoai, loaiEnum,
                    String.format("%.2f", heSo),
                    DF.format(viDuGia) + " đ"
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        // Hệ số cự ly ga
        dsGa = gaDAO.selectAll();
        modelGa.setRowCount(0);
        for (Ga ga : dsGa) {
            modelGa.addRow(new Object[]{
                ga.getMaGa(), ga.getTenGa(),
                String.format("%.2f", ga.getHeSoCuLy())
            });
        }
    }

    // ── Build UI ─────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();

        // Row 0: Giá cơ bản (full width)
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        g.weightx = 1; g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);
        p.add(buildGiaCoBanCard(), g);

        // Row 1: Bảng toa (trái) + Bảng ga (phải)
        g.gridwidth = 1; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        g.gridx = 0; g.gridy = 1; g.weightx = 0.42; g.insets = new Insets(0, 0, 0, 8);
        p.add(buildToaCard(), g);

        g.gridx = 1; g.weightx = 0.58; g.insets = new Insets(0, 0, 0, 0);
        p.add(buildGaCard(), g);

        return p;
    }

    private JPanel buildGiaCoBanCard() {
        JPanel card = buildSectionCard("Giá vé cơ bản");

        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        body.setOpaque(false);

        // Giá hiện tại
        body.add(buildInfoBlock("Giá cơ bản hiện tại"));
        lblGiaHienTai = new JLabel("...");
        lblGiaHienTai.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        lblGiaHienTai.setForeground(NAVY);
        body.add(lblGiaHienTai);

        body.add(Box.createHorizontalStrut(24));

        // Công thức
        JLabel lblFormula = new JLabel(
            "<html><font color='#606060' size='3'>Công thức: </font>" +
            "<b>Giá vé = Giá cơ bản × Hệ số cự ly × Hệ số loại toa</b></html>");
        lblFormula.setFont(FONT_13);
        body.add(lblFormula);

        body.add(Box.createHorizontalStrut(24));

        // Nút sửa giá cơ bản
        JButton btnSua = makeNavyBtn("Cập nhật giá cơ bản");
        btnSua.addActionListener(e -> suaGiaCoBan());
        body.add(btnSua);

        card.add(body, BorderLayout.CENTER);
        return card;
    }
    private JPanel buildToaCard() {
        JPanel card = buildSectionCard("Hệ số loại toa");

        // 4 cột — cột index 1 sẽ bị ẩn
        String[] cols = {"Loại toa", "_enum", "Hệ số", "Ví dụ giá (cự ly 1.2)"};
        modelToa = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = buildTable(modelToa);

        // Ẩn cột enum
        tbl.getColumnModel().getColumn(1).setMinWidth(0);
        tbl.getColumnModel().getColumn(1).setMaxWidth(0);
        tbl.getColumnModel().getColumn(1).setWidth(0);

        tbl.getColumnModel().getColumn(0).setPreferredWidth(100);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(60);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(150);

        tbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tbl.getSelectedRow() >= 0)
                    suaHeSoToa(tbl.getSelectedRow());
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        btnRow.setOpaque(false);
        JButton btn = makeOutlineBtn("Sửa hệ số đã chọn");
        btn.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng để sửa."); return; }
            suaHeSoToa(r);
        });
        btnRow.add(btn);

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(6, 10, 6, 10));
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        body.add(sp, BorderLayout.CENTER);
        body.add(btnRow, BorderLayout.SOUTH);

        JLabel note = new JLabel("* Double-click dòng để sửa nhanh");
        note.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        note.setForeground(Color.GRAY);
        note.setBorder(new EmptyBorder(0, 10, 6, 0));

        card.add(body, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildGaCard() {
        JPanel card = buildSectionCard("Hệ số cự ly theo ga");

        String[] cols = {"Mã ga", "Tên ga", "Hệ số cự ly"};
        modelGa = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tbl = buildTable(modelGa);
     // Xóa các dòng setMinWidth/setMaxWidth/setPreferredWidth cũ, thay bằng:
        TableColumnModel tcm = tbl.getColumnModel();
        int totalWidth = 580; // tổng width bảng ga
        tcm.getColumn(0).setPreferredWidth(totalWidth / 3); // Mã ga
        tcm.getColumn(1).setPreferredWidth(totalWidth / 3); // Tên ga
        tcm.getColumn(2).setPreferredWidth(totalWidth / 3); // Hệ số cự ly

        tbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tbl.getSelectedRow() >= 0)
                    suaHeSoGa(tbl.getSelectedRow());
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        btnRow.setOpaque(false);
        JButton btn = makeOutlineBtn("Sửa hệ số đã chọn");
        btn.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng để sửa."); return; }
            suaHeSoGa(r);
        });
        btnRow.add(btn);

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(6, 10, 6, 10));
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        body.add(sp, BorderLayout.CENTER);
        body.add(btnRow, BorderLayout.SOUTH);

        JLabel note = new JLabel("* Double-click dòng để sửa nhanh");
        note.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        note.setForeground(Color.GRAY);
        note.setBorder(new EmptyBorder(0, 10, 6, 0));

        card.add(body, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

        JButton btnRefresh = makeOutlineBtn("Tải lại");
        btnRefresh.addActionListener(e -> loadData());
        bar.add(btnRefresh);

        return bar;
    }

    // ── Dialogs sửa ──────────────────────────────────────────────────────────

    private void suaGiaCoBan() {
        String input = JOptionPane.showInputDialog(this,
            "Nhập giá vé cơ bản mới (VNĐ):",
            (long) giaCoBanHienTai);
        if (input == null) return;
        try {
            double giaMoi = Double.parseDouble(input.replaceAll("[^0-9]", ""));
            if (giaMoi <= 0) throw new NumberFormatException();
            if (cauHinhDAO.updateGiaCoBan(giaMoi)) {
                JOptionPane.showMessageDialog(this, "Cập nhật giá cơ bản thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void suaHeSoToa(int row) {
        String tenLoai  = modelToa.getValueAt(row, 0).toString();
        String loaiEnum = modelToa.getValueAt(row, 1).toString();
        String hienTai  = modelToa.getValueAt(row, 2).toString();

        String input = JOptionPane.showInputDialog(this,
            "Nhập hệ số mới cho " + tenLoai + ":", hienTai);
        if (input == null) return;
        try {
            double heMoi = Double.parseDouble(input.trim());
            if (heMoi <= 0) throw new NumberFormatException();

            String sql = "UPDATE t SET t.heSoLoaiToa = ? " +
                         "FROM ToaTau t " +
                         "JOIN Ghe g ON g.maToaTau = t.maToaTau " +
                         "WHERE g.loaiGhe = ?";
            try (java.sql.Connection con = connect_DB.Connect_DB.getInstance().getConnection();
                 java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, heMoi);
                ps.setString(2, loaiEnum);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Cập nhật thành công! (" + rows + " toa được cập nhật)");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Không tìm thấy toa phù hợp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Hệ số không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void suaHeSoGa(int row) {
        String maGa   = modelGa.getValueAt(row, 0).toString();
        String tenGa   = modelGa.getValueAt(row, 1).toString();
        String hienTai = modelGa.getValueAt(row, 2).toString();

        String input = JOptionPane.showInputDialog(this,
            "Nhập hệ số cự ly mới cho " + tenGa + ":", hienTai);
        if (input == null) return;
        try {
            double heMoi = Double.parseDouble(input.trim());
            if (heMoi <= 0) throw new NumberFormatException();

            Ga ga = gaDAO.selectById(maGa);
            if (ga == null) throw new Exception("Không tìm thấy ga");
            ga.setHeSoCuLy(heMoi);
            if (gaDAO.update(ga)) {
                JOptionPane.showMessageDialog(this, "Cập nhật hệ số cự ly thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Hệ số không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers UI ───────────────────────────────────────────────────────────

    private JPanel buildSectionCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(BORDER_C, 1, true));

        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(FONT_B14);
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(NAVY);
        lbl.setBorder(new EmptyBorder(7, 12, 7, 12));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable tbl = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(isRowSelected(row)
                    ? new Color(210, 228, 245)
                    : row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                c.setForeground(Color.BLACK);
                if (c instanceof JLabel) ((JLabel)c).setBorder(new EmptyBorder(4, 10, 4, 10));
                return c;
            }
        };
        tbl.setFont(FONT_13);
        tbl.setRowHeight(32);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.getTableHeader().setFont(FONT_B13);
        tbl.getTableHeader().setBackground(new Color(245, 248, 252));
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return tbl;
    }

    private JLabel buildInfoBlock(String label) {
        JLabel l = new JLabel(label + ": ");
        l.setFont(FONT_14);
        l.setForeground(new Color(80, 80, 80));
        return l;
    }

    private JButton makeNavyBtn(String text) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(18,42,85)
                    : getModel().isRollover() ? new Color(38,68,128) : NAVY);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_B14); b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(6,16,6,16));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeOutlineBtn(String text) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(220,230,245)
                    : getModel().isRollover() ? new Color(230,240,250) : new Color(242,247,252));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.setColor(NAVY);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_14); b.setForeground(NAVY);
        b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}