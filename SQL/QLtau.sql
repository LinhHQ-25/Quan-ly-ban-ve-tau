CREATE DATABASE QuanLyBanVeTau;
GO

-- SỬ DỤNG DATABASE
USE QuanLyBanVeTau;
GO
-- DROP TABLE

IF OBJECT_ID('DonDoiTraVe') IS NOT NULL DROP TABLE DonDoiTraVe;
IF OBJECT_ID('Ve') IS NOT NULL DROP TABLE Ve;
IF OBJECT_ID('HoaDon') IS NOT NULL DROP TABLE HoaDon;
IF OBJECT_ID('Ghe') IS NOT NULL DROP TABLE Ghe;
IF OBJECT_ID('ChuyenTau') IS NOT NULL DROP TABLE ChuyenTau;
IF OBJECT_ID('ToaTau') IS NOT NULL DROP TABLE ToaTau;
IF OBJECT_ID('TaiKhoan') IS NOT NULL DROP TABLE TaiKhoan;
IF OBJECT_ID('KhuyenMai') IS NOT NULL DROP TABLE KhuyenMai;
IF OBJECT_ID('KhachHang') IS NOT NULL DROP TABLE KhachHang;
IF OBJECT_ID('NhanVien') IS NOT NULL DROP TABLE NhanVien;
IF OBJECT_ID('Tau') IS NOT NULL DROP TABLE Tau;
IF OBJECT_ID('Ga') IS NOT NULL DROP TABLE Ga;



CREATE TABLE NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTenNV NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
    ngaySinh DATE,
    soDT VARCHAR(15),
    gioiTinh BIT,
    diaChi NVARCHAR(255),
    soCCCD VARCHAR(20) UNIQUE,
    loaiNV VARCHAR(50) NOT NULL CHECK (
        loaiNV IN ('NHAN_VIEN_QUAN_LY', 'NHAN_VIEN_BAN_VE')
    )
);

CREATE TABLE KhachHang (
    maKH VARCHAR(20) PRIMARY KEY,
    hoTenKH NVARCHAR(100) NOT NULL,
    cccd VARCHAR(20) UNIQUE,
    sdt VARCHAR(15),
    email VARCHAR(100),
    namSinh DATE,
    laSinhVien BIT DEFAULT 0
);

CREATE TABLE KhuyenMai (
    maKhuyenMai VARCHAR(20) PRIMARY KEY,
    tenKhuyenMai NVARCHAR(255),
    trangThai BIT,
    moTaChiTiet NVARCHAR(MAX),
    loaiHanhKhach VARCHAR(50)
);

CREATE TABLE Tau (
    maTau VARCHAR(20) PRIMARY KEY,
    tenTau NVARCHAR(100) NOT NULL,
    trangThai VARCHAR(50) DEFAULT 'HOAT_DONG' CHECK (trangThai IN ('HOAT_DONG', 'BAO_TRI')),
    ghiChu NVARCHAR(MAX)
);

CREATE TABLE Ga (
    maGa VARCHAR(20) PRIMARY KEY,
    tenGa NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255)
);



CREATE TABLE TaiKhoan (
    maTaiKhoan VARCHAR(20) PRIMARY KEY,
    tenTaiKhoan VARCHAR(50) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL,
    ngayDangNhap DATETIME,
    ngayDangXuat DATETIME,
    trangThai BIT DEFAULT 1,
    maNV VARCHAR(20) UNIQUE NOT NULL,
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE ToaTau (
    maToaTau VARCHAR(20) PRIMARY KEY,
    soToa INT,
    soLuongGhe INT CHECK (soLuongGhe > 0),
    loaiToa VARCHAR(50) CHECK (loaiToa IN ('TOA_THUONG', 'TOA_VIP')),
    heSoLoaiToa DECIMAL(5,2) CHECK (heSoLoaiToa > 0),
    maTau VARCHAR(20) NOT NULL,
    FOREIGN KEY (maTau) REFERENCES Tau(maTau)
);

CREATE TABLE ChuyenTau (
    maChuyen VARCHAR(20) PRIMARY KEY,
    thoiGianKhoiHanh DATETIME,
    thoiGianDuKien DATETIME,
    ghiChu NVARCHAR(MAX),
    gaDi VARCHAR(20) NOT NULL,
    gaDen VARCHAR(20) NOT NULL,
    maTau VARCHAR(20) NOT NULL,
    FOREIGN KEY (gaDi) REFERENCES Ga(maGa),
    FOREIGN KEY (gaDen) REFERENCES Ga(maGa),
    FOREIGN KEY (maTau) REFERENCES Tau(maTau),
    CHECK (gaDi <> gaDen)
);



CREATE TABLE Ghe (
    maGhe VARCHAR(20) PRIMARY KEY,
    loaiGhe VARCHAR(50) CHECK (loaiGhe IN ('GIUONG_NAM', 'GHE_MEM', 'GHE_CUNG')),
    maToaTau VARCHAR(20) NOT NULL,
    FOREIGN KEY (maToaTau) REFERENCES ToaTau(maToaTau)
);

CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    ngayLapHD DATETIME DEFAULT GETDATE(),
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    tongTien DECIMAL(18,2),
    tienNhan DECIMAL(18,2),
    tienThua AS (tienNhan - tongTien),
    phuongThucThanhToan VARCHAR(50) CHECK (
        phuongThucThanhToan IN (
            'TIEN_MAT',
            'CHUYEN_KHOAN',
            'THE_NGAN_HANG',
            'VI_DIEN_TU'
        )
    ),
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

-- BẢNG VÉ


CREATE TABLE Ve (
    maVe VARCHAR(20) PRIMARY KEY,
    ngayMua DATETIME DEFAULT GETDATE(),
    loaiVe VARCHAR(50) CHECK (loaiVe IN ('MOT_CHIEU', 'KHU_HOI')),
    trangThaiVe VARCHAR(50) NOT NULL CHECK (
        trangThaiVe IN ('CHO_THANH_TOAN', 'DA_THANH_TOAN', 'DA_HUY')
    ),
    giaVe DECIMAL(18,2) CHECK (giaVe >= 0),
    maGhe VARCHAR(20) NOT NULL,
    maKhuyenMai VARCHAR(20),
    maChuyen VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,

    FOREIGN KEY (maGhe) REFERENCES Ghe(maGhe),
    FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai),
    FOREIGN KEY (maChuyen) REFERENCES ChuyenTau(maChuyen),
    FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

-- UNIQUE INDEX (CHỐNG TRÙNG GHẾ THÔNG MINH)


CREATE UNIQUE INDEX UX_ChongTrungGhe 
ON Ve (maGhe, maChuyen) 
WHERE trangThaiVe IN ('CHO_THANH_TOAN', 'DA_THANH_TOAN');


-- ĐƠN ĐỔI TRẢ VÉ


CREATE TABLE DonDoiTraVe (
    maDon VARCHAR(20) PRIMARY KEY,
    ngayLap DATETIME DEFAULT GETDATE(),
    loaiDon VARCHAR(50) CHECK (loaiDon IN ('DON_DOI', 'DON_TRA')),
    tienBu DECIMAL(18,2),
    tienHoanTra DECIMAL(18,2),
    maVe VARCHAR(20) UNIQUE,
    FOREIGN KEY (maVe) REFERENCES Ve(maVe)
);
GO

-- ==========================================
-- DỮ LIỆU MẪU (MOCK DATA)
-- ==========================================

-- 1. NhanVien
INSERT INTO NhanVien (maNV, hoTenNV, email, ngaySinh, soDT, gioiTinh, diaChi, soCCCD, loaiNV) VALUES
('MP0001', N'Nguyễn Văn Quản Lý', 'quanly@tau.vn', '1985-05-15', '0901234567', 1, N'Hà Nội', '001085000001', 'NHAN_VIEN_QUAN_LY'),
('MP0002', N'Trần Thị Bán Vé 1', 'banve1@tau.vn', '1995-08-20', '0912345678', 0, N'Hà Nội', '001095000002', 'NHAN_VIEN_BAN_VE'),
('MP0003', N'Lê Văn Bán Vé 2', 'banve2@tau.vn', '1998-02-10', '0923456789', 1, N'TP.HCM', '079098000003', 'NHAN_VIEN_BAN_VE');

-- 2. TaiKhoan
INSERT INTO TaiKhoan (maTaiKhoan, tenTaiKhoan, matKhau, trangThai, maNV) VALUES
('USER0001', 'admin', '123456', 1, 'MP0001'),
('USER0002', 'nvbanve1', '123456', 1, 'MP0002'),
('USER0003', 'nvbanve2', '123456', 1, 'MP0003');

-- 3. KhachHang (10 rows)
INSERT INTO KhachHang (maKH, hoTenKH, cccd, sdt, email, namSinh, laSinhVien) VALUES
('HK0000000001', N'Nguyễn Văn A', '001090000001', '0912345671', 'a@gmail.com', '1990-01-01', 0),
('HK0000000002', N'Trần Thị B', '001090000002', '0912345672', 'b@gmail.com', '1995-02-02', 1),
('HK0000000003', N'Lê Văn C', '001090000003', '0912345673', 'c@gmail.com', '1985-03-03', 0),
('HK0000000004', N'Phạm Thị D', '001090000004', '0912345674', 'd@gmail.com', '2000-04-04', 1),
('HK0000000005', N'Hoàng Văn E', '001090000005', '0912345675', 'e@gmail.com', '1970-05-05', 0),
('HK0000000006', N'Đặng Thị F', '001090000006', '0912345676', 'f@gmail.com', '1992-06-06', 0),
('HK0000000007', N'Bùi Văn G', '001090000007', '0912345677', 'g@gmail.com', '1988-07-07', 0),
('HK0000000008', N'Võ Thị H', '001090000008', '0912345678', 'h@gmail.com', '1998-08-08', 1),
('HK0000000009', N'Lý Văn I', '001090000009', '0912345679', 'i@gmail.com', '1955-09-09', 0),
('HK0000000010', N'Mai Thị J', '001090000010', '0912345680', 'j@gmail.com', '2002-10-10', 1);

-- 4. KhuyenMai (Stay as is)
INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, trangThai, moTaChiTiet, loaiHanhKhach) VALUES
('KM-0526-0001', N'Giảm giá cho Học sinh - Sinh viên', 1, N'Giảm 10% giá vé', 'HOC_SINH_SINH_VIEN'),
('KM-0526-0002', N'Giảm giá cho Người cao tuổi', 1, N'Giảm 15% giá vé', 'NGUOI_CAO_TUOI'),
('KM-0526-0003', N'Giảm giá cho Trẻ em', 1, N'Giảm 25% giá vé', 'TRE_EM_6_DEN_10');

-- 5. Ga
INSERT INTO Ga (maGa, tenGa, diaChi) VALUES
('G_00HAN', N'Ga Hà Nội', N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội'),
('G_00DAN', N'Ga Đà Nẵng', N'791 Hải Phòng, Thanh Khê, Đà Nẵng'),
('G_00SGO', N'Ga Sài Gòn', N'01 Nguyễn Thông, Quận 3, TP.HCM'),
('G_00HP', N'Ga Hải Phòng', N'Lương Khánh Thiện, Ngô Quyền, Hải Phòng'),
('G_00NT', N'Ga Nha Trang', N'17 Thái Nguyên, Phước Tân, Nha Trang');

-- 6. Tau (10 rows)
INSERT INTO Tau (maTau, tenTau, trangThai, ghiChu) VALUES
('SE1', N'Tàu Bắc Nam SE1', 'HOAT_DONG', N'Tàu chất lượng cao'),
('SE2', N'Tàu Bắc Nam SE2', 'HOAT_DONG', N'Tàu chạy đêm'),
('SE3', N'Tàu Bắc Nam SE3', 'HOAT_DONG', N'Tàu giường nằm'),
('SE4', N'Tàu Bắc Nam SE4', 'HOAT_DONG', N'Tàu nhanh'),
('SE5', N'Tàu Bắc Nam SE5', 'HOAT_DONG', N'Tàu Thống Nhất'),
('SE6', N'Tàu Bắc Nam SE6', 'BAO_TRI', N'Đang bảo dưỡng'),
('SE7', N'Tàu Bắc Nam SE7', 'HOAT_DONG', N'Tàu mới'),
('SE8', N'Tàu Bắc Nam SE8', 'HOAT_DONG', N'Tàu khách'),
('TN1', N'Tàu Thống Nhất 1', 'HOAT_DONG', N'Tàu truyền thống'),
('TN2', N'Tàu Thống Nhất 2', 'BAO_TRI', N'Bảo trì định kỳ');

-- 7. ToaTau
INSERT INTO ToaTau (maToaTau, soToa, soLuongGhe, loaiToa, heSoLoaiToa, maTau) VALUES
('Toa01SE1', 1, 40, 'TOA_THUONG', 1.0, 'SE1'),
('Toa02SE1', 2, 30, 'TOA_VIP', 1.5, 'SE1'),
('Toa01SE2', 1, 40, 'TOA_THUONG', 1.0, 'SE2');

-- 8. Ghe
INSERT INTO Ghe (maGhe, loaiGhe, maToaTau) VALUES
('G01', 'GHE_MEM', 'Toa01SE1'),
('G02', 'GHE_MEM', 'Toa01SE1'),
('G03', 'GHE_MEM', 'Toa01SE1'),
('G04', 'GHE_MEM', 'Toa01SE1'),
('G05', 'GHE_MEM', 'Toa01SE1'),
('G10', 'GIUONG_NAM', 'Toa02SE1'),
('G11', 'GIUONG_NAM', 'Toa02SE1'),
('G20', 'GHE_MEM', 'Toa01SE2');

-- 9. ChuyenTau (10 rows)
INSERT INTO ChuyenTau (maChuyen, thoiGianKhoiHanh, thoiGianDuKien, gaDi, gaDen, maTau, ghiChu) VALUES
('CT001', '2026-06-01 06:00:00', '2026-06-02 10:00:00', 'G_00HAN', 'G_00SGO', 'SE1', N'Chuyến nhanh'),
('CT002', '2026-06-01 08:00:00', '2026-06-02 12:00:00', 'G_00SGO', 'G_00HAN', 'SE2', N'Chuyến về'),
('CT003', '2026-06-01 19:00:00', '2026-06-02 05:00:00', 'G_00HAN', 'G_00DAN', 'SE3', N'Chuyến đêm'),
('CT004', '2026-06-01 22:00:00', '2026-06-02 08:00:00', 'G_00DAN', 'G_00HAN', 'SE4', N'Chuyến sáng'),
('CT005', '2026-06-02 06:00:00', '2026-06-03 10:00:00', 'G_00HAN', 'G_00SGO', 'SE5', N'Thống nhất'),
('CT006', '2026-06-02 14:00:00', '2026-06-02 16:30:00', 'G_00HAN', 'G_00HP', 'SE7', N'Hải Phòng'),
('CT007', '2026-06-02 17:00:00', '2026-06-02 19:30:00', 'G_00HP', 'G_00HAN', 'SE8', N'Về Hà Nội'),
('CT008', '2026-06-03 08:00:00', '2026-06-03 20:00:00', 'G_00SGO', 'G_00DAN', 'SE1', N'Miền Trung'),
('CT009', '2026-06-03 20:00:00', '2026-06-04 08:00:00', 'G_00DAN', 'G_00SGO', 'SE1', N'Vào Nam'),
('CT010', '2026-06-04 06:00:00', '2026-06-05 10:00:00', 'G_00HAN', 'G_00SGO', 'SE5', N'Cuối tuần');

-- 10. HoaDon
INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) VALUES
('HD001', '2026-05-01 10:00:00', 'MP0002', 'HK0000000001', 1500000, 1500000, 'TIEN_MAT'),
('HD002', '2026-05-01 11:00:00', 'MP0002', 'HK0000000002', 800000, 1000000, 'TIEN_MAT');

-- 11. Ve (10 rows)
INSERT INTO Ve (maVe, ngayMua, loaiVe, trangThaiVe, giaVe, maGhe, maChuyen, maKH) VALUES
('V001', '2026-05-01 10:00:00', 'MOT_CHIEU', 'DA_THANH_TOAN', 500000, 'G01', 'CT001', 'HK0000000001'),
('V002', '2026-05-01 10:00:00', 'MOT_CHIEU', 'DA_THANH_TOAN', 500000, 'G02', 'CT001', 'HK0000000001'),
('V003', '2026-05-01 11:00:00', 'KHU_HOI', 'DA_THANH_TOAN', 800000, 'G05', 'CT002', 'HK0000000002'),
('V004', '2026-05-02 09:00:00', 'MOT_CHIEU', 'CHO_THANH_TOAN', 400000, 'G10', 'CT003', 'HK0000000003'),
('V005', '2026-05-02 09:30:00', 'MOT_CHIEU', 'CHO_THANH_TOAN', 400000, 'G11', 'CT003', 'HK0000000004'),
('V006', '2026-05-02 10:00:00', 'MOT_CHIEU', 'DA_THANH_TOAN', 600000, 'G20', 'CT004', 'HK0000000005'),
('V007', '2026-05-03 08:00:00', 'MOT_CHIEU', 'DA_HUY', 500000, 'G01', 'CT005', 'HK0000000006'),
('V008', '2026-05-03 09:00:00', 'MOT_CHIEU', 'DA_THANH_TOAN', 300000, 'G02', 'CT006', 'HK0000000007'),
('V009', '2026-05-04 14:00:00', 'MOT_CHIEU', 'DA_THANH_TOAN', 300000, 'G03', 'CT007', 'HK0000000008'),
('V010', '2026-05-04 15:00:00', 'KHU_HOI', 'DA_THANH_TOAN', 900000, 'G04', 'CT008', 'HK0000000009');

