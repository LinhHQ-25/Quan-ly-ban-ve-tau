package gui;

import java.awt.*;
import javax.swing.*;

public class LoadingPanel extends JPanel {
    private JProgressBar progressBar;
    private JLabel lblStatus;

    public LoadingPanel() {
        setLayout(new GridBagLayout());
        setBackground(GuiTheme.LIGHT_BG); // Màu nền trắng xám của hệ thống

        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        // Logo hoặc Icon Tàu (Có thể dùng icon khác tùy bác)
        JLabel iconTrain = new JLabel(GuiIcons.loadIcon(LoadingPanel.class, "/Images/logoTrain.png", 150, 150));
        iconTrain.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("ĐANG THIẾT LẬP CA LÀM VIỆC...");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(GuiTheme.NAVY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblStatus = new JLabel("Đang tải dữ liệu: 0%");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblStatus.setForeground(Color.GRAY);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Thanh Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setMaximumSize(new Dimension(400, 20));
        progressBar.setForeground(GuiTheme.ACCENT); // Màu thanh chạy
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setBorderPainted(false);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerBox.add(iconTrain);
        centerBox.add(Box.createVerticalStrut(20));
        centerBox.add(lblTitle);
        centerBox.add(Box.createVerticalStrut(10));
        centerBox.add(progressBar);
        centerBox.add(Box.createVerticalStrut(5));
        centerBox.add(lblStatus);

        add(centerBox);
    }

    // Hàm gọi để chạy loading, chạy xong thì thực thi hành động (chuyển trang)
    public void startLoading(Runnable onComplete) {
        progressBar.setValue(0);
        lblStatus.setText("Đang kết nối cơ sở dữ liệu...");

        Timer timer = new Timer(30, new java.awt.event.ActionListener() {
            int progress = 0;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                progress += 2;
                progressBar.setValue(progress);
                lblStatus.setText("Đang tải dữ liệu: " + progress + "%");

                if (progress >= 100) {
                    ((Timer) e.getSource()).stop();
                    onComplete.run(); // Chạy xong 100% thì gọi lệnh chuyển sang Home
                }
            }
        });
        timer.start();
    }
}