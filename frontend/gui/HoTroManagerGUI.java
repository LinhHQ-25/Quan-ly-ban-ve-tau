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

public final class HoTroManagerGUI extends JPanel {

    // ===== DỮ LIỆU TAB CHO QUẢN LÝ =====
    private static final String[] TAB_TITLES = {
        "Tổng quan vai trò Quản lý",
        "Quản lý Nhân viên & Ca làm",
        "Quản lý Tàu & Chuyến tàu",
        "Chính sách Khuyến mãi",
        "Báo cáo & Thống kê doanh thu",
        "Bảng phím tắt nhanh",
        "Câu hỏi thường gặp",
        "Liên hệ kỹ thuật"
    };

    private static final String[] TAB_BADGES = {
        "Tổng quan điều hành",
        "Quản lý nhân sự",
        "Quản lý vận tải",
        "Chương trình ưu đãi",
        "Giám sát tài chính",
        "Tối ưu thao tác quản lý",
        "Xử lý sự cố nhanh",
        "Hỗ trợ 24/7"
    };

    private static final String[] TAB_CARDS = {
        "OVERVIEW", "NHAN_VIEN_CA_LAM", "TAU_CHUYEN", "KHUYEN_MAI", "THONG_KE", "PHIM_TAT", "FAQ", "LIEN_HE"
    };

    private static final String[] ICON_PATHS = {
        "/images/logoTrain.png",
        "/images/iconNV.png",
        "/images/Tau.png",
        "/images/KhuyenMai.png",
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
    public HoTroManagerGUI() {
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

        JLabel lblDesc = new JLabel("Tra cứu nhanh các tổ hợp phím tắt, quy trình vận hành và nghiệp vụ điều hành dành riêng cho Quản lý.");
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
            Icon icon = GuiIcons.loadIcon(HoTroManagerGUI.class, ICON_PATHS[i], 16, 16);
            TabButton btn = new TabButton(TAB_TITLES[i], icon, i);
            sidebarPanel.add(btn);
            sidebarPanel.add(Box.createVerticalStrut(6));
            buttons.add(btn);
        }

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

        lblContentTitle = new JLabel("Tổng quan vai trò Quản lý");
        lblContentTitle.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 18));
        lblContentTitle.setForeground(GuiTheme.NAVY);

        lblContentBadge = new RoundedBadge("Tổng quan điều hành");

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
        buildCard_NhanVienCaLam();
        buildCard_TauChuyen();
        buildCard_KhuyenMai();
        buildCard_ThongKe();
        buildCard_PhimTat();
        buildCard_FAQ();
        buildCard_LienHe();

        // Card "Không tìm thấy kết quả"
        JPanel pnlNoResults = new JPanel(new GridBagLayout());
        pnlNoResults.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);
        JLabel lblIcon = new JLabel(GuiIcons.loadIcon(HoTroManagerGUI.class, "/images/traCuu.png", 48, 48), SwingConstants.CENTER);
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
        addTitle(body, "Giới thiệu vai trò Quản lý");
        addParagraph(body,
            "Chào mừng bạn đến với phân hệ quản trị dành riêng cho vai trò Quản lý ga tàu. " +
            "Đây là cổng hỗ trợ cung cấp toàn bộ tài liệu hướng dẫn nghiệp vụ điều hành và quy định chính sách của hệ thống.");
        addParagraph(body,
            "Với tư cách là Quản lý, bạn nắm giữ quyền điều phối nhân sự, kiểm soát tài chính ca trực, " +
            "cấu hình tài sản toa tàu, lập lịch các chuyến đi và thiết lập các chính sách khuyến mãi kích cầu kinh doanh.");
        addSectionTitle(body, "CÁC LĨNH VỰC QUẢN TRỊ TRỌNG TÂM");
        addBullet(body, "Quản lý nhân viên: Cấp mới tài khoản, kích hoạt hoặc tạm khóa tài khoản nhân viên quầy vé.");
        addBullet(body, "Quản lý ca làm việc: Thiết lập khung giờ ca, giám sát khai báo đầu ca và xác nhận bàn giao khớp số liệu thực tế cuối ca.");
        addBullet(body, "Quản lý tàu & toa: Thêm tàu mới, cấu hình số lượng và phân loại toa thường/VIP, kiểm tra trạng thái bảo dưỡng.");
        addBullet(body, "Lập lịch trình chuyến đi: Thiết lập ga đi/ga đến, giờ xuất phát/đến dự kiến và đơn giá chặng cơ sở.");
        addBullet(body, "Cấu hình chính sách khuyến mãi: Triển khai các ưu đãi đối tượng đặc biệt và chiến dịch giảm giá theo thời hạn.");
        addBullet(body, "Giám sát tài chính & doanh số: Phân tích biểu đồ doanh thu thời gian thực và xuất báo cáo tài chính PDF ca làm.");
        addParagraph(body, "Vui lòng bấm chọn danh mục hướng dẫn ở menu bên trái để xem các quy trình nghiệp vụ chi tiết!");
        rightCardPanel.add(card, "OVERVIEW");
    }

    private void buildCard_NhanVienCaLam() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Nghiệp vụ Quản lý Nhân viên & Ca làm việc");
        addParagraph(body, "Công tác quản lý nhân sự chặt chẽ là chìa khóa giúp vận hành quầy vé thông suốt, tránh thất thoát doanh số bán vé.");
        
        addSectionTitle(body, "1. Quy trình quản lý hồ sơ nhân viên");
        addBullet(body, "Thêm mới nhân viên: Nhập đầy đủ Họ tên, CCCD/Hộ chiếu, Số điện thoại, Email và phân quyền vai trò.");
        addBullet(body, "Phân quyền: Hệ thống hỗ trợ 2 vai trò chính là 'Nhân viên bán vé' (bán, đổi, trả vé) và 'Quản lý' (quản trị hệ thống).");
        addBullet(body, "Điều chỉnh trạng thái: Khi nhân viên nghỉ việc hoặc tạm dừng công tác, chuyển trạng thái tài khoản sang 'Ngưng hoạt động' để khóa truy cập.");

        addSectionTitle(body, "2. Quy trình 4 bước kiểm tra ca làm việc");
        addStepRow(body, "Bước 1", "Khởi tạo ca trực mẫu",
            "Định nghĩa các ca trực chuẩn của ga (ví dụ: Ca sáng: 06:00 - 14:00, Ca chiều: 14:00 - 22:00, Ca đêm: 22:00 - 06:00).");
        addStepRow(body, "Bước 2", "Phân công nhân sự quầy",
            "Gán nhân viên bán vé chịu trách nhiệm trực tiếp cho ca trực của ngày cụ thể. Mỗi quầy trực có tối đa 1 nhân viên chịu trách nhiệm quỹ.");
        addStepRow(body, "Bước 3", "Giám sát quá trình Mở ca",
            "Khi vào ca, nhân viên bắt buộc khai báo số tiền mặt nhận bàn giao đầu ca (tiền lẻ trả lại khách). Quản lý duyệt số liệu để nhân viên bắt đầu bán vé.");
        addStepRow(body, "Bước 4", "Đối chiếu & Chốt đóng ca trực",
            "Cuối ca, nhân viên chốt số liệu. Hệ thống tự động tổng hợp doanh thu tiền mặt và chuyển khoản (VietQR). Quản lý đối chiếu số tiền mặt thực tế nhân viên nộp lại, ghi nhận chênh lệch nếu có và bấm Xác nhận đóng ca.");
        rightCardPanel.add(card, "NHAN_VIEN_CA_LAM");
    }

    private void buildCard_TauChuyen() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Điều phối phương tiện & Lập lịch trình chuyến đi");
        addParagraph(body, "Đảm bảo hạ tầng tàu hỏa hoạt động ở hiệu suất tối đa và đáp ứng chính xác nhu cầu đi lại của hành khách.");

        addSectionTitle(body, "1. Quản lý Tàu, Toa và Ghế ngồi");
        addBullet(body, "Cấu hình Tàu: Tạo mới tàu (ví dụ: SE1, SE3, SE5) và quản lý tình trạng hoạt động (Đang chạy, Đang bảo trì, Tạm ngưng).");
        addBullet(body, "Quản lý Toa tàu: Liên kết toa vào đoàn tàu. Phân loại toa cụ thể: Toa ghế ngồi thường, Toa ghế ngồi điều hòa VIP, Toa giường nằm.");
        addBullet(body, "Định cấu hình số ghế: Hệ thống hỗ trợ tự động sinh số ghế theo sơ đồ toa đã chọn để nhân viên bán vé dễ dàng thao tác chọn chỗ trực quan.");

        addSectionTitle(body, "2. Quy trình lập lịch Chuyến tàu mới");
        addStepRow(body, "Bước 1", "Thiết lập thông tin hành trình",
            "Chọn Ga đi (ví dụ: Ga Sài Gòn), Ga đến (ví dụ: Ga Hà Nội) và mã hiệu tàu sẽ đảm nhận chuyến chạy.");
        addStepRow(body, "Bước 2", "Xác định thời gian xuất hành",
            "Nhập chính xác Ngày đi, Giờ khởi hành dự kiến và Giờ đến dự kiến. Hệ thống tự động tính toán tổng thời gian hành trình.");
        addStepRow(body, "Bước 3", "Cấu hình đơn giá gốc chuyến đi",
            "Nhập giá vé cơ sở cho chuyến tàu. Đơn giá ghế thực tế sẽ bằng: [Giá cơ sở] x [Hệ số loại ghế] x [Hệ số toa VIP/Toa thường].");
        rightCardPanel.add(card, "TAU_CHUYEN");
    }

    private void buildCard_KhuyenMai() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Quản lý chính sách Khuyến mãi & Giảm giá");
        addParagraph(body, "Hệ thống hỗ trợ quản lý linh hoạt hai nhóm chính sách giảm giá: Giảm giá đối tượng ưu tiên bắt buộc và các chương trình khuyến mãi kích cầu kinh doanh.");

        addSectionTitle(body, "1. Chính sách giảm giá đối tượng ưu tiên (Theo quy định Nhà nước)");
        addTableRow(body, true, "Đối tượng", "Mức giảm giá", "Giấy tờ xác minh yêu cầu");
        addTableRow(body, false, "Trẻ em dưới 6 tuổi", "Miễn phí (ngồi chung ghế)", "Giấy khai sinh / Hộ chiếu của bé");
        addTableRow(body, false, "Trẻ em từ 6 – 10 tuổi", "Giảm 25% giá vé", "Giấy khai sinh để xác minh tuổi");
        addTableRow(body, false, "Học sinh / Sinh viên", "Giảm 10% giá vé", "Thẻ học sinh, sinh viên còn thời hạn");
        addTableRow(body, false, "Người cao tuổi (≥ 60 tuổi)", "Giảm 15% giá vé", "Thẻ CCCD / Hộ chiếu bản gốc");
        addTableRow(body, false, "Thương binh / Người khuyết tật", "Giảm 30% giá vé", "Thẻ thương binh / Giấy chứng nhận khuyết tật");

        addSectionTitle(body, "2. Quản lý chiến dịch Khuyến mãi kích cầu");
        addBullet(body, "Khởi tạo chiến dịch: Đặt tên chương trình khuyến mãi, mô tả điều kiện áp dụng.");
        addBullet(body, "Định nghĩa mã giảm giá: Thiết lập Tỷ lệ giảm giá (%) hoặc số tiền giảm cụ thể kèm thời gian hiệu lực (Ngày bắt đầu - Ngày kết thúc).");
        addBullet(body, "Điều kiện áp dụng: Có thể ràng buộc theo chặng bay/chặng tàu cụ thể, hoặc số lượng mua vé tối thiểu của hóa đơn.");
        rightCardPanel.add(card, "KHUYEN_MAI");
    }

    private void buildCard_ThongKe() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Giám sát tài chính & Báo cáo doanh thu");
        addParagraph(body, "Công cụ đắc lực giúp Quản lý nắm bắt dòng tiền bán vé, theo dõi hiệu suất kinh doanh của từng chặng chạy và hiệu quả làm việc của nhân viên.");

        addSectionTitle(body, "1. Các bảng số liệu tài chính quan trọng");
        addBullet(body, "Doanh thu tổng hợp: Biểu đồ cột thể hiện biến động doanh thu theo Ngày, Tuần, Tháng, Quý hoặc Năm.");
        addBullet(body, "Tỷ lệ lấp đầy chỗ (Occupancy Rate): Phân tích biểu đồ tròn tỷ lệ chỗ ngồi đã bán trên tổng số ghế của chuyến tàu để điều chỉnh lịch trình phù hợp.");
        addBullet(body, "Bảng xếp hạng hiệu suất: Bảng so sánh doanh số bán vé giữa các nhân viên để khen thưởng hoặc đào tạo lại kỹ năng phục vụ.");

        addSectionTitle(body, "2. Quy trình 3 bước xuất báo cáo tài chính");
        addStepRow(body, "Bước 1", "Lọc dữ liệu báo cáo",
            "Chọn khoảng thời gian cần tổng hợp (ví dụ: Từ ngày 01/05 đến ngày 31/05) và bộ lọc đối tượng (Chuyến tàu, Nhân viên).");
        addStepRow(body, "Bước 2", "Phân tích biểu đồ trực quan",
            "Hệ thống kết xuất dữ liệu và hiển thị biểu đồ xu hướng. Kiểm tra các điểm đỉnh doanh thu và các chặng vắng khách.");
        addStepRow(body, "Bước 3", "Kết xuất văn bản PDF chính thức",
            "Nhấn nút 'Xuất Báo Cáo PDF'. Hệ thống sẽ tự động tổng hợp số liệu, vẽ biểu đồ và xuất file PDF có định dạng chuẩn chỉnh để in ấn ký duyệt.");
        rightCardPanel.add(card, "THONG_KE");
    }

    private void buildCard_PhimTat() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Bảng phím tắt nhanh của Quản lý");
        addParagraph(body, "Sử dụng các tổ hợp phím tắt nhanh giúp Quản lý tối ưu hóa tốc độ làm việc, giảm thời gian thao tác qua lại giữa các màn hình điều hành.");
        addTableRow(body, true, "Phím tắt", "Màn hình / Chức năng", "Mô tả chi tiết tác vụ");
        addTableRow(body, false, "F1", "Trang Hỗ trợ Quản lý", "Mở ngay trang hướng dẫn nghiệp vụ và bảng phím tắt này.");
        addTableRow(body, false, "Ctrl + N", "Quản lý nhân viên", "Chuyển nhanh sang màn hình thêm mới và phân quyền nhân sự.");
        addTableRow(body, false, "Ctrl + C", "Quản lý ca làm việc", "Mở nhanh giao diện giám sát ca trực và chốt sổ tiền mặt.");
        addTableRow(body, false, "Ctrl + U", "Quản lý thông tin tàu", "Chuyển sang màn hình cấu hình tàu và phân chia toa.");
        addTableRow(body, false, "Ctrl + T", "Quản lý chuyến tàu", "Mở nhanh giao diện lập lịch trình chuyến đi và đặt giá vé.");
        addTableRow(body, false, "Ctrl + M", "Quản lý khuyến mãi", "Chuyển sang màn hình cấu hình các đợt giảm giá ưu đãi.");
        addTableRow(body, false, "Ctrl + G", "Thống kê doanh thu", "Mở nhanh trang tổng hợp biểu đồ doanh thu ga tàu.");
        addTableRow(body, false, "Ctrl + H", "Hồ sơ cá nhân", "Quay lại trang thông tin tài khoản của quản lý.");
        addTableRow(body, false, "Ctrl + L", "Đăng xuất nhanh", "Đăng xuất khỏi hệ thống để bảo mật tài khoản quản lý.");
        addTableRow(body, false, "ESC", "Đóng Dialog nhanh", "Đóng lập tức các cửa sổ pop-up đang hiển thị trên màn hình.");
        rightCardPanel.add(card, "PHIM_TAT");
    }

    private void buildCard_FAQ() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Câu hỏi thường gặp & Hướng dẫn xử lý sự cố (FAQ)");
        addParagraph(body, "Dưới đây là cẩm nang giải quyết các sự cố vận hành khẩn cấp thường gặp nhất tại ga dành riêng cho Quản lý:");
        
        addSectionTitle(body, "Q1: Nhân viên báo sai lệch quỹ tiền mặt khi chốt ca trực cuối ngày?");
        addParagraph(body,
            "Giải pháp: Quản lý cùng nhân viên đếm lại tiền mặt thực tế 2 lần độc lập. Kiểm tra lịch sử giao dịch trực tuyến (VietQR) " +
            "để đảm bảo không có giao dịch nào bị chậm lệnh từ phía ngân hàng. Đối chiếu các vé in lỗi đã thu hồi. " +
            "Ghi nhận số chênh lệch vào biên bản kết ca trên phần mềm và duyệt kết ca.");

        addSectionTitle(body, "Q2: Xảy ra tình trạng trùng chỗ ngồi của hành khách trên cùng một toa (Overbooking)?");
        addParagraph(body,
            "Giải pháp: Lỗi này xảy ra do đồng bộ dữ liệu ghế trống bị gián đoạn trong lúc mất mạng tạm thời. " +
            "Quản lý sử dụng quyền admin để tra cứu các chỗ còn trống khác trong cùng chuyến tàu. Tiến hành chuyển hành khách sang " +
            "ghế trống tương đương ở toa khác hoặc nâng cấp lên toa VIP miễn phí nếu đã hết ghế thường.");

        addSectionTitle(body, "Q3: Lỗi kết nối Cơ sở dữ liệu và hệ thống bị đóng băng không thể giao dịch?");
        addParagraph(body,
            "Giải pháp: Yêu cầu nhân viên tạm ngưng giao dịch tại quầy. Báo cáo ngay lên phòng điều hành trung tâm " +
            "và chuyển hướng khách hàng sang hình thức mua vé online hoặc ghi nhận thông tin viết tay tạm thời đối với trường hợp cấp bách.");
        rightCardPanel.add(card, "FAQ");
    }

    private void buildCard_LienHe() {
        JPanel card = makeScrollCard();
        JPanel body = (JPanel) ((JScrollPane) card.getComponent(0)).getViewport().getView();
        addTitle(body, "Liên hệ kỹ thuật & Hỗ trợ điều hành");
        addParagraph(body, "Trong trường hợp gặp sự cố hệ thống nghiêm trọng nằm ngoài thẩm quyền hoặc khả năng tự giải quyết kỹ thuật, vui lòng liên hệ các kênh hỗ trợ sau:");
        
        addSectionTitle(body, "1. Đường dây nóng Hỗ trợ kỹ thuật phần mềm (IT Helpdesk)");
        addBullet(body, "Hotline nội bộ: Nhánh số 102 hoặc 204 (Phục vụ 24/7 cho các ga trực thuộc).");
        addBullet(body, "Email liên hệ chính thức: support.railway@railway.gov.vn");
        addBullet(body, "Trưởng ban hỗ trợ: Kỹ sư Nguyễn Văn A (SĐT: 0987.654.321).");

        addSectionTitle(body, "2. Trung tâm điều hành đường sắt khu vực");
        addBullet(body, "Phòng điều vụ ga trung tâm: Nhánh số 301 (dùng để báo cáo các sự cố chậm tàu, hủy chuyến lớn).");
        addBullet(body, "Đầu mối hỗ trợ khẩn cấp: 0912.345.678 (Mr. Bình - Trưởng ca điều vụ).");

        addSectionTitle(body, "3. Cổng thông tin tài liệu & Wiki nội bộ");
        addParagraph(body, "Truy cập mạng WAN nội bộ của ga để tải tài liệu hướng dẫn kỹ thuật chi tiết tại: https://wiki.railway.vn/docs/admin-manual");
        rightCardPanel.add(card, "LIEN_HE");
    }

    // ===== HELPER BUILD SWING CONTENT =====

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

    private void addTitle(JPanel body, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(GuiTheme.NAVY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        body.add(lbl);
    }

    private void addSectionTitle(JPanel body, String text) {
        body.add(Box.createVerticalStrut(10));
        JLabel lbl = new JLabel(text);
        lbl.setFont(GuiTheme.font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(28, 52, 92));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(4, 0, 6, 0));
        body.add(lbl);
    }

    private void addParagraph(JPanel body, String text) {
        JLabel lbl = makeWrappedLabel(text, GuiTheme.font("Segoe UI", Font.PLAIN, 13), GuiTheme.TEXT);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        body.add(lbl);
    }

    private void addBullet(JPanel body, String text) {
        JLabel lbl = makeWrappedLabel("  •  " + text, GuiTheme.font("Segoe UI", Font.PLAIN, 13), GuiTheme.TEXT);
        lbl.setBorder(new EmptyBorder(2, 10, 4, 0));
        body.add(lbl);
    }

    private void addStepRow(JPanel body, String step, String title, String desc) {
        body.add(Box.createVerticalStrut(6));
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

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

            if (active) {
                g2.setColor(COLOR_ACTIVE);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            } else if (hover) {
                g2.setColor(COLOR_HOVER_BG);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            }

            ImageIcon ico = active ? activeIcon : (hover ? hoverIcon : originalIcon);
            int iconX = 15;
            int iconY = (h - 16) / 2;
            if (ico != null) {
                g2.drawImage(ico.getImage(), iconX, iconY, 16, 16, this);
            }

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
            this.searchIcon = GuiIcons.loadIcon(HoTroManagerGUI.class, "/images/traCuu.png", 16, 16);
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
