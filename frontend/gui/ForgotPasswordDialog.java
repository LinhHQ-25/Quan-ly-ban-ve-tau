package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import service.PasswordResetService;

public class ForgotPasswordDialog extends JDialog {

    private JTextField txtMaNV, txtEmail, txtOtp;
    private JPasswordField txtNewPassword;
    private JButton btnAction, btnResendOtp;
    private JLabel lblError, lblTitle;
    private JPanel pnlButtons;
    private int step = 1; // Quản lý luồng: 1 (MaNV) -> 2 (Email) -> 3 (OTP) -> 4 (New Pass)
    
    private Timer resendTimer;
    private int countdown = 30;

    public ForgotPasswordDialog(JFrame parent) {
        super(parent, "Khôi phục mật khẩu", true);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- KHỞI TẠO CÁC Ô NHẬP ---
        lblTitle = new JLabel("Nhập mã nhân viên:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtMaNV = createTextField();
        txtEmail = createTextField();
        txtOtp = createTextField();
        txtNewPassword = new JPasswordField();
        txtNewPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNewPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Note xanh/đỏ
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblError.setForeground(Color.RED);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- NÚT CHỨC NĂNG ---
        btnAction = createButton("Tiếp tục");
        btnAction.addActionListener(e -> handleAction());
        
        btnResendOtp = createButton("Gửi lại mã (30s)");
        btnResendOtp.addActionListener(e -> resendOtpAction());

        // --- PANEL CHỨA NÚT (HÀNG NGANG & CĂN GIỮA) ---
        pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
        pnlButtons.setBackground(Color.WHITE);
        pnlButtons.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Thêm Glue ở 2 đầu để đẩy nút vào chính giữa
        pnlButtons.add(Box.createHorizontalGlue());
        pnlButtons.add(btnAction); 
        pnlButtons.add(Box.createHorizontalGlue());

        // --- LẮP RÁP FORM ---
        pnlMain.add(lblTitle);
        pnlMain.add(Box.createVerticalStrut(5));
        pnlMain.add(txtMaNV);
        pnlMain.add(Box.createVerticalStrut(15));
        pnlMain.add(pnlButtons); 
        pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(lblError);

        add(pnlMain);
        
        pack(); 
        setSize(360, getHeight()); 
        setLocationRelativeTo(parent);
    }

    private void handleAction() {
        JPanel pnlMain = (JPanel) getContentPane().getComponent(0);
        
        if (step == 1) {
            String maNV = txtMaNV.getText().trim();
            if (PasswordResetService.checkMaNV(maNV)) {
                step = 2;
                pnlMain.remove(txtMaNV);
                lblTitle.setText("Nhập email liên kết:");
                pnlMain.add(txtEmail, 2);
                btnAction.setText("Gửi mã OTP");
                showError("Tìm thấy tài khoản! Vui lòng nhập Email.", true);
                
                updateDialogSize(pnlMain); 
            } else showError("Mã nhân viên không tồn tại!", false);
        } 
        else if (step == 2) {
            if (PasswordResetService.verifyAndSendOtp(txtMaNV.getText().trim(), txtEmail.getText().trim())) {
                step = 3;
                pnlMain.remove(txtEmail);
                lblTitle.setText("Nhập mã OTP (6 số):");
                pnlMain.add(txtOtp, 2);
                btnAction.setText("Xác thực OTP");
                
                // THÊM NÚT GỬI LẠI MÃ (Ép 2 nút vào giữa)
                pnlButtons.removeAll();
                pnlButtons.add(Box.createHorizontalGlue());
                pnlButtons.add(btnAction);
                pnlButtons.add(Box.createHorizontalStrut(10)); // Khoảng cách giữa 2 nút
                pnlButtons.add(btnResendOtp);
                pnlButtons.add(Box.createHorizontalGlue());
                
                startResendTimer();
                showError("Mã đã gửi đến email!", true);
                updateDialogSize(pnlMain); 
            } else showError("Email không khớp!", false);
        } 
        else if (step == 3) {
            if (PasswordResetService.verifyOtp(txtOtp.getText().trim())) {
                step = 4;
                stopResendTimer();
                
                // XÓA NÚT GỬI LẠI VÀ CHỈNH NÚT DUY NHẤT RA GIỮA LẠI
                pnlButtons.removeAll();
                pnlButtons.add(Box.createHorizontalGlue());
                pnlButtons.add(btnAction);
                pnlButtons.add(Box.createHorizontalGlue());
                
                pnlMain.remove(txtOtp);
                lblTitle.setText("Nhập mật khẩu mới:");
                pnlMain.add(txtNewPassword, 2);
                btnAction.setText("Xác nhận đổi mật khẩu");
                showError("Xác thực thành công!", true);
                
                updateDialogSize(pnlMain); 
            } else showError("Mã OTP sai!", false);
        }
        else if (step == 4) {
            String newPass = new String(txtNewPassword.getPassword()).trim();
            if (PasswordResetService.resetPassword(newPass)) {
                showSuccessAnimation(); 
            } else showError("Lỗi! Kiểm tra lại tên bảng trong DB.", false);
        }
    }
    
    private void updateDialogSize(JPanel pnlMain) {
        pnlMain.revalidate(); 
        pnlMain.repaint();
        pack();
        setSize(360, getHeight()); 
    }
    
    private void resendOtpAction() {
        if (PasswordResetService.verifyAndSendOtp(txtMaNV.getText().trim(), txtEmail.getText().trim())) {
            startResendTimer();
            showError("Đã gửi lại mã OTP mới!", true);
        } else {
            showError("Lỗi gửi lại mã!", false);
        }
    }
    
    private void startResendTimer() {
        countdown = 30;
        btnResendOtp.setEnabled(false);
        btnResendOtp.setText("Gửi lại mã (" + countdown + "s)");
        
        if (resendTimer != null && resendTimer.isRunning()) {
            resendTimer.stop();
        }
        
        resendTimer = new Timer(1000, e -> {
            countdown--;
            if (countdown > 0) {
                btnResendOtp.setText("Gửi lại mã (" + countdown + "s)");
            } else {
                btnResendOtp.setText("Gửi lại mã");
                btnResendOtp.setEnabled(true);
                resendTimer.stop();
            }
        });
        resendTimer.start();
    }
    
    private void stopResendTimer() {
        if (resendTimer != null && resendTimer.isRunning()) {
            resendTimer.stop();
        }
    }

    private void showSuccessAnimation() {
        JPanel pnlMain = (JPanel) getContentPane().getComponent(0);
        pnlMain.removeAll(); 
        
        JLabel lblTick = new JLabel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(80, 80); }
            @Override
            public Dimension getMaximumSize() { return new Dimension(80, 80); }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(34, 170, 70));
                g2.fillOval(0, 0, 80, 80);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(6, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(22, 42, 35, 55); 
                g2.drawLine(35, 55, 58, 28); 
                
                g2.dispose();
            }
        };
        lblTick.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSuccessText = new JLabel("Đổi mật khẩu thành công!");
        lblSuccessText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSuccessText.setForeground(new Color(34, 170, 70));
        lblSuccessText.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlMain.add(Box.createVerticalGlue());
        pnlMain.add(lblTick);
        pnlMain.add(Box.createVerticalStrut(20));
        pnlMain.add(lblSuccessText);
        pnlMain.add(Box.createVerticalGlue());

        updateDialogSize(pnlMain); 

        Timer closeTimer = new Timer(2000, e -> dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();
    }
   
    private void showError(String msg, boolean isSuccess) {
        lblError.setForeground(isSuccess ? new Color(34, 170, 70) : Color.RED);
        lblError.setText(msg);
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return txt;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                int width = fm.stringWidth(getText()) + 40; 
                return new Dimension(width, 40); 
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize(); 
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) g2.setColor(Color.GRAY);
                else if (getModel().isPressed()) g2.setColor(GuiTheme.NAVY.darker());
                else if (getModel().isRollover()) g2.setColor(GuiTheme.NAVY.brighter());
                else g2.setColor(GuiTheme.NAVY);
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        
        // Đổi font size về 14 theo yêu cầu
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}