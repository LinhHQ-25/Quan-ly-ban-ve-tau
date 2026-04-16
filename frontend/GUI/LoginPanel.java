package GUI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginPanel extends JPanel {
    private AppFrame parent;

    public LoginPanel(AppFrame parent) {
        this.parent = parent;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(null);

        // 1. PHẦN BÊN TRÁI: ẢNH NỀN 
        JPanel leftPanel = new JPanel(new BorderLayout()); 
        leftPanel.setBackground(GuiTheme.NAVY);
        leftPanel.setBorder(null); 

        leftPanel.setPreferredSize(new Dimension(670, 0)); 
        leftPanel.setMaximumSize(new Dimension(670, Integer.MAX_VALUE));
        JLabel bgLabel = new JLabel(GuiIcons.loadIcon(LoginPanel.class, "/Images/Ga.png", 670, 760));

        bgLabel.setBorder(null); 
        
        leftPanel.add(bgLabel, BorderLayout.CENTER);
        
        // 2. PHẦN BÊN PHẢI: FORM ĐĂNG NHẬP
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(GuiTheme.LIGHT_BG);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 40, 0, 40));

        // --- Logo & Tiêu đề ---
        JLabel iconTrain = new JLabel(GuiIcons.loadIcon(LoginPanel.class, "/Images/logoTrain.png", 100, 100));
        iconTrain.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Đăng nhập");
        title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 30));
        title.setForeground(GuiTheme.NAVY_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Ô nhập Tài khoản & Mật khẩu ---
        JTextField txtUsername = new JTextField("Tài khoản");
        JPanel userWrapper = createInputWrapper(txtUsername, "Tài khoản", false);

        JPasswordField txtPassword = new JPasswordField("Mật khẩu");
        txtPassword.setEchoChar((char) 0); // Hiện chữ mờ lúc đầu
        JPanel passWrapper = createInputWrapper(txtPassword, "Mật khẩu", true);

        // --- Checkbox Quản lý ---
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        checkPanel.setOpaque(false);
        checkPanel.setMaximumSize(new Dimension(300, 30));
        JCheckBox chkAdmin = new JCheckBox("Quản lý");
        chkAdmin.setOpaque(false);
        chkAdmin.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        chkAdmin.setForeground(GuiTheme.NAVY);
        checkPanel.add(chkAdmin);

        // --- Nút Đăng nhập ---
     // --- Nút Đăng nhập bo tròn ---
        JButton btnLogin = new JButton("Đăng nhập") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Tự vẽ nền bo góc
                if (getModel().isPressed()) {
                    g2.setColor(GuiTheme.NAVY.darker()); // Nhấn xuống thì màu đậm hơn tí
                } else {
                    g2.setColor(GuiTheme.NAVY);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Vẽ chữ lên trên nền vừa vẽ
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };

        // Thiết lập các thuộc tính phụ để nút trông sạch sẽ
        btnLogin.setContentAreaFilled(false); // Quan trọng: Bỏ nền mặc định của Java
        btnLogin.setBorderPainted(false);     // Bỏ viền mặc định
        btnLogin.setFocusPainted(false);      // Bỏ khung nét đứt khi click
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Kích thước nút
        btnLogin.setPreferredSize(new Dimension(300, 45));
        btnLogin.setMaximumSize(new Dimension(300, 45));
        btnLogin.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sự kiện click
        btnLogin.addActionListener(e -> {
            parent.onLoginSuccess();
        });

        // --- Quên mật khẩu ---
        JLabel lblForgot = new JLabel("Quên mật khẩu!");
        lblForgot.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblForgot.setForeground(GuiTheme.ACCENT);
        lblForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- Lắp ráp Form ---
        form.add(iconTrain);
        form.add(Box.createVerticalStrut(-15));
        form.add(title);
        form.add(Box.createVerticalStrut(15));
        form.add(userWrapper);
        form.add(Box.createVerticalStrut(15));
        form.add(passWrapper);
        form.add(Box.createVerticalStrut(5));
        form.add(checkPanel);
        form.add(Box.createVerticalStrut(15));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(15));
        form.add(lblForgot);

        rightPanel.add(form);
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }


    private JPanel createInputWrapper(JTextField textField, String placeholder, boolean isPassword) {
  
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
  
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                

                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                g2.dispose();
            }
        };

        wrapper.setOpaque(false); 
        wrapper.setMaximumSize(new Dimension(320, 48));
        wrapper.setBorder(new EmptyBorder(8, 15, 8, 15)); 

        textField.setBorder(null);
        textField.setOpaque(false); 
        textField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        textField.setForeground(Color.GRAY);


        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    if (isPassword) ((JPasswordField) textField).setEchoChar('•');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                    if (isPassword) ((JPasswordField) textField).setEchoChar((char) 0);
                }
            }
        });

        wrapper.add(textField, BorderLayout.CENTER);

        if (isPassword) {
            Icon iconOpen = GuiIcons.loadIcon(LoginPanel.class, "/Images/eyeon.png", 20, 20);
            Icon iconClose = GuiIcons.loadIcon(LoginPanel.class, "/Images/eyeoff.png", 20, 20);
            
            JLabel eyeLabel = new JLabel(iconClose);
            eyeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            eyeLabel.addMouseListener(new MouseAdapter() {
                boolean isHidden = true;
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!textField.getText().equals(placeholder)) {
                        isHidden = !isHidden;
                        ((JPasswordField) textField).setEchoChar(isHidden ? '•' : (char) 0);
                        eyeLabel.setIcon(isHidden ? iconClose : iconOpen);
                    }
                }
            });
            wrapper.add(eyeLabel, BorderLayout.EAST);
        }

        return wrapper;
    }
}