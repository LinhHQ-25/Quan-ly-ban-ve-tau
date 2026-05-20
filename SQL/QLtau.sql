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

-- 13. DONDOITRAVE
CREATE TABLE [dbo].[DonDoiTraVe](
                                    [maDon] [varchar](20) NOT NULL PRIMARY KEY,
                                    [tienBu] [float] NULL,
                                    [ngayLap] [datetime] DEFAULT GETDATE(),
                                    [tienHoanTra] [float] NULL,
                                    [loaiDon] [nvarchar](50) NULL,
                                    [maVe] [varchar](20) NOT NULL FOREIGN KEY REFERENCES [Ve]([maVe])
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
                -- Khoảng thời gian từ 4 tuần trước đến 1 tuần trước (Period 1)
                IF @dayOffset < -7
                    BEGIN
                        SET @numOut = 5 + (@gaIdx % 2); -- 5-6 chuyến đi
                        SET @numIn = 3 + (@gaIdx % 2);  -- 3-4 chuyến về
                    END
                    -- Khoảng thời gian từ 1 tuần trước đến nay (Period 2)
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

DECLARE @KhachHangIds TABLE (
                                RowNo INT IDENTITY(1,1) PRIMARY KEY,
                                maKH VARCHAR(20) NOT NULL
                            );

DECLARE @KhachHangSeed TABLE (
                                 RowNo INT IDENTITY(1,1) PRIMARY KEY,
                                 hoTenKH NVARCHAR(100),
                                 cccd VARCHAR(20),
                                 sdt VARCHAR(15),
                                 email VARCHAR(100),
                                 namSinh DATE,
                                 laSinhVien BIT
                             );

INSERT INTO @KhachHangSeed (hoTenKH, cccd, sdt, email, namSinh, laSinhVien) VALUES
                                                                                (N'Nguyễn Văn Nam', '052095000121', '0912345678', 'nam.nv@gmail.com', '1990-01-01', 0),
                                                                                (N'Trần Thị Mai', 'P052095122', '0923456789', 'mai.tt@gmail.com', '1995-05-15', 1),
                                                                                (N'Lê Hoàng Anh', '052095000123', '0934567890', 'anh.lh@gmail.com', '1988-10-20', 0),
                                                                                (N'Phạm Minh Đức', 'P052095124', '0945678901', 'duc.pm@gmail.com', '2000-12-12', 1),
                                                                                (N'Vũ Kim Chi', '052095000125', '0956789012', 'chi.vk@gmail.com', '1992-03-08', 0),
                                                                                (N'Đặng Văn Bình', 'P052095126', '0967890123', 'binh.dv@gmail.com', '1985-07-20', 0),
                                                                                (N'Lý Thu Thảo', '052095000127', '0978901234', 'thao.lt@gmail.com', '1998-02-28', 1),
                                                                                (N'Hoàng Minh Tuấn', 'P052095128', '0989012345', 'tuan.hm@gmail.com', '1993-11-10', 0),
                                                                                (N'Phan Thanh Hà', '052095000129', '0990123456', 'ha.pt@gmail.com', '1991-04-05', 0),
                                                                                (N'Bùi Quang Hải', 'P052095130', '0901234567', 'hai.bq@gmail.com', '1997-09-15', 1),
                                                                                (N'Trịnh Xuân Bách', '052095000131', '0912233445', 'bach.tx@gmail.com', '1980-01-01', 0),
                                                                                (N'Mai Hồng Nhung', 'P052095132', '0922334455', 'nhung.mh@gmail.com', '1994-06-12', 0),
                                                                                (N'Đỗ Thế Vinh', '052095000133', '0933445566', 'vinh.dt@gmail.com', '1989-12-30', 0),
                                                                                (N'Lương Gia Bảo', 'P052095134', '0944556677', 'bao.lg@gmail.com', '2002-03-22', 1),
                                                                                (N'Tô Ngọc Vân', '052095000135', '0955667788', 'van.tn@gmail.com', '1996-08-18', 0);

DECLARE @khSeedIdx INT = 1;
DECLARE @khSeedMax INT = (SELECT COUNT(*) FROM @KhachHangSeed);
WHILE @khSeedIdx <= @khSeedMax
    BEGIN
        DECLARE @maKHSeed VARCHAR(20);
        WHILE 1 = 1
            BEGIN
                SET @maKHSeed = 'KH' + LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 6);
                IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH = @maKHSeed)
                    AND NOT EXISTS (SELECT 1 FROM @KhachHangIds WHERE maKH = @maKHSeed) BREAK;
            END

        INSERT [dbo].[KhachHang] ([maKH], [hoTenKH], [cccd], [sdt], [email], [namSinh], [laSinhVien])
        SELECT @maKHSeed, hoTenKH, cccd, sdt, email, namSinh, laSinhVien
        FROM @KhachHangSeed
        WHERE RowNo = @khSeedIdx;

        INSERT INTO @KhachHangIds (maKH) VALUES (@maKHSeed);
        SET @khSeedIdx = @khSeedIdx + 1;
    END

INSERT [dbo].[NhanVien] ([maNV], [hoTenNV], [email], [ngaySinh], [soDT], [gioiTinh], [diaChi], [soCCCD], [loaiNV]) VALUES
                                                                                                                       ('NV001', N'Nguyễn Văn An', 'nva@railway.com', '1995-02-15', '0987654321', 1, N'Quy Nhơn', '052095000123', 'NHAN_VIEN_BAN_VE'),
                                                                                                                       ('NV002', N'Trần Thị Mai', 'mai.tt@railway.com', '1998-08-20', '0912345678', 0, N'Hà Nội', '001198000001', 'NHAN_VIEN_QUAN_LY'),
                                                                                                                       ('NV003', N'Lê Hoàng Tuấn', 'tuan.lh@railway.com', '1990-12-05', '0923456789', 1, N'Đà Nẵng', '048190000002', 'NHAN_VIEN_BAN_VE'),
                                                                                                                       ('NV004', N'Phạm Thu Hương', 'huong.pt@railway.com', '2000-04-10', '0934567890', 0, N'TP.HCM', '079200000003', 'NHAN_VIEN_BAN_VE'),
                                                                                                                       ('NV005', N'Hoàng Đình Bảo', 'bao.hd@railway.com', '1985-11-25', '0945678901', 1, N'Hải Phòng', '031185000004', 'NHAN_VIEN_QUAN_LY');

INSERT [dbo].[KhuyenMai] ([maKhuyenMai], [tenKhuyenMai], [trangThai], [moTaChiTiet], [tiLeGiamGia], [loaiKhachHang], [thoiGianBatDau], [thoiGianKetThuc]) VALUES
                                                                                                                                                              ('KM001', N'Giảm giá sinh viên', 1, N'Giảm 20% cho sinh viên', 0.2, 'SINH_VIEN', '2024-01-01', '2025-12-31'),
                                                                                                                                                              ('KM002', N'Giảm giá người cao tuổi', 1, N'Giảm 15% cho người trên 60 tuổi', 0.15, 'TU_60_TRO_LEN', '2024-01-01', '2025-12-31');

DECLARE @v INT = 1;
WHILE @v <= 500 -- Tạo 500 vé cho dữ liệu phong phú
    BEGIN
        DECLARE @ngayLapHD DATETIME = DATEADD(MINUTE, -((@v * 17) % 1440), DATEADD(DAY, -(@v % 28), GETDATE()));
        DECLARE @maHD VARCHAR(20);
        WHILE 1 = 1
            BEGIN
                SET @maHD = 'HD' + FORMAT(@ngayLapHD, 'ddMMyy') + '-' + LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 4);
                IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHoaDon = @maHD) BREAK;
            END

        -- Xác định loại vé trước
        DECLARE @lv NVARCHAR(50) = CASE @v % 2 WHEN 0 THEN 'MOT_CHIEU' ELSE 'KHU_HOI' END;

        -- Vé 1 chiều: chỉ lấy chuyến đi từ DIEUTRI (maGaDi = 'DIEUTRI')
        -- Vé khứ hồi: lấy ngẫu nhiên bất kỳ chuyến
        DECLARE @maCT VARCHAR(20);
        IF @lv = 'MOT_CHIEU'
            SET @maCT = (
                SELECT TOP 1 ct.maChuyenTau
                FROM ChuyenTau ct
                         JOIN ChiTietChuyenTau dt ON ct.maChuyenTau = dt.maChuyenTau
                WHERE dt.maGaDi = 'DIEUTRI'
                ORDER BY NEWID()
            );
        ELSE
            SET @maCT = (SELECT TOP 1 maChuyenTau FROM ChuyenTau ORDER BY NEWID());

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
        DECLARE @maKHVe VARCHAR(20) = (SELECT maKH FROM @KhachHangIds WHERE RowNo = @khIdx);
        DECLARE @statusVe NVARCHAR(50) = CASE @v % 3 WHEN 0 THEN N'Đã thanh toán' WHEN 1 THEN N'Đã hủy' ELSE N'Chờ thanh toán' END;
        DECLARE @maVe VARCHAR(20);
        WHILE 1 = 1
            BEGIN
                SET @maVe = LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 9);
                IF NOT EXISTS (SELECT 1 FROM Ve WHERE maVe = @maVe) BREAK;
            END

        INSERT [dbo].[HoaDon] ([maHoaDon], [ngayLapHD], [maNV], [maKH], [tongTien], [tienNhan], [phuongThucThanhToan])
        VALUES (@maHD, @ngayLapHD, 'NV001', @maKHVe, 500000, 500000, 'TIEN_MAT');

        INSERT [dbo].[Ve] ([maVe], [ngayMua], [loaiVe], [trangThaiVe], [giaVe], [maGhe], [maHoaDon], [maChuyenTau], [maKH], [maKhuyenMai])
        VALUES (@maVe, @ngayLapHD, @lv, @statusVe, 500000, @mG, @maHD, @maCT, @maKHVe, NULL);

        IF @v % 10 = 0
            BEGIN
                DECLARE @ngayLapDon DATETIME = DATEADD(HOUR, 2, @ngayLapHD);
                DECLARE @maDon VARCHAR(20);
                WHILE 1 = 1
                    BEGIN
                        SET @maDon = 'DT-' + FORMAT(@ngayLapDon, 'MMyy') + '-' + LEFT(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 4);
                        IF NOT EXISTS (SELECT 1 FROM DonDoiTraVe WHERE maDon = @maDon) BREAK;
                    END

                INSERT [dbo].[DonDoiTraVe] ([maDon], [tienBu], [ngayLap], [tienHoanTra], [loaiDon], [maVe])
                VALUES (@maDon, CASE WHEN @v % 20 = 0 THEN 30000 ELSE 0 END, @ngayLapDon, CASE WHEN @v % 20 = 0 THEN 0 ELSE 350000 END, CASE WHEN @v % 20 = 0 THEN 'DON_DOI' ELSE 'DON_TRA' END, @maVe);
            END

        SET @v = @v + 1;
    END
GO