package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import service.AuthService;

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
        JLabel bgLabel = new JLabel(GuiIcons.loadIcon(LoginPanel.class, "/Images/Ga.png", 700, 790));

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
        JLabel iconTrain = new JLabel(GuiIcons.loadIcon(LoginPanel.class, "/Images/logoTrain.png", 120, 120));
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

        // --- Nút Đăng nhập bo tròn (Có Hover & Chiều sâu) ---
        JButton btnLogin = new JButton("Đăng nhập") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isPressed = getModel().isPressed();
                boolean isHovered = getModel().isRollover();

                // 1. Tạo bóng đổ (Drop Shadow) tạo chiều sâu (Chỉ hiện khi không nhấn)
                if (!isPressed) {
                    g2.setColor(new Color(0, 0, 0, 40)); // Bóng xám mờ
                    g2.fillRoundRect(0, 4, getWidth(), getHeight() - 4, 20, 20);
                }

                // 2. Vẽ nền nút (Hiệu ứng Hover & Nhấn)
                if (isPressed) {
                    g2.setColor(GuiTheme.NAVY.darker());
                    g2.fillRoundRect(0, 3, getWidth(), getHeight() - 6, 20, 20); // Nút lún xuống
                } else if (isHovered) {
                    g2.setColor(GuiTheme.NAVY.brighter()); // Sáng lên khi Hover
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() - 6, 20, 20);
                } else {
                    g2.setColor(GuiTheme.NAVY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() - 6, 20, 20);
                }

                // 3. Vẽ chữ
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - 6 + fm.getAscent()) / 2 - 2;
                if (isPressed) y += 3;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        txtUsername.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtPassword.requestFocusInWindow();
                }
            }
        });
        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    btnLogin.requestFocusInWindow();
                }
            }
        });
        btnLogin.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    btnLogin.doClick();
                }
            }
        });

        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(300, 50)); 
        btnLogin.setMaximumSize(new Dimension(300, 50));
        btnLogin.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sự kiện click nút Đăng nhập
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();

            if (username.isEmpty() || username.equals("Tài khoản")) {
                showStatusPopup(false, "Vui lòng nhập tài khoản!");
                return;
            }
            if (password.isEmpty() || password.equals("Mật khẩu")) {
                showStatusPopup(false, "Vui lòng nhập mật khẩu!");
                return;
            }

            boolean isAdminChecked = chkAdmin.isSelected();
            AuthService.LoginResult result = AuthService.login(username, password);

            switch (result) {
                case SUCCESS_QUAN_LY -> {
                    if (!isAdminChecked) {
                        showStatusPopup(false, "Tài khoản này là Quản lý!<br>Vui lòng tích vào ô 'Quản lý'.");
                        return;
                    }
                    showStatusPopup(true, "Đăng nhập thành công!");
                    Timer timer = new Timer(1500, ev -> parent.onLoginSuccess(true));
                    timer.setRepeats(false);
                    timer.start();
                }
                case SUCCESS_BAN_VE -> {
                    if (isAdminChecked) {
                        showStatusPopup(false, "Tài khoản này không có quyền Quản lý!");
                        return;
                    }
                    showStatusPopup(true, "Đăng nhập thành công!");
                    Timer timer = new Timer(1500, ev -> parent.onLoginSuccess(false));
                    timer.setRepeats(false);
                    timer.start();
                }
                case WRONG_PASSWORD    -> showStatusPopup(false, "Mật khẩu không đúng!");
                case ACCOUNT_NOT_FOUND -> showStatusPopup(false, "Tài khoản không tồn tại!");
            }
        });

        // --- Quên mật khẩu (Có Hover) ---
        JLabel lblForgot = new JLabel("Quên mật khẩu!");
        lblForgot.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblForgot.setForeground(GuiTheme.ACCENT);
        lblForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new ForgotPasswordDialog(parent).setVisible(true);
            }
        });

        // --- Lắp ráp Form ---
        form.add(iconTrain);
        form.add(Box.createVerticalStrut(5));
        form.add(title);
        form.add(Box.createVerticalStrut(20));
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

    // --- POPUP THÔNG BÁO CAO CẤP CHUNG (TỰ ĐỘNG ĐÓNG SAU 1.5 GIÂY) ---
    private void showStatusPopup(boolean isSuccess, String message) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
        dialog.setSize(320, 180);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền trắng bo góc mềm mại
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                // Viền mảnh tinh tế bao quanh
                g2.setColor(new Color(235, 235, 235));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 20, 25, 20));

        // Tự vẽ icon Vector tùy theo trạng thái Thành công / Thất bại
        JLabel lblIcon = new JLabel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(70, 70); }
            @Override
            public Dimension getMaximumSize() { return new Dimension(70, 70); }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSuccess) {
                    // Vẽ hình tròn xanh lá cho thành công
                    g2.setColor(new Color(34, 170, 70));
                    g2.fillOval(0, 0, 70, 70);
                    // Vẽ dấu tích v trắng
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(20, 36, 31, 47);
                    g2.drawLine(31, 47, 51, 23);
                } else {
                    // Vẽ hình tròn đỏ cho lỗi / thất bại
                    g2.setColor(new Color(220, 80, 80));
                    g2.fillOval(0, 0, 70, 70);
                    // Vẽ dấu X trắng chéo nhau
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(24, 24, 46, 46);
                    g2.drawLine(46, 24, 24, 46);
                }
                g2.dispose();
            }
        };
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nhãn nội dung thông báo
        JLabel msg = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.BOLD, 15));
        msg.setForeground(isSuccess ? new Color(34, 170, 70) : new Color(60, 60, 60));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(lblIcon);
        panel.add(Box.createVerticalStrut(15));
        panel.add(msg);
        panel.add(Box.createVerticalGlue());

        dialog.setContentPane(panel);
        dialog.setBackground(new Color(0, 0, 0, 0)); // Làm trong suốt phần rìa JDialog để thấy góc bo tròn

        // Đóng tự động sau 1.5 giây
        Timer closeTimer = new Timer(1500, ev -> dialog.dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();

        dialog.setVisible(true);
    }

    private JPanel createInputWrapper(JTextField textField, String placeholder, boolean isPassword) {
        final boolean[] isHovered = {false};

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(1, 3, getWidth() - 2, getHeight() - 3, 20, 20);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, 20, 20);
            }
        };

        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(320, 52)); 
        wrapper.setBorder(new EmptyBorder(6, 15, 10, 15));

        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapper.repaint(); 
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    if (isPassword) ((JPasswordField) textField).setEchoChar('•');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                wrapper.repaint(); 
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                    if (isPassword) ((JPasswordField) textField).setEchoChar((char) 0);
                }
            }
        });

        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                wrapper.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                wrapper.repaint();
            }
        };
        wrapper.addMouseListener(hoverAdapter);
        textField.addMouseListener(hoverAdapter); 

        wrapper.add(textField, BorderLayout.CENTER);

        if (isPassword) {
            Icon iconOpen = GuiIcons.loadIcon(LoginPanel.class, "/Images/eyeon.png", 20, 20);
            Icon iconClose = GuiIcons.loadIcon(LoginPanel.class, "/Images/eyeoff.png", 20, 20);

            JLabel eyeLabel = new JLabel(iconClose);
            eyeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            eyeLabel.addMouseListener(hoverAdapter); 

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