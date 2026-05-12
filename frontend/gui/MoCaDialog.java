package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MoCaDialog extends JDialog {
    private boolean confirmed = false;
    private String tienMoCa = "";
    private final String PLACEHOLDER = "2,000,000";

    public MoCaDialog(JFrame parent) {
        super(parent, true);
        setUndecorated(true);
        // Trả lại kích thước chuẩn, không làm to nữa
        setSize(420, 360); 
        setLocationRelativeTo(parent);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);

        // --- HEADER ---
        JPanel header = new JPanel();
        header.setBackground(GuiTheme.NAVY);
        header.setPreferredSize(new Dimension(420, 55));
        header.setLayout(new GridBagLayout());
        JLabel lblTitle = new JLabel("MỞ CA LÀM VIỆC");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle);
        contentPane.add(header, BorderLayout.NORTH);

        // --- BODY ---
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        // Chỉnh lại padding để chữ không bị chèn ép
        body.setBorder(new EmptyBorder(15, 30, 5, 30));

        JLabel lblInfo = new JLabel("<html><div style='text-align: center; width: 100%;'>Nhân viên: Trần Văn A<br>Mã nhân viên: NV001</div></html>");
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtTien = new JTextField(PLACEHOLDER);
        txtTien.setFont(new Font("Segoe UI", Font.BOLD, 42));
        txtTien.setForeground(new Color(41, 128, 185)); // Màu xanh chủ đạo
        txtTien.setHorizontalAlignment(JTextField.CENTER);
        txtTien.setMaximumSize(new Dimension(320, 65));
        txtTien.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));

        // Logic giữ màu xanh
        txtTien.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtTien.getText().equals(PLACEHOLDER)) {
                    txtTien.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtTien.getText().isEmpty()) {
                    txtTien.setText(PLACEHOLDER);
                }
            }
        });

        // Hộp chứa dòng Gợi ý (Đẩy sang phải, đủ cao để không bị cắt xén)
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        hintPanel.setBackground(Color.WHITE);
        hintPanel.setMaximumSize(new Dimension(320, 45)); // Rộng 320, cao 45 để chứa thoải mái 2 dòng
        JLabel lblHint = new JLabel("<html><div style='text-align: right; color: gray;'>Tiền mặt đầu ca (VNĐ)<br>Nhấn Enter để xác nhận</div></html>");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        hintPanel.add(lblHint);

        body.add(lblInfo);
        body.add(Box.createVerticalStrut(15));
        body.add(txtTien);
        body.add(Box.createVerticalStrut(5));
        body.add(hintPanel);

        contentPane.add(body, BorderLayout.CENTER);

        // --- FOOTER ---
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(5, 40, 20, 40));

        JButton btnXacNhan = createRoundedButton("XÁC NHẬN MỞ CA", GuiTheme.NAVY, Color.WHITE);
        JButton btnHuy = createRoundedButton("HỦY", Color.GRAY, Color.WHITE);

        btnXacNhan.addActionListener(e -> xacNhan(txtTien.getText()));
        txtTien.addActionListener(e -> xacNhan(txtTien.getText())); 

        btnHuy.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        footer.add(btnXacNhan);
        footer.add(Box.createVerticalStrut(10));
        footer.add(btnHuy);

        contentPane.add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnXacNhan);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                btnXacNhan.requestFocus();
            }
        });
    }

    // Nút bo góc: Đã giảm chiều cao xuống 40px cho gọn gàng
    private JButton createRoundedButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
                g2.dispose();
                super.paintComponent(g); 
            }
        };
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(340, 40)); // Chiều cao 40
        btn.setMaximumSize(new Dimension(340, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void xacNhan(String tien) {
        this.tienMoCa = tien;
        this.confirmed = true;
        dispose(); // Nhường việc gọi LoadingPanel lại cho AppFrame lo
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getTienMoCa() {
        return tienMoCa;
    }
}