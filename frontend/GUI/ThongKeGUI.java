package GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
final class ThongKeGUI extends JPanel {
    private static final Color BORDER = new Color(210, 215, 224);
    private static final Color FIELD_BG = new Color(141, 184, 219);
    private static final Color PRIMARY = new Color(71, 71, 156);
    ThongKeGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout());

        JPanel pnlPage = new JPanel();
        pnlPage.setOpaque(false);
        pnlPage.setLayout(new BoxLayout(pnlPage, BoxLayout.Y_AXIS));
        pnlPage.setBorder(new EmptyBorder(
            GuiTheme.PAGE_PAD_TOP,
            GuiTheme.PAGE_PAD_LEFT,
            GuiTheme.PAGE_PAD_BOTTOM,
            GuiTheme.PAGE_PAD_LEFT
        ));
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(buildSummaryBar());
        pnlPage.add(Box.createVerticalStrut(12));
        pnlPage.add(buildTableWithChart());
        add(pnlPage, BorderLayout.NORTH);
    }

    private JPanel buildSummaryBar() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lbDoanhThu = new JLabel("Tổng doanh thu: 0");
        JLabel lbVeBan = new JLabel("Vé đã bán: 0");
        JLabel lbVeHuy = new JLabel("Vé đã hủy: 0");

        Font f = GuiTheme.font("Segoe UI", Font.BOLD, 15);
        lbDoanhThu.setFont(f);
        lbVeBan.setFont(f);
        lbVeHuy.setFont(f);

        pnl.add(lbDoanhThu);
        pnl.add(Box.createHorizontalStrut(40));
        pnl.add(lbVeBan);
        pnl.add(Box.createHorizontalStrut(40));
        pnl.add(lbVeHuy);

        return pnl;
    }
    private JPanel buildTableWithChart() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setOpaque(false);

        pnl.add(buildTablePanel(), BorderLayout.CENTER);
        pnl.add(new ChartPanel(), BorderLayout.EAST);

        return pnl;
    }

    private JPanel buildTablePanel() {
        DefaultTableModel tblModel = new DefaultTableModel(
            new Object[] { "Mã HĐ", "Ga đi", "Ga đến", "Ngày bán vé", "Số lượng HK"},
            0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
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
        JScrollPane spnScroll = new JScrollPane(tblData);
        spnScroll.setBorder(new LineBorder(BORDER, 1, true));
        spnScroll.getViewport().setBackground(Color.WHITE);
        spnScroll.setPreferredSize(new Dimension(10000, 300));
        SwingUtilities.invokeLater(() -> {
            if (tblData.getColumnModel().getColumnCount() >= 7) {
                tblData.getColumnModel().getColumn(0).setPreferredWidth(50);
                tblData.getColumnModel().getColumn(1).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(2).setPreferredWidth(120);
                tblData.getColumnModel().getColumn(3).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(4).setPreferredWidth(100);
                tblData.getColumnModel().getColumn(5).setPreferredWidth(150);
                tblData.getColumnModel().getColumn(6).setPreferredWidth(100);
            }
        });
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);
        pnlWrap.add(spnScroll, BorderLayout.CENTER);
        return pnlWrap;
    }
} 
class ChartPanel extends JPanel {

    private static final Color BORDER = null;

	public ChartPanel() {
        setPreferredSize(new Dimension(220, 220));
        setBackground(Color.WHITE);
        setBorder(new LineBorder(BORDER, 1, true));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // chưa có data → chỉ hiện text
        g.setColor(Color.GRAY);
        g.drawString("Chưa có dữ liệu", 40, 120);
    }
}
