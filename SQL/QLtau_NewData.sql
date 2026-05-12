-- XÓA DỮ LIỆU CŨ ĐỂ LÀM MỚI
DELETE FROM Ve;
DELETE FROM HoaDon;
DELETE FROM KhachHang;

-- 7. KHACHHANG (Tên thực tế)
INSERT [dbo].[KhachHang] ([maKH], [hoTenKH], [cccd], [sdt], [email], [namSinh], [laSinhVien]) VALUES 
('KH001', N'Nguyễn Văn Nam', '052095000121', '0912345678', 'nam.nv@gmail.com', '1990-01-01', 0),
('KH002', N'Trần Thị Mai', '052095000122', '0923456789', 'mai.tt@gmail.com', '1995-05-15', 1),
('KH003', N'Lê Hoàng Anh', '052095000123', '0934567890', 'anh.lh@gmail.com', '1988-10-20', 0),
('KH004', N'Phạm Minh Đức', '052095000124', '0945678901', 'duc.pm@gmail.com', '2000-12-12', 1),
('KH005', N'Vũ Kim Chi', '052095000125', '0956789012', 'chi.vk@gmail.com', '1992-03-08', 0),
('KH006', N'Đặng Quốc Bảo', '052095000126', '0967890123', 'bao.dq@gmail.com', '1985-07-25', 0),
('KH007', N'Hoàng Thanh Trúc', '052095000127', '0978901234', 'truc.ht@gmail.com', '1997-09-30', 0),
('KH008', N'Bùi Tiến Dũng', '052095000128', '0989012345', 'dung.bt@gmail.com', '1993-11-11', 0),
('KH009', N'Đỗ Thùy Linh', '052095000129', '0990123456', 'linh.dt@gmail.com', '1998-02-14', 1),
('KH010', N'Ngô Gia Huy', '052095000130', '0901234567', 'huy.ng@gmail.com', '1991-04-22', 0);

-- Thêm 20 khách hàng nữa bằng vòng lặp với tên ghép
DECLARE @n INT = 11;
WHILE @n <= 50
BEGIN
    DECLARE @ho NVARCHAR(50) = CASE WHEN @n % 5 = 0 THEN N'Nguyễn' WHEN @n % 5 = 1 THEN N'Trần' WHEN @n % 5 = 2 THEN N'Lê' WHEN @n % 5 = 3 THEN N'Phạm' ELSE N'Hoàng' END;
    DECLARE @ten NVARCHAR(50) = CASE WHEN @n % 4 = 0 THEN N'Tuấn' WHEN @n % 4 = 1 THEN N'Hương' WHEN @n % 4 = 2 THEN N'Thắng' ELSE N'Lan' END;
    INSERT [dbo].[KhachHang] ([maKH], [hoTenKH], [cccd], [sdt], [email], [namSinh], [laSinhVien])
    VALUES ('KH' + RIGHT('000' + CAST(@n AS VARCHAR), 3), @ho + ' ' + @ten + ' ' + CAST(@n AS VARCHAR), 
            '0520' + CAST(1980 + (@n % 40) AS VARCHAR) + RIGHT('000000' + CAST(@n AS VARCHAR), 6), 
            '03' + RIGHT('00000000' + CAST(@n * 789 AS VARCHAR), 8), 
            'user' + CAST(@n AS VARCHAR) + '@gmail.com', CAST(1980 + (@n % 40) AS VARCHAR) + '-01-01', @n % 3);
    SET @n = @n + 1;
END

-- 11 & 12. HOADON & VE (Đa dạng hóa cực mạnh)
DECLARE @v INT = 1;
WHILE @v <= 80 -- Tạo 80 vé để dữ liệu dày hơn
BEGIN
    DECLARE @maHD VARCHAR(20) = 'HD' + RIGHT('000' + CAST(((@v-1)/2 + 1) AS VARCHAR), 3); -- 2 vé chung 1 hóa đơn
    DECLARE @maVE VARCHAR(20) = 'VE' + RIGHT('000' + CAST(@v AS VARCHAR), 3);
    DECLARE @ctID VARCHAR(20) = 'CT' + RIGHT('000' + CAST((ABS(CHECKSUM(NEWID())) % 140 + 1) AS VARCHAR), 3);
    DECLARE @tauV VARCHAR(20) = 'SEVN' + RIGHT('0' + CAST((ABS(CHECKSUM(NEWID())) % 30 + 1) AS VARCHAR), 2);
    
    -- Đa dạng Toa/Ghế: 1-2 Cứng, 3 Nằm, 4-5 Mềm
    DECLARE @toaNum INT = (ABS(CHECKSUM(NEWID())) % 5) + 1;
    DECLARE @gheNum INT = (ABS(CHECKSUM(NEWID())) % 20) + 1;
    DECLARE @gID VARCHAR(20) = 'G' + RIGHT('0' + CAST(@gheNum AS VARCHAR), 2) + 'T0' + CAST(@toaNum AS VARCHAR) + @tauV;
    
    -- Đa dạng trạng thái & loại vé
    DECLARE @stt NVARCHAR(50) = CASE WHEN @v % 15 = 0 THEN N'Đã hủy' ELSE N'Đã thanh toán' END;
    DECLARE @lVe NVARCHAR(50) = CASE WHEN @v % 3 = 0 THEN N'Khứ hồi' ELSE N'Một chiều' END;
    DECLARE @gVe DOUBLE PRECISION = 400000 + (ABS(CHECKSUM(NEWID())) % 20 * 25000);

    -- Chèn hóa đơn nếu chưa có (mỗi hóa đơn 2 vé)
    IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHoaDon = @maHD)
    BEGIN
        INSERT [dbo].[HoaDon] ([maHoaDon], [maNV], [maKH], [tongTien], [tienNhan], [phuongThucThanhToan])
        VALUES (@maHD, 'NV001', 'KH' + RIGHT('000' + CAST((ABS(CHECKSUM(NEWID())) % 50 + 1) AS VARCHAR), 3), @gVe * 2, @gVe * 2, 
                CASE WHEN @v % 4 = 0 THEN N'Tiền mặt' ELSE N'Chuyển khoản' END);
    END

    INSERT [dbo].[Ve] ([maVe], [loaiVe], [trangThaiVe], [giaVe], [maGhe], [maHoaDon], [maChuyenTau])
    VALUES (@maVE, @lVe, @stt, @gVe, @gID, @maHD, @ctID);
    
    SET @v = @v + 1;
END
GO