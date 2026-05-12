package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class HomeGUI extends JPanel {

    public HomeGUI() {
        setBackground(new Color(235, 238, 243));
        setLayout(new BorderLayout(10, 15)); 
        setBorder(new EmptyBorder(5, 10, 10, 10)); 

        // 1. TIÊU ĐỀ
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("HỒ SƠ NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(26, 46, 68)); 
        pnlTitle.add(lblTitle);
        add(pnlTitle, BorderLayout.NORTH);

        // 2. MAIN CONTENT (2 Card)
        JPanel pnlMain = new JPanel(new GridLayout(1, 2, 15, 0)); 
        pnlMain.setOpaque(false);
        pnlMain.add(buildPersonalInfoCard());
        pnlMain.add(buildScheduleCard());
        add(pnlMain, BorderLayout.CENTER);

        // 3. FOOTER
        add(buildFooterCard(), BorderLayout.SOUTH);
    }

    private JPanel buildPersonalInfoCard() {
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("THÔNG TIN CHI TIẾT CÁ NHÂN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 0.5;

        // Xóa các text placeholder rác, chuẩn hóa nhãn Tiếng Việt
        addFormDisplay(form, gbc, 0, 0, "Mã nhân viên", "NV001", 1);
        addFormDisplay(form, gbc, 1, 0, "Vai trò", "Bán vé", 1);
        
        addFormDisplay(form, gbc, 0, 1, "Họ và tên", "Trần Văn A", 2); 
        
        addFormDisplay(form, gbc, 0, 2, "Ngày sinh", "01/01/1990", 1);
        addFormDisplay(form, gbc, 1, 2, "Giới tính", "Nam", 1);
        
        addFormDisplay(form, gbc, 0, 3, "Số điện thoại", "0123456789", 1);
        addFormDisplay(form, gbc, 1, 3, "Email", "tranvana@train.com", 1);
        
        addFormDisplay(form, gbc, 0, 4, "Địa chỉ", "123 Đường ray, TP. HCM", 2); 

        card.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        RoundedPanel btnUpdate = new RoundedPanel(8, new Color(240, 240, 240));
        btnUpdate.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Nút cập nhật Font 14
        JLabel lblUpdate = new JLabel("Cập nhật thông tin");
        lblUpdate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUpdate.add(lblUpdate);
        
        btnPanel.add(btnUpdate);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    // Cỡ chữ Tiêu đề (Label) và Nội dung (Value) đều được ép cứng mức 14
    private void addFormDisplay(JPanel parent, GridBagConstraints gbc, int x, int y, String label, String value, int width) {
        gbc.gridx = x; gbc.gridy = y; gbc.gridwidth = width;
        
        JPanel wrapper = new JPanel(new BorderLayout(0, 5));
        wrapper.setOpaque(false);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); // FONT CHUẨN 14
        lbl.setForeground(Color.DARK_GRAY);
        wrapper.add(lbl, BorderLayout.NORTH);

        RoundedPanel inputBg = new RoundedPanel(8, new Color(250, 250, 250)); 
        inputBg.setLayout(new BorderLayout(10, 0));
        inputBg.setBorder(new EmptyBorder(8, 10, 8, 10)); // Canh lề chuẩn
        
        // Nhãn ẩn để dành chèn Icon sau, không có text rác
        JLabel iconLbl = new JLabel(); 
        inputBg.add(iconLbl, BorderLayout.WEST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // FONT CHUẨN 14
        valLbl.setForeground(Color.BLACK);
        inputBg.add(valLbl, BorderLayout.CENTER);
        
        wrapper.add(inputBg, BorderLayout.CENTER);
        parent.add(wrapper, gbc);
    }

    // =========================================================================
    // CARD 2: LỊCH LÀM VIỆC (CHỈNH FONT BẢNG LÊN 14)
    // =========================================================================
    private JPanel buildScheduleCard() {
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("LỊCH LÀM VIỆC TUẦN NÀY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"STT", "Mã ca", "Tên ca", "Bắt đầu", "Kết thúc", "Ngày"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        
        model.addRow(new Object[]{"1", "C01", "Ca sáng", "08:00", "16:00", "2026-05-18"});
        model.addRow(new Object[]{"2", "C02", "Ca chiều", "16:00", "00:00", "2026-05-19"});
        model.addRow(new Object[]{"3", "C01", "Ca sáng", "08:00", "16:00", "2026-05-20"});
        model.addRow(new Object[]{"4", "C01", "Ca sáng", "08:00", "16:00", "2026-05-21"});
        model.addRow(new Object[]{"5", "C02", "Ca chiều", "16:00", "00:00", "2026-05-22"});

        // Ép Font trong bảng lên 14
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28); 
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        
        // Ép Font Header bảng lên 14
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        header.setBackground(new Color(245, 245, 245));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        card.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        RoundedPanel btnExport = new RoundedPanel(8, new Color(240, 240, 240));
        btnExport.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Font xuất lịch cũng 14
        JLabel lblExport = new JLabel("Xuất lịch làm việc");
        lblExport.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExport.add(lblExport);
        
        btnPanel.add(btnExport);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================================
    // CARD 3: FOOTER THAO TÁC CA LÀM
    // =========================================================================
    private JPanel buildFooterCard() {
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 15, 15, 15));
        
        JLabel title = new JLabel("THAO TÁC CA LÀM LÕI & BÁO CÁO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel btnGrid = new JPanel(new GridLayout(1, 3, 15, 0));
        btnGrid.setOpaque(false);

        btnGrid.add(createFooterActionBtn("Đổi mật khẩu", ""));
        btnGrid.add(createFooterActionBtn("Chốt ca / Bàn giao ca", ""));
        btnGrid.add(createFooterActionBtn("Báo cáo sự cố hệ thống", "Sự cố mạng, Báo lỗi máy in"));

        card.add(btnGrid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFooterActionBtn(String title, String subTxt) {
        RoundedPanel btn = new RoundedPanel(12, new Color(238, 246, 255)); 
        btn.setLayout(new BorderLayout(15, 0)); 
        btn.setBorder(new EmptyBorder(12, 15, 12, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Nhãn Icon để rỗng, chèn sau
        JLabel lblIcon = new JLabel();
        btn.add(lblIcon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15)); // Chữ nút chính bự lên xíu
        lblTitle.setForeground(new Color(26, 46, 68));
        textPanel.add(lblTitle);

        if (!subTxt.isEmpty()) {
            textPanel.add(Box.createVerticalStrut(4));
            JLabel lblSub = new JLabel(subTxt);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13)); 
            lblSub.setForeground(Color.GRAY);
            textPanel.add(lblSub);
        }

        btn.add(textPanel, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                btn.setBgColor(new Color(220, 235, 250)); 
                btn.repaint(); 
            }
            public void mouseExited(MouseEvent e) { 
                btn.setBgColor(new Color(238, 246, 255)); 
                btn.repaint(); 
            }
        });

        return btn;
    }

    // =========================================================================
    // VẼ KHUNG BO GÓC
    // =========================================================================
    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        public void setBgColor(Color color) {
            this.bgColor = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            
            g2.setColor(new Color(210, 215, 224));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
}