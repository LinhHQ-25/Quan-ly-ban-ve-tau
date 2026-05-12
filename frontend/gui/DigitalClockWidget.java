package gui;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DigitalClockWidget extends JPanel {
    private JLabel lblTime;
    private JLabel lblDate;

    public DigitalClockWidget() {
        setOpaque(false);
        setLayout(new BorderLayout());
        
        RoundedShadowPanel card = new RoundedShadowPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(8, 20, 8, 20)
        ));
        
        lblTime = new JLabel("00:00:00");
        lblTime.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
        lblTime.setForeground(GuiTheme.NAVY);
        lblTime.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblDate = new JLabel("Thứ Hai, 01/01/2024");
        lblDate.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(GuiTheme.SUB_TEXT);
        lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTime);
        card.add(Box.createVerticalStrut(2));
        card.add(lblDate);
        
        add(card, BorderLayout.CENTER);

        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
        updateTime();
    }

    private void updateTime() {
        Date now = new Date();
        lblTime.setText(new SimpleDateFormat("HH:mm:ss").format(now));
        lblDate.setText(new SimpleDateFormat("EEEE, dd/MM/yyyy").format(now));
    }
}
