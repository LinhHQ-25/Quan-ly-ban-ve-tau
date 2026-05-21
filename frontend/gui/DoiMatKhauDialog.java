package gui;

import service.AuthService;
import connect_DB.Connect_DB;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DoiMatKhauDialog extends JDialog {

    private static final Color NAVY        = new Color(26, 46, 68);
    private static final Color BLUE_ACCENT = new Color(52, 152, 219);
    private static final Color LIGHT_BG    = new Color(245, 248, 252);
    private static final Color BORDER_CLR  = new Color(210, 215, 224);
    private static final Color ERROR_CLR   = new Color(220, 53, 69);
    private static final Color SUCCESS_CLR = new Color(40, 167, 69);

    private JPasswordField pfCu, pfMoi, pfMoiLai;
    private JLabel lblError;

    public DoiMatKhauDialog(Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Glass pane làm mờ nền
        if (owner instanceof JFrame) {
            JPanel glass = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(new Color(0, 0, 0, 180));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            glass.setOpaque(false);
            ((JFrame) owner).setGlassPane(glass);
            glass.setVisible(true);
            addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) { glass.setVisible(false); }
            });
        }

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private JPanel buildContent() {
        // Card chính
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(400, 420));
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(20, 24, 20, 24));
        // Bo góc trên cho header
        JPanel headerWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 18, 18, 18); // chỉ bo góc trên
                g2.dispose();
            }
        };
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel lblTitle = new JLabel("Đổi mật khẩu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Vui lòng nhập đầy đủ thông tin bên dưới");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(180, 200, 220));
        lblSub.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(lblSub);
        headerWrapper.add(titleBox);
        card.add(headerWrapper, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(24, 24, 8, 24));

        pfCu     = makePassField();
        pfMoi    = makePassField();
        pfMoiLai = makePassField();

        body.add(makeFieldGroup("Mật khẩu hiện tại", pfCu));
        body.add(Box.createVerticalStrut(14));
        body.add(makeFieldGroup("Mật khẩu mới", pfMoi));
        body.add(Box.createVerticalStrut(14));
        body.add(makeFieldGroup("Nhập lại mật khẩu mới", pfMoiLai));
        body.add(Box.createVerticalStrut(12));

        // Label lỗi
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(ERROR_CLR);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(lblError);

        card.add(body, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new GridLayout(1, 2, 12, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(4, 24, 24, 24));

        JButton btnHuy = makeBtn("Hủy", false);
        JButton btnXN  = makeBtn("Xác nhận", true);

        btnHuy.addActionListener(e -> dispose());
        btnXN.addActionListener(e -> handleConfirm());

        // Enter để xác nhận
        getRootPane().setDefaultButton(btnXN);

        footer.add(btnHuy);
        footer.add(btnXN);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPasswordField makePassField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBackground(LIGHT_BG);
        pf.setBorder(new EmptyBorder(8, 12, 8, 8));
        pf.setOpaque(false);
        return pf;
    }

    private JPanel makeFieldGroup(String label, JPasswordField pf) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(60, 70, 85));
        group.add(lbl, BorderLayout.NORTH);

      
        JPanel inputWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(LIGHT_BG);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.setColor(BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
            }
        };
        inputWrapper.setOpaque(false);
        inputWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        inputWrapper.setPreferredSize(new Dimension(0, 40));

        // Icon mắt
        ImageIcon iconOff = GuiIcons.loadIcon(DoiMatKhauDialog.class, "/Images/eyeoff.png", 18, 18);
        ImageIcon iconOn  = GuiIcons.loadIcon(DoiMatKhauDialog.class, "/Images/eyeon.png",  18, 18);
        JLabel eyeBtn = new JLabel(iconOff);
        eyeBtn.setBorder(new EmptyBorder(0, 6, 0, 10));
        eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final boolean[] showing = {false};
        eyeBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showing[0] = !showing[0];
                pf.setEchoChar(showing[0] ? (char) 0 : '•');
                eyeBtn.setIcon(showing[0] ? iconOn : iconOff);
            }
        });

        inputWrapper.add(pf, BorderLayout.CENTER);
        inputWrapper.add(eyeBtn, BorderLayout.EAST);
        group.add(inputWrapper, BorderLayout.CENTER);
        return group;
    }

    private JButton makeBtn(String text, boolean isPrimary) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? (isPrimary ? new Color(41, 128, 185) : new Color(225, 230, 238))
                        : (isPrimary ? BLUE_ACCENT : new Color(240, 243, 248)));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(isPrimary ? Color.WHITE : NAVY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(0, 42));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =========================================================================
    // LOGIC XÁC NHẬN
    // =========================================================================
    private void handleConfirm() {
        String cu     = new String(pfCu.getPassword()).trim();
        String moi    = new String(pfMoi.getPassword()).trim();
        String moiLai = new String(pfMoiLai.getPassword()).trim();

        // Validate rỗng
        if (cu.isEmpty() || moi.isEmpty() || moiLai.isEmpty()) {
            setError("Vui lòng điền đầy đủ tất cả các trường.");
            return;
        }

        // Validate mật khẩu mới khớp
        if (!moi.equals(moiLai)) {
            setError("Mật khẩu mới nhập lại không khớp.");
            return;
        }

        // Validate độ dài tối thiểu
        if (moi.length() < 6) {
            setError("Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        // Validate không trùng mật khẩu cũ
        if (cu.equals(moi)) {
            setError("Mật khẩu mới không được trùng mật khẩu hiện tại.");
            return;
        }

        // Kiểm tra mật khẩu cũ đúng không + đổi trong DB
        String maNV = AuthService.getCurrentMaNV();
        try (Connection con = Connect_DB.getConnection()) {
            if (con == null) { setError("Không thể kết nối cơ sở dữ liệu."); return; }

            // Kiểm tra mật khẩu cũ
            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT matKhau FROM TaiKhoan WHERE maNV = ?");
            psCheck.setString(1, maNV);
            ResultSet rs = psCheck.executeQuery();
            if (!rs.next() || !rs.getString("matKhau").equals(cu)) {
                setError("Mật khẩu hiện tại không đúng.");
                return;
            }

            // Cập nhật mật khẩu mới
            PreparedStatement psUpdate = con.prepareStatement(
                    "UPDATE TaiKhoan SET matKhau = ? WHERE maNV = ?");
            psUpdate.setString(1, moi);
            psUpdate.setString(2, maNV);
            psUpdate.executeUpdate();

            // Thành công
            JOptionPane.showMessageDialog(this,
                    "Đổi mật khẩu thành công!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            setError("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    private void setError(String msg) {
        lblError.setText(msg);
        lblError.setForeground(ERROR_CLR);
    }
}