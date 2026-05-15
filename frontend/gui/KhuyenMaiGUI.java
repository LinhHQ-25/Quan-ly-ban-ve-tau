package gui;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;
import entity.LoaiKhachHang;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KhuyenMaiGUI extends JPanel {
    private final KhuyenMaiDAO kmDAO = new KhuyenMaiDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;
    private final List<Object[]> allRows = new ArrayList<>();
    private List<Object[]> filteredRows = new ArrayList<>();
    
    private int currentPage = 1;
    private final int rowsPerPage = 12;
    private JLabel lblPageInfo;
    private JPopupMenu popupMenu;
    private int popupRow = -1;

    public KhuyenMaiGUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(GuiTheme.LIGHT_BG);
        
        initPopupMenu();
        
        // Phần Header: Tiêu đề và nút Thêm
        add(buildHeader(), BorderLayout.NORTH);
        
        // Phần thân: Bảng dữ liệu
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, 20, 0, 20));
        pnlCenter.add(buildTableArea(), BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);
        
        // Phần đuôi: Phân trang
        add(buildPagination(), BorderLayout.SOUTH);
        
        loadData();
    }

    private void initPopupMenu() {
        popupMenu = new JPopupMenu();
        JMenuItem itemSua = new JMenuItem(" Sửa thông tin");
        JMenuItem itemXoa = new JMenuItem(" Xóa khuyến mãi");
        itemXoa.setForeground(Color.RED);
        
        itemSua.addActionListener(e -> { /* Logic mở form sửa */ });
        itemXoa.addActionListener(e -> handleActionDelete());
        
        popupMenu.add(itemSua);
        popupMenu.add(new JSeparator());
        popupMenu.add(itemXoa);
    }

    // Tự động dịch Enum sang Tiếng Việt để hiện lên bảng
    private String chuyenDoiLoaiKH(LoaiKhachHang loai) {
        if (loai == null) return "Tất cả";
        switch (loai) {
            case DUOI_6_TUOI:        return "Trẻ em < 6t";
            case TU_6_TOI_DUOI_10: return "Trẻ em 6-10t";
            case TU_60_TRO_LEN:     return "Người cao tuổi";
            case SINH_VIEN:         return "Sinh viên";
            case NGUOI_LON:         return "Người lớn";
            default:                return "Khác";
        }
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
                chuyenDoiLoaiKH(km.getLoaiKhachHang()), // Hiện tiếng Việt
                km.getThoiGianBatDau() != null ? km.getThoiGianBatDau().format(dtf) : "",
                km.getThoiGianKetThuc() != null ? km.getThoiGianKetThuc().format(dtf) : "",
                km.getTrangThai() ? "Đang áp dụng" : "Ngừng áp dụng",
                "⋮"
            });
        }
        applyFilters();
    }

    private void applyFilters() {
        String search = tfSearch.getText().toLowerCase().trim();
        filteredRows = allRows.stream().filter(row -> 
            row[0].toString().toLowerCase().contains(search) || 
            row[1].toString().toLowerCase().contains(search)
        ).collect(Collectors.toList());
        
        currentPage = 1;
        renderTable();
    }

    private void renderTable() {
        tableModel.setRowCount(0);
        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, filteredRows.size());
        for (int i = start; i < end; i++) tableModel.addRow(filteredRows.get(i));
        
        int totalPages = (int) Math.ceil((double) filteredRows.size() / rowsPerPage);
        lblPageInfo.setText(String.format("Trang %d / %d", currentPage, Math.max(1, totalPages)));
    }

    private JPanel buildHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 15));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(25, 20, 20, 20));

        // Dòng 1: Tiêu đề và Nút
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        JLabel lblTitle = new JLabel("Quản lý Chương trình Khuyến mãi");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(GuiTheme.NAVY);
        
        JButton btnAdd = new JButton("+ THÊM MỚI");
        btnAdd.setBackground(new Color(34, 197, 94));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(130, 38));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(btnAdd, BorderLayout.EAST);

        // Dòng 2: Thanh tìm kiếm
        tfSearch = new JTextField();
        tfSearch.setPreferredSize(new Dimension(350, 40));
        tfSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên khuyến mãi...");
        tfSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { applyFilters(); }
        });

        pnlHeader.add(pnlTop, BorderLayout.NORTH);
        pnlHeader.add(tfSearch, BorderLayout.WEST);
        
        return pnlHeader;
    }

    private JScrollPane buildTableArea() {
        String[] cols = {"Mã KM", "Tên chương trình", "Tỉ lệ", "Đối tượng", "Bắt đầu", "Kết thúc", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(48);
        table.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(240, 244, 255));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // CHỈNH CỘT 3 CHẤM (THAO TÁC)
        TableColumn actionCol = table.getColumnModel().getColumn(7);
        actionCol.setMaxWidth(80);
        actionCol.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, "⋮", s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 22)); 
                lbl.setForeground(new Color(148, 163, 184));
                return lbl;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 7) {
                    popupRow = r;
                    table.setRowSelectionInterval(r, r);
                    popupMenu.show(table, e.getX(), e.getY());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240)));
        return scroll;
    }

    private JPanel buildPagination() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        p.setOpaque(false);
        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        lblPageInfo = new JLabel("Trang 1 / 1");
        
        btnPrev.addActionListener(e -> { if(currentPage > 1) { currentPage--; renderTable(); } });
        btnNext.addActionListener(e -> {
            if(currentPage * rowsPerPage < filteredRows.size()) { currentPage++; renderTable(); }
        });

        p.add(btnPrev); p.add(lblPageInfo); p.add(btnNext);
        return p;
    }

    private void handleActionDelete() {
        if (popupRow == -1) return;
        String id = tableModel.getValueAt(popupRow, 0).toString();
        int opt = JOptionPane.showConfirmDialog(this, "Bạn muốn xóa " + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION && kmDAO.delete(id)) loadData();
    }
}