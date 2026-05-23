package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import service.AuthService;

public class LoginPanel extends JPanel {
    private AppFrame parent;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginPanel(AppFrame parent) {
        this.parent = parent;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(null);

        // 1. PHẦN BÊN TRÁI: ẢNH NỀN
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(GuiTheme.NAVY);
        leftPanel.setBorder(null);

        leftPanel.setPreferredSize(new Dimension(750, 0));
        leftPanel.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));
        JLabel bgLabel = new JLabel(GuiIcons.loadIcon(LoginPanel.class, "/Images/Ga.png", 780, 800));

        bgLabel.setBorder(null);

        leftPanel.add(bgLabel, BorderLayout.CENTER);

        // 2. PHẦN BÊN PHẢI: FORM ĐĂNG NHẬP
        // Gradient siêu nhạt + decorative circles navy mờ
        JPanel rightPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient từ trắng -> xanh nhạt
                g2.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth(), getHeight(), new Color(0xF5, 0xF8, 0xFC)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decoration: 3 vòng tròn navy mờ
                g2.setColor(new Color(GuiTheme.NAVY.getRed(), GuiTheme.NAVY.getGreen(), GuiTheme.NAVY.getBlue(), 12));
                g2.fillOval(-120, -120, 360, 360);
                g2.fillOval(getWidth() - 180, getHeight() - 180, 320, 320);
                g2.setColor(new Color(GuiTheme.NAVY.getRed(), GuiTheme.NAVY.getGreen(), GuiTheme.NAVY.getBlue(), 8));
                g2.fillOval(getWidth() - 80, 60, 180, 180);
                g2.dispose();
            }
        };
        rightPanel.setOpaque(false);

        // --- ĐÃ CHỈNH SỬA Ở ĐÂY ---
        // Thay vì vẽ Card trắng, chỉ dùng một JPanel trong suốt để căn giữa các component
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false); // Trong suốt hoàn toàn để lộ nền gradient
        form.setPreferredSize(new Dimension(420, 620)); // Vẫn giữ nguyên kích thước để căn giữa chuẩn
        form.setMaximumSize(new Dimension(420, 620));
        form.setBorder(new EmptyBorder(40, 50, 40, 50));

        // --- Logo & Tiêu đề ---
        JLabel iconTrain = new JLabel(GuiIcons.loadIcon(LoginPanel.class, "/Images/logoTrain.png", 120, 120));
        iconTrain.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Đăng nhập");
        title.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 30));
        title.setForeground(GuiTheme.NAVY_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Gạch ngang navy ngắn dưới tiêu đề
        JPanel underline = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        underline.setOpaque(false);
        underline.setPreferredSize(new Dimension(60, 4));
        underline.setMaximumSize(new Dimension(60, 4));
        underline.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Dòng phụ
        JLabel subtitle = new JLabel("Hệ thống quản lý vé tàu");
        subtitle.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 130, 145));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Ô nhập Tài khoản & Mật khẩu ---
        txtUsername = new JTextField("Tài khoản");
        JPanel userWrapper = createInputWrapper(txtUsername, "Tài khoản", false);

        txtPassword = new JPasswordField("Mật khẩu");
        txtPassword.setEchoChar((char) 0); // Hiện chữ mờ lúc đầu
        JPanel passWrapper = createInputWrapper(txtPassword, "Mật khẩu", true);

    

        // --- Nút Đăng nhập bo tròn ---
        JButton btnLogin = new JButton("Đăng nhập") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isPressed = getModel().isPressed();
                boolean isHovered = getModel().isRollover();

                // 1. Shadow navy đậm tạo chiều sâu
                if (!isPressed) {
                    for (int i = 0; i < 5; i++) {
                        g2.setColor(new Color(GuiTheme.NAVY.getRed(), GuiTheme.NAVY.getGreen(), GuiTheme.NAVY.getBlue(), 40 - i * 6));
                        g2.fillRoundRect(0, 4 + i, getWidth(), getHeight() - 4, 20, 20);
                    }
                }

                // 2. Vẽ nền nút
                if (isPressed) {
                    g2.setColor(GuiTheme.NAVY.darker());
                    g2.fillRoundRect(0, 3, getWidth(), getHeight() - 6, 20, 20);
                } else if (isHovered) {
                    g2.setColor(GuiTheme.NAVY.brighter());
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

        // ENTER ở bất kỳ đâu cũng đăng nhập
        txtUsername.addActionListener(e -> btnLogin.doClick());
        txtPassword.addActionListener(e -> btnLogin.doClick());

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

            AuthService.LoginResult result = AuthService.login(username, password);

            switch (result) {
                case SUCCESS_QUAN_LY -> {
                    showStatusPopup(true, "Đăng nhập thành công!");
                    Timer timer = new Timer(1500, ev -> parent.onLoginSuccess(true));
                    timer.setRepeats(false);
                    timer.start();
                }
                case SUCCESS_BAN_VE -> {
                    showStatusPopup(true, "Đăng nhập thành công!");
                    Timer timer = new Timer(1500, ev -> parent.onLoginSuccess(false));
                    timer.setRepeats(false);
                    timer.start();
                }
                case WRONG_PASSWORD    -> showStatusPopup(false, "Mật khẩu không đúng!");
                case ACCOUNT_NOT_FOUND -> showStatusPopup(false, "Tài khoản không tồn tại!");
                case ACCOUNT_LOCKED    -> showStatusPopup(false, "Tài khoản đã bị vô hiệu hóa!");
            }
        });

        // --- Quên mật khẩu ---
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
        form.add(Box.createVerticalStrut(8));
        form.add(underline);
        form.add(Box.createVerticalStrut(8));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(22));
        form.add(userWrapper);
        form.add(Box.createVerticalStrut(15));
        form.add(passWrapper);
        form.add(Box.createVerticalStrut(15));
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(15));
        form.add(lblForgot);

        rightPanel.add(form);
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Mặc định ENTER ở bất cứ đâu trong form đều đăng nhập
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JRootPane rp = SwingUtilities.getRootPane(this);
            if (rp != null) rp.setDefaultButton(btnLogin);
        });
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
                g2.setColor(new Color(255, 255, 255));
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

    public void refresh() {
        if (txtUsername != null) txtUsername.setText("Tài khoản");
        if (txtPassword != null) {
            txtPassword.setText("Mật khẩu");
            txtPassword.setEchoChar((char) 0); // hiện chữ như placeholder
        }
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

                // Viền navy khi focus
                if (textField.hasFocus()) {
                    g2.setColor(GuiTheme.NAVY);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 20, 20);
                }
            }
        };

        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(300, 52));
        wrapper.setPreferredSize(new Dimension(300, 52));
        wrapper.setBorder(new EmptyBorder(6, 15, 10, 15));

        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 15));
        textField.setForeground(Color.GRAY);

        final JPanel wrapperRef = wrapper;
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapperRef.repaint();
                wrapper.repaint();
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    if (isPassword) ((JPasswordField) textField).setEchoChar('•');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                wrapperRef.repaint();
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