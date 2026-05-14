USE [master]
GO
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'QuanLyBanVeTau')
BEGIN
    ALTER DATABASE [QuanLyBanVeTau] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [QuanLyBanVeTau];
END
GO

CREATE DATABASE [QuanLyBanVeTau]
GO
USE [QuanLyBanVeTau]
GO

-- 1. GA
CREATE TABLE [dbo].[Ga](
    [maGa] [varchar](20) NOT NULL PRIMARY KEY,
    [tenGa] [nvarchar](100) NOT NULL,
    [diaChi] [nvarchar](255) NULL,
    [tinhThanh] [nvarchar](100) NULL
);

-- 2. TAU
CREATE TABLE [dbo].[Tau](
    [maTau] [varchar](20) NOT NULL PRIMARY KEY,
    [tenTau] [nvarchar](100) NOT NULL,
    [soToa] [int] DEFAULT 5,
    [tongSoGhe] [int] DEFAULT 100,
    [trangThai] [nvarchar](50) DEFAULT N'Đang hoạt động',
    [ghiChu] [nvarchar](255) NULL
);

-- 3. TOATAU
CREATE TABLE [dbo].[ToaTau](
    [maToaTau] [varchar](20) NOT NULL PRIMARY KEY,
    [soToa] [int] NOT NULL,
    [soLuongGhe] [int] NOT NULL,
    [loaiToa] [nvarchar](50) NULL, -- Enum: TOA_THUONG, TOA_VIP
    [heSoLoaiToa] [float] DEFAULT 1.0,
    [maTau] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [Tau]([maTau])
);

-- 4. GHE
CREATE TABLE [dbo].[Ghe](
    [maGhe] [varchar](20) NOT NULL PRIMARY KEY,
    [soGhe] [varchar](20) NOT NULL,
    [loaiGhe] [nvarchar](50) NULL, -- Enum: GHE_MEM, GHE_CUNG, GIUONG_NAM
    [maToaTau] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [ToaTau]([maToaTau])
);

-- 5. CHUYENTAU
CREATE TABLE [dbo].[ChuyenTau](
    [maChuyenTau] [varchar](20) NOT NULL PRIMARY KEY,
    [ghiChu] [nvarchar](255) NULL,
    [maTau] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [Tau]([maTau]),
    [trangThai] [nvarchar](50) NULL -- Enum: CHUAN_BI, DANG_CHAY, DA_DEN, HUY
);

-- 6. CHITIETCHUYENTAU
CREATE TABLE [dbo].[ChiTietChuyenTau](
    [maChuyenTau] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [ChuyenTau]([maChuyenTau]),
    [thoiGianKhoiHanh] [datetime] NOT NULL,
    [thoiGianDuKien] [datetime] NOT NULL,
    [maGaDi] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [Ga]([maGa]),
    [maGaDen] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [Ga]([maGa])
);

-- 7. KHACHHANG
CREATE TABLE [dbo].[KhachHang](
    [maKH] [varchar](20) NOT NULL PRIMARY KEY,
    [hoTenKH] [nvarchar](100) NOT NULL,
    [cccd] [varchar](20) NULL,
    [sdt] [varchar](15) NULL,
    [email] [varchar](100) NULL,
    [namSinh] [date] NULL,
    [laSinhVien] [bit] DEFAULT 0
);

-- 8. NHANVIEN
CREATE TABLE [dbo].[NhanVien](
    [maNV] [varchar](20) NOT NULL PRIMARY KEY,
    [hoTenNV] [nvarchar](100) NOT NULL,
    [email] [varchar](100) NULL,
    [ngaySinh] [date] NULL,
    [soDT] [varchar](15) NULL,
    [gioiTinh] [bit] NULL,
    [diaChi] [nvarchar](255) NULL,
    [soCCCD] [varchar](20) NULL,
    [loaiNV] [nvarchar](50) NULL -- Enum: NHAN_VIEN_QUAN_LY, NHAN_VIEN_BAN_VE
);

-- 9. KHUYENMAI
CREATE TABLE [dbo].[KhuyenMai](
    [maKhuyenMai] [varchar](20) NOT NULL PRIMARY KEY,
    [tenKhuyenMai] [nvarchar](100) NOT NULL,
    [trangThai] [bit] DEFAULT 1,
    [moTaChiTiet] [nvarchar](255) NULL,
    [tiLeGiamGia] [float] NOT NULL,
    [loaiKhachHang] [nvarchar](50) NULL, -- Enum: DUOI_6_TUOI, TU_6_TOI_DUOI_10, ...
    [thoiGianBatDau] [datetime] NOT NULL,
    [thoiGianKetThuc] [datetime] NOT NULL
);

-- 11. HOADON
CREATE TABLE [dbo].[HoaDon](
    [maHoaDon] [varchar](20) NOT NULL PRIMARY KEY,
    [ngayLapHD] [datetime] DEFAULT GETDATE(),
    [maNV] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [NhanVien]([maNV]),
    [maKH] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [KhachHang]([maKH]),
    [tongTien] [float] NULL,
    [tienNhan] [float] NULL,
    [phuongThucThanhToan] [nvarchar](50) NULL -- Enum: TIEN_MAT, CHUYEN_KHOAN, THE
);

-- 12. VE
CREATE TABLE [dbo].[Ve](
    [maVe] [varchar](20) NOT NULL PRIMARY KEY,
    [ngayMua] [datetime] DEFAULT GETDATE(),
    [loaiVe] [nvarchar](50) NULL, -- Enum: MOT_CHIEU, KHU_HOI
    [trangThaiVe] [nvarchar](50) NULL,
    [giaVe] [float] NULL,
    [maGhe] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [Ghe]([maGhe]),
    [maHoaDon] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [HoaDon]([maHoaDon]),
    [maChuyenTau] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [ChuyenTau]([maChuyenTau]),
    [maKH] [varchar](20) NULL FOREIGN KEY REFERENCES [KhachHang]([maKH]),
    [maKhuyenMai] [varchar](20) NULL FOREIGN KEY REFERENCES [KhuyenMai]([maKhuyenMai])
);

GO
-- DATA
-- DATA
INSERT [dbo].[Ga] ([maGa], [tenGa], [diaChi], [tinhThanh]) VALUES 
('NGOCHOI', N'Ngọc Hồi', N'Hà Nội', N'Hà Nội'),
('PHULY', N'Phủ Lý', N'Hà Nam', N'Hà Nam'),
('NAMDINH', N'Nam Định', N'Nam Định', N'Nam Định'),
('NINHBINH', N'Ninh Bình', N'Ninh Bình', N'Ninh Bình'),
('THANHHOA', N'Thanh Hóa', N'Thanh Hóa', N'Thanh Hóa'),
('VINH', N'Vinh', N'Nghệ An', N'Nghệ An'),
('HATINH', N'Hà Tĩnh', N'Hà Tĩnh', N'Hà Tĩnh'),
('VUNGANG', N'Vũng Áng', N'Hà Tĩnh', N'Hà Tĩnh'),
('DONGHOI', N'Đồng Hới', N'Quảng Bình', N'Quảng Bình'),
('DONGHA', N'Đông Hà', N'Quảng Trị', N'Quảng Trị'),
('HUE', N'Huế', N'Thừa Thiên Huế', N'Thừa Thiên Huế'),
('DANANG', N'Đà Nẵng', N'Đà Nẵng', N'Đà Nẵng'),
('TAMKY', N'Tam Kỳ', N'Quảng Nam', N'Quảng Nam'),
('QUANGNGAI', N'Quảng Ngãi', N'Quảng Ngãi', N'Quảng Ngãi'),
('BONGSON', N'Bồng Sơn', N'Bình Định', N'Bình Định'),
('DIEUTRI', N'Diêu Trì', N'Bình Định', N'Bình Định'),
('TUYHOA', N'Tuy Hòa', N'Phú Yên', N'Phú Yên'),
('KHANHHOA', N'Khánh Hòa', N'Khánh Hòa', N'Khánh Hòa'),
('THAPCHAM', N'Tháp Chàm', N'Ninh Thuận', N'Ninh Thuận'),
('PHANRI', N'Phan Rí', N'Bình Thuận', N'Bình Thuận'),
('LONGTHANH', N'Long Thành', N'Đồng Nai', N'Đồng Nai'),
('THUTHIEM', N'Thủ Thiêm', N'TP. HCM', N'TP. HCM');

-- 132 ga, 100 tau, tao du lieu cho 100 tau
DECLARE @t INT = 1;
WHILE @t <= 100
BEGIN
    DECLARE @trangThaiT NVARCHAR(50) = CASE WHEN @t % 10 = 0 THEN N'Bảo trì' ELSE N'Đang hoạt động' END;
    DECLARE @ghiChuT NVARCHAR(255) = CASE WHEN @trangThaiT = N'Bảo trì' THEN N'Kiểm tra định kỳ hệ thống phanh' ELSE N'Sẵn sàng phục vụ' END;
    INSERT [dbo].[Tau] ([maTau], [tenTau], [soToa], [tongSoGhe], [trangThai], [ghiChu]) 
    VALUES ('SEVN' + RIGHT('00' + CAST(@t AS VARCHAR), 3), N'Tàu SE ' + CAST(@t AS VARCHAR), 12, 240, @trangThaiT, @ghiChuT);
    
    DECLARE @toa INT = 1;
    WHILE @toa <= 12
    BEGIN
        DECLARE @maT VARCHAR(20) = 'T' + RIGHT('0' + CAST(@toa AS VARCHAR), 2) + 'SEVN' + RIGHT('00' + CAST(@t AS VARCHAR), 3);
        DECLARE @slG INT = 20;
        INSERT [dbo].[ToaTau] ([maToaTau], [soToa], [soLuongGhe], [loaiToa], [heSoLoaiToa], [maTau]) 
        VALUES (@maT, @toa, @slG, CASE WHEN @toa <= 4 THEN 'TOA_THUONG' ELSE 'TOA_VIP' END, CASE WHEN @toa <= 4 THEN 1.0 ELSE 1.5 END, 'SEVN' + RIGHT('00' + CAST(@t AS VARCHAR), 3));
        DECLARE @g INT = 1;
        WHILE @g <= @slG
        BEGIN
            INSERT [dbo].[Ghe] ([maGhe], [soGhe], [loaiGhe], [maToaTau])
            VALUES ('G' + RIGHT('0' + CAST(@g AS VARCHAR), 2) + @maT, CAST(@g AS VARCHAR), CASE WHEN @toa <= 4 THEN N'Ghế cứng' WHEN @toa <= 8 THEN N'Giường nằm' ELSE N'Ghế mềm' END, @maT);
            SET @g = @g + 1;
        END
        SET @toa = @toa + 1;
    END
    SET @t = @t + 1;
END

DECLARE @gaDen VARCHAR(20), @tripIdx INT = 1;
DECLARE @dayOffset INT = -28; -- Bắt đầu từ 4 tuần trước

WHILE @dayOffset <= 7 -- Đến 1 tuần sau để có dữ liệu tương lai
BEGIN
    DECLARE @gaIdx INT = 0;
    DECLARE curGa CURSOR FOR SELECT maGa FROM Ga WHERE maGa <> 'DIEUTRI';
    OPEN curGa; FETCH NEXT FROM curGa INTO @gaDen;
    
    WHILE @@FETCH_STATUS = 0
    BEGIN
        DECLARE @numOut INT, @numIn INT;
        -- Đặc biệt cho riêng ga Thủ Thiêm vào ngày 15 và 16 để test
        IF @dayOffset IN (1, 2) AND @gaDen = 'THUTHIEM'
        BEGIN
            SET @numOut = 20;
            SET @numIn = 15;
        END
        -- Khoảng thời gian từ 4 tuần trước đến 1 tuần trước (Period 1)
        ELSE IF @dayOffset < -7
        BEGIN
            SET @numOut = 5 + (@gaIdx % 2); -- 5-6 chuyến đi
            SET @numIn = 3 + (@gaIdx % 2);  -- 3-4 chuyến về
        END
        -- Các ngày bình thường khác
        ELSE
        BEGIN
            SET @numOut = 6 + (@gaIdx % 2); -- 6-7 chuyến đi
            SET @numIn = 3 + (@gaIdx % 2);  -- 3-4 chuyến khứ hồi (về)
        END

        DECLARE @slot INT = 1;
        WHILE @slot <= @numOut
        BEGIN
            -- Phân bổ tàu (tránh tàu bảo trì)
            DECLARE @tIdx INT = (ABS(@dayOffset * 100) + (@gaIdx * 10) + @slot) % 100 + 1;
            IF @tIdx % 10 = 0 SET @tIdx = CASE WHEN @tIdx < 100 THEN @tIdx + 1 ELSE @tIdx - 1 END;
            DECLARE @trainID VARCHAR(20) = 'SEVN' + RIGHT('00' + CAST(@tIdx AS VARCHAR), 3);

            DECLARE @gaPos INT = (SELECT COUNT(*) FROM Ga g2 WHERE g2.maGa < @gaDen); 
            DECLARE @duration INT = ABS(@gaPos - 15) * 1 + 2;

            -- Trạng thái chuyến tàu dựa trên thời gian
            DECLARE @status NVARCHAR(50) = CASE 
                WHEN @dayOffset < 0 THEN N'DA_DEN' 
                WHEN @dayOffset = 0 THEN N'DANG_CHAY' 
                ELSE N'CHUAN_BI' END;

            -- CHUYẾN ĐI: Diêu Trì -> Ga Đến
            DECLARE @maCT_Di VARCHAR(20) = 'CT' + RIGHT('00000' + CAST(@tripIdx AS VARCHAR), 5);
            INSERT [dbo].[ChuyenTau] ([maChuyenTau], [ghiChu], [maTau], [trangThai]) VALUES (@maCT_Di, N'Đi ' + @gaDen, @trainID, @status);
            
            DECLARE @dep_Time_Di FLOAT = (24.0 / @numOut) * (@slot - 1) + (@gaIdx % 2);
            DECLARE @dep_Di DATETIME = DATEADD(MINUTE, CAST(@dep_Time_Di * 60 AS INT), CAST(CAST(DATEADD(DAY, @dayOffset, GETDATE()) AS DATE) AS DATETIME));
            DECLARE @arr_Di DATETIME = DATEADD(HOUR, @duration, @dep_Di);
            
            INSERT [dbo].[ChiTietChuyenTau] ([maChuyenTau], [thoiGianKhoiHanh], [thoiGianDuKien], [maGaDi], [maGaDen]) 
            VALUES (@maCT_Di, @dep_Di, @arr_Di, 'DIEUTRI', @gaDen);
            SET @tripIdx = @tripIdx + 1;

            -- CHUYẾN VỀ: Ga Đến -> Diêu Trì (nếu slot nằm trong số lượng chuyến về)
            IF @slot <= @numIn
            BEGIN
                DECLARE @maCT_Ve VARCHAR(20) = 'CT' + RIGHT('00000' + CAST(@tripIdx AS VARCHAR), 5);
                INSERT [dbo].[ChuyenTau] ([maChuyenTau], [ghiChu], [maTau], [trangThai]) VALUES (@maCT_Ve, N'Về Diêu Trì từ ' + @gaDen, @trainID, @status);
                
                DECLARE @dep_Ve DATETIME = DATEADD(HOUR, 5, @arr_Di); -- Nghỉ ít nhất 5 tiếng
                DECLARE @arr_Ve DATETIME = DATEADD(HOUR, @duration, @dep_Ve);
                
                INSERT [dbo].[ChiTietChuyenTau] ([maChuyenTau], [thoiGianKhoiHanh], [thoiGianDuKien], [maGaDi], [maGaDen]) 
                VALUES (@maCT_Ve, @dep_Ve, @arr_Ve, @gaDen, 'DIEUTRI');
                SET @tripIdx = @tripIdx + 1;
            END

            SET @slot = @slot + 1;
        END
        SET @gaIdx = @gaIdx + 1;
        FETCH NEXT FROM curGa INTO @gaDen;
    END
    CLOSE curGa; DEALLOCATE curGa;
    SET @dayOffset = @dayOffset + 1;
END

INSERT [dbo].[KhachHang] ([maKH], [hoTenKH], [cccd], [sdt], [email], [namSinh], [laSinhVien]) VALUES 
('KH001', N'Nguyễn Văn Nam', '052095000121', '0912345678', 'nam.nv@gmail.com', '1990-01-01', 0),
('KH002', N'Trần Thị Mai', '052095000122', '0923456789', 'mai.tt@gmail.com', '1995-05-15', 1),
('KH003', N'Lê Hoàng Anh', '052095000123', '0934567890', 'anh.lh@gmail.com', '1988-10-20', 0),
('KH004', N'Phạm Minh Đức', '052095000124', '0945678901', 'duc.pm@gmail.com', '2000-12-12', 1),
('KH005', N'Vũ Kim Chi', '052095000125', '0956789012', 'chi.vk@gmail.com', '1992-03-08', 0),
('KH006', N'Đặng Văn Bình', '052095000126', '0967890123', 'binh.dv@gmail.com', '1985-07-20', 0),
('KH007', N'Lý Thu Thảo', '052095000127', '0978901234', 'thao.lt@gmail.com', '1998-02-28', 1),
('KH008', N'Hoàng Minh Tuấn', '052095000128', '0989012345', 'tuan.hm@gmail.com', '1993-11-10', 0),
('KH009', N'Phan Thanh Hà', '052095000129', '0990123456', 'ha.pt@gmail.com', '1991-04-05', 0),
('KH010', N'Bùi Quang Hải', '052095000130', '0901234567', 'hai.bq@gmail.com', '1997-09-15', 1),
('KH011', N'Trịnh Xuân Bách', '052095000131', '0912233445', 'bach.tx@gmail.com', '1980-01-01', 0),
('KH012', N'Mai Hồng Nhung', '052095000132', '0922334455', 'nhung.mh@gmail.com', '1994-06-12', 0),
('KH013', N'Đỗ Thế Vinh', '052095000133', '0933445566', 'vinh.dt@gmail.com', '1989-12-30', 0),
('KH014', N'Lương Gia Bảo', '052095000134', '0944556677', 'bao.lg@gmail.com', '2002-03-22', 1),
('KH015', N'Tô Ngọc Vân', '052095000135', '0955667788', 'van.tn@gmail.com', '1996-08-18', 0);

INSERT [dbo].[NhanVien] ([maNV], [hoTenNV], [email], [ngaySinh], [soDT], [gioiTinh], [diaChi], [soCCCD], [loaiNV]) VALUES ('NV001', N'Nguyễn Văn A', 'nva@railway.com', '1995-02-15', '0987654321', 1, N'Quy Nhơn', '052095000123', 'NHAN_VIEN_BAN_VE');

INSERT [dbo].[KhuyenMai] ([maKhuyenMai], [tenKhuyenMai], [trangThai], [moTaChiTiet], [tiLeGiamGia], [loaiKhachHang], [thoiGianBatDau], [thoiGianKetThuc]) VALUES 
('KM001', N'Giảm giá sinh viên', 1, N'Giảm 20% cho sinh viên', 0.2, 'SINH_VIEN', '2024-01-01', '2025-12-31'),
('KM002', N'Giảm giá người cao tuổi', 1, N'Giảm 15% cho người trên 60 tuổi', 0.15, 'TU_60_TRO_LEN', '2024-01-01', '2025-12-31');

DECLARE @v INT = 1;
WHILE @v <= 500 -- Tạo 500 vé cho dữ liệu phong phú
BEGIN
    DECLARE @maHD VARCHAR(20) = 'HD' + RIGHT('0000' + CAST(@v AS VARCHAR), 4);
    
    -- Lấy ngẫu nhiên một chuyến tàu đã tồn tại
    DECLARE @maCT VARCHAR(20) = (SELECT TOP 1 maChuyenTau FROM ChuyenTau ORDER BY NEWID());
    DECLARE @tauID VARCHAR(20) = (SELECT maTau FROM ChuyenTau WHERE maChuyenTau = @maCT);
    
    -- Lấy ngẫu nhiên một ghế thuộc về tàu đó
    DECLARE @mG VARCHAR(20) = (
        SELECT TOP 1 g.maGhe 
        FROM Ghe g 
        JOIN ToaTau t ON g.maToaTau = t.maToaTau 
        WHERE t.maTau = @tauID 
        ORDER BY NEWID()
    );
    
    DECLARE @khIdx INT = (@v % 15) + 1;
    DECLARE @statusVe NVARCHAR(50) = CASE @v % 3 WHEN 0 THEN N'Đã thanh toán' WHEN 1 THEN N'Đã hủy' ELSE N'Chưa thanh toán' END;
    DECLARE @lv NVARCHAR(50) = CASE @v % 2 WHEN 0 THEN N'Một chiều' ELSE N'Khứ hồi' END;

    INSERT [dbo].[HoaDon] ([maHoaDon], [maNV], [maKH], [tongTien], [tienNhan], [phuongThucThanhToan]) VALUES (@maHD, 'NV001', 'KH' + RIGHT('000' + CAST(@khIdx AS VARCHAR), 3), 500000, 500000, 'TIEN_MAT');
    INSERT [dbo].[Ve] ([maVe], [loaiVe], [trangThaiVe], [giaVe], [maGhe], [maHoaDon], [maChuyenTau], [maKH], [maKhuyenMai]) 
    VALUES ('VE' + RIGHT('0000' + CAST(@v AS VARCHAR), 4), @lv, @statusVe, 500000, @mG, @maHD, @maCT, 'KH' + RIGHT('000' + CAST(@khIdx AS VARCHAR), 3), NULL);
    SET @v = @v + 1;
END
GO
