package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class TauGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color CARD_BORDER = new Color(125, 192, 225);
    private static final Color ACTIVE_BG = new Color(229, 244, 234);
    private static final Color ACTIVE_TEXT = new Color(31, 125, 70);
    private static final Color PRIMARY = new Color(71, 71, 156);
    private static final Color MAINTAIN_BG = new Color(252, 239, 220);
    private static final Color MAINTAIN_TEXT = new Color(166, 107, 24);
    private static final int CARD_WIDTH = 118;
    private static final int CARD_HEIGHT = 82;
    private static final int CARD_GAP_X = 26;
    private static final int VISIBLE_CARD_COLUMNS = 4;

    private static final Object[][] TRAIN_CARDS = {
        { "SE1", "Tàu Bắc Nam", "Bảo trì" },
        { "SE2", "Tàu Sài Gòn", "Bảo trì" },
        { "TN3", "Tàu Thống Nhất", "Bảo trì" },
        { "SE4", "Tàu Biển Đông", "Hoạt động" },
        { "SE5", "Tàu Miền Trung", "Hoạt động" },
        { "TN6", "Tàu Hòa Bình", "Bảo trì" },
        { "SE7", "Tàu Phương Nam", "Hoạt động" },
        { "TN8", "Tàu Đông Dương", "Hoạt động" }
    };

    TauGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BorderLayout(0, 12));
        pnlPage.setBorder(new EmptyBorder(
            0,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 12));
        pnlTop.setOpaque(false);
        pnlTop.add(buildTrainPreviewPanel(), BorderLayout.NORTH);
        pnlTop.add(buildFilterPanel(), BorderLayout.CENTER);

        pnlPage.add(pnlTop, BorderLayout.NORTH);
        pnlPage.add(buildTablePanel(), BorderLayout.CENTER);

        add(pnlPage, BorderLayout.CENTER);
    }

    private JScrollPane buildTrainPreviewPanel() {
        int cardCount = TRAIN_CARDS.length;
        int columnCount = Math.max(VISIBLE_CARD_COLUMNS, cardCount);

        JPanel pnlCards = new JPanel(new GridLayout(1, columnCount, CARD_GAP_X, 0));
        pnlCards.setOpaque(false);

        for (Object[] train : TRAIN_CARDS) {
            pnlCards.add(buildTrainCard((String) train[0], (String) train[1], (String) train[2]));
        }

        int preferredWidth = columnCount * CARD_WIDTH + (columnCount - 1) * CARD_GAP_X;
        int preferredHeight = CARD_HEIGHT;
        pnlCards.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

        JPanel pnlCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlCenter.setOpaque(false);
        pnlCenter.add(pnlCards);

        JScrollPane scroll = new JScrollPane(
            pnlCenter,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getHorizontalScrollBar().setUnitIncrement(22);
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight + 14));
        scroll.setPreferredSize(new Dimension(
            VISIBLE_CARD_COLUMNS * CARD_WIDTH + (VISIBLE_CARD_COLUMNS - 1) * CARD_GAP_X + 4,
            preferredHeight + 14
        ));
        scroll.getViewport().setViewPosition(new java.awt.Point(0, 0));
        return scroll;
    }

    private JPanel buildTrainCard(String code, String name, String status) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(2, 2, 2, 2));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        JPanel shell = new RoundedPanel(18, Color.WHITE, CARD_BORDER, 2f);
        shell.setLayout(new BorderLayout(0, 4));
        shell.setBorder(new EmptyBorder(7, 8, 7, 8));

        JPanel imagePanel = new RoundedPanel(14, new Color(238, 247, 253), CARD_BORDER, 1.5f);
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(96, 24));

        JLabel image = new JLabel(loadTrainIcon(), SwingConstants.CENTER);
        imagePanel.add(image, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 3));
        footer.setOpaque(false);

        JLabel lbCode = new JLabel(code, SwingConstants.CENTER);
        lbCode.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        lbCode.setForeground(GuiTheme.TEXT);

        JLabel lbName = new JLabel(name, SwingConstants.CENTER);
        lbName.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 9));
        lbName.setForeground(GuiTheme.SUB_TEXT);

        JLabel lbStatus = buildStatusTag(status);

        footer.add(lbCode, BorderLayout.NORTH);
        footer.add(lbName, BorderLayout.CENTER);
        footer.add(lbStatus, BorderLayout.SOUTH);

        shell.add(imagePanel, BorderLayout.NORTH);
        shell.add(footer, BorderLayout.CENTER);
        card.add(shell, BorderLayout.CENTER);
        return card;
    }

    private Icon loadTrainIcon() {
        return GuiIcons.loadIcon(TauGUI.class, "/Images/logoTrain.png", 58, 20);
    }

    private JLabel buildStatusTag(String status) {
        Color bg = "Hoạt động".equals(status) ? ACTIVE_BG : MAINTAIN_BG;
        Color fg = "Hoạt động".equals(status) ? ACTIVE_TEXT : MAINTAIN_TEXT;

        JLabel lbStatus = new JLabel(status, SwingConstants.CENTER);
        lbStatus.setOpaque(true);
        lbStatus.setBackground(bg);
        lbStatus.setForeground(fg);
        lbStatus.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 10));
        lbStatus.setBorder(new EmptyBorder(3, 8, 3, 8));
        return lbStatus;
    }

    private JPanel buildFilterPanel() {
        JPanel pnlOuter = new JPanel(new BorderLayout());
        pnlOuter.setOpaque(false);

        JPanel pnlGrid = new JPanel(new GridBagLayout());
        pnlGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 8, 18);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1.1;
        pnlGrid.add(buildField("Mã tàu", buildTextField(150)), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.1;
        pnlGrid.add(buildField("Tên tàu:", buildTextField(150)), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.75;
        pnlGrid.add(buildField("Trạng thái:", buildStatusCombo()), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        pnlGrid.add(buildField("Số toa:", buildCoachSpinner()), gbc);

        gbc.gridy = 1;
        gbc.gridx = 3;
        gbc.weightx = 1.2;
        pnlGrid.add(buildField("", buildActionBlock()), gbc);

        pnlOuter.add(pnlGrid, BorderLayout.CENTER);
        return pnlOuter;
    }

    private JPanel buildField(String label, Component comp) {
        JPanel pnlField = new JPanel(new BorderLayout(0, 4));
        pnlField.setOpaque(false);
        JLabel lbField = new JLabel(label);
        lbField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        lbField.setForeground(PRIMARY);
        pnlField.add(lbField, BorderLayout.NORTH);
        pnlField.add(comp, BorderLayout.CENTER);
        return pnlField;
    }

    private JTextField buildTextField(int width) {
        JTextField txtField = new JTextField();
        txtField.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        txtField.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtField.setForeground(PRIMARY);
        txtField.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        txtField.setPreferredSize(new Dimension(width, 28));
        return txtField;
    }

    private JComboBox<String> buildStatusCombo() {
        JComboBox<String> cboStatus = new JComboBox<>(new String[] { "", "Hoạt động", "Bảo trì" });
        cboStatus.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        cboStatus.setBackground(GuiTheme.SEARCH_FIELD_BG);
        cboStatus.setForeground(PRIMARY);
        cboStatus.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        cboStatus.setPreferredSize(new Dimension(90, 28));
        cboStatus.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setForeground(PRIMARY);
                return c;
            }
        });
        return cboStatus;
    }

    private JSpinner buildCoachSpinner() {
        JSpinner spnCoach = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        spnCoach.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor)spnCoach.getEditor()).getTextField().setForeground(PRIMARY);
        spnCoach.setPreferredSize(new Dimension(100, 30));
        spnCoach.setBorder(new LineBorder(GuiTheme.SEARCH_FIELD_BORDER, 1, true));
        return spnCoach;
    }

    private JPanel buildActionBlock() {
        JPanel pnlBlock = new JPanel(new BorderLayout());
        pnlBlock.setOpaque(false);
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        pnlButtons.setOpaque(false);

        JButton btnReset = buildNavyButton("Xóa bộ lọc");
        JButton btnSearch = buildNavyButton("Tra cứu");

        pnlButtons.add(btnReset);
        pnlButtons.add(btnSearch);
        pnlBlock.add(pnlButtons, BorderLayout.CENTER);
        return pnlBlock;
    }

    private JButton buildNavyButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GuiTheme.NAVY_DARK
                    : getModel().isRollover() ? GuiTheme.NAVY_HOVER : GuiTheme.NAVY);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(Color.WHITE);
                g2.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 14));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,(getWidth()-fm.stringWidth(text))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildTablePanel() {
        JPanel pnlWrap = new JPanel(new BorderLayout(0, 8));
        pnlWrap.setOpaque(false);
        pnlWrap.add(buildSectionTitle("Danh sách tàu"), BorderLayout.NORTH);

        DefaultTableModel tblModel = new DefaultTableModel(
            new Object[] { "STT", "Mã tàu", "Tên tàu", "Số toa", "Năm sản xuất", "Trạng thái", "Ghi chú" },
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tblModel.addRow(new Object[] { "01", "SE1", "Tàu Bắc Nam", 10, 2020, "Bảo trì", "Kiểm tra điều hòa" });
        tblModel.addRow(new Object[] { "02", "SE4", "Tàu Biển Đông", 10, 2020, "Hoạt động", "" });
        tblModel.addRow(new Object[] { "03", "SE5", "Tàu Miền Trung", 10, 2020, "Hoạt động", "" });

        JTable tblData = new JTable(tblModel);
        tblData.setRowHeight(28);
        tblData.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.setForeground(GuiTheme.TEXT);
        tblData.setGridColor(new Color(230, 233, 238));
        tblData.setSelectionBackground(new Color(207, 209, 214));
        tblData.setSelectionForeground(GuiTheme.TEXT);
        tblData.getTableHeader().setReorderingAllowed(false);
        tblData.getTableHeader().setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        tblData.getTableHeader().setBackground(Color.WHITE);
        tblData.getTableHeader().setForeground(GuiTheme.TEXT);
        tblData.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblData.getColumnModel().getColumn(0).setCellRenderer(center);
        tblData.getColumnModel().getColumn(1).setCellRenderer(center);
        tblData.getColumnModel().getColumn(3).setCellRenderer(center);
        tblData.getColumnModel().getColumn(4).setCellRenderer(center);
        tblData.getColumnModel().getColumn(5).setCellRenderer(center);

        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);

        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 7) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(50);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(150);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(90);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(110);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(170);
            }
        });

        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    private JPanel buildSectionTitle(String title) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel lb = new JLabel(title);
        lb.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 14));
        lb.setForeground(Color.WHITE);
        lb.setOpaque(true);
        lb.setBackground(PRIMARY);
        lb.setBorder(new EmptyBorder(6, 12, 6, 12));
        pnl.add(lb);
        return pnl;
    }

    private static final class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color stroke;
        private final float strokeWidth;

        private RoundedPanel(int arc, Color fill, Color stroke, float strokeWidth) {
            this.arc = arc;
            this.fill = fill;
            this.stroke = stroke;
            this.strokeWidth = strokeWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
