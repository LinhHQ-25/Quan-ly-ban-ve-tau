package gui;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;

public class QuanLyCaLamGUI extends JPanel {

    private static final Color NAVY = new Color(28, 57, 110);
    private static final Color HIGHLIGHT_TODAY = new Color(255, 245, 230);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private JTable tableCaLam;
    private DefaultTableModel modelCaLam;
    private JDateChooser dcChonNgay, dcNgayPhanCong;
    private JComboBox<String> cmbKieuXem, cmbNhanVien, cmbCaLam;
    private JButton btnXemLich, btnLuuPhanCong, btnLamMoi;
    
    private Map<String, String> nhanVienMap = new HashMap<>();

    public QuanLyCaLamGUI() {
        // DÒNG CODE VÀNG: Ép các Popup (bao gồm JDateChooser) phải dùng cửa sổ HĐH
        // Giúp nó tự động dội ngược lên trên khi đụng mép Taskbar dưới đáy màn hình
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // =========================================================
        // 1. TOP PANEL: BỘ LỌC TÌM KIẾM
        // =========================================================
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Bộ lọc Lịch làm việc", TitledBorder.LEFT, TitledBorder.TOP, FONT_TITLE, NAVY));

        pnlTop.add(createLabel("Kiểu xem:"));
        cmbKieuXem = new JComboBox<>(new String[]{"Theo Ngày", "Theo Tuần", "Theo Tháng"});
        cmbKieuXem.setFont(FONT_TEXT);
        cmbKieuXem.setSelectedIndex(1);
        pnlTop.add(cmbKieuXem);

        pnlTop.add(createLabel("   Chọn ngày mốc:"));
        dcChonNgay = new JDateChooser();
        dcChonNgay.setDate(new Date());
        dcChonNgay.setDateFormatString("dd/MM/yyyy");
        dcChonNgay.setPreferredSize(new Dimension(150, 30));
        dcChonNgay.setFont(FONT_TEXT);
        pnlTop.add(dcChonNgay);

        btnXemLich = createButton("Tải Lịch", "/Images/iconLoad.png", NAVY, Color.WHITE);
        btnXemLich.addActionListener(e -> loadMaTranLich());
        pnlTop.add(btnXemLich);

        add(pnlTop, BorderLayout.NORTH);

        // =========================================================
        // 2. CENTER PANEL: BẢNG LỊCH TRỰC
        // =========================================================
        modelCaLam = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableCaLam = new JTable(modelCaLam);
        tableCaLam.setRowHeight(90); 
        tableCaLam.setFont(FONT_TEXT);
        tableCaLam.setShowGrid(true);
        tableCaLam.setGridColor(new Color(220, 220, 220));
        tableCaLam.getTableHeader().setFont(FONT_BOLD);
        tableCaLam.getTableHeader().setBackground(new Color(240, 245, 250));
        tableCaLam.getTableHeader().setForeground(NAVY);
        tableCaLam.getTableHeader().setPreferredSize(new Dimension(100, 40));

        JScrollPane scrollPane = new JScrollPane(tableCaLam);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        add(scrollPane, BorderLayout.CENTER);

        // =========================================================
        // 3. BOTTOM PANEL: FORM PHÂN CÔNG (Vẫn nằm gọn ở đáy)
        // =========================================================
        JPanel pnlBottom = new JPanel(new BorderLayout()); 
        pnlBottom.setBackground(Color.WHITE);
        
        // Đã xóa cái viền 35px xấu xí
        pnlBottom.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Thao tác Phân công ca trực", TitledBorder.LEFT, TitledBorder.TOP, FONT_TITLE, NAVY));

        JPanel pnlForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlForm.setBackground(Color.WHITE);

        pnlForm.add(createLabel("Nhân viên:"));
        cmbNhanVien = new JComboBox<>();
        cmbNhanVien.setFont(FONT_TEXT);
        cmbNhanVien.setPreferredSize(new Dimension(200, 30));
        loadDanhSachNhanVienComboBox();
        pnlForm.add(cmbNhanVien);

        pnlForm.add(createLabel("Ngày trực:"));
        dcNgayPhanCong = new JDateChooser();
        dcNgayPhanCong.setDateFormatString("dd/MM/yyyy");
        dcNgayPhanCong.setPreferredSize(new Dimension(140, 30));
        dcNgayPhanCong.setFont(FONT_TEXT);
        dcNgayPhanCong.setMinSelectableDate(new Date()); // KHÓA NGÀY QUÁ KHỨ
        pnlForm.add(dcNgayPhanCong);

        pnlForm.add(createLabel("Ca làm:"));
        cmbCaLam = new JComboBox<>(new String[]{"Ca sáng (06:00 - 12:00)", "Ca chiều (12:00 - 18:00)"});
        cmbCaLam.setFont(FONT_TEXT);
        pnlForm.add(cmbCaLam);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBtns.setBackground(Color.WHITE);

        btnLuuPhanCong = createButton("Lưu phân công", "/Images/iconSave.png", new Color(34, 170, 70), Color.WHITE);
        btnLuuPhanCong.addActionListener(e -> xuLyLuuPhanCong());
        
        btnLamMoi = createButton("Làm mới", "/Images/iconRefresh.png", Color.GRAY, Color.WHITE);
        btnLamMoi.addActionListener(e -> lamMoiForm());
        
        pnlBtns.add(btnLamMoi); 
        pnlBtns.add(btnLuuPhanCong);

        pnlBottom.add(pnlForm, BorderLayout.CENTER);
        pnlBottom.add(pnlBtns, BorderLayout.EAST);
        
        add(pnlBottom, BorderLayout.SOUTH);

        // Tải lịch lần đầu và dọn sạch form
        loadMaTranLich();
        lamMoiForm();
    }

    private void lamMoiForm() {
        if (cmbNhanVien.getItemCount() > 0) cmbNhanVien.setSelectedIndex(-1);
        if (cmbCaLam.getItemCount() > 0) cmbCaLam.setSelectedIndex(-1);
        dcNgayPhanCong.setDate(new Date()); 
    }

    private void loadMaTranLich() {
        if (dcChonNgay.getDate() == null) return;
        
        String kieuXem = cmbKieuXem.getSelectedItem().toString();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dcChonNgay.getDate());
        
        ArrayList<Date> danhSachNgay = new ArrayList<>();

        if (kieuXem.equals("Theo Ngày")) {
            danhSachNgay.add(cal.getTime());
        } 
        else if (kieuXem.equals("Theo Tuần")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            for (int i = 0; i < 7; i++) {
                danhSachNgay.add(cal.getTime());
                cal.add(Calendar.DATE, 1);
            }
        } 
        else if (kieuXem.equals("Theo Tháng")) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < daysInMonth; i++) {
                danhSachNgay.add(cal.getTime());
                cal.add(Calendar.DATE, 1);
            }
        }

        SimpleDateFormat sdfTieuDe = new SimpleDateFormat("dd/MM");
        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd");

        String[] columnNames = new String[danhSachNgay.size() + 1];
        columnNames[0] = "Ca Làm Việc";
        String[] thuArr = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        for (int i = 0; i < danhSachNgay.size(); i++) {
            Calendar tmp = Calendar.getInstance();
            tmp.setTime(danhSachNgay.get(i));
            String tenThu = thuArr[tmp.get(Calendar.DAY_OF_WEEK) - 1];
            columnNames[i + 1] = tenThu + " (" + sdfTieuDe.format(danhSachNgay.get(i)) + ")";
        }
        modelCaLam.setColumnIdentifiers(columnNames);

        tableCaLam.getColumnModel().getColumn(0).setPreferredWidth(160); 
        for (int i = 1; i < tableCaLam.getColumnCount(); i++) {
            tableCaLam.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
        
        String todayStr = sdfTieuDe.format(new Date());
        setupTableRenderer(todayStr); 

        String startDate = sdfDB.format(danhSachNgay.get(0));
        String endDate = sdfDB.format(danhSachNgay.get(danhSachNgay.size() - 1));
        
        Map<String, String> lichMap = new HashMap<>(); 
        String sqlLich = "SELECT llv.maCa, llv.ngayLam, nv.hoTenNV " +
                         "FROM LichLamViec llv " +
                         "JOIN NhanVien nv ON llv.maNV = nv.maNV " +
                         "WHERE llv.ngayLam BETWEEN ? AND ?";

        try (Connection con = connect_DB.Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sqlLich)) {
            
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String key = rs.getString("maCa") + "_" + rs.getString("ngayLam");
                String currentStr = lichMap.getOrDefault(key, "");
                String newStr = rs.getString("hoTenNV");
                if(currentStr.isEmpty()) {
                    lichMap.put(key, newStr);
                } else {
                    lichMap.put(key, currentStr + "<br>" + newStr); 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        modelCaLam.setRowCount(0);
        Object[] rowSang = new Object[danhSachNgay.size() + 1];
        Object[] rowChieu = new Object[danhSachNgay.size() + 1];
        
        rowSang[0] = "<html><div style='text-align: center;'>Ca Sáng<br><span style='font-size: 10px; font-weight: normal; color: #555555;'>(06:00 - 12:00)</span></div></html>";
        rowChieu[0] = "<html><div style='text-align: center;'>Ca Chiều<br><span style='font-size: 10px; font-weight: normal; color: #555555;'>(12:00 - 18:00)</span></div></html>";

        for (int i = 0; i < danhSachNgay.size(); i++) {
            String checkDate = sdfDB.format(danhSachNgay.get(i));
            String textSang = lichMap.getOrDefault("C01_" + checkDate, "<i style='color:#a0a0a0;'>Trống</i>");
            String textChieu = lichMap.getOrDefault("C02_" + checkDate, "<i style='color:#a0a0a0;'>Trống</i>");
            
            rowSang[i + 1] = "<html><div style='text-align: center;'>" + textSang + "</div></html>";
            rowChieu[i + 1] = "<html><div style='text-align: center;'>" + textChieu + "</div></html>";
        }
        
        modelCaLam.addRow(rowSang);
        modelCaLam.addRow(rowChieu);
        
        if(kieuXem.equals("Theo Tháng")) tableCaLam.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        else tableCaLam.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void xuLyLuuPhanCong() {
        if (cmbNhanVien.getSelectedItem() == null || cmbCaLam.getSelectedItem() == null || dcNgayPhanCong.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ (Nhân viên, Ngày trực, Ca làm) trước khi lưu!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nvItem = cmbNhanVien.getSelectedItem().toString();
        String maNV = nvItem.split(" - ")[0];
        String maCa = cmbCaLam.getSelectedIndex() == 0 ? "C01" : "C02";
        Date ngayChon = dcNgayPhanCong.getDate();

        Calendar calChon = Calendar.getInstance();
        calChon.setTime(ngayChon);
        if (maCa.equals("C01")) {
            calChon.set(Calendar.HOUR_OF_DAY, 6);
            calChon.set(Calendar.MINUTE, 0);
        } else {
            calChon.set(Calendar.HOUR_OF_DAY, 12);
            calChon.set(Calendar.MINUTE, 0);
        }

        if (calChon.getTime().before(new Date())) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không được phân công ca trước thời điểm hiện tại!", "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd");
        String ngaySQL = sdfDB.format(ngayChon);

        try (Connection con = connect_DB.Connect_DB.getInstance().getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM LichLamViec WHERE maNV = ? AND maCa = ? AND ngayLam = ?";
            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                checkPs.setString(1, maNV);
                checkPs.setString(2, maCa);
                checkPs.setString(3, ngaySQL);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Lỗi: Nhân viên này đã được phân công vào ca này rồi!", "Bị trùng lặp", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            String maLich = "L" + System.currentTimeMillis(); 
            String insertSql = "INSERT INTO LichLamViec (maLich, maNV, maCa, ngayLam) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {
                insertPs.setString(1, maLich);
                insertPs.setString(2, maNV);
                insertPs.setString(3, maCa);
                insertPs.setString(4, ngaySQL);
                insertPs.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Phân công thành công!");
            dcChonNgay.setDate(ngayChon); 
            loadMaTranLich();
            lamMoiForm();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDanhSachNhanVienComboBox() {
        cmbNhanVien.removeAllItems();
        nhanVienMap.clear();
        String sql = "SELECT maNV, hoTenNV FROM NhanVien WHERE loaiNV = 'NHAN_VIEN_BAN_VE'";
        try (Connection con = connect_DB.Connect_DB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String val = rs.getString("maNV") + " - " + rs.getString("hoTenNV");
                cmbNhanVien.addItem(val);
                nhanVienMap.put(rs.getString("maNV"), rs.getString("hoTenNV"));
            }
        } catch (Exception e) {
            cmbNhanVien.addItem("NV001 - Hồ Quang Linh");
        }
    }

    private void setupTableRenderer(String todayStr) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setVerticalAlignment(SwingConstants.CENTER); 
                
                String colName = table.getColumnName(column);
                
                if (colName.contains(todayStr) && column > 0) {
                    lbl.setBackground(HIGHLIGHT_TODAY); 
                    lbl.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 2, new Color(255, 150, 50)));
                    lbl.setFont(FONT_TEXT);
                } else if (column == 0) {
                    lbl.setBackground(new Color(245, 245, 245));
                    lbl.setFont(FONT_BOLD);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setFont(FONT_TEXT); 
                    lbl.setBorder(null);
                }
                
                if (isSelected) lbl.setBackground(table.getSelectionBackground());
                return lbl;
            }
        };
        for (int i = 0; i < tableCaLam.getColumnCount(); i++) {
            tableCaLam.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TEXT);
        return lbl;
    }

    private JButton createButton(String text, String iconPath, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(FONT_BOLD);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));

        try {
            java.net.URL url = getClass().getResource(iconPath);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
                btn.setIconTextGap(8); 
            }
        } catch (Exception e) {}
        
        return btn;
    }
}