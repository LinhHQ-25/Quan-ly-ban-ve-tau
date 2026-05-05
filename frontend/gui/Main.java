package gui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Khởi tạo kết nối CSDL ngay khi chạy ứng dụng
        connect_DB.Connect_DB.getInstance().connect();

        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}
