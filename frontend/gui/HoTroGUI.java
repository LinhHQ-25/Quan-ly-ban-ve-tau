package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class HoTroGUI extends JPanel {

    // ===== DỮ LIỆU TAB =====
    private static final String[] TAB_TITLES = {
        "Tổng quan hệ thống",
        "Quy trình đặt vé tàu",
        "Quy trình đổi trả vé",
        "Thống kê ca làm việc",
        "Bảng phím tắt nhanh",
        "Câu hỏi thường gặp",
        "Liên hệ kỹ thuật"
    };

    private static final String[] TAB_BADGES = {
        "Giới thiệu chung",
        "Nghiệp vụ cơ bản",
        "Nghiệp vụ nâng cao",
        "Thống kê kết ca",
        "Tối ưu thao tác bán vé",
        "Xử lý sự cố nhanh",
        "Hỗ trợ 24/7"
    };

    private static final String[] TAB_CARDS = {
        "OVERVIEW", "DAT_VE", "DOI_TRA", "THONG_KE_CA", "PHIM_TAT", "FAQ", "LIEN_HE"
    };

    private static final String[] ICON_PATHS = {
        "/images/HoTro.png",
        "/images/trainTicket.png",
        "/images/change.png",
        "/images/ThongKe.png",
        "/images/HoTro.png",
        "/images/traCuu.png",
        "/images/KhachHang.png"
    };

    // ===== STATE =====
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rightCardPanel = new JPanel(cardLayout);
    private final JPanel sidebarPanel = new JPanel();
    private final List<TabButton> buttons = new ArrayList<>();

    private TabButton activeBtn = null;
    private JTextField txtSearch;
    private JLabel lblNoResultsQuery;
    private JLabel lblContentTitle;
    private JLabel lblContentBadge;

    // ===== CONSTRUCTOR =====
    HoTroGUI() {
        setBackground(GuiTheme.LIGHT_BG);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(0, GuiTheme.PAGE_PAD_LEFT, GuiTheme.PAGE_PAD_BOTTOM, GuiTheme.PAGE_PAD_LEFT));

        add(buildHeaderPanel(), BorderLayout.NORTH);

        JPanel pnlMain = new JPanel(new BorderLayout(20, 0));
        pnlMain.setOpaque(false);
        pnlMain.add(buildSidebarPanel(), BorderLayout.WEST);
        pnlMain.add(buildContentPanel(), BorderLayout.CENTER);
        add(pnlMain, BorderLayout.CENTER);

        if (!buttons.isEmpty()) switchTab(0);
    }

    // ===== HEADER =====
    private JPanel buildHeaderPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(10, 0, 15, 0));

        JPanel pnlTitles = new JPanel();
        pnlTitles.setOpaque(false);
        pnlTitles.setLayout(new BoxLayout(pnlTitles, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Tìm kiếm tài liệu hướng dẫn");
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(GuiTheme.NAVY_DARK);

        JLabel lblDesc = new JLabel("Tra cứu nhanh phím tắt và quy trình nghiệp vụ cho nhân viên bán vé.");
        lblDesc.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(GuiTheme.SUB_TEXT);

        pnlTitles.add(lblTitle);
        pnlTitles.add(Box.createVerticalStrut(4));
        pnlTitles.add(lblDesc);
        pnl.add(pnlTitles, BorderLayout.WEST);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        pnlSearch.setOpaque(false);
        txtSearch = new JTextFieldWithPlaceholder("Nhập từ khóa cần tra cứu...");
        txtSearch.setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBackground(GuiTheme.SEARCH_FIELD_BG);
        txtSearch.setForeground(GuiTheme.TEXT);
        GuiTheme.setupRoundedComponent(txtSearch);
        txtSearch.setPreferredSize(new Dimension(320, 36));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterDocs(); }
            public void removeUpdate(DocumentEvent e) { filterDocs(); }
            public void changedUpdate(DocumentEvent e) { filterDocs(); }
        });
        pnlSearch.add(txtSearch);
        pnl.add(pnlSearch, BorderLayout.EAST);
        return pnl;
    }

    // ===== SIDEBAR =====
    private JPanel buildSidebarPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout());
        pnlOuter.setPreferredSize(new Dimension(240, 0));
        pnlOuter.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel lblSidebarTitle = new JLabel("DANH MỤC HƯỚNG DẪN", SwingConstants.CENTER);
        lblSidebarTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lblSidebarTitle.setForeground(GuiTheme.NAVY);
        lblSidebarTitle.setBorder(new EmptyBorder(5, 0, 12, 0));

        JPanel pnlTitleBlock = new JPanel(new BorderLayout());
        pnlTitleBlock.setOpaque(false);
        pnlTitleBlock.add(lblSidebarTitle, BorderLayout.CENTER);

        JPanel pnlSep = new JPanel();
        pnlSep.setBackground(new Color(225, 230, 240));
        pnlSep.setPreferredSize(new Dimension(0, 1));
        pnlTitleBlock.add(pnlSep, BorderLayout.SOUTH);

        pnlOuter.add(pnlTitleBlock, BorderLayout.NORTH);

        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setOpaque(false);
        sidebarPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        for (int i = 0; i < TAB_TITLES.length; i++) {
            Icon icon = GuiIcons.loadIcon(HoTroGUI.class, ICON_PATHS[i], 16, 16);
            TabButton btn = new TabButton(TAB_TITLES[i], icon, i);
            sidebarPanel.add(btn);
            sidebarPanel.add(Box.createVerticalStrut(6));
            buttons.add(btn);
        }

        // Đặt trực tiếp sidebarPanel vào pnlOuter, không dùng JScrollPane
        // JScrollPane chặn mouseEntered/mouseExited làm hover không hoạt động
        sidebarPanel.setPreferredSize(new Dimension(220, TAB_TITLES.length * 52));
        pnlOuter.add(sidebarPanel, BorderLayout.CENTER);
        return pnlOuter;
    }

    // ===== CONTENT PANEL =====
    private JPanel buildContentPanel() {
        RoundedPanel pnlOuter = new RoundedPanel(12, Color.WHITE, GuiTheme.SEARCH_FIELD_BORDER, 1.0f);
        pnlOuter.setLayout(new BorderLayout());
        pnlOuter.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Header nội dung động
        JPanel pnlContentHeaderOuter = new JPanel();
        pnlContentHeaderOuter.setOpaque(false);
        pnlContentHeaderOuter.setLayout(new BoxLayout(pnlContentHeaderOuter, BoxLayout.Y_AXIS));

        JPanel pnlContentHeader = new JPanel(new BorderLayout());
        pnlContentHeader.setOpaque(false);
        pnlContentHeader.setBorder(new EmptyBorder(0, 0, 12, 0));

        lblContentTitle = new JLabel("Tổng quan hệ thống");
        lblContentTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
        lblContentTitle.setForeground(GuiTheme.NAVY);

        lblContentBadge = new RoundedBadge("Giới thiệu chung");

        pnlContentHeader.add(lblContentTitle, BorderLayout.WEST);
        pnlContentHeader.add(lblContentBadge, BorderLayout.EAST);
        pnlContentHeaderOuter.add(pnlContentHeader);

        JPanel pnlHeaderSep = new JPanel();
        pnlHeaderSep.setBackground(new Color(218, 224, 235));
        pnlHeaderSep.setPreferredSize(new Dimension(0, 1));
        pnlHeaderSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        pnlContentHeaderOuter.add(pnlHeaderSep);
        pnlContentHeaderOuter.add(Box.createVerticalStrut(15));

        pnlOuter.add(pnlContentHeaderOuter, BorderLayout.NORTH);

        // Xây dựng các card nội dung bằng thuần Swing — không dùng HTML
        rightCardPanel.setOpaque(false);
        buildCard_Overview();
        buildCard_DatVe();
        buildCard_DoiTra();
        buildCard_ThongKeCa();
        buildCard_PhimTat();
        buildCard_FAQ();
        buildCard_LienHe();

        // Card "Không tìm thấy kết quả"
        JPanel pnlNoResults = new JPanel(new GridBagLayout());
        pnlNoResults.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);
        JLabel lblIcon = new JLabel(GuiIcons.loadIcon(HoTroGUI.class, "/images/traCuu.png", 48, 48), SwingConstants.CENTER);
        pnlNoResults.add(lblIcon, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 5, 0);
        JLabel lblNoTitle = new JLabel("Không tìm thấy kết quả phù hợp", SwingConstants.CENTER);
        lblNoTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
        lblNoTitle.setForeground(GuiTheme.TEXT);
        pnlNoResults.add(lblNoTitle, gbc);
        gbc.gridy = 2;
        lblNoResultsQuery = new JLabel("", SwingConstants.CENTER);
        lblNoResultsQuery.setFont(GuiTheme.font("Segoe UI", Font.ITALIC, 14));
        lblNoResultsQuery.setForeground(GuiTheme.SUB_TEXT);
        pnlNoResults.add(lblNoResultsQuery, gbc);
        rightCardPanel.add(pnlNoResults, "NO_RESULTS");

        pnlOuter.add(rightCardPanel, BorderLayout.CENTER);
        return pnlOuter;
    }

    // ===== CÁC CARD NỘI DUNG (THUẦN SWING) =====

    private void buildCard_Overview() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Giới thiệu");
        addParagraph(body,
            "Chào mừng bạn đến với hệ thống Quản lý bán vé tàu hỏa. " +
            "Đây là cổng hỗ trợ thông tin và hướng dẫn nghiệp vụ dành riêng cho nhân viên bán vé mới.");
        addParagraph(body,
            "Hệ thống được thiết kế tối ưu hóa nhằm giúp bạn thực hiện các thao tác tìm kiếm chuyến đi, " +
            "đặt chỗ, đổi trả vé và báo cáo ca làm việc một cách chính xác và nhanh chóng nhất.");
        addSectionTitle(body, "MỤC TIÊU BÀI HỌC CHO NHÂN VIÊN MỚI");
        addBullet(body, "Hiểu rõ quy trình tra cứu chuyến đi và đặt vé tàu cho khách hàng.");
        addBullet(body, "Nắm vững nghiệp vụ đổi vé, trả vé và tính toán mức phí hoàn trả theo quy định.");
        addBullet(body, "Biết cách quản lý ca làm việc cá nhân (Mở ca, bàn giao ca) chính xác, khớp số liệu tiền mặt thực tế.");
        addBullet(body, "Thành thạo các tổ hợp phím tắt để tối ưu hóa thời gian phục vụ hành khách tại quầy.");
        addParagraph(body, "Hãy lựa chọn các chuyên mục hướng dẫn cụ thể ở thanh thực đơn bên trái để bắt đầu học thao tác!");
        rightCardPanel.add(card, "OVERVIEW");
    }

    private void buildCard_DatVe() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Quy trình đặt vé tàu — 5 bước chuẩn");
        addParagraph(body, "Để hoàn tất một giao dịch đặt vé cho khách hàng, vui lòng tuân thủ quy trình sau:");
        addStepRow(body, "Bước 1", "Tra cứu thông tin hành trình",
            "Tại giao diện Đặt vé tàu, chọn Ga đi, Ga đến, Ngày đi. Sau đó nhấn Tìm kiếm.");
        addStepRow(body, "Bước 2", "Chọn Toa & Chỗ ngồi",
            "Hệ thống hiển thị sơ đồ tàu trực quan. Toa màu xanh là còn chỗ trống. Click chọn Toa, rồi click chọn Ghế trống (màu trắng).");
        addStepRow(body, "Bước 3", "Nhập thông tin hành khách",
            "Bắt buộc nhập chính xác Số CCCD/Hộ chiếu và Họ tên theo giấy tờ tùy thân.");
        addStepRow(body, "Bước 4", "Áp dụng ưu đãi & khuyến mãi",
            "Trẻ em dưới 6 tuổi: miễn phí hoặc giảm giá. Sinh viên: giảm 10%. Người cao tuổi (≥60): giảm 15%.");
        addStepRow(body, "Bước 5", "Thanh toán & In vé giấy",
            "Xác nhận phương thức thanh toán (Tiền mặt hoặc VietQR). Sau khi xác nhận thành công, hệ thống sẽ in vé.");
        rightCardPanel.add(card, "DAT_VE");
    }

    private void buildCard_DoiTra() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Quy trình đổi trả vé");
        addParagraph(body, "Nghiệp vụ đổi trả vé yêu cầu độ chính xác cao để bảo vệ quyền lợi khách hàng và tránh thất thoát quỹ ca.");
        addSectionTitle(body, "1. Quy trình thực hiện chung");
        addBullet(body, "Tìm kiếm vé cần xử lý bằng Mã vé hoặc Số điện thoại khách hàng tại màn hình Đổi/Trả vé.");
        addBullet(body, "Xác minh thông tin hành khách trùng khớp với vé hệ thống trước khi tiếp tục.");
        addSectionTitle(body, "2. Chính sách Trả vé (Hoàn tiền)");
        addTableRow(body, true, "Loại vé & Thời gian yêu cầu", "Phí khấu trừ", "Điều kiện áp dụng");
        addTableRow(body, false, "Vé cá nhân: Trước chạy tàu ≥ 48 giờ", "10% giá vé", "Hoàn lại 90% tiền vé");
        addTableRow(body, false, "Vé cá nhân: Trước chạy tàu 12 – 48 giờ", "20% giá vé", "Hoàn lại 80% tiền vé");
        addTableRow(body, false, "Vé cá nhân: Trước chạy tàu < 12 giờ", "Không áp dụng", "Không hoàn trả vé");
        addTableRow(body, false, "Vé nhóm: Trước chạy tàu ≥ 72 giờ", "20% giá vé", "Hoàn lại 80% tiền vé");
        addTableRow(body, false, "Vé nhóm: Trước chạy tàu 24 – 72 giờ", "30% giá vé", "Hoàn lại 70% tiền vé");
        addTableRow(body, false, "Vé nhóm: Trước chạy tàu < 24 giờ", "Không áp dụng", "Không hoàn trả vé");
        addSectionTitle(body, "3. Chính sách Đổi vé");
        addBullet(body, "Chỉ áp dụng đối với vé cá nhân (không áp dụng cho vé mua theo nhóm).");
        addBullet(body, "Yêu cầu thực hiện đổi vé trước giờ tàu chạy ít nhất 24 giờ.");
        addBullet(body, "Mức phí đổi vé cố định là 30.000 đ / vé.");
        addBullet(body, "Hành khách thanh toán thêm chênh lệch nếu giá vé mới cao hơn giá vé cũ.");
        rightCardPanel.add(card, "DOI_TRA");
    }

    private void buildCard_ThongKeCa() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Nghiệp vụ Báo cáo & Thống kê ca trực");
        addParagraph(body, "Nghiệp vụ thống kê giúp nhân viên theo dõi doanh số bán vé trong ngày, tự kiểm tra quỹ tiền mặt và thực hiện chốt ca đúng quy định để bàn giao cho ca tiếp theo.");
        
        addSectionTitle(body, "1. Các chỉ số thống kê cá nhân thời gian thực");
        addBullet(body, "Doanh số bán vé: Tổng số vé đã in và xuất thành công cho khách hàng.");
        addBullet(body, "Doanh thu tiền mặt (Cash): Tổng tiền mặt thu trực tiếp từ khách tại quầy (dùng để nộp lại cuối ca).");
        addBullet(body, "Doanh thu chuyển khoản (VietQR): Tổng tiền khách thanh toán qua quét mã QR (đã khớp lệnh tự động).");
        addBullet(body, "Doanh thu hoàn trả: Số tiền mặt đã chi ra để hoàn vé cho khách theo chính sách đổi trả.");

        addSectionTitle(body, "2. Quy trình 4 bước chốt ca & Bàn giao quỹ tiền mặt");
        addStepRow(body, "Bước 1", "Kiểm đếm tiền mặt thực tế",
            "Đếm toàn bộ số tiền mặt trong ngăn kéo. Loại trừ số tiền lẻ nhận bàn giao đầu ca để ra số tiền thực thu.");
        addStepRow(body, "Bước 2", "Đối chiếu số liệu hệ thống",
            "Mở giao diện 'Thống kê ca trực' (phím tắt Ctrl + G). Đối chiếu số tiền mặt thực đếm với số tiền mặt báo cáo trên phần mềm.");
        addStepRow(body, "Bước 3", "Khai báo kết ca & Bàn giao",
            "Nhập số tiền mặt thực tế nộp lại. Nếu có chênh lệch (thừa hoặc thiếu tiền mặt so với phần mềm), bắt buộc ghi rõ lý do để Quản lý duyệt.");
        addStepRow(body, "Bước 4", "Bấm chốt ca & Đăng xuất",
            "Nhấn nút 'Kết ca' để gửi dữ liệu chốt ca trực đến tài khoản Quản lý, sau đó bấm Ctrl + L để đăng xuất và bàn giao quầy.");
        rightCardPanel.add(card, "THONG_KE_CA");
    }

    private void buildCard_PhimTat() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Bảng phím tắt nhanh");
        addParagraph(body, "Sử dụng phím tắt giúp nhân viên thao tác nhanh hơn, giảm thiểu thời gian chờ của hành khách tại quầy.");
        addTableRow(body, true, "Phím tắt", "Màn hình / Chức năng", "Mô tả chi tiết");
        addTableRow(body, false, "F1", "Màn hình Hỗ trợ", "Mở ngay trang hướng dẫn sử dụng và bảng phím tắt này.");
        addTableRow(body, false, "Ctrl + B", "Màn hình Đặt vé tàu", "Mở nhanh giao diện đặt vé tàu để phục vụ khách mới.");
        addTableRow(body, false, "Ctrl + R", "Màn hình Đổi/Trả vé", "Chuyển nhanh sang tab đổi vé, hoàn vé cho khách hàng.");
        addTableRow(body, false, "Ctrl + F", "Thực đơn Tra cứu", "Mở/Đóng nhanh thanh menu phụ dùng cho các mục tìm kiếm.");
        addTableRow(body, false, "Ctrl + D", "Danh sách chuyến đi", "Tra cứu nhanh thông tin các chuyến tàu chạy trong ngày.");
        addTableRow(body, false, "Ctrl + H", "Trang Thông tin cá nhân", "Quay lại trang thông tin tài khoản nhân viên đang đăng nhập.");
        addTableRow(body, false, "Ctrl + G", "Màn hình Thống kê ca làm", "Kiểm tra tổng số vé đã bán và doanh thu lũy kế của ca hiện tại.");
        addTableRow(body, false, "Ctrl + L", "Đăng xuất tài khoản", "Đăng xuất nhanh khỏi hệ thống để giao máy cho ca sau.");
        addTableRow(body, false, "F5", "Làm mới (Refresh)", "Tải lại dữ liệu màn hình hiện tại để cập nhật ghế trống mới nhất.");
        addTableRow(body, false, "ESC", "Đóng pop-up nhanh", "Tự động đóng bất kỳ hộp thoại JDialog nào đang hiển thị.");
        rightCardPanel.add(card, "PHIM_TAT");
    }

    private void buildCard_FAQ() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Câu hỏi thường gặp (FAQ)");
        addParagraph(body, "Dưới đây là cẩm nang giải quyết các sự cố thường gặp nhất khi bán vé tại quầy:");
        addSectionTitle(body, "Q1: Khách mua vé giảm giá nhưng không mang theo giấy tờ minh chứng?");
        addParagraph(body,
            "Giải pháp: Tuyệt đối không xuất vé giảm giá nếu khách không xuất trình được giấy tờ gốc hợp lệ " +
            "(Thẻ sinh viên, Giấy khai sinh/CCCD của trẻ). Giải thích lịch sự và hướng dẫn khách mua vé Người lớn bình thường.");
        addSectionTitle(body, "Q2: Khách mua vé xong làm mất hoặc rách ướt vé giấy?");
        addParagraph(body,
            "Giải pháp: Yêu cầu khách cung cấp SĐT hoặc CCCD đã dùng để đặt vé. " +
            "Dùng màn hình Tra cứu vé để tìm lại thông tin, xác nhận đúng thông tin cá nhân rồi thực hiện in lại vé.");
        addSectionTitle(body, "Q3: Khách thanh toán QR thành công nhưng hệ thống chưa báo nhận?");
        addParagraph(body,
            "Giải pháp: Yêu cầu khách cung cấp hóa đơn giao dịch trên ứng dụng ngân hàng. Đối chiếu Mã giao dịch " +
            "và Nội dung chuyển khoản (phải khớp Mã hóa đơn). Chụp màn hình làm minh chứng rồi bấm Xác nhận thanh toán thủ công.");
        rightCardPanel.add(card, "FAQ");
    }

    private void buildCard_LienHe() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Liên hệ kỹ thuật & Hỗ trợ");
        addParagraph(body,
            "Trong trường hợp gặp sự cố hệ thống (lỗi kết nối cơ sở dữ liệu, lỗi máy in vé, hoặc lỗi phần mềm " +
            "không thể tự khắc phục), vui lòng liên hệ ngay các đầu mối sau:");
        addSectionTitle(body, "1. Đội ngũ Hỗ trợ kỹ thuật phần mềm (IT Helpdesk)");
        addBullet(body, "Hotline nội bộ: nhánh số 102 (Phục vụ 24/7).");
        addBullet(body, "Email liên hệ: support.railway@railway.gov.vn");
        addBullet(body, "Người chịu trách nhiệm chính: Kỹ sư Nguyễn Văn A (SĐT: 0987.654.321).");
        addSectionTitle(body, "2. Người quản lý ca trực trực tiếp tại ga");
        addBullet(body,
            "Vui lòng báo cáo ngay với Trưởng ga hoặc Trưởng ca trực để được hướng dẫn xử lý " +
            "các vấn đề phát sinh liên quan đến khách hàng, tiền mặt bàn giao ca hoặc sự cố hạ tầng tại ga.");
        addSectionTitle(body, "3. Tài liệu số hóa");
        addParagraph(body,
            "Truy cập mạng nội bộ để xem video hướng dẫn chi tiết và tài liệu Wiki tại: " +
            "https://wiki.railway.vn/docs/huong-dan-ban-ve");
        rightCardPanel.add(card, "LIEN_HE");
    }

    // ===== HELPER BUILD SWING CONTENT =====

    /** Tạo một card có JScrollPane bao ngoài một body JPanel dạng BoxLayout Y */
    private JPanel makeScrollCard() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(5, 5, 20, 5));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /** Tiêu đề lớn */
    private void addTitle(JPanel body, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(GuiTheme.NAVY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        body.add(lbl);
    }

    /** Tiêu đề phần (section) */
    private void addSectionTitle(JPanel body, String text) {
        body.add(Box.createVerticalStrut(10));
        JLabel lbl = new JLabel(text);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(28, 52, 92));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(4, 0, 6, 0));
        body.add(lbl);
    }

    /** Đoạn văn bản */
    private void addParagraph(JPanel body, String text) {
        JLabel lbl = makeWrappedLabel(text, GuiTheme.font("Segoe UI", Font.PLAIN, 13), GuiTheme.TEXT);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        body.add(lbl);
    }

    /** Bullet point */
    private void addBullet(JPanel body, String text) {
        JLabel lbl = makeWrappedLabel("  •  " + text, GuiTheme.font("Segoe UI", Font.PLAIN, 13), GuiTheme.TEXT);
        lbl.setBorder(new EmptyBorder(2, 10, 4, 0));
        body.add(lbl);
    }

    /** Row bước (số thứ tự + tiêu đề bước + mô tả) */
    private void addStepRow(JPanel body, String step, String title, String desc) {
        body.add(Box.createVerticalStrut(6));
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        // Badge số bước
        JLabel badge = new JLabel(step, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GuiTheme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setPreferredSize(new Dimension(56, 32));
        badge.setOpaque(false);

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(71, 71, 156));
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblDesc = makeWrappedLabel(desc, GuiTheme.font("Segoe UI", Font.PLAIN, 13), GuiTheme.TEXT);
        lblDesc.setAlignmentX(LEFT_ALIGNMENT);

        textBlock.add(lblTitle);
        textBlock.add(Box.createVerticalStrut(2));
        textBlock.add(lblDesc);

        row.add(badge, BorderLayout.WEST);
        row.add(textBlock, BorderLayout.CENTER);

        JPanel rowWrap = new JPanel(new BorderLayout());
        rowWrap.setBackground(new Color(248, 250, 255));
        rowWrap.setBorder(new EmptyBorder(8, 10, 8, 10));
        rowWrap.setAlignmentX(LEFT_ALIGNMENT);
        rowWrap.add(row, BorderLayout.CENTER);

        body.add(rowWrap);
        body.add(Box.createVerticalStrut(4));
    }

    /** Row bảng */
    private void addTableRow(JPanel body, boolean isHeader, String col1, String col2, String col3) {
        JPanel row = new JPanel(new java.awt.GridLayout(1, 3, 1, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);

        Color bg = isHeader ? new Color(238, 242, 250) : Color.WHITE;
        Font font = isHeader
            ? GuiTheme.font("Segoe UI", Font.BOLD, 12)
            : GuiTheme.font("Segoe UI", Font.PLAIN, 12);
        Color fg = isHeader ? new Color(37, 69, 121) : GuiTheme.TEXT;

        for (String col : new String[]{col1, col2, col3}) {
            JLabel cell = new JLabel(col);
            cell.setFont(font);
            cell.setForeground(fg);
            cell.setBackground(bg);
            cell.setOpaque(true);
            cell.setBorder(new EmptyBorder(7, 10, 7, 10));
            row.add(cell);
        }

        row.setBackground(new Color(218, 224, 235));
        row.setBorder(new EmptyBorder(1, 0, 0, 0));
        body.add(row);
    }

    /** JLabel tự xuống dòng bằng HTML wrapper (chỉ cho text, không có HTML content) */
    private JLabel makeWrappedLabel(String text, Font font, Color fg) {
        JLabel lbl = new JLabel("<html><body style='width:520px'>" + escapeHtml(text) + "</body></html>");
        lbl.setFont(font);
        lbl.setForeground(fg);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ===== LOGIC SWITCH TAB =====

    private void switchTab(int index) {
        if (index < 0 || index >= buttons.size()) return;

        TabButton newBtn = buttons.get(index);
        for (TabButton btn : buttons) {
            if (btn != newBtn) btn.setActive(false);
        }
        activeBtn = newBtn;
        activeBtn.setActive(true);

        lblContentTitle.setText(TAB_TITLES[index]);
        lblContentBadge.setText(TAB_BADGES[index]);
        cardLayout.show(rightCardPanel, TAB_CARDS[index]);
        sidebarPanel.repaint();
    }

    public void selectTabForScreen(String screenKey) {
        switchTab(0);
    }

    // ===== FILTER =====

    private void filterDocs() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            for (TabButton btn : buttons) btn.setVisible(true);
            sidebarPanel.revalidate();
            sidebarPanel.repaint();
            int activeIdx = buttons.indexOf(activeBtn);
            switchTab(activeIdx != -1 ? activeIdx : 0);
            return;
        }

        int firstVisibleIdx = -1;
        for (int i = 0; i < buttons.size(); i++) {
            boolean matches = TAB_TITLES[i].toLowerCase().contains(query);
            buttons.get(i).setVisible(matches);
            if (matches && firstVisibleIdx == -1) firstVisibleIdx = i;
        }

        if (firstVisibleIdx != -1) {
            boolean activeStillVisible = activeBtn != null && activeBtn.isVisible();
            if (!activeStillVisible) {
                switchTab(firstVisibleIdx);
            } else {
                int activeIdx = buttons.indexOf(activeBtn);
                if (activeIdx != -1) switchTab(activeIdx);
            }
        } else {
            lblNoResultsQuery.setText("Không tìm thấy thông tin nào chứa từ khóa: \"" + txtSearch.getText().trim() + "\"");
            if (activeBtn != null) activeBtn.setActive(false);
            activeBtn = null;
            cardLayout.show(rightCardPanel, "NO_RESULTS");
        }
        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }

    // ===== COLORIZE ICON =====
    private static ImageIcon colorizeIcon(ImageIcon icon, Color color) {
        if (icon == null) return null;
        int w = icon.getIconWidth(), h = icon.getIconHeight();
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;
                if (alpha > 0) {
                    int newArgb = (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
                    img.setRGB(x, y, newArgb);
                }
            }
        }
        return new ImageIcon(img);
    }

    // ===== TAB BUTTON — dùng JPanel thuần, KHÔNG dùng JButton =====
    private final class TabButton extends JPanel {
        private final String title;
        private final int tabIndex;
        private boolean active = false;
        private boolean hover = false;
        private final ImageIcon originalIcon;
        private final ImageIcon activeIcon;
        private final ImageIcon hoverIcon;

        private static final Color COLOR_ACTIVE    = new Color(28, 52, 92);   // GuiTheme.NAVY
        private static final Color COLOR_HOVER_BG  = new Color(235, 240, 250);
        private static final Color COLOR_ACTIVE_FG = Color.WHITE;
        private static final Color COLOR_HOVER_FG  = new Color(28, 52, 92);
        private static final Color COLOR_NORMAL_FG = new Color(73, 80, 87);

        TabButton(String title, Icon rawIcon, int tabIndex) {
            this.title = title;
            this.tabIndex = tabIndex;
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setPreferredSize(new Dimension(220, 46));
            setAlignmentX(LEFT_ALIGNMENT);

            if (rawIcon instanceof ImageIcon base) {
                this.originalIcon = colorizeIcon(base, COLOR_NORMAL_FG);
                this.activeIcon   = colorizeIcon(base, Color.WHITE);
                this.hoverIcon    = colorizeIcon(base, COLOR_HOVER_FG);
            } else {
                this.originalIcon = null;
                this.activeIcon   = null;
                this.hoverIcon    = null;
            }

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
                @Override public void mousePressed(MouseEvent e) {
                    switchTab(tabIndex);
                }
            });
        }

        public String getPlainTextTitle() { return title; }
        public boolean isActive() { return active; }

        public void setActive(boolean active) {
            this.active = active;
            if (active) hover = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Nền
            if (active) {
                g2.setColor(COLOR_ACTIVE);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            } else if (hover) {
                g2.setColor(COLOR_HOVER_BG);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            }

            // Icon
            ImageIcon ico = active ? activeIcon : (hover ? hoverIcon : originalIcon);
            int iconX = 15;
            int iconY = (h - 16) / 2;
            if (ico != null) {
                g2.drawImage(ico.getImage(), iconX, iconY, 16, 16, this);
            }

            // Text
            g2.setColor(active ? COLOR_ACTIVE_FG : (hover ? COLOR_HOVER_FG : COLOR_NORMAL_FG));
            g2.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int textX = iconX + 16 + 12;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(title, textX, textY);

            g2.dispose();
        }
    }

    // ===== INNER CLASSES =====

    private static final class RoundedBadge extends JLabel {
        public RoundedBadge(String text) {
            super(text);
            setFont(GuiTheme.font("Segoe UI", Font.PLAIN, 12));
            setForeground(GuiTheme.SUB_TEXT);
            setBorder(new EmptyBorder(4, 10, 4, 10));
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(241, 243, 245));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(new Color(218, 224, 235));
            g2.setStroke(new java.awt.BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill, stroke;
        private final float strokeWidth;
        RoundedPanel(int arc, Color fill, Color stroke, float strokeWidth) {
            this.arc = arc; this.fill = fill; this.stroke = stroke; this.strokeWidth = strokeWidth;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(stroke);
            g2.setStroke(new java.awt.BasicStroke(strokeWidth));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class JTextFieldWithPlaceholder extends JTextField {
        private final String placeholder;
        private final ImageIcon searchIcon;
        JTextFieldWithPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            this.searchIcon = GuiIcons.loadIcon(HoTroGUI.class, "/images/traCuu.png", 16, 16);
            setBorder(new EmptyBorder(5, 32, 5, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (searchIcon != null) {
                int iconY = (getHeight() - searchIcon.getIconHeight()) / 2;
                g2.drawImage(searchIcon.getImage(), 10, iconY, this);
            }
            if (getText().isEmpty() && placeholder != null) {
                g2.setColor(new Color(160, 160, 160));
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                int padding = getInsets().left;
                g2.drawString(placeholder, padding, g2.getFontMetrics().getAscent() + (getHeight() - g2.getFontMetrics().getHeight()) / 2);
            }
            g2.dispose();
        }
    }
}