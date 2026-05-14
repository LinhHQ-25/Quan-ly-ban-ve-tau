package gui;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;
import entity.LoaiKhachHang;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiGUI extends JPanel {
    private final KhuyenMaiDAO kmDAO = new KhuyenMaiDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private final List<Object[]> allRows = new ArrayList<>();
    private JPopupMenu popupMenu;
    private int popupRow = -1;

    // --- CẤU HÌNH MAPPING LOẠI KHÁCH HÀNG ---
    private final String[] LOAI_KH_VN = {
        "Tất cả", 
        "Trẻ em (<6t)", 
        "Trẻ em (6-10t)", 
        "Sinh viên", 
        "Người lớn", 
        "Người cao tuổi"
    };
    private final LoaiKhachHang[] LOAI_KH_ENUM = {
        null, 
        LoaiKhachHang.DUOI_6_TUOI, 
        LoaiKhachHang.TU_6_TOI_DUOI_10, 
        LoaiKhachHang.SINH_VIEN, 
        LoaiKhachHang.NGUOI_LON, 
        LoaiKhachHang.TU_60_TRO_LEN
    };

    public KhuyenMaiGUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initPopupMenu();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        
        loadData();
    }

    private void initPopupMenu() {
        popupMenu = new JPopupMenu();
        JMenuItem itemSua = new JMenuItem(" Sửa thông tin");
        JMenuItem itemXoa = new JMenuItem(" Xóa khuyến mãi");
        itemXoa.setForeground(Color.RED);
        
        itemSua.addActionListener(e -> openFormSua(popupRow));
        itemXoa.addActionListener(e -> xoaKhuyenMai(popupRow));
        
        popupMenu.add(itemSua);
        popupMenu.add(new JSeparator());
        popupMenu.add(itemXoa);
    }

    private String getLoaiKHVn(LoaiKhachHang loai) {
        if (loai == null) return LOAI_KH_VN[0];
        for (int i = 1; i < LOAI_KH_ENUM.length; i++) {
            if (LOAI_KH_ENUM[i] == loai) return LOAI_KH_VN[i];
        }
        return LOAI_KH_VN[0];
    }

    private void loadData() {
        allRows.clear();
        List<KhuyenMai> list = kmDAO.selectAll();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (KhuyenMai km : list) {
            allRows.add(new Object[]{
                km.getMaKhuyenMai(),
                km.getTenKhuyenMai(),
                String.format("%.0f%%", km.getTiLeGiamGia() * 100),
                getLoaiKHVn(km.getLoaiKhachHang()), // Hiển thị Tiếng Việt
                km.getThoiGianBatDau() != null ? km.getThoiGianBatDau().format(dtf) : "",
                km.getThoiGianKetThuc() != null ? km.getThoiGianKetThuc().format(dtf) : "",
                km.getTrangThai() ? "Đang áp dụng" : "Ngừng áp dụng",
                "⋮" // Ký tự 3 chấm dọc
            });
        }
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Object[] row : allRows) tableModel.addRow(row);
    }

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        JLabel lblTitle = new JLabel("Quản lý Chương trình Khuyến mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        JButton btnAdd = new JButton("+ Thêm mới");
        btnAdd.setBackground(new Color(34, 197, 94));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> openFormThem());

        pnl.add(lblTitle, BorderLayout.WEST);
        pnl.add(btnAdd, BorderLayout.EAST);
        return pnl;
    }

    private JScrollPane buildTableArea() {
        String[] cols = {"Mã KM", "Tên khuyến mãi", "Tỉ lệ", "Đối tượng", "Bắt đầu", "Kết thúc", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        
        // --- RENDERER CHO DẤU 3 CHẤM (THAO TÁC) ---
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, "⋮", s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Dấu 3 chấm to lên
                lbl.setForeground(new Color(100, 116, 139));
                if (s) lbl.setForeground(Color.WHITE);
                return lbl;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r < 0) return;
                
                if (c == 7) { // Click vào cột 3 chấm
                    popupRow = r;
                    table.setRowSelectionInterval(r, r);
                    popupMenu.show(table, e.getX(), e.getY());
                }
            }
        });

        return new JScrollPane(table);
    }

    private void openFormThem() {
        new KhuyenMaiFormDialog(null).setVisible(true);
        loadData();
    }

    private void openFormSua(int row) {
        String id = tableModel.getValueAt(row, 0).toString();
        new KhuyenMaiFormDialog(id).setVisible(true);
        loadData();
    }

    private void xoaKhuyenMai(int row) {
        String id = tableModel.getValueAt(row, 0).toString();
        int opt = JOptionPane.showConfirmDialog(this, "Xóa khuyến mãi " + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            if (kmDAO.delete(id)) loadData();
        }
    }

    // =========================================================
    // INNER CLASS: Dialog Form
    // =========================================================
    class KhuyenMaiFormDialog extends JDialog {
        private JTextField tfMa = new JTextField(), tfTen = new JTextField(), tfTiLe = new JTextField();
        private JComboBox<String> cbLoai = new JComboBox<>(LOAI_KH_VN);
        private JCheckBox chkActive = new JCheckBox("Kích hoạt ngay", true);
        private String maEdit;

        public KhuyenMaiFormDialog(String ma) {
            this.maEdit = ma;
            setTitle(ma == null ? "Thêm khuyến mãi" : "Sửa khuyến mãi");
            setModal(true); setSize(400, 450); setLocationRelativeTo(null);
            setLayout(new GridLayout(0, 1, 10, 10));

            add(new JLabel(" Mã Khuyến Mãi:")); add(tfMa);
            add(new JLabel(" Tên Chương Trình:")); add(tfTen);
            add(new JLabel(" Tỉ lệ giảm (ví dụ: 0.1 cho 10%):")); add(tfTiLe);
            add(new JLabel(" Đối tượng áp dụng:")); add(cbLoai);
            add(chkActive);

            if (ma != null) {
                KhuyenMai km = kmDAO.selectById(ma);
                if (km != null) {
                    tfMa.setText(km.getMaKhuyenMai()); tfMa.setEditable(false);
                    tfTen.setText(km.getTenKhuyenMai());
                    tfTiLe.setText(String.valueOf(km.getTiLeGiamGia()));
                    cbLoai.setSelectedItem(getLoaiKHVn(km.getLoaiKhachHang()));
                    chkActive.setSelected(km.getTrangThai());
                }
            }

            JButton btnSave = new JButton("LƯU THÔNG TIN");
            btnSave.addActionListener(e -> {
                try {
                    KhuyenMai km = new KhuyenMai(
                        tfMa.getText(), tfTen.getText(), chkActive.isSelected(), "",
                        Double.parseDouble(tfTiLe.getText()),
                        LOAI_KH_ENUM[cbLoai.getSelectedIndex()],
                        LocalDateTime.now(), LocalDateTime.now().plusMonths(1)
                    );
                    if (maEdit == null) kmDAO.insert(km); else kmDAO.update(km);
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!");
                }
            });
            add(btnSave);
        }
    }
}